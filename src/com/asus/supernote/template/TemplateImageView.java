package com.asus.supernote.template;

import java.util.ArrayList;

import com.asus.supernote.R;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteStringItem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TemplateImageView extends ImageView implements TemplateControl {
	private short mContentType = NoteItemArray.TEMPLATE_CONTENT_DEFAULT;
	private NotePage mNotePage = null;
	private boolean mIsModified = false;
	@Override
	public void setContentType(short contentType) {
		this.mContentType = contentType;		
	}

	@Override
	public short getContentType() {
		return mContentType;
	}	
	
	private PageEditor mPageEditor = null;
	private String mImageFilePath = null;
	
	public String getImageFilePath() {
		return mImageFilePath;
	}
	public void setImageFilePath(String imageFilePath) {
		this.mImageFilePath = imageFilePath;
		mIsModified = true;
		if(mPageEditor!=null){
			mPageEditor.onModified(true);
		}
	}
	public TemplateImageView(Context context) {
		super(context);
	}
	public TemplateImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public TemplateImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	@Override
	public void setPageEditor(PageEditor mPageEditor, NotePage notePage) {
		this.mPageEditor = mPageEditor;
		this.mNotePage = notePage;
	}
	
	@Override
	public void ClearControlState() {
		setImageFilePath(null);	
		if(mNotePage.isPhoneSizeMode() )
		{
			setImageResource(R.drawable.asus_diary_pic1);
		}
		else
		{
			setImageResource(R.drawable.pf_p_picture_l_pad);
		}
		mIsModified = false;
	}
	@Override
	public boolean LoadContent(short contentType, NoteItem[] noteItems) {
		if(contentType==mContentType){
	    	String imageFileName = noteItems[0].getText();
	    	if(imageFileName!=null&&imageFileName.trim().length()!=0){
	      	    BitmapFactory.Options option = new BitmapFactory.Options();
	            option.inJustDecodeBounds = true;
	            option.inJustDecodeBounds = false;
	            String path = null;
	            if(mPageEditor!=null){
	            	path = mPageEditor.getFilePath()+"/"+imageFileName;
	            }
	            else{
	            	path = mNotePage.getFilePath()+"/"+imageFileName;
	            }
	            Bitmap bmp = BitmapFactory.decodeFile(path, option);
	            if(bmp==null){
	        		if(mNotePage.isPhoneSizeMode() )
	        		{
	        			setImageResource(R.drawable.asus_diary_pic1);
	        		}
	        		else
	        		{
	        			setImageResource(R.drawable.pf_p_picture_l_pad);
	        		}
	            }else{
	                setImageBitmap(bmp);
	            }
	            setImageFilePath(imageFileName);
	    	}
	    	else{
	    		if(mNotePage.isPhoneSizeMode() )
	    		{
	    			setImageResource(R.drawable.asus_diary_pic1);
	    		}
	    		else
	    		{
	    			setImageResource(R.drawable.pf_p_picture_l_pad);
	    		}
	    	}
	    	return true;
		}
		else{
			return false;
		}
	}	
	
	@Override
	public NoteItemArray getNoteItem() {
    	NoteItem stringItem = new NoteStringItem(mImageFilePath==null?"":mImageFilePath);
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
