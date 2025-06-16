package com.asus.supernote.doodle.drawtool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.GraphicDrawInfo;

public class GraphicDrawTool extends DrawTool {

    public static final int GRAPHIC_TOOL = DrawTool.GRAPHIC_TOOL;
    private GraphicDrawInfo mDrawInfo = null;
    private final Bitmap mBitmap;

    public GraphicDrawTool(Bitmap bitmap) {
        super(GRAPHIC_TOOL);
        mBitmap = bitmap;
    }
    //begin jason
    private int mShapeType=-1;
    public GraphicDrawTool(int type){
    	super(GRAPHIC_TOOL);
    	mBitmap=null;
    	mShapeType=type;
    }
    //end 
    @Override
    protected void doDraw(Canvas canvas, DrawInfo drawInfo) {
        GraphicDrawInfo graphicDrawInfo = (GraphicDrawInfo) drawInfo;
        //begin jason
        if (graphicDrawInfo.isShapeGraphic()) {
			canvas.drawPath(graphicDrawInfo.getPathToDraw(), graphicDrawInfo.getPaint());
			return;
		}
        //end jason
        
        if(canvas != null && graphicDrawInfo.getGraphic() != null   //smilefish fix NullPointerException
        		&& graphicDrawInfo.getTransformMatrix() != null && graphicDrawInfo.getPaint() != null){
        	canvas.drawBitmap(graphicDrawInfo.getGraphic(), graphicDrawInfo.getTransformMatrix(), graphicDrawInfo.getPaint());
        }
    }

    @Override
    public DrawInfo getDrawInfo(Paint usedPaint) {
        if (mDrawInfo == null) {
        	//begin jason
        	if (mShapeType!=-1&&mBitmap==null) {
				mDrawInfo=new GraphicDrawInfo(mShapeType, this, new Paint());
				return mDrawInfo;
			}
        	//end jason
            mDrawInfo = new GraphicDrawInfo(mBitmap, this, usedPaint);
        }
        return mDrawInfo;
    }

    public void recycle() {
        if ((mBitmap != null) && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap.recycle();
        }
    }
}
