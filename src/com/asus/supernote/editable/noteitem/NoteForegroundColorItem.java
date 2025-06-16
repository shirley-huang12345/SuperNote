package com.asus.supernote.editable.noteitem;

import java.io.IOException;
import java.io.Serializable;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.asus.supernote.data.AsusFormat;
import com.asus.supernote.data.AsusFormatReader;
import com.asus.supernote.data.AsusFormatWriter;
import com.asus.supernote.editable.PageEditor;

public class NoteForegroundColorItem extends ForegroundColorSpan implements NoteItem, AsusFormat {

    private static final String TAG = "NoteForegroundColorItem";
    private static final int mDefaultColor = Color.BLACK;  //Carol[TT322534]
    private int mColor = 0;

    private int mStart = -1;
    private int mEnd = -1;

    public NoteForegroundColorItem() {
        super(mDefaultColor);
    }

    public NoteForegroundColorItem(int color) {
        super(color);
        mColor = color;
    }

    @Override
    public int getForegroundColor() {
        return mColor;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(mColor);
    }

    public Serializable save() {
        NoteForegroundColorItemSavedData nfcisd = new NoteForegroundColorItemSavedData();

        nfcisd.mColor = mColor;
        nfcisd.mStart = mStart;
        nfcisd.mEnd = mEnd;
        nfcisd.mOuterClassName = this.getClass().getName();

        return nfcisd;
    }

    public void load(Serializable s, PageEditor pe) {
        NoteForegroundColorItemSavedData nfcisd = (NoteForegroundColorItemSavedData) s;

        mColor = nfcisd.mColor;
        mStart = nfcisd.mStart;
        mEnd = nfcisd.mEnd;
    }

    public String getText() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getStart() {
        return mStart;
    }

    public int getEnd() {
        return mEnd;
    }

    public void setStart(int start) {
        mStart = start;

    }

    public void setEnd(int end) {
        mEnd = end;
    }

    @SuppressWarnings( "serial" )
    public static class NoteForegroundColorItemSavedData implements Serializable, NoteItem.NoteItemSaveData {

        public int mColor = 0;
        public int mStart = -1;
        public int mEnd = -1;

        public String mOuterClassName = null;

        public String getOuterClassName() {
            return mOuterClassName;
        }
    }

    @Override
    public void itemSave(AsusFormatWriter afw) throws IOException {
        afw.writeByteArray(SNF_NITEM_FCOLOR_BEGIN, null, 0, 0);
        afw.writeInt(SNF_NITEM_FCOLOR_COLOR, mColor);
        afw.writeInt(SNF_NITEM_FCOLOR_POS_START, mStart);
        afw.writeInt(SNF_NITEM_FCOLOR_POS_END, mEnd);
        afw.writeByteArray(SNF_NITEM_FCOLOR_END, null, 0, 0);
    }

    @Override
    public void itemLoad(AsusFormatReader afr) throws IOException {
        AsusFormatReader.Item item = afr.readItem();
        while (item != null) {
            switch (item.getId()) {
                case SNF_NITEM_FCOLOR_BEGIN:
                    break;
                case SNF_NITEM_FCOLOR_COLOR:
                    mColor = item.getIntValue();
                    break;
                case SNF_NITEM_FCOLOR_POS_START:
                    mStart = item.getIntValue();
                    break;
                case SNF_NITEM_FCOLOR_POS_END:
                    mEnd = item.getIntValue();
                    break;
                case SNF_NITEM_FCOLOR_END:
                    return;
                default:
                    Log.w(TAG, "Unknow id = 0x" + Integer.toHexString(item.getId()));
                    break;
            }
            item = afr.readItem();
        }
    }
}
