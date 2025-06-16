package com.asus.supernote.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.provider.BaseColumns;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import com.asus.supernote.R;
import com.asus.supernote.publicFinalStrings;
import com.asus.supernote.sync.LocalPageItem;
import com.asus.supernote.sync.SyncPageItem;

public class MetaData {
	public static String DEBUG_TAG = "supernote_blank_page_issue";
	
	public static final int DEVICE_TYPE_A86 = 2; //smilefish
	public static final int DEVICE_TYPE_320DP = -1; //smilefish
	public static final int DEFAULT_SYNC_TIME = 30; //smilefish
	
	public static final String existLastPage = "exist_last_page";
	
	// BEGIN: Better
	public static boolean SyncUpgradedFirstTime = false;
	public static ArrayList<LocalPageItem> SyncLocalUntreatedPageList = new ArrayList<LocalPageItem>();
	public static Context AppContext = null;
	public static String DEFAULT_SYNC_MODE = "auto";
	public static boolean HasMultiClipboardSupport = false;
	public static Long SyncExceptionPadBookId = -1L;
	public static Long SyncExceptionPhoneBookId = -1L;
	public static String DefaultPadId = "SyncExceptionPadBookId";
	public static String DefaultPhoneId = "SyncExceptionPhoneBookId";
	public static ArrayList<SyncPageItem> SyncExceptionList = new ArrayList<SyncPageItem>();
	public static String SAVE_PAGE = "SAVE_PAGE";
	public static String RELOAD_PAGE = "RELOAD_PAGE";
	public static String RELOAD_PAGE_ID = "RELOAD_PAGE_ID";
	public static long CurrentEditPageId = -1;
	public static boolean SavingEditPage = false;
	public static boolean SavedEditPage = false;
	public static boolean IsJustLaunching = true;
	public static int SyncCurrentPage = 0;
	public static int SyncTotalPage = 0;
	public static long CurUserAccount = 0L;
	public static int SyncFailedPageCount = 0;
	public static String SYNC_UPDATE_UI = "SYNC UPDATE UI";
	public static String CHOOSE_BOOKS = "CHOOSE BOOKS";
	public static String LOGIN_MESSAGE = "LOGIN MESSAGE";
	public static String LOGIN_RESULT = "LOGIN RESULT";
	public static String LOGIN_ACCOUNT = "LOGIN ACCOUNT";
	public static String LOGIN_USER_SPACE = "LOGIN USER SPACE";
	public static String LOGIN_INTENT_FROM = "LOGIN INTENT FROM";
	public static int Sync_Result = -4;
	public static String SyncSucessTime = null;
	public static ArrayList<Long> V1BookIdList = new ArrayList<Long>();
	public static ArrayList<Long> TransferringBookIdList = new ArrayList<Long>();
	public static ArrayList<Long> SavingPageIdList = new ArrayList<Long>();
	public static final Boolean IS_ENABLE_WEBSTORAGE_DATA_MIGRATING = false;
	public final static String SYNC_VERSION_2 = "2";
	public final static String SYNC_VERSION_3_1 = "3.1";
	public final static String SYNC_VERSION_3_2 = "3.2";
	public final static String SYNC_VERSION_3_3 = "3.3";
	public final static String SYNC_VERSION_3_4 = "3.4";

	public static final boolean Switch_Neon_Pressure = false;
	//end noah
	
	public static String SYNC_CURRENT_VERSION = SYNC_VERSION_3_4;
	public static String[] SupportedVersions = {
		//SYNC_VERSION_2,
		SYNC_VERSION_3_1,
		SYNC_VERSION_3_2,
		SYNC_VERSION_3_3,
		SYNC_VERSION_3_4
	};
	
	public static boolean IsFileVersionCompatible = true;
	public static ArrayList<String> NotSupportedVersionList = new ArrayList<String>();
	public static final float PRESSURE_FACTOR = 10000.0f;
	public static final short ITEM_VERSION_302 = 0x302;
	public static final short ITEM_VERSION_303 = 0x303;
	public static final short ITEM_VERSION_304 = 0x304;
	public static short ITEM_VERSION = ITEM_VERSION_304;
	public static short[] SupportedDataFormatVersions = {
		ITEM_VERSION_302,
		ITEM_VERSION_303,
		ITEM_VERSION_304,
	};

	static{
		if(Switch_Neon_Pressure){
			SYNC_CURRENT_VERSION = SYNC_VERSION_3_4;
			ITEM_VERSION = 0x304;
			SupportedVersions = new String[]{
					SYNC_VERSION_3_1,
					SYNC_VERSION_3_2,
					SYNC_VERSION_3_3,
					SYNC_VERSION_3_4
				};
			SupportedDataFormatVersions = new short[]{
					ITEM_VERSION_302,
					ITEM_VERSION_303,
					ITEM_VERSION_304,
				};
		}else {
			SYNC_CURRENT_VERSION = SYNC_VERSION_3_3;
			ITEM_VERSION = 0x303;
			SupportedVersions = new String[]{
					//SYNC_VERSION_2,
					SYNC_VERSION_3_1,
					SYNC_VERSION_3_2,
					SYNC_VERSION_3_3
				};
			SupportedDataFormatVersions = new short[]{
					ITEM_VERSION_302,
					ITEM_VERSION_303,
				};
		}
	}
	//end noah;for 数据格式版本切换;5.30

	public static final int CLIPBOARD_TYPE_NONE = 0;
	public static final int CLIPBOARD_TYPE_NOTE = 1;
	public static final int CLIPBOARD_TYPE_DOODLE = 2;
	public static final String PREFERENCE_EDITOR_COPY_CONTENT_TYPE = "CopyContentType";
	public static final String PREFERENCE_EDITOR_COPY_PAGE_PATH = "CoyPagePath";
	public static final String CLIPBOARD_NOTE_DESC = "SuperNote_Note";
	public static final String CLIPBOARD_DOODLE_DESC = "SuperNote_Doodle";

	//emmanual to copy from QuickMemo to SuperNote
	public static final String CLIPBOARD_MEMO_DESC = "QuickMemo_Note";
	public static final String QUICK_MEMO_ROOT_DIR = Environment
	        .getExternalStorageDirectory() + "/.ASUS_QuickMemo/"; //TT636888 [Carol]
	public static final String COPY_TEMP_DIR = QUICK_MEMO_ROOT_DIR + "CropTempFolder/";
	public static final String COPY_FILENAME = "copied_items";
	public static final int MAX_ARRAY_SIZE = 2147483647;// 81920;
	
	// END: Better
	public static ArrayList<Long> lockBookIdList = new ArrayList<Long>();//wendy
	//begin darwin
	public static final int THIS_IS_LAST_EDIT = 1;
	public static final int NOT_LAST_EDIT = 0;
	//end  darwin
	
	//BEGIN: RICHARD
	public static final int INDEX_FILE_CREATE_NOT = 0;
	public static final int INDEX_FILE_CREATE_SUCESS = 1;
	public static final int INDEX_FILE_CREATE_FAILE = 2;
	public static final int INDEX_FILE_CREATE_LOCK = 3;
	public static final int INDEX_FILE_CREATE_RECREATE = 4;
	public static final int INDEX_FILE_DISABLE = 5;
	
	
	public static final String ANDROID_INTENT_ACTION_INDEXSERVICE_NEWINDEXFILE="android.intent.action.indexservice.newindexfile";
	public static final String ANDROID_INTENT_ACTION_INDEXSERVICE_DELETEINDEXFILE="android.intent.action.indexservice.deleteindexfile";
	public static final String ANDROID_INTENT_ACTION_INDEXSERVICE_MOVEPAGE="android.intent.action.indexservice.movepage";
	public static final String ANDROID_INTENT_ACTION_INDEXSERVICE_DELETEPAGE="android.intent.action.indexservice.deletepage";
	public static final int NOT_LOAD_CFG_RESOURCE = 0;
	public static final int ONLY_LOAD_CFG_RESOURCE = 1;
	public static final int LOAD_CFG_RESOURCE_AND_SNE = 2;
	public static int SEARCH_TEXT_HEIGH_LIGHT_COLOR = 0xFFFF6C00;//defalut
	//END: RICHARD
	
