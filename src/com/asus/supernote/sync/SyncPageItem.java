// BEGIN: Better

package com.asus.supernote.sync;

import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;

public class SyncPageItem {
	private String mVersion = "";
	private long mPageId;
	private String mSha;
	private long mLastSyncModifiedTime;
	private boolean mIsDeleted;
	private long mLastSyncOwnerBookId;
	private String mNotebookTitle;
	private boolean mIsBookmark;
	private int mNotebookBakColor;
	private int mNotebookGridLine;
	private boolean mIsNotebookPhoneMemo;
	
	//darwin
	private int mNotebooktemplate;
	//darwin
	
	//BEGIN: RICHARD
	private int mNotebookIndexLanguage;
	//END: RICHARD

	public SyncPageItem() {
		mPageId = -1;
		mSha = "";
		mLastSyncModifiedTime = -1;
		mIsDeleted = false;
		mLastSyncOwnerBookId = -1;
		mNotebookTitle = "";
		mIsBookmark = false;
		mNotebookBakColor = MetaData.BOOK_COLOR_WHITE;
		mNotebookGridLine = MetaData.BOOK_GRID_LINE;
		mIsNotebookPhoneMemo = false;
		
		//darwin
		mNotebooktemplate = MetaData.Template_type_normal;
		//darwin
		
		//BEGIN: RICHARD
		mNotebookIndexLanguage = NoteBook.WAIT_TO_SET_INDEX_LANGUAGE;
		//END: RICHARD
	}

	public SyncPageItem(SyncPageItem item) {
		mPageId = item.mPageId;
		mSha = item.mSha;
		mLastSyncModifiedTime = item.mLastSyncModifiedTime;
		mIsDeleted = item.mIsDeleted;
		mLastSyncOwnerBookId = item.mLastSyncOwnerBookId;
		mNotebookTitle = item.mNotebookTitle;
		mIsBookmark = item.mIsBookmark;
		mNotebookBakColor = item.mNotebookBakColor;
		mNotebookGridLine = item.mNotebookGridLine;
		mIsNotebookPhoneMemo = item.mIsNotebookPhoneMemo;
		//darwin
		mNotebooktemplate = item.mNotebooktemplate;
		//darwin
		
		//BEGIN: RICHARD
		mNotebookIndexLanguage = item.mNotebookIndexLanguage;
		//END: RICHARD
	}
	
	public String getVersion() {
		return mVersion;
	}
	
	public void setVersion(String version) {
		mVersion = version;
	}

	public long getPageId() {
		return mPageId;
	}

	public void setPageId(long pageId) {
		mPageId = pageId;
	}

	public String getSha() {
		return mSha;
	}

	public void setSha(String sha) {
		mSha = sha;
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

	public long getLastSyncOwnerBookId() {
		return mLastSyncOwnerBookId;
	}

	public void setLastSyncOwnerBookId(long lastSyncOwnerBookId) {
		mLastSyncOwnerBookId = lastSyncOwnerBookId;
	}

	public String getNotebookTitle() {
		return mNotebookTitle;
	}

	public void setNotebookTitle(String title) {
		mNotebookTitle = title;
	}

	public boolean isBookmark() {
		return mIsBookmark;
	}

	public void setBookmark(boolean isBookmark) {
		mIsBookmark = isBookmark;
	}

	public int getNotebookBakColor() {
		return mNotebookBakColor;
	}

	public void setNotebookBakColor(int color) {
		mNotebookBakColor = color;
	}

	public int getNotebookGridLine() {
		return mNotebookGridLine;
	}

	public void setNotebookGridLine(int gridLine) {
		mNotebookGridLine = gridLine;
	}

	//darwin
	public void setNotebookTemplate(int template) {
		mNotebooktemplate = template;
	}
	
	public int getNotebookTemplate() {
		return mNotebooktemplate ;
	}
	//darwin
	
	//BEGIN: RICHARD
	public void setNotebookIndexLanguage(int language) {
		mNotebookIndexLanguage = language;
	}
	
	public int getNotebookIndexLanguage() {
		return mNotebookIndexLanguage;
	}
	//END: RICHARD
	
	public boolean isNotebookPhoneMemo() {
		return mIsNotebookPhoneMemo;
	}

	public void setNotebookPhoneMemo(boolean isPhoneMemo) {
		mIsNotebookPhoneMemo = isPhoneMemo;
	}
}

// END: Better
