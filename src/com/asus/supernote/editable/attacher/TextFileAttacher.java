package com.asus.supernote.editable.attacher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import com.asus.supernote.EditorActivity;
import com.asus.supernote.EditorUiUtility;
import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.editable.PageEditorManager;
import com.asus.supernote.picker.PickerUtility;
import com.asus.supernote.template.TemplateToDoUtility;

public class TextFileAttacher{

    public static final String TAG = "TextFileAttacher";
    public static final long MAX_FILE_SIZE = 10*1024L; //max file 10KB
    
  public static Intent getIntentTextFile() {
	  String[] textlist = { "text/plain", };
	  try {
			Intent intent = new Intent();
			intent.setClassName("com.asus.filemanager", "com.asus.filemanager.activity.FileManagerActivity");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.setType("TEXT/*");
			intent.putExtra("mime", textlist);
			return intent;
		} catch (ActivityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
  }
  
	public static void attachItem(Intent intent, PageEditorManager pageEditorManager) {
	
	    try {
	        // BEGIN: archie_huang@asus.com
	        // To avoid NullPointerException
	        Context context = EditorUiUtility.getContextStatic();
	        if (context == null) {
	            return;
	        }
	        // END: archie_huang@asus.com
	
	        String filepath = PickerUtility.getRealFilePathForText(EditorUiUtility.getContextStatic(), intent.getData()); //smilefish
	        if (!intent.getData().toString().contains(".txt")&&!filepath.contains(".txt")) {
	            Cursor cursor = context.getContentResolver().query(intent.getData(), null, null, null, null);
                //BEGIN: RICHARD
                if (cursor == null ){//|| cursor.getCount() == 0) {
                    return;
                }
                if(cursor.getCount() == 0)
                {
                	cursor.close();
                	return;
                }
                cursor.moveToFirst();
                filepath = cursor.getString(cursor.getColumnIndex("_data"));
                cursor.close();
                //END:RICHARD
	        }
	
	        StringBuilder fileTextBuilder = new StringBuilder();
	        File file = new File(filepath);
	        //[ASUS][James][Begin]Filter the big text file
	        if (file.length() > MAX_FILE_SIZE) {
	            EditorActivity.showToast(context, R.string.insert_text_too_large);
	            return;
	        }
	        //[ASUS][James][End]Filter the big text file
	        FileInputStream fis = new FileInputStream(file);
	        String encoding = PickerUtility.getTextFileCharset(file); 
	        InputStreamReader isr = new InputStreamReader(fis, (encoding != null) ? encoding : 
	        	((MetaData.INDEX_CURRENT_LANGUAGE == MetaData.INDEX_LANGUAGE_ZH_TW )? "Big5" : Charset.defaultCharset().displayName()));//darwin modify
	        BufferedReader buf = new BufferedReader(isr);
	        String str = null;
	        
	        while ((str = buf.readLine()) != null) {
	            fileTextBuilder.append(str + "\n");
	        }
	        buf.close();
	        if (fileTextBuilder.length() < 1) {
	            return;
	        }
	        String fileText = fileTextBuilder.substring(0, fileTextBuilder.length() - 1).toString();
	        int templateType = pageEditorManager.getCurrentPageEditor().getTemplateType();
	        if(templateType == MetaData.Template_type_todo){//todo超过标题大小，要提示
				TemplateToDoUtility toDoUtility = pageEditorManager.getCurrentPageEditor().getTemplateUtility().geTemplateToDoUtility();
				if(toDoUtility == null){
					return;
				}
				if(fileText.contains("\n") || toDoUtility.isOverTitleWidth(fileText)){//todo标题不支持换行
					EditorActivity.showToast(context,
							R.string.content_is_too_long);
					return;
				}
			}
	        pageEditorManager.getCurrentPageEditor().insertText(fileText);
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
