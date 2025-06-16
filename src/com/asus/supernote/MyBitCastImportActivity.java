package com.asus.supernote;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;

import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.NotePageValue;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.doodle.DoodleItem;
import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.GraphicDrawInfo;
import com.asus.supernote.doodle.drawtool.GraphicDrawTool;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteSendIntentItem;
import com.asus.supernote.editable.noteitem.NoteStringItem;
import com.asus.supernote.picker.PickerUtility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.widget.EditText;

public class MyBitCastImportActivity extends Activity{
	Context mContext = null;
	BookCase mBookCase = null;
	NoteBook mBook = null;
	NotePage mPage = null;
	int mDoodleViewWidth = 0;
	int mDoodleViewHeight = 0;
	ProgressDialog mProgressDialog = null;
	String mProgressDialogMessage = null;
	//ProgressBar mProgressBar = null;
	Intent mIntent = null;
	long mBookId = 0;
	ImportTask mImportTask;
	private Handler mHandler = new Handler();//noah
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		PickerUtility.lockRotation(this);
		mProgressDialogMessage = getResources().getString(R.string.my_bit_cast_wait_text);
		mContext = this;
        // BEGIN: Better
        if (MetaData.AppContext == null) {
    		MetaData.AppContext = getApplicationContext();
		}
        // END: Better
		mBookCase = BookCase.getInstance(mContext);
		mIntent = getIntent();
        if (MetaData.IMPORT_NOTE_ACTION.equals(mIntent.getAction())) {
        	generateBookAndPage(getResources().getString(MetaData.MY_BIT_CAST_NAME));
        }
	}
	
	
	private void computeDoodleViewWidthAndHeight(NoteBook noteBook) {
    	boolean isSmallScreen = true;
        
        EditText editText = new EditText(mContext);
        editText.setText("ABCDEFGHIJKLMNOPQRSTabcdefghijklmnopqrst");        
        
        int firstLineHeight = getFirstLineHeight(isSmallScreen);
        int lineCountLimited = NotePageValue.getLineCountLimited(mContext,noteBook.getFontSize(), isSmallScreen);//RICHARD
        
        editText.setGravity(Gravity.START | Gravity.TOP | Gravity.CENTER_VERTICAL | Gravity.CENTER_VERTICAL);
        editText.setTextColor(Color.BLACK);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSize(noteBook));
        editText.setSingleLine(false);
        editText.setPadding(0, firstLineHeight, 0, 0);
        editText.setLineSpacing(0, PickerUtility.getLineSpace(mContext));

        int width = (int)NotePageValue.getBookWidth(mContext,isSmallScreen);//RICHARD
        mDoodleViewWidth = width;
        
        MetaData.Template_EditText_width = width;//wendy allen++ for template 0706
        MetaData.Template_EditText_line_height = editText.getLineHeight();//wendy allen++ for template 0706
    	
        editText.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), 
        					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int height = editText.getLineHeight() * lineCountLimited + firstLineHeight;
        mDoodleViewHeight = height;
	}



	// BEGIN： Shane_Wang@asus.com 2012-11-23
    void generateBookAndPage(String bookName) {
        Cursor cursor = getContentResolver().query(MetaData.BookTable.uri, null, "title = ?", new String[] {bookName}, null);
        NoteBook book = null;
        long bookId = 0;
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            bookId = cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
            book = mBookCase.getNoteBook(bookId);
            mBook = book;
            mBookId = bookId;
            
            if(book != null) {
            	computeDoodleViewWidthAndHeight(mBook);
            	mImportTask = new ImportTask();
            	mImportTask.execute(0);
            }
        }
        else{
        	int pageSize = MetaData.PAGE_SIZE_PHONE;
    		int deviceType =  getResources().getInteger(R.integer.device_type);
    		if (deviceType > 100) {
    			pageSize = MetaData.PAGE_SIZE_PAD;
    		}
    		else{
    			pageSize = MetaData.PAGE_SIZE_PHONE;
    		}
	        book = new NoteBook(this);
	        book.setTitle(bookName);
	        book.setBookColor(MetaData.BOOK_COLOR_WHITE);
	        book.setPageSize(pageSize);
	        book.setGridType(MetaData.BOOK_GRID_LINE);
	        book.setIsLocked(false);
	        mBookCase.addNewBook(book);
	        //saveAllImportPages(book, book.getCreatedTime(), intent);
	        bookId = book.getCreatedTime();
	        mBook = book;
	        mBookId = bookId;
            computeDoodleViewWidthAndHeight(mBook);
            ImportTask importTask = new ImportTask();
        	importTask.execute(0);
        }
        
        //BEGIN: RICHARD
        if(cursor != null)
        {
        	cursor.close();
        }
        //END: RICHARD
    }
    
    void saveAllImportPages(NoteBook book, long bookId, Intent intent) {
    	ArrayList<String> files = intent.getStringArrayListExtra(MetaData.IMPORT_FILE_PATH);
    	NotePage page = null;
    	PageDataLoader loader = new PageDataLoader(mContext);
    	for(String fileName : files) {
    		Log.d("mybitcast", "handle file: " + fileName);
    	    page = new NotePage(mContext, bookId);
    	    book.addPage(page);
    	    page = book.getNotePage(page.getCreatedTime());
    		String type = fileName.substring(fileName.lastIndexOf("."));
    		// BEGIN: Shane_Wang@asus.com 2013-2-17
    		Drawable tmpDrawable = Drawable.createFromPath(fileName);
    		// END: Shane_Wang@asus.com 2013-2-17
    		//if is txt file:
    		if(type.equals(".txt")) {
    			insertTextToPage(fileName, page);
    		}
    		//if is image file:
    		else if(tmpDrawable != null) {
    			tmpDrawable.setCallback(null);
    			insertBitmapToPage(fileName, page);
    		}
    		//if is acc file:
    		else if(type.equals(".aac")) {
    			insertRecordToPage(fileName, page);
    		}
    		else {
    			Log.d("mybitcast", "Unsupported file type: " + type);
    			Log.d("mybitcast", fileName + "can not be imported");
    		}
    		//update page thumbnail
    		
    		
    		if(loader.load(page)) {
    			page.updateSyncFilesInfo(loader, true);
				page.updateTimestampInfo(loader, true);
				final NotePage page2 = page;
				final PageDataLoader loader2 = loader;
				final NoteBook book2 = book;
				/**
				 * noah_zhang
				 * page.genThumb最终会调用NotePage.drawNoteItem,drawNoteItem代码中，
				 * EditText editText = new EditText(mContext);
				 * 如果放在其他线程中去做，会出异常，导致不能正常生成缩略图
				 * 这里强制放在主线程执行
				 */
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						page2.genThumb(loader2, false, book2.getPageSize() == MetaData.PAGE_SIZE_PHONE);
					}
				});
    		}
    	}
    	
    	//last page:
    	mPage = page;
    	//update book thumbnail
    	book.saveBookCoverThumb(loader, false, book.getNotePage(book.getPageOrder(0)));
    }
    
    private void insertTextToPage(String currentFileName, NotePage page) {
    	SpannableStringBuilder builder = new SpannableStringBuilder();
		File file = new File(currentFileName);
		try{
	        FileInputStream fis = new FileInputStream(file);
	        String encoding = PickerUtility.getTextFileCharset(file);
	        InputStreamReader isr = new InputStreamReader(fis, (encoding != null) ? encoding : 
	        	((MetaData.INDEX_CURRENT_LANGUAGE == MetaData.INDEX_LANGUAGE_ZH_TW )? "Big5" : Charset.defaultCharset().displayName()));//darwin modify
	        BufferedReader buf = new BufferedReader(isr);
	        String str = null;
	
	        while ((str = buf.readLine()) != null) {
	            builder.append(str + "\n");
	        }
	        buf.close();
	        if (builder.length() < 1) {
	            return;
	        }
	        String fileText = builder.subSequence(0, builder.length() - 1).toString();
	
	        //add to page:
	        NoteStringItem nsi = new NoteStringItem(fileText);
	        ArrayList<NoteItem>items = new ArrayList<NoteItem>();
	        NoteItemArray itemArray = new NoteItemArray(items, NoteItemArray.TEMPLATE_CONTENT_DEFAULT_NOTE_EDITTEXT);
	        
	        ArrayList<NoteItemArray> newItem = new ArrayList<NoteItemArray>();
	        items.add(nsi);
	        newItem.add(itemArray);
	        page.save(newItem, null);
	        Log.d("mybitcast", "fileName:" + currentFileName + " succeed");
		}catch(Exception e) {
		}
    }
    
    private void insertBitmapToPage(String currentFileName, NotePage page) {
    	String targetFileName = page.getFilePath() + currentFileName.substring(currentFileName.lastIndexOf("/"));
    	try{
    		copyFile(targetFileName, currentFileName);
    	}catch(Exception e) {
    		
    	}
    	File srcFile = new File(targetFileName);
        File destFile = new File(targetFileName);
        try {
            destFile.createNewFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = null;
        try{
        	bitmap = saveBitmap(srcFile, destFile, Bitmap.CompressFormat.JPEG);
        }catch(Exception e) {
        	bitmap = BitmapFactory.decodeFile(srcFile.getAbsolutePath());
        }
        
        Paint mBitmapPaint = new Paint();
        mBitmapPaint = new Paint();
        mBitmapPaint.setDither(true);
        mBitmapPaint.setAntiAlias(true);
        
        GraphicDrawInfo bitmapDrawInfo = (GraphicDrawInfo) (new GraphicDrawTool(bitmap)).getDrawInfo(mBitmapPaint);
        Matrix matrix = new Matrix();
        matrix.setTranslate((mDoodleViewWidth - bitmap.getWidth())/2, (mDoodleViewHeight - bitmap.getHeight())/2);
        bitmapDrawInfo.setTransform(matrix);
        LinkedList<DrawInfo> objects = new LinkedList<DrawInfo>();
        objects.add(bitmapDrawInfo);
        DoodleItem item = new DoodleItem(mDoodleViewWidth, mDoodleViewHeight);
        item.save(objects, page);
        page.save(null, item);
        Log.d("mybitcast", "fileName:" + currentFileName + " succeed");
    }
    
    private static final String OBJ = String.valueOf((char) 65532);
    private void insertRecordToPage(String currentFileName, NotePage page) {
        String targetFileName = page.getFilePath() + currentFileName.substring(currentFileName.lastIndexOf("/"));
    	
    	try{
    		copyFile(targetFileName, currentFileName);
    	}catch(Exception e) {
    		
    	}
    	Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        String fullPath = "file:///" + page.getFilePath() + "//" + targetFileName.substring(targetFileName.lastIndexOf("/") + 1);
        intent.setDataAndType(Uri.parse(fullPath), "video/3gpp/aac");
        NoteSendIntentItem sendIntentItem = new NoteSendIntentItem(intent);
        sendIntentItem.setStart(0);
        sendIntentItem.setEnd(1);
        
        NoteStringItem nsi = new NoteStringItem(OBJ);
        ArrayList<NoteItem>items = new ArrayList<NoteItem>();
        NoteItemArray itemArray = new NoteItemArray(items, NoteItemArray.TEMPLATE_CONTENT_DEFAULT_NOTE_EDITTEXT);
        
        ArrayList<NoteItemArray> newItem = new ArrayList<NoteItemArray>();
        items.add(nsi);
        items.add(sendIntentItem);
        newItem.add(itemArray);
        page.save(newItem, null);
        Log.d("mybitcast", "fileName:" + currentFileName + " succeed");
    }
    
	private void copyFile(String targetFileName, String currentFileName) throws IOException {
	  FileInputStream input = new FileInputStream(currentFileName);   
	  BufferedInputStream inBuff=new BufferedInputStream(input);   
	
	  FileOutputStream output = new FileOutputStream(targetFileName);   
	  BufferedOutputStream outBuff=new BufferedOutputStream(output);
	  byte[] b = new byte[1024 * 5];   
	  int len;   
	  while ((len =inBuff.read(b)) != -1) {
	      outBuff.write(b, 0, len);
	  }
	  outBuff.flush();
	
	  inBuff.close();   
	  outBuff.close();   
	  output.close();   
	  input.close(); 
	}

	private static final int MAX_BITMAP_SIZE = 1920 * 1200;
	protected Bitmap saveBitmap(File srcFile, File destFile, Bitmap.CompressFormat format) {
	  if (srcFile == null || destFile == null) {
	      return null;
	  }
	
	  BitmapFactory.Options option = new BitmapFactory.Options();
	  option.inJustDecodeBounds = true;
	  BitmapFactory.decodeFile(srcFile.getPath(), option);
	  Bitmap b = null;
	  double w = option.outWidth;
	  double h = option.outHeight;
	
	  double ratio = ((w * h) / MAX_BITMAP_SIZE);
	  try {
	      if (ratio > 1.0f) {
	          ratio = Math.sqrt(ratio);
	          w = w / ratio;
	          h = h / ratio;
	          int scale = 1;
	          while ((option.outWidth * option.outHeight) * (1 / Math.pow(scale, 2)) > MAX_BITMAP_SIZE) {
	              scale++;
	          }
	          scale--;
	          option.inSampleSize = scale;
	          option.inJustDecodeBounds = false;
	          Bitmap t = BitmapFactory.decodeFile(srcFile.getPath(), option);
	
	          w = option.outWidth;
	          h = option.outHeight;
	          ratio = ((w * h) / MAX_BITMAP_SIZE);
	          ratio = Math.sqrt(ratio);
	          w = w / ratio;
	          h = h / ratio;
	          b = Bitmap.createScaledBitmap(t, (int) w, (int) h, true);
	          Log.d("[91]", "bw = " + b.getWidth() + ", bh = " + b.getHeight());
	          t.recycle();
	          t = null;
	
	      }
	      else {
	          b = BitmapFactory.decodeFile(srcFile.getPath());
	      }
	  }
	  catch (OutOfMemoryError e) {
	  }
	
	  if (b != null) {
	      try {
	          BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));
	          b.compress(format, 100, bos);
	          bos.close();
	      }
	      catch (FileNotFoundException e) {
	          e.printStackTrace();
	      }
	      catch (IOException e) {
	          e.printStackTrace();
	      }
	  }
	  return b;
	}
    // END： Shane_Wang@asus.com 2012-11-19
	
	private int getFirstLineHeight(boolean isSmallScreen) {
    	if (isSmallScreen) {
            return mContext.getResources().getInteger(R.integer.first_line_height_small_screen);
    	} else {
    		return mContext.getResources().getInteger(R.integer.first_line_height);
    	}
    }
	
	private float getFontSize(NoteBook noteBook) {//change int to float
   	 boolean isSmallScreen = false;
        int textSize = MetaData.BOOK_FONT_NORMAL;
        float fontSize = 0;
        if (noteBook != null) {
            if (noteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE) {
                isSmallScreen = true;
            }
            textSize = noteBook.getFontSize();
        }
        
        //BEGIN: RICHARD
        fontSize = NotePageValue.getFontSize(mContext,textSize,isSmallScreen);
	     //END: RICHARD
        return fontSize;
   }
	
	class ImportTask extends AsyncTask<Integer, Integer, String> {

		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			mProgressDialog = ProgressDialog.show(mContext, "", mProgressDialogMessage, true);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... arg0) {
			// TODO Auto-generated method stub
			try{
			    saveAllImportPages(mBook, mBookId, mIntent);
			    Log.d("mybitcast", "Import task succeed");
			}catch(Exception e) {
				Log.d("mybitcast", "Exception in Import task");
				((Activity)mContext).finish();
				System.exit(-1);
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			if(mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();	
			}
			
			if(!isPause) {
				launchSuperNote();
			}
			isTaskOn = false;
			((Activity)mContext).finish();
		}
		
	}
	
	private void launchSuperNote() {
		Intent intent = new Intent(mContext, EditorActivity.class);
		intent.putExtra(MetaData.BOOK_ID, mBook.getCreatedTime());
        intent.putExtra(MetaData.PAGE_ID, mPage.getCreatedTime());
        startActivity(intent);
	}
	
	
	boolean isPause = false;
	boolean isTaskOn = true;
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isPause = true;
		if(mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();	
		}
		
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isPause = false;
		if(isTaskOn && !mProgressDialog.isShowing()) {
			mProgressDialog = ProgressDialog.show(mContext, "", mProgressDialogMessage, true);
		}
		
	}
	
	

	
	
}
