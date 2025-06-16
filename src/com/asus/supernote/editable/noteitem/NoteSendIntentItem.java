package com.asus.supernote.editable.noteitem;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ServiceManager;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.asus.supernote.EditorActivity;
import com.asus.supernote.EditorUiUtility;
import com.asus.supernote.InputManager;
import com.asus.supernote.R;
import com.asus.supernote.data.AsusFormat;
import com.asus.supernote.data.AsusFormatReader;
import com.asus.supernote.data.AsusFormatWriter;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.picker.PickerUtility;

public class NoteSendIntentItem extends ClickableSpan implements NoteItem, AsusFormat {

    private static final String TAG = "NoteSendIntentItem";
    public static final String FILENAME_EXTENSION_VIDEO_OLD = "3gp";
    public static final String FILENAME_EXTENSION_VIDEO = "mp4";
    public static final String FILENAME_EXTENSION_VOICE = "3gpp";
    public static final String FILENAME_EXTENSION_VOICE_AMR = "amr"; //add by mars_li
    public static final String INTENT_TYPE_VIDEO_OLD = "video/3gp";
    public static final String INTENT_TYPE_VIDEO = "video/mp4";
    public static final String INTENT_TYPE_VOICE = "audio/3gpp";
    public static final String INTENT_TYPE_VOICE_AMR = "audio/amr"; //add by mars_li
    
    //emmanual for voice file
    public static final String FILENAME_EXTENSION_VOICE_MP3 = "mp3";
    public static final String FILENAME_EXTENSION_VOICE_WAV = "wav";
    public static final String FILENAME_EXTENSION_VOICE_WMA = "wma";
    public static final String FILENAME_EXTENSION_VOICE_OGG = "ogg";
    public static final String FILENAME_EXTENSION_VOICE_MID = "mid";
    public static final String FILENAME_EXTENSION_VOICE_M4A = "m4a"; //add by smilefish fix bug 629045/629108
    public static final String INTENT_TYPE_VOICE_MP3 ="audio/mpeg";
    public static final String INTENT_TYPE_VOICE_WAV ="audio/x-wav";
    public static final String INTENT_TYPE_VOICE_WMA = "audio/x-ms-wma";
    public static final String INTENT_TYPE_VOICE_OGG = "application/ogg";
    public static final String INTENT_TYPE_VOICE_MID = "audio/midi";
    public static final String INTENT_TYPE_VOICE_M4A = "audio/mpeg"; //add by smilefish
    
    // BEGIN： Shane_Wang@asus.com 2012-11-20
    public static final String FILENAME_EXTENSION_VOICE_NEW = "aac";
    public static final String INTENT_TYPE_VOICE_NEW = "audio/3gpp/aac";
    // END： Shane_Wang@asus.com 2012-11-20

    private String mFileName = null;

    private Intent mIntent = null;

    private int mStart = -1;
    private int mEnd = -1;

    public NoteSendIntentItem() {
    }

    public NoteSendIntentItem(Intent intent) {
        mIntent = intent;
        if (mIntent != null) {
        	//add by mars_li
        	String path = Uri.decode(mIntent.getDataString()); //add for chinese path
        	//end mars_li
            int lastSlashPos = path.lastIndexOf("/");
            if (lastSlashPos >= 0) {
                mFileName = path.substring(lastSlashPos + 1, path.length());
            }
        }
    }

