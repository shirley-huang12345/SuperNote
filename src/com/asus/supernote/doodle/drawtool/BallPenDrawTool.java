package com.asus.supernote.doodle.drawtool;

import android.graphics.Paint;

import com.asus.supernote.doodle.drawinfo.BallPenDrawInfo;
import com.asus.supernote.doodle.drawinfo.DrawInfo;

public class BallPenDrawTool  extends SpotDrawTool{

	public BallPenDrawTool(int toolCode) {
		super(toolCode);
	}	

	@Override
	public DrawInfo getDrawInfo(Paint usedPaint) {
		return new BallPenDrawInfo(this, usedPaint);
	}	

}
