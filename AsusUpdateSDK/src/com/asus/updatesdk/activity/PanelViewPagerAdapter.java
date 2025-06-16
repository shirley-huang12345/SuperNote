package com.asus.updatesdk.activity;

import com.asus.updatesdk.AppInfo;
import com.asus.updatesdk.ZenUiFamily.AppStatus;
import com.asus.updatesdk.analytic.AnalyticUtils.Category;
import com.asus.updatesdk.analytic.TrackerManager;
import com.asus.updatesdk.cache.ImageFetcher;
import com.asus.updatesdk.utility.GeneralUtils;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rocio_Wang on 2015/7/3.
 */
public class PanelViewPagerAdapter extends PagerAdapter {
    private ArrayList<AppInfo> mAppInfos;
    private int mSize;
    private List<ImageView> mImageViewsArrayList = new ArrayList<>();
    private Context mContext;
    private ImageFetcher mImageFetcher;

    public PanelViewPagerAdapter(Context context, ArrayList<AppInfo> appInfos,
                                 ImageFetcher imageFetcher) {
        mAppInfos = appInfos;
        mSize = mAppInfos.size();
        mContext = context;
        mImageFetcher = imageFetcher;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mImageViewsArrayList.get(position));
        return mImageViewsArrayList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position,
                            Object object) {
        container.removeView(mImageViewsArrayList.get(position));
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void notifyDataSetChanged() {
        mSize = mAppInfos.size();
        initImage();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mSize;
    }

    public void refreshImagePanel(){
        mImageViewsArrayList.clear();
        initImage();
    }

    public void initImage() {
        for (final AppInfo appInfo : mAppInfos) {
            ImageView imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //If PlayImageUrl image is null or not exist , download from CDNImageUrl
            mImageFetcher.loadImage(appInfo.getPlayImageUrl(), appInfo.getCDNImageUrl(), imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String packageName = appInfo.getPackageName();
                    AppStatus appStatus = appInfo.getStatus();
                    if (AppStatus.UP_TO_DATE == appStatus) {
                        boolean success = GeneralUtils.openAsusApp(mContext, packageName);
                        if (!success) {
                            GeneralUtils.openGooglePlayPage(mContext, packageName);
                        }
                    } else {
                        GeneralUtils.startCheckAppUpdateAlarm(mContext, packageName);
                        GeneralUtils.openGooglePlayPage(mContext, packageName);
                    }
                    TrackerManager.sendEvents(mContext, TrackerManager.GA_TRACKER,
                            Category.CLICK_PANEL_APPS, packageName, appStatus.getName(),
                            TrackerManager.DEFAULT_VALUE);
                }
            });
            mImageViewsArrayList.add(imageView);
        }
    }
}
