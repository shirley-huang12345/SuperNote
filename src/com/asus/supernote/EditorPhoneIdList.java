package com.asus.supernote;

public class EditorPhoneIdList extends EditorIdList {

    public EditorPhoneIdList() {
        funcButtonIdsKeyboard = new int[] {
                R.id.note_inputmode,
                R.id.note_kb_scribble,
                R.id.note_kb_keyboard,
                R.id.note_kb_doodle,
                R.id.note_kb_color_bold,
                R.id.note_kb_undo,
                R.id.note_kb_redo,  //By Show, new layout
                R.id.note_kb_k_more, //by smilefish
        };
        funcButtonIdsScribble = new int[] {
                R.id.note_inputmode,
                R.id.note_kb_scribble,
                R.id.note_kb_keyboard,
                R.id.note_kb_doodle,
                R.id.note_kb_color_bold,
                R.id.note_kb_undo,
                R.id.note_kb_redo,//by Shane
                R.id.note_kb_write_to_type,//RICHARD
                R.id.note_kb_s_more, //smilefish
		
        };
        
        funcButtonIdsDoodle = new int[] {
                R.id.note_inputmode,
                R.id.note_kb_scribble,
                R.id.note_kb_keyboard,
                R.id.note_kb_doodle,
                R.id.note_kb_d_brush,//By Show
                R.id.note_kb_d_eraser,
                R.id.note_kb_undo,
                R.id.note_kb_d_shape,//RICHARD
                R.id.note_kb_redo,//By Shane
                R.id.note_kb_d_more, //smilefish
        };
        funcButtonIdsSelectionText = new int[] {
                R.id.note_kb_copy,
                R.id.note_kb_color_bold,//By Show
                R.id.note_kb_sel_write_to_type,//RICHARD
                R.id.note_kb_done //by show
        };
        funcButtonIdsReadOnly = new int[] {
        		R.id.note_kb_editabel, //smilefish
        		R.id.note_kb_more //smilefish
                };
        funcButtonIdsInsert = new int[] {
        		R.id.note_kb_d_done //smilefish
                };
        funcButtonIdsSelectionDoodle = new int[] {
                R.id.note_kb_d_crop,		//By Show
                R.id.note_kb_d_textedit,	//By Show
                R.id.note_kb_d_group,
                R.id.note_kb_d_ungroup,
                R.id.note_kb_copy,
                R.id.note_kb_d_done //smilefish
        };
        funcButtonIdsTextImgKeyboard = new int[] {
                R.id.note_kb_textimg_keyboard,
                R.id.note_kb_textimg_scribble,
                R.id.note_kb_color_bold,
                R.id.note_kb_undo,
                R.id.note_kb_redo, //Carol
                R.id.note_kb_textimg_cancle,//smilefish
                R.id.note_kb_textimg_done //smilefish
        };
        funcButtonIdsTextImgScribble = new int[] {
                R.id.note_kb_textimg_keyboard,
                R.id.note_kb_textimg_scribble,
                R.id.note_kb_color_bold,
                R.id.note_kb_undo,
                R.id.note_kb_redo, //Carol
                R.id.note_kb_textimg_cancle,//smilefish
                R.id.note_kb_textimg_done //smilefish
        };
        funcButtonIdsColorPicker = new int[] { //smilefish
                R.id.note_kb_color_cancel,
                R.id.note_kb_color_done
        };
        bottomButtonIdsKeyboard = new int[] {
                R.id.bottom_last,
                R.id.bottom_next,
                R.id.note_kb_insert,//smilefish
                R.id.note_kb_add, //smilefish
        };
        bottomButtonIdsScrible = new int[] {
                R.id.bottom_last,
                R.id.bottom_next,
                R.id.note_kb_insert,//smilefish
                R.id.note_kb_add, //smilefish
        };
        bottomButtonIdsOthers = new int[] {
                R.id.bottom_last,
                R.id.bottom_next,
                R.id.note_kb_insert,//smilefish
                R.id.note_kb_add, //smilefish
        };
        editorColorIds = new int[] {
                R.id.editor_func_color_green,
                R.id.editor_func_color_red,
                R.id.editor_func_color_blue,
                R.id.editor_func_color_black
        };
        editorBoldIds = new int[] {
                R.id.editor_func_bold_true,
                R.id.editor_func_bold_false
        };
        editorDoodleEraserIds = new int[] {
                R.id.editor_func_eraser_3,
                R.id.editor_func_eraser_2,
                R.id.editor_func_eraser_1,
                R.id.clear_all
        };
        editorDoodleBrushIds = new int[] {
                R.id.editor_func_d_brush_normal,
                R.id.editor_func_d_brush_scribble,
                R.id.editor_func_d_brush_mark,
                R.id.editor_func_d_brush_sketch,
                //begin wendy
                R.id.editor_func_d_brush_markpen,
                R.id.editor_func_d_brush_writingbrush
                //end wendy        
        };
        
        //BEGIN:shuan_xu@asus.com
        editorDoodleUnityIds = new int[]{
       			R.id.editor_func_d_brush_normal,			
       			R.id.editor_func_d_brush_scribble,			
       			R.id.editor_func_d_brush_mark,
       			R.id.editor_func_d_brush_sketch,
       			// begin wendy
       			R.id.editor_func_d_brush_markpen,
       			R.id.editor_func_d_brush_writingbrush,
       			// end wendy

       			R.id.editor_func_color_A,
       			R.id.editor_func_color_B,
       			R.id.editor_func_color_C,
       			R.id.editor_func_color_D,
       			R.id.editor_func_color_E,
       			R.id.editor_func_color_F,
       			R.id.editor_func_color_G,
       			R.id.editor_func_color_H, 
       			R.id.editor_func_color_I,
       			R.id.editor_func_color_J,
       			R.id.editor_func_color_H,
       			R.id.editor_func_color_K,
       			R.id.editor_func_color_M, //smilefish
       			R.id.editor_func_color_L
       			 };
       	editorDoodleUnityColorIds = new int[] {
       			R.id.editor_func_color_A,
       			R.id.editor_func_color_B,
       			R.id.editor_func_color_C,
       			R.id.editor_func_color_D,
       			R.id.editor_func_color_E,
       			R.id.editor_func_color_F,
       			R.id.editor_func_color_G,
       			R.id.editor_func_color_H, 
       			R.id.editor_func_color_I,
       			R.id.editor_func_color_J,
       			R.id.editor_func_color_H,
       			R.id.editor_func_color_K,
       			R.id.editor_func_color_M, //smilefish
       			R.id.editor_func_color_L			
       			 };  
       	// END:shaun_xu@asus.com	

        editorColorBoldIds = new int[] {
                R.id.editor_func_color_black,
                R.id.editor_func_color_red,
                R.id.editor_func_color_blue,
                R.id.editor_func_color_green,
                R.id.editor_func_bold_false,
                R.id.editor_func_bold_true
        };
        
        //begin smilefish
        menuItemIds = new int[]{
        		R.id.editor_menu_search,
        		R.id.editor_menu_save,
        		R.id.editor_menu_delete,
        		R.id.editor_menu_bookmark,
        		R.id.editor_menu_read,
        		R.id.editor_menu_pen,
        		R.id.editor_menu_read_later,
        		R.id.editor_menu_share,
        		R.id.editor_menu_setting
        };
        menuItemReadonlyIds = new int[]{
    		R.id.editor_menu_bookmark,
    		R.id.editor_menu_read_later,
    		R.id.editor_menu_share,
    		R.id.editor_menu_setting
        };
        //end smilefish
    }

    public int[] editorModeIds = {
            R.id.note_kb_scribble,
            R.id.note_kb_keyboard,
            R.id.note_kb_doodle,
    };
}
