package com.asus.updatesdk.cdn;

import com.asus.updatesdk.activity.ZenFamilyActivity;
import com.asus.updatesdk.utility.DeviceUtils;
import com.asus.updatesdk.utility.GeneralUtils;
import com.asus.updatesdk.utility.PreferenceUtils;
import com.asus.updatesdk.utility.SystemPropertiesReflection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

/**
 * Created by Chauncey_Li on 2015/6/25.
 */
public class CdnUtils {

    private static final String TAG = "CdnUtils" ;
    private static final String NODE_CDN_VERSION = "cdn_version";
    private static final String NODE_ZENUI_APPS = "zenui_apps";
    private static final String NODE_IUD = "IUD";
    public static final String NODE_FEPL = "FEPL";
    private static final String NODE_INFO = "INFO";
    private static final String NODE_ROOT = "Root";
    private static final String NODE_ASUS_FEPL_PATH = "FEPL_PATH";
    private static final String NODE_UTA_FEPL_PATH = "UTA_FEPL_PATH";
    public static final String NODE_STRING_PATH = "STRING_PATH";
    public static final String NODE_APPS_ARRAY = "apps";
    public static final String NODE_PACKAGE = "package_name";
    public static final String NODE_VERSION = "version_code";
    public static final String NODE_DOWNLOAD = "download";
    public static final String NODE_RATING = "rating";
    public static final String NODE_TOPIC = "topic";
    public static final String NODE_APK = "apk";
    public static final String NODE_BLACKLIST = "blacklist";
    private static final String NODE_ASUS_DESC_ID = "desc_id";
    private static final String NODE_UTA_DESC_ID = "UTA_desc_id";
    public static final String NODE_SPONSOR = "sponsor";
    public static final String NODE_CUSTOMIZED = "customized";
    public static final String NODE_IMAGE_URL = "imageUrl";
    public static final String NODE_ICON_URL = "iconUrl";
    public static final String NODE_ASUS_URL = "URL";
    public static final String NODE_UTA_URL = "UTA_URL";
    public static final String NODE_PAD_URL = "pad_URL";
    public static final String NODE_PHONE_URL = "phone_URL";
    public static final String NODE_PLAY_FEPL_URL = "play_fepl_url";
    public static final String NODE_PLAY_ICON_URL = "play_icon_url";
    public static final String NODE_SLOGAN = "slogan";
    public static final String NODE_STATUS = "stage";
    public static final String NODE_URGENT = "Urgent";

    private static final String CDN_URL_RES = "http://dlcdnamax.asus.com/Rel/App/ZenUI_IUD/res";
    private static final String CDN_URL_PLAY
            = "http://dlcdnamax.asus.com/Rel/SDK/IUD/play.json.gz";
    private static final String CDN_URL_IUD
            = "http://dlcdnamax.asus.com/Rel/App/ZenUI_IUD/IUD.json";
    private static final String CDN_URL_TEST_PLAY
            = "http://amaxcdntest.asus.com/ZenUI_IUD/play.json.gz";
    private static final String CDN_URL_TEST_IUD = "http://amaxcdntest.asus.com/ZenUI_IUD/IUD.json";
    private static final String CDN_URL_TEST_RES = "http://amaxcdntest.asus.com/ZenUI_IUD/res";
    private static final String CDN_URL_TEST_ROOT = "http://amaxcdntest.asus.com/ZenUI_IUD/";
    private static final String CDN_STRING_FILE = "strings.json";
    private static final String CDN_ICON_FILE = ".png";
    private static final String CDN_ICON_PATH = "Icon";