  //BEGIN:shaun_xu@asus.com
	public static int DPI = -1;
	public static int BASE_DPI = -1;
	//END:shaun_xu@asus.com
	
	//begin wendy allen++
	public static final int Template_type_normal = 0;
	public static final int Template_type_meeting = 1;
	public static final int Template_type_travel = 2;
	public static final int Template_type_todo = 3;
	public static final int Template_type_blank = 4;
	
	public static int Template_EditText_width = 1164 ;
	public static int Template_EditText_line_height = 50;
	public static int Template_EditText_paddingTop = 65;
	
	
	public static float Scale = 1;
	public static int INDEX_LANGUAGE_DEFAULT = -1;
	//end wendy allen++
	
	//BEGIN: RICHARD
	public static final Boolean IS_ENABLE_CONTINUES_MODE = false;
	//END: RICHARD
	
	//Begin Allen todo function switch
	public static final boolean IS_ENABLE_TODO_TEMPLATE = true;
	//End Allen

	//Begin Allen  
	/* indicate supernote data changed */
	public final static String ACTION_SUPERNOTE_UPDATE = "com.asus.supernote.action.SUPERNOTE_UPDATE";
	public final static String EXTRA_SUPERNOTE_UPDATE_FROM = "EXTRA_SUPERNOTE_UPDATE_FROM";
	
	/*indicate supernote update source*/
	public enum SuperNoteUpdateFrom{
		SUPERNOTE_UPDATE_FROM_DEFAULT,
		SUPERNOTE_UPDATE_FROM_ADD_BOOK,
		SUPERNOTE_UPDATE_FROM_DELETE_BOOK,
		SUPERNOTE_UPDATE_FROM_RENAME_BOOK,
		SUPERNOTE_UPDATE_FROM_LOCK_BOOK,
		SUPERNOTE_UPDATE_FROM_UNLOCK_BOOK,
		SUPERNOTE_UPDATE_FROM_ADD_PAGE,
		SUPERNOTE_UPDATE_FROM_DELETE_PAGE,
		SUPERNOTE_UPDATE_FROM_MODIFY_PAGE,
		SUPERNOTE_UPDATE_FROM_WIDGET_DATA_INVALID,
		SUPERNOTE_UPDATE_FROM_EDIT_CORVER
	}
	
	/* record update info */
	public static HashMap<SuperNoteUpdateFrom,Object> SuperNoteUpdateInfoSet = new HashMap<SuperNoteUpdateFrom, Object>();
	
	@SuppressWarnings("unchecked")
	public static void updateSuperNoteUpdateInfoSet(SuperNoteUpdateFrom updateFrom,Object value) {
		if(updateFrom == SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_BOOK || //value is deleted bookIds
				updateFrom == SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_LOCK_BOOK){  //value is locked bookIds
			ArrayList<Long> bookIds = null;
			if(!MetaData.SuperNoteUpdateInfoSet.containsKey(updateFrom)){
				bookIds = new ArrayList<Long>();
			}
			else{
				try{
					bookIds = (ArrayList<Long>) MetaData.SuperNoteUpdateInfoSet.get(updateFrom);
				}
				catch(ClassCastException e){
					e.printStackTrace();
					return;
				}
			}
			bookIds.add((Long)value);
			MetaData.SuperNoteUpdateInfoSet.put(updateFrom,bookIds);
		}
	}	
	
	/* indicate the activity is started by widget */
	public final static String START_FROM_WIDGET = "START_FROM_WIDGET";
	
	public static final String TODO_WIDGET_TABLE = "todo_widget";
	
	public static final class ToDoWidgetTable implements BaseColumns {
		public static final Uri uri = Uri.parse("content://" + AUTHORITY + "/" + TODO_WIDGET_TABLE);
		public static final String DEFAULT_SORT_ORDER = "modified DESC";
		//field
		public static final String ID="_id";
		public static final String WIDGET_ID="widget_id";
		public static final String ADAPTER_TYPE="adapter_type";
		public static final String BOOK_ID="book_id";
		public static final String SORT_BY="sort_by";
		
		//index od field
		public static final int INDEX_WIDGET_ID = 1;
		public static final int INDEX_ADAPTER_TYPE = 2;
		public static final int INDEX_BOOK_ID = 3;
		public static final int INDEX_SORT_BY = 4;
		//constructor
		ToDoWidgetTable()
		{}
	}
	
	//End Allen
	
	//begin:clare
	
	//widget Table data Fields
	public static final class WidgetTable implements BaseColumns {
		public static final Uri uri = Uri.parse("content://" + AUTHORITY + "/"
				+ WIDGET_TABLE);
		public static final String DEFAULT_SORT_ORDER = "modified DESC";
		//field
		public static final String ID="_id";
		public static final String WIDGET_ID="widget_id";
		public static final String WIDGET_MODE="widget_mode";
		
		//index od field
		public static final int INDEX_WIDGET_ID = 1;
		public static final int INDEX_WIDGET_MODE = 2;
		//constructor
		WidgetTable()
		{}
	}
	//widget item Table data Fields
		public static final class WidgetItemTable implements BaseColumns {
			public static final Uri uri = Uri.parse("content://" + AUTHORITY + "/"
					+ WIDGET_ITEM_TABLE);
			public static final String DEFAULT_SORT_ORDER = "modified DESC";
			//field
			public static final String ID="_id";
			public static final String ITEM_ID="item_id";
			public static final String ITEM_WIDGET_ID="item_widget_id";
			public static final String ITEM_ORDER_ID="item_order_id";
			public static final String ITEM_MODE="item_mode";
			public static final String ITEM_BOOK_ID="item_book_id";
			public static final String ITEM_PAGE_ID="item_page_id";
			public static final String ITEM_ALLBOOK_ALLTEMPLATE="item_allbook_alltemplate";
			public static final String ITEM_LOCK_STATE="item_lock_state";
			//index od field
			public static final int INDEX_ID = 0;
			public static final int INDEX_ITEM_ID = 1;
			public static final int INDEX_ITEM_WIDGET_ID = 2;
			public static final int INDEX_ITEM_ORDER_ID = 3;
			public static final int INDEX_ITEM_MODE = 4;
			public static final int INDEX_ITEM_BOOK_ID = 5;
			public static final int INDEX_ITEM_PAGE_ID = 6;
			public static final int INDEX_ITEM_ALLBOOK_ALLTEMPLATE = 7;
			public static final int INDEX_ITEM_LOCK_STATE = 8;
			//constructor
			WidgetItemTable()
			{}
		}
	//end:clare
	// BEGIN: Better
	public static boolean isATT(){ //Carol
		String model = SystemProperties.get("ro.product.model");
		String device = SystemProperties.get("ro.product.device");
		if(device!=null && model!=null){
			if(device.equalsIgnoreCase("ASUS-T00S")||model.equalsIgnoreCase("ASUS PadFone X mini") //PF450CL
					||device.equalsIgnoreCase("K00X")||model.equalsIgnoreCase("K00X")){ //ME375CL
			    return true;
			}
		}
		return false;
	}
	
	public static boolean isEnablePadOrPhoneChoose(){ //Smilefish
		String model = SystemProperties.get("ro.product.model");
		String device = SystemProperties.get("ro.product.device");
		if(device!=null && model!=null){
			if(device.equalsIgnoreCase("K01Q")||model.equalsIgnoreCase("K01Q")){ //FE375CL
			    return false;
			}
		}
		return true;
	}
	
	public static boolean isEnableTwoFingerDrag(){
		return false;
	}
	// END: Better
	
