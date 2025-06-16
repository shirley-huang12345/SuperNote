package com.asus.updatesdk.utility;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.tagmanager.Container;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.TagManager;

import com.asus.updatesdk.R;
import com.asus.updatesdk.ZenUiFamily;
import com.asus.updatesdk.activity.ZenFamilyActivity;
import com.asus.updatesdk.analytic.CheckAppUpdateReceiver;
import com.asus.updatesdk.analytic.TrackerManager;
import com.asus.updatesdk.tagmanager.ContainerHolderSingleton;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by Chauncey_Li on 2015/7/9.
 */
public class GeneralUtils {
    private static final String TAG = "GeneralUtils";
    private static final String ZENUI_FAMILY_SEARCH_URI = "market://search?q=pub:\"ZenUI," +
            "+ASUS+Computer+Inc.\"";
    private static final Uri ZENUI_FAMILY_URI = Uri.parse("https://play.google.com/store/apps/" +
            "dev?id=6704106470901776285");
    private static final String GOOGLE_PLAY_PACKAGE_NAME = "com.android.vending";
    private static final int GOOGLE_PLAY_INTENT_JUDGE_BAES_VERSION = 80371000;
    private static final int CHECK_UPDATE_TIME = 10;

    private static final String PLAY_URL = "https://play.google.com/store/apps/details?id=";
    private static final String MARKET_URL = "market://details?id=";

    private static final String PACKAGE_NAME_LAUNCHER = "com.asus.launcher";

    private static final String CONTAINER_ID = "GTM-NQFFFJ";
    private static final String GTM_THEME_COLOR = "theme_color";
    private static final String GTM_UPDATE_BUTTON_COLOR = "update_button_color";
    private static final String GTM_OPEN_BUTTON_COLOR = "open_button_color";
    private static final String GTM_IUD_VERSION = "iud_version";
    private static final String GTM_STRINGS_VERSION = "strings_version";
    private static final String GTM_SAMPLE_RATE = "ga_sample_rate";
    private static final String GTM_ASUS_TRACKER_ID = "asus_tracker_id";
    private static final String GTM_NON_ASUS_TRACKER_ID = "non_asus_tracker_id";
    private static final String GTM_AUTO_PLAY_MILLIS = "auto_play_millis";
    private static final long TIMEOUT_FOR_CONTAINER_OPEN_MILLISECONDS = 1000;

    private static long mCDNIudVersion = 0L;
    private static long mCDNStringsVersion = 0L;

    public static void setCDNIudVersion(long CDNIudVersion) {
        mCDNIudVersion = CDNIudVersion;
    }

    public static void setCDNStringsVersion(long CDNStringsVersion) {
        mCDNStringsVersion = CDNStringsVersion;
    }

    public static long getCDNIudVersion() {
        return mCDNIudVersion;
    }

    public static long getCDNStringsVersion() {
        return mCDNStringsVersion;
    }

    public static int getDeviceApiLevel() {
        return Build.VERSION.SDK_INT;
    }

