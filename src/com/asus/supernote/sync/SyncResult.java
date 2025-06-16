package com.asus.supernote.sync;

public class SyncResult {
	public static final int DOWNLOAD_NEW_PAGE 		= 0;
	public static final int UPLOAD_NEW_PAGE 		= 1;
	public static final int MODIFY_CONFLICT 		= 2;
	public static final int DELETE_LOCAL_PAGE 		= 3;
	public static final int DELETE_REMOTE_PAGE 		= 4;
	public static final int UPLOAD_FAILED 			= 5;
	public static final int DOWNLOAD_FAILED 		= 6;
	public static final int SET_PAGE_ATTR			= 7;
	public static final int PACK_FAILED 			= 8;
	public static final int UNPACK_FAILED 			= 9;
	public static final int SET_PAGE_ATTR_FAILED 	= 10;
	
	private long mLocalPageId;
	private long mRemotePageId;
	private int mStatus;
	private String mMessage;
	
	public SyncResult(long localPageId, long remotePageId, int status) {
		mLocalPageId = localPageId;
		mRemotePageId = remotePageId;
		mStatus = status;
		mMessage = "";
	}
	
	public long getLocalPageId() {
		return mLocalPageId;
	}
	
	public void setLocalPageId(long pageId) {
		mLocalPageId = pageId;
	}
	
	public long getRemotePageId() {
		return mRemotePageId;
	}
	
	public void setRemotePageId(long pageId) {
		mRemotePageId = pageId;
	}
	
	public int getStatus() {
		return mStatus;
	}
	
	public void setStatus(int status) {
		mStatus = status;
	}
	
	public String getMessage() {
		return mMessage;
	}
	
	public void setMessage(String message) {
		mMessage = message;
	}
}
