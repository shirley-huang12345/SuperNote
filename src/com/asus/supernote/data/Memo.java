package com.asus.supernote.data;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.asus.supernote.R;
import com.asus.supernote.picker.PickerActivity;
import com.asus.supernote.picker.PickerUtility;
import com.asus.supernote.widget.MemoReceiver;
import com.asus.supernote.widget.WidgetProvider;
import com.asus.supernote.widget.WidgetRemoteService;

public class Memo implements AsusFormat {

    public static void addMemo(Context context, Long bookId, Long pageId, Bitmap bitmap, RectF hint) {

        // BEGIN: archie_huang@asus.com
        // To avoid null pointer exception
        if (context == null || bitmap == null || hint == null) {
            return;
        }
        // END: archie_huang@asus.com

        String dirPath = MetaData.MEMO_DIR;
        File dir = new File(dirPath);
        String fileName = MetaData.MEMO_PREFIX + System.currentTimeMillis() + MetaData.MEMO_EXTENSION;
        File file = new File(dir, fileName);
        try {
            PickerUtility.forceMkDir(dirPath);
            file.createNewFile();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Cursor cursor = context.getContentResolver().query(MetaData.BookTable.uri, null, "created_date = ?", new String[] { bookId.toString() }, null);
        cursor.moveToFirst();

        ContentValues cv = new ContentValues();
        cv.put(MetaData.MemoTable.WIDGET_ID, System.currentTimeMillis());
        cv.put(MetaData.MemoTable.BOOK_ID, bookId);
        cv.put(MetaData.MemoTable.PAGE_ID, pageId);
        cv.put(MetaData.MemoTable.THUMBNAIL, file.getPath());
        cv.put(MetaData.MemoTable.IS_HIDDEN, cursor.getInt(MetaData.BookTable.INDEX_IS_LOCKED));
        cv.put(MetaData.MemoTable.BOOK_NAME, cursor.getString(MetaData.BookTable.INDEX_TITLE));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dis = new DataOutputStream(baos);
        try {
            dis.writeShort((short) hint.top);
            dis.writeShort((short) hint.bottom);
            dis.writeShort((short) hint.left);
            dis.writeShort((short) hint.right);
            cv.put(MetaData.MemoTable.MEMO_INFO, baos.toByteArray());
            dis.close();
            baos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        cursor.close();//RICHARD FIX MEMORY LEAK
        context.getContentResolver().insert(MetaData.MemoTable.uri, cv);
        updateWidget(context);
    }

    public static void deleteWidget(Context context, Long widgetId) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(MetaData.MemoTable.uri, null, "widget_id = ?", new String[] { widgetId.toString() }, null);
        cursor.moveToFirst();
        String filePath = cursor.getString(MetaData.MemoTable.INDEX_THUMBNAIL);
        cursor.close();
        File file = new File(filePath);
        file.delete();
        updateWidget(context);
    }

    // update all memo widgets on the desktop
    public static void updateWidget(Context context) {
        Cursor cursor = context.getContentResolver().query(MetaData.MemoTable.uri, null, "is_hidden = 0", null, null);
        if (cursor == null) {
            return;
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        for (int id : ids) {
            RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            remoteView.setViewVisibility(R.id.memoLoadingSpinner, View.GONE);
            if (cursor.getCount() == 0) {
                remoteView.setViewVisibility(R.id.noMemoHintView, View.VISIBLE);
                remoteView.setViewVisibility(R.id.memoStackView, View.GONE);
                try {
					Intent intent = new Intent(context, PickerActivity.class);
					PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, 0);
					remoteView.setOnClickPendingIntent(R.id.noMemoHintView, pendingIntent);
					appWidgetManager.updateAppWidget(id, remoteView);
				} catch (ActivityNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            else {
                remoteView.setViewVisibility(R.id.noMemoHintView, View.GONE);
                remoteView.setViewVisibility(R.id.memoStackView, View.VISIBLE);
                try {
					Intent intent = new Intent(context, WidgetRemoteService.class);
					intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
					intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
					remoteView.setRemoteAdapter(id, R.id.memoStackView, intent);
					intent = new Intent();
					intent.setAction(MemoReceiver.ACTION);
					PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
					remoteView.setPendingIntentTemplate(R.id.memoStackView, pendingIntent);
					appWidgetManager.updateAppWidget(id, remoteView);
					appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.memoStackView);
				} catch (ActivityNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
        cursor.close();
    }

    @Override
    public void itemSave(AsusFormatWriter afw) throws IOException {
        
    }

    @Override
    public void itemLoad(AsusFormatReader afr) throws IOException {
        
    }
}
