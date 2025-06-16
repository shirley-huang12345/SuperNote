package com.asus.supernote.editable;

import java.util.LinkedList;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import com.asus.supernote.EditorActivity;
import com.asus.supernote.EditorUiUtility;
import com.asus.supernote.PaintSelector;
import com.asus.supernote.R;
import com.asus.supernote.SuperNoteApplication;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.doodle.drawtool.AirBrushDrawTool;
import com.asus.supernote.doodle.drawtool.BrushDrawTool;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawtool.EraseDrawTool;
import com.asus.supernote.doodle.drawtool.MarkerDrawTool;
import com.asus.supernote.doodle.drawtool.PathDrawTool;
import com.asus.supernote.doodle.drawtool.PenDrawTool;
import com.asus.supernote.doodle.drawtool.PencilDrawTool;
import com.asus.supernote.doodle.drawtool.SelectionDrawTool;
import com.asus.supernote.picker.PickerUtility;

public class PageEditorManager {
	PageEditor mFirstPageEditor = null;
	PageEditor mCurrentPageEditor = null;	
	private final EditorUiUtility mEditorUiUtility;
	private final LinearLayout pageEditorManagerLinaerLayout;
	private LinkedList<PageEditor> mPageEditorList;
	private int deviceType = 0; 
	
	//begin emmanual to fix bug 415028
	private int mInsertState = 0;//1:stamp
	
	public int getInsertState() {
		return mInsertState;
	}

	public void setInsertState(int state) {
		this.mInsertState = state;
	}
	//end emmanual

	public LinkedList<PageEditor> getPageEditorList()
	{
		return mPageEditorList;
	}

	public void SetTwoPageMode(Boolean Mode)
	{						
			mFirstPageEditor.getLayout().requestLayout();
			mFirstPageEditor.getDoodleView().requestLayout();
			//some clear may need do.
			if(mEditorUiUtility.getDeviceType() > 100)
			{
				if(mEditorUiUtility.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
				{
					mFirstPageEditor.setScale(1f,1f);
				}
				else
				{				
					if(!mEditorUiUtility.isPhoneSizeMode())
					{
						mFirstPageEditor.setScale(MetaData.Scale,MetaData.Scale);
					}
					else
					{
						mFirstPageEditor.setScale(1f,1f);				
					}
				}
			}
			else
			{
				mFirstPageEditor.setScale(1f,1f);		
			}
	}
	
    public PageEditorManager(EditorUiUtility utility) {
    	mEditorUiUtility = utility;
    	NoteFrameLayout firstLayout = (NoteFrameLayout) ((Activity) mEditorUiUtility.getContext()).findViewById(R.id.first_item);
    	
    	mFirstPageEditor = new PageEditor(this,utility,firstLayout);
    	mCurrentPageEditor = mFirstPageEditor;

    	mPageEditorList = new LinkedList<PageEditor>();
    	mPageEditorList.add(mFirstPageEditor);
    	SetTwoPageMode(false);
    	
    	pageEditorManagerLinaerLayout = (LinearLayout)((Activity) mEditorUiUtility.getContext()).findViewById(R.id.pageEditorManagerLinaerLayout);

    	//smilefish fix bug 380042
    	deviceType = PickerUtility.getDeviceType(SuperNoteApplication.getContext());
    	if(deviceType == MetaData.DEVICE_TYPE_320DP){
    		mFirstPageEditor.getLayout().requestLayout();
            mFirstPageEditor.getDoodleView().requestLayout();
            mFirstPageEditor.setScale(0.85f, 0.85f);
    	}
    	//end smilefish
    }
    
    
    public PageEditor getFirstPageEditor()
    {
    	return mFirstPageEditor;
    }

    
    public PageEditor getCurrentPageEditor()
    {
    	return mCurrentPageEditor;
    }
    
    public void setCurrentPageEditor(PageEditor pe)
    {
    	mCurrentPageEditor = pe;
    }
    
	private Boolean isClickOnView(View vi,MotionEvent event)
	{
		if(vi == null)
		{
			return false;
		}
		
		int[] location = new  int[2] ;
		vi.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
		
		float absX = event.getRawX(); 
		float absY = event.getRawY(); 
		
		if(absX > location[0] && absX < location[0] + vi.getMeasuredWidth()
				&& absY > location[1] && absY < location[1] + vi.getMeasuredHeight())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
    
    public PageEditor setCurrentPageEditor(MotionEvent event)
    {
    	
    	for(PageEditor pe:mPageEditorList)
    	{
        	if(isClickOnView(pe.getLayout(),event))
        	{
        		setCurrentPageEditor(pe);
        		break;
        	}
    	}    
    	
    	return mCurrentPageEditor;
    }
    
    
    public void setNoteEditTextEnable(boolean enable) {
    	for(PageEditor pe:mPageEditorList)
    	{
    		pe.setNoteEditTextEnable(enable);
    	}
    }   
    
    public void enableDoodleView(boolean enable) {
    	for(PageEditor pe:mPageEditorList)
    	{
    		pe.enableDoodleView(enable);
    	} 	
    }
    
    public Boolean dispatchPointerEventToScroll(MotionEvent event)
    {
    	Boolean res = false;
    	for(PageEditor pe:mPageEditorList)
    	{
        	res = pe.getPageEditorScrollBar().dispatchPointerEventToScroll(event);
        	if(res)
        	{
        		return true;
        	}
    	} 	
    	return res;
    }
    
    public void dispatchShortPressPointerMessage(MotionEvent event)
    {
        MotionEvent down = MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, event.getX(), event.getY(), 0);
        pageEditorManagerLinaerLayout.dispatchTouchEvent(down);         
        MotionEvent up = MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,event.getX(), event.getY(), 0);         
        pageEditorManagerLinaerLayout.dispatchTouchEvent(up);           	
    }
    
