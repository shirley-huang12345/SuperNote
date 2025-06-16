package com.asus.supernote.widget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.asus.supernote.R;
import com.asus.supernote.SuperNoteApplication;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;


public class WidgetGridViewService extends RemoteViewsService
{
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		// TODO Auto-generated method stub
		Log.i("WidgetRemoteService", "000");
		return new GridViewRemoteViewsFactory(this.getApplicationContext(),intent);
	}
}
class GridViewRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
	private Context mContext;
	private Cursor mCursor = null;
	private int mBookCount = 0;
	private int mShowBook=-1;
	private long mBookId = -1;
	public final static int BITMAPWIDTH = 120;//scale the Bitmpa to 100px
	public final static int BITMAPHEIGHT = 150;//scale the Bitmpa to 120px
	public static boolean showLockedPage = false; 
	public static Bitmap lockedPageBitmap = null;
	List<Long> page_list;
	SharedPreferences sharePre;
	//Allen++ show all template book to add a new book	
	private  ArrayList<BookItem> mTemplateBookItems = new ArrayList<BookItem>();
	private int mDeviceType = -1;
	
	private class BookItem{
		public int imagesResources;
		public String bookStyle;
		public BookItem(int imagesResources, int bookStyle, int color){
			this.imagesResources = imagesResources;
			String colorCode="";
			switch(color){ //Carol- add color description
			case 1:
				colorCode=" ("+mContext.getResources().getString(R.string.white)+")";
				break;
			case 2:
				colorCode=" ("+mContext.getResources().getString(R.string.yellow)+")";
				break;
			}
			this.bookStyle = mContext.getResources().getString(bookStyle)+colorCode;
		}
	}
	//End Allen
	
	public GridViewRemoteViewsFactory(Context context,Intent intent)
	{
		mContext = context;
		mShowBook = intent.getIntExtra("mShowBook", -1);
		mBookId = intent.getLongExtra("BookID", -1);
		mDeviceType =  mContext.getResources().getInteger(R.integer.device_type);
	}
	
	@Override
	public void onCreate()
	{
		//Begin Allen
		BookItem item0 = new BookItem(R.drawable.asus_supernote_cover2_blank_white_local,R.string.book_type_blank,1);
		mTemplateBookItems.add(item0);
		//change template order [Carol]
		BookItem item1 = new BookItem(R.drawable.asus_supernote_cover2_line_white_local,R.string.book_type_line,1);
		mTemplateBookItems.add(item1);//2);
		BookItem item2 = new BookItem(R.drawable.asus_supernote_cover2_grid_white_local,R.string.book_type_grid,1);
		mTemplateBookItems.add(item2);//4);
		BookItem item3 = new BookItem(R.drawable.asus_supernote_cover2_blank_yellow_local,R.string.book_type_blank,2);
		mTemplateBookItems.add(item3);//1);
		BookItem item4 = new BookItem(R.drawable.asus_supernote_cover2_line_yellow_local,R.string.book_type_line,2);
		mTemplateBookItems.add(item4);//3);
		BookItem item5 = new BookItem(R.drawable.asus_supernote_cover2_grid_yellow_local,R.string.book_type_grid,2);
		mTemplateBookItems.add(item5);
		BookItem item6 = new BookItem(R.drawable.asus_supernote_template_meeting,R.string.book_type_meeting,0);
		mTemplateBookItems.add(item6);
		BookItem item7 = new BookItem(R.drawable.asus_supernote_template_diary,R.string.book_type_diary,0);
		mTemplateBookItems.add(item7);
		if(MetaData.IS_ENABLE_TODO_TEMPLATE){
			BookItem item8 = new BookItem(R.drawable.asus_supernote_template_memo,R.string.todo_title,0);
			mTemplateBookItems.add(item8);
		}
	//End Allen		
	}
	
	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub		
		return getDataCount();
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public RemoteViews getLoadingView()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position)
	{
		// TODO Auto-generated method stub
		Log.i("position", String.valueOf(position));
		
		RemoteViews rv = getItemRemoteViews();
		
		
		Intent fillInIntent = new Intent();
		
		if(position>=mBookCount)
		{
			return rv;
		}
			
		if(mShowBook==WidgetService.SHOWBOOK)
		{
			if( mDeviceType >100 && position==0){
				rv = new RemoteViews(mContext.getPackageName(),R.layout.widget_tab_allbook_item_add);
				Bundle dataExtras = new Bundle();
				dataExtras.putInt("Type", WidgetService.ITEMCLICK_NEWBOOK);//add_from_blank_item);
				dataExtras.putInt("BookStyle", position);
				fillInIntent.putExtras(dataExtras);
			}else{
				if(mDeviceType > 100)
					position --;
				mCursor.moveToPosition(position);
				long bookId = mCursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
				BookCase bookcase = BookCase.getInstance(mContext);
				NoteBook notebook = bookcase.getNoteBook(bookId);
				if(notebook == null){//emmanual to fix bug 474950
					return rv;
				}
				Log.i("position_SHOWBOOK", String.valueOf(position));
				String bookName = notebook.getTitle();
				rv.setTextViewText(R.id.text_item, bookName);
				//add created date & notebook type
				Bitmap mPhoneIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_supernote_cover_phone);
				Bitmap mPadIcon   = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_supernote_cover_pad);
				rv.setImageViewBitmap(R.id.type_mark, (notebook.getPageSize() == MetaData.PAGE_SIZE_PHONE) ? mPhoneIcon : mPadIcon);

				//begin smilefish fix bug 817660
				Date mDate = new Date();
				Cursor modCursor = mContext.getContentResolver().query(MetaData.BookTable.uri, 
						new String[] {MetaData.BookTable.MODIFIED_DATE}, "created_date = ?",
						new String[] { Long.toString(bookId) }, null);
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
				rv.setTextViewText(R.id.notebook_date, data);
				
				//read from cache if the thumbnail exist[Carol]
				if(MetaData.mCacheCoverThumbnailList != null && MetaData.mCacheCoverThumbnailList.containsKey(bookId)
						&& !MetaData.CoverChangedBookIdList.contains(bookId)){
					rv.setImageViewBitmap(R.id.image_item, MetaData.mCacheCoverThumbnailList.get(bookId));
				}else{
					try{
						//begin:clare
						int gridViewBookItemWidth=mContext.getResources().getDimensionPixelSize(R.dimen.widget_book_template_width);
						int gridViewBookItemHeight=mContext.getResources().getDimensionPixelSize(R.dimen.widget_book_template_height);
						Bitmap result = NotePage.getNoteBookUsingCover(notebook,mContext,gridViewBookItemWidth,gridViewBookItemHeight);

						MetaData.mCacheCoverThumbnailList.put(bookId, result); //Carol
						if(MetaData.mCacheCoverThumbnailList.size()>=16){
							MetaData.mCacheCoverThumbnailList.remove(0);
							MetaData.mCacheCoverThumbnailList.remove(0);
							MetaData.mCacheCoverThumbnailList.remove(0);
							MetaData.mCacheCoverThumbnailList.remove(0);
						}
						MetaData.CoverChangedBookIdList.remove(bookId);
						rv.setImageViewBitmap(R.id.image_item, result);
					}
					catch(Exception e)
					{
						Log.e("Loading Bitmap", "getViewAt");
					}
				}
	//			WidgetService.bookID = bookId;
				Bundle dataExtras = new Bundle();
				dataExtras.putInt("Type", WidgetService.ITEMCLICK_OPENBOOK);
				dataExtras.putLong("ID", bookId);
				fillInIntent.putExtras(dataExtras);
			}
		}
		else if(mShowBook==WidgetService.ADDTEMPLATE)
		{
			rv.setImageViewResource(R.id.image_item, mTemplateBookItems.get(position).imagesResources);
			rv.setTextViewText(R.id.text_item, mTemplateBookItems.get(position).bookStyle);
			Bundle dataExtras = new Bundle();
			dataExtras.putInt("Type", WidgetService.ITEMCLICK_NEWBOOK);
			dataExtras.putInt("ID", position);
			dataExtras.putInt("BookStyle", position);
			fillInIntent.putExtras(dataExtras);
		}
		else if(mShowBook==WidgetService.SHOWPAGE)
		{
			try
			{
				BookCase bookCase = BookCase.getInstance(mContext);
				NoteBook noteBook = bookCase.getNoteBook(mBookId);	
				page_list=noteBook.getPageOrderList();
				long pageId = page_list.get(position);
				int templateType = noteBook.getTemplate();
				float leftRatio = 0.0f;
				float topRatio = 0.0f;

				leftRatio = mContext.getResources().getDimension(R.dimen.pageLeftRatio);
				topRatio = mContext.getResources().getDimension(R.dimen.pageTopRatio);
				
				Bitmap pageBitmap = SingleWidgetItem.getBookOrPageCombineBitmap(mContext ,
						noteBook,
						SingleWidgetItem.getBookOrPageType(noteBook,SingleWidgetItem.PAD_PORT_PAGE_DRAWABLE_ID),
						leftRatio,topRatio,
						position);//Page			
				rv.setImageViewBitmap(R.id.image_item, pageBitmap);			
		
				java.text.DateFormat timeFormat = DateFormat.getTimeFormat(mContext);
				String dateTime1 = timeFormat.format(new Date(noteBook.getModifiedTime()));
				rv.setTextViewText(R.id.text_item_date, dateTime1);

				Bundle dataExtras = new Bundle();
				dataExtras.putInt("Type", WidgetService.ITEMCLICK_OPENPAGE);
				dataExtras.putLong("ID", mBookId);
				dataExtras.putLong("PageID", pageId);
				dataExtras.putInt("Template", templateType);
				fillInIntent.putExtras(dataExtras);
			}
			catch(Exception e)
			{
				e.getStackTrace();
				Log.v("WidgetGridViewService_Land","Page Information");
			}
		}
		else if(mShowBook == WidgetService.SHOWLOCKEDPAGE)
		{
			try
			{
				if(!showLockedPage)
				{
					showLockedPage = true;
					lockedPageBitmap = Bitmap.createBitmap(BITMAPWIDTH, BITMAPHEIGHT, Bitmap.Config.ARGB_8888);
					Canvas cs = new Canvas(lockedPageBitmap);
					Paint mBitmapPaint = new Paint();
				    mBitmapPaint.setDither(true);
				    mBitmapPaint.setAntiAlias(true);
				    
					Bitmap pageBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
							mContext.getResources(), R.drawable.asus_scroll_page), 
							BITMAPWIDTH, BITMAPHEIGHT, true);
					Bitmap lockBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
							mContext.getResources(), R.drawable.asus_page_lock), 
							BITMAPWIDTH/3, BITMAPHEIGHT/3, true);
					cs.drawBitmap(pageBitmap,0,0, mBitmapPaint);
					cs.drawBitmap(lockBitmap,BITMAPWIDTH/3,BITMAPHEIGHT/3, mBitmapPaint);
				}
			
				rv.setImageViewBitmap(R.id.image_item, lockedPageBitmap);
				rv.setTextViewText(R.id.text_item, String.valueOf(position+1));
				Bundle dataExtras = new Bundle();
				dataExtras.putInt("Type", WidgetService.ITEMCLICK_OPENLOCKEDPAGE);
				dataExtras.putLong("ID", mBookId);
				fillInIntent.putExtras(dataExtras);
			}
			catch(Exception e)
			{
				e.getStackTrace();
				Log.v("WidgetGridViewService_Land","LockedPage Information");
			}
		}
		else
		{
			Log.i("getViewAt()", String.valueOf(mShowBook));
		}

		rv.setOnClickFillInIntent(R.id.GridItem, fillInIntent);

		return rv;
	}
	
	@Override
	public int getViewTypeCount()
	{
		// TODO Auto-generated method stub
		return 2; //Allen 
	}

	@Override
	public boolean hasStableIds()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onDataSetChanged()
	{
//		 TODO Auto-generated method stub
	}

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
	}
	
	public int getDataCount()
	{
		int count = 0;
		mBookCount = count;
		try
		{
			if(mShowBook==WidgetService.SHOWBOOK)
			{
				if (mCursor != null) {
		            mCursor.close(); 
		            mCursor = null;
		        }
				
				if(MetaData.CurUserAccount == 0L)  //smilefish fix bug 556454
					BookCase.getInstance(mContext); 
				
				String selection = WidgetService.NOT_SHOW_LOCK_BOOK ? "(is_locked = 0) AND ((userAccount = 0) OR (userAccount = ?))"
						: "((userAccount = 0) OR (userAccount = ?))";

				mCursor = mContext.getContentResolver().query(MetaData.BookTable.uri,
						null, selection,
						new String[] { Long.toString(MetaData.CurUserAccount) },
						"title");
				
//				mCursor = mContext.getContentResolver().query(MetaData.BookTable.uri, null, null, null, null);
				count = mCursor.getCount();
				if(mDeviceType >100)
					mBookCount = ++count;
				else
					mBookCount = count;
			}
			else if(mShowBook == WidgetService.ADDTEMPLATE)
			{
				count = mTemplateBookItems.size();//Allen
				mBookCount = count;
			}
			else if(mShowBook==WidgetService.SHOWPAGE)
			{
				BookCase bookCase = BookCase.getInstance(mContext);
				NoteBook noteBook = bookCase.getNoteBook(mBookId);
				count = noteBook.getPageOrderList().size();
				mBookCount = count;
			}
			else if(mShowBook==WidgetService.SHOWLOCKEDPAGE)
			{
				BookCase bookCase = BookCase.getInstance(mContext);
				NoteBook noteBook = bookCase.getNoteBook(mBookId);
				count = noteBook.getPageOrderList().size();
				mBookCount = count;
			}
			else
			{
				//
			}
		}
		catch(Exception e)
		{
			count = 0;
			mBookCount = count;
		}

		return count;
	}
	public RemoteViews getItemRemoteViews()
	{
		RemoteViews rv;
		if(mShowBook==WidgetService.SHOWPAGE||mShowBook == WidgetService.SHOWLOCKEDPAGE)
		{
			rv = new RemoteViews(mContext.getPackageName(),R.layout.widget_allpage_item);
		}
		else if(mShowBook==WidgetService.SHOWBOOK)//||mShowBook==WidgetService.ADDTEMPLATE)
		{
			rv = new RemoteViews(mContext.getPackageName(),R.layout.widget_tab_allbook_item);
		}else if(mShowBook==WidgetService.ADDTEMPLATE)
		{
			rv = new RemoteViews(mContext.getPackageName(),R.layout.widget_tab_template_item);
		}else
		{
			rv = new RemoteViews(mContext.getPackageName(),R.layout.widget_allpage_item);
		}
		return rv;
	}
}
