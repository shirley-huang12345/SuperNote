package com.asus.supernote.picker;

import java.io.File;
import java.util.ArrayList;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.editable.attacher.BitmapAttacher;




public class EditBookCoverAdapter extends BaseAdapter {
	public static final String TAG = "EditBookCoverAdapter";

	private Context mContext;

	public ArrayList<Long> mSelectedItems;

	public static final int NO_SELECTED = -1;
	private Resources mResources;

	private Bitmap mCoverDefault;
	private Bitmap mCoverChoose;
	
	private Bitmap mCover1;
	private Bitmap mCover2; 
	private Bitmap mCover3; 
	private Bitmap mCover4; 
	private Bitmap mCover5; 
	private Bitmap mCover6; 
	private Bitmap mCover7; 
	private Bitmap mCover8; 
	private Bitmap mCover9; 
	private Bitmap mCover10; 
	private Bitmap mCover11; 
	private Bitmap mCover12; 
	private Bitmap mCover13; 
	private Bitmap mCover14; 
	private Bitmap mCover15; 
	
	//begin by smilefish
	private Bitmap mCoverLast; //book cover from from camera or gallery
	private boolean bIsLastCoverExist = false; //is last book cover exist
	private GridView mParentView = null;
	//end
	
	private NoteBookPickerActivity mNoteBookPickerActivity;	
	public boolean bIsLock = false;

	public int mCheckIndex = -1;
	public int mCheckCoverIndex = -1;//emmanual to fix bug 466553
	public Dialog mDialog = null;
	public ArrayList<Bitmap> list = new ArrayList<Bitmap>();
	public NoteBook mBook;
	//darwin
	private boolean isNeedToSetSelection = false; //smilefish
	
