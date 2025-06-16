package com.asus.supernote.doodle.drawinfo;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem.SerDrawInfo;
import com.asus.supernote.doodle.DoodleItem.SerPointInfo;
import com.asus.supernote.doodle.drawtool.DrawTool;
//added by noah_zhang@asus.com; 13.3.5
//��ʾһ����ѹ�е�neon drwninfo
//302֮ǰ�İ汾����ѹ�У���Ȼʹ��PathDrawInfo��
public class NeonDrawInfo extends PathDrawInfo {
	private Path mPathleft;
	private Path mPathright;
	private Paint msecondPaint;
	private Paint mPaintLeft;
	private Paint mPaintRight;
	private float mdistance;
	private int mtoolType;
	public NeonDrawInfo(DrawTool drawTool, Paint usedPaint) {
		super(drawTool, usedPaint);
		// TODO Auto-generated constructor stub
		mPathleft = new Path();
		mPathright = new Path();
		msecondPaint = new Paint(usedPaint);
		mPaintLeft = new Paint(usedPaint);
		mPaintRight = new Paint(usedPaint);
		mdistance = usedPaint.getStrokeWidth() * 1 / 2;//noah
		resetStrokeWidth();
	}

	public NeonDrawInfo(DrawTool drawTool, Paint usedPaint, short[] points) {
		this(drawTool, usedPaint);
		initLists(points);
	}
	
	//add by mars for deal with float point 
	public NeonDrawInfo(DrawTool drawTool, Paint usedPaint, float[] points) {
		this(drawTool, usedPaint);
		initLists(points);
	}
	
	public Paint getSecontPaint() {
		return msecondPaint;
	}

	public Paint getLeftPaint() {
		return mPaintLeft;
	}

	public Paint getRightPaint() {
		return mPaintRight;
	}
	public void resetStrokeWidth()
	{
		msecondPaint.setStrokeWidth(mPaint.getStrokeWidth() * 2 / 5 );
		mPaintLeft.setStrokeWidth(mPaint.getStrokeWidth() * 2 / 5 );
		mPaintRight.setStrokeWidth(mPaint.getStrokeWidth() * 2 / 5 );
	}
	
	public Path getLeftPath() {
		return mPathleft;
	}

	public Path getRightPath() {
		return mPathright;
	}
	
	protected void initLists(short[] points) {
		int length = points.length;
		int index = 0;
		Point point;

		// Start Point
		point = new Point(points[index++], points[index++], points[index++] / MetaData.PRESSURE_FACTOR);
		mPointList.add(point);
		mPath.moveTo(point.x, point.y);
		mPathleft.moveTo(point.x, point.y);
		mPathright.moveTo(point.x, point.y);
		
		mEndX_right = mEndX_left = mEndX = point.x;
		mEndY_right = mEndY_left = mEndY = point.y;
		mControlX = point.x;
		mControlY = point.y;
		
		// Other Points
		while (index < length - 5) {
			point = new Point(points[index++], points[index++], points[index++] / MetaData.PRESSURE_FACTOR);
			addPoint(point.x,point.y,point.pressure);

		}

		// Last Point
		point = new Point(points[index++], points[index++], points[index++] / MetaData.PRESSURE_FACTOR);
		mPath.lineTo(point.x, point.y);
		mPathleft.lineTo(point.x, point.y);
		mPathright.lineTo(point.x, point.y);
		mPointList.add(point);
	}
	
