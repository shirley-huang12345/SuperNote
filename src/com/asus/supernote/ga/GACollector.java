package com.asus.supernote.ga;


import com.asus.supernote.InputManager;
import com.asus.supernote.data.MetaData;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;

/***
 * 
 * @author Dave
 *
 */

public class GACollector {
    
	public static final boolean isMonkey = SystemProperties.getBoolean("debug.monkey", false);
	public static final boolean isDedug = SystemProperties.getInt("ro.debuggable", 0) == 1;
	public static final boolean isUser = Build.TYPE.equals("user");

	public static final long DEFAULT_VALUE = 0L;
	
	private Tracker mGaTracker = null;
	private GoogleAnalytics mGaInstance = null;
	private static boolean mIsTrackerEnable = true;

	public GACollector(Context context) {
		mGaInstance = GoogleAnalytics.getInstance(context);
		mGaTracker = mGaInstance.newTracker("UA-42149473-14"); //ID for SuperNote
		
		try{        //AT&T / CN device / Monkey - no GA need
			if(MetaData.isATT() || isMonkey || SystemProperties.get("ro.build.asus.sku").equals("CN")) 
		    {
				GoogleAnalytics.getInstance(context).setAppOptOut(true);
				mIsTrackerEnable = false;
		    }
			else { 
				mIsTrackerEnable = AnalyticsReflectionUtility.getEnableAsusAnalytics(context);    
		        GoogleAnalytics.getInstance(context).setAppOptOut(!mIsTrackerEnable);
		    }
		}catch(NoClassDefFoundError err){
			mGaTracker = null;
			MetaData.IS_GA_ON = false;
			mIsTrackerEnable = false;
		}
    }
	
	public static boolean getEnableStatus(){
		return mIsTrackerEnable && MetaData.IS_GA_ON;
	}
	
	private void sendEvent(String category, String action, String label,
            Long value) {
        if(mGaTracker != null){
        	mGaTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
        }
    }
	
	
    /**
     * 用来统计哪种note的template被用的最多
     * @param bookType
	 * @param pageSize
	 */
	public void addNewBook(int bookType, int pageSize)
	{
		String bookTyString = "";
		String bookSizeString = "";
		
		switch (bookType) {
		case 0:
			bookTyString = "normal_white_blank";
			break;
		case 1:
			bookTyString = "normal_white_line";
			break;
		case 2:
			bookTyString = "normal_white_grid";
			break;
		case 3:
			bookTyString = "normal_yellow_blank";
			break;
		case 4:
			bookTyString = "normal_yellow_line";
			break;
		case 5:
			bookTyString = "normal_yellow_grid";
			break;
		case 6:
			bookTyString = "meeting";
			break;
		case 7:
			bookTyString = "diary";
			break;
		case 8:
			bookTyString = "memo";
			break;

		default:
			bookTyString = "normal_white_blank";
			break;
		}
		
		if (pageSize == MetaData.PAGE_SIZE_PAD) {
			bookSizeString = "Pad";
		} else {
			bookSizeString = "Phone";
		}
		
		sendEvent("add_new_book_event", bookTyString, bookSizeString, DEFAULT_VALUE);
	}
	
	
	/**
	 * 用来统计Note通常会编辑/新增到第几页
	 * @param bookType
	 * @param maxPageNum
	 */
	public void addNewPage(int bookType, int maxPageNum)
	{
		String bookTyString = "";
		
		switch (bookType) {
		case 0:
			bookTyString = "normal_white_blank";
			break;
		case 1:
			bookTyString = "normal_white_line";
			break;
		case 2:
			bookTyString = "normal_white_grid";
			break;
		case 3:
			bookTyString = "normal_yellow_blank";
			break;
		case 4:
			bookTyString = "normal_yellow_line";
			break;
		case 5:
			bookTyString = "normal_yellow_grid";
			break;
		case 6:
			bookTyString = "meeting";
			break;
		case 7:
			bookTyString = "diary";
			break;
		case 8:
			bookTyString = "memo";
			break;

		default:
			bookTyString = "normal_white_blank";
			break;
		}
		
		sendEvent("add_new_page_event", bookTyString, null, (long)maxPageNum);
	}
	
	
	/**
	 * 统计哪种编辑模式最常用
	 * @param editorType
	 */
	public void editorModeChange(int editorType)
	{
		String editorTypeString = "";
		
		switch (editorType) {
		case 0:
			editorTypeString = "type";
			break;
		case 1:
			editorTypeString = "scribble";
			break;
		case 2:
			editorTypeString = "doodle";
			break;

		default:
			editorTypeString = "type";
			break;
		}
		
		sendEvent("editor_mode_change_event", editorTypeString, null, DEFAULT_VALUE);
	}
	
	
	/**
	 * 用来统计select功能被呼叫的次数
	 */
	public void selectCall()
	{
		sendEvent("select_call_event", null, null, DEFAULT_VALUE);
	}
	
