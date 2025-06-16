package com.asus.supernote.doodle.drawinfo;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem.SerDrawInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo.Point;
import com.asus.supernote.doodle.drawtool.DrawTool;

public abstract class TransformDrawInfo extends DrawInfo implements OnScaleGestureListener {
    public static final int CTR_LEFT_TOP = 0;
    public static final int CTR_RIGHT_TOP = 1;
    public static final int CTR_RIGHT_BOTTOM = 2;
    public static final int CTR_LEFT_BOTTOM = 3;
    public static final float TOUCH_TOLERANCE = 20f;
    protected static final int MOTION_NONE = 0;
    protected static final int MOTION_MOVE = 1;
    protected static final int MOTION_SCALE = 2;
    protected static final int MOTION_ROTATE = 3;
    protected static final int MOTION_ROTATE_AND_SCALE = 4;
    private static final int ESCAPE_BOUND = 50;
    private static final int CORNER_PADDING = 5;
    private static final float SNAP_ANGLE = 3;

    protected float mEndX, mEndY;
    protected float mOffsetX, mOffsetY;
    protected float mRotateAngle = 0;
    private boolean mSnapOn = false;
    protected int mMotion = MOTION_NONE;
    protected RectF mTransformBounds;
    protected Point mPole = null;
    // Control point
    // 0---------1
    // |.........|
    // |....x <------- mPole
    // |.........|
    // 3---------2 <-- rotate point
    protected Point[] mCorners = null;
    protected Matrix mTransMatrix = new Matrix();
    protected Matrix mGestureMatrix = new Matrix();
    private Matrix mTempMatrix= new Matrix();  //By Show

    public TransformDrawInfo(DrawTool drawTool, Paint usedPaint) {
        super(drawTool, usedPaint);
    }

    public void clone(TransformDrawInfo drawInfo) {
        if (mPole != null) {
            drawInfo.mPole = new Point(mPole);
        }
        if (mCorners != null) {
            int index = 0;
            drawInfo.mCorners = new Point[mCorners.length];
            for (Point point : mCorners) {
                drawInfo.mCorners[index++] = new Point(point);
            }
        }
    }

    @Override
    protected DrawInfo cloneLock() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RectF getBounds() {
        return getStrictBounds();
    }

    public Point[] getCorners() {
        return mCorners;
    }

    public Matrix getGestureMatrix() {
        return mGestureMatrix;
    }

    public Point getPole() {
        return mPole;
    }

    public float getRotateAngle() {
        return mRotateAngle;
    }

    @Override
    public RectF getStrictBounds() {
        float left;
        float right;
        float top;
        float bottom;

        if ((mPole == null) || (mCorners == null)) {
            return new RectF();
        }

        left = mPole.x;
        right = mPole.x;
        top = mPole.y;
        bottom = mPole.y;

        for (Point point : mCorners) {
            if (point.x < left) {
                left = point.x;
            }
            else if (point.x > right) {
                right = point.x;
            }
            if (point.y < top) {
                top = point.y;
            }
            else if (point.y > bottom) {
                bottom = point.y;
            }
        }
        return new RectF(left, top, right, bottom);
    }

    public Matrix getTransformMatrix() {
        return mTransMatrix;
    }

    protected void initControlPoints(float left, float top, float right, float bottom) {
        float centerX, centerY;
        float cornerLeft = left - CORNER_PADDING;
        float cornerTop = top - CORNER_PADDING;
        float cornerRight = right + CORNER_PADDING;
        float cornerBottom = bottom + CORNER_PADDING;

        centerX = cornerLeft + (Math.abs(cornerRight - cornerLeft) / 2);
        centerY = cornerTop + (Math.abs(cornerBottom - cornerTop) / 2);
        mPole = new Point(centerX, centerY);

        mCorners = new Point[4];
        mCorners[CTR_LEFT_TOP] = new Point(cornerLeft, cornerTop);
        mCorners[CTR_RIGHT_TOP] = new Point(cornerRight, cornerTop);
        mCorners[CTR_RIGHT_BOTTOM] = new Point(cornerRight, cornerBottom);
        mCorners[CTR_LEFT_BOTTOM] = new Point(cornerLeft, cornerBottom);

        mRotateAngle = 0;
    }

