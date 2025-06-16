package com.asus.supernote;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.asus.supernote.editable.HandWritePanelView;
import com.asus.supernote.editable.HandWritingView;
import com.asus.supernote.editable.IHandWritePanel;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.PageEditorManager;
import com.asus.supernote.editable.attacher.Attacher;
import com.asus.supernote.editable.attacher.ClipboardAttacher;

public class InputManager {
    private static final String TAG = "InputManager";

    private static final int NUM_ATTACHER = 6;

    public static final int RESULT_VIDEO = 0;
    public static final int RESULT_CAMERA = 1;
    public static final int RESULT_VOICE = 2;
    public static final int RESULT_GALLERY = 3;
    public static final int RESULT_TEXT = 4;
    public static final int RESULT_CLIPBOARD = 5; // Better
    public static final int RESULT_VIEW = 6;
    public static final int REQUEST_CROP_ICON = 7;//darwin
    public static final int RESULT_VOICEFILE = 8;

    public static final int INPUT_METHOD_KEYBOARD = 0;
    public static final int INPUT_METHOD_SCRIBBLE = 1;
    public static final int INPUT_METHOD_DOODLE = 2;
    public static final int INPUT_METHOD_SELECTION_TEXT = 3;
    public static final int INPUT_METHOD_READONLE = 4;
    public static final int INPUT_METHOD_INSERT = 5;
    public static final int INPUT_METHOD_SELECTION_DOODLE = 6;
    public static final int INPUT_METHOD_TEXT_IMG_KEYBOARD = 7;
    public static final int INPUT_METHOD_TEXT_IMG_SCRIBBLE = 8;
    public static final int INPUT_METHOD_COLOR_PICKER = 12;//smilefish

    private int mInputMode = INPUT_METHOD_KEYBOARD;

    private EditorUiUtility mEditorUiUtility = null;
    private PageEditorManager mPageEditorManager = null;

    private HandWritePanelView mHandWritePanelView = null; 
    
    private IHandWritePanel curHandWritePanel = null;

    private Attacher[] mAttachers = new Attacher[NUM_ATTACHER];

    public InputManager(EditorUiUtility utility, PageEditorManager pageEditorManager) {

        mEditorUiUtility = utility;
        mPageEditorManager = pageEditorManager;
        
        mHandWritePanelView = (HandWritePanelView)((Activity) mEditorUiUtility.getContext()).findViewById(R.id.hand_writeview_panel);
        mHandWritePanelView.setPageEditorManager(mPageEditorManager);
        mHandWritePanelView.setEnable(false);
        curHandWritePanel = mHandWritePanelView;

        mPageEditorManager.setReadOnlyViewStatus(false);

        initAllAttacher();
    }

    private void initAllAttacher() {
        mAttachers[RESULT_CLIPBOARD] = new ClipboardAttacher(mPageEditorManager); // Better
    }

    public void setInputMode(int inputMode) {
        if (inputMode != INPUT_METHOD_KEYBOARD &&
                inputMode != INPUT_METHOD_SCRIBBLE &&
                inputMode != INPUT_METHOD_DOODLE &&
                inputMode != INPUT_METHOD_SELECTION_TEXT &&
                inputMode != INPUT_METHOD_READONLE &&
                inputMode != INPUT_METHOD_INSERT &&
                inputMode != INPUT_METHOD_SELECTION_DOODLE &&
                inputMode != INPUT_METHOD_TEXT_IMG_KEYBOARD &&
                inputMode != INPUT_METHOD_TEXT_IMG_SCRIBBLE &&
                inputMode != INPUT_METHOD_COLOR_PICKER) {//Smilefish
            Log.e(TAG, "fail Input Mode !!");
            return;
        }

        mInputMode = inputMode;
        updateViewRelation();
    }

