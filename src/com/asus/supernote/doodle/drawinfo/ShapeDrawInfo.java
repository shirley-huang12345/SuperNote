package com.asus.supernote.doodle.drawinfo;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem.SerDrawInfo;
import com.asus.supernote.doodle.DoodleItem.SerPointInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo.Point;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawtool.MyPoint;
import com.asus.supernote.doodle.drawtool.ShapeDrawTool;
import com.visionobjects.myscript.shape.ShapeEllipticArcData;
import com.visionobjects.myscript.shape.ShapePointData;

public class ShapeDrawInfo extends DrawInfo {

    // index of point coordinate
    public static final short START_A = 0;
    public static final short END_C = 1;
    public static final short SQUINCH_B = 2;
    public static final short SQUINCH_D = 3;
    protected Path mPath = new Path();
    protected Point[] mPoints = new Point[4];
    protected float[] mArcInfo = new float[7];//RICHARD

    public ShapeDrawInfo(DrawTool drawTool, Paint usedPaint) {
        super(drawTool, usedPaint);
        for (int i = 0; i < mPoints.length; i++) {
            mPoints[i] = new Point();
        }
    }

    public ShapeDrawInfo(DrawTool drawTool, Paint usedPaint, short[] points) {
        this(drawTool, usedPaint);

        int length = points.length;

        if (length < 2) {
            return;
        }
        else if (length < 8) {
            mPoints[START_A].x = points[0];
            mPoints[START_A].y = points[1];

            mPoints[END_C].x = points[2];
            mPoints[END_C].y = points[3];
            computeSquinch();
        }
        else {
            for (int i = 0; i < 4; i++) {
                mPoints[i] = new Point(points[2 * i], points[(2 * i) + 1]);
            }
        }

        drawPath();//RICHARD
    }
    
    //BEGIN: RICHARD
    public ShapeDrawInfo(DrawTool drawTool, Paint usedPaint, short[] points, float[] info) {
        this(drawTool, usedPaint);

        int length = points.length;

        if (length < 2) {
            return;
        }
        else if (length < 8) {
            mPoints[START_A].x = points[0];
            mPoints[START_A].y = points[1];

            mPoints[END_C].x = points[2];
            mPoints[END_C].y = points[3];
            computeSquinch();
        }
        else {
            for (int i = 0; i < 4; i++) {
                mPoints[i] = new Point(points[2 * i], points[(2 * i) + 1]);
            }
        }
        
        mArcInfo = info;

        mPath.reset();
        drawPath();//RICHARD
    }
    //END: RICHARD
    
    //BEGIN: RICHARD
    private void drawPath()
    {
    	if(mDrawTool.getToolCode() == DrawTool.ARC_TOOL)
    	{
        	MyPoint mp0 = new MyPoint(mPoints[0].x,mPoints[0].y);
        	MyPoint mp1 = new MyPoint(mPoints[1].x,mPoints[1].y);
        	MyPoint mp2 = new MyPoint(mPoints[2].x,mPoints[2].y);
        	MyPoint mp3 = new MyPoint(mPoints[3].x,mPoints[3].y);
        	
        	
        	double addRotate = 0;
        	MyPoint vectorNow = mp1.getDistance(mp0);
        	double cosAddRotate =	vectorNow.getCosTwoPoint(new MyPoint(1,0));
        	addRotate =  Math.acos(cosAddRotate);
        	if(!vectorNow.getDirect(new MyPoint(1,0)))
        	{
        		addRotate = 0 -addRotate;
        	}
        	addRotate = Math.toDegrees(addRotate);
        	
            float arcInfo[] =  mArcInfo;
        	float centerPointX = (mPoints[0].x + mPoints[1].x + mPoints[2].x + mPoints[3].x)/4;//arcInfo[0];
        	float centerPointY = (mPoints[0].y + mPoints[1].y + mPoints[2].y + mPoints[3].y)/4;//arcInfo[1];
        	
        	float maxRadius = (float)(mp1.getDistance(mp0)).getAbs()/2;
        	float minRadius = (float)(mp3.getDistance(mp0)).getAbs()/2;
        	
        	float beginArg = arcInfo[4];
        	float sweepArg = arcInfo[5];

        	RectF oval = new RectF(centerPointX - maxRadius,centerPointY - minRadius,centerPointX + maxRadius,centerPointY + minRadius);
        	mPath.addArc(oval, beginArg, sweepArg);
        	RectF bounds = new RectF();
        	mPath.computeBounds(bounds, false);
        	Matrix matrix = new Matrix();
        	matrix.postRotate((float)-addRotate,centerPointX,centerPointY);
        	mPath.transform(matrix);
        	
        	mPath.computeBounds(bounds, false);
        	
        	int i = 0;
        	i++;
    	}
    	else
    	{
            mPath.moveTo(mPoints[START_A].x, mPoints[START_A].y);
            mPath.lineTo(mPoints[END_C].x, mPoints[END_C].y);
    	}
    }
    //END: RICHARD
    
