package com.asus.supernote.data;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.asus.supernote.picker.SimplePageInfo;

public class BackupPageInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private SortedSet<SimplePageInfo> mSet;
    private BackupPageInfo mInfo;

    public BackupPageInfo(SortedSet<SimplePageInfo> set) {
        mSet = new TreeSet<SimplePageInfo>(set);
        mInfo = this;
    }

    public Serializable save() {
        return mInfo;
    }

    public void load(Serializable object) {
        mInfo = (BackupPageInfo) object;
        mSet = mInfo.mSet;
    }

    public Set<SimplePageInfo> getPages() {
        return mSet;
    }

}