	// Book Table Data Fields
	public static final class BookTable implements BaseColumns {
		public static final Uri uri = Uri.parse("content://" + AUTHORITY + "/"
				+ BOOK_TABLE);
		public static final String DEFAULT_SORT_ORDER = "modified DESC";
		// Fields
		public static final String ID = "_id";
		public static final String TITLE = "title";
		public static final String CREATED_DATE = "created_date";
		public static final String MODIFIED_DATE = "modified_date";
		public static final String PAGE_COUNT = "page_counter";
		public static final String CURR_PAGE = "page_current";
		public static final String IS_LOCKED = "is_locked";
		public static final String THUMBNAIL = "thumbnail";
		public static final String PAGE_ORDER = "page_order";
		public static final String BOOK_SIZE = "book_size";
		public static final String BOOK_COLOR = "book_color";
		public static final String BOOK_GRID = "book_grid";
		public static final String IS_PHONE_MEMO = "is_phone_memo";
		public static final String FONT_SIZE = "font_size";

		// add by wendy begin++
		public static final String LASTSYNC_MODIFYTIME = "lastsync_modifytime";
		public static final String IS_DELETED = "is_deleted";
		public static final String USER_ACCOUNT = "userAccount";
		// add by wendy end---
		// BEGIN: Better
		public static final String VERSION = "version";
		// END: Better
		public static final String TEMPLATE = "template";//wendy allen++
		
		public static final String INDEX_LANGUAGE = "index_language";//Allen
		
		public static final String INDEX_COVER = "index_cover";//darwin
		public static final String COVER_MODIFYTIME = "cover_modifytime";//darwin
		// Index of Fields
		public static final int INDEX_TITLE = 1;
		public static final int INDEX_CREATED_DATE = 2;
		public static final int INDEX_MODIFIED_DATE = 3;
		public static final int INDEX_PAGE_COUNT = 4;
		public static final int INDEX_CURR_PAGE = 5;
		public static final int INDEX_IS_LOCKED = 6;
		public static final int INDEX_THUMBNAIL = 7;
		public static final int INDEX_PAGE_ORDER = 8;
		public static final int INDEX_BOOK_SIZE = 9;
		public static final int INDEX_BOOK_COLOR = 10;
		public static final int INDEX_BOOK_GRID = 11;
		public static final int INDEX_IS_PHONE_MEMO = 12;
		public static final int INDEX_FONT_SIZE = 13;
		// add by wendy begin ++
		public static final int INDEX_LASYSYNC_MODIFYTIME = 14;
		public static final int INDEX_IS_DELETED = 15;

		// add by wendy end---
		// BEGIN: Better
		public static final int INDEX_USER_ACCOUNT 	= 16;
		public static final int INDEX_VERSION 		= 17;
		// END: Better
		public static final int INDEX_TEMPLATE 		= 18;//wendy allen++
		
		public static final int INDEX_INDEX_LANGUAGE = 19;//Allen

		public static final int INDEX_INDEX_COVER = 20;//darwin
		public static final int INDEX_COVER_MODIFYTIME = 21;//darwin
		// Constructor
		private BookTable() {
			// Nothing
		}
	}

	// Clipboard Table Data Fields
	public static final class ClipboardTable implements BaseColumns {
		public static final Uri uri = Uri.parse("content://" + AUTHORITY + "/"
				+ CLIPBOARD_TABLE);
		public static final String DEFAULT_SORT_ORDER = "modified DESC";
		// Fields
		public static final String ID = "_id";
		public static final String DATA = "data";
		public static final int INDEX_ID = 0;

		// Index of Fields
		public static final int INDEX_DATA = 1;

		// Constructor
		private ClipboardTable() {
		}
	}

	// add by wendy begin webaccount table
	public static final class WebAccountTable implements BaseColumns {
		public static final Uri uri = Uri.parse("content://" + AUTHORITY + "/"
				+ WEBACCOUNT_TABLE);
		public static final String ID = "account_id";
		public static final String ACCOUNT_NAME = "account_name";
		public static final String ACCOUNT_SHA = "account_sha";
		public static final String ACCOUNT_PASSWORD = "account_password";

		public static final int INDEX_ID = 1;
		public static final int INDEX_ACCOUNT = 2;
		public static final int INDEX_SHA = 3;
		public static final int INDEX_PASSWORD = 4;
	}

	// add by wendy end
	//add by wendy begin
    
    public static final class TimestampTable implements BaseColumns{
    	public static final Uri uri 			= Uri.parse("content://" + AUTHORITY + "/" + TIMESTAMP_TABLE);
    	public static final String ID 			= "_id";
    	public static final String TITLE 		= "title";
    	public static final String OWNER 		= "owner";
    	public static final String CREATED_DATE = "create_date";
    	public static final String POSITION		= "position";
    	public static final String USER_ACCOUNT = "userAccount";//darwin
    	
    	public static final int INDEX_TITLE 		= 2;
    	public static final int INDEX_OWNER 		= 3;
    	public static final int INDEX_CREATE_DATE 	= 1;
    	public static final int INDEX_POSITION 		= 4;
    	public static final int INDEX_USER_ACCOUNT 	= 5;//darwin
    }
    
	    
    //Begin Add by Darwin_Yu@asus.com
    
    public static final String SEARCH_FILE = "search.index";
    
    public static final String DOODLE_TAG 	  = "Doodle";
    public static final String ITEM_TAG 	  = "Item";
    public static final String ATTACHMENT_TAG = "Attachment";
    
    public static final class DoodleTable implements BaseColumns{
    	public static final Uri uri 			= Uri.parse("content://" + AUTHORITY + "/" + DOODLE_TABLE);
    	public static final String INDEX					= "_index";
    	public static final String ID 						= "_id";
    	public static final String MODIFIED_DATE 			= "modified_date";
		public static final String LASTSYNC_MODIFYTIME 		= "lastsync_modifytime";
		public static final String IS_DELETE 				= "is_delete";
    	
    	public static final int INDEX_MODIFIED_DATE 			= 1;
    	public static final int INDEX_LASTSYNC_MODIFYTIME 		= 2;
    	public static final int INDEX_IS_DELETE 				= 3;
    	public static final int INDEX_ID 						= 4;
    	// BEGIN: Shane_Wang@asus.com 2012-12-10
    	public static final String LAST_SHA = "last_sha";
    	// END: Shane_Wang@asus.com 2012-12-10
    }
    
    public static final class ItemTable implements BaseColumns{
    	public static final Uri uri 			= Uri.parse("content://" + AUTHORITY + "/" + ITEM_TABLE);
    	public static final String INDEX					= "_index";
    	public static final String ID 						= "_id";
    	public static final String MODIFIED_DATE 			= "modified_date";
		public static final String LASTSYNC_MODIFYTIME 		= "lastsync_modifytime";
		public static final String IS_DELETE 				= "is_delete";
    	
    	public static final int INDEX_MODIFIED_DATE 			= 1;
    	public static final int INDEX_LASTSYNC_MODIFYTIME 		= 2;
    	public static final int INDEX_IS_DELETE 				= 3;
    	public static final int INDEX_ID 						= 4;
    	// BEGIN: Shane_Wang@asus.com 2012-12-10
    	public static final String LAST_SHA = "last_sha";
    	// END: Shane_Wang@asus.com 2012-12-10
    }
    
    public static final class AttachmentTable implements BaseColumns{
    	public static final Uri uri 			= Uri.parse("content://" + AUTHORITY + "/" + ATTACHMENT_TABLE);
    	public static final String INDEX					= "_index";
    	public static final String FILE_NAME                = "file_name";
    	public static final String ID 						= "_id";
    	public static final String MODIFIED_DATE 			= "modified_date";
		public static final String LASTSYNC_MODIFYTIME 		= "lastsync_modifytime";
		public static final String IS_DELETE 				= "is_delete";
    	
		public static final int INDEX_FILE_NAME                 = 1;
    	public static final int INDEX_MODIFIED_DATE 			= 2;
    	public static final int INDEX_LASTSYNC_MODIFYTIME 		= 3;
    	public static final int INDEX_IS_DELETE 				= 4;
    	public static final int INDEX_ID 						= 5;
    	// BEGIN: Shane_Wang@asus.com 2012-12-10
    	public static final String LAST_SHA = "last_sha";
    	// END: Shane_Wang@asus.com 2012-12-10
    }
    
