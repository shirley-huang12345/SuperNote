package com.asus.supernote.data;

import android.content.Context;

import com.asus.supernote.R;

public class NotePageValue {
	
    //BEGIN: RICHARD    
	public static float getFontSize(Context context,int textSize, Boolean IsPhoneOrPad) {
		float fontSize = 0F;
		if (IsPhoneOrPad) {
			switch (textSize) {
			case MetaData.BOOK_FONT_BIG:
				fontSize = context.getResources().getDimension(
						R.dimen.phone_book_text_size_big);
				break;
			case MetaData.BOOK_FONT_NORMAL:
				fontSize = context.getResources().getDimension(
						R.dimen.phone_book_text_size_normal);
				break;
			case MetaData.BOOK_FONT_SMALL:
				fontSize = context.getResources().getDimension(
						R.dimen.phone_book_text_size_small);
				break;
			}
		} else {
			switch (textSize) {
			case MetaData.BOOK_FONT_BIG:
				fontSize = context.getResources().getDimension(
						R.dimen.pad_book_text_size_big);
				break;
			case MetaData.BOOK_FONT_NORMAL:
				fontSize = context.getResources().getDimension(
						R.dimen.pad_book_text_size_normal);
				break;
			case MetaData.BOOK_FONT_SMALL:
				fontSize = context.getResources().getDimension(
						R.dimen.pad_book_text_size_small);
				break;
			}
		}
		return fontSize;
	}
	
    public static int getLineCountLimited(Context context,int textSize, boolean isSmallScreen) {
    	int lineCountLimited = 0;
        if (isSmallScreen) {
            switch (textSize) {
                case MetaData.BOOK_FONT_BIG:
                	lineCountLimited = context.getResources().getInteger(R.integer.text_line_count_big_small_screen);
                    break;
                case MetaData.BOOK_FONT_NORMAL:
                	lineCountLimited = context.getResources().getInteger(R.integer.text_line_count_normal_small_screen);
                    break;
                case MetaData.BOOK_FONT_SMALL:
                	lineCountLimited = context.getResources().getInteger(R.integer.text_line_count_small_small_screen);
                    break;
            }
        } else {
            switch (textSize) {
                case MetaData.BOOK_FONT_BIG:
                	lineCountLimited = context.getResources().getInteger(R.integer.text_line_count_big);
                    break;
                case MetaData.BOOK_FONT_NORMAL:
                	lineCountLimited = context.getResources().getInteger(R.integer.text_line_count_normal);
                    break;
                case MetaData.BOOK_FONT_SMALL:
                	lineCountLimited = context.getResources().getInteger(R.integer.text_line_count_small);
                    break;
            }
        }
        return lineCountLimited;
    }
    
    public static int getPaddingTop(Context context,boolean isSmallScreen)
    {
    	int firstLineHeight = 0;
    	if(isSmallScreen)
    	{
    		firstLineHeight = context.getResources().getInteger(R.integer.first_line_height_small_screen);
    	}
    	else
    	{
    		firstLineHeight = context.getResources().getInteger(R.integer.first_line_height);
    	}
    	return firstLineHeight;
    }
    
    public static float getPadBookWidth(Context context)
    {
    	return context.getResources().getDimension(
				R.dimen.pad_book_width);
    }
    
    public static float getPhoneBookWidth(Context context)
    {
    	return context.getResources().getDimension(
				R.dimen.phone_book_width);
    }
    
    public static float getBookWidth(Context context,Boolean isPhoneOrPadBook)
    {
    	if(isPhoneOrPadBook)
    	{
    		return getPhoneBookWidth(context);
    	}
    	else
    	{
    		return getPadBookWidth(context);
    	}
    }
    
}
