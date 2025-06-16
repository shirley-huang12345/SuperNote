package com.asus.supernote.picker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.ui.CoverHelper;

public class AllPageViewAdapter extends BaseAdapter {
    public static final String TAG = "AllPageViewAdapter";
    private static final int TYPE_TITLE = 0;
    private static final int TYPE_ITEM = 1;
//BEGIN: RICHARD MODIFY FOR MULTI WINDOW
    private int sItemPerRow = 1;
    private int mItemPerRowMax = 1;
//END: RICHARD MODIFY FOR MULTI WINDOW

    private final Context mContext;
    private final Resources mRes;
    private final BookCase mBookcase;
    private final ContentResolver mContentResolver;
    private final Date mPageDate;
    private java.text.DateFormat mTimeFormat;
    private java.text.DateFormat mDateFormat;
    private Cursor mCursor = null;
    private Cursor mPageCursor = null;
    private List<BookPageMappingTable> mappingList;
    private SortedSet<SimplePageInfo> mSelectedItems;
    private List<SimplePageInfo> mPagesInfo;
    private boolean mIsNextBook;
    private boolean mIsSelectable = false;
    private boolean mSelectedAll;
    private boolean mIsBookmark = false;
    private int mItemNum = 0;
    private int mBookNum = 0;
    private int mPageNum = 0;
    private Bitmap mLoadingPageCover;
    private Bitmap mLoadingPageCoverYellow;
    private DataCounterListener mDataCounterListener;
    //begin wendy
    private Long mLatestEditPageId = 0L;
    private Long mLatestEditBookId = 0L;
    private boolean mHasLastestEditPage = false;
    //end wendy

    //darwin
    static private boolean isNotebookAllPageProcessing = false;
    static private Object mLockObj = new Object();
    public boolean getIsNotebookAllPageProcessing()
    {
    	synchronized (mLockObj)
		{
    		if(isNotebookAllPageProcessing == true)
    		{
    			return true;
    		}
    		else
    		{
    			isNotebookAllPageProcessing = true;
    			return false;
    		}
		}
    }
    //begin smilefish
    public int getPageCount() {
		return mPageNum;
	}
    //end smilefish

    static public void resetNotebookAllPageProcessing()
    {
    	synchronized (mLockObj)
		{
    		isNotebookAllPageProcessing = false;
		}
    }
    //darwin
    
    public void setDateAndTimeFormat(){
        mTimeFormat = DateFormat.getTimeFormat(mContext);
        mDateFormat = DateFormat.getDateFormat(mContext);//smilefish
    }
    
    public AllPageViewAdapter(Context context, boolean isBookmark) {
        mContext = context;
        mRes = mContext.getResources();
        mContentResolver = mContext.getContentResolver();
        mSelectedItems = new TreeSet<SimplePageInfo>(new PageComparator());
        mPagesInfo = new ArrayList<SimplePageInfo>();
        mIsBookmark = isBookmark;
        mBookcase = BookCase.getInstance(mContext);
        mPageDate = new Date();
        setDateAndTimeFormat();
        changeData();
        // BEGIN: archie_huang@asus.com
        mLoadingPageCover = BitmapFactory.decodeResource(mRes, R.drawable.asus_supernote_cover2_blank_white_local);
        mLoadingPageCoverYellow = BitmapFactory.decodeResource(mRes, R.drawable.asus_supernote_cover2_blank_yellow_local);
        // END: archie_huang@asus.com
    }
    
