package com.asus.supernote.doodle.drawinfo;

import android.graphics.Paint;
import android.graphics.Path;

import com.asus.supernote.doodle.drawtool.BallPenDrawTool;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawutil.HWPoint;

public class BallPenDrawInfo extends SpotDrawInfo{
	
	public BallPenDrawInfo(DrawTool drawTool, Paint usedPaint) {
		super(drawTool, usedPaint);
	}

	public BallPenDrawInfo(DrawTool drawTool, Paint usedPaint, short[] points){
		super(drawTool, usedPaint, points);
	}
	
	@Override
	protected DrawInfo cloneLock() {
		BallPenDrawInfo drawInfo = new BallPenDrawInfo(new BallPenDrawTool(mDrawTool.getToolCode()),mPaint);
		
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
	
}