    public static final String KEY_MASTER_PACKAGE = "master_package";
    public static final String KEY_SLAVE_PACKAGE = "slave_package";
    public static final String KEY_IUD_VERSION = "iud_version";
    public static final String KEY_IUD_JSON_FILE = "iud_json_file";
    public static final String KEY_STRINGS_VERSION = "strings_version";
    public static final String KEY_STRINGS_JSON_FILE = "strings_json_file";
    public static final String KEY_LANGUAGE_LOCALE = "language_local";
    public static final String KEY_PLAY_JSON_FILE = "play_json_file";
    public static final String KEY_PLAY_CHECK_TIME = "play_check_time";
    public static final String KEY_PASSED_MASTER = "passed_master";
    public static final String KEY_PASSED_IUD_VERSION = "passed_iud_ver";
    public static final String KEY_PASSED_IUD_JSON_FILE = "passed_iud_json_file";
    public static final String KEY_PASSED_STRINGS_VERSION = "passed_strings_ver";
    public static final String KEY_PASSED_STRINGS_JSON_FILE = "passed_strings_json_file";
    public static final String KEY_PASSED_PLAY_CHECK_TIME = "passed_play_time";
    public static final String KEY_PASSED_PLAY_JSON_FILE = "passed_play_json_file";
    public static final String KEY_PASSED_LANGUAGE = "passed_lang";

    private static final int TIMEOUT_URL_CONNECTION_MILLISECONDS = 1000;
    private static final int TIMEOUT_URL_READ_MILLISECONDS = 1000;
    private static final String REQUEST_METHOD_GET = "GET";
    private static final String REQUEST_METHOD_POST = "POST";

    private static final String SLACK_WEBHOOK_URL
            = "https://hooks.slack.com/services/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";
    private static final String SLACK_WEBHOOK_PAYLOAD_TEXT = "text";

    public static final String DEFAULT_LANGUAGE = "en-rUS";

    private static final boolean DEBUG = SystemPropertiesReflection
            .getBoolean("debug.cdn_path", false);

    private static final int TIMEOUT_GET_MASTER_JSON_MILLISECONDS = 250;
    private static final String ACTION_UPDATE = "com.asus.zenuifamily.action.UPDATE";
    public static final String ACTION_REQUEST_MASTER = "com.asus.zenuifamily.action.REQUEST_MASTER";
    public final static String ACTION_SEND_MASTER_DATA = "com.asus.zenuifamily.action.SEND_MASTER";
    public static final Object LOCK = new Object();
    private static final boolean ASUS_DEVICE = DeviceUtils.checkAsusDevice();

