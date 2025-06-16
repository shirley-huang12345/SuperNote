package com.uservoice.uservoicesdk.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;

import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.UserVoice;
import com.uservoice.uservoicesdk.model.BaseModel;
import com.uservoice.uservoicesdk.rest.Callback;
import com.uservoice.uservoicesdk.rest.RestResult;

public abstract class DefaultCallback<T> extends Callback<T> {

    private static final String TAG = "com.uservoice.uservoicesdk";

    private final Context context;

    public DefaultCallback(Context context) {
        this.context = context;
    }

    @Override
    public void onError(RestResult error) {
        Log.e(TAG, error.getMessage());
        try {
            BaseModel.cancelTask();
            int wifiId = Utils.isCNSku()? R.string.uf_sdk_wlan_settings: R.string.uf_sdk_wifi_settings;
            new AlertDialog.Builder(context).setTitle(R.string.uf_sdk_connection_error_title)
                .setMessage(R.string.uf_sdk_connection_error_msg)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(wifiId, new OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    UserVoice.sNeedReload = true;
                    context.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                }
            }).show();


        } catch (Exception e) {
            // This can happen if the activity is already gone
            Log.e(TAG, "Failed trying to show alert: " + e.getMessage());
        }
    }

}
