package com.asus.supernote.ratingus;

import com.asus.supernote.data.MetaData;
import com.asus.supernote.ga.GACollector;

import com.asus.supernote.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class SuperNoteDialogFragment extends DialogFragment{
    public final static String TAG = "SuperNoteDialogFragment";

    public static enum DIALOG_TYPE {
        ENCOURAGE_US
    }

    public interface DialogEventListener {
        public void onDialogDismissed();
    }

    public static SuperNoteDialogFragment newDialog(DIALOG_TYPE type) {
    	SuperNoteDialogFragment frag = new SuperNoteDialogFragment();
        Bundle args = new Bundle();
        args.putInt(DIALOG_TYPE.class.getSimpleName(), type.ordinal());
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        int typeIndex = getArguments()
                .getInt(DIALOG_TYPE.class.getSimpleName());
        DIALOG_TYPE type = DIALOG_TYPE.values()[typeIndex];
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
                getActivity());
        switch (type) {
            case ENCOURAGE_US:
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                View mRootView = layoutInflater.inflate(R.layout.encourage_us_dialog,
                        null);
                TextView mTextViewDescription = (TextView) mRootView
                        .findViewById(R.id.encourage_us_description);
                String text = String.format(
                        getString(R.string.settings_encourage_us_detail),
                        getString(R.string.app_name));
                mTextViewDescription.setText(text);

                dialogBuilder
                        .setTitle(getString(R.string.settings_encourage_us_title))
                        .setPositiveButton(R.string.toolbar_encourage_dialog_rate_now,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub
                                        Log.i(TAG, "Click rate us in encourage us dialog");
                                        if(MetaData.IS_GA_ON)
                        				{
                        					GACollector gaCollector = new GACollector(getActivity());
                        					gaCollector.encourageUs("RateNow");
                        				}
                                        RatingUsUtil.rateUs(getActivity(), true);
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        // TODO Auto-generated method stub
                                        Log.i(TAG, "Click cancel in encourage us dialog");
                                        if(MetaData.IS_GA_ON)
                        				{
                        					GACollector gaCollector = new GACollector(getActivity());
                        					gaCollector.encourageUs("NotRate");
                        				}
                                    }
                                })
                        .setView(mRootView).create();
                break;
            default:
                break;
        }

        return dialogBuilder.create();
    }
}
