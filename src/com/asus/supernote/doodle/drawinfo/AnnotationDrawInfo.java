package com.asus.supernote.doodle.drawinfo;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem.SerAnnotationInfo;
import com.asus.supernote.doodle.DoodleItem.SerDrawInfo;
import com.asus.supernote.doodle.drawtool.AnnotationDrawTool;
import com.asus.supernote.doodle.drawtool.DrawTool;

public class AnnotationDrawInfo extends DrawInfo {
    private LinkedHashMap<Integer, DrawInfo> mDrawInfos = new LinkedHashMap<Integer, DrawInfo>();

    public AnnotationDrawInfo(DrawTool drawTool, Paint usedPaint) {
        super(drawTool, usedPaint);
    }

    @Override
    public boolean add(EraseDrawInfo eraseInfo, boolean isSubset) {
        boolean success = false;

        Collection<DrawInfo> drawInfos = mDrawInfos.values();
        for (DrawInfo drawInfo : drawInfos) {
        	if (drawInfo instanceof  GraphicDrawInfo || drawInfo instanceof TextImgDrawInfo ) continue;//by show
            if (drawInfo.add(eraseInfo, isSubset)) {
                success = true;
            }
        }
        return success;
    }

    private int minDrawInfoKey = 0;
    
    private int maxDrawInfoKey = 0;
    public void add(DrawInfo drawInfo, boolean isDrawForward) {
    	int tmpKey;
    	if(!isDrawForward) {
    		minDrawInfoKey--;
    		tmpKey = minDrawInfoKey;
    	}else {
    		maxDrawInfoKey++;
    		tmpKey = maxDrawInfoKey;
    	}
    	
        if (drawInfo != null) {
        	if(!(drawInfo instanceof AnnotationDrawInfo))
        	{
                mDrawInfos.put(tmpKey, drawInfo);
        	}
        	else
        	{
        		AnnotationDrawInfo tempDrawInfo = (AnnotationDrawInfo)drawInfo;
        		add(tmpKey, tempDrawInfo.getDrawInfoMap(), !isDrawForward);
        	}
        		

        }
    }

    public void add(int location, LinkedHashMap<Integer, DrawInfo> collection, boolean isDrawAfterward) {
        if (collection != null) {
            Set<Integer> keys = new TreeSet<Integer>(collection.keySet());

            if(isDrawAfterward) {
            	location = location - keys.size() + 1;
            	minDrawInfoKey = location;
            }else {
            	maxDrawInfoKey = location + keys.size() - 1;
            }
            for (Integer key : keys) {
                mDrawInfos.put(location++, collection.get(key));
      
            }
        }
    }

    @Override
    public DrawInfo cloneLock() {
        AnnotationDrawTool tool = new AnnotationDrawTool();
        AnnotationDrawInfo drawInfo = (AnnotationDrawInfo) tool.getDrawInfo(mPaint);
        Set<Integer> keys = mDrawInfos.keySet();
        for (Integer key : keys) {
            drawInfo.mDrawInfos.put(key, mDrawInfos.get(key).clone());
        }
        return drawInfo;
    }

    @Override
    public RectF getBounds() {
        return getStrictBounds();
    }

    public LinkedHashMap<Integer, DrawInfo> getDrawInfoMap() {
        return mDrawInfos;
    }

    public Collection<DrawInfo> getDrawInfos() {
		List<DrawInfo> aList = new LinkedList<DrawInfo>();
		Set<Integer> keys = new TreeSet<Integer>(mDrawInfos.keySet());
		
    	for(int key : keys) {
    		aList.add(0, mDrawInfos.get(key));
    	}
		
		return aList;
    }

    @Override
    public RectF getStrictBounds() {
        RectF unionBounds = new RectF();
        Collection<DrawInfo> collection = mDrawInfos.values();
        for (DrawInfo drawInfo : collection) {
            unionBounds.union(drawInfo.getBounds());
        }
        return unionBounds;
    }

    // BEGIN: Shane_Wang@asus.com 2013-1-18
    private HashMap<String, DrawInfo> fileToInfoMap = new HashMap<String, DrawInfo>();
    public HashMap<String, DrawInfo> getFileToInfoMap() {
    	return fileToInfoMap;
    }
    // END: Shane_Wang@asus.com 2013-1-18
    public LinkedList<String> getUsingFiles() {
        LinkedList<String> usingFiles = new LinkedList<String>();
        Collection<DrawInfo> drawInfos = mDrawInfos.values();

        for (DrawInfo drawInfo : drawInfos) {
            if (drawInfo instanceof AnnotationDrawInfo) {
                usingFiles.addAll(((AnnotationDrawInfo) drawInfo).getUsingFiles());
            }
            else if (drawInfo instanceof GraphicDrawInfo) {
                String fileName = ((GraphicDrawInfo) drawInfo).getFileName();
                if (fileName != null) {
                    usingFiles.add(fileName);
                    fileToInfoMap.put(fileName, drawInfo);
                }
            }
            else if (drawInfo instanceof TextImgDrawInfo) {
                String fileName = ((TextImgDrawInfo) drawInfo).getFileName();
                if (fileName != null) {
                    usingFiles.add(fileName);
                    fileToInfoMap.put(fileName, drawInfo);
                }
            }
        }
        return usingFiles;
    }

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
    protected SerDrawInfo saveLock(NotePage note) {
        return new SerAnnotationInfo(mDrawInfos, note);
    }

    public int size() {
        return mDrawInfos.size();
    }

    @Override
    protected void transformLock(Matrix matrix) {
        Collection<DrawInfo> collection = mDrawInfos.values();
        for (DrawInfo drawInfo : collection) {
            drawInfo.transform(matrix);
        }
    }

    public LinkedList<DrawInfo> unGroup() {
        LinkedList<DrawInfo> infos = new LinkedList<DrawInfo>();
        TreeSet<Integer> ts = new TreeSet<Integer>(new Comparator(){
   		 public int compare(Object o1,Object o2){
   		 Integer i1 = (Integer)o1;
   		 Integer i2 = (Integer)o2;
   		 return i2.intValue() - i1.intValue();
   		 }
   		 });
       	ts.addAll(mDrawInfos.keySet());
   	
        Set<Integer> keys = ts;
           
        for (Integer key : keys) {
            DrawInfo drawInfo = mDrawInfos.get(key);
            if (drawInfo instanceof AnnotationDrawInfo) {
                infos.addAll(((AnnotationDrawInfo) drawInfo).unGroup());
            }
            else {
                infos.add(drawInfo);
            }
        }
        return infos;
    }
}
