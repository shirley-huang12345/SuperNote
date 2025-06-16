package com.asus.supernote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PageOrderList implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Long> mPages;
    private PageOrderList mList;

    public PageOrderList(List<Long> pages) {
        mPages = new ArrayList<Long>(pages);
        mList = this;
    }

    public Serializable save() {
        return mList;
    }

    public void load(Serializable object) {
        mList = (PageOrderList) object;
        mPages = mList.mPages;
    }

    public List<Long> getList() {
        return mPages;
    }

}
