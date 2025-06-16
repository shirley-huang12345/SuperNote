package com.asus.supernote.picker;

import java.util.ArrayList;

import com.asus.supernote.R;
import com.asus.supernote.classutils.ColorfulStatusActionBarHelper;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.picker.SyncCancelDialogFragment.SyncCancelDialog;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

public class BookPickerView extends Activity implements DataCounterListener , SyncCancelDialog{
	
	private BookPickerViewAdapter mAdapter;	
	private Context mContext;
	private int mIntentFrom;
	private BookCase mBookcase;
	
	public ArrayList<Long>  mRemoteList;
	public ArrayList<Long>  mLocalList;
	private FrameLayout mActionBar_layout ;
	private ActionBar mActionBar;
	
	
	public static boolean mislocked = true;// wendy
	private SharedPreferences mPreference;
	private SharedPreferences.Editor mPreferenceEditor;
	private Resources mResources;
	//darwin
	
	private long[] mRemoteArray = null;//Carol-store variation of check-box
	private long[] mLocalArray = null;
	private String[] mValues;	//store book names 
	
	private SelectionMenu mSelectionMenu = null; //smilefish
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	this.finish();
                break;
        }
        return true;
    }
	//darwin
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		ColorfulStatusActionBarHelper.setContentView(R.layout.bookpicker_list, true, this);//smilefish		
		mContext = this;	
		
        // BEGIN: Better
        if (MetaData.AppContext == null) {
    		MetaData.AppContext = getApplicationContext();
		}
        // END: Better

        //Modified reason: for multi-dpi
		int IsOrientationAllowOrNot = this.getResources().getInteger(R.integer.is_orientation_allow); //by show
        if (IsOrientationAllowOrNot == 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        else  if (IsOrientationAllowOrNot ==1){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
		 else  {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //End: show_wang@asus.com
	    mPreference = getSharedPreferences(
				MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		mPreferenceEditor = mPreference.edit();
		mResources = getResources();
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		mIntentFrom = bundle.getInt("intentfrom");
		if(mIntentFrom == NoteBookPickerActivity.LOGIN)
		{
			String account = bundle.getString("toast");
			String msg = String.format(getResources().getString(R.string.sync_setting_sign_success), account);
			Toast toast = Toast
					.makeText(mContext, msg, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
			toast.show();
		}
		
		mActionBar_layout = (FrameLayout) View.inflate(
				BookPickerView.this, R.layout.book_picker_actionbar, null);
		
		
		mislocked = mPreference.getBoolean(
				mResources.getString(R.string.lock_state), true);

		mActionBar = getActionBar();
		mActionBar.setCustomView(mActionBar_layout);
		mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		
		mBookcase = BookCase.getInstance(this);
		GridView listView = (GridView)findViewById(R.id.book_grid);
		mAdapter = new BookPickerViewAdapter(this);

		//emmanual to fix bug 465474
		if (savedInstanceState != null) {
			boolean[] mCheckArray = savedInstanceState.getBooleanArray("BookPickerViewCheck");
			if (mCheckArray != null) {
				mAdapter.mCheckBox.clear();
				for (int i = 0; i < mCheckArray.length; i++) {
					mAdapter.mCheckBox.add(mCheckArray[i]);
				}
			}
		}
		
		mAdapter.registerDataCounterListener(this);//smilefish
		listView.setAdapter(mAdapter);
		//begin smilefish
			ImageButton doneButton = (ImageButton)mActionBar_layout.findViewById(R.id.book_picker_ok);
			doneButton.setOnClickListener(mListener);
			ImageButton cancelButton = (ImageButton)mActionBar_layout.findViewById(R.id.book_picker_cancel);
			cancelButton.setOnClickListener(mListener);
			
	        mSelectionMenu = new SelectionMenu(this, 
	        		(Button) mActionBar_layout.findViewById(R.id.book_select_menu));
	        mSelectionMenu.addOuterListener(notifyOuter);
	        setSelectionCountTitle(mAdapter.getSelectedCount());
		//end smilefish
				
	}
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
	    // TODO Auto-generated method stub
		//emmanual to fix bug 465474
		if (mAdapter != null && mAdapter.mCheckBox != null) {
			boolean[] mCheckArray = new boolean[mAdapter.mCheckBox.size()];
			for (int i = 0; i < mAdapter.mCheckBox.size(); i++) {
				mCheckArray[i] = mAdapter.mCheckBox.get(i);
			}
			outState.putBooleanArray("BookPickerViewCheck", mCheckArray);
		}
	    super.onSaveInstanceState(outState);
    }

	//begin smilefish	
	SelectionMenu.INotifyOuter notifyOuter =new SelectionMenu.INotifyOuter(){

		@Override
		public void setSelectAll(boolean flag) {
			mAdapter.setSelectAll(flag);
		}
    };
    
    private void setSelectionCountTitle(int count){
		String msg = String.format(
				getResources().getString(R.string.notebook_selected),
				Integer.toString(count));
		mSelectionMenu.setTitle(msg); 
		if(count == mAdapter.getCount())
			mSelectionMenu.updateSelectAllMode(true);
		else 	
			mSelectionMenu.updateSelectAllMode(false);
    }
    
	@Override
	public void onStop() {
		super.onStop();

		if(mSelectionMenu != null)
			mSelectionMenu.hidePopupWindow();
	}
  //end smilefish
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

        //Begin: show_wang@asus.com
    	//Modified reason: for multi-dpi
    	int IsOrientationAllowOrNot = this.getResources().getInteger(R.integer.is_orientation_allow); //by show
        if (IsOrientationAllowOrNot == 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        else  if (IsOrientationAllowOrNot ==1){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
		 else  {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //End: show_wang@asus.com
        
        ColorfulStatusActionBarHelper.setContentView(R.layout.bookpicker_list, true, this);//smilefish
		
		mActionBar_layout = (FrameLayout) View.inflate(
				BookPickerView.this, R.layout.book_picker_actionbar, null);
		
		
		mislocked = mPreference.getBoolean(
				mResources.getString(R.string.lock_state), true);
		
		mActionBar = getActionBar();
		mActionBar.setCustomView(mActionBar_layout);
		mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		
		//begin smilefish
		ImageButton doneButton = (ImageButton)mActionBar_layout.findViewById(R.id.book_picker_ok);
		doneButton.setOnClickListener(mListener);
		ImageButton cancelButton = (ImageButton)mActionBar_layout.findViewById(R.id.book_picker_cancel);
		cancelButton.setOnClickListener(mListener);
		//end smilefish
		
		GridView listView = (GridView)findViewById(R.id.book_grid);		
		listView.setAdapter(mAdapter);
		
		listView.setScrollingCacheEnabled(false);//RICHARD
		
		//begin smilefish
		if(mSelectionMenu != null)
			mSelectionMenu.hidePopupWindow(); //smilefish fix bug 608203
		
        mSelectionMenu = new SelectionMenu(this, 
        		(Button) mActionBar_layout.findViewById(R.id.book_select_menu));
        mSelectionMenu.addOuterListener(notifyOuter);
        setSelectionCountTitle(mAdapter.getSelectedCount());
        //end smilefish
	}
	
	private OnClickListener mListener = new OnClickListener()
	{
		@Override
		public void onClick(View v) { //modify by Carol- add sync changes confirm dialog for prompt
		
			if(v.getId() == R.id.book_picker_ok)
			{
				ArrayList<Long>  mRemoteList = new ArrayList<Long>();
				ArrayList<Long>  mLocalList = new ArrayList<Long>();
				for(int i = 0;i<mAdapter.getCount();i++){
					if(mAdapter.mOriginalCheckBox.get(i) != mAdapter.mCheckBox.get(i)){
						if(mAdapter.mCheckBox.get(i)){
							mRemoteList.add(mAdapter.mBookId.get(i));
						} else {
							mLocalList.add(mAdapter.mBookId.get(i));
						}
					} 
				}
			
				if(mRemoteList.size()==0 && mLocalList.size() ==0){
					BookPickerView.this.finish(); //empty list, no change
				} else {
					//one or more book is selected 
					mRemoteArray = new long[mRemoteList.size()];
					for(int i=0;i< mRemoteList.size();i++){
						mRemoteArray[i] = mRemoteList.get(i);//get new sync books
					}
					mLocalArray = new long[mLocalList.size()];
					mValues = new String[mLocalList.size()];
					for(int i=0;i<mLocalList.size();i++){
						mLocalArray[i] = mLocalList.get(i);
						mValues[i]= mBookcase.getNoteBook(mLocalList.get(i)).getTitle();
					}
					boolean showHint = mPreference.getBoolean(mResources.getString(R.string.pref_hide_syncpompt), true);
					if(mLocalList.size()>0){
						//some books are removed from sync function
						if(showHint){
							//show the confirm dialog
							String PREFERNECE_NAME = "SuperNote";
							mResources = getResources();
							mPreference = getSharedPreferences(PREFERNECE_NAME, Activity.MODE_PRIVATE);//Context.MODE_MULTI_PROCESS);
				    		Bundle b = new Bundle();
							SyncCancelDialogFragment dialogFragment = SyncCancelDialogFragment.newInstance(b);
							dialogFragment.setListener(BookPickerView.this);
							dialogFragment.show(getFragmentManager(), SyncCancelDialogFragment.TAG);
						} else {
							updateSyncBookList(true);
						}
					} else if(mRemoteList.size()>0) {
						//add sync books only
						updateSyncBookList(false);
						BookPickerView.this.finish();
					}
				}	
			}
			else if(v.getId() == R.id.book_picker_cancel)
			{
				BookPickerView.this.finish();				
			}
		}
	};
	
	

	@Override
    protected void onDestroy() {
		//emmanual, update widget when sign in
    	Intent updateIntent = new Intent();
		updateIntent.setAction(MetaData.ACTION_SUPERNOTE_UPDATE);
		updateIntent.putExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM, MetaData.SuperNoteUpdateInfoSet);
		sendBroadcast(updateIntent);
		MetaData.SuperNoteUpdateInfoSet.clear();
		
		if(mAdapter != null)
			mAdapter.closeCursor(); //smilefish add fix memory leak
	    super.onDestroy();
    }

	@Override
	public void onDataChange(int count) {
		// TODO Auto-generated method stub

	}
	
    @Override
    public void onResume() {
        super.onResume();
        
		//smilefish add for runtime permission
        if(MethodUtils.needShowPermissionPage(mContext)){
			MethodUtils.showPermissionPage(this, false);
		}else{
			mAdapter.dataChange();
		}
    }

	@Override
	public void onSelectedDataChange(int count) {
		setSelectionCountTitle(count);//smilefish

	}

	@Override
	public void onPressPositive(){ //Carol-'OK' button in SyncCancelDialogFragment dialog
		updateSyncBookList(true);
	}
	
	@Override
	public void onPressNegative(){ //Carol-'Cancel' button in SyncCancelDialogFragment dialog
		//cancel
		updateSyncBookList(false);
	}
	
	@Override
	public String[] onAdapterGetter(){
		//emmanual to fix bug 465474
		if(mValues == null){
			ArrayList<Long>  mRemoteList = new ArrayList<Long>();
			ArrayList<Long>  mLocalList = new ArrayList<Long>();
			for(int i = 0;i<mAdapter.getCount();i++){
				if(mAdapter.mOriginalCheckBox.get(i) != mAdapter.mCheckBox.get(i)){
					if(mAdapter.mCheckBox.get(i)){
						mRemoteList.add(mAdapter.mBookId.get(i));
					} else {
						mLocalList.add(mAdapter.mBookId.get(i));
					}
				} 
			}
		
			if(mRemoteList.size()!=0 || mLocalList.size() !=0){
				//one or more book is selected 
				mRemoteArray = new long[mRemoteList.size()];
				for(int i=0;i< mRemoteList.size();i++){
					mRemoteArray[i] = mRemoteList.get(i);//get new sync books
				}
				mLocalArray = new long[mLocalList.size()];
				mValues = new String[mLocalList.size()];
				for(int i=0;i<mLocalList.size();i++){
					mLocalArray[i] = mLocalList.get(i);
					mValues[i]= mBookcase.getNoteBook(mLocalList.get(i)).getTitle();
				}
				boolean showHint = mPreference.getBoolean(mResources.getString(R.string.pref_hide_syncpompt), true);
				if(mLocalList.size()>0){
					//some books are removed from sync function
					if(showHint){
						//show the confirm dialog
						String PREFERNECE_NAME = "SuperNote";
						mResources = getResources();
						mPreference = getSharedPreferences(PREFERNECE_NAME, Activity.MODE_PRIVATE);//Context.MODE_MULTI_PROCESS);
			    		Bundle b = new Bundle();
						SyncCancelDialogFragment dialogFragment = SyncCancelDialogFragment.newInstance(b);
						dialogFragment.setListener(BookPickerView.this);
						dialogFragment.show(getFragmentManager(), SyncCancelDialogFragment.TAG);
					} else {
						updateSyncBookList(true);
					}
				} else if(mRemoteList.size()>0) {
					//add sync books only
					updateSyncBookList(false);
				}
			}
		}
		return mValues;
	}
	
	private void updateSyncBookList(Boolean confirm) //Carol- update the result
	{
		BookPickerView.this.finish();
		//update the sync result according the 'OK' or ‘Cancel’ button choice
		try {
			Intent intent= new Intent(MetaData.CHOOSE_BOOKS);
			intent.putExtra("remotearray", mRemoteArray.length>0 ? mRemoteArray : new long[0]);
			intent.putExtra("localarray", confirm ? mLocalArray : new long[0]);
			mContext.sendBroadcast(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
