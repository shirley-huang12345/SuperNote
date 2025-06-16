package com.asus.supernote.doodle.drawinfo;

import java.util.LinkedList;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem.SerDrawInfo;
import com.asus.supernote.doodle.DoodleItem.SerPointInfo;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawtool.NeonDrawTool;
import com.asus.supernote.doodle.drawtool.PathDrawTool;

public class PathDrawInfo extends DrawInfo {

    public static class Point {
        public float x;
        public float y;
        public float pressure = 1.0f;

        public Point() {
            this(0, 0, 0);
        }

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
            this.pressure = 1.0f;
        }
        public Point(float x, float y ,float pressure)
        {
        	   this.x = x;
               this.y = y;
               this.pressure = pressure;
        }

        public Point(Point point) {
            x = point.x;
            y = point.y;
            pressure = point.pressure;
        }
    }

    private static final int INVALIDATE_MARGIN = 15;
    protected static final int TOUCH_TOLERANCE = 20;
    private static final int BOUND_TOLERANCE = 30;
    public static final int SEGMENT_WIDTH = 50;//update by jason
    protected Path mPath;
    protected LinkedList<Point> mPointList;
    protected float mControlX, mControlY;
    protected float mEndX, mEndY;
	protected float tempCtrlX,tempCtrlY;

    public PathDrawInfo(DrawTool drawTool, Paint usedPaint) {
        super(drawTool, usedPaint);
        mPath = new Path();
        mPointList = new LinkedList<Point>();
    }

    public PathDrawInfo(DrawTool drawTool, Paint usedPaint, short[] points) {
        this(drawTool, usedPaint);
        initLists(points);
    }
    
    public PathDrawInfo(DrawTool drawTool, Paint usedPaint, float[] points) {
        this(drawTool, usedPaint);
        initLists(points);
    }
    

    @Override
    public DrawInfo cloneLock() {
        PathDrawInfo drawInfo;
        switch (mDrawTool.getToolCode()) {
            case DrawTool.NEON_TOOL:
                drawInfo = (PathDrawInfo) (new NeonDrawTool()).getDrawInfo(mPaint);
                break;
            default:
                drawInfo = (PathDrawInfo) (new PathDrawTool(mDrawTool.getToolCode())).getDrawInfo(mPaint);
        }

        drawInfo.mPath = new Path(mPath);
        for (Point point : mPointList) {
            drawInfo.mPointList.add(new Point(point));
        }
        return drawInfo;
    }

    protected void computeDirty(float x, float y) {
        // Compute Dirty Rectangle
        int invalidateMargin = getInvalidateMargin();
		int left = (int) (min(x, mControlX, tempCtrlX)) - invalidateMargin;
		int right = (int) (max(x, mControlX, tempCtrlX)) + invalidateMargin;
		int top = (int) (min(y, mControlY, tempCtrlY)) - invalidateMargin;
		int bottom = (int) (max(y, mControlY, tempCtrlY)) + invalidateMargin;
        if (mDirtyRect == null) {
            mDirtyRect = new Rect(left, top, right, bottom);
        }
        else {
            mDirtyRect.union(left, top, right, bottom);
        }
    }

    @Override
    public RectF getBounds() {
        RectF bounds = addStrokeToBounds(getStrictBounds());
        return bounds;
    }

    protected int getInvalidateMargin() {
        return INVALIDATE_MARGIN + (int) (mPaint.getStrokeWidth());
    }

    public Path getPath() {
        return mPath;
    }

    public LinkedList<Point> getPoints() {
        return mPointList;
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

    protected void initLists(short[] points) {
        float mX = 0;
        float mY = 0;
        int length = points.length;
        int index = 0;
        Point point;
        
        if(length <= 1){
        	return ;
        }

        // Start Point
        point = new Point(points[index++], points[index++]);
        mPointList.add(point);
        mPath.moveTo(point.x, point.y);
        mX = point.x;
        mY = point.y;

        // Other Points
        while (index < length) {
            point = new Point(points[index++], points[index++]);
            mPointList.add(point);
            mPath.quadTo(mX, mY, (point.x + mX) / 2, (point.y + mY) / 2);
            mX = point.x;
            mY = point.y;
        }

        // Last Point
        mPath.lineTo(mX, mY);
    }
    
    //add mars for deal float point 
    protected void initLists(float[] points) {
        float mX = 0;
        float mY = 0;
        int length = points.length;
        int index = 0;
        Point point;

        // Start Point
        point = new Point(points[index++], points[index++]);
        mPointList.add(point);
        mPath.moveTo(point.x, point.y);
        mX = point.x;
        mY = point.y;

        // Other Points
        while (index < length) {
            point = new Point(points[index++], points[index++]);
            mPointList.add(point);
            mPath.quadTo(mX, mY, (point.x + mX) / 2, (point.y + mY) / 2);
            mX = point.x;
            mY = point.y;
        }

        // Last Point
        mPath.lineTo(mX, mY);
    }

    @Override
    public boolean isTouched(float x, float y) {
        PathMeasure pathMeasure = new PathMeasure(mPath, false);
        RectF bounds = new RectF();
        float length = pathMeasure.getLength();

        for (int i = 0; i < length; i += SEGMENT_WIDTH) {
            Path path = new Path();
            if (pathMeasure.getSegment(i, i + SEGMENT_WIDTH, path, true)) {
                path.computeBounds(bounds, true);
                bounds.left -= TOUCH_TOLERANCE;
                bounds.top -= TOUCH_TOLERANCE;
                bounds.right += TOUCH_TOLERANCE;
                bounds.bottom += TOUCH_TOLERANCE;
                if (bounds.contains(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void mapPoints(Matrix matrix) {
        float[] pts = new float[mPointList.size() * 2];
        int index = 0;

        for (Point point : mPointList) {
            pts[index++] = point.x;
            pts[index++] = point.y;
        }

        matrix.mapPoints(pts);

        index = 0;
        for (Point point : mPointList) {
            point.x = pts[index++];
            point.y = pts[index++];
        }
    }

    @Override
    public void onDown(float[] x, float[] y) {
        mEndX = x[0];
        mEndY = y[0];
        mControlX = x[0];
        mControlY = y[0];
        mPath.moveTo(x[0], y[0]);
        mPointList.add(new Point(x[0], y[0]));
    }

    //smilefish fix bug 795382/801996
    private void moveToHistory(MotionEvent ev){
    	final int historySize = ev.getHistorySize();
		for (int h = 0; h < historySize; h++) {
			float x = ev.getHistoricalX(h);
			float y = ev.getHistoricalY(h);

	        final float dx = Math.abs(x - mEndX);  
	        final float dy = Math.abs(y - mEndY); 

	        if((dx * dx + dy * dy) < 4) continue;		

	        tempCtrlX = mEndX;
	        tempCtrlY = mEndY;

	        mEndX = x;
	        mEndY = y;

	        mPath.quadTo(tempCtrlX, tempCtrlY, (x + tempCtrlX) / 2, (y + tempCtrlY) / 2);

	        mPointList.add(new Point(x, y));

	        computeDirty(x, y);

	        mControlX = tempCtrlX;
	        mControlY = tempCtrlY;
		}
    }

    @Override
    public void onMove(MotionEvent event, float[] x, float[] y, boolean multiPoint) {
    	moveToHistory(event);

        final float dx = Math.abs(x[0] - mEndX);  
        final float dy = Math.abs(y[0] - mEndY);

        if((dx * dx + dy * dy) < 4) return;

        tempCtrlX = mEndX;
        tempCtrlY = mEndY;

        mEndX = x[0];
        mEndY = y[0];
          
//        final float dx = Math.abs(x[0] - tempCtrlX);  
//        final float dy = Math.abs(y[0] - tempCtrlY);
//
//        //emmenual to fix bug 564477
//        if (dx >= 2 || dy >= 2)  
//        {  
        	mPath.quadTo(tempCtrlX, tempCtrlY, (x[0] + tempCtrlX) / 2, (y[0] + tempCtrlY) / 2);
//        }else{
//            mPath.lineTo(x[0], y[0]);
//        }
        
        mPointList.add(new Point(x[0], y[0]));

        computeDirty(x[0], y[0]);

        mControlX = tempCtrlX;
        mControlY = tempCtrlY;
    }

    protected float min(float a, float b){
		return Math.min(a, b);
	}
	
	protected float max(float a, float b){
		return Math.max(a, b);
	}
	
	protected float min(float a, float b, float c){
		return Math.min(a, Math.min(b, c));
	}
	
	protected float max(float a, float b, float c){
		return Math.max(a, Math.max(b, c));
	}

    @Override
    public void onUp(float[] x, float[] y) {
        mEndX = x[0];
        mEndY = y[0];
        mPath.lineTo(mEndX, mEndY);
        mPointList.add(new Point(mEndX, mEndY));
    }

    @Override
    public SerDrawInfo saveLock(NotePage note) {
        SerPointInfo serInfo = new SerPointInfo();
        serInfo.setColor(mPaint.getColor());
        serInfo.setStrokeWidth(mPaint.getStrokeWidth());
        // BEGIN: Better
        int toolCode = mDrawTool.getToolCode();
        serInfo.setPaintTool(toolCode);
        if (toolCode == DrawTool.MARKPEN_TOOL) {
        	serInfo.setAlpha(mPaint.getAlpha());
        }
        // END: Better
        serInfo.setPoints(mPointList);
        return serInfo;
    }

    @Override
    protected void transformLock(Matrix matrix) {
        mPath.transform(matrix);
        mapPoints(matrix);
    }

    //BEGIN: RICHARD
    public float[] getPointArray()
    {        
    	float[] pointArray = new float[mPointList.size() * 2];
    	
    	int i = 0;
    	for (Point point : mPointList) {
    		pointArray[i++] = point.x;
    		pointArray[i++] = point.y;
    	}
    	
    	return pointArray;
    }
    //END: RICHARD
}
