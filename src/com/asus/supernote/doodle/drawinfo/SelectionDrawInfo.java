package com.asus.supernote.doodle.drawinfo;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.asus.supernote.R;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem.SerDrawInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo.Point;
import com.asus.supernote.doodle.drawtool.AnnotationDrawTool;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawtool.SelectionDrawTool;

public class SelectionDrawInfo extends TransformDrawInfo {
    public interface OnDeleteListener {
        public void onDelete(Collection<DrawInfo> drawInfos);
    }

    public static  float TOUCH_TOLERANCE = 30f ;//darwin
    private static  float CLICK_TOLERANCE = 20f ;//darwin
    private ShapeDrawInfo mShapeDrawInfo;
    private LinkedHashMap<Integer, DrawInfo> mSelections = new LinkedHashMap<Integer, DrawInfo>();
    private OnDeleteListener mListener;
    private Matrix mCacheMatrix = new Matrix();

    private boolean mSomethingSelected = false;

    public SelectionDrawInfo(DrawTool drawTool, Paint usedPaint,Context context) {
        super(drawTool, usedPaint);
        //Begin Siupo
        TOUCH_TOLERANCE =  context.getResources().getDimension(R.dimen.touch_tolerance);
        CLICK_TOLERANCE = context.getResources().getDimension(R.dimen.click_tolerance);
        //End Siupo
    }

    public void addSelection(int location, Collection<DrawInfo> drawInfos) {
        if (drawInfos == null) {
            return;
        }

        for (DrawInfo drawInfo : drawInfos) {
            mSelections.put(location, drawInfo);
            location++;
        }

        initControlPoints(getSelectBounds());
    }

    public void addSelection(int location, DrawInfo drawInfo) {
        if (drawInfo == null) {
            return;
        }

        mSelections.put(location, drawInfo);

        initControlPoints(getSelectBounds());
    }

    public void addSelection(Map<? extends Integer, ? extends DrawInfo> map) {
        if (map == null) {
            return;
        }
        mSelections.putAll(map);

        initControlPoints(getSelectBounds());
    }

    public void clearSelection() {
        mSelections.clear();
        mPole = null;
        mCorners = null;
    }

    public void clearSelectionFrame() {
        mShapeDrawInfo = null;
    }

    @Override
    public DrawInfo cloneLock() {
        Context context = ((SelectionDrawTool) getDrawTool()).getContext();
        SelectionDrawInfo drawInfo = (SelectionDrawInfo) (new SelectionDrawTool(context)).getDrawInfo(mPaint);
        Set<Integer> keys = mSelections.keySet();
        for (Integer i : keys) {
            DrawInfo child = mSelections.get(i).clone();
            if (child != null) {
                drawInfo.mSelections.put(i, child);
            }
        }
        drawInfo.mListener = mListener;
        drawInfo.mSomethingSelected = mSomethingSelected;
        clone(drawInfo);
        return drawInfo;
    }

    public boolean containGroup() {
        Collection<DrawInfo> infos = mSelections.values();
        for (DrawInfo info : infos) {
            if (info instanceof AnnotationDrawInfo) {
                return true;
            }
        }
        return false;
    }

    public void delete() {
        if (mListener != null) {
            mListener.onDelete(getSelectedObjects());
        }
    }

    @Override
    public RectF getBounds() {
        RectF bounds = addStrokeToBounds(getStrictBounds());
        return bounds;
    }

    //darwin
    public RectF getBoundsForClipboard()
    {
    	RectF bounds = super.getStrictBounds();
        return bounds;
    }
    //darwin
    
    public Matrix getCacheMatrix() {
        return mCacheMatrix;
    }

    private RectF getSelectBounds() {
        RectF unionBounds = new RectF();
        Collection<DrawInfo> selectedInfos;

        selectedInfos = mSelections.values();
        for (DrawInfo info : selectedInfos) {
        	if (info != null) {
        		unionBounds.union(info.getBounds());
        	}
        }

        return unionBounds;
    }

    public Collection<DrawInfo> getSelectedObjects() {
        LinkedList<DrawInfo> selectedObjects = new LinkedList<DrawInfo>();
        selectedObjects.addAll(mSelections.values());
        return selectedObjects;
    }

