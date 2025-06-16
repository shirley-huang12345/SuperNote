package com.asus.supernote.editable.noteitem;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;

import com.asus.supernote.PaintSelector;
import com.asus.supernote.data.AsusFormat;
import com.asus.supernote.data.AsusFormatReader;
import com.asus.supernote.data.AsusFormatWriter;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.editable.DrawableSpan;
import com.asus.supernote.editable.PageEditor;

public class NoteHandWriteItem extends DrawableSpan implements NoteItem, AsusFormat {
    private static class HandWriteSaveData implements Serializable, NoteItem.NoteItemSaveData {
        private static final long serialVersionUID = 1L;
        private SerPathInfo[] mSerPathInfoList;
        private int mColor;
        private float mStokeWidth;
        private short mStart = -1, mEnd = -1;
        private short mFontWidth = -1, mFontHeight = -1;
        private String mOuterClassName = null;

        public HandWriteSaveData(int fontWidth, int fontHeight, int color, float stokeWidth) {
            mFontWidth = (short) fontWidth;
            mFontHeight = (short) fontHeight;
            mColor = color;
            mStokeWidth = stokeWidth;
        }

        public Paint genPaint() {
            Paint paint = new Paint();
            PaintSelector.initPaint(paint, mColor, mStokeWidth);
            return paint;
        }

        @Override
        public String getOuterClassName() {
            return mOuterClassName;
        }
    }

    //BEGIN: RICHARD
    public LinkedList<SerPathInfo> getSerPathInfo()
    {
    	return mSerPathInfoList;
    }
    //END: RICHARD
    
    public static class PathInfo {
        private static Path genPath(short[] pointArray) {
            Path path = new Path();
            float mX = 0;
            float mY = 0;
            int length = pointArray.length;
            // Start Point

            path.moveTo(pointArray[0], pointArray[1]);
            mX = pointArray[0];
            mY = pointArray[1];

            // Other Points
            int i = 2;
            while (i < length) {
                path.quadTo(mX, mY, (pointArray[i] + mX) / 2,
                        (pointArray[i + 1] + mY) / 2);
                mX = pointArray[i];
                mY = pointArray[i + 1];
                i += 2;
            }

            // Last Point
            path.lineTo(mX, mY);

            return path;
        }

        private Path mPath;

        //BEGIN: RICHARD
        //private to public
        public short[] mPointArray;
        //END: RICHARD

        public PathInfo() {
        }

        public PathInfo(Path path, LinkedList<PointF> pointList) {
            mPath = new Path(path);
            if (pointList != null) {
                mPointArray = new short[pointList.size() * 2];
                int index = 0;
                for (PointF pointInfo : pointList) {
                    mPointArray[index++] = (short) pointInfo.x;
                    mPointArray[index++] = (short) pointInfo.y;
                }
            }
        }

        public PathInfo(Path path, PointF[] pointArray) {
            mPath = new Path(path);
            if (pointArray != null) {
                mPointArray = new short[pointArray.length * 2];
                int index = 0;
                for (PointF pointInfo : pointArray) {
                    mPointArray[index++] = (short) pointInfo.x;
                    mPointArray[index++] = (short) pointInfo.y;
                }
            }
        }

        public PathInfo(short[] pointArray) {
            mPath = genPath(pointArray);
            mPointArray = pointArray;
        }

        public Path getPath() {
            return mPath;
        }

        public short[] getPoints() {
            return mPointArray;
        }

        public SerPathInfo getSerPathInfo() {
            return new SerPathInfo(mPointArray);
        }

        private void mapPoints(Matrix matrix) {
            float[] points = new float[mPointArray.length];
            int index = 0;
            for (short point : mPointArray) {
                points[index++] = point;
            }
            matrix.mapPoints(points);

            index = 0;
            for (float point : points) {
                mPointArray[index++] = (short) point;
            }
        }

        public void setPointArray(short[] points) {
            mPath = genPath(points);
            mPointArray = points;
        }

        public void transform(Matrix matrix) {
            mPath.transform(matrix);
            mapPoints(matrix);
        }
    }

    public static class SerPathInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private short[] mPointArray;

        public SerPathInfo() {
        }

        public SerPathInfo(short[] pointArray) {
            mPointArray = pointArray;
        }

        public PathInfo getPathInfo() {
            return new PathInfo(mPointArray);
        }

