package com.uservoice.uservoicesdk.activity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.UserVoice;
import com.uservoice.uservoicesdk.babayaga.Babayaga;
import com.uservoice.uservoicesdk.model.Article;
import com.uservoice.uservoicesdk.model.Topic;
import com.uservoice.uservoicesdk.rest.Callback;
import com.uservoice.uservoicesdk.ui.DefaultCallback;
import com.uservoice.uservoicesdk.ui.Utils;

public class NoAnimNewsActivity extends BaseExpandableActivity {
    private final static String TAG = "NoAnimNewsActivity";
    private ExpandableListView mAnimatedExpandableListView;
    private MyNewsAdapter myNewsAdapter;
    private Config mConfig;
    private Topic mTopic;
    private int mTopicID;
    private View mLoadingView;
    private int mWebViewBg;
    private String mActionBarTitle;

    private String mMenuZen="ZenUI apps update";
    private String mMenuAbout="About";

    @Override
    @SuppressLint({"InlinedApi", "NewApi"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConfig = Session.getInstance().getConfig();
        if(mConfig == null) finish();

        Intent intent = getIntent();
        mTopicID = mConfig.getTopicId();
        if (mTopicID == -1) mTopicID = 64236;//ZenUI topic id

        mWebViewBg = intent.getIntExtra("webview_bg_color", Color.argb(255, 232, 232, 232));
        mActionBarTitle = intent.getStringExtra("action_bar_title");
        if (mActionBarTitle == null) mActionBarTitle = "NEWS";

        if (hasActionBar()) {
            ActionBar actionBar = getActionBar();
            if(Build.VERSION.SDK_INT >= 21) {
                actionBar.setTitle(Html.fromHtml("<font color = '" + String.format("#%06X", 0xFFFFFF & UserVoice.sColor) + "'>" + mActionBarTitle + "</font>"));
            } else {
                actionBar.setTitle(mActionBarTitle);
            }
        }

        mMenuZen = intent.getStringExtra("menu_zen");
        mMenuAbout = intent.getStringExtra("menu_about");

        setContentView(R.layout.uf_sdk_no_anim_news_activity_main);
        mLoadingView = getView(R.id.no_anim_loading_view);
        mAnimatedExpandableListView = getView(R.id.no_anim_listView);
        switchLoadingAndListView(true);

        mAnimatedExpandableListView.setGroupIndicator(null);
        mAnimatedExpandableListView.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                    int groupPosition, long id) {
                GroupHolder holder = (GroupHolder) v.getTag();
                if (mAnimatedExpandableListView.isGroupExpanded(groupPosition)) {
                    holder.indicator.setBackgroundResource(R.drawable.uf_sdk_asus_btn_expand_normal);
                } else {
                    holder.indicator.setBackgroundResource(R.drawable.uf_sdk_asus_btn_expand_pressed);
                }
                return false;
            }
        });

        getTopics();
        setupListview();
    }

    @Override
    public void setTitle(CharSequence title) {
    	if(Build.VERSION.SDK_INT >= 21 && !(Utils.isSimilarToWhite(UserVoice.sColor)))
            super.setTitle(Html.fromHtml("<font color = '" + String.format("#%06X", 0xFFFFFF & UserVoice.sColor) + "'>" + title.toString() + "</font>"));
        else
            super.setTitle(title);
    }

    private class GroupHolder {
        TextView title;
        TextView message;
        ImageView indicator;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.uf_sdk_news_menu, menu);
        MenuItem zenui = menu.findItem(R.id.uf_sdk_zenui_apps_item);
        MenuItem about = menu.findItem(R.id.uf_sdk_about_item);
        zenui.setTitle(mMenuZen);
        about.setTitle(mMenuAbout);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.uf_sdk_zenui_apps_item) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.setData(Uri.parse("market://details?id="+pkg));
                intent.setData(Uri.parse("market://search?q=pub:ZenUI, ASUS Computer Inc."));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=ZenUI,+ASUS+Computer+Inc."));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                List<ResolveInfo> result = getPackageManager().queryIntentActivities(intent, 0);
                if (result.size() == 0) {
                    Log.d(TAG, "No activity can handle this intent:"+intent.toString());
                } else {
                    startActivity(intent);
                }
            }
            return true;
        } else if (itemId == R.id.uf_sdk_feedback_and_help_item) {
            sendBroadcast(new Intent("com.asus.userfeedback.intent.action.LAUNCH_PORTAL"));
            return true;
        } else if (itemId == R.id.uf_sdk_about_item) {
            sendBroadcast(new Intent("com.asus.userfeedback.intent.action.LAUNCH_ABOUT"));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    };

    @SuppressWarnings("unchecked")
    public final <E extends View> E getView(int id) {
        return (E)findViewById(id);
    }

    @Override
    @SuppressLint("NewApi")
    public boolean hasActionBar() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && getActionBar() != null;
    }

    private void getTopics() {
        Topic.loadTopic(mTopicID, new DefaultCallback<Topic>(this) {
            @Override
            public void onModel(Topic model) {
                if (model != null) {
                    ArrayList<Topic> topics = new ArrayList<Topic>();
                    topics.add(model);
                    Session.getInstance().setTopics(topics);
                    mTopic = model;
                    myNewsAdapter.updateTopic();
                }
            }
        });
    }

    private void setupListview() {
        myNewsAdapter = new MyNewsAdapter(this);
//        mAnimatedExpandableListView.setDivider(null);
        mAnimatedExpandableListView.setOnScrollListener(new NewsScrollListener(myNewsAdapter));
        mAnimatedExpandableListView.setAdapter(myNewsAdapter);
        Babayaga.track(Babayaga.Event.VIEW_TOPIC, mTopicID);
    }

    public void switchLoadingAndListView(boolean show_loading) {
        if (show_loading) {
            mLoadingView.setVisibility(View.VISIBLE);
            mAnimatedExpandableListView.setVisibility(View.INVISIBLE);
        } else {
            mLoadingView.setVisibility(View.INVISIBLE);
            mAnimatedExpandableListView.setVisibility(View.VISIBLE);
        }
    }

    public class NewsScrollListener implements AbsListView.OnScrollListener {

        private final MyNewsAdapter adapter;

        public NewsScrollListener(MyNewsAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                adapter.loadMore(false);
            }
        }
    }

    public static class ViewHolder {
        // I added a generic return type to reduce the casting noise in client code
        @SuppressWarnings("unchecked")
        public static <T extends View> T get(View view, int id) {
            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
            if (viewHolder == null) {
                viewHolder = new SparseArray<View>();
                view.setTag(viewHolder);
            }
            View childView = viewHolder.get(id);
            if (childView == null) {
                childView = view.findViewById(id);
                viewHolder.put(id, childView);
            }
            return (T) childView;
        }
    }

    public class MyNewsAdapter extends BaseExpandableListAdapter {

//      private List<Article> searchResults = new ArrayList<Article>();
//      private boolean searchActive = false;
//      private String currentQuery;
//      private String pendingQuery;
//      private int scope;
//      private SearchTask currentSearch;

      private List<Article> mItems = new ArrayList<Article>();
      private SparseArray<WebView> mWVGroup = new SparseArray<WebView>();
      private boolean mIsLoading;

      private LayoutInflater mInflater;
      private Context mContext;
      private int mPage = 1;
      private int mTotalObjects;

      public MyNewsAdapter(Context context) {
           mInflater = LayoutInflater.from(context);
           mContext = context;
           loadMore(true);
           if (mTopic != null) {
               mTotalObjects = mTopic.getNumberOfArticles();
           }
      }

      private void updateTopic() {
          if (mTopic != null) {
              mTotalObjects = mTopic.getNumberOfArticles();
              notifyDataSetChanged();
          }
      }

      @Override
      public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
          GroupHolder holder;
          Article item = getGroup(groupPosition);

          if (convertView == null) {
              holder = new GroupHolder();
              convertView = mInflater.inflate(R.layout.uf_sdk_news_group_item, parent, false);
              holder.title = (TextView) convertView.findViewById(R.id.news_group_title);
              holder.message = (TextView) convertView.findViewById(R.id.news_group_message);
              holder.indicator = (ImageView) convertView.findViewById(R.id.news_group_indicator);
              holder.indicator.setBackgroundResource(R.drawable.uf_sdk_asus_btn_expand_normal);
              convertView.setTag(holder);
          } else {
              holder = (GroupHolder) convertView.getTag();
          }

          holder.title.setText(item.getTitle());

          Date date =  item.getUpdatedAt();
          if (date != null) {
              holder.message.setVisibility(View.VISIBLE);
              holder.message.setText(DateFormat.getDateInstance().format(date));
          } else {
              holder.message.setVisibility(View.GONE);
          }

          if (getGroupCount() != 0) {
              switchLoadingAndListView(false);
          }
          return convertView;
      }

      @SuppressWarnings("deprecation")
      @SuppressLint("SetJavaScriptEnabled")
      @Override
      public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
          WebView wv = null;
          if (mWVGroup.size() < 1) {
              for (int i = 0; i < mItems.size(); i++) {
                  Article article = getGroup(i);
                  final WebView webViewItem = new WebView(mContext);
                  String styles = "iframe, img { width: 100%; }";
                  webViewItem.setBackgroundColor(mWebViewBg);
                  String html = String.format("<html><head><meta charset=\"utf-8\"><link rel=\"stylesheet\" type=\"text/css\" href=\"http://cdn.uservoice.com/stylesheets/vendor/typeset.css\"/><style>%s</style></head><body class=\"typeset\" style=\"font-family: sans-serif; margin: 1em\">%s</body></html>", styles, article.getHtml());
                  //webViewItem.setWebChromeClient(new WebChromeClient());
                  webViewItem.setWebViewClient(new MyWebViewClient());
                  webViewItem.getSettings().setJavaScriptEnabled(true);
                  webViewItem.getSettings().setPluginState(PluginState.ON);
                  webViewItem.loadUrl(String.format("data:text/html;charset=utf-8,%s", Uri.encode(html)));
                  mWVGroup.append(i, webViewItem);
              }
          }
          wv = mWVGroup.get(groupPosition);
          //wv.setPadding(30, 10, 10, 10);
          return wv;
      }

      @Override
      public Article getGroup(int groupPosition) {
          return mItems.get(groupPosition);
      }

      @Override
      public int getGroupCount() {
          return mItems.size();
      }

