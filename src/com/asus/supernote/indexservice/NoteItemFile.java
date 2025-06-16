package com.asus.supernote.indexservice;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.asus.supernote.data.AsusFormat;
import com.asus.supernote.data.AsusFormatWriter;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteImageItem;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteStringItem;

public class NoteItemFile {
	public static final int MAX_ARRAY_SIZE = 81920;
	public static final String OBJ = String.valueOf((char) 65532);
	private Long mBookID;
	private Long mPageID;
	private String mFilePath;
	public Boolean mIsLoaded = false;
	private ArrayList<NoteItemArray> mNoteItem = null;
	private Context mContext= null;
	private PageDataLoader mPageDataLoader = null;
	
	public NoteItemFile(Context Context,Long bookID,Long pageID)
	{
		mBookID = bookID;
		mPageID = pageID;
		
		mFilePath = MetaData.DATA_DIR
				+ mBookID.toString() + "/"
				+ mPageID.toString() + "/" 
				+ MetaData.NOTE_ITEM_PREFIX;
		
		mIsLoaded = false;
		mContext = Context;
		
		mPageDataLoader = new PageDataLoader(mContext);
	}
	
	public Long getPageID()
	{
		return mPageID;
	}
	
	public Boolean getIsLoaded()
	{
		return mIsLoaded;
	}
	
	public ArrayList<NoteItemArray> getNoteItem()
	{
		if(mIsLoaded)
		{
			return mNoteItem;
		}
		
		mNoteItem = loadNoteItem();
		mIsLoaded = true;
		
		return mNoteItem;
	}
	
