package com.asus.supernote.widget;

import android.app.Activity;
import java.util.ArrayList;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;

public class WidgetItemStoreAdapter extends BaseAdapter {
	public static final String TAG = "PageGridViewAdapter";
	private  int mMinItemNum = 1;
	private Context mContext;
	private BookCase mBookcase;
	private boolean mIsEditMode;
	public static final int NO_SELECTED = -1;
	private Cursor mCursor = null;
	private Resources mResources;

   private  int mWidgetID;
   private int mClickItemId;
   private long mBookId;
   private int mAddReplaceFlag;
   private int mDialogBookItemWidth;
   private int mDialogBookItemHeight;
   boolean widgetMode;
   ArrayList<Long> itemModes;
    //end:clare

	public WidgetItemStoreAdapter(Context context,int WidgetID,int ClickItemId,Long BookId,int ADD_REPLACE_FLAG,ArrayList<Long> arrModes) {
		mContext = context;
		this.mWidgetID=WidgetID;
		this.mBookId=BookId;
		mAddReplaceFlag=ADD_REPLACE_FLAG;
		mClickItemId=ClickItemId;
		mBookcase = BookCase.getInstance(mContext);
		String selection = WidgetService.NOT_SHOW_LOCK_BOOK ? "(is_locked = 0) AND ((userAccount = 0) OR (userAccount = ?))"
				: "((userAccount = 0) OR (userAccount = ?))";

		mCursor = mContext.getContentResolver().query(MetaData.BookTable.uri,
				null, selection,
				new String[] { Long.toString(MetaData.CurUserAccount) },
				"title");
		
		mResources = mContext.getResources();	
	
		mDialogBookItemWidth=mContext.getResources().getDimensionPixelSize(R.dimen.widget_dialog_book_width);
		mDialogBookItemHeight=mContext.getResources().getDimensionPixelSize(R.dimen.widget_dialog_book_height);
		
        if(mAddReplaceFlag==WidgetService.add_from_blank_item){
    	   mMinItemNum=1;
        }
        else if(mAddReplaceFlag==WidgetService.replace_from_book_item){
        	 mMinItemNum=2;
        }
        else if(mAddReplaceFlag==WidgetService.replace_from_book_template_item){
        	mMinItemNum=1;
        }
        else{} 
        
        if(arrModes.size()==2)
        {
        	widgetMode = true;
        	itemModes = arrModes;
        }
        else
        {
        	widgetMode = false;
        }
	}
	
	@Override
	public int getCount() {
		return mCursor.getCount()+mMinItemNum;
	}

