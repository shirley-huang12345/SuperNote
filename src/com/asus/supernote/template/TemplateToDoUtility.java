package com.asus.supernote.template;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.NotePageValue;
import com.asus.supernote.editable.NoteEditText;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteSendIntentItem;
import com.asus.supernote.editable.noteitem.NoteStringItem;
import com.asus.supernote.picker.PickerUtility;

public class TemplateToDoUtility {

	public interface onModifiedListener{
    	public void onModified(boolean isModified);
    }
    
    public interface onTextChangedListener{
    	public void onTextChanged(int length);
    }
    
    public interface onLoadingPageIsFullListener{
    	public boolean onLoadingPageIsFull();
    }
    
    public interface onFocusLostListener{
    	public void onFocusLost(TemplateToDoEditText edit);
    }
    
	public static final short TODO_PRIORITY_HIGH = 0;
	public static final short TODO_PRIORITY_NORMAL = 1;
	public static final short TODO_PRIORITY_LOW= 2;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 17;//Smilefish
	
	private Context mContext = null;
	private PageEditor mPageEditor = null;
	private View mParentView = null;
	private NotePage mNotePage = null;
	private LinearLayout templateTodoLinearlayout = null;
	TemplateLayout templatelayout = null;
	private TemplateLinearLayout mTemplateLinearLayout;
	private LinkedList<ToDoItem> toDoList = new LinkedList<ToDoItem>();
	private View mToDoTitleLayout= null;
	private  int todoItemsMaxHetigt = 0;
	private int mTodoLineHeight = 0;
	private int mTodoTitleHeight = 0;
	private Handler handler = new Handler();
	private boolean hasDeleteItem = false;
	private TemplateToDoTitleEditText mToDoTitleEditText = null;
	private onLoadingPageIsFullListener mLoadingPageIsFullListener = null;
	private int deviceType = 0; //smilefish
	private boolean isPhonePageSize = false;
	private PopupWindow mPriorityWindow = null;//smilefish
	private TemplateToDoPriorityButton mPriorityButton; //smilefish
	private ImageView mHighImage = null;//smilefish
	private ImageView mNormalImage = null;//smilefish
	private ImageView mLowImage = null;//smilefish
	private Button mVoiceButton = null;//smilefish
	private SharedPreferences mSharedPreference = null;//smilefish

	public void setLoadingPageIsFullListener(
			onLoadingPageIsFullListener mLoadingPageIsFullListener) {
		this.mLoadingPageIsFullListener = mLoadingPageIsFullListener;
	}
	public TemplateLayout getTemplatelayout() {
		return templatelayout;
	}
	public void setTodoItemsMaxHetigt(int todoItemsMaxHetigt) {
		this.todoItemsMaxHetigt = todoItemsMaxHetigt;
	}
	
	public TemplateToDoUtility(Context mContext,PageEditor mPageEditor,View parentView,TemplateLayout templatelayout,LinearLayout templateTodoLinearlayout,NotePage mNotePage,int mTemplateType){
		this.mContext = mContext;
		this.mPageEditor = mPageEditor;
		this.mParentView = parentView;
		this.mNotePage = mNotePage;
		this.templateTodoLinearlayout = templateTodoLinearlayout;
		this.templatelayout = templatelayout;
		mToDoTitleLayout = (View) mParentView.findViewById(R.id.todoTitleLinearLayout);
    	if(mToDoTitleLayout!=null){
    		LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)mToDoTitleLayout.getLayoutParams();
    		mTodoTitleHeight = lp.height+lp.topMargin;
    	}
    	mTodoLineHeight = mContext.getResources().getInteger(R.integer.todo_line_height);
    	mTemplateLinearLayout = (TemplateLinearLayout) mParentView.findViewById(R.id.template_linearlayout);
    	mToDoTitleEditText = (TemplateToDoTitleEditText)mParentView.findViewById(R.id.todoTitleEditText);
    	mToDoTitleEditText.setContentType(NoteItemArray.TEMPLATE_CONTENT_TODO_TITLE);
    	mToDoTitleEditText.initTodoEdit(this); //smilefish
    	
