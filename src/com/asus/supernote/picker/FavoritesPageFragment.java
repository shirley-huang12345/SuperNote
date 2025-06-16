package com.asus.supernote.picker;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ActionMode.Callback;
import android.widget.GridView;
import android.widget.TextView;

import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;

public class FavoritesPageFragment extends Fragment implements Callback, DataCounterListener {
    public static final String TAG = "FavoritesPageFragment";

    private FavortiesPageAdapter mAdapter;
    private TextView pageCounterTextView;
    Long mBookId;
    public FavoritesPageFragment() {    
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new FavortiesPageAdapter(getActivity());
        mAdapter.registerDataCounterListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	mBookId = getActivity().getIntent().getLongExtra(MetaData.BOOK_ID, 0L);
        View view = inflater.inflate(R.layout.bookmarks_view, null);
        TextView bookNameTextView = (TextView) view.findViewById(R.id.title_book_name);
         pageCounterTextView = (TextView) view.findViewById(R.id.title_book_count);
        NoteBook book = BookCase.getInstance(getActivity()).getNoteBook(mBookId);
        bookNameTextView.setText(book.getTitle());
        
        Cursor cursor = getActivity().getContentResolver().query(MetaData.PageTable.uri, null, "is_bookmark > 0 AND owner = ?", new String[] { mBookId.toString() }, null);
        String pageNumber = String.format(getActivity().getResources().getString(R.string.pg_title_counter), cursor.getCount());
        cursor.close();
        pageCounterTextView.setText(pageNumber);
        
        GridView mGridView = (GridView) view.findViewById(R.id.page_gridview);  
        mGridView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onResume() {
        setHasOptionsMenu(true);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        // BEGIN: Shane_Wang@asus.com 2013-1-7
        mAdapter.setDateAndTimeFormat();
        mAdapter.dataChange();
        mAdapter.notifyDataSetChanged();
        // END: Shane_Wang@asus.com 2013-1-7
        
        //darwin
        if(pageCounterTextView != null)
        {
        	Cursor cursor = getActivity().getContentResolver().query(MetaData.PageTable.uri, null, "is_bookmark > 0 AND owner = ?", new String[] { mBookId.toString() }, null);
            String pageNumber = String.format(getActivity().getResources().getString(R.string.pg_title_counter), cursor.getCount());
            cursor.close();
            pageCounterTextView.setText(pageNumber);	
        }
        //darwin
        super.onResume();
    }

    @Override
    public void onPause() {
        setHasOptionsMenu(false);
        super.onPause();
    }
 
    @Override
    public void onDestroy() {     
        mAdapter.removeDataCounterListener();
        mAdapter.closeCursor(); //smilefish fix memory leak
        super.onDestroy();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
			try {
				Intent intent = new Intent();
                intent.putExtra(MetaData.BOOK_ID, 0L);
                intent.putExtra(MetaData.PAGE_ID, 0L);
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
                break;
        }
        return true;
    }

	//begin:clare
    @Override
	public void onDataChange(int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSelectedDataChange(int count) {
		// TODO Auto-generated method stub
		setSelectionCountTitle(count);
	}

	private void setSelectionCountTitle(int count) {
		// TODO Auto-generated method stub
		String pageNumber = String.format(getActivity().getResources().getString(R.string.pg_title_counter), count);		     
        pageCounterTextView.setText(pageNumber);
        Log.v("Clare________setSelectionCountTitle", pageNumber);
       
	}

	@Override
	public boolean onActionItemClicked(ActionMode arg0, MenuItem arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode arg0, Menu arg1) {
		// TODO Auto-generated method stub	
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
		// TODO Auto-generated method stub
		return false;
	}
//end:clare
}