    public static Bitmap getAppIcon(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            return ((BitmapDrawable)pm.getApplicationIcon(packageName)).getBitmap();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static void openGooglePlayPage(Context context, String packagename) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URL + packagename));
        List<ResolveInfo> activities = context.getPackageManager().queryIntentActivities(intent, 0);
        if (activities.size() <= 0) {
            intent.setData(Uri.parse(PLAY_URL + packagename));
        }
        context.startActivity(intent);
    }

    public static boolean openAsusApp(Context context, String packagename) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packagename);
        if (null == intent || packagename.equals(PACKAGE_NAME_LAUNCHER)) return false;
        context.startActivity(intent);
        return true;
    }

    public static void openZenFamilyPage(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            int version = packageManager.getPackageInfo(GOOGLE_PLAY_PACKAGE_NAME, 0)
                    .versionCode;
            if (version >= GOOGLE_PLAY_INTENT_JUDGE_BAES_VERSION) {
                intent.setPackage(GOOGLE_PLAY_PACKAGE_NAME);
                intent.setData(ZENUI_FAMILY_URI);
            } else {
                intent.setData(Uri.parse(ZENUI_FAMILY_SEARCH_URI));
            }
            List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
            if (activities.size() <= 0) {
                intent.setPackage(null);
                intent.setData(ZENUI_FAMILY_URI);
            }
            context.startActivity(intent);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            intent.setData(ZENUI_FAMILY_URI);
            context.startActivity(intent);
        }
    }

    public static void getGtmValues(Context context) {
        ContainerHolder containerHolder = ContainerHolderSingleton.getContainerHolder();
        if (null == containerHolder) {
            TagManager tagManager = TagManager.getInstance(context);
            PendingResult<ContainerHolder> pending = tagManager
                    .loadContainerPreferNonDefault(CONTAINER_ID,
                            R.raw.ud_sdk_container_binary);
            containerHolder = pending.
                    await(TIMEOUT_FOR_CONTAINER_OPEN_MILLISECONDS, TimeUnit.MILLISECONDS);

            if (!containerHolder.getStatus().isSuccess()) {
                Log.e(TAG, "failure loading container");
                return;
            }
            ContainerHolderSingleton.setContainerHolder(containerHolder);
            ContainerHolderSingleton.getContainerHolder().refresh();
        }
        Container container = containerHolder.getContainer();
        try {
            if (container == null) {
                Log.e(TAG, "failure getting container");
                return;
            }
            Log.v(TAG, "get Data version : " + container.getString(GTM_IUD_VERSION));
            if (context instanceof ZenFamilyActivity) {
                ((ZenFamilyActivity) context).setThemeColor(
                        container.getString(GTM_THEME_COLOR),
                        container.getString(GTM_UPDATE_BUTTON_COLOR),
                        container.getString(GTM_OPEN_BUTTON_COLOR));
                ((ZenFamilyActivity) context).setPanelAutoPlayMillis(Long.parseLong(container
                        .getString(GTM_AUTO_PLAY_MILLIS)));
            }
            mCDNIudVersion = Long.parseLong(container.getString(GTM_IUD_VERSION));
            mCDNStringsVersion = Long.parseLong(container.getString(GTM_STRINGS_VERSION));
            TrackerManager.GA_TRACKER.setSampleRate(
                    Double.parseDouble(container.getString(GTM_SAMPLE_RATE)));
            String trackerId;
            switch (TrackerManager.GA_TRACKER){
                case TRACKER_ASUS_DEVICE:
                    trackerId = container.getString(GTM_ASUS_TRACKER_ID);
                    break;
                case TRACKER_NON_ASUS_DEVICE:
                    trackerId = container.getString(GTM_NON_ASUS_TRACKER_ID);
                    break;
                default:
                    trackerId = null;
                    break;
            }
            if (null != trackerId && !trackerId.isEmpty()) {
                TrackerManager.GA_TRACKER.setTrackerId(trackerId);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException, use default polling time");
        }
    }

    public static void startCheckAppUpdateAlarm(Context context, String packageName) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, CHECK_UPDATE_TIME);
        long version = ZenUiFamily.getAppVersion(context, packageName);
        boolean isCheckInstall = ZenUiFamily.INVALID_VERSION == version;

        Intent intent = new Intent(context, CheckAppUpdateReceiver.class);
        intent.putExtra(CheckAppUpdateReceiver.KEY_PACKAGE_NAME, packageName);
        intent.putExtra(CheckAppUpdateReceiver.KEY_OLD_VERSION, version);
        intent.putExtra(CheckAppUpdateReceiver.KEY_IS_CHECK_INSTALL, isCheckInstall);

        PendingIntent pi = PendingIntent.getBroadcast(context,
                (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
    }

    public static boolean hasInternetPermisson(Context context) {
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        return (pm.checkPermission(Manifest.permission.INTERNET, packageName)
                == PackageManager.PERMISSION_GRANTED) &&
                (pm.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, packageName)
                        == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean isActivityDestroyed(Activity activity) {
        return Build.VERSION.SDK_INT >= 17 && activity.isDestroyed();
    }
}
