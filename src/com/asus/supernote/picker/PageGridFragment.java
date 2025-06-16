package com.asus.supernote.picker;

import java.util.SortedSet;
import java.util.TreeSet;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.inksearch.CFG;
import com.asus.updatesdk.ZenUiFamily;
import com.asus.updatesdk.utility.DeviceUtils;

public class PageGridFragment extends Fragment implements Callback, DataCounterListener {
    public static final String TAG = "PageGridFragment";
    public static final String ALIAS_TAG = "PersonalPageFragment";
    public static final String SORT_BY_PAGE = null;
    public static final String SORT_BY_MODIFIED = MetaData.PageTable.MODIFIED_DATE + " DESC";
    public static final int NORMAL_MENU = 0;
    public static final int EDIT_MENU = 1;

    private static final int SORT_BY_DATE_INDEX = 0;
    private static final int SORT_BY_MODIFIED_INDEX = 1;
    private BookCase mBookcase;
    private ContentResolver mContentResolver;
    private PageGridViewAdapter mAdapter;
    private Menu mMenu;

    private int deviceType;
    private boolean mIsPrivate;
    private boolean mIsPrivateBook = false;//darwin;
    private int mMenuState = NORMAL_MENU;
    private long mBookId = BookCase.NO_SELETED_BOOK;
    static private String mSortOrder = SORT_BY_PAGE;
    static private boolean mIsSortOrderChanged = false; //smilefish
    public String getSortOrder() {
		return mSortOrder;
	}

    private ActionMode mActionMode = null;

    //BEGIN: RICHARD
    public static String mSearchString = "";
    //END: RICHARD
    
    private SelectionMenu mSelectionMenu = null; //smilefish
    private int mSelectedPageCount = 0; //smilefish
    private SharedPreferences mPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceType = PickerUtility.getDeviceType(getActivity());
        if (deviceType > 100) {
        	//begin darwin
        	//NoteBookPickerActivity pa = (NoteBookPickerActivity) getActivity();
            //begin wendy
            mIsPrivate = NoteBookPickerActivity.islocked();//(pa.getDisplayType() == PickerActivity.DISPLAY_PERSONAL) ? true : false;
            //end wendy
          //end darwin
        }
        
        mPreference = getActivity().getSharedPreferences(
				MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);

		mSortOrder = mPreference.getString(
				getActivity().getResources().getString(R.string.pref_page_sort_type), 
				SORT_BY_PAGE);

        mBookcase = BookCase.getInstance(getActivity());
        mContentResolver = getActivity().getContentResolver();
        Cursor cursor = null;
        {
        	cursor = mContentResolver.query(MetaData.BookTable.uri, null, "((userAccount = 0) OR (userAccount = ?))", new String[]{Long.toString(MetaData.CurUserAccount)}, null);
            mBookId = (cursor.getCount() == 0) ? BookCase.NO_SELETED_BOOK : mBookcase.getCurrentBookId();
            mBookcase.setCurrentBook(mBookId);
            NoteBook currentBook = mBookcase.getCurrentBook();
            if (currentBook!=null) {
            	mIsPrivateBook = currentBook.getIsLocked();
            }
        }
        mAdapter = new PageGridViewAdapter(getActivity(), mBookId, mSortOrder, mIsPrivate);
        //darwin
        PageGridViewAdapter.resetPageGridProcessing();
        //darwin
        mAdapter.registerDataCounterListener(this);
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        changeData(mBookId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_grid_view, null);
        GridView gridView = (GridView) view.findViewById(R.id.page_gridview);
        gridView.setAdapter(mAdapter);
        gridView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        
        registerForContextMenu(gridView);

