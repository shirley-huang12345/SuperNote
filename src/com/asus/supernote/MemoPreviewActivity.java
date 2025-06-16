package com.asus.supernote;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;

public class MemoPreviewActivity extends Activity {
    public static final String TAG = "MemoPreviewActivity";
    public static final int CONFIRM_DIALOG = 1;
    private Long mBookId = 0L;
    private Long mPageId = 0L;
    private Long mMemoId = 0L;
    private RectF mRect = null;
    private boolean mIsEmpty = false;

    private ImageView mMemoImage;
    private Button mLunchButton;
    private TextView mMemoDate;
    private java.text.DateFormat mDateFormater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // BEGIN: Better
        if (MetaData.AppContext == null) {
    		MetaData.AppContext = getApplicationContext();
		}
        // END: Better
        WindowManager.LayoutParams windowLayoutParams = getWindow().getAttributes();
        windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        windowLayoutParams.dimAmount = 0.1f;
        getWindow().setAttributes(windowLayoutParams);
        Intent intent = getIntent();
        mBookId = intent.getLongExtra(MetaData.BOOK_ID, 0L);
        mPageId = intent.getLongExtra(MetaData.PAGE_ID, 0L);
        mMemoId = intent.getLongExtra(MetaData.MEMO_ID, 0L);
        mRect = (RectF) intent.getParcelableExtra(MetaData.MEMO_INFO);

        int pageIndex = -1;
        NoteBook book = BookCase.getInstance(this).getNoteBook(mBookId);
        if (book == null) {
            mIsEmpty = true;
        }
        else if ((pageIndex = book.getPageIndex(mPageId)) == -1) {
            mIsEmpty = true;
        }
        else {
            mIsEmpty = false;
        }

        setContentView(R.layout.memo_preview);
        Cursor cursor = getContentResolver().query(MetaData.MemoTable.uri, null, "widget_id = ?", new String[] { mMemoId.toString() }, null);
        mMemoImage = (ImageView) findViewById(R.id.memo_preview);
        mLunchButton = (Button) findViewById(R.id.memoLunchButton);
        mMemoDate = (TextView) findViewById(R.id.memoDateText);
        mDateFormater = DateFormat.getDateFormat(this);
        if (mMemoId != 0L) {
            mMemoDate.setText(mDateFormater.format(new Date(mMemoId)));
        }
        mLunchButton.setOnClickListener(lunchClick);
        int count = cursor.getCount();
        cursor.moveToFirst();
        if ((pageIndex != -1) && (count > 0)) {
        	String filePath = cursor.getString(MetaData.MemoTable.INDEX_THUMBNAIL);
            try {
                mMemoImage.setImageBitmap(BitmapFactory.decodeFile(filePath));
            }
            catch (OutOfMemoryError e) {
                Log.w(TAG, "[OutOfMemoryError] mMemoImage.setImageBitmap() failed !!!");
            }
            String msg = cursor.getString(MetaData.MemoTable.INDEX_BOOK_NAME) + ", #" + (pageIndex + 1);
            String buttonText = String.format(getResources().getString(R.string.memo_link), msg);
            mLunchButton.setText(buttonText);
        }
        else {
            mLunchButton.setText(android.R.string.cancel);
        }
        cursor.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    // BEGIN: archie_huang@asus.com
    // To avoid memory leak
    @Override
    protected void onDestroy() {
        if (mMemoImage != null) {
            Drawable drawable = mMemoImage.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        }
        super.onDestroy();
    }
    // END: archie_huang@asus.com

    private View.OnClickListener lunchClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIsEmpty) {
                finish();
            }
            else {
                Intent intent = getIntent();
                intent.setClass(getApplicationContext(), EditorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(MetaData.BOOK_ID, mBookId);
                intent.putExtra(MetaData.PAGE_ID, mPageId);
                intent.putExtra(MetaData.MEMO_INFO, mRect);
                startActivity(intent);
                finish();
            }
        }
    };

    @Override
    protected android.app.Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pg_not_exist);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(CONFIRM_DIALOG);
            }
        });
        return builder.create();
    };

    @Override
    protected void onStop() {
        super.onStop();
    }

}
