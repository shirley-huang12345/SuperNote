package com.asus.supernote.editable.attacher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.EditorUiUtility;
import com.asus.supernote.R;
import com.asus.supernote.editable.PageEditorManager;
import com.asus.supernote.editable.noteitem.AttacherTool;
import com.asus.supernote.editable.noteitem.NoteImageItem;
import com.asus.supernote.editable.noteitem.NoteSendIntentItem;
import com.asus.supernote.picker.PickerUtility;

public class VoiceAttacher{

    public static final String DATA = "VOICE_DATA_PATH";
    private static final String TAG = "VoiceAttacher";

    private static final String OBJ = String.valueOf((char) 65532);
    
    public static Intent getIntent(PageEditorManager pageEditorManager) {
    	try {
			Intent intent = new Intent();
			intent.setAction(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
			intent.setType("audio/3gpp");
			intent.setClassName("com.asus.soundrecorder", "com.asus.soundrecorder.SoundRecorder");
			intent.putExtra(MediaStore.EXTRA_OUTPUT, pageEditorManager.getCurrentPageEditor().getFilePath());
			return intent;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
	  public static void attachItem(Intent data, PageEditorManager pageEditorManager) {
		  Intent intent = new Intent();
		  intent.setAction(Intent.ACTION_VIEW);
		  String path = PickerUtility.getRealFilePath(EditorUiUtility.getContextStatic(), data.getDataString());
		  //add by mars fixed for sounder update
		  if(path == null){
			  return;
		  }
		  File f=new File(path);
          if(!f.exists()){
                  return ;
          }
          //end mars_li
		  int lastSlashPos = path.lastIndexOf("/");
		  String fileName = null;
		  if (lastSlashPos >= 0) {
		      fileName = path.substring(lastSlashPos + 1, path.length());
		  }
		  
		  //Begin Dave.To modify voice/video attacher UI.  
		  String fullPath = pageEditorManager.getCurrentPageEditor().getFilePath() + "//" + fileName;
		  //End Dave.
		  
		  Uri uri = Uri.fromFile(new File(pageEditorManager.getCurrentPageEditor().getFilePath(), fileName));//darwin
		  intent.setDataAndType(uri, NoteSendIntentItem.INTENT_TYPE_VOICE);//Uri.parse(fullPath)//darwin
		
		  NoteSendIntentItem item = new NoteSendIntentItem(intent);
		  
		  //Begin Dave.To modify voice/video attacher UI.  
		  AttacherTool tool = new AttacherTool();
		  String imageItemInfo = tool.getFileNameNoEx(fileName) + tool.getElapsedTime(fullPath);
		  NoteImageItem icon = new NoteImageItem(false, pageEditorManager.getCurrentPageEditor().getImageSpanHeight(),imageItemInfo);
		  //End Dave.
		  // BEGIN ryan_lin@asus.com, add for scribble space
		  SpannableString spannableString = new SpannableString(OBJ);
		  // END ryan_lin@asus.com
		  spannableString.setSpan(item, 0, OBJ.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		  spannableString.setSpan(icon, 0, OBJ.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		  //Begin Darwin_Yu@asus.com
		  String sfileName = item.getFileName();
		  pageEditorManager.getCurrentPageEditor().addItemToEditText(spannableString,sfileName);
		  //End   Darwin_Yu@asus.com
		  Log.i(TAG, "attachItem");
	}
	    
	public static void attachFile(Intent data,
	        PageEditorManager pageEditorManager) {
		if (data != null && data.getData() != null) {
		    String uri = data.getData().toString();
		    if(uri != null&&uri.startsWith("content://com.google.android.apps.docs.storage")){
		        EditorActivity.showToast(EditorUiUtility.getContextStatic(),
	                    R.string.prompt_err_open);
	            return;
		    }
		}
		
		String path = PickerUtility.getRealFilePath(
		        EditorUiUtility.getContextStatic(), data.getDataString());
		attachFile(path, pageEditorManager);
	}
	
	public static void attachFile(String path,
	        PageEditorManager pageEditorManager) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		if (path == null) {
			Context context = pageEditorManager.getCurrentPageEditor()
			        .getEditorUiUtility().getContext();
			EditorActivity.showToast(context,
			        context.getString(R.string.audiofile_not_exist));
			return;
		}
		// emmanual to fix bug 481775
		if (path.endsWith(".mp4") || path.endsWith(".MP4")) {
			Context context = pageEditorManager.getCurrentPageEditor()
			        .getEditorUiUtility().getContext();
			EditorActivity.showToast(context,
			        context.getString(R.string.audiofile_not_mp4));
			return;
		}
		File f = new File(path);
		if (!f.exists()) {
			Context context = pageEditorManager.getCurrentPageEditor()
			        .getEditorUiUtility().getContext();
			EditorActivity.showToast(context,
			        context.getString(R.string.audiofile_not_exist) + "\n" + f);
			return;
		}
		int lastSlashPos = path.lastIndexOf("/");
		String fileName = null;
		if (lastSlashPos >= 0) {
			fileName = path.substring(lastSlashPos + 1, path.length());
		}

		String fullPath = pageEditorManager.getCurrentPageEditor()
		        .getFilePath() + "//" + fileName;

		File file = new File(fullPath);
		if (!file.exists()) {
			try {
				FileChannel srcChannel = new FileInputStream(f).getChannel();
				FileChannel destChannel = new FileOutputStream(file)
				        .getChannel();
				destChannel.transferFrom(srcChannel, 0, srcChannel.size());
				srcChannel.close();
				destChannel.close();
			} catch (FileNotFoundException e) {
				return;
			} catch (IOException e) {
				return;
			}
		} else {
			File[] filelist = file.getParentFile().listFiles();
			// 判断同源文件是否存在
			boolean exist = false;
			for (File tf : filelist) {
				String tfName = tf.getName();
				boolean prename = tfName.lastIndexOf("_") > 0
				        && (tfName.subSequence(0, tfName.lastIndexOf("_")) + tfName
				                .substring(tfName.lastIndexOf('.')))
				                .equals(fileName);
				if ((tfName.equals(fileName) || prename)
				        && tf.length() == f.length()) {
					exist = true;
					file = tf;
					fileName = tfName;
					fullPath = tf.getPath();
					break;
				}
			}
			if (!exist) {
				// 复制文件并重命名
				boolean repeat = true;
				int index = 0;
				while (repeat) {
					index += 1;
					repeat = false;
					for (File tf : filelist) {
						String tfName = tf.getName();
						String tempName = fileName.substring(0,
						        fileName.lastIndexOf('.'))
						        + "_"
						        + index
						        + fileName.substring(fileName.lastIndexOf('.'));
						if (tfName.equals(tempName)) {
							repeat = true;
							break;
						}
					}
				}
				try {
					fileName = fileName.substring(0, fileName.lastIndexOf('.'))
					        + "_" + index
					        + fileName.substring(fileName.lastIndexOf('.'));
					fullPath = pageEditorManager.getCurrentPageEditor()
					        .getFilePath() + "//" + fileName;
					file = new File(fullPath);
					FileChannel srcChannel = new FileInputStream(f)
					        .getChannel();
					FileChannel destChannel = new FileOutputStream(file)
					        .getChannel();
					destChannel.transferFrom(srcChannel, 0, srcChannel.size());
					srcChannel.close();
					destChannel.close();
				} catch (FileNotFoundException e) {
					return;
				} catch (IOException e) {
					return;
				}
			}
		}

		Uri uri = Uri.fromFile(new File(pageEditorManager
		        .getCurrentPageEditor().getFilePath(), fileName));// darwin
		intent.setDataAndType(uri, NoteSendIntentItem.INTENT_TYPE_VOICE);// Uri.parse(fullPath)//darwin

		NoteSendIntentItem item = new NoteSendIntentItem(intent);

		// Begin Dave.To modify voice/video attacher UI.
		AttacherTool tool = new AttacherTool();
		String imageItemInfo = tool.getFileNameNoEx(fileName)
		        + tool.getElapsedTime(fullPath);
		NoteImageItem icon = new NoteImageItem(false, pageEditorManager
		        .getCurrentPageEditor().getImageSpanHeight(), imageItemInfo);
		// End Dave.
		// BEGIN ryan_lin@asus.com, add for scribble space
		SpannableString spannableString = new SpannableString(OBJ);
		// END ryan_lin@asus.com
		spannableString.setSpan(item, 0, OBJ.length(),
		        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannableString.setSpan(icon, 0, OBJ.length(),
		        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// Begin Darwin_Yu@asus.com
		String sfileName = item.getFileName();
		pageEditorManager.getCurrentPageEditor().addItemToEditText(
		        spannableString, sfileName);
		// End Darwin_Yu@asus.com
	}
}
