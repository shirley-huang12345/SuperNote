package com.asus.supernote.data;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.asus.supernote.PageOrderList;
import com.asus.supernote.R;
import com.asus.supernote.ga.GACollector;
import com.asus.supernote.picker.SimplePageInfo;
import com.asus.supernote.sync.SyncBookItem;
import com.asus.supernote.sync.SyncHelper;
import com.asus.supernote.ui.CoverHelper;

public class NoteBook implements AsusFormat {
    public static final String TAG = "NoteBook";
    public static final long START_PAGE = -1;
    public static final long END_PAGE = -2;
    // Data
    private final Context mContext;
    private String mTitle = "";
    private long mCreatedTime;
    private long mModifiedTime;
    private long mCurrPageId;
    private long mImportedTime;
    private int mFontSize;
    private int mPageSize;
    private int mColor;
    private int mGridLine;
    private boolean mIsLocked;
    private boolean mIsPhoneMemo;
    // BEGIN: Better
    private long mLastSyncModifiedTime;
    private boolean mIsDeleted;
    private long mUserId;
    private int mVersion;
    // END: Better
    private List<Long> mPageOrderList;
    private List<Long> mBookmarksList;
    private SortedSet<SimplePageInfo> mSelectedItemsList;
    private final ContentResolver mContentResolver;
    private int mTemplateType = MetaData.Template_type_normal;//wendy allen++
    private int mIndexLanguage = NO_INDEX_LAGNUAGE;//default Allen

    public static int NO_INDEX_LAGNUAGE = -1;
    public static int WAIT_TO_SET_INDEX_LANGUAGE = -2;

    private int mCoverIndex = 0;
    private long mCoverModifyTime = 0;

	public void setIndexLanguage(int mIndexLanguage) {
		this.mIndexLanguage = mIndexLanguage;
	}

    public NoteBook(Context context) {
        mContext = context;
        mCreatedTime = System.currentTimeMillis();
        mModifiedTime = System.currentTimeMillis();
        mPageOrderList = new LinkedList<Long>();
        mBookmarksList = new ArrayList<Long>();
        mIsLocked = false;
        mIsPhoneMemo = false;
        // BEGIN: Better
        mLastSyncModifiedTime = mModifiedTime;
        mIsDeleted = false;
        mUserId = 0;
        mVersion = 3;
        // END: Better
        mContentResolver = mContext.getContentResolver();
        mPageSize = MetaData.PAGE_SIZE_PHONE;
        mColor = MetaData.BOOK_COLOR_WHITE;
        mGridLine = MetaData.BOOK_GRID_LINE;
        mFontSize = MetaData.BOOK_FONT_NORMAL;
        mTemplateType = MetaData.Template_type_normal;//Allen
        mCoverIndex = 0;//darwin
        mCoverModifyTime = 0;//darwin
    }
    
    // BEGIN: Better
    public NoteBook(Context context, long createdTime) {
    	mContext = context;
        mCreatedTime = createdTime;
        mModifiedTime = createdTime;
        mPageOrderList = new LinkedList<Long>();
        mBookmarksList = new ArrayList<Long>();
        mIsLocked = true;//false;
        mIsPhoneMemo = false;
        // BEGIN: Better
        mLastSyncModifiedTime = mModifiedTime;
        mIsDeleted = false;
        mUserId = 0;
        mVersion = 3;
        // END: Better
        mContentResolver = mContext.getContentResolver();
        mPageSize = MetaData.PAGE_SIZE_PHONE;
        mColor = MetaData.BOOK_COLOR_WHITE;
        mGridLine = MetaData.BOOK_GRID_LINE;
        mFontSize = MetaData.BOOK_FONT_NORMAL;
        mTemplateType = MetaData.Template_type_normal;//Allen
        mCoverIndex = 0;//darwin
        mCoverModifyTime = 0;//darwin
    }
    // END: Better
	// BEGIN wendy
	public NoteBook(Context context, SyncBookItem item) {
		mCreatedTime = item.getBookId();
		mModifiedTime = item.getCurModifiedTime();
		mPageOrderList = new LinkedList<Long>(item.getPageOrderList());
		mBookmarksList = new ArrayList<Long>(item.getBookmarksList());
		mIsLocked = item.isLocked();			
		mLastSyncModifiedTime = item.getLastSyncModifiedTime();
		mIsDeleted = item.isDeleted();
		mTitle = item.getTitle();
		
		mContext = context;
		mIsPhoneMemo = false;
		mContentResolver = mContext.getContentResolver();
		mPageSize = item.getPageSize();
		mColor = item.getColor();
		mGridLine = item.getGridLine();
		
		mFontSize = MetaData.BOOK_FONT_NORMAL;
		// BEGIN: Better
		mUserId = 0;
		mVersion = 3;
		// END: Better
		mTemplateType = item.mTemplate_type;//wendy allen++ for template 0706
		
		// BEGIN: Better
		mIndexLanguage = item.getIndexLanguage();
		// END: Better
		mCoverIndex = item.getIndexCover();//darwin
		mCoverModifyTime = item.getCoverModifiedTime();//darwin
	}
	// END wendy
	