    //End Add by Darwin_Yu@asus.com

    //add by wendy end

	// Page Table Data Fields
	public static final class PageTable implements BaseColumns {
		public static final Uri uri = Uri.parse("content://" + AUTHORITY + "/"
				+ PAGE_TABLE);

		public static final String DEFAULT_SORT_ORDER = "modified DESC";
		// Fields
		public static final String ID = "_id"; // 0
		public static final String OWNER = "owner"; // 1
		public static final String CONTENT = "content"; // 2
		public static final String CREATED_DATE = "created_date"; // 3
		public static final String MODIFIED_DATE = "modified_date"; // 4
		public static final String IS_BOOKMARK = "is_bookmark"; // 5
		public static final String IS_NOTIFIED = "is_notified"; // 6
		public static final String THUMBNAIL = "thumbnail"; // 7
		public static final String PAGE_SIZE = "page_size"; // 8
		public static final String COLOR = "color"; // 9
		public static final String STYLE = "style"; // 10

		// add by wendy begin++
		public static final String LASTSYNC_MODIFYTIME = "lastsync_modifytime";
		public static final String IS_DELETED = "is_deleted";
		public static final String LASTSYNC_OWNER = "lastsync_owner";
		public static final String USER_ACCOUNT = "userAccount";
		// BEGIN: Shane_Wang@asus.com 2012-12-10
    	public static final String LAST_SHA = "last_sha";
    	// END: Shane_Wang@asus.com 2012-12-10
		/**
		 * 
		 "lastsync_modifytime Long NOT NULL DEFAULT 0," +
		 * "is_deleted INTERGER NOT NULL DEFAULT 0," +
		 * "lastsync_owner long NOT NULL DEFAULT 0);";
		 */
		// add by wendy end--
		// BEGIN: Better
		public static final String VERSION = "version";
		// END: Better
		
	    public static final String TEMPLATE = "template_type";//wendy allen++
		
		//begin darwin
		public static final String IS_LAST_EDIT = "is_last_edit";
		//end  darwin
		
		//BEGIN: RICHARD
		public static final String IS_INDEXED = "is_indexed";
		public static final String INDEX_LANGUAGE = "index_language";
		//END: RICHARD
		
		// Index of Fields
		public static final int INDEX_OWNER = 1;
		public static final int INDEX_CONTENT = 2;
		public static final int INDEX_CREATED_DATE = 3;
		public static final int INDEX_MODIFIED_DATE = 4;
		public static final int INDEX_IS_BOOKMARK = 5;
		public static final int INDEX_IS_NOTIFIED = 6;
		public static final int INDEX_THUMBNAIL = 7;
		public static final int INDEX_PAGE_SIZE = 8;
		public static final int INDEX_COLOR = 9;
		public static final int INDEX_STYLE = 10;
		// add by wendy begin ++
		public static final int INDEX_LASYSYNC_MODIFYTIME = 11;
		public static final int INDEX_IS_DELETED = 12;
		public static final int INDEX_LASTSYNC_OWNER = 13;

		// add by wendy end---
		// BEGIN: Better
		public static final int INDEX_USER_ACCOUNT 	= 14;
		public static final int INDEX_VERSION 		= 15;
		// END: Better
		//begin darwin
		public static final int INDEX_IS_LAST_EDIT = 16;
		//end  darwin
		
		//BEGIN: RICHARD
		public static final int INDEX_IS_INDEXED = 17;
		//END: RICHARD

		public static final int INDEX_TEMPALTE = 18;//wendy allen++

		//BEGIN: RICHARD
		public static final int INDEX_INDEX_LANGUAGE = 19;
		//END: RICHARD
		
		// Constructor
		private PageTable() {
		}
	}

	public static final class MemoTable implements BaseColumns {
		public static final Uri uri = Uri.parse("content://" + AUTHORITY + "/"
				+ MEMOBROAD_TABLE);
		public static final String DEFAULT_SORT_ORDER = "modified DESC";
		// Fields
		public static final String ID = "_id";
		public static final String WIDGET_ID = "widget_id";
		public static final String BOOK_ID = "book_id";
		public static final String PAGE_ID = "page_id";
		public static final String MEMO_INFO = "memo_info";
		public static final String THUMBNAIL = "thumbnail";
		public static final String IS_HIDDEN = "is_hidden";
		public static final String BOOK_NAME = "book_name";
		// Index
		public static final int INDEX_WIDGET_ID = 1;
		public static final int INDEX_BOOK_ID = 2;
		public static final int INDEX_PAGE_ID = 3;
		public static final int INDEX_MEMO_INFO = 4;
		public static final int INDEX_THUMBNAIL = 5;
		public static final int INDEX_IS_HIDDEN = 6;
		public static final int INDEX_BOOK_NAME = 7;
	}

	// common used data
	public static final String PREFERENCE_NAME = "SuperNote";
	public static final String PREFERENCE_DIR = Environment.getDataDirectory()
			+ "/data/com.asus.supernote/shared_prefs";
	public static final String PREFERENCE_INPUT_MODE_KEY = "InputMode";
	public static final String NOTE_ITEM_PREFIX = "editor.info";
	
	//BEGIN: RICHARD
	public static final String NOTE_INDEX_PREFIX = "search.index";
	//END: RICHARD
	
	// BEGIN ryan_lin@asus.com, change file name
	@Deprecated
	public static final String NOTE_ITEM_PREFIX_DEPRECATED = "Items";
	// END ryan_lin@asus.com, change file name
	// BEGIN archie_huang@asus.com
	public static final String DOODLE_ITEM_PREFIX = "doodle.info";
	@Deprecated
	public static final String DOODLE_ITEM_PREFIX_LEGEND = "Doodle_Items";
	public static final String DOODLE_PREFIX = "Doodle";
	public static final String THUMBNAIL_PREFIX = "thumb.info";
	public static final String THUMBNAIL_COVER = "thumb_cover.info";//darwin
	public static final String THUMBNAIL_WIDGET = "thumb_widget.info";//darwin
	public static final String THUMBNAIL_COVER_CROP = "BookCover.jpg";//darwin
	public static final String AIRVIEW_PREFIX = "airview.info";//Allen
	@Deprecated
	public static final String THUMBNAIL_PREFIX_LEGEND = "Thumb";
	// END: archie_huang@asus.com
	public static final String MEMO_PREFIX = "Memo_";
	public static final String MEMO_EXTENSION = ".jpg";
	public static final String EXPORT_PREFIX = "Export";

	public static final String EXPORT_EXTENSION = ".sne";
	public static final String MUTLI_EXPORT_EXTENSION = ".snem";
	public static final String BACKUP_EXTENSION = ".snb";
	public static final String PAGES_INFOMATION = "pages.info";
	public static final String BOOK_INFORMATION = "books.info";
	public static final String BOOK_INFO_EXT = ".info";
	public static final String BOOK_LIST = "books.list";
	// BEGIN: Better
	public static final String EXPORT_PDF_EXTENSION = ".pdf";
	// END: Better

	// begin wendy
	public static final String BOOK_SHA_INFO = "pref_bookinfo_sha";
	// end wendy

