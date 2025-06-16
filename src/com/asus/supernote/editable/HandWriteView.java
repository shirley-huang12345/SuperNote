package com.asus.supernote.editable;
/*
 * author:mars_li@asus.com
 * Description: implement IWriteView
 */

import java.util.LinkedList;

import com.asus.supernote.PaintSelector;
import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.editable.noteitem.NoteForegroundColorItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteBaselineItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteTextStyleItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem.PathInfo;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.NinePatchDrawable;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class HandWriteView extends View implements IHandWriteView {
	static final String TAG = "HandWriteView";
	
	private LinkedList<PathInfo> mHistPathList;
	private LinkedList<PointF> mSavedPoints;
	private float mStartX;
	private float mStartY;
	private float mEndX;
	private float mEndY;
	private float mControlX;
	private float mControlY;
	private float mStartPressure;
	private Path mPath;
	private float mAccX;
	private float mAccY;
	
	private Paint mDrawPaint;
	private float paintFactor = 1.0f;
	private Paint mWritePaint;
	private Canvas mCacheBitmapCanvas;
	private Bitmap mCacheBitmap;
	private Bitmap mHintRegionBitmap;
	
	private int mPointer1Id = 0;
	
	Paint mBaselineSolidPaint;
	Paint mBaselineDashPaint;
	float mDisplayLineTop = 10f;
	float mDisplayBaseline = mDisplayLineTop + 20f;
	float mDisplayLineBottom = mDisplayBaseline +10f;
	
	private float mWriteBounds = 0, mShiftBounds = 0;
	
	boolean isShowBaseLine = false;
	
	boolean mIsErasing = false;
	
	IHandWritePanel editorPanel ;

    private float mLineTop;

    private float mLineBottom;

    private float mDisplayLineHeightScale;

    private float mLineBaseline;

    private float mLineHeightScale;

    private float mLineBottomShift;

    private SendTextCountDown mSendTextCountDown;
    
    private DrawingCountDown mDrawingCountDown = new DrawingCountDown(DRAWING_COUNT_DOWN_TIMER, DRAWING_COUNT_DOWN_TIMER);
    
    private boolean mIsInfoValidate = true;

	private int mCanvasWidth;

	private int mCanvasHeight;
	
	private Rect mDirtyRect = null;

    private void initBaseLineData1(){
        
        float topShift = 30;
        float bottomShift = 20;
        float baseLineSize = this.getHeight()-topShift -bottomShift;
        float  baselineShift = getResources().getDimension(R.dimen.hand_panel_view_baseline_shift);
        baselineShift = (baseLineSize/209)*baselineShift;//209 is a test value, get by ui adjust
        FontMetricsInt fontMetricsInt = this.editorPanel.getFontMetricInt();
        if(fontMetricsInt == null) //Dave
        	return;
        
        int fontHeight = fontMetricsInt.descent - fontMetricsInt.ascent;
        mDisplayLineHeightScale = baseLineSize/fontHeight;
        mLineHeightScale = mDisplayLineHeightScale * ALGORITHM_HEIGHT_FACTOR;
        
        mDisplayLineTop = topShift;
        
        mLineBaseline = mDisplayLineTop - ((fontMetricsInt.ascent + FONT_HEIGHT_OFFSET) * mDisplayLineHeightScale);
        mDisplayLineBottom = mLineBaseline + (fontMetricsInt.descent * mDisplayLineHeightScale) /*+ mLineBottomShift*/;
        mDisplayBaseline = mLineBaseline - baselineShift;
        
        mLineTop = mLineBaseline + ((fontMetricsInt.ascent) * mLineHeightScale);
        mLineBottom = mLineBaseline + (fontMetricsInt.descent * mLineHeightScale);   
    }
    
	public HandWriteView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mDrawPaint = new Paint();
		mDrawPaint.setStrokeWidth(2.0f);
		mDrawPaint.setAntiAlias(true);
		mDrawPaint.setStyle(Style.STROKE);
		
		paintFactor = this.getResources().getInteger(R.integer.paint_stroke_factor); 
		
		mPath = new Path();
		
		mHistPathList = new LinkedList<PathInfo>();
		
		int baseLineSolidAlpha=getResources().getInteger(R.integer.BASELINE_SOLID_LINE_ALPHA);
		mBaselineSolidPaint = new Paint();
		int baseLineColor=getResources().getColor(R.color.BASELINE_COLOR);
        PaintSelector.initPaint(mBaselineSolidPaint, baseLineColor, BASELINE_SOLID_LINE_WIDTH);
        mBaselineSolidPaint.setAlpha(baseLineSolidAlpha);
        
        mBaselineDashPaint = new Paint();
        PaintSelector.initPaint(mBaselineDashPaint, baseLineColor, BASELINE_DASH_LINE_WIDTH);
		int baseLineDashAlpha=getResources().getInteger(R.integer.BASELINE_DASH_LINE_ALPHA);
   
        mBaselineDashPaint.setAlpha(baseLineDashAlpha);
        mBaselineDashPaint.setStyle(Style.STROKE);
        mBaselineDashPaint.setStrokeCap(Paint.Cap.BUTT);
        mBaselineDashPaint.setPathEffect(new DashPathEffect(new float[] { 
                context.getResources().getInteger(R.integer.baseline_dashline_blank),
                context.getResources().getInteger(R.integer.baseline_dashline_line) },
                context.getResources().getInteger(R.integer.baseline_dashline_blank) + 
                context.getResources().getInteger(R.integer.baseline_dashline_line) ));
		
		//initBaseLineData();
	}
	

	@Override
	public void setBaseLineMode(boolean showBaseLine) {
		// TODO Auto-generated method stub
	    isShowBaseLine = showBaseLine;
	    if(isShowBaseLine){
	        drawBaseline(mCacheBitmapCanvas);
	    }else{
	        /*this.mIsErasing = true;
	        if(mCacheBitmap!= null){
	            mCacheBitmap.eraseColor(Color.TRANSPARENT);
	        }*/
	    	this.eraseCache();
	    }
	    this.requestRender(null);
	}

	@Override
	public void onTouchDown(float x, float y, float pressure) {
		// TODO Auto-generated method stub

		mSavedPoints = new LinkedList<PointF>();
		mSavedPoints.add(new PointF(x, y));
		mPath.reset();
		mPath.moveTo(x, y);

		mStartX = x;
		mStartY = y;
		mEndX = x;
		mEndY = y;
		mControlX = x;
		mControlY = y;

		mStartPressure = pressure;
		this.requestRender(null);

	}
	
	protected void computeDirty(float x, float y) {
        // Compute Dirty Rectangle
		int invalidateMargin = INVALIDATE_MARGIN + (int) ((getDrawPaint().getStrokeWidth()) / 2);
        int left = (int) ((x < mControlX) ? x : mControlX) - invalidateMargin;
        int right = (int) ((x < mControlX) ? mControlX : x) + invalidateMargin;
        int top = (int) ((y < mControlY) ? y : mControlY) - invalidateMargin;
        int bottom = (int) ((y < mControlY) ? mControlY : y) + invalidateMargin;
        if (mDirtyRect == null) {
            mDirtyRect = new Rect(left, top, right, bottom);
        }
        else {
            mDirtyRect.union(left, top, right, bottom);
        }
        
        if (DEBUG_TOUCH) {
            Log.d(TAG, "Dirty Rect = (" + left + "," + top + "," + right + "," + bottom + ")");
        }
    }

	@Override
	public void onTouchMove(float x, float y, float pressure) {
		
        float absX = Math.abs(x - mEndX);
        float absY = Math.abs(y - mEndY);
        
        mAccX += absX;
        mAccY += absY;
        
		float controlX = mEndX;
        float controlY = mEndY;

        if ((absX * absX + absY * absY) >= (TOUCH_TOLERANCE * TOUCH_TOLERANCE)) {
            // If the previous drawing stopped in invalid motion, we reset the information
            if (mSavedPoints == null) {
                onTouchDown(x, y, pressure);
            }
            else {
                if ((mAccX >= SAVE_TOLERANCE) && (mAccY >= SAVE_TOLERANCE)) {
                    mSavedPoints.add(new PointF(x, y));
                    mAccX = 0;
                    mAccY = 0;
                }
               // mPath.addCircle(x, y, 6.0f,Direction.CW);
                //mPath.lineTo(x, y);
                mPath.quadTo(controlX, controlY, (x + controlX) / 2, (y + controlY) / 2);        
                mStartPressure = pressure;
                 //wendy 
            }

            computeDirty(x, y);

            mControlX = controlX;
            mControlY = controlY;
            mEndX = x;
            mEndY = y;
        }
	}

	@Override
	public void onTouchUp(float x, float y, float pressure) {
		// TODO Auto-generated method stub
		if (mStartX == mEndX) {
            mEndX += 1;
        }

        if (mStartY == mEndY) {
            mEndX += 1;
        }

        mPath.lineTo(mEndX, mEndY);

        if (mSavedPoints != null) {
            mSavedPoints.add(new PointF(mEndX, mEndY));
            mHistPathList.addLast(new PathInfo(mPath, mSavedPoints));
        }

        if (mCacheBitmapCanvas != null) {
        	mCacheBitmapCanvas.drawPath(mPath, getDrawPaint());                	        
        }
        mPath.reset();
        this.requestRender(null);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if (mCacheBitmap == null || mCacheBitmap.isRecycled()) {
			for (PathInfo info : mHistPathList) {
				canvas.drawPath(info.getPath(), getDrawPaint());
			}

			canvas.drawPath(mPath, getDrawPaint());

			return;
		}
		
		if(mIsErasing){
		    canvas.drawColor(Color.TRANSPARENT);
		    mIsErasing = false;
		    canvas.drawBitmap(mCacheBitmap, 0, 0, null);
		}
		else{
		    canvas.drawBitmap(mCacheBitmap, 0, 0, null);
		    if (mPath != null) {
	            canvas.drawPath(mPath, getDrawPaint());
	        }
		}
		
		mDirtyRect = null;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		
		 if(w<= 0 || h <= 0)
	     {
	        return;
	     }
		 
		 if(w == oldw&&h == oldh){
			 return;
		 }
		 
		 mCanvasWidth = w;
         mCanvasHeight = h;
		 
		 if ((mCacheBitmap != null) && !mCacheBitmap.isRecycled()) {
		     mCacheBitmapCanvas = null; //this maybe a memory leak
             mCacheBitmap.recycle();
             mCacheBitmap = null;
         }
		 
		 
		// BEGIN: archie_huang@asus.com
         if (mHintRegionBitmap != null && !mHintRegionBitmap.isRecycled()) {
             mHintRegionBitmap.recycle();
             mHintRegionBitmap = null;
         }
         int IsContinueWrite = this.getResources().getInteger(R.integer.is_continue_write); 
     	 if ( IsContinueWrite == 1 ) {
     		 //mCanvasWidth can be set to  mWriteBounds
             mHintRegionBitmap = getNinePatch(R.drawable.fill_normal, w, h);
         }
     	 
     	mWriteBounds = w * WRITING_BOUNDS_RATIO;
        mShiftBounds = w * WRITING_SHIFT_RATIO;
         // END: archie_huang@asus.com
		 
		 mCacheBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         mCacheBitmap.setDensity(Bitmap.DENSITY_NONE);
         mCacheBitmapCanvas = new Canvas(mCacheBitmap);
         
         initBaseLineData1();
         
         if(isShowBaseLine){
             drawBaseline(mCacheBitmapCanvas);
         }
         this.eraseCache();
         
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		//END: RICHARD TEST
    	float x = event.getX();
        float y = event.getY();
        
        float pressure = event.getPressure();
        
        int pt1Index = event.findPointerIndex(mPointer1Id);
        int actionIndex = event.getActionIndex();
        
        switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
       
            if (mDrawingCountDown != null) {
            	mDrawingCountDown.cancelCountDown();
            }
            mIsInfoValidate = true;
        	onTouchDown(x, y, pressure);
        	break;
        case MotionEvent.ACTION_MOVE:
            if(mIsInfoValidate){
            	final int historySize = event.getHistorySize();
        		for (int h = 0; h < historySize; h++) {
        			onTouchMove(event.getHistoricalX(h), event.getHistoricalY(h), event.getHistoricalPressure(h));
        		}
                onTouchMove(x, y, pressure);
                this.requestRender(mDirtyRect);
            }
        	
        	break;
        case MotionEvent.ACTION_UP:
            if (mIsInfoValidate){
                onTouchUp(x, y, pressure);
                if (mDrawingCountDown != null) {
                	RectF bounds = getCurrentBounds();
                    boolean isSendTextDirectly = !(bounds.right > mWriteBounds);
                	mDrawingCountDown.startCountDown(isSendTextDirectly);
                }
                mIsInfoValidate = false;
            }
        	break;
        case MotionEvent.ACTION_POINTER_DOWN:
        	if (actionIndex == pt1Index) {
        	    onTouchDown(x, y, pressure);
        	    mIsInfoValidate = true;
            }
        	break;
        case MotionEvent.ACTION_POINTER_UP:
        	if (actionIndex == pt1Index) {
        	    onTouchUp(x, y, pressure);
        	    mIsInfoValidate = false;
        	    if (mDrawingCountDown != null) {
        	    	RectF bounds = getCurrentBounds();
                    boolean isSendTextDirectly = !(bounds.right > mWriteBounds);
                	mDrawingCountDown.startCountDown(isSendTextDirectly);
                }
            }
        	break;
        }
        
		return true;
	}

	@Override
	public void recycleBitmaps() {
		// TODO Auto-generated method stub
		if ((mCacheBitmap != null) && !mCacheBitmap.isRecycled()) {
            mCacheBitmap.recycle();
            mCacheBitmap = null;
        }
        mCacheBitmapCanvas = null;
	}

	@Override
	public void requestRender(Rect rect) {
		// TODO Auto-generated method stub
		if(rect == null)
			this.invalidate();
		else
			this.invalidate(rect);
	}

	@Override
	public void setPaint(Paint paint) {
		// TODO Auto-generated method stub
		if(paint != null)
			mDrawPaint = paint;
	}


	@Override
	public void clear() {
		// TODO Auto-generated method stub
		mHistPathList.clear();
		eraseCache();
	}
	
	private void eraseCache(){
		if (mCacheBitmap == null) {
            return;
        }
        mCacheBitmap.eraseColor(Color.TRANSPARENT);
        
        this.mIsErasing = true;
        
        if(this.isShowBaseLine){
            drawBaseline(mCacheBitmapCanvas);
        }
        
     // BEGIN: archie_huang@asus.com
        if (mHintRegionBitmap != null) {
        	Paint bitmapPaint = new Paint();
            bitmapPaint.setAntiAlias(true);
            bitmapPaint.setDither(true);
            Rect src = new Rect(0, 0, mHintRegionBitmap.getWidth(), mHintRegionBitmap.getHeight());
            Rect dst = new Rect((int) mWriteBounds, 0, mCanvasWidth, mCanvasHeight);
            mCacheBitmapCanvas.drawBitmap(mHintRegionBitmap, src, dst, bitmapPaint);
        }
        // END: archie_huang@asus.com
        this.requestRender(null);
	}
	
	private void drawBaseline(Canvas canvas) {
		if(mCacheBitmapCanvas == null)
			return;
		int width = mCacheBitmapCanvas.getWidth();
		
		//canvas.drawLine(0, mDisplayLineTop,  width, mDisplayLineTop, mBaselineSolidPaint);
        canvas.drawLine(0, mDisplayBaseline, width, mDisplayBaseline, mBaselineDashPaint);
        //canvas.drawLine(0, mDisplayLineBottom, width, mDisplayLineBottom, mBaselineSolidPaint);
        
    }

    @Override
    public void setInputPanel(IHandWritePanel panel) {
        // TODO Auto-generated method stub
        editorPanel = panel;
    }

    @Override
    public CharSequence genResultSpannableString() {
        // TODO Auto-generated method stub
        DrawableSpan span = null;
        SpannableStringBuilder ss = null;
      
        if(mCacheBitmapCanvas == null){
        	return null;
        }
        
        int canvasHeight = this.mCacheBitmapCanvas.getHeight();
        
        if(this.editorPanel.needNoteBaseLineItem()&&this.isShowBaseLine){
            span = new NoteHandWriteItem(mHistPathList, mDrawPaint, new RectF(0, mLineTop, 0, mLineBottom), canvasHeight, editorPanel.getFullImageSpanHeight());
        }else{
            span = new NoteHandWriteBaselineItem(mHistPathList, mDrawPaint, canvasHeight, editorPanel.getImageSpanHeight());
        }
        
       
        if(this.editorPanel.needRecognizer()){
            String res = this.editorPanel.getRecognizerResult((NoteHandWriteItem)span);
            
            if(res == null){
                ss = new SpannableStringBuilder(OBJ);
                ss.setSpan(span, 0, OBJ.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            else{
                int color = ((NoteHandWriteItem)span).getColor();
                float strokeWidth = ((NoteHandWriteItem)span).getStrokeWidth();
                ss = new SpannableStringBuilder(res);
                
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
                
            }
            
        }
        else{
            ss = new SpannableStringBuilder(OBJ);
            ss.setSpan(span, 0, OBJ.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        return ss; 
    }

    @Override
    public void loadTimer() {
        // TODO Auto-generated method stub
        String countDownKey = getContext().getResources().getString(R.string.pref_fast_input_value);
        int sendCountDownTimer = getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(countDownKey, DEFAULT_SEND_TIMER);
        
        if (mSendTextCountDown != null) {
            mSendTextCountDown.cancel();
        }       
        
        mSendTextCountDown = new SendTextCountDown(sendCountDownTimer, sendCountDownTimer);
        
        mDrawingCountDown = new DrawingCountDown(DRAWING_COUNT_DOWN_TIMER, DRAWING_COUNT_DOWN_TIMER);
        
    }
    
    private class SendTextCountDown extends CountDownTimer {
        public SendTextCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            // TODO Auto-generated method stub
            if(editorPanel != null&&!isClick(mHistPathList)){
                editorPanel.writeFinished(HandWriteView.this);
            }
           
            clear();
        }

        @Override
        public void onTick(long arg0) {
            // TODO Auto-generated method stub
            
        }
    }

    @Override
    public boolean isDeleteGesture() {
        // TODO Auto-generated method stub
        boolean isBackDelete = false;
        //if  lines > 1 
        if (mHistPathList.size() > 1)
        {
            return false;
        }
        
        if(mSavedPoints.size()<=4)
        {
            //the num of Points is less than 4;
            isBackDelete = pointsFitRule(0,mSavedPoints.size());
        }else{

            //the num of Points is more than 4;
            PointF firstPoint = mSavedPoints.getFirst();
            PointF endPoint = mSavedPoints.getLast();
            if(firstPoint.x > endPoint.x && 
                    Math.abs(endPoint.x - firstPoint.x) > Math.abs(endPoint.y - firstPoint.y)*ratioXtoY)
            {
                isBackDelete = pointsFitRule(1,mSavedPoints.size()-2);
            }
            else
            {
                isBackDelete = false;
            }
        }
        
        return isBackDelete;
    }
    
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
    
    private boolean isClick(LinkedList<PathInfo> pathInfos) {
        if (DEBUG_TOUCH) {
            Log.d(TAG, "isClick()");
            Log.d(TAG, "pathInfos size = " + pathInfos.size());
        }
        //removed by emmanual to fix bug 471462
//        if ((pathInfos.size() == 1) && (NoteHandWriteItem.getPathLength(pathInfos.get(0).getPath()) <= CLICK_TOLERANCE)) {
//            return true;
//        }
        return false;
    }

    @Override
    public void setEnable(boolean isEnable) {
        // TODO Auto-generated method stub
        if(isEnable){
            this.setVisibility(View.VISIBLE);
        }else{
            this.setVisibility(View.GONE);
        }
    }
    
    private Paint getDrawPaint(){
        if(mWritePaint == null){
            mWritePaint = new Paint(mDrawPaint);
        }
        mWritePaint.setStrokeWidth(mDrawPaint.getStrokeWidth()*paintFactor);
        mWritePaint.setColor(mDrawPaint.getColor());
        
        return mWritePaint; 
    }
    
    //add by mars_li for continue mode
    private class DrawingCountDown extends CountDownTimer {
        private static final long ANIMATION_TIME = 50;
        private ValueAnimator mAnimation = null;

        public DrawingCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void startCountDown(boolean isSendTextDirectly) {
        	//BEGIN: RICHARD TEST
        	//END: RICHARD TEST
            // BEGIN: archie_huang@asus.com
            // Remove Pad Handwrite shift mechanism
        	//Begin: show_wang@asus.com
        	//Modified reason: Remove Phone LandSpace Handwrite shift mechanism
        	int IsContinueWrite = getResources().getInteger(R.integer.is_continue_write);
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
        
        @Override
        public void onFinish() {
            if (mHistPathList.isEmpty()) {
                return;
            }

            RectF bounds = getCurrentBounds();
            final boolean isSendTextDirectly = !(bounds.right > mWriteBounds);
            if (isSendTextDirectly) {
                startCountDown(true);
            }
            else {
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
                    	clear();
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
	                     
	                        for (PathInfo pathInfo : mHistPathList) {
	                            pathInfo.transform(shiftMatrix);
	                            mCacheBitmapCanvas.drawPath(pathInfo.getPath(), getDrawPaint());
	                        }
	                        invalidate();
	                        oldValue = newValue;
	                        //mPaths.clear();
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
    
    private RectF getCurrentBounds() {
        RectF result = new RectF();
        for (PathInfo pathInfo : mHistPathList) {
            RectF bounds = new RectF();
            pathInfo.getPath().computeBounds(bounds, true);
            result.union(bounds);
        }
        return result;
    }
    
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
    
    //end mars_li
    
}
