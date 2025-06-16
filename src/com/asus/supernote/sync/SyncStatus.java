package com.asus.supernote.sync;

public class SyncStatus {
	public static final int SYNC_FAILED 	= -1;
	public static final int SYNC_PENDING 	=  0;
	public static final int SYNC_SUCCESS 	=  1;
	public static final int SYNC_INIT_SYNCFILESINOF_FINISH = 10;//noah

	//begin darwin
	public static final int SYNC_FOR_SAVE_THUMB 	=  -2;
	private long mBookId;
	public SyncStatus(long pageId, long bookId) {
		mPageId = pageId;
		mBookId = bookId;
		mStatus = SYNC_FOR_SAVE_THUMB;
	}
	//end   darwin
	private long mPageId;
	private int	mStatus;
	
	public SyncStatus(long pageId, int status) {
		mPageId = pageId;
		mStatus = status;
	}
	
	public long getPageId() {
		return mPageId;
	}
	
	public void setPageId(long pageId) {
		mPageId = pageId;
	}
	
	//begin darwin
	public long getBookId() {
		return mBookId;
	}
	public void setBookId(long bookId) {
		mBookId = bookId;
	}
	//end   darwin
	public int getStatus() {
		return mStatus;
	}
	
	public void setStatus(int status) {
		mStatus = status;
	}
}
