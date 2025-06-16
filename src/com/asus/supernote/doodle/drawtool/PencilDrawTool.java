package com.asus.supernote.doodle.drawtool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.PencilDrawInfo;

public class PencilDrawTool extends PathDrawTool {
	Bitmap mTexture = null;
	
	public PencilDrawTool(){
		super(DrawTool.SKETCH_TOOL);
	}
	
	public void SetTexture(Bitmap texture){
		mTexture = texture;
	}
	
	@Override
    protected void doDraw(Canvas canvas, DrawInfo drawInfo) {
		if (drawInfo == null) {
            return;
        }
		
		PencilDrawInfo pencilDrawInfo = (PencilDrawInfo)drawInfo;
		canvas.drawPath(pencilDrawInfo.getPath(), pencilDrawInfo.GetTexturePaint());
	}
	
	@Override
    public DrawInfo getDrawInfo(Paint usedPaint) {
		PencilDrawInfo pencilInfo= new PencilDrawInfo(this, usedPaint);
		pencilInfo.SetTexture(mTexture);
		return pencilInfo;
	}
	
}
