package com.uservoice.uservoicesdk.ui;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.model.CustomField;

public class SpinnerAdapter<T> extends BaseAdapter {

    private static int NONE = 0;
    private static int OBJECT = 1;

    private final List<T> objects;
    private final List<Integer> mIDs;
    private LayoutInflater inflater;
    private int color;
    private Context mContext;

    public SpinnerAdapter(Activity context, List<T> objects) {
        this(context, objects, null);
    }

    public SpinnerAdapter(Activity context, List<T> objects, List<Integer> ids) {
        this.objects = objects;
        inflater = context.getLayoutInflater();
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true);
        color = context.getResources().getColor(tv.resourceId);
        mContext = context;
        mIDs = ids;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {

        return objects.get(position);
    }

    private int getID(int position){
        if(mIDs == null){
            return 0;
        }else{
            return mIDs.get(position);
        }

    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return OBJECT;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        int type = getItemViewType(position);
        if (view == null) {
            view = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        TextView textView = (TextView) view;
        if (type == OBJECT) {
            textView.setTextColor(color);
            //textView.setText(getItem(position).toString());
            String packageName = mContext.getPackageName();

            int resId = mContext.getResources().getIdentifier(CustomField.PREFIX + String.valueOf(getID(position)), "string", mContext.getPackageName());
            String titleText = resId == 0? getItem(position).toString(): mContext.getResources().getString(resId);
            textView.setText(titleText);
        } else {
            textView.setTextColor(Color.GRAY);
            textView.setText(R.string.uv_select_none);
        }
        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        int type = getItemViewType(position);
        if (view == null) {
            view = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        TextView textView = (TextView) view;
        if (type == OBJECT) {
            textView.setTextColor(color);
            //textView.setText(getItem(position).toString());
            String packageName = mContext.getPackageName();

            int resId = mContext.getResources().getIdentifier(CustomField.PREFIX + String.valueOf(getID(position)), "string", mContext.getPackageName());
            String titleText = resId == 0? getItem(position).toString(): mContext.getResources().getString(resId);
            textView.setText(titleText);
        } else {
            textView.setTextColor(color);
            textView.setText(R.string.uv_select_one);
        }
        return view;
    }

}
