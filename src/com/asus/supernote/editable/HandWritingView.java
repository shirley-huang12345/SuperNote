package com.asus.supernote.editable;

import java.util.LinkedList;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import com.asus.supernote.PaintSelector;
import com.asus.supernote.R;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.editable.noteitem.NoteForegroundColorItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteBaselineItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteTextStyleItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem.PathInfo;
import com.asus.supernote.inksearch.AsusInputRecognizer;
import com.asus.supernote.inksearch.CFG;

public class HandWritingView extends View implements OnGestureListener, IHandWritePanel{
    private class DrawingCountDown extends CountDownTimer {
        private static final long ANIMATION_TIME = 50;
        private ValueAnimator mAnimation = null;

        public DrawingCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void startCountDown(boolean isSendTextDirectly) {
        	//BEGIN: RICHARD TEST
        	mPageEditor = mPageEditorManager.getCurrentPageEditor();
        	//END: RICHARD TEST
            // BEGIN: archie_huang@asus.com
            // Remove Pad Handwrite shift mechanism
        	//Begin: show_wang@asus.com
        	//Modified reason: Remove Phone LandSpace Handwrite shift mechanism
        	int IsContinueWrite = getResources().getInteger(R.integer.is_continue_write); 
//            if (mPageEditor.getDeviceType()>100 || isSendTextDirectly) {
        	if ( IsContinueWrite == 0 || isSendTextDirectly) {
                if (mSendTextCountDown != null) {
                    mSendTextCountDown.start();
                }
            }
            else {
                start();
            }
            // END: archie_huang@asus.com
        }

        public void cancelCountDown() {
            cancel();
            if (mSendTextCountDown != null) {
                mSendTextCountDown.cancel();
            }
        }
        
        public void cancelCountDownAndAnimation() {
        	cancel();
            if (mSendTextCountDown != null) {
                mSendTextCountDown.cancel();
            }
            if (mAnimation != null) {
            	mAnimation.cancel();
            }
        }

        @Override
        public void onFinish() {
            if (DEBUG) {
                Log.d(TAG, "DrawingCountDown.onFinish()");
            }

            if (mHistPathList.isEmpty()) {
                return;
            }

            RectF bounds = getCurrentBounds();
            final boolean isSendTextDirectly = !(bounds.right > mWriteBounds);
            if (isSendTextDirectly) {
                startCountDown(true);
            }
            else {
//            	SharedPreferences pref = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE);
//            	Resources res = mContext.getResources();
//            	String promptAnimating = res.getString(
//            					R.string.pref_prompt_handwriting_animating);
//            	boolean isPrompt = pref.getBoolean(promptAnimating, true);
//            	if (isPrompt) {
//            		Toast.makeText(mContext, res.getString(
//            				R.string.prompt_handwriting_animating), Toast.LENGTH_LONG).show();
//            		SharedPreferences.Editor prefEditor = pref.edit();
//            		prefEditor.putBoolean(promptAnimating, false);
//            		prefEditor.commit();
//            	}
                // Shift the drawing board when drawing over right bounds
                // Implement slide animation
                float shift = mShiftBounds - bounds.right;
                mAnimation = ValueAnimator.ofFloat(0f, shift);
                mAnimation.setDuration(ANIMATION_TIME);
                mAnimation.addListener(new AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startCountDown(false);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    	mHistPathList.clear();
                    	eraseCache();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
                mAnimation.addUpdateListener(new AnimatorUpdateListener() {
                    private Float oldValue = 0f;

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                    	if(mCacheBitmapCanvas!=null)
                    	{
	                        Float newValue = (Float) animation.getAnimatedValue();
	                        Float shift = newValue - oldValue;
	                        Matrix shiftMatrix = new Matrix();
	                        shiftMatrix.setTranslate(shift, 0);
	                        eraseCache();
	                        Paint paint = getScreenPaint();
	                        for (PathInfo pathInfo : mHistPathList) {
	                            pathInfo.transform(shiftMatrix);
	                            mCacheBitmapCanvas.drawPath(pathInfo.getPath(), paint);
	                        }
	                        invalidate();
	                        oldValue = newValue;
	                        mPaths.clear();
                    	}
                    }
                });
                mAnimation.start();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }
    }

    private class SendTextCountDown extends CountDownTimer {
        public SendTextCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
        	if(mPageEditorManager!=null)
        	{
	            if (DEBUG) {
	                Log.d(TAG, "SendTextCountDown.onFinish()");
	            }
        	//BEGIN: RICHARD TEST
        	mPageEditor = mPageEditorManager.getCurrentPageEditor();
        	//END: RICHARD TEST
        	
        	//Begin: Dave. change handwriting background
        	if(MetaData.CHANGE_HANDWRITE_BACKGROUND)
        		mPageEditor.getDoodleView().setBackgroundColor(Color.TRANSPARENT);
        	//End: Dave
        	
            if ((mUpEvent != null) && isClick(mHistPathList)) {
            	//BEGIN: RICHARD TEST
            	mPageEditor = mPageEditorManager.setCurrentPageEditor(mUpEvent);
            	mPageEditor.setIsDispatchByHandWritingViewEvent(true);
            	mPageEditorManager.dispatchShortPressPointerMessage(mUpEvent);
            	//END: RICHARD
                //mPageEditor.onShortPress(mUpEvent);
	            }
	            else if (mHistPathList.size() > 0) {
	                CharSequence s = genResultSpannableString();
	                //modify by shane, begin
	                if ( !backDelete() ) {
	            		mPageEditor.addItemToEditText(s);
	                }
	                //modify by shane, end
	            }
	            strokeNum = 0;
	            mHistPathList.clear();
	            mPaths.clear();
	            mIsErasing = true;
	            invalidate();
        	}
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }
    }

    public static final String TAG = "HandWritingView";
    private static final String OBJ = String.valueOf((char) 65532);
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_TOUCH = false;
    private static final float TOUCH_TOLERANCE = 0f;
    private static final float SAVE_TOLERANCE = 4f;
    private static  float CLICK_TOLERANCE = 20f ;//darwin
    private static final float BASELINE_SOLID_LINE_WIDTH = 9f;
    private static final float BASELINE_DASH_LINE_WIDTH = 4f;
    private static final float WRITING_BOUNDS_RATIO = 0.85f;
    private static final float WRITING_SHIFT_RATIO = 0.25f;

