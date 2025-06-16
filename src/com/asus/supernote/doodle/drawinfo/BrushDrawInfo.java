package com.asus.supernote.doodle.drawinfo;

import java.util.LinkedList;

import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import com.asus.supernote.data.MetaData;
import com.asus.supernote.doodle.drawtool.BrushDrawTool;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawutil.HWPoint;

public class BrushDrawInfo extends PenDrawInfo{
	
	public static final boolean ISBRUSHWITHALPHA = true;

	public BrushDrawInfo(DrawTool drawTool, Paint usedPaint) {
		super(drawTool, usedPaint);
	}
	
	public BrushDrawInfo(DrawTool drawTool, Paint usedPaint, short[] points){
		super(drawTool, usedPaint, points);
	}
	
	//For brush preview
	public BrushDrawInfo(DrawTool drawTool, Paint usedPaint, float[] points){
		this(drawTool, usedPaint);
		initHWPointLists(points);
	}
	
	@Override
	public void onDown(float[] x, float[] y, long timestamp) {
		HWPoint curPoint = new HWPoint(x[0],y[0],timestamp);
		
		mLastWidth = mBaseWidth;
		curPoint.width = (float)mLastWidth;
        mLastVel = 0;
		
		mPointList.add(curPoint);
		mLastPoint = curPoint;
		mPath.moveTo(x[0], y[0]);
		
		mOnTimeDrawList.clear();
	}
	
	@Override
	public void onMove(float[] x, float[] y, boolean multiPoint, long timestamp) {
		HWPoint curPoint = new HWPoint(x[0],y[0],timestamp);		
		
		// V->W
        double deltaX = curPoint.x - mLastPoint.x;
        double deltaY = curPoint.y - mLastPoint.y;
        double curDis = Math.hypot(deltaX, deltaY);
        double curVel = curDis / (curPoint.timestamp - mLastPoint.timestamp);
        double curWidth;		
        
		if(mPointList.size() < 2){
			curWidth = calcNewWidth(curVel, mLastVel, curDis, 1.3, mLastWidth);
			curPoint.width = (float)curWidth;
			mBezier.Init(mLastPoint, curPoint);
		}
		else{
			mLastVel = curVel;
            curWidth = calcNewWidth(curVel, mLastVel, curDis, 1.3, mLastWidth);
            curPoint.width = (float)curWidth;
			mBezier.AddNode(curPoint);
		}
		mLastWidth = curWidth;
		
		mPointList.add(curPoint);
		
		mOnTimeDrawList.clear();

		int steps = 1 + (int)curDis / STEPFACTOR;
		double step = 1.0 / steps;
		for (double t = 0; t < 1.0; t += step){
			HWPoint point = mBezier.GetPoint(t);
			if(ISBRUSHWITHALPHA){
				point = dealWithPointAlpha(point);
			}
	        mHWPointList.add(point);
	        mOnTimeDrawList.add(point);
	    }
		if(ISBRUSHWITHALPHA){
			mOnTimeDrawList.add(dealWithPointAlpha(mBezier.GetPoint(1.0)));
		}else{
			mOnTimeDrawList.add(mBezier.GetPoint(1.0));
		}
		calcNewDirtyRect(mOnTimeDrawList.get(0), mOnTimeDrawList.get(mOnTimeDrawList.size()-1));
		
		mPath.quadTo(mLastPoint.x, mLastPoint.y, (x[0] + mLastPoint.x) / 2, (y[0] + mLastPoint.y) / 2);
		
		mLastPoint = curPoint;
	}

	@Override
	public void onUp(float[] x, float[] y, long timestamp) {
		HWPoint curPoint = new HWPoint(x[0],y[0],timestamp);		
		mOnTimeDrawList.clear();
		double deltaX = curPoint.x - mLastPoint.x;
        double deltaY = curPoint.y - mLastPoint.y;
        double curDis = Math.hypot(deltaX, deltaY);
        curPoint.width = 0;
        
        mPointList.add(curPoint);
        
		mBezier.AddNode(curPoint);

		int steps = 1 + (int)curDis / STEPFACTOR;
		double step = 1.0 / steps;
		for (double t = 0; t < 1.0; t += step) {
	        HWPoint point = mBezier.GetPoint(t);
	        if(ISBRUSHWITHALPHA){
				point = dealWithPointAlpha(point);
			}
	        mHWPointList.add(point);
	    }
		
		mBezier.End();
		for (double t = 0; t < 1.0; t += step) {
			HWPoint point = mBezier.GetPoint(t);
			if(ISBRUSHWITHALPHA){
				point = dealWithPointAlpha(point);
			}
	        mHWPointList.add(point);
        }
		
		mPath.quadTo(mLastPoint.x, mLastPoint.y, (x[0] + mLastPoint.x) / 2, (y[0] + mLastPoint.y) / 2);
		mPath.lineTo(x[0],y[0]);
	}
	
