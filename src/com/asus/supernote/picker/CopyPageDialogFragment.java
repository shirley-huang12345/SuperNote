package com.asus.supernote.picker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;

public class CopyPageDialogFragment extends DialogFragment {
    public static final String TAG = "CopyPageDialogFragment";
    public static final int COPY_SELECT_DIALOG = 1;
    public static final int COPY_CONFIRM_DIALOG = 2;
    public static final int COPY_PROGRESS_DIALOG = 3;
    public static final int COPY_SUCCESS_DIALOG = 4;
    public static final int COPY_FAIL_DIALOG = 5;
    public static final int COPY_NEW_BOOK_DIALOG = 6;
    public static final int COPY_SELECT_DIALOG_BOOK = 7;
    public static final int COPY_CONFIRM_DIALOG_BOOK = 8;

    public static int mStyle = COPY_SELECT_DIALOG;
    private SimpleBookListAdapter mAdapter = null;

    static CopyPageDialogFragment newInstance(Bundle b) {
        CopyPageDialogFragment f = new CopyPageDialogFragment();
        f.setArguments(b);
        f.setCancelable(false);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mStyle = getArguments().getInt("style", COPY_SELECT_DIALOG);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        boolean isPadSize;
        switch (mStyle) {
	        case COPY_SELECT_DIALOG_BOOK: {
	            boolean isPrivate = getArguments().getBoolean("isPrivate", false);

	            isPadSize = (BookCase.getInstance(getActivity()).getCurrentBook().getPageSize() == MetaData.PAGE_SIZE_PAD) ? true : false;
	          //end wendy
	
	            View view = View.inflate(getActivity(), R.layout.page_copy_select_who_dialog, null);
	            final LinearLayout addBookLayout = (LinearLayout) view.findViewById(R.id.add_book_layout);
	            addBookLayout.setOnClickListener(addNewDestBookListener);
	            ImageView addBookButton = (ImageView) view.findViewById(R.id.add_book_button);
	            addBookButton.setOnClickListener(new View.OnClickListener() {
	                @Override
	                public void onClick(View v) {
	                    if (addBookLayout != null) {
	                        addBookLayout.performClick();
	                    }
	                }
	            });
	            ListView list = (ListView) view.findViewById(R.id.pg_copy_list);
	
	            mAdapter = new SimpleBookListAdapter(getActivity(), isPrivate, isPadSize, 0L,
	            		BookCase.getInstance(getActivity()).getCurrentBook().getTemplate());
	            list.setAdapter(mAdapter);
	            list.setOnItemClickListener(notebookSelectDestBookListener);
	            builder.setTitle(R.string.pg_copy_to);
	            builder.setView(view);
	            builder.setNegativeButton(android.R.string.cancel, null);
	            return builder.create();
	        }
            case COPY_SELECT_DIALOG: {
                boolean isPrivate = getArguments().getBoolean("isPrivate", false);

                isPadSize = (BookCase.getInstance(getActivity()).getCurrentBook().getPageSize() == MetaData.PAGE_SIZE_PAD) ? true : false;
              //end wendy

                View view = View.inflate(getActivity(), R.layout.page_copy_select_who_dialog, null);
                final LinearLayout addBookLayout = (LinearLayout) view.findViewById(R.id.add_book_layout);
                addBookLayout.setOnClickListener(addNewDestBookListener);
                ImageView addBookButton = (ImageView) view.findViewById(R.id.add_book_button);
                addBookButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (addBookLayout != null) {
                            addBookLayout.performClick();
                        }
                    }
                });
                ListView list = (ListView) view.findViewById(R.id.pg_copy_list);

