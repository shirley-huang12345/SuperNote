package com.asus.supernote.sync;

import com.asus.supernote.EncryptAES;
import com.asus.supernote.R;
import com.asus.supernote.classutils.ColorfulStatusActionBarHelper;
import com.asus.supernote.data.MetaData;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class SyncSettingActivity extends PreferenceActivity implements OnClickListener {
	private static Resources mResources = null;
	private static Context mContext = null;
	private static Preference mAccountPreference = null;
	private static Preference mFrequencyPreference = null;
	private static Preference mAccountUesdPreference = null; //By Show
	private static CheckBoxPreference mAutoPreference = null;
	private static String[] items = new String[5];
	private static final int SET_AUTO_SYNC_FREQUENCY_DIALOG = 2;
	private static CheckBoxPreference mWifiPreference = null; //smilefish

	@Override
    protected void onCreate(Bundle savedInstanceState) {		
        // BEGIN: Better
        if (MetaData.AppContext == null) {
    		MetaData.AppContext = getApplicationContext();
		}
        // END: Better
		
		int IsOrientationAllowOrNot = this.getResources().getInteger(R.integer.is_orientation_allow); //by show
        if (IsOrientationAllowOrNot == 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        else  if (IsOrientationAllowOrNot ==1){
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
		 else  {
			 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //End: show_wang@asus.com
		super.onCreate(savedInstanceState);
				
		ColorfulStatusActionBarHelper.setContentView(R.layout.preference_layout, true, this);//smilefish
		
		addPreferencesFromResource(R.xml.sync_settings);

		mResources = getResources();
		mContext = this;
		
		items[0] = mResources.getString(R.string.sync_auto_interval_1);
		items[1] = mResources.getString(R.string.sync_auto_interval_15);
		items[2] = mResources.getString(R.string.sync_auto_interval_30);
		items[3] = mResources.getString(R.string.sync_auto_interval_60);
		items[4] = mResources.getString(R.string.sync_auto_interval_1d);
		
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		
		Preference accountPref = findPreference(mResources.getString(R.string.sync_setting_account_key));
		
		mAccountPreference = accountPref;        	
		mFrequencyPreference = findPreference(mResources.getString(R.string.sync_setting_auto_frequency_key));
		mAccountUesdPreference = findPreference(mResources.getString(R.string.account_space_usage));//By Show 
		mAutoPreference = (CheckBoxPreference)findPreference(mResources.getString(R.string.sync_setting_auto_key)); 
		mWifiPreference = (CheckBoxPreference)findPreference(mResources.getString(R.string.sync_setting_wifi_auto_key));
		SharedPreferences pref = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, 
				Context.MODE_MULTI_PROCESS);
		String sAsusAccount = pref.getString(this.getResources().getString(R.string.pref_AsusAccount), null);
		String sAsusPassword = pref.getString(this.getResources().getString(R.string.pref_AsusPassword), null);
		String RAsusKey =  mContext.getResources().getString(R.string.pref_AsusKey);
		String RAsusIV =  mContext.getResources().getString(R.string.pref_AsusIV);
		String keyStr = pref.getString(RAsusKey, "");
		String ivStr = pref.getString(RAsusIV, "");
		String usedSpace = pref.getString(this.getResources().getString(R.string.pref_Asus_AccountUsed), "0MB of 0GB");//By Show
		mAccountUesdPreference.setSummary(usedSpace);//By Show
		//Modify By Show, being
		String status = pref.getString(mResources.getString(R.string.pref_asus_auto_sync), "auto");
		if (status.equals("auto")) {
			mAutoPreference.setChecked(true);
		}
		status = pref.getString(mResources.getString(R.string.pref_asus_wifi_auto_sync), "true");
		if (status.equals("true")) {
			mWifiPreference.setChecked(true);
		}
		int interval = pref.getInt(mResources.getString(R.string.pref_default_sync_time), 
				MetaData.DEFAULT_SYNC_TIME);
		int index = -1;
		switch (interval) {
		case 1:
			index = 0;
			break;
		case 15:
			index = 1;
			break;
		case 30:
			index = 2;
			break;
		case 60:
			index = 3;
			break;
		case 24 * 60:
			index = 4;
			break;
		}
		if (index >= 0) {
			mFrequencyPreference.setSummary(items[index]);
		}
		//Modify By Show, end
		if(sAsusAccount != null && !sAsusAccount.equals("") && sAsusPassword != null && !sAsusPassword.equals("")){
			try
			{
				sAsusAccount = EncryptAES.decrypt(keyStr, ivStr, sAsusAccount);
				mAccountPreference.setSummary(sAsusAccount);
				mAutoPreference.setOnPreferenceClickListener(onPreferenceClickListener);
				String auto = pref.getString(mResources.getString(R.string.pref_asus_auto_sync), "auto");
				if (auto.equals("auto")) {
					mAutoPreference.setChecked(true);
					mAccountUesdPreference.setEnabled(true);//By Show
					mFrequencyPreference.setEnabled(true);
					mFrequencyPreference.setOnPreferenceClickListener(onPreferenceClickListener);
					mWifiPreference.setEnabled(true);
					mWifiPreference.setOnPreferenceClickListener(onPreferenceClickListener);
				} else {
					mAutoPreference.setChecked(false);
					mFrequencyPreference.setEnabled(false);
					mWifiPreference.setEnabled(false);
				}
			}
			catch(Exception e)
			{
				mAutoPreference.setEnabled(false);
				mFrequencyPreference.setEnabled(false);
				mWifiPreference.setEnabled(false);
				mAccountUesdPreference.setEnabled(false);//By Show
			}
		} else {
			mAccountPreference.setSummary("");
			mAutoPreference.setEnabled(false);
			mFrequencyPreference.setEnabled(false);
			mWifiPreference.setEnabled(false);
			mAccountUesdPreference.setEnabled(false);//By Show
		}
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MetaData.LOGIN_MESSAGE);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
//Modify By Show, Begin
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		AlertDialog.Builder builder = null;
		switch (id) {
		case SET_AUTO_SYNC_FREQUENCY_DIALOG: {
			builder = new AlertDialog.Builder(
					mContext);
			builder.setTitle(mResources
					.getString(R.string.sync_setting_auto_frequency));
			SharedPreferences pref = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, 
					Context.MODE_MULTI_PROCESS);
			int interval = pref.getInt(mResources.getString(R.string.pref_default_sync_time), 
					MetaData.DEFAULT_SYNC_TIME);//modify by show
			int index = -1;
			switch (interval) {
			case 1:
				index = 0;
				break;
			case 15:
				index = 1;
				break;
			case 30:
				index = 2;
				break;
			case 60:
				index = 3;
				break;
			case 24 * 60:
				index = 4;
				break;
			}
			builder.setSingleChoiceItems(items, index, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int interval = -1;
					switch (which) {
					case 0:
						interval = 1;
						break;
					case 1:
						interval = 15;
						break;
					case 2:
						interval = 30;
						break;
					case 3:
						interval = 60;
						break;
					case 4:
						interval = 24 * 60;
						break;
					}
					
					if (interval > 0) {
						SharedPreferences pref = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, 
								Context.MODE_MULTI_PROCESS);
						SharedPreferences.Editor prefEditor = pref.edit();
						prefEditor.putInt(
								mResources.getString(R.string.pref_default_sync_time),
								interval);
						prefEditor.commit();
						mFrequencyPreference.setSummary(items[which]);
						CountDownClass countDown = CountDownClass.getInstance(mContext.getApplicationContext());
						countDown.stopCountDown();
						countDown.StartCountDown(interval, false, true);
					}
					
					dialog.dismiss();
				}
			});
			AlertDialog alertDialog = builder.create();
			alertDialog.setCancelable(true);
			alertDialog.setCanceledOnTouchOutside(true);
			DialogInterface.OnClickListener listener = null;
			alertDialog.setButton(Dialog.BUTTON_NEGATIVE, mContext.getString(android.R.string.cancel), listener);
			return alertDialog;
		}
		}
		return null;
	}
