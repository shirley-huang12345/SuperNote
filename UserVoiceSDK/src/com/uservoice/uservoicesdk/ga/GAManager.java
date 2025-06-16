package com.uservoice.uservoicesdk.ga;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.UserVoice;

public class GAManager {

    private static final String DEFAULT_GA_ID = Build.TYPE.equals("user")? "UA-57133151-3": "UA-57133151-4";
    private static String GA_ID = DEFAULT_GA_ID;

    private static sGATracker instance;

    private static boolean enable = false;

    private enum Category {
        FAQ, FAQ_fromHelp, FORUM
    }

    public static class FAQ {
        public static enum Action {
            Click_Useful, Click_UnUseful, Read_FAQ
        }

        public static void ClickYes(Context context, int articleId) {

            if (enable) {
                getInstance(context).send(getBaseAction(Action.Click_Useful, articleId));
            }
        }

        public static void ClickNo(Context context, int articleId) {
            if (enable) {
                getInstance(context).send(getBaseAction(Action.Click_UnUseful, articleId));
            }
        }

        public static void Read(Context context, int articleId) {
            if (enable) {
                getInstance(context).send(getBaseAction(Action.Read_FAQ, articleId));
            }
        }

        private static Map<String, String> getBaseAction(Action action, int articleId) {
            Category category = Category.FAQ;

            if (Session.getInstance().getConfig().isFromAppsHelp()) {
                category = Category.FAQ_fromHelp;
            }

            HashMap<String, String> params = new HashMap<String, String>();
            params.put(sGATracker.Fields.EVENT_CATEGORY, category.name());
            params.put(sGATracker.Fields.EVENT_ACTION, action.name());
            params.put(sGATracker.Fields.EVENT_LABEL, String.valueOf(articleId));

            return params;
        }
    }

    public static class FORUM {
        public static enum Action {
            View
        }

        public static void view(Context context, int forumId) {

            if (enable) {
                getInstance(context).send(getBaseAction(Action.View, forumId));
            }
        }


        private static Map<String, String> getBaseAction(Action action, int forumId) {
            Category category = Category.FORUM;
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(sGATracker.Fields.EVENT_CATEGORY, category.name());
            params.put(sGATracker.Fields.EVENT_ACTION, action.name());
            params.put(sGATracker.Fields.EVENT_LABEL, String.valueOf(forumId));

            return params;
        }
    }

    public static sGATracker getInstance(Context context) {
        if (instance==null){
            instance = sGATracker.getInstance(context, GA_ID);
            instance.set(sGATracker.Fields.APP_ID, Session.getInstance().getConfig().getAPPID());
            instance.set(sGATracker.Fields.APP_NAME, Session.getInstance().getConfig().getAPPLabel());
        }
        return instance;
    }

    public static void setEnable(boolean bool){
        enable = bool;
    }

    public static void enableGALog(){
        DebugLogConfig.enable();
    }

    public static void setGAID(String id){
        if(!TextUtils.equals(GA_ID, id)){
            resetInstance();
            GA_ID = id;
        }
    }

    public static void resetGAID(){
        if(!TextUtils.equals(GA_ID, DEFAULT_GA_ID)){
            resetInstance();
            GA_ID = DEFAULT_GA_ID;
        }
    }

    private static void resetInstance(){
        instance = null;
        sGATracker.reset();
    }
}
