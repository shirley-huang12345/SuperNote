package com.asus.supernote.uservoice;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.os.SystemProperties;
import android.view.View;
import android.view.View.OnClickListener;

import com.asus.supernote.R;
import com.uservoice.uservoicesdk.NewConfigInterface;
import com.uservoice.uservoicesdk.UserVoice;

public class UserVoiceConfig extends NewConfigInterface{

	int mActionBarColor = 0;
	String mCatalogName = null;
	
	public UserVoiceConfig(int color, String catalogName){
		mActionBarColor = color;
		mCatalogName = catalogName;
	}
	
	@Override
    public int getTopicID() {
		return 0;
    }

    @Override
    public int getForumID() {
    	return 0 ;
    }

    
	@Override
    public int getPrimaryColor() {
	    // TODO Auto-generated method stub
	    return mActionBarColor;
    }

	@Override
	public String getAppCatalogName() {
		return mCatalogName;
	}
	
    public static void init(final Context context){
    	String catalogName = context.getString(R.string.catalog_name);
    	int color = context.getResources().getColor(R.color.uservoice_action_bar_color);
    	final UserVoiceConfig config = new UserVoiceConfig(color, catalogName);
    	
		if(SystemProperties.get("ro.build.asus.sku").equals("CN")){
			Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(context.getResources().getString(
			        R.string.cta_act_mobile_network));
			builder.setMessage(context.getResources().getString(
			        R.string.cta_msg_mobile_network));
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setNegativeButton(android.R.string.cancel, null);
			final AlertDialog dialog = builder.create();
			dialog.show();
			dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
							UserVoice.init(config, context);
							UserVoice.launchUserVoice(context);
						}
					});
			dialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
		}else{
			UserVoice.init(config, context);
			UserVoice.launchUserVoice(context);
		}
    	
    }

}
