package com.asus.supernote.doodle.drawutil;

import com.asus.supernote.doodle.drawinfo.PathDrawInfo.Point;

public class HWPoint {
	public float x;
	public float y;
	public long timestamp;  //timestamp
	public float pressure;
	public float width;  //render width
	public int alpha;

    public HWPoint(){
    }

    public HWPoint(float x, float y){
        this.x = x;
        this.y = y;
    }
    
    public HWPoint(float x, float y, long t){
        this.x = x;
        this.y = y;
        this.timestamp = t;
    }

    public void Set(float x, float y, float w){
    	this.x = x;
    	this.y = y;
    	this.width = w;
    }

    public void Set(HWPoint point){
    	this.x = point.x;
    	this.y = point.y;
        this.width = point.width;
    }
    
    public void Set(float x, float y){
    	this.x = x;
    	this.y = y;
    }

    public String toString(){
        String str = "X = " + x + "; Y = " + y + "; W = " + width;
        return str;
    }
    
    public Point ToPoint() {
    	return new Point(x,y);
    }
}
