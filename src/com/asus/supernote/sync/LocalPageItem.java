// BEGIN: Better

package com.asus.supernote.sync;

import com.asus.supernote.data.MetaData;

import android.database.Cursor;

public class LocalPageItem {
	private long mPageId;
	private long mCurModifiedTime;
	private long mLastSyncModifiedTime;

	private boolean mIsBookmark;
	private boolean mIsDeleted;
	private long mCurOwnerBookId;
	private long mLastSyncOwnerBookId;

	public LocalPageItem() {
		mPageId = -1;
		mCurModifiedTime = -1;
		mLastSyncModifiedTime = -1;

		mIsBookmark = false;
		mIsDeleted = false;
		mCurOwnerBookId = -1;
		mLastSyncOwnerBookId = -1;
	}

	public LocalPageItem(LocalPageItem item) {
		mPageId = item.mPageId;
		mCurModifiedTime = item.mCurModifiedTime;
		mLastSyncModifiedTime = item.mLastSyncModifiedTime;

		mIsBookmark = item.mIsBookmark;
		mIsDeleted = item.mIsDeleted;
		mCurOwnerBookId = item.mCurOwnerBookId;
		mLastSyncOwnerBookId = item.mLastSyncOwnerBookId;
	}

	public void load(Cursor cursor) {
		mPageId = cursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE);
		mCurModifiedTime = cursor
				.getLong(MetaData.PageTable.INDEX_MODIFIED_DATE);
		int culid = cursor
				.getColumnIndex(MetaData.PageTable.LASTSYNC_MODIFYTIME);
		if (culid != -1) {
			mLastSyncModifiedTime = cursor.getLong(culid);
		}
		culid = cursor.getColumnIndex(MetaData.PageTable.IS_BOOKMARK);
		if (culid != -1) {
			mIsBookmark = cursor.getInt(culid) > 0 ? true : false;
		}
		culid = cursor.getColumnIndex(MetaData.PageTable.IS_DELETED);
		if (culid != -1) {
			mIsDeleted = cursor.getInt(culid) > 0 ? true : false;
		}
		mCurOwnerBookId = cursor.getLong(MetaData.PageTable.INDEX_OWNER);
		culid = cursor.getColumnIndex(MetaData.PageTable.LASTSYNC_OWNER);
		if (culid != -1) {
			mLastSyncOwnerBookId = cursor.getLong(culid);
		}
	}

	public long getPageId() {
		return mPageId;
	}

	public void setPageId(long pageId) {
		mPageId = pageId;
	}

	public long getCurModifiedTime() {
		return mCurModifiedTime;
	}

	public void setCurModifiedTime(long curModifiedTime) {
		mCurModifiedTime = curModifiedTime;
	}

	public long getLastSyncModifiedTime() {
		return mLastSyncModifiedTime;
	}

	public void setLastSyncModifiedTime(long lastSyncModifiedTime) {
		mLastSyncModifiedTime = lastSyncModifiedTime;
	}

	public boolean isBookmark() {
		return mIsBookmark;
	}

	public void setBookmark(boolean isBookmark) {
		mIsBookmark = isBookmark;
	}

	public boolean isDeleted() {
		return mIsDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		mIsDeleted = isDeleted;
	}

	public long getCurOwnerBookId() {
		return mCurOwnerBookId;
	}

	public void setCurOwnerBookId(long curOwnerBookId) {
		mCurOwnerBookId = curOwnerBookId;
	}

	public long getLastSyncOwnerBookId() {
		return mLastSyncOwnerBookId;
	}

	public void setLastSyncOwnerBookId(long lastSyncOwnerBookId) {
		mLastSyncOwnerBookId = lastSyncOwnerBookId;
	}
	//Begin Darwin_Yu@asus.com
	private String mTargetSha = "";
	private String mAttachmentName = "";
	private int mIsLocalOrServerModify = -1;//-1 -- default  0 -- Local modify 1 -- Server modify 
	public String getSha() 
	{
		return mTargetSha;
	}

	public void setSha(String sha) 
	{
		mTargetSha = sha;
	}
	public String getAttachmentName() 
	{
		return mAttachmentName;
	}

	public void setAttachmentName(String name) 
	{
		mAttachmentName = name;
	}
	
	public int getIsLocalOrServerModify()
	{
		return mIsLocalOrServerModify;
	}
	
	public void setIsLocalOrServerModify(int iModify)
	{
		mIsLocalOrServerModify = iModify;
	}
	//End   Darwin_Yu@asus.com
}

// END: Better
