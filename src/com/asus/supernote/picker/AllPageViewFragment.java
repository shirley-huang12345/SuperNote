package com.asus.supernote.picker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.editable.NoteFrameLayout;
import com.asus.supernote.inksearch.CFG;
import com.asus.updatesdk.ZenUiFamily;
import com.asus.updatesdk.utility.DeviceUtils;

public class AllPageViewFragment extends Fragment implements Callback, DataCounterListener {
    public static final String TAG = "AllPageViewFragment";

    private static final int NORMAL_MENU = 0;
    private static final int EDIT_MENU = 1;
    private static final int NO_MENU = 2;

    private BookCase mBookcase;
    private AllPageViewAdapter mAdapter;
    private TextView mListMask;
    private Menu mMenu;
    private ActionMode mActionMode = null;
    private int mMenuState = NORMAL_MENU;
    private boolean mIsShowBookmark = false;
    
    //BEGIN: RICHARD
    private MenuItem mSearchItem = null;
    public static String mSearchString = "";
    //END: RICHARD

    private SelectionMenu mSelectionMenu = null; //smilefish

//BEGIN: RICHARD MODIFY FOR MULTIWINDOW
    private int currentWidth = -5;
    private NoteFrameLayout.OnMeasureListener mOnNoteFrameLayoutSizeChaneListener = new NoteFrameLayout.OnMeasureListener() {
        @Override
        public void onMeasure(int widthMeasure, int heightMeasure, int width,
                int height) {
        	if(currentWidth != widthMeasure)
        	{
        		if(mAdapter != null)
        		{
        			currentWidth = widthMeasure;
        			mAdapter.calculateItemPerRow(currentWidth);
        		}
        	}
        }
    };
//END: RICHARD MODIFY FOR MULTIWINDOW    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBookcase = BookCase.getInstance(getActivity());
        NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
        mIsShowBookmark = (activity.getDisplayType() == NoteBookPickerActivity.DISPLAY_BOOKMARK) ? true : false;
        mAdapter = new AllPageViewAdapter(activity, this, mIsShowBookmark);
        mAdapter.registerDataCounterListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_page_list, null);
        ListView listView = (ListView) view.findViewById(R.id.page_list);
//BEGIN: RICHRD MODIFY FOR MULTIWINDOW
        NoteFrameLayout noteFrameLayout = (NoteFrameLayout)view.findViewById(R.id.all_page_noteframelayout);
        noteFrameLayout.setOnSizeChangeListner(mOnNoteFrameLayoutSizeChaneListener);
