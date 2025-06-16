package com.asus.supernote.widget;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;

public class MemoReceiver extends BroadcastReceiver {
    public static final String TAG = "MemoReceiver";
    public static final String ACTION = "com.asus.supernote.MEMO_RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        ContentResolver cr = context.getContentResolver();
        Long bookId = intent.getLongExtra(MetaData.BOOK_ID, 0L);
        Long pageId = intent.getLongExtra(MetaData.PAGE_ID, 0L);
        boolean isEmpty = false;
        Cursor bookCursor = cr.query(MetaData.BookTable.uri, null, "created_date = ?", new String[] { bookId.toString() }, null);
        Cursor pageCursor = cr.query(MetaData.PageTable.uri, null, "created_date = ?", new String[] { pageId.toString() }, null);
        if (bookCursor.getCount() == 0) {
            isEmpty = true;
        }
        else if (pageCursor.getCount() == 0) {
            isEmpty = true;
        }
        else {
            isEmpty = false;
        }

        if (isEmpty == true) {
            Toast.makeText(context, R.string.memo_not_exists, Toast.LENGTH_SHORT).show();
        }
        else {
            try {
				Intent activityIntent = new Intent();
				activityIntent.setClass(context, EditorActivity.class);
				activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				activityIntent.putExtra(MetaData.BOOK_ID, bookId);
				activityIntent.putExtra(MetaData.PAGE_ID, pageId);
				context.startActivity(activityIntent);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        //BEGIN: RICHARD
        if(bookCursor != null)
        {
        	bookCursor.close();
        }
        if(pageCursor != null)
        {
        	pageCursor.close();
        }
        //END: RICHARD

    }
}
