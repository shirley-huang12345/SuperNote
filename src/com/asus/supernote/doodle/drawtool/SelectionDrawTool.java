package com.asus.supernote.doodle.drawtool;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeSet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.asus.supernote.R;
import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo.Point;
import com.asus.supernote.doodle.drawinfo.GraphicDrawInfo;
import com.asus.supernote.doodle.drawinfo.SelectionDrawInfo;
import com.asus.supernote.doodle.drawinfo.TextImgDrawInfo;
import com.asus.supernote.doodle.drawinfo.TransformDrawInfo;

public class SelectionDrawTool extends DrawTool {
    private static final String TAG = "SelectionDrawTool";
    public static final int SELECTION_TOOL = DrawTool.SELECTION_TOOL;
    private static int STROKE_WIDTH = 3;
    public static int BOUND_GROUP_COLOR = 0xFF717171;
    public static int BOUND_SELECT_COLOR = 0xFF717171;
    private static int BOUND_CONTAIN_GROUP_COLOR = 0xFF717171;
    private SelectionDrawInfo mDrawInfo = null;
    private Paint mBoundPaint, mBmpPaint;;
    private Bitmap mRotateImage, mCancelImage;
    private Bitmap mCacheBitmap;
    private Matrix mPositionMatrix = new Matrix();
    private Context mContext;
    private boolean mIsCacheDirty = true;

    public SelectionDrawTool(Context context) {
        super(SELECTION_TOOL);
        mBoundPaint = new Paint();
        mBoundPaint.setAntiAlias(true);
        mBoundPaint.setDither(true);
        //Begin :clare
        if(context!=null)
        {
        	BOUND_GROUP_COLOR=context.getResources().getInteger(R.color.dashline_BOUND_GROUP_COLOR);
        	BOUND_SELECT_COLOR=context.getResources().getInteger(R.color.dashline_BOUND_SELECT_COLOR);
        	BOUND_CONTAIN_GROUP_COLOR=context.getResources().getInteger(R.color.dashline_BOUND_CONTAIN_GROUP_COLOR);  
        }
        //End :clare
        mBoundPaint.setColor(BOUND_SELECT_COLOR);       
        mBoundPaint.setStyle(Paint.Style.STROKE);
        mBoundPaint.setXfermode(null);
        mBoundPaint.setAlpha(0xFF);
        //Begin: clare  :set the style of dashline
        float dashline_real = 0;
        float dashline_imag = 0;
        if(context!=null)
        {
        	STROKE_WIDTH=context.getResources().getInteger(R.integer.dashline_STROKE_WIDTH);     	    
        	 dashline_real=context.getResources().getInteger(R.integer.dashline_real);
        	 dashline_imag=context.getResources().getInteger(R.integer.dashline_imag);        	
        }
        mBoundPaint.setStrokeWidth(STROKE_WIDTH); 
        mBoundPaint.setPathEffect(new DashPathEffect(new float[] { dashline_real, dashline_imag }, 10));
        //End: clare
        mBmpPaint = new Paint();
        mBmpPaint.setDither(true);
        mBmpPaint.setAntiAlias(true);

        if (context != null) {
            mContext = context;
        }
        initControlImage();
    }

    private boolean createCache(SelectionDrawInfo selectionInfo, boolean isUseTempBitmap) {//RICHARD 2012/1/15

        if ((mCacheBitmap != null) && !mCacheBitmap.isRecycled()) {
            mCacheBitmap.recycle();
            mCacheBitmap = null;
        }

        selectionInfo.resetCache();

        RectF bounds = selectionInfo.getBounds();
        Canvas cacheCanvas;

        try {
            mCacheBitmap = Bitmap.createBitmap((int) (bounds.width()), (int) (bounds.height()), Bitmap.Config.ARGB_8888);
        }
        catch (OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] Create cache failed!!");
            return false;
        }
        //BEGIN: RICHARD
        catch(Exception exp)
        {
        	exp.printStackTrace();
        	return false;
        }
        //END: RICHARD
        cacheCanvas = new Canvas(mCacheBitmap);
        cacheCanvas.save();
        cacheCanvas.translate(-bounds.left, -bounds.top);

        drawSelections(selectionInfo, cacheCanvas, isUseTempBitmap);

