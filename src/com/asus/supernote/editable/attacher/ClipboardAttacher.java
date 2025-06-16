package com.asus.supernote.editable.attacher;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.EditorUiUtility;
import com.asus.supernote.R;
import com.asus.supernote.editable.PageEditorManager;
import com.asus.supernote.picker.PickerUtility;

public class ClipboardAttacher extends BitmapAttacher {
    private PageEditorManager mPageEditorManager;
    public static final String TAG = "ClipboardAttacher";
    public static final String CLIPBOARD_PREFIX_NAME = "SuperNoteClipboard";
    public static final String CLIPBOARD_FILE_EXTENSION = "jpg";

    public ClipboardAttacher(PageEditorManager pageEditorManager) {
    	mPageEditorManager = pageEditorManager;
    }

    @Override
    public Intent getIntent() {
        return null;
    }

    @Override
    public void attachItem(Intent intent) {
        String filePath = PickerUtility.getRealFilePath(EditorUiUtility.getContextStatic(), intent.getDataString());
        if (filePath == null) {
            EditorActivity.showToast(EditorUiUtility.getContextStatic(), R.string.prompt_err_open);
            return;
        }
        File imageFile = new File(Uri.decode(filePath));
        
        String dir = mPageEditorManager.getCurrentPageEditor().getFilePath();
        String extension = CLIPBOARD_FILE_EXTENSION;
        try {
            extension = MimeTypeMap.getFileExtensionFromUrl(imageFile.toURI().toURL().toString());
        }
        catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
        String fileName = CLIPBOARD_PREFIX_NAME + System.currentTimeMillis() + "." + extension;
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
        mPageEditorManager.getCurrentPageEditor().addBmpToDoodleView(b, destFile.getName());
    }
}
