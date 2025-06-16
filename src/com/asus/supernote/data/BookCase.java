package com.asus.supernote.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Environment;
import com.asus.supernote.EncryptAES;
import com.asus.supernote.R;
import com.asus.supernote.RestoreDBActivity;
import com.asus.supernote.picker.NoteBookPageGridFragment;
import com.asus.supernote.picker.NoteBookPickerActivity;
import com.asus.supernote.widget.WidgetService;

public class BookCase implements AsusFormat {
    public static final String TAG = "BookCase";
    private static final String PREFERNECE_NAME = "SuperNote";
    public static final long NO_SELETED_BOOK = 0L;
    public static final String DIR = Environment.getExternalStorageDirectory() + "/AsusSuperNote/";

    // Data
    private volatile static BookCase mInstance;
    private Long mImportedBook = 0L;
    private Context mContext;
    private List<NoteBook> mBookList;
    private List<NoteBook> mSelectedBookList;
    private long mCurrBookId;
    private ContentResolver mContentResolver;
    private Resources mResources;
    private SharedPreferences mPreference;
    private SharedPreferences.Editor mPreferenceEditor;

    public static final String[] RESTORE_FILE_FILTER = new String[] { "application/snb" };
    public static final String[] IMPORT_FILE_FILTER = new String[] { "application/sne" };

    // Constructor of BookCase
    private BookCase(Context context) {
        mInstance = this;
        mContext = context.getApplicationContext();
        loadData(mContext);
        removeInvalidData();
        getUserAccountId();
    }
    
	// BEGIN: Wendy
    private void getUserAccountId()
    {
    	mPreference = mContext.getSharedPreferences(PREFERNECE_NAME, Context.MODE_MULTI_PROCESS);
    	
    	String RAsusAccount = mContext.getResources().getString(R.string.pref_AsusAccount);
		String RAsusPassword = mContext.getResources().getString(R.string.pref_AsusPassword);
        String RAsusKey =  mContext.getResources().getString(
                R.string.pref_AsusKey);
        String RAsusIV =  mContext.getResources().getString(
                R.string.pref_AsusIV);
		String account = mPreference.getString(RAsusAccount, "");
        String keyStr = mPreference.getString(RAsusKey, "");
        String ivStr = mPreference.getString(RAsusIV, "");

		try{
			account = EncryptAES.decrypt(keyStr, ivStr, account );
		}
		catch(Exception e){}
		Cursor cor = mContext.getContentResolver().query(MetaData.WebAccountTable.uri, null, "account_name = ?", new String[]{account}, null);
    	if(cor.getCount()>0)
    	{
    		cor.moveToFirst();
    		MetaData.CurUserAccount = cor.getLong(MetaData.WebAccountTable.INDEX_ID);    		
    	}
    	cor.close();
    }
	// END: Wendy

