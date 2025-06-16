package com.uservoice.uservoicesdk.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.List;


/**
 * Created by SemonCat on 2014/7/15.
 */
public abstract class BaseFragment extends Fragment {
    protected static final String TAG = BaseFragment.class.getName();
    protected static final String EMPTY = "";
    private boolean alreadyLoaded;
    private View rootView;
    private Activity activity;

    protected abstract void init();

    protected abstract void setupView(View rootView);

    protected abstract void setupAdapter();

    protected abstract void setupEvent();

    protected abstract int setupLayout();

    @Override
    public void onAttach(Activity activity) {
        this.activity = activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public void onRestoreInstanceState(Bundle outState) {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (setupLayout() == 0) {
            throw new IllegalArgumentException("You must overwrite setupLayout() method.");
        }

        setHasOptionsMenu(true);

        if (savedInstanceState != null && !alreadyLoaded) {
            alreadyLoaded = true;
        }

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
        rootView = inflater.inflate(setupLayout(), container, false);
        init();
        setupView(rootView);
        setupAdapter();
        setupEvent();
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();

        return rootView;
    }

    protected boolean onBackPressed() {
        return false;
    }


    Toast mToast;

    protected void showToast(int stringResourceId) {
        if (!isAdded()) {
            return;
        }

        showToast(getString(stringResourceId));
    }

    protected void showToast(String Message) {
        if (!isAdded()) {
            return;
        }

        if (mToast == null) {
            mToast = Toast.makeText(activity, Message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(Message);
        }
        mToast.show();
    }

    public final <E extends View> E findViewById(int id) {
        return (E) rootView.findViewById(id);
    }
}
