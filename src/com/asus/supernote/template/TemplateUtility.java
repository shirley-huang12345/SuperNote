package com.asus.supernote.template;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import android.app.Activity;
import com.asus.commonui.datetimepicker.date.DatePickerDialog;
import com.asus.commonui.datetimepicker.time.RadialPickerLayout;
import com.asus.commonui.datetimepicker.time.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewStub;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.asus.supernote.EditorActivity;
import com.asus.supernote.EditorUiUtility;
import com.asus.supernote.InputManager;
import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.editable.NoteEditText;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.NoteSendIntentItem;
import com.asus.supernote.template.TemplateToDoUtility.onLoadingPageIsFullListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;

public class TemplateUtility {
	private static final String TAG = "TemplateUtility";
	private Context mContext = null;
	private PageEditor mPageEditor = null;
	private View mParentView = null;
	private NotePage mNotePage = null;
	private int mTemplateType = MetaData.Template_type_normal;
	private boolean isPhonePageSize = false;
	private int deviceType;
	
	TemplateLayout templatelayout = null;
	private TemplateToDoUtility mTemplateToDoUtility = null;		
	private boolean isAddNewMeetingPage = false;
	
	private CalendarWrapper mCalendarWrapper;//noah
	private Calendar mTravelCalendar;
	public TemplateUtility(Context context,View parentView,int templateType,NotePage notePage){
		this.mContext = context;
		this.mParentView = parentView;
		this.mTemplateType = templateType;
		this.mNotePage = notePage;
		deviceType = mContext.getResources().getInteger(R.integer.device_type);
		isPhonePageSize = mNotePage.getNoteBook().getPageSize() == MetaData.PAGE_SIZE_PHONE;
		mCalendarWrapper = new CalendarWrapper();
		mTravelCalendar = Calendar.getInstance();
	}
	
	public TemplateUtility(Context context,View parentView,PageEditor pageEditor,int templateType){
		this.mContext = context;
		this.mParentView = parentView;
		this.mPageEditor = pageEditor;
		this.mTemplateType = templateType;
		this.mNotePage = ((EditorActivity)mContext).getNotePage();
		deviceType = mContext.getResources().getInteger(R.integer.device_type);
		isPhonePageSize = mNotePage.getNoteBook().getPageSize() == MetaData.PAGE_SIZE_PHONE;
		mCalendarWrapper = new CalendarWrapper();
		mTravelCalendar = Calendar.getInstance();
	}
	
	public TemplateLayout PrepareTemplateViewStub(){
		int viewStubId = -1;
		int templatelayoutId = -1;
		switch(mTemplateType){
		case MetaData.Template_type_meeting:
			if(deviceType > 100&&isPhonePageSize){
				viewStubId = R.id.template_meeting_viewstub_phone;
				templatelayoutId = R.id.templateMeetinglayoutphone;
			}
			else if(!(deviceType>100)&&!isPhonePageSize){
				viewStubId = R.id.template_meeting_viewstub_pad;
				templatelayoutId = R.id.templateMeetinglayoutpad;
			}
			else{
				viewStubId = R.id.template_meeting_viewstub;
				templatelayoutId = R.id.templateMeetinglayout;
			}
			ViewStub meetingViewStub = (ViewStub)mParentView.findViewById(viewStubId);
			View meetingView = meetingViewStub.inflate();
			prepareTemplateControls(meetingView);
			templatelayout = (TemplateLayout) mParentView.findViewById(templatelayoutId);
			if(mPageEditor!=null){
				mPageEditor.setTemplateLayoutHeight((int)(templatelayout.getLayoutParams().height));
			}
			break;
		case MetaData.Template_type_travel:
			if(deviceType>100&&isPhonePageSize){
				viewStubId = R.id.template_travel_viewstub_phone;
				templatelayoutId = R.id.templateTravellayoutphone;
			}
			else if(!(deviceType>100)&&!isPhonePageSize){
				viewStubId = R.id.template_travel_viewstub_pad;
				templatelayoutId = R.id.templateTravellayoutpad;
			}
			else{
				viewStubId = R.id.template_travel_viewstub;
				templatelayoutId = R.id.templateTravellayout;
			}
			ViewStub travelViewStub = (ViewStub)mParentView.findViewById(viewStubId);
			View travelView = travelViewStub.inflate();
			prepareTemplateControls(travelView);
			templatelayout = (TemplateLayout) mParentView.findViewById(templatelayoutId);
			if(mPageEditor!=null){
				mPageEditor.setTemplateLayoutHeight((int)(templatelayout.getLayoutParams().height));
			}
			break;
		case MetaData.Template_type_todo:
			if(deviceType>100&&isPhonePageSize){
				viewStubId = R.id.template_todo_viewstub_phone;
			}
			else if(!(deviceType>100)&&!isPhonePageSize){
				viewStubId = R.id.template_todo_viewstub_pad;
			}
			else{
				viewStubId = R.id.template_todo_viewstub;
			}
			templatelayoutId = R.id.templateTodolayout;
			ViewStub todoViewStub = (ViewStub)mParentView.findViewById(viewStubId);
			View todoView = todoViewStub.inflate();

			prepareTemplateControls(todoView);
			templatelayout = (TemplateLayout) mParentView.findViewById(templatelayoutId);
			if(mPageEditor!=null){
				mPageEditor.setNoteEditTextInvisiable(false);
				mPageEditor.setTemplateLayoutHeight(mPageEditor.getNoteEditText().getEditorPageHeight());
			}
			mTemplateToDoUtility = new TemplateToDoUtility(mContext, mPageEditor, mParentView, templatelayout, 
									    (LinearLayout) todoView.findViewById(R.id.templateTodoLinearlayout), mNotePage, mTemplateType);
			break;
		}
		if(templatelayout!=null){
			templatelayout.initTemplateLayout(mPageEditor, mNotePage);
		}
		return templatelayout;
	}
	