    //begin emmanual
    private AllPageViewFragment mAllPageFragment;
    public AllPageViewAdapter(Context context, AllPageViewFragment fragment, boolean isBookmark) {
    	this(context,isBookmark);
    	mAllPageFragment = fragment;
    }
    //end emmanual

//BEIGN: RICHARD MODIFY FOR MULTI WINDOW    
    public int calculateItemPerRow(int width)
    {
    	float leftMargin = mContext.getResources().getDimension(R.dimen.multi_window_page_item_left_margin);
    	float disatanceBetweenPageItem =  mContext.getResources().getDimension(R.dimen.all_page_view_itemspace);
    	float pageItemWidth = mContext.getResources().getDimension(R.dimen.multi_window_page_item);
    	int itemperRow = (int)((width - leftMargin + disatanceBetweenPageItem)/(disatanceBetweenPageItem + pageItemWidth));
    	if(itemperRow < 1)
    	{
    		itemperRow = 1;
    	}
    	
    	if(itemperRow != sItemPerRow)
    	{
    		sItemPerRow = itemperRow;
        	//begin smilefish
        	sItemPerRow = mItemPerRowMax;
        	//end smilefish
    		changeData();
    	}
    	
    	return sItemPerRow;
    }
//END: RICHARD MODIFY FOR MULTI WINDOW    
    @Override
    public int getCount() {
        return mItemNum;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    //Begin Allen
    private void attachItem(ViewHolder holder,View convertView,int itemPerRow){
        for(int i=0;i<itemPerRow;i++){
        	switch(i){
        	case 0:
        		holder.pageItem[0].pageLayout = convertView.findViewById(R.id.all_page_1);
        		break;
        	case 1:
        		holder.pageItem[1].pageLayout = convertView.findViewById(R.id.all_page_2);
        		break;
        	case 2:
        		holder.pageItem[2].pageLayout = convertView.findViewById(R.id.all_page_3);
        		break;
        	case 3:
        		holder.pageItem[3].pageLayout = convertView.findViewById(R.id.all_page_4);
        		break;
        	case 4:
        		holder.pageItem[4].pageLayout = convertView.findViewById(R.id.all_page_5);
        		break;
        	case 5:
        		holder.pageItem[5].pageLayout = convertView.findViewById(R.id.all_page_6);
        		break;
        	default:
        		return;
        	}
        	findPageItem(holder, i);
        }
    }
    //End Allen
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        BookPageMappingTable bp = null;
        int type = (mIsNextBook) ? TYPE_TITLE : TYPE_ITEM;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.all_page_row, null);
            holder = new ViewHolder();
            holder.titleBar = (FrameLayout) convertView.findViewById(R.id.all_page_title_layout);
            holder.itemBar = (LinearLayout) convertView.findViewById(R.id.all_page_item_layout);
            holder.bookName = (TextView) convertView.findViewById(R.id.all_page_title_book_name);
            holder.bookCount = (TextView) convertView.findViewById(R.id.all_page_title_book_count);  
            holder.bookmark = (ImageView)convertView.findViewById(R.id.all_page_bookmark);//smilefish

