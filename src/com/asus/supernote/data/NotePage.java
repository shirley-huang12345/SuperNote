package com.asus.supernote.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.EditText;
import com.asus.supernote.BitmapLender;
import com.asus.supernote.R;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.doodle.DoodleItem;
import com.asus.supernote.doodle.drawinfo.AnnotationDrawInfo;
import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.EraseDrawInfo;
import com.asus.supernote.doodle.drawinfo.GraphicDrawInfo;
import com.asus.supernote.doodle.drawinfo.TextImgDrawInfo;
import com.asus.supernote.editable.NoteEditText;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.AttacherTool;
import com.asus.supernote.editable.noteitem.NoteHandWriteBaselineItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteImageItem;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteSendIntentItem;
import com.asus.supernote.editable.noteitem.NoteTimestampItem;
import com.asus.supernote.picker.NoteBookPickerActivity;
import com.asus.supernote.picker.PickerUtility;
import com.asus.supernote.sync.SyncHelper;
import com.asus.supernote.template.TemplateLayout;
import com.asus.supernote.template.TemplateUtility;
import com.asus.supernote.ui.CoverHelper;

public class NotePage {
    public static final String TAG = "NotePage";
    public static final String NOTE_ITEM = "Item";
    public static final long EMPTY_ITME = 0L;
    public static final int MAX_ARRAY_SIZE = 2147483647 ;//81920;

    private Context mContext;
    private long mCreatedTime;
    private long mModifiedTime;
    private long mOwnerBookId;
    private boolean mIsBookmark;
    private int mPageSize;
    private int mPageColor;
    private int mPageStyle;
    
	// BEGIN: Better
    private long mLastSyncModifiedTime;
    private long mLastSyncOwnerBookId;
    private boolean mIsDeleted;
	// END: Better

    private List<String> mValidDataSet;
    private boolean mDoodleItemCorrect;
    
    // BEGIN: Better
    private long mUserId;
    private int mVersion;
    // END: Better
    
    //begin wendy allen++
    private int mTemplate_type = MetaData.Template_type_normal;//template_type
    //end wendy allen++
    
    //BEGIN: RICHARD
    private int mIndexLanguage = 0;//default
    private int mIndexStatus = MetaData.INDEX_FILE_CREATE_NOT;
    //END:RICHARD
    
    // BEGIN: Better
    public NotePage(Context context) {
    	mContext = context;
        mCreatedTime = -1;
        mModifiedTime = -1;
        mOwnerBookId = -1;
        mIsBookmark = false;
        mPageSize = MetaData.PAGE_SIZE_SAME_AS_BOOK;
        mPageColor = MetaData.PAGE_COLOR_SAME_AS_BOOK;
        mPageStyle = MetaData.PAGE_STYLE_SAME_AS_BOOK;

        mLastSyncModifiedTime = 0;
        mLastSyncOwnerBookId = 0;
        mIsDeleted = false;
        
        mUserId = 0;
        mVersion = 3;
        mTemplate_type = MetaData.Template_type_normal;//wendy allen++
    }
    // END: Better
    
    //BEGIN: RICHARD
    public static final String OBJ = String.valueOf((char) 65532);
    //END: RICHARD
    
    public NotePage(Context context, long ownerId) {
        mContext = context;
        Cursor cursor = context.getContentResolver().query(MetaData.BookTable.uri, null, 
        		"created_date = ?", new String[] {Long.toString(ownerId)}, null);
        if (cursor.getCount() > 0) {
        	cursor.moveToFirst();
	        int userIdIndex = cursor.getColumnIndex(MetaData.BookTable.USER_ACCOUNT);
	        if (userIdIndex >= 0) {
	        	mUserId = cursor.getLong(userIdIndex);
	        } else {
	        	mUserId = 0;
	        }
        } else {
        	mUserId = 0;
        }
        cursor.close();
        long createdTime = System.currentTimeMillis();
        if (mUserId > 0) {
        	mCreatedTime = SyncHelper.pageTime2Id(createdTime);
        } else {
        	mCreatedTime = createdTime;
        }
        mModifiedTime = createdTime;
        mOwnerBookId = ownerId;
        mIsBookmark = false;
        mPageSize = MetaData.PAGE_SIZE_SAME_AS_BOOK;
        mPageColor = MetaData.PAGE_COLOR_SAME_AS_BOOK;
        mPageStyle = MetaData.PAGE_STYLE_SAME_AS_BOOK;
        
        // BEGIN: Better
        mLastSyncModifiedTime = 0;
        mLastSyncOwnerBookId = 0;
        mIsDeleted = false;
        mVersion = 3;
        // END: Better
        //begin wendy allen++
       mTemplate_type =  MetaData.Template_type_normal;
        //end wendy allen++
    }

