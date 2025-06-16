package com.asus.supernote.classutils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.asus.supernote.picker.ForcedPermissionActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

public class MethodUtils {
	private static final String SIGN = "test_time_";
	private static final String TAG = "MethodUtils";
	private static long sLastTime = System.currentTimeMillis();
	public static final int REQUEST_PERMISSION_PAGE = 18;

	public static void startMethodTracing(String tag) {
		String realTag = TAG;
		if (tag != null && tag != "")
			realTag = tag;
		long startTime = System.currentTimeMillis();
		sLastTime = startTime;
		Log.i(realTag, SIGN + "开始时间:" + startTime);
	}

	public static void stopMethodTracing(String tag) {
		String realTag = TAG;
		if (tag != null && tag != "")
			realTag = tag;
		long endTime = System.currentTimeMillis();
		Log.i(realTag, SIGN + "结束时间:" + endTime);
		Log.i(realTag, SIGN + "用时:" + (endTime - sLastTime));
		sLastTime = endTime;
	}
	
    public static String getFileNameNoExtension(String filename) {
        if (filename == null) {
            return "";
        }
        String dotString = ".";
        int index = filename.lastIndexOf(dotString);
        String retString = "";
        if (index > 0 && index <= filename.length()) {
            retString = filename.substring(0, index);
        } else {
            retString = filename;
        }
        return retString;
    }
    
    public static boolean isFileNameOk(String name,File[] files){
    	if(files==null)
        {
            return true;
        }

        for (File file : files) {
            
            if(!file.isDirectory())
            {
                String existNameString=file.getName();
                String noExtName= getFileNameNoExtension(existNameString);
                    
                if(noExtName.equalsIgnoreCase(name))
                {               
                    return false;
                }
            }
        }
        
        return true;
    }
    
	public static float getPressureRatio(){ //Smilefish
		String model = SystemProperties.get("ro.product.model");
		String device = SystemProperties.get("ro.product.device");
		if(device!=null && model!=null){
			if(model.equalsIgnoreCase("P01M")||model.equalsIgnoreCase("P01MA")  //Z580C
					||model.equalsIgnoreCase("P023")||model.equalsIgnoreCase("P021")){ //Z300C/CG
			    return 1.5f;
			}
		}
		return 2.0f;
	}
	
	public static int getMinAlpha(Context context) { // Smilefish
		if (isPenDevice(context)) {
			return 125;
		}
		return 10;
	}
	
	// Emmanual
	public static boolean isPenDevice(Context context) {
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
			boolean activeStylusSupport = false;
			try {
			    Method m = View.class.getMethod("hasActiveStylusSupport");
			    m.setAccessible(true);
			    activeStylusSupport = (Boolean)m.invoke(null);
			} catch(Exception e){
				e.printStackTrace();
			}
			return activeStylusSupport;
		}else{
			if(context == null){
				return false;
			}
			PackageManager pm = context.getPackageManager();
			return pm != null && pm.hasSystemFeature("asus.hardware.pen");
		}
	}
	
	public static boolean isAirViewTextHintEnable(Context context){ //smilefish fix bug 621605
		boolean isAirViewEnabled = false;
		boolean isPreviewEnabled = false;
		try {
			Field f = Settings.System.class.getField("AIRVIEW");
			f.setAccessible(true);
			String airview = (String)f.get(null);
			isAirViewEnabled = Settings.System.getInt(context.getContentResolver(), airview, -1) == 1 ? true : false;

			f = Settings.System.class.getField("AIRVIEW_PREVIEW");
			f.setAccessible(true);
			String preview = (String)f.get(null);
			isPreviewEnabled = Settings.System.getInt(context.getContentResolver(), preview, -1) == 1 ? true : false;
		} catch(Exception e){
			e.printStackTrace();
		}

		// Should check both "airview" & its sub-item
		if(isAirViewEnabled) {
			if(isPreviewEnabled) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isEnableAirview(Context context){
		return isPenDevice(context);
	}
	public static boolean isEnableAirViewContentPreview(Context context){
		return isEnableAirview(context);
	}
	public static boolean isEnableAirViewActionBarHint(Context context){
		if (isEnableAirview(context) && isAirViewTextHintEnable(context)) {
			return true;
		}

		return false;
	}
	
	public static boolean needShowPermissionPage(Context context){
		boolean isPermissionGranted = ContextCompat.checkSelfPermission(context, 
				Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !isPermissionGranted){
			return true;
		}else{
			return false;
		}
	}
	
	public static void showPermissionPage(Activity activity, boolean isNeedResultCode){
  		try {
  			Intent intent = new Intent();
  			intent.setClass(activity, ForcedPermissionActivity.class);
  			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); 
  			if(isNeedResultCode){
  				activity.startActivityForResult(intent, REQUEST_PERMISSION_PAGE);
  			}
  			else{
  				activity.startActivity(intent);
  			}
  		} catch (Exception e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}
	}
	
	public static void showPermissionPage(Context context){
		try {
  			Intent intent = new Intent();
  			intent.setClass(context, ForcedPermissionActivity.class);
  			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
  			context.startActivity(intent);
  		} catch (Exception e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}
	}
	
    public static String toSafeString(Uri uri) {
        String scheme = uri.getScheme();
        String ssp = uri.getSchemeSpecificPart();
        if (scheme != null) {
            if (scheme.equalsIgnoreCase("tel") || scheme.equalsIgnoreCase("sip")
                    || scheme.equalsIgnoreCase("sms") || scheme.equalsIgnoreCase("smsto")
                    || scheme.equalsIgnoreCase("mailto")) {
                StringBuilder builder = new StringBuilder(64);
                builder.append(scheme);
                builder.append(':');
                if (ssp != null) {
                    for (int i=0; i<ssp.length(); i++) {
                        char c = ssp.charAt(i);
                        if (c == '-' || c == '@' || c == '.') {
                            builder.append(c);
                        } else {
                            builder.append('x');
                        }
                    }
                }
                return builder.toString();
            }
        }
        // Not a sensitive scheme, but let's still be conservative about
        // the data we include -- only the ssp, not the query params or
        // fragment, because those can often have sensitive info.
        StringBuilder builder = new StringBuilder(64);
        if (scheme != null) {
            builder.append(scheme);
            builder.append(':');
        }
        if (ssp != null) {
            builder.append(ssp);
        }
        return builder.toString();
    }
}
