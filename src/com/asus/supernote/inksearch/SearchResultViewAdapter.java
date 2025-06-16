package com.asus.supernote.inksearch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.asus.supernote.BitmapLender;
import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.editable.DrawableSpan;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.indexservice.NoteItemFile;
import com.asus.supernote.picker.NoteBookPickerActivity;
import com.asus.supernote.picker.PickerActivity;
import com.visionobjects.myscript.inksearch.FindResult;
import com.visionobjects.myscript.inksearch.OccurrenceIterator;

public class SearchResultViewAdapter extends BaseAdapter {
	public static final String TAG = "SearchResultViewAdapter";
	public static final int MAX_ARRAY_SIZE = 81920;
	public static final String OBJ = String.valueOf((char) 65532);

	private final Context mContext;
	private final BookCase mBookcase;

	private ArrayList<SearchResultItemInfo> mSearchResultItemInfoList;

	private ArrayList<NoteItemFile> mNoteItemFileList = null;
	private Object mNoteItemFileListLockObj = new Object();

	private Toast mToast = null;
	private Thread mSearchThread = null;

	
	/**
	 * Tell us we must check the search result.
	 * mode 1 
	 *		changed for new pageID
	 * 
	 * @author Richard
	 *
	 */
	public class SearchSourceChanged
	{
		public static final int MODE_NEW_INDEX_FILE = 1;
		public SearchSourceChanged(int m,Long page,Long book)
		{
			mode = m;
			pageID = page;
			bookID = book;
		}
		int mode;
		Long pageID;
		Long bookID;
	}
	
	public void clearNoteItemFileList()
	{
		synchronized (mNoteItemFileListLockObj)
		{
			if(mNoteItemFileList != null)
			{
				mNoteItemFileList.clear();
			}
			else
			{
				mNoteItemFileList = new ArrayList<NoteItemFile>();
			}
		}
	}
	
	private Boolean setFileList(NoteItemFile file)
	{
		synchronized (mNoteItemFileListLockObj)
		{
			mNoteItemFileList.add(file);
			return true;
		}
	}
	
	public NoteItemFile getSpecificFile(Long pageID,Boolean flag)
	{
		synchronized(mNoteItemFileListLockObj)
		{
			
			for(NoteItemFile notefile : mNoteItemFileList)
			{
				if(notefile.getPageID() == pageID)
				{
					if(notefile.getIsLoaded() || flag)
					{
						return notefile;
					}
					else 
					{
						return null;
					}
				}
			}
			
			//not found
			//Need do something later //Richard
			return null;
		}
	}
	
	public SearchResultViewAdapter(Context context, boolean isBookmark) {
		mContext = context;
		clearNoteItemFileList();
		int duration = Toast.LENGTH_SHORT;
		mToast = Toast.makeText(mContext, R.string.Last_Search_Result_Info, duration);
		mSearchResultItemInfoList = new ArrayList<SearchResultItemInfo>();
		mBookcase = BookCase.getInstance(mContext);
	}

	@Override
	public int getCount() {
		return mSearchResultItemInfoList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public class SpecialSearchViewHolder{
		TextView title;
	}
	
	public class SpecialSearchViewBookNameHolder{
		TextView title;
		FrameLayout framelayout;//darwin
	}
	
	private View getSpecialView(SearchResultItemInfo itemInfo, View convertView, ViewGroup parent)
	{
		SpecialSearchViewHolder holder = null;
		
		int resource = R.layout.single_page_search_result_head_row;//default
		
		switch(itemInfo.mSpecialKind)
		{
		case SearchResultItemInfo.SpecialKind_NOTEBOOKS:
			resource = R.layout.single_page_search_result_head_row;
			break;
		case SearchResultItemInfo.SpecialKind_PAGES:
			resource = R.layout.single_page_search_result_head_row;
			break;
		case SearchResultItemInfo.SpecialKind_PERSONAL:
			resource = R.layout.single_page_search_result_head_row;
			break;
		case SearchResultItemInfo.SpecialKind_PAGES_CURRENT:
			resource = R.layout.single_page_search_result_head_row;
			break;
		}
		
		if(convertView != null && !(convertView.getTag() instanceof SpecialSearchViewHolder))
		{
			if(convertView.getTag() instanceof SearchViewHolder)
			{
				recycleBitmaps(((SearchViewHolder)convertView.getTag()).editText.getEditableText());
			}
			convertView = null;
		}
		
		if(convertView == null){
			convertView = View.inflate(mContext,resource, null);
			holder = new SpecialSearchViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.Search_page_title);
			convertView.setTag(holder);
		} else {
			holder = (SpecialSearchViewHolder) convertView.getTag();
		}
		
		holder.title.setText(itemInfo.getSpecialString());

		
		return convertView;
	}
	
