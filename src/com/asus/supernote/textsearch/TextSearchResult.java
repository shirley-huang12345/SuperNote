package com.asus.supernote.textsearch;

import android.content.Context;
import android.text.Editable;

import com.asus.supernote.R;

public class TextSearchResult {
	private Context mContext = null;
	public Long mBookID;
	public Long mPageID;
	public String mBookName;
	public int mPageIndex = 0;
	public int mCount = 0;

	public Editable mEditable = null;
	public int mResultSort = 0;
	public static final int ResultSort_NULL = -1;
	public static final int ResultSort_TEXTBOOK = 0;
	public static final int ResultSort_NOTEBOOK = 1;
	public static final int ResultSort_TEXTPAGE = 2;
	public static final int ResultSort_NOTEPAGE = 3;
	public static final int ResultSort_PAGECONTENT = 4;

	private static int LastResutSort = ResultSort_NULL;

	public TextSearchResult(Context mContext, int mResultSort) {
		this.mContext = mContext;
		this.mResultSort = mResultSort;
		LastResutSort = mResultSort;
	}

	public String getSpecialString() {
		switch (mResultSort) {
		case ResultSort_TEXTBOOK:
			return mContext.getString(R.string.SearchResultItemInfo_NoteBooks);
		case ResultSort_TEXTPAGE:
			return mContext.getString(R.string.SearchResultItemInfo_Pages);
		}
		return "";
	}
	
	public String getPageIndexString()
	{
		return mContext.getString(R.string.SearchResultItemInfo_Page_Space) + Integer.toString(mPageIndex);
	}
	
	public static void resetLastResutSort(){
		LastResutSort = ResultSort_NULL;
	}
	
	public static boolean isLastResutSortNull(){
		return LastResutSort == ResultSort_NULL;
	}
}
