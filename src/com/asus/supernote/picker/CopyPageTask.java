package com.asus.supernote.picker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.ga.GACollector;

public class CopyPageTask extends AsyncTask<Void, Void, Void> {
    public static final String TAG = "CopyPageTask";
    private Activity mActivity;
    private BookCase mBookCase;
    private NoteBook mDestBook;
    private Set<SimplePageInfo> mItems;
    private ProgressDialog mProgressDialog;
    private boolean mIsTaskRunning = false;
    private boolean mHasDestBook = false;

    //begin darwin
    private boolean mDesBookNeedChangeCover = false;
    private List<Long> mCopiedPagesList = new ArrayList<Long>();
    //end darwin
    
    public CopyPageTask(Activity activity, NoteBook srcBook) {
        mActivity = activity;
        mItems = new TreeSet<SimplePageInfo>(srcBook.getSelectedItems());
        mHasDestBook = false;
    }

    public CopyPageTask(Activity activity, SortedSet<SimplePageInfo> items) {
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
        mHasDestBook = (mDestBook == null);
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

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mIsTaskRunning = true;
        FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(CopyPageDialogFragment.TAG);
        if (fragment != null && fragment.isAdded()) {
            ft.remove(fragment);
            ft.commit();
        }

        //darwin
        if(mDestBook.getPageOrderList().isEmpty() == true)
        {
        	mDesBookNeedChangeCover = true;
        }
        //darwin
        
        Bundle b = new Bundle();
        b.putInt("style", CopyPageDialogFragment.COPY_PROGRESS_DIALOG);
        b.putInt("max", (mItems != null) ? mItems.size() : 0);
        CopyPageDialogFragment newFragment = CopyPageDialogFragment.newInstance(b);
        newFragment.show(mActivity.getFragmentManager(), CopyPageDialogFragment.TAG);

    }

    @Override
    protected Void doInBackground(Void... values) {
        mBookCase = BookCase.getInstance(mActivity);
        for (SimplePageInfo info : mItems) {
            NoteBook oldBook = mBookCase.getNoteBook(info.bookId);
            NotePage oldPage = oldBook.getNotePage(info.pageId);

            NotePage newPage = new NotePage(mActivity, mDestBook.getCreatedTime());
            newPage.setBookmark(oldPage.isBookmark());
            newPage.setPageSize(oldPage.getPageSize());
            newPage.setPageColor(oldPage.getPageColor());
            newPage.setPageStyle(oldPage.getPageStyle());
            newPage.setIndexStatus(oldPage.getIndexStatus());//RICHARD
            newPage.getFilePath();
            mDestBook.addPage(newPage);
            //begin wendy
            copyTimestampView(oldPage.getCreatedTime(), newPage.getCreatedTime());
            //end wendy
            copyDoodleItemAttachmentDB(oldPage.getCreatedTime(), newPage.getCreatedTime());
            
            File srcFile = new File(MetaData.DATA_DIR + info.bookId, info.pageId.toString());
            File destFile = new File(MetaData.DATA_DIR + mDestBook.getCreatedTime(), Long.toString(newPage.getCreatedTime()));
            copyFile(srcFile, destFile);
            //begin darwin
            mCopiedPagesList.add(newPage.getCreatedTime());
            //end darwin
            publishProgress();
        }

		//Begin by Emmanual
		if(MetaData.IS_GA_ON)
		{
			GACollector gaCollector = new GACollector(mActivity);
			gaCollector.copyPage();
		}
		//End
		
        return null;
    }
    
    private void copyTimestampView(long oldpageid, long newpageid)
    {
    	ContentResolver cr = mActivity.getContentResolver();
    	Cursor cursor = cr.query(MetaData.TimestampTable.uri, null, "owner = ?", new String[]{Long.toString(oldpageid)}, null);
    	if(cursor.getCount() > 0 )
    	{
    		cursor.moveToFirst();
    		while(!cursor.isAfterLast())
    		{
    			  ContentValues cv = new ContentValues();
    			  cv.put(MetaData.TimestampTable.CREATED_DATE, cursor.getLong(MetaData.TimestampTable.INDEX_CREATE_DATE));
    			  cv.put(MetaData.TimestampTable.POSITION, cursor.getInt(MetaData.TimestampTable.INDEX_POSITION));
    			  cv.put(MetaData.TimestampTable.OWNER, newpageid);
    			//darwin
    			  Cursor cur = cr.query(MetaData.PageTable.uri, 
    					  null, 
    					  "created_date = ?", 
    					  new String[]{Long.toString(newpageid)}, 
    					  null);
    			  if(cur.getCount() > 0 )
    			  {
    				  cur.moveToFirst();
    				  if(!cur.isAfterLast())
    				  {
    					  cv.put(MetaData.TimestampTable.USER_ACCOUNT,cur.getLong(MetaData.PageTable.INDEX_USER_ACCOUNT));
    				  }
    			  }
				  cur.close();//RICHARD FIX MEMORY LEAK
    			  //darwin
    			  cr.insert(MetaData.TimestampTable.uri, cv);
    			  cursor.moveToNext();
    		}    		
    	}
    	cursor.close();
    }
    
