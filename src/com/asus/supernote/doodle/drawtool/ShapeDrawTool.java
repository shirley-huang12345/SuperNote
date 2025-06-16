package com.asus.supernote.doodle.drawtool;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo.Point;
import com.asus.supernote.doodle.drawinfo.ShapeDrawInfo;

public class ShapeDrawTool extends DrawTool {

    public static final int ARC_TOOL = DrawTool.ARC_TOOL;

    public ShapeDrawTool(int toolCode) {
        super(toolCode);
    }

    @Override
    protected void doDraw(Canvas canvas, DrawInfo drawInfo) {
        ShapeDrawInfo shapeDrawInfo;
        Paint paint;
        Point[] points;

        if (drawInfo == null) {
            return;
        }

        shapeDrawInfo = (ShapeDrawInfo) drawInfo;
        paint = shapeDrawInfo.getPaint();
        points = shapeDrawInfo.getPoints();

        switch (mToolCode) {
            case ARC_TOOL:

            	MyPoint mp0 = new MyPoint(points[0].x,points[0].y);
            	MyPoint mp1 = new MyPoint(points[1].x,points[1].y);
            	MyPoint mp2 = new MyPoint(points[2].x,points[2].y);
            	MyPoint mp3 = new MyPoint(points[3].x,points[3].y);
            	
            	
            	double addRotate = 0;
            	MyPoint vectorNow = mp1.getDistance(mp0);
            	double cosAddRotate =	vectorNow.getCosTwoPoint(new MyPoint(1,0));
            	addRotate =  Math.acos(cosAddRotate);
            	if(!vectorNow.getDirect(new MyPoint(1,0)))
            	{
            		addRotate = 0 -addRotate;
            	}
            	addRotate = Math.toDegrees(addRotate);
            	
                float arcInfo[] =  shapeDrawInfo.getArcInfo();
            	float centerPointX = (points[0].x + points[1].x + points[2].x + points[3].x)/4;//arcInfo[0];
            	float centerPointY = (points[0].y + points[1].y + points[2].y + points[3].y)/4;//arcInfo[1];
            	
            	float maxRadius = (float)(mp1.getDistance(mp0)).getAbs()/2;
            	float minRadius = (float)(mp3.getDistance(mp0)).getAbs()/2;
            	
            	float beginArg = calculatorRealEllipseArg(arcInfo[4],maxRadius,minRadius);//arcInfo[4];
            	float endArg = calculatorRealEllipseArg(arcInfo[4] + arcInfo[5],maxRadius,minRadius);
            	float sweepArg = endArg - beginArg;
            	
            	canvas.rotate(0 - (float)addRotate,centerPointX,centerPointY);
            	RectF oval = new RectF(centerPointX - maxRadius,centerPointY - minRadius,centerPointX + maxRadius,centerPointY + minRadius);
            	canvas.drawArc(oval, beginArg, sweepArg, false, paint);
            	canvas.rotate((float)addRotate,centerPointX,centerPointY);
            	
            	break;
        }
    }

    //BEGIN: RICHARD
    private float calculatorRealEllipseArg(float arg, float maxR, float minR)
    {
    	double argRad =arg * Math.PI/180;
    	Double tangValue = Math.tan(argRad);
    	if(tangValue == 0 || tangValue.isNaN())
    	{
    		return arg;
    	}
    	
    	double x = 1/Math.sqrt((1/(maxR*maxR) + tangValue*tangValue/(minR*minR)));
    	
    	if(x >= maxR)//Calculation precision lead
    	{
    		x = maxR;
    	}
    	
    	if(Math.cos(argRad) < 0)
    	{
    		x = -x;
    	}
    	
    	double realArgRad = Math.acos(x/maxR);
    	
    	if(Math.sin(argRad) < 0)
    	{
    		realArgRad = -realArgRad;
    	}
    	double realArgDeg = Math.toDegrees(realArgRad);
    	
    	while(arg - realArgDeg > 90)
    	{
    		realArgDeg += 360;
    	}
    	
    	while(arg - realArgDeg < -90)
    	{
    		realArgDeg -= 360;
    	}
    	
    	return (float)realArgDeg;
    }
    //END: RICHARD
    
    @Override
    public DrawInfo getDrawInfo(Paint usedPaint) {
            return new ShapeDrawInfo(this, usedPaint);
    }

}
