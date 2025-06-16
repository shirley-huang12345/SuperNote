package com.asus.supernote.editable.noteitem;

import java.io.Serializable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;

import com.asus.supernote.R;
import com.asus.supernote.SuperNoteApplication;
import com.asus.supernote.editable.DrawableSpan;
import com.asus.supernote.editable.PageEditor;

public class NoteImageItem extends DrawableSpan implements NoteItem {

    final static int ALIGN_LEFT = 4;
	private Drawable mDrawable = null;
    private int mResourceId = 0;
    private int mfoneHeight = 0;
    private String mTextString = "";

    private int mStart = -1;
    private int mEnd = -1;
    private static final String TAG = "NoteImageItem";
    private static final Drawable FakeDrawablw = null;
    private int mWidth = 0; //smilefish
    private boolean mIsVideoItem = false; //smilefish

    public NoteImageItem() {
        super(FakeDrawablw, DynamicDrawableSpan.ALIGN_BASELINE);
    }

    // BEGIN: Better
    public NoteImageItem(Context context, boolean isVideo, int fontHeight, String text) {
        super(FakeDrawablw, DynamicDrawableSpan.ALIGN_BASELINE);

        //begin smilefish
        mIsVideoItem = isVideo;
        if(mIsVideoItem)
        	mResourceId = R.drawable.asus_insert_video_orange;
        else
        	mResourceId = R.drawable.asus_insert_audio_blue;
        //end smilefish
        
        mDrawable = genDrawable(context, mResourceId, fontHeight);

        mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
        
        mfoneHeight = fontHeight;
        mTextString = text;
        init(false);
    }
    
