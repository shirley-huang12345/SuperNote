package com.asus.supernote.editable;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout.LayoutParams;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.ga.GACollector;

public class PageEditorScrollBar {
		
    //BEGIN: RICHARD SCROLLBAR
    private View mEditTextHorizontScroller = null;    
    private View mEditTextVerticalScroller = null;
    //begin jason
    private EditorScrollBarContainer mEditTextHorizontScrollerContainer = null;    
    private EditorScrollBarContainer mEditTextVerticalScrollerContainer = null;
    //end jason
    private boolean mIsChangeScrollbar = false;
    private int mSoftKeyBoardHeight = 0;
    private int mVerticalScrollbarContainerHeight = 0;
    private int mMarginBottom = 0;
    private Context mContext = null;
    private PageEditor mPageEditor = null;
    private View mCurrentScroller = null;
    
    private double mVerticalScrollerRation = 0;
    private int mActualMaxScrollY = 0;//
    private int mVisableHeight = 0;//not scale value.
	private int mVerticalScrollerContainerOriginalBottom = 0;
    
    private double mHorizontScrollerRation = 0;
    private int mActualMaxScrollX = 0;//
    private int mVisableWidth = 0;//not scale value.   
	private int mScrollX;
	private int mScrollY;
    //END: RICHARD

    public PageEditorScrollBar(Context context,PageEditor pe,NoteFrameLayout layout)
    {
    	mContext = context;
    	mPageEditor = pe; 
    	// begin jason update 
        mEditTextHorizontScrollerContainer = (EditorScrollBarContainer)layout.findViewById(R.id.horizontalScrollBarContainer);
        mEditTextVerticalScrollerContainer = (EditorScrollBarContainer)layout.findViewById(R.id.verticalScrollBarContainer);
        mEditTextHorizontScrollerContainer.setPageEditor(pe);
        mEditTextVerticalScrollerContainer.setPageEditor(pe);
        //end jason
        mSoftKeyBoardHeight = 0;
        mVerticalScrollbarContainerHeight = 0;
        mMarginBottom = 300;
    }

	//BEGIN: RICHARD SCROLLERBAR
    public void initScrollerBar(NoteFrameLayout layout,Boolean isPhoneSizeMode) {   	
        mEditTextVerticalScroller = layout.findViewById(R.id.editTextScroller);
        if (mEditTextVerticalScroller != null) {
            mEditTextVerticalScroller.setOnTouchListener(mOnVerticalScrollBarTouchListener);
            //begin smilefish
            mEditTextVerticalScroller.setOnHoverListener(new View.OnHoverListener() {
				
				@Override
				public boolean onHover(View arg0, MotionEvent arg1) {
					switch (arg1.getActionMasked()) {
					case MotionEvent.ACTION_HOVER_ENTER:
						mEditTextVerticalScroller.setBackgroundResource(R.drawable.edit_scrollbar_fat_p);
						break;
					case MotionEvent.ACTION_HOVER_MOVE:
						break;
					case MotionEvent.ACTION_HOVER_EXIT:
						mScrollbarHandler.removeMessages(SCROLLBAR_PORTRAIT);
						mScrollbarHandler.sendEmptyMessageDelayed(SCROLLBAR_PORTRAIT, 500);						
						break;
					}
					return true;
				}
			});
            //end smilefish
        }
        mEditTextHorizontScroller = layout.findViewById(R.id.editTextHoriScroller);
        if (mEditTextHorizontScroller != null) {
            mEditTextHorizontScroller.setOnTouchListener(mOnHorizontalScrollBarTouchListener);
            //begin smilefish
            mEditTextHorizontScroller.setOnHoverListener(new View.OnHoverListener() {
				
				@Override
				public boolean onHover(View arg0, MotionEvent arg1) {
					switch (arg1.getActionMasked()) {
					case MotionEvent.ACTION_HOVER_ENTER:
						mEditTextHorizontScroller.setBackgroundResource(R.drawable.edit_scrollbar_fat_l);
						break;
					case MotionEvent.ACTION_HOVER_MOVE:
						break;
					case MotionEvent.ACTION_HOVER_EXIT:
						mScrollbarHandler.removeMessages(SCROLLBAR_LANDSCAPE);
						mScrollbarHandler.sendEmptyMessageDelayed(SCROLLBAR_LANDSCAPE, 500);
						break;
					}
					return true;
				}
			});
            //end smilefish
        }

        // set Scroll bar container
        EditorScrollBarContainer container = (EditorScrollBarContainer) layout.findViewById(R.id.verticalScrollBarContainer);

        container = (EditorScrollBarContainer) layout.findViewById(R.id.horizontalScrollBarContainer);
        
        //RICHARD TEST
        mIsChangeScrollbar = true;//!mPageEditor.ChangeVerticalScrollBarVisible();//Allen
    }
    
