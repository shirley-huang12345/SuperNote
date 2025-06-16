package com.asus.supernote.sync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;

import com.asus.supernote.R;
import com.asus.supernote.WebStorageException;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.editable.attacher.BitmapAttacher;
import com.asus.supernote.picker.NoteBookPickerActivity;

public class SyncBookCompareFun {
	 private static ArrayList<SyncBookItem> mSyncBookList_local = null;

	 public static ArrayList<SyncBookItem> mSyncBookList_remote = null;
	 
	 private static ArrayList<SyncBookItem> mSyncBookCoverModifyList_local = null;//darwin

	 public static ArrayList<SyncBookItem> mSyncBookCoverModifyList_remote = null;//darwin
	 
	 private ContentResolver mcontentResolver;
	 private SyncTableInfo   mtableinfo;
	 private Context mContext = null;
	 private SyncFilesWorkTask mtask;

	 public SyncBookCompareFun(ContentResolver contentresolver, Context context)
	 {
		 mcontentResolver = contentresolver;
		 mContext = context;
	 }
	 
	 // BEGIN: Better
	 private boolean processExceptionPages(SyncPageCompareFun pageCompareFun) throws WebStorageException {
		// process pages whose attributes have disappeared
		boolean isReloadLocalList = false;
		BookCase bookcase = BookCase.getInstance(mContext);
		for (SyncPageItem item : MetaData.SyncExceptionList) {
			String ver = item.getVersion();
			if ((ver != null) && (!SyncHelper.isVersionSupported(ver))) { // skip pages whose version is not supported
				continue;
			}
			Long id = item.getPageId();
			Cursor cursor = mcontentResolver.query(MetaData.PageTable.uri, null,
					MetaData.SYNC_CUSTOM_DB_QUERY_TAG + "created_date = ?",
					new String[] { Long.toString(id) },
					null);
			int count = cursor.getCount();
			if (count > 0) {
				LocalPageItem pageItem = new LocalPageItem();
				cursor.moveToFirst();
				pageItem.load(cursor);
				if (pageItem.isDeleted()) { // local exists but deleted
					mcontentResolver.delete(MetaData.PageTable.uri, 
	    					 "created_date = ?", new String[] { Long.toString(id) });
					count = 0;
				}
			}
			cursor.close();
		    if (count <= 0) { // local not exists or deleted, download remote page and then upload it to restore attributes
		    	SyncBookItem bookInfo = null;
		    	for (SyncBookItem bookItem : mSyncBookList_remote) {
		    		if (bookItem.getPageOrderList().contains(id)) {
		    			bookInfo = bookItem;
		    			break;
		    		}
		    	}
		    	if (bookInfo != null) { // owner id of the page exists in book.info
		    		Long bookId = bookInfo.getBookId();
		    		item.setBookmark(bookInfo.getBookmarksList().contains(id));
		    		item.setLastSyncOwnerBookId(bookId);
		    		item.setNotebookBakColor(bookInfo.getColor());
		    		item.setNotebookGridLine(bookInfo.getGridLine());
		    		item.setNotebookPhoneMemo(bookInfo.isPhoneMemo());
		    		item.setNotebookTemplate(bookInfo.getTemplate());
		    		item.setNotebookIndexLanguage(bookInfo.getIndexLanguage());//RICHARD
		    		item.setNotebookTitle(bookInfo.getTitle());
		    		bookInfo.setDeleted(false);
		    		
		    		if (bookcase.getNoteBook(bookInfo.getBookId()) == null) { // local not exists
		    			 mcontentResolver.delete(MetaData.BookTable.uri, 
		    					 "created_date = ?", new String[] { Long.toString(bookId) });
		    			
		        		NoteBook notebook = new NoteBook(mContext, bookInfo);
		        		notebook.setUserId(SyncFilesWorkTask.mSyncUserId);
		        	  	if(bookInfo.isLocked())
		             	{
		             		MetaData.lockBookIdList.add(bookId);
		             	} else {
		             		MetaData.lockBookIdList.remove(bookInfo.getBookId());
		             	}
		 		
		         		bookcase.addNewBookFromWebPage(notebook);
		    		}
		    	} else { // owner id of the page not exists in book.info, place the page into a default book
		    		Long defaultId = pageCompareFun.isPad() ? MetaData.SyncExceptionPadBookId : MetaData.SyncExceptionPhoneBookId;
		    		if (defaultId <= 0) {
		    			defaultId = System.currentTimeMillis() + SyncHelper.SYNC_PAGE_ID_BASE;
		    		}
		    		if (defaultId > 0) {
		    			item.setLastSyncOwnerBookId(defaultId);
		    		}
		    	}
		    	
		    	isReloadLocalList = true;
		    	
		    	if ((ver == null) 
		    			|| (!ver.equalsIgnoreCase(MetaData.SYNC_VERSION_3_1)) && (!ver.equalsIgnoreCase(MetaData.SYNC_VERSION_2))) { // for version '3.2' or null
		    		// download remote page
		    		pageCompareFun.mCurrentVersion = MetaData.SYNC_CURRENT_VERSION;
					pageCompareFun.mFullLocalPageChanged = true;
					pageCompareFun.findDownloadElement(id);
					if (pageCompareFun.downloadRemotePageElement(null, item, pageCompareFun, null)) {
						pageCompareFun.downloadRemotePageNotAddPage(null, item);
						pageCompareFun.unpackRemotePageElement(null, item, pageCompareFun);
						pageCompareFun.insertDoodleItemAttachmentDB(id, item.getLastSyncOwnerBookId());
					}
					pageCompareFun.mFullLocalPageChanged = false;
					
					SyncPageFile.mListAttachmentModify.clear();
					
					// upload the page downloaded to restore its attributes
					cursor = mcontentResolver.query(MetaData.PageTable.uri, null,
							MetaData.SYNC_CUSTOM_DB_QUERY_TAG + "created_date = ?",
							new String[] { Long.toString(id) },
							null);
					cursor.moveToFirst();
					if (!cursor.isAfterLast()) {
						LocalPageItem pageItem = new LocalPageItem();
						pageItem.load(cursor);
						pageCompareFun.mFullPageChanged = true;
						pageCompareFun.findUploadElement(id);
						if (pageCompareFun.uploadLocalPageElement(pageItem, null)) {
							if (pageCompareFun.uploadLocalPage(pageItem, null, MetaData.SYNC_CURRENT_VERSION)) {
								pageCompareFun.finishUploadPageElement(pageItem, null);
							}
						}
						pageCompareFun.mFullPageChanged = false;
					}
					cursor.close();
					
					SyncPageFile.mListAttachmentModify.clear();
				} else if (ver.equalsIgnoreCase(MetaData.SYNC_VERSION_3_1) || ver.equalsIgnoreCase(MetaData.SYNC_VERSION_2)) { // for version '3.1' or '2'
					// download remote page
					pageCompareFun.mCurrentVersion = ver;
					pageCompareFun.downloadRemotePage(null, item);
					
					// upload the page downloaded to restore its attributes
					cursor = mcontentResolver.query(MetaData.PageTable.uri, null,
							MetaData.SYNC_CUSTOM_DB_QUERY_TAG + "created_date = ?",
							new String[] { Long.toString(id) },
							null);
					cursor.moveToFirst();
					if (!cursor.isAfterLast()) {
						LocalPageItem pageItem = new LocalPageItem();
						pageItem.load(cursor);
						pageCompareFun.uploadLocalPage(pageItem, null);
					}
					cursor.close();
				}
		    } else { // local exists but not modified, sync failed
		    	MetaData.SyncFailedPageCount++;
		    }
		}
		
		if (isReloadLocalList) {
			mSyncBookList_local.clear();
			mSyncBookCoverModifyList_local.clear();
			return GetBookListFromDB();
		}
		
		return false;
	 }
	 // END: Better
	 
