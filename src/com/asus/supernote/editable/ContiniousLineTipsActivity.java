package com.asus.supernote.editable;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.picker.PickerUtility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class ContiniousLineTipsActivity extends Activity {
	private FrameLayout mLayout = null;
	private ImageView mWelcomeImage1 = null;
	private ImageView mWelcomeImage2 = null;
	private TextView mDesc = null;
	private int mStep = 0;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // BEGIN: Better
        if (MetaData.AppContext == null) {
    		MetaData.AppContext = getApplicationContext();
		}
        // END: Better
        
        if ((PickerUtility.getDeviceType(this) > 100)
        	|| (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT)) {
        	setResult(Activity.RESULT_CANCELED);
        	finish();
        	return;
        }
        
        setContentView(R.layout.continious_line_tips);
        
        mLayout = (FrameLayout) findViewById(R.id.continious_line_layout);
        mWelcomeImage1 = (ImageView) findViewById(R.id.image_welcome1);
        mWelcomeImage2 = (ImageView) findViewById(R.id.image_welcome2);
        mDesc = (TextView) findViewById(R.id.continiouse_line_desc);
        Button btnContinue = (Button) findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(mOnClickContinueListener);
	}
	
	//emmanual to fix bug 497001
	@Override
    protected void onDestroy() {
	    // TODO Auto-generated method stub
		SharedPreferences sharedPref = getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(getResources().getString(R.string.pref_prompt_handwriting_animating), false);
		editor.commit();
	    super.onDestroy();
    }

	private OnClickListener mOnClickContinueListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (mStep == 0) {
				mWelcomeImage1.setVisibility(View.GONE);
				mWelcomeImage2.setVisibility(View.VISIBLE);
				mDesc.setText(R.string.continious_line_desc2);
				mStep++;
			} else if (mStep == 1) {
				SharedPreferences sharedPref = getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putBoolean(getResources().getString(R.string.pref_prompt_handwriting_animating), false);
				editor.commit();
				setResult(RESULT_OK);
				finish();
			}
		}
		
	};
}
