package com.asus.supernote.editable.noteitem;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

import com.asus.supernote.BitmapLender;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem.PathInfo;

public class LazyDrawable extends BitmapDrawable implements BitmapLender.BitmapBorrower {
    private static final boolean DEBUG_SIZE = false;
    // BEGIN ryan_lin@asus.com
    private static final int DEFAULT_WIDTH = 10;
    private static final int DEFAULT_HEIGHT = 10;
    // END ryan_lin@asus.com
    private Paint mPaint = new Paint();
    private Path mPath = new Path();
    private Bitmap mCache = null;
    private static int[] mCacheLock = new int[0]; // Better: avoid draw using recycled bitmap

    @SuppressWarnings("deprecation")
    public LazyDrawable() {
    }

    @SuppressWarnings("deprecation")
    public LazyDrawable(List<PathInfo> pathInfoList, Paint paint) {
        for (PathInfo pathInfo : pathInfoList) {
            mPath.addPath(pathInfo.getPath());
        }
        mPaint = paint;
    }

    private void createCache() {
        Rect bounds = getBounds();
        BitmapLender bitmapPool = BitmapLender.getInstance();
        // BEGIN ryan_lin@asus.com
        if (!bounds.isEmpty()) {
            mCache = bitmapPool.borrow(this, bounds.width(), bounds.height());
            if (mCache != null) {
                Canvas bmpCanvas = new Canvas(mCache);
                if (DEBUG_SIZE) {
                    bmpCanvas.drawColor(0x5500DC03);
                }
                bmpCanvas.drawPath(mPath, mPaint);
            }
        }
        else {
            mCache = bitmapPool.borrow(this, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }
        // END ryan_lin@asus.com
    }

    @Override
    public void draw(Canvas canvas) {
        if (mCache == null) {
            createCache();
        }
        
        synchronized (mCacheLock) { // Better
	        if (mCache != null) {
	            canvas.drawBitmap(mCache, 0, 0, getPaint());
	        }
	        else {
	            canvas.drawPath(mPath, mPaint);
	        }
        }
    }

    @Override
    public Bitmap recycle() {
    	synchronized (mCacheLock) { // Better
	        Bitmap bitmap = mCache;
	        mCache = null;
	        return bitmap;
        }
    }

    public void render() {
        createCache();
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
    }

    public void setPathInfos(List<PathInfo> pathInfoList) {
        mPath.reset();
        for (PathInfo pathInfo : pathInfoList) {
            mPath.addPath(pathInfo.getPath());
        }
    }

    @Override
    public int getIntrinsicHeight() {
        return getBounds().height();
    }

    @Override
    public int getIntrinsicWidth() {
        return getBounds().width();
    }

    @Override
    public int getByteCount() {
        return mCache.getByteCount();
    }

}
