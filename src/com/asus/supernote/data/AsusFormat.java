package com.asus.supernote.data;

import java.io.IOException;

public interface AsusFormat {
	// BEGIN: BOOK List
    int SNF_BOOKLIST_BEGIN                 = 0x01200000;
    int SNF_BOOKINFO_BEGIN                 = 0x01210000;
    int SNF_BOOKINFO_ID                    = 0x01210001;
    int SNF_BOOKINFO_TITLE                 = 0x01210002;
    int SNF_BKPROP_INDEX_COVER			   = 0x01210003;//darwin
    int SNF_BKPROP_COVER_FILE			   = 0x01210004;//darwin
    int SNF_BOOKINFO_END                   = 0x0121FFFF;
    int SNF_BOOKLIST_END                   = 0x0120FFFF;
    // END: BOOK List
    
    // BEGIN: James
    // BEGIN: BOOK Format
    int SNF_BKCASEPROP_BEGIN               = 0x01000000;
    int SNF_BKCASEPROP_END                 = 0x0100FFFF;
    int SNF_BKPROP_BEGIN                   = 0x01010000;
    int SNF_BKPROP_END                     = 0x0101FFFF;
    int SNF_BKPROP_ID                      = 0x01010001;
    int SNF_BKPROP_TITLE                   = 0x01010002;
    int SNF_BKPROP_LOCKED                  = 0x01010003;
    int SNF_BKPROP_PGSIZE                  = 0x01010004;
    int SNF_BKPROP_COLOR                   = 0x01010005;
    int SNF_BKPROP_STYLE                   = 0x01010006;
    int SNF_BKPROP_TYPE                    = 0x01010007;
    int SNF_BKPROP_PGORDER                 = 0x01010008;
    int SNF_BKPROP_PGBOOKMARK              = 0x01010009;
    int SNF_BKPROP_FONT_SIZE               = 0x0101000A;
    int SNF_BKPROP_TEMPLATE_TYPE           = 0x0101000B;
    int SNF_BKPROP_INDEX_LANGUAGE          = 0x0101000C;//Allen
    // END: BOOK Format
    // BEGIN: MEMO Format
    int SNF_MO_BEGIN                       = 0x02000000;
    int SNF_MO_END                         = 0x0200FFFF;
    int SNF_MOPROP_BEGIN                   = 0x02010000;
    int SNF_MOPROP_END                     = 0x0201FFFF;
    int SNF_MOPROP_ID                      = 0x02010001;
    int SNF_MOPROP_BKID                    = 0x02010002;
    int SNF_MOPROP_PGID                    = 0x02010003;
    int SNF_MOPROP_LEFT                    = 0x02010004;
    int SNF_MOPROP_TOP                     = 0x02010005;
    int SNF_MOPROP_RIGHT                   = 0x02010006;
    int SNF_MOPROP_BOTTOM                  = 0x02010007;
    int SNF_MOPROP_THUMBNAIL_NAME          = 0x02010008;
    int SNF_MOPROP_HIDDEN                  = 0x02010009;
    int SNF_MOPROP_BKNAME                  = 0x0201000A;

    // END: MEMO Format
    // END: James

    // BEGIN Ryan
    int SNF_NITEM_BEGIN                    = 0x03000000;
    int SNF_NITEM_END                      = 0x0300FFFF;
    
    int SNF_NITEM_VERSION				   = 0x03000010; // Better

    int SNF_NITEM_TEMPLATE_ITEM_TYPE	   = 0x03000020;//Allen
    
    int SNF_NITEM_STRING_BEGIN             = 0x03010000;
    int SNF_NITEM_STRING_TEXT              = 0x03010001;
    int SNF_NITEM_STRING_END               = 0x0301FFFF;

