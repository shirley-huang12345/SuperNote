package com.asus.supernote.share.utils;

import android.content.Intent;

/**
 * @author: noah_zhang
 * @date: 2013-07-24
 * @description: share工具类
 * @remark
 * */
public class ShareUtils {
	/**
	 * 是否从浏览器share过来
	 * 
	 * @return
	 */
	public static boolean isFromBrower(Intent intent) {
		if(intent.getAction() == null) //smilefish fix google play bug
			return false; 
		if (!intent.getAction().equals(Intent.ACTION_SEND))
			return false;
		String type = intent.getType();
		if (type == null || type == "")
			return false;
		if (!type.startsWith("text/plain"))
			return false;
		String text = intent.getStringExtra(Intent.EXTRA_TEXT);
		if (text == null || text == "")
			return false;
		return isLink(text);
	}

	/**
	 * 判断是否是一个超链接
	 * 
	 * @return
	 */
	public static boolean isLink(String text) {
		if (text == null || text == "")
			return false;
		if (text.contains(" "))
			return false;
		return text.startsWith("http://") || text.startsWith("https://");
	}
}
