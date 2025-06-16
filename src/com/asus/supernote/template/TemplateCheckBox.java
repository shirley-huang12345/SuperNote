package com.asus.supernote.template;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteStringItem;

public class TemplateCheckBox extends CheckBox implements TemplateControl {
	private short mContentType = NoteItemArray.TEMPLATE_CONTENT_DEFAULT;
	private TemplateEditText todoEditText = null;
	private boolean mIsModified = false;
	private TemplateToDoUtility.onModifiedListener mOnModifiedListener = null;
	private boolean mIsLoadContent = false;
	
	public void setTodoEditText(TemplateEditText todoEditText) {
		this.todoEditText = todoEditText;
	}
	public void setOnModifiedListener(
			TemplateToDoUtility.onModifiedListener mOnModifiedListener) {
		this.mOnModifiedListener = mOnModifiedListener;
	}
	
	@Override
	public void setContentType(short contentType) {
		this.mContentType = contentType;		
	}

	@Override
	public short getContentType() {
		return mContentType;
	}	
	
	
	public TemplateCheckBox(Context context) {
		super(context);
	}

	public TemplateCheckBox(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TemplateCheckBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setPageEditor(final PageEditor mPageEditor, NotePage notePage) {
		setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1){
					todoEditText.setCursorVisible(false);
			        todoEditText.setActivated(false);
					todoEditText.setNoteEditTextEnabled(false);
					todoEditText.setFocusable(false);
				}
				else{
					todoEditText.setCursorVisible(true);
			        todoEditText.setActivated(true);
					todoEditText.setNoteEditTextEnabled(true);
					todoEditText.setFocusable(true);
					todoEditText.setFocusableInTouchMode(true);
					if(!mIsLoadContent){
						todoEditText.requestFocus();
					}
				}
				if (!mIsLoadContent) {
					mIsModified = true;
					if (mOnModifiedListener != null) {
						mOnModifiedListener.onModified(true);
					}
				}
			}			
		});
	}
	
	public void initTodoCheckBox(PageEditor mPageEditor, NotePage notePage,TemplateToDoUtility todoUtility){
		setPageEditor(mPageEditor,notePage);
	}
	
	@Override
	public void ClearControlState() {
		mIsLoadContent = true;
		setChecked(false);	
		mIsLoadContent = false;
		
		mIsModified = false;
	}

	@Override
	public boolean LoadContent(short contentType, NoteItem[] noteItems) {
		if(contentType == mContentType){
			String isChecked = noteItems[0].getText();
			if(isChecked.equals("true")){
				mIsLoadContent = true;
				setChecked(true);
				mIsLoadContent = false;
			}
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public NoteItemArray getNoteItem() {
    	NoteItem stringItem = new NoteStringItem(isChecked()?"true":"false");
    	NoteItem[] allnoteItem = new NoteItem[1];
    	allnoteItem[0] = stringItem;
        ArrayList<NoteItem> noteItemsArrayList = new ArrayList<NoteItem>();
        for(NoteItem item:allnoteItem){
        	noteItemsArrayList.add(item);
        }
        NoteItemArray noteItemArray = new NoteItemArray(noteItemsArrayList,mContentType);
        return noteItemArray;
	}

	@Override
	public boolean isModified() {
		return mIsModified;
	}

	@Override
	public void setIsModified(boolean isModified) {
		this.mIsModified = isModified;		
	}
}