        return view;
    }

    public void changeData(Long bookId) {
        mBookId = bookId;
        mAdapter.changeSortOrder(mBookId, mSortOrder);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
            case R.id.menu_edit:
                getActivity().startActionMode(this);
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
        int option = 0;
        int mask = 0;
        switch (mMenuState) {
            case NORMAL_MENU:
                inflater.inflate(R.menu.picker_menu, menu);
                option = ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP;//smilefish
                mask = ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP;//darwin
                    
                bar.setDisplayOptions(option, mask);  
                
                Boolean isCnSku = DeviceUtils.checkCnSku();
                MenuItem item = menu.findItem(R.id.menu_zenFamily);
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
    }
    
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

    //Begin Allen
    public void SortNotePage(int type) {
            if (type == SORT_BY_DATE_INDEX) {
                mSortOrder = SORT_BY_PAGE;
            }
            else if (type == SORT_BY_MODIFIED_INDEX) {
                mSortOrder = SORT_BY_MODIFIED;
            }
            mAdapter.changeSortOrder(mBookId, mSortOrder);  
            
    		SharedPreferences.Editor editor = mPreference.edit();
    		editor.putString(getActivity().getResources().getString(R.string.pref_page_sort_type), 
    				mSortOrder);
    		editor.commit();
    }
    //End Allen
    public void dataSetChanged() {
        mAdapter.changeSortOrder(mBookId, mSortOrder);
    }

    public View getDraggedView() {
        return mAdapter.getDraggedView();
    }

    private void deletePages() {
        PickerActivity activity = (PickerActivity) getActivity();
        activity.confirmDeletePages(mBookcase.getNoteBook(mBookId).getSelectedItems());
    }

    private void copyPages(boolean isPrivate) {
        PickerActivity activity = (PickerActivity) getActivity();
        activity.selectCopyDestBook(mBookcase.getNoteBook(mBookId).getSelectedItems(), isPrivate);
    }

    private void movePages(boolean isPrivate) {
        PickerActivity activity = (PickerActivity) getActivity();
        activity.selectMoveDestBook(mBookcase.getNoteBook(mBookId), isPrivate);
    }

    public void setNormalModeMenu() {
        mMenuState = NORMAL_MENU;
        finishActionMode();
        mAdapter.setSelectAll(false);
        mAdapter.setEditMode(false);
        dataSetChanged();
        getActivity().invalidateOptionsMenu();
        if (deviceType > 100) {
            PickerActivity pickerActivity = (PickerActivity) getActivity();
            pickerActivity.setDisplaySelectorEnabled(true);

        }
    }

    public void exportPages() {
        final PickerActivity activity = (PickerActivity) getActivity();
        final NoteBook book = mBookcase.getNoteBook(mBookId);
        activity.exportNote(getActivity(), book.getSelectedItems(), book.getTitle());
    }
    
    // BEGIN: Better
    public void exportPages2Pdf() {
        final PickerActivity activity = (PickerActivity) getActivity();
        final NoteBook book = mBookcase.getNoteBook(mBookId);
        activity.exportNoteToPdf(getActivity(), book.getSelectedItems(), book.getTitle());
    }
    // END: Better

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

    public void setPrivateMode(boolean b) {
        mIsPrivate = b;
    }

    public boolean isPrivateMode() {
        return mIsPrivate;
    }

    @Override
    public void onStop() {
        super.onStop();

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
    	mAdapter.setDateAndTimeFormat();
    	
    	//modify by wendy fix bug id 30
        changeData(mBookId);
        //modify by wendy

		AllPageViewFragment.mSearchString = "";//RICHARD
		NoteBookPageGridFragment.mSearchString = "";//RICHARD
		//darwin
		PageGridViewAdapter.resetPageGridProcessing();
        //darwin
        super.onResume();
    }
	// END: Better

    public long getBookId() {
        return mBookId;
    }
    
    //begin smilefish for long press event
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if(!mAdapter.isEditMode()){
			super.onCreateContextMenu(menu, v, menuInfo);
			
			PickerActivity activity = (PickerActivity)getActivity();
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			if((info == null) || (info.position == 0))
			{
				return;
			}
			
			activity.getMenuInflater().inflate(R.menu.page_item_edit_menu, menu);
			NoteBook book = mBookcase.getNoteBook(mBookId);
			String title = String.format(getResources().getString(R.string.pg_in_notebook), 
					Integer.toString(info.position), book.getTitle());
			menu.setHeaderTitle(title);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo;
		menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        
		switch (item.getItemId()) {
		case R.id.menu_page_copy:
			copyPage(menuInfo);
			break;
		case R.id.menu_page_move:
			movePage(menuInfo);
			break;
		case R.id.menu_page_del:
			deletePage(menuInfo);
			break;
		case R.id.menu_page_reorder:
			reorderPage();
			break;
		case R.id.menu_page_share:
			showShareDialog(menuInfo);
			break;
		case R.id.menu_page_export:
			showExportDialog(menuInfo);	
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	private SortedSet<SimplePageInfo> getSelectPages(AdapterView.AdapterContextMenuInfo menuInfo){
		NoteBook book = mBookcase.getNoteBook(mBookId);
		Long pageId = mAdapter.getItemId(menuInfo.position);
        int pageIndex = book.getPageIndex(pageId);
        SimplePageInfo info = new SimplePageInfo(mBookId, pageId, 0, pageIndex);
        SortedSet<SimplePageInfo> mSelectedPages = new TreeSet<SimplePageInfo>(new PageComparator());
        mSelectedPages.add(info);
        return mSelectedPages;
	}
	
	private void copyPage(AdapterView.AdapterContextMenuInfo menuInfo){
		SortedSet<SimplePageInfo> mSelectedPages = getSelectPages(menuInfo);
		PickerActivity activity = (PickerActivity) getActivity();
		activity.selectCopyDestBook(mSelectedPages, mIsPrivateBook);
	}
	
	private void movePage(AdapterView.AdapterContextMenuInfo menuInfo){
		SortedSet<SimplePageInfo> mSelectedPages = getSelectPages(menuInfo);
		PickerActivity activity = (PickerActivity) getActivity();
		activity.selectMoveDestBook(mSelectedPages, mIsPrivateBook);
	}
	
	private void deletePage(AdapterView.AdapterContextMenuInfo menuInfo){
		SortedSet<SimplePageInfo> mSelectedPages = getSelectPages(menuInfo);
		PickerActivity activity = (PickerActivity) getActivity();
		activity.confirmDeletePages(mSelectedPages);
	}
	
	private void reorderPage(){
		PickerActivity activity = (PickerActivity) getActivity();
		activity.startActionMode(this);
	}
	
	private void showShareDialog(AdapterView.AdapterContextMenuInfo menuInfo){
		final SortedSet<SimplePageInfo> mSelectedPages = getSelectPages(menuInfo);
		final PickerActivity activity = (PickerActivity) getActivity();
		final NoteBook book = mBookcase.getNoteBook(mBookId);
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	View view = View.inflate(getActivity(), R.layout.share_actionitem_dialog, null);
    	ListView shareFormatLV=(ListView)view.findViewById(R.id.share_format_listview);
    	String[] formatText = getResources().getStringArray(R.array.share_format_array);
    	shareFormatLV.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.share_format_text, formatText));

    	builder.setTitle(R.string.share_title)
    			.setView(view);
    	final AlertDialog dialog = builder.create();
    	dialog.show();
    	
    	shareFormatLV.setOnItemClickListener(new OnItemClickListener(){
    		public void onItemClick(AdapterView<?> a, View v, int position, long id){
    			if(position == 0)
    				activity.shareAsImage(activity, mSelectedPages, book.getTitle());
    			else if(position ==1)
    				activity.shareAsPDF(activity, mSelectedPages, book.getTitle());
    			else if(position == 2)
    				activity.shareAsSupernoteFormat(activity, mSelectedPages, book.getTitle());
    			dialog.cancel();
    		}
    	});
	}
	
	private void showExportDialog(AdapterView.AdapterContextMenuInfo menuInfo){
		final SortedSet<SimplePageInfo> mSelectedPages = getSelectPages(menuInfo);
		final PickerActivity activity = (PickerActivity) getActivity();
		final NoteBook book = mBookcase.getNoteBook(mBookId);
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	View view = View.inflate(getActivity(), R.layout.export_actionitem_dialog, null);
    	ListView exportFormatLV=(ListView)view.findViewById(R.id.export_format_listview);
    	String[] formatText = getResources().getStringArray(R.array.export_format_array);
    	exportFormatLV.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.export_format_text, formatText));            	
    	builder.setTitle(R.string.export_as)
    			.setView(view)
    			.setNegativeButton(android.R.string.cancel, null);
    	final AlertDialog dialog = builder.create();
    	dialog.show();
    	//export to different versions when clicking on items
    	exportFormatLV.setOnItemClickListener(new OnItemClickListener(){
    		public void onItemClick(AdapterView<?> a, View v, int position, long id){
    			if(position == 0){
    				activity.exportNoteToPdf(activity, mSelectedPages, book.getTitle());
    				dialog.cancel();
				}else if(position ==1){
					activity.exportNote(activity, mSelectedPages, book.getTitle());
    				dialog.cancel();
				}
    		}
    	});
	}
	
	//end smilefish

    /********************************************************
     * Action Mode callback start
     *******************************************************/
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_page_del:
                deletePages();
                break;
            case R.id.menu_page_copy:
                copyPages(mIsPrivateBook);//darwin

                break;
            case R.id.menu_page_move:
                movePages(mIsPrivate);
                break;
            case R.id.menu_export: //Carol
            	{
	            	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	            	View view = View.inflate(getActivity(), R.layout.export_actionitem_dialog, null);
	            	ListView exportFormatLV=(ListView)view.findViewById(R.id.export_format_listview);
	            	String[] formatText = getResources().getStringArray(R.array.export_format_array);
	            	exportFormatLV.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.export_format_text, formatText));            	
	            	builder.setTitle(R.string.export_as)
	            			.setView(view)
	            			.setNegativeButton(android.R.string.cancel, null);
	            	final AlertDialog dialog = builder.create();
	            	dialog.show();
	            	//export to different versions when clicking on items
	            	exportFormatLV.setOnItemClickListener(new OnItemClickListener(){
	            		public void onItemClick(AdapterView<?> a, View v, int position, long id){
	            			if(position == 0){
	            				exportPages2Pdf();
	            				dialog.cancel();
            				}else if(position ==1){
	            				exportPages();
	            				dialog.cancel();
            				}
	            		}
	            	});
            	}
            	break;
            case R.id.menu_page_share: //Carol
            	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            	View view = View.inflate(getActivity(), R.layout.share_actionitem_dialog, null);
            	ListView shareFormatLV=(ListView)view.findViewById(R.id.share_format_listview);
            	String[] formatText = getResources().getStringArray(R.array.share_format_array);
            	shareFormatLV.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.share_format_text, formatText));

            	builder.setTitle(R.string.share_title)
            			.setView(view);
            	final AlertDialog dialog = builder.create();
            	dialog.show();
            	
            	shareFormatLV.setOnItemClickListener(new OnItemClickListener(){
            		public void onItemClick(AdapterView<?> a, View v, int position, long id){
            			if(position == 0)
            				sharePageAsImage();
            			else if(position ==1)
            				sharePageAsPDF();
            			else if(position == 2)
            			 	sharePageAsSupernoteFormat();
            			dialog.cancel();
            		}
            	});

            	break;
        }
        return true;
    }

    //Begin Carol
    public void sharePageAsImage(){
    	final PickerActivity activity = (PickerActivity) getActivity();
        final NoteBook book = mBookcase.getNoteBook(mBookId);
        activity.shareAsImage(getActivity(), book.getSelectedItems(), book.getTitle());
    }
    
    public void sharePageAsPDF(){
    	final PickerActivity activity = (PickerActivity) getActivity();
        final NoteBook book = mBookcase.getNoteBook(mBookId);
        activity.shareAsPDF(getActivity(), book.getSelectedItems(), book.getTitle());
    }
    
    public void sharePageAsSupernoteFormat(){
    	final PickerActivity activity = (PickerActivity) getActivity();
        final NoteBook book = mBookcase.getNoteBook(mBookId);
        activity.shareAsSupernoteFormat(getActivity(), book.getSelectedItems(), book.getTitle());
    }
    //End
    
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mActionMode = mode;
        mMenuState = EDIT_MENU;
        mAdapter.changeSortOrder(mBookId, mSortOrder);
        mAdapter.setEditMode(true);

        //begin smilefish fix bug 400968
        if(mSortOrder != SORT_BY_PAGE){
        	mSortOrder = SORT_BY_PAGE;
        	mIsSortOrderChanged = true;
        }
        dataSetChanged();
        Toast.makeText(getActivity(), R.string.prompt_move_pages, Toast.LENGTH_LONG).show();
        if (deviceType > 100) {
            PickerActivity pickerActivity = (PickerActivity) getActivity();
            pickerActivity.setDisplaySelectorEnabled(false);
            pickerActivity = null;
        }
        mode.getMenuInflater().inflate(R.menu.page_edit_menu, menu);
        setCustomActionBar(); //smilefish
        setSelectionCountTitle(0);
        mMenu = menu;
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        mMenu = null;
        if(mIsSortOrderChanged){
	        Toast.makeText(getActivity(), R.string.pg_sort_by_name, Toast.LENGTH_LONG).show();
	        mIsSortOrderChanged= false;
        }
        setNormalModeMenu();
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
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
        View customView = LayoutInflater.from(getActivity()).inflate(
                R.layout.book_edit_actionbar, null);
        mSelectionMenu = new SelectionMenu(getActivity(), 
        		(Button) customView.findViewById(R.id.book_select_menu));
        mSelectionMenu.addOuterListener(notifyOuter); //smilefish
        mActionMode.setCustomView(customView);	         
	}
	//end smilefish

    public void finishActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    public void setSelectionCountTitle(int count) {
        if (mActionMode != null) {
        	mSelectedPageCount = count;
            String msg = String.format(getResources().getString(R.string.notebook_selected), Integer.toString(count));
            //begin smilefish
			mSelectionMenu.setTitle(msg); 
			if(count == mAdapter.getCount() - 1)
				mSelectionMenu.updateSelectAllMode(true);
			else
				mSelectionMenu.updateSelectAllMode(false);
            //end smilefish
        }
    }
    
    //begin smilefish
    public void updateSelectionCountTitle()
    {
    	if (mActionMode != null) {
    		String msg = String.format(getResources().getString(R.string.notebook_selected), Integer.toString(mSelectedPageCount));
			mSelectionMenu.setTitle(msg); 
    	}
    }
    //end smilefish

    @Override
    public void onDataChange(int count) {
    }

    @Override
    public void onSelectedDataChange(int count) {
        setSelectionCountTitle(count);
        setMenuVisible(R.id.menu_edit_group, (count != 0));
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
}
