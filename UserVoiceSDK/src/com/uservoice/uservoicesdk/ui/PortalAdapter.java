package com.uservoice.uservoicesdk.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.UserVoice;
import com.uservoice.uservoicesdk.activity.ArticleActivity;
import com.uservoice.uservoicesdk.activity.ContactActivity;
import com.uservoice.uservoicesdk.activity.ForumActivity;
import com.uservoice.uservoicesdk.activity.SearchActivity;
import com.uservoice.uservoicesdk.flow.InitManager;
import com.uservoice.uservoicesdk.model.Article;
import com.uservoice.uservoicesdk.model.BaseModel;
import com.uservoice.uservoicesdk.model.Forum;
import com.uservoice.uservoicesdk.model.Suggestion;
import com.uservoice.uservoicesdk.model.Topic;
import com.uservoice.uservoicesdk.rest.RestResult;

public class PortalAdapter extends SearchAdapter<BaseModel> implements AdapterView.OnItemClickListener {

    public static int SCOPE_ALL = 0;
    public static int SCOPE_ARTICLES = 1;
    public static int SCOPE_IDEAS = 2;

    private static int KB_HEADER = 0;
    private static int FORUM = 1;
    private static int TOPIC = 2;
    private static int LOADING = 3;
    private static int CONTACT = 4;
    private static int ARTICLE = 5;
    private static int SDK_VERSION = 6;
    private static int FORUM_LOADING = 7;

    private LayoutInflater inflater;
    private final FragmentActivity context;
    private boolean configLoaded = false;
    private List<Integer> staticRows;
    private List<Article> articles;
    private String mSDKVersion;
    private int forumSuggestionCounter = -1;
    private boolean mForumLoading;
    private boolean mShowLoading;

    public PortalAdapter(FragmentActivity context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSDKVersion = UserVoice.getVersion();
        mShowLoading = true;
        new InitManager(context, new Runnable() {
            @Override
            public void run() {
                configLoaded = true;
                notifyDataSetChanged();
                loadForum();
                loadForumSuggestionCounter();
                loadTopics();
            }
        }, new Runnable() {
            @Override
            public void run() {
                mShowLoading = false;
                notifyDataSetChanged();

            }
        }).init();
    }
    public void reload(){
    	new InitManager(context, new Runnable() {
            @Override
            public void run() {
                configLoaded = true;
                notifyDataSetChanged();
                loadForum();
                loadForumSuggestionCounter();
                loadTopics();
            }
        }, new Runnable() {
            @Override
            public void run() {
                mShowLoading = false;
                notifyDataSetChanged();

            }
        }).init();
    }

    private List<Topic> getTopics() {
        return Session.getInstance().getTopics();
    }

    private boolean shouldShowArticles() {
        return Session.getInstance().getConfig().getTopicId() != -1 || (getTopics() != null && getTopics().isEmpty());
    }

    private void loadForum() {
        mForumLoading = true;
        Forum.loadForum(Session.getInstance().getConfig().getForumId(), new DefaultCallback<Forum>(context) {
            @Override
            public void onModel(Forum model) {
                Session.getInstance().setForum(model);
                mForumLoading = false;
                notifyDataSetChanged();
            }
            @Override
            public void onError(RestResult error) {
                // TODO Auto-generated method stub
                mShowLoading = false;
                notifyDataSetChanged();
                super.onError(error);
            }
        });
    }

    private void loadForumSuggestionCounter(){
        Forum.loadForumSuggestionCounter(Session.getInstance().getConfig().getForumId(), new DefaultCallback<Integer>(context) {
            @Override
            public void onModel(Integer counter) {
                forumSuggestionCounter = counter;
                notifyDataSetChanged();
            }
            @Override
            public void onError(RestResult error) {
                // TODO Auto-generated method stub
                mShowLoading = false;
                notifyDataSetChanged();
                super.onError(error);
            }
        });
    }

