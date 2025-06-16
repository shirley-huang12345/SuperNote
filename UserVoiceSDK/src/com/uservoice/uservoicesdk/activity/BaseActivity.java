package com.uservoice.uservoicesdk.activity;

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.SearchManager;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnSuggestionListener;
import android.widget.ViewFlipper;

import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.UserVoice;
import com.uservoice.uservoicesdk.ui.MixedSearchAdapter;
import com.uservoice.uservoicesdk.ui.PortalAdapter;
import com.uservoice.uservoicesdk.ui.SearchAdapter;
import com.uservoice.uservoicesdk.ui.SearchExpandListener;
import com.uservoice.uservoicesdk.ui.SearchQueryListener;
import com.uservoice.uservoicesdk.ui.Utils;

public class BaseActivity extends FragmentActivity {

    protected Tab allTab;
    protected Tab articlesTab;
    protected Tab ideasTab;
    private int originalNavigationMode = -1;
    protected MixedSearchAdapter searchAdapter;
    private Menu mMenu;
    private SearchView mSearchView;

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        if(Utils.isSimilarToWhite(UserVoice.sColor))setTheme(R.style.UserVoiceSDKTheme_light);
        else setTheme(R.style.UserVoiceSDKTheme);
        super.onCreate(savedInstanceState);
        if (hasActionBar()) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayUseLogoEnabled(false);
        }
    }
/*
    @Override
    public void setTitle(CharSequence title) {
        if(Build.VERSION.SDK_INT >= 21 && !(Utils.isSimilarToWhite(UserVoice.sColor)))
            super.setTitle(Html.fromHtml("<font color = '" + String.format("#%06X", 0xFFFFFF & UserVoice.sColor) + "'>" + title.toString() + "</font>"));
        else
            super.setTitle(title);
    }
*/
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent != null && intent.getAction() != null && intent.getAction().endsWith(Intent.ACTION_SEARCH)){
            String query = intent.getStringExtra(SearchManager.QUERY);
            mSearchView.setQuery(query, true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public SearchAdapter<?> getSearchAdapter() {
        return searchAdapter;
    }

    @SuppressLint("NewApi")
    protected void setupScopedSearch(Menu menu) {
        mMenu = menu;
        if (hasActionBar()) {
            menu.findItem(R.id.uv_action_search).setOnActionExpandListener(new SearchExpandListener((SearchActivity) this, menu));
            mSearchView = (SearchView) menu.findItem(R.id.uv_action_search).getActionView();

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());

            mSearchView.setSearchableInfo(searchableInfo);
            mSearchView.setOnSuggestionListener(new OnSuggestionListener() {

                @Override
                public boolean onSuggestionSelect(int position) {
                    return false;
                }

                @Override
                public boolean onSuggestionClick(int position) {
                    Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
                    String suggest1 = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                    mSearchView.setQuery(suggest1, true);
                    mSearchView.clearFocus();
                    return false;
                }
            });

            mSearchView.setQueryHint(getResources().getString(R.string.uf_sdk_search_hint));
            mSearchView.setOnQueryTextListener(new SearchQueryListener((SearchActivity) this));
            mSearchView.setImeOptions(0x00000003);
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View searchView = layoutInflater.inflate(R.layout.uf_sdk_search_view, null);
            ListView searchResult = (ListView)searchView.findViewById(R.id.uf_sdk_search_result);
            View noResult = searchView.findViewById(R.id.no_result_text);
            searchResult.setEmptyView(noResult);
            noResult.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    //Do nothing
                }
            });
            searchAdapter = new MixedSearchAdapter(this);
            searchResult.setAdapter(searchAdapter);
            searchResult.setOnItemClickListener(searchAdapter);
            ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.uv_view_flipper);
            viewFlipper.addView(searchView, 1);
            ActionBar.TabListener listener = new ActionBar.TabListener() {
                @Override
                public void onTabUnselected(Tab tab, FragmentTransaction ft) {
                }

                @Override
                public void onTabSelected(Tab tab, FragmentTransaction ft) {
                    searchAdapter.setScope((Integer) tab.getTag());
                }

                @Override
                public void onTabReselected(Tab tab, FragmentTransaction ft) {
                }
            };
            allTab = getActionBar().newTab().setText(getString(R.string.uv_all_results_filter)).setTabListener(listener).setTag(PortalAdapter.SCOPE_ALL);
            getActionBar().addTab(allTab);
            articlesTab = getActionBar().newTab().setText(getString(R.string.uf_sdk_faq)).setTabListener(listener).setTag(PortalAdapter.SCOPE_ARTICLES);
            getActionBar().addTab(articlesTab);
            ideasTab = getActionBar().newTab().setText(getString(R.string.uf_sdk_topic_text_heading).toUpperCase()).setTabListener(listener).setTag(PortalAdapter.SCOPE_IDEAS);
            getActionBar().addTab(ideasTab);
            forceTabs();
        } else {
            menu.findItem(R.id.uv_action_search).setVisible(false);
        }
    }

    @SuppressLint("NewApi")
    public void updateScopedSearch(int results, int articleResults, int ideaResults) {
        if (hasActionBar()) {
            allTab.setText(String.format("%s (%d)", getString(R.string.uv_all_results_filter), results));
            articlesTab.setText(String.format("%s (%d)", getString(R.string.uf_sdk_faq), articleResults));
            ideasTab.setText(String.format("%s (%d)", getString(R.string.uf_sdk_topic_text_heading).toUpperCase(), ideaResults));
        }
    }



    public void forceTabs() {
        try {
            final ActionBar actionBar = getActionBar();
            final Method setHasEmbeddedTabsMethod = actionBar.getClass()
                .getDeclaredMethod("setHasEmbeddedTabs", boolean.class);
            setHasEmbeddedTabsMethod.setAccessible(true);
            setHasEmbeddedTabsMethod.invoke(actionBar, false);
        }
        catch(final Exception e) {
            // Handle issues as needed: log, warn user, fallback etc
            // This error is safe to ignore, standard tabs will appear.
        }
    }


    @SuppressLint("NewApi")
    public void showSearch() {
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.uv_view_flipper);
        viewFlipper.setDisplayedChild(1);
        if (hasActionBar()) {
            if (originalNavigationMode == -1)
                originalNavigationMode = getActionBar().getNavigationMode();
            getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
    }

    @SuppressLint("NewApi")
    public void hideSearch() {
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.uv_view_flipper);
        viewFlipper.setDisplayedChild(0);
        if (hasActionBar()) {
            getActionBar().setNavigationMode(originalNavigationMode == -1 ? ActionBar.NAVIGATION_MODE_STANDARD : originalNavigationMode);
        }
    }

    @SuppressLint("NewApi")
    public boolean hasActionBar() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && getActionBar() != null;
    }

    protected void checkSearchSetting() {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());

        if (searchableInfo==null){
            throw new IllegalArgumentException("Please check more info at Confluence and add meta-data into your AndroidManifest.xml or Contact Semon Huang or Ed Chou");
        }
    }
}