    private static int BASELINE_COLOR = 0x0051a8;
    private static int BASELINE_DASH_LINE_ALPHA = 100;
    private static int BASELINE_SOLID_LINE_ALPHA = 18;

    private static final int INVALIDATE_MARGIN = 10;
    private static final int FONT_HEIGHT_OFFSET = 5;
    private float PAINT_STROKE_FACTOR = this.getResources().getInteger(R.integer.paint_stroke_factor); //Carol
    private static final float ALGORITHM_HEIGHT_FACTOR = 0.8f;
    public static int DEFAULT_SEND_TIMER = 400, DRAWING_COUNT_DOWN_TIMER = 500;
    private float mStartX, mStartY, mEndX, mEndY;
    private float mControlX, mControlY;
    private float mAccX, mAccY;
    private float mLineBaseline, mLineTop, mLineBottom;
    private float mDisplayBaseline, mDisplayLineTop, mDisplayLineBottom;
    private float mWriteBounds = 0, mShiftBounds = 0;
    private float mLineHeightScale = 5f;
    private float mDisplayLineHeightScale = 5f;
    private float mLineBottomShift = 0;
    private int mCanvasWidth = 1, mCanvasHeight = 1;
    private int mImageAlignment = ImageSpan.ALIGN_BASELINE;
    private int mSendCountDownTimer = 400;
    private int mPointer1Id = 0;
    private boolean mIsErasing = false;
    private boolean mIsSelecting = false;
    private boolean mIsInfoValidate = true;
    private LinkedList<PathInfo> mHistPathList;
    private LinkedList<PointF> mSavedPoints;
    private Path mPath;
    private Path mPath_pressure = new Path();
    private LinkedList<WritePathInfo> mPaths = new LinkedList<WritePathInfo>(); // wendy 0305
    private float mStartPressure = 1.0f; // wendy
    private Paint mDrawPaint, mBaselineSolidPaint, mBaselineDashPaint, mBitmapPaint;
    private Bitmap mCacheBitmap, mHintRegionBitmap;
    private Canvas mCacheBitmapCanvas;
    private PageEditor mPageEditor;
    private PageEditorManager mPageEditorManager;
    private CountDownTimer mSendTextCountDown;
    private DrawingCountDown mDrawingCountDown = new DrawingCountDown(DRAWING_COUNT_DOWN_TIMER, DRAWING_COUNT_DOWN_TIMER);
    private MotionEvent mUpEvent;
    private LongClickDetector mLongClickDetector = new LongClickDetector();
    
    //Begin: Allen
    private GestureDetector detector; 
    private boolean mIsScrolling = false;
    private boolean mTwoFingers = false;
    //End: Allen
    
    //Begin: Shane
    private boolean mIsBackDelete = true;
    private boolean mIsBackspaceStatus = false; //by show
    private PointF lastPoint;
    private static final float ratioXtoY = 2.0f;
    private long strokeNum = 0L;
    //End: Shane
    
    private boolean ignorePoints = false; //Dave
    
    
    //begin wendy
    // BEGIN: Better
   	   private static final float PRESSURE_PEN_MAX = 1.0f;
       private static final float PRESSURE_PEN_MIN = 0.2f;
       private static final float PRESSURE_FINGER_MAX = 0.3f;
       private static final float PRESSURE_FINGER_MIN = 0.2f;	
   	
       public static class WritePathInfo {
       	private Path mPath = null;
       	private float mStartPressure = 1.0f;
       	private float mEndPressure = 1.0f;
       	       	
       	public WritePathInfo(Path path, float startPressure, float endPressure) {
       		mPath = path;
       		mStartPressure = startPressure;
       		mEndPressure = endPressure;
       	}
       	
       	public WritePathInfo(WritePathInfo pathInfo) {
       		mPath = new Path(pathInfo.mPath);
       		mStartPressure = pathInfo.mStartPressure;
       		mEndPressure = pathInfo.mEndPressure;
       	}
       	
       	public Path getPath() {
       		return mPath;
       	}
       	
       	public float getStartPressure() {
       		return mStartPressure;
       	}
       	
       	public float getEndPressure() {
       		return mEndPressure;
       	}
       	
       	public void setPath(Path path) {
       		mPath = path;
       	}
       	
       	public void setStartPressure(float startPressure) {
       		mStartPressure = startPressure;
       	}
       	
       	public void setEndPressure(float endPressure) {
       		mEndPressure = endPressure;
       	}
       }
       // END: Better
    
    //end wendy
    
    //BEGIN: RICHARD
    private AsusInputRecognizer mAsusInputRecognizer = null;
    //END: RICHARD
    
    private LongClickDetector.OnLongClickListener mLongClickListener = new LongClickDetector.OnLongClickListener() {     	
    	
        @Override
        public void onLongClick(MotionEvent event) {
            if (!mTwoFingers && mHistPathList.isEmpty()) //Allen
			{
            	//BEGIN: RICHARD TEST
            	mPageEditor = mPageEditorManager.getCurrentPageEditor();
            	//END: RICHARD TEST
            	//darwin
//            	Vibrator  vibrator = (Vibrator)mContext.getSystemService(mContext.VIBRATOR_SERVICE);
//            	vibrator.vibrate(500);
            	//darwin
            	
            	int x = (int) event.getX();
            	int y = (int) event.getY();            	
//            	if (mPageEditor.getEditorUiUtility().getInputMode() == InputManager.INPUT_METHOD_SCRIBBLE) {
//	            	if (!mPageEditor.getDoodleView().onLongLongPress(x, y, true)) {
//		                mIsSelecting = true;
//		                mIsErasing = true;
//		                mHistPathList.clear();
//		                mPath.reset();
//		                invalidate();
//		                //mPageEditor.onLongLongPress(x, y);
//		                mPageEditorManager.dispatchLongPressPointerMessage(event);
//	            	}
//            	} else 
            	{
            		mIsSelecting = true;
	                mIsErasing = true;
	                mHistPathList.clear();
	                mPath.reset();
	                invalidate();
	                //mPageEditor.onLongLongPress(x, y);
	                mPageEditorManager.setCurrentPageEditor(event);//RICHARD
	                mPageEditorManager.dispatchLongPressPointerMessage(event);
	                
	                
	                //Begin:Dave. To add text selector.
	                mPageEditor.switchToSelectionTextMode();
	                mPageEditor.editSelectionHandleLeft.setVisibility(View.INVISIBLE);
	                mPageEditor.editSelectionHandleRight.setVisibility(View.INVISIBLE);
	                //End:Dave.
            	}
            }
        }

    };

