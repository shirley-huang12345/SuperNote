package com.asus.supernote.widget;

import java.util.ArrayList;
import java.util.HashMap;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.data.MetaData.WidgetItemTable;
import com.asus.supernote.picker.NoteBookPickerActivity;
import com.asus.supernote.picker.PickerActivity;
import com.asus.supernote.picker.PickerUtility;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.database.Cursor;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WidgetService extends Service {
	public Intent mIntent = null;
	public Context mContext;
	public final static Boolean NOT_SHOW_LOCK_BOOK = true; //show unlocked books only

	//Begin Allen	
	/* indicate the value of page list scroll to in pad portrait mode */
	public final static String ACTION_LISTVIEW_SCROLL = "com.asus.supernote.action.ACTION_LISTVIEW_SCROLL";
	public final static String EXTRA_LISTVIEW_SCROLL_TO = "EXTRA_LISTVIEW_SCROLL_TO";	
	public final static short LISTVIEW_SCROLL_TO_FIRST = 0;
	public final static short LISTVIEW_SCROLL_TO_LAST = 1;
	
	//End Allen

	public final static int SHOWPAGE = 2;
	public final static int SHOWBOOK = 1;
	public final static int ADDTEMPLATE = 0;
	public final static int SHOWLOCKEDPAGE = 3;//Siupo
	//Begin Siupo
	public final static int ITEMCLICK_OPENBOOK = 0;
	public final static int ITEMCLICK_NEWBOOK = 1;
	public final static int ITEMCLICK_OPENPAGE = 2;
	public final static int ITEMCLICK_OPENLOCKEDPAGE = 3;
	//End Siupo
	public final static String UPDATE_ACTION_WIDGETTYPE = "com.asus.supernote.update_provider_widgetType";
	public final static String ADDBOOK_ACTION_ONEWIDGET = "com.asus.supernote.addbook_onewidget";
	public final static int ADDBOOK_SUBACTION_ONEWIDGET = 999;//used to add book in phone device;
	
	public final static String WIDGET_ITEM_ID_INTENT_KEY = "widget_item_id_intent_key";
	public final static String WIDGET_BOOK_ID_INTENT_KEY = "widget_book_id_intent_key";
	public static final String WIDGET_BOOK_LOCK_STATUS = "widget_book_lock_status";

	public final static String WIDGET_UPDATE_FROM = "widget_update_from";


	// five main action
	public final static String UPDATE_ACTION = "com.asus.supernote.update_from_provider";
	public final static String ADDBOOK_ACTION = "com.asus.supernote.addbook";
	public final static String EDITBOOK_ACTION = "com.asus.supernote.editbook";
	public final static String ALLNOTEBOOK_TEMPLATE_ACTION = "com.asus.supernote.allNotebookTemplates";
	public final static String MODE_CHANGE_ACTION = "com.asus.supernote.modeChange";
	public final static String PREV_NEXT_BOOK_ACTION = "com.asus.supernote.prev_next_book";//add by richard
	// subaction
	public final static String SUBACTION = "subaction";

	// UPDATE_ACTION
	public final static int UPDATE_ACTION_ADD_WIDGET = 0;
	public final static int UPDATE_ACTION_DELETE_WIDGET = 1;
	// EDIT_NOTEBOOK_SUBACTION
	public final static int EDIT_NOTEBOOK_ACTION_NONE = -1;
	public final static int EDIT_NOTEBOOK_ACTION_FIRST_PAGE = 0;
	public final static int EDIT_NOTEBOOK_ACTION_FRONT_PAGE = 1;
	public final static int EDIT_NOTEBOOK_ACTION_NEXT_PAGE = 2;
	public final static int EDIT_NOTEBOOK_ACTION_LAST_PAGE = 3;
	public final static int EDIT_NOTEBOOK_ACTION_ENTERPAEG = 4;
	public final static int EDIT_NOTEBOOK_ACTION_ALLPAGE = 5;
	public final static int EDIT_NOTEBOOK_ACTION_REPLACEBOOK = 6;
	public static final int EDIT_NOTEBOOK_ACTION_LOCKED_PASSWORD = 7;
	public static final int EDIT_NOTEBOOK_ACTION_LOCKED_REPLACE = 8;
	public static final int EDIT_NOTEBOOK_ACTION_LOCK_BOOK=9;
	public final static int EDIT_NOTEBOOK_ACTION_FIRSTBUTTON = 10; //Carol
	public final static int EDIT_NOTEBOOK_ACTION_SECONDBUTTON = 11;
	public final static int EDIT_NOTEBOOK_ACTION_THIRDBUTTON = 12;
	public final static int EDIT_NOTEBOOK_ACTION_FOURTHBUTTON = 13;
	public final static int EDIT_NOTEBOOK_ACTION_LASTBUTTON = 14;
	
	public final static String WIDGET_BOOK_ID = "widget_book_id";
	public final static String WIDGET_PAGE_ID = "widget_page_id";

	public final static String WIDGET_ADDBOOK_ACTIVITY_FLAG="addbook_flag";
	public final static int add_from_blank_item = 9;
	public final static int replace_from_book_item = 10;
	public final static int replace_from_book_template_item = 11;
	
	// ALLNOTEBOOK_TEMPLATE_ACTION
	public final static int ALLNOTEBOOK_TEMPLATE_ACTION_SHOW_NOTEBOOKS = 1;
	public final static int ALLNOTEBOOK_TEMPLATE_ACTION_ADD_NOTEBOOK = 2;
	public final static int ALLNOTEBOOK_TEMPLATE_ACTION_ITEM_CLICK = 0;
	//AllNoteBookTemplate_subAction Siupo
	public final static int ALLNOTEBOOK_TEMPLATE_SUBACTION_SHOWMYBOOK = 1;
	public final static int ALLNOTEBOOK_TEMPLATE_SUBACTION_ADDNEWBOOK = 2;
	public final static int ALLNOTEBOOK_TEMPLATE_SUBACTION_CLICKITEM = 0;
	public final static int ALLNOTEBOOK_TEMPLATE_SUBACTION_REPLACEWDBOOK = 5;
	
	//PREV_NEXT_BOOK_ACTION
	public final static int PREV_NEXT_BOOK_ACTION_PREV = 0;
	public final static int PREV_NEXT_BOOK_ACTION_NEXT = 1;
	
	
	//End Siupo
	// MODE_CHANGE_ACTION
	public final static int MODE_CHANGE_BOOK_TO_BLANK = 1;

	private Dialog mAddBookDialog;
	
	public void initWidgetRemoteView(int appWidgetId,int subaction)
	{		
		switch(subaction)
		{
		case UPDATE_ACTION_ADD_WIDGET:
			if(!isWidgetIDExist(appWidgetId))
			{
				ArrayList<SingleWidgetItem> list = new ArrayList<SingleWidgetItem>();
				SingleWidgetItem mSingleWidgetItem = null;
				MetaData.WIDGET_ONE_COUNT = 1;
				addNewWidget2WidgetTable(appWidgetId,1);
				mSingleWidgetItem = new SingleWidgetItem(-1,this,appWidgetId,    
						1,  SingleWidgetItem.BLANK_MODE,
		                0L,  0L, 
		                getResources().getInteger(R.integer.device_type)>100||MetaData.IS_LOAD ? 1: 0,
		                1, R.id.FrameLayout_middle);
				mSingleWidgetItem.setIsChagned(true);
				list.add(mSingleWidgetItem);
				
				doUpdateWidget(list,appWidgetId, false);
			}
			else
			{
				doUpdateWidget(getOldItemList(appWidgetId),appWidgetId, false);
				//just refresh the data
			}
			
			Log.i("WidgetService", "UPDATE_ACTION_ADD_WIDGET,Widget Exist");
			break;
		case UPDATE_ACTION_DELETE_WIDGET:
			if(isWidgetIDExist(appWidgetId))
			{
				deleteNewWidgetFromWidgetTable(appWidgetId);
			}
			break;
		default:
			break;
		}
		Log.i("WidgetService", "initWidgetRemoteView()");//Siupo
	}
	
	
	private PendingIntent getPrevNextBookActionPendingIntent(int action,int appwidgetID)
	{
		Intent intent = new Intent();
		intent.setClass(mContext, WidgetService.class);
		intent.setAction(WidgetService.PREV_NEXT_BOOK_ACTION);
		intent.setData(Uri.parse(action+"custom"+System.currentTimeMillis()));
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(WidgetService.SUBACTION, action);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appwidgetID);
		PendingIntent pendingIntent = null;
		pendingIntent = PendingIntent.getService(mContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}
	
	private void setLeftRightButtons(RemoteViews remoteViews,int appwidgetID)
	{
		//false:prev;true next
		ArrayList<SingleWidgetItem> list = getOldItemList(appwidgetID);
		if (list.size() > 0)
		{
			long bookid = list.get(0).getBookId();
			BookCase mBookCase = BookCase.getInstance(mContext);
			NoteBook mNotebook = mBookCase.getNoteBook(bookid);
			if (mNotebook == null) 
			{
				Log.i("setLeftRightButtons()", "No Books");
			} 
			else 
			{
				long tempBookIdPrev = mBookCase.getNextOrPrevBookID(bookid,
						false);
				long tempBookIdNext = mBookCase.getNextOrPrevBookID(bookid,
						true);
				if (bookid == tempBookIdPrev||tempBookIdPrev == -1L){
					remoteViews.setInt(R.id.btLeft, "setVisibility",View.INVISIBLE);
				}else{
					remoteViews.setInt(R.id.btLeft, "setVisibility",View.VISIBLE);
				}
				if (bookid == tempBookIdNext) {
					remoteViews.setInt(R.id.btRight, "setVisibility",View.INVISIBLE);
				}else{
					remoteViews.setInt(R.id.btRight, "setVisibility",View.VISIBLE);
				}
			}
		}
	}
	
	private void setLeftRightButtonInOneLayout(RemoteViews remoteViews, int appwidgetID)
	{
		remoteViews.setOnClickPendingIntent(R.id.btLeft, 
				getPrevNextBookActionPendingIntent(WidgetService.PREV_NEXT_BOOK_ACTION_PREV,appwidgetID));
		remoteViews.setOnClickPendingIntent(R.id.btRight, 
				getPrevNextBookActionPendingIntent(WidgetService.PREV_NEXT_BOOK_ACTION_NEXT,appwidgetID));	
	}
	
	private void addNotebook(Intent _intent,int _appWidgetId,int _clickID,Long _bookID)
	{	
		ArrayList<SingleWidgetItem> list = getOldItemList(_appWidgetId);
		int tempIndex = getIndexInList(list,_clickID);
		if(tempIndex != -1)
		{
			SingleWidgetItem item = list.get(tempIndex);
			
			item.setMode(SingleWidgetItem.BOOK_MODE);
			item.setBookId(_bookID);
			item.setPageId(0L);
			item.setIsChagned(true);
			
			doUpdateWidget(list,_appWidgetId, true);
			MetaData.Changed_Book_List.clear();
			MetaData.Changed_Page_List.clear();
		}

	}

	//Begin Allen ++ to handle update from supernote
	@SuppressWarnings("unchecked")
	private void updateWidget(Intent intent){
		int _appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		ArrayList<SingleWidgetItem> list = getOldItemList(_appWidgetId);
		
		for(SingleWidgetItem item : list){
			if(item.getMode() == SingleWidgetItem.BOOK_MODE){
				ArrayList<Long> deletedBookIds = null;
				ArrayList<Long> deletedPageIds = null;
				try{
					HashMap<MetaData.SuperNoteUpdateFrom,Object> superNoteUpdateInfoSet = (HashMap<MetaData.SuperNoteUpdateFrom,Object>) intent.getSerializableExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM);
					deletedBookIds = (ArrayList<Long>) superNoteUpdateInfoSet.get(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_BOOK);
					deletedPageIds = (ArrayList<Long>) superNoteUpdateInfoSet.get(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_PAGE);
				}
				catch(ClassCastException e){
					e.printStackTrace();
					return;
				}
				/* the book has been deleted */
				if(deletedBookIds!=null && deletedBookIds.contains(item .getBookId())){
					MetaData.WIDGET_ONE_COUNT = 1;
					item.setMode(SingleWidgetItem.BLANK_MODE);
					item.setBookId(0);
					item.setPageId(0);
					item.setAllBookTemplateStatus( //Carol
							mContext.getResources().getInteger(R.integer.device_type)>100||MetaData.IS_LOAD?1:0);
					item.setLockStatus(1);
					item.setIsChagned(true);																											
				}
				/* page has been deleted */
				else if(deletedPageIds != null && deletedPageIds.contains(item.getPageId())){
					NoteBook noteBook = BookCase.getInstance(mContext).getNoteBook(item.getBookId());
					if(noteBook != null){ //488555[Carol]
						if(noteBook.getPageOrderList().size()>0){
							item.setPageId(-1);
							item.setIsChagned(true);
						}
						else{
							MetaData.WIDGET_ONE_COUNT = 1;
							item.setMode(SingleWidgetItem.BLANK_MODE);
							item.setBookId(0);
							item.setPageId(0);
							item.setAllBookTemplateStatus( //Carol
									mContext.getResources().getInteger(R.integer.device_type)>100||MetaData.IS_LOAD?1:0);
							item.setLockStatus(1);
							item.setIsChagned(true);
						}
					}
				}
			}
		}	
		doUpdateWidget(list,_appWidgetId, false);
	}
	//End Allen
	
	public void getAdapterPageData(int appWidgetId,int itemID,long bookID,RemoteViews remoteViews)
	{
		int gridID = -1;
		int emptyID = -1;
		Intent remoteIntent = new Intent();
		remoteIntent.setClass(this, WidgetGridViewService.class);//start the service；
		gridID = R.id.GV1;
		emptyID = R.id.empty_gv1;
		String strData = "THE 1th ITEM SHOW THE PAGES" + String.valueOf(itemID)+" "+
				String.valueOf(bookID);
		remoteIntent.setAction(strData);
		
		//in order to distinguish the intent;
		remoteIntent.setData(Uri.parse(remoteIntent.toUri(Intent.URI_INTENT_SCHEME)));
		remoteIntent.putExtra("mShowBook", WidgetService.SHOWPAGE);//add tag
        remoteIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        remoteIntent.putExtra("BookID", bookID);
        //In this place, can update the subRemoteViews;
        if(getResources().getInteger(R.integer.device_type)>100&&
        		getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT){
        	remoteViews.setRemoteAdapter(gridID, remoteIntent); //portrait mode, display all pages [Carol]
        }
        remoteViews.setEmptyView(gridID, emptyID);
        Log.v("Item ID", String.valueOf(itemID));
        Log.v("Book ID", String.valueOf(bookID));//Log information
        
        
        Intent onClickPageIntent = new Intent();
        onClickPageIntent.setClass(this, WidgetService.class);
        onClickPageIntent.setAction(ALLNOTEBOOK_TEMPLATE_ACTION);
        onClickPageIntent.putExtra(SUBACTION, 0);
        onClickPageIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        onClickPageIntent.setData(Uri.parse(onClickPageIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent onClickPagePendingIntent = PendingIntent.getService(this, 0,
                onClickPageIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(gridID, onClickPagePendingIntent);
	}

	@Override
	public void onStart(Intent intent , int startId)
	{
		mContext=this;
		//BEGIN: RICHARD
		if(intent == null)
		{
			return;
		}
		//END: RICHARD fix start crash.
		
		String mainAction = intent.getAction();
		
		if (mainAction == null)
			return;		
		
		int appWidgetId = 0;
		int subaction = -1;
		int clickID = -1;
		
		if (mainAction.equals(UPDATE_ACTION)) {			
			appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			subaction = intent.getIntExtra(WidgetService.SUBACTION, -1);
			if(appWidgetId==-1 && subaction==-1 ) 
				 return;
			initWidgetRemoteView(appWidgetId, subaction);
		} 
		else if (mainAction.equals(ADDBOOK_ACTION)) {
			appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);	
			if (appWidgetId == -1)
				return;
			
			int goStore=intent.getIntExtra(WidgetService.WIDGET_ADDBOOK_ACTIVITY_FLAG, -1);
			if(goStore==-1)
			{
				int clickItemId = intent.getIntExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, -1);
				long bookId = intent.getLongExtra(WidgetService.WIDGET_BOOK_ID_INTENT_KEY, -1);	
				addNotebook(intent, appWidgetId, clickItemId, bookId);
			}
			else
			{
				int clickItemId = intent.getIntExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, -1);
				long bookId = intent.getLongExtra(WidgetService.WIDGET_BOOK_ID_INTENT_KEY, -1);	
				showAddReplaceDialog( clickItemId,  appWidgetId,
						 bookId, WidgetService.add_from_blank_item );
			}
		} else if (mainAction.equals(EDITBOOK_ACTION)) {
			// Clare
			  appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			  subaction = intent.getIntExtra(WidgetService.SUBACTION, -1);
			 int itemOrderId = intent.getIntExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, -1);						
			if (appWidgetId == -1 || itemOrderId==-1 ||  subaction==-1)
				return;	
			int goStore=intent.getIntExtra(WidgetService.WIDGET_ADDBOOK_ACTIVITY_FLAG, -1);
			if(goStore==-1)
			{
				try{//Allen catch the NullPointerException when the notebook is not exist
					editNotebook(intent, appWidgetId, itemOrderId, subaction);	
				}
				catch(NullPointerException e){
					e.printStackTrace();
					updateAllWidget();
					return;
				}
			}
			else
			{
				int clickItemId = intent.getIntExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, -1);
				long bookId = intent.getLongExtra(WidgetService.WIDGET_BOOK_ID_INTENT_KEY, -1);	
				showAddReplaceDialog( clickItemId,  appWidgetId,
						 bookId, WidgetService.replace_from_book_item );
			}
						
		} else if (mainAction.equals(ALLNOTEBOOK_TEMPLATE_ACTION)) {
			// Siupo
			appWidgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

			clickID = intent.getIntExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, -1);
			subaction = intent.getExtras().getInt(WidgetService.SUBACTION);
			int goStore=intent.getIntExtra(WidgetService.WIDGET_ADDBOOK_ACTIVITY_FLAG, -1);
			if(goStore==-1)
			{
				dealAllNotebookTemplateAction(appWidgetId,clickID,subaction,intent);	
			}
			else
			{
				int clickItemId = intent.getIntExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, -1);
				long bookId = intent.getLongExtra(WidgetService.WIDGET_BOOK_ID_INTENT_KEY, -1);	
				showAddReplaceDialog( clickItemId,  appWidgetId,
						 bookId, WidgetService.replace_from_book_template_item );
			}
			
		}
		else if(mainAction.equals(MODE_CHANGE_ACTION))
		{
			appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			clickID = intent.getIntExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, 0);
			removeNotebook(intent,appWidgetId,clickID);
		}
		else if(mainAction.equals(PREV_NEXT_BOOK_ACTION))
		{
			appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			subaction = intent.getIntExtra(WidgetService.SUBACTION, -1);
			if(appWidgetId ==-1||subaction ==-1)
			{
				return;
			}
			prevOrNext(intent, appWidgetId,subaction);
		}
		else if(mainAction.equals(MetaData.ACTION_SUPERNOTE_UPDATE)) 
		{
			//Beign Allen++ to update widget
			updateWidget(intent);
			//End Allen
		}	
		else if(mainAction.equals(ACTION_LISTVIEW_SCROLL)){
			//Begin Allen
			short scrollTo = intent.getShortExtra(EXTRA_LISTVIEW_SCROLL_TO, (short) -1);
			appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			int clickItemId = intent.getIntExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, -1);
			
			ArrayList<SingleWidgetItem> list = getOldItemList(appWidgetId);
			int tempIndex = getIndexInList(list,clickItemId);
			if(tempIndex != -1)
			{
				SingleWidgetItem item = list.get(tempIndex);			
				item.setPageListScrollPosition(scrollTo);		
				doUpdateWidget(list,appWidgetId,true);
			}
			//End Allen
		}
	}
	
	private void prevOrNext(Intent intent, int appWidgetId,int subaction)
	{
		ArrayList<SingleWidgetItem> list = getOldItemList(appWidgetId);
		if(list.size() > 0)
		{
			SingleWidgetItem item = list.get(0);
			

			switch(subaction)
			{
			case PREV_NEXT_BOOK_ACTION_PREV:
				item.setNextOrPrevBook(false);
				break;
			case PREV_NEXT_BOOK_ACTION_NEXT:
				item.setNextOrPrevBook(true);
				break;			
			}
			item.setIsChagned(true);
			
			doUpdateWidget(list,appWidgetId, true);
		}
	}
	
	private void removeNotebook(Intent intent, int _appWidgetId,int _itemOrderId) {
		
		ArrayList<SingleWidgetItem> list = getOldItemList(_appWidgetId);
		int tempIndex = getIndexInList(list,_itemOrderId);
		if(tempIndex != -1)
		{
			SingleWidgetItem item = list.get(tempIndex);
			item.setMode(SingleWidgetItem.BLANK_MODE);
			item.setBookId(0L);
			item.setPageId(0L);
			item.setIsChagned(true);
			
			doUpdateWidget(list,_appWidgetId, true);
			MetaData.Changed_Book_List.clear();
			MetaData.Changed_Page_List.clear();
		}

	}

	private void editNotebook(Intent intent, int _appWidgetId,int _itemOrderId, int subaction) {
		// TODO Auto-generated method stub
		switch (subaction) {
		case WidgetService.EDIT_NOTEBOOK_ACTION_ENTERPAEG:
			enterPage(intent);
			return;
		case WidgetService.EDIT_NOTEBOOK_ACTION_LOCK_BOOK:
			lockNotebook(_appWidgetId, _itemOrderId);
			return;
		case WidgetService.EDIT_NOTEBOOK_ACTION_ALLPAGE:			
			displayAllPage(intent);
			return;
		case WidgetService.EDIT_NOTEBOOK_ACTION_REPLACEBOOK:	
			replaceNotebook(intent);
			return;
		case WidgetService.EDIT_NOTEBOOK_ACTION_FIRST_PAGE:

		case WidgetService.EDIT_NOTEBOOK_ACTION_FRONT_PAGE:

		case WidgetService.EDIT_NOTEBOOK_ACTION_NEXT_PAGE:

		case WidgetService.EDIT_NOTEBOOK_ACTION_LAST_PAGE:

		case WidgetService.EDIT_NOTEBOOK_ACTION_FIRSTBUTTON: //Carol

		case WidgetService.EDIT_NOTEBOOK_ACTION_SECONDBUTTON:

		case WidgetService.EDIT_NOTEBOOK_ACTION_THIRDBUTTON:

		case WidgetService.EDIT_NOTEBOOK_ACTION_FOURTHBUTTON:

		case WidgetService.EDIT_NOTEBOOK_ACTION_LASTBUTTON:

		case WidgetService.EDIT_NOTEBOOK_ACTION_NONE:
			changePage(_appWidgetId, _itemOrderId, subaction);
			return;
		case WidgetService.EDIT_NOTEBOOK_ACTION_LOCKED_PASSWORD:
			passwordIdentify(intent);  //check password
			return;
		case WidgetService.EDIT_NOTEBOOK_ACTION_LOCKED_REPLACE:
			replaceNotebook(intent);
			return;
		default:
			break;
		}

	}

	private void lockNotebook(int _appWidgetId, int _itemOrderId) {
		
		ArrayList<SingleWidgetItem> list = getOldItemList(_appWidgetId);
		int tempIndex = getIndexInList(list,_itemOrderId);
		if(tempIndex != -1)
		{
			SingleWidgetItem item = list.get(tempIndex);
			
			item.setLockStatus(1);
			item.setIsChagned(true);
			
			doUpdateWidget(list,_appWidgetId, true);
		}

	}

	private void passwordIdentify(Intent intent) {
		// TODO Auto-generated method stub
		/* create ui */
		
		final int _appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		final int _itemOrderId= intent.getIntExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, -1);
		final long _bookid_edit = intent.getLongExtra(WIDGET_BOOK_ID, -1L);
		
		View v = View.inflate(this, R.layout.widget_password_dialog,null);
		final EditText input_password = (EditText) v.findViewById(R.id.widget_password);
		input_password.setTextColor(Color.BLACK);
		AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
		String passWordTitle=getResources().getString(R.string.password);
		String passWordOK=getResources().getString(android.R.string.ok);//R.string.bookpicker_ok);
		String passWordCANCEL=getResources().getString(android.R.string.cancel);//R.string.bookpicker_cancel);
		builder.setTitle(passWordTitle) //clare
				.setCancelable(true)
				.setPositiveButton(passWordOK,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								
								 SharedPreferences mPreference = getSharedPreferences(
										MetaData.PREFERENCE_NAME,
										Context.MODE_MULTI_PROCESS);
								String oldpassword = mPreference.getString(getResources().getString(R.string.pref_password),null);

								if (input_password.getText().toString().equals(oldpassword)) {
									
									SingleWidgetItem.updataWidgetLockStatus(mContext ,_appWidgetId,_itemOrderId,0);									
									addNotebook(null, _appWidgetId,_itemOrderId, _bookid_edit);
								
								} else {
									input_password.setText("");
									input_password.setHint(R.string.reset_password_dialog_password_invalid); //Carol
									input_password.requestFocus();
								}
							}
						})
				.setNegativeButton(passWordCANCEL,new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								dialog.cancel();
							}
						});
		builder.setView(v);
		AlertDialog d = builder.create();
		d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);				
		d.show();			
	}

	private void replaceNotebook(Intent intent) {
	
		int appWidgetId_replace = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		int itemOrderId_replace=intent.getIntExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, -1);
		long bookid_replace=intent.getLongExtra(WIDGET_BOOK_ID, -1L);
		showAddReplaceDialog(itemOrderId_replace,appWidgetId_replace,
				bookid_replace,  WidgetService.replace_from_book_item);
	}

	public void changePage(int _appWidgetId, int _itemOrderId,int pageChangeMode) {
		
		
		ArrayList<SingleWidgetItem> list = getOldItemList(_appWidgetId);
		int tempIndex = getIndexInList(list,_itemOrderId);
		if(tempIndex != -1)
		{
			SingleWidgetItem item = list.get(tempIndex);
			
			item.setFlipCommand(pageChangeMode);
			item.setIsChagned(true);
			
			doUpdateWidget(list,_appWidgetId, true);
		}

	}

	public void displayAllPage(Intent intent) {
		long bookid_allpage=intent.getLongExtra(WidgetService.WIDGET_BOOK_ID, -1L);
		Intent allPageIntent=new Intent();
		allPageIntent.setClass(this, PickerActivity.class);
		allPageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		allPageIntent.putExtra(MetaData.BOOK_ID, bookid_allpage);
		allPageIntent.putExtra(MetaData.START_FROM_WIDGET, true);//Allen
		startActivity(allPageIntent);
	}

	public void enterPage(Intent intent) {
		long bookid_enterpage=intent.getLongExtra(WIDGET_BOOK_ID, -1L);
		long pageid_enterpage=intent.getLongExtra(WIDGET_PAGE_ID, -1L);
		
		NoteBook mNotebook = BookCase.getInstance(this).getNoteBook(bookid_enterpage);		
		
		Intent enterPageIntent=new Intent();
		enterPageIntent.setClass(this, EditorActivity.class);
		enterPageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		enterPageIntent.putExtra(MetaData.BOOK_ID, bookid_enterpage);
		enterPageIntent.putExtra(MetaData.PAGE_ID,pageid_enterpage );
		enterPageIntent.putExtra(MetaData.IS_NEW_PAGE, false);
		
		if(mNotebook.getTemplate() != MetaData.Template_type_normal&&
				mNotebook.getTemplate() != MetaData.Template_type_blank)
		{
			intent.putExtra(MetaData.TEMPLATE_BOOK_NEW, 1L);//not 0 is OK;
		}
		enterPageIntent.putExtra(MetaData.START_FROM_WIDGET, true);//Allen
		startActivity(enterPageIntent);
	}
	public void dealAllNotebookTemplateAction(int appWidgetId,int itemID,int subaction,Intent intent)
	{
		switch(subaction)
		{
		case ALLNOTEBOOK_TEMPLATE_SUBACTION_SHOWMYBOOK:
			onbtMyNotebooksAction(appWidgetId,itemID,intent);//Show all books
			break;
		case ALLNOTEBOOK_TEMPLATE_SUBACTION_ADDNEWBOOK:
			onbtAddNotebookAction(appWidgetId,itemID,intent);//show the bookType that can be created;
			break;
		case ALLNOTEBOOK_TEMPLATE_SUBACTION_CLICKITEM:
			onItemClick(appWidgetId,itemID,intent);//GridView item which is clicked;
			break;
		case ALLNOTEBOOK_TEMPLATE_SUBACTION_REPLACEWDBOOK:
			onbtReplaceAction(appWidgetId,itemID,intent);
			break;
		default:
			break;
		}
	}
	
	//Update the RemoteViews;
	public void onUpdateSubRemoteViews(int appWidgetId,int itemID,Intent intent,RemoteViews remoteViews,
			int showStatus)
	{
		
		ArrayList<SingleWidgetItem> list = getOldItemList(appWidgetId);
		int tempIndex = getIndexInList(list,itemID);
		if(tempIndex != -1)
		{
			SingleWidgetItem item = list.get(tempIndex);
			
			item.setMode(SingleWidgetItem.BOOK_TEMPLATE_MODE);
			item.setAllBookTemplateStatus(showStatus);
			item.setIsChagned(true);
			
			doUpdateWidget(list,appWidgetId, true);
		}

	}
	void getAdapterBookData(int appWidgetId,int itemID,int mShowBook,RemoteViews remoteViews)
	{
		int gridID = -1;
		int emptyID = -1;
		Intent remoteIntent = new Intent();

		emptyID = R.id.empty_gridView1;
		if(mShowBook==1)
		{
			gridID = R.id.gridView1; //Carol
			remoteIntent.setAction("THE 1th ITEM DISPLAY ALL NOTEBOOK");
		}
		else
		{
			int deviceType = mContext.getResources().getInteger(R.integer.device_type);
			if(deviceType == MetaData.DEVICE_TYPE_A86)  //Carol
				gridID = R.id.gridView1_template;
			else
				gridID = R.id.gridView1;
			remoteIntent.setAction("THE 1th ITEM SHOW THE TEMPLATE");
		}
		
		remoteIntent.setClass(this, WidgetGridViewService.class);//start service；
		remoteIntent.putExtra("mShowBook", mShowBook);//add tag
        remoteIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        remoteIntent.setData(Uri.parse("custom"+System.currentTimeMillis()));
        //In this place, can update the subRemoteViews;
        remoteViews.setRemoteAdapter(gridID, remoteIntent);
        remoteViews.setEmptyView(gridID, emptyID);
        
        setBtBackground(itemID,mShowBook,remoteViews);//Update the button Background
        Log.v("Item ID", String.valueOf(itemID));
        
        Intent onClickIntent = new Intent();
        onClickIntent.setClass(this, WidgetService.class);
        onClickIntent.setAction(ALLNOTEBOOK_TEMPLATE_ACTION);
        onClickIntent.putExtra(SUBACTION, 0);
        onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent onClickPendingIntent = PendingIntent.getService(this, 0,
                onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(gridID, onClickPendingIntent);
	}
	
	public void onbtMyNotebooksAction(int appWidgetId,int itemID,Intent intent)
	{
		RemoteViews remoteViews;
		remoteViews = new RemoteViews(getPackageName(),R.layout.widget_one_layout);
		onUpdateSubRemoteViews(appWidgetId,itemID,intent,remoteViews,SHOWBOOK);
	}

	public void setBtBackground(int itemID,int showBook,RemoteViews remoteViews)
	{
		int btID1 = R.id.btAllBook1;
		int btID2 = R.id.btTemplate1;
		int srcID1,srcID2;
		if(showBook == SHOWBOOK)
		{
			srcID1 = R.drawable.asus_ep_bar_press_1;
			srcID2 = R.drawable.asus_ep_bar_normal_1;
		}
		else
		{
			srcID1 = R.drawable.asus_ep_bar_normal_1;
			srcID2 = R.drawable.asus_ep_bar_press_1;
		}
		remoteViews.setInt(btID1, "setBackgroundResource",srcID1);
		remoteViews.setInt(btID2, "setBackgroundResource",srcID2);
	}
	
	public void onbtAddNotebookAction(int appWidgetId,int itemID,Intent intent)
	{
		RemoteViews remoteViews;
		remoteViews = new RemoteViews(getPackageName(),R.layout.widget_one_layout);
		onUpdateSubRemoteViews(appWidgetId,itemID,intent,remoteViews,ADDTEMPLATE);
	}

	public void onbtReplaceAction(int appWidgetId,int mItemOrderId,Intent intent)
	{
		Log.i("onbtReplaceAction", "onbtReplaceAction");
		showAddReplaceDialog(mItemOrderId,appWidgetId,
				-1,  WidgetService.replace_from_book_template_item);
	}
	
	public void onItemClick(int appWidgetId,int itemID,Intent intent)
	{
		//
		int type = intent.getExtras().getInt("Type");
		switch(type)
		{
		case ITEMCLICK_OPENBOOK:
			long bookID = intent.getExtras().getLong("ID");
			BookCase bookcase = BookCase.getInstance(mContext);
			NoteBook book = bookcase.getNoteBook(bookID);
			if(book != null){ //smilefish fix bug 645600
				Intent strIntent = new Intent();
				strIntent.setClass(this, PickerActivity.class);
				strIntent.putExtra(MetaData.BOOK_ID, bookID);
				strIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				strIntent.putExtra(MetaData.START_FROM_WIDGET, true);//Allen
				startActivity(strIntent);
			}else{
				Toast.makeText(mContext, R.string.all_page_view_empty, Toast.LENGTH_SHORT).show();
			}
			
			break;
		case ITEMCLICK_NEWBOOK:
			int bookType = 0;
			bookType = intent.getExtras().getInt("BookStyle");
			widgetAddNewBook(bookType);
			break;
		case ITEMCLICK_OPENPAGE:
			//open a Page;
			long bID = intent.getExtras().getLong("ID");
			long pageID = intent.getExtras().getLong("PageID");
			int templateType = intent.getExtras().getInt("Template");
			Intent pageIntent = new Intent();
			pageIntent.setClass(mContext, EditorActivity.class);
			pageIntent.putExtra(MetaData.BOOK_ID, bID);
			pageIntent.putExtra(MetaData.PAGE_ID, pageID);
			pageIntent.putExtra(MetaData.IS_NEW_PAGE, false);
			if(templateType != MetaData.Template_type_normal&&
					templateType != MetaData.Template_type_blank)
			{
				intent.putExtra(MetaData.TEMPLATE_BOOK_NEW, 1L);//not 0 is OK;
			}
			pageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
			pageIntent.putExtra(MetaData.START_FROM_WIDGET, true);//Allen
			startActivity(pageIntent);
		default:
			break;
		}
	}

	public void widgetAddNewBook(int bookType)
	{
		int pagesize = MetaData.PAGE_SIZE_PHONE;//default for the PHONE
		if(PickerUtility.getDeviceType(this)>100)
		{
			pagesize = MetaData.PAGE_SIZE_PAD;
		}
		int indexLanguage = -1;
		EditText bookName = null;
		switch(bookType)
		{
		case 0: 
			addNewBook(MetaData.BOOK_COLOR_WHITE,MetaData.Template_type_blank,pagesize,MetaData.BOOK_GRID_BLANK,bookName,indexLanguage);
			break;
		//change template order [Carol]
		case 1://2: 
			addNewBook(MetaData.BOOK_COLOR_WHITE,MetaData.Template_type_normal,pagesize,MetaData.BOOK_GRID_LINE,bookName,indexLanguage);
			break;
		case 2://4: 
			addNewBook(MetaData.BOOK_COLOR_WHITE,MetaData.Template_type_normal,pagesize,MetaData.BOOK_GRID_GRID,bookName,indexLanguage);
			break;
		case 3://1: 
			addNewBook(MetaData.BOOK_COLOR_YELLOW,MetaData.Template_type_blank,pagesize,MetaData.BOOK_GRID_BLANK,bookName,indexLanguage);
			break;
		case 4://3: 
			addNewBook(MetaData.BOOK_COLOR_YELLOW,MetaData.Template_type_normal,pagesize,MetaData.BOOK_GRID_LINE,bookName,indexLanguage);
			break;
		case 5:
			addNewBook(MetaData.BOOK_COLOR_YELLOW,MetaData.Template_type_normal,pagesize,MetaData.BOOK_GRID_GRID,bookName,indexLanguage);
			break;
		case 6:
			addNewBook(MetaData.BOOK_COLOR_WHITE,MetaData.Template_type_meeting,pagesize,MetaData.BOOK_GRID_LINE,bookName,indexLanguage);
			break;
		case 7:
			addNewBook(MetaData.BOOK_COLOR_WHITE,MetaData.Template_type_travel,pagesize,MetaData.BOOK_GRID_LINE,bookName,indexLanguage);
			break;
		case 8:
			addNewBook(MetaData.BOOK_COLOR_WHITE,MetaData.Template_type_todo,pagesize,MetaData.BOOK_GRID_BLANK,bookName,indexLanguage);
			break;	
	    default: break;
		}						
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		Log.i("WidgetService", "onBind()");
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("WidgetService", "onCreate()");
		mContext = this;
		updateAllWidget();
	}
	
	
	/*
	 * Query appWidgetId from database
	 * */
		public boolean  isWidgetIDExist(int appWidgetId) {
			//check whether appWidgetId is existing in the database 
			String selection=MetaData.WidgetTable.WIDGET_ID+"="+appWidgetId;		
			Cursor cursor_widget = this.getContentResolver().query(
					MetaData.WidgetTable.uri,
					null,
					selection,
					null,
					null);
			if (cursor_widget != null && cursor_widget.getCount()>0  ) {			
				if (cursor_widget.moveToFirst()) {
					int widget_id = 0;
					int widget_mode = 0;
					widget_id = cursor_widget.getInt(cursor_widget.getColumnIndex(MetaData.WidgetTable.WIDGET_ID));
					widget_mode = cursor_widget.getInt(cursor_widget.getColumnIndex(MetaData.WidgetTable.WIDGET_MODE));
					
					if(widget_id==appWidgetId)
					{
						Log.v("widget already exists:","widget_id: "+ widget_id +"widget_mode: "+widget_mode);
						cursor_widget.close();
						return true;
					}
					cursor_widget.close();
					return false;
				}			
			}
			
			cursor_widget.close();
			return false;		
		}

	public void addNewWidget2WidgetTable(int appWidgetId,int widgetType  ) {
		Log.v("ContentProvider", "widget not exists!!!");
		ContentValues values = new ContentValues();
		values.put(MetaData.WidgetTable.WIDGET_ID, appWidgetId);
		values.put(MetaData.WidgetTable.WIDGET_MODE, widgetType);//1:oneITem,3:ThreeItem;
		getContentResolver().insert(MetaData.WidgetTable.uri, values);
		Log.v("ContentProvider", "add "+Integer.toString(appWidgetId)+" to database!");
	}

	public Long getCurrentWidgetItemID(int appwidgetId)
	{
		Long res = -1L;
		String where = MetaData.WidgetItemTable.ITEM_WIDGET_ID +" = "+appwidgetId;
		Cursor queryCur = getContentResolver().query(MetaData.WidgetItemTable.uri, null, where, null, null);
		
		if(queryCur!=null && queryCur.getCount()>0)
		{
			queryCur.moveToFirst();
			res = queryCur.getLong(queryCur.getColumnIndex(MetaData.WidgetItemTable.ITEM_BOOK_ID));
		}
		if(queryCur!=null)
		{
			queryCur.close();
		}
		
		return res;
	}
	
	//delete the Widget infomation from table
	public void deleteNewWidgetFromWidgetTable(int appWidgetId) {
		getContentResolver().delete(MetaData.WidgetTable.uri, MetaData.WidgetTable.WIDGET_ID + " = " + appWidgetId, null);
		
		getContentResolver().delete(MetaData.WidgetItemTable.uri, MetaData.WidgetItemTable.ITEM_WIDGET_ID + " = " + appWidgetId, null);
	}
		
	/**
	 * Add a new book
	 * @author Allen_Lai@asus.com.cn
	 * @param bookColor the color of book
	 * @param templateType the template
	 * @param bookStyle the style
	 * @param bookNameEditText the book name EditText
	 */
	private void addNewBook(int bookColor,int templateType,int pageSize,int bookStyle,EditText bookNameEditText,int indexLanguage)
	{
		if(getResources().getInteger(R.integer.device_type)>100){
			//PAD style-add book from NoteBookPickerActivity
			try {
				Intent intent = new Intent();
				intent.setClass(this, NoteBookPickerActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(MetaData.STAY_NOTEBOOKPICKER_ACTIVITY, true);
				intent.putExtra(MetaData.DISPLAY_ADDBOOK_DIALOG, true);
				intent.putExtra(MetaData.START_FROM_WIDGET, true);
				this.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			//phone style
			NoteBook book = new NoteBook(this);
			if (bookNameEditText == null
					|| bookNameEditText.getText().toString().equals("")) {
				book.setTitle(PickerUtility
						.getDefaultBookName(getApplicationContext()));
			} else {
				book.setTitle(bookNameEditText.getText().toString());
			}
			int color = bookColor;
			int template = templateType;
			book.setTemplate(template);
			int style = bookStyle;
			book.setPageSize(pageSize);
			book.setBookColor(color);
			book.setGridType(style);
			book.setIsLocked(false);
			book.setIndexLanguage(indexLanguage);
			NotePage notepage = new NotePage(getApplicationContext(),
					book.getCreatedTime());
			//begin wendy allen++
			notepage.setTemplate(template);
			//end wendy allen++
			
			book.addPage(notepage);
			BookCase mBookcase = BookCase.getInstance(this);
			mBookcase.addNewBook(book);

			if ( mAddBookDialog != null ) 
			{
				mAddBookDialog.dismiss();
			}

			// BEGIN: Better
			PageDataLoader loader = PageDataLoader.getInstance(this);
			loader.load(notepage);
			// END: Better
			try {
				Intent intent = new Intent();
				intent.setClass(this, EditorActivity.class);
				intent.putExtra(MetaData.BOOK_ID, notepage.getOwnerBookId());
				intent.putExtra(MetaData.PAGE_ID, notepage.getCreatedTime());
				intent.putExtra(MetaData.IS_NEW_PAGE, true);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				if(book.getTemplate() != MetaData.Template_type_normal&&
						book.getTemplate() != MetaData.Template_type_blank)
				{
					intent.putExtra(MetaData.TEMPLATE_BOOK_NEW, 1L);//not 0 is OK;
				}
				intent.putExtra(MetaData.START_FROM_WIDGET, true);//Allen
				this.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// End, James5
		}
	}

	
	public void showAddReplaceDialog(int clickItemId, int widgetId,
			long bookId, int add_replace_flag) {
		//Begin Allen
		Intent showAllBookIntent = new Intent();
		showAllBookIntent.setClass(this, WidgetShowAllBookActivity.class);
		showAllBookIntent.putExtra(WidgetShowAllBookActivity.EXTRA_CLICKITEM_ID, clickItemId);
		showAllBookIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId);
		showAllBookIntent.putExtra(WIDGET_BOOK_ID,bookId);
		showAllBookIntent.putExtra(WidgetShowAllBookActivity.EXTRA_ADD_REPLACE_FLAG,add_replace_flag);
		showAllBookIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(showAllBookIntent);
		//End Allen
	}
	//Begin Siupo
	public void getAdapterData(int itemMode,int isLocked,int appWidgetID,int itemID,long bookID,int itemWidgetShow,RemoteViews remoteViews)
	{
		if(itemMode == SingleWidgetItem.BOOK_MODE)
		{
			if(isLocked==1)
			{
				getAdapterLockedPageData(appWidgetID,itemID,bookID,remoteViews);
			}
			else
			{
				getAdapterPageData(appWidgetID,itemID,bookID,remoteViews);
			}
		}
		else if(itemMode == SingleWidgetItem.BOOK_TEMPLATE_MODE)
		{
			getAdapterBookData(appWidgetID,itemID,itemWidgetShow,remoteViews);
		}
		else
		{
			//NOTHING
		}
	}
	//End Siupo
	

	
	private ArrayList<SingleWidgetItem> getOldItemList(int appWidgetId)
	{
		int tempindex = -1;
		ArrayList<SingleWidgetItem> resultArray = new ArrayList<SingleWidgetItem>();
		String selection=MetaData.WidgetItemTable.ITEM_WIDGET_ID+"="+appWidgetId;
		
		Cursor cursor_widget_item = this.getContentResolver().query(
			MetaData.WidgetItemTable.uri,null,selection,null,WidgetItemTable.ID + " ASC ");	//需要排序，根据item_order_id排序
		
		if (cursor_widget_item != null   ) 
		{			
			if (cursor_widget_item.getCount()>0 && cursor_widget_item.moveToFirst()) 
			{						
				do{
					Log.v("WidgetService", "do while");
					int idInDataBase			= cursor_widget_item.getInt(MetaData.WidgetItemTable.INDEX_ID);
					int app_widget_id 			= cursor_widget_item.getInt(MetaData.WidgetItemTable.INDEX_ITEM_WIDGET_ID);
					int item_order_id  			= cursor_widget_item.getInt(MetaData.WidgetItemTable.INDEX_ITEM_ORDER_ID);
					int item_mode      			= cursor_widget_item.getInt(MetaData.WidgetItemTable.INDEX_ITEM_MODE);
					Long item_book_id  			= cursor_widget_item.getLong(MetaData.WidgetItemTable.INDEX_ITEM_BOOK_ID);
					Long item_page_id  			= cursor_widget_item.getLong(MetaData.WidgetItemTable.INDEX_ITEM_PAGE_ID);
					int item_all_book_template  = cursor_widget_item.getInt(MetaData.WidgetItemTable.INDEX_ITEM_ALLBOOK_ALLTEMPLATE);
					int item_lock_state			= cursor_widget_item.getInt(MetaData.WidgetItemTable.INDEX_ITEM_LOCK_STATE);

					SingleWidgetItem mSingleWidgetItem = new SingleWidgetItem(idInDataBase,this,app_widget_id, 
							item_order_id,item_mode,item_book_id,item_page_id,
							item_all_book_template,item_lock_state,R.id.FrameLayout_middle);	
					
					tempindex = getIndexInList(resultArray,item_order_id);
					if(tempindex != -1)
					{
						mSingleWidgetItem.deleteWidgetItem();
						resultArray.remove(tempindex);
					}
					
					resultArray.add(mSingleWidgetItem);//put it into array
					
				}while (cursor_widget_item.moveToNext());				
			}
			
			cursor_widget_item.close();	
		}
		
		return resultArray;
	}
	
	private int getIndexInList(ArrayList<SingleWidgetItem> list,int orderid)
	{
		int length = list.size();
		for(int i = 0; i < length ; i++ )
		{
			if(list.get(i).getItemOrderID() == orderid)
			{
				return i;
			}
		}
		
		return -1;
	}
	
	private void doUpdateWidget(ArrayList<SingleWidgetItem> resultArray,int appWidgetId ,boolean partiallyUpdate)
	{		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		RemoteViews remoteViews;
		remoteViews = new RemoteViews(getPackageName(),R.layout.widget_one_layout);
		if(!partiallyUpdate){
			for(SingleWidgetItem item : resultArray)
			{
				remoteViews.removeAllViews(item.mFrameLayoutId);			
				remoteViews.addView(item.mFrameLayoutId,item.getRemoteViews());
				
				
				if(item.getIsChanged() || MetaData.IS_STATUS_CHANGED)
				{
					MetaData.IS_STATUS_CHANGED = false;
					item.writeToDataBase();
				}
				
				getAdapterData(item.getMode(),
							item.getLockStatus(),
							appWidgetId,
							item.getItemOrderID(),
							item.getBookId(),
							item.getAllBookTemplateStatus(),
							remoteViews);//Siupo
				
			}
		}
		else{//Allen++
			for(SingleWidgetItem item : resultArray){
				if(!item.updatePageListScrollPosition(remoteViews))
				{	
					if(item.getIsChanged())
					{
						remoteViews.removeAllViews(item.mFrameLayoutId);			
						remoteViews.addView(item.mFrameLayoutId,item.getRemoteViews());
						item.writeToDataBase();
						
						getAdapterData(item.getMode(),
								item.getLockStatus(),
								appWidgetId,
								item.getItemOrderID(),
								item.getBookId(),
								item.getAllBookTemplateStatus(),
								remoteViews);//Siupo
					}					
				}
			}
		}			
		
		if(mContext.getResources().getConfiguration().smallestScreenWidthDp >= 720 &&
				resultArray.size()>0 && resultArray.get(0).getMode() == SingleWidgetItem.BOOK_MODE)
		{
			setLeftRightButtonInOneLayout(remoteViews,appWidgetId);
			setLeftRightButtons(remoteViews,appWidgetId);
		}
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

	}
//Begin Siupo  get the data for the Locked book
	public void getAdapterLockedPageData(int appWidgetId,int itemID,long bookID,RemoteViews remoteViews)
	{
		int gridID = -1;
		int emptyID = -1;
		Intent remoteIntent = new Intent();
		remoteIntent.setClass(this, WidgetGridViewService.class);//start the service；
		gridID = R.id.GV1;
		emptyID = R.id.empty_gv1;
		String strData = "THE 1th ITEM SHOW THE LOCKED PAGES "+
				String.valueOf(itemID) + " "+String.valueOf(bookID);
		remoteIntent.setAction(strData);

		//in order to distinguish the intent;
		remoteIntent.setData(Uri.parse(remoteIntent.toUri(Intent.URI_INTENT_SCHEME)));
		remoteIntent.putExtra("mShowBook", WidgetService.SHOWLOCKEDPAGE);//add tag
        remoteIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        remoteIntent.putExtra("BookID", bookID);
        //In this place, can update the subRemoteViews;
        if(getResources().getInteger(R.integer.device_type)>100&&
        		getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT){
        	remoteViews.setRemoteAdapter(gridID, remoteIntent); //portrait mode, display all pages [Carol]
        }
        remoteViews.setEmptyView(gridID, emptyID);
        Log.v("Item ID", String.valueOf(itemID));
        
        Intent onClickPageIntent = new Intent();
        onClickPageIntent.setClass(this, WidgetService.class);
        onClickPageIntent.setAction(ALLNOTEBOOK_TEMPLATE_ACTION);
        onClickPageIntent.putExtra(SUBACTION, 0);
        onClickPageIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        onClickPageIntent.setData(Uri.parse(onClickPageIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent onClickPagePendingIntent = PendingIntent.getService(this, 0,
                onClickPageIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(gridID, onClickPagePendingIntent);
	}
	//End Siupo
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		updateAllWidget();
	}
	
	private void updateAllWidget()
	{
		Class<?> cls = WidgetOneProvider.class;
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(mContext, cls));
		for(int appWidgetId:ids)
		{
			ArrayList<SingleWidgetItem> list = getOldItemList(appWidgetId);
			if (list.size() == 0)
            {   
                initWidgetRemoteView(appWidgetId, UPDATE_ACTION_ADD_WIDGET);
            }else{
            	doUpdateWidget(list,appWidgetId, false);
            }
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onStart(intent , startId);
		return START_NOT_STICKY;
	}
	
}