        	attachItem(holder,convertView,mItemPerRowMax);
            
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        for (BookPageMappingTable item : mappingList) {
            if (item.startRowIndex > position) {
                break;
            }
            else {
                bp = item;
            }
        }
        type = (position == bp.startRowIndex) ? TYPE_TITLE : TYPE_ITEM;
        mCursor.moveToPosition(bp.bookIndex);
        NoteBook notebook = mBookcase.getNoteBook(mCursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE));
        //emmanual to fix bug 482411
        if(notebook == null){
        	return convertView;
        }
        Long bookId = notebook.getCreatedTime();
        Long pageId = 0L;

        // BEGIN: archie_huang@asus.com
        int color = notebook.getBookColor();
        // END: archie_huang@asus.com

        mPagesInfo.clear();
        if (mPageCursor != null) {
            mPageCursor.close();
        }
        if (mIsBookmark) {
        	holder.bookmark.setVisibility(View.VISIBLE);//smilefish
            mPageCursor = mContentResolver.query(MetaData.PageTable.uri, null, "owner = ? AND is_bookmark = ?", new String[] { bookId.toString(), "1" }, null);
        }
        else {
        	holder.bookmark.setVisibility(View.GONE);//smilefish
            mPageCursor = mContentResolver.query(MetaData.PageTable.uri, null, "owner = ?", new String[] { bookId.toString() }, null);
        }
        mPageCursor.moveToFirst();
        while (!mPageCursor.isAfterLast()) {
            pageId = mPageCursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE);
            int pageIndex = notebook.getPageIndex(pageId);
            mPagesInfo.add(new SimplePageInfo(bookId, pageId, 0, pageIndex));
            mPageCursor.moveToNext();
        }
        Collections.sort(mPagesInfo, new PageComparator());
        if (type == TYPE_TITLE) {
            if (mPageCursor.getCount() == 0) {
                holder.titleBar.setVisibility(View.GONE);
                holder.itemBar.setVisibility(View.GONE);
                holder.bookName.setText(notebook.getTitle());
                String bookCountText = "";
                if(mIsBookmark)
                {
                	bookCountText = String.format(mRes.getString(R.string.pg_title_counter), mPageCursor.getCount());
                }
                else
                {
                	bookCountText = mPageCursor.getCount() + " " + mRes.getString(R.string.SearchResultItemInfo_Page_Space);
                }
                holder.bookCount.setText(bookCountText);
            }
            else {
                holder.titleBar.setVisibility(View.VISIBLE);
                holder.itemBar.setVisibility(View.VISIBLE);
                holder.bookName.setText(notebook.getTitle());
                String bookCountText = "";                 
                
                if(mIsBookmark)
                {
                	bookCountText = String.format(mRes.getString(R.string.pg_title_counter), mPageCursor.getCount());
                }
                else
                {
                	if(mPageCursor.getCount() == 1)
                	{
                		bookCountText = mPageCursor.getCount() + " " + mRes.getString(R.string.SearchResultItemInfo_Page_Space);
                	}
                	else
                	{
                		bookCountText = mPageCursor.getCount() + " " + mRes.getString(R.string.SearchResultItemInfo_Pages);
                	}
                }
                holder.bookCount.setText(bookCountText);
                for (int i = 0; i < mItemPerRowMax; ++i) {//richard modify for multi window
                    if (i < mPagesInfo.size() && i<sItemPerRow) {//richard modify for multi window
                        SimplePageInfo pageInfo = mPagesInfo.get(i);
                        bookId = pageInfo.bookId;
                        pageId = pageInfo.pageId;
                        // BEGIN: Shane_Wang 2012-10-8
                        Cursor modCursor = mContext.getContentResolver().query(MetaData.PageTable.uri, 
            					new String[] {MetaData.PageTable.MODIFIED_DATE}, "created_date = ?",
            					new String[] { Long.toString(pageId) }, null);
            			modCursor.moveToFirst();
            			long modTime = modCursor.getLong(0);
            			modCursor.close();
                        mPageDate.setTime(modTime);
                        // END: Shane_Wang 2012-10-8
                        
                        Drawable oldDrawable = holder.pageItem[i].pageCover.getDrawable();
                        if (oldDrawable != null && oldDrawable instanceof BitmapDrawable) {
                            BitmapDrawable oldBitmapDrawable = (BitmapDrawable) oldDrawable;
                            Bitmap oldBitmap = oldBitmapDrawable.getBitmap();
                            if (oldBitmap != null) {
                                boolean same = false;
                                if(oldBitmap.sameAs(mLoadingPageCover) ||oldBitmap.sameAs(mLoadingPageCoverYellow))
                                {
                                	same = true;
                                }
                                //END: RICHARD
                                if (same == false && oldBitmap.isRecycled() == false) {
                                    oldBitmap.recycle();
                                    oldBitmap = null;
                                }
                            }
                        }
                        switch (color) {
                            case MetaData.BOOK_COLOR_BLUE:
                            case MetaData.BOOK_COLOR_WHITE:
                                holder.pageItem[i].pageCover.setImageBitmap(mLoadingPageCover);
                                break;
                            case MetaData.BOOK_COLOR_YELLOW:
                                holder.pageItem[i].pageCover.setImageBitmap(mLoadingPageCoverYellow);
                                break;
                            default:
                                holder.pageItem[i].pageCover.setImageBitmap(mLoadingPageCover);
                                break;
                        }
                        holder.pageItem[i].pageLayout.setVisibility(View.VISIBLE);
                        holder.pageItem[i].pageNumber.setText("" + (i + 1));
                        holder.pageItem[i].pageTime.setText(mTimeFormat.format(mPageDate));
                        holder.pageItem[i].pageDate.setText(mDateFormat.format(mPageDate));
                        holder.pageItem[i].pageSelected.setVisibility(mIsSelectable && isPageSelected(pageId)? View.VISIBLE : View.GONE);
                        holder.pageItem[i].pageSelected.setTag(R.string.book_id, bookId);
                        holder.pageItem[i].pageSelected.setTag(R.string.page_id, pageId);
                        holder.pageItem[i].pageSelected.setOnClickListener(mIsSelectable ? selectedPageListener : null);
                        holder.pageItem[i].pageLayout.setTag(R.string.book_id, bookId);
                        holder.pageItem[i].pageLayout.setTag(R.string.page_id, pageId);
                        holder.pageItem[i].pageLayout.setOnClickListener(mIsSelectable ? selectedPageListener : onPageClickListener);                       
                        holder.pageItem[i].setLongClickListener();//emmanual                        
                        holder.pageItem[i].pageCoverMask.setTag(holder.pageItem[i].pageLayout);
                        holder.pageItem[i].pageCoverMask.setOnClickListener(onMaskClickListener);  
                        
                        new GetBitmapTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, holder.pageItem[i]);
                    }
                    else {
                    	if(i < holder.pageItem.length){ //smilefish fix bug 543558
	                        holder.pageItem[i].pageLayout.setVisibility(View.INVISIBLE);
	                        holder.pageItem[i].pageLayout.setOnClickListener(null);
                    	}
                    }
                }
            }
        }
        else if (type == TYPE_ITEM) {
            holder.titleBar.setVisibility(View.GONE);
            holder.itemBar.setVisibility(View.VISIBLE);
            int indexShift = (position - bp.startRowIndex) * sItemPerRow;
            for (int i = 0; i < mItemPerRowMax; ++i) {//richard modify for multi window
                if ((i + indexShift) < mPagesInfo.size()  && i<sItemPerRow) {//richard modify for multi window
                    SimplePageInfo pageInfo = mPagesInfo.get((i + indexShift));
                    bookId = pageInfo.bookId;
                    pageId = pageInfo.pageId;
                    mPageDate.setTime(pageId);

                    Drawable oldDrawable = holder.pageItem[i].pageCover.getDrawable();
                    if (oldDrawable != null && oldDrawable instanceof BitmapDrawable) {
                        BitmapDrawable oldBitmapDrawable = (BitmapDrawable) oldDrawable;
                        Bitmap oldBitmap = oldBitmapDrawable.getBitmap();
                        if (oldBitmap != null) {
                            boolean same = false;
                          if(oldBitmap.sameAs(mLoadingPageCover) ||oldBitmap.sameAs(mLoadingPageCoverYellow))
                          {
                          	same = true;
                          }
                          //END: RICHARD
                            if (same == false && oldBitmap.isRecycled() == false) {
                                oldBitmap.recycle();
                                oldBitmap = null;
                            }

                        }
                    }
                    switch (color) {
                        case MetaData.BOOK_COLOR_BLUE:
                        case MetaData.BOOK_COLOR_WHITE:
                            holder.pageItem[i].pageCover.setImageBitmap(mLoadingPageCover);
                            break;
                        case MetaData.BOOK_COLOR_YELLOW:
                            holder.pageItem[i].pageCover.setImageBitmap(mLoadingPageCoverYellow);
                            break;
                        default:
                            holder.pageItem[i].pageCover.setImageBitmap(mLoadingPageCover);
                            break;
                    }

                    holder.pageItem[i].pageLayout.setVisibility(View.VISIBLE);
                    holder.pageItem[i].pageNumber.setText("" + (indexShift + i + 1));
                    holder.pageItem[i].pageTime.setText(mTimeFormat.format(mPageDate));
                    holder.pageItem[i].pageDate.setText(mDateFormat.format(mPageDate));
                    holder.pageItem[i].pageSelected.setVisibility(mIsSelectable && isPageSelected(pageId)? View.VISIBLE : View.GONE);
                    holder.pageItem[i].pageSelected.setTag(R.string.book_id, bookId);
                    holder.pageItem[i].pageSelected.setTag(R.string.page_id, pageId);
                    holder.pageItem[i].pageSelected.setOnClickListener(mIsSelectable ? selectedPageListener : null);
                    holder.pageItem[i].pageLayout.setTag(R.string.book_id, bookId);
                    holder.pageItem[i].pageLayout.setTag(R.string.page_id, pageId);
                    holder.pageItem[i].pageLayout.setOnClickListener(mIsSelectable ? selectedPageListener : onPageClickListener);
                    holder.pageItem[i].setLongClickListener();
                    holder.pageItem[i].pageCoverMask.setTag(holder.pageItem[i].pageLayout);
                    holder.pageItem[i].pageCoverMask.setOnClickListener(onMaskClickListener);
                    
                    new GetBitmapTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, holder.pageItem[i]);
                }
                else {
                	if(i < holder.pageItem.length){ //smilefish fix bug 543558
	                    holder.pageItem[i].pageLayout.setVisibility(View.INVISIBLE);
	                    holder.pageItem[i].pageLayout.setOnClickListener(null);
                	}
                }
            }
        }
        mPageCursor.close();
        return convertView;
    }

    private class ViewHolder {
        FrameLayout titleBar;
        LinearLayout itemBar;
        TextView bookName;
        TextView bookCount;
        PageItemHolder[] pageItem = new PageItemHolder[mItemPerRowMax];//richard modify for multi window
        //darwin
        ImageView bookmark; //smilefish

        //darwin

        public ViewHolder() {
            for (int i = 0; i < mItemPerRowMax; ++i) {//richard modify for multi window
                pageItem[i] = new PageItemHolder();
            }
        }
    }

    private class PageItemHolder {
        View pageLayout;
        ImageView pageCover;
        ImageView pageCoverMask;
        TextView pageNumber;
        TextView pageTime;
        TextView pageDate;
        ImageView pageSelected;//smilefish

		public void setLongClickListener() {
			pageCoverMask.setTag(pageLayout);
			pageCoverMask.setOnLongClickListener(onPageLongClickListener);
		}
    }

    private void findPageItem(ViewHolder holder, int index) {
        holder.pageItem[index].pageCover = (ImageView) holder.pageItem[index].pageLayout.findViewById(R.id.page_cover);
        holder.pageItem[index].pageCoverMask = (ImageView) holder.pageItem[index].pageLayout.findViewById(R.id.page_cover_mask);
        holder.pageItem[index].pageNumber = (TextView) holder.pageItem[index].pageLayout.findViewById(R.id.page_number);
        holder.pageItem[index].pageTime = (TextView) holder.pageItem[index].pageLayout.findViewById(R.id.page_time);
        holder.pageItem[index].pageDate = (TextView) holder.pageItem[index].pageLayout.findViewById(R.id.page_date);
        holder.pageItem[index].pageSelected = (ImageView) holder.pageItem[index].pageLayout.findViewById(R.id.page_check);
    }

    private static class BookPageMappingTable {
        int bookIndex;
        int startRowIndex;
    }

    private final View.OnClickListener onMaskClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View view = (View) v.getTag();
            view.performClick();
        }
    };

    private final OnClickListener onPageClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	//darwin
        	if(getIsNotebookAllPageProcessing())
        	{
        		return;
        	}
            //darwin
        	// BEGIN: Better
        	long pageId = (Long) v.getTag(R.string.page_id);
        	if (!MetaData.SavingPageIdList.contains(pageId)) {
        		NoteBook book = mBookcase.getNoteBook((Long) v.getTag(R.string.book_id));
        		  if(book == null) return ;        		
        		  if (book.getIsLocked()&& NoteBookPickerActivity.islocked()) {//wendy
                      launchLatestEditPageInPersonal();
                  }else
                  {
	                NotePage page = book.getNotePage((Long) v.getTag(R.string.page_id));
	                if (page != null) {
		            	try
		            	{
							Intent intent = new Intent(mContext, EditorActivity.class);
							intent.putExtra(MetaData.BOOK_ID, book.getCreatedTime());
							intent.putExtra(MetaData.PAGE_ID, pageId);
							mContext.startActivity(intent);
		            	}catch(Exception e)
		            	{
		            		e.printStackTrace();
		            	}
		            	AllPageViewFragment.mSearchString = "";//RICHARD
	                }
        		}
            }
        	// END: Better
        }
    };
    
    //begin emmanual
	private final OnLongClickListener onPageLongClickListener = new OnLongClickListener() {
		public boolean onLongClick(View v) {
			final long pageId = (Long) ((View) v.getTag())
			        .getTag(R.string.page_id);
			if (!MetaData.SavingPageIdList.contains(pageId)) {
				final NoteBook book = mBookcase.getNoteBook((Long) ((View) v
				        .getTag()).getTag(R.string.book_id));
				if (book == null)
					return false;
				//emmanual to fix bug 448557
				if(mIsSelectable){
					return false;
				}
				if (book.getIsLocked() && NoteBookPickerActivity.islocked()) {// wendy
					launchLatestEditPageInPersonal();
				} else {
					final NotePage page = book.getNotePage((Long) ((View) v
					        .getTag()).getTag(R.string.page_id));
					if (page != null) {
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						View view = View.inflate(mContext, R.layout.share_actionitem_dialog, null);
						ListView shareFormatLV = (ListView) view.findViewById(R.id.share_format_listview);

						String[] bookmarkText = { mContext.getResources().getString(R.string.nb_del) };
						String[] allpageText = {mContext.getResources().getString(R.string.nb_del),
						        mContext.getResources().getString(R.string.export) };
						final long bookId = book.getCreatedTime();
				
						final int bookIndex = mBookcase.getNoteBookIndex(bookId);
						final int pageIndex = mBookcase.getNoteBook(bookId).getPageIndex(pageId);
						//if the page is selected, just return
				        SimplePageInfo pageInfo = new SimplePageInfo(bookId, pageId, bookIndex,pageIndex);
				        
				        //emmanual to fix bug 423958
				        mSelectedItems.clear();
				        mSelectedItems.add(pageInfo);
				        
						String title;
						if (mIsBookmark) {
							shareFormatLV.setAdapter(new ArrayAdapter<String>(mContext, R.layout.share_format_text, bookmarkText));
							title = String.format(mContext.getResources().getString(R.string.bm_in_notebook), 
									book.getTitle());
						} else {
							shareFormatLV.setAdapter(new ArrayAdapter<String>(mContext, R.layout.share_format_text, allpageText));
							title = String.format(mContext.getResources().getString(R.string.pg_in_notebook), 
									Integer.toString(pageIndex + 1), book.getTitle());
						}

						builder.setTitle(title)
						        .setView(view);
						final AlertDialog dialog = builder.create();
						dialog.show();

						shareFormatLV
						        .setOnItemClickListener(new OnItemClickListener() {
							        public void onItemClick(AdapterView<?> a,
							                View v, int position, long id) {
								        SimplePageInfo pageInfo = new SimplePageInfo(bookId, pageId, bookIndex,pageIndex);
								        mSelectedItems.clear();
								        mSelectedItems.add(pageInfo);
								        if (position == 0) {
											if (mIsBookmark) {
												mAllPageFragment.removeBookmarks();
											}else{
												mAllPageFragment.deletePages();
											}
								        } else if (position == 1
								                && !mIsBookmark) {
								        	mAllPageFragment.chooseExport(); //emmanual to fix bug 438156								        
								        }
								        dialog.cancel();
							        }
						        });
					}
				}
			}
			return true;
		}
	};
    //end emmanual
	
	private void launchLatestEditPage() {
		if (mLatestEditBookId != 0L && mLatestEditPageId != 0L) {
			NoteBook book = mBookcase.getNoteBook(mLatestEditBookId);

			NotePage page = book.getNotePage(mLatestEditPageId);
			if (page != null) {				
            	try
            	{
					Intent intent = new Intent(mContext, EditorActivity.class);
					intent.putExtra(MetaData.BOOK_ID, mLatestEditBookId);
					intent.putExtra(MetaData.PAGE_ID, mLatestEditPageId);
					mContext.startActivity(intent);
            	}catch(Exception e)
            	{
            		e.printStackTrace();
            	}
			}

		}
	}

	    private void launchLatestEditPageInPersonal() {
	        //using one_input_dialog
	        final View view = View.inflate(mContext, R.layout.one_input_dialog, null);
	        final EditText editText = (EditText) view.findViewById(R.id.input_edit_text);
	        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	        builder.setTitle(R.string.password);
	        builder.setView(view);
	        builder.setPositiveButton(android.R.string.ok, null);
	        builder.setNegativeButton(android.R.string.cancel, null);
	        final AlertDialog dialog = builder.create();
	        dialog.show();
	        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {	            	  
	            	SharedPreferences  Preference = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
               
	                String password = Preference.getString(mRes.getString(R.string.pref_password), null);
	                if (password == null) {
	                    dialog.dismiss();
	                }
	                else if (password.equals(editText.getText().toString())) {
	                    dialog.dismiss();
	                    launchLatestEditPage();
	                }
	                else {
	                    editText.setText("");
	                    editText.setHint(R.string.password_diff_password);
	                }
	            }
	        });

	    }
    //end wendy
    
    public void changeData() {
    	//BEGIN: RICHARD MODIFY FOR MULTI WINDOW
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mItemPerRowMax = mRes.getInteger(R.integer.item_per_row);//Allen
        }
        else {
            mItemPerRowMax = mRes.getInteger(R.integer.item_per_row_landscape);//Allen
        }
        if(sItemPerRow > mItemPerRowMax)
        {
        	sItemPerRow = mItemPerRowMax;
        }
        //END: RICHARD
        if (mCursor != null) {
            mCursor.close();
        }
        //begin wendy
		mHasLastestEditPage = false;
        boolean islocked = NoteBookPickerActivity.islocked();
        String selection = islocked?"(is_locked = 0) AND ((userAccount = 0) OR (userAccount = ?))":"((userAccount = 0) OR (userAccount = ?))";
        //end wendy
        mCursor = mContext.getContentResolver().query(MetaData.BookTable.uri, null, selection, new String[]{ Long.toString(MetaData.CurUserAccount)}, "title");
        mBookNum = mCursor.getCount();

        dataChange(mCursor.getCount());

        mIsNextBook = true;
        mItemNum = 0;
        if (mappingList == null) {
            mappingList = new ArrayList<BookPageMappingTable>();
        }
        else {
            mappingList.clear();
        }
        mPageNum = 0;
        for (int i = 0; i < mBookNum; ++i) {
            mCursor.moveToPosition(i);
            BookPageMappingTable rowMapping = new BookPageMappingTable();
            //begin wendy
            rowMapping.bookIndex = i;
            rowMapping.startRowIndex = mHasLastestEditPage? mItemNum+1 : mItemNum;
            //end wendy
            mappingList.add(rowMapping);
            NoteBook notebook = mBookcase.getNoteBook(mCursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE));
            Cursor pageCursor = null;
            if(notebook == null) continue; //smilefish fix bug 586505
            if (mIsBookmark) {
                pageCursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null, "owner = ? AND is_bookmark = ?", new String[] { notebook.getCreatedTime().toString(), "1" }, null);
            }
            else {
                pageCursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null, "owner = ?", new String[] { notebook.getCreatedTime().toString() }, null);
            }
            int pageNumber = pageCursor.getCount();
            mItemNum = mItemNum + (pageNumber / sItemPerRow) + (((pageNumber % sItemPerRow) == 0) ? 0 : 1);
            mItemNum = (pageNumber == 0) ? (mItemNum + 1) : mItemNum;
            mPageNum = mPageNum + pageNumber;
            pageCursor.close();
        }

        notifyDataSetChanged();
    }

    public void setSelectedAll(boolean enabled) {
        if (mSelectedItems == null) {
            mSelectedItems.clear();
            mSelectedItems = new TreeSet<SimplePageInfo>(new PageComparator());
        }
        mSelectedAll = (mSelectedItems.size() == mPageNum) ? false : enabled;
        if (mSelectedAll) {
        	mSelectedItems.clear(); //smilefish
            Cursor cursor = null;
            if (mIsBookmark) {
                cursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null, "is_bookmark > 0", null, null);
            }
            else {
                cursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null, null, null, null);
            }
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Long bookId = cursor.getLong(MetaData.PageTable.INDEX_OWNER);
                Long pageId = cursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE);
                NoteBook book = mBookcase.getNoteBook(bookId);
                if (book != null && book.getIsLocked() == false
                		||(book.getIsLocked() == true && NoteBookPickerActivity.getIsLock() == false )) {
                    int bookIndex = mBookcase.getNoteBookIndex(bookId);
                    int pageIndex = mBookcase.getNoteBook(bookId).getPageIndex(pageId);
                    SimplePageInfo pageInfo = new SimplePageInfo(bookId, pageId, bookIndex, pageIndex);
                    mSelectedItems.add(pageInfo);
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        else {
            mSelectedItems.clear();
        }
        selectedDataChange(mSelectedItems.size());

        notifyDataSetChanged();
    }

    public boolean isSelectedAll() {
        return mSelectedAll;
    }

    public void setItemSelectable(boolean b) {
        mIsSelectable = b;
    }

    public void requeryData() {
        mCursor.requery();
        notifyDataSetChanged();
    }

    public SortedSet<SimplePageInfo> getSelectedItems() {
        return mSelectedItems;
    }

    public void clearSelectedItems() {
        if (mSelectedItems != null) {
            mSelectedItems.clear();
        }
    }

    private boolean isPageSelected(Long pageId) {
        if (mSelectedItems == null) {
            return false;
        }
        else {
            for (SimplePageInfo info : mSelectedItems) {
                if (info.pageId.equals(pageId)) {
                    return true;
                }
            }
            return false;
        }
    }

    private View.OnClickListener selectedPageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Long bookId = (Long) v.getTag(R.string.book_id);
            Long pageId = (Long) v.getTag(R.string.page_id);
            int bookIndex = mBookcase.getNoteBookIndex(bookId);
            int pageIndex = mBookcase.getNoteBook(bookId).getPageIndex(pageId);
            SimplePageInfo pageInfo = new SimplePageInfo(bookId, pageId, bookIndex, pageIndex);
            if (mSelectedItems.contains(pageInfo) == false) {
                mSelectedItems.add(pageInfo);
            }
            else {
                mSelectedItems.remove(pageInfo);
            }
            selectedDataChange(mSelectedItems.size());

            notifyDataSetChanged();
        }
    };

    public void closeCursor() {
        if (mCursor != null) {
            mCursor.close();
        }
        if (mPageCursor != null) {
            mPageCursor.close();
        }
    }

    public void registerDataCounterListener(DataCounterListener listener) {
        mDataCounterListener = listener;
    }

    public void removeDataCounterListener() {
        mDataCounterListener = null;
    }

    private void dataChange(int count) {
        if (mDataCounterListener != null) {
            mDataCounterListener.onDataChange(count);
        }
    }

    private void selectedDataChange(int count) {
        if (mDataCounterListener != null) {
            mDataCounterListener.onSelectedDataChange(count);
        }
    }

    public class GetBitmapTask extends AsyncTask<PageItemHolder, Void, Bitmap> {
        private Long mOldBookId = 0L;
        private Long mOldPageId = 0L;
        private WeakReference<PageItemHolder> v;

        @Override
        protected Bitmap doInBackground(PageItemHolder... params) {
            v = new WeakReference<AllPageViewAdapter.PageItemHolder>(params[0]);
            PageItemHolder holder = v.get();
            mOldBookId = (Long) holder.pageLayout.getTag(R.string.book_id);
            mOldPageId = (Long) holder.pageLayout.getTag(R.string.page_id);
            Bitmap b = null;
            b = NotePage.getThumbnail(mOldBookId, mOldPageId);
            if (b == null) {
                String selection = "owner = " + mOldBookId + " and created_date = " + mOldPageId;
                Cursor cursor = mContentResolver.query(MetaData.PageTable.uri, null, selection, null, null);
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    int color = cursor.getInt(MetaData.PageTable.INDEX_COLOR);
                    int line = cursor.getInt(MetaData.PageTable.INDEX_STYLE);
                    // BEGIN: archie_huang@asus.com
                    int pageSize = cursor.getInt(MetaData.PageTable.INDEX_PAGE_SIZE);
                    b = CoverHelper.getDefaultCoverBitmap(color, line,mContext.getResources());//Allen
                    // END: archie_huang@asus.com
                }
                cursor.close();
            } else {
            	// BEGIN: Better
                // add background to version 1 page thumbnail
            	Cursor cursor = mContext.getContentResolver().query(MetaData.PageTable.uri, 
    					new String[] {MetaData.PageTable.VERSION}, "created_date = ?", 
    					new String[] { Long.toString(mOldPageId) }, null);
            	cursor.moveToFirst();
    			int version = cursor.getInt(0);
    			cursor.close();
    			if ((b != null)
    					&& (mLoadingPageCover != null)
    					&& (version == 1)
    					&& !MetaData.SavingPageIdList.contains(mOldPageId)) {
    				int width = mLoadingPageCover.getWidth();
    				int height = mLoadingPageCover.getHeight();
    				Bitmap bmp = Bitmap.createBitmap(width, height,
    						Config.ARGB_8888);
    				Canvas canvas = new Canvas(bmp);
    				canvas.drawBitmap(mLoadingPageCover, 0, 0, new Paint());
    				Resources res = mContext.getResources();
    				float left = res.getDimension(R.dimen.thumb_padding_left);
    	            float top = res.getDimension(R.dimen.thumb_padding_top);
    	            canvas.translate(left, top);
    	            Paint paint = new Paint();
    	            paint.setAntiAlias(true);
    	            paint.setDither(true);
    	            paint.setFilterBitmap(true);
    				canvas.drawBitmap(b, 0, 0, paint);
    				b = bmp;
    			}
    			if (MetaData.SavingPageIdList.contains(mOldPageId)) {
    				Paint paint = new Paint();
    	            paint.setAntiAlias(true);
    	            paint.setDither(true);
    	            paint.setFilterBitmap(true);
    	            Bitmap bmp = Bitmap.createBitmap(b.getWidth(), b.getHeight(),
    						Config.ARGB_8888);
    				Canvas canvas = new Canvas(bmp);
    				canvas.drawBitmap(b, 0, 0, paint);
    				Resources res = mContext.getResources();
    				float left = res.getDimension(R.dimen.thumb_padding_left);
    	            float top = res.getDimension(R.dimen.thumb_padding_top);
    	            canvas.translate(left, top);
    				canvas.drawText("Saving", 0, 0, paint);
    				b = bmp;
    			}
    			// END: Better
            }
            return b;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (v.get() != null && result != null) {
                PageItemHolder holder = v.get();
                Long mNewBookId = (Long) holder.pageLayout.getTag(R.string.book_id);
                Long mNewPageId = (Long) holder.pageLayout.getTag(R.string.page_id);
                boolean usingTheResult = mNewBookId.equals(mOldBookId) && mNewPageId.equals(mOldPageId);
                if (usingTheResult) {
                    if (holder.pageCover.getDrawable() instanceof BitmapDrawable) {
                        BitmapDrawable bd = (BitmapDrawable) holder.pageCover.getDrawable();
                        if (bd != null) {
                            Bitmap b = bd.getBitmap();
                            if (b != null && (b.isRecycled() == false) && (b.sameAs(mLoadingPageCover) == false) && (b.sameAs(mLoadingPageCoverYellow) == false)) {
                                b.recycle();
                                b = null;
                            }
                        }
                    }
                    holder.pageCover.setImageBitmap(result);

                }
                else if (result != null && (result.isRecycled() == false)) {
                    result.recycle();
                    result = null;
                }
            }
        }
    }

}
