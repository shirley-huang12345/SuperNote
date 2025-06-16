package com.asus.supernote.inksearch;

import java.text.NumberFormat;
import java.util.ArrayList;
import com.visionobjects.myscript.engine.Engine;
import com.visionobjects.myscript.engine.EngineObject;
import com.visionobjects.myscript.shape.ShapeBeautifier;
import com.visionobjects.myscript.shape.ShapeCandidate;
import com.visionobjects.myscript.shape.ShapeDecoratedEllipticArc;
import com.visionobjects.myscript.shape.ShapeDecoratedEllipticArcData;
import com.visionobjects.myscript.shape.ShapeDecoratedLine;
import com.visionobjects.myscript.shape.ShapeDecoratedLineData;
import com.visionobjects.myscript.shape.ShapeDocument;
import com.visionobjects.myscript.shape.ShapeEllipticArc;
import com.visionobjects.myscript.shape.ShapeEllipticArcData;
import com.visionobjects.myscript.shape.ShapeErased;
import com.visionobjects.myscript.shape.ShapeInkRange;
import com.visionobjects.myscript.shape.ShapeKnowledge;
import com.visionobjects.myscript.shape.ShapeLine;
import com.visionobjects.myscript.shape.ShapeLineData;
import com.visionobjects.myscript.shape.ShapeModel;
import com.visionobjects.myscript.shape.ShapePointData;
import com.visionobjects.myscript.shape.ShapePrimitive;
import com.visionobjects.myscript.shape.ShapeRecognized;
import com.visionobjects.myscript.shape.ShapeRecognizer;
import com.visionobjects.myscript.shape.ShapeRejected;
import com.visionobjects.myscript.shape.ShapeScratchOut;
import com.visionobjects.myscript.shape.ShapeSegment;

public class AsusShape {
	private Engine mEngine = null;
	private ShapeRecognizer mShapeRecognizer = null;
	private ShapeBeautifier mShapeBeautifier = null;//ShapeBeautifier.create(engine);
	private ShapeDocument mShapeDocument = null;

	
	public void initShapeRecognizer()
	{
		mEngine = Engine.create(Cert.getBytes());
		mShapeRecognizer = ShapeRecognizer.create(mEngine);
		final ShapeKnowledge shapeKnowledge = (ShapeKnowledge)EngineObject.load(mEngine, CFG.PATH_TO_ASSETS + CFG.SHAPE_KNOWLEDGE_RES);
	    
	    System.out.println(" . ShapeKnowledge resource loaded successfully");
	    mShapeRecognizer.attach(shapeKnowledge);
	    
	    mShapeBeautifier = ShapeBeautifier.create(mEngine);
	    mShapeBeautifier.attach(shapeKnowledge);
	    
	    shapeKnowledge.dispose();
	}
	
	public void deinitShapeRecognizer()
	{
		mShapeBeautifier.dispose();
		mShapeRecognizer.dispose();
		mEngine.dispose();
	}
	
	public void prepareShapeDocument()
	{
		mShapeDocument = ShapeDocument.create(mEngine);
	}
	
	public void addStroke(float[] xy)
	{
		if(xy.length >= 2)
		{
			mShapeDocument.addStroke(xy, 0, 2, xy, 1, 2, xy.length / 2);
		}
	}
	
	public void addStroke(float[] x,float[] y)
	{
		//in example IncrementalShapeRecognitionBeautification.
		//not use startSegment and endSegment.
		mShapeDocument.addStroke(x,y);
	}
	
	public void clearStrokes()
	{
		mShapeDocument.clear();
	}
	
