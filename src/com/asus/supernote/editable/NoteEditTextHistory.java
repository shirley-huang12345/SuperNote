package com.asus.supernote.editable;

import java.util.Iterator;
import java.util.Stack;

import com.asus.supernote.editable.noteitem.NoteForegroundColorItem;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteTextStyleItem;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;

public class NoteEditTextHistory {

    private static final String TAG = "NoteEditTextHistory";

    private Stack<EditTextMemento> mUndoStack = new Stack<EditTextMemento>();
    private Stack<EditTextMemento> mRedoStack = new Stack<EditTextMemento>();

    private int mFinalCursorPos = -1;

    public void insertNew(Editable os, Editable cs, int start) {

        mUndoStack.push(new EditTextMemento(os, cs, start));
        // Log.d(TAG, "from os =\"" + os + "\" change to cs =\"" + cs + "\" start = " + start);

        if (mRedoStack.size() != 0) {
            mRedoStack.clear();
        }
    }

    public Editable undoWithoutRedo(Editable editable) {
        if (mUndoStack.isEmpty()) return null;

        EditTextMemento memento = mUndoStack.pop();

        return updateText(editable, memento.getStart(), memento.getChangedString().length(), memento.getOriginalString());
    }

    public Editable undo(Editable editable) {
        if (mUndoStack.isEmpty()) return null;

        EditTextMemento memento = mUndoStack.pop();
        mRedoStack.push(memento);

        return updateText(editable, memento.getStart(), memento.getChangedString().length(), memento.getOriginalString());
    }

    public Editable redo(Editable editable) {
        if (mRedoStack.isEmpty()) return null;

        EditTextMemento memento = mRedoStack.pop();
        mUndoStack.push(memento);

        return updateText(editable, memento.getStart(), memento.getOriginalString().length(), memento.getChangedString());
    }

    public int getFinalCursorPos() {
        return mFinalCursorPos;
    }

    public void clearStack() {
        if (mUndoStack.size() != 0) {
            mUndoStack.clear();
        }
        if (mRedoStack.size() != 0) {
            mRedoStack.clear();
        }
    }

    public void dumpUndoStack() {
        Iterator<EditTextMemento> iterator = mUndoStack.iterator();
        while (iterator.hasNext()) {
            EditTextMemento memento = iterator.next();
            Log.i(TAG, "----dump undo stack----\n" +
                    "startAt = " + memento.mStart +
                    " oriStr = " + memento.mOriginalString +
                    " changeStr = " + memento.mChangedString);
        }
    }

    private Editable updateText(Editable alleditable, int start, int length, Editable neweditable) {

        // If there are some customized enter, should remove them. Because they did not record in the history.
        int end = start + length;
        mFinalCursorPos = start + neweditable.length();
        Editable editable = new SpannableStringBuilder(alleditable);

        //emmanual to fix bug 428376 428427
        if(start <= end && end <= editable.length())
        	editable.replace(start, end, "");
        
        
        // BEGIN: Shane_Wang 2012-11-7
        if(neweditable.toString().indexOf(String.valueOf((char) 65532)) == -1) {
	        // BEGIN: Shane_Wang 2012-11-2
	        NoteItem[] noteItems = neweditable.getSpans(0, neweditable.length(), NoteItem.class);
	        //neweditable.clearSpans();
	        for (NoteItem item : noteItems) {
	        	if(item instanceof NoteTextStyleItem) {
	        		int itemStyle = ((NoteTextStyleItem)item).getStyle();
	        		int x = neweditable.getSpanStart(item);
	        		int y = neweditable.getSpanEnd(item);
	        		neweditable.removeSpan(item);
	        		NoteTextStyleItem textStyelItem = new NoteTextStyleItem(itemStyle);
	        	    neweditable.setSpan(textStyelItem, x, y, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	        		
	        	}
	        	else if(item instanceof NoteForegroundColorItem) {
	        		int itemColor = ((NoteForegroundColorItem)item).getForegroundColor();
	        		int x = neweditable.getSpanStart(item);
	        		int y = neweditable.getSpanEnd(item);
	        		neweditable.removeSpan(item);
	        		NoteForegroundColorItem foreColorItem = new NoteForegroundColorItem(itemColor);
	                neweditable.setSpan(foreColorItem, x, y, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	        	}
	
	        }
	        // END: Shane_Wang 2012-11-2
        }
        // END: Shane_Wang 2012-11-7
        
        editable.insert(start, neweditable);

        return editable;
    }

    public boolean isUndoStackEmpty() {
        return mUndoStack.isEmpty();
    }

    public boolean isRedoStackEmpty() {
        return mRedoStack.isEmpty();
    }

    public class EditTextMemento {

        private Editable mOriginalString = null;
        private Editable mChangedString = null;
        private int mStart = 0;

        public EditTextMemento(Editable os, Editable cs, int start) {
            mOriginalString = os;
            mChangedString = cs;
            mStart = start;
        }

        public Editable getOriginalString() {
            return mOriginalString;
        }

        public Editable getChangedString() {
            return mChangedString;
        }

        public int getStart() {
            return mStart;
        }
    }
}