	 //darwin
	 
	 public static void packCover(long bookId) throws IOException 
	{
		String destDirPath = MetaData.DATA_DIR ;
		String srcDirPath = MetaData.DATA_DIR + Long.toString(bookId) ;
		File destFile = new File(destDirPath, bookId + MetaData.THUMBNAIL_COVER_CROP);
		File srcFile = new File(srcDirPath, bookId + MetaData.THUMBNAIL_COVER_CROP);
		if (srcFile.exists()) {
			if (destFile.exists()) {
				destFile.delete();
			}
			
			try {
				if (destFile.createNewFile()) 
				{
					InputStream in = null;
					in = new BufferedInputStream(new FileInputStream(srcFile));
					OutputStream out = null;
					out = new BufferedOutputStream(new FileOutputStream(destFile));
					byte[] buffer = new byte[4096];
					int bytes;
					while ((bytes = in.read(buffer)) != -1) {
						out.write(buffer, 0, bytes);
					}
					in.close();
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	 
	 static public void changeBookCoverThumb(long bookId ,Bitmap bitmap)
	    {
	    	try {
	             if (bitmap != null) {
	                File file = new File(MetaData.DATA_DIR + Long.toString(bookId), MetaData.THUMBNAIL_COVER);
	                if (file.exists() == false) {
	                    file.createNewFile();
	                }
	                FileOutputStream fos = new FileOutputStream(file);
	                BufferedOutputStream bos = new BufferedOutputStream(fos);
	                bitmap.compress(Bitmap.CompressFormat.PNG, MetaData.THUMBNAIL_QUALITY, bos);
	                bitmap.recycle();
	                bitmap = null;
	                bos.close();
	                fos.close();
	                //Begin Allen for update widget 
	        		if(!MetaData.SuperNoteUpdateInfoSet.containsKey(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_EDIT_CORVER)){
	        			MetaData.SuperNoteUpdateInfoSet.put(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_EDIT_CORVER,null);
	        		}
	        		//End Allen
	            }
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	 
	 public void createCover(long bookId)
	 {
		 String srcDirPath =  MetaData.DATA_DIR + Long.toString(bookId) ;
		 String destDirPath = MetaData.DATA_DIR + Long.toString(bookId) ;
		 File destFile = new File(destDirPath, bookId + MetaData.THUMBNAIL_COVER_CROP);
		 File srcFile = new File(srcDirPath, bookId + MetaData.THUMBNAIL_COVER_CROP);
		 Bitmap b = BitmapAttacher.saveBitmap(srcFile, destFile, Bitmap.CompressFormat.JPEG);
	     int width = (int)mContext.getResources().getDimension(R.dimen.change_cover_crop_width);
	     int height = (int)mContext.getResources().getDimension(R.dimen.change_cover_crop_height);
		 Bitmap newBitmap = NoteBookPickerActivity.setBitmapMask(b, width, height);
		 changeBookCoverThumb(bookId,newBitmap);
	 }
	 
	 public static void unpackCover(long bookId) throws IOException 
	{
		String srcDirPath = MetaData.DATA_DIR ;
		String destDirPath = MetaData.DATA_DIR + Long.toString(bookId) ;
		File destFile = new File(destDirPath, bookId + MetaData.THUMBNAIL_COVER_CROP);
		File srcFile = new File(srcDirPath, bookId + MetaData.THUMBNAIL_COVER_CROP);
		if (srcFile.exists()) {
			if (destFile.exists()) {
				destFile.delete();
			}
			
			try {
				if (destFile.createNewFile()) 
				{
					InputStream in = null;
					in = new BufferedInputStream(new FileInputStream(srcFile));
					OutputStream out = null;
					out = new BufferedOutputStream(new FileOutputStream(destFile));
					byte[] buffer = new byte[4096];
					int bytes;
					while ((bytes = in.read(buffer)) != -1) {
						out.write(buffer, 0, bytes);
					}
					in.close();
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	srcFile.delete();
	}
	 
	 public void uploadBookCover(long bookid)throws Exception 
	 {		 
		String strPageFilePath = MetaData.DATA_DIR + bookid + MetaData.THUMBNAIL_COVER_CROP;
		String attribute = "";

		try {
			packCover(bookid);
			String fileSha = mtableinfo.getFileSha(strPageFilePath); // Better
			if (MetaData.webStorage.uploadOneFile(strPageFilePath, attribute, null, fileSha) != 1) {
				throw new WebStorageException(WebStorageException.OTHER_ERROR);
			} else {

			}
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: {
			}
				break;
			default: {
				throw webex;
			}
			}
		}
		File tempFile = new File(strPageFilePath);
		if (tempFile.exists()) {
			tempFile.delete();
		}

	 }
	 
	 public void downloadBookCover( long bookid)throws WebStorageException 
	 {
		String tempDownloadDirPath = MetaData.DATA_DIR ;
		File tempDownloadDir = new File(tempDownloadDirPath);
		if (!tempDownloadDir.exists()) {
			tempDownloadDir.mkdirs();
		}
		String strPageFileName = bookid + MetaData.THUMBNAIL_COVER_CROP;
		File tempFile = new File(tempDownloadDirPath + strPageFileName);
		if (tempFile.exists()) {
			tempFile.delete();
		}
		try {
			if (MetaData.webStorage.downloadOneFile(strPageFileName,
					tempDownloadDirPath + strPageFileName) != 1) {
				throw new WebStorageException(WebStorageException.OTHER_ERROR);
			} else {
				
			}
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: 
			}
		}
		
		try{
		
			unpackCover(bookid);
			createCover(bookid);
		}
		catch(Exception e){
			
		}
			
	 }
	 
	 public void changeBookCover(Long bookid,int coverIndex)throws Exception
	 {
		 Bitmap bitmap = NoteBook.getDefaultNoteBookThumbnail(coverIndex, mContext);
		 if (bitmap != null) {
			 File dir = new File(MetaData.DATA_DIR);
	        dir.mkdir();
	        File bookDir = new File(MetaData.DATA_DIR, Long.toString(bookid));
	        bookDir.mkdir();
             File file = new File(bookDir.getPath(), MetaData.THUMBNAIL_COVER);
             if (file.exists() == false) {
                 file.createNewFile();
             }
             FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             bitmap.compress(Bitmap.CompressFormat.PNG, MetaData.THUMBNAIL_QUALITY, bos);
             bitmap.recycle();
             bitmap = null;
             bos.close();
             fos.close();
         }
	 }
	 
	 public void CompareBookCover()
	 {
		 for(SyncBookItem syncitem : mSyncBookList_remote)
		 {
			 for(SyncBookItem localitem : mSyncBookCoverModifyList_local)
			 {
				 if(localitem.getBookId() == syncitem.getBookId())
				 {
					 try
					 {
						 if(localitem.mIndexCover == 1)
						 {
							 uploadBookCover(localitem.getBookId());
						 }
					 }
					 catch(Exception e)
					 {
						 
					 }
					 syncitem.mIsChecked = true;
				 }
			 }
			 if(syncitem.mIsChecked == false)
			 {
				 for(SyncBookItem localitem : mSyncBookList_local)
				 {
					 if(localitem.getBookId() == syncitem.getBookId())
					 {
						 if(localitem.getLastSyncModifiedTime() != syncitem.getLastSyncModifiedTime())
						 {
							 try
							 {
								 if(syncitem.mIndexCover == 1)
								 {
									 downloadBookCover(localitem.getBookId());
								 }
								 else
								 {
									 changeBookCover(localitem.getBookId(),syncitem.mIndexCover);
								 }
							 }
							 catch(Exception e)
							 {
								 
							 }
							 
						 }
					 }
				 }
			 }
		 }
		 
	 }
	 //darwin
	 
	 public String CompareAndSyncBookList(SyncFilesWorkTask task, String shaformconfig, 
			 SyncPageCompareFun pageCompareFun) throws Exception
	 {
		 mSyncBookList_local = new ArrayList<SyncBookItem>();	
		 mSyncBookCoverModifyList_local = new ArrayList<SyncBookItem>();//darwin
		 mtableinfo = new SyncTableInfo();
		 
		 MetaData.webStorage.setTimeout(2 * 60 * 1000);
		 mtask = task;
		 String sha = null;
		 String version = null; // Better
		 boolean isFileExist = false; // Better
		 if (MetaData.webStorage.isFileExistOnEeeStorage(MetaData.SYNC_TABLE_INFO_FILE_NAME)) {
			 mtableinfo.downloadTableInfoAttribute();
			 sha = mtableinfo.getTableInfoSha();
			 version = mtableinfo.getVersion(); // Better
			 isFileExist = true; // Better
		 }
		 
		 if (!isFileExist || (version == null) || SyncHelper.isVersionSupported(version)) { // Better
			 boolean bookinfo_ismodify = GetBookListFromDB();
			 
			 mSyncBookList_remote = (ArrayList<SyncBookItem>) mtableinfo.getBookList();	
			 
			 if(sha !=null && sha.equals(shaformconfig))
			 {
				 // BEGIN: Better
				 if (MetaData.SyncExceptionList.size() > 0) {
					 GetBookListFromWebstroage();
					 bookinfo_ismodify = processExceptionPages(pageCompareFun);
				 }
				 // END: Better
 
				if(!bookinfo_ismodify)
					return null;
				else
				{
					for(SyncBookItem localitem:mSyncBookList_local)
					{	
						/**
						 * case 11: local is modified, remote is not modified.
						 * see also: sync book flow chart.
						 */
			   			FunNo2(localitem);
			   			   
					}
					 mtableinfo.saveTableInfo();
					 String newsha = mtableinfo.uploadTableInfo();
					//darwin
					 CompareBookCover();
					 //darwin
					 return newsha;
				}				 					
			 }
			 
			GetBookListFromWebstroage();// maybe catch error
			
			processExceptionPages(pageCompareFun); // Better
			 
			 CompareAndSync();			 
			 mtableinfo.saveTableInfo();
			 String newsha = mtableinfo.uploadTableInfo();
			 
			 
			 //darwin
			 CompareBookCover();
			 //darwin
			 
			 return newsha;
		 } else { // Better
			MetaData.IsFileVersionCompatible = false;
			if (!MetaData.NotSupportedVersionList.contains(version)) {
				MetaData.NotSupportedVersionList.add(version);
			}
			
			return null;
		 }
	 }
	 
     private boolean GetBookListFromWebstroage()throws Exception
     {

    	 try
		 {		
    		Log.v("wendy","downloadTableInfo: " );
    		mtableinfo.downloadTableInfo();
    		mtableinfo.loadTableInfo();
    		
			 return true;
		 }	
		 catch(WebStorageException e)
		 {
			if( e.getErrorKind()!=WebStorageException.FILE_NOT_EXIST)
			{
				return true;
			}
			else
			{
				throw e;
			}
		 }  	  
     }
     
     private boolean GetBookListFromDB()
     {
    	 boolean localbookinfo_ismodify = false;
         Cursor cursor = mcontentResolver.query(MetaData.BookTable.uri, null, 
                 MetaData.SYNC_CUSTOM_DB_QUERY_TAG + 
                 "userAccount = ?", new String[] {
                 Long.toString(SyncFilesWorkTask.mSyncUserId)}, null);
         cursor.moveToFirst(); 
         while (!cursor.isAfterLast()) {
      	   SyncBookItem syncbook = new SyncBookItem();
      	   syncbook.load(cursor);
      	   syncbook.getBookMarklist(mcontentResolver, syncbook.getBookId());
      	   if(!localbookinfo_ismodify){
      		   localbookinfo_ismodify = syncbook.IsModify(syncbook.getCurModifiedTime());
      	   }
      	   mSyncBookList_local.add(syncbook);
      	   //darwin
      	   boolean isModify = syncbook.IsModify(syncbook.getCoverModifiedTime());
      	   if(isModify)
      	   {
      		   mSyncBookCoverModifyList_local.add(syncbook);
      	   }
      	   //darwin
           cursor.moveToNext();
         }
         cursor.close();
         return localbookinfo_ismodify;
        
     }
     private void CompareAndSync()
     {
  	   if(mSyncBookList_local == null && mSyncBookList_remote == null)
  	   {
  		   return;
  	   }
  	   boolean local_book_isdel = false;
  	   boolean local_book_ismodify = false;    
  	   
  	   boolean remote_book_isdel = false;    	   
  	   boolean remote_book_ismodify = false;
  	   
  	  ArrayList<SyncBookItem> remote_book_templist = null;
  	  if(mSyncBookList_remote!=null)
  	  {
  		remote_book_templist = (ArrayList<SyncBookItem>)mSyncBookList_remote.clone();
  	  }
   	  
  	  for( SyncBookItem local_item_temp: mSyncBookList_local )
   	  {
   		  local_book_isdel = local_item_temp.isDeleted();
   		  local_book_ismodify = local_item_temp.IsModify(local_item_temp.getCurModifiedTime());
   		  SyncBookItem remote_item_temp = getBookItem(remote_book_templist, local_item_temp.getBookId());
    		
   		  if(remote_item_temp == null ){ //only exsit in local list 
   			  if(local_book_isdel) {
   				/**
   				 * case 1: book only in local, and is deleted. 
   				 * see also: sync book flow chart.
   				 */
   				FunNo1(local_item_temp);
   			  }
   			  else{
   				  /**
   				   * case 2: book only in local, and is not deleted.
   				   * see also: sync book flow chart.
   				   */
   				  FunNo2(local_item_temp);
   			  } 
   			 continue;
   		  }
   		 
   		 remote_book_isdel = remote_item_temp.isDeleted();
   		 remote_book_ismodify = remote_item_temp.IsModify(local_item_temp.getLastSyncModifiedTime());
   		 
   		 if(remote_book_isdel && local_book_isdel) {
   			 /**
   			  * case 3: book in local and remote are both deleted.
   			  * see also: sync book flow chart.
   			  */
   			 FunNo1(local_item_temp);
   			 continue;
   		 }
   		 
   		 byte result = 0;
   		 byte del_l = (byte) ( local_book_isdel ? 1 : 0 );
   		 byte del_r = (byte) ( remote_book_isdel ? 1 : 0 );
   		 byte mod_l = (byte) ( local_book_ismodify ? 1: 0 );
   		 byte mod_r = (byte) ( remote_book_ismodify ? 1 : 0);
   		 
   		result = (byte) (del_l << 3 | del_r << 2 | mod_l << 1 | mod_r);
   		
   	   switch(result)
		   {
		   case 0x06://6 0110
		   case 0x07://7 0111
			   /**
			    * case 4: remote is deleted. local is modified.
			    * see also: sync book flow chart.
			    */
			   FunNo4(local_item_temp);
			   break;
		   case 0x04://04 0100
		   case 0x05://05 0101	   
			   /**
			    * case 5: remote is deleted. local is not  modified.
			    * see more: FunNo7( flow 5.1, 5.2 )
			    * see also: sync book flow chart.
			    */
			   FunNo7(local_item_temp);
			   break;
		   case 0x09://9 10 0 1
		   case 0x0b://11 10 1 1 , 09or 11	mol_l is not  important  
			   /**
			    * case 6: local is deleted. remote is modified.
			    * see also: sync book flow chart.
			    */
			   FunNo4(local_item_temp);
			   Log.v("wendy","No.5");
			   break;
		   case 0x08://8 1000
		   case 0x0a://10 1010
			   /**
			    * case 7: local is deleted. remote is not modified.
			    * see also: sync book flow chart.
			    */
			   FunNo4(local_item_temp);
			   Log.v("wendy","No.4");
			   break;
		   case 0x01://01 0001
			   /**
			    * case 8: local is not modified. remote is modified.
			    * see also: sync book flow chart.
			    */
			   FunNo8(remote_item_temp,local_item_temp);
			   break;
		  
		   case 0x02://02 0010			  
		   case 0x03://03 0011
			   /**
			    * case 9: local is modified.
			    * see also: sync book flow chart.
			    */
			   FunNo4(local_item_temp);
			   break;    			   
		   }
   		remote_book_templist.remove(remote_item_temp);   		  
   	  }
   	  
   	  for(SyncBookItem bookitem:remote_book_templist)
   	  {
   		  if(!bookitem.isDeleted()) {
   			/**
   			 * case 10: book only in remote, and is modified.
   			 * see also: sync book flow chart.
   			 */
   			FunNo9(bookitem);
   		  }
   	  } 

     }
     private void updatelocalitemproperties(SyncBookItem remoteitem)
     {
    	ContentValues cvbook = new ContentValues();
     	cvbook.put(MetaData.BookTable.TITLE, remoteitem.getTitle());
     	cvbook.put(MetaData.BookTable.CREATED_DATE, remoteitem.getBookId());
     	cvbook.put(MetaData.BookTable.IS_LOCKED, remoteitem.isLocked()? 1 : 0);
     	cvbook.put(MetaData.BookTable.BOOK_SIZE, remoteitem.getPageSize());
     	cvbook.put(MetaData.BookTable.BOOK_COLOR, remoteitem.getColor());
     	cvbook.put(MetaData.BookTable.BOOK_GRID, remoteitem.getGridLine());
     	cvbook.put(MetaData.BookTable.IS_DELETED, remoteitem.isDeleted()? 1 : 0);  
     	cvbook.put(MetaData.BookTable.MODIFIED_DATE, remoteitem.getLastSyncModifiedTime());
     	cvbook.put(MetaData.BookTable.LASTSYNC_MODIFYTIME, remoteitem.getLastSyncModifiedTime()); 
     	cvbook.put(MetaData.BookTable.TEMPLATE, remoteitem.getTemplate());//wendy allen++ for template 0706
     	cvbook.put(MetaData.BookTable.INDEX_LANGUAGE, remoteitem.getIndexLanguage());//Allen
     	cvbook.put(MetaData.BookTable.INDEX_COVER, remoteitem.getIndexCover());//darwin
     	cvbook.put(MetaData.BookTable.COVER_MODIFYTIME, remoteitem.getCurModifiedTime());//darwin
     	mcontentResolver.update(MetaData.BookTable.uri, cvbook,"created_date = ?", new String[] { Long.toString(remoteitem.getBookId())});
     	remoteitem.setCurModifiedTime(remoteitem.getLastSyncModifiedTime());
     	addBookTobookcase(remoteitem);	
     	
     }
    private void updatelocalitemOrderlist(List<Long> list, Long bookid)
    {
     	 ContentValues cvbook = new ContentValues(); 
    	 ByteArrayOutputStream baos = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(baos);
         try {
              for (long value : list) {
                  dos.writeLong(value);
              }
              cvbook.put(MetaData.BookTable.PAGE_ORDER, baos.toByteArray());
              dos.close();
              baos.close();
          }
          catch (IOException e) {
              e.printStackTrace();
          }
          mcontentResolver.update(MetaData.BookTable.uri, cvbook,"created_date = ?", new String[] { Long.toString(bookid)});
   	 
    }
     private void FunNo9(SyncBookItem remoteitem)
     { 
    	  //1.add local item properties
     	ContentValues cvbook = new ContentValues();
     	cvbook.put(MetaData.BookTable.TITLE, remoteitem.getTitle());
     	cvbook.put(MetaData.BookTable.CREATED_DATE, remoteitem.getBookId());
     	cvbook.put(MetaData.BookTable.IS_LOCKED, remoteitem.isLocked()? 1 : 0);
     	cvbook.put(MetaData.BookTable.BOOK_SIZE, remoteitem.getPageSize());
     	cvbook.put(MetaData.BookTable.BOOK_COLOR, remoteitem.getColor());
     	cvbook.put(MetaData.BookTable.BOOK_GRID, remoteitem.getGridLine());
     	cvbook.put(MetaData.BookTable.IS_DELETED, remoteitem.isDeleted()? 1 : 0);  
     	//cvbook.put(MetaData.BookTable.BOOK_SIZE, MetaData.PAGE_SIZE_PHONE);
     	cvbook.put(MetaData.BookTable.USER_ACCOUNT,MetaData.CurUserAccount);
     	cvbook.put(MetaData.BookTable.TEMPLATE, remoteitem.getTemplate());//wendy allen++ for template 0706
     	cvbook.put(MetaData.BookTable.INDEX_LANGUAGE, remoteitem.getIndexLanguage()); //Allen 	
     	cvbook.put(MetaData.BookTable.INDEX_COVER, remoteitem.getIndexCover()); //darwin
     	cvbook.put(MetaData.BookTable.COVER_MODIFYTIME, remoteitem.getCurModifiedTime());//darwin
     	  //2.update orderlist
     	
     	List<Long> orderlist = remoteitem.getPageOrderList();
     	if(orderlist != null && orderlist.size() > 0)     	
     	{
     	
     	
     		orderlist.clear();
     		remoteitem.setLastSyncModifiedTime(System.currentTimeMillis());
        }
    	cvbook.put(MetaData.BookTable.MODIFIED_DATE, remoteitem.getLastSyncModifiedTime());
     	cvbook.put(MetaData.BookTable.LASTSYNC_MODIFYTIME, remoteitem.getLastSyncModifiedTime());     	 
     	mcontentResolver.insert(MetaData.BookTable.uri,cvbook);  
     	
     	//
     	addBookTobookcase(remoteitem);
         	
          //3.update marklist
     }
     private void addBookTobookcase(SyncBookItem remoteitem)
     {
    	 if(remoteitem.isDeleted()) return;
    		NoteBook notebook = new NoteBook(mContext, remoteitem);
    		notebook.setUserId(SyncFilesWorkTask.mSyncUserId);
    	  	if(remoteitem.isLocked())
         	{
         		MetaData.lockBookIdList.add(remoteitem.getBookId());
         	} else {
         		MetaData.lockBookIdList.remove(remoteitem.getBookId());
         	}

         	BookCase  bookcase = BookCase.getInstance(mContext);
         	if(bookcase !=null)
         		{	         		
         			bookcase.addNewBookFromWeb(notebook);	         		
	         		//mtask.publishStatus(null);
         		}
    	 
     }
     
     private void ModifyBookMarklist(List<Long> list)
     {
    	 for(Long pageid:list)
    	 {
    		 ContentValues cv = new ContentValues();
    		 cv.put(MetaData.PageTable.IS_BOOKMARK, 1);
    		 mcontentResolver.update(MetaData.PageTable.uri, cv, "created_date = ?", new String[]{pageid.toString()});
  		   	    
    	 }
     }
     
     private void FunNo8(SyncBookItem remoteitem, SyncBookItem localitem)
     {
    	 //2.reoderPagelist()
    	 boolean flag = reoderPagelist(remoteitem,localitem);
    	 resetBookMarks(remoteitem,localitem);
    	 //1.upate properties
    	 if (flag)
    	 {
    		 long modifytime = System.currentTimeMillis();
    		 remoteitem.setLastSyncModifiedTime(modifytime); 
    	 }
    	 updatelocalitemproperties(remoteitem);    	 
     }
     private boolean reoderPagelist(SyncBookItem remoteitem, SyncBookItem localitem)
     {
    	 List<Long> remotelist = remoteitem.getPageOrderList();
    	 List<Long> locallist = localitem.getPageOrderList();
    	 if(remotelist == null || locallist == null||remotelist.equals(locallist))
    		 return false;
    	 
    	 List<Long> newlist = new ArrayList<Long>();
    	 for(Long pageid:remotelist)
    	 {
    		 if(locallist.contains(pageid))
    		 {
    			 if(!newlist.contains(pageid))
    			 newlist.add(pageid);
    		 }
    		 else
    		 {
    			 Cursor cv =  mcontentResolver.query(MetaData.PageTable.uri, null, "created_date = ? And owner = ?", new String[]{pageid.toString(),Long.toString(localitem.getBookId())}, null);
    			 if(cv.getCount()>0&&!newlist.contains(pageid))
    			 {
    				 newlist.add(pageid);    				 
    			 }
    			 cv.close();
    		 }
    	 }
    	 for(Long localid:locallist)
    	 {
    		 if(!newlist.contains(localid))
    		 {
    			 int index = locallist.indexOf(localid);
    			 if(index > newlist.size()){
    				 index = newlist.size();
    			 }
    			 newlist.add(index, localid);
    		 }
    	 }    	 
    	 if(newlist.equals(remotelist))
    	 {
    		 updatelocalitemOrderlist(newlist, localitem.getBookId());
    		 return false;
    	 }
    	 remoteitem.setPageOrderList(newlist);
    	 updatelocalitemOrderlist(newlist, localitem.getBookId());
    	 return true;
     }
     private void  resetBookMarks(SyncBookItem remoteitem, SyncBookItem localitem)
     {
    	 List<Long> remotelist = remoteitem.getBookmarksList();
    	 List<Long> locallist = localitem.getBookmarksList();
    	 
		for (long markid : locallist) {
			if (remotelist == null || !remotelist.contains(markid)) {
				ContentValues cvs = new ContentValues();
				cvs.put(MetaData.PageTable.IS_BOOKMARK, 0);
				mcontentResolver.update(MetaData.PageTable.uri, cvs,
						"created_date = ?",
						new String[] { Long.toString(markid) });
				
			}			
		}
		for(long remotemarkid:remotelist)
		{
			if(locallist == null || !locallist.contains(remotemarkid))
			{
				ContentValues cvs = new ContentValues();
				cvs.put(MetaData.PageTable.IS_BOOKMARK, 1);
				mcontentResolver.update(MetaData.PageTable.uri, cvs,
						"created_date = ?",
						new String[] { Long.toString(remotemarkid) });
			}
		}

    	 
     }
     
     
     private void deleteDir(File dir)
     {
         if (dir.isDirectory()) {
             String[] subDirs = dir.list();
             for (String sub : subDirs) {
                 deleteDir(new File(dir, sub));
             }
         }
         dir.delete();
     }

	private void FunNo7(SyncBookItem localitem) {
		List<Long> orderlist = localitem.getPageOrderList();
		if (orderlist == null || orderlist.size() == 0) 
		{
			/**
			 * case 5.1: no page in book.
			 * see also: sync book flow chart.
			 */
			// 1.delete local file & book
			deleteDir(new File(MetaData.DATA_DIR, Long.toString(localitem
					.getBookId())));
			// delte from bookcase and change focus
			DeldeteBookAndChangeFocus(localitem.getBookId());//

			// 2.modify local book delete flag.
			ContentValues cvbook = new ContentValues();
			cvbook.put(MetaData.BookTable.IS_DELETED, 1);
			cvbook.put(MetaData.BookTable.LASTSYNC_MODIFYTIME,
					localitem.getCurModifiedTime());
			mcontentResolver.update(MetaData.BookTable.uri, cvbook,
					"created_date = ?",
					new String[] { Long.toString(localitem.getBookId()) });
		}else
		{
			/**
			 * case 5.2: have one or more pages in book.
			 * see also: sync book flow chart.
			 */
			FunNo4(localitem);//add by wendy
		}

	}
     
     private void DeldeteBookAndChangeFocus(Long bookid)
     {
         BookCase  bookcase = BookCase.getInstance(mContext);         
         if(bookcase == null) return;
         
         boolean  PrivateMode = NoteBookPickerActivity.islocked();//PickerActivity.isPrivateMode();
         String selection = PrivateMode ?  "(is_locked = 0)AND ((userAccount = 0) OR (userAccount = ?))":"(userAccount = 0) OR (userAccount = ?)";

         Cursor cursor = mcontentResolver.query(MetaData.BookTable.uri, null, selection, new String[]{Long.toString(SyncFilesWorkTask.mSyncUserId)}, null);
         cursor.moveToFirst();
         while (cursor.isAfterLast() == false) {
             int index = cursor.getPosition();
             if (cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE) == bookid.longValue()) {
                 boolean b = (index > 0) ? cursor.moveToPrevious() : cursor.moveToNext();
                 if (b == false) {
                	 //begin wendy
                    	 bookcase.setCurrentBook(BookCase.NO_SELETED_BOOK);
                	 //wendy end
                     break;
                 }
                 else {
                     long id = cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
                    bookcase.setCurrentBook(id);
                     break;
                 }
             }
             cursor.moveToNext();
         }
         cursor.close();
         bookcase.removeNoteBookById(bookid);
		 //mtask.publishStatus(null);
     }
     
     private void FunNo4(SyncBookItem localitem)
     {
    	 //1.remote item --> local item 
    	 SyncBookItem remote_item = getBookItem(mSyncBookList_remote, localitem.getBookId());     	 
    	 remote_item.ModifyBookItem(localitem);
    	//2.set remote last synctime = modify time
    	 remote_item.setLastSyncModifiedTime(localitem.getCurModifiedTime());
    	// remote_item.
    	 
    	 //2.set local synctime = modify time;
    	 modifyCurModifyTime(localitem);
    	 
     }
     private void FunNo2(SyncBookItem localitem)
     {    	
    	 //1.add new item to remote list SN_L,last sync time = modify time;
    	 SyncBookItem newItem = new SyncBookItem(localitem);
    	 newItem.setLastSyncModifiedTime(localitem.getCurModifiedTime());
    	 mSyncBookList_remote.add(newItem);    	 
    	 //2.modify LN : last sync time = modify time;    	
    	 modifyCurModifyTime(localitem);
     }
     private void modifyCurModifyTime(SyncBookItem item)
     {
    	 boolean modify = item.IsModify(item.getCurModifiedTime());
    	 if(!modify) return;
    	 
    	 ContentValues cv = new ContentValues();
    	 long curModifytime = item.getCurModifiedTime();
    	 cv.put(MetaData.BookTable.LASTSYNC_MODIFYTIME, curModifytime);
    	 mcontentResolver.update(MetaData.BookTable.uri, cv, "created_date = ?", new String[] { Long.toString(item.getBookId()) });
      	 
     }
     
     private void FunNo1(SyncBookItem localitem)
     {
    	 //1.delete info from db  ==> delete item from LN  
    	 mcontentResolver.delete(MetaData.BookTable.uri, "created_date = ?", new String[] { Long.toString(localitem.getBookId()) });//Long.toString(mCreatedTime)
        
     }
   
     private SyncBookItem getBookItem( ArrayList<SyncBookItem> list, long itemid)
     {
       if(list == null) return null;
  	   for(SyncBookItem tempitem: list)
  	   {
  		   if(tempitem.getBookId() == itemid)
  		   {
  			   return tempitem;
  		   }
  	   }
  	   return null;    	   
     }
     
}