	//darwin
	public static final int NOTEBOOK_DEFAULT_COVER_1_INDEX = 2;
	public static final int NOTEBOOK_DEFAULT_COVER_2_INDEX = 3;
	public static final int NOTEBOOK_DEFAULT_COVER_3_INDEX = 4;
	public static final int NOTEBOOK_DEFAULT_COVER_4_INDEX = 5;
	public static final int NOTEBOOK_DEFAULT_COVER_5_INDEX = 6;
	public static final int NOTEBOOK_DEFAULT_COVER_6_INDEX = 7;
	public static final int NOTEBOOK_DEFAULT_COVER_7_INDEX = 8;
	public static final int NOTEBOOK_DEFAULT_COVER_8_INDEX = 9;
	public static final int NOTEBOOK_DEFAULT_COVER_9_INDEX = 10;
	public static final int NOTEBOOK_DEFAULT_COVER_10_INDEX = 11;
	public static final int NOTEBOOK_DEFAULT_COVER_11_INDEX = 12;
	public static final int NOTEBOOK_DEFAULT_COVER_12_INDEX = 13;
	public static final int NOTEBOOK_DEFAULT_COVER_13_INDEX = 14;
	public static final int NOTEBOOK_DEFAULT_COVER_14_INDEX = 15;
	public static final int NOTEBOOK_DEFAULT_COVER_15_INDEX = 16;
	//darwin
	
	// BEGIN: Better
	public static final publicFinalStrings webStorage = new publicFinalStrings();
	public static final String SYNC_CUSTOM_DB_QUERY_TAG = "SyncFile&";
	public static final String SYNC_TEMP_DOWNLOAD_DIR = "Downloads_Temp/";
	public static final String SYNC_TEMP_UPLOAD_DIR = "Uploads_Temp/";
	public static final String SYNC_PAGE_FILE_EXTENSION = ".file";
	public static final String SYNC_TABLE_INFO_FILE_NAME = "book.info";
	public static final String SYNC_SERVER_ROOT_DIR_NAME = "SuperNoteCloudSync";
	public static final String SYNC_FILE_VERSION = "FileVersion";
	public static final String SYNC_PAGE_FILE_ATTR_SHA = "FileSHA";
	public static final String SYNC_PAGE_FILE_ATTR_LASTCHANGETIME = "LastChangeTime";
	public static final String SYNC_PAGE_FILE_ATTR_ISDELETED = "PageIsDelete";
	public static final String SYNC_PAGE_FILE_ATTR_LASTNOTEBOOKID = "LastNoteBookID";
	public static final String SYNC_PAGE_FILE_ATTR_NOTEBOOKTITLE = "NoteBookTitle";
	public static final String SYNC_PAGE_FILE_ATTR_ISBOOKMARK = "IsBookmark";
	public static final String SYNC_PAGE_FILE_ATTR_DEFAULTBACKGROUND = "DefaultBackground";
	public static final String SYNC_PAGE_FILE_ATTR_DEFAULTLINE = "DefaultLine";
	public static final String SYNC_PAGE_FILE_ATTR_CATEGORY = "Category";
	
	//begin darwin
	public static final String SYNC_PAGE_FILE_ATTR_TEMPLATE = "Template";
	//end   darwin
	
	//BEGIN: RICHARD
	public static final String SYNC_PAGE_FILE_ATTR_INDEXLANGUAGE = "IndexLanguage";
	//END: RICHARD
	
	// END: Better

	public static final String PREFERENCE_DOODLE_COLOR[] = { "DoodleColor1",
			"DoodleColor2", "DoodleColor3", "DoodleColor4", "DoodleColor5",
			"DoodleColor6", "DoodleColor7", "DoodleColor8", "DoodleColor9",
			"DoodleColor10" };
	public static final String PREFERENCE_DOODLE_STROKE = "DoodleStroke";
	public static final String PREFERENCE_DOODLE_ERASER_WIDTH = "DoodleEraser";
	public static final String PREFERENCE_DOODLE_BRUSH = "DoodleBrush";
	public static final String PREFERENCE_DOODLE_SEL_COLOR_INDEX = "DoodleColorIndex";
	public static final String PREFERENCE_DOODLE_ALPHA = "DoodleAlpha"; // Better
	public static final String PREFERENCE_PEN_COLOR[] = { "PenColor1",
		"PenColor2", "PenColor3", "PenColor4", "PenColor5", "PenColor6", 
		"PenColor7", "PenColor8", "PenColor9", "PenColor10", "PenColor11", "PenColor12"};  //smilefish
	public static final String PREFERENCE_IS_PALETTE = "penpalette"; // By Show
	public static final String PREFERENCE_PALETTE_COLOR = "palettecolor"; // By Show
	public static final String PREFERENCE_PALETTE_COLORX = "palttecurrentx"; // By Show
	public static final String PREFERENCE_PALETTE_COLORY = "palttecurrenty"; // By Show
	public static final String PREFERENCE_IS_COLOR_MODE= "colormode"; // By Show
	public static final String PREFERENCE_IS_CUSTOM_COLOR_SET= "customcolorset"; // By Smilefish

	//BEGIN: RICHARD
	public static final String PREFERENCE_EDITOR_TEXT_WT = "AutoChangeHeadWriteToType";
	public static final String PREFERENCE_DOODLE_SHAPE = "AutoRecognizeShape";
	//END: RICHARD
	
	// BEGIN ryan_lin@asus.com, to remember color and style.
	public static final String PREFERENCE_EDITOR_TEXT_STYLE = "EditorTextStyle";
	public static final String PREFERENCE_EDITOR_TEXT_COLOR = "EditorTextColor";
	// BEGIN ryan_lin@asus.com
	
	//begin smilefish
	public static final String PREFERENCE_MEMO_EDIT_INDEX = "MemoEditIndex";
	public static final String PREFERENCE_MEMO_CURSOR_INDEX = "MemoCursorIndex";
	//end smilefish

	//begin wendy
	public static final String HIDE_DIR = Environment.getExternalStorageDirectory()
			+ "/.AsusSuperNote/";
	public static final boolean IS_HIDE_DATA = true;
	//end wendy
	
	public static final String SYSTEM_DIR = Environment
			.getExternalStorageDirectory().toString();
	public static final String DIR = Environment.getExternalStorageDirectory()
			+ "/AsusSuperNote/";
	public static final String DATA_DIR = (IS_HIDE_DATA? HIDE_DIR :DIR) + "Data/";//wendy
	public static final String SHARE_DIR = DIR + "Share/";
	public static final String EXPORT_DIR = DIR + "Export/";
	public static final String BACKUP_DIR = DIR + "Backup/";
	public static final String MEMO_DIR = DIR + "Memos/";
	public static final String CROP_TEMP_DIR = DIR + "CropTempFolder/";//emmanual
	public static final String TEMP_DIR = (IS_HIDE_DATA ? HIDE_DIR : DIR)
			+ "temp/";// noah
	//begin jason
	public static final String PICTURES_DIR=SYSTEM_DIR+"/Pictures/";
	//end jason
	// BEGIN: Better
	public static final String USER_TO_UPGRAGE_LIST_DIR = DATA_DIR + ".UserToUpgradeList/";
	// END: Better
	
    // BEGIN: Better
    public static final String EXPORT_PDF_DIR = EXPORT_DIR;
    public static final String CLIPBOARD_TEMP_DIR = DIR + "clipboard/";
    public static final String THUMBNAIL_TEMP_DIR = DATA_DIR + ".thumb_tmp/";
    // END: Better
	public static final int DOODLE_QUALITY = 100;
	public static final int THUMBNAIL_QUALITY = 100;
	public static final String BOOK_ID = "BookId";
	public static final String BOOK_ID_LIST = "BookIdList";//emmanual
	public static final String PAGE_ID = "PageId";
	public static final String MEMO_ID = "memo_id";
	
	//begin jason
	public static final String SELECT_MODE="SELECT_MODE";
	public static final String INSTANT_SHARE="INSTANT_SHARE";
	public static final String INSTANT_DOODLEITEM_PATH_STRING="INSTANT_SHARE_PAGEID";
	//end jason
	//begin darwin
	public static final String TEMPLATE_BOOK_NEW = "template_book_new";
	//end  darwin
	// BEGIN: Better
	public static final String IS_ASYNC = "is_async";
	public static final String IS_NEW_PAGE = "is_new_page";
	// END: Better
    public static final String  TIMESTAMP_POS 					  = "timestamp_pos";//wendy
	// BEGIN: archie_huang@asus.com
	public static final String MEMO_INFO = "memo.info";
	// END: archie_huang@asus.com