    //begin smilefish
    private static final int SCROLLBAR_PORTRAIT=1;
    private static final int SCROLLBAR_LANDSCAPE=2;
    private Handler mScrollbarHandler=new Handler(){
    	@Override
        public void handleMessage(Message msg) {
    		switch (msg.what) {
    		case SCROLLBAR_PORTRAIT:
    			mEditTextVerticalScroller.setBackgroundResource(R.drawable.asus_ep_scrollbar_n);
    			break;
    		case SCROLLBAR_LANDSCAPE:
				mEditTextHorizontScroller.setBackgroundResource(R.drawable.scroller_phone_down_dis);
    			break;
    		}
    	}
    };
    //end smilefish
    
    private OnTouchListener mOnVerticalScrollBarTouchListener = new OnTouchListener() {

        private float original = 0;
        private float originalX = 0;
        private float origianlY = 0;
        private float scrolling = 0;
        //BEGIN: RICHARD
        private Boolean mStartScroll = false;
        //END: RICHARD

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int viewHeight = v.getHeight();
            int parentHeight = ((View) v.getParent()).getHeight();
            int top = (int) v.getTranslationY();
            int bottom = top + viewHeight;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                original = event.getRawY();
                originalX = event.getRawX();
                origianlY = original;
                if (event.getToolType(event.getActionIndex())==MotionEvent.TOOL_TYPE_STYLUS ){ //smilefish
                	v.setBackgroundResource(R.drawable.edit_scrollbar_fat_p);
                	mScrollbarHandler.removeMessages(SCROLLBAR_PORTRAIT);
                }
                
                mStartScroll = true;
                mCurrentScroller = v;
                mPageEditor.requestToBeCurrent();
            }
            else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            	if(!mStartScroll)
            	{
            		return false;
            	}
                scrolling = event.getRawY() - original;
                original = event.getRawY();
                if ((bottom + (int) scrolling) > parentHeight) {
                    scrolling = parentHeight - bottom;
                }
                else if ((top + (int) scrolling) < 0) {
                    scrolling = 0 - top;
                }
                int y = (int)((top+(int)scrolling)*mVerticalScrollerRation);
        		
