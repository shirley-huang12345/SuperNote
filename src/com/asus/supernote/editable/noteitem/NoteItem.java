package com.asus.supernote.editable.noteitem;

import java.io.Serializable;

import com.asus.supernote.editable.PageEditor;

public interface NoteItem {

    public int getStart();

    public int getEnd();

    public void setStart(int start);

    public void setEnd(int end);

    public Serializable save();

    public void load(Serializable s, PageEditor pe);

    public String getText();

    public static interface NoteItemSaveData {
        public String getOuterClassName();
    }
}