	public static final String PHONE_WHO = "phone_who";
	public static final String PHONE_TIME = "phone_time";
	public static final String PHONE_MEMO_ACTION = "com.asus.supernote.PHONE_MEMO_ACTION";
	public static final int PHONE_BOOK_NAME = R.string.phone_book_name;

	public static final int BOOK_ID_KEY = 0;
	public static final int PAGE_ID_KEY = 1;
	
	// BEGIN�� Shane_Wang@asus.com 2012-11-19
	public static final String IMPORT_FILE_PATH = "filePath";
	public static final int MY_BIT_CAST_NAME = R.string.my_bit_cast_name;
	public static final String IMPORT_NOTE_ACTION = "com.asus.supernote.ImportNote";
	// END�� Shane_Wang@asus.com 2012-11-19

	// BEGIN: archie_huang@asus.com
	public static final int HINT_TIMEOUT = 3500;
	// END: archie_huang@asus.com

	// provider data
	public static final String AUTHORITY = "com.asus.supernote.NoteProvider";
	public static final String DATABASE_NAME = "note.db";
	public static final String BOOK_TABLE = "Book";
	public static final String PAGE_TABLE = "Page";
	public static final String CLIPBOARD_TABLE = "Clipboard";
	public static final String MEMOBROAD_TABLE = "Memoboard";
	public static final String NO_DATA = "NO_DATA";
	public static final String WEBACCOUNT_TABLE = "WebAccount";// add by wendy
																// webstorage-account
	public static final String  TIMESTAMP_TABLE 				  = "Timestamp";//ADD BY WENDY
	
	 //begin:clare
	public static final String WIDGET_TABLE = "Widget";
	public static final String WIDGET_ITEM_TABLE ="Widget_Item";
    //end:clare
    
	public static final int DATABASE_VERSION = 1;
	//Begin Add by Darwin_Yu@asus.com
	
	public static String LAST_SYNC_TIME = "";
	public static String THIS_SYNC_TIME = "";
	
	public static final String DOODLE_TABLE 				  = "Doodle";
	public static final String ITEM_TABLE 				  	  = "Item";
	public static final String ATTACHMENT_TABLE 			  = "Attachment";
	public static final String SYNC_DOODLE_FILE_EXTENSION	  = ".Doodle";
	public static final String SYNC_ITEM_FILE_EXTENSION		  = ".Item";
	public static final String SYNC_ATTACHMENT_FILE_EXTENSION = ".Attachment";
	
	public static final String SYNC_ATTR_FILE_VERSION 		  = "FileVersion";
	public static final String SYNC_ATTR_SHA 				  = "FileSHA";
	public static final String SYNC_ATTR_LASTCHANGETIME 	  = "LastChangeTime";
	public static final String SYNC_DOODLE_ATTR_ISDELETED 	  = "DoodleIsDelete";
	public static final String SYNC_ITEM_ATTR_ISDELETED 	  = "ItemIsDelete";
	public static final String SYNC_ATTACHMENT_ATTR_ISDELETED = "AttachmentIsDelete";
	
	
	//End   Add by Darwin_Yu@asus.com
	// Doodle
	public static final float[] DOODLE_PAINT_WIDTHS = { 3, 6, 10, 14, 18, 22,
			26, 30 };
	//begin jason src =1
	public static final int DOODLE_DEFAULT_PAINT_WIDTH = 2;
	//end jason
	public static final float[] DOODLE_ERASER_WIDTHS = { 15, 30, 45 };
	public static final int DOODLE_DEFAULT_ERASER_WIDTHS = 0;

	// Scribble
	public static final float SCRIBBLE_PAINT_WIDTHS_NORMAL = 1.5f;
	public static final float SCRIBBLE_PAINT_WIDTHS_BOLD = 2.2f;

	// Notebook
	public static final int BOOK_FONT_SMALL = 0;
	public static final int BOOK_FONT_NORMAL = 1;
	public static final int BOOK_FONT_BIG = 2;

	public static final int BOOK_COLOR_YELLOW = 0xFFFFF9AF;
	public static final int BOOK_COLOR_BLUE = 0xFFD4F8F8;
	public static final int BOOK_COLOR_WHITE = 0xFFFFFFFF;

	public static final int BOOK_GRID_LINE = 0;
	public static final int BOOK_GRID_GRID = 1;
	public static final int BOOK_GRID_BLANK = 2;
	
	// NotePage
	public static final int PAGE_FONT_SIZE_SAME_AS_BOOK = -1;
	public static final int PAGE_COLOR_SAME_AS_BOOK = -1;
	public static final int PAGE_STYLE_SAME_AS_BOOK = -2;

	public static final int TYPE_STRING = 0;
	public static final int TYPE_BOOLEAN = 1;
	public static final int TYPE_INTEGER = 2;
	public static final int TYPE_LONG = 3;
	public static final int TYPE_FLOAT = 4;

	public static final int PAGE_SIZE_SAME_AS_BOOK = 0;
	public static final int PAGE_SIZE_PAD = 1;
	public static final int PAGE_SIZE_PHONE = 2;

	// SNE version
	public static final String VERSION = "version";
	public static final int UNKNOWN_VERSION = 0;
	public static final int SNE_VERSION_0302 = 3;
	public static final int SNE_VERSION_3 = 0x0303;

	// Thumb size
	public static final int THUMB_WIDTH = 206;
	public static final int THUMB_HEIGHT = 278;

	public static final String WIDGET_INIT = "WIDGET_INIT";
	public static final String DEFAULT_PAD_BOOK = "DEFAULT_PAD_BOOK";
	public static final String DEFAULT_PHONE_BOOK = "DEFAULT_PHONE_BOOK";
	
	// Setting
	public static final int[] INPUT_RECOGNITION_TIMES = { 180, 400, 800 };
	
	//begin darwin
	public static int INDEX_CURRENT_LANGUAGE = -1;
	//end   darwin
	public static final int INDEX_LANGUAGE_EN_US = 0;
	public static final int INDEX_LANGUAGE_ZH_CN = 1;
	public static final int INDEX_LANGUAGE_ZH_TW = 2;
	public static final int INDEX_LANGUAGE_ZH_CN_NO_EN = 3;
	public static final int INDEX_LANGUAGE_ZH_TW_NO_EN = 4;
	public static final int INDEX_LANGUAGE_AR = 5;
	public static final int INDEX_LANGUAGE_CS_CZ = 6;
	public static final int INDEX_LANGUAGE_DA_DK = 7;
	public static final int INDEX_LANGUAGE_DE_DE = 8;
	public static final int INDEX_LANGUAGE_EL_GR = 9;
	public static final int INDEX_LANGUAGE_EN_CA = 10;
	public static final int INDEX_LANGUAGE_EN_GB = 11;
	public static final int INDEX_LANGUAGE_ES_ES = 12;
	public static final int INDEX_LANGUAGE_FI_FI = 13;
	public static final int INDEX_LANGUAGE_FR_CA = 14;
	public static final int INDEX_LANGUAGE_FR_FR = 15;
	public static final int INDEX_LANGUAGE_HE_IL = 16;
	public static final int INDEX_LANGUAGE_HU_HU = 17;
	public static final int INDEX_LANGUAGE_IT_IT = 18;
	public static final int INDEX_LANGUAGE_KO_KR = 19;
	public static final int INDEX_LANGUAGE_NL_NL = 20;
	public static final int INDEX_LANGUAGE_NO_NO = 21;
	public static final int INDEX_LANGUAGE_PL_PL = 22;
	public static final int INDEX_LANGUAGE_PT_BR = 23;
	public static final int INDEX_LANGUAGE_PT_PT = 24;
	public static final int INDEX_LANGUAGE_RU_RU = 25;
	public static final int INDEX_LANGUAGE_SV_SE = 26;
	public static final int INDEX_LANGUAGE_TR_TR = 27;
	
	
	
