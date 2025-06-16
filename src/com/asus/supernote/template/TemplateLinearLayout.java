package com.asus.supernote.template;

import java.util.ArrayList;
import com.asus.supernote.EditorActivity;
import com.asus.supernote.EditorUiUtility;
import com.asus.supernote.InputManager;
import com.asus.supernote.R;
import com.asus.supernote.SuperNoteApplication;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.editable.NoteEditText;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.NoteSendIntentItem;
import com.asus.supernote.ui.CursorIconLibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class TemplateLinearLayout extends LinearLayout implements OnGestureListener{
   
	private GestureDetector detector; 
	private PageEditor mPageEditor;
	private boolean isEnable = true;
	private boolean isScrolling = false;//RICHARD
	private StylusButtonState mStylusButtonState=null;// jason
	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}

	public void setPageEditor(PageEditor pageEditor){
		mPageEditor = pageEditor;
	}
	
	public TemplateLinearLayout(Context context) {
		super(context);
        detector = new GestureDetector(this);
        if(MethodUtils.isEnableAirview(context)){
        	initAirView(); //by show
        }
	}
	public TemplateLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
        detector = new GestureDetector(this);
        if(MethodUtils.isEnableAirview(context)){
        	initAirView(); //by show
        }
		// TODO Auto-generated constructor stub
	}
	public TemplateLinearLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
        detector = new GestureDetector(this);
        if(MethodUtils.isEnableAirview(context)){
        	initAirView(); //by show
        }
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) 
	{
		// begin jason : is Stylus input only? yes: intercept the event that is not the  TOOL_TYPE_STYLUS
    	if ((!mPageEditor.isDispatchByHandWritingViewEvent())&&MethodUtils.isPenDevice(mContext)) { //smilefish
			if((mPageEditor.getEditorUiUtility().getmIsStylusInputOnly())){
				if (event.getToolType(event.getActionIndex())!=MotionEvent.TOOL_TYPE_STYLUS ) {
					return true;
				}
			}
		}
    	// end jason
    	return super.onInterceptTouchEvent(event);
	};
	
	public void setAirDefaultIcon(){

		if (mPageEditor.getEditorUiUtility().getInputMode() == InputManager.INPUT_METHOD_DOODLE) {
			CursorIconLibrary.tryStylusIcon(
			        (android.hardware.input.InputManager) getContext()
			                .getSystemService(Context.INPUT_SERVICE),
			        this, mDefaultDoodleDrawable);
			if (!CursorIconLibrary.setStylusIcon(this, mDefaultDoodleDrawable)) {
				if (brushView != null) {
					brushView.setBackgroundDrawable(mDefaultDoodleDrawable);
				}
			}
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		//Begin: show_wang@asus.com
		//Modified reason: for airview
		if ((event.getActionMasked()==MotionEvent.ACTION_DOWN)&&(event.getToolType(event.getActionIndex())== MotionEvent.TOOL_TYPE_STYLUS )) {
			if(MethodUtils.isEnableAirview(mContext)){
				addCountDown.cancel();
				removeCountDown.cancel();
				removeAirView();
				
				//emmanual
				if(!IsSpan){
	    			mStylusButtonState.onLongPressedEvent(event);
				}
				
				setAirDefaultIcon(); //smilefish fix bug 636994
			}
		}
		IsFromTouchUpEvent = true;
		//End: show_wang@asus.com
		
		if(isEnable){
			detector.onTouchEvent(event);
			//BEGIN: RICHARD
			if(isScrolling)
			{
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					isScrolling = false;
				}
				return true;
			}
			//END: RICHARD
			return super.dispatchTouchEvent(event);
		}
		else{
		//BEGIN: RICHARD,READONLY CLICK CLICKABLESPAN
			if(event.getDownTime() == 1-event.getEventTime())
			{
				return super.dispatchTouchEvent(event);
			}
			//END: RICHARD
			return false;
		}
	}
	
	private WindowManager wmEraser = null;
	WindowManager.LayoutParams wmEraserParams = null;
	private View mEraserView = null;
	
	public void addEraserView(float x, float y) {
		if(mEraserView == null){
			mEraserView = new ImageView(getContext());
			mEraserView.setBackgroundResource(R.drawable.eraser_stylus_icon);
		}
		if (mEraserView.getParent() == null) {
			Context context = SuperNoteApplication.getContext();
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M 
					&& !Settings.canDrawOverlays(context)) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
						Uri.parse("package:" + context.getPackageName()));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				return;
			}
			initEraserView();
			wmEraserParams.type = WindowManager.LayoutParams.TYPE_PHONE;
			wmEraserParams.format = PixelFormat.RGBA_8888;
			wmEraserParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
			        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			wmEraserParams.gravity = Gravity.START | Gravity.TOP;

			wmEraserParams.x = (int) x;
			wmEraserParams.y = (int) (y - outRect.top);

			int stroke = (int) mPageEditor.getEditorUiUtility().getDoodlePaint().getStrokeWidth();
			wmEraserParams.width = stroke;
			wmEraserParams.height = stroke;
			wmEraserParams.x = wmEraserParams.x - stroke / 2;
			wmEraserParams.y = wmEraserParams.y - stroke / 2;

			wmEraser.addView(mEraserView, wmEraserParams);
		}
	}

	private void initEraserView() {
		wmEraser = (WindowManager) getContext().getApplicationContext()
				.getSystemService("window");
		wmEraserParams = new WindowManager.LayoutParams();
		this.getLocationOnScreen(location);
		this.getWindowVisibleDisplayFrame(outRect);
		mScale = getScale();
	}
	
	public void updateEraserView(float x, float y) {
		if (wmEraserParams == null || wmEraser == null || mEraserView == null) {
			return;
		}
		wmEraserParams.x = (int) x;
		wmEraserParams.y = (int) (y - outRect.top);
		int stroke = (int) mPageEditor.getEditorUiUtility().getDoodlePaint().getStrokeWidth();
		wmEraserParams.x = wmEraserParams.x - stroke / 2;
		wmEraserParams.y = wmEraserParams.y - stroke / 2;

		if (mEraserView.getParent() != null) {
			wmEraser.updateViewLayout(mEraserView, wmEraserParams);
		}
	}
    
	public void removeEraserView() {
		try {
			if (mEraserView != null && mEraserView.getParent() != null) {
				wmEraser.removeView(mEraserView);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
	

	//Begin: show_wang@asus.com
	//Modified reason: for airview
	NoteEditText mAirEdittext;//emmanual
	@Override
	protected boolean dispatchHoverEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(MethodUtils.isEnableAirview(getContext())) { 
			int action = event.getActionMasked();
			float x = event.getX();
			float y = event.getY();
			if ( !IsFromTouchUpEvent ) {
		    	if ( action == MotionEvent.ACTION_HOVER_ENTER) {
	    			IsSpan = checkAndStartClickableSpan((int)x, (int)y, event);
		    		if (IsSpan) {
		    			preSpan = mAirEdittext.getNoteSendIntentItem();
		    		}
		    		addCountDown.cancel();
		    		removeCountDown.cancel();
		    		addCountDown.setViewXY(x,y);
		    		addCountDown.start();
//		    		Log.v("MotionEvent","MotionEvent.ACTION_HOVER_ENTER" );
		    	}
		    	else if ( action == MotionEvent.ACTION_HOVER_MOVE) {
			    		if ( !IsSpan ) {
			    			IsSpan = checkAndStartClickableSpan((int)x, (int)y, event);
			    			if (IsSpan) {
			    				preSpan = mAirEdittext.getNoteSendIntentItem();
			    			}
		    				updateView(x, y); 
		    			} else {
			    			IsSpan = checkAndStartClickableSpan((int)x, (int)y, event);
			    			NoteSendIntentItem temp = mAirEdittext.getNoteSendIntentItem();
			    			if (IsSpan ) {
			    				if (temp!=null&&!temp.equals(preSpan)) {
			    					preSpan = temp;
			    					updateView(x, y);
			    				}
			    			 else {
			    				updateView(x, y);
			    			}}
		    			}
//		    		Log.v("MotionEvent","MotionEvent.ACTION_HOVER_MOVE" );
		    	}
		    	else if ( action == MotionEvent.ACTION_HOVER_EXIT) {
		    		preSpan = null;
		    		removeCountDown.cancel();
		    		addCountDown.cancel();
		    		removeCountDown.start();
		    		Log.v("MotionEvent","MotionEvent.ACTION_HOVER_EXIT" );
		    	}
			}
			else {
				if ( action == MotionEvent.ACTION_HOVER_EXIT) {
					IsFromTouchUpEvent = false;
				}
				if (action == MotionEvent.ACTION_HOVER_ENTER) {
					CursorIconLibrary.setStylusIcon(this, CursorIconLibrary.STYLUS_ICON_FIRST);// jason
				}
			}
			//begin jason
			IsSpan = checkAndStartClickableSpan((int)x, (int)y, event);
			switch (action) {
			case MotionEvent.ACTION_HOVER_ENTER:
			case MotionEvent.ACTION_HOVER_MOVE:
	    		if (!IsSpan) {
	    			mStylusButtonState.onTouchOrHoverEvent(event);
				}
				break;
			case MotionEvent.ACTION_HOVER_EXIT:
				mStylusButtonState.reSetAll();
			default:
				break;
			}
			//end jason
		}
		return super.dispatchHoverEvent(event);
	}
	//End: show_wang@asus.com
	
	//emmanual to fix bug 445841
	private boolean checkAndStartClickableSpan(int x, int y0, MotionEvent event) {
		View view = ((EditorActivity) mContext).findViewById(R.id.first_item);
		ArrayList<NoteEditText> mNoteEditTexts = getAllNoteEditText(view);
		int y = y0;
		for (NoteEditText edittext : mNoteEditTexts) {
			if (mPageEditor.getEditorUiUtility().getInputMode() != InputManager.INPUT_METHOD_DOODLE) {
				if (edittext.getContentType() == NoteItemArray.CONTENT_NOTEEDIT) {
					int offset = mPageEditor.getTemplateLayoutScaleHeight()
					        - mPageEditor.getScrollY();
					if (offset < 0) {
						offset = 0;
					}
					y = y0 - offset;
				} else {
					int[] location = new int[2];
					edittext.getLocationInWindow(location);
					Rect frame = new Rect();
					((Activity) mContext).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
					y = (int) (y0 - location[1] + getResources().getDimension(
					        R.dimen.edit_func_bar_height))
					        + frame.top;
				}
			}
			if (edittext.checkAndStartClickableSpan((int) x, (int) y, event,
			        false)) {
				mAirEdittext = edittext;
				return true;
			}
		}
		mAirEdittext = mPageEditor.getNoteEditText();
		return false;
	}

	private ArrayList<NoteEditText> getAllNoteEditText(View v) {
		if (!(v instanceof ViewGroup)) {
			ArrayList<NoteEditText> viewArrayList = new ArrayList<NoteEditText>();
			if (v instanceof NoteEditText)
				viewArrayList.add((NoteEditText) v);
			return viewArrayList;
		}

		ArrayList<NoteEditText> result = new ArrayList<NoteEditText>();
		ViewGroup vg = (ViewGroup) v;
		for (int i = 0; i < vg.getChildCount(); i++) {
			View child = vg.getChildAt(i);
			ArrayList<NoteEditText> viewArrayList = new ArrayList<NoteEditText>();
			if (v instanceof NoteEditText)
				viewArrayList.add((NoteEditText) v);
			viewArrayList.addAll(getAllNoteEditText(child));
			result.addAll(viewArrayList);
		}
		return result;
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if(e2.getPointerCount()==2)
		{
			isScrolling = true;//RICHARD
			mPageEditor.scrollEditText(Math.round(distanceX), Math.round(distanceY));
		}
		return true;
	}
	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		isScrolling = false;//RICHARD
		return false;
	}
	
	//Begin: show_wang@asus.com
	//Modified reason: for airview
	private float mScale = 1.0f;
	private DismissCountDown addCountDown = null; 
	private DismissCountDown removeCountDown = null; 
	private WindowManager wm = null;
	WindowManager.LayoutParams wmParams = null;
	private View mAirView = null;
	private View mAirParent =null;
	private TextView mNameTextView = null;
	private TextView mSizeTextView = null;
	private TextView mTypeTextView = null;
	private ImageView brushView = null;
	private Context mContext = null;
	private boolean IsSpan = false;
	private NoteSendIntentItem preSpan = null;
	private boolean IsFromTouchUpEvent = false;
    private int location[] = { 0, 0 };
    private Rect outRect = new Rect();
    private int ImgViewHeight = 0, ImgViewWidth = 0;
    private Drawable mIndicatorDrawable = null;
    private Drawable mDefaultDoodleDrawable = null;
    private boolean mFirstHoverIn = true;
    
	private void initAirView() {
		mStylusButtonState=new StylusButtonState();// jason
		mContext = getContext();
		mAirView = View.inflate(mContext, R.layout.editpage_airview, null);
		mNameTextView = (TextView) mAirView.findViewById(R.id.editpage_tipview);
		mSizeTextView=(TextView) mAirView.findViewById(R.id.editpage_size_air);
		mTypeTextView =(TextView) mAirView.findViewById(R.id.editpage_type_air);
		brushView = (ImageView) mAirView.findViewById(R.id.editpage_airview);
		mAirParent =mAirView.findViewById(R.id.editpage_air_parent);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M 
				&& !Settings.canDrawOverlays(mContext)) {
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
					Uri.parse("package:" + mContext.getPackageName()));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		}else{
			wm = (WindowManager) mContext.getApplicationContext()
					.getSystemService("window");
		}
		wmParams = new WindowManager.LayoutParams();
		addCountDown = new DismissCountDown(100, 100, true, 0, 0);
		removeCountDown = new DismissCountDown(10, 1000, false, 0, 0);
	}
	
    private void addAirView(float x, float y) {
    	if(mAirView.getParent()==null) {
			wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
			wmParams.format = PixelFormat.RGBA_8888;
			wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			wmParams.gravity = Gravity.START | Gravity.TOP;
	
			initAirViewPosition ();
			wmParams.x = (int) ((x + location[0]) * mScale) ;
			wmParams.y = (int) (y * mScale + location[1] - outRect.top );

			wmParams.width =  WindowManager.LayoutParams.WRAP_CONTENT;
			wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
	
			if (IsSpan) {
				brushView.setVisibility(View.GONE);
				mAirParent.setVisibility(View.VISIBLE);	
				preSpan = mPageEditor.getNoteEditText().getNoteSendIntentItem();
				if (preSpan!=null) {// update by jason
					mNameTextView.setText(mContext.getString(R.string.name)+": "+preSpan.getFileName());
					mTypeTextView.setText(mContext.getString(R.string.airview_type)+": "+preSpan.getIntentType());
					mSizeTextView.setText(mContext.getString(R.string.preview_size)+getFileSizeReadable(preSpan.getFileSize(mPageEditor)));
				}
			} else {
				mAirParent.setVisibility(View.GONE);
				brushView.setVisibility(View.VISIBLE);
				//emmanual
				if (mFirstHoverIn || mPageEditor.getEditorUiUtility().getInputMode() != InputManager.INPUT_METHOD_DOODLE) {
					mFirstHoverIn = false;
					//begin jason
					if (!CursorIconLibrary.setStylusIcon(this, mIndicatorDrawable)) {
						brushView.setBackgroundDrawable(mIndicatorDrawable);
					}
				//end jason
				}
				
				ImgViewHeight = brushView.getHeight()/2;
				ImgViewWidth = brushView.getWidth()/2;
				wmParams.x = wmParams.x - ImgViewWidth;
				wmParams.y = wmParams.y - ImgViewHeight;
			}
		
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M 
					&& !Settings.canDrawOverlays(mContext)) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
						Uri.parse("package:" + mContext.getPackageName()));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
			}else{
				if(wm == null){
					wm = (WindowManager) mContext.getApplicationContext()
						.getSystemService("window");
				}
				
				wm.addView(mAirView, wmParams);
			}
		}
	}
    
    private void initAirViewPosition () {
		this.getLocationOnScreen(location);
		this.getWindowVisibleDisplayFrame(outRect);
		mScale = getScale();
		mIndicatorDrawable = getAirViewDrawableId();
		if (mDefaultDoodleDrawable == null) {
			mDefaultDoodleDrawable = resIdToDrawable(R.drawable.asus_indicator_default);
		}
    }
    private void updateView(float x, float y) {
		wmParams.x = (int) ((x + location[0]) * mScale) ;
		wmParams.y = (int) (y * mScale + location[1] - outRect.top );
		
		if (IsSpan) {
			brushView.setVisibility(View.GONE);
			mAirParent.setVisibility(View.VISIBLE);
			preSpan = mAirEdittext.getNoteSendIntentItem();
			if (preSpan!=null) {// update by jason
				mNameTextView.setText(mContext.getString(R.string.name)+": "+preSpan.getFileName());
				mTypeTextView.setText(mContext.getString(R.string.airview_type)+": "+preSpan.getIntentType());
				mSizeTextView.setText(mContext.getString(R.string.preview_size)+getFileSizeReadable(preSpan.getFileSize(mPageEditor)));
			}
		} else {
			mAirParent.setVisibility(View.GONE);
			brushView.setVisibility(View.VISIBLE);

			wmParams.x = wmParams.x - ImgViewWidth;
			wmParams.y = wmParams.y - ImgViewHeight;
		}
		if(mAirView.getParent()!=null) {
    		wm.updateViewLayout(mAirView, wmParams);
    	}
    }
    
    private void removeAirView() {
    	try {
			if(mAirView!=null&&mAirView.getParent()!=null) {
				wm.removeView(mAirView);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    public Drawable getAirViewDrawableId() {
    	int inputMode = mPageEditor.getEditorUiUtility().getInputMode();
    	switch (inputMode) {
	    	case InputManager.INPUT_METHOD_SCRIBBLE:
	    		return getHandWriteDrawable();
	    	case InputManager.INPUT_METHOD_KEYBOARD:
	    		return getTypeDrawabel();
	    	case InputManager.INPUT_METHOD_DOODLE:
	    		return getDoodleDrawable();

    	}
    	return mDefaultDoodleDrawable;
    }
    
    private Drawable getHandWriteDrawable() {
        int style = mPageEditor.getEditorUiUtility().getTextStyle();
        int color = mPageEditor.getEditorUiUtility().getTextColor();
        switch (color) {
	        case Color.BLACK:
	        	if (style == Typeface.NORMAL) {
	        		return resIdToDrawable(R.drawable.asus_indicator_write_black_small);
	        	} else {
	        		return resIdToDrawable(R.drawable.asus_indicator_write_black_big);
	        	}
	        case Color.BLUE:
	        	if (style == Typeface.NORMAL) {
	        		return resIdToDrawable(R.drawable.asus_indicator_write_blue_small);
	        	} else {
	        		return resIdToDrawable(R.drawable.asus_indicator_write_blue_big);
	        	}
	        case Color.RED:
	        	if (style == Typeface.NORMAL) {
	        		return resIdToDrawable(R.drawable.asus_indicator_write_red_small);
	        	} else {
	        		return resIdToDrawable(R.drawable.asus_indicator_write_red_big);
	        	}
	        case Color.GREEN:
	        	if (style == Typeface.NORMAL) {
	        		return resIdToDrawable(R.drawable.asus_indicator_write_green_small);
	        	} else {
	        		return resIdToDrawable(R.drawable.asus_indicator_write_green_big);
	        	}
        }
        return mDefaultDoodleDrawable;
    }
    
    private Drawable getDoodleDrawable() {
    	int doodleToolCode = mPageEditor.getEditorUiUtility().getDoodleTool();
    	switch (doodleToolCode) {
    		case DrawTool.NORMAL_TOOL :
    			if (pressState!=null) {// by jason
    				return pressState.getDoodleDrawable(R.drawable.asus_indicator_brushes_rollerpen);
    			}
    			return drawCurrentBrush(R.drawable.asus_indicator_brushes_rollerpen);
    		case DrawTool.NEON_TOOL :
    			if (pressState!=null) {// by jason
    				return pressState.getDoodleDrawable(R.drawable.asus_indicator_brushes_airbrush);
    			}
    			return drawCurrentBrush(R.drawable.asus_indicator_brushes_airbrush);
    		case DrawTool.SCRIBBLE_TOOL :
    			if (pressState!=null) {// by jason
    				return pressState.getDoodleDrawable(R.drawable.asus_indicator_brushes_pen);
    			}
    			return drawCurrentBrush(R.drawable.asus_indicator_brushes_pen);
    		case DrawTool.MARKPEN_TOOL :
    			if (pressState!=null) {// by jason
    				return pressState.getDoodleDrawable(R.drawable.asus_indicator_brushes_marker);
    			}
    			return drawCurrentBrush(R.drawable.asus_indicator_brushes_marker);
    		case DrawTool.WRITINGBRUSH_TOOL :
    			if (pressState!=null) {// by jason
    				return pressState.getDoodleDrawable(R.drawable.asus_indicator_brushes_brush);
    			}
    			return drawCurrentBrush(R.drawable.asus_indicator_brushes_brush);
    		case DrawTool.SKETCH_TOOL :
    			if (pressState!=null) {// by jason
    				return pressState.getDoodleDrawable(R.drawable.asus_indicator_brushes_pencil);
    			}
    			return drawCurrentBrush(R.drawable.asus_indicator_brushes_pencil);
    		case DrawTool.ERASE_TOOL :
    			return getEraserDrawable();
    	}
    	return mDefaultDoodleDrawable;
    }
    
    private Drawable getTypeDrawabel() {
    	return resIdToDrawable(R.drawable.asus_indicator_text);
    }
    
	private Bitmap setDrawableColor(int id) {
  		int color = mPageEditor.getEditorUiUtility().getDoodlePaint().getColor();
  		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
  		int width = bitmap.getWidth();
  		int height = bitmap.getHeight();
  		int[] colors = new int[width * height]; 
  		bitmap.getPixels(colors, 0, width, 0, 0, width, height);
  		for (int i= 0; i < width * height; i++) {
  			colors[i] = (colors[i] & 0xFF000000) | (color & 0x00FFFFFF);
  		}
  		Bitmap newbitmap =  Bitmap.createBitmap(width, height, Config.ARGB_8888);
  		newbitmap.setPixels(colors, 0, width, 0, 0, width, height);
  		return newbitmap;
  	}
  	
  	private Drawable drawCurrentBrush(int resId) {
  		Bitmap bmpTmp = BitmapFactory.decodeResource(getResources(), resId);
  		Bitmap bmpTmp1 = BitmapFactory.decodeResource(getResources(), R.drawable.asus_supernote_function_pen3_line1);
  		Bitmap bitmap = Bitmap.createBitmap(bmpTmp.getWidth(),bmpTmp.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        float strokeWidth = mPageEditor.getEditorUiUtility().getDoodlePaint().getStrokeWidth();
		if ( strokeWidth > 0 && strokeWidth <= 6 ) {

			bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line1);
		}
		else if ( strokeWidth > 6 && strokeWidth <= 12 ) {

			bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line2);
		}
		else if ( strokeWidth > 12 && strokeWidth <= 18 ) {

			bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line3);
		}
		else if ( strokeWidth > 18 && strokeWidth <= 24 ) {

			bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line4);
		}
		else if ( strokeWidth > 24 && strokeWidth <= 30 ) {

			bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line5);
		}
        canvas.drawBitmap(bmpTmp, 0, 0, paint);
  		Matrix matrix = new Matrix();
  		double scale = MetaData.DPI * 1.0 / 160;
  		matrix.setScale((float)scale, (float)scale);
        canvas.drawBitmap(bmpTmp1, 0, 0, paint);
  		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        Drawable drawable = new BitmapDrawable(newbmp);
        return drawable;
  	}
  	
  	private Drawable resIdToDrawable(int resId) {
  		Bitmap bmpTmp = BitmapFactory.decodeResource(getResources(), resId);
  		Drawable drawable = new BitmapDrawable(bmpTmp);
  		return drawable;
  	}
  	
  	private Drawable getEraserDrawable() {
  		float eraserWidth = mPageEditor.getEditorUiUtility().getEraserWidth();
  		if(eraserWidth == MetaData.DOODLE_ERASER_WIDTHS[0]) {
  			return resIdToDrawable(R.drawable.asus_supernote_function_eraser3);
  		} else if(eraserWidth == MetaData.DOODLE_ERASER_WIDTHS[1]) {
  			return resIdToDrawable(R.drawable.asus_supernote_function_eraser2);
  		} else {
  			return resIdToDrawable(R.drawable.asus_supernote_function_eraser1);
  		}
  	}
  	
  	private float getScale () {
  		float scale = 1f;
  		EditorUiUtility editorUiUtility = mPageEditor.getEditorUiUtility();
  		if(editorUiUtility.getDeviceType() > 100) {
			if(editorUiUtility.getContext().getResources().getConfiguration()
					.orientation == Configuration.ORIENTATION_PORTRAIT){
				if(!editorUiUtility.isPhoneSizeMode()){
					scale = MetaData.Scale;
				}
			} 
		}
  		return scale;
  	}
  	
  	public void setClearAirView () {
  		if ( addCountDown != null ) {
  			addCountDown.cancel();
  		}
  		if ( removeCountDown != null ) {
  			removeCountDown.cancel();
  		}
		removeAirView();
		IsFromTouchUpEvent = true;
  	}
  	
  	private class DismissCountDown extends CountDownTimer{
  		private boolean flag = false;
  		private float x = 0f;
  		private float y = 0f;
  		
		public DismissCountDown(long millisInFuture, long countDownInterval, boolean type, float eventx, float eventy) {
			super(millisInFuture, countDownInterval);
			flag = type;
			x = eventx;
			y = eventy;
			// TODO Auto-generated constructor stub
		}

		public void setViewXY(float eventx, float eventy) {
			x = eventx;
			y = eventy;
		}
		
		@Override
		public void onFinish() {	
			if ( flag ) {
				addAirView(x, y);
			} else {
				removeAirView();
			}
		}

		@Override
		public void onTick(long arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	//End: show_wang@asus.com
  	
  	private String getFileSizeReadable(long size){
  		String builder="0B";
  		long s=size/1024;
  		if (s>=1) {
			if ((s/1024)>=1) {
				s/=1024;
				if (s/1024>=1) {
					builder=s/1024+"GB";
				}else {
				  builder=s+"MB";
				}
			}else {
				builder=s+"KB";
			}
		}else if(s>0){// Byte
			builder=size+"B";
		}
  		return builder.toString();
  	}
/***
 * Stylus Button  press discovery
 * by jason
 * =====================================================
 */
  	private IStylusButtonPress pressState=null;
  	
  	public void setStylusButtonPressListener(IStylusButtonPress listener){
  		pressState=listener;
  	}
  	public static interface IStylusButtonPress{
  		void longPress(boolean isTouch);
  		void shortPress(boolean isTouch);
  		Drawable getDoodleDrawable(int brushRes);
		void resetToBrush();//emmanual
  	}
  	protected void longPressHappen(boolean isTouch) {
		if (pressState!=null) {
			pressState.longPress(isTouch);
			mIndicatorDrawable = getAirViewDrawableId();
			if (!CursorIconLibrary.setStylusIcon(this, mIndicatorDrawable)) {				
				if (brushView!=null) {
					brushView.setBackgroundDrawable(mIndicatorDrawable);
				}
			}
		}
		
	}
  	protected void shortPressHappen(boolean isTouch) {
  		if (pressState!=null) {
			pressState.shortPress(isTouch);
			mIndicatorDrawable = getAirViewDrawableId();
			if (!CursorIconLibrary.setStylusIcon(this, mIndicatorDrawable)) {				
				if (brushView!=null) {
					brushView.setBackgroundDrawable(mIndicatorDrawable);
				}
			}
		}
	}
  	
  	//emmanual
  	protected void restoreBrush(){
  		if (pressState!=null) {
  			pressState.resetToBrush();
			mIndicatorDrawable = getAirViewDrawableId();
			if (!CursorIconLibrary.setStylusIcon(this, mIndicatorDrawable)) {				
				if (brushView!=null) {
					brushView.setBackgroundDrawable(mIndicatorDrawable);
				}
			}
  		}
  	}
  	
  	public class StylusButtonState
  	{
  		private final static int SHORTMAXVALUE=19;
  		private final static int SHORTMINVALUE=1;
  		private final static int LONGMINVALUE=20;
  		private int shortPressCount =0;
  		private int longPressCount=0;
  		private final static int BUTTON_TOUCH_IDENTIFY=MotionEvent.BUTTON_SECONDARY;
  		private final static int BUTTON_NO_IDENTIFY=0;
  		private boolean mIsTouch=false;
  		public StylusButtonState() {
			// TODO Auto-generated constructor stub
  			shortPressCount=0;
  			longPressCount=0;
		}
  		public void reSetAll(){
  			reSetShortPress();
  			reSetLongPress();
  			lastState=BUTTON_NO_IDENTIFY;
  		}
  		public void reSetShortPress(){
  			shortPressCount=0;
  		}
  		public void reSetLongPress(){
  			longPressCount=0;
  		}
  		private int lastState=BUTTON_NO_IDENTIFY;
  		public void onTouchOrHoverEvent(MotionEvent event){
  			if (event==null) {
				return;
			}
  			final int _state=event.getButtonState();
  			if ( (_state != BUTTON_NO_IDENTIFY)&&(lastState == _state)) {
				if (lastState == BUTTON_TOUCH_IDENTIFY) {
					mIsTouch=true;
				}else {
					mIsTouch=false;
				}
				++shortPressCount;
				++longPressCount;
				if (longPressCount>=LONGMINVALUE) {
					longPressHappen(mIsTouch);
					reSetLongPress();
					return;
				}
			}else if((_state == BUTTON_NO_IDENTIFY) && (lastState != BUTTON_NO_IDENTIFY)) {
				if (lastState == BUTTON_TOUCH_IDENTIFY) {
					mIsTouch=true;
				}else {
					mIsTouch=false;
				}
				if (shortPressCount<=SHORTMAXVALUE&&shortPressCount>=SHORTMINVALUE) {
					shortPressHappen(mIsTouch);
				}
				if (longPressCount >= LONGMINVALUE) {
					longPressHappen(mIsTouch);
				}
				reSetAll();
				return;
			}else if ((lastState == BUTTON_NO_IDENTIFY) && (BUTTON_NO_IDENTIFY != _state)) {// first come in
				lastState=_state;
				if (_state == BUTTON_TOUCH_IDENTIFY) {
					mIsTouch=true;
				}else {
					mIsTouch=false;
				}
				shortPressCount++;
				longPressCount++;
			}
  		}

  		//begin emmanual
  		private int lastPressState=BUTTON_NO_IDENTIFY;
  		public void onLongPressedEvent(MotionEvent event){
  			if (event==null) {
				return;
			}
  			final int _state=event.getButtonState();
  			if ((_state == MotionEvent.BUTTON_TERTIARY)) {// first come in
  				lastPressState=_state;
				longPressHappen(mIsTouch);
  			}else if ((lastPressState == MotionEvent.BUTTON_TERTIARY) && (_state == BUTTON_NO_IDENTIFY)){
  				lastPressState = BUTTON_NO_IDENTIFY;
  				restoreBrush();
  			}
  		}
  		//end emmanual
  	}
}