//      protected int getTotalNumberOfObjects() {
//          if (mTopic.getId() == -1) {
//              return -1; // we don't know. keep trying to load more.
//          } else {
//              return mTopic.getNumberOfArticles();
//          }
//      }

//      private void reload() {
//          // not *correct* but probably good enough. the correct thing would be to cancel the load somehow and proceed
//          if (mIsLoading)
//              return;
//          mPage = 1;
//          mItems = new ArrayList<Article>();
//          loadMore();
//      }

      public void loadMore(boolean first) {
          if (mIsLoading) return;
          if (mTopic != null) {
              if (mItems.size() >= mTotalObjects) return;
          } else {
              if (!first) return;
          }

          mIsLoading = true;
          notifyDataSetChanged();
          loadPage(mPage, new DefaultCallback<List<Article>>(mContext) {

              @SuppressWarnings("deprecation")
              @SuppressLint("SetJavaScriptEnabled")
              @Override
              public void onModel(List<Article> model) {
                  if (mTopic != null) {
                      if (mItems.size() < mTotalObjects) {
                          mItems.addAll(model);
                      }
                  } else {
                      mItems.clear();
                      mItems.addAll(model);
                  }
                  mPage += 1;
                  mIsLoading = false;

                  if (mWVGroup.size() != mItems.size()) {
                      mWVGroup.clear();
                      for (int i = 0; i < mItems.size(); i++) {
                          Article article = getGroup(i);
                          final WebView webViewItem = new WebView(mContext);
                          String styles = "iframe, img { width: 100%; }";
                          webViewItem.setBackgroundColor(mWebViewBg);
                          String html = String.format("<html><head><meta charset=\"utf-8\"><link rel=\"stylesheet\" type=\"text/css\" href=\"http://cdn.uservoice.com/stylesheets/vendor/typeset.css\"/><style>%s</style></head><body class=\"typeset\" style=\"font-family: sans-serif; margin: 1em\">%s</body></html>", styles, article.getHtml());
                          //webViewItem.setWebChromeClient(new WebChromeClient());
                          webViewItem.setWebViewClient(new MyWebViewClient());
                          webViewItem.getSettings().setJavaScriptEnabled(true);
                          webViewItem.getSettings().setPluginState(PluginState.ON);
                          webViewItem.loadUrl(String.format("data:text/html;charset=utf-8,%s", Uri.encode(html)));
                          mWVGroup.append(i, webViewItem);
                      }
                  }

                  notifyDataSetChanged();
              }
          });
      }

      private void loadPage(int page, Callback<List<Article>> callback) {
          if (mTopicID == -1) {
              Article.loadPage(page, callback);
          } else {
              Article.loadPageForTopic(mTopicID, page, callback);
          }
      }

      @Override
      public long getChildId(int groupPosition, int childPosition) {
          return childPosition;
      }

      @Override
      public long getGroupId(int groupPosition) {
          return groupPosition;
      }

      @Override
      public boolean hasStableIds() {
          return true;
      }

      @Override
      public boolean isChildSelectable(int arg0, int arg1) {
          return true;
      }

      @Override
      public int getChildrenCount(int groupPosition) {
          return 1;
      }

      @Override
      public Object getChild(int groupPosition, int childPosition) {
          return null;
      }

