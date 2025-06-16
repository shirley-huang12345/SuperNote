package com.asus.supernote.doodle.drawtool;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo;

public class PathDrawTool extends DrawTool {

    public static final int NORMAL_TOOL = DrawTool.NORMAL_TOOL;

    public PathDrawTool(int toolCode) {
        super(toolCode);
    }

    @Override
    protected void doDraw(Canvas canvas, DrawInfo drawInfo) {
        PathDrawInfo pathDrawInfo;

        if (drawInfo == null) {
            return;
        }

        pathDrawInfo = (PathDrawInfo) drawInfo;
        canvas.drawPath(pathDrawInfo.getPath(), pathDrawInfo.getPaint());
    }

    @Override
    public DrawInfo getDrawInfo(Paint usedPaint) {
        return new PathDrawInfo(this, usedPaint);
    }

}
