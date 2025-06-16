package com.asus.supernote.doodle.drawinfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

import com.asus.supernote.PaintSelector;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem.SerDrawInfo;
import com.asus.supernote.doodle.DoodleItem.SerGraphicInfo;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawtool.GraphicDrawTool;

public class GraphicDrawInfo extends TransformDrawInfo {
    private static final String TAG = "GraphicDrawInfo";
    private String mFileName = null;
    private Bitmap mOriBitmap;
    
    private int mWidth = 0;
    private int mHeight = 0;

    //begin jason
    private boolean isShapeGrapich=false;
    private Path mShapePath=null;
    private int mShapeType=-1;
    public GraphicDrawInfo(int type,DrawTool drawTool, Paint usedPaint){
    	super(drawTool, usedPaint);
    	mShapeType=type;
    	isShapeGrapich=true;
    }
    //end jason
    public GraphicDrawInfo(Bitmap bitmap, DrawTool drawTool, Paint usedPaint) {
        super(drawTool, usedPaint);
        mOriBitmap = bitmap;

        // We need control points because we need to know the Graphic's bounds
        initControlPoints(0, 0, mOriBitmap.getWidth(), mOriBitmap.getHeight());
    }
    
    @Override
    public DrawInfo cloneLock() {
    	//begin jason
    	if (isShapeGrapich) {
			GraphicDrawInfo shapeDrawInfo=(GraphicDrawInfo) (new GraphicDrawTool(mShapeType).getDrawInfo(mPaint));
			shapeDrawInfo.mFileName = mFileName;
			shapeDrawInfo.mTransMatrix = new Matrix(mTransMatrix);
			shapeDrawInfo.reSetPaint(GraphicDrawInfo.createPaintForShapeGraphic(shapeDrawInfo));
			shapeDrawInfo.mWidth = mWidth;
			shapeDrawInfo.mHeight = mHeight;
			shapeDrawInfo.mShapePath=new Path(mShapePath);
			shapeDrawInfo.initControlPointsForShape();
			clone(shapeDrawInfo);
			return shapeDrawInfo;
		}
    	//end jason
        Bitmap bitmap = null;
        try {
            bitmap = mOriBitmap.copy(Bitmap.Config.ARGB_8888, false);
        }
        catch(OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] cloneLock() failed !!!");
        }
        if (bitmap == null) {
            return null;
        }
        GraphicDrawInfo drawInfo = (GraphicDrawInfo) (new GraphicDrawTool(bitmap).getDrawInfo(mPaint));
        drawInfo.mFileName = mFileName;
        drawInfo.mTransMatrix = new Matrix(mTransMatrix);
        drawInfo.mWidth = mWidth;
        drawInfo.mHeight = mHeight;
        clone(drawInfo);
        
        return drawInfo;
    }

    public String getFileName() {
        return mFileName;
    }
    
    public int getWidth() {
    	return mWidth;
    }
    
    public void setWidth(int width) {
    	mWidth = width;
    }
    
    public int getHeight() {
    	return mHeight;
    }
    
    public void setHeight(int height) {
    	mHeight = height;
    }
    private String getFilePath(NotePage note) {
        if (mFileName == null) {
            String directory = note.getFilePath();
            mFileName = mOriBitmap.toString();
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(new File(directory, mFileName));
                mOriBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
   
        return mFileName;
    }
    //begin jason
    public void reStoreBitmap(NotePage note){
    	getFilePath(note);
    }
    //end jason
    public Bitmap getGraphic() {
        return mOriBitmap;
    }

    public void releaseMemory() {
        if ((mOriBitmap != null) && !mOriBitmap.isRecycled()) {
            mOriBitmap.recycle();
            mOriBitmap = null;
        }
    }

    @Override
    public SerDrawInfo saveLock(NotePage note) {
        SerGraphicInfo serInfo = new SerGraphicInfo();
        String fileName = getFilePath(note);
        serInfo.setFileName(fileName);
        serInfo.setPaintTool(mDrawTool.getToolCode());
        serInfo.setTransform(mTransMatrix);
        serInfo.setWidth(mWidth);
        serInfo.setHeight(mHeight);
        return serInfo;
    }

    public void setFileName(String name) {
        mFileName = name;
    }
    //begin jason
    public Path getShapeGraphicPath(){
    	return mShapePath;
    }
    public void setShapeGraphicPath(Path path){
    	mShapePath=path;
    }
    public void initControlPointsForShape(){
    	if (isShapeGrapich) {
        	RectF rectF=new RectF();
        	Path path=getPathToDraw();
        	if (path==null) {
				return;
			}
        	path.computeBounds(rectF, false);
        	initControlPoints(rectF);
		}
    }
    public Path getPathToDraw(){
    	if (mShapePath==null) {
			return null;
		}
    	Path p=new Path(mShapePath);
    	Matrix m=getTransformMatrix();
    	if (m!=null) {
			p.transform(m);
		}
    	return p;
    }
    
    public static Paint createPaintForShapeGraphic(GraphicDrawInfo d){
   	 Paint paint=new Paint();
   	 int width=5;
      PaintSelector.initPaint(paint, Color.BLACK, width);
      return paint;
    }
    public void reSetPaint(Paint p){
    	mPaint=p;
    }
    public boolean isShapeGraphic(){
    	return isShapeGrapich;
    }
    public int getShapeGraphicType(){
    	return mShapeType;
    }
}
