package com.asus.supernote.dialog.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * @author: noah_zhang
 * @date: 2013-08-09
 * @description: dilaog工具类
 * @remark
 * */
public class DialogUtils {
	private static final Handler sHandler = new Handler();

	/**
	 * 在360dp以下，如果dialog上有edittext，默认不会弹出keyboard。 设置相关属性，强制其弹出
	 */
	public static void forcePopupSoftInput(Dialog dialog) {
		if (dialog == null) {
			return;
		}
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}

	/**
	 * 在360dp以下，如果dialog上有edittext，默认不会弹出keyboard。 
	 * 如果该dialog，在java代码里强制设置了width,height,即使用forceDialogPopupKeyboard(Dialog dialog)，也不能弹出
	 * 这个方法直接用InputMethodManager，迫使软键盘弹出
	 */
	public static void showSoftInput(final EditText editText) {
		if (editText == null)
			return;
		sHandler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				InputMethodManager inputManager = (InputMethodManager) editText
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(editText, 0);
			}
		});
	}
	
	public static void hideSoftInput(final EditText editText){
		if (editText == null)
			return;
		sHandler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				InputMethodManager inputManager = (InputMethodManager) editText
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
			}
		});
	}
}