	//BEGIN: RICHARD
	public int getIndexLanguage()
	{
		return mIndexLanguage;
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
	//begin wendy allen++ 0705
	public void setTemplate(int temp)
	{
		mTemplateType = temp;
	}
	public int getTemplate()
	{
		return mTemplateType;
	}
	//end wendy allen++ 0705
	
    public void setPhoneMemo(boolean b) {
        mIsPhoneMemo = b;
    }

    public boolean isPhoneMemo() {
        return mIsPhoneMemo;
    }

    public void setTitle(String title) {
        mTitle = new String(title);
        //add by wendy 0401 begin++
        ContentValues cv = new ContentValues();
        cv.put(MetaData.BookTable.TITLE, mTitle);
        cv.put(MetaData.BookTable.MODIFIED_DATE, System.currentTimeMillis());
        mContentResolver.update(MetaData.BookTable.uri, cv, "created_date = ?", new String[] { Long.toString(mCreatedTime) });
        //Log.v("wendy","set title modify curtime");
        //add by wendy 0401 end---
    }
    
    // BEGIN: Better
    public void setTitleNoUpdateDB(String title) {
        mTitle = new String(title);
    }
    // END: Better

    public String getTitle() {
        return mTitle;
    }

    public void setCreatedTime(long time) {
        ContentValues cv = new ContentValues();
        cv.put(MetaData.BookTable.CREATED_DATE, time);
        cv.put(MetaData.BookTable.MODIFIED_DATE, System.currentTimeMillis());//add by wendy 0401
       // Log.v("wendy","setCreatedTime modify curtime");
        mContentResolver.update(MetaData.BookTable.uri, cv, "created_date = ?", new String[] { Long.toString(mCreatedTime) });
        mCreatedTime = time;
    }
    
    // BEGIN: Better
    public void setCreatedTimeNoUpdateDB(long time) {
        mCreatedTime = time;
    }
    // END: Better

    public Long getCreatedTime() {
        return mCreatedTime;
    }

    public void setModifiedTime(long time) {
        mModifiedTime = time;
    }

    public long getModifiedTime() {
        return mModifiedTime;
    }
    //add by wendy
    public void setPageOrderList(List<Long> list)
    {
    	mPageOrderList = new LinkedList<Long>(list);
    }
    //add by wendy
    
    //+++ Dave  GA
    public int getPageNum()
    {
    	if(mPageOrderList != null)
    		return mPageOrderList.size();
    	return 0;
    }
    //---

    public void setIsLocked(boolean isLocked) {
        mIsLocked = isLocked;
        ContentValues cv = new ContentValues();
        cv.put(MetaData.BookTable.IS_LOCKED, (mIsLocked) ? 1 : 0);
        cv.put(MetaData.BookTable.MODIFIED_DATE, System.currentTimeMillis());//add by wendy 0401
       // Log.v("wendy","setCreatedTime modify curtime");
        mContentResolver.update(MetaData.BookTable.uri, cv, "created_date = ?", new String[] { Long.toString(mCreatedTime) });
    }
    
    // BEGIN: Better
    public void setLockedNoUpdateDB(boolean isLocked) {
    	mIsLocked = isLocked;
    }
    // END: Better

    public boolean getIsLocked() {
        return mIsLocked;
    }
    
    // BEGIN: Better
    public long getLastSyncModifiedTime() {
    	return mLastSyncModifiedTime;
    }
    
    public void setLastSyncModifiedTime(long time) {
    	mLastSyncModifiedTime = time;
    }
    
    public boolean isDeleted() {
    	return mIsDeleted;
    }
    
    public void setDeleted(boolean isDeleted) {
    	mIsDeleted = isDeleted;
    }
    // END: Better

    public int getTotalPageNumber() {
        return mPageOrderList.size();
    }

    public void setCurrentPageId(long pageId) {
        mCurrPageId = pageId;
    }

    public long getCurrentPageId() {
        return mCurrPageId;
    }

    //BEGIN: RICHARD
    public List<Long> getPageOrderList()
    {
    	return mPageOrderList;
    }
    //END: RICHARD
    
    public long getPageOrder(int index) {
        if (index < 0 || index >= mPageOrderList.size()) {
            return 0L;
        }
        else {
            return mPageOrderList.get(index);

        }

    }

    public int getPageIndex(Long item) {
        return mPageOrderList.indexOf(item);
    }

    public void setGridType(int type) {
        mGridLine = type;
    }

    public int getGridType() {
        return mGridLine;
    }
	
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
    public void updatePageFromWeb(NotePage notePage)
    {
    	if ((mPageOrderList != null) && (!mPageOrderList.contains(notePage.getCreatedTime()))) {
    		mPageOrderList.add(notePage.getCreatedTime());
    	}
    	ContentResolver cr = mContext.getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(MetaData.PageTable.MODIFIED_DATE, notePage.getModifiedTime());
		cv.put(MetaData.PageTable.LASTSYNC_MODIFYTIME,
				notePage.getLastSyncModifiedTime());
		cv.put(MetaData.PageTable.IS_DELETED, notePage.isDeleted() ? 1 : 0);
		cv.put(MetaData.PageTable.USER_ACCOUNT, notePage.getUserId());
		//Begin Darwin_Yu@asus.com
        cv.put(MetaData.PageTable.IS_LAST_EDIT, setLastestEditPage());
        //End   Darwin_Yu@asus.com
		cr.update(MetaData.PageTable.uri, cv,"created_date = ?",
				new String[] { Long.toString(notePage.getCreatedTime()) });
		updateOrderListDataNoModifyTime();
    }
    //End   Darwin_Yu@asus.com
	// BEGIN: Wendy
	public void updateOrderListDataNoModifyTime() {
		ContentValues cv = new ContentValues();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		for (Long value : mPageOrderList) {
			try {
				dos.writeLong(value);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		cv.put(MetaData.BookTable.PAGE_ORDER, baos.toByteArray());
		
		mContentResolver.update(MetaData.BookTable.uri, cv, "created_date = ?",
				new String[] { Long.toString(mCreatedTime) });
	}
	
	//Begin Allen ++ to update widget 
	@SuppressWarnings("unchecked")
	private void updateWidget(MetaData.SuperNoteUpdateFrom update_from ,long pageId){
		if(update_from == MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_PAGE){
			ArrayList<Long> deletedPageIds = null;
			if(!MetaData.SuperNoteUpdateInfoSet.containsKey(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_PAGE)){
				deletedPageIds = new ArrayList<Long>();
			}
			else{
				try{
					deletedPageIds = (ArrayList<Long>) MetaData.SuperNoteUpdateInfoSet.get(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_PAGE);
				}
				catch(ClassCastException e){
					e.printStackTrace();
					return;
				}
			}
			deletedPageIds.add(pageId);
			MetaData.SuperNoteUpdateInfoSet.put(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_PAGE,deletedPageIds);
		}
		else if(!MetaData.SuperNoteUpdateInfoSet.containsKey(update_from)){
			MetaData.SuperNoteUpdateInfoSet.put(update_from,null);
		}
		//End Allen
	}
	
	public void addPageFromweb(NotePage notePage)
	{
		ContentResolver cr = mContext.getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(MetaData.PageTable.OWNER, notePage.getOwnerBookId());
		cv.put(MetaData.PageTable.CREATED_DATE, notePage.getCreatedTime());
		cv.put(MetaData.PageTable.MODIFIED_DATE, notePage.getModifiedTime());
		cv.put(MetaData.PageTable.IS_BOOKMARK, notePage.isBookmark() ? 1 : 0);
		cv.put(MetaData.PageTable.PAGE_SIZE, mPageSize);
		cv.put(MetaData.PageTable.COLOR, mColor);
		cv.put(MetaData.PageTable.STYLE, mGridLine);
		// BEGIN: Better
		cv.put(MetaData.PageTable.LASTSYNC_MODIFYTIME,
				notePage.getLastSyncModifiedTime());
		cv.put(MetaData.PageTable.LASTSYNC_OWNER,
				notePage.getLastSyncOwnerBookId());
		cv.put(MetaData.PageTable.IS_DELETED, notePage.isDeleted() ? 1 : 0);
		cv.put(MetaData.PageTable.USER_ACCOUNT, notePage.getUserId());
		cv.put(MetaData.PageTable.VERSION, notePage.getVersion());
		// END: Better
		
		cv.put(MetaData.PageTable.TEMPLATE, notePage.getTemplate());//wendy allen++
		
		//Begin Darwin_Yu@asus.com
        cv.put(MetaData.PageTable.IS_LAST_EDIT, setLastestEditPage());
        //End   Darwin_Yu@asus.com
		
        //BEGIN: RICHARD
        cv.put(MetaData.PageTable.INDEX_LANGUAGE,notePage.getIndexLanguage());// getIndexLanguage());
        //END: RICHARD
        
		cr.insert(MetaData.PageTable.uri, cv);
		if (!mPageOrderList.contains(notePage.getCreatedTime())) {
		mPageOrderList.add(notePage.getCreatedTime());
		}
		updateOrderListDataNoModifyTime();
		String path = "/" + mCreatedTime + "/" + notePage.getCreatedTime();
		File file = new File(MetaData.DATA_DIR, path);
		file.mkdirs();
	    //Begin Allen to update widget
		updateWidget(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_ADD_PAGE,notePage.getCreatedTime());
		//End Allen
		}
	// END: Wendy
	
	public void addPage(NotePage notePage) {
		ContentResolver cr = mContext.getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(MetaData.PageTable.OWNER, notePage.getOwnerBookId());
		cv.put(MetaData.PageTable.CREATED_DATE, notePage.getCreatedTime());
		cv.put(MetaData.PageTable.MODIFIED_DATE, notePage.getModifiedTime());
		cv.put(MetaData.PageTable.IS_BOOKMARK, notePage.isBookmark() ? 1 : 0);
		cv.put(MetaData.PageTable.PAGE_SIZE, mPageSize);
		cv.put(MetaData.PageTable.COLOR, mColor);
		cv.put(MetaData.PageTable.STYLE, mGridLine);
		// BEGIN: Better
		cv.put(MetaData.PageTable.USER_ACCOUNT, notePage.getUserId());
		cv.put(MetaData.PageTable.VERSION, notePage.getVersion());
		// END: Better
		
		cv.put(MetaData.PageTable.TEMPLATE, notePage.getTemplate());//wendy allen++ for template 0706
		
		//Begin Darwin_Yu@asus.com
        cv.put(MetaData.PageTable.IS_LAST_EDIT, setLastestEditPage());
        //End   Darwin_Yu@asus.com
        //BEGIN: RICHARD
        cv.put(MetaData.PageTable.INDEX_LANGUAGE, getIndexLanguage());
        cv.put(MetaData.PageTable.IS_INDEXED, notePage.getIndexStatus());
        //END: RICHARD
		cr.insert(MetaData.PageTable.uri, cv);

		//Begin Darwin_Yu@asus.com
		ContentValues cvDoodle = new ContentValues();
		cvDoodle.put(MetaData.DoodleTable.ID, notePage.getCreatedTime());
		cvDoodle.put(MetaData.DoodleTable.IS_DELETE, notePage.isDeleted() ? 1 : 0);
		cr.insert(MetaData.DoodleTable.uri, cvDoodle);
		
		ContentValues cvItem = new ContentValues();
		cvItem.put(MetaData.ItemTable.ID, notePage.getCreatedTime());
		cvItem.put(MetaData.ItemTable.IS_DELETE, notePage.isDeleted() ? 1 : 0);
		cr.insert(MetaData.ItemTable.uri, cvItem);
		//End   Darwin_Yu@asus.com
        mPageOrderList.add(notePage.getCreatedTime());
        updateOrderListData();
        String path = "/" + mCreatedTime + "/" + notePage.getCreatedTime();
        File file = new File(MetaData.DATA_DIR, path);
        file.mkdirs();
        //Begin Allen to update widget
        updateWidget(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_ADD_PAGE,notePage.getCreatedTime());
        //End Allen
    }
    
	// BEGIN: Wendy
	public void addPageFromweb(NotePage notePage, int index) {
		ContentResolver cr = mContext.getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(MetaData.PageTable.OWNER, notePage.getOwnerBookId());
		cv.put(MetaData.PageTable.CREATED_DATE, notePage.getCreatedTime());
		cv.put(MetaData.PageTable.MODIFIED_DATE, notePage.getModifiedTime());
		cv.put(MetaData.PageTable.IS_BOOKMARK, notePage.isBookmark() ? 1 : 0);
		cv.put(MetaData.PageTable.PAGE_SIZE, mPageSize);
		cv.put(MetaData.PageTable.COLOR, mColor);
		cv.put(MetaData.PageTable.STYLE, mGridLine);
		// BEGIN: Better
		cv.put(MetaData.PageTable.LASTSYNC_MODIFYTIME,
				notePage.getLastSyncModifiedTime());
		cv.put(MetaData.PageTable.LASTSYNC_OWNER,
				notePage.getLastSyncOwnerBookId());
		cv.put(MetaData.PageTable.IS_DELETED, notePage.isDeleted() ? 1 : 0);
		cv.put(MetaData.PageTable.USER_ACCOUNT, notePage.getUserId());
		cv.put(MetaData.PageTable.VERSION, notePage.getVersion());
		// END: Better
		
		cv.put(MetaData.PageTable.TEMPLATE, notePage.getTemplate());//wendy allen++ for template 0706
		
		//Begin Darwin_Yu@asus.com
        cv.put(MetaData.PageTable.IS_LAST_EDIT, setLastestEditPage());
        //End   Darwin_Yu@asus.com
        //BEGIN: RICHARD
        cv.put(MetaData.PageTable.INDEX_LANGUAGE, notePage.getIndexLanguage());//getIndexLanguage());
        //END: RICHARD
		cr.insert(MetaData.PageTable.uri, cv);

		if (index >= mPageOrderList.size()) {
			if (!mPageOrderList.contains(notePage.getCreatedTime())) {
			mPageOrderList.add(notePage.getCreatedTime());
			}
		} else {
			if (!mPageOrderList.contains(notePage.getCreatedTime())) {
			mPageOrderList.add(index, notePage.getCreatedTime());
			}
		}
		//updateOrderListData();
		updateOrderListDataNoModifyTime();
		String path = "/" + mCreatedTime + "/" + notePage.getCreatedTime();
		File file = new File(MetaData.DATA_DIR, path);
		file.mkdirs();
		//Begin Allen to update widget
        updateWidget(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_ADD_PAGE,notePage.getCreatedTime());
        //End Allen
	}
    public void addPage(NotePage notePage, int index) {
        ContentResolver cr = mContext.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(MetaData.PageTable.OWNER, notePage.getOwnerBookId());
        cv.put(MetaData.PageTable.CREATED_DATE, notePage.getCreatedTime());
        cv.put(MetaData.PageTable.MODIFIED_DATE, notePage.getModifiedTime());
        cv.put(MetaData.PageTable.IS_BOOKMARK, notePage.isBookmark() ? 1 : 0);
        cv.put(MetaData.PageTable.PAGE_SIZE, mPageSize);
        cv.put(MetaData.PageTable.COLOR, mColor);
        cv.put(MetaData.PageTable.STYLE, mGridLine);
        // BEGIN: Better
        cv.put(MetaData.PageTable.USER_ACCOUNT, notePage.getUserId());
        cv.put(MetaData.PageTable.VERSION, notePage.getVersion());
        // END: Better
        
		cv.put(MetaData.PageTable.TEMPLATE, notePage.getTemplate());//wendy allen++ for template 0706
		
        //Begin Darwin_Yu@asus.com
        cv.put(MetaData.PageTable.IS_LAST_EDIT, setLastestEditPage());
        //End   Darwin_Yu@asus.com
        //BEGIN: RICHARD
        cv.put(MetaData.PageTable.INDEX_LANGUAGE, getIndexLanguage());
        //END: RICHARD
        cr.insert(MetaData.PageTable.uri, cv);
		//Begin Darwin_Yu@asus.com
		ContentValues cvDoodle = new ContentValues();
		cvDoodle.put(MetaData.DoodleTable.ID, notePage.getCreatedTime());
		cvDoodle.put(MetaData.DoodleTable.IS_DELETE, notePage.isDeleted() ? 1 : 0);
		cr.insert(MetaData.DoodleTable.uri, cvDoodle);
		
		ContentValues cvItem = new ContentValues();
		cvItem.put(MetaData.ItemTable.ID, notePage.getCreatedTime());
		cvItem.put(MetaData.ItemTable.IS_DELETE, notePage.isDeleted() ? 1 : 0);
		cr.insert(MetaData.ItemTable.uri, cvItem);
		//End   Darwin_Yu@asus.com
        if (index >= mPageOrderList.size()) {
            mPageOrderList.add(notePage.getCreatedTime());
        }
        else {
            mPageOrderList.add(index, notePage.getCreatedTime());
        }
        updateOrderListData();
        String path = "/" + mCreatedTime + "/" + notePage.getCreatedTime();
        File file = new File(MetaData.DATA_DIR, path);
        file.mkdirs();
        //Begin Allen to update widget
        updateWidget(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_ADD_PAGE,notePage.getCreatedTime());
        //End Allen
    }
	// END: Wendy

    public void addPage(NotePage notePage, NotePage previousPage) {
        ContentResolver cr = mContext.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(MetaData.PageTable.OWNER, notePage.getOwnerBookId());
        cv.put(MetaData.PageTable.CREATED_DATE, notePage.getCreatedTime());
        cv.put(MetaData.PageTable.MODIFIED_DATE, notePage.getModifiedTime());
        cv.put(MetaData.PageTable.IS_BOOKMARK, notePage.isBookmark() ? 1 : 0);
        cv.put(MetaData.PageTable.PAGE_SIZE, mPageSize);
        cv.put(MetaData.PageTable.COLOR, mColor);
        cv.put(MetaData.PageTable.STYLE, mGridLine);
        // BEGIN: Better
        cv.put(MetaData.PageTable.USER_ACCOUNT, notePage.getUserId());
        cv.put(MetaData.PageTable.VERSION, notePage.getVersion());
        // END: Better
        
		cv.put(MetaData.PageTable.TEMPLATE, notePage.getTemplate());//wendy allen++ for template 0706
		
        //Begin Darwin_Yu@asus.com
        cv.put(MetaData.PageTable.IS_LAST_EDIT, setLastestEditPage());
        //End   Darwin_Yu@asus.com
        //BEGIN: RICHARD
        cv.put(MetaData.PageTable.INDEX_LANGUAGE, getIndexLanguage());
        //END: RICHARD
        cr.insert(MetaData.PageTable.uri, cv);
		//Begin Darwin_Yu@asus.com
		ContentValues cvDoodle = new ContentValues();
		cvDoodle.put(MetaData.DoodleTable.ID, notePage.getCreatedTime());
		cvDoodle.put(MetaData.DoodleTable.IS_DELETE, notePage.isDeleted() ? 1 : 0);
		cr.insert(MetaData.DoodleTable.uri, cvDoodle);
		
		ContentValues cvItem = new ContentValues();
		cvItem.put(MetaData.ItemTable.ID, notePage.getCreatedTime());
		cvItem.put(MetaData.ItemTable.IS_DELETE, notePage.isDeleted() ? 1 : 0);
		cr.insert(MetaData.ItemTable.uri, cvItem);
		//End   Darwin_Yu@asus.com
        int index = mPageOrderList.indexOf(previousPage.getCreatedTime());
        if (index == mPageOrderList.size() - 1) {
            mPageOrderList.add(notePage.getCreatedTime());
        }
        else {
            mPageOrderList.add(index + 1, notePage.getCreatedTime());
        }
        updateOrderListData();
      //Begin Allen to update widget
        updateWidget(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_ADD_PAGE,notePage.getCreatedTime());
        //End Allen
    }

    public NotePage getNotePage(long time) {
        Cursor cursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null, "created_date = ?", new String[] { Long.toString(time) }, null);
        if (cursor.getCount() == 0) {
        	cursor.close();//RICHARD FIX MEMORY LEAK
            return null;
        }
        else {
            cursor.moveToFirst();
            NotePage notePage = new NotePage(mContext, mCreatedTime);
            notePage.setBookmarkNoUpdateDB((cursor.getInt(MetaData.PageTable.INDEX_IS_BOOKMARK) > 0));
            notePage.setCreatedTime(cursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE));
            notePage.setPageColor((cursor.getInt(MetaData.PageTable.INDEX_COLOR)));
            notePage.setPageSize(cursor.getInt(MetaData.PageTable.INDEX_PAGE_SIZE));
            notePage.setPageStyle(cursor.getInt(MetaData.PageTable.INDEX_STYLE));
            // BEGIN: Better
            notePage.setModifiedTime(cursor.getLong(MetaData.PageTable.INDEX_MODIFIED_DATE));
            notePage.setLastSyncModifiedTime(cursor.getLong(MetaData.PageTable.INDEX_LASYSYNC_MODIFYTIME));
            notePage.setOwnerBookId(cursor.getLong(MetaData.PageTable.INDEX_OWNER));
            notePage.setIndexStatus(cursor.getInt(MetaData.PageTable.INDEX_IS_INDEXED));//RICHARD
            notePage.setIndexLanguage(cursor.getInt(MetaData.PageTable.INDEX_INDEX_LANGUAGE));//RICHARD
            long bookId = cursor.getLong(MetaData.PageTable.INDEX_LASTSYNC_OWNER);
            if (bookId <= 0) {
            	bookId = cursor.getLong(MetaData.PageTable.INDEX_OWNER);
            }
            notePage.setLastSyncOwnerBookId(bookId);
            notePage.setDeleted(cursor.getInt(MetaData.PageTable.INDEX_IS_DELETED) > 0);
            notePage.setTemplate(cursor.getInt(MetaData.PageTable.INDEX_TEMPALTE));//wendy allen++ for template 0706
            int userIdIndex = cursor.getColumnIndex(MetaData.PageTable.USER_ACCOUNT);
            if (userIdIndex >= 0) {
            	long userId = cursor.getLong(userIdIndex);
            	notePage.setUserId(userId);
            } else {
            	notePage.setUserId(0);
            }
            notePage.setVersion(cursor.getInt(MetaData.PageTable.INDEX_VERSION));
            // END: Better
            cursor.close();
            return notePage;
        }

    }

    public List<NotePage> getNotePages(Set<Long> pageIds) {
        List<NotePage> pages = new ArrayList<NotePage>();
        Cursor cursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null, "owner = ?", new String[] { Long.toString(mCreatedTime) }, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Long id = cursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE);
            if (pageIds.contains(id)) {
                NotePage page = new NotePage(mContext, mCreatedTime);
                page.setBookmark((cursor.getInt(MetaData.PageTable.INDEX_IS_BOOKMARK) > 0));
                page.setCreatedTime(cursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE));
            }
            cursor.moveToNext();
        }
        cursor.close();
        return pages;
    }
	
	// BEGIN: Wendy
    public void saveNoModify() {
        ContentValues cv = new ContentValues();
        cv.put(MetaData.BookTable.TITLE, mTitle);
        cv.put(MetaData.BookTable.CREATED_DATE, mCreatedTime);
        cv.put(MetaData.BookTable.IS_LOCKED, mIsLocked);
        cv.put(MetaData.BookTable.BOOK_SIZE, mPageSize);
        cv.put(MetaData.BookTable.BOOK_COLOR, mColor);
        cv.put(MetaData.BookTable.BOOK_GRID, mGridLine);
        cv.put(MetaData.BookTable.IS_PHONE_MEMO, mIsPhoneMemo ? 1 : 0);
        cv.put(MetaData.BookTable.FONT_SIZE, mFontSize);
        cv.put(MetaData.BookTable.MODIFIED_DATE, mLastSyncModifiedTime);//add by wendy 0401
        Log.v("wendy","save notebook");
        cv.put(MetaData.BookTable.LASTSYNC_MODIFYTIME, mLastSyncModifiedTime);
        cv.put(MetaData.BookTable.IS_DELETED, mIsDeleted ? 1 : 0);
        // BEGIN: Better
        cv.put(MetaData.BookTable.USER_ACCOUNT, mUserId);
        cv.put(MetaData.BookTable.VERSION, mVersion);
        // END: Better
        cv.put(MetaData.BookTable.TEMPLATE, mTemplateType);//wendy allen++ for template 0706
        cv.put(MetaData.BookTable.INDEX_LANGUAGE, mIndexLanguage);//Allen
        cv.put(MetaData.BookTable.INDEX_COVER, mCoverIndex);//darwin
        cv.put(MetaData.BookTable.COVER_MODIFYTIME, mCoverModifyTime);//darwin
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            for (long value : mPageOrderList) {
                dos.writeLong(value);
            }
            cv.put(MetaData.BookTable.PAGE_ORDER, baos.toByteArray());
            dos.close();
            baos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        int count = mContentResolver.update(MetaData.BookTable.uri, cv, "created_date = ?", new String[] { Long.toString(mCreatedTime) });
        if (count == 0) {
            mContentResolver.insert(MetaData.BookTable.uri, cv);
        }
    }
	// END: Wendy
    
    // BEGIN: Better
    public void insert() {
        ContentValues cv = new ContentValues();
        cv.put(MetaData.BookTable.TITLE, mTitle);
        cv.put(MetaData.BookTable.CREATED_DATE, mCreatedTime);
        cv.put(MetaData.BookTable.IS_LOCKED, mIsLocked);
        cv.put(MetaData.BookTable.BOOK_SIZE, mPageSize);
        cv.put(MetaData.BookTable.BOOK_COLOR, mColor);
        cv.put(MetaData.BookTable.BOOK_GRID, mGridLine);
        cv.put(MetaData.BookTable.IS_PHONE_MEMO, mIsPhoneMemo ? 1 : 0);
        cv.put(MetaData.BookTable.FONT_SIZE, mFontSize);
        cv.put(MetaData.BookTable.MODIFIED_DATE, System.currentTimeMillis());//add by wendy 0401
        cv.put(MetaData.BookTable.USER_ACCOUNT, mUserId);
        cv.put(MetaData.BookTable.VERSION, mVersion);
        cv.put(MetaData.BookTable.TEMPLATE, mTemplateType);//wendy allen++ for template 0706
        cv.put(MetaData.BookTable.INDEX_LANGUAGE, mIndexLanguage);//Allen
        cv.put(MetaData.BookTable.INDEX_COVER, mCoverIndex); //darwin 	
        cv.put(MetaData.BookTable.COVER_MODIFYTIME, mCoverModifyTime);//darwin
        Log.v("wendy","save notebook");

        // new page order save method
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            for (long value : mPageOrderList) {
                dos.writeLong(value);
            }
            cv.put(MetaData.BookTable.PAGE_ORDER, baos.toByteArray());
            dos.close();
            baos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        mContentResolver.insert(MetaData.BookTable.uri, cv);
    }
    // END: Better

    public void save() {
        ContentValues cv = new ContentValues();
        cv.put(MetaData.BookTable.TITLE, mTitle);
        cv.put(MetaData.BookTable.CREATED_DATE, mCreatedTime);
        cv.put(MetaData.BookTable.IS_LOCKED, mIsLocked);
        cv.put(MetaData.BookTable.BOOK_SIZE, mPageSize);
        cv.put(MetaData.BookTable.BOOK_COLOR, mColor);
        cv.put(MetaData.BookTable.BOOK_GRID, mGridLine);
        cv.put(MetaData.BookTable.IS_PHONE_MEMO, mIsPhoneMemo ? 1 : 0);
        cv.put(MetaData.BookTable.FONT_SIZE, mFontSize);
        cv.put(MetaData.BookTable.MODIFIED_DATE, System.currentTimeMillis());//add by wendy 0401
        // BEGIN: Better
        cv.put(MetaData.BookTable.USER_ACCOUNT, mUserId);
        cv.put(MetaData.BookTable.VERSION, mVersion);
        // END: Better
        cv.put(MetaData.BookTable.TEMPLATE, mTemplateType);//wendy allen++ for template 0706
        cv.put(MetaData.BookTable.INDEX_LANGUAGE, mIndexLanguage);//Allen
        cv.put(MetaData.BookTable.INDEX_COVER, mCoverIndex);//darwin
        cv.put(MetaData.BookTable.COVER_MODIFYTIME, mCoverModifyTime);//darwin
        Log.v("wendy","save notebook");

        // new page order save method
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            for (long value : mPageOrderList) {
                dos.writeLong(value);
            }
            cv.put(MetaData.BookTable.PAGE_ORDER, baos.toByteArray());
            dos.close();
            baos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        int count = mContentResolver.update(MetaData.BookTable.uri, cv, "created_date = ?", new String[] { Long.toString(mCreatedTime) });
        if (count == 0) {
            mContentResolver.insert(MetaData.BookTable.uri, cv);
        }
    }

    public void load(Cursor cursor) {
        mTitle = cursor.getString(MetaData.BookTable.INDEX_TITLE);
        mCreatedTime = cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
        mIsLocked = (cursor.getInt(MetaData.BookTable.INDEX_IS_LOCKED)) > 0 ? true : false;
        mPageSize = cursor.getInt(MetaData.BookTable.INDEX_BOOK_SIZE);
        mColor = cursor.getInt(MetaData.BookTable.INDEX_BOOK_COLOR);
        mGridLine = cursor.getInt(MetaData.BookTable.INDEX_BOOK_GRID);
        // BEGIN: Better
        mLastSyncModifiedTime = cursor.getLong(MetaData.BookTable.INDEX_LASYSYNC_MODIFYTIME);
        mIsDeleted = cursor.getInt(MetaData.BookTable.INDEX_IS_DELETED) > 0;
        int userIdIndex = cursor.getColumnIndex(MetaData.BookTable.USER_ACCOUNT);
        if (userIdIndex >= 0) {
        	long userId = cursor.getLong(userIdIndex);
        	mUserId = userId;
        } else {
        	mUserId = 0;
        }
        mVersion = cursor.getInt(MetaData.BookTable.INDEX_VERSION);
        mTemplateType = cursor.getInt(MetaData.BookTable.INDEX_TEMPLATE);//wendy allen++ for template 0706
        mIndexLanguage = cursor.getInt(MetaData.BookTable.INDEX_INDEX_LANGUAGE);//Allen
        mCoverIndex = cursor.getInt(MetaData.BookTable.INDEX_INDEX_COVER);//darwin
        mCoverModifyTime = cursor.getLong(MetaData.BookTable.INDEX_COVER_MODIFYTIME);//darwin
        // END: Better
        byte[] data = cursor.getBlob(MetaData.BookTable.INDEX_PAGE_ORDER);
        // translate old data to new data
        if (data != null) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bais);
                PageOrderList list = (PageOrderList) ois.readObject();
                mPageOrderList = list.getList();
                ois.close();
                bais.close();
            }
            catch (Exception e) {
                // e.printStackTrace();
                // read object failed, so read byte
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                DataInputStream dis = new DataInputStream(bais);
                mPageOrderList = new ArrayList<Long>();
                try {
                    while (dis.available() != 0) {
                        long value = dis.readLong();
                        mPageOrderList.add(Long.valueOf(value));
                    }
                    dis.close();
                    bais.close();
                }
                catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        }
       // updateOrderListData(); //modify by wendy 0406

    }
    
    // BEGIN: Better
    public void loadNewBook(Cursor cursor) {
    	int index = -1;
    	index = cursor.getColumnIndex(MetaData.BookTable.TITLE);
    	if (index >= 0) {
    		mTitle = cursor.getString(index);
    	}
        index = cursor.getColumnIndex(MetaData.BookTable.IS_LOCKED);
    	if (index >= 0) {
    		mIsLocked = cursor.getInt(index) > 0 ? true : false;
    	}
    	index = cursor.getColumnIndex(MetaData.BookTable.BOOK_SIZE);
    	if (index >= 0) {
    		mPageSize = cursor.getInt(index);
    	}
    	index = cursor.getColumnIndex(MetaData.BookTable.BOOK_COLOR);
    	if (index >= 0) {
    		mColor = cursor.getInt(index);
    	}
    	index = cursor.getColumnIndex(MetaData.BookTable.BOOK_GRID);
    	if (index >= 0) {
    		mGridLine = cursor.getInt(index);
    	}
    	index = cursor.getColumnIndex(MetaData.BookTable.TEMPLATE);
    	if (index >= 0) {
    		mTemplateType = cursor.getInt(index);
    	}
    	index = cursor.getColumnIndex(MetaData.BookTable.INDEX_LANGUAGE);
    	if (index >= 0) {
    		mIndexLanguage = cursor.getInt(index);//Allen
    	}
    	//darwin
    	index = cursor.getColumnIndex(MetaData.BookTable.INDEX_COVER);
    	if (index >= 0) {
    		mCoverIndex = cursor.getInt(index);
    	}
    	index = cursor.getColumnIndex(MetaData.BookTable.COVER_MODIFYTIME);
    	if (index >= 0) {
    		mCoverModifyTime = cursor.getLong(index);
    	}
    	//darwin
    }
    // END: Better

    public static void deleteDir(File dir) { // change to static by jason
        if (dir.exists()) {
        	if (dir.isDirectory()) {
        		 String[] subDirs = dir.list();
                 for (String sub : subDirs) {
                     deleteDir(new File(dir, sub));
                 }
			}else {
				dir.delete();
			}
        }   
    }

    //begin darwin
    public void clearPageFoemLocal(Long item)
    {
    	mPageOrderList.remove(item);
        mContentResolver.delete(MetaData.PageTable.uri, "(created_date = ?) AND (owner = ?)", 
       		 new String[] { item.toString(), Long.toString(mCreatedTime) });
        mContentResolver.delete(MetaData.TimestampTable.uri, "owner = ?", new String[]{item.toString()});
        updateOrderListDataNoModifyTime();
    }
    //end  darwin
	// BEGIN: Wendy
    public void deletePagefromWeb(Long item)
    {
    	deleteDir(new File(MetaData.DATA_DIR + mCreatedTime + "/" + item));
        mPageOrderList.remove(item);
        // BEGIN: Better
        mContentResolver.delete(MetaData.PageTable.uri, "(created_date = ?) AND (owner = ?)", 
       		 new String[] { item.toString(), Long.toString(mCreatedTime) });
        // END: Better
        mContentResolver.delete(MetaData.TimestampTable.uri, "owner = ?", new String[]{item.toString()});//wendy
        mContentResolver.delete(MetaData.ItemTable.uri, "_id = ?", new String[]{item.toString()});
        mContentResolver.delete(MetaData.DoodleTable.uri, "_id = ?", new String[]{item.toString()});
        mContentResolver.delete(MetaData.AttachmentTable.uri, "_id = ?", new String[]{item.toString()});
        updateOrderListDataNoModifyTime();
      //Begin Allen to update widget
        updateWidget(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_PAGE ,item);
        //End Allen
    }
	// END: Wendy
	
    public void deletePage(Long item) {
        List<Long> items = new ArrayList<Long>();
        items.add(item);
        deletePages(items);
      //Begin Allen to update widget
        updateWidget(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_PAGE,item);
        //End Allen
    }
    
    // BEGIN: Better
    public void deletePageFully(Long item) {
    	 deleteDir(new File(MetaData.DATA_DIR + mCreatedTime + "/" + item));
         mPageOrderList.remove(item);
         mContentResolver.delete(MetaData.PageTable.uri, "(created_date = ?) AND (owner = ?)", 
        		 new String[] { item.toString(), Long.toString(mCreatedTime) });
         mContentResolver.delete(MetaData.MemoTable.uri, "(page_id = ?) AND (book_id = ?)", 
        		 new String[] { item.toString(), Long.toString(mCreatedTime) });
         mContentResolver.delete(MetaData.TimestampTable.uri, "owner = ?", new String[]{item.toString()});//wendy
         mContentResolver.delete(MetaData.ItemTable.uri, "_id = ?", new String[]{item.toString()});
         mContentResolver.delete(MetaData.DoodleTable.uri, "_id = ?", new String[]{item.toString()});
         mContentResolver.delete(MetaData.AttachmentTable.uri, "_id = ?", new String[]{item.toString()});
         updateOrderListData();
       //Begin Allen to update widget
         updateWidget(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_PAGE,item);
         //End Allen
    }
    // END: Better

    public int deletePages(List<Long> items) {
        int count = 0;
        if (items == null) {
            return count;
        }
        else {
            for (Long item : items) {
                deleteDir(new File(MetaData.DATA_DIR + mCreatedTime + "/" + item));
                mPageOrderList.remove(item);
                if (mUserId > 0) {
	                //count = mContentResolver.delete(MetaData.PageTable.uri, "created_date = ?", new String[] { item.toString() });//modify by wendy 0401
	               //add by wendy 0401 delete -> update begin++
	                ContentValues cv = new ContentValues();
	                cv.put(MetaData.PageTable.IS_DELETED, 1);
	                count = mContentResolver.update(MetaData.PageTable.uri, cv, "created_date = ?", new String[] { item.toString() });//add by wendy 0401
	              //add by wendy 0401 delete -> update end--
                } else {
                	count = mContentResolver.delete(MetaData.PageTable.uri, "created_date = ?", new String[] { item.toString() });
                	 mContentResolver.delete(MetaData.TimestampTable.uri, "owner = ?", new String[]{item.toString()});//wendy
                	 mContentResolver.delete(MetaData.ItemTable.uri, "_id = ?", new String[]{item.toString()});
                	 mContentResolver.delete(MetaData.DoodleTable.uri, "_id = ?", new String[]{item.toString()});
                	 mContentResolver.delete(MetaData.AttachmentTable.uri, "_id = ?", new String[]{item.toString()});
                }
                
                count = mContentResolver.delete(MetaData.MemoTable.uri, "page_id = ?", new String[] { item.toString() });
              //Begin Allen to update widget
                updateWidget(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_PAGE,item);
                //End Allen
            }
            updateOrderListData();       
            return count;
        }
    }

    public void updateOrderListData() {
        ContentValues cv = new ContentValues();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for (Long value : mPageOrderList) {
            try {
                dos.writeLong(value);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        cv.put(MetaData.BookTable.PAGE_ORDER, baos.toByteArray());
        cv.put(MetaData.BookTable.MODIFIED_DATE, System.currentTimeMillis());//add by wendy 0401
        Log.v("wendy","updateOrderListData modify curtime");

        mContentResolver.update(MetaData.BookTable.uri, cv, "created_date = ?", new String[] { Long.toString(mCreatedTime) });
    }

    //begin  darwin
    public void changeBookCover()
    {
    	long pageid = getPageOrder(0);
		NotePage notepage = getNotePage(pageid);
		if(notepage == null)
		{
			deleteBookCoverThumb();
	    	//emmanual to fix bug 479300
			saveBookCoverThumb(new PageDataLoader(mContext), false, getNotePage(getPageOrder(0)));
			return;
		}
		
		//emmanual to fix the book thumbnail change after reorder
		File srcFile = new File(getBookPath() + "/" + pageid, MetaData.THUMBNAIL_PREFIX);
		File dstFile = new File(getBookPath(), MetaData.THUMBNAIL_PREFIX);
		if (srcFile.exists()) {
			try {
				FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
				FileChannel destChannel = new FileOutputStream(dstFile).getChannel();
				destChannel.transferFrom(srcChannel, 0, srcChannel.size());
				srcChannel.close();
				destChannel.close();
			} catch (Exception e) {
				saveBookCoverThumb(new PageDataLoader(mContext), false, notepage);
			}
		} else {
			//emmanual to fix bug 419021
			saveBookCoverThumb(new PageDataLoader(mContext), false, notepage);
		}
		//end emmanual
    }
    public void deleteBookCoverThumb()
    {
    	File file = new File(getBookPath(), MetaData.THUMBNAIL_PREFIX);
        if (file.exists()) 
        {
            file.delete();
        }
    }
    
    //begin smilefish
    static public Bitmap getNoteBookCover(NoteBook book, Context context)
    {
		Bitmap bitmap = null;
			switch(book.getCoverIndex() - 1)
			{
			case 0:
				bitmap = null;
				break;
			case 1:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover01);
				break;
			case 2:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover02);
				break;	
			case 3:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover03);
				break;
			case 4:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover04);
				break;
			case 5:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover05);
				break;	
			case 6:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover06);
				break;
			case 7:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover07);
				break;
			case 8:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover08);
				break;
			case 9:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover09);
				break;
			case 10:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover10);
				break;
			case 11:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover11);
				break;
			case 12:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover12);
				break;
			case 13:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover13);
				break;
			case 14:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover14);
				break;
			case 15:
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover15);
				break;
			}

		return bitmap;
    }
    //end smilefish
    
    static public Bitmap getDefaultNoteBookThumbnail(int index,Context context)
    {
    	Bitmap bitmap = null;
    	switch(index)
    	{
    	case MetaData.NOTEBOOK_DEFAULT_COVER_1_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover01 );
    		break;
    	case MetaData.NOTEBOOK_DEFAULT_COVER_2_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover02 );
    		break;
    	case MetaData.NOTEBOOK_DEFAULT_COVER_3_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover03 );
    		break;
    	case MetaData.NOTEBOOK_DEFAULT_COVER_4_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover04 );
    		break;
    	case MetaData.NOTEBOOK_DEFAULT_COVER_5_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover05 );
    		break;
    	case MetaData.NOTEBOOK_DEFAULT_COVER_6_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover06 );
    		break;
    	case MetaData.NOTEBOOK_DEFAULT_COVER_7_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover07 );
    		break;
    	case MetaData.NOTEBOOK_DEFAULT_COVER_8_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover08 );
    		break;
    	case MetaData.NOTEBOOK_DEFAULT_COVER_9_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover09 );
    		break;
    	case MetaData.NOTEBOOK_DEFAULT_COVER_10_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover10 );
    		break;
    	case MetaData.NOTEBOOK_DEFAULT_COVER_11_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover11 );
    		break;
    	case MetaData.NOTEBOOK_DEFAULT_COVER_12_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover12 );
    		break;
    	case MetaData.NOTEBOOK_DEFAULT_COVER_13_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover13 );
    		break;
    	case MetaData.NOTEBOOK_DEFAULT_COVER_14_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover14 );
    		break;
    	case MetaData.NOTEBOOK_DEFAULT_COVER_15_INDEX:
    		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_supernote_bookcover15 );
    		break;
    	}
    	return bitmap;
    }
    
    public void saveBookCoverThumbFromRecognize()
    {
    	try {
    		//Bitmap bitmap = null;
    		String name = "";
    		if(this.mCoverIndex == 1)
    		{
    			name = MetaData.THUMBNAIL_COVER;
    		}
    		
    		Bitmap bitmap = NotePage.getNoteBookDefaultCoverThumbnail(this.getCreatedTime());
            if (bitmap != null) {
                File file = new File(getBookPath(), name);
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
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void saveBookCoverThumbFromImport(PageDataLoader loader, boolean isLoadAsync, NotePage page)
    {
    	try {
    		Bitmap bitmap = null;
    		String name = "";
    		if(this.mCoverIndex == 0)
    		{
    			bitmap = getThumbnailNoBackground(loader, isLoadAsync, this,page,getBookColor(),getGridType());
    			name = MetaData.THUMBNAIL_PREFIX;
    		}
    		else if(this.mCoverIndex == 1)
    		{
    			name = MetaData.THUMBNAIL_COVER;
    		}
    		else
    		{
    			bitmap = getDefaultNoteBookThumbnail(this.mCoverIndex,mContext);
    			name = MetaData.THUMBNAIL_COVER;
    		}
            
            if (bitmap != null) {
                File file = new File(getBookPath(), name);
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
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void saveBookCoverThumb(PageDataLoader loader, boolean isLoadAsync, NotePage page)
    {
    	try {
            Bitmap bitmap = getThumbnailNoBackground(loader, isLoadAsync, this,page,getBookColor(),getGridType());
            if(bitmap != null)
            	saveBookCoverThumb(bitmap);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Emmanual
    public void savePageThumb(PageDataLoader loader, boolean isLoadAsync, NotePage page)
    {
    	try {
            Bitmap bitmap = getThumbnailNoBackground(loader, isLoadAsync, this,page,getBookColor(),getGridType());
            if(bitmap != null)
            	savePageThumb(bitmap, page);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //Emmanual
    public void savePageThumb(Bitmap bitmap, NotePage notepage){
    	try {
            if (bitmap != null) {
                File file = new File(notepage.getFilePath(), MetaData.THUMBNAIL_PREFIX);
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
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 保存书籍的缩略图
     * @author noah_zhang
     * @param inputStream
     */
    public void saveBookCoverThumb(Bitmap bitmap){
    	try {
            if (bitmap != null) {
                File file = new File(getBookPath(), MetaData.THUMBNAIL_PREFIX);
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
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 保存书籍的缩略图
     * @author noah_zhang
     * @param inputStream
     */
    public void saveBookCoverThumb(InputStream inputStream){
    	try {
    		File file = new File(getBookPath(), MetaData.THUMBNAIL_PREFIX);
            if (file.exists() == false) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            copy(inputStream, fos);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void copy(InputStream is, OutputStream os) {
        byte[] byteBuffer = new byte[8192];
        int byteIn = 0;
        try {
            while ((byteIn = is.read(byteBuffer)) >= 0) {
                os.write(byteBuffer, 0, byteIn);
            }
            is.close();
            os.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获得page的缩略图
     * @param loader
     * @param isLoadAsync
     * @param notebook
     * @param page
     * @param color
     * @param line
     * @param width
     * @param height
     * @return
     */
    public Bitmap getThumbnailNoBackground(PageDataLoader loader, boolean isLoadAsync, NoteBook notebook ,NotePage page ,int color, int line,
    		int width, int height){
    	Bitmap result = null;
    	Resources res = mContext.getResources();
        Bitmap content;
        Canvas resultCanvas, contentCanvas;
        Paint paint = new Paint();
        int targetWidth, targetHeight;
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        try {
            result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            resultCanvas = new Canvas(result);
            targetWidth = (int) (result.getWidth() * 0.9);
            targetHeight = (int) (result.getHeight() * 0.85);
            content = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
            content.setDensity(Bitmap.DENSITY_NONE);
            contentCanvas = new Canvas(content);
            if (!isLoadAsync) {
            	loader.load(page);
            }
            page.load(loader, isLoadAsync, contentCanvas, true, false, false);
            float left = res.getDimension(R.dimen.thumb_padding_left);
            float top = res.getDimension(R.dimen.thumb_padding_top);
            resultCanvas.translate(left, top);
            resultCanvas.drawBitmap(content, 0, 0, paint);
            content.recycle();
            content = null;
        }
        catch (OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] Loading cover failed !!!");
        }
        return result;
    }
    
    public Bitmap getThumbnailNoBackground(PageDataLoader loader, boolean isLoadAsync, NoteBook notebook ,NotePage page ,int color, int line) {
    	Resources res = mContext.getResources();
    	Bitmap cover = null;
        cover = CoverHelper.getDefaultCoverBitmap(color, line, res);//Allen
        if(cover == null)
        	return null;
        return getThumbnailNoBackground(loader, isLoadAsync, notebook, page, color, line, cover.getWidth(), cover.getHeight());
    }
    //end    darwin
    public void changePageOrder(Long pageId, Long who) {
        int position = mPageOrderList.indexOf(who);
        mPageOrderList.remove(pageId);
        mPageOrderList.add(position, pageId);
        updateOrderListData();

		//Begin by Emmanual
		if(MetaData.IS_GA_ON)
		{
			GACollector gaCollector = new GACollector(mContext);
			gaCollector.reorderPage();
		}
		//End
    }

    public void setSelectedItems(SortedSet<SimplePageInfo> items) {
        mSelectedItemsList = new TreeSet<SimplePageInfo>(items);
    }

    public SortedSet<SimplePageInfo> getSelectedItems() {
        return mSelectedItemsList;
    }

    public List<Long> getSelectedIds() {
        List<Long> ids = new ArrayList<Long>();
        if (mSelectedItemsList == null) {
            return null;
        }
        else {
            for (SimplePageInfo info : mSelectedItemsList) {
                ids.add(info.pageId);
            }
            return ids;
        }
    }
    
    //begin wendy
    private void copyTimestampView(long oldpageid, long newpageid)
    {    	
    	Cursor cursor = mContentResolver.query(MetaData.TimestampTable.uri, null, "owner = ?", new String[]{Long.toString(oldpageid)}, null);
    	if(cursor.getCount() > 0 )
    	{
    		cursor.moveToFirst();
    		while(!cursor.isAfterLast())
    		{
    			  ContentValues cv = new ContentValues();
    			  cv.put(MetaData.TimestampTable.CREATED_DATE, cursor.getLong(MetaData.TimestampTable.INDEX_CREATE_DATE));
    			  cv.put(MetaData.TimestampTable.POSITION, cursor.getInt(MetaData.TimestampTable.INDEX_POSITION));
    			  cv.put(MetaData.TimestampTable.OWNER, newpageid);
    			  //darwin
    			  Cursor cur = mContentResolver.query(MetaData.PageTable.uri, 
    					  null, 
    					  "created_date = ?", 
    					  new String[]{Long.toString(newpageid)}, 
    					  null);
    			  if(cur.getCount() > 0 )
    			  {
    				  cur.moveToFirst();
    				  if(!cur.isAfterLast())
    				  {
    					  cv.put(MetaData.TimestampTable.USER_ACCOUNT,cur.getLong(MetaData.PageTable.INDEX_USER_ACCOUNT));
    				  }
    			  }
				  cur.close();//RICHARD FIX MEMORY LEAK
    			  //darwin
    			  mContentResolver.insert(MetaData.TimestampTable.uri, cv);
    			  cursor.moveToNext();
    		}    		
    	}
    	cursor.close();
    
    }
    //end wendy
    
    private void copyDoodleItemAttachmentDB(long oldpageid, long newpageid)
	{	
    	Cursor cursor = null;
		try
		{
			ContentResolver cr = mContext.getContentResolver();
	    	cursor = cr.query(MetaData.ItemTable.uri, null, "_id = ?", new String[]{Long.toString(oldpageid)}, null);
	    	if(cursor.getCount() > 0 )
	    	{
	    		cursor.moveToFirst();
	    		while(!cursor.isAfterLast())
	    		{
	    			ContentValues cvItem = new ContentValues();
	    			cvItem.put(MetaData.ItemTable.ID, newpageid);
	    			cvItem.put(MetaData.ItemTable.IS_DELETE, cursor.getInt(MetaData.ItemTable.INDEX_IS_DELETE));
	    			cvItem.put(MetaData.ItemTable.MODIFIED_DATE,  cursor.getLong(MetaData.ItemTable.INDEX_MODIFIED_DATE));
	    			cvItem.put(MetaData.ItemTable.LASTSYNC_MODIFYTIME,  cursor.getLong(MetaData.ItemTable.INDEX_LASTSYNC_MODIFYTIME));
	    			cr.insert(MetaData.ItemTable.uri, cvItem);
	    			cursor.moveToNext();
	    		}    		
	    	}
	    	cursor.close();
	    	
	    	cursor = cr.query(MetaData.DoodleTable.uri, null, "_id = ?", new String[]{Long.toString(oldpageid)}, null);
	    	if(cursor.getCount() > 0 )
	    	{
	    		cursor.moveToFirst();
	    		while(!cursor.isAfterLast())
	    		{
	    			ContentValues cvDoodle = new ContentValues();
	    			cvDoodle.put(MetaData.DoodleTable.ID, newpageid);
	    			cvDoodle.put(MetaData.DoodleTable.IS_DELETE, cursor.getInt(MetaData.DoodleTable.INDEX_IS_DELETE));
	    			cvDoodle.put(MetaData.DoodleTable.MODIFIED_DATE, cursor.getLong(MetaData.DoodleTable.INDEX_MODIFIED_DATE));
	    			cvDoodle.put(MetaData.DoodleTable.LASTSYNC_MODIFYTIME,  cursor.getLong(MetaData.DoodleTable.INDEX_LASTSYNC_MODIFYTIME));
	    			cr.insert(MetaData.DoodleTable.uri, cvDoodle);
	    			cursor.moveToNext();
	    		}    		
	    	}
	    	cursor.close();
	    	
	    	cursor = cr.query(MetaData.AttachmentTable.uri, null, "_id = ?", new String[]{Long.toString(oldpageid)}, null);
	    	if(cursor.getCount() > 0 )
	    	{
	    		cursor.moveToFirst();
	    		while(!cursor.isAfterLast())
	    		{
	    			ContentValues cvAttachment = new ContentValues();
		        	cvAttachment.put(MetaData.AttachmentTable.ID, newpageid);
		        	cvAttachment.put(MetaData.AttachmentTable.MODIFIED_DATE, cursor.getLong(MetaData.AttachmentTable.INDEX_MODIFIED_DATE));
		        	cvAttachment.put(MetaData.AttachmentTable.IS_DELETE, cursor.getInt(MetaData.AttachmentTable.INDEX_IS_DELETE));
		        	cvAttachment.put(MetaData.AttachmentTable.FILE_NAME, cursor.getString(MetaData.AttachmentTable.INDEX_FILE_NAME));
		        	cr.insert(MetaData.AttachmentTable.uri, cvAttachment);
	    			cursor.moveToNext();
	    		}    		
	    	}
	    	cursor.close();
		} catch(Exception e)
		{
			e.printStackTrace();
		}finally{
			if(cursor != null)
				cursor.close(); //smilefish add for memory leak
		}
	}
    
    public void copyPagesFrom(NoteBook src) {
        for (Long item : src.getSelectedIds()) {
            Cursor cursor = mContentResolver.query(MetaData.PageTable.uri, null, "created_date = ?", new String[] { item.toString() }, null);
            cursor.moveToFirst();
            NotePage page = new NotePage(mContext, mCreatedTime);
            page.setBookmark((cursor.getInt(MetaData.PageTable.INDEX_IS_BOOKMARK) > 0) ? true : false);
            addPage(page);
            //begin wendy
            copyTimestampView(item, page.getCreatedTime());
            //end wendy
            copyDoodleItemAttachmentDB(item, page.getCreatedTime());
            page.getFilePath();
            File srcFile = new File(MetaData.DATA_DIR + src.getCreatedTime(), item.toString());
            File destFile = new File(MetaData.DATA_DIR + mCreatedTime, Long.toString(page.getCreatedTime()));
            copyFile(srcFile, destFile);
            cursor.close();
        }
    }
    
  //BEGIN: Darwin
    public void copyPicFrom(NoteBook src) {

        File srcFile = new File(MetaData.DATA_DIR + src.getCreatedTime(), src.getCreatedTime() + MetaData.THUMBNAIL_COVER_CROP);
        if(!srcFile.exists())
        {
        	return ;
        }
        File destFile = new File(MetaData.DATA_DIR + mCreatedTime, mCreatedTime + MetaData.THUMBNAIL_COVER_CROP);
        try{
        	destFile.createNewFile();
        }
        catch(Exception e)
        {
        	Log.i(this.TAG, e.toString());
        }
        copyFile(srcFile, destFile);
        
    }
    
    public void copyCoverFrom(NoteBook src) {

        File srcFile = new File(MetaData.DATA_DIR + src.getCreatedTime(), MetaData.THUMBNAIL_COVER);
        if(!srcFile.exists())
        {
        	return ;
        }
        File destFile = new File(MetaData.DATA_DIR + mCreatedTime, MetaData.THUMBNAIL_COVER);
        try{
        	destFile.createNewFile();
        }
        catch(Exception e)
        {
        	Log.i(this.TAG, e.toString());
        }
        copyFile(srcFile, destFile);
        
    }
    
    //END: Darwin
    
    //BEGIN: RICHARD
    public void copyOnePagesFrom(NoteBook src,Long item) {
            Cursor cursor = mContentResolver.query(MetaData.PageTable.uri, null, "created_date = ?", new String[] { item.toString() }, null);
            cursor.moveToFirst();
            NotePage page = new NotePage(mContext, mCreatedTime);
            page.setBookmark((cursor.getInt(MetaData.PageTable.INDEX_IS_BOOKMARK) > 0) ? true : false);
            page.setIndexStatus(MetaData.INDEX_FILE_CREATE_LOCK);
            addPage(page);
            //begin wendy
            copyTimestampView(item, page.getCreatedTime());
            //end wendy
            copyDoodleItemAttachmentDB(item, page.getCreatedTime());
            page.getFilePath();
            File srcFile = new File(MetaData.DATA_DIR + src.getCreatedTime(), item.toString());
            File destFile = new File(MetaData.DATA_DIR + mCreatedTime, Long.toString(page.getCreatedTime()));
            copyFile(srcFile, destFile);
            cursor.close();
    }
    //END: RICHARD

    public void copyPagesFrom(Set<SimplePageInfo> items) {
        for (SimplePageInfo info : items) {
            NotePage oldPage = getNotePage(info.pageId);
            NotePage newPage = new NotePage(mContext, mCreatedTime);
            newPage.setBookmark(oldPage.isBookmark());
            newPage.getFilePath();
            addPage(newPage);
            copyTimestampView(oldPage.getCreatedTime(), newPage.getCreatedTime());
            copyDoodleItemAttachmentDB(oldPage.getCreatedTime(), newPage.getCreatedTime());
            File srcFile = new File(MetaData.DATA_DIR + info.bookId, info.pageId.toString());
            File destFile = new File(MetaData.DATA_DIR + mCreatedTime, Long.toString(newPage.getCreatedTime()));
            copyFile(srcFile, destFile);
        }
    }

    private void copyFile(File src, File dest) {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
            }
            String[] children = src.list();
            for (String child : children) {
                copyFile(new File(src, child), new File(dest, child));
            }
        }
        else {
            try {
                FileChannel srcChannel = new FileInputStream(src).getChannel();
                FileChannel destChannel = new FileOutputStream(dest).getChannel();
                destChannel.transferFrom(srcChannel, 0, srcChannel.size());
                srcChannel.close();
                destChannel.close();
            }
            catch (FileNotFoundException e) {
            	Log.i(this.TAG, e.toString());
            }
            catch (IOException e) {
            }
        }
    }
    
    private void updateTimestampInfo(long oldPageId, long newPageId) {
		ContentValues cv = new ContentValues();
		cv.put(MetaData.TimestampTable.OWNER, newPageId);
		mContext.getContentResolver().update(MetaData.TimestampTable.uri, cv, "owner = ?", 
				new String[] {Long.toString(oldPageId)});
	}
    
    private void updateDoodleItemAttachmentDB(long oldPageId, long newPageId)
	{	
		try
		{
			ContentValues cvDoodle = new ContentValues();
			cvDoodle.put(MetaData.DoodleTable.ID, newPageId);
			mContext.getContentResolver().update(MetaData.DoodleTable.uri, cvDoodle, "_id = ?", new String[] {Long.toString(oldPageId)});
	        
			ContentValues cvItem = new ContentValues();
			cvItem.put(MetaData.ItemTable.ID, newPageId);
			mContext.getContentResolver().update(MetaData.ItemTable.uri, cvItem, "_id = ?", new String[] {Long.toString(oldPageId)});
	        
			ContentValues cvAttachment = new ContentValues();
			cvAttachment.put(MetaData.AttachmentTable.ID, newPageId);
			mContext.getContentResolver().update(MetaData.AttachmentTable.uri, cvAttachment, "_id = ?", new String[] {Long.toString(oldPageId)});
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

    public void movePagesFrom(NoteBook src) {
        List<Long> selectedIds = src.getSelectedIds();
        if (selectedIds == null) {
            return;
        }
        for (Long item : src.getSelectedIds()) {
        	long newPageId = item.longValue();
        	if (src.mUserId > 0) {
        		if (src.mUserId == mUserId) {
        			ContentValues cv = new ContentValues();
    	            cv.put(MetaData.PageTable.OWNER, mCreatedTime);
    	            cv.put(MetaData.PageTable.USER_ACCOUNT, mUserId);
    	            mContentResolver.update(MetaData.PageTable.uri, cv, "created_date = ?", new String[] { Long.toString(newPageId) });
    	            src.removePageFromOrder(item);
    	            mPageOrderList.add(newPageId);
    	            updateOrderListData();
    	            
    	            File file = new File(MetaData.DATA_DIR + mCreatedTime);
    	            if (file.exists() == false) {
    	                file.mkdir();
    	                file = new File(file, item.toString());
    	                file.mkdir();
    	            }
    	            File srcFile = new File(MetaData.DATA_DIR + src.getCreatedTime(), item.toString());
    	            File destFile = new File(MetaData.DATA_DIR + mCreatedTime, Long.toString(newPageId));
    	            srcFile.renameTo(destFile);
    	            
    	            updateTimestampInfo(item.longValue(), newPageId);
       	         	updateDoodleItemAttachmentDB(item.longValue(), newPageId);
        		} else {
	        		Cursor cursor = mContentResolver.query(MetaData.PageTable.uri, null, "created_date = ?", new String[] { item.toString() }, null);
	                cursor.moveToFirst();
	                NotePage page = new NotePage(mContext, mCreatedTime);
	                page.setBookmark((cursor.getInt(MetaData.PageTable.INDEX_IS_BOOKMARK) > 0) ? true : false);
	                addPage(page);
	                page.getFilePath();
	                cursor.close();
	                newPageId = page.getCreatedTime();
	                
	                File file = new File(MetaData.DATA_DIR + mCreatedTime);
	                if (file.exists() == false) {
	                    file.mkdir();
	                    file = new File(file, item.toString());
	                    file.mkdir();
	                }
	                File srcFile = new File(MetaData.DATA_DIR + src.getCreatedTime(), item.toString());
	                File destFile = new File(MetaData.DATA_DIR + mCreatedTime, Long.toString(newPageId));
	                srcFile.renameTo(destFile);
	                
	                src.deletePage(item);
	                
	                updateTimestampInfo(item.longValue(), newPageId);
	   	         	updateDoodleItemAttachmentDB(item.longValue(), newPageId);
        		}
        	} else {
	            ContentValues cv = new ContentValues();
	            cv.put(MetaData.PageTable.OWNER, mCreatedTime);
	            cv.put(MetaData.PageTable.USER_ACCOUNT, mUserId);
	            if (mUserId > 0) {
	            	newPageId = SyncHelper.pageTime2Id(System.currentTimeMillis());
	            	cv.put(MetaData.PageTable.CREATED_DATE, newPageId);
	            }
	            mContentResolver.update(MetaData.PageTable.uri, cv, "created_date = ?", new String[] { Long.toString(newPageId) });
	            src.removePageFromOrder(item);
	            mPageOrderList.add(newPageId);
	            updateOrderListData();
	            
	            File file = new File(MetaData.DATA_DIR + mCreatedTime);
	            if (file.exists() == false) {
	                file.mkdir();
	                file = new File(file, item.toString());
	                file.mkdir();
	            }
	            File srcFile = new File(MetaData.DATA_DIR + src.getCreatedTime(), item.toString());
	            File destFile = new File(MetaData.DATA_DIR + mCreatedTime, Long.toString(newPageId));
	            srcFile.renameTo(destFile);
	            
	            updateTimestampInfo(item.longValue(), newPageId);
   	         	updateDoodleItemAttachmentDB(item.longValue(), newPageId);
        	}
        }
    }

    public void movePagesFrom(Set<SimplePageInfo> items) {
        BookCase bookCase = BookCase.getInstance(mContext);
        for (SimplePageInfo info : items) {
        	long newPageId = info.pageId;
        	NoteBook oldBook = bookCase.getNoteBook(info.bookId);
        	if (oldBook.mUserId > 0) {
        		if (oldBook.mUserId == mUserId) {
        			ContentValues cv = new ContentValues();
    	            cv.put(MetaData.PageTable.OWNER, mCreatedTime);
    	            cv.put(MetaData.PageTable.USER_ACCOUNT, mUserId);
    	            mContentResolver.update(MetaData.PageTable.uri, cv, "created_date = ?", new String[] { info.pageId.toString() });
    	            
    	            oldBook.removePageFromOrder(info.pageId);
    	            mPageOrderList.add(newPageId);
    	            updateOrderListData();
    	            
    	            File file = new File(MetaData.DATA_DIR + mCreatedTime);
    	            if (file.exists() == false) {
    	                file.mkdir();
    	                file = new File(file, info.pageId.toString());
    	                file.mkdir();
    	            }
    	            File srcFile = new File(MetaData.DATA_DIR + info.bookId, info.pageId.toString());
    	            File destFile = new File(MetaData.DATA_DIR + mCreatedTime, Long.toString(newPageId));
    	            srcFile.renameTo(destFile);
    	            
    	            updateTimestampInfo(info.pageId, newPageId);
       	         	updateDoodleItemAttachmentDB(info.pageId, newPageId);
        		} else {
	        		 NotePage oldPage = getNotePage(info.pageId);
	                 NotePage newPage = new NotePage(mContext, mCreatedTime);
	                 newPage.setBookmark(oldPage.isBookmark());
	                 newPage.getFilePath();
	                 addPage(newPage);
	                 newPageId = newPage.getCreatedTime();
	                 
	                 File file = new File(MetaData.DATA_DIR + mCreatedTime);
	                 if (file.exists() == false) {
	                     file.mkdir();
	                     file = new File(file, info.pageId.toString());
	                     file.mkdir();
	                 }
	                 File srcFile = new File(MetaData.DATA_DIR + info.bookId, info.pageId.toString());
	                 File destFile = new File(MetaData.DATA_DIR + mCreatedTime, Long.toString(newPageId));
	                 srcFile.renameTo(destFile);
	                 
	                 oldBook.deletePage(info.pageId);
	                 
	                 updateTimestampInfo(info.pageId, newPageId);
	       	         updateDoodleItemAttachmentDB(info.pageId, newPageId);
        		}
        	} else {
	            ContentValues cv = new ContentValues();
	            cv.put(MetaData.PageTable.OWNER, mCreatedTime);
	            cv.put(MetaData.PageTable.USER_ACCOUNT, mUserId);
	            if (mUserId > 0) {
	            	newPageId = SyncHelper.pageTime2Id(System.currentTimeMillis());
	            	cv.put(MetaData.PageTable.CREATED_DATE, newPageId);
	            }
	            mContentResolver.update(MetaData.PageTable.uri, cv, "created_date = ?", new String[] { info.pageId.toString() });
	            
	            oldBook.removePageFromOrder(info.pageId);
	            mPageOrderList.add(newPageId);
	            updateOrderListData();
	            
	            File file = new File(MetaData.DATA_DIR + mCreatedTime);
	            if (file.exists() == false) {
	                file.mkdir();
	                file = new File(file, info.pageId.toString());
	                file.mkdir();
	            }
	            File srcFile = new File(MetaData.DATA_DIR + info.bookId, info.pageId.toString());
	            File destFile = new File(MetaData.DATA_DIR + mCreatedTime, Long.toString(newPageId));
	            srcFile.renameTo(destFile);
	            
	            updateTimestampInfo(info.pageId, newPageId);
      	        updateDoodleItemAttachmentDB(info.pageId, newPageId);
        	}
        }
    }

    public void removePageFromOrder(Long item) {
        mPageOrderList.remove(item);
        updateOrderListData();
        //Begin Allen to update widget
        updateWidget(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_PAGE,item);
        //End Allen
    }

    public void addPageToOrderList(Long item) {
        mPageOrderList.add(item);
        updateOrderListData();//????  add by wendy 0401 used in movepagetask
        //Begin Allen to update widget
        updateWidget(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_ADD_PAGE,item);
        //End Allen
    }
    
    public Long getNextPageId(Long currPageId) {
        int index = mPageOrderList.indexOf(currPageId);
        if (index == mPageOrderList.size() - 1) {
            return END_PAGE;
        }
        else {
            index = index + 1;
            return mPageOrderList.get(index);
        }
    }

    public Long getPrevPageId(Long currPageId) {
        int index = mPageOrderList.indexOf(currPageId);
        if (index == 0 || index == -1) {
            return START_PAGE;
        }
        else {
            index = index - 1;
            return mPageOrderList.get(index);
        }
    }

    public void clearPageOrder() {
        if (mPageOrderList != null) {
            mPageOrderList.clear();
        }
    }

    public List<Long> getBookmarks() {
        return mBookmarksList;
    }

    public void setPageSize(int size) {
        mPageSize = size;
    }

    public int getPageSize() {
        return mPageSize;
    }

    //begin darwin
    public String getBookPath() {
        File dir = new File(MetaData.DATA_DIR);
        dir.mkdir();
        File bookDir = new File(MetaData.DATA_DIR, Long.toString(mCreatedTime));
        bookDir.mkdir();
        return bookDir.getPath();
    }
    //end  darwin
    
    public void setBookColor(int color) {
        mColor = color;
    }

    public int getBookColor() {
        return mColor;
    }

    public Long getImportedTime() {
        return mImportedTime;
    }

    @Override
    public void itemSave(AsusFormatWriter afw) throws IOException {
        afw.writeByteArray(AsusFormat.SNF_BKPROP_BEGIN, null, 0, 0);
        afw.writeLong(AsusFormat.SNF_BKPROP_ID, mCreatedTime);
        afw.writeString(AsusFormat.SNF_BKPROP_TITLE, mTitle);
        afw.writeInt(AsusFormat.SNF_BKPROP_LOCKED, mIsLocked ? 1 : 0);
        afw.writeInt(AsusFormat.SNF_BKPROP_PGSIZE, mPageSize);
        afw.writeInt(AsusFormat.SNF_BKPROP_COLOR, mColor);
        afw.writeInt(AsusFormat.SNF_BKPROP_STYLE, mGridLine);
        afw.writeInt(AsusFormat.SNF_BKPROP_TYPE, mIsPhoneMemo ? 1 : 0);
        long[] pageOrder = new long[mSelectedItemsList.size()];
        int count = 0;
        for (SimplePageInfo item : mSelectedItemsList) {
            pageOrder[count] = item.pageId;
            count++;
        }
        ArrayList<Long> bookmarkList = new ArrayList<Long>();
        String selection = "owner = " + mCreatedTime + " and is_bookmark = 1";

        Cursor cursor = mContentResolver.query(MetaData.PageTable.uri, null, selection, null, null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            bookmarkList.add(cursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE));
            cursor.moveToNext();
        }
        cursor.close();
        long[] bookmarkArray = new long[bookmarkList.size()];
        for (int i = 0; i < bookmarkList.size(); ++i) {
            bookmarkArray[i] = bookmarkList.get(i);
        }
        bookmarkList.clear();
        afw.writeLongArray(AsusFormat.SNF_BKPROP_PGBOOKMARK, bookmarkArray, 0, bookmarkArray.length);
        afw.writeLongArray(AsusFormat.SNF_BKPROP_PGORDER, pageOrder, 0, pageOrder.length);
        afw.writeInt(AsusFormat.SNF_BKPROP_TEMPLATE_TYPE, mTemplateType);
        afw.writeInt(AsusFormat.SNF_BKPROP_INDEX_LANGUAGE, mIndexLanguage);//Allen
        //darwin Begin
        afw.writeInt(AsusFormat.SNF_BKPROP_INDEX_COVER, mCoverIndex);//darwin
        //end darwin
        afw.writeByteArray(AsusFormat.SNF_BKPROP_END, null, 0, 0);
    }

    @Override
    public void itemLoad(AsusFormatReader afr) throws IOException {
        AsusFormatReader.Item item = null;
        for (item = afr.readItem(); item != null; item = afr.readItem()) {
            switch (item.getId()) {
                case AsusFormat.SNF_BKPROP_ID:
                    mImportedTime = item.getLongValue();
                    break;
                case AsusFormat.SNF_BKPROP_TITLE:
                    mTitle = item.getStringValue();
                    break;
                case AsusFormat.SNF_BKPROP_LOCKED:
                    mIsLocked = (item.getIntValue() == 1) ? true : false;
                    break;
                case AsusFormat.SNF_BKPROP_PGSIZE:
                    mPageSize = item.getIntValue();
                    break;
                case AsusFormat.SNF_BKPROP_COLOR:
                    mColor = item.getIntValue();
                    break;
                case AsusFormat.SNF_BKPROP_STYLE:
                    mGridLine = item.getIntValue();
                    break;
                case AsusFormat.SNF_BKPROP_TYPE:
                    mIsPhoneMemo = (item.getIntValue() == 1) ? true : false;
                    break;
                case AsusFormat.SNF_BKPROP_PGORDER: {
                    long[] list = item.getLongArray();
                    mPageOrderList = new ArrayList<Long>(list.length);
                    for (long id : list) {
                        mPageOrderList.add(Long.valueOf(id));
                    }
                }
                    break;
                case AsusFormat.SNF_BKPROP_PGBOOKMARK: {
                    long[] list = item.getLongArray();
                    mBookmarksList.clear();
                    for (long id : list) {
                        mBookmarksList.add(Long.valueOf(id));
                    }
                }
                    break;
                case AsusFormat.SNF_BKPROP_TEMPLATE_TYPE: {
                	mTemplateType = item.getIntValue();
                }
                break;//RICHARD
                case AsusFormat.SNF_BKPROP_INDEX_LANGUAGE: {//Allen
                	mIndexLanguage = item.getIntValue();
                }
                break;//RICHARD
                case AsusFormat.SNF_BKPROP_INDEX_COVER: {//darwin
                	mCoverIndex = item.getIntValue();
                }

                	break;
                case AsusFormat.SNF_BKPROP_END:
                    return;
                default:
            }
        }
    }

    public void setFontSize(int size) {
        if (size == MetaData.BOOK_FONT_BIG || size == MetaData.BOOK_FONT_NORMAL || size == MetaData.BOOK_FONT_SMALL) {
            mFontSize = size;
            save();
        }
    }
    
    // BEGIN: Better
    public void setFontSizeNoUpdateDB(int size) {
        if (size == MetaData.BOOK_FONT_BIG || size == MetaData.BOOK_FONT_NORMAL || size == MetaData.BOOK_FONT_SMALL) {
            mFontSize = size;
        }
    }
    // END: Better

    public int getFontSize() {
        return mFontSize;
    }
    
    public int getCoverIndex()
    {
    	return mCoverIndex;//Siupo
    }
    
    public void setCoverIndex(int index)
    {
    	mCoverIndex = index;
    }
    
    public long getCoverModifyTime()
    {
    	return mCoverModifyTime;
    }
    
    public void setCoverModifyTime(long time)
    {
    	mCoverModifyTime = time;
    }
}
