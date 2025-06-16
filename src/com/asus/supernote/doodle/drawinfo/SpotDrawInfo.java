package com.asus.supernote.doodle.drawinfo;

import java.util.LinkedList;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.asus.supernote.SuperNoteApplication;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem.SerDrawInfo;
import com.asus.supernote.doodle.DoodleItem.SerPointInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo.Point;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawutil.HWPoint;
import com.asus.supernote.doodle.drawutil.QuadBezierSpline;

public class SpotDrawInfo extends DrawInfo {
	
	protected int BOUND_TOLERANCE = 30;
	protected int INVALIDATE_MARGIN = 15;
	protected float DIS_VEL_CAL_FACTOR = 0.02f;
	protected float WIDTH_THRES_MAX = 0.6f;
	protected int STEPFACTOR = 8;

	protected LinkedList<HWPoint> mPointList;
	protected LinkedList<HWPoint> mHWPointList;
	protected float mWidth = 2;
	protected int mColor = 0;
	protected QuadBezierSpline mBezier;
	protected HWPoint mLastPoint;
	protected Path mPath; // for bound calc
	protected double mBaseWidth;
	protected LinkedList<HWPoint> mOnTimeDrawList;
	protected float mPressureRatio = 2.0f;
	protected int mMinAlpha = 10;
	
	public SpotDrawInfo(DrawTool drawTool, Paint usedPaint){
		super(drawTool, usedPaint);
		mPointList = new LinkedList<HWPoint>();
		mHWPointList = new LinkedList<HWPoint>();
		
		mBezier = new QuadBezierSpline();
		mBaseWidth = mPaint.getStrokeWidth();
		
		mPath = new Path();
		mOnTimeDrawList = new LinkedList<HWPoint>();
		
		mLastPoint = new HWPoint(0,0);
		mPressureRatio = MethodUtils.getPressureRatio();
		mMinAlpha = MethodUtils.getMinAlpha(SuperNoteApplication.getContext());
	}
	
	public SpotDrawInfo(DrawTool drawTool, Paint usedPaint, short[] points){
		this(drawTool, usedPaint);
		initHWPointLists(points);
	}

	@Override
	protected DrawInfo cloneLock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RectF getBounds() {
		RectF bounds = addStrokeToBounds(getStrictBounds());
        return bounds;
	}

	@Override
	public RectF getStrictBounds() {
		RectF bounds = new RectF();
        mPath.computeBounds(bounds, false);
        if (bounds.height() < BOUND_TOLERANCE) {
            bounds.top = bounds.centerY() - BOUND_TOLERANCE;
            bounds.bottom = bounds.centerY() + BOUND_TOLERANCE;
        }
        if (bounds.width() < BOUND_TOLERANCE) {
            bounds.left = bounds.centerX() - BOUND_TOLERANCE;
            bounds.right = bounds.centerX() + BOUND_TOLERANCE;
        }
        return bounds;
	}

	@Override
	public void onDown(float[] x, float[] y) {
		HWPoint curPoint = new HWPoint(x[0],y[0]);
		mPointList.add(curPoint);
		
		mLastPoint = curPoint;
		mPath.moveTo(x[0], y[0]);
	}

	@Override
	public void onMove(MotionEvent event, float[] x, float[] y, boolean multiPoint) {
		HWPoint curPoint = new HWPoint(x[0],y[0]);
		mPointList.add(curPoint);
		
		if(mPointList.size()<3){
			mBezier.Init(mPointList.get(0),mPointList.get(1));
		}
		else{
			mBezier.AddNode(curPoint);
		}
		
		double curDis = getDistance(curPoint, mLastPoint);
		int steps = 1 + (int)curDis / STEPFACTOR;
		double step = 1.0 / steps;
		for (double t = 0; t < 1.0; t += step){
	        HWPoint point = mBezier.GetPoint(t);
	        mHWPointList.add(point);	        
	    }
		
		mPath.quadTo(mLastPoint.x, mLastPoint.y, (x[0] + mLastPoint.x) / 2, (y[0] + mLastPoint.y) / 2);
		
		mLastPoint = curPoint;
	}

