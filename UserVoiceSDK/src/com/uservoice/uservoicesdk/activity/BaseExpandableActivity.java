package com.uservoice.uservoicesdk.activity;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.UserVoice;
import com.uservoice.uservoicesdk.ui.ColorfulLinearLayout;
import com.uservoice.uservoicesdk.ui.Utils;

/**
 * <em>Copy from Android source to enable {@link Fragment} support.</em>
 *
 * @see ListActivity
 */
public abstract class BaseExpandableActivity extends BaseActivity {

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
        if(getResources().getIdentifier("windowTranslucentStatus", "attr", "android") != 0  && !Utils.isSimilarToWhite(UserVoice.sColor)){
            if (mTextViewColorful == null) {
                mTextViewColorful = new TextView(this);
                int statusH = getStatusBarHeight();
                int actionbarH = getActionBarHeight();
                mTextViewColorful.setHeight(statusH + actionbarH);
                mTextViewColorful.setBackgroundColor(UserVoice.sColor);
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
        return mLinearLayout;
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
    @Override
    @SuppressLint("NewApi")
    public void showSearch() {
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.uv_view_flipper);
        if (viewFlipper!=null){
        viewFlipper.getChildAt(1).setPaddingRelative(0, getActionBarHeight(), 0, 0);
        }
        super.showSearch();
    }
}