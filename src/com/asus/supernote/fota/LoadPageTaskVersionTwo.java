package com.asus.supernote.fota;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.picker.PageGridViewAdapter;

public class LoadPageTaskVersionTwo extends AsyncTask<Void, Void, Boolean> {

	private Context mContext = null;
	private NotePage mNotePage = null;
	private PageEditor mPageEditor = null;
	private PageDataLoader mPageDataLoader = null;
	private Boolean mIsOnResume = false;
	private static Boolean mIsTaskRunning = false;//by show
	private boolean mFromAddNew = false;// noah

	//begin noah;7.5
	public LoadPageTaskVersionTwo(Context context, NotePage page, PageEditor pe,Boolean isOnResume) {
		this(context, page, pe, isOnResume, false);
	}
	
	public LoadPageTaskVersionTwo(Context context, NotePage page, PageEditor pe,Boolean isOnResume, boolean fromAddNew) {
		mContext = context;
		mNotePage = page;
		mPageEditor = pe;
		mIsOnResume = isOnResume;
		mFromAddNew = fromAddNew;
	}
	//end noah;7.5
	
	@Override
	protected void onPreExecute() {		
		mIsTaskRunning = true;//by show
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		while(!mPageEditor.beginLoad())
		{
			try
			{
				Thread.sleep(1000);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		mPageEditor.setNotePage(mNotePage);
		mPageDataLoader = new PageDataLoader(mContext);//Richard
		boolean result = mPageDataLoader.load(mNotePage);

		return result;

	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (!result) {
			fileFormatError();
			PageGridViewAdapter.resetPageGridProcessing();
		} else {
			mPageEditor.onLoadPage();
			mPageEditor.loadNoteEditText(mPageDataLoader.getAllNoteItems());
			mPageEditor.loadDoodle(mPageDataLoader.getDoodleItem());
			mPageEditor.cleanEdited(false);
			mPageEditor.endLoad();
			
			Log.i("RICHARD_",this.toString() +"END LOAD");

			if(mIsOnResume)
			{
				if(mContext instanceof EditorActivity)
				{
					try{ //smilefish fix bug 379561
						((EditorActivity) mContext).doAfterResume();
					}catch(Exception e){
						e.printStackTrace();
					}
					
				}
			}
			if(mContext instanceof EditorActivity){//add by noah
				((EditorActivity) mContext).updatePageNumber(mFromAddNew);
			}

		}
		mIsTaskRunning = false;//by show
	}
	//Begin: show_wang@asus.com
	//Modified reason: for dds
	 @Override  
     protected void onCancelled() {  
         super.onCancelled(); 
		 mPageEditor.endLoad();
         mIsTaskRunning = false;
     }  
	 
	 public boolean isTaskRunning() {
			return mIsTaskRunning;
		}
	 //End: show_wang@asus.com
	 
	private void fileFormatError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.pg_open_fail);
		builder.setPositiveButton(android.R.string.ok, null);
		Dialog dialog = builder.create();
		dialog.show();
		((Activity)mContext).finish();
	}

}
