package com.asus.supernote.widget;

import java.util.ArrayList;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.picker.PickerUtility;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;

public class WidgetShowAllBookActivity extends Activity{

	public WidgetItemStoreAdapter mItemAdapter=null; 
	
	public final static String EXTRA_CLICKITEM_ID = "clickItemId";
	public final static String EXTRA_ADD_REPLACE_FLAG = "addReplaceFlag";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub				
		Intent intent = getIntent();
		int clickItemId = intent.getIntExtra(EXTRA_CLICKITEM_ID, -1);
		int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		long bookId = intent.getLongExtra(WidgetService.WIDGET_BOOK_ID, -1);
		int addReplaceFlag = intent.getIntExtra(EXTRA_ADD_REPLACE_FLAG, -1);
		int dialogTitle = 0;
		if (bookId > 0  && addReplaceFlag == WidgetService.replace_from_book_item) 
		{
			dialogTitle = R.string.widget_dialog_replace_notebook;
		} else if (bookId == -1) 
		{
			if (addReplaceFlag == WidgetService.add_from_blank_item || 
					addReplaceFlag == WidgetService.ADDBOOK_SUBACTION_ONEWIDGET)
			{
				dialogTitle = R.string.widget_bt_add_a_notebook;//Carol widget_select_notebook);
			} else if (addReplaceFlag == WidgetService.replace_from_book_template_item) 
			{
				dialogTitle = R.string.widget_dialog_replace_notebook;
			} 
		} 
		setContentView(R.layout.widget_page_grid_view);
		setTitle(dialogTitle);
		GridView itemgridview = (GridView)findViewById(R.id.page_gridview);
		ArrayList<Long> arrItemModes = getOtherItemsMode(widgetId,clickItemId);//Siupo
		mItemAdapter  = new WidgetItemStoreAdapter(
				this,
				widgetId,
				clickItemId,
				bookId,
				addReplaceFlag,arrItemModes);//Siupo,add a parameter
		itemgridview.setAdapter(mItemAdapter);
		itemgridview.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
			WindowManager.LayoutParams params = this.getWindow().getAttributes();
			params.width = (int)getResources().getDimension(R.dimen.widget_replace_dialog_width);
			
			if(PickerUtility.getDeviceType(this)==MetaData.DEVICE_TYPE_A86){
				params.gravity = Gravity.TOP;
				String model = SystemProperties.get("ro.product.model");
		        if (model!=null)
		        	params.verticalMargin = model.equalsIgnoreCase("k00g")?0.02f:0.05f;
			}
	        this.getWindow().setAttributes(params);
		//}
		Button cancelButton = (Button)findViewById(R.id.widget_cancel_button);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		super.onCreate(savedInstanceState);
	}
	public ArrayList<Long> getOtherItemsMode(int appWidgetId,int itemID)
	{
		ArrayList<Long> itemModes = new ArrayList<Long>();
		String selection=MetaData.WidgetItemTable.ITEM_WIDGET_ID+"="+appWidgetId;
		Cursor cursor_widget_item = this.getContentResolver().query(MetaData.WidgetItemTable.uri,null,selection,null,null);	
		if (cursor_widget_item != null   ) 
		{			
			if (cursor_widget_item.getCount()>0 && cursor_widget_item.moveToFirst()) 
			{						
				do{
					int item_order_id = cursor_widget_item.getInt(MetaData.WidgetItemTable.INDEX_ITEM_ORDER_ID);
					int item_mode     = cursor_widget_item.getInt(MetaData.WidgetItemTable.INDEX_ITEM_MODE);
					if(item_order_id != itemID)
					{
						if(item_mode == SingleWidgetItem.BOOK_MODE)
						{
							itemModes.add(cursor_widget_item.getLong(MetaData.WidgetItemTable.INDEX_ITEM_BOOK_ID));
						}
						else if(item_mode == SingleWidgetItem.BOOK_TEMPLATE_MODE)
						{
							itemModes.add(1L);
						}
						else
						{
							itemModes.add(0L);//put it into array
						}
					}
				}while (cursor_widget_item.moveToNext());				
			}
			
			cursor_widget_item.close();	
		}
		return itemModes;
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mItemAdapter!=null)
		{
			mItemAdapter.releaseMemory();
		}
	}
}