	@Override
	protected DrawInfo cloneLock() {
		BrushDrawInfo drawInfo = new BrushDrawInfo(new BrushDrawTool(mDrawTool.getToolCode()),mPaint);
		
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
		
		drawInfo.SetTexture(mTextureOrigin);
		
		return drawInfo;
	}
	
	private HWPoint dealWithPointAlpha(HWPoint point){
		HWPoint nPoint = new HWPoint();
		nPoint.x = point.x;
		nPoint.y = point.y;
		nPoint.width = point.width;
		int alpha = (int)(255 * point.width / mBaseWidth / mPressureRatio);
		if(alpha < mMinAlpha){
			alpha = mMinAlpha;
		}else if(alpha > 255){
			alpha = 255;
		}
		nPoint.alpha = alpha;
		return nPoint;
	}
	
	@Override
	protected void initHWPointLists(short[] points) {
        float mX = 0;
        float mY = 0;
        int length = points.length-2;
        int index = 0;
        HWPoint point;
        HWPoint pointLast;
        double curWidth = 0;
		double curDis = 0;
		
        // Start Point
        point = new HWPoint(points[index++], points[index++]);
        //point.width = (float)(points[index++] / MetaData.PRESSURE_FACTOR * mPaint.getStrokeWidth());
        point.width = (float)(points[index++] / MetaData.PRESSURE_FACTOR * mPaint.getStrokeWidth()*(1+WIDTH_THRES_MAX));
        
        mPointList.add(point);
        mPath.moveTo(point.x, point.y);
        mX = point.x;
        mY = point.y;
        pointLast = point;

        // Other Points		
        while (index < length) {
            point = new HWPoint(points[index++], points[index++]);
            curWidth = points[index++] / MetaData.PRESSURE_FACTOR * mPaint.getStrokeWidth()*(1+WIDTH_THRES_MAX);
            
            mPath.quadTo(mX, mY, (point.x + mX) / 2, (point.y + mY) / 2);
            mX = point.x;
            mY = point.y;
            
            if(index==6){
            	point.width = (float)curWidth;
            	mBezier.Init(mPointList.get(0),point);
            }
            else{
                point.width = (float)curWidth;
            	mBezier.AddNode(point);
            }
            
            mPointList.add(point);
            curDis = getDistance(pointLast, point);
    		int steps = 1 + (int)curDis / STEPFACTOR;
    		double step = 1.0 / steps;
    		for (double t = 0; t < 1.0; t += step){
    			if(ISBRUSHWITHALPHA){
    				mHWPointList.add(dealWithPointAlpha(mBezier.GetPoint(t)));
    			}else{
    				mHWPointList.add(mBezier.GetPoint(t));
    			}
    	    }
    		pointLast = point;
        }

        // Last Point
        mPath.lineTo(mX, mY);
        mBezier.End();
        curDis = getDistance(pointLast, point);
		int steps = 1 + (int)curDis / STEPFACTOR;
		double step = 1.0 / steps;
		for (double t = 0; t < 1.0; t += step) {
			if(ISBRUSHWITHALPHA){
				mHWPointList.add(dealWithPointAlpha(mBezier.GetPoint(t)));
			}else{
				mHWPointList.add(mBezier.GetPoint(t));
			}
        }
    }
	
	@Override
	protected void initHWPointLists(LinkedList<HWPoint> points) {
		float mX = 0;
        float mY = 0;
        HWPoint point;
        HWPoint pointLast;
		double curDis = 0;

		mHWPointList.clear();

        // Start Point
        point = points.get(0);

        mPath.moveTo(point.x, point.y);
        mX = point.x;
        mY = point.y;
        
        pointLast = point;

        // Other Points		
        for(int i=1; i<points.size(); i++) {
            point = points.get(i);
            
            mPath.quadTo(mX, mY, (point.x + mX) / 2, (point.y + mY) / 2);
            mX = point.x;
            mY = point.y;
            
            if(i==1){
            	mBezier.Init(pointLast,point);
            }
            else{
            	mBezier.AddNode(point);
            }
            
            curDis = getDistance(pointLast, point);
    		int steps = 1 + (int)curDis / STEPFACTOR;
    		double step = 1.0 / steps;
    		for (double t = 0; t < 1.0; t += step){
    			if(ISBRUSHWITHALPHA){
    				mHWPointList.add(dealWithPointAlpha(mBezier.GetPoint(t)));
    			}else{
    				mHWPointList.add(mBezier.GetPoint(t));
    			}
    	    }
    		pointLast = point;
        }

        // Last Point
        mPath.lineTo(mX, mY);
        
        mBezier.End();        
        curDis = getDistance(pointLast, point);
		int steps = 1 + (int)curDis / STEPFACTOR;
		double step = 1.0 / steps;
		for (double t = 0; t < 1.0; t += step) {
			if(ISBRUSHWITHALPHA){
				mHWPointList.add(dealWithPointAlpha(mBezier.GetPoint(t)));
			}else{
				mHWPointList.add(mBezier.GetPoint(t));
			}
        }
	}
	
