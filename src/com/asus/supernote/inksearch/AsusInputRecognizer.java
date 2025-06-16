package com.asus.supernote.inksearch;

import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spannable;
import android.util.Log;

import com.asus.supernote.data.MetaData;
import com.asus.supernote.editable.noteitem.NoteForegroundColorItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteTextStyleItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem.PathInfo;
import com.visionobjects.myscript.engine.Engine;
import com.visionobjects.myscript.engine.EngineObject;
import com.visionobjects.myscript.engine.ShortUnstructuredInput;
import com.visionobjects.myscript.hwr.CandidateIterator;
import com.visionobjects.myscript.hwr.Lexicon;
import com.visionobjects.myscript.hwr.RecognitionResult;
import com.visionobjects.myscript.hwr.Resource;
import com.visionobjects.myscript.hwr.UnstructuredInputRecognizer;
import com.visionobjects.myscript.hwr.Archive;

public class AsusInputRecognizer {
	
    private static Boolean mIsInited = false;
    private static Engine mEngine = null;
    private Resource mResourceAK = null;//(Resource) EngineObject.load(mEngine, CFG.AK_RES);
    private Resource mResourceLK = null;//(Resource)EngineObject.load(mEngine, CFG.LK_TEXT_RES);
    private UnstructuredInputRecognizer mUnstructuredInputRecognizer = null;
    private ShortUnstructuredInput mInput = null;
    private int mLanguage = -1;
    
    public static void initEngine()
    {
    	if(!mIsInited)
    	{
    		mIsInited = true;
    		
    		//Add try/catch by Dave.
    		try {
    	    	mEngine = Engine.create(Cert.getBytes());
			}catch (NoClassDefFoundError e){
    			mEngine = null;
    		}catch(NoSuchMethodError e){ 
    			mEngine = null;
    		}
    		catch (Exception e) {
				// TODO: handle exception
				mEngine = null;
			}
    		

    	}
    }
    
    public void loadResource(int language)
    {
    	if(mLanguage != language)
    	{
    		disposeLanguageResource();
    		
	    	mResourceAK = (Resource) EngineObject.load(mEngine, CFG.getAK_RESPath(language));//CFG.PATH_TO_ASSETS +  darwin
	    	mResourceLK = (Resource)EngineObject.load(mEngine, CFG.getLK_TEXT_RESPath(language));//CFG.PATH_TO_ASSETS + darwin

	    	if(MetaData.INDEX_LANGUAGES_EN_OR_NOT[MetaData.INDEX_LANGUAGE_ZH_TW] == MetaData.INDEX_LANGUAGE_NO_EN)
	    	{
		        int numberOfResources = ((Archive)mResourceLK).getAttachedCount();
		        
		        for (int i = 0; i < numberOfResources - 1; i++)
		        {
		            Resource resToDetach = (Resource) ((Archive)mResourceLK).getAttachedAt(i);
		            if (resToDetach.getName().indexOf("en_US")!= -1)
		            {
		            	((Archive)mResourceLK).detach(resToDetach);
		                resToDetach.dispose();
		                break;
		            }
		        }
	    	}

	    	
	    	mUnstructuredInputRecognizer.attach(mResourceAK);
	    	mUnstructuredInputRecognizer.attach(mResourceLK);
	    	initLexicon(); // BEGIN: Shane_Wang 2012-11-12
	    	mLanguage = language;
    	}
    }
    
    // BEGIN: Shane_Wang 2012-11-12
    private void initLexicon() {
    	try{
	    	Lexicon lexicon = Lexicon.create(mEngine);
	    	for(String elemLexicon : CFG.storedLexicon) {
		        lexicon.addWord(elemLexicon);
	    	}
	        lexicon.compile(); // compile before attaching
	        mUnstructuredInputRecognizer.attach(lexicon);
	        lexicon.dispose();
    	}catch(Exception e) {
    		Log.e("Shane: ", "set lexicon error, no lexicon in CFG?");
    	}
    }
    // END: Shane_Wang 2012-11-12
    
