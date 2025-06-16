package com.uservoice.uservoicesdk.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.activity.ArticleActivity;
import com.uservoice.uservoicesdk.babayaga.Babayaga;
import com.uservoice.uservoicesdk.dialog.UnhelpfulDialogFragment;
import com.uservoice.uservoicesdk.ga.GAManager;
import com.uservoice.uservoicesdk.model.Article;
import com.uservoice.uservoicesdk.ui.Utils;

public class ArticleFragment extends BaseFragment {
    private static final String KEY_INDEX = "key_index";
    private Article article;

    private WebView articleContainer;

    private View articleHelpful, articleNotHelpful;
    private int mIdx;

    public static ArticleFragment getInstance(Article article, int idx) {
        ArticleFragment articleFragment = new ArticleFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Article.class.getName(), article);
        bundle.putInt(KEY_INDEX, idx);
        articleFragment.setArguments(bundle);
        return articleFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private final static String temp = "\u771f\u662f\u500b\u58de\u5973\u5b69\u2229__\u2229y \u54c8\u54c8\u54c8\u54c8~~";
    private int counter;

    @Override
    protected void init() {
        article = getArguments().getParcelable(Article.class.getName());
        mIdx = getArguments().getInt(KEY_INDEX);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (articleContainer != null) articleContainer.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (articleContainer != null) articleContainer.onResume();
    }

    @Override
    protected void setupView(View rootView) {
        articleHelpful = findViewById(R.id.uv_helpful_button);
        articleNotHelpful = findViewById(R.id.uv_unhelpful_button);

        articleContainer = new WebView(getActivity());
        ((LinearLayout)findViewById(R.id.uv_container)).addView(articleContainer);
        displayArticle(articleContainer, article);
    }

    @Override
    protected void setupAdapter() {

    }

    @Override
    protected void setupEvent() {
        articleHelpful.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Babayaga.track(Babayaga.Event.VOTE_ARTICLE, article.getId());
                showToast(R.string.uv_thanks);
                setRead();
                //doAnimation(findViewById(R.id.uv_helpful_section));
                findViewById(R.id.uv_helpful_section).setVisibility(View.GONE);
                GAManager.FAQ.ClickYes(Session.getInstance().getContext(), article.getId());
            }
        });

        articleNotHelpful.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!Session.getInstance().getConfig().isAssistance() && Session.getInstance().getConfig().shouldShowContactUs()) showContactDialog();
                setRead();
                //doAnimation(findViewById(R.id.uv_helpful_section));
                findViewById(R.id.uv_helpful_section).setVisibility(View.GONE);
                GAManager.FAQ.ClickNo(Session.getInstance().getContext(), article.getId());
            }
        });

        articleContainer.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(!checkRead())findViewById(R.id.uv_helpful_section).setVisibility(View.VISIBLE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try{
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                }catch(Exception e){}
                return true;

            }
        });
    }

    private boolean checkRead(){
        Activity activity = getActivity();
        if(activity != null)
            return ((ArticleActivity)activity).checkRead(mIdx);
        else
            return false;
    }

    private void setRead(){
        Activity activity = getActivity();
        if(activity != null) ((ArticleActivity)getActivity()).setRead(mIdx);
    }
    /*
    private void doAnimation(View view){
        TranslateAnimation animation = new TranslateAnimation (0, 0, 0, 500);
        animation.setDuration(500);
        view.startAnimation(animation);
    }
    */
    @Override
    protected int setupLayout() {

        return R.layout.uv_article_layout;
    }

    private void displayArticle(WebView webView, Article article) {
        Utils.displayArticle(webView, article, getActivity());
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if(articleContainer != null){
            articleContainer.removeAllViews();
            articleContainer.destroy();
//            articleContainer = null;
        }
        super.onDestroy();

    }

    private void showContactDialog() {
        new UnhelpfulDialogFragment().show(getFragmentManager(), "UnhelpfulDialogFragment");
    }
}
