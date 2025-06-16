package com.asus.updatesdk;

import com.asus.updatesdk.activity.ZenFamilyActivity;
import com.asus.updatesdk.analytic.TrackerManager;
import com.asus.updatesdk.cdn.CdnUtils;
import com.asus.updatesdk.utility.DeviceUtils;
import com.asus.updatesdk.utility.GeneralUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by Chauncey_Li on 2015/6/25.
 */
public class ZenUiFamily {
    private final static String TAG = "ZenUiFamily";
    private  static final int INVALID_DATE = 0;
    public final static long INVALID_VERSION = -1L;
    public final static long STAGE_ROLLOUT_VERSION = -2L;
    private final static String NAME_IMPORTANT = "IMPORTANT";
    private final static String NAME_NOT_INSTALLED = "NOT_INSTALLED";
    private final static String NAME_NEED_UPDATE = "NEED_UPDATE";
    private final static String NAME_UP_TO_DATE = "UP_TO_DATE";
    private final static String NAME_NOT_SUPPORTED = "NOT_SUPPORTED";
    private final static String KEY_API_LEVEL = "api_level";
    private final static String KEY_STATUS_TRUE = "true";
    private final static String KEY_FEATURES = "features";
    private final static String KEY_PLATFORMS = "platforms";
    private final static String ENTRY_DIALOG = "entry dialog";

    public enum AppStatus {
        IMPORTANT(0), NOT_INSTALLED(1), NEED_UPDATE(2), UP_TO_DATE(3), NOT_SUPPORTED(4);

        int mIndex;
        AppStatus(int index) {
            mIndex = index;
        }

        public int getIndex() {
            return mIndex;
        }
        public static AppStatus getFromIndex(int index){
            switch (index) {
                case 0:
                    return IMPORTANT;
                case 1:
                    return NOT_INSTALLED;
                case 2:
                    return NEED_UPDATE;
                case 3:
                    return UP_TO_DATE;
                default:
                    return NOT_SUPPORTED;
            }
        }
        public String getName() {
            switch (mIndex) {
                case 0:
                    return NAME_IMPORTANT;
                case 1:
                    return NAME_NOT_INSTALLED;
                case 2:
                    return NAME_NEED_UPDATE;
                case 3:
                    return NAME_UP_TO_DATE;
                default:
                    return NAME_NOT_SUPPORTED;
            }
        }
    }

