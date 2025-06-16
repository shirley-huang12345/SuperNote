package com.asus.supernote.ratingus;

import com.asus.supernote.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import java.util.List;

public class RatingUsUtil {

    private static final Uri SUPERNOTE_MARKET_URI = Uri
            .parse("market://details?id=com.asus.supernote");
    private static final Uri SUPERNOTE_GOOGLE_PLAY_URI = Uri
            .parse("http://play.google.com/store/apps/details?id=com.asus.supernote");
    //private static final long RATE_US_TOAST_ANIMATION_DELAY_TIME_IN_MILLIS = 1700L;
    private static final String PKG_PLAY_STORE = "com.android.vending";
    private static final String ACCOUNT_TYPE_GOOGLE = "com.google";

    public static void rateUs(Context context, boolean showAnimation) {
        Intent intent = new Intent(Intent.ACTION_VIEW, SUPERNOTE_MARKET_URI);
        intent.setPackage(PKG_PLAY_STORE);
        List<ResolveInfo> activities = context.getPackageManager().queryIntentActivities(intent, 0);
        // No matching activities, an empty list is returned.
        if (activities.size() <= 0) {
            // set URI opened by browser.
            intent.setPackage(null);
            intent.setData(SUPERNOTE_GOOGLE_PLAY_URI);
            // check can be handled by browser
            if (context.getPackageManager().queryIntentActivities(intent, 0).size() <= 0) {
                Toast.makeText(context, R.string.later_action_no_apps, Toast.LENGTH_SHORT).show();
                return;
            }
        }/* else if (showAnimation) {
            new Handler().postDelayed(new RateUsToastAnimation(context),
                    RATE_US_TOAST_ANIMATION_DELAY_TIME_IN_MILLIS);
        }*/
        context.startActivity(intent);
    }

    private static boolean hasLoginGoogleAccount(Context context) {
        return getAccountManagerGoogleAccounts(context).length > 0;
    }

    public static Account[] getAccountManagerGoogleAccounts(Context context) {
        return AccountManager.get(context).getAccountsByType(ACCOUNT_TYPE_GOOGLE);
    }

}
