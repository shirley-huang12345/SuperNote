package com.asus.supernote.template;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.data.NoteItemArray;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

public class TemplateToDoTitleEditText extends TemplateEditText{
	
	private TemplateToDoUtility todoUtility = null; //smilefish
	private int maxCharacterNum = 50; //smilefish
	private boolean isFocusLost = false; //smilefish
	private CharSequence mHintText = null; //smilefish

	public TemplateToDoTitleEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		maxCharacterNum = context.getResources().getInteger(R.integer.todo_title_max_character_num); //smilefish
		
		initHintText(context);
	}
	
	//begin smilefish fix bug 384164/386615/381578
	private void initHintText(Context context){
		String html = getContext().getResources().getString(R.string.todo_title_task);
		html = html.replace("1", "<img src = 'image'/>");
		
		mHintText = Html.fromHtml(html, new Html.ImageGetter() {  
  
            @Override  
            public Drawable getDrawable(String source) {
                Drawable drawable = getResources().getDrawable(  
                		R.drawable.asus_ic_memo_enter);  
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),  
                        drawable.getIntrinsicHeight());    
                return drawable;  
            }  
        }, null);  
	}
	//end smilefish
	
	public CharSequence getHintText(){
		return mHintText;
	}

	@Override
	public void ClearControlState() {
		setIsFirstTimeLoad(true);

		setHint(mHintText);//smilefish

		setIsFirstTimeLoad(false);
		clearUndoRedoStack();
	}
	
	public void enterKeyProcess(){
		CharSequence taskStr = getText();
		if(taskStr != null && taskStr.length() > 0 && mContentType == NoteItemArray.TEMPLATE_CONTENT_TODO_TITLE){
			if(todoUtility.AddNewTodoItem(taskStr,false,this))
			{
				setText("");
				setHint(mHintText);
				clearUndoRedoStack();
			}
		}
	}

	//begin smilefish	
	@Override
	public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter){
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		CharSequence str = getText();
		if(maxCharacterNum > 0 && str.length() >= maxCharacterNum)
		{
			EditorActivity.showToast(getContext(), R.string.todo_title_max_character_num);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(mContentType == NoteItemArray.TEMPLATE_CONTENT_TODO_TITLE){
			int length = getText().length();
			if(keyCode == KeyEvent.KEYCODE_ENTER && todoUtility.getToDoListSize() > 0)
				isFocusLost = true;
			if(keyCode == KeyEvent.KEYCODE_ENTER && length > 0){
				CharSequence taskStr = getText();
				if(todoUtility.AddNewTodoItem(taskStr,false,this))
				{
					setText("");
					setHint(mHintText);
					clearUndoRedoStack();
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}	

	@Override
    protected void onFocusChanged(boolean arg0, int arg1, Rect arg2) {
        // TODO Auto-generated method stub
        super.onFocusChanged(arg0, arg1, arg2);
        if(!arg0){
            if(isFocusLost){
                isFocusLost = false;
                post(new Runnable(){

                    @Override
                    public void run() {
                        requestFocus();
                    }
                    
                });         
            }       
        }
    }

    public void initTodoEdit(TemplateToDoUtility todoUtility){
		this.todoUtility = todoUtility;
		mLineSpace = TEMPLETE_TODO_LINE_SPACE;
		setLineSpacing(0, mLineSpace); //fix bug 302700 by smilefish
		setPadding(0, (int) ((mLineSpace-1)*getLineHeight()), 0, 0);
	}	
	
	EnterInputConnection mConnection = null;
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN|EditorInfo.IME_ACTION_NONE);//fix TT406890
		return new EnterInputConnection(super.onCreateInputConnection(outAttrs),true);
    }
	   
	private class EnterInputConnection extends InputConnectionWrapper {

        public EnterInputConnection(InputConnection target, boolean mutable) {
	        super(target, mutable);
	    }

	    @Override
	    public boolean commitText(CharSequence text, int newCursorPosition) {
	        if(text.equals("\n")){
	        	if(todoUtility.getToDoListSize() > 0)
	        		isFocusLost = true;
	        	TemplateToDoTitleEditText.this.enterKeyProcess();
	            return true;   
	        }
	        return super.commitText(text, newCursorPosition);
	     }
    }
	
	//end smilefish
}
