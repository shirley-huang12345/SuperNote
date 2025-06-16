package com.asus.supernote.picker;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.IsHitInRegion;
import com.asus.supernote.R;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.ui.CoverHelper;
import com.asus.supernote.ui.CursorIconLibrary;

public class PageGridViewAdapter extends BaseAdapter {
    public static final String TAG = "PageGridViewAdapter";
    private static final int MIN_ITEM_NUM = 1;
    private Context mContext;
    private BookCase mBookcase;
    private NoteBook mNotebook;
    private Cursor mPageCursor = null;
    private Date mPageDate;
    private java.text.DateFormat mTimeFormat;
    private java.text.DateFormat mDateFormat;
    private View mDraggedView;
    private ArrayList<SimplePageInfo> mSelectedItems;
    private boolean mIsCursorNull;
    private boolean mIsEditMode;
    private boolean mIsSelectAll;
    private boolean mIsPrivate;
    private String mSortOrder;
    private Bitmap mLoadingPageCover;
    private DataCounterListener mDataCounterListener;
    //darwin
    static private boolean isPageGridProcessing = false;
    static private Object mLockObj = new Object();
    //Begin Allen
    private View mAirView = null;
	private ImageView mAirViewImage = null;
	private boolean mHoverExit = true;
	DismissCountDown mAirViewDismissCountDown = new DismissCountDown(1000, 1000);
	DisplayCountDown mAirViewDisplayCountDown = new DisplayCountDown(500, 500);
	
	private int deviceType; //smilefish
	//End Allen
    public boolean getIsPageGridProcessing()
    {
    	synchronized (mLockObj)
		{
    		if(isPageGridProcessing == true)
    		{
    			return true;
    		}
    		else
    		{
    			isPageGridProcessing = true;
    			return false;
    		}
		}
    }

    static public void resetPageGridProcessing()
    {
    	synchronized (mLockObj)
		{
    		isPageGridProcessing = false;
		}
    }
    //darwin
    
    //smilefish fix bug 700568
    public void setDateAndTimeFormat(){
        mTimeFormat = DateFormat.getTimeFormat(mContext);
        mDateFormat = DateFormat.getDateFormat(mContext);//smilefish
    }

    public PageGridViewAdapter(Context context, Long owner, String sortOrder, boolean isPrivate) {
        mContext = context;
        mBookcase = BookCase.getInstance(mContext);
        mSortOrder = sortOrder;
        mPageCursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null, "owner = ?", new String[] { owner.toString() }, mSortOrder);

        deviceType = PickerUtility.getDeviceType(mContext); //smilefish
        mIsSelectAll = false;
        mIsCursorNull = false;
        mIsPrivate = isPrivate;
        mPageDate = new Date();
        setDateAndTimeFormat();
        mPageCursor.moveToFirst();
        if (mIsPrivate == false) {
            BookCase.getInstance(mContext).setCurrentBook(owner);
        }
        mNotebook = BookCase.getInstance(mContext).getNoteBook(owner);
        if (mNotebook == null) {
            mIsCursorNull = true;
        }