    public RectF getSelectionFrame() {
        if (mShapeDrawInfo != null) {
            return mShapeDrawInfo.getStrictBounds();
        }
        return new RectF();
    }

    public LinkedHashMap<Integer, DrawInfo> getSelections() {
        return mSelections;
    }

    @Override
    public RectF getStrictBounds() {
        if ((mCorners != null) && (mPole != null)) {
            // Add TOUCH_TOLERANCE to make selecting easily
            RectF bounds = super.getStrictBounds();
            bounds.left -= TOUCH_TOLERANCE;
            bounds.top -= TOUCH_TOLERANCE;
            bounds.right += TOUCH_TOLERANCE;
            bounds.bottom += TOUCH_TOLERANCE;
            return bounds;
        }
        return new RectF();
    }

    public DrawInfo group() {
        AnnotationDrawInfo drawInfo;
        unGroup();
        if (mSelections.size() > 0) {
            drawInfo = new AnnotationDrawInfo(new AnnotationDrawTool(), mPaint);
            drawInfo.add(0, mSelections, true);
            mSelections.clear();
            mSelections.put(0, drawInfo);
            return drawInfo;
        }
        else {
            return null;
        }
    }

    @Override
    protected void initControlPoints(RectF bounds) {
        super.initControlPoints(bounds);
        if (mSelections.size() == 1) {
            DrawInfo selected = mSelections.values().iterator().next();
            if (selected instanceof TransformDrawInfo) {
                Point[] corners = ((TransformDrawInfo) selected).getCorners();
                int index = 0;
                for (Point point : corners) {
                    mCorners[index++] = new Point(point);
                }
                mRotateAngle = ((TransformDrawInfo) selected).getRotateAngle();
            }
        }
        ((SelectionDrawTool) getDrawTool()).setCacheDirty(true);
    }

    public boolean isGroup() {
        if ((mSelections.size() == 1) && (mSelections.values().iterator().next() instanceof AnnotationDrawInfo)) {
            return true;
        }
        return false;
    }

    public boolean isMultiSelect() {
        if (mSelections.size() > 1) {
            return true;
        }
        return false;
    }

    public boolean isSomethingSelected() {
        return mSomethingSelected;
    }

    @Override
    public boolean isTouched(float x, float y) {
        RectF unionBounds = getStrictBounds();

        if (unionBounds.contains(x, y)) {
            mSomethingSelected = true;
            return true;
        }
        mSomethingSelected = false;
        return false;
    }

    // Control point
    // 0---------1 <-- delete point
    // |.........|
    // |....x <------- mPole
    // |.........|
    // 3---------2 <-- rotate point
    @Override
    public void onDown(float[] x, float[] y) {
        super.onDown(x, y);
        mShapeDrawInfo = new ShapeDrawInfo(mDrawTool, mPaint);
        mShapeDrawInfo.onDown(x, y);

        if ((mCorners != null) && (Math.abs(x[0] - mCorners[CTR_RIGHT_TOP].x) < TOUCH_TOLERANCE) && (Math.abs(y[0] - mCorners[CTR_RIGHT_TOP].y) < TOUCH_TOLERANCE)) {
            delete();
        }
    }

    @Override
    public void onMove(MotionEvent event, float[] x, float[] y, boolean multiPoint) {
        if (mSomethingSelected) {
            super.onMove(event, x, y, multiPoint);
            transformSelections();
        }
        else {
            if (mShapeDrawInfo != null) {
                mShapeDrawInfo.onMove(event, x, y, multiPoint);
            }
        }
    }

    @Override
    public void onUp(float[] x, float[] y) {
        if (mSomethingSelected) {
            super.onUp(x, y);
        }
        else {
            if (mShapeDrawInfo != null) {
                mShapeDrawInfo.onUp(x, y);
            }
        }
    }

    public void register(OnDeleteListener listener) {
        mListener = listener;
    }

    public void resetCache() {
        mCacheMatrix.reset();
    }

    @Override
    public SerDrawInfo saveLock(NotePage note) {
        return null;
    }

