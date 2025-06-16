package com.asus.updatesdk.utility;

import android.os.Build;

/**
 * Created by Ken1_Yu on 2015/7/14.
 */
public class SystemPropertiesReflection {
    public static final class Key {
        public static final String BUILD_ASUS_VERSION = "ro.build.asus.version";
        public static final String BUILD_ASUS_SKU     = "ro.build.asus.sku";
        public static final String BUILD_PRODUCT      = "ro.build.product";
        public static final String PRODUCT_DEVICE     = "ro.product.device";
        public static final String CPU_ABILIST = "ro.product.cpu.abilist";
        public static final String CPU_ABI = "ro.product.cpu.abi";
        public static final String CPU_ABI2 = "ro.product.cpu.abi2";
    }

    private static final String CLASS_SYSTEM_PROPERTIES = "android.os.SystemProperties";
    private static final Class<?> CLASS = ReflectionUtils.getClass(CLASS_SYSTEM_PROPERTIES);

    private static final String METHOD_GET         = "get";
    private static final String METHOD_GET_BOOLEAN = "getBoolean";
    private static final String METHOD_GET_INT     = "getInt";
    private static final String METHOD_GET_LONG    = "getLong";
    private static final Class<?>[] PARAM_TYPE_GET         = { String.class, String.class };
    private static final Class<?>[] PARAM_TYPE_GET_BOOLEAN = { String.class, boolean.class };
    private static final Class<?>[] PARAM_TYPE_GET_INT     = { String.class, int.class };
    private static final Class<?>[] PARAM_TYPE_GET_LONG    = { String.class, long.class };

    /**
     * {@link android.os.SystemProperties#get(String)}
     */
    public static String get(String key) {
        return get(key, Build.UNKNOWN);
    }

    /**
     * {@link android.os.SystemProperties#get(String, String)}
     */
    public static String get(String key, String def) {
        return get(METHOD_GET, PARAM_TYPE_GET, key, def);
    }

    /**
     * {@link android.os.SystemProperties#getBoolean(String, boolean)}
     */
    public static boolean getBoolean(String key, boolean def) {
        return get(METHOD_GET_BOOLEAN, PARAM_TYPE_GET_BOOLEAN, key, def);
    }

    /**
     * {@link android.os.SystemProperties#getInt(String, int)}
     */
    public static int getInt(String key, int def) {
        return get(METHOD_GET_INT, PARAM_TYPE_GET_INT, key, def);
    }

    /**
     * {@link android.os.SystemProperties#getLong(String, long)}
     */
    public static long getLong(String key, long def) {
        return get(METHOD_GET_LONG, PARAM_TYPE_GET_LONG, key, def);
    }

    private static <T> T get(String methodName, Class<?>[] paramType, String key, T def) {
        T systemProperties = ReflectionUtils.callFeatureMethod(methodName, CLASS, paramType,
                new Object[] {key, def});
        return systemProperties != null ? systemProperties : def;
    }
}
