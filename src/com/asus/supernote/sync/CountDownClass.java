package com.asus.supernote.sync;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;

public class CountDownClass {

	//begin darwin
	private static Activity mActivity = null;
	//end   darwin
	private static SyncFilesWorkTask mtask = null;
	private static Context mContext = null;
	private static CountDownClass mInstance;
	private FinishCountDown mCountDown = null;
	private int mSyncInterval = 0;
	private boolean mIsImmediate = false;
	private Thread mCountDownThread = null;
	private Thread mStartThread = null;
	private static final int MSG_START_TASK = 0;
	private static final int MSG_START_COUNT_DOWN = 1;
	private static final int MSG_START_COUNT_DOWN_AND_TASK = 2;
	private boolean mCanSync = true;

	public CountDownClass(Context context) {
		mContext = context;
		mInstance = this;
	}

	//begin darwin
	public void SetActivity(Activity activity)
	{
		mActivity = activity;
	}
	//end   darwin
	
	public synchronized static final CountDownClass getInstance(Context context) {
		if (context == null) {
			return null;
		}
		if (mInstance == null) {
			mInstance = new CountDownClass(context);
		}
		return mInstance;
	}
	
	//BEGIN: RICHARD
	public static boolean isTaskRunning()
	{	
		if ((mtask != null) && mtask.isTaskRunning()) {
			return true;
		}
		
		return false;
	}
	//END: RICHARD
	
	public boolean disableSync() {
		mCanSync = false;
		if ((mtask != null) && mtask.isTaskRunning()) {
			return mtask.cancel(true);
		}
		return true;
	}
	
	public void enableSync() {
		mCanSync = true;
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_START_TASK:
				if (mCanSync && (MetaData.CurUserAccount > 0)) {
					if ((mtask == null) || (!mtask.isTaskRunning())) {
						//begin darwin
						mtask = new SyncFilesWorkTask(mContext,mActivity);
						//end   darwin
						mtask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "test");
					}
				}
				break;
			case MSG_START_COUNT_DOWN:
				if (mCountDown != null) {
					mCountDown.cancel();
				}
				
