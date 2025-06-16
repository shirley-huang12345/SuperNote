package com.asus.supernote.inksearch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.widget.EditText;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.NoteForegroundColorItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteBaselineItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.indexservice.NoteItemFile;
import com.visionobjects.myscript.hwr.InputItemLocator;
import com.visionobjects.myscript.hwr.InputRange;
import com.visionobjects.myscript.hwr.InputRangeElement;
import com.visionobjects.myscript.inksearch.OccurrenceIterator;

public class SearchResultItemInfo {

	public static final int MAX_ARRAY_SIZE = 81920;
	public static final String OBJ = String.valueOf((char) 65532);
	public float mScore = 0;
	public InputRange mInputRange;
	public short mSearchUnit = 0;
	public Long mPageID;
	public NoteBook mNotebook;
	public int mPageIndex = 0;
	public Boolean mIsFirst=true;
	public int mCount =1;
	public SearchResultItemInfo mFirstResultOFBook = this;
	
	public Editable mEditable = null;
	public Boolean mIsGetEditable = false;
	
	public Boolean mIsSpecialInfo = false; //for "NoteBooks" "Pages" "Personal"
	public int mSpecialKind = 0;
	public static final int SpecialKind_NORMAL= 0;
	public static final int SpecialKind_NOTEBOOKS = 1;
	public static final int SpecialKind_PAGES = 2;
	public static final int SpecialKind_PERSONAL = 3;
	public static final int SpecialKind_PAGES_CURRENT = 4;
	public static final int SpecialKind_NOTEBOOK_NAME = 5;
	//NoteBooks 1
	//Pages 2
	//Personal 3
	//Normal 0
	private Context mContext=null;
	private SearchResultViewAdapter mSearchResultViewAdapter= null;
	private static int mfontsize = -1;
	private static int mTextheight = -1;
	
	public SearchResultItemInfo(SearchResultViewAdapter srva,Context context,int special)
	{
		mSearchResultViewAdapter = srva;
		mIsSpecialInfo = true;
		mSpecialKind = special;	
		mContext=context;
	}
	
	public SearchResultItemInfo(SearchResultViewAdapter srva,int special,NoteBook notebook,int start,int length)
	{
		mSearchResultViewAdapter = srva;
		mIsSpecialInfo = true;
		mSpecialKind = special;
		
		mNotebook = notebook;
		String bookName = mNotebook.getTitle();
		mEditable = new SpannableStringBuilder(bookName);
		NoteForegroundColorItem fcs = new NoteForegroundColorItem(MetaData.SEARCH_TEXT_HEIGH_LIGHT_COLOR);
		mEditable.setSpan(fcs, start, start + length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);		
	}
	
	public String getSpecialString()
	{
		if(mIsSpecialInfo)
		{
			switch(mSpecialKind)
			{
			case SpecialKind_NOTEBOOKS:
				return mContext.getString(R.string.SearchResultItemInfo_NoteBooks);
			case SpecialKind_PAGES:
				return mContext.getString(R.string.SearchResultItemInfo_Pages);
			case SpecialKind_PERSONAL:
				return mContext.getString(R.string.SearchResultItemInfo_Personal);
			case SpecialKind_PAGES_CURRENT:
				return "";
			}
		}
		
		return "";
	}
	
	
	public Editable getEditable()
	{
		return mEditable;
	}
	
	public Boolean isInSameBook(SearchResultItemInfo info)
	{
		if(info.mIsSpecialInfo)
			return false;
		
		if(info.mNotebook.getCreatedTime().compareTo(mNotebook.getCreatedTime()) == 0)
		{
			return true;
		}
		
		return false;
	}
	
	public SearchResultItemInfo(SearchResultViewAdapter srva,Context context,NoteBook notebook,Long pageID,OccurrenceIterator occurrences)
	{
		mSearchResultViewAdapter= srva;
		mNotebook = notebook;
		mPageID = pageID;
		
		mSpecialKind = 0;
		
		mScore = occurrences.getScore();
		mInputRange = occurrences.getInputRange();
		mSearchUnit = occurrences.getSearchUnit();
		mContext=context;
		
		mEditable = new SpannableStringBuilder(mContext.getString(R.string.SearchResultItemInfo_Loading));
	}	
	
	public void setEditableToHolder(SearchViewHolder holder)
	{		
		holder.editText.setText(mEditable);//(CreateEditableFromFile(iteminfo));
	}
	
	public String getPageIndexString()
	{
		return mContext.getString(R.string.SearchResultItemInfo_Page_Space) + Integer.toString(mPageIndex);
	}
	
