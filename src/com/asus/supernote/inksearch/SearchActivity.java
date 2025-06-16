package com.asus.supernote.inksearch;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.asus.supernote.R;
import com.asus.supernote.classutils.ColorfulStatusActionBarHelper;
import com.asus.supernote.data.MetaData;

public class SearchActivity extends Activity  {
    private SearchResultViewAdapter mAdapter;
    private IndexServiceReceiver mIndexServiceReceiver = null;

    private Boolean mIsPaused = false;
    private DelayCountDownTimer mDelayCountDownTimer = new DelayCountDownTimer(10000,10000);
    private Boolean mIsNewSearch = false;
    private String mQueryString = null;
    private ListView mListView = null; 
    private Long mBookID;
    private SearchView mSearchView = null;
    private MenuItem mSearchItem = null;
    private Handler mHandler = new Handler();
    public class DelayCountDownTimer extends CountDownTimer
    {
    	public Boolean mIsStoped = false;
        public DelayCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        
        public void startCount()
        {
        	if(mIsStoped)
        	{
	        	mIsStoped = false;
	        	start();
        	}
        }
        
        public void cancelCount()
        {
        	mIsStoped = true;
        	cancel();
        }
        
        @Override
        public void onTick(long millisUntilFinished) {
        }
        
        @Override
        public void onFinish() {
        	if(mIsStoped)
        	{
        		return;
        	}
        	else
        	{
        		mIsStoped = true;
    			if(mIsPaused)
    			{
    				mIsNewSearch = true;
    			}else
    			{
    				mAdapter.restartSearch(mQueryString,mBookID);
    			}
        	}
        }
    }
    

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        //Begin: show_wang@asus.com
        //Modified reason: for multi-dpi
		
        // BEGIN: Better
        if (MetaData.AppContext == null) {
    		MetaData.AppContext = getApplicationContext();
		}
        // END: Better
		
