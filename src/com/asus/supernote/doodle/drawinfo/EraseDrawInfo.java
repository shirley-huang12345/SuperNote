package com.asus.supernote.doodle.drawinfo;

import java.util.LinkedList;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem.SerDrawInfo;
import com.asus.supernote.doodle.DoodleItem.SerEraseInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo.Point;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawtool.EraseDrawTool;


public class EraseDrawInfo extends DrawInfo {
	
	public class TwoPressPointPathInfo{
		private LinkedList<Path> mPathList = new LinkedList<Path>();
		private Point mPointBegin;
		private Point mPointEnd;
		private float mDefaultWidth;
		
		public TwoPressPointPathInfo(Point begin,Point end,float defaultWidth)
		{
			mPointBegin = new Point(begin);
			mPointEnd = new Point(end);
			mDefaultWidth = defaultWidth;
			
//			//BEGIN: RICHARD//Current version does not take pressure
				Path path = new Path();
				path.moveTo(mPointBegin.x, mPointBegin.y);
				path.lineTo(mPointEnd.x, mPointEnd.y);
				mPathList.add(path);
			//END: RICHARD
		}
		
		public TwoPressPointPathInfo(TwoPressPointPathInfo info)
		{
			mPointBegin = new Point(info.getPointBegin());
			mPointEnd = new Point(info.getPointEnd());
			mDefaultWidth = info.getDefaultWidth();
			
			for(Path path : info.getPathList())
			{
				mPathList.add(new Path(path));
			}
		}
		
		public Point getPointBegin()
		{
			return mPointBegin;
		}
		
		public Point getPointEnd()
		{
			return mPointEnd;
		}
		
		public float getDefaultWidth()
		{
			return mDefaultWidth;
		}
		
		public LinkedList<Path> getPathList()
		{
			return mPathList;
		}
		
	    private void mapPoints(Matrix matrix) {
	        float[] pts = new float[4];

            pts[0] = mPointBegin.x;
            pts[1] = mPointBegin.y;
            pts[2] = mPointEnd.x;
            pts[3] = mPointEnd.y;

            matrix.mapPoints(pts);

            mPointBegin.x = pts[0];
            mPointBegin.y = pts[1];
            mPointEnd.x = pts[2];
            mPointEnd.y = pts[3];
	    }
	    
	    void transformLock(Matrix matrix) {
	        for (Path path : mPathList) {
	            path.transform(matrix);
	        }

	        mapPoints(matrix);	     
	    }
		
	    private float getWidth(Point pt,float defaultWidth)
	    {
			float press = pt.pressure;
			if(press < 0.2)
			{
				press = 0.2f;
			}else if(press > 0.8)
			{
				press = 0.8f;
			}
			
			return  (float)(defaultWidth*(press*EraseDrawInfo.STORKE_WIDTH_PRESS_RATIO - 0.5));
	    }
	    
	    private double[] getxyDistance(double xDistance,double yDistance,double orignalWidth,double distance)
	    {
	    	double [] xyDistance= new double[2];
	    	if(yDistance == 0)
	    	{
	    		xyDistance[1] = orignalWidth;
	    		xyDistance[0] = 0;
	    	}else if(xDistance == 0)
	    	{
	    		xyDistance[0]  = orignalWidth;
	    		xyDistance[1] = 0;
	    	}
	    	else
	    	{
	    		xyDistance[0] = orignalWidth * (yDistance/distance);
	    		xyDistance[1] = orignalWidth * (xDistance/distance);
	    	}
	    	
	    	return xyDistance;
	    }
	    
	    public void clear()
	    {
	    	mPathList.clear();
	    }
	}
	
    private int mId;
    public int mReferenceCount = 0;
    private boolean mIsVisible = true;
    private Path mFullPath = new Path();
    public static final float STORKE_WIDTH_PRESS_RATIO = 5f;
    private static final float DEFAULT_PRESSER = 0.3f;
    private float mPaintWidth;
	private LinkedList<TwoPressPointPathInfo> mTwoPressPointPathInfoList = new LinkedList<TwoPressPointPathInfo>();
	private Point mPointLast;
	private Boolean mIsHaveOtherPresser = false;
	private Boolean mIsNeedComputDirty = false;

    public EraseDrawInfo(DrawTool drawTool, Paint usedPaint) {
        super(drawTool, usedPaint);
        mPaintWidth = usedPaint.getStrokeWidth();
        mId = hashCode();
    }

    public EraseDrawInfo(DrawTool drawTool, Paint usedPaint, short[] points,Boolean isHavePress) {
        this(drawTool, usedPaint);
        initLists(points,isHavePress);
    }

