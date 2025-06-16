package com.asus.supernote.widget;

import java.util.Date;
import java.util.List;

import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.ui.CoverHelper;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.RemoteViews;

public class SingleWidgetItem {

	public final static int BLANK_MODE = 0;
	public final static int BOOK_MODE = 1;
	public final static int BOOK_TEMPLATE_MODE = 2;

	//main property
	private int mIDInDatabase = -1;
	private int mMode = BLANK_MODE;  
	private Context mContext;
	private int mAppWidgetID; 
	private int mItemOrderId;
	private long mBookId;
	private long mPageId;
	
	private BookCase mBookCase;
	private NoteBook mNotebook;
	private List<Long> page_list;
	private int mBookPageNumber;
	
	private int mCurrentPage;
	private int mAll_book_template_Status = 1; //1:all book, 0:all template
	private int mLockStatus =1;  //1:lock, 0:unlock

	private RemoteViews mRemoteViews;
	public int mFrameLayoutId;
	private int mFlipCommand=-1;
	private Boolean mChangedFlag = false;
	private int mPageListScrollPosition = -1; //Allen  
	public final static int BOOK_DRAWABLE_ID = 0;
	public final static int PAGE_DRAWABLE_ID = 1;
	public final static int DIALOG_BOOK_DRAWABLE_ID = 2;
	public final static int PAD_PORT_PAGE_DRAWABLE_ID = 3;
	
	private int secondBtn_pageNo=-1; //Carol
	private int thirdBtn_pageNo=-1;
	private int fourthBtn_pageNo=-1;
	private int mDeviceType = -1;

	public SingleWidgetItem(int idInDataBase,
			Context Context,   int item_widegt_id,    
			int item_order_id,  int item_mode,
            Long item_book_id,  Long item_page_id, 
            int item_all_book_template , int item_lock_state,
            int frameLayoutId )
	{
		this.mIDInDatabase  =   idInDataBase;
		this.mContext       = 	Context;
		this.mAppWidgetID	=	item_widegt_id; 
		this.mItemOrderId	=	item_order_id;
		this.mMode			=	item_mode;
		this.mBookId		=	item_book_id;
		this.mPageId			=	item_page_id;
		this.mAll_book_template_Status=	item_all_book_template;
		this.mLockStatus		=	item_lock_state;
		this.mFrameLayoutId =  frameLayoutId;
		mDeviceType =  mContext.getResources().getInteger(R.integer.device_type);
		checkSyncStatusChanges();
		MetaData.WIDGET_BOOK_ID = mBookId;
	}

	
	public Boolean getIsChanged()
	{
		return mChangedFlag;
	}
	
	public void setIsChagned(Boolean change)
	{
		mChangedFlag = change;
	}
	
	public int getItemOrderID()
	{
		return mItemOrderId;
	}
	
	public void setBookId(long bookId)
	{
		mBookId = bookId;
	}
	
	public long getBookId()
	{
		return mBookId;
	}
	
	public long getPageId(){
		return mPageId;
	}
	
	public void setPageId(long pageId)
	{
		mPageId = pageId;
	}
	
	public void setLockStatus(int lockStatus)
	{
		mLockStatus = lockStatus;
	}
	
	public void setFlipCommand(int command)
	{
		mFlipCommand = command;
	}
	
	//Begin:Siupo
	private String toString(int srcID)
	{
		String ret = null;
		ret = 	"str"+
				mAppWidgetID+
				mItemOrderId+
				mBookId+
				mPageId+
				mAll_book_template_Status+
				mLockStatus+mFrameLayoutId+
				srcID;
		return ret;
	}//End Siupo
	public void setWidgetItem() {
		switch (mMode) {
		case BLANK_MODE:
			if(MetaData.WIDGET_ONE_COUNT == 0){
				setBlankRemoteViews();
			}else{
				SetBookcaseRemoteViews();
			}
			break;
		case BOOK_MODE:
			if(prepareBookAndPageInfo())
			{			
				setBookRemoteViews();
			}
			else
			{
				setBlankRemoteViews();
				setIsChagned(true);
			}
			break;
		case BOOK_TEMPLATE_MODE:
			setBookTemplateRemoteViews();
			break;
		default:
			break;
		}
	}

