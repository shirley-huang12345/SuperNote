package com.asus.supernote.editable;

import java.util.LinkedList;

import android.graphics.Path;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.asus.supernote.data.MetaData;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;

public class LongClickDetector {
    public interface OnLongClickListener {
        public void onLongClick(MotionEvent event);
    }

    private class CountDown extends CountDownTimer {
        public CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            if (mLongClickListeners != null) {
                for (OnLongClickListener listener : mLongClickListeners) {
                    listener.onLongClick(mDownEvent);
                }
            }
            mPath.reset(); // Better
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // TODO Auto-generated method stub
        }
    }

    public static final int LONG_CLICK_TIMER = ViewConfiguration.getLongPressTimeout();//1500;
    private static final float TOUCH_TOLERANCE = 40;
    private static final float TOUCH_TOLERANCE_FOR_PEN = 10;
    private Path mPath = new Path();
    private CountDown mLongClickCountDown = new CountDown(LONG_CLICK_TIMER * 2, LONG_CLICK_TIMER * 2);
    private LinkedList<OnLongClickListener> mLongClickListeners = new LinkedList<OnLongClickListener>();
    private MotionEvent mDownEvent = null;

    public void onTouch(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownEvent = MotionEvent.obtain(event);
                mLongClickCountDown.start();
                mPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(x, y);
                float tolerance = TOUCH_TOLERANCE;
				if (event.getToolType(event.getActionIndex()) == MotionEvent.TOOL_TYPE_STYLUS) {
					tolerance = TOUCH_TOLERANCE_FOR_PEN * MetaData.DPI / MetaData.BASE_DPI; //smilefish fix bug 638224
				}
                if (NoteHandWriteItem.getPathLength(mPath) > tolerance) {
                    mLongClickCountDown.cancel();
                }
                break;
            case MotionEvent.ACTION_UP:
                mLongClickCountDown.cancel();
                mPath.reset();
                break;
        }
    }

    public void addLongClickListener(OnLongClickListener listener) {
        mLongClickListeners.add(listener);
    }
}
