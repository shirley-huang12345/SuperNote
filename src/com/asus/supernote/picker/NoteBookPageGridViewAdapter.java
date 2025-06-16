package com.asus.supernote.picker;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnHoverListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;


import com.asus.supernote.EditorActivity;
import com.asus.supernote.IsHitInRegion;
import com.asus.supernote.R;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.ui.CoverHelper;
import com.asus.supernote.ui.CursorIconLibrary;

public class NoteBookPageGridViewAdapter extends BaseAdapter {
	public static final String TAG = "PageGridViewAdapter";
	private static final int MIN_ITEM_NUM = 0;
	private Context mContext;
	private BookCase mBookcase;
	private NoteBook mNotebook;
	private Date mPageDate;
	private java.text.DateFormat mDateFormat;//emmanual
	private View mDraggedView;
	public ArrayList<Long> mSelectedItems;
	private boolean mIsEditMode;
	private boolean mIsSelectAll;
	private String mSortOrder;
	private Bitmap mLoadingPageCover;
	private DataCounterListener mDataCounterListener;

	public static final int NO_SELECTED = -1;
	private Cursor mCursor = null;
	private Resources mResources;
	private Bitmap mPhoneIcon;
	private Bitmap mPadIcon;
	private Bitmap mSyncIcon; // shaun
	private Bitmap mSyncPadIcon; //smilefish
	private NoteBookPageGridFragment mNoteBookPageGridFragment;
	private NoteBookPickerActivity mNoteBookPickerActivity;
	public boolean bIsLock = false;

	//darwin
    static private boolean isNotebookPageGridProcessing = false;
    static private Object mLockObj = new Object();
    public boolean getIsNotebookPageGridProcessing()
    {
    	synchronized (mLockObj)
		{
    		if(isNotebookPageGridProcessing == true)
    		{
    			return true;
    		}
    		else
    		{
    			isNotebookPageGridProcessing = true;
    			return false;
    		}
		}
    }

    static public void resetNotebookPageGridProcessing()
    {
    	synchronized (mLockObj)
		{
    		isNotebookPageGridProcessing = false;
		}
    }
    //darwin
    
    public void setDateFormat(){
        mDateFormat = DateFormat.getDateFormat(mContext);//smilefish
    } 
	