                mAdapter = new SimpleBookListAdapter(getActivity(), isPrivate, isPadSize, 0L,
                		BookCase.getInstance(getActivity()).getCurrentBook().getTemplate());
                list.setAdapter(mAdapter);
                list.setOnItemClickListener(selectDestBookListener);
                builder.setTitle(R.string.pg_copy_to);
                builder.setView(view);
                builder.setNegativeButton(android.R.string.cancel, null);
                return builder.create();
            }
            case COPY_CONFIRM_DIALOG: {
                String title = getArguments().getString("title", "");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getActivity().getResources().getString(R.string.pg_copy_confirm));
                stringBuilder.append(" \" ");
                stringBuilder.append(title);
                stringBuilder.append(" \" ?");
                final View view = View.inflate(getActivity(), R.layout.one_msg_dialog, null);
                final TextView textView = (TextView) view.findViewById(R.id.msg_text_view);
                textView.setText(stringBuilder.toString());
                builder.setTitle(R.string.pg_copy_to);
                builder.setView(view);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	//begin wendy
                        Activity activity = getActivity();
                        if(activity instanceof PickerActivity)
                        {
                        	((PickerActivity)activity).executeCopyPage();
                        }
                        if(activity instanceof NoteBookPickerActivity)
                        {
                        	((NoteBookPickerActivity)activity).executeCopyPage();
                        }
                        //end wendy
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                return builder.create();
            }
            case COPY_CONFIRM_DIALOG_BOOK: {
                String title = getArguments().getString("title", "");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getActivity().getResources().getString(R.string.pg_copy_confirm));
                stringBuilder.append(" \" ");
                stringBuilder.append(title);
                stringBuilder.append(" \" ?");
                final View view = View.inflate(getActivity(), R.layout.one_msg_dialog, null);
                final TextView textView = (TextView) view.findViewById(R.id.msg_text_view);
                textView.setText(stringBuilder.toString());
                builder.setTitle(R.string.pg_copy_to);
                builder.setView(view);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
                        activity.executeCopyPage();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                return builder.create();
            }
            case COPY_PROGRESS_DIALOG: {
                ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setTitle(R.string.pg_copy);
                dialog.setMax(getArguments().getInt("max", 0));
                //begin wendy 
                Activity activity = getActivity();
                if(activity instanceof PickerActivity)
                {
                	((PickerActivity)activity).setCopyPagesProgressDialog(dialog);
                }
                if(activity instanceof NoteBookPickerActivity)
                {
                	((NoteBookPickerActivity)activity).setCopyPagesProgressDialog(dialog);
                }
                
                //end wendy
                return dialog;
            }
            case COPY_NEW_BOOK_DIALOG: {
                isPadSize = (BookCase.getInstance(getActivity()).getCurrentBook().getPageSize() == MetaData.PAGE_SIZE_PAD) ? true : false;
                View addBookView = View.inflate(getActivity(), R.layout.notebooks_new, null);
                final EditText bookTitle = (EditText) addBookView.findViewById(R.id.nb_name);
                LinearLayout bookSizeLayout = (LinearLayout) addBookView.findViewById(R.id.pageSizeLayout);
                bookSizeLayout.setVisibility(View.GONE);
                builder.setTitle(R.string.add_new);
                builder.setView(addBookView);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NoteBook srcBook = BookCase.getInstance(getActivity()).getCurrentBook();
                        NoteBook newBook = new NoteBook(getActivity());

                        if (bookTitle.getText().toString().isEmpty()) {
                            newBook.setTitle(PickerUtility.getDefaultBookName(getActivity()));
                        }
                        else {
                            newBook.setTitle(bookTitle.getText().toString());
                        }
                        newBook.setPageSize(srcBook.getPageSize());
                        newBook.setBookColor(srcBook.getBookColor());
                        newBook.setGridType(srcBook.getGridType());
                        newBook.setTemplate(srcBook.getTemplate());//begin wendy for template 0706    
                        newBook.setIndexLanguage(srcBook.getIndexLanguage());//RICHARD

                        BookCase bookCase = BookCase.getInstance(getActivity());
                        bookCase.addNewBook(newBook);
                        
                        //begin wendy
                        Activity activity = getActivity();
                        if(activity instanceof PickerActivity)
                        {
                        	((PickerActivity)activity).setCopyPageDestBook(newBook);
                        	((PickerActivity)activity).executeCopyPage();
                        }
                        if(activity instanceof NoteBookPickerActivity)
                        {                        	
                        	((NoteBookPickerActivity)activity).setCopyPageDestBook(newBook);
                        	((NoteBookPickerActivity)activity).executeCopyPage();
                        }
                        //end wendy

                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                return builder.create();
            }
        }
        return super.onCreateDialog(savedInstanceState);
    }

	@Override
	public void onDestroy() {
		if(mAdapter != null) //smilefish fix memory leak
			mAdapter.closeCursor();
		super.onDestroy();
	}
	
    private final OnItemClickListener selectDestBookListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> listView, View view, int index, long arg3) {
            final Long destBookId = (Long) view.findViewById(R.id.book_size).getTag();
            
            Activity activity = getActivity();
            if(activity instanceof PickerActivity)
            {
            	((PickerActivity)activity).confirmCopyPages(BookCase.getInstance(getActivity()).getNoteBook(destBookId));
                
            }
            if(activity instanceof NoteBookPickerActivity)
            {
            	((NoteBookPickerActivity)activity).confirmCopyPages(BookCase.getInstance(getActivity()).getNoteBook(destBookId));
                
            }
        }
    };
    
    private final OnItemClickListener notebookSelectDestBookListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> listView, View view, int index, long arg3) {
            final Long destBookId = (Long) view.findViewById(R.id.book_size).getTag();
            NoteBookPickerActivity activity = (NoteBookPickerActivity) getActivity();
            activity.confirmCopyPages(BookCase.getInstance(getActivity()).getNoteBook(destBookId));
        }
    };

    private final View.OnClickListener addNewDestBookListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	//add try some time have nullpointer
        	try{
	            FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
	            Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(CopyPageDialogFragment.TAG);
	            if (fragment != null && fragment.isAdded()) {
	                ft.remove(fragment);
	            }
	            ft.commit();
	            Bundle b = new Bundle();
	            b.putInt("style", COPY_NEW_BOOK_DIALOG);
	            CopyPageDialogFragment newFragment = CopyPageDialogFragment.newInstance(b);
	            newFragment.show(getActivity().getFragmentManager(), CopyPageDialogFragment.TAG);
        	}
        	catch(Exception e){
        		e.printStackTrace();
        	}
        }
    };

}
