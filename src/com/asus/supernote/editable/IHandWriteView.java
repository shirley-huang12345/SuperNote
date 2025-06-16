package com.asus.supernote.editable;

import android.graphics.Paint;
import android.graphics.Rect;
/*
 * author:mars_li@asus.com
 * Description: 
 */

public interface IHandWriteView {
	static final float TOUCH_TOLERANCE = 3f;
	static final float SAVE_TOLERANCE = 4f;
	static final int INVALIDATE_MARGIN = 10;
	static final boolean DEBUG_TOUCH = false;
	static final int FONT_HEIGHT_OFFSET = 5;
	static final float ALGORITHM_HEIGHT_FACTOR = 0.8f;
	static final float ratioXtoY = 2.0f;
	static final int DEFAULT_SEND_TIMER = 400;
	static final int DRAWING_COUNT_DOWN_TIMER = 500;
	static final float BASELINE_SOLID_LINE_WIDTH = 4f;
	static final float BASELINE_DASH_LINE_WIDTH = 2f;
	static final float CLICK_TOLERANCE = 20f;
	static final float WRITING_BOUNDS_RATIO = 0.85f;
    static final float WRITING_SHIFT_RATIO = 0.25f;
	
	static final String OBJ = String.valueOf((char) 65532);
	
	public void setBaseLineMode(boolean showBaseLine);
	
	public void setInputPanel(IHandWritePanel panel);
	
	public void onTouchDown(float x, float y, float pressure);
	
	public void onTouchMove(float x, float y, float pressure);
	
	public void onTouchUp(float x, float y, float pressure);
	
	public void requestRender(Rect rect);
	
	public void setPaint(Paint paint);
	
    public void recycleBitmaps();
    
    public void clear();
    
    public CharSequence genResultSpannableString();
    
    public void loadTimer();
    
    public boolean isDeleteGesture();
    
    public void setEnable(boolean isEnable);
}
