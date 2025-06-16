package com.asus.supernote.sync;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;

import com.asus.supernote.EncryptAES;
import com.asus.supernote.R;
import com.asus.supernote.WebStorageException;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.ga.GACollector;
import com.asus.supernote.picker.NoteBookPickerActivity;
import com.asus.supernote.picker.PickerActivity;
import com.asus.supernote.ui.CoverHelper;
import com.asus.supernote.widget.InitSyncFilesInfoHelper;
import com.asus.supernote.widget.InitSyncFilesInfoHelper.InitSyncFilesInfoAsyncTask;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Process;
import android.util.Log;

public class SyncFilesWorkTask extends AsyncTask<String, SyncStatus, Integer> {
	public static final String TAG = "SyncFilesWorkTask";
	private boolean mIsTaskRunning = false;

	private ContentResolver mcontentResolver;
	private SyncPageCompareFun mPageCompareFun;
	private SyncBookCompareFun comparefun;
	private static CountDownClass mcountdownclass = null;
	private String booksha = null;
	private String oldbooksha = null;
	private Context mContext;
	private SyncTableInfo mTableInfo = new SyncTableInfo();
	
	public static long mSyncUserId = 0;

	//begin darwin
	private Activity mActivity = null;
	//end   darwin
	
	//BEGIN: RICHARD
	public static LinkedList<Long> mNeedReIndexPageIDList = new LinkedList<Long>();
	//END: RICHARD
	
	private String usedSpace = null; //smilefish
	
	private int lastProcess = 0;//emmanual
	
	//begin darwin
	public SyncFilesWorkTask(Context context,Activity activity) {
		//end   darwin
		mContext = context;
		mcontentResolver = context.getContentResolver();
		comparefun = new SyncBookCompareFun(mcontentResolver, context);
		mPageCompareFun = new SyncPageCompareFun(this, mcontentResolver, context);
		mcountdownclass = CountDownClass.getInstance(context);
		//begin darwin
		mActivity = activity;
		//end   darwin
		
		//emmanual
		if(MetaData.IS_GA_ON)
		{
			GACollector gaCollector = new GACollector(mContext);
			gaCollector.syncCount();
		}
	}

	public boolean isTaskRunning() {
		return mIsTaskRunning;
	}

	@Override
	protected void onCancelled()
	{
		super.onCancelled();
		mIsTaskRunning = false;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		mIsTaskRunning = true;
		mSyncUserId = MetaData.CurUserAccount;
		MetaData.Sync_Result = -1;
		java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance();
		MetaData.SyncSucessTime = df.format(Calendar.getInstance().getTime());
		mcountdownclass.FunBeforeTask();
		getSha();

		MetaData.SyncCurrentPage = 0;
		Cursor cursorCount = mContext.getContentResolver().query(
				MetaData.PageTable.uri, null, "userAccount = ?",
				new String[] { Long.toString(mSyncUserId) }, null);
		MetaData.SyncTotalPage = cursorCount.getCount();
		cursorCount.close();
		MetaData.SyncFailedPageCount = MetaData.SyncTotalPage;
		MetaData.IsFileVersionCompatible = true;
		MetaData.NotSupportedVersionList.clear();
		MetaData.SyncTotalPage += 1;
		MetaData.lockBookIdList.clear();
		MetaData.SyncExceptionList.clear();
		
		// BEGIN: Better
		SharedPreferences pref = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, 
				Context.MODE_MULTI_PROCESS);
		MetaData.SyncExceptionPadBookId = pref.getLong(MetaData.DefaultPadId, -1);
		MetaData.SyncExceptionPhoneBookId = pref.getLong(MetaData.DefaultPhoneId, -1);
		// END: Better
		
