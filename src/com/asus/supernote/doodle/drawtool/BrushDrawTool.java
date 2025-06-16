package com.asus.supernote.doodle.drawtool;

import java.util.LinkedList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.asus.supernote.doodle.drawinfo.BrushDrawInfo;
import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.PenDrawInfo;
import com.asus.supernote.doodle.drawutil.HWPoint;

public class BrushDrawTool extends PenDrawTool{
	
	Rect src = new Rect();
	RectF dst = new RectF();

	public BrushDrawTool(int toolCode) {
		super(toolCode);
	}
	
	@Override
	protected void doDraw(Canvas canvas, DrawInfo drawInfo) {
		PenDrawInfo penDrawInfo=(PenDrawInfo)drawInfo;
		Paint paint = penDrawInfo.getPaint();
		paint.setStyle(Paint.Style.FILL);
		//paint.setAlpha(100);

		renderTexture = penDrawInfo.GetLocalTexture();
		src.set(0,0,renderTexture.getWidth(),renderTexture.getHeight());
		
		LinkedList<HWPoint> mOTPointList = penDrawInfo.getOnTimeDrawList();
		if(mOTPointList.size()>1){
			curPoint = mOTPointList.get(0);
			for(int i =1; i<mOTPointList.size(); i++){				
				HWPoint point = mOTPointList.get(i);
				drawToPoint(canvas, point, paint);
				curPoint = point;
			}
			penDrawInfo.clearOnTimeDrawList();
			return;
		}
		
		LinkedList<HWPoint> mHWPointList = penDrawInfo.getDrawList();
		
		if(mHWPointList==null ||  mHWPointList.size()<1 )
			return;
		
		if(mHWPointList.size()<2){
			HWPoint point = mHWPointList.get(0);
			canvas.drawCircle(point.x, point.y, point.width, paint);
		}else if(mHWPointList.size()==2){
			Paint iPaint = new Paint(paint);
			iPaint.setAlpha(255);
			HWPoint point = mHWPointList.get(0);
			dst.set((float)(point.x-point.width/2.0f), (float)(point.y-point.width/2.0f), 
					(float)(point.x+point.width/2.0f), (float)(point.y+point.width/2.0f));			
			canvas.drawBitmap(renderTexture, src, dst, iPaint);
		}else{
			curPoint = mHWPointList.get(0);
			for(int i =1; i<mHWPointList.size(); i++){
				HWPoint point = mHWPointList.get(i);
				drawToPoint(canvas, point, paint);
				curPoint = point;
			}
		}
	}
	
	@Override
	public DrawInfo getDrawInfo(Paint usedPaint) {
		BrushDrawInfo brushDrawInfo = new BrushDrawInfo(this, usedPaint);
		brushDrawInfo.SetTexture(mTexture);
		return brushDrawInfo;
	}
	
	protected HWPoint curPoint;
	protected void drawToPoint(Canvas canvas, HWPoint point, Paint paint){
		// avoiding repaint
		if((curPoint.x == point.x) && (curPoint.y == point.y))
			return;
		if(BrushDrawInfo.ISBRUSHWITHALPHA){
			drawLine(canvas, curPoint.x, curPoint.y, curPoint.width, curPoint.alpha, 
					point.x, point.y, point.width, point.alpha, paint);
		}else{
			drawLine(canvas, curPoint.x, curPoint.y, curPoint.width, point.x, point.y, point.width, paint);
		}
	}
	
	protected void drawLine(Canvas canvas, double x0,double y0,double w0,double x1,double y1,double w1,Paint paint){
		double curDis = Math.hypot(x0-x1, y0-y1);
		int factor = 2;
		if(paint.getStrokeWidth() < 6){
			factor = 1;
		}else if(paint.getStrokeWidth() > 60){
			factor = 3;
		}
		int steps = 1+(int)(curDis / factor);
		double deltaX=(x1-x0)/steps;
		double deltaY=(y1-y0)/steps;
		double deltaW=(w1-w0)/steps;
		double x=x0;
		double y=y0;
		double w=w0;
		
		for(int i=0;i<steps;i++){
			if(w<1.5)
				w=1.5;
			dst.set((float)(x-w/2.0f), (float)(y-w/2.0f), (float)(x+w/2.0f), (float)(y+w/2.0f));			
			canvas.drawBitmap(renderTexture, src, dst, paint);
			x+=deltaX;
			y+=deltaY;
			w+=deltaW;
		}
	}
	
	protected void drawLine(Canvas canvas, double x0,double y0,double w0, int a0, 
			double x1,double y1,double w1, int a1, Paint paint){
		double curDis = Math.hypot(x0-x1, y0-y1);
		int factor = 2;
		if(paint.getStrokeWidth() < 6){
			factor = 1;
		}else if(paint.getStrokeWidth() > 60){
			factor = 3;
		}
		int steps = 1+(int)(curDis / factor);
		double deltaX=(x1-x0)/steps;
		double deltaY=(y1-y0)/steps;
		double deltaW=(w1-w0)/steps;
		double deltaA=(a1-a0)/steps;
		double x=x0;
		double y=y0;
		double w=w0;
		double a=a0;

		for(int i=0;i<steps;i++){
			if(w<1.5)
				w=1.5;
			dst.set((float)(x-w/2.0f), (float)(y-w/2.0f), (float)(x+w/2.0f), (float)(y+w/2.0f));
			paint.setAlpha((int)(a/2.0f));
			canvas.drawBitmap(renderTexture, src, dst, paint);
			x+=deltaX;
			y+=deltaY;
			w+=deltaW;
			a+=deltaA;
		}
	}
	
	@Override
	public void drawPreview(Canvas canvas, DrawInfo drawInfo) {
        HWPoint point;
		
		BrushDrawInfo brushDrawInfo=(BrushDrawInfo)drawInfo;
		Paint paint = brushDrawInfo.getPaint();
		paint.setStyle(Paint.Style.FILL);

		renderTexture = brushDrawInfo.GetLocalTexture();
		src.set(0,0,renderTexture.getWidth(),renderTexture.getHeight());
		
		LinkedList<HWPoint> mHWPointList = brushDrawInfo.getDrawList();
		
		if(mHWPointList==null)
			return;
		
			curPoint = mHWPointList.get(0);
			for(int i =1; i<mHWPointList.size(); i++){
				point = mHWPointList.get(i);
				point.alpha = 255;
				drawToPoint(canvas, point, paint);
				curPoint = point;
			}
	}

}

