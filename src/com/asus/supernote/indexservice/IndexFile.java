package com.asus.supernote.indexservice;

import java.util.ArrayList;

import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.inksearch.AsusSearch;

public class IndexFile {
	public static final int MAX_ARRAY_SIZE = 81920;
	public static final String OBJ = String.valueOf((char) 65532);
	
    //BEGIN: RICHARD
    private static ArrayList<NoteItem> getASCOrderedNoteItems(NoteItem[] items)
    {
        ArrayList<NoteItem> res = new ArrayList<NoteItem>(); 
        
        for(int i = 1; i < items.length ; i++)
        {
    		int j=0;
    		for(j = 0; j<res.size();j++)
    		{
    			if(res.get(j).getStart() > items[i].getStart())
    			{
    				break;
    			}
    		}
    		res.add(j, items[i]);
        }
        
        res.add(0,items[0]);
        return res;
    }
    
    private static ArrayList<NoteItem> getSubHandWriteItems(ArrayList<NoteItem> itemList,int arrayStartPos,int start,int end)
    {
    	if (itemList == null || itemList.size() == 0) 
    	{
    		return null;
    	}
    	
    	ArrayList<NoteItem> res = new ArrayList<NoteItem>(); 
    	for(int i = arrayStartPos; i<itemList.size();i++)
    	{
    		NoteItem item = itemList.get(i);
    		if(item.getStart() > start)
    		{
    			break;
    		}
    		
    		if(item.getStart() < start)
    		{
    			continue;
    		}
    		
    		if(item instanceof NoteHandWriteItem)
    		{
    			res.add(item);
    		}
    	}
    	return res;
    }
    
    private static boolean addStrokeToIndexFile(AsusSearch asusSearch,ArrayList<NoteItem> itemList,int arrayStartPos,int start)
    {
		ArrayList<NoteItem> tempspanItems = getSubHandWriteItems(itemList,arrayStartPos,start, start+1);
		if(tempspanItems == null || tempspanItems.size() == 0)
		{
			asusSearch.addString(" ");
		}
		else
		{
			NoteItem tempNoteItem = tempspanItems.get(tempspanItems.size() - 1);
        	if(tempNoteItem instanceof NoteHandWriteItem)
        	{
            	NoteHandWriteItem nhw = (NoteHandWriteItem)tempNoteItem;
            	asusSearch.addStroke(nhw);
        	}
        	else
        	{
        		return false;
        	}
		}
		
		return true;
    }
    
	public static boolean saveIndexFile(ArrayList<NoteItemArray> itemA,
			String filePath, int language) {
		if (itemA == null || itemA.size() == 0) {// Allen++
			return true;
		}
		try {
			AsusSearch asusSearch = new AsusSearch();// Richard+
			asusSearch.prepareIndexFile(filePath, language);// Richard+
			for (NoteItemArray item : itemA) {

				if (item.getNoteItemArray() == null
						|| item.getNoteItemArray().length == 0
						|| !item.canSearchItems()) {// RICHARD Allen++
					// return true;
					continue;
				}
				ArrayList<NoteItem> items = getASCOrderedNoteItems(item
						.getNoteItemArray());
				String totalString = items.get(0).getText();

				int arrayListIndex = 1;
				int tempBegin = 0;
				int tempEnd = 0;
				while (tempEnd >= 0) {
					tempEnd = totalString.indexOf(OBJ, tempBegin);
					if (tempEnd > tempBegin) {
						// string + span + ...
						String addinstring = totalString.substring(tempBegin,
								tempEnd).toString();
						asusSearch.addString(addinstring);

						addStrokeToIndexFile(asusSearch, items, arrayListIndex,
								tempEnd);
						tempBegin = tempEnd + 1;
					} else if (tempEnd == tempBegin) {
						// span + ...
						addStrokeToIndexFile(asusSearch, items, arrayListIndex,
								tempEnd);
						tempBegin = tempEnd + 1;
					} else {
						// tempend must be -1
						if (totalString.length() > tempBegin) {
							String addinstring = totalString.substring(
									tempBegin, totalString.length()).toString();
							asusSearch.addString(addinstring);
						}
					}

				}

			}

			asusSearch.generateFile();
			asusSearch.destroyEngine();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
    //END: RICHARD

}
