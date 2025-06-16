package com.asus.supernote.doodle.drawtool;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo;

public class AirBrushDrawTool extends PathDrawTool{
	public static final int NEON_TOOL = DrawTool.NEON_TOOL;
    private float mCurrentStrokeWidth = 0;
    private BlurMaskFilter mBlurMaskFilter = null;
    
    public AirBrushDrawTool() {
    	super(NEON_TOOL);
	}
    
    @Override
    protected void doDraw(Canvas canvas, DrawInfo drawInfo) {
        PathDrawInfo pathDrawInfo;
        if (drawInfo == null) {
            return;
        }

        pathDrawInfo = (PathDrawInfo) drawInfo;
        Paint paint = new Paint(pathDrawInfo.getPaint());
        float strokeWidth = paint.getStrokeWidth();
        
        if (strokeWidth != mCurrentStrokeWidth) {
            float blurRadius = strokeWidth * 0.2f;
            mBlurMaskFilter = new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL);
            mCurrentStrokeWidth = strokeWidth;
        }

        paint.setMaskFilter(mBlurMaskFilter);
        paint.setStrokeWidth(strokeWidth);
        
        canvas.drawPath(pathDrawInfo.getPath(), paint);
    }

}
