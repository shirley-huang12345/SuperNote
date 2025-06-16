package com.asus.supernote.doodle.drawtool;

import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemProperties;

import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.PenDrawInfo;
import com.asus.supernote.doodle.drawutil.HWPoint;

public class PenDrawTool extends BallPenDrawTool{
	
	Bitmap mTexture = null;
	Bitmap renderTexture = null;

	public PenDrawTool(int toolCode) {
		super(toolCode);
	}
	
	public void SetTexture(Bitmap texture){
		mTexture = texture;
	}
	
	@Override
	protected void doDraw(Canvas canvas, DrawInfo drawInfo) {
		PenDrawInfo penDrawInfo=(PenDrawInfo)drawInfo;
		Paint paint = new Paint(penDrawInfo.getPaint());
		paint.setStyle(Paint.Style.FILL);
		renderTexture = penDrawInfo.GetLocalTexture();
		
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
		PenDrawInfo penDrawInfo = new PenDrawInfo(this, usedPaint);
		penDrawInfo.SetTexture(mTexture);
		return penDrawInfo;
	}
	
	protected HWPoint curPoint;
	protected void drawToPoint(Canvas canvas, HWPoint point, Paint paint){
		// avoiding repaint
		if((curPoint.x == point.x) && (curPoint.y == point.y))
			return;
		drawLine(canvas, curPoint.x, curPoint.y, curPoint.width, point.x, point.y, point.width, paint);
	}

	//emmanual
	String product, model;
	protected void drawLine(Canvas canvas, double x0,double y0,double w0,double x1,double y1,double w1,Paint paint){
		double curDis = Math.hypot(x0-x1, y0-y1);
		int steps = 1;
		if(paint.getStrokeWidth() < 6){
			steps = 1+(int)(curDis/2);
		}else if(paint.getStrokeWidth() > 60){
			steps = 1+(int)(curDis/4);
		}else{
			steps = 1+(int)(curDis/3);
		}
		double deltaX=(x1-x0)/steps;
		double deltaY=(y1-y0)/steps;
		double deltaW=(w1-w0)/steps;
		double x=x0;
		double y=y0;
		double w=w0;
		
		//emmanual
		if (product == null || product.equals("")) {
			product = SystemProperties.get("ro.product.device");
		}
		if (model == null || model.equals("")) {
			model = SystemProperties.get("ro.product.model");
		}
		if (product.equals("K00C") && model.equals("K00C")) {
			curDis = Math.hypot(x0 - x1, y0 - y1);
		}
		
		for(int i=0;i<steps;i++){
			RectF oval = new RectF();
			oval.set((float)(x-w/4.0f), (float)(y-w/2.0f), (float)(x+w/4.0f), (float)(y+w/2.0f));
			canvas.drawOval(oval, paint);
			
			x+=deltaX;
			y+=deltaY;
			w+=deltaW;
		}
	}

	@Override
	public void drawPreview(Canvas canvas, DrawInfo drawInfo) {
        HWPoint point;
        Rect src = new Rect();
        RectF dst = new RectF();
		
        PenDrawInfo penDrawInfo=(PenDrawInfo)drawInfo;
		Paint paint = penDrawInfo.getPaint();
		paint.setStyle(Paint.Style.FILL);
		renderTexture = penDrawInfo.GetLocalTexture();
		src.set(0,0,renderTexture.getWidth(),renderTexture.getHeight());
		
		LinkedList<HWPoint> mHWPointList = penDrawInfo.getDrawList();
		
		if(mHWPointList==null)
			return;
		
			curPoint = mHWPointList.get(0);
			for(int i =1; i<mHWPointList.size(); i++){
				point = mHWPointList.get(i);
				drawToPoint(canvas, point, paint);
				curPoint = point;
			}
	}
}