	public EditBookCoverAdapter(Context context ,NoteBook book,GridView view,NoteBookPickerActivity picker) {
		mContext = context;//.getApplicationContext();
		mBook = book;
		mParentView = view; //smilefish
		mResources = mContext.getResources();

		mNoteBookPickerActivity = picker;
		
		mCoverDefault = NotePage.getNoteBookUsingCoverLgnoreCoverIndex( book,mContext,(int)mResources.getDimension(R.dimen.change_cover_crop_width),(int)mResources.getDimension(R.dimen.change_cover_crop_height));
		mCoverChoose  = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_add_photo );
		mCover1  = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover01 );
		mCover2  = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover02 );
		mCover3  = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover03 );
		mCover4  = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover04 );
		mCover5	 = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover05 );
		mCover6	 = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover06 );
		mCover7  = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover07 );
		mCover8  = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover08 );
		mCover9  = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover09 );
		mCover10 = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover10);
		mCover11 = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover11);
		mCover12 = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover12);
		mCover13 = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover13);
		mCover14 = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover14);
		mCover15 = BitmapFactory.decodeResource(mResources, R.drawable.asus_supernote_bookcover15);
		list.add(mCoverChoose); //modified by smilefish
		list.add(mCoverDefault);	
		list.add(mCover1);
		list.add(mCover2);
		list.add(mCover3);
		list.add(mCover4);
		list.add(mCover5);
		list.add(mCover6);
		list.add(mCover7);
		list.add(mCover8);
		list.add(mCover9);
		list.add(mCover10);
		list.add(mCover11);
		list.add(mCover12);
		list.add(mCover13);
		list.add(mCover14);
		list.add(mCover15);
		
		//begin by smilefish
		getLastCover();
		
		if(bIsLastCoverExist)
			list.add(mCoverLast);
		//end
		
		//begin by smilefish
		int index = mBook.getCoverIndex(); 
		if(index == 0)
		{
			mCheckIndex = 1;
		}
		else if(index == 1)
		{
			if(bIsLastCoverExist)
				mCheckIndex = list.size() - 1;
		}
		else
		{
			mCheckIndex = index;
		}
		mCheckCoverIndex = mCheckIndex;
		//end smilefish
	}
	
	//begin by smilefish
	private void getLastCover() //get last book cover from camera or gallery
	{
		File file = new File(MetaData.DATA_DIR + "/" + mBook.getCreatedTime(), mBook.getCreatedTime() + MetaData.THUMBNAIL_COVER_CROP);
		if(file.exists())
		{
			bIsLastCoverExist = true;
			BitmapFactory.Options option = new BitmapFactory.Options();
			Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), option);
			mCoverLast = NoteBookPickerActivity.setBitmapMask(bitmap, mCover1.getWidth(), mCover1.getHeight());		
		}
		else
			bIsLastCoverExist = false;
	}
	//end

	public void changeCoverFile(String path,String name) //save book cover from camera or gallery
	{
		File srcFile = new File(path,name);
    	File destCoverFile = new File(path + "/" + mBook.getCreatedTime(), mBook.getCreatedTime() + MetaData.THUMBNAIL_COVER_CROP);
    	BitmapAttacher.saveBitmap(srcFile, destCoverFile, Bitmap.CompressFormat.JPEG);
    	srcFile.delete();
	}
	
	public void changeCoverChooseBitmap(Bitmap bitmap)
	{
		//begin by smilefish		
		mCoverLast = NoteBookPickerActivity.setBitmapMask(bitmap, mCover1.getWidth(), mCover1.getHeight());
		if(bIsLastCoverExist)
			list.set(list.size() - 1, mCoverLast);
		else
			list.add(mCoverLast);
		mCheckIndex = list.size() - 1;
		//end
		notifyDataSetChanged();
		
		//begin by smilefish
		if(mParentView != null){ //fix bug: 301828
			mParentView.post(new Runnable(){
				@Override
				public void run(){
					mParentView.setSelection(16);
				}
			});
		}
		else
			isNeedToSetSelection = true;
		//end
	}	
	
	//begin smilefish
	public boolean isNeedToSetSelection(){
		return isNeedToSetSelection;
	}
	
	public void setCoverSelection(){
		isNeedToSetSelection = false;
		if(mParentView != null){
			mParentView.post(new Runnable(){
				@Override
				public void run(){
					mParentView.setSelection(16);
				}
			});
		}
	}
	
	public int getLastIndex(){
		return mCheckIndex;
	}
	
	public void setLastIndex(int index){
		if(mCheckIndex != index){
			mCheckIndex = index;
			notifyDataSetChanged();
		}		
	}
	//end smilefish

	//emmanual to fix bug 466553
	public void restoreCoverIndex(){
		setLastIndex(mCheckCoverIndex);
	}
	
	public void setDialog(Dialog dialog)
	{
		mDialog = dialog;
	}
	
	@Override
	public int getCount() {
		return list.size(); //modified by smilefish
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder holder;
		view = View.inflate(mContext, R.layout.notebook_editcover_item, null);

		if (convertView == null || convertView.getTag() == null) {
			holder = new ViewHolder();
			holder.pageCover = (ImageView) view.findViewById(R.id.notebook_cover);
			holder.pageCoverCheck = (ImageView) view.findViewById(R.id.notebook_cover_check);
			holder.pageCoverMask = (ImageView) view.findViewById(R.id.notebook_edit_cover_mask);
			//begin smilefish
				if(position == 0)
				{
					holder.pageCoverPhoto = (TextView) view.findViewById(R.id.notebook_cover_photo);
					holder.pageCoverPhoto.setVisibility(View.VISIBLE);
				}
			//end smilefish
		}
		else
		{
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		holder.pageCover.setImageBitmap(list.get(position));
		if(mCheckIndex == position)
		{
			holder.pageCoverCheck.setBackgroundResource(R.drawable.asus_icon_c);
		}
		holder.pageCoverMask.setTag(holder);
		holder.pageCoverMask.setOnClickListener( maskClickListener);
		holder.index = position;
		
		return view;
	}

	private final View.OnClickListener maskClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			mCheckIndex = holder.index;
			if(holder.index == 0) //modified by smilefish
			{
				mNoteBookPickerActivity.showMenu();
			}else{
				mCheckCoverIndex = mCheckIndex;
			}
			
			notifyDataSetChanged();
		}
	};

	private static class ViewHolder {
		ImageView pageCover;
		ImageView pageCoverMask;
		ImageView pageCoverCheck;
		TextView  pageCoverPhoto; //smilefish
		int index;
	}
	
	

}
