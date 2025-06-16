package com.asus.supernote.editable;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class NoteFrameLayout extends FrameLayout {
    OnMeasureListener mOnSizeChangedListener = null;//RICHARD

    public NoteFrameLayout(Context context) {
        super(context);
        
        setChildrenDrawingCacheEnabled(true);
        setChildrenDrawnWithCacheEnabled(true);
    }

    public NoteFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        setChildrenDrawingCacheEnabled(true);
        setChildrenDrawnWithCacheEnabled(true);
    }

    public NoteFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        setChildrenDrawingCacheEnabled(true);
        setChildrenDrawnWithCacheEnabled(true);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        if (mOnSizeChangedListener != null) {
        	mOnSizeChangedListener.onMeasure(
                    w,
                    h,
                    oldw,
                    oldh);
        }
        super.onSizeChanged(w,h,oldw,oldh);
    }
    
    public void setOnSizeChangeListner(OnMeasureListener listener) {
        mOnSizeChangedListener= listener;
    }
//BEGIN: RICHARD MODIFY FOR MULTIWINDOW
    public static interface OnMeasureListener {
        public void onMeasure(int widthMeasure, int heightMeasure, int width, int height);
    }
//END: RICHARD 
}
