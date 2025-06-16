package com.asus.supernote.editable;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.InputManager;
import com.asus.supernote.R;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePageValue;
import com.asus.supernote.dialog.utils.DialogUtils;
import com.asus.supernote.editable.noteitem.NoteForegroundColorItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteImageItem;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteSendIntentItem;
import com.asus.supernote.editable.noteitem.NoteStringItem;
import com.asus.supernote.editable.noteitem.NoteTextStyleItem;
import com.asus.supernote.editable.noteitem.NoteTimestampItem;
import com.asus.supernote.inksearch.CFG;
import com.asus.supernote.picker.NoteBookPickerActivity;
import com.asus.supernote.picker.PickerUtility;
import com.asus.supernote.template.TemplateEditText;
import com.asus.supernote.ui.CursorIconLibrary;

public class NoteEditText extends EditText{

    // should be same as TextView.ASUS_SUPERNOTE_TAG in framework.
    private static final String ASUS_SPLIT_TEXT_TAG = "com.asus.splittext=";

    public static final int PHONEBOOK_IN_PAD_PORTRAIT_MARGIN_TOP = 144;

    private static final String TAG = "NoteEditText";

    public static final int NOTE_LINE_WIDTH = 1;
    public static final int NOTE_LINE_WIDTH_FOR_SHARE = 2;//darwin modify for fix TTbug 260228
    public static final int NOTE_GRID_LINE_WIDTH = 1;
    public static final int NOTE_GRID_SPACING = 13;
    public static final int NOTE_GRID_COLOR =  0xFFE4E2DB;
    // BEGIN: Shane_Wang 2012-10-9 issue 5
//    public static final int NOTE_LINE_COLOR = 0xFFE4E2DB;
    public static final int NOTE_LINE_COLOR = 0xFFB0AFAC;
    // END: Shane_Wang 2012-10-9
    public static final int NOTE_LEFT_LINE_COLOR = 0xFFF1AB8C;

    private static final int MY_HANDLE_LONG_CLICK = 1;
//    private static final int MY_DISPATCH_HANDLE_LONG_CLICK = 2;//RICHARD TEST
    private static final int MY_HANDLE_CURSOT_POS_XY = 1;
    private static final int MY_HANDLE_CURSOT_POS_X = 2;
    private static final int MY_HANDLE_CURSOT_POS_Y = 3;
    
    //Begin: Dave. To add text selector.
    public int SELECTION_LEFT_ARROW_WIDTH = (int)getResources().getDimension(R.dimen.left_handle_width);
    public int SELECTION_RIGHT_ARROW_WIDTH = (int)getResources().getDimension(R.dimen.right_handle_width);
    public int SELECTION_HANDLE_HEIGHT = (int)getResources().getDimension(R.dimen.selector_handle_height);
    
    private int mleftSelectionOffset = -1;
    private int mRightSelectionOffset = -1;
    //End: Dave.

    private int EDITTEXT_WIDTH_PHONE = 0;

    //Begin : Darwin_yu@asus.com
    //Modified reason : multi-DPI support
    //private int mTureWidth = 0;
    private int mEditWidthPhone = 0;
    //private int ORIGIN_PAD_WIDTH = 0;
    //End   : Darwin_yu@asus.com

	 //BEGIN:shaun_xu@asus.com
    private int EDITTEXT_WIDTH_PAD = 0;
    //END:shaun_xu@asus.com
    public static final float TEMPLETE_TODO_LINE_SPACE = 1.20f; //smilefish

    private static final long LONG_CLICK_TIME = LongClickDetector.LONG_CLICK_TIMER;
    public static final double TOUCH_TOLERANCE = 8.0;

    private static final String OBJ = String.valueOf((char) 65532);

    private boolean mChangeBecauseUnRedo = false;
    private boolean mIsEnable = true;
    private boolean mIsLongClick = false;
    private int mDownX = 0;
    private int mDownY = 0;
    private int mOldCursorOffset = -1;

    private boolean mChangeBecauseAutoRecgnizer = false;//Richard

    protected float mFontSize = 0;//RICHARD int to float
    protected int mLineCountLimited = 0;
    protected boolean mIsSmallScreen = false;

    protected int mWidth = 0;
    protected int mHeight = 0;
    protected int mEditorPageHeight = 0;//Allen
	protected int mScreenWidth = -1;

    private boolean mAllowSystemScrollTo = false;

    protected PageEditor mPageEditor = null;
    private final NoteEditTextHistory mHistory = new NoteEditTextHistory();

    // for draw line
    private Rect mLineBound = new Rect();
    private int mBaseLine = -1;
    private Paint mLinePaint = null;

    protected boolean mIsFirstTimeLoad = false;

    private boolean mIsUsingSetColorOrStyle = true;

    // BEGIN: archie_huang@asus.com
    // To fix TT-218753
    private boolean mIsLayoutReady = false;
    private Editable mLoadedEditable = null;
    private int mLoadedPos = 0;//wendy
    // END: archie_huang@asus.com

    //BEGIN: RICHARD
    private float mAutoJumpDistance = 0;
    //END: RICHARD
    // BEGIN: archie_huang@asus.com
    // To fix TT-223986
    private InputConnection mInputConnection = null;
    // END: archie_huang@asus.com
    
    //BEGIN: RICHARD
    private int mLineHeight = 0;
    //END: RICHARD
 
    private String itemName = null; // by show
    private NoteSendIntentItem noteSendIntentItem = null;//by show
    
    //Begin Allen
    protected boolean mIsModified = false;
	protected short mContentType = NoteItemArray.TEMPLATE_CONTENT_DEFAULT;
	private boolean isBaseLineMode = false;
	
	protected float mLineSpace = 1.20f; //smilefish
	
	//Begin: show_wang@asus.com
	//Modified reason: for dds
	private boolean mIsFirstTextChange = true; 
	public void setFirstTextChange(boolean isFirstTextChange) {
		mIsFirstTextChange = isFirstTextChange;		
	}
	
	public void setContentType(short contentType) {
		this.mContentType = contentType;		
	}

	public short getContentType() {
		return mContentType;
	}
	
    public boolean isModified() {
    	Log.i(MetaData.DEBUG_TAG,"noteEditText is modify " + mIsModified);
		return mIsModified;
	}

	public void setIsModified(boolean mIsModified) {
		Log.i(MetaData.DEBUG_TAG,"set noteEditText " + mIsModified);
		this.mIsModified = mIsModified;
		mPageEditor.onModified(mIsModified);
	}
	//End Allen
	
	public boolean isLayoutReady() {
		return mIsLayoutReady;
	}

    public int getEditorPageHeight() {
		return mEditorPageHeight;
	}

    public NoteEditText(Context context) {
        super(context);
        
        mLineSpace = PickerUtility.getLineSpace(context); //smilefish
        
        //Begin Allen++ for 1080dp
        EDITTEXT_WIDTH_PAD = (int)NotePageValue.getPadBookWidth(context);//mContext.getResources().getInteger(R.integer.edittext_width_pad)* MetaData.DPI / MetaData.BASE_DPI;
        EDITTEXT_WIDTH_PHONE = (int)NotePageValue.getPhoneBookWidth(context);//mContext.getResources().getInteger(R.integer.edittext_width_phone)* MetaData.DPI / MetaData.BASE_DPI;
        //End Allen
        
        mEditWidthPhone = EDITTEXT_WIDTH_PHONE;// * (mTureWidth) / (ORIGIN_PAD_WIDTH);
        
        //EDITTEXT_WIDTH_PAD = (1280 - 58 - 58) * MetaData.DPI / MetaData.BASE_DPI;//darwin
        //End   : Darwin_yu@asus.com
		setPrivateImeOptions(ASUS_SPLIT_TEXT_TAG + OBJ);
		setInputType(getInputType() | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); //emmanual_chen google键盘输入自动补全
        setAutoLinkMask(Linkify.WEB_URLS);//noah:默认可以点开网络链接
        setLinkTextColor(getResources().getColor(R.color.noteeditext_link_default_color));//noah

        MetaData.checkRTL(this);
    }

    public NoteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mLineSpace = PickerUtility.getLineSpace(context); //smilefish
        
        //Begin Allen++ for 1080dp
        EDITTEXT_WIDTH_PAD = (int)NotePageValue.getPadBookWidth(context);//mContext.getResources().getInteger(R.integer.edittext_width_pad)* MetaData.DPI / MetaData.BASE_DPI;
        EDITTEXT_WIDTH_PHONE = (int)NotePageValue.getPhoneBookWidth(context);//mContext.getResources().getInteger(R.integer.edittext_width_phone)* MetaData.DPI / MetaData.BASE_DPI;
        //End Allen
        
        mEditWidthPhone = EDITTEXT_WIDTH_PHONE;// * (mTureWidth) / (ORIGIN_PAD_WIDTH);        

        setPrivateImeOptions(ASUS_SPLIT_TEXT_TAG + OBJ);
		setInputType(getInputType() | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); //emmanual_chen google键盘输入自动补全
        setAutoLinkMask(Linkify.WEB_URLS);//noah:默认可以点开网络链接
        setLinkTextColor(getResources().getColor(R.color.noteeditext_link_default_color));//noah

