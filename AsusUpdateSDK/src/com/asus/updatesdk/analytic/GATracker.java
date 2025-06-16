
package com.asus.updatesdk.analytic;

import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.asus.updatesdk.analytic.TrackerManager.TrackerName;
import com.asus.updatesdk.tagmanager.ContainerHolderSingleton;
import com.asus.updatesdk.utility.DeviceUtils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import java.util.Locale;

public class GATracker extends AnalyticTracker {
    public static final String TRACKER_ID_ASUS_DEVICE = "UA-65515524-1";//test:UA-65517850-1
    public static final String TRACKER_ID_NON_ASUS_DEVICE = "UA-65515524-2";//test:UA-65517850-2

    private static final String CUSTOM_DIMENTION_PARAM = "&cd";
    private static final String DEFAULT_VERSION = "none";

    private static final String[] CUSTOM_DIMENSIONS = {
            Build.MODEL, DeviceUtils.SYSPROP_ASUS_SKU, DeviceUtils.SYSPROP_ASUS_VERSION,
            DeviceUtils.SYSPROP_BUILD_PRODUCT, Build.TYPE, Build.DEVICE, Build.PRODUCT
    };
    private static final String GTM_CONTAINER_VERSION = "Container Version";

    private final Tracker mGaTracker;
    private final TrackerName mTrackerName;

    public GATracker(Context context, TrackerName trackerName) {
        mTrackerName = trackerName;
        mGaTracker = GoogleAnalytics.getInstance(context).newTracker(mTrackerName.getTrackerID());
        initGaTracker(context);
    }

    private void initGaTracker(Context context) {
        for (int i = 0; i < CUSTOM_DIMENSIONS.length; i++) {
            //key reference: http://goo.gl/M6dK2U
            mGaTracker.set(String.format(Locale.US, "%s%d", CUSTOM_DIMENTION_PARAM, i + 1),
                    CUSTOM_DIMENSIONS[i]);
        }
        mGaTracker.setSampleRate(mTrackerName.getSampleRate());
        if (!mTrackerName.isAutoTrack()) return;
        mGaTracker.enableAutoActivityTracking(true);
        mGaTracker.enableAdvertisingIdCollection(true);
        Thread.UncaughtExceptionHandler exceptionHandler = new ExceptionReporter(mGaTracker,
                Thread.getDefaultUncaughtExceptionHandler(), context);
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
    }

    @Override
    public void activityStart(Activity activity) {
        GoogleAnalytics.getInstance(activity).reportActivityStart(activity);
    }

    @Override
    public void activityStop(Activity activity) {
        GoogleAnalytics.getInstance(activity).reportActivityStop(activity);
    }

    @Override
    public void sendEvents(Context context, String category, String action, String label,
            Long value) {
        String gtmVersion = DEFAULT_VERSION;
        if (ContainerHolderSingleton.getContainerHolder() != null) {
            gtmVersion = ContainerHolderSingleton.getContainerHolder().getContainer()
                    .getString(GTM_CONTAINER_VERSION);
        }
        mGaTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .setCustomDimension(8, gtmVersion) //custom dimension of Container Version
                .build());
    }

    @Override
    public void sendTiming(Context context, String category, long intervalInMillis, String name,
            String label) {
        mGaTracker.send(new HitBuilders.TimingBuilder()
                .setCategory(category)
                .setLabel(label)
                .setValue(intervalInMillis)
                .setVariable(name)
                .build());
    }

    @Override
    public void sendException(Context context, String description, Throwable ex, boolean fatal) {
        mGaTracker.send(new HitBuilders.ExceptionBuilder()
                .setDescription(new AnalyticsExceptionParser().getDescription(description, ex))
                .setFatal(fatal)
                .build());
    }

    @Override
    public void sendView(Context context, String viewName) {
        mGaTracker.setScreenName(viewName);
        mGaTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