//END: RICHRD MODIFY FOR MULTIWINDOW
        mListMask = (TextView) view.findViewById(R.id.listView_mask);
        listView.setAdapter(mAdapter);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
    	NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
    	if(activity.getDisplayType() == NoteBookPickerActivity.DISPLAY_ALL && mActionMode != null){
    		mActionMode.finish(); //smilefish fix bug 521488
    	}
    	
    	mAdapter.setDateAndTimeFormat();

		PageGridFragment.mSearchString = "";//RICHARD
		NoteBookPageGridFragment.mSearchString = "";//RICHARD
    	
		
        super.onResume();
        try{//emmanual to fix bug 470190
	        mAdapter.changeData();
	        mAdapter.notifyDataSetChanged();
        }catch(Exception ex){
        	
        }
        
        //darwin
		AllPageViewAdapter.resetNotebookAllPageProcessing();
		//darwin
		
		if(mActionMode != null)
			activity.setDrawerEnabled(false); //smilefish fix bug 563458/563530
		else
			activity.setDrawerEnabled(true);
    }

    //darwin
    public void onDoResume()
    {
    	if(mAdapter != null)
    	{
    		mAdapter.changeData();
    		mAdapter.notifyDataSetChanged();
    	}
    	else
    	{
    		Log.i("onDoResume", "    mAdapter == null ");
    	}
    }
    //darwin
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                getActivity().onBackPressed();
                break;
            case R.id.menu_edit:
                getActivity().startActionMode(this);
                NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();//smilefish
                activity.showStateBar(false); //smilefish
                break;
            default:
                break;
        }
        return true;
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
                menuId = R.menu.allpages_edit_menu;
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
        	Boolean isCnSku = DeviceUtils.checkCnSku();
            MenuItem item = null;
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
        
        // BEGIN: Shane_Wang 2012-10-15
        if(CFG.getCanDoVO() == false && !MetaData.TEXT_SEARCH_ENABLE) {
        	MenuItem itemSearch = menu.findItem(R.id.menu_search);
			if (itemSearch != null) {
				itemSearch.setVisible(false);
			}
        }
        // END: Shane_Wang 2012-10-15
    }
    
    //begin smilefish fix bug 408299/408281
    @Override 
    public void onPrepareOptionsMenu(Menu menu){ 
    	super.onPrepareOptionsMenu(menu);
    	
    	NoteBookPickerActivity noteBookPickerActivity = (NoteBookPickerActivity)getActivity();
    	boolean drawerOpen = noteBookPickerActivity.isNavigationDrawerShown();
    	menu.setGroupVisible(Menu.NONE, !drawerOpen);

		MenuItem sort = menu.findItem(R.id.menu_sort);
		if (sort != null) {
			sort.setVisible(false);
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
		MenuItem item = menu.findItem(R.id.menu_zenFamily);
		if(isCnSku && item != null)
			item.setVisible(false);
		
		item = menu.findItem(R.id.menu_encourageUs);
		if(isCnSku && item != null)
			item.setVisible(false);
    }
    //end smilefish

    public void setNormalModeMenu() {
        mMenuState = NORMAL_MENU;
        finishActionMode();
        mAdapter.clearSelectedItems();
        mAdapter.setSelectedAll(false);
        mAdapter.setItemSelectable(false);
        mAdapter.requeryData();
        mAdapter.changeData();
        NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
        activity.setDrawerEnabled(true);
        activity.invalidateOptionsMenu();

    }

    public void deletePages() {
        NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
        activity.confirmDeletePages(mAdapter.getSelectedItems());
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void removeBookmarks() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setTitle(R.string.remove_from_jump);
        builder.setMessage(R.string.remove_from_list2);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (SimplePageInfo pageInfo : mAdapter.getSelectedItems()) {
                    NoteBook note = mBookcase.getNoteBook(pageInfo.bookId);
                    NotePage page = note.getNotePage(pageInfo.pageId);
                    page.setBookmark(false);
                }
                mAdapter.changeData();
                mAdapter.clearSelectedItems();
                setNormalModeMenu();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        Dialog dialog = builder.create();
        dialog.show();

    }
    
    //emmanual to fix bug 438156
	public void chooseExport() {
		Context mContext = (NoteBookPickerActivity) getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		View view = View.inflate(mContext, R.layout.share_actionitem_dialog, null);
		ListView shareFormatLV = (ListView) view.findViewById(R.id.share_format_listview);

		String[] exportText = {mContext.getResources().getString(R.string.export_pdf),
		        mContext.getResources().getString(R.string.export_supernote_format) };
		shareFormatLV.setAdapter(new ArrayAdapter<String>(mContext,
		        R.layout.share_format_text, exportText));

		builder.setTitle(R.string.export).setView(view);
		final AlertDialog dialog = builder.create();
		dialog.show();

		shareFormatLV.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position,
			        long id) {
				if (position == 0) {
					exportPages2Pdf();
				} else if (position == 1) {
					exportPages();
				}
				dialog.cancel();
			}
		});
	}

    public void exportPages() {
        NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
        activity.exportNote(getActivity(), mAdapter.getSelectedItems());
        setNormalModeMenu();
    }

    public void dataSetChange() {
        mAdapter.changeData();
    }

    public void setMenuGroupVisible(int id, boolean b) {
        if (mMenu != null) {
            mMenu.setGroupVisible(id, b);
        }
    }

    public void setMenuItemVisible(int id, boolean b) {
        if (mMenu != null) {
            MenuItem item = mMenu.findItem(id);
            if (item != null) {
                item.setVisible(b);
            }
        }
    }

    public void clearSelectedItems() {
        if (mAdapter != null) {
            mAdapter.clearSelectedItems();
        }
    }

    public void setMaskVisibleAndText(boolean b, int stringId) {
        if (mListMask != null) {
            String msg = getResources().getString(stringId);
            mListMask.setVisibility(b ? View.GONE : View.VISIBLE);
            mListMask.setText(msg);
        }
    }

    @Override
    public void onDestroy() {
        setHasOptionsMenu(false);
        mAdapter.closeCursor();
        mAdapter.removeDataCounterListener();
        super.onDestroy();
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_page_del:
            	if(mIsShowBookmark){
            		removeBookmarks();
            	}else{
            		deletePages();
            	}
                break;
            case R.id.menu_page_export:
                exportPages();
                break;
				//begin wendy
            case R.id.menu_page_export_pdf:
            	exportPages2Pdf();
            	break;
				//end wendy
        }
        return true;
    }
    // BEGIN: wendy
    public void exportPages2Pdf() {
        final NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
       // final NoteBook book = mBookcase.getNoteBook(mBookId);        
        activity.exportNoteToPdf(getActivity(), mAdapter.getSelectedItems(), "export2pdf");
    }
    // END: wendy

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mMenuState = EDIT_MENU;
        mAdapter.setItemSelectable(true);
        mAdapter.setSelectedAll(false);
        mAdapter.changeData();//wendy
        mAdapter.notifyDataSetChanged();
        NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
        activity.setDrawerEnabled(false);
        mode.getMenuInflater().inflate(R.menu.allpages_edit_menu, menu);

        mActionMode = mode;
        mMenu = menu;

        setCustomActionBar(); //smilefish
        setSelectionCountTitle(0);       

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        mMenu = null;
        setNormalModeMenu();
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }
    
	//begin smilefish	
	SelectionMenu.INotifyOuter notifyOuter =new SelectionMenu.INotifyOuter(){

		@Override
		public void setSelectAll(boolean flag) {
			mAdapter.setSelectedAll(flag);
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
        
        NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();//smilefish
        activity.showStateBar(true); //smilefish
    }

    public void setSelectionCountTitle(int count) {
        if (mActionMode != null) {
            String msg = String.format(getResources().getString(R.string.notebook_selected), Integer.toString(count));
			//begin smilefish
            mSelectionMenu.setTitle(msg); 
            if(count == mAdapter.getPageCount())
				mSelectionMenu.updateSelectAllMode(true);
            else
				mSelectionMenu.updateSelectAllMode(false);
			//end smilefish
        }
    }


    @Override
    public void onDataChange(int count) {
        setMaskVisibleAndText(!(count == 0), mIsShowBookmark ? R.string.no_bookmark_exists : R.string.all_page_view_empty);

    }

    @Override
    public void onSelectedDataChange(int count) {
        setSelectionCountTitle(count);
        if (mIsShowBookmark) {
        	setMenuGroupVisible(R.id.menu_edit_group_allpage, (count != 0));
        	setMenuItemVisible(R.id.menu_page_export_pdf, false);
        }
        else {
		   //begin wendy
            setMenuGroupVisible(R.id.menu_edit_group_allpage, (count != 0));
        	//end wendy
		}
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
