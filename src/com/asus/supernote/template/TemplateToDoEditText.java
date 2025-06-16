package com.asus.supernote.template;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.editable.PageEditor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class TemplateToDoEditText extends TemplateEditText{
	@Override
	protected void onFocusChanged(boolean arg0, int arg1, Rect arg2) {
		super.onFocusChanged(arg0, arg1, arg2);
		setSelection(length());
		
		if(!arg0){
		    if(length() == 0){
	            if(mOnFocusLostListener != null){
	                mOnFocusLostListener.onFocusLost(this);
	            }
	        }
		}
		
	}

	private TemplateCheckBox mToDoCheckBox = null;
	private TemplateToDoUtility todoUtility = null;
	private Paint linePaint = new Paint();
	private TemplateToDoUtility.onModifiedListener mOnModifiedListener = null;
	private TemplateToDoUtility.onFocusLostListener mOnFocusLostListener = null;
	
	public void setOnFocusLostListener(
			TemplateToDoUtility.onFocusLostListener mOnFocusLostListener) {
		this.mOnFocusLostListener = mOnFocusLostListener;
	}
	
	//emmanual to fix bug 480760, 481625, 482010, 480771, 481405, 481239, 480683
	private int maxCharacterNum = 0; 
	@Override
	public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter){
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		if(maxCharacterNum == 0){
			maxCharacterNum = getContext().getResources().getInteger(R.integer.todo_title_max_character_num);
		}
		if(maxCharacterNum > 0 && getText().length() >= maxCharacterNum)
		{
			EditorActivity.showToast(getContext(), R.string.todo_title_max_character_num);
		}
	}

	public void setOnModifiedListener(
			TemplateToDoUtility.onModifiedListener mOnModifiedListener) {
		this.mOnModifiedListener = mOnModifiedListener;
	}
	
	@Override
	public void setIsModified(boolean mIsModified) {
		super.setIsModified(mIsModified);
		if(mOnModifiedListener!=null){
			mOnModifiedListener.onModified(mIsModified);
		}
	}
	
	public TemplateToDoEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(4f);
	}
	
	public void setCheckBox(TemplateCheckBox checkBox) {
		this.mToDoCheckBox = checkBox;
	}

	public boolean isChecked() {
		if(mToDoCheckBox != null){
			return mToDoCheckBox.isChecked();
		}
		else{
			return false;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(mContentType == NoteItemArray.TEMPLATE_CONTENT_TODO_EDIT){
			if(keyCode == KeyEvent.KEYCODE_DEL && getText().length()==0){
				todoUtility.DeleteToDoItem(this);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
    	if(mToDoCheckBox.isChecked()){
    		StaticLayout tempLayout = new StaticLayout(getText(), getPaint(), getMeasuredWidth(), android.text.Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
	        int lineCount = tempLayout.getLineCount();
	        Rect rt = new Rect();
	        for(int i=0;i<lineCount;i++){
		        float textWidth = tempLayout.getLineWidth(i);
		        getLineBounds(i, rt);
		        int baseline = rt.centerY();
                canvas.drawLine(0, baseline ,textWidth, baseline, linePaint);
            }
    	}     
	}
	
	public void initTodoEdit(PageEditor mPageEditor, NotePage notePage,TemplateToDoUtility todoUtility){
		this.todoUtility = todoUtility;
		setPageEditor(mPageEditor,notePage);
	}
	
	@Override
	public void initNoteEditText(PageEditor pageEditor, int textSize, boolean isSmallScreen) {
        super.initNoteEditText(pageEditor, textSize, isSmallScreen);
        mLineSpace = TEMPLETE_TODO_LINE_SPACE;
        setLineSpacing(0, mLineSpace);
        setPadding(0, (int) ((mLineSpace-1)*getLineHeight()), 0, 0);
	}
	
}
