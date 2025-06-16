package com.asus.supernote.editable;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.RemoteException;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.asus.supernote.BitmapLender;
import com.asus.supernote.EditorActivity;
import com.asus.supernote.EditorActivityButtomButtonsContainer;
import com.asus.supernote.EditorUiUtility;
import com.asus.supernote.InputManager;
import com.asus.supernote.PaintSelector;
import com.asus.supernote.R;
import com.asus.supernote.ShapeManager;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.AsusFormat;
import com.asus.supernote.data.AsusFormatReader;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem;
import com.asus.supernote.doodle.DoodleView;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawtool.PathDrawTool;
import com.asus.supernote.editable.noteitem.NoteForegroundColorItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteBaselineItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteSendIntentItem;
import com.asus.supernote.editable.noteitem.NoteStringItem;
import com.asus.supernote.editable.noteitem.NoteTextStyleItem;
import com.asus.supernote.editable.noteitem.NoteTimestampItem;
import com.asus.supernote.inksearch.AsusInputRecognizer;
import com.asus.supernote.inksearch.CFG;
import com.asus.supernote.microview.MicroView;
import com.asus.supernote.picker.PickerUtility;
import com.asus.supernote.share.utils.ShareUtils;
import com.asus.supernote.template.TemplateToDoUtility;
import com.asus.supernote.template.TemplateUtility;
import com.asus.supernote.template.TemplateLinearLayout;
import com.asus.supernote.template.TemplateEditText;
import com.asus.supernote.template.TemplateLayout;
import com.asus.supernote.template.TemplateToDoUtility.onLoadingPageIsFullListener;
import com.asus.supernote.ui.CoverHelper;

public class PageEditor {
    private static final int BOX_EDITTEXT_LINE_LIMIT = 4;

    private static final int NOTEEEDITTEST_HIHGLIGNT_COLOR = 0xBBFFC066;

    // Used to align image span
    public static final float FONT_DESCENT_RATIO = 0.85f;

    private static final String TAG = "SuperNote_PageEditor";

    public static final String OBJ = String.valueOf((char) 65532);
    
    // BEGIN: Better
    PageEditorWrapper mPageEditorWrapper = null;
    // END: Better

    public boolean HasPopUpWindows = false; //Dave. To fix the bug: keyboard will show and disappear one time in keyboard_input_mode.
    public boolean shouldDiscardPoint = false; //Dave. To fix the bug: an extra stroke will be shown when the selection mode ended.
    private InputManager mInputManager = null;
    private final EditorUiUtility mEditorUiUtility;
    private final PageEditorManager mPageEditorManager;

    private final ReadOnlyMaskView mReadOnlyMaskView;
    private final DoodleView mDoodleView;
    private final NoteEditText mNoteEditText;
    private final NoteEditText mBoxEditText;
    private static Editable mContent = new SpannableStringBuilder();
    private NotePage mNotePage;
    private Boolean mIsSaveing = false;
    private Object mLock = new Object();
	//begin jason
    private  EditorActivityButtomButtonsContainer mBottomButtonContainer=null;
    private ShapeManager mShapeManager=null;
    //end jason
	public NotePage getNotePage() {
		return mNotePage;
	}
	public void setNotePage(NotePage notePage) {
		this.mNotePage = notePage;
	}
    public NoteEditText getBoxEditText() {
		return mBoxEditText;
	}

	private NoteEditText mCurrentEditText = null;
	public void setmCurrentEditText(NoteEditText mCurrentEditText) {
		this.mCurrentEditText = mCurrentEditText;
	}

	private final InputConnection mNoteEditTextInputConnection;
    private final InputConnection mBoxEditTextInputConnection;
    private InputConnection mCurrentInputConnection = null;

    public void setmCurrentInputConnection(InputConnection mCurrentInputConnection) {
		this.mCurrentInputConnection = mCurrentInputConnection;
	}

    private Paint mTextScribblePaint = null;
    private Paint mDoodlePaint = null;
    private int mTextStyle = Typeface.NORMAL;
    
    public View mHandleView = null;


    //private boolean mIsDoodleModified = false;
    //darwin
    public boolean mIsForceModified = false;
    public void setForceModified()
    {
    	this.mDoodleView.setForceModify();
    }
    public boolean IsAllForceModify()
    {
    	return mDoodleView.IsAllForceModify();
    }
    //darwin

    private int mScreenWidth = -1, mScreenHeight = -1;

    private int mImgTextPreviousMode = -1;

    private boolean mIsScrollDoodle = true;

    private onClipBoardAvailableListener mClipBoardAvailableListener = null;
    private onUndoStackAvailableListener mUndoStackAvailableListener = null;
    private onRedoStackAvailableListener mRedoStackAvailableListener = null;
    private onDoodleItemAvailableListener mDoodleItemAvailableListener = null;
    private onDoodleItemSelectListener mDoodleItemSelectListener = null;
    private onEditorColorChangeListener mEditorColorChangeListener = null;
    private onEditorBoldChangeListener mEditorBoldChangeListener = null;
    
    //Begin Allen
	private TemplateUtility mTemplateUtility = null;
	private TemplateLayout mTemplatelayout;//Allen
	public TemplateLayout getTemplatelayout() {
		return mTemplatelayout;
	}
	private TemplateLinearLayout mTemplateLinearLayout;
	private LinearLayout mAdditionalViewLayout;
	private int mTemplateLayoutHeight = 0;
	private Context mContext = null;
	private int mTemplateType = MetaData.Template_type_normal;
    private MicroView mMicroView = null;
    private boolean isNoteEditEnable = true;
	public int getTemplateType() {
		return mTemplateType;
	}

	public int getTemplateLayoutHeight() {
		return mTemplateLayoutHeight;
	}
	
	public void setTemplateLayoutHeight(int templateLayoutHeight) {
		this.mTemplateLayoutHeight = templateLayoutHeight;
		if (mPageEditorScrollBar != null) {
			updateVerticalScrollerHeight(mViewHeight, getPageTotalHeight());
		}
	}
	public int getTemplateLayoutScaleHeight(){
		return (int) (mTemplateLayoutHeight*getScaleY());
	}
	
	public int getTemplateLinearLayoutScrollY(){
		return mTemplateLinearLayout.getScrollY();
	}
	public void TemplateLinearLayoutDispatchMotionEvent(MotionEvent event)
	{
		mTemplateLinearLayout.dispatchTouchEvent(event);
		//mNoteEditText.dispatchTouchEvent(event);
	}
	
	public int getTemplateLinearLayoutTopMargin()
	{
		return ((android.widget.FrameLayout.LayoutParams)(mTemplateLinearLayout.getLayoutParams())).topMargin;
	}
	//End Allen
	
    //BEGIN: RICHARD
    private AsusInputRecognizer mAsusInputRecognizer = null;
    //private boolean mIsIndexFileNeedUpdate = false;//Richard add
    //END: RICHARD
    
    //BEGIN: RICHARD SCROLLBAR
    PageEditorScrollBar mPageEditorScrollBar = null;
    NoteFrameLayout mNoteFrameLayout = null;
    float mScaleX = 1f;
    float mScaleY = 1f;
	private int mViewHeight = 0;
	private int mViewWidth = 0;
    //END: RICHARD
    
    private SharedPreferences mPreferences = null; // Better
    
    private Boolean mIsDataLoading = false;//RICHARD
    
    private int mBoxEditTextTop = 0; //PaddingTop value for Text Image[Carol]

    //BEGIN: RICHARD TEST
    public void requestNextOrPrevPage(Boolean flag)
    {
    	if(getScrollX() == 0||getScrollX() == mPageEditorScrollBar.getMaxScrollX()){//Allen
    		mPageEditorManager.requestNextOrPrevPage(flag);
    	}
    }
    
    public void requestToBeCurrent()
    {
    	mPageEditorManager.setCurrentPageEditor(this);
    }
    public NoteFrameLayout getLayout()
    {
    	return mNoteFrameLayout;
    }
    
    public void setReadOnlyViewStatus(Boolean status)
    {
    	if(status)
    	{
    		mReadOnlyMaskView.enable();
    	}
    	else
    	{
    		mReadOnlyMaskView.disable();
    	}
    }
    
    public float getScaleX()
    {
    	return mScaleX;
    }
    
    public float getScaleY()
    {
    	return mScaleY;
    }
    
    
    public void adjustNoteFrameLayoutForPadLookPhoneP(int viewHeight)
    {
		int pageHeight = mNoteEditText.getOrignalPageHeight();
		int marginTop = mEditorUiUtility.getContext().getResources().getDimensionPixelSize(R.dimen.additionalview_marginTop); //by show;    	//need do px to dp//different device may be different top
    		
		int marginBottom = viewHeight - pageHeight -marginTop;
		((android.widget.FrameLayout.LayoutParams)(mTemplateLinearLayout.getLayoutParams())).topMargin = marginTop;
		((android.widget.FrameLayout.LayoutParams)(mTemplateLinearLayout.getLayoutParams())).bottomMargin = marginBottom;
		
		((android.widget.FrameLayout.LayoutParams)(mDoodleView.getLayoutParams())).topMargin = marginTop;
		if(MetaData.IS_KEYBOARD_SHOW){
			((android.widget.FrameLayout.LayoutParams)(mDoodleView.getLayoutParams())).height = pageHeight;
		}else{	    		
    		((android.widget.FrameLayout.LayoutParams)(mDoodleView.getLayoutParams())).bottomMargin = marginBottom;
		}
		
		((android.widget.FrameLayout.LayoutParams)mReadOnlyMaskView.getLayoutParams()).topMargin = marginTop;
		((android.widget.FrameLayout.LayoutParams)mReadOnlyMaskView.getLayoutParams()).bottomMargin = marginBottom;
		
		mPageEditorScrollBar.setVerticalScrollTopAndBottomMargin(marginTop,marginBottom);
		
		((android.widget.FrameLayout.LayoutParams)(mAdditionalViewLayout.getLayoutParams())).topMargin = marginTop - 42;
		((android.widget.FrameLayout.LayoutParams)(mAdditionalViewLayout.getLayoutParams())).bottomMargin = marginBottom - 152;
		
		mViewHeight -= marginTop;
		mViewHeight -= marginBottom;
    }
    
    public void setScale(float x,float y)
    {   	
    	mTemplateLinearLayout.getLayoutParams().width = mNoteEditText.getOrignalEditTextWidth();
    	android.widget.FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) mTemplateLinearLayout.getLayoutParams();	
    	if((lp.gravity | Gravity.START) == Gravity.START)
    	{
    		mTemplateLinearLayout.setPivotX(0);
    	}
    	else
    	{
    		mTemplateLinearLayout.setPivotX(mNoteEditText.getOrignalEditTextWidth()/2);
    	}
        mTemplateLinearLayout.setPivotY(0);
        mTemplateLinearLayout.setScaleX(x);
        mTemplateLinearLayout.setScaleY(y);
        
        int templateHeight = getTemplateHeight();
        float editTextHeight = (mNoteEditText.getLayoutParams().height + templateHeight)*mScaleY - templateHeight;
        
        mTemplateLinearLayout.getLayoutParams().height = (int)((templateHeight + editTextHeight)/y);
        mNoteEditText.getLayoutParams().height = mTemplateLinearLayout.getLayoutParams().height - templateHeight;

    	mScaleX = x;
    	mScaleY = y;
    	mNoteEditText.getLayoutParams().width = mNoteEditText.getOrignalEditTextWidth();
    	mDoodleItemWidth = (int) (mNoteEditText.getLayoutParams().width * x);
    	mDoodleItemHeight = getNoteEditTextHeight()+getTemplateLayoutScaleHeight();
        mDoodleView.reInitCache(mDoodleItemWidth, mDoodleItemHeight);
        
        setPageHeight(getNoteEditText().getOrignalEditTextHeight() + getTemplateLayoutHeight());
        
        mReadOnlyMaskView.getLayoutParams().width = (int)(mNoteEditText.getLayoutParams().width *x);
        
