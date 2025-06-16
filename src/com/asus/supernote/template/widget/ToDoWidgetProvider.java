package com.asus.supernote.template.widget;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class ToDoWidgetProvider extends AppWidgetProvider {

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		/* delete widget info from database */
		for(int id: appWidgetIds){
			context.getContentResolver().delete(MetaData.ToDoWidgetTable.uri, MetaData.ToDoWidgetTable.WIDGET_ID+" = " + id, null);
			Log.i("Allen delete","WidgetId:"+ id);
		}
		
		ComponentName mComponentName = new ComponentName(context, ToDoWidgetProvider.class); 
		int[] widgetIds =  (AppWidgetManager.getInstance(context)).getAppWidgetIds(mComponentName);
		if(widgetIds.length == 0){
			Intent intent = new Intent();
			intent.setClass(context, ToDoWidgetService.class);
			intent.setData(Uri.parse(System.currentTimeMillis() + ""));
			context.stopService(intent);
		}
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		super.onDisabled(context);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
	}


	@Override
	public void onReceive(Context context, Intent intent) {
		/* handle update broadcast */
		if (intent.getAction().equals(MetaData.ACTION_SUPERNOTE_UPDATE)) {
			ComponentName mComponentName = new ComponentName(context, ToDoWidgetProvider.class);
			int[] appWidgetIds = (AppWidgetManager.getInstance(context)).getAppWidgetIds(mComponentName);
			for (int i = 0; i < appWidgetIds.length; i++) {
				Intent updateIntent = new Intent(context, ToDoWidgetService.class);
				updateIntent.setAction(intent.getAction());
				updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetIds[i]);
				updateIntent.putExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM,
								intent.getSerializableExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM));
				context.startService(updateIntent);
			}
		}
		super.onReceive(context, intent);
	}


	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		for (int id : appWidgetIds) {
			Intent intent = new Intent();
			intent.setClass(context, ToDoWidgetService.class);
			intent.setAction(ToDoWidgetService.ACTION_TODO_WIDGET_INIT);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
			context.startService(intent);
		}
	}

	@Override
	public void onAppWidgetOptionsChanged (Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions){
		int MAX_HEIGHT = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 123);
		int MAX_WIDTH = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 123);
		int width = 0;
		int height = 0;
		if(MAX_HEIGHT>600){
			height = 6;
		}else if(MAX_HEIGHT>500){
			height = 5;
		}else if(MAX_HEIGHT>400){
			height = 4;
		}
		//Carol
		int deviceType = context.getResources().getInteger(R.integer.device_type);
		int startDP = context.getResources().getInteger(R.integer.start_width);
		int incrementalDP = context.getResources().getInteger(R.integer.increment_width);
		if(deviceType>100){
			width = (MAX_WIDTH < startDP)?2:(MAX_WIDTH - startDP + 30)/incrementalDP + 3;
		}else if(deviceType>=3){
			width = (MAX_WIDTH - startDP + 30)/incrementalDP + 3;
		}
		Intent intent = new Intent();
		intent.setClass(context, ToDoWidgetService.class);
		intent.setAction(ToDoWidgetService.ACTION_TODO_WIDGET_OPTIONS_CHANGED);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		intent.putExtra(ToDoWidgetService.EXTRA_TODO_WIDGET_HEIGHT, height);
		intent.putExtra(ToDoWidgetService.EXTRA_TODO_WIDGET_WIDTH, width);
		context.startService(intent);
	}
}
