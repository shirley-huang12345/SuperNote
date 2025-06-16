package com.asus.supernote.picker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.mozilla.universalchardet.UniversalDetector;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemProperties;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import com.asus.supernote.R;
import com.asus.supernote.SuperNoteApplication;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;

public class PickerUtility {
	public static final String TAG = "PickerUtility";
	public static final String GALLERY_FILE_EXTENSION = "jpg";// noah
	public static final String TEXT_FILE_EXTENSION = "txt";// emmanual

	public static final int getDeviceType(Context context) {
		int deviceType = 0;
		deviceType = context.getResources().getInteger(R.integer.device_type);
		return deviceType;
	}

	// Begin smilefish
	public static boolean isPhone(Context context) {
		int deviceType = context.getResources().getInteger(
				R.integer.device_type);
		if (deviceType <= MetaData.DEVICE_TYPE_A86)
			return true;
		else {
			return false;
		}
	}
	
	//Phone or 600dp pad
	public static boolean isPhoneOrSmallScreenPad(Context context){
		int deviceType = context.getResources().getInteger(
				R.integer.device_type);
		if (deviceType < 100)
			return true;
		else {
			return false;
		}
	}
	
	public static boolean is720DPDevice(Context context){
		int deviceType = context.getResources().getInteger(
				R.integer.device_type);
		if(deviceType > 100 && deviceType < 108)
			return true;
		else
			return false;
	}
	
	public static float getLineSpace(Context context){
		String model = SystemProperties.get("ro.product.model");
		if(model!=null && model.equalsIgnoreCase("k00g")) //ME560CG
			return 1.20f;
		else if(isPhone(context))
			return 1.35f;
		else 
			return 1.20f;
	}
	
	public static boolean isDeviceWithSoftKey(){
        String model = SystemProperties.get("ro.product.model");
        if(model.equalsIgnoreCase("ASUS_T00N")) //PF500KL
        	return true;
        else if(model.startsWith("ASUS_X550")) //T550KLC
        	return true;
        else
        	return false;
	}

	// end smilefish

	public static final boolean isPhoneSizeMode(NoteBook book) {
		return (book == null) ? false
				: (book.getPageSize() == MetaData.PAGE_SIZE_PHONE);
	}

	public static final int getScreenWidth(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return dm.widthPixels;
	}

	// RICHARD NEED CHECK LATER 1008
	public static final void lockRotation(Activity activity) {
		int value = 0;
		int orientation = activity.getWindowManager().getDefaultDisplay()
				.getOrientation();
		int IsInitHorizontal = activity.getResources().getInteger(
				R.integer.orientation_lock_init_horizontal); // by show
		int IsOrientationAllowOrNot = activity.getResources().getInteger(
				R.integer.is_orientation_allow); // by show

		if (IsInitHorizontal == 0) {
			switch (orientation) {
			case Surface.ROTATION_0:
				value = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			case Surface.ROTATION_90:
				if (IsOrientationAllowOrNot == 1) {
					value = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				} else {
					value = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
				}
				break;
			case Surface.ROTATION_180:
				value = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
				break;
			case Surface.ROTATION_270:
				if (IsOrientationAllowOrNot == 1) {
					value = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
				} else {
					value = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				}
				break;
			}
		} else {
			switch (orientation) {
			case Surface.ROTATION_0:
				value = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			case Surface.ROTATION_90:
				if (IsOrientationAllowOrNot == 1) {
					value = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
				} else {
					value = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				}
				break;
			case Surface.ROTATION_180:
				value = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
				break;
			case Surface.ROTATION_270:
				if (IsOrientationAllowOrNot == 1) {
					value = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				} else {
					value = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
				}
				break;
			}
		}
		activity.setRequestedOrientation(value);
	}

