package com.asus.supernote.ui;

import com.asus.supernote.R;
import com.asus.supernote.HoverHintImp;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class ColorPickerSnapView extends View{
	private int mCurrentX = 0, mCurrentY = 0;
	private Bitmap snapBitmap = null;
	private Bitmap mColorBitmap = null;
	private Paint mPaint;
	private Matrix matrix;

	public ColorPickerSnapView(Context c, AttributeSet attrSet) {
		super(c, attrSet);
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		matrix = new Matrix();

		Drawable d = c.getResources().getDrawable(R.drawable.asus_color_selector);
		mColorBitmap = ((BitmapDrawable)d).getBitmap();
		HoverHintImp hover=new HoverHintImp(c);
		hover.setHoverImageRes(R.drawable.content_dropper);
		this.setOnHoverListener(hover);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(snapBitmap, 0, 0, mPaint);

		if(mCurrentX > 0 && mCurrentY > 0){
			int width = mColorBitmap.getWidth() / 2;
			int height = mColorBitmap.getHeight() / 2;
			matrix.setTranslate(mCurrentX - width, mCurrentY - height);
			canvas.drawBitmap(mColorBitmap, matrix, mPaint);
		}
	}
	
	public void setBitmap(Bitmap bitmap)
	{
		mCurrentX = 0;
		mCurrentY = 0;
		snapBitmap = bitmap;
		invalidate();
	}
	
	public void setColorXY(int x, int y)
	{
		mCurrentX = x;
		mCurrentY = y;
		invalidate();
	}

}
