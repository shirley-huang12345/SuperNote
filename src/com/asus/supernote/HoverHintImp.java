package com.asus.supernote;

//import com.asus.pen.provider.PenSettings;

import com.asus.supernote.ui.CursorIconLibrary;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;

public class HoverHintImp implements OnHoverListener {
	int mShowRes=-1;
	private boolean entry=false;
	private Context mContext;
	public HoverHintImp(Context context){
		mContext=context;
	}
	@Override
	public boolean onHover(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
//		if (PenSettings.isAirViewEnabled(context)) {
//			
//		}
		switch (arg1.getActionMasked()) {
		case MotionEvent.ACTION_HOVER_ENTER:
			entry=true;
			if (mShowRes!=-1) {
				CursorIconLibrary.setStylusIcon(arg0, mContext.getResources().getDrawable(mShowRes));
			}
			break;
		case MotionEvent.ACTION_HOVER_MOVE:
			if (!entry) {
				CursorIconLibrary.setStylusIcon(arg0, CursorIconLibrary.STYLUS_ICON_FIRST);
				return false;
			}
			break;
		case MotionEvent.ACTION_HOVER_EXIT:
			entry=false;
			CursorIconLibrary.setStylusIcon(arg0, CursorIconLibrary.STYLUS_ICON_FIRST);
			break;
		default:
			break;
		}
		return true;
	}
	public void setHoverImageRes(int res){
		mShowRes=res;
	}
}