    public void addPointList(short[] points,Boolean isHavePress)
    {
        int length = points.length;
        int singlePointLength = 2;
        int index = 0;
        
        if(isHavePress)
        {
        	singlePointLength++;
        }
        
        //BEGIN: RICHARD
        mIsHaveOtherPresser = false;
//        //END: RICHARD
        mIsNeedComputDirty = false;
        while (index <= (length - singlePointLength*2)) {
            Point startPoint ; 
            Point endPoint ; 
            if(!isHavePress)
            {
            	startPoint = new Point(points[index++], points[index++],DEFAULT_PRESSER);
            	endPoint = new Point(points[index++], points[index++],DEFAULT_PRESSER);
            }
            else
            {
            	startPoint = new Point(points[index++], points[index++],points[index++]/MetaData.PRESSURE_FACTOR);
            	endPoint = new Point(points[index++], points[index++],points[index++]/MetaData.PRESSURE_FACTOR);
            }
    		TwoPressPointPathInfo temp = new TwoPressPointPathInfo(startPoint,endPoint,mPaintWidth);
    		this.addTwoPressPointPathInfo(temp);
        }
        mIsNeedComputDirty = true;
    }
    
    @Override
    public boolean add(EraseDrawInfo eraseInfo, boolean isSubset) {
        return false;
    }

    public Boolean getIsHaveOtherPresser() {
		return mIsHaveOtherPresser;
	}
    
    public float getPaintWidth() {
		return mPaintWidth;
	}
    
    public void clearData() {
    	//RICHARD
    	for(TwoPressPointPathInfo info :mTwoPressPointPathInfoList)
    	{
    		info.clear();
    	}
    	mTwoPressPointPathInfoList.clear();
        mFullPath.reset();
    }

    public void addTwoPressPointPathInfo(TwoPressPointPathInfo info)
    {
    	mTwoPressPointPathInfoList.add(info);
    	for(Path path :info.getPathList())
    	{
    		mFullPath.addPath(path);
    	}
    	if(mIsNeedComputDirty)
    	{
    		computeDirty(info);
    	}
    }
    
