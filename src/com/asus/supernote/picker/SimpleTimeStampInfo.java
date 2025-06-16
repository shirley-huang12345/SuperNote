package com.asus.supernote.picker;

import android.text.Editable;


public class SimpleTimeStampInfo {
    public Long 	mbookId;
    public Long 	mpageId; 
    public Long 	mtimestampId;
    public int 		mtimestampPos;
    public Editable  mtimestampContent;
    public String     mbookName;
   
    public SimpleTimeStampInfo()
    {
    	
    }
    
    public SimpleTimeStampInfo(Long bookid, Long pageid, Long timestampId, int timestampPos, 
    		Editable timestampContent, String bookname)
    {
        mbookId = bookid;
        mpageId = pageid;
        mtimestampId = timestampId;
        mtimestampPos = timestampPos;
        mtimestampContent = timestampContent;
        mbookName = bookname;        
    	
    }

}
