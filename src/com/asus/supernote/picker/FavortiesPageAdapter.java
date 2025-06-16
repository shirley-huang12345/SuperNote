package com.asus.supernote.picker;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.ui.CoverHelper;

public class FavortiesPageAdapter extends BaseAdapter {
    public static final String TAG = "FavortiesPageAdapter";
    private Context mContext;
    private Date mDate;
    private java.text.DateFormat mTimeFormat;
    private java.text.DateFormat mDateFormat;
    private Cursor mCursor = null;//RICHARD INIT AS NULL
    private BookCase mBookcase;
    private Bitmap mconver_phone;
    private Bitmap mconver_pad;
    //begin:clare
    private DataCounterListener mDataCounterListener;
    //end:clare
    // BEGIN: Shane_Wang@asus.com 2013-1-7
    Intent mIntent = null;
    // END: Shane_Wang@asus.com 2013-1-7
    
    public void setDateAndTimeFormat(){
        mTimeFormat = DateFormat.getTimeFormat(mContext);
        mDateFormat = DateFormat.getDateFormat(mContext);//smilefish
    }
    
    public FavortiesPageAdapter(Activity activity) {
        mContext = activity;
        mDate = new Date();
        setDateAndTimeFormat();
        try {
			Intent intent = activity.getIntent();
			mIntent = intent;
			Long bookId = intent.getLongExtra(MetaData.BOOK_ID, 0L);
			mCursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null, "is_bookmark > 0 AND owner = ?", new String[] { bookId.toString() }, null);
			mBookcase = BookCase.getInstance(mContext);
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.page_item, null);
            holder = new ViewHolder();
            holder.pageCover = (ImageView) convertView.findViewById(R.id.page_cover);
            holder.pageCoverMask = (ImageView) convertView.findViewById(R.id.page_cover_mask);
            holder.pageNumber = (TextView) convertView.findViewById(R.id.page_number);
            holder.pageTime = (TextView) convertView.findViewById(R.id.page_time);
            holder.pageDate = (TextView) convertView.findViewById(R.id.page_date);
            holder.pageChecked = (ImageView) convertView.findViewById(R.id.page_check);
            
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        mCursor.moveToPosition(position);
        long bookId = mCursor.getLong(MetaData.PageTable.INDEX_OWNER);
        long pageId = mCursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE);

        mDate.setTime(mCursor.getLong(MetaData.PageTable.INDEX_MODIFIED_DATE));//darwin
        NoteBook notebook = mBookcase.getNoteBook(bookId);
        Drawable d = holder.pageCover.getDrawable();
        if (d != null && d instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) holder.pageCover.getDrawable();
            if (bd != null) {
                Bitmap b = bd.getBitmap();
                if (b != null && b.isRecycled() == false && !b.equals(mconver_pad) && !b.equals(mconver_phone)) {
                    b.recycle();
                    b = null;
                }

            }
        }

        Bitmap cover = NotePage.getThumbnail(bookId, pageId);
        if (cover == null) {
            // BEGIN: archie_huang@asus.com
            int color = notebook.getBookColor();
            int line = notebook.getGridType();//notebook.getFontSize();
            cover = CoverHelper.getDefaultCoverBitmap(color, line,mContext.getResources());//Allen
//            // END: archie_huang@asus.com
        }
        holder.pageCover.setImageBitmap(cover);
        holder.pageCoverMask.setTag(R.string.book_id, bookId);
        holder.pageCoverMask.setTag(R.string.page_id, pageId);
        holder.pageNumber.setText("" + (notebook.getPageIndex(pageId) + 1));
        holder.pageTime.setText(mTimeFormat.format(mDate));
        holder.pageDate.setText(mDateFormat.format(mDate));
        holder.pageChecked.setVisibility(View.GONE);

        holder.pageCoverMask.setOnClickListener(onPageClickListener);
        holder.pageCoverMask.setOnLongClickListener(onBookmarkLongClickListener); // Better

        return convertView;
    }

    private View.OnClickListener onPageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // BEGIN: Better
        	//begin darwin
            long bookId = (Long) v.getTag(R.string.book_id);
            //end   darwin
            NoteBook book = mBookcase.getNoteBook(bookId);
            if (book != null) {
	            long pageId = (Long) v.getTag(R.string.page_id);
	            if (!MetaData.SavingPageIdList.contains(pageId)) {
		            NotePage page = book.getNotePage(pageId);
		            if (page != null) {
			        	
		            	//BEGIN: RICHARD
		            	try
		            	{
							Intent intent = new Intent(mContext, EditorActivity.class);
							intent.putExtra(MetaData.BOOK_ID, bookId);
							intent.putExtra(MetaData.PAGE_ID, pageId);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							mContext.startActivity(intent);
		            	}catch(Exception e)
		            	{
		            		e.printStackTrace();
		            	}
						//END: RICHARD
		            }
	            }
            }
        	// END: Better
        }
    };
    
    // BEGIN: Better
    private View.OnLongClickListener onBookmarkLongClickListener = new View.OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			final Long bookId = (Long) v.getTag(R.string.book_id);
			NoteBook book = mBookcase.getNoteBook(bookId);
            if (book != null) {
	            Long pageId = (Long) v.getTag(R.string.page_id);
		        final NotePage page = book.getNotePage(pageId);
		        if (page != null) {
		        	//begin:clare
		        	 AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		             builder.setCancelable(false);
		             builder.setTitle(R.string.remove_from_jump);
		             builder.setMessage(R.string.remove_from_list2);
		             builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		                 @Override
		                 public void onClick(DialogInterface dialog, int which) {
		                	 page.setBookmarkByUser(false);
		                	 //BEGIN: RICHARD
		                	 if(mCursor != null)
		                	 {
		                		 mCursor.close();
		                	 }
		                	 //END: RICHARD
		 		        	mCursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null, "is_bookmark > 0 AND owner = ?", new String[] { bookId.toString() }, null);
		 		        	
		 		        	
		 		        	Log.v("Clare__________Adapter", "notifyDataSetChanged");	
		 		        	
		 		        	selectedDataChange(getCount());		        	
		 		        	
		 		        	FavortiesPageAdapter.this.notifyDataSetChanged();
		 		        	Toast.makeText(mContext, R.string.remove_from_list, Toast.LENGTH_LONG).show();
		 		        	
		                     }
		                   		                 
		             });
		             builder.setNegativeButton(android.R.string.cancel, null);
		             Dialog dialog = builder.create();
		             dialog.show();
		             return true;
		        	//end:clare
		        }
            }
			return false;
		}
		
	};
    // END: Better

	//begin:clare
	  public void registerDataCounterListener(DataCounterListener listener) {
	        mDataCounterListener = listener;
	    }

	    public void removeDataCounterListener() {
	        mDataCounterListener = null;
	    }

	    private void selectedDataChange(int count) {
	        if (mDataCounterListener != null) {
	            mDataCounterListener.onSelectedDataChange(count);
	        }
	    }
	//end:clare
    private static class ViewHolder {
        ImageView pageCover;
        ImageView pageCoverMask;
        TextView pageNumber;

        TextView pageDate;
        TextView pageTime;
        ImageView pageChecked; //smilefish
    }
    
    // BEGIN: Shane_Wang@asus.com 2013-1-7
    public void dataChange() {
    	if(mIntent != null) {
	    	Long bookId = mIntent.getLongExtra(MetaData.BOOK_ID, 0L);
	       	 //BEGIN: RICHARD
	       	 if(mCursor != null)
	       	 {
	       		 mCursor.close();
	       	 }
	       	 //END: RICHARD
	    	mCursor = mContext.getContentResolver().query(MetaData.PageTable.uri, null, "is_bookmark > 0 AND owner = ?", new String[] { bookId.toString() }, null);
    	}
    }
    // END: Shane_Wang@asus.com 2013-1-7
    
    public void closeCursor() { //smilefish fix memory leak
        if (mCursor != null) {
            mCursor.close();
        }
    }

}
