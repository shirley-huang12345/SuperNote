package com.asus.supernote.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ColorfulLinearLayout extends LinearLayout {
    private int mInsetsBottom;
    public ColorfulLinearLayout(Context context) {
        super(context);
    }
    public ColorfulLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ColorfulLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    protected boolean fitSystemWindows(Rect insets) {
        final int vis = getWindowSystemUiVisibility();
        final boolean stable = (vis & SYSTEM_UI_FLAG_LAYOUT_STABLE) != 0;
        final int lastInsetsBottom = mInsetsBottom;
        if (!stable) {
            mInsetsBottom = 0;
        } else {
            mInsetsBottom = insets.bottom;
        }
        if (mInsetsBottom != lastInsetsBottom) {
            MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
            lp.bottomMargin = mInsetsBottom;
            requestLayout();
        }
        return stable;
    }
}
