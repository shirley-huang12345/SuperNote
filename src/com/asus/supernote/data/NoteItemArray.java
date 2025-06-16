package com.asus.supernote.data;

import java.util.ArrayList;

import com.asus.supernote.editable.noteitem.NoteItem;

/**
 * 
 * @author Allen_Lai@asus.com
 * add for multi-edit save and load content
 */
public class NoteItemArray {
	public static final short TEMPLATE_CONTENT_DEFAULT = -1;
	public static final short TEMPLATE_CONTENT_DEFAULT_NOTE_EDITTEXT = 0;
	public static final short TEMPLATE_CONTENT_MEETING_TOPIC = 1;
	public static final short TEMPLATE_CONTENT_METTING_STARTDATE = 2;
	public static final short TEMPLATE_CONTENT_MEETING_ENDDATE = 3;
	public static final short TEMPLATE_CONTENT_MEETING_STARTTIME = 4;
	public static final short TEMPLATE_CONTENT_MEETING_ENDTIME = 5;
	public static final short TEMPLATE_CONTENT_MEETING_ATTENDEE = 6;
	public static final short TEMPLATE_CONTENT_TRAVEL_TITLE = 7;
	public static final short TEMPLATE_CONTENT_TRAVEL_DATE = 8;
	public static final short TEMPLATE_CONTENT_TRAVEL_IMAGE = 9;
	public static final short TEMPLATE_CONTENT_TODO_EDIT = 10;
	public static final short TEMPLATE_CONTENT_TODO_CHECKBOX = 11;
	public static final short CONTENT_NOTEEDIT = 12;
	public static final short CONTENT_BOXEDIT = 13;
	public static final short TEMPLATE_CONTENT_TODO_PRIORITY = 14;
	public static final short TEMPLATE_CONTENT_TODO_MODIFY_TIME = 15;
	public static final short TEMPLATE_CONTENT_TODO_TITLE = 16;
	public static final short TEMPLATE_SEPERATER_TODO_NEW_ITEM_BEGIN = 17;
	public static final short TEMPLATE_SEPERATER_TODO_NEW_ITEM_END = 18;
	public static final short TEMPLATE_TODO_PAGE_FULL_FLAG = 19;
	
	public static final String OBJ = String.valueOf((char) 65532);
	private ArrayList<NoteItem> mNoteItems;
	private int mUnitCount = 0;//add for multi-edit search

	private short mTemplateItemType = TEMPLATE_CONTENT_DEFAULT;

	public int getUnitCount() {
		return mUnitCount;
	}
	public NoteItemArray(){
		mNoteItems = new ArrayList<NoteItem>();
		mTemplateItemType = TEMPLATE_CONTENT_DEFAULT;
	}
	
	public NoteItemArray(ArrayList<NoteItem> noteItems, short templateItemType) {
		this.mNoteItems = noteItems;
		this.mTemplateItemType = templateItemType;
	}

	public NoteItem[] getNoteItemArray(){
		NoteItem[] noteItems = new NoteItem[mNoteItems.size()];
		for(int i = 0;i<mNoteItems.size();i++){
			noteItems[i]=mNoteItems.get(i);
		}
		return noteItems;
	}
	
	public ArrayList<NoteItem> getNoteItems() {
		return mNoteItems;
	}

	public void setNoteItems(ArrayList<NoteItem> noteItems) {
		this.mNoteItems = noteItems;
	}

	public short getTemplateItemType() {
		return mTemplateItemType;
	}

	public void setTemplateItemType(short templateItemType) {
		this.mTemplateItemType = templateItemType;
	}
	
	/**
	 * add for multi-edit search
	 */
	public boolean canSearchItems(){
		if(mTemplateItemType == TEMPLATE_CONTENT_TRAVEL_IMAGE ||
				mTemplateItemType == TEMPLATE_CONTENT_TODO_CHECKBOX ||
				mTemplateItemType == TEMPLATE_CONTENT_TODO_MODIFY_TIME ||
				mTemplateItemType == TEMPLATE_CONTENT_TODO_PRIORITY ||
				mTemplateItemType == TEMPLATE_CONTENT_TODO_TITLE ||
				mTemplateItemType == TEMPLATE_TODO_PAGE_FULL_FLAG){
			return false;
		}
		else{
			return true;
		}
	}
	
	/**
	 * add for multi-edit search
	 */
	public void initUnitCount(){
		if(mNoteItems!=null && canSearchItems()){
			String totalString = mNoteItems.get(0).getText();
			int tempBegin = 0;
            int tempEnd = 0;
            mUnitCount = 0;
            while(tempEnd >= 0)
            {
            	//string + span + ...
            	tempEnd = totalString.indexOf(OBJ, tempBegin);
            	if(tempEnd > tempBegin)
            	{
            		mUnitCount+=2;
            		tempBegin = tempEnd + 1;
            	}
            	//span + ...
            	else if(tempEnd == tempBegin)
            	{
            		mUnitCount++;
            		tempBegin = tempEnd + 1;
            	}
            	else
            	{
            		//tempend must be -1
            		if(totalString.length() > tempBegin){
            			mUnitCount++;
            		}
            	}
            }
		}
	}
}
