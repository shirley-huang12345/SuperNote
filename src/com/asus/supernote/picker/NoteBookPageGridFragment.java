package com.asus.supernote.picker;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.SettingActivity;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.datacopy.DataCopyActivity;
import com.asus.supernote.dialog.utils.DialogUtils;
import com.asus.supernote.dialog.utils.TextViewUtils;
import com.asus.supernote.inksearch.CFG;
import com.asus.supernote.picker.SyncCancelDialogFragment.SyncCancelDialog;
import com.asus.updatesdk.ZenUiFamily;
import com.asus.updatesdk.utility.DeviceUtils;

public class NoteBookPageGridFragment extends Fragment implements Callback,
		DataCounterListener, SyncCancelDialog {
	public static final String TAG = "NoteBookPageGridFragment";
	public static final String ALIAS_TAG = "PersonalPageFragment";
	public static final String SORT_BY_PAGE = MetaData.BookTable.TITLE
			+ " ASC";;
	public static final String SORT_BY_MODIFIED = MetaData.BookTable.MODIFIED_DATE
			+ " DESC";
	public static final String SORT_BY_HIDE = MetaData.BookTable.IS_LOCKED
			+ " DESC";
	public static final int NORMAL_MENU = 0;
	public static final int EDIT_MENU = 1;

	private static final int SORT_BY_DATE_INDEX = 0;
	private static final int SORT_BY_MODIFIED_INDEX = 1;
	private TextView mMaskView;
	private BookCase mBookcase;
	private ContentResolver mContentResolver;
	private NoteBookPageGridViewAdapter mAdapter;
	private Menu mMenu;
	private ImageButton mAddButton; //smilefish

	private NoteBookPickerActivity mActivity;
	private int deviceType;
	private boolean mIsPrivate;
	private int mMenuState = NORMAL_MENU;
	private long mBookId = BookCase.NO_SELETED_BOOK;
	static private String mSortOrder = SORT_BY_PAGE;// + "," + SORT_BY_HIDE
	public String getSortOrder() {
		return mSortOrder;
	}

	private Resources mResources;
	private SharedPreferences mPreference;

	private ActionMode mActionMode = null;

    //BEGIN: RICHARD
    public static String mSearchString = "";
    //END: RICHARD
    
	public String[] mValues;  //Carol-store book names
	
    private Switch mSwitchLockUnlockSwitch = null; //smilefish
    private Boolean mIsAutoChanged = false; //smilefish
    private SelectionMenu mSelectionMenu = null; //smilefish
    private boolean mShowLockDialogForOneBook = false;//smilefih
    private boolean mIsPositiveButtonClicked = false;//smilefih
	
	//darwin
	public void setonCreate()
	{
		mIsOnCreate = true;
	}
	private boolean mIsOnCreate = false;
	public void gotoEditPage()
	{
		if((NoteBookPickerActivity.getFirstIn() == 1) && (mAdapter != null) && mIsOnCreate)
		{
			this.findLatestEditBook();
			if(mAdapter.mLatestEditBookId != 0L &&(mAdapter.mLatestEditPageId != 0L))
			{
				if (mAdapter.bIsLock == true) 
				{
				} 
				else 
				{
					Log.v(MetaData.DEBUG_TAG, "launchLatestEditPage");
					launchLatestEditPage();
				}
			}
		}
		mIsOnCreate = false;
	}
	
	public void gotoEditPageWithoutCheck(){//add by noah
		this.findLatestEditBook();
		if(mAdapter.mLatestEditBookId != 0L &&(mAdapter.mLatestEditPageId != 0L))
		{
			if (mAdapter.bIsLock == true) 
			{
			} 
			else 
			{
				Log.v(MetaData.DEBUG_TAG, "launchLatestEditPage");
				launchLatestEditPage();
			}
		}
	}
	
	//darwin
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		deviceType = PickerUtility.getDeviceType(getActivity());
		if (deviceType>100) {
			// begin wendy
			mIsPrivate = NoteBookPickerActivity.islocked();
			// end wendy
		}
		mActivity = (NoteBookPickerActivity) getActivity();
		mResources = getActivity().getResources();
		mBookcase = BookCase.getInstance(getActivity());
		mContentResolver = getActivity().getContentResolver();

		mPreference = getActivity().getSharedPreferences(
				MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);

		mSortOrder = mPreference.getString(
				mResources.getString(R.string.pref_book_sort_type), SORT_BY_PAGE);
		
		Cursor cursor = null;
		if (mIsPrivate == true && deviceType > 100) {
			cursor = mContentResolver
					.query(MetaData.BookTable.uri,
							null,
							"(is_locked > 0)AND ((userAccount = 0) OR (userAccount = ?))",
							new String[] { Long
									.toString(MetaData.CurUserAccount) }, null);
			cursor.moveToFirst();
			mBookId = (cursor.getCount() == 0) ? BookCase.NO_SELETED_BOOK
					: cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
		} else if (mIsPrivate == true && deviceType <= 100) {

		} else {
			if (NoteBookPickerActivity.getIsLock()) {
				cursor = mContentResolver
						.query(MetaData.BookTable.uri,
								null,
								"(is_locked = 0) AND ((userAccount = 0) OR (userAccount = ?))",
								new String[] { Long
										.toString(MetaData.CurUserAccount) },
								"title");
			} else {
				cursor = mContentResolver
						.query(MetaData.BookTable.uri, null,
								"(userAccount = 0) OR (userAccount = ?)",
								new String[] { Long
										.toString(MetaData.CurUserAccount) },
								"title");
			}
			mBookId = (cursor.getCount() == 0) ? BookCase.NO_SELETED_BOOK
					: mBookcase.getCurrentBookId();
			mBookcase.setCurrentBook(mBookId);
		}
		mAdapter = new NoteBookPageGridViewAdapter(getActivity(), this);
		//darwin
        NoteBookPageGridViewAdapter.resetNotebookPageGridProcessing();
        //darwin
		mAdapter.registerDataCounterListener(this);
		if (cursor != null) {
			cursor.close();
		}
		//darwin
		if(!mActivity.isShareMode() && 
		        !DataCopyActivity.needDataCopy()//add by noah
		        &&mActivity.existLastPage() //fixed 382097
		        &&!MetaData.ADD_BOOK_FROM_WIDGET) //add book from widget[Carol]
		{
			boolean isOpenLastEditPage = getActivity().getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
			        .getBoolean(getActivity().getResources().getString(R.string.pref_last_edit_page),false);// emmanual
			if(isOpenLastEditPage){
				gotoEditPage();
			}
		}
        //darwin
	}

	@Override
	public void onStart() {
		super.onStart();
		changeData(mBookId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.book_grid_view, null);
		mMaskView = (TextView) view.findViewById(R.id.book_gridview_mask);
		mAddButton = (ImageButton)view.findViewById(R.id.book_add); //smilefish
		mAddButton.setOnClickListener(addNoteBookListener);
		GridView gridView = (GridView) view.findViewById(R.id.book_gridview);
		gridView.setAdapter(mAdapter);
		gridView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);

		registerForContextMenu(gridView);
		return view;
	}
	
    private final OnClickListener addNoteBookListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	//darwin
        	if(mAdapter.getIsNotebookPageGridProcessing())
        	{
        		return;
        	}
        	NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
        	activity.createAddBookDialog(null);
        }
    };

	public void changeData(Long bookId) {
		mBookId = bookId;
		mAdapter.changeSortOrder(mBookId, mSortOrder);
		mAdapter.notifyDataSetChanged();
	}

	public void dataSetChangedUpdate() {
		mAdapter.requeryDataUpdate(NoteBookPickerActivity.getIsLock(), mSortOrder);
		// mAdapter.requery();
		findLatestEditBook();
	}

	public void launchLatestEditPageInPersonal() {
		// using one_input_dialog
		final View view = View.inflate(getActivity(),
				R.layout.one_input_dialog, null);
		final EditText editText = (EditText) view
				.findViewById(R.id.input_edit_text);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.password);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setNegativeButton(android.R.string.cancel, null);
		final AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String password = mPreference.getString(
								mResources.getString(R.string.pref_password),
								null);
						if (password == null) {
							dialog.dismiss();
						} else if (password.equals(editText.getText()
								.toString())) {
							dialog.dismiss();
							launchLatestEditPage();
						} else {
							editText.setText("");
							editText.setHint(R.string.password_diff_password);
						}
					}
				});

	}

	public void launchLatestEditPage() {
		if (mAdapter.mLatestEditBookId != 0L
				&& mAdapter.mLatestEditPageId != 0L) {
			try {
				Log.v(MetaData.DEBUG_TAG, "launch: bookid=" + mAdapter.mLatestEditBookId + ", pageid=" + mAdapter.mLatestEditPageId);
				Intent intent = new Intent();
				intent.setClass(getActivity(), EditorActivity.class);
				intent.putExtra(MetaData.BOOK_ID, mAdapter.mLatestEditBookId);
				intent.putExtra(MetaData.PAGE_ID, mAdapter.mLatestEditPageId);
				getActivity().startActivity(intent);
			} catch (ActivityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mSearchString = "";//RICHARD
		}
	}

	public void findLatestEditBook() {
		ContentResolver cr = getActivity().getContentResolver();
		Cursor cursor = cr.query(MetaData.PageTable.uri, null, "(version = ? AND is_deleted = 0 AND ((userAccount = 0) OR (userAccount = ?)))",
				new String[] { "3",Long.toString(MetaData.CurUserAccount) }, "is_last_edit DESC");
		if(cursor != null && cursor.getCount() != 0)
		{
			cursor.moveToFirst();
			while(!cursor.isAfterLast())
			{
				if(NoteBookPickerActivity.getIsLock())
				{
					long bookid = cursor.getLong(MetaData.PageTable.INDEX_OWNER);
					if (mBookcase == null) {
						mBookcase = BookCase.getInstance(getActivity());
					}
					NoteBook book = mBookcase.getNoteBook(bookid);
					if(book == null )//|| (book.getIsLocked())
					{
						cursor.moveToNext();
						continue;
					}
				}
				long returnValue = cursor.getLong(MetaData.PageTable.INDEX_IS_LAST_EDIT);
				if(returnValue != 0)
				{
					mAdapter.mLatestEditPageId = cursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE);
					mAdapter.mLatestEditBookId = cursor.getLong(MetaData.PageTable.INDEX_OWNER);
				}
				else
				{
					mAdapter.mLatestEditPageId = 0L;
					mAdapter.mLatestEditBookId = 0L;
				}
				break;
			}
			cursor.close();
		}
		else
		{
			//BEGIN: RICHARD
			if(cursor != null)
			{
				cursor.close();
			}
			//END: RICHARD
			mAdapter.mLatestEditPageId = 0L;
			mAdapter.mLatestEditBookId = 0L;
			Log.i("Darwin Test", "findLatestEditBook 11111   mLatestEditBookId == " + mAdapter.mLatestEditBookId + "  mLatestEditPageId == " + mAdapter.mLatestEditPageId);
		}
		if (mAdapter.mLatestEditBookId != 0L) {
			if (mBookcase == null) {
				mBookcase = BookCase.getInstance(getActivity());
			}
			NoteBook book = mBookcase.getNoteBook(mAdapter.mLatestEditBookId);
			if (book != null) {
				mAdapter.mLatestEditBookName = book.getTitle();
				mAdapter.mLatestEditBookBitmap = NotePage.getThumbnail(
						mAdapter.mLatestEditBookId, mAdapter.mLatestEditPageId);
			}
		}
		mAdapter.bIsLock = false;
		if ( mAdapter.mLatestEditBookId == 0L && mAdapter.mLatestEditPageId == 0L) {
			mAdapter.mLatestEditBookName = "";
			mAdapter.mLatestEditBookBitmap = null;
		} else {
			if (mBookcase == null) {
				mBookcase = BookCase.getInstance(getActivity());
			}
			NoteBook book = mBookcase.getNoteBook(mAdapter.mLatestEditBookId);
			if (book != null) { // shaun
				mAdapter.mLatestEditBookName = book.getTitle();
				if (book.getIsLocked() && NoteBookPickerActivity.islocked()) {// wendy
					//begin darwin 
					mAdapter.mLatestEditBookBitmap = null;
					//end darwin
					mAdapter.bIsLock = true;
				} else {
					mAdapter.mLatestEditBookBitmap = NotePage.getThumbnail(
							mAdapter.mLatestEditBookId,
							mAdapter.mLatestEditPageId);
					mAdapter.bIsLock = false;
				}
			}

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			break;
		case R.id.menu_edit:
			getActivity().startActionMode(this);
            NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();//smilefish
            activity.showStateBar(false); //smilefish
			break;
		default:
			if (mMenuState != NORMAL_MENU) {
			}
		}
		return true;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		ActionBar bar = getActivity().getActionBar();

			bar.setDisplayHomeAsUpEnabled(true);
			bar.setDisplayShowHomeEnabled(true);
			bar.setHomeButtonEnabled(true);

		switch (mMenuState) {
		case NORMAL_MENU:
			inflater.inflate(R.menu.notebook_picker_menu, menu);
			MenuItem item = menu.findItem(R.id.menu_lock);
			if (NoteBookPickerActivity.islocked()) {
				item.setTitle(getActivity().getString(R.string.show_personal_notebook));
			} else 
			{
				item.setTitle(getActivity().getString(R.string.hide_personal_notebook));
			}
			
			Boolean isCnSku = DeviceUtils.checkCnSku();
			item = menu.findItem(R.id.menu_zenFamily);
			item.setTitle(ZenUiFamily.getZenUiFamilyTitle());
			if(isCnSku)
				item.setVisible(false);
			
			item = menu.findItem(R.id.menu_encourageUs);
			if(isCnSku)
				item.setVisible(false);
                
			break;
		case EDIT_MENU:
			break;
		}
		
		// BEGIN: Shane_Wang 2012-10-15
		if(CFG.getCanDoVO() == false && !MetaData.TEXT_SEARCH_ENABLE) {
			MenuItem itemSearch = menu.findItem(R.id.menu_search);
			if (itemSearch != null) {
				itemSearch.setVisible(false);
			}
        }
		// END: Shane_Wang 2012-10-15
		//Begin noah;for share
		NoteBookPickerActivity noteBookPickerActivity = (NoteBookPickerActivity)getActivity();
		boolean shareMode = noteBookPickerActivity.isShareMode();
		if(shareMode){
			bar.setDisplayHomeAsUpEnabled(false); //smilefish fix bug 570481
			bar.setDisplayShowHomeEnabled(false);
			bar.setHomeButtonEnabled(false);
			bar.setDisplayShowCustomEnabled(true);
			int[] menuIDs = new int[]{R.id.menu_edit, R.id.menu_set, R.id.menu_encourageUs, R.id.menu_zenFamily, R.id.menu_userVoice, R.id.menu_lock, R.id.menu_page_import};
			for (int id : menuIDs) {
				MenuItem menuItem = menu.findItem(id);
				if (menuItem != null) {
					menuItem.setVisible(false);
				}
			}
			
			MenuItem itemSearch = menu.findItem(R.id.menu_search);
			if (itemSearch != null) {
				itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER); //smilefish fix bug 577002
			}
			
		}
		//end noah;for share
	}
	//Begin: show_wang@asus.com
	//Modified reason: menu_lock title
    @Override 
    public void onPrepareOptionsMenu(Menu menu){ 
    	super.onPrepareOptionsMenu(menu);
    	
    	//begin smilefish fix bug 408299/408281
    	NoteBookPickerActivity noteBookPickerActivity = (NoteBookPickerActivity)getActivity();
    	boolean drawerOpen = noteBookPickerActivity.isNavigationDrawerShown();
    	menu.setGroupVisible(Menu.NONE, !drawerOpen);
    	//end smilefish
    	
		MenuItem item = menu.findItem(R.id.menu_lock);
		if (item != null) {
			if (NoteBookPickerActivity.islocked()) {
				item.setTitle(getActivity().getString(R.string.show_personal_notebook));
			} else 
			{
				item.setTitle(getActivity().getString(R.string.hide_personal_notebook));
			}
		}
		
		// BEGIN: Shane_Wang 2012-10-15
		if(CFG.getCanDoVO() == false && !MetaData.TEXT_SEARCH_ENABLE) {
			MenuItem itemSearch = menu.findItem(R.id.menu_search);
			if (itemSearch != null) {
				itemSearch.setVisible(false);
			}
        }
		// END: Shane_Wang 2012-10-15
		
		Boolean isCnSku = DeviceUtils.checkCnSku();
		item = menu.findItem(R.id.menu_zenFamily);
		if(isCnSku && item != null)
			item.setVisible(false);
		
		item = menu.findItem(R.id.menu_encourageUs);
		if(isCnSku && item != null)
			item.setVisible(false);
		
		//Begin noah;for share
		boolean shareMode = noteBookPickerActivity.isShareMode();
		if(shareMode){
			int[] menuIDs = new int[]{R.id.menu_edit, R.id.menu_set, R.id.menu_encourageUs, R.id.menu_zenFamily, R.id.menu_userVoice, R.id.menu_lock, R.id.menu_page_import};
			for (int id : menuIDs) {
				MenuItem menuItem = menu.findItem(id);
				if (menuItem != null) {
					menuItem.setVisible(false);
				}
			}
			
		}
		//end noah;for share
    }
	//End: show_wang@asus.com

	public void SortNoteBook(int type) {

		if (type == SORT_BY_DATE_INDEX) {
			mSortOrder = SORT_BY_PAGE;// + "," + SORT_BY_HIDE
		} else if (type == SORT_BY_MODIFIED_INDEX) {
			mSortOrder = SORT_BY_MODIFIED;// SORT_BY_HIDE + "," +
		}
		if(mAdapter != null) //smilefish fix bug 307726
			mAdapter.changeSortOrder(mBookId, mSortOrder);
		
		//smilefish fix bug 605309
		SharedPreferences.Editor editor = mPreference.edit();
		editor.putString(mResources.getString(R.string.pref_book_sort_type), mSortOrder);
		editor.commit();
	}
	
	public void dataSetChanged() {
		mAdapter.changeSortOrder(mBookId, mSortOrder);
	}

	public View getDraggedView() {
		return mAdapter.getDraggedView();
	}

	public ArrayList<Long> getSelectList() {
		return mAdapter.getSelectList();
	}

	private void deletePages() {
		NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
		activity.deleteBook(activity, mAdapter.getSelectList());
		mAdapter.requeryDataUpdate(NoteBookPickerActivity.getIsLock(), mSortOrder);
	}

	private void deleteBook(AdapterView.AdapterContextMenuInfo menuInfo) {
		NoteBook noteBook = (NoteBook) mAdapter.getItem(menuInfo.position );
		NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
		activity.deleteBook(activity, noteBook.getCreatedTime());
		mAdapter.requeryDataUpdate(NoteBookPickerActivity.getIsLock(), mSortOrder);
	}

	private void showBookHWRDialog(AdapterView.AdapterContextMenuInfo menuInfo) {
		NoteBook noteBook = (NoteBook) mAdapter.getItem(menuInfo.position );
		NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
		activity.showBookHWRDialog(activity, noteBook.getCreatedTime());
		mAdapter.requeryDataUpdate(NoteBookPickerActivity.getIsLock(), mSortOrder);
	}
	public void setNormalModeMenu() {
		mMenuState = NORMAL_MENU;
		finishActionMode();
		mAdapter.setSelectAll(false);
		mAdapter.setEditMode(false);
		dataSetChanged();
		getActivity().invalidateOptionsMenu();
		
		NoteBookPickerActivity pickerActivity = (NoteBookPickerActivity) getActivity();
		pickerActivity.setDrawerEnabled(true);
	}

	public void exportPage() {
		Long bookId = mSelectedBook.getCreatedTime();
		ArrayList<Long> list = new ArrayList<Long>();
		list.add(bookId);
		final NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
		activity.exportNote(getActivity(), list);
	}

	public void exportPages() {
		final NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
		activity.exportNote(getActivity(), mAdapter.getSelectList());
	}

	public void setMenuVisible(int id, boolean b) {
		if (mMenuState == EDIT_MENU && mMenu != null) {
			mMenu.setGroupVisible(id, b);
		}
	}

	public int getMenuState() {
		return mMenuState;
	}

	public void setMenuState(int state) {
		mMenuState = state;
		getActivity().invalidateOptionsMenu();
	}

	private void setMaskVisible(boolean b) {
		if (mMaskView != null) {
			mMaskView.setVisibility(b ? View.VISIBLE : View.GONE);
		}
	}

	public void setPrivateMode(boolean b) {
		mIsPrivate = b;
	}

	public boolean isPrivateMode() {
		return mIsPrivate;
	}

	@Override
	public void onStop() {
		super.onStop();

		if(mSelectionMenu != null)
			mSelectionMenu.hidePopupWindow();//smilefish
	}

	@Override
	public void onDestroy() {
		setHasOptionsMenu(false);
		mAdapter.closeCursor();
		mAdapter.removeDataCounterListener();
		super.onDestroy();
	}

	// BEGIN: Better
	@Override
	public void onResume() {
		mAdapter.setDateFormat();
		
		// modify by wendy fix bug id 30
		changeData(mBookId);
		// modify by wendy
		findLatestEditBook();
		((NoteBookPickerActivity)getActivity()).checkPersonal();

		PageGridFragment.mSearchString = "";//RICHARD
		AllPageViewFragment.mSearchString = "";//RICHARD
		//darwin
        NoteBookPageGridViewAdapter.resetNotebookPageGridProcessing();
        //darwin
		super.onResume();
		
		NoteBookPickerActivity activity = (NoteBookPickerActivity)getActivity();
		if(mActionMode != null || activity.isShareMode()) //smilefish fix bug 570323 465126
			activity.setDrawerEnabled(false); 
		else
			activity.setDrawerEnabled(true);
	}

	// END: Better

	public long getBookId() {
		return mBookId;
	}

	/********************************************************
	 * Action Mode callback start
	 *******************************************************/
	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_page_del_book:
			deletePages();
			break;
		case R.id.menu_page_export:
			exportPages();
			break;
		case R.id.menu_page_export_pdf:
			exportPagesToPdf();
			break;
		}
		return true;
	}
	//darwin
	
	boolean bIsSelectAll = false;
	
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mActionMode = mode;
		mMenuState = EDIT_MENU;
		mAdapter.changeSortOrder(mBookId, mSortOrder);
		mAdapter.setEditMode(true);

		dataSetChanged();
		
		NoteBookPickerActivity pickerActivity = (NoteBookPickerActivity) getActivity();
		pickerActivity.setDrawerEnabled(false);

		mode.getMenuInflater().inflate(R.menu.book_edit_menu, menu);
		setCustomActionBar();//smilefish
		setSelectionCountTitle(0);
		mMenu = menu;
		{
			MenuItem item = mMenu.findItem(R.id.menu_lock);
			View view = item.getActionView();
			//begin smilefish
				mSwitchLockUnlockSwitch = (Switch)view.findViewById(R.id.notebook_menu_lock_unlock);
				mSwitchLockUnlockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if(mIsAutoChanged)
						{
							mIsAutoChanged = false;
							return;
						}
						
						if(isChecked)
						{
							showHideDialog_edit();
						}
						else {
							showUnhideDialog_edit();
						}
					}
					
				});
			//end smilefish
		}
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		mActionMode = null;
		mMenu = null;
		setNormalModeMenu();
		NoteBookPickerActivity pickerActivity = (NoteBookPickerActivity) getActivity();
		pickerActivity.setNavigationDrawerBackground(mResources.getColor(R.color.drawer_background_color)); //smilefish fix bug 503241
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		NoteBookPickerActivity pickerActivity = (NoteBookPickerActivity) getActivity();
		pickerActivity.setNavigationDrawerBackground(Color.WHITE); //smilefish fix bug 503241
		return true;
	}

	/********************************************************
	 * Action Mode callback end
	 *******************************************************/
	//begin smilefish	
	SelectionMenu.INotifyOuter notifyOuter =new SelectionMenu.INotifyOuter(){

		@Override
		public void setSelectAll(boolean flag) {
			mAdapter.setSelectAll(flag);
		}
    };

	private void setCustomActionBar()
	{
        View customView = LayoutInflater.from(mActivity).inflate(
                R.layout.book_edit_actionbar, null);
        mSelectionMenu = new SelectionMenu(mActivity, 
        		(Button) customView.findViewById(R.id.book_select_menu));
        mSelectionMenu.addOuterListener(notifyOuter); //smilefish
        mActionMode.setCustomView(customView);	         
	}
	//end smilefish

	public void finishActionMode() {
		if (mActionMode != null) {
			mActionMode.finish();
		}
		
        NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();//smilefish        
        if(activity != null){//emmanual to fix bug 465369
        	activity.showStateBar(true); //smilefish
        }
	}

	public void setSelectionCountTitle(int count) {
		if (mActionMode != null) {
			String msg = String.format(
					getResources().getString(R.string.notebook_selected),
					Integer.toString(count));
			//begin smiefish
			if(PickerUtility.isPhone(mActivity) && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT))
				mSelectionMenu.setTitle(Integer.toString(count)); 
			else
				mSelectionMenu.setTitle(msg); 
			if(count == mAdapter.getCount())
				mSelectionMenu.updateSelectAllMode(true);
			else
				mSelectionMenu.updateSelectAllMode(false);
			//end smilefish
		}
	}

	private NoteBook mSelectedBook;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		//begin noah;for share
		boolean shareMode = ((NoteBookPickerActivity)getActivity()).isShareMode();
		if(shareMode)
			return;
		//end noah;for share
		if (!mAdapter.isEditMode()) {
			super.onCreateContextMenu(menu, v, menuInfo);
			NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			if(info == null)
			{
				return;
			}
			mSelectedBook = (NoteBook) mAdapter.getItem(info.position);
			if(mSelectedBook == null)
			{
				return;
			}
			if (!MetaData.TransferringBookIdList.contains(mSelectedBook
					.getCreatedTime())) { // Better
				activity.getMenuInflater().inflate(R.menu.book_item_edit_menu,
						menu);
				// BEGIN: Shane_Wang 2012-10-11
				if(CFG.getCanDoVO() == false) {
					menu.findItem(R.id.menu_book_hwr).setVisible(false);
				}
				// END: Shane_Wang 2012-10-11
				
				// begin wendy
				if (mSelectedBook.getIsLocked()) {
					menu.findItem(R.id.menu_book_hide).setVisible(false);
					menu.findItem(R.id.menu_book_unhide).setVisible(true);
					menu.findItem(R.id.menu_book_lock_hide).setVisible(false);
				} else {
					if (NoteBookPickerActivity.getIsLock()){
						menu.findItem(R.id.menu_book_hide).setVisible(false);
						menu.findItem(R.id.menu_book_unhide).setVisible(false);
						menu.findItem(R.id.menu_book_lock_hide).setVisible(true);
					}
					else {
						menu.findItem(R.id.menu_book_hide).setVisible(true);
						menu.findItem(R.id.menu_book_unhide).setVisible(false);
						menu.findItem(R.id.menu_book_lock_hide).setVisible(false);
					}
				}
				// end wendy
				menu.setHeaderTitle(mSelectedBook.getTitle());
				mBookcase.setCurrentBook(mSelectedBook.getCreatedTime());

				SharedPreferences msPreference = getActivity()
						.getSharedPreferences(MetaData.PREFERENCE_NAME,
								Context.MODE_MULTI_PROCESS);
				String sAsusAccount = msPreference.getString(this
						.getResources().getString(R.string.pref_AsusAccount),
						null);
				
				if(MetaData.isATT()){ //Carol
					menu.findItem(R.id.menu_book_sync).setVisible(false);
					menu.findItem(R.id.menu_book_async).setVisible(false);
				}else{
				if ((MetaData.CurUserAccount > 0) && !sAsusAccount.equals("")
						&& sAsusAccount != null) {

					Long userAccountId = 0L;
					Cursor cus = mContentResolver.query(MetaData.BookTable.uri,
							null, "created_date = ? ",
							new String[] { mSelectedBook.getCreatedTime()
									.toString() }, null);
					if (cus.getCount() > 0) {
						cus.moveToFirst();
						int culid = cus
								.getColumnIndex(MetaData.BookTable.USER_ACCOUNT);
						if (culid != -1) {
							userAccountId = cus.getLong(culid);
						}
					}
					cus.close();
					if (userAccountId > 0) {
						menu.findItem(R.id.menu_book_sync).setVisible(false);
						menu.findItem(R.id.menu_book_async).setVisible(true);
					} else {
						menu.findItem(R.id.menu_book_sync).setVisible(true);
						menu.findItem(R.id.menu_book_async).setVisible(false);
					}
				} else {
					menu.findItem(R.id.menu_book_sync).setVisible(true);
					menu.findItem(R.id.menu_book_async).setVisible(false);
				}
				}
			}
		}

	}

	private final DialogInterface.OnClickListener dialogClick = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (mWhichDialog) {
			case RENAME_DIALOG:
				if (mRenamedTitle != null
						&& mRenamedTitle.getText().length() != 0) {//mRenamedTitle.getText().toString().trim() != ""
					if (mSelectedBook != null) {
						mSelectedBook.setTitle(mRenamedTitle.getText()
								.toString());
						mSelectedBook.save();
						//darwin
						dataSetChangedUpdate();
						
						//Begin Allen for update widget 
						if(!MetaData.SuperNoteUpdateInfoSet.containsKey(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_RENAME_BOOK)){
							MetaData.SuperNoteUpdateInfoSet.put(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_RENAME_BOOK,null);
						}
						//End Allen
					}
				}
				break;
			case SET_PASSWORD_DIALOG:
				SettingActivity.setPassword(getActivity());
				break;
			}
			mWhichDialog = NO_DIALOG;
			mSelectedBook = null;
		}
	};

	private EditText mRenamedTitle;
	private int mWhichDialog = 0;
	private static final int NO_DIALOG = 0;
	private static final int RENAME_DIALOG = 1;
	private static final int HIDE_DIALOG = 2;
	private static final int PROPERTY_DIALOG = 5;
	private static final int SET_PASSWORD_DIALOG = 6;

	private void showRenameDialog(AdapterView.AdapterContextMenuInfo menuInfo) {
		NoteBook noteBook = (NoteBook) mAdapter.getItem(menuInfo.position );

		mWhichDialog = RENAME_DIALOG;
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View view = View.inflate(getActivity(),
				R.layout.notebook_rename_dialog, null);
		mRenamedTitle = (EditText) view.findViewById(R.id.rename_title);
		TextViewUtils.enableCapSentences(mRenamedTitle);
		mRenamedTitle.setText(noteBook.getTitle()); //smilefish
		mRenamedTitle.selectAll(); //smilefish
		builder.setTitle(R.string.rename);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, dialogClick);
		builder.setNegativeButton(android.R.string.cancel, null);
		AlertDialog dialog = builder.create();
		DialogUtils.forcePopupSoftInput(dialog);//noah
		dialog.show();
		
		//smilefish fix bug 596655
		final Button button = dialog.getButton(Dialog.BUTTON_POSITIVE);
		mRenamedTitle.addTextChangedListener(new TextWatcher(){

			@Override
            public void afterTextChanged(Editable arg0) {
				String str = mRenamedTitle.getText().toString();
	            if(str.isEmpty()){
	            	button.setEnabled(false);
	            }else{
	            	button.setEnabled(true);
	            }
            }

			@Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3) {
            }

			@Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                    int arg3) {
            }
			
		});
	}

	private void showDeleteDialog(AdapterView.AdapterContextMenuInfo menuInfo) {
		deleteBook(menuInfo);
	}

	private void showPropertyDialog(AdapterView.AdapterContextMenuInfo menuInfo) {
		NoteBook noteBook = (NoteBook) mAdapter.getItem(menuInfo.position );
		mWhichDialog = PROPERTY_DIALOG;
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View view = View.inflate(getActivity(), R.layout.notebooks_property,
				null);
		TextView bookName = (TextView) view.findViewById(R.id.nb_info_name);
		TextView bookSize = (TextView) view.findViewById(R.id.nb_info_pageSize);
		TextView bookColor = (TextView) view
				.findViewById(R.id.nb_info_pagecolor);
		TextView bookStyle = (TextView) view.findViewById(R.id.nb_info_style);
		bookName.setText(noteBook.getTitle());
		String bookSizeStr = null;
		String bookColorStr = null;
		String bookStyleStr = null;

		switch (mSelectedBook.getPageSize()) {
		case MetaData.PAGE_SIZE_PAD:
			bookSizeStr = mResources.getStringArray(R.array.pg_size)[0];
			break;
		case MetaData.PAGE_SIZE_PHONE:
			bookSizeStr = mResources.getStringArray(R.array.pg_size)[1];
			break;
		}
		switch (mSelectedBook.getBookColor()) {
		case MetaData.BOOK_COLOR_WHITE:
			bookColorStr = mResources.getString(R.string.white);
			break;
		case MetaData.BOOK_COLOR_YELLOW:
			bookColorStr = mResources.getString(R.string.yellow);
			break;
		}
		switch (mSelectedBook.getGridType()) {
		case MetaData.BOOK_GRID_GRID:
			bookStyleStr = mResources.getString(R.string.book_grid_grid);
			break;
		case MetaData.BOOK_GRID_LINE:
			bookStyleStr = mResources.getString(R.string.book_grid_line);
			break;
		//Begin Allen ++ for todo and blank template
		case MetaData.BOOK_GRID_BLANK:
			bookStyleStr = mResources.getString(R.string.book_grid_none);
			break;
		//End Allen
		}

		bookSize.setText(bookSizeStr);
		bookColor.setText(bookColorStr);
		bookStyle.setText(bookStyleStr);
		builder.setTitle(R.string.nb_property);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, dialogClick);
		Dialog dialog = builder.create();
		dialog.show();
	}

	public void exportPagesToPdf(AdapterView.AdapterContextMenuInfo menuInfo) {
		final SortedSet<SimplePageInfo> pages = new TreeSet<SimplePageInfo>(
				new PageComparator());
		Long bookId = mSelectedBook.getCreatedTime();
		for (int i = 0; i < mSelectedBook.getTotalPageNumber(); ++i) {
			Long pageId = mSelectedBook.getPageOrder(i);
			SimplePageInfo info = new SimplePageInfo(bookId, pageId, 0, i);
			pages.add(info);
		}
		NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
		activity.exportNoteToPdf(getActivity(), pages, mSelectedBook.getTitle());
	}

	public void exportPagesToPdf() {
		NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
		if (activity != null) {
			activity.exportNoteToPdf(getActivity(), mAdapter.getSelectList());
		}
	}

	// begin wendy
	private void showUnhideDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		boolean hasPassword = mPreference.getBoolean(
				mResources.getString(R.string.pref_has_password), false);
		if (hasPassword) {
			mWhichDialog = HIDE_DIALOG;
			builder.setTitle(R.string.move_out_from_personal_title);
			// BEGIN: archie_huang@asus.com
			builder.setMessage(R.string.move_out_from_personal_content);
			// END: archie_huang@asus.com
			builder.setPositiveButton(android.R.string.ok, dialogClick);
			builder.setNegativeButton(android.R.string.cancel, null);
			final AlertDialog dialog = builder.create();
			dialog.show();
			Button positiveButton = dialog
					.getButton(AlertDialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mSelectedBook.setIsLocked(false);

					NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
					activity.updateFragment();
					dialog.dismiss();
					
					//Begin Allen for update widget 
					if(!MetaData.SuperNoteUpdateInfoSet.containsKey(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_UNLOCK_BOOK)){
						MetaData.SuperNoteUpdateInfoSet.put(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_UNLOCK_BOOK,null);
					}
					//End Allen
				}
			});
		} else {
			SettingActivity.setPassword(getActivity());
		}
	}

	// end wendy
	
	//begin smilefish
	private void resetLockSwitchStatus() {
			boolean isChecked = mSwitchLockUnlockSwitch.isChecked();
			mIsAutoChanged = true;
			mSwitchLockUnlockSwitch.setChecked(!isChecked); 
	}
	//end smilefish
	
	//begin darwin
	private void showUnhideDialog_edit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		boolean hasPassword = mPreference.getBoolean(
				mResources.getString(R.string.pref_has_password), false);
		if (hasPassword) {
			mWhichDialog = HIDE_DIALOG;
			builder.setTitle(R.string.move_out_from_personal_title);
			builder.setMessage(R.string.move_out_from_personal_content);
			builder.setPositiveButton(android.R.string.ok, dialogClick);
			builder.setNegativeButton(android.R.string.cancel, null);
			final AlertDialog dialog = builder.create();
			dialog.show();
			Button positiveButton = dialog
					.getButton(AlertDialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					List<Long> list = mAdapter.getSelectList();
					for(Long bookId : list)
					{
						NoteBook book = mBookcase.getNoteBook(bookId);
						if(!book.getIsLocked())
						{
							continue;
						}
						book.setIsLocked(false);
					}
					NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
					activity.updateFragment_edit();
					mIsPositiveButtonClicked = true;
					dialog.dismiss();
				}
			});
			//end smilefish
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface arg0) {
					if(mIsPositiveButtonClicked){
						mIsPositiveButtonClicked = false;
					}else{
						resetLockSwitchStatus(); //smilefish
					}
					mWhichDialog = NO_DIALOG;
				}
			});
		} else {			
			//begin smilefish
			Dialog dialog = SettingActivity.setPassword(getActivity());
			dialog.setOnDismissListener(new Dialog.OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface arg0) {
					resetLockSwitchStatus(); 
				}
				
			});
			//end smilefish
		}
	}
	private void showHideDialog_edit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		boolean hasPassword = mPreference.getBoolean(
				mResources.getString(R.string.pref_has_password), false);
		if (hasPassword) {
			mWhichDialog = HIDE_DIALOG;
			View view = View.inflate(getActivity(), R.layout.one_msg_dialog,
					null);
			TextView textView = (TextView) view
					.findViewById(R.id.msg_text_view);
			textView.setText(R.string.move_to_personal_content);
			builder.setTitle(R.string.move_to_personal_title);
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok, dialogClick);
			builder.setNegativeButton(android.R.string.cancel, null);
			final AlertDialog dialog = builder.create();
			dialog.show();
			Button positiveButton = dialog
					.getButton(AlertDialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					List<Long> list = mAdapter.getSelectList();
					for(Long bookId : list)
					{
						NoteBook book = mBookcase.getNoteBook(bookId);
						if(book.getIsLocked())
						{
							continue;
						}
						book.setIsLocked(true);
						if (NoteBookPickerActivity.islocked()) {
							NoteBook nextSelectedBook = mBookcase
									.getNextBook(book);
							if (nextSelectedBook != null) {
								mBookcase.setCurrentBook(nextSelectedBook
										.getCreatedTime());
								if (deviceType > 100) {
									changeData(nextSelectedBook.getCreatedTime());
								}
							} else {
								mBookcase.setCurrentBook(BookCase.NO_SELETED_BOOK);
								if (deviceType > 100) {
									changeData(BookCase.NO_SELETED_BOOK);
								}
							}
						}
					}
					NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
					//darwin
					if(NoteBookPickerActivity.getIsLock())
					{
						mAdapter.clearSelectList();
						setSelectionCountTitle(0);	
						
						//begin smilefish
						setMenuVisible(R.id.menu_edit_group_book, false);
						setMenuVisible(R.id.menu_lock_group, false);

						//end smilefish
					}
					//darwin
					activity.updateFragment_edit();
					mIsPositiveButtonClicked = true;
					dialog.dismiss();
				}
			});
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if(mIsPositiveButtonClicked){//smilefish fix bug 789845
						mIsPositiveButtonClicked = false;
					}else{
						resetLockSwitchStatus(); //smilefish
					}
					mWhichDialog = NO_DIALOG;
				}
			});
		} else {
			//begin smilefish
			Dialog dialog = SettingActivity.setPassword(getActivity());
			dialog.setOnDismissListener(new Dialog.OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface arg0) {
					resetLockSwitchStatus(); 
				}
				
			});
			//end smilefish
		}
	}
	//end   darwin
	public void showHideDialog() {
		mShowLockDialogForOneBook = false;
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		boolean hasPassword = mPreference.getBoolean(
				mResources.getString(R.string.pref_has_password), false);
		if (hasPassword) {
			// using one_msg_dialog
			mWhichDialog = HIDE_DIALOG;
			View view = View.inflate(getActivity(), R.layout.one_msg_dialog,
					null);
			TextView textView = (TextView) view
					.findViewById(R.id.msg_text_view);
			textView.setText(R.string.move_to_personal_content);
			if (NoteBookPickerActivity.islocked()) {
				builder.setTitle(R.string.nb_lock_hide);
			}
			else {
				builder.setTitle(R.string.move_to_personal_title);
			}
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok, dialogClick);
			builder.setNegativeButton(android.R.string.cancel, null);
			final AlertDialog dialog = builder.create();
			dialog.show();
			Button positiveButton = dialog
					.getButton(AlertDialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// begin wendy

					mSelectedBook.setIsLocked(true);
					if (NoteBookPickerActivity.islocked()) {
						NoteBook nextSelectedBook = mBookcase
								.getNextBook(mSelectedBook);

						if (nextSelectedBook != null) {
							mBookcase.setCurrentBook(nextSelectedBook
									.getCreatedTime());
							if (deviceType > 100) {
								changeData(nextSelectedBook.getCreatedTime());
							}
						} else {
							mBookcase.setCurrentBook(BookCase.NO_SELETED_BOOK);
							if (deviceType > 100) {
								changeData(BookCase.NO_SELETED_BOOK);
							}
						}
						
					}
					
					//Begin Allen for update widget 
					MetaData.updateSuperNoteUpdateInfoSet(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_LOCK_BOOK,mSelectedBook.getCreatedTime());
					//End Allen
					
					// end wendy
					NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
					activity.updateFragment();
					dialog.dismiss();
				}
			});
		} else {
			//begin smilefish
			Dialog dialog = SettingActivity.setPassword(getActivity());
			dialog.setOnDismissListener(new Dialog.OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface arg0) {
					boolean hasPassword = mPreference.getBoolean(
							mResources.getString(R.string.pref_has_password), false);
					if (hasPassword) {
						mShowLockDialogForOneBook = true;
					}
				}
				
			});
			//end smilefish
		}
	}
	
	//begin smilefish
	public boolean isShowLockDialogForOneBook(){
		return mShowLockDialogForOneBook;
	}
	//end smilefish

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo;
		menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		//begin darwin
		case R.id.menu_book_editcover:
			//emmanual to fix bug 569009
			NoteBook book = (NoteBook) mAdapter.getItem(menuInfo.position);
			int index = mBookcase.getBookList().indexOf(book);
			mPreference.edit().putInt(MetaData.LAST_EDITED_BOOK_INDEX, index).commit();
			
			mActivity.editBookCoverDialog(book, true);
			break;
		//end   darwin
		case R.id.menu_book_rename:
			showRenameDialog(menuInfo);
			break;
		//BEGIN: RICHARD
		case R.id.menu_book_hwr:
			showBookHWRDialog(menuInfo);
			break;
		//END: RICHARD
		case R.id.menu_book_hide:
			showHideDialog();
			break;
		// begin wendy
		case R.id.menu_book_unhide:
			showUnhideDialog();
			// end wendy
			break;
		case R.id.menu_book_lock_hide:
			showHideDialog();
			break;
		case R.id.menu_book_delete:
			showDeleteDialog(menuInfo);
			break;
		case R.id.menu_book_export:
			exportPage();
			break;
		// BEGIN: Better
		case R.id.menu_book_export_to_pdf:
			exportPagesToPdf(menuInfo);
			break;
		// END: Better
		case R.id.menu_book_property:
			showPropertyDialog(menuInfo);
			break;
		case R.id.menu_book_sync:
			// BEGIN:shaun_xu@asus.com
			mPreference = getActivity().getSharedPreferences(
					MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
			String sAsusAccount = mPreference.getString(this.getResources()
					.getString(R.string.pref_AsusAccount), null);
			String sAsusPassword = mPreference.getString(this.getResources()
					.getString(R.string.pref_AsusPassword), null);
			if (sAsusAccount != null && !sAsusAccount.equals("")
					&& sAsusPassword != null && !sAsusPassword.equals("")) {
				if (NoteBookPickerActivity.isNetworkConnected(getActivity())) {
					if (mBookcase.getCurrentBook().getVersion() == 1) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setPositiveButton(android.R.string.yes, null);
						builder.setCancelable(false);
						builder.setNegativeButton(android.R.string.no, null);
						final AlertDialog dialog = builder.create();
						dialog.setMessage(getActivity().getString(R.string.prompt_transfer_format));
						dialog.show();
						dialog.getButton(Dialog.BUTTON_POSITIVE)
								.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										long bookId = mBookcase
												.getCurrentBookId();
										if (!MetaData.TransferringBookIdList
												.contains(bookId)) {
											ArrayList<Long> bookIds = new ArrayList<Long>();
											bookIds.add(bookId);
											mActivity.changeBookStatus(
													mActivity, bookIds, null);
										}
										dialog.dismiss();
									}
								});
					} else {
						long bookId = mBookcase.getCurrentBookId();
						if (!MetaData.TransferringBookIdList.contains(bookId)) {
							ArrayList<Long> bookIds = new ArrayList<Long>();
							bookIds.add(bookId);
							mActivity
									.changeBookStatus(mActivity, bookIds, null);
						}
					}
				} else {
					Toast toast = Toast.makeText(getActivity(),
							R.string.sync_setting_networkless, Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
					toast.show();
					
				}
			} else {
				try {
					((NoteBookPickerActivity)getActivity()).showLoginDialog(NoteBookPickerActivity.PICK_OTHERS, "NoteBookPickerActivity");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mSearchString = "";//RICHARD
			}
			// END:shaun_xu@asus.com
			break;
		case R.id.menu_book_async: //modify by Carol- add sync changes confirm dialog for prompt
			long bookId = mBookcase.getCurrentBookId();
			if(NoteBookPickerActivity.isNetworkConnected(getActivity()))
			{
				String PREFERNECE_NAME = "SuperNote";
				mResources = getActivity().getResources();
				mPreference = getActivity().getSharedPreferences(PREFERNECE_NAME, Activity.MODE_PRIVATE);//Context.MODE_MULTI_PROCESS);
		        boolean showHint = mPreference.getBoolean(mResources.getString(R.string.pref_hide_syncpompt), true);
		        
		    	if(showHint){
			        //FragmentManager fm = getFragmentManager();
		    		Bundle b = new Bundle();
		            //display the confirm dialog
					SyncCancelDialogFragment dialogFragment = SyncCancelDialogFragment.newInstance(b);
					mValues = new String[1];
					mValues[0]= mBookcase.getCurrentBook().getTitle();
					dialogFragment.setListener(this);
					dialogFragment.show(getFragmentManager(), SyncCancelDialogFragment.TAG);
		    	}else{
		    		removeAsyncBook(bookId);
		    	}
	    	}else
			{
				Toast toast = Toast.makeText(getActivity(),
						R.string.sync_setting_networkless, Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
				toast.show();
			}
				
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onDataChange(int count) {
		setMaskVisible((count == 0) ? true : false);
	}

	@Override
	public void onSelectedDataChange(int count) {
		setSelectionCountTitle(count);
		setMenuVisible(R.id.menu_edit_group_book, (count != 0));
		setMenuVisible(R.id.menu_lock_group, (count != 0));

		//begin smilefish
		List<Long> list = mAdapter.getSelectList();
		if (count == 1 && mMenuState == EDIT_MENU && mMenu != null) {
			NoteBook book = mBookcase.getNoteBook(list.get(0));
			if (book != null) {

				boolean isLocked = book.getIsLocked();
				if (isLocked != mSwitchLockUnlockSwitch.isChecked()) {
					mIsAutoChanged = true;
					mSwitchLockUnlockSwitch.setChecked(isLocked || isHideDialogShowing());
				}

			}
		}
		//end smilefish
		if (mSwitchLockUnlockSwitch != null) {
			boolean mAllSelectedLocked = true;
			for (Long l : list) {
				NoteBook book = mBookcase.getNoteBook(l);
				if (book != null && !book.getIsLocked()) {
					mAllSelectedLocked = false;
					break;
				}
			}
			mIsAutoChanged = true;
			if (mAllSelectedLocked || isHideDialogShowing()) {
				mSwitchLockUnlockSwitch.setChecked(true);
			} else {
				mSwitchLockUnlockSwitch.setChecked(false);
			}
			mIsAutoChanged = false;
		}
	}
	
	private boolean isHideDialogShowing(){
		return SettingActivity.isPasswordDialogShowing() || mWhichDialog == HIDE_DIALOG;
	}

	public boolean isActionMode() {
		return (mActionMode != null);
	}
    //BEGIN: RICHARD
    void traverseView(View view,int index)
    {
        if (view instanceof SearchView) {
            SearchView v = (SearchView) view;
            for(int i = 0; i < v.getChildCount(); i++) {
                traverseView(v.getChildAt(i), i);
            }
        } else if (view instanceof LinearLayout) {
            LinearLayout ll = (LinearLayout) view;
            for(int i = 0; i < ll.getChildCount(); i++) {
                traverseView(ll.getChildAt(i), i);
            }
        } else if (view instanceof EditText) {
            ((EditText) view).setTextColor(Color.YELLOW);
            ((EditText) view).setHintTextColor(Color.BLUE);
            ((EditText) view).setBackgroundColor(Color.TRANSPARENT);
        } else if (view instanceof TextView) {
            ((TextView) view).setTextColor(Color.YELLOW);
        } else {
            //Log.v("View Scout", "Undefined view type here...");
        }

    }
    //END: RICHARD

	@Override
    public void onPressPositive(){ //Carol-'OK' button in SyncCancelDialogFragment dialog
		long bookId = mBookcase.getCurrentBookId();
		removeAsyncBook(bookId);
    }
	
	@Override
	public void onPressNegative(){ //Carol-'Cancel' button in SyncCancelDialogFragment dialog
		//cancel
	}
	
	@Override
	public String[] onAdapterGetter(){
		return mValues;
	}
	
	public void removeAsyncBook(long bookId)
	{
		//ensure to remove the sync function of this book
		if (!MetaData.TransferringBookIdList.contains(bookId)) {
			ArrayList<Long> bookIds = new ArrayList<Long>();
			bookIds.add(bookId);
			mActivity.changeBookStatus(mActivity, null, bookIds);
		}
	}
}