	private View getSpecialViewBookName(SearchResultItemInfo itemInfo, View convertView, ViewGroup parent)
	{
		SpecialSearchViewBookNameHolder holder = null;
		
		int resource = R.layout.single_page_search_result_row_book;//default
		
		if(convertView != null && !(convertView.getTag() instanceof SpecialSearchViewBookNameHolder))
		{
			if(convertView.getTag() instanceof SearchViewHolder)
			{
				recycleBitmaps(((SearchViewHolder)convertView.getTag()).editText.getEditableText());
			}
			convertView = null;
		}
		
		if(convertView == null){
			convertView = View.inflate(mContext,resource, null);
			holder = new SpecialSearchViewBookNameHolder();
			holder.title = (TextView) convertView.findViewById(R.id.Search_page_title_book_name);
			holder.framelayout = (FrameLayout) convertView.findViewById(R.id.Search_title_layout);//darwin
			convertView.setTag(holder);
		} else {
			holder = (SpecialSearchViewBookNameHolder) convertView.getTag();
		}
		
		holder.title.setText(itemInfo.getEditable());
		final Long bookid = itemInfo.mNotebook.getCreatedTime();
		holder.framelayout.setOnClickListener(new OnClickListener() {//darwin
			
			@Override
			public void onClick(View v) {
				// Jump to page view.
					
				try {
					Intent intent = new Intent(mContext, PickerActivity.class);
					intent.putExtra(MetaData.BOOK_ID, bookid);
					mContext.startActivity(intent);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

			}
		});

		
		return convertView;
	}
	
    public void recycleBitmaps(Editable editable) {
    	if(editable == null)
    	{
    		return;
    	}
        DrawableSpan[] spans = editable.getSpans(0, editable.length(), DrawableSpan.class);
        if (spans != null) {
            for (DrawableSpan span : spans) {
                Drawable drawable = span.getDrawable();
                if (drawable instanceof BitmapDrawable) {
                	((BitmapDrawable) drawable).setCallback(null);
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }                    
                }
            }
        }
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SearchResultItemInfo itemInfo = mSearchResultItemInfoList.get(position);
		final SearchActivity searchActivity = (SearchActivity)mContext;//noah
		if(itemInfo.mIsSpecialInfo)
		{
			switch(itemInfo.mSpecialKind)
			{
				case SearchResultItemInfo.SpecialKind_NOTEBOOK_NAME:
					return getSpecialViewBookName(itemInfo,convertView,parent);
				default:
					return getSpecialView(itemInfo,convertView,parent);
			}
		}
		
		final Long bookid = itemInfo.mNotebook.getCreatedTime();
		final Long pagid = itemInfo.mPageID;
		
		SearchViewHolder holder = null;
		if(convertView != null && !(convertView.getTag() instanceof SearchViewHolder))
		{
			convertView = null;
		}
		
		if (convertView == null) {
			convertView = View.inflate(mContext,
					R.layout.single_page_search_result_row, null);
			holder = new SearchViewHolder();
			holder.titleBar = (FrameLayout) convertView
					.findViewById(R.id.Search_title_layout);
			holder.itemBar = (FrameLayout) convertView
					.findViewById(R.id.Search_item_layout);
			holder.editText = (TextView) convertView
					.findViewById(R.id.Search_editText);
			holder.pageIndex = (TextView) convertView
					.findViewById(R.id.Search_pageIndex);

			holder.bookName = (TextView) convertView
					.findViewById(R.id.Search_page_title_book_name);
			holder.bookCount = (TextView) convertView
					.findViewById(R.id.Search_page_title_book_count);
			convertView.setTag(holder);
		} else {
			holder = (SearchViewHolder) convertView.getTag();
		}

		itemInfo.setEditableToHolder(holder);
		holder.pageIndex.setText(itemInfo.getPageIndexString());
		holder.itemBar.setOnClickListener(new OnClickListener() {//darwin

			@Override
			public void onClick(View v) {
				// Jump to page.

				try {
					Intent intent = new Intent(mContext, EditorActivity.class);
					intent.putExtra(MetaData.BOOK_ID, bookid);
					intent.putExtra(MetaData.PAGE_ID, pagid);
					//begin noah;for share
					if(searchActivity.isShareMode()){
						intent.putExtra(Intent.EXTRA_INTENT, searchActivity.getShareIntent());
					}
					//end noah;for share
					mContext.startActivity(intent);
					if(searchActivity.isShareMode()){
						searchActivity.finish();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		if (!itemInfo.mIsFirst) {
			holder.titleBar.setVisibility(View.GONE);
		} else {
			holder.titleBar.setVisibility(View.VISIBLE);
			holder.bookName.setText(itemInfo.mNotebook.getTitle());
			if(itemInfo.mCount == 1)
			{
				holder.bookCount.setText(String.valueOf(itemInfo.mCount) + mContext.getResources().getString(R.string.SearchResultItemInfo_Page_Space));
			}
			else
			{
				holder.bookCount.setText(String.valueOf(itemInfo.mCount) + mContext.getResources().getString(R.string.SearchResultItemInfo_Pages));
			}
		}

		return convertView;

	}

	public void restartSearch(String searchstring,long bookID) {
		synchronized (synchronizedObject)
		{
			if(mIsSearching)
			{
				mIsNeedResearch = true;
				return;
			}
		}
		startSearch(searchstring,bookID);
	}
	
	static Object synchronizedObject= new Object();
	Boolean mIsSearching = false;//Thread.isAlive is not work well.
	Boolean mIsNeedResearch = false;
	//start a thread to search
	public void startSearch(String searchstring,long bookID) {
		final String finalSearchString = searchstring;
		final Long finalBookID = bookID;
			
		mSearchThread = new Thread(new Runnable() {			
			public void run() {
				Looper.prepare();
				do{
					synchronized (synchronizedObject)
					{
						if(mIsSearching)
						{
							Message msg1 = new Message();
							msg1.what = SEARCHRESULTERSPLEASEWAIT;
							myHandler.sendMessage(msg1);
							return;
						}
						else
						{
							mIsSearching = true;
						}
					}
					mIsNeedResearch = false;
					//clear the old search result first.
					Message msg = new Message();
					msg.what = SEARCHRESULTERSTARTFITER;
					myHandler.sendMessage(msg);				
					
					if(finalBookID == -1)
					{
						Message msg1 = new Message();
						msg1.what = SEARCHRESULTERSOURCETYPECHANGE;
						msg1.obj = Integer.valueOf(SearchResultItemInfo.SpecialKind_NOTEBOOKS);
						myHandler.sendMessage(msg1);
						
						List<NoteBook> noteBookList = mBookcase.getBookList();
						int start = 0;
						int length = 0;
						//emmanual to fix bug 501586
						if(finalSearchString != null){
							length = finalSearchString.length();
						}
						String bookname = "";
						
						Boolean globalLockState = NoteBookPickerActivity.islocked();
						//begin noah;for share
						if(!((SearchActivity)mContext).isShareMode()){
							for(NoteBook nb : noteBookList)
							{
								if(globalLockState && nb.getIsLocked())
								{
									continue;
								}
								
								if(nb.getUserId() != 0 && nb.getUserId() != MetaData.CurUserAccount)
								{
									continue;
								}
								
								bookname = nb.getTitle();
								if(bookname == null || bookname.length() < length)
								{
									continue;
								}
								start = bookname.indexOf(finalSearchString);
								if( start != -1)
								{
									SearchResultItemInfo temp = new SearchResultItemInfo(
											SearchResultViewAdapter.this,
											SearchResultItemInfo.SpecialKind_NOTEBOOK_NAME,
											nb,
											start,
											length);
									Message msg2 = new Message();
									msg2.what = SEARCHRESULTERBOOKNAME;
									msg2.obj = temp;
									myHandler.sendMessage(msg2);
								}
							}
						}
						//end noah;for share
						Message msg2 = new Message();
						msg2.what = SEARCHRESULTERSOURCETYPECHANGE;
						msg2.obj = Integer.valueOf(SearchResultItemInfo.SpecialKind_PAGES);
						myHandler.sendMessage(msg2);
						
						for(NoteBook nb : noteBookList)
						{
							if(globalLockState && nb.getIsLocked())
							{
								continue;
							}
							
							if(nb.getUserId() != 0 && nb.getUserId() != MetaData.CurUserAccount)
							{
								continue;
							}
							searchOneBook(nb,finalSearchString);
						}
					}
					else
					{
						Message msg2 = new Message();
						msg2.what = SEARCHRESULTERSOURCETYPECHANGE;
						msg2.obj = Integer.valueOf(SearchResultItemInfo.SpecialKind_PAGES_CURRENT);
						myHandler.sendMessage(msg2);
						
						searchOneBook(mBookcase.getNoteBook(finalBookID),finalSearchString);
					}
					
					Message msgOver = new Message();
					msgOver.what = SEARCHRESULTERSEARCHOVER;
					myHandler.sendMessage(msgOver);
					
					synchronized (synchronizedObject)
					{
						mIsSearching = false;
					}
			}while(mIsNeedResearch);
			Looper.loop();
			}
		});
		mSearchThread.start();

	}
	// END: Richard

	// BEGIN: Richard
	protected static final int SEARCHRESULTERFITER = 0x101;
	protected static final int SEARCHRESULTERSTARTFITER = 0x102;
	protected static final int SEARCHRESULTERSOURCETYPECHANGE = 0x103;
	protected static final int SEARCHRESULTERBOOKNAME = 0x104;
	protected static final int SEARCHRESULTERSEARCHOVER= 0x105;
	protected static final int SEARCHRESULTERSEARCHPAGESTART= 0x106;
	protected static final int SEARCHRESULTERSEARCHPAGESTOP= 0x107;
	protected static final int SEARCHRESULTERSPLEASEWAIT= 0x108;
	
	private int mSearchResoultSource = 0;
	private Boolean mIsSearchResultSourceChanged = false;
	//add or clear show result
	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			
			//BEGIN: RICHARD
			if(((Activity)mContext).isFinishing())
			{
				super.handleMessage(msg);
				return;
			}
			//END: RICHARD
			
			switch (msg.what) {
			case SEARCHRESULTERSPLEASEWAIT:
				mToast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
				mToast.show();
				break;
			case SEARCHRESULTERSEARCHOVER:
			{
				if(mSearchResultItemInfoList.size() == 0)
				{
					Toast tempToast = Toast.makeText(mContext, R.string.no_search_result, Toast.LENGTH_LONG);
					//begin:clare
					tempToast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
					//end:clare
					tempToast.show();
				}
			}
			break;
			case SEARCHRESULTERSTARTFITER:
				//Clear search result.
				mSearchResoultSource = 0;
				mIsSearchResultSourceChanged = false;
				mSearchResultItemInfoList.clear();
				clearNoteItemFileList();
				changeData();
				break;
			case SEARCHRESULTERSOURCETYPECHANGE:
				{
					Integer source = (Integer) msg.obj;
					mSearchResoultSource = source;
					mIsSearchResultSourceChanged = true;
				}
				break;
				
			case SEARCHRESULTERBOOKNAME:
				{
					//Add a search result.
					//First check Source changed or not.
					if(mIsSearchResultSourceChanged)
					{
						mIsSearchResultSourceChanged = false;
						SearchResultItemInfo temp = new SearchResultItemInfo(SearchResultViewAdapter.this,mContext,mSearchResoultSource);
						
						mSearchResultItemInfoList.add(temp);
					}				
					//Second Add resoult
					SearchResultItemInfo temp = (SearchResultItemInfo) msg.obj;	
					mSearchResultItemInfoList.add((SearchResultItemInfo) msg.obj);
					changeData();
				}
				break;
			case SEARCHRESULTERSEARCHPAGESTART:
				break;
			case SEARCHRESULTERSEARCHPAGESTOP:
				break;
			case SEARCHRESULTERFITER:
				//Add a search result.
				//First check Source changed or not.
				if(mIsSearchResultSourceChanged)
				{
					mIsSearchResultSourceChanged = false;
					SearchResultItemInfo temp = new SearchResultItemInfo(SearchResultViewAdapter.this,mContext,mSearchResoultSource);
					mSearchResultItemInfoList.add(temp);
				}				
				
				//Second Add resoult
				SearchResultItemInfo temp = (SearchResultItemInfo) msg.obj;
				if (mSearchResultItemInfoList.size() > 0) {
					SearchResultItemInfo last = mSearchResultItemInfoList
							.get(mSearchResultItemInfoList.size() - 1);
					if (temp.isInSameBook(last)) {
						// the same book
						temp.mIsFirst = false;
						temp.mFirstResultOFBook = last.mFirstResultOFBook;
						last.mFirstResultOFBook.mCount++;
					}
				}

				mSearchResultItemInfoList.add((SearchResultItemInfo) msg.obj);
				changeData();
				break;
			}
			super.handleMessage(msg);
		}
	};

	// END: Richard

	private long getPageIndexFlageInDB(Long pageID)
	{
		long pageIndexStatus =-1;
		Cursor cursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null,
				"created_date = ?", new String[] { Long.toString(pageID) }, null);
		
		if(cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			pageIndexStatus = cursor
					.getLong(MetaData.PageTable.INDEX_IS_INDEXED);
		}
		cursor.close();
		
		return pageIndexStatus;
	}
	
	// BEGIN: Richard
	private void searchOneBook(NoteBook notebook,String finalSearchString)
	{	
		List<Long> pageList = notebook.getPageOrderList();
		for(int i = 0; i<pageList.size();i++)
		{
			searchOneFile(i + 1, finalSearchString,
					pageList.get(i), notebook);
		}
	}
	//search the file with the string.
	private void searchOneFile(int pageindex, String text, Long pageID,
			NoteBook notebook) {
		
		if(getPageIndexFlageInDB(pageID) != MetaData.INDEX_FILE_CREATE_SUCESS)
		{
			return;
		}
		
		String filePath = MetaData.DATA_DIR
				+ notebook.getCreatedTime().toString() + "/" + pageID + "/"
				+ MetaData.NOTE_INDEX_PREFIX;
		
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}

		try
		{
			AsusSearch temp = new AsusSearch();
			FindResult result = temp.doQuery(text, filePath);

			OccurrenceIterator occurrences = result.getOccurrences();
	
			while(!occurrences.isAtEnd()) {
	
				SearchResultItemInfo searchresultiteminfo = new SearchResultItemInfo(
						this,
						mContext,notebook, pageID, occurrences);
				
				if(null == getSpecificFile(pageID,true))
				{
					NoteItemFile itemFile = new NoteItemFile(mContext,notebook.getCreatedTime(),pageID);
					setFileList(itemFile);
					itemFile.getNoteItem();//Allen++
				}
				if(searchresultiteminfo.GetEditable()){//Allen
					Message msg = new Message();
					msg.what = SEARCHRESULTERFITER;
					searchresultiteminfo.mPageIndex = pageindex;
					msg.obj = searchresultiteminfo;
					myHandler.sendMessage(msg);
				}
	
				occurrences.next();
			}
			System.out.println();
			occurrences.dispose();
		}catch(Exception e)
		{
			return;
		}
	}

	// END: Richard

	public void changeData() {
		notifyDataSetChanged();
	}

	public void releaseMemory()
	{		
		try
		{
			mToast.cancel();
			mToast = null;
		}catch(Exception ex)
		{
			
		}
		
		if(mIsSearching)
		{
			try
			{
				mSearchThread.interrupt();
			}catch(Exception e)
			{
				
			}
		}
		
        BitmapLender lender = BitmapLender.getInstance();
        lender.recycleBitmaps();		
		
		synchronized (mNoteItemFileListLockObj)
		{
			for(int i = 0;i < mNoteItemFileList.size();i++)
			{
				NoteItemFile tempFile = mNoteItemFileList.get(i);
				if(tempFile.mIsLoaded )
				{
					ArrayList<NoteItemArray> allnoteItems = null;
					if((allnoteItems = tempFile.getNoteItem()) != null)
					{
						for(NoteItemArray noteItems: allnoteItems){//Allen++
							for(NoteItem item :noteItems.getNoteItems())
							{
								if(item instanceof DrawableSpan)
								{
									Drawable drawable = ((DrawableSpan)item).getDrawable();
					                if (drawable instanceof BitmapDrawable) {
					                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
					                    if (bitmap != null && !bitmap.isRecycled()) {
					                        bitmap.recycle();
					                    }
					                }
									((DrawableSpan) item).getDrawable().setCallback(null);
								}
							}
						}				
					}
				}				
			}
			mNoteItemFileList.clear();
			
			for(int i = 0;i < mSearchResultItemInfoList.size();i++)
			{
				recycleBitmaps(mSearchResultItemInfoList.get(i).getEditable());
			}
			mSearchResultItemInfoList.clear();
			
		}
		
	}
}