    private void copyDoodleItemAttachmentDB(long oldpageid, long newpageid)
	{	
    	Cursor cursor = null;
		try
		{
			ContentResolver cr = mActivity.getContentResolver();
	    	cursor = cr.query(MetaData.ItemTable.uri, null, "_id = ?", new String[]{Long.toString(oldpageid)}, null);
	    	if(cursor.getCount() > 0 )
	    	{
	    		cursor.moveToFirst();
	    		while(!cursor.isAfterLast())
	    		{
	    			ContentValues cvItem = new ContentValues();
	    			cvItem.put(MetaData.ItemTable.ID, newpageid);
	    			cvItem.put(MetaData.ItemTable.IS_DELETE, cursor.getInt(MetaData.ItemTable.INDEX_IS_DELETE));
	    			cvItem.put(MetaData.ItemTable.MODIFIED_DATE,  cursor.getLong(MetaData.ItemTable.INDEX_MODIFIED_DATE));
	    			cvItem.put(MetaData.ItemTable.LASTSYNC_MODIFYTIME,  cursor.getLong(MetaData.ItemTable.INDEX_LASTSYNC_MODIFYTIME));
	    			cr.insert(MetaData.ItemTable.uri, cvItem);
	    			cursor.moveToNext();
	    		}    		
	    	}
	    	cursor.close();
	    	
	    	cursor = cr.query(MetaData.DoodleTable.uri, null, "_id = ?", new String[]{Long.toString(oldpageid)}, null);
	    	if(cursor.getCount() > 0 )
	    	{
	    		cursor.moveToFirst();
	    		while(!cursor.isAfterLast())
	    		{
	    			ContentValues cvDoodle = new ContentValues();
	    			cvDoodle.put(MetaData.DoodleTable.ID, newpageid);
	    			cvDoodle.put(MetaData.DoodleTable.IS_DELETE, cursor.getInt(MetaData.DoodleTable.INDEX_IS_DELETE));
	    			cvDoodle.put(MetaData.DoodleTable.MODIFIED_DATE, cursor.getLong(MetaData.DoodleTable.INDEX_MODIFIED_DATE));
	    			cvDoodle.put(MetaData.DoodleTable.LASTSYNC_MODIFYTIME,  cursor.getLong(MetaData.DoodleTable.INDEX_LASTSYNC_MODIFYTIME));
	    			cr.insert(MetaData.DoodleTable.uri, cvDoodle);
	    			cursor.moveToNext();
	    		}    		
	    	}
	    	cursor.close();
	    	
	    	cursor = cr.query(MetaData.AttachmentTable.uri, null, "_id = ?", new String[]{Long.toString(oldpageid)}, null);
	    	if(cursor.getCount() > 0 )
	    	{
	    		cursor.moveToFirst();
	    		while(!cursor.isAfterLast())
	    		{
	    			ContentValues cvAttachment = new ContentValues();
		        	cvAttachment.put(MetaData.AttachmentTable.ID, newpageid);
		        	cvAttachment.put(MetaData.AttachmentTable.MODIFIED_DATE, cursor.getLong(MetaData.AttachmentTable.INDEX_MODIFIED_DATE));
		        	cvAttachment.put(MetaData.AttachmentTable.IS_DELETE, cursor.getInt(MetaData.AttachmentTable.INDEX_IS_DELETE));
		        	cvAttachment.put(MetaData.AttachmentTable.FILE_NAME, cursor.getString(MetaData.AttachmentTable.INDEX_FILE_NAME));
		        	cr.insert(MetaData.AttachmentTable.uri, cvAttachment);
	    			cursor.moveToNext();
	    		}    		
	    	}
	    	cursor.close();
		} catch(Exception e)
		{
			e.printStackTrace();
		}finally{
			if(cursor != null)
				cursor.close(); //smilefish add for memory leak
		}
	}

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mIsTaskRunning = false;
        BookCase bookCase = BookCase.getInstance(mActivity);
        //begin wendy
        if(mDesBookNeedChangeCover && mDestBook != null)
        {
        	mDestBook.changeBookCover();
        }
        //darwin
        if (!mDestBook.getIsLocked() || !NoteBookPickerActivity.islocked())
        	bookCase.setCurrentBook(mDestBook.getCreatedTime());
        //end wendy
        FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(CopyPageDialogFragment.TAG);
        if (fragment != null && fragment.isAdded()) {
            ft.remove(fragment);
        }
        ft.commit();
        String msg = String.format(mActivity.getResources().getString(R.string.pg_copy_count), mItems.size(), mDestBook.getTitle());
        Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
        //begin wendy
        Activity activity = mActivity;
        if(activity instanceof PickerActivity)
        {
        	//begin darwin
        	for(int i = 0; i < mCopiedPagesList.size(); i++)
        	{
        		((PickerActivity)activity).genThumb(new PageDataLoader(mActivity), false, mDestBook, mCopiedPagesList.get(i));
        	}
        	mCopiedPagesList.clear();
        	//end   darwin
        	((PickerActivity)activity).changeTitle(mDestBook);
        	((PickerActivity)activity).updateFragment();
        }
        if(activity instanceof NoteBookPickerActivity)
        {
        	((NoteBookPickerActivity)activity).updateFragment();
        }
        //end wendy
        mActivity = null;
    }

    private void copyFile(File src, File dest) {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
            }
            String[] children = src.list();
            for (String child : children) {
                copyFile(new File(src, child), new File(dest, child));
            }
        }
        else {
            try {
                FileChannel srcChannel = new FileInputStream(src).getChannel();
                FileChannel destChannel = new FileOutputStream(dest).getChannel();
                destChannel.transferFrom(srcChannel, 0, srcChannel.size());
                srcChannel.close();
                destChannel.close();
            }
            catch (FileNotFoundException e) {
            }
            catch (IOException e) {
            }
        }
    }

    /********************************************************
     * Copy Page Task END
     *******************************************************/

}
