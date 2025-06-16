package com.asus.supernote;

import com.asus.supernote.data.MetaData;
import com.asus.supernote.picker.NoteBookPickerActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


//Dave. Add a import task from fileManager.
public class ImportFileActivity extends Activity {
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Intent intent = new Intent(this, NoteBookPickerActivity.class);
		Intent intent2 = getIntent();
		Uri uri = (Uri)intent2.getData();
		String path = uri.getPath();
		Bundle extras = new Bundle();
		extras.putString("path", path);
//		intent.putExtras(extras);
		intent.putExtra(MetaData.IMPORT_INTENT, extras);
		startActivity(intent);
		this.finish();
	}
}