    protected void initControlPoints(RectF bounds) {
        initControlPoints(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    protected boolean isInScreen(float[] pts) {
        float left = pts[0];
        float top = pts[1];
        float right = pts[0];
        float bottom = pts[1];

        for (int i = 1; i < 4; i++) {
            if (pts[2 * i] < left) {
                left = pts[2 * i];
            }
            else if (pts[2 * i] > right) {
                right = pts[2 * i];
            }
            if (pts[(2 * i) + 1] < top) {
                top = pts[(2 * i) + 1];
            }
            else if (pts[(2 * i) + 1] > bottom) {
                bottom = pts[(2 * i) + 1];
            }
        }

        left += mOffsetX;
        top += mOffsetY;
        right += mOffsetX;
        bottom += mOffsetY;
        if (((bottom - mTransformBounds.top) < ESCAPE_BOUND)
                || ((mTransformBounds.bottom - top) < ESCAPE_BOUND)
                || ((right - mTransformBounds.left) < ESCAPE_BOUND)
                || ((mTransformBounds.right - left) < ESCAPE_BOUND)) {
            return false;
        }
        return true;
    }

    protected boolean mapPoints(Matrix matrix, boolean restrict) {
        float[] pts = new float[10];
        int index = 0;

        if ((mCorners == null) || (mPole == null)) {
            return false;
        }

        for (Point point : mCorners) {
            if (point != null) {
                pts[index++] = point.x;
                pts[index++] = point.y;
            }
        }

        pts[index++] = mPole.x;
        pts[index++] = mPole.y;

        matrix.mapPoints(pts);

        if (restrict && (mTransformBounds != null)) {
            float ctrButtonDistance = (float) Math.sqrt(Math.pow(pts[2] - pts[4], 2)
                    + Math.pow(pts[3] - pts[5], 2));

            // Avoid objects become too small
            if (ctrButtonDistance < (TOUCH_TOLERANCE * 2)) {
                return false;
            }

            // Avoid objects escape from the screen
            if (!isInScreen(pts)) {
                return false;
            }
        }

        index = 0;
        for (Point point : mCorners) {
            point.x = pts[index++];
            point.y = pts[index++];
        }

        mPole.x = pts[index++];
        mPole.y = pts[index++];
        return true;
    }

    @Override
    public void onDown(float[] x, float[] y) {
        mEndX = x[0];
        mEndY = y[0];
        
        float rotate_touch_tolerance = TOUCH_TOLERANCE * MetaData.DPI / MetaData.BASE_DPI; //smilefish fix bug 752790
        if ((mCorners != null) && (Math.abs(x[0] - mCorners[CTR_RIGHT_BOTTOM].x) < rotate_touch_tolerance) 
        		&& (Math.abs(y[0] - mCorners[CTR_RIGHT_BOTTOM].y) < rotate_touch_tolerance)) {
            mMotion = MOTION_ROTATE_AND_SCALE;
        }

    }

    // We need to make effect to Corners immediately because
    // 1. They are control point
    // 2. We need to draw them at correct place immediately
    @Override
    public void onMove(MotionEvent event, float[] x, float[] y, boolean multiPoint) {
//    	if(misTemplate) return;//wendy allen++
        Matrix matrix = new Matrix();

        if (multiPoint) {
            return;
        }

        if (mMotion == MOTION_ROTATE_AND_SCALE) {
            float vectorOriX, vectorOriY;
            float vectorMoveX, vectorMoveY;
            float px, py;
            float crossProduct;
            float lengthOri, lengthMove;
            float cos;
            float scaleX = 1, scaleY = 1;
            float degree;

            px = mPole == null ? 0 : mPole.x;
            py = mPole == null ? 0 : mPole.y;

            //emmanual to fix bug 493279
            if(mCorners == null){
            	return ;
            }
            vectorOriX = mCorners[CTR_RIGHT_BOTTOM].x - px;
            vectorOriY = mCorners[CTR_RIGHT_BOTTOM].y - py;
            vectorMoveX = x[0] - px;
            vectorMoveY = y[0] - py;
            lengthOri = (float) Math.sqrt((vectorOriX * vectorOriX) + (vectorOriY * vectorOriY));
            lengthMove = (float) Math.sqrt((vectorMoveX * vectorMoveX) + (vectorMoveY * vectorMoveY));

            // Compute the angle of rotation
            cos = ((vectorOriX * vectorMoveX) + (vectorOriY * vectorMoveY)) / (lengthOri * lengthMove);
            cos = cos > 1f ? 1 : cos;
            cos = cos < -1f ? -1 : cos;
            degree = (float) Math.toDegrees(Math.acos(cos));

            // Scaling
            scaleX = lengthMove / lengthOri;
            scaleY = lengthMove / lengthOri;

            if ((scaleX > 0) && (scaleY > 0)) {
                if (mPole == null) {
                    matrix.postScale(scaleX, scaleY);
                }
                else {
                    matrix.postScale(scaleX, scaleY, mPole.x, mPole.y);
                }
            }

            // Rotating
            // Get the orientation of rotation
            crossProduct = (vectorOriX * vectorMoveY) - (vectorOriY * vectorMoveX);
            degree = crossProduct > 0 ? degree : degree * -1;

            if (mSnapOn) {
                if (Math.abs(degree) > SNAP_ANGLE) {
                    mSnapOn = false;
                }
            }
            else {
                float testAngle = Math.abs((mRotateAngle + degree) % 90);
                if (testAngle > (90 - SNAP_ANGLE)) {
                    degree += degree > 0 ? 90 - testAngle : testAngle - 90;
                    matrix.postRotate(degree, px, py);
                    mRotateAngle = ((mRotateAngle + degree) % 360);
                    mSnapOn = true;
                }
                else if (testAngle < SNAP_ANGLE) {
                    degree += degree > 0 ? testAngle : 0 - testAngle;
                    matrix.postRotate(degree, px, py);
                    mRotateAngle = ((mRotateAngle + degree) % 360);
                    mSnapOn = true;
                }
                else {
                    matrix.postRotate(degree, px, py);
                    mRotateAngle = ((mRotateAngle + degree) % 360);
                }
            }
        }
        else {
            mMotion = MOTION_MOVE;
            // Translating
            float diffX = 0;
            float diffY = 0;
            diffX = x[0] - mEndX;
            diffY = y[0] - mEndY;
            mEndX = x[0];
            mEndY = y[0];
            matrix.postTranslate(diffX, diffY);
        }
        if (mapPoints(matrix, true)) {
            mTransMatrix.postConcat(matrix);
            mGestureMatrix.postConcat(matrix);
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
//    	if(misTemplate) return true;//wendy allen++
        Matrix matrix = new Matrix();
        float scale = detector.getScaleFactor();
        if (mPole == null) {
            matrix.postScale(scale, scale);
        }
        else {
            matrix.postScale(scale, scale, mPole.x, mPole.y);
        }
        if (mapPoints(matrix, true)) {
            mTransMatrix.postConcat(matrix);
            mGestureMatrix.postConcat(matrix);
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        if (mMotion == MOTION_ROTATE_AND_SCALE) {
            return false;
        }
        mMotion = MOTION_SCALE;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    @Override
    public void onUp(float[] x, float[] y) {
        mMotion = MOTION_NONE;
    }

    @Override
    protected SerDrawInfo saveLock(NotePage note) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setOffset(float offsetX, float offsetY) {
        mOffsetX = offsetX;
        mOffsetY = offsetY;
    }

    public void setTransformBounds(RectF bounds) {
        mTransformBounds = bounds;
    }

    @Override
    protected void transformLock(Matrix matrix) {
        float[] values = new float[9];

        mapPoints(matrix, false);
        mTransMatrix.postConcat(matrix);
        matrix.getValues(values);
        mRotateAngle = (float) ((mRotateAngle + Math.toDegrees(Math.atan(values[Matrix.MSKEW_Y] / values[Matrix.MSCALE_Y]))) % 360);
        mTempMatrix.set(mTransMatrix);  //By Show

    }
    
	//BEGIN:Show
    public Matrix getMatrix(){
    	return mTempMatrix ;
    }
	//END:Show


}
