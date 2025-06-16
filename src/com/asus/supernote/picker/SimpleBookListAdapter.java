package com.asus.supernote.picker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;

public class SimpleBookListAdapter extends BaseAdapter {
    private Context mContext;
    private Cursor mCursor = null;
    private final boolean mIsPrivate;
    private Bitmap mIcon;
    private View mEmptyView;
    private long mCurrBookId;

    //darwin
    public SimpleBookListAdapter(Context context, boolean isPrivate, boolean isXLarge, long currentBookId,int templateIndex) {
    	//darwin
        mContext = context;
        mIsPrivate = isPrivate;
        String selection = null;
        String template = null;
        if(templateIndex == MetaData.Template_type_normal || (templateIndex == MetaData.Template_type_blank))
        {
        	template = " and ((template = " + MetaData.Template_type_normal + ") OR (template = " + MetaData.Template_type_blank + "))";
        }
        else
        {
        	template = " and template = " + templateIndex;
        }
        if (mIsPrivate) {
		//modify by wendy begin
            if (isXLarge) {
                selection = "is_locked = 0 and book_size = 1" + template;
            }
            else {
                selection = "is_locked = 0 and book_size = 2" + template;
            }
        }
        else {
            if (isXLarge) {
                selection = "book_size = 1" + template;
            }
            else {
                selection = "book_size = 2" + template;
            }
        }
        selection = selection + " and ((userAccount = 0) OR (userAccount = ?))";//add by darwin
		//modify by wendy end
        mCursor = mContext.getContentResolver().query(MetaData.BookTable.uri, null, selection, new String[]{ Long.toString(MetaData.CurUserAccount)}, null);//modify by darwin
        mIcon = BitmapFactory.decodeResource(mContext.getResources(), (isXLarge) ? R.drawable.asus_supernote_cover_pad2 : R.drawable.asus_supernote_cover_phone2);
        mEmptyView = new View(mContext);
        mEmptyView.setVisibility(View.GONE);
        mCurrBookId = currentBookId;
    }
    
    //smilefish fix memory leak
    public void closeCursor() {
        if (mCursor != null) {
            mCursor.close();
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
        mCursor.moveToPosition(position);
        String title = mCursor.getString(MetaData.BookTable.INDEX_TITLE);
        long bookId = mCursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
        if (bookId == mCurrBookId) {
            return mEmptyView;
        }
        else {
            ViewHolder holder;
            if (convertView == null || convertView.getId() != R.layout.simple_book_item) {
                holder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.simple_book_item, null);
                holder.icon = (ImageView) convertView.findViewById(R.id.book_size);
                holder.title = (TextView) convertView.findViewById(R.id.book_title);
                holder.icon.setImageBitmap(mIcon);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.title.setText(title);
            holder.icon.setTag(bookId);
            return convertView;
        }

    }

    private class ViewHolder {
        public ImageView icon;
        public TextView title;
    }
}