        mPageEditorScrollBar.setScrollBarPosition((int)(getScrollX()*getScaleX()),(int)(getScrollY()*getScaleY()));
        //Begin Allen
        if(mMicroView!=null){
        	mMicroView.onTotalSizeChanged((int) (getPageTotalWidth()*getScaleX()),(int) (getPageTotalHeight()*getScaleX()));
        }
        //End Allen
    }
    //END: RICHARD
    private void calViewHeightWhetherBottom()
    {
    	//must record scrollbarcontainer margin bottom.    	
		int[] location = new  int[2] ;
		mTemplateLinearLayout.getLocationOnScreen(location);
		
		if(getScreenHeight() == location[1] + mViewHeight)
		{
			mPageEditorScrollBar.restoreEditTextVerticalScrollerContainerBottom(true);
			mViewHeight -= mPageEditorScrollBar.getEditTextVerticalScrollerContainerBottom();
		}
		else
		{
			mPageEditorScrollBar.restoreEditTextVerticalScrollerContainerBottom(false);
		}
    }
    
    private NoteFrameLayout.OnMeasureListener mOnNoteFrameLayoutSizeChaneListener = new NoteFrameLayout.OnMeasureListener() {

        @Override
        public void onMeasure(int widthMeasure, int heightMeasure, int width,
                int height) {
        	//begin smilefish hide bottom bar when keyboard or hand writing view shown
        	int[] location = new int[2];
        	mDoodleView.getLocationOnScreen(location);
        	int pageShownHeight = getScreenHeight() - location[1];
        	if(heightMeasure < pageShownHeight){
        		mBottomButtonContainer.setVisibility(View.GONE);
        		MetaData.IS_KEYBOARD_SHOW = true;	
				//add by Emmanual
				mEditorUiUtility.getPageEditor().setMicroViewVisible(false);
				//end Emmanual
        	}
        	else{
        		mBottomButtonContainer.setVisibility(View.VISIBLE);
        		MetaData.IS_KEYBOARD_SHOW = false;	
            	if(getDeviceType() < 100 || !mEditorUiUtility.isPhoneSizeMode())//emmanual to fix bug 407909
        		//emmanual to fix bug 382407
        		if(!mEditorUiUtility.isColorBoldPopupShown() && //smilefish fixed bug 400947
        				mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD){
					if (MetaData.IS_TEXTIMAGE_CONFIG) {
						MetaData.IS_TEXTIMAGE_CONFIG = false;
						MetaData.IS_KEYBOARD_SHOW = true;
						showSoftKeyboard();
					} else {
						mEditorUiUtility.setInputMode(mEditorUiUtility
						        .getInputModeFromPreference());
						//emmanual to fix bug 576211
						((EditorActivity)mContext).cancelBoxEditText();
					}
        		}				
        	}
        	//emmanual to keep textimage keyboard when device rotates
        	if(((EditorActivity)mContext).getConfigStatus()){
        		MetaData.IS_TEXTIMAGE_CONFIG = true;
        	}
        	
        	//emmanual to fix bug 452028,452523,452394,452845
			if (mEditorUiUtility.getPageEditor().getTemplateType() == MetaData.Template_type_todo
			        && (mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD
			        || mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_TEXT_IMG_SCRIBBLE)) {
				if(!mEditorUiUtility.isPhoneSizeMode()){
					mPageEditorScrollBar.getEditTextHorizontalScrollerContainer().setVisibility(View.GONE);
				}
				return;
			}
        	
        	//end smilefish
        	mViewHeight = heightMeasure;
        	mViewWidth = widthMeasure;
        	if(mViewWidth>getPageTotalWidth()*getScaleX()){//Allen
        		mViewWidth = (int) (getPageTotalWidth()*getScaleX());
        	}
        	
//    		Toast.makeText(mContext," viewWidth is " + mViewWidth , Toast.LENGTH_SHORT).show();
        	
        	//BEGIN: RICHARD FOR MULTI WINDOW
            if (getNoteEditTextWidth() > getScreenWidth()) {
            	mDoodleView.getLayoutParams().width = mViewWidth;
            	android.widget.FrameLayout.LayoutParams doodleLp = (android.widget.FrameLayout.LayoutParams) mDoodleView.getLayoutParams();	
            	doodleLp.width = mViewWidth;
            	doodleLp.gravity = Gravity.START;
 //           	Toast.makeText(mContext," SET DOODLE WIDTH TO" + doodleLp.width , Toast.LENGTH_SHORT).show();
            	mDoodleView.setLayoutParams(doodleLp);
            	
                android.widget.FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) mTemplateLinearLayout.getLayoutParams();
                android.widget.FrameLayout.LayoutParams  maskviewLp = (android.widget.FrameLayout.LayoutParams)mReadOnlyMaskView.getLayoutParams();
                maskviewLp.gravity = doodleLp.gravity;
                maskviewLp.width = mViewWidth;
                lp.gravity = doodleLp.gravity;
                lp.width = mNoteEditText.getOrignalEditTextWidth();
                
	    		mTemplateLinearLayout.setPivotX(0);
	//    		Toast.makeText(mContext," mTemplateLinearLayout setPivotX " + 0 , Toast.LENGTH_SHORT).show();

            }
            else
            {
            	mDoodleView.getLayoutParams().width = getNoteEditTextWidth();
            	android.widget.FrameLayout.LayoutParams doodleLp = (android.widget.FrameLayout.LayoutParams) mDoodleView.getLayoutParams();	
            	doodleLp.width = getNoteEditTextWidth();
            	doodleLp.gravity = Gravity.CENTER_HORIZONTAL;
  //          	Toast.makeText(mContext," SET DOODLE WIDTH TO" + doodleLp.width +" getScreenWidth()  is" +getScreenWidth() , Toast.LENGTH_SHORT).show();
            	mDoodleView.setLayoutParams(doodleLp);
            	
                android.widget.FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) mTemplateLinearLayout.getLayoutParams();
                android.widget.FrameLayout.LayoutParams  maskviewLp = (android.widget.FrameLayout.LayoutParams)mReadOnlyMaskView.getLayoutParams();
                maskviewLp.gravity = doodleLp.gravity;
                maskviewLp.width = mViewWidth;
                lp.gravity = doodleLp.gravity;
                lp.width = mNoteEditText.getOrignalEditTextWidth();
                
        		mTemplateLinearLayout.setPivotX(mNoteEditText.getOrignalEditTextWidth()/2);
  //      		Toast.makeText(mContext," mTemplateLinearLayout setPivotX " + mNoteEditText.getOrignalEditTextWidth()/2 , Toast.LENGTH_SHORT).show();

            }
            //END: RICHARD
            
        	if(getDeviceType() > 100 && mEditorUiUtility.isPhoneSizeMode())
        	{
        		mPageEditorScrollBar.setVerticalScrollRightMargin((int)((widthMeasure - (mTemplateLinearLayout.getMeasuredWidth()*getScaleX()))/2));  
	            if(mEditorUiUtility.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        		{
        			//emmanual to fix bug 449887
						((Activity) mContext).getWindow()
						        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
						adjustNoteFrameLayoutForPadLookPhoneP(heightMeasure);
        		}
        		else
        		{
        			calViewHeightWhetherBottom();
        		}
        	}
        	else
        	{
        		calViewHeightWhetherBottom();
        	}

        	if(MetaData.IS_KEYBOARD_SHOW || mEditorUiUtility.getHandWritingView().getEnable()){
        		updateVerticalScrollerHeight(mViewHeight,getPageTotalHeight()
        				+ getEdittextPaddingBottom());
        	}else{
            	updateVerticalScrollerHeight(mViewHeight,getPageTotalHeight());        		
        	}
        	mPageEditorScrollBar.updateHorizontScrollerWidth(mViewWidth,getPageTotalWidth());
        	mNoteEditText.calAutoJumpLineCount(mViewHeight);
        	ScrollViewTo(mPageEditorScrollBar.getScrollX(), mPageEditorScrollBar.getScrollY(),true);
        	
        	//Begin Allen
        	if(mMicroView!=null){
        		mMicroView.onViewWidthAndHeightChanged(mViewWidth,mViewHeight);
        	}
        	//End Allen
        	mCurrentEditText.bringCursorIntoView();
        	
        	mDoodleView.setVisibleSize(mViewWidth,mViewHeight);
        }
    };
    
    private int getEdittextPaddingBottom(){
    	return (int) (mContext.getResources().getDimension(R.dimen.edittext_padding_bottom));
    }
    
    //add by mars_li for bug about black flash when input board visibility change
    IWritePanelEnableListener mEnableListener = new IWritePanelEnableListener(){

		@Override
		public void onEnableChange(boolean visible) {
			// TODO Auto-generated method stub
			if(visible){
				updateVerticalScrollerHeight(mViewHeight,getPageTotalHeight()
    				+ getEdittextPaddingBottom());				
			}else{
				updateVerticalScrollerHeight(mViewHeight,getPageTotalHeight());
			}
		}
    	
    };
    
    
    public void registerEnableListener(){
    	 mEditorUiUtility.getHandWritingView().setEableListener(mEnableListener);
    }
    
    //emmanual
	public void updateVerticalScrollerHeight() {
		updateVerticalScrollerHeight(mViewHeight,getPageTotalHeight()
				+ getEdittextPaddingBottom());
    }

    private void updateVerticalScrollerHeight(int viewHeight, int totalHeight){
    	if(mPageEditorScrollBar != null){
			EditorScrollBarContainer vScrollBarContainer = mPageEditorScrollBar
					.getEditTextVerticalScrollerContainer();
			EditorScrollBarContainer hScrollBarContainer = mPageEditorScrollBar
					.getEditTextHorizontalScrollerContainer();
			IHandWritePanel handWritePanel = this.mEditorUiUtility
					.getHandWritingView();
			int actureViewHeight = mViewHeight;

			FrameLayout.LayoutParams hparams = (FrameLayout.LayoutParams) hScrollBarContainer.getLayoutParams();
			FrameLayout.LayoutParams vparams = (FrameLayout.LayoutParams) vScrollBarContainer.getLayoutParams();
			if (handWritePanel.getEnable() ||
					getDeviceType() > 100 && isPhoneSizeMode()
					&& mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_KEYBOARD
					&& EditorActivity.s_orientation == Configuration.ORIENTATION_PORTRAIT) { //smilefish fix bug 919574
				int bottombarHeight = (int)this.mContext.getResources().getDimension(R.dimen.bottom_bar_button_height);
				hparams.bottomMargin = handWritePanel.getHeightForScroll();
				actureViewHeight = mViewHeight
						- handWritePanel.getHeightForScroll() +bottombarHeight;
				if(PickerUtility.is720DPDevice(mContext) && isPhoneSizeMode()){
					actureViewHeight += ((FrameLayout.LayoutParams)(mTemplateLinearLayout.getLayoutParams())).bottomMargin
							- getEdittextPaddingBottom();
				}
			} else {
				hparams.bottomMargin = vparams.bottomMargin;
			}

			ViewGroup.LayoutParams params = vScrollBarContainer
					.getLayoutParams();
			params.height = actureViewHeight;
			vScrollBarContainer.requestLayout();
			if (actureViewHeight < 0)
				actureViewHeight = 0;

			mPageEditorScrollBar.updateVerticalScrollerHeight(actureViewHeight,
			        getPageTotalHeight() > totalHeight ? getPageTotalHeight()
			                : totalHeight);
			//emmanual to fix bug 444128, 444397, 444411, 445905
			if(mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_KEYBOARD 
					&& getTemplateType() == MetaData.Template_type_travel){
				mCurrentEditText.bringCursorIntoView();
			}else if (actureViewHeight == mViewHeight) {
				mPageEditorScrollBar.setScrollY(0);
				ScrollViewTo(-1, -1, true);// this maybe a bug, fix it after
											// test out
			} else {
				mCurrentEditText.bringCursorIntoView();

			}
			// vScrollBarContainer.setH
		}
    }
    //end mars_li
    public int getEditablePageHeight()
    {
    	return mPageHeight - (int)(mContext.getResources().getDimension(R.dimen.bottom_bar_button_height));
    }
    
    
    public int getPageTotalHeight()
    {
    	int height = mPageHeight * mNumPages;
    	return height;
    }
    
    public int getPageTotalWidth()
    {
    	return mNoteEditText.getOrignalEditTextWidth();
    }
    
    //Begin: Dave. To add text selector.
    public ImageView editSelectionHandleLeft;
    public ImageView editSelectionHandleRight;
    
    public boolean editSelectionLeftSelected = false;
    public boolean editSelectionRightSelected = false;
    //End: Dave.

	public PageEditor(PageEditorManager pem,EditorUiUtility utility,NoteFrameLayout layout) {
		// BEGIN: Better
		mPageEditorWrapper = new PageEditorWrapper(this);
		// END: Better
		
    	mPageEditorManager = pem;
        mEditorUiUtility = utility;
        mNoteFrameLayout = layout;
    	mContext = (Activity) (mEditorUiUtility.getContext());//Allen
        mBoxEditTextTop = mContext.getResources().getInteger(R.integer.first_line_height_small_screen);
        initPaint();
        //begin jason
        Activity activity=(Activity)mContext;
        mBottomButtonContainer=(EditorActivityButtomButtonsContainer)activity.findViewById(R.id.bottom_button_container);
        mBottomButtonContainer.setPageEditor(this);
        //end jason
        mNoteEditText = (NoteEditText) layout.findViewById(R.id.editText);
        mBoxEditText = (NoteEditText) layout.findViewById(R.id.BoxEditText); 
        
        //Begin:Dave. To add text selector. 
        editSelectionHandleLeft = (ImageView)layout.findViewById(R.id.editSelectionHandleLeft);
        editSelectionHandleRight = (ImageView)layout.findViewById(R.id.editSelectionHandleRight);
        
        
        //Begin: 0807. Dave. To fix a bug: the memo mode can not receive touch event from selector.
        editSelectionHandleLeft.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				// TODO Auto-generated method stub
				if(MetaData.ENABLE_SELECTOR_HANDLER)
					editSelectionLeftSelected = true;
				
                final float rawX = event.getRawX();
                final float rawY = event.getRawY();
                
                event.setLocation(rawX, rawY);
				                
				if(mCurrentEditText != null)
					mCurrentEditText.dispatchTouchEvent(event);
				
				return true;

			}
		});
        
        editSelectionHandleRight.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				// TODO Auto-generated method stub
				if(MetaData.ENABLE_SELECTOR_HANDLER)
					editSelectionRightSelected = true;
				
                final float rawX = event.getRawX();
                final float rawY = event.getRawY();
                
                event.setLocation(rawX, rawY);
				                
				if(mCurrentEditText != null)
					mCurrentEditText.dispatchTouchEvent(event);
					
				return true;
			}
		});
        //End.0807.Dave.
        
        editSelectionHandleLeft.setVisibility(View.INVISIBLE);
        editSelectionHandleRight.setVisibility(View.INVISIBLE);

        editSelectionLeftSelected = false;
        editSelectionRightSelected = false;
        //End: Dave.
        
        //Begin Allen
        mTemplateLinearLayout = (TemplateLinearLayout) layout.findViewById(R.id.template_linearlayout);//Allen
        mTemplateType = ((EditorActivity)mContext).getBookTemplateType();//Allen
        mTemplateLinearLayout.setPageEditor(this);
        mTemplateUtility = new TemplateUtility(mContext,mTemplateLinearLayout,this,mTemplateType);
        mTemplatelayout = mTemplateUtility.PrepareTemplateViewStub();
		//begin jason
		if (MethodUtils.isEnableAirview(mContext)) {
			mTemplateLinearLayout.setStylusButtonPressListener(((EditorActivity)mContext).new StylusButtonPressImpl());
		}
		//end jason
        //End Allen
        
        if(getTemplateType() == MetaData.Template_type_meeting) //Carol-add hint for meeting template
        	mNoteEditText.setHint(mNoteEditText.getContext().getString(R.string.template_meeting_text_hint));
        if(getTemplateType() == MetaData.Template_type_travel) //Emmanual-add hint for travel template
        	mNoteEditText.setHint(mNoteEditText.getContext().getString(R.string.template_travel_text_hint));

        mAdditionalViewLayout = (LinearLayout)layout.findViewById(R.id.phone_page_additional_view_layout);
        
        // BEGIN: archie_huang@asus.com
        // To fix TT-223986
        EditorInfo noteEditTextInfo = new EditorInfo();
        noteEditTextInfo.packageName = mNoteEditText.getContext().getPackageName();
        noteEditTextInfo.fieldId = mNoteEditText.getId();
        mNoteEditTextInputConnection = mNoteEditText.onCreateInputConnection(noteEditTextInfo);

        EditorInfo boxEditTextInfo = new EditorInfo();
        boxEditTextInfo.packageName = mBoxEditText.getContext().getPackageName();
        boxEditTextInfo.fieldId = mBoxEditText.getId();
        mBoxEditTextInputConnection = mBoxEditText.onCreateInputConnection(boxEditTextInfo);
        // END: archie_huang@asus.com

        initEditText();
        
        if (mTemplateType == MetaData.Template_type_todo) {
        	//begin smilefish
        	if(((EditorActivity)mContext).isPhoneScreen())
        		mTemplateLayoutHeight = mContext.getResources().getDimensionPixelSize(R.dimen.todo_total_height_phone);
        	else
        		mTemplateLayoutHeight = mContext.getResources().getDimensionPixelSize(R.dimen.todo_total_height_pad);

            mTemplateUtility.setTodoItemsMaxHetigt(mTemplateLayoutHeight);
            //end smilefish
        }
        setPageHeight(getNoteEditTextHeight() + getTemplateLayoutScaleHeight());//Allen
        
        mDoodleView = (DoodleView) layout.findViewById(R.id.doodleSurfaceView);
        initDoodleView();

    	mReadOnlyMaskView = (ReadOnlyMaskView) layout.findViewById(R.id.readOnlyMaskView);
    	initReadOnlyView();
        
        //BEGIN: Richard fix bug for phone look pad
    	//doodleview readonlymaskview templateLinearLayout should use the same gravity
        android.widget.FrameLayout.LayoutParams doodleLp = (android.widget.FrameLayout.LayoutParams) mDoodleView.getLayoutParams();	
        android.widget.FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) mTemplateLinearLayout.getLayoutParams();
        android.widget.FrameLayout.LayoutParams  maskviewLp = (android.widget.FrameLayout.LayoutParams)mReadOnlyMaskView.getLayoutParams();
        maskviewLp.gravity = doodleLp.gravity;
        lp.gravity = doodleLp.gravity;
        //END: Richard fix bug for phone look pad

        //BEGIN: RICHARD
        NoteFrameLayout noteFrameLayout = layout;//(NoteFrameLayout) ((Activity) mEditorUiUtility.getContext()).findViewById(R.id.noteFramelayout);
        if (noteFrameLayout != null) {
            noteFrameLayout.setOnSizeChangeListner(mOnNoteFrameLayoutSizeChaneListener);
        }
        //END: RICHARD
        
        //BEGIN: RICHARD
        //CFG.setPath(context.getDir("Data", 0).getAbsolutePath());
        if(CFG.getCanDoVO() == true)//darwin
        {
	        try
	        {
	        	mAsusInputRecognizer = new AsusInputRecognizer();
	        	mAsusInputRecognizer.prepareUnstructuredInputRecognizer();
	        }catch(Exception e)
	        {
	        	e.printStackTrace();
	        	mAsusInputRecognizer = null;
	        }
        }
        //END:RICHARD
        
        // BEGIN: Better
        if (mEditorUiUtility != null) {
        	Context context = mEditorUiUtility.getContext();
        	if (context != null) {
        		mPreferences = context.getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
        	}
        }
        // END: Better

        //BEGIN: RICHARD TEST
        mPageEditorScrollBar = new PageEditorScrollBar(mContext,this,layout);
        mPageEditorScrollBar.initScrollerBar(layout, isPhoneSizeMode());
        //END: RICHARD 
        
        //Begin Allen
        mMicroView = (MicroView)layout.findViewById(R.id.microView);
        if(mMicroView!=null){
        	mMicroView.initViewPort(this);
        	mMicroView.setOnScrollChangedListener(onMicroViewScrollChanged);
        }
        //End Allen

        View additionalView1 = layout.findViewById(R.id.phone_page_additional_view1);
        View additionalView2 = layout.findViewById(R.id.phone_page_additional_view2);

        if(isPhoneSizeMode())
        {
        	//begin smilefish
        	if(PickerUtility.isPhoneOrSmallScreenPad(mContext) && mContext.getResources().getConfiguration().orientation 
        			== Configuration.ORIENTATION_PORTRAIT){
            	layout.setBackgroundColor(mEditorUiUtility.getBookColor());
        	}else{
        		layout.setBackgroundColor(mContext.getResources().getColor(R.color.edit_page_bg_color)); //smilefish
        	}
        	//end smilefish
        	
            if (additionalView1 == null || additionalView2 == null) {
                return;
            }

            additionalView1.setVisibility(View.VISIBLE);
            additionalView2.setVisibility(View.VISIBLE);

            if (mEditorUiUtility.getBookColor() == MetaData.BOOK_COLOR_YELLOW) {
                additionalView1.setBackgroundResource(R.drawable.for_phone_page_lshadow_l_yellow);
                additionalView2.setBackgroundResource(R.drawable.for_phone_page_rshadow_l_yellow);
            }
        }
        else
        {
        	layout.setBackgroundColor(mEditorUiUtility.getBookColor());
        }
        
        //emmanual to fix TX201LAF Bug 428430 
		if (mEditorUiUtility.getBookColor() == MetaData.BOOK_COLOR_YELLOW
				&& getDeviceType() > 100 && !isPhoneSizeMode()) {
			if (additionalView1 == null || additionalView2 == null) {
				return;
			}
            additionalView1.setVisibility(View.VISIBLE);
            additionalView2.setVisibility(View.VISIBLE);
			additionalView1.setBackgroundColor(mEditorUiUtility.getBookColor());
			additionalView2.setBackgroundColor(mEditorUiUtility.getBookColor());
		}

    }
	
	/**
	 * @author noah_zhang
	 * @return
	 */
	public TemplateUtility getTemplateUtility(){
		return mTemplateUtility;
	}
    
    private void initReadOnlyView() {
    	//BEGIN: RICHARD TEST MODIFY
        int layoutWidth = LayoutParams.MATCH_PARENT;
        int layoutHeight = LayoutParams.MATCH_PARENT;
        int layoutGravity = Gravity.CENTER_HORIZONTAL;

        mReadOnlyMaskView.setLayoutParams(new FrameLayout.LayoutParams(layoutWidth, layoutHeight, layoutGravity));
        mReadOnlyMaskView.setPageEditor(this);
        //END: RICHARD
    }
    
    public PageEditorScrollBar getPageEditorScrollBar()
    {
    	return mPageEditorScrollBar;
    }
    
	 //Begin Allen    
    public boolean isScrollBarVisible(){
    	if(mPageEditorScrollBar!=null){
    		return mPageEditorScrollBar.getMaxScrollY()>0||mPageEditorScrollBar.getMaxScrollY()>0;
    	}
    	else{
    		return false;
    	}
    }
    
    public boolean dispatchTouchEventToMicroView(MotionEvent event){
    	if(mMicroView!=null){
    		return mMicroView.onHandWritingViewTouchEvent(event);
    	}
    	return false;
    }
    
    public void setMicroViewVisible(boolean visible){
		if (((mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_SCRIBBLE || mEditorUiUtility
		                .getInputMode() == InputManager.INPUT_METHOD_TEXT_IMG_SCRIBBLE) && mEditorUiUtility
		                .isHandWriteViewEnable()) || mBottomButtonContainer.getVisibility() == View.GONE) {
			if (mMicroView != null) {
				mMicroView.setMicroViewVisible(false);
			}
		} else if (mMicroView != null) {
			mMicroView.setMicroViewVisible(visible);// Allen
		}
    }
    
    public void setRegenerateMicroView(boolean regenerateMicroView){
    	if(mMicroView!=null){
    		mMicroView.setRegenerateMicroView(regenerateMicroView);
    	}
    }
    
    private MicroView.onScrollChangedListener onMicroViewScrollChanged = new MicroView.onScrollChangedListener(){

		@Override
		public void onScrollChanged(int scrollX, int scrollY) {
			ScrollViewTo(scrollX, scrollY, true);			
		}
    
    };
    
    public boolean IsToDoPageFull(int offset){
        return mTemplateUtility.IsToDoPageFull(offset);
    }	
    
    public void setLoadingPageIsFullListener(
			onLoadingPageIsFullListener loadingPageIsFullListener) {
    	if(mTemplateUtility != null){
    		mTemplateUtility.setLoadingPageIsFullListener(loadingPageIsFullListener);
    	}
	}
    
    //darwin
    public boolean isPadPageWithXLargePortraitModeIgnoreRotation() {
    	if(mNoteEditText==null){
    		return false;
    	}
    	return (getDeviceType()>100) && !isPhoneSizeMode() && (EditorActivity.s_orientation == Configuration.ORIENTATION_PORTRAIT);
    }
    //darwin
    
    public void switchBaselineMode(boolean enableBaseLine){
    	mNoteEditText.switchBaselineMode(enableBaseLine);
    }
    //End Allen
    private void initPaint() {
        mTextScribblePaint = new Paint();
        mDoodlePaint = new Paint();

        PaintSelector.initPaint(mTextScribblePaint, Color.BLACK, MetaData.SCRIBBLE_PAINT_WIDTHS_NORMAL);
        PaintSelector.initPaint(mDoodlePaint, Color.BLACK, MetaData.DOODLE_PAINT_WIDTHS[MetaData.DOODLE_DEFAULT_PAINT_WIDTH]);
    }
    //darwin
    public void setSelectAllText()
    {
    	mCurrentEditText.selectAll();
    	mCurrentEditText.setSelection(mCurrentEditText.getSelectionStart(),mCurrentEditText.getSelectionEnd()); //Dave.
        mPreferences.edit().putInt(MetaData.PREFERENCE_SELECTION_TEXT_START, 0).commit(); //by show
        mPreferences.edit().putInt(MetaData.PREFERENCE_SELECTION_TEXT_END, mCurrentEditText.getText().length()).commit();//by show
    }
    //darwin
    private void initEditText() {
    	mNoteEditText.setContentType(NoteItemArray.CONTENT_NOTEEDIT);//Allen
        mNoteEditText.initNoteEditText(this, mEditorUiUtility.getBookFontSize(), mEditorUiUtility.isPhoneSizeMode());
		if (mTemplateType == MetaData.Template_type_todo) {// Allen
			mNoteEditText.ResetHeight(0);
		} else if (mTemplateType == MetaData.Template_type_meeting) { // smilefish
			mTemplateUtility.setMeetingFontSize();
		} else if (mTemplateType == MetaData.Template_type_travel) { // emmanual
			mTemplateUtility.setDiaryFont();
		}
        
        mNoteEditText.setHighlightColor(NOTEEEDITTEST_HIHGLIGNT_COLOR);
        
        mBoxEditText.setContentType(NoteItemArray.CONTENT_BOXEDIT);//Allen
        mBoxEditText.setLineCountLimited(BOX_EDITTEXT_LINE_LIMIT);
        mBoxEditText.initNoteEditText(this, mEditorUiUtility.getBookFontSize(), mEditorUiUtility.getDeviceType()<=100);
        mBoxEditText.setHighlightColor(NOTEEEDITTEST_HIHGLIGNT_COLOR);
        mBoxEditText.setPadding(10, mBoxEditTextTop, 10, 5);
        mBoxEditText.setScaleX(1);
        mBoxEditText.setScaleY(1);

        mCurrentEditText = mNoteEditText;
        mCurrentInputConnection = mNoteEditTextInputConnection;
    }
    
    // BEGIN: Better
    private int mDoodleItemWidth = 0;
    private int mDoodleItemHeight = 0;
    
    public int getDoodleItemWidth() {
    	return mDoodleItemWidth;
    }
    
    public void setDoodleItemWidth(int width) {
    	mDoodleItemWidth = width;
    }
    
    public int getDoodleItemHeight() {
    	return mDoodleItemHeight;
    }
    
    public void setDoodleItemHeight(int height) {
    	mDoodleItemHeight = height;
    }
    
    // END: Better

    private void initDoodleView() {
        int noteEditTextWidth = getNoteEditTextWidth();
        int noteEditTextHeight = getNoteEditTextHeight();
        int screenWidth = getScreenWidth();
        int screenHeight = getScreenHeight();
        int layoutWidth = LayoutParams.MATCH_PARENT;
        int layoutGravity = Gravity.START;

        mDoodleView.setPaintTool(new PathDrawTool(PathDrawTool.NORMAL_TOOL));
        mDoodleView.setPaint(mDoodlePaint);
        mDoodleView.bind(mPageEditorWrapper);
        mDoodleItemWidth = noteEditTextWidth;
        mDoodleItemHeight = noteEditTextHeight+getTemplateLayoutScaleHeight();
        mDoodleView.initCanvasSize(mDoodleItemWidth, mDoodleItemHeight);//Allen

        if (noteEditTextWidth < screenWidth) {
            layoutWidth = noteEditTextWidth;
            layoutGravity = Gravity.CENTER_HORIZONTAL;
        }
        
        //BEGIN: RICHARD FOR MULTI WINDOW
        if(noteEditTextWidth > screenWidth)
        {
        	layoutWidth = screenWidth;
        }
        //END: RICHARD
        
        if (noteEditTextHeight+getTemplateLayoutScaleHeight() < screenHeight) {
            layoutGravity = Gravity.CENTER_HORIZONTAL;
        }

        mDoodleView.setLayoutParams(new FrameLayout.LayoutParams(layoutWidth, LayoutParams.MATCH_PARENT, layoutGravity));
        mDoodleView.setBackground(mEditorUiUtility.getBookColor());
    }

    public void setClipBoardAvailableListener(onClipBoardAvailableListener listener) {
        mClipBoardAvailableListener = listener;
    }

    public void setUndoStackAvailableListener(onUndoStackAvailableListener listener) {
        mUndoStackAvailableListener = listener;
    }

    public void setRedoStackAvailableListener(onRedoStackAvailableListener listener) {
        mRedoStackAvailableListener = listener;
    }

    public void setDoodleItemAvailableListener(onDoodleItemAvailableListener listener) {
        mDoodleItemAvailableListener = listener;
    }

    public void setEditorColorChangeListener(onEditorColorChangeListener listener) {
        mEditorColorChangeListener = listener;
    }

    public void setEditorBoldChangeListener(onEditorBoldChangeListener listener) {
        mEditorBoldChangeListener = listener;
    }

    public void insertSpace() {
    	KeyEvent downEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE);
    	KeyEvent upEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE);
        mCurrentInputConnection.sendKeyEvent(KeyEvent.changeFlags(downEvent, KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
        mCurrentInputConnection.sendKeyEvent(KeyEvent.changeFlags(upEvent, KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
    }

    public void insertEnter() {
    	KeyEvent downEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER);
    	KeyEvent upEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER);
        mCurrentInputConnection.sendKeyEvent(KeyEvent.changeFlags(downEvent, KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
        mCurrentInputConnection.sendKeyEvent(KeyEvent.changeFlags(upEvent, KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
    }

    public void insertBackSpace() {
    	KeyEvent downEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
    	KeyEvent upEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL);
        mCurrentInputConnection.sendKeyEvent(KeyEvent.changeFlags(downEvent, KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
        mCurrentInputConnection.sendKeyEvent(KeyEvent.changeFlags(upEvent, KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE));
    }
    
    /**
     * 插入字符串
     * @author noah_zhang
     * @param text
     */
    public void insertText(String text) {
        int pos = mCurrentEditText.getSelectionEnd();
        pos = pos < 0 ? 0 : pos;
        mCurrentEditText.getText().insert(pos, text);
    }
    
    /**
     * @author Emmanual
     * @param text
     */
    public void insetSharedText(String text) {
    	if(getTemplateType() == MetaData.Template_type_travel){    		
            int pos = mNoteEditText.getSelectionEnd();
            pos = pos < 0 ? 0 : pos;
            mNoteEditText.getText().insert(pos, text);
    	} else{
	        int pos = mCurrentEditText.getSelectionEnd();
	        pos = pos < 0 ? 0 : pos;
	        mCurrentEditText.getText().insert(pos, text);
    	}
    }

    public void toggleSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) mEditorUiUtility.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void showSoftKeyboard() {
        // BEGIN: archie_huang@asus.com
        // To avoid NullPointerException
        Context context = EditorUiUtility.getContextStatic();
        if (!checkHaveInputManager() || context == null) {
            return;
        }
        if (mInputManager.getInputMode() == InputManager.INPUT_METHOD_KEYBOARD ||
                mInputManager.getInputMode() == InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if(mCurrentEditText.hasFocus())
            imm.showSoftInput(mCurrentEditText, 0);
        }
        // END: archie_huang@asus.com
    }

    public void hiddenSoftKeyboard() {
        // BEGIN: archie_huang@asus.com
        // To avoid NullPointerException
        Context context = EditorUiUtility.getContextStatic();
        if (!checkHaveInputManager() || context == null) {
            return;
        }
        if (mInputManager.getInputMode() == InputManager.INPUT_METHOD_KEYBOARD ||
                mInputManager.getInputMode() == InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if(mCurrentEditText.hasFocus())            
            imm.hideSoftInputFromWindow(mCurrentEditText.getWindowToken(), 0);
        }
        // END: archie_huang@asus.com
    }

    private boolean checkHaveInputManager() {
        if (mInputManager == null) {
            mInputManager = mEditorUiUtility.getInputManager();
        }

        return mInputManager == null ? false : true;
    }

    public void cleanEdited(boolean onSave){
    	mNoteEditText.setIsModified(false);
    	mTemplateUtility.cleanTemplateEditModified();//Allen
    	mDoodleView.setModified(false);
    	EnableSaveButton(false);//by show
    	if(!onSave){
    		setRegenerateMicroView(true);//Allen
    	}
    	//darwin
    	mDoodleView.clearRedoUndoList();
    	mNoteEditText.clearUndoRedoStack();
    	//darwin
    }
    
    //Begin Allen
    public void onModified(boolean mIsModified){
    	if(mIsModified){
    		setRegenerateMicroView(true);//Allen
    		EnableSaveButton(true);//darwin
    	}
    }

    //End Allen
    
    //darwin
    public void EnableSaveButton(boolean bEnable)
    {
    	((EditorActivity)mContext).EnableSaveButton(bEnable);
    }
    //darwin
    public void setDoodleModified() {
        mDoodleView.setModified(true);
    }
    
    //emmanual
    public void setEditTextModified(){
    	mCurrentEditText.setIsModified(true);
    }
    
    public boolean isEditTextModified() {
    	return mTemplateUtility.isTemplateEditModified()||mNoteEditText.isModified();//Allen
    }
    
    public boolean isDoodleModified() {
    	return mDoodleView.isModified();//Allen
    }

    //Begin Allen
    public boolean isNoteEditTextLayoutReady()
    {
    	return mNoteEditText.isLayoutReady();
    }
    //End Allen

    public List<String> getPageUsedFileList() {
        List<String> fileList = new LinkedList<String>();

        // NoteEditText
        NoteSendIntentItem[] items = mNoteEditText.getText().getSpans(0, mNoteEditText.getText().length(), NoteSendIntentItem.class);
        for (NoteSendIntentItem item : items) {
            fileList.add(item.getFileName());
        }
                
        // Doodle
        fileList.addAll(mDoodleView.getUsingFiles());

        //travel template image
        fileList.addAll(mTemplateUtility.getUsedFileList());//Allen
        
        return fileList;
    }

    public void undo() {
        if (!checkHaveInputManager()) {
            return;
        }

        if (mInputManager.getInputMode() == InputManager.INPUT_METHOD_DOODLE) {
            mDoodleView.undo();
        }
        else {
            mCurrentEditText.undo();
        }
    }

    public void redo() {
        if (!checkHaveInputManager()) {
            return;
        }

        if (mInputManager.getInputMode() == InputManager.INPUT_METHOD_DOODLE) {
            mDoodleView.redo();
        }
        else {
            mCurrentEditText.redo();
        }
    }
    
    public void addItemToEditText(CharSequence string,String fileName) {
    	addItemToEditText(string);
    	mIsAttachmentModified = true;
    	m_list.add(fileName);
    }
    //End   Darwin_Yu@asus.com
	
    public void addItemToEditText(CharSequence string) {

        Editable editable = mCurrentEditText.getText();
        int selectionStart = Math.min(mCurrentEditText.getSelectionStart(), mCurrentEditText.getSelectionEnd());
        int selectionEnd = Math.max(mCurrentEditText.getSelectionStart(), mCurrentEditText.getSelectionEnd());

        selectionStart = selectionStart < 0 ? 0 : selectionStart;
        selectionEnd = selectionEnd < 0 ? 0 : selectionEnd;

        Log.d(TAG, "addItemToEditText " + selectionStart + " to " + selectionEnd + " with " + string);
        mCurrentEditText.setIsUsingSetColorOrStyle(false);
        try{  //smilefish fix google play bug 
	        if (selectionStart < selectionEnd) {
	            editable.replace(selectionStart, selectionEnd, string);
	        }
	        else if(selectionStart <= editable.length()){
	            editable.insert(selectionStart, string);
	        }
        }catch(IndexOutOfBoundsException e){
        	e.printStackTrace();
        }
        mCurrentEditText.setIsUsingSetColorOrStyle(true);
    }
    
    public void addBmpToDoodleView(Bitmap bmp, String fileName){
    	addBmpToDoodleView(bmp, fileName, Float.MIN_VALUE);
    }
    
    /**
     * 
     * @param bmp
     * @param fileName
     * @param dy
     */
	public void addBmpToDoodleView(Bitmap bmp, String fileName,
			float dy) {
		// BEGIN:Show, Modify reason:change image
		if (mInputManager.getInputMode() == InputManager.INPUT_METHOD_SELECTION_DOODLE) {
			mDoodleView.updateGraphic(bmp, fileName, DoodleView.GRAPHIC_CHANGE);
		} else {
			// END:Show
			boolean result = false;
			if(dy == Float.MIN_VALUE){
				result = mDoodleView.insertGraphic(bmp, fileName);
			}else {
				mDoodleView.insertGraphic(bmp, fileName, dy);
			}
			if (result) {
				// Begin Darwin_Yu@asus.com
				mIsAttachmentModified = true;
				m_list.add(fileName);
				// End Darwin_Yu@asus.com
				mInputManager.setInputMode(InputManager.INPUT_METHOD_SELECTION_DOODLE);
				if(getTemplateType() == MetaData.Template_type_todo){
					mEditorUiUtility.redrawDoodleView();
				}
			}
		}// By Show
	} // END: archie_huang@asus.com
    //begin jason
    public void addShapeGraphicToDoodleView(int type,Path p,String fileName){
    	mDoodleView.insertShapeGraphic(type, p, fileName);
    	mIsAttachmentModified = true;
    	m_list.add(fileName);
        mInputManager.setInputMode(InputManager.INPUT_METHOD_INSERT);
    }
    public ShapeManager getShapeManager(){
    	if (mShapeManager==null) {
    		mShapeManager=ShapeManager.getInstance();
		}
    	return mShapeManager;
    }
    public void addDoodleItemToDoodleView(DoodleItem doodleItem ,String path){
    	if (doodleItem!=null) {
			mDoodleView.mergeDoodleItem(doodleItem, path,getNotePage());
		}
    }
    //end jason
    // BEGIN: archie_huang@asus.com
    public void copyDoodleObject(NotePage page) {
        mDoodleView.copy(page);
        if (mClipBoardAvailableListener != null) {
            mClipBoardAvailableListener.onAvailableChange(true);
        }
    }// END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public void deleteDoodleObject() {
        mDoodleView.delete();
    }// END: archie_huang@asus.com

    public void setInsertMode(boolean isInsert) {
    	mDoodleView.setInsertMode(isInsert);
    }
    
    // BEGIN: archie_huang@asus.com
    public void pastDoodleObject(NotePage page) {
        mDoodleView.past(page);
    } // END: archie_huang@asus.com

    public void loadDoodle(DoodleItem doodleItem) {
        mDoodleView.load(doodleItem, mEditorUiUtility.getBookColor());
        mDoodleView.redrawAll(true);
    }
    
    public void loadNoteEditText(ArrayList<NoteItemArray> allNoteItems) {
    	mTemplateUtility.LoadTemplateContent(allNoteItems);//Allen
    	// BEGIN: archie_huang@asus.com
        // To fix TT-220984
        mNoteEditText.setText(null);
        // END: archie_huang@asus.com
    	
        //BEGIN: RICHARD MOVE HERE
          ScrollViewTo(0, 0,true);//Allen
        //END: RICHARD
        
    	if(allNoteItems==null||allNoteItems.size() == 0){//Allen
    		mNoteEditText.clearUndoRedoStack();
    		return;
    	}
    	
    	NoteItem[] noteItems = null;
    	for(NoteItemArray items:allNoteItems){
    		if(items.getTemplateItemType() == NoteItemArray.TEMPLATE_CONTENT_DEFAULT_NOTE_EDITTEXT){
    			noteItems = items.getNoteItemArray();
    			break;
    		}
    	}
    	

        if (noteItems == null || noteItems.length == 0) {
            mNoteEditText.clearUndoRedoStack();            
            return;
        }
        String str = noteItems[0].getText();
        Editable editable = new SpannableStringBuilder(str);

        for (int i = 1; i < noteItems.length; i++) {
            if (noteItems[i].getStart() < 0 || noteItems[i].getEnd() < 0
                    || noteItems[i].getStart() > editable.length() /*|| noteItems[i].getEnd() > editable.length()*/) {
                Log.w(TAG, "This NoteItem is wrong." +
                        " noteItem = " + noteItems[i].getClass().getSimpleName() +
                        " start = " + noteItems[i].getStart() +
                        " end = " + noteItems[i].getEnd());
                continue;
            }
            
            // BEGIN: Shane_Wang 2012-11-5
            if(noteItems[i].getEnd() > editable.length()) {
            	noteItems[i].setEnd(editable.length());
            }
            // END: Shane_Wang 2012-11-5
            
            editable.setSpan(noteItems[i], noteItems[i].getStart(), noteItems[i].getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        setFontSize(editable);

        mNoteEditText.setIsFirstTimeLoad(true);
        // BEGIN: archie_huang@asus.com
        // To fix TT-218753
        mNoteEditText.load(editable);
        // END: archie_huang@asus.com
        mNoteEditText.setIsFirstTimeLoad(false);
        mNoteEditText.clearUndoRedoStack();

        // BEGIN: Better
        mNoteEditText.setSelection(0);
        // END: Better
    }

    // BEGIN: archie_huang@asus.com
    public void drawHint(RectF hint) {
        mDoodleView.drawHint(hint);
    } // END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public void setFontSize(Editable editable) {
        NoteHandWriteItem[] handwriteitems = editable.getSpans(0, editable.length(), NoteHandWriteItem.class);
        for (NoteHandWriteItem item : handwriteitems) {
            if (item instanceof NoteHandWriteBaselineItem) {
                item.setFontHeight(getImageSpanHeight());
            }
            else {
                item.setFontHeight(getFullImageSpanHeight());
            }
        }
    } // END: archie_huang@asus.com

    public int getNoteEditTextHeight() {
        return mNoteEditText.getNoteEditTextHeight();
    }
    
 // BEGIN: Better
    private int mNumPages = 1;
    private int mPageHeight = 0;
    
    public void setPageNum(int num) {
        if(MetaData.IS_ENABLE_CONTINUES_MODE)
        {
	    	boolean isUpdateVScrollbar = false;
	    	if (num != mNumPages) {
	    		isUpdateVScrollbar = true;
	    	}
	    	mNumPages = num;
	    	if (isUpdateVScrollbar) {
	    		onPageNumChanged();
	    		mDoodleView.onPageNumChanged();
	    	}
	    	if(mMicroView!=null){
	    		mMicroView.onTotalSizeChanged((int) (getPageTotalWidth()*getScaleX()),(int) (getPageTotalHeight()*getScaleX()));//Allen
	    	}
        }		
    }
    
    public int getPageNum() {
    	return mNumPages;
    }
    
    public void setPageHeight(int height) {
    	mPageHeight = height;
    }
    
    public int getPageHeight() {
    	return mPageHeight;
    }
    
    public void onLoadPage() {
    	setPageNum(1);
    	mNoteEditText.setFirstDrawn();
    	if(mTemplatelayout != null)
    	{
    		mTemplatelayout.setFirstDrawn();
    	}
    }
    
    public void onPageNumChanged() {
    	updateVerticalScrollerHeight(mViewHeight, getPageTotalHeight());
    	mPageEditorScrollBar.setScrollBarPosition((int) (getScrollX() * getScaleX()), (int) (getScrollY() * getScaleY()));
    	mTemplateUtility.onPageNumChanged(mNumPages);//Allen
    }
    // END: Better

    //Begin Allen
    public int getTemplateHeight()
    {
    	int height = 0;
    	if(mTemplatelayout != null)
    	{
    		height = mTemplateLayoutHeight;
    	}
    	return height;
    }
    public int getViewWidth()
    {
    	return mViewWidth;
    }
    
    public int getViewHeight()
    {
    	IHandWritePanel handWritePanel =  this.mEditorUiUtility.getHandWritingView();
		int actureViewHeight = mViewHeight;
		if(handWritePanel.getEnable()){
			//add height of bottombar
			int bottombarHeight = (int)this.mContext.getResources().getDimension(R.dimen.bottom_bar_button_height);
			actureViewHeight = actureViewHeight - handWritePanel.getHeightForScroll() + bottombarHeight;
		}
		if(actureViewHeight<0)
			actureViewHeight = 0;
		
    	return actureViewHeight;
    }


    public Boolean isNeedVerticalScroller()
    {
    	if(mPageEditorScrollBar.getMaxScrollY() > 0)
    	{
    		return true;
    	}
    	return false;
    }
    
    public int getBottomButtonHeight()
    {
		View bottomButtonContainer = ((Activity)mContext).findViewById(R.id.bottom_button_container);
		int bottomButtonHeght = 0;
		if(bottomButtonContainer!=null){
			bottomButtonHeght = bottomButtonContainer.getHeight();
		}
		return bottomButtonHeght;
    }
    //End Allen
    public int getNoteEditTextWidth() {
        return mNoteEditText.getNoteEditTextWidth();
    }

    // BEGIN: archie_huang@asus.com
    public Bitmap getScreenShot(int width, int height, boolean isTextOnly, boolean isHideGrid) {
        Bitmap resultBmp = null;
        try {
            resultBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas resultCanvas = new Canvas(resultBmp);
            mIsScrollDoodle = false;
            //Begin Siupo
            int templateHeight = 0;
            int templateDoodleScaleHeight = 0;
            if(mTemplatelayout!=null)
            {
            	templateHeight = mTemplateLayoutHeight;
            	Log.v("PageEditor--templatePaddingTop", String.valueOf(templateHeight));
            	templateDoodleScaleHeight = getTemplateLayoutScaleHeight();
            	Log.v("PageEditor--templateDoodlePaddingTop", String.valueOf(templateDoodleScaleHeight));
            }
            //End Siupo
            if (isTextOnly) {
                resultBmp.eraseColor(Color.WHITE);
                mDoodleView.getLineLock(resultCanvas, width, height, true, isHideGrid, templateHeight, -1);
            }
            else {
                mDoodleView.getResult(resultCanvas, width, height, true, isHideGrid, templateDoodleScaleHeight, -1,true);//darwin
            }
            mNoteEditText.getResultFull(resultCanvas, width, height, isTextOnly, templateHeight, -1);//darwin
            
            this.drawTemplateView(resultCanvas, width, 
            		(int) ((float)mTemplateLayoutHeight/(mNoteEditText.getOrignalEditTextHeight()+mTemplateLayoutHeight)*height));
            //darwin
            mIsScrollDoodle = true;
        }
        catch (OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] Create screen shot Failed !!!");
        }
        return resultBmp;
    } // END: archie_huang@asus.com
    
    public Bitmap getScreenShotNotForPdf(int width, int height, boolean isTextOnly, boolean isHideGrid,float scale) {
        Bitmap resultBmp = null;
        //emmanual to fix bug 418926
        int mTemplateHeight = getTemplateLayoutScaleHeight();
		if (getTemplateType() == MetaData.Template_type_travel
		        && getDeviceType() > 100 && !isPhoneSizeMode() 
		        || PickerUtility.getDeviceType(mContext) == MetaData.DEVICE_TYPE_320DP) {
			mTemplateHeight += mContext.getResources().getDimension(R.dimen.template_meeting_thumbnail_bottom);
		}
		
        int iWidth = (int)(width * scale);
        int iHeight = (int)(((mTemplatelayout!=null && (this.getTemplateType() == MetaData.Template_type_todo)) ?
				height :
        		(height + mTemplateHeight * getNoteEditTextScaleY()) * getPageNum()
        		) * scale);
        try {
            resultBmp = Bitmap.createBitmap(iWidth, 
            		iHeight, 
            		Bitmap.Config.ARGB_8888);
            Canvas resultCanvas = new Canvas(resultBmp);
            mIsScrollDoodle = false;
            
            int templatePaddingTop = 0;
            int templateDoodlePaddingTop = 0;
            if(mTemplatelayout!=null && !isTextOnly)
            {
            	templatePaddingTop = mTemplateLayoutHeight;
            	templateDoodlePaddingTop = mTemplateHeight;
            }
            
            if (isTextOnly) {
                resultBmp.eraseColor(Color.WHITE);
                mDoodleView.getLineLock(resultCanvas, iWidth, iHeight, true, isHideGrid, templatePaddingTop, -1);
            }
            else {
                mDoodleView.getResult(resultCanvas, iWidth, iHeight, true, isHideGrid, templateDoodlePaddingTop, -1,true);//darwin
            }
            mNoteEditText.getResultFull(resultCanvas, iWidth, iHeight, isTextOnly, templatePaddingTop, -1);//darwin
            
            if(!isTextOnly)
            {
            	drawTemplateView(resultCanvas, iWidth,
            		(int) ((float)mTemplateLayoutHeight * scale));//Allen/(mNoteEditText.getOrignalEditTextHeight()+mTemplateLayoutHeight)*height
            }
            //darwin
            mIsScrollDoodle = true;
        }
        catch (OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] Create screen shot Failed !!!");
        }
        return resultBmp;
    } // END: archie_huang@asus.com

    //Begin Allen
    public void getResult(Canvas canvas,int width,int height,boolean getFullPage,boolean isFromShare){//darwin                          
        mIsScrollDoodle = false; 
    	if(getFullPage){
    		if(MetaData.IS_ENABLE_CONTINUES_MODE){
    			mDoodleView.getResult(canvas, width, height, false, false, mTemplateLayoutHeight, -1,false);
    		}
    		else{
    			mDoodleView.getCacheResult(canvas,width,height,isFromShare);
    		} 		
    		mNoteEditText.getResultFull(canvas, width, height, false, mTemplateLayoutHeight, -1);
    		int thumbnailHeight = 0;
    		if(mTemplateType==MetaData.Template_type_todo){
    			thumbnailHeight = height;
    		}
    		else{
    			thumbnailHeight = (int) ((float)mTemplateLayoutHeight/((mNoteEditText.getOrignalEditTextHeight()+mTemplateLayoutHeight)*mNumPages)*height);
    		}
    		drawTemplateView(canvas, width, thumbnailHeight);   
    	}
    	else{
    		//emmanual to fix FE170CG Bug -	375665
			mDoodleView.getCacheResultForThumbnail(canvas, width, height, false);
//            mDoodleView.getResult(canvas, width, height, false, true,mNoteEditText.getEditorPageHeight());            
            mNoteEditText.getResult(canvas, width, height, false,mTemplateLayoutHeight);           
            drawTemplateView(canvas, width, (int) ((float)mTemplateLayoutHeight/mNoteEditText.getEditorPageHeight()*height));
    	}
    	mIsScrollDoodle = true;
    }
    
    
    private Bitmap drawWidgetThumbnail(boolean drawFullPage){
    	Bitmap  result = null;
        Resources res = mEditorUiUtility.getContext().getResources();
        Bitmap content;
        Canvas resultCanvas, contentCanvas;
        Paint paint = new Paint();
        int targetWidth, targetHeight;

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);

        try {
            result = Bitmap.createBitmap((int)res.getDimension(R.dimen.widget_cover_width),(int)res.getDimension(R.dimen.widget_cover_height), Bitmap.Config.ARGB_8888);//cover.getWidth(), cover.getHeight()
            resultCanvas = new Canvas(result);
            targetWidth = (int) result.getWidth() ;//(* 0.9);
            targetHeight = (int) result.getHeight() ;//(* 0.85);
            content = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
            content.setDensity(Bitmap.DENSITY_NONE);
            contentCanvas = new Canvas(content);
            getResult(contentCanvas,targetWidth,targetHeight,drawFullPage,false);
            resultCanvas.drawBitmap(content, 0, 0, paint);

            content.recycle();
            content = null;
        }
        catch (OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] Loading cover failed !!!");
        }
    return result;
    }
    
    public Bitmap drawThumbnail(Bitmap cover,boolean needBackground,boolean drawFullPage){
    	Bitmap  result = null;
        Resources res = mEditorUiUtility.getContext().getResources();
        Bitmap content;
        Canvas resultCanvas, contentCanvas;
        Paint paint = new Paint();
        int targetWidth, targetHeight;

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);

        try {
            result = Bitmap.createBitmap(cover.getWidth(), cover.getHeight(), Bitmap.Config.ARGB_8888);//
            result.setDensity(Bitmap.DENSITY_NONE); //add by mars fixed bug when dds thmbnail error
            resultCanvas = new Canvas(result);
            if(needBackground){
            	resultCanvas.drawBitmap(cover, 0, 0, paint);
            }

            targetWidth = (int) (result.getWidth() * 0.9);
            targetHeight = (int) (result.getHeight()* 0.85);
            content = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
            content.setDensity(Bitmap.DENSITY_NONE);
            contentCanvas = new Canvas(content);
            getResult(contentCanvas,targetWidth,targetHeight,drawFullPage,false);
            float left = res.getDimension(R.dimen.thumb_padding_left);
            float top = res.getDimension(R.dimen.thumb_padding_top);
            resultCanvas.translate(left, top);
            resultCanvas.drawBitmap(content, 0, 0, paint);
            
            resultCanvas = null; //add by mars set null before recycle
            content.recycle();
            content = null;
        }
        catch (OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] Loading cover failed !!!");
        }
    return result;
    }
    
    public Bitmap getMicroView(){
    	return drawThumbnail(CoverHelper.DefaultCover,true,true);      
    }
    
    public Bitmap getThumbnail(int color, int line,boolean needBackground) {
        Bitmap result = null,cover = null;
//        if (mDoodleView.isModified() || isEditTextModified()) {
            Resources res = mEditorUiUtility.getContext().getResources();
            cover = CoverHelper.getDefaultCoverBitmap(color, line, res);
            result = drawThumbnail(cover,needBackground,false);  
            cover.recycle();
            cover = null;
//        }
        return result;
    }
    
    public Bitmap getAirViewThumbnail(int color, int line) {
        Resources res = mEditorUiUtility.getContext().getResources();
    	int width = (int)res.getDimension(R.dimen.pageview_airview_width);
		int height = (int)res.getDimension(R.dimen.pageview_airview_height);
		
        Bitmap airviewCover = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas resultCanvas = new Canvas(airviewCover);
        try {
            getResult(resultCanvas,width,height,true,false);
        }
        catch (OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] Loading cover failed !!!");
        }
        return airviewCover;
    }
    //End Allen
    
    
    public Bitmap getWidgetThumbnail(int color, int line,boolean needBackground) {
        Bitmap result = null;
        if (mDoodleView.isModified() || isEditTextModified()) {
            Resources res = mEditorUiUtility.getContext().getResources();
            result = drawWidgetThumbnail(false);  
        }
        return result;
    }
    
    // BEGIN: archie_huang@asus.com
    public DoodleItem getDoodleItem(NotePage notePage) {
            return mDoodleView.save(notePage);
    } // END: archie_huang@asus.com

    public float getNoteEditTextFontSize() {
        return mNoteEditText.getFontSize();
    }

    // BEGIN: archie_huang@asus.com
    public int getImageSpanHeight() {
        FontMetricsInt fontMetricsInt;
        Paint paint = new Paint();
        paint.setTextSize(mCurrentEditText.getFontSize());
        fontMetricsInt = paint.getFontMetricsInt();
        return (int) (fontMetricsInt.descent * FONT_DESCENT_RATIO - fontMetricsInt.ascent);
    } // END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public int getFullImageSpanHeight() {
        FontMetricsInt fontMetricsInt;
        Paint paint = new Paint();
        paint.setTextSize(mCurrentEditText.getFontSize());
        fontMetricsInt = paint.getFontMetricsInt();
        if (mCurrentEditText == mBoxEditText) {
            return getNoteEditTextLineHeight();
        }
        else {
        return (int) (getNoteEditTextLineHeight() + fontMetricsInt.descent * FONT_DESCENT_RATIO);
        }
    } // END: archie_huang@asus.com

    //Begin Allen
    public ArrayList<NoteItemArray> getNoteItem() {
        ArrayList<NoteItemArray> allNoteItems = new ArrayList<NoteItemArray>();
        allNoteItems.add(mNoteEditText.getNoteItem(0, mNoteEditText.getText().length()));
		mTemplateUtility.getTemplateNoteItems(allNoteItems);
        return allNoteItems;
    }
   //End Allen
    
    //begin noah
    private NoteItem[] getNoteItemToCopy(EditText editText, int start, int end) {
        if (editText == null || start < 0 || end > editText.getText().length()) {
            Log.e(TAG, "error selected when copy, start = " + start + " end = " + end + " CurrentText = " + editText.getText());
            return null;
        }
        Editable editable = null;
        if (editText != null && editText == mBoxEditText) {
            editable = (Editable) editText.getText().subSequence(start, end);
        }
        else {
        	editable = (Editable) editText.getText().subSequence(start, end);//Allen for todo template
        }

        if (editable == null) {
            return null;
        }

        // text part
        NoteItem stringItem = new NoteStringItem(editable.toString());

        // span part
        NoteItem[] spanItems = editable.getSpans(0, editable.length(), NoteItem.class);
        NoteItem[] allnoteItem = new NoteItem[spanItems.length + 1];

        allnoteItem[0] = stringItem;
        for (int i = 0; i < spanItems.length; i++) {
            int spanstart = editable.getSpanStart(spanItems[i]);
            int spanend = editable.getSpanEnd(spanItems[i]);

            spanItems[i].setStart(spanstart);
            spanItems[i].setEnd(spanend);
            allnoteItem[i + 1] = spanItems[i];
        }

        return allnoteItem;
    }

    

    private NoteItem[] getNoteItemToCopy(int start, int end) {
    	return getNoteItemToCopy(mCurrentEditText, start, end);
    }
    
  //end noah

    public Paint getEditorPaint() {
        return mTextScribblePaint;
    }

    public Paint getDoodlePaint() {
        return mDoodlePaint;
    }

    public Paint getPaint() {

        if (!checkHaveInputManager()) {
            return mTextScribblePaint;
        }

        int inputMode = mInputManager.getInputMode();
        if (inputMode == InputManager.INPUT_METHOD_DOODLE
                || inputMode == InputManager.INPUT_METHOD_INSERT
                || inputMode == InputManager.INPUT_METHOD_SELECTION_DOODLE
                || inputMode == InputManager.INPUT_METHOD_COLOR_PICKER) { //smilefish
            return mDoodlePaint;
        }
        return mTextScribblePaint;
    }

    // BEGIN: archie_huang@asus.com
    public void setPaintTool(DrawTool paintTool) {
        mDoodleView.setPaintTool(paintTool);
    } // END: archie_huang@asus.com
    
    // BEGIN: Better
    public int getPaintTool() {
    	return mDoodleView.getPaintTool();
    }
    // END: Better

    boolean changeFromBrowser = true;
    public void onColorChange() {
    	//BEGIN:emmanual
		Intent shareIntent = ((EditorActivity) (mEditorUiUtility.getContext())).getShareIntent();
		if (changeFromBrowser && shareIntent != null && ShareUtils.isFromBrower(shareIntent)) {
			changeFromBrowser = false;
			return;
		}
		//END:emmanual
        if (mCurrentEditText == null) {
            return;
        }
        int selectionStart = Math.min(mCurrentEditText.getSelectionStart(), mCurrentEditText.getSelectionEnd());
        int selectionEnd = Math.max(mCurrentEditText.getSelectionStart(), mCurrentEditText.getSelectionEnd());

        if (mEditorColorChangeListener != null) {
            mEditorColorChangeListener.onColorChange(mTextScribblePaint.getColor());
        }

        if (selectionStart == selectionEnd) return;

        Editable editable = mCurrentEditText.getEditableText();
        Editable orieditable = new SpannableStringBuilder(editable.subSequence(selectionStart, selectionEnd));

        NoteHandWriteItem[] handwriteitems = editable.getSpans(selectionStart, selectionEnd, NoteHandWriteItem.class);
        // set color for hand write item (NoteHandWriteItem)
        for (NoteHandWriteItem item : handwriteitems) {
            int start = editable.getSpanStart(item);
            int end = editable.getSpanEnd(item);

            NoteHandWriteItem newitem = null;
            if (item instanceof NoteHandWriteBaselineItem) {
                newitem = new NoteHandWriteBaselineItem(item);
            }
            else {
                newitem = new NoteHandWriteItem(item);
            }

            newitem.setColor(mTextScribblePaint.getColor());

            editable.removeSpan(item);
            editable.setSpan(newitem, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        //change color of text:
        NoteItem[] noteItems = editable.getSpans(0, editable.length(), NoteItem.class);
        NoteForegroundColorItem elemItem = null;
        int start;
        int end;
        for (NoteItem item : noteItems) {
        	//if item is not color-style span, skip & continue.
        	if(!(item instanceof NoteForegroundColorItem)) {
        		continue;
        	}
        	
        	if(editable.getSpanStart(item) < selectionStart
            		&& editable.getSpanEnd(item) > selectionStart
            		) {
        		elemItem = (NoteForegroundColorItem)item;
        		start = editable.getSpanStart(item);
        		end = editable.getSpanEnd(item);
        		
        		if(editable.getSpanEnd(item) <= selectionEnd) {	
        			elemItem = new NoteForegroundColorItem(elemItem.getForegroundColor());
        			editable.setSpan(elemItem, start, selectionStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        			editable.removeSpan(item);
        		}
        		
        		if(editable.getSpanEnd(item) > selectionEnd) {	
        			elemItem = new NoteForegroundColorItem(elemItem.getForegroundColor());
        			editable.setSpan(elemItem, start, selectionStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        			
        			elemItem = new NoteForegroundColorItem(elemItem.getForegroundColor());
        			editable.setSpan(elemItem, selectionEnd, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        			editable.removeSpan(item);
        		}
        	}
        	
        	if(editable.getSpanEnd(item) > selectionEnd
            		&& editable.getSpanStart(item) < selectionEnd
            		) {
        		elemItem = (NoteForegroundColorItem)item;
        		start = editable.getSpanStart(item);
        		end = editable.getSpanEnd(item);
        		
        		if(editable.getSpanStart(item) >= selectionStart) {	
        			elemItem = new NoteForegroundColorItem(elemItem.getForegroundColor());
        			editable.setSpan(elemItem, selectionEnd, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        			editable.removeSpan(item);
        			
        		}	
        		//done before, here skip
        	}	

        	//span in the selection. We should remove it. 
            if(editable.getSpanStart(item) >= selectionStart
            		&& editable.getSpanEnd(item) <= selectionEnd
            		) {
            	editable.removeSpan(item);
            }     
        }
        
        //set new span (selected text)
        elemItem = new NoteForegroundColorItem(mTextScribblePaint.getColor());
		editable.setSpan(elemItem, selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// END: Shane_Wang 2012-11-5
		
        Editable chaneditable = new SpannableStringBuilder(editable.subSequence(selectionStart, selectionEnd));

        mCurrentEditText.insertToHistoryStack(orieditable, chaneditable, selectionStart);
        mCurrentEditText.setIsModified(true);//Allen
    }

    public void onStyleAndStrokeChange() {
        if (mCurrentEditText == null) {
            return;
        }
        int selectionStart = Math.min(mCurrentEditText.getSelectionStart(), mCurrentEditText.getSelectionEnd());
        int selectionEnd = Math.max(mCurrentEditText.getSelectionStart(), mCurrentEditText.getSelectionEnd());

        if (mEditorBoldChangeListener != null) {
            mEditorBoldChangeListener.onBoldChange(mTextStyle);
        }

        if (selectionStart == selectionEnd) return;

        Editable editable = mCurrentEditText.getEditableText();
        Editable orieditable = new SpannableStringBuilder(editable.subSequence(selectionStart, selectionEnd));

        NoteHandWriteItem[] handwriteitems = editable.getSpans(selectionStart, selectionEnd, NoteHandWriteItem.class);
        // set Stroke for hand write item (NoteHandWriteItem)
        for (NoteHandWriteItem item : handwriteitems) {
            int start = editable.getSpanStart(item);
            int end = editable.getSpanEnd(item);

            NoteHandWriteItem newitem = null;
            if (item instanceof NoteHandWriteBaselineItem) {
                newitem = new NoteHandWriteBaselineItem(item);
            }
            else {
                newitem = new NoteHandWriteItem(item);
            }
            newitem.setStrokeWidth(mTextScribblePaint.getStrokeWidth());

            editable.removeSpan(item);
            editable.setSpan(newitem, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // BEGIN: Shane_Wang 2012-11-5     
      //change style of spans
		NoteItem[] noteItems = editable.getSpans(0, editable.length(), NoteItem.class);
        NoteTextStyleItem elemItem = null;
        int start;
        int end;
        for (NoteItem item : noteItems) {
        	//if item is not text-style span, skip & continue.
        	if(!(item instanceof NoteTextStyleItem)) {
        		continue;
        	}
        	
        	if(editable.getSpanStart(item) < selectionStart
            		&& editable.getSpanEnd(item) > selectionStart
            		) {
        		elemItem = (NoteTextStyleItem)item;
        		start = editable.getSpanStart(item);
        		end = editable.getSpanEnd(item);
        		
        		if(editable.getSpanEnd(item) <= selectionEnd) {	
        			elemItem = new NoteTextStyleItem(elemItem.getStyle());
        			editable.setSpan(elemItem, start, selectionStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        			editable.removeSpan(item);
        		}
        		
        		if(editable.getSpanEnd(item) > selectionEnd) {	
        			elemItem = new NoteTextStyleItem(elemItem.getStyle());
        			editable.setSpan(elemItem, start, selectionStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        			
        			elemItem = new NoteTextStyleItem(elemItem.getStyle());
        			editable.setSpan(elemItem, selectionEnd, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        			editable.removeSpan(item);
        		}
        	}
        	
        	if(editable.getSpanEnd(item) > selectionEnd
            		&& editable.getSpanStart(item) < selectionEnd
            		) {
        		elemItem = (NoteTextStyleItem)item;
        		start = editable.getSpanStart(item);
        		end = editable.getSpanEnd(item);
        		
        		if(editable.getSpanStart(item) >= selectionStart) {	
        			elemItem = new NoteTextStyleItem(elemItem.getStyle());
        			editable.setSpan(elemItem, selectionEnd, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        			editable.removeSpan(item);
        		}
        		
        		//done before
        	}	
        	
        	//span in the selection. We should remove it.
            if(editable.getSpanStart(item) >= selectionStart
            		&& editable.getSpanEnd(item) <= selectionEnd
            		) {
            	editable.removeSpan(item);
            }     
        }
        
        elemItem = new NoteTextStyleItem(mTextStyle);
		editable.setSpan(elemItem, selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// END: Shane_Wang 2012-11-5
		
        Editable chaneditable = new SpannableStringBuilder(editable.subSequence(selectionStart, selectionEnd));

        mCurrentEditText.insertToHistoryStack(orieditable, chaneditable, selectionStart);
        mCurrentEditText.setIsModified(true);//Allen
    }

    public void clearDoodleView() {
        mDoodleView.clearView();
    }

    public void setNoteEditTextInvisiable(boolean visiable){
    	mNoteEditText.setFocusable(false);
    	mNoteEditText.setEnabled(false);
    	mNoteEditText.setVisibility(visiable?View.VISIBLE:View.INVISIBLE);
    }
    
    public void setNoteEditTextEnable(boolean enable){
        mNoteEditText.setCursorVisible(enable);
        mNoteEditText.setActivated(enable);
        mNoteEditText.setNoteEditTextEnabled(enable);
        mTemplateLinearLayout.setEnable(enable); 
    	mTemplateUtility.setEditTextEnable(enable);
    	isNoteEditEnable = enable;
    }
    
    // BEGIN: archie_huang@asus.com
    public void enableDoodleView(boolean enable) {
        mDoodleView.enable(enable);
    } // END: archie_huang@asus.com

    public void unbindResources() {
    	enableDoodleView(false); //smilefish
    	
    	if(mBoxEditText != null)
    		mBoxEditText.recycleBitmaps(); //Dave.
    	
        mNoteEditText.recycleBitmaps();
        mDoodleView.recycleBitmaps();
        // BEGIN: archie_huang@asus.com
        BitmapLender.getInstance().recycleBitmaps();
        // END: archie_huang@asus.com
    }

    private Queue<Integer> mSearchResultIndexs = null;

    public void searchForCurrentText(String searchstr) {

        Spannable editString = mNoteEditText.getText();
        BackgroundColorSpan[] bcss = editString.getSpans(0, editString.length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan bcs : bcss) {
            editString.removeSpan(bcs);
        }

        if (searchstr == null || searchstr.length() == 0) return;

        mSearchResultIndexs = new LinkedList<Integer>();

        int index = editString.toString().indexOf(searchstr);
        while (index != -1) {
            mSearchResultIndexs.offer(Integer.valueOf(index));
            BackgroundColorSpan bcs = new BackgroundColorSpan(Color.CYAN);
            editString.setSpan(bcs, index, index + searchstr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            index = editString.toString().indexOf(searchstr, index + 1);
        }
    }

    public void passTouchEventToNoteEditText(MotionEvent me) {
        mCurrentEditText.dispatchTouchEvent(me);
    }

    public void checkSystemClipBoard() {
    	boolean isClipboardAvalable = false;
    	
    	// BEGIN: Better
    	if (MetaData.HasMultiClipboardSupport) {
	    	if (mPreferences != null) {
	    		int type = mPreferences.getInt(MetaData.PREFERENCE_EDITOR_COPY_CONTENT_TYPE, MetaData.CLIPBOARD_TYPE_NONE);
	    		if (type != MetaData.CLIPBOARD_TYPE_NONE) {
	    			isClipboardAvalable = true;
	    		}
	    	}
    	} else {
	    	ClipboardManager clipboard = (ClipboardManager) mEditorUiUtility.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
	    	CharSequence cs = null;
	    	if (clipboard.hasPrimaryClip()) {
	    		ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
	    		cs = item.getText();
	    	}
	    	isClipboardAvalable = (cs != null);
    	}

        if (mClipBoardAvailableListener != null) {
            mClipBoardAvailableListener.onAvailableChange(isClipboardAvalable);
        }
        // END: Better
    }

    public void copyTheSelectionText(NotePage page) {
    	// BEGIN: Better
    	if ((page == null) || (mPreferences == null)) {
    		return;
    	}
    	// END: Better
    
        int start = Math.min(mCurrentEditText.getSelectionStart(), mCurrentEditText.getSelectionEnd());
        int end = Math.max(mCurrentEditText.getSelectionStart(), mCurrentEditText.getSelectionEnd());

        // copy to SuperNote's Database
        ContentResolver cr = mEditorUiUtility.getContext().getContentResolver();
        ContentProviderClient cpc = cr.acquireContentProviderClient(MetaData.ClipboardTable.uri);
        cr.delete(MetaData.ClipboardTable.uri, null, null);
        NoteItem[] items = getNoteItemToCopy(start, end);
        if ((items == null) || (items.length <= 0)) {
            return;
        }
        ContentValues[] cvs = new ContentValues[items.length];
        int index = 0;

        // BEGIN: Better
        SharedPreferences.Editor prefEditor = mPreferences.edit();
        prefEditor.putInt(MetaData.PREFERENCE_EDITOR_COPY_CONTENT_TYPE, MetaData.CLIPBOARD_TYPE_NOTE);
        if (page != null && mNotePage != null) { //smilefish fix google play NullPointerException
        	copyFilePath = mNotePage.getFilePath();
        	prefEditor.putString(MetaData.PREFERENCE_EDITOR_COPY_PAGE_PATH, page.getFilePath());
        }
        prefEditor.commit();
        // END: Better

        File dir = new File(MetaData.CROP_TEMP_DIR);
        if (dir.exists() == false) {
            dir.mkdir();
        }else{
        	String[] files = dir.list();
			for (String name : files) {
				File invalidFile = new File(dir, name);
				boolean hasSame = false;
		        for (NoteItem item : items) {
		            if(item instanceof NoteSendIntentItem){
	                	String fileName = ((NoteSendIntentItem)item).getFileName();
	                	if(fileName.equals(name)){
	                		hasSame = true;
	                		break;
	                	}
		            }
		        }
		        if(!hasSame){
		        	invalidFile.delete();
		        }
			}
        }
        for (NoteItem item : items) {
            ContentValues cv = new ContentValues();
            Serializable s = item.save();
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            try {
                ObjectOutputStream obj = new ObjectOutputStream(b);
                obj.writeObject(s);
                cv.put(MetaData.ClipboardTable.DATA, b.toByteArray());
                obj.close();
                b.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            cvs[index++] = cv;
            if(item instanceof NoteSendIntentItem){
    	        if (dir.exists() == false) {
    	            dir.mkdir();
    	        }else{    	        	
    		        String srcPath = mPreferences.getString(MetaData.PREFERENCE_EDITOR_COPY_PAGE_PATH, "");
                	String fileName = ((NoteSendIntentItem)item).getFileName();
                	File srcFile = new File(srcPath, fileName);
                	File dstFile = new File(dir, fileName);
                	if (srcFile.exists() && !dstFile.exists()) {
                		try {
                            FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
                            FileChannel destChannel = new FileOutputStream(dstFile).getChannel();
                            destChannel.transferFrom(srcChannel, 0, srcChannel.size());
                            srcChannel.close();
                            destChannel.close();
                        } catch (FileNotFoundException e) {

                        } catch (IOException e) {
                        	
                        }
                	}
    	        }
            }
        }
        try {
        	copyCvs = cvs;
            cpc.bulkInsert(MetaData.ClipboardTable.uri, cvs);
            cpc.release();
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        
        if (mClipBoardAvailableListener != null) {
            mClipBoardAvailableListener.onAvailableChange(true);
        }
        
        // BEGIN: Better
        ClipboardManager clipboard = (ClipboardManager) mEditorUiUtility.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        //begin darwin
        String str = mCurrentEditText.getText().subSequence(start, end).toString();
        copyString = str;
        str = str.replace(OBJ, "");
        if((str.trim().length() > 0) || (!MetaData.HasMultiClipboardSupport))
        {
	        CharSequence copydata = new SpannableString(str);
	        //end  darwin
	        ClipData clip = ClipData.newPlainText(MetaData.CLIPBOARD_NOTE_DESC, copydata);
	        clipboard.setPrimaryClip(clip);
        }
        // END: Better
        
        //Begin:Dave. To add text selector.
        editSelectionHandleLeft.setVisibility(View.INVISIBLE);
        editSelectionHandleRight.setVisibility(View.INVISIBLE);
        //End:Dave.
    }

    /**
     * emmanual for auto page
     * @param start
     * @param end
     */
    public void copyTheSelectionText(int start, int end) {
    	// BEGIN: Better
    	if ((mNotePage == null) || (mPreferences == null)) {
    		return;
    	}
    	// END: Better

        // copy to SuperNote's Database
        ContentResolver cr = mEditorUiUtility.getContext().getContentResolver();
        ContentProviderClient cpc = cr.acquireContentProviderClient(MetaData.ClipboardTable.uri);
        cr.delete(MetaData.ClipboardTable.uri, null, null);
        NoteItem[] items = getNoteItemToCopy(start, end);
        if ((items == null) || (items.length <= 0)) {
            return;
        }
        ContentValues[] cvs = new ContentValues[items.length];
        int index = 0;

        // BEGIN: Better
        SharedPreferences.Editor prefEditor = mPreferences.edit();
        prefEditor.putInt(MetaData.PREFERENCE_EDITOR_COPY_CONTENT_TYPE, MetaData.CLIPBOARD_TYPE_NOTE);
        if (mNotePage != null) {
        	prefEditor.putString(MetaData.PREFERENCE_EDITOR_COPY_PAGE_PATH, mNotePage.getFilePath());
        }
        prefEditor.commit();
        // END: Better

        File dir = new File(MetaData.CROP_TEMP_DIR);
        if (dir.exists() == false) {
            dir.mkdir();
        }else{
        	String[] files = dir.list();
			for (String name : files) {
				File invalidFile = new File(dir, name);
				boolean hasSame = false;
		        for (NoteItem item : items) {
		            if(item instanceof NoteSendIntentItem){
	                	String fileName = ((NoteSendIntentItem)item).getFileName();
	                	if(fileName.equals(name)){
	                		hasSame = true;
	                		break;
	                	}
		            }
		        }
		        if(!hasSame){
		        	invalidFile.delete();
		        }
			}
        }
        for (NoteItem item : items) {
            ContentValues cv = new ContentValues();
            Serializable s = item.save();
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            try {
                ObjectOutputStream obj = new ObjectOutputStream(b);
                obj.writeObject(s);
                cv.put(MetaData.ClipboardTable.DATA, b.toByteArray());
                obj.close();
                b.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            cvs[index++] = cv;
            if(item instanceof NoteSendIntentItem){
    	        if (dir.exists() == false) {
    	            dir.mkdir();
    	        }else{    	        	
    		        String srcPath = mPreferences.getString(MetaData.PREFERENCE_EDITOR_COPY_PAGE_PATH, "");
                	String fileName = ((NoteSendIntentItem)item).getFileName();
                	File srcFile = new File(srcPath, fileName);
                	File dstFile = new File(dir, fileName);
                	if (srcFile.exists() && !dstFile.exists()) {
                		try {
                            FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
                            FileChannel destChannel = new FileOutputStream(dstFile).getChannel();
                            destChannel.transferFrom(srcChannel, 0, srcChannel.size());
                            srcChannel.close();
                            destChannel.close();
                        } catch (FileNotFoundException e) {

                        } catch (IOException e) {
                        	
                        }
                	}
    	        }
            }
        }
        try {
            cpc.bulkInsert(MetaData.ClipboardTable.uri, cvs);
            cpc.release();
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        
        if (mClipBoardAvailableListener != null) {
            mClipBoardAvailableListener.onAvailableChange(true);
        }
        
        // BEGIN: Better
        ClipboardManager clipboard = (ClipboardManager) mEditorUiUtility.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        //begin darwin
        String str = mCurrentEditText.getText().subSequence(start, end).toString();
        str = str.replace(OBJ, "");
        if((str.trim().length() > 0) || (!MetaData.HasMultiClipboardSupport))
        {
	        CharSequence copydata = new SpannableString(str);
	        //end  darwin
	        ClipData clip = ClipData.newPlainText(MetaData.CLIPBOARD_NOTE_DESC, copydata);
	        clipboard.setPrimaryClip(clip);
        }
        // END: Better
        
        //Begin:Dave. To add text selector.
        editSelectionHandleLeft.setVisibility(View.INVISIBLE);
        editSelectionHandleRight.setVisibility(View.INVISIBLE);
        //End:Dave.
    }

    String copyFilePath = "", copyString = "";
    ContentValues[] copyCvs;
    public void recopyTheSelectionText() {
    	// BEGIN: Better
    	if (mPreferences == null) {
    		return;
    	}
    	// END: Better

        // copy to SuperNote's Database
        ContentResolver cr = mEditorUiUtility.getContext().getContentResolver();
        ContentProviderClient cpc = cr.acquireContentProviderClient(MetaData.ClipboardTable.uri);
        cr.delete(MetaData.ClipboardTable.uri, null, null);
        
        // BEGIN: Better
        SharedPreferences.Editor prefEditor = mPreferences.edit();
        prefEditor.putInt(MetaData.PREFERENCE_EDITOR_COPY_CONTENT_TYPE, MetaData.CLIPBOARD_TYPE_NOTE);
        prefEditor.putString(MetaData.PREFERENCE_EDITOR_COPY_PAGE_PATH, copyFilePath);
        prefEditor.commit();
        // END: Better

        try {
        	if(copyCvs != null){ //smilefish fix bug 698016
        		cpc.bulkInsert(MetaData.ClipboardTable.uri, copyCvs);
        	}
            cpc.release();
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        
        if (mClipBoardAvailableListener != null) {
            mClipBoardAvailableListener.onAvailableChange(true);
        }
        
        // BEGIN: Better
        ClipboardManager clipboard = (ClipboardManager) mEditorUiUtility.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        //begin darwin
        copyString = copyString.replace(OBJ, "");
        if((copyString.trim().length() > 0) || (!MetaData.HasMultiClipboardSupport))
        {
	        CharSequence copydata = new SpannableString(copyString);
	        //end  darwin
	        ClipData clip = ClipData.newPlainText(MetaData.CLIPBOARD_NOTE_DESC, copydata);
	        clipboard.setPrimaryClip(clip);
        }
        // END: Better
    }

	//emmanual to copy from QuickMemo to SuperNote
    public boolean pasteFromQuickMemo(){
		CharSequence cs = null;
		ClipboardManager clipboard = (ClipboardManager) mEditorUiUtility
				.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
		if (clipboard.hasPrimaryClip()) {
			ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
			cs = item.getText();
			if (cs != null) {
				ClipDescription cd = clipboard.getPrimaryClipDescription();
				if (cd != null) {
					CharSequence label = cd.getLabel();
					if (label != null) {
						String str = label.toString();
						if (str.equals(MetaData.CLIPBOARD_MEMO_DESC)) {
							return true;
						}
					}
				}
			}
		}
		return false;
    }	

    private NoteItemArray mNoteItemArray = null;
	//emmanual to copy from QuickMemo to SuperNote
    public void pasteQuickMemo() {
		ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
			//copy from QuickMemo
		File file = new File(MetaData.COPY_TEMP_DIR, MetaData.COPY_FILENAME);
		if (file == null || !file.exists()) {
			return;
		}
		getNoteItemArray(file);
		ArrayList<NoteItem> items = mNoteItemArray.getNoteItems();
		if (items.size() > 0) {
			String str = items.get(0).getText();
			Editable editable = new SpannableStringBuilder(str);
			for (int i = 1; i < items.size(); i++) {
				editable.setSpan(items.get(i), items.get(i).getStart(), items
				        .get(i).getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				if (items.get(i) instanceof NoteHandWriteBaselineItem) {
					// set foreground color for each handwriting item
					int itemColor = mEditorUiUtility.getPaintColor();
					int x = items.get(i).getStart();
					int y = items.get(i).getEnd();
					NoteHandWriteBaselineItem baseline = new NoteHandWriteBaselineItem(
					        (NoteHandWriteBaselineItem) items.get(i), true);
					baseline.setColor(itemColor);
					editable.removeSpan(items.get(i));
					editable.setSpan(baseline, x, y,
					        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			setFontSize(editable);
			addItemToEditText(editable);
		}
		quitSelectionTextMode();
    }

	//emmanual to copy from QuickMemo to SuperNote
    public void getNoteItemArray(File file){
    	FileInputStream fis = null;
		BufferedInputStream bis = null;
		ArrayList<NoteItem> noteItemList = null;
		boolean isNoteItemCorrect = false;
    	try {
    		fis = new FileInputStream(file);
    		bis = new BufferedInputStream(fis);
			AsusFormatReader afr = new AsusFormatReader(bis, MetaData.MAX_ARRAY_SIZE);
			AsusFormatReader.Item item = afr.readItem();
	
			while (item != null) {
				switch (item.getId()) {
				case AsusFormat.SNF_NITEM_BEGIN:
					isNoteItemCorrect = true;
					noteItemList = new ArrayList<NoteItem>();
					mNoteItemArray = new NoteItemArray(
					        noteItemList,
					        NoteItemArray.TEMPLATE_CONTENT_DEFAULT_NOTE_EDITTEXT);
					break;
				case AsusFormat.SNF_NITEM_VERSION:
					if (!isNoteItemCorrect) {
						return;
					}
					break;
				case AsusFormat.SNF_NITEM_TEMPLATE_ITEM_TYPE:
					short templateItemType = item.getShortValue();
					if (mNoteItemArray != null) {
						mNoteItemArray.setTemplateItemType(templateItemType);
					}
					break;
				case AsusFormat.SNF_NITEM_STRING_BEGIN:
					if (!isNoteItemCorrect) {
						return;
					}
					NoteStringItem stringItem = new NoteStringItem();
					stringItem.itemLoad(afr);
					if (noteItemList != null) {
						noteItemList.add(0, stringItem);
					} else {
						return;
					}
					break;
				case AsusFormat.SNF_NITEM_HANDWRITE_BEGIN:
					if (!isNoteItemCorrect) {
						return;
					}
					NoteHandWriteItem hwItem = new NoteHandWriteItem();
					hwItem.itemLoad(afr);
					if (noteItemList != null) {
						noteItemList.add(hwItem);
					} else {
						return;
					}
					break;
				case AsusFormat.SNF_NITEM_HANDWRITEBL_BEGIN:
					if (!isNoteItemCorrect) {
						return;
					}
					NoteHandWriteBaselineItem hwblItem = new NoteHandWriteBaselineItem();
					hwblItem.itemLoad(afr);
					if (noteItemList != null) {
						noteItemList.add(hwblItem);
					} else {
						return;
					}
					break;
				case AsusFormat.SNF_NITEM_TEXTSTYLE_BEGIN:
					if (!isNoteItemCorrect) {
						return;
					}
					NoteTextStyleItem styleItem = new NoteTextStyleItem();
					styleItem.itemLoad(afr);
					if (noteItemList != null) {
						noteItemList.add(styleItem);
					} else {
						return;
					}
					break;
				case AsusFormat.SNF_NITEM_FCOLOR_BEGIN:
					if (!isNoteItemCorrect) {
						return;
					}
					NoteForegroundColorItem fColorItem = new NoteForegroundColorItem();
					fColorItem.itemLoad(afr);
					if (noteItemList != null) {
						noteItemList.add(fColorItem);
					} else {
						return;
					}
					break;
				case AsusFormat.SNF_NITEM_SENDINTENT_BEGIN:
					if (!isNoteItemCorrect) {
						return;
					}
					break;
				case AsusFormat.SNF_NITEM_TIMESTAMP_BEGIN:
					break;
				case AsusFormat.SNF_NITEM_END:
					isNoteItemCorrect = false;
					break;
				default:
					break;
				}
				item = afr.readItem();
			}
			bis.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
    }


    public void pastToNoteEditText(NotePage page) {
    	// BEGIN: Better
        if ((page == null) || (mPreferences == null)) {
        	return;
        }
        
		boolean isCopyFromClipboard = true;
		CharSequence cs = null;
		
		if (!MetaData.HasMultiClipboardSupport) {
			ClipboardManager clipboard = (ClipboardManager) mEditorUiUtility
					.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
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
							if (str.equals(MetaData.CLIPBOARD_NOTE_DESC)) {
								isCopyFromClipboard = false;
							}
						}
					}
				}
			}
		}
		ContentResolver cr = mEditorUiUtility.getContext().getContentResolver();
        
		if (MetaData.HasMultiClipboardSupport || !isCopyFromClipboard) {
	        boolean isSamePage = false;
	        String dstPath = page.getFilePath();
	        String srcPath = mPreferences.getString(MetaData.PREFERENCE_EDITOR_COPY_PAGE_PATH, "");
	        if (srcPath.equalsIgnoreCase(dstPath)) {
	        	isSamePage = true;
	        }
	        // END: Better
	
	        // Load form SuperNote's Database
	        List<NoteItem> temp = new ArrayList<NoteItem>();
	        Cursor cursor = cr.query(MetaData.ClipboardTable.uri, null, null, null, null);
	        cursor.moveToFirst();
	        while (!cursor.isAfterLast()) {
	            byte[] data = cursor.getBlob(MetaData.ClipboardTable.INDEX_DATA);
	            ByteArrayInputStream b = new ByteArrayInputStream(data);
	            try {
	                ObjectInputStream obj = new ObjectInputStream(b);
	                Object object = obj.readObject();
	                Class<?> privateClass = Class.forName(((NoteItem.NoteItemSaveData) object).getOuterClassName());
	                NoteItem noteitem = (NoteItem) privateClass.newInstance();
	                noteitem.load((Serializable) object, this);
	                if ((noteitem instanceof NoteSendIntentItem) && !isSamePage) { // Better
	                	String fileName = ((NoteSendIntentItem)noteitem).getFileName();
	                	File srcFile = new File(srcPath, fileName);
	                	if(!srcFile.exists()){
	                		srcFile = new File(MetaData.CROP_TEMP_DIR, fileName);
	                	}
	                	if (srcFile.exists()) {
	                		boolean isAdd = true;
	                		File dstFile = new File(dstPath, fileName);
	                		try {
	                            FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
	                            FileChannel destChannel = new FileOutputStream(dstFile).getChannel();
	                            destChannel.transferFrom(srcChannel, 0, srcChannel.size());
	                            srcChannel.close();
	                            destChannel.close();
	                        } catch (FileNotFoundException e) {
	                        	isAdd = false;
	                        } catch (IOException e) {
	                        	isAdd = false;
	                        }
	                        if (isAdd) {
	                        	temp.add(noteitem);
	                        }
	                	}else{
			                temp.add(noteitem);
	                	}
	                } else {
		                temp.add(noteitem);
	                }
	                obj.close();
	            }
	            catch (Exception e) {
	                e.printStackTrace();
	            }
	            cursor.moveToNext();
	        }
	        cursor.close();
	        NoteItem[] noteitems = temp.toArray(new NoteItem[0]);
	
	        // if both of they have the same string, then use the data in Database (have span data)
	        //
	        if (noteitems.length > 0) {
	
	            String str = noteitems[0].getText();
	            Editable editable = new SpannableStringBuilder(str);
	
	            for (int i = 1; i < noteitems.length; i++) {
	                editable.setSpan(noteitems[i], noteitems[i].getStart(), noteitems[i].getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	            }
	
	            // BEGIN: archie_huang@asus.com
	            setFontSize(editable);
	            // END: archie_huang@asus.com
	
	            addItemToEditText(editable);
	        } else if (cs != null) {
	        	addItemToEditText(cs);
	            cr.delete(MetaData.ClipboardTable.uri, null, null);
	        } else {
	            EditorActivity.showToast(mEditorUiUtility.getContext(), R.string.warning_no_data_has_been_copy);
	        }
		} else {
			if (cs != null) {
	        	addItemToEditText(cs);
	            cr.delete(MetaData.ClipboardTable.uri, null, null);
	        } else {
	            EditorActivity.showToast(mEditorUiUtility.getContext(), R.string.warning_no_data_has_been_copy);
	        }
		}
        
        if (mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_DOODLE) {
			mDoodleView.redrawAll(true);
		}
    }

    public void enableBoxEditor() {
        if (mBoxEditText == mCurrentEditText) {
            return;
        }
        mCurrentEditText = mBoxEditText;
        mBoxEditText.setText("");
        mBoxEditText.setCursorVisible(true);
        mBoxEditText.clearUndoRedoStack();
        mBoxEditText.setVisibility(View.VISIBLE);
        mBoxEditText.requestFocus();

        mCurrentInputConnection = mBoxEditTextInputConnection;
    }

    public void cancleBoxEditor() {
		if (mCurrentEditText == mBoxEditText) {
			mCurrentEditText = mNoteEditText;
			mCurrentInputConnection = mNoteEditTextInputConnection;
			mNoteEditText.requestFocus();
			mBoxEditText.setVisibility(View.GONE);
		}
    }

    public void finishBoxEditor() {
        cancleBoxEditor();

        if (mBoxEditText.getText().length() <= 0) {
		//BEGIN: Show
            if (mEditorUiUtility.IsTextImgEdit()){ 
            	mDoodleView.deleteTextImg();
            }
            else
            {
		//END: Show
            	mEditorUiUtility.setInputMode(mEditorUiUtility.getInputModeFromPreference());
            } //By Show
	            return;
        }
        Editable content =(Editable) mBoxEditText.getText();
        Rect boundrect = mBoxEditText.getBound();
        if (boundrect.right <= 0) {
            boundrect.right = 1;
        }
        if (boundrect.bottom <= 0) {
            boundrect.bottom = 1;
        }
        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(boundrect.right, boundrect.bottom, Bitmap.Config.ARGB_8888);
        }
        catch (OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] Save text image failed !!!");
        }

        if (bmp != null) {
            Canvas canvas = new Canvas(bmp);

            mBoxEditText.setPadding(0, mBoxEditTextTop, 0, 0);
            mBoxEditText.setBackgroundColor(Color.TRANSPARENT);
            mBoxEditText.setCursorVisible(false);
            mBoxEditText.drawContent(canvas);
            mBoxEditText.setCursorVisible(true);
            mBoxEditText.setBackgroundResource(R.drawable.boxedittext_bg);
            mBoxEditText.setPadding(10, mBoxEditTextTop, 10, 5);
        }
        //BEGIN:Show  
        if (mEditorUiUtility.IsTextImgEdit())
        { 
        	mDoodleView.updateTextImg(bmp, content);
        }
        else
        {  
        // BEGIN: archie_huang@asus.com
        	mDoodleView.insertTextImg(bmp, content.append(" "), null);     // By Show
            mEditorUiUtility.setInputMode(InputManager.INPUT_METHOD_INSERT);
        }
        // END: archie_huang@asus.com
		//END:Show 
    }

    //Begin Allen    
    public void onVoiceRecognitionResult(String result){
    	//begin smilefish fix bug 374903
    	int index = mCurrentEditText.getSelectionEnd();
    	mCurrentEditText.getText().replace(index, index, result);
    	//end smilefish
    }
    
    public int getScrollY(){
    	return mPageEditorScrollBar.getScrollY();
	}
    
    public int getScrollX(){
    	return mPageEditorScrollBar.getScrollX();
    }    
    //End Allen
        
    public void ScrollViewTo(int x, int y,boolean needReSetScrollBarPosition){
    	//here x,y is the real value.
    	if(y < -1)
    	{
    		y = 0;
    	}
    	else if(y == -1)
    	{
    		y = mPageEditorScrollBar.getScrollY();
    	}
    	else if(y > mPageEditorScrollBar.getMaxScrollY())
    	{
    		y = (int)mPageEditorScrollBar.getMaxScrollY();
    	}
    	
    	if(x < -1)
    	{
    		x = 0;
    	}
    	else if(x == -1)
    	{
    		x = mPageEditorScrollBar.getScrollX();
    	}
    	else if(x > mPageEditorScrollBar.getMaxScrollX())
    	{
    		x = (int)mPageEditorScrollBar.getMaxScrollX();
    	}
//    	if(PickerUtility.is720DPDevice(mContext) && !isPhoneSizeMode()
//    			&& mNoteEditText.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
//    			&& !mEditorUiUtility.getHandWritingView().getEnable() 
//    			&& !MetaData.IS_KEYBOARD_SHOW){
//			y /= getScaleY();
//    	}
    	scrollEditTextTo(x,y);
    	scrollDoodleViewTo(x,y);
    	mPageEditorScrollBar.setScrollX(x);
    	mPageEditorScrollBar.setScrollY(y);
    	if(needReSetScrollBarPosition){
    		mPageEditorScrollBar.setScrollBarPosition(x,y);//Allen
    	}
    	if(mMicroView!=null)//&&mEditorUiUtility.getInputMode()==InputManager.INPUT_METHOD_READONLE)
    	{
    		mMicroView.onScrollChanged(x, y);//Allen
    	}
    }
    
    private void scrollEditTextTo(int x, int y) {
        if (mNoteEditText == null) {
            return;
        }
    	//Begin Allen
    	if(y <= getTemplateLayoutScaleHeight() + mNoteEditText.getPaddingTop()*getScaleY())
    		y = 0;
    	else
    		y = (int)(y-getTemplateLayoutScaleHeight() - mNoteEditText.getPaddingTop()*getScaleY());
    	//End Allen
    	
    	//Begin:Dave. 0814. Fix the bug: the meeting mode can not auto scroll.
    	if(mCurrentEditText != null && mCurrentEditText.getSelectionStart() != mCurrentEditText.getSelectionEnd())
    	{
    		if(mCurrentEditText != null)
			mCurrentEditText.scrollNoteEditTextTo(0, (int) (y/getScaleY()));
    	}else 
    	{
    		mNoteEditText.scrollNoteEditTextTo(0, (int) (y/getScaleY()));
		}
    	//End.Dave.0814.
    }

    //Begin Allen
    private void scrollDoodleViewTo(int x,int y){
		if (y <= getTemplateLayoutScaleHeight()  + mNoteEditText.getPaddingTop()*getScaleY()){
				mTemplateLinearLayout.scrollTo((int)(x/getScaleX()), (int)(y/getScaleY()));
		}
		else{
				mTemplateLinearLayout.scrollTo((int)(x/getScaleX()), (int)(getTemplateLayoutScaleHeight()/getScaleY() + mNoteEditText.getPaddingTop()));
		}

		if (mDoodleView != null && mIsScrollDoodle) {	
				mDoodleView.scrollContentTo(x, y);
		}
    }
    
    public EditorUiUtility getEditorUiUtility()
    {
    	return mEditorUiUtility;
    }
    
    public boolean isLeftBound() {
        return mNoteEditText.getScrollX() <= 0;
    }
    
    //End:Allen
    public void scrollEditText(int x, int y) {
         scrollViewBy(x,y);             
    }
    
    //Begin Allen    
    public void scrollViewBy(int x,int y){
    	if(x == 0 && y ==0)
    	{
    		return;
    	}
		x += getScrollX();
		y += getScrollY();
		if (mMicroView != null) {
			//emmanual to fix bug 493124
			setMicroViewVisible(true);
		}
		ScrollViewTo(x,y,true);
    }

    // BEGIN: archie_huang@asus.com
    public boolean groupDoodleObject(boolean group) {
        if (group) {
            return mDoodleView.group();
        }
        else {
            return mDoodleView.unGroup();
        }
    } // END: archie_huang@asus.com

    public void setTextStyle(int style) {
        mTextStyle = style;
    }

    public int getTextStyle() {
        return mTextStyle;
    }

    public int getNoteEditTextLayoutHeight() {
        return (int) (mNoteEditText.getHeight()*getScaleY());//Allen
    }

    public float getNoteEditTextScaleX() {
    	return getScaleX();
    }
    
    public float getNoteEditTextScaleY() {
    	return getScaleY();
    }

    public int getScreenWidth() {
    	//BEGIN: RICHARD MODIFY FOR MULTI WINDOW
    	if(mDoodleView != null && mDoodleView.getRootView() != null)
    	{
    		mScreenWidth = mDoodleView.getRootView().getWidth();
    	}
    	else //if (mScreenWidth == -1)
		{
            // BEGIN: archie_huang@asus.com
            Context context = EditorUiUtility.getContextStatic();
            if (context == null) {
                return -1;
            }
            // To avoid NullPointerException
            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
            mScreenWidth = dm.widthPixels;
        }
        return mScreenWidth;
    	//END: RICHARD MODIFY FOR MULTI WINDOW
    }

    // BEGIN: archie_huang@asus.com
    public int getScreenHeight() {
        if (mScreenHeight == -1) {
            Context context = EditorUiUtility.getContextStatic();
            if (context == null) {
                return -1;
            }
            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
            mScreenHeight = dm.heightPixels;
        }
        return mScreenHeight;
    } // END: archie_huang@asus.com

    //Begin Allen
    public int[] getViewTopLoaction(){
    	int[] location = new int[2];
        mTemplateLinearLayout.getLocationOnScreen(location);
        location[0] -= mTemplateLinearLayout.getScrollX();
        location[1] -= mTemplateLinearLayout.getScrollY();
        return location;
    }
    //End Allen
    
    public int getNoteEditTextLineBounds(Rect lineBounds) {
        int baseLine = (int) (mNoteEditText.getLineBounds(0, lineBounds));
        return baseLine;
    }

    public int getNoteEditTextLineHeight() {
        return mNoteEditText.getLineHeight();
    }

    public int getNoteEditTextLineCountLimited() {
        return mNoteEditText.getLineCountLimited();
    }

    // BEGIN: archie_huang@asus.com
    public boolean drawNoteEditTextLine(Canvas canvas, int pageNo,boolean bIsShare) {
    	if (mTemplateType == MetaData.Template_type_todo) {
    		return true;
    	} else {
    		return mNoteEditText.drawLine(canvas, mEditorUiUtility.getBookGridType(), pageNo,bIsShare);
    	}
    } // END: archie_huang@asus.com

    public String getEditorText() {
        if (mNoteEditText == null) {
            return "";
        }

        return new String(mNoteEditText.getText().toString());
    }

    //darwin
    public String getOnlyTextWithVO() {
        if (mNoteEditText == null) {
            return "";
        }
        //add by mars fix editable == null 
        Editable editable = changeEditTextToType();
        if(editable == null){
        	return "";
        }
        //end by mars
        String str = editable.toString();
        str = str.replace(OBJ, " ");

        return str;
    }
    
    //darwin
    
    public String getOnlyText() {
        if (mNoteEditText == null) {
            return "";
        }

        String str = mNoteEditText.getText().toString();
        str = str.replace(OBJ, " ");

        return str;
    }

    public View getCurrentEditor() {
        return mCurrentEditText;
    }
    
    // BEGIN: Better
    public NoteEditText getNoteEditText() {
    	return mNoteEditText;
    }
    // END: Better

    // BEGIN: james5_chan@asus.com
    public String getFilePath() {
        return mEditorUiUtility.getFilePath();
    }
    // END: james5_chan@asus.com

    public void setEditorUndoEmpty(boolean empty) {
        if (mUndoStackAvailableListener == null) {
            return;
        }
        if (mCurrentEditText == mNoteEditText) {
            mUndoStackAvailableListener.onNoteUndoStackAvailableChange(!empty);
        }
        else if(mCurrentEditText==mBoxEditText){
            mUndoStackAvailableListener.onBoxUndoStackAvailableChange(!empty);
        }
        else 
        //Beign Allen
        if(mCurrentEditText instanceof TemplateEditText){
        	mUndoStackAvailableListener.onNoteUndoStackAvailableChange(!empty);
        }
        //End Allen
    }
    
    public void setEditorRedoEmpty(boolean empty) {
        if (mRedoStackAvailableListener == null) {
            return;
        }

        if (mCurrentEditText == mNoteEditText) {
            mRedoStackAvailableListener.onNoteRedoStackAvailableChange(!empty);
        }
        else if(mCurrentEditText==mBoxEditText){
            mRedoStackAvailableListener.onBoxRedoStackAvailableChange(!empty);
        }
        else
        //Beign Allen
        if(mCurrentEditText instanceof TemplateEditText){
        	mRedoStackAvailableListener.onNoteRedoStackAvailableChange(!empty);
        }
        //End Allen
    }
    
    public void setDoodleUndoEmpty(boolean empty) {
        if (mUndoStackAvailableListener == null) {
            return;
        }
        mUndoStackAvailableListener.onDoodleUndoStackAvailableChange(!empty);
    }

    // BEGIN: archie_huang@asus.com
    public void setDoodleRedoEmpty(boolean empty) {
        if (mRedoStackAvailableListener == null) {
            return;
        }
        mRedoStackAvailableListener.onDoodleRedoStackAvailableChange(!empty);
    } // END: archie_huang@asus.com

    public void setDoodleItemEmpty(boolean empty) {
        if (mDoodleItemAvailableListener == null) {
            return;
        }
        mDoodleItemAvailableListener.onAvailableChange(!empty);
    }

    // BEGIN: archie_huang@asus.com
    public void setDoodleItemSelect(boolean somethingSelected, boolean multiple, boolean group) {
        if (somethingSelected) {
            mDoodleItemSelectListener.onSelect(multiple, group);
        }
        else {
            mDoodleItemSelectListener.onUnSelect();
        }
    } // END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public void setDoodleItemSelectListener(onDoodleItemSelectListener listener) {
        mDoodleItemSelectListener = listener;
    } // END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public void backToPreviousMode() {
        mEditorUiUtility.setInputMode(mEditorUiUtility.getInputModeFromPreference());
    } // END: archie_huang@asus.com

    //Begin Allen   
    public boolean isTouchOnTemplateRect(MotionEvent event)
    {
    	if(mTemplatelayout!=null && mTemplateType==MetaData.Template_type_todo){
    		return true;
    	}
    	if(mTemplatelayout!=null&&(getDeviceType()>100)&&isPhoneSizeMode()&&mNoteEditText.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
    		int touchYOffset =  NoteEditText.PHONEBOOK_IN_PAD_PORTRAIT_MARGIN_TOP;
            float layoutOffset = (mScreenWidth - mTemplatelayout.getWidth()) / 2;
    		if((mTemplatelayout!=null && 
    				(event.getY()-touchYOffset+mTemplateLinearLayout.getScrollY())<(mTemplatelayout.getHeight()+mTemplatelayout.getTop()))&&
    				(event.getY()>touchYOffset)&&
    				(event.getX()+layoutOffset)<(mTemplatelayout.getWidth()+mTemplatelayout.getLeft())&&
    				(event.getX())>layoutOffset){
            	return true;
            }
            else{
            	return false;
            }
    	}
    	
        if(mTemplatelayout!=null && (event.getY()+mTemplateLinearLayout.getScrollY())<(mTemplatelayout.getHeight()*mTemplatelayout.getScaleY()+mTemplatelayout.getTop()))
        {
        	return true;
        }
        else{
        	return false;
        }
    }
    
    public void setNoteEditTextRequestFocus(){
    	if(mCurrentEditText!=mBoxEditText && mTemplateType!=MetaData.Template_type_todo){
        	mCurrentEditText = mNoteEditText;
        	mCurrentEditText.requestFocus();
    		}
    }
    //End Allen

    public DoodleView getDoodleView() {
    	return mDoodleView;
    }

    // BEGIN: archie_huang@asus.com
    public void onCheckClickableItem(MotionEvent event) {
        mCurrentEditText.onCheckClickableItem(event);
    } // END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public void onNoteEditTextReady() {
        mDoodleView.drawLine();
    } // END: archie_huang@asus.com

    public boolean isSelectionTextMode() {
        if (!checkHaveInputManager()) {
            return false;
        }

        return mInputManager.getInputMode() == InputManager.INPUT_METHOD_SELECTION_TEXT;
    }

    public boolean isReadOnlyMode() {
        if (!checkHaveInputManager()) {
            return false;
        }

        return mInputManager.getInputMode() == InputManager.INPUT_METHOD_READONLE;
    }

    public boolean isTextImgMode() {
        if (!checkHaveInputManager()) {
            return false;
        }

        return mInputManager.getInputMode() == InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD
                || mInputManager.getInputMode() == InputManager.INPUT_METHOD_TEXT_IMG_SCRIBBLE;
    }

    public void switchToSelectionTextMode() {
        if (!checkHaveInputManager()) {
            return;
        }

        if (mCurrentEditText == mBoxEditText) {
        	//emmanual to fix bug 465474
        	if(mInputManager.getInputMode() != InputManager.INPUT_METHOD_SELECTION_TEXT){
        		mImgTextPreviousMode = mInputManager.getInputMode();
        	}
            //Begin: show_wang@asus.com
            //Modified reason: for dds
            mEditorUiUtility.getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
            .edit().putBoolean(MetaData.PREFERENCE_IS_TEXTIMG_SELECTION_TEXT, true).commit();
            mEditorUiUtility.getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
            .edit().putInt(MetaData.PREFERENCE_TEXTIMG_MODE_SELECTION_TEXT, mImgTextPreviousMode).commit();
          //End: show_wang@asus.com
        }
        else {
        	//Begin: show_wang@asus.com
            //Modified reason: for dds
        	mEditorUiUtility.getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
            .edit().putBoolean(MetaData.PREFERENCE_IS_TEXTIMG_SELECTION_TEXT, false).commit();
        	//End: show_wang@asus.com
        }
        if (mInputManager.getInputMode() != InputManager.INPUT_METHOD_SELECTION_TEXT) {
            mInputManager.setInputMode(InputManager.INPUT_METHOD_SELECTION_TEXT);
        }
        
        //Begin:Dave. To add text selector.
        if(MetaData.ENABLE_SELECTOR_HANDLER)
        {
        	editSelectionHandleLeft.setVisibility(View.VISIBLE);
        	editSelectionHandleRight.setVisibility(View.VISIBLE);
        }
        //End:Dave.
    }

    public void quitSelectionTextMode() {
        //Begin:Dave. To add text selector.
        editSelectionHandleLeft.setVisibility(View.INVISIBLE);
        editSelectionHandleRight.setVisibility(View.INVISIBLE);
        //End:Dave.
        
        if (!checkHaveInputManager()) {
            return;
        }
        if (mInputManager.getInputMode() != InputManager.INPUT_METHOD_SELECTION_TEXT) {
            return;
        }

        mCurrentEditText.setSelection(mCurrentEditText.getSelectionEnd());
        if (mCurrentEditText == mBoxEditText && mImgTextPreviousMode != -1) {
            mInputManager.setInputMode(mImgTextPreviousMode);
        }
        else {
            mInputManager.setInputMode(mEditorUiUtility.getInputModeFromPreference());
        }
        
        //emmanual to fix bug 377969
        if (mCurrentEditText != mBoxEditText 
        		//emmanual to fix bug 568597
        		&& mInputManager.getInputMode() != InputManager.INPUT_METHOD_SCRIBBLE) {
        	getEditorUiUtility().setHandWriteViewEnable(false);
        }
        ((EditorActivity)(mEditorUiUtility.getContext())).setScrollBarBottom(false);
        shouldDiscardPoint = true; //Dave.
    }

    //darwin
    public boolean isEditTexthaveObjects() {
        return (mNoteEditText.getText().length() != 0);
    }
    
    public boolean isTemplatehaveObjects() {
        return (mTemplatelayout != null);
    }
    //darwin
    
    public boolean isDoodlehaveObjects() {
        return mDoodleView.haveObjects();
    }

    public int getCurrentEditTextPaddingLeft() {
        return mCurrentEditText.getPaddingLeft();
    }

    public int getDeviceType()
    {
    	return mEditorUiUtility.getDeviceType();
    }
    public boolean isPhoneSizeMode() {
        return mEditorUiUtility.isPhoneSizeMode();
    }

    public void showSelectionTextHint(boolean show) {
		//emmanual to fix bug 499120, 498495
    	if(mCurrentEditText!=null&&!mCurrentEditText.getText().toString().equals("")){
    		mEditorUiUtility.showSelectionTextHint(show);
    	}
    }

    // Interface
    public interface onUndoStackAvailableListener {
        public void onNoteUndoStackAvailableChange(boolean availabele);

        public void onBoxUndoStackAvailableChange(boolean availabele);

        public void onDoodleUndoStackAvailableChange(boolean availabele);
    }

    public interface onRedoStackAvailableListener {
        public void onNoteRedoStackAvailableChange(boolean availabele);

        public void onBoxRedoStackAvailableChange(boolean availabele);

        public void onDoodleRedoStackAvailableChange(boolean availabele);
        
    }

    public interface onCheckAvailableListener {
        public void onAvailableChange(boolean available);
    }

    public interface onClipBoardAvailableListener extends onCheckAvailableListener {
    }

    public interface onDoodleItemAvailableListener extends onCheckAvailableListener {
    }

    // BEGIN: archie_huang@asus.com
    public interface onDoodleItemSelectListener {
        void onSelect(boolean mutiple, boolean group);

        void onUnSelect();
    } // END: archie_huang@asus.com

    public interface onEditorColorChangeListener {
        void onColorChange(int color);
    }

    public interface onEditorBoldChangeListener {
        void onBoldChange(int TextStyel);
    }

    // for orientation cursor
    public int getCursorPos() {
        return mNoteEditText.getSelectionEnd();
    }

    public void setCursorPos(int pos) {
        if (pos <= mNoteEditText.getText().length()) {//Richard modify < to <=
            mNoteEditText.setSelection(pos);
        }else if(mNoteEditText.getText().length() == 0)//begin wendy
        {
        	mNoteEditText.setSelectionEx(pos);
        }//end wendy
        //emmanual to fix bug 431667
        else if(pos > mNoteEditText.getText().length()){
            mNoteEditText.setSelection(mNoteEditText.getText().length());
        }
    }

    // BEGIN: archie_huang@asus.com
    public boolean drawScreen() {
        if (mCurrentEditText != mBoxEditText) {
            mDoodleView.drawScreen(false, null);
            return true;
        }
        return false;
    } // END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public void drawNoteEditText(Canvas canvas,int scrollX,int scrollY) {//Allen
        mNoteEditText.drawEditText(canvas,scrollX,scrollY);
    } // END: archie_huang@asus.com

    //Begin Allen
    public void drawTemplateView(Canvas canvas)
    {
    	if(mTemplatelayout!=null){
    		mTemplatelayout.drawToSurfaceView(canvas);
    	}
    }
    //darwin

    public void drawTemplateView(Canvas canvas,int width,int hight)
    {
    	if(mTemplatelayout!=null){
    		float scaleX = (float) width / (float) mNoteEditText.mWidth;
            float scaleY = (float) hight / (float) mTemplateLayoutHeight;//Allen
            canvas.save();
            //darwin
			//BEGING: RICHARD
            canvas.scale(scaleX, scaleY);
			//END
            //darwin
            int currentSelectionStart = 0;
            int currentSelectionEnd = 0;
            boolean setSelection = false;
            if(mCurrentEditText instanceof TemplateEditText && mCurrentEditText.hasFocus()){
            	setSelection = true;
            	currentSelectionStart = mCurrentEditText.getSelectionStart();
                currentSelectionEnd = mCurrentEditText.getSelectionEnd();
                mCurrentEditText.setSelection(0, 0);
            }
            
            boolean tempNoteEditEnable = isNoteEditEnable;
            if(isNoteEditEnable){
              setNoteEditTextEnable(false);
            }

            mTemplatelayout.drawToSurfaceView(canvas);//mTemplateLinearLayout.getScrollX(),mTemplateLinearLayout.getScrollY()
	        if(tempNoteEditEnable != isNoteEditEnable){
	            setNoteEditTextEnable(tempNoteEditEnable);
	        }
	        canvas.restore();
	          
	        if(setSelection){
	            mCurrentEditText.setSelection(currentSelectionStart, currentSelectionEnd);
	        }

    	}
    }
    //darwin
    //End Allen
    
    public void insertTimestamp(long time, long pageid) {
        NoteTimestampItem item = new NoteTimestampItem(time, getImageSpanHeight(), pageid);
        Editable editable = new SpannableStringBuilder(OBJ);
        editable.setSpan(item, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        addItemToEditText(editable);

        // BEGIN: archie_huang@asus.com
        // Fix bug: Screen will not refresh in doodle mode
        if (!mNoteEditText.isNoteEditTextEnable()) {
            drawScreen();
        }
        // END: archie_huang@asus.com
    }
    //begin wendy :
    //fix bug:screen will not refresh in doodle mode
    public void reflashScreen()
    {
        if (!mNoteEditText.isNoteEditTextEnable()) {
            drawScreen();
        }
    }
    //end wendy :
    //fix bug:screen will not refresh in doodle mode
	
	//BEGIN:Show   
    public String getFileInfo(){
    	return mDoodleView.getFileInfo();    	
    }
    
    //Begin Darwin_Yu@asus.com
    public void UpDateCurDrawInfo(String filename,String originalFileName)
    {
    	mDoodleView.UpDateCurDrawInfo(filename);
    	mIsAttachmentModified = true;
        m_list.add(filename);
        boolean bIsRemoved = false;
        for(String name : m_list)
        {
        	if(name.equalsIgnoreCase(originalFileName))
        	{
        		m_list.remove(originalFileName);
        		bIsRemoved = true;
        	}
        }
        if(!bIsRemoved)
        {
        	m_removeList.add(originalFileName);
        }
    }
    //End   Darwin_Yu@asus.com
    
    public void setDoodleCropButtonsEnable(boolean flag){
    	
    	mEditorUiUtility.setDoodleCropButtonsEnable(flag);
    }
	
	public void setDoodleTextEditButtonsEnable(boolean flag){    	
    	mEditorUiUtility.setDoodleTextEditButtonsEnable(flag);
    }
    
    public void setDoodleChangeImgButtonsEnable(boolean flag){    	
    	mEditorUiUtility.setDoodleChangeImgButtonsEnable(flag);
    }
    //begin wendy
    public void setDoodlePastButtonsEnable(boolean flag)
    {
    	mEditorUiUtility.setDoodlePastButtonsEnable(flag);
    }
    
    //end wendy
	
	public void enableModifyBoxEditor() 
	{
        if (mBoxEditText == mCurrentEditText) {
            return;
        }
        Editable content = mDoodleView.getEditBoxTextContent();
        mCurrentEditText = mBoxEditText;
        setFontSize(content);
        mBoxEditText.setIsFirstTimeLoad(true);
        mBoxEditText.setText(content); 
        mBoxEditText.setIsFirstTimeLoad(false);
        mBoxEditText.setCursorVisible(true);
        mBoxEditText.setSelection(content.length());
        mBoxEditText.clearUndoRedoStack();
        mBoxEditText.setVisibility(View.VISIBLE);
        mBoxEditText.requestFocus();
        mCurrentInputConnection = mBoxEditTextInputConnection;
    }
	
	//END:Show
//END:Show

    //begin noah
	public ArrayList<NoteHandWriteItem> getOrderedNoteHandWriteItems(EditText editText, int start, int end)
    {
        NoteItem[] selectItems = getNoteItemToCopy(editText, start, end);
        ArrayList<NoteHandWriteItem> res = new ArrayList<NoteHandWriteItem>(); 
        
        for(int i = 1; i < selectItems.length ; i++)
        {
        	if(selectItems[i] instanceof NoteHandWriteItem)
        	{
        		int j=0;
        		for(j = 0; j<res.size();j++)
        		{
        			if(res.get(j).getStart() > selectItems[i].getStart())
        			{
        				break;
        			}
        		}
        		res.add(j, (NoteHandWriteItem)selectItems[i]);
        	}
        }
        
        return res;
    }
	//end noah
	//BEGIN: RICHARD
    public ArrayList<NoteHandWriteItem> getOrderedNoteHandWriteItems(int start, int end)
    {
        NoteItem[] selectItems = getNoteItemToCopy(start, end);
        ArrayList<NoteHandWriteItem> res = new ArrayList<NoteHandWriteItem>(); 
        
        for(int i = 1; i < selectItems.length ; i++)
        {
        	if(selectItems[i] instanceof NoteHandWriteItem)
        	{
        		int j=0;
        		for(j = 0; j<res.size();j++)
        		{
        			if(res.get(j).getStart() > selectItems[i].getStart())
        			{
        				break;
        			}
        		}
        		res.add(j, (NoteHandWriteItem)selectItems[i]);
        	}
        }
        
        return res;
    }
	//END: RICHARD
    
    public Editable changeEditTextToType() {
		// TODO Auto-generated method stub
		if(mAsusInputRecognizer == null)
		{
			return null;
		}
		//begin noah;��Ϊtravel template��ʱ����Ҫ����noteEditText�����ݷ��أ�����title
		EditText editText = mCurrentEditText;
		if(mTemplateType == MetaData.Template_type_travel){
			editText = mNoteEditText;
		}
		//end noah
        int start = 0;
        int end = editText.length() ;       
        
        ArrayList<NoteHandWriteItem> itemList = getOrderedNoteHandWriteItems(editText, start, end);//noah
        Editable editableTemp  = new SpannableStringBuilder(editText.getText().subSequence(start, end));
        
		int language =  mEditorUiUtility.getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
				.getInt(mEditorUiUtility.getContext().getResources().getString(R.string.pref_index_language), 0);
		try
		{
			mAsusInputRecognizer.loadResource(language);
		}catch(Exception e)
		{
			return null;
		}
        for(int i = itemList.size() -1; i>=0 ;i--)
        {
        	mAsusInputRecognizer.addStroke(itemList.get(i));
        	int color = itemList.get(i).getColor();
        	float strokeWidth = itemList.get(i).getStrokeWidth();
    		String recognizerString = mAsusInputRecognizer.getResult();

    		editableTemp.replace(itemList.get(i).getStart() + start, itemList.get(i).getEnd() + start, recognizerString);
    		editableTemp.removeSpan(itemList.get(i));
    		
    		
    		int currentColorStartIndex = itemList.get(i).getStart();
            if (color != Color.BLACK) {
                for (int j = 0; j < recognizerString.length(); j++) {
                    NoteForegroundColorItem foreColorItem = new NoteForegroundColorItem(color);
                    editableTemp.setSpan(foreColorItem, start+currentColorStartIndex + j , start+currentColorStartIndex +j + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //mCurrentEditText.getText().setSpan(foreColorItem, currentColorStartIndex + j , currentColorStartIndex +j + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            if (strokeWidth != MetaData.SCRIBBLE_PAINT_WIDTHS_NORMAL) {
                for (int j = 0; j < recognizerString.length(); j++) {
                    NoteTextStyleItem stylespan = new NoteTextStyleItem(Typeface.BOLD);
                    editableTemp.setSpan(stylespan, start + currentColorStartIndex + j, start+ j + currentColorStartIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return editableTemp;        
    }
    
    //BEGIN: RICHARD
	public void changeTheSelectionTextToType() {
		// TODO Auto-generated method stub
		if(mAsusInputRecognizer == null)
		{
			return;
		}
		
        int start = Math.min(mCurrentEditText.getSelectionStart(), mCurrentEditText.getSelectionEnd());
        int end = Math.max(mCurrentEditText.getSelectionStart(), mCurrentEditText.getSelectionEnd());       
        
        ArrayList<NoteHandWriteItem> itemList = getOrderedNoteHandWriteItems(start,end);
        
        Editable orieditable = new SpannableStringBuilder(mCurrentEditText.getText().subSequence(start, end));
        mCurrentEditText.setChangeBecauseAutoRecgnizer(true);
        int changedCount = 0;
		int language =  mEditorUiUtility.getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
				.getInt(mEditorUiUtility.getContext().getResources().getString(R.string.pref_index_language), 0);
		try
		{
			mAsusInputRecognizer.loadResource(language);
		}catch(Exception e)
		{
			mCurrentEditText.setChangeBecauseAutoRecgnizer(false);
			return;
		}
        for(int i = itemList.size() -1; i>=0 ;i--)
        {
        	mAsusInputRecognizer.addStroke(itemList.get(i));
        	int color = itemList.get(i).getColor();
        	float strokeWidth = itemList.get(i).getStrokeWidth();
    		String recognizerString = mAsusInputRecognizer.getResult();
    		changedCount += recognizerString.length() - 1;
    		
    		mCurrentEditText.getText().replace(itemList.get(i).getStart() + start, itemList.get(i).getEnd() + start, recognizerString);
    		mCurrentEditText.getText().removeSpan(itemList.get(i));
    		
    		
    		int currentColorStartIndex = itemList.get(i).getStart();
            if (color != Color.BLACK) {
                for (int j = 0; j < recognizerString.length(); j++) {
                    NoteForegroundColorItem foreColorItem = new NoteForegroundColorItem(color);
                    mCurrentEditText.getText().setSpan(foreColorItem, start+currentColorStartIndex + j , start+currentColorStartIndex +j + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            if (strokeWidth != MetaData.SCRIBBLE_PAINT_WIDTHS_NORMAL) {
                for (int j = 0; j < recognizerString.length(); j++) {
                    NoteTextStyleItem stylespan = new NoteTextStyleItem(Typeface.BOLD);
                    mCurrentEditText.getText().setSpan(stylespan, start + currentColorStartIndex + j, start+ j + currentColorStartIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        mCurrentEditText.setChangeBecauseAutoRecgnizer(false);
        Editable chaneditable = new SpannableStringBuilder(mCurrentEditText.getText().subSequence(start, end + changedCount));

        mCurrentEditText.insertToHistoryStack(orieditable, chaneditable, start);
        mCurrentEditText.setIsModified(true);//Allen
	}


	public void setIsAutoRecognizerShape(boolean checked) {
		// TODO Auto-generated method stub
		mDoodleView.setIsAutoRecognizerShape(checked);
	}
	//END: RICHARD
	
	//Begin Darwin_Yu@asus.com
    private boolean mIsAttachmentModified = false;
    public void setIsAttachmentModified(boolean mIsAttachmentModified) {
		this.mIsAttachmentModified = mIsAttachmentModified;
	}

	private List<String> m_list = new CopyOnWriteArrayList<String>();
    private List<String> m_removeList = new CopyOnWriteArrayList<String>();
    public List<String> m_ItemListInit =  new CopyOnWriteArrayList<String>();
    private List<String> m_ItemListSave =  new CopyOnWriteArrayList<String>();
    public List<String> getAttachmentNameList()
    {
    	return m_list;
    }
    public List<String> getAttachmentRemoveNameList()
    {
    	return m_removeList;
    }
    public void updateAttachmentRemoveNameList(String name)
    {
    	boolean bIsRemoved = false;
    	if(m_list.size() != 0)
    	{
    		for(String fileName : m_list)
    		{
    			if(fileName.equalsIgnoreCase(name))
    			{
    				m_list.remove(fileName);
    				bIsRemoved = true;
    				break;
    			}
    		}
    	}
    	if(!bIsRemoved)
    	{
    		m_removeList.add(name);
    	}
    	if((m_list.size() != 0) || (m_removeList.size() != 0))
    	{
    		mIsAttachmentModified = true;
    	}
    	else
    	{
    		mIsAttachmentModified = false;
    	}
    }
    public void updateAttachmentAddNameList(String name)
    {
    	boolean bIsRemoved = false;
    	if(m_removeList.size() != 0)
    	{
    		for(String fileName : m_removeList)
    		{
    			if(fileName.equalsIgnoreCase(name))
    			{
    				m_removeList.remove(fileName);
    				bIsRemoved = true;
    				break;
    			}
    		}
    	}
    	if(!bIsRemoved)
    	{
    		m_list.add(name);
    	}
    	if((m_list.size() != 0) || (m_removeList.size() != 0))
    	{
    		mIsAttachmentModified = true;
    	}
    	else
    	{
    		mIsAttachmentModified = false;
    	}
    }
    public boolean isAttachmentModified()
    {
    	return mIsAttachmentModified;
    }
    public void resetAttachmentModified()
    {
    	mIsAttachmentModified = false;
    }
    
    public void getItemList(List<String> list)
    {
    	Editable editable = (Editable) mNoteEditText.getText().subSequence(0, mNoteEditText.getText().length());
        NoteItem[] spanItems = editable.getSpans(0, editable.length(), NoteItem.class);
        for(NoteItem item : spanItems)
        {
        	if( item instanceof NoteSendIntentItem )
        	{
        		list.add(((NoteSendIntentItem) item).getFileName());
        	}
        }
    }
    public void getDeleteItamList()
    {
    	getItemList(m_ItemListSave);
    	if(m_ItemListInit.size() != 0)
    	{
	    	for(String nameIn : m_ItemListInit)
	    	{
	    		boolean bIsExist = false;
	    		for(String nameOut : m_ItemListSave)
	    		{
	    			if(nameIn.equalsIgnoreCase(nameOut))
	    			{
	    				bIsExist = true;
	    				break;
	    			}
	    		}
	    		if(!bIsExist)
	    		{
	    			m_removeList.add(nameIn);
	    			mIsAttachmentModified = true;
	    		}
	    	}
    	}
    }
    //End   Darwin_Yu@asus.com
    
    //Begin: show_wang@asus.com
    //Modified reason: fix bug, insert textimg paste button disable when clipboard type is CLIPBOARD_TYPE_DOODLE
    public Boolean IsNoteEditTextCurrentEditor() {
        if ( mCurrentEditText == mNoteEditText ) {
        	return true;
        }
        return false;
    }
    //End: show_wang@asus.com
    
    //Begin: show_wang@asus.com
    //Modified reason: for dds
    public void saveSelectionDrawInfo(){
    	mDoodleView.saveSelectionDrawInfo();
    }
    
    public void saveBoxEditTextContent() {
    	if (mBoxEditText.getText().length() > 0) {
    		mContent =(Editable) mBoxEditText.getText();
    	}
    }
    
    public void setBoxEditTextContent() {
    	if( mContent != null ) {
    		mCurrentEditText = mBoxEditText;
	       	setFontSize(mContent);
	      	mBoxEditText.setIsFirstTimeLoad(true);
	      	mBoxEditText.setText(mContent); 
	     	mBoxEditText.setIsFirstTimeLoad(false);
	     	mBoxEditText.setCursorVisible(true);
        	SharedPreferences preferences = mEditorUiUtility.getContext()
        			.getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE);
	     	if (preferences.getInt(MetaData.PREFERENCE_CURRENT_INPUT_MODE, -1) 
	     			== InputManager.INPUT_METHOD_SELECTION_TEXT) {
	        	if (preferences.getBoolean(MetaData.PREFERENCE_IS_TEXTIMG_SELECTION_TEXT, false)) {
	        		int start = preferences.getInt(MetaData.PREFERENCE_SELECTION_TEXT_START, 0);
	        		int end = preferences.getInt(MetaData.PREFERENCE_SELECTION_TEXT_END, 0);
	        		if (end > mBoxEditText.length()) {
	        			end = mBoxEditText.length();
	        		}
	        		if (start > mBoxEditText.length()) {
	        			start = mBoxEditText.length();
	        		}
	        		mBoxEditText.setSelection(start, end);
	            }
	     	}
	      	mBoxEditText.setVisibility(View.VISIBLE);
	        mBoxEditText.clearUndoRedoStack();
	      	mBoxEditText.requestFocus();
	      	mCurrentInputConnection = mBoxEditTextInputConnection;
	      	mContent = new SpannableStringBuilder("");
    	}
    }
    
    public Boolean getConfigStatus(){
    	return mEditorUiUtility.getConfigStatus();
    }
    
  	public void selectCurrentDrawInfo() {
  		mDoodleView.selectCurrentDrawInfo();
  	}
  	
  	public void setTextImgStatus() {
  		mImgTextPreviousMode =  mEditorUiUtility.getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
  	            .getInt(MetaData.PREFERENCE_TEXTIMG_MODE_SELECTION_TEXT, InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD);
  	}
  	
	public void setFirstTextChange(boolean isFirstTextChange) {
		mNoteEditText.setFirstTextChange(isFirstTextChange);
	}
    //End: show_wang@asus.com

	public void setDrawingDialogStatus(Boolean show)
	{
		if(show)
		{
			mPageEditorManager.showDrawingDialog();
		}
		else
		{
			mPageEditorManager.hideDrawingDialog();
		}
	}

	private void setIsDataLoading(boolean flag) {
		mIsDataLoading = flag;
		if(mIsDataLoading)
		{
			mPageEditorManager.showLoadingDialog();
		}
		else
		{
			mPageEditorManager.onPageEditorLoadComplete();
		}
		
	}
	
	public Boolean getIsLoadComplete() {
		return !mIsDataLoading;
	}
	
	public boolean beginLoad()
	{
    	synchronized(mLock)
    	{
    		if(mIsSaveing || mIsDataLoading)
    		{
    			//current thread is saving or loading
    			return false;
    		}
    		else
    		{
    			//set beginload
    			mIsDataLoading = true;
    			setIsDataLoading(mIsDataLoading);
    			getDoodleView().setIsNeedDrawDoodleViewContent(false);
    			return true;
    		}
    	}
	}
	
	public boolean endLoad()
	{
	  	synchronized(mLock)
    	{
	  		mIsDataLoading = false;
	  		setIsDataLoading(mIsDataLoading);
			getDoodleView().setIsNeedDrawDoodleViewContent(true);
    		if(mIsSaveing || !mIsDataLoading)
    		{
    			// something must wrong
    			return false;
    		}
    		else
    		{
    			//set endload
    			return true;
    		}
    	}
	}
	
		public boolean beginSave()
	{
    	synchronized(mLock)
    	{
    		if(mIsSaveing || mIsDataLoading)
    		{
    			//current thread is saving or loading
    			Log.i("MetaData.DEBUG_TAG","current thread is saving or loading,we can not begin save");
    			return false;
    		}
    		else
    		{
    			//set beginload
    			Log.i("MetaData.DEBUG_TAG","beign save");
    			mIsSaveing = true;
    			return true;
    		}
    	}
	}
	
	public boolean endSave()
	{
	  	synchronized(mLock)
    	{
    		if(!mIsSaveing || mIsDataLoading)
    		{
    			// something must wrong
    			mIsSaveing = false;
    			return false;
    		}
    		else
    		{
    			mIsSaveing = false;
    			return true;
    		}
    	}
	}
	
	//Begin: show_wang@asus.com
	//Modified reason: for dds
	public void setDoodleGroupButtonsEnable(boolean enabled) {
		((EditorActivity)(mEditorUiUtility.getContext())).setDoodleGroupButtonsEnable(enabled);
	}
	
	public void setDoodleUnGroupButtonsEnable(boolean enabled) {
		((EditorActivity)(mEditorUiUtility.getContext())).setDoodleUnGroupButtonsEnable(enabled);
	}
	//End: show_wang@asus.com
	
	//Begin: show_wang@asus.com
	//Modified reason: for airview
	public TemplateLinearLayout getTemplateLinearLayout () {
		return mTemplateLinearLayout;
	}
	//End: show_wang@asus.com
	
	// begin jason : mark the event dispatch by Handwritingview
	private boolean mCoustomEvent=false;
	
	public boolean isDispatchByHandWritingViewEvent() {
		
		return mCoustomEvent;
		
	}
	public void setIsDispatchByHandWritingViewEvent(boolean flag)
	{
		mCoustomEvent=flag;
	}
	
	//begin smilefish for color picker
	public void drawSnapshot(Canvas resultCanvas, boolean isFirstLoad){
        try {            
            getResultNew(resultCanvas, resultCanvas.getWidth(), resultCanvas.getHeight(), isFirstLoad);
        }
        catch (OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] Get snapshot failed !!!");
        }
    }
	
	public void getResultNew(Canvas canvas, int width, int height, boolean isFirstLoad){                
    	int mFullWidth = mDoodleItemWidth;
    	int mFullHeight = mDoodleItemHeight;
    	int mOffsetX = getScrollX();
    	int mOffsetY = getScrollY();
    	Rect src = new Rect();
    	Rect dst = new Rect();
    	
    	Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
    	
    	if(getDeviceType() > 100 && mEditorUiUtility.isPhoneSizeMode())
    	{   		
    		int phoneBookWidth = mAdditionalViewLayout.getMeasuredWidth();
    		int phoneBookHeight = mAdditionalViewLayout.getMeasuredHeight();
    		Bitmap bitmap = Bitmap.createBitmap(phoneBookWidth, phoneBookHeight, Bitmap.Config.ARGB_8888);
    		bitmap.eraseColor(mEditorUiUtility.getBookColor());
    		
    		android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams)mAdditionalViewLayout.getLayoutParams();
    		int x = (width - phoneBookWidth) / 2;
    		int y = params.topMargin;
    		if(phoneBookHeight > height)
    		{
        		src.set(0, 0, phoneBookWidth, height);
        		dst.set(x, 0, phoneBookWidth + x, height);
    		}
    		else
    		{
    			src.set(0, 0, phoneBookWidth, phoneBookHeight);
    			dst.set(x, y, phoneBookWidth + x, phoneBookHeight + y);
    		}
    		
    		canvas.drawBitmap(bitmap, src, dst, paint);
    	}
    	 	
    	Bitmap mFullPageBitmap = Bitmap.createBitmap(mFullWidth, mFullHeight, Bitmap.Config.ARGB_8888);
    	Canvas mFullPageCanvas = new Canvas(mFullPageBitmap);  	
    	
    	int tempHeight = 0;
		if(mTemplateType==MetaData.Template_type_todo){
			tempHeight = mFullHeight;
		}
		else{
			tempHeight = (int) ((float)mTemplateLayoutHeight/((mNoteEditText.getOrignalEditTextHeight()+mTemplateLayoutHeight)*mNumPages)*mFullHeight);
		}
		drawTemplateView(mFullPageCanvas, mFullWidth, tempHeight);
    	if(isFirstLoad){
			mDoodleView.getResult(mFullPageCanvas, mFullWidth, mFullHeight, false, false, mTemplateLayoutHeight, -1,false);
		}
		else{
			mDoodleView.getCacheResult(mFullPageCanvas, mFullWidth, mFullHeight, false);
		}
    	mNoteEditText.getResultFullNew(mFullPageCanvas, mFullWidth, mFullHeight, false, mTemplateLayoutHeight, -1);    	
		
    	if((mFullWidth>=width) && (mFullHeight>=height)){
    		src.set(mOffsetX, mOffsetY, mOffsetX+width, mOffsetY+height);
    		dst.set(0, 0, width, height);
    	}else{
    		src.set(0, 0, mFullWidth, mFullHeight);
    		int x = 0;
    		int y = 0;
    		if(width > mFullWidth)
    			x = (width - mFullWidth) / 2;
    		if(height > mFullHeight)
    		{
    			android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams)mDoodleView.getLayoutParams();
    			y = params.topMargin;
    		}
    		dst.set(x, y, mFullWidth + x, mFullHeight + y);
    	}
    	canvas.drawBitmap(mFullPageBitmap, src, dst, paint);
    }
	//end smilefish
	
	/**
	 * 插入一个文本。该文本包含一个subject,一个链接
	 * @param subject
	 * @param link		
	 * @param positionY  插入位置的y坐标
	 */
	public void insertShareLink(String subject, String link, int positionY){
		positionY = positionY >= 0 ? positionY : 0;
		if(mTemplateType == MetaData.Template_type_todo){
			//pageEditor.gettem
			TemplateToDoUtility toDoUtility = getTemplateUtility().geTemplateToDoUtility();
			StringBuilder sb = new StringBuilder(subject);
			sb.append("\n").append(link);
			toDoUtility.AddNewTodoItem(sb.toString(),true, null);//每一个todo会新建一个NoteEditText,然后setText,这样子系统会自动设置超链接
		}else {
			insertShareLinkToPosition(mNoteEditText, subject, link, positionY);
		}
	}
	
	/**
	 * 将share Link插入到某个位置
	 * @author noah_zhang
	 * @param subject
	 * @param link
	 * @param positionY  插入位置的y坐标
	 */
	private void insertShareLinkToPosition(NoteEditText noteEditText, String subject, String link, int positionY){
		Log.i(TAG, "share link,positionY:" + positionY);
		Log.i(TAG, "share link,getLineHeight:" + noteEditText.getLineHeight());
		float linesF = (float)positionY / (float)noteEditText.getLineHeight();
		int lines = (int)Math.ceil(linesF) + 1;
		Log.i(TAG, "share link,lines:" + lines);
		
		int width = noteEditText.getNoteEditTextWidth();
		Editable editable = noteEditText.getText();
        for (int i = 0; i < lines; i++) {
        	noteEditText.append("\n");
        }
        int spaceWidth = (int) getPaint().measureText(" ");
		if(subject != null && subject != ""){
			int xOffset = 0;
			int textWidth = (int)noteEditText.getPaint().measureText(subject);
			if(textWidth < width){
				xOffset = (width - textWidth) / 2;
			}
	        for (int i = 0; i < xOffset/spaceWidth/4; i++) {
	        	editable.append(" ");
	        }
			String text = subject + "\n";
			editable.insert(editable.length(), text);
			Log.i(TAG, "share link,textWidth:" + textWidth);
			Log.i(TAG, "share link,getNoteEditTextWidth:" + width);
			float subjectLinesF = (float)textWidth / (float)width;
			int subjectLines = (int)Math.ceil(subjectLinesF);
			lines += subjectLines;
			Log.i(TAG, "share link,lines:" + lines);
		}
		if(link != null && link != ""){
			int textWidth = (int)noteEditText.getPaint().measureText(link);
			int xOffset = 0;
			if(textWidth < width){
				xOffset = (width - textWidth) / 2;
			}
	        for (int i = 0; i < xOffset/spaceWidth/4; i++) {
	        	editable.append(" ");
	        }
	        noteEditText.setText(editable);
			insertLink(noteEditText, editable.length(), link);
		}
	}
	
	/**
     * 插入一个超链接
     * @author noah_zhang
     * @param position
     * @param link
     */
    private void insertLink(EditText editText, int position, String link) {
		SpannableString ss = new SpannableString(link);
		int start = 0;
		int end = link.length();
		ss.setSpan(new URLSpan(link), start, end,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); 
		editText.getText().append(ss);
		editText.setMovementMethod(LinkMovementMethod.getInstance());
		
		//emmanual
		NoteForegroundColorItem colorItem = new NoteForegroundColorItem(Color.BLUE);
		int positionEnd = editText.getText().length();
		if (position > positionEnd) {
			position = positionEnd;
		}
		editText.getText().setSpan(colorItem, position, positionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
    
    //begin smilefish
	public void showShapeFailedToast()
	{
		mEditorUiUtility.showShapeFailedToast();
	}
	
	public void redrawDoodleView(){
		mDoodleView.redrawAll(true);
	}
	
    public void showVoiceInputButton(boolean flag){
    	if(mTemplateType == MetaData.Template_type_todo)
    		mTemplateUtility.showVoiceInputButton(flag);
    }
    
    public void setHandWriteViewEnable(){
    	if(mEditorUiUtility.getInputMode()==InputManager.INPUT_METHOD_SCRIBBLE && !mEditorUiUtility.isHandWriteViewEnable())
    		mEditorUiUtility.setHandWriteViewEnable(true);
    }
    //end smilefish
	// begin jason
	public void clearTemplayoutWindow(){
		if (mTemplateLinearLayout!=null) {
			mTemplateLinearLayout.setClearAirView();
		}
	}
	// end jason
	
	//add by mars_li for QC240582
    public void clickScrollBar(float x, float y){
        if(mNoteEditText != null){
            mNoteEditText.clickScrollBar(x, y, mScaleX, mScaleY);
        }
    }
    //end mars_li
}
