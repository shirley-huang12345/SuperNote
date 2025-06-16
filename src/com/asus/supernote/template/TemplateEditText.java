package com.asus.supernote.template;

import com.asus.supernote.InputManager;
import com.asus.supernote.R;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.editable.NoteEditText;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.picker.PickerUtility;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.Editable;
import com.asus.supernote.EditorActivity;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class TemplateEditText extends NoteEditText implements TemplateControl{

	public TemplateEditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public TemplateEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray type = context.obtainStyledAttributes(attrs, R.styleable.TemplateEditText);  
        mNeedDrawBackground = type.getBoolean(R.styleable.TemplateEditText_needDrawBackground, false);
        type.recycle(); 
	}

	protected NotePage mNotePage = null;
	protected PageEditor mPageEditor = null;
	protected boolean mNeedDrawBackground = false;
	public void setNeedDrawBackground(boolean mNeedDrawBackground) {
		this.mNeedDrawBackground = mNeedDrawBackground;
	}

	@Override
	public void load(Editable editable) {
		setText(editable);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onSuperMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public short getContentType() {
		return mContentType;
	}
	
	@Override
	public void initNoteEditText(PageEditor pageEditor, int textSize, boolean isSmallScreen) {
		mPageEditor = pageEditor;
        super.mPageEditor = pageEditor;
        mIsSmallScreen = isSmallScreen;
        mScreenWidth = PickerUtility.getScreenWidth(getContext());
		if (getId() != R.id.travel_date_edit) {//emmanual to fix bug 480840
			setPadding(getPaddingLeft(), 0, 0, 0);
		}
        updateTemplateFontSizeFromResource(textSize, isSmallScreen);
        if (mPageEditor != null) {
	        addTextChangedListener(new MyTextWatcher());
        }
        
        //emmanual
		if (mContentType == NoteItemArray.TEMPLATE_CONTENT_MEETING_TOPIC
		        || mContentType == NoteItemArray.TEMPLATE_CONTENT_TRAVEL_TITLE) {
			InputFilter[] filters = {new LengthFilter(getContext().getResources().getInteger(R.integer.todo_title_max_character_num))};  
        	setFilters(filters);
        }
	}
	
	/**
	 * emmanual
	 */
	private int maxCharacterNum = 0; 
	@Override
	public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter){
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		if(maxCharacterNum == 0){
			maxCharacterNum = getContext().getResources().getInteger(R.integer.todo_title_max_character_num);
		}
		if (mContentType == NoteItemArray.TEMPLATE_CONTENT_MEETING_TOPIC
		        || mContentType == NoteItemArray.TEMPLATE_CONTENT_TRAVEL_TITLE) {
			if(maxCharacterNum > 0 && getText().length() >= maxCharacterNum)
			{
				EditorActivity.showToast(getContext(), R.string.todo_title_max_character_num);
			}
        }
	}
	
	//begin smilefish fix bug 300147
	@Override
    protected void replaceChangeWatcher(Editable text) {
    	
    }
	//end smilefish
	
	//begin smilefish
	public void setFontSize()
	{
		int fontSize = getContext().getResources().getInteger(R.integer.meeting_template_font_size);
		setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
	}
	//end smilefish

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if(canGetFocus()){
			if (event.getAction() == MotionEvent.ACTION_UP) {
				if(mPageEditor.getEditorUiUtility().getInputMode() == InputManager.INPUT_METHOD_DOODLE){
					mPageEditor.getEditorUiUtility().setInputMode(InputManager.INPUT_METHOD_SCRIBBLE);
				}
			}
			super.dispatchTouchEvent(event);
		}else{
			if (event.getAction() == MotionEvent.ACTION_UP) {
				super.performClick();
			}
		}
		return true;
	}

	@Override
	public void draw(Canvas canvas) {
		if(mNeedDrawBackground){
			drawContentWithBackground(canvas);
		}
		else{
			super.drawContent(canvas); 	
		}
	}
	
	@Override
	public void setPageEditor(PageEditor mPageEditor, NotePage notePage) {
		this.mPageEditor = mPageEditor;
		this.mNotePage = notePage;
		initNoteEditText(mPageEditor, notePage.getNoteBook().getFontSize(), false);
		mLineCountLimited = 1;
        setTextColor(Color.BLACK);
	}
	
	private boolean canGetFocus(){
		if(mContentType == NoteItemArray.TEMPLATE_CONTENT_METTING_STARTDATE 
				|| mContentType == NoteItemArray.TEMPLATE_CONTENT_MEETING_ENDDATE
				|| mContentType == NoteItemArray.TEMPLATE_CONTENT_MEETING_STARTTIME
				|| mContentType == NoteItemArray.TEMPLATE_CONTENT_MEETING_ENDTIME
				|| mContentType == NoteItemArray.TEMPLATE_CONTENT_TRAVEL_DATE){
			return false;
		}
		else{
			return true;
		}
	}
	
	@Override
	public boolean LoadContent(short contentType,NoteItem[] noteItems){
		if(contentType == mContentType){
			if(mPageEditor!=null){
		        String str = noteItems[0].getText();
		        Editable editable = new SpannableStringBuilder(str);

		        for (int i = 1; i < noteItems.length; i++) {
		            if (noteItems[i].getStart() < 0 || noteItems[i].getEnd() < 0
		                    || noteItems[i].getStart() > editable.length()/* || noteItems[i].getEnd() > editable.length()*/) {
		                continue;
		            }
		            // BEGIN: Shane_Wang 2012-11-5
		            if(noteItems[i].getEnd() > editable.length()) {
		            	noteItems[i].setEnd(editable.length());
		            }
		            // END: Shane_Wang 2012-11-5
		            editable.setSpan(noteItems[i], noteItems[i].getStart(), noteItems[i].getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        }

		        mPageEditor.setFontSize(editable);
		        setIsFirstTimeLoad(true);
		        load(editable);
		        setIsFirstTimeLoad(false);
		        setSelection(editable.length());
		        clearUndoRedoStack();
			}
			else{
				Editable editable = mNotePage.loadNoteEditTextExportPDF(noteItems);
				if(editable!=null){
					setText(editable);
				}
				else{
					setText("");
				}
			}
			return true;			
		}
		else{
			return false;
		}
	}	
	
	@Override
	public NoteItemArray getNoteItem() {
		NoteItemArray noteItemArray = super.getNoteItem(0, getText().length());
		noteItemArray.setTemplateItemType(mContentType); 
        return noteItemArray;
	}
	
	@Override
	public void ClearControlState() {
		if(getText().toString().length() > 0)
			setText(null);
		clearUndoRedoStack();		
	}
	
	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		super.onSelectionChanged(selStart, selEnd);
		if(mContentType==NoteItemArray.TEMPLATE_CONTENT_MEETING_ATTENDEE||
			mContentType==NoteItemArray.TEMPLATE_CONTENT_MEETING_TOPIC||
			mContentType==NoteItemArray.TEMPLATE_CONTENT_TODO_TITLE || //smilefish
			mContentType==NoteItemArray.TEMPLATE_CONTENT_TRAVEL_TITLE)
			autoScroll();
	}
	
    private void autoScroll(){
    	int cursorX = getCursorXPos(getSelectionEnd());
    	int lineHeight = getLineHeight();
    	if(cursorX-getScrollX() > getWidth()-lineHeight){
    		if(canScrollHorizontally(View.TEXT_DIRECTION_INHERIT)){
    			superScrollTo(cursorX - getWidth() + lineHeight, 0);
    		}
    	}else if(cursorX-getScrollX()-lineHeight < 0){
    		superScrollTo((cursorX-lineHeight)>0 ? cursorX-lineHeight:0, 0);
    	}
    }
}