    private void loadTopics() {
        final DefaultCallback<List<Article>> articlesCallback = new DefaultCallback<List<Article>>(context) {
            @Override
            public void onModel(List<Article> model) {
                Session.getInstance().setTopics(new ArrayList<Topic>());
                articles = model;
                notifyDataSetChanged();
            }
            @Override
            public void onError(RestResult error) {
                // TODO Auto-generated method stub
                mShowLoading = false;
                notifyDataSetChanged();
                super.onError(error);
            }
        };

        if (Session.getInstance().getConfig().getTopicId() != -1) {
            Article.loadPageForTopic(Session.getInstance().getConfig().getTopicId(), 1, articlesCallback);
        } else {
            Topic.loadTopics(new DefaultCallback<List<Topic>>(context) {
                @Override
                public void onModel(List<Topic> model) {
                    if (model.isEmpty()) {
                        Session.getInstance().setTopics(model);
                        Article.loadPage(1, articlesCallback);
                    } else {
                        ArrayList<Topic> topics = new ArrayList<Topic>(model);
                        topics.add(Topic.ALL_ARTICLES);
                        Session.getInstance().setTopics(topics);
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void computeStaticRows() {
        if (staticRows == null) {
            staticRows = new ArrayList<Integer>();
            Config config = Session.getInstance().getConfig();
            /*
            if (config.shouldShowContactUs())
                staticRows.add(CONTACT);
            */
            if (config.shouldShowForum())
                staticRows.add(FORUM);
            if (config.shouldShowKnowledgeBase())
                staticRows.add(KB_HEADER);
        }
    }

    @Override
    public int getCount() {
        if (!configLoaded) {
            if(mShowLoading)
                return 1;
            else
                return 0;
        } else {
            computeStaticRows();
            int rows = staticRows.size();
            if (Session.getInstance().getConfig().shouldShowKnowledgeBase()) {
                if (getTopics() == null || (shouldShowArticles() && articles == null)) {
                    rows += 1;
                } else {
                    rows += shouldShowArticles() ? articles.size() : getTopics().size();
                }
            }
            //rows += 1;
            return rows;
        }
    }

    public List<BaseModel> getScopedSearchResults() {
        if (scope == SCOPE_ALL) {
            return searchResults;
        } else if (scope == SCOPE_ARTICLES) {
            List<BaseModel> articles = new ArrayList<BaseModel>();
            for (BaseModel model : searchResults) {
                if (model instanceof Article)
                    articles.add(model);
            }
            return articles;
        } else if (scope == SCOPE_IDEAS) {
            List<BaseModel> ideas = new ArrayList<BaseModel>();
            for (BaseModel model : searchResults) {
                if (model instanceof Suggestion)
                    ideas.add(model);
            }
            return ideas;
        }
        return null;
    }

    @Override
    public Object getItem(int position) {
        computeStaticRows();
        if (position < staticRows.size() && staticRows.get(position) == FORUM)
            return Session.getInstance().getForum();
        else if (getTopics() != null && !shouldShowArticles() && position >= staticRows.size() && position - staticRows.size() < getTopics().size())
            return getTopics().get(position - staticRows.size());
        else if (articles != null && shouldShowArticles() && position >= staticRows.size() && position - staticRows.size() < articles.size())
            return articles.get(position - staticRows.size());
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        if (!configLoaded)
            return false;
        computeStaticRows();
        if (position < staticRows.size()) {
            int type = staticRows.get(position);
            if (type == KB_HEADER || type == LOADING)
                return false;
        }
        return true;
    }

    @Override
    protected void searchResultsUpdated() {
        int articleResults = 0;
        int ideaResults = 0;
        for (BaseModel model : searchResults) {
            if (model instanceof Article)
                articleResults += 1;
            else
                ideaResults += 1;
        }
        ((SearchActivity) context).updateScopedSearch(searchResults.size(), articleResults, ideaResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Config config = Session.getInstance().getConfig();
        View view = convertView;
        int type = getItemViewType(position);
        if (type == LOADING)
            view = inflater.inflate(R.layout.uv_loading_item, null);
        else if (type == FORUM)
            view = inflater.inflate(R.layout.uf_sdk_forum_item, null);
        else if (type == KB_HEADER){
            if(config.isAssistance()){
                view = inflater.inflate(R.layout.uf_sdk_device_info, null);
            }else{
                view = inflater.inflate(R.layout.uf_sdk_header_item_light, null);
            }
        }else if (type == TOPIC)
            view = inflater.inflate(R.layout.uv_text_item, null);
        else if (type == CONTACT)
            view = inflater.inflate(R.layout.uv_text_item, null);
        else if (type == ARTICLE)
            view = inflater.inflate(R.layout.uv_text_item, null);
        else if (type == SDK_VERSION)
            view = inflater.inflate(R.layout.uf_sdk_version_item, null);
        else if (type == FORUM_LOADING)
            view = inflater.inflate(R.layout.uf_sdk_forum_item, null);


        if (type == FORUM) {

            View progress = view.findViewById(R.id.uf_sdk_progress);
            progress.setVisibility(View.GONE);
            TextView count = (TextView) view.findViewById(R.id.uf_sdk_forum_count);
            if(forumSuggestionCounter >= 0){
                String countString = String.valueOf(forumSuggestionCounter);
                if(forumSuggestionCounter > 999){
                    countString = "999+";
                }
                count.setText(countString);
                ((TextView) view.findViewById(R.id.uf_sdk_forum_count_unit)).setText(context.getResources().getQuantityString(R.plurals.uf_sdk_topics, forumSuggestionCounter));
                view.findViewById(R.id.uf_sdk_forum_count_section).setVisibility(View.VISIBLE);
            }else{
                view.findViewById(R.id.uf_sdk_forum_count_section).setVisibility(View.GONE);
            }


        } else if (type == KB_HEADER) {

            if(config.isAssistance()){
                String[] names = config.getDeviceInfoName();
                if(names != null && names.length > 0){
                	String[] values = config.getDeviceInfoValue();

                    LinearLayout deviceInfoLayout = (LinearLayout) view.findViewById(R.id.uf_sdk_device_info);
                    deviceInfoLayout.removeAllViews();
                	for(int i = 0; i < names.length; i++){

                        View deviceInfoItemView = inflater.inflate(R.layout.uf_sdk_device_info_item, null);
                        TextView infoName = (TextView) deviceInfoItemView.findViewById(R.id.uf_sdk_info_name);
                        TextView infoValue = (TextView) deviceInfoItemView.findViewById(R.id.uf_sdk_info_value);
                        if(infoName != null)
                        	infoName.setText(names[i]);
                        if(infoValue != null)
                        	infoValue.setText(values[i]);
                        deviceInfoLayout.addView(deviceInfoItemView);
                     }
                }else{
                    //view.setVisibility(View.GONE);
                    view = new View(context);
                }
            }else{
                TextView textView = (TextView) view.findViewById(R.id.uv_header_text);
                if(textView != null)textView.setText(R.string.uf_sdk_faq);
            }
        } else if (type == TOPIC) {
            Topic topic = (Topic) getItem(position);
            TextView textView = (TextView) view.findViewById(R.id.uv_text);
            textView.setText(topic.getName());
            textView = (TextView) view.findViewById(R.id.uv_text2);
            if (topic == Topic.ALL_ARTICLES) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
                textView.setText(String.format("%d %s", topic.getNumberOfArticles(), context.getResources().getQuantityString(R.plurals.uv_articles, topic.getNumberOfArticles())));
            }
        } else if (type == CONTACT) {
            TextView textView = (TextView) view.findViewById(R.id.uv_text);
            textView.setText(R.string.uf_sdk_send_feedback);
            view.findViewById(R.id.uv_text2).setVisibility(View.GONE);
        } else if (type == ARTICLE) {
            TextView textView = (TextView) view.findViewById(R.id.uv_text);
            Article article = (Article) getItem(position);
            textView.setText(article.getTitle());
            textView = (TextView) view.findViewById(R.id.uv_text2);
            textView.setVisibility(View.GONE);
        } else if (type == SDK_VERSION) {
            TextView textView = (TextView) view.findViewById(R.id.sdk_version);
            textView.setText(context.getString(R.string.uf_sdk_sdk) + " v" + mSDKVersion);
            textView.setVisibility(View.GONE);
        }else if (type == FORUM_LOADING ){
            View progress = view.findViewById(R.id.uf_sdk_progress);
            if (mShowLoading) progress.setVisibility(View.VISIBLE);
            else progress.setVisibility(View.GONE);
        }else if (type == LOADING){
        	if (!mShowLoading) view = new View(context);
        }
        View divider = view.findViewById(R.id.uv_divider);
        if (divider != null)
            divider.setVisibility((position == getCount() - 2 && getItemViewType(getCount() - 1) == SDK_VERSION) || position == getCount() - 1 ? View.GONE : View.VISIBLE);
        return view;
    }

    public void setShowLoading(boolean show){
        mShowLoading = show;
    }

    @Override
    public int getViewTypeCount() {
        return 8;
    }

    @Override
    public int getItemViewType(int position) {
        if (!configLoaded)
            return LOADING;
        computeStaticRows();
        if (position < staticRows.size()) {
            int type = staticRows.get(position);
            if (type == FORUM && mForumLoading)
                return FORUM_LOADING;
            return type;
        }
        if (Session.getInstance().getConfig().shouldShowKnowledgeBase()) {
	        if (getTopics() == null || (shouldShowArticles() && articles == null)) {
	        	if (position - staticRows.size() == 0)
	        		return LOADING;
	        } else if (shouldShowArticles() && position - staticRows.size() < articles.size()) {
	        	return ARTICLE;
	        } else if (!shouldShowArticles() && position - staticRows.size() < getTopics().size()) {
	        	return TOPIC;
	        }
        }
        return SDK_VERSION;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int type = getItemViewType(position);
        if (type == CONTACT) {
            context.startActivity(new Intent(context, ContactActivity.class));
        } else if (type == FORUM) {
            context.startActivity(new Intent(context, ForumActivity.class));
        } else if (type == TOPIC) {
            Utils.showModel(context, (BaseModel) getItem(position));
        } else if (type == ARTICLE) {
            Article article = (Article) getItem(position);
            Intent intent = new Intent(context, ArticleActivity.class);
            intent.putExtra(Article.class.getName(), new ArrayList<Article>(articles));
            intent.putExtra(ArticleActivity.POSITION, articles.indexOf(article));
            context.startActivity(intent);
        }
    }
}