    public static void launchZenUiFamily(Context context) {
        Intent intent = new Intent(context, ZenFamilyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void launchZenUiFamily(Context context, int flags) {
        Intent intent = new Intent(context, ZenFamilyActivity.class);
        intent.addFlags(flags);
        context.startActivity(intent);
    }

    public static int getZenUiFamilyTitle() {
        return DeviceUtils.checkAsusDevice() ? R.string.ud_sdk_update_sdk_asus
                                             : R.string.ud_sdk_update_sdk;
    }

    public static void setGAEnable(boolean enable){
        TrackerManager.setEnableStatus(enable);
    }

    public static AppStatus getAppUpdateState(Context context, String packageName,
            long versionCode, int urgentDate) {
        if (INVALID_VERSION == versionCode) return AppStatus.NOT_SUPPORTED;
        boolean isUrgent = false;
        int appDate;
        try {
            appDate = Integer.parseInt(ZenUiFamily.getAppDate(context, packageName));
        } catch (NumberFormatException e) {
            appDate = INVALID_DATE;
        }
        if (urgentDate != 0 && urgentDate > appDate) {
            isUrgent = true;
            Log.v(TAG, packageName + " is urgent. App date is " + String.valueOf(appDate)
                    + "; Urgent date is " + String.valueOf(urgentDate));
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            if (packageManager.getPackageInfo(packageName, 0).versionCode < versionCode) {
                return isUrgent ? AppStatus.IMPORTANT : AppStatus.NEED_UPDATE;
            }
            return AppStatus.UP_TO_DATE;
        } catch (PackageManager.NameNotFoundException e) {
            return AppStatus.NOT_INSTALLED;
        }
    }

    public static boolean checkBlackList(String blacklist){
        if(null == blacklist) return false;
        String[] array = blacklist.split("[ ,]");
        return Arrays.asList(array).contains(DeviceUtils.SYSPROP_PRODUCT_DEVICE);
    }

    public static long getAppVersion(Context context, String packageName){
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getPackageInfo(packageName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return INVALID_VERSION;
        }
    }

    private static String getAppDate(Context context, String packageName){
        PackageManager packageManager = context.getPackageManager();
        try {
            String versionName = packageManager.getPackageInfo(packageName, 0).versionName;
            String[] array = versionName.split(" |_|\\.");
            for (String arrays :array){
                if (arrays.length() == 6) return arrays;
            }
            return "0";
        } catch (PackageManager.NameNotFoundException e) {
            return "0";
        }
    }

    public static long getLatestVersionCode(Context context, JSONArray apks) {
        try {
            for (int i = 0; i < apks.length(); i++) {
                JSONObject apk = apks.getJSONObject(i);
                if (isApkSupported(context, apk)) {
                    if (isStageRollout(context, apk)) return STAGE_ROLLOUT_VERSION;
                    return Long.valueOf(apk.getString(CdnUtils.NODE_VERSION));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return INVALID_VERSION;
    }

    private static boolean isApkSupported(Context context, JSONObject apk) {
        try {
            if (GeneralUtils.getDeviceApiLevel() < Integer.valueOf(apk.getString(KEY_API_LEVEL))
                    || !isCPUSupported(apk.optString(KEY_PLATFORMS))) {
                return false;
            }
            String[] features = apk.getString(KEY_FEATURES).split(",");
            PackageManager pm = context.getPackageManager();
            for (String feature : features) {
                if(!pm.hasSystemFeature(feature.toLowerCase())) return false;
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isStageRollout(Context context, JSONObject apk){
        try {
            return apk.getString(CdnUtils.NODE_STATUS).equals(KEY_STATUS_TRUE);
        } catch (JSONException e) {
            return true;
        }
    }
    private static boolean isCPUSupported(String platforms) {
        if ("".equals(platforms)) return true;
        String abiList = DeviceUtils.SYSPROP_CPU_ABILIST.toLowerCase();
        String CPU_ABI = DeviceUtils.SYSPROP_CPU_ABI.toLowerCase();
        String CPU_ABI2 = DeviceUtils.SYSPROP_CPU_ABI2.toLowerCase();
        String[] platformsArray = platforms.toLowerCase().split("[ ,]");
        for (String platform : platformsArray) {
            if (abiList.contains(platform) || CPU_ABI.startsWith(platform)
                    || CPU_ABI2.startsWith(platform)) {
                return true;
            }
        }
        return false;
    }
    /**
     * This function must run in background thread
     * @param context
     * @param packageName the package name of the app
     * @return version code of the app, INVALID_VERSION(-1L) if the app is not supported
     */
    public static long getApkLatestVersion(Context context, String packageName) {
        context = context.getApplicationContext();
        JSONObject jsonObject;
        if (GeneralUtils.hasInternetPermisson(context)) {
            GeneralUtils.getGtmValues(context);
            jsonObject = CdnUtils.getJson(context);
        } else {
            jsonObject = CdnUtils.getJsonNoNetwork(context);
        }
        if (null == jsonObject) return INVALID_VERSION;
            try {
                JSONArray appsJsonArray = jsonObject.getJSONArray(CdnUtils.NODE_APPS_ARRAY);
                for (int i = 0; i < appsJsonArray.length(); i++) {
                    JSONObject jsonAppInfo = appsJsonArray.getJSONObject(i);
                    String nodePackage = jsonAppInfo.getString(CdnUtils.NODE_PACKAGE);
                    if (!nodePackage.equals(packageName)) continue;
                    if (checkBlackList(jsonAppInfo.getString(CdnUtils.NODE_BLACKLIST))) {
                        return INVALID_VERSION;
                    }
                    return getLatestVersionCode(context,
                            jsonAppInfo.getJSONArray(CdnUtils.NODE_APK));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return INVALID_VERSION;
    }

}
