package com.asus.supernote.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.asus.supernote.R;
import com.asus.supernote.WebStorageException;
import com.asus.supernote.WebStorageHelper;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.picker.PageComparator;
import com.asus.supernote.picker.PickerUtility;
import com.asus.supernote.picker.SimplePageInfo;
import com.asus.supernote.data.PageDataLoader;

public class SyncPageCompareFun {
	public static final String TAG = "SyncPageCompareFun";
	
	private static ArrayList<LocalPageItem> mLocalPageList;
	private static ArrayList<SyncPageItem> mRemotePageList;
	private static ArrayList<SyncPageItem> mRemotePageListAll;
	private static ArrayList<SyncPageItem> mRemotePageListBookId;
	//begin darwin
	private static ArrayList<LocalPageItem> mLocalOwnerModifyPageList;
	List<LocalPageItem> mRemoteDoodleList;
	List<LocalPageItem> mRemoteItemList;
	List<LocalPageItem> mRemoteAttachmentList;
	//end   darwin

	private ContentResolver mContentResolver;
	private Context mContext;
	private BookCase mBookCase;

	private SyncFilesWorkTask mTask = null;
	
	public String mCurrentVersion = "";
	
	private static WebStorageHelper sWebStorageHelper = new WebStorageHelper();

	public SyncPageCompareFun(SyncFilesWorkTask task,
			ContentResolver contentResolver, Context context) {
		mContentResolver = contentResolver;
		mContext = context;
		mBookCase = BookCase.getInstance(mContext);
		mTask = task;
	}