	public void setShapeDocument(ShapeDocument shapeDocument)
	{
		mShapeDocument.dispose();
		try
		{
			mShapeDocument = (ShapeDocument)shapeDocument.clone();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public ShapeDocument getShapeDocument()
	{
		return mShapeDocument;
	}
	
	public ShapeDocument getResultShapeDocument()
	{
		try {
			System.out.println("getResultShapeDocument start");
			mShapeRecognizer.process(mShapeDocument);
			mShapeBeautifier.process(mShapeDocument);
			System.out.println("getResultShapeDocument end");
			return mShapeDocument;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	//return shape list, group by Integer
	public ArrayList<Object> getShapeAndStrok(ShapeDocument shapedocument)
	{
    	System.out.println("getShapeList start");
    	final int segmentCount = shapedocument.getSegmentCount();
    	ArrayList<Object> shapeList = new ArrayList<Object>();
    	
		final NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		
		for (int i = 0; i < segmentCount; ++i) {
			final ShapeSegment segment = shapedocument.getSegmentAt(i);
			final int candidateCount = segment.getCandidateCount();
			
			if (candidateCount != 0) {
				final ShapeCandidate candidate = segment.getCandidateAt(0);

				if (candidate instanceof ShapeRecognized) {
					final ShapeModel model = ((ShapeRecognized) candidate)
							.getModel();

					final float rs = ((ShapeRecognized) candidate)
							.getResemblanceScore();
					final float nrs = ((ShapeRecognized) candidate)
							.getNormalizedRecognitionScore();

					nf.setMinimumFractionDigits(4);
					nf.setMaximumFractionDigits(4);
					
					final int primitiveCount = ((ShapeRecognized) candidate)
							.getPrimitiveCount();

					nf.setMinimumFractionDigits(2);
					nf.setMaximumFractionDigits(2);
					
					for (int j = 0; j < primitiveCount; ++j) {
						if(j==0)
						{
							Integer groupPrimitiveCount = primitiveCount;
							shapeList.add(groupPrimitiveCount);
						}
						
						ShapePrimitive primitive = null;
						try {
							primitive = ((ShapeRecognized) candidate)
							        .getPrimitiveAt(j);
						} catch (com.visionobjects.myscript.engine.LimitExceededException ex) {
							model.dispose();
							candidate.dispose();
							segment.dispose();
							return shapeList;
						}

						if (primitive instanceof ShapeLine) {
							final ShapeLineData data = ((ShapeLine) primitive)
									.getData();
							shapeList.add(data);
				        	
						} else if (primitive instanceof ShapeEllipticArc) {
							final ShapeEllipticArcData data = ((ShapeEllipticArc) primitive)
									.getData();
							shapeList.add(data);
							
						} else if (primitive instanceof ShapeDecoratedLine) {
							final ShapeDecoratedLineData data = ((ShapeDecoratedLine) primitive)
									.getData();
							shapeList.add(data);
						}
						else if(primitive instanceof ShapeDecoratedEllipticArc)
						{
							final ShapeDecoratedEllipticArcData data = ((ShapeDecoratedEllipticArc) primitive)
									.getData();
							shapeList.add(data);
						}

						primitive.dispose();
					}

					model.dispose();
				}        
				else if (candidate instanceof ShapeScratchOut)
		        {
		          System.out.println("   . segment " + i + ", scratch out");
		        }
		        else if (candidate instanceof ShapeErased)
		        {
		          System.out.println("   . segment " + i + ", erased");
		        }
		        else if(candidate instanceof ShapeRejected)
		        {
		        	System.out.println("   . segment " + i + ", Shape Rejected");
		        }

				candidate.dispose();
			}

			segment.dispose();
		}
		System.out.println("getShapeList end");
		return shapeList;
	}
	
	
    public ArrayList<Object> getShapeList(ShapeDocument shapedocument)
    {
    	System.out.println("getShapeList start");
    	final int segmentCount = shapedocument.getSegmentCount();
    	ArrayList<Object> shapeList = new ArrayList<Object>();
    	
		final NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		
		for (int i = 0; i < segmentCount; ++i) {
			final ShapeSegment segment = shapedocument.getSegmentAt(i);
			final int candidateCount = segment.getCandidateCount();

			int count = segment.getInkRangeCount();
			if(count > 0)
			{
				ShapeInkRange sir = segment.getInkRangeAt(0);
			}
			
			if (candidateCount != 0) {
				final ShapeCandidate candidate = segment.getCandidateAt(0);

				if (candidate instanceof ShapeRecognized) {
					final ShapeModel model = ((ShapeRecognized) candidate)
							.getModel();

					nf.setMinimumFractionDigits(4);
					nf.setMaximumFractionDigits(4);

					final int primitiveCount = ((ShapeRecognized) candidate)
							.getPrimitiveCount();

					nf.setMinimumFractionDigits(2);
					nf.setMaximumFractionDigits(2);
					for (int j = 0; j < primitiveCount; ++j) {
						final ShapePrimitive primitive = ((ShapeRecognized) candidate)
								.getPrimitiveAt(j);

						if (primitive instanceof ShapeLine) {
							final ShapeLineData data = ((ShapeLine) primitive)
									.getData();
							shapeList.add(data);
				        	
						} else if (primitive instanceof ShapeEllipticArc) {
							final ShapeEllipticArcData data = ((ShapeEllipticArc) primitive)
									.getData();
							shapeList.add(data);
							
						} else if (primitive instanceof ShapeDecoratedLine) {
							final ShapeDecoratedLineData data = ((ShapeDecoratedLine) primitive)
									.getData();
							shapeList.add(data);
						}
						else if(primitive instanceof ShapeDecoratedEllipticArc)
						{
							final ShapeDecoratedEllipticArcData data = ((ShapeDecoratedEllipticArc) primitive)
									.getData();
							shapeList.add(data);
						}

						primitive.dispose();
					}

					model.dispose();
				}        
				else if (candidate instanceof ShapeScratchOut)
		        {
		          System.out.println("   . segment " + i + ", scratch out");
		        }
		        else if (candidate instanceof ShapeErased)
		        {
		          System.out.println("   . segment " + i + ", erased");
		        }
		        else if(candidate instanceof ShapeRejected)
		        {
		        	System.out.println("   . segment " + i + ", Shape Rejected");
		        }

				candidate.dispose();
			}

			segment.dispose();
		}
		System.out.println("getShapeList end");
		return shapeList;
    }
    
    //the first element show weather oldlist's all item is contained by new list or not;
    //
    public static ArrayList<Object> compareTwoShapeList(ArrayList<Object> oldList,ArrayList<Object> newList)
    {
    	System.out.println("compareTwoShapeList start");
    	ArrayList<Object> resultArray = new ArrayList<Object>();
    	
    	if(newList.size() < oldList.size())
    	{
    		resultArray.add(false);
    		return resultArray;
    	}
    	
    	ArrayList<Integer> alreadExistInNewArray = new ArrayList<Integer>();    	
    	Boolean isItemExistInNewList = false;
    	
    	for(int i = 0 ; i < oldList.size();i++)
    	{
    		Object oldObj = oldList.get(i);
    		isItemExistInNewList = false;
    		for(int j = 0 ; j< newList.size();j++)
    		{
    			Object newObj = newList.get(j);
    			
    			if(oldObj instanceof ShapeLineData)
    			{
    				if(newObj instanceof ShapeLineData)
    				{
    					if(oldObj.equals(newObj))
    					{
    						alreadExistInNewArray.add(j);
    						isItemExistInNewList = true;
    						break;
    					}
    				}
    				else
    				{
    					//do it later ShapeDecoratedLine
    				}
    			}
    			else if(oldObj instanceof  ShapeEllipticArcData)
    			{
    				if(newObj instanceof ShapeEllipticArcData)
    				{
    					if(oldObj.equals(newObj))
    					{
    						alreadExistInNewArray.add(j);
    						isItemExistInNewList = true;
    						break;
    					}
    				}
    				else
    				{
    					//do it later ShapeDecoratedEllipticArc
    				}
    			}
    			else if(oldObj instanceof ShapeDecoratedLineData)
    			{
    				if(newObj instanceof ShapeDecoratedLineData)
    				{
    					if(oldObj.equals(newObj))
    					{
    						alreadExistInNewArray.add(j);
    						isItemExistInNewList = true;
    						break;
    					}
    				}
    			}
    			else if(oldObj instanceof ShapeDecoratedEllipticArcData)
    			{
    				if(newObj instanceof ShapeDecoratedEllipticArcData)
    				{
    					if(oldObj.equals(newObj))
    					{
    						alreadExistInNewArray.add(j);
    						isItemExistInNewList = true;
    						break;
    					}
    				}	
    			}

    		}
			if(!isItemExistInNewList)
    		{
        		resultArray.add(isItemExistInNewList);
        		System.out.println("compareTwoShapeList end1");
        		return resultArray;
    		}
    	}
    	
    	resultArray.add(isItemExistInNewList);//must be true
    	for(int i = 0 ; i< newList.size();i++)
    	{
    		if(alreadExistInNewArray.contains(i))
    		{
    			continue;
    		}
    		
    		resultArray.add(newList.get(i));
    	}
    	System.out.println("compareTwoShapeList end2");
    	return resultArray;
    }
}
