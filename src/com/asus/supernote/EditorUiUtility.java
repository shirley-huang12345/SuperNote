package com.asus.supernote;

import java.util.Date;
import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.editable.IHandWritePanel;
import com.asus.supernote.editable.NoteEditText;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.PageEditorManager;
import com.asus.supernote.editable.noteitem.NoteForegroundColorItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteBaselineItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem.PathInfo;
import com.asus.supernote.fota.LoadPageTaskVersionTwo;
import com.asus.supernote.ga.GACollector;
import com.asus.supernote.recorder.Recorder;

public class EditorUiUtility implements Recorder.OnStateChangedListener{

    private static final String TAG = "EditorUiUtility";

    private static Context sContext = null;
    // BEGIN: archie_huang@asus.com
    private static int sObjectCount = 0;
    // END: archie_huang@asus.com
    public static final int SCROLLING_BUTTON_OFFSET = 70;

    private EditorActivity mEditorActivity = null;
    private PageEditor mPageEditor = null;
    private PageEditorManager mPageEditorManager = null;
    private InputManager mInputManager = null;
    private IHandWritePanel mHandWritePanel = null;
    private Recorder mRecorder = null;

    private SharedPreferences mPreference = null;
    private SharedPreferences.Editor mPrefEditor = null;

    private boolean mIsClipBoardAvailable = false;
    private boolean mIsDoodleItemAvailable = false;
    private boolean mIsDoodleGroupAvailable = false;
    private boolean mIsNoteEditTextUndoStackAvailable = false;
    private boolean mIsNoteEditTextRedoStackAvailable = false;
    private boolean mIsBoxEditorTextUndoStackAvailable = false;
    private boolean mIsBoxEditorTextRedoStackAvailable = false;
    private boolean mIsDoodleUndoStackAvailable = false;
    private boolean mIsDoodleRedoStackAvailable = false;
    private boolean mIsDoodlePastAvailable = false;
    private boolean mIsDoodleCropAvailable = false; 	//By Show
    private boolean mIsDoodleTextEditAvailable = false; //By Show
    private boolean mIsDoodleChangeImgAvailable = false; //By Show
    private static LoadPageTaskVersionTwo mLoadPageTask = null; //by show
	public void addContent() {
		//BEGIN: RICHARD TEST
		mPageEditor = mPageEditorManager.getCurrentPageEditor();
		//END: RICHARD
    	LinkedList<PathInfo> pathList = new LinkedList<PathInfo>();
    	short[] pointArray1 = new short[] {22, 3, 19, 6, 13, 18, 10, 30, 7, 42, 6, 53, 7, 71, 9, 72, 15, 69, 21, 62, 28, 48, 34, 34, 41, 24, 44, 18, 45, 16, 45, 16, 44, 25, 42, 41, 44, 66, 45, 69, 48, 71, 57, 66, 65, 56, 71, 44, 75, 33, 79, 21, 80, 15, 80, 4,};
    	short[] pointArray2 = new short[]{85, 54, 83, 57, 91, 59, 101, 54, 109, 50, 113, 45, 116, 41, 120, 36, 118, 30, 106, 33, 100, 39, 94, 47, 89, 54, 88, 60, 89, 68, 95, 71, 109, 69, 113, 68, 116, 66, 118, 65, 118, 63, };
    	short[] pointArray3 = new short[]{170, 1, 168, 1, 165, 10, 161, 19, 156, 31, 151, 44, 148, 54, 145, 62, 142, 68, 141, 71, 141, 75, 139, 75,};
    	short[] pointArray4 = new short[]{209, 34, 209, 33, 208, 31, 194, 34, 188, 39, 185, 44, 180, 51, 177, 59, 180, 71, 183, 74, 195, 72, 202, 68, 205, 65, 208, 62, 208, 60,};
    	short[] pointArray5 = new short[]{243, 34, 241, 34, 235, 39, 230, 45, 226, 54, 224, 62, 227, 71, 229, 74, 243, 69, 249, 63, 255, 54, 259, 48, 261, 42, 258, 36, 253, 34, 243, 36, 240, 37, 238, 39,};
    	short[] pointArray6 = new short[]{285, 31, 285, 30, 284, 39, 281, 47, 276, 54, 273, 62, 270, 66, 270, 68, 270, 63, 276, 54, 282, 45, 287, 41, 291, 36, 294, 34, 297, 34, 299, 45, 297, 53, 296, 59, 293, 63, 291, 66, 290, 69, 293, 62, 297, 54, 300, 50, 305, 45, 309, 42, 312, 39, 316, 39, 314, 57, 312, 63, 311, 68, 309, 71, 308, 74, 308, 74, };
    	short[] pointArray7 = new short[]{338, 54, 349, 54, 355, 51, 363, 48, 367, 45, 370, 42, 373, 39, 370, 33, 360, 34, 352, 39, 347, 42, 343, 48, 340, 54, 340, 65, 341, 68, 347, 71, 363, 69, 367, 68, 370, 66, 372, 65, 372, 65,};
    	pathList.add(new PathInfo(pointArray1));
    	pathList.add(new PathInfo(pointArray2));
    	pathList.add(new PathInfo(pointArray3));
    	pathList.add(new PathInfo(pointArray4));
    	pathList.add(new PathInfo(pointArray5));
    	pathList.add(new PathInfo(pointArray6));
    	pathList.add(new PathInfo(pointArray7));
    	Paint paint = new Paint(mPageEditor.getEditorPaint());
    	paint.setColor(Color.BLUE);
    	NoteHandWriteBaselineItem span = new NoteHandWriteBaselineItem(pathList, paint);
    	SpannableStringBuilder msg = new SpannableStringBuilder(String.valueOf((char) 65532));
    	msg.setSpan(span, 0, msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    	mPageEditor.setFontSize(msg);
    	mPageEditor.addItemToEditText(msg);
    }

	//BEGIN: RICHARD
    private boolean mIsAutoChangeWriteToType = false;
    
    public boolean getIsAutoChangeWriteToType()
    {
    	return mIsAutoChangeWriteToType;
    }
    
    public void setIsAutoChangeWriteToType(Boolean flag)
    {
    	mIsAutoChangeWriteToType = flag;
    }
	//END: RICHARD
    
    //begin jason
    private boolean mIsStylusInputOnly = false;
    
    public boolean getmIsStylusInputOnly()
    {
    	return mIsStylusInputOnly;
    }
    
    public void setmIsStylusInputOnly(Boolean flag)
    {
    	mIsStylusInputOnly = flag;
    }
    //end jason    

  //BEGIN:shaun_xu@asus.com
    public Paint getDoodlePaint()
    {
		//BEGIN: RICHARD TEST
		mPageEditor = mPageEditorManager.getCurrentPageEditor();
		//END: RICHARD
    	return mPageEditor.getDoodlePaint();
    }
    //END:shaun_xu@asus.com

    public void resumeScrollChange() {
		//BEGIN: RICHARD TEST
		mPageEditorManager.resumeScrollChange();
		//END: RICHARD
    }

    //darwin
    public void setSelectAllText()
    {
    	mPageEditorManager.setSelectAllText();
    }
    //darwin
    public EditorUiUtility(EditorActivity editorActivity) {
        sContext = editorActivity;
        // BEGIN: archie_huang@asus.com
        sObjectCount++;
        // END: archie_huang@asus.com
        mEditorActivity = editorActivity;

        mPageEditorManager = new PageEditorManager(this);//RICHARD
        //mPageEditor = new PageEditor(this);
        mPageEditor = mPageEditorManager.getFirstPageEditor();
        for(PageEditor pe : mPageEditorManager.getPageEditorList())
        {
	        pe.setClipBoardAvailableListener(mOnClipBoardAvailableListener);
	        pe.setDoodleItemAvailableListener(mOnDoodleItemAvailableListener);
	        pe.setUndoStackAvailableListener(mOnUndoStackEmptyListener);
	        pe.setRedoStackAvailableListener(mOnRedoStackAvailableListener);
	        pe.setDoodleItemSelectListener(mOnDoodleItemSelectListener);
	        pe.setEditorColorChangeListener(mOnEditorColorChangeListener);
	        pe.setEditorBoldChangeListener(mOnEditorBoldChangeListener);
	        
	        pe.checkSystemClipBoard();
	        
        }

        //mInputManager = new InputManager(this, mPageEditor);
        mInputManager = new InputManager(this, mPageEditorManager);

        mHandWritePanel = mInputManager.getHandWritingView();
        //after HandWriteingView init then register listener
        mPageEditor.registerEnableListener();

        mPreference = editorActivity.getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE);
        mPrefEditor = mPreference.edit();
    }