	public ArrayList<NoteItemArray> loadNoteItem() {
		Cursor cursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null,
				"created_date = ?", new String[] { Long.toString(mPageID) }, null);
		if (cursor.moveToFirst()) {
			NotePage notePage = new NotePage(mContext, cursor);
			if(cursor != null)
				cursor.close();  //smilefish fix memory leak
			return mPageDataLoader.getAllNoteItemsForSearch();// Allen
		}
		if(cursor != null)
			cursor.close();  //smilefish fix memory leak
		return new ArrayList<NoteItemArray>();
	}
	// END: Richard
    
    public String getFolderPath() {
        File dir = new File(MetaData.DATA_DIR);
        dir.mkdir();
        File bookDir = new File(MetaData.DATA_DIR, Long.toString(mBookID));
        bookDir.mkdir();
        File pageDir = new File(bookDir, Long.toString(mPageID));
        pageDir.mkdir();
        return pageDir.getPath();
    }
    
    //darwin
    
    public ArrayList<NoteItemArray> loadNoteAndTemplateItems() {
		Cursor cursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null,
				"created_date = ?", new String[] { Long.toString(mPageID) }, null);
		cursor.moveToFirst();
		NotePage notePage = new NotePage(mContext,cursor);
		if(cursor != null)
			cursor.close();
		return mPageDataLoader.getAllNoteItems();
    }
    public ArrayList<NoteItemArray> loadAllNoteAndTemplateItems()
    {
    	return loadNoteAndTemplateItems();
    }
    //darwin
    
    public NoteItem[] loadAllNoteItem() {
    	ArrayList<NoteItemArray> list = loadNoteItem();
    	if(list == null ||list.get(0) ==null)
    	{
    		return null;
    	}
    	else
    	{
    		return list.get(0).getNoteItemArray();
    	}
    }
    
    public Editable loadNoteEditText(NoteItem[] noteItems) {
        String str = noteItems[0].getText();
        Editable editable = new SpannableStringBuilder(str);

        for (int i = 1; i < noteItems.length; i++) {
            if (noteItems[i].getStart() < 0 || noteItems[i].getEnd() < 0
                    || noteItems[i].getStart() > editable.length() || noteItems[i].getEnd() > editable.length()) {

                continue;
            }
            editable.setSpan(noteItems[i], noteItems[i].getStart(), noteItems[i].getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return editable;
    }

    public ArrayList<NoteHandWriteItem> getOrderedNoteHandWriteItems(NoteItem[] noteItems)
    {
        ArrayList<NoteHandWriteItem> res = new ArrayList<NoteHandWriteItem>(); 
        
        for(int i = 1; i < noteItems.length ; i++)
        {
        	if(noteItems[i] instanceof NoteHandWriteItem)
        	{
        		int j=0;
        		for(j = 0; j<res.size();j++)
        		{
        			if(res.get(j).getStart() > noteItems[i].getStart())
        			{
        				break;
        			}
        		}
        		res.add(j, (NoteHandWriteItem)noteItems[i]);
        	}
        }
        
        return res;
    }
    
	    //add by wendy
	private void deleteTimestamps(Context context)
	{
		try{
			context.getContentResolver().delete(MetaData.TimestampTable.uri, "owner = ? ", new String[]{Long.toString(mBookID)});
		}catch(Exception e)
		{
			Log.v("wendy","e " + e.toString());
		}
	}//add by wendy
	
	
	//darwin
	public boolean saveNoteItemsAndTemplate(Context context, ArrayList<NoteItemArray> noteItemsSave) {
    	if (noteItemsSave == null || noteItemsSave.size() == 0) {//RICHARD
        	return false;
        }
        try {
        	deleteTimestamps(context);//add by wendy
        	File file = new File(mFilePath);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            AsusFormatWriter afw = new AsusFormatWriter(bos);
            
            for(NoteItemArray nia : noteItemsSave )
            {
            	afw.writeByteArray(AsusFormat.SNF_NITEM_BEGIN, null, 0, 0);
            	afw.writeShort(AsusFormat.SNF_NITEM_VERSION, MetaData.ITEM_VERSION);
            	afw.writeShort(AsusFormat.SNF_NITEM_TEMPLATE_ITEM_TYPE, nia.getTemplateItemType());
	            for (NoteItem item : nia.getNoteItemArray()) {
	                if (item instanceof NoteImageItem) {
	                    continue;
	                }
	                if (item instanceof AsusFormat) {
	                    AsusFormat af = (AsusFormat) item;
	                    af.itemSave(afw);
	                }
	            }
	            afw.writeByteArray(AsusFormat.SNF_NITEM_END, null, 0, 0);
            }
            updatePageIndexFlagInDB(context,MetaData.INDEX_FILE_CREATE_NOT);
            bos.close();
            fos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
	
    public ArrayList<NoteItemArray> getASCOrderedNoteItems(ArrayList<NoteItemArray> arraylistitems)
    {
    	if (arraylistitems == null || arraylistitems.size() == 0) 
    	{
    		return null;
    	}
    	ArrayList<NoteItemArray> arraylistitemsOut = new ArrayList<NoteItemArray>();
    	for(NoteItemArray nia : arraylistitems)
    	{
	        ArrayList<NoteItem> res = new ArrayList<NoteItem>(); 
	        
	        for(int i = 1; i < nia.getNoteItemArray().length ; i++)
	        {
	        	res.add(i-1, nia.getNoteItemArray()[i]);
	        }
	        
	        res.add(0,nia.getNoteItemArray()[0]);
	        
	        NoteItemArray noteitemarray = new NoteItemArray();
	        noteitemarray.setNoteItems(res);
	        noteitemarray.setTemplateItemType(nia.getTemplateItemType());
	        arraylistitemsOut.add(noteitemarray);
    	}
        return arraylistitemsOut;
    }
	//darwin
    public boolean saveNoteItems(Context context,ArrayList<NoteItem> items) {//RICHARD NoteItem[] ->ArrayList<NoteItem>
    	if (items == null || items.size() == 0) {//RICHARD
        	return false;
        }
        try {
        	deleteTimestamps(context);//add by wendy
        	File file = new File(mFilePath);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            AsusFormatWriter afw = new AsusFormatWriter(bos);
            afw.writeByteArray(AsusFormat.SNF_NITEM_BEGIN, null, 0, 0);
            afw.writeShort(AsusFormat.SNF_NITEM_VERSION, MetaData.ITEM_VERSION);
            for (NoteItem item : items) {
                if (item instanceof NoteImageItem) {
                    continue;
                }
                if (item instanceof AsusFormat) {
                    AsusFormat af = (AsusFormat) item;
                    af.itemSave(afw);
                }
            }
            afw.writeByteArray(AsusFormat.SNF_NITEM_END, null, 0, 0);

            updatePageIndexFlagInDB(context,MetaData.INDEX_FILE_CREATE_NOT);
            bos.close();
            fos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    
	//BEGIN: RICHARD 
    private void updatePageIndexFlagInDB(Context context,int status)
    {
        ContentValues cvUpdate = new ContentValues();
        cvUpdate.put(MetaData.PageTable.IS_INDEXED, status);
        context.getContentResolver().update(MetaData.PageTable.uri, cvUpdate, MetaData.PageTable.CREATED_DATE + "=" + mPageID, null);
    }
	//END: RICHARD
    
    public NoteItem[] getNoteItemFromEditable(Editable editable,int start, int end) {
        // text part
        NoteItem stringItem = new NoteStringItem(editable.toString());

        // span part
        NoteItem[] spanItems = editable.getSpans(0, editable.length(), NoteItem.class);
        NoteItem[] allnoteItem = new NoteItem[spanItems.length + 1];

        allnoteItem[0] = stringItem;
        for (int i = 0; i < spanItems.length; i++) {
            int spanstart = editable.getSpanStart(spanItems[i]);
            int spanend = editable.getSpanEnd(spanItems[i]);

            spanItems[i].setStart(spanstart);
            spanItems[i].setEnd(spanend);
            allnoteItem[i + 1] = spanItems[i];
        }

        return allnoteItem;
    }

    //BEGIN: RICHARD
    public ArrayList<NoteItem> getASCOrderedNoteItems(NoteItem[] items)
    {
    	if (items == null || items.length == 0) 
    	{
    		return null;
    	}
    	
        ArrayList<NoteItem> res = new ArrayList<NoteItem>(); 
        
        for(int i = 1; i < items.length ; i++)
        {
        	res.add(i-1, items[i]);
        }
        
        res.add(0,items[0]);
        return res;
    }

}
