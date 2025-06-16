package com.asus.supernote.picker;

import java.io.Serializable;
import java.util.Comparator;

public class PageComparator implements Comparator<SimplePageInfo>, Serializable {
    private static final long serialVersionUID = 1L;
    public static final String TAG = "PageComparator";
    public static final int BIG = 1;
    public static final int SAME = 0;
    public static final int SMALL = -1;
    public static final int UNKNOWN = -2;

    @Override
    public int compare(SimplePageInfo lhs, SimplePageInfo rhs) {
        SimplePageInfo newPage = lhs;
        SimplePageInfo oldPage = rhs;

        if (newPage.bookIndex < oldPage.bookIndex) {
            return SMALL;
        }
        else if (newPage.bookIndex > oldPage.bookIndex) {
            return BIG;
        }
        else if (newPage.pageIndex < oldPage.pageIndex) {
            return SMALL;
        }
        else if (newPage.pageIndex > oldPage.pageIndex) {
            return BIG;
        }
        else {
            return SAME;
        }

    }
}