                v.setTranslationY(top + (int) scrolling);
                v.requestLayout();
                mPageEditor.setMicroViewVisible(true);//Allen
                mPageEditor.ScrollViewTo(-1,y,false);//Allen
            }
            else if (event.getAction() == MotionEvent.ACTION_UP) {
            	if (event.getToolType(event.getActionIndex())==MotionEvent.TOOL_TYPE_STYLUS ){ //smilefish          	
            		mScrollbarHandler.removeMessages(SCROLLBAR_PORTRAIT);
            		mScrollbarHandler.sendEmptyMessageDelayed(SCROLLBAR_PORTRAIT, 500);
            	}
                mStartScroll = false;
                mCurrentScroller = null;
               //add by mars_li for QC240582
                if(Math.abs(event.getRawX()-originalX)<4&&
                        Math.abs(event.getRawY() - origianlY) <4&&
                        mPageEditor != null){
                        mPageEditor.clickScrollBar(event.getRawX(), event.getRawY());
                }
                //end mars_li
                
                if(MetaData.IS_GA_ON)
				{
					GACollector gaCollector = new GACollector(mContext);
					gaCollector.scrollBar(mPageEditor.getEditorUiUtility().getInputMode(), "vertical");
				}
            }
            
            return true;
        }
    };
    
    private OnTouchListener mOnHorizontalScrollBarTouchListener = new OnTouchListener() {

        private float original = 0;
        private float scrolling = 0;
        //BEGIN: RICHARD
        private Boolean mStartScroll = false;
        //END: RICHARD

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int viewWidth = v.getWidth();
            int parentWidth = ((View) v.getParent()).getWidth();
            int left = (int) v.getTranslationX();
            int right = left + viewWidth;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                original = event.getRawX();
                if (event.getToolType(event.getActionIndex())==MotionEvent.TOOL_TYPE_STYLUS ){ //smilefish
                	v.setBackgroundResource(R.drawable.edit_scrollbar_fat_l);
                	mScrollbarHandler.removeMessages(SCROLLBAR_LANDSCAPE);
                }
                mStartScroll = true;
                mCurrentScroller = v;
                mPageEditor.requestToBeCurrent();
            }
            else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            	if(!mStartScroll)
            	{
            		return false;
            	}
                scrolling = event.getRawX() - original;

                if ((right + (int) scrolling) > parentWidth) {
                    scrolling = parentWidth - right;
                }
                else if ((left + (int) scrolling) < 0) {
                    scrolling = 0 - left;
                }
                original = event.getRawX();

                int x = (int)((left + (int) scrolling) *mHorizontScrollerRation);
                v.setTranslationX(left + (int) scrolling);
                v.requestLayout();
                mPageEditor.setMicroViewVisible(true);//Allen
                mPageEditor.ScrollViewTo(x,-1,false);
            }
            else if (event.getAction() == MotionEvent.ACTION_UP) {
            	if (event.getToolType(event.getActionIndex())==MotionEvent.TOOL_TYPE_STYLUS ){ //smilefish
            		mScrollbarHandler.removeMessages(SCROLLBAR_LANDSCAPE);
            		mScrollbarHandler.sendEmptyMessageDelayed(SCROLLBAR_LANDSCAPE, 500);
            	}
                mStartScroll = false;
                mCurrentScroller = null;
                
                if(MetaData.IS_GA_ON)
				{
					GACollector gaCollector = new GACollector(mContext);
					gaCollector.scrollBar(mPageEditor.getEditorUiUtility().getInputMode(), "horizontal");
				}
            }
            return true;
        }
    };
    
    public void setScrollBarPosition(int xPos, int yPos) {
     	//BEGIN: RICHARD TEST
    	if(mEditTextHorizontScroller != null)
    	{
    		if(mActualMaxScrollX > 0)
    		{
    			if(xPos > mActualMaxScrollX)
    			{
    				xPos = mActualMaxScrollX;
    			}
    			setHorizontalScrollBarPosition((float)(xPos/mHorizontScrollerRation));
    		}
    	}
    	
    	if(mEditTextVerticalScroller != null)
    	{
    		if(mActualMaxScrollY > 0)
    		{
    			if(yPos > mActualMaxScrollY)
    			{
    				yPos = mActualMaxScrollY;
    			}
    			setVerticalScrollBarPosition((float)(yPos/mVerticalScrollerRation));
    		}
    	}
    	
     } 
    //End Allen
    private void setHorizontalScrollBarPosition(float x) {
        if (mEditTextHorizontScroller == null) {
            return;
        }
        mEditTextHorizontScroller.setTranslationX(x);
        mEditTextVerticalScroller.requestLayout();//Allen
    }

    private void setVerticalScrollBarPosition(float y) {
        if (mEditTextVerticalScroller == null) {
            return;
        }
        mEditTextVerticalScroller.setTranslationY(y);
        mEditTextVerticalScroller.requestLayout();//Allen
    }

    
    public int getVerticalScrollbarContainerHeight() {
		return mVerticalScrollbarContainerHeight;
	}
	
	private Boolean isClickOnView(View vi,MotionEvent event)
	{
		if(vi == null || View.VISIBLE != vi.getVisibility())
		{
			return false;
		}
		
		int[] location = new  int[2] ;
		vi.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐�?
		
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
	
	public Boolean dispatchPointerEventToScroll(MotionEvent event)
	{
		if(mCurrentScroller == null)
		{
			Boolean res = false;
			if(event.getAction() == MotionEvent.ACTION_DOWN)
			{
				if(isClickOnView(mEditTextVerticalScroller,event))
				{
					res = mEditTextVerticalScroller.dispatchTouchEvent(event);
				}
				else if(isClickOnView(mEditTextHorizontScroller,event))
				{
				    res = mEditTextVerticalScroller.dispatchTouchEvent(event);
				}
			}
			return res;
		}
		else
		{
		    return mCurrentScroller.dispatchTouchEvent(event);
		}
	}

	public void setVerticalScrollRightMargin(int pos) {
		if(pos > 0)
		{
			//begin smilefish fix bug 303689
			if(mEditTextVerticalScrollerContainer.getMeasuredWidth() == 0)
				mEditTextVerticalScrollerContainer.measure(0, 0);
			if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
				pos -= mEditTextVerticalScrollerContainer.getMeasuredWidth()/2;
			else
				pos -= mEditTextVerticalScrollerContainer.getMeasuredWidth();
			//end smilefish
		}
		if(pos < 0)
		{
			pos = 0;
		}
		((LayoutParams)(mEditTextVerticalScrollerContainer.getLayoutParams())).rightMargin = pos;
		
	}
	
	public void setVerticalScrollTopAndBottomMargin(int top,int bottom ) {
		((LayoutParams)(mEditTextVerticalScrollerContainer.getLayoutParams())).topMargin = top;
		((LayoutParams)(mEditTextVerticalScrollerContainer.getLayoutParams())).bottomMargin = bottom;
		
	}
	
	public int getEditTextVerticalScrollerContainerBottom()
	{
		if(mEditTextVerticalScrollerContainer == null)
		{
			return 0;
		}
		else
		{
			return ((LayoutParams)(mEditTextVerticalScrollerContainer.getLayoutParams())).bottomMargin;
		}
	}
	
	public void restoreEditTextVerticalScrollerContainerBottom(Boolean flag)
	{
		if(((LayoutParams)(mEditTextVerticalScrollerContainer.getLayoutParams())).bottomMargin != 0)
		{
			//if bottomMargin is 0. The Value is not Original.
			mVerticalScrollerContainerOriginalBottom = ((LayoutParams)(mEditTextVerticalScrollerContainer.getLayoutParams())).bottomMargin;
			if(flag)
			{
				return;
			}
		}
		if(flag)
		{
			((LayoutParams)(mEditTextVerticalScrollerContainer.getLayoutParams())).bottomMargin = mVerticalScrollerContainerOriginalBottom;
			if(mEditTextHorizontScrollerContainer != null)
			{
				((LayoutParams)(mEditTextHorizontScrollerContainer.getLayoutParams())).bottomMargin = mVerticalScrollerContainerOriginalBottom;
			}
		}
		else
		{
			((LayoutParams)(mEditTextVerticalScrollerContainer.getLayoutParams())).bottomMargin = 0;
			if(mEditTextHorizontScrollerContainer != null)
			{
				((LayoutParams)(mEditTextHorizontScrollerContainer.getLayoutParams())).bottomMargin = 0;
			}
		}
	}
	
	public void updateVerticalScrollerHeight(int viewHeight,int totalHeight)
	{		
		if(mEditTextVerticalScrollerContainer == null)
			return;
		
		mVisableHeight = viewHeight;
		mActualMaxScrollY = (int)(totalHeight*mPageEditor.getScaleY() - mVisableHeight);
		//begin smilefish
		if(mPageEditor.getDeviceType() == MetaData.DEVICE_TYPE_320DP && mPageEditor.isPhoneSizeMode()
				&& mActualMaxScrollY < 30){
			mActualMaxScrollY -= 30;
		}
		//end smilefish
		if(mActualMaxScrollY <= 0)
		{
			mActualMaxScrollY = 0;
	        Handler handler = new Handler();
	        handler.post(new Runnable() {
	            @Override
	            public void run() {
	            	mEditTextVerticalScroller.setVisibility(View.GONE);
	            	mEditTextVerticalScrollerContainer.setVisibility(View.GONE);
	            	mEditTextVerticalScrollerContainer.requestLayout();
	            	mPageEditor.setMicroViewVisible(false);//Allen
	            }
	        });
			return;
		}
		
		int verticalScrollerContainerHeight = mVisableHeight ;
		if(mPageEditor.getTemplateLinearLayoutTopMargin() == 0)
		{
			//adjust verticalScrollerContainerHeight
			verticalScrollerContainerHeight -= ((LayoutParams)(mEditTextVerticalScrollerContainer.getLayoutParams())).topMargin;
		}
		
		double scrollerHeight = (mVisableHeight*verticalScrollerContainerHeight*1.0/(totalHeight*mPageEditor.getScaleY()));
		
		double heightFromDimension = mContext.getResources().getDimension(R.dimen.phone_vertical_scroll_bar_height);
		if(scrollerHeight > heightFromDimension)
		{
			scrollerHeight = heightFromDimension;
		}
		else if(scrollerHeight < heightFromDimension/3)
		{
			//avoid scroller too small
			scrollerHeight = Math.min(heightFromDimension/3, verticalScrollerContainerHeight/2);
		}
		mEditTextVerticalScroller.getLayoutParams().height = (int)scrollerHeight;
		
		mVerticalScrollerRation = (totalHeight*mPageEditor.getScaleY() - mVisableHeight + 0.0)/(verticalScrollerContainerHeight - mEditTextVerticalScroller.getLayoutParams().height);
		
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
            	mEditTextVerticalScroller.setVisibility(View.VISIBLE);
            	mEditTextVerticalScrollerContainer.setVisibility(View.VISIBLE);
            	mEditTextVerticalScrollerContainer.requestLayout();
            	mEditTextVerticalScroller.requestLayout();
            }
        });
	}

	public int getMaxScrollY() {
		return mActualMaxScrollY;
	}

	public void updateHorizontScrollerWidth(int viewWidth,int totalWidth)
	{		
		if(mEditTextHorizontScrollerContainer == null)
			return;
		mVisableWidth = viewWidth;
		mActualMaxScrollX = (int)(totalWidth*mPageEditor.getScaleX() - mVisableWidth);
		if(mActualMaxScrollX <= 0)
		{
			mActualMaxScrollX = 0;
			mPageEditor.ScrollViewTo(0,-1,true);//adjust position
	        Handler handler = new Handler();
	        handler.post(new Runnable() {
	            @Override
	            public void run() {
	            	mEditTextHorizontScroller.setVisibility(View.GONE);
	            	mEditTextHorizontScrollerContainer.setVisibility(View.GONE);
	            	mEditTextHorizontScrollerContainer.requestLayout();
	            }
	        });
			return;
		}
		int horizontScrollerContainerWidth = mVisableWidth - ((LayoutParams)(mEditTextHorizontScrollerContainer.getLayoutParams())).rightMargin;
		horizontScrollerContainerWidth -= ((LayoutParams)(mEditTextHorizontScrollerContainer.getLayoutParams())).leftMargin;
		
		double scrollerWidth = (viewWidth*horizontScrollerContainerWidth*1.0/(totalWidth*mPageEditor.getScaleX()));
		
		double widthFromDimension = mContext.getResources().getDimension(R.dimen.phone_vertical_scroll_bar_height);
		if(scrollerWidth > widthFromDimension)
		{
			scrollerWidth = widthFromDimension;
		}
		mEditTextHorizontScroller.getLayoutParams().width = (int)scrollerWidth;
		
		mHorizontScrollerRation = (totalWidth*mPageEditor.getScaleX() - mVisableWidth + 0.0)/(horizontScrollerContainerWidth - mEditTextHorizontScroller.getLayoutParams().width);
		
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
            	mEditTextHorizontScroller.setVisibility(View.VISIBLE);
            	mEditTextHorizontScrollerContainer.setVisibility(View.VISIBLE);
            	mEditTextHorizontScrollerContainer.requestLayout();
            	mEditTextHorizontScroller.requestLayout();
            }
        });
	}

	public int getMaxScrollX() {
		return mActualMaxScrollX;
	}

	public int getScrollX() {
		return mScrollX;
	}

	public void setScrollX(int mScrollX) {
		this.mScrollX = mScrollX;
	}

	public int getScrollY() {
		return mScrollY;
	}

	public void setScrollY(int mScrollY) {
		this.mScrollY = mScrollY;
	}
	
	//add by mars_li for bug about black flash when input board visibility change
	public EditorScrollBarContainer getEditTextVerticalScrollerContainer(){
		return mEditTextVerticalScrollerContainer;
	}
	//end mars

	public EditorScrollBarContainer getEditTextHorizontalScrollerContainer(){
		return mEditTextHorizontScrollerContainer;
	}

}
