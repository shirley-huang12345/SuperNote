package com.asus.supernote.picker;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.updatesdk.ZenUiFamily;
import com.asus.updatesdk.utility.DeviceUtils;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class TimeStampViewFragment extends Fragment implements
		DataCounterListener {
	public static final String TAG = "TimeStampViewFragment";
	private TimeStampViewAdapter mAdapter;
	private boolean mIsShowBookmark = false;

	private static final int NORMAL_MENU = 0;
	private static final int EDIT_MENU = 1;
	private static final int NO_MENU = 2;

	private int mMenuState = NORMAL_MENU;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
		mAdapter = new TimeStampViewAdapter(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.timestamp_list, null);
		ListView listView = (ListView) view.findViewById(R.id.page_list);
		listView.setAdapter(mAdapter);
		setHasOptionsMenu(true);
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		int menuId = R.menu.picker_menu;
		switch (mMenuState) {
		case NO_MENU:
		case NORMAL_MENU:
			menuId = R.menu.picker_menu;
			break;
		case EDIT_MENU:
			menuId = R.menu.picker_menu;
			break;
		default:
			menuId = R.menu.picker_menu;
			break;
		}

		inflater.inflate(menuId, menu);
		MenuItem sort = menu.findItem(R.id.menu_sort);
		if (sort != null) {
			sort.setVisible(false);
		}
		if (mMenuState == NORMAL_MENU || mMenuState == NO_MENU) {
            //begin smilefish
            MenuItem item = null;
            //end smilefish
			if (mIsShowBookmark == false) {
				item = menu.findItem(R.id.menu_edit);
				item.setVisible(false);
				
				if (!MetaData.TEXT_SEARCH_ENABLE) {
					item = menu.findItem(R.id.menu_search);
					item.setVisible(false);
				}
			}
			
			Boolean isCnSku = DeviceUtils.checkCnSku();
			item = menu.findItem(R.id.menu_set);
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			item = menu.findItem(R.id.menu_encourageUs);
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			if(isCnSku)
				item.setVisible(false);
			item = menu.findItem(R.id.menu_userVoice);
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			item = menu.findItem(R.id.menu_zenFamily);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            item.setTitle(ZenUiFamily.getZenUiFamilyTitle());
            if(isCnSku)
				item.setVisible(false);
			
		}
	}
	
	//begin smilefish fix bug 408299/408281
    @Override 
    public void onPrepareOptionsMenu(Menu menu){ 
    	super.onPrepareOptionsMenu(menu);
    	
    	Boolean isCnSku = DeviceUtils.checkCnSku();
		MenuItem item = menu.findItem(R.id.menu_zenFamily);
		if(isCnSku && item != null)
			item.setVisible(false);
		
		item = menu.findItem(R.id.menu_encourageUs);
		if(isCnSku && item != null)
			item.setVisible(false);

    }
    //end smilefish
	
    public void dataSetChange() {
        mAdapter.changeDataTimestamp();
    }

	@Override
	public void onDataChange(int count) {
		// TODO Auto-generated method stub

	}
	
    @Override
    public void onResume() {
        super.onResume();
        
        mAdapter.changeDataTimestamp();
        //darwin
        TimeStampViewAdapter.resetNotebookTimeStampPageProcessing();
        //darwin
    }

	@Override
	public void onSelectedDataChange(int count) {
		// TODO Auto-generated method stub

	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
