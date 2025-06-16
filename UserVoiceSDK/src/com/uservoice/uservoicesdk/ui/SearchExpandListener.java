package com.uservoice.uservoicesdk.ui;

import android.annotation.SuppressLint;
import android.view.Menu;
import android.view.MenuItem;

import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.activity.SearchActivity;

@SuppressLint("NewApi")
public class SearchExpandListener implements MenuItem.OnActionExpandListener {
    private final SearchActivity searchActivity;
    private final Menu mMenu;

    public SearchExpandListener(SearchActivity searchActivity, Menu menu) {
        this.searchActivity = searchActivity;
        mMenu = menu;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        searchActivity.getSearchAdapter().setSearchActive(true);
        MenuItem contacts = mMenu.findItem(R.id.uv_action_contact);
        contacts.setVisible(false);
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        MenuItem contacts = mMenu.findItem(R.id.uv_action_contact);
        if(contacts != null && Session.getInstance().getConfig().shouldShowContactUs()) contacts.setVisible(true);
        searchActivity.getSearchAdapter().setSearchActive(false);
        searchActivity.hideSearch();
        return true;
    }
}