	private void setBookTemplateRemoteViews()
	{
		// TODO Auto-generated method stub
		mRemoteViews = new RemoteViews(mContext.getPackageName(),
						R.layout.widget_allbook_template_layout_al);//for Andorid L
		setNoteBookTemplateIntent(R.id.btAllBook1);
		setNoteBookTemplateIntent(R.id.btTemplate1);
		setNoteBookTemplateIntent(R.id.btReplace1);
	}
	private void setNoteBookTemplateIntent(int rscID)
	{
		Bundle exData = new Bundle();
		Intent intent = new Intent();
		intent.setClass(mContext, WidgetService.class);
		intent.setAction(WidgetService.ALLNOTEBOOK_TEMPLATE_ACTION);
		String strData = String.valueOf(mAppWidgetID)+" "+String.valueOf(mItemOrderId)+" "+String.valueOf(rscID);
		intent.setData(Uri.fromParts("id", strData, null));//Vary the different Intent;
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		exData.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetID);
		exData.putInt(WidgetService.WIDGET_ITEM_ID_INTENT_KEY,mItemOrderId);
		PendingIntent pendingIntent = null;
		if(rscID==R.id.btAllBook1)
		{
			exData.putInt(WidgetService.SUBACTION, WidgetService.ALLNOTEBOOK_TEMPLATE_SUBACTION_SHOWMYBOOK);
		}
		if(rscID==R.id.btTemplate1)
		{
			exData.putInt(WidgetService.SUBACTION, WidgetService.ALLNOTEBOOK_TEMPLATE_SUBACTION_ADDNEWBOOK);
		}
		if(rscID==R.id.btReplace1)
		{
			exData.putInt(WidgetService.SUBACTION, WidgetService.ALLNOTEBOOK_TEMPLATE_SUBACTION_REPLACEWDBOOK);
		}
		
		intent.putExtras(exData);
		pendingIntent = PendingIntent.getService(mContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews.setOnClickPendingIntent(rscID, pendingIntent);
	}

	private void setBookRemoteViews( ) {
		// TODO Auto-generated method stub
		if( mNotebook.getIsLocked() && this.mLockStatus==1)
		{
			mRemoteViews = new RemoteViews(mContext.getPackageName(),R.layout.widget_edit_book_layout1);
			mRemoteViews.setViewVisibility(R.id.locked_book_frame,0);
			setBookImageAndText();
			mRemoteViews.setImageViewResource(R.id.imageView_locked_blank, R.drawable.asus_ep_bar_lock_);//asus_ep_bg_lock_right);
			setBookClickIntent(R.id.imageView_locked_blank);
		}
		else
		{
			//Siupo
			mRemoteViews = new RemoteViews(mContext.getPackageName(),
					R.layout.widget_edit_book_layout1);
            if(this.mPageId!=-1){
			setBookClickIntent(R.id.imageView_inside);
			setBookClickIntent(R.id.imageView_BookCover);//Siupo
            }
			setBookClickIntent(R.id.all_page_view);
			setBookClickIntent(R.id.replace_book);
			setBookClickIntent(R.id.front_page);
			setBookClickIntent(R.id.next_page);
			
			if(mNotebook.getIsLocked()==true)
			{
				mRemoteViews.setViewVisibility(R.id.book_private, View.VISIBLE);
				setBookClickIntent(R.id.book_private);
			}
			else
			{
				mRemoteViews.setViewVisibility(R.id.book_private, View.INVISIBLE);
			}
			
			setBookImageAndText();
		}
		setScrollToButtonIntent();//Allen
	}	
	
