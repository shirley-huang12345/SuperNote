package com.asus.supernote.template.widget;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.MetaData.SuperNoteUpdateFrom;
import com.asus.supernote.data.MetaData.ToDoWidgetTable;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteStringItem;
import com.asus.supernote.indexservice.IndexService;
import com.asus.supernote.indexservice.IndexServiceClient;
import com.asus.supernote.inksearch.CFG;
import com.asus.supernote.picker.NoteBookPickerActivity;
import com.asus.supernote.picker.PickerUtility;
import com.asus.supernote.ui.CoverHelper;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class ToDoWidgetService extends Service {
	public final static String ACTION_TODO_WIDGET_INIT = "com.asus.supernote.action.TODO_WIDGET_INIT";
	public final static String ACTION_TODO_WIDGET_EDIT_TODO_ITEM = "com.asus.supernote.action.TODO_WIDGET_EDIT_TODO_ITEM";
	public final static String ACTION_TODO_WIDGET_SHOW_DIALOG = "com.asus.supernote.action.TODO_WIDGET_SHOW_DIALOG";
	public final static String ACTION_TODO_WIDGET_SORT_BY_RESULT = "com.asus.supernote.action.TODO_WIDGET_SORT_BY_RESULT";
	public final static String ACTION_TODO_WIDGET_ADD_TO_RESULT = "com.asus.supernote.action.TODO_WIDGET_ADD_TO_RESULT";
	public final static String ACTION_TODO_WIDGET_ADD_ITEM = "com.asus.supernote.action.TODO_WIDGET_ADD_ITEM";
	public final static String ACTION_TODO_WIDGET_SELECT_BOOK_RESULT = "com.asus.supernote.action.TODO_WIDGET_SELECT_BOOK_RESULT";
	public final static String ACTION_TODO_WIDGET_OPTIONS_CHANGED = "com.asus.supernote.action.TODO_WIDGET_OPTIONS_CHANGED";
	/* adapter type indicate the type of content that ListView will show */
	public static final String EXTRA_ADAPTER_TYPE = "EXTRA_ADAPTER_TYPE";
	
	/* indicate whether need to show the voice input dialog */
	public static final String EXTRA_TODO_WIDGET_VOICE_INPUT = "EXTRA_TODO_WIDGET_START_VOICE_INPUT";
	
	/* indicate add new ToDo item from ToDo widget */
	public static final String EXTRA_TODO_WIDGET_IS_ADD_TODO_ITEM = "EXTRA_TODO_WIDGET_IS_ADD_TODO_ITEM";
	
	public static final String EXTRA_TODO_BOOK_ID = "EXTRA_TODO_BOOK_ID";
	public static final String EXTRA_TODO_PAGE_ID = "EXTRA_TODO_PAGE_ID";
	public static final String EXTRA_TODO_WIDGET_TODO_ITEM_POSITION = "EXTRA_TODO_WIDGET_TODO_ITEM_POSITION";
	public static final String EXTRA_TODO_WIDGET_SORT_BY = "EXTRA_TODO_WIDGET_SORT_BY";
	public static final String EXTRA_TODO_WIDGET_DIALOG_TYPE = "EXTRA_TODO_WIDGET_DIALOG_TYPE";
	public static final String EXTRA_TODO_WIDGET_EDIT_TODO_ITEM_TYPE = "EXTRA_TODO_WIDGET_EDIT_TODO_ITEM_TYPE";
	public static final String EXTRA_TODO_WIDGET_WIDTH = "EXTRA_TODO_WIDGET_WIDTH";
	public static final String EXTRA_TODO_WIDGET_HEIGHT = "EXTRA_TODO_WIDGET_HEIGHT";
	public static final String EXTRA_TODO_WIDGET_SELECT_ITEM = "EXTRA_TODO_WIDGET_SELECT_ITEM"; //smilefish
	
	public static final short EDIT_TODO_ITEM_TYPE_DELETE = 0;
	public static final short EDIT_TODO_ITEM_TYPE_CHECK = 1;
	public static final short EDIT_TODO_ITEM_TYPE_CLICK_ITEM = 2;

	private BookCase mBookcase = null;
    private IndexServiceClient mIndexServiceClient = null;
    private boolean mIsEnableToAddNewItem = true;
	
	@Override
	public void onCreate() {
		if(MetaData.AppContext == null){
			MetaData.AppContext = getApplicationContext();
		}
		if(mBookcase == null){
			mBookcase = BookCase.getInstance(this);
		}		
        //BEGIN: RICHARD
        if(CFG.getCanDoVO() == true)//darwin
        {
	        ComponentName componantName = new ComponentName(this.getPackageName(), IndexService.class.getName());
	        Intent sIntent = new Intent().setComponent(componantName);
	        sIntent.setAction(IndexService.INDEXER_START_INTENT);
	        startService(sIntent);
	        mIndexServiceClient = new IndexServiceClient();
	        mIndexServiceClient.doBindService(this);    
        }
        //END: RICHARD
		super.onCreate();
	}

	private ToDoWidgetItem initToDoWidget(int widgetId, ToDoWidgetItem toDoWidget){
		if(toDoWidget == null){
			toDoWidget = new ToDoWidgetItem(widgetId,ToDoWidgetItem.ADAPTER_TYPE_ALL_TODOS);
			insertToDatabase(toDoWidget);
		}
		return toDoWidget;
	}
	
    //begin smilefish
	private static final int MSG_DELAY_ENABLE_ADD_ACTION = 1;
	private Handler mHandler = new Handler()
    {
		@Override
        public void handleMessage(Message msg) {
        	if(msg.what == MSG_DELAY_ENABLE_ADD_ACTION)
        		mIsEnableToAddNewItem = true;
	    }
    };
    //end smilefish
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent == null){
			return START_NOT_STICKY;
		}
		ToDoWidgetItem toDoWidget = getToDoWidgetItemFromDatabase(intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1));
		String action = intent.getAction();
		if(action.equals(ACTION_TODO_WIDGET_INIT)){
			int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			ToDoWidgetItem widget =  initToDoWidget(widgetId,toDoWidget);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
			appWidgetManager.updateAppWidget(widgetId, getRemoteViews(widget));
		}
		else if(toDoWidget == null){ /* when the user clear data */
			ToDoWidgetItem widget = initToDoWidget(intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1),toDoWidget);
			updateToDoWidget(widget, false);
			
			//begin smilefish
			if(action.equals(ACTION_TODO_WIDGET_ADD_ITEM)){
				boolean voiceInput = intent.getBooleanExtra(EXTRA_TODO_WIDGET_VOICE_INPUT, false);

				Bundle bundle = new Bundle();
				bundle.putBoolean(EXTRA_TODO_WIDGET_VOICE_INPUT,voiceInput);
				bundle.putBoolean(EXTRA_TODO_WIDGET_IS_ADD_TODO_ITEM, true);
				
				List<NoteBook> todoBooks = mBookcase.getToDoTemplateBookList();
				/* no ToDo book */
				if(todoBooks.size() == 0){
					NoteBook book = addNewBook();
				    bundle.putLong(MetaData.TEMPLATE_BOOK_NEW, 1L);//not 0 is OK;
					bundle.putLong(MetaData.BOOK_ID, book.getCreatedTime());
					addNewToDoItem(bundle);
					return START_NOT_STICKY;
				}
			}
			//end smilefish
		}
		else if(action.equals(ACTION_TODO_WIDGET_EDIT_TODO_ITEM)){
			editToDoItem(intent,toDoWidget);
		}
		else if(action.equals(ACTION_TODO_WIDGET_SHOW_DIALOG)){
			int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			short dialogType = intent.getShortExtra(EXTRA_TODO_WIDGET_DIALOG_TYPE, (short) -1);
			Bundle bundle = new Bundle();
			bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			bundle.putShort(EXTRA_TODO_WIDGET_DIALOG_TYPE, dialogType);
			switch(dialogType){
			case ToDoWidgetDialogActivity.DIALOG_TYPE_SELECT_BOOK:	
				bundle.putString(EXTRA_TODO_WIDGET_SELECT_ITEM, getTitle(toDoWidget)); //smilefish
				startActivity(getDialogActivityIntent(bundle,widgetId));
				break;
			case ToDoWidgetDialogActivity.DIALOG_TYPE_SORT_BY:
				bundle.putShort(EXTRA_TODO_WIDGET_SORT_BY, toDoWidget.sortBy);
				startActivity(getDialogActivityIntent(bundle,widgetId));
				break;
			default: break;
			}
		}
		else if(action.equals(ACTION_TODO_WIDGET_SORT_BY_RESULT)){
			short sortBy = intent.getShortExtra(EXTRA_TODO_WIDGET_SORT_BY, ToDoComparator.SORT_BY_TIME);
			if(toDoWidget.sortBy != sortBy){  //just update widget when changed sort type
				toDoWidget.sortBy = sortBy;
				updateToDatabase(toDoWidget);
				updateToDoWidget(toDoWidget,false);
			}
		}
		else if(action.equals(ACTION_TODO_WIDGET_ADD_ITEM)){
			int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			boolean voiceInput = intent.getBooleanExtra(EXTRA_TODO_WIDGET_VOICE_INPUT, false);
			Bundle bundle = new Bundle();
			bundle.putBoolean(EXTRA_TODO_WIDGET_VOICE_INPUT,voiceInput);
			bundle.putBoolean(EXTRA_TODO_WIDGET_IS_ADD_TODO_ITEM, true);
			
			List<NoteBook> todoBooks = mBookcase.getToDoTemplateBookList();
			/* no ToDo book */
			if(todoBooks.size() == 0){
				NoteBook book = addNewBook();
			    bundle.putLong(MetaData.TEMPLATE_BOOK_NEW, 1L);//not 0 is OK;
				bundle.putLong(MetaData.BOOK_ID, book.getCreatedTime());
				addNewToDoItem(bundle);
				return START_NOT_STICKY;
			}
			
			if(toDoWidget.adapterType == ToDoWidgetItem.ADAPTER_TYPE_NOTEBOOK){
				bundle.putLong(MetaData.BOOK_ID, toDoWidget.bookId);
				addNewToDoItem(bundle);
			}
			else if(toDoWidget.adapterType == ToDoWidgetItem.ADAPTER_TYPE_ALL_TODOS){
				/* only one ToDo book */
				if(todoBooks.size() == 1){
					long bookId = todoBooks.get(0).getCreatedTime();
					bundle.putLong(MetaData.BOOK_ID, bookId);
					addNewToDoItem(bundle);
				}
				else{
					bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
					bundle.putShort(EXTRA_TODO_WIDGET_DIALOG_TYPE, ToDoWidgetDialogActivity.DIALOG_TYPE_ADD_TO);
					startActivity(getDialogActivityIntent(bundle,widgetId));
				}
			}
		}
		else if(action.equals(ACTION_TODO_WIDGET_ADD_TO_RESULT)){
			long bookId = intent.getLongExtra(EXTRA_TODO_BOOK_ID, -1);
			
			if(bookId != -1){
				Bundle bundle = new Bundle();
				bundle.putBoolean(EXTRA_TODO_WIDGET_VOICE_INPUT,
						intent.getBooleanExtra(EXTRA_TODO_WIDGET_VOICE_INPUT, false));
				bundle.putBoolean(EXTRA_TODO_WIDGET_IS_ADD_TODO_ITEM, true);
				bundle.putLong(MetaData.BOOK_ID, bookId);
				addNewToDoItem(bundle);
			}
		}
		else if(action.equals(ACTION_TODO_WIDGET_SELECT_BOOK_RESULT)){
			long bookId = intent.getLongExtra(EXTRA_TODO_BOOK_ID, -1);
			if(bookId == -1){ //-1 indicate select all todos mode
				toDoWidget.adapterType = ToDoWidgetItem.ADAPTER_TYPE_ALL_TODOS;
			}
			else{
				toDoWidget.adapterType = ToDoWidgetItem.ADAPTER_TYPE_NOTEBOOK;
				toDoWidget.bookId = bookId;
			}
			updateToDatabase(toDoWidget);
			updateToDoWidget(toDoWidget,false);
		}
		else if(action.equals(MetaData.ACTION_SUPERNOTE_UPDATE)){
			updateWidget(intent);
		}
		else if(action.equals(ACTION_TODO_WIDGET_OPTIONS_CHANGED)){
			toDoWidget.height = intent.getIntExtra(EXTRA_TODO_WIDGET_HEIGHT, 0);
			toDoWidget.width = intent.getIntExtra(EXTRA_TODO_WIDGET_WIDTH, 0);
			updateToDoWidget(toDoWidget,false);
		}
		return START_NOT_STICKY;
	}

	/* edit ToDo item from widget: delete and check item */
	private void editToDoItem(Intent intent,ToDoWidgetItem toDoWidget){
		long bookId = intent.getLongExtra(EXTRA_TODO_BOOK_ID, -1);
		long pageId = intent.getLongExtra(EXTRA_TODO_PAGE_ID, -1);
		int itemPosition = intent.getIntExtra(EXTRA_TODO_WIDGET_TODO_ITEM_POSITION, -1); //index start with 1
		short editType = intent.getShortExtra(EXTRA_TODO_WIDGET_EDIT_TODO_ITEM_TYPE, (short) -1);
		NoteBook book = mBookcase.getNoteBook(bookId);
		if(book != null){
			NotePage page = book.getNotePage(pageId);
			boolean isModified = false;
			if (page != null && itemPosition != -1) {
				/* click item from widget */
				if(editType == EDIT_TODO_ITEM_TYPE_CLICK_ITEM){
					Bundle bundle = new Bundle();
					bundle.putLong(MetaData.BOOK_ID, intent.getLongExtra(EXTRA_TODO_BOOK_ID, -1));
					bundle.putLong(MetaData.PAGE_ID, intent.getLongExtra(EXTRA_TODO_PAGE_ID, -1));
					startActivity(getIntent(bundle));
				}
				
				ArrayList<NoteItemArray> allNoteItems = page.loadNoteItems(this);
				if(allNoteItems == null){
					//smilefish add for runtime permission
			        if(MethodUtils.needShowPermissionPage(getApplicationContext())){
			        	Bundle bundle = new Bundle();
						bundle.putLong(MetaData.BOOK_ID, intent.getLongExtra(EXTRA_TODO_BOOK_ID, -1));
						bundle.putLong(MetaData.PAGE_ID, intent.getLongExtra(EXTRA_TODO_PAGE_ID, -1));
						startActivity(getIntent(bundle));
					}
			        
					return;
				}
				
				/* delete item from widget */
				if (editType == EDIT_TODO_ITEM_TYPE_DELETE) {
					ArrayList<NoteItemArray> removeNoteItems = new ArrayList<NoteItemArray>();
					int index = 0;
					int isPageFullFlagItemIndex = -1;
					for (int i=0; i < allNoteItems.size(); i++) {
						NoteItemArray item = allNoteItems.get(i);
						if (item.getTemplateItemType() == NoteItemArray.TEMPLATE_TODO_PAGE_FULL_FLAG) {
							String isPageFull = item.getNoteItems().get(0).getText();
							if(isPageFull.equals("true")){
								isPageFullFlagItemIndex = i;
							}
						} else if (item.getTemplateItemType() == NoteItemArray.TEMPLATE_SEPERATER_TODO_NEW_ITEM_BEGIN) {
							index++;
						}
						
						if (itemPosition == index) {
							removeNoteItems.add(item);
						} else if (itemPosition < index) {
							break;
						}
					}
					
					if(isPageFullFlagItemIndex != -1){
			        	/* update is page full flag */
			        	ArrayList<NoteItem> noteItemsArrayList = new ArrayList<NoteItem>();
			            noteItemsArrayList.add(new NoteStringItem("false"));
			            NoteItemArray noteItemArray = new NoteItemArray(noteItemsArrayList,NoteItemArray.TEMPLATE_TODO_PAGE_FULL_FLAG);
			            allNoteItems.remove(isPageFullFlagItemIndex);
				        allNoteItems.add(isPageFullFlagItemIndex, noteItemArray);
			        }
					
					if (removeNoteItems.size() > 0) {
						allNoteItems.removeAll(removeNoteItems);
						isModified = true;
					}
					/* check item from widget */
				} else if (editType == EDIT_TODO_ITEM_TYPE_CHECK) {
					int index = 0;
					int checkedItemIndex = -1;
					int lastModifyTimeItemIndex = -1;
					String isChecked = null;
					
					for (int i = 0;i < allNoteItems.size();i++) {
						NoteItemArray item = allNoteItems.get(i);
						if (item.getTemplateItemType() == NoteItemArray.TEMPLATE_SEPERATER_TODO_NEW_ITEM_BEGIN) {
							index++;
						}
						
						if(itemPosition == index){
							if (item.getTemplateItemType() == NoteItemArray.TEMPLATE_CONTENT_TODO_CHECKBOX) {
								NoteItem stringItem = item.getNoteItems().get(0);
								 isChecked = stringItem.getText();
								if(isChecked.equals("true")){
									isChecked = "false";
								}
								else if(isChecked.equals("false")){
									isChecked = "true";
								}
								checkedItemIndex = i;
								isModified = true;
							}
							else if(item.getTemplateItemType() == NoteItemArray.TEMPLATE_CONTENT_TODO_MODIFY_TIME){
								lastModifyTimeItemIndex = i;
							}
						}
						else if (itemPosition < index) {
							break;
						}
					}
					if(isChecked != null){
						if(checkedItemIndex != -1){
							/* update checked info */
					        ArrayList<NoteItem> noteItemsArrayList = new ArrayList<NoteItem>();
					        noteItemsArrayList.add(new NoteStringItem(isChecked));
					        NoteItemArray noteItemArray = new NoteItemArray(noteItemsArrayList,NoteItemArray.TEMPLATE_CONTENT_TODO_CHECKBOX);
					        allNoteItems.remove(checkedItemIndex);
					        allNoteItems.add(checkedItemIndex, noteItemArray);
						}
				        if(lastModifyTimeItemIndex != -1){
				        	/* update last modify time */
				        	ArrayList<NoteItem> noteItemsArrayList = new ArrayList<NoteItem>();
				            noteItemsArrayList.add(new NoteStringItem(System.currentTimeMillis()+""));
				            NoteItemArray noteItemArray = new NoteItemArray(noteItemsArrayList,NoteItemArray.TEMPLATE_CONTENT_TODO_MODIFY_TIME);
				            allNoteItems.remove(lastModifyTimeItemIndex);
					        allNoteItems.add(lastModifyTimeItemIndex, noteItemArray);
				        }
					}
				}

				if (isModified) {
					page.save(allNoteItems, null);
					genThumb(book,page);//darwin
	    			if(mIndexServiceClient != null)
	    			{
	    				mIndexServiceClient.sendPageIDToIndexService(page.getCreatedTime());
	    			}
					updateToDoWidget(null, true);
				}
			}
		}
	}
	
	//darwin
	private void genThumb(NoteBook book,NotePage nPage)
	{
		final PageDataLoader loader = PageDataLoader.getInstance(MetaData.AppContext);
		int numPage = book.getTotalPageNumber();
		for (int i = 0; i < numPage; i++) {
			long pageId = book.getPageOrder(i);
			final NotePage page = book.getNotePage(pageId);

			
			genThumb(
							loader,
							page,
							book.getPageSize() == MetaData.PAGE_SIZE_PHONE);					

		}

		genNotebookthumbFromBook(loader, book);					

			
		
	}
	
	public void genNotebookthumbFromBook(PageDataLoader loader,NoteBook notebook)
	{
		long pageid = notebook.getPageOrder(0);
		NotePage notepage = notebook.getNotePage(pageid);
		if (notepage != null) {
			saveBookCoverThumb(loader, false, notebook,notepage);
		}
	}
	
	public void saveBookCoverThumb(PageDataLoader loader, boolean isLoadAsync, NoteBook book ,NotePage page)
    {
    	try {
            Bitmap bitmap = getThumbnailNoBackground(loader, isLoadAsync, book,page,book.getBookColor(),book.getGridType());
            if (bitmap != null) {
                File file = new File(book.getBookPath(), MetaData.THUMBNAIL_PREFIX);
                if (file.exists() == false) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bitmap.compress(Bitmap.CompressFormat.PNG, MetaData.THUMBNAIL_QUALITY, bos);
                bitmap.recycle();
                bitmap = null;
                bos.close();
                fos.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public Bitmap getThumbnailNoBackground(PageDataLoader loader, boolean isLoadAsync, NoteBook notebook ,NotePage page ,int color, int line) {
        Bitmap cover = null, result = null;
        {
            Resources res = getResources();
            Bitmap content;
            Canvas resultCanvas, contentCanvas;
            Paint paint = new Paint();
            int targetWidth, targetHeight;
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            try {
            	cover = CoverHelper.getDefaultCoverBitmap(color, line, res);//Allen
                result = Bitmap.createBitmap(cover.getWidth(), cover.getHeight(), Bitmap.Config.ARGB_8888);
                resultCanvas = new Canvas(result);
                cover.recycle();
                cover = null;
                targetWidth = (int) (result.getWidth() * 0.9);
                targetHeight = (int) (result.getHeight() * 0.85);
                content = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
                content.setDensity(Bitmap.DENSITY_NONE);
                contentCanvas = new Canvas(content);
                if (!isLoadAsync) {
                	loader.load(page);
                }
                page.load(loader, isLoadAsync, contentCanvas, true, false, false);
                float left = res.getDimension(R.dimen.thumb_padding_left);
                float top = res.getDimension(R.dimen.thumb_padding_top);
                resultCanvas.translate(left, top);
                resultCanvas.drawBitmap(content, 0, 0, paint);
                content.recycle();
                content = null;
            }
            catch (OutOfMemoryError e) {
                //Log.w(TAG, "[OutOfMemoryError] Loading cover failed !!!");
            }
        }
        return result;
    }
		
		private void genThumb(PageDataLoader loader, NotePage page, boolean isPhoneSize) {
			Bitmap cover = null;
			int color = page.getPageColor();
			int line = page.getPageStyle();
			Resources res = getApplicationContext().getResources();
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
	//darwin
	
	private NoteBook addNewBook(){
		NoteBook book = new NoteBook(MetaData.AppContext);
		book.setTitle(PickerUtility.getDefaultBookName(getApplicationContext()));

		int pageSize = MetaData.PAGE_SIZE_PHONE;
		int deviceType =  getResources().getInteger(R.integer.device_type);
		if (deviceType > 100) {
			pageSize = MetaData.PAGE_SIZE_PAD;
		}
		else{
			pageSize = MetaData.PAGE_SIZE_PHONE;
		}
		int indexLanguage = -1;
		if(CFG.getCanDoVO() == false){
	        CFG.setPath(this.getDir("Data", 0).getAbsolutePath());
	        CFG.checkVOResourcesExist();
		}
		if(CFG.getCanDoVO() == true){
			/* get default language */
			indexLanguage = NoteBookPickerActivity.sLanguageHelper.getRecordIndexLaguage();
		}
		book.setTemplate(MetaData.Template_type_todo);
		book.setPageSize(pageSize);
		book.setBookColor(MetaData.BOOK_COLOR_WHITE);
		book.setGridType(MetaData.BOOK_GRID_BLANK);
		book.setIsLocked(false);
		book.setIndexLanguage(indexLanguage);
		
		mBookcase.addNewBook(book);

		if(!MetaData.SuperNoteUpdateInfoSet.containsKey(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_ADD_BOOK)){
			MetaData.SuperNoteUpdateInfoSet.put(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_ADD_BOOK,null);
		}
		return book;
	}
	
	private void addNewToDoItem(Bundle bundle){
		//begin smilefish. avoid repeat to add
		if(!mIsEnableToAddNewItem) return;
		mIsEnableToAddNewItem = false;
		mHandler.sendEmptyMessageDelayed(MSG_DELAY_ENABLE_ADD_ACTION, 500);
		//end smilefish
		
		long bookId = bundle.getLong(MetaData.BOOK_ID, -1);
		if(bookId == -1){
			return;
		}
		NoteBook book = mBookcase.getNoteBook(bookId);
		List<Long> pages =  book.getPageOrderList();
		if(pages.size() == 0){
			NotePage notePage = new NotePage(this, book.getCreatedTime());
	        notePage.setPageColor(book.getBookColor());
	        notePage.setPageSize(book.getPageSize());
	        notePage.setTemplate(book.getTemplate());
	        notePage.setIndexLanguage(book.getIndexLanguage());
	        book.addPage(notePage);
	        
			bundle.putLong(MetaData.PAGE_ID, notePage.getCreatedTime());
			startActivity(getIntent(bundle));
		}
		else{
			bundle.putLong(MetaData.PAGE_ID, pages.get(pages.size()-1));
			startActivity(getIntent(bundle));
		}
	}
	
	@Override
	public void onDestroy() {	
		//BEGIN: RICHARD
        if(mIndexServiceClient != null)//darwin
        {
        	mIndexServiceClient.doUnbindService(this);
        }
        //END: RICHARD
		super.onDestroy();
	}

	/* update widget info to database */
	private void updateToDatabase(ToDoWidgetItem item){
		ContentValues values = new ContentValues();
		values.put(MetaData.ToDoWidgetTable.WIDGET_ID, item.widgetId);
		values.put(MetaData.ToDoWidgetTable.ADAPTER_TYPE, item.adapterType);
		values.put(MetaData.ToDoWidgetTable.BOOK_ID, item.bookId);
		values.put(MetaData.ToDoWidgetTable.SORT_BY, item.sortBy);

		getContentResolver().update(MetaData.ToDoWidgetTable.uri, values,MetaData.ToDoWidgetTable.WIDGET_ID + " = " + item.widgetId,null);
		Log.i("Allen update","Widget:"+ item.widgetId);
	}
	
	/* insert widget info to database */
	private void insertToDatabase(ToDoWidgetItem item){
		ContentValues values = new ContentValues();
		values.put(MetaData.ToDoWidgetTable.WIDGET_ID, item.widgetId);
		values.put(MetaData.ToDoWidgetTable.ADAPTER_TYPE, item.adapterType);
		values.put(MetaData.ToDoWidgetTable.BOOK_ID, item.bookId);
		values.put(MetaData.ToDoWidgetTable.SORT_BY, item.sortBy);
		
		getContentResolver().insert(MetaData.ToDoWidgetTable.uri, values);
		Log.i("Allen insert","WidgetId:"+ item.widgetId);
	}
	
	/* get widget data from database */
	private ToDoWidgetItem getToDoWidgetItemFromDatabase(int widgetId) {
		ToDoWidgetItem item = null;
		Cursor cursor = null;
		try {
			cursor = getContentResolver().query(
					MetaData.ToDoWidgetTable.uri, null,
					MetaData.ToDoWidgetTable.WIDGET_ID + " = " + widgetId,
					null, null);
			if (cursor != null) {
				if (cursor.getCount() > 0 && cursor.moveToFirst()) {
					item = new ToDoWidgetItem();
					item.widgetId = widgetId;
					item.adapterType = cursor
							.getShort(MetaData.ToDoWidgetTable.INDEX_ADAPTER_TYPE);
					item.bookId = cursor
							.getLong(MetaData.ToDoWidgetTable.INDEX_BOOK_ID);
					item.sortBy = cursor
							.getShort(MetaData.ToDoWidgetTable.INDEX_SORT_BY);
				}
				cursor.close();
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		}finally {
		    if (cursor != null) //smilefish fix memory leak
		        cursor.close();
		}
		return item;
	}
	
	/* get all widget items from database */
	private List<ToDoWidgetItem> getAllToDoWidgetItemsFromDatabase() {
		ArrayList<ToDoWidgetItem> items = new ArrayList<ToDoWidgetItem>();
		try {
			Cursor cursor = getContentResolver().query(
					MetaData.ToDoWidgetTable.uri,null,null,null,ToDoWidgetTable.ID + " ASC ");
			if (cursor != null) {
				if (cursor.getCount() > 0 && cursor.moveToFirst()) {
					do{
						ToDoWidgetItem item = new ToDoWidgetItem();
						item.widgetId = cursor.getInt(MetaData.ToDoWidgetTable.INDEX_WIDGET_ID);
						item.adapterType = cursor
								.getShort(MetaData.ToDoWidgetTable.INDEX_ADAPTER_TYPE);
						item.bookId = cursor
								.getLong(MetaData.ToDoWidgetTable.INDEX_BOOK_ID);
						item.sortBy = cursor
								.getShort(MetaData.ToDoWidgetTable.INDEX_SORT_BY);
						items.add(item);
					}while (cursor.moveToNext());
				}
				cursor.close();
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		return items;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/* handle the update from widget*/
	private RemoteViews getRemoteViews(ToDoWidgetItem widget) {
		RemoteViews remoteView = null;
		RemoteViews newRemoteView = null;
		Intent intent = new Intent(this, ToDoWidgetRemoteViewsService.class);
		intent.setAction(widget.widgetId + "");
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.widgetId);
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		remoteView = new RemoteViews(getPackageName(),
				R.layout.template_todo_widget_layout);
				
		remoteView.setRemoteAdapter(R.id.todo_widget_listview, intent);
		Intent intentTemplate = new Intent(this,ToDoWidgetService.class);
		intentTemplate.setAction(ACTION_TODO_WIDGET_EDIT_TODO_ITEM);
		intentTemplate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.widgetId);
		intentTemplate.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));		
		PendingIntent deletePendingIntent = PendingIntent.getService(this, 0, intentTemplate,
                PendingIntent.FLAG_UPDATE_CURRENT);
		remoteView.setPendingIntentTemplate(R.id.todo_widget_listview, deletePendingIntent);

		remoteView.setTextViewText(R.id.todo_widget_switch_notebook_button, getTitle(widget));
		remoteView.setImageViewResource(R.id.todo_widget_sort_button, getSortButtonResourceId(widget.sortBy));

		//Begin: add to support change widget width
		if(widget.width!=0){ //widget.height != 0 && 
			MetaData.TODO_WIDGET_WIDTH_SIZE = 0;
			if(widget.width >= 2 && widget.width <= 8) //Carol
				MetaData.TODO_WIDGET_WIDTH_SIZE = widget.width;
			else
				MetaData.TODO_WIDGET_WIDTH_SIZE = 0;
		}
		//End
		
		Bundle addToBundle = new Bundle();
		addToBundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.widgetId);
		
		remoteView.setOnClickPendingIntent(R.id.todo_widget_add_button,
				getPendingIntent(ACTION_TODO_WIDGET_ADD_ITEM, addToBundle));
		
		addToBundle.putBoolean(EXTRA_TODO_WIDGET_VOICE_INPUT, true);
		remoteView.setOnClickPendingIntent(R.id.todo_widget_voice_input_button,
				getPendingIntent(ACTION_TODO_WIDGET_ADD_ITEM, addToBundle));
	
		Bundle bundle = new Bundle();
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.widgetId);
		bundle.putShort(EXTRA_ADAPTER_TYPE, widget.adapterType);
		
		bundle.putShort(EXTRA_TODO_WIDGET_DIALOG_TYPE, ToDoWidgetDialogActivity.DIALOG_TYPE_SELECT_BOOK);
		remoteView.setOnClickPendingIntent(R.id.todo_widget_switch_notebook_button,
				getPendingIntent(ACTION_TODO_WIDGET_SHOW_DIALOG, bundle));
	
		bundle.putShort(EXTRA_TODO_WIDGET_DIALOG_TYPE, ToDoWidgetDialogActivity.DIALOG_TYPE_SORT_BY);
		remoteView.setOnClickPendingIntent(R.id.todo_widget_sort_button,
				getPendingIntent(ACTION_TODO_WIDGET_SHOW_DIALOG, bundle));

		//begin noah
		if(canSystemRecognizeSpeech()){
			remoteView.setViewVisibility(R.id.todo_widget_voice_input_button, View.VISIBLE);
		}else {
			remoteView.setViewVisibility(R.id.todo_widget_voice_input_button, View.GONE);  //modified by Carol 7.18
		}
		//end noah
		
		//begin emmanual
		if (widget.sortBy == ToDoComparator.SORT_BY_PAGE) {
			remoteView.setInt(R.id.todo_widget_sort_button,
			        "setBackgroundResource",
			        R.drawable.template_todo_widget_sort_page_selector);
		}else{
			remoteView.setInt(R.id.todo_widget_sort_button,
			        "setBackgroundResource",
			        R.drawable.template_todo_widget_sort_time_selector);
		}
		//end emmanual
		return remoteView;
	}
	//begin noah
	private boolean canSystemRecognizeSpeech(){
		PackageManager pm = getPackageManager();
		List<ResolveInfo> list = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if(list.size() > 0){
			return true;
		}
		return false;
	}
	//end noah
	
	/* handle the update from supernote */
	@SuppressWarnings("unchecked")
	private void updateWidget(Intent intent){
		ArrayList<Long> deletedBookIds = null;
		ArrayList<Long> lockedBookIds = null;
		try{
			HashMap<SuperNoteUpdateFrom,Object> superNoteUpdateInfoSet = 
					(HashMap<SuperNoteUpdateFrom,Object>) intent.getSerializableExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM);
			if(superNoteUpdateInfoSet.containsKey(SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_BOOK)){
				deletedBookIds = (ArrayList<Long>) superNoteUpdateInfoSet.get(SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_BOOK);
			}
			else if(superNoteUpdateInfoSet.containsKey(SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_LOCK_BOOK)){
				lockedBookIds = (ArrayList<Long>) superNoteUpdateInfoSet.get(SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_LOCK_BOOK);
			}
		}
		catch(ClassCastException e){
			e.printStackTrace();
			return;
		}
		List<ToDoWidgetItem> items = getAllToDoWidgetItemsFromDatabase();
		for(ToDoWidgetItem item: items){
			long bookId = item.bookId;
			if(getTitle(item)==null || //sync book to be deleted
					deletedBookIds != null && deletedBookIds.contains(bookId) || //the book has been deleted 
					lockedBookIds != null && lockedBookIds.contains(bookId)){  //the book has been locked
				item.adapterType = ToDoWidgetItem.ADAPTER_TYPE_ALL_TODOS;
				item.bookId = -1;
				updateToDatabase(item);
			}
		}
		updateToDoWidget(null,true);
	}
	
	/* update ToDo widget and flush the listview */
	private void updateToDoWidget(ToDoWidgetItem toDoWidget, boolean updateAll){
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		if(updateAll == true){
			List<ToDoWidgetItem> items = getAllToDoWidgetItemsFromDatabase();
			for(ToDoWidgetItem item : items){
				appWidgetManager.updateAppWidget(item.widgetId, getRemoteViews(item));
				appWidgetManager.notifyAppWidgetViewDataChanged(item.widgetId, R.id.todo_widget_listview);
			}
		}else{
			appWidgetManager.updateAppWidget(toDoWidget.widgetId, getRemoteViews(toDoWidget));
			appWidgetManager.notifyAppWidgetViewDataChanged(toDoWidget.widgetId, R.id.todo_widget_listview);
		}
	}
	
	private PendingIntent getPendingIntent(String action, Bundle bundle) {
		// emmanual
		if (MetaData.isSDCardFull()) {
			return null;
		}
		
		PendingIntent pendingIntent = null;
		Intent intent = new Intent(this, ToDoWidgetService.class);
		intent.setAction(action);
		intent.setData(Uri.parse(bundle + ""));
		intent.putExtras(bundle);
		pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}
	
	/* get the intent start SuperNnote EditorActivity */
	private Intent getIntent(Bundle bundle){
		Intent intent = new Intent(this, EditorActivity.class);
		intent.setAction(System.currentTimeMillis() + "");
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtras(bundle);
		return intent;
	}
	/* get the intent start ToDoWidgetDialogActivity */
	private Intent getDialogActivityIntent(Bundle bundle, int widgetId){
		Intent intent = new Intent(this, ToDoWidgetDialogActivity.class);
		intent.setAction(ACTION_TODO_WIDGET_SHOW_DIALOG);
		intent.putExtras(bundle);
		intent.setData(Uri.parse(widgetId + ""));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}
	
	/* get notebook title */
	public String getTitle(ToDoWidgetItem item){
		if(item.adapterType == ToDoWidgetItem.ADAPTER_TYPE_NOTEBOOK){
			NoteBook notebook = mBookcase.getNoteBook(item.bookId);
			if(notebook != null){
				return notebook.getTitle();
			}
			else{
				return null;
			}
		}
		else if(item.adapterType == ToDoWidgetItem.ADAPTER_TYPE_ALL_TODOS){
			return getResources().getString(R.string.todo_widget_all_todos);
		}
		else{
			return null;
		}
	}
	
	private int getSortButtonResourceId(short sortBy){
		switch(sortBy){
		case ToDoComparator.SORT_BY_PAGE:
			return R.drawable.asus_widget_supernote_icon_sortpage_n;
		case ToDoComparator.SORT_BY_TIME:
			return R.drawable.asus_widget_supernote_icon_sortdate_n;
		default:return R.drawable.asus_widget_supernote_icon_g;
		}
		
	}
}
