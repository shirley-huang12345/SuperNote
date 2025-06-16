package com.asus.supernote.template;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteItemArray;

public class TemplateIdList {
	public static final int[] MeetingTemplateIds = new int[]{
		R.id.attendee_button,
		R.id.topic_edit,
		R.id.start_date_edit,
		R.id.start_time_edit,
		R.id.end_date_edit,
		R.id.end_time_edit,
		R.id.attendee_edit,
	};
	
	public static final short[] MeetingTemplateContentType = new short[]{
		NoteItemArray.TEMPLATE_CONTENT_DEFAULT,
		NoteItemArray.TEMPLATE_CONTENT_MEETING_TOPIC,
		NoteItemArray.TEMPLATE_CONTENT_METTING_STARTDATE,
		NoteItemArray.TEMPLATE_CONTENT_MEETING_STARTTIME,
		NoteItemArray.TEMPLATE_CONTENT_MEETING_ENDDATE,
		NoteItemArray.TEMPLATE_CONTENT_MEETING_ENDTIME,
		NoteItemArray.TEMPLATE_CONTENT_MEETING_ATTENDEE,
	};
	
	public static final int[] TravelTemplateIds = new int[]{
		R.id.travel_title_edit,
		R.id.travel_date,
		R.id.travel_date_edit,
		R.id.travel_image,
		R.id.travel_image_edit,
	};
	
	public static final short[] TravelTemplateContentType = new short[]{
		NoteItemArray.TEMPLATE_CONTENT_TRAVEL_TITLE,
		NoteItemArray.TEMPLATE_CONTENT_TRAVEL_DATE,
		NoteItemArray.TEMPLATE_CONTENT_TRAVEL_DATE,
		NoteItemArray.TEMPLATE_CONTENT_TRAVEL_IMAGE,
		NoteItemArray.TEMPLATE_CONTENT_TRAVEL_IMAGE,
	};
	
	public static int[] GetTemplateIdList(int templateType){
		int[] templateControlIds = new int[0];
		switch(templateType){
		case MetaData.Template_type_meeting:
			templateControlIds = MeetingTemplateIds;
			break;
		case MetaData.Template_type_travel:
			templateControlIds = TravelTemplateIds;
			break;
		}
		return templateControlIds;		
	}
	
	public static short[] GetTemplateContentTypeList(int templateType){
		short[] templateContentTypes = new short[0];
		switch(templateType){
		case MetaData.Template_type_meeting:
			templateContentTypes = MeetingTemplateContentType;
			break;
		case MetaData.Template_type_travel:
			templateContentTypes = TravelTemplateContentType;
			break;
		}
		return templateContentTypes;		
	}
}