    private Drawable genDrawable(Context context, int resourceId, int fontHeight) {
        Resources res = context.getResources();

        Bitmap bitmap = BitmapFactory.decodeResource(res, resourceId);
        float scale = (float) fontHeight / (float) bitmap.getHeight();
        return new BitmapDrawable(res, Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scale), (int) fontHeight, false));
    }
    // END: Better

    public NoteImageItem(boolean isVideo, int fontHeight, String text) {
        super(FakeDrawablw, DynamicDrawableSpan.ALIGN_BASELINE);

        //begin smilefish
        mIsVideoItem = isVideo;
        if(mIsVideoItem)
        	mResourceId = R.drawable.asus_insert_video_orange;
        else
        	mResourceId = R.drawable.asus_insert_audio_blue;
        //end smilefish
        
        mDrawable = genDrawable(mResourceId, fontHeight);

        mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
        mfoneHeight = fontHeight;
        mTextString = text;
        init(false);
    }
    
    private void init(boolean isWidget) { //add parameter for widget case [Carol]
        if (mTextString == "" || mfoneHeight <= 0) {
            return;
        }
        // Log.d("Ryan", "dateTime = " + dateTime);
        
        //Begin Dave.To modify voice/video attacher UI. 
        Context context = SuperNoteApplication.getContext(); //Carol-Bug292338
        int text_height = context.getResources().getDimensionPixelSize(R.dimen.attacher_line_height); //smilefish
        float textsize = context.getResources().getDimension(
				R.dimen.timestamp_textsize);
        //Log.v("wendy", "textsize = " + textsize);
        int text_width = context.getResources().getDimensionPixelSize(R.dimen.attacher_text_width); //smilefish
        if(isWidget){ //decrease each value in widget [Carol]
            textsize -= context.getResources().getDimension(R.dimen.widget_diff_timestamp_textsize);
            text_width -= context.getResources().getDimensionPixelSize(R.dimen.widget_diff_attacher_text_width);
            text_height -= context.getResources().getDimensionPixelSize(R.dimen.widget_diff_attacher_line_height);
        }
        
        Bitmap bitmap2 = BitmapFactory.decodeResource(context.getResources(), mResourceId);
        int width = text_width + text_height;
        mWidth = width; //smilefish
        int rectHeight = text_height;
 
        //begin smilefish
        Rect rect_left = new Rect(0,0,text_height, text_height);
        int icon_size = bitmap2.getWidth();
        int rect_top = (text_height - icon_size) / 2;
        
        int leftColor = 0;
        int rightColor = 0;
        int textColor = 0;
        if(mIsVideoItem){
            leftColor = R.color.video_item_left_color;
            rightColor = R.color.video_item_right_color;
            textColor = R.color.video_item_text_color;
        }
        else{
            leftColor = R.color.audio_item_left_color;
            rightColor = R.color.audio_item_right_color;
            textColor = R.color.audio_item_text_color;
        }

		Paint textPaint = new Paint();
		textPaint.setColor(context.getResources().getColor(leftColor));
		textPaint.setStyle(Paint.Style.FILL);
		//end smilefish

        // BEGIN: archie_huang@asus.com
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(width, rectHeight, Bitmap.Config.ARGB_8888);
            bitmap.setDensity(Bitmap.DENSITY_NONE);
            Canvas canvas = new Canvas(bitmap);
            
            //begin smilefish
            canvas.drawColor(context.getResources().getColor(rightColor));
            canvas.drawRect(rect_left, textPaint);
            canvas.drawBitmap(bitmap2, rect_top, rect_top, textPaint);
            
            TextPaint textPaint2 = new TextPaint();
            textPaint2.setColor(context.getResources().getColor(textColor));
            textPaint2.setTextSize(textsize);
            //end smilefish

            StaticLayout layout = new StaticLayout(mTextString, textPaint2, text_width, Alignment.ALIGN_NORMAL,1.0F,0.0F,true);
            canvas.save();
            canvas.translate(text_height, 0);
            layout.draw(canvas);
            canvas.restore();

        }
        catch (OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] init() failed !!!");
        }
        // END: archie_huang@asus.com
        //End Dave.
        BitmapDrawable bmpDrawable = new BitmapDrawable(null, bitmap);
        mDrawable = bmpDrawable;
        mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
    }

    @Override
    public Drawable getDrawable() {
        return mDrawable;
    }

    public Serializable save() {
        NoteImageItemSavedData niisd = new NoteImageItemSavedData();
        niisd.mTextString = mTextString;
        niisd.mResourceId = mResourceId;
        
        niisd.mIsVideoItem = mIsVideoItem;

        niisd.mStart = mStart;
        niisd.mEnd = mEnd;

        niisd.mFontHeight = (short) mDrawable.getIntrinsicHeight();

        niisd.mOuterClassName = this.getClass().getName();
        return niisd;
    }

    public void load(Serializable s, PageEditor pe) {
        NoteImageItemSavedData niisd = (NoteImageItemSavedData) s;

        mDrawable = genDrawable(niisd.mResourceId, pe.getImageSpanHeight());
        mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());

        mResourceId = niisd.mResourceId;
        mfoneHeight = pe.getImageSpanHeight();
        
        //Begin Dave.To modify voice/video attacher UI. 
        mTextString = niisd.mTextString;
        //End Dave.
        mIsVideoItem = niisd.mIsVideoItem;
        mStart = niisd.mStart;
        mEnd = niisd.mEnd;
        
        init(false);
    }

    public void beComeUnTransparent() {
        mDrawable = genDrawable(mResourceId, mfoneHeight);
        mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
    }

    //Begin Allen
    @Override
   	public void setFontHeight(int fontHeight) {
    	mDrawable = genDrawable(mResourceId,fontHeight);
        mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
        mfoneHeight = fontHeight;
        init(true); //Carol
   	}
    //End Allen
    
    private Drawable genDrawable(int resourceId, int fontHeight) {
        // BEGIN: archie_huang@asus.com
        // To avoid NullPointerException
        Context context = SuperNoteApplication.getContext(); //MetaData.AppContext;
        if (context == null) {
            return null;
        }

        Resources res = context.getResources();
        // END: archie_huang@asus.com

        Bitmap bitmap = BitmapFactory.decodeResource(res, resourceId);
        float scale = (float) fontHeight / (float) bitmap.getHeight();
        return new BitmapDrawable(res, Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scale), (int) fontHeight, false));
    }

    public String getText() {
        return null;
    }

    public int getStart() {
        return mStart;
    }

    public int getEnd() {
        return mEnd;
    }

    public void setStart(int start) {
        mStart = start;
    }

    public void setEnd(int end) {
        mEnd = end;
    }
    
    //begin smilefish
    public int getWidth() {
        return mWidth;
    }
    //end smilefish
    
    @SuppressWarnings( "serial" )
    public static class NoteImageItemSavedData implements Serializable, NoteItem.NoteItemSaveData {

        public int mResourceId;

        public int mStart = -1;
        public int mEnd = -1;
        
        private String mTextString = "";
        private int mColor = 0;
        boolean mIsVideoItem = false;
        public short mFontHeight;

        public String mOuterClassName = null;

        public String getOuterClassName() {
            return mOuterClassName;
        }
    }
}
