package com.asus.supernote;

import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class BitmapLender {
    public interface BitmapBorrower {
        Bitmap recycle();

        int getByteCount();
    }

    private class BitmapWrapper {
        private Bitmap mBitmap;
        private int mWidth = 0, mHeight = 0;
        private final int mId;

        public BitmapWrapper(Bitmap bitmap) {
            mId = mBitmapWrapperId++;
            mBitmap = bitmap;
            if (mBitmap != null) {
                mWidth = mBitmap.getWidth();
                mHeight = mBitmap.getHeight();
            }
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public int getHeight() {
            return mHeight;
        }

        public int getWidth() {
            return mWidth;
        }

        public int getId() {
            return mId;
        }
    }

    private static final String TAG = "BitmapLender";
    private static final boolean DEBUG_MEM_USAGE = false;
    private static int mBitmapWrapperId = 0;
    private static BitmapLender mBitmapPool = null;

    public static synchronized BitmapLender getInstance() {
        if (mBitmapPool == null) {
            mBitmapPool = new BitmapLender();
        }
        return mBitmapPool;
    }

    private TreeSet<BitmapWrapper> mBitmapSet = new TreeSet<BitmapWrapper>(new Comparator<BitmapWrapper>() {

        @Override
        public int compare(BitmapWrapper lhs, BitmapWrapper rhs) {
            int lId = lhs.getId();
            int rId = rhs.getId();
            int lWidth = lhs.getWidth();
            int rWidth = rhs.getWidth();

            if (lId == rId) {
                return 0;
            }
            else if (lWidth > rWidth) {
                return 1;
            }
            else if (lWidth < rWidth) {
                return -1;
            }
            else {
                if (lId > rId) {
                    return 1;
                }
                else if (lId < rId) {
                    return -1;
                }
                return 0;
            }
        }

    });
    private HashSet<BitmapBorrower> mBorrowers = new HashSet<BitmapBorrower>();

    private BitmapLender() {
        mBitmapWrapperId = 0;
    }

    public synchronized Bitmap borrow(BitmapBorrower borrower, int width, int height) {
        BitmapWrapper loanBitmapWrapper = null;
        if (borrower == null) {
            return null;
        }

        recycle(borrower.recycle());
        mBorrowers.add(borrower);

        for (BitmapWrapper bitmapWrapper : mBitmapSet) {
            if ((bitmapWrapper.getWidth() >= width) && (bitmapWrapper.getHeight() >= height)) {
                loanBitmapWrapper = bitmapWrapper;
                break;
            }
        }

        if (loanBitmapWrapper != null) {
            Bitmap bitmap = loanBitmapWrapper.getBitmap();
            bitmap.eraseColor(Color.TRANSPARENT);
            mBitmapSet.remove(loanBitmapWrapper);
            return bitmap;
        }
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        catch(OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] borrow() failed !!!");
        }
        return bitmap;
    }

    public synchronized void recycleBitmaps() {
        recycle();
        for (BitmapWrapper bitmapWrapper : mBitmapSet) {
            Bitmap bitmap = bitmapWrapper.getBitmap();
            if ((bitmap != null) && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
        }
        mBitmapSet.clear();
        mBitmapWrapperId = 0;
    }

    public synchronized void recycle() {
        for (BitmapBorrower borrower : mBorrowers) {
            recycle(borrower.recycle());
        }
        mBorrowers.clear();
        if (DEBUG_MEM_USAGE) {
            dumpUsageInformation();
        }
    }

    public synchronized void recycle(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        mBitmapSet.add(new BitmapWrapper(bitmap));
    }

    public synchronized void dumpUsageInformation() {
        int bitmapNum;
        bitmapNum = mBitmapSet.size() + mBorrowers.size();
        Log.i(TAG, "Number of created bitmap: " + bitmapNum);

        int byteCount = 0;
        for (BitmapWrapper bitmapWrapper : mBitmapSet) {
            byteCount += bitmapWrapper.getBitmap().getByteCount();
        }
        for (BitmapBorrower borrower : mBorrowers) {
            byteCount += borrower.getByteCount();
        }
        Log.i(TAG, "Memory usage: " + byteCount + " bytes");
    }
}