        MetaData.checkRTL(this);
    }


    // BEGIN: archie_huang@asus.com
    // To fix TT-218753
    // Because some [editable] will cause TextView generating wrong mLayout,
    // We need to set such [editable] after mLayout is generated correctly.
    // ps. mLayout is a member of TextView
    public void load(Editable editable) {
        if (mIsLayoutReady) {
            setText(editable);
			//Begin Darwin_Yu@asus.com
        	mPageEditor.m_ItemListInit.clear();
        	mPageEditor.getItemList(mPageEditor.m_ItemListInit);
            //End   Darwin_Yu@asus.com
        }
        else {
            mLoadedEditable = editable;
        }
    }// END: archie_huang@asus.com
    //begin wendy
    
    
    public NoteItemArray getNoteItem(int start, int end) {
        Editable editable = (Editable) getText().subSequence(start, end);
        
        // BEGIN: Shane_Wang@asus.com 2013-3-5
        //for getting the final string length:
        String editTextString = getTextWithoutTailSpacing();
        NoteItem stringItem = new NoteStringItem(editTextString);
        // END: Shane_Wang@asus.com 2013-3-5
        
        // span part
        NoteItem[] spanItems = editable.getSpans(0, editable.length(), NoteItem.class);
        NoteItem[] allnoteItem = new NoteItem[spanItems.length + 1];

        allnoteItem[0] = stringItem;
        for (int i = 0; i < spanItems.length; i++) {
            int spanstart = editable.getSpanStart(spanItems[i]);
            int spanend = editable.getSpanEnd(spanItems[i]);
            
            // BEGIN: Shane_Wang@asus.com 2013-3-5
            //if length of span is longer than editText.Cut span to fit the text:
            if(spanend > editTextString.length()) {
            	spanend = editTextString.length();
            }
            // END: Shane_Wang@asus.com 2013-3-5
            
            spanItems[i].setStart(spanstart);
            spanItems[i].setEnd(spanend);
            allnoteItem[i + 1] = spanItems[i];
        }
        ArrayList<NoteItem> noteItemsArrayList = new ArrayList<NoteItem>();
        for(NoteItem item:allnoteItem){
        	noteItemsArrayList.add(item);
        }
        NoteItemArray noteItemArray = new NoteItemArray(noteItemsArrayList,NoteItemArray.TEMPLATE_CONTENT_DEFAULT_NOTE_EDITTEXT);
        return noteItemArray;
    }
    
    public void setSelectionEx(int pos)
    {
    	mLoadedPos = pos;
    }

    protected void onSuperMeasure(int widthMeasureSpec, int heightMeasureSpec){
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	
    	// BEGIN: Better
        replaceChangeWatcher(getText());
        // END: Better
    }
    
    // BEGIN: Better
    protected void replaceChangeWatcher(Editable text) {
    	if (text != null) {
        	SpanWatcher[] watchers = text.getSpans(0, text.length(), SpanWatcher.class);
	        if (watchers != null) {
	        	for (SpanWatcher watcher : watchers) {
	        		if (!(watcher instanceof NoteChangeWatcher)) {
	        			int flag = text.getSpanFlags(watcher);
	        			text.removeSpan(watcher);
	        			if (watcher instanceof TextWatcher) {
	        				text.setSpan(new NoteTextWatcher(watcher), 0, text.length(), flag);
	        			} else {
	        				text.setSpan(new NoteSpanWatcher(watcher), 0, text.length(), flag);
	        			}
	        		}
	        	}
	        }
        }
    }
    
    @Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
		
		replaceChangeWatcher(getText());
	}
    // END: Better
    
    // BEGIN: archie_huang@asus.com
    // To fix TT-218753
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        // BEGIN: Better
        replaceChangeWatcher(getText());
        // END: Better
        
        if (mPageEditor != null) {
	        if (!mIsLayoutReady && getLayout() != null) {
	            mIsLayoutReady = true;
	            if (mLoadedEditable != null) {
	                mIsFirstTimeLoad = true;
	                setText(mLoadedEditable);
	                mIsFirstTimeLoad = false;
	                setSelection(mLoadedEditable.length());
	                clearUndoRedoStack();
	                mLoadedEditable = null;
					//begin wendy
	           		 if(mLoadedPos < getText().length())
	            		setSelection(mLoadedPos);
	            	mLoadedPos = 0;
	            	//end wendy 
					//Begin Darwin_Yu@asus.com
	            	mPageEditor.m_ItemListInit.clear();
	            	mPageEditor.getItemList(mPageEditor.m_ItemListInit);
	                //End   Darwin_Yu@asus.com
	            }
	        }
        }
    } // END: archie_huang@asus.com

    boolean checkKeyboardShow = false;//emmanual
    public void initNoteEditText(PageEditor pageEditor, int textSize, boolean isSmallScreen) {
        mPageEditor = pageEditor;
        mIsSmallScreen = isSmallScreen;
        mScreenWidth = mPageEditor.getScreenWidth();

        updateFontSizeFromResource(textSize, isSmallScreen);
        caculateWidthAndHeight();
        addTextChangedListener(new MyTextWatcher());
        
        checkSelectionMode();
    }

    //Begin Allen
    public void ResetHeight(int height){
        setPadding(0, 0, 0, 0);
        getLayoutParams().height = height;
        mHeight = height;
    }
    //End Allen
    
    // BEGIN: archie_huang@asus.com
    public boolean isNoteEditTextEnable() {
        return mIsEnable;
    } // END: archie_huang@asus.com

    protected void updateFontSizeFromResource(int textSize, boolean isSmallScreen) {
        int firstLineHeight = 0;//RICHARD Just for padding Top
        
        //BEGIN: RICHARD
        firstLineHeight = NotePageValue.getPaddingTop(getContext(), isSmallScreen);
        mFontSize = NotePageValue.getFontSize(getContext(), textSize, isSmallScreen);
        if(mContentType!=NoteItemArray.CONTENT_BOXEDIT){
        	mLineCountLimited = NotePageValue.getLineCountLimited(getContext(), textSize, isSmallScreen);
        }
        //END: RICHARD
        
        
        setTextSize(TypedValue.COMPLEX_UNIT_PX, mFontSize);
        setLineSpacing(0, mLineSpace); //smilefish
        setPadding(
                getPaddingLeft(),
                firstLineHeight,
                getPaddingRight(),
                0);
        // BEGIN: archie_huang@asus.com
        // setLineSpacing() && setPadding() will regenerate mLayout later
        mIsLayoutReady = false;
        // END: archie_huang@asus.com
    }
    
    protected void updateTemplateFontSizeFromResource(int textSize, boolean isSmallScreen) {
        
    	//BEGIN: RICHARD
        mFontSize = NotePageValue.getFontSize(getContext(), textSize, isSmallScreen);
        mLineCountLimited = NotePageValue.getLineCountLimited(getContext(), textSize, isSmallScreen);
        //END: RICHARD
        	setTextSize(TypedValue.COMPLEX_UNIT_PX, mFontSize);

        // BEGIN: archie_huang@asus.com
        // setLineSpacing() && setPadding() will regenerate mLayout later
        mIsLayoutReady = false;
        // END: archie_huang@asus.com
    }

    protected void caculateWidthAndHeight() {
        if(mIsSmallScreen)
        {
        	mWidth = mEditWidthPhone;
        }else
        {
        	mWidth = EDITTEXT_WIDTH_PAD;
        }
		mHeight = getLineHeight() * mLineCountLimited + getPaddingTop();
        mEditorPageHeight = mHeight;
        if (mPageEditor.getDeviceType()>100 && mIsSmallScreen) {
        	//Begin : Darwin_yu@asus.com
            //Modified reason : multi-DPI support
        	mWidth = mEditWidthPhone;
            //End   : Darwin_yu@asus.com
            setPadding(0, getPaddingTop(), 0, getPaddingBottom());
            getLayoutParams().width = mWidth;//Allen
        }
        if (!(mPageEditor.getDeviceType()>100) && !mIsSmallScreen) {
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom()); //smilefish
            LinearLayout.LayoutParams linearLayoutParams = null;
            linearLayoutParams = new LinearLayout.LayoutParams(getLayoutParams());
            linearLayoutParams.gravity = Gravity.START;
            setLayoutParams(linearLayoutParams);
            mWidth = EDITTEXT_WIDTH_PAD;           
            getLayoutParams().width = mWidth;                
            
            
        }

        // BEGIN: archie_huang@asus.com
        // setPadding() will regenerate mLayout later
        mIsLayoutReady = false;
        // END: archie_huang@asus.com
    }

    public void setLineCountLimited(int lineCountLimited) {
        mLineCountLimited = lineCountLimited;
    }

    public int getLineCountLimited() {
        return mLineCountLimited;
    }

    public float getFontSize() {
        return mFontSize;
    }

    //BEGIN: RICHARD
    public int getOrignalEditTextWidth()
    {
    	return mWidth;
    }
    
    public int getOrignalEditTextHeight()
    {
    	return mHeight;    	
    }

    public int getOrignalPageHeight(){
    	return mEditorPageHeight;
    }
    
    public int getNoteEditTextWidth() {
        return (int) (mWidth * mPageEditor.getScaleX());
    }

    public int getNoteEditTextHeight() {
        return (int) (mHeight * mPageEditor.getScaleY());
    }

    public void setNoteEditTextEnabled(boolean enable) {
        mIsEnable = enable;
    }
    //END: RICHARD
    protected void checkSelectionMode() {
        if (mPageEditor == null) {
            return;
        }

        if (getSelectionStart() == getSelectionEnd()) {
            mPageEditor.quitSelectionTextMode();
        }
        else {
            mPageEditor.switchToSelectionTextMode();
        }
    }

    public void onCheckClickableItem(MotionEvent event) {
        float layoutOffset = (mScreenWidth - getWidth()) / 2;
        layoutOffset = layoutOffset > 0 ? layoutOffset : 0;
        int x = (int) ((event.getX() - layoutOffset) / mPageEditor.getScaleX());
        int y = (int) ((event.getY()) / mPageEditor.getScaleY());

        checkAndStartClickableSpan(x, y, event, true);
    }

    // BEGIN: archie_huang@asus.com
    // To fix TT-223986
    // super.setText() + setPadding() will cause this bug
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (mInputConnection == null) {
            mInputConnection = super.onCreateInputConnection(outAttrs);
        }
        // BEGIN: ryan_lin@asus.com
        // To fix TT-227049
        else {
            super.onCreateInputConnection(outAttrs);
        }
        // END: ryan_lin@asus.com
        return mInputConnection;
    } // END: archie_huang@asus.com

    public boolean checkAndStartClickableSpan(int x, int y, MotionEvent event, boolean flag) {
        // BEGIN chilung_chen@asus.com
    	if ( getLayout() == null ) {//noah 将判断移动到该位�?        	
    		return false;
        }
    	//emmanual to fix bug 445841
		if (mPageEditor.getEditorUiUtility().getInputMode() == InputManager.INPUT_METHOD_DOODLE) {
			if (mContentType == NoteItemArray.CONTENT_NOTEEDIT) {
				int offset = mPageEditor.getTemplateLayoutScaleHeight()
				        - mPageEditor.getScrollY();
				if (offset < 0) {
					offset = 0;
				}
				y = y - offset;
			} else {
				int[] location = new int[2];
				getLocationInWindow(location);
				Rect frame = new Rect();
				((Activity) this.getContext()).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
				y = (int) (y - location[1] + getResources().getDimension(
				        R.dimen.edit_func_bar_height))
				        + frame.top;
			}
    	}
        x -= getTotalPaddingLeft();
    	//emmanual to fix bug 445841
        if (mContentType == NoteItemArray.CONTENT_NOTEEDIT) {
	        try{ //smilefish fix bug 404046
	        	y -= getTotalPaddingTop();
	        }catch(Exception e){
				e.printStackTrace();
			}
        }
        x += getScrollX();
        y += getScrollY();
        int line = getLayout().getLineForVertical(y);
        Rect rect = new Rect();
        getLayout().getLineBounds(line, rect);
        boolean isStart = false;
        if (rect.contains(x, y) && x <= getLayout().getLineWidth(line)) {
            isStart = startClickableSpan(x, y, flag);//by show
        }
        // END chilung_chen@asus.com
        return isStart;
    }

    private boolean startClickableSpan(int x, int y, boolean isClick) { //by show
        Editable buffer = getText();
        Layout layout = getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);
        // Log.d("Ryann", "off = " + off);        
        //Begin Allen
        if(x < layout.getPrimaryHorizontal(off)){
        	off--;
        }
        //End Allen
        ClickableSpan[] link = buffer.getSpans(off, off+1, ClickableSpan.class);
        if (link.length != 0) {
		//Begin: show_wang@asus.com
		//Modified reason: for airview
        	if (isClick) {
        		link[0].onClick(this);
        	} else if(MethodUtils.isEnableAirview(getContext())){
        		NoteSendIntentItem[] item = buffer.getSpans(off, off+1, NoteSendIntentItem.class);
        		if (item.length != 0) {
        			itemName = item[0].getFileName();
        			noteSendIntentItem = item[0];
        		}
        	}
		//End: show_wang@asus.com
    		return true;
        }
        return false;
    }
    
	@Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
	    // TODO Auto-generated method stub
		if (MethodUtils.isEnableAirview(getContext())) {
			CursorIconLibrary.setLastStylusIcon(this);
		}
	    return super.dispatchHoverEvent(event);
    }

	//Begin: show_wang@asus.com
	//Modified reason: for airview
    public String getNoteSendItentItemName () {
    	return itemName;
    }
    
    public NoteSendIntentItem getNoteSendIntentItem () {
    	return noteSendIntentItem;
    }
	//End: show_wang@asus.com

    private int getCursorYPos(int pos) {
        Layout layout = getLayout();
        if (layout == null) {
            return -1;
        }
        int line = layout.getLineForOffset(pos);
        int baseline = layout.getLineBaseline(line);
        int ascent = layout.getLineAscent(line);

        return baseline + ascent;
    }

    protected int getCursorXPos(int pos) {
        Layout layout = getLayout();
        if (layout == null) {
            return -1;
        }

        return (int) layout.getPrimaryHorizontal(pos);
    }

    public void bringCursorIntoPoint(int x, int y) {
        // Log.d("Ryan", "-----------------------------------");
        int cursorPos = getOffsetForPosition(x, y);
        // Log.d("Ryan", "---bringCursorIntoPoint x = " + x + " y = " + y + " cursorPos = " + cursorPos);
        int realX = x + getScrollX();
        int realY = y + getScrollY();
        setSelection(cursorPos);
        if (mMyScrollCorrecterHandler != null) {
            mMyScrollCorrecterHandler.removeMessages(MY_HANDLE_CURSOT_POS_XY);
        }
        // Log.d("Ryan", "---getScrollY() = " + getScrollY());
        // Log.d("Ryan", "---realX = " + realX + " realY = " + realY);
        int addLineCount = 0;
        int addSpaceCount = 0;
        int lineHeight = getLineHeight();
        int spaceWidth = (int) getPaint().measureText(" ");
        // Log.d("Ryan", "---lineHeight = " + lineHeight);
        int curXPos = getCursorXPos(cursorPos);
        int curYPos = getCursorYPos(cursorPos) + lineHeight / 2;

        //BEGIN emmanual
        int[] location = new int[2]; //get location in editText layout.
        getLocationOnScreen(location);
    	
    	int[] location1 = new int[2];
    	mPageEditor.getLayout().getLocationOnScreen(location1); //get location in NoteFrameLayout.
    	int offsetX = location[0] - location1[0];
    	int offsetY = location[1] - location1[1];
    	
        //Dave.2013.8.13. Fix the bug: the selector posision in Memo/meeting mode is wrong.
        int leftCursorPosX = getCursorXPos(cursorPos) - getScrollX();
        int leftCursorPosY = getCursorYPos(cursorPos) - getScrollY() + SELECTION_HANDLE_HEIGHT;
        
        leftCursorPosX = (int)(leftCursorPosX * mPageEditor.getScaleX()) + offsetX  + getPaddingLeft();
        leftCursorPosY = (int)(leftCursorPosY * mPageEditor.getScaleY()) + offsetY;
		
        // Log.d("Ryan", "---getCurX = " + curXPos + " getCurY = " + curYPos);

        final int yOffset = realY - curYPos;
        final int xOffset = realX - curXPos;

        //emmanual, move code here to fix bug 394414
		if(mPageEditor.getEditorUiUtility().getInputMode() != InputManager.INPUT_METHOD_DOODLE){
			((EditorActivity) (mPageEditor.getEditorUiUtility().getContext()))
			        .showEditTextPopMenu(this, leftCursorPosX-getPaddingLeft(), leftCursorPosY, false);
		}
		
        if (yOffset <= lineHeight && xOffset <= spaceWidth) {
            return;
        }
        if (yOffset > lineHeight) {
            addLineCount = yOffset / lineHeight;
        }
        boolean haveNewLine = addLineCount > 0;
        boolean isEndOfThisLine = false;
        if(cursorPos >= 0 && getText().length() > 0){//cursorPos有可能为-1;noah  //Dave.0902.Fix the bug:292375.
	        isEndOfThisLine = (cursorPos + 1 <= getText().length() && getText().subSequence(cursorPos, cursorPos + 1).toString().equals("\n"))
	                || cursorPos == getText().length();
        }
        // Log.d("Ryan", "---isEndOfThisLine = " + isEndOfThisLine);
        if ((xOffset > spaceWidth && isEndOfThisLine) || haveNewLine) {
            int xStartPos = haveNewLine ? 0 : curXPos;
            addSpaceCount = (realX - xStartPos) / spaceWidth;
            // Log.d("Ryan", "---xStartPos = " + xStartPos);
        }
        // Log.d("Ryan", "---addLineCount = " + addLineCount);
        // Log.d("Ryan", "---addSpaceCount = " + addSpaceCount);

        if(getId() != R.id.attendee_edit){
	        StringBuilder strBuilder = new StringBuilder();
	        for (int i = 0; i < addLineCount; i++) {
	            strBuilder.append("\n");
	        }
	        for (int i = 0; i < addSpaceCount; i++) {
	            strBuilder.append(" ");
	        }
	
	        if (haveNewLine) {
	            getText().append(strBuilder);
	        }
	        else {
	            getText().insert(getSelectionEnd(), strBuilder);
	        }
        }
        
        mIsSelectionChangeButNoteReScroll = true;
        mOldCursorOffset = getOffsetForPosition(x, y);
        setSelection(mOldCursorOffset);
		// END emmanual
        // Log.d("Ryan", "---mOldOffset = " + mOldCursorOffset);
    }
    
    //emmanual
    private boolean isClickOnDrawItem(){
		if (mContentType == NoteItemArray.CONTENT_NOTEEDIT) {
			int offset = mPageEditor.getTemplateLayoutScaleHeight()
			        - mPageEditor.getScrollY();
			if (offset < 0) {
				offset = 0;
			}
			return mPageEditor.getDoodleView().IsClickOnDrawItem(
			        (int) (mDownX * mPageEditor.getScaleX()),
			        (int) ((mDownY + offset) * mPageEditor.getScaleY())
			                + mPageEditor.getScrollY());
		} else {
			int[] location = new int[2];
			getLocationInWindow(location);
			return mPageEditor.getDoodleView().IsClickOnDrawItem(
			        (int) ((mDownX + location[0]) * mPageEditor.getScaleX()),
			        (int) ((mDownY + location[1] - getResources().getDimension(
			                R.dimen.edit_func_bar_height)) * mPageEditor
			                .getScaleY()) + mPageEditor.getScrollY());
		}
    }

    private Handler mMyLongClickHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MY_HANDLE_LONG_CLICK:               	
                	int offset = mPageEditor.getTemplateLayoutScaleHeight() - mPageEditor.getScrollY();
                	if (offset < 0) {
                		offset = 0;
                	}
                	if(mContentType == NoteItemArray.CONTENT_BOXEDIT ||
                			!isClickOnDrawItem()){ //RICHARD 2013/1/7
	                    mPageEditor.showSelectionTextHint(true);
	                    mIsLongClick = true;
						if (mContentType == NoteItemArray.CONTENT_BOXEDIT
								|| mContentType == NoteItemArray.TEMPLATE_CONTENT_MEETING_TOPIC
								|| mContentType == NoteItemArray.TEMPLATE_CONTENT_MEETING_ATTENDEE
								|| mContentType == NoteItemArray.TEMPLATE_CONTENT_TRAVEL_TITLE) {
	                    	requestFocus();
	                    }
	                    else
	                    {
	                    	mPageEditor.setNoteEditTextRequestFocus();//Allen
	                    }
	                    bringCursorIntoPoint(mDownX, mDownY);
                	}
                	break;
                default:
                    super.handleMessage(msg);
            }
        }

    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
    	// begin jason : is Stylus input only? yes: intercept the event that is not the  TOOL_TYPE_STYLUS this is for BoxEdit
    	if(mContentType == NoteItemArray.CONTENT_BOXEDIT)
		{
    		if ((!mPageEditor.isDispatchByHandWritingViewEvent())&&MethodUtils.isPenDevice(getContext())) { //smilefish
    			if((mPageEditor.getEditorUiUtility().getmIsStylusInputOnly())){
    				if (event.getToolType(event.getActionIndex())!=MotionEvent.TOOL_TYPE_STYLUS ) {
    					return false;
    				}
    			}
    		}
        	mPageEditor.setIsDispatchByHandWritingViewEvent(false);
		}
    	
    	//emmanual
    	if ((mPageEditor.getEditorUiUtility().getInputMode() == InputManager.INPUT_METHOD_KEYBOARD
    			|| mPageEditor.getEditorUiUtility().getInputMode() == InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD)
		        && !mPageEditor.isReadOnlyMode()) {
    		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    		if(imm != null && imm.getCurrentInputMethodSubtype() != null
    				&& imm.getCurrentInputMethodSubtype().getMode().contains("voice")){
	    		mPageEditor.hiddenSoftKeyboard();
				DialogUtils.showSoftInput(this);
    		}
		}
    	
    	// end jason
    	// BEGIN锟斤�?Shane_Wang@asus.com 2012-12-7
    	if(getLayout() == null) {
    		return false;
    	}
    	
