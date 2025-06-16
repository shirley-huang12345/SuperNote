package com.asus.supernote.fota;

public final class V1DataFormat extends DataFormat {

	// directories and files
	public static final String DIR_NAME 			= "V1/";
	public static final String DIR 					= FOTA_ROOT_DIR 
			+ DIR_NAME;
	public static final String DATA_DIR_NAME 		= "supernote/";
	public static final String DATA_DIR 			= DIR + DATA_DIR_NAME;

	public static final String PREFERENCE_FILE_NAME = "pref";
	public static final String BOOKLIST_FILE_NAME 	= "nbs.fn";
	public static final String BOOKINFO_FILE_NAME 	= "mf.fn";
	public static final String PAGELIST_FILE_NAME 	= "idx.fn";
	
	// preference
	public static final int PREF_BEGIN 						= 0x0000;
	public static final int PREF_DOODLE_STYLE				= 0x0001;
	public static final int PREF_DOODLE_SIZE				= 0x0002;
	public static final int PREF_ERASER_SIZE 				= 0x0003;
	public static final int PREF_SCRIBBLE_SIZE				= 0x0004;
	public static final int PREF_ANNOTATION_STYLE			= 0x0005;
	public static final int PREF_ANNOTATION_SIZE			= 0x0006;
	public static final int PREF_ANNOTATION_ERASER_SIZE		= 0x0007;
	public static final int PREF_CHARACTER_DISTANCE			= 0x0008;
	public static final int PREF_STOP_TIME					= 0x0009;
	public static final int PREF_FLAG						= 0x000A;
	public static final int PREF_DOODLE_COLOR				= 0x000B;
	public static final int PREF_SCRIBBLE_COLOR				= 0x000C;
	public static final int PREF_ANNOTATION_COLOR			= 0x000D;
	public static final int PREF_VERSION					= 0x000E;
	public static final int PREF_PASSWORD_LEN				= 0x000F;
	public static final int PREF_COLORS						= 0x0010;
	public static final int PREF_COLOR_PANEL_X				= 0x0011;
	public static final int PREF_COLOR_PANEL_Y				= 0x0012;
	public static final int PREF_CURRENT_BOOK_ID			= 0x0013;
	public static final int PREF_CURRENT_PAGE_ID			= 0x0014;
	public static final int PREF_PASSWORD					= 0x0015;
	public static final int PREF_END 						= 0x00FF;
	
	// book list
	public static final int NBS_BEGIN 						= 0x0100;
	public static final int NBS_VERSION 					= 0x0101;
	public static final int NBS_BOOK_NUM 					= 0x0102;
	public static final int NBS_BOOK_VERSION 				= 0x0103;
	public static final int NBS_BOOK_FONT_SIZE				= 0x0104;
	public static final int NBS_BOOK_BAK_COLOR				= 0x0105;
	public static final int NBS_BOOK_ID						= 0x0106;
	public static final int NBS_BOOK_NAME_LEN				= 0x0107;
	public static final int NBS_BOOK_TYPE 					= 0x0108;
	public static final int NBS_BOOK_NAME 					= 0x0109;
	public static final int NBS_END 						= 0x01FF;

	// index data
	public static final int INDEX_BEGIN 					= 0x1000;
	public static final int INDEX_VERSION 					= 0x1001;
	public static final int INDEX_PAGE_NUM 					= 0x1002;
	public static final int INDEX_PAGE_VERSION 				= 0x1003;
	public static final int INDEX_PAGE_ID 					= 0x1004;
	public static final int INDEX_PAGE_CREATE_TIME 			= 0x1005;
	public static final int INDEX_PAGE_MODIFIED_TIME 		= 0x1006;
	public static final int INDEX_PAGE_DEFAULT_FONT_SIZE 	= 0x1007;
	public static final int INDEX_PAGE_IS_BOOKMARK 			= 0x1008;
	public static final int INDEX_PAGE_RESERVED 			= 0x1009;
	public static final int INDEX_PAGE_TIMESTAMP_NUM 		= 0x100A;
	public static final int INDEX_PAGE_TIMESTAMP_VALUE 		= 0x100B;
	public static final int INDEX_END 						= 0x10FF;