    public Collection<DrawInfo> select(int cacheWidth, int cacheHeight, LinkedList<DrawInfo> histList, int offsetX, int offsetY) {
        int histSize = histList.size();
        int index = 0;
        boolean isClick = false;
        RectF selectionBounds;
        RectF testBounds = new RectF();
        RectF unionBounds = new RectF();

        selectionBounds = getSelectionFrame();
        selectionBounds.offset(offsetX, offsetY);

        isClick = (selectionBounds.width() <= CLICK_TOLERANCE) && (selectionBounds.height() <= CLICK_TOLERANCE);

        if (!isClick) {
            if ((selectionBounds.left - TOUCH_TOLERANCE) <= mTransformBounds.left) {
                selectionBounds.left -= mTransformBounds.width();
            }
            if ((selectionBounds.top - TOUCH_TOLERANCE) <= mTransformBounds.top) {
                selectionBounds.top -= mTransformBounds.height();
            }
            if ((selectionBounds.right + TOUCH_TOLERANCE) >= mTransformBounds.right) {
                selectionBounds.right += mTransformBounds.width();
            }
            if ((selectionBounds.bottom + TOUCH_TOLERANCE) >= mTransformBounds.bottom) {
                selectionBounds.bottom += mTransformBounds.height();
            }
        }

        // Find all selected object
        // 1. If the object is a instance of EraseDrawInfo, we just need the
        // copy of selected subset
        // 2. Other objects is selected only if the selection region contains
        // its bounds
        // 3. all selected object need to translate to screen coordinate
        Matrix tranMatrix = new Matrix();
        tranMatrix.postTranslate(-offsetX, -offsetY);

        while (index < histSize) {
            DrawInfo d = histList.get(index);
            if ((d == null) || (d instanceof EraseDrawInfo)) {
                index++;
                continue;
            }
            testBounds = d.getStrictBounds();
            if (isClick) {
                if (d.isTouched(selectionBounds.centerX(), selectionBounds.centerY())) {
                    d.transform(tranMatrix);
                    testBounds = d.getBounds();
                    mSelections.put(index, d);
                    unionBounds.union(testBounds);
                    break;
                }
            }
            else if (selectionBounds.contains(testBounds)) {
                d.transform(tranMatrix);
                testBounds = d.getBounds();
                mSelections.put(index, d);
                unionBounds.union(testBounds);
            }
            index++;
        }

        if (!unionBounds.isEmpty()) {
            initControlPoints(unionBounds);
        }

        return mSelections.values();
    }

    @Override
    protected void transformLock(Matrix matrix) {
        super.transformLock(matrix);
        transformSelections();
    }

    private void transformSelections() {
        Collection<DrawInfo> drawInfos = mSelections.values();
        for (DrawInfo drawInfo : drawInfos) {
            drawInfo.transform(mTransMatrix);
        }
        mCacheMatrix.postConcat(mTransMatrix);
        // reset scale & translate because they has made effect
        mTransMatrix.reset();
    }

    public Collection<DrawInfo> unGroup() {
        LinkedHashMap<Integer, DrawInfo> drawInfos;
        LinkedList<DrawInfo> infoList;
        if (mSelections.size() > 0) {
            int index = 0;
            drawInfos = new LinkedHashMap<Integer, DrawInfo>();
            infoList = new LinkedList<DrawInfo>();
            
            TreeSet<Integer> ts = new TreeSet<Integer>(new Comparator(){
          		 public int compare(Object o1,Object o2){
          		 Integer i1 = (Integer)o1;
          		 Integer i2 = (Integer)o2;
          		 return i2.intValue() - i1.intValue();
          		 }
          		 });
              	ts.addAll(mSelections.keySet());
              	Set<Integer> keys = ts;
              	
              	
            for (Integer key : keys) {
                DrawInfo drawInfo = mSelections.get(key);
                if (drawInfo instanceof AnnotationDrawInfo) {
                    infoList.addAll(((AnnotationDrawInfo) drawInfo).unGroup());
                }
                else {
                    infoList.add(drawInfo);
                }
            }
            index = infoList.size() - 1;
            for (DrawInfo info : infoList) {
                drawInfos.put(index--, info);
            }

            mSelections.clear();
            mSelections.putAll(drawInfos);

            drawInfos = null;

            return mSelections.values();
        }
        else {
            return null;
        }
    }
}
