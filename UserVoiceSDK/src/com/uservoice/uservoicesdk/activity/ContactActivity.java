package com.uservoice.uservoicesdk.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.UserVoice;
import com.uservoice.uservoicesdk.babayaga.Babayaga;
import com.uservoice.uservoicesdk.babayaga.Babayaga.Event;
import com.uservoice.uservoicesdk.flow.InitManager;
import com.uservoice.uservoicesdk.model.Attachment;
import com.uservoice.uservoicesdk.model.CustomField;
import com.uservoice.uservoicesdk.model.Ticket;
import com.uservoice.uservoicesdk.rest.RestResult;
import com.uservoice.uservoicesdk.ui.DefaultCallback;
import com.uservoice.uservoicesdk.ui.SpinnerAdapter;
import com.uservoice.uservoicesdk.ui.Utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ContactActivity extends FragmentActivity implements OnClickListener{
    public static final String KEY_BUG_REPORT = "bug_report";
    private EditText textField, emailField, nameField;
    private ViewGroup CustomFieldArea;
    private static final String TAG = "ContactActivity";
    private Pattern emailFormat = Pattern.compile("\\A(\\w[-+.\\w!\\#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\|\\}~]*@([-\\w]*\\.)+[a-zA-Z]{2,9})\\z");
    private Map<String, String> customFieldValues;
    private boolean mBugDefault;
    private static final int MAX_LOG_SIZE = 2*1024*1024;
    private View mQuickBug;
    private ArrayList<String> mBugs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Utils.isSimilarToWhite(UserVoice.sColor))setTheme(R.style.UserVoiceSDKTheme_light);
        else setTheme(R.style.UserVoiceSDKTheme);
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mBugDefault = intent.getBooleanExtra(KEY_BUG_REPORT, false);
        if(Session.getInstance().getConfig() == null){
            finish();
            return;
        }
        Log.d("NPECHECKING", "onCreate, instance is " + this);
        setTitle(R.string.uf_sdk_send_feedback);
        setContentView(R.layout.uf_sdk_activity_contact);
        mQuickBug = findViewById(R.id.quick_bug);
        mQuickBug.setOnClickListener(this);
        mBugs = new ArrayList<String>();
        mBugs.add("Bug 1");
        mBugs.add("Bug 2");
        mQuickBug.setVisibility(View.GONE);
        setupActionBar();
        setupBackground();

        setupView();

        setSubmitProgressBarVisible(true);
        new InitManager(this, new Runnable() {
            @Override
            public void run() {
                setupData();
            }
        },
        new Runnable() {
            @Override
            public void run() {
                setSubmitProgressBarVisible(false);
            }
        }).init();
    }

    private void setupData() {
        customFieldValues = new HashMap<String, String>(Session.getInstance().getConfig().getCustomFields());

        setupCustomFeild();

        setupDefaultEmailName();

        setSubmitProgressBarVisible(false);
    }

    private void setupView() {
        textField = (EditText) findViewById(R.id.contact_text);
        emailField = (EditText) findViewById(R.id.email_address);
        nameField = (EditText) findViewById(R.id.name);
        CustomFieldArea = (ViewGroup) findViewById(R.id.custom_feild_area);
    }

    private void setupCustomFeild() {
        for (CustomField customField : Session.getInstance().getClientConfig().getCustomFields()) {
            if (customField.isPredefined()) {
                addPredefinedFeild(customField);
            } else {
                addTextFeild(customField);
            }
        }
    }

    private void setupDefaultEmailName() {
        if (!TextUtils.isEmpty(Session.getInstance().getEmail())) {
            emailField.setText(Session.getInstance().getEmail());
        }

        if (!TextUtils.isEmpty(Session.getInstance().getName())) {
            nameField.setText(Session.getInstance().getName());
        }
    }

    private void addPredefinedFeild(final CustomField customField) {
        View view = getLayoutInflater().inflate(R.layout.uv_select_field_item, CustomFieldArea, true);

        String value = customFieldValues.get(customField.getName());
        TextView title = (TextView) view.findViewById(R.id.uv_header_text);
        int resId = getResources().getIdentifier(CustomField.PREFIX + customField.getId(), "string", getPackageName());
        String titleText = resId == 0? customField.getName(): getString(resId);
        title.setText(titleText);
        Spinner field = (Spinner) view.findViewById(R.id.uv_select_field);
        field.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                customFieldValues.put(customField.getName(), customField.getPredefinedValues().get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        field.setAdapter(new SpinnerAdapter<String>(ContactActivity.this, customField.getPredefinedValues(), customField.getPredefinedIDs()));
        if(field.getAdapter().getCount() > 0){
            field.setSelection(0);
        }
        if (value != null && customField.getPredefinedValues().contains(value)) {
            field.setSelection(customField.getPredefinedValues().indexOf(value) + 1);
        }

        if(mBugDefault && customField.getId() == 117562){
            List<Integer> ids = customField.getPredefinedIDs();
            int idx = -1;
            for(int i = 0; i < ids.size(); i++){
                if(ids.get(i).intValue() == 2529543) idx = i;
            }
            if(idx != -1) field.setSelection(idx);
        }
    }

    private void addTextFeild(final CustomField customField) {
        View view = getLayoutInflater().inflate(R.layout.uv_text_field_item, CustomFieldArea, true);

        TextView title = (TextView) view.findViewById(R.id.uv_header_text);
        final EditText field = (EditText) view.findViewById(R.id.uv_text_field);
        String value = customFieldValues.get(customField.getName());
        title.setText(customField.getName());
        field.setHint(R.string.uv_value);
        field.setInputType(EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
        field.setText(value);
        field.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    customFieldValues.put(customField.getName(), field.getText().toString());
                }
            }
        });
    }

    private void setupBackground() {
        if (getResources().getIdentifier("windowTranslucentStatus", "attr", "android") != 0) {
            if(!Utils.isSimilarToWhite(UserVoice.sColor))
                findViewById(R.id.background).setBackgroundColor(UserVoice.sColor);
            else
            	findViewById(R.id.background).setBackgroundColor(Color.BLACK);
            getActionBar().setBackgroundDrawable(new ColorDrawable(UserVoice.sColor));
        } else {
            getActionBar().setBackgroundDrawable(new ColorDrawable(UserVoice.sColor));
        }
        if(Build.VERSION.SDK_INT >= 21){
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(255, 254, 254, 254)));
        }
    }

    private void setupActionBar() {
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayUseLogoEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (TextUtils.isEmpty(textField.getText().toString())) {
            super.onBackPressed();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.uv_confirm);
            builder.setMessage(R.string.uf_sdk_msg_confirm_discard_topic);
            builder.setPositiveButton(R.string.uv_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setNegativeButton(R.string.uv_no, null);
            builder.show();
        }
    }

    private boolean validateCustomFields() {
        for (CustomField field : Session.getInstance().getClientConfig().getCustomFields()) {
            if (field.isRequired()) {
                String string = customFieldValues.get(field.getName());
                if (string == null || string.length() == 0)
                    return false;
            }
        }
        return true;
    }

    public void doSubmit(View view) {

        String email = emailField.getText().toString();

        String text = textField.getText().toString();

        if (TextUtils.isEmpty(email)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setTitle(R.string.uf_sdk_warning);
            builder.setMessage(R.string.uv_msg_user_identity_validation);
            builder.create().show();
            return;
        }
        Session.getInstance().persistIdentity(nameField.getText().toString(), email);

        if (!emailFormat.matcher(email).matches()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setTitle(R.string.uf_sdk_warning);
            builder.setMessage(R.string.uf_sdk_msg_bad_email_format);
            builder.create().show();
            return;
        }

        if (TextUtils.isEmpty(text)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setTitle(R.string.uf_sdk_warning);
            builder.setMessage(R.string.uv_msg_custom_fields_validation);
            builder.create().show();
            return;
        }
        if (Session.getInstance().getClientConfig() != null && validateCustomFields()) {
            setSubmitProgressBarVisible(true);
            textField.setFocusable(false);
            emailField.setFocusable(false);
            hideKeyboard();
            Map<String, String> additionalCustomFields = Session.getInstance().getConfig().getAdditionalCustomFields();
            if (additionalCustomFields != null && additionalCustomFields.size() > 0) {// Ed+++
                Set<String> set = additionalCustomFields.keySet();
                Iterator<String> it = set.iterator();
                while (it.hasNext()) {
                    String key = it.next().toString();
                    String value = additionalCustomFields.get(key);
                    customFieldValues.put(key, value);
                }
            }

            List<Attachment> attachments = new ArrayList<Attachment>();
            Set<String> set = customFieldValues.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next().toString();
                if(key.equalsIgnoreCase("Feedback type")){
                    if(customFieldValues.get(key).equalsIgnoreCase("Bug report")){
                        attachments = tryGetLog();
                    }
                }
            }

            Ticket.createTicket(textField.getText().toString(), emailField.getText().toString(), nameField.getText().toString(), customFieldValues, attachments, new DefaultCallback<Ticket>(ContactActivity.this) {
                @Override
                public void onModel(Ticket model) {
                    Babayaga.track(Event.SUBMIT_TICKET);
                    Toast.makeText(ContactActivity.this, R.string.uf_sdk_msg_ticket_created, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(RestResult error) {
                    super.onError(error);
                    setSubmitProgressBarVisible(false);
                    emailField.setFocusable(true);
                    textField.setFocusable(true);
                }
            });
        } else {
            Toast.makeText(ContactActivity.this, R.string.uv_msg_custom_fields_validation, Toast.LENGTH_SHORT).show();
        }
    }

    private void setSubmitProgressBarVisible(boolean visible) {
        if (visible) {
            findViewById(R.id.submit_progressBar).setVisibility(View.VISIBLE);
            findViewById(R.id.submit_button).setVisibility(View.GONE);
        } else {
            findViewById(R.id.submit_progressBar).setVisibility(View.GONE);
            findViewById(R.id.submit_button).setVisibility(View.VISIBLE);
        }
    }

    private void hideKeyboard() {
        if (this.getCurrentFocus() != null) {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("NPECHECKING", "onDestroy, instance is " + this);
    }

    private List<Attachment> tryGetLog() {
        List<Attachment> attachments = new ArrayList<Attachment>();
        Config config = Session.getInstance().getConfig();
        String path = config == null? null: config.getAttachmentPath();
        if(path == null) return attachments;
        File logFile = new File(path);

        if (logFile.exists() && logFile.length() < MAX_LOG_SIZE) {
            String fileBase64 = encodeFileToBase64(logFile);
            if (!TextUtils.isEmpty(fileBase64)) {
                Attachment attachment = new Attachment("attachment", "text/plain", fileBase64);
                attachments.add(attachment);
            }
        }

        return attachments;
    }

    private String encodeFileToBase64(File file) {
        try {
            FileInputStream in = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length() + 100];
            int length = in.read(buffer);
            return Base64.encodeToString(buffer, 0, length, Base64.DEFAULT);
        } catch (FileNotFoundException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

        return null;
    }
/*
    @Override
    public void setTitle(CharSequence title) {
    	if(Build.VERSION.SDK_INT >= 21 && !(Utils.isSimilarToWhite(UserVoice.sColor)))
            super.setTitle(Html.fromHtml("<font color = '" + String.format("#%06X", 0xFFFFFF & UserVoice.sColor) + "'>" + title.toString() + "</font>"));
        else
            super.setTitle(title);
    }
*/
    @Override
    public void onClick(View v) {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        for(String bug: mBugs){
            arrayAdapter.add(bug);
        }
        build.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	textField.getText().insert(0, mBugs.get(which) + "\n\n");
                    }
                });
        build.show();
    }

}
