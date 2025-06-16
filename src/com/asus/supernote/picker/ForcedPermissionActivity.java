package com.asus.supernote.picker;

import com.asus.supernote.R;
import com.asus.supernote.classutils.MethodUtils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ForcedPermissionActivity extends Activity{
	public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
	private Button turnOnButton;
	private ImageView permissionImage;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        setContentView(R.layout.forced_permission_page);
        
        TextView subTitle = (TextView)findViewById(R.id.force_permission_sub_title);
        String app_name = this.getString(R.string.app_name);
        String req_permission = this.getString(R.string.force_permission_storage);
        String subTitleText = getString(R.string.force_permission_sub_title, app_name, req_permission);
        subTitle.setText(makeBoldText(subTitleText, app_name, req_permission));
        
        turnOnButton = (Button)findViewById(R.id.turn_on_button);
        turnOnButton.setOnClickListener(onTurnOnButtonListener);
		if(!showRationale()){
			turnOnButton.setText(R.string.force_permission_settings);
		}

		permissionImage = (ImageView)findViewById(R.id.force_permission_image);
		boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		if(isLandscape){
			permissionImage.setBackgroundResource(R.drawable.asus_permission_image_l);
		}else{
			permissionImage.setBackgroundResource(R.drawable.asus_permission_image_p);
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		if(!MethodUtils.needShowPermissionPage(this)){
			setResult(RESULT_OK);
        	ForcedPermissionActivity.this.finish();
		}
	}
	
	private OnClickListener onTurnOnButtonListener = new OnClickListener() {

		@Override
        public void onClick(View arg0) {	
			if (showRationale()){
				showPermissionDialog();  // show the dialog that asks for permission
			}else{
				goSettings();   // go Settings
			}

		}
	};
	
	private boolean showRationale(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		boolean requested = sp.getBoolean("STORAGE_PERMISSION_REQUESTED", false);
		return !requested || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}
	
	private void showPermissionDialog(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
	    sp.edit().putBoolean("STORAGE_PERMISSION_REQUESTED", true).commit();
	    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
	}
	
	private void goSettings(){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setClassName("com.android.settings", "com.android.settings.applications.InstalledAppDetails");
		intent.putExtra("package", getPackageName());
		intent.putExtra(":settings:fragment_args_key", "permission_settings");
		intent.putExtra(":settings:fragment_args_key_highlight_times", 3);

		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}

		finish();
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode,
	        String permissions[], int[] grantResults) {
	    switch (requestCode) {
	        case REQUEST_WRITE_EXTERNAL_STORAGE: {
	            // If request is cancelled, the result arrays are empty.
	            if (grantResults.length > 0
	                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
	            	setResult(RESULT_OK);
	            	ForcedPermissionActivity.this.finish();
	            }else if(!showRationale()){
					turnOnButton.setText(R.string.force_permission_settings);
				}
	            
	            return;
	        }

	        // other 'case' lines to check for other
	        // permissions this app might request
	    }
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	
    private SpannableString makeBoldText (String str, String ... targetStrings) {
        SpannableString resStr = new SpannableString(str);
        int startIndex, endIndex;
        for (String target : targetStrings) {
            startIndex = str.indexOf(target);
            if (startIndex != -1) {
                endIndex = startIndex + target.length();
                resStr.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, 0);
            }
        }
        return resStr;
    }
}