	/**
	 * @author noah_zhang
	 * @return
	 */
	public TemplateToDoUtility geTemplateToDoUtility(){
		return mTemplateToDoUtility;
	}
	
	public void prepareTemplateControls(View view){
		setTemplateControlContentType();
        View subView = null;
        for (int id : TemplateIdList.GetTemplateIdList(mTemplateType)) {
            subView = view.findViewById(id);
            if (subView == null) {
            	continue;
            }
           switch(id){
           case R.id.attendee_button:
        	   subView.setOnClickListener(onAttendeeButtonClick);
        	   break;
           case R.id.start_date_edit:
        	   subView.setOnClickListener(onStartDateButtonClick);
        	   break;
           case R.id.start_time_edit:
        	   subView.setOnClickListener(onStartTimeButtonClick);	        		        	
        	   break;
           case R.id.end_date_edit:
        	   subView.setOnClickListener(onEndDateButtonClick);
        	   break;
           case R.id.end_time_edit:
        	   subView.setOnClickListener(onEndTimeButtonClick);
        	   break;
           case R.id.travel_date:
           case R.id.travel_date_edit:
        	   subView.setOnClickListener(onTravelDateButtonClick);
        	   break;
           case R.id.travel_image_edit:
        	   subView.setOnClickListener(onTravelImageClick);
        	   break;
           }
        }
	}	
	
	//emmanual to save data for auto page
	Editable mStartDate, mEndDate, mStartTime, mEndTime, mMeetingTopic, mAttendee;
	public void saveMeetingData(){
		mStartDate = ((TemplateEditText)mParentView.findViewById(R.id.start_date_edit)).getText();
		mEndDate = ((TemplateEditText)mParentView.findViewById(R.id.end_date_edit)).getText();
		mStartTime = ((TemplateEditText)mParentView.findViewById(R.id.start_time_edit)).getText();
		mEndTime = ((TemplateEditText)mParentView.findViewById(R.id.end_time_edit)).getText();
		mMeetingTopic = ((TemplateEditText)mParentView.findViewById(R.id.topic_edit)).getText();
		mAttendee = ((TemplateEditText)mParentView.findViewById(R.id.attendee_edit)).getText();
	}
	
	public void loadMeetingData(){
		((TemplateEditText)mParentView.findViewById(R.id.start_date_edit)).setText(mStartDate);
		((TemplateEditText)mParentView.findViewById(R.id.end_date_edit)).setText(mEndDate);
		((TemplateEditText)mParentView.findViewById(R.id.start_time_edit)).setText(mStartTime);
		((TemplateEditText)mParentView.findViewById(R.id.end_time_edit)).setText(mEndTime);
		((TemplateEditText)mParentView.findViewById(R.id.topic_edit)).setText(mMeetingTopic);
		((TemplateEditText)mParentView.findViewById(R.id.attendee_edit)).setText(mAttendee);
	}
	
	String imageFileName, previousFilePath;
	Editable mDiaryDate, mDiaryTopic;
	public void saveTravelData(){
		imageFileName = ((TemplateImageView)mParentView.findViewById(R.id.travel_image)).getImageFilePath();
		if(mPageEditor != null){
			previousFilePath = mPageEditor.getNotePage().getFilePath() + "/" + imageFileName;
		}
		mDiaryDate = ((TemplateEditText)mParentView.findViewById(R.id.travel_date_edit)).getText();
		mDiaryTopic = ((TemplateEditText)mParentView.findViewById(R.id.travel_title_edit)).getText();
	}
	