		//BEGIN: RICHARD
		mNeedReIndexPageIDList.clear();
		//END: RICHARD
}

	private void getSha()
	{
		Cursor cur = mContext.getContentResolver().query(MetaData.WebAccountTable.uri, null, "account_id = ?", new String[]{Long.toString(mSyncUserId)}, null);
		if(cur.getCount() > 0)
		{
			cur.moveToFirst();
			booksha = cur.getString(MetaData.WebAccountTable.INDEX_SHA);	
			oldbooksha = booksha;
		}
		cur.close();
	}
	private void saveSha()
	{
		ContentValues cvs = new ContentValues();
		cvs.put(MetaData.WebAccountTable.ACCOUNT_SHA, booksha);
		mContext.getContentResolver().update(MetaData.WebAccountTable.uri, cvs, "account_id = ?", new String[]{Long.toString(mSyncUserId)});
	}
	
	// BEGIN: Better
	private int sync() throws Exception {
		int success = -1;
		
		boolean needDataMigrating = false;
		
		if (MetaData.IS_ENABLE_WEBSTORAGE_DATA_MIGRATING) {
			File file = new File(MetaData.USER_TO_UPGRAGE_LIST_DIR, Long.toString(mSyncUserId));
			needDataMigrating = file.exists();
		}
		
		boolean isFileExist = false;
		String version = null;
		if (MetaData.webStorage.isFileExistOnEeeStorage(MetaData.SYNC_TABLE_INFO_FILE_NAME)) {
			mTableInfo.downloadTableInfoAttribute();
			version = mTableInfo.getVersion();
			isFileExist = true;
		}
		if (isCancelled()) {
			return success;
		}
		if (!isFileExist ||  (version == null) || SyncHelper.isVersionSupported(version)) {
			Log.v("wendy", " before CompareAndSyncPageList ====");
			
			if (!needDataMigrating && isFileExist && ((version == null) 
					|| (!version.equalsIgnoreCase(MetaData.SYNC_CURRENT_VERSION)))) { // Better
				mTableInfo.downloadTableInfo();
				try {
					mTableInfo.uploadTableInfo(MetaData.DATA_DIR
							+ MetaData.SYNC_TEMP_DOWNLOAD_DIR
							+ MetaData.SYNC_TABLE_INFO_FILE_NAME);
				} catch (WebStorageException webex) {
					boolean throwit = true;
					if (webex.getErrorKind() == WebStorageException.UPLOAD_INIT_SHAVALUE_NOT_MATCH) {
						String fileSha = mTableInfo.getDownloadedTableInfoFileSha();
						if ((fileSha != null) && (!fileSha.isEmpty())) {
							mTableInfo.setTableInfoSha(fileSha);
							mTableInfo.uploadTableInfo(MetaData.DATA_DIR
									+ MetaData.SYNC_TEMP_DOWNLOAD_DIR
									+ MetaData.SYNC_TABLE_INFO_FILE_NAME);
							throwit = false;
						}
					}
					if (throwit) {
						throw webex;
					}
				}
			}

			if (isCancelled()) {
				return success;
			}
			
			//begin darwin
			MetaData.THIS_SYNC_TIME = MetaData.webStorage.getEndTime();
			MetaData.THIS_SYNC_TIME = formatString( MetaData.THIS_SYNC_TIME );
			//end   darwin
			mPageCompareFun.CompareAndSyncPageList(this, MetaData.SYNC_CURRENT_VERSION);
			updateUI();
			usedSpace = MetaData.webStorage.getMemberUsedCapacity();//smilefish fix bug 341862
			if (isCancelled()) {
				return success;
			}
			booksha = comparefun.CompareAndSyncBookList(this,booksha, mPageCompareFun);
			if ((MetaData.SyncFailedPageCount <= 0) && (MetaData.IsFileVersionCompatible || needDataMigrating)) {
				success = -3;
				if (MetaData.IS_ENABLE_WEBSTORAGE_DATA_MIGRATING) {
					if (needDataMigrating && MetaData.IsFileVersionCompatible && (booksha == null)) {
						mTableInfo.downloadTableInfo();
						try {
							mTableInfo.uploadTableInfo(MetaData.DATA_DIR
									+ MetaData.SYNC_TEMP_DOWNLOAD_DIR
									+ MetaData.SYNC_TABLE_INFO_FILE_NAME);
						} catch (WebStorageException webex) {
							boolean throwit = true;
							if (webex.getErrorKind() == WebStorageException.UPLOAD_INIT_SHAVALUE_NOT_MATCH) {
								String fileSha = mTableInfo.getDownloadedTableInfoFileSha();
								if ((fileSha != null) && (!fileSha.isEmpty())) {
									mTableInfo.setTableInfoSha(fileSha);
									mTableInfo.uploadTableInfo(MetaData.DATA_DIR
											+ MetaData.SYNC_TEMP_DOWNLOAD_DIR
											+ MetaData.SYNC_TABLE_INFO_FILE_NAME);
									throwit = false;
								}
							}
							if (throwit) {
								throw webex;
							}
						}
					}
				}
			}
		} else {
			if (!needDataMigrating) {
				MetaData.IsFileVersionCompatible = false;
				if (!MetaData.NotSupportedVersionList.contains(version)) {
					MetaData.NotSupportedVersionList.add(version);
				}
			} else {
				success = -3;
			}
		}
		
		return success;
	}
	
	private int tryLoginAndSync() throws Exception {
		int success = -1;
		
		// BEGIN: Shane_Wang 2012-10-30
		if(NoteBookPickerActivity.getIsLoginTaskRunning()) {
			return success;
		}
		// END: Shane_Wang 2012-10-30
		final SharedPreferences mPreference = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		//shaun
		String account = mPreference.getString(mContext.getResources().getString(R.string.pref_AsusAccount), null);
		String password = mPreference.getString(mContext.getResources().getString(R.string.pref_AsusPassword), null);//shaun
		String RAsusKey =  mContext.getResources().getString(R.string.pref_AsusKey);
		String RAsusIV =  mContext.getResources().getString(R.string.pref_AsusIV);
		String keyStr = mPreference.getString(RAsusKey, "");
		String ivStr = mPreference.getString(RAsusIV, "");
		account = EncryptAES.decrypt(keyStr, ivStr, account);
		password = EncryptAES.decrypt(keyStr, ivStr, password);
		boolean useNewFolder = false;
		if (MetaData.IS_ENABLE_WEBSTORAGE_DATA_MIGRATING) {
			File file = new File(MetaData.USER_TO_UPGRAGE_LIST_DIR, Long.toString(mSyncUserId));
			useNewFolder = !file.exists();
		}
		if (MetaData.webStorage.init(account, password, useNewFolder)) {
			if (isCancelled()) {
				return success;
			}
			
			success = sync();
		}
		return success;
	}
	
	private void showLoginDialog() {
		if(mActivity instanceof NoteBookPickerActivity)
		{
			((NoteBookPickerActivity)mActivity).showLoginDialog(NoteBookPickerActivity.PICK_OTHERS, "NoteBookPickerActivity");
		}
	}
	// END: Better
	
	@Override
	protected Integer doInBackground(String... params) {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		lastProcess = 0;
		updateUI();
		int success = -1;
		
		if (isCancelled()) {
			return success;
		}
		if(!InitSyncFilesInfoHelper.isSyncFilesInfoInited()){//noah
			InitSyncFilesInfoAsyncTask task = new InitSyncFilesInfoAsyncTask();
			task.initAllBookSyncInfo();//就放在SyncFilesWorkTask的线程中去完成
			publishStatus(new SyncStatus(0, SyncStatus.SYNC_INIT_SYNCFILESINOF_FINISH));
		}
		Resources res = mContext.getResources();
		SharedPreferences pref = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, 
				Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor prefEditor = pref.edit();
		if (MetaData.IS_ENABLE_WEBSTORAGE_DATA_MIGRATING) {
			MetaData.SyncUpgradedFirstTime = pref.getBoolean(res.getString(R.string.pref_sync_upgrade_first_time), false);
			if (MetaData.SyncUpgradedFirstTime) {
				MetaData.SyncLocalUntreatedPageList.clear();
			}
		}

		Log.v("wendy", "start sync files ====");
		try {
			success = sync();
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.TOKEN_ERROR:
			case WebStorageException.LOGIN_ERROR: {
				Log.v(TAG, "Login error, try to login...");
				try {
					success = tryLoginAndSync();
				} catch (WebStorageException ex) {
					Log.v(TAG, "Exception: " + ex.toString()
							+ " occurred, Message: " + ex.getMessage());
					if ((ex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
							|| (ex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
						mHandler.sendEmptyMessage(SHOW_LOGIN_DIALOG); //smilefish fix bug 452874
					}
				} catch (Exception e) {
					Log.v(TAG, "Exception: " + e.toString()
							+ " occurred, Message: " + e.getMessage());
				} catch (OutOfMemoryError err) {
					Log.v(TAG, "Out Of Memory");
				}
			}
				break;
			case WebStorageException.FILE_NOT_EXIST: {
				Log.v(TAG, "Web Exception: File not exist");
			}
				break;
			case WebStorageException.NETWORK_ERROR: {
				Log.v(TAG, "Web Exception: Network error");
			}
				break;
			case WebStorageException.UPLOAD_SHA_ERROR:
			case WebStorageException.UPLOAD_INIT_SHAVALUE_NOT_MATCH: {
				Log.v(TAG, "Web Exception: SHA error");
			}
				break;
			case WebStorageException.USER_ACCOUNT_FROZEN_OR_CLOSED:
			case WebStorageException.USER_ACCOUNT_SPACE_FULL: {
				Log.v(TAG, "Web Exception: User account error");
			}
				break;
			case WebStorageException.TIMEOUT_ERROR: {
				Log.v(TAG, "Web Exception: Timeout error");
			}
				break;
			case WebStorageException.OTHER_ERROR: {
				Log.v(TAG, "Web Exception: Other error");
			}
				break;
			default: {
				Log.v(TAG, webex.toString() + ", " + webex.getMessage());
			}
				break;
			}
		} catch (IOException ioex) {
			Log.v(TAG, "IOException occurred, Message: " + ioex.getMessage());
		} catch (Exception ex) {
			Log.v(TAG, "Exception: " + ex.toString() + " occurred, Message: "
					+ ex.getMessage());
		} catch (OutOfMemoryError err) {
			Log.v(TAG, "Out Of Memory");
		}
		
		if (MetaData.IS_ENABLE_WEBSTORAGE_DATA_MIGRATING) {
			if (success == -3) {
				File file = new File(MetaData.USER_TO_UPGRAGE_LIST_DIR, Long.toString(mSyncUserId));
				if (file.exists()) {
					file.delete();
					MetaData.SyncUpgradedFirstTime = true;
					prefEditor.putBoolean(res.getString(R.string.pref_sync_upgrade_first_time), true);
					prefEditor.commit();
					MetaData.SyncLocalUntreatedPageList.clear();
					try {
						MetaData.SyncCurrentPage = 0;
						MetaData.IsFileVersionCompatible = true;
						MetaData.NotSupportedVersionList.clear();
						MetaData.SyncExceptionList.clear();
						success = tryLoginAndSync();
					} catch (WebStorageException ex) {
						Log.v(TAG, "Exception: " + ex.toString()
								+ " occurred, Message: " + ex.getMessage());
						if ((ex.getErrorKind() == WebStorageException.LOGIN_ERROR) 
								|| (ex.getErrorKind() == WebStorageException.TOKEN_ERROR)) {
							mHandler.sendEmptyMessage(SHOW_LOGIN_DIALOG); 
						}
					} catch (Exception e) {
						Log.v(TAG, "Exception: " + e.toString()
								+ " occurred, Message: " + e.getMessage());
					} catch (OutOfMemoryError err) {
						Log.v(TAG, "Out Of Memory");
					}
					MetaData.SyncLocalUntreatedPageList.clear();
					if (success == -3) {
						MetaData.SyncUpgradedFirstTime = false;
						prefEditor.putBoolean(res.getString(R.string.pref_sync_upgrade_first_time), false);
						prefEditor.commit();
					}
				}
			}
			
			if (MetaData.SyncUpgradedFirstTime) {
				MetaData.SyncUpgradedFirstTime = false;
				MetaData.SyncLocalUntreatedPageList.clear();
				if (success == -3) {
					prefEditor.putBoolean(res.getString(R.string.pref_sync_upgrade_first_time), false);
					prefEditor.commit();
				}
			}
		}
		
		return success;
	}
	
	public void callUIThreadSavethumb(long pageId,long bookId)
	{
		SyncStatus ss = new SyncStatus(pageId,bookId);
		publishProgress(ss);
	}
	public void publishStatus(SyncStatus... values) {
		publishProgress(values);
	}
	
	public void updateUI() {
	    try {
			Intent intent= new Intent(MetaData.SYNC_UPDATE_UI);
			mContext.sendBroadcast(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onProgressUpdate(SyncStatus... values) {
		int rate = 0;
		if ((values != null) && (values[0] != null)) {			
			if(values[0].getStatus() == SyncStatus.SYNC_FOR_SAVE_THUMB)
			{
				BookCase bookCase = BookCase.getInstance(mContext);
				NoteBook noteBook = bookCase.getNoteBook(values[0].getBookId());
				if (noteBook != null) {
					NotePage notePageTemp = noteBook.getNotePage(values[0].getPageId());
					if (notePageTemp != null) {
						genThumb(notePageTemp,(noteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE));
						if(MetaData.IS_ENABLE_WIDGET_THUMBNAIL)
							genWidgetThumb(notePageTemp,(noteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE));
						if(MethodUtils.isEnableAirview(mContext)){
							genAirViewThumb(notePageTemp,(noteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE));//Allen
							}
						}
				}
				return;
			}
			rate = (int)(((double)MetaData.SyncCurrentPage / MetaData.SyncTotalPage)*100);
			if(rate == 100)
			{
				rate = 98;
			}
		}
		if (rate > lastProcess || rate > 95) {
			updateUI();
			lastProcess = rate;
		}
	}
	
	private void genAirViewThumb(NotePage page, boolean isPhoneSize) {
		Resources res = mContext.getResources();
        int width =0,height = 0;
        
		width = (int)res.getDimension(R.dimen.pageview_airview_width);
		height = (int)res.getDimension(R.dimen.pageview_airview_height);
		
		Bitmap result = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
        page.load(new PageDataLoader(mContext), false, canvas, true, false, false);
        File file = new File(page.getFilePath(), MetaData.AIRVIEW_PREFIX);
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
            result.compress(Bitmap.CompressFormat.PNG, MetaData.THUMBNAIL_QUALITY, bos);
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
	
	private void genWidgetThumb(NotePage page, boolean isPhoneSize) {
		Resources res = mContext.getResources();
		Paint paint = new Paint();
        int targetWidth, targetHeight;
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
		Bitmap result = Bitmap.createBitmap((int)res.getDimension(R.dimen.widget_cover_width),(int)res.getDimension(R.dimen.widget_cover_height), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);

        targetWidth = (int) result.getWidth() ;//(* 0.9);
        targetHeight = (int) result.getHeight() ;//(* 0.85);
        Bitmap content;
        Canvas contentCanvas;
        content = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        content.setDensity(Bitmap.DENSITY_NONE);
        contentCanvas = new Canvas(content);
        page.load(new PageDataLoader(mContext), false, contentCanvas, true, false, false);

        canvas.drawBitmap(content, 0, 0, paint);
        content.recycle();
        content = null;
        File file = new File(page.getFilePath(), MetaData.THUMBNAIL_WIDGET);
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
            result.compress(Bitmap.CompressFormat.PNG, MetaData.THUMBNAIL_QUALITY, bos);
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
	
	//copy from pickeractivity.java
    private void genThumb(NotePage page, boolean isPhoneSize) {
		Bitmap cover = null;
		int color = page.getPageColor();
		int line = page.getPageStyle();
		Resources res = mContext.getResources();
		cover = CoverHelper.getDefaultCoverBitmap(color, line, res);//Allen

		Paint paint = new Paint();
        int targetWidth, targetHeight;
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
		Bitmap result = Bitmap.createBitmap(cover.getWidth(), cover.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(cover, 0, 0, paint);
		cover.recycle();
        cover = null;
        targetWidth = (int) (result.getWidth() * 0.9);
        targetHeight = (int) (result.getHeight() * 0.85);
        Bitmap content;
        Canvas contentCanvas;
        content = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        content.setDensity(Bitmap.DENSITY_NONE);
        contentCanvas = new Canvas(content);
        page.load(new PageDataLoader(mContext), false, contentCanvas, true, false, false);
        float left = res.getDimension(R.dimen.thumb_padding_left);
        float top = res.getDimension(R.dimen.thumb_padding_top);
        canvas.translate(left, top);
        canvas.drawBitmap(content, 0, 0, paint);
        content.recycle();
        content = null;
        File file = new File(page.getFilePath(), MetaData.THUMBNAIL_PREFIX);
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
            result.compress(Bitmap.CompressFormat.PNG, MetaData.THUMBNAIL_QUALITY, bos);
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
	
	@Override
	protected void onCancelled(Integer result) {
		super.onCancelled(result);
		
		MetaData.SyncExceptionList.clear();
		
		postProcess(result);
		
		//Begin Allen for update widget
        if(MetaData.SuperNoteUpdateInfoSet.size()>0){
        	Intent updateIntent = new Intent();
    		updateIntent.setAction(MetaData.ACTION_SUPERNOTE_UPDATE);
    		updateIntent.putExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM, MetaData.SuperNoteUpdateInfoSet);
    		mContext.sendBroadcast(updateIntent);
    		MetaData.SuperNoteUpdateInfoSet.clear();
        }
        //End Allen
	}

	//BEGIN: RICHARD
	private void updateDBIndexStatusInList()
	{
        ContentValues cvUpdate = new ContentValues();
        cvUpdate.put(MetaData.PageTable.IS_INDEXED, MetaData.INDEX_FILE_CREATE_NOT);
		for(long pageID : mNeedReIndexPageIDList)
		{
			mContext.getContentResolver().update(MetaData.PageTable.uri, cvUpdate, MetaData.PageTable.CREATED_DATE + "=" + pageID, null);
		}
	}
	//END: RICHARD
	
	//darwin
	public void changeBookdefaultCoverThumb(NoteBook book,Context context)
    {
    	try {
            Bitmap bitmap = NoteBook.getDefaultNoteBookThumbnail(book.getCoverIndex(), context);
            if (bitmap != null) {
                File file = new File(book.getBookPath(), MetaData.THUMBNAIL_COVER);
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
	//darwin
	
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		
		MetaData.SyncExceptionList.clear();

		updateDBIndexStatusInList();
		mNeedReIndexPageIDList.clear();
		
		
		postProcess(result);
		//begin darwin
		if(comparefun.mSyncBookList_remote != null && comparefun.mSyncBookList_remote.size() != 0)
		{
			PageDataLoader loader = new PageDataLoader(mActivity);
			for(SyncBookItem sbi : comparefun.mSyncBookList_remote)
			{
				long bookid = sbi.getBookId();
				BookCase bookCase = BookCase.getInstance(mContext);
				NoteBook noteBook = bookCase.getNoteBook(bookid);
				if(noteBook != null)
				{
					int num = noteBook.getTotalPageNumber();
					if((noteBook.getCoverIndex() != 0) && (noteBook.getCoverIndex()!=1))
					{
						changeBookdefaultCoverThumb(noteBook,mContext);
					}
					if (num > 0) {
						long pageidtemp = noteBook.getPageOrder(0);
		
						
						NotePage notePageTemp = noteBook.getNotePage(pageidtemp);
						if(mActivity != null)
						{
							if(mActivity instanceof  NoteBookPickerActivity)
					        {
					        	((NoteBookPickerActivity)mActivity).saveBookCoverThumb(loader, false, noteBook,notePageTemp);
					        }
							else if(mActivity instanceof  PickerActivity)
							{
								((PickerActivity)mActivity).saveBookCoverThumbInPageView(loader, false, noteBook,notePageTemp);
								
							}
						}
					}
					else if(num == 0)
					{
						noteBook.changeBookCover();
					}
				}
				
			}
		}

		if(MetaData.Sync_Result == -3)
		{
			putLastSyncTime();
		}
		//end darwin
		
		//Begin Allen for update widget
        if(MetaData.SuperNoteUpdateInfoSet.size()>0){
        	Intent updateIntent = new Intent();
    		updateIntent.setAction(MetaData.ACTION_SUPERNOTE_UPDATE);
    		updateIntent.putExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM, MetaData.SuperNoteUpdateInfoSet);
    		mContext.sendBroadcast(updateIntent);
    		MetaData.SuperNoteUpdateInfoSet.clear();
        }
        //End Allen
		
	}
	
	private void postProcess(Integer result) {
		mcountdownclass.FunAferTask();
		MetaData.Sync_Result = result;
		if (booksha != null && !(booksha.equals(oldbooksha))) {
			saveSha();
		}
		
		boolean haspassword = NoteBookPickerActivity.HasPersonalPassword();
		if (!haspassword) {
			if(MetaData.lockBookIdList.size() > 0)
			{
				Log.v("wendy","has locked book!");
				try {
					final Intent intent = new Intent(mContext, SyncSetprivatePassword.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);	mContext.startActivity(intent);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
		
		updateUI();
		updateUsedSpace(); //Smilefish fix bug 341862
		
		mIsTaskRunning = false;
	}
	
	private void updateUsedSpace(){
        SharedPreferences mPreference = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor mPreferenceEditor = mPreference.edit();
		mPreferenceEditor.putString(mContext.getResources().getString(R.string.pref_Asus_AccountUsed), 
        		usedSpace);
		mPreferenceEditor.commit();
	}
	
	//begin darwin
	public void putLastSyncTime()
	{
		SharedPreferences preference = mContext.getSharedPreferences(
				MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor preferenceEditor = preference.edit();
		preferenceEditor.putString(mContext.getResources().getString(R.string.last_sync_time),MetaData.THIS_SYNC_TIME);
		preferenceEditor.commit();
	}
	
	public String formatString(String string)
	{
		if (string == null) {
			return "";
		}
		
		int len = string.length();
		String outString = "";
		for(int i = 0; i < len; i++)
		{
			if(string.charAt(i) == ' ' ||
			   string.charAt(i) == '-' ||
			   string.charAt(i) == ':')
			{
				continue;
			}
			else
			{
				outString += string.charAt(i);
			}
		}
		return outString+"000";
	}
	//end   darwin
	
	//smilefish fix bug 452874
	private static final int SHOW_LOGIN_DIALOG=1;
	private Handler mHandler =new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SHOW_LOGIN_DIALOG:
				showLoginDialog();
				break;
			default:
				break;
			}
		};
	};
	//end smilefish
}