	// book info
	public static final int MF_BEGIN 				= 0x2000;
	public static final int MF_VERSION 				= 0x2001;
	public static final int MF_DEFAULT_FONT_SIZE 	= 0x2002;
	public static final int MF_BACKGROUND_COLOR 	= 0x2003;
	public static final int MF_PAGE_TYPE 			= 0x2004;
	public static final int MF_END 					= 0x20FF;

	// page items
	public static final int PAGECOTENT_BEGIN 					= 0x3000;
	public static final int PAGECOTENT_VERSION 					= 0x3001;
	public static final int PAGECOTENT_BYTE_NUM 				= 0x3002;
	public static final int PAGECOTENT_STRING 					= 0x3003;
	public static final int PAGECOTENT_HW_TYPE 					= 0x3011;
	public static final int PAGECOTENT_HW_SCALE 				= 0x3012;
	public static final int PAGECOTENT_HW_FONT_COLOR 			= 0x3013;
	public static final int PAGECOTENT_HW_MODE 					= 0x3014;
	public static final int PAGECOTENT_HW_HEIGHT_SCALE 			= 0x3015;
	public static final int PAGECOTENT_HW_POS_IN_STRING 		= 0x3016;
	public static final int PAGECOTENT_HW_BYTE_NUM 				= 0x3017;
	public static final int PAGECOTENT_HW_PATH_XY 				= 0x3018;
//	public static final int PAGECOTENT_HW_PATH_Y				= 0x3019;
	public static final int PAGECOTENT_ICON_POS_IN_STRING 		= 0x3021;
	public static final int PAGECOTENT_ICON_ID 					= 0x3022;
	public static final int PAGECOTENT_TIMESTAMP_POS_IN_STRING 	= 0x3031;
	public static final int PAGECOTENT_TIMESTAMP_VALUE 			= 0x3032;
	public static final int PAGECOTENT_FONTCOLOR_COLOR 			= 0x3041;
	public static final int PAGECOTENT_FONTCOLOR_POS_BEGIN 		= 0x3042;
	public static final int PAGECOTENT_FONTCOLOR_POS_END 		= 0x3043;
	public static final int PAGECOTENT_FONTSTYLE_STYLE 			= 0x3051;
	public static final int PAGECOTENT_FONTSTYLE_POS_BEGIN 		= 0x3052;
	public static final int PAGECOTENT_FONTSTYLE_POS_END 		= 0x3053;
	public static final int PAGECOTENT_ATTACHMENT_POS_BEGIN 	= 0x3061;
	public static final int PAGECOTENT_ATTACHMENT_POS_END 		= 0x3062;
	public static final int PAGECOTENT_ATTACHMENT_PATH_BYTE_NUM = 0x3063;
	public static final int PAGECOTENT_ATTACHMENT_PATH 			= 0x3064;
	public static final int PAGECOTENT_END 						= 0x30FF;

	// page thumbnail
	public static final int PAGETHUMBNAIL_BEGIN 	= 0x4000;
	public static final int PAGETHUMBNAIL_DATA 		= 0x4001;
	public static final int PAGETHUMBNAIL_END 		= 0x40FF;

