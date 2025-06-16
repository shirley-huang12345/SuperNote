package com.asus.supernote.dialog.utils;

import android.text.InputType;
import android.widget.TextView;

/**
 * @author: noah_zhang
 * @date: 2013-08-09
 * @description: EditText工具类
 * @remark
 * */
public class TextViewUtils {
	/**
	 * 启用首字母大小写模式
	 */
	public static void enableCapSentences(TextView textView){
		if(textView == null)
			return;
		int type = textView.getInputType();
		textView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | type);
	}
}
