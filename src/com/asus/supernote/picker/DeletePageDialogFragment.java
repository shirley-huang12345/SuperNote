package com.asus.supernote.picker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.asus.supernote.R;

public class DeletePageDialogFragment extends DialogFragment {
    public static final String TAG = "DeletePageDialogFragment";
    public static final int DELETE_CONFIRM_DIALOG = 1;
    public static final int DELETE_PROGRESS_DIALOG = 2;
    public static final int DELETE_SUCCESS_DIALOG = 3;
    public static final int DELETE_FAIL_DIALOG = 4;
    public static int mStyle = 1;

    public static DeletePagesTask mDeletePagesTask = null;
    static DeletePageDialogFragment newInstance(Bundle b,DeletePagesTask dpt) {
    	mDeletePagesTask = dpt;
        DeletePageDialogFragment f = new DeletePageDialogFragment();
        f.setArguments(b);
        f.setCancelable(false);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mStyle = getArguments().getInt("style", DELETE_CONFIRM_DIALOG);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        switch (mStyle) {
            case DELETE_CONFIRM_DIALOG: {
                final View view = View.inflate(getActivity(), R.layout.one_msg_dialog, null);
                final TextView textView = (TextView) view.findViewById(R.id.msg_text_view);
                textView.setText(R.string.pg_del);
                builder.setCancelable(false);
                builder.setTitle(R.string.del);
                builder.setView(view);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	//begin wendy                    
                        Activity activity = getActivity();
                        if(activity instanceof PickerActivity)
                        {
                        	((PickerActivity)activity).executeDeletePages();
                        }
                        if(activity instanceof NoteBookPickerActivity)
                        {
                        	((NoteBookPickerActivity)activity).executeDeletePages();
                        }
                        //end wendy
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                return builder.create();
            }
            case DELETE_PROGRESS_DIALOG: {
                ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setCancelable(false);
                dialog.setTitle(R.string.pg_del);
                dialog.setMax(getArguments().getInt("max", 1));
                //begin wendy
                Activity activity = getActivity();
                if(activity instanceof PickerActivity)
                {
                	((PickerActivity)activity).executeDeletePages();
                }
                if(activity instanceof NoteBookPickerActivity)
                {
                	((NoteBookPickerActivity)activity).executeDeletePages();
                }
                //end wendy
                if(mDeletePagesTask != null)
                {
                	mDeletePagesTask.setDialog(dialog);
                }
                return dialog;
            }
            case DELETE_SUCCESS_DIALOG:
                break;
            case DELETE_FAIL_DIALOG:
                break;

        }
        return super.onCreateDialog(savedInstanceState);
    }

}
