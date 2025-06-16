package com.asus.supernote.doodle.drawtool;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo;
import com.asus.supernote.doodle.drawinfo.NeonDrawInfo;
//added by noah_zhang@asus.com;13.3.5
public class NeonDrawTool extends PathDrawTool {

	public static final int Neon_TOOL = DrawTool.NEON_TOOL;
    private final Paint secondPaint;
    private float mCurrentStrokeWidth = 0;
    private BlurMaskFilter mBlurMaskFilter = null;

    public NeonDrawTool() {
        super(Neon_TOOL);
        secondPaint = new Paint();
        secondPaint.setAntiAlias(true);
        secondPaint.setDither(true);
        secondPaint.setColor(Color.WHITE);
        secondPaint.setStyle(Paint.Style.STROKE);
        secondPaint.setStrokeJoin(Paint.Join.ROUND);
        secondPaint.setStrokeCap(Paint.Cap.ROUND);
        secondPaint.setXfermode(null);
        secondPaint.setAlpha(0xFF);
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
        
        float blurRadius = strokeWidth * 0.17f * 1.5f;
        if (strokeWidth != mCurrentStrokeWidth) {
            
            mBlurMaskFilter = new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL);
            mCurrentStrokeWidth = strokeWidth;
        }
        
        
        BlurMaskFilter middlePaintFilter = new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL);
        secondPaint.setStrokeWidth(strokeWidth * 0.4f);
        
        NeonDrawInfo neonDrawInfo = (NeonDrawInfo)drawInfo;
        neonDrawInfo.resetStrokeWidth();
        Paint leftPaint = new Paint(neonDrawInfo.getLeftPaint());
        leftPaint.setMaskFilter(mBlurMaskFilter);
        Paint middlePaintBlurPaint = new Paint(neonDrawInfo.getSecontPaint());
        middlePaintBlurPaint.setMaskFilter(middlePaintFilter);
        Paint rightPaint = new Paint(neonDrawInfo.getRightPaint());
        rightPaint.setMaskFilter(mBlurMaskFilter);
        canvas.drawPath(neonDrawInfo.getPath(), middlePaintBlurPaint);
        canvas.drawPath(neonDrawInfo.getLeftPath(), leftPaint);
        canvas.drawPath(neonDrawInfo.getRightPath(), rightPaint);

        //canvas.drawPath(neonDrawInfo.getPath(), secondPaint);//空心
    }
    
    public DrawInfo getDrawInfo(Paint usedPaint) {
        return new NeonDrawInfo(this, usedPaint);
    }
}
