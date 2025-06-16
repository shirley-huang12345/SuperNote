package com.asus.supernote.picker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.dialog.utils.DialogUtils;
import com.asus.supernote.dialog.utils.TextViewUtils;
import com.asus.supernote.ga.GACollector;
import com.asus.supernote.inksearch.CFG;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AddNewDialogFragment extends DialogFragment{
	public static final String TAG = "AddNewDialogFragment";
	public static LanguageHelper sLanguageHelper = new LanguageHelper();
	
	private int mDeviceType;
	private String mNoteBookName = null;
	private boolean mIsBasicType = true;
	private EditText mNotebookNameEditText = null;
	
	private static String NOTEBOOKNAME = "NoteBookName";
	private static String ISBASICTYPE = "IsBasicType";
	private static boolean mAddBookProperty_Switch = false;
	private static boolean mAddBookProperty_isPad = false;
	private static boolean mAddBookProperty_checkbox_ischecked = true;
	private static int 	mAddBookProperty_languageIndex = -1;
	private boolean mIsDialogShown = false;
	
	public static AddNewDialogFragment newInstance(String notebookname, boolean isBasicType){
		AddNewDialogFragment fragment = new AddNewDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString(NOTEBOOKNAME, notebookname);
		bundle.putBoolean(ISBASICTYPE, isBasicType);
		fragment.setArguments(bundle);
		return fragment;
	}

	public String getBookName(){
		return mNotebookNameEditText.getText().toString();
	}
	
	public boolean getType(){
		return mIsBasicType;
	}
	
	public boolean isDialogShown(){
		return mIsDialogShown;
	}
	
	public void clearAddBookProperty()
	{
		mAddBookProperty_Switch = false;
		mAddBookProperty_isPad = false;
		mAddBookProperty_checkbox_ischecked = true;
		mAddBookProperty_languageIndex = -1;
		mIsDialogShown = false;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDeviceType = PickerUtility.getDeviceType(getActivity());
        mIsDialogShown = true;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.notebook_template_picker, container, false);
		
		GridView gridview = (GridView)view.findViewById(R.id.template_gridview);
		final Spinner spinner = (Spinner) view.findViewById(R.id.language_spanner);		
		final CheckBox checkbox = (CheckBox) view.findViewById(R.id.content_search_checkbox);
		
		if(CFG.getCanDoVO() == true) { // BEGIN: Shane_Wang 2012-10-30  
			ArrayAdapter< String> languageAdapter = new ArrayAdapter< String>(getActivity(),  
					R.layout.language_select_spinnier,getResources().getStringArray(R.array.not_translate_index_language_values));	
			spinner.setAdapter(languageAdapter);   
	        int position = 0;
	        //darwin
	        if(mAddBookProperty_Switch)
	        {
	        	spinner.setSelection(mAddBookProperty_languageIndex);
	        	checkbox.setChecked(mAddBookProperty_checkbox_ischecked);
	        	if(!mAddBookProperty_checkbox_ischecked)
	        	{
	        		spinner.setVisibility(View.GONE);
	        	}
	        }
	        else
	        {
	        //darwin
	        int recordIndexLanguage = sLanguageHelper.getRecordIndexLaguage();//darwin
	        for (int indexLanguage : MetaData.INDEX_LANGUAGES) {
	            if (recordIndexLanguage == indexLanguage) {
	                break;
	            }
	            position++;
	        }
			spinner.setSelection(position);
				mAddBookProperty_languageIndex = position;//darwin
	        }
	        //darwin
	        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    		@Override
	    		public void onItemSelected(AdapterView<?> adapterView, View view,
	    				int index, long arg3) {
	    			mAddBookProperty_languageIndex = index;//darwin
	    		}

	    		@Override
	    		public void onNothingSelected(AdapterView<?> arg0) {
	    		}
	    	});
	        //darwin
			checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					if(arg1){
						spinner.setVisibility(View.VISIBLE);
						mAddBookProperty_checkbox_ischecked = true;//darwin
					}
					else{
						spinner.setVisibility(View.GONE);
						mAddBookProperty_checkbox_ischecked = false;//darwin
					}
					
				}
				
			});
		}else{
			View layout = view.findViewById(R.id.content_search_layout); //smilefish
			if(layout != null)
				layout.setVisibility(View.GONE);
		}// END: Shane_Wang 2012-10-30
			
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();   
		String[] strings = getResources().getStringArray(R.array.pg_size);
		
		Map<String, Object> map1 = new HashMap<String, Object>();  
		map1.put("Image", R.drawable.asus_supernote_cover_pad2);
		map1.put("Text", strings[0]);
		list.add(map1);
		
		Map<String, Object> map2 = new HashMap<String, Object>();  
		map2.put("Image", R.drawable.asus_supernote_cover_phone2);
		map2.put("Text", strings[1]);
		list.add(map2);
		
		final Spinner pagesizeSpinner = (Spinner)view.findViewById(R.id.pagesize_spinner);
		SimpleAdapter adapter = new SimpleAdapter(getActivity(), list, R.layout.simple_spinner_with_icon_item, 
				 new String[]{"Image","Text"}, new int[]{R.id.listitem_img,R.id.listitem_text}); 
		adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		pagesizeSpinner.setAdapter(adapter);   
		pagesizeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
    		@Override
    		public void onItemSelected(AdapterView<?> adapterView, View view,
    				int index, long arg3) {
    			if(index == 0){
    				mAddBookProperty_isPad = true;
    			}else{
    				mAddBookProperty_isPad = false;
    			}
    		}

    		@Override
    		public void onNothingSelected(AdapterView<?> arg0) {
    		}
    	});
		if(MetaData.isEnablePadOrPhoneChoose()) //for FE375CL smilefish
			pagesizeSpinner.setVisibility(View.VISIBLE);
		else
			pagesizeSpinner.setVisibility(View.GONE);

        if(mAddBookProperty_Switch)
        {
        	if (mAddBookProperty_isPad) {
        		pagesizeSpinner.setSelection(0);
			}
			else
			{
				pagesizeSpinner.setSelection(1);
			}
        }
        else
        {
			if (mDeviceType > 100) {
				pagesizeSpinner.setSelection(0);
				mAddBookProperty_isPad = true;//darwin
			}
			else
			{
				pagesizeSpinner.setSelection(1);
				mAddBookProperty_isPad = false;//darwin
			}
        }
        
        Bundle b = getArguments();
        mNoteBookName = b.getString(NOTEBOOKNAME);
        mIsBasicType = b.getBoolean(ISBASICTYPE);
        
		final BookNameEditText bookNameEditText = (BookNameEditText)view.findViewById(R.id.nb_name); 
		LinearLayout nameLayout = (LinearLayout)view.findViewById(R.id.notebook_name_layout);
		bookNameEditText.setBookNameLayout(nameLayout);
		bookNameEditText.setHint(PickerUtility.getDefaultBookName(getActivity()));
	    mNotebookNameEditText = bookNameEditText;
	    mNotebookNameEditText.setText(mNoteBookName);
	    TextViewUtils.enableCapSentences(bookNameEditText);//noah
	    if (mNoteBookName != null) {
	    	bookNameEditText.setText(mNoteBookName);
	    	bookNameEditText.setSelection(mNoteBookName.length());
	    }
		ArrayList<HashMap<String, Object>> mTemplateItem = new ArrayList<HashMap<String, Object>>();   
		
		if(mIsBasicType){
			HashMap<String, Object> item1 = new HashMap<String, Object>();  
			item1.put("ItemImage", R.drawable.asus_supernote_cover2_blank_white_local);
			item1.put("ItemText", getResources().getString(R.string.book_type_blank));
			mTemplateItem.add(item1);
			
			HashMap<String, Object> item2 = new HashMap<String, Object>();  
			item2.put("ItemImage", R.drawable.asus_supernote_cover2_line_white_local);
			item2.put("ItemText", getResources().getString(R.string.book_type_line));
			mTemplateItem.add(item2);
			
			HashMap<String, Object> item3 = new HashMap<String, Object>();  
			item3.put("ItemImage", R.drawable.asus_supernote_cover2_grid_white_local);  
			item3.put("ItemText", getResources().getString(R.string.book_type_grid)); 
			mTemplateItem.add(item3);
			
			HashMap<String, Object> item4 = new HashMap<String, Object>();  
			item4.put("ItemImage", R.drawable.asus_supernote_cover2_blank_yellow_local);
			item4.put("ItemText", getResources().getString(R.string.book_type_blank));
			mTemplateItem.add(item4);
			
			HashMap<String, Object> item5 = new HashMap<String, Object>();  
			item5.put("ItemImage", R.drawable.asus_supernote_cover2_line_yellow_local);
			item5.put("ItemText", getResources().getString(R.string.book_type_line));
			mTemplateItem.add(item5);		
			
			HashMap<String, Object> item6 = new HashMap<String, Object>();  
			item6.put("ItemImage", R.drawable.asus_supernote_cover2_grid_yellow_local);  
			item6.put("ItemText", getResources().getString(R.string.book_type_grid)); 
			mTemplateItem.add(item6);
		}else {
			HashMap<String, Object> item7 = new HashMap<String, Object>();  
			item7.put("ItemImage", R.drawable.asus_supernote_template_meeting);  
			item7.put("ItemText", getResources().getString(R.string.book_type_meeting)); 
			mTemplateItem.add(item7);

			HashMap<String, Object> item8 = new HashMap<String, Object>();  
			item8.put("ItemImage", R.drawable.asus_supernote_template_diary);  
			item8.put("ItemText", getResources().getString(R.string.book_type_diary)); 
			mTemplateItem.add(item8);	
			
			if(MetaData.IS_ENABLE_TODO_TEMPLATE){
				HashMap<String, Object> item9 = new HashMap<String, Object>();  
				item9.put("ItemImage", R.drawable.asus_supernote_template_memo);  
				item9.put("ItemText", getResources().getString(R.string.todo_title)); 
				mTemplateItem.add(item9);
			}
		}
		
		SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(),mTemplateItem,R.layout.notebook_template_page_item,
				new String[] {"ItemImage","ItemText"},new int[] {R.id.template_cover,R.id.template_name});   
		gridview.setAdapter(simpleAdapter);  
	    gridview.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				int pagesize;
				if(pagesizeSpinner.getSelectedItemPosition() == 0)
				{
					pagesize = MetaData.PAGE_SIZE_PAD;
				}
				else{
					pagesize = MetaData.PAGE_SIZE_PHONE;
				}
				int indexLanguage = -1;
				if(CFG.getCanDoVO() == true) { // BEGIN: Shane_Wang 2012-10-30
					if(checkbox.isChecked())
					{
						indexLanguage = MetaData.INDEX_LANGUAGES[spinner.getSelectedItemPosition()];
					}
					else{
						indexLanguage =-1; 
					}
				}// END: Shane_Wang 2012-10-30
				if(mIsBasicType){
					switch(arg2)
					{
					case 0: 
						addNewBook(MetaData.BOOK_COLOR_WHITE,MetaData.Template_type_blank,pagesize,MetaData.BOOK_GRID_BLANK,bookNameEditText,indexLanguage);
						break;
					case 1: 
						addNewBook(MetaData.BOOK_COLOR_WHITE,MetaData.Template_type_normal,pagesize,MetaData.BOOK_GRID_LINE,bookNameEditText,indexLanguage);
						break;
					case 2: 
						addNewBook(MetaData.BOOK_COLOR_WHITE,MetaData.Template_type_normal,pagesize,MetaData.BOOK_GRID_GRID,bookNameEditText,indexLanguage);
						break;
					case 3: 
						addNewBook(MetaData.BOOK_COLOR_YELLOW,MetaData.Template_type_blank,pagesize,MetaData.BOOK_GRID_BLANK,bookNameEditText,indexLanguage);
						break;
					case 4: 
						addNewBook(MetaData.BOOK_COLOR_YELLOW,MetaData.Template_type_normal,pagesize,MetaData.BOOK_GRID_LINE,bookNameEditText,indexLanguage);
						break;
					case 5: 
						addNewBook(MetaData.BOOK_COLOR_YELLOW,MetaData.Template_type_normal,pagesize,MetaData.BOOK_GRID_GRID,bookNameEditText,indexLanguage);
						break;	
				    default: break;
					}	
				}else{
					switch(arg2)
					{
					case 0:
						addNewBook(MetaData.BOOK_COLOR_WHITE,MetaData.Template_type_meeting,pagesize,MetaData.BOOK_GRID_LINE,bookNameEditText,indexLanguage);
						break;
					case 1:
						addNewBook(MetaData.BOOK_COLOR_WHITE,MetaData.Template_type_travel,pagesize,MetaData.BOOK_GRID_LINE,bookNameEditText,indexLanguage);
						break;
					case 2:
						addNewBook(MetaData.BOOK_COLOR_WHITE,MetaData.Template_type_todo,pagesize,MetaData.BOOK_GRID_BLANK,bookNameEditText,indexLanguage);
						break;	
				    default: break;
					}
				}
				
				//+++ Dave  GA
				if(MetaData.IS_GA_ON)
				{
					GACollector gaCollector = new GACollector(getActivity());
					gaCollector.addNewBook(arg2, pagesize);//emmanual:添加pagesize的记�?
				}
				//---
				
				// BEGIN: Shane_Wang@asus.com 2013-1-28
				clearAddBookProperty();
				// END: Shane_Wang@asus.com 2013-1-28
			}
	    
	    });
		
	    mAddBookProperty_Switch = true;
	    
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		return view;
	}
	
	@Override
	public void onStart() {
		if(mNotebookNameEditText != null)
			DialogUtils.forcePopupSoftInput(getDialog()); //smilefish show keyboard when resume 308093
		
		super.onStart();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		NoteBookPageGridViewAdapter.resetNotebookPageGridProcessing();
		clearAddBookProperty();
		
		super.onCancel(dialog);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	/**
	 * Add a new book
	 * @author Allen_Lai@asus.com.cn
	 * @param bookColor the color of book
	 * @param templateType the template
	 * @param bookStyle the style
	 * @param bookNameEditText the book name EditText
	 */
	private void addNewBook(int bookColor,int templateType,int pageSize,int bookStyle,EditText bookNameEditText,int indexLanguage)
	{
		NoteBookPickerActivity pickerActivity = (NoteBookPickerActivity)getActivity();
		NoteBook book = new NoteBook(pickerActivity);
		if (bookNameEditText == null
				|| bookNameEditText.getText().toString().equals("")) {
			book.setTitle(PickerUtility
					.getDefaultBookName(pickerActivity));
		} else {
			book.setTitle(bookNameEditText.getText().toString());
		}

		int color = bookColor;
		int template = templateType;
		book.setTemplate(template);
		int style = bookStyle;
		book.setPageSize(pageSize);
		book.setBookColor(color);
		book.setGridType(style);
		book.setIsLocked(false);
		book.setIndexLanguage(indexLanguage);
		BookCase bookcase = BookCase.getInstance(pickerActivity);
		bookcase.addNewBook(book);
		pickerActivity.updateFragment();
		AddNewDialogFragment.this.dismiss();

		// BNGIN, James5
		//mDisplayType = DISPLAY_PAGE;
		NotePage notepage = pickerActivity.createNewPageAndLoad(book);
	     
		try {
			Intent intent = new Intent();
			intent.setClass(getActivity(), EditorActivity.class);
			intent.putExtra(MetaData.BOOK_ID, notepage.getOwnerBookId());
			intent.putExtra(MetaData.PAGE_ID, notepage.getCreatedTime());
			intent.putExtra(MetaData.IS_NEW_PAGE, true);
			if(pickerActivity.isShareMode()){
				intent.putExtra(Intent.EXTRA_INTENT, pickerActivity.getShareIntent());//noah;for share
			}
			if(book.getTemplate() != MetaData.Template_type_normal&&
					book.getTemplate() != MetaData.Template_type_blank)
			{
				intent.putExtra(MetaData.TEMPLATE_BOOK_NEW, 1L);//not 0 is OK;
			}
			pickerActivity.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// End, James5
		
		//Begin Allen for update widget 
		if(!MetaData.SuperNoteUpdateInfoSet.containsKey(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_ADD_BOOK)){
			MetaData.SuperNoteUpdateInfoSet.put(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_ADD_BOOK,null);
		}
		//End Allen
		//begin noah;for share
		if(pickerActivity.isShareMode()){
			pickerActivity.finish();
		}
		//end noah;for share
	}
}