		int IsOrientationAllowOrNot = this.getResources().getInteger(R.integer.is_orientation_allow); //by show
        if (IsOrientationAllowOrNot == 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        else  if (IsOrientationAllowOrNot ==1){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
		 else  {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //End: show_wang@asus.com
	    super.onCreate(savedInstanceState);
	
	    // TODO Auto-generated method stub
	    //BEGIN: RICHARD
	    MetaData.SEARCH_TEXT_HEIGH_LIGHT_COLOR = getResources().getColor(R.color.search_text_high_light);
	    //END: RICHARD
	    ColorfulStatusActionBarHelper.setContentView(R.layout.search_activity, true, this);//smilefish
        
        Bundle bundle = getIntent().getExtras();    
        String data = null;
        Long bookId = -1L;
        if(bundle != null)
        	bookId = bundle.getLong("bookId",-1L);
        if(savedInstanceState != null)
        {
        	data = savedInstanceState.getString("searchString");
        }
        setQueryString(data,bookId);
        
        mAdapter = new SearchResultViewAdapter(this, false);
        
        mIndexServiceReceiver = new IndexServiceReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction(MetaData.ANDROID_INTENT_ACTION_INDEXSERVICE_NEWINDEXFILE);
		filter.addAction(MetaData.ANDROID_INTENT_ACTION_INDEXSERVICE_DELETEINDEXFILE);
		filter.addAction(MetaData.ANDROID_INTENT_ACTION_INDEXSERVICE_MOVEPAGE);
		filter.addAction(MetaData.ANDROID_INTENT_ACTION_INDEXSERVICE_DELETEPAGE);
		registerReceiver(mIndexServiceReceiver,filter);
		
        mListView = (ListView) findViewById(R.id.page_list);
        mListView.setAdapter(mAdapter);
	}
	
    public void collapseOrExpandSearchView(boolean flag)
    {
    	if(mSearchItem!= null)
    	{
    		if(flag)
    		{
    			mSearchItem.collapseActionView();
    		}
    		else
    		{
    			mSearchItem.expandActionView();
    		}
    	}
    }
    
    
	
    @Override
    public void onResume() {
        super.onResume();
        mAdapter.changeData();
        mAdapter.notifyDataSetChanged();
        
        if(mQueryString != null && mIsNewSearch)//may need change
        {
        	mIsNewSearch = false;
        	if(mSearchView!= null)
        	{
        		collapseOrExpandSearchView(false);
            	mSearchView.setQuery(mQueryString,false);

        	}
        	mAdapter.startSearch(mQueryString,mBookID);
        	mDelayCountDownTimer.cancelCount();
        	
            mHandler.postDelayed(new Runnable() {//noah:从其他页面过来，也隐藏软键盘
    			
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				hideSoftkeyboard();
    			}
    		}, 100);
        }
        
        mIsPaused = false;
    }
    
    @Override
    public void onPause()
    {
    	mIsPaused = true;
    	super.onPause();
    }
	
	public class IndexServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			mDelayCountDownTimer.startCount();
		}
		public IndexServiceReceiver(){
		}
 
	}
	
	//begin noah;for share
		public boolean isShareMode(){
			Intent intent = getShareIntent();
			if(intent == null)
				return false;
			return true;
		}
		public Intent getShareIntent(){
			Intent extraIntent = getIntent().getParcelableExtra(Intent.EXTRA_INTENT);
			return extraIntent;
		}
		//end noah;for share
	
	@Override
	public void onNewIntent(Intent intent) 
	{
        Bundle bundle = intent.getExtras();    
        String data = null;
        Long bookId = -1L;
        if(bundle != null)
        	bookId = bundle.getLong("bookId",-1L);
		setQueryString(data,bookId);
		
		handleIntent(intent);
	}
	
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
          String query = intent.getStringExtra(SearchManager.QUERY);
	      mQueryString = query;
	      mAdapter.startSearch(query,mBookID);
        }
    }  

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);    
    	outState.putString("searchString", mQueryString);

    }
    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public void onDestroy()
    {
        unregisterReceiver(mIndexServiceReceiver);
		  mAdapter.releaseMemory();
		  mSearchView = null;
		  mListView = null;
    	super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        int menuId = R.menu.search_menu;

        ActionBar bar = getActionBar();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(menuId, menu);
        int option = 0;
        int mask = 0;
        
		option = ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_CUSTOM;
		mask = ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_SHOW_TITLE;
		bar.setDisplayOptions(option, mask);
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setHomeButtonEnabled(true);
            
        //BEGIN: Richard
        mSearchItem = menu.findItem(R.id.menu_search);
        mSearchItem.setVisible(true);
        mSearchItem.expandActionView();
        
        mSearchView = (SearchView) mSearchItem.getActionView();  
        
		//begin smilefish add voice search
		SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
		SearchableInfo info = searchManager.getSearchableInfo(this.getComponentName());
		mSearchView.setSearchableInfo(info);
		//end smilefish
		
        mSearchView.setOnQueryTextListener(queryTextListener);
        mSearchView.setIconifiedByDefault(true);
    	if(mSearchView != null && mQueryString!=null)
    	{
    		mSearchView.setQuery(mQueryString,false);
    		

    	}
        //END: Richard
    	//begin noah;for share;5.15
    	if(isShareMode()){
	    	MenuItem menuItem = menu.findItem(R.id.menu_cancel);
			menuItem.setVisible(true);
    	}
    	//end noah;for share
        return true;
    }
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_cancel:
		case android.R.id.home:
			onBackPressed();
			break;
		default:
			break;
		}
        return true;
    }
    
    public void setQueryString(String searchstring,Long bookId)
    {
 	   mBookID = bookId;
 	   mQueryString = searchstring;
 	   mIsNewSearch = true;
    }
   
    
    final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String newText) {
            // Do something
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
        	hideSoftkeyboard();//noah:用户点击search后，隐藏软键盘
        	mQueryString = query;
        	mAdapter.startSearch(query,mBookID);
            return true;
        }
    };
    //END: Richard
    
    /**
     * 隐藏软键盘
     * @author noah_zhang
     */
    private void hideSoftkeyboard(){
    	if(mListView != null){
	    	InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    	imm.hideSoftInputFromWindow(mListView.getWindowToken(), 0);
    	}
    }
    
    //BEGIN: RICHARD
    void traverseView(View view,int index)
    {
        if (view instanceof SearchView) {
            SearchView v = (SearchView) view;
            for(int i = 0; i < v.getChildCount(); i++) {
                traverseView(v.getChildAt(i), i);
            }
        } else if (view instanceof LinearLayout) {
            LinearLayout ll = (LinearLayout) view;
            for(int i = 0; i < ll.getChildCount(); i++) {
                traverseView(ll.getChildAt(i), i);
            }
        } else if (view instanceof EditText) {
            ((EditText) view).setTextColor(Color.YELLOW);
            ((EditText) view).setHintTextColor(Color.BLUE);
            ((EditText) view).setBackgroundColor(Color.TRANSPARENT);
        } else if (view instanceof TextView) {
            ((TextView) view).setTextColor(Color.YELLOW);
        } else {
            //Log.v("View Scout", "Undefined view type here...");
        }

    }
    //END: RICHARD
}
