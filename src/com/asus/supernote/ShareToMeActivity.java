package com.asus.supernote;

import com.asus.supernote.picker.NoteBookPickerActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
/**
 * @author: noah_zhang
 * @date: 2013-07-24
 * @description: 其他程序share给SuperNote的入口。该activity没有ui
 * @remark
 * */
public class ShareToMeActivity extends Activity {
	
	public static ShareToMeActivity mShareToMeActivity; //Carol
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		mShareToMeActivity = this;
		Intent intent = new Intent(this, NoteBookPickerActivity.class);
		intent.putExtra(Intent.EXTRA_INTENT, this.getIntent());
		startActivity(intent);
		//this.finish();
	}
}