	public boolean GetEditable(){		
        try
        {
        	if(mfontsize == -1)
            {
            	mfontsize = (int) mContext.getResources().getDimension(R.dimen.page_search_result_fontsize);

                EditText et= new EditText(mContext);
                et.setTextSize(TypedValue.COMPLEX_UNIT_PX,mfontsize);
                mTextheight = et.getLineHeight();
                
            }
        	mEditable = CreateEditableFromFile();
        	if(mEditable==null){
        		return false;
        	}
        	else{
        		return true;
        	}
        }catch(Exception e)
        {
        	e.printStackTrace();
        	mEditable = new SpannableStringBuilder(mContext.getString(R.string.SearchResultItemInfo_Wrong_Index));
        	return false;
        }
	}
	
	    	// BEGIN: Richard
	    	private Editable CreateEditableFromFile() {

	    		ArrayList<NoteItemArray> items  = null;

	    		NoteItemFile itemFile = mSearchResultViewAdapter.getSpecificFile(mPageID,false);
	    		if(itemFile == null)
	    		{
	    			Editable editable = new SpannableStringBuilder(mContext.getString(R.string.SearchResultItemInfo_Loading));
	    			return editable;
	    		}
	    		else 			
	    		{
	    			items = itemFile.getNoteItem();
	    		}	    			    		
	    		
	    		if (items != null && items.size() > 0) {
	    			return setEditable(items);
	    		} else {
	    			Editable editable = new SpannableStringBuilder(mContext.getString(R.string.SearchResultItemInfo_Read_File_Error));
	    			return editable;
	    		}

	    	}

	    	private int deletUnusefulCharBeforeString(String inputstring) {
	    		int jumpcount = 0;
	    		for (int i = 0; inputstring.length() > i + 1; i++) {
	    			char temp = inputstring.charAt(i);
	    			if (temp == '\n' || temp == ' ' || temp == '\t') {
	    				jumpcount++;
	    				continue;
	    			} else {
	    				break;
	    			}
	    		}
	    		return jumpcount;
	    	}