	public NoteBookPageGridViewAdapter(Context context,
			NoteBookPageGridFragment nbpgf) {
		mContext = context;//.getApplicationContext();
		mBookcase = BookCase.getInstance(mContext);
		mNoteBookPageGridFragment = nbpgf;
		mNoteBookPickerActivity = (NoteBookPickerActivity) mNoteBookPageGridFragment.getActivity();
		boolean islocked = NoteBookPickerActivity.islocked();
		String selection = islocked ? "(is_locked = 0) AND ((userAccount = 0) OR (userAccount = ?))"
				: "((userAccount = 0) OR (userAccount = ?))";

		mCursor = mContext.getContentResolver().query(MetaData.BookTable.uri,
				null, selection,
				new String[] { Long.toString(MetaData.CurUserAccount) },
				"title");

		mResources = mContext.getResources();
		mPageDate = new Date();

		mDateFormat = android.text.format.DateFormat.getDateFormat(mContext);//emmanual
		mLatestEditView = View.inflate(mContext, R.layout.page_item, null);

		// //shaun
		mSyncIcon  = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_cover_phone);
		mSyncPadIcon = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_cover_pad); //smilefish
		mPhoneIcon = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_cover_phone);
		mPadIcon   = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_cover_pad);
		dataChange(mCursor.getCount());
	}

	public void requery() {
		mCursor.requery();
	}

	@Override
	public int getCount() {
		return mCursor.getCount()+MIN_ITEM_NUM;
	}

	@Override
	public Object getItem(int position) {
		if(position>=0)
		{
			mCursor.moveToPosition(position - MIN_ITEM_NUM);
			Long bookId = mCursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
			return mBookcase.getNoteBook(bookId);
		}
		else
		{
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void requeryDataUpdate(boolean bIsLock,String sortOrder) {
		mSortOrder = sortOrder;
		if (mCursor != null) {
			mCursor.close();
		}// "(is_locked = 0) AND ((userAccount = 0) OR (userAccount = ?))"

		if (bIsLock) {
			mCursor = mContext
					.getContentResolver()
					.query(MetaData.BookTable.uri,
							null,
							"(is_locked = 0) AND ((userAccount = 0) OR (userAccount = ?))",
							new String[] { Long
									.toString(MetaData.CurUserAccount) },
									mSortOrder);
		} else {
			mCursor = mContext.getContentResolver().query(
					MetaData.BookTable.uri, null,
					"(userAccount = 0) OR (userAccount = ?)",
					new String[] { Long.toString(MetaData.CurUserAccount) },
					mSortOrder);
		}
		if (mCursor != null) {
			notifyDataSetChanged();
			dataChange(mCursor.getCount());
		}

	}

	public Long mLatestEditPageId = 0L;
	public Long mLatestEditBookId = 0L;
	public String mLatestEditBookName = "";
	public Bitmap mLatestEditBookBitmap = null;
	public View mLatestEditView;

	public void getLastestEditInfo() {
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(MetaData.PageTable.uri, null, "(version = ?)",
				new String[] { "3" }, null);
		Long max = 0L;
		if (cursor != null) {
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false) {
				if (cursor.getLong(MetaData.PageTable.INDEX_MODIFIED_DATE) >= max) {
					max = cursor
							.getLong(MetaData.PageTable.INDEX_MODIFIED_DATE);
					mLatestEditPageId = cursor
							.getLong(MetaData.PageTable.INDEX_CREATED_DATE);
					mLatestEditBookId = cursor
							.getLong(MetaData.PageTable.INDEX_OWNER);
				}
				cursor.moveToNext();
			}
			cursor.close();
		}
		if (mLatestEditBookId != 0L) {
			if (mBookcase == null) {
				mBookcase = BookCase.getInstance(mContext);
			}
			NoteBook book = mBookcase.getNoteBook(mLatestEditBookId);
			if (book != null) {
				NotePage page = book.getNotePage(mLatestEditPageId);
				if (page != null) {
					mLatestEditBookName = book.getTitle();
					mLatestEditBookBitmap = NotePage.getThumbnail(
							mLatestEditBookId, mLatestEditPageId);
				}
			}
		}

		if (!(max != 0 && mLatestEditBookId != 0L && mLatestEditPageId != 0L)) {
			mLatestEditBookName = "";
			mLatestEditBookBitmap = null;
		} else {
			if (mBookcase == null) {
				mBookcase = BookCase.getInstance(mContext);
			}
			NoteBook book = mBookcase.getNoteBook(mLatestEditBookId);
			if (book != null) {
				mLatestEditBookName = book.getTitle();
				if (book.getIsLocked() && NoteBookPickerActivity.islocked()) {// wendy
					mLatestEditBookBitmap = BitmapFactory.decodeResource(
							mResources, R.drawable.asus_ep_book_lock);
				} else {
					NotePage page = book.getNotePage(mLatestEditPageId);
					if (page != null) {
						mLatestEditBookBitmap = NotePage.getThumbnail(
								mLatestEditBookId, mLatestEditPageId);
					}
				}
			}

		}
	}
	private Rect mVisiableRect=null;
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder holder;

			int [] l=new int[2];
			parent.getLocationOnScreen(l);
			mVisiableRect = new Rect(l[0], l[1], l[0]+parent.getMeasuredWidth(), l[1]+parent.getMeasuredHeight());

			mCursor.moveToPosition(position - MIN_ITEM_NUM);
			int id = mCursor.getColumnIndex(MetaData.BookTable.USER_ACCOUNT);
			Long useAccountid = 0L;
			if (id != -1) {
				useAccountid = mCursor.getLong(id);
			}
			if (convertView == null || convertView.getTag() == null) {
				view = View.inflate(mContext, R.layout.notebook_item, null);
				holder = new ViewHolder();
				holder.pageCover = (ImageView) view
						.findViewById(R.id.page_cover);
				holder.pageCoverMask = (ImageView) view
						.findViewById(R.id.page_cover_mask);
				holder.pageCheck = (ImageView) view
						.findViewById(R.id.page_check);
				holder.pageDate = (TextView) view.findViewById(R.id.page_date);
				holder.pagePadOrPhone = (ImageView) view.findViewById(R.id.book_pad_or_phone);
				holder.pageCount = (TextView) view.findViewById(R.id.page_count);
				holder.notebookCover = (ImageView) view.findViewById(R.id.notebook_cover);
				holder.notebookPreview = (ImageView) view.findViewById(R.id.page_preview);
				holder.pagePrivate = (ImageView) view.findViewById(R.id.page_private);
				holder.pageCloud = (ImageView) view.findViewById(R.id.page_cloud); //smilefish
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}
			CursorIconLibrary.setStylusIcon(view, CursorIconLibrary.STYLUS_ICON_FOCUS);//by jason
			Long bookId = mCursor
					.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
			NoteBook notebook = mBookcase.getNoteBook(bookId);
			if (notebook != null) {
				if (mIsEditMode) {
					holder.pageCoverMask.setVisibility(View.GONE); //smilefish
					if(isPageSelected(bookId))
						holder.pageCheck.setVisibility(View.VISIBLE);
					else
						holder.pageCheck.setVisibility(View.GONE);
					holder.pageCheck.setOnClickListener(pageCheckedListener); //smilefish
					
					//emmanual to fix bug 439459,440262, disable dragging
					view.setOnClickListener(selectedPageListener);
				} else {
					holder.pageCoverMask.setVisibility(View.VISIBLE); //smilefish
					holder.pageCheck.setVisibility(View.GONE);
					holder.pageCheck.setOnClickListener(null);
					view.setOnLongClickListener(null);
					view.setOnDragListener(null);
				}
				holder.pageCoverMask.setTag(view);
				holder.pageCoverMask.setOnClickListener(mIsEditMode ? selectedPageMaskListener : maskClickListener);
				holder.pageCoverMask
						.setOnLongClickListener(maskLongClickListener);
				//Begin Siupo
				boolean shareMode = mNoteBookPickerActivity.isShareMode();//noah
				if(MethodUtils.isEnableAirViewContentPreview(mContext) && !shareMode)//true,open the function；
				{
					holder.pageCoverMask.setOnHoverListener(new OnHoverListener(){
						@Override
						public boolean onHover(View arg0, MotionEvent arg1) {
							// TODO Auto-generated method stub
							if (!MethodUtils.isEnableAirViewContentPreview(mContext)) {
								return false;
							}
							final int action = arg1.getActionMasked();
							
							if(action ==MotionEvent.ACTION_HOVER_ENTER)
							{
								CursorIconLibrary.setStylusIcon(arg0, CursorIconLibrary.STYLUS_ICON_FOCUS);//by jason
								View view = (View) arg0.getTag();
								ViewHolder holder = (ViewHolder) view.getTag();
								int point[] = new int[2];
								view.getLocationOnScreen(point);
								int contentWidth=view.getWidth();
								int contentHeight=view.getHeight();
								Long bookID = (Long) holder.pageCheck.getTag();
								mBookAirViewDisplayCountDown.init(bookID, point[0], point[1],contentWidth,contentHeight, 0);
								mBookAirViewDisplayCountDown.start();
								
							}
							else if(action == MotionEvent.ACTION_HOVER_MOVE)
							{
								
							}
							else if(action == MotionEvent.ACTION_HOVER_EXIT)
							{
								//
								mBookAirViewDisplayCountDown.cancel();
								if (mIsViewShow&&IsHitInRegion.isHitIn(mAirView, (int)arg1.getRawX(), (int)arg1.getRawY())) {
									return true;
								}
								dismissView();
							}	
							return true;
						}
		    		});
				}
				holder.pageCover.setTag(Integer.valueOf(position - MIN_ITEM_NUM));

				holder.pageCover.setScaleType(ScaleType.FIT_XY);//CENTER_CROP
				
				// BEGIN: Shane_Wang 2012-10-8
	            Cursor modCursor = mContext.getContentResolver().query(MetaData.BookTable.uri, 
						new String[] {MetaData.BookTable.MODIFIED_DATE}, "created_date = ?",
						new String[] { Long.toString(bookId) }, null);
	            if ((modCursor != null) && (modCursor.getCount() > 0)) { // Better
					modCursor.moveToFirst();
					long modTime = modCursor.getLong(0);
					modCursor.close();
		            mPageDate.setTime(modTime);
	            }
	            //BEGIN: RICHARD
	            else if(modCursor != null)
	            {
	            	modCursor.close();
	            }
	            //END: RICHARD
                // END: Shane_Wang 2012-10-8
				holder.pageDate.setTag("yu");
				holder.pageDate.setText(mDateFormat.format(mPageDate));// mDateFormat.format(mPageDate)
				holder.pageCheck.setTag(bookId);
				holder.pageCount.setText( notebook.getTitle() );
				if(notebook.getCoverIndex() == 0)
				{
					Bitmap bitmap = NotePage.getNoteBookCoverThumbnail( bookId);
					if (useAccountid > 0) { //smilefish
						holder.pageCover.setImageBitmap(NotePage.getCloudCover(notebook, mContext));
					}
					else
					{
						holder.pageCover.setImageBitmap(NotePage.getNoCloudCover(notebook, mContext));
					}
					if(bitmap == null)
					{
						int type = notebook.getTemplate();
			    		if( type == 1)
			    		{
			    			holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_template_meeting));
			    		}
			    		else if( type == 2)
			    		{
			    			holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_template_diary));
			    		}
			    		else if( type == 3)
			    		{
			    			holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_template_memo));
			    		}
					}
					holder.notebookPreview.setImageBitmap(bitmap);
					holder.notebookCover.setImageBitmap(null);
				}
				else if(notebook.getCoverIndex() == 1)
				{
					holder.notebookCover.setImageBitmap(NotePage.getNoteBookDefaultCoverThumbnail( bookId));
					//begin smilefish
					holder.pageCover.setImageBitmap(NoteBook.getNoteBookCover(notebook, mContext));					
					//end smilefish
					holder.notebookPreview.setImageBitmap(null);
				}
				else
				{
					//begin smilefish
					holder.notebookCover.setImageBitmap(NoteBook.getNoteBookCover(notebook, mContext));					
					//end smilefish
					holder.pageCover.setImageBitmap(null);
					holder.notebookPreview.setImageBitmap(null);
				}
				
				float alpha = 1.0f;
				if(notebook.getIsLocked())
				{
					holder.pagePrivate.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.pagePrivate.setVisibility(View.GONE);
				}
				
				//begin smilefish
					if (useAccountid > 0) 
					{
						holder.pageCloud.setVisibility(View.VISIBLE);//smilefish
						holder.pagePadOrPhone.setImageBitmap((notebook.getPageSize() == MetaData.PAGE_SIZE_PHONE) ? mSyncIcon : mSyncPadIcon);
					}
					else{
						holder.pageCloud.setVisibility(View.GONE);//smilefish
						holder.pagePadOrPhone.setImageBitmap((notebook.getPageSize() == MetaData.PAGE_SIZE_PHONE) ? mPhoneIcon : mPadIcon);
					}
				//end smilefish
				
				view.findViewById(R.id.page_cover).setAlpha(alpha);
				view.findViewById(R.id.book_pad_or_phone).setAlpha(alpha);
				view.findViewById(R.id.page_count).setAlpha(alpha);
				view.findViewById(R.id.page_date).setAlpha(alpha);
			}
		return view;
	}

	private static class ViewHolder {
		ImageView pageCheck;//smilefish
		ImageView pageCover;
		ImageView pageCoverMask;
		TextView pageDate;
		ImageView pagePadOrPhone;
		TextView pageCount;
		ImageView pagePrivate;
		ImageView notebookCover;
		ImageView notebookPreview;
		ImageView pageCloud; //smilefish
		
	}
	
	public void requeryData() {
		if (mCursor != null) {
			mCursor.close();
		}// "(is_locked = 0) AND ((userAccount = 0) OR (userAccount = ?))"
		mCursor = mContext.getContentResolver().query(MetaData.BookTable.uri,
				null,
				"(is_locked = 0) AND ((userAccount = 0) OR (userAccount = ?))",
				new String[] { Long.toString(MetaData.CurUserAccount) },
				"title");
		if (mCursor != null) {
			notifyDataSetChanged();
			dataChange(mCursor.getCount());
		}

	}

	public void closeCursor() {
		if (mCursor != null) {
			mCursor.close();
		}
	}

	public void registerDataCounterListener(DataCounterListener listener) {
		if (mDataCounterListener != null) {
			removeDataCounterListener();
		}
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

    final BookAirViewCountDown mBookAirViewDisplayCountDown = new BookAirViewCountDown(500,1000);
    private WindowManager wm=null;  
    private WindowManager.LayoutParams wmParams=null;  
    private boolean mIsViewShow = false;
    private AirViewPolicy mAirViewPolicy = new AirViewPolicy();
    View mAirView = null ;
    View mBookAirView = null;//receive the Hover Event;
    ImageView mIv1 = null;
    ImageView mIv2 = null;
    ImageView mIv3 = null;
    ImageView mIv4 = null;
    ImageView arrowLeftView = null;
    ImageView arrowRightView = null;
    NoteBook mCurrentNotebookForHover = null;
    private int mNum = 0;//the numbers of the book; 
	int mPageIndexFor4 = 0;//An airView puts four pages;
	
	private void setImageViewOne(int mCount,NoteBook notebook)
	{
		if(mNum>mCount)
		{
			Bitmap pageBitmap = null;
	    	if(mNum > mCount)
	    	{
	    		Long pageId = notebook.getPageOrder(mCount);
	    		pageBitmap = NotePage.getAirView(notebook.getCreatedTime(), pageId);
	    		if(pageBitmap == null)
	    		{
	    			pageBitmap=CoverHelper.getDefaultCoverBitmap(notebook.getBookColor(), notebook.getGridType(), mContext.getResources());
	    		}
	    		mIv1.setImageBitmap(pageBitmap);
	    	}
	    	else
	    	{
	    		mIv1.setImageResource(R.drawable.asus_airview_page_default);
	    	}
		}
	}
	private void setImageViewFour(int mCount,NoteBook notebook)
	{
		Bitmap pageBitmap = null;
    	if(mNum > mCount)
    	{
    		Long pageId = notebook.getPageOrder(mCount);
    		pageBitmap = NotePage.getAirView(notebook.getCreatedTime(), pageId);
    		if(pageBitmap == null)
    		{
    			pageBitmap=CoverHelper.getDefaultCoverBitmap(notebook.getBookColor(), notebook.getGridType(), mContext.getResources());
    		}
    		mIv1.setImageBitmap(pageBitmap);
    	}
    	else
    	{
    		mIv1.setImageResource(R.drawable.asus_airview_page_default);
    	}
    	mCount++;
    	if(mNum > mCount)
    	{
    		Long pageId = notebook.getPageOrder(mCount);
    		pageBitmap = NotePage.getAirView(notebook.getCreatedTime(), pageId);
    		if(pageBitmap == null)
    		{
    			pageBitmap=CoverHelper.getDefaultCoverBitmap(notebook.getBookColor(), notebook.getGridType(), mContext.getResources());
    		}
    		mIv2.setImageBitmap(pageBitmap);
    	}
    	else
    	{
    		mIv2.setImageResource(R.drawable.asus_airview_page_default);
    	}
    	mCount++;
    	if(mNum > mCount)
    	{
    		Long pageId = notebook.getPageOrder(mCount);
    		pageBitmap = NotePage.getAirView(notebook.getCreatedTime(), pageId);
    		if(pageBitmap == null)
    		{
    			pageBitmap=CoverHelper.getDefaultCoverBitmap(notebook.getBookColor(), notebook.getGridType(), mContext.getResources());
    		}
    		mIv3.setImageBitmap(pageBitmap);
    	}
    	else
    	{
    		mIv3.setImageResource(R.drawable.asus_airview_page_default);
    	}
    	mCount++;
    	if(mNum > mCount)
    	{
    		Long pageId = notebook.getPageOrder(mCount);
    		pageBitmap = NotePage.getAirView(notebook.getCreatedTime(), pageId);
    		if(pageBitmap == null)
    		{
    			pageBitmap=CoverHelper.getDefaultCoverBitmap(notebook.getBookColor(), notebook.getGridType(), mContext.getResources());
    		}
    		mIv4.setImageBitmap(pageBitmap);
    	}
    	else
    	{
    		mIv4.setImageResource(R.drawable.asus_airview_page_default);
    	}
	}
    private void setImageView(NoteBook notebook,int pageIndexFor4,BookAirViewCountDown pageCountDown,int type)
    {
    	int mCount = pageIndexFor4 * 4;
    	if(mNum<=1)
    	{
    		//
    		setImageViewOne(mCount,notebook);
    	}
    	else
    	{
    		setImageViewFour(mCount,notebook);
    		arrowLeftView.setVisibility(View.VISIBLE);
    		arrowRightView.setVisibility(View.VISIBLE);
    		if(mCount+4>=mNum)
    		{
    			arrowRightView.setVisibility(View.INVISIBLE);
    		}
    		if(mCount==0)
    		{
    			arrowLeftView.setVisibility(View.INVISIBLE);
    		}
    	}
    }
    
   private void openHoverPage(int imageViewIndex)
    {
    	if(mNum >= mPageIndexFor4*4 + imageViewIndex)
		{
    		dismissView();
			Intent pageIntent = new Intent();
			pageIntent.setClass(mContext, EditorActivity.class);
			long bookID = mCurrentNotebookForHover.getCreatedTime();
			long pageID = mCurrentNotebookForHover.getPageOrder(mPageIndexFor4*4-1+imageViewIndex);
			pageIntent.putExtra(MetaData.BOOK_ID, bookID);
			pageIntent.putExtra(MetaData.PAGE_ID, pageID);
			pageIntent.putExtra(MetaData.IS_NEW_PAGE, false);
			pageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
			mContext.startActivity(pageIntent);
		}
    }
    private void dismissView()
    {
    	if(mIsViewShow&&mAirView != null && wm != null && mAirView.getParent()!=null)
		{
			wm.removeView(mAirView);
			mIsViewShow = false;
		}
    }
    private void createView(Long bookId,int pX,int pY, int viewWidth, int viewHeight){  
    	if(mIsViewShow )
    	{
    		return;
    	}
    	final BookAirViewCountDown mBookAirViewDismissCountDown = new BookAirViewCountDown(300,1000);
    	mCurrentNotebookForHover = BookCase.getInstance(mContext).getNoteBook(bookId);
    	mNum = mCurrentNotebookForHover.getTotalPageNumber();
    	mIsViewShow = true;
    	int airView_Bookview_Width = 0;
    	int airView_Bookview_Height = 0;
    	if(mNum<=1)
    	{
    		mAirView = View.inflate(mContext, R.layout.airview_bookview_one, null);
    		airView_Bookview_Width = (int)(mContext.getResources().getDimension(R.dimen.AirView_BookView_One_Width));
    		airView_Bookview_Height= (int)(mContext.getResources().getDimension(R.dimen.AirView_BookView_One_Height));
    	}
    	else
    	{
    		mAirView = View.inflate(mContext, R.layout.airview_bookview_four, null);
    		airView_Bookview_Width = (int)(mContext.getResources().getDimension(R.dimen.AirView_BookView_Four_Width));
    		airView_Bookview_Height= (int)(mContext.getResources().getDimension(R.dimen.AirView_BookView_Four_Height));
    	}
    	mBookAirView = (ImageView) mAirView.findViewById(R.id.airview);
    	
    	View layout = mAirView.findViewById(R.id.airview_layout);
    	int color = mCurrentNotebookForHover.getBookColor();
    	if(color == MetaData.BOOK_COLOR_WHITE){
    		layout.setBackgroundResource(R.drawable.asus_airview_photo_board_light_bg);
    	}else{  //smilefish fix bug 597959
    		layout.setBackgroundResource(R.drawable.asus_airview_photo_board_light_bg_yellow);
    	}
    	
    	mIv1 = (ImageView) mAirView.findViewById(R.id.air1); 
    	CursorIconLibrary.setStylusIcon(mIv1, CursorIconLibrary.STYLUS_ICON_FOCUS);//by jason
    	mIv2 = (ImageView) mAirView.findViewById(R.id.air2);
    	CursorIconLibrary.setStylusIcon(mIv2, CursorIconLibrary.STYLUS_ICON_FOCUS);//by jason
    	mIv3 = (ImageView) mAirView.findViewById(R.id.air3);
    	CursorIconLibrary.setStylusIcon(mIv3, CursorIconLibrary.STYLUS_ICON_FOCUS);//by jason
    	mIv4 = (ImageView) mAirView.findViewById(R.id.air4);
    	CursorIconLibrary.setStylusIcon(mIv4, CursorIconLibrary.STYLUS_ICON_FOCUS);//by jason
    	arrowLeftView = (ImageView) mAirView.findViewById(R.id.arrowleft);
    	arrowRightView = (ImageView) mAirView.findViewById(R.id.arrowright);
    	CursorIconLibrary.setStylusIcon(arrowLeftView, CursorIconLibrary.STYLUS_ICON_FOCUS);//by jason
    	CursorIconLibrary.setStylusIcon(arrowRightView, CursorIconLibrary.STYLUS_ICON_FOCUS);//by jason
    	mPageIndexFor4 = 0;
    	setImageView(mCurrentNotebookForHover,mPageIndexFor4,null,1);
        wm=(WindowManager)mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);  
 
        wmParams = new WindowManager.LayoutParams();
        wmParams.type=LayoutParams.TYPE_SYSTEM_ALERT;   
        wmParams.format=PixelFormat.RGBA_8888;
        wmParams.flags|=LayoutParams.FLAG_NOT_FOCUSABLE|LayoutParams.FLAG_NOT_TOUCH_MODAL ;
        wmParams.gravity=Gravity.START|Gravity.TOP; 
        
        
        wmParams.width=airView_Bookview_Width;
        wmParams.height=airView_Bookview_Height;
        
        int[] ll=mAirViewPolicy.getGalleryAirViewPoistion().getAirViewPoistion(mVisiableRect, new Rect(pX, pY, pX+viewWidth, pY+viewHeight), airView_Bookview_Width, airView_Bookview_Height);
        wmParams.x=ll[0];  
        wmParams.y=ll[1];  
        wm.addView(mAirView, wmParams); 
        
        mBookAirView.setOnHoverListener(new OnHoverListener(){
			@Override
			public boolean onHover(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				final int action = arg1.getAction();
				if(action == MotionEvent.ACTION_HOVER_ENTER)
				{
					mBookAirViewDismissCountDown.cancel();
				}
				else if(action== MotionEvent.ACTION_HOVER_MOVE)
				{
					mBookAirViewDismissCountDown.cancel();
				}
				else if(action ==MotionEvent.ACTION_OUTSIDE ||
						action == MotionEvent.ACTION_HOVER_EXIT)
				{
					//begin jason
					if (arrowLeftView!=null) {
						if (IsHitInRegion.isHitIn(arrowLeftView, (int)arg1.getRawX(), (int)arg1.getRawY())) {
							mBookAirViewDismissCountDown.cancel();
							return false;
						}
					}
					if (arrowRightView!=null) {
						if (IsHitInRegion.isHitIn(arrowRightView, (int)arg1.getRawX(), (int)arg1.getRawY())) {
							mBookAirViewDismissCountDown.cancel();
							return false;
						}
					}
					//begin jason
					mBookAirViewDismissCountDown.init(0L, 0, 0, 0,0, 3);//dimissView
					mBookAirViewDismissCountDown.start();
				}
				return false;
			}
		});
        mBookAirView.setOnTouchListener(new OnTouchListener()
        {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1)
			{
				// TODO Auto-generated method stub
				if (mNum == 1) {
					openHoverPage(1);
					return false;
				}
				
				int x = (int)arg1.getX();
				int y = (int)arg1.getY();
				int vCenterX = arg0.getWidth()/2;
				int vCenterY = arg0.getHeight()/2;
				mBookAirViewDisplayCountDown.cancel();
				if(x<=vCenterX && y<=vCenterY)
				{
					openHoverPage(1);
				}
				else if(x<=vCenterX && y>vCenterY)
				{
					openHoverPage(3);
				}
				else if(x>vCenterX && y<=vCenterY)
				{
					openHoverPage(2);
				}
				else if(x>vCenterX && y>vCenterY)
				{
					openHoverPage(4);
				}
				else
				{
					//
				}
				Log.v("Siupo", "mBookAirView");
				return false;
			}
        	
        });
        if(mNum > 4)
        {
	        arrowLeftView.setOnClickListener(new OnClickListener()
	        {
	
				@Override
				public void onClick(View arg0)
				{
					// TODO Auto-generated method stub
					Log.v("Siupo", "arrowLeftView");
					mBookAirViewDismissCountDown.cancel();
					if(mPageIndexFor4>0)
					{
						mPageIndexFor4--;
						setImageView(mCurrentNotebookForHover,mPageIndexFor4,null,1);
					}
				}	
	        });
	        arrowRightView.setOnClickListener(new OnClickListener()
	        {
	
				@Override
				public void onClick(View arg0)
				{
					// TODO Auto-generated method stub
					Log.v("Siupo", "arrowRightView");
					mBookAirViewDismissCountDown.cancel();
					if(mNum > ((mPageIndexFor4 + 1)*4))
					{
						mPageIndexFor4++;
						setImageView(mCurrentNotebookForHover,mPageIndexFor4,null,2);
					}
				}	
	        });
			arrowLeftView.setOnHoverListener(new OnHoverListener(){
				@Override
				public boolean onHover(View arg0, MotionEvent arg1) {
					// TODO Auto-generated method stub
					final int action = arg1.getActionMasked();
					boolean canscroll = true;
					
					if(action ==MotionEvent.ACTION_HOVER_ENTER)
					{
						mBookAirViewDismissCountDown.cancel();
						arrowLeftView.setImageResource(R.drawable.asus_airview_scroll_lft);

						//emmanual to fix bug 570212
						if (mPageIndexFor4 > 0 && canscroll) {
							canscroll = false;
							mPageIndexFor4--;
							setImageView(mCurrentNotebookForHover,
							        mPageIndexFor4, null, 1);
						}
					}
					else if(action ==MotionEvent.ACTION_OUTSIDE ||
							action == MotionEvent.ACTION_HOVER_EXIT)
					{
						Log.i("AirView", "LeftView receive Exit event,Action = " + action);
						arrowLeftView.setImageResource(R.drawable.asus_airview_scroll_lft);
						arrowRightView.setImageResource(R.drawable.asus_airview_scroll_rit);
						mBookAirViewDismissCountDown.init(0L, 0, 0, 0,0, 3);//dimissView
						mBookAirViewDismissCountDown.start();
						canscroll = true;
					}
					return false;
				}
			});
	        
	        arrowRightView.setOnHoverListener(new OnHoverListener(){
				@Override
				public boolean onHover(View arg0, MotionEvent arg1) {
					// TODO Auto-generated method stub
					final int action = arg1.getActionMasked();
					boolean canscroll = true;
					
					if(action ==MotionEvent.ACTION_HOVER_ENTER)
					{
						mBookAirViewDismissCountDown.cancel();
						arrowRightView.setImageResource(R.drawable.asus_airview_scroll_rit);

						//emmanual to fix bug 570212
						if (mNum > ((mPageIndexFor4 + 1) * 4) && canscroll) {
							canscroll = false;
							mPageIndexFor4++;
							setImageView(mCurrentNotebookForHover, mPageIndexFor4, null, 2);
						}
					}
					else if(action ==MotionEvent.ACTION_OUTSIDE ||
							action == MotionEvent.ACTION_HOVER_EXIT)
					{
						Log.i("AirView", "RightView receive Exit event");
						arrowLeftView.setImageResource(R.drawable.asus_airview_scroll_lft);
						arrowRightView.setImageResource(R.drawable.asus_airview_scroll_rit);
						mBookAirViewDismissCountDown.init(0L, 0, 0, 0,0, 3);//dimissView
						mBookAirViewDismissCountDown.start();
						canscroll = true;
					}
	
					return false;
				}
						
			});
        }
        else
        {
        	if(mNum>2)
        	{
        		arrowLeftView.setVisibility(View.GONE);
        		arrowRightView.setVisibility(View.GONE);
        	}
        }
    }  
    private final View.OnClickListener maskClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mBookAirViewDisplayCountDown.cancel();
			View view = (View) v.getTag();
			//darwin
        	if(getIsNotebookPageGridProcessing())
        	{
        		return;
        	}
        	//begin noah;for share
        	boolean shareMode = mNoteBookPickerActivity.isShareMode();
        	if(shareMode){
        		ViewHolder holder = (ViewHolder) view.getTag();
        		long bookId = (Long) holder.pageCheck.getTag();
        		clickBookInShareMode(bookId);
        		return;
        	}
        	//end noah;for share
            //darwin
			try {
				Intent intent = new Intent();
				intent.setClass(mContext, PickerActivity.class);
				ViewHolder holder = (ViewHolder) view.getTag();
				long bookId = (Long) holder.pageCheck.getTag();
				intent.putExtra(MetaData.BOOK_ID, bookId);
				// intent.putExtra(MetaData.PAGE_ID, mLatestEditPageId);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			NoteBookPageGridFragment.mSearchString = "";//RICHARD
		}
	};
	
	/**
	 * share模式下，用户点击一本book
	 * @author noah_zhang
	 * 
	 */
	private void clickBookInShareMode(long bookId){
		NoteBook noteBook = mBookcase.getNoteBook(bookId);
		if(noteBook == null || mNoteBookPickerActivity == null)
			return;
		long lastPageId = 0;
		NotePage notePage = mNoteBookPickerActivity.createNewPageAndLoad(noteBook);
		lastPageId = notePage.getCreatedTime();
		if(lastPageId > 0){
			NoteBookPageGridFragment.mSearchString = "";
			Intent intent = new Intent();
			intent.setClass(mContext, EditorActivity.class);
			intent.putExtra(MetaData.BOOK_ID, bookId);
			intent.putExtra(MetaData.PAGE_ID, lastPageId);
			intent.putExtra(MetaData.IS_NEW_PAGE, false);
			NoteBookPickerActivity activity = (NoteBookPickerActivity)mContext;
			intent.putExtra(Intent.EXTRA_INTENT, activity.getShareIntent());
			mContext.startActivity(intent);
			activity.finish();
		}
	}

	private final View.OnLongClickListener maskLongClickListener = new View.OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			return false;
		}
	};

	public View getDraggedView() {
		return mDraggedView;
	}

	public boolean isEditMode() {
		return mIsEditMode;
	}

	//darwin
	public void setIsEditMode(boolean b) {
		mIsEditMode = b;
	}
	//darwin
	public void setEditMode(boolean b) {
		mIsEditMode = b;
		mSelectedItems = (mIsEditMode) ? (new ArrayList<Long>()) : null;
	}

	public boolean isSelectAll() {
		return mIsSelectAll;
	}

	public ArrayList<Long> getSelectList() {
		return mSelectedItems;
	}
	
	//darwin
	public void setSelectList(ArrayList<Long> list) {
		mSelectedItems = list;
	}
	public void clearSelectList()
	{
		mSelectedItems.clear();
	}
	//darwin
	

	public void setSelectAll(boolean b) {
		if (mSelectedItems == null) {
			mSelectedItems = new ArrayList<Long>();
		}
		mIsSelectAll = (mSelectedItems.size() == mCursor.getCount()) ? false
				: b;
		if (mIsSelectAll) {
			mSelectedItems.clear(); //smilefish	        
			if(!mCursor.isClosed()){//emmanual to fix bug 465369
				mCursor.moveToFirst();
				while (!mCursor.isAfterLast()) {
					Long bookId = mCursor
							.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
					mSelectedItems.add(bookId);
					mCursor.moveToNext();
				}
			}
		} else {
			mSelectedItems.clear();
		}
		selectedDataChange(mSelectedItems.size());

		notifyDataSetChanged();
	}

	public void changeSortOrder(Long id, String sortOrder) {
		mSortOrder = sortOrder;
		if (mCursor != null) {
			mCursor.close();
		}
		boolean islocked = NoteBookPickerActivity.islocked();
		if (islocked) {
			mCursor = mContext
					.getContentResolver()
					.query(MetaData.BookTable.uri,
							null,
							"(is_locked = 0) AND ((userAccount = 0) OR (userAccount = ?))",
							new String[] { Long
									.toString(MetaData.CurUserAccount) },
							mSortOrder);
		} else {
			mCursor = mContext.getContentResolver().query(
					MetaData.BookTable.uri, null,
					"(userAccount = 0) OR (userAccount = ?)",
					new String[] { Long.toString(MetaData.CurUserAccount) },
					mSortOrder);
		}

		if (mCursor == null) {
			return;
		}
		notifyDataSetChanged();
		dataChange(getCount());
	}

	private boolean isPageSelected(Long bookId) {
		if (mSelectedItems == null) {
			return false;
		}
		for (Long info : mSelectedItems) {
			if (info.equals(bookId)) {
				return true;
			}
		}
		return false;
	}

	//begin smilefish
	private void updatePageCheckedStatus(Long bookId)
	{
		int bookIndex = mBookcase.getNoteBookIndex(bookId);
		new SimplePageInfo(bookId, 0L, 0, bookIndex);
		if (isPageSelected(bookId) == false) {
			if(mSelectedItems == null) return; //edit by smilefish: crash reason not found
			mSelectedItems.add(bookId);
		} else {
			synchronized (mSelectedItems) {
				Iterator<Long> iter = mSelectedItems.iterator();
				while (iter.hasNext()) {
					Long target = iter.next();
					if (target.equals(bookId)) {
						iter.remove();
						break;
					}
				}
			}

		}

		selectedDataChange(mSelectedItems.size());

		notifyDataSetChanged();
	}
	
	private View.OnClickListener pageCheckedListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Long bookId = (Long)v.getTag();
			updatePageCheckedStatus(bookId);
		}
	
	};
	//end smilefish
	
	private View.OnClickListener selectedPageListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Long bookId = 0L;

			ViewHolder holder = (ViewHolder) v.getTag();
			bookId = (Long) holder.pageCheck.getTag();

			updatePageCheckedStatus(bookId);
		}
	};
	
	private View.OnClickListener selectedPageMaskListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			//add by mars fix monkey bug because sometimes mSelectedItems == null,trace TBD
			if(mSelectedItems == null){
				return ;
			}
			Long bookId = 0L;
			View view = (View) v.getTag();
			ViewHolder holder = (ViewHolder) view.getTag();
			bookId = (Long) holder.pageCheck.getTag();

			updatePageCheckedStatus(bookId);
		}
	};

	private void selectedDataChange(int count) {
		if (mDataCounterListener != null) {
			mDataCounterListener.onSelectedDataChange(count);
		}
	}

	// BEGIN: Better
	private Bitmap getCover() {
		Bitmap cover = null;
		if (mNotebook != null) {
			int line = mNotebook.getGridType();
			int color = mNotebook.getBookColor();
			mNotebook.getPageSize();
			cover = CoverHelper.getDefaultCoverBitmap(color, line, mContext.getResources());//Allen
		}
		return cover;
	}

	// END: Better

	public class GetBitmapTask extends AsyncTask<ViewHolder, Void, Bitmap> {
		private Long mOldId = 0L;
		private ViewHolder mOldHolder;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Bitmap doInBackground(ViewHolder... params) {
			mOldHolder = params[0];
			synchronized (mOldHolder) {
				mOldId = (Long) mOldHolder.pageCheck.getTag();
			}

			// BEGIN: Better
			// add background to version 1 page thumbnail
			Bitmap result = NotePage.getThumbnail(mNotebook.getCreatedTime(),
					mOldId);
			Cursor cursor = mContext.getContentResolver().query(
					MetaData.PageTable.uri,
					new String[] { MetaData.PageTable.VERSION },
					"created_date = ?", new String[] { Long.toString(mOldId) },
					null);
			cursor.moveToFirst();
			int version = cursor.getInt(0);
			cursor.close();
			if ((result != null) && (version == 1)
					&& !(MetaData.SavingPageIdList.contains(mOldId))) {
				Bitmap cover = getCover();
				int width = cover.getWidth();
				int height = cover.getHeight();
				Bitmap bmp = Bitmap.createBitmap(width, height,
						Config.ARGB_8888);
				Canvas canvas = new Canvas(bmp);
				canvas.drawBitmap(cover, 0, 0, new Paint());
				Resources res = mContext.getResources();
				float left = res.getDimension(R.dimen.thumb_padding_left);
				float top = res.getDimension(R.dimen.thumb_padding_top);
				canvas.translate(left, top);
				Paint paint = new Paint();
				paint.setAntiAlias(true);
				paint.setDither(true);
				paint.setFilterBitmap(true);
				canvas.drawBitmap(result, 0, 0, paint);
				result = bmp;
			}
			if (MetaData.SavingPageIdList.contains(mOldId)) {
				Paint paint = new Paint();
				paint.setAntiAlias(true);
				paint.setDither(true);
				paint.setFilterBitmap(true);
				Bitmap bmp = Bitmap.createBitmap(result.getWidth(),
						result.getHeight(), Config.ARGB_8888);
				Canvas canvas = new Canvas(bmp);
				canvas.drawBitmap(result, 0, 0, paint);
				Resources res = mContext.getResources();
				float left = res.getDimension(R.dimen.thumb_padding_left);
				float top = res.getDimension(R.dimen.thumb_padding_top);
				canvas.translate(left, top);
				canvas.drawText("Saving", 0, 0, paint);
				result = bmp;
			}
			return result;
			// END: Better
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (mOldHolder != null && result != null) {
				Long newId = (Long) mOldHolder.pageCheck.getTag();
				if (newId.equals(mOldId)) {
					if (mOldHolder.pageCover.getDrawable() instanceof BitmapDrawable) {
						BitmapDrawable bd = (BitmapDrawable) mOldHolder.pageCover
								.getDrawable();
						if (bd != null) {
							Bitmap b = bd.getBitmap();
							if (b != null && b.isRecycled() == false
									&& b.sameAs(mLoadingPageCover) == false) {
								b.recycle();
								b = null;
							}
						}
					}
					mOldHolder.pageCover.setImageBitmap(result);
				} else if (result != null && (result.isRecycled() == false)) {
					result.recycle();
					result = null;
				}
			}
		}
	}
	
	public class BookAirViewCountDown extends CountDownTimer
	{
		public int pX,pY;
		public Long bookID;
		public int  type;
		public int mViewWidth=0;
		public int mViewHeight=0;
		public BookAirViewCountDown(long millisInFuture, long countDownInterval)
		{
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
		}
		public void init(Long bookID,int pX,int pY,int viewWidth,int viewHeight, int type)
		{
			this.pX = pX;
			this.pY = pY;
			this.bookID = bookID;
			this.type = type;
			mViewWidth = viewWidth;
			mViewHeight = viewHeight;
		}
		@Override
		public void onFinish()
		{
			// TODO Auto-generated method stub
			switch(type)
			{
			case 0://Display View
				createView(bookID,pX,pY, mViewWidth, mViewHeight);
				break;
			case 1:
				if(mPageIndexFor4>0)
				{
					mPageIndexFor4--;
					setImageView(mCurrentNotebookForHover,mPageIndexFor4,this,type);
				}
				break;
			case 2:
				if( mNum > ((mPageIndexFor4 + 1)*4))
				{
					mPageIndexFor4++;
					setImageView(mCurrentNotebookForHover,mPageIndexFor4,this,type);
				}
				break;
			case 3://Dismiss View
				dismissView();
				break;
			default:
				break;
			}
		}

		@Override
		public void onTick(long arg0)
		{
			// TODO Auto-generated method stub
			
		}
		
	}
}
