package com.asus.updatesdk.activity;

import com.asus.updatesdk.AppInfo;
import com.asus.updatesdk.R;
import com.asus.updatesdk.ZenUiFamily;
import com.asus.updatesdk.ZenUiFamily.AppStatus;
import com.asus.updatesdk.analytic.AnalyticUtils;
import com.asus.updatesdk.analytic.TrackerManager;
import com.asus.updatesdk.cache.ImageCache;
import com.asus.updatesdk.cache.ImageFetcher;
import com.asus.updatesdk.cdn.CdnUtils;
import com.asus.updatesdk.utility.DeviceUtils;
import com.asus.updatesdk.utility.GeneralUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.volley.VolleyUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

public class ZenFamilyActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = "ZenFamilyActivity";
    private static final String KEY_APP_LIST = "app_list";
    private static final String KEY_PANEL_LIST = "panel_list";
    private static final String KEY_GTM_IUD_VERSION = "iud_version";
    private static final String KEY_GTM_STRINGS_VERSION = "strings_version";
    private static final String KEY_GTM_THEME_COLOR = "theme_color";
    private static final String KEY_GTM_UPDATE_BUTTON_COLOR = "update_button_color";
    private static final String KEY_GTM_OPEN_BUTTON_COLOR = "open_button_color";
    private static final String KEY_GTM_AUTO_PLAY_MILLIS = "auto_play_millis";
    private static final String KEY_HAS_BUNDLE = "has_bundle";
    private static final String IMAGE_CACHE_DIR = "images";
    private static final String STATUS_BAR_RESOURCE_NAME = "status_bar_height";
    private static final String STATUS_BAR_RESOURCE_DEFAULT_TYPE = "dimen";
    private static final String STATUS_BAR_RESOURCE_DEFAULT_PACKAGE = "android";

    private static final String DATA_SCHEME = "package";

    private static final int STATE_INFO_NON_CHANGE = 1;
    private static final int STATE_INFO_UPDATE = 2;
    private static final int STATE_INIT_FIRST_TIME = 3;

    private static final int SHOW_APP_LIST_SIZE = 10;
    private static final float MEMORY_CACHE_SIZE = 0.25F;

    public static final int VIEW_PAGER_NOT_SCROLLING = 0;
    public static final int VIEW_PAGER_IS_SCROLLING = 1;

    private Activity mActivity;

    private AppInfoAdapter mAppInfoAdapter;
    private ArrayList<AppInfo> mAppInfoList = new ArrayList<>();
    private ArrayList<AppInfo> mPanelList = new ArrayList<>();
    private PanelViewPagerAdapter mPanelViewPagerAdapter;
    private ViewPager mPanelViewPager;
    private AlertDialog mNetworkFailDialog;
    private ImageFetcher mImageFetcher;
    private Button mFooterButton;
    private View mStatusBar;
    private ListView mAppListView;
    private View mFooterView;
    private String mThemeColor = "#0DAFED";
    private String mUpdateButtonColor = "#0DAFED";
    private String mOpenButtonColor = "#0DAFED";
    private Handler mPanelAutoPlayHandler;
    private Runnable mPanelAutoPlayRunnable;
    private int mPanelHeight = 0;
    private int mPanelCount = 0;
    private long mPanelAutoPlayMillis = 6000;
    private boolean mIsNoNetworkReturn = false;

    private BroadcastReceiver mAppUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG , "mUpdateReceiver onReceive");
            FrameLayout syncLayout = (FrameLayout) findViewById(R.id.ud_sdk_sync_layout);
            if (null != syncLayout && View.GONE == syncLayout.getVisibility()) {
                LoadListTask loadListTask = new LoadListTask();
                loadListTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AsusUpdateSdkTheme);
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        setContentView(R.layout.ud_sdk_zenfamily_activity);
        mActivity = this;

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme(DATA_SCHEME);
        registerReceiver(mAppUpdateReceiver, intentFilter);

        initCache();
        setupStatusBar();
        setupActionBar();
        initPanelAutoPlayHandler();

        if (null == savedInstanceState) {
            setContentInvisible();
            initContentLayout();
        } else {
            mAppInfoList = savedInstanceState.getParcelableArrayList(KEY_APP_LIST);
            mPanelList = savedInstanceState.getParcelableArrayList(KEY_PANEL_LIST);
            if (null == mPanelList || 0 == mPanelList.size()) {
                setContentInvisible();
                return;
            }
            getGtmValuesFromBundle(savedInstanceState);
            initContentLayout();
            setContentVisible();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        LoadListTask loadListTask = new LoadListTask();
        loadListTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_APP_LIST, mAppInfoList);
        outState.putParcelableArrayList(KEY_PANEL_LIST, mPanelList);
        outState.putLong(KEY_GTM_IUD_VERSION, GeneralUtils.getCDNIudVersion());
        outState.putLong(KEY_GTM_STRINGS_VERSION, GeneralUtils.getCDNStringsVersion());
        outState.putLong(KEY_GTM_AUTO_PLAY_MILLIS, mPanelAutoPlayMillis);
        outState.putString(KEY_GTM_THEME_COLOR, mThemeColor);
        outState.putString(KEY_GTM_UPDATE_BUTTON_COLOR, mUpdateButtonColor);
        outState.putString(KEY_GTM_OPEN_BUTTON_COLOR, mOpenButtonColor);
        getIntent().putExtra(KEY_HAS_BUNDLE, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mNetworkFailDialog) {
            mNetworkFailDialog.dismiss();
            mNetworkFailDialog = null;
        }
        if (null != mPanelAutoPlayHandler) {
            mPanelAutoPlayHandler.removeCallbacks(mPanelAutoPlayRunnable);
        }
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAppUpdateReceiver);
        mImageFetcher.closeCache();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.ud_sdk_list_view_button_more) {
            Log.v(TAG, "open Google Play ZenFamilyPage");
            GeneralUtils.openZenFamilyPage(mActivity);
        }
    }

    public void setThemeColor(String themeColor, String updateButtonColor, String openButtonColor) {
        mThemeColor = themeColor;
        mUpdateButtonColor = updateButtonColor;
        mOpenButtonColor = openButtonColor;
    }

    public void setPanelAutoPlayMillis(long autoPlayMillis) {
        mPanelAutoPlayMillis = autoPlayMillis;
    }

    public ImageFetcher getImageFetcher() {
        return mImageFetcher;
    }

    private class LoadListTask extends AsyncTask<Void, JSONArray, ArrayList<AppInfo>> {
        @Override
        protected ArrayList<AppInfo> doInBackground(Void... voids) {
            Log.v(TAG, "LoadListTask doInBackground");
            if (!getIntent().getBooleanExtra(KEY_HAS_BUNDLE, false)) {
                Log.v(TAG , "getGtmValues");
                GeneralUtils.getGtmValues(mActivity);
            }
            JSONObject jsonObject = CdnUtils.getJson(mActivity);
            if (null == jsonObject) return null;
            try {
                updatePanelList(jsonObject.getJSONArray(CdnUtils.NODE_FEPL));
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            if (GeneralUtils.isActivityDestroyed(mActivity)) return null;
            return getAppInfoList(jsonObject);
        }

        @Override
        protected void onPostExecute(ArrayList<AppInfo> appInfoList) {
            super.onPostExecute(appInfoList);
            Log.v(TAG, "onPostExecute");
            TrackerManager.sendDailyReport(mActivity, appInfoList);
            if (null == appInfoList) {
                if (!isFinishing() && !GeneralUtils.isActivityDestroyed(mActivity)) {
                    showNetworkFailDialog();
                }
                mIsNoNetworkReturn = true;
                return;
            }
            if (!getIntent().getBooleanExtra(KEY_HAS_BUNDLE, false)) {
                drawItemButtonColor(mThemeColor, mUpdateButtonColor, mOpenButtonColor);
                drawThemeColor(mThemeColor);
            }
            switch (updateAppInfoList(appInfoList)) {
                case STATE_INFO_NON_CHANGE:
                    Log.v(TAG, "STATE_INFO_NON_CHANGE");
                    FrameLayout syncLayout = (FrameLayout) findViewById(R.id.ud_sdk_sync_layout);
                    if (View.GONE != syncLayout.getVisibility()) {
                        refreshActivity();
                    } else {
                        setFooterLayout();
                    }
                    if (mPanelViewPagerAdapter != null) {
                        mPanelViewPagerAdapter.refreshImagePanel();
                    }
                    break;
                case STATE_INFO_UPDATE:
                    Log.v(TAG, "STATE_INFO_UPDATE");
                    mAppInfoAdapter.notifyDataSetChanged();
                    setFooterLayout();
                    break;
                case STATE_INIT_FIRST_TIME:
                    Log.v(TAG, "STATE_INIT_FIRST_TIME");
                    refreshActivity();
                    break;
            }
            if (isPortrait() && !mPanelAutoPlayHandler.hasMessages(0)) {
                mPanelAutoPlayHandler.postDelayed(mPanelAutoPlayRunnable, mPanelAutoPlayMillis);
            }
        }

        @Override
        protected void onProgressUpdate(JSONArray... values) {
            JSONArray feplJsonArray = values[0];
            if (0 == mPanelList.size()) {
                try {
                    for (int i = 0; i < feplJsonArray.length(); i++) {
                        JSONObject feplInfo = feplJsonArray.getJSONObject(i);
                        boolean isSponsor = "1".equals(feplInfo.optString(CdnUtils.NODE_SPONSOR));
                        String packageName = feplInfo.getString(CdnUtils.NODE_PACKAGE);
                        if (!isSponsor && ZenUiFamily.checkBlackList(
                                feplInfo.getString(CdnUtils.NODE_BLACKLIST))) {
                            continue;
                        }
                        String cdnImageUrl = feplInfo.getString(CdnUtils.NODE_IMAGE_URL);
                        String playImageUrl = null;
                        String campaignUrlToPlay;
                        AppStatus status = AppStatus.NOT_INSTALLED;
                        if (isSponsor) {
                            campaignUrlToPlay = DeviceUtils.isVoiceCapable(mActivity)
                                    ? feplInfo.getString(CdnUtils.NODE_PHONE_URL)
                                    : feplInfo.getString(CdnUtils.NODE_PAD_URL);
                        } else {
                            playImageUrl = feplInfo.getString(CdnUtils.NODE_PLAY_FEPL_URL);
                            if (null != playImageUrl && !"1"
                                    .equals(feplInfo.optString(CdnUtils.NODE_CUSTOMIZED))) {
                                playImageUrl = playImageUrl.replace("h150-rw", "h320-rw");
                            } else {
                                //Notify image fetcher we will download image from CDN
                                playImageUrl = null;
                            }
                            if (DeviceUtils.checkAsusDevice()){
                                if ("".equals(feplInfo.optString(CdnUtils.NODE_ASUS_URL))) continue;
                                campaignUrlToPlay = feplInfo.getString(CdnUtils.NODE_ASUS_URL);
                            } else {
                                if ("".equals(feplInfo.optString(CdnUtils.NODE_UTA_URL))) continue;
                                campaignUrlToPlay = feplInfo.getString(CdnUtils.NODE_UTA_URL);
                            }
                            status = ZenUiFamily.getAppUpdateState(mActivity, packageName,
                                    ZenUiFamily.getLatestVersionCode(mActivity,
                                            feplInfo.getJSONArray(CdnUtils.NODE_APK)), 0);
                            if (AppStatus.NOT_SUPPORTED == status || (
                                    packageName.equals(mActivity.getPackageName())
                                            && AppStatus.UP_TO_DATE == status)) continue;
                        }
                        if (!cdnImageUrl.trim().isEmpty()) {
                            AppInfo appinfo = new AppInfo(
                                    feplInfo.getString(CdnUtils.NODE_TOPIC),
                                    packageName,
                                    null,
                                    0,
                                    null,
                                    playImageUrl,
                                    cdnImageUrl,
                                    null,
                                    campaignUrlToPlay,
                                    status);
                            mPanelList.add(appinfo);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void updatePanelList(JSONArray feplJsonArray) {
            publishProgress(feplJsonArray);
        }
    }

    private ArrayList<AppInfo> getAppInfoList(JSONObject jsonObject) {
        ArrayList<AppInfo> appInfoList = new ArrayList<>();
        try {
            JSONArray appsJsonArray = jsonObject.getJSONArray(CdnUtils.NODE_APPS_ARRAY);

            Log.v(TAG, "product devices : " + DeviceUtils.SYSPROP_PRODUCT_DEVICE);
            Log.v(TAG, "CPU abilist : " + DeviceUtils.SYSPROP_CPU_ABILIST);
            Log.v(TAG, "CPU abi : " + DeviceUtils.SYSPROP_CPU_ABI);
            Log.v(TAG, "CPU abi2 : " + DeviceUtils.SYSPROP_CPU_ABI2);
            for (int i = 0; i < appsJsonArray.length(); i++) {
                JSONObject jsonAppInfo = appsJsonArray.getJSONObject(i);
                String packageName = jsonAppInfo.getString(CdnUtils.NODE_PACKAGE);
                String cdnImageUrl = jsonAppInfo.getString(CdnUtils.NODE_ICON_URL);
                String playImageUrl = jsonAppInfo.getString(CdnUtils.NODE_PLAY_ICON_URL);
                if(null != playImageUrl) {
                    playImageUrl = playImageUrl.replace("h150-rw","h64-rw");
                }
                String launchUrl = jsonAppInfo.getString(CdnUtils.NODE_ASUS_URL);
                if (ZenUiFamily.checkBlackList(jsonAppInfo.getString(CdnUtils.NODE_BLACKLIST))) {
                    continue;
                }
                int urgentDate = jsonAppInfo.optInt(CdnUtils.NODE_URGENT);
                AppStatus status = ZenUiFamily.getAppUpdateState(mActivity, packageName,
                        ZenUiFamily.getLatestVersionCode(mActivity,
                                jsonAppInfo.getJSONArray(CdnUtils.NODE_APK)), urgentDate);

                if (AppStatus.NOT_SUPPORTED.equals(status) || (
                        (DeviceUtils.checkAsusDevice() || mActivity.getPackageName().equals(
                                packageName)) && AppStatus.UP_TO_DATE.equals(status))) {
                    continue;
                }
                AppInfo appInfo = new AppInfo(
                        jsonAppInfo.getString(CdnUtils.NODE_TOPIC),
                        packageName,
                        jsonAppInfo.getString(CdnUtils.NODE_SLOGAN),
                        (float) jsonAppInfo.getDouble(CdnUtils.NODE_RATING),
                        jsonAppInfo.getString(CdnUtils.NODE_DOWNLOAD),
                        playImageUrl,
                        cdnImageUrl,
                        GeneralUtils.getAppIcon(mActivity, packageName),
                        launchUrl,
                        status);
                if (getPackageName().equals(packageName) && (AppStatus.NEED_UPDATE == status
                        || AppStatus.IMPORTANT == status)) {
                    appInfoList.add(0, appInfo);
                } else {
                    appInfoList.add(appInfo);
                }
                if (SHOW_APP_LIST_SIZE == appInfoList.size()) {
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        Collections.sort(appInfoList);
        return appInfoList;
    }

    private boolean isPortrait() {
        return Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation;
    }

    private int updateAppInfoList(ArrayList<AppInfo> appInfos) {
        if (0 == mAppInfoList.size()) {
            mAppInfoList.addAll(appInfos);
            return STATE_INIT_FIRST_TIME;
        }
        int size = appInfos.size();
        if (size == mAppInfoList.size()) {
            for (int i = 0; i < size; i++) {
                AppInfo newAppInfo = appInfos.get(i);
                AppInfo oldAppInfo = mAppInfoList.get(i);
                if (!newAppInfo.getStatus().equals(oldAppInfo.getStatus()) ||
                        !newAppInfo.getPackageName().equals(oldAppInfo.getPackageName()) ||
                        !newAppInfo.getSlogan().equals(oldAppInfo.getSlogan())) {
                    mAppInfoList.clear();
                    mAppInfoList.addAll(appInfos);
                    return STATE_INFO_UPDATE;
                }
            }
            return STATE_INFO_NON_CHANGE;
        }
        mAppInfoList.clear();
        mAppInfoList.addAll(appInfos);
        return STATE_INFO_UPDATE;
    }

    private void drawThemeColor(String themeColor) {
        int theme_color = parseColorCode(themeColor);
        mStatusBar.setBackgroundColor(theme_color);
        if (mFooterButton != null) {
            mFooterButton.getBackground().setColorFilter(theme_color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void drawItemButtonColor(String themeColor, String updateButtonColor, String
            openButtonColor) {
        if (null != mAppInfoAdapter) {
            int theme_color = parseColorCode(themeColor);
            int updateButton_color = parseColorCode(updateButtonColor);
            int openButton_color = parseColorCode(openButtonColor);
            mAppInfoAdapter.setItemButtonColor(theme_color, updateButton_color, openButton_color);
        }
    }

    private int parseColorCode(String colorCode) {
        int color;
        try {
            color = Color.parseColor(colorCode);
        } catch (Exception e) {
            color = getResources().getColor(R.color.ud_sdk_system_light_blue);
        }
        return color;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setupStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mStatusBar = findViewById(R.id.ud_sdk_background);
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(ZenUiFamily.getZenUiFamilyTitle());
            actionBar.setBackgroundDrawable(
                    new ColorDrawable(getResources().getColor(R.color.ud_sdk_background_white)));
        }
    }

    private void initContentLayout() {
        mAppListView = (ListView) findViewById(R.id.ud_sdk_list_view);
        if (isPortrait()) {
            mAppListView.addHeaderView(getLayoutInflater()
                    .inflate(R.layout.ud_sdk_listview_item_header_pager, mAppListView, false));
            mPanelViewPagerAdapter = new PanelViewPagerAdapter(this, mPanelList, mImageFetcher);
            mPanelViewPager = (ViewPager) findViewById(R.id.ud_sdk_list_view_header_pager);
            mPanelViewPager.setAdapter(mPanelViewPagerAdapter);
            mPanelViewPagerAdapter.initImage();
            initDot();
            mPanelHeight = (int) getResources().getDimension(R.dimen
                    .ud_sdk_list_view_header_root_layout_height);
        }
        mFooterView = getLayoutInflater()
                .inflate(R.layout.ud_sdk_listview_item_footer_more, mAppListView, false);
        mAppListView.addFooterView(mFooterView);
        mFooterButton = (Button) mFooterView.findViewById(R.id.ud_sdk_list_view_button_more);
        if (DeviceUtils.checkAsusDevice()) {
            mFooterButton.setOnClickListener(ZenFamilyActivity.this);
        } else {
            mFooterButton.setVisibility(View.GONE);
        }
        mAppInfoAdapter = new AppInfoAdapter(this, R.layout.ud_sdk_listview_item, mAppInfoList);
        mAppListView.setAdapter(mAppInfoAdapter);
        mAppListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int offset = 0;
                if (isPortrait()) {
                    offset = 1;
                }
                AppInfo appInfo = mAppInfoList.get(position - offset);
                String packageName = appInfo.getPackageName();
                AppStatus appStatus = appInfo.getStatus();
                if (AppStatus.UP_TO_DATE == appStatus) {
                    boolean success = GeneralUtils.openAsusApp(mActivity, packageName);
                    if (!success) {
                        GeneralUtils.openGooglePlayPage(mActivity, packageName);
                    }
                } else {
                    GeneralUtils.startCheckAppUpdateAlarm(mActivity, packageName);
                    GeneralUtils.openGooglePlayPage(mActivity, packageName);
                }
                TrackerManager.sendEvents(mActivity, TrackerManager.GA_TRACKER,
                        AnalyticUtils.Category.CLICK_LIST_VIEW_APPS, packageName,
                        appStatus.getName(), TrackerManager.DEFAULT_VALUE);
            }
        });
        if (getIntent().getBooleanExtra(KEY_HAS_BUNDLE, false)) {
            drawItemButtonColor(mThemeColor, mUpdateButtonColor, mOpenButtonColor);
            drawThemeColor(mThemeColor);
        }
    }

    private void initDot() {
        mPanelCount = mPanelViewPagerAdapter.getCount();
        if (0 == mPanelCount) return;
        mPanelViewPager.setCurrentItem(0);
        final LinearLayout dotLayout = (LinearLayout) findViewById(
                R.id.ud_sdk_list_view_header_pager_dot);
        if (mPanelCount == dotLayout.getChildCount()) return;
        for (int i = 0; i < mPanelCount; i++) {
            ImageView imageView = new ImageView(mActivity);
            imageView.setImageResource(R.drawable.ud_sdk_asus_zenui_family_indicator_dot);
            imageView.setColorFilter(parseColorCode(mThemeColor), PorterDuff.Mode.SRC_ATOP);
            dotLayout.addView(imageView);
        }
        //First ViewPager Dot
        ImageView imageViewDot = (ImageView) dotLayout.getChildAt(0);
        imageViewDot.setImageResource(R.drawable.ud_sdk_asus_zenui_family_indicator_pager);
        imageViewDot.setColorFilter(parseColorCode(mThemeColor), PorterDuff.Mode.SRC_ATOP);
        mPanelViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int nowPosition) {
                for (int i = 0; i < mPanelCount; i++) {
                    ImageView allDot = (ImageView) dotLayout.getChildAt(i);
                    allDot.setImageResource(R.drawable.ud_sdk_asus_zenui_family_indicator_dot);
                    allDot.setColorFilter(parseColorCode(mThemeColor), PorterDuff.Mode.SRC_ATOP);
                }
                ImageView currentDot = (ImageView) dotLayout.getChildAt(nowPosition);
                currentDot.setImageResource(R.drawable.ud_sdk_asus_zenui_family_indicator_pager);
                currentDot.setColorFilter(parseColorCode(mThemeColor), PorterDuff.Mode.SRC_ATOP);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                switch (arg0) {
                    case VIEW_PAGER_NOT_SCROLLING:
                        mPanelAutoPlayHandler.postDelayed(mPanelAutoPlayRunnable,
                                mPanelAutoPlayMillis);
                        break;
                    case VIEW_PAGER_IS_SCROLLING:
                        mPanelAutoPlayHandler.removeCallbacks(mPanelAutoPlayRunnable);
                        break;
                }
            }
        });
    }

    private void initPanelAutoPlayHandler() {
        mPanelAutoPlayHandler = new Handler();
        mPanelAutoPlayRunnable = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (mPanelViewPagerAdapter == null) {
                            return;
                        }
                        int newIndex = mPanelViewPager.getCurrentItem() + 1;
                        if (newIndex < mPanelCount) {
                            mPanelViewPager.setCurrentItem(newIndex);
                        } else {
                            mPanelViewPager.setCurrentItem(0, false);
                            mPanelAutoPlayHandler.postDelayed(mPanelAutoPlayRunnable,
                                    mPanelAutoPlayMillis);
                        }
                    }
                });
            }
        };
    }

    private void refreshActivity() {
        if (null == mAppInfoAdapter) {
            Log.v(TAG, "mAppInfoAdapter is null");
            initContentLayout();
            setContentVisible();
            return;
        }
        mAppInfoAdapter.notifyDataSetChanged();
        if (isPortrait() && null != mPanelViewPagerAdapter) {
            mPanelViewPagerAdapter.notifyDataSetChanged();
            initDot();
        }

        if(mIsNoNetworkReturn){
            drawItemButtonColor(mThemeColor, mUpdateButtonColor, mOpenButtonColor);
            drawThemeColor(mThemeColor);
            mIsNoNetworkReturn = false;
        }
        setContentVisible();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initCache() {
        Glide.get(this).register(GlideUrl.class, InputStream.class,
                new VolleyUrlLoader.Factory(mActivity));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        int cacheImageSize = (height > width ? height : width) / 2;
        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(MEMORY_CACHE_SIZE);
        mImageFetcher = new ImageFetcher(this, cacheImageSize);
        mImageFetcher.setLoadingImage(R.drawable.ud_sdk_empty_photo);
        mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
        mImageFetcher.setImageFadeIn(true);
    }

    private void showNetworkFailDialog() {
        if (null == mNetworkFailDialog) {
            mNetworkFailDialog = new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.ud_sdk_no_network_connection_title)
                    .setMessage(R.string.ud_sdk_no_network_connection_content)
                    .setNegativeButton(android.R.string.no,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                    .setPositiveButton(R.string.ud_sdk_wifi_settings,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mActivity.startActivity(new Intent(
                                            android.provider.Settings.ACTION_WIFI_SETTINGS));
                                }
                            })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    }).create();
        }
        mNetworkFailDialog.show();
    }

    private void setContentVisible() {
        FrameLayout syncLayout = (FrameLayout) findViewById(R.id.ud_sdk_sync_layout);
        syncLayout.setVisibility(View.GONE);
        RelativeLayout contentLayout = (RelativeLayout) findViewById(R.id.ud_sdk_content_layout);
        contentLayout.setVisibility(View.VISIBLE);
        setFooterLayout();
    }

    private void setContentInvisible() {
        FrameLayout syncLayout = (FrameLayout) findViewById(R.id.ud_sdk_sync_layout);
        syncLayout.setVisibility(View.VISIBLE);
        RelativeLayout contentLayout = (RelativeLayout) findViewById(R.id.ud_sdk_content_layout);
        contentLayout.setVisibility(View.INVISIBLE);
    }

    private void getGtmValuesFromBundle(Bundle bundle) {
        GeneralUtils.setCDNIudVersion(bundle.getLong(KEY_GTM_IUD_VERSION));
        GeneralUtils.setCDNStringsVersion(bundle.getLong(KEY_GTM_STRINGS_VERSION));
        mPanelAutoPlayMillis = bundle.getLong(KEY_GTM_AUTO_PLAY_MILLIS);
        mThemeColor = bundle.getString(KEY_GTM_THEME_COLOR);
        mUpdateButtonColor = bundle.getString(KEY_GTM_UPDATE_BUTTON_COLOR);
        mOpenButtonColor = bundle.getString(KEY_GTM_OPEN_BUTTON_COLOR);
    }

    private void setFooterLayout() {
        int screenHeight = mActivity.getResources().getDisplayMetrics().heightPixels;
        int footerLayoutHeight = (int) getResources().getDimension(R.dimen
                .ud_sdk_list_view_footer_layout_layout_height);
        int contentViewTotalHeight = getContentViewTotalHeight(footerLayoutHeight);
        int footerLayoutOffset = (int) getResources().getDimension(R.dimen
                .ud_sdk_footer_layout_offset);
        RelativeLayout footerLayout = (RelativeLayout) mFooterView.findViewById(R.id
                .ud_sdk_list_view_footer);

        if (!isPortrait()) {
            footerLayoutOffset += (int) getResources().getDimension(R.dimen
                    .ud_sdk_land_list_view_layout_marginTop);
        }

        if (!DeviceUtils.checkAsusDevice()) {
            mAppListView.setFooterDividersEnabled(false);
            footerLayoutHeight = 0;
        }

        if (getAppItemCounts() > 0) {
            setNoAppItemTextShow(false);
        } else {
            setNoAppItemTextShow(true);
        }

        if (screenHeight - contentViewTotalHeight > 0) {
            footerLayoutHeight += (screenHeight - contentViewTotalHeight - footerLayoutOffset);
        }
        footerLayout.getLayoutParams().height = footerLayoutHeight;
    }

    private void setNoAppItemTextShow(boolean isAppItemTextShow) {
        TextView noAppItemTextView = (TextView) mFooterView.findViewById(R.id
                .ud_sdk_list_view_footer_no_app_text);
        if (isAppItemTextShow) {
            noAppItemTextView.setVisibility(View.VISIBLE);
        } else {
            noAppItemTextView.setVisibility(View.GONE);
        }
    }

    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier(STATUS_BAR_RESOURCE_NAME,
                STATUS_BAR_RESOURCE_DEFAULT_TYPE, STATUS_BAR_RESOURCE_DEFAULT_PACKAGE);
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    private int getActionBarHeight() {
        TypedValue typedValue = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data,
                    getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    private int getAppItemCounts() {
        return (mAppListView.getCount() - mAppListView.getHeaderViewsCount()
                - mAppListView.getFooterViewsCount());
    }

    private int getContentViewTotalHeight(int footerLayoutHeight) {
        float appItemHeight = getResources().getDimension(R.dimen
                .ud_sdk_item_image_view_icon_height) + getResources().getDimension(R.dimen
                .ud_sdk_item_linearLayout_layout_marginBottom);

        if (isPortrait()) {
            appItemHeight += getResources().getDimension(R.dimen
                    .ud_sdk_item_slogan_text_view_layout_height);
        } else {
            appItemHeight += getResources().getDimension(R.dimen
                    .ud_sdk_land_slogan_text_view_layout_height);
        }

        int appItemsTotalHeight = (int) appItemHeight * getAppItemCounts();
        return getStatusBarHeight() + getActionBarHeight() + mPanelHeight +
                appItemsTotalHeight + footerLayoutHeight;
    }

}
