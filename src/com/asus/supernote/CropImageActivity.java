//BEGIN:Show
package com.asus.supernote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.asus.supernote.classutils.ColorfulStatusActionBarHelper;
import com.asus.supernote.data.MetaData;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.graphics.Path;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CropImageActivity extends Activity {
    /** Called when the activity is first created. */
	private Button rectBtn, freeBtn; //smilefish
	private String cropImage ;
	private String mFileName ;
	private boolean rectStyle = false, isFirst = false;
	private MyView myView = null;
	private Context mContext = null;
	//Begin Darwin_Yu@asus.com
	private String mOriginalFileName;
	//End   Darwin_Yu@asus.com
	private int deviceType;
	
	private boolean IsRun = false;
	private boolean mIsConfig = false;//by show
	public static final int SAVING_PROGRESS_DIALOG = 1;
	private Path mPath = new Path();//by show
	private Matrix mTempMatrix;//by show
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);//darwin
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
	public Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
			case SAVING_PROGRESS_DIALOG: {
				ProgressDialog progressDialog = new ProgressDialog(mContext);
	 			progressDialog.setMessage(mContext.getString(R.string.prompt_saving)); 
	 			progressDialog.setCanceledOnTouchOutside(false);
	 			progressDialog.setCancelable(false);
	 			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				return progressDialog;
			}
		}
		return null;
    }
    
    //darwin
    @Override
   	public void onBackPressed() {
    	setResult(RESULT_CANCELED);
		finish();
   	}
    //darwin
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = this;
    	
        super.onCreate(savedInstanceState);
        
        //Begin: show_wang@asus.com
        //Modified reason: for dds
        try{
        	
        Path path = (Path) getLastNonConfigurationInstance();
        if (path != null) {
        	mPath = path;
        	mIsConfig = true;
        }
        if (savedInstanceState != null) {
        	rectStyle = savedInstanceState.getBoolean("crop_style");
        }
        //End: show_wang@asus.com
        // BEGIN: Better
        if (MetaData.AppContext == null) {
    		MetaData.AppContext = getApplicationContext();
		}
        // END: Better
        ColorfulStatusActionBarHelper.setContentView(R.layout.cropimage, true, this);//smilefish
        //setContentView(R.layout.cropimage);
        LinearLayout viewcontainer = (LinearLayout)findViewById(R.id.Imagelayout);
        myView = new MyView(this);
        if(myView==null||myView.CreateBitmapFailed==true){//Allen
        	setResult(RESULT_CANCELED);
			finish();
    		EditorActivity.showToast(EditorUiUtility.getContextStatic(), R.string.wrong_file_type);
			return;
        }
        viewcontainer.addView(myView);
        
        //Begin: show_wang@asus.com
        //Modified reason: for dds
        if (savedInstanceState != null) {
        	myView.isCrop = savedInstanceState.getBoolean("is_crop");
        }
        //End: show_wang@asus.com

        deviceType = getResources().getInteger(R.integer.device_type);//Siupo
        
        //begin smilefish

    	ActionBar actionBar = getActionBar();
    	actionBar.setDisplayShowHomeEnabled(true);
    	actionBar.setDisplayHomeAsUpEnabled(true);
        
    	Button doneBtn = (Button)findViewById(R.id.crop_image_ok);	
        doneBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				 //TODO Auto-generated method stub
				//no area is selected 
				if (!myView.isCrop )
				{
					setResult(RESULT_CANCELED);
					finish();
				}			
				else
				{
			        if (!IsRun) {
				        IsRun = true;	
				        SavePageTask task = new SavePageTask(mContext);

				        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			        }
				}
			}
		}); 
        
        Button clearBtn = (Button)findViewById(R.id.crop_image_cancel);
        //clearBtn.setOnLongClickListener(mFuncIconLongClickListener);
        clearBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setResult(RESULT_CANCELED);
				finish();
			}
		});  
        
        //end smilefish
        
        rectBtn = (Button)findViewById(R.id.rect);
        rectBtn.setOnLongClickListener(mFuncIconLongClickListener);
        rectBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!rectStyle)
				{
					rectStyle = true;
					isFirst = true;
					myView.clearAll();
				}
				freeBtn.setSelected(false);
				v.setSelected(true);
			}
		}); 
        
        freeBtn = (Button)findViewById(R.id.free);
		freeBtn.setOnLongClickListener(mFuncIconLongClickListener);
        freeBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(rectStyle)
				{
					rectStyle = false;
					myView.clearAll();
				}
				rectBtn.setSelected(false);
				v.setSelected(true);
			}
		});
        
        //Begin: show_wang@asus.com
        //Modified reason: for dds
        if (rectStyle) {
        	rectBtn.setSelected(true);
        	isFirst = false;
        } else { 
    		freeBtn.setSelected(true);
        }
        //End: show_wang@asus.com
        
        //Begin Allen++ for template
        if(getIntent().hasExtra("IsTemplate"))
        {
        	freeBtn.setVisibility(View.GONE);
        	rectBtn.setVisibility(View.GONE);
        	if(!rectStyle)
			{
				rectStyle = true;
				isFirst = true;
				myView.clearAll();
			}
			freeBtn.setSelected(false);
        }
        //End Allen++

		} catch (OutOfMemoryError err) {
			//emmanual to fix bug 579037, 579254
			Intent intent = new Intent();
			intent.putExtra("OutOfMemoryError", true);
			setResult(RESULT_CANCELED , intent);
			this.finish();
		}
    }
	
    //Begin: show_wang@asus.com
    //Modified reason: for dds
    @Override
    public Path onRetainNonConfigurationInstance() {
        if (mTempMatrix != null) {
        	mPath.transform(mTempMatrix);//by show, save path
        }
        return mPath;
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("crop_style", rectStyle); 
		if (myView != null) {
			outState.putBoolean("is_crop", myView.isCrop);
		}
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
  //End: show_wang@asus.com    
    
    public class MyView extends View{
    	Bitmap mBitmap;
    	private Paint mPaint, mPaintline;
		private Region re=new Region();
    	float startx,starty,minx,miny,maxx,maxy;
    	float dx,dy,lastx, lasty, fx, fy, flastx, flasty;
    	boolean isCrop = false; 
    	private String mFilePath, mFile;   	
    	private int width, height;
    	private float max, scaleTemp, scaleTempNew; 
    	private Matrix matrix;
    	private DisplayMetrics displayMatrix;
    	private float scalex = 0;
    	private float scaley = 0;
    	private float ViewWidth, ViewHeight;
    	private boolean isMove = false, isGrow = false;
    	private RectF moveRect = new RectF(), growRect = new RectF();
        private Drawable mResizeDrawableWidth;
        private Drawable mResizeDrawableHeight;
        private boolean isTemplate = false;//Allen++ for template
        private float rate = (float)1164 / 328;//Allen++
    	public boolean CreateBitmapFailed = false;//Allen indicate image format error
        private boolean isReset = false; //Modified by show, reset crop region
    	
    	public MyView(Context context){
            super(context);
            init();
        }
    	
    	@Override
    	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    		setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
    	}
        
        public void init(){       	
        	//Synonym for opening the given resource 
        	//Begin Allen++ for template
        	if(getIntent().hasExtra("IsTemplate"))
        	{
        		isTemplate = true;
        	}
        	if(getIntent().hasExtra("IsPhoneSizePage")){
        		rate = getResources().getDimension(R.dimen.diary_crop_image_rate_phone);
        	}else{
        		rate = getResources().getDimension(R.dimen.diary_crop_image_rate_pad);
        	}
        	//End Allen++
        	mFilePath = getIntent().getStringExtra("filePath");
        	mFileName = getIntent().getStringExtra("fileName");

			//Begin Darwin_Yu@asus.com
			mOriginalFileName = mFileName;
			//End   Darwin_Yu@asus.com
        	mFile = mFilePath + "/"+mFileName;
        	mBitmap = BitmapFactory.decodeFile(mFile);
        	
        	//emmanual to fix bug 457423,458949
        	int degree = readPictureDegree(mFile);
        	if(degree != 0){
        		mBitmap = rotaingImageView(degree, mBitmap);
        	}
        	
        	if(mBitmap==null){//Allen
        		CreateBitmapFailed = true;
        		return;
        	}
        	width = mBitmap.getWidth();
	        height = mBitmap.getHeight();
	        //get the size of screen 
	    	displayMatrix = new DisplayMetrics();    	 
	        getWindowManager().getDefaultDisplay().getMetrics(displayMatrix);
	        ViewWidth = displayMatrix.widthPixels;
	        int outViewHeight=mContext.getResources().getInteger(R.integer.cropimage_out_view_height);
	        ViewHeight = displayMatrix.heightPixels - outViewHeight;
	        // enlarge the image
        	mTempMatrix = new Matrix();//by show
	        if(width < 512 && height < 512)
	        { 
	        	if(width > height){
	            	max = width;
	            }
	            else
	            	max = height;
	        	matrix = new Matrix();
	        	matrix.setScale((float)512/max, (float)512/max);
	        	mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, true);
	        	mTempMatrix.setScale((float)max/512, (float)max/512); 
	        }
	        
	        // reduce the size of the image
	        if(width > ViewWidth || height > ViewHeight)
	        {
		        if(width > ViewWidth )
		        {
		        	scalex = width / ViewWidth ; 
		        }
		        if(height > ViewHeight )
		        {
		        	scaley = height / ViewHeight; 
		        }
		        if(scalex > scaley )
		        {
		        	scaleTemp = ViewWidth / width;
		        	scaleTempNew = scalex;
		        	
		        }
		        else
		        {
		        	scaleTemp = ViewHeight / height;
		        	scaleTempNew = scaley;
		        }
		        
		        matrix = new Matrix();
		        
		        //smilefish fix bug 674902
		        if(scaleTemp * width <= 1){ 
		        	matrix.setScale(1, scaleTemp);
		        }else if(scaleTemp * height <=1){
		        	matrix.setScale(scaleTemp, 1);
		        }else{
		        	matrix.setScale(scaleTemp, scaleTemp);
		        }
	        	mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, true);
	        	mTempMatrix.setScale(scaleTempNew, scaleTempNew);//by show
	        }
        	mPaint = new Paint();
        	mPaintline = new Paint();
        	mPaintline.setStyle(Paint.Style.STROKE);  
        	mPaintline.setStrokeWidth(3);
        	mPaintline.setColor(0xFF3679bd);
        	//Set the alpha component [0..255] of the paint's color. Set the transparency of images. 
            mPaint.setAlpha(62);
            mPaint.setAntiAlias(true);
            mPaint.setPathEffect(new CornerPathEffect(5));
            mResizeDrawableWidth =
                    this.getResources().getDrawable(R.drawable.crop_width);
            mResizeDrawableHeight =
            		this.getResources().getDrawable(R.drawable.crop_height);
			//Begin: show_wang@asus.com
            //Modified reason: save path
            if (mIsConfig) {
            	if ( matrix != null ) {
            		mPath.transform(matrix);
            	}
				if (rectStyle) {
					RectF bounds = new RectF();
		        	mPath.computeBounds(bounds, true);
		        	createRect(bounds);
				}else {
					setFreeCropRegion();
				}
				
			}
            //End: show_wang@asus.com
        }
        
        @Override
        public void onDraw(Canvas canvas){
        	//Draw the picture 
        	canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        	Paint paint = new Paint();
        	paint.setColor(Color.BLACK);
        	paint.setStrokeWidth(1);
        	paint.setStyle(Paint.Style.STROKE);   
        	//Modify the canvas
     		canvas.save();
        	if(canvas != null){
        		// if chose rectcrop draw a init rect
        		if ( rectStyle )
        		{
        			if ( isFirst )
        			{
	        			float moveWidth = mBitmap.getWidth();
	        			float moveHeight = mBitmap.getHeight();
	        			RectF cropRect = null;
	        			//Begin Allen++ for template
	        			if(isTemplate)
	        			{
	        				float top,bottom,left,right;
	        				if(moveWidth/moveHeight<rate)
	        				{
	        					float newHeight=(moveWidth-5)/rate;
	        					top = (moveHeight-newHeight)/2;
	        					bottom = (moveHeight+newHeight)/2;
	        					left=2;
	        					right=moveWidth - 3;
	        				}
	        				else{
	        					float newWidth = (moveHeight-5)*rate;
	        					left = (moveWidth-newWidth)/2;
	        					right = (moveWidth+newWidth)/2;
	        					top=2;
	        					bottom = moveHeight-3;
	        				}
	        				
		        			cropRect = new RectF(left, top,
		        					right, bottom);
		        			moveRect = new RectF(left + 30, top + 30,
		        					right - 30, bottom - 30);
		        			growRect = new RectF(left - 30 , top - 30,
		        					right + 30, bottom + 30);
	        			}
	        			else
	        			{
	        				cropRect = new RectF(moveWidth/4, moveHeight/4,
		        					moveWidth/4*3, moveHeight/4*3);
		        			moveRect = new RectF(moveWidth/4 + 30, moveHeight/4 + 30,
		        					moveWidth/4*3 - 30, moveHeight/4*3 - 30);
		        			growRect = new RectF(moveWidth/4 - 30 , moveHeight/4 - 30,
		        					moveWidth/4*3 + 30, moveHeight/4*3 + 30);
	        			}	 
	        			//End Allen++

	        			if ( moveRect.isEmpty())
	        			{
	        				moveRect = cropRect;
	        			}
	        			if ( growRect.isEmpty())
	        			{
	        				growRect = cropRect;
	        			}
	        			minx = cropRect.left;
	        			maxx = cropRect.right;
	        			miny = cropRect.top;
	        			maxy = cropRect.bottom;
	        			mPath.addRect(cropRect, Path.Direction.CW);
	        			isFirst = false;
	        			isCrop = true;
        			}
        			else if ( isMove || isGrow )
            		{
            			canvas.clipPath(mPath);
            		}
        		}
        		if (isCrop)
        		{
        			canvas.clipPath(mPath);
        		}
            	canvas.drawBitmap(mBitmap, 0, 0, new Paint());
        	} 
        	canvas.restore();
    		canvas.drawPath(mPath, mPaintline);
        	canvas.drawRect(0, 0, mBitmap.getWidth()- 1, mBitmap.getHeight()- 1, paint);
			if ( isGrow )//Allen++ for template
			{
	    		drawCropIndicator(canvas);
			}
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
        	float x = event.getX();
        	float y = event.getY();
        	switch(event.getAction()){
        	case MotionEvent.ACTION_DOWN:
            	if ( rectStyle )
            	{
                	isMove = false;
                	isGrow = false;
            		if (moveRect.contains(x, y))
            		{
                		isMove = true;
            		}
            		else if (!(moveRect.contains(x, y)) && growRect.contains(x, y))
            		{
            			isGrow = true;
            		}
            		lastx = x;
            		lasty = y;
            	}
            	else {
            		flastx = x;
            		flasty = y;
            		if (re.contains((int)event.getX(), (int)event.getY()) && !isReset)
            		{
            			isMove = true;
            		}
            		else
            		{
            			isReset = false; //Modified by show, reset crop region
	            		isCrop = false;
	                	startx = x;
	                	starty = y;
		            	minx = startx;
		            	miny = starty; 
		            	maxx = startx;
		            	maxy = starty;
		            	touchDown(startx,starty);
            		}
	            	}
                break;
                
            case MotionEvent.ACTION_MOVE:
            	if ( rectStyle )
            	{
            		dx = x - lastx;
            		dy = y - lasty;
	            	if ( isMove )
	            	{
	            		moveBy(dx, dy);
	            	}
	            	else if ( isGrow )
	            	{
	            		growBy(dx, dy, x, y);
	            	}
            		lastx = x; 
            		lasty = y; 
            	}
            	else{
            		if ( isMove )
            		{
            			fx = x - flastx;
                		fy = y - flasty;
                		freeMoveBy(fx, fy);
                		flastx = x; 
                		flasty = y; 
            		}
            		else
            		{
	            		touchMove(startx, starty, x, y);
	                	minx = findMin(startx,x,minx);
	                	miny = findMin(starty,y,miny);
	                	maxx = findMax(startx,x,maxx);
	                	maxy = findMax(starty,y,maxy);
            		}
      		  	}
            	invalidate();
                break;
                
            case MotionEvent.ACTION_UP:	
            	if (rectStyle){
        			RectF bounds = new RectF();
            		mPath.computeBounds(bounds, true);
            		computerBoard(mPath);
        			minx = bounds.left;
        			miny = bounds.top;
        			maxx = bounds.right;
        			maxy = bounds.bottom ;
        			isMove = false;
        			isGrow = false;
            	}
            	else
            	{
                	if(startx != x && starty != y){
                		isCrop = true;
        	    	}
                	if ( minx == maxx || miny == maxy){
                		isCrop = false;
                	}
            		touchUp();
	              	isMove = false;
            	}
    	    	invalidate();
                break;
            }    
        return true;
        }

    	//emmanual to fix bug 457423,458949
		public int readPictureDegree(String path) {
			int degree = 0;
			try {
				ExifInterface exifInterface = new ExifInterface(path);
				int orientation = exifInterface.getAttributeInt(
				        ExifInterface.TAG_ORIENTATION,
				        ExifInterface.ORIENTATION_NORMAL);
				switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return degree;
		}

    	//emmanual to fix bug 457423,458949
		public Bitmap rotaingImageView(int angle, Bitmap bitmap) {
			Matrix matrix = new Matrix();
			matrix.postRotate(angle);
			Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
			        bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			return resizedBitmap;
		}

        public void saveBitmap(String bitName) throws IOException{
        	//save the canvas as a bitmap
        	Bitmap bmpTmp = Bitmap.createBitmap(mBitmap.getWidth(),mBitmap.getHeight(), Config.ARGB_8888);
        	Canvas canvas = new Canvas(bmpTmp);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);
            if(canvas != null){
            	canvas.clipPath(mPath);   	
          	}  
          	canvas.drawBitmap(mBitmap, 0, 0, paint);   
        	RectF bound = new RectF();
    		mPath.computeBounds(bound, true);
			minx = bound.left;
			maxx = bound.right;
			miny = bound.top;
			maxy = bound.bottom;
          	if(minx < 0){
          		minx = 0;
          	}
          	if(miny < 0){
          		miny = 0;
          	}
          	if(maxx > mBitmap.getWidth() - 1){
          		maxx = mBitmap.getWidth() - 1;
          	}
          	if(maxy > mBitmap.getHeight() - 1){
          		maxy = mBitmap.getHeight() - 1;
          	}
          	
            //smilefish fix bug 674902
          	if(maxx < 1){ 
          		maxx = 1;
          	}
          	if(maxy < 1){ 
          		maxy = 1;
          	}
         
          	//crop the image as the chosen          
          	bmpTmp = Bitmap.createBitmap(bmpTmp, (int)minx, (int)miny, 
          			((int)maxx - (int) minx),((int)maxy - (int)miny)).copy(Config.ARGB_8888, true);
          	if(matrix != null ){
          		if(scaleTemp != 0)
          		{
    	        	matrix.setScale(scaleTempNew, scaleTempNew);
          		}
          		else if (max != 0)
          		{
	          		matrix.setScale((float)max/512, (float)max/512);          		
	          		if(bmpTmp.getWidth() * (max /512) < 1 || bmpTmp.getHeight() * (max /512) < 1)
	          		{
	          			return;
	          		}
          		}          		
          		
          		bmpTmp = Bitmap.createBitmap(bmpTmp,0,0,bmpTmp.getWidth(), bmpTmp.getHeight(),matrix,true);
          	}
          	
			if (getIntent().hasExtra("pageFilePath")) {// Allen
				mFilePath = getIntent().getStringExtra("pageFilePath");// Allen
			} 

            //save the bitmap as a file
          	if(mFileName.contains("SuperNoteGallery"))
          	{
          		mFileName = "SuperNoteGallery" + System.currentTimeMillis() + ".jpg";
          	}
          	else
          	{
          		mFileName = "SuperNotePic_" + System.currentTimeMillis() +  "_.jpg";
          	}
          	
	      	File file = new File(mFilePath + "/" + mFileName);
	          try {
	  			file.createNewFile();
	  		} catch (IOException e1) {
	  			// TODO Auto-generated catch block
	  			e1.printStackTrace(); 
	  		}
	  	    FileOutputStream fOut = null;
	  	    try {
	  	    	fOut = new FileOutputStream(file);
	  	    } catch (FileNotFoundException e) {
	  	    	e.printStackTrace();
	  	    }
	  	    try{
	  	    	bmpTmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
	  	    } catch (Exception e) {//emmanual to fix bug 489197
	  	    	e.printStackTrace();
	  	    	return;
	  	    }
	  	    try {
	  	    	fOut.flush();
	  	    } catch (IOException e) {
	  	    	e.printStackTrace();
	  	    }
	  	    try {
	  	    	fOut.close();
	  	    } catch (IOException e) {
	  	    	e.printStackTrace();
	  	    }

       }
        
        public float findMin(float a, float b, float min){
        	if(a < b && a < min){
        		min = a;
        	} 
        	if(b < a && b < min){
        		min = b;
        	}
        	return min;
        }
        
        public float findMax(float a, float b, float max){
        	if(a > b && a > max){
        		max = a;
        	} 
        	if(b > a && b > max){
        		max = b;
        	}
        	return max;
        }
        
        public void touchDown(float lx, float ly){
    		mPath.reset();
    		mPath.moveTo(lx, ly); 
    		//begin:clare
		int device=mContext.getResources().getInteger(R.integer.device_type);
		if(device==102)
		{
    		flastx=lx;
    		flasty=ly;   
		} 		
    		//end:clare
    	}
    	
    	public void touchMove(float lx, float ly, float x, float y){	
    		if(x < 2){
          		x = 2;
          	}
          	if(y < 2){
          		y = 2;
          	}
    		if(x > mBitmap.getWidth() - 3){
          		x = mBitmap.getWidth() - 3;
          	}
          	if(y > mBitmap.getHeight() - 3){
          		y = mBitmap.getHeight() - 3;
          	}
         //begin:clare
          	int device=mContext.getResources().getInteger(R.integer.device_type);
          	final float TOUCH_DISTANCE = 10;		
            float dx = Math.abs(x - flastx);
            float dy = Math.abs(y - flasty);
            if ( device==102 && ( dx >= TOUCH_DISTANCE || dy >= TOUCH_DISTANCE)  ) {   
            	
            		  mPath.quadTo(flastx, flasty, (flastx+x)/2, (flasty+y)/2 );   
            		  
                  flastx = x;
                  flasty = y;
            }
            else
            {
            	mPath.lineTo(x, y);
            }
        //end:clare

    	}
    	
    	public void touchUp(){	
    		mPath.close();
    		setFreeCropRegion();
    	}
    	
    	public void moveBy(float x, float y){
    		RectF bounds = new RectF();
    		mPath.computeBounds(bounds, true);
        	mPath.offset(x, y);
        	computerBoard(mPath);
    		createRect(bounds);
    	}
    	public void freeMoveBy(float x, float y){
    		RectF bounds = new RectF();
    		mPath.computeBounds(bounds, true);
        	mPath.offset(x, y);
        	computerBoard(mPath);
        	RectF bound = new RectF();
    		mPath.computeBounds(bound, true);
			minx = bound.left;
			maxx = bound.right;
			miny = bound.top;
			maxy = bound.bottom;
    	}
    	
    	public void growBy(float mx, float my, float x, float y){
    		RectF bounds = new RectF();
    		mPath.computeBounds(bounds, true);
    		float left = bounds.left;
    		float right = bounds.right;
    		float top = bounds.top;
    		float bottom = bounds.bottom;
    		float w = mBitmap.getWidth();
    		float h = mBitmap.getHeight();
    		if ( x > growRect.left && x < moveRect.left)
    		{
    			if (isTemplate) {
    				if ( top > 1 && left > 1 ) {
    					if ((( left == 2 || top == 2 )&& mx < 0 ) 
    							||( left == right - 100 && mx > 0 ) 
    							||( top == bottom - 100 && mx > 0 )
    							||( mx / rate + top) > bottom - 100 
    							||( mx / rate + top) < 2) {
    						return;
    					}
	    				bounds.left = mx + left;
        				bounds.top = mx / rate + top;
    				}
    			}
    			else {
    				bounds.left = mx + left;
    			}
    			if (bounds.left > right - 100 )
    			{
    				bounds.left = right - 100;
    			}
    		}
    		if ( x < growRect.right && x > moveRect.right)
    		{
    			if (isTemplate) {
    				if (( bottom < h - 2 ) && ( right < w - 2 )) {
    					if ((( right == w - 3 || bottom == h -3 ) && mx > 0 ) 
    							||( right == left + 100 && mx < 0 )
    							||( bottom == top + 100 && mx < 0 ) 
    							||( mx / rate + bottom ) < top + 100
    							||( mx / rate + bottom ) > h - 2) {
    						return;
    					}
    	    			bounds.right = mx + right;
    	    			bounds.bottom = mx / rate + bottom;
    				}
    			}
    			else {
    				bounds.right = mx + right;
    			}
    			if (bounds.right < left + 100)
    			{
    				bounds.right = left + 100;
    			}
    		}
    		if ( y > growRect.top && y < moveRect.top)
    		{
    			if (isTemplate) {
    				if ( top > 1 && left > 1 ) {
    					if ((( left == 2 || top == 2 )&& my < 0 ) 
    							||( left == right - 100 && my > 0 ) 
    							||( top == bottom - 100 && my > 0 )
    							||( my * rate + left) > right - 100 * rate
    							||( my * rate + left) < 2) {
    						return;
    					}
    	    			bounds.top = my + top;
    	    			bounds.left = left + my * rate;
    				}
    			}
    			else {
        			bounds.top = my + top;
    			}
    			if (bounds.top > bottom - 100)
    			{
    				bounds.top = bottom - 100;
    			}
    		}
    		if ( y < growRect.bottom && y > moveRect.bottom)
    		{
    			if (isTemplate) {
    				if (( bottom < h - 2 ) && ( right < w - 2 )) {
    					if ((( bottom == h - 3 || right == w -3 ) && my > 0) 
    							||( bottom == top + 100 && my < 0 )
    							||( right == left + 100 && my < 0 ) 
    							||( right + my * rate ) < left + 100 * rate
    							||( right + my * rate ) > w - 2) {
    						return;
    					}	
    	    			bounds.bottom = my + bottom;
    	    			bounds.right = right + my * rate;
    				}
    			}
    			else {
    				bounds.bottom = my + bottom;
    			}
    			if (bounds.bottom < top + 100)
    			{
    				bounds.bottom = top + 100;
    			}
    		}
    		
			if (bounds.left < 2)
			{
				bounds.left = 2;
			}
			if (bounds.right > w - 3)
			{
				bounds.right = w - 3;
			}
			if (bounds.top < 2)
			{
				bounds.top = 2;
			}
			if (bounds.bottom > h - 3)
			{
				bounds.bottom = h - 3;
			}
			
    		createRect(bounds);
        	mPath.reset();
        	mPath.addRect(bounds, Path.Direction.CW);
    	}
    	
        public void clearAll(){
        	// deselect the area 
        	mPath.reset();
        	isCrop = false;
        	invalidate();
        }
        
        public void drawCropIndicator(Canvas canvas)
        {
        	RectF mDrawRect = new RectF();
    		mPath.computeBounds(mDrawRect, true);
			int left    = (int)mDrawRect.left   + 1;
            int right   = (int)mDrawRect.right  + 1;
            int top     = (int)mDrawRect.top    + 4;
            int bottom  = (int)mDrawRect.bottom + 3;

            int widthWidth   =
                    mResizeDrawableWidth.getIntrinsicWidth() / 2;
            int widthHeight  =
                    mResizeDrawableWidth.getIntrinsicHeight() / 2;
            int heightHeight =
                    mResizeDrawableHeight.getIntrinsicHeight() / 2;
            int heightWidth  =
                    mResizeDrawableHeight.getIntrinsicWidth() / 2;

            int xMiddle = (int)mDrawRect.left
                    + (int)((mDrawRect.right  - mDrawRect.left) / 2);
            int yMiddle = (int)mDrawRect.top
                    + (int)((mDrawRect.bottom - mDrawRect.top) / 2);

            mResizeDrawableWidth.setBounds(left - widthWidth,
                                           yMiddle - widthHeight,
                                           left + widthWidth,
                                           yMiddle + widthHeight);
            mResizeDrawableWidth.draw(canvas);

            mResizeDrawableWidth.setBounds(right - widthWidth,
                                           yMiddle - widthHeight,
                                           right + widthWidth,
                                           yMiddle + widthHeight);
            mResizeDrawableWidth.draw(canvas);

            mResizeDrawableHeight.setBounds(xMiddle - heightWidth,
                                            top - heightHeight,
                                            xMiddle + heightWidth,
                                            top + heightHeight);
            mResizeDrawableHeight.draw(canvas);

            mResizeDrawableHeight.setBounds(xMiddle - heightWidth,
                                            bottom - heightHeight,
                                            xMiddle + heightWidth,
                                            bottom + heightHeight);
            mResizeDrawableHeight.draw(canvas);
        }
        
        public void createRect( RectF bounds)
        {
        	float left = bounds.left;
			float top = bounds.top;
			float right = bounds.right;
			float bottom = bounds.bottom;
			moveRect = new RectF (left + 30 ,top + 30, right - 30, bottom -30);
			growRect = new RectF (left - 30 ,top - 30, right + 30, bottom +30);
			if ( moveRect.isEmpty())
			{
				moveRect = bounds;
			}
			if ( growRect.isEmpty())
			{
				growRect = bounds;
			}
        }
        
        public void computerBoard(Path path)
        {
        	float x = 0F, y = 0F;
			RectF bounds = new RectF();
			path.computeBounds(bounds, true);
        	if (bounds.left < 2)
        	{
        		x = 2 - bounds.left;
        	}
        	if (bounds.right > mBitmap.getWidth() - 3)
        	{
        		x = mBitmap.getWidth() - 3 - bounds.right ;
        	}
        	if (bounds.top < 2)
        	{
        		y = 2 - bounds.top;
        	}
        	if (bounds.bottom > mBitmap.getHeight() - 3)
        	{
        		y = mBitmap.getHeight() - 3 - bounds.bottom;
        	}
        	path.offset(x, y);
        }
        
        private void setFreeCropRegion(){
        	RectF r=new RectF();  
        	mPath.computeBounds(r, true);  
        	re.setPath(mPath, new Region((int)r.left,(int)r.top,(int)r.right,(int)r.bottom));
        	//Begin: show_wang@asus.com
        	//Modified reason: reset crop region
        	if ( r.width() > (mBitmap.getWidth() - 6) && r.height() > (mBitmap.getHeight() - 6) ) {
        		isReset = true;
        	}
        	//End: show_wang@asus.com
        }
    }
    
    private class SavePageTask extends AsyncTask<Void, Void, Void> {
 		public SavePageTask( Context context ) {
 	        super();
 		}

 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			showDialog(SAVING_PROGRESS_DIALOG);
 		}

 		@Override
 		protected Void doInBackground(Void... params) {
 			try {
				myView.saveBitmap(cropImage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 			return null;
 		}
 		
 		@Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
			try {
				Intent intent = new Intent();
				intent.putExtra("FileName", mFileName);
				//Begin Darwin_Yu@asus.com
				intent.putExtra("OriginalFileName", mOriginalFileName);
				//End   Darwin_Yu@asus.com
				//Begin Allen
				intent.putExtra("FilePath", getIntent().getStringExtra("filePath"));
				//End Allen
				intent.putExtra("IsConfig", mIsConfig); //by show
				setResult(RESULT_OK , intent);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finish();
			removeDialog(SAVING_PROGRESS_DIALOG);
			IsRun = false;
        }
 	}
    
    private OnLongClickListener mFuncIconLongClickListener = new OnLongClickListener() {

		@Override
        public boolean onLongClick(View v) {
			if ( deviceType <= 100 ) {
				Toast.makeText(CropImageActivity.this, ((Button) v).getText().toString(), Toast.LENGTH_SHORT).show();
			}
			return true;
		}
    };
}
//END:Show