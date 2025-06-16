package com.asus.supernote.textsearch;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.inksearch.SearchViewHolder;
import com.asus.supernote.picker.PickerActivity;

public class TextSearchResultAdapter extends BaseAdapter {
	private final Context mContext;
	private List<TextSearchResult> mResultList;

	public TextSearchResultAdapter(Context context, List<TextSearchResult> list) {
		super();
		mContext = context;
		mResultList = list;
	}

	public void setEditableList(List<TextSearchResult> resultList) {
		this.mResultList = resultList;
	}



	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mResultList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mResultList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView != null && !(convertView.getTag() instanceof SearchViewHolder))
		{
			convertView = null;
		}

		final TextSearchResult item = mResultList.get(position);
		if (item.mResultSort == TextSearchResult.ResultSort_TEXTBOOK
		        || item.mResultSort == TextSearchResult.ResultSort_TEXTPAGE) {
			SortSearchHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(mContext,
				        R.layout.single_page_search_result_head_row, null);
				holder = new SortSearchHolder();
				holder.sortText = (TextView) convertView
				        .findViewById(R.id.Search_page_title);
				convertView.setTag(holder);
			} else {
				holder = (SortSearchHolder) convertView.getTag();
			}
			holder.sortText.setText(item.getSpecialString());
			return convertView;
		}

		TextSearchHolder holder = null;
		if (convertView == null) {
			convertView = View.inflate(mContext,
					R.layout.single_page_search_result_row, null);
			holder = new TextSearchHolder();
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
			holder.pageCount = (TextView) convertView
					.findViewById(R.id.Search_page_title_book_count);
			convertView.setTag(holder);
		} else {
			holder = (TextSearchHolder) convertView.getTag();
		}		
		
		switch (item.mResultSort) {
		case TextSearchResult.ResultSort_NOTEBOOK:
			holder.itemBar.setVisibility(View.GONE);
			holder.pageCount.setVisibility(View.GONE);
			holder.bookName.setText(item.mEditable);
			holder.titleBar.setOnClickListener(new OnClickListener() {//darwin
				
				@Override
				public void onClick(View v) {
					// Jump to page view.
						
					try {
						TextSearchActivity activity = (TextSearchActivity)mContext;
						if(activity.isShareMode()){
							Intent intent = new Intent();
							intent.setClass(mContext, EditorActivity.class);
							BookCase mBookcase = BookCase.getInstance(mContext);
							NoteBook noteBook = mBookcase.getNoteBook(item.mBookID);
							NotePage notePage = createNewPageAndLoad(noteBook);
							long lastPageId = notePage.getCreatedTime();
							intent.putExtra(MetaData.BOOK_ID, item.mBookID); //emmanual to fix bug 576805
							intent.putExtra(MetaData.PAGE_ID, lastPageId);
							intent.putExtra(MetaData.IS_NEW_PAGE, false);
							intent.putExtra(Intent.EXTRA_INTENT, activity.getShareIntent());
							mContext.startActivity(intent);
							activity.finishFromActivity();
							activity.finish();
						}else{
							Intent intent = new Intent(mContext, PickerActivity.class);
							intent.putExtra(MetaData.BOOK_ID, item.mBookID);
							mContext.startActivity(intent);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					

				}
			});
			break;
		case TextSearchResult.ResultSort_NOTEPAGE:
			holder.itemBar.setVisibility(View.GONE);
			holder.bookName.setText(item.mBookName);
			if(item.mCount == 1)
			{
				holder.pageCount.setText(String.valueOf(item.mCount) + " " + mContext.getResources().getString(R.string.SearchResultItemInfo_Page_Space));
			}
			else
			{
				holder.pageCount.setText(String.valueOf(item.mCount) + " " + mContext.getResources().getString(R.string.SearchResultItemInfo_Pages));
			}
			break;
		case TextSearchResult.ResultSort_PAGECONTENT:
			holder.titleBar.setVisibility(View.GONE);
			final TextSearchActivity searchActivity = (TextSearchActivity)mContext;
			holder.editText.setText(item.mEditable);
			holder.pageIndex.setText(item.getPageIndexString());
			holder.itemBar.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Intent intent = new Intent(mContext, EditorActivity.class);
						intent.putExtra(MetaData.BOOK_ID, item.mBookID);
						intent.putExtra(MetaData.PAGE_ID, item.mPageID);
						if(searchActivity.isShareMode()){
							intent.putExtra(Intent.EXTRA_INTENT, searchActivity.getShareIntent());
						}
						mContext.startActivity(intent);
						if(searchActivity.isShareMode()){
							searchActivity.finish();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			break;
		default:
			break;
		}
		return convertView;
	}	

	public NotePage createNewPageAndLoad(NoteBook book) {
		NotePage notepage = new NotePage(mContext, book.getCreatedTime());
		notepage.setTemplate(book.getTemplate());
		notepage.setIndexLanguage(book.getIndexLanguage());
		book.addPage(notepage);
		PageDataLoader loader = PageDataLoader.getInstance(mContext);
		loader.load(notepage);
		return notepage;
	}
	
	public class SortSearchHolder{
		TextView sortText;
	}

	public class TextSearchHolder {
		FrameLayout titleBar;
		FrameLayout itemBar;
		TextView editText;
		TextView pageIndex;
		TextView bookName;
		TextView pageCount;
	}
}
