package com.asus.supernote.editable.noteitem;

import java.io.IOException;
import java.util.LinkedList;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;

import com.asus.supernote.PaintSelector;
import com.asus.supernote.data.AsusFormatReader;
import com.asus.supernote.data.AsusFormatWriter;
import com.asus.supernote.data.MetaData;

public class NoteHandWriteBaselineItem extends NoteHandWriteItem {
    private static final float PADDING_TOP = 2f;
    private static final float PADDING_BOTTOM = 1f;
    private static final int FONT_HEIGHT_DEFAULT = 36;
    //darwin
    final static int HEIGHT_FACTOR = 3;
    //darwin

    public NoteHandWriteBaselineItem() {
        super(new LazyDrawable(), DynamicDrawableSpan.ALIGN_BASELINE);
    }

    // Used by SupernoteFormat1to2Transfer
    public NoteHandWriteBaselineItem(LinkedList<PathInfo> pathInfoList, Paint paint) {
        super(new LazyDrawable(), DynamicDrawableSpan.ALIGN_BASELINE);

        mPathInfoList = new LinkedList<PathInfo>();
        mPathInfoList.addAll(pathInfoList);

        if (paint != null) {
            mPaint = new Paint(paint);
        }

        RectF itemRect = new RectF();
        for (PathInfo pathInfo : mPathInfoList) {
            RectF pathRect = new RectF();
            pathInfo.getPath().computeBounds(pathRect, true);
            itemRect.union(pathRect);
        }

        Matrix matrix = new Matrix();
        matrix.setTranslate(-itemRect.left + PADDING_LEFT, -itemRect.top + PADDING_TOP);
        for (PathInfo pathInfo : mPathInfoList) {
            pathInfo.transform(matrix);
        }

        mFontHeight = (int) (Math.round(itemRect.height()) + (PADDING_TOP + PADDING_BOTTOM));
        mFontHeight = mFontHeight < FONT_HEIGHT_CONSTRICT ? FONT_HEIGHT_CONSTRICT : mFontHeight;
        mCurrentFontHeight = FONT_HEIGHT_DEFAULT;
        initFontHeight();
        genDrawable(0);
    }

    // Used by HandWritingView when drawing finish
    public NoteHandWriteBaselineItem(LinkedList<PathInfo> pathInfoList, Paint paint, int screenHeight, int fontHeight) {

        super(new LazyDrawable(), DynamicDrawableSpan.ALIGN_BASELINE);

        mPathInfoList = new LinkedList<PathInfo>();
        mPathInfoList.addAll(pathInfoList);
        if (paint != null) {
            mPaint = new Paint(paint);
        }
        mFontHeight = fontHeight;
        mCurrentFontHeight = mFontHeight;
        initFontHeight();
        genDrawable(screenHeight);
    }

    public NoteHandWriteBaselineItem(NoteHandWriteItem oldItem) {
        super(new LazyDrawable(), DynamicDrawableSpan.ALIGN_BASELINE);

        mPathInfoList = new LinkedList<PathInfo>();
        mPathInfoList.addAll(oldItem.mPathInfoList);

        if (oldItem.mPaint != null) {
            mPaint = new Paint(oldItem.mPaint);
        }

        mFontHeight = oldItem.mFontHeight;
        mCurrentFontHeight = oldItem.mCurrentFontHeight;
        initFontHeight();
        genDrawable(0);
    }

	//emmanual to copy from QuickMemo to SuperNote
    public NoteHandWriteBaselineItem(NoteHandWriteItem oldItem, boolean changeColor) {
        super(new LazyDrawable(), DynamicDrawableSpan.ALIGN_BASELINE);

        if(changeColor){ //pen color is changed
              mSerPathInfoList = new LinkedList<SerPathInfo>();
              mSerPathInfoList.addAll(oldItem.mSerPathInfoList);
        }else{
              mPathInfoList = new LinkedList<PathInfo>();
              mPathInfoList.addAll(oldItem.mPathInfoList);
        }

        if (oldItem.mPaint != null) {
            mPaint = new Paint(oldItem.mPaint);
        }

        mFontHeight = oldItem.mFontHeight;
        mCurrentFontHeight = oldItem.mCurrentFontHeight;
        initFontHeight();
        genDrawable(0);
    }

