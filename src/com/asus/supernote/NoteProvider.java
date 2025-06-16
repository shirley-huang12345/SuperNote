package com.asus.supernote;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.asus.supernote.data.MetaData;

public class NoteProvider extends ContentProvider {
    public static final String TAG = "NoteProvider";
    public static final boolean DEBUG = false;
    public static Boolean LOCK = Boolean.valueOf(false);
    private static final int NO_MATCH = 0;
    private static final int BOOK_TABLE = 1;
    private static final int PAGE_TABLE = 2;
    private static final int CLIPBOARD_TABLE = 3;
    private static final int MEMOBOARD_TABLE = 4;
    private static final int WEBACCOUNT_TABLE = 5;//add by wendy webstorage-account
    private static final int TIMESTAMP_TABLE = 6;// wendy
	    //Begin Add by Darwin_Yu@asus.com
    private static final int DOODLE_TABLE     = 7;
    private static final int ITEM_TABLE       = 8;
    private static final int ATTACHMENT_TABLE = 9;
	//End   Add by Darwin_Yu@asus.com
    private static final int TODO_WIDGET_TABLE = 10;//Allen
    
	//begin:clare
    private static final int WIDGET_TABLE = 11;
    private static final int WIDGET_ITEM_TABLE = 12;
    //end:clare

    private static final UriMatcher mMatcher;

    static {
        mMatcher = new UriMatcher(NO_MATCH);
        mMatcher.addURI(MetaData.AUTHORITY, MetaData.BOOK_TABLE, BOOK_TABLE);
		//Begin Add by Darwin_Yu@asus.com
        mMatcher.addURI(MetaData.AUTHORITY, MetaData.DOODLE_TABLE, DOODLE_TABLE);
        mMatcher.addURI(MetaData.AUTHORITY, MetaData.ITEM_TABLE, ITEM_TABLE);
        mMatcher.addURI(MetaData.AUTHORITY, MetaData.ATTACHMENT_TABLE, ATTACHMENT_TABLE);
    	//End   Add by Darwin_Yu@asus.com
        mMatcher.addURI(MetaData.AUTHORITY, MetaData.PAGE_TABLE, PAGE_TABLE);
        mMatcher.addURI(MetaData.AUTHORITY, MetaData.CLIPBOARD_TABLE, CLIPBOARD_TABLE);
        mMatcher.addURI(MetaData.AUTHORITY, MetaData.MEMOBROAD_TABLE, MEMOBOARD_TABLE);
        mMatcher.addURI(MetaData.AUTHORITY, MetaData.WEBACCOUNT_TABLE, WEBACCOUNT_TABLE);//add by wendy webstorage-account
        mMatcher.addURI(MetaData.AUTHORITY, MetaData.TIMESTAMP_TABLE, TIMESTAMP_TABLE);//wendy
        mMatcher.addURI(MetaData.AUTHORITY, MetaData.TODO_WIDGET_TABLE, TODO_WIDGET_TABLE);//Allen
				
		//begin:clare
        mMatcher.addURI(MetaData.AUTHORITY, MetaData.WIDGET_TABLE, WIDGET_TABLE);//clare
        mMatcher.addURI(MetaData.AUTHORITY, MetaData.WIDGET_ITEM_TABLE, WIDGET_ITEM_TABLE);//clare
        //end:clare

    }

    private static DatabaseHelper mDatabaseHelper;
    private static SQLiteDatabase mWritableDatabase;
    private static ContentResolver mContentResolver;

    public static final void reloadDatabase(Context context) {
        if (mWritableDatabase == null) {
            mDatabaseHelper = new DatabaseHelper(context, MetaData.DATABASE_NAME, null, MetaData.DATABASE_VERSION);
            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        }

    }

    public static final void releaseDatabase() {
        if (mWritableDatabase != null) {
            mWritableDatabase.releaseReference();
            mWritableDatabase.close();
            mWritableDatabase = null;
        }
    }

    @Override
    public boolean onCreate() {
        // BEGIN: archie_huang@asus.com
        // To avoid memory leak
        Context context = getContext().getApplicationContext();
        mDatabaseHelper = new DatabaseHelper(context, MetaData.DATABASE_NAME, null, MetaData.DATABASE_VERSION);
        if(mWritableDatabase != null){
            mWritableDatabase.close();
        }
        mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        mContentResolver = context.getContentResolver();
        // END: archie_huang@asus.com
        //Begin Allen
		SharedPreferences mPreference = context.getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE);
		MetaData.INDEX_LANGUAGE_DEFAULT = mPreference.getInt(context.getResources().getString(R.string.pref_index_language), 0);
		//End Allen
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String tableName = selectTable(uri);
        long rowId = mWritableDatabase.insert(tableName, MetaData.NO_DATA, values);
        if (rowId > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, rowId);
            return newUri;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String tableName = selectTable(uri);
        int count = mWritableDatabase.delete(tableName, selection, selectionArgs);
        mContentResolver.notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String tableName = selectTable(uri);
		
