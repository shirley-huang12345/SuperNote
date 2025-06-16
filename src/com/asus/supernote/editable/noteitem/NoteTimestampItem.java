package com.asus.supernote.editable.noteitem;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.text.format.DateFormat;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;

import com.asus.supernote.R;
import com.asus.supernote.SuperNoteApplication;
import com.asus.supernote.data.AsusFormat;
import com.asus.supernote.data.AsusFormatReader;
import com.asus.supernote.data.AsusFormatWriter;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.editable.DrawableSpan;
import com.asus.supernote.editable.PageEditor;

public class NoteTimestampItem extends DrawableSpan implements NoteItem,
		AsusFormat {

	private static final int DEFIND_TIMESTAMP_WIDTH = 155;

	private static final String TAG = "NoteTimestampItem";

	private int mStart = -1;
	private int mEnd = -1;

	private long mTime = -1;
	private int mfoneHeight = 0;
	private long mowner = 0L;// wendy

	private Drawable mDrawable = null;
	private int mWidth = 0; //smilefish

	public NoteTimestampItem() {
		super(new LazyDrawable(), DynamicDrawableSpan.ALIGN_BASELINE);
	}

	public NoteTimestampItem(long pageid, int fontHeight) {
		super(new LazyDrawable(), DynamicDrawableSpan.ALIGN_BASELINE);
		mowner = pageid;
		mfoneHeight = fontHeight;
	}

	public NoteTimestampItem(long time, int fontHeight, long pageid) {
		super(new LazyDrawable(), DynamicDrawableSpan.ALIGN_BASELINE);
		mowner = pageid;
		mTime = time;
		mfoneHeight = fontHeight;
		init(false);
	}

	// Begin Allen
	@Override
	public void setFontHeight(int fontHeight) {
		mfoneHeight = fontHeight;
		init(true);
	}

	// End Allen

	public NoteTimestampItem(Drawable _d, int _valign) {
		super(_d, _valign);
	}

	private void init(boolean isWidget) {  //add parameter for widget case [Carol]
		if (mTime == -1 || mfoneHeight <= 0) {
			return;
		}
		Context context = SuperNoteApplication.getContext();
		//begin smilefish fix bug 385646
		java.text.DateFormat dataFormat1 = DateFormat.getTimeFormat(context);
		java.text.DateFormat dataFormat2 = DateFormat.getDateFormat(context);
		//end smilefish
		String dateTime1 = dataFormat1.format(new Date(mTime));
		String dateTime2 = dataFormat2.format(new Date(mTime));
		String dateTime = dateTime1 + '\n' + dateTime2;
		
        //Begin Dave.To modify TimeStamp attacher UI. 
		 //NoteBookPickerActivity.getAppContext();
		int text_height = context.getResources().getDimensionPixelSize(R.dimen.attacher_line_height); //smilefish
		float textsize = context.getResources().getDimension(
				R.dimen.timestamp_textsize);
		// Log.v("wendy", "textsize = " + textsize);
        int text_width = context.getResources().getDimensionPixelSize(  //smilefish
                R.dimen.attacher_text_width);
        if(isWidget){ //decrease each value in widget [Carol]
            textsize -= context.getResources().getDimension(R.dimen.widget_diff_timestamp_textsize);
            text_width -= context.getResources().getDimensionPixelSize(R.dimen.widget_diff_attacher_text_width);
            text_height -= context.getResources().getDimensionPixelSize(R.dimen.widget_diff_attacher_line_height);
        }

		Bitmap bitmap2 = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.asus_insert_time_green);

		int width = text_width + text_height;
		mWidth = width; //smilefish
		int rectHeight = text_height;

		//begin smilefish
    	Rect rect_left = new Rect(0,0,text_height, text_height);	
    	int icon_size = bitmap2.getWidth();
    	int rect_top = (text_height - icon_size) / 2;

		Paint textPaint = new Paint();
		textPaint.setColor(context.getResources().getColor(R.color.timestamp_item_left_color));
		textPaint.setStyle(Paint.Style.FILL);
		//end smilefish
		
		// BEGIN: archie_huang@asus.com
		Bitmap bitmap = null;
		try {
            bitmap = Bitmap.createBitmap(width, rectHeight, Bitmap.Config.ARGB_8888);
            bitmap.setDensity(Bitmap.DENSITY_NONE);
            Canvas canvas = new Canvas(bitmap);
            
            //begin smilefish
            canvas.drawColor(context.getResources().getColor(R.color.timestamp_item_right_color));
            canvas.drawRect(rect_left, textPaint);
            canvas.drawBitmap(bitmap2, rect_top, rect_top, textPaint);
            
            TextPaint textPaint2 = new TextPaint();
            textPaint2.setColor(context.getResources().getColor(R.color.timestamp_item_text_color));
            textPaint2.setTextSize(textsize);
            //end smilefish

            StaticLayout layout = new StaticLayout(dateTime, textPaint2, text_width, Alignment.ALIGN_NORMAL,1.0F,0.0F,true);
            canvas.save();
            canvas.translate(text_height, 0);
            layout.draw(canvas);
            canvas.restore();

		} catch (OutOfMemoryError e) {
			Log.w(TAG, "[OutOfMemoryError] init() failed !!!");
		}
		// END: archie_huang@asus.com
		//End Dave.
		BitmapDrawable bmpDrawable = new BitmapDrawable(null, bitmap);
		mDrawable = bmpDrawable;
		mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(),
				mDrawable.getIntrinsicHeight());
	}

	@Override
	public Drawable getDrawable() {
		return mDrawable;
	}

	@Override
	public int getStart() {
		return mStart;
	}

	@Override
	public int getEnd() {
		return mEnd;
	}

	@Override
	public void setStart(int start) {
		mStart = start;
	}

	@Override
	public void setEnd(int end) {
		mEnd = end;
	}

	@Override
	public Serializable save() {
		NoteTimestampItemSavedData ntsd = new NoteTimestampItemSavedData();

		ntsd.mTime = mTime;
		ntsd.mStart = mStart;
		ntsd.mEnd = mEnd;
		ntsd.mOuterClassName = this.getClass().getName();
		ntsd.mOwner = mowner;// wendy

		return ntsd;
	}

	@Override
	public void load(Serializable s, PageEditor pe) {
		NoteTimestampItemSavedData ntsd = (NoteTimestampItemSavedData) s;
		mowner = ntsd.mOwner;// wendy
		mTime = ntsd.mTime;
		mStart = ntsd.mStart;
		mEnd = ntsd.mEnd;
		mfoneHeight = pe.getImageSpanHeight();

		init(false);
	}
	
	public void load(Serializable s, int height){
		NoteTimestampItemSavedData ntsd = (NoteTimestampItemSavedData) s;
		mowner = ntsd.mOwner;
		mTime = ntsd.mTime;
		mStart = ntsd.mStart;
		mEnd = ntsd.mEnd;
		mfoneHeight = height;
		init(false);
	}

	@Override
	public String getText() {
		return null;
	}

	public long getTimestamp() {
		return mTime;
	}

	public void setTimestamp(long time) {
		mTime = time;
		init(false);
	}
	
    //begin smilefish
    public int getWidth() {
        return mWidth;
    }
    //end smilefish

	@SuppressWarnings("serial")
	public static class NoteTimestampItemSavedData implements Serializable,
			NoteItem.NoteItemSaveData {

		public long mTime = 0;
		public int mStart = -1;
		public int mEnd = -1;
		public String mstampTitle = null;// wendy
		public long mOwner = 0L;// wendy

		public String mOuterClassName = null;

		@Override
		public String getOuterClassName() {
			return mOuterClassName;
		}
	}

	@Override
	public void itemSave(AsusFormatWriter afw) throws IOException {
		Log.d("Ryan", "itemSave mTime = " + mTime);
		afw.writeByteArray(SNF_NITEM_TIMESTAMP_BEGIN, null, 0, 0);
		afw.writeLong(SNF_NITEM_TIMESTAMP_TIME, mTime);

		afw.writeInt(SNF_NITEM_TIMESTAMP_POS_START, mStart);
		afw.writeInt(SNF_NITEM_TIMESTAMP_POS_END, mEnd);
		afw.writeByteArray(SNF_NITEM_TIMESTAMP_END, null, 0, 0);
		saveToDataBase();
	}

	// begin wendy
	private void saveToDataBase() {
		Cursor cur = null;
		try {

			// darwin
			Context context = SuperNoteApplication.getContext(); //NoteBookPickerActivity.getAppContext();
			ContentValues cvs = new ContentValues();
			cvs.put(MetaData.TimestampTable.CREATED_DATE, mTime);
			cvs.put(MetaData.TimestampTable.OWNER, mowner);
			cvs.put(MetaData.TimestampTable.POSITION, mStart);
			// darwin
			cur = context.getContentResolver().query(
					MetaData.PageTable.uri, null, "created_date = ?",
					new String[] { Long.toString(mowner) }, null);
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				if (!cur.isAfterLast()) {
					cvs.put(MetaData.TimestampTable.USER_ACCOUNT,
							cur.getLong(MetaData.PageTable.INDEX_USER_ACCOUNT));
				}
			}
			// darwin
			context.getContentResolver().insert(MetaData.TimestampTable.uri,
					cvs);
		} catch (Exception e) {
			Log.v("wendy", "e " + e.toString());
		}finally{
			if(cur != null)
				cur.close(); //smilefish add for memory leak
		}
	}

	// end wendy

	@Override
	public void itemLoad(AsusFormatReader afr) throws Exception {
		AsusFormatReader.Item item = afr.readItem();
		while (item != null) {
			switch (item.getId()) {
			case SNF_NITEM_TIMESTAMP_BEGIN:
				break;
			case SNF_NITEM_TIMESTAMP_TIME:
				mTime = item.getLongValue();
				break;
			case SNF_NITEM_TIMESTAMP_POS_START:
				mStart = item.getIntValue();
				break;
			case SNF_NITEM_TIMESTAMP_POS_END:
				mEnd = item.getIntValue();
				break;
			case SNF_NITEM_TIMESTAMP_END:
				init(false);
				return;
			default:
				Log.w(TAG, "Unknow id = 0x" + Integer.toHexString(item.getId()));
				break;
			}
			item = afr.readItem();
		}
	}

}
