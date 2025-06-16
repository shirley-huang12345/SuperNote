package com.asus.supernote.textsearch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.asus.supernote.R;
import com.asus.supernote.classutils.ColorfulStatusActionBarHelper;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.NoteForegroundColorItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteBaselineItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteSendIntentItem;
import com.asus.supernote.editable.noteitem.NoteTimestampItem;
import com.asus.supernote.picker.NoteBookPickerActivity;

public class TextSearchActivity extends Activity {
	public static NoteBookPickerActivity mShareActivity;
	private Boolean mIsNewSearch = false;
	private String mQueryString = null;
	private ListView mListView = null;
	private SearchView mSearchView = null;
	private MenuItem mSearchItem = null;
	private Handler mHandler = new Handler();
	
	private TextSearch mTextSearch;
	private TextSearchResultAdapter mInfoAdapter;
	private BookCase mBookcase;
	List<TextSearchResult> mSearchResultList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// BEGIN: Better
		if (MetaData.AppContext == null) {
			MetaData.AppContext = getApplicationContext();
		}
		// END: Better

		int IsOrientationAllowOrNot = this.getResources().getInteger(
		        R.integer.is_orientation_allow); // by show
		if (IsOrientationAllowOrNot == 0) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		} else if (IsOrientationAllowOrNot == 1) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		// End: show_wang@asus.com
		super.onCreate(savedInstanceState);

		// TODO Auto-generated method stub
		// BEGIN: RICHARD
		MetaData.SEARCH_TEXT_HEIGH_LIGHT_COLOR = getResources().getColor(
		        R.color.search_text_high_light);
		// END: RICHARD
		ColorfulStatusActionBarHelper.setContentView(R.layout.search_activity,
		        true, this);// smilefish

		String data = null;
		if (savedInstanceState != null) {
			data = savedInstanceState.getString("searchString");
		}
		setQueryString(data);

		mListView = (ListView) findViewById(R.id.page_list);
		mTextSearch = new TextInfoSearch(this);
		mBookcase = BookCase.getInstance(TextSearchActivity.this);
		mSearchResultList = new ArrayList<TextSearchResult>();
		mInfoAdapter = new TextSearchResultAdapter(TextSearchActivity.this, mSearchResultList);
		mListView.setAdapter(mInfoAdapter);
	}

	public void collapseOrExpandSearchView(boolean flag) {
		if (mSearchItem != null) {
			if (flag) {
				mSearchItem.collapseActionView();
			} else {
				mSearchItem.expandActionView();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mQueryString != null &&!mQueryString.equals("") && mIsNewSearch)// may need change
		{
			mHandler.postDelayed(new Runnable() {

		        @Override
		        public void run() {
			        // TODO Auto-generated method stub
		        	hideSoftkeyboard();
		        }
	        }, 100);
			mHandler.postDelayed(new Runnable() {

		        @Override
		        public void run() {
			        // TODO Auto-generated method stub
		    		mListView.requestFocus();
					startSearch(mQueryString);
		        }
	        }, 300);
		}
	}
	
	public boolean isShareMode() {
		Intent intent = getShareIntent();
		if (intent == null)
			return false;
		return true;
	}

	public Intent getShareIntent() {
		Intent extraIntent = getIntent()
		        .getParcelableExtra(Intent.EXTRA_INTENT);
		return extraIntent;
	}

	@Override
	public void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);      
	    setIntent(intent);
	    handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	        // Gets the search query from the voice recognizer intent
	        String query = intent.getStringExtra(SearchManager.QUERY);

	        // Set the search box text to the received query and submit the search
	        mSearchView.setQuery(query, true);
	    }
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) { 
		super.onSaveInstanceState(outState);
		if(mSearchView != null)
			outState.putString("searchString", mSearchView.getQuery().toString());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		int menuId = R.menu.search_menu;

		ActionBar bar = getActionBar();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(menuId, menu);
		int option = 0;
		int mask = 0;

		option = ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM;
		mask = ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM
		        | ActionBar.DISPLAY_SHOW_TITLE;
		bar.setDisplayOptions(option, mask);
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setHomeButtonEnabled(true);

		// BEGIN: Richard
		mSearchItem = menu.findItem(R.id.menu_search);
		mSearchItem.setVisible(true);
		mSearchItem.expandActionView();

		mSearchView = (SearchView) mSearchItem.getActionView();

		// begin smilefish add voice search
		SearchManager searchManager = (SearchManager) this
		        .getSystemService(Context.SEARCH_SERVICE);
		SearchableInfo info = searchManager.getSearchableInfo(this
		        .getComponentName());
		mSearchView.setSearchableInfo(info);
		// end smilefish

		//emmanual to fix bug 435193,528773
		mSearchItem.setOnActionExpandListener(new OnActionExpandListener() {
			
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				// TODO Auto-generated method stub
            	TextSearchActivity.this.finish();
				return true;
			}
		});

		mSearchView.setOnQueryTextListener(queryTextInfoListener);
		mSearchView.setIconifiedByDefault(true);
		if (mSearchView != null && mQueryString != null) {
			mSearchView.setQuery(mQueryString, false);

		}
		// END: Richard
		// begin noah;for share;5.15
		if (isShareMode()) {
			MenuItem menuItem = menu.findItem(R.id.menu_cancel);
			menuItem.setVisible(true);
		}
		// end noah;for share
		return true;// super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_cancel:
		case android.R.id.home:
			onBackPressed();
			break;
		default:
			break;
		}
		return true;
	}

	public void setQueryString(String searchstring) {
		mQueryString = searchstring;
		mIsNewSearch = true;
	}

	final SearchView.OnQueryTextListener queryTextInfoListener = new SearchView.OnQueryTextListener() {
		@Override
		public boolean onQueryTextChange(String newText) {
			// Do something
			return true;
		}

		@Override
		public boolean onQueryTextSubmit(String query) {
			if (mQueryString == null || !mQueryString.equals(query)) {
				startSearch(query);
			}
			return true;
		}
	};
	
	private void startSearch(String query){
		hideSoftkeyboard();
		setQueryString(query);
		mSearchResultList = searchText(query);
		mInfoAdapter.setEditableList(mSearchResultList);
		mInfoAdapter.notifyDataSetChanged();
	}
	
	private List<TextSearchResult> searchText(String query){
		mSearchResultList.clear();
		for (NoteBook book : mBookcase.getBookList()) {
			if (book.isDeleted() || book.getIsLocked()
			        && NoteBookPickerActivity.getIsLock()
			        || book.getUserId() != 0 && book.getUserId() != MetaData.CurUserAccount) {
				continue;
			}
			if (book.getTitle().toUpperCase().contains(query.toUpperCase())) {
				if (TextSearchResult.isLastResutSortNull()) {
					TextSearchResult textitem = new TextSearchResult(this, TextSearchResult.ResultSort_TEXTBOOK);
					mSearchResultList.add(textitem);
				}
				TextSearchResult bookitem = new TextSearchResult(this, TextSearchResult.ResultSort_NOTEBOOK);
				bookitem.mBookID = book.getCreatedTime();
				String title = book.getTitle();
				int start = title.toUpperCase().indexOf(query.toUpperCase());
				int end = start + query.length();
				EditText edittext = new EditText(TextSearchActivity.this);
				edittext.setText(title);
				NoteForegroundColorItem fcs = new NoteForegroundColorItem(MetaData.SEARCH_TEXT_HEIGH_LIGHT_COLOR);
				edittext.getText().setSpan(fcs, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				bookitem.mEditable = edittext.getText();
				mSearchResultList.add(bookitem);
			}
		}
		TextSearchResult.resetLastResutSort();
		
		if(!isShareMode()){
			for (NoteBook book : mBookcase.getBookList()) {
				if (book.isDeleted() || book.getIsLocked() && NoteBookPickerActivity.getIsLock()
				        || book.getUserId() != 0 && book.getUserId() != MetaData.CurUserAccount) {
					continue;
				}
				Long bookId = book.getCreatedTime();
				for (long pageId : book.getPageOrderList()) {
					TextSearchResult mPageItem = null;
					for(Editable temp:mTextSearch.getEditable(bookId, pageId)){
						Editable editable = loadNoteItems(temp);
						
						List<SearchIndex> result = searchText(editable
						        .toString().toUpperCase(), query.toUpperCase());
						if(result.size() >0){
							if(TextSearchResult.isLastResutSortNull()){
								TextSearchResult textitem = new TextSearchResult(TextSearchActivity.this, TextSearchResult.ResultSort_TEXTPAGE);
								mSearchResultList.add(textitem);
							}
							if(mPageItem == null){
								mPageItem = new TextSearchResult(TextSearchActivity.this, TextSearchResult.ResultSort_NOTEPAGE);
								mPageItem.mBookName = book.getTitle();
								mSearchResultList.add(mPageItem);
							}
							mPageItem.mCount += result.size();
							
							for (SearchIndex index : result) {
								TextSearchResult item = new TextSearchResult(TextSearchActivity.this, TextSearchResult.ResultSort_PAGECONTENT);
								int start = getStartIndex(editable, index.start);
								Editable edit = (Editable) editable.subSequence(start, editable.length());
								NoteForegroundColorItem fcs = new NoteForegroundColorItem(MetaData.SEARCH_TEXT_HEIGH_LIGHT_COLOR);
								edit.setSpan(fcs, index.start-start, index.end-start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
								
								item.mEditable = edit;
								item.mBookID = bookId;
								item.mPageID = pageId;
								item.mPageIndex = book.getPageIndex(pageId) + 1;
								mSearchResultList.add(item);
							}
						}
					}
				}
			}
		}
		TextSearchResult.resetLastResutSort();
		
		if (mSearchResultList.size() == 0) {
			Toast tempToast = Toast.makeText(TextSearchActivity.this, R.string.no_search_result, Toast.LENGTH_LONG);
			tempToast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
			tempToast.show();
		}
		return mSearchResultList;
	}
	
	private int getStartIndex(Editable editable, int start) {
		int index = start - 3 > 0 ? start - 3 : 0;
		NoteItem[] items = editable.getSpans(start - 3, start, NoteHandWriteBaselineItem.class);
		if (items.length > 0) {
			int r1 = editable.getSpanEnd(items[items.length - 1]);
			index = r1 > index ? r1 : index;
		}
		items = editable.getSpans(start - 3, start, NoteHandWriteItem.class);
		if (items.length > 0) {
			int r1 = editable.getSpanEnd(items[items.length - 1]);
			index = r1 > index ? r1 : index;
		}
		items = editable.getSpans(start - 3, start, NoteTimestampItem.class);
		if (items.length > 0) {
			int r1 = editable.getSpanEnd(items[items.length - 1]);
			index = r1 > index ? r1 : index;
		}
		items = editable.getSpans(start - 3, start, NoteSendIntentItem.class);
		if (items.length > 0) {
			int r1 = editable.getSpanEnd(items[items.length - 1]);
			index = r1 > index ? r1 : index;
		}
		return index;
	}

	public List<SearchIndex> searchText(String wholetext, String searchtext) {
		List<SearchIndex> result = new ArrayList<SearchIndex>();
		int start = 0, end = 0;
		while (true) {
			start = wholetext.indexOf(searchtext, start);
			end = start + searchtext.length();
			if (start >= 0 && end >= 0) {
				result.add(new SearchIndex(start, end));
			} else {
				break;
			}
			start = end;
		}

		return result;
	}

	public class SearchIndex {
		public int start;
		public int end;

		public SearchIndex(int start, int end) {
			this.start = start;
			this.end = end;
		}
	}
	
	private Editable loadNoteItems(Editable editable){
		if (mfontsize == -1) {
			mfontsize = (int) getResources().getDimension(
			        R.dimen.page_search_result_fontsize);
            EditText et= new EditText(this);
            et.setTextSize(TypedValue.COMPLEX_UNIT_PX,mfontsize);
            mTextheight = et.getLineHeight();
		}
		NoteItem[] noteItems = editable.getSpans(0, editable.length(), NoteItem.class);
		
		for (int i = 0; i < noteItems.length; i++) {
			NoteItem currentNoteItem = null;
			if (noteItems[i] instanceof NoteHandWriteBaselineItem) {
				Serializable data = ((NoteHandWriteBaselineItem) noteItems[i])
				        .save();
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				try {
					ObjectOutputStream obj = new ObjectOutputStream(b);
					obj.writeObject(data);

					ByteArrayInputStream inputb = new ByteArrayInputStream(
					        b.toByteArray());
					ObjectInputStream inputobj = new ObjectInputStream(inputb);
					Serializable serDrawInfo = (Serializable) inputobj
					        .readObject();
					data = serDrawInfo;
				} catch (Exception e) {
					e.printStackTrace();
				}
				currentNoteItem = new NoteHandWriteBaselineItem();
				currentNoteItem.load(data, null);
			} else if(noteItems[i] instanceof NoteHandWriteItem){
				Serializable data = ((NoteHandWriteItem) noteItems[i]).save();
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				try {
					ObjectOutputStream obj = new ObjectOutputStream(b);
					obj.writeObject(data);

					ByteArrayInputStream inputb = new ByteArrayInputStream(
					        b.toByteArray());
					ObjectInputStream inputobj = new ObjectInputStream(inputb);
					Serializable serDrawInfo = (Serializable) inputobj
					        .readObject();
					data = serDrawInfo;
				} catch (Exception e) {
					e.printStackTrace();
				}
				currentNoteItem = new NoteHandWriteItem();
				currentNoteItem.load(data, null);
			} else if(noteItems[i] instanceof NoteTimestampItem){
				Serializable data = ((NoteTimestampItem) noteItems[i]).save();
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				try {
					ObjectOutputStream obj = new ObjectOutputStream(b);
					obj.writeObject(data);

					ByteArrayInputStream inputb = new ByteArrayInputStream(
					        b.toByteArray());
					ObjectInputStream inputobj = new ObjectInputStream(inputb);
					Serializable serDrawInfo = (Serializable) inputobj
					        .readObject();
					data = serDrawInfo;
				} catch (Exception e) {
					e.printStackTrace();
				}
				currentNoteItem = new NoteTimestampItem();
				((NoteTimestampItem)currentNoteItem).load(data, getImageSpanHeight());
			} else if(noteItems[i] instanceof NoteSendIntentItem){
				Serializable data = ((NoteSendIntentItem) noteItems[i]).save();
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				try {
					ObjectOutputStream obj = new ObjectOutputStream(b);
					obj.writeObject(data);

					ByteArrayInputStream inputb = new ByteArrayInputStream(
					        b.toByteArray());
					ObjectInputStream inputobj = new ObjectInputStream(inputb);
					Serializable serDrawInfo = (Serializable) inputobj
					        .readObject();
					data = serDrawInfo;
				} catch (Exception e) {
					e.printStackTrace();
				}
				currentNoteItem = new NoteSendIntentItem();
				currentNoteItem.load(data, null);
			}

			editable.setSpan(currentNoteItem, noteItems[i].getStart(),
			        noteItems[i].getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);			
		}
		setFontSize(editable);
		return editable;
	}

	private static int mfontsize = -1;
	private static int mTextheight = -1;

	private int getImageSpanHeight() {
		FontMetricsInt fontMetricsInt;
		Paint paint = new Paint();
		paint.setTextSize(mfontsize);
		fontMetricsInt = paint.getFontMetricsInt();
		return (int) (fontMetricsInt.descent * PageEditor.FONT_DESCENT_RATIO - fontMetricsInt.ascent);
	}

	private int getFullImageSpanHeight() {
		FontMetricsInt fontMetricsInt;
		Paint paint = new Paint();
		paint.setTextSize(mfontsize);
		fontMetricsInt = paint.getFontMetricsInt();
		return (int) (mTextheight + fontMetricsInt.descent
		        * PageEditor.FONT_DESCENT_RATIO);
	}

	public void setFontSize(Editable editable) {
		NoteHandWriteItem[] handwriteitems = editable.getSpans(0,
				editable.length(), NoteHandWriteItem.class);
		for (NoteHandWriteItem item : handwriteitems) {
			if (item instanceof NoteHandWriteBaselineItem) {
				item.setFontHeight(getImageSpanHeight());
			} else {
				item.setFontHeight(getFullImageSpanHeight());
			}
		}
	}

	private void hideSoftkeyboard() {
		if (mListView != null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mListView.getWindowToken(), 0);
		}
	}

	public void finishFromActivity() {
		if (mShareActivity != null) {
			mShareActivity.finish();
			mShareActivity = null;
		}

	}
}
