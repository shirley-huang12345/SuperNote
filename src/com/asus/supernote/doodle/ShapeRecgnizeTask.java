package com.asus.supernote.doodle;


import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo;
import com.asus.supernote.doodle.drawinfo.SpotDrawInfo;
import com.asus.supernote.inksearch.AsusShape;
import com.visionobjects.myscript.shape.ShapeDocument;

import android.app.ProgressDialog;

import android.os.AsyncTask;


	public class ShapeRecgnizeTask extends AsyncTask<Void, Void, Void> {
	    public static final String TAG = "ShapeRecgnizeTask";
	    private ProgressDialog mProgressDialog;
	    private DoodleView.ShapeRecgnizeCountdownCounter mShapeRecgnizeCountdownCounter;
	    private ShapeDocument shapedocument = null;
	    DoodleView mDoodleView = null;
	    private AsusShape mAsusShape = null;
	    private DrawInfo mDrawInfo = null;
	    public ShapeRecgnizeTask(DoodleView doodleView ,AsusShape asusShape,DrawInfo drawInfo) {
	    	mDoodleView = doodleView;
	    	mAsusShape = asusShape;
	    	mDrawInfo = drawInfo;
	    }
	    
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();


	    }
	    
	    public void setDialog(ProgressDialog d) {
	        mProgressDialog = d;
	    }
	    public void setCounter(DoodleView.ShapeRecgnizeCountdownCounter c) {
	    	mShapeRecgnizeCountdownCounter = c;
	    }
	    
		@Override
		protected Void doInBackground(Void... arg0) {
			if(mDrawInfo instanceof PathDrawInfo)
			{
				PathDrawInfo temp = (PathDrawInfo)mDrawInfo;
				if(mAsusShape != null)
		    	{
		    		mAsusShape.addStroke(temp.getPointArray());
		    	}
			}
			else if(mDrawInfo instanceof SpotDrawInfo){ // carrot: enable shape recognize for new brushes
				SpotDrawInfo temp = (SpotDrawInfo)mDrawInfo;
				if(mAsusShape != null)
		    	{
		    		mAsusShape.addStroke(temp.getPointArray());
		    	}
			}
			else
			{
				return null;
			}
			if(mAsusShape != null)//darwin
	    	{
				shapedocument = mAsusShape.getResultShapeDocument();
	    	}
			publishProgress();
			return null;
		}
		
		@Override
	    protected void onProgressUpdate(Void... values) {
	        super.onProgressUpdate(values);
	        if (mProgressDialog != null && mProgressDialog.isShowing()) {
	            mProgressDialog.setProgress(1);
	        }

	    }

	    @Override
	    protected void onPostExecute(Void result) {
	        super.onPostExecute(result);
	        
			if(shapedocument == null)
	    	{
				mProgressDialog.dismiss();
				mShapeRecgnizeCountdownCounter.setNotShow();
	    		return;
	    	}
	    	try
	    	{
	    		if(mDoodleView == null)
		    	{
	    			mProgressDialog.dismiss();
	    			mShapeRecgnizeCountdownCounter.setNotShow();
		    		return;
		    	}
	    		mDoodleView.saveAction(DoodleView.ActionRecord.ACTION_SHAPE,(ShapeDocument)shapedocument.clone());
	    		mDoodleView.doDrawShapeDocument(shapedocument);
	    		mDoodleView.drawScreen();
		    }catch(Exception e)
			{
				e.printStackTrace();
				mProgressDialog.dismiss();
				mShapeRecgnizeCountdownCounter.setNotShow();
				return;
			}
	    	
	    	mProgressDialog.dismiss();
	    	mShapeRecgnizeCountdownCounter.setNotShow();
	        
	    }
}
