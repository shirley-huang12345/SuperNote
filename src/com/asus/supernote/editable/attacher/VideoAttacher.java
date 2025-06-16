package com.asus.supernote.editable.attacher;

import java.io.File;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;

import com.asus.supernote.EditorUiUtility;
import com.asus.supernote.editable.PageEditorManager;
import com.asus.supernote.editable.noteitem.AttacherTool;
import com.asus.supernote.editable.noteitem.NoteImageItem;
import com.asus.supernote.editable.noteitem.NoteSendIntentItem;
import com.asus.supernote.picker.PickerUtility;

public class VideoAttacher{
    public static final String VIDEO_PREFIX_NAME = "SuperNoteVideo";
    public static final String VIDEO_FILE_EXTENSION = ".mp4";
    public static final int VIDEO_FILE_LENGTH = 20000;
    public static final int VIDEO_FILE_QUALITY = 1;

    private static final String OBJ = String.valueOf((char) 65532);
 
    public static Intent getIntent(PageEditorManager pageEditorManager) {
        String fileName = VIDEO_PREFIX_NAME + "_" + System.currentTimeMillis() + VIDEO_FILE_EXTENSION;
        Uri uri = Uri.fromFile(new File(pageEditorManager.getCurrentPageEditor().getFilePath(), fileName));
        try {
			Intent intent = new Intent();
			intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, VIDEO_FILE_LENGTH);
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, VIDEO_FILE_QUALITY);
			return intent;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
    public static void attachItem(Intent data, PageEditorManager pageEditorManager) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(data.getData(), "video/mp4");
        
		String path = PickerUtility.getRealFilePath(
				EditorUiUtility.getContextStatic(), data.getDataString());
		int lastSlashPos = path.lastIndexOf("/");
		String fileName = null;
		if (lastSlashPos >= 0) {
			fileName = path.substring(lastSlashPos + 1, path.length());
		}

		//Begin Dave: To modify voice/video attacher UI.  
		String fullPath = pageEditorManager.getCurrentPageEditor().getFilePath() + "//" + fileName;
		//End Dave.
        
        NoteSendIntentItem item = new NoteSendIntentItem(intent);
        //Begin Dave: To modify voice/video attacher UI.  
        AttacherTool tool = new AttacherTool();
		String imageItemInfo = tool.getFileNameNoEx(fileName) + tool.getElapsedTime(fullPath);
        NoteImageItem icon = new NoteImageItem(true, pageEditorManager.getCurrentPageEditor().getImageSpanHeight(),imageItemInfo);
        //End Dave.
        // BEGIN ryan_lin@asus.com, add for scribble space
        SpannableString spannableString = new SpannableString(OBJ);
        // END ryan_lin@asus.com
        spannableString.setSpan(item, 0, OBJ.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(icon, 0, OBJ.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //Begin Darwin_Yu@asus.com
        fileName = item.getFileName();
        pageEditorManager.getCurrentPageEditor().addItemToEditText(spannableString,fileName);
        //End   Darwin_Yu@asus.com
    }
}
