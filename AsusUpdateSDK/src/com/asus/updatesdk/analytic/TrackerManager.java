package com.asus.updatesdk.analytic;

import com.asus.updatesdk.AppInfo;
import com.asus.updatesdk.analytic.AnalyticUtils.Action;
import com.asus.updatesdk.analytic.AnalyticUtils.Category;
import com.asus.updatesdk.analytic.AnalyticUtils.Label;
import com.asus.updatesdk.utility.DeviceUtils;
import com.asus.updatesdk.utility.PreferenceUtils;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class TrackerManager {

    private static final String TAG = "TrackerManager";

    public static final TrackerName GA_TRACKER = DeviceUtils.checkAsusDevice()
            ? TrackerName.TRACKER_ASUS_DEVICE
            : TrackerName.TRACKER_NON_ASUS_DEVICE;

    public enum TrackerName {
        TRACKER_ASUS_DEVICE(true, 0.1D, GATracker.TRACKER_ID_ASUS_DEVICE),
        TRACKER_NON_ASUS_DEVICE(true, 0.1D, GATracker.TRACKER_ID_NON_ASUS_DEVICE);

        private boolean mAutoTrack;
        private double mSampleRate;
        private String mId;
        TrackerName(boolean isAutoTrack, double sampleRate, String id) {
            mAutoTrack = isAutoTrack;
            mSampleRate = sampleRate;
            mId = id;
        }

        public boolean isAutoTrack() {
            return mAutoTrack;
        }

        public double getSampleRate() {
            return mSampleRate;
        }

        public String getTrackerID() {
            return mId;
        }

        public void setSampleRate(double sampleRate) {
            mSampleRate = sampleRate;
        }

        public void setTrackerId(String id) {
            mId = id;
        }
    }

    //we can force enable GA by set DEBUG_FORCE_ENABLE_TRACKERS = true
    private static final boolean DEBUG_FORCE_ENABLE_TRACKERS = false;

    public static final long DEFAULT_VALUE = 0L;

    private static TrackerManager sInstance;

    private HashMap<TrackerName, AnalyticTracker> mTrackers = new HashMap<>();

    private boolean mEnableTracker = false;

    private TrackerManager() {
    }

    private AnalyticTracker getTracker(Context context, TrackerName trackerName) {
        AnalyticTracker tracker = mTrackers.get(trackerName);
        if (null == tracker) {
            tracker = new GATracker(context, trackerName);
            mTrackers.put(trackerName, tracker);
        }
        return tracker;
    }

    public static void setEnableStatus(boolean enable) {
        TrackerManager tm = getInstance();
        tm.mEnableTracker = DEBUG_FORCE_ENABLE_TRACKERS || enable;
    }

    private boolean getEnableStatus() {
        return mEnableTracker;
    }

    private static TrackerManager getInstance() {
        if (sInstance == null) {
            sInstance = new TrackerManager();
        }
        return sInstance;
    }

    public static void putBoolItem(Context context, String key, boolean bool) {
        TrackerManager tm = getInstance();
        if (tm.getEnableStatus()) {
            PreferenceUtils.putBoolean(context, key, bool);
        }
    }

    public static void putIntItem(Context context, String key, int value) {
        TrackerManager tm = getInstance();
        if (tm.getEnableStatus()) {
            PreferenceUtils.putInt(context, key, value);
        }
    }

    public static int getIntItem(Context context, String key) {
        TrackerManager tm = getInstance();
        if (!tm.getEnableStatus()) return 0;
        // get 0 while item not exist
        return PreferenceUtils.getInt(context, key, 0);
    }

    public static boolean getBoolItem(Context context, String key) {
        TrackerManager tm = getInstance();
        if (!tm.getEnableStatus()) return false;
        // get false while item not exist
        return PreferenceUtils.getBoolean(context, key, false);
    }

    public static void activityStart(Activity activity, TrackerName trackerName) {
        TrackerManager tm = getInstance();
        if (tm.getEnableStatus()) {
            tm.getTracker(activity, trackerName).activityStart(activity);
        }
    }

    public static void activityStop(Activity activity, TrackerName trackerName) {
        TrackerManager tm = getInstance();
        if (tm.getEnableStatus()) {
            tm.getTracker(activity, trackerName).activityStop(activity);
        }
    }

    public static void sendEvents(Context context, TrackerName trackerName, String category,
            String action, String label, Long value) {
        TrackerManager tm = getInstance();
        if (tm.getEnableStatus()) {
            tm.getTracker(context, trackerName).sendEvents(
                    context,
                    category,
                    action,
                    label,
                    value);
        }
    }

    public static void sendTiming(Context context, TrackerName trackerName, String category,
            long intervalInMillis, String name, String label) {
        TrackerManager tm = getInstance();
        if (tm.getEnableStatus()) {
            tm.getTracker(context, trackerName).sendTiming(
                    context,
                    category,
                    intervalInMillis,
                    name,
                    label);
        }
    }

    public static void sendException(Context context, TrackerName trackerName, String description,
            Throwable ex, boolean fatal) {
        TrackerManager tm = getInstance();
        if (tm.getEnableStatus()) {
            tm.getTracker(context, trackerName).sendException(context, description, ex, fatal);
        }
    }

    public static void sendView(Context context, TrackerName trackerName, String viewName) {
        TrackerManager tm = getInstance();
        if (tm.getEnableStatus()) {
            tm.getTracker(context, trackerName).sendView(context, viewName);
        }
    }

    public static void sendDailyReport(Context context, ArrayList<AppInfo> appInfoList) {
        int day = getIntItem(context, PreferenceUtils.KEY_REPORT_DAY);
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        if (day != currentDay) {
            TrackerManager.sendEvents(context, GA_TRACKER, Category.START_ACTIVITY_REPORT,
                    context.getPackageName(), Label.NO_LABEL, TrackerManager.DEFAULT_VALUE);
            TrackerManager.sendEvents(context, GA_TRACKER, Category.DEVICE_PROPERTIES,
                    Action.DEVICE_CPU, String.format("%s, %s, %s", DeviceUtils.SYSPROP_CPU_ABILIST,
                    DeviceUtils.SYSPROP_CPU_ABI, DeviceUtils.SYSPROP_CPU_ABI2),
                    TrackerManager.DEFAULT_VALUE);
            putIntItem(context, PreferenceUtils.KEY_REPORT_DAY, currentDay);
            if (null == appInfoList) return;
            for (AppInfo appInfo : appInfoList) {
                sendEvents(context, GA_TRACKER, Category.UPDATE_LIST,
                        appInfo.getPackageName(), appInfo.getStatus().getName(), DEFAULT_VALUE);
            }
        }
    }
}