    public void dispatchLongPressPointerMessage(MotionEvent event)
    {
    	long time = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(time,-time, event.getAction(), event.getX(), event.getY(), 0);
        down.setSource(0);
        pageEditorManagerLinaerLayout.dispatchTouchEvent(down);           
        //share.dispatchTouchEvent(up);     	
    }
    
    public void resetAllDoodleViewLastLongClickPos()
    {
    	mFirstPageEditor.getDoodleView().resetLastLongClick();
    }
    
    public void resumeScrollChange() {

        // when click audio or video, resume back. The scroller position will be wrong.
    	for(PageEditor pe:mPageEditorList)
    	{
    		pe.ScrollViewTo(pe.getScrollX(), pe.getScrollY(),true);
    	}
    }
    
    public void unbindResources() {
    	for(PageEditor pe:mPageEditorList)
    	{
    		pe.unbindResources();
    	}
    	
    	mDialogManager.sendEmptyMessage(MSG_HIDE_DRAWING_DIALOG); //fix bug 305380 by smilefish
    }
    
    public void cleanEdited() {
    	for(PageEditor pe:mPageEditorList)
    	{
    		pe.cleanEdited(false);//Allen
    	}
    }
    
    public void quitSelectionTextMode() {
    	for(PageEditor pe:mPageEditorList)
    	{
    		pe.quitSelectionTextMode();
    	}
    }
    
    public void cancleBoxEditor() {
    	for(PageEditor pe:mPageEditorList)
    	{
    		pe.cancleBoxEditor();
    	}
    }
    
