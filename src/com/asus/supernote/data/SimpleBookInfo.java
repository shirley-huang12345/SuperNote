package com.asus.supernote.data;

import java.io.Serializable;

public class SimpleBookInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String TAG = "SimpleBookInfo";
    public int mBookColor = MetaData.BOOK_COLOR_WHITE;
    public int mBookStyle = MetaData.BOOK_GRID_LINE;
    public int mPageSize = MetaData.PAGE_SIZE_PHONE;

    public SimpleBookInfo(NoteBook book) {
        mBookColor = book.getBookColor();
        mBookStyle = book.getGridType();
        mPageSize = book.getPageSize();
    }

    public void setupBook(NoteBook target) {
        target.setBookColor(mBookColor);
        target.setGridType(mBookStyle);
        target.setPageSize(mPageSize);
    }
}
