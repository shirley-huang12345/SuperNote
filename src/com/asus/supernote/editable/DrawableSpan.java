package com.asus.supernote.editable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ReplacementSpan;

public class DrawableSpan extends ReplacementSpan {

    /**
     * A constant indicating that the bottom of this span should be aligned with
     * the bottom of the surrounding text, i.e., at the same level as the lowest
     * descender in the text.
     */
    public static final int ALIGN_BOTTOM = 0;

    /**
     * A constant indicating that the bottom of this span should be aligned with
     * the baseline of the surrounding text.
     */
    public static final int ALIGN_BASELINE = 1;

    public static final int DRAW_SHIFT = 3;

    protected Drawable mDrawable;
    protected final int valign;

    private boolean mIsVisible = true;

    /**
     * @param valign
     *            one of {@link #ALIGN_BOTTOM} or {@link #ALIGN_BASELINE}.
     */
    public DrawableSpan(Drawable _d, int _valign) {
        mDrawable = _d;
        valign = _valign;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
            float x, int top, int y, int bottom, Paint paint) {

        if (!mIsVisible) {
            return;
        }

        Drawable b = getDrawable();
        if (b == null) {
            return;
        }
        canvas.save();

        int transY;
        if (ALIGN_BOTTOM == valign) {
            transY = bottom;
        }
        else { // ALIGN_BASELINE == valign
            transY = y + DRAW_SHIFT;
        }
        transY -= b.getBounds().bottom;
        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end,
            Paint.FontMetricsInt fm) {

        Drawable b = getDrawable();

        if (b == null) {
            return 0;
        }

        Rect rect = b.getBounds();

        return rect.right;
    }

    /**
     * Returns the vertical alignment of this span, one of {@link #ALIGN_BOTTOM} or {@link #ALIGN_BASELINE}.
     */
    public int getValign() {
        return valign;
    }

    public void setVisible(boolean visible) {
        mIsVisible = visible;
    }
    /**set new font height to regenerate drawableSpan
     * @author Allen
     * @param fontHeight: the new font height
     */
    public void setFontHeight(int fontHeight){
    	//Override by subclass
    }
}
