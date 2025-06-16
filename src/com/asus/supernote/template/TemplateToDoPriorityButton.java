package com.asus.supernote.template;

import java.util.ArrayList;

import com.asus.supernote.R;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteStringItem;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class TemplateToDoPriorityButton extends ImageButton implements TemplateControl{

	private short mContentType = NoteItemArray.TEMPLATE_CONTENT_DEFAULT;
	private boolean mIsModified = false;
	private short mPriority = TemplateToDoUtility.TODO_PRIORITY_NORMAL;
	private TemplateToDoUtility.onModifiedListener mOnModifiedListener = null;
	
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
	
	@Override
	public boolean isModified() {
		return mIsModified;
	}

	@Override
	public void setIsModified(boolean isModified) {
		this.mIsModified = isModified;
	}
	
	public short getPriority() {
		return mPriority;
	}

	public void setPriority(short mPriority) {
		if(this.mPriority != mPriority){
			this.mPriority = mPriority;
			onPriorityChanged();
		}
	}

	private void onPriorityChanged(){
		int resid = -1;
		switch(mPriority){
		case TemplateToDoUtility.TODO_PRIORITY_HIGH:
			resid = R.drawable.asus_memo_high_ic;
			break;
		case TemplateToDoUtility.TODO_PRIORITY_LOW:
			resid = R.drawable.asus_memo_low_ic;
			break;
		default: 
			resid = R.drawable.asus_memo_normal_ic;
			break;
		}

		setBackgroundResource(resid);
		mIsModified = true;
		if(mOnModifiedListener != null){
			mOnModifiedListener.onModified(true);
		}
	}
	
	public TemplateToDoPriorityButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean LoadContent(short contentType, NoteItem[] noteItems) {
		if(contentType==mContentType){
			try{
				setPriority(Short.parseShort(noteItems[0].getText()));
			}
			catch(NumberFormatException e){
				e.printStackTrace();
			}
	    	return true;
		}
		else{
			return false;
		}
	}

	@Override
	public NoteItemArray getNoteItem() {
		NoteItem stringItem = new NoteStringItem(mPriority+"");
        ArrayList<NoteItem> noteItemsArrayList = new ArrayList<NoteItem>();
        noteItemsArrayList.add(stringItem);
        NoteItemArray noteItemArray = new NoteItemArray(noteItemsArrayList,mContentType);
        return noteItemArray;
	}

	@Override
	public void setPageEditor(PageEditor mPageEditor, NotePage notePage) {
		// TODO Auto-generated method stub
	}

	@Override
	public void ClearControlState() {
		setPriority(TemplateToDoUtility.TODO_PRIORITY_NORMAL);	
	}
}