    int SNF_NITEM_HANDWRITE_BEGIN          = 0x03020000;
    int SNF_NITEM_HANDWRITE_POS_START      = 0x03020001;
    int SNF_NITEM_HANDWRITE_POS_END        = 0x03020002;
    int SNF_NITEM_HANDWRITE_COLOR          = 0x03020003;
    int SNF_NITEM_HANDWRITE_STROKE_WIDTH   = 0x03020004;
    int SNF_NITEM_HANDWRITE_FONT_HEIGHT    = 0x03020005;
    int SNF_NITEM_HANDWRITE_PATHS          = 0x03020006;
    // BEGIN archie_huang@asus.com
    int SNF_NITEM_HANDWRITE_FONT_WIDTH     = 0x03020007;
    // END archie_huang@asus.com
    int SNF_NITEM_HANDWRITE_END            = 0x0302FFFF;

    int SNF_NITEM_HANDWRITEBL_BEGIN        = 0x03030000;
    int SNF_NITEM_HANDWRITEBL_POS_START    = 0x03030001;
    int SNF_NITEM_HANDWRITEBL_POS_END      = 0x03030002;
    int SNF_NITEM_HANDWRITEBL_COLOR        = 0x03030003;
    int SNF_NITEM_HANDWRITEBL_STROKE_WIDTH = 0x03030004;
    int SNF_NITEM_HANDWRITEBL_FONT_HEIGHT  = 0x03030005;
    int SNF_NITEM_HANDWRITEBL_PATHS        = 0x03030006;
    // BEGIN archie_huang@asus.com
    int SNF_NITEM_HANDWRITEBL_FONT_WIDTH   = 0x03030007;
    // END archie_huang@asus.com
    int SNF_NITEM_HANDWRITEBL_END          = 0x0303FFFF;

    int SNF_NITEM_TEXTSTYLE_BEGIN          = 0x03040000;
    int SNF_NITEM_TEXTSTYLE_POS_START      = 0x03040001;
    int SNF_NITEM_TEXTSTYLE_POS_END        = 0x03040002;
    int SNF_NITEM_TEXTSTYLE_STYLE          = 0x03040003;
    int SNF_NITEM_TEXTSTYLE_END            = 0x0304FFFF;

    int SNF_NITEM_FCOLOR_BEGIN             = 0x03050000;
    int SNF_NITEM_FCOLOR_POS_START         = 0x03050001;
    int SNF_NITEM_FCOLOR_POS_END           = 0x03050002;
    int SNF_NITEM_FCOLOR_COLOR             = 0x03050003;
    int SNF_NITEM_FCOLOR_END               = 0x0305FFFF;

    int SNF_NITEM_SENDINTENT_BEGIN         = 0x03060000;
    int SNF_NITEM_SENDINTENT_POS_START     = 0x03060001;
    int SNF_NITEM_SENDINTENT_POS_END       = 0x03060002;
    int SNF_NITEM_SENDINTENT_FILE_NAME     = 0x03060003;
    int SNF_NITEM_SENDINTENT_END           = 0x0306FFFF;

    int SNF_NITEM_TIMESTAMP_BEGIN          = 0x03070000;
    int SNF_NITEM_TIMESTAMP_POS_START      = 0x03070001;
    int SNF_NITEM_TIMESTAMP_POS_END        = 0x03070002;
    int SNF_NITEM_TIMESTAMP_TIME           = 0x03070003;
    int SNF_NITEM_TIMESTAMP_END            = 0x0307FFFF;
    // END Ryan

    // BEGIN: archie_huang@asus.com
    int SNF_DITEM_BEGIN                    = 0x04000000;
    int SNF_DITEM_END                      = 0x0400FFFF;
    
    int SNF_DITEM_VERSION				   = 0x04000010; // Better
    
