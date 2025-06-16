package com.asus.supernote.uservoice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FeedbackReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		UserVoiceConfig.init(arg0);
	}

}
