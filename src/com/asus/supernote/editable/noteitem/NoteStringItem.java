package com.asus.supernote.editable.noteitem;

import java.io.IOException;
import java.io.Serializable;

import com.asus.supernote.data.AsusFormat;
import com.asus.supernote.data.AsusFormatReader;
import com.asus.supernote.data.AsusFormatWriter;
import com.asus.supernote.editable.PageEditor;

import android.util.Log;

public class NoteStringItem implements NoteItem, AsusFormat {

    private static final String TAG = "NoteStringItem";
    private String mString = null;

    public NoteStringItem() {

    }

    public NoteStringItem(String str) {
        mString = str;
    }

    public Serializable save() {
        NoteStringItemSavedData nsisd = new NoteStringItemSavedData();

        nsisd.mString = mString;
        nsisd.mOuterClassName = this.getClass().getName();

        return nsisd;
    }

    public void load(Serializable s, PageEditor pe) {
        mString = ((NoteStringItemSavedData) s).mString;
    }

    public String getText() {
        return mString;
    }

    public int getStart() {
        return -2;
    }

    public int getEnd() {
        return -2;
    }

    public void setStart(int start) {
        Log.w("NoteStringItem", "You should not setStart to StrintItem!");
    }

    public void setEnd(int end) {
        Log.w("NoteStringItem", "You should not setEnd to StrintItem!");
    }

    @SuppressWarnings( "serial" )
    public static class NoteStringItemSavedData implements Serializable, NoteItem.NoteItemSaveData {

        public String mString = null;

        public String mOuterClassName = null;

        public String getOuterClassName() {
            return mOuterClassName;
        }
    }

    @Override
    public void itemSave(AsusFormatWriter afw) throws IOException {
        afw.writeByteArray(SNF_NITEM_STRING_BEGIN, null, 0, 0);
        afw.writeString(SNF_NITEM_STRING_TEXT, mString);
        afw.writeByteArray(SNF_NITEM_STRING_END, null, 0, 0);

    }

    @Override
    public void itemLoad(AsusFormatReader afr) throws IOException {
        AsusFormatReader.Item item = afr.readItem();
        while (item != null) {
            switch (item.getId()) {
                case SNF_NITEM_STRING_BEGIN:
                    break;
                case SNF_NITEM_STRING_TEXT:
                    mString = item.getStringValue();
                    break;
                case SNF_NITEM_STRING_END:
                    return;
                default:
                    Log.w(TAG, "Unknow id = 0x" + Integer.toHexString(item.getId()));
                    break;
            }
            item = afr.readItem();
        }
    }
}
