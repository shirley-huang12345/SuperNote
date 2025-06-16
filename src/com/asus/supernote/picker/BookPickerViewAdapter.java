package com.asus.supernote.picker;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class BookPickerViewAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private Cursor mCursor = null;//RICHARD INIT AS NULL
	private Context mContext;
	private BookCase mBookcase;	 
	public ArrayList<Boolean> mOriginalCheckBox; 
	public ArrayList<Boolean> mCheckBox; 
	public ArrayList<Long> mBookId; 
	private BookPickerView mActivity;
	//begin smilefish
	private Bitmap mPhoneIcon;
	private Bitmap mPadIcon;
	private Bitmap mSyncIcon; 
	private Bitmap mSyncPadIcon;
	private DataCounterListener mDataCounterListener;
	//end smilefish
		
	//emmanual
	private Map<Long, Bitmap> mapCoverThumbnail;
	private Map<NoteBook, Bitmap> mapNoCloudCover;
	private Map<Integer, BookInfo> mStringInfoMap;
	
	public BookPickerViewAdapter(Context context) {
		this.mInflater = LayoutInflater.from(context);		
		mContext = context.getApplicationContext();
		mActivity = (BookPickerView)context;
		mOriginalCheckBox = new ArrayList<Boolean>();
		mCheckBox = new ArrayList<Boolean>();
		mBookId = new ArrayList<Long>();
		 
		//begin smilefish
			mSyncIcon  = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_supernote_cover_phone);
			mSyncPadIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_supernote_cover_pad);
			mPhoneIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_supernote_cover_phone);
			mPadIcon   = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_supernote_cover_pad);
		//end smilefish
		
		dataChange();
		
		//emmanual
		mapCoverThumbnail = new HashMap<Long, Bitmap>();
		mapNoCloudCover = new HashMap<NoteBook, Bitmap>();
		mStringInfoMap = new HashMap<Integer, BookInfo>();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mCursor.getCount();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		mCursor.moveToPosition(position);
        Long bookId = mCursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
        return mBookcase.getNoteBook(bookId);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	//begin smilefish
	public void registerDataCounterListener(DataCounterListener listener) {
		if (mDataCounterListener != null) {
			removeDataCounterListener();
		}
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
	
	public int getSelectedCount(){
		int count = 0;
		for(int i=0;i<mCheckBox.size();i++)
		{
			if(mCheckBox.get(i))
				count++;
		}
		return count;
	}
	//end smilefish
	
	public void setSelectAll(boolean ischeck)//smilefish
	{
		if(ischeck)
		{
			for(int i=0;i<mCheckBox.size();i++)
			{
				mCheckBox.set(i, true);
			}
			selectedDataChange(mCheckBox.size());
		}
		else
		{
			for(int i=0;i<mCheckBox.size();i++)
			{
				mCheckBox.set(i, false);
			}
			selectedDataChange(0);
		}
		this.notifyDataSetChanged();

	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
			//emmanual
			BookInfo info = getStringInfo(position);
			String data = info.date;
			Boolean Enable = mCheckBox.get(position);
			String name = info.name;
			
			ViewHolder holder = null;
			if (convertView == null) {

				holder = new ViewHolder();

				convertView = mInflater.inflate(R.layout.bookpicker_item,
						null);
				holder.bookCheck = (ImageView) convertView
						.findViewById(R.id.book_check);
				holder.name = (TextView) convertView
						.findViewById(R.id.book_name);
				holder.date = (TextView) convertView
						.findViewById(R.id.book_time);
				holder.cover = (ImageView) convertView.findViewById(R.id.book_cover);
				holder.thumbnail = (ImageView) convertView.findViewById(R.id.book_thumbnail);
				holder.default_cover = (ImageView) convertView.findViewById(R.id.book_default_cover);
				//beign smilefish
					holder.pagePadOrPhone = (ImageView) convertView.findViewById(R.id.book_pad_or_phone);
					
				holder.pagePrivate = (ImageView) convertView.findViewById(R.id.page_private);
				holder.pageCloud = (ImageView) convertView.findViewById(R.id.page_cloud); //smilefish
				//end smilefish
				holder.bookCheck.setOnClickListener(listener);
				convertView.setOnClickListener(pageSelectListener); //smilefish
				convertView.setTag(holder);

			} else {

				holder = (ViewHolder) convertView.getTag();
			}
 
			holder.pagePrivate.setVisibility(info.pagePrivateVisibility);  
			holder.pagePadOrPhone.setImageBitmap(info.padOrPhone);
			holder.cover.setImageBitmap(info.cover);
			holder.pageCloud.setVisibility(info.pageCloudVisibility);
			holder.thumbnail.setImageBitmap(info.thumbnail);
			holder.default_cover.setImageBitmap(info.defaultCover);
			
			holder.bookCheck.setTag(position);
			if(Enable)
				holder.bookCheck.setVisibility(View.VISIBLE);
			else
				holder.bookCheck.setVisibility(View.GONE);
			holder.name.setText(name);
			holder.date.setText(data);

			return convertView;
	}
	
	//emmanual: save the images that consume much time
	private Bitmap getNoteBookCoverThumbnail(long id){
		if(mapCoverThumbnail.containsKey(id)){
			return mapCoverThumbnail.get(id);
		}else{
			Bitmap bitmap = NotePage.getNoteBookCoverThumbnail(id);
			mapCoverThumbnail.put(id, bitmap);
			return bitmap;
		}
	}

	private Bitmap getNoCloudCover(NoteBook notebook){
		if(mapNoCloudCover.containsKey(notebook)){
			return mapNoCloudCover.get(notebook);
		}else{
			Bitmap bitmap = NotePage.getNoCloudCover(notebook, mContext);
			mapNoCloudCover.put(notebook, bitmap);
			return bitmap;
		}
	}
	//end emmanual	
	
	public View.OnClickListener listener = new View.OnClickListener()
	{

		@Override
        public void onClick(View v) {
			int index = (Integer)(v.getTag());
			boolean isChecked = (v.getVisibility() == View.GONE);
			mCheckBox.set(index, isChecked);
			selectedDataChange(getSelectedCount());//smilefish
			notifyDataSetChanged();
        }
		
	};
	
	public View.OnClickListener pageSelectListener = new View.OnClickListener()
	{

		@Override
        public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			int index = (Integer)(holder.bookCheck.getTag());
			boolean isChecked = (holder.bookCheck.getVisibility() == View.GONE);
			mCheckBox.set(index, isChecked);
			selectedDataChange(getSelectedCount());//smilefish
			notifyDataSetChanged();
        }
		
	};
	
	public void dataChange()
	{
		boolean islocked = mActivity.mislocked;
		mOriginalCheckBox.clear();
		mCheckBox.clear();
		mBookId.clear();
		mBookcase = BookCase.getInstance(mContext);
		String selection = islocked?"(is_locked = 0) AND ((userAccount = 0) OR (userAccount = ?))":"((userAccount = 0) OR (userAccount = ?))";        
        //BEGIN: RICHARD
		if(mCursor != null)
		{
			mCursor.close();
		}
		//END: RICHARD
		mCursor = mContext.getContentResolver().query(MetaData.BookTable.uri, null, selection, new String[]{Long.toString(MetaData.CurUserAccount)}, "title");
        for(int i=0;i<mCursor.getCount() ;i++)
        {
        	mCursor.moveToPosition(i);        
    		int IsEnable_index = mCursor.getColumnIndex(MetaData.BookTable.USER_ACCOUNT);
    		Long IsEnable = mCursor.getLong(IsEnable_index);   		
    		Boolean Enable = (IsEnable == 0L)?false:true;
    		mOriginalCheckBox.add(i, Enable);
    		mCheckBox.add(i, Enable);
    		int id_index = mCursor
					.getColumnIndex(MetaData.BookTable.CREATED_DATE);
			Long id = mCursor.getLong(id_index);

			mBookId.add(i, id);
			
        }
        this.notifyDataSetChanged();
	}

	public void closeCursor(){
		if(mCursor != null)
			mCursor.close();
	}

