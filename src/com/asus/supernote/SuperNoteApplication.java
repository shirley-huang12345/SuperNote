/**
 * 
 */
package com.asus.supernote;

import com.asus.supernote.inksearch.CFG;
import com.asus.supernote.uservoice.UserVoiceConfig;

import android.app.Application;
import android.content.Context;

/**
 * @author mars_li@asus.com
 * Description: for get context 
 */
public class SuperNoteApplication extends Application {
	private static SuperNoteApplication sInstance;
	private static Context sContext;
	public EditorActivity singleEditorActivity = null;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		sInstance = this;
		sContext = this.getApplicationContext();
		initGlobalData();
	}
	//add by jason
	private void initGlobalData(){
		CFG.setPath(sContext.getDir("Data", 0).getAbsolutePath());
	}//end
	
	public static void setContext(Context context){
		if(sContext == null)
			sContext = context;
	}
	
	public static Context getContext(){
		return sContext;
	}
	
	public static SuperNoteApplication getInstance() {
		return sInstance;
	}
}
