package com.asus.supernote.editable.attacher;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.EditorUiUtility;
import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.PageEditorManager;
import com.asus.supernote.picker.NoteBookPickerActivity;
import com.asus.supernote.picker.PickerUtility;

public class GalleryAttacher extends BitmapAttacher {
    private PageEditorManager mPageEditorManager;
    public static final String TAG = "GalleryAttacher";
    public static final String GALLERY_PREFIX_NAME = "SuperNoteGallery";
    public static final String GALLERY_FILE_EXTENSION = "jpg";
    public static final String[] FILE_FILTER = new String[] { "image/jpeg", "image/png", "image/gif", "image/bmp", "image/x-ms-bmp", "image/vnd.wap.wbmp" };
    private boolean misFromChangeCover = false;

    public GalleryAttacher() {
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
    	try {
			Intent intent = new Intent();
			
			// intent.setClassName("com.asus.filemanager", "com.asus.filemanager.activity.FileManagerActivity");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			intent.putExtra("path", Environment.getExternalStorageDirectory().toString());
			intent.putExtra("mime", FILE_FILTER);
			return intent;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	
    }

    @Override
    public void attachItem(Intent intent) {
    	String filePath = PickerUtility.getRealFilePathForImage(EditorUiUtility.getContextStatic(), intent.getData());
        if (filePath == null) {
        	filePath = (String)intent.getStringExtra("path");
        	if(filePath == null)
        	{
            EditorActivity.showToast(EditorUiUtility.getContextStatic(), R.string.prompt_err_open);
            return;
        	}
        }
        File imageFile = new File(Uri.decode(filePath));
        
        String dir = (mPageEditorManager != null && (misFromChangeCover == false)) ? mPageEditorManager.getCurrentPageEditor().getFilePath() : MetaData.DATA_DIR;//darwin
        String extension = GALLERY_FILE_EXTENSION;
        try {
            extension = MimeTypeMap.getFileExtensionFromUrl(imageFile.toURI().toURL().toString());
        }
        catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
        String fileName = GALLERY_PREFIX_NAME + System.currentTimeMillis() + "." + extension;
        File destFile = new File(dir, fileName);
        if (destFile.exists() == false) {
            try {
                destFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        if ("png".equals(extension)) {
            format = Bitmap.CompressFormat.PNG;
        }
        Bitmap b = saveBitmap(imageFile, destFile, format);
        if (b == null) {
            EditorActivity.showToast(EditorUiUtility.getContextStatic(), R.string.wrong_file_type);
            return;
        }
        if(mPageEditorManager != null && (misFromChangeCover == false))
        {
        	mPageEditorManager.getCurrentPageEditor().addBmpToDoodleView(b, destFile.getName());
        }
        //darwin
        if(mNBPA != null && (misFromChangeCover == true))
        {
        	mNBPA.returnBitmapFromCameraAndGallery(b,dir,fileName);
        }
        //darwin
    }
    
    public static Intent getIntentGallery() {
		try {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			intent.putExtra("path", Environment.getExternalStorageDirectory().toString());
			intent.putExtra("mime", FILE_FILTER);
			return intent;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	  
	public static void attachItem(Intent intent,
			PageEditorManager pageEditorManager) {
		String uri = null;
		if (intent != null && intent.getData() != null) {//emmanual to fix bug 421593
		    uri = intent.getData().toString();
		}
		String filePath = PickerUtility.getRealFilePathForImage(EditorUiUtility.getContextStatic(), intent.getData());
		if(filePath == null 
				//emmanual to fix bug 463320
				|| (uri!= null && uri.startsWith("content://media/external/images")
				//emmanual to fix bug 453162
					&& !new File(filePath).exists())){
			EditorActivity.showToast(EditorUiUtility.getContextStatic(),
					R.string.prompt_err_open);
			return;
		}
		attachItem(filePath, pageEditorManager);// by Jason
	}
	
	/** 
	 * 插入图片。图片是一个Bitmap对象，例如chrome share过来的时候，会传入一个截屏的Bitmap
	 * @author noah_zhang
	 * @param bitmap
	 * @param pageEditorManager
	 * @param dy
	 */
	public static void attachItem(Bitmap bitmap, PageEditorManager pageEditorManager, float dy){
		String filePath = PickerUtility.getRealFilePathForImage(EditorUiUtility.getContextStatic(), bitmap);
		if(filePath == null){
			return;
		}
		attachItem(filePath, pageEditorManager, dy);
	}
	
	/**
	 * @author noah_zhang
	 * @param imgFileName
	 * @param pageEditorManager
	 */
	public static void attachItem(String imgFileName, PageEditorManager pageEditorManager){
		attachItem(imgFileName, pageEditorManager, Float.MIN_VALUE);
	}
	
    //begin jason
    public static void attachItem(String imgFileName, PageEditorManager pageEditorManager, float dy){
    	 File imageFile = new File(Uri.decode(imgFileName));
    	    
    	    String dir = pageEditorManager.getCurrentPageEditor().getFilePath();
    	    String extension = GALLERY_FILE_EXTENSION;
    	    try {
    	    	String url = imageFile.toURI().toURL().toString();   
	            url = URLEncoder.encode(url, "UTF-8");  //smilefish fix bug 575662/573483
    	        extension = MimeTypeMap.getFileExtensionFromUrl(url);
    	    }
		    catch (UnsupportedEncodingException e) {
		        e.printStackTrace();
		    }
    	    catch (MalformedURLException e1) {
    	        e1.printStackTrace();
    	    }
    	    String fileName = GALLERY_PREFIX_NAME + System.currentTimeMillis() + "." + extension;
    	    File destFile = new File(dir, fileName);
    	    if (destFile.exists() == false) {
    	        try {
    	            destFile.createNewFile();
    	        }
    	        catch (IOException e) {
    	            e.printStackTrace();
    	        }
    	    }
    	    Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
    	    if ("png".equalsIgnoreCase(extension)) {
    	        format = Bitmap.CompressFormat.PNG;
    	    }
    	    Bitmap b;
    	    
    	    //emmanual to fix bug 415028,424283 for device >= 600dp
			if (pageEditorManager.getCurrentPageEditor().getDeviceType() > 2
			        && pageEditorManager.getInsertState() == 1){
    	    	b = saveStampShape(imageFile, destFile, format);
    	    }else if(pageEditorManager.getCurrentPageEditor().getScreenWidth() == 0
			        || pageEditorManager.getCurrentPageEditor().getEditablePageHeight() == 0){
        		b = saveBitmap(imageFile, destFile, format);
    	    }else{
        		b = saveBitmap(imageFile, destFile, format, 
    	    		pageEditorManager.getCurrentPageEditor().getScreenWidth(),
            		pageEditorManager.getCurrentPageEditor().getEditablePageHeight());
    	    }
    	    pageEditorManager.setInsertState(0);
    	    if (b == null) {
    	        EditorActivity.showToast(EditorUiUtility.getContextStatic(), R.string.wrong_file_type);
    	        return;
    	    }
    	    if(imageFile.getPath().startsWith(MetaData.CROP_TEMP_DIR) && imageFile.exists()){ //smilefish fix bug 731671
    	    	imageFile.delete();
    	    }
    	    pageEditorManager.getCurrentPageEditor().addBmpToDoodleView(b, destFile.getName(), dy);
    }
    /**
     * for shape graphic
     */
    public static void attachItem(int type , PageEditorManager pageEditorManager){
    	StringBuilder nameBuilder=new StringBuilder();
    	nameBuilder.append(DoodleItem.SerGraphicInfo.SHAPE_HEAD+
    			type+
    			DoodleItem.SerGraphicInfo.SHAPE_LINE+
    			System.currentTimeMillis()+
    			DoodleItem.SerGraphicInfo.SHAPE_END);
    	PageEditor pEditor= pageEditorManager.getCurrentPageEditor();
    	pEditor.addShapeGraphicToDoodleView(type, pEditor.getShapeManager().GetShapeById(type).mPath, nameBuilder.toString());
    	
    }
    
    /***
     * for merge the instant page to current page
     */
    public static void attachDoodleItem(String path,PageEditorManager pageEditorManager){
    	pageEditorManager.getCurrentPageEditor().addDoodleItemToDoodleView(NotePage.loadDoodleItem(path),path);
    }
    //end jason
}
