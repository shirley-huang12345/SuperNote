package com.asus.supernote.picker;

import android.graphics.Rect;

public class AirViewPolicy {
	public interface IAirViewPoistion{
		int[] getAirViewPoistion(Rect visiableRect,Rect anchorRect,int airViewWidth,int airViewHeight);
	}
	private class GalleryAirViewPolicy implements IAirViewPoistion{

		@Override
		public int[] getAirViewPoistion(Rect visiableRect, Rect anchorRect,int airViewWidth,int airViewHeight) {
			// TODO Auto-generated method stub
			int[] result=new int[]{0,0};
			result[0]=getRealX(visiableRect, anchorRect, airViewWidth);
			result[1]=getRealY(visiableRect, anchorRect, airViewHeight);
			return result;
		}
		private int getRealX(Rect visiableRect,Rect anchorRect,int airViewWidth){
			int middleCompare = visiableRect.centerX()- anchorRect.centerX();
			int poistion=0;
			if (Math.abs(middleCompare)<=10) {
				poistion = 2;// middle
			}else if(middleCompare > 0){
				poistion = 1; // left
			}else {
				poistion = 3; // right
			}
			int retX=anchorRect.left;
			switch (poistion) {
			case 1://left
				if (retX<visiableRect.left) {
					retX=visiableRect.left;
				}
				break;
			case 2:// middle
				retX += (anchorRect.width()-visiableRect.width())/2;
				break;
			case 3:// right
				retX = anchorRect.right;
				if (retX > visiableRect.right) {
					retX = visiableRect.right;
				}
				retX-=airViewWidth;
				break;
			default:
				break;
			}
			return retX;
		}
		private int getRealY(Rect visiableRect,Rect anchorRect,int airViewHeight){
			int retY= anchorRect.top;
			int horizontalPoistion = visiableRect.centerY()-anchorRect.centerY();
			if (horizontalPoistion>0) {
				horizontalPoistion =1;//top 
			}else {
				horizontalPoistion =2; // bottom
			}
			if (horizontalPoistion == 1) {
				if (anchorRect.top<visiableRect.top) {
					retY = visiableRect.top;
				}
			}else {
				retY = anchorRect.bottom;
				if (anchorRect.bottom>visiableRect.bottom) {
					retY = visiableRect.bottom;
				}
				retY-=airViewHeight;
			}
			return retY;
		}
	}
	private GalleryAirViewPolicy mGalleryAirViewPolicy=null;
	public IAirViewPoistion getGalleryAirViewPoistion(){
		if (mGalleryAirViewPolicy == null) {
			mGalleryAirViewPolicy = new GalleryAirViewPolicy();
		}
		return mGalleryAirViewPolicy;
	}
}
