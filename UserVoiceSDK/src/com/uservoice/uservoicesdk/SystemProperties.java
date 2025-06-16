package com.uservoice.uservoicesdk;

import java.lang.reflect.Method;

public class SystemProperties{
    public static String get(String key){
        Class clazz = null;
        String prop = null;
        try{
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            prop = (String)method.invoke(null, key);
            //Log.e("so_test", "my prop is: <" + prop  + ">");
        }catch(java.lang.Exception e){
        }
        if(prop == null)
            return "";
        else return prop;
    }

    public static String get(String key, String defaultValue){
        Class clazz = null;
        String prop = null;
        try{
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            prop = (String)method.invoke(null, key);
            //Log.e("so_test", "my prop is: <" + prop  + ">");
	    }catch(java.lang.Exception e){
	    }
        if(prop == null || prop.equals(""))
            return defaultValue;
        else return prop;
    }

    public static void set(String key, String value){
    }
}