	// page doodles
	public static final int PAGELAYER_BEGIN 						= 0x5000;
	public static final int PAGELAYER_VERSION 						= 0x5001;
	public static final int PAGELAYER_LAYER_NUM 					= 0x5002;
	public static final int PAGELAYER_LAYER_VERSION 				= 0x5011;
	public static final int PAGELAYER_LAYER_RECT 					= 0x5012;
	public static final int PAGELAYER_LAYER_ROTATE_DEGREE 			= 0x5013;
	public static final int PAGELAYER_LAYER_FILE_ID 				= 0x5014;
	public static final int PAGELAYER_LAYER_OP_DESC_VERSION0 		= 0x5021;
	public static final int PAGELAYER_LAYER_OP_DESC_VERSION1 		= 0x5022;
	public static final int PAGELAYER_LAYER_OP_DESC_RECT 			= 0x5023;
	public static final int PAGELAYER_LAYER_OP_DESC_BRUSH_STYLE 	= 0x5024;
	public static final int PAGELAYER_LAYER_OP_DESC_VERSION3 		= 0x5025;
	public static final int PAGELAYER_LAYER_OP_DESC_REAL_WIDTH 		= 0x5026;
	public static final int PAGELAYER_LAYER_OP_DESC_REAL_HEIGHT 	= 0x5027;
	public static final int PAGELAYER_LAYER_OP_NUM 					= 0x5028;
	public static final int PAGELAYER_LAYER_OP_PT_VERSION 			= 0x5031;
	public static final int PAGELAYER_LAYER_OP_PT_NUM 				= 0x5032;
	public static final int PAGELAYER_LAYER_OP_PT_XY 				= 0x5033;
	public static final int PAGELAYER_LAYER_OP_PT_SEED 				= 0x5034;
	public static final int PAGELAYER_LAYER_OP_VERSION0 			= 0x5041;
	public static final int PAGELAYER_LAYER_OP_RECT 				= 0x5042;
	public static final int PAGELAYER_LAYER_OP_STYLE 				= 0x5043;
	public static final int PAGELAYER_LAYER_OP_VERSION2 			= 0x5044;
	public static final int PAGELAYER_LAYER_OP_REAL_WIDTH 			= 0x5045;
	public static final int PAGELAYER_LAYER_OP_REAL_HEIGHT 			= 0x5046;
	public static final int PAGELAYER_LAYER_OP_PEN_SHIFT_XY 		= 0x5051;
	public static final int PAGELAYER_LAYER_OP_PEN_RT_CENTER_XY 	= 0x5053;
	public static final int PAGELAYER_LAYER_OP_PEN_RT_DEGREE 		= 0x5055;
	public static final int PAGELAYER_LAYER_OP_PEN_SCALE_CENTER_XY 	= 0x5056;
	public static final int PAGELAYER_LAYER_OP_PEN_SCALE_RATIO 		= 0x5058;
	public static final int PAGELAYER_LAYER_OP_PEN_BRUSH_COLOR 		= 0x5059;
	public static final int PAGELAYER_LAYER_OP_PEN_BRUSH_WIDTH 		= 0x505A;
	public static final int PAGELAYER_END 							= 0x50FF;

	// page attachment
	public static final int EXTERNALFILE_BEGIN 	= 0x6000;
	public static final int EXTERNALFILE_DATA 	= 0x6001;
	public static final int EXTERNALFILE_END 	= 0x60FF;

	// flags, not stored in file
	public static final int FILE_BEGIN 					= 0xF000;
	public static final int FILE_INDEX_BEGIN 			= 0xF001;
	public static final int FILE_INDEX_END 				= 0xF002;
	public static final int FILE_MF_BEGIN 				= 0xF003;
	public static final int FILE_MF_END 				= 0xF004;
	public static final int FILE_PAGE_CONTENT_BEGIN 	= 0xF005;
	public static final int FILE_PAGE_CONTENT_END 		= 0xF006;
	public static final int FILE_PAGE_THUMBNAIL_BEGIN 	= 0xF007;
	public static final int FILE_PAGE_THUMBNAIL_END 	= 0xF008;
	public static final int FILE_PAGE_LAYER_BEGIN 		= 0xF009;
	public static final int FILE_PAGE_LAYER_END 		= 0xF00A;
	public static final int FILE_EXTERNAL_FILE_BEGIN 	= 0xF00B;
	public static final int FILE_EXTERNAL_FILE_END 		= 0xF00C;
	public static final int FILE_PREF_BEGIN				= 0xF00D;
	public static final int FILE_PREF_END				= 0xF00E;
	public static final int FILE_NBS_BEGIN				= 0xF00F;
	public static final int FILE_NBS_END				= 0xF010;
	public static final int FILE_END 					= 0xF0FF;

	// end flags
	public static final int END = 0xFFFF;

}
