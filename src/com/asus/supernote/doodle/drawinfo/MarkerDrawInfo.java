package com.asus.supernote.doodle.drawinfo;

import android.graphics.Paint;
import android.graphics.Path;

import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawtool.MarkerDrawTool;
import com.asus.supernote.doodle.drawutil.HWPoint;

public class MarkerDrawInfo extends SpotDrawInfo{

	public MarkerDrawInfo(DrawTool drawTool, Paint usedPaint) {
		super(drawTool, usedPaint);
	}
	
	public MarkerDrawInfo(DrawTool drawTool, Paint usedPaint, short[] points) {
		super(drawTool, usedPaint, points);
	}
	
	public MarkerDrawInfo(DrawTool drawTool, Paint usedPaint, float[] points) {
		//super(drawTool, usedPaint, points);
		this(drawTool, usedPaint);
		initHWPointLists(points);
	}
	
	@Override
	protected DrawInfo cloneLock() {
		MarkerDrawInfo drawInfo = new MarkerDrawInfo(new MarkerDrawTool(mDrawTool.getToolCode()),mPaint);
		
		drawInfo.mPath = new Path(mPath);
		
		for (HWPoint point : mPointList) {
			HWPoint sPoint = new HWPoint();
			sPoint.Set(point);
            drawInfo.mPointList.add(sPoint);
        }
		
		for (HWPoint point : mHWPointList) {
			HWPoint sPoint = new HWPoint();
			sPoint.Set(point);
            drawInfo.mHWPointList.add(sPoint);
        }		
		return drawInfo;
	}
	
	protected void initHWPointLists(float[] points) {
		int length = points.length-2;
        int index = 0;
        HWPoint point;
        
        while (index < length) {
            point = new HWPoint(points[index++], points[index++]);
            mPointList.add(point);
            mHWPointList.add(point);
		}
    }

}
