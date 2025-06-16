package com.asus.updatesdk;

import com.asus.updatesdk.cdn.CdnUtils;
import com.asus.updatesdk.utility.PreferenceUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Chauncey_Li on 2015/8/12.
 */
public class ZenFamilyShareReceiver extends BroadcastReceiver {
    private final static String TAG = "ZenFamilyShareReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = context.getPackageName();
        Log.v(TAG, "onReceive in " + packageName);
        switch (intent.getAction()) {
            //First query to Master
            case CdnUtils.ACTION_REQUEST_MASTER:
                String masterPackage = PreferenceUtils
                        .getString(context, PreferenceUtils.KEY_MASTER_PACKAGE, null);
                if (null != masterPackage && masterPackage.equals(packageName)) {
                    Log.v(TAG, "Send data from " + packageName);
                    Intent sendMasterIntent = new Intent(CdnUtils.ACTION_SEND_MASTER_DATA);
                    sendMasterIntent.putExtra(CdnUtils.KEY_PASSED_IUD_VERSION, PreferenceUtils
                            .getLong(context, PreferenceUtils.KEY_IUD_VERSION, 0L));
                    sendMasterIntent
                            .putExtra(CdnUtils.KEY_PASSED_STRINGS_JSON_FILE, PreferenceUtils.getString(
                                    context, PreferenceUtils.KEY_STRINGS_JSON_FILE, null));
                    sendMasterIntent
                            .putExtra(CdnUtils.KEY_PASSED_IUD_JSON_FILE, PreferenceUtils.getString(
                                    context, PreferenceUtils.KEY_IUD_JSON_FILE, null));
                    sendMasterIntent
                            .putExtra(CdnUtils.KEY_PASSED_PLAY_JSON_FILE, PreferenceUtils.getString(
                                    context, PreferenceUtils.KEY_PLAY_JSON_FILE, null));
                    sendMasterIntent.putExtra(CdnUtils.KEY_PASSED_STRINGS_VERSION, PreferenceUtils
                            .getLong(context, PreferenceUtils.KEY_STRINGS_VERSION, 0L));
                    sendMasterIntent.putExtra(CdnUtils.KEY_PASSED_MASTER, PreferenceUtils
                            .getString(context, PreferenceUtils.KEY_MASTER_PACKAGE,
                                    masterPackage));
                    sendMasterIntent.putExtra(CdnUtils.KEY_PASSED_LANGUAGE, PreferenceUtils
                            .getString(context, PreferenceUtils.KEY_LANGUAGE_LOCALE,
                                    CdnUtils.DEFAULT_LANGUAGE));
                    sendMasterIntent.putExtra(CdnUtils.KEY_PASSED_PLAY_CHECK_TIME,
                            PreferenceUtils.getLong(
                                    context, PreferenceUtils.KEY_PLAY_CHECK_TIME, 0L));
                    sendMasterIntent.setPackage(intent.getStringExtra(CdnUtils.KEY_SLAVE_PACKAGE));
                    context.sendBroadcast(sendMasterIntent);
                }
                break;
        }
    }
}
