package com.asus.supernote.picker;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import com.asus.supernote.R;
import com.asus.supernote.classutils.ColorfulStatusActionBarHelper;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.MetaData;

public class FavoriteActivity extends Activity {
    public static final String TAG = "FavoriteActivity";

    private FavoritesPageFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) { 	
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
        ColorfulStatusActionBarHelper.setContentView(R.layout.favorite_layout, true, this);//smilefish

        getActionBar().setDisplayHomeAsUpEnabled(true);
        mFragment = new FavoritesPageFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.favorite_layout, mFragment,FavoritesPageFragment.TAG);
        ft.commit();       
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
		//smilefish add for runtime permission
        if(MethodUtils.needShowPermissionPage(getApplicationContext())){
			MethodUtils.showPermissionPage(this, false);
		}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    //darwin
    @Override
	public void onBackPressed() {
    	Intent intent = new Intent();
        intent.putExtra(MetaData.BOOK_ID, 0L);
        intent.putExtra(MetaData.PAGE_ID, 0L);
        setResult(Activity.RESULT_OK, intent);
        finish();
	}
    //darwin
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		ColorfulStatusActionBarHelper.setContentView(R.layout.favorite_layout, true, this);//smilefish

		getActionBar().setDisplayHomeAsUpEnabled(true);
		FragmentManager fm = getFragmentManager();
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		mFragment = (FavoritesPageFragment) fm.findFragmentByTag(FavoritesPageFragment.TAG);
		if(mFragment!=null)
		{
			fragmentTransaction.detach(mFragment);
			fragmentTransaction.commit();			
		}else
		{
			 mFragment = new FavoritesPageFragment();
		}
		FragmentTransaction reattach = getFragmentManager().beginTransaction();
		reattach.attach(mFragment);
		reattach.commit();
		
    }
    
    @Override
	protected void onSaveInstanceState(Bundle outState) {

	}

}
