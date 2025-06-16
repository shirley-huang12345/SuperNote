package com.uservoice.uservoicesdk;

public abstract class ConfigInterface {

    public String getAttachmentPath(){
        return null;
    }
    public abstract int getTopicID();
    public abstract int getForumID();
    public abstract int getPrimaryColor();
}
