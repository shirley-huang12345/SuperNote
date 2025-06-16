package com.asus.supernote.editable;

import java.util.LinkedList;
import java.util.List;

import com.asus.supernote.PaintSelector;
import com.asus.supernote.R;
import com.asus.supernote.data.BrushCollection;
import com.asus.supernote.data.BrushInfo;
import com.asus.supernote.doodle.drawtool.DrawTool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

public class BrushLibraryAdapter extends BaseAdapter{
	private Context mContext;
	private BrushCollection mBrushCollection = null;
	private Paint mBrushPaint = null;
	
    private int[] mDefaultColorCodes = {
    		0xffffffff, 
    		0xffb4b4b4, 
    		0xff5a5a5a, 
    		0xff000000,
    		0xffe70012, 
    		0xffff9900,
    		0xfffff100, 
    		0xff8fc31f, 
    		0xff009944, 
    		0xff00a0e9, 
    		0xff1d2088,
    		0xffe5007f
    }; 
	
	public BrushLibraryAdapter(Context context, BrushCollection brushCollection)
	{
		mContext = context;
		mBrushCollection = brushCollection;
		mBrushPaint = new Paint();
		PaintSelector.initPaint(mBrushPaint, Color.LTGRAY, 3);
	}
	
	@Override
	public int getCount() {
		return mBrushCollection.brushSize();
	}

	@Override
	public Object getItem(int position) {
		return mBrushCollection.getBrush(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder holder;
		view = View.inflate(mContext, R.layout.brush_library_item, null);
		
		if (convertView == null || convertView.getTag() == null) {
			holder = new ViewHolder();
			holder.brushImage = (ImageView) view.findViewById(R.id.brush_image);
			holder.brushPreview = (ImageView) view.findViewById(R.id.brush_preview);
			holder.removeBrush = (ImageButton) view.findViewById(R.id.remove_brush);
		}
		else
		{
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		
		BrushInfo brush = mBrushCollection.getBrush(position);
		
		view.setOnClickListener(brushClickListener);
		holder.index = position;
		view.setTag(holder);
		holder.removeBrush.setOnClickListener(removeBrushClickListener);
		holder.removeBrush.setTag(holder);
		holder.brushImage.setBackgroundResource(getBrushImageId(brush));	
		
		//Bitmap PreviewBitmap = Bitmap.createBitmap(438, 66, Config.ARGB_4444);
		float width = mContext.getResources().getDimension(R.dimen.preview_image_width);
		float height = mContext.getResources().getDimension(R.dimen.preview_image_height);
		Bitmap PreviewBitmap = Bitmap.createBitmap((int)width, (int)height, Config.ARGB_8888);
		Canvas PreviewCanvas = new Canvas(PreviewBitmap);

		setBrushbrushPaint(brush);
		for(INotifyOuter outer:outers)
			outer.draw(PreviewCanvas, mBrushPaint, brush.getDoodleToolCode());
		holder.brushPreview.setImageBitmap(PreviewBitmap);
		
		return view;
	}
	
	private int getBrushImageId(BrushInfo brush)
	{		
		int resId = 0;
		switch(brush.getDoodleToolCode())
		{
		case DrawTool.NORMAL_TOOL:
			resId = R.drawable.asus_popup_pen3;
			break;
		case DrawTool.SKETCH_TOOL:
			resId = R.drawable.asus_popup_pen1;
			break;
		case DrawTool.MARKPEN_TOOL:
			resId = R.drawable.asus_popup_pen4;
			break;
		case DrawTool.SCRIBBLE_TOOL:
			resId = R.drawable.asus_popup_pen2;
			break;
		case DrawTool.WRITINGBRUSH_TOOL:
			resId = R.drawable.asus_popup_pen5;
			break;
		case DrawTool.NEON_TOOL:
			resId = R.drawable.asus_popup_pen6;
			break;
		}
		
		return resId;
	}
	
	private void setBrushbrushPaint(BrushInfo brush)
	{		
		mBrushPaint.setStrokeWidth(brush.getStrokeWidth());
        if ( brush.getIsPalette())
        	mBrushPaint.setColor(brush.getCustomColor()); 
        else
        	mBrushPaint.setColor(mDefaultColorCodes[brush.getSelectedColorIndex()]);        
        
		switch(brush.getDoodleToolCode())
		{
		case DrawTool.NORMAL_TOOL:
			PaintSelector.setNormal(mBrushPaint);
			break;
		case DrawTool.SKETCH_TOOL:
			PaintSelector.setSketch(mBrushPaint);
			break;
		case DrawTool.MARKPEN_TOOL:			
			PaintSelector.setMarkPen(mBrushPaint);
			mBrushPaint.setAlpha(brush.getDoodleToolAlpha()); //edit by smilefish for marker pen
			break;
		case DrawTool.SCRIBBLE_TOOL:
			PaintSelector.setScribble(mBrushPaint);
			break;
		case DrawTool.WRITINGBRUSH_TOOL:
			PaintSelector.setWritingBrush(mBrushPaint);
			break;
		case DrawTool.NEON_TOOL:
			PaintSelector.setNeon(mBrushPaint);
			break;
		}		
	}
	
	private final View.OnClickListener brushClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			for(INotifyOuter outer:outers)
				outer.selectBrush(mBrushCollection.getBrush(holder.index));
			notifyDataSetChanged();
		}
	};
	
	private final View.OnClickListener removeBrushClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			mBrushCollection.deleteBrush(holder.index);
			for(INotifyOuter outer:outers)
				outer.showAddBrushButton();
			notifyDataSetChanged();
		}
	};

	private static class ViewHolder {
		ImageView brushImage;
		ImageView brushPreview;
		ImageButton removeBrush;
		int index;
	}
	
	//begin smilefish
	private List<INotifyOuter> outers=new LinkedList<INotifyOuter>();
	public void addOuterListener(INotifyOuter outer){
		outers.add(outer);
	}
	public interface INotifyOuter{
		void selectBrush(BrushInfo info);
		void showAddBrushButton();
		void draw(Canvas canvas, Paint paint, int toolCode);
	}
	//end smilefish
}