    public HandWritingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (DEBUG) {
            Log.d(TAG, "HandWritingView created");
        }
//        CLICK_TOLERANCE = 20f * MetaData.DPI / MetaData.BASE_DPI;//darwin
        CLICK_TOLERANCE = getResources().getDimension(R.dimen.click_tolerance);//Begin Siupo
        mPath = new Path();
        mHistPathList = new LinkedList<PathInfo>();
        // BEGIN: archie_huang@asus.com
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setDither(true);
        // END: archie_huang@asus.com
        mBaselineSolidPaint = new Paint();
        //Begin :Clare
        BASELINE_COLOR=getResources().getColor(R.color.BASELINE_COLOR);
        //End :Clare
        PaintSelector.initPaint(mBaselineSolidPaint, BASELINE_COLOR, BASELINE_SOLID_LINE_WIDTH);
       //Begin:clare
        BASELINE_SOLID_LINE_ALPHA=getResources().getInteger(R.integer.BASELINE_SOLID_LINE_ALPHA);
        //End:Clare
        mBaselineSolidPaint.setAlpha(BASELINE_SOLID_LINE_ALPHA);

        mBaselineDashPaint = new Paint();
        PaintSelector.initPaint(mBaselineDashPaint, BASELINE_COLOR, BASELINE_DASH_LINE_WIDTH);
      //Begin:clare
        BASELINE_DASH_LINE_ALPHA=getResources().getInteger(R.integer.BASELINE_DASH_LINE_ALPHA);
        //End:Clare
        mBaselineDashPaint.setAlpha(BASELINE_DASH_LINE_ALPHA);
        mBaselineDashPaint.setStrokeCap(Paint.Cap.BUTT);
        mBaselineDashPaint.setPathEffect(new DashPathEffect(new float[] { context.getResources().getInteger(R.integer.baseline_dashline_blank), context.getResources().getInteger(R.integer.baseline_dashline_line) }, context.getResources().getInteger(R.integer.baseline_dashline_blank) + context.getResources().getInteger(R.integer.baseline_dashline_line) ));

        mLongClickDetector.addLongClickListener(mLongClickListener);

        //Begin:Allen
        detector = new GestureDetector(this);
        //End:Allen
		
        Resources res = context.getResources();
        mDisplayLineHeightScale = res.getDimension(R.dimen.baseline_scale);
        mLineHeightScale = mDisplayLineHeightScale * ALGORITHM_HEIGHT_FACTOR;
        mDisplayLineTop = res.getDimension(R.dimen.baseline_top);
        mLineBottomShift = res.getDimension(R.dimen.baseline_bottom_shift);
        
