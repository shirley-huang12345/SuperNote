package com.asus.supernote.editable.attacher;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.EditorUiUtility;
import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.editable.PageEditorManager;
import com.asus.supernote.picker.NoteBookPickerActivity;
import com.asus.supernote.picker.PickerUtility;

public class CameraAttacher extends BitmapAttacher {
    public static final String CAMERA_PREFIX_NAME = "SuperNotePic";
    public static final String CAMERA_FILE_EXTENSION = ".jpg";
    public static final String PREFERENCE_DIR = "CameraAttacher_dir";
    public static final String PREFERENCE_FILENAME = "CameraAttacher_filename";
    public static final String PAGE_WIDTH = "CameraAttacher_pagewidth";
    public static final String PAGE_HEIGHT = "CameraAttacher_pageheight";
    private PageEditorManager mPageEditorManager;
    private static Uri mUri;
    private static String dir;
    private static String fileName;
    private boolean misFromChangeCover = false;
    
    public CameraAttacher() {
    	misFromChangeCover = true;
    	mPageEditorManager = null;
    }

    //darwin
    public NoteBookPickerActivity mNBPA = null;
    public void setActivity(NoteBookPickerActivity picker) {
    	mNBPA = picker;
    }
    //darwin
    @Override
    public Intent getIntent() {
        dir = (mPageEditorManager != null && (misFromChangeCover == false)) ? mPageEditorManager.getCurrentPageEditor().getFilePath() : MetaData.DATA_DIR;//darwin
        fileName = CAMERA_PREFIX_NAME + "_" + System.currentTimeMillis() + "_" + CAMERA_FILE_EXTENSION;
        mUri = Uri.fromFile(new File(dir, fileName));
        try {
        	Intent intent;
			intent = new Intent();
			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
			 return intent;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
       
    }

    //darwin
    public String getPath()
    {
    	return dir + fileName;
    }
    //darwin
    @Override
    public void attachItem(Intent intent) {

    	//add by mars_li fixed bug 290943
        try {
        	//smilefish fix bug 349687
        	String filePath = PickerUtility.getRealFilePathForImage(EditorUiUtility.getContextStatic(), intent.getData());
            if (filePath == null) {
            	filePath = (String)intent.getStringExtra("path");
            	if(filePath == null)
            	{
            		EditorActivity.showToast(EditorUiUtility.getContextStatic(), R.string.prompt_err_open);
                return;
            	}
            }
            
        	File srcFile = new File(Uri.decode(filePath));
        	//end smilefish
            File destFile = new File(dir, fileName);
            
            destFile.createNewFile();
            
            Bitmap b = saveBitmap(srcFile, destFile, Bitmap.CompressFormat.JPEG);
            if(mPageEditorManager != null && (misFromChangeCover == false))
            {
            	mPageEditorManager.getCurrentPageEditor().addBmpToDoodleView(b, destFile.getName());
            }
            //darwin
            if(mNBPA != null && (misFromChangeCover == true))
            {
            	mNBPA.returnBitmapFromCameraAndGallery(b,dir,fileName);
            }          
        }
        catch (IOException e) {
            e.printStackTrace();
        }

       
        //darwin
    }
    
    private static void savePath(PageEditorManager pageEditorManager){
		SharedPreferences.Editor mEditor = pageEditorManager
		        .getCurrentPageEditor()
		        .getEditorUiUtility()
		        .getContext()
		        .getSharedPreferences(MetaData.PREFERENCE_NAME,
		                Context.MODE_MULTI_PROCESS).edit();
		mEditor.putString(PREFERENCE_DIR, dir);
		mEditor.putString(PREFERENCE_FILENAME, fileName);
		int width = pageEditorManager.getCurrentPageEditor().getScreenWidth(); 
        int height = pageEditorManager.getCurrentPageEditor().getEditablePageHeight();
		if (width > 0) {
			mEditor.putInt(PAGE_WIDTH, width);
		}
		if (height > 0) {
			mEditor.putInt(PAGE_HEIGHT, height);
		}
		mEditor.commit();
    }
    
    public static Intent getIntent(PageEditorManager pageEditorManager) {
        dir = pageEditorManager.getCurrentPageEditor().getFilePath();
        fileName = CAMERA_PREFIX_NAME + "_" + System.currentTimeMillis() + "_" + CAMERA_FILE_EXTENSION;
        savePath(pageEditorManager);
		
        mUri = Uri.fromFile(new File(dir, fileName));
        try {
        	Intent intent;
			intent = new Intent();
			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
			 return intent;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
       
    }
    
    public static void attachItem(PageEditorManager pageEditorManager) {
    	
        //add by mars_li fixed bug 290943
		try {
			int width = pageEditorManager.getCurrentPageEditor()
			        .getScreenWidth();
			int height = pageEditorManager.getCurrentPageEditor()
			        .getEditablePageHeight();
			boolean restart = false;
			if (dir == null && fileName == null) {
				restart = true;
				SharedPreferences mPreference = pageEditorManager
				        .getCurrentPageEditor()
				        .getEditorUiUtility()
				        .getContext()
				        .getSharedPreferences(MetaData.PREFERENCE_NAME,
				                Context.MODE_MULTI_PROCESS);
				dir = mPreference.getString(PREFERENCE_DIR, "");
				fileName = mPreference.getString(PREFERENCE_FILENAME, "");
				if (width == 0) {
					width = mPreference.getInt(PAGE_WIDTH, 0);
				}
				if (height == 0) {
					height = mPreference.getInt(PAGE_HEIGHT, 0);
				}
        	}
        	File srcFile = new File(dir, fileName);
            File destFile = new File(dir, fileName);
	        destFile.createNewFile();
            
		    //emmanual to save photo into gallery
			insertImage(pageEditorManager.getCurrentPageEditor()
			        .getEditorUiUtility().getContext().getContentResolver(),
			        srcFile, fileName, null);
            
			Bitmap b = saveBitmap(srcFile, destFile,
			        Bitmap.CompressFormat.JPEG, width, height);
			if(restart){
				pageEditorManager.getCurrentPageEditor().addBmpToDoodleView(b, destFile.getName(), Float.MIN_NORMAL);
			} else {
				pageEditorManager.getCurrentPageEditor().addBmpToDoodleView(b, destFile.getName());
			}
        }
        catch (Exception e) {
            e.printStackTrace();
        }        
    }

	/**
	 * emmanual to save photo into gallery
	 */
    public static final void insertImage(ContentResolver cr, 
			File srcFile, 
			String title, 
			String description) {		
		ContentValues values = new ContentValues();
		values.put(Images.Media.TITLE, title);
		values.put(Images.Media.DISPLAY_NAME, title);
		values.put(Images.Media.DESCRIPTION, description);
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		// Add the date meta data to ensure the image is added at the front of the gallery
		values.put(Images.Media.DATE_ADDED, System.currentTimeMillis());
		values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
 
        Uri url = null;
		BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = 1;
        option.inJustDecodeBounds = false;
		Bitmap origin = BitmapFactory.decodeFile(srcFile.getPath(), option);

		Bitmap temp = null;
        Matrix matrix = new Matrix();
        float rotation = rotationForImage(srcFile.getPath());
        if (rotation != 0f) {
             matrix.preRotate(rotation);
             temp = Bitmap.createBitmap(
                     origin, 0, 0, origin.getWidth(), origin.getHeight(), matrix, true);                 
             origin.recycle();
             origin = null;
        }else{
        	temp = origin;
        }
 
        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
 
            if (temp != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                	temp.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }
            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }
	}
}