    public void setPaintTool(int toolCode) {
    	for(PageEditor pe:mPageEditorList)
    	{
        	Paint paint = pe.getDoodlePaint();
            switch (toolCode) {
            case DrawTool.NORMAL_TOOL:
            	mEditorUiUtility.setEraserStatus(false);//by show

            	pe.setPaintTool(new PathDrawTool(PathDrawTool.NORMAL_TOOL));
            	
                PaintSelector.setNormal(paint);
                break;
            case DrawTool.NEON_TOOL:
            	mEditorUiUtility.setEraserStatus(false);//emmanual to fix bug 471597,471425,470715,466127,467007
	            	//Carrot: new version of pens
	            	pe.setPaintTool(new AirBrushDrawTool());
          	
                PaintSelector.setNeon(paint);
                break;
            case DrawTool.SELECTION_TOOL:
            	pe.setPaintTool(new SelectionDrawTool(mEditorUiUtility.getContext()));
                break;
            case DrawTool.ERASE_TOOL:
            	mEditorUiUtility.setEraserStatus(true);//by show
            	pe.setPaintTool(new EraseDrawTool());
                PaintSelector.setErase(paint);
                break;
            case DrawTool.SCRIBBLE_TOOL:
            	mEditorUiUtility.setEraserStatus(false);//by show
	            	//Carrot: new version of pens
	            	pe.setPaintTool(new PenDrawTool(PathDrawTool.SCRIBBLE_TOOL));
                PaintSelector.setScribble(paint);
                break;
            case DrawTool.SKETCH_TOOL:
            	mEditorUiUtility.setEraserStatus(false);//by show
	            	//Carrot: new version of pens
	            	pe.setPaintTool(new PencilDrawTool());
                PaintSelector.setSketch(paint);
                break;
				//begin wendy
            case DrawTool.MARKPEN_TOOL:
            	mEditorUiUtility.setEraserStatus(false);//by show
	            	//Carrot: new version of pens
	            	pe.setPaintTool(new MarkerDrawTool(DrawTool.MARKPEN_TOOL));
            	PaintSelector.setMarkPen(paint);
            	changeAlpha(((EditorActivity)(mEditorUiUtility.getContext())).getDoodleToolAlpha());
            	break;
            case DrawTool.WRITINGBRUSH_TOOL:
            	mEditorUiUtility.setEraserStatus(false);//by show
	            	//Carrot: new version of pens
	            	pe.setPaintTool(new BrushDrawTool(PathDrawTool.WRITINGBRUSH_TOOL));
            	PaintSelector.setWritingBrush(paint);
            	break;
				//end wendy
            }
    	}    
	} 
    
    public void changeColor(int color) {
    	for(PageEditor pe:mPageEditorList)
    	{
    		PaintSelector.setColor(pe.getPaint(), color);
    		pe.onColorChange();
    	}  
    }
    
    public void changeTextColor(int color) 
    {
    	for(PageEditor pe:mPageEditorList)
    	{
            PaintSelector.setColor(pe.getEditorPaint(), color);
            pe.onColorChange();
    	} 
    }
    
    public void changeTextStyle(int style) {
    	for(PageEditor pe:mPageEditorList)
    	{
            float width = style == Typeface.BOLD ? MetaData.SCRIBBLE_PAINT_WIDTHS_BOLD : MetaData.SCRIBBLE_PAINT_WIDTHS_NORMAL;
            PaintSelector.setPaintWidth(pe.getPaint(), width);

            pe.setTextStyle(style);
            pe.onStyleAndStrokeChange();
    	}     
    }
    

    public void changeScribleStroke(float width) {
    	for(PageEditor pe:mPageEditorList)
    	{
            PaintSelector.setPaintWidth(pe.getPaint(), width);
            pe.onColorChange();
    	} 
    } 

    public void changeAlpha(int alpha) {
    	for(PageEditor pe:mPageEditorList)
    	{
    		PaintSelector.setAlpha(pe.getDoodlePaint(), alpha);//Modified by show
    	} 
    }
    
    public void reflashScreen()
    {
    	for(PageEditor pe:mPageEditorList)
    	{
        	pe.reflashScreen();
    	} 
    }
    
    public void setReadOnlyViewStatus(Boolean status)
    {
    	for(PageEditor pe:mPageEditorList)
    	{
        	pe.setReadOnlyViewStatus(status);
    	} 
    }

	public void requestNextOrPrevPage(Boolean flag) {
		if(flag)
		{
			mEditorUiUtility.nextPage();
		}else
		{
			mEditorUiUtility.prevPage();
		}
		
	}
	
	public void setMicroViewVisible(boolean visible){
		for(PageEditor pe:mPageEditorList)
    	{
        	pe.setMicroViewVisible(visible);
    	} 
	}
	public Boolean dispatchTouchEventToMicroView(MotionEvent event)
    {
    	for(PageEditor pe:mPageEditorList)
    	{
        	if(pe.dispatchTouchEventToMicroView(event))
        	{
        		return true;
        	}
    	} 	
    	return false;
    }
	//End Allen
	
