package com.asus.supernote.doodle.drawtool;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeSet;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.asus.supernote.doodle.drawinfo.AnnotationDrawInfo;
import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.GraphicDrawInfo;
import com.asus.supernote.doodle.drawinfo.TextImgDrawInfo;

public class AnnotationDrawTool extends DrawTool {
    public static final int ANNOTATION_TOOL = DrawTool.ANNOTATION_TOOL;

    public AnnotationDrawTool() {
        super(ANNOTATION_TOOL);
    }

    @Override
    protected void doDraw(Canvas canvas, DrawInfo drawInfo) {
    }

    @Override
    public void draw(Canvas canvas, boolean isUseTempBitmap, DrawInfo drawInfo) {//RICHARD
        AnnotationDrawInfo annotationDrawInfo;
        LinkedHashMap<Integer, DrawInfo> drawInfos;
        TreeSet<Integer> keys;
        Iterator<Integer> iterator;

        if ((drawInfo == null) || !(drawInfo instanceof AnnotationDrawInfo)) {
            return;
        }
        annotationDrawInfo = (AnnotationDrawInfo) drawInfo;
        annotationDrawInfo.getDrawInfoMap();
        drawInfos = annotationDrawInfo.getDrawInfoMap();
        keys = new TreeSet<Integer>(drawInfos.keySet());
        iterator = keys.descendingIterator();
        LinkedList<DrawInfo> DrawInfos = new LinkedList<DrawInfo>();//by show
        
        while (iterator.hasNext()) {
            int key = iterator.next();
            DrawInfo info = drawInfos.get(key);
            DrawInfos.add(info);//by show
            if ((info instanceof GraphicDrawInfo) || (info instanceof TextImgDrawInfo)) {//by show
            	info.getDrawTool().draw(canvas, isUseTempBitmap, info);//RICHARD
            }
        }
        
        //Begin: show_wang@asus.com
        //Modified reason: for eraser
        for (DrawInfo info : DrawInfos) {
        	if (!(info instanceof GraphicDrawInfo) && !(info instanceof TextImgDrawInfo)) {
            	info.getDrawTool().draw(canvas, true, info);
            }
        }
        //End: show_wang@asus.com
    }

    @Override
    public DrawInfo getDrawInfo(Paint usedPaint) {
        return new AnnotationDrawInfo(this, usedPaint);
    }

}
