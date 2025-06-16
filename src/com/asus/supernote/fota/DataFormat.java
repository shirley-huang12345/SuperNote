package com.asus.supernote.fota;

import android.os.Environment;

public class DataFormat {
	
	public static final String FOTA_ROOT_DIR_NAME = "FOTA/";
	public static final String FOTA_ROOT_DIR = Environment.getExternalStorageDirectory() + "/"
			+ FOTA_ROOT_DIR_NAME;
	
	public static final String UTF8_CHARSET_NAME = "UTF-8";

}