//      public void setData(List<Article> items) {
//          this.mItems = items;
//      }
//      public void performSearch(String query) {
//          pendingQuery = query;
//          if (query.length() == 0) {
//              searchResults = new ArrayList<Article>();
//              loading = false;
//              notifyDataSetChanged();
//          } else {
//              loading = true;
//              notifyDataSetChanged();
//              if (currentSearch != null) {
//                  currentSearch.cancel();
//              }
//              currentSearch = new SearchTask(query);
//              currentSearch.run();
//          }
//      }
  //
//      public void setSearchActive(boolean searchActive) {
//          this.searchActive = searchActive;
//          loading = false;
//          notifyDataSetChanged();
//      }
  //
//      private class SearchTask extends TimerTask {
//          private final String query;
//          private boolean stop;
//          private RestTask task;
  //
//          public SearchTask(String query) {
//              this.query = query;
//          }
  //
//          @Override
//          public boolean cancel() {
//              stop = true;
//              if (task != null) {
//                  task.cancel(true);
//              }
//              return true;
//          }
  //
//          @Override
//          public void run() {
//              currentQuery = query;
//              task = search(query, new DefaultCallback<List<Article>>(context) {
//                  @Override
//                  public void onModel(List<Article> model) {
//                      if (!stop) {
//                          searchResults = model;
//                          loading = false;
//                          notifyDataSetChanged();
//                          searchResultsUpdated();
//                      }
//                  }
//              });
//              if (task == null) {
//                  // can't search
//                  loading = false;
//              }
//          }
//      }
  //
//      protected void searchResultsUpdated() {
//      }
  //
//      protected boolean shouldShowSearchResults() {
//          return searchActive && pendingQuery != null && pendingQuery.length() > 0;
//      }
  //
//      protected RestTask search(String query, Callback<List<Article>> callback) {
//          return null;
//      }
  //
//      public void setScope(int scope) {
//          this.scope = scope;
//          notifyDataSetChanged();
//      }
  }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
//            view.loadUrl(url);
            return true;
        }
    }
}
