package com.asus.supernote.textsearch;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.EditText;

import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.editable.noteitem.NoteItem;

public class TextInfoSearch implements TextSearch {
	private Context mContext;
	private BookCase mBookCase;

	public TextInfoSearch(Context context) {
		mContext = context;
		mBookCase = BookCase.getInstance(mContext);
	}

	@Override
    public void prepareSearch() {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public void savePageFile(long pageId) {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public void updatePageFile(long pageId) {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public Editable searchInPage(String str) {
	    // TODO Auto-generated method stub
	    return null;
    }

	@Override
    public String getPageText(long pageId) {
	    // TODO Auto-generated method stub
	    return null;
    }

	@Override
    public List<Editable> getEditable(long bookId, long pageId) {
	    // TODO Auto-generated method stub
		List<Editable> editables = new ArrayList<Editable>();
		if(getNoteItemArray(bookId, pageId) == null){
	        return editables;
		}
		for(NoteItemArray item:getNoteItemArray(bookId, pageId)){
			editables.add(loadNoteItems(item));
		}
        return editables;
    }

	private Editable loadNoteItems(NoteItemArray allNoteItems){
        if (allNoteItems == null) {
            return new EditText(mContext).getText();
        }
		NoteItem[] noteItems = allNoteItems.getNoteItemArray();
		if (noteItems == null || noteItems.length == 0) {
            return null;
        }
		String str = noteItems[0].getText();
        Editable editable = new SpannableStringBuilder(str);

        for (int i = 1; i < noteItems.length; i++) {
            if (noteItems[i].getStart() < 0 || noteItems[i].getEnd() < 0
                    || noteItems[i].getStart() > editable.length() ) {
                continue;
            }
            
            if(noteItems[i].getEnd() > editable.length()) {
            	noteItems[i].setEnd(editable.length());
            }
            
            editable.setSpan(noteItems[i], noteItems[i].getStart(), noteItems[i].getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return editable;
	}

    public List<NoteItemArray> getNoteItemArray(long bookId, long pageId) {
		NotePage mNotePage = mBookCase.getNoteBook(bookId).getNotePage(pageId);
		PageDataLoader loader = new PageDataLoader(mContext);
		loader.loadNoteItems(mNotePage);
		ArrayList<NoteItemArray> allNoteItems = loader.getAllNoteItems();
        if (allNoteItems == null || allNoteItems.size() == 0) {
            return null;
        }
        List<NoteItemArray> items = new ArrayList<NoteItemArray>();
    	for(NoteItemArray item:allNoteItems){
			if (item.getTemplateItemType() == NoteItemArray.TEMPLATE_CONTENT_DEFAULT_NOTE_EDITTEXT
			        || item.getTemplateItemType() == NoteItemArray.TEMPLATE_CONTENT_MEETING_TOPIC
			        || item.getTemplateItemType() == NoteItemArray.TEMPLATE_CONTENT_MEETING_ATTENDEE
			        || item.getTemplateItemType() == NoteItemArray.TEMPLATE_CONTENT_TRAVEL_TITLE
			        || item.getTemplateItemType() == NoteItemArray.TEMPLATE_CONTENT_TODO_TITLE
			        || item.getTemplateItemType() == NoteItemArray.TEMPLATE_CONTENT_TODO_EDIT) {
    			items.add(item);
    		}
    	}
	    return items;
    }
}
