package com.asus.supernote.template.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.NoteBook;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ToDoWidgetDialogActivity extends Activity{

	public static final short DIALOG_TYPE_SORT_BY = 0;
	public static final short DIALOG_TYPE_SELECT_BOOK = 1;
	public static final short DIALOG_TYPE_ADD_TO = 2;
	private String selectItem = ""; //smilefish
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		short dialogType = intent.getShortExtra(ToDoWidgetService.EXTRA_TODO_WIDGET_DIALOG_TYPE, (short) -1);
		switch (dialogType) {
		case DIALOG_TYPE_SORT_BY:
			short currentSortBy = intent.getShortExtra(ToDoWidgetService.EXTRA_TODO_WIDGET_SORT_BY,
					ToDoComparator.SORT_BY_TIME);
			prepareSortByDialog(widgetId, currentSortBy);
			break;
		case DIALOG_TYPE_ADD_TO:
			prepareBookListDialog(widgetId, dialogType);
			break;
		case DIALOG_TYPE_SELECT_BOOK:
			selectItem = intent.getStringExtra(ToDoWidgetService.EXTRA_TODO_WIDGET_SELECT_ITEM); //smilefish
			prepareBookListDialog(widgetId,dialogType);
			break;
		default:
			finish();
		}
		super.onCreate(savedInstanceState);
	}
	
	private void prepareSortByDialog(final int widgetId,short currentSortBy){
		setContentView(R.layout.template_todo_widget_activity_dialog_listview_layout);
		setTitle(R.string.Sort);
		ListView sortByList = (ListView) findViewById(R.id.todo_widget_activity_dialog_listview);

        String[] valueTexts = getResources().getStringArray(R.array.todo_widget_sort);
       
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();   
		
		HashMap<String, Object> item1 = new HashMap<String, Object>();  
		item1.put("ItemImage", R.drawable.asus_todo_widget_sort_page);
		item1.put("ItemText", valueTexts[0]);
		listItem.add(item1);
		
		HashMap<String, Object> item2 = new HashMap<String, Object>();  
		item2.put("ItemImage", R.drawable.asus_todo_widget_sort_time);
		item2.put("ItemText", valueTexts[1]);
		listItem.add(item2);
		
		SimpleAdapter simpleAdapter = new SimpleAdapter(this,listItem,R.layout.template_todo_widget_sortby_list_item_view,
				new String[] {"ItemImage","ItemText"},new int[] {R.id.list_item_image,R.id.list_item_text});   
        
        sortByList.setAdapter(simpleAdapter);
        sortByList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	Bundle bundle = new Bundle();
            	bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            	bundle.putShort(ToDoWidgetService.EXTRA_TODO_WIDGET_SORT_BY, (short)position);
            	startService(getResultIntent(ToDoWidgetService.ACTION_TODO_WIDGET_SORT_BY_RESULT, bundle));
            	finish();
            }
        });
        sortByList.setItemChecked(currentSortBy,true);
        
        Button cancelButton = (Button)findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();				
			}
		});	
	}
	
	private void prepareBookListDialog(final int widgetId, final short dialogType){
		int titleResourseId = 0;
		switch(dialogType){
		case DIALOG_TYPE_ADD_TO:
			titleResourseId = R.string.todo_widget_add_to;
			break;
		case DIALOG_TYPE_SELECT_BOOK:
			titleResourseId = R.string.select_all;
			break;
		default:finish();
		}
		
		BookCase mBookcase = BookCase.getInstance(this);
		final List<NoteBook> mToDoBookList = mBookcase.getToDoTemplateBookList();

		setContentView(R.layout.template_todo_widget_activity_dialog_listview_layout);
		setTitle(titleResourseId);
		ListView addToList = (ListView) findViewById(R.id.todo_widget_activity_dialog_listview);

        ArrayList<String> bookNames = new ArrayList<String>();
        if(dialogType == DIALOG_TYPE_SELECT_BOOK){
        	bookNames.add(getResources().getString(R.string.todo_widget_all_todos));
        }
        for(NoteBook book: mToDoBookList){
        	bookNames.add(book.getTitle());
        }
        addToList.setAdapter(new ArrayAdapter<String>(this, R.layout.asus_list_item_single_choice, bookNames));
        //begin smilefish
        addToList.setChoiceMode(ListView.CHOICE_MODE_SINGLE); 
        if(dialogType == DIALOG_TYPE_SELECT_BOOK)
        {        
	        int index = bookNames.indexOf(selectItem);
	        if(index < 0)
	        	index = 0;
	        addToList.setItemChecked(index, true); 
        }
        else
        	addToList.setItemChecked(0, true); 
        //end smilefish
        addToList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	Bundle bundle = new Bundle();
            	bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            	switch(dialogType){
        		case DIALOG_TYPE_ADD_TO:
        			Intent intent = getIntent();
        			bundle.putBoolean(ToDoWidgetService.EXTRA_TODO_WIDGET_VOICE_INPUT, intent.getBooleanExtra(ToDoWidgetService.EXTRA_TODO_WIDGET_VOICE_INPUT, false));
        			bundle.putLong(ToDoWidgetService.EXTRA_TODO_BOOK_ID, mToDoBookList.get(position).getCreatedTime());
                	startService(getResultIntent(ToDoWidgetService.ACTION_TODO_WIDGET_ADD_TO_RESULT, bundle));
        			break;
        		case DIALOG_TYPE_SELECT_BOOK:
        			if(position != 0){
                    	bundle.putLong(ToDoWidgetService.EXTRA_TODO_BOOK_ID, mToDoBookList.get(position-1).getCreatedTime());
        			}
                	startService(getResultIntent(ToDoWidgetService.ACTION_TODO_WIDGET_SELECT_BOOK_RESULT, bundle));
        			break;
        		default: break;
        		}
            	finish();
            }
        });
        
        Button cancelButton = (Button)findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();				
			}
		});	
	}
	
	private Intent getResultIntent(String action, Bundle bundle){
		Intent resultIntent = new Intent(ToDoWidgetDialogActivity.this, ToDoWidgetService.class);
    	resultIntent.setAction(action);
    	resultIntent.setData(Uri.parse(bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) + ""));
    	resultIntent.putExtras(bundle);
		return resultIntent;
	}
}