    public void loadData(Context context) {
        mBookList = new CopyOnWriteArrayList<NoteBook>();
        mResources = mContext.getResources();
        mPreference = mContext.getSharedPreferences(PREFERNECE_NAME, Context.MODE_MULTI_PROCESS);
        mPreferenceEditor = mPreference.edit();

        File file = new File(DIR);
        if (file.exists() == false) {
            file.mkdir();
        }
        else {
            File dataDir = new File(MetaData.DATA_DIR);
            File memoDir = new File(MetaData.MEMO_DIR);
            dataDir.mkdir();
            memoDir.mkdir();
            try {
                File noMediaFile = new File(dataDir, ".nomedia");
                noMediaFile.createNewFile();
                noMediaFile = new File(memoDir, ".nomedia");
                noMediaFile.createNewFile();
            }
            catch (IOException e) {

            }
        }

        mContentResolver = mContext.getContentResolver();
        mCurrBookId = mPreference.getLong(mResources.getString(R.string.pref_default_selected_book), NO_SELETED_BOOK);
        //add by jason for data restore
        SharedPreferences dbSharedPreferences = mContext.getSharedPreferences(RestoreDBActivity.PER_NAME, Context.MODE_MULTI_PROCESS);
        boolean restoreDB = dbSharedPreferences.getBoolean(RestoreDBActivity.PER_KEY, false);
        boolean isFirstLoad = mPreference.getBoolean(mResources.getString(R.string.pref_first_load), true);
        if (isFirstLoad&&!restoreDB) {

        }
        else {
        	if (restoreDB) {
	            Editor ed = dbSharedPreferences.edit();
	            ed.putBoolean(RestoreDBActivity.PER_KEY,false);
	            ed.commit();
            }
            Cursor cursor = mContentResolver.query(MetaData.BookTable.uri, null, null, null, null);
            cursor.moveToFirst();
            long selectedBookId = mPreference.getLong(mResources.getString(R.string.pref_default_selected_book), NO_SELETED_BOOK);
            if (selectedBookId != NO_SELETED_BOOK) {
                setCurrentBook(selectedBookId);
            }
            else if (cursor.getCount() > 0) {
                setCurrentBook(cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE));
            }
            while (!cursor.isAfterLast()) {
                NoteBook notebook = new NoteBook(mContext);
                notebook.load(cursor);
                mBookList.add(notebook);
                cursor.moveToNext();
            }
            cursor.close();

        }

    }

    // Singlton pattern
    public synchronized static final BookCase getInstance(Context context) {
        if (context == null) {
            return null;
        }
        if (mInstance == null) {
            mInstance = new BookCase(context);
        }
        return mInstance;
    }

	// BEGIN: Wendy
    public void addNewBookFromWebPage(NoteBook notebook)
    {
        if (notebook != null && mBookList != null) {
            notebook.saveNoModify();
        	if(mBookList.size() == 0)
        	{
        		mCurrBookId = notebook.getCreatedTime();
        	}           
            mBookList.add(notebook);
        }
    }
    
    public void addBook(NoteBook noteBook, int index) {
        if (noteBook != null && mBookList != null) {
            noteBook.insert();
            mCurrBookId = noteBook.getCreatedTime();
            mBookList.add(index, noteBook);
        }
    }
	// END: Wendy
    
    // BEGIN: Better
    public void addBookNoSetCurrent(NoteBook noteBook, int index) {
        if (noteBook != null && mBookList != null) {
            noteBook.insert();
            mBookList.add(index, noteBook);
        }
    }
    // END: Better
    
    public void addNewBook(NoteBook notebook) {
        if (notebook != null && mBookList != null) {
            notebook.save();
            mCurrBookId = notebook.getCreatedTime();
            mBookList.add(notebook);
        }
    }
	
	//add by wendy 0406 begin++
    public void removeNoteBookById(Long id)
    {
    	NoteBook notebook = getNoteBook(id);
    	if(notebook!=null)
    		mBookList.remove(notebook);
    }
    
    public void addNewBookFromWeb(NoteBook notebook) {
        if (notebook != null && mBookList != null) {    
        	removeNoteBookById(notebook.getCreatedTime());
            mBookList.add(notebook);
        }
    }
    //add by wendy 0406 end---

    public void addNewPrivateBook(NoteBook notebook) {
        if (notebook != null && mBookList != null) {
        notebook.save();
		//wendy lock  begin
        boolean islocked = NoteBookPickerActivity.islocked();
        if(!islocked)
        {
        	mCurrBookId = notebook.getCreatedTime();
        }
		//wendy lock end
        mBookList.add(notebook);
    }
    }

    
    //BEGIN: RICHARD
    public List<NoteBook> getBookList()
    {
    	return mBookList;
    }
    //END: RICHARD
    
    //Begin Allen
    public List<NoteBook> getToDoTemplateBookList(){
    	ArrayList<NoteBook> bookList = new  ArrayList<NoteBook>();
    	for(NoteBook book: mBookList){
    		if(book.getTemplate() == MetaData.Template_type_todo 
			        && (book.getUserId() == MetaData.CurUserAccount || book.getUserId() == 0)){//emmanual
    			if(!book.getIsLocked()){//hide locked book
    				bookList.add(book);
    			}
    		}
    	}
    	return bookList;
    }
    //End Allen
    
    public void delBooks(List<Long> list) {
        for (Long createdTime : list) {
            NoteBook notebook = getNoteBook(createdTime);
            if (notebook != null) {
	            notebook.deleteDir(new File(MetaData.DATA_DIR, Long.toString(createdTime)));
	            String[] selectionArgs = new String[] { Long.toString(createdTime) };
	            mContentResolver.delete(MetaData.BookTable.uri, "created_date = ?", selectionArgs);
	            mContentResolver.delete(MetaData.PageTable.uri, "owner = ?", selectionArgs);
	            int index = getNoteBookIndex(createdTime);
	            index = (index > 0) ? (index - 1) : (index + 1);
	            if ((mBookList.size() - 1) > 0) {
	                setCurrentBook(mBookList.get(index).getCreatedTime());
	            }
	            else {
	                setCurrentBook(BookCase.NO_SELETED_BOOK);
	            }
	            mBookList.remove(notebook);
            }
        }
    }

    public NoteBook getCurrentBook() {
        return getNoteBook(mCurrBookId);
    }

    public Long getSelectedPrivateBookId() {
        return mCurrBookId;//mSelectedPrivateBook;
    }

    //end wendy

    public void setImportedBookId(Long id) {
        mImportedBook = id;
        //begin wendy fix TTbug 263759
        mPreferenceEditor.putLong(mResources.getString(R.string.pref_imported_bookid), id);
        mPreferenceEditor.commit();
       //end wendy fix TTbug 263759
    }

    public Long getImportedBookId() {
        return mImportedBook;
    }

    public NoteBook getImportedNoteBook() {
    	//begin wendy fix TTbug 263759
    	 if(mImportedBook == 0L)
    	 {
    		 mImportedBook =  mPreference.getLong(mResources.getString(R.string.pref_imported_bookid), NO_SELETED_BOOK);
    	 }
    	//end wendy fix TTbug 263759
        return getNoteBook(mImportedBook);
    }

    public void setCurrentBook(long id) {
        mCurrBookId = id;
        mPreferenceEditor.putLong(mResources.getString(R.string.pref_default_selected_book), mCurrBookId);
        mPreferenceEditor.commit();
    }

    public long getCurrentBookId() {
        return mCurrBookId;
    }

    public int getTotalBookNum() {
        return (mBookList != null) ? mBookList.size() : 0;
    }

    public NoteBook getNoteBook(long createdTime) {
        for (NoteBook nb : mBookList) {
            if (nb.getCreatedTime() == createdTime) {
                return nb;
            }
        }
        return null;
    }

    public NoteBook getNoteBook(int position) {
        return mBookList.get(position);
    }

    public int getNoteBookIndex(long createdTime) {
        int index = 0;
        for (NoteBook nb : mBookList) {
            if (nb.getCreatedTime() == createdTime) {
                return index;
            }
            index = index + 1;
        }
        index = -1;
        return index;
    }

    private static final int FIRST_POSITION = 0;
    private static final int ONLY_ONE_BOOK = 1;
	//begin wendy
    public long getNextUnlockedBookID(boolean locked)
    {
    	String selection = null;
    	if(locked)
    	{
    		selection = "(is_locked = 0)AND((userAccount = 0) OR (userAccount = ?))";
    	}else
    	{
    		selection = "(userAccount = 0) OR (userAccount = ?)";
    	}
    			
    	
          Cursor cursor = mContentResolver.query(MetaData.BookTable.uri, null, selection, new String[]{ Long.toString(MetaData.CurUserAccount)}, "title");
         
          if(cursor.getCount() == 0)
          {
        	  cursor.close();//RICHARD FIX MEMORY LEAK
        	  return 0;
          }else
          {
        	  cursor.moveToFirst();
        	  Long bookid = cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
        	  cursor.close();
        	  return bookid;
          }        
           	
    }
	//end wendy
    public NoteBook getNextBook(NoteBook currentBook) {
        long currentBookId = currentBook.getCreatedTime();
        Cursor cursor = null;
    	if(NoteBookPickerActivity.getIsLock())
    	{
    		cursor = mContentResolver.query(MetaData.BookTable.uri, null, "(is_locked = 0)AND ((userAccount = 0) OR (userAccount = ?))", new String[]{ Long.toString(MetaData.CurUserAccount)}, "userAccount");
    	}
    	else
    	{
    		cursor = mContentResolver.query(MetaData.BookTable.uri, null, "(userAccount = 0) OR (userAccount = ?)", new String[]{ Long.toString(MetaData.CurUserAccount)}, "userAccount");
    	}
        
        cursor.moveToFirst();
        int count = cursor.getCount();
        while (!cursor.isAfterLast()) {
            if (cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE) == currentBookId) {
                break;
            }
            cursor.moveToNext();
        }
        if (cursor.isAfterLast()) {

        	cursor.close();//RICHARD ADD FOR MEMORY LEAK
            return null;
        }
        else if (cursor.getPosition() == FIRST_POSITION) {
            if (cursor.getCount() == ONLY_ONE_BOOK) {
            	cursor.close();//RICHARD ADD FOR MEMORY LEAK
                return null;
            }
            else {
                cursor.moveToNext();
                long tempTime = cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
                cursor.close();//RICHARD ADD FOR MEMORY LEAK
                return getNoteBook(tempTime);
            }
        }
        else {
            cursor.moveToPosition((cursor.getPosition() - 1));
            long tempTime = cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
            cursor.close();//RICHARD ADD FOR MEMORY LEAK
            return getNoteBook(tempTime);
        }
    }

    public void removeBookFromList(NoteBook book) {
        mBookList.remove(book);
    }

    public void removeInvalidData() {
        File file = new File(MetaData.DATA_DIR);
        String[] totalFileList = file.list();
        if (totalFileList == null) {
            return;
        }
        List<Long> bookIds = new ArrayList<Long>();
        for (NoteBook book : mBookList) {
            bookIds.add(book.getCreatedTime());
        }
        for (String f : totalFileList) {
            Long id = 0L;
            try {
                id = Long.valueOf(f);
            }
            catch (NumberFormatException e) {}
            if (bookIds.contains(id) == false && f.contains(".nomedia") == false) {
                File dir = new File(file, f);
                if (dir.exists()) {
                    deleteDir(dir);
                }
            }
        }
    }

    public void deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] subDirs = dir.list();
            if (subDirs != null) {
            	for (String sub : subDirs) {
                    deleteDir(new File(dir, sub));
                }
            }
        }
        dir.delete();
    }

    public void setSelectedBookList(List<NoteBook> list) {
        mSelectedBookList = list;
    }

    // TODO Asus Format
    @Override
    public void itemSave(AsusFormatWriter afw) throws IOException {
        afw.writeByteArray(AsusFormat.SNF_BKCASEPROP_BEGIN, null, 0, 0);
        for (NoteBook book : mSelectedBookList) {
            book.itemSave(afw);
        }
        afw.writeByteArray(SNF_BKCASEPROP_END, null, 0, 0);
    }

    //BEGIN: RICHARD
    public void itemLoadFromTutorial(AsusFormatReader afr) throws Exception {
        AsusFormatReader.Item item = null;
        for (item = afr.readItem(); item != null; item = afr.readItem()) {
            switch (item.getId()) {
                case AsusFormat.SNF_BKCASEPROP_BEGIN:
                    break;
                case AsusFormat.SNF_BKPROP_BEGIN:
                    NoteBook book = new NoteBook(mContext);
                    book.itemLoad(afr);
                    book.setIndexLanguage(NoteBookPickerActivity.sLanguageHelper.getRecordIndexLaguage());//different with itemLoad 
                    if (book.getIsLocked()) {
                        addNewPrivateBook(book);
                    }
                    else {
                        addNewBook(book);
                    }
                    setImportedBookId(book.getCreatedTime());
                    break;
                case AsusFormat.SNF_BKCASEPROP_END:
                    break;
                default:
                    break;
            }
        }
    }
    //END: RICHARD

    @Override
    public void itemLoad(AsusFormatReader afr) throws Exception {
        AsusFormatReader.Item item = null;
        for (item = afr.readItem(); item != null; item = afr.readItem()) {
            switch (item.getId()) {
                case AsusFormat.SNF_BKCASEPROP_BEGIN:
                    break;
                case AsusFormat.SNF_BKPROP_BEGIN:
                    NoteBook book = new NoteBook(mContext);
                    book.itemLoad(afr);
                    if (book.getIsLocked()) {
                        addNewPrivateBook(book);
                    }
                    else {
                        addNewBook(book);
                    }
                    setImportedBookId(book.getCreatedTime());
                    break;
                case AsusFormat.SNF_BKCASEPROP_END:
                    break;
                default:
                    break;
            }
        }
    }
    
	//end wendy
    
    //BEGIN: RICHARD
    public long getNextOrPrevBookID(long currentBookId,Boolean isNextflag) {
        Cursor cursor = null;
        long res = -1L;
        long tempID = -1L;

    		cursor = mContentResolver.query(MetaData.BookTable.uri, null,
    				WidgetService.NOT_SHOW_LOCK_BOOK?"(is_locked = 0)AND ((userAccount = 0) OR (userAccount = ?))":"(userAccount = 0) OR (userAccount = ?)",//  Modify for widget not to show lock book
    				new String[]{ Long.toString(MetaData.CurUserAccount)}, 
    				NoteBookPageGridFragment.SORT_BY_PAGE);
        
    	if(cursor == null)
    	{
    		return res;
    	}else if(cursor.getCount() == 0)
    	{
    		cursor.close();
    		return res;
    	}
    	
    	
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {    
        	tempID = cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
            if (tempID == currentBookId) {
                if(isNextflag)
                {
                	res = tempID;
            		cursor.moveToNext();
            		if(!cursor.isAfterLast())
            		{
            			res = cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
            		}
                }
                break;
            }
            else
            {
	            res = tempID;
	            cursor.moveToNext();
            }
        }

        cursor.close();
        return res;
    }
    //END: RICHARD
}
