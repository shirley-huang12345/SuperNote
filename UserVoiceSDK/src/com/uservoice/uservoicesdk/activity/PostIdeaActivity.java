package com.uservoice.uservoicesdk.activity;

import java.util.regex.Pattern;

import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.UserVoice;
import com.uservoice.uservoicesdk.babayaga.Babayaga;
import com.uservoice.uservoicesdk.babayaga.Babayaga.Event;
import com.uservoice.uservoicesdk.flow.InitManager;
import com.uservoice.uservoicesdk.flow.SigninCallback;
import com.uservoice.uservoicesdk.flow.SigninManager;
import com.uservoice.uservoicesdk.model.Category;
import com.uservoice.uservoicesdk.model.Suggestion;
import com.uservoice.uservoicesdk.rest.RestResult;
import com.uservoice.uservoicesdk.ui.DefaultCallback;
import com.uservoice.uservoicesdk.ui.SpinnerAdapter;
import com.uservoice.uservoicesdk.ui.Utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.uservoice.uservoicesdk.model.Forum;

public class PostIdeaActivity extends FragmentActivity {

    private EditText emailField, nameField, titleField, descriptionField;
    private Spinner categorySelect;

    private Pattern emailFormat = Pattern
            .compile("\\A(\\w[-+.\\w!\\#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\|\\}~]*@([-\\w]*\\.)+[a-zA-Z]{2,9})\\z");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Utils.isSimilarToWhite(UserVoice.sColor))setTheme(R.style.UserVoiceSDKTheme_light);
        else setTheme(R.style.UserVoiceSDKTheme);
        super.onCreate(savedInstanceState);
        if(Session.getInstance().getConfig() == null){
            finish();
            return;
        }
        setTitle(R.string.uf_sdk_topic_form_title);
        setContentView(R.layout.uf_sdk_activity_post_idea);
        setupActionBar();
        setupBackground();
        setupView();

        new InitManager(this, new Runnable() {
            @Override
            public void run() {
                setupData();
            }
        }).init();
    }

    private void setupData() {
        if(Session.getInstance().getForum() != null){
            if (Session.getInstance().getForum().getCategories().size() > 0) {
                findViewById(R.id.categoryArea).setVisibility(View.VISIBLE);
                categorySelect = (Spinner) findViewById(R.id.category);
                categorySelect.setAdapter(new SpinnerAdapter<Category>(this,
                Session.getInstance().getForum().getCategories()));
            }
        }else{
            Forum.loadForum(Session.getInstance().getConfig().getForumId(), new DefaultCallback<Forum>(this) {
            @Override
            public void onModel(Forum model) {
                Session.getInstance().setForum(model);
                if (Session.getInstance().getForum().getCategories().size() > 0) {
                    findViewById(R.id.categoryArea).setVisibility(View.VISIBLE);
                    categorySelect = (Spinner) findViewById(R.id.category);
                    categorySelect.setAdapter(new SpinnerAdapter<Category>(PostIdeaActivity.this,
                    Session.getInstance().getForum().getCategories()));
                }
            }
        });
        }
        setupDefaultEmailName();
    }

    private void setupView() {
        emailField = (EditText) findViewById(R.id.email_address);
        nameField = (EditText) findViewById(R.id.name);
        titleField = (EditText) findViewById(R.id.topic_text);
        descriptionField = (EditText) findViewById(R.id.topic_description);


    }

    private void setupDefaultEmailName() {
        if (Session.getInstance().getUser() != null) {

            if (!TextUtils.isEmpty(Session.getInstance().getUser().getEmail())) {
                emailField.setText(Session.getInstance().getUser().getEmail());
            }

            if (!TextUtils.isEmpty(Session.getInstance().getUser().getName())) {
                nameField.setText(Session.getInstance().getUser().getName());
            }
        } else {
            if (!TextUtils.isEmpty(Session.getInstance().getEmail())) {
                emailField.setText(Session.getInstance().getEmail());
            }

            if (!TextUtils.isEmpty(Session.getInstance().getName())) {
                nameField.setText(Session.getInstance().getName());
            }
        }

    }

    private void setupBackground() {
        if (getResources().getIdentifier("windowTranslucentStatus", "attr",
                "android") != 0) {
            if(!Utils.isSimilarToWhite(UserVoice.sColor))
                findViewById(R.id.background).setBackgroundColor(UserVoice.sColor);
            else
                findViewById(R.id.background).setBackgroundColor(Color.BLACK);
            getActionBar().setBackgroundDrawable(
                    new ColorDrawable(UserVoice.sColor));
        } else {
            getActionBar().setBackgroundDrawable(
                    new ColorDrawable(UserVoice.sColor));
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
        if (TextUtils.isEmpty(titleField.getText().toString())) {
            super.onBackPressed();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.uv_confirm);
            builder.setMessage(R.string.uf_sdk_msg_confirm_discard_topic);
            builder.setPositiveButton(R.string.uv_yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            builder.setNegativeButton(R.string.uv_no, null);
            builder.show();
        }
    }

    public void doSubmit(View view) {

        String email = emailField.getText().toString();

        String title = titleField.getText().toString();

        if (TextUtils.isEmpty(email)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setTitle(R.string.uf_sdk_warning);
            builder.setMessage(R.string.uv_msg_user_identity_validation);
            builder.create().show();
            return;
        }

        if (!emailFormat.matcher(email).matches()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setTitle(R.string.uf_sdk_warning);
            builder.setMessage(R.string.uf_sdk_msg_bad_email_format);
            builder.create().show();
            return;
        }

        if (TextUtils.isEmpty(title)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setTitle(R.string.uf_sdk_warning);
            builder.setMessage(R.string.uv_msg_custom_fields_validation);
            builder.create().show();
            return;
        }
        setSubmitProgressBarVisible(true);
        hideKeyboard();
        SigninManager.signIn(PostIdeaActivity.this, emailField.getText()
                .toString(), nameField.getText().toString(),
                new SigninCallback() {
                    @Override
                    public void onSuccess() {
                        Category category = categorySelect == null ? null
                                : (Category) categorySelect.getSelectedItem();
                        Suggestion.createSuggestion(Session.getInstance()
                                .getForum(), category, titleField.getText()
                                .toString(), descriptionField.getText()
                                .toString(), 1,
                                new DefaultCallback<Suggestion>(
                                        PostIdeaActivity.this) {
                                    @Override
                                    public void onModel(Suggestion model) {
                                        Babayaga.track(Event.SUBMIT_IDEA);
                                        Toast.makeText(
                                                PostIdeaActivity.this,
                                                R.string.uf_sdk_msg_topic_created,
                                                Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        finish();
                                    }

                                    @Override
                                    public void onError(RestResult error) {
                                        setSubmitProgressBarVisible(false);
                                        super.onError(error);
                                    }
                                });
                    }
                });
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
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
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
}