	    	private Editable setEditable(ArrayList<NoteItemArray> allnoteItems) {
	    		
	    		InputRangeElement[] inputRangeElementArray = mInputRange.getElements();
	    		InputItemLocator locator = inputRangeElementArray[0].getFirst();
	    		int startUnit = locator.getUnitIndex();
	    		int startItem = locator.getItemIndex();
	    		locator = inputRangeElementArray[inputRangeElementArray.length - 1]
	    				.getLast();
	    		int stopUnit = locator.getUnitIndex();
	    		int stopitem = locator.getItemIndex();
	    		
	    		//Begin Allen
	    		NoteItem[] noteItems = null;
	    		int unitCount = 0;
	    		for(NoteItemArray items:allnoteItems){
	    			unitCount += items.getUnitCount();
	    			if(startUnit<unitCount){
	    				if(stopUnit<unitCount){
		    				noteItems = items.getNoteItemArray();
		    				startUnit -= (unitCount - items.getUnitCount());
		    				stopUnit -= (unitCount - items.getUnitCount());
	    					break;
	    				}
	    				else{
	    					return null;
	    				}
	    			}
	    		}
	    		//End Allen
	    		
	    		String str = noteItems[0].getText();


	    		String totalString = str;
	    		String tempResultString = "";

	    		int tempStart = 0;
	    		int tempEnd = 0;
	    		int tempUnitIndex = 0;
	    		int startIndex = -1; // show string. show string contains result string.

	    		int realBeginIndex = 0; // result start
	    		int realEndIndex = 0; // result end
	    		while (tempEnd >= 0) {
	    			tempEnd = totalString.indexOf(OBJ, tempStart);
	    			if (tempEnd > tempStart) {
	    				// string + span + ...
	    				if (tempUnitIndex > startUnit - 2
	    						&& tempUnitIndex < stopUnit + 2) {
	    					tempResultString += totalString.substring(tempStart,
	    							tempEnd + 1).toString();
	    					if (startIndex == -1) {
	    						startIndex = tempStart;
	    					}

	    					if (tempUnitIndex == startUnit) {
	    						// in string
	    						realBeginIndex = tempStart + startItem;
	    					} else if (tempUnitIndex == startUnit - 1) {
	    						// head write item
	    						realBeginIndex = tempEnd;
	    					}

	    					if (tempUnitIndex == stopUnit) {
	    						// in string
	    						realEndIndex = tempStart + stopitem + 1;
	    					} else if (tempUnitIndex == stopUnit - 1) {
	    						// head write item
	    						realEndIndex = tempEnd + 1;
	    					}
	    				}

	    				tempUnitIndex += 2;
	    				tempStart = tempEnd + 1;
	    			} else if (tempEnd == tempStart) {
	    				// span + ...
	    				if (tempUnitIndex > startUnit - 2
	    						&& tempUnitIndex < stopUnit + 2) {
	    					tempResultString += totalString.substring(tempStart,
	    							tempEnd + 1).toString();
	    					if (startIndex == -1) {
	    						startIndex = tempStart;
	    					}
	    				}

	    				if (tempUnitIndex == startUnit) {
	    					// in span
	    					realBeginIndex = tempStart;
	    				}

	    				if (tempUnitIndex == stopUnit) {
	    					// in span
	    					realEndIndex = tempEnd + 1;
	    				}

	    				tempUnitIndex++;
	    				tempStart = tempEnd + 1;
	    			} else {
	    				// tempend must be -1

	    				tempResultString += totalString.substring(tempStart,
	    						totalString.length()).toString();
	    				// must be string
	    				if (totalString.length() > tempStart) {
	    					if (tempUnitIndex == startUnit) {
	    						// in string
	    						realBeginIndex = tempStart + startItem;// startcomponent;
	    					}

	    					if (tempUnitIndex == stopUnit) {
	    						// in string
	    						realEndIndex = tempStart + stopitem + 1;
	    					}
	    				}

	    				if (startIndex == -1) {
	    					startIndex = tempStart;
	    				}
	    				break;
	    			}

	    			if (tempUnitIndex >= stopUnit + 2) {
	    				break;
	    			}

	    		}

	    		int jumpCount = deletUnusefulCharBeforeString(tempResultString);
	    		startIndex += jumpCount;

	    		tempResultString = tempResultString.substring(jumpCount,
	    				tempResultString.length()).toString();

	    		int firstEnter = tempResultString.lastIndexOf('\n',realBeginIndex - startIndex);
	    		if(firstEnter == -1)
	    		{
	    			firstEnter = 0;
	    		}
    			startIndex += firstEnter;
    			
    			if(realBeginIndex - startIndex > 5)
    			{
    				int tempIndex = realBeginIndex - 5;
    				firstEnter +=  (tempIndex-startIndex);
    				startIndex = tempIndex;
    			}
    			
	    		int lastEnter = tempResultString.indexOf('\n',realEndIndex);
	    		if (lastEnter == -1) {
	    			lastEnter = tempResultString.length();
	    		}
    			tempResultString = tempResultString.substring(firstEnter, lastEnter)
    					.toString();

	    		realBeginIndex -= startIndex;
	    		realEndIndex -= startIndex;
	    		if (realEndIndex - realBeginIndex > tempResultString.length()) {
	    			// need do something
	    			realEndIndex = realBeginIndex + tempResultString.length();
	    		}

	    		Editable editable = new SpannableStringBuilder(tempResultString);

	    		for (int i = 1; i < noteItems.length; i++) {
	    			if (noteItems[i].getStart() >= startIndex
	    					&& noteItems[i].getEnd() <= startIndex
	    							+ tempResultString.length()) {
	    				
	    				NoteItem currentNoteItem = null;
	    				
	    				if (noteItems[i] instanceof NoteHandWriteItem) {
	    					if (noteItems[i].getStart() - startIndex >= realBeginIndex
	    							&& noteItems[i].getEnd() - startIndex <= realEndIndex) {
	    	    				if(noteItems[i] instanceof NoteHandWriteBaselineItem)
	    	    				{
	    	    					Serializable data = ((NoteHandWriteBaselineItem)noteItems[i]).save();
	    	    					ByteArrayOutputStream b = new ByteArrayOutputStream();
	    	    		            try {
	    	    		                ObjectOutputStream obj = new ObjectOutputStream(b);
	    	    		                obj.writeObject(data);
	    	    		                
	    	    		                ByteArrayInputStream inputb = new ByteArrayInputStream(b.toByteArray());
	    	    		                ObjectInputStream inputobj = new ObjectInputStream(inputb);
	    	    		                Serializable serDrawInfo = (Serializable)inputobj.readObject();
	    	    		                data = serDrawInfo;
	    	    		            }catch (Exception e)
	    	    		            {
	    	    		            	e.printStackTrace();
	    	    		            }
	    	    					currentNoteItem = new NoteHandWriteBaselineItem();
	    	    					currentNoteItem.load(data,null);	
	    	    					((NoteHandWriteBaselineItem) currentNoteItem).setColor(MetaData.SEARCH_TEXT_HEIGH_LIGHT_COLOR);
	    	    				}
	    	    				else
	    	    				{
	    	    					Serializable data = ((NoteHandWriteItem)noteItems[i]).save();
	    	    					ByteArrayOutputStream b = new ByteArrayOutputStream();
	    	    		            try {
	    	    		                ObjectOutputStream obj = new ObjectOutputStream(b);
	    	    		                obj.writeObject(data);
	    	    		                
	    	    		                ByteArrayInputStream inputb = new ByteArrayInputStream(b.toByteArray());
	    	    		                ObjectInputStream inputobj = new ObjectInputStream(inputb);
	    	    		                Serializable serDrawInfo = (Serializable)inputobj.readObject();
	    	    		                data = serDrawInfo;
	    	    		            }catch (Exception e)
	    	    		            {
	    	    		            	e.printStackTrace();
	    	    		            }
	    	    					currentNoteItem = new NoteHandWriteItem();
	    	    					currentNoteItem.load(data,null);	 
	    	    					((NoteHandWriteItem) currentNoteItem).setColor(MetaData.SEARCH_TEXT_HEIGH_LIGHT_COLOR);
	    	    				}	    						
	    					}else
	    					{
	    						currentNoteItem = noteItems[i];
	    					}
	    				}
	    				else
	    				{
	    					currentNoteItem = noteItems[i];
	    				}

	    				editable.setSpan(currentNoteItem, noteItems[i].getStart()
	    						- startIndex, noteItems[i].getEnd() - startIndex,
	    						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    			}
	    			//BEGIN: RICHARD MODIFY FOR SPAN NOT FULL IN RESULT
	    			else if((noteItems[i].getEnd() >= startIndex && noteItems[i].getStart() <= startIndex+tempResultString.length()))
	    			{
	    				if(!(noteItems[i] instanceof NoteHandWriteItem))
	    				{
	    					int tempStyleStartindex = noteItems[i].getStart()- startIndex;
	    					int tempStyleEndindex = noteItems[i].getEnd() - startIndex;

	    					if(tempStyleStartindex < 0)
	    					{
	    						tempStyleStartindex = 0;
	    					}
	    					if(tempStyleEndindex > tempResultString.length())
	    					{
	    						tempStyleEndindex = tempResultString.length();
	    					}
		    				editable.setSpan(noteItems[i], tempStyleStartindex, tempStyleEndindex,
		    						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    				}
	    			}
	    			//END: RICHARD

	    		}

	    		setFontSize(editable);

	    		for (int i = realBeginIndex; i < realEndIndex; i++) {
	    			if (editable.subSequence(i, i + OBJ.length()).toString()
	    					.equals(OBJ))
	    				continue;
	    			NoteForegroundColorItem fcs = new NoteForegroundColorItem(MetaData.SEARCH_TEXT_HEIGH_LIGHT_COLOR);
	    			editable.setSpan(fcs, i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    		}

	    		mIsGetEditable = true;
	    		return editable;
	    	}

	    	public int getImageSpanHeight() {
	    		FontMetricsInt fontMetricsInt;
	    		Paint paint = new Paint();
	    		paint.setTextSize(mfontsize);
	    		fontMetricsInt = paint.getFontMetricsInt();
	    		return (int) (fontMetricsInt.descent * PageEditor.FONT_DESCENT_RATIO - fontMetricsInt.ascent);
	    	}

	    	public int getFullImageSpanHeight() {
	    		FontMetricsInt fontMetricsInt;
	    		Paint paint = new Paint();
	    		paint.setTextSize(mfontsize);
	    		fontMetricsInt = paint.getFontMetricsInt();
	    		return (int) (mTextheight + fontMetricsInt.descent
	    				* PageEditor.FONT_DESCENT_RATIO);
	    	}

	    	// BEGIN: archie_huang@asus.com
	    	public void setFontSize(Editable editable) {
	    		NoteHandWriteItem[] handwriteitems = editable.getSpans(0,
	    				editable.length(), NoteHandWriteItem.class);
	    		for (NoteHandWriteItem item : handwriteitems) {
	    			if (item instanceof NoteHandWriteBaselineItem) {
	    				item.setFontHeight(getImageSpanHeight());
	    			} else {
	    				item.setFontHeight(getFullImageSpanHeight());
	    			}
	    		}
	    	} // END: archie_huang@asus.com
	
}