    private static final long DEFAULT_POLLING_CDN_INTERVAL = 3 * DateUtils.DAY_IN_MILLIS;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getJSONFromUrl(String urlString) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(REQUEST_METHOD_GET);
            urlConnection.setConnectTimeout(TIMEOUT_URL_CONNECTION_MILLISECONDS);
            urlConnection.setReadTimeout(TIMEOUT_URL_READ_MILLISECONDS);
            urlConnection.connect();
            try (InputStream input = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, "utf-8"))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while (null != (line = reader.readLine())) {
                    sb.append(line);
                    sb.append("\n");
                }
                return sb.toString();
            }
        } catch (IOException e) {
            Log.e(TAG, "getJSON from\" " + urlString + "\" failed");
            return null;
        }
        finally{
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

    private static void sendUpdateBroadcast(Context context) {
        String currentLocale = Locale.getDefault().toString();
        Intent intentUpdate = new Intent(ACTION_UPDATE);
        intentUpdate.putExtra(KEY_IUD_VERSION, GeneralUtils.getCDNIudVersion());
        intentUpdate.putExtra(KEY_STRINGS_VERSION, GeneralUtils.getCDNStringsVersion());
        intentUpdate.putExtra(KEY_MASTER_PACKAGE, context.getPackageName());
        intentUpdate.putExtra(KEY_LANGUAGE_LOCALE, currentLocale);
        intentUpdate.putExtra(KEY_IUD_JSON_FILE,
                PreferenceUtils.getString(context, PreferenceUtils.KEY_IUD_JSON_FILE, null));
        intentUpdate.putExtra(KEY_STRINGS_JSON_FILE,
                PreferenceUtils.getString(context, PreferenceUtils.KEY_STRINGS_JSON_FILE, null));
        intentUpdate.putExtra(KEY_PLAY_JSON_FILE,
                PreferenceUtils.getString(context, PreferenceUtils.KEY_PLAY_JSON_FILE, null));
        intentUpdate.putExtra(KEY_PLAY_CHECK_TIME,
                PreferenceUtils.getLong(context, PreferenceUtils.KEY_PLAY_CHECK_TIME, 0));
        context.sendBroadcast(intentUpdate);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getJsonGzipFromUrl(String urlString){
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(REQUEST_METHOD_GET);
            urlConnection.setConnectTimeout(TIMEOUT_URL_CONNECTION_MILLISECONDS);
            urlConnection.setReadTimeout(TIMEOUT_URL_READ_MILLISECONDS);
            urlConnection.connect();
            try (InputStream input = urlConnection.getInputStream();
                 GZIPInputStream gzip = new GZIPInputStream(input);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(gzip))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while (null != (line = reader.readLine())) {
                    sb.append(line);
                    sb.append("\n");
                }
                return sb.toString();
            }
        } catch (IOException e) {
            Log.e(TAG, "getJSON from\" " + urlString + "\" failed");
            return null;
        }
        finally{
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

    @Nullable
    private static String getStringsJsonFromCdn(){
        Log.v(TAG,"getStringsJsonFromCdn");
        String currentLocale = Locale.getDefault().toString();
        String resPath = DEBUG ? CDN_URL_TEST_RES : CDN_URL_RES ;
        String languageAndRegion = currentLocale.replace("_", "-r");
        String jsonStrings = getJSONFromUrl(
                resPath + "/" + languageAndRegion+ "/" + CDN_STRING_FILE);
        if(null == jsonStrings){
            jsonStrings = getJSONFromUrl(
                    resPath + "/" + DEFAULT_LANGUAGE + "/" + CDN_STRING_FILE);
        }
        return  jsonStrings;
    }

    @Nullable
    private static String getIudJsonFromCdn(){
        Log.v(TAG,"getIudJsonFromCdn");
        String iudPath = DEBUG ? CDN_URL_TEST_IUD : CDN_URL_IUD;
        return getJSONFromUrl(iudPath);
    }

    @Nullable
    private static String getPlayJsonFromCdn(){
        Log.v(TAG,"getPlayJsonFromCdn");
        String playPath = DEBUG ? CDN_URL_TEST_PLAY : CDN_URL_PLAY;
        return getJsonGzipFromUrl(playPath);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static JSONObject mergeJson(Context context, String jsonPlay, String jsonIud,
            String jsonStrings) {
        try {
            JSONObject mergedJson = new JSONObject();
            JSONObject iudInfo = new JSONObject(jsonIud);
            JSONArray iudJSONArray = iudInfo.getJSONArray(NODE_IUD);
            JSONArray feplJSONArray = iudInfo.getJSONArray(NODE_FEPL);
            JSONObject stringsObject = new JSONObject(jsonStrings);
            JSONObject playInfo = new JSONObject(jsonPlay);
            JSONObject infoObject = iudInfo.getJSONObject(NODE_INFO);
            String rootPath = DEBUG ? CDN_URL_TEST_ROOT : infoObject.getString(NODE_ROOT);
            String feplPath = ASUS_DEVICE ? infoObject.getString(NODE_ASUS_FEPL_PATH)
                                          : infoObject.getString(NODE_UTA_FEPL_PATH);
            if (iudJSONArray == null || iudJSONArray.length() == 0) return null;
            JSONObject playAppsObject = playInfo.getJSONObject(NODE_ZENUI_APPS);
            mergedJson.put(NODE_CDN_VERSION, playInfo.getString(NODE_CDN_VERSION));
            mergedJson.put(NODE_INFO, infoObject);
            JSONArray feplArray = new JSONArray();
            for (int i = 0; i < feplJSONArray.length(); i++){
                JSONObject feplObject = iudInfo.getJSONArray(NODE_FEPL).getJSONObject(i);
                String packageName = feplObject.getString(NODE_PACKAGE);
                JSONObject appObject = playAppsObject.optJSONObject(packageName);
                if(!"1".equals(feplObject.optString(NODE_SPONSOR))){
                    if(null == appObject ) {
                        Log.w(TAG, "There is no app: " + packageName + " data");
                        continue;
                    }
                    feplObject.put(NODE_PLAY_FEPL_URL, appObject.getString(NODE_PLAY_FEPL_URL));
                    feplObject.put(NODE_BLACKLIST, appObject.getString(NODE_BLACKLIST));
                    feplObject.put(NODE_APK, appObject.getJSONArray(NODE_APK));
                }
                //FEPL cdn path
                String feplImageUrl = rootPath + "/" + feplPath + "/" + packageName + CDN_ICON_FILE;
                feplObject.put(NODE_IMAGE_URL, feplImageUrl);
                feplArray.put(feplObject);
            }
            mergedJson.put(NODE_FEPL, feplArray);
            JSONArray appArray = new JSONArray();
            for (int i = 0; i < iudJSONArray.length(); i++){
                JSONObject appInfoObject = iudJSONArray.getJSONObject(i);
                String packageName = appInfoObject.getString(NODE_PACKAGE);
                JSONObject appStringObject = stringsObject.getJSONObject(packageName);
                //query play info
                JSONObject appObject = playAppsObject.optJSONObject(packageName);
                if(null == appObject) {
                    Log.w(TAG, "There is no app: " + packageName + " data");
                    continue;
                }
                String iconUrl = rootPath + "/" + CDN_ICON_PATH + "/" + packageName + CDN_ICON_FILE;
                String descId = appInfoObject
                        .getString(ASUS_DEVICE ? NODE_ASUS_DESC_ID : NODE_UTA_DESC_ID);
                appInfoObject.put(NODE_SLOGAN, appStringObject.getString(descId));
                appInfoObject.put(NODE_ICON_URL, iconUrl);
                appInfoObject.put(NODE_PLAY_ICON_URL, appObject.getString(NODE_PLAY_ICON_URL));
                appInfoObject.put(NODE_DOWNLOAD, appObject.getString(NODE_DOWNLOAD));
                appInfoObject.put(NODE_RATING, appObject.getString(NODE_RATING));
                appInfoObject.put(NODE_BLACKLIST, appObject.getString(NODE_BLACKLIST));
                appInfoObject.put(NODE_APK, appObject.getJSONArray(NODE_APK));
                appArray.put(appInfoObject);
            }
            mergedJson.put(NODE_APPS_ARRAY, appArray);
            PreferenceUtils.putBoolean(context, PreferenceUtils.KEY_MERGE_JSON_SUCCESS,
                    true);
            return mergedJson;
        } catch (JSONException e) {
            Log.e(TAG, "merge JSON failed");
            Log.e(TAG, e.toString());
            if(DEBUG){
                JSONObject payload = new JSONObject();
                try {
                    payload.put(SLACK_WEBHOOK_PAYLOAD_TEXT,
                            "CDN JSON file got exception : " + e.toString());
                } catch (JSONException e1) {
                    Log.e(TAG, "payload jsonException");
                    return null;
                }
                sendWebhookToSlack(payload);
            }
            PreferenceUtils.putBoolean(context, PreferenceUtils.KEY_MERGE_JSON_SUCCESS, false);
            GeneralUtils.openZenFamilyPage(context);
            ((Activity) context).finish();
            return null;
        }
    }

    private static void sendWebhookToSlack(JSONObject payload){
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(SLACK_WEBHOOK_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod(REQUEST_METHOD_POST);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream(),
                    "UTF-8");
            writer.write(payload.toString());
            writer.flush();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.v(TAG, line);
            }
            writer.close();
        } catch (IOException IOexception) {
            Log.e(TAG, "send webhook error");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Nullable
    public static JSONObject getJson(Context context) {
        boolean needUpdate = false;
        ZenFamilyActivity activity = null;
        if(context instanceof ZenFamilyActivity)
        {
            activity = (ZenFamilyActivity) context;
        }
        String masterPackage = PreferenceUtils
                .getString(context, PreferenceUtils.KEY_MASTER_PACKAGE, null);
        if (null == masterPackage) {
            getMasterInfos(context);
        }
        boolean hasMergeSuccess = PreferenceUtils
                .getBoolean(context, PreferenceUtils.KEY_MERGE_JSON_SUCCESS, true);
        //Check IUD & ImageCache Clear
        long localIudVersion = PreferenceUtils.getLong(context,
                PreferenceUtils.KEY_IUD_VERSION, 0L);
        String iudJsonString = PreferenceUtils
                .getString(context, PreferenceUtils.KEY_IUD_JSON_FILE, null);
        if (GeneralUtils.getCDNIudVersion() > localIudVersion || null == iudJsonString
                || !hasMergeSuccess) {
            needUpdate = true;
            if(null != activity) {
                activity.getImageFetcher().clearCache();
            }
            iudJsonString = getIudJsonFromCdn();
            PreferenceUtils.putString(context, PreferenceUtils.KEY_IUD_JSON_FILE, iudJsonString);
            PreferenceUtils.putLong(
                    context, PreferenceUtils.KEY_IUD_VERSION, GeneralUtils.getCDNIudVersion());
        }
        if (null == iudJsonString ||
                (activity != null && GeneralUtils.isActivityDestroyed(activity))) return null;
        //check Strings
        String stringsJsonString = PreferenceUtils
                .getString(context, PreferenceUtils.KEY_STRINGS_JSON_FILE, null);
        String currentLocale = Locale.getDefault().toString();
        String localLanguageAndRegion = PreferenceUtils.getString(context,
                PreferenceUtils.KEY_LANGUAGE_LOCALE, "en_US");
        long localStringsVersion = PreferenceUtils
                .getLong(context, PreferenceUtils.KEY_STRINGS_VERSION, 0L);
        if (GeneralUtils.getCDNStringsVersion() > localStringsVersion || !currentLocale
                .equals(localLanguageAndRegion) || null == stringsJsonString || !hasMergeSuccess) {
            needUpdate = true;
            stringsJsonString = getStringsJsonFromCdn();
            PreferenceUtils.putString(
                    context, PreferenceUtils.KEY_LANGUAGE_LOCALE, currentLocale);
            PreferenceUtils.putString(
                    context, PreferenceUtils.KEY_STRINGS_JSON_FILE, stringsJsonString);
            PreferenceUtils.putLong(
                    context, PreferenceUtils.KEY_STRINGS_VERSION,
                    GeneralUtils.getCDNStringsVersion());
        }
        if (null == stringsJsonString || (activity != null && GeneralUtils.isActivityDestroyed(
                activity))) return null;
        //check refresh time
        String playJsonString = PreferenceUtils
                .getString(context, PreferenceUtils.KEY_PLAY_JSON_FILE, null);
        long checkTime = PreferenceUtils
                .getLong(context, PreferenceUtils.KEY_PLAY_CHECK_TIME, 0L);
        if (System.currentTimeMillis() > checkTime || null == playJsonString || !hasMergeSuccess) {
            needUpdate = true;
            playJsonString = getPlayJsonFromCdn();
            checkTime =  System.currentTimeMillis() + DEFAULT_POLLING_CDN_INTERVAL;
            PreferenceUtils.putString(
                    context, PreferenceUtils.KEY_PLAY_JSON_FILE, playJsonString);
            PreferenceUtils.putLong(context, PreferenceUtils.KEY_PLAY_CHECK_TIME,
                    checkTime);
        }
        if (null == playJsonString || (activity != null && GeneralUtils.isActivityDestroyed(
                activity))) return null;
        if (needUpdate) {
            masterPackage = context.getPackageName();
            PreferenceUtils
                    .putString(context, PreferenceUtils.KEY_MASTER_PACKAGE, masterPackage);
            sendUpdateBroadcast(context);
        }
        if (activity != null && GeneralUtils.isActivityDestroyed(activity)) return null;
        return mergeJson(context, playJsonString, iudJsonString, stringsJsonString);
    }

    @Nullable
    public static JSONObject getJsonNoNetwork(Context context) {
        String masterPackage = PreferenceUtils
                .getString(context, PreferenceUtils.KEY_MASTER_PACKAGE, null);
        if (null == masterPackage) {
            getMasterInfos(context);
        }
        String iudJsonString = PreferenceUtils
                .getString(context, PreferenceUtils.KEY_IUD_JSON_FILE, null);
        String stringsJsonString = PreferenceUtils
                .getString(context, PreferenceUtils.KEY_STRINGS_JSON_FILE, null);
        String playJsonString = PreferenceUtils
                .getString(context, PreferenceUtils.KEY_PLAY_JSON_FILE, null);
        if (null == iudJsonString || null == stringsJsonString || null == playJsonString) {
            return null;
        }
        return mergeJson(context, playJsonString, iudJsonString, stringsJsonString);
    }

    private static void getMasterInfos(Context context) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                synchronized (LOCK) {
                    Log.v(TAG, "Receive Master info");
                    PreferenceUtils.putLong(context, PreferenceUtils.KEY_IUD_VERSION,
                            intent.getLongExtra(KEY_PASSED_IUD_VERSION, 0L));
                    PreferenceUtils.putLong(context, PreferenceUtils.KEY_STRINGS_VERSION,
                            intent.getLongExtra(KEY_PASSED_STRINGS_VERSION, 0L));
                    PreferenceUtils.putString(context, PreferenceUtils.KEY_IUD_JSON_FILE,
                            intent.getStringExtra(KEY_PASSED_IUD_JSON_FILE));
                    PreferenceUtils.putString(context, PreferenceUtils.KEY_STRINGS_JSON_FILE,
                            intent.getStringExtra(KEY_PASSED_STRINGS_JSON_FILE));
                    PreferenceUtils.putString(context, PreferenceUtils.KEY_PLAY_JSON_FILE,
                            intent.getStringExtra(KEY_PASSED_PLAY_JSON_FILE));
                    PreferenceUtils.putString(context, PreferenceUtils.KEY_MASTER_PACKAGE,
                            intent.getStringExtra(KEY_PASSED_MASTER));
                    PreferenceUtils.putString(context, PreferenceUtils.KEY_LANGUAGE_LOCALE,
                            intent.getStringExtra(KEY_PASSED_LANGUAGE));
                    PreferenceUtils.putLong(context, PreferenceUtils.KEY_PLAY_CHECK_TIME,
                            intent.getLongExtra(KEY_PASSED_PLAY_CHECK_TIME, 0L));
                    LOCK.notify();
                }
            }
        };
        synchronized (LOCK) {
            try {
                context.registerReceiver(receiver, new IntentFilter(ACTION_SEND_MASTER_DATA));
                Intent intent = new Intent(ACTION_REQUEST_MASTER);
                intent.putExtra(KEY_SLAVE_PACKAGE, context.getPackageName());
                Log.v(TAG, "Send query broadcast from " + context.getPackageName());
                context.sendBroadcast(intent);
                LOCK.wait(TIMEOUT_GET_MASTER_JSON_MILLISECONDS);
                context.unregisterReceiver(receiver);
            } catch (InterruptedException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }
}