	/**
	 * 统计scribe mode下笔刷的颜色及粗细
	 * @param brushType
	 * @param colorType
	 */
	public void scribePenSet(int brushType, int colorType)
	{
		String brushTypeString = "";
		String colorTypeString = "";
		
		switch (brushType) {
		case 0:
			brushTypeString = "normal";
			break;
		case 1:
			brushTypeString = "bold";
			break;

		default:
			brushTypeString = "normal";
			break;
		}
		
		switch (colorType) {
		case 0:
			colorTypeString = "green";
			break;
		case 1:
			colorTypeString = "red";
			break;
		case 2:
			colorTypeString = "blue";
			break;
		case 3:
			colorTypeString = "black";
			break;

		default:
			colorTypeString = "green";
			break;
		}
		
		sendEvent("scribe_penSet_event", brushTypeString, colorTypeString, DEFAULT_VALUE);
	}
	
	/**
	 * 统计doodle mode下颜色及类型
	 * @param brushType
	 * @param color
	 */
	public void doodleColorSet(int brushType, String colorStr)
	{
		String brushTypeString = "";
		
		switch (brushType) {
		case 0:
			brushTypeString = "roller";
			break;
		case 1:
			brushTypeString = "pen";
			break;
		case 2:
			brushTypeString = "brush";
			break;
		case 3:
			brushTypeString = "air_brush";
			break;
		case 4:
			brushTypeString = "pencil";
			break;
		case 5:
			brushTypeString = "marker";
			break;

		default:
			brushTypeString = "roller";
			break;
		}
		
		sendEvent("doodle_colorSet_event", brushTypeString, colorStr, DEFAULT_VALUE);
		
	}
	
	/**
	 * 统计doodle mode下笔刷粗细及类型
	 * @param brushType
	 * @param brushWidth
	 */
	public void doodleBrushSet(int brushType, int brushWidth)
	{
		String brushTypeString = "";
		
		switch (brushType) {
		case 0:
			brushTypeString = "roller";
			break;
		case 1:
			brushTypeString = "pen";
			break;
		case 2:
			brushTypeString = "brush";
			break;
		case 3:
			brushTypeString = "air_brush";
			break;
		case 4:
			brushTypeString = "pencil";
			break;
		case 5:
			brushTypeString = "marker";
			break;

		default:
			brushTypeString = "roller";
			break;
		}
		
		sendEvent("doodle_brushSet_event", brushTypeString, null, (long)brushWidth);
	}
	
	
	/**
	 * 统计user insert了哪几种object
	 * @param objectType
	 */
	public void objectInsert(int objectType)
	{
		String objectTypeString = "";
		
		switch (objectType) {
		case 0:
			objectTypeString = "take_photo";
			break;
		case 1:
			objectTypeString = "picture";
			break;
		case 2:
			objectTypeString = "text_image";
			break;
		case 3:
			objectTypeString = "pdf_page";
		case 4:
			objectTypeString = "audio_record";
			break;
		case 5:
			objectTypeString = "video_record";
			break;
		case 6:
			objectTypeString = "text_file";
			break;
		case 7:
			objectTypeString = "timestamp";
			break;
		case 8:
			objectTypeString = "from_clipboard";
			break;
		case 9:
			objectTypeString = "stamp";
			break;
		case 10:
			objectTypeString = "shape";
			break;

		default:
			objectTypeString = "take_photo";
			break;
		}
		
		sendEvent("insert_object_event", objectTypeString, null, DEFAULT_VALUE);
	}
	
	
	/**
	 * 统计user用笔写的笔画数
	 * @param penCount
	 */
	public void penDrawCount(int penCount)
	{
		sendEvent("draw_pen_event", null, null, (long)penCount);
	}

	
	/**
	 * 统计user用手指写的笔画数
	 * @param fingerCount
	 */
	public void fingerDrawCount(int fingerCount)
	{
		sendEvent("draw_finger_event", null, null, (long)fingerCount);
	}
	
	
	/**
	 * 统计user常用的编辑模式是portrait还是landscape
	 * @param screenMode
	 */
	public void screenModeChange(int screenMode)
	{
		String screenModeString = "";
		
		switch (screenMode) {
		case 0:
			screenModeString = "portrait";
			break;
		case 1:
			screenModeString = "landscape";
			break;

		default:
			screenModeString = "portrait";
			break;
		}
		
		sendEvent("screen_mode_event", screenModeString, null, DEFAULT_VALUE);
	}
	
	
	/**
	 * 统计user同步次数
	 */
	public void syncCount()
	{
		sendEvent("sync_event", null, null, DEFAULT_VALUE);
	}	
	