    	//begin smilefish
		deviceType = PickerUtility.getDeviceType(mContext);
		TextView dateView = (TextView)mParentView.findViewById(R.id.todo_title_date);
		isPhonePageSize = mNotePage.getNoteBook().getPageSize() == MetaData.PAGE_SIZE_PHONE;
		Date date = new Date();
		date.setTime(mNotePage.getModifiedTime());
		java.text.DateFormat dateFormat = DateFormat.getDateFormat(mContext);
		dateView.setText(dateFormat.format(date));
		
		mVoiceButton = (Button)mParentView.findViewById(R.id.memoVoiceButton);          
        mVoiceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                if(!MetaData.READ_ONLY_MODE) //fix TTbug 309688 [Carol]
                    startVoiceInput();
			}
		});
        
        mSharedPreference = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE);
    	//end smilefish
	}
	
	/**
	 * text是否超过了mToDoTitleEditText的宽度
	 * @author noah_zhang
	 * @return
	 */
	public boolean isOverTitleWidth(String text){
		if(mToDoTitleEditText == null)
			return true;
		String content = mToDoTitleEditText.getText().toString();
		content += text;
		return getTitleEditTextWidth() <= measureTextByTitleEditText(content);
	}
	
	/**
	 * 返回title EditTextWidth空间的宽度
	 * @author noah_zhang
	 * @return
	 */
	public int getTitleEditTextWidth(){
		int width = 0;
		if(mToDoTitleEditText != null){
			width = mToDoTitleEditText.getWidth();
		}
		return width;
	}
	
	/**
	 * 使用mToDoTitleEditText，测量text的宽度
	 * @return
	 */
	public float measureTextByTitleEditText(String text){
		float width = 0;
		if(mToDoTitleEditText != null){
			width = mToDoTitleEditText.getPaint().measureText(text);
		}
		return width;
	}
	
	/* load ToDo template book */
	public void LoadTemplateContent(ArrayList<NoteItemArray> allNoteItems){
		int index = 0;
		boolean isPageFull = false;
		try {
			for(NoteItemArray noteItems:allNoteItems){			
				if(noteItems==null||noteItems.getNoteItems().size()==0)
					return;
				switch(noteItems.getTemplateItemType()){
					case NoteItemArray.TEMPLATE_TODO_PAGE_FULL_FLAG:
						NoteItem[] items = noteItems.getNoteItemArray();
						if(items[0].getText().equals("true")){
							isPageFull = true;
							if(mLoadingPageIsFullListener != null){
								if(mLoadingPageIsFullListener.onLoadingPageIsFull()){
									return;
								}
							}					
						}
						break;
					case NoteItemArray.TEMPLATE_CONTENT_TODO_TITLE:
						mToDoTitleEditText.LoadContent(NoteItemArray.TEMPLATE_CONTENT_TODO_TITLE, noteItems.getNoteItemArray());
						break;
					case NoteItemArray.TEMPLATE_SEPERATER_TODO_NEW_ITEM_END://ensure start with 0
						index++;
						break;
					case NoteItemArray.TEMPLATE_CONTENT_TODO_EDIT:
						if(index < toDoList.size()){
							toDoList.get(index).getEditText().LoadContent(NoteItemArray.TEMPLATE_CONTENT_TODO_EDIT, noteItems.getNoteItemArray());
						}
						else{
							AddNewTodoItem(false,true,null,noteItems.getNoteItemArray());
						}
						break;
					case NoteItemArray.TEMPLATE_CONTENT_TODO_CHECKBOX:
						toDoList.get(index).getCheckBox().LoadContent(NoteItemArray.TEMPLATE_CONTENT_TODO_CHECKBOX, noteItems.getNoteItemArray());
						break;
					case NoteItemArray.TEMPLATE_CONTENT_TODO_PRIORITY:
						toDoList.get(index).getPriorityButton().LoadContent(NoteItemArray.TEMPLATE_CONTENT_TODO_PRIORITY, noteItems.getNoteItemArray());
						break;
					case NoteItemArray.TEMPLATE_CONTENT_TODO_MODIFY_TIME:
						toDoList.get(index).LoadLastModified(NoteItemArray.TEMPLATE_CONTENT_TODO_MODIFY_TIME, noteItems.getNoteItemArray());
						break;
				}
			}
			
			if(index < toDoList.size()){
				int removeCount = templateTodoLinearlayout.getChildCount()- index - 1;
				templateTodoLinearlayout.removeViews(index + 1, removeCount);
				for (int i = 0; i < removeCount; i++) {
					toDoList.removeLast();
				}
			}
		} catch (IndexOutOfBoundsException e) {
			Log.e("Allen IndexOutOfBoundsException", "index: " + index);
		}

		loadCursorFocusPosition(); //smilefish fix bug 374903

		return;
	}
	
	//begin smilefish
	private void loadCursorFocusPosition(){
		int editIndex = mSharedPreference.getInt(MetaData.PREFERENCE_MEMO_EDIT_INDEX, -1) ;
		int cursorIndex = mSharedPreference.getInt(MetaData.PREFERENCE_MEMO_CURSOR_INDEX, -1) ;
		if(editIndex == -1){
			mToDoTitleEditText.requestFocus();
			return;
		}
		
		if(editIndex > 0 && editIndex <= toDoList.size()){
			NoteEditText edit = toDoList.get(editIndex - 1).getEditText();
			if(!toDoList.get(editIndex-1).getCheckBox().isChecked()){
				edit.requestFocus();
				if(cursorIndex >= 0 && cursorIndex < edit.length())
					edit.setSelection(cursorIndex);
			}
		}else{
			mToDoTitleEditText.requestFocus(); 
			if(cursorIndex >= 0 && cursorIndex < mToDoTitleEditText.length())
				mToDoTitleEditText.setSelection(cursorIndex);
		}
		
		mSharedPreference.edit().putInt(MetaData.PREFERENCE_MEMO_EDIT_INDEX, -1).commit(); 
		mSharedPreference.edit().putInt(MetaData.PREFERENCE_MEMO_CURSOR_INDEX, -1).commit();
	}
	//end smilefish
	
	public void getTemplateNoteItems(ArrayList<NoteItemArray> noteItemArrayList){
		/* delete invalidate items */
		for(ToDoItem item : toDoList){
			if(item.getEditText().length() == 0){ //smilefish
				toDoList.remove(item);
    			templateTodoLinearlayout.removeView(item.getLinearLayout());
				break;
			}
		}
		
		/* write isPageFull flag to file */
		ArrayList<NoteItem> isPageFullNoteItems = new ArrayList<NoteItem>();
		boolean isPageFull = false;
		if(toDoList.size() > 0 && toDoList.getLast().getEditText().length() != 0 && IsToDoPageFull(mTodoLineHeight)){ //smilefish
			isPageFull = true;
		}
		isPageFullNoteItems.add(new NoteStringItem(isPageFull ? "true" : "false"));
		noteItemArrayList.add(new NoteItemArray(isPageFullNoteItems, NoteItemArray.TEMPLATE_TODO_PAGE_FULL_FLAG));
		
		NoteItemArray noteItem  = null;		
		noteItem = mToDoTitleEditText.getNoteItem();
		if(noteItem != null){
			noteItemArrayList.add(noteItem);
		}
		
		for(ToDoItem item : toDoList){
			noteItem = item.getEditText().getNoteItem();
			
			/* insert seperater between two item*/
			ArrayList<NoteItem> beginNoteItems = new ArrayList<NoteItem>();
			beginNoteItems.add(new NoteStringItem(" "));
			noteItemArrayList.add(new NoteItemArray(beginNoteItems, NoteItemArray.TEMPLATE_SEPERATER_TODO_NEW_ITEM_BEGIN));
			
            if(noteItem!=null){
            	noteItemArrayList.add(noteItem);
            }
            noteItem = item.getCheckBox().getNoteItem();
            if(noteItem!=null){
            	noteItemArrayList.add(noteItem);
            }
            noteItem = item.getPriorityButton().getNoteItem();
            if(noteItem!=null){
            	noteItemArrayList.add(noteItem);
            }
            noteItem = item.getLastModifiedNoteItem();
            if(noteItem!=null){
            	noteItemArrayList.add(noteItem);
            }
            
            ArrayList<NoteItem> endNoteItems = new ArrayList<NoteItem>();
            endNoteItems.add(new NoteStringItem(" "));
			noteItem = new NoteItemArray(endNoteItems, NoteItemArray.TEMPLATE_SEPERATER_TODO_NEW_ITEM_END); 
			noteItemArrayList.add(noteItem);
    	}
    	return;
	}
	
	public void ClearTemplateControlsState(){
		for(ToDoItem item : toDoList){
			item.getCheckBox().ClearControlState();
			item.getEditText().ClearControlState();
			item.getPriorityButton().ClearControlState();
		}
		mTemplateLinearLayout.getLayoutParams().height = todoItemsMaxHetigt;
		hasDeleteItem = false;
		mToDoTitleEditText.ClearControlState();
	}
	
    public void setTodoEditTextEnable(boolean enable){
    	mToDoTitleEditText.setCursorVisible(enable);
    	mToDoTitleEditText.setActivated(enable);
    	mToDoTitleEditText.setNoteEditTextEnabled(enable);
    	
    	for(ToDoItem item : toDoList){
    		NoteEditText edit = item.getEditText();
    		edit.setCursorVisible(enable);
    		edit.setActivated(enable);
    		edit.setNoteEditTextEnabled(enable);
    	}
    }
	
    /* new ToDo template book */
    public void LoadNoTodoItems(){
      int toDoItemCount = toDoList.size();
      //begin smilefish
	    templateTodoLinearlayout.removeViews(1, templateTodoLinearlayout.getChildCount()-1); 
		for(int i=0;i<toDoItemCount;i++){
			toDoList.removeLast();
		}
      
		if(toDoList.size() == 0)
		{
			mToDoTitleEditText.setText("");
			mToDoTitleEditText.setHint(mToDoTitleEditText.getHintText());			
			//emmanual to fix bug 442263
			mToDoTitleEditText.ClearControlState();
		}
		
		mToDoTitleEditText.requestFocus();
		//end smilefish
    }
    
    public void AddNewTodoItem(boolean isChecked,boolean isUsingSetColorOrStyle,CharSequence task,NoteItem[] noteItems){
    	ToDoItem todoItem = new ToDoItem(isChecked,isUsingSetColorOrStyle,task,noteItems,this);
    	toDoList.addLast(todoItem);
    	IsToDoPageFull(mTodoLineHeight);
    }
    
    public boolean AddNewTodoItem(CharSequence task,boolean isUsingSetColorOrStyle,TemplateEditText editText)
    {
    	//begin smilefish
    	EditText tempEditText = new EditText(mContext);
    	tempEditText.setText(task);
    	tempEditText.setGravity(Gravity.START | Gravity.TOP | Gravity.CENTER_VERTICAL | Gravity.CENTER_VERTICAL);
    	float fontSize = NotePageValue.getFontSize(mContext, MetaData.BOOK_FONT_NORMAL, mNotePage.isPhoneSizeMode());
    	tempEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
    	tempEditText.setSingleLine(false);
    	tempEditText.setLineSpacing(0, NoteEditText.TEMPLETE_TODO_LINE_SPACE);
    	tempEditText.setPadding(0, (int) ((NoteEditText.TEMPLETE_TODO_LINE_SPACE-1)*tempEditText.getLineHeight()), 0, 0);
    	int width = 0;
    	if( mNotePage.isPhoneSizeMode())
    		width = mContext.getResources().getDimensionPixelSize(R.dimen.todo_edit_text_width_phone);
    	else
    		width = mContext.getResources().getDimensionPixelSize(R.dimen.todo_edit_text_width_pad);
    	tempEditText.setWidth(width);
    	tempEditText.measure(0, 0);
    	int measureHeight = tempEditText.getMeasuredHeight();
    	//end smilefish
    	
		if(!IsToDoPageFull(measureHeight)){
			AddNewTodoItem(false,isUsingSetColorOrStyle,task,null);
			return true;
		}  
		else{ //smilefish
			//emmanual to add memo in next page
			mToDoTitleEditText.setText("");
			((EditorActivity)mContext).addNewMemoPage(isUsingSetColorOrStyle, task);
//			EditorActivity.showToast(mContext, R.string.editor_page_is_full);
			return true;
		}
    }
    
    //begin smilefish
    public int getToDoListSize(){
    	return toDoList.size();
    }
    //end smilefish
    
    public boolean IsToDoPageFullForContinuses(int offset){   	    	
    	if (mPageEditor != null) {
			int height = 0;
			int itemHeight = 0;
			for(ToDoItem item : toDoList){
				itemHeight = item.getLinearLayout().getMeasuredHeight();
				height += itemHeight == 0 ? mTodoLineHeight : itemHeight;
			}
			
			height += offset + mTodoTitleHeight;
	 		if(height > todoItemsMaxHetigt*mPageEditor.getPageNum()){
	 			if (mPageEditor != null) {
		 			mTemplateLinearLayout.getLayoutParams().height += mPageEditor.getNoteEditText().getEditorPageHeight();
		 			mPageEditor.setTemplateLayoutHeight(mTemplateLinearLayout.getLayoutParams().height);
		 			int num = mPageEditor.getPageNum();
		 			int canNum = (int) Math.ceil((float) mTemplateLinearLayout.getLayoutParams().height / mPageEditor.getPageHeight());
		 			if (canNum > num) {
		 				mPageEditor.setPageNum(canNum);
		 			}
	 			}
			}
    	}   	
    	return false;
    }
    
    private boolean IsToDoPageFullForNonContinuses(int offset)
    {
		int height = 0,itemHeight = 0;
		for(ToDoItem item : toDoList){
			itemHeight = item.getLinearLayout().getMeasuredHeight();
			height += itemHeight == 0 ? mTodoLineHeight : itemHeight;
		}
		
		height+=offset + mTodoTitleHeight;
 		if(height > todoItemsMaxHetigt){
			return true;
		}
		else{
			return false;
		}
    }
	
    public boolean IsToDoPageFull(int offset){  
		if(MetaData.IS_ENABLE_CONTINUES_MODE)
		{
			return IsToDoPageFullForContinuses(offset);
		}
		else
		{
			return IsToDoPageFullForNonContinuses(offset);
		}
    }
	
    public void DeleteToDoItem(TemplateToDoEditText currentEditText){
    	//begin smilefish 
    	if(toDoList.size()>0){    		   			
        	for(ToDoItem item : toDoList){
        		if(item.getEditText()==currentEditText){         			
        			for(int i = toDoList.indexOf(item);i>=0;i--){
        				if(i == 0){
        					if(currentEditText.hasFocus()){
        						mToDoTitleEditText.requestFocus();
        					}
        					break;
						}//end smilefish
        				else if(!toDoList.get(i-1).getCheckBox().isChecked()){
        					if(currentEditText.hasFocus()){
        						toDoList.get(i-1).getEditText().requestFocus();
        					}
        					break;
        				}
        			}
        			templateTodoLinearlayout.removeView(item.getLinearLayout());
        			toDoList.remove(item);
        			hasDeleteItem = true;
        			mPageEditor.onModified(true);
        			 Handler handler = new Handler();
    	                handler.post(new Runnable() {
    	                    @Override
    	                    public void run() {
    	            			templateTodoLinearlayout.requestLayout();
    	            			mPageEditor.drawScreen();
    	                    }
    	                });
        			break;
        		}
        	}
    	}
    }
	   
    public boolean isTodoTemplateEditModified(){
    	if(hasDeleteItem || mToDoTitleEditText.isModified()){
    		return true;
    	}
    	for(ToDoItem item : toDoList){
    		if(item.getEditText().isModified()||item.getCheckBox().isModified()||
    				item.getPriorityButton().isModified())
    		{
    			return true;
    		}
    	}
    	return false;
    }
    
    public void cleanTodoTemplateEditModified(){
    	for(ToDoItem item : toDoList){
    		item.getEditText().setIsModified(false);
    		item.getCheckBox().setIsModified(false);
    		item.getPriorityButton().setIsModified(false);
    	}
		hasDeleteItem = false;
		mToDoTitleEditText.setIsModified(false);
    }
    
    public void onPageNumChanged(int newPageNum){
    	if (mPageEditor != null) {
    		int newLayoutHeight = mPageEditor.getNoteEditText().getEditorPageHeight()*newPageNum;
    		if(mTemplateLinearLayout.getLayoutParams().height < newLayoutHeight){
    			mTemplateLinearLayout.getLayoutParams().height = newLayoutHeight;
    			mPageEditor.setTemplateLayoutHeight(newLayoutHeight);
    		}
    	}
    }
    
    public void getUsedFileList(LinkedList<String> usedFiles){
    	NoteSendIntentItem[] items = null;
    	for(ToDoItem todoitem : toDoList){
    		items = todoitem.getEditText().getText().getSpans(0, todoitem.getEditText().getText().length(), NoteSendIntentItem.class);
            for (NoteSendIntentItem item : items) {
            	usedFiles.add(item.getFileName());
            }
    	}
    	
    	//begin smilefish
    	items = mToDoTitleEditText.getText().getSpans(0, mToDoTitleEditText.getText().length(), NoteSendIntentItem.class);
        for (NoteSendIntentItem item : items) {
        	usedFiles.add(item.getFileName());
        }
    	//end smilefish
    }
    
    //begin smilefish
    public void showVoiceInputButton(boolean flag){
		if(canSystemRecognizeSpeech() && flag) 
			mVoiceButton.setVisibility(View.VISIBLE);
		else
			mVoiceButton.setVisibility(View.INVISIBLE);
    }
    //end smilefish
    
	//begin smilefish
	private boolean canSystemRecognizeSpeech(){
		PackageManager pm = mContext.getPackageManager();
		List<ResolveInfo> list = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if(list.size() > 0){
			return true;
		}
		return false;
	}
	
	private void saveCursorFocusPosition(){
		int editIndex = -1;
		int cursorIndex = -1;
		if(mToDoTitleEditText.hasFocus()){
			editIndex = 0;
			cursorIndex = mToDoTitleEditText.getSelectionEnd();
		}
		else{
			int i = 1;
			for(ToDoItem item : toDoList){
				NoteEditText edit = item.getEditText();
				if(edit.hasFocus()){
					editIndex = i;
					cursorIndex = edit.getSelectionEnd();
					break;
				}
				i++;
			}
		}
		
		if(editIndex >= 0 && cursorIndex >= 0){
			mSharedPreference.edit().putInt(MetaData.PREFERENCE_MEMO_EDIT_INDEX, editIndex).commit(); 
			mSharedPreference.edit().putInt(MetaData.PREFERENCE_MEMO_CURSOR_INDEX, cursorIndex).commit(); 
		}
	}
	
	private void startVoiceInput()
	{
		saveCursorFocusPosition(); //smilefish
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 6);
		try {
			((EditorActivity)mContext).startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//mToDoTitleEditText.requestFocus(); //smilefish
	}
	//end smilefish
	
	//begin smilefish
	private final OnClickListener priorityClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
            if (mPriorityWindow != null && mPriorityWindow.isShowing()) {
            	mPriorityWindow.dismiss();
            }
            
			if(v.getId() == R.id.todo_priority_high) {
				mPriorityButton.setPriority(TODO_PRIORITY_HIGH);
			}
			else if(v.getId() == R.id.todo_priority_normal){
				mPriorityButton.setPriority(TODO_PRIORITY_NORMAL);
			}
			else if (v.getId() == R.id.todo_priority_low) {
				mPriorityButton.setPriority(TODO_PRIORITY_LOW);
			}
		}
		
	};
	
	private void preparePriorityWindow(View parentView){
		//emmanual to fix bug 453601
		if(mPageEditor != null){
	        mPageEditor.hiddenSoftKeyboard();
		}
		
		mPriorityButton = (TemplateToDoPriorityButton)parentView;
		
		if(mPriorityWindow == null){
			View view = View.inflate(mContext, R.layout.template_todo_priority_dialog_layout, null);
			mHighImage = (ImageView)view.findViewById(R.id.todo_priority_high_image);
			mNormalImage = (ImageView)view.findViewById(R.id.todo_priority_normal_image);
			mLowImage = (ImageView)view.findViewById(R.id.todo_priority_low_image);
			
			String[] valueTexts = mContext.getResources().getStringArray(R.array.todo_widget_priority);
			TextView highText = (TextView)view.findViewById(R.id.todo_priority_high_text);
			highText.setText(valueTexts[0]);
			TextView normalText = (TextView)view.findViewById(R.id.todo_priority_normal_text);
			normalText.setText(valueTexts[1]);
			TextView lowText = (TextView)view.findViewById(R.id.todo_priority_low_text);
			lowText.setText(valueTexts[2]);							
			
			View highView = (View)view.findViewById(R.id.todo_priority_high);
			View normalView = (View)view.findViewById(R.id.todo_priority_normal);
			View lowView = (View)view.findViewById(R.id.todo_priority_low);
			highView.setOnClickListener(priorityClickListener);
			normalView.setOnClickListener(priorityClickListener);
			lowView.setOnClickListener(priorityClickListener);
			
			mPriorityWindow = new PopupWindow(
                    view,
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT,
                    false);
            
			mPriorityWindow.setFocusable(true);
			mPriorityWindow.setOutsideTouchable(true);
			mPriorityWindow.setTouchable(true);
			mPriorityWindow.setTouchInterceptor(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                	Log.i("getAction=", event.getAction() + "");
                    if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                        if (mPriorityWindow != null && mPriorityWindow.isShowing()) {
                        	mPriorityWindow.dismiss();
                        }
                        return true;
                    }

                    return false;
                }
            });
		}
		
		switch (mPriorityButton.getPriority()) {
		case TODO_PRIORITY_HIGH:
			mHighImage.setVisibility(View.VISIBLE);
			mNormalImage.setVisibility(View.INVISIBLE);
			mLowImage.setVisibility(View.INVISIBLE);
			break;
		case TODO_PRIORITY_NORMAL:
			mHighImage.setVisibility(View.INVISIBLE);
			mNormalImage.setVisibility(View.VISIBLE);
			mLowImage.setVisibility(View.INVISIBLE);
			break;
		case TODO_PRIORITY_LOW:
			mHighImage.setVisibility(View.INVISIBLE);
			mNormalImage.setVisibility(View.INVISIBLE);
			mLowImage.setVisibility(View.VISIBLE);
			break;
		default:
			mHighImage.setVisibility(View.INVISIBLE);
			mNormalImage.setVisibility(View.VISIBLE);
			mLowImage.setVisibility(View.INVISIBLE);
			break;
		}
		
		mPriorityWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.asus_memo_popup));		
		int x = (int)mContext.getResources().getDimension(R.dimen.todo_priority_dialog_x);
		int y = (int)mContext.getResources().getDimension(R.dimen.todo_priority_dialog_y);
		if(deviceType > 100 && !isPhonePageSize 
				&& mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			x = (int)mContext.getResources().getDimension(R.dimen.todo_priority_dialog_port_x);
			y = (int)mContext.getResources().getDimension(R.dimen.todo_priority_dialog_port_y);
		}
		mPriorityWindow.showAsDropDown(parentView,x,y);
		
	}
	
	//end smilefish
    
    private class ToDoItem{
    	private TemplateCheckBox mCheckBox;
		private TemplateToDoEditText mEditText;
		private Button mDeleteButton;
		private TemplateToDoPriorityButton mPriorityButton;
		private TemplateTodoItemLinearLayout mLinearLayout;
		private long mLastModifyTime;

		public TemplateCheckBox getCheckBox() {
			return mCheckBox;
		}
		public TemplateToDoEditText getEditText() {
			return mEditText;
		}
		public TemplateToDoPriorityButton getPriorityButton() {
			return mPriorityButton;
		}
    	public LinearLayout getLinearLayout() {
			return mLinearLayout; 
		}
		public ToDoItem(boolean isChecked,boolean isUsingSetColorOrStyle, CharSequence task,NoteItem[] noteItems,TemplateToDoUtility todoUtility){
			mLastModifyTime = System.currentTimeMillis();
			mLinearLayout=(TemplateTodoItemLinearLayout)LayoutInflater.from(mContext).inflate(R.layout.template_todo_item, null);
        	mCheckBox = (TemplateCheckBox)mLinearLayout.findViewById(R.id.todoCheckBox);
        	mCheckBox.setContentType(NoteItemArray.TEMPLATE_CONTENT_TODO_CHECKBOX);
        	mCheckBox.initTodoCheckBox(mPageEditor, mNotePage,todoUtility);
        	mCheckBox.setChecked(isChecked);
        	mCheckBox.setOnModifiedListener(mOnModifiedListener);
        	
        	mEditText = (TemplateToDoEditText)mLinearLayout.findViewById(R.id.todoEditText);
        	mEditText.setContentType(NoteItemArray.TEMPLATE_CONTENT_TODO_EDIT);
        	mEditText.initTodoEdit(mPageEditor, mNotePage,todoUtility);
        	mEditText.setCheckBox(mCheckBox);
        	//begin smilefish fix bug 302797
        	if(isUsingSetColorOrStyle){
        		mEditText.setText(task);
        	}else{
	        	mEditText.setIsUsingSetColorOrStyle(false);
	        	mEditText.setText(task); //smilefish
	        	mEditText.setIsUsingSetColorOrStyle(true);
        	}
        	//end smilefish
        	
            mCheckBox.setTodoEditText(mEditText);
            mEditText.setOnModifiedListener(mOnModifiedListener);           

            mDeleteButton = (Button)mLinearLayout.findViewById(R.id.todoDeleteButton);
            final Dialog alertDialog = new AlertDialog.Builder(mContext). 
            		setTitle(R.string.del).
	                setMessage(mContext.getResources().getString(R.string.memo_del)). 
	                setPositiveButton(mContext.getResources().getString(R.string.bookpicker_ok), new DialogInterface.OnClickListener() { 
	                     
	                    @Override 
	                    public void onClick(DialogInterface dialog, int which) { 
	                    	DeleteToDoItem(mEditText);
	                    } 
	                }). 
	                setNegativeButton(mContext.getResources().getString(R.string.bookpicker_cancel), new DialogInterface.OnClickListener() { 
	                     
	                    @Override 
	                    public void onClick(DialogInterface dialog, int which) { 
	                    } 
	                }).create();
            mDeleteButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					alertDialog.show();
				}
			});
            
            
            mPriorityButton = (TemplateToDoPriorityButton)mLinearLayout.findViewById(R.id.todoPriorityButton);
            mPriorityButton.setContentType(NoteItemArray.TEMPLATE_CONTENT_TODO_PRIORITY);
            mPriorityButton.setOnModifiedListener(mOnModifiedListener);
            
            mPriorityButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					preparePriorityWindow(v); //smilefish
				}
			});           
            
            mEditText.setOnFocusLostListener(new onFocusLostListener() {
				
				@Override
				public void onFocusLost(final TemplateToDoEditText edit) {
					handler.post(new Runnable() {
	                    @Override
	                    public void run() {
							DeleteToDoItem(edit);
	                    }
	                });
				}
			});
            
            templateTodoLinearlayout.addView(mLinearLayout);
            if(noteItems!=null){
            	mEditText.LoadContent(NoteItemArray.TEMPLATE_CONTENT_TODO_EDIT, noteItems);
                 handler.post(new Runnable() {
                     @Override
                     public void run() {
                    	 mEditText.requestLayout();
                     }
                 });
            }
            
    	}		
      
        private onModifiedListener mOnModifiedListener =  new onModifiedListener(){

			@Override
			public void onModified(boolean isModified) {
				if(isModified){
					mLastModifyTime = System.currentTimeMillis();
				}
			}
        };
        
        public NoteItemArray getLastModifiedNoteItem() {
    		NoteItem stringItem = new NoteStringItem(mLastModifyTime+"");
            ArrayList<NoteItem> noteItemsArrayList = new ArrayList<NoteItem>();
            noteItemsArrayList.add(stringItem);
            NoteItemArray noteItemArray = new NoteItemArray(noteItemsArrayList,NoteItemArray.TEMPLATE_CONTENT_TODO_MODIFY_TIME);
            return noteItemArray;
    	}
        
    	public boolean LoadLastModified(short contentType, NoteItem[] noteItems) {
    		if(contentType==NoteItemArray.TEMPLATE_CONTENT_TODO_MODIFY_TIME){
    			try{
    				mLastModifyTime = Long.parseLong(noteItems[0].getText());
    			}
    			catch(NumberFormatException e){
    				e.printStackTrace();
    			}
    	    	return true;
    		}
    		else{
    			return false;
    		}
    	}
    }
}