    Long mLastClickTime = 0l;//emmanual
    @Override
    public void onClick(View widget) {
    	//emmanual to avoid double click event
		if (System.currentTimeMillis() - mLastClickTime < 1000) {
			return;
		}
		mLastClickTime = System.currentTimeMillis();
		
        // BEGIN: archie_huang@asus.com
        // To avoid NullPointerException
        Context context = EditorUiUtility.getContextStatic();
        if (mIntent == null || context == null) {
            return;
        }
        // END: archie_huang@asus.com
        //begin smilefish fix bug 317338
//        if(isPhoneInUse()){
//        	EditorActivity.showToast(context, R.string.cannot_play_media_when_calling);
//        	return;
//        }
        //end smilefish
        try {
            PickerUtility.lockRotation((Activity) context);
            ((Activity) context).startActivityForResult(mIntent, InputManager.RESULT_VIEW);
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(widget.getContext(),
                    widget.getContext().getResources().getString(R.string.warning_activity_not_found),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        catch (StringIndexOutOfBoundsException e){ //smilefish fix bug 698902
        	Toast.makeText(widget.getContext(),
                    widget.getContext().getResources().getString(R.string.warning_activity_not_found),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    //begin smilefish
//    private boolean isPhoneInUse(){
//        boolean phoneInUse = false; 
//        if(android.os.Build.VERSION.SDK_INT > 22) return false; //fix android M NoSuchMethodError
//        try { 
//        	ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone")); 
//        	if (phone != null) phoneInUse = !phone.isIdle(); 
//        } catch (Exception e) { 
//        	Log.w(TAG, "phone.isIdle() failed", e); 
//        } 
//        return phoneInUse;
//    }
    //end smilefish

    @Override
    public Serializable save() {
        NoteSendIntentItemSavedData nsiisd = new NoteSendIntentItemSavedData();

        nsiisd.mFileName = mFileName;

        nsiisd.mStart = mStart;
        nsiisd.mEnd = mEnd;

        nsiisd.mOuterClassName = this.getClass().getName();
        return nsiisd;
    }

    @Override
    public void load(Serializable s, PageEditor pe) {
        NoteSendIntentItemSavedData nsiisd = (NoteSendIntentItemSavedData) s;

        mFileName = nsiisd.mFileName;
        if(pe!=null)
        prepareIntent(pe);

        mStart = nsiisd.mStart;
        mEnd = nsiisd.mEnd;
    }

    @Override
    public String getText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getStart() {
        return mStart;
    }

    @Override
    public int getEnd() {
        return mEnd;
    }

    @Override
    public void setStart(int start) {
        mStart = start;

    }

    @Override
    public void setEnd(int end) {
        mEnd = end;
    }

    @SuppressWarnings( "serial" )
    public static class NoteSendIntentItemSavedData implements Serializable, NoteItem.NoteItemSaveData {

        public String mFileName;

        public int mStart = -1;
        public int mEnd = -1;

        public String mOuterClassName = null;

        @Override
        public String getOuterClassName() {
            return mOuterClassName;
        }
    }

    @Override
    public void itemSave(AsusFormatWriter afw) throws IOException {
    	
        if (mIntent == null || mIntent.getDataString() == null) {
            return;
        }
        //add by mars_li
        String path = Uri.decode(mIntent.getDataString()); //add support for Chinese path
        // end mars_li
        int lastSlashPos = path.lastIndexOf("/");
        if (lastSlashPos == -1) {
            Log.w(TAG, "Do not support to save this file : " + mIntent.getDataString());
            return;
        }
        afw.writeByteArray(SNF_NITEM_SENDINTENT_BEGIN, null, 0, 0);
        afw.writeString(SNF_NITEM_SENDINTENT_FILE_NAME, path.substring(lastSlashPos + 1, path.length()));
        afw.writeInt(SNF_NITEM_SENDINTENT_POS_START, mStart);
        afw.writeInt(SNF_NITEM_SENDINTENT_POS_END, mEnd);
        afw.writeByteArray(SNF_NITEM_SENDINTENT_END, null, 0, 0);
    }

    @Override
    public void itemLoad(AsusFormatReader afr) throws IOException {
        mIntent = null;
        mFileName = null;

        AsusFormatReader.Item item = afr.readItem();
        while (item != null) {
            switch (item.getId()) {
                case SNF_NITEM_SENDINTENT_BEGIN:
                    break;
                case SNF_NITEM_SENDINTENT_FILE_NAME:
                    mFileName = item.getStringValue();
                    break;
                case SNF_NITEM_SENDINTENT_POS_START:
                    mStart = item.getIntValue();
                    break;
                case SNF_NITEM_SENDINTENT_POS_END:
                    mEnd = item.getIntValue();
                    break;
                case SNF_NITEM_SENDINTENT_END:
                    return;
                default:
                    Log.w(TAG, "Unknow id = 0x" + Integer.toHexString(item.getId()));
                    break;
            }
            item = afr.readItem();
        }
    }
    public String getIntentType(){
    	 String intentType = null;
    	 if (mFileName != null) {
             int dotPos = mFileName.lastIndexOf(".");
             if (dotPos == -1 || dotPos + 1 == mFileName.length()) {
                 Log.w(TAG, "Do not support to open this file : " + mFileName);
                 return null;
             }
             String extension = mFileName.substring(dotPos + 1, mFileName.length());

             if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VIDEO)) {
                 intentType = INTENT_TYPE_VIDEO;
             } else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VIDEO_OLD)) {
                 intentType = INTENT_TYPE_VIDEO_OLD;
             }
             else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE)) {
                 intentType = INTENT_TYPE_VOICE;
             }
             // BEGIN： Shane_Wang@asus.com 2012-11-20
             else if(extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_NEW)) {
            	 intentType = INTENT_TYPE_VOICE_NEW;
             }
           //add by mars for amr support
             else if(extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_AMR)){
             	intentType = INTENT_TYPE_VOICE_AMR;
             }
             // END： Shane_Wang@asus.com 2012-11-20
             //add by emmanual for audio files
			else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_MP3)) {
				intentType = INTENT_TYPE_VOICE_MP3;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_WMA)) {
				intentType = INTENT_TYPE_VOICE_WMA;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_WAV)) {
				intentType = INTENT_TYPE_VOICE_WAV;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_OGG)) {
				intentType = INTENT_TYPE_VOICE_OGG;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_MID)) {
				intentType = INTENT_TYPE_VOICE_MID;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_M4A)) {
				intentType = INTENT_TYPE_VOICE_M4A;
			}
            
             else {
                 Log.w(TAG, "Do not support to open this file : " + mFileName);
                 return null;
             }
    	 }
    	 return intentType;
    }
    public void prepareIntent(PageEditor pageEditor) {
        if (mFileName != null) {
            int dotPos = mFileName.lastIndexOf(".");
            if (dotPos == -1 || dotPos + 1 == mFileName.length()) {
                Log.w(TAG, "Do not support to open this file : " + mFileName);
                return;
            }
            String extension = mFileName.substring(dotPos + 1, mFileName.length());

            String intentType = null;
            if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VIDEO)) {
                intentType = INTENT_TYPE_VIDEO;
            } else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VIDEO_OLD)) {
                intentType = INTENT_TYPE_VIDEO_OLD;
            }
            else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE)) {
                intentType = INTENT_TYPE_VOICE;
            }
            // BEGIN： Shane_Wang@asus.com 2012-11-20
            else if(extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_NEW)) {
           	 intentType = INTENT_TYPE_VOICE_NEW;
            }
          //add by mars for amr support
            else if(extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_AMR)){
            	intentType = INTENT_TYPE_VOICE_AMR;
            }
            // END： Shane_Wang@asus.com 2012-11-20
            //add by emmanual for audio files
			else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_MP3)) {
				intentType = INTENT_TYPE_VOICE_MP3;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_WMA)) {
				intentType = INTENT_TYPE_VOICE_WMA;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_WAV)) {
				intentType = INTENT_TYPE_VOICE_WAV;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_OGG)) {
				intentType = INTENT_TYPE_VOICE_OGG;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_MID)) {
				intentType = INTENT_TYPE_VOICE_MID;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_M4A)) {
				intentType = INTENT_TYPE_VOICE_M4A;
			}
            
            else {
                Log.w(TAG, "Do not support to open this file : " + mFileName);
                return;
            }

            Uri uri = Uri.fromFile(new File(pageEditor.getFilePath(), mFileName));//darwin
            try {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(uri, intentType);//darwin //Uri.parse(fullPath)
				mIntent = intent;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    // BEGIN: Better
    public void prepareIntent(NotePage notePage) {
        if (mFileName != null) {
            int dotPos = mFileName.lastIndexOf(".");
            if (dotPos == -1 || dotPos + 1 == mFileName.length()) {
                Log.w(TAG, "Do not support to open this file : " + mFileName);
                return;
            }
            String extension = mFileName.substring(dotPos + 1, mFileName.length());

            String intentType = null;
            if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VIDEO)) {
                intentType = INTENT_TYPE_VIDEO;
            } else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VIDEO_OLD)) {
                intentType = INTENT_TYPE_VIDEO_OLD;
            }
            else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE)) {
                intentType = INTENT_TYPE_VOICE;
            }
            // BEGIN： Shane_Wang@asus.com 2012-11-20
            else if(extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_NEW)) {
           	 intentType = INTENT_TYPE_VOICE_NEW;
            } 
            //add by mars for amr support
            else if(extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_AMR)){
            	intentType = INTENT_TYPE_VOICE_AMR;
            }
            // END： Shane_Wang@asus.com 2012-11-20
            //add by emmanual for audio files
			else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_MP3)) {
				intentType = INTENT_TYPE_VOICE_MP3;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_WMA)) {
				intentType = INTENT_TYPE_VOICE_WMA;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_WAV)) {
				intentType = INTENT_TYPE_VOICE_WAV;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_OGG)) {
				intentType = INTENT_TYPE_VOICE_OGG;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_MID)) {
				intentType = INTENT_TYPE_VOICE_MID;
			} else if (extension.equalsIgnoreCase(FILENAME_EXTENSION_VOICE_M4A)) {
				intentType = INTENT_TYPE_VOICE_M4A;
			}
            
            else {
                Log.w(TAG, "Do not support to open this file : " + mFileName);
                return;
            }

            //String fullPath = "file:///" + notePage.getFilePath() + "//" + mFileName;//darwin

            Uri uri = Uri.fromFile(new File(notePage.getFilePath(), mFileName));//darwin
            try {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(uri, intentType);//darwin//Uri.parse(fullPath)
				mIntent = intent;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    // END: Better

    public Intent getIntent() {
        return mIntent;
    }

    public String getFileName() {
        return mFileName;
    }
    //begin jason
    private long sizeCache=0;
    public long getFileSize(PageEditor pageEditor){
    	if (sizeCache == 0) {
			File file=new File(pageEditor.getFilePath(), mFileName);
			if (file.exists()&&file.isFile()&&file.length()>0) {
				sizeCache = file.length();
			}
		}
    	return sizeCache;
    }
    
    //end jason
}
