package com.asus.supernote.doodle.drawinfo;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawtool.PencilDrawTool;

public class PencilDrawInfo extends PathDrawInfo {
	private Paint mTexturePaint;

	public PencilDrawInfo(DrawTool drawTool, Paint usedPaint){
		super(drawTool, usedPaint);
		mTexturePaint = new Paint(mPaint);
	}
	
	public PencilDrawInfo(DrawTool drawTool, Paint usedPaint, short[] points) {
		super(drawTool,usedPaint,points);
		mTexturePaint = new Paint(mPaint);
	}
	
	public PencilDrawInfo(DrawTool drawTool, Paint usedPaint, float[] points) {
		super(drawTool,usedPaint,points);
		mTexturePaint = new Paint(mPaint);
	}
	
	public void SetTexture(Bitmap texture){
		//emmanual
		if(texture == null){
			return;
		}
		
		Canvas canvas = new Canvas();
		Bitmap result = Bitmap.createBitmap(texture.getWidth(), texture.getHeight(), Bitmap.Config.ARGB_8888);
		result.eraseColor(Color.rgb(Color.red(mPaint.getColor()), Color.green(mPaint.getColor()), Color.blue(mPaint.getColor())));
		canvas.setBitmap(result);
		
		Paint paint = new Paint();		
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawBitmap(texture, 0, 0, paint);
		
		BitmapShader fillBMPshader = new BitmapShader(result, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		mTexturePaint.setStyle(Paint.Style.STROKE);
		mTexturePaint.setStrokeCap(Paint.Cap.ROUND);
		mTexturePaint.setStrokeJoin(Paint.Join.ROUND);
		mTexturePaint.setMaskFilter(null);
		mTexturePaint.setXfermode(null);
		mTexturePaint.setPathEffect(null);
        
		mTexturePaint.setAlpha(0xFF);
		mTexturePaint.setShader(fillBMPshader);
	}
	
	public Paint GetTexturePaint(){
		return mTexturePaint;
	}
	
	@Override
    public DrawInfo cloneLock() {
		PencilDrawInfo drawInfo = (PencilDrawInfo) new PencilDrawTool().getDrawInfo(new Paint(mPaint));
        drawInfo.mPath = new Path(mPath);
        for (Point point : mPointList) {
            drawInfo.mPointList.add(new Point(point));
        }
        drawInfo.mTexturePaint = new Paint(mTexturePaint);
        return drawInfo;
	}
	
}
