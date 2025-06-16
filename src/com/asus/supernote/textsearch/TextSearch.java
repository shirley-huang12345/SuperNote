package com.asus.supernote.textsearch;

import java.util.List;

import android.text.Editable;

public interface TextSearch {
	public void prepareSearch();
	public void savePageFile(long pageId);
	public void updatePageFile(long pageId);
	public Editable searchInPage(String str);
	public String getPageText(long pageId);
	public List<Editable> getEditable(long bookId, long pageId);
}
