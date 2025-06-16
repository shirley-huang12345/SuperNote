package com.asus.supernote.classutils;


import com.asus.supernote.R;
import com.asus.supernote.SuperNoteApplication;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ColorfulStatusActionBarHelper {
	public static final int COLOR_STATUS_ACTION_BAR = 0xffe9ac1d;
	static public View attchColorView(LayoutInflater inflater,  View childView, boolean showActionbar, Activity activity){
		LinearLayout layout =(LinearLayout)inflater.inflate(R.layout.colorful_layout, null);
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(     
                LinearLayout.LayoutParams.FILL_PARENT,     
                LinearLayout.LayoutParams.FILL_PARENT     
        );     
		layout.addView(childView, p);
		updateColorView(activity, showActionbar, layout);
		return layout;
	}
	
	static public void setContentView(int id, boolean showActionbar, Activity activity){
		/* mars_li for colorful bar*/
		if(isColorfulTextViewNeeded()){
	        LayoutInflater inflater = (LayoutInflater) activity
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(id, null);
			View parentView = ColorfulStatusActionBarHelper.attchColorView(inflater, view, showActionbar, activity);
			
			activity.setContentView(parentView);
		}else{
			activity.setContentView(id);
		}
			
	}
	
	static public void updateColorView(Context context, boolean showActionbar, View root){
		TextView textViewColorful = (TextView) root.findViewById(R.id.textViewColorful);
        int statusH = getStatusBarHeight(context);
        int actionbarH = getActionBarHeight(context);
        if(showActionbar){
        	textViewColorful.setHeight(statusH + actionbarH);
        	textViewColorful.setBackgroundColor(COLOR_STATUS_ACTION_BAR);
        }	
        else{
        	textViewColorful.setHeight(statusH);
        	textViewColorful.setBackgroundColor(COLOR_STATUS_ACTION_BAR);
        }
        
	}
	
    private static boolean isColorfulTextViewNeeded() {
        // TODO: need to use constant L API level
        return android.os.Build.VERSION.SDK_INT < 21 ? true : false;
        // return android.os.Build.VERSION.SDK_INT <
        // android.os.Build.VERSION_CODES.L ? true : false;
    }
	
	static private int getStatusBarHeight(Context context) {
		final Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
	    if (display != null && display.getDisplayId() != Display.DEFAULT_DISPLAY) {
	        return 0;
	    }
        int h = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            h = context.getResources().getDimensionPixelSize(resourceId);
        }
        return h; 
    }
	
	static private int getActionBarHeight(Context context) {
        int h = 0;
        TypedValue tv = new TypedValue();
        SuperNoteApplication.getInstance().getBaseContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        h = context.getResources().getDimensionPixelSize(tv.resourceId);
        return h;
    }
	
}