        cacheCanvas.restore();
        mPositionMatrix.reset();
        mPositionMatrix.setTranslate(bounds.left, bounds.top);
        return true;
    }

    @Override
    protected void doDraw(Canvas canvas, DrawInfo drawInfo) {
    }

    @Override
    public void draw(Canvas canvas, boolean isUseTempBitmap, DrawInfo drawInfo) {
        SelectionDrawInfo selectionDrawInfo = (SelectionDrawInfo) drawInfo;
        Collection<DrawInfo> selections = selectionDrawInfo.getSelectedObjects();

        // Selecting
        if (selections.size() == 0) {
            RectF bounds = selectionDrawInfo.getSelectionFrame();
            mBoundPaint.setColor(BOUND_SELECT_COLOR);
            canvas.drawRect(bounds, mBoundPaint);
        }
        else { // something has selected
            if ((mCacheBitmap == null) || mIsCacheDirty) {
            	mIsCacheDirty=false;
                createCache(selectionDrawInfo, isUseTempBitmap);
            }
            if (mCacheBitmap != null) {
                Matrix matrix = new Matrix(mPositionMatrix);
                matrix.postConcat(selectionDrawInfo.getCacheMatrix());
                canvas.drawBitmap(mCacheBitmap, matrix, mBmpPaint);
            }
            else {
                drawSelections(selectionDrawInfo, canvas, true);//isUseTempBitmap);//outside pass is false.avoid black.RICHARD
            }
            drawBounds(canvas, selectionDrawInfo);
        }
    }

    private void drawBounds(Canvas canvas, SelectionDrawInfo selectionDrawInfo) {
        Point[] corners = selectionDrawInfo.getCorners();

        if (selectionDrawInfo.isGroup()) {
            mBoundPaint.setColor(BOUND_GROUP_COLOR);
        }
        else if (selectionDrawInfo.containGroup()) {
            mBoundPaint.setColor(BOUND_CONTAIN_GROUP_COLOR);
        }
        else {
            mBoundPaint.setColor(BOUND_SELECT_COLOR);
        }

        canvas.drawLine(corners[TransformDrawInfo.CTR_LEFT_TOP].x, corners[TransformDrawInfo.CTR_LEFT_TOP].y, corners[TransformDrawInfo.CTR_RIGHT_TOP].x, corners[TransformDrawInfo.CTR_RIGHT_TOP].y,
                mBoundPaint);
        canvas.drawLine(corners[TransformDrawInfo.CTR_RIGHT_TOP].x, corners[TransformDrawInfo.CTR_RIGHT_TOP].y, corners[TransformDrawInfo.CTR_RIGHT_BOTTOM].x,
                corners[TransformDrawInfo.CTR_RIGHT_BOTTOM].y, mBoundPaint);
        canvas.drawLine(corners[TransformDrawInfo.CTR_RIGHT_BOTTOM].x, corners[TransformDrawInfo.CTR_RIGHT_BOTTOM].y, corners[TransformDrawInfo.CTR_LEFT_BOTTOM].x,
                corners[TransformDrawInfo.CTR_LEFT_BOTTOM].y, mBoundPaint);
        canvas.drawLine(corners[TransformDrawInfo.CTR_LEFT_BOTTOM].x, corners[TransformDrawInfo.CTR_LEFT_BOTTOM].y, corners[TransformDrawInfo.CTR_LEFT_TOP].x,
                corners[TransformDrawInfo.CTR_LEFT_TOP].y, mBoundPaint);
        if ((mRotateImage == null) || (mCancelImage == null)) {
            initControlImage();
        }
        if ((mRotateImage != null) && (mCancelImage != null)) {
            int width = mRotateImage.getWidth();
            int height = mRotateImage.getHeight();
            	 canvas.drawBitmap(mRotateImage, corners[TransformDrawInfo.CTR_RIGHT_BOTTOM].x - (width / 2), corners[TransformDrawInfo.CTR_RIGHT_BOTTOM].y - (height / 2), null);
            	 canvas.drawBitmap(mCancelImage, corners[TransformDrawInfo.CTR_RIGHT_TOP].x - (width / 2), corners[TransformDrawInfo.CTR_RIGHT_TOP].y - (height / 2), null);
         }
    }

    private void drawSelections(SelectionDrawInfo selectionInfo, Canvas canvas,boolean isUseTempBitmap) {//2013/1/15
        LinkedHashMap<Integer, DrawInfo> selections = selectionInfo.getSelections();
        TreeSet<Integer> keys = new TreeSet<Integer>(selections.keySet());
        Iterator<Integer> iterator = keys.descendingIterator();
        LinkedList<DrawInfo> DrawInfos = new LinkedList<DrawInfo>();//by show

        while (iterator.hasNext()) {
            DrawInfo d = selections.get(iterator.next());
            DrawInfos.add(d);//by show
            if ((d instanceof GraphicDrawInfo) || (d instanceof TextImgDrawInfo)) {//by show
            	d.getDrawTool().draw(canvas, isUseTempBitmap, d);
            	if (d instanceof GraphicDrawInfo&& ((GraphicDrawInfo)d).isShapeGraphic()) {// by jason ,if use cache,the path will distortion
            		mIsCacheDirty=true;
				}
            }
        }
        
        //Begin: show_wang@asus.com
        //Modified reason: for eraser
        for (DrawInfo info : DrawInfos) {
        	if (!(info instanceof GraphicDrawInfo) && !(info instanceof TextImgDrawInfo)) {
            	info.getDrawTool().draw(canvas, true, info);
            }
        }
        //End: show_wang@asus.com
  
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public DrawInfo getDrawInfo(Paint usedPaint) {
        if (mDrawInfo == null) {
            mDrawInfo = new SelectionDrawInfo(this, usedPaint,mContext);
        }
        return mDrawInfo;
    }

    private void initControlImage() {
        if ((mRotateImage != null) && !mRotateImage.isRecycled()) {
            mRotateImage.recycle();
            mRotateImage = null;
        }
        if ((mCancelImage != null) && !mCancelImage.isRecycled()) {
            mCancelImage.recycle();
            mCancelImage = null;
        }
        if (mContext != null) {
            mRotateImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_edit_pic_rotation);
            mCancelImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_edit_pic_delete);
        }
    }

    public void releaseCache() {
        if ((mCacheBitmap != null) && !mCacheBitmap.isRecycled()) {
            mCacheBitmap.recycle();
            mCacheBitmap = null;
        }
        if ((mRotateImage != null) && !mRotateImage.isRecycled()) {
            mRotateImage.recycle();
            mRotateImage = null;
        }
        if ((mCancelImage != null) && !mCancelImage.isRecycled()) {
            mCancelImage.recycle();
            mCancelImage = null;
        }
    }

    public void setCacheDirty(boolean isDirty) {
        mIsCacheDirty = isDirty;
    }
}
