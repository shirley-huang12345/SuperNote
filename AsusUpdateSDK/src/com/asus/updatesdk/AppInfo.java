package com.asus.updatesdk;

import com.asus.updatesdk.ZenUiFamily.AppStatus;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by Chauncey_Li on 2015/6/30.
 */
public class AppInfo implements Parcelable, Comparable {
    private String mTitle;
    private String mPackageName;
    private String mSlogan;
    private float mRating;
    private String mDownloadCounts;
    private String mPlayImageUrl;
    private String mCDNImageUrl;
    private Bitmap mIconBitmap;
    private String mGoToGooglePlayUrl;
    private AppStatus mStatus;

    public AppInfo(String title, String packageName, String slogan, float rating,
            String downloadCounts, String playImageUrl, String CDNImageUrl,
            Bitmap iconBitmap, String goToGooglePlayUrl, AppStatus status) {
        mTitle = title;
        mPackageName = packageName;
        mSlogan = slogan;
        mRating = rating;
        mDownloadCounts = downloadCounts;
        mPlayImageUrl = playImageUrl;
        mCDNImageUrl = CDNImageUrl;
        mIconBitmap = iconBitmap;
        mGoToGooglePlayUrl = goToGooglePlayUrl;
        mStatus = status;
    }

    private AppInfo(Parcel in) {
        mTitle = in.readString();
        mPackageName = in.readString();
        mSlogan = in.readString();
        mRating = in.readFloat();
        mDownloadCounts = in.readString();
        mPlayImageUrl = in.readString();
        mCDNImageUrl = in.readString();
        mIconBitmap = in.readParcelable(getClass().getClassLoader());
        mGoToGooglePlayUrl = in.readString();
        mStatus = AppStatus.getFromIndex(in.readInt());
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public void setSlogan(String slogan) {
        mSlogan = slogan;
    }

    public void setRating(float rating) {
        mRating = rating;
    }

    public void setDownloadCounts(String downloadCounts) {
        mDownloadCounts = downloadCounts;
    }

    public void setPlayImageUrl(String playImageUrl) {
        mPlayImageUrl = playImageUrl;
    }

    public void setCDNImageUrl(String CDNImageUrl) {
        mCDNImageUrl = CDNImageUrl;
    }

    public void setIconBitmap(Bitmap iconBitmap) {
        mIconBitmap = iconBitmap;
    }

    public void setGoToGooglePlayUrl(String goToGooglePlayUrl) {
        mGoToGooglePlayUrl = goToGooglePlayUrl;
    }

    public void setStatus(AppStatus status) {
        mStatus = status;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getSlogan() {
        return mSlogan;
    }

    public float getRating() {
        return mRating;
    }

    public String getDownloadCounts() {
        return mDownloadCounts;
    }

    public String getPlayImageUrl() {
        return mPlayImageUrl;
    }

    public String getCDNImageUrl() {
        return mCDNImageUrl;
    }

    public Bitmap getIconBitmap() {
        return mIconBitmap;
    }

    public String getGoToGooglePlayUrl(){
        return mGoToGooglePlayUrl;
    }

    public AppStatus getStatus(){
        return mStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mPackageName);
        dest.writeString(mSlogan);
        dest.writeFloat(mRating);
        dest.writeString(mDownloadCounts);
        dest.writeString(mPlayImageUrl);
        dest.writeString(mCDNImageUrl);
        dest.writeParcelable(mIconBitmap, flags);
        dest.writeString(mGoToGooglePlayUrl);
        dest.writeInt(mStatus.getIndex());
    }

    public static final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() {
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

    @Override
    public int compareTo(@NonNull Object object) {
        AppInfo appInfo = (AppInfo) object;
        return this.getStatus().getIndex() - appInfo.getStatus().getIndex();
    }
}
