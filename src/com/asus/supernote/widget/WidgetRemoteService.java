package com.asus.supernote.widget;

import java.util.HashMap;
import java.util.Iterator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;

public class WidgetRemoteService extends RemoteViewsService {
    public static final String TAG = "WidgetRemoteService";
    private static final int MAX_FILE_SIZE = 512 * 512;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        return new WidgetViewFactory(getApplicationContext(), id);
    }

    private class WidgetViewFactory implements RemoteViewsFactory {
        public static final int MAX_CACHE_SIZE = 10;
        private Context mContext;
        private Cursor mCursor = null;
        private BitmapFactory.Options mOption;
        private HashMap<String, Bitmap> mBitmapCache;

        public WidgetViewFactory(Context context, int widgetId) {
            mContext = context;
            mOption = new BitmapFactory.Options();
            mBitmapCache = new HashMap<String, Bitmap>();
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public long getItemId(int position) {
            return 0L;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteView = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
            try {
                mCursor.moveToPosition(position);
            }
            catch (Exception e) {
                mCursor.close();
                mCursor = mContext.getContentResolver().query(MetaData.MemoTable.uri, null, "is_hidden = 0", null, null);
                mCursor.moveToFirst();
            }

            String filePath = mCursor.getString(MetaData.MemoTable.INDEX_THUMBNAIL);
            Long bookId = mCursor.getLong(MetaData.MemoTable.INDEX_BOOK_ID);
            Long pageId = mCursor.getLong(MetaData.MemoTable.INDEX_PAGE_ID);
            Long memoId = mCursor.getLong(MetaData.MemoTable.INDEX_WIDGET_ID);
            mOption.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, mOption);

            mOption.inSampleSize = computeSampleSize(mOption, -1, MAX_FILE_SIZE);
            mOption.inJustDecodeBounds = false;
            Bitmap bitmap = null;
            if (mBitmapCache.containsKey(filePath)) {
                bitmap = mBitmapCache.get(filePath);
            }
            else {
                if (mBitmapCache.size() > MAX_CACHE_SIZE) {
                    for (Iterator<Bitmap> iter = mBitmapCache.values().iterator(); iter.hasNext();) {
                        Bitmap b = iter.next();
                        if (b != null && b.isRecycled() == false) {
                            b.recycle();
                            b = null;
                        }
                    }
                    mBitmapCache.clear();
                }
                bitmap = BitmapFactory.decodeFile(filePath, mOption);
                mBitmapCache.put(filePath, bitmap);

            }
            try {
				Intent intent = new Intent();
				intent.putExtra(MetaData.BOOK_ID, bookId);
				intent.putExtra(MetaData.PAGE_ID, pageId);
				intent.putExtra(MetaData.MEMO_ID, memoId);
				intent.putExtra(MetaData.MemoTable.THUMBNAIL, filePath);
				remoteView.setOnClickFillInIntent(R.id.memoImage, intent);
				remoteView.setImageViewBitmap(R.id.memoImage, bitmap);
				remoteView.setTextViewText(R.id.memoPageNumber, Integer.toString(position + 1) + "/" + mCursor.getCount());
				return remoteView;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public void onCreate() {
            mCursor = mContext.getContentResolver().query(MetaData.MemoTable.uri, null, "is_hidden = 0", null, null);
        }

        @Override
        public void onDataSetChanged() {
            mCursor.requery();
        }

        @Override
        public void onDestroy() {
            mCursor.close();
        }

        private int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
            double w = options.outWidth;
            double h = options.outHeight;
            int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
            int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength),

                    Math.floor(h / minSideLength));

            if (upperBound < lowerBound) {
                // return the larger one when there is no overlapping zone.
                return lowerBound;
            }

            if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
                return 1;
            }
            else if (minSideLength == -1) {
                return lowerBound;
            }
            else {
                return upperBound;
            }
        }

        private int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
            int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

            int roundedSize;

            if (initialSize <= 8) {
                roundedSize = 1;
                while (roundedSize < initialSize) {
                    roundedSize <<= 1;
                }
            }
            else {
                roundedSize = (initialSize + 7) / 8 * 8;
            }
            return roundedSize;
        }
    }

}
