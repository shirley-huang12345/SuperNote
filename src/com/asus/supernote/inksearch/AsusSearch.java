package com.asus.supernote.inksearch;

import android.text.format.Time;
import android.util.Log;

import com.asus.supernote.data.MetaData;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem.PathInfo;
import com.visionobjects.myscript.engine.Engine;
import com.visionobjects.myscript.engine.EngineObject;
import com.visionobjects.myscript.engine.InputUnit;
import com.visionobjects.myscript.engine.ShortStructuredInput;
import com.visionobjects.myscript.hwr.Archive;
import com.visionobjects.myscript.hwr.Lexicon;
import com.visionobjects.myscript.hwr.Resource;
import com.visionobjects.myscript.inksearch.FindResult;
import com.visionobjects.myscript.inksearch.Finder;
import com.visionobjects.myscript.inksearch.Index;
import com.visionobjects.myscript.inksearch.StringQuery;
import com.visionobjects.myscript.inksearch.StructuredInputIndexer;

public class AsusSearch {

    /**
     * The file where the index will be stored.
     */
	private String mIndexFilePath = "/sdcard/InkIndexing-output.index";
    private ShortStructuredInput mInput = null;
    private StructuredInputIndexer mStructuredInputIndexer = null;
    private StringQuery mStringQuery = null;
    private Index mIndex = null;
    
    private Boolean mIsInited = false;
    private Engine mEngine = null;
    private Resource mResourceAK = null;//(Resource) EngineObject.load(mEngine, CFG.AK_RES);
    private Resource mResourceLK = null;//(Resource)EngineObject.load(mEngine, CFG.LK_TEXT_RES);
    
    public void initEngine(int language)
    {
    	if(!mIsInited)
    	{
    		mIsInited = true;
	    	mEngine = Engine.create(Cert.getBytes());
	    	mResourceAK = (Resource) EngineObject.load(mEngine, CFG.getAK_RESPath(language));// darwin   CFG.PATH_TO_ASSETS +
	    	mResourceLK = (Resource)EngineObject.load(mEngine, CFG.getLK_TEXT_RESPath(language));//darwin  CFG.PATH_TO_ASSETS +
	    	
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
    	}
    }
    
    public void initEngine()
    {
    	if(!mIsInited)
    	{
    		mIsInited = true;
	    	mEngine = Engine.create(Cert.getBytes());
    	}
    }
    
    public void destroyEngine()
    {
    	if(!mIsInited)
    	{
    		mIsInited = false;
    		if(mResourceLK != null)
    		{
    			mResourceLK.dispose();
    		}
    		if(mResourceAK != null)
    		{
    			mResourceAK.dispose();
    		}
	    	mEngine.dispose();
	    	mEngine = null;
    	}
    }
    
    public Boolean prepareIndexFile(String file,int language)
    {
    	mIndexFilePath = file;
    	
        if( mIndexFilePath == null || mIndexFilePath.length() == 0 )
        {
          System.err.println("file name not ok!\n");
          System.exit(-1);
        }
        
        initEngine(language);

        mStructuredInputIndexer = StructuredInputIndexer.create(mEngine);
        mStructuredInputIndexer.attach(mResourceAK);
        mStructuredInputIndexer.attach(mResourceLK);
        initLexicon(); // BEGIN: Shane_Wang 2012-11-12
        mInput = ShortStructuredInput.create(mEngine);
        
    	return true;
    }

    // BEGIN: Shane_Wang 2012-11-12
    private void initLexicon() {
    	try{
	    	Lexicon lexicon = Lexicon.create(mEngine);
	    	for(String elemLexicon : CFG.storedLexicon) {
		        lexicon.addWord(elemLexicon);
	    	}
	        lexicon.compile(); // compile before attaching
	        mStructuredInputIndexer.attach(lexicon);
	        lexicon.dispose();
    	}catch(Exception e) {
    		Log.e("Shane: ", "set lexicon error, no lexicon in CFG?");
        }
    }
    // END: Shane_Wang 2012-11-12
    
    public void addStroke(NoteHandWriteItem item)
    {
    	mInput.startInputUnit(InputUnit.SINGLE_LINE_TEXT);
        for (PathInfo sPathInfo : item.getPathInfo()) {
        	mInput.addStroke(sPathInfo.mPointArray,0,2,
        			sPathInfo.mPointArray,1,2,
        			sPathInfo.mPointArray.length/2);
        }
        mInput.endInputUnit(InputUnit.SINGLE_LINE_TEXT);    	
    }
    
    
    ///
    ///can not add this substring String.valueOf((char) 65532)
    ///it will crash the search.
    public void addString(String temp)
    {
    	if(temp == null )
    	{
    		return;
    	}
    	mInput.startInputUnit(InputUnit.MULTI_LINE_TEXT);
    	try
    	{
    		mInput.addString(temp);  
    	}catch(Exception e)
    	{
            mInput.endInputUnit(InputUnit.MULTI_LINE_TEXT);  	
    		return;
    	}
        mInput.endInputUnit(InputUnit.MULTI_LINE_TEXT);  	
    }
    
    
    public void generateFile()
    {
    	Time startTime=new Time();//.setToNow();
    	Time endTime=new Time();//.setToNow();
    	long useTime = 0;
    	startTime.setToNow();
    	
        mStructuredInputIndexer.setSource(mInput);
        
        endTime.setToNow();
    	useTime = endTime.toMillis(true) - startTime.toMillis(true);
    	Log.d("Richard", "before run" + useTime);
    	
        mStructuredInputIndexer.run();
        
    	endTime.setToNow();
    	useTime = endTime.toMillis(true) - startTime.toMillis(true);
    	Log.d("Richard", "after run" + useTime);
    	
        mIndex = mStructuredInputIndexer.getResult();
        mIndex.store(mIndexFilePath);
    	
        mIndex.dispose();
        mStructuredInputIndexer.dispose();
        mInput.dispose();
        

    }
    
    public void setQueryString(String temp)
    {
    	//set query string;
    	mStringQuery = StringQuery.create(mEngine, temp, false, true, false);
    }   
    
	public FindResult doQuery(String temp, String filename)
    {
		initEngine();
		setQueryString(temp);

		final Finder finder = Finder.create(mEngine);

		finder.attach(mStringQuery);
		mIndex =(Index)EngineObject.load(mEngine, filename);
		finder.attach(mIndex);   
		finder.run();
		final FindResult result = finder.getResult();

		finder.dispose();
		mStringQuery.dispose();
		mIndex.dispose();
		  
		return result;
    }

}