	@Override
	public void onUp(float[] x, float[] y) {
		HWPoint curPoint = new HWPoint(x[0],y[0]);
		mPointList.add(curPoint);
		
		mBezier.AddNode(curPoint);
		double curDis = getDistance(curPoint, mLastPoint);
		int steps = 1 + (int)curDis / STEPFACTOR;
		double step = 1.0 / steps;
		for (double t = 0; t < 1.0; t += step) {
	        HWPoint point = mBezier.GetPoint(t);
	        mHWPointList.add(point);
	    }
		
		mBezier.End();
		for (double t = 0; t < 1.0; t += step) {
			HWPoint point = mBezier.GetPoint(t);
	        mHWPointList.add(point);
        }
		
		mPath.quadTo(mLastPoint.x, mLastPoint.y, (x[0] + mLastPoint.x) / 2, (y[0] + mLastPoint.y) / 2);
		mPath.lineTo(x[0],y[0]);
	}

	@Override
	protected SerDrawInfo saveLock(NotePage note) {
		SerPointInfo serInfo = new SerPointInfo();
        serInfo.setColor(mPaint.getColor());
        serInfo.setStrokeWidth(mPaint.getStrokeWidth());
        int toolCode = mDrawTool.getToolCode();
        serInfo.setPaintTool(toolCode);
        if (toolCode == DrawTool.MARKPEN_TOOL) {
        	serInfo.setAlpha(mPaint.getAlpha());
        }
        LinkedList<Point> points = getPointList();
        serInfo.setPoints(points);
        return serInfo;
	}

	@Override
	protected void transformLock(Matrix matrix) {
		mPath.transform(matrix);
		mBaseWidth = mPaint.getStrokeWidth();
	       
		float[] matrixValue = new float[9];
		matrix.getValues(matrixValue);
		float scaleX = (float) Math.sqrt(Math.pow(matrixValue[Matrix.MSCALE_X], 2) + Math.pow(matrixValue[Matrix.MSKEW_X], 2));
		float[] pts = new float[mPointList.size() * 2];
        int index = 0;

        for (HWPoint point : mPointList) {
            pts[index++] = point.x;
            pts[index++] = point.y;
        }

        matrix.mapPoints(pts);

        index = 0;
        for (HWPoint point : mPointList) {
            point.x = pts[index++];
            point.y = pts[index++];
            point.width = point.width * scaleX;
        }
        
       initHWPointLists(mPointList);
	}
	
	protected double getDistance(HWPoint p0, HWPoint p1){
		return Math.sqrt((p0.x-p1.x)*(p0.x-p1.x)+(p0.y-p1.y)*(p0.y-p1.y));
	}
	
	protected LinkedList<Point> getPointList() {
		LinkedList<Point> points = new LinkedList<Point>();
		for(HWPoint point:mPointList){
			points.add(point.ToPoint());
		}
		return points;
	}
	
