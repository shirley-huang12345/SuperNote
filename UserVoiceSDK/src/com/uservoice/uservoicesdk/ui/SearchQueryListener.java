package com.uservoice.uservoicesdk.ui;

import android.text.TextUtils;
import android.widget.SearchView;
import android.widget.Toast;

import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.UserVoice;
import com.uservoice.uservoicesdk.activity.SearchActivity;

public class SearchQueryListener implements SearchView.OnQueryTextListener {
    private final SearchActivity searchActivity;

    public SearchQueryListener(SearchActivity searchActivity) {
        this.searchActivity = searchActivity;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if(TextUtils.equals(query.toLowerCase(), "sdk version")){
            Toast.makeText(Session.getInstance().getContext(), UserVoice.getVersion(), Toast.LENGTH_LONG).show();
        }
        searchActivity.getSearchAdapter().performSearch(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        searchActivity.getSearchAdapter().performSearch(query);
        if (query.length() > 0) {
            searchActivity.showSearch();
        } else {
            searchActivity.hideSearch();
        }
        return true;
    }
}
