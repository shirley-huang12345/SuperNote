package com.asus.supernote;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.asus.supernote.classutils.ColorfulStatusActionBarHelper;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.dialog.utils.DialogUtils;
import com.asus.supernote.inksearch.CFG;
import com.asus.supernote.picker.NoteBookPickerActivity;

public class SettingActivity extends PreferenceActivity {
    public static final String TAG = "SettingActivity";
    public static final int MIN_PASSWORD_LENGTH = 4;
    public static final int MAX_PASSWORD_LENGTH = 7;
    private ActionBar mActionBar;
    private Resources mResources;
    private SharedPreferences mPreference;
    private SharedPreferences.Editor mPreferenceEditor;
    private PreferenceScreen mPasswordPreference;
    private PreferenceScreen mResetPasswordPreference;
    private PreferenceScreen mStopTimePreference;
    private PreferenceScreen mAboutPreference;
    private PreferenceScreen mIndexLanguage;//RICHARD
    private CheckBoxPreference mBackspace;//Show
    private CheckBoxPreference mBaselinePreference;//smilefish
    private CheckBoxPreference mLastEditPagePreference;//emmanual
    private CheckBoxPreference mPageTranstionPreference;//emmanual
    
    // BEGIN archie_huang@asus.com
    private HashMap<Integer, String> mInputRecogTable = new LinkedHashMap<Integer, String>();
    // END archie_huang@asus.com
    
    private HashMap<Integer, String> mIndexLanguageTable = new LinkedHashMap<Integer, String>();
    
    // BEGIN: Better
    private static final int SET_PASSWORD_DIALOG = 0;
    private static final int SEND_PASSWORD_TO_MAIL_DIALOG = 1;
    private static final int SET_FAST_INPUT_INTERVAL_DIALOG = 2;
    private static final int RESET_PASSWORD_DIALOG = 3;
    private static final int RESET_PASSWORD_CONFIRM_DIALOG = 4;
    private static final int SET_INDEX_LANGUAGE_DIALOG = 5;
    // END: Better
    private static final int ABOUT_DIALOG = 6; //by show
    
    private static AlertDialog setPasswordDialog = null; //smilefish

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Begin: show_wang@asus.com
    	//Modified reason: for multi-dpi
    	
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

        {
	        // BEGIN archie_huang@asus.com
	        String[] inputRecogTexts = getResources().getStringArray(R.array.handwrite_recognition_speed_values);
	        int index = 0;
	        for (int time : MetaData.INPUT_RECOGNITION_TIMES) {
	            if (index >= inputRecogTexts.length) {
	                break;
	            }
	            mInputRecogTable.put(time, inputRecogTexts[index]);
	            index++;
	        }
	        // END archie_huang@asus.com
        }
        {
	        String[] inputRecogTexts = getResources().getStringArray(R.array.not_translate_index_language_values);
	        int index = 0;
	        for (int language : MetaData.INDEX_LANGUAGES) {
	            if (index >= inputRecogTexts.length) {
	                break;
	            }
	            mIndexLanguageTable.put(language, inputRecogTexts[index]);
	            index++;
	        }
        }
        
        
        ColorfulStatusActionBarHelper.setContentView(R.layout.preference_layout, true, this); //smilefish
        

