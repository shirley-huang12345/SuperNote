package com.asus.supernote.recorder;

import java.io.File;
import java.io.IOException;

import com.asus.supernote.R;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.editable.PageEditorManager;
import com.asus.supernote.editable.noteitem.AttacherTool;
import com.asus.supernote.editable.noteitem.NoteImageItem;
import com.asus.supernote.editable.noteitem.NoteSendIntentItem;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

public class Recorder {
	final static private int MAX_FILENAME_INDEX_COUNT = 99999999;
	
	public static final int IDLE_STATE = 0;
    public static final int RECORDING_STATE = 1;
    
    public static final int SDCARD_ACCESS_ERROR = 0;
    public static final int INTERNAL_ERROR_START = 1;
    
    private static final String OBJ = String.valueOf((char) 65532);
    
    public interface OnStateChangedListener {
        public void onStateChanged(int state);
        public void onError(int error);
    }
    OnStateChangedListener mOnStateChangedListener = null;
    
	private ImageButton mRecordButton = null;
	private TextView mRecordTimeView = null;
	private int mState = IDLE_STATE;
	
	private File mRecordFile = null;
	private MediaRecorder mRecorder = null;
	private PageEditorManager mPageEditorManager = null;
	private Context mContext = null;
	private PopupWindow mRecorderView = null;
	
    private String mInitTime = "00:00:00";
    private String mTimerFormat = "%02d:%02d:%02d";
    private long mRecordStart = 0;
    private int mRecordLength = 0;
	
    final Handler mHandler = new Handler();
    Runnable mUpdateTimer = new Runnable() {
        public void run() { updateRecordTime(); }
    };
    
    public Recorder(PageEditorManager pageEditorManager, Context context, View parentView, String folderPath) {
    	mPageEditorManager = pageEditorManager;
    	mContext = context;
    	
    	showRecorder(parentView, folderPath);
    }
    