	@Override
	public Object getItem(int position) {
		mCursor.moveToPosition(position - mMinItemNum);
		Long bookId = mCursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
		return mBookcase.getNoteBook(bookId);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder holder;           
		if(((Activity)mContext).isFinishing() 
				//emmanual to fix bug 453579
				&& convertView != null)
		{
			return convertView;
		}
        if (position == 0) {
            view = View.inflate(mContext, R.layout.widget_notebook_add_item, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.page_cover);           
            int imageId=0;
            if(mAddReplaceFlag==WidgetService.add_from_blank_item){
            	imageId=R.drawable.asus_l_popup_all;
            	view.findViewById(R.id.page_remove_tip).setVisibility(View.INVISIBLE);
            }
            else if(mAddReplaceFlag==WidgetService.replace_from_book_item){	
            	imageId=R.drawable.asus_l_popup_remove; 
            	view.findViewById(R.id.page_remove_tip).setVisibility(View.VISIBLE);
            }
            else if(mAddReplaceFlag==WidgetService.replace_from_book_template_item){
            	imageId=R.drawable.asus_l_popup_remove;
            	view.findViewById(R.id.page_remove_tip).setVisibility(View.VISIBLE);
            }
            else{}
            imageView.setImageDrawable(mContext.getResources().getDrawable(imageId));
            
			view.findViewById(R.id.page_cover).setAlpha(1.0f);
            view.findViewById(R.id.page_add_icon).setAlpha(0);
            view.findViewById(R.id.page_add_text).setAlpha(1.0f);
            view.findViewById(R.id.add_notebook_cover_mask).setEnabled(true);
            
            if(mAddReplaceFlag==WidgetService.add_from_blank_item){
            	TextView book_template_name=(TextView)view.findViewById(R.id.page_add_text);
            	book_template_name.setText(R.string.widget_all_book_template);         
				//add tradition to adjust
            	if(widgetMode &&(itemModes.get(0)==1 || itemModes.get(1)==1))
            	{
            		view.findViewById(R.id.page_add_icon).setAlpha(1.0f);
            		((ImageView)view.findViewById(R.id.page_add_icon)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.asus_l_popup_check));
            	}
            	else
            	{
            		view.findViewById(R.id.add_notebook_cover_mask).setOnClickListener(blankAddBookTemplateListener); 
            	}

            }
            else if(mAddReplaceFlag==WidgetService.replace_from_book_item){            	
                view.findViewById(R.id.add_notebook_cover_mask).setOnClickListener(removeBookClickListener);
            }
            else if(mAddReplaceFlag==WidgetService.replace_from_book_template_item){           	
            	view.findViewById(R.id.add_notebook_cover_mask).setOnClickListener(removeBookTemplateListener);
            }          
		}
        else if(position == 1 && mAddReplaceFlag==WidgetService.replace_from_book_item)
   		{
				view = View.inflate(mContext, R.layout.widget_notebook_add_item, null);
				ImageView imageView = (ImageView) view.findViewById(R.id.page_cover);
				imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.asus_l_popup_all));
				float alpha0 = mIsEditMode ? 0.3f : 1.0f;
				boolean enabled = mIsEditMode ? false : true;
				view.findViewById(R.id.page_remove_tip).setVisibility(View.INVISIBLE);
				view.findViewById(R.id.page_cover).setAlpha(alpha0);
				view.findViewById(R.id.page_add_icon).setAlpha(0);			
				view.findViewById(R.id.page_add_text).setAlpha(alpha0);
				TextView book_template_name=(TextView)view.findViewById(R.id.page_add_text);
	            book_template_name.setText(R.string.widget_all_book_template);// My notebooks and templates");
				view.findViewById(R.id.add_notebook_cover_mask).setEnabled(enabled);
				//add tradition to adjust
				if(widgetMode &&(itemModes.get(0)==1 || itemModes.get(1)==1))
				{
					view.findViewById(R.id.page_add_icon).setAlpha(1.0f);
            		((ImageView)view.findViewById(R.id.page_add_icon)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.asus_l_popup_check));
				}
				else
				{
					view.findViewById(R.id.add_notebook_cover_mask).setOnClickListener(book2BookTemplateListener);
				}
		}
        else
		{
			mCursor.moveToPosition(position - mMinItemNum);
			int id = mCursor.getColumnIndex(MetaData.BookTable.USER_ACCOUNT);
			Long useAccountid = 0L;
			if (id != -1) {
				useAccountid = mCursor.getLong(id);
			}
			if (convertView == null || convertView.getTag() == null) {
				view = View.inflate(mContext, R.layout.widget_notebook_item, null);
				holder = new ViewHolder();
				holder.pageCover = (ImageView) view.findViewById(R.id.page_cover);
				holder.pageCoverMask = (ImageView) view.findViewById(R.id.page_cover_mask);
				holder.itemName = (TextView) view.findViewById(R.id.page_count);			
				holder.notebookCover = (ImageView) view.findViewById(R.id.notebook_cover);
				holder.pagePrivate = (ImageView) view.findViewById(R.id.page_private);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}
			
			Long bookId = mCursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
			NoteBook notebook = mBookcase.getNoteBook(bookId);
			if (notebook != null) {								
				holder.notebookCover.setImageBitmap(NotePage.getNoteBookUsingCover(notebook,mContext,mDialogBookItemWidth,mDialogBookItemHeight));				
				holder.pageCoverMask.setTag(view);	
				holder.pageCoverMask.setEnabled(true);

				setClickEvent(itemModes,bookId,holder);//Siupo
				holder.pageCover.setTag(Integer.valueOf(position - mMinItemNum));			
				holder.pageCover.setScaleType(ScaleType.FIT_XY);								
	            Cursor modCursor = mContext.getContentResolver().query(MetaData.BookTable.uri, 
						new String[] {MetaData.BookTable.MODIFIED_DATE}, "created_date = ?",
						new String[] { Long.toString(bookId) }, null);
				modCursor.moveToFirst();
				modCursor.getLong(0);
				modCursor.close();

				holder.itemName.setTag(bookId);
				holder.itemName.setText( notebook.getTitle() );
				setBookCoverAppearance(holder, useAccountid, notebook);//clare
			
				float alpha = 1.0f;
				if(notebook.getIsLocked()){
					holder.pagePrivate.setVisibility(View.VISIBLE);
				}
				else{
					holder.pagePrivate.setVisibility(View.GONE);
				}				
				view.findViewById(R.id.page_cover).setAlpha(alpha);
				view.findViewById(R.id.page_count).setAlpha(alpha);
			}
		}
        return view;
	}
	public void setBookCoverAppearance(ViewHolder holder, Long useAccountid,
			NoteBook notebook) {
		if (useAccountid > 0) {
			if(notebook.getBookColor() == MetaData.BOOK_COLOR_YELLOW)
			{
				if(notebook.getGridType() == MetaData.BOOK_GRID_GRID)
				{
					holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(
							mResources, R.drawable.asus_supernote_cover2_grid_yellow_cloud));
				}						
				else if ( notebook.getGridType() == MetaData.BOOK_GRID_BLANK) {
					holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(
							mResources, R.drawable.asus_supernote_cover2_blank_yellow_local));
				}
				else
				{
					holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(
							mResources, R.drawable.asus_supernote_cover2_line_yellow_cloud));
				}
			}
			else
			{
				if(notebook.getGridType() == MetaData.BOOK_GRID_GRID)
				{
					holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(
							mResources, R.drawable.asus_supernote_cover2_grid_white_cloud));
				}						
				else if ( notebook.getGridType() == MetaData.BOOK_GRID_BLANK) {
					holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(
							mResources, R.drawable.asus_supernote_cover2_blank_white_local));
				}						
				else
				{
					holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(
							mResources, R.drawable.asus_supernote_cover2_line_white_cloud));
				}
			}
		}
		else
		{
			if(notebook.getBookColor() == MetaData.BOOK_COLOR_YELLOW)
			{
				if(notebook.getGridType() == MetaData.BOOK_GRID_GRID)
				{
			holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(
							mResources, R.drawable.asus_supernote_cover2_grid_yellow_local));
				}						
				else if ( notebook.getGridType() == MetaData.BOOK_GRID_BLANK) {
					holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(
							mResources, R.drawable.asus_supernote_cover2_blank_yellow_local));
				}						
				else
				{
					holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(
							mResources, R.drawable.asus_supernote_cover2_line_yellow_local));
				}
			}
			else
			{
				if(notebook.getGridType() == MetaData.BOOK_GRID_GRID)
				{
					holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(
							mResources, R.drawable.asus_supernote_cover2_grid_white_local));
				}						
				else if ( notebook.getGridType() == MetaData.BOOK_GRID_BLANK) {
					holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(
							mResources, R.drawable.asus_supernote_cover2_blank_white_local));
				}						
				else
				{
					holder.pageCover.setImageBitmap(BitmapFactory.decodeResource(
							mResources, R.drawable.asus_supernote_cover2_line_white_local));
				}
			}

		}
	}


	private static class ViewHolder {
		ImageView pageCover;
		ImageView pageCoverMask;
		TextView itemName;
		ImageView pagePrivate;
		ImageView notebookCover;		
	}
	

    private final OnClickListener blankAddBookTemplateListener = new OnClickListener() {
        @Override
        public void onClick(View v) {    	
        	//Begin Siupo
    		try
    		{
				Intent intent = new Intent(mContext, WidgetService.class);									
					intent.setAction(WidgetService.ALLNOTEBOOK_TEMPLATE_ACTION);
					intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,mWidgetID);
					intent.putExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, mClickItemId); 	
					intent.putExtra(WidgetService.SUBACTION, WidgetService.ALLNOTEBOOK_TEMPLATE_SUBACTION_SHOWMYBOOK);//默认可以修改为1									
				mContext.startService(intent);	
			    
				Log.v(TAG, "widget_maskClickListener");
			} catch (ActivityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		((Activity)mContext).finish();//Allen
    		Log.v(TAG, "widget_addNotebookTemplateListener");
        	//End Siupo
        }
    };
    
    private final OnClickListener removeBookClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {     	
        	Intent intent = new Intent(mContext, WidgetService.class);									
			intent.setAction(WidgetService.MODE_CHANGE_ACTION);
			intent.putExtra(WidgetService.SUBACTION, WidgetService.MODE_CHANGE_BOOK_TO_BLANK);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetID);
			intent.putExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, mClickItemId); 	
												
		mContext.startService(intent);	
		((Activity)mContext).finish();//Allen
        }
    };
    
    private final OnClickListener book2BookTemplateListener = new OnClickListener() {
        @Override
        public void onClick(View v) {    	
        	//Begin Siupo
    		try
    		{
				Intent intent = new Intent(mContext, WidgetService.class);									
					intent.setAction(WidgetService.ALLNOTEBOOK_TEMPLATE_ACTION);
					intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetID);
					intent.putExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, mClickItemId); 	
					intent.putExtra(WidgetService.SUBACTION, WidgetService.ALLNOTEBOOK_TEMPLATE_SUBACTION_SHOWMYBOOK);//默认可以修改为1									
				mContext.startService(intent);		        
				Log.v(TAG, "widget_maskClickListener");
			} catch (ActivityNotFoundException e) {
				// TODO Auto-generated catch block			
					e.printStackTrace();
			}

    		((Activity)mContext).finish();//Allen
    		Log.v(TAG, "widget_addNotebookTemplateListener");
        	//End Siupo
        }
    };
    private final OnClickListener removeBookTemplateListener = new OnClickListener() {
        @Override
        public void onClick(View v) {      	
        	MetaData.WIDGET_ONE_COUNT = 0;
        	Intent intent = new Intent(mContext, WidgetService.class);									
			intent.setAction(WidgetService.MODE_CHANGE_ACTION);
			intent.putExtra(WidgetService.SUBACTION, WidgetService.MODE_CHANGE_BOOK_TO_BLANK);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetID);
			intent.putExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, mClickItemId); 	 
			   mContext.startService(intent);
			   ((Activity)mContext).finish();//Allen
        }
    };
	private final View.OnClickListener maskClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			View view = (View) v.getTag();
			 //get bookId				
			ViewHolder holder = (ViewHolder) view.getTag();				
			long bookId = (Long) holder.itemName.getTag();	
			try {				
				 Intent intent = new Intent(mContext, WidgetService.class);				 
				intent.setAction(WidgetService.ADDBOOK_ACTION);				
				// get clicked ID					
					intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetID);
					intent.putExtra(WidgetService.WIDGET_ITEM_ID_INTENT_KEY, mClickItemId); 		
					intent.putExtra(WidgetService.WIDGET_BOOK_ID_INTENT_KEY, bookId);  			
				//notify service update				
		        mContext.startService(intent);		        		  
				Log.v(TAG, "widget_maskClickListener");
			} catch (ActivityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			((Activity)mContext).finish();//Allen
		}
	};
	public void releaseMemory()
	{
		if(mCursor != null)
		{
			mCursor.close();
			mCursor=null;
		}
	}

	public void setClickEvent(ArrayList<Long> items,long bookId,ViewHolder holder)
	{
		if(widgetMode)
		{
			if((bookId==mBookId && mAddReplaceFlag==WidgetService.replace_from_book_item )
				||bookId==items.get(0)||bookId==items.get(1))
			{
				holder.pageCoverMask.setImageDrawable(mContext.getResources().getDrawable(R.drawable.asus_l_popup_check));
			    Log.v("BookID", "mBookId:"+String.valueOf(mBookId)+"\n"+" get(0):"+String.valueOf(items.get(0))+
			    		"\n"+" get(1):"+String.valueOf(items.get(1))+"\n"+"bookId:"+String.valueOf(bookId)+"\n");
			}
			else
			{	
				holder.pageCoverMask.setImageDrawable(null);
				holder.pageCoverMask.setOnClickListener( maskClickListener);
			}
		}
		else
		{
			if(bookId==mBookId && mAddReplaceFlag==WidgetService.replace_from_book_item )
			{
				holder.pageCoverMask.setImageDrawable(mContext.getResources().getDrawable(R.drawable.asus_l_popup_check));
			}
			else
			{	
				holder.pageCoverMask.setImageDrawable(null);
				holder.pageCoverMask.setOnClickListener( maskClickListener);
			}
		}
	}
}
