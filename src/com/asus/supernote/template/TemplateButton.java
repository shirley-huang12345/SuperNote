package com.asus.supernote.template;

import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.NoteItem;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class TemplateButton extends Button implements TemplateControl{	
	private short mContentType = NoteItemArray.TEMPLATE_CONTENT_DEFAULT;
	@Override
	public void setContentType(short contentType) {
		this.mContentType = contentType;		
	}

	@Override
	public short getContentType() {
		return mContentType;
	}			
	

	public TemplateButton(Context context) {
		super(context);
	}
	
	public TemplateButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public TemplateButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}	
	
	@Override
	public void setPageEditor(PageEditor mPageEditor, NotePage notePage) {
	
	}

	@Override
	public void ClearControlState() {
		
	}

	@Override
	public boolean LoadContent(short contentType, NoteItem[] noteItems) {
		return false;
	}

	@Override
	public NoteItemArray getNoteItem() {
		return null;
	}

	@Override
	public boolean isModified() {
		return false;
	}

	@Override
	public void setIsModified(boolean isModified) {
		
	}
}
