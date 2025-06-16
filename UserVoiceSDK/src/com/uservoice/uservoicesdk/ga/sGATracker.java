package com.uservoice.uservoicesdk.ga;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import android.text.TextUtils;

public class sGATracker {
    public class Fields {

        public static final String TRACKING_ID = "tid";
        public static final String APP_ID = "aid";
        public static final String APP_NAME = "an";
        public static final String APP_VERSION = "av";
        public static final String CLIENT_ID = "cid";
        public static final String EVENT_ACTION = "ea";
        public static final String EVENT_CATEGORY = "ec";
        public static final String EVENT_LABEL = "el";
        public static final String EVENT_VALUE = "ev";
    }

    private static final String API_URL = "http://www.google-analytics.com/collect";

    private static sGATracker intance;
    private String gaId;
    private Context appContext;
    private ExecutorService executorService;

    private Map<String, String> customFeild;

    public static sGATracker getInstance(Context context, String gaId) {
        if (intance == null) {
            intance = new sGATracker(context, gaId);
        }

        return intance;
    }

    private sGATracker() {

    }

    public static void reset(){
        intance = null;
    }

    private sGATracker(Context context, String gaId) {
        this.gaId = gaId;
        appContext = context.getApplicationContext();
        executorService = Executors.newFixedThreadPool(10);
        customFeild = new HashMap<String, String>();
    }

    public void set(String key, String value) {
        customFeild.put(key, value);
    }

    public void sendEvent(String category, String action, String label, Long value) {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put(Fields.EVENT_CATEGORY, category);
        params.put(Fields.EVENT_ACTION, action);
        params.put(Fields.EVENT_LABEL, label);

        if (value != null) {
            params.put(Fields.EVENT_VALUE, String.valueOf(value));
        }

        executorService.execute(new Runnable() {

            @Override
            public void run() {
                sendSync(params);
            }
        });
    }

    public void send(final Map<String, String> params) {

        executorService.execute(new Runnable() {

            @Override
            public void run() {
                sendSync(params);
            }
        });
    }

    private void sendSync(Map<String, String> params) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, String.format("%s/%s (Linux; U; Android %s; %s; %s Build/%s)", "GoogleAnalytics", "4.0", "56", getLanguage(Locale.getDefault()), Build.MODEL, Build.ID));
            client.execute(getBaseHttpPost(params));
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HttpPost getBaseHttpPost(Map<String, String> params) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(API_URL);
        httpPost.setEntity(new UrlEncodedFormEntity(getParams(params)));
        return httpPost;
    }

    public static String getLanguage(Locale locale) {
        if(locale == null) {
            return null;
        } else if(TextUtils.isEmpty(locale.getLanguage())) {
            return null;
        } else {
            StringBuilder result = new StringBuilder();
            result.append(locale.getLanguage().toLowerCase());
            if(!TextUtils.isEmpty(locale.getCountry())) {
            	result.append("-").append(locale.getCountry().toLowerCase());
            }

            return result.toString();
        }
    }

    private List<NameValuePair> getBaseParams() {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(Fields.TRACKING_ID, gaId));
        nameValuePairs.add(new BasicNameValuePair("v", "1"));
        nameValuePairs.add(new BasicNameValuePair("t", "event"));
        nameValuePairs.add(new BasicNameValuePair(Fields.CLIENT_ID, getAndroidId()));

        for (String key : customFeild.keySet()) {
            nameValuePairs.add(new BasicNameValuePair(key, customFeild.get(key)));
        }

        return nameValuePairs;
    }

    private List<NameValuePair> getParams(Map<String, String> params) {
        List<NameValuePair> nameValuePairs = getBaseParams();

        for (String key : params.keySet()) {
            nameValuePairs.add(new BasicNameValuePair(key, params.get(key)));
        }

        return nameValuePairs;
    }

    private String getAndroidId() {
        return Secure.getString(appContext.getContentResolver(), Secure.ANDROID_ID);
    }
}
