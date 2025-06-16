package com.asus.updatesdk.analytic;

import com.asus.updatesdk.analytic.AnalyticUtils.Label;
import com.asus.updatesdk.ZenUiFamily;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Chauncey_Li on 2015/9/21.
 */
public class CheckAppUpdateReceiver extends BroadcastReceiver {
    private static final String TAG = "CheckAppUpdateReceiver";

    public static final String KEY_PACKAGE_NAME = "packagename";
    public static final String KEY_OLD_VERSION = "old_version";
    public static final String KEY_IS_CHECK_INSTALL = "is_check_install";

    @Override
    public void onReceive(Context context, Intent intent) {
        String packagename = intent.getStringExtra(KEY_PACKAGE_NAME);
        long currentVersion = ZenUiFamily.getAppVersion(context, packagename);

        if (intent.getBooleanExtra(KEY_IS_CHECK_INSTALL, false)) {
            String installLabel = (ZenUiFamily.INVALID_VERSION == currentVersion)
                    ? Label.NOT_INSTALL
                    : Label.INSTALL;
            TrackerManager.sendEvents(context, TrackerManager.GA_TRACKER,
                    AnalyticUtils.Category.APP_UPDATED_BY_SDK, packagename,
                    installLabel, TrackerManager.DEFAULT_VALUE);
            Log.v(TAG, packagename + " is installed: " + installLabel);
            return;
        }

        if (ZenUiFamily.INVALID_VERSION == currentVersion) return;
        Boolean isUpdated = !(intent.getLongExtra(KEY_OLD_VERSION, 0) == currentVersion);
        TrackerManager.sendEvents(context, TrackerManager.GA_TRACKER,
                AnalyticUtils.Category.APP_UPDATED_BY_SDK, packagename,
                isUpdated.toString(), TrackerManager.DEFAULT_VALUE);
        Log.v(TAG, packagename + " is update: " + isUpdated.toString());
    }
}
