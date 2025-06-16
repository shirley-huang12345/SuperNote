package com.asus.supernote.microview;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.editable.PageEditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class MicroView extends ImageView{
	private RectF viewport = null;
	private float viewportHeight = 0;
	private float viewportWidth = 0;
	private float microViewHeight = 0;
	private float microViewWidth = 0;
	private float viewportPaddingTop = 0;
	private float viewportPaddingLeft = 0;
	private Paint paint = null;
	private float mLastPosX = 0;
	private float mLastPosY = 0;
	private int viewHeight = 0;
	private int viewWidth = 0;
	private int totalHeight = 0;
	private int totalWidth = 0;
	private float widthRatio = 0.9f;
	private float heightRatio = 0.85f;
	private onScrollChangedListener mOnScrollChangedListener = null;
	private boolean mRegenerateMicroView = false;
	private DismissCountDown dismissCountDown = null;
	private Animation dismissAnimation = null;
	private PageEditor mPageEditor = null;
	private Handler handler = new Handler(); 
	Rect mBounds = new Rect();
	private boolean isMicroViewRegenerated = false;
	private boolean isScrollFromTouch = false;
	public void setRegenerateMicroView(boolean mRegenerateMicroView) {
		this.mRegenerateMicroView = mRegenerateMicroView;
	}
	public MicroView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
    	paint.setColor(0xfffe0000);
    	paint.setStrokeWidth((int)getResources().getDimension(R.dimen.microview_red_frame_stroke_width));//smilefish
    	paint.setStyle(Paint.Style.STROKE);
    	dismissCountDown = new DismissCountDown(1500, 1500);
    	dismissAnimation=new AlphaAnimation(1.0f, 0f);
    	dismissAnimation.setDuration(1000);
    	dismissAnimation.setAnimationListener(dismissAnimationListener);
	}

	Animation.AnimationListener dismissAnimationListener = new Animation.AnimationListener(){

		@Override
		public void onAnimationEnd(Animation arg0) {
			if(arg0.getStartTime()!=Long.MIN_VALUE){
				setVisibility(View.INVISIBLE);		
			}
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
			
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			
		}
		
	};
	
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if(changedView==this){
			if(dismissCountDown!=null){
				if(visibility==View.VISIBLE){
					dismissCountDown.start();
				}
				else{
					dismissCountDown.cancel();				
				}
			}
		}
	}
	
	public boolean onHandWritingViewTouchEvent(MotionEvent event){
		if(getVisibility()!=View.VISIBLE){
			return false;
		}	
		getDrawingRect(mBounds);
		mBounds.offset(this.getLeft(), getTop());
		if(mBounds.contains((int)event.getX(), (int)event.getY())){
			event.offsetLocation(-getLeft(), -getTop());
			onTouchEvent(event);
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
    	canvas.drawRect(viewport, paint);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
    	float y = event.getY();
    	switch(event.getAction()){
    	case MotionEvent.ACTION_DOWN:
    		dismissCountDown.cancel();
    		dismissAnimation.cancel();
    		mLastPosX = x;
    		mLastPosY = y;
    		break;
    	case MotionEvent.ACTION_MOVE:
    		if(viewport.contains(x, y)){
    			moveBy(x,y);
    		}
    		else{
    			moveTo(x,y);
    		}
    		mLastPosX = x;
    		mLastPosY = y;
    		break;
    	case MotionEvent.ACTION_UP:
    		dismissCountDown.start();
    		break;
    	}
		return true;
	}

	public void initViewPort(PageEditor pe){
		this.mPageEditor = pe;	
		microViewWidth = getLayoutParams().width*widthRatio;
		microViewHeight = getLayoutParams().height*heightRatio;

		viewportPaddingLeft = getResources().getDimension(R.dimen.thumb_padding_left);
        viewportPaddingTop = getResources().getDimension(R.dimen.thumb_padding_top);
		viewport = new RectF();
		viewport.top = viewportPaddingTop;
		viewport.left = viewportPaddingLeft;
	}	
	
	public void onViewWidthAndHeightChanged(int newViewWidth,int newViewHeight){
		this.viewWidth = newViewWidth; 
		this.viewHeight = newViewHeight;
		this.viewportWidth = (float)viewWidth/totalWidth*microViewWidth;
		viewport.right = viewportWidth+viewport.left;
		this.viewportHeight = (float)viewHeight/totalHeight*microViewHeight;
		viewport.bottom = viewportHeight+viewport.top;
		invalidate();
	}
	
	private void moveBy(float dx,float dy){
		if(dx-mLastPosX==0||dy-mLastPosY==0){
			return;
		}
		viewport.offset(dx-mLastPosX, dy-mLastPosY);
		checkBounds();
	}
	
	public void moveTo(float dx,float dy){
		viewport.offsetTo(dx-viewportWidth/2, dy-viewportHeight/2);
		checkBounds();
	}
	
	private void  checkBounds(){
		if(viewport.left<viewportPaddingLeft){
			viewport.left = viewportPaddingLeft;
			viewport.right = viewportWidth+viewportPaddingLeft;
		}		
		if(viewport.right>microViewWidth+viewportPaddingLeft){
			viewport.right = microViewWidth+viewportPaddingLeft;
			viewport.left = microViewWidth + viewportPaddingLeft -viewportWidth;
		}
		if(viewport.top<viewportPaddingTop){
			viewport.top = viewportPaddingTop;
			viewport.bottom = viewportHeight+viewportPaddingTop;
		}
		if(viewport.bottom>microViewHeight+viewportPaddingTop){
			viewport.bottom = microViewHeight+viewportPaddingTop;
			viewport.top = microViewHeight+viewportPaddingTop-viewportHeight;
		}
		int scrollX = (int) ((float)(viewport.left-viewportPaddingLeft)/microViewWidth*totalWidth);
		int scrollY = (int) ((float)(viewport.top-viewportPaddingTop)/microViewHeight*totalHeight);
		isScrollFromTouch = true;
		mOnScrollChangedListener.onScrollChanged(scrollX, scrollY);
		isScrollFromTouch = false;
		invalidate();
	}
	
	public void onScrollChanged(int scrollX,int scrollY){
		if(getVisibility()!=View.VISIBLE||isScrollFromTouch){
			return;
		}
		dismissCountDown.cancel();
		dismissAnimation.cancel();
		int top = (int) ((float)scrollY/totalHeight*microViewHeight);
		int left = (int) ((float)scrollX/totalWidth*microViewWidth);
		viewport.offsetTo(left+viewportPaddingLeft,top+viewportPaddingTop);
		invalidate();
		dismissCountDown.start();
	}
	
	public void setOnScrollChangedListener(
			onScrollChangedListener mOnScrollChangedListener) {
		this.mOnScrollChangedListener = mOnScrollChangedListener;
	}
	
	public static interface onScrollChangedListener{
		public void onScrollChanged(int scrollX,int scrollY);
	}
	
	public void onTotalSizeChanged(int newTotalWidth,int newTotalHeight){
		this.totalHeight = newTotalHeight;
		this.totalWidth = newTotalWidth;
		viewportWidth = (float)viewWidth/totalWidth*microViewWidth;
		viewport.right = viewportWidth+viewport.left;
		viewportHeight = (float)viewHeight/totalHeight*microViewHeight;
		viewport.bottom = viewportHeight+viewport.top;
		invalidate();
	}
	
	private boolean canMicroViewVisible(){
		if(mPageEditor!=null&&mPageEditor.isScrollBarVisible()){
			if(mPageEditor.getDeviceType()>100 || MetaData.IS_ENABLE_CONTINUES_MODE){
				if(mPageEditor.isReadOnlyMode()){
					return true;
				}else{
					return false;
				}
			}
			else{
				return true;
			}
		}
		return false;
	}
	
	public void setMicroViewVisible(boolean visible){
		if(!visible){
			if(getVisibility()==View.VISIBLE){	
				handler.post(HideMicroViewRunnable);				
			}
			return;
		}
		else if(canMicroViewVisible()){
				handler.post(regenerateMicroViewRunnable);
    	}					
	}
	
	private Runnable HideMicroViewRunnable = new Runnable() {
		@Override
		public void run() {	
			clearAnimation();
			setVisibility(View.INVISIBLE);
		} 
	};
	
	 private Runnable regenerateMicroViewRunnable= new Runnable() {  
		
		 	@Override
	        public void run() {  
	        	if(mRegenerateMicroView||!isMicroViewRegenerated){	        
	        		Bitmap microViewBmp = mPageEditor.getMicroView();
	        		mPageEditor.drawScreen();
					mRegenerateMicroView = false;
					if(microViewBmp!=null){
						//begin smilefish
						Paint paint = new Paint();
				    	paint.setColor(0xff2f2f2f);
				    	int width = (int)getResources().getDimension(R.dimen.microview_black_frame_stroke_width);
				    	paint.setStrokeWidth(width);
				    	paint.setStyle(Paint.Style.STROKE);
				    	
						Matrix matrix = new Matrix();
						float scaleX = getResources().getDimension(R.dimen.microview_width) / microViewBmp.getWidth();
						float scaleY = getResources().getDimension(R.dimen.microview_height) / microViewBmp.getHeight();
						matrix.setScale(scaleX, scaleY);
						microViewBmp = Bitmap.createBitmap(microViewBmp, 0, 0, microViewBmp.getWidth(), microViewBmp.getHeight(), matrix, true);
				    	Canvas canvas = new Canvas(microViewBmp);
				    	canvas.drawRect(new Rect(1,1,microViewBmp.getWidth()-width,microViewBmp.getHeight()-width), paint);
				    	//end smilefish
				    	
						setImageBitmap(microViewBmp);
						isMicroViewRegenerated = true;
					}	
				}	             	        			
				if(getVisibility()!=View.VISIBLE){
					setVisibility(View.VISIBLE);
				}
	        }  
	    };
	
	private class DismissCountDown extends CountDownTimer{

		public DismissCountDown(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {	
	    	startAnimation(dismissAnimation);			
		}

		@Override
		public void onTick(long arg0) {
			
		}
	}
}
