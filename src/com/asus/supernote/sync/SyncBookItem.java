package com.asus.supernote.sync;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import com.asus.supernote.PageOrderList;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.picker.LanguageHelper;


public class SyncBookItem {
	public static LanguageHelper sLanguageHelper = new LanguageHelper();

	private long mBookId;
    private String mTitle;
    private boolean mIsLocked;
    private int mPagesize;
    private int mColor;
    private int mGridLine;  
    private boolean mIsPhoneMemo;
    private List<Long> mPageOrderList;
    private List<Long> mBookmarksList;	
	
	private long mCurModifiedTime;
	private long mLastSyncModifiedTime;
	private boolean mIsDeleted;
    
    //add by wendy begin++
    public static final String LASTSYNC_MODIFYTIME = "lastsync_modifytime";
    public static final String IS_DELETED 		   = "is_deleted";
	//
    
    //begin wendy allen++ for template 0706
    public int mTemplate_type ;
    
    public int mIndexCover ;//darwin
    private long mCoverModifiedTime;//darwin
    public boolean mIsChecked = false;//darwin
    
    public long getCoverModifiedTime()
    {
    	return mCoverModifiedTime;
    }
    
    public void setCoverModifiedTime(long index)
    {
    	mCoverModifiedTime = index;
    }
    
    public int getIndexCover()
    {
    	return mIndexCover;
    }
    
    public void setIndexCover(int index)
    {
    	mIndexCover = index;
    }
    public int getTemplate()
    {
    	return mTemplate_type;
    }
    public void setTemplate(int temp)
    {
    	mTemplate_type = temp;
    }
    //end wendy allen++
    
    //Begin Allen
    private int mIndexLanguage = sLanguageHelper.getRecordIndexLaguage();//default
    
	public int getIndexLanguage() {
		return mIndexLanguage;
	}
	public void setIndexLanguage(int mIndexLanguage) {
		this.mIndexLanguage = mIndexLanguage;
	}
	//End Allen
	public SyncBookItem() {
		mBookId = -1;
		mTitle = "";
		mIsLocked = false;
		mPagesize = MetaData.PAGE_SIZE_PAD;
		mColor = MetaData.BOOK_COLOR_WHITE;
		mGridLine = MetaData.BOOK_GRID_LINE;
		mIsPhoneMemo = false;
		mPageOrderList = new LinkedList<Long>();
		mBookmarksList = new ArrayList<Long>();
		mLastSyncModifiedTime = -1;
		mIsDeleted = false;
		//begin wendy allen++ for template 0706
		mTemplate_type = MetaData.Template_type_normal;
		//end wendy allen++
		mIndexCover = 0;
		mCoverModifiedTime = -1;//darwin
		// BEGIN: Better
		mIndexLanguage = sLanguageHelper.getRecordIndexLaguage();
		// END: Better
	}

		public SyncBookItem(SyncBookItem item) {
			mBookId = item.mBookId;
			mTitle = item.mTitle;
			mIsLocked = item.mIsLocked;
			mPagesize = item.mPagesize;
			mColor = item.mColor;
			mGridLine = item.mGridLine;
			mIsPhoneMemo = item.mIsPhoneMemo;
			mPageOrderList = new LinkedList<Long>(item.mPageOrderList);
			mBookmarksList = new ArrayList<Long>(item.mBookmarksList);
			mLastSyncModifiedTime = item.mLastSyncModifiedTime;
			mIsDeleted = item.mIsDeleted;
			mCurModifiedTime = item.mCurModifiedTime;
			//begin wendy allen++ for template 0706
			mTemplate_type = item.mTemplate_type;
			//end wendy allen++
			// BEGIN: Better
			mIndexLanguage = item.mIndexLanguage;
			// END: Better
			mIndexCover = item.mIndexCover;//darwin
			mCoverModifiedTime = item.mCoverModifiedTime;//darwin
		}
	   
	
	public void ModifyBookItem(SyncBookItem item)
	{
		mBookId = item.mBookId;
		mTitle = item.mTitle;
		mIsLocked = item.mIsLocked;
		mPagesize = item.mPagesize;
		mColor = item.mColor;
		mGridLine = item.mGridLine;
		mIsPhoneMemo = item.mIsPhoneMemo;
		mPageOrderList = new LinkedList<Long>(item.mPageOrderList);
		mBookmarksList = new ArrayList<Long>(item.mBookmarksList);
		mLastSyncModifiedTime = item.mLastSyncModifiedTime;
		mIsDeleted = item.mIsDeleted;
		mCurModifiedTime = item.mCurModifiedTime;
		//begin wendy allen++ for template 0706
		mTemplate_type = item.mTemplate_type;
		//end wendy allen++
		mIndexLanguage = item.mIndexLanguage;//allen
		mIndexCover = item.mIndexCover;//darwin
		mCoverModifiedTime = item.mCoverModifiedTime;//darwin
	}
		
