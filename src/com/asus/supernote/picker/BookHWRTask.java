package com.asus.supernote.picker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Editable;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.indexservice.NoteItemFile;
import com.asus.supernote.inksearch.AsusInputRecognizer;
import com.asus.supernote.ui.CoverHelper;

public class BookHWRTask extends AsyncTask<Void, Void, Void> {
    public static final String TAG = "BooKHWRTask";
    private Activity mActivity;
    private BookCase mBookcase;

    private boolean mIsTaskRunning = false;
    private int mCount = 0;
    private NoteBook mSourceNoteBook;
    private ProgressBar mProgressBar;
    private Dialog mDialog;
    private TextView mTextView;
    
    private boolean mIsCanceling = false;
    private ContentResolver mContentResolver;
    private Handler handler = new Handler();//Allen

    public BookHWRTask(Activity activity, NoteBook book, ProgressBar pb,TextView tv) {
        mActivity = activity;
        mBookcase = BookCase.getInstance(mActivity);
        mContentResolver = activity.getContentResolver();
        mSourceNoteBook = book;
        mProgressBar = pb;
        mTextView = tv;
        
        mIsCanceling = false;
    }

    public boolean isTaskRunning() {
        return mIsTaskRunning;
    }
    
    public void setDialog(Dialog dialog)
    {
    	mDialog = dialog;
    }
    
    public void cancelHWRTask()
    {
    	mIsCanceling = true;
    	publishProgress();
    }

