package com.asus.supernote.editable;

import java.util.LinkedList;

import com.asus.supernote.EditorUiUtility;
import com.asus.supernote.InputManager;
import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NotePageValue;
import com.asus.supernote.picker.PickerUtility;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.widget.EditText;

public class PageEditorWrapper {
	public interface HistoryListener {
		public void onUndoStackAvailable(boolean available);
		public void onRedoStackAvailable(boolean available);
	}
	//begin jason
	public interface IUIListener{
		public void onModify(boolean b);
		public void onShapeRecognizeFailed();
	}
	//end jason
	private PageEditor mPageEditor = null;
	
	private int mNumPages = 1;
	private int mViewHeight = 0;
	private float mScaleX = 1.0f;
	private float mScaleY = 1.0f;
	private int mDoodleItemWidth = 0;
	private int mDoodleItemHeight = 0;
	private int mTemplateLayoutHeight = 0;
	private int mTemplateLinearLayoutScrollY = 0;
	private Context mContext = null;
	private String mPageFilePath = null;
	private int mInputMode = InputManager.INPUT_METHOD_DOODLE;
	private boolean mIsDoodleBackgroundTransparent = false;
	private HistoryListener mHistoryListener = null;
	private boolean mDisplayLine = true;
	//begin jason
	private LinkedList<IUIListener> mUIListeners=new LinkedList<IUIListener>();
	//end jason
	public static final int GRID_COLOR = 0x602CE8FF;
	public static final int GRID_SPACING = 15;
	
	public PageEditorWrapper(PageEditor pageEditor) {
		mPageEditor = pageEditor;
	}
	
	public PageEditor getPageEditor() {
		return mPageEditor;
	}
	
	public void setDisplayLine(boolean display) {
		mDisplayLine = display;
	}
	
	public void setHistoryListener(HistoryListener listener) {
		mHistoryListener = listener;
	}
	//begin jason
	public void addUIListener(IUIListener listener){
		if (listener==null) {
			return;
		}
		mUIListeners.add(listener);
	}
	//end jason
	public int getDoodleItemWidth() {
		if (mPageEditor != null) {
			return mPageEditor.getDoodleItemWidth();
		} else {
			return mDoodleItemWidth;
		}
	}
	
	public void setDoodleItemWidth(int width) {
		if (mPageEditor != null) {
			mPageEditor.setDoodleItemWidth(width);
		} else {
			mDoodleItemWidth = width;
		}
	}
	
	public int getDoodleItemHeight() {
		if (mPageEditor != null) {
			return mPageEditor.getDoodleItemHeight();
		} else {
			return mDoodleItemHeight;
		}
	}
	
	public void setDoodleItemHeight(int height) {
		if (mPageEditor != null) {
			mPageEditor.setDoodleItemHeight(height);
		} else {
			mDoodleItemHeight = height;
		}
	}
	
	public boolean isDoodleBackroundTransparent() {
		return mIsDoodleBackgroundTransparent;
	}
	
	public void setDoodleBackgroundTransparent(boolean isTransparent) {
		mIsDoodleBackgroundTransparent = isTransparent;
	}
	
	public static float getEditorFontSize(Context context, boolean isSmallScreen, int fontSize) {
		return NotePageValue.getFontSize(context, fontSize, isSmallScreen);
	}
	
	public static int getEditorFirstLineHeight(Context context, boolean isSmallScreen) {
    	if (isSmallScreen) {
            return context.getResources().getInteger(R.integer.first_line_height_small_screen);
    	} else {
    		return context.getResources().getInteger(R.integer.first_line_height);
    	}
    }
	
