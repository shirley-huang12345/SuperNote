package com.asus.supernote.doodle.drawtool;

import java.util.LinkedList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.MarkerDrawInfo;
import com.asus.supernote.doodle.drawutil.HWPoint;

public class MarkerDrawTool extends SpotDrawTool{

	double mWidth;
	Paint mLinePaint;
	
	public MarkerDrawTool(int toolCode) {
		super(toolCode);
	}
	
	@Override
	protected void doDraw(Canvas canvas, DrawInfo drawInfo) {
		MarkerDrawInfo spotDrawInfo=(MarkerDrawInfo)drawInfo;
		Paint paint = spotDrawInfo.getPaint();
		paint.setStyle(Paint.Style.FILL);
		mWidth = paint.getStrokeWidth();
		
		mLinePaint = new Paint(paint);
		mLinePaint.setStrokeWidth(0.5f);

		LinkedList<HWPoint> mHWPointList = spotDrawInfo.getDrawList();
		
		if(mHWPointList==null ||  mHWPointList.size()<1 )
			return;
		
		if(mHWPointList.size()<2){
			HWPoint point = mHWPointList.get(0);
			canvas.drawCircle(point.x, point.y, point.width, paint);
		}else{
			drawStartPoints(mHWPointList.get(0),mHWPointList.get(1),canvas, paint);
			curPoint = mHWPointList.get(0);			
			for(int i =1; i<mHWPointList.size(); i++){				
				HWPoint point = mHWPointList.get(i);
				drawToPoint(canvas, point, paint);
				curPoint = point;
			}
			drawStartPoints(mHWPointList.get(mHWPointList.size()-1),mHWPointList.get(mHWPointList.size()-2),canvas, paint);
		}
	}
	
	@Override
	public DrawInfo getDrawInfo(Paint usedPaint) {
		return new MarkerDrawInfo(this, usedPaint);
	}
	
	HWPoint curPoint;
	private void drawToPoint(Canvas canvas, HWPoint point, Paint paint){
		drawLine(canvas, curPoint.x, curPoint.y, curPoint.width, point.x, point.y, point.width, paint);
	}
	
	private void drawLine(Canvas canvas, double x0,double y0,double w0,double x1,double y1,double w1,Paint paint){
		Path path = new Path();

		double tan = Math.tan(Math.toRadians(85));
		double temp = Math.sqrt(mWidth*mWidth/(4*(1+tan*tan)));

		path.moveTo((float)(x0-temp), (float)(y0-temp*tan));
		path.lineTo((float)(x1-temp), (float)(y1-temp*tan));
		path.lineTo((float)(x1+temp), (float)(y1+temp*tan));
		path.lineTo((float)(x0+temp), (float)(y0+temp*tan));
		path.lineTo((float)(x0-temp), (float)(y0-temp*tan));
		path.close();

		canvas.drawPath(path, paint);

		//emmanual to fix bug 537067, 537904
//		Path path2 = new Path();
//		path2.moveTo((float)(x0), (float)(y0-temp*tan));
//		path2.lineTo((float)(x1), (float)(y1-temp*tan));
//		path2.lineTo((float)(x1+temp*2), (float)(y1+temp*tan));
//		path2.lineTo((float)(x0+temp*2), (float)(y0+temp*tan));
//		path2.lineTo((float)(x0), (float)(y0-temp*tan));
//		path2.close();
//		canvas.drawPath(path2, paint);
	}
	
	private void drawPoints(Canvas canvas, HWPoint point1, HWPoint point2, Paint paint){
		drawLine(canvas, point1.x, point1.y, point1.width, point2.x, point2.y, point2.width, paint);
	}
	
	private void drawStartPoints(HWPoint p0, HWPoint p1, Canvas canvas, Paint paint){
		double d = 3;
		double k = 0;
		double x2,y2;
		double x3,y3;
		HWPoint p2,p3;
		
		if(p0.x != p1.x){
			k = (p0.y-p1.y)/(p0.x-p1.x);
			
			x2 = p0.x - d/Math.sqrt(1+k*k);
			y2 = p0.y - d*k/Math.sqrt(1+k*k);
			p2 = new HWPoint((float)x2,(float)y2);
			
			x3 = p0.x + d/Math.sqrt(1+k*k);
			y3 = p0.y + d*k/Math.sqrt(1+k*k);
			p3 = new HWPoint((float)x3,(float)y3);
		}else{
			p2 = new HWPoint((float)p0.x, (float)(p0.y-d));
			p3 = new HWPoint((float)p0.x, (float)(p0.y+d));
		}		
		
		Paint pa = new Paint(paint);
		int alpha = pa.getAlpha()*3;
		if(alpha > 255)
			alpha = 255;
		pa.setAlpha(alpha);
		drawPoints(canvas, p2, p0, pa);
	}
	
	@Override
	public void drawPreview(Canvas canvas, DrawInfo drawInfo) {	
		MarkerDrawInfo spotDrawInfo=(MarkerDrawInfo)drawInfo;
		Paint paint = spotDrawInfo.getPaint();
		paint.setStyle(Paint.Style.FILL);
		mWidth = paint.getStrokeWidth();
		
		mLinePaint = new Paint(paint);
		mLinePaint.setStrokeWidth(0.5f);
		
		LinkedList<HWPoint> mHWPointList = spotDrawInfo.getDrawList();
		
		if(mHWPointList==null ||  mHWPointList.size()<1 )
			return;
		
		drawStartPoints(mHWPointList.get(0),mHWPointList.get(1),canvas, paint);
		
		canvas.drawPath(getPreviewPath(mHWPointList), paint);
		
		drawStartPoints(mHWPointList.get(mHWPointList.size()-1),mHWPointList.get(mHWPointList.size()-2),canvas, paint);
	}
	
	private Path getPreviewPath(LinkedList<HWPoint> mHWPointList){
		LinkedList<HWPoint> upPoints = new LinkedList<HWPoint>();
		LinkedList<HWPoint> downPoints = new LinkedList<HWPoint>();
		double tan = Math.tan(Math.toRadians(85));
		double temp = Math.sqrt(mWidth*mWidth/(4*(1+tan*tan)));
		HWPoint point;
		for(int i = 0; i<mHWPointList.size(); i++){
			point = mHWPointList.get(i);
			upPoints.add(new HWPoint((float)(point.x-temp), (float)(point.y-temp*tan)));
			downPoints.add(new HWPoint((float)(point.x+temp), (float)(point.y+temp*tan)));
		}
		
		Path path = new Path();
		path.moveTo(upPoints.get(0).x, upPoints.get(0).y);
		for(int i = 1; i<upPoints.size(); i++){
			point = upPoints.get(i);
			path.lineTo(point.x, point.y);
		}
		for(int i = downPoints.size()-1; i >= 0; i--){
			point = downPoints.get(i);
			path.lineTo(point.x, point.y);
		}
		path.lineTo(upPoints.get(0).x, upPoints.get(0).y);
		
		return path;
	}

}