		// BEGIN: Better
		// query all include deleted pages when sync starts
		String sel = selection;

		if (MetaData.BOOK_TABLE.equals(tableName)
				|| MetaData.PAGE_TABLE.equals(tableName)) {
			if (selection == null || selection.length() == 0) {
				sel = "is_deleted = 0";
			} else {
				if (selection.length() >= MetaData.SYNC_CUSTOM_DB_QUERY_TAG
						.length()
						&& selection.substring(0,
								MetaData.SYNC_CUSTOM_DB_QUERY_TAG.length())
								.equals(MetaData.SYNC_CUSTOM_DB_QUERY_TAG)) {
					if (selection.length() > MetaData.SYNC_CUSTOM_DB_QUERY_TAG
							.length()) {
						sel = selection
								.substring(MetaData.SYNC_CUSTOM_DB_QUERY_TAG
										.length());
					} else {
						sel = null;
					}
				} else {
					sel = "(" + selection + ")" + "AND is_deleted = 0";
				}
			}
		}
		// END: Better
		
		SQLiteDatabase readableDatebase = mDatabaseHelper.getReadableDatabase(); //smilefish fix bug 695977
        if (readableDatebase != null) {
            Cursor cursor = readableDatebase.query(tableName, projection, sel, selectionArgs, null, null, sortOrder);
            return cursor;
        }
        else {
            return null;
        }

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String tableName = selectTable(uri);
        int count = mWritableDatabase.update(tableName, values, selection, selectionArgs);
        return count;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
    	//emmanual to fix bug 479661
        if(values == null){
        	return 0;
        }
        String tableName = selectTable(uri);
        mWritableDatabase.beginTransaction();
        for (int i = 0; i < values.length; ++i) {
            mWritableDatabase.insert(tableName, MetaData.NO_DATA, values[i]);
        }
        mWritableDatabase.setTransactionSuccessful();
        mWritableDatabase.endTransaction();
        return values.length;
    }

    private String selectTable(Uri uri) {
        switch (mMatcher.match(uri)) {
            case BOOK_TABLE:
                return MetaData.BOOK_TABLE;
            case PAGE_TABLE:
                return MetaData.PAGE_TABLE;
            case CLIPBOARD_TABLE:
                return MetaData.CLIPBOARD_TABLE;
            case MEMOBOARD_TABLE:
                return MetaData.MEMOBROAD_TABLE;
            case WEBACCOUNT_TABLE://add by wendy webstorage-account
            	return MetaData.WEBACCOUNT_TABLE;
			case TIMESTAMP_TABLE: // add by wendy timestamp-table
            	return MetaData.TIMESTAMP_TABLE;
			//Begin Add by Darwin_Yu@asus.com
			case DOODLE_TABLE:
				return MetaData.DOODLE_TABLE;
			case ITEM_TABLE:
				return MetaData.ITEM_TABLE;
			case ATTACHMENT_TABLE:
				return MetaData.ATTACHMENT_TABLE;
			case WIDGET_TABLE:
				return MetaData.WIDGET_TABLE;
			case WIDGET_ITEM_TABLE:
				return MetaData.WIDGET_ITEM_TABLE;
        	//End   Add by Darwin_Yu@asus.com
			//Begin Allen
			case TODO_WIDGET_TABLE:
				return MetaData.TODO_WIDGET_TABLE;
			//End Allen
            default:
                new IllegalArgumentException("Unknown URI = " + uri.toString());
                return null;
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
	//Begin Allen
		private static final String CREATE_TODO_WIDGET_TABLE_COMMAND="CREATE TABLE todo_widget(" + 
				"_id INTEGER PRIMARY KEY, " + 		   // 0
				"widget_id INTEGER,"+  				   //1
        		"adapter_type smallint,"+ 			   //2
				"book_id Long,"+  				   	   //3
				"sort_by smallint);";  			   //4
    //End Allen
    	
	//begin:clare
		private static final String CREATE_WIDGET_TABLE_COMMAND="CREATE TABLE Widget(" + "_id INTEGER PRIMARY KEY, " + // 0
				"widget_id INTEGER,"+  				   //1
        		"widget_mode INTEGER);"; 			   //2
		
		private static final String CREATE_WIDGET_ITEM_TABLE_COMMAND="CREATE TABLE Widget_Item(" + "_id INTEGER PRIMARY KEY, " + // 0
				"item_id INTEGER,"+                    //1 	  item id
				"item_widget_id INTEGER,"+             //2    the widget in which this item locates
				"item_order_id INTEGER,"+              //3    which grid this item inserts          
        		"item_mode INTEGER,"+                  //4    0: blank  1:notebook  2:all book and all template
				"item_book_id Long,"+                  //5           
				"item_page_id Long,"+                  //6        
				"item_allbook_alltemplate INTEGER,"+   //7    1: all book  0:all template
				"item_lock_state INTEGER);";            //8    1: lock  0:unlock 
        		 
	//end:clare
	
        private static final String CREATE_BOOK_TABLE_COMMAND = "CREATE TABLE Book(" + "_id INTEGER PRIMARY KEY, " + // 0
                "title TEXT, " + // 1
                "created_date Long, " + // 2
                "modified_date Long, " + // 3
                "page_counter INTEGER, " + // 4
                "page_current INTEGER, " + // 5
                "is_locked INTEGER, " + // 6
                "thumbnail TEXT, " + // 7
                "page_order BLOB, " + // 8
                "book_size INTEGER, " + // 9
                "book_color INTEGER, " + // 10
                "book_grid INTEGER," + // 11
               // "is_phone_memo INTEGER);"; // 12
             //add by wendy begin++
               "is_phone_memo INTEGER," + // 12
			   "font_size INTEGER DEFAULT 1," + // 13
               "lastsync_modifytime Long NOT NULL DEFAULT 0, " + //14
              // "is_deleted INTEGER NOT NULL DEFAULT 0);"; //+ 14 
              "is_deleted INTEGER NOT NULL DEFAULT 0,"+ //+ 15 
				"userAccount Long NOT NULL DEFAULT 0," + // 16
				// add by wendy end---
		        // BEGIN: Better
				"version INTEGER DEFAULT 3," + // 17
				// END: Better
        		"template INTEGER DEFAULT 0,"+ //wendy allen++
        		"index_language INTEGER DEFAULT -1," + //Allen
        		"index_cover INTEGER DEFAULT 0," +//darwin
        		"cover_modifytime Long NOT NULL DEFAULT 0);";//darwin
        /*
         *     public static final String LASTSYNC_MODIFYTIME = "lastsync_modifytime";
        public static final String IS_DELETED 		   = "is_deleted";
         */
        
        //add by wendy timestamp table
        private static final String CREATE_TIMESTAMP_TABLE_COMMAND = "CREATE TABLE Timestamp(" + "_id INTEGERPRIMARY KEY, " +
        		"create_date long,"+
        		"title TEXT,"+
        		"owner long,"+
        		"position INTEGER,"+
        		"userAccount Long NOT NULL DEFAULT 0);";
        
        //add by wendy timestamp table
        //add by wendy webstorage-account begin
        private static final String CREATE_WEBSTORAGEACCOUNT_TABLE_COMMAND = "CREATE TABLE WebAccount(" + "_id INTEGER PRIMARY KEY,"+
        		"account_id long,"+
        		"account_name TEXT,"+
        		"account_sha  TEXT,"+
        		"account_password TEXT);";
        //add by wendy webstorage-account end

        private static final String CREATE_PAGE_TABLE_COMMAND = "CREATE TABLE Page(" + "_id INTEGER PRIMARY KEY, " + // 0
                "owner long, " + // 1
                "content TEXT, " + // 2
                "created_date Long, " + // 3
                "modified_date Long, " + // 4
                "is_bookmark INTEGER, " + // 5
                "is_notified INTEGER, " + // 6
                "thumbnail TEXT, " + // 7
                "page_size INTEGER, " + // 8
                "color INTEGER, " + // 9
               // "style INTEGER);"; // 10
                //add by wendy begin++
               "style INTEGER," + //10
               "lastsync_modifytime Long NOT NULL DEFAULT 0," + //11
               "is_deleted INTERGER NOT NULL DEFAULT 0," + //12
               //"lastsync_owner long NOT NULL DEFAULT 0);"; //13
        		"lastsync_owner long NOT NULL DEFAULT 0," + //13
				"userAccount Long NOT NULL DEFAULT 0," + // 14
				// add by wendy end--
				// BEGIN: Better
				"version INTEGER DEFAULT 3," + //; // 15
				// END: Better
				//begin darwin
				"is_last_edit long NOT NULL DEFAULT 0," + //16
				//end  darwin
				//BEGIN: RICHARD
				MetaData.PageTable.IS_INDEXED+" INTEGER DEFAULT 0,"+//17
        		//END: RICHARD
                "template_type INTEGER DEFAULT 0,"+// 18 wendy allen++
				//BEGIN: RICHARD
				MetaData.PageTable.INDEX_LANGUAGE+" INTEGER DEFAULT 2,"+ //19
				//END: RICHARD
				// BEGIN: Shane_Wang@asus.com 2012-12-10
				MetaData.PageTable.LAST_SHA + " TEXT);";
				// END: Shane_Wang@asus.com 2012-12-10
        
		//Begin Add by Darwin_Yu@asus.com
        private static final String CREATE_DOODLE_TABLE_COMMAND = "CREATE TABLE Doodle(" + 
        		"_index INTEGER PRIMARY KEY, " + 
        		"modified_date long,"+
        		"lastsync_modifytime long,"+
        		"is_delete INTERGER NOT NULL DEFAULT 0," +
        		"_id INTEGER," + 
				// BEGIN: Shane_Wang@asus.com 2012-12-10
				MetaData.PageTable.LAST_SHA + " TEXT);";
				// END: Shane_Wang@asus.com 2012-12-10
        
        private static final String CREATE_ITEM_TABLE_COMMAND = "CREATE TABLE Item(" + 
        		"_index INTEGER PRIMARY KEY, " + 
        		"modified_date long,"+
        		"lastsync_modifytime long,"+
        		"is_delete INTERGER NOT NULL DEFAULT 0,"+
        		"_id INTEGER," +
				// BEGIN: Shane_Wang@asus.com 2012-12-10
				MetaData.PageTable.LAST_SHA + " TEXT);";
				// END: Shane_Wang@asus.com 2012-12-10
        
        private static final String CREATE_ATTACHMENT_TABLE_COMMAND = "CREATE TABLE Attachment(" + 
        		"_index INTEGER PRIMARY KEY, " + 
        		"file_name TEXT ,"+
        		"modified_date long,"+
        		"lastsync_modifytime long,"+
        		"is_delete INTERGER NOT NULL DEFAULT 0,"+
        		"_id INTEGER," +
				// BEGIN: Shane_Wang@asus.com 2012-12-10
				MetaData.PageTable.LAST_SHA + " TEXT);";
				// END: Shane_Wang@asus.com 2012-12-10
        
    	//End   Add by Darwin_Yu@asus.com

        private static final String CREATE_CLIPBOARD_TABLE_COMMAND = "CREATE TABLE Clipboard(" + "_id INTEGER PRIMARY KEY, " + // 0
                "data BOLB);"; // 1

        private static final String CREATE_MEMOBOARD_TABLE_COMMAND = "CREATE TABLE Memoboard(" + "_id INTEGER PRIMARY KEY, " + "widget_id LONG, " + // 1
                "book_id LONG, " + // 2
                "page_id LONG, " + // 3
                "memo_info BLOB, " + // 4
                "thumbnail TEXT, " + // 5
                "is_hidden INTEGER, " + // 6
                "book_name TEXT);"; // 7
        private static final String CREATE_INDEX_ON_BOOK_TABLE_COMMAND = "CREATE INDEX BookIndex on Book (created_date)";
        private static final String CREATE_INDEX_ON_PAGE_TABLE_COMMAND = "CREATE INDEX PageIndex on Page (owner)";

        public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	db.execSQL(CREATE_TODO_WIDGET_TABLE_COMMAND);//Allen for todo widget
        	//begin:clare
        	db.execSQL(CREATE_WIDGET_TABLE_COMMAND);
        	db.execSQL(CREATE_WIDGET_ITEM_TABLE_COMMAND);
        	//end:clare
            db.execSQL(CREATE_BOOK_TABLE_COMMAND);
            db.execSQL(CREATE_INDEX_ON_BOOK_TABLE_COMMAND);
            db.execSQL(CREATE_PAGE_TABLE_COMMAND);
            db.execSQL(CREATE_INDEX_ON_PAGE_TABLE_COMMAND);
            db.execSQL(CREATE_CLIPBOARD_TABLE_COMMAND);
            db.execSQL(CREATE_MEMOBOARD_TABLE_COMMAND);
            db.execSQL(CREATE_WEBSTORAGEACCOUNT_TABLE_COMMAND);
			db.execSQL(CREATE_TIMESTAMP_TABLE_COMMAND);//add by wendy 
			//Begin Add by Darwin_Yu@asus.com
			db.execSQL(CREATE_DOODLE_TABLE_COMMAND);
			db.execSQL(CREATE_ITEM_TABLE_COMMAND);
			db.execSQL(CREATE_ATTACHMENT_TABLE_COMMAND);
	    	//End   Add by Darwin_Yu@asus.com
            // Cursor cursor = db.rawQuery("select * from Book", null);
            // cursor.moveToFirst();
            // Log.d(TAG, "cursor.getColumnCount() = " + cursor.getColumnCount());
            // ArrayList<String> cols = new ArrayList<String>();
            // Collections.addAll(cols, cursor.getColumnNames());
            // if (cols.contains(MetaData.BookTable.FONT_SIZE)) {
            // Log.d(TAG, "[onCreate]has font_size field");
            // // db.execSQL("ALTER TABLE Book ADD font_size INTEGER DEFAULT 1");
            // }
            // else {
            // Log.d(TAG, "[onCreate]no font_size field");
            // }
            //
            // cursor.close();

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + MetaData.BOOK_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + MetaData.TODO_WIDGET_TABLE);//Allen
            //begin:clare
            db.execSQL("DROP TABLE IF EXISTS " + MetaData.WIDGET_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + MetaData.WIDGET_ITEM_TABLE);
            //end:clare
            onCreate(db);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            
            // BEGIN: Better
            // move to the front to avoid bad query while tables not exist
            Cursor cursor = null;
            
            //Begin Allen
            try{//SELECT COUNT(*) FROM sqlite_master where type='table' and name='DBInfo'
            	String sql = "select count(*) from sqlite_master where type ='table' and name ='todo_widget' ";
            	cursor = db.rawQuery(sql, null);
            	if(cursor.moveToNext()){
                    int count = cursor.getInt(0);
                    if(count<=0){
                    	 db.execSQL(CREATE_TODO_WIDGET_TABLE_COMMAND);
                    }
            	}
            }catch(Exception  e){
            	Log.v("Allen", e.toString());            	
            }finally {
			    if (cursor != null) //smilefish fix memory leak
			        cursor.close();
			}
            //End Allen
            
            //begin:clare
            
            try{//SELECT COUNT(*) FROM sqlite_master where type='table' and name='DBInfo'
            	String sql = "select count(*) from sqlite_master where type ='table' and name ='Widget' ";
            	cursor = db.rawQuery(sql, null);
            	if(cursor.moveToNext()){
                    int count = cursor.getInt(0);
                    if(count<=0){
                    	 db.execSQL(CREATE_WIDGET_TABLE_COMMAND);
                    }
            	}
            	cursor.close();//RICHARD FIX MEMORY LEAK
            }catch(Exception  e){
            	Log.v("Clare", e.toString());            	
            }finally {
			    if (cursor != null) //smilefish fix memory leak
			        cursor.close();
			}
            
            try{//SELECT COUNT(*) FROM sqlite_master where type='table' and name='DBInfo'
            	String sql = "select count(*) from sqlite_master where type ='table' and name ='Widget_Item' ";
            	cursor = db.rawQuery(sql, null);
            	if(cursor.moveToNext()){
                    int count = cursor.getInt(0);
                    if(count<=0){
                    	 db.execSQL(CREATE_WIDGET_ITEM_TABLE_COMMAND);
                    }
            	}
            	cursor.close();//RICHARD FIX MEMORY LEAK
            }catch(Exception  e){
            	Log.v("clare", e.toString());            	
            }finally {
			    if (cursor != null) //smilefish fix memory leak
			        cursor.close();
			}
            //end:clare
            
            //add by wendy end--       
            try{//SELECT COUNT(*) FROM sqlite_master where type='table' and name='DBInfo'
            	String sql = "select count(*) from sqlite_master where type ='table' and name ='WebAccount' ";
            	cursor = db.rawQuery(sql, null);
            	if(cursor.moveToNext()){
                    int count = cursor.getInt(0);
                    if(count<=0){
                    	 db.execSQL(CREATE_WEBSTORAGEACCOUNT_TABLE_COMMAND);//add by wendy 
                    }
            	}
            	cursor.close();//RICHARD FIX MEMORY LEAK

            }catch(Exception  e){
            	Log.v("wendy", e.toString());            	
            }finally {
			    if (cursor != null) //smilefish fix memory leak
			        cursor.close();
			}
            
			try{//SELECT COUNT(*) FROM sqlite_master where type='table' and name='DBInfo'
            	String sql = "select count(*) from sqlite_master where type ='table' and name ='Timestamp' ";
            	cursor = db.rawQuery(sql, null);
            	if(cursor.moveToNext()){
                    int count = cursor.getInt(0);
                    if(count<=0){
                    	 db.execSQL(CREATE_TIMESTAMP_TABLE_COMMAND);//add by wendy 
                    }
            	}
            	cursor.close();//RICHARD FIX MEMORY LEAK
            }catch(Exception  e){
            	Log.v("wendy", e.toString());            	
            }finally {
			    if (cursor != null) //smilefish fix memory leak
			        cursor.close();
			}
			
			//Begin Add by Darwin_Yu@asus.com
			try{//SELECT COUNT(*) FROM sqlite_master where type='table' and name='DBInfo'
            	String sql = "select count(*) from sqlite_master where type ='table' and name ='Doodle' ";
            	cursor = db.rawQuery(sql, null);
            	if(cursor.moveToNext()){
                    int count = cursor.getInt(0);
                    if(count<=0){
                    	 db.execSQL(CREATE_DOODLE_TABLE_COMMAND);
                    }
            	}
            	cursor.close();//RICHARD FIX MEMORY LEAK
            }catch(Exception  e){
            	Log.v("darwin", e.toString());            	
            }finally {
			    if (cursor != null) //smilefish fix memory leak
			        cursor.close();
			}
			
			try{//SELECT COUNT(*) FROM sqlite_master where type='table' and name='DBInfo'
            	String sql = "select count(*) from sqlite_master where type ='table' and name ='Item' ";
            	cursor = db.rawQuery(sql, null);
            	if(cursor.moveToNext()){
                    int count = cursor.getInt(0);
                    if(count<=0){
                    	 db.execSQL(CREATE_ITEM_TABLE_COMMAND);
                    }
            	}
            	cursor.close();//RICHARD FIX MEMORY LEAK
            }catch(Exception  e){
            	Log.v("darwin", e.toString());            	
            }finally {
			    if (cursor != null) //smilefish fix memory leak
			        cursor.close();
			}
			
			try{//SELECT COUNT(*) FROM sqlite_master where type='table' and name='DBInfo'
            	String sql = "select count(*) from sqlite_master where type ='table' and name ='Attachment' ";
            	cursor = db.rawQuery(sql, null);
            	if(cursor.moveToNext()){
                    int count = cursor.getInt(0);
                    if(count<=0){
                    	 db.execSQL(CREATE_ATTACHMENT_TABLE_COMMAND);
                    }
            	}
            	cursor.close();//RICHARD FIX MEMORY LEAK
            }catch(Exception  e){
            	Log.v("darwin", e.toString());            	
            }finally {
			    if (cursor != null) //smilefish fix memory leak
			        cursor.close();
			}
			
	    	//End   Add by Darwin_Yu@asus.com
			// END: Better
            
            cursor = db.rawQuery("select * from Book", null);
            cursor.moveToFirst();
            ArrayList<String> cols = new ArrayList<String>();
            Collections.addAll(cols, cursor.getColumnNames());
			//add by wendy begin++
            if((cols.contains(MetaData.BookTable.LASTSYNC_MODIFYTIME)== false))
    		{
            	db.execSQL("ALTER TABLE Book ADD lastsync_modifytime INTEGER DEFAULT 0");
               //Log.v("wendy","cant find modifytime");
    		}
			//add by wendy end---
            if (cols.contains(MetaData.BookTable.FONT_SIZE) == false) {
                db.execSQL("ALTER TABLE Book ADD font_size INTEGER DEFAULT 1");
            }
			//add by wendy begin++
            if((cols.contains(MetaData.BookTable.IS_DELETED)== false))
            {
            	//Log.v("wendy","cant find modifytime");
            	db.execSQL("ALTER TABLE Book ADD is_deleted INTEGER DEFAULT 0");
            } 
            if((cols.contains(MetaData.BookTable.USER_ACCOUNT))==false)
            {
            	db.execSQL("ALTER TABLE Book ADD userAccount Long DEFAULT 0");
            }
			//add by wendy end---  
            // BEGIN: Better
            if(!cols.contains(MetaData.BookTable.VERSION))
            {
            	db.execSQL("ALTER TABLE Book ADD version INTEGER DEFAULT 3");
            }
            // END: Better
            //BEGIN WENDY allen++
            if(!cols.contains(MetaData.BookTable.TEMPLATE))
            {
            	db.execSQL("ALTER TABLE Book ADD template INTEGER DEFAULT 0");
            }
            //END WENDY allen++
            cursor.close();
            //add by wendy begin++
            cursor = db.rawQuery("select * from Page", null);
            cursor.moveToFirst();
            ArrayList<String> colsTables = new ArrayList<String>();
            Collections.addAll(colsTables, cursor.getColumnNames());
            if((colsTables.contains(MetaData.PageTable.LASTSYNC_MODIFYTIME)== false))
    		{
            	db.execSQL("ALTER TABLE Page ADD lastsync_modifytime INTEGER DEFAULT 0");
            //	Log.v("wendy","cant find modifytime");
    		}
            if((colsTables.contains(MetaData.PageTable.IS_DELETED)== false))
            {
            	//Log.v("wendy","cant find modifytime");
            	db.execSQL("ALTER TABLE Page ADD is_deleted INTEGER DEFAULT 0");
            } 
            if (colsTables.contains(MetaData.PageTable.LASTSYNC_OWNER) == false) {
                db.execSQL("ALTER TABLE Page ADD lastsync_owner INTEGER DEFAULT 0");
            }
            if((colsTables.contains(MetaData.PageTable.USER_ACCOUNT)) == false)
            {
            	db.execSQL("ALTER TABLE Page ADD userAccount Long DEFAULT 0");
            }
            // BEGIN: Better
            if(!colsTables.contains(MetaData.PageTable.VERSION))
            {
            	db.execSQL("ALTER TABLE Page ADD version INTEGER DEFAULT 3");
            }
            // END: Better
            
			
            //begin darwin
            if(!colsTables.contains(MetaData.PageTable.IS_LAST_EDIT))
            {
            	db.execSQL("ALTER TABLE Page ADD is_last_edit long DEFAULT 0");
            }
            //begin darwin
            
            //BEGIN: RICHARD
            if(!colsTables.contains(MetaData.PageTable.IS_INDEXED))
            {
            	db.execSQL("ALTER TABLE Page ADD " + MetaData.PageTable.IS_INDEXED + " INTEGER DEFAULT 0");
            }
            //END: RICHARD
            
 		   //BEGIN WENDY allen++
            if(!colsTables.contains(MetaData.PageTable.TEMPLATE))
            {
            	db.execSQL("ALTER TABLE Page ADD template_type INTEGER DEFAULT 0");
            }
            //END WENDY allen++
            
            //BEGIN: RICHARD
            if(!colsTables.contains(MetaData.PageTable.INDEX_LANGUAGE))
            {
            	db.execSQL("ALTER TABLE Page ADD " + MetaData.PageTable.INDEX_LANGUAGE + " INTEGER DEFAULT 0");           	
            }
            //END: RICHARD
            //Begin Allen
            if(!cols.contains(MetaData.BookTable.INDEX_LANGUAGE))
            {         	
            	db.execSQL("ALTER TABLE Book ADD index_language INTEGER DEFAULT "+MetaData.INDEX_LANGUAGE_DEFAULT);
            	db.execSQL("UPDATE Page SET is_indexed = 0 WHERE index_language <> "+MetaData.INDEX_LANGUAGE_DEFAULT);
            }
            //End Allen
			
            // BEGIN: Shane_Wang@asus.com 2012-12-16
            if(!colsTables.contains(MetaData.PageTable.LAST_SHA))
            {
            	db.execSQL("ALTER TABLE Page ADD " + MetaData.PageTable.LAST_SHA + " TEXT");
            }
            // END: Shane_Wang@asus.com 2012-12-16
            
          //Begin Darwin
            if(!cols.contains(MetaData.BookTable.INDEX_COVER))
            {         	
            	db.execSQL("ALTER TABLE Book ADD index_cover INTEGER DEFAULT 0");
            }
            if(!cols.contains(MetaData.BookTable.COVER_MODIFYTIME))
            {         	
            	db.execSQL("ALTER TABLE Book ADD cover_modifytime Long DEFAULT 0");
            }
            //End Darwin
            cursor.close();    
            //darwin begin
            cursor = db.rawQuery("select * from Timestamp", null);
            cursor.moveToFirst();
            ArrayList<String> colsTimeStamp = new ArrayList<String>();
            Collections.addAll(colsTimeStamp, cursor.getColumnNames());
            
            if((colsTimeStamp.contains(MetaData.TimestampTable.USER_ACCOUNT)== false))
    		{
            	db.execSQL("ALTER TABLE Timestamp ADD userAccount Long DEFAULT 0");
    		}
            cursor.close();
            //end darwin   
            
            //begin:clare
            cursor = db.rawQuery("select * from Widget", null);
            cursor.moveToFirst();
            ArrayList<String> colsWidget = new ArrayList<String>();
            Collections.addAll(colsWidget, cursor.getColumnNames());
            
            if((colsWidget.contains(MetaData.WidgetTable.WIDGET_ID)== false))
    		{
            	db.execSQL("ALTER TABLE Widget ADD widget_id Integer");
    		}
            if((colsWidget.contains(MetaData.WidgetTable.WIDGET_MODE)== false))
    		{
            	db.execSQL("ALTER TABLE Widget ADD widget_mode Integer DEFAULT 0");
    		}
            cursor.close();
            
            cursor = db.rawQuery("select * from Widget_Item", null);
            cursor.moveToFirst();
            ArrayList<String> colsWidgetItem = new ArrayList<String>();
            Collections.addAll(colsWidgetItem, cursor.getColumnNames());
            
            if((colsWidgetItem.contains(MetaData.WidgetItemTable.ITEM_ALLBOOK_ALLTEMPLATE)== false))
    		{
            	db.execSQL("ALTER TABLE Widget ADD item_allbook_alltemplate Integer ");
    		}
            if((colsWidgetItem.contains(MetaData.WidgetItemTable.ITEM_BOOK_ID)== false))
    		{
            	db.execSQL("ALTER TABLE Widget ADD item_book_id Long ");
    		}
            if((colsWidgetItem.contains(MetaData.WidgetItemTable.ITEM_ID)== false))
    		{
            	db.execSQL("ALTER TABLE Widget ADD item_id Integer  ");
    		}
            if((colsWidgetItem.contains(MetaData.WidgetItemTable.ITEM_LOCK_STATE)== false))
    		{
            	db.execSQL("ALTER TABLE Widget ADD item_lock_state Integer  ");
    		}
            if((colsWidgetItem.contains(MetaData.WidgetItemTable.ITEM_MODE)== false))
    		{
            	db.execSQL("ALTER TABLE Widget ADD item_mode Integer ");
    		}
            if((colsWidgetItem.contains(MetaData.WidgetItemTable.ITEM_ORDER_ID)== false))
    		{
            	db.execSQL("ALTER TABLE Widget ADD item_order_id Integer ");
    		}
            if((colsWidgetItem.contains(MetaData.WidgetItemTable.ITEM_PAGE_ID)== false))
    		{
            	db.execSQL("ALTER TABLE Widget ADD item_page_id Long  ");
    		}
            if((colsWidgetItem.contains(MetaData.WidgetItemTable.ITEM_WIDGET_ID)== false))
    		{
            	db.execSQL("ALTER TABLE Widget ADD item_widget_id Integer ");
    		}
            cursor.close();
            //end:clare
            
            //Begin Allen
            cursor = db.rawQuery("select * from todo_widget", null);
            cursor.moveToFirst();
            ArrayList<String> colsToDoWidget = new ArrayList<String>();
            Collections.addAll(colsToDoWidget, cursor.getColumnNames());
            
            if((colsToDoWidget.contains(MetaData.ToDoWidgetTable.WIDGET_ID)== false))
    		{
            	db.execSQL("ALTER TABLE todo_widget ADD widget_id Integer");
    		}
            if((colsToDoWidget.contains(MetaData.ToDoWidgetTable.ADAPTER_TYPE)== false))
    		{
            	db.execSQL("ALTER TABLE todo_widget ADD adapter_type smallint DEFAULT 0");
    		}
            if((colsToDoWidget.contains(MetaData.ToDoWidgetTable.BOOK_ID)== false))
    		{
            	db.execSQL("ALTER TABLE todo_widget ADD book_id Long DEFAULT -1");
    		}
            if((colsToDoWidget.contains(MetaData.ToDoWidgetTable.SORT_BY)== false))
    		{
            	db.execSQL("ALTER TABLE todo_widget ADD sort_by smallint DEFAULT 0");
    		}
            cursor.close();
            //End Allen
        }
    }

}
