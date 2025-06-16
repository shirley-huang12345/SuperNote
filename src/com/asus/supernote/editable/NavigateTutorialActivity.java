package com.asus.supernote.editable;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NavigateTutorialActivity extends Activity{

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.navigate_tutorial);
        
        Button btnOK = (Button) findViewById(R.id.btn_ok);
        btnOK.setOnClickListener(mOnClickOKListener);
	}
	
	@Override
    protected void onDestroy() {
	    // TODO Auto-generated method stub
		SharedPreferences sharedPref = getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(getResources().getString(R.string.pref_prompt_navigate_tutorial), false);
		editor.commit();
	    super.onDestroy();
    }
	
	private OnClickListener mOnClickOKListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			setResult(RESULT_OK);
			finish();
		}
		
	};
}
