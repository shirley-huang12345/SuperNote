package com.asus.supernote.editable.noteitem;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import com.asus.supernote.SuperNoteApplication;
import com.asus.supernote.data.MetaData;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

public class AttacherTool {
	private SharedPreferences mSharedPreference;
	private String EMPTY_TIME = "empty_time";
	
    //Begin Dave: To modify voice/video attacher UI.  
	public String getElapsedTime(String fullPath) {
		//emmanual to save time
		if(mSharedPreference == null){
			mSharedPreference = SuperNoteApplication.getContext()
			        .getSharedPreferences(MetaData.PREFERENCE_NAME,
			                Activity.MODE_PRIVATE);
		}
		if(!mSharedPreference.getString(fullPath, EMPTY_TIME).equals(EMPTY_TIME)){
			return mSharedPreference.getString(fullPath, EMPTY_TIME);
		}

		MediaPlayer mediaPlayer = new MediaPlayer();
		int durationTime = 0;

		try {
			File file = new File(fullPath);
			if (file.exists()) {
				file.setReadable(true, false);
				FileInputStream inputStream = new FileInputStream(file);
				FileDescriptor fd = inputStream.getFD();
				mediaPlayer.setDataSource(fd, 0, 0x7ffffffffffffffL);
				mediaPlayer.prepare();
			}
			durationTime = mediaPlayer.getDuration();

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mediaPlayer.release();
		mediaPlayer = null;
		long elapsedTime = durationTime;
		String format = String.format("%%0%dd", 2);
		elapsedTime = elapsedTime / 1000;
		String seconds = String.format(format, elapsedTime % 60);
		String minutes = String.format(format, (elapsedTime % 3600) / 60);
		String hours = String.format(format, elapsedTime / 3600);
		String time = hours + ":" + minutes + ":" + seconds;
		String result = "(" + time + ")";
		mSharedPreference.edit().putString(fullPath, result).commit();
		return result;
	}
	
	public void deleteElapsedTimeFromMap(String fullPath){
		if(mSharedPreference == null){
			mSharedPreference = SuperNoteApplication.getContext()
			        .getSharedPreferences(MetaData.PREFERENCE_NAME,
			                Activity.MODE_PRIVATE);
		}
		mSharedPreference.edit().remove(fullPath).commit();
	}
	
	public String getFileNameNoEx(String fileName)
	{
		if(fileName != null && fileName.length() > 0)
		{
			int start = fileName.lastIndexOf("SuperNoteVideo_");
			if(start != -1)
			{
				start = 15;
			}
			else
			{
				start = fileName.lastIndexOf("recording-");
				if(start != -1)
					start = 10;
				else 
					start = 0;
			}
			
			String trimString = fileName.substring(start,fileName.length());
			
			if(trimString.length() > 20)
			{
				trimString = trimString.substring(0, 17) + "...";
			}

			return trimString + " "; //add by mars_li 
			
		}

		return fileName;
	}
	
	//End Dave.

}