	/**
	 * 统计SuperNote开启次数
	 * @author Emmanual
	 */
	public void openSuperNote()
	{
		sendEvent("open_supernote", null, null, DEFAULT_VALUE);
	}
	
	/**
	 * 统计显示上锁笔记本的次数
	 * @author Emmanual
	 */
	public void showLockedBooks()
	{
		sendEvent("show_locked_books", null, null, DEFAULT_VALUE);
	}
	
	/**
	 * 统计隐藏上锁笔记本的次数
	 * @author Emmanual
	 */
	public void hideLockedBooks()
	{
		sendEvent("hide_locked_books", null, null, DEFAULT_VALUE);
	}
	
	/**
	 * 统计编辑封面的次数
	 * @author Emmanual
	 */
	public void editBookCover()
	{
		sendEvent("edot_book_cover", null, null, DEFAULT_VALUE);
	}
	
	/**
	 * 统计移动页面的次数
	 * @author Emmanual
	 */
	public void movePage()
	{
		sendEvent("move_page", null, null, DEFAULT_VALUE);
	}
	
	/**
	 * 统计复制页面的次数
	 * @author Emmanual
	 */
	public void copyPage()
	{
		sendEvent("copy_page", null, null, DEFAULT_VALUE);
	}
	
	/**
	 * 统计重排序页面的次数
	 * @author Emmanual
	 */
	public void reorderPage()
	{
		sendEvent("reorder_page", null, null, DEFAULT_VALUE);
	}
	
	/**
	 * 统计每种输入模式的使用时间
	 * @author Emmanual
	 * @param editorType
	 * @param time
	 */
	public void inputMethodTime(int editorType, long time)
	{
		String editorTypeString = "";
		
		switch (editorType) {
		case InputManager.INPUT_METHOD_KEYBOARD:
			editorTypeString = "keyboard";
			break;
		case InputManager.INPUT_METHOD_SCRIBBLE:
			editorTypeString = "scribble";
			break;
		case InputManager.INPUT_METHOD_DOODLE:
			editorTypeString = "doodle";
			break;

		default:
			editorTypeString = "keyboard";
			break;
		}
		sendEvent("input_method_time", editorTypeString, null, time);
	}
	
	/**
	 * 统计每种笔刷的绘画笔数
	 * @author Emmanual
	 * @param brushType
	 * @param count
	 */
	public void doodleBrushCount(int brushType, int count )
	{
		String brushTypeString = "";
		
		switch (brushType) {
		case 0:
			brushTypeString = "roller";
			break;
		case 1:
			brushTypeString = "pen";
			break;
		case 2:
			brushTypeString = "brush";
			break;
		case 3:
			brushTypeString = "air_brush";
			break;
		case 4:
			brushTypeString = "pencil";
			break;
		case 5:
			brushTypeString = "marker";
			break;

		default:
			brushTypeString = "roller";
			break;
		}
		sendEvent("doodle_brush_count", brushTypeString, null, (long) count);
	}
	
	/**
	 * 统计鼓励我们对话框是否打分
	 * @author smilefish
	 * @param RateString: RateNow/NotRate
	 */
	public void encourageUs(String RateString)
	{
		sendEvent("EncourageUS", RateString, null, DEFAULT_VALUE);
	}
	
	/**
	 * 统计不同模式下滚动条使用频率
	 * @author smilefish
	 * @param editorType
	 * @param scrollBarType
	 */
	public void scrollBar(int editorType, String scrollBarType)
	{
		String editorTypeString = "";
		
		switch (editorType) {
		case 0:
			editorTypeString = "type";
			break;
		case 1:
			editorTypeString = "scribble";
			break;
		case 2:
			editorTypeString = "doodle";
			break;
			
		case 4:
			editorTypeString = "readonly";

		default:
			editorTypeString = "type";
			break;
		}
		
		sendEvent("scroll_bar_event", editorTypeString, scrollBarType, DEFAULT_VALUE);
	}
}