    //BEGIN: RICHARD    
    public ShapeDrawInfo(DrawTool drawTool, Paint usedPaint,ShapeEllipticArcData data) {
		// TODO Auto-generated constructor stub
    	this(drawTool, usedPaint);
    	
		final ShapePointData center = data.getCenter();

		short maxR = (short)data.getMaxRadius();
		short minR = (short)data.getMinRadius();
		
		float left = (short)center.getX() -   (short)maxR;//data.getMaxRadius();
		float right = (short)center.getX() +  (short)maxR;//data.getMaxRadius();
		float top = (short)center.getY() -    (short)minR;//data.getMinRadius();
		float bottom = (short)center.getY() + (short)minR;//data.getMinRadius();

		float[] pts = new float[8];
		pts[0] = -maxR;
		pts[1] = minR;
		
		pts[2] = maxR;
		pts[3] = minR;
		
		pts[4] = maxR;
		pts[5] = -minR;
		
		pts[6] = -maxR;
		pts[7] = -minR;
		
		Matrix matrix = new Matrix();
		matrix.setRotate((float)Math.toDegrees(data.getOrientation()), 0, 0);
		matrix.mapPoints(pts);
		
		pts[0] += (short)center.getX();
		pts[1] += (short)center.getY();
		
		pts[2] += (short)center.getX();
		pts[3] += (short)center.getY();
		
		pts[4] += (short)center.getX();
		pts[5] += (short)center.getY();
		
		pts[6] += (short)center.getX();
		pts[7] += (short)center.getY();
		
        for (int i = 0; i < 4; i++) {
            mPoints[i] = new Point(pts[2 * i], pts[(2 * i) + 1]);
        }
		
		float[] info = new float[7];
		info[0] = (short)center.getX();
		info[1] = (short)center.getY();
		info[2] = maxR;//data.getMaxRadius();
		info[3] = minR;//data.getMinRadius();
		
		info[4] = (float)Math.toDegrees(data.getStartAngle());
		info[5] = (float)Math.toDegrees(data.getSweepAngle());
		info[6] = (float)Math.toDegrees(data.getOrientation());
		
		mArcInfo = info;
        drawPath();//RICHARD
        
	}
	//END: RICHARD

    @Override
    public DrawInfo cloneLock() {
        int index = 0;
        ShapeDrawInfo drawInfo = (ShapeDrawInfo) (new ShapeDrawTool(getDrawTool().getToolCode())).getDrawInfo(mPaint);
        drawInfo.mPath = new Path(mPath);

        for (Point point : mPoints) {
            drawInfo.mPoints[index++] = new Point(point);
        }
        return drawInfo;
    }

    private void computeSquinch() {
        mPoints[SQUINCH_B].x = mPoints[END_C].x;
        mPoints[SQUINCH_B].y = mPoints[START_A].y;
        mPoints[SQUINCH_D].x = mPoints[START_A].x;
        mPoints[SQUINCH_D].y = mPoints[END_C].y;
    }

    @Override
    public RectF getBounds() {
        RectF bounds = addStrokeToBounds(getStrictBounds());
        return bounds;
    }

    public Path getPath() {
        return mPath;
    }

    public Point[] getPoints() {
        return mPoints;
    }
    
    //BEGIN: RICHARD
    public float[] getArcInfo()
    {
    	return mArcInfo;
    }
    //END: RICHARD

    @Override
    public RectF getStrictBounds() {
        float left, top, right, bottom;
        RectF bounds;
        left = mPoints[0].x;
        right = mPoints[0].x;
        top = mPoints[0].y;
        bottom = mPoints[0].y;

        for (int i = 1; i < mPoints.length; i++) {
            if (mPoints[i].x < left) {
                left = mPoints[i].x;
            }
            else if (mPoints[i].x > right) {
                right = mPoints[i].x;
            }

            if (mPoints[i].y < top) {
                top = mPoints[i].y;
            }
            else if (mPoints[i].y > bottom) {
                bottom = mPoints[i].y;
            }
        }

        bounds = new RectF(left, top, right, bottom);
        return bounds;
    }

    private void mapPoints(Matrix matrix) {
        float[] pts = new float[8];

        int index = 0;
        for (Point point : mPoints) {
            pts[index++] = point.x;
            pts[index++] = point.y;
        }

        matrix.mapPoints(pts);

        index = 0;
        for (Point point : mPoints) {
            point.x = pts[index++];
            point.y = pts[index++];
        }
    }

    @Override
    public void onDown(float[] x, float[] y) {
        mPoints[START_A].x = x[0];
        mPoints[START_A].y = y[0];
        mPoints[END_C].x = x[0];
        mPoints[END_C].y = y[0];
        computeSquinch();
    }

    @Override
    public void onMove(MotionEvent event, float[] x, float[] y, boolean multiPoint) {
        mPoints[END_C].x = x[0];
        mPoints[END_C].y = y[0];

        mPath.reset();
        drawPath();//RICHARD
        computeSquinch();
    }

    @Override
    public void onUp(float[] x, float[] y) {
        mPoints[END_C].x = x[0];
        mPoints[END_C].y = y[0];
        mPath.reset();
        drawPath();//RICHARD
        computeSquinch();
    }

    @Override
    public SerDrawInfo saveLock(NotePage note) {
        SerPointInfo serInfo = new SerPointInfo();
        serInfo.setColor(mPaint.getColor());
        serInfo.setStrokeWidth(mPaint.getStrokeWidth());
        serInfo.setPaintTool(mDrawTool.getToolCode());
        serInfo.setPoints(mPoints);

        if(mDrawTool.getToolCode() == DrawTool.ARC_TOOL)
        {
        	serInfo.setExtraInfo(mArcInfo);
        }
        
        return serInfo;
    }

    @Override
    protected void transformLock(Matrix matrix) {
        mPath.transform(matrix);
        mapPoints(matrix);
    }
}