    @Override
    public void itemLoad(AsusFormatReader afr) throws IOException {
        mSerPathInfoList = new LinkedList<SerPathInfo>();
        if (mPaint == null) {
            mPaint = new Paint();
            PaintSelector.initPaint(mPaint, Color.BLACK, MetaData.SCRIBBLE_PAINT_WIDTHS_NORMAL);
        }

        AsusFormatReader.Item item = afr.readItem();
        while (item != null) {
            switch (item.getId()) {
                case SNF_NITEM_HANDWRITEBL_BEGIN:
                    break;
                case SNF_NITEM_HANDWRITEBL_POS_START:
                    mStart = item.getIntValue();
                    break;
                case SNF_NITEM_HANDWRITEBL_POS_END:
                    mEnd = item.getIntValue();
                    break;
                case SNF_NITEM_HANDWRITEBL_COLOR:
                    mPaint.setColor(item.getIntValue());
                    break;
                case SNF_NITEM_HANDWRITEBL_STROKE_WIDTH:
                    mPaint.setStrokeWidth(item.getFloatValue());
                    break;
                case SNF_NITEM_HANDWRITEBL_FONT_HEIGHT:
                    mFontHeight = item.getIntValue();
                    mCurrentFontHeight = mFontHeight;
                    break;
                // BEGIN: archie_huang@asus.com
                case SNF_NITEM_HANDWRITEBL_FONT_WIDTH:
                    mFontWidth = item.getIntValue();
                    break;
                // END: archie_huang@asus.com
                case SNF_NITEM_HANDWRITEBL_PATHS:
                    short[] pointArray = item.getShortArray();
                    if ((pointArray == null) && (item.getType() == AsusFormatReader.Item.ITEM_TYPE_BYTE_ARRAY)) {
                        byte[] byteArray = item.getByteArray();
                        pointArray = new short[byteArray.length];
                        for (int i = 0; i < byteArray.length; i++) {
                            pointArray[i] = byteArray[i];
                        }
                    }
                    mSerPathInfoList.add(new SerPathInfo(pointArray));
                    break;
                case SNF_NITEM_HANDWRITEBL_END:
                    initFontHeight();
                    // TODO: For compatibility, remove it finally
                    if (mFontWidth == -1) {
                        genPaths();
                        mSerPathInfoList = null;
                        RectF unionBounds = getPathBounds();
                        mFontWidth = (int) ((unionBounds.right - unionBounds.left) + (3 * PADDING_LEFT));
                    }
                    mDrawable.setBounds(0, 0, mFontWidth, mFontHeight);
                    return;
                default:
                    Log.w(TAG, "Unknow id = 0x" + Integer.toHexString(item.getId()));
                    break;
            }
            item = afr.readItem();
        }

    }

    @Override
    public void itemSave(AsusFormatWriter afw) throws IOException {
        afw.writeByteArray(SNF_NITEM_HANDWRITEBL_BEGIN, null, 0, 0);

        afw.writeInt(SNF_NITEM_HANDWRITEBL_POS_START, mStart);
        afw.writeInt(SNF_NITEM_HANDWRITEBL_POS_END, mEnd);
        afw.writeInt(SNF_NITEM_HANDWRITEBL_COLOR, mPaint.getColor());
        afw.writeFloat(SNF_NITEM_HANDWRITEBL_STROKE_WIDTH, mPaint.getStrokeWidth());
        afw.writeInt(SNF_NITEM_HANDWRITEBL_FONT_HEIGHT, mFontHeight);
        // BEGIN: archie_huang@asus.com
        afw.writeInt(SNF_NITEM_HANDWRITEBL_FONT_WIDTH, mFontWidth);
        // END: archie_huang@asus.com
        
        //Begin Allen
        if(mPathInfoList == null){
        	genPaths();
        }
        //End Allen
        
        for (PathInfo info : mPathInfoList) {
            short[] points = info.getPoints();
            afw.writeShortArray(SNF_NITEM_HANDWRITEBL_PATHS, points, 0, points.length);
        }

        afw.writeByteArray(SNF_NITEM_HANDWRITEBL_END, null, 0, 0);

    }

    @Override
    public void scalePath(int screenHeight) {
        if (screenHeight > 0) {
            Matrix transMatrix = new Matrix();
            RectF unionBounds = getPathBounds();
            float height = unionBounds.bottom - unionBounds.top;
            float width = unionBounds.right - unionBounds.left;
            float scale = 1;
            float leftTopX;
            float leftTopY;

            // If the item's height is too small, extend the height to targetHeight,
            // and the X & Y coordinate of item will be shift to fit the new bitmap
            // else the item will align with top
            //darwin
            if (height < mFontHeight * HEIGHT_FACTOR || (height < mRectMinHeight && width < mRectMinWidth)) {
            //darwin
            	// BEGIN: Shane_Wang 2012-11-15
            	scale =  (mFontHeight) /  mRectMinHeight;  //add
                transMatrix.setScale(scale, scale);
                transform(transMatrix);
                
                //add three parameters
                //screenHeight means canvas height:
                float paddingTopRatio = unionBounds.top / (screenHeight - unionBounds.bottom + unionBounds.top);
                float marginHeight = (mRectMinHeight - (unionBounds.bottom - unionBounds.top)) * scale;
                float paddingTop = paddingTopRatio * marginHeight;
                
                unionBounds = getPathBounds();
                leftTopY = unionBounds.top - paddingTop; //add
                // END: Shane_Wang 2012-11-15
            }
            else {
                scale = (mFontHeight - (PADDING_TOP + PADDING_BOTTOM)) / height;
                transMatrix.setScale(scale, scale, unionBounds.centerX(), unionBounds.centerY());
                transform(transMatrix);
                unionBounds = getPathBounds();
                leftTopY = unionBounds.top;
            }
            mFontWidth = (int) ((unionBounds.right - unionBounds.left) + (3 * PADDING_LEFT));
            leftTopX = unionBounds.left;
            transMatrix.reset();
            transMatrix.setTranslate(-leftTopX + PADDING_LEFT, -leftTopY + PADDING_TOP);
            transform(transMatrix);

        }
    }
}
