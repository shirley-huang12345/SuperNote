package com.asus.supernote.indexservice;

import java.util.LinkedList;

import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.sync.CountDownClass;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class IndexService extends Service {
	// debugging support
	private static final String TAG = IndexService.class.getSimpleName();
	private static final boolean DBG = true;

	// service start and stop custom intents
	public static final String INDEXER_START_INTENT = "com.asus.supernote.indexservice.intent.action.START";
	public static final String INDEXER_STOP_INTENT = "com.asus.supernote.indexservice.intent.action.STOP";

	public enum IndexerServiceMessage {
		START_INDEXER, NEW_INK_AVAILABLE, PRIORITY_CHANGED, INDEXER_QUIT
	};

	// content provider access
	private ContentResolver cr;

	static final int MSG_INDEX_SPECIFIC_PAGE = 1;
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
    	IndexService getService() {
            return IndexService.this;
        }
    }
	private final IBinder mBinder = new LocalBinder();
	private Messenger mMessenger = null;
	
	private Boolean mThreadIsRunning = false;
	private static Object mListLockObj = new Object();
	private LinkedList<IndexPageItem> mIndexPageList = null;
	
	private class IndexPageItem
	{
		public int mLanguage;
		public Long mPageID;
		
		public IndexPageItem(int language,Long pageID)
		{
			mLanguage = language;
			mPageID = pageID;
		}
	};
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		
		if (DBG)
			Log.d(TAG, "IBinder onBind");
		
		
		return mBinder;
	}

	/**
	 * This method is provided for use by the Service. Do not override, or
	 * directly call from your own code.
	 */
	@Override
	public void onCreate() {
		if (DBG)
			Log.d(TAG, "IndexerService.onCreate");

		cr = getContentResolver();
		mThreadIsRunning = true;
		mIndexPageList = new LinkedList<IndexPageItem>();
		
		updateDBIndexLockStatusToNotCtreateStatus();//Richard ,check if some page's index flag is in wrong status
		updateDBIndexRecreateStatusToNotCtreateStatus();
		mThread.start();
		
		Thread t = new Thread() {
			public void run() {
				Looper.prepare(); 
				mMessenger = new Messenger(new ServiceHandler());
				Looper.loop(); 
			}
		};
		t.start();
	}

	/**
	 * This method is provided for use by the Service. Do not override, or
	 * directly call from your own code.
	 */
	@Override
	public ComponentName startService(Intent service) {
		if (service.getAction() == INDEXER_START_INTENT)
			return super.startService(service);
		else
			return null;
	}

	/**
	 * This method is provided for use by the Service. Do not override, or
	 * directly call from your own code.
	 */
	@Override
	public boolean stopService(Intent name) {
		if (name.getAction() == INDEXER_STOP_INTENT)
			return super.stopService(name);
		else
			return false;
	}

	/**
	 * This method is provided for use by the Service. Do not override, or
	 * directly call from your own code.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (DBG)
			Log.d(TAG, "IndexerService.onStartCommand");

		// RICHARD
		// LOAD INDEX

		// if we get killed, after returning from here, restart last pending
		// task
		return START_REDELIVER_INTENT;
	}

	/**
	 * This method is provided for use by the Service. Do not override, or
	 * directly call from your own code.
	 */
	@Override
	public void onDestroy() {
		if (DBG)
			Log.d(TAG, "Indexer service done");

		// remove resource
	}

	public void sendMsg(Message msg)
	{
		try
		{
			mMessenger.send(msg);
		}catch(Exception exp)
		{
			
		}
	}
	
	// handler that receives messages from the thread
	final class ServiceHandler extends Handler {
		/**
		 * This method is provided for use by the Handler. Do not override, or
		 * directly call from your own code.
		 */
		@Override
		public void handleMessage(Message msg) {
			if (DBG)
				Log.d(TAG, "IndexerService.handleMessage");
			
			if(!mThreadIsRunning)
			{
				return;
			}

			switch (msg.what) {
				case MSG_INDEX_SPECIFIC_PAGE: {
					Long pageID = (Long) msg.obj;
					Cursor cursor = cr.query(MetaData.PageTable.uri, null,
							MetaData.PageTable.CREATED_DATE +" = " + pageID,null , null);
					int language = 0;
					if(cursor.getCount() > 0)
					{
						cursor.moveToFirst();
						language = cursor
								.getInt(MetaData.PageTable.INDEX_INDEX_LANGUAGE);
					}
					cursor.close();
					
					if(language > NoteBook.NO_INDEX_LAGNUAGE)
					{
						putIndexPageID(new IndexPageItem(language,pageID));
						synchronized (mThread) {
							mThread.notify();
						}
					}
				}
				break;
			}

		}
	}
	
	private void saveIndexFile(IndexPageItem indexPageItem)
	{
		Cursor cursor = cr.query(MetaData.PageTable.uri, null,
				"created_date = ?", new String[] { Long.toString(indexPageItem.mPageID) }, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		} else {
			cursor.moveToFirst();
			Long bookID = cursor
					.getLong(MetaData.PageTable.INDEX_OWNER);
			cursor.close();

			if(getPageIndexFlageInDB(indexPageItem.mPageID) == MetaData.INDEX_FILE_CREATE_LOCK)
			{
				return;//the file is changing.
			}
			 
			NoteItemFile noteItemFile = new NoteItemFile(this,bookID,indexPageItem.mPageID);
			Boolean indexFileStatus;
			indexFileStatus = IndexFile.saveIndexFile(noteItemFile.loadNoteItem(),noteItemFile.getFolderPath()+"/" + MetaData.NOTE_INDEX_PREFIX,indexPageItem.mLanguage);
			
			if(getPageIndexFlageInDB(indexPageItem.mPageID) != MetaData.INDEX_FILE_CREATE_LOCK)
			{
				if(indexFileStatus)
				{
					updateDBIndexStatus(MetaData.INDEX_FILE_CREATE_SUCESS,indexPageItem.mPageID);		
					try {
						Intent intent=new Intent();
						intent.putExtra("pageID", indexPageItem.mPageID);
						intent.setAction(MetaData.ANDROID_INTENT_ACTION_INDEXSERVICE_NEWINDEXFILE);
						sendBroadcast(intent);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					updateDBIndexStatus(MetaData.INDEX_FILE_CREATE_FAILE,indexPageItem.mPageID);	
				}
				Log.d(TAG, "save over");
			}
		}
	}
	
	/*
	 * update all page
	 */
	private void updateDBIndexLockStatusToNotCtreateStatus()
	{
        ContentValues cvUpdate = new ContentValues();
        cvUpdate.put(MetaData.PageTable.IS_INDEXED, MetaData.INDEX_FILE_CREATE_NOT);
        cr.update(MetaData.PageTable.uri, cvUpdate, MetaData.PageTable.INDEX_IS_INDEXED + "=" + MetaData.INDEX_FILE_CREATE_LOCK, null);
	}
	
	private void updateDBIndexRecreateStatusToNotCtreateStatus()
	{
        ContentValues cvUpdate = new ContentValues();
        cvUpdate.put(MetaData.PageTable.IS_INDEXED, MetaData.INDEX_FILE_CREATE_NOT);
        cr.update(MetaData.PageTable.uri, cvUpdate, MetaData.PageTable.INDEX_IS_INDEXED + "=" + MetaData.INDEX_FILE_CREATE_RECREATE, null);
	}
	
	private void updateDBIndexStatus(int status,Long pageID)
	{
        ContentValues cvUpdate = new ContentValues();
        cvUpdate.put(MetaData.PageTable.IS_INDEXED, status);
        cr.update(MetaData.PageTable.uri, cvUpdate, MetaData.PageTable.CREATED_DATE + "=" + pageID, null);
	}
	
	private void updateDBIndexStatusByBook(int status,Long bookID)
	{
        ContentValues cvUpdate = new ContentValues();
        cvUpdate.put(MetaData.PageTable.IS_INDEXED, status);
        cr.update(MetaData.PageTable.uri, cvUpdate, MetaData.PageTable.OWNER + "=" + bookID, null);
	}
	
	private void updateDBPageIndexLanguage(int language,Long bookID)
	{
        ContentValues cvUpdate = new ContentValues();
        cvUpdate.put(MetaData.PageTable.INDEX_LANGUAGE, language);
        cr.update(MetaData.PageTable.uri, cvUpdate, MetaData.PageTable.OWNER + "=" + bookID, null);
	}
	
	private long getPageIndexFlageInDB(Long pageID)
	{
		long pageIndexStatus =-1;
		Cursor cursor = cr.query(MetaData.PageTable.uri, null,
				"created_date = ?", new String[] { Long.toString(pageID) }, null);
		
		if(cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			pageIndexStatus = cursor
					.getLong(MetaData.PageTable.INDEX_IS_INDEXED);
		}
		cursor.close();
		
		return pageIndexStatus;
	}
	
	private void putIndexPageID(IndexPageItem indexPageItem)
	{
		synchronized(mListLockObj)
		{
        	mIndexPageList.add(indexPageItem);
		}
	}
	
	private int getNoteBookIndexLaguage(long bookID)
	{
		Cursor tempCursor = cr.query(MetaData.BookTable.uri, null,
				MetaData.BookTable.CREATED_DATE +" = " + bookID,null , null);

		if(tempCursor.getCount() > 0)
		{
			tempCursor.moveToFirst();
			int bookLanguage = tempCursor.getInt(MetaData.BookTable.INDEX_INDEX_LANGUAGE);
			if(bookLanguage == NoteBook.NO_INDEX_LAGNUAGE)
			{
				updateDBIndexStatusByBook(MetaData.INDEX_FILE_DISABLE,bookID);
			}
			tempCursor.close();
			return bookLanguage;
		}
		else
		{
			//NOTE BOOK IS NOT READY
			tempCursor.close();
			return NoteBook.WAIT_TO_SET_INDEX_LANGUAGE;
		}
	}
	
	private IndexPageItem getIndexPageItemFromThread()
	{
		IndexPageItem resItem = null;
		Cursor cursor = cr.query(MetaData.PageTable.uri, null,
				MetaData.PageTable.IS_INDEXED +" = " + MetaData.INDEX_FILE_CREATE_NOT + 
				" AND " + MetaData.PageTable.INDEX_LANGUAGE + " <> " + NoteBook.NO_INDEX_LAGNUAGE,null , null);
		
		if(cursor.getCount() > 0)
		{
			Long resID;
			int language;
			cursor.moveToFirst();
			do
			{
				resID = cursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE);
				language =cursor.getInt(MetaData.PageTable.INDEX_INDEX_LANGUAGE);
				
				long bookID = cursor.getLong(MetaData.PageTable.INDEX_OWNER);

				if(language == NoteBook.WAIT_TO_SET_INDEX_LANGUAGE)
				{
					int booklanguage = getNoteBookIndexLaguage(bookID);
					if(booklanguage > NoteBook.NO_INDEX_LAGNUAGE)
					{
						updateDBPageIndexLanguage(booklanguage,resID);
						resItem = new IndexPageItem(booklanguage,resID);
						break;
					}		
				}
				else 
				{
					resItem = new IndexPageItem(language,resID);
					break;
				}
			}while(cursor.moveToNext());
		}
		cursor.close();
		
		return resItem;
	}
	
	private IndexPageItem getIndexPageID()
	{
		IndexPageItem resItem = null;
			
		if(!CountDownClass.isTaskRunning())
		{
			synchronized(mListLockObj)
			{
	        	if(mIndexPageList.size() > 0)
	        	{
	        		resItem = mIndexPageList.pollFirst();
	        	}
			}
			
			if(resItem != null)
			{
				return resItem;
			}
			
			return getIndexPageItemFromThread();
		}
		
		return null;
	}
	
    /**
     * This is the thread that will do our work.  It sits in a loop running
     * the progress up until it has reached the top, then stops and waits.
     */
    final Thread mThread = new Thread() {
        @Override
        public void run() {

            // This thread runs almost forever.
            while (true) {

                // Update our shared state with the UI.
                synchronized (this) {
                	if(!mThreadIsRunning)
                	{
                		return;
                	}
                	IndexPageItem indexPageItem = getIndexPageID();
                	if(indexPageItem != null)
                	{
                    	if(indexPageItem.mLanguage == NoteBook.NO_INDEX_LAGNUAGE){
                    		updateDBIndexStatus(MetaData.INDEX_FILE_DISABLE,indexPageItem.mPageID);
                    		continue;
                    	}
                		saveIndexFile(indexPageItem);
                	}
                	else
                	{
                		//no index file need to save,Sleep 500ms
	                    try {
	                        wait(500);
	                    } catch (InterruptedException e) {
	                    }
                	}
                }

            }
        }
    };

}
