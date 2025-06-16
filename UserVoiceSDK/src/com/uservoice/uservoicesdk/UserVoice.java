package com.uservoice.uservoicesdk;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;

import java.util.Locale;

import com.uservoice.uservoicesdk.activity.ArticleActivity;
import com.uservoice.uservoicesdk.activity.ContactActivity;
import com.uservoice.uservoicesdk.activity.ForumActivity;
import com.uservoice.uservoicesdk.activity.PortalActivity;
import com.uservoice.uservoicesdk.activity.PostIdeaActivity;
import com.uservoice.uservoicesdk.babayaga.Babayaga;
import com.uservoice.uservoicesdk.ga.GAManager;

public class UserVoice {

    public static int sColor = Color.argb(255, 255, 174, 201);
    public static boolean sNeedReload = false;
/*
    public static boolean checkCTANetworkPermission(Context ctx) {
        CtaChecker checker = new CtaChecker(ctx);
        return checker.checkPermission(CtaChecker.CTA_USE_NETWORK, ctx.getPackageName());
    }
*/
    public static void launchArticle(Context context, int articleId){
        Intent intent = new Intent(context, ArticleActivity.class);
        intent.putExtra(ArticleActivity.KEY_ARTICLE_ID, articleId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void launchBugReport(Context context) {
        //if(!checkCTANetworkPermission(context)) return;
        Babayaga.init(context);
        Babayaga.setUserTraits(Session.getInstance().getConfig().getUserTraits());
        Intent intent = new Intent(context, ContactActivity.class);
        intent.putExtra(ContactActivity.KEY_BUG_REPORT, true);
        context.startActivity(intent);
    }

    public static void launchUserVoice(Context context) {
        //if(!checkCTANetworkPermission(context)) return;
        Babayaga.init(context);
        Babayaga.setUserTraits(Session.getInstance().getConfig().getUserTraits());
        Intent intent = new Intent(context, PortalActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void launchUserVoice(Context context, int flags) {
        Babayaga.init(context);
        Babayaga.setUserTraits(Session.getInstance().getConfig().getUserTraits());
        Intent intent = new Intent(context, PortalActivity.class);
        intent.addFlags(flags);
        context.startActivity(intent);
    }

    public static void launchForum(Context context) {
        context.startActivity(new Intent(context, ForumActivity.class));
    }

    public static void launchContactUs(Context context) {
        context.startActivity(new Intent(context, ContactActivity.class));
    }

    public static void launchPostIdea(Context context) {
        context.startActivity(new Intent(context, PostIdeaActivity.class));
    }

    public static void init(ConfigInterface configInterface, Context context){
        resetGAID();
        Config config = new Config("asus.uservoice.com", "YRVMmyxiwx992928okg", "3L10HXrOyfHPj5DZO8sLdpmVct7qPKoVM5amYI3sQ");
        config.setForumId(configInterface.getForumID());
        config.setTopicId(configInterface.getTopicID());
        if(configInterface.getAttachmentPath() != null){
            config.enableAttachment(configInterface.getAttachmentPath());
        }
        String appName = "Unknown";

        Map map = new HashMap<String, String>();
        if(configInterface instanceof Henrittable){
            Henrittable henritta = (Henrittable)configInterface;
            map.put(Config.Fields.AppID.KEY, henritta.getPackage());
            map.put(Config.Fields.APPVersion.KEY, henritta.getVersionName());
            map.put(Config.Fields.APPLabel.KEY, henritta.getAPPLabel());
            map.put(Config.Fields.FromAppsHelp.KEY, "true");
            config.setFromAppsHelp(true);
            config.setAPPTitle(henritta.getAPPTitle());
            config.setAPPID(henritta.getPackage());
            config.setAPPLabel(henritta.getAPPLabel());
        }else{

            String appVersion = "Unknown";

            PackageManager manager = context.getPackageManager();
            PackageInfo info;
            try {
                info = manager.getPackageInfo(
                    context.getPackageName(), 0);
                appVersion = info.versionName;
            } catch (Exception e) {
                appVersion = "Unknown";
            }

            map.put(Config.Fields.APPVersion.KEY, appVersion);

            String appLabel = "Unknown";
            Resources res = context.getResources();
            Configuration originalConfig = res.getConfiguration();
            Configuration original = new Configuration(originalConfig);
            try {

                ApplicationInfo appInfo = context.getApplicationInfo();
                Configuration enConfig = new Configuration();
                enConfig.locale = Locale.ENGLISH;
                res.updateConfiguration(enConfig, res.getDisplayMetrics());
                appLabel = res.getString(appInfo.labelRes);


            }catch(Exception e){
                e.printStackTrace();
                appLabel = "Unknown";
            }finally{
                res.updateConfiguration(original, res.getDisplayMetrics());
            }

            map.put(Config.Fields.APPLabel.KEY, appLabel);
            map.put(Config.Fields.AppID.KEY, context.getPackageName());
            map.put(Config.Fields.FromAppsHelp.KEY, "false");
            config.setFromAppsHelp(false);
            config.setAPPID(context.getPackageName());
            config.setAPPLabel(appLabel);
        }
        config.setCustomFields(map);
        sColor = configInterface.getPrimaryColor();
        init(config, context);
    }

    public static void setPrimaryColor(int color){
        sColor = color;
    }

    public static void init(Config config, Context context) {
        Session.reset();
        Session.getInstance().setContext(context);
        Locale locale = context.getResources().getConfiguration().locale;
        config.getCustomFields().put(Config.Fields.UserInfo.KEY, "Country: " + locale.getCountry() + "; Language: " + locale.getLanguage());
        config.getCustomFields().put(Config.Fields.ModelName.KEY, Build.MODEL);
        config.getCustomFields().put(Config.Fields.BuildNumber.KEY, Build.DISPLAY);
        Session.getInstance().setConfig(config);
    }

    public static void setExternalId(String scope, String id) {
        Session.getInstance().setExternalId(scope, id);
    }

    public static void setGAEnable(boolean enable){
        GAManager.setEnable(enable);
    }

    public static void enableGALog(){
        GAManager.enableGALog();
    }

    public static void track(String event, Map<String, Object> properties) {
        Babayaga.track(event, properties);
    }

    public static void track(String event) {
        track(event, null);
    }

    public static String getVersion() {
        return "1.1.0.150430_5";
    }

    public static void setGAID(String id){
    	GAManager.setGAID(id);
    }

    public static void resetGAID(){
    	GAManager.resetGAID();
    }

}
