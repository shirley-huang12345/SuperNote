/*
 * Description: parse shape from shape.xml
 * Author: mars_li@asus.com
 */

package com.asus.supernote;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;
import android.util.TimingLogger;

public class ShapeManager {
	public static String Tag = "ParserPath";
	private static String Tag_Drawing = "GeometryDrawing";
	private static String Attr_Geometry = "Geometry";
	private static String Tag_Shape = "Shape";
	private static String Attr_ID = "id";
	private static ShapeManager sInstance;
	
	
	ArrayList<Shape> mShapes = new ArrayList<Shape>();
	public ShapeManager( XmlPullParser parser){
		TimingLogger timings = new TimingLogger("TopicLogTag",
				"initPath");
		 initPath(parser);
		 timings.dumpToLog();
	}
	
	public static ShapeManager getInstance(){
		if(sInstance == null){
			XmlPullParser xpp = SuperNoteApplication.getContext().getResources().getXml(R.xml.shapes); 
			sInstance = new ShapeManager(xpp);
		}
		return sInstance;
	}
	
	public int  getCount(){
		return mShapes.size();
	}
	
	public Shape GetShapeById(int id){
		Shape rval= null;
		for(Shape shape:mShapes){
			if(shape.mId == id){
				rval = new Shape();
				rval.mId = shape.mId;
				rval.mPath.addPath(shape.mPath);
				break;
			}
		}
		return rval;
	}
	
	@SuppressLint("UseValueOf")
	private void  initPath( XmlPullParser parser){
		String tagName;
		Shape curShape = null;
		try{
			int eventType = parser.getEventType();
			while(eventType != XmlPullParser.END_DOCUMENT){
				switch(eventType){
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if(Tag_Shape.equals(tagName)){
						curShape = new Shape();
						curShape.mId = Integer.valueOf(parser.getAttributeValue(null, Attr_ID));
					}
					else if(Tag_Drawing.equals(tagName)){
						String geometry = parser.getAttributeValue(null, Attr_Geometry);
						Path path = getPathFromString(geometry);
						if(path != null){
							curShape.mPath.addPath(path);
						}
					}
					break;
				case XmlPullParser.END_TAG:
					if(Tag_Shape.equals(parser.getName())){
						mShapes.add(curShape);
					}
					break;
				}
				eventType = parser.next();
			}
		}
		catch(XmlPullParserException e){
			Log.w(this.Tag, e.getMessage());
		}
		catch(IOException e){
			Log.w(this.Tag, e.getMessage());
		}
	}
	
	public static Path getPathFromString(String pathData){
		Path path = new Path();
		int index = 0;
		try{
			while(index<pathData.length()){
				char c = pathData.charAt(index);
				switch(c){
				case 'M':
					index = moveTo(path, pathData, ++index);
					break;
				case 'L':
					index = lineTo(path, pathData, ++index);
					break;
				case 'C':
					index = cubicTo(path, pathData, ++index);
					break;
				case 'Z':
					index = close(path, pathData, ++index);
				default:
					index++;
				}
			}
		}
		catch(Exception e){
			path = null;
		}
		
		return path;
	}
	
	private static int cubicTo(Path path, String s, int index){
		PointF c1 = new PointF();
		index = parsePointF(c1, s, index);
		PointF c2 = new PointF();
		index = parsePointF(c2, s, index);
		PointF c3 = new PointF();
		index = parsePointF(c3, s, index);
		path.cubicTo(c1.x, c1.y, c2.x, c2.y, c3.x, c3.y);
		return index;
	}
	
	private static int lineTo(Path path, String s, int index ){
		PointF p = new PointF();
		index = parsePointF(p, s, index);
		path.lineTo(p.x, p.y);
		return index;
	}
	
	private static int moveTo(Path path, String s, int index){
		PointF p = new PointF();
		index = parsePointF(p, s, index);
		path.moveTo(p.x, p.y);
		return index;
	}
	
	private static int close(Path path, String s ,int index){
		path.close();
		return index;
	}
	
	private static int parsePointF(PointF p, String pathData, int index){
		int first = index;
		int last = index;
		//remove space char
		while(pathData.charAt(first) ==' '){
			first++;
		}
		last = first;
		
		while(pathData.charAt(last)<='9'&&pathData.charAt(last)>='0'||
				pathData.charAt(last) == '.'|| 
				pathData.charAt(last)=='-'||
				pathData.charAt(last)=='e'){
			last++;
		}
		
		String x = pathData.substring(first, last);
		p.x = Float.parseFloat(x); //get x 
		
		
		first = last+1;
		last = first;
		while(pathData.charAt(first) ==' '){
			first++;
		}
		last = first;
		
		//get second point must consider out of bound
		while((last<pathData.length())&&
				(pathData.charAt(last)<='9'&&pathData.charAt(last)>='0'||
				pathData.charAt(last) == '.'|| 
				pathData.charAt(last)=='-'||
				pathData.charAt(last)=='e')){
			last++;	
		}
		String y = pathData.substring(first, last);
		p.y = Float.parseFloat(y);
		return last;
	}
	
	public class Shape{
		public int mId;
		public Path mPath;
		public Shape(int id, Path path){
			mId = id;
			mPath = path;
		}
		public Shape(){
			mPath = new Path();
		}
	}
}