	//This array is information about INDEX_LANGUAGES.
	public static final int INDEX_LANGUAGE_HAVE_EN = 1;
	public static final int INDEX_LANGUAGE_NO_EN = 0;
	public static final int[] INDEX_LANGUAGES_EN_OR_NOT = { 	
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_EN_US
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_ZH_CN
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_ZH_TW
		INDEX_LANGUAGE_NO_EN,//INDEX_LANGUAGE_ZH_CN_NO_EN
		INDEX_LANGUAGE_NO_EN, //INDEX_LANGUAGE_ZH_TW_NO_EN
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_AR = 5
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_CS_CZ = 6;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_DA_DK = 7;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_DE_DE = 8;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_EL_GR = 9;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_EN_CA = 10;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_EN_GB = 11;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_ES_ES = 12;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_FI_FI = 13;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_FR_CA = 14;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_FR_FR = 15;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_HE_IL = 16;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_HU_HU = 17;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_IT_IT = 18;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_KO_KR = 19;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_NL_NL = 20;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_NO_NO = 21;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_PL_PL = 22;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_PT_BR = 23;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_PT_PT = 24;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_RU_RU = 25;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_SV_SE = 26;
		INDEX_LANGUAGE_HAVE_EN,//INDEX_LANGUAGE_TR_TR = 27;
		};
	
	public static final String[] INDEX_LANGUAGES_LANGUAGE = {
		"en_US",//INDEX_LANGUAGE_EN_US = 0
		"zh_CN",//INDEX_LANGUAGE_ZH_CN = 1
		"zh_TW",//INDEX_LANGUAGE_ZH_TW = 2
		"zh_CN",//INDEX_LANGUAGE_ZH_CN_NO_EN = 3
		"zh_TW", //INDEX_LANGUAGE_ZH_TW_NO_EN = 4
		"ar",//INDEX_LANGUAGE_AR = 5
		"cs_CZ",//INDEX_LANGUAGE_CS_CZ = 6;
		"da_DK",//INDEX_LANGUAGE_DA_DK = 7;
		"de_DE",//INDEX_LANGUAGE_DE_DE = 8;
		"el_GR",//INDEX_LANGUAGE_EL_GR = 9;
		"en_CA",//INDEX_LANGUAGE_EN_CA = 10;
		"en_GB",//INDEX_LANGUAGE_EN_GB = 11;
		"es_ES",//INDEX_LANGUAGE_ES_ES = 12;
		"fi_FI",//INDEX_LANGUAGE_FI_FI = 13;
		"fr_CA",//INDEX_LANGUAGE_FR_CA = 14;
		"fr_FR",//INDEX_LANGUAGE_FR_FR = 15;
		"he_IL",//INDEX_LANGUAGE_HE_IL = 16;
		"hu_HU",//INDEX_LANGUAGE_HU_HU = 17;
		"it_IT",//INDEX_LANGUAGE_IT_IT = 18;
		"ko_KR",//INDEX_LANGUAGE_KO_KR = 19;
		"nl_NL",//INDEX_LANGUAGE_NL_NL = 20;
		"no_NO",//INDEX_LANGUAGE_NO_NO = 21;
		"pl_PL",//INDEX_LANGUAGE_PL_PL = 22;
		"pt_BR",//INDEX_LANGUAGE_PT_BR = 23;
		"pt_PT",//INDEX_LANGUAGE_PT_PT = 24;
		"ru_RU",//INDEX_LANGUAGE_RU_RU = 25;
		"sv_SE",//INDEX_LANGUAGE_SV_SE = 26;
		"tr_TR",//INDEX_LANGUAGE_TR_TR = 27;
	};
	
	public static final String[] INDEX_LANGUAGES_CODE_SET_SUFFIX = {
		"",//INDEX_LANGUAGE_EN_US
		"_gb2312",//INDEX_LANGUAGE_ZH_CN
		"_big5",//INDEX_LANGUAGE_ZH_TW
		"_gb2312",//INDEX_LANGUAGE_ZH_CN_NO_EN
		"_big5", //INDEX_LANGUAGE_ZH_TW_NO_EN
		"",//INDEX_LANGUAGE_AR = 5
		"",//INDEX_LANGUAGE_CS_CZ = 6;
		"",//INDEX_LANGUAGE_DA_DK = 7;
		"",//INDEX_LANGUAGE_DE_DE = 8;
		"",//INDEX_LANGUAGE_EL_GR = 9;
		"",//INDEX_LANGUAGE_EN_CA = 10;
		"",//INDEX_LANGUAGE_EN_GB = 11;
		"",//INDEX_LANGUAGE_ES_ES = 12;
		"",//INDEX_LANGUAGE_FI_FI = 13;
		"",//INDEX_LANGUAGE_FR_CA = 14;
		"",//INDEX_LANGUAGE_FR_FR = 15;
		"",//INDEX_LANGUAGE_HE_IL = 16;
		"",//INDEX_LANGUAGE_HU_HU = 17;
		"",//INDEX_LANGUAGE_IT_IT = 18;
		"_wansung",//INDEX_LANGUAGE_KO_KR = 19;
		"",//INDEX_LANGUAGE_NL_NL = 20;
		"",//INDEX_LANGUAGE_NO_NO = 21;
		"",//INDEX_LANGUAGE_PL_PL = 22;
		"",//INDEX_LANGUAGE_PT_BR = 23;
		"",//INDEX_LANGUAGE_PT_PT = 24;
		"",//INDEX_LANGUAGE_RU_RU = 25;
		"",//INDEX_LANGUAGE_SV_SE = 26;
		"",//INDEX_LANGUAGE_TR_TR = 27;
	};
	
	//this array is bind to ui list. R.array.not_translate_index_language_values
	public static final int[] INDEX_LANGUAGES = { 	
		INDEX_LANGUAGE_EN_US,
		INDEX_LANGUAGE_ZH_CN,
		INDEX_LANGUAGE_ZH_TW,
		//INDEX_LANGUAGE_ZH_CN_NO_EN,
		//INDEX_LANGUAGE_ZH_TW_NO_EN,
		INDEX_LANGUAGE_AR,
		INDEX_LANGUAGE_CS_CZ,
		INDEX_LANGUAGE_DA_DK,
		INDEX_LANGUAGE_DE_DE,
		INDEX_LANGUAGE_EL_GR,
		INDEX_LANGUAGE_EN_CA,
		INDEX_LANGUAGE_EN_GB,
		INDEX_LANGUAGE_ES_ES,
		INDEX_LANGUAGE_FI_FI,
		INDEX_LANGUAGE_FR_CA,
		INDEX_LANGUAGE_FR_FR,
		INDEX_LANGUAGE_HE_IL,
		INDEX_LANGUAGE_HU_HU,
		INDEX_LANGUAGE_IT_IT,
		INDEX_LANGUAGE_KO_KR,
		INDEX_LANGUAGE_NL_NL,
		INDEX_LANGUAGE_NO_NO,
		INDEX_LANGUAGE_PL_PL,
		INDEX_LANGUAGE_PT_BR,
		INDEX_LANGUAGE_PT_PT,
		INDEX_LANGUAGE_RU_RU,
		INDEX_LANGUAGE_SV_SE,
		INDEX_LANGUAGE_TR_TR,};
	//begin noah
	public static final int[] Index_Languages_Limited = {
		INDEX_LANGUAGE_EN_US,
		INDEX_LANGUAGE_ZH_TW,
	};
	public static final boolean Switch_VO_Languages_Limited = true;
	//end noah
	public static final String PREFERENCE_CURRENT_INPUT_MODE = "CurrentInputMode"; //by Show
	public static ArrayList<Integer> CurrentDrawList = new ArrayList<Integer>();//by Show
	public static final String PREFERENCE_SELECTION_TEXT_START = "selectiontextStart";//by Show
	public static final String PREFERENCE_SELECTION_TEXT_END = "selectiontextEnd";//by Show
	public static final String PREFERENCE_IS_TEXTIMG_SELECTION_TEXT = "isSelectiontext";//by Show
	public static final String PREFERENCE_TEXTIMG_MODE_SELECTION_TEXT = "SelectiontextMode";//by Show
	//Begin: show_wang@asus.com
	
