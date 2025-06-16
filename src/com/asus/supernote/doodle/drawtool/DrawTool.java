package com.asus.supernote.doodle.drawtool;

import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Matrix;
import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.EraseDrawInfo;

public abstract class DrawTool {
    // New DrawTool should register its tool code here
    // Then publish its tool code in it own class
    // The tool code is used because
    // 1. Some tool code may use the same DrawTool
    // 2. We need tool code to restore all saved data
    public static final int NORMAL_TOOL = 0;
    public static final int NEON_TOOL = 3;
    public static final int GRAPHIC_TOOL = 8;
    public static final int SELECTION_TOOL = 9;
    public static final int ERASE_TOOL = 10;
    public static final int ANNOTATION_TOOL = 11;
    public static final int SCRIBBLE_TOOL = 12;
    public static final int SKETCH_TOOL = 13;
    public static final int MEMO_TOOL = 14;
    public static final int MARKPEN_TOOL = 15;//wendy
    public static final int WRITINGBRUSH_TOOL = 16;//wendy
    public static final int TEXTIMG_TOOL = 17; //By Show
    public static final int ARC_TOOL = 18;//RICHARD

    protected int mToolCode;
    private Paint mBmpPaint;

    public DrawTool(int toolCode) {
        mToolCode = toolCode;
        mBmpPaint = new Paint();
        mBmpPaint.setAntiAlias(true);
        mBmpPaint.setDither(true);
        mBmpPaint.setFilterBitmap(true);
    }

    abstract protected void doDraw(Canvas canvas, DrawInfo drawInfo);

    public Boolean draw(Canvas canvas, boolean isUsetempBitmap, DrawInfo drawInfo, float posX, float posY, float offX, float offY) {
        LinkedList<EraseDrawInfo> eraseInfos;

        if (drawInfo == null) {
            return false;
        }
        eraseInfos = drawInfo.getEraseDrawInfos();

        if ((eraseInfos != null) && (eraseInfos.size() > 0)) {
            if (!isUsetempBitmap) {
                drawInfo.getDrawTool().doDraw(canvas, drawInfo);
                for (EraseDrawInfo eraseInfo : eraseInfos) {
                    eraseInfo.getDrawTool().doDraw(canvas, eraseInfo);
                }
            }
            else {
            	RectF rect = drawInfo.getBounds();
            	try{

            		Matrix matrix = new Matrix();
            		float[] matrixValues = new float[9];
                	canvas.getMatrix(matrix);
                	matrix.getValues(matrixValues);
                	
            		int bmpWidth = (int)rect.width();
            		int bmpHeight = (int)rect.height();
            		float leftMove = rect.left;
            		float topMove = rect.top;
            		if(bmpWidth > canvas.getWidth() / matrixValues[Matrix.MSCALE_X])
            		{
            			bmpWidth = (int)(canvas.getWidth() / matrixValues[Matrix.MSCALE_X]);
            			if(leftMove < 0)
            			{
            				leftMove = 0;
            			}
            		}
            		if(bmpHeight > canvas.getHeight() / matrixValues[Matrix.MSCALE_Y])
            		{
            			bmpHeight =	(int)(canvas.getHeight() / matrixValues[Matrix.MSCALE_Y]);
            			if(topMove < 0)
            			{
            				topMove = 0;
            			}
            		}
	            	Bitmap testBitmap;
	            	try{
	            		testBitmap = Bitmap.createBitmap(bmpWidth,bmpHeight,Bitmap.Config.ARGB_8888);
	            	}catch(OutOfMemoryError err){
		            	testBitmap = Bitmap.createBitmap(bmpWidth,bmpHeight,Bitmap.Config.ARGB_4444);
	            	}
	            	testBitmap.eraseColor(Color.TRANSPARENT);
	            	Canvas bmpCanvas = new Canvas(testBitmap);
	            	bmpCanvas.save();
	            	bmpCanvas.translate(offX - leftMove, offY -topMove);
	            	drawInfo.getDrawTool().doDraw(bmpCanvas, drawInfo);
		              for (EraseDrawInfo eraseInfo : eraseInfos) {
		            	  eraseInfo.getDrawTool().doDraw(bmpCanvas, eraseInfo);
	//              		eraseInfo.getDrawTool().doDraw(canvas, eraseInfo);
		              }
		              bmpCanvas.restore();
		              canvas.drawBitmap(testBitmap, posX + leftMove, posY+topMove, mBmpPaint);
		              testBitmap.recycle();
		              testBitmap = null;
		              bmpCanvas = null;
            	}catch(Exception e)
            	{
            		e.printStackTrace();
                    drawInfo.getDrawTool().doDraw(canvas, drawInfo);
                    for (EraseDrawInfo eraseInfo : eraseInfos) {
                        eraseInfo.getDrawTool().doDraw(canvas, eraseInfo);
                    }
            	}
                
            }
        }
        else {
            drawInfo.getDrawTool().doDraw(canvas, drawInfo);
        }
        
//        Log.d("RICHARD_SHOW_DRAW_TIME","DRAW TIME IS " + (System.nanoTime() - startTime));
        return true;

    }
    
    public void draw(Canvas canvas, boolean isUsetempBitmap, DrawInfo drawInfo) {
    	draw( canvas,  isUsetempBitmap,  drawInfo, 0, 0, 0,  0);

    }

    public abstract DrawInfo getDrawInfo(Paint usedPaint);

    public int getToolCode() {
        return mToolCode;
    }
    
    //carrot
    public void drawPreview(Canvas canvas, DrawInfo drawInfo){
    	draw(canvas, false, drawInfo);
    }
}