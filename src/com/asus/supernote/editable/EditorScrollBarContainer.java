package com.asus.supernote.editable;

import com.asus.supernote.classutils.MethodUtils;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class EditorScrollBarContainer extends FrameLayout {
	//Begin Allen
	private float mVerticalScrollBarHeightFactor = 0;
	
    public float getVerticalScrollBarHeightFactor() {
		return mVerticalScrollBarHeightFactor;
	}

	public void setVerticalScrollBarHeightFactor(
			float mVerticalScrollBarHeightFactor) {
		this.mVerticalScrollBarHeightFactor = mVerticalScrollBarHeightFactor;
	}	

    //End Allen
	OnContainerSizeChangeListener mOnContainerSizeChangeListener = null;

    public EditorScrollBarContainer(Context context) {
        super(context);
    }

    public EditorScrollBarContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditorScrollBarContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnContainerSizeChangeListener(OnContainerSizeChangeListener listener) {
        mOnContainerSizeChangeListener = listener;
    }


	public static interface OnContainerSizeChangeListener {
        public void onSizeChanged(int w, int h, int oldw, int oldh);
        public void onScrollBarSizeChanged(int verticalScrollBarHeight);//Allen
    }
	//begin jason
	@Override
	public boolean dispatchTouchEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		if (MethodUtils.isEnableAirViewActionBarHint(getContext())) { //smilefish
			if (mPageEditor!=null&&mPageEditor.getEditorUiUtility().getmIsStylusInputOnly()) {
				if (arg0.getToolType(arg0.getActionIndex())!=MotionEvent.TOOL_TYPE_STYLUS ) {
					return true;
				}
			}
		}
		return super.dispatchTouchEvent(arg0);
	}
	private PageEditor mPageEditor=null;
	public void setPageEditor(PageEditor p){
		mPageEditor=p;
	}
	//end jason
}
