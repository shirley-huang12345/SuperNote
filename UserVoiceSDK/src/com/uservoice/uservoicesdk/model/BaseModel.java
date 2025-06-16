package com.uservoice.uservoicesdk.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Html;
import android.text.TextUtils;

import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.rest.RestMethod;
import com.uservoice.uservoicesdk.rest.RestResult;
import com.uservoice.uservoicesdk.rest.RestTask;
import com.uservoice.uservoicesdk.rest.RestTaskCallback;


public class BaseModel {

    protected int id;

    private static Vector<RestTask> mRestTaskVector = new Vector<RestTask>();
    public static ExecutorService mFixedThreadPool = Executors.newFixedThreadPool(10);

    public void load(JSONObject object) throws JSONException {
        id = object.getInt("id");
    }

    public int getId() {
        return id;
    }

    public boolean persist(SharedPreferences prefs, String prefsKey, String rootKey) {
        JSONObject object = new JSONObject();
        JSONObject container = new JSONObject();
        try {
            save(object);
            container.put(rootKey, object);
        } catch (JSONException e) {
            return false;
        }
        Editor edit = prefs.edit();
        edit.putString(prefsKey, container.toString());
        return edit.commit();
    }

    public static <T extends BaseModel> T load(SharedPreferences prefs, String prefsKey, String rootKey, Class<T> modelClass) {
        try {
            JSONObject container = new JSONObject(prefs.getString(prefsKey, "{}"));
            return deserializeObject(container, rootKey, modelClass);
        } catch (JSONException e) {
            return null;
        }
    }

    public void save(JSONObject object) throws JSONException {
        object.put("id", id);
    }

    protected String getString(JSONObject object, String key) throws JSONException {
        return object.isNull(key) ? null : Html.fromHtml(object.getString(key)).toString().trim();
    }

    protected String getHtml(JSONObject object, String key) throws JSONException {
        return object.isNull(key) ? null : object.getString(key);
    }

    @SuppressLint("SimpleDateFormat")
    protected Date getDate(JSONObject object, String key) throws JSONException {
        String dateString = getString(object, key);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
        try {
            return format.parse(dateString);
        } catch (ParseException e1) {
            throw new JSONException("Could not parse date: " + dateString);
        }
    }

    protected static Session getSession() {
        return Session.getInstance();
    }

    protected static Config getConfig() {
        return getSession().getConfig();
    }

    protected static ClientConfig getClientConfig() {
        return getSession().getClientConfig();
    }

    protected static String apiPath(String path, Object... args) {
        return "/api/v1" + String.format(Locale.US, path, args);
    }

    protected static RestTask doGet(String path, RestTaskCallback callback) {
        return doGet(path, null, callback);
    }

    protected static RestTask doPost(String path, RestTaskCallback callback) {
        return doPost(path, null, callback);
    }

    protected static RestTask doDelete(String path, RestTaskCallback callback) {
        return doDelete(path, null, callback);
    }

    protected static RestTask doPut(String path, RestTaskCallback callback) {
        return doPut(path, null, callback);
    }

    protected static RestTask doGet(String path, Map<String, String> params, RestTaskCallback callback) {
        RestTask task = new RestTask(RestMethod.GET, path, params, callback) {
            @Override
            protected void onPostExecute(RestResult result) {
                super.onPostExecute(result);
                mRestTaskVector.remove(this);
            }
        };
        mRestTaskVector.add(task);
        task.executeOnExecutor(mFixedThreadPool);
        return task;
    }

    protected static RestTask doPost(String path, Map<String, String> params, RestTaskCallback callback) {
        RestTask task = new RestTask(RestMethod.POST, path, params, callback) {
            @Override
            protected void onPostExecute(RestResult result) {
                super.onPostExecute(result);
                mRestTaskVector.remove(this);
            }
        };
        mRestTaskVector.add(task);
        task.executeOnExecutor(mFixedThreadPool);
        return task;
    }

    protected static RestTask doDelete(String path, Map<String, String> params, RestTaskCallback callback) {
        RestTask task = new RestTask(RestMethod.DELETE, path, params, callback) {
            @Override
            protected void onPostExecute(RestResult result) {
                super.onPostExecute(result);
                mRestTaskVector.remove(this);
            }
        };
        mRestTaskVector.add(task);
        task.executeOnExecutor(mFixedThreadPool);
        return task;
    }

    protected static RestTask doPut(String path, Map<String, String> params, RestTaskCallback callback) {
        RestTask task = new RestTask(RestMethod.PUT, path, params, callback) {
            @Override
            protected void onPostExecute(RestResult result) {
                super.onPostExecute(result);
                mRestTaskVector.remove(this);
            }
        };
        mRestTaskVector.add(task);
        task.executeOnExecutor(mFixedThreadPool);
        return task;
    }

    protected static <T extends BaseModel> List<T> deserializeList(JSONObject object, String rootKey, Class<T> modelClass) throws JSONException {
        if (!object.has(rootKey))
            return null;
        try {
            JSONArray array = object.getJSONArray(rootKey);
            List<T> list = new ArrayList<T>(array.length());
            for (int i = 0; i < array.length(); i++) {
                T model = modelClass.newInstance();
                model.load(array.getJSONObject(i));
                list.add(model);
            }
            return list;
        } catch (IllegalArgumentException e) {
            throw new JSONException("Reflection failed trying to call load on " + modelClass + " " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new JSONException("Reflection failed trying to call load on " + modelClass + " " + e.getMessage());
        } catch (InstantiationException e) {
            throw new JSONException("Reflection failed trying to instantiate " + modelClass + " " + e.getMessage());
        }
    }

    protected static <T extends BaseModel> T deserializeObject(JSONObject object, String rootKey, Class<T> modelClass) throws JSONException {
        if (!object.has(rootKey))
            return null;
        try {
            JSONObject singleObject = object.getJSONObject(rootKey);
            T model = modelClass.newInstance();
            model.load(singleObject);
            return modelClass.cast(model);
        } catch (JSONException e) {
            throw new JSONException("JSON deserialization failure for " + modelClass + " " + e.getMessage() + " JSON: " + object.toString());
        } catch (IllegalArgumentException e) {
            throw new JSONException("Reflection failed trying to call load on " + modelClass + " " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new JSONException("Reflection failed trying to call load on " + modelClass + " " + e.getMessage());
        } catch (InstantiationException e) {
            throw new JSONException("Reflection failed trying to instantiate " + modelClass + " " + e.getMessage());
        }
    }

    protected static List<BaseModel> deserializeHeterogenousList(JSONObject object, String rootKey) throws JSONException {
        if (!object.has(rootKey))
            return null;
        JSONArray array = object.getJSONArray(rootKey);
        List<BaseModel> list = new ArrayList<BaseModel>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.getJSONObject(i);
            String type = o.getString("type");
            BaseModel model;

            if (type.equals("suggestion")) {
                model = new Suggestion();
            } else if (type.equals("article")) {
                model = new Article();

            } else {
                continue;
            }

            model.load(o);
            if (model instanceof Article && TextUtils.isEmpty(((Article) model).getTopicName())) {
                continue;
            }
            list.add(model);
        }
        return list;
    }

    public static void cancelTask() {
        Iterator<RestTask> i = mRestTaskVector.iterator();
        while (i.hasNext()) {
            RestTask restTask = i.next();
            if (!restTask.isCancelled()) {
                restTask.cancel(true);
            }else{
                i.remove();
            }
        }
    }

}
