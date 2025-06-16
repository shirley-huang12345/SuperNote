package com.asus.supernote.sync;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

import com.asus.supernote.data.MetaData;

public class SyncHelper {
    public static final long SYNC_PAGE_ID_BASE = (long)(10E12);
    
    public static long pageTime2Id(long time) {
        if (time <= SYNC_PAGE_ID_BASE) {
            time += SYNC_PAGE_ID_BASE;
        }
        return time;
    }
    
    public static long pageId2Time(long id) {
        if (id > SYNC_PAGE_ID_BASE) {
            id -= SYNC_PAGE_ID_BASE;
        }
        return id;
    }
    
    public static boolean isVersionSupported(String version) {
    	if (version != null) {
	    	for (String v : MetaData.SupportedVersions) {
	    		if (v.equalsIgnoreCase(version)) {
	    			return true;
	    		}
	    	}
    	}
    	return false;
    }
    
    public static boolean isForeground(Context context) {
        ActivityManager activityManager = (ActivityManager)context.getSystemService("activity"); 
        List<ActivityManager.RunningTaskInfo> list = activityManager.getRunningTasks(1);
        
        if (list.size() > 0) {
            ActivityManager.RunningTaskInfo temp = list.get(0);
            String PackageName = null;
            if(temp != null )
            {
                PackageName = temp.baseActivity.getPackageName();
            }
            if ((PackageName != null) && PackageName.equals("com.asus.supernote")) {
                return true;
            }
        }
        
        return false;
    }
}