    public NotePage(Context context, Cursor cursor) {
        mContext = context;
        mCreatedTime = cursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE);
        mModifiedTime = cursor.getLong(MetaData.PageTable.INDEX_MODIFIED_DATE);
        mOwnerBookId = cursor.getLong(MetaData.PageTable.INDEX_OWNER);
        mIsBookmark = (cursor.getInt(MetaData.PageTable.INDEX_IS_BOOKMARK) > 0) ? true : false;
        mPageSize = cursor.getInt(MetaData.PageTable.INDEX_PAGE_SIZE);
        mPageColor = cursor.getInt(MetaData.PageTable.INDEX_COLOR);
        mPageStyle = cursor.getInt(MetaData.PageTable.INDEX_STYLE);
        mTemplate_type = cursor.getInt(MetaData.PageTable.INDEX_TEMPALTE);//wendy allen++ for template 0706
        mIndexLanguage = cursor.getInt(MetaData.PageTable.INDEX_INDEX_LANGUAGE);//RICHARD
        mIndexStatus = cursor.getInt(MetaData.PageTable.INDEX_IS_INDEXED);//RICHARD
        // BEGIN: Better
        mLastSyncModifiedTime = cursor.getLong(MetaData.PageTable.INDEX_LASYSYNC_MODIFYTIME);
        long bookId = cursor.getLong(MetaData.PageTable.INDEX_LASTSYNC_OWNER);
        if (bookId <= 0) {
        	bookId = mOwnerBookId;
        }
        mLastSyncOwnerBookId = bookId;
        mIsDeleted = cursor.getInt(MetaData.PageTable.INDEX_IS_DELETED) > 0;
        int userIdIndex = cursor.getColumnIndex(MetaData.PageTable.USER_ACCOUNT);
        if (userIdIndex >= 0) {
        	mUserId = cursor.getLong(userIdIndex);
        } else {
        	mUserId = 0;
        }
        mVersion = cursor.getInt(MetaData.PageTable.INDEX_VERSION);
        cursor.close();
        // END: Better
    }
    
    // BEGIN: Better
    public void loadNewPage(Cursor cursor) {
    	int index = -1;
    	index = cursor.getColumnIndex(MetaData.PageTable.IS_BOOKMARK);
    	if (index >= 0) {
    		mIsBookmark = (cursor.getInt(index) > 0) ? true : false;
    	}
    	index = cursor.getColumnIndex(MetaData.PageTable.PAGE_SIZE);
    	if (index >= 0) {
    		mPageSize = cursor.getInt(index);
    	}
    	index = cursor.getColumnIndex(MetaData.PageTable.COLOR);
    	if (index >= 0) {
    		mPageColor = cursor.getInt(index);
    	}
    	index = cursor.getColumnIndex(MetaData.PageTable.STYLE);
    	if (index >= 0) {
    		mPageStyle = cursor.getInt(index);
    	}
    	index = cursor.getColumnIndex(MetaData.PageTable.TEMPLATE);
    	if (index >= 0) {
    		mTemplate_type = cursor.getInt(index);
    	}
    }
    // END: Better
    
    //begin wendy allen++
    public NoteBook getNoteBook(){
		BookCase bookCase = BookCase.getInstance(mContext);
		NoteBook noteBook = bookCase.getNoteBook(mOwnerBookId);
		return noteBook;
    }
    
    public void setTemplate(int type)
    {
    	mTemplate_type = type;
    }
    public int getTemplate()
    {
    	return mTemplate_type;
    }
    //end wendy allen++
    
    //BEGIN: RICHARD
    public int getIndexLanguage()
    {
    	return mIndexLanguage;
    }
    
    public void setIndexLanguage(int indexLanguage)
    {
    	mIndexLanguage = indexLanguage;
    }
    
    public int getIndexStatus()
    {
    	return mIndexStatus;
    }
    
    public void setIndexStatus(int status)
    {
    	mIndexStatus = status;
    }
    //END: RICHARD
    
    // BEGIN: Better
    public void setUserId(long userId) {
    	mUserId = userId;
    }
    
    public long getUserId() {
    	return mUserId;
    }
    public void setVersion(int version) {
    	mVersion = version;
    }
    public int getVersion() {
    	return mVersion;
    }
    // END: Better

    public void setCreatedTime(long time) {
        mCreatedTime = time;
    }

    public long getCreatedTime() {
        return mCreatedTime;
    }

    public void setModifiedTime(long time) {
        mModifiedTime = time;
    }

    public long getModifiedTime() {
        return mModifiedTime;
    }

    public void setOwnerBookId(long ownerId) {
        mOwnerBookId = ownerId;
    }

    public long getOwnerBookId() {
        return mOwnerBookId;
    }

    public boolean isBookmark() {
        return mIsBookmark;
    }

    public void setBookmark(boolean b) {
        mIsBookmark = b;
        ContentValues cv = new ContentValues();
        cv.put(MetaData.PageTable.IS_BOOKMARK, (mIsBookmark) ? 1 : 0);
        mContext.getContentResolver().update(MetaData.PageTable.uri, cv, "created_date = ?", new String[] { Long.toString(mCreatedTime) });
    }
    
    // BEGIN: Better
    public void setBookmarkNoUpdateDB(boolean b) {
        mIsBookmark = b;
    }
    // END: Better
    
    //add by wendy begin
   public void setBookmarkByUser(boolean b)
   {
       mIsBookmark = b;
       ContentValues cv = new ContentValues();
       cv.put(MetaData.PageTable.IS_BOOKMARK, (mIsBookmark) ? 1 : 0);
       mContext.getContentResolver().update(MetaData.PageTable.uri, cv, "created_date = ?", new String[] { Long.toString(mCreatedTime) });

		// add by wendy begin ,modify book modify time
		ContentValues cvbook = new ContentValues();
		cvbook.put(MetaData.BookTable.MODIFIED_DATE, System.currentTimeMillis());
		mContext.getContentResolver().update(MetaData.BookTable.uri, cvbook,
				"created_date = ?",
				new String[] { Long.toString(mOwnerBookId) });
		// add by wendy end,
		
   }
   //add by wendy end
   
    // BEGIN: Better
    public long getLastSyncModifiedTime() {
    	return mLastSyncModifiedTime;
    }
    
    public void setLastSyncModifiedTime(long time) {
    	mLastSyncModifiedTime = time;
    }
    
    public long getLastSyncOwnerBookId() {
    	return mLastSyncOwnerBookId;
    }
    
    public void setLastSyncOwnerBookId(long id) {
    	mLastSyncOwnerBookId = id;
    }
    
    public boolean isDeleted() {
    	return mIsDeleted;
    }
    
    public void setDeleted(boolean isDeleted) {
    	mIsDeleted = isDeleted;
    }
    // END: Better
    
  //Begin Darwin_Yu@asus.com
    public long setLastestEditPage()
    {
    	long returnValue = 0;
	    Cursor cursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null, "(version = ? AND is_deleted = 0)", new String[] { "3"}, "is_last_edit DESC");
	    if(cursor != null )
		{
	    	if(cursor.getCount() != 0)
	    	{
	    		cursor.moveToFirst();
	    		returnValue = cursor.getLong(MetaData.PageTable.INDEX_IS_LAST_EDIT);
	    	}
	    	cursor.close();
		}
	    return returnValue + 1;
    }
    //End   Darwin_Yu@asus.com
    // BEGIN: Better
    public boolean save(ArrayList<NoteItemArray> items, DoodleItem doodleItem) {//Allen
    	boolean isSavedNoteItem = saveNoteItems(items);//Richard Allen
    	boolean isSavedDoodle = saveDoodleItem(doodleItem);
    	if (isSavedNoteItem || isSavedDoodle) {
        	if (mVersion == 1) {
        		mVersion = 3;
        	}
            ContentValues cv = new ContentValues();
            cv.put(MetaData.PageTable.OWNER, mOwnerBookId);
            cv.put(MetaData.PageTable.CREATED_DATE, mCreatedTime);
            mModifiedTime = System.currentTimeMillis();
            cv.put(MetaData.PageTable.MODIFIED_DATE, mModifiedTime);
            cv.put(MetaData.PageTable.IS_BOOKMARK, (mIsBookmark) ? 1 : 0);
            cv.put(MetaData.PageTable.THUMBNAIL, getFilePath() + "/" + MetaData.THUMBNAIL_PREFIX);
            cv.put(MetaData.PageTable.PAGE_SIZE, mPageSize);
            cv.put(MetaData.PageTable.COLOR, mPageColor);
            cv.put(MetaData.PageTable.STYLE, mPageStyle);
            // BEGIN: Better
            cv.put(MetaData.PageTable.USER_ACCOUNT, mUserId);
            cv.put(MetaData.PageTable.VERSION, mVersion);
            // END: Better
			
			cv.put(MetaData.PageTable.TEMPLATE, mTemplate_type);//wendy allen++ for template 0706
			
            //Begin Darwin_Yu@asus.com
            cv.put(MetaData.PageTable.IS_LAST_EDIT, setLastestEditPage());
            //End   Darwin_Yu@asus.com
            
            //BEGIN: RICHARD
            cv.put(MetaData.PageTable.IS_INDEXED, mIndexStatus);
            //END: RICHARD
            
            mContext.getContentResolver().update(MetaData.PageTable.uri, cv, "created_date = ?", new String[] { Long.toString(mCreatedTime) });
            //Begin Darwin_Yu@asus.com
            ContentValues cv_book = new ContentValues();
            cv_book.put(MetaData.BookTable.MODIFIED_DATE, mModifiedTime);
            mContext.getContentResolver().update(MetaData.BookTable.uri, cv_book, "created_date = ?", new String[] { Long.toString(mOwnerBookId) });

            if (isSavedNoteItem)
            {
        		ContentValues cvItem = new ContentValues();
        		cvItem.put(MetaData.ItemTable.MODIFIED_DATE, mModifiedTime);
        		mContext.getContentResolver().update(MetaData.ItemTable.uri,cvItem, "_id = ?", new String[] { Long.toString(mCreatedTime) });

            }
            if (isSavedDoodle)
            {
            	ContentValues cvDoodle = new ContentValues();
        		cvDoodle.put(MetaData.DoodleTable.MODIFIED_DATE, mModifiedTime);
        		mContext.getContentResolver().update(MetaData.DoodleTable.uri,cvDoodle, "_id = ?", new String[] { Long.toString(mCreatedTime) });
            }
            //End   Darwin_Yu@asus.com
            
    	}
    	return true;
    }
    
    private boolean saveNoteItems(ArrayList<NoteItemArray> allItems) {//RICHARD NoteItem[] ->ArrayList<NoteItem> Allen<--
    	if (allItems == null || allItems.size() == 0) {//RICHARD 
    		Log.i(MetaData.DEBUG_TAG,"NoteItems null");
        	return false;
        }
        try {
        	deleteTimestamps();//add by wendy
			//BEGIN: RICHARD
        	updatePageIndexFlagInDB(MetaData.INDEX_FILE_CREATE_LOCK);    //RICHARD 
        	//Delete old Index File.
            String pagePath = getFilePath();
    		File itemsFile = new File(pagePath, MetaData.NOTE_INDEX_PREFIX);
    		if (itemsFile.exists()) {
    			itemsFile.delete();
				try {
					Intent intent=new Intent();
					intent.setAction(MetaData.ANDROID_INTENT_ACTION_INDEXSERVICE_DELETEINDEXFILE);
					mContext.sendBroadcast(intent);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
			//END: RICHARD
			File file = new File(getFilePath(), MetaData.NOTE_ITEM_PREFIX);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            AsusFormatWriter afw = new AsusFormatWriter(bos);
			for (NoteItemArray items : allItems) {//Allen
				afw.writeByteArray(AsusFormat.SNF_NITEM_BEGIN, null, 0, 0);
				afw.writeShort(AsusFormat.SNF_NITEM_VERSION,MetaData.ITEM_VERSION);
				afw.writeShort(AsusFormat.SNF_NITEM_TEMPLATE_ITEM_TYPE, items.getTemplateItemType());//Allen
				for (NoteItem item : items.getNoteItems()) {
					if (item instanceof NoteImageItem) {
						continue;
					}
					if (item instanceof AsusFormat) {
						AsusFormat af = (AsusFormat) item;
						af.itemSave(afw);
					}
				}
				afw.writeByteArray(AsusFormat.SNF_NITEM_END, null, 0, 0);
			}
            bos.close();
            fos.close();
            updatePageIndexFlagInDB(MetaData.INDEX_FILE_CREATE_RECREATE); //RICHARD     
        }
        catch (IOException e) {
        	Log.i(MetaData.DEBUG_TAG,"NoteItems IOException");
        	updatePageIndexFlagInDB(MetaData.INDEX_FILE_CREATE_NOT);   //RICHARD 
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private boolean saveDoodleItem(DoodleItem doodleItem) {
    	if (doodleItem == null) {
    		Log.i(MetaData.DEBUG_TAG,"doodleItem null");
            return false;
        }

        try {
        	Log.i(MetaData.DEBUG_TAG,"save doodleItem");
        	File file = new File(getFilePath(), MetaData.DOODLE_ITEM_PREFIX);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            AsusFormatWriter afw = new AsusFormatWriter(bos);
            doodleItem.itemSave(afw);
            bos.close();
            fos.close();
        }
        catch (IOException e) {
        	Log.i(MetaData.DEBUG_TAG,"doodle item IOException");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    // END: Better

    //Begin Darwin_Yu@asus.com
    public void modifyAttachmentDB(PageEditor pe)
    {
    	pe.getDeleteItamList();

        if(pe.isAttachmentModified())
        {
	        List<String> list = new ArrayList<String>();
	        list = pe.getAttachmentNameList();
	        if(list.size() != 0)
	        {
	        	for(String fileName : list)
	        	{
	        		long modifiedTime = System.currentTimeMillis();
		        	ContentValues cvAttachment = new ContentValues();
		        	cvAttachment.put(MetaData.AttachmentTable.ID, mCreatedTime);
		        	cvAttachment.put(MetaData.AttachmentTable.MODIFIED_DATE, modifiedTime);
		        	cvAttachment.put(MetaData.AttachmentTable.IS_DELETE,  0 );
		        	cvAttachment.put(MetaData.AttachmentTable.FILE_NAME, mCreatedTime + "." + fileName);
		        	mContext.getContentResolver().insert(MetaData.AttachmentTable.uri, cvAttachment);
	        	}
	        	list.clear();
	        }
	        List<String> removeList = new ArrayList<String>();
	        removeList = pe.getAttachmentRemoveNameList();
	        if(removeList.size() != 0)
	        {
	        	for(String fileName : removeList)
	        	{
	        		mContext.getContentResolver().delete(MetaData.AttachmentTable.uri,"file_name = ?", new String[] { mCreatedTime + "." + fileName });
	        		File file = new File(getFilePath(), fileName);
	                if (file.exists()) {
	                    file.delete();
	                }
	        	}
	        }
	        pe.resetAttachmentModified();
        }
    }
    //End   Darwin_Yu@asus.com

    private ArrayList<NoteItem> getSubHandWriteItems(ArrayList<NoteItem> itemList,int arrayStartPos,int start,int end)
    {
    	if (itemList == null || itemList.size() == 0) 
    	{
    		return null;
    	}
    	
    	ArrayList<NoteItem> res = new ArrayList<NoteItem>(); 
    	for(int i = arrayStartPos; i<itemList.size();i++)
    	{
    		NoteItem item = itemList.get(i);
    		if(item.getStart() > start)
    		{
    			break;
    		}
    		
    		if(item.getStart() < start)
    		{
    			continue;
    		}
    		
    		if(item instanceof NoteHandWriteItem)
    		{
    			res.add(item);
    		}
    	}
    	return res;
    }
    
    // BEGIN: Better
    public void genThumb(PageDataLoader loader, boolean isLoadAsync, boolean isPhoneSize) {
    	genAPThumb(loader,  isLoadAsync,  isPhoneSize);
    	if(MetaData.IS_ENABLE_WIDGET_THUMBNAIL)
    		genWidgetThumb(loader,  isLoadAsync,  isPhoneSize);
    	if(MethodUtils.isEnableAirview(mContext)){
    		genAirViewThumb(loader,  isLoadAsync,  isPhoneSize);//Allen
    	}
    }
    
    public Bitmap genAirViewThumb(PageDataLoader loader, NoteBook book) {
		Resources res = mContext.getResources();
		int width = 0,height = 0;
		
		width = (int)res.getDimension(R.dimen.pageview_airview_width);
		height = (int)res.getDimension(R.dimen.pageview_airview_height);
		
		Bitmap result = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
		result.setDensity(Bitmap.DENSITY_NONE);
		Canvas canvas = new Canvas(result);
		load(loader, false, canvas,
		        book.getGridType() == MetaData.BOOK_GRID_BLANK,
		        book.getBookColor() != MetaData.BOOK_COLOR_WHITE, true);
		File file = new File(getFilePath(), MetaData.AIRVIEW_PREFIX);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (fos != null) {
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			result.compress(Bitmap.CompressFormat.PNG,
					MetaData.THUMBNAIL_QUALITY, bos);
			try {
				bos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
    
    public Bitmap genAirViewThumb(PageDataLoader loader, boolean isLoadAsync, boolean isPhoneSize) {
		Resources res = mContext.getResources();
		int width = 0,height = 0;
		
		width = (int)res.getDimension(R.dimen.pageview_airview_width);
		height = (int)res.getDimension(R.dimen.pageview_airview_height);
		
		Bitmap result = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
		result.setDensity(Bitmap.DENSITY_NONE);
		Canvas canvas = new Canvas(result);
		load(loader, isLoadAsync, canvas, false, false, true);
		File file = new File(getFilePath(), MetaData.AIRVIEW_PREFIX);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (fos != null) {
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			result.compress(Bitmap.CompressFormat.PNG,
					MetaData.THUMBNAIL_QUALITY, bos);
			try {
				bos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
    
    public void genWidgetThumb(PageDataLoader loader, boolean isLoadAsync, boolean isPhoneSize) {
		int color = getPageColor();
		int line = getPageStyle();
		Resources res = mContext.getResources();
		Paint paint = new Paint();
		int targetWidth, targetHeight;

		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setFilterBitmap(true);

		Bitmap result = Bitmap.createBitmap((int)res.getDimension(R.dimen.widget_cover_width),
											(int)res.getDimension(R.dimen.widget_cover_height), 
											Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);

		targetWidth = (int) result.getWidth() ;//(* 0.9);
		targetHeight = (int) result.getHeight();//( * 0.85);
		Bitmap content;
		Canvas contentCanvas;
		content = Bitmap.createBitmap(targetWidth, targetHeight,
				Bitmap.Config.ARGB_8888);
		content.setDensity(Bitmap.DENSITY_NONE);
		contentCanvas = new Canvas(content);
		load(loader, isLoadAsync, contentCanvas, true, false, false);

		canvas.drawBitmap(content, 0, 0, paint);
		content.recycle();
		content = null;
		File file = new File(getFilePath(), MetaData.THUMBNAIL_WIDGET);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (fos != null) {
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			result.compress(Bitmap.CompressFormat.PNG,
					MetaData.THUMBNAIL_QUALITY, bos);
			result.recycle();
			result = null;
			try {
				bos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
	public void genAPThumb(PageDataLoader loader, boolean isLoadAsync, boolean isPhoneSize) {
		Bitmap cover = null;
		int color = getPageColor();
		int line = getPageStyle();
		Resources res = mContext.getResources();
		cover = CoverHelper.getDefaultCoverBitmap(color, line, res);//Allen
		Paint paint = new Paint();
		int targetWidth, targetHeight;

		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setFilterBitmap(true);

		Bitmap result = Bitmap.createBitmap(cover.getWidth(),
				cover.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(cover, 0, 0, paint);
		cover.recycle();
		cover = null;
		targetWidth = (int) (result.getWidth() * 0.9);
		targetHeight = (int) (result.getHeight() * 0.85);
		Bitmap content;
		Canvas contentCanvas;
		content = Bitmap.createBitmap(targetWidth, targetHeight,
				Bitmap.Config.ARGB_8888);
		content.setDensity(Bitmap.DENSITY_NONE);
		contentCanvas = new Canvas(content);
		load(loader, isLoadAsync, contentCanvas, true, false, false);
		float left = res.getDimension(R.dimen.thumb_padding_left);
		float top = res.getDimension(R.dimen.thumb_padding_top);
		canvas.translate(left, top);
		canvas.drawBitmap(content, 0, 0, paint);
		content.recycle();
		content = null;
		File file = new File(getFilePath(), MetaData.THUMBNAIL_PREFIX);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (fos != null) {
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			result.compress(Bitmap.CompressFormat.PNG,
					MetaData.THUMBNAIL_QUALITY, bos);
			result.recycle();
			result = null;
			try {
				bos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    // END: Better
    
    public void deleteInvalidData(List<String> usedFileList) {
        mValidDataSet = new LinkedList<String>();
        File dir = new File(getFilePath());
        String[] filesName = dir.list();
        //BEGIN: RICHARD
        mValidDataSet.add(MetaData.NOTE_INDEX_PREFIX);
        //END: RICHARD
        //BEGIN: darwin
        mValidDataSet.add(MetaData.THUMBNAIL_WIDGET);
        //END: darwin
        //Begin Allen
        mValidDataSet.add(MetaData.AIRVIEW_PREFIX);
        //End Allen
        mValidDataSet.add(MetaData.DOODLE_ITEM_PREFIX);
        // BEGIN: archie_huang@asus.com
        mValidDataSet.add(MetaData.DOODLE_ITEM_PREFIX_LEGEND);
        // END: archie_huang@asus.com
        mValidDataSet.add(MetaData.NOTE_ITEM_PREFIX);
        mValidDataSet.add(MetaData.NOTE_ITEM_PREFIX_DEPRECATED);
        mValidDataSet.add(MetaData.THUMBNAIL_PREFIX);
        // BEGIN: archie_huang@asus.com
        mValidDataSet.add(MetaData.THUMBNAIL_PREFIX_LEGEND);
        // END: archie_huang@asus.com
        mValidDataSet.addAll(usedFileList);
        //emmanual
		if (filesName == null || filesName.length == 0) {
			return;
		}
        AttacherTool tool = new AttacherTool();
        for (String name : filesName) {
            if (mValidDataSet.contains(name) == false) {
                File invalidFile = new File(dir, name);
				tool.deleteElapsedTimeFromMap(dir + "//" + name);
                invalidFile.delete();
                mContext.getContentResolver().delete(MetaData.AttachmentTable.uri, "(_id = ?) and (file_name = ?)", 
                		new String[] {Long.toString(mCreatedTime), mCreatedTime + "." + name});
            }
        }
    }

    //add by wendy
    private void deleteTimestamps()
    {
    	try{
    	mContext.getContentResolver().delete(MetaData.TimestampTable.uri, "owner = ? ", new String[]{Long.toString(mCreatedTime)});
    	}catch(Exception e)
    	{
    		Log.v("wendy","e " + e.toString());
    	}
    }//add by wendy
    
	//BEGIN: RICHARD 
    private void updatePageIndexFlagInDB(int status)
    {
        ContentValues cvUpdate = new ContentValues();
        cvUpdate.put(MetaData.PageTable.IS_INDEXED, status);
        mContext.getContentResolver().update(MetaData.PageTable.uri, cvUpdate, MetaData.PageTable.CREATED_DATE + "=" + mCreatedTime, null);
    }
	//END: RICHARD

    public void loadDoodleItem(PageEditor pageEditor) {
        File doodleFile = new File(getFilePath(), MetaData.DOODLE_ITEM_PREFIX);
        // BEGIN: archie_huang@asus.com
        if (!doodleFile.exists()) {
            doodleFile = new File(getFilePath(), MetaData.DOODLE_ITEM_PREFIX_LEGEND);
        }
        // END: archie_huang@asus.com
        DoodleItem doodleItem = null;
        if ((doodleFile != null) && doodleFile.exists()) {
            try {
                doodleItem = new DoodleItem();
                FileInputStream fis = new FileInputStream(doodleFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                AsusFormatReader afr = new AsusFormatReader(bis, MAX_ARRAY_SIZE);
                doodleItem.itemLoad(afr);
                mDoodleItemCorrect = doodleItem.getParsingResult();
                doodleItem = doodleItem.size() > 0 ? doodleItem : null;
                bis.close();
                fis.close();
            }
            catch (Exception e) {
                mDoodleItemCorrect = false;
                e.printStackTrace();
            }
        }
        else {
            mDoodleItemCorrect = true;
        }
        pageEditor.loadDoodle(doodleItem);

    }
    // begin jason 
    public static DoodleItem loadDoodleItem(String path){
    	if (path==null||path.length()==0) {
			return null;
		}
    	File doodleFile = new File(path, MetaData.DOODLE_ITEM_PREFIX);
        // BEGIN: archie_huang@asus.com
        if (!doodleFile.exists()) {
            doodleFile = new File(path, MetaData.DOODLE_ITEM_PREFIX_LEGEND);
        }
        // END: archie_huang@asus.com
        DoodleItem doodleItem = null;
        if ((doodleFile != null) && doodleFile.exists()) {
            try {
                doodleItem = new DoodleItem();
                FileInputStream fis = new FileInputStream(doodleFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                AsusFormatReader afr = new AsusFormatReader(bis, MAX_ARRAY_SIZE);
                doodleItem.itemLoad(afr);
                doodleItem = doodleItem.size() > 0 ? doodleItem : null;
                bis.close();
                fis.close();
            }
            catch (Exception e) {
            	doodleItem=null;
                e.printStackTrace();
            }
        }
        else {
        }
        return doodleItem;
    }
    // end jason 
    //Begin Allen
    public ArrayList<NoteItemArray> loadNoteItems(Context context){
    	PageDataLoader loader = new PageDataLoader(context);
    	loader.load(this);   	
		return loader.getAllNoteItems();
    }
    //End Allen
    
    public boolean load(PageDataLoader loader, boolean isLoadAsync, PageEditor pe) {
        BitmapLender lender = BitmapLender.getInstance();
        lender.recycle();
        
        // BEGIN: Better
        if (!isLoadAsync) {
	        if (!loader.load(this)) {
	        	return false;
	        }
        }
        
        // BEGIN: Better
        pe.onLoadPage();
        // END: Better
        
        pe.loadNoteEditText(loader.getAllNoteItems());
        pe.loadDoodle(loader.getDoodleItem());
        return true;
        // END: Better
    }
        
    // BEGIN: Better
    private float getFontSize(NoteBook noteBook) {//change int to float
    	 boolean isSmallScreen = false;
         int textSize = MetaData.BOOK_FONT_NORMAL;
         float fontSize = 0;
         if (noteBook != null) {
             if (noteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE) {
                 isSmallScreen = true;
             }
             textSize = noteBook.getFontSize();
         }
         
         //BEGIN: RICHARD
         fontSize = NotePageValue.getFontSize(mContext,textSize,isSmallScreen);
	     //END: RICHARD
         return fontSize;
    }
    
    private int getFirstLineHeight(boolean isSmallScreen) {
    	if (isSmallScreen) {
            return mContext.getResources().getInteger(R.integer.first_line_height_small_screen);
    	} else {
    		return mContext.getResources().getInteger(R.integer.first_line_height);
    	}
    }
    
    private int getImageSpanHeight(boolean isFull, boolean isSmallScreen) {
        int textSize = MetaData.BOOK_FONT_NORMAL;
        float fontSize = 0;
        textSize = getNoteBook().getFontSize();
        
        //BEGIN: RICHARD
        fontSize = NotePageValue.getFontSize(mContext,textSize,isSmallScreen);
        //END: RICHARD
        
        FontMetricsInt fontMetricsInt;
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        fontMetricsInt = paint.getFontMetricsInt();
        if (!isFull) {
            return (int) (fontMetricsInt.descent * PageEditor.FONT_DESCENT_RATIO - fontMetricsInt.ascent);
        } else {
        	// BEGIN: Shane_Wang 2012-11-14
        	EditText editText = new EditText(mContext);
        	float tempFontSize = NotePageValue.getFontSize(mContext, textSize, isSmallScreen);
        	editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, tempFontSize);
            editText.setLineSpacing(0, PickerUtility.getLineSpace(mContext));
            int lineHeight = editText.getLineHeight();
            return (int) (lineHeight + fontMetricsInt.descent * PageEditor.FONT_DESCENT_RATIO);
            // END: Shane_Wang 2012-11-14
        }
    }
    
    public boolean isPhoneSizeMode() {
        return getNoteBook().getPageSize() == MetaData.PAGE_SIZE_PHONE;
    }     
    
    public Editable loadNoteEditTextExportPDF(NoteItem[] noteItems){
    	Editable editable = null;
        if ((noteItems != null) && (noteItems.length > 0)) {
        	String str = noteItems[0].getText();
            editable = new SpannableStringBuilder(str);

            for (int i = 1; i < noteItems.length; i++) {
                if (noteItems[i].getStart() < 0 || noteItems[i].getEnd() < 0
                        || noteItems[i].getStart() > editable.length() /*|| noteItems[i].getEnd() > editable.length()*/) {
                    continue;
                }
                
                // BEGIN: Shane_Wang 2012-11-5
                if(noteItems[i].getEnd() > editable.length()) {
                	noteItems[i].setEnd(editable.length());
                }
                // END: Shane_Wang 2012-11-5
                
                editable.setSpan(noteItems[i], noteItems[i].getStart(), noteItems[i].getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            NoteHandWriteItem[] handwriteitems = editable.getSpans(0, editable.length(), NoteHandWriteItem.class);
	        for (NoteHandWriteItem item : handwriteitems) {
	            if (item instanceof NoteHandWriteBaselineItem) {
	                item.setFontHeight(getImageSpanHeight(false, isPhoneSizeMode()));
	            }
	            else {
	                item.setFontHeight(getImageSpanHeight(true, isPhoneSizeMode()));
	            }
	        }
        }
        return editable;
    }     
    
    private void drawNoteItem(PageDataLoader loader, Canvas canvas, 
    		NoteBook noteBook, boolean isSmallScreen, boolean isHideLine, boolean isGrid, boolean isDrawNoteItemToPdf) {
    	Editable editable = null;

    	float sx = 1.0f, sy = 1.0f;
    	float offsetY = 0.0f;
    	int templateHeight = 0;
    	
    	NoteItem[] noteItems = loader.getNoteItems();    	                       	    	         	     	     	     	
        
        EditText editText = new EditText(mContext);
        editable = loadNoteEditTextExportPDF(noteItems);
        if (editable != null) {
        	editText.setText(editable);
        } else {
        	editText.setText("ABCDEFGHIJKLMNOPQRSTabcdefghijklmnopqrst");
        }        
        
        int firstLineHeight = getFirstLineHeight(isSmallScreen);
        int lineCountLimited = NotePageValue.getLineCountLimited(mContext,noteBook.getFontSize(), isSmallScreen);//RICHARD
        
        editText.setGravity(Gravity.START | Gravity.TOP | Gravity.CENTER_VERTICAL | Gravity.CENTER_VERTICAL);
        editText.setTextColor(Color.BLACK);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSize(noteBook));
        editText.setSingleLine(false);
        editText.setPadding(0, firstLineHeight, 0, 0);
        editText.setLineSpacing(0, PickerUtility.getLineSpace(mContext)); //smilefish
        
        boolean isXLargeScreen = (mContext.getResources().getConfiguration().screenLayout & 
        		Configuration.SCREENLAYOUT_SIZE_MASK) == 
                Configuration.SCREENLAYOUT_SIZE_XLARGE;
        int width = (int)NotePageValue.getBookWidth(mContext,isSmallScreen);//RICHARD
        
        MetaData.Template_EditText_width = width;//wendy allen++ for template 0706
        MetaData.Template_EditText_line_height = editText.getLineHeight();//wendy allen++ for template 0706
    	
        editText.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), 
        					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int height = editText.getLineHeight() * lineCountLimited + firstLineHeight;
        editText.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), 
				MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        editText.layout(0, 0, editText.getMeasuredWidth(), editText.getMeasuredHeight());
        sx = (float)canvas.getWidth() / editText.getMeasuredWidth();
	    sy = (float)canvas.getHeight() / editText.getMeasuredHeight();
        
    	int templateType = noteBook.getTemplate();
    	if (templateType != MetaData.Template_type_normal&&
    			templateType != MetaData.Template_type_blank) {
	    	LayoutInflater inflater = LayoutInflater.from(mContext);
	    	if (inflater != null) {
	    		View view = inflater.inflate(R.layout.editor_aitvity, null);
	    		if (view != null) {	    			
	    			TemplateUtility  templateUtility = new TemplateUtility(mContext,view,templateType,this);
	    			TemplateLayout templateLayout = templateUtility.PrepareTemplateViewStub();
	    			if (templateLayout != null) {
						templateUtility.LoadTemplateContent(loader.getAllNoteItems());
						
						view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
						view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
						templateHeight = templateLayout.getMeasuredHeight();
						sx = (float)canvas.getWidth() / templateLayout.getMeasuredWidth();
						
						// BEGIN: Shane_Wang 2012-10-19
						if(isDrawNoteItemToPdf) {
							sy = (float)canvas.getHeight() / (editText.getMeasuredHeight() + templateHeight);
						}else {
							sy = (float)canvas.getHeight() / (editText.getMeasuredHeight());// + templateHeight//darwin
						}
						canvas.save();
						canvas.scale(sx, sy);
						templateUtility.setEditTextEnable(false);
						templateLayout.drawToSurfaceView(canvas);
						canvas.restore();
						offsetY = templateHeight * sy;
					}
	    		}
	    	}
    	}
        
        editText.setScaleX(sx);
        editText.setScaleY(sy);
        canvas.save();
        canvas.translate(0, offsetY);
        if (!isHideLine) {
        	if (!isGrid) {
        		Rect lineBound = new Rect();
        		int baseLine = editText.getLineBounds(0, lineBound);
        		Paint linePaint = new Paint();
        		
        		//Begin:clare
               int colorValue=mContext.getResources().getColor(R.color.PAGELINE_COLOR);
        		linePaint.setColor( colorValue);
     		   //End :clare
        		linePaint.setStrokeWidth(NoteEditText.NOTE_LINE_WIDTH);
                for (int i = 0; i < lineCountLimited; i++) {
                    int drawBaseLine = (int) Math.rint(baseLine * editText.getScaleY());
                    canvas.drawLine(lineBound.left - editText.getPaddingLeft(), 
                    		drawBaseLine + NoteEditText.NOTE_LINE_WIDTH, 
                    		lineBound.right, 
                    		drawBaseLine + NoteEditText.NOTE_LINE_WIDTH, 
                    		linePaint);
                    baseLine += (editText.getLineHeight());
                }
            } else {
            	Paint gridPaint = new Paint();
            	gridPaint.setColor(NoteEditText.NOTE_GRID_COLOR);
            	gridPaint.setStrokeWidth(NoteEditText.NOTE_GRID_LINE_WIDTH);
                for (int i = 0; i < editText.getHeight(); i += NoteEditText.NOTE_GRID_SPACING) {
                    int drawLine = (int) Math.rint(i * editText.getScaleY());
                    canvas.drawLine(0, drawLine, editText.getWidth(), drawLine, gridPaint);
                }
                for (int i = 0; i < editText.getWidth(); i += NoteEditText.NOTE_GRID_SPACING) {
                    int drawLine = (int) Math.rint(i * editText.getScaleX());
                    canvas.drawLine(drawLine, 0, drawLine, editText.getHeight(), gridPaint);
                }
            }
        }
        canvas.restore();
        canvas.save();
        //Begin Allen
        int doodleOffsetY = 0;
        if(templateType != MetaData.Template_type_todo){
        	doodleOffsetY = templateHeight;
        }
        //End Allen
        loadDoodleItem(loader, canvas, isSmallScreen,doodleOffsetY);
        canvas.restore();
        if (editable != null) {
	        canvas.save();
	        canvas.translate(0, offsetY);
	        canvas.scale(sx, sy);
	        // BEGIN： Shane_Wang@asus.com 2012-11-29
	        editText.setBackgroundColor(Color.TRANSPARENT);
	        // END： Shane_Wang@asus.com 2012-11-29
	        editText.draw(canvas);
	        canvas.restore();
        }
    }
    
    private void loadDoodleItem(PageDataLoader loader, Canvas canvas, boolean isSmallScreen,int offsetY) {
		File doodleFile = new File(getFilePath(), MetaData.DOODLE_ITEM_PREFIX);
		DoodleItem doodleItem = null;
		if ((doodleFile != null) && doodleFile.exists()) {
			try {
				doodleItem = loader.getDoodleItem();
				if (doodleItem != null) {
					LinkedList<DrawInfo> drawList = doodleItem.load(getFilePath());
					float orgWidth = doodleItem.getCanvasWidth();
					float orgHeight = doodleItem.getCanvasHeight() - offsetY;
					float canvasWidth = canvas.getWidth();
					float canvasHeight = canvas.getHeight();
					float sx = 1.0f;
					if (orgWidth > 0) { 
						sx = canvasWidth / orgWidth;
					}
					float sy = 1.0f;
					if (orgHeight > 0) {
						sy = canvasHeight / orgHeight;
					}
					int size = drawList.size();
					Bitmap drawBmp = Bitmap.createBitmap(canvas.getWidth(), 
							canvas.getHeight(), Config.ARGB_8888);
					drawBmp.eraseColor(Color.TRANSPARENT);
					Canvas drawCanvas = new Canvas(drawBmp);
					Bitmap graphicBmp = Bitmap.createBitmap(canvas.getWidth(), 
							canvas.getHeight(), Config.ARGB_8888);
					graphicBmp.eraseColor(Color.TRANSPARENT);
					Canvas graphicCanvas = new Canvas(graphicBmp);
					Matrix matrix = new Matrix();
					matrix.postScale(sx, sy);
					for (int i = size - 1; i >= 0; i--) {
						DrawInfo d = drawList.get(i);
						if (d != null) {
							d.transform(matrix);
							if(d instanceof AnnotationDrawInfo)
							{
								for(DrawInfo childd : ((AnnotationDrawInfo)d).getDrawInfos())
								{
									if (childd instanceof GraphicDrawInfo) {
										childd.getDrawTool().draw(graphicCanvas, false, childd);//Richard null -> false
									} else {
										childd.getDrawTool().draw(drawCanvas, true, childd);//RICHARD 2013/1/15
									}
								}
							} else if (d instanceof GraphicDrawInfo) {
								d.getDrawTool().draw(graphicCanvas, false, d);//Richard null -> false
							} else {
								d.getDrawTool().draw(drawCanvas, true, d);//RICHARD 2013/1/15
							}
						}
					}
					Paint paint = new Paint();
					paint.setDither(true);
					paint.setAntiAlias(true);
					paint.setFilterBitmap(true);
					canvas.drawBitmap(graphicBmp, 0, 0, paint);
					canvas.drawBitmap(drawBmp, 0, 0, paint);
					if (!graphicBmp.isRecycled()) {
						graphicBmp.recycle();
						graphicBmp = null;
	                }
					if (!drawBmp.isRecycled()) {
						drawBmp.recycle();
						drawBmp = null;
	                }
					mDoodleItemCorrect = true;
				} else {
					mDoodleItemCorrect = false;
				}
			} catch (Exception e) {
				mDoodleItemCorrect = false;
				e.printStackTrace();
			}
		} else {
			mDoodleItemCorrect = true;
		}
    }

    public void load(PageDataLoader loader, boolean isLoadAsync, Canvas canvas, boolean isHideLine, boolean isDrawBackground, boolean isDrawNoteItemToPdf) {
    	BitmapLender lender = BitmapLender.getInstance();
        lender.recycle();
        
        if (!isLoadAsync) {
        	loader.load(this);
        }

    	boolean isSmallScreen = false;
		BookCase bookCase = BookCase.getInstance(mContext);
		NoteBook noteBook = bookCase.getNoteBook(mOwnerBookId);
		if (noteBook != null) {
			if (noteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE) {
	            isSmallScreen = true;
	        }
		}
        
        if (isDrawBackground) {
        	int backColor = MetaData.BOOK_COLOR_WHITE;
        	if (noteBook != null) {
        		backColor = noteBook.getBookColor();
        	}
			canvas.drawColor(backColor);
        }

    	drawNoteItem(loader, canvas, 
    			noteBook, isSmallScreen, isHideLine, 
    			noteBook.getGridType() == MetaData.BOOK_GRID_GRID, isDrawNoteItemToPdf);
    }
    // END: Better
    
    // BEGIN: Better
    private int processDrawList(LinkedList<DrawInfo> drawList, 
    		int pageHeight, 
    		LinkedList<LinkedList<DrawInfo>> pageOrderedDrawList, 
    		LinkedList<LinkedList<DrawInfo>> pageOrderedGraphicDrawList) {
    	int pageNum = 0;
    	if ((drawList != null) && (drawList.size() > 0)) {
    		for (DrawInfo drawInfo : drawList) {
    			if (drawInfo != null) {
    				if (drawInfo instanceof EraseDrawInfo) {
    					continue;
    				}
			    	RectF rc = drawInfo.getBounds();
			        if (rc != null && ((rc.top >= 0) || (rc.bottom >= 0))) { // BEGIN: Shane_Wang 2012-11-14
			        	int minPageNo = (int)rc.top / pageHeight;
			        	if (minPageNo < 0) {
			        		minPageNo = 0;
			        	}
			        	int maxPageNo = (int)rc.bottom / pageHeight;
			        	
			        	if (!(drawInfo instanceof GraphicDrawInfo)) {
				        	for (int i = minPageNo; i <= maxPageNo; i++) {
				        		LinkedList<DrawInfo> subList = null;
				        		if (i < pageOrderedDrawList.size()) {
				        			subList = pageOrderedDrawList.get(i);
				        		} else {
				        			for (int j = pageOrderedDrawList.size(); j <= i; j++) {
				        				pageOrderedDrawList.add(null);
				        			}
				        		}
				        		if (subList == null) {
				        			subList = new LinkedList<DrawInfo>();
				        			pageOrderedDrawList.set(i, subList);
				        		}
				        		subList.add(drawInfo);
				        	}
			        	} else {
			        		for (int i = minPageNo; i <= maxPageNo; i++) {
				        		LinkedList<DrawInfo> subList = null;
				        		if (i < pageOrderedGraphicDrawList.size()) {
				        			subList = pageOrderedGraphicDrawList.get(i);
				        		} else {
				        			for (int j = pageOrderedGraphicDrawList.size(); j <= i; j++) {
				        				pageOrderedGraphicDrawList.add(null);
				        			}
				        		}
				        		if (subList == null) {
				        			subList = new LinkedList<DrawInfo>();
				        			pageOrderedGraphicDrawList.set(i, subList);
				        		}
				        		subList.add(drawInfo);
				        	}
			        	}
			        	if (maxPageNo + 1 > pageNum) {
			        		pageNum = maxPageNo + 1;
			        	}
			        }
    			}
    		}
    	}
        return pageNum;
    }
    
    private void doDrawPage(Canvas c, RectF bounds, List<DrawInfo> list,boolean isUsetempBitmap, float posX, float posY, float offX, float offY)
    {
		if (list != null) {
			int size = list.size();
			for (int i = size - 1; i >= 0; i--) {
				DrawInfo d = list.get(i);
				if (d != null) {
					RectF rc = d.getBounds();
					if (rc != null) {
						if (rc.intersect(bounds)) {
							//BEGIN: RICHARD
							if(d instanceof AnnotationDrawInfo)
							{
								for(DrawInfo childd : ((AnnotationDrawInfo)d).getDrawInfos())
								{
									Log.d("pdftest", "18:" + Float.toString(childd.getScale()));
									Log.d("pdftest", "19:" + Float.toString(childd.getPaint().getStrokeWidth()));
									Log.d("pdftest", "20:" + Float.toString(childd.getPaint().getStrokeMiter()));
									RectF rcChild = childd.getBounds();
									if (rcChild.intersect(bounds)) {
										childd.getDrawTool().draw(c, isUsetempBitmap, childd, posX, posY, offX, offY);
									}
								}
							}									
							else
							{
								d.getDrawTool().draw(c, isUsetempBitmap, d, posX, posY, offX, offY);								
							}
							//END: RICHARD
						}
					}
				}
			}
		}
    }
    
    private void drawPage(Canvas canvas, float width, float pageHeight, int page, 
    		LinkedList<LinkedList<DrawInfo>> pageOrderedDrawList, 
    		LinkedList<LinkedList<DrawInfo>> pageOrderedGraphicDrawList) {  
    	canvas.save();
    	canvas.translate(0, -page * pageHeight);
    	
        RectF bounds = new RectF();
        bounds.left = 0;
        bounds.right = width;
        bounds.top = page * pageHeight;
        bounds.bottom = pageHeight + page * pageHeight;
        
        Log.d("pdftest", "12:" + Float.toString(bounds.left));
        Log.d("pdftest", "13:" + Float.toString(bounds.right));
        Log.d("pdftest", "14:" + Float.toString(bounds.top));
        Log.d("pdftest", "15:" + Float.toString(bounds.bottom));
        Log.d("pdftest", "16:" + Float.toString(canvas.getHeight()));
        Log.d("pdftest", "17:" + Float.toString(canvas.getWidth()));
        if (page >= 0) {
			if (page < pageOrderedGraphicDrawList.size()) {
				LinkedList<DrawInfo> list = pageOrderedGraphicDrawList.get(page);
				doDrawPage(canvas, bounds, list, false, 0, page * pageHeight, 0, -page * pageHeight);//richard 2013/1/16 null -> false
			}

			if (page < pageOrderedDrawList.size()) {
				LinkedList<DrawInfo> list = pageOrderedDrawList.get(page);
				doDrawPage(canvas, bounds, list, true, 0, page * pageHeight, 0, -page * pageHeight);//richard 2013/1/15
			}

		}
         
		canvas.restore();
    }
    
    public ArrayList<String> getThumbnail(PageDataLoader loader, boolean isLoadAsync, 
    		Bitmap bitmap, 
    		boolean isHideLine, boolean isDrawBackground, boolean isFull, String savedFilePath) {
    	if (!isLoadAsync) {
        	loader.load(this);
        }
    	
    	boolean isSmallScreen = false;
    	boolean isGrid = false;
		BookCase bookCase = BookCase.getInstance(mContext);
		NoteBook noteBook = bookCase.getNoteBook(mOwnerBookId);
		int backColor = MetaData.BOOK_COLOR_WHITE;
		
		if (noteBook != null) {
			if (noteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE) {
	            isSmallScreen = true;
	        }
			if (noteBook.getGridType() == MetaData.BOOK_GRID_GRID) {
				isGrid = true;
			}
		}
        
        if (isDrawBackground) {
        	if (noteBook != null) {
        		backColor = noteBook.getBookColor();
        	}
        }
        
        Editable editable = null;

    	float sx = 1.0f, sy = 1.0f;
    	float offsetY = 0.0f;
    	int templateHeight = 0;
    	
    	NoteItem[] noteItems = loader.getNoteItems();
        
        EditText editText = new EditText(mContext);
        editable = loadNoteEditTextExportPDF(noteItems);
        if (editable != null) {
        	editText.setText(editable);
        } else {
        	editText.setText("");
        }        
        
        int firstLineHeight = getFirstLineHeight(isSmallScreen);
        int lineCountLimited = NotePageValue.getLineCountLimited(mContext, noteBook.getFontSize(), isSmallScreen);
        
        editText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL | Gravity.CENTER_VERTICAL); //smilefish fix bug 287168
        editText.setTextColor(Color.BLACK);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSize(noteBook));
        editText.setSingleLine(false);
        editText.setPadding(0, firstLineHeight, 0, 0);
        editText.setLineSpacing(0, PickerUtility.getLineSpace(mContext)); //smilefish
        
        boolean isXLargeScreen = (mContext.getResources().getConfiguration().screenLayout & 
        		Configuration.SCREENLAYOUT_SIZE_MASK) == 
                Configuration.SCREENLAYOUT_SIZE_XLARGE;
        int width = (int)NotePageValue.getBookWidth(mContext,isSmallScreen);
        
        Log.d("pdftest", "1:" + Float.toString(width));
        
        MetaData.Template_EditText_width = width;//wendy allen++ for template 0706
        MetaData.Template_EditText_line_height = editText.getLineHeight();//wendy allen++ for template 0706
    	
        editText.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), 
        					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int height = editText.getLineHeight() * lineCountLimited + firstLineHeight;
        
        Log.d("pdftest", "2:" + Float.toString(height));
        
        editText.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), 
				MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        editText.layout(0, 0, editText.getMeasuredWidth(), editText.getMeasuredHeight());
        
    	int templateType = noteBook.getTemplate();
    	TemplateLayout templateLayout = null;
    	if (templateType != MetaData.Template_type_normal && 
    			templateType != MetaData.Template_type_blank) {
	    	LayoutInflater inflater = LayoutInflater.from(mContext);
	    	if (inflater != null) {
	    		View view = inflater.inflate(R.layout.editor_aitvity, null);
	    		if (view != null) {	    			
	    			TemplateUtility  templateUtility = new TemplateUtility(mContext,view,templateType,this);
	    			templateLayout = templateUtility.PrepareTemplateViewStub();
	    			if (templateLayout != null) {
						templateUtility.LoadTemplateContent(loader.getAllNoteItems());
						
						if(templateType == MetaData.Template_type_meeting) //smilefish
							templateUtility.setMeetingFontSize();
						
						view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
						view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
						templateHeight = templateLayout.getMeasuredHeight();
					}
	    		}
	    	}
    	}
    	
    	float contentHeight = editText.getLineHeight() * editText.getLineCount() + firstLineHeight + templateHeight;
    	int pageHeight = height;
    	
    	Log.d("pdftest", "3:" + Float.toString(pageHeight));
    	
    	if ((!MetaData.IS_ENABLE_CONTINUES_MODE) && (mTemplate_type != MetaData.Template_type_todo)) {
    		pageHeight = height + templateHeight;
    		if (templateHeight > 0) {
    			if ((bitmap != null) && (!bitmap.isRecycled())) {
    				bitmap.recycle();
    				bitmap = null;
    			}
				int newwidth = isSmallScreen ? (int)mContext.getResources().getDimension(R.dimen.phone_page_share_bitmap_default_width)
						: (int)mContext.getResources().getDimension(R.dimen.pad_page_share_bitmap_default_width);
				int newheight = isSmallScreen ? (int)mContext.getResources().getDimension(R.dimen.phone_page_share_bitmap_default_height)
						: (int)mContext.getResources().getDimension(R.dimen.pad_page_share_bitmap_default_height);
				
				newheight = (int) (newheight * pageHeight / (float) height);
				bitmap = Bitmap.createBitmap(newwidth, newheight,
						Bitmap.Config.ARGB_8888);
				
				Log.d("pdftest", "4:" + Float.toString(newwidth));
				Log.d("pdftest", "5:" + Float.toString(newheight));
    		}
    	}
    	
    	Canvas canvas = new Canvas(bitmap);
        
        sx = (float)canvas.getWidth() / editText.getMeasuredWidth();
        if ((!MetaData.IS_ENABLE_CONTINUES_MODE) && (mTemplate_type != MetaData.Template_type_todo)) {
        	sy = (float)canvas.getHeight() / (editText.getMeasuredHeight() + templateHeight);
        } else {
        	sy = (float)canvas.getHeight() / editText.getMeasuredHeight();
        }
	    offsetY = templateHeight * sy;
	    
	    Log.d("pdftest", "6:" + Float.toString(sx));
	    Log.d("pdftest", "7:" + Float.toString(sy));
	    
        int pageNum = (int) Math.ceil(contentHeight / pageHeight);
        DoodleItem doodleItem = loader.getDoodleItem();
        LinkedList<DrawInfo> drawList = null;
        LinkedList<LinkedList<DrawInfo>> pageOrderedDrawList = null;
        LinkedList<LinkedList<DrawInfo>> pageOrderedGraphicDrawList = null;
        if (doodleItem != null) {
	    	drawList = doodleItem.load(getFilePath());
	    	if (drawList != null) {
		        pageOrderedDrawList = new LinkedList<LinkedList<DrawInfo>>();
		        pageOrderedGraphicDrawList = new LinkedList<LinkedList<DrawInfo>>();
				float dsx = 1.0f;
				int orgWidth = doodleItem.getCanvasWidth();
				if (orgWidth > 0) {
					dsx = (float) editText.getMeasuredWidth() / orgWidth;
				}
				float dsy = 1.0f;
				int orgHeight = doodleItem.getCanvasHeight();
				if (orgHeight > 0) {
					dsy = (float) ((mTemplate_type == MetaData.Template_type_todo) 
							? pageHeight : (height + templateHeight)) / orgHeight;
				}
				
				Log.d("pdftest", "8:" + Float.toString(dsx));
				Log.d("pdftest", "9:" + Float.toString(orgWidth));
				Log.d("pdftest", "10:" + Float.toString(dsy));
				Log.d("pdftest", "11:" + Float.toString(orgHeight));
				
				Matrix matrix = new Matrix();
				matrix.postScale(dsx, dsy);
                for (DrawInfo drawInfo : drawList) {
                    drawInfo.transform(matrix);
                }
		        int num = processDrawList(drawList, pageHeight, pageOrderedDrawList, pageOrderedGraphicDrawList);
		        if (num > pageNum) {
		        	pageNum = num;
		        }
	    	}
        }
        
        editText.setScaleX(sx);
        editText.setScaleY(sy);
        
        if (!isFull) {
        	pageNum = 1;
        }
        
        if (!MetaData.IS_ENABLE_CONTINUES_MODE) {
        	pageNum = 1;
        }
        
        ArrayList<String> fileList = new ArrayList<String>();
        if (isFull) {
        	File dir = new File(MetaData.THUMBNAIL_TEMP_DIR);
        	if (!dir.exists()) {
        		dir.mkdirs();
        	}
        }
        
        Rect lineBound = new Rect();
        float line = editText.getLineBounds(0, lineBound);
        Log.d("pdftest", "12:" + Float.toString(line));
//        editText.setPadding(0, 0, 0, 0);
        
        int templatePageNum = (int) Math.ceil((float) templateHeight / pageHeight);
        
        Log.d("pdftest", "13:" + Float.toString(templatePageNum));
        
        if (mTemplate_type == MetaData.Template_type_todo) {
        	isHideLine = true;
        }
        
        for (int pageNo = 0; pageNo < pageNum; pageNo++) {
        	if (isDrawBackground) {
        		canvas.drawColor(backColor);
        	} else {
        		bitmap.eraseColor(Color.TRANSPARENT);
        	}
	        canvas.save();
	        if (pageNo > 0) {
	        	canvas.translate(0, - (((pageNo - 1) * pageHeight + (pageHeight - templateHeight)) * editText.getScaleY()));
	        } else {
	        	canvas.translate(0, offsetY);
	        }
	        canvas.scale(sx, sy);
	        
	        Log.d("pdftest", "14:" + Float.toString(canvas.getHeight()));
	        Log.d("pdftest", "15:" + Float.toString(canvas.getWidth()));
	        
	        //Begin Allen
	        if (templateType == MetaData.Template_type_todo || 
	    			templateType == MetaData.Template_type_blank) {
	        	isHideLine = true;
	        }
	        //End Allen
	        if (!isHideLine) {
	        	if (!isGrid) {
	        		float baseLine = editText.getLineHeight(); //smilefish
	        		Paint linePaint = new Paint();
	        		linePaint.setColor(Color.LTGRAY);
	        		linePaint.setStrokeWidth(NoteEditText.NOTE_LINE_WIDTH);
	                while (baseLine < pageNum * pageHeight) {
	                    canvas.drawLine(lineBound.left - editText.getPaddingLeft(), 
	                    		baseLine + NoteEditText.NOTE_LINE_WIDTH, 
	                    		lineBound.right, 
	                    		baseLine + NoteEditText.NOTE_LINE_WIDTH, 
	                    		linePaint);
	                    baseLine += editText.getLineHeight();
	                }
	            } else {
	            	Paint gridPaint = new Paint();
	            	gridPaint.setColor(NoteEditText.NOTE_GRID_COLOR);
	            	gridPaint.setStrokeWidth(NoteEditText.NOTE_GRID_LINE_WIDTH);
	                for (int i = 0; i < pageNum * pageHeight; i += NoteEditText.NOTE_GRID_SPACING) {
	                    canvas.drawLine(0, i, editText.getWidth(), i, gridPaint);
	                }
	                for (int i = 0; i < editText.getWidth(); i += NoteEditText.NOTE_GRID_SPACING) {
	                    canvas.drawLine(i, 0, i, pageNum * pageHeight, gridPaint);
	                }
	            }
	        }
	        canvas.restore();
	        if (drawList != null) {
		        canvas.save();
				canvas.scale(sx, sy);
				
				Log.d("pdftest", "16:" + Float.toString(canvas.getHeight()));
				Log.d("pdftest", "17:" + Float.toString(canvas.getWidth()));
				
				drawPage(canvas, editText.getMeasuredHeight(), pageHeight, pageNo, pageOrderedDrawList, pageOrderedGraphicDrawList);
		        canvas.restore();
	        }
	        if (editable != null) {
	        	if (pageNo > 0) {
	        		editText.scrollTo(0, (int) ((pageNo - 1) * pageHeight * editText.getScaleY() + (pageHeight - templateHeight) * editText.getScaleY()));
	        	}
		        canvas.save();
		        offsetY += (editText.getLineHeight() - line) * sy; //smilefish
		        canvas.translate(0, (pageNo == 0) ? offsetY : -((pageNo - 1) * pageHeight * editText.getScaleY() + (pageHeight - templateHeight) * editText.getScaleY()));
		        canvas.scale(sx, sy);
		        editText.setBackgroundColor(Color.TRANSPARENT);
		        editText.draw(canvas);
		        canvas.restore();
	        }
	        
	        if ((templateLayout != null) && (pageNo < templatePageNum)) {
		        canvas.save();
				canvas.scale(sx, sy);
				
				Log.d("pdftest", "18:" + Float.toString(canvas.getHeight()));
				Log.d("pdftest", "19:" + Float.toString(canvas.getWidth()));
				
				canvas.translate(0, -pageNo * pageHeight);
				templateLayout.drawToSurfaceView(canvas);
				canvas.restore();
	        }

        	File jpgFile = null;
	        String jpgFilename = isFull ? System.currentTimeMillis() + ".jpg" : savedFilePath;
	        if (isFull) {
	        	jpgFile = new File(MetaData.THUMBNAIL_TEMP_DIR, jpgFilename);
	        } else {
	        	jpgFile = new File(savedFilePath);
	        }

			try {
				if (jpgFile.exists()) {
					jpgFile.delete();
				}
				jpgFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(
						jpgFile);
				bitmap.compress(Bitmap.CompressFormat.JPEG,
						100, fos);
				
				Log.d("pdftest", "20:" + Float.toString(bitmap.getHeight()));
				Log.d("pdftest", "21:" + Float.toString(bitmap.getWidth()));
				
				fos.close();
				fileList.add(jpgFile.getAbsolutePath());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

    	return fileList;
    }

    // END: Better

    public String getFilePath() {
        File dir = new File(MetaData.DATA_DIR);
        dir.mkdir();
        File bookDir = new File(MetaData.DATA_DIR, Long.toString(mOwnerBookId));
        bookDir.mkdir();
        File pageDir = new File(bookDir, Long.toString(mCreatedTime));
        pageDir.mkdir();
        return pageDir.getPath();
    }

    private static final BitmapFactory.Options OPTION = new BitmapFactory.Options();
    static {
        OPTION.inSampleSize = 1;
        OPTION.inDither = true;
        OPTION.inTempStorage = new byte[1024 * 1024];
        OPTION.inPurgeable = true;
    }
    
    // BEGIN: Better
	public void updateTimestampInfo(PageDataLoader loader, boolean isLoadAsync) {		
		if (!isLoadAsync) {
			if (!loader.load(this, false)) {
				return;
			}
		}
		
		NoteItem[] items = loader.getNoteItems();
		if ((items != null) && (items.length > 0)) {
			for (NoteItem item : items) {
				if (item instanceof NoteTimestampItem) {
					updateTimeStampDB(((NoteTimestampItem) item).getTimestamp(), mCreatedTime, item.getStart());
				}
			}
		}
	}
	
	private void updateTimeStampDB(long id, long pageid, long position )
	{
		boolean bIsNeedInsert = true;
        ContentValues cvs = new ContentValues();
        Cursor cursor = mContext.getContentResolver().query(MetaData.TimestampTable.uri, null, "(owner = ?) AND (create_date = ?) AND (position = ?)", 
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
		  Cursor cur = mContext.getContentResolver().query(MetaData.PageTable.uri, 
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
    
    public void updateSyncFilesInfo(PageDataLoader loader, boolean isLoadAsync) {
    	if (!isLoadAsync) {
    		if (!loader.load(this)) {
    			return;
    		}
    	}
    	NoteItem[] items = loader.getNoteItems();
		if ((items != null) && (items.length > 0)) {		
			ContentValues cvItem = new ContentValues();
			cvItem.put(MetaData.ItemTable.ID, mCreatedTime);
			cvItem.put(MetaData.ItemTable.IS_DELETE, mIsDeleted);
			cvItem.put(MetaData.ItemTable.MODIFIED_DATE,  mModifiedTime);
			cvItem.put(MetaData.ItemTable.LASTSYNC_MODIFYTIME,  mLastSyncModifiedTime);
			mContext.getContentResolver().insert(MetaData.ItemTable.uri, cvItem);
			
			for (NoteItem item : items) {
				if (item instanceof NoteSendIntentItem) {
					String filename = ((NoteSendIntentItem) item).getFileName();
		        	ContentValues cvAttachment = new ContentValues();
		        	cvAttachment.put(MetaData.AttachmentTable.ID, mCreatedTime);
		        	cvAttachment.put(MetaData.AttachmentTable.MODIFIED_DATE, mModifiedTime);
		        	cvAttachment.put(MetaData.AttachmentTable.IS_DELETE, mIsDeleted);
		        	cvAttachment.put(MetaData.AttachmentTable.FILE_NAME, mCreatedTime + "." + filename);
		        	mContext.getContentResolver().insert(MetaData.AttachmentTable.uri, cvAttachment);
				}
			}
		}
		DoodleItem doodle = loader.getDoodleItem();
		if ((doodle != null) && (doodle.size() > 0)) {
			ContentValues cvDoodle = new ContentValues();
			cvDoodle.put(MetaData.DoodleTable.ID, mCreatedTime);
			cvDoodle.put(MetaData.DoodleTable.IS_DELETE,  mIsDeleted);
			cvDoodle.put(MetaData.DoodleTable.MODIFIED_DATE,  mModifiedTime);
			cvDoodle.put(MetaData.DoodleTable.LASTSYNC_MODIFYTIME,  mLastSyncModifiedTime);
			mContext.getContentResolver().insert(MetaData.DoodleTable.uri, cvDoodle);
			
			LinkedList<DrawInfo> drawList = doodle.load(getFilePath());
			for (DrawInfo info : drawList) {
				if (info instanceof GraphicDrawInfo) {
					String filename = ((GraphicDrawInfo) info).getFileName();
					ContentValues cvAttachment = new ContentValues();
		        	cvAttachment.put(MetaData.AttachmentTable.ID, mCreatedTime);
		        	cvAttachment.put(MetaData.AttachmentTable.MODIFIED_DATE, mModifiedTime);
		        	cvAttachment.put(MetaData.AttachmentTable.IS_DELETE,  0);
		        	cvAttachment.put(MetaData.AttachmentTable.FILE_NAME, mCreatedTime + "." + filename);
		        	mContext.getContentResolver().insert(MetaData.AttachmentTable.uri, cvAttachment);
				} else if (info instanceof TextImgDrawInfo) {
					String filename = ((TextImgDrawInfo) info).getFileName();
					ContentValues cvAttachment = new ContentValues();
		        	cvAttachment.put(MetaData.AttachmentTable.ID, mCreatedTime);
		        	cvAttachment.put(MetaData.AttachmentTable.MODIFIED_DATE, mModifiedTime);
		        	cvAttachment.put(MetaData.AttachmentTable.IS_DELETE, mIsDeleted);
		        	cvAttachment.put(MetaData.AttachmentTable.FILE_NAME, mCreatedTime + "." + filename);
		        	mContext.getContentResolver().insert(MetaData.AttachmentTable.uri, cvAttachment);
				}
			}
		}
    }
    // END: Better

    //begin darwin
    public static Bitmap SyntheticBitmap(Bitmap bitmapA,Bitmap bitmapB,int outWidth,int outHeight)
    {
    	if(bitmapA == null && bitmapB == null)
    	{
    		return null;
    	}
		Bitmap result = null;
        Canvas resultCanvas;
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        float scaleX = 0.0f;
        float scaleY = 0.0f;

        try {
        	result = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);
        	resultCanvas = new Canvas(result);
        	if(bitmapA != null)
        	{
	            scaleX = (float) outWidth / (float) bitmapA.getWidth();
	            scaleY = (float) outHeight / (float) bitmapA.getHeight() ;
	            resultCanvas.save();
	            resultCanvas.scale(scaleX, scaleY);
	            resultCanvas.drawBitmap(bitmapA, 0, 0, paint);
	            resultCanvas.restore();
        	}
        	if(bitmapB != null)
        	{
	            scaleX = (float) outWidth / (float) bitmapB.getWidth();
	            scaleY = (float) outHeight / (float) bitmapB.getHeight() ;
	            resultCanvas.save();
	            resultCanvas.scale(scaleX, scaleY);
	            resultCanvas.drawBitmap(bitmapB, 0, 0, paint);
	            resultCanvas.restore();
        	}
        }
        catch(Exception e)
        {
        	return null;
        }
    	return result;
    }
    
    public static Bitmap getCloudCover(NoteBook book,Context context)
    {
    	if(book != null)
    	{
    		return getNoteBookDefaultCloudCoverBitmap(book.getBookColor(), book.getGridType(), context.getResources());
    	}
    	return null;
    }
    
    public static Bitmap getNoCloudCover(NoteBook book,Context context)
    {
    	if(book != null)
    	{
    		return getNoteBookDefaultCoverBitmap(book.getBookColor(), book.getGridType(), context.getResources());
    	}
    	return null;
    }
    
    public static Bitmap getNoteBookDefaultCoverBitmap(int color, int line,Resources res){
		Bitmap cover = null;
        try{
    		switch (color) {
            case MetaData.BOOK_COLOR_WHITE:
                if (line == MetaData.BOOK_GRID_LINE) {
                    {
                        cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_line_white_local);
                    }
                }
                else if(line == MetaData.BOOK_GRID_GRID){
                    {
                        cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_grid_white_local);
                    }
                }
                else if(line == MetaData.BOOK_GRID_BLANK){
                	cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_blank_white_local);
                }
                break;
            case MetaData.BOOK_COLOR_YELLOW:
                if (line == MetaData.BOOK_GRID_LINE) {
                    {
                        cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_line_yellow_local);
                    }
                }
                else if(line == MetaData.BOOK_GRID_GRID){
                    {
                        cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_grid_yellow_local);
                    }
                }
                else if(line == MetaData.BOOK_GRID_BLANK){
                	cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_blank_yellow_local);
                }
                break;
            default:
                {
                    cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_line_white_local);
                }
                break;
    		}
        }
        catch(OutOfMemoryError err){//Emmanual to avoid OOM in Monkey Test
			if(cover != null){
				cover.recycle();
				cover = null;
			}
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_4444;
			cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_line_white_local, options);
        }
        catch (Exception e) {
			e.printStackTrace();
		}
        return cover;
	}
    
    public static Bitmap getNoteBookDefaultCloudCoverBitmap(int color, int line,Resources res){
		Bitmap cover = null;
        try{
    		switch (color) {
            case MetaData.BOOK_COLOR_WHITE:
                if (line == MetaData.BOOK_GRID_LINE) {
                    {
                        cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_line_white_cloud);
                    }
                }
                else if(line == MetaData.BOOK_GRID_GRID){
                    {
                        cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_grid_white_cloud);
                    }
                }
                else if(line == MetaData.BOOK_GRID_BLANK){
                	cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_blank_white_local);
                }
                break;
            case MetaData.BOOK_COLOR_YELLOW:
                if (line == MetaData.BOOK_GRID_LINE) {
                    {
                        cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_line_yellow_cloud);
                    }
                }
                else if(line == MetaData.BOOK_GRID_GRID){
                    {
                        cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_grid_yellow_cloud);
                    }
                }
                else if(line == MetaData.BOOK_GRID_BLANK){
                	cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_blank_yellow_local);
                }
                break;
            default:
                {
                    cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_line_white_cloud);
                }
                break;
    		}
        }
        catch (Exception e) {
			e.printStackTrace();
		}
        return cover;
	}	
    
    public static Bitmap getDefaultCover(NoteBook book,Context context)
    {
    	if(book != null)
    	{
    		if(book.getUserId() != 0)
    		{
    			return getCloudCover(book,context);
    		}
    		else
    		{
    			return getNoCloudCover(book,context);
    		}
    		
    	}
    	return null;
    }
    public static Bitmap getNoteBookUsingCoverLgnoreCoverIndex(NoteBook book ,Context context,int width,int height)
    {
    	if(book != null)
    	{
    		return SyntheticBitmap(getDefaultCover(book,context),getNoteBookCoverThumbnail(book,context),width,height);
    	}
    	return null;
    }
    
    public static Bitmap getNoteBookUsingCover(NoteBook book ,Context context,int width,int height)
    {
    	if(book != null)
    	{
    		if(book.getCoverIndex() == 0)
    		{
    			return SyntheticBitmap(getDefaultCover(book,context),getNoteBookCoverThumbnail(book,context),width,height);
    		}
    		else
    		{
    			return getNoteBookDefaultCoverThumbnail(book.getCreatedTime());
    		}
    	}
    	return null;
    }
    
    public static Bitmap getNoteBookDefaultCoverThumbnail(Long bookId) {
        StringBuilder path = new StringBuilder();
        path.append(MetaData.DATA_DIR).append(bookId).append("/").append(MetaData.THUMBNAIL_COVER);
        
        File file = new File(path.toString());
        if (!file.exists()) {
            path = new StringBuilder();
            path.append(MetaData.DATA_DIR).append(bookId).append("/").append(MetaData.THUMBNAIL_PREFIX_LEGEND);
        }
        
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(path.toString()));
        }
        catch (FileNotFoundException e) {
            bis = null;
        }
        if (bis == null) {
            return null;
        }
        else {
            Bitmap b = BitmapFactory.decodeStream(bis, null, OPTION);
            if (b == null) {
                return null;
            }
            // BEGIN: archie_huang@asus.com
            b.setDensity(Bitmap.DENSITY_NONE);
            // END: archie_huang@asus.com
            try {
                bis.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return b;
        }
    }
    
    public static Bitmap getNoteBookCoverThumbnail(NoteBook book,Context context)
    {
    	Bitmap bitmap = getNoteBookCoverThumbnail(book.getCreatedTime());
    	if( bitmap == null)
    	{
    		int type = book.getTemplate();
    		if( type == 1)
    		{
    			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_template_meeting);
    		}
    		else if( type == 2)
    		{
    			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_template_diary);
    		}
    		else if( type == 3)
    		{
    			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_template_memo);
    		}
    	}
    	return bitmap;
    }
    
    public static Bitmap getNoteBookCoverThumbnail(Long bookId) {
        StringBuilder path = new StringBuilder();
        path.append(MetaData.DATA_DIR).append(bookId).append("/").append(MetaData.THUMBNAIL_PREFIX);
        
        File file = new File(path.toString());
        if (!file.exists()) {
            path = new StringBuilder();
            path.append(MetaData.DATA_DIR).append(bookId).append("/").append(MetaData.THUMBNAIL_PREFIX_LEGEND);
        }
        
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(path.toString()));
        }
        catch (FileNotFoundException e) {
            bis = null;
        }
        if (bis == null) {
            return null;
        }
        else {
            Bitmap b = BitmapFactory.decodeStream(bis, null, OPTION);
            if (b == null) {
                return null;
            }
            // BEGIN: archie_huang@asus.com
            b.setDensity(Bitmap.DENSITY_NONE);
            // END: archie_huang@asus.com
            try {
                bis.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return b;
        }
    }
    
    private static boolean mTryGenCoverThumb = true;
    public static Bitmap getWidgetThumbnail(Long bookId, Long pageId,Context context) {
        StringBuilder path = new StringBuilder();
        path.append(MetaData.DATA_DIR).append(bookId).append("/").append(pageId).append("/").append(MetaData.THUMBNAIL_WIDGET);
        // BEGIN: archie_huang@asus.com
        File file = new File(path.toString());
        if (!file.exists()) {
            path = new StringBuilder();
            path.append(MetaData.DATA_DIR).append(bookId).append("/").append(pageId).append("/").append(MetaData.THUMBNAIL_PREFIX_LEGEND);
        }
        // END: archie_huang@asus.com
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(path.toString()));
        }
        catch (FileNotFoundException e) {
            bis = null;
        }
        if (bis == null) {
        	if(mTryGenCoverThumb)
        	{
        		mTryGenCoverThumb = false;
        		BookCase bookCase = BookCase.getInstance(context);
				NoteBook noteBook = bookCase.getNoteBook(bookId);
				if(noteBook == null){
					return null;
				}
				NotePage notePageTemp = noteBook.getNotePage(pageId);
        		NoteBookPickerActivity.saveBookWidgetCoverThumb(context,false,noteBook,notePageTemp);
        		return getWidgetThumbnail(bookId,pageId,context	);
        	}
        	else
        	{
        		mTryGenCoverThumb = true;
        		return null;
        	}
            
        }
        else {
        	mTryGenCoverThumb = true;
            Bitmap b = BitmapFactory.decodeStream(bis, null, OPTION);
            if (b == null) {
                return null;
            }
            // BEGIN: archie_huang@asus.com
            b.setDensity(Bitmap.DENSITY_NONE);
            // END: archie_huang@asus.com
            try {
                bis.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return b;
        }
    }
    
    //end   darwin
    
    public static Bitmap getThumbnail(Long bookId, Long pageId) {
        StringBuilder path = new StringBuilder();
        path.append(MetaData.DATA_DIR).append(bookId).append("/").append(pageId).append("/").append(MetaData.THUMBNAIL_PREFIX);
        // BEGIN: archie_huang@asus.com
        File file = new File(path.toString());
        if (!file.exists()) {
            path = new StringBuilder();
            path.append(MetaData.DATA_DIR).append(bookId).append("/").append(pageId).append("/").append(MetaData.THUMBNAIL_PREFIX_LEGEND);
        }
        // END: archie_huang@asus.com
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(path.toString()));
        }
        catch (FileNotFoundException e) {
            bis = null;
        }
        if (bis == null) {
            return null;
        }
        else {
        	Bitmap b = null;
			try {
				b = BitmapFactory.decodeStream(bis, null, OPTION);
			} catch (OutOfMemoryError err) {// Emmanual to avoid OOM in Monkey Test
				if (b != null) {
					b.recycle();
					b = null;
				}
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.ARGB_4444;
				b = BitmapFactory.decodeStream(bis, null, options);
			}
            if (b == null) {
                return null;
            }
            // BEGIN: archie_huang@asus.com
            b.setDensity(Bitmap.DENSITY_NONE);
            // END: archie_huang@asus.com
            try {
                bis.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return b;
        }
    }

    public static Bitmap getAirView(Long bookId, Long pageId) {
        StringBuilder path = new StringBuilder();
        path.append(MetaData.DATA_DIR).append(bookId).append("/").append(pageId).append("/").append(MetaData.AIRVIEW_PREFIX);
        // BEGIN: archie_huang@asus.com
        File file = new File(path.toString());
        if (!file.exists()) {
            path = new StringBuilder();
            path.append(MetaData.DATA_DIR).append(bookId).append("/").append(pageId).append("/").append(MetaData.THUMBNAIL_PREFIX);
        }
        // END: archie_huang@asus.com
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(path.toString()));
        }
        catch (FileNotFoundException e) {
            bis = null;
        }
        if (bis == null) {
            return null;
        }
        else {
            Bitmap b = BitmapFactory.decodeStream(bis, null, OPTION);
            if (b == null) {
                return null;
            }
            // BEGIN: archie_huang@asus.com
            b.setDensity(Bitmap.DENSITY_NONE);
            // END: archie_huang@asus.com
            try {
                bis.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return b;
        }
    }
    
    public void setPageSize(int size) {
        mPageSize = size;
    }

    public void setPageColor(int color) {
        mPageColor = color;
    }

    public void setPageStyle(int style) {
        mPageStyle = style;
    }

    public int getPageSize() {
        return mPageSize;
    }

    public int getPageColor() {
        return mPageColor;
    }

    public int getPageStyle() {
        return mPageStyle;
    }
}