	protected void initHWPointLists(float[] points) {
		int length = points.length-2;
        int index = 0;
        HWPoint point;
        double curWidth = 0;
        
        while (index < length) {
            point = new HWPoint(points[index++], points[index++]);
            curWidth = points[index++] / MetaData.PRESSURE_FACTOR * mPaint.getStrokeWidth();
            point.width = (float)curWidth;
            mPointList.add(point);
            mHWPointList.add(point);
		}
    }
	
	@Override
	public void onMove(float[] x, float[] y, boolean multiPoint, int tooltype, long timestamp, float[] pressure){
		HWPoint curPoint = new HWPoint(x[0],y[0],timestamp);		
		
		// V->W
        double deltaX = curPoint.x - mLastPoint.x;
        double deltaY = curPoint.y - mLastPoint.y;
        double curDis = Math.hypot(deltaX, deltaY);
        double curVel = curDis * DIS_VEL_CAL_FACTOR;
        double curWidth;
        
		if(mPointList.size() < 2){
			if(tooltype == MotionEvent.TOOL_TYPE_STYLUS){
				curWidth = pressure[0] * mBaseWidth;
			}else {
				curWidth = calcNewWidth(curVel, mLastVel, curDis, 1.5, mLastWidth);
			}
			curPoint.width = (float)curWidth;
			mBezier.Init(mLastPoint, curPoint);
		}
		else{
			mLastVel = curVel;
			if(tooltype == MotionEvent.TOOL_TYPE_STYLUS){
				curWidth = pressure[0] * mBaseWidth;
			}else {
				curWidth = calcNewWidth(curVel, mLastVel, curDis, 1.5, mLastWidth);
			}
            curPoint.width = (float)curWidth;
			mBezier.AddNode(curPoint);
		}
		mLastWidth = curWidth;
		
		mPointList.add(curPoint);
		
		mOnTimeDrawList.clear();
		int steps = 1 + (int)curDis / STEPFACTOR;
		double step = 1.0 / steps;
		for (double t = 0; t < 1.0; t += step){
			HWPoint point = mBezier.GetPoint(t);
			if(ISBRUSHWITHALPHA){
				point = dealWithPointAlpha(point);
			}
	        mHWPointList.add(point);
	        mOnTimeDrawList.add(point);
	    }
		if(ISBRUSHWITHALPHA){
			mOnTimeDrawList.add(dealWithPointAlpha(mBezier.GetPoint(1.0)));
		}else{
			mOnTimeDrawList.add(mBezier.GetPoint(1.0));
		}
		calcNewDirtyRect(mOnTimeDrawList.get(0), mOnTimeDrawList.get(mOnTimeDrawList.size()-1));
		
		mPath.quadTo(mLastPoint.x, mLastPoint.y, (x[0] + mLastPoint.x) / 2, (y[0] + mLastPoint.y) / 2);
		
		mLastPoint = curPoint;
	}
	
	@Override
	public void onUp(float[] x, float[] y, int tooltype, long timestamp, float[] pressure){
		HWPoint curPoint = new HWPoint(x[0],y[0],timestamp);		
		mOnTimeDrawList.clear();
		double deltaX = curPoint.x - mLastPoint.x;
        double deltaY = curPoint.y - mLastPoint.y;
        double curDis = Math.hypot(deltaX, deltaY);
        if(tooltype == MotionEvent.TOOL_TYPE_STYLUS){
        	curPoint.width = (float)(pressure[0] * mBaseWidth);
        }else{
        	curPoint.width = 0;
        }
        
        mPointList.add(curPoint);
        
		mBezier.AddNode(curPoint);

		int steps = 1 + (int)curDis / STEPFACTOR;
		double step = 1.0 / steps;
		for (double t = 0; t < 1.0; t += step) {
	        HWPoint point = mBezier.GetPoint(t);
	        if(ISBRUSHWITHALPHA){
				point = dealWithPointAlpha(point);
			}
	        mHWPointList.add(point);
	        mOnTimeDrawList.add(point);
	    }
		
		mBezier.End();
		for (double t = 0; t < 1.0; t += step) {
			HWPoint point = mBezier.GetPoint(t);
			if(ISBRUSHWITHALPHA){
				point = dealWithPointAlpha(point);
			}
	        mHWPointList.add(point);
	        mOnTimeDrawList.add(point);
        }
		
		calcNewDirtyRect(mOnTimeDrawList.get(0), mOnTimeDrawList.get(mOnTimeDrawList.size()-1));
		mPath.quadTo(mLastPoint.x, mLastPoint.y, (x[0] + mLastPoint.x) / 2, (y[0] + mLastPoint.y) / 2);
		mPath.lineTo(x[0],y[0]);
	}
}
