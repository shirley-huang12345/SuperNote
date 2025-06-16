package com.asus.updatesdk.utility;

import com.asus.updatesdk.utility.SystemPropertiesReflection.Key;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

/**
 * Get device information
 *
 * @author Hellena
 * @date 2015/02/06
 * @version 1.0
 */
public class DeviceUtils {

    public static final String SYSPROP_ASUS_SKU = SystemPropertiesReflection
            .get(Key.BUILD_ASUS_SKU);

    public static final String SYSPROP_ASUS_VERSION = SystemPropertiesReflection
            .get(Key.BUILD_ASUS_VERSION);

    public static final String SYSPROP_BUILD_PRODUCT = SystemPropertiesReflection
            .get(Key.BUILD_PRODUCT);

    public static final String SYSPROP_PRODUCT_DEVICE = SystemPropertiesReflection
            .get(Key.PRODUCT_DEVICE);

    public static final String SYSPROP_CPU_ABILIST = SystemPropertiesReflection
            .get(Key.CPU_ABILIST);

    public static final String SYSPROP_CPU_ABI = SystemPropertiesReflection
            .get(Key.CPU_ABI);

    public static final String SYSPROP_CPU_ABI2 = SystemPropertiesReflection
            .get(Key.CPU_ABI2);

    private static final String BRAND_ASUS = "asus";
    private static final String BRAND_GOOGLE = "google";

    public static boolean checkAsusDevice() {
        return Build.BRAND.equalsIgnoreCase(BRAND_ASUS) ||
                (Build.MANUFACTURER.equalsIgnoreCase(BRAND_ASUS) &&
                !Build.BRAND.equalsIgnoreCase(BRAND_GOOGLE));
    }

    public static boolean checkATTSku() {
        return checkSku("ATT");
    }

    public static boolean checkCnSku() {
        return checkSku("CN", "CTA", "CUCC", "CMCC");
    }

    /** SKU Checker. */
    private static boolean checkSku(String ... targetSkuList) {
        if (targetSkuList == null) return false;

        String sku = SystemPropertiesReflection.get(Key.BUILD_ASUS_SKU, null);
        if (sku == null) return false;

        for (String targetSku : targetSkuList) {
            if (sku.toLowerCase(Locale.US).startsWith(targetSku.toLowerCase(Locale.US))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the device is voice-capable (meaning, it is also a phone).
     */
    public static boolean isVoiceCapable(Context context) {
        boolean voiceCapable = false;
        int vid = Resources.getSystem().getIdentifier("config_voice_capable", "bool", "android");
        try {
            voiceCapable = context.getResources().getBoolean(vid);
        }
        catch (Resources.NotFoundException e){
            e.printStackTrace() ;
        }
        return voiceCapable;
    }
}