	public static Point getEditorSize(Context context, boolean isSmallScreen, int fontSize) {
		int firstLineHeight = getEditorFirstLineHeight(context, isSmallScreen);
        int lineCountLimited = NotePageValue.getLineCountLimited(context, fontSize, isSmallScreen);
        EditText editText = new EditText(context);
        editText.setText("ABCDEFGHIJKLMNOPQRSTabcdefghijklmnopqrst");
        editText.setGravity(Gravity.START | Gravity.TOP | Gravity.CENTER_VERTICAL | Gravity.CENTER_VERTICAL);
        editText.setTextColor(Color.BLACK);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getEditorFontSize(context, isSmallScreen, fontSize));
        editText.setSingleLine(false);
        editText.setPadding(0, firstLineHeight, 0, 0);
        editText.setLineSpacing(0, PickerUtility.getLineSpace(context)); //smilefish
        int width = (int) NotePageValue.getBookWidth(context, isSmallScreen);
        editText.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), 
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        return new Point(width, editText.getLineHeight() * lineCountLimited + firstLineHeight);
	}
	
	public int getPageNum() {
		if (mPageEditor != null) {
			return mPageEditor.getPageNum();
		} else {
			return mNumPages;
		}
	}
	
	public void setPageNum(int num) {
		if (mPageEditor != null) {
			mPageEditor.setPageNum(num);
		} else {
			mNumPages = num;
		}
	}
	
	public int getViewHeight() {
		if (mPageEditor != null) {
			return mPageEditor.getViewHeight();
		} else {
			return mViewHeight;
		}
	}
	
	public void setViewHeight(int height) {
		mViewHeight = height;
	}
	
	public float getScaleX() {
		if (mPageEditor != null) {
			return mPageEditor.getScaleX();
		} else {
			return mScaleX;
		}
	}

	public float getScaleY() {
		if (mPageEditor != null) {
			return mPageEditor.getScaleY();
		} else {
			return mScaleY;
		}
	}
	
	public void setScale(float scaleX, float scaleY) {
		if (mPageEditor != null) {
			mPageEditor.setScale(scaleX, scaleY);
		} else {
			mScaleX = scaleX;
			mScaleY = scaleY;
		}
	}
	
	public int getTemplateLayoutScaleHeight() {
		if (mPageEditor != null) {
			return mPageEditor.getTemplateLayoutScaleHeight();
		} else {
			return (int) (mTemplateLayoutHeight * mScaleY);
		}
	}
	
	public void setTemplateLayoutHeight(int height) {
		if (mPageEditor != null) {
			mPageEditor.setTemplateLayoutHeight(height);
		} else {
			mTemplateLayoutHeight = height;
		}
	}
	
	public int getTemplateLinearLayoutScrollY() {
		if (mPageEditor != null) {
			return mPageEditor.getTemplateLinearLayoutScrollY();
		} else {
			return mTemplateLinearLayoutScrollY;
		}
	}
	
	public void setTemplateLinearLayoutScrollY(int scrollY) {
		mTemplateLinearLayoutScrollY = scrollY;
	}
	
	public int getInputMode() {
		if (mPageEditor != null) {
			return mPageEditor.getEditorUiUtility().getInputMode();
		} else {
			return mInputMode;
		}
	}
	
	public void setInputMode(int mode) {
		if (mPageEditor != null) {
			mPageEditor.getEditorUiUtility().setInputMode(mode);
		} else {
			mInputMode = mode;
		}
	}
	
	public void setDoodleItemSelect(boolean somethingSelected, boolean multiple, boolean group) {
		if (mPageEditor != null) {
			mPageEditor.setDoodleItemSelect(somethingSelected, multiple, group);
		}
	}
	
	public void setDoodleItemEmpty(boolean isEmpty) {
		if (mPageEditor != null) {
			mPageEditor.setDoodleItemEmpty(isEmpty);
		}
	}
	
	public void setDoodleUndoEmpty(boolean isEmpty) {
		if (mPageEditor != null) {
			mPageEditor.setDoodleUndoEmpty(isEmpty);
		} else {
			mHistoryListener.onUndoStackAvailable(!isEmpty);
		}
	}
	
	public void setDoodleRedoEmpty(boolean isEmpty) {
		if (mPageEditor != null) {
			mPageEditor.setDoodleRedoEmpty(isEmpty);
		} else {
			mHistoryListener.onRedoStackAvailable(!isEmpty);
		}
	}
	
	public void setDoodleCropButtonsEnable(boolean enable) {
		if (mPageEditor != null) {
			mPageEditor.setDoodleCropButtonsEnable(enable);
		}
	}
	
	public void setDoodleTextEditButtonsEnable(boolean enable) {
		if (mPageEditor != null) {
			mPageEditor.setDoodleTextEditButtonsEnable(enable);
		}
	}
	
	public Context getContext() {
		if (mPageEditor != null) {
			return mPageEditor.getEditorUiUtility().getContext();
		} else {
			return mContext;
		}
	}
	
	public void setContext(Context context) {
		mContext = context;
	}
	
	public String getFilePath() {
		if (mPageEditor != null) {
			return mPageEditor.getFilePath();
		} else {
			return mPageFilePath;
		}
	}
	
	public void setFilePath(String path) {
		mPageFilePath = path;
	}
	
	public int getTemplateType() {
		if (mPageEditor != null) {
			return mPageEditor.getTemplateType();
		} else {
			return MetaData.Template_type_normal;
		}
	}
	
	public void setIsAttachmentModified(boolean isModified) {
		if (mPageEditor != null) {
			mPageEditor.setIsAttachmentModified(isModified);
		}
	}
	
	public void updateAttachmentRemoveNameList(String name) {
		if (mPageEditor != null) {
			mPageEditor.updateAttachmentRemoveNameList(name);
		}
	}
	
	public void updateAttachmentAddNameList(String name) {
		if (mPageEditor != null) {
			mPageEditor.updateAttachmentAddNameList(name);
		}
	}
	
	public void addToAttachmentNameList(String fileName) {
		if (mPageEditor != null) {
			mPageEditor.getAttachmentNameList().add(fileName);
		}
	}
	
	public void setDoodleModified() {
		if (mPageEditor != null) {
			mPageEditor.setDoodleModified();
		}
	}
	
	public void onModified(boolean isModified) {
		if (mPageEditor != null) {
			mPageEditor.onModified(isModified);
		}
		//begin jason
		for (IUIListener listener : mUIListeners) {
			listener.onModify(isModified);
		}
		//end jason
	}
	
	public boolean isTouchOnTemplateRect(MotionEvent event) {
		if (mPageEditor != null) {
			return mPageEditor.isTouchOnTemplateRect(event);
		} else {
			return false;
		}
	}
	
	public void backToPreviousMode() {
		if (mPageEditor != null) {
			mPageEditor.backToPreviousMode();
		}
	}
	
	public boolean drawNoteEditTextLine(Canvas canvas, int page,boolean bIsShare) {
		if (mPageEditor != null) {
			return mPageEditor.drawNoteEditTextLine(canvas, page,bIsShare);
		} else {
			if (mDisplayLine) {
	            Paint linePaint = new Paint();
	            linePaint.setColor(GRID_COLOR);
	            linePaint.setStrokeWidth(NoteEditText.NOTE_GRID_LINE_WIDTH);
	            for (int i = 0; i < canvas.getHeight(); i += GRID_SPACING) {
	                int drawLine = i;
	                canvas.drawLine(0, drawLine, canvas.getWidth(), drawLine, linePaint);
	            }
	            for (int i = 0; i < canvas.getWidth(); i += GRID_SPACING) {
	                int drawLine = i;
	                canvas.drawLine(drawLine, 0, drawLine, canvas.getHeight(), linePaint);
	            }
			}
			return true;
		}
	}
	
	public void drawTemplateView(Canvas canvas) {
		if (mPageEditor != null) {
			mPageEditor.drawTemplateView(canvas);
		}
	}
	
	public void drawNoteEditText(Canvas canvas, int scrollX, int scrollY) {
		if (mPageEditor != null) {
			mPageEditor.drawNoteEditText(canvas, scrollX, scrollY);
		}
	}
	
	public void requestToBeCurrent() {
		if (mPageEditor != null) {
			mPageEditor.requestToBeCurrent();
		}
	}
	
	public void setMicroViewVisible(boolean visible) {
		if (mPageEditor != null) {
			mPageEditor.setMicroViewVisible(visible);
		}
	}
	
	public void ScrollViewTo(int scrollX, int scrollY, boolean resetPos) {
		if (mPageEditor != null) {
			mPageEditor.ScrollViewTo(scrollX, scrollY, resetPos);
		}
	}
	
	public void scrollEditText(int scrollX, int scrollY) {
		if (mPageEditor != null) {
			mPageEditor.scrollEditText(scrollX, scrollY);
		}
	}
    
	//Begin: show_wang@asus.com
    //Modified reason: for dds
    public Boolean getConfigStatus(){
    	if (mPageEditor != null) {
    		return mPageEditor.getConfigStatus();
    	} else {
    		return false;
    	}
    }
    
	public void setDoodleGroupButtonsEnable(boolean enable) {
		if (mPageEditor != null) {
			mPageEditor.setDoodleGroupButtonsEnable(enable);
		}
	}
	
	public void setDoodleUnGroupButtonsEnable(boolean enable) {
		if (mPageEditor != null) {
			mPageEditor.setDoodleUnGroupButtonsEnable(enable);
		}
	}
    //End: show_wang@asus.com
    
	public void setDrawingDialogStatus(Boolean show)
	{
		if (mPageEditor != null) {
			mPageEditor.setDrawingDialogStatus(show);
		}
	}
	// begin jason
	public EditorUiUtility getEditorUiUtility()
	{
		if (mPageEditor!=null) {
			return mPageEditor.getEditorUiUtility();
		}
		return null;
	}
	//end 
	
    //begin smilefish
	public void showShapeFailedToast()
	{
		if(mPageEditor!=null)
			mPageEditor.showShapeFailedToast();
		//begin jason
		for (IUIListener uiListern : mUIListeners) {
			uiListern.onShapeRecognizeFailed();
		}
		//end 
	}
    //end smilefish
}
