package com.asus.supernote.widget;

import com.asus.supernote.data.MetaData;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WidgetOneProvider extends AppWidgetProvider
{
	@Override
	public void onEnabled(Context context)
	{
		Log.i("WidgetOneProvider", "onEnable()");
		
	}
	@Override
	public void onUpdate(Context context,AppWidgetManager appWidgetManager,
			int[] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) 
		{
			 Intent intent1 = new Intent(context, WidgetService.class);
			 intent1.setAction(WidgetService.UPDATE_ACTION);
			 intent1.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);	
			 intent1.putExtra(WidgetService.SUBACTION, WidgetService.UPDATE_ACTION_ADD_WIDGET);
	         context.startService(intent1);
		}		
	}
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onDeleted(context, appWidgetIds);
		final int N = appWidgetIds.length;
	}
	@Override
	public void onReceive (Context context, Intent intent)
	{
		//Begin Allen handle update broadcast
		if(intent.getAction().equals(MetaData.ACTION_SUPERNOTE_UPDATE)){
			ComponentName mComponentName = new ComponentName(context, WidgetOneProvider.class); 
			int[] appWidgetIds =  (AppWidgetManager.getInstance(context)).getAppWidgetIds(mComponentName);
			for(int i=0;i<appWidgetIds.length;i++){
				Intent updateIntent = new Intent(context,WidgetService.class);
				updateIntent.setAction(intent.getAction());
				updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
				updateIntent.putExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM, intent.getSerializableExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM));
				context.startService(updateIntent);
			}
		}
		//End Allen
		super.onReceive(context, intent);
	}
}
