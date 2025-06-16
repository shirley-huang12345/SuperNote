//BEGIN: Show
package com.asus.supernote.doodle.drawinfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.Editable;
import android.util.Log;

import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem.SerDrawInfo;
import com.asus.supernote.doodle.DoodleItem.SerTextImgInfo;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawtool.TextImgDrawTool;

public class TextImgDrawInfo extends TransformDrawInfo {
    private static final String TAG = "TextImgDrawInfo";
    private Editable mContent = null;
    private String mFileName = null;
    private Bitmap mBitmap;
    
    private int mWidth = 0;
    private int mHeight = 0;
    
    public TextImgDrawInfo(Bitmap bitmap,Editable content, DrawTool drawTool, Paint usedPaint) {
        super(drawTool, usedPaint);
        mContent = content;
        mBitmap = bitmap;
        // We need control points because we need to know the TextImg's bounds
        initControlPoints(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
    }

    @Override
    public DrawInfo cloneLock() {
        Bitmap bitmap = null;
        try {
            bitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, false);
        }
        catch(OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] cloneLock() failed !!!");
        }
        if (bitmap == null) {
            return null;
        }
        TextImgDrawInfo drawInfo = (TextImgDrawInfo) (new TextImgDrawTool(mBitmap, mContent).getDrawInfo(mPaint));
        drawInfo.mFileName = mFileName;
        drawInfo.mContent = mContent;
        drawInfo.mTransMatrix = new Matrix(mTransMatrix);
        drawInfo.mWidth = mWidth;
        drawInfo.mHeight = mHeight;
        clone(drawInfo);

        return drawInfo;
    }

    public Editable getContent() {
        return mContent;
    }
    public String getFileName() {
        return mFileName;
    }
    
    public int getWidth() {
    	return mWidth;
    }
    
    public void setWidth(int width) {
    	mWidth = width;
    }
    
    public int getHeight() {
    	return mHeight;
    }
    
    public void setHeight(int height) {
    	mHeight = height;
    }

    private String getFilePath(NotePage note) {
        if (mFileName == null) {
            String directory = note.getFilePath();
            mFileName = mBitmap.toString();
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(new File(directory, mFileName));
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mFileName;
    }
    
    public void setFileName(String name) {
        mFileName = name;
    }

    public Bitmap getGraphic() {
        return mBitmap;
    }

    public void releaseMemory() {
        if ((mBitmap != null) && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    @Override
    public SerDrawInfo saveLock(NotePage note) {
    	SerTextImgInfo serInfo = new SerTextImgInfo();
        String fileName = getFilePath(note);
        serInfo.setFileName(fileName);
        Editable content = getContent();
        serInfo.setContent(content);
        serInfo.setPaintTool(mDrawTool.getToolCode());
        serInfo.setTransform(mTransMatrix);
        serInfo.setWidth(mWidth);
        serInfo.setHeight(mHeight);
        return serInfo;
    }

    public void setContent(Editable content) {
        mContent = content;
    }
}
//END: Show
