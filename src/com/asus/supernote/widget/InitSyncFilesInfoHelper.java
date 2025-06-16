package com.asus.supernote.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.asus.supernote.R;
import com.asus.supernote.SuperNoteApplication;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;

/**
 * @author noah_zhang
 * @date 2013-9-3
 * @deprecated：同步信息辅助类
 * @remark：第一次运行supernote的时候，需要将同步信息写入数据库中，由于这部分操作比较花时间,这里写一个辅助类,将这些事情放在一个异步任务里去做;
 * 			在用户跟网络数据同步之前,需要保证InitSyncFilesInfoAsyncTask成功运行一次
 */
public class InitSyncFilesInfoHelper {
	public static boolean isSyncFilesInfoInited(){
		Context context = SuperNoteApplication.getContext();
		if(context == null)
			return true;
		SharedPreferences pref = context.getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		String string = context.getResources().getString(R.string.pref_is_sync_files_inited);
        boolean isInited = pref.getBoolean(string, false);
		return isInited;
	}
	
	public static class InitSyncFilesInfoAsyncTask extends AsyncTask<Void, Void, Void>{
		private static final String TAG = "InitSyncFilesInfoAsyncTask";
		private Context mContext;
		private static boolean mIsTaskRunning = false;
		public InitSyncFilesInfoAsyncTask(){
			mContext = SuperNoteApplication.getContext();
		}
		
	    public static boolean isTaskRunning() {
	        return mIsTaskRunning;
	    }
	    
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        mIsTaskRunning = true;
	    }
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mIsTaskRunning = false;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			initAllBookSyncInfo();
			return null;
		}
		
		public void initAllBookSyncInfo(){
			Log.i(TAG, "初始化同步信息开始");
			BookCase bookCase = BookCase.getInstance(mContext);
			for (NoteBook book : bookCase.getBookList()) {
				initBookSyncInfo(book);
			}
			Log.i(TAG, "初始化同步信息结束");
			SharedPreferences pref = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
	        SharedPreferences.Editor editor = pref.edit();
	        editor.putBoolean(mContext.getResources().getString(R.string.pref_is_sync_files_inited), true);
	        editor.commit();
		}
		
		private void initBookSyncInfo(NoteBook book){
			int num = book.getTotalPageNumber();
			if(num > 0)
			{
				PageDataLoader loader = new PageDataLoader(mContext);
				for (int i = 0; i < num; i++) {
					NotePage notePage = book.getNotePage(book.getPageOrder(i));
					if (notePage != null) {
						if (loader.load(notePage)) {
							notePage.updateSyncFilesInfo(loader, true);
							notePage.updateTimestampInfo(loader, true);
						}
					}
				}
			}
		}
		
	}
}
