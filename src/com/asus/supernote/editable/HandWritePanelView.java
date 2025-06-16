package com.asus.supernote.editable;

import com.asus.supernote.InputManager;
import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.inksearch.AsusInputRecognizer;
import com.asus.supernote.inksearch.CFG;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;

public class HandWritePanelView extends LinearLayout implements IHandWritePanel {

    private PageEditorManager mPageEditorManager;
    private AsusInputRecognizer mAsusInputRecognizer = null;
    private int mLanguage = -1;
    private IHandWriteView mNormalView1;
    private IHandWriteView mNormalView2;
    IWritePanelEnableListener mEnableListener;
    TranslateAnimation mShowAction;
    TranslateAnimation mHiddenAction;
    private SharedPreferences mPreference = null;

    public HandWritePanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        
        if(CFG.getCanDoVO() == true)//darwin
        {
            try{
                mAsusInputRecognizer = new AsusInputRecognizer();
                mAsusInputRecognizer.prepareUnstructuredInputRecognizer();
            }catch(Exception e)
            {
                e.printStackTrace();
                mAsusInputRecognizer = null;
            }
        }
        
        //disable  for dashline bug 
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,     
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,     
                1.0f, Animation.RELATIVE_TO_SELF, 0.0f); 
        mShowAction.setDuration(300);
        
        mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF,     
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,     
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,     
                1.0f);    
        mHiddenAction.setDuration(300);
        mPreference = context.getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE);
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        mNormalView1 = (IHandWriteView) this.findViewById(R.id.hand_writing_view1);
        mNormalView2 = (IHandWriteView) this.findViewById(R.id.hand_writing_view2);
        
        mNormalView1.setInputPanel(this);
        mNormalView2.setInputPanel(this);
        mNormalView1.loadTimer();
        mNormalView2.loadTimer();
        
        boolean isEnableBaseLine =  getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
                .getBoolean(getContext().getResources().getString(R.string.pref_baseline), false);//smilefish
        mNormalView1.setBaseLineMode(isEnableBaseLine);
        mNormalView2.setBaseLineMode(isEnableBaseLine);
        
        Button buttonDelete = (Button) this.findViewById(R.id.hand_wirte_edit_delete);
        buttonDelete.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                PageEditor pageEditor = mPageEditorManager.getCurrentPageEditor();
                pageEditor.insertBackSpace();
            }
            
        });
        
        Button buttonSpace = (Button) this.findViewById(R.id.hand_wirte_edit_space);
        buttonSpace.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                PageEditor pageEditor = mPageEditorManager.getCurrentPageEditor();
                pageEditor.insertSpace();
            }
            
        });
        
        Button buttonEnter = (Button) this.findViewById(R.id.hand_wirte_edit_enter);
        buttonEnter.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                PageEditor pageEditor = mPageEditorManager.getCurrentPageEditor();
                pageEditor.insertEnter();
            }
            
        });
        
        
        
    }

    @Override
    public boolean needRecognizer() {
        // TODO Auto-generated method stub
        PageEditor pageEditor = mPageEditorManager.getCurrentPageEditor();
        if(pageEditor.getEditorUiUtility().getIsAutoChangeWriteToType() && 
                mAsusInputRecognizer != null){
            return true;
        }
        
        return false;
    }

    @Override
    public String getRecognizerResult(NoteHandWriteItem noteHandWriteItem) {
        // TODO Auto-generated method stub
        if(mAsusInputRecognizer == null)
            return null;
        int language = getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
                .getInt(getContext().getResources().getString(R.string.pref_index_language), 0);
        if(mLanguage != language){
            try
            {
                mAsusInputRecognizer.loadResource(language);
            }catch(Exception e){
                return null;
            }
        }
        mAsusInputRecognizer.addStroke(noteHandWriteItem);
        return mAsusInputRecognizer.getResult();
    }

    @Override
    public void writeFinished(IHandWriteView writeView) {
        // TODO Auto-generated method stub
        boolean isBackspaceStatus = getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
                .getBoolean(getContext().getResources().getString(R.string.pref_backspace_gesture), false); //By Show
        PageEditor pageEditor = mPageEditorManager.getCurrentPageEditor();
        
        if(isBackspaceStatus&&writeView.isDeleteGesture()){
            pageEditor.insertBackSpace();
        }
        else{
            CharSequence s = writeView.genResultSpannableString();
            if(s != null)
            pageEditor.addItemToEditText(s);
        }
        

    }

    @Override
    public boolean needNoteBaseLineItem() {
        // TODO Auto-generated method stub
        PageEditor pageEditor = mPageEditorManager.getCurrentPageEditor();
        short contentType  = ((NoteEditText)pageEditor.getCurrentEditor()).getContentType();
        if( contentType == NoteItemArray.TEMPLATE_CONTENT_MEETING_ATTENDEE
                || contentType == NoteItemArray.TEMPLATE_CONTENT_MEETING_TOPIC
                || contentType == NoteItemArray.TEMPLATE_CONTENT_TRAVEL_TITLE){
            return false;
        }
        return true;
    }

    @Override
    public int getFullImageSpanHeight() {
        // TODO Auto-generated method stub
        PageEditor pageEditor = mPageEditorManager.getCurrentPageEditor();
        return pageEditor.getFullImageSpanHeight();
    }

    @Override
    public int getImageSpanHeight() {
        // TODO Auto-generated method stub
        PageEditor pageEditor = mPageEditorManager.getCurrentPageEditor();
        return pageEditor.getImageSpanHeight();
    }

    @Override
    public FontMetricsInt getFontMetricInt() {
        // TODO Auto-generated method stub
    	if(mPageEditorManager == null) //Dave
    		return null;
    	
        PageEditor pageEditor = mPageEditorManager.getCurrentPageEditor();
        Paint textPaint = new Paint();
        FontMetricsInt fontMetricsInt;
        textPaint.setTextSize(pageEditor.getNoteEditTextFontSize());
        fontMetricsInt = textPaint.getFontMetricsInt();
        return fontMetricsInt;
    }
    
    public void setPageEditorManager(PageEditorManager pageEditorManager) {
        // TODO Auto-generated method stub
        mPageEditorManager = pageEditorManager;     
    }

    @Override
    public void setEnable(boolean isVisblity) {
        // TODO Auto-generated method stub
    	if(this.getVisibility() == (isVisblity?View.VISIBLE:View.GONE)){
    		return;
    	}
    	
    	boolean visblityChange = false;
    	int inputMode = mPreference.getInt(MetaData.PREFERENCE_INPUT_MODE_KEY, 
        		MetaData.isATT()? InputManager.INPUT_METHOD_KEYBOARD:InputManager.INPUT_METHOD_SCRIBBLE);
    	if(isVisblity){
        	if(this.getVisibility() != View.VISIBLE)
        		visblityChange = true;
            this.setVisibility(View.VISIBLE);
            if(inputMode == InputManager.INPUT_METHOD_SCRIBBLE){
                this.startAnimation(mShowAction);
            }
        }else{
        	if(this.getVisibility() != View.GONE){
        		visblityChange = true;
        	}else{
        		return ;
        	}
        	
            this.setVisibility(View.GONE);
            if(inputMode == InputManager.INPUT_METHOD_SCRIBBLE){
            	this.startAnimation(mHiddenAction);
            }  
        }
        
        if(mEnableListener != null&&visblityChange){
        	mEnableListener.onEnableChange(isVisblity);
        }
    }
    
    @Override
    public boolean getEnable(){//add by smilefish
    	return this.getVisibility() == View.VISIBLE;
    }

    @Override
    public void setPaint(Paint paint) {
        // TODO Auto-generated method stub
        if(this.mNormalView1 != null){
            mNormalView1.setPaint(paint);
        }
        
        if(this.mNormalView2 != null){
            mNormalView2.setPaint(paint);
        }
    }

    @Override
    public void setBaseLine(boolean isEnableBaseLine) {
        // TODO Auto-generated method stub
        if(this.mNormalView1 != null){
            mNormalView1.setBaseLineMode(isEnableBaseLine);
        }
        
        if(this.mNormalView2 != null){
            mNormalView2.setBaseLineMode(isEnableBaseLine);
        }
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        if(this.mNormalView1 != null){
            mNormalView1.recycleBitmaps();
        }
        
        if(this.mNormalView2 != null){
            mNormalView2.recycleBitmaps();
        }
    }

    @Override
    public void reloadTimer() {
        // TODO Auto-generated method stub
        if(this.mNormalView1 != null){
            mNormalView1.loadTimer();
        }
        
        if(this.mNormalView2 != null){
            mNormalView2.loadTimer();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        super.onSizeChanged(w, h, oldw, oldh);
        float minHeight = getContext().getResources().getDimension(R.dimen.hand_panel_view_min_height);
        if(h<=minHeight){
            mNormalView2.setEnable(false);
        }else{
            mNormalView2.setEnable(true);
        }
    }

	@Override
	public void setEableListener(IWritePanelEnableListener listener) {
		// TODO Auto-generated method stub
		mEnableListener = listener;
	}

	@Override
	public int getHeightForScroll() {
		// TODO Auto-generated method stub
		return (int)this.getResources().getDimension(R.dimen.hand_panel_view_height);
	}

	@Override
	public boolean reponseBackKey() {
		// TODO Auto-generated method stub
		return true;
	}

}
