package com.asus.supernote.picker;

import java.util.LinkedList;
import java.util.List;

import com.asus.supernote.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

public class SelectionMenu implements OnClickListener{
    private final Context mContext;
    private final Button mButton;
    private PopupWindow mPopupWindow = null;
    private TextView mSelectText = null;
    private boolean isSelectAll = false;
    
	public SelectionMenu(Context context, Button button){
        mContext = context;
        mButton = button;
        mButton.setOnClickListener(this);
        CreatePopupWindow();
	}
	
	private void CreatePopupWindow(){
		if(mPopupWindow == null)
		{
			View view = LayoutInflater.from(mContext).inflate(R.layout.simple_dropdown_menu_item, null);
			mSelectText = (TextView)view.findViewById(R.id.select_text);
			mSelectText.setText(mContext.getString(R.string.pg_select_all));
			mSelectText.setOnClickListener(selectMenuClickListener);
			
			mPopupWindow = new PopupWindow(view,
					LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT,
                    false );
			
			mPopupWindow.setFocusable(true);
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.setTouchable(true);
			mPopupWindow.setTouchInterceptor(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                        if (mPopupWindow != null && mPopupWindow.isShowing()) {
                        	mPopupWindow.dismiss();
                        }
                        return true ;
                    }

                    return false ;
                }
            });
		}
	}

	@Override
	public void onClick(View arg0) {	
		if(mPopupWindow != null){
			mPopupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(android.R.drawable.spinner_dropdown_background));
			mPopupWindow.showAsDropDown(mButton,0,0);
		}
	}
	
	private final OnClickListener selectMenuClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if(isSelectAll){
				isSelectAll = false;
				for(INotifyOuter outer:outers)
					outer.setSelectAll(isSelectAll);
			}else{
				isSelectAll = true;
				for(INotifyOuter outer:outers)
					outer.setSelectAll(isSelectAll);
			}
		}
		
	};
	
    public void updateSelectAllMode(boolean isSelectAllMode) {
    	isSelectAll = isSelectAllMode;
        if (mSelectText != null) {
        	mSelectText.setText(mContext.getString(
        			isSelectAllMode ? R.string.pg_deselect_all : R.string.pg_select_all));
        }
        //emmanual
        hidePopupWindow();
    }
	
    public void setTitle(CharSequence title) {
        mButton.setText(title);
    }
    
    public void hidePopupWindow() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
        	mPopupWindow.dismiss();
        }
	}

	private List<INotifyOuter> outers=new LinkedList<INotifyOuter>();
	public void addOuterListener(INotifyOuter outer){
		outers.add(outer);
	}
	public interface INotifyOuter{
		void setSelectAll(boolean flag);
	}
    
}