//Modify By Show, End	

	@Override
	public void onClick(View view) {
		
	}
	
	public final OnPreferenceClickListener onPreferenceClickListener = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			String key = preference.getKey();
			if (key.equals(mResources.getString(R.string.sync_setting_auto_key))) {
				SharedPreferences pref = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, 
						Context.MODE_MULTI_PROCESS);
				SharedPreferences.Editor prefEditor = pref.edit();
				if (((CheckBoxPreference)preference).isChecked()) {
					CountDownClass countDown = CountDownClass.getInstance(mContext.getApplicationContext());
					int interval = pref.getInt(mResources.getString(R.string.pref_default_sync_time), 
							MetaData.DEFAULT_SYNC_TIME);//Modify By Show
					countDown.StartCountDown(interval, false, true);
					prefEditor.putString(mContext.getResources().getString(R.string.pref_asus_auto_sync), 
							"auto");
					prefEditor.commit();
					mAccountUesdPreference.setEnabled(true);//By Show
					mFrequencyPreference.setEnabled(true);
					mFrequencyPreference.setOnPreferenceClickListener(onPreferenceClickListener);
					mWifiPreference.setEnabled(true);
					mWifiPreference.setOnPreferenceClickListener(onPreferenceClickListener);
					int index = -1;
					switch (interval) {
					case 1:
						index = 0;
						break;
					case 15:
						index = 1;
						break;
					case 30:
						index = 2;
						break;
					case 60:
						index = 3;
						break;
					case 24 * 60:
						index = 4;
						break;
					}
					if (index >= 0) {
						mFrequencyPreference.setSummary(items[index]);
					}
				} else {
					CountDownClass countDown = CountDownClass.getInstance(mContext.getApplicationContext());
					countDown.stopCountDown();
					prefEditor.putString(mContext.getResources().getString(R.string.pref_asus_auto_sync), 
							"manual");
					prefEditor.commit();
					
					mFrequencyPreference.setEnabled(false);
					mWifiPreference.setEnabled(false);
				}
				return true;
			} else if (key.equals(mResources.getString(R.string.sync_setting_auto_frequency_key))) {
				showDialog(SET_AUTO_SYNC_FREQUENCY_DIALOG);
				return true;
			} else if(key.equals(mResources.getString(R.string.sync_setting_wifi_auto_key))) {
				SharedPreferences pref = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, 
						Context.MODE_MULTI_PROCESS);
				SharedPreferences.Editor prefEditor = pref.edit();
				if (((CheckBoxPreference)preference).isChecked()) {
					prefEditor.putString(mContext.getResources().getString(R.string.pref_asus_wifi_auto_sync), 
							"true");
					prefEditor.commit();
				}else{
					prefEditor.putString(mContext.getResources().getString(R.string.pref_asus_wifi_auto_sync), 
							"false");
					prefEditor.commit();
				}
			}

			return false;
		}
	};
}
