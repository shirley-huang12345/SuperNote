package com.asus.supernote.template;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class TemplateTodoItemLinearLayout extends LinearLayout {
	private Paint linePaint = new Paint();
	private float[] matrixValues = new float[9];
	private Matrix matrix = new Matrix();

	public TemplateTodoItemLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
        linePaint.setColor(Color.rgb(113, 113, 113));
        linePaint.setStrokeWidth(2f);
	}
	public TemplateTodoItemLinearLayout(Context context) {
		super(context);
        linePaint.setColor(Color.rgb(113, 113, 113));
        linePaint.setStrokeWidth(2f);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		try{ //smilefish fix google play bug
			super.dispatchDraw(canvas);    	 
		}catch(IndexOutOfBoundsException e){
			e.printStackTrace();
		}
    	canvas.save();
    	canvas.getMatrix(matrix);
    	matrix.getValues(matrixValues);
    	matrixValues[0]=1;
    	matrixValues[4]=1; 
    	matrix.setValues(matrixValues);
    	canvas.setMatrix(matrix);
        canvas.restore();
	}
}
