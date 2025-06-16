package com.asus.supernote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.asus.supernote.data.MetaData;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

public class RestoreDBActivity extends Activity{
	public static final String PER_NAME ="RestoreDBActivity";
	public static final String PER_KEY = "RESTOREDB";
	private static final String KEY_SUPERNOTEDB_BACK = "key_supernotedb_back";
	
	@Override
	protected void onCreate(Bundle arg0) {
	    // TODO Auto-generated method stub
	    super.onCreate(arg0);
	    RestoreDB(getIntent());
	    android.os.Process.killProcess(android.os.Process.myPid());
	    finish();
	}

	private void RestoreDB(Intent intent) {
	    // TODO Auto-generated method stub
		if (intent==null) {
	        return;
        }
	    String path = intent.getStringExtra(KEY_SUPERNOTEDB_BACK);
	    if (path!=null&&!path.equalsIgnoreCase("")) {
	        if (!path.endsWith("/")) {
	            path = new String(path+"/");
            }
	        path = new String(path+MetaData.DATABASE_NAME);
	        File dbFile = getDatabasePath(MetaData.DATABASE_NAME);
	        if (dbFile.exists()) {
	            dbFile.delete();
            }
	        File srcFile = new File(path);
	        if (srcFile.exists()) {
	            try {
	                dbFile.createNewFile();
	                FileInputStream inputStream = new FileInputStream(srcFile);
	                FileOutputStream outputStream = new FileOutputStream(dbFile);
	                byte[]  buffer    = new byte[2048];
	                int bytesread = 0;
	                while ((bytesread=inputStream.read(buffer))!=-1) {
	                    outputStream.write(buffer, 0, bytesread);
                    }
	                inputStream.close();
	                outputStream.flush();
	                outputStream.close();
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	                return;
                }	            
	            SharedPreferences mPreference = getSharedPreferences(PER_NAME, Context.MODE_MULTI_PROCESS);
	            Editor editor = mPreference.edit();
	            editor.putBoolean(PER_KEY, true);
	            editor.commit();
            }
        }
    }
}