	private void setBookClickIntent(int rscID) {
		Intent intent ;
		PendingIntent pendingIntent = null;		
		intent = new Intent();
		
		intent.setClass(mContext, WidgetService.class);
		intent.setAction(WidgetService.EDITBOOK_ACTION);
		String str = toString(rscID);
		intent.setData(Uri.fromParts("id", str, null));//Siupo,in order to distinguish the intent;
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,mAppWidgetID);
		intent.putExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY,mItemOrderId);
		
		switch (rscID) {
		case R.id.imageView_inside:	
			intent.putExtra(WidgetService.SUBACTION,WidgetService.EDIT_NOTEBOOK_ACTION_ENTERPAEG);				
			intent.putExtra(WidgetService.WIDGET_BOOK_ID, mBookId);
			intent.putExtra(WidgetService.WIDGET_PAGE_ID, mPageId);
			break;
		case R.id.all_page_view:
			intent.putExtra(WidgetService.SUBACTION,WidgetService.EDIT_NOTEBOOK_ACTION_ALLPAGE);
			intent.putExtra(WidgetService.WIDGET_BOOK_ID, mBookId);
			break;
		case R.id.replace_book:
			intent.putExtra(WidgetService.SUBACTION,WidgetService.EDIT_NOTEBOOK_ACTION_REPLACEBOOK);			
			intent.putExtra(WidgetService.WIDGET_BOOK_ID, mBookId);
			break;
		case R.id.front_page:			
			intent.putExtra(WidgetService.SUBACTION,WidgetService.EDIT_NOTEBOOK_ACTION_FRONT_PAGE);		
			break;
		case R.id.next_page:			
			intent.putExtra(WidgetService.SUBACTION,WidgetService.EDIT_NOTEBOOK_ACTION_NEXT_PAGE);			
			break;
		case R.id.imageView_locked_blank:
			intent.putExtra(WidgetService.SUBACTION,WidgetService.EDIT_NOTEBOOK_ACTION_LOCKED_PASSWORD);
			intent.putExtra(WidgetService.WIDGET_BOOK_ID, mBookId);
			break;
		case R.id.book_private:
			intent.putExtra(WidgetService.SUBACTION,WidgetService.EDIT_NOTEBOOK_ACTION_LOCK_BOOK);
			intent.putExtra(WidgetService.WIDGET_BOOK_ID, mBookId);
			break;
		case R.id.imageView_BookCover:
			//Siupo
			intent.putExtra(WidgetService.SUBACTION,WidgetService.EDIT_NOTEBOOK_ACTION_ENTERPAEG);				
			intent.putExtra(WidgetService.WIDGET_BOOK_ID, mBookId);
			//transfer the latest modified page id
			intent.putExtra(WidgetService.WIDGET_PAGE_ID, page_list.get(0));
			break;
		default:			
			break;
		}
		 pendingIntent = PendingIntent.getService(mContext,0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews.setOnClickPendingIntent(rscID, pendingIntent);		
	}
	

	private void setBlankRemoteViews() {
		mRemoteViews = new RemoteViews(mContext.getPackageName(),R.layout.widget_blank_image);
		Intent intent = new Intent();	
		intent.setAction(WidgetService.ADDBOOK_ACTION);
		intent.setClass(mContext, WidgetService.class);		
		intent.setData(Uri.parse("custom"+System.currentTimeMillis()));
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(WidgetService.WIDGET_ADDBOOK_ACTIVITY_FLAG,WidgetService.add_from_blank_item);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetID);
		intent.putExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY,mItemOrderId);
		PendingIntent pendingIntent = PendingIntent.getService(mContext, 0,
				intent,PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.imageView_blank, pendingIntent);
	}
	
	private void SetBookcaseRemoteViews() { //my notebooks and add book template
		Intent intent = new Intent(mContext, WidgetService.class);
		setAllBookTemplateStatus(mDeviceType>100||MetaData.IS_LOAD? 1 : 0); //modified for PAD[Carol]
		try
		{
			intent.setAction(WidgetService.ALLNOTEBOOK_TEMPLATE_ACTION);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,mAppWidgetID);
			intent.putExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, mItemOrderId); 
			intent.putExtra(WidgetService.SUBACTION, mDeviceType>100||MetaData.IS_LOAD ? WidgetService.
				ALLNOTEBOOK_TEMPLATE_SUBACTION_SHOWMYBOOK : WidgetService.ALLNOTEBOOK_TEMPLATE_SUBACTION_ADDNEWBOOK); //Carol
			mContext.startService(intent);
		} catch (ActivityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public RemoteViews getRemoteViews() {
		setWidgetItem() ;
		return mRemoteViews;

	}	
		
	public void setBitMap(Bitmap bitmap) {
		// TODO Auto-generated method stub
		mRemoteViews.setImageViewBitmap(R.id.imageView_blank, bitmap);
	}

	public void setMode(int bookMode) {
		// TODO Auto-generated method stub
		
		this.mMode=bookMode;
	}
	
	public int getMode()
	{
		return mMode;
	}
	
	
	public Boolean prepareBookAndPageInfo() {
		// TODO Auto-generated method stub		
		mBookCase = BookCase.getInstance(mContext);
		mNotebook = mBookCase.getNoteBook(mBookId);		
		
		int first_page = 0;//mDeviceType>100 ? 0 : -1; //choose the first page[Carol]
		
		if(mNotebook != null)
		{
			//BEGIN: RICHARD
			//NOT SHOW LOCK BOOK IN WIDGET
			if(WidgetService.NOT_SHOW_LOCK_BOOK)
			{
				if(mNotebook.getIsLocked())
				{
					mMode = BLANK_MODE;
					//need update,set to blank
					return false;
				}
			}
			//END: RICHARD
			
			page_list = mNotebook.getPageOrderList();
			mBookPageNumber = page_list.size();
			mCurrentPage = first_page; //remove the cover page[Carol]
			if(mBookPageNumber<=0){
				mPageId=-1; 
				return true;
				}
			for(int  pageNum =0;pageNum<mBookPageNumber;pageNum++)
			{
				if(page_list.get(pageNum) == mPageId)
				{
					mCurrentPage = pageNum;
					break;
				}
			}
			
			switch (mFlipCommand) {
				case WidgetService.EDIT_NOTEBOOK_ACTION_FIRST_PAGE:
					mCurrentPage=0;
					break;
				case WidgetService.EDIT_NOTEBOOK_ACTION_FRONT_PAGE:
					mCurrentPage--;
					if(mCurrentPage < first_page) //remove cover page[Carol]
					{
						mCurrentPage = first_page;
					}
					break;
				case WidgetService.EDIT_NOTEBOOK_ACTION_NEXT_PAGE:
					mCurrentPage++;	
					if(mCurrentPage >= mBookPageNumber)
					{
						mCurrentPage = mBookPageNumber - 1;
					}
					break;
				case WidgetService.EDIT_NOTEBOOK_ACTION_LAST_PAGE:
					mCurrentPage= mBookPageNumber-1;
					break;
			}	
			updatePageId();
			return true;
		}
		else
		{
			MetaData.WIDGET_ONE_COUNT = 1;
			mMode = BLANK_MODE;
			//need update,set to blank
			return false;
		}
	}
	//Begin Siupo
	
	public static int getBookOrPageDrawableId(int Type,int id1,int id2,int id3,int id4)
	{
		int drawableId = -1;
		switch(Type)
		{
		case PAGE_DRAWABLE_ID:
			drawableId = id1;//Page
			break;
		case DIALOG_BOOK_DRAWABLE_ID:
			drawableId = id2;//Dialog BOok
			break;
		case BOOK_DRAWABLE_ID:
			drawableId = id3;//Book
			break;
		case PAD_PORT_PAGE_DRAWABLE_ID:
			drawableId = id4;//pad port page
			break;
		}
		return drawableId;
	}
	//Type=0:Book ;Type=1: Page ;Type=2:Dialog Book
	public static int getBookOrPageType(NoteBook noteBook, int Type)
	{
		int drawableId = -1;
		boolean isCloudFile = noteBook.getUserId()>0; //Carol-add cloud image cover
		if(noteBook.getBookColor()==MetaData.BOOK_COLOR_YELLOW) //Yellow-blank,grid,line(normal&cloud)
		{
			if(noteBook.getGridType()==MetaData.BOOK_GRID_BLANK)
			{
				drawableId = getBookOrPageDrawableId(Type,
						isCloudFile?R.drawable.asus_l_middle_blank_y_b:R.drawable.asus_l_middle_page_blank_y,
						isCloudFile?R.drawable.asus_l_popup_blank_y_b:R.drawable.asus_l_popup_blank_y,
						isCloudFile?R.drawable.asus_l_middle_blank_y_b:R.drawable.asus_l_middle_blank_y,
						R.drawable.asus_scroll_page_blank_y);
			}else if(noteBook.getGridType()==MetaData.BOOK_GRID_GRID){
				drawableId = getBookOrPageDrawableId(Type,
						isCloudFile?R.drawable.asus_l_middle_page_grid_y_b:R.drawable.asus_l_middle_page_grid_y,
						isCloudFile?R.drawable.asus_l_popup_grid_y_b:R.drawable.asus_l_popup_grid_y,
						isCloudFile?R.drawable.asus_l_middle_grid_y_b:R.drawable.asus_l_middle_grid_y,
						R.drawable.asus_scroll_page_grid_y);
			}else{
				drawableId = getBookOrPageDrawableId(Type,
						isCloudFile?R.drawable.asus_l_middle_page_line_y_b:R.drawable.asus_l_middle_page_line_y,
						isCloudFile?R.drawable.asus_l_popup_line_y_b:R.drawable.asus_l_popup_line_y,
						isCloudFile?R.drawable.asus_l_middle_grid_y_b:R.drawable.asus_l_middle_line_y,
						R.drawable.asus_scroll_page_line_y);
			}		
		}else if(noteBook.getBookColor()==MetaData.BOOK_COLOR_WHITE){ //White-blank,grid,line(normal&cloud)
			if(noteBook.getGridType()==MetaData.BOOK_GRID_BLANK){
				drawableId = getBookOrPageDrawableId(Type,
						isCloudFile?R.drawable.asus_l_middle_page_blank_b:R.drawable.asus_l_middle_page_blank,
						isCloudFile?R.drawable.asus_l_popup_blank_b:R.drawable.asus_l_popup_blank,
						isCloudFile?R.drawable.asus_l_middle_blank_b:R.drawable.asus_l_middle_blank,
						R.drawable.asus_scroll_page_blank);
			}else if(noteBook.getGridType()==MetaData.BOOK_GRID_GRID){
				drawableId = getBookOrPageDrawableId(Type,
						isCloudFile?R.drawable.asus_l_middle_page_grid_b:R.drawable.asus_l_middle_page_grid,
						isCloudFile?R.drawable.asus_l_popup_grid_b:R.drawable.asus_l_popup_grid,
						isCloudFile?R.drawable.asus_l_middle_grid_b:R.drawable.asus_l_middle_grid,
						R.drawable.asus_scroll_page_grid);
			}else{
				drawableId = getBookOrPageDrawableId(Type,
						isCloudFile?R.drawable.asus_l_middle_page_line_b:R.drawable.asus_l_middle_page_line,
						isCloudFile?R.drawable.asus_l_popup_line_b:R.drawable.asus_l_popup_line,
						isCloudFile?R.drawable.asus_l_middle_line_b:R.drawable.asus_l_middle_line,
						R.drawable.asus_scroll_page_line);
			}
		}
		return drawableId;
	}
	
	public static int getPageCoverMaskBitmap(NoteBook noteBook){
		int drawableId = -1;
		if(noteBook.getUserId()>0){
			drawableId = R.drawable.asus_l_middle_page_b;
		}else{
			if(noteBook.getBookColor()==MetaData.BOOK_COLOR_YELLOW)
				drawableId = R.drawable.asus_l_middle_page_y;
			else if(noteBook.getBookColor()==MetaData.BOOK_COLOR_WHITE)
				drawableId = R.drawable.asus_l_middle_page_g;
		}
		return drawableId;
	}
	
	public static Bitmap getCoverBitmap(Context context,int index)
	{
		Bitmap coverBitmap = null;
		Resources mResources = context.getResources();
		switch(index)
		{
		case 1:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover01);
			break;
		case 2:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover01);
			break;
		case 3:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover02);
			break;
		case 4:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover03);
			break;
		case 5:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover04);
			break;
		case 6:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover05);
			break;
		case 7:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover06);
			break;
		case 8:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover07);
			break;
		case 9:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover08);
			break;
		case 10:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover09);
			break;
		case 11:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover10);
			break;
		case 12:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover11);
			break;
		case 13:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover12);
			break;
		case 14:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover13);
			break;
		case 15:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover14);
			break;
		case 16:
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover15);
			break;
		default :
			coverBitmap = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover01);
			break;
		}
		return coverBitmap;
	}
	
	//return the Book Bitmap
	public static Bitmap getBookOrPageCombineBitmap(Context context, NoteBook noteBook, int drawableId,float leftRatio,float topRatio,int pageIndex)
	{
		Bitmap bottomBitmap = null;
		Bitmap pageContentBitmap = null;
		Bitmap combinBitmap = null;
		int book_item_width = 0;
		int book_item_height = 0;
		int widget_pagecover_width = 0;
		int widget_pagecover_height = 0;
		List<Long> tmp_page_list=null;
		long tmp_bookid;
		tmp_page_list=noteBook.getPageOrderList();
		tmp_bookid=noteBook.getCreatedTime();
		
			if(noteBook.getCoverIndex()>0 && pageIndex==-1)
			{
				combinBitmap = getCoverBitmap(context,noteBook.getCoverIndex());
			}
			else
			{
				bottomBitmap = BitmapFactory.decodeResource(context.getResources(), drawableId);//Book, page ,dialog book;
				book_item_width = bottomBitmap.getWidth();
				book_item_height = bottomBitmap.getHeight();
				if(tmp_page_list!=null&&tmp_page_list.size()>0)
				{
					widget_pagecover_width = book_item_width - (int)(book_item_width*2*leftRatio);
					widget_pagecover_height = book_item_height - (int)(book_item_height*topRatio);
					pageIndex = (pageIndex==-1)? 0:pageIndex;
					Bitmap pageTempBitmap = NotePage.getWidgetThumbnail(tmp_bookid, tmp_page_list.get(pageIndex),context);
					if(pageTempBitmap!=null)
					{
						pageContentBitmap = Bitmap.createScaledBitmap(pageTempBitmap,
								widget_pagecover_width, widget_pagecover_height, true);
						pageTempBitmap.recycle();
					}
				}
				combinBitmap = combinePageContent(bottomBitmap,pageContentBitmap,0);
			}

		return combinBitmap;
	}

	//combine the CoverBitmap and the PageContentBitmap;
	public static Bitmap combinePageContent(Bitmap bitmapCover,Bitmap bitmapContent,float topRatio)
	{
		if(bitmapContent==null)
			return bitmapCover;
		
		int CoverWidth = bitmapCover.getWidth();
		int CoverHeight = bitmapCover.getHeight();
		Paint mBitmapPaint = new Paint();
	    mBitmapPaint.setDither(true);
	    mBitmapPaint.setAntiAlias(true);
	    Bitmap combineResult = Bitmap.createBitmap(CoverWidth, CoverHeight, Bitmap.Config.ARGB_8888);
		Canvas cs = new Canvas(combineResult);
		cs.drawBitmap(bitmapCover, 0, 0, mBitmapPaint);
		float marginLeft = (CoverWidth-bitmapContent.getWidth())/2;
		float marginTop = CoverHeight*topRatio;
		cs.drawBitmap(bitmapContent, marginLeft, marginTop, mBitmapPaint);
		
		if(bitmapCover!=null && !bitmapCover.isRecycled())
		{
			bitmapCover.recycle();
			bitmapCover = null;
		}
		if(bitmapContent!=null && !bitmapContent.isRecycled())
		{
			bitmapContent.recycle();
			bitmapContent = null;
		}
		return combineResult;
	}
	
	//End Siupo
	private void setBookImageAndText()
	{
		float leftRatio = 0.0f;
		float topRatio = 0.0f;
		String book_title=mNotebook.getTitle();
		
		leftRatio = mContext.getResources().getDimension(R.dimen.leftRatio);
		topRatio = mContext.getResources().getDimension(R.dimen.topRatio);

		mRemoteViews.setImageViewResource(R.id.pageCoverMask, getPageCoverMaskBitmap(mNotebook));
		
		if(mCurrentPage < 0 ){
			mRemoteViews.setViewVisibility(R.id.pageCoverMask, View.INVISIBLE);
			mCurrentPage=-1;
			mRemoteViews.setTextViewText(R.id.textView_book_title, book_title);	
			mRemoteViews.setTextViewText(R.id.textview_page_number, Integer.toString(mCurrentPage+1)+"/"+this.mBookPageNumber);
			mRemoteViews.setInt(R.id.front_page, "setImageResource", R.drawable.asus_ep_little_icon_03_d);
			Bitmap bookBitmap = getBookOrPageCombineBitmap(mContext,mNotebook,getBookOrPageType(mNotebook,BOOK_DRAWABLE_ID),leftRatio,topRatio,-1);//Book
			mRemoteViews.setImageViewBitmap(R.id.imageView_inside, bookBitmap);//Siupo
			setBookDateAndType(mNotebook);
		}
		if(mCurrentPage == 0)
			mRemoteViews.setInt(R.id.front_page, "setImageResource", R.drawable.asus_ep_little_icon_03_d);
		if(mCurrentPage >= 0)
		{	
			mRemoteViews.setViewVisibility(R.id.pageCoverMask, View.INVISIBLE);//View.VISIBLE);
			mRemoteViews.setTextViewText(R.id.textView_book_title, book_title);
			setBookDateAndType(mNotebook);
			
			Bitmap pageBitmap = getBookOrPageCombineBitmap(mContext,mNotebook,getBookOrPageType(mNotebook,
					mDeviceType>100?BOOK_DRAWABLE_ID:PAGE_DRAWABLE_ID),leftRatio,topRatio,mCurrentPage);//Page
			mRemoteViews.setImageViewBitmap(R.id.imageView_inside, pageBitmap);//Siupo
			
			Bitmap portCoverBitmap = getBookOrPageCombineBitmap(mContext,mNotebook,getBookOrPageType(mNotebook,BOOK_DRAWABLE_ID),leftRatio,topRatio,-1);//port Book
			mRemoteViews.setImageViewBitmap(R.id.imageView_BookCover, portCoverBitmap);//Siupo
			
			mRemoteViews.setTextViewText(R.id.textview_page_number,  this.mBookPageNumber == 0?"0":Integer.toString(mCurrentPage+1)+"/"+this.mBookPageNumber);
			if(mCurrentPage+1 == mBookPageNumber || this.mBookPageNumber == 0)
			{
				mRemoteViews.setInt(R.id.next_page, "setImageResource", R.drawable.asus_ep_little_icon_04_d);
			}
		}
	}

	private void setBookDateAndType(NoteBook book){
		//add created date & notebook type [Carol]
		Bitmap mPhoneIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_supernote_cover_phone);
		Bitmap mPadIcon   = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_supernote_cover_pad);
		mRemoteViews.setImageViewBitmap(R.id.textView_book_typeMark, (book.getPageSize() == MetaData.PAGE_SIZE_PHONE) ? mPhoneIcon : mPadIcon);

		//begin smilefish fix bug 817660
		Date mDate = new Date();
		Cursor modCursor = mContext.getContentResolver().query(MetaData.BookTable.uri, 
				new String[] {MetaData.BookTable.MODIFIED_DATE}, "created_date = ?",
				new String[] { Long.toString(book.getCreatedTime()) }, null);
        if ((modCursor != null) && (modCursor.getCount() > 0)) { // Better
			modCursor.moveToFirst();
			long modTime = modCursor.getLong(0);
			modCursor.close();
			mDate.setTime(modTime);
        }
        else if(modCursor != null)
        {
			modCursor.close();
        }
        //end smilefish

		java.text.DateFormat dateFormat = DateFormat.getDateFormat(mContext);
		String data = dateFormat.format(mDate);
		mRemoteViews.setTextViewText(R.id.textView_book_date, data);
	}

	private void updatePageId() {
		if (this.mMode == BOOK_MODE) {
			if (mCurrentPage != -1) {
				mPageId = page_list.get(mCurrentPage);
			} else {
				mPageId = page_list.get(0);
			}
		}
	}

	
    public void flipPage(int flipCommand)
    {
		switch (flipCommand) {
		case WidgetService.EDIT_NOTEBOOK_ACTION_FIRST_PAGE:
			mCurrentPage=0;
			break;
		case WidgetService.EDIT_NOTEBOOK_ACTION_FRONT_PAGE:
			mCurrentPage--;
			break;
		case WidgetService.EDIT_NOTEBOOK_ACTION_NEXT_PAGE:
			mCurrentPage++;	
			break;
		case WidgetService.EDIT_NOTEBOOK_ACTION_LAST_PAGE:
			mCurrentPage=mBookPageNumber-1;
			break;
		case WidgetService.EDIT_NOTEBOOK_ACTION_FIRSTBUTTON: //Carol
			mCurrentPage=0;
			break;
		case WidgetService.EDIT_NOTEBOOK_ACTION_SECONDBUTTON:
			mCurrentPage=secondBtn_pageNo-1;
			break;
		case WidgetService.EDIT_NOTEBOOK_ACTION_THIRDBUTTON:
			mCurrentPage=thirdBtn_pageNo-1;
			break;
		case WidgetService.EDIT_NOTEBOOK_ACTION_FOURTHBUTTON:
			mCurrentPage=fourthBtn_pageNo-1;
			break;
		case WidgetService.EDIT_NOTEBOOK_ACTION_LASTBUTTON:
			mCurrentPage=mBookPageNumber-1;
			break;
		default:
				return;
		}
		
		if(mCurrentPage<0 )
		{
			mCurrentPage=-1;
			String book_title=mNotebook.getTitle();
			mRemoteViews.setTextViewText(R.id.textView_book_title, book_title);	
			mRemoteViews.setTextViewText(R.id.textview_page_number, "/");
			mRemoteViews.setImageViewResource(R.id.imageView_inside, R.drawable.asus_supernote_bookcover10);
			setBookDateAndType(mNotebook);
		    return;
		} 
		if( mCurrentPage>mBookPageNumber-1)
		{
			mCurrentPage=mBookPageNumber-1;
		}
		
		String book_title=mNotebook.getTitle();
		mRemoteViews.setTextViewText(R.id.textView_book_title, book_title);
		setBookDateAndType(mNotebook);

				Bitmap bitmap = NotePage.getThumbnail(mBookId, page_list.get(mCurrentPage));
				if(bitmap==null){//Allen
		            bitmap = CoverHelper.getDefaultCoverBitmap(mNotebook.getBookColor(), mNotebook.getGridType(),mContext.getResources());
				}
				mRemoteViews.setImageViewBitmap(R.id.imageView_inside, bitmap);
				mRemoteViews.setTextViewText(R.id.textview_page_number,  Integer.toString(mCurrentPage+1)+"/"+this.mBookPageNumber);
    }
     
	public void insertWidgetItem() {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();
        values.put(MetaData.WidgetItemTable.ITEM_WIDGET_ID,mAppWidgetID );       
        values.put(MetaData.WidgetItemTable.ITEM_ORDER_ID,mItemOrderId );
        values.put(MetaData.WidgetItemTable.ITEM_MODE, mMode);        
        values.put(MetaData.WidgetItemTable.ITEM_BOOK_ID, mBookId);
        values.put(MetaData.WidgetItemTable.ITEM_PAGE_ID,mPageId );
        values.put(MetaData.WidgetItemTable.ITEM_ALLBOOK_ALLTEMPLATE,mAll_book_template_Status );
        values.put(MetaData.WidgetItemTable.ITEM_LOCK_STATE, mLockStatus);
        
        mContext.getContentResolver().insert(MetaData.WidgetItemTable.uri, values);
        
	}
	

	public void writeToDataBase()
	{
		if(mIDInDatabase == -1)
		{
			checkSyncStatusChanges();
			insertWidgetItem() ;
		}
		else
		{
			checkSyncStatusChanges();
			updataWidgetItem();
		}
	}
	
	private void updataWidgetItem() {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues(); 
		values.put(MetaData.WidgetItemTable.ITEM_WIDGET_ID,mAppWidgetID );
        values.put(MetaData.WidgetItemTable.ITEM_ORDER_ID,mItemOrderId );
        values.put(MetaData.WidgetItemTable.ITEM_MODE, mMode);        
        values.put(MetaData.WidgetItemTable.ITEM_BOOK_ID, mBookId);
        if(mCurrentPage < 0)
        {
        	values.put(MetaData.WidgetItemTable.ITEM_PAGE_ID,mCurrentPage );
        }else
        {
        	values.put(MetaData.WidgetItemTable.ITEM_PAGE_ID,mPageId );
        }
        values.put(MetaData.WidgetItemTable.ITEM_ALLBOOK_ALLTEMPLATE,mAll_book_template_Status );
        values.put(MetaData.WidgetItemTable.ITEM_LOCK_STATE, mLockStatus);
        
        String where=MetaData.WidgetItemTable.ITEM_WIDGET_ID+"="+mAppWidgetID+
        		            " AND "+MetaData.WidgetItemTable.ITEM_ORDER_ID+"="+mItemOrderId;
      mContext.getContentResolver().update(MetaData.WidgetItemTable.uri, values, where, null);
	}
	
	private void checkSyncStatusChanges(){
		//update Ids if sync status is changed [Carol]
		if(!MetaData.Changed_Book_List.isEmpty() && MetaData.Changed_Book_List.containsKey(mBookId)){
			mBookId = MetaData.Changed_Book_List.get(mBookId);
	    }
		if(!MetaData.Changed_Page_List.isEmpty() && MetaData.Changed_Page_List.containsKey(mPageId)){
			mPageId = MetaData.Changed_Page_List.get(mPageId);
	    }
	}
	
	public void deleteWidgetItem()
	{
		mContext.getContentResolver().delete(MetaData.WidgetItemTable.uri,MetaData.WidgetItemTable.ID + " = " + mIDInDatabase, null);
	}
	
	public static void updataWidgetLockStatus(Context context,int widgetID,int itemorederID,int status) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues(); 
        values.put(MetaData.WidgetItemTable.ITEM_LOCK_STATE, status);
        
        String where=MetaData.WidgetItemTable.ITEM_WIDGET_ID+"="+widgetID+
        		            " AND "+MetaData.WidgetItemTable.ITEM_ORDER_ID+"="+itemorederID;
        context.getContentResolver().update(MetaData.WidgetItemTable.uri, values, where, null);
	}

	//Begin Siupo
	public void setAllBookTemplateStatus(int status)
	{
		mAll_book_template_Status = status;
	}
	public int getAllBookTemplateStatus()
	{
		return mAll_book_template_Status;
	}
	public int getLockStatus()
	{
		if(mNotebook==null)
			return 0;
		if( mNotebook.getIsLocked() && this.mLockStatus==1)
			return 1;//Locked Status;
		else
			return 0;//Unlocked Status;
	}
	//End Siupo
	
	public void setNextOrPrevBook(Boolean flag)
	{
		mBookCase = BookCase.getInstance(mContext);
		mNotebook = mBookCase.getNoteBook(mBookId);	
		if(mNotebook == null)
		{
			mMode = BLANK_MODE;
		}
		else
		{
			long tempBookId = mBookCase.getNextOrPrevBookID(mBookId, flag);
			if(mBookCase.getNoteBook(tempBookId) != null)
			{
				mBookId = tempBookId;
			}
		}		
	}
	
	//Begin Allen
	private PendingIntent getScrollToPendingIntent(short scrollTo){
		Intent intent = new Intent();
		intent.setClass(mContext, WidgetService.class);
		intent.setAction(WidgetService.ACTION_LISTVIEW_SCROLL);
		intent.setData(Uri.parse(toString(scrollTo)));
		intent.putExtra(WidgetService.EXTRA_LISTVIEW_SCROLL_TO, scrollTo);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,mAppWidgetID);
		intent.putExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY,mItemOrderId);
		PendingIntent pendingIntent = null;
		pendingIntent = PendingIntent.getService(mContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}
	
	private void setScrollToButtonIntent(){
		mRemoteViews.setOnClickPendingIntent(R.id.ibListFirst, getScrollToPendingIntent(WidgetService.LISTVIEW_SCROLL_TO_FIRST));	
		mRemoteViews.setOnClickPendingIntent(R.id.ibListLast, getScrollToPendingIntent(WidgetService.LISTVIEW_SCROLL_TO_LAST));	
	}
	
	public void setPageListScrollPosition(short scrollTo) {
		if(page_list == null){
			initPageList();
		}
		if(scrollTo == WidgetService.LISTVIEW_SCROLL_TO_LAST){			
			if(page_list != null){ /* avoid initial faild*/
				this.mPageListScrollPosition = page_list.size();
			}
		}
		else{
			this.mPageListScrollPosition = 0;
		}
	}
	
	private void initPageList(){
		mBookCase = BookCase.getInstance(mContext);
		mNotebook = mBookCase.getNoteBook(mBookId);					
		if(mNotebook != null)
		{
			page_list = mNotebook.getPageOrderList();
		}
	}
	
	public Boolean updatePageListScrollPosition(RemoteViews remoteViews){
		if(mPageListScrollPosition == -1){
			return false;
		}
		int gridId = R.id.GV1;
		remoteViews.setScrollPosition(gridId, mPageListScrollPosition);
		return true;
	}
	//End Allen
}