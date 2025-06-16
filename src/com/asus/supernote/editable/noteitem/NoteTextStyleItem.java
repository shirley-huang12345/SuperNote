package com.asus.supernote.editable.noteitem;

import java.io.IOException;
import java.io.Serializable;

import com.asus.supernote.data.AsusFormat;
import com.asus.supernote.data.AsusFormatReader;
import com.asus.supernote.data.AsusFormatWriter;
import com.asus.supernote.editable.PageEditor;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Parcel;
import android.text.TextPaint;
import android.text.style.StyleSpan;
import android.util.Log;

public class NoteTextStyleItem extends StyleSpan implements NoteItem, AsusFormat {

    private static final String TAG = "NoteTextStyleItem";
    private static final int sDefaultStyle = Typeface.NORMAL;

    private int mStyle = Typeface.NORMAL;
    private int mStart = -1;
    private int mEnd = -1;

    public NoteTextStyleItem() {
        super(sDefaultStyle);
    }

    public NoteTextStyleItem(int style) {
        super(style);
        mStyle = mStyle == Typeface.BOLD ? Typeface.BOLD : Typeface.NORMAL;
        mStyle = style;
    }

    @Override
    public int getStyle() {
        return mStyle;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        apply(ds, mStyle);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        apply(paint, mStyle);
    }

    private static void apply(Paint paint, int style) {
        int oldStyle;

        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        }
        else {
            oldStyle = old.getStyle();
        }

        int want = oldStyle | style;

        Typeface tf;
        if (old == null) {
            tf = Typeface.defaultFromStyle(want);
        }
        else {
            tf = Typeface.create(old, want);
        }

        int fake = want & ~tf.getStyle();

        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(tf);
    }

    @Override
    public int getStart() {
        return mStart;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mStyle);
    }

    @Override
    public int getEnd() {
        return mEnd;
    }

    @Override
    public void setStart(int start) {
        mStart = start;
    }

    @Override
    public void setEnd(int end) {
        mEnd = end;
    }

    @Override
    public Serializable save() {
        NoteTextSytleItemSavedData ntsisd = new NoteTextSytleItemSavedData();

        ntsisd.mStyle = mStyle;
        ntsisd.mStart = mStart;
        ntsisd.mEnd = mEnd;
        ntsisd.mOuterClassName = this.getClass().getName();

        return ntsisd;
    }

    @Override
    public void load(Serializable s, PageEditor pe) {
        NoteTextSytleItemSavedData ntsisd = (NoteTextSytleItemSavedData) s;

        mStyle = ntsisd.mStyle;
        mStart = ntsisd.mStart;
        mEnd = ntsisd.mEnd;
    }

    @Override
    public String getText() {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings( "serial" )
    public static class NoteTextSytleItemSavedData implements Serializable, NoteItem.NoteItemSaveData {

        public int mStyle = Typeface.NORMAL;
        public int mStart = -1;
        public int mEnd = -1;

        public String mOuterClassName;

        @Override
        public String getOuterClassName() {
            return mOuterClassName;
        }

    }

    @Override
    public void itemSave(AsusFormatWriter afw) throws IOException {
        afw.writeByteArray(SNF_NITEM_TEXTSTYLE_BEGIN, null, 0, 0);
        afw.writeInt(SNF_NITEM_TEXTSTYLE_STYLE, mStyle);
        afw.writeInt(SNF_NITEM_TEXTSTYLE_POS_START, mStart);
        afw.writeInt(SNF_NITEM_TEXTSTYLE_POS_END, mEnd);
        afw.writeByteArray(SNF_NITEM_TEXTSTYLE_END, null, 0, 0);

    }

    @Override
    public void itemLoad(AsusFormatReader afr) throws IOException {
        AsusFormatReader.Item item = afr.readItem();
        while (item != null) {
            switch (item.getId()) {
                case SNF_NITEM_TEXTSTYLE_BEGIN:
                    break;
                case SNF_NITEM_TEXTSTYLE_STYLE:
                    mStyle = item.getIntValue();
                    break;
                case SNF_NITEM_TEXTSTYLE_POS_START:
                    mStart = item.getIntValue();
                    break;
                case SNF_NITEM_TEXTSTYLE_POS_END:
                    mEnd = item.getIntValue();
                    break;
                case SNF_NITEM_TEXTSTYLE_END:
                    return;
                default:
                    Log.w(TAG, "Unknow id = 0x" + Integer.toHexString(item.getId()));
                    break;
            }
            item = afr.readItem();
        }

    }

}
