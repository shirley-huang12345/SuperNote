package com.asus.supernote.doodle.drawinfo;

import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;

import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem.SerDrawInfo;
import com.asus.supernote.doodle.DoodleItem.SerPointInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo.Point;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawtool.PenDrawTool;
import com.asus.supernote.doodle.drawutil.HWPoint;

public class PenDrawInfo extends BallPenDrawInfo{
	
	private Paint pPaint;
	protected Bitmap mTexture;
	protected Bitmap mTextureOrigin;

	public PenDrawInfo(DrawTool drawTool, Paint usedPaint) {
		super(drawTool, usedPaint);
		pPaint = new Paint(usedPaint);
	}
	
	public PenDrawInfo(DrawTool drawTool, Paint usedPaint, short[] points){
		super(drawTool, usedPaint, points);
		pPaint = new Paint(usedPaint);
	}
	
	public PenDrawInfo(DrawTool drawTool, Paint usedPaint, float[] points){
		this(drawTool, usedPaint);
		initHWPointLists(points);
		pPaint = new Paint(usedPaint);
	}
	
	public void SetTexture(Bitmap texture){
		mTextureOrigin = texture;
		Canvas canvas = new Canvas();
		mTexture = Bitmap.createBitmap(texture.getWidth(), texture.getHeight(), Bitmap.Config.ARGB_8888);
		mTexture.eraseColor(Color.rgb(Color.red(pPaint.getColor()), Color.green(pPaint.getColor()), Color.blue(pPaint.getColor())));
		canvas.setBitmap(mTexture);
		
		Paint paint = new Paint();		
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawBitmap(texture, 0, 0, paint);	
	}

	public Bitmap GetLocalTexture(){
		return mTexture;
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
	        mHWPointList.add(point);
	        mOnTimeDrawList.add(point);
	    }
		mOnTimeDrawList.add(mBezier.GetPoint(1.0));
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
        LinkedList<Point> points = getPointList();
        serInfo.setPoints(points, true);
        return serInfo;
	}
	
	@Override
	protected LinkedList<Point> getPointList() {
		LinkedList<Point> points = new LinkedList<Point>();
		for(HWPoint point:mPointList){
			Point newPoint = point.ToPoint();
			newPoint.pressure = point.width/(mPaint.getStrokeWidth()*(1+WIDTH_THRES_MAX));
			points.add(newPoint);
		}
		return points;
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
	
	@Override
	protected DrawInfo cloneLock() {
		PenDrawInfo drawInfo = new PenDrawInfo(new PenDrawTool(mDrawTool.getToolCode()),mPaint);
		
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
	
	protected void initHWPointLists(float[] points) {
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
        point.width = (float)(points[index++] / MetaData.PRESSURE_FACTOR * mPaint.getStrokeWidth());
        
        mPointList.add(point);
        mPath.moveTo(point.x, point.y);
        mX = point.x;
        mY = point.y;
        pointLast = point;

        // Other Points		
        while (index < length) {
            point = new HWPoint(points[index++], points[index++]);
            curWidth = (points[index++] / MetaData.PRESSURE_FACTOR * mPaint.getStrokeWidth());
            
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
	
	public Path getPath() {
        return mPath;
    }
}