        public void setPointArray(short[] points) {
            mPointArray = points;
        }
        
        //BEGIN: RICHARD
        public short[] getPointArray() {
            return mPointArray;
        }
        //END: RICHARD
    }

    public static final String TAG = "NoteHandWriteItem";
    protected static final boolean DEBUG_SIZE = false;
    private static final float PADDING_TOP = 2f;
    private static final float PADDING_BOTTOM = 5f;
    protected static final float PADDING_LEFT = 4f;
    private static final int FONT_HEIGHT_DEFAULT = 53;
    protected static final int FONT_HEIGHT_CONSTRICT = FONT_HEIGHT_DEFAULT;
    private static float HEIGHT_FACTOR = 3f;
    private static float WIDTH_FACTOR = 3f;

    public static float getPathLength(Path path) {
        PathMeasure pathMeasure = new PathMeasure(path, false);
        return pathMeasure.getLength();
    }

    // protected Drawable mDrawable;
    protected Paint mPaint;
    //BEGIN: RICHARD
    //Protected to public
    public LinkedList<PathInfo> mPathInfoList;
    //END: RICHARD
    protected LinkedList<SerPathInfo> mSerPathInfoList;
    protected int mFontHeight, mCurrentFontHeight;
    protected int mFontWidth = -1;
    protected float mRectMinHeight;
    protected float mRectMinWidth;
    protected int mStart = -1;
    protected int mEnd = -1;

    private float mBoundsTop = -1;
    private float mBoundsBottom = -1;

    // Used by Reflection when loading
    public NoteHandWriteItem() {
        super(new LazyDrawable(), DynamicDrawableSpan.ALIGN_BOTTOM);
    }

    // Used by sub-class
    public NoteHandWriteItem(Drawable drawable, int verticalAlignment) {
        super(drawable, verticalAlignment);
    }

    // Used by SupernoteFormat1to2Transfer
    public NoteHandWriteItem(LinkedList<PathInfo> pathInfoList, Paint paint) {
        super(new LazyDrawable(), DynamicDrawableSpan.ALIGN_BOTTOM);

        mPathInfoList = new LinkedList<PathInfo>();
        mPathInfoList.addAll(pathInfoList);

        if (paint != null) {
            mPaint = new Paint(paint);
        }

        RectF itemRect = getPathBounds();

        Matrix matrix = new Matrix();
        matrix.setTranslate(-itemRect.left + PADDING_LEFT, -itemRect.top + PADDING_TOP);
        for (PathInfo pathInfo : mPathInfoList) {
            pathInfo.transform(matrix);
        }

        mFontHeight = (int) (Math.round(itemRect.height()) + (PADDING_TOP + PADDING_BOTTOM));
        mFontHeight = mFontHeight < FONT_HEIGHT_CONSTRICT ? FONT_HEIGHT_CONSTRICT : mFontHeight;
        mCurrentFontHeight = FONT_HEIGHT_DEFAULT;
        initFontHeight();
        genDrawable(0);
    }

    // Used by HandWritingView when drawing finish
    public NoteHandWriteItem(LinkedList<PathInfo> pathInfoList, Paint paint, RectF bounds, int screenHeight, int fontHeight) {
        super(new LazyDrawable(), DynamicDrawableSpan.ALIGN_BOTTOM);

        if (pathInfoList != null) {
            mPathInfoList = new LinkedList<PathInfo>();
            mPathInfoList.addAll(pathInfoList);
        }
        if (paint != null) {
            mPaint = new Paint(paint);
        }

        if (bounds != null) {
            mBoundsTop = bounds.top;
            mBoundsBottom = bounds.bottom;
        }

        mFontHeight = fontHeight;
        mCurrentFontHeight = fontHeight;
        initFontHeight();
        genDrawable(screenHeight);
    }

    // Used when Paint change
    public NoteHandWriteItem(NoteHandWriteItem oldItem) {
        super(new LazyDrawable(), DynamicDrawableSpan.ALIGN_BOTTOM);

        mPathInfoList = new LinkedList<PathInfo>();
        mPathInfoList.addAll(oldItem.mPathInfoList);

        if (oldItem.mPaint != null) {
            mPaint = new Paint(oldItem.mPaint);
        }

        mFontHeight = oldItem.mFontHeight;
        mCurrentFontHeight = oldItem.mCurrentFontHeight;
        initFontHeight();
        genDrawable(0);
    }

    public void genDrawable(int screenHeight) {
        Matrix transMatrix = null;
        RectF unionBounds = null;
        float scale = 1;

        if (mSerPathInfoList != null) {
            genPaths();
            mSerPathInfoList = null;
        }

        scalePath(screenHeight);

        if (DEBUG_SIZE) {
            Log.d(TAG, "[mCurrentFontHeight, mFontHeight] = " + "[" + mCurrentFontHeight + ", " + mFontHeight + "]");
        }

        // Scale to current font height
        if (mCurrentFontHeight == mFontHeight) {
            scale = 1;
        }
        else {
            scale = (float) mCurrentFontHeight / (float) mFontHeight;
        }

        if (DEBUG_SIZE) {
            Log.d(TAG, "font size scale = " + scale);
        }

        if ((scale != 1) || (mFontWidth == -1)) {
            transMatrix = new Matrix();
            unionBounds = getPathBounds();
            transMatrix.setScale(scale, scale);
            mFontWidth = (int) (((unionBounds.right - unionBounds.left) * scale) + (3 * PADDING_LEFT));

            if (!transMatrix.isIdentity()) {
                for (PathInfo pathInfo : mPathInfoList) {
                    pathInfo.transform(transMatrix);
                }
            }

            mFontHeight = mCurrentFontHeight;

        }
        mDrawable = new LazyDrawable(mPathInfoList, mPaint);
        mDrawable.setBounds(0, 0, mFontWidth, mFontHeight);
    }

    public void genPaths() {
        if (mSerPathInfoList == null) {
            return;
        }
        mPathInfoList = new LinkedList<PathInfo>();
        for (SerPathInfo serPathInfo : mSerPathInfoList) {
            mPathInfoList.add(serPathInfo.getPathInfo());
        }
    }
    
    //BEGIN: RICHARD
    public LinkedList<PathInfo> getPathInfo()
    {
    	if(mPathInfoList != null)
    		return mPathInfoList;
    	
        if (mSerPathInfoList != null) {
            genPaths();
            mSerPathInfoList = null;
        }
        return mPathInfoList;
    }
    //END: RICHARD

    @Override
    public Drawable getDrawable() {
        return mDrawable;
    }

    @Override
    public int getEnd() {
        return mEnd;
    }

    public Paint getPaint() {
        return mPaint;
    }

    /* Calculate the union bound of Path */
    protected RectF getPathBounds() {
        RectF bounds = new RectF();
        boolean isFirstRound = true;
        float left = 0, top = 0, right = 0, bottom = 0;

        for (PathInfo pathInfo : mPathInfoList) {
            pathInfo.mPath.computeBounds(bounds, false);
            if (isFirstRound) {
                left = bounds.left;
                top = bounds.top;
                right = bounds.right;
                bottom = bounds.bottom;
                isFirstRound = false;
            }
            else {
                left = bounds.left < left ? bounds.left : left;
                top = bounds.top < top ? bounds.top : top;
                right = bounds.right > right ? bounds.right : right;
                bottom = bounds.bottom > bottom ? bounds.bottom : bottom;
            }
        }

        return new RectF(left, top, right, bottom);
    }

    public LinkedList<PathInfo> getPathInfoList() {
        return mPathInfoList;
    }

    @Override
    public int getStart() {
        return mStart;
    }

    @Override
    public String getText() {
        return null;
    }

    public int getWidth() {
        return mFontWidth;
    }

    protected void initFontHeight() {
        mRectMinHeight = mFontHeight * HEIGHT_FACTOR;
        mRectMinWidth = mFontHeight * WIDTH_FACTOR;

        if (DEBUG_SIZE) {
            Log.d(TAG, "font height = " + mFontHeight);
            Log.d(TAG, "mRectMinHeight = " + mRectMinHeight);
            Log.d(TAG, "mRectMinWidth = " + mRectMinWidth);
        }
    }

    @Override
    public void itemLoad(AsusFormatReader afr) throws IOException {
        mSerPathInfoList = new LinkedList<SerPathInfo>();
        if (mPaint == null) {
            mPaint = new Paint();
            PaintSelector.initPaint(mPaint, Color.BLACK, MetaData.SCRIBBLE_PAINT_WIDTHS_NORMAL);
        }

        AsusFormatReader.Item item = afr.readItem();
        while (item != null) {
            switch (item.getId()) {
                case SNF_NITEM_HANDWRITE_BEGIN:
                    break;
                case SNF_NITEM_HANDWRITE_POS_START:
                    mStart = item.getIntValue();
                    break;
                case SNF_NITEM_HANDWRITE_POS_END:
                    mEnd = item.getIntValue();
                    break;
                case SNF_NITEM_HANDWRITE_COLOR:
                case 0303020003: // ryan_lin@asus.com, wrong id.
                    mPaint.setColor(item.getIntValue());
                    break;
                case SNF_NITEM_HANDWRITE_STROKE_WIDTH:
                    mPaint.setStrokeWidth(item.getFloatValue());
                    break;
                case SNF_NITEM_HANDWRITE_FONT_HEIGHT:
                    mFontHeight = item.getIntValue();
                    mCurrentFontHeight = mFontHeight;
                    break;
                // BEGIN: archie_huang@asus.com
                case SNF_NITEM_HANDWRITE_FONT_WIDTH:
                    mFontWidth = item.getIntValue();
                    break;
                // END: archie_huang@asus.com
                case SNF_NITEM_HANDWRITE_PATHS:
                    short[] pointArray = item.getShortArray();
                    if ((pointArray == null) && (item.getType() == AsusFormatReader.Item.ITEM_TYPE_BYTE_ARRAY)) {
                        byte[] byteArray = item.getByteArray();
                        pointArray = new short[byteArray.length];
                        for (int i = 0; i < byteArray.length; i++) {
                            pointArray[i] = byteArray[i];
                        }
                    }
                    mSerPathInfoList.add(new SerPathInfo(pointArray));
                    break;
                case SNF_NITEM_HANDWRITE_END:
                    initFontHeight();
                    // TODO: For compatibility, remove it finally
                    if (mFontWidth == -1) {
                        genPaths();
                        mSerPathInfoList = null;
                        RectF unionBounds = getPathBounds();
                        mFontWidth = (int) ((unionBounds.right - unionBounds.left) + (3 * PADDING_LEFT));
                    }
                    mDrawable.setBounds(0, 0, mFontWidth, mFontHeight);
                    return;
                default:
                    Log.w(TAG, "Unknow id = 0x" + Integer.toHexString(item.getId()));
                    break;
            }
            item = afr.readItem();
        }
    }

    @Override
    public void itemSave(AsusFormatWriter afw) throws IOException {
        afw.writeByteArray(SNF_NITEM_HANDWRITE_BEGIN, null, 0, 0);

        afw.writeInt(SNF_NITEM_HANDWRITE_POS_START, mStart);
        afw.writeInt(SNF_NITEM_HANDWRITE_POS_END, mEnd);
        afw.writeInt(SNF_NITEM_HANDWRITE_COLOR, mPaint.getColor());
        afw.writeFloat(SNF_NITEM_HANDWRITE_STROKE_WIDTH, mPaint.getStrokeWidth());
        afw.writeInt(SNF_NITEM_HANDWRITE_FONT_HEIGHT, mFontHeight);
        // BEGIN archie_huang@asus.com
        afw.writeInt(SNF_NITEM_HANDWRITE_FONT_WIDTH, mFontWidth);
        // END archie_huang@asus.com
        
        //Begin Allen 
        if(mPathInfoList == null){
        	genPaths();
        }
        //End Allen
        
        for (PathInfo info : mPathInfoList) {
            afw.writeShortArray(SNF_NITEM_HANDWRITE_PATHS, info.mPointArray, 0, info.mPointArray.length);
        }

        afw.writeByteArray(SNF_NITEM_HANDWRITE_END, null, 0, 0);

    }

    @Override
    public void load(Serializable s, PageEditor pe) {
        HandWriteSaveData hwsd = (HandWriteSaveData) s;
        mFontWidth = hwsd.mFontWidth;
        mFontHeight = hwsd.mFontHeight;
        mCurrentFontHeight = mFontHeight;
        if(pe != null)
        {
	        mPathInfoList = new LinkedList<PathInfo>();
	
	        for (SerPathInfo sPathInfo : hwsd.mSerPathInfoList) {
	            PathInfo pathInfo = sPathInfo.getPathInfo();
	            mPathInfoList.add(pathInfo);
	        }
        }else
        {
        	mSerPathInfoList = new LinkedList<SerPathInfo>();
        	for (SerPathInfo sPathInfo : hwsd.mSerPathInfoList)
        	{
        		mSerPathInfoList.add(sPathInfo);
        	}
        }

        mPaint = hwsd.genPaint();
        mStart = hwsd.mStart;
        mEnd = hwsd.mEnd;
        initFontHeight();
        if(pe != null)//RICHARD ADD CONDITION
        {
        	genDrawable(0);
        }
    }

    @Override
    public Serializable save() {
        HandWriteSaveData hwsd = new HandWriteSaveData(mFontWidth, mFontHeight, mPaint.getColor(), mPaint.getStrokeWidth());
        if(mSerPathInfoList != null)
        {
        	//RICHARD
	        int size = mSerPathInfoList.size();
	        hwsd.mSerPathInfoList = new SerPathInfo[size];
	        for (int i = 0; i < size; i++) {
	            hwsd.mSerPathInfoList[i] = mSerPathInfoList.get(i);
	        } 	
        }
        else
        {
	        int size = mPathInfoList.size();
	        hwsd.mSerPathInfoList = new SerPathInfo[size];
	        for (int i = 0; i < size; i++) {
	            hwsd.mSerPathInfoList[i] = mPathInfoList.get(i).getSerPathInfo();
	        }
        }

        hwsd.mStart = (short) mStart;
        hwsd.mEnd = (short) mEnd;
        hwsd.mOuterClassName = this.getClass().getName();

        return hwsd;
    }

    public void scalePath(int screenHeight) {
        if (screenHeight > 0) {
            Matrix transMatrix = new Matrix();
            RectF unionBounds = getPathBounds();
            float scale = 1;
            float leftTopX;
            float leftTopY;
            float targetTop;
            float targetBottom;
            float targetHeight;

            if (DEBUG_SIZE) {
                Log.d(TAG, "[unionBound.left, unionBound.top, unionBound.right, unionBound.bottom] = "
                        + "[" + unionBounds.left + ", " + unionBounds.top
                        + ", " + unionBounds.right + ", " + unionBounds.bottom + "]");
            }

            targetTop = Math.min(unionBounds.top, mBoundsTop);
            targetBottom = Math.max(unionBounds.bottom, mBoundsBottom);
            targetHeight = targetBottom - targetTop;

            scale = (mFontHeight - (PADDING_TOP + PADDING_BOTTOM)) / targetHeight;
            transMatrix.setScale(scale, scale);
            transform(transMatrix);
            unionBounds = getPathBounds();
            mFontWidth = (int) ((unionBounds.right - unionBounds.left) + (3 * PADDING_LEFT));
            leftTopY = targetTop * scale;
            leftTopX = unionBounds.left;
            transMatrix.reset();
            transMatrix.setTranslate(-leftTopX + PADDING_LEFT, -leftTopY + PADDING_TOP);
            transform(transMatrix);
        }
    }

    // BEGIN ryan_lin@asus.com, for change only color or stroke
    public void setColor(int color) {
        if (color != mPaint.getColor()) {
            mPaint.setColor(color);
            genDrawable(0);
        }
    } // END ryan_lin@asus.com

    //BEGIN: RICHARD
    public int getColor()
    {
    	return mPaint.getColor();
    }
    
    public float getStrokeWidth()
    {
    	return mPaint.getStrokeWidth();
    }
    //END: RICHARD
    
    @Override
    public void setEnd(int end) {
        mEnd = end;
    }

    // TODO: generate path in appropriate method
    @Override
    public void setFontHeight(int fontHeight) {
        if ((fontHeight != mCurrentFontHeight) || (mSerPathInfoList != null)) {
            mCurrentFontHeight = fontHeight;
            genDrawable(0);
        }
    }

    @Override
    public void setStart(int start) {
        mStart = start;
    }

    // BEGIN ryan_lin@asus.com, for change only color or stroke
    public void setStrokeWidth(float width) {
        if (width != mPaint.getStrokeWidth()) {
            mPaint.setStrokeWidth(width);
            genDrawable(0);
        }
    } // END ryan_lin@asus.com

    protected void transform(Matrix transMatrix) {
        for (PathInfo pathInfo : mPathInfoList) {
            pathInfo.transform(transMatrix);
        }
    }
}
