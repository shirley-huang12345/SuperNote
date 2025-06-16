package com.asus.supernote.picker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;

public class DeletePagesTask extends AsyncTask<Void, Void, Void> {
    public static final String TAG = "DeletePagesTask";
    private Activity mActivity;
    private BookCase mBookcase;
    private SortedSet<SimplePageInfo> mItems;
    private FragmentManager mFragmentManager;
    private ContentResolver mContentResolver;
    private ProgressDialog mProgressDialog;
    private boolean mIsTaskRunning = false;
    private int mCount = 0;

    //begin darwin
    private List<NoteBook> mSrcBooks = null;//modify by emmanual to fix bug 479300
    private boolean mNeedChangeCover = false;
    //end darwin
    public DeletePagesTask(Activity activity, SortedSet<SimplePageInfo> items) {
        mActivity = activity;
        mBookcase = BookCase.getInstance(mActivity);
        mFragmentManager = mActivity.getFragmentManager();
        mContentResolver = mActivity.getContentResolver();
        if (items == null) {
            mItems = new TreeSet<SimplePageInfo>();
        }
        else {
            mItems = new TreeSet<SimplePageInfo>(items);
        }

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        Fragment fragment = mFragmentManager.findFragmentByTag(DeletePageDialogFragment.TAG);
        if (fragment != null && fragment.isAdded()) {
            ft.remove(fragment);
        }
        ft.commit();
        Bundle b = new Bundle();
        b.putInt("style", DeletePageDialogFragment.DELETE_CONFIRM_DIALOG);
        DeletePageDialogFragment newFragment = DeletePageDialogFragment.newInstance(b,this);
        newFragment.show(mFragmentManager, DeletePageDialogFragment.TAG);

    }

    public boolean isTaskRunning() {
        return mIsTaskRunning;
    }

    public void setDialog(ProgressDialog d) {
        mProgressDialog = d;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mIsTaskRunning = true;
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        Fragment fragment = mFragmentManager.findFragmentByTag(DeletePageDialogFragment.TAG);
        if (fragment != null && fragment.isAdded()) {
            ft.remove(fragment);
        }
        ft.commit();
        Bundle b = new Bundle();
        b.putInt("max", mItems.size());
        b.putInt("style", DeletePageDialogFragment.DELETE_PROGRESS_DIALOG);
        DeletePageDialogFragment newFragment = DeletePageDialogFragment.newInstance(b,this);
        newFragment.show(mFragmentManager, DeletePageDialogFragment.TAG);

    }

    @Override
    protected Void doInBackground(Void... params) {
    	//emmanual to fix bug 479300
		if (mSrcBooks == null) {
			mSrcBooks = new ArrayList<NoteBook>();
		}
        for (SimplePageInfo item : mItems) {
            NoteBook book = mBookcase.getNoteBook(item.bookId);
            //darwin
            mSrcBooks.add(book);
            if(book.getPageOrder(0) == item.pageId)
            {
            	mNeedChangeCover = true;
            }
            //darwin
            deleteDir(new File(MetaData.DATA_DIR + item.bookId + "/" + item.pageId));
            book.removePageFromOrder(item.pageId);
           
            //add by wendy 0401 delete -> update begin ++
           ContentValues cv = new ContentValues();
            cv.put(MetaData.PageTable.IS_DELETED, 2);
            mContentResolver.update(MetaData.PageTable.uri, cv,"created_date = ?", new String[] { item.pageId.toString() });
            //add by wendy 0401 delete -> update end --- 
            
            mContentResolver.delete(MetaData.TimestampTable.uri, 
					"owner = ?", new String[]{ item.pageId.toString() });
       	 	mContentResolver.delete(MetaData.ItemTable.uri, 
       	 			"_id = ?", new String[]{ item.pageId.toString() });
       	 	mContentResolver.delete(MetaData.DoodleTable.uri, 
    	 			"_id = ?", new String[]{ item.pageId.toString() });
       	 	mContentResolver.delete(MetaData.AttachmentTable.uri, 
    	 			"_id = ?", new String[]{ item.pageId.toString() });
              
            publishProgress();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        mCount = mCount + 1;
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.setProgress(mCount);
        }

    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mIsTaskRunning = false;
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        Fragment fragment = mFragmentManager.findFragmentByTag(DeletePageDialogFragment.TAG);
        if (fragment != null && fragment.isAdded()) {
            ft.remove(fragment);
        }
        ft.commit();
        String msg = String.format(mActivity.getResources().getString(R.string.pg_del_count), mItems.size());
        Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
        //begin wendy
        Activity activity = mActivity;
        if(activity instanceof PickerActivity)
        {
        	((PickerActivity)activity).updateFragment();
        }
        if(activity instanceof NoteBookPickerActivity)
        {
        	((NoteBookPickerActivity)activity).updateFragment();
        }
        //end wendy
        
        //BEGIN: RICHARD
		try {
			Intent intent=new Intent();
			intent.setAction(MetaData.ANDROID_INTENT_ACTION_INDEXSERVICE_DELETEPAGE);
			mActivity.sendBroadcast(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//END: RICHARD
		//begin darwin
        if(mNeedChangeCover && mSrcBooks != null)
        {
			for (NoteBook mSrcBook : mSrcBooks){
				mSrcBook.changeBookCover();
			}
        }
        mSrcBooks.clear();
        //end darwin
    }

    public void deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] subDirs = dir.list();
            for (String sub : subDirs) {
                deleteDir(new File(dir, sub));
            }
        }
        dir.delete();
    }

}
