package com.asus.supernote.picker;

import java.io.Serializable;

public class SimplePageInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    public Long bookId;
    public Long pageId;
    public int bookIndex;
    public int pageIndex;

    public SimplePageInfo(Long b, Long p, int bI, int pI) {
        bookId = b;
        pageId = p;
        bookIndex = bI;
        pageIndex = pI;
    }
}