    int SNF_DITEM_CANVAS_WIDTH             = 0x04000001;
    int SNF_DITEM_CANVAS_HEIGHT            = 0x04000002;
    @Deprecated
    int SNF_DITEM_CANVAS_HEIGHT_ERROR      = 0x0400002; // TODO: Remove this case
    @Deprecated
    int SNF_DITEM_PAGE_ID                  = 0x04000003;
    int SNF_DITEM_DRAWS_BEGIN              = 0x04010000;
    int SNF_DITEM_DRAWS_END                = 0x0401FFFF;
    int SNF_DITEM_DRAW_BEGIN               = 0x04020000;
    int SNF_DITEM_DRAW_END                 = 0x0402FFFF;
    int SNF_DITEM_DRAW_PAINT_TOOL          = 0x04020001;
    int SNF_DITEM_DRAW_STROKE_WIDTH        = 0x04020002;
    int SNF_DITEM_DRAW_COLOR               = 0x04020003;
    int SNF_DITEM_DRAW_ALPHA               = 0x04020004; // 
    int SNF_DITEM_DRAW_ERASES_BEGIN        = 0x04030000;
    int SNF_DITEM_DRAW_ERASES_END          = 0x0403FFFF;
    int SNF_DITEM_ERASE_BEGIN              = 0x04040000;
    int SNF_DITEM_ERASE_END                = 0x0404FFFF;
    @Deprecated
    int SNF_DITEM_ERASE_ID                 = 0x04040001;
    @Deprecated
    int SNF_DITEM_ERASE_VISIBLE            = 0x04040002;
    int SNF_DITEM_ANNOTATION_BEGIN         = 0x04050000;
    int SNF_DITEM_ANNOTATION_END           = 0x0405FFFF;
    int SNF_DITEM_ANNOTATION_DRAWS_BEGIN   = 0x04060000;
    int SNF_DITEM_ANNOTATION_DRAWS_END     = 0x0406FFFF;
    int SNF_DITEM_ANNOTATION_LOCATIONS     = 0x04060001;
    int SNF_DITEM_GRAPHIC_BEGIN            = 0x04070000;
    int SNF_DITEM_GRAPHIC_END              = 0x0407FFFF;
    int SNF_DITEM_GRAPHIC_FILE_NAME        = 0x04070001;
    int SNF_DITEM_GRAPHIC_MATRIX_VALUES    = 0x04070002;
    int SNF_DITEM_GRAPHIC_WIDTH			   = 0x04070003; // Better
    int SNF_DITEM_GRAPHIC_HEIGHT		   = 0x04070004; // Better
//	int SNF_DITEM_GRAPHIC_TEMPLATE_FLAG	   = 0x04070005;//wendy allen++
    int SNF_DITEM_POINT_BEGIN              = 0x04080000;
    int SNF_DITEM_POINT_END                = 0x0408FFFF;
    int SNF_DITEM_POINT_POINTS             = 0x04080001;
    // END: archie_huang@asus.com
    
    //BEGIN: Show
    int SNF_DITEM_TEXTIMG_BEGIN            = 0x04090000;
    int SNF_DITEM_TEXTIMG_END              = 0x0409FFFF;
    int SNF_DITEM_TEXTIMG_CONTENT          = 0x04090001;
    int SNF_DITEM_TEXTIMG_MATRIX_VALUES    = 0x04090002;
    int SNF_DITEM_TEXTIMG_NAME       	   = 0x04090003;
    int SNF_DITEM_TEXTIMG_WIDTH			   = 0x04090004; // Better
    int SNF_DITEM_TEXTIMG_HEIGHT		   = 0x04090005; // Better
    //END: Show
    
    //BEGIN: RICHARD
    int SNF_DITEM_EXTRA_BEGIN            = 0x04100000;
    int SNF_DITEM_EXTRA_END              = 0x0410FFFF;
    int SNF_DITEM_EXTRA_INFO          	 = 0x04100001;
    //END: RICHARD

    // BEGIN: James
    int SNF_PREF_BEGIN                     = 0x05000000;
    int SNF_PREF_ITEM_BEGIN                = 0x05010000;
    int SNF_PREF_ITEM_END                  = 0x0501FFFF;
    int SNF_PREF_NAME                      = 0x05010001;
    int SNF_PREF_TYPE                      = 0x05010002;
    int SNF_PREF_VALUE                     = 0x05010003;
    int SNF_PREF_END                       = 0x0500FFFF;

    // END: James
    