    // BEGIN: archie_huang@asus.com
    // To avoid memory leak
    public void unbindResources() {
        sObjectCount--;
        if (sObjectCount == 0) {
            sContext = null;
        }
        mPageEditorManager.unbindResources();//RICHARD Modify
        mHandWritePanel.close();
    }

    // END: archie_huang@asus.com

    static public Context getContextStatic() {
        if (sContext == null) {
            Log.e(TAG, "sContext == null");
        }
        return sContext;
    }

    public Context getContext() {
        return mEditorActivity;
    }

    public InputManager getInputManager() {
        return mInputManager;
    }

    public int getInputModeFromPreference() {
        return mPreference.getInt(MetaData.PREFERENCE_INPUT_MODE_KEY, 
        		MetaData.isATT()? InputManager.INPUT_METHOD_KEYBOARD:InputManager.INPUT_METHOD_SCRIBBLE);
    }

    public void cleanEdited() {
        mPageEditorManager.cleanEdited();
    }

    public void setInputMode(int inputMode) {
        if (mInputManager.getInputMode() == InputManager.INPUT_METHOD_SELECTION_TEXT) {
            mPageEditorManager.quitSelectionTextMode();
        }
        if (inputMode != InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD 
            	&&inputMode != InputManager.INPUT_METHOD_TEXT_IMG_SCRIBBLE ) {
			//emmanual to fix bug 407909
				mEditorActivity.getWindow().setSoftInputMode(
			         WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
			             | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        		mPageEditorManager.cancleBoxEditor();
            }
        mInputManager.setInputMode(inputMode);
        if (inputMode == InputManager.INPUT_METHOD_KEYBOARD ||
                inputMode == InputManager.INPUT_METHOD_SCRIBBLE ||
                inputMode == InputManager.INPUT_METHOD_DOODLE) {
            mPrefEditor.putInt(MetaData.PREFERENCE_INPUT_MODE_KEY, inputMode).commit();
        }

        if (inputMode == InputManager.INPUT_METHOD_READONLE) {
            mPrefEditor.putBoolean(mEditorActivity.getResources().getString(R.string.pref_default_read_mode), true).commit();
            mPageEditorManager.getCurrentPageEditor().showVoiceInputButton(false); //smilefish fix bug 310018
        }
        else {
            mPrefEditor.putBoolean(mEditorActivity.getResources().getString(R.string.pref_default_read_mode), false).commit();
            mPageEditorManager.getCurrentPageEditor().showVoiceInputButton(true);
        }
    }

    public void quitSelectionTextMode() {
        mPageEditorManager.quitSelectionTextMode();
    }

    public int getInputMode() {
        return mInputManager.getInputMode();
    }

    public void onInputModeChange(int inputMode) {
        mEditorActivity.onInputModeChange(inputMode);
    }

    public void undo() {
		//BEGIN: RICHARD TEST
		mPageEditor = mPageEditorManager.getCurrentPageEditor();
		//END: RICHARD
        mPageEditor.undo();
    }

    public void redo() {
		//BEGIN: RICHARD TEST
		mPageEditor = mPageEditorManager.getCurrentPageEditor();
		//END: RICHARD
        mPageEditor.redo();
    }

    public void insertSpace() {
		//BEGIN: RICHARD TEST
		mPageEditor = mPageEditorManager.getCurrentPageEditor();
		//END: RICHARD
        mPageEditor.insertSpace();
    }

    public void insertEnter() {
		//BEGIN: RICHARD TEST
		mPageEditor = mPageEditorManager.getCurrentPageEditor();
		//END: RICHARD
        mPageEditor.insertEnter();
    }

    public void insertBackSpace() {
		//BEGIN: RICHARD TEST
		mPageEditor = mPageEditorManager.getCurrentPageEditor();
		//END: RICHARD
        mPageEditor.insertBackSpace();
    }

    public void toggleSoftKeyboard() {
		//BEGIN: RICHARD TEST
		mPageEditor = mPageEditorManager.getCurrentPageEditor();
		//END: RICHARD
        mPageEditor.toggleSoftKeyboard();
    }

    public void showSoftKeyboard() {
		//BEGIN: RICHARD TEST
		mPageEditor = mPageEditorManager.getCurrentPageEditor();
		//END: RICHARD
        mPageEditor.showSoftKeyboard();
    }

    public void hiddenSoftKeyboard() {
		//BEGIN: RICHARD TEST
		mPageEditor = mPageEditorManager.getCurrentPageEditor();
		//END: RICHARD
        mPageEditor.hiddenSoftKeyboard();
    }

    public Bitmap getScreenShot(int width, int height, boolean isTextOnly, boolean isHideGrid) {
    	//NEED CHECK
		//BEGIN: RICHARD TEST
		mPageEditor = mPageEditorManager.getCurrentPageEditor();
		//END: RICHARD
        return mPageEditor.getScreenShot(width, height, isTextOnly, isHideGrid);
    }
    
    public Bitmap getScreenShotNotForPdf(int width, int height, boolean isTextOnly, boolean isHideGrid,float scale) {
        		//BEGIN: RICHARD TEST
		mPageEditor = mPageEditorManager.getCurrentPageEditor();
		//END: RICHARD
		return mPageEditor.getScreenShotNotForPdf(width, height, isTextOnly, isHideGrid,scale);
    }
    
    //darwin
    public int getTemplateLayoutHeight() {
			//BEGIN: RICHARD TEST
		mPageEditor = mPageEditorManager.getCurrentPageEditor();
		//END: RICHARD
        return mPageEditor.getTemplateLayoutHeight();
    }
    //darwin

    // BEGIN: archie_huang@asus.com
    public void copy(NotePage page) { // Better
        int inputMode = mInputManager.getInputMode();
        if (inputMode == InputManager.INPUT_METHOD_SELECTION_DOODLE) {
    		mPageEditorManager.getCurrentPageEditor().copyDoodleObject(page);
            setInputMode(getInputModeFromPreference());
        }
        else {
        	mPageEditorManager.getCurrentPageEditor().copyTheSelectionText(page);
            quitSelectionTextMode();
        }
    } // END: archie_huang@asus.com
    
    public void cut(NotePage page) { // Better
    	NoteEditText mCurrentEditText = (NoteEditText) mPageEditorManager.getCurrentPageEditor().getCurrentEditor();
        int start = Math.min(mCurrentEditText.getSelectionStart(), mCurrentEditText.getSelectionEnd());
        int end = Math.max(mCurrentEditText.getSelectionStart(), mCurrentEditText.getSelectionEnd());
        mPageEditorManager.getCurrentPageEditor().copyTheSelectionText(page);
        quitSelectionTextMode();
        mCurrentEditText.getText().replace(start, end, "");
    }
    
    //BEGIN: RICHARD
    public void changeHandWriteToType()
    {
    	mPageEditorManager.getCurrentPageEditor().changeTheSelectionTextToType();
    }
    //END: RICHARD
    
    // BEGIN: archie_huang@asus.com
    public void delete() {
        int inputMode = mInputManager.getInputMode();
        if ((inputMode == InputManager.INPUT_METHOD_SELECTION_DOODLE) || (inputMode == InputManager.INPUT_METHOD_INSERT)) {
        	mPageEditorManager.getCurrentPageEditor().deleteDoodleObject();
        }
    } // END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public void setDoodleTool(int toolCode) {
        // setDoodleTool() is not just called at Doodle mode
        // so we need to get Doodle paint explicitly
    	mPageEditorManager.setPaintTool(toolCode);
    } // END: archie_huang@asus.com
    