    public void disposeLanguageResource()
    {
    	if(!mIsInited)
    	{
			if(mResourceAK != null)
			{
				mUnstructuredInputRecognizer.detach(mResourceAK);   
	    		mResourceLK.dispose();
			}
			if(mResourceLK != null)
			{
				mUnstructuredInputRecognizer.detach(mResourceLK); 
				mResourceLK.dispose();
			} 
    	}
    }
    
    public static void destroyEngine()
    {
    	if(!mIsInited)
    	{
    		mIsInited = false;
	    	mEngine.dispose();
	    	mEngine = null;
    	}
    }
    
    public void prepareUnstructuredInputRecognizer()
    {
    	initEngine();

    	if(mEngine != null) //Dave
    	{
    		mUnstructuredInputRecognizer = UnstructuredInputRecognizer.create(mEngine);
    		mInput = ShortUnstructuredInput.create(mEngine);
    	}
    }
    
    public void addStroke(NoteHandWriteItem item)
    {
    	if(item.getPathInfo() == null)
    		return;
    	for (PathInfo sPathInfo : item.getPathInfo()) {
        	mInput.addStroke(sPathInfo.mPointArray,0,2,
        			sPathInfo.mPointArray,1,2,
        			sPathInfo.mPointArray.length/2);
        }
  	
    }

    public void clearStroke()
    {
    	mInput.clear(false);
    	mInput.dispose();
    	mInput = ShortUnstructuredInput.create(mEngine);
    }

    public String getResult()
    {
    	String res ="";
    	mUnstructuredInputRecognizer.setSource(mInput);
    	mUnstructuredInputRecognizer.run();
    	RecognitionResult result = mUnstructuredInputRecognizer.getResult();
    	CandidateIterator iterator = result.getCandidates();
    	
        if (!iterator.isAtEnd())
        	res = iterator.getLabel();            

        iterator.dispose();
        result.dispose();
        
        mInput.clear(true);
        
        if(res.length() > 0)
        {
        	char lastchar = res.charAt(res.length() - 1);
        	lastchar =  Character.toLowerCase(lastchar);
        	if(lastchar >= 'a' && lastchar <= 'z')
        	{
        		res = res + " ";
        	}
        }
        return res;
    }
    
    public int getHWRResult(Editable inputEditable,int start,int end,ArrayList<NoteHandWriteItem> itemList)
    {            
        int changedCount = 0;
        for(int i = itemList.size() -1; i>=0 ;i--)
        {
        	addStroke(itemList.get(i));
        	int color = itemList.get(i).getColor();
        	float strokeWidth = itemList.get(i).getStrokeWidth();
    		String recognizerString = getResult();
    		changedCount += recognizerString.length() - 1;
    		
    		inputEditable.replace(itemList.get(i).getStart() + start, itemList.get(i).getEnd() + start, recognizerString);
    		inputEditable.removeSpan(itemList.get(i));
    		
    		
    		int currentColorStartIndex = itemList.get(i).getStart();
            if (color != Color.BLACK) {
                for (int j = 0; j < recognizerString.length(); j++) {
                    NoteForegroundColorItem foreColorItem = new NoteForegroundColorItem(color);
                    inputEditable.setSpan(foreColorItem, start+currentColorStartIndex + j , start+currentColorStartIndex +j + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    }
            }
            if (strokeWidth != MetaData.SCRIBBLE_PAINT_WIDTHS_NORMAL) {
                for (int j = 0; j < recognizerString.length(); j++) {
                    NoteTextStyleItem stylespan = new NoteTextStyleItem(Typeface.BOLD);
                    inputEditable.setSpan(stylespan, start + currentColorStartIndex + j, start+ j + currentColorStartIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        
        return changedCount;
    }

}
