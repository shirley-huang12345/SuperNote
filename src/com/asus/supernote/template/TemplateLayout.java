package com.asus.supernote.template;

import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.editable.PageEditor;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class TemplateLayout extends FrameLayout{

	// BEGIN: Better
	private boolean mIsFirstDrawn = true;
	// END: Better
	private PageEditor mPageEditor = null;
	private NotePage mNotePage = null;
	public TemplateLayout(Context context) {
		super(context);
	}
	
	public TemplateLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TemplateLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}	
	
	// BEGIN: Better
	public void setFirstDrawn() {
		mIsFirstDrawn = true;
	}
	// END: Better
	
	@Override
	protected void dispatchDraw(Canvas arg0) {
		if (mIsFirstDrawn) { // Better
			mIsFirstDrawn = false;
			if (mPageEditor.getTemplateType() == MetaData.Template_type_todo) {
				mPageEditor.IsToDoPageFull(0);
			}
		}
		mPageEditor.drawScreen();
	}

	
	protected void dispatchGetDisplayList() {
		mPageEditor.getDoodleView().drawScreen();
	}
	
    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(getScrollX(), y);
    }
       
	public void drawToSurfaceView(Canvas canvas) {
		super.dispatchDraw(canvas);
	}	
	    
	private void initChildView(ViewGroup viewGroup) {
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			if (viewGroup.getChildAt(i) instanceof ViewGroup) {
				initChildView((ViewGroup) (viewGroup.getChildAt(i)));
			} else if (viewGroup.getChildAt(i) instanceof TemplateControl) {
				((TemplateControl) (viewGroup.getChildAt(i))).setPageEditor(mPageEditor, mNotePage);
			}

		}
	}
    
	public void initTemplateLayout(PageEditor mPageEditor, NotePage page) {
		this.mPageEditor = mPageEditor;
		mNotePage = page;
		initChildView(this);
	}    	
}
