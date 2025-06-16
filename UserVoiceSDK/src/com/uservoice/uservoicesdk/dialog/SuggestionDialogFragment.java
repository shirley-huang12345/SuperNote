package com.uservoice.uservoicesdk.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.activity.ForumActivity;
import com.uservoice.uservoicesdk.activity.InstantAnswersActivity;
import com.uservoice.uservoicesdk.babayaga.Babayaga;
import com.uservoice.uservoicesdk.deflection.Deflection;
import com.uservoice.uservoicesdk.image.ImageCache;
import com.uservoice.uservoicesdk.model.Comment;
import com.uservoice.uservoicesdk.model.Suggestion;
import com.uservoice.uservoicesdk.rest.Callback;
import com.uservoice.uservoicesdk.ui.DefaultCallback;
import com.uservoice.uservoicesdk.ui.PaginatedAdapter;
import com.uservoice.uservoicesdk.ui.PaginationScrollListener;
import com.uservoice.uservoicesdk.ui.Utils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressLint("ValidFragment")
public class SuggestionDialogFragment extends DialogFragmentBugfixed {
    public static final String ARG_SUGGESTION = "suggestion";
    public static final String ARG_DEFLECTING_TYPE = "deflecting_type";
    private Suggestion suggestion;
    private PaginatedAdapter<Comment> adapter;
    private View headerView;
    private View view;
    private Context context;
    private String deflectingType;
    private int conmentCount = -1;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(Session.getInstance().getConfig() == null) dismiss();
        suggestion = getArguments().getParcelable(ARG_SUGGESTION);
        deflectingType = getArguments().getString(ARG_DEFLECTING_TYPE, "Suggestion");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        this.context = getActivity();
        setStyle(STYLE_NO_TITLE, getTheme());
        if (!Utils.isDarkTheme(getActivity())) {
            builder.setInverseBackgroundForced(true);
        }
        view = getActivity().getLayoutInflater().inflate(R.layout.uv_idea_dialog, null);
        headerView = getActivity().getLayoutInflater().inflate(R.layout.uv_idea_dialog_header, null);
        if(suggestion.getNumberOfComments() == 0) conmentCount = 0;
        headerView.findViewById(R.id.uv_subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DefaultCallback<Suggestion> callback = new DefaultCallback<Suggestion>(getActivity()) {
                    @Override
                    public void onModel(Suggestion model) {
                        if (getActivity() instanceof InstantAnswersActivity)
                            Deflection.trackDeflection("subscribed", deflectingType, model);
                        suggestionSubscriptionUpdated(model);
                    }
                };
                if (suggestion.isSubscribed()) {
                    suggestion.unsubscribe(callback);
                } else {
                    SubscribeDialogFragment dialog = new SubscribeDialogFragment(suggestion, SuggestionDialogFragment.this, deflectingType);
                    dialog.show(getFragmentManager(), "SubscribeDialogFragment");

                }
            }
        });
        headerView.findViewById(R.id.uv_post_comment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentDialogFragment dialog = new CommentDialogFragment(suggestion, SuggestionDialogFragment.this);
                dialog.show(getActivity().getSupportFragmentManager(), "CommentDialogFragment");
            }
        });
        ListView listView = (ListView) view.findViewById(R.id.uv_list);
        listView.addHeaderView(headerView);
        displaySuggestion(view, suggestion);
        adapter = getListAdapter();
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setOnScrollListener(new PaginationScrollListener(adapter));
        builder.setView(view);
        builder.setNegativeButton(R.string.uv_close, null);
        Babayaga.track(Babayaga.Event.VIEW_IDEA, suggestion.getId());
        return builder.create();
    }

    public void suggestionSubscriptionUpdated(Suggestion model) {
        if (getActivity() == null)
            return;
        CheckBox checkbox = (CheckBox) headerView.findViewById(R.id.uv_subscribe_checkbox);
        if (suggestion.isSubscribed()) {
            Toast.makeText(context, R.string.uf_sdk_msg_subscribe_success, Toast.LENGTH_SHORT).show();
            checkbox.setChecked(true);
        } else {
            Toast.makeText(context, R.string.uf_sdk_msg_unsubscribe, Toast.LENGTH_SHORT).show();
            checkbox.setChecked(false);
        }
        displaySuggestion(view, suggestion);
        if (getActivity() instanceof ForumActivity)
            ((ForumActivity) getActivity()).suggestionUpdated(model);
    }

    private PaginatedAdapter<Comment> getListAdapter() {
        return new PaginatedAdapter<Comment>(getActivity(), R.layout.uv_comment_item, new ArrayList<Comment>()) {
            @Override
            protected int getTotalNumberOfObjects() {
                return suggestion.getNumberOfComments();
            }

            @Override
            protected void customizeLayout(View view, Comment model) {
                TextView textView = (TextView) view.findViewById(R.id.uv_text);
                textView.setText(model.getText());

                textView = (TextView) view.findViewById(R.id.uv_name);
                textView.setText(model.getUserName());

                textView = (TextView) view.findViewById(R.id.uv_date);
                textView.setText(DateFormat.getDateInstance().format(model.getCreatedAt()));

                ImageView avatar = (ImageView) view.findViewById(R.id.uv_avatar);
                ImageCache.getInstance().loadImage(model.getAvatarUrl(), avatar);
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }

            @Override
            protected void loadPage(int page, Callback<List<Comment>> callback) {
                Comment.loadComments(suggestion, page, callback, new Comment.CountCallback(){
                    @Override
                    public void updateCount(int count) {
                        // TODO Auto-generated method stub
                        conmentCount = count;
                        updateConmentCount(view);
                    }
                });
            }
        };
    }

    private void updateConmentCount(View view){
        if(view != null){
            TextView countView = (TextView)view.findViewById(R.id.uv_comment_count);
            View loading = view.findViewById(R.id.uf_sdk_progress);
            if(countView != null){
                if(conmentCount >= 0){
                    countView.setText(Utils.getQuantityString(view, R.plurals.uv_comments, conmentCount).toUpperCase(Locale.getDefault()));
                    countView.setVisibility(View.VISIBLE);
                }else{
                    countView.setVisibility(View.GONE);
                }
            }
            if(loading != null){
                if(conmentCount < 0){
                    loading.setVisibility(View.VISIBLE);
                }else{
                    loading.setVisibility(View.GONE);
                }
            }
        }
    }

    public void commentPosted(Comment comment) {
        try{
            adapter.add(0, comment);
            suggestion.commentPosted(comment);
            conmentCount++;
            displaySuggestion(view, suggestion);
        }catch(Exception e){

        }
    }

    private void displaySuggestion(View view, Suggestion suggestion) {
        TextView status = (TextView) view.findViewById(R.id.uv_status);
        TextView responseStatus = (TextView) view.findViewById(R.id.uv_response_status);
        View responseDivider = view.findViewById(R.id.uv_response_divider);
        TextView title = (TextView) view.findViewById(R.id.uv_title);

        if (suggestion.isSubscribed()) {
            ((CheckBox) view.findViewById(R.id.uv_subscribe_checkbox)).setChecked(true);
        }

        if (suggestion.getStatus() == null) {
            status.setVisibility(View.GONE);
            int defaultColor = Color.DKGRAY;
            responseStatus.setTextColor(defaultColor);
            responseDivider.setBackgroundColor(defaultColor);
        } else {
            int color = Color.parseColor(suggestion.getStatusColor());
            status.setBackgroundColor(color);
            status.setText(suggestion.getStatus());
            responseStatus.setTextColor(color);
            responseStatus.setText(String.format(getString(R.string.uv_admin_response_format), suggestion.getStatus().toUpperCase(Locale.getDefault())));
            responseDivider.setBackgroundColor(color);
        }

        title.setText(suggestion.getTitle());
        ((TextView) view.findViewById(R.id.uv_text)).setText(suggestion.getText());
        ((TextView) view.findViewById(R.id.uv_creator)).setText(String.format(view.getContext().getString(R.string.uv_posted_by_format), suggestion.getCreatorName(), DateFormat.getDateInstance().format(suggestion.getCreatedAt())));

        if (suggestion.getAdminResponseText() == null) {
            view.findViewById(R.id.uv_admin_response).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.uv_admin_response).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.uv_admin_name)).setText(suggestion.getAdminResponseUserName());
            ((TextView) view.findViewById(R.id.uv_response_date)).setText(DateFormat.getDateInstance().format(suggestion.getAdminResponseCreatedAt()));
            ((TextView) view.findViewById(R.id.uv_response_text)).setText(suggestion.getAdminResponseText());
            ImageView avatar = (ImageView) view.findViewById(R.id.uv_admin_avatar);
            ImageCache.getInstance().loadImage(suggestion.getAdminResponseAvatarUrl(), avatar);
        }

        //((TextView) view.findViewById(R.id.uv_comment_count)).setText(Utils.getQuantityString(view, R.plurals.uv_comments, suggestion.getNumberOfComments()).toUpperCase(Locale.getDefault()));
        updateConmentCount(view);
        if (Session.getInstance().getClientConfig() != null && Session.getInstance().getClientConfig().shouldDisplaySuggestionsByRank()) {
            ((TextView) view.findViewById(R.id.uv_subscriber_count)).setText(String.format(view.getContext().getResources().getString(R.string.uv_ranked), suggestion.getRankString()));
        } else {
            ((TextView) view.findViewById(R.id.uv_subscriber_count)).setText(String.format(view.getContext().getResources().getQuantityString(R.plurals.uf_sdk_number_of_subscribers_format, suggestion.getNumberOfSubscribers()),
                  Utils.getQuantityString(view, R.plurals.uv_subscribers, suggestion.getNumberOfSubscribers())));
        }
    }

}