    public void showRecorder(View parentView, final String folderPath){
    	LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewContainer = inflater.inflate(R.layout.recorder_layout, null, false);
		mRecorderView = new PopupWindow(viewContainer,
		        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);

		mRecordButton = (ImageButton)viewContainer.findViewById(R.id.recorder_button);
		mRecordButton.setSelected(false);
		mRecordButton.setOnClickListener(new OnClickListener(){

			@Override
            public void onClick(View v) {
	            if(mState == IDLE_STATE){
	            	mRecordButton.setSelected(true);
	            	mRecordLength = 0;
	            	startRecording(folderPath);
	            	updateRecordTime();
	            }else{
	            	mRecordButton.setSelected(false);
	            	stopRecording();
	            	mRecorderView.dismiss();
	            	addItemToEditText();
	            }
	            
            }
			
		});
		
		mRecordTimeView = (TextView)viewContainer.findViewById(R.id.record_time);
		mRecordTimeView.setText(mInitTime);
		
		mRecordTimeView.setOnTouchListener(new View.OnTouchListener(){
			private int dx = 0;
			private int dy = 0;
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					dx = (int)event.getX();
					dy = (int)event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					int xp = (int)event.getRawX() - dx;
					int yp = (int)event.getRawY() - dy;
					mRecorderView.update(xp, yp, -1, -1, true);
					break;
				}
				return true;
			}
		});
		
		mRecorderView.showAtLocation(parentView, Gravity.START | Gravity.TOP, 0, 300);
    }
    
    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mOnStateChangedListener = listener;
    }
    
    private void signalStateChanged(int state) {
        if (mOnStateChangedListener != null)
            mOnStateChangedListener.onStateChanged(state);
    }
    
    private void setError(int error) {
        if (mOnStateChangedListener != null)
            mOnStateChangedListener.onError(error);
    }
    
    private void updateRecordTime(){
    	long time = (SystemClock.elapsedRealtime() - mRecordStart)/1000;
        String timeStr = String.format(mTimerFormat, time/60/60,  (time / 60) % 60, time%60);
        mRecordTimeView.setText(timeStr);
        
        long delayTime = 500;
        mHandler.postDelayed(mUpdateTimer, delayTime);
    }
    
    private void startRecording(String folderPath) {
    	stopRecording();
    	
    	String filename = getVoiceName(folderPath);
        try {
        	mRecordFile = new File(folderPath, filename);
            if (!mRecordFile.exists()){
            	mRecordFile.createNewFile();
            } else
                throw new IOException("Duplicate filename!");

        } catch (IOException e) {
            setError(SDCARD_ACCESS_ERROR);
            return;
        }
        
    	mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mRecordFile.getAbsolutePath());
        
        try {
            mRecorder.prepare();
        } catch(IOException exception) {
            delete();
            setError(INTERNAL_ERROR_START);
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
            return;
        }
        
        try {
            mRecorder.start();
        } catch (RuntimeException exception) {
            delete(); 
            setError(INTERNAL_ERROR_START);
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
            return;
        }
        
        mRecordStart = SystemClock.elapsedRealtime();
        setState(RECORDING_STATE);
    }
    
    private void stopRecording() {
        if (mRecorder == null)
            return;
        
        mHandler.removeCallbacks(mUpdateTimer);
        
        long time = SystemClock.elapsedRealtime() - mRecordStart;
        mRecordLength = (int)( time/1000 );  // FIXME: should read recorded length from codec
        double timed = time/1000.0;
        int timeInt = ((int)timed);
        double timeMid = timeInt + 0.5;
        if(timed >= timeMid)
        {
            mRecordLength =(int)Math.ceil(( timed));
        }
        else 
        {
            mRecordLength =(int)Math.floor((timed));
        }
        if(timeInt == 0)
        {
            mRecordLength = 1;
        }
        
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null; 
       } catch (Exception e) {
           e.printStackTrace();
       }
       setState(IDLE_STATE);
    }
    
    private void setState(int state) {
        if (state == mState)
            return;
        
        mState = state;
        signalStateChanged(mState);
    }
    
    private void delete() {
    	stopRecording();
        
        if (mRecordFile != null)
            mRecordFile.delete();

        mRecordFile = null;
        
        signalStateChanged(IDLE_STATE);
    }
    
    private void addItemToEditText(){
    	Intent intent = new Intent();
		  intent.setAction(Intent.ACTION_VIEW);
		  
        Uri uri = Uri.fromFile(mRecordFile);//darwin
		  intent.setDataAndType(uri, NoteSendIntentItem.INTENT_TYPE_VOICE);//Uri.parse(fullPath)//darwin
		
		  NoteSendIntentItem item = new NoteSendIntentItem(intent);
		  
		  String fullPath = mRecordFile.getAbsolutePath();
				  
		  //Begin Dave.To modify voice/video attacher UI.  
		  AttacherTool tool = new AttacherTool();
		  String timeStr = String.format(mTimerFormat, mRecordLength/60/60,  (mRecordLength / 60) % 60, mRecordLength%60);
		  timeStr = "(" + timeStr + ")";
		  String imageItemInfo = tool.getFileNameNoEx(mRecordFile.getName()) + timeStr;
		  NoteImageItem icon = new NoteImageItem(false, mPageEditorManager.getCurrentPageEditor().getImageSpanHeight(),imageItemInfo);
		  //End Dave.
		  // BEGIN ryan_lin@asus.com, add for scribble space
		  SpannableString spannableString = new SpannableString(OBJ);
		  // END ryan_lin@asus.com
		  spannableString.setSpan(item, 0, OBJ.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		  spannableString.setSpan(icon, 0, OBJ.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		  //Begin Darwin_Yu@asus.com
		  String sfileName = item.getFileName();
		  mPageEditorManager.getCurrentPageEditor().addItemToEditText(spannableString,sfileName);
		  
		  mPageEditorManager.reflashScreen();
    }
    
    private String getVoiceName(String folderPath){
    	File voiceDir ;
        if(folderPath!=null)                
        {  
        	voiceDir = new File(folderPath); 
        	File[] files = voiceDir.listFiles();
        	
        	String prefixString = mContext.getString(R.string.voice);
        	String extension = ".3gpp"; 
        	for (int i = 1; i < MAX_FILENAME_INDEX_COUNT; i++) {
                //add into space between prefix and number for recording filename.
                String filename = prefixString + " " + String.valueOf(i);//+extension;
                if(MethodUtils.isFileNameOk(filename, files))
                {
                    return filename + extension;
                }
            }
        }
    	
        return null;
    }
    

}