    // BEGIN: Better
    public int getDoodleTool() {
    	return mPageEditorManager.getCurrentPageEditor().getPaintTool();
    }
    // END: Better

    // BEGIN: archie_huang@asus.com
    public void switchBaselineMode(boolean enableBaseLine) {
        mHandWritePanel.setBaseLine(enableBaseLine);
        setBaselineMode(enableBaseLine);//Allen
    } // END: archie_huang@asus.com

    //Begin Allen
    public void setBaselineMode(boolean enableBaseLine){
        mPageEditorManager.getCurrentPageEditor().switchBaselineMode(enableBaseLine);//Allen
    }
    //End Allen
    
    // BEGIN: archie_huang@asus.com
    public void paste(NotePage page) { // Better
    	if (MetaData.HasMultiClipboardSupport) {
	    	if (mPreference == null) {
	    		return;
	    	}
	    	
	    	int type = mPreference.getInt(MetaData.PREFERENCE_EDITOR_COPY_CONTENT_TYPE, MetaData.CLIPBOARD_TYPE_NONE);
	    	if (type == MetaData.CLIPBOARD_TYPE_NOTE) {
    		mPageEditorManager.getCurrentPageEditor().pastToNoteEditText(page);
	    	} else if (type == MetaData.CLIPBOARD_TYPE_DOODLE) {
    		mPageEditorManager.getCurrentPageEditor().pastDoodleObject(page);
	    		setInputMode(InputManager.INPUT_METHOD_INSERT);
    		mPageEditorManager.getCurrentPageEditor().setInsertMode(true);
	    	}
    	} else if(mPageEditorManager.getCurrentPageEditor().pasteFromQuickMemo()){
    		//emmanual to copy from QuickMemo to SuperNote
    		mPageEditorManager.getCurrentPageEditor().pasteQuickMemo();
    	} else {
			ClipboardManager clipboard = (ClipboardManager) getContext()
					.getSystemService(Context.CLIPBOARD_SERVICE);
			CharSequence cs = null;
			if (clipboard.hasPrimaryClip()) {
				ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
				cs = item.getText();
				//emmanual to fix bug 514615, 523762
				if (cs == null && item != null && item.getUri() != null) {
					cs = item.getUri().toString();
				}
				if (cs != null) {
					ClipDescription cd = clipboard.getPrimaryClipDescription();
					if (cd != null) {
						CharSequence label = cd.getLabel();
						if (label != null) {
							String str = label.toString();
							if (str.equals(MetaData.CLIPBOARD_DOODLE_DESC)) {
								mPageEditorManager.getCurrentPageEditor().pastDoodleObject(page);
								setInputMode(InputManager.INPUT_METHOD_INSERT);
								mPageEditorManager.getCurrentPageEditor().setInsertMode(true);
								MetaData.INSERT_PHOTO_SELECTION = true;  //Carol
								return;
							}
						}
					}
					mPageEditorManager.getCurrentPageEditor().pastToNoteEditText(page);
				}
			}
    	}
    } 
    // END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public void changeColor(int color) {
    	mPageEditorManager.changeColor(color);
    	