	public void loadTravelData(){
		((TemplateEditText)mParentView.findViewById(R.id.travel_date_edit)).setText(mDiaryDate);
		((TemplateEditText)mParentView.findViewById(R.id.travel_title_edit)).setText(mDiaryTopic);
		if(imageFileName != null){
			mPageEditor.getAttachmentNameList().add(imageFileName);
			((TemplateImageView) mParentView.findViewById(R.id.travel_image)).setImageFilePath(imageFileName);
			if(previousFilePath != null){
				Bitmap bmp = BitmapFactory.decodeFile(previousFilePath);
				saveTravelImage(mPageEditor.getNotePage().getFilePath() + "/" + imageFileName, bmp);
				((ImageView) mParentView.findViewById(R.id.travel_image)).setImageBitmap(bmp);
			}
		}
	}
	
	private void saveTravelImage(String path, Bitmap bmp){
		File file = new File(path);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//begin smilefish
	public void setMeetingFontSize() {
		TemplateEditText startDateEdit = (TemplateEditText)mParentView.findViewById(R.id.start_date_edit);
		TemplateEditText endDateEdit = (TemplateEditText)mParentView.findViewById(R.id.end_date_edit);
		TemplateEditText startTimeEdit = (TemplateEditText)mParentView.findViewById(R.id.start_time_edit);
		TemplateEditText endTimeEdit = (TemplateEditText)mParentView.findViewById(R.id.end_time_edit);
		TemplateEditText topicEdit = (TemplateEditText)mParentView.findViewById(R.id.topic_edit);
		TemplateEditText attendeeEdit = (TemplateEditText)mParentView.findViewById(R.id.attendee_edit);
		startDateEdit.setFontSize();
		endDateEdit.setFontSize();
		startTimeEdit.setFontSize();
		endTimeEdit.setFontSize();
		topicEdit.setFontSize();
		attendeeEdit.setFontSize();
	}
	//end smilefish
	
	//begin emmanual
	public void setDiaryFont() {
		TemplateEditText dateEdit = (TemplateEditText) mParentView
		        .findViewById(R.id.travel_date_edit);
		dateEdit.setTextSize(
		        TypedValue.COMPLEX_UNIT_SP,
		        mContext.getResources().getInteger(
		                R.integer.diary_template_date_font_size));
		dateEdit.setTextColor(Color.WHITE);
		dateEdit.setIsFirstTimeLoad(false);
	}
	//end emmanual
	
	public void LoadTemplateContent(ArrayList<NoteItemArray> allNoteItems){
		ClearTemplateControlsState();
		mCalendarWrapper = new CalendarWrapper();
		mTravelCalendar = Calendar.getInstance();
		if(allNoteItems==null){
			if(mTemplateType==MetaData.Template_type_todo){
				mTemplateToDoUtility.LoadNoTodoItems();
			}
			//Begin Siupo
			if(mTemplateType == MetaData.Template_type_meeting)
			{
				TemplateEditText startDateEdit = (TemplateEditText)mParentView.findViewById(R.id.start_date_edit);
				TemplateEditText endDateEdit = (TemplateEditText)mParentView.findViewById(R.id.end_date_edit);
				TemplateEditText startTimeEdit = (TemplateEditText)mParentView.findViewById(R.id.start_time_edit);
				TemplateEditText endTimeEdit = (TemplateEditText)mParentView.findViewById(R.id.end_time_edit);
				
				startDateEdit.setIsFirstTimeLoad(true);
				String[] startsStrings = mCalendarWrapper.getStartFormatString().split(" ");
				String[] endStrings = mCalendarWrapper.getEndFormatString().split(" ");
				if(startsStrings.length >= 1){
					startDateEdit.setText(startsStrings[0]);
				}
				startDateEdit.setIsFirstTimeLoad(false);
				endDateEdit.setIsFirstTimeLoad(true);
				if(endStrings.length >= 1){
					endDateEdit.setText(endStrings[0]);
				}
				endDateEdit.setIsFirstTimeLoad(false);
				startTimeEdit.setIsFirstTimeLoad(true);
				if(startsStrings.length >= 2){
					startTimeEdit.setText(startsStrings[1]);
				}
				
				startTimeEdit.setIsFirstTimeLoad(false);
				endTimeEdit.setIsFirstTimeLoad(true);
				if(endStrings.length >= 2){
					endTimeEdit.setText(endStrings[1]);
				}
				endTimeEdit.setIsFirstTimeLoad(false);
				isAddNewMeetingPage = true;
			}
			//End Siupo
			return;
		}
		if(mTemplateType==MetaData.Template_type_todo){
			mTemplateToDoUtility.LoadTemplateContent(allNoteItems);
			return;
		}
		LoadContent(allNoteItems);
		if(mTemplateType == MetaData.Template_type_meeting){
			TemplateEditText startDateEdit = (TemplateEditText)mParentView.findViewById(R.id.start_date_edit);
			TemplateEditText endDateEdit = (TemplateEditText)mParentView.findViewById(R.id.end_date_edit);
			TemplateEditText startTimeEdit = (TemplateEditText)mParentView.findViewById(R.id.start_time_edit);
			TemplateEditText endTimeEdit = (TemplateEditText)mParentView.findViewById(R.id.end_time_edit);
			String start = startDateEdit.getText() + " " + startTimeEdit.getText();
			String end = endDateEdit.getText() + " " + endTimeEdit.getText();
			mCalendarWrapper.setStartCalendar(start);
			mCalendarWrapper.setEndCalendar(end);
		}
		if(mTemplateType == MetaData.Template_type_travel){
			TemplateEditText dateEditText = (TemplateEditText)(mParentView.findViewById(R.id.travel_date_edit));
			try {
				DateFormat DateFormat = new SimpleDateFormat("yyyy/M/d");
				Date date = DateFormat.parse(dateEditText.getText().toString());
				mTravelCalendar.setTime(date);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	private void LoadContent(ArrayList<NoteItemArray> allNoteItems){
		int[] templateControlIds = TemplateIdList.GetTemplateIdList(mTemplateType);
		if(templateControlIds.length==0)
			return;
		for(NoteItemArray noteItems:allNoteItems){
			if(noteItems==null||noteItems.getNoteItems().size()==0)
				return;
	        for (int id : templateControlIds) {
	        	View v = mParentView.findViewById(id);
	        	if(v instanceof TemplateControl){
	        		if(((TemplateControl)v).LoadContent(noteItems.getTemplateItemType(), noteItems.getNoteItemArray()))
		            	break;
	        	}
	        }
		}
	}
	
	public void getTemplateNoteItems(ArrayList<NoteItemArray> noteItemArrayList){
		if(mTemplateType==MetaData.Template_type_todo){
			mTemplateToDoUtility.getTemplateNoteItems(noteItemArrayList);
			return;
		}
		int[] templateControlIds = TemplateIdList.GetTemplateIdList(mTemplateType);
        for (int id : templateControlIds) {
        	View v = mParentView.findViewById(id);
        	if(v instanceof TemplateControl){
        		NoteItemArray noteItem = ((TemplateControl)v).getNoteItem();
                if(noteItem!=null){
                	noteItemArrayList.add(noteItem);
                }
        	}
        }
	}
	
	public void ClearTemplateControlsState(){
		if(mPageEditor==null)
			return;
		if(mTemplateType==MetaData.Template_type_todo){
			mTemplateToDoUtility.ClearTemplateControlsState();
			return;
		}
		int[] templateControlIds = TemplateIdList.GetTemplateIdList(mTemplateType);
        for (int id : templateControlIds) {
        	View v = mParentView.findViewById(id);
        	if(v instanceof TemplateControl){
        		((TemplateControl)v).ClearControlState();
        	}
        }
	}
	
    private void setTemplateControlContentType(){
    	int[] templateIdList = TemplateIdList.GetTemplateIdList(mTemplateType);
    	short[] templateContentTypeList = TemplateIdList.GetTemplateContentTypeList(mTemplateType);
    	if(templateIdList.length==0||templateContentTypeList.length==0||
    			templateIdList.length!=templateContentTypeList.length){
    		return;
    	}
    	for(int i=0;i<templateIdList.length;i++){
    		View v = mParentView.findViewById(templateIdList[i]); 
    		if(v instanceof TemplateControl){
    			((TemplateControl)v).setContentType(templateContentTypeList[i]);
    		}
    	}
    }
    
    private OnClickListener onStartDateButtonClick = new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			if (//mIsPickerDialogShowing||
					mPageEditor.getEditorUiUtility().getInputMode()==InputManager.INPUT_METHOD_READONLE) {
				return;
			}
			DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener(){  				  
				        @Override  
				        public void onDateSet(DatePickerDialog dialog, int year,
	                            int monthOfYear, int dayOfMonth) {
				        	mCalendarWrapper.setStartCalendar(year, monthOfYear, dayOfMonth);
				        	String[] startsStrings = mCalendarWrapper.getStartFormatString().split(" ");
				        	TemplateEditText startDate = (TemplateEditText)(mParentView.findViewById(R.id.start_date_edit));
				        	TemplateEditText startTime = (TemplateEditText)(mParentView.findViewById(R.id.start_time_edit));
				        	if(startsStrings.length >= 1){
				        		startDate.setText(startsStrings[0]);
				        		startDate.setIsModified(true);
				        	}
				        	if(startsStrings.length >= 2){
				        		startTime.setText(startsStrings[1]);
				        		startTime.setIsModified(true);
				        	}
				        	
				        }  
			};
			Calendar calendar = mCalendarWrapper.getStartCalendar();
			 final DatePickerDialog datePickerDialog =  DatePickerDialog.newInstance(onDateSetListener, calendar.get(Calendar.YEAR),
					 calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
			 /*datePickerDialog.setOnDismissListener(new OnDismissListener(){
					@Override
					public void onDismiss(DialogInterface arg0) {
						mIsPickerDialogShowing = false;
					}	        	   
		           });*/
			 Activity activity =( Activity) mContext;
			 if(activity != null){
				 datePickerDialog.show(activity.getFragmentManager(), TAG);	
			 }
		}  	
    };
	
    private OnClickListener onEndDateButtonClick = new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			if (//mIsPickerDialogShowing||
					mPageEditor.getEditorUiUtility().getInputMode()==InputManager.INPUT_METHOD_READONLE) {
				return;
			}
			DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener(){  				  
				        @Override  
				        public void onDateSet(DatePickerDialog dialog, int year, int month, int day) { 
				        	mCalendarWrapper.setEndCalendar(year, month, day);
				        	String[] endStrings = mCalendarWrapper.getEndFormatString().split(" ");
				        	TemplateEditText endDate = (TemplateEditText)(mParentView.findViewById(R.id.end_date_edit));
				        	TemplateEditText endTime = (TemplateEditText)(mParentView.findViewById(R.id.end_time_edit));
				        	if(endStrings.length >= 1){
				        		endDate.setText(endStrings[0]);
				        		endDate.setIsModified(true);
				        	}
				        	if(endStrings.length >= 2){
				        		endTime.setText(endStrings[1]);
				        		endTime.setIsModified(true);
				        	}
				        }  
			};
			Calendar calendar = mCalendarWrapper.getEndCalendar();
			 final DatePickerDialog datePickerDialog =  DatePickerDialog.newInstance(onDateSetListener, calendar.get(Calendar.YEAR),
					 calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
			 Activity activity =( Activity) mContext;
			 if(activity != null){
				 datePickerDialog.show(activity.getFragmentManager(), TAG);	
			 }         					
		}   	
    };
    private OnClickListener onStartTimeButtonClick = new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			if (mPageEditor.getEditorUiUtility().getInputMode()==InputManager.INPUT_METHOD_READONLE) {
				return;
			}
			TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener(){  				  
		        @Override  
		        public void onTimeSet(RadialPickerLayout view,int hourofday, int minute) {  
		        	mCalendarWrapper.setStartCalendar(hourofday, minute);
		        	String[] startsStrings = mCalendarWrapper.getStartFormatString().split(" ");
		        	TemplateEditText startDate = (TemplateEditText)(mParentView.findViewById(R.id.start_date_edit));
		        	TemplateEditText startTime = (TemplateEditText)(mParentView.findViewById(R.id.start_time_edit));
		        	if(startsStrings.length >= 1){
		        		startDate.setText(startsStrings[0]);
		        		startDate.setIsModified(true);
		        	}
		        	if(startsStrings.length >= 2){
		        		startTime.setText(startsStrings[1]);
		        		startTime.setIsModified(true);
		        	}
		        }  
	          };
	          Calendar calendar = mCalendarWrapper.getStartCalendar();
	          boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(mContext);
	          final TimePickerDialog timePickerDialog =  TimePickerDialog.newInstance(onTimeSetListener,
	        		  calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), is24HourFormat);//Siupo
	          Activity activity =( Activity) mContext;
			  if(activity != null){
				  timePickerDialog.show(activity.getFragmentManager(), TAG);	
			  }        
	                    		
		}     
    };
    
    private OnClickListener onEndTimeButtonClick = new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			if (mPageEditor.getEditorUiUtility().getInputMode()==InputManager.INPUT_METHOD_READONLE) {
				return;
			}
			TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener(){  				  
		        @Override  
		        public void onTimeSet(RadialPickerLayout view,int hourofday, int minute) {  
		        	mCalendarWrapper.setEndCalendar(hourofday, minute);
		        	String[] endStrings = mCalendarWrapper.getEndFormatString().split(" ");
		        	TemplateEditText endDate = (TemplateEditText)(mParentView.findViewById(R.id.end_date_edit));
		        	TemplateEditText endTime = (TemplateEditText)(mParentView.findViewById(R.id.end_time_edit));
		        	if(endStrings.length >= 1){
		        		endDate.setText(endStrings[0]);
		        		endDate.setIsModified(true);
		        	}
		        	if(endStrings.length >= 2){
		        		endTime.setText(endStrings[1]);
		        		endTime.setIsModified(true);
		        	}
		        }  
	          };
	          Calendar calendar = mCalendarWrapper.getEndCalendar();
	          boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(mContext);
	          final TimePickerDialog timePickerDialog =  TimePickerDialog.newInstance(onTimeSetListener,
	        		  calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), is24HourFormat);//Siupo
	          Activity activity =( Activity) mContext;
			  if(activity != null){
				  timePickerDialog.show(activity.getFragmentManager(), TAG);	
			  }                		
		}     
    };
    
    private OnClickListener onAttendeeButtonClick = new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			if(mPageEditor.getEditorUiUtility().getInputMode()==InputManager.INPUT_METHOD_READONLE){
				return;
			}
			try {
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);  
				((Activity) (EditorUiUtility.getContextStatic())).startActivityForResult(intent, EditorActivity.CONTACT_PICKER_RESULT);
			} catch (Exception e) {
				e.printStackTrace();
			} 			
		}   
    };
    
    private OnClickListener onTravelDateButtonClick = new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			if (mPageEditor.getEditorUiUtility().getInputMode()==InputManager.INPUT_METHOD_READONLE) {
				return;
			}
			DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener(){  				  
				        @Override  
				        public void onDateSet(DatePickerDialog dialog, int year, int month, int day) {  
				        	TemplateEditText date = (TemplateEditText)(mParentView.findViewById(R.id.travel_date_edit));
				        	mTravelCalendar.set(year, month, day);
				        	date.setTextColor(Color.WHITE);
				        	date.setText(year + "/" + (month + 1) + "/"+day);
				        	date.setIsModified(true);
				        }  
			};			
			Calendar calendar = mCalendarWrapper.getStartCalendar();
			 final DatePickerDialog datePickerDialog =  DatePickerDialog.newInstance(onDateSetListener, calendar.get(Calendar.YEAR),
					 calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

			 Activity activity =( Activity) mContext;
			 if(activity != null){
				 datePickerDialog.show(activity.getFragmentManager(), TAG);	
			 }           						
		}   	
    };
    
    private OnClickListener onTravelImageClick = new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			if(mPageEditor.getEditorUiUtility().getInputMode()==InputManager.INPUT_METHOD_READONLE){
				return;
			}
			((EditorActivity) (mContext)).onTravelTemplateImageClick();
		}   	
    };    
    
    public void setLoadingPageIsFullListener(
			onLoadingPageIsFullListener loadingPageIsFullListener) {
    	if(mTemplateToDoUtility!=null){
    		mTemplateToDoUtility.setLoadingPageIsFullListener(loadingPageIsFullListener);
    	}
	}
    
    public void setTodoItemsMaxHetigt(int todoItemsMaxHetigt){
    	if(mTemplateToDoUtility!=null){
    		mTemplateToDoUtility.setTodoItemsMaxHetigt(todoItemsMaxHetigt);
    	}
    }
    
    public boolean IsToDoPageFull(int offset){
    	if(mTemplateToDoUtility!=null){
    		return mTemplateToDoUtility.IsToDoPageFull(offset); 
    	}
    	return true;
    }
   
    public void onPageNumChanged(int newPageNum){
    	if(mTemplateType==MetaData.Template_type_todo&&
    			mTemplateToDoUtility!=null){
    		mTemplateToDoUtility.onPageNumChanged(newPageNum);
    	}
    }
    
    private void setEditTextEnable(NoteEditText edit ,boolean enable){
    	edit.setCursorVisible(enable);
    	edit.setNoteEditTextEnabled(enable);
    }
    
    public void setEditTextEnable(boolean enable){
    	switch(mTemplateType){
    	case MetaData.Template_type_meeting:
    		setEditTextEnable((NoteEditText)mParentView.findViewById(R.id.topic_edit),enable);
    		setEditTextEnable((NoteEditText)mParentView.findViewById(R.id.attendee_edit),enable);  		
    		break;
    	case MetaData.Template_type_travel:
    		setEditTextEnable((NoteEditText)mParentView.findViewById(R.id.travel_title_edit),enable);
    		break;
    	case MetaData.Template_type_todo:
    		mTemplateToDoUtility.setTodoEditTextEnable(enable);
    		break;
    	}
    }

    
    public boolean isTemplateEditModified(){
    	if(mTemplateType==MetaData.Template_type_todo){
    		return mTemplateToDoUtility.isTodoTemplateEditModified();			
		}
    	int[] templateControlIds = TemplateIdList.GetTemplateIdList(mTemplateType);
        for (int id : templateControlIds) {
        	View v = mParentView.findViewById(id);
        	if(v instanceof TemplateControl){
        		if(((TemplateControl)v).isModified())
            	{
            		return true;
            	}
        	}
        }
    	return false;
    }
    
    public void cleanTemplateEditModified(){
    	if(mTemplateType==MetaData.Template_type_todo){
			mTemplateToDoUtility.cleanTodoTemplateEditModified();
		}
    	if(isAddNewMeetingPage){
    		isAddNewMeetingPage = false;
    		return;
    	}
    	
    	int[] templateControlIds = TemplateIdList.GetTemplateIdList(mTemplateType);
        for (int id : templateControlIds) {
        	View v = mParentView.findViewById(id);
        	if(v instanceof TemplateControl){
        		((TemplateControl)v).setIsModified(false);
        	}
        }   	
    }

    private void getEditUsedFiles(EditText edit,LinkedList<String> usedFiles){
    	NoteSendIntentItem[] items = edit.getText().getSpans(0, edit.getText().length(), NoteSendIntentItem.class);
        for (NoteSendIntentItem item : items) {
        	usedFiles.add(item.getFileName());
        }
    }
    
    public LinkedList<String> getUsedFileList(){
    	LinkedList<String> usedFiles = new LinkedList<String>();
    	switch(mTemplateType){
    	case MetaData.Template_type_meeting:
    		getEditUsedFiles((EditText)mParentView.findViewById(R.id.topic_edit),usedFiles);
    		getEditUsedFiles((EditText)mParentView.findViewById(R.id.attendee_edit),usedFiles);
    		break;
    	case MetaData.Template_type_travel:
    		getEditUsedFiles((EditText)mParentView.findViewById(R.id.travel_title_edit),usedFiles);
    		usedFiles.add(((TemplateImageView)mParentView.findViewById(R.id.travel_image)).getImageFilePath());
    		break;
    	case MetaData.Template_type_todo:
    		mTemplateToDoUtility.getUsedFileList(usedFiles);
    		break;
    	}
		return usedFiles;
    }
    
    //begin smilefish
    public void showVoiceInputButton(boolean flag){
    	if(mTemplateType == MetaData.Template_type_todo)
    		mTemplateToDoUtility.showVoiceInputButton(flag);
    }
    //end smilefish
    
    //begin noah
    public class CalendarWrapper
    {
    	private DateFormat mDateFormat;
    	private Calendar mStartCalendar;
    	private Calendar mEndCalender;
    	public CalendarWrapper()
    	{
    		mDateFormat = new SimpleDateFormat("yyyy/M/d HH:mm");
    		mStartCalendar = Calendar.getInstance();
    		mEndCalender = (Calendar)mStartCalendar.clone();
    		mEndCalender.add(Calendar.HOUR, 1);
    	}
    	public CalendarWrapper(String start, String end)
    	{
    		mDateFormat = new SimpleDateFormat("yyyy/M/d HH:mm");
    		Calendar now = Calendar.getInstance();
    		Date startDate = now.getTime();
    		Date endDate = now.getTime();
    		try {
				startDate = mDateFormat.parse(start);
			} catch (Exception e) {
			}
    		try {
    			endDate = mDateFormat.parse(end);
			} catch (Exception e) {
			}
    		
    		mStartCalendar = Calendar.getInstance();
    		mEndCalender = Calendar.getInstance();
    		mStartCalendar.setTime(startDate);
    		mEndCalender.setTime(endDate);
    		if(mStartCalendar.after(mEndCalender)){
    			mEndCalender = (Calendar)mStartCalendar.clone();
    			mEndCalender.add(Calendar.HOUR, 1);
    		}
    	}
    	
    	public Calendar getStartCalendar(){
    		return mStartCalendar;
    	}
    	
    	public Calendar getEndCalendar(){
    		return mEndCalender;
    	}
    	
    	public void setDateFormat(DateFormat dateFormat){
    		mDateFormat = dateFormat;
    	}
    	
    	public void setStartCalendar(String start){
    		try {
				Date date = mDateFormat.parse(start);
				mStartCalendar.setTime(date);
				if(mStartCalendar.after(mEndCalender)){
					mStartCalendar = (Calendar)mEndCalender.clone();
					mStartCalendar.add(Calendar.HOUR, -1);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
    	}
    	
    	public void setStartCalendar(int year, int month, int day){
    		mStartCalendar.set(year, month, day);
    		if(mStartCalendar.after(mEndCalender)){
				mEndCalender = (Calendar)mStartCalendar.clone();
				mEndCalender.add(Calendar.HOUR, 1);
				
	        	String[] endStrings = getEndFormatString().split(" ");
	        	TemplateEditText endDate = (TemplateEditText)(mParentView.findViewById(R.id.end_date_edit));
	        	TemplateEditText endTime = (TemplateEditText)(mParentView.findViewById(R.id.end_time_edit));
	        	if(endStrings.length >= 1){
	        		endDate.setText(endStrings[0]);
	        		endDate.setIsModified(true);
	        	}
	        	if(endStrings.length >= 2){
	        		endTime.setText(endStrings[1]);
	        		endTime.setIsModified(true);
	        	}
			}
    	}
    	
    	public void setStartCalendar(int hour, int minute){
    		mStartCalendar.set(Calendar.HOUR_OF_DAY, hour);
    		mStartCalendar.set(Calendar.MINUTE, minute);
    		if(mStartCalendar.after(mEndCalender)){
				mEndCalender = (Calendar)mStartCalendar.clone();
				mEndCalender.add(Calendar.HOUR, 1);
				
	        	String[] endStrings = getEndFormatString().split(" ");
	        	TemplateEditText endDate = (TemplateEditText)(mParentView.findViewById(R.id.end_date_edit));
	        	TemplateEditText endTime = (TemplateEditText)(mParentView.findViewById(R.id.end_time_edit));
	        	if(endStrings.length >= 1){
	        		endDate.setText(endStrings[0]);
	        		endDate.setIsModified(true);
	        	}
	        	if(endStrings.length >= 2){
	        		endTime.setText(endStrings[1]);
	        		endTime.setIsModified(true);
	        	}
			}
    	}
    	
    	public String getStartFormatString(){
    		return mDateFormat.format(mStartCalendar.getTime());
    	}
    	
    	public void setEndCalendar(String end){
    		try {
    			Date date = mDateFormat.parse(end);
				mEndCalender.setTime(date);
				if(mStartCalendar.after(mEndCalender)){
					mEndCalender = (Calendar)mStartCalendar.clone();
					mEndCalender.add(Calendar.HOUR, 1);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
    	}
    	
    	public void setEndCalendar(int year, int month, int day){
    		mEndCalender.set(year, month, day);
    		if(mStartCalendar.after(mEndCalender)){
				mStartCalendar = (Calendar)mEndCalender.clone();
				mStartCalendar.add(Calendar.HOUR, -1);
				
	        	String[] startsStrings = getStartFormatString().split(" ");
	        	TemplateEditText startDate = (TemplateEditText)(mParentView.findViewById(R.id.start_date_edit));
	        	TemplateEditText startTime = (TemplateEditText)(mParentView.findViewById(R.id.start_time_edit));
	        	if(startsStrings.length >= 1){
	        		startDate.setText(startsStrings[0]);
	        		startDate.setIsModified(true);
	        	}
	        	if(startsStrings.length >= 2){
	        		startTime.setText(startsStrings[1]);
	        		startTime.setIsModified(true);
	        	}
			}
    	}
    	
    	public void setEndCalendar(int hour, int minute){
    		mEndCalender.set(Calendar.HOUR_OF_DAY, hour);
    		mEndCalender.set(Calendar.MINUTE, minute);
    		if(mStartCalendar.after(mEndCalender)){
				mStartCalendar = (Calendar)mEndCalender.clone();
				mStartCalendar.add(Calendar.HOUR, -1);
				
	        	String[] startsStrings = getStartFormatString().split(" ");
	        	TemplateEditText startDate = (TemplateEditText)(mParentView.findViewById(R.id.start_date_edit));
	        	TemplateEditText startTime = (TemplateEditText)(mParentView.findViewById(R.id.start_time_edit));
	        	if(startsStrings.length >= 1){
	        		startDate.setText(startsStrings[0]);
	        		startDate.setIsModified(true);
	        	}
	        	if(startsStrings.length >= 2){
	        		startTime.setText(startsStrings[1]);
	        		startTime.setIsModified(true);
	        	}
			}
    	}
    	
    	public String getEndFormatString(){
    		return mDateFormat.format(mEndCalender.getTime());
    	}
    }
    //end noah
 
}
