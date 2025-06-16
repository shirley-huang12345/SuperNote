package com.asus.supernote.sync;


import com.asus.supernote.R;
import com.asus.supernote.SettingActivity;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class SyncSetprivatePassword extends Activity{
    private SharedPreferences mPreference;
    private SharedPreferences.Editor mPreferenceEditor;
    private Resources mResources;
    private boolean mSetUpShow;//emmanual
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	 super.onCreate(savedInstanceState);
    	  mPreference = getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
          mPreferenceEditor = mPreference.edit();
          mResources = getResources();         
          //emmanual to fix bug 420250
          mSetUpShow = false;
          if(savedInstanceState != null){
        	  mSetUpShow = savedInstanceState.getBoolean("setup_is_showing");
          }
		// BEGIN: Better
		if (MetaData.AppContext == null) {
			MetaData.AppContext = getApplicationContext();
		}
		// END: Better
    	  
    	  if (mPreference.getBoolean(mResources.getString(R.string.pref_has_password), false) == false) {
    		  if(mSetUpShow){
    			  setupPassword();
    		  }else{
              final View view = View.inflate(this, R.layout.one_msg_dialog, null);
              final TextView textView = (TextView) view.findViewById(R.id.msg_text_view);
              String sendtintent = SyncSetprivatePassword.this.getIntent().getStringExtra("sendintent");
              if ( sendtintent != null && sendtintent.equals("NoteBookActivity")) {
            	  textView.setText(R.string.password_not_set_import);
              } else {
            	  textView.setText(R.string.password_not_set_sync);
              }
              AlertDialog.Builder builder = new AlertDialog.Builder(this);
              builder.setTitle(R.string.nb_hide);
              builder.setView(view);
              builder.setPositiveButton(R.string.set_password_dialog_title, null);
              builder.setNegativeButton(android.R.string.cancel, null);
              final AlertDialog dialog = builder.create();
              dialog.show();
              dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      dialog.dismiss();
                      setupPassword();
                  }
              });
              dialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      dialog.dismiss();
                      SyncSetprivatePassword.this.finish();
                      setBookLocked(false);
                      
                      //Begin:show_wang@asus.com
                      //Modified reason: setpassword if the import notebook is lucked when there is no password
                      String sendtintent = SyncSetprivatePassword.this.getIntent().getStringExtra("sendintent");
                      if ( sendtintent != null && sendtintent.equals("NoteBookActivity")) {
                    	  BookCase bookcase = BookCase.getInstance(SyncSetprivatePassword.this);
		              	  bookcase.getImportedNoteBook().setIsLocked(false);
                      }
                      //End: show_wang@asus.com   
              		
                  }
              }); 
    		  }
    	  }
    }
    
    private void setupPassword() {
        View view = View.inflate(this, R.layout.password_dialog, null);
        final View currPasswordGroup = view.findViewById(R.id.currPasswordGroup);
        final EditText oldPasswordText = (EditText) view.findViewById(R.id.oldPassword);
        final EditText newPasswordText = (EditText) view.findViewById(R.id.password);
        final EditText confirmPasswordText = (EditText) view.findViewById(R.id.password2);
        oldPasswordText.setTypeface(Typeface.DEFAULT);
        newPasswordText.setTypeface(Typeface.DEFAULT);
        confirmPasswordText.setTypeface(Typeface.DEFAULT);
        oldPasswordText.setTransformationMethod(new PasswordTransformationMethod());
        newPasswordText.setTransformationMethod(new PasswordTransformationMethod());
        confirmPasswordText.setTransformationMethod(new PasswordTransformationMethod());
        if (mPreference.getBoolean(getResources().getString(R.string.pref_has_password), false) == false) {
            oldPasswordText.setEnabled(false);
        }
        boolean hasPassword = mPreference.getBoolean(getResources().getString(R.string.pref_has_password), false);
        currPasswordGroup.setVisibility(hasPassword ? View.VISIBLE : View.GONE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.password);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        mSetUpShow = true;
        dialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mSetUpShow = false;
                SyncSetprivatePassword.this.finish();
                setBookLocked(false);
            }
        }); 
        dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // no old password
                String newPassword = newPasswordText.getText().toString();
                String confirmPassword = confirmPasswordText.getText().toString();
                int passwordLength = newPassword.length();
                // illegal passowrd length
                if (passwordLength < SettingActivity.MIN_PASSWORD_LENGTH || passwordLength > SettingActivity.MAX_PASSWORD_LENGTH) {
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
                    dialog.dismiss(); 
                    mSetUpShow = false;
                    mPreferenceEditor.putString(getResources().getString(R.string.pref_password), newPasswordText.getText().toString());
                    mPreferenceEditor.putBoolean(getResources().getString(R.string.pref_has_password), true);
                    mPreferenceEditor.commit();
                    SettingActivity.sendPasswordToMail(SyncSetprivatePassword.this, newPasswordText.getText().toString());
                    setBookLocked(true);
                    SyncSetprivatePassword.this.finish();
                }
            }
        });
    }
    private void setBookLocked(boolean locked)
    {
    	BookCase bookcase = BookCase.getInstance(this);
    	
    	for(Long bookid:MetaData.lockBookIdList)
    	{
    		NoteBook notebook = bookcase.getNoteBook(bookid);
    		if (notebook != null) {
    			notebook.setIsLocked(locked);
    		}
    	}
    }

	@Override
    protected void onSaveInstanceState(Bundle outState) {
		//emmanual to fix bug 420250
		outState.putBoolean("setup_is_showing", mSetUpShow);
	    super.onSaveInstanceState(outState);
    }
}
