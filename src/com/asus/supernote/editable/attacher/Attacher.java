package com.asus.supernote.editable.attacher;

import android.content.Intent;

public interface Attacher {
    public Intent getIntent();

    public void attachItem(Intent intent);
}
