package com.asus.supernote.doodle.drawinfo;

import java.util.LinkedList;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem.SerDrawInfo;
import com.asus.supernote.doodle.drawtool.DrawTool;

public abstract class DrawInfo {
    protected DrawTool mDrawTool;
    protected Paint mPaint;
    protected Rect mDirtyRect = null;
    private LinkedList<EraseDrawInfo> mEraseInfos = null;
    private short mPageVersion;

    public short getPageVersion() {
		return mPageVersion;
	}

	public void setPageVersion(short pageVersion) {
		this.mPageVersion = pageVersion;
	}
    
    public DrawInfo(DrawTool drawTool, Paint usedPaint) {
        mDrawTool = drawTool;
        if (usedPaint == null) {
            mPaint = new Paint();
        }
        else {
            mPaint = new Paint(usedPaint);
        }
    }
    //darwin
    private boolean mIsModifyMatrix = false;
    private float mScale = 0;
    public boolean getModifyMatrix()
    {
    	return mIsModifyMatrix;
    }
    public void setModifyMatrix(boolean isModify)
    {
    	mIsModifyMatrix = isModify;
    }
    public float getScale()
    {
    	return mScale;
    }
    public void setScale(float scale)
    {
    	mScale = scale;
    }
    //darwin
    // If we add the eraseInfo to the DrawInfo, return true
    // Else return false
    public boolean add(EraseDrawInfo eraseInfo, boolean isSubset) {
        int index;
        EraseDrawInfo subset;

        if (mEraseInfos == null) {
            mEraseInfos = new LinkedList<EraseDrawInfo>();
        }

        index = mEraseInfos.indexOf(eraseInfo);

        if (index != -1) {
            mEraseInfos.get(index).setVisible(true);
        }
        else {
            if (isSubset) {
                mEraseInfos.add(eraseInfo);
            }
            else {
                subset = eraseInfo.getSubset(getBounds());
                if (subset == null) {
                    return false;
                }
                mEraseInfos.add(subset);
            }
            return true;
        }
        return false;
    }

    protected RectF addStrokeToBounds(RectF bounds) {
        float strokeWidth = mPaint.getStrokeWidth();
        RectF result = new RectF(bounds);
        result.left -= strokeWidth;
        result.top -= strokeWidth;
        result.right += strokeWidth;
        result.bottom += strokeWidth;
        return result;
    }

    @Override
    public DrawInfo clone() {
        DrawInfo drawInfo;

        drawInfo = cloneLock();

        if ((drawInfo != null) && (mEraseInfos != null) && (mEraseInfos.size() > 0)) {
            for (EraseDrawInfo eraseInfo : mEraseInfos) {
                drawInfo.add((EraseDrawInfo) eraseInfo.clone(), true);
            }
        }
        return drawInfo;
    }

    abstract protected DrawInfo cloneLock();

    abstract public RectF getBounds();

    public Rect getDirty() {
        return mDirtyRect;
    }

    public DrawTool getDrawTool() {
        return mDrawTool;
    }

    public LinkedList<EraseDrawInfo> getEraseDrawInfos() {
        return mEraseInfos;
    }

    public Paint getPaint() {
        return mPaint;
    }

    abstract public RectF getStrictBounds();

    public boolean isTouched(float x, float y) {
        return getBounds().contains(x, y);
    }

    abstract public void onDown(float[] x, float[] y);

    abstract public void onMove(MotionEvent event, float[] x, float[] y, boolean multiPoint);

    abstract public void onUp(float[] x, float[] y);

    public void remove(EraseDrawInfo eraseInfo) {
        if (mEraseInfos == null) {
            return;
        }
        int index = mEraseInfos.indexOf(eraseInfo);
        if (index != -1) {
            mEraseInfos.get(index).setVisible(false);
        }
    }

    public void resetDirty() {
        mDirtyRect = null;
    }

    public SerDrawInfo save(NotePage note) {
        LinkedList<SerDrawInfo> serEraseInfos;
        SerDrawInfo serDrawInfo = saveLock(note);

        if (mEraseInfos != null) {
            serEraseInfos = new LinkedList<SerDrawInfo>();
            for (EraseDrawInfo eraseInfo : mEraseInfos) {
                if (eraseInfo.isVisible()) {
                    serEraseInfos.add(eraseInfo.save(note));
                }
            }

            serDrawInfo.setEraseInfos(serEraseInfos);
        }
        return serDrawInfo;
    }

    abstract protected SerDrawInfo saveLock(NotePage note);

    public void setTransform(Matrix matrix) {
        if (matrix != null) {
            transform(matrix);
        }
    }

    public void transform(Matrix matrix) {
    	//emmanual to fix bug 384581,384581,384636,384636
    	if(matrix == null){
    		return ;
    	}
        float[] matrixValue = new float[9];
        float scaleX;

        if (mPaint != null) {
            matrix.getValues(matrixValue);
            scaleX = (float) Math.sqrt(Math.pow(matrixValue[Matrix.MSCALE_X], 2) + Math.pow(matrixValue[Matrix.MSKEW_X], 2));
            mPaint.setStrokeWidth(mPaint.getStrokeWidth() * scaleX);
        }

        transformLock(matrix);

        if (mEraseInfos != null) {
            for (EraseDrawInfo eraseInfo : mEraseInfos) {
                eraseInfo.transform(matrix);
            }
        }
    }

    abstract protected void transformLock(Matrix matrix);
}
