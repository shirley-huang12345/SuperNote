package com.asus.supernote.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;

public class CoverHelper {
	
	public static Bitmap DefaultCover = null;
	
	public static void initDefaultCoverBitmap(int color, int line,Resources res){
		if(DefaultCover!=null){
			DefaultCover.recycle();
			DefaultCover = null;
		}
		DefaultCover = getDefaultCoverBitmap(color, line,res);
	}
	
	public static Bitmap getDefaultCoverBitmap(int color, int line,Resources res){
		Bitmap cover = null;
        try{
    		switch (color) {
            case MetaData.BOOK_COLOR_WHITE:
                if (line == MetaData.BOOK_GRID_LINE) {
                    {
                        cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_line_white_local);
                    }
                }
                else if(line == MetaData.BOOK_GRID_GRID){
                    {
                        cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_grid_white_local);
                    }
                }
                else if(line == MetaData.BOOK_GRID_BLANK){
                	cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_blank_white_local);
                }
                break;
            case MetaData.BOOK_COLOR_YELLOW:
                if (line == MetaData.BOOK_GRID_LINE) {
                    {
                        cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_line_yellow_local);
                    }
                }
                else if(line == MetaData.BOOK_GRID_GRID){
                    {
                        cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_grid_yellow_local);
                    }
                }
                else if(line == MetaData.BOOK_GRID_BLANK){
                	cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_blank_yellow_local);
                }
                break;
            default:
                {
                    cover = BitmapFactory.decodeResource(res, R.drawable.asus_supernote_cover2_line_white_local);
                }
                break;
    		}
        }
        catch (Exception e) {
			e.printStackTrace();
		}
        return cover;
	}	
	/***
	 * by jason
	 * @param context
	 * @param coverResID 
	 * @param CoverColor
	 * @return
	 */
	public static Drawable createGradientColorAndCover(Context context,int coveredResID,int CoverColor){
		final Resources res=context.getResources();
		Bitmap bitmap = BitmapFactory.decodeResource(res, coveredResID);
		if (bitmap==null) {
			return null;
		}
		final int length=bitmap.getWidth();
		final int height=bitmap.getHeight();
		int[] shape=new int[length*height];
		int[] colors=new int[length*height];
		bitmap.getPixels(shape, 0, length, 0, 0, length, height);
		int color=CoverColor;
		for (int i=0 ;i<length*height;i++) {
			float percent=((float)i%length/length)*0xff;
			int alpha=((int)percent<<6*4);
			alpha&=shape[i] & 0xFF000000;
			colors[i]=(alpha)|(color&0x00FFFFFF);
		}
		Bitmap newbitmap =  Bitmap.createBitmap(length, height, Config.ARGB_8888);
  		newbitmap.setPixels(colors, 0, length, 0, 0, length, height);
  		Bitmap fooBitmap=Bitmap.createBitmap(length, height, Config.ARGB_8888);
  		Canvas canvas=new Canvas(fooBitmap);
  		Paint paint=new Paint();
  		canvas.drawBitmap(bitmap, 0, 0, paint);
  		canvas.drawBitmap(newbitmap, 0, 0, paint);
  		newbitmap.recycle();
  		bitmap.recycle();
  		return new BitmapDrawable(res, fooBitmap);
	}
	//end jason
	
	//begin smilefish
	public static Drawable createRoundColorBackground(Context context,int coveredResID,int CoverColor){
		final Resources res=context.getResources();
		Bitmap bitmap = BitmapFactory.decodeResource(res, coveredResID);
		if (bitmap==null) {
			return null;
		}
		final int length=bitmap.getWidth();
		final int height=bitmap.getHeight();

  		Bitmap fooBitmap=Bitmap.createBitmap(length, height, Config.ARGB_8888);
  		Canvas canvas=new Canvas(fooBitmap);
  		Paint paint=new Paint();
  		canvas.drawColor(CoverColor);
  		canvas.drawBitmap(bitmap, 0, 0, paint);
  		bitmap.recycle();
  		return new BitmapDrawable(res, fooBitmap);
	}
	//end smilefish
}
