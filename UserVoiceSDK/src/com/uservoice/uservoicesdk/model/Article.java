package com.uservoice.uservoicesdk.model;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.cache.SCache;
import com.uservoice.uservoicesdk.rest.Callback;
import com.uservoice.uservoicesdk.rest.RestResult;
import com.uservoice.uservoicesdk.rest.RestTask;
import com.uservoice.uservoicesdk.rest.RestTaskCallback;

public class Article extends BaseModel implements Parcelable {

    private String title;
    private String html;
    private String topicName;
    private int weight;
    private Date updatedAt;
    private Date createdAt;
    private static AsyncTask mCacheTask;

    public Article() {
    }

    public static void loadPage(int page, final Callback<List<Article>> callback) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sort", "ordered");
        params.put("per_page", "50");
        params.put("page", String.valueOf(page));
        doGet(apiPath("/articles.json"), params, new RestTaskCallback(callback) {
            @Override
            public void onComplete(JSONObject result) throws JSONException {
                callback.onModel(deserializeList(result, "articles", Article.class));
            }
        });
    }

    public static void loadPageForTopic(final int topicId, int page, final Callback<List<Article>> callback) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sort", "ordered");
        params.put("per_page", "50");
        params.put("page", String.valueOf(page));



        RestTaskCallback cacheTaskCallback = new RestTaskCallback(callback) {
            @Override
            public void onComplete(JSONObject result) throws JSONException {
                callback.onModel(deserializeList(result, "articles", Article.class));
            }

            @Override
            public void onError(RestResult result) {
                // do-nothing
            }
        };
        if(mCacheTask != null) {
        	mCacheTask.cancel(true);
        }
        mCacheTask = SCache.GetCache(Session.getInstance().getContext(), String.valueOf(topicId), cacheTaskCallback);

        RestTaskCallback restTaskCallback = new RestTaskCallback(callback) {
            @Override
            public void onComplete(JSONObject result) throws JSONException {
                if(mCacheTask != null) {
                    mCacheTask.cancel(true);
                    mCacheTask = null;
                }
                callback.onModel(deserializeList(result, "articles", Article.class));
                SCache.PutCache(Session.getInstance().getContext(), String.valueOf(topicId), result);
            }
        };

        doGet(apiPath("/topics/%d/articles.json", topicId), params, restTaskCallback);


    }

    public static void loadPageForTopicNetworkFirst(final int topicId, int page, final Callback<List<Article>> callback) {
    	Map<String, String> params = new HashMap<String, String>();
        params.put("sort", "ordered");
        params.put("per_page", "50");
        params.put("page", String.valueOf(page));
        RestTaskCallback restTaskCallback = new RestTaskCallback(callback) {
            @Override
            public void onComplete(JSONObject result) throws JSONException {
                callback.onModel(deserializeList(result, "articles", Article.class));
                SCache.PutCache(Session.getInstance().getContext(), String.valueOf(topicId), result);
            }
            @Override
            public void onError(RestResult result) {
                // TODO Auto-generated method stub
                RestTaskCallback cacheTaskCallback = new RestTaskCallback(callback) {
                    @Override
                    public void onComplete(JSONObject result) throws JSONException {
                        callback.onModel(deserializeList(result, "articles", Article.class));
                    }
                };
                SCache.GetCache(Session.getInstance().getContext(), String.valueOf(topicId), cacheTaskCallback);
            }
        };

        doGet(apiPath("/topics/%d/articles.json", topicId), params, restTaskCallback);
    }

    public static void loadArticle(int articleId, final Callback<Article> callback){
    	Map<String, String> params = new HashMap<String, String>();
    	params.put("article_id", String.valueOf(articleId));
        RestTaskCallback restTaskCallback = new RestTaskCallback(callback) {
            @Override
            public void onComplete(JSONObject result) throws JSONException {
                callback.onModel(deserializeObject(result, "article", Article.class));
            }
        };

        doGet(apiPath("/articles/%d.json", articleId), params, restTaskCallback);
    }

    public static RestTask loadInstantAnswers(String query, final Callback<List<BaseModel>> callback) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("per_page", "3");
        params.put("query", query);
        Config config = getConfig();
        if (config != null) {
            Log.d("NPECHECKING", "5256: session is " + getSession());
            Log.d("NPECHECKING", "5256: config is " + config);
            params.put("forum_id", String.valueOf(config.getForumId()));
            if (config.getTopicId() != -1) {
                params.put("topic_id", String.valueOf(config.getTopicId()));
            }
        }else{
            Log.d("NPECHECKING", "5256: session is " + getSession());
            Log.d("NPECHECKING", "5256: config is " + config);
        }
        return doGet(apiPath("/instant_answers/search.json"), params, new RestTaskCallback(callback) {
            @Override
            public void onComplete(JSONObject result) throws JSONException {
                callback.onModel(deserializeHeterogenousList(result, "instant_answers"));
            }
        });
    }

    @Override
    public void load(JSONObject object) throws JSONException {
        super.load(object);
        title = getString(object, "question");
        html = getHtml(object, "answer_html");
        updatedAt = getDate(object, "updated_at");
        createdAt = getDate(object, "created_at");
        if (object.has("normalized_weight")) {
            weight = object.getInt("normalized_weight");
        }
        if (!object.isNull("topic")) {
            JSONObject topic = object.getJSONObject("topic");
            topicName = topic.getString("name");
        }
    }

    public String getTitle() {
        return title;
    }

    public String getHtml() {
        return html;
    }

    public String getTopicName() {
        return topicName;
    }

    public int getWeight() {
        return weight;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    //
    // Parcelable
    //

    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        // TODO Auto-generated method stub
        Article a = (Article)o;
        return a.getId() == getId() && a.getUpdatedAt().equals(getUpdatedAt());
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(title);
        out.writeString(html);
        out.writeString(topicName);
        out.writeInt(weight);
        out.writeLong(updatedAt != null ? updatedAt.getTime() : -1);
        out.writeLong(createdAt != null ? createdAt.getTime() : -1);
    }

    public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>() {
        public Article createFromParcel(Parcel in) {
            return new Article(in);
        }

        public Article[] newArray(int size) {
            return new Article[size];
        }
    };

    private Article(Parcel in) {
        id = in.readInt();
        title = in.readString();
        html = in.readString();
        topicName = in.readString();
        weight = in.readInt();
        long tmpUpdatedAt = in.readLong();
        this.updatedAt = tmpUpdatedAt == -1 ? null : new Date(tmpUpdatedAt);
        long tmpCreatedAt = in.readLong();
        this.createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
    }
}