public final class ViewHolder {
	public ImageView bookCheck;//smilefish
	public TextView name;
	public TextView date;	
	public ImageView thumbnail;
	public ImageView cover;
	public ImageView default_cover;
	ImageView pagePadOrPhone;//smilefish
	ImageView pagePrivate;//smilefish
	ImageView pageCloud; //smilefish
}

	//emmanual
	public class BookInfo {
		public Long id;
		public String date;
		public String name;
		public int pagePrivateVisibility;
		public int pageCloudVisibility;
		
		public Bitmap belt;
		public Bitmap padOrPhone;
		public Bitmap cover;
		public Bitmap pageCloud;
		public Bitmap thumbnail;
		public Bitmap defaultCover;		
	}

	//emmanual
	private BookInfo getStringInfo(int position) {
		if (mStringInfoMap.get(position) != null) {
			return mStringInfoMap.get(position);
		} else {
			BookInfo info = new BookInfo();
			mCursor.moveToPosition(position);
			int id_index = mCursor
			        .getColumnIndex(MetaData.BookTable.CREATED_DATE);
			Long id = mCursor.getLong(id_index);
			info.id = id;
			Date mDate = new Date();

			mDate.setTime(System.currentTimeMillis());
			Cursor modCursor = mContext.getContentResolver().query(
			        MetaData.BookTable.uri,
			        new String[] { MetaData.BookTable.MODIFIED_DATE },
			        "created_date = ?", new String[] { Long.toString(id) },
			        null);
			if (modCursor.moveToFirst()) {
				long modTime = modCursor.getLong(0);
				mDate.setTime(modTime);
			}
			if(modCursor != null)
				modCursor.close();
			
			java.text.DateFormat dateFormat = DateFormat.getDateFormat(mContext); // smilefish
			info.date = dateFormat.format(mDate);
			int name_index = mCursor.getColumnIndex(MetaData.BookTable.TITLE);
			info.name = mCursor.getString(name_index);
			
			NoteBook notebook = mBookcase.getNoteBook(id);			  
			if(notebook == null){//emmanual to fix bug 474951
				return info;
			}
			int userid = mCursor.getColumnIndex(MetaData.BookTable.USER_ACCOUNT);
			Long useAccountid = 0L;
			if (userid != -1) {
				useAccountid = mCursor.getLong(userid);
			}
			
			if(notebook.getIsLocked())
			{
				info.pagePrivateVisibility = View.VISIBLE;
			}
			else
			{
				info.pagePrivateVisibility = View.GONE;
			}
            
			if(useAccountid > 0)
			{
				info.padOrPhone = notebook.getPageSize() == MetaData.PAGE_SIZE_PHONE ? mSyncIcon : mSyncPadIcon;
				info.cover = NotePage.getCloudCover(notebook, mContext);
				info.pageCloudVisibility = View.VISIBLE;
			}
			else
			{
				info.padOrPhone = notebook.getPageSize() == MetaData.PAGE_SIZE_PHONE ? mPhoneIcon : mPadIcon;
				info.cover = getNoCloudCover(notebook);
				info.pageCloudVisibility = View.GONE;
			}
			if(notebook.getCoverIndex() == 0)
			{
				info.thumbnail = getNoteBookCoverThumbnail(id);
				info.defaultCover = null;
			}
			else if(notebook.getCoverIndex() == 1)
			{
				info.cover = NotePage.getNoteBookDefaultCoverThumbnail(id);
				info.defaultCover = NoteBook.getNoteBookCover(notebook, mContext);
				info.thumbnail = null;
			}
			else
			{
				info.cover = NoteBook.getNoteBookCover(notebook, mContext);
				info.thumbnail = null;
				info.defaultCover = null;
			}
			
			
			mStringInfoMap.put(position, info);
			return info;
		}
	}


}
