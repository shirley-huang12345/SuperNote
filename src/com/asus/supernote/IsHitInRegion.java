package com.asus.supernote;

import android.graphics.Rect;
import android.view.View;

public class IsHitInRegion {
	public static boolean isHitIn(View region,int xInScreen,int yInScreen){
		if (region==null) {
			return false;
		}
		int[] location=new int[2];
		region.getLocationOnScreen(location);
		Rect rect=new Rect(location[0], location[1], location[0]+region.getWidth(), location[1]+region.getHeight());
		return rect.contains(xInScreen, yInScreen);
	}
}