	//Begin Darwin_Yu@asus.com
    int SNF_SYNC_FILE_NOTE_BEGIN				= 0x23000000;
    int SNF_SYNC_FILE_NOTE_END					= 0x2300FFFF;
    int SNF_SYNC_FILE_TIME_STAMP_INDEX_BEGIN					= 0x2A000000;
    int SNF_SYNC_FILE_TIME_STAMP_INDEX_END						= 0x2A00FFFF;
    int SNF_SYNC_FILE_TIME_STAMP_INDEX_ITEM_BEGIN				= 0x2A010000;
    int SNF_SYNC_FILE_TIME_STAMP_INDEX_ITEM_END					= 0x2A01FFFF;
    int SNF_SYNC_FILE_TIME_STAMP_INDEX_ITEM_ID					= 0x2A010001;
    int SNF_SYNC_FILE_TIME_STAMP_INDEX_ITEM_PAGEID				= 0x2A010002;
    int SNF_SYNC_FILE_TIME_STAMP_INDEX_ITEM_POSITION			= 0x2A010003;
    //End   Darwin_Yu@asus.com
	
    // BEGIN: Better
	// Sync <page id>.file & book.info format
    
    // Page file
    int SNF_SYNC_FILE_INFO_BEGIN				= 0x20000000;
    int SNF_SYNC_FILE_INFO_END					= 0x2000FFFF;
    
    int SNF_SYNC_FILE_THUMBNAIL					= 0x25010001;
    int SNF_SYNC_FILE_DOODLE					= 0x24010001;
    int SNF_SYNC_FILE_NOTE						= 0x23010001;
    
    int SNF_SYNC_FILE_ATTACHMENT_INFO_BEGIN		= 0x28000000;
    int SNF_SYNC_FILE_ATTACHMENT_INFO_END		= 0x2800FFFF;
    
    int SNF_SYNC_FILE_FILE_BEGIN				= 0x28010000;
    int SNF_SYNC_FILE_FILE_END					= 0x2801FFFF;
    
    int SNF_SYNC_FILE_FILE_NAME					= 0x28010001;
    int SNF_SYNC_FILE_FILE_CONTENT				= 0x28010002;
    
    // Book Info
    int SNF_TABLE_BOOK_INFO_BEGIN				= 0x11000000;
    int SNF_TABLE_BOOK_INFO_END					= 0x1100FFFF;
    
    int SNF_TABLE_NOTEBOOK_BEGIN				= 0x11010000;
    int SNF_TABLE_NOTEBOOK_END					= 0x1101FFFF;

    int SNF_TABLE_NOTEBOOK_BOOKID				= 0x11010001;
    int SNF_TABLE_NOTEBOOK_TITLE				= 0x11010002;
    int SNF_TABLE_NOTEBOOK_IS_LOCKED			= 0x11010003;
    int SNF_TABLE_NOTEBOOK_PAGE_COUNT			= 0x11010004;
    int SNF_TABLE_NOTEBOOK_BAKCOLOR				= 0x11010005;
    int SNF_TABLE_NOTEBOOK_GRIDLINE				= 0x11010006;
    int SNF_TABLE_NOTEBOOK_TYPE					= 0x11010007;
    int SNF_TABLE_NOTEBOOK_PAGEID_COLLECTION	= 0x11010008;
    int SNF_TABLE_NOTEBOOK_BOOKMARK_COLLECTION	= 0x11010009;

    int SNF_TABLE_NOTEBOOK_LAST_SYNC_MODTIME	= 0x11010051;
    int SNF_TABLE_NOTEBOOK_IS_DELETED			= 0x11010052;
    int SNF_TABLE_NOTEBOOK_TEMPLATE				= 0x11010053;//wendy allen++ template 0706
    int SNF_TABLE_NOTEBOOK_INDEXLANGUAGE		= 0X11010054;//Allen
    int SNF_TABLE_NOTEBOOK_INDEXCOVER			= 0X11010055;//darwin
    int SNF_TABLE_NOTEBOOK_COVERMODIFYTIME		= 0X11010056;//darwin
    // END: Better
    
    public void itemSave(AsusFormatWriter afw) throws IOException;

    public void itemLoad(AsusFormatReader afr) throws Exception;
}
