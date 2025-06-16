package com.asus.supernote.doodle.drawtool;

import java.util.LinkedList;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.SpotDrawInfo;
import com.asus.supernote.doodle.drawutil.HWPoint;

public class SpotDrawTool extends DrawTool {

	public SpotDrawTool(int toolCode) {
		super(toolCode);
	}

	@Override
	protected void doDraw(Canvas canvas, DrawInfo drawInfo) {
		SpotDrawInfo spotDrawInfo=(SpotDrawInfo)drawInfo;
		Paint paint = spotDrawInfo.getPaint();
		paint.setStyle(Paint.Style.FILL);
		
		LinkedList<HWPoint> mOTPointList = spotDrawInfo.getOnTimeDrawList();
		if(mOTPointList.size()>1){
			curPoint = mOTPointList.get(0);
			for(int i =1; i<mOTPointList.size(); i++){				
				HWPoint point = mOTPointList.get(i);
				drawToPoint(canvas, point, paint);
				curPoint = point;
			}
			return;
		}
		
		LinkedList<HWPoint> mHWPointList = spotDrawInfo.getDrawList();
		
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
		return new SpotDrawInfo(this, usedPaint);
	}
	
	HWPoint curPoint;
	private void drawToPoint(Canvas canvas, HWPoint point, Paint paint){
		drawLine(canvas, curPoint.x, curPoint.y, curPoint.width, point.x, point.y, point.width, paint);
	}
	
	private void drawLine(Canvas canvas, double x0,double y0,double w0,double x1,double y1,double w1,Paint paint){
		double curDis = Math.hypot(x0-x1, y0-y1);
		int steps = 1+(int)(curDis/2.0);
		double deltaX=(x1-x0)/steps;
		double deltaY=(y1-y0)/steps;
		double deltaW=(w1-w0)/steps;
		double x=x0;
		double y=y0;
		double w=w0;
		
		for(int i=0;i<steps;i++){
			canvas.drawCircle((float)x, (float)y, (float)w/2.0f, paint);
			x+=deltaX;
			y+=deltaY;
			w+=deltaW;
		}
	}

}
