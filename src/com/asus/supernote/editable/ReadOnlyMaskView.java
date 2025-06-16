package com.asus.supernote.editable;

import android.content.Context;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import com.asus.supernote.EditorUiUtility;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;

public class ReadOnlyMaskView extends View {
    private static final float TOUCH_TOLERANCE = 10f; //smilefish
    private static final float LIMIT_ANGLE_TAN = 1.5f;

    private PageEditor mPageEditor = null;
    private GestureDetector mDetector = null;
    // BEGIN: archie_huang@asus.com
    // Used to detect click event
    private Path mPath = new Path();

    // END: archie_huang@asus.com

    public ReadOnlyMaskView(Context context) {
        super(context);
        initGestureListener();
    }

    public ReadOnlyMaskView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initGestureListener();
    }

    private void initGestureListener() {
        mDetector = new GestureDetector(new ReadOnlyMaskGesture());
    }

    public void disable() {
        this.setVisibility(View.GONE);
    }

    public void enable() {
        this.setVisibility(View.VISIBLE);
    }

    public void setPageEditor(PageEditor editor) {
        mPageEditor = editor;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // BEGIN: archie_huang@asus.com
        // Add for clicking icon in read only mode
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                mPath.lineTo(event.getX(), event.getY());
                if (NoteHandWriteItem.getPathLength(mPath) < TOUCH_TOLERANCE) {
                	//BEGIN: RICHARD
                	dispatchClickOnView(event);
                	//END: RICHARD
                }
                mPath.reset();
                break;
        }
        // END: archie_huang@asus.com
        mDetector.onTouchEvent(event);
        return true;
    }

    public class ReadOnlyMaskGesture implements OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {           
            mPageEditor.scrollEditText(Math.round(distanceX), Math.round(distanceY));            
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			//BEGIN: RICHARD
        	//shane
        	float ver = Math.abs(e1.getY() - e2.getY());
        	float hor = Math.abs(e1.getX() - e2.getX());
                if ( ver / hor > LIMIT_ANGLE_TAN
                		|| Math.abs(velocityX)<500
                	) {
                    return false;
                    
                }

                if (e2.getX() - e1.getX() < 0) {
                	mPageEditor.requestNextOrPrevPage(true);
                }
                else {
                	mPageEditor.requestNextOrPrevPage(false);
                }
                return true;
			//END: RICHARD
        }

    }
    
    //BEGIN: RICHARD	
    private Boolean dispatchClickOnView(MotionEvent event)
	{ 
			long uptime = SystemClock.uptimeMillis();
			MotionEvent down = MotionEvent.obtain(uptime,1-uptime, MotionEvent.ACTION_DOWN,event.getX()/mPageEditor.mScaleX,  event.getY()/mPageEditor.mScaleY, 0);             
			mPageEditor.TemplateLinearLayoutDispatchMotionEvent(down);
			MotionEvent up = MotionEvent.obtain(uptime,1-uptime, MotionEvent.ACTION_UP,event.getX()/mPageEditor.mScaleX,  event.getY()/mPageEditor.mScaleY, 0);             
			mPageEditor.TemplateLinearLayoutDispatchMotionEvent(up);
			return true;
	}
    //END: RICHARD

}