    @Override
    public DrawInfo cloneLock() {
        EraseDrawInfo drawInfo = (EraseDrawInfo) (new EraseDrawTool()).getDrawInfo(mPaint);
        
        drawInfo.mId = mId;
        drawInfo.mIsVisible = mIsVisible;
        drawInfo.mFullPath = new Path(mFullPath);
        for(TwoPressPointPathInfo info : mTwoPressPointPathInfoList)
        {
        	TwoPressPointPathInfo newInfo = new TwoPressPointPathInfo(info.getPointBegin(),info.getPointEnd(),info.getDefaultWidth());
        	drawInfo.addTwoPressPointPathInfo(newInfo);
        }

        return drawInfo;

    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EraseDrawInfo) {
            return mId == ((EraseDrawInfo) o).getId();
        }
        return false;
    }

    @Override
    public RectF getBounds() {
        RectF bounds = addStrokeToBounds(getStrictBounds());
        return bounds;
    }

    public int getId() {
        return mId;
    }

    public Path getPath() {
//    	mFullPath.setFillType(FillType.WINDING);
        return mFullPath;
    }

    public LinkedList<Point> getPointList() {
        LinkedList<Point> list = new LinkedList<Point>();
        for(TwoPressPointPathInfo info : mTwoPressPointPathInfoList)
        {
        	list.add(info.getPointBegin());
        	list.add(info.getPointEnd());
        }
        return list;
    }
    
    public LinkedList<TwoPressPointPathInfo> getTwoPressPointPathInfoList()
    {
    	return mTwoPressPointPathInfoList;
    }

    @Override
    public RectF getStrictBounds() {
        RectF totalBounds = new RectF();

        mFullPath.computeBounds(totalBounds, true);

        return totalBounds;
    }

    // Generate a subset who has the same id
    // And we can add/delete it together
    public EraseDrawInfo getSubset(RectF bounds) {
        boolean hasSubset = false;
        RectF testBounds = new RectF();
        
        mFullPath.computeBounds(testBounds, true);
        if (!RectF.intersects(bounds, testBounds) && !RectF.intersects(testBounds, bounds)
                && !bounds.contains(testBounds) && !testBounds.contains(bounds)) {
            return null;
        }
        
        Paint paint = new Paint(mPaint);
        // We scale the paint stroke width because we will draw the object to a bitmap first,
        // and then draw that bitmap to the cache bitmap.
        // Such drawing will make paint stroke width look thinner.
        EraseDrawInfo subset = new EraseDrawInfo(mDrawTool, paint);

        mIsNeedComputDirty = false;
        subset.setId(mId);
        for(TwoPressPointPathInfo info : mTwoPressPointPathInfoList)
        {
        	for (Path path : info.getPathList()) 
        	{
        		path.computeBounds(testBounds, true);
				
        		if (RectF.intersects(bounds, testBounds)) 
        		{	
        			TwoPressPointPathInfo newInfo = new TwoPressPointPathInfo(info);
        			subset.addTwoPressPointPathInfo(newInfo);
        			hasSubset = true;
        			break;
        		}
        	}          	  	
    	}
        mIsNeedComputDirty = true;
        if (hasSubset) 
        {
          return subset;
        }
        else 
        {
        	return null;
        }
    }

    public boolean isVisible() {
        return mIsVisible;
    }

    private void initLists(short[] points,Boolean isHavePress) {
        mFullPath.reset();  
        addPointList(points,isHavePress);
    }

    // Save each fragment of path to apply to selection mode
    @Override
    public void onDown(float[] x, float[] y) {
    }

    @Override
    public void onMove(MotionEvent event, float[] x, float[] y, boolean multiPoint) {

    }

    @Override
    public void onUp(float[] x, float[] y) {

    }

    @Override
    public SerDrawInfo saveLock(NotePage note) {
        SerEraseInfo serInfo = new SerEraseInfo(mId, mIsVisible);
        serInfo.setStrokeWidth(mPaint.getStrokeWidth());
        serInfo.setPaintTool(mDrawTool.getToolCode());
        serInfo.setPoints(getPointList(),true);
        return serInfo;
    }

    public void setId(int id) {
        mId = id;
    }

    public void setVisible(boolean isVisible) {
        mIsVisible = isVisible;
    }

    @Override
    protected void transformLock(Matrix matrix) {
    	for(TwoPressPointPathInfo temp : mTwoPressPointPathInfoList)
    	{
    		temp.transformLock(matrix);
    	}
    	mFullPath.transform(matrix);
    }
    

    private void addPoints(float[] x, float[] y,float[] pressure)
    {
    	Point ptCurrent = new Point(x[0],y[0],pressure[0]);
		if(mPointLast == null)
    	{
    		mPointLast = ptCurrent;
    		return;
    	}else
    	{   		
    		TwoPressPointPathInfo temp = new TwoPressPointPathInfo(mPointLast,ptCurrent,mPaintWidth);
    		addTwoPressPointPathInfo(temp);
    		mPointLast = ptCurrent;
    	}
    	    	
    }
    
	public void onMove(float[] x, float[] y, boolean multiPoint, int tooltype,
			float[] pressure) {
		if (tooltype != MotionEvent.TOOL_TYPE_STYLUS) {
			pressure[0] = DEFAULT_PRESSER;
		}
		addPoints(x, y,pressure);		
	}

	public void onDown(float[] x, float[] y, int tooltype,float[] pressure) {
		if (tooltype != MotionEvent.TOOL_TYPE_STYLUS) {
			pressure[0] = DEFAULT_PRESSER;
		}
		addPoints(x, y,pressure);		
	}

	public void onUp(float[] x, float[] y, int tooltype, float[] pressure) {
		if (tooltype != MotionEvent.TOOL_TYPE_STYLUS) {
			pressure[0] = DEFAULT_PRESSER;
		}
		addPoints(x, y,pressure);	
		
	}
	
    protected void computeDirty(TwoPressPointPathInfo info) {
    	
    	RectF testBounds = new RectF();
    	Rect dirtyRect = new Rect();
    	for(Path path : info.getPathList())
    	{
    		path.computeBounds(testBounds, true);
    		if(mIsHaveOtherPresser)
    		{
    			dirtyRect.union((int)testBounds.left - 1 ,(int)testBounds.top - 1,(int)testBounds.right + 1,(int)testBounds.bottom + 1);
    		}
    		else
    		{
    			dirtyRect.union((int)(testBounds.left - mPaintWidth) ,(int)(testBounds.top - mPaintWidth),(int)(testBounds.right + mPaintWidth),(int)(testBounds.bottom + mPaintWidth));
    		}
    	}
        // Compute Dirty Rectangle
    	mDirtyRect = dirtyRect;
    }
    
    public TwoPressPointPathInfo getLastTwoPressPointPathInfo()
    {
    	try{
    		return mTwoPressPointPathInfoList.getLast();
    	}catch(Exception e)
    	{
    		return null;
    	}
    }
}