	public static final void unlockRotation(Activity activity) {
		int IsOrientationAllowOrNot = activity.getResources().getInteger(
				R.integer.is_orientation_allow); // by show
		if (IsOrientationAllowOrNot == 0) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		} else if (IsOrientationAllowOrNot == 1) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		} else {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		// End: show_wang@asus.com
	}

	public static boolean forceMkDir(String path) {
		Boolean result = false;
		File dir = new File(path);
		dir.mkdirs();
		while (dir.exists() == false) {
			result = dir.mkdirs();
			if (dir.exists() == false) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	//begin smilefish get real path of text file
	public static String getRealFilePathForText(Context context, Uri uri) {
		if (uri == null)
			return null;
		String filePath = getRealFilePath(context, uri.toString());
		if (filePath == null) {
			filePath = copyTextByUri(uri);
		}
		return filePath;
	}
	//end smilefish

	/*
	 * 获得图片的真实地址
	 * 
	 * @author noah
	 */
	public static String getRealFilePathForImage(Context context, Uri uri) {
		if (uri == null)
			return null;
		//emmanual to fix bug 497919
		if (isDownloadsDocument(uri)) {
			try {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
				        Uri.parse("content://downloads/public_downloads"),
				        Long.valueOf(id));
				return Uri.decode(getDataColumn(context, contentUri, null, null));
			} catch (Exception e) {
				Log.e(TAG, "getRealFilePath error");
			}
		}

		String filePath = getRealFilePath(context, uri.toString());
		if (filePath == null) {
			filePath = copyByUri(uri);
		}
		return Uri.decode(filePath);
	}
	
	/**
	 * @author Emmanual
	 * @param context
	 * @param uri
	 * @return
	 */
	public static String getDocumentPath(Context context, Uri uri) {
	    // DocumentProvider
	    if (DocumentsContract.isDocumentUri(context, uri)) {
	        // ExternalStorageProvider
	        if (isExternalStorageDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            if ("primary".equalsIgnoreCase(type)) {
	                return Environment.getExternalStorageDirectory() + "/" + split[1];
	            }else if(uri.toString().contains("com.android.externalstorage")){
	            	return "/storage/MicroSD/" + split[1];
	            }

	            // TODO handle non-primary volumes
	        }
	        // DownloadsProvider
	        else if (isDownloadsDocument(uri)) {

	            final String id = DocumentsContract.getDocumentId(uri);
	            final Uri contentUri = ContentUris.withAppendedId(
	                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

	            return getDataColumn(context, contentUri, null, null);
	        }
	        // MediaProvider
	        else if (isMediaDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            Uri contentUri = null;
	            if ("image".equals(type)) {
	                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	            } else if ("video".equals(type)) {
	                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	            } else if ("audio".equals(type)) {
	                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	            }

	            final String selection = "_id=?";
	            final String[] selectionArgs = new String[] {
	                    split[1]
	            };

	            return getDataColumn(context, contentUri, selection, selectionArgs);
	        }
	    }
	    // MediaStore (and general)
	    else if ("content".equalsIgnoreCase(uri.getScheme())) {

	        // Return the remote address
	        if (isGooglePhotosUri(uri))
	            return uri.getLastPathSegment();

	        return getDataColumn(context, uri, null, null);
	    }

	    return null;
	}

	/**
	 * @author Emmanual
	 * @param uri
	 * @return
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @author Emmanual
	 * @param uri
	 * @return
	 */
	public static boolean isMediaDocument(Uri uri) {
	    return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @author Emmanual
	 * @param uri
	 * @return
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
	    return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}

	//emmanual to fix bug 497919
	public static String getDataColumn(Context context, Uri uri,
	        String selection, String[] selectionArgs) {
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
			        selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				String str = cursor.getString(index);
				cursor.close(); //smilefish fix memory leak
				return str;
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	//emmanual to fix bug 497919
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
		        .getAuthority());
	}


	/**
	 * 获得图片的真实地址
	 * 
	 * @author noah_zhang
	 * @param uri
	 * @return
	 */
	public static String getRealFilePathForImage(Context context, Bitmap bitmap) {
		if (bitmap == null)
			return null;
		String filePath = copyFromBitmap(bitmap);
		return filePath;
	}

	/*
	 * 从云端(如gmail)下载到本地的图片，有可能获得不到真实的地址。此时需要直接打开一个stream来读取；并把它复制到temp目录下
	 * 
	 * @author noah
	 * 
	 * @return 返回复制成功的文件地址
	 */
	private static String copyByUri(Uri uri) {
		InputStream fis = null;
		String path = getTempFileName();
		try {
			fis = SuperNoteApplication.getContext().getContentResolver()
					.openInputStream(uri);
			File temp = new File(MetaData.TEMP_DIR);
			if (!temp.exists()) {
				temp.mkdirs();
			}
			File desFile = new File(path);
			FileOutputStream fos = new FileOutputStream(desFile);
			int c;
			byte[] b = new byte[1024 * 5];
			while ((c = fis.read(b)) != -1) {
				fos.write(b, 0, c);
			}
			fis.close();
			fos.flush();
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return path;
	}

	//emmanual
	private static String copyTextByUri(Uri uri) {
		InputStream fis = null;
		String path = getTempTextFileName();
		try {
			fis = SuperNoteApplication.getContext().getContentResolver()
					.openInputStream(uri);
			File temp = new File(MetaData.TEMP_DIR);
			if (!temp.exists()) {
				temp.mkdirs();
			}
			File desFile = new File(path);
			FileOutputStream fos = new FileOutputStream(desFile);
			int c;
			byte[] b = new byte[1024 * 5];
			while ((c = fis.read(b)) != -1) {
				fos.write(b, 0, c);
			}
			fis.close();
			fos.flush();
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return path;
	}

	/*
	 * 将bitmap保存成一个文件
	 * 
	 * @author noah
	 * 
	 * @return 返回复制成功的文件地址
	 */
	private static String copyFromBitmap(Bitmap bitmap) {
		String path = getTempFileName();
		FileOutputStream fo = null;
		try {
			File temp = new File(MetaData.TEMP_DIR);
			if (!temp.exists()) {
				temp.mkdirs();
			}
			File file = new File(path);
			fo = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fo);
			fo.flush();
			fo.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return path;
	}

	private static String getTempFileName() {
		String path = MetaData.TEMP_DIR + System.currentTimeMillis() + "."
				+ GALLERY_FILE_EXTENSION;
		return path;
	}
	
	private static String getTempTextFileName() {
		String path = MetaData.TEMP_DIR + System.currentTimeMillis() + "."
				+ TEXT_FILE_EXTENSION;
		return path;
	}

	public static String getRealFilePath(Context context, String path) {
		//emmanual to fix bug 516236
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			try {
				String documentPath = getDocumentPath(context, Uri.parse(path));
				if (documentPath != null) {
					return Uri.decode(documentPath);
				}
			} catch (Exception err) {

			}
		}
		
		String filePath = null;
		Cursor cursor = null;
		try {
			if (path != null && path.contains("content://")) {
				cursor = context.getContentResolver().query(
						Uri.parse(path), null, null, null, null); // modified by
																	// smilefish
				if (cursor.getCount() == 0) {
					cursor.close();// RICHARD FIX MEMORY LEAK
					return null;
				}
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					System.out.println(i + "-----------------"
							+ cursor.getString(i));
				}
				filePath = cursor.getString(cursor.getColumnIndex("_data"));
			} else {
				filePath = path;
			}
			if (filePath != null) {
				// BEGIN: archie_huang@asus.com
				filePath = filePath.replace("file://", "");
				// END: archie_huang@asus.com
			}
		} catch (Exception e) {
			Log.e(TAG, "getRealFilePath error");
		}finally{
			if(cursor != null)
				cursor.close(); //smilefish add for memory leak
		}
		return Uri.decode(filePath);
	}
		
	public static final String getDefaultBookName(Context context) {
		int count = 1;
		Resources res = context.getResources();
		String defaultNamePrefix = res.getString(R.string.nb_name_default);
		String name = null;
		String[] projection = new String[] { MetaData.BookTable.TITLE };
		Cursor cursor = context.getContentResolver().query(
				MetaData.BookTable.uri, projection,
				"(userAccount = 0) OR (userAccount = ?)",
				new String[] { Long.toString(MetaData.CurUserAccount) }, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false) {
				name = cursor.getString(0);
				if (name != null && defaultNamePrefix != null
						&& name.contains(defaultNamePrefix)) {
					try {
						int number = Integer.parseInt(name.replace(
								defaultNamePrefix, ""));
						count = (number >= count) ? (++number) : count;
					} catch (NumberFormatException e) {
						Log.d(TAG, "not a number");
					}
				}
				cursor.moveToNext();
			}

		}
		// BEGIN: RICHARD
		if (cursor != null) {
			cursor.close();
		}
		// END: RICHARD
		return new String(defaultNamePrefix + count);
	}

	/**
	 * 获得文件编码格式。会先用jUniCharset.apk去判断，如果不能获得编码格式，则进一步使用getCharset方法
	 * 
	 * @param file
	 * @return
	 */
	public static String getTextFileCharset(File file) {
		String encode = getCharsetByjUniCharset(file);
		if( encode == null || encode.equalsIgnoreCase("Windows-1252")){ //smilefish fix bug 647289
			return getCharset(file);
		}
		
		return encode;
	}

	/**
	 * 使用jUniCharset库，识别文件编码
	 * 
	 * @author noah_zhang
	 * @param file
	 *            要分析的文件
	 **/
	private static String getCharsetByjUniCharset(File file) {
		byte[] buffer = new byte[4096];
		FileInputStream fis;
		String encoding = null;
		try {
			fis = new FileInputStream(file);
			UniversalDetector detector = new UniversalDetector(null);
			int read = 0;
			while ((read = fis.read(buffer)) > 0 && !detector.isDone()) {
				detector.handleData(buffer, 0, read);
			}
			detector.dataEnd();
			encoding = detector.getDetectedCharset();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return encoding;
	}
	
	//Emmanual
	public static String getCharset(File file) {
        String charset = "GB18030";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(
                  new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1){
            	bis.close();
                return charset;
            }
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1]
                == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1]
                    == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
				while ((read = bis.read()) != -1) {
					if (read >= 0xF0) {
						break;
					}
					if (0x80 <= read && read <= 0xBF) {
						break;
					}
					if (0xC0 <= read && read <= 0xDF) {
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) {
							continue;
						} else {
							break;
						}
                    } else if (0xE0 <= read && read <= 0xEF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }
}
