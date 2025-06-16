package com.uservoice.uservoicesdk.activity;

import java.util.List;
import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.UserVoice;
import com.uservoice.uservoicesdk.fragment.ArticleFragment;
import com.uservoice.uservoicesdk.ga.GAManager;
import com.uservoice.uservoicesdk.model.Article;
import com.uservoice.uservoicesdk.ui.DefaultCallback;
import com.uservoice.uservoicesdk.ui.Utils;

public class ArticleActivity extends BaseActivity implements SearchActivity {

    public static final String POSITION = "position";
    public static final String KEY_ARTICLE_ID = "article_id";
    public static final String KEY_ARTICLE = "article";

    private int position;
    private ViewPager viewPager;
    private List<Article> listArticle;
    private PagerAdapter articlePagerAdapter;
    private boolean[] mReadArray;
    private Article mArticle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserVoice.sNeedReload = true;
        if(Session.getInstance().getConfig() == null){
            finish();
            return;
        }
        setContentView(R.layout.uf_sdk_activity_article);
        setTitle(R.string.uf_sdk_faq);

        setupBackground();
        int articleId = getIntent().getIntExtra(KEY_ARTICLE_ID, -1);
        if(savedInstanceState != null) mArticle = savedInstanceState.getParcelable(KEY_ARTICLE);
        if(mArticle == null && articleId != -1){
            mReadArray = new boolean[1];
            DefaultCallback<Article> callback = new DefaultCallback<Article>(this){
                @Override
                public void onModel(Article model) {
                    // TODO Auto-generated method stub
                    if(model != null){
                        listArticle = new ArrayList<Article>();
                        mArticle = model;
                        listArticle.add(mArticle);
                        position = getIntent().getIntExtra(POSITION, 0);
                        viewPager = (ViewPager) findViewById(R.id.pager);
                        articlePagerAdapter = new ArticlePagerAdapter(getSupportFragmentManager());
                        viewPager.setAdapter(articlePagerAdapter);
                        viewPager.setOnPageChangeListener(new OnPageChangeListener(){
                            @Override
                            public void onPageScrollStateChanged(int arg0) {

                            }

                            @Override
                            public void onPageScrolled(int arg0, float arg1, int arg2) {

                            }

                            @Override
                            public void onPageSelected(int idx) {
                                GAManager.FAQ.Read(ArticleActivity.this, listArticle.get(idx).getId());
                            }
                        });
                        if(position == 0) GAManager.FAQ.Read(ArticleActivity.this, listArticle.get(0).getId());
                        viewPager.setCurrentItem(position);
                    }else{
                        finish();
                    }
                }
            };
            Article.loadArticle(articleId, callback);
            return;
        }else if(mArticle != null){
            mReadArray = new boolean[1];
            listArticle = new ArrayList<Article>();
            listArticle.add(mArticle);
            position = 0;
            viewPager = (ViewPager) findViewById(R.id.pager);
            articlePagerAdapter = new ArticlePagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(articlePagerAdapter);
            viewPager.setOnPageChangeListener(new OnPageChangeListener(){
                @Override
                public void onPageScrollStateChanged(int arg0) {

                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {

                }

                @Override
                public void onPageSelected(int idx) {
                    GAManager.FAQ.Read(ArticleActivity.this, listArticle.get(idx).getId());
                }
            });
            if(position == 0) GAManager.FAQ.Read(ArticleActivity.this, listArticle.get(0).getId());
            viewPager.setCurrentItem(position);
            return;
        }
        listArticle = getIntent().getParcelableArrayListExtra(Article.class.getName());
        mReadArray = new boolean[listArticle.size()];
        position = getIntent().getIntExtra(POSITION, 0);

        viewPager = (ViewPager) findViewById(R.id.pager);
        if (listArticle != null) {
            articlePagerAdapter = new ArticlePagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(articlePagerAdapter);
            viewPager.setOnPageChangeListener(new OnPageChangeListener(){
                @Override
                public void onPageScrollStateChanged(int arg0) {

                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {

                }

                @Override
                public void onPageSelected(int idx) {
                    GAManager.FAQ.Read(ArticleActivity.this, listArticle.get(idx).getId());
                }
            });
            if(position == 0) GAManager.FAQ.Read(this, listArticle.get(0).getId());
            viewPager.setCurrentItem(position);

        } else {
            finish();
        }

    }

    public void setRead(int idx){
        mReadArray[idx] = true;
    }

    public boolean checkRead(int idx){
        return mReadArray[idx];
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

    private class ArticlePagerAdapter extends FragmentStatePagerAdapter {
        public ArticlePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ArticleFragment.getInstance(listArticle.get(position), position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return listArticle.get(position).getTitle();
        }

        @Override
        public int getCount() {
            return listArticle.size();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        if(mArticle != null) outState.putParcelable(KEY_ARTICLE, mArticle);
        super.onSaveInstanceState(outState);
    }

}