		//+++ Dave  GA
		if(MetaData.IS_GA_ON)
		{
			int sendColor = 0;
			if(color == Color.GREEN)
			{
				sendColor = 0;
			}else if(color == Color.RED)
			{
				sendColor = 1;
			}else if(color == Color.BLUE)
			{
				sendColor = 2;
			}else {
				sendColor = 3;
			}
			
			GACollector gaCollector = new GACollector(sContext);
			gaCollector.scribePenSet(mPageEditor.getTextStyle(), sendColor);
		}
		//---
    } // END:archie_huang@asus.com

    public void changeTextColor(int color) {
    	mPageEditorManager.changeTextColor(color);
    }

    public void changeTextStyle(int style) {
    	mPageEditorManager.changeTextStyle(style);
    	
		//+++ Dave  GA
		if(MetaData.IS_GA_ON)
		{
			int color = mPageEditor.getEditorPaint().getColor();
			int sendColor = 0;
			if(color == Color.GREEN)
			{
				sendColor = 0;
			}else if(color == Color.RED)
			{
				sendColor = 1;
			}else if(color == Color.BLUE)
			{
				sendColor = 2;
			}else {
				sendColor = 3;
			}
			
			GACollector gaCollector = new GACollector(sContext);
			gaCollector.scribePenSet(style, sendColor);
		}
    }

    // BEGIN: archie_huang@asus.com
    public void changeScribleStroke(float width) {
    	mPageEditorManager.changeScribleStroke(width);
        // mPageEditor.onStyleAndStrokeChange();
    } // END: archie_huang@asus.com
    
    // BEGIN: Better
    public void changeAlpha(int alpha) {
    	mPageEditorManager.changeAlpha(alpha);
    	//PaintSelector.setAlpha(mPageEditor.getDoodlePaint(), alpha);//Modified by show
    }
    // END: Better

    // BEGIN: archie_huang@asus.com
    public void clearAll() {
    	mPageEditorManager.getCurrentPageEditor().clearDoodleView();
    } // END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public boolean groupDoodleObject(boolean group) {
        return mPageEditorManager.getCurrentPageEditor().groupDoodleObject(group);
    } // END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public void reloadTimer() {
        mHandWritePanel.reloadTimer();
    } // END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public void drawHint(RectF hint) {
    	mPageEditorManager.getCurrentPageEditor().drawHint(hint);
    } // END: archie_huang@asus.com
    
    //BEGIN:Show
    //Begin Darwin_Yu@asus.com
    public void updateCropDrawInfo(String filename,String originalFileName)
    {
    	mPageEditorManager.getCurrentPageEditor().UpDateCurDrawInfo(filename,originalFileName);
    }
    //End   Darwin_Yu@asus.com
    //END:Show

    public void showSelectionTextHint(boolean show) {
        mEditorActivity.showSelectionTextHint(show);
    }

    public int getBookFontSize() {
        return mEditorActivity.getBookFontSize();
    }

    public int getBookColor() {
        return mEditorActivity.getBookColor();
    }

    public int getBookGridType() {
        return mEditorActivity.getBookGridType();
    }

    public int getPaintColor() {
        return mPageEditorManager.getCurrentPageEditor().getPaint().getColor();
    }

    public int getTextStyle() {
        return mPageEditorManager.getCurrentPageEditor().getTextStyle();
    }

    public int getTextColor() {
        return mPageEditorManager.getCurrentPageEditor().getEditorPaint().getColor();
    }

    public float getScribleStroke() {
        return mPageEditorManager.getCurrentPageEditor().getPaint().getStrokeWidth();
    }

	// begin noah;7.5
	public boolean loadPage(NotePage page, Boolean isOnResume,
			boolean fromAddNew) {
		// BEGIN: RICHARD
		// CLEAR BITMAP CACHE
		BitmapLender lender = BitmapLender.getInstance();
		lender.recycle();
		// END: RICHARD

		// mPageEditorManager.getFirstPageEditor().getDoodleView().setIsNeedDrawDoodleViewContent(false);
		if (mLoadPageTask != null && mLoadPageTask.isTaskRunning()) {
			mLoadPageTask.cancel(true);
		}
		mLoadPageTask = new LoadPageTaskVersionTwo(getContext(), page,
				mPageEditorManager.getFirstPageEditor(), isOnResume, fromAddNew);

		//emmanual:SERIAL_EXECUTOR => THREAD_POOL_EXECUTOR, to fix bug 543793
		mLoadPageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		return true;
		// return page.load(new PageDataLoader(getContext()), false,
		// mPageEditorManager.getFirstPageEditor());
	}

	public boolean loadPage(NotePage page, Boolean isOnResume) {
		return loadPage(page, isOnResume, false);
	}

	// end noah;7.5
    
    //darwin
    public void getCacheBitmap(Canvas c ,int width,int height)
    {
    	mPageEditor.getResult(c, width, height,true,true);
    }
    //darwin
    
    // BEGIN: Better
    public boolean loadPageAsync(NotePage page) {
        return page.load(PageDataLoader.getInstance(getContext()), true, mPageEditorManager.getFirstPageEditor());
    }
    // END: Better
    
    //BEGIN: RICHARD
    public PageEditorManager getPageEditorManager()
    {
    	return mPageEditorManager;
    }
    //END: RICHARD
    
    // BEGIN: Better
    public PageEditor getPageEditor() {
    	//RICHARD NEED CHECK
    	return mPageEditorManager.getCurrentPageEditor();
    }
    // END: Better
	
    //BEGIN: RICHARD
    public Boolean isNeedUpdateIndexFile(NotePage page) {
    	//NEED DO LATER
    	if(mPageEditor.isEditTextModified())
    	{
    		return true;
    	}
    	else if(mPageEditor.isDoodleModified() && page.getVersion() == 1)
    	{
    		return true;
    	}
    	return false;
    }
    //END: RICHARD

    public int getDeviceType()
    {
    	return mEditorActivity.getDeviceType();
    }
    public boolean isPhoneSizeMode() {
        return mEditorActivity.isPhoneScreen();
    }

    public void prevPage() {
        mEditorActivity.prevPage();
    }

    public void nextPage() {
        mEditorActivity.nextPage();
    }

    // BEGIN: james5_chan@asus.com
    public void insertTextForPhone(Intent intent) {
        if (MetaData.PHONE_MEMO_ACTION.equals(intent.getAction())) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            if (intent.getStringExtra(MetaData.PHONE_WHO) != null) {
                builder.append(intent.getStringExtra(MetaData.PHONE_WHO));
                builder.append(", ");

            }
            //begin smilefish fix bug 359769
            java.text.DateFormat timeFormat = DateFormat.getTimeFormat(sContext);
            java.text.DateFormat dateFormat = DateFormat.getDateFormat(sContext);
            Date date = new Date(intent.getLongExtra(MetaData.PHONE_TIME, 0L));
            builder.append(dateFormat.format(date));
            builder.append(" ");
            builder.append(timeFormat.format(date));
            //end smilefish
            builder.append("]");

            SpannableStringBuilder msg = new SpannableStringBuilder(builder.toString());
            NoteForegroundColorItem color = new NoteForegroundColorItem(Color.RED);
            msg.setSpan(color, 0, msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mPageEditorManager.getCurrentPageEditor().addItemToEditText(msg);
            intent.setAction("");
        }

    }
    // END: james5_chan@asus.com

    // BEGIN: james5_chan@asus.com
    public String getFilePath() {
        return mEditorActivity.getFilePath();
    }
    // END: james5_chan@asus.com

    public String getOnlyText() {
        return mPageEditorManager.getCurrentPageEditor().getOnlyText();
    }
    
    //darwin
    public String getOnlyTextWithVO() {
        return mPageEditorManager.getCurrentPageEditor().getOnlyTextWithVO();
    }
    //darwin

    public String getEditorText() {
        return mPageEditor.getEditorText();
    }

    //darwin
    public boolean isEditTexthaveObjects() {
        return mPageEditorManager.getCurrentPageEditor().isEditTexthaveObjects();
    }
    
    public boolean isTemplatehaveObjects() {
        return mPageEditorManager.getCurrentPageEditor().isTemplatehaveObjects();
    }
    //darwin
    
    public boolean isDoodlehaveObjects() {
        return mPageEditor.isDoodlehaveObjects();
    }

    public void finishBoxEditText() {
        mPageEditor.finishBoxEditor();
    }

    private PageEditor.onClipBoardAvailableListener mOnClipBoardAvailableListener = new PageEditor.onClipBoardAvailableListener() {

        @Override
        public void onAvailableChange(boolean available) {
            mEditorActivity.setPasteButtonsEnable(available);
            mIsClipBoardAvailable = available;
        }
    };

    private PageEditor.onDoodleItemAvailableListener mOnDoodleItemAvailableListener = new PageEditor.onDoodleItemAvailableListener() {

        @Override
        public void onAvailableChange(boolean available) {
            mEditorActivity.setSelectButtonsEnable(available);
            mIsDoodleItemAvailable = available;
        }
    };

    // BEGIN: archie_huang@asus.com
    private PageEditor.onDoodleItemSelectListener mOnDoodleItemSelectListener = new PageEditor.onDoodleItemSelectListener() {

        @Override
        public void onUnSelect() {
            mIsDoodlePastAvailable = false;
            mEditorActivity.setDoodleGroupButtonsEnable(false);
            mEditorActivity.setDoodleUnGroupButtonsEnable(false);
            mEditorActivity.setDoodlePastButtonsEnable(false);
            //BEGIN:Show
        	mIsDoodleCropAvailable = false; 
            mEditorActivity.setDoodleCropButtonsEnable(false); 
            mIsDoodleTextEditAvailable = false; 
            mEditorActivity.setDoodleTextEditButtonsEnable(false);
            mIsDoodleChangeImgAvailable = false; 
            mEditorActivity.setDoodleChangeImgButtonsEnable(false); 
            //END:Show
            mEditorActivity.setSelectedModeHintTextWith(false);
        }

        @Override
        public void onSelect(boolean multiple, boolean group) {
            mIsDoodlePastAvailable = true;
            mEditorActivity.setDoodlePastButtonsEnable(true);
            mEditorActivity.setSelectedModeHintTextWith(true);
            if (multiple) {
                mEditorActivity.setDoodleGroupButtonsEnable(true);
                if (group) {
                    mEditorActivity.setDoodleUnGroupButtonsEnable(true);
                }
                else {
                    mEditorActivity.setDoodleUnGroupButtonsEnable(false);
                }
            }
            else if (group) {
                mEditorActivity.setDoodleGroupButtonsEnable(false);
                mEditorActivity.setDoodleUnGroupButtonsEnable(true);
            }
            else {
                mEditorActivity.setDoodleGroupButtonsEnable(false);
                mEditorActivity.setDoodleUnGroupButtonsEnable(false);
            }
        }
    }; // END: archie_huang@asus.com

    private PageEditor.onUndoStackAvailableListener mOnUndoStackEmptyListener = new PageEditor.onUndoStackAvailableListener() {

        @Override
        public void onNoteUndoStackAvailableChange(boolean availabele) {
            if (mIsNoteEditTextUndoStackAvailable != availabele) {
                mEditorActivity.setNoteUndoButtonsEnable(availabele);
                mIsNoteEditTextUndoStackAvailable = availabele;
            }
        }

        @Override
        public void onBoxUndoStackAvailableChange(boolean availabele) {
            if (mIsBoxEditorTextUndoStackAvailable != availabele) {
                mEditorActivity.setBoxUndoButtonsEnable(availabele);
                mIsBoxEditorTextUndoStackAvailable = availabele;
            }
        }

        @Override
        public void onDoodleUndoStackAvailableChange(boolean availabele) {
            if (mIsDoodleUndoStackAvailable != availabele) {
                mEditorActivity.setDoodleUndoButtonsEnable(availabele);
                mIsDoodleUndoStackAvailable = availabele;
            }
        }
    };

    private PageEditor.onRedoStackAvailableListener mOnRedoStackAvailableListener = new PageEditor.onRedoStackAvailableListener() {

        @Override
        public void onNoteRedoStackAvailableChange(boolean availabele) {
            if (mIsNoteEditTextRedoStackAvailable != availabele) {
                mEditorActivity.setNoteRedoButtonsEnable(availabele);
                mIsNoteEditTextRedoStackAvailable = availabele;
            }
        }

        @Override
        public void onDoodleRedoStackAvailableChange(boolean availabele) {
            if (mIsBoxEditorTextRedoStackAvailable != availabele) {
                mEditorActivity.setDoodleRedoButtonsEnable(availabele);
                mIsBoxEditorTextRedoStackAvailable = availabele;
            }
        }

        @Override
        public void onBoxRedoStackAvailableChange(boolean availabele) {
            if (mIsDoodleRedoStackAvailable != availabele) {
                mEditorActivity.setBoxRedoButtonsEnable(availabele);
                mIsDoodleRedoStackAvailable = availabele;
            }
        }
    };

    private PageEditor.onEditorColorChangeListener mOnEditorColorChangeListener = new PageEditor.onEditorColorChangeListener() {

        @Override
        public void onColorChange(int color) {
            mEditorActivity.setColorButton(color);
        }
    };

    private PageEditor.onEditorBoldChangeListener mOnEditorBoldChangeListener = new PageEditor.onEditorBoldChangeListener() {

        @Override
        public void onBoldChange(int textStyel) {
            mEditorActivity.setBoldButton(textStyel);
        }
    };

    public boolean isClipBoardAvailable() {
        return mIsClipBoardAvailable;
    }

    public boolean isDoodleItemAvailable() {
        return mIsDoodleItemAvailable;
    }

    public boolean isDoodleUnGroupAvailable() {
        return mIsDoodleGroupAvailable;
    }

    // BEGIN: archie_huang@asus.com
    public boolean isDoodlePastAvailable() {
        return mIsDoodlePastAvailable;
    } // END: archie_huang@asus.com
   
    // BEGIN: Show
    public boolean isDoodleCropAvailable() {
        return mIsDoodleCropAvailable;
    }
    
    public boolean isDoodleTextEditAvailable() {
        return mIsDoodleTextEditAvailable;
    } 
    
    public boolean isDoodleChangeImgAvailable() {
        return mIsDoodleChangeImgAvailable;
    } 
    
	// END: Show

    public boolean isNoteEditTextUndoStackAvailable() {
        return mIsNoteEditTextUndoStackAvailable;
    }

    public boolean isNoteEditTextRedoStackAvailable() {
        return mIsNoteEditTextRedoStackAvailable;
    }

    public boolean isBoxEditorTextUndoStackAvailable() {
        return mIsBoxEditorTextUndoStackAvailable;
    }

    public boolean isBoxEditorTextRedoStackAvailable() {
        return mIsBoxEditorTextRedoStackAvailable;
    }

    public boolean isDoodleUndoStackAvailable() {
        return mIsDoodleUndoStackAvailable;
    }

    public boolean isDoodleRedoStackAvailable() {
        return mIsDoodleRedoStackAvailable;
    }

    // for orientation cursor
    public int getCursorPos() {
    	//NEED DO LATER
        return mPageEditorManager.getCurrentPageEditor().getCursorPos();
    }

    public void setCursorPos(int pos) {
    	//NEED DO LATER
    	mPageEditorManager.getCurrentPageEditor().setCursorPos(pos);
    }

    public boolean onUpperViewShortPress(MotionEvent ev) {
        return mEditorActivity.onUpperViewShortPress(ev);
    }

    public void preparePopupWindowInsert() {
        mEditorActivity.preparePopupWindowInsert();
    }

    public void insertTimestamp(long time, long pageid) {
    	//NEED DO LATER
    	mPageEditorManager.getCurrentPageEditor().insertTimestamp(time, pageid);
    }
	//BEGIN: Show    
    public String getFileInfo(){
    	//NEED DO LATER
    	return mPageEditorManager.getCurrentPageEditor().getFileInfo();    	
    }    

    public void setDoodleCropButtonsEnable(boolean flag){
    	mIsDoodleCropAvailable = flag;
    	mEditorActivity.setDoodleCropButtonsEnable(flag);
    }
    
    public void setDoodleTextEditButtonsEnable(boolean flag){
    	mIsDoodleTextEditAvailable = flag;
    	mEditorActivity.setDoodleTextEditButtonsEnable(flag);
    }
    
    public void setDoodleChangeImgButtonsEnable(boolean flag){
    	mIsDoodleChangeImgAvailable = flag;
    	mEditorActivity.setDoodleChangeImgButtonsEnable(flag);
    }
    
	//begin wendy
    public void setDoodlePastButtonsEnable(boolean flag)
    {
    	  mIsDoodlePastAvailable = false;
          mEditorActivity.setDoodlePastButtonsEnable(false);
    }
   //end wendy
    public void enableModifyBoxEditor() {
    	//NEED DO LATER
    	mPageEditorManager.getCurrentPageEditor().enableModifyBoxEditor();
    }
   
    public void cancleBoxEditor(){
    	//NEED DO LATER
    	mPageEditorManager.getCurrentPageEditor().cancleBoxEditor();
    }
    
    public boolean IsTextImgEdit(){
    	return mEditorActivity.IsTextImgEdit();
    } 
    
    //END: Show
    //begin wendy
    //fix bug:screen will not refresh in doodle mode
    public void reflashScreen()
    {
    	mPageEditorManager.reflashScreen();
    }
    //end wendy 
    //fix bug:screen will not refresh in doodle mode
    
    //Begin: show_wang@asus.com
    //Modified reason: fix bug, insert textimg paste button disable when clipboard type is CLIPBOARD_TYPE_DOODLE
    public Boolean IsNoteEditTextCurrentEditor() {
		//BEGIN: RICHARD
        return mPageEditorManager.getCurrentPageEditor().IsNoteEditTextCurrentEditor();
    	//END: RICHARD
	}
    //End: show_wang@asus.com
    
    //Begin: show_wang@asus.com
    //Modified reason: for dds
    public void saveSelectionDrawInfo(){
    	mPageEditorManager.getCurrentPageEditor().saveSelectionDrawInfo();
    }
    
    public void saveBoxEditTextContent() {
        mPageEditorManager.getCurrentPageEditor().saveBoxEditTextContent();
    }
    
    public void setBoxEditTextContent() {
        mPageEditorManager.getCurrentPageEditor().setBoxEditTextContent();
    }
    
  	public Boolean getConfigStatus() {
  		return mEditorActivity.getConfigStatus();
  	}
  	
  	public void selectCurrentDrawInfo() {
  		mPageEditorManager.getCurrentPageEditor().selectCurrentDrawInfo();
  	}
  	
  	public void setTextImgStatus() {
  		mPageEditorManager.getCurrentPageEditor().setTextImgStatus();
  	}
  	
  	public void setEraserStatus(boolean status){
  		mEditorActivity.setEraserStatus(status); 
  	}
  	
  	public LoadPageTaskVersionTwo getLoadPageTask() {
  		return mLoadPageTask;
  	}
    //End: show_wang@asus.com
	
	//Begin: show_wang@asus.com
    //Modified reason: for airview
	public float getEraserWidth () {
  		return mEditorActivity.getEraserWidth();
  	}
	//End: show_wang@asus.com
	
	//begin smilefish
	public void showShapeFailedToast()
	{
		EditorActivity.showToast(mEditorActivity, R.string.vo_shape_failed);
	}
	
	public int getScrollBarX()
	{
		return mPageEditorManager.getCurrentPageEditor().getPageEditorScrollBar().getScrollX();
	}
	
	public int getScrollBarY()
	{
		return mPageEditorManager.getCurrentPageEditor().getPageEditorScrollBar().getScrollY();
	}
	
	public void redrawDoodleView(){
		mRedrawHander.sendEmptyMessageDelayed(MSG_REDRAW_DOODLEVIEW, 500);
	}
	
	private static final int MSG_REDRAW_DOODLEVIEW = 1;
    private Handler mRedrawHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REDRAW_DOODLEVIEW:
                	mPageEditorManager.getCurrentPageEditor().redrawDoodleView();
                    break;
                default:

            }
        }

    };
    
    public void setHandWriteViewEnable(boolean enable){
    	if(mHandWritePanel != null){
    		mHandWritePanel.setEnable(enable);
    	}
		//add by Emmanual
    	if(enable){
    		getPageEditor().setMicroViewVisible(false);
    	}
		//end Emmanual
    }
    
    public boolean isHandWriteViewEnable()
    {
    	if(mHandWritePanel != null)
    		return mHandWritePanel.getEnable(); 
    	else
    		return false;
    }
    
	public boolean isColorBoldPopupShown(){
		return mEditorActivity.isColorBoldPopupShown();
	}
	
	public void insertRecorder(View parentView){
		if(mRecorder == null){
			mRecorder = new Recorder(mPageEditorManager, sContext, parentView, mEditorActivity.getFilePath());
			mRecorder.setOnStateChangedListener(this);
		}else{
			mRecorder.showRecorder(parentView, mEditorActivity.getFilePath());
		}
	}
	
	@Override
    public void onStateChanged(int state) {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public void onError(int error) {
		Resources res = mEditorActivity.getResources();
        String message = null;
        switch (error) {
            case Recorder.SDCARD_ACCESS_ERROR:
            	if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable()){
            		message = res.getString(R.string.error_sdcard_access);
            	}else{
            		message = res.getString(R.string.error_sdcard_access_nosdcard);
            	}
                break;
            
            case Recorder.INTERNAL_ERROR_START:
                message = res.getString(R.string.startfail);
                break;
        }
        if (message != null) {
            new AlertDialog.Builder(mEditorActivity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setTitle(R.string.app_name)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .show();
        }
    }
	
	//end smilefish
    //begin jason
    public void clearSelfWindow(){
    	mPageEditorManager.getCurrentPageEditor().clearTemplayoutWindow();
    }
    public void flushUnStoreData(){
        //mars_li don't implement flushData interface, if need do it later
    	//mHandWritePanel.flushData();
    }
    //end jason
    //add by mars
    public IHandWritePanel getHandWritingView(){
    	 return mHandWritePanel;
    }
    public boolean reponseBackKey(){
    	if(mHandWritePanel == null){
    		return false;
    	}
    	return mHandWritePanel.reponseBackKey();
    }
    //end mars
}
