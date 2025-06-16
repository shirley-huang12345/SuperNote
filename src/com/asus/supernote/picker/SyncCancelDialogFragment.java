package com.asus.supernote.picker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import com.asus.supernote.R;

public class SyncCancelDialogFragment extends DialogFragment{ //Carol-a dialog for confirm if canceling sync
	
	public static final String TAG = "SyncCancelDialogFragment";
	private ListView mSyncCancelLV;
	private CheckBox mSyncCancelHintCB;
	private SharedPreferences mPreference;
	private Resources mResources;
	private static final String PREFERNECE_NAME = "SuperNote";
	private ArrayAdapter<String> adpter;
	private SyncCancelDialog syncCancelDialogListener = null;
	private boolean mShowHint = true;
	
	public interface SyncCancelDialog{
		public void onPressPositive();
		public void onPressNegative();
		public String[] onAdapterGetter();
	}
	
	public void setListener(SyncCancelDialog listener)
	{
		syncCancelDialogListener = listener;
	}
	
	static SyncCancelDialogFragment newInstance(Bundle b){
		SyncCancelDialogFragment f = new SyncCancelDialogFragment();
        f.setCancelable(false);
        return f;
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		mResources = getResources();
		View view = View.inflate(getActivity(),R.layout.sync_cancel_confirmdialog, null);
		mSyncCancelHintCB = (CheckBox)view.findViewById(R.id.syncCancel_hideHint_checkbox);
		mSyncCancelLV = (ListView)view.findViewById(R.id.syncCancelbook_listview);  //list view of notebook
		//emmanual to fix bug 465474
		if(syncCancelDialogListener == null && getActivity() instanceof SyncCancelDialog){
			syncCancelDialogListener = (SyncCancelDialog) getActivity();
		}
		adpter = new ArrayAdapter<String>(getActivity(),R.layout.sync_notebook_name, syncCancelDialogListener.onAdapterGetter());
		mSyncCancelLV.setAdapter(adpter);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.sync_cancel_confirmText)
		 	   .setView(view)
		 	   .setCancelable(false)
		       .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
		    	   @Override
					public void onClick(DialogInterface dialog, int whichButton){
						if(mShowHint && mSyncCancelHintCB.isChecked())
						{
							//save the value of checkbox
							mPreference = getActivity().getSharedPreferences(PREFERNECE_NAME, Activity.MODE_PRIVATE);
							SharedPreferences.Editor editor = mPreference.edit();
							editor.putBoolean(mResources.getString(R.string.pref_hide_syncpompt), !mSyncCancelHintCB.isChecked());
							editor.commit();
							mShowHint = false;
						}
						syncCancelDialogListener.onPressPositive();
					}
				})
		       .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int whichButton){
						// cancel the dialog
						syncCancelDialogListener.onPressNegative();
					}
				});
		return builder.create();
	}
}