    public void doDeleteBook(Long bookId)
    {
    	publishProgress();
		NoteBook notebook = mBookcase.getNoteBook(bookId);
		notebook.deleteDir(new File(MetaData.DATA_DIR, Long
				.toString(bookId)));

		String selection =  null;

		Cursor cursor = mContentResolver.query(MetaData.BookTable.uri,
				null, selection, null, null);
		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			int index = cursor.getPosition();
			if (cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE) == bookId
					.longValue()) {
				boolean b = (index > 0) ? cursor.moveToPrevious() : cursor
						.moveToNext();
				if (b == false) {
					// begin wendy
					mBookcase.setCurrentBook(BookCase.NO_SELETED_BOOK);
					break;
				} else {
					long id = cursor
							.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
					// begin wendy
					mBookcase.setCurrentBook(id);
					break;
				}
			}
			cursor.moveToNext();
		}
		cursor.close();
		publishProgress();
		String[] selectionArgs = new String[] { Long.toString(bookId) };

		ContentValues cvbook = new ContentValues();
		cvbook.put(MetaData.BookTable.IS_DELETED, 1);
		cvbook.put(MetaData.BookTable.MODIFIED_DATE,
				System.currentTimeMillis());
		mContentResolver.update(MetaData.BookTable.uri, cvbook,
				"created_date = ?", selectionArgs);

		publishProgress();
		ContentValues cvpage = new ContentValues();
		cvpage.put(MetaData.PageTable.IS_DELETED, 1);
		mContentResolver.update(MetaData.PageTable.uri, cvpage,
				"owner = ?", selectionArgs);
		
		publishProgress();

		mBookcase.removeBookFromList(notebook);
		publishProgress();
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mIsTaskRunning = true;

    }
    
    private void turnBookToVersion3(NoteBook oldNoteBook)
    {
		// transfer version 1 book to version 3
		if (oldNoteBook.getVersion() == 1) {
			PageDataLoader loader = PageDataLoader
					.getInstance(mActivity);
			int numPage = oldNoteBook.getTotalPageNumber();
			for (int i = 0; i < numPage; i++) {
				long pageId = oldNoteBook.getPageOrder(i);
				NotePage page = oldNoteBook.getNotePage(pageId);
				if ((page != null) && (page.getVersion() == 1)) {
					loader.load(page);
					page.save(loader.getAllNoteItems(), loader.getDoodleItem());//Allen
				}
				publishProgress();
			}
			oldNoteBook.setVersion(3);
			File file = new File(MetaData.DATA_DIR
					+ oldNoteBook.getCreatedTime(), "paintbook");
			if (file.exists()) {
				file.delete();
			}
		}
    }

    @Override
    protected Void doInBackground(Void... params) {  
    	mProgressBar.setMax(mSourceNoteBook.getTotalPageNumber() * 4 + 1);
    	publishProgress();    	
    	//step 1 copy all pages
		final NoteBook book = new NoteBook(mActivity.getApplicationContext());
		book.setPageSize(mSourceNoteBook.getPageSize());
		book.setBookColor(mSourceNoteBook.getBookColor());
		book.setGridType(mSourceNoteBook.getGridType());
		book.setIsLocked(mSourceNoteBook.getIsLocked());
		book.setVersion(mSourceNoteBook.getVersion());
		book.setTitle(mSourceNoteBook.getTitle() + "_" + mActivity.getString(R.string.hwr_book_name_suffix));//"_duplicated"); Richard change to recognized
		book.setTemplate(mSourceNoteBook.getTemplate());//Allen
		book.setCoverIndex(mSourceNoteBook.getCoverIndex());//darwin
		book.setIndexLanguage(mSourceNoteBook.getIndexLanguage());//RICHARD
		book.setCoverModifyTime(mSourceNoteBook.getCoverModifyTime());//darwin
		mBookcase.addNewBook(book);
		for(Long item : mSourceNoteBook.getPageOrderList())
		{
			book.copyOnePagesFrom(mSourceNoteBook,item);
			if(mIsCanceling)
			{
				doDeleteBook(book.getCreatedTime());
				return null;
			}
			else
			{
				mCount++;
				publishProgress();
			}
			
		}
		//darwin
		if(book.getCoverIndex() >= 1)
		{
			if(book.getCoverIndex() == 1)
			{
				book.copyPicFrom(mSourceNoteBook);
			}
			book.copyCoverFrom(mSourceNoteBook);
		}
		//darwin
		
		mCount = mSourceNoteBook.getTotalPageNumber();
		//step2 update to version 3
    	turnBookToVersion3(book);

    	mCount = mSourceNoteBook.getTotalPageNumber() * 2;
		if(mIsCanceling)
		{
			doDeleteBook(book.getCreatedTime());
			return null;
		}
		else
		{
			mCount++;
			publishProgress();
		}
    	//step3 HWR
		for(Long item : book.getPageOrderList())
		{
			//darwin
			if(book.getTemplate() == MetaData.Template_type_normal || book.getTemplate() == MetaData.Template_type_blank)
			{
			//darwin
				NoteItemFile noteItemFile = new NoteItemFile(mActivity.getApplicationContext(),book.getCreatedTime(),item);
				NoteItem[] noteItems = noteItemFile.loadAllNoteItem();
				
				if(noteItems ==  null ||noteItems.length <2)
				{
					continue;
				}
	    		try
	    		{			
					Editable et = noteItemFile.loadNoteEditText(noteItems);
					AsusInputRecognizer asusInputRecognizer = new AsusInputRecognizer();
					asusInputRecognizer.prepareUnstructuredInputRecognizer();
		    		int language = mActivity.getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
		    				.getInt(mActivity.getResources().getString(R.string.pref_index_language), 0);
		
		    			asusInputRecognizer.loadResource(language);
		
		    		
					asusInputRecognizer.getHWRResult(et,0,et.length(),noteItemFile.getOrderedNoteHandWriteItems(noteItems));
					
					NoteItem[] itemSave = noteItemFile.getNoteItemFromEditable(et,0,et.length());
					
					noteItemFile.saveNoteItems(mActivity.getApplicationContext(), noteItemFile.getASCOrderedNoteItems(itemSave));
	      		}catch(Exception e)
	    		{
	      			mIsCanceling = true;
	    		}
	    	//darwin
			}
			else
			{
				NoteItemFile noteItemFile = new NoteItemFile(mActivity.getApplicationContext(),book.getCreatedTime(),item);
				ArrayList<NoteItemArray> noteItems = noteItemFile.loadAllNoteAndTemplateItems();
				
				if(noteItems ==  null ||noteItems.size() <2)
				{
					continue;
				}
	    		try
	    		{	
	    			ArrayList<NoteItemArray> noteItemsSave = new ArrayList<NoteItemArray>();
	    			for (NoteItemArray items : noteItems) 
					{
	    				Editable et = noteItemFile.loadNoteEditText(items.getNoteItemArray());
						AsusInputRecognizer asusInputRecognizer = new AsusInputRecognizer();
						asusInputRecognizer.prepareUnstructuredInputRecognizer();
			    		int language = mActivity.getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
			    				.getInt(mActivity.getResources().getString(R.string.pref_index_language), 0);
			
			    			asusInputRecognizer.loadResource(language);
			
			    		
						asusInputRecognizer.getHWRResult(et,0,et.length(),noteItemFile.getOrderedNoteHandWriteItems(items.getNoteItemArray()));
						
						NoteItem[] itemSave = noteItemFile.getNoteItemFromEditable(et,0,et.length());
						ArrayList<NoteItem> al = new ArrayList<NoteItem>();
						for(int i = 0; i < itemSave.length;i++)
						{
							al.add(itemSave[i]);
						}
						noteItemsSave.add(new NoteItemArray(al,items.getTemplateItemType()));
					}
	    			
					noteItemFile.saveNoteItemsAndTemplate(mActivity.getApplicationContext(), noteItemFile.getASCOrderedNoteItems(noteItemsSave));
	      		}catch(Exception e)
	    		{
	      			mIsCanceling = true;
	    		}
				
			}
    		//darwin
			if(mIsCanceling)
			{
				doDeleteBook(book.getCreatedTime());
				return null;
			}
			else
			{
				mCount++;
				publishProgress();
			}
		}
		
		mCount = mSourceNoteBook.getTotalPageNumber() * 3;
		//step4 thumbnail
		final PageDataLoader loader = PageDataLoader.getInstance(mActivity);
		int numPage = book.getTotalPageNumber();
		for (int i = 0; i < numPage; i++) {
			long pageId = book.getPageOrder(i);
			final NotePage page = book.getNotePage(pageId);

			//Begin Allen
			handler.post(new Runnable(){

				@Override
				public void run() {
					genThumb(
							loader,
							page,
							book.getPageSize() == MetaData.PAGE_SIZE_PHONE);					
				}
				
			});
			//End Allen
			if(mIsCanceling)
			{
				doDeleteBook(book.getCreatedTime());
				return null;
			}
			else
			{
				mCount++;
				publishProgress();
			}
		}
		mCount = mSourceNoteBook.getTotalPageNumber() * 4;
		publishProgress();
		//step 5 gen book thumb
		handler.post(new Runnable(){

			@Override
			public void run() {
				((NoteBookPickerActivity)mActivity).genNotebookthumbFromBook(loader, book);					
			}
			
		});
		
		if(mIsCanceling)
		{
			doDeleteBook(book.getCreatedTime());
			return null;
		}
		else
		{
			mCount++;
			publishProgress();
		}
        return null;
    }
    
	private void genThumb(PageDataLoader loader, NotePage page, boolean isPhoneSize) {
		Bitmap cover = null;
		int color = page.getPageColor();
		int line = page.getPageStyle();
		Resources res = mActivity.getApplicationContext().getResources();
		cover = CoverHelper.getDefaultCoverBitmap(color, line,res);//Allen

		Paint paint = new Paint();
		int targetWidth, targetHeight;

		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setFilterBitmap(true);

		Bitmap result = Bitmap.createBitmap(cover.getWidth(),
				cover.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(cover, 0, 0, paint);
		cover.recycle();
		cover = null;
		targetWidth = (int) (result.getWidth() * 0.9);
		targetHeight = (int) (result.getHeight() * 0.85);
		Bitmap content;
		Canvas contentCanvas;
		content = Bitmap.createBitmap(targetWidth, targetHeight,
				Bitmap.Config.ARGB_8888);
		content.setDensity(Bitmap.DENSITY_NONE);
		contentCanvas = new Canvas(content);
		page.load(loader,false,contentCanvas, true, false, false);//RICHAR FOR NEW LOAD
		//page.load(contentCanvas, true, false);
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
			result.compress(Bitmap.CompressFormat.PNG,
					MetaData.THUMBNAIL_QUALITY, bos);
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
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        if(mIsCanceling)
        {
        	mProgressBar.setProgress(0);
        	mTextView.setText("");
        	return;
        }
        mProgressBar.setProgress(mCount);
        
        int show = (int)((100.0* mCount)/mProgressBar.getMax());
        String realshow = show + "%";
        mTextView.setText(realshow);

    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mIsTaskRunning = false;
        //begin wendy
        //PickerActivity activity = (PickerActivity) mActivity;
        //activity.updateFragment();
        mDialog.dismiss();
        mActivity.removeDialog(NoteBookPickerActivity.HWR_PROGRESS_DIALOG);
        
        Activity activity = mActivity;
        if(activity instanceof NoteBookPickerActivity)
        {
        	((NoteBookPickerActivity)activity).updateFragment();
        }
        //end wendy
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