    private void updateViewRelation() {
    	PageEditor currentPageEditor = mPageEditorManager.getCurrentPageEditor();
        switch (mInputMode) {
            case INPUT_METHOD_SCRIBBLE:
            	mPageEditorManager.setReadOnlyViewStatus(false);
            	curHandWritePanel.setPaint(currentPageEditor.getPaint());
                mPageEditorManager.setNoteEditTextEnable(true);
                mPageEditorManager.enableDoodleView(false);
                new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(mInputMode == INPUT_METHOD_SCRIBBLE) //smilefish fix bug 557743/557752
							curHandWritePanel.setEnable(true);						
					}
				}, 100);
                break;
            case INPUT_METHOD_KEYBOARD:
                mPageEditorManager.setReadOnlyViewStatus(false);
                curHandWritePanel.setEnable(false);
                mPageEditorManager.setNoteEditTextEnable(true);
                mPageEditorManager.enableDoodleView(false);
                break;
            case INPUT_METHOD_DOODLE:
            	mPageEditorManager.setReadOnlyViewStatus(false);
            	curHandWritePanel.setEnable(false);
                mPageEditorManager.setNoteEditTextEnable(false);
                mPageEditorManager.enableDoodleView(true);
                break;
            case INPUT_METHOD_INSERT:
            	mPageEditorManager.setReadOnlyViewStatus(false);
            	curHandWritePanel.setEnable(false);
                mPageEditorManager.setNoteEditTextEnable(false);
                mPageEditorManager.enableDoodleView(true);
                break;
            case INPUT_METHOD_SELECTION_DOODLE:
            	mPageEditorManager.setReadOnlyViewStatus(false);
            	curHandWritePanel.setEnable(false);
                mPageEditorManager.setNoteEditTextEnable(false);
                mPageEditorManager.enableDoodleView(true);
                break;
            case INPUT_METHOD_READONLE:
                curHandWritePanel.setEnable(false);
                mPageEditorManager.setNoteEditTextEnable(false);
                mPageEditorManager.enableDoodleView(false);
                mPageEditorManager.setReadOnlyViewStatus(true);
                break;
            case INPUT_METHOD_TEXT_IMG_SCRIBBLE:
                curHandWritePanel.setPaint(currentPageEditor.getPaint());
                mPageEditorManager.setNoteEditTextEnable(false);
                mPageEditorManager.enableDoodleView(false);
                curHandWritePanel.setEnable(true);
                //BEGIN: Show
                if(!mEditorUiUtility.getConfigStatus()) {
	                if(mEditorUiUtility.IsTextImgEdit())
	                {
	                	mEditorUiUtility.enableModifyBoxEditor();
	                }
	                else
	                //END: Show
	                	currentPageEditor.enableBoxEditor();
                }
                break;
            case INPUT_METHOD_TEXT_IMG_KEYBOARD:
            	 mPageEditorManager.setReadOnlyViewStatus(false);
            	 curHandWritePanel.setEnable(false);
                 mPageEditorManager.setNoteEditTextEnable(false);
                 mPageEditorManager.enableDoodleView(false);
                 //BEGIN: Show
                 if(!mEditorUiUtility.getConfigStatus()) {
	                 if(mEditorUiUtility.IsTextImgEdit())
	                 {
	                 	mEditorUiUtility.enableModifyBoxEditor();
	                 }
	                 else
	                 //END: Show
	                	 currentPageEditor.enableBoxEditor();
                 }
                 break;
                 
            case INPUT_METHOD_COLOR_PICKER: //smilefish
                curHandWritePanel.setEnable(false);
            	mPageEditorManager.setNoteEditTextEnable(false);
            	mPageEditorManager.enableDoodleView(false);
                mPageEditorManager.setReadOnlyViewStatus(false);
            	break;
        }
        mEditorUiUtility.onInputModeChange(mInputMode);
    }

    public int getInputMode() {
        return mInputMode;
    }

    public Attacher doAttach(int type) {
        if (type >= NUM_ATTACHER) return null;
        return mAttachers[type];
    }

    public IHandWritePanel getHandWritingView() {
        return this.curHandWritePanel;
    }
    
    //emmanual_chen
    public void setHandWritePanelEnable(boolean enabled){
    	curHandWritePanel.setEnable(enabled);
    }
}
