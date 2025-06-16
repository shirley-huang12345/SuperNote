package com.asus.supernote;

import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.editable.PageEditor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
/***
 * 
 * @author Jason
 *
 */
public class EditorActivityButtomButtonsContainer extends FrameLayout {

	public EditorActivityButtomButtonsContainer(Context context,
			AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	private PageEditor mPageEditor;
	public void setPageEditor(PageEditor pageEditor){
		mPageEditor=pageEditor;
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		if (MethodUtils.isPenDevice(getContext())) { //smilefish
			if (mPageEditor!=null&&mPageEditor.getEditorUiUtility().getmIsStylusInputOnly()) {
				if (arg0.getToolType(arg0.getActionIndex())!=MotionEvent.TOOL_TYPE_STYLUS ) {
					return true;
				}
			}
		}
		return super.dispatchTouchEvent(arg0);
	}
}
