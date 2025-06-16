package com.uservoice.uservoicesdk;

import android.util.Log;

import com.uservoice.uservoicesdk.model.Attachment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Config {
    private String site;
    private String key;
    private String secret;
    private String email;
    private String name;
    private String guid;
    private Map<String, String> customFields = new HashMap<String, String>();
    private int topicId = -1;
    private int forumId = -1;
    private boolean showForum = true;
    private boolean showPostIdea = true;
    private boolean showContactUs = true;
    private boolean showKnowledgeBase = true;
    private Map<String, Object> userTraits = new HashMap<String, Object>();
    private List<Attachment> attachmentList = new ArrayList<Attachment>();
    private Map<String, String> mAdditionalustomFields = new HashMap<String, String>();
    private String mPath;
    private String mAPPTitle;
    private String mAPPID;
    private String mAPPLabel;
    private boolean mIsFromAppsHelp;
    private boolean mIsAssistance;
    private String[] mDeviceInfoName;
    private String[] mDeviceInfoValue;

    public static class Fields{
        public static class AppID{
            public static final String KEY = "APP_ID";
        }

        public static class UserInfo{
            public static final String KEY = "User info";
        }

        public static class BuildNumber{
            public static final String KEY = "Build number";
        }

        public static class APPVersion{
            public static final String KEY = "APP_Version";
        }

        public static class APPLabel{
            public static final String KEY = "APP_Label";
        }

        public static class FromAppsHelp{
            public static final String KEY = "From_AppsHelp";
        }
        
        public static class ModelName{
            public static final String KEY = "Model_Name";
        }

    }

    public Config(String site) {
        this.site = site;
    }

    public Config(String site, String key, String secret) {
        this.site = site;
        this.key = key;
        this.secret = secret;
    }

    public String getSite() {
        return site;
    }

    public String getKey() {
        return key;
    }

    public String getSecret() {
        return secret;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getGuid() {
        return guid;
    }

    public void setAPPTitle(String title){
        mAPPTitle = title;
    }

    public String getAPPTitle(){
        return mAPPTitle;
    }

    public void setAPPID(String id){
        mAPPID = id;
    }

    public String getAPPID(){
        return mAPPID;
    }

    public void setAPPLabel(String label){
        mAPPLabel = label;
    }

    public String getAPPLabel(){
        return mAPPLabel;
    }

    public void setFromAppsHelp(boolean bool){
        mIsFromAppsHelp = bool;
    }

    public boolean isFromAppsHelp(){
        return mIsFromAppsHelp;
    }
    
    public void setAssistance(boolean bool){
        mIsAssistance = bool;
    }

    public boolean isAssistance(){
        return mIsAssistance;
    }

    public void setDeviceInfoName(String[] name){
        mDeviceInfoName = name;
    }

    public String[] getDeviceInfoName(){
        return mDeviceInfoName;
    }

    public void setDeviceInfoValue(String[] value){
    	mDeviceInfoValue = value;
    }

    public String[] getDeviceInfoValue(){
        return mDeviceInfoValue;
    }


    public Map<String, String> getCustomFields() {
        return customFields;
    }

    public String getAttachmentPath(){
        return mPath;
    }

    public void enableAttachment(String path){
        mPath = path;
    }

    public void setAdditionalCustomFields(Map<String, String> additionalustomFields){
        Set set = additionalustomFields.keySet();
        Iterator it = set.iterator();
        while(it.hasNext()){
            String key = it.next().toString();
            String value = additionalustomFields.get(key);
        }
        this.mAdditionalustomFields = additionalustomFields;
    }

    public Map<String, String> getAdditionalCustomFields(){
        Set set = mAdditionalustomFields.keySet();
        Iterator it = set.iterator();
        while(it.hasNext()){
            String key = it.next().toString();
            String value = mAdditionalustomFields.get(key);
        }

        return mAdditionalustomFields;
    }

    public void setCustomFields(Map<String, String> customFields) {
        this.customFields = customFields;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getForumId() {
        if (forumId == -1 && Session.getInstance().getClientConfig() != null)
            return Session.getInstance().getClientConfig().getDefaultForumId();
        return forumId;
    }

    public void setForumId(int forumId) {
        this.forumId = forumId;
    }

    public List<Attachment> getAttachmentList() {
        return attachmentList;
    }

    public boolean shouldShowForum() {
        if (Session.getInstance().getClientConfig() != null && !Session.getInstance().getClientConfig().isFeedbackEnabled())
            return false;
        return showForum;
    }

    public void setShowForum(boolean showForum) {
        this.showForum = showForum;
    }

    public boolean shouldShowPostIdea() {
        if (Session.getInstance().getClientConfig() != null && !Session.getInstance().getClientConfig().isFeedbackEnabled())
            return false;
        return showPostIdea;
    }

    public void setShowPostIdea(boolean showPostIdea) {
        this.showPostIdea = showPostIdea;
    }

    public boolean shouldShowContactUs() {
        if (Session.getInstance().getClientConfig() != null && !Session.getInstance().getClientConfig().isTicketSystemEnabled())
            return false;
        return showContactUs;
    }

    public void setShowContactUs(boolean showContactUs) {
        this.showContactUs = showContactUs;
    }

    public boolean shouldShowKnowledgeBase() {
        if (Session.getInstance().getClientConfig() != null && !Session.getInstance().getClientConfig().isTicketSystemEnabled())
            return false;
        return showKnowledgeBase;
    }

    public void setShowKnowledgeBase(boolean showKnowledgeBase) {
        this.showKnowledgeBase = showKnowledgeBase;
    }

    public void identifyUser(String id, String name, String email) {
        guid = id;
        this.name = name;
        this.email = email;
        putUserTrait("id", id);
        putUserTrait("name", name);
        putUserTrait("email", email);
    }

    public void putUserTrait(String key, String value) {
        userTraits.put(key, value);
    }

    public void putUserTrait(String key, int value) {
        userTraits.put(key, value);
    }

    public void putUserTrait(String key, boolean value) {
        userTraits.put(key, value);
    }

    public void putUserTrait(String key, float value) {
        userTraits.put(key, value);
    }

    public void putUserTrait(String key, Date value) {
        userTraits.put(key, value.getTime() / 1000);
    }

    public void putAccountTrait(String key, String value) {
        putUserTrait("account_" + key, value);
    }

    public void putAccountTrait(String key, int value) {
        putUserTrait("account_" + key, value);
    }

    public void putAccountTrait(String key, boolean value) {
        putUserTrait("account_" + key, value);
    }

    public void putAccountTrait(String key, float value) {
        putUserTrait("account_" + key, value);
    }

    public void putAccountTrait(String key, Date value) {
        putUserTrait("account_" + key, value);
    }

    public Map<String, Object> getUserTraits() {
        return userTraits;
    }

    public void addAttachment(Attachment attachment) {
        if (attachment != null) {
            attachmentList.add(attachment);
        }
    }
}