				mCountDown = new FinishCountDown(1000 * 60 * mSyncInterval,
						1000 * 60 * mSyncInterval);
				mCountDown.start();
				break;
			case MSG_START_COUNT_DOWN_AND_TASK:
				if (mCanSync && (MetaData.CurUserAccount > 0)) {
					if (mtask == null || mtask.isTaskRunning() == false) {
						//begin darwin
						mtask = new SyncFilesWorkTask(mContext,mActivity);
						//end   darwin
						mtask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "test");
					}
				}
				if (mCountDown != null) {
					mCountDown.cancel();
				}
				
				mCountDown = new FinishCountDown(1000 * 60 * mSyncInterval,
						1000 * 60 * mSyncInterval);
				mCountDown.start();
				break;
			}
		}
	};

	public void StartTask(boolean isCancelPrevious) {
		if (mCanSync && (MetaData.CurUserAccount > 0)) {
			if (isCancelPrevious) {
				if ((mtask != null) && mtask.isTaskRunning()) {
					mtask.cancel(true);
					if ((mStartThread == null) || !mStartThread.isAlive()) {
						mStartThread = new Thread(new Runnable() {
							
							@Override
							public void run() {
								while ((mtask != null) && mtask.isTaskRunning()) {
									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
									}
								}
	
								mHandler.sendEmptyMessage(MSG_START_TASK);
							}
	
						});
						mStartThread.start();
					}
				} else {
					//begin darwin
					mtask = new SyncFilesWorkTask(mContext,mActivity);
					//end   darwin
					mtask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "test");
				}
			} else {
				if ((mtask == null) || !mtask.isTaskRunning()) {
					//begin darwin
					mtask = new SyncFilesWorkTask(mContext,mActivity);
					//end   darwin
					mtask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "test");
				}
			}
		}
	}
	
	public void StopTask() {
		if ((mtask != null) && mtask.isTaskRunning()) {
			mtask.cancel(true);
		}
	}

	public void StartCountDown(int default_synctime, boolean isImmediate, boolean isCancelPrevious) {
		if (mCountDown != null)
			mCountDown.cancel();
		Log.v("wendy", "sync time ==" + default_synctime);
		if (default_synctime == 0)
			return;
		
		mSyncInterval = default_synctime;
		mIsImmediate = isImmediate;
		
		if (isCancelPrevious) {
			if ((mtask != null) && mtask.isTaskRunning()) {
				mtask.cancel(true);
				if ((mCountDownThread == null) || !mCountDownThread.isAlive()) {
					mCountDownThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							while ((mtask != null) && mtask.isTaskRunning()) {
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
								}
							}
							
							if (mIsImmediate) {
								mHandler.sendEmptyMessage(MSG_START_COUNT_DOWN_AND_TASK);
							} else {
								mHandler.sendEmptyMessage(MSG_START_COUNT_DOWN);
							}
						}
	
					});
					mCountDownThread.start();
				}
			} else {
				if (mIsImmediate) {
					if (mCanSync && (MetaData.CurUserAccount > 0)) {
						if (mtask == null || mtask.isTaskRunning() == false) {
							//begin darwin
							mtask = new SyncFilesWorkTask(mContext,mActivity);
							//end   darwin
							mtask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "test");
						}
					}
				}
				mCountDown = new FinishCountDown(1000 * 60 * default_synctime,
						1000 * 60 * default_synctime);
				mCountDown.start();
			}
		} else {
			if (mIsImmediate) {
				if (mCanSync && (MetaData.CurUserAccount > 0)) {
					if (mtask == null || mtask.isTaskRunning() == false) {
						//begin darwin
						mtask = new SyncFilesWorkTask(mContext,mActivity);
						//end   darwin
						mtask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "test");
					}
				}
			}
			mCountDown = new FinishCountDown(1000 * 60 * default_synctime,
					1000 * 60 * default_synctime);
			mCountDown.start();
		}
	}

	public void FunAferTask() {
		if (mCountDown != null)
			mCountDown.start();
	}

	public void FunBeforeTask() {
		if (mCountDown != null)
			mCountDown.cancel();
	}

	// BEGIN: Better
	public void stopCountDown() {
		if (mCountDown != null) {
			mCountDown.cancel();
			mCountDown = null;
			if ((mtask != null) && (mtask.isTaskRunning())) {
				mtask.cancel(true);
			}
		}
	}

	// END: Better

	// add by wendy test begin ++
	private class FinishCountDown extends CountDownTimer {
		public FinishCountDown(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			SharedPreferences pref = mContext.getSharedPreferences(
					MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
			String syncOnlyWifi = pref.getString(mContext.getResources().getString(R.string.pref_asus_wifi_auto_sync),"true");
			Boolean canSync = true;
			if(syncOnlyWifi.equalsIgnoreCase("true")){
				if(isConnectingWifi(mContext)){
					canSync = true;
				}else{
					canSync = false;
				}
			}
			if (mCanSync && (MetaData.CurUserAccount > 0) && canSync) {
				if (mtask == null || mtask.isTaskRunning() == false) {
					//begin darwin
					mtask = new SyncFilesWorkTask(mContext,mActivity);
					//end   darwin
					mtask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "test");
				}
			}
		}

		@Override
		public void onTick(long millisUntilFinished) {
		}
	}
	
	private boolean isConnectingWifi(Context mContext) {  
	    ConnectivityManager connectivityManager = (ConnectivityManager) mContext  
	            .getSystemService(Context.CONNECTIVITY_SERVICE);  
	    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();  
	    if (activeNetInfo != null  
	            && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {  
	        return true;  
	    }  
	    return false;  
	}
}
