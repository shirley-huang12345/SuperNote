package com.asus.supernote.template;

import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.NoteItem;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class TemplateTextView extends TextView implements TemplateControl{
	private short mContentType = NoteItemArray.TEMPLATE_CONTENT_DEFAULT;
	@Override
	public void setContentType(short contentType) {
		this.mContentType = contentType;		
	}

	@Override
	public short getContentType() {
		return mContentType;
	}	
	
	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		return true;
	}

	public TemplateTextView(Context arg0) {
		super(arg0);
	}
	
	public TemplateTextView(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
	}
	
	public TemplateTextView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}
	
	@Override
	public void setPageEditor(PageEditor mPageEditor, NotePage notePage) {
		setTextColor(Color.BLACK);
	}

	@Override
	public void ClearControlState() {
		// TODO Auto-generated method stub
		
	}	
	@Override
	public boolean LoadContent(short contentType, NoteItem[] noteItems) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public NoteItemArray getNoteItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isModified() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setIsModified(boolean isModified) {
		// TODO Auto-generated method stub
		
	}
}