	public void GetPageListFromWeb() throws Exception {
		mRemotePageList = (ArrayList<SyncPageItem>) SyncPageFile
				.getServerFileList();
	}
	//begin darwin
	private static String decodeBase64(String str) {
		String decodedStr;
		try {
			byte[] decodeBytes = Base64.decode(str, Base64.URL_SAFE);
			try {
				decodedStr = new String(decodeBytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				decodedStr = new String(decodeBytes);
			}
		} catch (Exception e) {
			decodedStr = "";
		}
		return decodedStr;
	}
	public void AddFileList(Node node,String strFileName)
	{
		SyncPageItem pageItem = new SyncPageItem();
		if (node != null) {
			boolean isValid = true;
			
			// version
			String version = null;
			String tmpVersion = sWebStorageHelper.getNodeValue(node, 
					MetaData.SYNC_FILE_VERSION);
			if ((tmpVersion == null) || tmpVersion.isEmpty()) {
				tmpVersion = sWebStorageHelper.getNodeValue(node, "VN");
				if ((tmpVersion != null) && !tmpVersion.isEmpty()) {
					version = tmpVersion;
				}
			} else {
				version = tmpVersion;
			}
			pageItem.setVersion(version);
			
			// id
			String strPageId = strFileName.substring(0,strFileName.length()- MetaData.SYNC_PAGE_FILE_EXTENSION.length());
			if ((strPageId == null) || strPageId.isEmpty()) {
				Log.e(TAG, "page id is null, file name '" + strFileName + "' is invalid");
				return;
			}
			try {
				Long pageId = Long.parseLong(strPageId);
				pageItem.setPageId(pageId);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "page id is invalid, file name '" + strFileName + "' is invalid");
				return;
			}
			
			// sha
			String sha = sWebStorageHelper.getNodeValue(
					node,
					MetaData.SYNC_PAGE_FILE_ATTR_SHA);
			if ((sha == null) || sha.isEmpty()) {
				Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_SHA + "' is null");
				isValid = false;
			} else {
				pageItem.setSha(sha);
			}
			
			// last sync modified time
			String strSyncTime = sWebStorageHelper.getNodeValue(
					node,
					MetaData.SYNC_PAGE_FILE_ATTR_LASTCHANGETIME);
			if ((strSyncTime == null) || (strSyncTime.isEmpty())) {
				Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_LASTCHANGETIME + "' is null");
				isValid = false;
				pageItem.setLastSyncModifiedTime(System.currentTimeMillis());
			} else {
				try {
					pageItem.setLastSyncModifiedTime(Long.parseLong(strSyncTime));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_LASTCHANGETIME 
							+ "' with value '" + strSyncTime + "' is invalid");
					isValid = false;
					pageItem.setLastSyncModifiedTime(System.currentTimeMillis());
				}
			}
			
			// is deleted
			String strDeleted = sWebStorageHelper.getNodeValue(
					node,
					MetaData.SYNC_PAGE_FILE_ATTR_ISDELETED);
			if ((strDeleted == null) || strDeleted.isEmpty()) {
				Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_ISDELETED + "' is null");
				isValid = false;
				pageItem.setDeleted(false);
			} else {
				try {
					pageItem.setDeleted(Boolean.parseBoolean(strDeleted));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_ISDELETED 
							+ "' with value '" + strDeleted + "' is invalid");
					isValid = false;
					pageItem.setDeleted(false);
				}
			}
			
			// last sync owner book id
			String strSyncOwnerId = sWebStorageHelper.getNodeValue(
					node,
					MetaData.SYNC_PAGE_FILE_ATTR_LASTNOTEBOOKID);
			if ((strSyncOwnerId == null) || strSyncOwnerId.isEmpty()) {
				Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_LASTNOTEBOOKID + "' is null");
				isValid = false;
				pageItem.setLastSyncOwnerBookId(-1);
			} else {
				try {
					pageItem.setLastSyncOwnerBookId(Long.parseLong(strSyncOwnerId));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_LASTNOTEBOOKID 
							+ "' with value '" + strSyncOwnerId + "' is invalid");
					isValid = false;
					pageItem.setLastSyncOwnerBookId(-1);
				}
			}
			
			// book title
			String strBookTitle = sWebStorageHelper.getNodeValue(
						node,
						MetaData.SYNC_PAGE_FILE_ATTR_NOTEBOOKTITLE);
			if ((strBookTitle == null) || strBookTitle.isEmpty()) {
				Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_NOTEBOOKTITLE + "' is null");
				pageItem.setNotebookTitle("");
			} else {
				try {
					pageItem.setNotebookTitle(decodeBase64(strBookTitle));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_NOTEBOOKTITLE 
							+ "' with value '" + strBookTitle + "' is invalid");
					pageItem.setNotebookTitle("");
				}
			}
			
			// is bookmark
			String strBookmarked = sWebStorageHelper.getNodeValue(
					node,
					MetaData.SYNC_PAGE_FILE_ATTR_ISBOOKMARK);
			if ((strBookmarked == null) || strBookmarked.isEmpty()) {
				Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_ISBOOKMARK + "' is null");
				pageItem.setBookmark(false);
			} else {
				try {
					pageItem.setBookmark(Boolean.parseBoolean(strBookmarked));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_ISBOOKMARK 
							+ "' with value '" + strBookmarked + "' is invalid");
					pageItem.setBookmark(false);
				}
			}
			
			// book background color
			String strBakColor = sWebStorageHelper.getNodeValue(
					node,
					MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTBACKGROUND);
			if ((strBakColor == null) || strBakColor.isEmpty()) {
				Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTBACKGROUND + "' is null");
				pageItem.setNotebookBakColor(MetaData.BOOK_COLOR_WHITE);
			} else {
				try {
					pageItem.setNotebookBakColor(Integer.parseInt(strBakColor));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTBACKGROUND 
							+ "' with value '" + strBakColor + "' is invalid");
					pageItem.setNotebookBakColor(MetaData.BOOK_COLOR_WHITE);
				}
			}
			
			// book line
			String strGridLine = sWebStorageHelper.getNodeValue(
					node,
					MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTLINE);
			if ((strGridLine == null) || strGridLine.isEmpty()) {
				Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTLINE + "' is null");
				pageItem.setNotebookGridLine(MetaData.BOOK_GRID_LINE);
			} else {
				try {
					pageItem.setNotebookGridLine(Integer.parseInt(strGridLine));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTLINE 
							+ "' with value '" + strGridLine + "' is invalid");
					pageItem.setNotebookGridLine(MetaData.BOOK_GRID_LINE);
				}
			}
			
			// book category
			String strCategory = (sWebStorageHelper.getNodeValue(
					node,
					MetaData.SYNC_PAGE_FILE_ATTR_CATEGORY));
			if ((strCategory == null) || strCategory.isEmpty()) {
				Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_CATEGORY + "' is null");
				pageItem.setNotebookPhoneMemo(false);
			} else {
				try {
					pageItem.setNotebookPhoneMemo(Integer.parseInt(strCategory) > 0);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_CATEGORY 
							+ "' with value '" + strCategory + "' is invalid");
					pageItem.setNotebookPhoneMemo(false);
				}
			}
			
			// template
			String strTemplate = (sWebStorageHelper.getNodeValue(
					node,
					MetaData.SYNC_PAGE_FILE_ATTR_TEMPLATE));
			if ((strTemplate == null) || strTemplate.isEmpty()) {
				Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_TEMPLATE + "' is null");
				pageItem.setNotebookTemplate(MetaData.Template_type_normal);
			} else {
				try {
					pageItem.setNotebookTemplate(Integer.parseInt(strTemplate));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_TEMPLATE 
							+ "' with value '" + strTemplate + "' is invalid");
					pageItem.setNotebookTemplate(MetaData.Template_type_normal);
				}
			}
			
			//BEGIN: RICHARD
			// index Language
			String strIndexLanguage = (sWebStorageHelper.getNodeValue(
					node,
					MetaData.SYNC_PAGE_FILE_ATTR_INDEXLANGUAGE));
			if ((strIndexLanguage == null) || strIndexLanguage.isEmpty()) {
				Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_INDEXLANGUAGE + "' is null");
				pageItem.setNotebookIndexLanguage(NoteBook.WAIT_TO_SET_INDEX_LANGUAGE);
			} else {
				try {
					pageItem.setNotebookIndexLanguage(Integer.parseInt(strIndexLanguage));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "page attribute '" + MetaData.SYNC_PAGE_FILE_ATTR_INDEXLANGUAGE 
							+ "' with value '" + strTemplate + "' is invalid");
					pageItem.setNotebookIndexLanguage(NoteBook.WAIT_TO_SET_INDEX_LANGUAGE);
				}
			}
			//END: RICHARD
			
			for (int i = 0; i < MetaData.SyncExceptionList.size(); i++) {
				SyncPageItem item = MetaData.SyncExceptionList.get(i);
				if ((item != null) && (item.getPageId() == pageItem.getPageId())) {
					isValid = false;
					MetaData.SyncExceptionList.remove(i);
					break;
				}
			}
			
			if (isValid) {
				mRemotePageListAll.add(pageItem);
			} else {
				MetaData.SyncExceptionList.add(pageItem);
			}
		}
	}
	public void AddDoodleFileList(Node node,String strFileName)
	{
		LocalPageItem doodleItem = new LocalPageItem();
		if (node != null) 
		{
			// id
			String strPageId = strFileName.substring(0,strFileName.length()- MetaData.SYNC_DOODLE_FILE_EXTENSION.length());
			if ((strPageId == null) || strPageId.isEmpty()) {
				Log.e(TAG, "doodle id is null, file name '" + strFileName + "' is invalid");
				return;
			}
			try {
				doodleItem.setPageId(Long.parseLong(strPageId));
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "doodle id is invalid, file name '" + strFileName + "' is invalid");
				return;
			}
			
			boolean isValid = true;
			
			// last sync modified time
			String strSyncTime = sWebStorageHelper.getNodeValue(
					node,
					MetaData.SYNC_ATTR_LASTCHANGETIME);
			if ((strSyncTime == null) || strSyncTime.isEmpty()) {
				Log.e(TAG, "doodle attribute '" + MetaData.SYNC_ATTR_LASTCHANGETIME + "'is null");
				isValid = false;
				doodleItem.setLastSyncModifiedTime(System.currentTimeMillis());
			} else {
				try {
					doodleItem.setLastSyncModifiedTime(Long.parseLong(strSyncTime));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "doodle attribute '" + MetaData.SYNC_ATTR_LASTCHANGETIME 
							+ "' with value '" + strSyncTime + "' is invalid");
					isValid = false;
					doodleItem.setLastSyncModifiedTime(System.currentTimeMillis());
				}
			}
			
			// sha
			String sha = sWebStorageHelper.getNodeValue(node,MetaData.SYNC_ATTR_SHA);
			if ((sha == null) || sha.isEmpty()) {
				Log.e(TAG, "doodle attribute '" + MetaData.SYNC_ATTR_SHA 
						+ "' is null");
				isValid = false;
			}
			doodleItem.setSha(sha);
			
			if (!isValid) {
				boolean isAdd = true;
				for (SyncPageItem pageItem : MetaData.SyncExceptionList) {
					if ((pageItem != null) && (pageItem.getPageId() == doodleItem.getPageId())) {
						isAdd = false;
						break;
					}
				}
				if (isAdd) {
					SyncPageItem item = null;
					
					for (int i = 0; i < mRemotePageListAll.size(); i++) {
						if (doodleItem.getPageId() == mRemotePageListAll.get(i).getPageId()) {
							item = mRemotePageListAll.remove(i);
							break;
						}
					}
					
					if (item == null) {
						item = new SyncPageItem();
						item.setPageId(doodleItem.getPageId());
						item.setVersion(null);
						item.setLastSyncOwnerBookId(-1);
						item.setLastSyncModifiedTime(System.currentTimeMillis());
					}
					
					MetaData.SyncExceptionList.add(item);
				}
			}
			
			mRemoteDoodleList.add(doodleItem);
		}
	}
	public void AddItemFileList(Node node,String strFileName)
	{
		LocalPageItem itemItem = new LocalPageItem();
		if (node != null) 
		{
			// id
			String strPageId = strFileName.substring(0,strFileName.length()- MetaData.SYNC_ITEM_FILE_EXTENSION.length());
			if ((strPageId == null) || strPageId.isEmpty()) {
				Log.e(TAG, "item id is null, file name '" + strFileName + "' is invalid");
				return;
			}
			try {
				itemItem.setPageId(Long.parseLong(strPageId));
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "item id is invalid, file name '" + strFileName + "' is invalid");
				return;
			}
			
			boolean isValid = true;
			// last sync modified time
			String strSyncTime = sWebStorageHelper.getNodeValue(
					node,
					MetaData.SYNC_ATTR_LASTCHANGETIME);
			if ((strSyncTime == null) || strSyncTime.isEmpty()) {
				Log.e(TAG, "item attribute '" + MetaData.SYNC_ATTR_LASTCHANGETIME + "' is null");
				isValid = false;
				itemItem.setLastSyncModifiedTime(System.currentTimeMillis());
			} else {
				try {
					itemItem.setLastSyncModifiedTime(Long.parseLong(strSyncTime));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "item attribute '" + MetaData.SYNC_ATTR_LASTCHANGETIME 
							+ "' with value '" + strSyncTime + "' is invalid");
					isValid = false;
					itemItem.setLastSyncModifiedTime(System.currentTimeMillis());
				}
			}

			// sha
			String sha = sWebStorageHelper.getNodeValue(node,MetaData.SYNC_ATTR_SHA);
			if ((sha == null) || sha.isEmpty()) {
				Log.e(TAG, "item attribute '" + MetaData.SYNC_ATTR_SHA 
						+ "' is null");
				isValid = false;
			}
			itemItem.setSha(sha);
			
			if (!isValid) {
				boolean isAdd = true;
				for (SyncPageItem pageItem : MetaData.SyncExceptionList) {
					if ((pageItem != null) && (pageItem.getPageId() == itemItem.getPageId())) {
						isAdd = false;
						break;
					}
				}
				if (isAdd) {
					SyncPageItem item = null;
					
					for (int i = 0; i < mRemotePageListAll.size(); i++) {
						if (itemItem.getPageId() == mRemotePageListAll.get(i).getPageId()) {
							item = mRemotePageListAll.remove(i);
							break;
						}
					}
					
					if (item == null) {
						item = new SyncPageItem();
						item.setPageId(itemItem.getPageId());
						item.setVersion(null);
						item.setLastSyncOwnerBookId(System.currentTimeMillis() + SyncHelper.SYNC_PAGE_ID_BASE);
					}
					
					MetaData.SyncExceptionList.add(item);
				}
			}
			
			mRemoteItemList.add(itemItem);
		}
	}
	public void GetModifyPageListFromWeb() throws Exception {
		Document doc = SyncPageFile.getServerModifyFileList();
		mRemotePageListAll = new ArrayList<SyncPageItem>();
		mRemoteDoodleList = new ArrayList<LocalPageItem>();
		mRemoteItemList = new ArrayList<LocalPageItem>();
		mRemoteAttachmentList = new ArrayList<LocalPageItem>();
		if (doc != null) {
			NodeList nodeList = doc.getElementsByTagName("file");
			if (nodeList != null) {
				for (int i = 0; i < nodeList.getLength(); i++) {
					String strFileName = sWebStorageHelper.decodeBase64(sWebStorageHelper.getNodeValue(
							nodeList.item(i), "display"));
					if (strFileName != null) {
						Node node = nodeList.item(i);
						if (strFileName.endsWith(MetaData.SYNC_PAGE_FILE_EXTENSION)) {
							AddFileList(node,strFileName);
						}
						else if (strFileName.endsWith(MetaData.SYNC_DOODLE_FILE_EXTENSION)) {
							AddDoodleFileList(node,strFileName);
						}
						else if (strFileName.endsWith(MetaData.SYNC_ITEM_FILE_EXTENSION)) {
							AddItemFileList(node,strFileName);
						}
						else if (strFileName.endsWith(MetaData.SYNC_ATTACHMENT_FILE_EXTENSION)) {
						}
					} else {
						Log.v(TAG, "Can not get " + strFileName
								+ "'s 'display' attribute");
					}
				}
			} else {
				Log.v(TAG, "Can not get 'file' attribute");
			}
		} else {
			Log.v(TAG, "None file list got");
		}
		GetPageModifyAndBookIdList();
	}
	public void GetPageModifyAndBookIdList()
	{
		mRemotePageList = new ArrayList<SyncPageItem>();
		mRemotePageListBookId = new ArrayList<SyncPageItem>();
		int count  = mRemotePageListAll.size();
		for(int i = 0 ; i < count ; i++)
		{
			SyncPageItem remoteItem = mRemotePageListAll.get(i);
			Cursor cursor = mContentResolver.query(MetaData.PageTable.uri, null,
					MetaData.SYNC_CUSTOM_DB_QUERY_TAG + "userAccount = ? AND created_date = ?",
					new String[] { Long.toString(SyncFilesWorkTask.mSyncUserId) ,Long.toString(remoteItem.getPageId())},
					null);
			if(cursor != null)
			{
				cursor.moveToFirst();
				if(!cursor.isAfterLast())
				{
					LocalPageItem pageItem = new LocalPageItem();
					pageItem.load(cursor);
					if(remoteItem.getLastSyncModifiedTime() != pageItem.getLastSyncModifiedTime() || (remoteItem.isDeleted()))
					{
						mRemotePageList.add(remoteItem);
					}
					if(remoteItem.getLastSyncOwnerBookId() != pageItem.getLastSyncOwnerBookId())
					{
						mRemotePageListBookId.add(remoteItem);
					}
				}
				else	
				{
					mRemotePageList.add(remoteItem);
				}
				cursor.close();
			}
			else	
			{
				mRemotePageList.add(remoteItem);
			}
		}
	}
	public void GetModifyPageListFromDB() {
		mLocalPageList = new ArrayList<LocalPageItem>();
		Cursor cursor = mContentResolver.query(MetaData.PageTable.uri, null,
				MetaData.SYNC_CUSTOM_DB_QUERY_TAG + "userAccount = ?",
				new String[] { Long.toString(SyncFilesWorkTask.mSyncUserId) },
				null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			LocalPageItem pageItem = new LocalPageItem();
			pageItem.load(cursor);
			if((pageItem.getLastSyncModifiedTime() != pageItem.getCurModifiedTime()) || (pageItem.getLastSyncModifiedTime()== 0 )|| pageItem.isDeleted())
			{
				mLocalPageList.add(pageItem);
			} else if (MetaData.IS_ENABLE_WEBSTORAGE_DATA_MIGRATING && MetaData.SyncUpgradedFirstTime) {
				boolean isAdd = true;
				for (SyncPageItem item : mRemotePageListAll) {
					if (item.getPageId() == pageItem.getPageId()) {
						isAdd = false;
						break;
					}
				}
				if (isAdd) {
					MetaData.SyncLocalUntreatedPageList.add(pageItem);
				}
			}
			cursor.moveToNext();
		}
		cursor.close();
	}
	public void GetModifyOwnerIdPageListFromDB() {
		mLocalOwnerModifyPageList = new ArrayList<LocalPageItem>();
		Cursor cursor = mContentResolver.query(MetaData.PageTable.uri, null,
				MetaData.SYNC_CUSTOM_DB_QUERY_TAG + "userAccount = ?",
				new String[] { Long.toString(SyncFilesWorkTask.mSyncUserId) },
				null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			LocalPageItem pageItem = new LocalPageItem();
			pageItem.load(cursor);
			if(pageItem.getCurOwnerBookId() != pageItem.getLastSyncOwnerBookId())
			{
				mLocalOwnerModifyPageList.add(pageItem);
			}
			cursor.moveToNext();
		}
		cursor.close();
	}
	//end   darwin

	public void GetPageListFromDB() {
		mLocalPageList = new ArrayList<LocalPageItem>();
		Cursor cursor = mContentResolver.query(MetaData.PageTable.uri, null,
				MetaData.SYNC_CUSTOM_DB_QUERY_TAG + "userAccount = ?",
				new String[] { Long.toString(SyncFilesWorkTask.mSyncUserId) },
				null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			LocalPageItem pageItem = new LocalPageItem();
			pageItem.load(cursor);
			mLocalPageList.add(pageItem);
			cursor.moveToNext();

		}
		cursor.close();
	}

	private NotePage getNotePage(SyncPageItem remotePageItem) {
		NotePage notePage = new NotePage(mContext);
		notePage.setCreatedTime(remotePageItem.getPageId());
		notePage.setModifiedTime(remotePageItem.getLastSyncModifiedTime());
		notePage.setLastSyncModifiedTime(remotePageItem
				.getLastSyncModifiedTime());
		notePage.setOwnerBookId(remotePageItem.getLastSyncOwnerBookId());
		notePage.setLastSyncOwnerBookId(remotePageItem.getLastSyncOwnerBookId());
		notePage.setBookmark(remotePageItem.isBookmark());
		notePage.setDeleted(remotePageItem.isDeleted());
		notePage.setUserId(SyncFilesWorkTask.mSyncUserId);
		
		notePage.setIndexLanguage(remotePageItem.getNotebookIndexLanguage());

		if (mCurrentVersion.equalsIgnoreCase(MetaData.SYNC_VERSION_2)) {
			notePage.setVersion(1);
		}
		return notePage;
	}
	
	public boolean isPad() {
		return (mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
        == Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	private NoteBook getNoteBook(SyncPageItem remotePageItem) {
		NoteBook noteBook = null;
		
		boolean isExceptionPage = false;
		for (SyncPageItem item : MetaData.SyncExceptionList) {
			if (item.getPageId() == remotePageItem.getPageId()) {
				isExceptionPage = true;
				break;
			}
		}
		
		if (!isExceptionPage) {
			noteBook = new NoteBook(mContext,
					remotePageItem.getLastSyncOwnerBookId());
			noteBook.setModifiedTime(System.currentTimeMillis());
			noteBook.setTitle(remotePageItem.getNotebookTitle());
			noteBook.setBookColor(remotePageItem.getNotebookBakColor());
			noteBook.setGridType(remotePageItem.getNotebookGridLine());
			noteBook.setPhoneMemo(remotePageItem.isNotebookPhoneMemo());
		} else {
			boolean ispad = isPad();
			NoteBook temp = mBookCase.getNoteBook(ispad ? 
					MetaData.SyncExceptionPadBookId : MetaData.SyncExceptionPhoneBookId);
			if (temp == null) {
				noteBook = new NoteBook(mContext, remotePageItem.getLastSyncOwnerBookId());
				SharedPreferences pref = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, 
						Context.MODE_MULTI_PROCESS);
				SharedPreferences.Editor prefEditor = pref.edit();
				if (ispad) {
					MetaData.SyncExceptionPadBookId = remotePageItem.getLastSyncOwnerBookId();
					prefEditor.putLong(MetaData.DefaultPadId, MetaData.SyncExceptionPadBookId);
					prefEditor.commit();
					noteBook.setPageSize(MetaData.PAGE_SIZE_PAD);
				} else {
					MetaData.SyncExceptionPhoneBookId = remotePageItem.getLastSyncOwnerBookId();
					prefEditor.putLong(MetaData.DefaultPhoneId, MetaData.SyncExceptionPhoneBookId);
					prefEditor.commit();
					noteBook.setPageSize(MetaData.PAGE_SIZE_PHONE);
				}
				noteBook.setLockedNoUpdateDB(false);
		        noteBook.setTitleNoUpdateDB(PickerUtility.getDefaultBookName(mContext));
		        Long time = System.currentTimeMillis();
		        noteBook.setModifiedTime(time);
		        noteBook.setLastSyncModifiedTime(time);
			} else {
				noteBook = temp;
			}
			
		}
		
		noteBook.setUserId(SyncFilesWorkTask.mSyncUserId);
		if (mCurrentVersion.equalsIgnoreCase(MetaData.SYNC_VERSION_2)) {
			noteBook.setVersion(1);
			noteBook.setPageSize(MetaData.PAGE_SIZE_PAD);
		}
		//begin darwin
		noteBook.setTemplate(remotePageItem.getNotebookTemplate());
		//end   darwin
		
		//BEGIN: RICHARD
		noteBook.setIndexLanguage(remotePageItem.getNotebookIndexLanguage());
		//END: RICHARD
		
		
		if (noteBook.getIsLocked()) {
			MetaData.lockBookIdList.add(noteBook.getCreatedTime());
		}
		
		return noteBook;
	}

	private SyncPageItem getUploadInfo(LocalPageItem localPageItem, 
			SyncPageItem remotePageItem) {
		SyncPageItem item = remotePageItem;
		if (remotePageItem == null) {
			item = new SyncPageItem();
		}
		item.setPageId(localPageItem.getPageId());
		item.setLastSyncModifiedTime(localPageItem.getCurModifiedTime());
		item.setLastSyncOwnerBookId(localPageItem.getCurOwnerBookId());
		item.setBookmark(localPageItem.isBookmark());
		item.setDeleted(localPageItem.isDeleted());
		Cursor cursor = mContentResolver
				.query(MetaData.BookTable.uri, null,
						MetaData.SYNC_CUSTOM_DB_QUERY_TAG + "created_date = ?",
						new String[] { Long.toString(localPageItem
								.getCurOwnerBookId()) }, null);
		
		//begin darwin
		if((cursor != null) && (cursor.getCount() != 0))
		{
			cursor.moveToFirst();
			item.setNotebookTitle(cursor.getString(MetaData.BookTable.INDEX_TITLE));
			item.setNotebookPhoneMemo(cursor
					.getInt(MetaData.BookTable.INDEX_IS_PHONE_MEMO) > 0);
			item.setNotebookBakColor(cursor
					.getInt(MetaData.BookTable.INDEX_BOOK_COLOR));
			item.setNotebookGridLine(cursor
					.getInt(MetaData.BookTable.INDEX_BOOK_GRID));
			item.setNotebookTemplate(cursor
							.getInt(MetaData.BookTable.INDEX_TEMPLATE));
			
			item.setNotebookIndexLanguage(cursor
					.getInt(MetaData.BookTable.INDEX_INDEX_LANGUAGE));//RICHARD
		}
		//end darwin
		cursor.close();
		return item;
	}
	
	private void updateSyncCount(int curPageDelta, int totalPageDelta, 
			int failedPageDelta, boolean isUpdateUI) {
		MetaData.SyncCurrentPage += curPageDelta;
		MetaData.SyncTotalPage += totalPageDelta;
		MetaData.SyncFailedPageCount += failedPageDelta;
		if (isUpdateUI)
			mTask.updateUI();
	}
	
	private boolean downloadRemotePageNew(LocalPageItem localPageItem,SyncPageItem remotePageItem)
	{
		boolean isBookExist = true;
		NoteBook noteBook = null;
		NotePage notePage = getNotePage(remotePageItem);
		

			noteBook = mBookCase.getNoteBook(remotePageItem
					.getLastSyncOwnerBookId());
			
			if (noteBook == null) {
				noteBook = getNoteBook(remotePageItem);
				isBookExist = false;
			}
		
			noteBook.addPageFromweb(notePage);

		
		if(!isBookExist)
			mBookCase.addNewBookFromWebPage(noteBook);
		

			updateSyncCount(1, 1, -1, true);
		

		return true;
	}
	
	public boolean downloadRemotePage(LocalPageItem localPageItem,
			SyncPageItem remotePageItem) throws WebStorageException {
		// download remote page data		
		try {
			SyncPageFile.downloadPage(remotePageItem.getPageId());
			SyncFilesWorkTask.mNeedReIndexPageIDList.add(remotePageItem.getPageId());//RICHARD
		} catch (WebStorageException e) {
			if ((localPageItem == null) || localPageItem.isDeleted()) {
				updateSyncCount(1, 1, 1, true);
			} else {
				updateSyncCount(1, 0, 0, true);
			}
			if ((e.getErrorKind() == WebStorageException.LOGIN_ERROR) 
					|| (e.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
				throw e;
			}
			return false;
		}
		
		int orgPageIndex = -1;
		long bookId = -1;
		boolean isBookExist = true;
		NoteBook noteBook = null;
		NotePage notePage = getNotePage(remotePageItem);
		
		if (localPageItem != null && !localPageItem.isDeleted()) {			
			// delete local page
			noteBook = mBookCase.getNoteBook(localPageItem
					.getCurOwnerBookId());
			orgPageIndex = noteBook.getPageIndex(localPageItem.getPageId());
			noteBook.deletePagefromWeb(localPageItem.getPageId());
			
			// download page to original book when local page exists;
			bookId = localPageItem.getCurOwnerBookId();
		} else {
			if (localPageItem != null) {
				mContentResolver.delete(MetaData.PageTable.uri, 
						"created_date = ?",
						new String[] { Long.toString(localPageItem.getPageId()) });
			}
			
			// create notebook if notebook not exist.
			noteBook = mBookCase.getNoteBook(remotePageItem
					.getLastSyncOwnerBookId());
			
			if (noteBook == null) {
				noteBook = getNoteBook(remotePageItem);
				isBookExist = false;
			}
			
			// download page to new book when local page note exist;
			bookId = noteBook.getCreatedTime();
		}
		
		try {
			SyncPageFile.unpackPage(
					bookId,
					remotePageItem.getPageId());
		} catch (IOException e) {
			if ((localPageItem == null) || localPageItem.isDeleted()) {
				updateSyncCount(1, 1, 1, true);
			} else {
				updateSyncCount(1, 0, 0, true);
			}
			return false;
		}
		
		if (orgPageIndex < 0) {
			noteBook.addPageFromweb(notePage);
		} else {
			noteBook.addPageFromweb(notePage, orgPageIndex);
		}
		
		if(!isBookExist)
			mBookCase.addNewBookFromWebPage(noteBook);
		
		notePage.updateTimestampInfo(new PageDataLoader(mContext), false);
		
		if ((localPageItem == null) || localPageItem.isDeleted()) {
			updateSyncCount(1, 1, 0, true);
		} else {
			updateSyncCount(1, 0, -1, true);
		}
		
		// BEGIN: Shane_Wang@asus.com 2012-12-10
    	SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.PageTable.uri, remotePageItem.getPageId(), remotePageItem.getSha()));
		// END: Shane_Wang@asus.com 2012-12-10

		return true;
	}
	//Begin Darwin_Yu@asus.com
	
	public boolean downloadRemotePageNotAddPage(LocalPageItem localPageItem,
			SyncPageItem remotePageItem) {

		NoteBook noteBook = null;
		NotePage notePage = getNotePage(remotePageItem);
		
			noteBook = mBookCase.getNoteBook(remotePageItem
					.getLastSyncOwnerBookId());
			
		if(noteBook != null)
		{
			if (noteBook.getNotePage(notePage.getCreatedTime()) == null) {
				noteBook.addPageFromweb(notePage);
			} else {
				// download page to new book when local page note exist;
				noteBook.updatePageFromWeb(notePage);
			}
		}
		else
		{
			if (localPageItem != null) {
				mContentResolver.delete(MetaData.PageTable.uri, 
						"created_date = ?",
						new String[] { Long.toString(localPageItem.getPageId()) });
			}
			noteBook = getNoteBook(remotePageItem);
			noteBook.addPageFromweb(notePage);
			mBookCase.addNewBookFromWebPage(noteBook);
		}
		
		notePage.getFilePath();		

			updateSyncCount(1, 0, -1, true);
		

		return true;
	}
	
	public boolean mFullPageChanged         = false;
	public boolean mFullLocalPageChanged    = false;
	private boolean mDoodleServerChanged	 = false;
	private boolean mDoodleLocalChanged		 = false;
	private boolean mItemServerChanged		 = false;
	private boolean mItemLocalChanged		 = false;
	
	// BEGIN: Better
	private boolean mIsDoodleExists = false;
	private boolean mIsItemExists = false;
	// END: Better
	
	private LocalPageItem mDoodleItem						 = new LocalPageItem();
	private LocalPageItem mItemItem						     = new LocalPageItem();
	private List<LocalPageItem> mListAttachmentLocal		 = new ArrayList<LocalPageItem>();
	private List<LocalPageItem> mListAttachmentServer		 = new ArrayList<LocalPageItem>();
	private List<LocalPageItem> mListAttachmentServerAll	 = new ArrayList<LocalPageItem>();
	
	public void insertDoodleItemAttachmentDB(long pageId,long bookId)
	{
		try
		{
			ContentValues cvDoodle = new ContentValues();
			cvDoodle.put(MetaData.DoodleTable.ID, pageId);
			cvDoodle.put(MetaData.DoodleTable.IS_DELETE,  mDoodleItem.isDeleted() ? 1 : 0);
			cvDoodle.put(MetaData.DoodleTable.MODIFIED_DATE,  mDoodleItem.getLastSyncModifiedTime());
			cvDoodle.put(MetaData.DoodleTable.LASTSYNC_MODIFYTIME,  mDoodleItem.getLastSyncModifiedTime());
			mContentResolver.insert(MetaData.DoodleTable.uri, cvDoodle);
			// BEGIN: Shane_Wang@asus.com 2012-12-10
			SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.DoodleTable.uri, pageId, mDoodleItem.getSha()));
			// END: Shane_Wang@asus.com 2012-12-10
			
			ContentValues cvItem = new ContentValues();
			cvItem.put(MetaData.ItemTable.ID, pageId);
			cvItem.put(MetaData.ItemTable.IS_DELETE, 0);
			cvItem.put(MetaData.DoodleTable.MODIFIED_DATE,  mItemItem.getLastSyncModifiedTime());
			cvItem.put(MetaData.DoodleTable.LASTSYNC_MODIFYTIME,  mItemItem.getLastSyncModifiedTime());
			mContentResolver.insert(MetaData.ItemTable.uri, cvItem);
			// BEGIN: Shane_Wang@asus.com 2012-12-10
			SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.ItemTable.uri, pageId, mItemItem.getSha()));
			// END: Shane_Wang@asus.com 2012-12-10
		}
		catch(Exception e)
		{
			Log.i("darwin test", e.toString());
		}
		
		if ((mListAttachmentServer != null) && (mListAttachmentServer.size() > 0)) {
			for (LocalPageItem item : mListAttachmentServer) {
				ContentValues cvAttachment = new ContentValues();
	        	cvAttachment.put(MetaData.AttachmentTable.ID, pageId);
	        	cvAttachment.put(MetaData.AttachmentTable.MODIFIED_DATE, item.getLastSyncModifiedTime());
	        	cvAttachment.put(MetaData.AttachmentTable.IS_DELETE, item.isDeleted() ? 1 : 0);
	        	cvAttachment.put(MetaData.AttachmentTable.FILE_NAME, pageId + "." + item.getAttachmentName());
	        	mContext.getContentResolver().insert(MetaData.AttachmentTable.uri, cvAttachment);
	        	// BEGIN: Shane_Wang@asus.com 2012-12-10
	        	SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.AttachmentTable.uri, pageId, item.getSha(), item.getAttachmentName()));
				// END: Shane_Wang@asus.com 2012-12-10
			}
		}

	}

	private void resetChangedValue()
	{
		mDoodleServerChanged	 = false;
		mDoodleLocalChanged		 = false;
		mItemServerChanged		 = false;
		mItemLocalChanged		 = false;
	}
	
	public void findDownloadElement(long pageID)
	{
		resetChangedValue();
		findDownDoodle(pageID);
		findDownItem(pageID);
		findDownAttachment(pageID);
	}
	private void findDownDoodle(long pageID)
	{
		mIsDoodleExists = false;
		for(LocalPageItem localItem : mRemoteDoodleList)
		{
			if(localItem.getPageId() == pageID)
			{
				mDoodleServerChanged = true;
				mDoodleItem = localItem;
				mIsDoodleExists = true;
				break;
			}
		}
	}
	private void findDownItem(long pageID)
	{
		mIsItemExists = false;
		for(LocalPageItem localItem : mRemoteItemList)
		{
			if(localItem.getPageId() == pageID)
			{
				mItemServerChanged = true;
				mItemItem = localItem;
				mIsItemExists = true;
				break;
			}
		}
	}
	private void findDownAttachment(long pageID)
	{
		compareAttachment(pageID);
	}
	public void findUploadElement(long pageID)
	{
		resetChangedValue();
		findUploadDoodle(pageID);
		findUploadItem(pageID);
		findUploadAttachment(pageID);
	}
	private void findUploadDoodle(long pageID)
	{
		mDoodleItem = getLocalItemFromDB(pageID,MetaData.DOODLE_TAG);
		mDoodleItem.setPageId(pageID);
		if(mDoodleItem.getCurModifiedTime() != mDoodleItem.getLastSyncModifiedTime())
		{
			mDoodleLocalChanged = true;
		}
	}
	private void findUploadItem(long pageID)
	{
		mItemItem = getLocalItemFromDB(pageID,MetaData.ITEM_TAG);
		mItemItem.setPageId(pageID);
		if(mItemItem.getCurModifiedTime() != mItemItem.getLastSyncModifiedTime())
		{
			mItemLocalChanged = true;
		}
	}
	private void findUploadAttachment(long pageID)
	{
		getLocalAttachmentFromDB( pageID ,true);
	}
	private void compareDoodle(long pageID) throws WebStorageException
	{
		String fileName = pageID + ".Doodle";
		String resultXml = "";
		String lastChangeTime = "";
		String sha = "";
		try
		{
			resultXml = MetaData.webStorage.foundSpecificFileORFolder(fileName, false);
			
			Node fileNode = MetaData.webStorage.getSpecificFileAttribute(fileName);
			if (fileNode != null) {
				lastChangeTime = sWebStorageHelper.getNodeValue(fileNode,
						MetaData.SYNC_ATTR_LASTCHANGETIME);
				sha = sWebStorageHelper.getNodeValue(fileNode,
						MetaData.SYNC_ATTR_SHA);
			}

		}
		catch(Exception e)
		{
			Log.v("darwin " , e.toString());
			
			// BEGIN: Better
			if (e instanceof WebStorageException) {
				WebStorageException webex = (WebStorageException) e;
				if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
						|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
					throw webex;
				}
			}
			// END: Better
		}
		mDoodleItem = getLocalItemFromDB(pageID,MetaData.DOODLE_TAG);
		mDoodleItem.setPageId(pageID);
		mDoodleItem.setSha(sha);
		if(mDoodleItem.getCurModifiedTime() != mDoodleItem.getLastSyncModifiedTime())
		{
			mDoodleLocalChanged = true;
		}
		if((lastChangeTime!= null) && (!Long.toString(mDoodleItem.getLastSyncModifiedTime()).equalsIgnoreCase(lastChangeTime)))
		{
			mDoodleServerChanged = true;
		}
	}
	
	private void compareItem(long pageID) throws WebStorageException
	{
		String fileName = pageID + ".Item";
		String resultXml = "";
		String lastChangeTime = "";
		String sha = "";
		try
		{
			resultXml = MetaData.webStorage.foundSpecificFileORFolder(fileName, false);
			
			Node fileNode = MetaData.webStorage.getSpecificFileAttribute(fileName);
			if (fileNode != null) {
				lastChangeTime = sWebStorageHelper.getNodeValue(fileNode,
						MetaData.SYNC_ATTR_LASTCHANGETIME);
				sha = sWebStorageHelper.getNodeValue(fileNode,
						MetaData.SYNC_ATTR_SHA);
			}

		}
		catch(Exception e)
		{
			Log.v("darwin " , e.toString());
			
			// BEGIN: Better
			if (e instanceof WebStorageException) {
				WebStorageException webex = (WebStorageException) e;
				if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
						|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
					throw webex;
				}
			}
			// END: Better
		}
		mItemItem = getLocalItemFromDB(pageID,MetaData.ITEM_TAG);
		mItemItem.setPageId(pageID);
		mItemItem.setSha(sha);
		if(mItemItem.getCurModifiedTime() != mItemItem.getLastSyncModifiedTime())
		{
			mItemLocalChanged = true;
		}
		if(!Long.toString(mItemItem.getLastSyncModifiedTime()).equalsIgnoreCase(lastChangeTime))
		{
			mItemServerChanged = true;
		}

	}
	
	private void GetAllAttachmentList() throws WebStorageException
	{
			try
			{
			mListAttachmentServerAll = SyncPageFile.getServerFileListAll(MetaData.ATTACHMENT_TAG);
			}
			catch(Exception e)
			{
				Log.v("darwin " , e.toString());
				
				// BEGIN: Better
				if (e instanceof WebStorageException) {
					WebStorageException webex = (WebStorageException) e;
					if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
							|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
						throw webex;
					}
				}
				// END: Better
		}
	}
	private void GetListByPageId (long pageID)
	{
		mListAttachmentServer.clear();
		for(LocalPageItem item : mListAttachmentServerAll)
		{
			if(item.getPageId() == pageID)
			{
				mListAttachmentServer.add(item);
			}
		}
	}
	private void compareAttachment(long pageID)
	{
		if(getLocalAttachmentFromDB( pageID,false))
		{
			GetListByPageId(pageID);
			compareAttachmentList();
		}
	}
	
	private void compareAttachmentList()
	{
		SyncPageFile.mListAttachmentModify.clear();
		for (int i = 0; i < mListAttachmentServer.size(); i++) 
		{
			boolean bIsMatchName = false;
			LocalPageItem serverItem = mListAttachmentServer.get(i);
			for (int j = 0; j < mListAttachmentLocal.size(); j++) 
			{
				LocalPageItem localItem = mListAttachmentLocal.get(j);
				if(serverItem.getAttachmentName().equalsIgnoreCase(localItem.getAttachmentName()))
				{
					//Attachment only has ADD and DELETE operation
					localItem.setIsLocalOrServerModify( -2 );
					bIsMatchName = true;
					break;
				}
			}
			if(!bIsMatchName)
			{
				serverItem.setIsLocalOrServerModify(1);
				SyncPageFile.mListAttachmentModify.add(serverItem);
			}
		}
		
		for (int j = 0; j < mListAttachmentLocal.size(); j++) 
		{
			LocalPageItem localItem = mListAttachmentLocal.get(j);
			if(localItem.getIsLocalOrServerModify() == -1)
			{
				localItem.setIsLocalOrServerModify(0);
				SyncPageFile.mListAttachmentModify.add(localItem);
			}
		}
	}
	
	private String getTrueAttachmentName(String dbName)
	{
		int index = dbName.indexOf(".");
		return dbName.substring(index + 1);
	}
	
	private boolean getLocalAttachmentFromDB(long pageID ,boolean bIsModify)
	{
		mListAttachmentLocal.clear();
		String strPageID = Long.toString(pageID);
		Cursor cur = null;
		try
		{
			cur = mContext.getContentResolver().query(MetaData.AttachmentTable.uri, null, "_id = ?", new String[]{strPageID}, null);
			if(cur != null && cur.getCount() > 0)
			{
				cur.moveToFirst();
				while (!cur.isAfterLast()) 
				{
					LocalPageItem item = new LocalPageItem();
					item.setAttachmentName(getTrueAttachmentName(cur.getString(MetaData.AttachmentTable.INDEX_FILE_NAME)));
					item.setLastSyncModifiedTime(cur.getLong(MetaData.AttachmentTable.INDEX_LASTSYNC_MODIFYTIME));
					item.setCurModifiedTime(cur.getLong(MetaData.AttachmentTable.INDEX_MODIFIED_DATE));
					item.setDeleted(cur.getInt(MetaData.AttachmentTable.INDEX_IS_DELETE) == 0 ? false : true);
					item.setPageId(pageID);
					if(bIsModify)
					{
						if(mFullPageChanged)
						{
							item.setIsLocalOrServerModify(0);
							SyncPageFile.mListAttachmentModify.add(item);
						}
						else if(item.getCurModifiedTime() != item.getLastSyncModifiedTime())
						{
							item.setIsLocalOrServerModify(0);
							SyncPageFile.mListAttachmentModify.add(item);
						}
					}
					else
					{
					mListAttachmentLocal.add(item);
					}
	                cur.moveToNext();
	            }
				cur.close();
			}
		}
		catch(Exception e)
		{
			if(cur != null)
			{
				cur.close();
			}
			return false;
		}finally {
		    if (cur != null) //smilefish fix memory leak
		        cur.close();
		}
		return true;
	}
	
	private LocalPageItem getLocalItemFromDB(long pageID,String value)
	{
		String strPageID = Long.toString(pageID);
		LocalPageItem item = new LocalPageItem();
		Cursor cur = null;
		if (value.equalsIgnoreCase(MetaData.DOODLE_TAG))
		{
			cur = mContext.getContentResolver().query(MetaData.DoodleTable.uri, null, "_id = ?", new String[]{strPageID}, null);
			if(cur != null){
				if(cur.getCount() > 0){
					Log.i("darwin", cur.toString());
					cur.moveToFirst();
					item.setLastSyncModifiedTime(cur.getLong(MetaData.DoodleTable.INDEX_LASTSYNC_MODIFYTIME));
					item.setCurModifiedTime(cur.getLong(MetaData.DoodleTable.INDEX_MODIFIED_DATE));
					item.setDeleted(cur.getInt(MetaData.DoodleTable.INDEX_IS_DELETE) == 0 ? false : true);
				}
				cur.close();
			}
			return item;
		}
		else if (value.equalsIgnoreCase(MetaData.ITEM_TAG))
		{
			cur = mContext.getContentResolver().query(MetaData.ItemTable.uri, null, "_id = ?", new String[]{strPageID}, null);
			if(cur != null){
				if(cur.getCount() > 0){
					cur.moveToFirst();
					item.setLastSyncModifiedTime(cur.getLong(MetaData.ItemTable.INDEX_LASTSYNC_MODIFYTIME));
					item.setCurModifiedTime(cur.getLong(MetaData.ItemTable.INDEX_MODIFIED_DATE));
					item.setDeleted(cur.getInt(MetaData.ItemTable.INDEX_IS_DELETE) == 0 ? false : true);
				}
				cur.close();
			}
			return item;
		}
		else if (value.equalsIgnoreCase(MetaData.ATTACHMENT_TAG))
		{
			//use another function

		}
		return null;
	}
	
	public boolean downloadRemotePageElement(LocalPageItem localPageItem,SyncPageItem remotePageItem,SyncPageCompareFun spcf,NotePage notepage) throws WebStorageException
	{
		// download remote page Element
		if(mIsDoodleExists && (mDoodleServerChanged || mFullLocalPageChanged))
		{
			try {
				SyncPageFile.downloadPageDoodle(remotePageItem.getPageId());
			} catch (WebStorageException e) {
				e.printStackTrace();
				
				// BEGIN: Better
				if ((e.getErrorKind() == WebStorageException.LOGIN_ERROR) 
						|| (e.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
					throw e;
				}
				
				return false;
				// END: Better
			}
		}
		if(mIsItemExists && (mItemServerChanged || mFullLocalPageChanged))
		{
			try {
			SyncPageFile.downloadPageItem(remotePageItem.getPageId());
			SyncFilesWorkTask.mNeedReIndexPageIDList.add(remotePageItem.getPageId());//RICHARD
			} catch (WebStorageException e) {
				e.printStackTrace();
				
				// BEGIN: Better
				if ((e.getErrorKind() == WebStorageException.LOGIN_ERROR) 
						|| (e.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
					throw e;
				}
				
				return false;
				// END: Better
			}
		}
		if((SyncPageFile.mListAttachmentModify.size() != 0) || mFullLocalPageChanged)
		{
			try {
				SyncPageFile.downloadPageAttachment(remotePageItem.getPageId());
			} catch (WebStorageException e) {
				e.printStackTrace();
				
				// BEGIN: Better
				if ((e.getErrorKind() == WebStorageException.LOGIN_ERROR) 
						|| (e.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
					throw e;
				}
				
				return false;
				// END: Better				
			}
		}
		
		return true;
	}
	
	// BEGIN: Better
	public boolean unpackRemotePageElement(LocalPageItem localPageItem,SyncPageItem remotePageItem,SyncPageCompareFun spcf) {
		if(mDoodleServerChanged || mFullLocalPageChanged) {
			try 
			{
				SyncPageFile.unpackPageDoodle(
						remotePageItem.getLastSyncOwnerBookId(),
						remotePageItem.getPageId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(mItemServerChanged || mFullLocalPageChanged) {
			try {
				SyncPageFile.unpackPageItem(
						remotePageItem.getLastSyncOwnerBookId(),
						remotePageItem.getPageId(),spcf);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if((SyncPageFile.mListAttachmentModify.size() != 0) || mFullLocalPageChanged) {
			try {
				SyncPageFile.unpackPageAttachment(
						remotePageItem.getLastSyncOwnerBookId(),
						remotePageItem.getPageId(),mContext);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		mTask.callUIThreadSavethumb(remotePageItem.getPageId(),remotePageItem.getLastSyncOwnerBookId());
		
		return true;
	}
	// END: Better
	
	private void getTimeStampList(long pageId)
	{
		SyncPageFile.m_listTimeStampUpload.clear();
        Cursor cur = mContext.getContentResolver().query(MetaData.TimestampTable.uri, 
        		null, 
        		"owner = ?",
        		new String[]{Long.toString(pageId)}, null);
        if(cur != null)
		{
        	if(cur.getCount() > 0)
        	{
				cur.moveToFirst();
				while (!cur.isAfterLast()) {
					TimeStampItem tsi = new TimeStampItem();
					tsi.Id = cur.getLong(MetaData.TimestampTable.INDEX_CREATE_DATE);
					tsi.PageId = pageId;
					tsi.position = cur.getInt(MetaData.TimestampTable.INDEX_POSITION);
					SyncPageFile.m_listTimeStampUpload.add(tsi);
					cur.moveToNext();
	            }
        	}
			cur.close();
		}

	}

	public void updateTimeStampDB(long id, long pageid, long position )
	{
		boolean bIsNeedInsert = true;
        ContentValues cvs = new ContentValues();
        Cursor cursor = mContentResolver.query(MetaData.TimestampTable.uri, null, "(owner = ?) AND (create_date = ?) AND (position = ?)", 
        		new String[]{Long.toString(pageid),Long.toString(id),Long.toString(position)}, null);
    	if(cursor != null)
    	{
	        if(cursor.getCount() > 0 )
	    	{
	        	bIsNeedInsert = false;  		
	    	}
	    	cursor.close();
    	}
    	if(bIsNeedInsert)
    	{
        cvs.put(MetaData.TimestampTable.CREATED_DATE, id);
        cvs.put(MetaData.TimestampTable.OWNER, pageid);
        cvs.put(MetaData.TimestampTable.POSITION, position);
      //darwin
		  Cursor cur = mContentResolver.query(MetaData.PageTable.uri, 
				  null, 
				  "created_date = ?", 
				  new String[]{Long.toString(pageid)}, 
				  null);
		  if(cur.getCount() > 0 )
		  {
			  cur.moveToFirst();
			  if(!cur.isAfterLast())
			  {
				  cvs.put(MetaData.TimestampTable.USER_ACCOUNT,cur.getLong(MetaData.PageTable.INDEX_USER_ACCOUNT));
			  }
		  }
		  cur.close();//RICHARD FIX MEMORY LEAK
		  //darwin
        mContext.getContentResolver().insert(MetaData.TimestampTable.uri,cvs);
    	}
	}
	
	public class TimeStampItem
	{
		public long Id;
		public long PageId;
		public int  position;
	}
	
	public boolean uploadLocalPageElement(LocalPageItem localPageItem,SyncPageItem remotePageItem) throws WebStorageException 
	{
		getTimeStampList(localPageItem.getPageId());
		try {
			SyncPageFile.packPageAll(localPageItem.getCurOwnerBookId(),
					localPageItem.getPageId(),
					mFullPageChanged ? mFullPageChanged : mDoodleLocalChanged,
					mFullPageChanged ? mFullPageChanged : mItemLocalChanged,
					mFullPageChanged ? mFullPageChanged : (SyncPageFile.mListAttachmentModify.size() != 0));
		} catch (IOException e) {
			//updateSyncCount(1, 0, 0, true);
			return false;
		}
		SyncPageItem item = getUploadInfo(localPageItem, remotePageItem);
		if ((remotePageItem == null) || remotePageItem.isDeleted()) {
			item.setSha("");
		}
		
		try 
		{
			if(mDoodleLocalChanged || mFullPageChanged)
			{
				try{
					SyncPageFile.uploadPageDoodle(mDoodleItem,mDoodleItem.getSha());
				}catch(WebStorageException webex) {
					if (webex.getErrorKind() == WebStorageException.UPLOAD_INIT_SHAVALUE_NOT_MATCH) {
					try {
						Node fileNode = MetaData.webStorage
								.getSpecificFileAttribute(mDoodleItem.getPageId() + MetaData.SYNC_DOODLE_FILE_EXTENSION);
						if (fileNode != null) {
							String tmpSha = sWebStorageHelper.getNodeValue(fileNode,
									MetaData.SYNC_PAGE_FILE_ATTR_SHA);
							if (tmpSha != null) {
								if (tmpSha.equals(mDoodleItem.getSha())) {
									mDoodleItem.setSha("");
									try {
										SyncPageFile.uploadPageDoodle(mDoodleItem, mDoodleItem.getSha());
									} catch (Exception e1) {
										return false;
									}
								}
								// BEGIN: Shane_Wang@asus.com 2012-12-10
								else {
									SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.DoodleTable.uri, mDoodleItem.getPageId(), tmpSha));
									return false;
								}
								
								// END: Shane_Wang@asus.com 2012-12-10
							}
							else{
								return false;
							}
						}
						else{
							return false;
						}
					} catch (WebStorageException webe) {
						// BEGIN: Better
						if ((webe.getErrorKind() == WebStorageException.LOGIN_ERROR) 
								|| (webe.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
							throw webe;
						}
						return false;
				// END: Shane_Wang@asus.com 2012-12-12
					}
					}
					else{
						return false;
					}
				}
			}
			if(mItemLocalChanged || mFullPageChanged)
			{
				try{
					SyncPageFile.uploadPageItem(mItemItem,mItemItem.getSha());
				}catch(WebStorageException webex) {
					// BEGIN: Shane_Wang@asus.com 2012-12-12
					if (webex.getErrorKind() == WebStorageException.UPLOAD_INIT_SHAVALUE_NOT_MATCH) {
						try {
							Node fileNode = MetaData.webStorage
									.getSpecificFileAttribute(mItemItem.getPageId() + MetaData.SYNC_ITEM_FILE_EXTENSION);
							if (fileNode != null) {
								String tmpSha = sWebStorageHelper.getNodeValue(fileNode,
										MetaData.SYNC_PAGE_FILE_ATTR_SHA);
								if (tmpSha != null) {
									if (tmpSha.equals(mItemItem.getSha())) {
										mItemItem.setSha("");
										try {
											SyncPageFile.uploadPageItem(mItemItem, mItemItem.getSha());
										} catch (Exception e1) {
											return false;
										}
									}
									// BEGIN: Shane_Wang@asus.com 2012-12-10
									else {
										SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.ItemTable.uri, mItemItem.getPageId(), tmpSha));
										return false;
									}
									// END: Shane_Wang@asus.com 2012-12-10
								}
								else{
									return false;
								}
							}
							else{
								return false;
							}
						}catch (WebStorageException webe) {
							// BEGIN: Better
							if ((webe.getErrorKind() == WebStorageException.LOGIN_ERROR) 
									|| (webe.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
								throw webe;
							}
							return false;
					// END: Shane_Wang@asus.com 2012-12-12
						}
					}
					else{
						return false;
					}
				}
				
			}
			if((SyncPageFile.mListAttachmentModify.size() != 0) || mFullPageChanged)
			{
				SyncPageFile.uploadPageAttachment(SyncPageFile.mListAttachmentModify);
			}
			//SyncPageFile.uploadPage(item);
			//isSuccess = true;
		} catch (WebStorageException webex) {
			// BEGIN: Better
			if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
					|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
				throw webex;
			}
			
			return false;
			// END: Better
		}
		catch (Exception e) 
		{
			Log.i("darwin", e.toString());
			return false;
		}

		return true;
	}
	
	public boolean finishUploadPageElement(LocalPageItem localPageItem,SyncPageItem remotePageItem) throws WebStorageException {
		if(!mFullPageChanged)
		{
			try
			{
				SyncPageItem syncPageItem = null;

				// find remote page in local page list
				for (SyncPageItem syncPageItemTemp : mRemotePageList) {
					if (syncPageItemTemp.getPageId() == mDoodleItem.getPageId()) {
						syncPageItem = syncPageItemTemp;
						break;
					}
				}
				if(syncPageItem != null)
				{
					String attribute = "";
					attribute += "<" + MetaData.SYNC_FILE_VERSION + ">" 
							+ MetaData.SYNC_CURRENT_VERSION + "</" 
							+ MetaData.SYNC_FILE_VERSION + ">";
					attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_SHA + ">"
							+ syncPageItem.getSha() + "</"
							+ MetaData.SYNC_PAGE_FILE_ATTR_SHA + ">";
					attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_LASTCHANGETIME + ">"
							+ mDoodleItem.getCurModifiedTime() + "</"
							+ MetaData.SYNC_PAGE_FILE_ATTR_LASTCHANGETIME + ">";
					attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_ISDELETED + ">"
							+ syncPageItem.isDeleted() + "</"
							+ MetaData.SYNC_PAGE_FILE_ATTR_ISDELETED + ">";
					attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_LASTNOTEBOOKID + ">"
							+ syncPageItem.getLastSyncOwnerBookId() + "</"
							+ MetaData.SYNC_PAGE_FILE_ATTR_LASTNOTEBOOKID + ">";
					attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_NOTEBOOKTITLE + ">"
							+ SyncPageFile.encodeBase64(syncPageItem.getNotebookTitle()) + "</"
							+ MetaData.SYNC_PAGE_FILE_ATTR_NOTEBOOKTITLE + ">";
					attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_ISBOOKMARK + ">"
							+ syncPageItem.isBookmark() + "</"
							+ MetaData.SYNC_PAGE_FILE_ATTR_ISBOOKMARK + ">";
					attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTBACKGROUND + ">"
							+ syncPageItem.getNotebookBakColor() + "</"
							+ MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTBACKGROUND + ">";
					attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTLINE + ">"
							+ syncPageItem.getNotebookGridLine() + "</"
							+ MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTLINE + ">";
					attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_CATEGORY + ">"
							+ (syncPageItem.isNotebookPhoneMemo() ? 1 : 0) + "</"
							+ MetaData.SYNC_PAGE_FILE_ATTR_CATEGORY + ">";
					MetaData.webStorage.setSpecificFileAttribute(mDoodleItem.getPageId() + ".file", attribute);
					
					// BEGIN: Shane_Wang@asus.com 2012-12-11
					SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.PageTable.uri, mDoodleItem.getPageId(), syncPageItem.getSha()));
					// END: Shane_Wang@asus.com 2012-12-11
				}
			}
			catch(Exception e)
			{
				Log.i("darwin", e.toString());
				
				// BEGIN: Better
				if (e instanceof WebStorageException) {
					WebStorageException webex = (WebStorageException) e;
					if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
							|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
						throw webex;
					}
				}
				
				return false;
				// END: Better
			}
		}

		ContentValues cv = new ContentValues();
		cv.put(MetaData.PageTable.LASTSYNC_MODIFYTIME,
				localPageItem.getCurModifiedTime());
		if ((remotePageItem == null) || remotePageItem.isDeleted()) {
			cv.put(MetaData.PageTable.LASTSYNC_OWNER,
					localPageItem.getCurOwnerBookId());
		}
		mContentResolver.update(MetaData.PageTable.uri, cv,
				"created_date = ?",
				new String[] { Long.toString(localPageItem.getPageId()) });
		
		if(mDoodleLocalChanged || mFullPageChanged)
		{
			ContentValues cvDoodle = new ContentValues();
			cvDoodle.put(MetaData.DoodleTable.LASTSYNC_MODIFYTIME,
					mDoodleItem.getCurModifiedTime());
			mContentResolver.update(MetaData.DoodleTable.uri, cvDoodle,
					"_id = ?",
					new String[] { Long.toString(localPageItem.getPageId()) });
		}
		
		if(mItemLocalChanged || mFullPageChanged)
		{
			ContentValues cvItem = new ContentValues();
			cvItem.put(MetaData.ItemTable.LASTSYNC_MODIFYTIME,
					mItemItem.getCurModifiedTime());
			mContentResolver.update(MetaData.ItemTable.uri, cvItem,
					"_id = ?",
					new String[] { Long.toString(localPageItem.getPageId()) });
		}
		
		if((SyncPageFile.mListAttachmentModify.size() != 0) || mFullPageChanged)
		{
			ContentValues cvAttachment = new ContentValues();
			cvAttachment.put(MetaData.AttachmentTable.LASTSYNC_MODIFYTIME,
					localPageItem.getCurModifiedTime());
			mContentResolver.update(MetaData.AttachmentTable.uri, cvAttachment,
					"_id = ?",
					new String[] { Long.toString(localPageItem.getPageId()) });
		}
		
		updateSyncCount(1, 0, -1, true);
		
		return true;
	}
    
	// BEGIN: Shane_Wang@asus.com 2012-12-10
	void updateSHAListItem(Uri uri, String pageId, String sha, String fileName) {
		ContentValues cv = new ContentValues();
		cv.put(MetaData.PageTable.LAST_SHA, sha); //same as item/doodle/attachmenttable
		if(uri.equals(MetaData.PageTable.uri)) {
			mContentResolver.update(
					uri,
					cv,
					"created_date=?",
					new String[]{pageId});
		}else if(uri.equals(MetaData.DoodleTable.uri)) {
			mContentResolver.update(
					uri,
					cv,
					"_id=?",
					new String[]{pageId});
		}else if(uri.equals(MetaData.ItemTable.uri)) {
			mContentResolver.update(
					uri,
					cv,
					"_id=?",
					new String[]{pageId});		
		}else if(uri.equals(MetaData.AttachmentTable.uri)) {
			String dbFileName = pageId + "." + fileName;
			mContentResolver.update(
					uri,
					cv,
					"(_id=?) and (file_name=?)",
					new String[]{pageId, dbFileName});
		}
	}
	void updateSHA() {
		for(DBInsertItem item : shaUpdateList) {
			updateSHAListItem(item.table, Long.toString(item.pageID), item.sha, item.fileName);
		}
	}
		
	String getPageSHA(LocalPageItem pageItem) {
		Cursor cursor = mContentResolver.query(MetaData.PageTable.uri, new String[]{"last_sha"}, "created_date=?", new String[]{Long.toString(pageItem.getPageId())}, null);
		if (cursor != null){
			if(cursor.getCount() > 0) {  
				cursor.moveToFirst();
				String str = cursor.getString(0);
				cursor.close(); //smilefish fix memory leak
				return str;
			}
			cursor.close(); //smilefish fix memory leak
			return "";
        }
		else
			return "";
	}
	
	String getDoodleSHA(LocalPageItem doodleItem) {
		Cursor cursor = mContentResolver.query(MetaData.DoodleTable.uri, new String[]{"last_sha"}, "_id=?", new String[]{Long.toString(doodleItem.getPageId())}, null);
		if (cursor != null){
			if(cursor.getCount() > 0) {  
	            cursor.moveToFirst();
	            String str = cursor.getString(0);
				cursor.close(); //smilefish fix memory leak
				return str;
			}
			cursor.close(); //smilefish fix memory leak
			return "";
        } 
		else
			return "";
	}
	
	String getItemSHA(LocalPageItem itemItem) {
		Cursor cursor = mContentResolver.query(MetaData.ItemTable.uri, new String[]{"last_sha"}, "_id=?", new String[]{Long.toString(itemItem.getPageId())}, null);
		if (cursor != null){
			if(cursor.getCount() > 0) {  
	            cursor.moveToFirst();
	            String str = cursor.getString(0);
				cursor.close(); //smilefish fix memory leak
				return str;
			}
			cursor.close(); //smilefish fix memory leak
			return "";
        }
		else
			return "";
	}
	
	String getAttachmentSHA(LocalPageItem attachmentItem) {
		String id = Long.toString(attachmentItem.getPageId());
		String name = attachmentItem.getAttachmentName();
		String fileName = id + "." + name;
		Cursor cursor = mContentResolver.query(MetaData.AttachmentTable.uri, new String[]{"last_sha"}, "(_id=?) and (file_name=?)", new String[]{id, fileName}, null);
		if (cursor != null){
			if(cursor.getCount() > 0) {  
	            cursor.moveToFirst();
	            String str = cursor.getString(0);
				cursor.close(); //smilefish fix memory leak
				return str;
			}
			cursor.close(); //smilefish fix memory leak
			return "";
        }
		else
			return "";
	}
	// END: Shane_Wang@asus.com 2012-12-10
	
    private boolean uploadLocalPageFromRename(LocalPageItem localPageItem,
			SyncPageItem remotePageItem,
			String version,NoteBook noteBook) throws WebStorageException {
		SyncPageItem item = getUploadInfo(localPageItem, remotePageItem);
		if ((remotePageItem == null) || remotePageItem.isDeleted()) {
			item.setSha("");
		}
		
		item.setNotebookTitle(noteBook.getTitle());
		item.setNotebookPhoneMemo(noteBook.isPhoneMemo());
		item.setNotebookBakColor(noteBook.getBookColor());
		item.setNotebookGridLine(noteBook.getGridType());
		
		try {
			SyncPageFile.packPage(localPageItem.getCurOwnerBookId(),
					localPageItem.getPageId(),version, item.getLastSyncModifiedTime());
		} catch (IOException e) {
			updateSyncCount(1, 0, 0, true);
			return false;
		}
		
		boolean isSuccess = false;
		
		try {
			SyncPageFile.uploadPage(item);
			isSuccess = true;
		} catch (WebStorageException webex) {
			if (webex.getErrorKind() == WebStorageException.UPLOAD_INIT_SHAVALUE_NOT_MATCH) {
				try {
					Node fileNode = MetaData.webStorage
							.getSpecificFileAttribute(localPageItem.getPageId() + MetaData.SYNC_PAGE_FILE_EXTENSION);
					if (fileNode != null) {
						String tmpSha = sWebStorageHelper.getNodeValue(fileNode,
								MetaData.SYNC_PAGE_FILE_ATTR_SHA);
						if (tmpSha != null) {
							if (tmpSha.equals(item.getSha())) {
								item.setSha("");
								try {
									SyncPageFile.uploadPage(item);
									isSuccess = true;
								} catch (Exception e1) {
									return false;
								}
							}
							// BEGIN: Shane_Wang@asus.com 2012-12-10
							else {
								SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.PageTable.uri, localPageItem.getPageId(), tmpSha));
								return false;
							}
							// END: Shane_Wang@asus.com 2012-12-10
						}
						else{
							return false;
						}
					}
					else{
						return false;
					}
				} catch (WebStorageException webe) {
					// BEGIN: Better
					if ((webe.getErrorKind() == WebStorageException.LOGIN_ERROR) 
							|| (webe.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
						throw webe;
					}
					return false;
					// END: Better
				} catch (Exception e2) {
					return false;
				}
			}
			else{
				return false;
			}
			
			// BEGIN: Better
			if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
					|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
				throw webex;
			}
			// END: Better
		} catch (Exception e) {
			return false;
		}
		
		if (!isSuccess) {
			updateSyncCount(1, 0, 0, true);
			return false;
		}

		return true;
	}
    
    public boolean uploadLocalPage(LocalPageItem localPageItem,
			SyncPageItem remotePageItem,
			String version) throws WebStorageException {
		SyncPageItem item = getUploadInfo(localPageItem, remotePageItem);
		if ((remotePageItem == null) || remotePageItem.isDeleted()) {
			item.setSha("");
		}
		
		try {
			SyncPageFile.packPage(localPageItem.getCurOwnerBookId(),
					localPageItem.getPageId(),version, item.getLastSyncModifiedTime());
		} catch (IOException e) {
			updateSyncCount(1, 0, 0, true);
			return false;
		}
		
		boolean isSuccess = false;
		
		try {
			SyncPageFile.uploadPage(item);
			isSuccess = true;
		} catch (WebStorageException webex) {
			if (webex.getErrorKind() == WebStorageException.UPLOAD_INIT_SHAVALUE_NOT_MATCH) {
				try {
					Node fileNode = MetaData.webStorage
							.getSpecificFileAttribute(localPageItem.getPageId() + MetaData.SYNC_PAGE_FILE_EXTENSION);
					if (fileNode != null) {
						String tmpSha = sWebStorageHelper.getNodeValue(fileNode,
								MetaData.SYNC_PAGE_FILE_ATTR_SHA);
						if (tmpSha != null) {
							if (tmpSha.equals(item.getSha())) {
								item.setSha("");
								try {
									SyncPageFile.uploadPage(item);
									isSuccess = true;
								} catch (Exception e1) {
									return false;
								}
							}
							// BEGIN: Shane_Wang@asus.com 2012-12-10
							else {
								SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.PageTable.uri, localPageItem.getPageId(), tmpSha));
								return false;
							}
							// END: Shane_Wang@asus.com 2012-12-10
						}
						else{
							return false;
						}
					}
					else{
						return false;
					}
				} catch (WebStorageException webe) {
					// BEGIN: Better
					if ((webe.getErrorKind() == WebStorageException.LOGIN_ERROR) 
							|| (webe.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
						throw webe;
					}
					return false;
					// END: Better
				} catch (Exception e2) {
					return false;
				}
			}
			else{
				return false;
			}
			
			// BEGIN: Better
			if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
					|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
				throw webex;
			}
			// END: Better
		} catch (Exception e) {
			return false;
		}
		
		if (!isSuccess) {
			updateSyncCount(1, 0, 0, true);
			return false;
		}

		return true;
	}
	//End   Darwin_Yu@asus.com
	public boolean uploadLocalPage(LocalPageItem localPageItem,
			SyncPageItem remotePageItem) throws WebStorageException {
		try {
			SyncPageFile.packPage(localPageItem.getCurOwnerBookId(),
					localPageItem.getPageId());
		} catch (IOException e) {
			updateSyncCount(1, 0, 0, true);
			return false;
		}
		SyncPageItem item = getUploadInfo(localPageItem, remotePageItem);
		if ((remotePageItem == null) || remotePageItem.isDeleted()) {
			item.setSha("");
		}
		
		boolean isSuccess = false;
		
		try {
			SyncPageFile.uploadPage(item);
			isSuccess = true;
		} catch (WebStorageException webex) {
			if (webex.getErrorKind() == WebStorageException.UPLOAD_INIT_SHAVALUE_NOT_MATCH) {
				try {
					Node fileNode = MetaData.webStorage
							.getSpecificFileAttribute(localPageItem.getPageId() + MetaData.SYNC_PAGE_FILE_EXTENSION);
					if (fileNode != null) {
						String tmpSha = sWebStorageHelper.getNodeValue(fileNode,
								MetaData.SYNC_PAGE_FILE_ATTR_SHA);
						if (tmpSha != null) {
							if (tmpSha.equals(item.getSha())) {
								item.setSha("");
								try {
									SyncPageFile.uploadPage(item);
									isSuccess = true;
								} catch (Exception e1) {
									return false;
								}
							}
							// BEGIN: Shane_Wang@asus.com 2012-12-10
							else {
								SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.PageTable.uri, localPageItem.getPageId(), tmpSha));
								return false;
							}
							// END: Shane_Wang@asus.com 2012-12-10
						}
						else {
							return false;
						}
					}
					else {
						return false;
					}
				} catch (WebStorageException webe) {
					// BEGIN: Better
					if ((webe.getErrorKind() == WebStorageException.LOGIN_ERROR) 
							|| (webe.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
						throw webe;
					}
					return false;
					// END: Better
				} catch (Exception e2) {
					return false;
				}
				
			}
			else {
				return false;
			}
			
			// BEGIN: Better
			if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
					|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
				throw webex;
			}
			// END: Better
		} catch (Exception e) {
			return false;
		}
		
		if (!isSuccess) {
			updateSyncCount(1, 0, 0, true);
			return false;
		}

		ContentValues cv = new ContentValues();
		cv.put(MetaData.PageTable.LASTSYNC_MODIFYTIME,
				localPageItem.getCurModifiedTime());
		if ((remotePageItem == null) || remotePageItem.isDeleted()) {
			cv.put(MetaData.PageTable.LASTSYNC_OWNER,
					localPageItem.getCurOwnerBookId());
		}
		mContentResolver.update(MetaData.PageTable.uri, cv,
				"created_date = ?",
				new String[] { Long.toString(localPageItem.getPageId()) });
		
		updateSyncCount(1, 0, -1, true);

		return true;
	}

	private boolean renameLocalPageAndDownloadRemotePage(
			LocalPageItem localPageItem, SyncPageItem remotePageItem) throws WebStorageException {
		SyncPageItem remotePageItemCopy = new SyncPageItem(remotePageItem);

		// rename local page
		Cursor cursor = mContentResolver
				.query(MetaData.PageTable.uri,
						null,
						"created_date = ?",
						new String[] { Long.toString(localPageItem.getPageId()) },
						null);
		cursor.moveToFirst();
		long newPageId = System.currentTimeMillis() + SyncHelper.SYNC_PAGE_ID_BASE;
		SyncFilesWorkTask.mNeedReIndexPageIDList.add(newPageId);//RICHARD
		NotePage notePage = new NotePage(mContext, cursor);
		notePage.setCreatedTime(newPageId);
		notePage.setLastSyncModifiedTime(notePage.getModifiedTime());
		NoteBook noteBook = mBookCase.getNoteBook(notePage.getOwnerBookId());
		File orgPageDir = new File(MetaData.DATA_DIR
				+ notePage.getOwnerBookId() + "/" + localPageItem.getPageId());
		File newPageDir = new File(MetaData.DATA_DIR
				+ notePage.getOwnerBookId() + "/" + newPageId);
		copyFile(orgPageDir, newPageDir);
		cursor.close();
		
		noteBook.addPageFromweb(notePage);
		
		if (remotePageItem.getPageId() == MetaData.CurrentEditPageId) {
        	reloadPage(newPageId);
        }

		// upload local new page
		try {
			SyncPageFile.packPage(notePage.getOwnerBookId(),
					notePage.getCreatedTime());
		} catch (IOException e) {
			updateSyncCount(1, 1, 1, true);
			return false;
		}
		remotePageItemCopy.setPageId(notePage.getCreatedTime());
		remotePageItemCopy.setSha("");
		remotePageItemCopy.setLastSyncModifiedTime(notePage.getModifiedTime());
		remotePageItemCopy.setLastSyncOwnerBookId(notePage
				.getLastSyncOwnerBookId());
		remotePageItemCopy.setBookmark(notePage.isBookmark());
		remotePageItemCopy.setDeleted(notePage.isDeleted());
		remotePageItemCopy.setNotebookTitle(noteBook.getTitle());
		remotePageItemCopy.setNotebookBakColor(noteBook.getBookColor());
		remotePageItemCopy.setNotebookGridLine(noteBook.getGridType());
		remotePageItemCopy.setNotebookPhoneMemo(noteBook.isPhoneMemo());
		try {
			SyncPageFile.uploadPage(remotePageItemCopy);
		} catch (Exception e) {
			updateSyncCount(1, 1, 1, true);
			
			// BEGIN: Better
			if (e instanceof WebStorageException) {
				WebStorageException webex = (WebStorageException) e;
				if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
						|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
					throw webex;
				}
			}
			// END: Better
			
			return false;
		}
		
		updateSyncCount(1, 1, 0, true);

		// download remote page data
		downloadRemotePage(localPageItem, remotePageItem);
		
		return true;
	}
	
	private boolean renameLocalPageAndDownloadRemotePage(
			LocalPageItem localPageItem, SyncPageItem remotePageItem ,String version) throws WebStorageException {
		// rename local page
		Cursor cursor = mContentResolver
				.query(MetaData.PageTable.uri,
						null,
						"created_date = ?",
						new String[] { Long.toString(localPageItem.getPageId()) },
						null);
		cursor.moveToFirst();
		long newPageId = System.currentTimeMillis() + SyncHelper.SYNC_PAGE_ID_BASE;
		SyncFilesWorkTask.mNeedReIndexPageIDList.add(newPageId);//RICHARD
		NotePage notePage = new NotePage(mContext, cursor);
		notePage.setCreatedTime(newPageId);
		NoteBook noteBook = mBookCase.getNoteBook(notePage.getOwnerBookId());
		File orgPageDir = new File(MetaData.DATA_DIR
				+ notePage.getOwnerBookId() + "/" + localPageItem.getPageId());
		File newPageDir = new File(MetaData.DATA_DIR
				+ notePage.getOwnerBookId() + "/" + newPageId);
		copyFile(orgPageDir, newPageDir);
		cursor.close();
		//begin darwin
		ContentValues cvItem = new ContentValues();
		cvItem.put(MetaData.ItemTable.ID, newPageId);
        mContentResolver.update(MetaData.ItemTable.uri, cvItem, "_id = ?", new String[] {Long.toString(localPageItem.getPageId())});
        
        ContentValues cvDoodle = new ContentValues();
        cvDoodle.put(MetaData.DoodleTable.ID, newPageId);
        mContentResolver.update(MetaData.DoodleTable.uri, cvDoodle, "_id = ?", new String[] {Long.toString(localPageItem.getPageId())});
		
		ContentValues cvAttachment = new ContentValues();
		cvAttachment.put(MetaData.AttachmentTable.ID, newPageId);
        mContentResolver.update(MetaData.AttachmentTable.uri, cvAttachment, "_id = ?", new String[] {Long.toString(localPageItem.getPageId())});
        
        noteBook.addPageFromweb(notePage);
        
        if (remotePageItem.getPageId() == MetaData.CurrentEditPageId) {
        	reloadPage(newPageId);
        }
        
		// upload local new page
        LocalPageItem localPageItem_temp = new LocalPageItem();
        localPageItem_temp.setPageId(notePage.getCreatedTime());
        localPageItem_temp.setSha("");
        localPageItem_temp.setLastSyncModifiedTime(notePage.getModifiedTime());
        localPageItem_temp.setCurModifiedTime(notePage.getModifiedTime());
        localPageItem_temp.setLastSyncOwnerBookId(notePage
				.getLastSyncOwnerBookId());
        localPageItem_temp.setBookmark(notePage.isBookmark());
        localPageItem_temp.setDeleted(notePage.isDeleted());
        localPageItem_temp.setCurOwnerBookId(notePage.getOwnerBookId());
        // upload local page to override remote page
		//Begin Darwin_Yu@asus.com
		mFullPageChanged = true;
		findUploadElement(localPageItem_temp.getPageId());
		if (uploadLocalPageElement(localPageItem_temp, null)) {
			if (uploadLocalPageFromRename(localPageItem_temp, null,version,noteBook)) {
				finishUploadPageElement(localPageItem_temp, null);
			} else {
				updateSyncCount(1, 1, 1, true);
			}
		}
		mFullPageChanged = false;
        //end  darwin

		// download remote page data
		//Begin Darwin_Yu@asus.com
		mFullLocalPageChanged = true;
		findDownloadElement(remotePageItem.getPageId());
		if (downloadRemotePageElement(localPageItem, remotePageItem,this,notePage)) {
			downloadRemotePageNotAddPage(localPageItem, remotePageItem);
			unpackRemotePageElement(localPageItem, remotePageItem, this);
			insertDoodleItemAttachmentDB(remotePageItem.getPageId(),remotePageItem.getLastSyncOwnerBookId());
		}
		mFullLocalPageChanged = false;
		//End   Darwin_Yu@asus.com
		
		return true;
	}

	private void deletePageElement(long pageId) throws WebStorageException
	{
		deleteRemoteFile(pageId + MetaData.SYNC_DOODLE_FILE_EXTENSION);
		deleteRemoteFile(pageId + MetaData.SYNC_ITEM_FILE_EXTENSION);
		try
		{
			List<String> nameList = SyncPageFile.getServerPageAttachmentNameList(Long.toString(pageId));
			for(String name : nameList)
			{
				deleteRemoteFile(name);
			}
		}
		catch(Exception e)
		{
			Log.i("darwin", e.toString());
			
			// BEGIN: Better
			if (e instanceof WebStorageException) {
				WebStorageException webex = (WebStorageException) e;
				if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
						|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
					throw webex;
				}
			}
			// END: Better
		}
	}
	
	private void deleteRemoteFile(String name) throws WebStorageException
	{
		try
		{
			MetaData.webStorage.deleteRemoteFile(name);
		}
		catch(Exception e)
		{
			Log.i("darwin", e.toString());
			
			// BEGIN: Better
			if (e instanceof WebStorageException) {
				WebStorageException webex = (WebStorageException) e;
				if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
						|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
					throw webex;
				}
			}
			// END: Better
		}
	}
	
	private boolean deleteRemotePage(LocalPageItem localPageItem,
			SyncPageItem remotePageItem) throws WebStorageException {
		// upload blank page
		String strUploadDir = MetaData.DATA_DIR + MetaData.SYNC_TEMP_UPLOAD_DIR;
		File uploadDir = new File(strUploadDir);
		if (!uploadDir.exists()) {
			uploadDir.mkdirs();
		}
		File blankFile = new File(strUploadDir, localPageItem.getPageId()
				+ MetaData.SYNC_PAGE_FILE_EXTENSION);
		try {
			blankFile.createNewFile();
		} catch (IOException e) {
			updateSyncCount(1, 1, 1, true);
			return false;
		}

		SyncPageItem item = getUploadInfo(localPageItem, remotePageItem);
		boolean isSuccess = false;
		
		try {
			SyncPageFile.uploadPage(item);
			isSuccess = true;
		} catch (WebStorageException webex) {
			if (webex.getErrorKind() == WebStorageException.UPLOAD_INIT_SHAVALUE_NOT_MATCH) {
				try {
					Node fileNode = MetaData.webStorage
							.getSpecificFileAttribute(localPageItem.getPageId() + MetaData.SYNC_PAGE_FILE_EXTENSION);
					if (fileNode != null) {
						String tmpSha = sWebStorageHelper.getNodeValue(fileNode,
								MetaData.SYNC_PAGE_FILE_ATTR_SHA);
						if (tmpSha != null) {
							if (tmpSha.equals(item.getSha())) {
								item.setSha("");
								try {
									SyncPageFile.uploadPage(item);
									isSuccess = true;
								} catch (Exception e1) {
									// BEGIN: Better
									if (e1 instanceof WebStorageException) {
										WebStorageException webe = (WebStorageException) e1;
										if ((webe.getErrorKind() == WebStorageException.LOGIN_ERROR) 
												|| (webe.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
											throw webe;
										}
									}
									// END: Better
								}
							}
						}
					}
				} catch (WebStorageException webe) {
					// BEGIN: Better
					if ((webe.getErrorKind() == WebStorageException.LOGIN_ERROR) 
							|| (webe.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
						throw webe;
					}
					// END: Better
				} catch (Exception e2) {
					
				}
			}
			
			// BEGIN: Better
			if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
					|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
				throw webex;
			}
			// END: Better
		} catch (Exception e) {
			
		}
		
		if (!isSuccess) {
			updateSyncCount(1, 1, 1, true);
			return false;
		}

		// update local record
		ContentValues cv = new ContentValues();
		cv.put(MetaData.PageTable.LASTSYNC_MODIFYTIME,
				localPageItem.getCurModifiedTime());
		cv.put(MetaData.PageTable.LASTSYNC_OWNER,
				localPageItem.getCurOwnerBookId());
		mContentResolver.update(MetaData.PageTable.uri, cv, "created_date = ?",
				new String[] { Long.toString(localPageItem.getPageId()) });

		updateSyncCount(1, 1, 0, true);

		return true;
	}

	public void ComparePageList(AsyncTask task) throws Exception {
		for (int i = 0; i < mRemotePageList.size(); i++) {
			SyncPageItem remotePageItem = mRemotePageList.get(i);
			LocalPageItem localPageItem = null;

			// find remote page in local page list
			for (LocalPageItem localPageItemTemp : mLocalPageList) {
				if (localPageItemTemp.getPageId() == remotePageItem.getPageId()) {
					localPageItem = localPageItemTemp;
					break;
				}
			}

			// process remote list
			if (localPageItem == null) { // local: not exist
				if (!remotePageItem.isDeleted()) { // remote: not deleted
					// download and add remote page
					downloadRemotePage(localPageItem, remotePageItem);
				}
			} else { // local: exist
				if (!remotePageItem.isDeleted() && !localPageItem.isDeleted()) {
					if (remotePageItem.getLastSyncModifiedTime() == localPageItem
							.getLastSyncModifiedTime()) {
						if (localPageItem.getCurModifiedTime() != localPageItem
								.getLastSyncModifiedTime()) {
							// upload local page to override remote page
							uploadLocalPage(localPageItem, remotePageItem);
						} else {
							// all not modified
							updateSyncCount(1, 0, -1, false);
						}
					} else {
						if (localPageItem.getCurModifiedTime() == localPageItem
								.getLastSyncModifiedTime()) {
							// download remote page to override local page
							downloadRemotePage(localPageItem, remotePageItem);
						} else {
							// rename local page and download remote page
							renameLocalPageAndDownloadRemotePage(localPageItem,
									remotePageItem);
						}
					}

					// local: not deleted; remote: not deleted
					if (localPageItem.getCurOwnerBookId() == localPageItem
							.getLastSyncOwnerBookId()) {
						if (remotePageItem.getLastSyncOwnerBookId() != localPageItem
								.getLastSyncOwnerBookId()) {
							// use remote book id to override local book id

							// create dest book
							NoteBook noteBookDest = mBookCase
									.getNoteBook(remotePageItem
											.getLastSyncOwnerBookId());
							if (noteBookDest == null) {
								noteBookDest = getNoteBook(remotePageItem);
								mBookCase.addNewBookFromWebPage(noteBookDest);
							}
							
							TreeSet<SimplePageInfo> pages = new TreeSet<SimplePageInfo>(new PageComparator());
							SimplePageInfo pageInfo = new SimplePageInfo(localPageItem.getCurOwnerBookId(), 
									localPageItem.getPageId(), 0, 0);
							pages.add(pageInfo);
							noteBookDest.movePagesFrom(pages);
							
							ContentValues cv = new ContentValues();
							cv.put(MetaData.PageTable.LASTSYNC_OWNER, remotePageItem.getLastSyncOwnerBookId());
							mContentResolver.update(MetaData.PageTable.uri, cv, 
									"created_date = ?", 
									new String[] {Long.toString(localPageItem.getPageId())});
						}
					} else {
						// use local book id to override remote book id

						// update remote book id
						remotePageItem.setLastSyncOwnerBookId(localPageItem
								.getCurOwnerBookId());
						try {
							SyncPageFile.updatePageAttribute(remotePageItem);
						} catch (Exception e) {
							// BEGIN: Better
							if (e instanceof WebStorageException) {
								WebStorageException webex = (WebStorageException) e;
								if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
										|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
									throw webex;
								}
							}
							// END: Better
							continue;
						}

						// update local record
						ContentValues cv = new ContentValues();
						cv.put(MetaData.PageTable.LASTSYNC_OWNER,
								localPageItem.getCurOwnerBookId());
						mContentResolver.update(MetaData.PageTable.uri, cv,
								"created_date = ?", new String[] { Long
										.toString(localPageItem.getPageId()) });
					}
				} else if (remotePageItem.isDeleted()
						&& !localPageItem.isDeleted()) {
					if (localPageItem.getCurModifiedTime() == localPageItem
							.getLastSyncModifiedTime()) {
						// delete local page
						mBookCase.getNoteBook(localPageItem.getCurOwnerBookId())
								.deletePagefromWeb(localPageItem.getPageId());

						updateSyncCount(1, 0, -1, true);
					} else {
						// upload local page
						uploadLocalPage(localPageItem, remotePageItem);
					}
				} else if (localPageItem.isDeleted()
						&& !remotePageItem.isDeleted()) {
					if (remotePageItem.getLastSyncModifiedTime() != localPageItem
							.getLastSyncModifiedTime()) {
						// download remote page
						downloadRemotePage(localPageItem, remotePageItem);
					} else {
						// delete remote page
						deleteRemotePage(localPageItem, remotePageItem);
					}
				} else { // both deleted
					// delete local page
					mContentResolver.delete(MetaData.PageTable.uri,
							"created_date = ?", new String[] { Long
									.toString(localPageItem.getPageId()) });
					
					updateSyncCount(1, 1, 0, true);
				}
			}
			
			if (task.isCancelled()) {
				Log.v(TAG, "Sync task is cancelled");
				return;
			}
		}

		// process local list
		for (LocalPageItem localItem : mLocalPageList) {
			boolean bExist = false;
			for (SyncPageItem remoteItem : mRemotePageList) {
				if (remoteItem.getPageId() == localItem.getPageId()) {
					bExist = true;
					break;
				}
			}
			if (!bExist) { // local: exist; remote: not exist

				if (localItem.isDeleted()) { // local: deleted
					// delete local page
					mContentResolver
							.delete(MetaData.PageTable.uri, "created_date = ?",
									new String[] { Long.toString(localItem
											.getPageId()) });

					updateSyncCount(1, 1, 0, true);
				} else { // local: not deleted
					// upload local page
					uploadLocalPage(localItem, null);
				}
			}
			
			if (task.isCancelled()) {
				Log.v(TAG, "Sync task is cancelled");
				return;
			}
		}
	}
	
	private LocalPageItem IsPageExistAndModifyDuringSyncLocalDB(long pageId)
	{
		Cursor cursor = mContentResolver.query(MetaData.PageTable.uri, null,
				MetaData.SYNC_CUSTOM_DB_QUERY_TAG + "userAccount = ? AND created_date = ?",
				new String[] { Long.toString(SyncFilesWorkTask.mSyncUserId) ,Long.toString(pageId)},
				null);
		if(cursor != null)
		{
			cursor.moveToFirst();
			if(!cursor.isAfterLast())
			{
				LocalPageItem pageItem = new LocalPageItem();
				pageItem.load(cursor);
					return pageItem;
			}
			cursor.close();
		}
		return null;
	}
	private String IsPageExistInServer(long pageId) throws WebStorageException
	{
		String fileName = pageId + ".file";
		try
		{
			Node fileNode = MetaData.webStorage.getSpecificFileAttribute(fileName);
			if (fileNode != null) {
				String version = null;
				String tmpVersion = sWebStorageHelper.getNodeValue(fileNode, 
						MetaData.SYNC_FILE_VERSION);
				if ((tmpVersion == null) || tmpVersion.isEmpty()) {
					tmpVersion = sWebStorageHelper.getNodeValue(fileNode, "VN");
					if ((tmpVersion != null) && !tmpVersion.isEmpty()) {
						version = tmpVersion;
					}
				} else {
					version = tmpVersion;
				}
				return version;
			}
		}
		catch(Exception e)
		{
			Log.i("IsPageExistInServer", e.toString());
			
			// BEGIN: Better
			if (e instanceof WebStorageException) {
				WebStorageException webex = (WebStorageException) e;
				if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
						|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
					throw webex;
				}
			}
			// END: Better
		}
		return null;
	}
	public static void rmdir(String strDir) {
		File dir = new File(strDir);
		if (dir.isDirectory()) {
			for (String strFile : dir.list()) {
				File file = new File(strDir + "/" + strFile);
				if (file.isDirectory()) {
					rmdir(strDir + "/" + strFile);
				} else if (file.isFile()) {
					file.delete();
				}
			}
			dir.delete();
		} else if (dir.isFile()) {
			dir.delete();
		}
	}
	
	// BEGIN: Better
	private LocalPageItem savePage(SyncPageItem remotePageItem) {
		try {
			Intent intent = new Intent(MetaData.SAVE_PAGE);
			mContext.sendBroadcast(intent);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int count = 0;
		while ((!MetaData.SavingEditPage) && (count < 10)) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			count++;
		}
		if (MetaData.SavingEditPage) {
			while (!MetaData.SavedEditPage) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		return IsPageExistAndModifyDuringSyncLocalDB(remotePageItem.getPageId());
	}
	
	private void reloadPage(long pageId) {
		try {
			Intent intent = new Intent(MetaData.RELOAD_PAGE);
			intent.putExtra(MetaData.RELOAD_PAGE_ID, pageId);
			mContext.sendBroadcast(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean renameAndUploadLocalPage(LocalPageItem localPageItem, String version) throws WebStorageException {
		// rename local page
		Cursor cursor = mContentResolver
				.query(MetaData.PageTable.uri,
						null,
						"created_date = ?",
						new String[] { Long.toString(localPageItem.getPageId()) },
						null);
		cursor.moveToFirst();
		long newPageId = System.currentTimeMillis() + SyncHelper.SYNC_PAGE_ID_BASE;
		NotePage notePage = new NotePage(mContext, cursor);
		notePage.setCreatedTime(newPageId);
		notePage.setLastSyncModifiedTime(notePage.getModifiedTime());
		NoteBook noteBook = mBookCase.getNoteBook(notePage.getOwnerBookId());
		File orgPageDir = new File(MetaData.DATA_DIR
				+ notePage.getOwnerBookId() + "/" + localPageItem.getPageId());
		File newPageDir = new File(MetaData.DATA_DIR
				+ notePage.getOwnerBookId() + "/" + newPageId);
		copyFile(orgPageDir, newPageDir);
		cursor.close();

		ContentValues cvItem = new ContentValues();
		cvItem.put(MetaData.ItemTable.ID, newPageId);
        mContentResolver.update(MetaData.ItemTable.uri, cvItem, "_id = ?", new String[] {Long.toString(localPageItem.getPageId())});
        
        ContentValues cvDoodle = new ContentValues();
        cvDoodle.put(MetaData.DoodleTable.ID, newPageId);
        mContentResolver.update(MetaData.DoodleTable.uri, cvDoodle, "_id = ?", new String[] {Long.toString(localPageItem.getPageId())});
		
		ContentValues cvAttachment = new ContentValues();
		cvAttachment.put(MetaData.AttachmentTable.ID, newPageId);
        mContentResolver.update(MetaData.AttachmentTable.uri, cvAttachment, "_id = ?", new String[] {Long.toString(localPageItem.getPageId())});
        
        noteBook.addPageFromweb(notePage);
        
        if (localPageItem.getPageId() == MetaData.CurrentEditPageId) {
        	reloadPage(newPageId);
        }
        
		// upload local new page
        LocalPageItem localPageItem_temp = new LocalPageItem();
        localPageItem_temp.setPageId(notePage.getCreatedTime());
        localPageItem_temp.setSha("");
        localPageItem_temp.setLastSyncModifiedTime(notePage.getModifiedTime());
        localPageItem_temp.setLastSyncOwnerBookId(notePage
				.getLastSyncOwnerBookId());
        localPageItem_temp.setBookmark(notePage.isBookmark());
        localPageItem_temp.setDeleted(notePage.isDeleted());
        localPageItem_temp.setCurOwnerBookId(notePage.getOwnerBookId());
        // upload local page to override remote page
		mFullPageChanged = true;
		findUploadElement(localPageItem_temp.getPageId());
		if (uploadLocalPageElement(localPageItem_temp, null)) {
			if (uploadLocalPageFromRename(localPageItem_temp, null,version,noteBook)) {
				finishUploadPageElement(localPageItem_temp, null);
			}
		}
		mFullPageChanged = false;
		
		return true;
	}
	
	private boolean renameAndUploadLocalPage(LocalPageItem localPageItem, SyncPageItem remotePageItem) throws WebStorageException {
		SyncPageItem remotePageItemCopy = new SyncPageItem(remotePageItem);
		
		// rename local page
		Cursor cursor = mContentResolver
				.query(MetaData.PageTable.uri,
						null,
						"created_date = ?",
						new String[] { Long.toString(localPageItem.getPageId()) },
						null);
		cursor.moveToFirst();
		long newPageId = System.currentTimeMillis() + SyncHelper.SYNC_PAGE_ID_BASE;
		NotePage notePage = new NotePage(mContext, cursor);
		notePage.setCreatedTime(newPageId);
		notePage.setLastSyncModifiedTime(notePage.getModifiedTime());
		NoteBook noteBook = mBookCase.getNoteBook(notePage.getOwnerBookId());
		File orgPageDir = new File(MetaData.DATA_DIR
				+ notePage.getOwnerBookId() + "/" + localPageItem.getPageId());
		File newPageDir = new File(MetaData.DATA_DIR
				+ notePage.getOwnerBookId() + "/" + newPageId);
		copyFile(orgPageDir, newPageDir);
		cursor.close();
		
		noteBook.addPageFromweb(notePage);
		
		if (localPageItem.getPageId() == MetaData.CurrentEditPageId) {
        	reloadPage(newPageId);
        }

		// upload local new page
		try {
			SyncPageFile.packPage(notePage.getOwnerBookId(),
					notePage.getCreatedTime());
		} catch (IOException e) {
			updateSyncCount(1, 1, 1, true);
			return false;
		}
		remotePageItemCopy.setPageId(notePage.getCreatedTime());
		remotePageItemCopy.setSha("");
		remotePageItemCopy.setLastSyncModifiedTime(notePage.getModifiedTime());
		remotePageItemCopy.setLastSyncOwnerBookId(notePage
				.getLastSyncOwnerBookId());
		remotePageItemCopy.setBookmark(notePage.isBookmark());
		remotePageItemCopy.setDeleted(notePage.isDeleted());
		remotePageItemCopy.setNotebookTitle(noteBook.getTitle());
		remotePageItemCopy.setNotebookBakColor(noteBook.getBookColor());
		remotePageItemCopy.setNotebookGridLine(noteBook.getGridType());
		remotePageItemCopy.setNotebookPhoneMemo(noteBook.isPhoneMemo());
		try {
			SyncPageFile.uploadPage(remotePageItemCopy);
		} catch (Exception e) {
			updateSyncCount(1, 1, 1, true);
			
			// BEGIN: Better
			if (e instanceof WebStorageException) {
				WebStorageException webex = (WebStorageException) e;
				if ((webex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
						|| (webex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
					throw webex;
				}
			}
			// END: Better
			
			return false;
		}
		
		updateSyncCount(1, 1, 0, true);
		
		return true;
	}
	// END: Better
	
	// BEGIN: Shane_Wang@asus.com 2012-12-11
	public static ArrayList<DBInsertItem>shaUpdateList = new ArrayList<DBInsertItem>();
	
	public static void AddToShaUpdateList(DBInsertItem dbInsertItem) {
		shaUpdateList.add(dbInsertItem);
	}
	// END: Shane_Wang@asus.com 2012-12-11
	
	//end   darwin
	public void ComparePageList(AsyncTask task, String version) throws Exception {
		shaUpdateList.clear();
		for (int i = 0; i < mRemotePageList.size(); i++) {
			SyncPageItem remotePageItem = mRemotePageList.get(i);
			LocalPageItem localPageItem = null;

			// find remote page in local page list
			for (LocalPageItem localPageItemTemp : mLocalPageList) {
				if (localPageItemTemp.getPageId() == remotePageItem.getPageId()) {
					localPageItem = localPageItemTemp;
					break;
				}
			}
			
			// BEGIN: Better
			String ver = remotePageItem.getVersion();
			if ((ver != null) && (!SyncHelper.isVersionSupported(ver))) {
				if (!MetaData.NotSupportedVersionList.contains(ver)) {
					MetaData.NotSupportedVersionList.add(ver);
				}

				if (task.isCancelled()) {
					Log.v(TAG, "Sync task is cancelled");
					return;
				} else {
					if (localPageItem != null) {
						updateSyncCount(1, 0, -1, false);
					}
					continue;
				}
			}
			// END: Better
			
			mCurrentVersion = ver;
			
			if (remotePageItem.getPageId() == MetaData.CurrentEditPageId) {
				localPageItem = savePage(remotePageItem);
				if ((localPageItem != null) && (localPageItem.getLastSyncModifiedTime() == localPageItem.getCurModifiedTime())) {
					localPageItem = null;
				}
			}
			
			if (ver == null) {
				if (!MetaData.NotSupportedVersionList.contains(mContext.getString(R.string.sync_file_no_version))) {
					MetaData.NotSupportedVersionList.add(mContext.getString(R.string.sync_file_no_version));
				}
				if (localPageItem == null) {
					continue;
				}
			}

			// process remote list
			if (localPageItem == null) {
				if (!remotePageItem.isDeleted()) {
					// download and add remote page
					
					//Begin Darwin_Yu@asus.com
					LocalPageItem localPageItemTemp = IsPageExistAndModifyDuringSyncLocalDB(remotePageItem.getPageId());
					if(localPageItemTemp == null) {
						/**
						 * case 1: remote is modified. local is not exist.
						 * see also: sync page flow chart.
						 */
						if ((!ver.equalsIgnoreCase(MetaData.SYNC_VERSION_3_1)) && (!ver.equalsIgnoreCase(MetaData.SYNC_VERSION_2))) {
							mFullLocalPageChanged = true;
							findDownloadElement(remotePageItem.getPageId());
							if (downloadRemotePageElement(localPageItem, remotePageItem,this,null)) {
								downloadRemotePageNew(localPageItem, remotePageItem);
								unpackRemotePageElement(localPageItem, remotePageItem, this);
								insertDoodleItemAttachmentDB(remotePageItem.getPageId(),remotePageItem.getLastSyncOwnerBookId());
							}
							mFullLocalPageChanged = false;
						} else { // Better
							downloadRemotePage(localPageItemTemp, remotePageItem);
						}
					}
					else
					{
						if((localPageItemTemp.getLastSyncModifiedTime() != localPageItemTemp.getCurModifiedTime())) {
							/**
							 * case 2: after being compared, local has been modified.
							 *         local is modified. remote is modified.
							 * see also: sync page flow chart.
							 */
							if ((!ver.equalsIgnoreCase(MetaData.SYNC_VERSION_3_1)) && (!ver.equalsIgnoreCase(MetaData.SYNC_VERSION_2))) {
								renameLocalPageAndDownloadRemotePage(localPageItemTemp,
										remotePageItem,version);
							} else {//Better
								renameLocalPageAndDownloadRemotePage(localPageItemTemp, remotePageItem);
							}
						}
						else {
							/**
							 * case 3: remote is modified. local is not modified.
							 * see also: sync page flow chart.
							 */
							if (remotePageItem.getPageId() == MetaData.CurrentEditPageId) {
								localPageItem = savePage(remotePageItem);
							}
							
							if ((!ver.equalsIgnoreCase(MetaData.SYNC_VERSION_3_1)) && (!ver.equalsIgnoreCase(MetaData.SYNC_VERSION_2))) {
								if ((localPageItem != null) && (localPageItem.getLastSyncModifiedTime() != localPageItem.getCurModifiedTime())) {
									renameAndUploadLocalPage(localPageItem, version);
								}
								findDownloadElement(remotePageItem.getPageId());
								if (downloadRemotePageElement(localPageItem, remotePageItem,this,null)) {
									downloadRemotePageNotAddPage(localPageItem, remotePageItem);
									unpackRemotePageElement(localPageItem, remotePageItem, this);
									insertDoodleItemAttachmentDB(remotePageItem.getPageId(),remotePageItem.getLastSyncOwnerBookId());
									if (remotePageItem.getPageId() == MetaData.CurrentEditPageId) {
										reloadPage(remotePageItem.getPageId());
									}
								}
								mFullLocalPageChanged = false;
							} else { // Better
								if ((localPageItem != null) && (localPageItem.getLastSyncModifiedTime() != localPageItem.getCurModifiedTime())) {
									renameAndUploadLocalPage(localPageItem, remotePageItem);
								}
								downloadRemotePage(localPageItem, remotePageItem);
								if (remotePageItem.getPageId() == MetaData.CurrentEditPageId) {
									reloadPage(remotePageItem.getPageId());
								}
							}
						}
					}
				}
				else
				{//remote is deleted.
					
					LocalPageItem localPageItemTemp = IsPageExistAndModifyDuringSyncLocalDB(remotePageItem.getPageId());
					
					if(localPageItemTemp != null)
					{//local is exist.

						if((localPageItemTemp.getLastSyncModifiedTime() != localPageItemTemp.getCurModifiedTime()))
						{
							/**
							 * case 4: after being compared, local has been modified.
							 *         local is modified. remote is deleted.
							 * see also: sync page flow chart.
							 */
							mFullPageChanged = true;
							findUploadElement(localPageItemTemp.getPageId());
							if (uploadLocalPageElement(localPageItemTemp, null)) {
								if (uploadLocalPage(localPageItemTemp, null,version)) {
									finishUploadPageElement(localPageItemTemp, null);
								}
							}
							mFullPageChanged = false;
						}
						else
						{
							/**
							 * case 5: remote is deleted. local is not modified.
							 * see also: sync page flow chart.
							 */
							NoteBook book = mBookCase.getNoteBook(remotePageItem.getLastSyncOwnerBookId());
							if (book != null) {
								book.deletePagefromWeb(remotePageItem.getPageId());
							} else {
								mContentResolver.delete(MetaData.PageTable.uri, "(created_date = ?)", 
							       		 new String[] { Long.toString(remotePageItem.getPageId()) });
							    mContentResolver.delete(MetaData.TimestampTable.uri, "owner = ?", new String[]{Long.toString(remotePageItem.getPageId())});
							    mContentResolver.delete(MetaData.ItemTable.uri, "_id = ?", new String[] {Long.toString(remotePageItem.getPageId())});
							    mContentResolver.delete(MetaData.DoodleTable.uri, "_id = ?", new String[] {Long.toString(remotePageItem.getPageId())});
							    mContentResolver.delete(MetaData.AttachmentTable.uri, "_id = ?", new String[] {Long.toString(remotePageItem.getPageId())});
							}
						}
					}
					
					else
					{
						/**
						 * case 6: remote is deleted. local is not exist.
						 *         note: the codes below is useless. You can delete 
						 *         all codes below.
						 * see also: sync page flow chart.
						 */
						NoteBook book = mBookCase.getNoteBook(remotePageItem.getLastSyncOwnerBookId());
						if(book != null)
						{
							book.deletePagefromWeb(remotePageItem.getPageId());
						} else {
							mContentResolver.delete(MetaData.PageTable.uri, "(created_date = ?)", 
						       		 new String[] { Long.toString(remotePageItem.getPageId()) });
						    mContentResolver.delete(MetaData.TimestampTable.uri, "owner = ?", new String[]{Long.toString(remotePageItem.getPageId())});
						    mContentResolver.delete(MetaData.ItemTable.uri, "_id = ?", new String[] {Long.toString(remotePageItem.getPageId())});
						    mContentResolver.delete(MetaData.DoodleTable.uri, "_id = ?", new String[] {Long.toString(remotePageItem.getPageId())});
						    mContentResolver.delete(MetaData.AttachmentTable.uri, "_id = ?", new String[] {Long.toString(remotePageItem.getPageId())});
						}
					}
				}
			}
			
			else 
			{//local is modified. remote is modified.
				if (ver == null) {
					if (!localPageItem.isDeleted()) {
						renameAndUploadLocalPage(localPageItem, MetaData.SYNC_CURRENT_VERSION);
						NoteBook book = mBookCase.getNoteBook(localPageItem.getCurOwnerBookId());
						if (book != null) {
							book.deletePagefromWeb(localPageItem.getPageId());
						} else {
							mContentResolver.delete(MetaData.PageTable.uri, "(created_date = ?)", 
						       		 new String[] { Long.toString(localPageItem.getPageId()) });
						    mContentResolver.delete(MetaData.TimestampTable.uri, "owner = ?", new String[]{Long.toString(localPageItem.getPageId())});
						    mContentResolver.delete(MetaData.ItemTable.uri, "_id = ?", new String[] {Long.toString(localPageItem.getPageId())});
						    mContentResolver.delete(MetaData.DoodleTable.uri, "_id = ?", new String[] {Long.toString(localPageItem.getPageId())});
						    mContentResolver.delete(MetaData.AttachmentTable.uri, "_id = ?", new String[] {Long.toString(localPageItem.getPageId())});
						}
					    updateSyncCount(1, 0, -1, true);
					}
				}
				/**
				 * case 7: local is modified. remote is modified.
				 * see also: sync page flow chart.
				 */
				else if (!remotePageItem.isDeleted() && !localPageItem.isDeleted()) 
				{
					// rename local page and download remote page
					if ((!ver.equalsIgnoreCase(MetaData.SYNC_VERSION_3_1)) && (!ver.equalsIgnoreCase(MetaData.SYNC_VERSION_2))) {
						renameLocalPageAndDownloadRemotePage(localPageItem,
								remotePageItem,version);
					} else { // Better
						renameLocalPageAndDownloadRemotePage(localPageItem, remotePageItem);
					}
				} 
				/**
				 * case 8: local is modified. remote is deleted.
				 * see also: sync page flow chart.
				 */
				else if (remotePageItem.isDeleted()
						&& !localPageItem.isDeleted()) 
				{
					mFullPageChanged = true;
					
					// upload local page
					//Begin Darwin_Yu@asus.com			
					findUploadElement(localPageItem.getPageId());
					if (uploadLocalPageElement(localPageItem, remotePageItem)) {
						if (uploadLocalPage(localPageItem, remotePageItem,version)) {
							finishUploadPageElement(localPageItem, remotePageItem);
						}
					}
					updateSyncCount(1, 0, -1, true);
					//End   Darwin_Yu@asus.com
					
					mFullPageChanged = false;
				} 
				/**
				 * case 9: local is deleted. remote is modified.
				 * see also: sync page flow chart.
				 */
				else if (localPageItem.isDeleted()
						&& !remotePageItem.isDeleted()) 
				{
					// download remote page
					if ((!ver.equalsIgnoreCase(MetaData.SYNC_VERSION_3_1)) && (!ver.equalsIgnoreCase(MetaData.SYNC_VERSION_2))) {
						//Begin Darwin_Yu@asus.com
						mFullLocalPageChanged = true;
						findDownloadElement(localPageItem.getPageId());
						if (downloadRemotePageElement(localPageItem, remotePageItem,this,null)) {
							downloadRemotePageNotAddPage(localPageItem, remotePageItem);
							unpackRemotePageElement(localPageItem, remotePageItem, this);
							insertDoodleItemAttachmentDB(remotePageItem.getPageId(),remotePageItem.getLastSyncOwnerBookId());
						}
						mFullLocalPageChanged = false;
						//End   Darwin_Yu@asus.com
					} else { // Better
						downloadRemotePage(localPageItem, remotePageItem);
					}
				} 
				
				else
				{
					/**
					 * case 10: local is deleted. remote is deleted.
					 * see also: sync page flow chart.
					 */
					// delete local page
					NoteBook book = mBookCase.getNoteBook(remotePageItem.getLastSyncOwnerBookId());
					if(book != null)
					{
						book.deletePagefromWeb(remotePageItem.getPageId());
					}
					else
					{
						mContentResolver.delete(MetaData.PageTable.uri, "(created_date = ?)", 
					       		 new String[] { Long.toString(remotePageItem.getPageId()) });
					    mContentResolver.delete(MetaData.TimestampTable.uri, "owner = ?", new String[]{Long.toString(remotePageItem.getPageId())});
					    mContentResolver.delete(MetaData.ItemTable.uri, "_id = ?", new String[] {Long.toString(remotePageItem.getPageId())});
					    mContentResolver.delete(MetaData.DoodleTable.uri, "_id = ?", new String[] {Long.toString(remotePageItem.getPageId())});
					    mContentResolver.delete(MetaData.AttachmentTable.uri, "_id = ?", new String[] {Long.toString(remotePageItem.getPageId())});
					}
					updateSyncCount(1, 1, -1, true);
				}
			}
			
			SyncPageFile.mListAttachmentModify.clear();
			
			if (task.isCancelled()) {
				Log.v(TAG, "Sync task is cancelled");
				return;
			}
		}
		
		// process local list
		for (LocalPageItem localItem : mLocalPageList) {
			boolean bExist = false;
			for (SyncPageItem remoteItem : mRemotePageList) {
				if (remoteItem.getPageId() == localItem.getPageId()) {
					bExist = true;
					break;
				}
			}
			if (!bExist) 
			{ // local: exist; remote: not exist
				
				// BEGIN: Better
				SyncPageItem exceptionItem = null;
				for (SyncPageItem item : MetaData.SyncExceptionList) {
					if (item.getPageId() == localItem.getPageId()) {
						exceptionItem = item;
						break;
					}
				}
				// END: Better
				if (exceptionItem == null) {
					if (localItem.isDeleted()) { // local: deleted
						String v = IsPageExistInServer(localItem.getPageId());
						if(v != null)
						{
							/**
							 * case 11: local is deleted. remote is not modified.
							 * see also: sync page flow chart.
							 */
							deleteRemotePage(localItem, null);
							if ((!v.equalsIgnoreCase(MetaData.SYNC_VERSION_3_1)) && (!v.equalsIgnoreCase(MetaData.SYNC_VERSION_2))) {
								deletePageElement(localItem.getPageId());
							}
						}
						else
						{
							/**
							 * case 12: local is deleted. remote is not exist.
							 * see also: sync page flow chart.
							 */
							mContentResolver
								.delete(MetaData.PageTable.uri, "created_date = ?",
										new String[] { Long.toString(localItem
												.getPageId()) });
						}
	
						updateSyncCount(1, 1, -1, true);
					} 
					//local is not deleted.
					else 
					{//local is modified.
						// upload local page
						//Begin Darwin_Yu@asus.com
						String v = IsPageExistInServer(localItem.getPageId());
						if((v == null) || v.equalsIgnoreCase(MetaData.SYNC_VERSION_3_1) || v.equalsIgnoreCase(MetaData.SYNC_VERSION_2))
						{
							/**
							 * case 13: local is modified. remote is not exist.
							 * see also: sync page flow chart.
							 */
							mFullPageChanged = true;
						}
						
						/**
						 * case 14: if mFullPageChanged is false:
						 *          local is modified. remote is not modified.
						 * see also: sync page flow chart.
						 */
						
						//sha conflict:
						// BEGIN: Shane_Wang@asus.com 2012-12-11
						String sha = getPageSHA(localItem);
						SyncPageItem tmpItem = new SyncPageItem();
						tmpItem.setSha(sha);
						// END: Shane_Wang@asus.com 2012-12-12

						findUploadElement(localItem.getPageId());
						// BEGIN: Shane_Wang@asus.com 2012-12-12
						if(mDoodleLocalChanged || mFullPageChanged) {
							sha = getDoodleSHA(mDoodleItem);
							mDoodleItem.setSha(sha);
						}
						if(mItemLocalChanged || mFullPageChanged) {
							sha = getItemSHA(mItemItem);
							mItemItem.setSha(sha);
						}
						
						if((SyncPageFile.mListAttachmentModify.size() != 0) || mFullPageChanged) {
							for(int i = 0; i < SyncPageFile.mListAttachmentModify.size(); ++i ) {
								sha = getAttachmentSHA(SyncPageFile.mListAttachmentModify.get(i));
								SyncPageFile.mListAttachmentModify.get(i).setSha(sha);
							}
						}
						// END: Shane_Wang@asus.com 2012-12-12
						
						if (uploadLocalPageElement(localItem, tmpItem)) {
							if (uploadLocalPage(localItem, tmpItem,version)) {
								finishUploadPageElement(localItem, null);
							}
						}
						mFullPageChanged = false;
						//End   Darwin_Yu@asus.com
					}
				} else {
					//about attributes lost:
					// Better
					if (!localItem.isDeleted()) {
						localItem.setSha("");
						mFullPageChanged = true;
						findUploadElement(localItem.getPageId());
						if (uploadLocalPageElement(localItem, null)) {
							if (uploadLocalPage(localItem, null,version)) {
								finishUploadPageElement(localItem, null);
							}
						}
						mFullPageChanged = false;
						MetaData.SyncExceptionList.remove(exceptionItem);
						SyncPageFile.mListAttachmentModify.clear();
					}
				}
			}
			
			SyncPageFile.mListAttachmentModify.clear();
			
			if (task.isCancelled()) {
				Log.v(TAG, "Sync task is cancelled");
				return;
			}
		}
		
		//darwin
		for (LocalPageItem localItem : mLocalOwnerModifyPageList) {
			// BEGIN: Better
			boolean isException = false;
			for (SyncPageItem item : MetaData.SyncExceptionList) {
				if (item.getPageId() == localItem.getPageId()) {
					localItem.setSha("");
					mFullPageChanged = true;
					findUploadElement(localItem.getPageId());
					if (uploadLocalPageElement(localItem, null)) {
						if (uploadLocalPage(localItem, null,version)) {
							finishUploadPageElement(localItem, null);
						}
					}
					mFullPageChanged = false;
					MetaData.SyncExceptionList.remove(item);
					SyncPageFile.mListAttachmentModify.clear();
					if (task.isCancelled()) {
						Log.v(TAG, "Sync task is cancelled");
						return;
					}
					isException = true;
					break;
				}
			}
			// END: Better
			
			if (!isException) {
				if (uploadLocalPage(localItem, null,version)) {
					finishUploadPageElement(localItem, null);
				}
				
				SyncPageFile.mListAttachmentModify.clear();
				
				if (task.isCancelled()) {
					Log.v(TAG, "Sync task is cancelled");
					return;
				}
			}
		}
		for(SyncPageItem remoteItem : mRemotePageListBookId)
		{
			boolean bExist = false;
			for (LocalPageItem localItem : mLocalOwnerModifyPageList) {
				if (remoteItem.getPageId() == localItem.getPageId()) {
					bExist = true;
					break;
				}
			}
			if (!bExist) { // local: exist; remote: not exist
				long bookid = 0;
				Cursor cursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null, 
						 "created_date = ?", new String[] { Long.toString(remoteItem.getPageId()) }, null);
		        if (cursor.getCount() != 0) 
		        {
		        	cursor.moveToFirst();
		            if(!cursor.isAfterLast())
		            {
		            	bookid = cursor.getLong(MetaData.PageTable.INDEX_OWNER);
		            	NoteBook noteBookDest = mBookCase.getNoteBook(remoteItem.getLastSyncOwnerBookId());
						if (noteBookDest == null) {
							noteBookDest = getNoteBook(remoteItem);
							mBookCase.addNewBookFromWebPage(noteBookDest);
						}
						
						TreeSet<SimplePageInfo> pages = new TreeSet<SimplePageInfo>(new PageComparator());
						SimplePageInfo pageInfo = new SimplePageInfo(bookid, 
								remoteItem.getPageId(), 0, 0);
						pages.add(pageInfo);
						noteBookDest.movePagesFrom(pages);
		            }
		        }
		        cursor.close();
				ContentValues cv = new ContentValues();
				cv.put(MetaData.PageTable.LASTSYNC_OWNER, remoteItem.getLastSyncOwnerBookId());
				cv.put(MetaData.PageTable.OWNER, remoteItem.getLastSyncOwnerBookId());
				mContentResolver.update(MetaData.PageTable.uri,cv, "created_date = ?",
						new String[] { Long.toString(remoteItem.getPageId()) });
				
	}
		}
		//darwin
		
		if (MetaData.IS_ENABLE_WEBSTORAGE_DATA_MIGRATING) {
			if (MetaData.SyncUpgradedFirstTime) {
				for (LocalPageItem localItem : MetaData.SyncLocalUntreatedPageList) {
					String v = IsPageExistInServer(localItem.getPageId());
					if(v == null) {
						mFullPageChanged = true;
						findUploadElement(localItem.getPageId());
						if (uploadLocalPageElement(localItem, null)) {
							if (uploadLocalPage(localItem, null,version)) {
								finishUploadPageElement(localItem, null);
							}
						}
						mFullPageChanged = false;
					}
				}
			}
		}
		
		SyncPageFile.mListAttachmentModify.clear();
		
		updateSHA();
	}
	public void CompareAndSyncPageList(AsyncTask task, String version) throws Exception {
		MetaData.webStorage.setTimeout(5 * 60 * 1000);
		{
			GetModifyPageListFromWeb();
			GetAllAttachmentList();
			if (task.isCancelled()) {
				Log.v(TAG, "Sync task is cancelled");
				return;
			}
			GetModifyPageListFromDB();
			MetaData.SyncFailedPageCount = mLocalPageList.size();
			MetaData.SyncTotalPage = MetaData.SyncFailedPageCount + 1;
			GetModifyOwnerIdPageListFromDB();
			if (task.isCancelled()) {
				Log.v(TAG, "Sync task is cancelled");
				return;
			}
			
			ComparePageList(task, version);
		}
	}
	
	public void CompareAndSyncPageList(AsyncTask task) throws Exception {
		MetaData.webStorage.setTimeout(5 * 60 * 1000);
		GetPageListFromWeb();
		if (task.isCancelled()) {
			Log.v(TAG, "Sync task is cancelled");
			return;
		}
		GetPageListFromDB();
		if (task.isCancelled()) {
			Log.v(TAG, "Sync task is cancelled");
			return;
		}
		ComparePageList(task);
	}

	private void copyFile(File src, File dest) {
		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdirs();
			}
			String[] children = src.list();
			for (String child : children) {
				copyFile(new File(src, child), new File(dest, child));
			}
		} else {
			try {
				FileChannel srcChannel = new FileInputStream(src).getChannel();
				FileChannel destChannel = new FileOutputStream(dest)
						.getChannel();
				destChannel.transferFrom(srcChannel, 0, srcChannel.size());
				srcChannel.close();
				destChannel.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
	}
}

// BEGIN: Shane_Wang@asus.com 2012-12-11
class DBInsertItem {
	long pageID = 0;
	String sha = "";
	Uri table = null;
	String fileName = "";
	DBInsertItem(Uri table, long pageID, String sha) {
		this.pageID = pageID;
		this.table = table;
		this.sha = sha;
	}
	DBInsertItem(Uri table, long pageID, String sha, String fileName) {
		this.pageID = pageID;
		this.table = table;
		this.sha = sha;
		this.fileName = fileName;
	}
}
// END: Shane_Wang@asus.com 2012-12-11