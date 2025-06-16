package com.asus.updatesdk;

import com.asus.updatesdk.cdn.CdnUtils;
import com.asus.updatesdk.utility.PreferenceUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Chauncey_Li on 2015/6/25.
 */
public class ZenFamilyUpdateReceiver extends BroadcastReceiver {
    private final static String TAG = "ZenFamilyUpdateReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");
        String masterPackage = intent.getStringExtra(CdnUtils.KEY_MASTER_PACKAGE);
        if (!masterPackage.equals(context.getPackageName())) {
            String languageAndRegion = intent.getStringExtra(CdnUtils.KEY_LANGUAGE_LOCALE);
            long iudVersion = intent.getLongExtra(CdnUtils.KEY_IUD_VERSION, 0L);
            long stringsVersion = intent.getLongExtra(CdnUtils.KEY_STRINGS_VERSION, 0L);
            long checkTime = intent.getLongExtra(CdnUtils.KEY_PLAY_CHECK_TIME, 0L);
            String playJson = intent.getStringExtra(CdnUtils.KEY_PLAY_JSON_FILE);
            String iudJson = intent.getStringExtra(CdnUtils.KEY_IUD_JSON_FILE);
            String stringsJson = intent.getStringExtra(CdnUtils.KEY_STRINGS_JSON_FILE);
            PreferenceUtils.putLong(context, PreferenceUtils.KEY_IUD_VERSION, iudVersion);
            PreferenceUtils.putLong(context, PreferenceUtils.KEY_STRINGS_VERSION, stringsVersion);
            PreferenceUtils.putLong(context, PreferenceUtils.KEY_PLAY_CHECK_TIME, checkTime);
            PreferenceUtils.putString(context, PreferenceUtils.KEY_MASTER_PACKAGE, masterPackage);
            PreferenceUtils
                    .putString(context, PreferenceUtils.KEY_LANGUAGE_LOCALE, languageAndRegion);
            PreferenceUtils.putString(context, PreferenceUtils.KEY_PLAY_JSON_FILE, playJson);
            PreferenceUtils.putString(context, PreferenceUtils.KEY_IUD_JSON_FILE, iudJson);
            PreferenceUtils.putString(context, PreferenceUtils.KEY_STRINGS_JSON_FILE, stringsJson);
        }
    }
}
