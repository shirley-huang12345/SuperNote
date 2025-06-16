package com.asus.supernote.template.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.widget.TextView;

import com.asus.supernote.R;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.editable.DrawableSpan;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.NoteHandWriteBaselineItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.template.TemplateToDoUtility;

public class ToDoWidgetListItem {

	public static final short ITEM_TYPE_CONTENT = 0;
	public static final short ITEM_TYPE_SEPERATER = 1;
	
	private Context mContext = null;
	public short itemType = ITEM_TYPE_CONTENT;
	public NoteItemArray noteItems = null;
	public long bookId = -1;
	public long pageId = -1;
	public int positon = 0;//start with 1
	public String bookTitle = null;
	public String pageTitle = null;
	
	public String seperaterLeft = null;//display seperater string
	public String seperaterRight = null;
	public boolean isChecked = false;
	public short priority = TemplateToDoUtility.TODO_PRIORITY_NORMAL;
	public long lastModifyTime = -1;

	private Bitmap mContentBitmap = null;
	private Paint linePaint = null;
	private int mFontSize = 0;
	private int mPaddingTop = 0;
	public ToDoWidgetListItem(Context context){
		mContext = context;
		linePaint = new Paint();
		linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(2 * mContext.getResources().getDisplayMetrics().density);
        mFontSize = (int) mContext.getResources().getDimension(R.dimen.todo_widget_item_fontsize);
        mPaddingTop = (int) (2 * mContext.getResources().getDisplayMetrics().density);
	}
	
	public Bitmap getContentBitmap() {
		if(mContentBitmap == null){
			mContentBitmap = getBitmap();
		}
		return mContentBitmap;
	}	
	
	public void recycleBitmap(){
		if(mContentBitmap != null){
			mContentBitmap.recycle();
			mContentBitmap = null;
		}
	}
	
	private Bitmap getBitmap(){
		int width = (int) mContext.getResources().getDimension(R.dimen.todo_widget_item_content_width);
		int startDP = mContext.getResources().getInteger(R.integer.start_width);
		int incrementalDP = mContext.getResources().getInteger(R.integer.increment_width);
		//Carol
		if(mContext.getResources().getConfiguration().smallestScreenWidthDp >= 720 && MetaData.TODO_WIDGET_WIDTH_SIZE != 0){
			width = startDP +(MetaData.TODO_WIDGET_WIDTH_SIZE - 3)*incrementalDP;
			double ratio = mContext.getResources().getDimension(R.dimen.widget_width_ratio);
			if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
				width = (int)((width/1.6 - 150)*ratio);
			}else{
				width = (int)((width - 150)*ratio);
			}
		}else if(mContext.getResources().getConfiguration().smallestScreenWidthDp >= 600 && MetaData.TODO_WIDGET_WIDTH_SIZE != 0){
			width = startDP +(MetaData.TODO_WIDGET_WIDTH_SIZE - 3)*incrementalDP;
			if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
				width -= 187;
			}else{
				width = (int)((double)width*1.3) - 163;
			}
		}
		int priorityWidth = (int) mContext.getResources().getDimension(R.dimen.todo_widget_priority_width); //smilefish
		TextView tv = new TextView(mContext);
		tv.setTextColor(mContext.getResources().getColor(R.color.memoWidgetItemTextColor));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mFontSize);
        tv.setSingleLine(true);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setPadding(0, mPaddingTop, 0, 0);
        tv.setEllipsize(TruncateAt.END);
        Editable edit = loadNoteItems(noteItems,tv);
        tv.setText(edit);
		tv.measure(MeasureSpec.makeMeasureSpec(
				(priority != TemplateToDoUtility.TODO_PRIORITY_NORMAL)?width - priorityWidth:width, 
				MeasureSpec.EXACTLY), //smilefish
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

		Bitmap content = Bitmap.createBitmap(width, tv.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas contentCanvas = new Canvas(content);
        tv.draw(contentCanvas);
        
        StaticLayout tempLayout = new StaticLayout(edit, tv.getPaint(), width, 
        		android.text.Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
        float textWidth = 0;
        for(int i = 0;i< tempLayout.getLineCount();i++){
        	if(textWidth < width){
        		textWidth += tempLayout.getLineWidth(i);
        	}
        	else{
        		break;
        	}
        }
        if(priority != TemplateToDoUtility.TODO_PRIORITY_NORMAL){
        	width -= priorityWidth; //smilefish
        }
        if(isChecked){
        	contentCanvas.drawLine(0, tv.getMeasuredHeight()/2 + mPaddingTop ,textWidth > width ? width : textWidth,
        				tv.getMeasuredHeight()/2 + mPaddingTop, linePaint);
        }
        
        return content;
	}
	
	//begin smilefish
	public int getPriorityImageViewResourceId(short priority){ 
		switch(priority){
		case TemplateToDoUtility.TODO_PRIORITY_HIGH:
			return R.drawable.asus_memo_high_ic;
		case TemplateToDoUtility.TODO_PRIORITY_LOW:
			return R.drawable.asus_memo_low_ic;
		default: return -1;
				
		}
	}
	//end smilefish
	
	private Editable loadNoteItems(NoteItemArray allNoteItems,TextView tv){
		NoteItem[] noteItems = allNoteItems.getNoteItemArray();
		if (noteItems == null || noteItems.length == 0) {
            return null;
        }
		String str = noteItems[0].getText();
        Editable editable = new SpannableStringBuilder(str);

        for (int i = 1; i < noteItems.length; i++) {
            if (noteItems[i].getStart() < 0 || noteItems[i].getEnd() < 0
                    || noteItems[i].getStart() > editable.length() ) {
                continue;
            }
            
            // BEGIN: Shane_Wang 2012-11-5
            if(noteItems[i].getEnd() > editable.length()) {
            	noteItems[i].setEnd(editable.length());
            }
            // END: Shane_Wang 2012-11-5
            
            editable.setSpan(noteItems[i], noteItems[i].getStart(), noteItems[i].getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        setFontSize(editable,tv);
        return editable;
	}
	
    public void setFontSize(Editable editable, TextView tv) {
        DrawableSpan[] drawableSpanItems = editable.getSpans(0, editable.length(), DrawableSpan.class);
        for (DrawableSpan item : drawableSpanItems) {
            if(item instanceof NoteHandWriteItem && !(item instanceof NoteHandWriteBaselineItem)){
                item.setFontHeight(getFullImageSpanHeight(tv));
            }
            else {
                item.setFontHeight(getImageSpanHeight());
            }
        }
    } 
    
    // BEGIN: archie_huang@asus.com
    public int getImageSpanHeight() {
        FontMetricsInt fontMetricsInt;
        Paint paint = new Paint();
        paint.setTextSize(mFontSize);
        fontMetricsInt = paint.getFontMetricsInt();
        return (int) (fontMetricsInt.descent * PageEditor.FONT_DESCENT_RATIO - fontMetricsInt.ascent);
    } // END: archie_huang@asus.com

    // BEGIN: archie_huang@asus.com
    public int getFullImageSpanHeight(TextView tv) {
        FontMetricsInt fontMetricsInt;
        Paint paint = new Paint();
        paint.setTextSize(mFontSize);
        fontMetricsInt = paint.getFontMetricsInt();
        return (int) (tv.getLineHeight() + fontMetricsInt.descent * PageEditor.FONT_DESCENT_RATIO);
        
    } // END: archie_huang@asus.com

    public String getLastModifyDay(){
    	Date d = new Date(lastModifyTime);
    	//emmanual
    	SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");
    	return sf.format(d);
    }
    
    public Context getContext(){
    	return mContext;
    }
}
