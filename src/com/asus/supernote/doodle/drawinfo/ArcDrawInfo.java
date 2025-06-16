package com.asus.supernote.doodle.drawinfo;

import android.graphics.Paint;
import android.graphics.RectF;

import com.asus.supernote.doodle.drawtool.DrawTool;
import com.visionobjects.myscript.shape.ShapeEllipticArcData;

public class ArcDrawInfo extends ShapeDrawInfo {

    public ArcDrawInfo(DrawTool drawTool, Paint usedPaint) {
        super(drawTool, usedPaint);
    }

    public ArcDrawInfo(DrawTool drawTool, Paint usedPaint, short[] points) {
        super(drawTool, usedPaint, points);
    }
    
    public ArcDrawInfo(DrawTool drawTool, Paint usedPaint, short[] points,float[] info) {
        super(drawTool, usedPaint, points,info);
    }
    
    //BEGIN: RICHARD
    public ArcDrawInfo(DrawTool drawTool, Paint usedPaint,ShapeEllipticArcData data)
    {
    	super(drawTool, usedPaint,data);
    }
    //END: RICHARD

    @Override
    public RectF getBounds() {
        RectF bounds = addStrokeToBounds(getStrictBounds());
        return bounds;
    }

    @Override
    public RectF getStrictBounds() {
        RectF bounds = new RectF();

        mPath.computeBounds(bounds, false);
        return bounds;//RICHARD
    }
}