	protected void initHWPointLists(short[] points) {
        float mX = 0;
        float mY = 0;
        int length = points.length-1;
        int index = 0;
        HWPoint point;
        HWPoint pointLast;
        double curWidth = 0;
		double curVel = 0;
		double curDis = 0;
		double lastWidth = 0;
		double lastVel = 0;
		mHWPointList.clear();

        // Start Point
        point = new HWPoint(points[index++], points[index++]);
        lastWidth = 0.8 * mBaseWidth;
        point.width = (float)lastWidth;
        mPointList.add(point);
        mPath.moveTo(point.x, point.y);
        mX = point.x;
        mY = point.y;
        pointLast = point;
        lastVel = 0; 

        // Other Points		
        while (index < length) {
            point = new HWPoint(points[index++], points[index++]);            
            
            curDis = getDistance(pointLast, point);
            curVel = curDis * DIS_VEL_CAL_FACTOR;
            
            mPath.quadTo(mX, mY, (point.x + mX) / 2, (point.y + mY) / 2);
            mX = point.x;
            mY = point.y;
            
            if(index==4){            	
            	curWidth = calcNewWidth(curVel, lastVel, curDis, 1.5, lastWidth);
            	point.width = (float)curWidth;
            	mBezier.Init(mPointList.get(0),point);
            }
            else{
            	lastVel = curVel;
                curWidth = calcNewWidth(curVel, lastVel, curDis, 1.5, lastWidth);
                point.width = (float)curWidth;
            	mBezier.AddNode(point);
            }
            
            mPointList.add(point);
            
    		int steps = 1 + (int)curDis / STEPFACTOR;
    		double step = 1.0 / steps;
    		for (double t = 0; t < 1.0; t += step){
    	        mHWPointList.add(mBezier.GetPoint(t));
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
			mHWPointList.add(mBezier.GetPoint(t));
        }
    }
	
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
    	        mHWPointList.add(mBezier.GetPoint(t));
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
			mHWPointList.add(mBezier.GetPoint(t));
        }
	}
	
	public LinkedList<HWPoint> getDrawList(){
		return mHWPointList;
	}
	
	public LinkedList<HWPoint> getOnTimeDrawList(){
		return mOnTimeDrawList;
	}
	
	public void clearOnTimeDrawList(){
		mOnTimeDrawList.clear();
	}
	
	protected double mLastVel;
	protected double mLastWidth;
	public void onDown(float[] x, float[] y, long timestamp) {
		HWPoint curPoint = new HWPoint(x[0],y[0],timestamp);
		
		mLastWidth = 0.8 * mBaseWidth;
		curPoint.width = (float)mLastWidth;
        mLastVel = 0;        
		
		mPointList.add(curPoint);
		mLastPoint = curPoint;
		mPath.moveTo(x[0], y[0]);
		
		mOnTimeDrawList.clear();
	}
	
	public void onMove(float[] x, float[] y, boolean multiPoint, long timestamp) {
		HWPoint curPoint = new HWPoint(x[0],y[0],timestamp);		
		
		// V->W
        double deltaX = curPoint.x - mLastPoint.x;
        double deltaY = curPoint.y - mLastPoint.y;
        double curDis = Math.hypot(deltaX, deltaY);
        double curVel = curDis * DIS_VEL_CAL_FACTOR;
        double curWidth;

		if(mPointList.size() < 2){
			curWidth = calcNewWidth(curVel, mLastVel, curDis, 1.5, mLastWidth);
			curPoint.width = (float)curWidth;
			mBezier.Init(mLastPoint, curPoint);
		}
		else{
			mLastVel = curVel;
            curWidth = calcNewWidth(curVel, mLastVel, curDis, 1.5, mLastWidth);
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
	        mHWPointList.add(point);
	        mOnTimeDrawList.add(point);
	    }
		mOnTimeDrawList.add(mBezier.GetPoint(1.0));
		calcNewDirtyRect(mOnTimeDrawList.get(0), mOnTimeDrawList.get(mOnTimeDrawList.size()-1));
		
		mPath.quadTo(mLastPoint.x, mLastPoint.y, (x[0] + mLastPoint.x) / 2, (y[0] + mLastPoint.y) / 2);
		
		mLastPoint = curPoint;
	}

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
	        mHWPointList.add(point);
	    }
		
		mBezier.End();
		for (double t = 0; t < 1.0; t += step) {
			HWPoint point = mBezier.GetPoint(t);
	        mHWPointList.add(point);
        }
		
		mPath.quadTo(mLastPoint.x, mLastPoint.y, (x[0] + mLastPoint.x) / 2, (y[0] + mLastPoint.y) / 2);
		mPath.lineTo(x[0],y[0]);
	}	
	
	protected double calcNewWidth(double curVel, double lastVel, double curDis, double factor, double lastWidth){
		// A simple low pass filter to mitigate velocity aberrations.
        double calVel = curVel * 0.6 + lastVel * (1 - 0.6);
        double vfac = Math.log(factor * 2.0f) * ( -calVel) ;
        double calWidth = mBaseWidth * Math.exp(vfac);
        
        double mMoveThres = curDis * 0.01f;
        if (mMoveThres > WIDTH_THRES_MAX) {
            mMoveThres = WIDTH_THRES_MAX;
        }

        if (Math.abs(calWidth - mBaseWidth) / mBaseWidth > mMoveThres) {
            if (calWidth > mBaseWidth) {
                calWidth = mBaseWidth * (1 + mMoveThres);
            }else {
                calWidth = mBaseWidth * (1 - mMoveThres);
            }
        }
        else if (Math.abs(calWidth - lastWidth) / lastWidth > mMoveThres){
            if (calWidth > lastWidth) {
                calWidth = lastWidth * (1 + mMoveThres);
            }else {
                calWidth = lastWidth * (1 - mMoveThres);
            }
        }
        return calWidth;
    }
	
	protected void calcNewDirtyRect(HWPoint p0, HWPoint p1){
		int margin = getMargin();
		mDirtyRect = new Rect();		
		mDirtyRect.left = (p0.x < p1.x) ? (int)p0.x-margin : (int)p1.x-margin;
		mDirtyRect.right = (p0.x > p1.x) ? (int)p0.x+margin : (int)p1.x+margin;
		mDirtyRect.top = (p0.y < p1.y) ? (int)p0.y-margin : (int)p1.y-margin;
		mDirtyRect.bottom = (p0.y > p1.y) ? (int)p0.y+margin : (int)p1.y+margin;
	}
	
	protected int getMargin(){
		return INVALIDATE_MARGIN + (int) (mPaint.getStrokeWidth());
	}
	
	// For shape recognize
	public float[] getPointArray()
    {        
    	float[] pointArray = new float[mPointList.size() * 2];
    	
    	int i = 0;
    	for (HWPoint point : mPointList) {
    		pointArray[i++] = point.x;
    		pointArray[i++] = point.y;
    	}
    	
    	return pointArray;
    }
	
	public void onDown(float[] x, float[] y, int tooltype, long timestamp, float[] pressure){		
		HWPoint curPoint = new HWPoint(x[0],y[0],timestamp);
		
		if(tooltype == MotionEvent.TOOL_TYPE_STYLUS){
			mLastWidth = pressure[0] * mBaseWidth;
		}else{
			mLastWidth = 0.8 * mBaseWidth;
		}
		curPoint.width = (float)mLastWidth;
        mLastVel = 0;        
		
		mPointList.add(curPoint);
		mLastPoint = curPoint;
		mPath.moveTo(x[0], y[0]);
		
		mOnTimeDrawList.clear();
	}
	
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
	        mHWPointList.add(point);
	        mOnTimeDrawList.add(point);
	    }
		mOnTimeDrawList.add(mBezier.GetPoint(1.0));
		calcNewDirtyRect(mOnTimeDrawList.get(0), mOnTimeDrawList.get(mOnTimeDrawList.size()-1));
		
		mPath.quadTo(mLastPoint.x, mLastPoint.y, (x[0] + mLastPoint.x) / 2, (y[0] + mLastPoint.y) / 2);
		
		mLastPoint = curPoint;
	}
	
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
	        mHWPointList.add(point);
	        mOnTimeDrawList.add(point);
	    }
		
		mBezier.End();
		for (double t = 0; t < 1.0; t += step) {
			HWPoint point = mBezier.GetPoint(t);
	        mHWPointList.add(point);
	        mOnTimeDrawList.add(point);
        }
		
		calcNewDirtyRect(mOnTimeDrawList.get(0), mOnTimeDrawList.get(mOnTimeDrawList.size()-1));
		mPath.quadTo(mLastPoint.x, mLastPoint.y, (x[0] + mLastPoint.x) / 2, (y[0] + mLastPoint.y) / 2);
		mPath.lineTo(x[0],y[0]);
	}
	
	protected double calcNewWidthWithPressure(double curVel, double lastVel, double curDis, double lastWidth, float curPressure){
		// A simple low pass filter to mitigate velocity aberrations.
        double calVel = curVel * 0.6 + lastVel * (1 - 0.6);
        double vfac = Math.log(3.0f) * (-calVel);
        double calWidth = mBaseWidth * (0.5 * Math.exp(vfac) + 0.5 * curPressure);
        
        double mMoveThres = curDis * 0.01f;
        if (mMoveThres > WIDTH_THRES_MAX) {
            mMoveThres = WIDTH_THRES_MAX;
        }

        if (Math.abs(calWidth - mBaseWidth) / mBaseWidth > mMoveThres) {
            if (calWidth > mBaseWidth) {
                calWidth = mBaseWidth * (1 + mMoveThres);
            }else {
                calWidth = mBaseWidth * (1 - mMoveThres);
            }
        }
        else if (Math.abs(calWidth - lastWidth) / lastWidth > mMoveThres){
            if (calWidth > lastWidth) {
                calWidth = lastWidth * (1 + mMoveThres);
            }else {
                calWidth = lastWidth * (1 - mMoveThres);
            }
        }
        return calWidth;
    }
}