	//add by mars for deal with float point 
	protected void initLists(float[] points) {
		int length = points.length;
		int index = 0;
		Point point;

		// Start Point
		point = new Point(points[index++], points[index++], points[index++] / MetaData.PRESSURE_FACTOR);
		mPointList.add(point);
		mPath.moveTo(point.x, point.y);
		mPathleft.moveTo(point.x, point.y);
		mPathright.moveTo(point.x, point.y);
		
		mEndX_right = mEndX_left = mEndX = point.x;
		mEndY_right = mEndY_left = mEndY = point.y;
		mControlX = point.x;
		mControlY = point.y;
		
		// Other Points
		while (index < length - 5) {
			point = new Point(points[index++], points[index++], points[index++] / MetaData.PRESSURE_FACTOR);
			addPoint(point.x,point.y,point.pressure);

		}

		// Last Point
		point = new Point(points[index++], points[index++], points[index++] / MetaData.PRESSURE_FACTOR);
		mPath.lineTo(point.x, point.y);
		mPathleft.lineTo(point.x, point.y);
		mPathright.lineTo(point.x, point.y);
		mPointList.add(point);
	}
	
	private float mEndX_left = 0;
	private float mEndY_left = 0;
	private float mEndX_right = 0;
	private float mEndY_right = 0;
	
	public void onDown(float[] x, float[] y, int toolType) {
		mPathleft.reset();
		mtoolType = toolType;
		mEndX_right = mEndX_left = mEndX = x[0];
		mEndY_right = mEndY_left = mEndY = y[0];
		mControlX = x[0];
		mControlY = y[0];
		mPath.moveTo(x[0], y[0]);
		mPathleft.moveTo(x[0], y[0]);
		mPathright.moveTo(x[0], y[0]);
		mPointList.add(new Point(x[0], y[0], 0f));
	}
	
	@Override
	public SerDrawInfo saveLock(NotePage note) {
		SerPointInfo serInfo = new SerPointInfo();
		serInfo.setColor(mPaint.getColor());
		serInfo.setStrokeWidth(mPaint.getStrokeWidth() );//* mScale
		serInfo.setPaintTool(mDrawTool.getToolCode());
		serInfo.setPoints(mPointList, true);
		return serInfo;
	}

	private static final float TOUCH_TOLERANCE = 1f;

