package com.uservoice.uservoicesdk.compatibility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.UserVoice;
import com.uservoice.uservoicesdk.activity.BaseActivity;
import com.uservoice.uservoicesdk.ui.ColorfulLinearLayout;
import com.uservoice.uservoicesdk.ui.Utils;

/**
 * <em>Copy from Android source to enable {@link Fragment} support.</em>
 *
 * @see ListActivity
 */
public abstract class FragmentListActivity extends BaseActivity {

    // changed to private as original suggested
    private ListAdapter mAdapter;
    // changed to private as original suggested
    private ListView mList;

    private Handler mHandler = new Handler();
    private boolean mFinishedStart = false;

    private Runnable mRequestFocus = new Runnable() {
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getListView().getItemAtPosition(position) if they need to access the data
     * associated with the selected item.
     *
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    protected void onListItemClick(ListView l, View v, int position, long id) {
    }

    /**
     * Ensures the list view has been created before Activity restores all of
     * the view states.
     *
     * @see Activity#onRestoreInstanceState(Bundle)
     */
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        ensureList();
        super.onRestoreInstanceState(state);
    }

    /**
     * Updates the screen state (current list and other views) when the content
     * changes.
     *
     * @see Activity#onContentChanged()
     */
    @Override
    public void onContentChanged() {
        super.onContentChanged();

        // changed references from com.android.internal.R to android.R.*
        View emptyView = findViewById(android.R.id.empty);
        mList = (ListView) findViewById(android.R.id.list);

        if (mList == null) {
            throw new RuntimeException("Your content must have a ListView whose id attribute is " + "'android.R.id.list'");
        }
        if (emptyView != null) {
            mList.setEmptyView(emptyView);
        }
        mList.setOnItemClickListener(mOnClickListener);
        if (mFinishedStart) {
            setListAdapter(mAdapter);
        }
        mHandler.post(mRequestFocus);
        mFinishedStart = true;
    }

    /**
     * Provide the cursor for the list view.
     */
    public void setListAdapter(ListAdapter adapter) {
        synchronized (this) {
            ensureList();
            mAdapter = adapter;
            mList.setAdapter(adapter);
            View view = findViewById(R.id.uf_sdk_no_network);
            if(view != null) mList.setEmptyView(view);
        }
    }

    /**
     * Set the currently selected list item to the specified position with the
     * adapter's data
     *
     * @param position
     */
    public void setSelection(int position) {
        mList.setSelection(position);
    }

    /**
     * Get the position of the currently selected list item.
     */
    public int getSelectedItemPosition() {
        return mList.getSelectedItemPosition();
    }

    /**
     * Get the cursor row ID of the currently selected list item.
     */
    public long getSelectedItemId() {
        return mList.getSelectedItemId();
    }

    /**
     * Get the activity's list view widget.
     */
    public ListView getListView() {
        ensureList();
        return mList;
    }

    /**
     * Get the ListAdapter associated with this activity's ListView.
     */
    public ListAdapter getListAdapter() {
        return mAdapter;
    }

    private void ensureList() {
        if (mList != null) {
            return;
        }
        setContentView(R.layout.uv_list_content);
    }
//Ed +++
    private int getStatusBarHeight() {
        final Display display = getWindowManager().getDefaultDisplay();
        if (display != null && display.getDisplayId() != Display.DEFAULT_DISPLAY) {
            return 0;
        }
        int h = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            h = getResources().getDimensionPixelSize(resourceId);
        }
        return h;
    }

    private int getActionBarHeight() {
        int h = 0;
        TypedValue tv = new TypedValue();
        getBaseContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        h = getResources().getDimensionPixelSize(tv.resourceId);
        return h;
    }

    ColorfulLinearLayout mLinearLayout = null;
    TextView mTextViewColorful = null;

    // Please make sure the outer linear layout is created and attached to window before inflating target content view
    private void createColorfulLayoutIfNeeded() {
        if (mLinearLayout == null) {
            mLinearLayout = new ColorfulLinearLayout(this);
            mLinearLayout.setOrientation(LinearLayout.VERTICAL);
            // IMPORTANT: use MATCH_PARENT to extend layout in both directions
            mLinearLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }
    @SuppressLint("NewApi")
    private ViewGroup relayoutContent(View view) {
        mLinearLayout.removeAllViews();
        if(getResources().getIdentifier("windowTranslucentStatus", "attr", "android") != 0){
            if (mTextViewColorful == null) {
                mTextViewColorful = new TextView(this);
                int statusH = getStatusBarHeight();
                int actionbarH = getActionBarHeight();
                mTextViewColorful.setHeight(statusH + actionbarH);
                if(!Utils.isSimilarToWhite(UserVoice.sColor))
                    mTextViewColorful.setBackgroundColor(UserVoice.sColor);
                else
                    mTextViewColorful.setBackgroundColor(Color.BLACK);
                getActionBar().setBackgroundDrawable(new ColorDrawable(UserVoice.sColor));
            }
            mLinearLayout.addView(mTextViewColorful);
        }else{
            getActionBar().setBackgroundDrawable(new ColorDrawable(UserVoice.sColor));
        }
        if(Build.VERSION.SDK_INT >= 21){
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(255, 254, 254, 254)));
        }
        mLinearLayout.addView(view);
        return (ViewGroup) mLinearLayout;
    }
    @Override
    public void setContentView(int layoutResID) {
        // IMPORTANT: create colorful layout before inflating the view
        createColorfulLayoutIfNeeded();
        View view = getLayoutInflater().inflate(layoutResID, mLinearLayout, false);
        super.setContentView(relayoutContent(view));
    }
    @Override
    public void setContentView(View view, LayoutParams params) {
        // IMPORTANT: create colorful layout before inflating the view
        createColorfulLayoutIfNeeded();
        super.setContentView(relayoutContent(view), params);
    }
    @Override
    public void setContentView(View view) {
        // IMPORTANT: create colorful layout before inflating the view
        createColorfulLayoutIfNeeded();
        super.setContentView(relayoutContent(view));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // update height of the colorful view
        if(mTextViewColorful != null) {
            int statusH = getStatusBarHeight();
            int actionbarH = getActionBarHeight();
            mTextViewColorful.setHeight(statusH + actionbarH);
        }
    }
//Ed ---
    private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            onListItemClick((ListView) parent, v, position, id);
        }
    };

    @SuppressLint("NewApi")
    public void showSearch() {
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.uv_view_flipper);
        if (viewFlipper!=null){
        viewFlipper.getChildAt(1).setPaddingRelative(0, getActionBarHeight(), 0, 0);
        }
        super.showSearch();
    }
}