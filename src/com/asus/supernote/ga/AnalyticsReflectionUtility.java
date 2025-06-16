package com.asus.supernote.ga;

import android.content.Context;
import android.database.ContentObserver;
import android.provider.Settings;

public class AnalyticsReflectionUtility {
    private static String Asus_Settings = null;
    static {
        try {
            Asus_Settings = (String) (Class.forName("android.provider.Settings$System").getField("ASUS_ANALYTICS").get(String.class));
        } catch (IllegalArgumentException e) {
            // Error : IllegalArgumentException
        } catch (IllegalAccessException e) {
            // Error : IllegalAccessException
        } catch (NoSuchFieldException e) {
            // Error : NoSuchFieldException
        } catch (ClassNotFoundException e) {
            // Error : ClassNotFoundException
        }
    }
    public static boolean getEnableAsusAnalytics(Context context) {
        if (Asus_Settings == null) {
            return false;
        }
        return Settings.System.getInt(context.getContentResolver(),
                Asus_Settings, 0) == 1 ? true : false;
    }
    public static void registerContentObserver(Context context,
            ContentObserver observer) {
        if (Asus_Settings != null) {
            context.getContentResolver().registerContentObserver(Settings.System.getUriFor(Asus_Settings), false, observer);
        }
    }
     
    public static void unregisterContentObserver(Context context, ContentObserver observer) {
        context.getContentResolver().unregisterContentObserver(observer);
    }
}