        //BEGIN: RICHARD
        //CFG.setPath(context.getDir("Data", 0).getAbsolutePath());
        if(CFG.getCanDoVO() == true)//darwin
        {
	        try{
	        	mAsusInputRecognizer = new AsusInputRecognizer();
	        	mAsusInputRecognizer.prepareUnstructuredInputRecognizer();
	        }catch(Exception e)
	        {
	        	e.printStackTrace();
	        	mAsusInputRecognizer = null;
	        }
        }
        //END:RICHARD
    }

    private void disable() {
        setVisibility(View.GONE);
    }

    //wendy
	public static final int SCRIBBLE_TOOL = DrawTool.SCRIBBLE_TOOL;
	private static final float THICKNESS_INTERVAL = 0.025f;
	private static final float THICKNESS_DEGREE_MAX = 1.8f;
	private static final float THICKNESS_DEGREE_MIN = 0.2f;
	private static final float MIN_STROKE_WIDTH = 1.5f;
	private static final float PRESSURE_MAX = 0.9f;
	private static final float PRESSURE_MIN = 0.2f;
	private static final int PRESSURE_LEVEL_NUM = 64;
	private static final float PRESSURE_INTERVAL = (PRESSURE_MAX - PRESSURE_MIN) / PRESSURE_LEVEL_NUM;
	   
	private int getPressureLevel(float pressure) {
	    	if (pressure < PRESSURE_MIN) {
	    		return 0;
	    	} else if (pressure > PRESSURE_MAX) {
	    		return PRESSURE_LEVEL_NUM;
	    	} else {
	    		return (int)((pressure - PRESSURE_MIN) / PRESSURE_INTERVAL);
	    	}
	}
	    
    private void DrawPathsWithPressure(Canvas canvas)
    {
		boolean mIncrease = true;
		Paint paint;
		float strokeWidth;

		if ((mPaths == null) || (canvas == null)) {
			return;
		}
		paint = getScreenPaint();
		strokeWidth = paint.getStrokeWidth();


		int size = mPaths.size();
		if (size > 0) {
			int prevLevel = getPressureLevel(mPaths.get(0).getEndPressure());
			float degree = THICKNESS_DEGREE_MIN + prevLevel
					* THICKNESS_INTERVAL;
			for (WritePathInfo pathInfo : mPaths) { // Better
				float pressure = pathInfo.getEndPressure();
				int level = getPressureLevel(pressure);
				if (level > prevLevel) {
					mIncrease = true;
				} else if (level < prevLevel) {
					mIncrease = false;
				}
				if (mIncrease) {
					float max = THICKNESS_DEGREE_MIN + level
							* THICKNESS_INTERVAL;
					if (degree < max) {
						degree += THICKNESS_INTERVAL;
					}

				} else {
					float min = THICKNESS_DEGREE_MIN + level
							* THICKNESS_INTERVAL;
					if (degree > min) {
						degree -= THICKNESS_INTERVAL;
					}
				}
				float resultStrokeWidth = strokeWidth * degree;
				paint.setStrokeWidth(resultStrokeWidth < MIN_STROKE_WIDTH ? MIN_STROKE_WIDTH
						: resultStrokeWidth);
				canvas.drawPath(pathInfo.getPath(), paint);


				prevLevel = level;
			}			
			paint.setStrokeWidth(strokeWidth);
		}

	}
    //wendy 
    @Override
    public void draw(Canvas canvas) {
        if (mCacheBitmap == null || mCacheBitmap.isRecycled()) {
            Paint paint = getScreenPaint();
            for (PathInfo info : mHistPathList) {
                canvas.drawPath(info.getPath(), paint);
            }
            if(MetaData.HandWriting_HasPressure){
            	  DrawPathsWithPressure(canvas);//wendy      
            }else{
            	canvas.drawPath(mPath, getScreenPaint());
            }
           
               
            return;
        }

        if (mIsErasing) {
            canvas.drawColor(Color.TRANSPARENT);
            eraseCache();
            canvas.drawBitmap(mCacheBitmap, 0, 0, null);
            mIsErasing = false;
        }
        else {
            canvas.drawBitmap(mCacheBitmap, 0, 0, null);
            if (mPath == null) {
                return;
            }
            if (getScreenPaint() == null) {
                return;
            }
            if(MetaData.HandWriting_HasPressure){
            	 DrawPathsWithPressure(canvas);//wendy   
            }else{
            	 canvas.drawPath(mPath, getScreenPaint());     
            }
                
                 
        }
    }

    private void drawBaseline(Canvas canvas) {
    	if(canvas == null){
    		return;
    	}
        canvas.drawLine(0, mDisplayLineTop, mCanvasWidth, mDisplayLineTop, mBaselineSolidPaint);
        canvas.drawLine(0, mDisplayBaseline, mCanvasWidth, mDisplayBaseline, mBaselineDashPaint);
        canvas.drawLine(0, mDisplayLineBottom, mCanvasWidth, mDisplayLineBottom, mBaselineSolidPaint);
    }

    private void enable() {
        setVisibility(View.VISIBLE);
    }

    private void eraseCache() {
        if (mCacheBitmap == null) {
            return;
        }
        mCacheBitmap.eraseColor(Color.TRANSPARENT);
        // BEGIN: archie_huang@asus.com
        if (mHintRegionBitmap != null) {
            Rect src = new Rect(0, 0, mHintRegionBitmap.getWidth(), mHintRegionBitmap.getHeight());
            Rect dst = new Rect((int) mWriteBounds, 0, mCanvasWidth, mCanvasHeight);
            mCacheBitmapCanvas.drawBitmap(mHintRegionBitmap, src, dst, mBitmapPaint);
        }
        // END: archie_huang@asus.com
        
        //Begin: show_wang@asus.com
        //Modified reason: draw baseline when PREFERENCE_IS_BASELINE is true
        if ( getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
        		.getBoolean(getContext().getResources().getString(R.string.pref_baseline), false)) { //smilefish
        	mImageAlignment = DynamicDrawableSpan.ALIGN_BOTTOM;
        }
        //End: show_wang@asus.com
        
        if (mImageAlignment == DynamicDrawableSpan.ALIGN_BOTTOM) {
            drawBaseline(mCacheBitmapCanvas);
        }
    }

    private CharSequence genResultSpannableString() {	
    	//BEGIN: RICHARD TEST
    	mPageEditor = mPageEditorManager.getCurrentPageEditor();
    	//END: RICHARD TEST

        DrawableSpan span;
        if ((mImageAlignment == DynamicDrawableSpan.ALIGN_BOTTOM)) {
        	short contentType = ((NoteEditText)mPageEditor.getCurrentEditor()).getContentType();
        	if ( contentType == NoteItemArray.TEMPLATE_CONTENT_MEETING_ATTENDEE
        			|| contentType == NoteItemArray.TEMPLATE_CONTENT_MEETING_TOPIC
        			|| contentType == NoteItemArray.TEMPLATE_CONTENT_TRAVEL_TITLE) {
                span = new NoteHandWriteBaselineItem(mHistPathList, mDrawPaint, mCanvasHeight, mPageEditor.getImageSpanHeight());
        	} else {
        		span = new NoteHandWriteItem(mHistPathList, mDrawPaint, new RectF(0, mLineTop, 0, mLineBottom), mCanvasHeight, mPageEditor.getFullImageSpanHeight());
        	}
        }
        else {
            span = new NoteHandWriteBaselineItem(mHistPathList, mDrawPaint, mCanvasHeight, mPageEditor.getImageSpanHeight());
        }
        
    	if(mPageEditor.getEditorUiUtility().getIsAutoChangeWriteToType() && mAsusInputRecognizer != null)
    	{
			//BEGIN: RICHARD
    		//String language = ((EditorActivity)getContext()).getNotePage().getIndexLanguage();
    		int language = getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
    				.getInt(getContext().getResources().getString(R.string.pref_index_language), 0);
    		try
    		{
    			mAsusInputRecognizer.loadResource(language);
    		}catch(Exception e)
    		{
        		SpannableStringBuilder ss = new SpannableStringBuilder(OBJ);
        		ss.setSpan(span, 0, OBJ.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        		return ss;
    		}
    		
    		mAsusInputRecognizer.addStroke((NoteHandWriteItem)span);
    		int color = ((NoteHandWriteItem)span).getColor();
    		float strokeWidth = ((NoteHandWriteItem)span).getStrokeWidth();
    		String res = mAsusInputRecognizer.getResult();
    		SpannableStringBuilder ss = new SpannableStringBuilder(res);
            if (color != Color.BLACK) {
                for (int i = 0; i < res.length(); i++) {
                    NoteForegroundColorItem foreColorItem = new NoteForegroundColorItem(color);
                    ss.setSpan(foreColorItem, i , i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            if (strokeWidth != MetaData.SCRIBBLE_PAINT_WIDTHS_NORMAL) {
                for (int i = 0; i < res.length(); i++) {
                    NoteTextStyleItem stylespan = new NoteTextStyleItem(Typeface.BOLD);
                    ss.setSpan(stylespan, i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
    		return ss;
			//END: RICHARD
    	}
    	else
    	{
    		SpannableStringBuilder ss = new SpannableStringBuilder(OBJ);
    		ss.setSpan(span, 0, OBJ.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    		return ss;
    	}

    }

    private RectF getCurrentBounds() {
        RectF result = new RectF();
        for (PathInfo pathInfo : mHistPathList) {
            RectF bounds = new RectF();
            pathInfo.getPath().computeBounds(bounds, true);
            result.union(bounds);
        }
        return result;
    }

    // BEGIN: archie_huang@asus.com
    private Bitmap getNinePatch(int resId, int width, int height) {
        if (width <= 0 || height <= 0) {
            return null;
        }

        Resources res = getContext().getResources();
        Bitmap resBmp = BitmapFactory.decodeResource(res, resId);
        if (resBmp != null) {
            byte[] chunk = resBmp.getNinePatchChunk();
            NinePatchDrawable resDrawable = new NinePatchDrawable(res, resBmp, chunk, new Rect(), null);
            resDrawable.setBounds(0, 0, width, height);

            Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            resDrawable.draw(canvas);

            resBmp.recycle();
            resBmp = null;
            resDrawable.setCallback(null);

            return result;
        }
        return null;
    }
    // END: archie_huang@asus.com

    private Paint getScreenPaint() {
    	//BEGIN: RICHARD TEST
    	mPageEditor = mPageEditorManager.getCurrentPageEditor();
    	//END: RICHARD TEST
        if (mDrawPaint == null) {
            return null;
        }
        Paint paint = new Paint(mDrawPaint);
        paint.setStrokeWidth(mDrawPaint.getStrokeWidth() * PAINT_STROKE_FACTOR);
        return paint;
    }

    private void initLineInfo() {
    	//BEGIN: RICHARD TEST
    	mPageEditor = mPageEditorManager.getCurrentPageEditor();
    	//END: RICHARD TEST
        if (mPageEditor == null) {
            return;
        }

        Paint textPaint = new Paint();
        FontMetricsInt fontMetricsInt;
        float baselineShift = getContext().getResources().getDimension(R.dimen.baseline_shift);
        textPaint.setTextSize(mPageEditor.getNoteEditTextFontSize());
        fontMetricsInt = textPaint.getFontMetricsInt();
        mLineBaseline = mDisplayLineTop - ((fontMetricsInt.ascent + FONT_HEIGHT_OFFSET) * mDisplayLineHeightScale);
        mDisplayLineBottom = mLineBaseline + (fontMetricsInt.descent * mDisplayLineHeightScale) + mLineBottomShift;
        mDisplayBaseline = mLineBaseline + baselineShift;

        mLineTop = mLineBaseline + ((fontMetricsInt.ascent) * mLineHeightScale);
        mLineBottom = mLineBaseline + (fontMetricsInt.descent * mLineHeightScale);
    }

    private boolean isClick(LinkedList<PathInfo> pathInfos) {
        if (DEBUG_TOUCH) {
            Log.d(TAG, "isClick()");
            Log.d(TAG, "pathInfos size = " + pathInfos.size());
        }
        if ((pathInfos.size() == 1) && (NoteHandWriteItem.getPathLength(pathInfos.get(0).getPath()) <= CLICK_TOLERANCE)) {
            return true;
        }
        return false;
    }

    private void loadTimer() {
        String countDownKey = getContext().getResources().getString(R.string.pref_fast_input_value);
        mSendCountDownTimer = getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(countDownKey, DEFAULT_SEND_TIMER);

        if (mSendTextCountDown != null) {
            mSendTextCountDown.cancel();
        }
        mSendTextCountDown = new SendTextCountDown(mSendCountDownTimer, mSendCountDownTimer);

        mDrawingCountDown = new DrawingCountDown(DRAWING_COUNT_DOWN_TIMER, DRAWING_COUNT_DOWN_TIMER);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //darwin
        if(w<= 0 || h <= 0)
        {
        	return;
        }
        //darwin
    	//BEGIN: RICHARD TEST
        if (mPageEditorManager != null) {
        	mPageEditor = mPageEditorManager.getCurrentPageEditor();
        	//END: RICHARD TEST
	        if (mCanvasWidth != w || mCanvasHeight != h) {
	            mCanvasWidth = w;
	            mCanvasHeight = h;
	
	            if ((mCacheBitmap != null) && !mCacheBitmap.isRecycled()) {
	                mCacheBitmap.recycle();
	                mCacheBitmap = null;
	                mCacheBitmapCanvas = null;
	            }
	            // BEGIN: archie_huang@asus.com
	            if (mHintRegionBitmap != null && !mHintRegionBitmap.isRecycled()) {
	                mHintRegionBitmap.recycle();
	                mHintRegionBitmap = null;
	            }
	            // END: archie_huang@asus.com
	            try {
	            	// BEGIN: archie_huang@asus.com
	            	//Begin: show_wang@asus.com
	            	//Modified reason: Remove Phone LandSpace Handwrite shift mechanism
	            	int IsContinueWrite = this.getResources().getInteger(R.integer.is_continue_write); 
//	                if (mPageEditor.getDeviceType()<=100) {
	            	if ( IsContinueWrite == 1 ) {
		            //End: show_wang@asus.com
	                    mHintRegionBitmap = getNinePatch(R.drawable.fill_normal, mCanvasWidth, mCanvasHeight);
	                }
	                // END: archie_huang@asus.com
	                mCacheBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	                mCacheBitmap.setDensity(Bitmap.DENSITY_NONE);
	                mCacheBitmapCanvas = new Canvas(mCacheBitmap);
	            }
	            catch (OutOfMemoryError e) {
	                Log.d(TAG, "[OutOfMemoryError] Create cache failed !!!");
	            }
	
	            // initialize line info here because we need to know the size of HandWritingView
	            initLineInfo();
	
	            mWriteBounds = mCanvasWidth * WRITING_BOUNDS_RATIO;
	            mShiftBounds = mCanvasWidth * WRITING_SHIFT_RATIO;
	        }
	
	        mIsErasing = true;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent arg0)
    {
    	// begin jason : is Stylus input only? yes: intercept the event that is not the  TOOL_TYPE_STYLUS
    	if (MethodUtils.isPenDevice(getContext())) { //smilefish
			if((mPageEditor.getEditorUiUtility().getmIsStylusInputOnly())){
				if (arg0.getToolType(arg0.getActionIndex())!=MotionEvent.TOOL_TYPE_STYLUS ) {
					return true;
				}
				
			}
		}
    	
    	return super.dispatchTouchEvent(arg0);
    	// end jason
    };
    
    float mFirstTouchX = 0; 
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(MethodUtils.isEnableAirview(this.getContext())){
    		mPageEditor.getTemplateLinearLayout().setClearAirView();//by show
    	}
    	//BEGIN: RICHARD TEST
    	mPageEditor = mPageEditorManager.getCurrentPageEditor();
    		
    	//END: RICHARD TEST
    	float x = event.getX();
        float y = event.getY();
        
        float pressure = event.getPressure();
    	
		//BEGIN: RICHARD
        Boolean isDispatchPointerEventToScroller = mPageEditorManager.dispatchPointerEventToScroll(event);
        boolean isDispatchTouchEventToMicroView = mPageEditorManager.dispatchTouchEventToMicroView(event);//Allen
        if(isDispatchPointerEventToScroller||isDispatchTouchEventToMicroView)
        {
        	return true;
        }
		//END: RICHARD
    	//Begin: Allen
    	mTwoFingers = event.getPointerCount()==2 ? true : false;
    	//begin jason when is pen only ,we close the two finger scroll page function
    	if(!(MethodUtils.isPenDevice(getContext())&&mPageEditor.getEditorUiUtility().getmIsStylusInputOnly())) //smilefish
    	{
    		this.detector.onTouchEvent(event);
        	if(event.getPointerCount()==2)
        	{
    			mDrawingCountDown.cancelCountDownAndAnimation();
            	mHistPathList.clear();
                if (mSavedPoints != null) {
                	mSavedPoints.clear();
                	mSavedPoints = null;
                }
                mPath.reset();
                mIsInfoValidate = false;
                eraseCache();
                invalidate();
        		return true;   		
        	}
    	}
    	//end jason
    	//End:Allen

        if(!mPageEditor.isTouchOnTemplateRect(event)||mPageEditor.getTemplateType()==MetaData.Template_type_todo){
        	mLongClickDetector.onTouch(MotionEvent.obtain(event));
        }
        
        if (mPageEditor.isSelectionTextMode()) //Dave. To add text selector. 
        { 
            //mPageEditor.onLongClickAndDrag(MotionEvent.obtain(event));
        	if(event.getPointerCount() == 1)
        	{
        		mPageEditorManager.dispatchLongPressPointerMessage(event);
        	}
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mIsSelecting = false;
            }
        	//Begin: Dave. change handwriting background
        	if(MetaData.CHANGE_HANDWRITE_BACKGROUND)
        		mPageEditor.getDoodleView().setBackgroundColor(Color.TRANSPARENT);
        	//End: Dave
        }else {
            int pt1Index = event.findPointerIndex(mPointer1Id);
            int actionIndex = event.getActionIndex();

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                	//Begin:Dave. To fix the bug: keyboard will show and disappear one time in keyboard_input_mode.
                	if(mPageEditor.HasPopUpWindows)
                	{
                		return true;
                	}
                	//End Dave.
                	
                	if(mPageEditor.shouldDiscardPoint)
                	{
                		return true;
                	}
                	
                	if(ignorePoints)
                	{
                		return true;
                	}
                	
                    if (DEBUG_TOUCH) {
                        Log.d(TAG, "ACTION_DOWN: (x,y) = " + "(" + x + "," + y + ")");
                    }
                    strokeNum ++;
                    mDrawingCountDown.cancelCountDown();
                    touch_start(x, y, pressure);
                    invalidate();
                    mIsInfoValidate = true;
                    mIsScrolling = false;//Allen
                    
                    mFirstTouchX = x;
                    break;
                case MotionEvent.ACTION_MOVE:
                	//Begin:Dave. To fix the bug: keyboard will show and disappear one time in keyboard_input_mode.
                	if(mPageEditor.HasPopUpWindows)
                	{
                		return true;
                	}
	                //End Dave.
                	
                	if(mPageEditor.shouldDiscardPoint)
                	{
                		return true;
                	}
                	
                    if (DEBUG_TOUCH) {
                        Log.d(TAG, "ACTION_MOVE: (x,y) = " + "(" + x + "," + y + ")");
                    }
                    
                	//Begin: Dave. change handwriting background
                    if(Math.abs(x-mFirstTouchX) > 30)
                    {
                    	if(MetaData.CHANGE_HANDWRITE_BACKGROUND)
                    		mPageEditor.getDoodleView().setBackgroundColor(Color.WHITE);
                    	
                    	if(mPageEditor.isSelectionTextMode())
                    	{
                    		mPageEditor.quitSelectionTextMode(); //Dave: to add text selector.
                    	}
                    	
                    }
                    //End: Dave
                    
                    if (mIsInfoValidate && !mIsScrolling) {//Allen
                    	mPageEditorManager.setMicroViewVisible(false);//Allen
                        Rect dirtyRect = touch_move(x, y,pressure);
                        //Begin: Allen
                        //mIsWriting = true;
                        mIsScrolling = false;
                        //End: Allen
                        if (dirtyRect != null) {
                            invalidate(dirtyRect);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:      
                	//Begin:Dave. To fix the bug: keyboard will show and disappear one time in keyboard_input_mode.
                	if(mPageEditor.HasPopUpWindows)
                	{
                		mPageEditor.HasPopUpWindows = false;
                		return true;
                	}
                	//End Dave.
                	
                	if(mPageEditor.shouldDiscardPoint)
                	{
                		mPageEditor.shouldDiscardPoint = false;
                		return true;
                	}
                	
                    if (DEBUG_TOUCH) {
                        Log.d(TAG, "ACTION_UP: (x,y) = " + "(" + x + "," + y + ")");
                    }
                    if (mIsInfoValidate && !mIsScrolling && !mPageEditor.shouldDiscardPoint) {//Allen
                        mUpEvent = MotionEvent.obtain(event);
                        touch_up(x, y,pressure);
                        invalidate();
                        RectF bounds = getCurrentBounds();
                        boolean isSendTextDirectly = !(bounds.right > mWriteBounds);
                        mDrawingCountDown.startCountDown(isSendTextDirectly);
                        mIsInfoValidate = false;
                    }
                    //mIsWriting = false;//Allen
                    
                    mFirstTouchX = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (DEBUG_TOUCH) {
                        Log.d(TAG, "ACTION_POINTER_1_DOWN: (x,y) = " + "(" + x + "," + y + ")");
                    }
                    if (actionIndex == pt1Index) {
                        mDrawingCountDown.cancelCountDown();
                        touch_start(x, y,pressure);
                        invalidate();
                        mIsInfoValidate = true;
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    if (DEBUG_TOUCH) {
                        Log.d(TAG, "ACTION_POINTER_1_UP: (x,y) = " + "(" + x + "," + y + ")");
                    }
                    if (actionIndex == pt1Index) {
                        mUpEvent = MotionEvent.obtain(event);
                        mUpEvent.setAction(MotionEvent.ACTION_UP);
                        touch_up(x, y, pressure);
                        invalidate();
                        RectF bounds = getCurrentBounds();
                        boolean isSendTextDirectly = !(bounds.right > mWriteBounds);
                        mDrawingCountDown.startCountDown(isSendTextDirectly);
                        mIsInfoValidate = false;
                    }
                    //mIsWriting = false;
                    break;
            }
        }
        return true;
    }

    private void recycleBitmaps() {
        if ((mCacheBitmap != null) && !mCacheBitmap.isRecycled()) {
            mCacheBitmap.recycle();
            mCacheBitmap = null;
        }
        mCacheBitmapCanvas = null;
        
        //BEGIN: RICHARD
        if (mHintRegionBitmap != null && !mHintRegionBitmap.isRecycled()) {
            mHintRegionBitmap.recycle();
            mHintRegionBitmap = null;
        }
        //END: RICHARD
    }

    public void setPageEditor(PageEditor pageEditor) {
        mPageEditor = pageEditor;
    }

    public void setPaint(Paint paint) {
        mDrawPaint = paint;
    }

    public void setSelecting(boolean isSelecting) {
        mIsSelecting = isSelecting;
    }

    public void switchBaselineMode() {
        if (mImageAlignment == DynamicDrawableSpan.ALIGN_BASELINE) {
            mImageAlignment = DynamicDrawableSpan.ALIGN_BOTTOM;
            drawBaseline(mCacheBitmapCanvas);
        }
        else if (mImageAlignment == DynamicDrawableSpan.ALIGN_BOTTOM) {
            mImageAlignment = DynamicDrawableSpan.ALIGN_BASELINE;
            mIsErasing = true;
        }
        invalidate();
    }

    private Rect touch_move(float x, float y, float pressure) {
        float controlX = mEndX;
        float controlY = mEndY;
        float absX = Math.abs(x - mEndX);
        float absY = Math.abs(y - mEndY);
        mAccX += absX;
        mAccY += absY;

        if ((absX >= TOUCH_TOLERANCE) || (absY >= TOUCH_TOLERANCE)) {
            // If the previous drawing stopped in invalid motion, we reset the information
            if (mSavedPoints == null) {
                touch_start(x, y, pressure);
            }
            else {
                if ((mAccX >= SAVE_TOLERANCE) && (mAccY >= SAVE_TOLERANCE)) {
                    mSavedPoints.add(new PointF(x, y));
                    mAccX = 0;
                    mAccY = 0;
                }
                mPath.quadTo(controlX, controlY, (x + controlX) / 2, (y + controlY) / 2);
                mPath_pressure.quadTo(controlX, controlY, (x + controlX) / 2, (y + controlY) / 2);
                mPaths.add(new WritePathInfo(mPath_pressure, mStartPressure, pressure)); // Better
                //wendy	
                mPath_pressure = new Path();
                // mPath.moveTo(x[0], y[0]);
                mPath_pressure.moveTo((x + controlX) / 2, (y + controlY) / 2);               
                mStartPressure = pressure;
                 //wendy 
            }

            int invalidateMargin = INVALIDATE_MARGIN + (int) ((mDrawPaint.getStrokeWidth() * PAINT_STROKE_FACTOR) / 2);
            int left = (int) ((x < mControlX) ? x : mControlX) - invalidateMargin;
            int right = (int) ((x < mControlX) ? mControlX : x) + invalidateMargin;
            int top = (int) ((y < mControlY) ? y : mControlY) - invalidateMargin;
            int bottom = (int) ((y < mControlY) ? mControlY : y) + invalidateMargin;

            mControlX = controlX;
            mControlY = controlY;
            mEndX = x;
            mEndY = y;

            if (DEBUG_TOUCH) {
                Log.d(TAG, "Dirty Rect = (" + left + "," + top + "," + right + "," + bottom + ")");
            }

            return new Rect(left, top, right, bottom);
        }
        return null;
    }

    

    
    private void touch_start(float x, float y, float pressure) {
        mSavedPoints = new LinkedList<PointF>();
        mSavedPoints.add(new PointF(x, y));
        mPath.reset();
        mPath_pressure.reset();
        mPath.moveTo(x, y);
        mPath_pressure.moveTo(x, y);
        //mPaths.clear();
        mStartX = x;
        mStartY = y;
        mEndX = x;
        mEndY = y;
        mControlX = x;
        mControlY = y;
        
        mStartPressure = pressure;
    }

    private void touch_up(float x, float y, float pressure) {
        if (mStartX == mEndX) {
            mEndX += 1;
        }

        if (mStartY == mEndY) {
            mEndX += 1;
        }

        mPath.lineTo(mEndX, mEndY);
        mPath_pressure.lineTo(mEndX, mEndY);
        mPaths.add(new WritePathInfo(mPath_pressure, mStartPressure, pressure));//wendy
        if (mSavedPoints != null) {
            mSavedPoints.add(new PointF(mEndX, mEndY));
            mHistPathList.addLast(new PathInfo(mPath, mSavedPoints));
        }

        if (mCacheBitmapCanvas != null) {
        	if(MetaData.HandWriting_HasPressure){
        		DrawPathsWithPressure(mCacheBitmapCanvas); 
        	}else{
        		mCacheBitmapCanvas.drawPath(mPath, getScreenPaint());                	
        	}
                    
        }
        mPath.reset();
    }
    //Begin: Allen
	@Override
	public boolean onDown(MotionEvent e) {
        return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
    	//BEGIN: RICHARD TEST
    	mPageEditor = mPageEditorManager.getCurrentPageEditor();
    	//END: RICHARD TEST
		if(e2.getPointerCount()==2)
		{
			mIsScrolling = true;
			
			strokeNum = 0; //smilefish. fix bug 309503/309495
			
//			if(Math.abs(distanceX) > Math.abs(distanceY))
//			{
//	            mTotalX += distanceX;
//			    mPageEditor.scrollEditText(Math.round(distanceX), 0);
//			}
//			else
//			{
//				mPageEditor.scrollEditText(0, Math.round(distanceY));
//			}
			
			mPageEditor.scrollEditText(Math.round(distanceX), Math.round(distanceY));
		}
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	//End:Allen
	
	//Begin Siupo
	boolean pointsFitRule(int first,int end)
	{
		boolean ret = false;
		float xielv = 0.0f;
		PointF prevPoint = mSavedPoints.get(first);
        for(int i = first+1;i<end;i++) 
        {
        	PointF p = mSavedPoints.get(i);
        	if(p.x >= prevPoint.x)
        	{
        		return false;
        	}else if( Math.abs(p.x - prevPoint.x) > Math.abs(p.y - prevPoint.y)*ratioXtoY )
        	{
        		ret = true;
        		continue;
        	}
        	else 
        	{
        		return false;
        	}       
        }
		return ret;
	}
	//BEGIN: SHANE_WANG    
    private boolean backDelete()
    {
        mIsBackspaceStatus = getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
				.getBoolean(getContext().getResources().getString(R.string.pref_backspace_gesture), false); //By Show
    	
        mIsBackDelete = false;
        if (mIsBackspaceStatus)
    	{
        	if (strokeNum > 1)
        	{
        		return false;
        	}

	        if(mSavedPoints.size()<=4)
	        {
	        	//the num of Points is less than 4;
	        	mIsBackDelete = pointsFitRule(0,mSavedPoints.size());
	        }
	        else
	        {
	        	//the num of Points is more than 4;
	        	PointF firstPoint = mSavedPoints.getFirst();
	        	PointF endPoint = mSavedPoints.getLast();
	        	if(firstPoint.x > endPoint.x && 
	        			Math.abs(endPoint.x - firstPoint.x) > Math.abs(endPoint.y - firstPoint.y)*ratioXtoY)
	        	{
	        		mIsBackDelete = pointsFitRule(1,mSavedPoints.size()-2);
	        	}
	        	else
	        	{
	        		mIsBackDelete = false;
	        	}
	        	
	        }
    		
	        //对所有点进行上下判断，若为上下走向，则不删除；
	        if ( mIsBackDelete ) 
	        {
	        	//BEGIN: RICHARD TEST
	        	mPageEditor = mPageEditorManager.getCurrentPageEditor();
	        	//END: RICHARD TEST
	            mPageEditor.insertBackSpace();
	        }
	        
	        return mIsBackDelete;
    	}
    	return false;
    }
    //END: SHANE_WANG

	public void setPageEditorManager(PageEditorManager pageEditorManager) {
		// TODO Auto-generated method stub
		mPageEditorManager = pageEditorManager;		
	}
	private PenOnlyHelper mPenOnlyHelper=null;
	private class PenOnlyHelper{
		public int penID;
	}
	// begin jason
	public void flushData(){
		if(mSendTextCountDown!=null){
			mSendTextCountDown.onFinish();
		}
	}
	// end jason
	
	////////////////////////////////////////////////////////////////
	//add by mars_li for implement IHandWritePanel
	///////////////////////////////////////////////////////////////
    @Override
    public boolean needRecognizer() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getRecognizerResult(NoteHandWriteItem noteHandWriteItem) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void writeFinished(IHandWriteView writeView) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean needNoteBaseLineItem() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getFullImageSpanHeight() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getImageSpanHeight() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setEnable(boolean isVisblity) {
        // TODO Auto-generated method stub
        if(isVisblity){
            this.enable();
        }else{
            this.disable();
        }
    }
    
    @Override
    public boolean getEnable(){//add by smilefish
    	return this.getVisibility() == View.VISIBLE;
    }

    @Override
    public void setBaseLine(boolean isEnableBaseLine) {
        // TODO Auto-generated method stub
        //this.switchBaselineMode();
    	 if (isEnableBaseLine) {
             mImageAlignment = DynamicDrawableSpan.ALIGN_BOTTOM;
             drawBaseline(mCacheBitmapCanvas);
         }
         else {
             mImageAlignment = DynamicDrawableSpan.ALIGN_BASELINE;
             mIsErasing = true;
         }
         invalidate();
    }

    @Override
    public FontMetricsInt getFontMetricInt() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        this.recycleBitmaps();
    }

    @Override
    public void reloadTimer() {
        // TODO Auto-generated method stub
        this.loadTimer();
    }

	@Override
	public void setEableListener(IWritePanelEnableListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getHeightForScroll() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean reponseBackKey() {
		// TODO Auto-generated method stub
		return false;
	}
}
