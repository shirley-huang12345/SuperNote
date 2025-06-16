//BEGIN: Show
package com.asus.supernote.doodle.drawtool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Editable;

import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.TextImgDrawInfo;

public class TextImgDrawTool extends DrawTool {

    public static final int TEXTIMG_TOOL = DrawTool.TEXTIMG_TOOL;
    private TextImgDrawInfo mDrawInfo = null;
    private final Editable mContent;
    private final Bitmap mBitmap;


    public TextImgDrawTool(Bitmap bitmap, Editable content) {
        super(TEXTIMG_TOOL);
        mContent = content;
        mBitmap = bitmap;
    }

	@Override
    protected void doDraw(Canvas canvas, DrawInfo drawInfo) {
    	TextImgDrawInfo textImgDrawInfo = (TextImgDrawInfo) drawInfo;
        canvas.drawBitmap(textImgDrawInfo.getGraphic(), textImgDrawInfo.getTransformMatrix(), textImgDrawInfo.getPaint());
    }

    @Override
    public DrawInfo getDrawInfo(Paint usedPaint) {
        if (mDrawInfo == null) {
            mDrawInfo = new TextImgDrawInfo(mBitmap, mContent, this, usedPaint);
        }
        return mDrawInfo;
    }
}
//END: Show
