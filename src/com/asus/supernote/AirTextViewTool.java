package com.asus.supernote;

import com.asus.supernote.classutils.MethodUtils;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnHoverListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
/***
 * 
 * @author Jason
 *
 */
public class AirTextViewTool implements OnHoverListener, OnTouchListener {

	private View mDownTouchView=null;
	private Context mContext =null;
	private TextView mContentShow=null;
	private PopupWindow mContentPopupWindow=null;
	private View mCurrHoverView=null;
	private String mContentString="";
	private View layout;
//	private LayoutInflater	mLayoutInflater=null;
	public AirTextViewTool(Context context){
		mContext = context;
//		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = LayoutInflater.from(context).inflate(R.layout.asus_air_view_hint, null);
		layout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		mContentShow = (TextView) layout.findViewById(R.id.air_text);
		mContentPopupWindow=new PopupWindow(layout,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,false);
	}
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		switch (arg1.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			mCurrHoverView = null;
			mDownTouchView = arg0;
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onHover(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		switch (arg1.getActionMasked()) {
		case MotionEvent.ACTION_HOVER_ENTER:
			if (!MethodUtils.isEnableAirViewActionBarHint(mContext)) {
				return false;
			}
			if (mDownTouchView!=null&&(mDownTouchView.getId() == arg0.getId())) {// stop once for click the view
				mDownTouchView = null;
				return false;
			}
			if (arg0 instanceof TextView) {
				mCurrHoverView = arg0;
				mContentString=((TextView)arg0).getText().toString();
				mHandler.sendMessageDelayed(mHandler.obtainMessage(SHOW_HINT, arg0), 300);
			}
			break;
		case MotionEvent.ACTION_HOVER_EXIT:
			hideHint(arg0);
			break;
		default:
			break;
		}
		return false;
	}
	private static final int SHOW_HINT=1;
	private static final int HIDE_HINT=2;
	private Handler mHandler =new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SHOW_HINT:
				showHint((View)msg.obj);
				break;
			case HIDE_HINT:
				break;
			default:
				break;
			}
		};
	};
	private void showHint(View showForView){
		if (mCurrHoverView == null || mContentString.equals("")) {
			return;
		}else if(mCurrHoverView.getId()!=showForView.getId()) {
			mDownTouchView = null;
			return;
		}
		if (mDownTouchView!= null) {
			if (showForView.getId()==mDownTouchView.getId()) {// cancel once
				mDownTouchView = null;
				return;
			}
		}
		mDownTouchView = null;
		if (mContentPopupWindow.isShowing()) {
			if (showForView.getId()==mCurrHoverView.getId()) {// the same View
				return ;
			}
			hideHint(null);
		}
		mContentShow.setText(mContentString);
		layout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		int[] offset=getRightHintPoistion(mContext,showForView,mContentShow.getMeasuredWidth());
		
//		mContentPopupWindow.showAsDropDown(showForView);
//		mContentPopupWindow.showAtLocation(showForView, Gravity.NO_GRAVITY, offset[0], offset[1]);
		mContentPopupWindow.showAsDropDown(showForView, offset[0], offset[1]);
	}
	private void hideHint(View v){
		if (v!=null) {
			if (mCurrHoverView!=null) {
				if (mCurrHoverView.getId()==v.getId()) {
					mCurrHoverView = null;
				}
			}
		}
//		if (mContentShow!=null) {
//			try {
//				mWindowManager.removeView(mContentShow);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			mContentShow = null;
//		}
		if (mContentPopupWindow.isShowing()) {
			mContentPopupWindow.dismiss();
		}
	}
	public static int[] getRightHintPoistion(Context context,View anchor,int contentWidth){
		int[] offest=new int[]{0,0};
		if (anchor==null||context == null) {
			return offest;
		}
		final float density =context.getResources().getDisplayMetrics().density;
		final int dp_4=(int)(4*density+0.5);
		int[] sLocation = new int[2];
		anchor.getLocationOnScreen(sLocation);
		final int anchorWidth=anchor.getMeasuredWidth();
		Rect frameRect=new Rect();
		anchor.getWindowVisibleDisplayFrame(frameRect);
		offest[0]=(anchorWidth-contentWidth)/2;
		final int hintLeft = sLocation[0]+offest[0];
		final int hintRight=sLocation[0]+offest[0]+contentWidth;
		if (hintLeft<(frameRect.left+dp_4)) {
			offest[0]=frameRect.left+dp_4-sLocation[0];
		}else if (hintRight>(frameRect.right-dp_4)) {
			offest[0]-=hintRight-(frameRect.right-dp_4);
		}
		offest[1]=-dp_4;
		if(sLocation[1]+context.getResources().getInteger(R.integer.airtext_max_bottom_margin)>frameRect.bottom){
			offest[1] = offest[1]-context.getResources().getInteger(R.integer.airtext_totop_offset);
		}
		return offest;
	}
	public static int[] getRightHintPoistion(Context context,View anchor,int contentWidth,Rect visiableFrame){
		int[] offest=new int[]{0,0};
		if (anchor==null||context == null) {
			return offest;
		}
		final float density =context.getResources().getDisplayMetrics().density;
		final int dp_4=(int)(4*density+0.5);
		int[] sLocation = new int[2];
		anchor.getLocationOnScreen(sLocation);
		final int anchorWidth=anchor.getMeasuredWidth();
		Rect frameRect=new Rect(visiableFrame);
		offest[0]=(anchorWidth-contentWidth)/2;
		final int hintLeft = sLocation[0]+offest[0];
		final int hintRight=sLocation[0]+offest[0]+contentWidth;
		if (hintLeft<(frameRect.left+dp_4)) {
			offest[0]=frameRect.left+dp_4-sLocation[0];
		}else if (hintRight>(frameRect.right-dp_4)) {
			offest[0]-=hintRight-(frameRect.right-dp_4);
		}
		offest[1]=-dp_4;
		return offest;
	}
}