        if (mNotebook != null) {
            if (mLoadingPageCover != null && mLoadingPageCover.isRecycled() == false) {
                mLoadingPageCover.recycle();
                mLoadingPageCover = null;
            }
            int line = mNotebook.getGridType();
            int color = mNotebook.getBookColor();
            // BEGIN: archie_huang@asus.com
            int pageSize = mNotebook.getPageSize();
            mLoadingPageCover = CoverHelper.getDefaultCoverBitmap(color, line, mContext.getResources());//Allen           
        }
    }

    @Override
    public int getCount() {
        if ((mNotebook == null) || mIsCursorNull) {
            return 0;
        } else {
            return mNotebook.getTotalPageNumber() + MIN_ITEM_NUM;
        }

    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {//smilefish
    	Long pageId = 0L;
    	if(position > 0){
            if (mSortOrder == null) {
                pageId = mNotebook.getPageOrder((position - 1));
            }
            else {
                mPageCursor.moveToPosition((position - 1));
                pageId = mPageCursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE);
            }
    	}
        return pageId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ViewHolder holder;
        if (position == 0) {
            view = View.inflate(mContext, R.layout.page_add_item, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.page_cover);
            imageView.setImageBitmap(null);
            float alpha = mIsEditMode ? 0.4f : 1.0f;
            boolean enabled = mIsEditMode ? false : true;//Modified by show
            view.findViewById(R.id.page_cover).setAlpha(alpha);
            view.findViewById(R.id.page_add_icon).setAlpha(alpha);
            view.findViewById(R.id.page_add_text).setAlpha(alpha);
            view.findViewById(R.id.add_page_cover_mask).setEnabled(enabled);//Modified by show
            view.findViewById(R.id.add_page_cover_mask).setOnClickListener(mIsEditMode ? null : addPageListener); //Modified by show
        }
        else {
            if (convertView == null || convertView.getTag() == null) {
                view = View.inflate(mContext, R.layout.page_item, null);
                holder = new ViewHolder();
                holder.pageCover = (ImageView) view.findViewById(R.id.page_cover);
                holder.pageCoverMask = (ImageView) view.findViewById(R.id.page_cover_mask);
                holder.pageNumber = (TextView) view.findViewById(R.id.page_number);
                holder.pageCheck = (ImageView) view.findViewById(R.id.page_check);
                holder.pageTime = (TextView) view.findViewById(R.id.page_time);
                holder.pageDate = (TextView) view.findViewById(R.id.page_date);
                view.setTag(holder);
            }
            else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            CursorIconLibrary.setStylusIcon( holder.pageCoverMask, CursorIconLibrary.STYLUS_ICON_FOCUS);//by jason
            Long pageId = 0L;
            if (mSortOrder == null) {
                pageId = mNotebook.getPageOrder((position - 1));
            }
            else {
                mPageCursor.moveToPosition((position - 1));
                pageId = mPageCursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE);
            }
            final Long finalPageId = pageId;         
            if (mIsEditMode) {        
                if(isPageSelected(pageId))
                	holder.pageCheck.setVisibility(View.VISIBLE);
                else
                	holder.pageCheck.setVisibility(View.GONE);
                holder.pageCheck.setOnClickListener(pageCheckedListener); //fix bug 290494 by smilefish
                view.setOnClickListener(selectedPageListener);
                view.setOnLongClickListener(startDragListener);
                view.setOnDragListener(dragListener);
            }
            else {
                holder.pageCheck.setVisibility(View.GONE);
                holder.pageCheck.setOnClickListener(null);
                view.setOnClickListener(enterPageListener);
                view.setOnLongClickListener(null);
                view.setOnDragListener(null);
            }

            holder.pageCoverMask.setTag(view);
            holder.pageCoverMask.setOnClickListener(mIsEditMode ? selectedPageMaskListener : maskClickListener);
            holder.pageCoverMask.setOnLongClickListener(maskLongClickListener);
            //Begin Allen
            if(MethodUtils.isEnableAirViewContentPreview(mContext)){
            	holder.pageCoverMask.setOnHoverListener(new View.OnHoverListener() {
            		@Override
            		public boolean onHover(View view, MotionEvent event) {
            			if (!MethodUtils.isEnableAirViewContentPreview(mContext)) {
							return false;
						}
            			int action = event.getActionMasked();
            			switch(action){
            			case MotionEvent.ACTION_HOVER_ENTER:
            				mHoverExit = false;
            				int[] location =  new int[2];
            				view.getLocationInWindow(location);
            				mAirViewDisplayCountDown.init(finalPageId, location[0],location[1]);
            				mAirViewDisplayCountDown.start();
            				break;
            			case MotionEvent.ACTION_HOVER_EXIT:
            				if (!mHoverExit&&mAirViewShow&&IsHitInRegion.isHitIn(mAirView, (int)event.getRawX(), (int)event.getRawY())) {
								return true;
							}
            				mAirViewDisplayCountDown.cancel();
            				dismissAirView();
            				mHoverExit = true;
            				break;
            			}
            			return false;
            		}
            	});
            }
            //End Allen
            
            holder.pageCover.setTag(Integer.valueOf(position));
            Drawable oldDrawable = holder.pageCover.getDrawable();
            if (oldDrawable != null && oldDrawable instanceof BitmapDrawable) {

                BitmapDrawable oldBitmapDrawable = (BitmapDrawable) oldDrawable;
                Bitmap oldBitmap = oldBitmapDrawable.getBitmap();
                if (oldBitmap != null && oldBitmap.isRecycled() == false && oldBitmap.sameAs(mLoadingPageCover) == false) {
                    oldBitmap.recycle();
                    oldBitmap = null;
                }
            }
            holder.pageCover.setImageBitmap(mLoadingPageCover);

            // BEGIN: Shane_Wang 2012-10-8
            Cursor modCursor = mContext.getContentResolver().query(MetaData.PageTable.uri, 
					new String[] {MetaData.PageTable.MODIFIED_DATE}, "created_date = ?",
					new String[] { Long.toString(pageId) }, null);
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
            
            holder.pageNumber.setText(Integer.toString(mNotebook.getPageIndex(pageId) + 1));
            holder.pageTime.setTag(pageId);
            holder.pageTime.setText(mTimeFormat.format(mPageDate));
            holder.pageDate.setTag(pageId);
            holder.pageDate.setText(mDateFormat.format(mPageDate));
            holder.pageCheck.setTag(pageId);
            //begin darwin
            Bitmap bmp = NotePage.getThumbnail(mNotebook.getCreatedTime(),pageId);
            if(bmp != null)
            {
            	// BEGIN: Better
                // add background to version 1 page thumbnail
    			Cursor cursor = mContext.getContentResolver().query(MetaData.PageTable.uri, 
    					new String[] {MetaData.PageTable.VERSION}, "created_date = ?",
    					new String[] { Long.toString(pageId) }, null);
    			if (cursor != null) {
					if(cursor.getCount() > 0) {
						cursor.moveToFirst();
		    			int version = cursor.getInt(0);
		    			if ((version == 1) && !(MetaData.SavingPageIdList.contains(pageId))) {
		    				Bitmap cover = getCover();
		    				int width = cover.getWidth();
		    				int height = cover.getHeight();
		    				Bitmap bmpCover = Bitmap.createBitmap(width, height,
		    						Config.ARGB_8888);
		    				Canvas canvas = new Canvas(bmpCover);
		    				canvas.drawBitmap(cover, 0, 0, new Paint());
		    				Resources res = mContext.getResources();
		    				float left = res.getDimension(R.dimen.thumb_padding_left);
		    	            float top = res.getDimension(R.dimen.thumb_padding_top);
		    	            canvas.translate(left, top);
		    	            Paint paint = new Paint();
		    	            paint.setAntiAlias(true);
		    	            paint.setDither(true);
		    	            paint.setFilterBitmap(true);
		    				canvas.drawBitmap(bmp, 0, 0, paint);
		    				bmp = bmpCover;
		    			}
		    			// END: Better
		            	holder.pageCover.setImageBitmap(bmp);
					}
					cursor.close();
    			}
            }
        }

        return view;
    }

    //Begin Allen
    private boolean mAirViewShow=false;
    private void displayAirView(final Long pageId,float x,float y){
    	dismissAirView();
    	Resources res = mContext.getResources();
    	mAirView = View.inflate(mContext, R.layout.airview_pageview, null);
		mAirViewImage = (ImageView) mAirView.findViewById(R.id.airview_pageview_image);
    	if(mNotebook.getBookColor() == MetaData.BOOK_COLOR_WHITE){
    		mAirViewImage.setBackgroundResource(R.drawable.asus_airview_photo_board_light_bg);
    	}else{  //smilefish
    		mAirViewImage.setBackgroundResource(R.drawable.asus_airview_photo_board_light_bg_yellow);
    	}
		
		View cover =mAirView.findViewById(R.id.airview_pageview_cover);
		CursorIconLibrary.setStylusIcon(cover, CursorIconLibrary.STYLUS_ICON_FOCUS);//by jason
		cover.setOnHoverListener(new View.OnHoverListener() {
    		@Override
    		public boolean onHover(View arg1, MotionEvent event) {
    			int action = event.getActionMasked();
    			switch(action){
    			case MotionEvent.ACTION_HOVER_ENTER:
    				mHoverExit = false;
    				break;
    			case MotionEvent.ACTION_HOVER_EXIT:
    				dismissAirView();
    				mHoverExit = true;
    				break;
    			}
    			return true;
    		}
    	});
		mAirViewImage.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				NotePage page = mNotebook.getNotePage(pageId);
				try
            	{
					Intent intent = new Intent(mContext, EditorActivity.class);
					intent.putExtra(MetaData.BOOK_ID, page.getOwnerBookId());
					intent.putExtra(MetaData.PAGE_ID, pageId);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
					mContext.startActivity(intent);
            	}catch(Exception e)
            	{
            		e.printStackTrace();
            	}
				return true;
			}
		});
		
		WindowManager wm = (WindowManager) mContext.getApplicationContext()
				.getSystemService("window");
		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
		wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		wmParams.format = PixelFormat.RGBA_8888;
		wmParams.gravity = Gravity.START|Gravity.TOP;
		wmParams.flags|=LayoutParams.FLAG_NOT_FOCUSABLE|LayoutParams.FLAG_NOT_TOUCH_MODAL ;
   		int airView_Bookview_Width = (int)(mContext.getResources().getDimension(R.dimen.AirView_BookView_One_Width));
		int airView_Bookview_Height= (int)(mContext.getResources().getDimension(R.dimen.AirView_BookView_One_Height));
		wmParams.width =  airView_Bookview_Width;
		wmParams.height =airView_Bookview_Height;
		wmParams.x = (int) x;
		wmParams.y = (int) y;
		Bitmap bmp = NotePage.getAirView(mNotebook.getCreatedTime(),pageId);
		if(bmp == null){
			PageDataLoader loader = new PageDataLoader(mContext);
			NotePage page = mNotebook.getNotePage(pageId);
			if (page!=null && loader.load(page)) {
				bmp = page.genAirViewThumb(loader, true, mNotebook.getPageSize() == MetaData.PAGE_SIZE_PHONE);
			}
		}
		
		if(bmp != null){
        	mAirViewImage.setImageBitmap(bmp);
        }
		wm.addView(mAirView, wmParams);
		mAirViewShow = true;
    }
    
    
	private class DismissCountDown extends CountDownTimer{

		public DismissCountDown(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {	
	    	if(mHoverExit){
	    		dismissAirView();
	    	}
		}

		@Override
		public void onTick(long arg0) {
			
		}
	}
	
	private class DisplayCountDown extends CountDownTimer{

		private Long pageId = (long) -1;
		private float x = 0,y = 0;
		public DisplayCountDown(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		public void init(Long pageId,float x ,float y){
			this.pageId = pageId;
			this.x = x;
			this.y = y;
		}
		
		@Override
		public void onFinish() {	
	    	if( pageId != -1&&!mHoverExit){
	    		displayAirView(pageId, x, y);
	    	}
		}

		@Override
		public void onTick(long arg0) {
			
		}
	}
    
    private void dismissAirView(){
    	if(mAirView!=null && mAirView.getParent()!=null&&mAirViewShow) {
    		WindowManager wm = (WindowManager) mContext.getApplicationContext()
    				.getSystemService("window");
	    	wm.removeView(mAirView);
    	}
    	mAirViewShow = false;
    }
    //End Allen
    
    private static class ViewHolder {
    	ImageView pageCheck;//smilefish
        ImageView pageCover;
        ImageView pageCoverMask;
        TextView pageNumber;
        TextView pageTime;
        TextView pageDate;
    }

    private final View.OnClickListener maskClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	if(MethodUtils.isEnableAirViewContentPreview(mContext)){
        		mAirViewDisplayCountDown.cancel();//Allen
        	}
            View view = (View) v.getTag();
            //darwin
        	if(getIsPageGridProcessing())
        	{
        		return;
        	}
            //darwin
            View viewPage = view.findViewById(R.id.page_date);
            // BEGIN: Better
            long pageId = (Long) viewPage.getTag();
            if (!MetaData.SavingPageIdList.contains(pageId)) {
	            NotePage page = mNotebook.getNotePage(pageId);
	            if (page != null) {
	            	PageGridFragment.mSearchString = "";//RICHARD
	            	try
	            	{
						Intent intent = new Intent(mContext, EditorActivity.class);
						intent.putExtra(MetaData.BOOK_ID, page.getOwnerBookId());
						intent.putExtra(MetaData.PAGE_ID, pageId);
						mContext.startActivity(intent);
	            	}catch(Exception e)
	            	{
	            		e.printStackTrace();
	            	}
	            }
            }
        	// END: Better
        }
    };

    private final View.OnLongClickListener maskLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            View view = (View) v.getTag();
            view.performLongClick();
            return true;
        }
    };

    private final OnClickListener addPageListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
    		//emmanual
    		if(MetaData.isSDCardFull()){
    			MetaData.showFullNoAddToast(mContext);
    			return ;
    		}
    		
        	//darwin
        	if(getIsPageGridProcessing())
        	{
        		return;
        	}
            //darwin
            NotePage notepage = new NotePage(mContext, mNotebook.getCreatedTime());
            notepage.setTemplate(mNotebook.getTemplate());//wendy allen++ for template 0706
            notepage.setIndexLanguage(mNotebook.getIndexLanguage());//RICHARD
            mNotebook.addPage(notepage);
            try {
				Intent intent = new Intent();
				intent.setClass(mContext, EditorActivity.class);
				intent.putExtra(MetaData.BOOK_ID, notepage.getOwnerBookId());
				intent.putExtra(MetaData.PAGE_ID, notepage.getCreatedTime());
				if(mNotebook.getTemplate() != MetaData.Template_type_normal&&
						mNotebook.getTemplate() != MetaData.Template_type_blank)
				{
					intent.putExtra(MetaData.TEMPLATE_BOOK_NEW, 1L);//not 0 is OK;
				}
				intent.putExtra(MetaData.IS_NEW_PAGE, true);
				mContext.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            PageGridFragment.mSearchString = "";//RICHARD
            mPageCursor.requery();
            notifyDataSetChanged();
        }
    };

    private final OnClickListener enterPageListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	//darwin
        	if(getIsPageGridProcessing())
        	{
        		return;
        	}
            //darwin
            View view = v.findViewById(R.id.page_date);
            // BEGIN: Better
            long pageId = (Long) view.getTag();
            if (!MetaData.SavingPageIdList.contains(pageId)) {
            	Log.v(MetaData.DEBUG_TAG, "enter page: book id=" + mNotebook.getCreatedTime() + ", page id=" + pageId);
	            NotePage page = mNotebook.getNotePage(pageId);
	            if (page != null) {
	            	PageGridFragment.mSearchString = "";//RICHARD
	            	
	            	try
	            	{
						Intent intent = new Intent(mContext, EditorActivity.class);
						intent.putExtra(MetaData.BOOK_ID, page.getOwnerBookId());
						intent.putExtra(MetaData.PAGE_ID, pageId);
						mContext.startActivity(intent);
	            	}catch(Exception e)
	            	{
	            		e.printStackTrace();
	            	}
	            }
            }
        	// END: Better
        }
    };

    private final OnLongClickListener startDragListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            mDraggedView = view;
            view.startDrag(null, new DragShadowBuilder(view), null, 0);
            view.setVisibility(View.INVISIBLE);
            return true;
        }
    };

    private final OnDragListener dragListener = new OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {

        	if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
        	}
        	else if (event.getAction() == DragEvent.ACTION_DRAG_LOCATION) {
        		
        		ViewHolder holderA = (ViewHolder) mDraggedView.getTag();
                ViewHolder holderB = (ViewHolder) v.getTag();
                Long pageAId = (Long) holderA.pageCheck.getTag();
                Long pageBId = (Long) holderB.pageCheck.getTag();

                holderA.pageCheck.setTag(pageBId);
                holderB.pageCheck.setTag(pageAId);

                // swap the infomation in the mSelectedItems
                synchronized (mSelectedItems) {
                    Long bookId = mNotebook.getCreatedTime();
                    int pageAIndex = mNotebook.getPageIndex(pageAId);
                    int pageBIndex = mNotebook.getPageIndex(pageBId);
                    SimplePageInfo infoA = getSimplePageInfo(bookId, pageAId, 0, pageAIndex);
                    SimplePageInfo infoB = getSimplePageInfo(bookId, pageBId, 0, pageBIndex);
                    if (infoA != null) {
                        infoA.pageIndex = mNotebook.getPageIndex(pageBId);
                    }
                    if (infoB != null) {
                        infoB.pageIndex = mNotebook.getPageIndex(pageAId);
                    }
                    mNotebook.changePageOrder(pageAId, pageBId);
                    //begin darwin
                    if(pageAIndex == 0 || (pageBIndex == 0))
                    {
                    	mNotebook.changeBookCover();
                    }
                    //end   darwin
                    notifyDataSetChanged();
                    mDraggedView.setVisibility(View.VISIBLE);
                }
                mDraggedView.setVisibility(View.VISIBLE);
        		v.setVisibility(View.INVISIBLE);
        		mDraggedView = v;
        		
        	}	
        	else if (event.getAction() == DragEvent.ACTION_DROP) {
        	
                

                return true;
            }
            //+++ James, to detect Is the dragging event overbound
            else if (event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                mDraggedView.post(new Runnable() {

                    @Override
                    public void run() {
                        mDraggedView.setVisibility(View.VISIBLE);
                    }
                });
                return true;
            }
            //--- 
            return true;
        }
    };

    public View getDraggedView() {
        return mDraggedView;
    }

    public boolean isEditMode() {
        return mIsEditMode;
    }

    public void setEditMode(boolean b) {
        mIsEditMode = b;
        mSelectedItems = (mIsEditMode) ? (new ArrayList<SimplePageInfo>()) : null;
    }

    public boolean isSelectAll() {
        return mIsSelectAll;
    }

    public void setSelectAll(boolean b) {
        if (mSelectedItems == null) {
            mSelectedItems = new ArrayList<SimplePageInfo>();
        }
        mIsSelectAll = (mSelectedItems.size() == mPageCursor.getCount()) ? false : b;
        if (mIsSelectAll) {
        	mSelectedItems.clear(); //smilefish
            mPageCursor.moveToFirst();
            while (!mPageCursor.isAfterLast()) {
                Long bookId = mNotebook.getCreatedTime();
                Long pageId = mPageCursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE);
                int pageIndex = mNotebook.getPageIndex(pageId);
                SimplePageInfo info = new SimplePageInfo(bookId, pageId, 0, pageIndex);
                mSelectedItems.add(info);
                mPageCursor.moveToNext();
            }
        }
        else {
            mSelectedItems.clear();
        }
        selectedDataChange(mSelectedItems.size());
        if (mNotebook != null) {
            SortedSet<SimplePageInfo> sortedItems = new TreeSet<SimplePageInfo>(new PageComparator());
            for (SimplePageInfo info : mSelectedItems) {
                sortedItems.add(info);
            }
            mNotebook.setSelectedItems(sortedItems);
        }
        notifyDataSetChanged();
    }

    public void changeSortOrder(Long id, String sortOrder) {
        mNotebook = mBookcase.getNoteBook(id);
        mSortOrder = sortOrder;
        if (mPageCursor != null) {
            mPageCursor.close();
        }
        mPageCursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null, "owner = ?", new String[] { id.toString() }, mSortOrder);

        if (mPageCursor == null) {
            return;
        }
        if (mPageCursor.getCount() == 0) {
            Cursor cursor = mContext.getContentResolver().query(MetaData.BookTable.uri, null, "created_date = ?", new String[] { id.toString() }, null);
            if (cursor != null && cursor.getCount() == 0) {
                mIsCursorNull = true;
            }
            if(cursor != null)
            	cursor.close();
        }
        else {
            mIsCursorNull = false;
        }
        if (mNotebook != null) {
            int line = mNotebook.getGridType();
            // BEGIN: archie_huang@asus.com
            mLoadingPageCover = CoverHelper.getDefaultCoverBitmap(mNotebook.getBookColor(), line, mContext.getResources());//Allen
        }

        notifyDataSetChanged();
        dataChange(getCount());
    }

    private boolean isPageSelected(Long pageId) {
        if (mSelectedItems == null) {
            return false;
        }
        for (SimplePageInfo info : mSelectedItems) {
            if (info.pageId.equals(pageId)) {
                return true;
            }
        }
        return false;
    }

    private SimplePageInfo getSimplePageInfo(Long b, Long p, int bI, int pI) {
        if (mSelectedItems == null) {
            return null;
        }
        for (SimplePageInfo info : mSelectedItems) {
            if (info.bookId.equals(b) == false) {
                continue;
            }
            else if (info.pageId.equals(p) == false) {
                continue;
            }
            else if (info.bookIndex != bI) {
                continue;
            }
            else if (info.pageIndex != pI) {
                continue;
            }
            else {
                return info;
            }
        }
        return null;
    }
    
    //begin smilefish
  	private void updatePageCheckedStatus(Long pageId){
        Long bookId = mNotebook.getCreatedTime();

        int pageIndex = mNotebook.getPageIndex(pageId);
        SimplePageInfo info = new SimplePageInfo(bookId, pageId, 0, pageIndex);
        if (isPageSelected(pageId) == false) {
            mSelectedItems.add(info);
        }
        else {
            synchronized (mSelectedItems) {
                Iterator<SimplePageInfo> iter = mSelectedItems.iterator();
                while (iter.hasNext()) {
                    SimplePageInfo target = iter.next();
                    if (target.pageId.equals(pageId) && target.bookId.equals(bookId)) {
                        iter.remove();
                        break;
                    }
                }
            }

        }

        selectedDataChange(mSelectedItems.size());
        if (mNotebook != null) {
            SortedSet<SimplePageInfo> sortedItems = new TreeSet<SimplePageInfo>(new PageComparator());
            for (SimplePageInfo itemInfo : mSelectedItems) {
                sortedItems.add(itemInfo);
            }
            mNotebook.setSelectedItems(sortedItems);
        }
        notifyDataSetChanged();
  	}
  	
	private View.OnClickListener pageCheckedListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Long pageId = (Long)v.getTag();
			updatePageCheckedStatus(pageId);
		}
	
	};
  	//end smilefish
  	
    private View.OnClickListener selectedPageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	//mars fixed for monkey test,sometimes mSelectedItems == null,need trace
        	if (mSelectedItems == null)
        		return;
        	
            Long pageId = 0L;
            if (v instanceof CheckBox) {
                pageId = (Long) v.getTag();
            }
            else {
                ViewHolder holder = (ViewHolder) v.getTag();
                pageId = (Long) holder.pageCheck.getTag();
            }
            
            updatePageCheckedStatus(pageId);
        }
    };
    
    private View.OnClickListener selectedPageMaskListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	if(MethodUtils.isEnableAirViewContentPreview(mContext)){
        		mAirViewDisplayCountDown.cancel();//Allen
        	}
            Long pageId = 0L;
            View view = (View) v.getTag();
            if (view instanceof CheckBox) {
                pageId = (Long) view.getTag();
            }
            else {
                ViewHolder holder = (ViewHolder) view.getTag();
                pageId = (Long) holder.pageCheck.getTag();
            }

            updatePageCheckedStatus(pageId);
        }
    };

    public void closeCursor() {
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
    
    // BEGIN: Better
	private Bitmap getCover() {
		Bitmap cover = null;
		if (mNotebook != null) {
			int line = mNotebook.getGridType();
			int color = mNotebook.getBookColor();
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
			Cursor cursor = mContext.getContentResolver().query(MetaData.PageTable.uri, 
					new String[] {MetaData.PageTable.VERSION}, "created_date = ?", 
					new String[] { Long.toString(mOldId) }, null);
			if (cursor != null) {
				if(cursor.getCount() > 0) {
					cursor.moveToFirst();
					int version = cursor.getInt(0);
					
					if ((result != null)
							&& (version == 1) 
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
			            Bitmap bmp = Bitmap.createBitmap(result.getWidth(), result.getHeight(),
								Config.ARGB_8888);
						Canvas canvas = new Canvas(bmp);
						canvas.drawBitmap(result, 0, 0, paint);
						Resources res = mContext.getResources();
						float left = res.getDimension(R.dimen.thumb_padding_left);
			            float top = res.getDimension(R.dimen.thumb_padding_top);
			            canvas.translate(left, top);
						canvas.drawText("Saving", 0, 0, paint);
						result = bmp;
					}
				}
				cursor.close();
			}
			return result;
			// END: Better
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            // Log.d(TAG, "[onPostExecute]" + mOldHolder.pageCover.getTag() + ", " + ((result == null) ? "no bitmap" : ""));
            if (mOldHolder != null && result != null) {
                Long newId = (Long) mOldHolder.pageCheck.getTag();
                if (newId.equals(mOldId)) {
                    if (mOldHolder.pageCover.getDrawable() instanceof BitmapDrawable) {
                        BitmapDrawable bd = (BitmapDrawable) mOldHolder.pageCover.getDrawable();
                        if (bd != null) {
                            Bitmap b = bd.getBitmap();
                            if (b != null && b.isRecycled() == false && b.sameAs(mLoadingPageCover) == false) {
                                b.recycle();
                                b = null;
                            }
                        }
                    }
                    mOldHolder.pageCover.setImageBitmap(result);
                }
                else if (result != null && (result.isRecycled() == false)) {
                    result.recycle();
                    result = null;
                }
            }
        }
    }

}
