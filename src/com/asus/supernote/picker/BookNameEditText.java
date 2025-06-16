package com.asus.supernote.picker;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;

public class BookNameEditText extends EditText{
	private LinearLayout mNameLayout = null;
	private int originalMarginTop = 0;
	private int marginTop = 0;
	
	public BookNameEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		marginTop = context.getResources().getDimensionPixelSize(R.dimen.addbook_dialog_name_margin_top);
        MetaData.checkRTL(this);
	}
	
	public void setBookNameLayout(LinearLayout layout){
		mNameLayout = layout;
		android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams)mNameLayout.getLayoutParams();
		originalMarginTop = params.topMargin;
	}

	@Override
	protected void onSelectionChanged(int selStart, int selEnd){
		super.onSelectionChanged(selStart, selEnd);
		if(mNameLayout != null){
			android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams)mNameLayout.getLayoutParams();
			int topMargin = params.topMargin;
			if(selStart == selEnd){
				params.topMargin = originalMarginTop;
			}else{
				params.topMargin = marginTop;
			}
			
			if(params.topMargin != topMargin) //smilefish fix bug 655948/656066
				mNameLayout.setLayoutParams(params);
		}
	}
}
