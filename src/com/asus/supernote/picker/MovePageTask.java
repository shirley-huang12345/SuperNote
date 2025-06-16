package com.asus.supernote.picker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.ga.GACollector;
import com.asus.supernote.sync.SyncHelper;

public class MovePageTask extends AsyncTask<Void, Void, Void> {
    public static final String TAG = "MovePageTask";
    private Activity mActivity;
    private BookCase mBookCase;
    private NoteBook mDestBook;
    private SortedSet<SimplePageInfo> mItems;
    private ProgressDialog mProgressDialog;
    private boolean mIsTaskRunning = false;
    private boolean mHasDestBook = false;

    public MovePageTask(Activity activity, NoteBook srcBook) {
        mActivity = activity;
        if (srcBook.getSelectedItems() != null) {
            mItems = new TreeSet<SimplePageInfo>(srcBook.getSelectedItems());
        }
        else {
            mItems = new TreeSet<SimplePageInfo>();
        }
        mHasDestBook = false;
    }

    //begin darwin
    private List<Long> mMovedPagesList = new ArrayList<Long>();
    private NoteBook mSrcBook = null;
    private boolean mNeedChangeCover = false;
    private boolean mDesBookNeedChangeCover = false;
    //end darwin
    
    public MovePageTask(Activity activity, SortedSet<SimplePageInfo> items) {
        mActivity = activity;
        if (items != null) {
            mItems = new TreeSet<SimplePageInfo>(items);
        }
        else {
            mItems = new TreeSet<SimplePageInfo>();
        }
        mHasDestBook = false;
    }

    public void setDestBook(NoteBook destBook) {
        mDestBook = destBook;
        mHasDestBook = true;
    }

    public boolean hasDestBook() {
        return mHasDestBook;
    }

    public boolean isTaskRunning() {
        return mIsTaskRunning;
    }

    public void setDialog(ProgressDialog d) {
        mProgressDialog = d;
    }
    
    private void updateTimestampInfo(long oldPageId, long newPageId) {
		ContentValues cv = new ContentValues();
		cv.put(MetaData.TimestampTable.OWNER, newPageId);
		mActivity.getContentResolver().update(MetaData.TimestampTable.uri, cv, "owner = ?", 
				new String[] {Long.toString(oldPageId)});
	}
    
    private void updateDoodleItemAttachmentDB(long oldPageId, long newPageId)
	{	
		try
		{
			ContentValues cvDoodle = new ContentValues();
			cvDoodle.put(MetaData.DoodleTable.ID, newPageId);
			mActivity.getContentResolver().update(MetaData.DoodleTable.uri, cvDoodle, "_id = ?", new String[] {Long.toString(oldPageId)});
	        
			ContentValues cvItem = new ContentValues();
			cvItem.put(MetaData.ItemTable.ID, newPageId);
			mActivity.getContentResolver().update(MetaData.ItemTable.uri, cvItem, "_id = ?", new String[] {Long.toString(oldPageId)});
	        
			ContentValues cvAttachment = new ContentValues();
			cvAttachment.put(MetaData.AttachmentTable.ID, newPageId);
			mActivity.getContentResolver().update(MetaData.AttachmentTable.uri, cvAttachment, "_id = ?", new String[] {Long.toString(oldPageId)});
		} catch(Exception e)
		{
			Log.i("darwin test", e.toString());
		}
	}

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mIsTaskRunning = true;
        BookCase bookCase = BookCase.getInstance(mActivity);

        if (!mDestBook.getIsLocked() || !NoteBookPickerActivity.islocked())
        	bookCase.setCurrentBook(mDestBook.getCreatedTime());
       //end wendy
        
        //darwin
        if(mDestBook.getPageOrderList().isEmpty() == true)
        {
        	mDesBookNeedChangeCover = true;
        }
        //darwin
        
        FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(MovePageDialogFragment.TAG);
        if (fragment != null && fragment.isAdded()) {
            ft.remove(fragment);
        }
        ft.commit();
        Bundle b = new Bundle();
        b.putInt("style", MovePageDialogFragment.MOVE_PROGRESS_DIALOG);
        b.putInt("max", (mItems != null) ? mItems.size() : 0);
        MovePageDialogFragment newFragment = MovePageDialogFragment.newInstance(b);
        newFragment.show(mActivity.getFragmentManager(), MovePageDialogFragment.TAG);
    }

    @Override
    protected Void doInBackground(Void... params) {
        mBookCase = BookCase.getInstance(mActivity);
        NoteBook srcBook = null;
        ContentResolver contentResolver = mActivity.getContentResolver();
        for (SimplePageInfo info : mItems) {
			// BEGIN: Better
        	long newPageId = info.pageId;
        	srcBook = mBookCase.getNoteBook(info.bookId);
        	//darwin
            mSrcBook = srcBook;
            if(srcBook.getPageOrder(0) == info.pageId)
            {
            	mNeedChangeCover = true;
            }
            //darwin
        	if (srcBook.getUserId() > 0) {
        		if (srcBook.getUserId() == mDestBook.getUserId()) {
        			ContentValues cv = new ContentValues();
    	            cv.put(MetaData.PageTable.OWNER, mDestBook.getCreatedTime());
    	            cv.put(MetaData.PageTable.USER_ACCOUNT, mDestBook.getUserId());
    	            contentResolver.update(MetaData.PageTable.uri, cv, "created_date = ?", new String[] { info.pageId.toString() });
    	            
    	            srcBook.removePageFromOrder(info.pageId);
    	            mDestBook.addPageToOrderList(newPageId);
    	            
    	            File file = new File(MetaData.DATA_DIR + mDestBook.getCreatedTime());
    	            if (file.exists() == false) {
    	            	file = new File(file, Long.toString(newPageId));
    	                PickerUtility.forceMkDir(file.toString());
    	            }
    	            File srcFile = new File(MetaData.DATA_DIR + info.bookId, info.pageId.toString());
    	            File destFile = new File(MetaData.DATA_DIR + mDestBook.getCreatedTime(), Long.toString(newPageId));
    	            srcFile.renameTo(destFile);
    	            
    	            updateTimestampInfo(info.pageId, newPageId);
    	            updateDoodleItemAttachmentDB(info.pageId, newPageId);
        		} else {
        			 NotePage oldPage = srcBook.getNotePage(info.pageId);
	                 NotePage newPage = new NotePage(mActivity.getApplicationContext(), mDestBook.getCreatedTime());
	                 newPage.setBookmark(oldPage.isBookmark());
	                 newPage.setIndexStatus(oldPage.getIndexStatus());//RICHARD
	                 newPage.getFilePath();
	                 mDestBook.addPage(newPage);
	                 newPageId = newPage.getCreatedTime();
	                 
	                 File file = new File(MetaData.DATA_DIR + mDestBook.getCreatedTime());
	                 if (file.exists() == false) {
	                 	file = new File(file, Long.toString(newPageId));
	                     PickerUtility.forceMkDir(file.toString());
	                 }
	                 File srcFile = new File(MetaData.DATA_DIR + info.bookId, info.pageId.toString());
	                 File destFile = new File(MetaData.DATA_DIR + mDestBook.getCreatedTime(), Long.toString(newPageId));
	                 srcFile.renameTo(destFile);
	                 
	                 srcBook.deletePage(info.pageId);
	                 
	                 updateTimestampInfo(info.pageId, newPageId);
	    	         updateDoodleItemAttachmentDB(info.pageId, newPageId);
        		}
        	} else {
	            ContentValues cv = new ContentValues();
	            cv.put(MetaData.PageTable.OWNER, mDestBook.getCreatedTime());
	            cv.put(MetaData.PageTable.USER_ACCOUNT, mDestBook.getUserId());
	            if (mDestBook.getUserId() > 0) {
	            	newPageId = SyncHelper.pageTime2Id(System.currentTimeMillis());
	            	cv.put(MetaData.PageTable.CREATED_DATE, newPageId);
	            }
	            contentResolver.update(MetaData.PageTable.uri, cv, "created_date = ?", new String[] { info.pageId.toString() });
	            
	            srcBook.removePageFromOrder(info.pageId);
	            mDestBook.addPageToOrderList(newPageId);
	            
	            File file = new File(MetaData.DATA_DIR + mDestBook.getCreatedTime());
	            if (file.exists() == false) {
	            	file = new File(file, Long.toString(newPageId));
	                PickerUtility.forceMkDir(file.toString());
	            }
	            File srcFile = new File(MetaData.DATA_DIR + info.bookId, info.pageId.toString());
	            File destFile = new File(MetaData.DATA_DIR + mDestBook.getCreatedTime(), Long.toString(newPageId));
	            srcFile.renameTo(destFile);
	            
	            updateTimestampInfo(info.pageId, newPageId);
   	         	updateDoodleItemAttachmentDB(info.pageId, newPageId);
        	}
            //begin darwin
            mMovedPagesList.add(newPageId);
            //end darwin
			// END: Better
            publishProgress();
        }
        mDestBook.updateOrderListData();

		//Begin by Emmanual
		if(MetaData.IS_GA_ON)
		{
			GACollector gaCollector = new GACollector(mActivity);
			gaCollector.movePage();
		}
		//End
		
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mIsTaskRunning = false;
        FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(MovePageDialogFragment.TAG);
        if (fragment != null && fragment.isAdded()) {
            ft.remove(fragment);
        }
        ft.commit();
        String msg = String.format(mActivity.getResources().getString(R.string.pg_move_count), mItems.size());
        msg = msg + " " + mDestBook.getTitle();
        Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
        //begin darwin
        if(mNeedChangeCover && mSrcBook != null)
        {
        	mSrcBook.changeBookCover();
        }
        if(mDesBookNeedChangeCover && mDestBook != null)
        {
        	mDestBook.changeBookCover();
        }
        Activity activity = mActivity;
        if(activity instanceof PickerActivity)
        {

        	for(int i = 0; i < mMovedPagesList.size(); i++)
        	{
        		((PickerActivity)activity).genThumb(new PageDataLoader(mActivity), false, mDestBook, mMovedPagesList.get(i));
        	}
        	mMovedPagesList.clear();
        	
        	((PickerActivity)activity).changeTitle(mDestBook);
        	((PickerActivity)activity).updateFragment();
        	
        }
        //BEGIN: RICHARD
		try {
			Intent intent=new Intent();
			intent.setAction(MetaData.ANDROID_INTENT_ACTION_INDEXSERVICE_MOVEPAGE);
			mActivity.sendBroadcast(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//END: RICHARD
        //end   darwin
        mActivity = null;
    }

}