        getPreferenceManager().setSharedPreferencesName(MetaData.PREFERENCE_NAME);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);
        mPreference = getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
        addPreferencesFromResource(R.xml.settings);
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);

        mResources = getResources();
        PreferenceManager pm = getPreferenceManager();
        mPreferenceEditor = mPreference.edit();
        mPasswordPreference = (PreferenceScreen) pm.findPreference(mResources.getString(R.string.pref_password));
        mResetPasswordPreference = (PreferenceScreen) pm.findPreference(mResources.getString(R.string.pref_reset_password));
        mStopTimePreference = (PreferenceScreen) pm.findPreference(mResources.getString(R.string.pref_fast_input_value));
        mAboutPreference = (PreferenceScreen) pm.findPreference(mResources.getString(R.string.pref_about));
        mIndexLanguage = (PreferenceScreen) pm.findPreference(mResources.getString(R.string.pref_index_language));
        mBackspace = (CheckBoxPreference) pm.findPreference(mResources.getString(R.string.pref_backspace_gesture));
        mBaselinePreference = (CheckBoxPreference) pm.findPreference(mResources.getString(R.string.pref_baseline));//smilefish
        mLastEditPagePreference = (CheckBoxPreference) pm.findPreference(mResources.getString(R.string.pref_last_edit_page));//emmanual
        mPageTranstionPreference = (CheckBoxPreference) pm.findPreference(mResources.getString(R.string.pref_page_transtion));//emmanual
        
        mPasswordPreference.setOnPreferenceClickListener(onClickListener);
        mResetPasswordPreference.setEnabled(mPreference.getBoolean(mResources.getString(R.string.pref_has_password), false));
        mResetPasswordPreference.setOnPreferenceClickListener(onClickListener);
        mStopTimePreference.setOnPreferenceClickListener(onClickListener);
        mIndexLanguage.setOnPreferenceClickListener(onClickListener);
        mBackspace.setOnPreferenceClickListener(onClickListener);
        mBaselinePreference.setOnPreferenceClickListener(onClickListener);//smilefish
        mLastEditPagePreference.setOnPreferenceClickListener(onClickListener);//emmanual
        mPageTranstionPreference.setOnPreferenceClickListener(onClickListener);//emmanual
        mAboutPreference.setOnPreferenceClickListener(onClickListener); //modified by show
        // BEGIN: archie_huang@asus.com
        int recogTime = mPreference.getInt(mStopTimePreference.getKey(), 400);
        mStopTimePreference.setSummary(mInputRecogTable.get(recogTime));
        // END: archie_huang@asus.com
        
        //BEGIN: RICHARD
        int indexLanguageRegion = mPreference.getInt(mIndexLanguage.getKey(), 0);
        mIndexLanguage.setSummary(mIndexLanguageTable.get(indexLanguageRegion));
        //END: RICHARD

        boolean hasPassword = mPreference.getBoolean(mResources.getString(R.string.pref_has_password), false);
        if (hasPassword == false) {
            mPreferenceEditor.putBoolean(mResources.getString(R.string.pref_has_password), false);
            mPreferenceEditor.commit();
        }
        
        //Begin: Show_wang@asus.com
        //Modified reason: backspace gesture 
        boolean isBackspace = mPreference.getBoolean(mResources.getString(R.string.pref_backspace_gesture), false);
        if (isBackspace) {
        	mBackspace.setChecked(true);
        }
        else {
        	mBackspace.setChecked(false);
        }
      //End: Show_wang@asus.com
        //begin smilefish
        boolean isBaseline = mPreference.getBoolean(mResources.getString(R.string.pref_baseline), false);//smilefish
        if(isBaseline){
        	mBaselinePreference.setChecked(true);
        }
        else{
        	mBaselinePreference.setChecked(false);
        }
        	
        //end smilefish
        
        //begin emmanual
        boolean isOpenLastEditPage = mPreference.getBoolean(mResources.getString(R.string.pref_last_edit_page), false);//emmanual
        if(isOpenLastEditPage){
        	mLastEditPagePreference.setChecked(true);
        }
        else{
        	mLastEditPagePreference.setChecked(false);
        }        	

        boolean isPageTranstion = mPreference.getBoolean(mResources.getString(R.string.pref_page_transtion), true);//emmanual
        if(isPageTranstion){
        	mPageTranstionPreference.setChecked(true);
        }
        else{
        	mPageTranstionPreference.setChecked(false);
        }        	
        //end emmanual
        
     // BEGIN: Shane_Wang 2012-10-29
        if(CFG.getCanDoVO() == false) {
        	try{
        		PreferenceScreen preferenceScreen = getPreferenceScreen(); 
                preferenceScreen.removePreference( (PreferenceScreen)mIndexLanguage);
        	}catch(Exception e) {
        		e.toString();
        	}
        }      
        // END: Shane_Wang 2012-10-29
    }
    
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case SET_PASSWORD_DIALOG: {
	        return setPassword();
		}
		case SEND_PASSWORD_TO_MAIL_DIALOG: {
			return sendPasswordToMail(args.getString("password"));
		}
		case SET_FAST_INPUT_INTERVAL_DIALOG: {
			return setFastInput();
		}
		case RESET_PASSWORD_DIALOG: {
			return resetPassword();
		}
		case RESET_PASSWORD_CONFIRM_DIALOG: {
			return resetPasswordConfirm();
		}
		case SET_INDEX_LANGUAGE_DIALOG:{
			return resetIndexLanguage();
		}
		//BEGIN: show_wang@asus.com
		//Modified reason: show about dialog
		case ABOUT_DIALOG:{
			return aboutDialog();
		}
		//END: show_wang@asus.com
		}
		
		return null;
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		switch (id) {
		case SET_PASSWORD_DIALOG: {
			final AlertDialog d = (AlertDialog)dialog;
			d.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                final EditText oldPasswordText = (EditText) d.findViewById(R.id.oldPassword);
	                final EditText newPasswordText = (EditText) d.findViewById(R.id.password);
	                final EditText confirmPasswordText = (EditText) d.findViewById(R.id.password2);
	                
	                boolean hasPassword = mPreference.getBoolean(getResources().getString(R.string.pref_has_password), false);
	                if (hasPassword) {
	                    String oldPassword = mPreference.getString(getResources().getString(R.string.pref_password), "");
	                    String newPassword = newPasswordText.getText().toString().trim();
	                    int passwordLength = newPassword.length();
	                    if (passwordLength < MIN_PASSWORD_LENGTH || passwordLength > MAX_PASSWORD_LENGTH) {
	                        oldPasswordText.setText("");
	                        newPasswordText.setText("");
	                        confirmPasswordText.setText("");
	                        //                        newPasswordText.setHint(R.string.password_tips);
	                    }
	                    else if ((oldPasswordText.getText().toString().equals(oldPassword)) == false) {
	                        oldPasswordText.setText("");
	                        newPasswordText.setText("");
	                        confirmPasswordText.setText("");
	                        oldPasswordText.setHint(R.string.reset_password_dialog_password_invalid);
	                    }
	                    else if ((newPassword.equals(confirmPasswordText.getText().toString())) == false) {
	                        oldPasswordText.setText("");
	                        newPasswordText.setText("");
	                        confirmPasswordText.setText("");
	                        confirmPasswordText.setHint(R.string.password_diff_password);
	                    }
	                    else {
	                        removeDialog(SET_PASSWORD_DIALOG);
	                        mPreferenceEditor.putString(getResources().getString(R.string.pref_password), newPassword);
	                        mPreferenceEditor.putBoolean(getResources().getString(R.string.pref_has_password), true);
	                        Bundle b = new Bundle();
	                        b.putString("password", newPassword);
	                        showDialog(SEND_PASSWORD_TO_MAIL_DIALOG, b);
	                        mPreferenceEditor.commit();
	                    }
	                }
	                else {
	                    // no old password
	                    String newPassword = newPasswordText.getText().toString().trim();
	                    String confirmPassword = confirmPasswordText.getText().toString().trim();
	                    int passwordLength = newPassword.length();
	                    // illegal passowrd length
	                    if (passwordLength < MIN_PASSWORD_LENGTH || passwordLength > MAX_PASSWORD_LENGTH) {
	                        confirmPasswordText.setText("");
	                        newPasswordText.setText("");
	                        newPasswordText.setHint(R.string.password_tips);
	                        newPasswordText.requestFocus();
	                    }
	                    else if ((newPassword.equals(confirmPassword)) == false) {
	                        confirmPasswordText.setText("");
	                        confirmPasswordText.setHint(R.string.password_diff_password);
	                        newPasswordText.setText("");
	                        newPasswordText.requestFocus();
	                    }
	                    else if (newPasswordText.getText().toString().equals(confirmPasswordText.getText().toString())) {
	                        removeDialog(SET_PASSWORD_DIALOG);
	                        mPreferenceEditor.putString(getResources().getString(R.string.pref_password), newPassword);
	                        mPreferenceEditor.putBoolean(getResources().getString(R.string.pref_has_password), true);
	                        mPreferenceEditor.commit();
	                        Bundle b = new Bundle();
	                        b.putString("password", newPassword);
	                        showDialog(SEND_PASSWORD_TO_MAIL_DIALOG, b);
	                        
	                        //begin wendy
	                        
							mPreferenceEditor.putBoolean(
									getResources().getString(R.string.lock_state),
									false);
							mPreferenceEditor.commit();
							NoteBookPickerActivity.updateLockedstate(true);
	                        
	                        //end wendy
	                	}
	            	}
	            }
	        });
	        d.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                removeDialog(SET_PASSWORD_DIALOG);
	            }
	        });
			d.setOnDismissListener(new DialogInterface.OnDismissListener() {
	            @Override
	            public void onDismiss(DialogInterface dialog) {
	                mResetPasswordPreference.setEnabled(mPreference.getBoolean(mResources.getString(R.string.pref_has_password), false));
	            }
	        });
			break;
		}
		case SEND_PASSWORD_TO_MAIL_DIALOG: {
			break;
		}
		case SET_FAST_INPUT_INTERVAL_DIALOG: {
			AlertDialog d = (AlertDialog)dialog;
			
			ListView countdownList = (ListView) d.findViewById(R.id.countdown_list);

	        String[] valueTexts = getResources().getStringArray(R.array.handwrite_recognition_speed_values);
	        countdownList.setAdapter(new ArrayAdapter<String>(this, R.layout.asus_list_item_single_choice, valueTexts));
	        countdownList.setOnItemClickListener(new OnItemClickListener() {

	            @Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	                if (position >= MetaData.INPUT_RECOGNITION_TIMES.length) {
	                    return;
	                }
	                int recogTime = MetaData.INPUT_RECOGNITION_TIMES[position];
	                mPreferenceEditor.putInt(mResources.getString(R.string.pref_fast_input_value), recogTime);
	                    mPreferenceEditor.apply();
	                    mPreferenceEditor.commit();

	                mStopTimePreference.setSummary(mInputRecogTable.get(recogTime));
	                removeDialog(SET_FAST_INPUT_INTERVAL_DIALOG); // Better
	            }
	        });
	        int value = mPreference.getInt(mResources.getString(R.string.pref_fast_input_value), 400);
	        int position = 0;
	        for (int fastInputValue : MetaData.INPUT_RECOGNITION_TIMES) {
	            if (value == fastInputValue) {
	                break;
	            }
	            position++;
	        }
	        countdownList.setItemChecked(position, true);
			break;
		}
		//BEGIN: RICHARD
		case SET_INDEX_LANGUAGE_DIALOG: {
			AlertDialog d = (AlertDialog)dialog;
			
			ListView countdownList = (ListView) d.findViewById(R.id.countdown_list);

	        String[] valueTexts = getResources().getStringArray(R.array.not_translate_index_language_values);
	        countdownList.setAdapter(new ArrayAdapter<String>(this, R.layout.asus_list_item_single_choice, valueTexts));
	        countdownList.setOnItemClickListener(new OnItemClickListener() {

	            @Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	                if (position >= MetaData.INDEX_LANGUAGES.length) {
	                    return;
	                }
	                int recordIndexLaguage = MetaData.INDEX_LANGUAGES[position];
	                mPreferenceEditor.putInt(mResources.getString(R.string.pref_index_language), recordIndexLaguage);
	                    mPreferenceEditor.apply();
	                    mPreferenceEditor.commit();

	                    mIndexLanguage.setSummary(mIndexLanguageTable.get(recordIndexLaguage));
	                removeDialog(SET_INDEX_LANGUAGE_DIALOG); // Better
	            }
	        });
	        int value = mPreference.getInt(mIndexLanguage.getKey(), 0);
	        int position = 0;
	        for (int indexLanguage : MetaData.INDEX_LANGUAGES) {
	            if (value == indexLanguage) {
	                break;
	            }
	            position++;
	        }
	        countdownList.setItemChecked(position, true);
			break;
		}
		//END: RICHARD
		case RESET_PASSWORD_DIALOG: {
			final AlertDialog d = (AlertDialog)dialog;
			d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	final EditText oldPasswordInput = (EditText) d.findViewById(R.id.input_edit_text);
	                String passwordToConfirmed = null;
	                if (oldPasswordInput != null && (passwordToConfirmed = oldPasswordInput.getText().toString()) != null) {
	                    String oldPassword = mPreference.getString(mResources.getString(R.string.pref_password), null);
	                    if (passwordToConfirmed.equals(oldPassword)) {
	                        //                        mPreferenceEditor.putBoolean(mResources.getString(R.string.pref_has_password), false);
	                        //                        mPreferenceEditor.commit();
	                        // BEGIN: Better
	                    	removeDialog(RESET_PASSWORD_DIALOG);
	                        showDialog(RESET_PASSWORD_CONFIRM_DIALOG);
	                        // END: Better
	                    }
	                    else {
	                        oldPasswordInput.setText("");
	                        oldPasswordInput.setHint(R.string.reset_password_dialog_password_invalid);
	                    }
	                }

	            }
	        });

	        d.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                removeDialog(RESET_PASSWORD_DIALOG); // Better
	            }
	        });
			break;
		}
		case RESET_PASSWORD_CONFIRM_DIALOG: {
			break;
		}
		
        //Begin: Show_wang@asus.com
        //Modified reason: show about dialog
		case ABOUT_DIALOG: {
			break;
		}
        //End: Show_wang@asus.com
		}
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

    private final OnPreferenceClickListener onClickListener = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            if (key.equals(mResources.getString(R.string.pref_password))) {
                showDialog(SET_PASSWORD_DIALOG); // Better
                return true;
            }
            else if (key.equals(mResources.getString(R.string.pref_fast_input_value))) {
                showDialog(SET_FAST_INPUT_INTERVAL_DIALOG); // Better
                return true;
            }
            else if (key.equals(mResources.getString(R.string.pref_reset_password))) {
                // Case 18-1 reset password
                showDialog(RESET_PASSWORD_DIALOG); // Better
                return true;
            }
            //BEGIN: RICHARD
            else if (key.equals(mResources.getString(R.string.pref_index_language))) {
                // Case 18-1 reset password
                showDialog(SET_INDEX_LANGUAGE_DIALOG); // RICHARD
                return true;
            }
            //END: RICHARD
            
            //Begin: Show_wang@asus.com
            //Modified reason: backspace gesture
            else if (key.equals(mResources.getString(R.string.pref_backspace_gesture))) {
            	setBackspaceStatus(preference);
            	return true;
            }
            //End: Show_wang@asus.com
            
            //begin smilefish
            else if (key.equals(mResources.getString(R.string.pref_baseline))) {
            	setBaselineStatus(preference);
            	return true;
            }
            //end smilefish
            
            //begin emmanual
            else if (key.equals(mResources.getString(R.string.pref_last_edit_page))) {
            	setLastEditPageStatus(preference);
            	return true;
            }
            else if (key.equals(mResources.getString(R.string.pref_page_transtion))) {
            	setPageTranstionStatus(preference);
            	return true;
            }
            //end emmanual
            
            //Begin: Show_wang@asus.com
            //Modified reason: show about dialog
            else if (key.equals(mResources.getString(R.string.pref_about))) {
        		removeDialog(ABOUT_DIALOG);
            	showDialog(ABOUT_DIALOG); 
            	return true;
            }
            //End: Show_wang@asus.com
            else {
            return false;
        }

        }
    };
    
    //Begin: Show_wang@asus.com
    //Modified reason: backspace gesture   
    private void setBackspaceStatus(Preference preference) {
		final SharedPreferences pref = getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor prefEditor = pref.edit();  
    	if (mBackspace.isChecked()) {
    		prefEditor.putBoolean(mResources.getString(R.string.pref_backspace_gesture), true);
    		prefEditor.commit();
    	}
    	else
    	{
    		prefEditor.putBoolean(mResources.getString(R.string.pref_backspace_gesture), false);
    		prefEditor.commit();
    	}
    }
    //End: Show_wang@asus.com
    
    //begin smilefish
    private void setBaselineStatus(Preference preference) {
		final SharedPreferences pref = getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor prefEditor = pref.edit();  
    	if (mBaselinePreference.isChecked()) {
    		prefEditor.putBoolean(mResources.getString(R.string.pref_baseline), true);
    		prefEditor.commit();
    	}
    	else
    	{
    		prefEditor.putBoolean(mResources.getString(R.string.pref_baseline), false);
    		prefEditor.commit();
    	}
    }
    //end smilefish
    
    //begin emmanual
    private void setLastEditPageStatus(Preference preference) {
		final SharedPreferences pref = getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor prefEditor = pref.edit();  
    	if (mLastEditPagePreference.isChecked()) {
    		prefEditor.putBoolean(mResources.getString(R.string.pref_last_edit_page), true);
    		prefEditor.commit();
    	}
    	else
    	{
    		prefEditor.putBoolean(mResources.getString(R.string.pref_last_edit_page), false);
    		prefEditor.commit();
    	}
    }

    private void setPageTranstionStatus(Preference preference) {
		final SharedPreferences pref = getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor prefEditor = pref.edit();  
    	if (mPageTranstionPreference.isChecked()) {
    		prefEditor.putBoolean(mResources.getString(R.string.pref_page_transtion), true);
    		prefEditor.commit();
    	}
    	else
    	{
    		prefEditor.putBoolean(mResources.getString(R.string.pref_page_transtion), false);
    		prefEditor.commit();
    	}
    }
    //end emmanual
    
    // BEGIN: Better
    private Dialog setPassword() {
    	final SharedPreferences mPreference = getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
        View view = View.inflate(this, R.layout.password_dialog, null);
        final View currPasswordGroup = view.findViewById(R.id.currPasswordGroup);
        final EditText oldPasswordText = (EditText) view.findViewById(R.id.oldPassword);
        final EditText newPasswordText = (EditText) view.findViewById(R.id.password);
        final EditText confirmPasswordText = (EditText) view.findViewById(R.id.password2);
        boolean hasPassword = mPreference.getBoolean(getResources().getString(R.string.pref_has_password), false);
        currPasswordGroup.setVisibility(hasPassword ? View.VISIBLE : View.GONE);

        oldPasswordText.setTypeface(Typeface.DEFAULT);
        newPasswordText.setTypeface(Typeface.DEFAULT);
        confirmPasswordText.setTypeface(Typeface.DEFAULT);
        oldPasswordText.setTransformationMethod(new PasswordTransformationMethod());
        newPasswordText.setTransformationMethod(new PasswordTransformationMethod());
        confirmPasswordText.setTransformationMethod(new PasswordTransformationMethod());

        if (mPreference.getBoolean(getResources().getString(R.string.pref_has_password), false) == false) {
            oldPasswordText.setEnabled(false);
            newPasswordText.requestFocus();
        }
        else {
            oldPasswordText.requestFocus();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(R.string.password);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        setPasswordDialog = builder.create();
        DialogUtils.forcePopupSoftInput(setPasswordDialog); //smilefish fixed bug 395392
        setPasswordDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			 public void onCancel(DialogInterface dialog) {
                 removeDialog(SET_PASSWORD_DIALOG);
			 }
			});
        return setPasswordDialog;
    }
    
    private Dialog sendPasswordToMail(final String newPassword) {
        //using the one_msg_dialog
        final View view = View.inflate(this, R.layout.one_msg_dialog, null);
        final TextView textView = (TextView) view.findViewById(R.id.msg_text_view);
        textView.setText(R.string.password_mail_confirm);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.password_set);
        builder.setView(view);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	final SharedPreferences mPreference = getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
            	String newpassword = mPreference.getString(getResources().getString(R.string.pref_password), newPassword);
                try {
					Intent intent = new Intent();
					intent.setType("plain/text");
					intent.setAction(Intent.ACTION_VIEW);//SEND); //[TT330132][Carol]limit to email app
					
					java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance();
					String date = df.format(new Date(System.currentTimeMillis()));
					String content = String.format(getResources().getString(R.string.password_mail_content), date, newpassword);
					Uri data = Uri.parse("mailto:?subject=" + getResources().getString(R.string.password_mail_subject) + "&body=" + content);
					intent.setData(data);
					//+++ James, Use the chooser's intent instead of orignal intent
					startActivity(Intent.createChooser(intent, getResources().getString(R.string.title_send_email)));
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                //---
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();

        return dialog;
    }
    // END: Better

    public static Dialog setPassword(final Context context) {
        final SharedPreferences mPreference = context.getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
        final SharedPreferences.Editor mPreferenceEditor = mPreference.edit();
        View view = View.inflate(context, R.layout.password_dialog, null);
        final View currPasswordGroup = view.findViewById(R.id.currPasswordGroup);
        final EditText oldPasswordText = (EditText) view.findViewById(R.id.oldPassword);
        final EditText newPasswordText = (EditText) view.findViewById(R.id.password);
        final EditText confirmPasswordText = (EditText) view.findViewById(R.id.password2);
        boolean hasPassword = mPreference.getBoolean(context.getResources().getString(R.string.pref_has_password), false);
        currPasswordGroup.setVisibility(hasPassword ? View.VISIBLE : View.GONE);

        oldPasswordText.setTypeface(Typeface.DEFAULT);
        newPasswordText.setTypeface(Typeface.DEFAULT);
        confirmPasswordText.setTypeface(Typeface.DEFAULT);
        oldPasswordText.setTransformationMethod(new PasswordTransformationMethod());
        newPasswordText.setTransformationMethod(new PasswordTransformationMethod());
        confirmPasswordText.setTransformationMethod(new PasswordTransformationMethod());
        EditText focusedEditText = null;
        if (mPreference.getBoolean(context.getResources().getString(R.string.pref_has_password), false) == false) {
            oldPasswordText.setEnabled(false);
            newPasswordText.requestFocus();
            focusedEditText = newPasswordText;
        }
        else {
            oldPasswordText.requestFocus();
            focusedEditText = oldPasswordText;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(R.string.password);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        setPasswordDialog = builder.create();
        DialogUtils.forcePopupSoftInput(setPasswordDialog);
        setPasswordDialog.show();
        setPasswordDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasPassword = mPreference.getBoolean(context.getResources().getString(R.string.pref_has_password), false);
                if (hasPassword) {
                    String oldPassword = mPreference.getString(context.getResources().getString(R.string.pref_password), "");
                    String newPassword = newPasswordText.getText().toString().trim();
                    int passwordLength = newPassword.length();
                    if (passwordLength < MIN_PASSWORD_LENGTH || passwordLength > MAX_PASSWORD_LENGTH) {
                        oldPasswordText.setText("");
                        newPasswordText.setText("");
                        confirmPasswordText.setText("");
                    }
                    else if ((oldPasswordText.getText().toString().equals(oldPassword)) == false) {
                        oldPasswordText.setText("");
                        newPasswordText.setText("");
                        confirmPasswordText.setText("");
                        oldPasswordText.setHint(R.string.reset_password_dialog_password_invalid);
                    }
                    else if ((newPassword.equals(confirmPasswordText.getText().toString())) == false) {
                        oldPasswordText.setText("");
                        newPasswordText.setText("");
                        confirmPasswordText.setText("");
                        confirmPasswordText.setHint(R.string.password_diff_password);
                    }
                    else {
                    	setPasswordDialog.dismiss();
                            mPreferenceEditor.putString(context.getResources().getString(R.string.pref_password), newPassword);
                            mPreferenceEditor.putBoolean(context.getResources().getString(R.string.pref_has_password), true);
                            sendPasswordToMail(context, newPassword);
                        mPreferenceEditor.commit();
                    }
                }
                else {
                    // no old password
                    String newPassword = newPasswordText.getText().toString().trim();
                    String confirmPassword = confirmPasswordText.getText().toString().trim();
                    int passwordLength = newPassword.length();
                    // illegal passowrd length
                    if (passwordLength < MIN_PASSWORD_LENGTH || passwordLength > MAX_PASSWORD_LENGTH) {
                        confirmPasswordText.setText("");
                        newPasswordText.setText("");
                        newPasswordText.setHint(R.string.password_tips);
                        newPasswordText.requestFocus();
                    }
                    else if ((newPassword.equals(confirmPassword)) == false) {
                        confirmPasswordText.setText("");
                        confirmPasswordText.setHint(R.string.password_diff_password);
                        newPasswordText.setText("");
                        newPasswordText.requestFocus();
                    }
                    else if (newPasswordText.getText().toString().equals(confirmPasswordText.getText().toString())) {
                    	setPasswordDialog.dismiss();
                        mPreferenceEditor.putString(context.getResources().getString(R.string.pref_password), newPassword);
                        mPreferenceEditor.putBoolean(context.getResources().getString(R.string.pref_has_password), true);
                        mPreferenceEditor.commit();
                        sendPasswordToMail(context, newPassword);
                        //begin wendy
                        
						mPreferenceEditor.putBoolean(
								context.getResources().getString(R.string.lock_state),
								false);
						mPreferenceEditor.commit();
						NoteBookPickerActivity.updateLockedstate(true);
                        
                        //end wendy
                        }
                    }
                }
        });
        setPasswordDialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	setPasswordDialog.dismiss();
            }
        });

        return setPasswordDialog;
    }
    
	public static boolean isPasswordDialogShowing() {
		return setPasswordDialog != null && setPasswordDialog.isShowing();
	}

    public static void sendPasswordToMail(final Context context, final String newPassword) {
        //using the one_msg_dialog
        final View view = View.inflate(context, R.layout.one_msg_dialog, null);
        final TextView textView = (TextView) view.findViewById(R.id.msg_text_view);
        textView.setText(R.string.password_mail_confirm);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.password_set);
        builder.setView(view);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
					Intent intent = new Intent();
					intent.setType("plain/text");
					intent.setAction(Intent.ACTION_VIEW);//SEND); //[TT330132][Carol]limit to email app

					java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance();
					String date = df.format(new Date(System.currentTimeMillis()));
					String content = String.format(context.getResources().getString(R.string.password_mail_content), date, newPassword);
					Uri data = Uri.parse("mailto:?subject=" + context.getResources().getString(R.string.password_mail_subject) + "&body=" + content);
					intent.setData(data);
					//+++ James, Use the chooser's intent instead of orignal intent
					context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.title_send_email)));
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                //---
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        //begin smilefish fix bug 397251
        dialog.setOnDismissListener(new Dialog.OnDismissListener(){

			@Override
			public void onDismiss(DialogInterface arg0) {
				((NoteBookPickerActivity)context).showHideDialog();
			}
			
        });
        //end smilefish
    }

    // BEGIN: archie_huang@asus.com
    private Dialog setFastInput() { // Better
        View view = View.inflate(this, R.layout.stop_time_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.handwrite_recognition_speed);
        builder.setView(view);
        builder.setNegativeButton(android.R.string.cancel, null);
        final AlertDialog dialog = builder.create();

        return dialog; // Better
    } // END: archie_huang@asus.com

    private Dialog resetIndexLanguage(){//RICHARD
        //using one_input_dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.stop_time_dialog, null);
        builder.setTitle(R.string.set_index_language_title);
        builder.setView(view);
//        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        final AlertDialog dialog = builder.create();
        return dialog; // Better
    }
    
    private Dialog resetPassword() { // Better
        //using one_input_dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.one_input_dialog, null);
        EditText input = (EditText) view.findViewById(R.id.input_edit_text);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setTypeface(Typeface.DEFAULT);
        input.setTransformationMethod(new PasswordTransformationMethod());
        builder.setTitle(R.string.reset_password_dialog_title);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        final AlertDialog dialog = builder.create();
        DialogUtils.forcePopupSoftInput(dialog);//noah
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			 public void onCancel(DialogInterface dialog) {
             	removeDialog(RESET_PASSWORD_DIALOG);
			 }
			});
        return dialog; // Better
    }

    private Dialog resetPasswordConfirm() { // Better
        //using one_msg_dialog
        final View view = View.inflate(this, R.layout.one_msg_dialog, null);
        final TextView textView = (TextView) view.findViewById(R.id.msg_text_view);
        textView.setText(R.string.reset_password_summary);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_reset_dialog_title);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BookCase bookcase = BookCase.getInstance(SettingActivity.this);
                ContentResolver cr = getContentResolver();
                Cursor cursor = cr.query(MetaData.BookTable.uri, null, "is_locked > 0", null, null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Long bookId = cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
                    NoteBook book = bookcase.getNoteBook(bookId);
                    book.setIsLocked(false);
                    cursor.moveToNext();
                }
                cursor.close();//RICHARD FIX MEMORY LEAK
                mPreferenceEditor.putBoolean(mResources.getString(R.string.pref_has_password), false);
                mResetPasswordPreference.setEnabled(false);
                mPreferenceEditor.commit();

                NoteBookPickerActivity.updateLockedstate(true);//Darwin bug 279
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing
            }
        });

        return builder.create(); // Better
    }
    
	//BEGIN: show_wang@asus.com
	//Modified reason: show about dialog
	private Dialog aboutDialog() {
       	AlertDialog.Builder builder = new AlertDialog.Builder(this);
       	View view = View.inflate(this, R.layout.setting_about_dialog, null); //About dialog[Carol]
       	builder.setTitle(R.string.about_title);
       	TextView version = (TextView)view.findViewById(R.id.version);
       	try{
       		PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
       		version.setText(mResources.getString(R.string.version_info) + " " + packageInfo.versionName);
       	}catch(NameNotFoundException e){
       		e.printStackTrace();
       	}
       	TextView legalInfoTitle = (TextView)view.findViewById(R.id.legalInfoTitle);
       	legalInfoTitle.setText(R.string.about_legal_information);
       	TextView legalInfoContent = (TextView)view.findViewById(R.id.legalInfoContent);
       	legalInfoContent.setText(R.string.about_legal_information_des);
       	TextView websiteTitle = (TextView)view.findViewById(R.id.websiteTitle);
       	websiteTitle.setText(R.string.about_website);
       	TextView allright = (TextView)view.findViewById(R.id.allRight);
       	allright.setText(R.string.about_allrights);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, null);
        final AlertDialog dialog = builder.create();
        return dialog;
	}
	//END: show_wang@asus.com
}
