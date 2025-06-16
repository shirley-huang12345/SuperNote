package com.asus.supernote.editable;

/*
 * author:mars_li@asus.com
 * Description: 
 */
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;

import com.asus.supernote.editable.noteitem.NoteHandWriteItem;

public interface IHandWritePanel {
    public boolean needRecognizer();
    
    public String getRecognizerResult(NoteHandWriteItem noteHandWriteItem);
    
    //when IWriteView timer out call this 
    public void writeFinished(IHandWriteView writeView);
    
    //sometime don't need consider baseline even is baseline opened
    public boolean needNoteBaseLineItem();
    
    //get total height of line
    public int getFullImageSpanHeight();
    
    //get normal height of line
    public int getImageSpanHeight();
    
    public void setPaint(Paint paint);
    
    public void setEnable(boolean isVisblity);
    
    public boolean getEnable(); //smilefish
    
    public void setBaseLine(boolean isEnableBaseLine);
    
    //for recycleBitmaps
    public void close();
    
    //for reloadTimer
    public void reloadTimer();
    
    //add by mars_li for bug about black flash when input board visibility change
    public void setEableListener(IWritePanelEnableListener listener);
    public int getHeightForScroll();
   //end mars_li
    
    //add by mars_li for input panel switch
    boolean reponseBackKey();
    //end mars_li
    
    //get font setting 
    FontMetricsInt getFontMetricInt();
    
    
}