	public void addPoint(float x,float y,float pressure)
	{
		float tempCtrlX = mEndX;
		float tempCtrlY = mEndY;
		float p = pressure;

		if (p > 1) {
			p = 1;
		} else if (p < 0) {
			p = 0;
		}
		float p_result = p;

		p = p * (float) 1.5 - 1;

		float dx = x - mEndX;
		float dy = y - mEndY;
		if (Math.abs(dx) >= TOUCH_TOLERANCE || Math.abs(dy) >= TOUCH_TOLERANCE) {

			if (p > 1) {
				p = 1;
			} else if (p < 0) {
				p = 0;
			}

			float xsub = mdistance * p;

			float xp1;// = x[0] + xsub;
			float yp1;// = y[0] - xsub;
			float xp2;// = x[0] - xsub;
			float yp2;// = y[0] + xsub;

			if ((dx < 0) && (dy < 0)) {
				xp1 = x - xsub;
				yp1 = y + xsub;
				xp2 = x + xsub;
				yp2 = y - xsub;
				Log.v("West", "1");
			} else if ((dx > 0) && (dy < 0)) {
				xp1 = x - xsub;
				yp1 = y - xsub;
				xp2 = x + xsub;
				yp2 = y + xsub;
				Log.v("West", "2");
			} else if ((dx < 0) && (dy > 0)) {
				xp1 = x + xsub;
				yp1 = y + xsub;
				xp2 = x - xsub;
				yp2 = y - xsub;
				Log.v("West", "3");
			} else if ((dx > 0) && (dy > 0)) {
				xp1 = x + xsub;
				yp1 = y - xsub;
				xp2 = x - xsub;
				yp2 = y + xsub;
				Log.v("West", "4");
			} else if (dx == 0) {
				if (dy < 0) {
					xp1 = x - xsub;
					yp1 = y;
					xp2 = x + xsub;
					yp2 = y;
					Log.v("West", "5");
				} else {
					xp1 = x + xsub;
					yp1 = y;
					xp2 = x - xsub;
					yp2 = y;
					Log.v("West", "6");
				}
			} else if (dy == 0) {
				if (dx > 0) {
					xp1 = x;
					yp1 = y - xsub;
					xp2 = x;
					yp2 = y + xsub;
					Log.v("West", "7");
				} else {
					xp1 = x;
					yp1 = y + xsub;
					xp2 = x;
					yp2 = y - xsub;
					Log.v("West", "8");
				}
			} else {
				Log.v("West", "9");
				return;
			}

			mPathleft.quadTo(mEndX_left, mEndY_left, (mEndX_left + xp1) / 2,
					(mEndY_left + yp1) / 2);

			mEndX_left = xp1;
			mEndY_left = yp1;

			mPathright.quadTo(mEndX_right, mEndY_right,
					(mEndX_right + xp2) / 2, (mEndY_right + yp2) / 2);

			mEndX_right = xp2;
			mEndY_right = yp2;
			// mPathright.quadTo();

			mPath.quadTo(tempCtrlX, tempCtrlY, (x + tempCtrlX) / 2,
					(y + tempCtrlY) / 2);
			mPointList.add(new Point(x, y, p_result));
			mEndX = x;
			mEndY = y;

			computeDirty(x, y);

			mControlX = tempCtrlX;
			mControlY = tempCtrlY;
		}		
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
	
	// @Override
	public void onMove(float[] x, float[] y, boolean multiPoint,
			float[] pressure) {
		if (mtoolType != MotionEvent.TOOL_TYPE_STYLUS || !MetaData.Switch_Neon_Pressure) {
			pressure[0] = 1f;
		}
		addPoint(x[0],y[0],pressure[0]);
	}

	@Override
	public void onUp(float[] x, float[] y) {
		mEndX = x[0];
		mEndY = y[0];
		mPath.lineTo(mEndX, mEndY);
		mPathleft.lineTo(mEndX, mEndY);
		mPathright.lineTo(mEndX, mEndY);
		mPointList.add(new Point(mEndX, mEndY, 0f));
	}

	@Override
	protected void transformLock(Matrix matrix) {
		mPath.transform(matrix);
		mPathleft.transform(matrix);
		mPathright.transform(matrix);
		mapPoints(matrix);
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
	
	public static short[] ConvertFromNoPressure(short[] pointsNoPressure){
		int pointCount = pointsNoPressure.length / 2;
		short[] pointsPressure = new short[pointCount * 3];
    	int j3 = 0;
    	int i3 = 0;
    	while (i3 < pointCount * 2 - 1) {
    		pointsPressure[j3++] = pointsNoPressure[i3++];
    		pointsPressure[j3++] = pointsNoPressure[i3++];
    		pointsPressure[j3++] = (short)(1.0f * MetaData.PRESSURE_FACTOR);
    	}
    	return pointsPressure;
	}
	
	public static float[] ConvertFromNoPressure(float[] pointsNoPressure){
		int pointCount = pointsNoPressure.length / 2;
		float[] pointsPressure = new float[pointCount * 3];
    	int j3 = 0;
    	int i3 = 0;
    	while (i3 < pointCount * 2 - 1) {
    		pointsPressure[j3++] = pointsNoPressure[i3++];
    		pointsPressure[j3++] = pointsNoPressure[i3++];
    		pointsPressure[j3++] = (1.0f * MetaData.PRESSURE_FACTOR);
    	}
    	return pointsPressure;
	}
	
	public static short[] ConvertFromPressure(short[] pointsPressure){
		int pointCount = pointsPressure.length / 3;
		short[] pointsNoPressure = new short[pointCount * 2];
    	int j = 0;
    	int i = 0;
    	while (i < pointCount * 3 - 1) {
    		pointsNoPressure[j++] = pointsPressure[i++];
    		pointsNoPressure[j++] = pointsPressure[i++];
    		i++;
    	}
    	return pointsNoPressure;
	}

}
