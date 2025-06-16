package com.asus.supernote.doodle.drawtool;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.EraseDrawInfo;
import com.asus.supernote.doodle.drawinfo.EraseDrawInfo.TwoPressPointPathInfo;

public class EraseDrawTool extends DrawTool {

    public static final int ERASE_TOOL = DrawTool.ERASE_TOOL;//RICHARD
    private Paint mCurrentPaint;

    public EraseDrawTool() {
        super(ERASE_TOOL);
    }

    public void doDrawLastPath(Canvas canvas, DrawInfo drawInfo)
    {
        if (drawInfo == null) {
            return;
        }
    	EraseDrawInfo eraseDrawInfo;     
        eraseDrawInfo = (EraseDrawInfo) drawInfo;

        if (eraseDrawInfo.isVisible()) {
        	if(eraseDrawInfo.getIsHaveOtherPresser())
        	{
	        	mCurrentPaint = new Paint(eraseDrawInfo.getPaint());
				mCurrentPaint.setStyle(Paint.Style.FILL_AND_STROKE);
				mCurrentPaint.setStrokeWidth(1);
				
				TwoPressPointPathInfo info  = eraseDrawInfo.getLastTwoPressPointPathInfo();
				if(info != null)
				{
					for(Path path: info.getPathList())
					{
						canvas.drawPath(path, mCurrentPaint);
					}
				}
        	}
        	else
        	{
				TwoPressPointPathInfo info  = eraseDrawInfo.getLastTwoPressPointPathInfo();
				if(info != null)
				{
					for(Path path: info.getPathList())
					{
						canvas.drawPath(eraseDrawInfo.getPath(), eraseDrawInfo.getPaint());
					}
				}
			}
        }

    }
    
    @Override
    protected void doDraw(Canvas canvas, DrawInfo drawInfo) {
        EraseDrawInfo eraseDrawInfo;     

        if (drawInfo == null) {
            return;
        }

        eraseDrawInfo = (EraseDrawInfo) drawInfo;

        if (eraseDrawInfo.isVisible()) {
        	//BEGIN: RICHARD
        	if(eraseDrawInfo.getIsHaveOtherPresser())
        	{
	        	mCurrentPaint = new Paint(eraseDrawInfo.getPaint());
				mCurrentPaint.setStyle(Paint.Style.FILL_AND_STROKE);
				mCurrentPaint.setStrokeWidth(1);
				
				for(TwoPressPointPathInfo info :eraseDrawInfo.getTwoPressPointPathInfoList())
				{
					for(Path path: info.getPathList())
					{
						canvas.drawPath(path, mCurrentPaint);
					}
				}
        	}
        	else
        	{
				canvas.drawPath(eraseDrawInfo.getPath(), eraseDrawInfo.getPaint());
        	}
			//END: RICHARD
        }
    	//Log.d("RICHARD2","EraseDrawTool do Draw " + (SystemClock.uptimeMillis() - mTeststartTime) +"rect is  "+  eraseDrawInfo.getDirty());
    }

    @Override
    public DrawInfo getDrawInfo(Paint usedPaint) {
        return new EraseDrawInfo(this, usedPaint);
    }

}
