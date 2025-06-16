package com.asus.supernote.picker;

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

public class MovePageDialogFragment extends DialogFragment {
    public static final String TAG = "MovePageDialogFragment";
    public static final int MOVE_SELECT_DIALOG = 1;
    public static final int MOVE_CONFIRM_DIALOG = 2;
    public static final int MOVE_PROGRESS_DIALOG = 3;
    public static final int MOVE_SUCCESS_DIALOG = 4;
    public static final int MOVE_FAIL_DIALOG = 5;
    public static final int MOVE_NEW_BOOK_DIALOG = 6;

    public static int mStyle = MOVE_SELECT_DIALOG;
    
    private SimpleBookListAdapter mAdapter = null;

    static MovePageDialogFragment newInstance(Bundle b) {
        MovePageDialogFragment f = new MovePageDialogFragment();
        f.setArguments(b);
        f.setCancelable(false);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mStyle = getArguments().getInt("style", MOVE_SELECT_DIALOG);
        boolean isPadSize;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        switch (mStyle) {
            case MOVE_SELECT_DIALOG: {
                //Done
				//begin wendy
               boolean isPrivate = getArguments().getBoolean("isPrivate", false);

                isPadSize = (BookCase.getInstance(getActivity()).getCurrentBook().getPageSize() == MetaData.PAGE_SIZE_PAD) ? true : false;
                
                View view = View.inflate(getActivity(), R.layout.page_copy_select_who_dialog, null);
                final LinearLayout addBookLayout = (LinearLayout) view.findViewById(R.id.add_book_layout);
                addBookLayout.setOnClickListener(addNewDestBookListener);
               //end wendy
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
                mAdapter = new SimpleBookListAdapter(getActivity(), isPrivate, isPadSize, 
                		BookCase.getInstance(getActivity()).getCurrentBookId(),
                		BookCase.getInstance(getActivity()).getCurrentBook().getTemplate());
                list.setAdapter(mAdapter);
                list.setOnItemClickListener(selectDestBookListener);
                builder.setTitle(R.string.pg_move_to);
                builder.setView(view);
                builder.setNegativeButton(android.R.string.cancel, null);
                return builder.create();
            }
            case MOVE_CONFIRM_DIALOG: {
                //Done
                String title = getArguments().getString("title", "");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getActivity().getResources().getString(R.string.pg_move_confirm));
                stringBuilder.append(" \" ");
                stringBuilder.append(title);
                stringBuilder.append(" \" ?");
                final View view = View.inflate(getActivity(), R.layout.one_msg_dialog, null);
                final TextView textView = (TextView) view.findViewById(R.id.msg_text_view);
                textView.setText(stringBuilder.toString());
                builder.setTitle(R.string.pg_move_to);
                builder.setView(view);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PickerActivity activity = (PickerActivity) getActivity();
                        activity.executeMovePage();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                return builder.create();
            }
            case MOVE_PROGRESS_DIALOG: {
                ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setTitle(R.string.pg_move);
                dialog.setMax(getArguments().getInt("max", 0));
                PickerActivity activity = (PickerActivity) getActivity();
                activity.setMovePagesProgressDialog(dialog);
                return dialog;
            }
            case MOVE_SUCCESS_DIALOG:
                break;
            case MOVE_FAIL_DIALOG:
                break;
            case MOVE_NEW_BOOK_DIALOG: {
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

                        PickerActivity activity = (PickerActivity) getActivity();
                        newBook.setPageSize(srcBook.getPageSize());
                        newBook.setBookColor(srcBook.getBookColor());
                        newBook.setGridType(srcBook.getGridType());
                        newBook.setTemplate(srcBook.getTemplate());//begin wendy allen++ for template 0706
                        newBook.setIndexLanguage(srcBook.getIndexLanguage());//RICHARD
                        BookCase bookCase = BookCase.getInstance(getActivity());
                        bookCase.addNewBook(newBook);

                        activity.setMovePageDestBook(newBook);
                        activity.executeMovePage();
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
            PickerActivity activity = (PickerActivity) getActivity();
            //mars fix monkey test bug
            if(activity == null)
            	return;
            activity.confirmMovePages(BookCase.getInstance(getActivity()).getNoteBook(destBookId));
        }
    };

    private final View.OnClickListener addNewDestBookListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	//mars add for monkey test
        	try{
	            FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
	            Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(MovePageDialogFragment.TAG);
	            if (fragment != null && fragment.isAdded()) {
	                ft.remove(fragment);
	            }
	            ft.commit();
	            Bundle b = new Bundle();
	            b.putInt("style", MOVE_NEW_BOOK_DIALOG);
	            MovePageDialogFragment newFragment = MovePageDialogFragment.newInstance(b);
	            newFragment.show(getActivity().getFragmentManager(), MovePageDialogFragment.TAG);
        	}
        	catch(Exception e){
        		e.printStackTrace();
        	}
        }
    };

}