	//begin  darwin
	public void setSelectAllText()
	{
		for(PageEditor pe:mPageEditorList)
    	{
        	pe.setSelectAllText();
    	}
	}
	//end   darwin
	
	public void startLoadingPage()
	{
		for(PageEditor pe:mPageEditorList)
    	{
			pe.beginLoad();
    	}
	}
	
	public void showLoadingDialog()
	{
		mDialogManager.sendEmptyMessage(MSG_SHOW_LOADING_DIALOG);
	}
	
	public void showDrawingDialog()
	{
		//begin smilefish fix bug 305380
		Context context = mEditorUiUtility.getContext();
		SharedPreferences preference= context.getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		boolean isFirstLoad = preference.getBoolean(context.getString(R.string.pref_first_load), true);
		if(!isFirstLoad){
			mDialogManager.sendEmptyMessage(MSG_SHOW_DRAWING_DIALOG);
		}
		//end smilefish
	}
	
	public void hideDrawingDialog()
	{
		mDialogManager.sendEmptyMessage(MSG_HIDE_DRAWING_DIALOG);
	}
	
	public void onPageEditorLoadComplete()
	{
		for(PageEditor pe:mPageEditorList)
    	{
        	if(!pe.getIsLoadComplete())
        	{
        		//some pageEditor is not ready
        		return;
        	}        	
    	} 
		mDialogManager.sendEmptyMessage(MSG_HIDE_LOADING_DIALOG);
	}
	
    private static final int MSG_SHOW_DRAWING_DIALOG = 1;
    private static final int MSG_HIDE_DRAWING_DIALOG = 2;
    private static final int MSG_SHOW_LOADING_DIALOG = 3;
    private static final int MSG_HIDE_LOADING_DIALOG = 4;
    //add by mars for delay loading
    private static final int MSG_DELAY_DRAWING_SHOW = 5;
    private static final int MSG_DELAY_LOADING_SHOW = 6;
    private static final int MSG_DELAY_CHECK_VOICE = 7; //smilefish
    private Handler mDialogManager = new Handler()
    {
    	private boolean needShowLoading = false;
    	private boolean needShowDrawing = false;
        @Override
        public void handleMessage(Message msg) {
        	try {
	        	switch (msg.what) {
	        	case MSG_SHOW_DRAWING_DIALOG:
	        		needShowDrawing = true;
	        		this.sendEmptyMessageDelayed(MSG_DELAY_DRAWING_SHOW, 300);
	        		break;
	        	case MSG_HIDE_DRAWING_DIALOG:
	        		needShowDrawing = false;
	        		((Activity)mEditorUiUtility.getContextStatic()).removeDialog(EditorActivity.DRAWING_PROGRESS_DIALOG);
	        		this.sendEmptyMessageDelayed(MSG_DELAY_CHECK_VOICE, 300); //smilefish fix bug 312547
	        		break;
	        	case MSG_SHOW_LOADING_DIALOG:
	        		needShowLoading = true;
	        		this.sendEmptyMessageDelayed(MSG_DELAY_LOADING_SHOW, 400);
	        		break;
	        	case MSG_HIDE_LOADING_DIALOG:
	        		needShowLoading = false;
	        		((Activity)mEditorUiUtility.getContextStatic()).removeDialog(EditorActivity.LOADING_PROGRESS_DIALOG);
	        		break;
	        	case MSG_DELAY_LOADING_SHOW:
	        		 //add by mars for delay loading
	        		if(needShowLoading){
	        			((Activity)mEditorUiUtility.getContextStatic()).showDialog(EditorActivity.LOADING_PROGRESS_DIALOG);
	        		}
	        		break;
	        	case MSG_DELAY_DRAWING_SHOW:
	        		 //add by mars for delay loading
	        		if(needShowDrawing){
	        			((Activity)mEditorUiUtility.getContextStatic()).showDialog(EditorActivity.DRAWING_PROGRESS_DIALOG);
	        		}
	        		break;
	        	case MSG_DELAY_CHECK_VOICE:
	        		((EditorActivity)mEditorUiUtility.getContextStatic()).checkVoiceInput();
	        		break;
	        	}
        	}catch(Exception e){
        		Log.e("HandleMessage Error", "Context may be destroyed. Context is null");
        	}
        }
    };
	
	
}
