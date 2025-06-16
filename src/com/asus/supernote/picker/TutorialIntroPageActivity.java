package com.asus.supernote.picker;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TutorialIntroPageActivity extends Activity implements OnTouchListener{
	private static final float LIMIT_ANGLE_TAN = 1.5f;
	
	private ImageView mIndicator1;
	private ImageView mIndicator2;
	private ImageView mIndicator3;
	private ImageView mIndicator4;
	private ImageView mAppIcon;
	private ImageView mTutorialImage;
	private TextView mTutorialTitle;
	private TextView mTutorialContent;
	private Button mSkipTutorialButton;
	private Button mDoneButton;
	private GestureDetector mDetector = null;
	private int mStep = 0;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tutorial_intro_page);
        
        mIndicator1 = (ImageView)findViewById(R.id.tutorial_indicator1);
        mIndicator2 = (ImageView)findViewById(R.id.tutorial_indicator2);
        mIndicator3 = (ImageView)findViewById(R.id.tutorial_indicator3);
        mIndicator4 = (ImageView)findViewById(R.id.tutorial_indicator4);
        
        mAppIcon = (ImageView)findViewById(R.id.tutorial_app_icon);
        mTutorialImage = (ImageView)findViewById(R.id.image_tutorial);
        mTutorialTitle = (TextView)findViewById(R.id.tutorial_title);
        mTutorialContent = (TextView)findViewById(R.id.tutorial_content);
        mSkipTutorialButton = (Button) findViewById(R.id.skip_button);
        mDoneButton = (Button) findViewById(R.id.done_button);
        mSkipTutorialButton.setOnClickListener(mOnDoneButtonClickListener);
        mDoneButton.setOnClickListener(mOnDoneButtonClickListener);
        
        mDetector = new GestureDetector(this, new TutorialImageGesture());
        mTutorialImage.setOnTouchListener(this);
        
        if(savedInstanceState != null)
        {
        	mStep = savedInstanceState.getInt("pageStep");
        }
        if(MetaData.isATT()){
        	mIndicator2.setVisibility(View.GONE);
        }
        
        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        
        switch(mStep){
        case 0:
        	break;
        case 1:
        	mIndicator1.setBackgroundResource(R.drawable.asus_tutorial_indicator_off);
			mIndicator2.setBackgroundResource(R.drawable.asus_tutorial_indicator_on);
			if(isLandscape)
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_02_l);
			else
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_02);
			mTutorialTitle.setText(R.string.tutorial_info_title2);
			mTutorialContent.setText(R.string.tutorial_info_content2);
			mAppIcon.setVisibility(View.GONE);
        	break;
        case 2:
        	mIndicator1.setBackgroundResource(R.drawable.asus_tutorial_indicator_off);
			mIndicator3.setBackgroundResource(R.drawable.asus_tutorial_indicator_on);
			if(isLandscape)
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_03_l);
			else
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_03);
			mTutorialTitle.setText(R.string.tutorial_info_title3);
			mTutorialContent.setText(R.string.tutorial_info_content3);
			mAppIcon.setVisibility(View.GONE);
        	break;
        case 3:
        	mIndicator1.setBackgroundResource(R.drawable.asus_tutorial_indicator_off);
			mIndicator4.setBackgroundResource(R.drawable.asus_tutorial_indicator_on);
			if(isLandscape)
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_04_l);
			else
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_04);
			mTutorialTitle.setText(R.string.tutorial_info_title4);
			mTutorialContent.setText(R.string.tutorial_info_content4);
			mSkipTutorialButton.setVisibility(View.GONE);
			mDoneButton.setVisibility(View.VISIBLE);
			mAppIcon.setVisibility(View.GONE);
        	break;
        }
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);    
    	outState.putInt("pageStep", mStep);
    }
    
    private void showPrePage(){
		boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		if(MetaData.isATT() && mStep == 2){ //remove cloud sync page
			mStep--;
		}
        if(mStep == 3){
			mIndicator3.setBackgroundResource(R.drawable.asus_tutorial_indicator_on);
			mIndicator4.setBackgroundResource(R.drawable.asus_tutorial_indicator_off);
			if(isLandscape)
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_03_l);
			else
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_03);
			mTutorialTitle.setText(R.string.tutorial_info_title3);
			mTutorialContent.setText(R.string.tutorial_info_content3);
			mSkipTutorialButton.setVisibility(View.VISIBLE);
			mDoneButton.setVisibility(View.GONE);
        	mStep--;
        }else if(mStep == 2){
			mIndicator2.setBackgroundResource(R.drawable.asus_tutorial_indicator_on);
			mIndicator3.setBackgroundResource(R.drawable.asus_tutorial_indicator_off);
			if(isLandscape)
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_02_l);
			else
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_02);
			mTutorialTitle.setText(R.string.tutorial_info_title2);
			mTutorialContent.setText(R.string.tutorial_info_content2);
        	mStep--;
        }else if(mStep == 1){
        	if(MetaData.isATT()){
        		mIndicator3.setBackgroundResource(R.drawable.asus_tutorial_indicator_off);
        	}
			mIndicator1.setBackgroundResource(R.drawable.asus_tutorial_indicator_on);
			mIndicator2.setBackgroundResource(R.drawable.asus_tutorial_indicator_off);
			if(isLandscape)
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_01_l);
			else
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_01);
			mTutorialTitle.setText(R.string.app_name);
			mTutorialContent.setText(R.string.tutorial_info_content1);
			mAppIcon.setVisibility(View.VISIBLE);
        	mStep--;
        }
    }
    
    private void showNextPage(){
		boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		if(MetaData.isATT() && mStep == 0){ //remove cloud sync page
			mStep++;
		}
		if(mStep == 0){
			mIndicator1.setBackgroundResource(R.drawable.asus_tutorial_indicator_off);
			mIndicator2.setBackgroundResource(R.drawable.asus_tutorial_indicator_on);
			if(isLandscape)
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_02_l);
			else
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_02);
			mTutorialTitle.setText(R.string.tutorial_info_title2);
			mTutorialContent.setText(R.string.tutorial_info_content2);
			mAppIcon.setVisibility(View.GONE);
			mStep++;
		}else if(mStep == 1){
			if(MetaData.isATT()){
				mIndicator1.setBackgroundResource(R.drawable.asus_tutorial_indicator_off);
			}
			mIndicator2.setBackgroundResource(R.drawable.asus_tutorial_indicator_off);
			mIndicator3.setBackgroundResource(R.drawable.asus_tutorial_indicator_on);
			if(isLandscape)
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_03_l);
			else
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_03);
			mTutorialTitle.setText(R.string.tutorial_info_title3);
			mTutorialContent.setText(R.string.tutorial_info_content3);
			mStep++;
		}else if(mStep == 2){
			mIndicator3.setBackgroundResource(R.drawable.asus_tutorial_indicator_off);
			mIndicator4.setBackgroundResource(R.drawable.asus_tutorial_indicator_on);
			if(isLandscape)
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_04_l);
			else
				mTutorialImage.setBackgroundResource(R.drawable.supernote_tutoriall_04);
			mTutorialTitle.setText(R.string.tutorial_info_title4);
			mTutorialContent.setText(R.string.tutorial_info_content4);
			mSkipTutorialButton.setVisibility(View.GONE);
			mDoneButton.setVisibility(View.VISIBLE);
			mStep++;
		}
    }

	private OnClickListener mOnDoneButtonClickListener = new OnClickListener() {

		@Override
        public void onClick(View arg0) {
			finish();
        }
		
	};
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_OK); //smilefish fix bug 663498
		
		super.onBackPressed();
	}
	
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	mDetector.onTouchEvent(event);
        return true;
    }
	
    public class TutorialImageGesture implements OnGestureListener {

		@Override
        public boolean onDown(MotionEvent arg0) {
	        // TODO Auto-generated method stub
	        return false;
        }

		@Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	float ver = Math.abs(e1.getY() - e2.getY());
        	float hor = Math.abs(e1.getX() - e2.getX());
                if ( ver / hor > LIMIT_ANGLE_TAN || Math.abs(velocityX)<500) {
                    return false;
                }

                if (e2.getX() - e1.getX() < 0) {
                	showNextPage();
                }
                else {
                	showPrePage();
                }
                return true;
        }

		@Override
        public void onLongPress(MotionEvent arg0) {
	        // TODO Auto-generated method stub
	        
        }

		@Override
        public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
                float arg3) {
	        // TODO Auto-generated method stub
	        return false;
        }

		@Override
        public void onShowPress(MotionEvent arg0) {
	        // TODO Auto-generated method stub
	        
        }

		@Override
        public boolean onSingleTapUp(MotionEvent arg0) {
	        // TODO Auto-generated method stub
	        return false;
        }
    	
    }

	@Override
    public boolean onTouch(View v, MotionEvent event) {
	    // TODO Auto-generated method stub
	    return false;
    }
}