//    	onTouchEvent(event);
    	
    	// END锟斤�?Shane_Wang@asus.com 2012-12-7
        if (!mIsEnable) {
        	if(event.getDownTime() == 1-event.getEventTime()) {
        		//READ ONLY MODE, CLICK CLICKABLESPAN
        		if(event.getAction() == MotionEvent.ACTION_UP )
        		{
        			int x = (int) event.getX();
                	int y = (int) event.getY();
                	checkAndStartClickableSpan(x, y, event, true);
        		}
                return true;
            }
            return false;
        }
        
        int x = (int) event.getX();
        int y = (int) event.getY();
        
        //Begin: 0807. Dave. To fix a bug: the memo mode can not receive touch event from selector.
        if(mPageEditor.editSelectionLeftSelected || mPageEditor.editSelectionRightSelected)
        {
        	int offsetX = 0;
        	int offsetY = 0;

            int[] location = new int[2]; //get location in editText layout.
            getLocationOnScreen(location);
        	
        	int[] location1 = new int[2];
        	mPageEditor.getLayout().getLocationOnScreen(location1); //get location in NoteFrameLayout.

        	offsetX = location[0] - location1[0];
        	offsetY = location[1]; //Dave.fix Bug324129
        	
        	x -= offsetX;
        	y -= offsetY;
        	
        	y -= SELECTION_HANDLE_HEIGHT;
        	
        	if(y < 0)
        		y = 0;
        }
        //End.0807.Dave.
        //add by mars_li for QC240582
        int offset = getRellyOffset(x, y);
        //end 
    	switch(event.getAction()&MotionEvent.ACTION_MASK)
    	{
    	case MotionEvent.ACTION_DOWN:
    		
    		//Begin:Dave. To add text selecotr.
			if (mPageEditor.editSelectionLeftSelected|| mPageEditor.editSelectionRightSelected) 
			{
				mIsLongClick = true;
				requestFocus();
				//0813.Fix a bug: the selector will not function when rotated.
                SharedPreferences preferences = ((Activity)getContext()).getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE);
				mOldCursorOffset = preferences.getInt(MetaData.PREFERENCE_SELECTION_TEXT_START, mOldCursorOffset);
			}
			//End:Dave.
			else 
			{
				mIsLongClick = false;
				// BEGIN: RICHARD TEST
				requestFocus();
				// END: RICHARD
				mOldCursorOffset = offset;

				mDownX = x;
				mDownY = y;
				if (event.getDownTime() != -event.getEventTime()) {
					mMyLongClickHandler.sendEmptyMessageDelayed(
							MY_HANDLE_LONG_CLICK, LONG_CLICK_TIME);
				} else {
					mMyLongClickHandler.sendEmptyMessage(MY_HANDLE_LONG_CLICK);
				}
				mPageEditor.quitSelectionTextMode();// darwin
			}
    		break;
    		
    	case MotionEvent.ACTION_POINTER_DOWN:
    		mMyLongClickHandler.removeMessages(MY_HANDLE_LONG_CLICK);
    		mIsLongClick = false;
    		break;
    	case MotionEvent.ACTION_MOVE:
    		  if (!mIsLongClick) {
	                double nowX = x;
	                double nowY = y;
	
	                double distanceX = mDownX - nowX;
	                double distanceY = mDownY - nowY;
	                if (Math.abs(distanceX) > TOUCH_TOLERANCE
	                        || Math.abs(distanceY) > TOUCH_TOLERANCE) {
	
	                    mMyLongClickHandler.removeMessages(MY_HANDLE_LONG_CLICK);
	                }
	                if (!mMyLongClickHandler.hasMessages(MY_HANDLE_LONG_CLICK)) {
	                    mOldCursorOffset = offset;
	                    mDownX = x;
	                    mDownY = y;
	                }
    		  }
    		  else
    		  {
                  if (mOldCursorOffset != -1 && mOldCursorOffset <= getText().length() && offset <= getText().length()) {
                	  //Begin:Dave. To add text selecotr.
                	  if(mPageEditor.editSelectionLeftSelected)
                	  {
                		  setSelection(offset, mRightSelectionOffset);
                	  }else if(mPageEditor.editSelectionRightSelected)
                	  {
                		  setSelection(mleftSelectionOffset, offset);
                	  }else {
                		  setSelection(mOldCursorOffset, offset);
                	  }
                	  //End:Dave.
                  }
    		  }
    		break;
    	case MotionEvent.ACTION_UP:
    		if (!mIsLongClick) {    	        
                mMyLongClickHandler.removeMessages(MY_HANDLE_LONG_CLICK);
                boolean startedClickable = checkAndStartClickableSpan(x, y, event, true);
                if (!startedClickable) {
                    setSelection(offset);
                    mPageEditor.setHandWriteViewEnable();//smilefish
                    mPageEditor.showSoftKeyboard();
                }else{
                	mPageEditor.hiddenSoftKeyboard();
                	mPageEditor.getEditorUiUtility().setHandWriteViewEnable(false);
                }
                mOldCursorOffset = -1;
                mPageEditor.quitSelectionTextMode();
                requestFocus();//Allen
    		}else
    		{
                if (mOldCursorOffset != -1 && mOldCursorOffset <= getText().length() && offset <= getText().length()) 
                {
                	//begin smilefish fix bug 404609/403202
                	int textStart = 0;
                	int textEnd = 0;
                	
                	  //Begin:Dave. To add text selecotr.
                	  if(mPageEditor.editSelectionLeftSelected)
                	  {
                		  setSelection(offset, mRightSelectionOffset);
                		  textStart = offset;
                		  textEnd = mRightSelectionOffset;
                	  }else if(mPageEditor.editSelectionRightSelected)
                	  {
                		  setSelection(mleftSelectionOffset, offset);
                		  textStart = mleftSelectionOffset;
                		  textEnd = offset;
                	  }else {
                		  setSelection(mOldCursorOffset, offset);
                		  textStart = mOldCursorOffset;
                		  textEnd = offset;
                	  }
                	  //End:Dave.
              	  
                    //Begin: show_wang@asus.com
                    //Modified reason: for dds
                    SharedPreferences preferences = ((Activity)getContext()).getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE);
                    preferences.edit().putInt(MetaData.PREFERENCE_SELECTION_TEXT_START, textStart).commit();
                    preferences.edit().putInt(MetaData.PREFERENCE_SELECTION_TEXT_END, textEnd).commit();
                    //end smilefish
                    
                    //Begin:Dave. To add text selecotr.
                    
                    if(mPageEditor.editSelectionLeftSelected || mPageEditor.editSelectionRightSelected)
                    {
                    	mPageEditor.editSelectionLeftSelected = false;
                    	mPageEditor.editSelectionRightSelected = false;
                    }else {
  						mOldCursorOffset = -1;
  					}
                    //End:Dave.
                    
                    checkSelectionMode();
                    if (getSelectionEnd() == getSelectionStart() && mPageEditor != null) {
                        mPageEditor.showSelectionTextHint(false);
                    }
                }
    		}
    		break;
    	}
        return true;
    }
    
    //add by mars_li for QC240582
    public void clickScrollBar(float x, float y, float scaleX, float scaleY){
        int[] location = new int[2];  
        this.getLocationOnScreen(location);
        x = (x - location[0])/scaleX;  
        y = (y - location[1])/scaleY; 
        int offset = getRellyOffset(x, y);
        setSelection(offset);
    }
    
    int getRellyOffset(float x, float y){
        int offset = getOffsetForPosition(x, y);
        //add by mars_li for QC240582
        int width = this.getWidth();
        if(x>=width-30){ //30 is test value
            offset += 1;
        }
        return offset;
    }
    //end mars_li

    private boolean mIsPressCtrl = false;

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (!mIsEnable && (
                (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) ||
                        (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_PERIOD) ||
                        keyCode == KeyEvent.KEYCODE_SPACE ||
                        keyCode == KeyEvent.KEYCODE_ENTER ||
                        keyCode == KeyEvent.KEYCODE_DEL ||
                        (keyCode >= KeyEvent.KEYCODE_GRAVE && keyCode <= KeyEvent.KEYCODE_AT) ||
                (keyCode >= KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_RIGHT_PAREN)
                )) {
            return true;
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_TAB) {
            int st = Math.min(getSelectionStart(), getSelectionEnd());
            int en = Math.max(getSelectionStart(), getSelectionEnd());
            getText().replace(st, en, "\t");
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP
                || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            checkSelectionMode();
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && (keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT)) {
            mIsPressCtrl = true;
        }
        if (event.getAction() == KeyEvent.ACTION_UP
                && (keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT)) {
            mIsPressCtrl = false;
        }
        if (mIsPressCtrl && event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_C) {
            mPageEditor.copyTheSelectionText(
            		((EditorActivity)(mPageEditor.getEditorUiUtility().getContext())).getNotePage()); // Better
            return true;
        }
        if (mIsPressCtrl && event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_X) {
            mPageEditor.copyTheSelectionText(
            		((EditorActivity)(mPageEditor.getEditorUiUtility().getContext())).getNotePage()); // Better
            mPageEditor.insertBackSpace();
            return true;
        }
        if (mIsPressCtrl && event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_V) {
            mPageEditor.pastToNoteEditText(
            		((EditorActivity)(mPageEditor.getEditorUiUtility().getContext())).getNotePage()); // Better
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    
	@Override
	protected void onFocusChanged(boolean arg0, int arg1, Rect arg2) {
		//Begin Allen
		if(arg0==true && mPageEditor!=null){
			mPageEditor.setmCurrentEditText(this);
			EditorInfo noteEditTextInfo = new EditorInfo();
	        noteEditTextInfo.packageName = this.getContext().getPackageName();
	        noteEditTextInfo.fieldId = getId();
			mPageEditor.setmCurrentInputConnection(onCreateInputConnection(noteEditTextInfo));
	        
			if (mHistory.isRedoStackEmpty()) {
	            mPageEditor.setEditorRedoEmpty(true);
	        }
	        else{
	        	mPageEditor.setEditorRedoEmpty(false);
	        }
	        if (mHistory.isUndoStackEmpty()) {
	            mPageEditor.setEditorUndoEmpty(true);
	        }
	        else{
	        	mPageEditor.setEditorUndoEmpty(false);
	        }
		}
		bringCursorIntoView();
		//End Allen
		super.onFocusChanged(arg0, arg1, arg2);
	}   

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (mPageEditor != null ) {	
	        // BEGIN chilung_chen@asus.com
	        if (selStart != -1 && selEnd != -1 && !mIsSelectionChangeButNoteReScroll) {
	            if (mMyScrollCorrecterHandler != null && !mMyScrollCorrecterHandler.hasMessages(MY_HANDLE_CURSOT_POS_XY)) {
	                mMyScrollCorrecterHandler.sendEmptyMessage(MY_HANDLE_CURSOT_POS_XY);
	            }
	        }
	        if (mIsSelectionChangeButNoteReScroll) {
	            mIsSelectionChangeButNoteReScroll = false;
	        }
	        // END chilung_chen@asus.com
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mPageEditor != null) {
	        if (mPageEditor != null && !mPageEditor.isTextImgMode()) {
	            mAllowSystemScrollTo = true;
	        }
        }
    }

    private boolean mIsSelectionChangeButNoteReScroll = false;
    
    public void adjustSelectionHandle(int start, int end)
    {
    	if(start != end) //Dave. 0808. Fix the bug:293154. The selector will dispear when rotated.
    	{
    		mPageEditor.editSelectionHandleLeft.setVisibility(View.VISIBLE);
    		mPageEditor.editSelectionHandleRight.setVisibility(View.VISIBLE);
    		mPageEditor.editSelectionHandleLeft.bringToFront();
    		mPageEditor.editSelectionHandleRight.bringToFront();
    		
    		//Emmanual to fix bug 470106, 602208
    		mPageEditor.setNoteEditTextEnable(true);
    	}
    		
    	mleftSelectionOffset = start;
    	mRightSelectionOffset = end;

        int[] location = new int[2]; //get location in editText layout.
        getLocationOnScreen(location);
    	
    	int[] location1 = new int[2];
    	mPageEditor.getLayout().getLocationOnScreen(location1); //get location in NoteFrameLayout.
    	int offsetX = location[0] - location1[0];
    	int offsetY = location[1] - location1[1];
    	
        //Dave.2013.8.13. Fix the bug: the selector posision in Memo/meeting mode is wrong.
        int leftCursorPosX = getCursorXPos(start) - getScrollX();
        int leftCursorPosY = getCursorYPos(start) - getScrollY() + SELECTION_HANDLE_HEIGHT;
        
        leftCursorPosX = (int)(leftCursorPosX * mPageEditor.getScaleX()) + offsetX  + getPaddingLeft();
        leftCursorPosY = (int)(leftCursorPosY * mPageEditor.getScaleY()) + offsetY;
		((EditorActivity) (mPageEditor.getEditorUiUtility().getContext()))
		        .showEditTextPopMenu(this,leftCursorPosX-getPaddingLeft(), leftCursorPosY, true);

    	mPageEditor.editSelectionHandleLeft.setX(leftCursorPosX - SELECTION_LEFT_ARROW_WIDTH);
    	mPageEditor.editSelectionHandleLeft.setY(leftCursorPosY);
    	
    	//Dave.2013.8.13. Fix the bug: the selector posision in Memo/meeting mode is wrong.
        int RightCursorPosX = getCursorXPos(end) - getScrollX();
        int RightCursorPosY = getCursorYPos(end) - getScrollY() + SELECTION_HANDLE_HEIGHT;
        
        RightCursorPosX = (int)(RightCursorPosX * mPageEditor.getScaleX()) + offsetX + getPaddingLeft();
        RightCursorPosY = (int)(RightCursorPosY * mPageEditor.getScaleY()) + offsetY;
    	
    	mPageEditor.editSelectionHandleRight.setX(RightCursorPosX - SELECTION_RIGHT_ARROW_WIDTH);
    	mPageEditor.editSelectionHandleRight.setY(RightCursorPosY);
    }
    //End:Dave.


    @Override
    public void setSelection(int start, int stop) {
        // Log.d("Ryan", "setSelection2 getSelectionStart = " + getSelectionStart() + " getSelectionEnd = " + getSelectionEnd());
        if (getSelectionStart() == -1 || getSelectionEnd() == -1) {
            // Log.d(TAG, "Calling setSelection getSelectionStart() = " + getSelectionStart() + " getSelectionEnd() = " + getSelectionEnd());
            mIsSelectionChangeButNoteReScroll = true;
        }
        
        int len = getText().length();
        
        if (start < 0) {
        	start = 0;
        }
        
        if (start > len) {
        	start = len;
        }
        
        if (stop < 0) {
        	stop = start + 1;
        }
        
        if(stop > len) {
        	stop = len;
    	}
        
		//begin richard
        if(stop == start)
        {
        	super.setSelection(start);
        }
        else if(stop < start)
        {
        	super.setSelection(stop, start);
        	
        	//Begin:Dave. To add text selector.
        	if(MetaData.ENABLE_SELECTOR_HANDLER)
        		adjustSelectionHandle(stop,start);
        	//End:Dave.
        }
        else
        {
        	super.setSelection(start, stop);
        	
        	//Begin:Dave. To add text selector.
        	if(MetaData.ENABLE_SELECTOR_HANDLER)
        		adjustSelectionHandle(start,stop);
        	//End:Dave.
        }
		//end richard
    }

    @Override
    public void setSelection(int index) {
        // Log.d("Ryan", "setSelection1 getSelectionStart = " + getSelectionStart() + " getSelectionEnd = " + getSelectionEnd());
        if (getSelectionStart() == -1 || getSelectionEnd() == -1) {
            // Log.d(TAG, "Calling setSelection getSelectionStart() = " + getSelectionStart() + " getSelectionEnd() = " + getSelectionEnd());
            mIsSelectionChangeButNoteReScroll = true;
        }
        if(index > getText().length())
        	index = getText().length();
        if (index < 0) {
        	index = 0;
        }
        super.setSelection(index);
    }

    public void scrollNoteEditTextTo(int x, int y) {
        super.scrollTo(x, y);
        if (mAllowSystemScrollTo) {
            mAllowSystemScrollTo = false;
        }
        
        //Begin:Dave. To add text selector.
    	if(mPageEditor.editSelectionHandleLeft.getVisibility() == View.VISIBLE && mPageEditor.editSelectionHandleRight.getVisibility() == View.VISIBLE)
    	{
    		setSelection(mleftSelectionOffset, mRightSelectionOffset);
    	}
    	//End:Dave.
    }
    
    // BEGIN: Better
    public void onScrollChanged() {
    	if (this instanceof TemplateEditText){
    		return;
        }
    	
    	if ((mPageEditor != null) && (mPageEditor.getTemplateType() != MetaData.Template_type_todo)) {
	        int num = (int) Math.ceil(((getLineHeight() * getLineCount() + getPaddingTop()) * mPageEditor.getScaleY()
	        		+ mPageEditor.getTemplateLayoutScaleHeight()) 
	        		/ (mHeight * mPageEditor.getScaleY() + mPageEditor.getTemplateLayoutScaleHeight()));
	        if (num > mPageEditor.getPageNum()) {
	        	mPageEditor.setPageNum(num);
	        }
    	}
    }
    // END: Better

    @Override
    public void scrollTo(int x, int y) {
        if (mAllowSystemScrollTo&&!(this instanceof TemplateEditText)) {//Allen
            // Log.d("Ryan", "System scroll to x = " + x + " y = " + y);
            super.scrollTo(getScrollX(), y);
            mAllowSystemScrollTo = false;

        }
        
        onScrollChanged();
    }
    
    public void superScrollTo(int x, int y){
    	super.scrollTo(x, y);
    }
    
    // BEGIN: Better
 	private boolean mIsFirstDrawn = true;
 	
 	public void setFirstDrawn() {
 		mIsFirstDrawn = true;
 	}
 	// END: Better

 	//BEGIN: RICHARD
 	private void prepareDrawLineArg()
 	{
 		mLineHeight = getLineHeight();
 		mBaseLine = getLineBounds(0, mLineBound);
 	}
 	//END: RICHARD
 	
    //Begin Allen
	@Override
	public void draw(Canvas arg0) {	
		if(mContentType == NoteItemArray.CONTENT_BOXEDIT)
		{
			super.draw(arg0);
		}
		else{
			mPageEditor.getDoodleView().drawScreen();
		}
		
		// BEGIN: Better
        if (mIsFirstDrawn) {       	
        	mIsFirstDrawn = false;
        	
        	if (this instanceof TemplateEditText){
        		return;
            }
        	
	        float height = (getLineHeight() * getLineCount() + getPaddingTop()) * mPageEditor.getScaleY() + mPageEditor.getTemplateLayoutScaleHeight();
	        int pageNum = (int) Math.ceil(height / (mHeight * getScaleY() + mPageEditor.getTemplateLayoutScaleHeight()));
	        if (pageNum > mPageEditor.getPageNum()) {
	        	mPageEditor.setPageNum(pageNum);
	        } else {
	        	mPageEditor.setPageNum(mPageEditor.getPageNum());
	        }
	        
	        prepareDrawLineArg();//RICHARD
	        mPageEditor.onNoteEditTextReady();
	        mPageEditor.setRegenerateMicroView(true);
        }
        // END: Better
	}
	
	public void drawContentWithBackground(Canvas canvas){
		super.draw(canvas);
	}
    //End Allen

    // BEGIN: archie_huang@asus.com
    public void drawContent(Canvas canvas) {
    	try{ //smilefish fix bug 586516
    		super.onDraw(canvas);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    } // END: archie_huang@asus.com
    
    // BEGIN: archie_huang@asus.com
    public void drawEditText(Canvas canvas,int scrollX,int scrollY) {//Allen
    	//Begin Allen
    	if(scrollY/mPageEditor.getScaleY() >= getTop() + getPaddingTop()){
    		scrollY = (int) ((getTop() + getPaddingTop())*mPageEditor.getScaleY());
    	}
    	//End Allen
        canvas.save();
        canvas.scale(mPageEditor.getScaleX(), mPageEditor.getScaleY());
        LinearLayout.LayoutParams newLinearLayoutParams =  (LayoutParams) getLayoutParams();//Allen
        canvas.translate(-scrollX/mPageEditor.getScaleX(), -getScrollY()-newLinearLayoutParams.topMargin+getTop()-scrollY/mPageEditor.getScaleY());//Allen
        try {
            setCursorVisible(mIsEnable);
            super.onDraw(canvas);
        }
        catch (Exception e) {//IndexOutOfBounds
            //Log.d(TAG, " " + e.getMessage());
        	Log.d("RICHARD", "DRAW EDIT TEXT FAIL");
            e.printStackTrace();
        }
        canvas.restore();
    } // END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public boolean drawLine(Canvas canvas, int bookGridType, int pageNo,boolean bIsShare) {
        if (mLineHeight  == 0 ) {
        	Log.d("RICHARD","DRAW LINE getLayout() NULL");
            return false;
        }
        if (bookGridType == MetaData.BOOK_GRID_LINE) {
            if (mLinePaint == null) {
                mLinePaint = new Paint();
            }
            if (bIsShare)
            {
            	mLinePaint.setColor(NOTE_LINE_COLOR);
                mLinePaint.setStrokeWidth(NOTE_LINE_WIDTH_FOR_SHARE);//darwin
            }
            else
            {
            	mLinePaint.setColor(NOTE_LINE_COLOR);
                mLinePaint.setStrokeWidth(NOTE_LINE_WIDTH);
            }            
            
            // BEGIN: Better
            float baseline = mBaseLine * mPageEditor.getScaleY();
            canvas.save();
            canvas.translate(0, -pageNo * (mHeight * mPageEditor.getScaleY() + mPageEditor.getTemplateLayoutScaleHeight()));
            while (baseline < mPageEditor.getPageNum() * (mHeight * mPageEditor.getScaleY() + mPageEditor.getTemplateLayoutScaleHeight())) {
                int drawBaseLine = (int) Math.rint(baseline + mPageEditor.getTemplateLayoutScaleHeight());
                if(mPageEditor.getDeviceType() == MetaData.DEVICE_TYPE_320DP)
                	canvas.drawLine(mLineBound.left - getPaddingLeft(), drawBaseLine + 2 ,mLineBound.right, drawBaseLine + 2, mLinePaint);
                else
                	canvas.drawLine(mLineBound.left, drawBaseLine + 2 ,mLineBound.right, drawBaseLine + 2, mLinePaint); //smilefish
                baseline += mLineHeight * mPageEditor.getScaleY();
            }
            canvas.restore();
            // END: Better
        }
        else if(bookGridType == MetaData.BOOK_GRID_GRID){
            if (mLinePaint == null) {
                mLinePaint = new Paint();
                mLinePaint.setColor(NOTE_LINE_COLOR);
                mLinePaint.setStrokeWidth(NOTE_GRID_LINE_WIDTH);
            }
            canvas.save();
            canvas.translate(0, -pageNo * (mHeight * mPageEditor.getScaleY() + mPageEditor.getTemplateLayoutScaleHeight()));
            for (int i = 0; i < mPageEditor.getPageHeight() * mPageEditor.getPageNum(); i += NOTE_GRID_SPACING) {
                int drawLine = (int) Math.rint(i * mPageEditor.getScaleY());
                canvas.drawLine(0, drawLine, mWidth, drawLine, mLinePaint);
            }
            for (int i = 0; i < mWidth; i += NOTE_GRID_SPACING) {
                int drawLine = (int) Math.rint(i * mPageEditor.getScaleX());
                canvas.drawLine(drawLine, 0, drawLine, mPageEditor.getPageHeight() * mPageEditor.getPageNum() * mPageEditor.getScaleY(), mLinePaint);
            }
            canvas.restore();
        }

        return true;
    } // END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public void drawScaledLine(Canvas canvas, float scaleX, float scaleY, boolean isGrid) {
        Rect lineBounds = new Rect();
        Paint linePaint = new Paint(mLinePaint);

        int baseLine;
        int lineCount = getLineCountLimited();

        linePaint.setStrokeWidth(1);
        if (isGrid) {
            linePaint.setAlpha(20);
        }
        else {
            linePaint.setAlpha(80);
        }

        baseLine = (int) (getLineBounds(0, lineBounds) * scaleY) + getPaddingTop();

        if (!isGrid) {
            for (int i = 0; i < lineCount; i++) {
                int drawBaseLine = (int) Math.rint(baseLine * mPageEditor.getScaleY());
                canvas.drawLine(lineBounds.left - getPaddingLeft(), drawBaseLine + 2, lineBounds.right, drawBaseLine + 2, linePaint);
                baseLine += (getLineHeight());
            }
        }
        else {
            for (int i = 0; i < mHeight; i += NOTE_GRID_SPACING) {
                int drawLine = (int) Math.rint(i * mPageEditor.getScaleY());
                canvas.drawLine(0, drawLine, mWidth, drawLine, linePaint);
            }
            for (int i = 0; i < mWidth; i += NOTE_GRID_SPACING) {
                int drawLine = (int) Math.rint(i * mPageEditor.getScaleX());
                canvas.drawLine(drawLine, 0, drawLine, mHeight, linePaint);
            }
        }
    } // END: archie_huang@asus.com

    public void undoWidthoutRedo() {
        Editable editable = mHistory.undoWithoutRedo(getText());
        if (editable != null) {
            mChangeBecauseUnRedo = true;
            setText(editable);
            if (mHistory.getFinalCursorPos() != -1)
                setSelection(mHistory.getFinalCursorPos());
            mChangeBecauseUnRedo = false;
        }

        if (mHistory.isUndoStackEmpty()) {
            mPageEditor.setEditorUndoEmpty(true);
            setIsModified(false);//Allen
        }
        // BEGIN chilung_chen@asus.com, for cursor scrollx
        if (mMyScrollCorrecterHandler != null && !mMyScrollCorrecterHandler.hasMessages(MY_HANDLE_CURSOT_POS_XY)) {
            mMyScrollCorrecterHandler.sendEmptyMessage(MY_HANDLE_CURSOT_POS_XY);
        }
        // END chilung_chen@asus.com, for cursor scrollx
    }

    public void undo() {
        if (mHistory.isRedoStackEmpty()) {
            mPageEditor.setEditorRedoEmpty(false);
        }

        Editable editable = mHistory.undo(getText());
        if (editable != null) {
            mChangeBecauseUnRedo = true;
            setText(editable);
            if (mHistory.getFinalCursorPos() != -1)
                setSelection(mHistory.getFinalCursorPos());
            mChangeBecauseUnRedo = false;
        }

        if (mHistory.isUndoStackEmpty()) {
            mPageEditor.setEditorUndoEmpty(true);
            setIsModified(false);//Allen
        }
        // BEGIN chilung_chen@asus.com, for cursor scrollx
        if (mMyScrollCorrecterHandler != null && !mMyScrollCorrecterHandler.hasMessages(MY_HANDLE_CURSOT_POS_XY)) {
            mMyScrollCorrecterHandler.sendEmptyMessage(MY_HANDLE_CURSOT_POS_XY);
        }
        // END chilung_chen@asus.com, for cursor scrollx
    }

    public void redo() {
        if (mHistory.isUndoStackEmpty()) {
            mPageEditor.setEditorUndoEmpty(false);
        }

        Editable editable = mHistory.redo(getText());
        if (editable != null) {
            mChangeBecauseUnRedo = true;
            setText(editable);
            if (mHistory.getFinalCursorPos() != -1)
                setSelection(mHistory.getFinalCursorPos());
            mChangeBecauseUnRedo = false;
        }

        if (mHistory.isRedoStackEmpty()) {
            mPageEditor.setEditorRedoEmpty(true);
        }
        // BEGIN chilung_chen@asus.com, for cursor scrollx
        if (mMyScrollCorrecterHandler != null && !mMyScrollCorrecterHandler.hasMessages(MY_HANDLE_CURSOT_POS_XY)) {
            mMyScrollCorrecterHandler.sendEmptyMessage(MY_HANDLE_CURSOT_POS_XY);
        }
        // END chilung_chen@asus.com, for cursor scrollx
    }

    // BEGIN: archie_huang@asus.com
    // To avoid memory leak
    public void recycleBitmaps() {
    	// BEGIN: Better
    	if (mMyScrollCorrecterHandler != null) {
    		if (mMyScrollCorrecterHandler.hasMessages(MY_HANDLE_CURSOT_POS_XY)) {
    			mMyScrollCorrecterHandler.removeMessages(MY_HANDLE_CURSOT_POS_XY);
    		}
    		if (mMyScrollCorrecterHandler.hasMessages(MY_HANDLE_CURSOT_POS_X)) {
    			mMyScrollCorrecterHandler.removeMessages(MY_HANDLE_CURSOT_POS_X);
    		}
    		if (mMyScrollCorrecterHandler.hasMessages(MY_HANDLE_CURSOT_POS_Y)) {
    			mMyScrollCorrecterHandler.removeMessages(MY_HANDLE_CURSOT_POS_Y);
    		}
    		mMyScrollCorrecterHandler = null;
    		while (mIsScrollHandlerRunning) {
    			try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    	}
    	// END: Better
    	
        Editable editable = getEditableText();
        DrawableSpan[] spans = editable.getSpans(0, editable.length(), DrawableSpan.class);
        if (spans != null) {
            for (DrawableSpan span : spans) {
                Drawable drawable = span.getDrawable();
                if (drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
            }
        }
    }

    // END: archie_huang@asus.com

    public void clearUndoRedoStack() {
    	//emmanual
    	if(mHistory == null || mPageEditor == null){
    		return ;
    	}
        mHistory.clearStack();

        mPageEditor.setEditorUndoEmpty(true);
        setIsModified(false);//Allen
        mPageEditor.setEditorRedoEmpty(true);
    }

    public void insertToHistoryStack(Editable ori, Editable changed, int startat) {
        if (mHistory.isUndoStackEmpty()) {
            mPageEditor.setEditorUndoEmpty(false);
        }
        mHistory.insertNew(ori, changed, startat);
        if (mHistory.isRedoStackEmpty()) {
            mPageEditor.setEditorRedoEmpty(true);
        }
    }

    private int caculateWidthBound() {
        TextPaint paint = new TextPaint(getPaint());
        String string = getText().toString();
        int enterIndex = -1;
        int previousEnterIndex = -1;
        int maxLineWidth = -1;
        while (enterIndex < string.length()) {

            enterIndex = string.indexOf("\n", enterIndex + 1);
            enterIndex = (enterIndex == -1) ? string.length() : enterIndex;

            Editable oneLine = (Editable) getText().subSequence(previousEnterIndex + 1, enterIndex);
            //begin smilefish fix bug 321827
            StaticLayout tempLayout = new StaticLayout(oneLine, paint, 10000, android.text.Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
            int lineWidth = 0;
            if(tempLayout.getLineCount() > 0)
                lineWidth = (int)tempLayout.getLineWidth(0);
            //end smilefish
            int lineLength = oneLine.length(); //smilefish

            // Calculate HandWriteItem
            NoteHandWriteItem items[] = oneLine.getSpans(0, lineLength, NoteHandWriteItem.class);
            int totalHandWidthOffset = 0;
            int objWidth = (int) paint.measureText(OBJ);
            for (NoteHandWriteItem item : items) {
                int handWidthOffset = item.getWidth() - objWidth;
				if (handWidthOffset > 0) {// emmanual to fix bug 471963
					totalHandWidthOffset += handWidthOffset;
				}
            }
            lineWidth += totalHandWidthOffset;
            
            //begin smilefish fix bug 292305
            //Calculate timestamp
            NoteTimestampItem timestampItems[] = oneLine.getSpans(0, lineLength, NoteTimestampItem.class);
            int totalTimestampWidthOffset = 0;
            int timestampCount = timestampItems.length;
            if(timestampCount > 0){
            	totalTimestampWidthOffset = (timestampItems[0].getWidth() - objWidth) * timestampCount;
            }
            lineWidth += totalTimestampWidthOffset;
            
            //Calculate audio and video
            NoteImageItem imageItems[] = oneLine.getSpans(0, lineLength, NoteImageItem.class);
            int totalImageWidthOffset = 0;
            int imageCount = imageItems.length;
            if(imageCount > 0){
            	totalImageWidthOffset = (imageItems[0].getWidth() - objWidth) * imageCount;
            }
            lineWidth += totalImageWidthOffset;
            //end smilefish

            maxLineWidth = (lineWidth > maxLineWidth) ? lineWidth : maxLineWidth;
            previousEnterIndex = enterIndex;
        }

        return Math.min(maxLineWidth, getWidth());
    }

    private int caculateHeightBound() {
        return getLineCount() * getLineHeight() + getPaddingTop();
    }

    public Rect getBound() {
        return getBound(mWidth, mHeight);
    }

    public Rect getBound(int maxWidth, int maxHeight) {
        Rect boundRect = new Rect();

        if (getText().length() == 0) {
            return boundRect;
        }

        float scaleX = (float) maxWidth / (float) mWidth;
        float scaleY = (float) maxHeight / (float) mHeight;

        boundRect.left = 0;
        boundRect.top = 0;
        boundRect.right = (int) (caculateWidthBound() * scaleX);
        boundRect.bottom = (int) (caculateHeightBound() * scaleY);
        return boundRect;
    }
    
    // BEGIN: Better
    public void getResultFull(Canvas result, int maxWidth, int maxHeight, boolean isTextOnly,int TemplatePaddingTop, int pageNo) {
        float scaleX = (float) maxWidth / (float) mWidth;
        float pageHeight = mHeight + TemplatePaddingTop;
        float contentHeight = (pageNo >= 0) ? pageHeight : pageHeight * mPageEditor.getPageNum();
        float scaleY = maxHeight / contentHeight;

        int currentScrollY = mPageEditor.getScrollY();
        int currentPaddingLeft = getPaddingLeft();
        int currentPaddingRight = getPaddingRight();
        int currentPaddingTop = getPaddingTop();
        int currentPaddingBottom = getPaddingBottom();

        int currentSelectionStart = getSelectionStart();
        int currentSelectionEnd = getSelectionEnd();

        int scrollY = (pageNo >= 0) ? (int) (pageNo * pageHeight) : 0;
        float editTextHeight = (pageNo >= 0) ? mHeight : contentHeight;
        int windowHeight = getHeight();

        setCursorVisible(false);
        setPadding(0, 0, getPaddingLeft() + getPaddingRight(), 0);
        setSelection(currentSelectionEnd, currentSelectionEnd);

        mPageEditor.ScrollViewTo(-1, (int) (scrollY * getScaleY()), true);

        result.save();
        result.scale(scaleX, scaleY);

        float offset = (pageNo <= 0) ? (currentPaddingTop + TemplatePaddingTop) : 0;
        result.translate(0, (pageNo > 0) ? offset - ((pageNo - 1) * pageHeight + (pageHeight - (currentPaddingTop + TemplatePaddingTop))) : offset);

        while (editTextHeight > 0 && windowHeight != 0) {
            if (isTextOnly) {
                drawOnlyText(result);
            }
            else {
                drawContent(result);
            }
            scrollY += windowHeight;
            mPageEditor.ScrollViewTo(-1, (int) (scrollY * getScaleY()), true);
            editTextHeight -= windowHeight;
        }
        setSelection(currentSelectionStart, currentSelectionEnd);
        mPageEditor.ScrollViewTo(-1, currentScrollY,true);//Allen
        setPadding(currentPaddingLeft, currentPaddingTop, currentPaddingRight, currentPaddingBottom);
        setCursorVisible(true);
        result.restore();

        mIsLayoutReady = false;
    }
    // END: Better
    
    //begin smilefish for color picker
    public void getResultFullNew(Canvas result, int maxWidth, int maxHeight, boolean isTextOnly,int TemplatePaddingTop, int pageNo) {
        float scaleX = (float) maxWidth / (float) mWidth;
        float pageHeight = mHeight + TemplatePaddingTop;
        float contentHeight = (pageNo >= 0) ? pageHeight : pageHeight * mPageEditor.getPageNum();
        float scaleY = maxHeight / contentHeight;

        int currentPaddingLeft = getPaddingLeft();
        int currentPaddingRight = getPaddingRight();
        int currentPaddingTop = getPaddingTop();
        int currentPaddingBottom = getPaddingBottom();

        int currentSelectionStart = getSelectionStart();
        int currentSelectionEnd = getSelectionEnd();

        float editTextHeight = (pageNo >= 0) ? mHeight : contentHeight;
        int windowHeight = getHeight();

        setCursorVisible(false);
        setPadding(getPaddingLeft(), 0, getPaddingRight(), 0);
        setSelection(currentSelectionEnd, currentSelectionEnd);

        result.save();
        result.scale(scaleX, scaleY);

        float offset = (pageNo <= 0) ? (currentPaddingTop + TemplatePaddingTop) : 0;
        result.translate(0, (pageNo > 0) ? offset - ((pageNo - 1) * pageHeight + (pageHeight - (currentPaddingTop + TemplatePaddingTop))) : offset);

        while (editTextHeight > 0 && windowHeight != 0) {
            if (isTextOnly) {
                drawOnlyText(result);
            }
            else {
            	try{ //smilefish fix bug 000112
            		drawContent(result);
            	}catch(Exception e){
            		e.printStackTrace();
            	}
            }
            editTextHeight -= windowHeight;
        }
        setSelection(currentSelectionStart, currentSelectionEnd);
        setPadding(currentPaddingLeft, currentPaddingTop, currentPaddingRight, currentPaddingBottom);
        setCursorVisible(true);
        result.restore();

        mIsLayoutReady = false;
    }
    //end smilefish
    
    //darwin
    public void getResult(Canvas result, int maxWidth, int maxHeight, boolean isTextOnly,int TemplatePaddingTop) {
    	//darwin
        float scaleX = (float) maxWidth / (float) mWidth;
        float scaleY = (float) maxHeight / (float) mHeight;

        int currentScrollY = mPageEditor.getScrollY();//computeVerticalScrollOffset();
        int currentPaddingLeft = getPaddingLeft();
        int currentPaddingRight = getPaddingRight();
        int currentPaddingTop = getPaddingTop();
        int currentPaddingBottom = getPaddingBottom();

        // BEGIN: archie_huang@asus.com
        int currentSelectionStart = getSelectionStart();
        int currentSelectionEnd = getSelectionEnd();
        // END: archie_huang@asus.com

        int scrollY = 0;//currentPaddingTop + TemplatePaddingTop;
        int editTextHeight = mHeight;//getNoteEditTextHeight();
        int windowHeight = getHeight();

        setCursorVisible(false);
        setPadding(0, 0, getPaddingLeft() + getPaddingRight(), 0);
        // BEGIN: archie_huang@asus.com
        setSelection(currentSelectionEnd, currentSelectionEnd);
        // END: archie_huang@asus.com

        scrollNoteEditTextTo(0, 0);
        //setScaleX(scaleX);
        //setScaleY(scaleY);
        result.save();
        result.scale(scaleX, scaleY);
        //darwin
        result.translate(0, currentPaddingTop + TemplatePaddingTop);
        //darwin
        while (editTextHeight > 0 && windowHeight != 0) {
            if (isTextOnly) {
                drawOnlyText(result);
            }
            else {
                // BEGIN: archie_huang@asus.com
                // this.draw(result);
                drawContent(result);
                // END: archie_huang@asus.com
            }
            scrollY += windowHeight;
            scrollNoteEditTextTo(0, scrollY);
            editTextHeight -= windowHeight;
        }
        // BEGIN: archie_huang@asus.com
        setSelection(currentSelectionStart, currentSelectionEnd);
        // BEGIN: archie_huang@asus.com

        setPadding(currentPaddingLeft, currentPaddingTop, currentPaddingRight, currentPaddingBottom);
        mPageEditor.ScrollViewTo(0, currentScrollY,true);//Allen
        setCursorVisible(true);
        result.restore();

        // BEGIN: archie_huang@asus.com
        // setPadding() will assign null to mLayout
        mIsLayoutReady = false;
        // END: archie_huang@asus.com
    }

    // BEGIN: archie_huang@asus.com
    // To improve performance
    private void drawOnlyText(Canvas canvas) {
        Editable editable = getText();

        DrawableSpan[] spans = editable.getSpans(0, editable.length(), DrawableSpan.class);
        for (DrawableSpan span : spans) {
        	//darwin
        	if(CFG.getCanDoVO())
        	{
        		if(span instanceof NoteHandWriteItem)
	        	{
	        		span.setVisible(true);
	        	}
	        	else
	        	{
	        		span.setVisible(false);
	        	}
        	}
        	else
        	{
        		span.setVisible(false);
        	}
        	
        	//darwin
        }

        setCursorVisible(false);

        // BEGIN: archie_huang@asus.com
        // super.draw(canvas);
        drawContent(canvas);
        // END: archie_huang@asus.com

        setCursorVisible(true);

        for (DrawableSpan span : spans) {
            span.setVisible(true);
        }
    } // END: archie_huang@asus.com

    public void setIsFirstTimeLoad(boolean isFirstTime) {
        mIsFirstTimeLoad = isFirstTime;
    }

    // BEGIN: Better
    private boolean mIsScrollHandlerRunning = false;
    // END: Better
    
    // BEGIN chilung_chen@asus.com, for cursor scrollx
    private Handler mMyScrollCorrecterHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
        	// BEGIN: Better
        	mIsScrollHandlerRunning = true;
        	// END: Better
            switch (msg.what) {
                case MY_HANDLE_CURSOT_POS_XY:
                    bringCursorIntoView();
                    break;
                case MY_HANDLE_CURSOT_POS_X:
                    bringCursorIntoView();
                    break;
                case MY_HANDLE_CURSOT_POS_Y:
                    bringCursorIntoView();
                    break;
                default:
                    super.handleMessage(msg);
            }
            // BEGIN: Better
            mIsScrollHandlerRunning= false;
            // END: Better
        }

    };
    
    //Begin Allen
    public void switchBaselineMode(boolean enableBaseLine){
    	isBaseLineMode = enableBaseLine;
    	if(hasFocus()){
    		bringCursorIntoView();
    	}
    }
    //End Allen
    
    public void calAutoJumpLineCount(int viewHeight)
    {
    	float singleLineHight = getLineHeight()*mPageEditor.mScaleX;
    	if(viewHeight > 3*singleLineHight)
    	{
    		mAutoJumpDistance = singleLineHight;
    	}
    	else
    	{
    		mAutoJumpDistance = 0;
    	}
    }

    public void bringCursorIntoView() {
    	if (!hasFocus() || mPageEditor==null || mPageEditor.isReadOnlyMode()) {//Allen
            return;
        }
    	int[] locationOffset = mPageEditor.getViewTopLoaction();
        int[] location = new int[2];
        getLocationOnScreen(location);
    	int offsetX = location[0] - locationOffset[0];
    	int offsetY = location[1] - locationOffset[1];
        
        int cursorX = getCursorXPos(getSelectionEnd()) + offsetX;
        int cursorY = getCursorYPos(getSelectionEnd()) + offsetY+getPaddingTop();
        
        
        float realCursorX = cursorX * mPageEditor.getScaleX();
        float realCursorY = cursorY * mPageEditor.getScaleY();
        float destionX = mPageEditor.getPageEditorScrollBar().getScrollX();
        float destionY = mPageEditor.getPageEditorScrollBar().getScrollY();
        Boolean needXScrollFlag = false;
        Boolean needYScrollFlag = false;
        
        if(realCursorX < mPageEditor.getPageEditorScrollBar().getScrollX())
        {
        	needXScrollFlag = true;
        	destionX = realCursorX - mPageEditor.getViewWidth()/3;
        	if(destionX < 0)
        	{
        		destionX = 0;
        	}
        }else if(realCursorX > mPageEditor.getPageEditorScrollBar().getScrollX() + mPageEditor.getViewWidth())
        {
        	needXScrollFlag = true;
        	destionX = realCursorX - mPageEditor.getViewWidth()*0.66F;
        }

        float singleLineHight = getLineHeight()*mPageEditor.mScaleX;
        //Begin Allen
        int viewHeight = 0;
        if(isBaseLineMode && mPageEditor.getEditorUiUtility().getInputMode()==InputManager.INPUT_METHOD_SCRIBBLE){
        	viewHeight = (int) getResources().getDimension(R.dimen.baseline_top);
        	singleLineHight = 0;
        	
        }
        else{
        	viewHeight = mPageEditor.getViewHeight();
        }
        //End Allen
        
        if(realCursorY < mPageEditor.getPageEditorScrollBar().getScrollY() + mAutoJumpDistance
        		//emmanual fix bug 355238
        		&& !(getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
        		&& mPageEditor.getEditorUiUtility().getInputMode() == InputManager.INPUT_METHOD_SCRIBBLE))
        {
        	needYScrollFlag = true;
        	destionY = realCursorY - mAutoJumpDistance - getPaddingTop()*mPageEditor.mScaleX;
        	if(destionY < 0)
        	{
        		destionY = 0;
        		if(mPageEditor.getPageEditorScrollBar().getScrollY() == 0)
        		{
        			needYScrollFlag = false;
        		}
        	}
        }else if(realCursorY > mPageEditor.getPageEditorScrollBar().getScrollY() + viewHeight - singleLineHight - mAutoJumpDistance)
        {
        	needYScrollFlag = true;
        	if (viewHeight<getLineHeight()) {// update by jason
        		destionY = realCursorY - viewHeight + singleLineHight -mAutoJumpDistance;
			}else {
				destionY = realCursorY - viewHeight + singleLineHight + mAutoJumpDistance;
			}        	
        }
        //emmanual to fix bug 357104
		if (getId() == R.id.travel_title_edit){
			if (destionY == 0) {
				needYScrollFlag = true;
				destionY = getResources().getDimension(R.dimen.travel_title_scroll_margin);
			}
		}
        if(needXScrollFlag||needYScrollFlag)
        {
        	mPageEditor.ScrollViewTo((int)destionX,(int)destionY,true);
        }
        // BEGIN: Better   
        if(MetaData.IS_ENABLE_CONTINUES_MODE){
        	if ((mPageEditor != null) && (mPageEditor.getTemplateType() != MetaData.Template_type_todo)) {
    	        int num = (int) Math.ceil(((getLineHeight() * getLineCount() + getPaddingTop()) * mPageEditor.getScaleY()
    	        		+ mPageEditor.getTemplateLayoutScaleHeight()) 
    	        		/ (mHeight * mPageEditor.getScaleY() + mPageEditor.getTemplateLayoutScaleHeight()));
    	        if (num > mPageEditor.getPageNum()) {
    	        	mPageEditor.setPageNum(num);
    	        }
            }
        }
        // END: Better
    }
    // END chilung_chen@asus.com, for cursor scrollx
    
    public String getTextWithoutTailSpacing() {
        String text = getText().toString();
        int lastIndex = 0;
        for (int i = text.length() - 1; i >= 0; i--) {
            if ((!text.substring(i, i + 1).equals("\n") && !text.substring(i, i + 1).equals(" "))) {
                lastIndex = i + 1;
                break;
            }
        }

        return new String(text.substring(0, lastIndex));
    }

    public void setIsUsingSetColorOrStyle(boolean isUse) {
        mIsUsingSetColorOrStyle = isUse;
    }

    //BEGIN: RICHARD
    public void setChangeBecauseAutoRecgnizer(Boolean flag)
    {
    	mChangeBecauseAutoRecgnizer = flag;
    }
    //END: RICHARD
    
    // BEGIN: Shane_Wang 2012-11-5
    int beforePosition = 0;
    int afterPosition = 0;
    // END: Shane_Wang 2012-11-5
    
    public class MyTextWatcher implements TextWatcher {

        private int mStartat = 0;
        private Editable mOriginalEditable = null;
        private Editable mChangedEditable = null;

        private int beforeTextChangedVerticalHeight = 0;//Allen
        private int afterTextChangedVerticalHeight = 0;//Allen
        public synchronized void beforeTextChanged(CharSequence s, int start, int count, int after) {

            if (mChangeBecauseUnRedo) return;
            if (mChangeBecauseAutoRecgnizer) return;//RICHARD
            mOriginalEditable = new SpannableStringBuilder(s.subSequence(start, start + count));
            beforeTextChangedVerticalHeight = computeVerticalScrollRange();//Allen
        }

        public synchronized void onTextChanged(CharSequence s, int start, int before, int count) {
        	mPageEditor.setMicroViewVisible(false);//Allen
            
            if (mChangeBecauseUnRedo) return;
            if (mChangeBecauseAutoRecgnizer) return;//RICHARD

        	// BEGIN: Shane_Wang 2012-11-5
        	beforePosition = start + before;
        	afterPosition = start + count;
            // END: Shane_Wang 2012-11-5
            mChangedEditable = new SpannableStringBuilder(s.subSequence(start, start + count));
            mStartat = start;
        }
        //Begin Allen
        private boolean canUndoRedo(){
        	if(mContentType==NoteItemArray.TEMPLATE_CONTENT_MEETING_ENDDATE||
        			mContentType==NoteItemArray.TEMPLATE_CONTENT_MEETING_ENDTIME||
        			mContentType==NoteItemArray.TEMPLATE_CONTENT_MEETING_STARTTIME||
        			mContentType==NoteItemArray.TEMPLATE_CONTENT_METTING_STARTDATE||
        			mContentType==NoteItemArray.TEMPLATE_CONTENT_TRAVEL_DATE){
        		return false;
        	}
        	else{
        		return true;
        	}
        }
        //End Allen
        
        public synchronized void afterTextChanged(Editable s) {
        	//emmanual to fix bug 354137
			if (getText().toString().replace(OBJ, " ").trim().equals("") && !mChangeBecauseAutoRecgnizer
					&& (mPageEditor.getTemplateType() == MetaData.Template_type_meeting
					|| getId() == R.id.travel_title_edit)
					//emmanual to fix bug 408916
					|| (mContentType == NoteItemArray.CONTENT_BOXEDIT
					//emmanual to fix bug 444382,442830
			        	&& getText().toString().replace(OBJ, " ").equals("") 
			        || (MetaData.isRTL() 
			        		&& (mPageEditor.getEditorUiUtility().getInputMode() == InputManager.INPUT_METHOD_SCRIBBLE 
			        		|| mPageEditor.getEditorUiUtility().getInputMode() == InputManager.INPUT_METHOD_TEXT_IMG_SCRIBBLE)))
			        && !mChangeBecauseAutoRecgnizer) {
				try{
					mChangeBecauseAutoRecgnizer = true;
					s.append(" ");
					s.delete(s.length()-1, s.length());
					mChangeBecauseAutoRecgnizer = false;
				}catch(Exception ex){
					mChangeBecauseAutoRecgnizer = false;
				}
			}
			//emmanual to fix bug 392671	
			NoteBookPickerActivity.dismissLoadProgressDialog();
			
			//emmanual to fix bug 441433, add try-catch
			try{
				updateTextStyleAndColor(s);
			}catch(Exception ex){
				
			}
            
            afterTextChangedVerticalHeight = computeVerticalScrollRange();//Allen
            // BEGIN chilung_chen@asus.com
            if (!mChangeBecauseUnRedo && !mChangeBecauseAutoRecgnizer) {//RICHARD
                // END chilung_chen@asus.com
                if (mHistory.isUndoStackEmpty()&&canUndoRedo()) {//Allen
                    mPageEditor.setEditorUndoEmpty(false);
                }
                
                try{//emmanual to fix bug 418841
                // BEGIN: Shane_Wang 2012-11-7
                Editable changedEditable = new SpannableStringBuilder(s.subSequence(mStartat, afterPosition));
                // END: Shane_Wang 2012-11-7
                // BEGIN锟斤�?Shane_Wang@asus.com 2012-12-6
                if(!mOriginalEditable.toString().equals(changedEditable.toString())) {
                	mHistory.insertNew(mOriginalEditable, changedEditable, mStartat);
                }
                // END锟斤�?Shane_Wang@asus.com 2012-12-6
                } catch (Exception e) {
					// TODO: handle exception
				}
                if (mHistory.isRedoStackEmpty()) {
                    mPageEditor.setEditorRedoEmpty(true);
                }
            }

//            checkLineCount(s);
            if(!mChangeBecauseAutoRecgnizer && !mChangeBecauseUnRedo && afterTextChangedVerticalHeight>=beforeTextChangedVerticalHeight)
            {
            	if (!mIsFirstTimeLoad) {
            		checkLineCount(s);
            	}
            }

            // BEGIN chilung_chen@asus.com, for cursor scrollx
            if (!mChangeBecauseUnRedo && !mChangeBecauseAutoRecgnizer) { //RICHARD
                if (mMyScrollCorrecterHandler != null && !mMyScrollCorrecterHandler.hasMessages(MY_HANDLE_CURSOT_POS_XY)) {
                    mMyScrollCorrecterHandler.sendEmptyMessage(MY_HANDLE_CURSOT_POS_XY);
                }
            }
            // END chilung_chen@asus.com, for cursor scrollx

            //Begin: show_wang@asus.com
            //Modified reason: for dds, test         
            if (mPageEditor.getEditorUiUtility().getInputMode() == InputManager.INPUT_METHOD_SELECTION_TEXT) {
            	 if (!mIsFirstTextChange) {
                 	mPageEditor.quitSelectionTextMode();
                 }
            	mIsFirstTextChange = false;
            }
            //End: show_wang@asus.com
            if (!mIsFirstTimeLoad) {
            	setIsModified(true);//Allen
            }
            
            //Begin:Dave. To add text selector.
            mPageEditor.editSelectionHandleLeft.setVisibility(View.INVISIBLE);
            mPageEditor.editSelectionHandleRight.setVisibility(View.INVISIBLE);
            //End:Dave.            

            //emmanual to fix bug 528854
			if (MetaData.isRTL()
					&& afterPosition > beforePosition
			        && (mPageEditor.getEditorUiUtility().getInputMode() == InputManager.INPUT_METHOD_SCRIBBLE 
			        	|| mPageEditor.getEditorUiUtility().getInputMode() == InputManager.INPUT_METHOD_TEXT_IMG_SCRIBBLE)) {
				setSelection(beforePosition);
        	}
        }

		protected void checkLineCount(Editable editable) {
			if (MetaData.IS_ENABLE_CONTINUES_MODE) {
				if (mChangedEditable.length() == 0) {
					return;
				}
				// Begin Siupo
				if (mPageEditor.getTemplateType() == MetaData.Template_type_meeting
						|| mPageEditor.getTemplateType() == MetaData.Template_type_travel) {
					if (mChangedEditable.length() == 0)
						return;
					if (mPageEditor.getCurrentEditor() instanceof TemplateEditText) {
						if (getLineCount() > 1) {
							undoWidthoutRedo();
						}
					}
				}
				// End Siupo

				// Begin Allen++ for todo template
				if (mContentType == NoteItemArray.TEMPLATE_CONTENT_TODO_EDIT) {
					int changedHeight = (int) (afterTextChangedVerticalHeight - beforeTextChangedVerticalHeight+
							getPaddingTop()+(TEMPLETE_TODO_LINE_SPACE-1)*getLineHeight());
					if(getLineCount() > mLineCountLimited||
							mPageEditor.IsToDoPageFull(changedHeight)){
						EditorActivity.showToast(getContext(),
								R.string.editor_page_is_full);
						undoWidthoutRedo();
					}
					return;
				}
				// End Allen
				
				// Begin: show_wang@asus.com
				// Modified reason: BoxEditText limit line
				if (!mPageEditor.IsNoteEditTextCurrentEditor()) {
					if (getLineCount() > mLineCountLimited) {
						EditorActivity.showToast(getContext(),
								R.string.editor_page_is_full);
						undoWidthoutRedo();
					}
				}
				// End: show_wang@asus.com
			} else {
				if (mChangedEditable.length() == 0) {
					return;
				}
				
				// Begin Allen++ for todo template
				if (mContentType == NoteItemArray.TEMPLATE_CONTENT_TODO_EDIT) {
					int changedHeight = (int) (afterTextChangedVerticalHeight - beforeTextChangedVerticalHeight+
								getPaddingTop()+(TEMPLETE_TODO_LINE_SPACE-1)*getLineHeight());
					if (mPageEditor.IsToDoPageFull(changedHeight)) {
						EditorActivity.showToast(getContext(),
								R.string.editor_page_is_full);
						undoWidthoutRedo();
					}
					return;
				}
				// End Allen
				
				if(!isCommonEditText()){
					return;
				}

				//if (getLineCount() > mLineCountLimited) {
				if(afterTextChangedVerticalHeight > mHeight && getLineCount()>1){//RICHARD
					int needLineCount = 0;
					if(MetaData.IS_AUTO_PAGING){
						needLineCount = (afterTextChangedVerticalHeight - mHeight + getLineHeight() - 1 )/getLineHeight();
					}else{
						needLineCount = (afterTextChangedVerticalHeight - mHeight + getLineHeight() - 3 )/getLineHeight();
					}
					if (needLineCount > 0 && !checkAndMakeMoreSpace(needLineCount)) {
						if (mLineCountLimited == 1) {// Allen
						} else {
							//emmanual
							EditorActivity activity = ((EditorActivity) (mPageEditor
							        .getEditorUiUtility().getContext()));
							if ((activity.isCurrentLastPage() || MetaData.IS_PASTE_OR_SHARE)
							        && mContentType != NoteItemArray.CONTENT_BOXEDIT) {
								autoAddTextPage(editable);
							} else {
								EditorActivity.showToast(getContext(),
								        R.string.editor_page_is_full);
								undoWidthoutRedo();
								if (MetaData.AUTO_ADD_PAGE) {
//									MetaData.AUTO_ADD_PAGE = false;
									((EditorActivity) (mPageEditor.getEditorUiUtility()
									        .getContext())).dismissAutoPageProgress();
									MetaData.IS_PASTE_OR_SHARE = false;
								}
							}
						}
					}
				} else {
					if (MetaData.AUTO_ADD_PAGE && !MetaData.PAUSE_AUTO_PAGING) {
//						MetaData.AUTO_ADD_PAGE = false;
						((EditorActivity) (mPageEditor.getEditorUiUtility()
						        .getContext())).dismissAutoPageProgress();
						MetaData.IS_PASTE_OR_SHARE = false;
					}
				}
			}
		}
		
		private boolean isCommonEditText() {
			if (mContentType == NoteItemArray.TEMPLATE_CONTENT_DEFAULT_NOTE_EDITTEXT
			        || mContentType == NoteItemArray.TEMPLATE_CONTENT_DEFAULT_NOTE_EDITTEXT
			        || mContentType == NoteItemArray.CONTENT_BOXEDIT
			        || mContentType == NoteItemArray.CONTENT_NOTEEDIT) {
				return true;
			}
			return false;
		}
	
		// emmanual
		boolean isFromShare = false;
		String mLastPasteStr = "";
		private void autoAddTextPage(Editable editable) {
			if (getLayout() == null) {
				return;
			}
			if (getLineCount() <= mLineCountLimited) {
				return ;
			}
			if(MetaData.INSERT_BROWSER_SHARE){
				return ;
			}
			
			int endPos = getLayout().getLineEnd(mLineCountLimited - 1);
			boolean showProgress = true;
			//emmanual to fix bug 498523
			if(endPos > editable.length()){
				return;
			}else if(endPos == editable.length() && editable.toString().substring(endPos-1, endPos).equals("\n")){
				editable.delete(endPos - 1, endPos);
				showProgress = false;
				endPos -=1;
			}
			final EditorActivity activity = ((EditorActivity) (mPageEditor
			        .getEditorUiUtility().getContext()));
			
			if(showProgress){
				activity.showAutoPageProgress();
			}
//			String allText = editable.toString();
			mChangeBecauseAutoRecgnizer = true;
			mPageEditor.setmCurrentEditText(mPageEditor.getNoteEditText());
			mPageEditor.copyTheSelectionText(endPos, editable.length());
			boolean addPage = !(activity.isShareMode() && endPos == editable.length())||isFromShare;
			isFromShare = activity.isShareMode();
			editable.delete(endPos, editable.length());
			mChangeBecauseAutoRecgnizer = false;
			boolean deletePage = false;
	        if(MetaData.IS_AUTO_PAGING && MetaData.IS_PASTE_OR_SHARE){
	        	String mPasteStr = editable.toString();
	        	if(mLastPasteStr.equals(mPasteStr) || mPasteStr.equals("")){
	        		deletePage = true;
	        		activity.deleteThisPage();
	        	}
	        	mLastPasteStr = mPasteStr;
	        }
			if(addPage){
				if (deletePage) {
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							activity.addNewPage();
						}
					}, 200);
				} else {
					activity.addNewPage();
				}
			}
		}


        private boolean checkAndMakeMoreSpace(int needLine) {
            boolean haveMoreSpace = false;

            String text = getText().toString();
            int lastIndex = text.length();
            int[] enterIndex = new int[50];
            int j = 0;
            for (int i = text.length() - 1; i >= 0; i--) {
                if ((!text.substring(i, i + 1).equals("\n") && !text.substring(i, i + 1).equals(" "))
                        || i < getSelectionEnd()) {
                    lastIndex = i + 1;
                    break;
                }
                if (text.substring(i, i + 1).equals("\n")) {
                    enterIndex[j++] = i;
                }
            }
            if (lastIndex == text.length()) {
                return false;
            }

            String tailText = new String(text.substring(lastIndex));
            int tailEnterCount = 0;
            int index = -1;
            while ((index = tailText.toString().indexOf("\n", index + 1)) != -1) {
                tailEnterCount++;
            }
            if (needLine - tailEnterCount > 1) {
                return false;
            }
            else if (needLine - tailEnterCount == 1) {
                return false;
            }
            else if (needLine - tailEnterCount < 1) {
                getText().delete(enterIndex[needLine - 1], getText().length());
                return true;
            }
            return haveMoreSpace;
        }

        private void updateTextStyleAndColor(Editable editable) {
            if (!mIsUsingSetColorOrStyle) return;
            if (mIsFirstTimeLoad) return;
            int changeTextCount = mChangedEditable.length();
            if (changeTextCount <= 0) return;
            if (mChangeBecauseUnRedo) return;
            if (mChangeBecauseAutoRecgnizer) return;//RICHARD
            if (editable.toString().length() >= mStartat 
            		&& editable.toString().length() >= mStartat + changeTextCount
            		&& editable.toString().substring(mStartat, mStartat + changeTextCount).indexOf(OBJ) != -1) return;

            // BEGIN: Shane_Wang 2012-11-5
            NoteItem[] noteItems = editable.getSpans(0, editable.length(), NoteItem.class);
            int style = mPageEditor.getTextStyle();
            int color = mPageEditor.getEditorPaint().getColor();//Modified by show
            boolean isTextStyleHasChanged = false;
            boolean isColorHasChanged = false;
            
            //step1: if delete, do nothing
            if(afterPosition - beforePosition < 0 
            		&& getId() != R.id.travel_title_edit //emmanual to fix bug 423115
            		//emmanual to fix bug 423093
            		&& mPageEditor.getEditorUiUtility().getInputMode() != InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD
            		&& !(PickerUtility.getDeviceType(getContext()) == MetaData.DEVICE_TYPE_320DP && getId() == R.id.BoxEditText)) {	
            	return;
        	}
            
            if(afterPosition - beforePosition != changeTextCount 
                    //emmanual to fix bug 428376 428427
            		&& mStartat < afterPosition) {
            	//begin smilefish fix bug 354165
                if(!isTextStyleHasChanged) {
                	if(style != Typeface.BOLD){//emmanual to fix bug 481758
						StyleSpan[] ss = editable.getSpans(mStartat, afterPosition, StyleSpan.class);
						for (int i = 0; i < ss.length; i++) {
							editable.removeSpan(ss[i]);
						}
                	}
            	    NoteTextStyleItem textStyleItem = new NoteTextStyleItem(style);
            	    editable.setSpan(textStyleItem, mStartat, afterPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                
                if(!isColorHasChanged && mContentType != NoteItemArray.TEMPLATE_CONTENT_TRAVEL_DATE) {
                	NoteForegroundColorItem foreColorItem = new NoteForegroundColorItem(color);
                    editable.setSpan(foreColorItem, mStartat, afterPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    resetUrlsColor(editable);
                }
                //end smilefish
            	return;
            }
            //step2: if styles of changed text are the same as the neighbor
            for(NoteItem item : noteItems) {
            	if(!isTextStyleHasChanged && item instanceof NoteTextStyleItem) {
            		if(editable.getSpanEnd(item) == mStartat && ((NoteTextStyleItem)item).getStyle() == style) {
            			int start = editable.getSpanStart(item);
            			int end = mStartat + changeTextCount;
            			editable.removeSpan(item);
            			NoteTextStyleItem textStyleItem = new NoteTextStyleItem(style);
            			editable.setSpan(textStyleItem, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            			isTextStyleHasChanged = true;
            		}
            	}
            	else if(!isColorHasChanged && item instanceof NoteForegroundColorItem) {//合并上一个和当前改变的text
            		if(editable.getSpanEnd(item) == mStartat && ((NoteForegroundColorItem)item).getForegroundColor() == color) {
            			int start = editable.getSpanStart(item);
            			int end = mStartat + changeTextCount;
            			editable.removeSpan(item);
            			NoteForegroundColorItem foreColorItem = new NoteForegroundColorItem(color);
            			editable.setSpan(foreColorItem, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            			isColorHasChanged = true;
            		}
            	}
            	if(isColorHasChanged){
            		resetUrlsColor(editable);
            	}
            	//if color style and text style of the text are changed. skip step3 and for loop.
            	if( isTextStyleHasChanged && isColorHasChanged ) {
                	return;
                }
            }
            
            //step3: if styles have changed, add new sapns(color or textstyle).
            for (NoteItem item : noteItems) {
            	int itemStart = editable.getSpanStart(item);
            	int itemEnd = editable.getSpanEnd(item);
            	
            	if(!isTextStyleHasChanged && item instanceof NoteTextStyleItem) {
            		int itemStyle = ((NoteTextStyleItem)item).getStyle();
	                if(itemStart < beforePosition && itemEnd > afterPosition) {              	  	
	                	if(itemStyle != style){
	                		//remove span锟斤�?	                		editable.removeSpan(item);
							StyleSpan[] ss = editable.getSpans(itemStart, itemEnd, StyleSpan.class);
							for (int i = 0; i < ss.length; i++) {
								editable.removeSpan(ss[i]);
							}
	                		
		                	//span before:
	                		NoteTextStyleItem textStyleItem = new NoteTextStyleItem(itemStyle);
		                	editable.setSpan(textStyleItem, itemStart, beforePosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		                	
                         	//span after:
		                	textStyleItem = new NoteTextStyleItem(itemStyle);
		                	editable.setSpan(textStyleItem, afterPosition, itemEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		                	
		                	//myself:
		                	textStyleItem = new NoteTextStyleItem(style);
		                	editable.setSpan(textStyleItem, beforePosition, afterPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	                	}
	                	isTextStyleHasChanged = true;
	                }
            	}
            	else if(!isColorHasChanged && item instanceof NoteForegroundColorItem) {
            		int itemColor = ((NoteForegroundColorItem)item).getForegroundColor();
            		
            		if(itemStart < beforePosition && itemEnd > afterPosition) {  //特殊情况
                    	if(itemColor != color) {
                    		//remove span:
                    		editable.removeSpan(item);
                    		
	                    	//before:
                    		NoteForegroundColorItem colorElemItem = new NoteForegroundColorItem(itemColor);
	                    	editable.setSpan(colorElemItem, itemStart, beforePosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	                    	
                        	//after:
	                    	colorElemItem = new NoteForegroundColorItem(itemColor);
	                    	editable.setSpan(colorElemItem, afterPosition, itemEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	                    	
	                    	//myself:
	
	                    	colorElemItem = new NoteForegroundColorItem(color);
	                        editable.setSpan(colorElemItem, beforePosition, afterPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    	}
                    	isColorHasChanged = true;
                    }
            	}
            	if(isColorHasChanged){
            		resetUrlsColor(editable);
            	}
            	//all changed, skip for loop.
            	if(isTextStyleHasChanged && isColorHasChanged) {
            		return;
            	}
            }
            
            //emmanual to fix bug 428376 428427
//            if(beforePosition >= afterPosition){
//            	return;
//            }
            //step4: since step1-3 have past. I should create a new span for this text.
            if(!isTextStyleHasChanged) {
        	    NoteTextStyleItem textStyleItem = new NoteTextStyleItem(style);
        	    editable.setSpan(textStyleItem, beforePosition, afterPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            if(!isColorHasChanged && mContentType != NoteItemArray.TEMPLATE_CONTENT_TRAVEL_DATE) {
            	NoteForegroundColorItem foreColorItem = new NoteForegroundColorItem(color);
                editable.setSpan(foreColorItem, beforePosition, afterPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                resetUrlsColor(editable);
            }
        }
        /**
         * 重新设置超链接的颜色。updateTextStyleAndColor会将超链接颜色改为跟其他字符统一的颜色，在这个方法里我们重置超链接颜�?         * @author noah_zhang
         */
        private void resetUrlsColor(Editable editable){
        	int color = mPageEditor.getEditorPaint().getColor();
        	int linkColor = getLinkTextColors().getDefaultColor();//怎么直接获得LinkTextColor?
        	
        	if(color == linkColor)
        		return;
        	UrlForegroundColorSpan[] urlForegroundColorSpans = editable.getSpans(0, editable.length(), UrlForegroundColorSpan.class);
        	for (UrlForegroundColorSpan urlForegroundColorSpan : urlForegroundColorSpans) {
				editable.removeSpan(urlForegroundColorSpan);
			}

        	Pattern pattern = Pattern
    				.compile("(http://|https://){1}[\\w\\.\\-/:]+");
    		Matcher matcher = pattern.matcher(editable);
    		while (matcher.find()) {
    			UrlForegroundColorSpan urlForegroundColorSpan = new UrlForegroundColorSpan(linkColor);
				editable.setSpan(urlForegroundColorSpan, matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    		}
        }
    }
    
    /**
     * 给UrlSpan用的。不做任何事，只是为了识�?     * @author noah_zhang
     *
     */
    public final static class UrlForegroundColorSpan extends ForegroundColorSpan{

		public UrlForegroundColorSpan(int color) {
			super(color);
			// TODO Auto-generated constructor stub
		}
    	
    }
    
    // BEGIN锟斤�?Shane_Wang@asus.com 2012-11-30
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 		switch(keyCode){
 		case KeyEvent.KEYCODE_TAB:
 			return super.onKeyDown('\t', event); // add tab to text
 		default:
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 	// END锟斤�?Shane_Wang@asus.com 2012-11-30
}