	public long getBookId() {
		return mBookId;
	}

	public void setBookId(long bookId) {
		mBookId = bookId;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public boolean isLocked() {
		return mIsLocked;
	}

	public void setLocked(boolean isLocked) {
		mIsLocked = isLocked;
	}

	public int getPageSize() {
		return mPagesize;
	}

	public void setPageSize(int pagesize) {
		mPagesize = pagesize;
	}

	public List<Long> getPageOrderList() {
		return mPageOrderList;
	}
	public void setPageOrderList(List<Long> list)
	{
		mPageOrderList = new ArrayList<Long>(list);
	}

	public List<Long> getBookmarksList() {
		return mBookmarksList;
	}

	public int getColor() {
		return mColor;
	}

	public void setColor(int color) {
		mColor = color;
	}

	public int getGridLine() {
		return mGridLine;
	}

	public void setGridLine(int gridLine) {
		mGridLine = gridLine;
	}

	public Long getCurModifiedTime()
	{
		return mCurModifiedTime ;
	}
	public void setCurModifiedTime(long time)
	{
		mCurModifiedTime = time;
	}
	
	public boolean isPhoneMemo() {
		return mIsPhoneMemo;
	}

	public void setPhoneMemo(boolean isPhoneMemo) {
		mIsPhoneMemo = isPhoneMemo;
	}

	public long getLastSyncModifiedTime() {
		return mLastSyncModifiedTime;
	}

	public void setLastSyncModifiedTime(long lastSyncModifiedTime) {
		mLastSyncModifiedTime = lastSyncModifiedTime;
	}

	public boolean isDeleted() {
		return mIsDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		mIsDeleted = isDeleted;
	}	
	public boolean IsModify(long time)
	{
		return ((time!= 0) && (time != mLastSyncModifiedTime))? true:false;
	}	
	

    public void load( Cursor cursor) {
        mTitle = cursor.getString(MetaData.BookTable.INDEX_TITLE);
        mBookId = cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
        mIsLocked = (cursor.getInt(MetaData.BookTable.INDEX_IS_LOCKED)) > 0 ? true : false;
        mPagesize = cursor.getInt(MetaData.BookTable.INDEX_BOOK_SIZE);
        mColor = cursor.getInt(MetaData.BookTable.INDEX_BOOK_COLOR);
        mGridLine = cursor.getInt(MetaData.BookTable.INDEX_BOOK_GRID);
        byte[] data = cursor.getBlob(MetaData.BookTable.INDEX_PAGE_ORDER);
        mCurModifiedTime = cursor.getLong(MetaData.BookTable.INDEX_MODIFIED_DATE);
        //begin wendy allen++ for template 0706
        mTemplate_type = cursor.getInt(MetaData.BookTable.INDEX_TEMPLATE);
        //end wendy allen++
        mIndexLanguage = cursor.getInt(MetaData.BookTable.INDEX_INDEX_LANGUAGE);//Allen
        mIndexCover = cursor.getInt(MetaData.BookTable.INDEX_INDEX_COVER);//darwin
        mCoverModifiedTime = cursor.getLong(MetaData.BookTable.INDEX_COVER_MODIFYTIME);//darwin
        int columid = cursor.getColumnIndex("lastsync_modifytime");        
        if(columid!= -1)
        {
        	mLastSyncModifiedTime = cursor.getLong(columid);
        	
        }
        columid = cursor.getColumnIndex(MetaData.BookTable.IS_DELETED);        
        if(columid != -1)
        {
        	//Log.v("wendy","get columid!");
        	mIsDeleted = (cursor.getInt(columid)) > 0 ? true : false;
        }    
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
    }
    public void getBookMarklist(ContentResolver contentresolver,Long bookid)
    {
            String selection = "owner = " + mBookId + " and is_bookmark = 1";
    	 if(mBookmarksList==null)
    		 mBookmarksList =  new ArrayList<Long>();

            Cursor cursorpage = contentresolver.query(MetaData.PageTable.uri, null, selection, null, null);
            cursorpage.moveToFirst();
            while (cursorpage.isAfterLast() == false) {
         	mBookmarksList.add(cursorpage.getLong(MetaData.PageTable.INDEX_CREATED_DATE));
                cursorpage.moveToNext();
            }
            cursorpage.close();
    }
}