	//begin wendy handwrite_pressure
	public static final boolean HandWriting_HasPressure = false; //smilefish
	//end  wendy
	
	//begin Jason
	public static final boolean Switch_Stylus_Only =true;
	public static final String STYLUS_ONLY_STATUS="stylusonlystatus";
	//end Jason
	
	public static int WIDGET_ONE_COUNT = 0; //Carol
	public static boolean IS_LOAD = false;
	
	//begin Dave
	public static boolean CHANGE_HANDWRITE_BACKGROUND = false;
	public static boolean ENABLE_SELECTOR_HANDLER = true;
	//end Dave
	
	// --- Carrot: temporary add for limition of brush width ---
	public static final String TEMP_PENCIL_WIDTH = "PencilWidth";
	public static final String TEMP_ROLLER_WIDTH = "RollerPenWidth";
	public static final String TEMP_PEN_WIDTH = "PenWidth";
	public static final String TEMP_MARKER_WIDTH = "MarkerWidth";
	public static final String TEMP_BRUSH_WIDTH = "BrushWidth";
	public static final String TEMP_AIRBRUSH_WIDTH = "AirBrushWidth";
	// --- Carrot: temporary add for limition of brush width ---
	// Carrot: individually set color of every brush
	public static final String TEMP_PENCIL_COLOR = "PencilColor";
	public static final String TEMP_ROLLER_COLOR = "RollerPenColor";
	public static final String TEMP_PEN_COLOR = "PenColor";
	public static final String TEMP_MARKER_COLOR = "MarkerColor";
	public static final String TEMP_BRUSH_COLOR = "BrushColor";
	public static final String TEMP_AIRBRUSH_COLOR = "AirBrushColor";
	public static final String TEMP_PENCIL_COLOR_INDEX = "PencilColorIndex";
	public static final String TEMP_ROLLER_COLOR_INDEX = "RollerPenColorIndex";
	public static final String TEMP_PEN_COLOR_INDEX = "PenColorIndex";
	public static final String TEMP_MARKER_COLOR_INDEX = "MarkerColorIndex";
	public static final String TEMP_BRUSH_COLOR_INDEX = "BrushColorIndex";
	public static final String TEMP_AIRBRUSH_COLOR_INDEX = "AirBrushColorIndex";
	public static final String TEMP_PENCIL_COLOR_X = "PencilColorX";
	public static final String TEMP_ROLLER_COLOR_X = "RollerPenColorX";
	public static final String TEMP_PEN_COLOR_X = "PenColorX";
	public static final String TEMP_MARKER_COLOR_X = "MarkerColorX";
	public static final String TEMP_BRUSH_COLOR_X = "BrushColorX";
	public static final String TEMP_AIRBRUSH_COLOR_X = "AirBrushColorX";
	public static final String TEMP_PENCIL_COLOR_Y = "PencilColorY";
	public static final String TEMP_ROLLER_COLOR_Y = "RollerPenColorY";
	public static final String TEMP_PEN_COLOR_Y = "PenColorY";
	public static final String TEMP_MARKER_COLOR_Y = "MarkerColorY";
	public static final String TEMP_BRUSH_COLOR_Y = "BrushColorY";
	public static final String TEMP_AIRBRUSH_COLOR_Y = "AirBrushColorY";
	// Carrot: individually set color of every brush
	
	public static final boolean Switch_DATA_COPY =true;//by noah
	public static final String IMPORT_INTENT = "Import_Intent"; //Dave 
	public static final boolean SWITCH_IMPORT_BOOKS = true;//true:导入apple_tart_recipe.sne,meeting_record.sne等书籍
	
	public static HashMap<Long,Bitmap> mCacheCoverThumbnailList = new HashMap<Long,Bitmap>(); //Carol
    public static boolean READ_ONLY_MODE = false; //Carol
    
    public static boolean IS_GA_ON = true; //Dave
    public static boolean INSERT_PHOTO_SELECTION = false; //Carol
    public static int TODO_WIDGET_WIDTH_SIZE = 0; //Carol
    
    public static boolean mAddPageFromPhoneRepeat = false; //Emmanual
    public final static String STAY_NOTEBOOKPICKER_ACTIVITY = "STAY NOTEBOOKPICKER ATIVITY"; //Carol
    public final static String DISPLAY_ADDBOOK_DIALOG = "DISPLAY ADDBOOK DIALOG"; //Carol
    public static boolean ADD_BOOK_FROM_WIDGET = false; //Carol
    
    public static boolean IS_KEYBOARD_SHOW = false; //Emmanual
    public static boolean IS_TEXTIMAGE_CONFIG = false; //Emmanual
    public static final String ACTION_READ_LATER = "com.asus.supernote.ACTION_READ_LATER"; //Smilefish
    public static boolean IS_ENABLE_WIDGET_THUMBNAIL = false; //smilefish
    public static boolean AUTO_ADD_PAGE = true; //emmanual
    public static boolean TEXT_SEARCH_ENABLE = true; //emmanual
    public static boolean IS_AUTO_PAGING = false; //emmanual
    public static boolean PAUSE_AUTO_PAGING = false; //emmanual
    public static boolean IS_PASTE_OR_SHARE = false; //emmanual
    public static boolean INSERT_BROWSER_SHARE = false; //emmanual
    
	public static boolean isSDCardFull() {
		File path = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(path.getPath());
		long free = sf.getFreeBytes();
		long all = sf.getTotalBytes();
		return 1.0 * free / all < 0.01;
	}

	private static Toast fullToast;
	public static void showFullNoAddToast(Context context) {
		if (fullToast == null)
			fullToast = Toast.makeText(context, R.string.sdcard_full_no_add,
			        Toast.LENGTH_SHORT);
		fullToast.show();
	}

	private static Toast nosaveToast;
	public static void showFullNoSaveToast(Context context) {
		if (nosaveToast == null)
			nosaveToast = Toast.makeText(context, R.string.sdcard_full_no_save,
			        Toast.LENGTH_SHORT);
		nosaveToast.show();
	}
	
	public static String DO_IT_LATER_PACKAGENAME = "com.asus.task"; //Carol
	public static ArrayList<Long> CoverChangedBookIdList = new ArrayList<Long>(); //record books with cover changed
	
	public static void checkRTL(EditText edit) {
		if (isRTL()) {
			switch (edit.getId()) {
			case R.id.editText:
			case R.id.BoxEditText:
				edit.setGravity(Gravity.END);
				break;
			case R.id.topic_edit:
			case R.id.attendee_edit:
			case R.id.travel_date_edit:
			case R.id.travel_title_edit:
				edit.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
				break;
			default:
				break;
			}
		}
	}
	
	public static boolean isRTL() {
		int directionality = Character.getDirectionality(Locale.getDefault()
		        .getDisplayName().charAt(0));
		if (directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT
		        || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC) {
			return true;
		} else {
			return false;
		}
	}
	
	public static HashMap<Long,Long> Changed_Book_List = new HashMap<Long, Long>(); //Carol
	public static HashMap<Long, Long> Changed_Page_List = new HashMap<Long, Long>(); //Carol
	public static Long WIDGET_BOOK_ID = 0L;
	public static boolean IS_STATUS_CHANGED = false;
	
	public static String LAST_EDITED_BOOK_INDEX = "last_edited_book_index"; //Emmanual
	
	//Emmanual: Check Network Connection
	public static boolean isNetworkAvailable(Context context) {
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		ConnectivityManager connectivityManager = (ConnectivityManager) context
		        .getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager == null) {
			return false;
		} else {
			// 获取NetworkInfo对象
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

			if (networkInfo != null && networkInfo.length > 0) {
				for (int i = 0; i < networkInfo.length; i++) {
					// 判断当前网络状态是否为连接状态
					if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
