package com.asus.updatesdk.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * PreferenceUtils records some functions and variables related to shared preference.
 *
 * Naming convention:
 * 1. File name - PREFERENCE_UPPERCASE : lowercase_lowercase
 * public static final String PREFERENCE_SHAKE = "last_shake_preference";
 * 2. Key name - KEY_UPPERCASE_UPPERCASE: lowercase_lowercase
 * public static final String KEY_DEFAULT_FOLDER_ID = "default_folder_id";
 *
 * @author meichang
 */

public class PreferenceUtils {
    // CDN
    public static final String KEY_MASTER_PACKAGE = "ud_sdk_master_package";
    public static final String KEY_PLAY_CHECK_TIME = "ud_sdk_play_check_time";
    public static final String KEY_IUD_JSON_FILE = "ud_sdk_iud_json_file";
    public static final String KEY_STRINGS_JSON_FILE = "ud_sdk_strings_json_file";
    public static final String KEY_PLAY_JSON_FILE = "ud_sdk_play_json_file";
    public static final String KEY_LANGUAGE_LOCALE = "ud_sdk_language_locale";
    public static final String KEY_IUD_VERSION = "ud_sdk_iud_version";
    public static final String KEY_STRINGS_VERSION = "ud_sdk_strings_version";
    public static final String KEY_REPORT_DAY = "ud_sdk_report_day";
    public static final String KEY_MERGE_JSON_SUCCESS = "ud_sdk_merge_json_success";

    private static SharedPreferences sSharedPreference;

    /**
     * Gets a SharedPreferences instance that points to the default file that is
     * used by the preference framework in the given context.
     */
    private static SharedPreferences getSharedPreferences(Context context) {
        if (sSharedPreference == null) {
            sSharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sSharedPreference;
    }

    /**
     * Retrieve and hold the contents of the preferences filename, returning
     * a SharedPreferences.
     */
    private static SharedPreferences getSharedPreferences(Context context, String filename) {
        if (TextUtils.isEmpty(filename)) {
            return getSharedPreferences(context);
        } else {
            return context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        }
    }

    /**
     * Get a default preferences editor.
     */
    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    /**
     * Get a preferences editor from filename.
     */
    private static SharedPreferences.Editor getEditor(Context context, String filename) {
        return getSharedPreferences(context, filename).edit();
    }

    /**
     * Returns a boolean indicating whether the key is existed in default preference.
     */
    public static boolean contains(Context context, String key) {
        return getSharedPreferences(context).contains(key);
    }

    /**
     * Returns a boolean indicating whether the key is existed in preference filename.
     */
    public static boolean contains(Context context, String key, String filename) {
        return getSharedPreferences(context, filename).contains(key);
    }

    /**
     * Remove a preference in default preference.
     */
    public static void remove(Context context, String key) {
        getEditor(context).remove(key).apply();
    }

    /**
     * Remove a preference in preference filename.
     */
    public static void remove(Context context, String key, String filename) {
        getEditor(context, filename).remove(key).apply();
    }

    /**
     * Retrieve a String value from the default preferences.
     */
    public static String getString(Context context, String key, String defValue) {
        return getSharedPreferences(context).getString(key, defValue);
    }

    /**
     * Retrieve a String value from the preferences filename.
     */
    public static String getString(Context context, String key, String defValue, String filename) {
        return getSharedPreferences(context, filename).getString(key, defValue);
    }

    /**
     * Set a String value in the default preferences.
     */
    public static void putString(Context context, String key, String value) {
        getEditor(context).putString(key, value).apply();
    }

    /**
     * Set a String value in the preferences filename.
     */
    public static void putString(Context context, String key, String value, String filename) {
        getEditor(context, filename).putString(key, value).apply();
    }

    /**
     * Retrieve an int value from the default preferences.
     */
    public static int getInt(Context context, String key, int defValue) {
        return getSharedPreferences(context).getInt(key, defValue);
    }

    /**
     * Retrieve an int value from the preferences filename.
     */
    public static int getInt(Context context, String key, int defValue, String filename) {
        return getSharedPreferences(context, filename).getInt(key, defValue);
    }

    /**
     * Set an int value in the default preferences.
     */
    public static void putInt(Context context, String key, int value) {
        getEditor(context).putInt(key, value).apply();
    }

    /**
     * Set an int value in the preferences filename.
     */
    public static void putInt(Context context, String key, int value, String filename) {
        getEditor(context, filename).putInt(key, value).apply();
    }

    /**
     * Retrieve a boolean value from the default preferences.
     */
    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return getSharedPreferences(context).getBoolean(key, defValue);
    }

    /**
     * Retrieve a boolean value from the preferences filename.
     */
    public static boolean getBoolean(Context context, String key, boolean defValue, String filename) {
        return getSharedPreferences(context, filename).getBoolean(key, defValue);
    }

    /**
     * Set a boolean value in the default preferences
     */
    public static void putBoolean(Context context, String key, boolean value) {
        getEditor(context).putBoolean(key, value).apply();
    }

    /**
     * Set a boolean value in the preferences filename;
     */
    public static void putBoolean(Context context, String key, boolean value, String filename) {
        getEditor(context, filename).putBoolean(key, value).apply();
    }

    /**
     * Retrieve a long value from the default preferences.
     */
    public static long getLong(Context context, String key, long defValue) {
        return getSharedPreferences(context).getLong(key, defValue);
    }

    /**
     * Retrieve a long value from the preferences filename.
     */
    public static long getLong(Context context, String key, long defValue, String filename) {
        return getSharedPreferences(context, filename).getLong(key, defValue);
    }

    /**
     * Set a long value in the default preferences.
     */
    public static void putLong(Context context, String key, long value) {
        getEditor(context).putLong(key, value).apply();
    }

    /**
     * Set a long value in the preferences filename.
     */
    public static void putLong(Context context, String key, long value, String filename) {
        getEditor(context, filename).putLong(key, value).apply();
    }

}

