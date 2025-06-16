package com.asus.updatesdk.activity;

import com.asus.updatesdk.AppInfo;
import com.asus.updatesdk.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Rocio_Wang on 2015/6/29.
 */
public class AppInfoAdapter extends ArrayAdapter<AppInfo> {
    private static final String TAG = "AppInfoAdapter";

    static class ViewHolder {
        TextView appTitle;
        TextView appRating;
        TextView appDownloads;
        TextView appSlogan;
        RatingBar ratingBar;
        ImageView icon;
        TextView gotoButtonTextView;
        ImageView itemButton;
        RelativeLayout importantLayout;
    }

    private ArrayList<AppInfo> mAppInfoList;
    private Context mContext;
    private int mThemeColor;
    private int mUpdateButtonColor;
    private int mOpenButtonColor;

    public AppInfoAdapter(Context context, int resource, ArrayList<AppInfo> appInfoList) {
        super(context, resource, appInfoList);
        mAppInfoList = appInfoList;
        mContext = context;
    }

    public void setItemButtonColor(int themeColor, int updateButtonColor, int openButtonColor) {
        mThemeColor = themeColor;
        mUpdateButtonColor = updateButtonColor;
        mOpenButtonColor = openButtonColor;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (null == convertView) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.ud_sdk_listview_item,
                    parent, false);

            holder.appTitle = (TextView) convertView.findViewById(R.id.ud_sdk_item_app_title);
            holder.appRating = (TextView) convertView.findViewById(R.id.ud_sdk_item_app_rating);
            holder.appDownloads = (TextView) convertView
                    .findViewById(R.id.ud_sdk_item_app_downloads);
            holder.appSlogan = (TextView) convertView.findViewById(R.id.ud_sdk_slogan_text_view);
            holder.ratingBar = (RatingBar) convertView
                    .findViewById(R.id.ud_sdk_item_app_rating_bar);
            holder.icon = (ImageView) convertView.findViewById(R.id.ud_sdk_item_image_view_icon);
            holder.gotoButtonTextView = (TextView) convertView
                    .findViewById(R.id.ud_sdk_list_view_button_text);
            holder.itemButton = (ImageView) convertView.findViewById(R.id.ud_sdk_list_view_button);
            holder.importantLayout = (RelativeLayout) convertView.findViewById(R.id
                    .ud_sdk_important_relativeLayout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.appTitle.setText(mAppInfoList.get(position).getTitle());
        holder.appSlogan.setText(mAppInfoList.get(position).getSlogan());
        holder.appRating.setText(String.valueOf(mAppInfoList.get(position).getRating()));
        holder.appDownloads
                .setText("(" + mAppInfoList.get(position).getDownloadCounts() + "K " +
                        mContext.getResources().getString(R.string.ud_sdk_downloads) + ")");
        holder.ratingBar.setRating(mAppInfoList.get(position).getRating());

        if (mAppInfoList.get(position).getIconBitmap() != null) {
            Glide.clear(holder.icon);
            holder.icon.setImageBitmap(mAppInfoList.get(position).getIconBitmap());
        } else {
            Glide.with(mContext)
                    .load(mAppInfoList.get(position).getPlayImageUrl())
                    .placeholder(R.mipmap.ud_sdk_none_image_icon)
                    .into(new GlideDrawableImageViewTarget(holder.icon) {
                        @Override
                        protected void setResource(GlideDrawable resource) {
                            super.setResource(resource);
                            holder.icon.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            Glide.with(mContext)
                                    .load(mAppInfoList.get(position).getCDNImageUrl())
                                    .placeholder(R.mipmap.ud_sdk_none_image_icon)
                                    .error(R.mipmap.ud_sdk_none_image_icon)
                                    .into(holder.icon);
                        }
                    });
        }

        int statusText;
        int statusButtonColor;
        switch (mAppInfoList.get(position).getStatus()) {
            case IMPORTANT:
                statusText = R.string.ud_sdk_update;
                statusButtonColor = mContext.getResources().getColor(R.color
                        .ud_sdk_important_remind_color);
                holder.importantLayout.setVisibility(View.VISIBLE);
                break;
            case UP_TO_DATE:
                statusText = R.string.ud_sdk_open;
                statusButtonColor = mOpenButtonColor;
                holder.importantLayout.setVisibility(View.GONE);
                break;
            case NOT_INSTALLED:
                statusText = R.string.ud_sdk_install;
                statusButtonColor = mThemeColor;
                holder.importantLayout.setVisibility(View.GONE);
                break;
            default:
                statusText = R.string.ud_sdk_update;
                statusButtonColor = mUpdateButtonColor;
                holder.importantLayout.setVisibility(View.GONE);
                break;
        }
        holder.itemButton.setColorFilter(statusButtonColor);
        holder.gotoButtonTextView.setText(statusText);
        return convertView;
    }

}
