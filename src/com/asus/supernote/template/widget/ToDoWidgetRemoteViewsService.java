package com.asus.supernote.template.widget;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.template.TemplateToDoUtility;

public class ToDoWidgetRemoteViewsService extends RemoteViewsService {
	
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		Log.e("Allen","new ToDoRemoteViewsFactory "+ System.currentTimeMillis());
		return new ToDoRemoteViewsFactory(this.getApplicationContext(), intent);
	}

	public class ToDoRemoteViewsFactory implements RemoteViewsFactory {

		private ArrayList<ToDoWidgetListItem> mToDoWidgetItems = new ArrayList<ToDoWidgetListItem>();
		private Context mContext;
		private BookCase mBookcase = null;
		private short mAdapterType = ToDoWidgetItem.ADAPTER_TYPE_ALL_TODOS;
		private Long mCurrentBookId = (long) -1;
		private short mSortBy = ToDoComparator.SORT_BY_TIME;//default sort by time
		private List<NoteBook> mToDoBookList = null;
		private int mWidgetId = -1;
		
		public ToDoRemoteViewsFactory(Context context, Intent intent) {
			mContext = context;
			mBookcase = BookCase.getInstance(context);
			mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		private void initDataSet() {
			for(ToDoWidgetListItem item : mToDoWidgetItems){
				item.recycleBitmap();
			}
			mToDoWidgetItems.clear();
			
			if (mAdapterType == ToDoWidgetItem.ADAPTER_TYPE_NOTEBOOK) {
				NoteBook book = mBookcase.getNoteBook(mCurrentBookId);
				if (book == null) {
					return;
				}
				LoadToDoBook(book);
			} else if (mAdapterType == ToDoWidgetItem.ADAPTER_TYPE_ALL_TODOS) {
				mToDoBookList = mBookcase.getToDoTemplateBookList();				
				if(mToDoBookList.size() == 0){ //no ToDo notebook exist
					return;
				}
				for(NoteBook book : mToDoBookList){
					LoadToDoBook(book);
				}
			}
			sortToDoWidgetItems(mSortBy);
			insertSeperater(mSortBy);
		}
		
		private void LoadToDoBook(NoteBook book){ 
			List<Long> pageIds = book.getPageOrderList();
			for (Long pageId : pageIds) {
				NotePage page = book.getNotePage(pageId);
				ArrayList<NoteItemArray> allNoteItems = page
						.loadNoteItems(mContext);
				if(allNoteItems == null){
					continue;
				}
				ToDoWidgetListItem contentItem = null;
				String pageTitle = null;
				int position = 0;
				for (NoteItemArray noteItems : allNoteItems) {
					switch(noteItems.getTemplateItemType()){
					case NoteItemArray.TEMPLATE_CONTENT_TODO_TITLE:
						pageTitle = noteItems.getNoteItems().get(0).getText();
						break;
					case NoteItemArray.TEMPLATE_SEPERATER_TODO_NEW_ITEM_BEGIN:
						contentItem = new ToDoWidgetListItem(mContext);
						contentItem.itemType = ToDoWidgetListItem.ITEM_TYPE_CONTENT;
						contentItem.bookTitle = book.getTitle();
						contentItem.bookId = book.getCreatedTime();
						contentItem.pageId = pageId;
						contentItem.pageTitle = pageTitle;
						break;
					case NoteItemArray.TEMPLATE_CONTENT_TODO_EDIT:
						contentItem.noteItems = noteItems;
						position++;
						contentItem.positon = position;
						break;
					case NoteItemArray.TEMPLATE_CONTENT_TODO_CHECKBOX:
						contentItem.isChecked = noteItems.getNoteItems().get(0).getText().equals("true") ? true : false;
						break;
					case NoteItemArray.TEMPLATE_CONTENT_TODO_PRIORITY:
						try{
							contentItem.priority = Short.parseShort(noteItems.getNoteItems().get(0).getText());
						}
						catch(NumberFormatException e){
							e.printStackTrace();
						}
						break;
					case NoteItemArray.TEMPLATE_CONTENT_TODO_MODIFY_TIME:
						try{
							contentItem.lastModifyTime = Long.parseLong(noteItems.getNoteItems().get(0).getText());
						}
						catch(NumberFormatException e){
							e.printStackTrace();
						}
						break;
					case NoteItemArray.TEMPLATE_SEPERATER_TODO_NEW_ITEM_END:
						/* filter empty todo item */
						if(contentItem.noteItems.getNoteItems().get(0).getText().length()!=0){
							mToDoWidgetItems.add(contentItem);
						}
						break;
					}
				}
			}
		}
		
		private void sortToDoWidgetItems(short sortBy){
			switch(sortBy){
			case ToDoComparator.SORT_BY_PAGE:
				Comparator<ToDoWidgetListItem> pageComparator = new ToDoComparator(ToDoComparator.SORT_BY_PAGE);
				Collections.sort(mToDoWidgetItems,pageComparator);
				break;
			case ToDoComparator.SORT_BY_TIME:
				Comparator<ToDoWidgetListItem> timeComparator = new ToDoComparator(ToDoComparator.SORT_BY_TIME);
				Collections.sort(mToDoWidgetItems,timeComparator);
				break;
			}
		}
		
		private void insertSeperater(short sortBy){
			switch(sortBy){
			case ToDoComparator.SORT_BY_PAGE:
				ArrayList<ToDoWidgetListItem> itemsSortByPage = new ArrayList<ToDoWidgetListItem>();
				long currentPageId = -1;
				for(ToDoWidgetListItem item : mToDoWidgetItems){
					long pageId = item.pageId;
					 if(pageId != currentPageId){
						 ToDoWidgetListItem seperaterItem = new ToDoWidgetListItem(mContext);
						 seperaterItem.itemType = ToDoWidgetListItem.ITEM_TYPE_SEPERATER;
						 
						 NoteBook book = mBookcase.getNoteBook(item.bookId);
						 if (book == null) {
								return;
							}
						 int index =  book.getPageOrderList().indexOf(pageId) + 1;//start width 1
						 seperaterItem.seperaterLeft = getResources().getString(R.string.SearchResultItemInfo_Page_Space) + index+"/"+book.getPageOrderList().size();
						 
						 String pageTitleWithoutOBJ = item.pageTitle;
						 pageTitleWithoutOBJ = pageTitleWithoutOBJ.replace((char) 65532, ' ');//replace OBJ with space
						 
						 seperaterItem.seperaterRight = pageTitleWithoutOBJ;
						 itemsSortByPage.add(seperaterItem);
						 currentPageId = pageId;
					 }
					 itemsSortByPage.add(item); 
				}
				mToDoWidgetItems = itemsSortByPage;
				break;
			case ToDoComparator.SORT_BY_TIME:
				ArrayList<ToDoWidgetListItem> itemsSortByTime = new ArrayList<ToDoWidgetListItem>();
				String lastModifyDay = "";
				boolean isChecked = false;
				for(ToDoWidgetListItem item : mToDoWidgetItems){
					 if(item.isChecked != isChecked){
						 ToDoWidgetListItem seperaterItem = new ToDoWidgetListItem(mContext);
						 seperaterItem.itemType = ToDoWidgetListItem.ITEM_TYPE_SEPERATER;
						 seperaterItem.seperaterLeft = mContext.getResources().getString(R.string.todo_item_completed);
						 itemsSortByTime.add(seperaterItem);
						 isChecked = item.isChecked;
					 }
					 else if(isChecked ==false && !item.getLastModifyDay().equals(lastModifyDay)){
						 ToDoWidgetListItem seperaterItem = new ToDoWidgetListItem(mContext);
						 seperaterItem.itemType = ToDoWidgetListItem.ITEM_TYPE_SEPERATER;
						 seperaterItem.seperaterLeft = DateFormat.getDateInstance(DateFormat.FULL).format(new Date(item.lastModifyTime));
						 itemsSortByTime.add(seperaterItem);
						 lastModifyDay = item.getLastModifyDay();
					 }
					 itemsSortByTime.add(item); 
				}
				mToDoWidgetItems = itemsSortByTime;
				break;
			}
		}
		
		@Override
		public int getCount() {
			RemoteViews remoteView = new RemoteViews(getPackageName(),
					R.layout.template_todo_widget_layout);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
			
			mToDoBookList = mBookcase.getToDoTemplateBookList();
			if(mToDoBookList==null||mToDoBookList.size()==0){ //fix bug[TT398756] Carol
				remoteView.setViewVisibility(R.id.todo_widget_no_item_tips_layout, View.VISIBLE);
				remoteView.setViewVisibility(R.id.button_panel, View.GONE);
			}else if(mToDoWidgetItems.size() == 0){
				remoteView.setViewVisibility(R.id.todo_widget_no_item_tips_layout, View.VISIBLE);
				remoteView.setViewVisibility(R.id.button_panel, View.VISIBLE);
				remoteView.setViewVisibility(R.id.todo_widget_sort_button, View.GONE);
			}else{
				remoteView.setViewVisibility(R.id.todo_widget_no_item_tips_layout, View.GONE);
				remoteView.setViewVisibility(R.id.todo_widget_sort_button, View.VISIBLE);
				remoteView.setViewVisibility(R.id.button_panel, View.VISIBLE);
			}
			appWidgetManager.partiallyUpdateAppWidget(mWidgetId, remoteView);
			return mToDoWidgetItems.size();
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public RemoteViews getLoadingView() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public RemoteViews getViewAt(int position) {
			RemoteViews rv = null;
			if (position >= mToDoWidgetItems.size()) {
				return rv;
			}
			ToDoWidgetListItem item = mToDoWidgetItems.get(position);
			if (item.itemType == ToDoWidgetListItem.ITEM_TYPE_CONTENT) {
				rv = new RemoteViews(mContext.getPackageName(),
						R.layout.template_todo_widget_listview_item);
				rv.setImageViewBitmap(R.id.todo_widget_item_content,
						item.getContentBitmap());
				//emmanual to fix bug 490560
				if(item.priority != TemplateToDoUtility.TODO_PRIORITY_NORMAL)
		        {		        	
		        	Drawable d = mContext.getResources().getDrawable(item.getPriorityImageViewResourceId(item.priority));
		        	Bitmap bp = ((BitmapDrawable)d).getBitmap();
		        	rv.setImageViewBitmap(R.id.todo_widget_item_priority, bp);
		        }else{//emmanual to fix bug 517162
		        	rv.setImageViewBitmap(R.id.todo_widget_item_priority, null);
		        }
				if (item.isChecked) {
					rv.setImageViewResource(R.id.todo_widget_checkbox,
							R.drawable.asus_widget_supernote_check_selector);
				} else {
					rv.setImageViewResource(R.id.todo_widget_checkbox,
							R.drawable.asus_widget_supernote_uncheck_selector);
				}

				rv.setTextViewText(R.id.todo_widget_item_time_textview, getItemTextString(item));
				
				Bundle extras = new Bundle();
				extras.putLong(ToDoWidgetService.EXTRA_TODO_BOOK_ID, item.bookId);
				extras.putLong(ToDoWidgetService.EXTRA_TODO_PAGE_ID, item.pageId);
				extras.putInt(ToDoWidgetService.EXTRA_TODO_WIDGET_TODO_ITEM_POSITION, item.positon);
				Intent deleteFillInIntent = new Intent();
				extras.putShort(ToDoWidgetService.EXTRA_TODO_WIDGET_EDIT_TODO_ITEM_TYPE, ToDoWidgetService.EDIT_TODO_ITEM_TYPE_DELETE);
				deleteFillInIntent.putExtras(extras);
	            rv.setOnClickFillInIntent(R.id.todo_widget_delete_item_button, deleteFillInIntent);
	            
	            Intent checkFillInIntent = new Intent();
				extras.putShort(ToDoWidgetService.EXTRA_TODO_WIDGET_EDIT_TODO_ITEM_TYPE, ToDoWidgetService.EDIT_TODO_ITEM_TYPE_CHECK);
	            checkFillInIntent.putExtras(extras);
	            rv.setOnClickFillInIntent(R.id.todo_widget_checkbox, checkFillInIntent);
	            
	            Intent clickItemFillInIntent = new Intent();
				extras.putShort(ToDoWidgetService.EXTRA_TODO_WIDGET_EDIT_TODO_ITEM_TYPE, ToDoWidgetService.EDIT_TODO_ITEM_TYPE_CLICK_ITEM);
				clickItemFillInIntent.putExtras(extras);
				rv.setOnClickFillInIntent(R.id.contentLayout, clickItemFillInIntent);//noah
			} else if (item.itemType == ToDoWidgetListItem.ITEM_TYPE_SEPERATER) {
				rv = new RemoteViews(mContext.getPackageName(),
						R.layout.template_todo_widget_listview_item_seperator);
				rv.setTextViewText(R.id.todo_widget_item_seperator_textview_l, item.seperaterLeft);
				//removed by emmanual to fix bug 454503
				//rv.setTextViewText(R.id.todo_widget_item_seperator_textview_r, item.seperaterRight);
			}
			return rv;
		}
		
		private String getItemTextString(ToDoWidgetListItem item){
			//begin smilefish fix bug 440370
            String formatStr = "hh:mm a";
            if(android.text.format.DateFormat.is24HourFormat(mContext))
            	formatStr = "kk:mm";
            SimpleDateFormat df = new SimpleDateFormat(formatStr);
            //end smilefish
			String result = df.format(new Date(item.lastModifyTime));
			String pageTitleWithoutOBJ = item.pageTitle;
			pageTitleWithoutOBJ = pageTitleWithoutOBJ.replace((char) 65532, ' ');//replace OBJ with space
			return result+","+item.bookTitle;//+"," + pageTitleWithoutOBJ;
		}	
		
		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onDataSetChanged() {
			updateAdapterData();
			initDataSet();
		}

		@Override
		public void onDestroy() {
			// TODO Auto-generated method stub
		}

		@Override
		public void onCreate() {
			// TODO Auto-generated method stub
		}
		
		private void updateAdapterData() {
			Cursor cursor = null;
			try {
				/* load the widget data from database */
				cursor = getContentResolver().query(
						MetaData.ToDoWidgetTable.uri, null,
						MetaData.ToDoWidgetTable.WIDGET_ID + " = " + mWidgetId,
						null, null);
				if (cursor != null) {
					if (cursor.getCount() > 0 && cursor.moveToFirst()) {
						mAdapterType = cursor
								.getShort(MetaData.ToDoWidgetTable.INDEX_ADAPTER_TYPE);
						mCurrentBookId = cursor
								.getLong(MetaData.ToDoWidgetTable.INDEX_BOOK_ID);
						mSortBy = cursor
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
		}
	}
}
