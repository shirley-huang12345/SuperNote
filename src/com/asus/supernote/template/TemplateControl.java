package com.asus.supernote.template;

import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.NoteItem;

public interface TemplateControl {
	public void setContentType(short contentType);
	public short getContentType();
	public boolean LoadContent(short contentType,NoteItem[] noteItems);
	public NoteItemArray getNoteItem();
	public void setPageEditor(PageEditor mPageEditor, NotePage notePage);
	public void ClearControlState();
	public boolean isModified();
	public void setIsModified(boolean isModified);
}
