package com.asus.supernote.doodle;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.asus.supernote.PaintSelector;
import com.asus.supernote.ShapeManager;
import com.asus.supernote.data.AsusFormat;
import com.asus.supernote.data.AsusFormatReader;
import com.asus.supernote.data.AsusFormatReader.Item;
import com.asus.supernote.data.AsusFormatWriter;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.doodle.drawinfo.AnnotationDrawInfo;
import com.asus.supernote.doodle.drawinfo.ArcDrawInfo;
import com.asus.supernote.doodle.drawinfo.BrushDrawInfo;
import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.EraseDrawInfo;
import com.asus.supernote.doodle.drawinfo.GraphicDrawInfo;
import com.asus.supernote.doodle.drawinfo.MarkerDrawInfo;
import com.asus.supernote.doodle.drawinfo.NeonDrawInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo.Point;
import com.asus.supernote.doodle.drawinfo.PenDrawInfo;
import com.asus.supernote.doodle.drawinfo.PencilDrawInfo;
import com.asus.supernote.doodle.drawinfo.TextImgDrawInfo;
import com.asus.supernote.doodle.drawtool.AirBrushDrawTool;
import com.asus.supernote.doodle.drawtool.AnnotationDrawTool;
import com.asus.supernote.doodle.drawtool.BrushDrawTool;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawtool.EraseDrawTool;
import com.asus.supernote.doodle.drawtool.GraphicDrawTool;
import com.asus.supernote.doodle.drawtool.MarkerDrawTool;
import com.asus.supernote.doodle.drawtool.NeonDrawTool;
import com.asus.supernote.doodle.drawtool.PathDrawTool;
import com.asus.supernote.doodle.drawtool.PenDrawTool;
import com.asus.supernote.doodle.drawtool.PencilDrawTool;
import com.asus.supernote.doodle.drawtool.ShapeDrawTool;
import com.asus.supernote.doodle.drawtool.TextImgDrawTool;
import com.asus.supernote.editable.noteitem.NoteForegroundColorItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteBaselineItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteStringItem;
import com.asus.supernote.editable.noteitem.NoteTextStyleItem;

public class DoodleItem implements Serializable, AsusFormat {
    public static class SerAnnotationInfo extends SerDrawInfo {
        private static final long serialVersionUID = 1L;
        private SerDrawInfo[] mSerInfos;
        private int[] mLocations;

        public SerAnnotationInfo() {
        }

        public SerAnnotationInfo(LinkedHashMap<Integer, DrawInfo> drawInfos, NotePage note) {
            int index = 0;
            //Collection<DrawInfo> collection = drawInfos.values();
            
            TreeSet<Integer> ts = new TreeSet<Integer>(new Comparator(){
         		 public int compare(Object o1,Object o2){
         		 Integer i1 = (Integer)o1;
         		 Integer i2 = (Integer)o2;
         		 return i2.intValue() - i1.intValue();
         		 }
         		 });
             	ts.addAll(drawInfos.keySet());
         	
              Set<Integer> keys = ts;
              
            //Set<Integer> keys = drawInfos.keySet();
            mSerInfos = new SerDrawInfo[drawInfos.size()];
            mLocations = new int[keys.size()];
            index = 0;
            for (Integer key : keys) {
            	if(drawInfos.get(key) != null) {
	            	mSerInfos[index] = drawInfos.get(key).save(note);
	                mLocations[index] = key;
	                index++;
            	}
            }
        }

        @Override
        protected DrawInfo getDrawInfoLock(String directory) {
        	int index = 0;
            AnnotationDrawInfo drawInfo = new AnnotationDrawInfo(new AnnotationDrawTool(), null);
            for (Integer key : mLocations) {
                drawInfo.add(mSerInfos[index++].getDrawInfo(directory), false);
            }
            return drawInfo;
        }

        @Override
        protected boolean isEmpty() {
            return (mSerInfos == null) || (mSerInfos.length == 0);
        }

        @Override
        public void itemLoad(AsusFormatReader afr) throws IOException {
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerAnnotationInfo.itemLoad() Begin");
            }
            LinkedList<SerDrawInfo> serDrawInfos = new LinkedList<SerDrawInfo>();
            SerDrawInfo drawInfo = null;
            super.itemLoad(afr);
            Item item = afr.readItem();
            while (item != null) {
                switch (item.getId()) {
                    case SNF_DITEM_ANNOTATION_BEGIN:
                        drawInfo = new SerAnnotationInfo();
                    	drawInfo.setPageVersion(mPageVersion);
                        break;
                    case SNF_DITEM_ANNOTATION_LOCATIONS:
                        mLocations = item.getIntArray();
                        break;
                    case SNF_DITEM_ANNOTATION_DRAWS_BEGIN:
                        break;
                    case SNF_DITEM_ERASE_BEGIN:
                        drawInfo = new SerEraseInfo();
                        drawInfo.setPageVersion(mPageVersion);
                        break;
                    case SNF_DITEM_GRAPHIC_BEGIN:
                        drawInfo = new SerGraphicInfo();
                        drawInfo.setPageVersion(mPageVersion);
                        break;
                    // BEGIN: Show
                    case SNF_DITEM_TEXTIMG_BEGIN:
                    	drawInfo = new SerTextImgInfo();
                    	drawInfo.setPageVersion(mPageVersion);
                    	break;
                    // END: Show
                    case SNF_DITEM_POINT_BEGIN:
                        drawInfo = new SerPointInfo();
                        drawInfo.setPageVersion(mPageVersion);
                        break;
                    case SNF_DITEM_ANNOTATION_DRAWS_END:
                        break;
                    case SNF_DITEM_ANNOTATION_END:
                        int size = serDrawInfos.size();
                        if (size > 0) {
                            mSerInfos = new SerDrawInfo[size];
                            serDrawInfos.toArray(mSerInfos);
                        }
                        if (DEBUG_SAVE) {
                            Log.d(TAG, "SerAnnotationInfo.itemLoad() End");
                        }
                        return;
                }
                if (drawInfo != null) {
                    drawInfo.itemLoad(afr);
                    serDrawInfos.add(drawInfo);
                    drawInfo = null;
                }
                item = afr.readItem();
            }
        }

        @Override
        public void itemSave(AsusFormatWriter afw) throws IOException {
            if ((mSerInfos == null) || (mSerInfos.length == 0)) {
                // Empty object, discard it
                return;
            }
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerAnnotationInfo.itemSave() Begin");
            }
            afw.writeByteArray(SNF_DITEM_ANNOTATION_BEGIN, null, 0, 0);
            super.itemSave(afw);
            afw.writeIntArray(SNF_DITEM_ANNOTATION_LOCATIONS, mLocations, 0, mLocations.length);
            afw.writeByteArray(SNF_DITEM_ANNOTATION_DRAWS_BEGIN, null, 0, 0);
            for (SerDrawInfo serInfo : mSerInfos) {
                if (serInfo != null) {
                    serInfo.itemSave(afw);
                }
            }
            afw.writeByteArray(SNF_DITEM_ANNOTATION_DRAWS_END, null, 0, 0);
            afw.writeByteArray(SNF_DITEM_ANNOTATION_END, null, 0, 0);
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerAnnotationInfo.itemSave() End");
            }
        }
        
        //Begin: show_wang@asus.com
        //Modified reason: AnnotationInfo include textimginfo 
        public SerDrawInfo[] getSerAnnotationInfo() {
     	   return mSerInfos;
        }
        //End: show_wang@asus.com
    }

    public static abstract class SerDrawInfo implements Serializable, AsusFormat {
        private static final long serialVersionUID = 1L;
        protected int mPaintTool;
        // Paint Info
        protected float mStrokeWidth;
        protected int mColor;
        protected short mAlpha = 255;
        protected SerEraseInfo[] mEraseInfos;
        protected short mPageVersion = MetaData.ITEM_VERSION;  //Carol

        public short getPageVersion() {
			return mPageVersion;
		}

		public void setPageVersion(short pageVersion) {
			this.mPageVersion = pageVersion;
		}

		public DrawInfo getDrawInfo(String directory) {
            DrawInfo drawInfo = getDrawInfoLock(directory);
            if (drawInfo == null) {
            	return null;
            }
            drawInfo.setPageVersion(mPageVersion);

            if (mEraseInfos != null) {
                for (SerEraseInfo eraseInfo : mEraseInfos) {
                    if (eraseInfo != null) {
                    	eraseInfo.setPageVersion(mPageVersion);
                        drawInfo.add((EraseDrawInfo) eraseInfo.getDrawInfo(directory), true);
                    }
                }
            }

            return drawInfo;
        }

        abstract protected DrawInfo getDrawInfoLock(String directory);

        abstract protected boolean isEmpty();

        @Override
        public void itemLoad(AsusFormatReader afr) throws IOException {
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerDrawInfo.itemLoad() Begin");
            }
            LinkedList<SerEraseInfo> eraseInfos = new LinkedList<SerEraseInfo>();
            Item item = afr.readItem();
            while (item != null) {
                switch (item.getId()) {
                    case SNF_DITEM_DRAW_BEGIN:
                        break;
                    case SNF_DITEM_DRAW_PAINT_TOOL:
                        mPaintTool = item.getIntValue();
                        break;
                    case SNF_DITEM_DRAW_STROKE_WIDTH:
                        mStrokeWidth = item.getFloatValue();
                        break;
                    case SNF_DITEM_DRAW_COLOR:
                        mColor = item.getIntValue();
                        break;
                    case SNF_DITEM_DRAW_ALPHA: // Better
                        mAlpha = item.getShortValue();
                        break;
                    case SNF_DITEM_DRAW_ERASES_BEGIN:
                        break;
                    case SNF_DITEM_ERASE_BEGIN:
                        SerEraseInfo drawInfo = new SerEraseInfo();
                        drawInfo.setPageVersion(mPageVersion);
                        drawInfo.itemLoad(afr);
                        eraseInfos.add(drawInfo);
                        break;
                    case SNF_DITEM_DRAW_ERASES_END:
                        break;
                    case SNF_DITEM_DRAW_END:
                        int size = eraseInfos.size();
                        if (size > 0) {
                            mEraseInfos = new SerEraseInfo[size];
                            eraseInfos.toArray(mEraseInfos);
                        }
                        if (DEBUG_SAVE) {
                            Log.d(TAG, "SerDrawInfo.itemLoad() End");
                        }
                        return;
                }
                item = afr.readItem();
            }
        }

        @Override
        public void itemSave(AsusFormatWriter afw) throws IOException {
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerDrawInfo.itemSave() Begin");
            }
            afw.writeByteArray(SNF_DITEM_DRAW_BEGIN, null, 0, 0);
            afw.writeInt(SNF_DITEM_DRAW_PAINT_TOOL, mPaintTool);
            afw.writeFloat(SNF_DITEM_DRAW_STROKE_WIDTH, mStrokeWidth);
            afw.writeInt(SNF_DITEM_DRAW_COLOR, mColor);
            // BEGIN: Better
            if (mPaintTool == DrawTool.MARKPEN_TOOL) {
            	afw.writeShort(SNF_DITEM_DRAW_ALPHA, mAlpha);
            }
            // END: Better
            afw.writeByteArray(SNF_DITEM_DRAW_ERASES_BEGIN, null, 0, 0);
            if (mEraseInfos != null) {
                for (SerEraseInfo eraseInfo : mEraseInfos) {
                    if (eraseInfo != null && eraseInfo.mVisible == 1) {
                        eraseInfo.itemSave(afw);
                    }
                }
            }
            afw.writeByteArray(SNF_DITEM_DRAW_ERASES_END, null, 0, 0);
            afw.writeByteArray(SNF_DITEM_DRAW_END, null, 0, 0);
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerDrawInfo.itemSave() End");
            }
        }

        public void setColor(int color) {
            mColor = color;
        }

        public void setEraseInfos(LinkedList<SerDrawInfo> eraseInfos) {
            mEraseInfos = new SerEraseInfo[eraseInfos.size()];
            eraseInfos.toArray(mEraseInfos);
        }

        public void setPaintTool(int toolCode) {
            mPaintTool = toolCode;
        }

        public void setStrokeWidth(float strokeWidth) {
            mStrokeWidth = strokeWidth;
        }
        
        public void setAlpha(int alpha) { // Better
        	mAlpha = (short)alpha;
        }
        
    }

    public static class SerEraseInfo extends SerPointInfo {
        private static final long serialVersionUID = 1L;
        @Deprecated
        private int mId;
        @Deprecated
        private int mVisible = 0;
        
        private boolean mHavePress = false;

        public SerEraseInfo() {
        }

        public SerEraseInfo(int id, boolean visible) {
            mId = id;
            if (visible) {
                mVisible = 1;
            }
            mHavePress = true;
        }

        @Override
        public DrawInfo getDrawInfoLock(String directory) {
            Paint paint = new Paint();
            EraseDrawInfo eraseInfo;
            PaintSelector.initPaint(paint, mColor, mStrokeWidth);
            PaintSelector.setErase(paint);
            if(!mHavePress && mPageVersion <= MetaData.ITEM_VERSION_302)
            {
            	eraseInfo = new EraseDrawInfo(new EraseDrawTool(), paint, mPoints,false);
            }
            else
            {
            	eraseInfo = new EraseDrawInfo(new EraseDrawTool(), paint, mPoints,true);
            }
            eraseInfo.setPageVersion(mPageVersion);
            eraseInfo.setVisible(true);
            return eraseInfo;
        }

        @Override
        public void itemLoad(AsusFormatReader afr) throws IOException {
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerEraseInfo.itemLoad() Begin");
            }
            super.itemLoad(afr);
            Item item = afr.readItem();
            while (item != null) {
                switch (item.getId()) {
                    case SNF_DITEM_ERASE_BEGIN:
                        break;
                    case SNF_DITEM_ERASE_ID:
                        mId = item.getIntValue();
                        break;
                    case SNF_DITEM_ERASE_VISIBLE:
                        mVisible = item.getIntValue();
                        break;
                    case SNF_DITEM_ERASE_END:
                        if (DEBUG_SAVE) {
                            Log.d(TAG, "SerEraseInfo.itemLoad() End");
                        }
                        return;
                }
                item = afr.readItem();
            }
        }

        @Override
        public void itemSave(AsusFormatWriter afw) throws IOException {
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerEraseInfo.itemSave() Begin");
            }
            afw.writeByteArray(SNF_DITEM_ERASE_BEGIN, null, 0, 0);
            super.itemSave(afw);
            afw.writeByteArray(SNF_DITEM_ERASE_END, null, 0, 0);
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerEraseInfo.itemSave() End");
            }
        }
    }

    public static class SerGraphicInfo extends SerDrawInfo {
        private static final long serialVersionUID = 1L;
        private String mFileName;
        private float[] mMatrixValues = null;
        private int mWidth = 0;
        private int mHeight = 0;

		@Override
        protected DrawInfo getDrawInfoLock(String directory) {
			//begin jason
			int type=-1;
			if ((type = checkIShape(mFileName))!=-1) {
				 GraphicDrawInfo shapeDrawInfo=new GraphicDrawInfo(type, new GraphicDrawTool(type), new Paint(Paint.DITHER_FLAG));
				
				 shapeDrawInfo.setFileName(mFileName);
				 shapeDrawInfo.setWidth(mWidth);
				 shapeDrawInfo.setHeight(mHeight);
				 Path pp=ShapeManager.getInstance().GetShapeById(type).mPath;
				 shapeDrawInfo.setShapeGraphicPath(pp);
				 shapeDrawInfo.initControlPointsForShape();
				 Matrix m=new Matrix();
				 if (mMatrixValues!=null) {
					m.setValues(mMatrixValues);
				 }				
				 shapeDrawInfo.transform(m);
				 shapeDrawInfo.reSetPaint(GraphicDrawInfo.createPaintForShapeGraphic(shapeDrawInfo));
				 return shapeDrawInfo;
			}
			//end jason
            Bitmap bitmap = null;
            Matrix matrix = new Matrix();
            String path = directory + "/" + mFileName;
            try {
                bitmap = BitmapFactory.decodeFile(path);
            }
            catch (OutOfMemoryError e) {
                Log.w(TAG, "[OutOfMemoryError] SerGraphicInfo.getDrawInfoLock() failed !!!");
            }
            if (bitmap != null) {
                GraphicDrawInfo drawInfo = new GraphicDrawInfo(bitmap, new GraphicDrawTool(bitmap), new Paint(Paint.DITHER_FLAG));
                if (mMatrixValues != null) {
                    matrix.setValues(mMatrixValues);
                    drawInfo.transform(matrix);
                }

                drawInfo.setFileName(mFileName);
                drawInfo.setWidth(mWidth);
                drawInfo.setHeight(mHeight);
                return drawInfo;
            }
            return null;
        }
		//begin jason
		//ishape_t1_25825745741.ishape
		public static final String SHAPE_HEAD="ishape_t";
		public static final String SHAPE_END=".ishape";
		public static final String SHAPE_LINE="_";
		private static int checkIShape(String name){
			int ret=-1;
			if (name==null||name.equals("")) {
				return -1;
			}
			final String res=name;
			if (res.startsWith(SHAPE_HEAD)&&res.endsWith(SHAPE_END)) {
				String cut=res.substring(SHAPE_HEAD.length());
				if (cut.length()>0) {
					int index=cut.lastIndexOf(SHAPE_LINE);
					if (index>0) {
						String type=cut.substring(0, index);
						try {
							ret=Integer.parseInt(type);
						} catch (Exception e) {
							// TODO: handle exception
							ret=-1;
						}
					}
				}
			}
			return ret;
		}
		//end jason
        public String getFileName() {
            return mFileName;
        }
        
        public int getWidth() {
        	return mWidth;
        }
        
        public void setWidth(int width) {
        	mWidth = width;
        }
        
        public int getHeight() {
        	return mHeight;
        }
        
        public void setHeight(int height) {
        	mHeight = height;
        }

        @Override
        protected boolean isEmpty() {
            return mFileName == null;
        }

        @Override
        public void itemLoad(AsusFormatReader afr) throws IOException {
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerGraphicInfo.itemLoad() Begin");
            }
            super.itemLoad(afr);
            Item item = afr.readItem();
            while (item != null) {
                switch (item.getId()) {
                    case SNF_DITEM_GRAPHIC_BEGIN:
                        break;
                    case SNF_DITEM_GRAPHIC_FILE_NAME:
                        mFileName = item.getStringValue();
                        break;
                    case SNF_DITEM_GRAPHIC_MATRIX_VALUES:
                        mMatrixValues = item.getFloatArray();
                        break;
                    case SNF_DITEM_GRAPHIC_WIDTH:
                    	mWidth = item.getIntValue();
                    	break;
                    case SNF_DITEM_GRAPHIC_HEIGHT:
                    	mHeight = item.getIntValue();
                    	break;
                    case SNF_DITEM_GRAPHIC_END:
                        if (DEBUG_SAVE) {
                            Log.d(TAG, "SerGraphicInfo.itemLoad() End");
                        }
                        return;
                }
                item = afr.readItem();
            }

        }

        @Override
        public void itemSave(AsusFormatWriter afw) throws IOException {
            if (mFileName == null) {
                // Empty object, discard it
                return;
            }
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerGraphicInfo.itemSave() Begin");
            }
            afw.writeByteArray(SNF_DITEM_GRAPHIC_BEGIN, null, 0, 0);
            super.itemSave(afw);
            afw.writeString(SNF_DITEM_GRAPHIC_FILE_NAME, mFileName);
            afw.writeFloatArray(SNF_DITEM_GRAPHIC_MATRIX_VALUES, mMatrixValues, 0, mMatrixValues.length);
            afw.writeInt(SNF_DITEM_GRAPHIC_WIDTH, mWidth);
            afw.writeInt(SNF_DITEM_GRAPHIC_HEIGHT, mHeight);
            afw.writeByteArray(SNF_DITEM_GRAPHIC_END, null, 0, 0);
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerGraphicInfo.itemSave() End");
            }
        }

        public void setFileName(String name) {
            mFileName = name;
        }

        public void setTransform(Matrix matrix) {
            if (matrix != null) {
                mMatrixValues = new float[9];
                matrix.getValues(mMatrixValues);
            }
        }
    }

	// BEGIN: Show
	public static class SerTextImgInfo extends SerDrawInfo {
		private static final long serialVersionUID = 1L;
		private transient Editable mContent; // Better
		private transient String mTextImgString; // Better
		private String mFileName;
		private float[] mMatrixValues = null;
		
		private int mWidth = 0;
		private int mHeight = 0;
		
		private transient ArrayList<NoteItem.NoteItemSaveData> mNoteItemDataList = null;
		
		// BEGIN: Better
		private void writeObject(java.io.ObjectOutputStream out)
				throws IOException {
			out.defaultWriteObject();
			
			NoteItem[] spanItems = mContent.getSpans(0, mContent.length(),
					NoteItem.class);
			
			int count = spanItems.length + 1;
			out.writeInt(count);
			
			NoteStringItem strItem = new NoteStringItem(mContent.toString());
			out.writeObject(strItem.save());

			for (int i = 0; i < spanItems.length; i++) {
				int spanstart = mContent.getSpanStart(spanItems[i]);
				int spanend = mContent.getSpanEnd(spanItems[i]);

				spanItems[i].setStart(spanstart);
				spanItems[i].setEnd(spanend);
			}
			
			for (NoteItem item : spanItems) {
				out.writeObject(item.save());
			}
		}

		private void readObject(java.io.ObjectInputStream in) 
				throws IOException, ClassNotFoundException, 
				InstantiationException, IllegalAccessException {
			in.defaultReadObject();
			
			int count = in.readInt();
			mNoteItemDataList = new ArrayList<NoteItem.NoteItemSaveData>();
			
			while (count > 0) {
				Object object = in.readObject();
                mNoteItemDataList.add((NoteItem.NoteItemSaveData) object);
                count--;
			}
		}
		
		public ArrayList<NoteItem.NoteItemSaveData> getNoteItemDataList() {
			return mNoteItemDataList;
		}
		// END: Better

		@Override
		protected DrawInfo getDrawInfoLock(String directory) {
			Bitmap bitmap = null;
			Matrix matrix = new Matrix();
			String path = directory + "/" + mFileName;

			try {
				bitmap = BitmapFactory.decodeFile(path);
			} catch (OutOfMemoryError e) {
				Log.w(TAG,
						"[OutOfMemoryError] SerTextImgInfo.getDrawInfoLock() failed !!!");
			}
			if (bitmap != null) {
				TextImgDrawInfo drawInfo = new TextImgDrawInfo(bitmap,
						mContent, new TextImgDrawTool(bitmap, mContent),
						new Paint(Paint.DITHER_FLAG));
				if (mMatrixValues != null) {
					matrix.setValues(mMatrixValues);
					drawInfo.transform(matrix);
				}
				drawInfo.setContent(mContent);
				drawInfo.setFileName(mFileName);
				drawInfo.setWidth(mWidth);
				drawInfo.setHeight(mHeight);
				return drawInfo;
			}
			return null;
		}

		public String getFileName() {
			return mFileName;
		}

		public Editable getContent() {
			return mContent;
		}
		
		public int getWidth() {
			return mWidth;
		}
		
		public void setWidth(int width) {
			mWidth = width;
		}
		
		public int getHeight() {
			return mHeight;
		}
		
		public void setHeight(int height) {
			mHeight = height;
		}

		@Override
		protected boolean isEmpty() {
			return mContent == null;
		}

		@Override
		public void itemLoad(AsusFormatReader afr) throws IOException {
			if (DEBUG_SAVE) {
				Log.d(TAG, "SerTextImgInfo.itemLoad() Begin");
			}
			super.itemLoad(afr);
			
			ArrayList<NoteItem> noteItem = new ArrayList<NoteItem>(); // Better
			
			Item item = afr.readItem();

			while (item != null) {
				switch (item.getId()) {
				case SNF_DITEM_TEXTIMG_BEGIN:
					break;
				case SNF_DITEM_TEXTIMG_CONTENT:
					mTextImgString = item.getStringValue();
					break;
				case SNF_NITEM_TEXTSTYLE_BEGIN:
					NoteTextStyleItem styleItem = new NoteTextStyleItem();
					styleItem.itemLoad(afr);
					noteItem.add(styleItem);
					break;
				case SNF_NITEM_FCOLOR_BEGIN:
					NoteForegroundColorItem fColorItem = new NoteForegroundColorItem();
					fColorItem.itemLoad(afr);
					noteItem.add(fColorItem);
					break;
				 case SNF_NITEM_HANDWRITE_BEGIN:
                     NoteHandWriteItem hwItem = new NoteHandWriteItem();
                     hwItem.itemLoad(afr);
                     hwItem.genPaths();
                     noteItem.add(hwItem);
                     break;
                 case SNF_NITEM_HANDWRITEBL_BEGIN:
                     NoteHandWriteBaselineItem hwblItem = new NoteHandWriteBaselineItem();
                     hwblItem.itemLoad(afr);
                     hwblItem.genPaths();
                     noteItem.add(hwblItem);
                     break;
				case SNF_DITEM_TEXTIMG_MATRIX_VALUES:
					mMatrixValues = item.getFloatArray();
					break;
				case SNF_DITEM_TEXTIMG_NAME:
					mFileName = item.getStringValue();
					break;
				case SNF_DITEM_TEXTIMG_WIDTH:
					mWidth = item.getIntValue();
					break;
				case SNF_DITEM_TEXTIMG_HEIGHT:
					mHeight = item.getIntValue();
					break;
				case SNF_DITEM_TEXTIMG_END:
					if (DEBUG_SAVE) {
						Log.d(TAG, "SerTextImgInfo.itemLoad() End");
					}
					mContent = new SpannableStringBuilder(mTextImgString);					
					for (int i = 0; i < noteItem.size(); i++) {
						NoteItem curItem = noteItem.get(i);
						if (curItem.getStart() < 0
								|| curItem.getEnd() < 0
								|| curItem.getStart() > mContent.length()
								|| curItem.getEnd() > mContent.length()) {
							Log.w(TAG, "This NoteItem is wrong."
									+ " noteItem = "
									+ curItem.getClass().getSimpleName()
									+ " start = " + curItem.getStart()
									+ " end = " + curItem.getEnd());
							continue;
						}
						mContent.setSpan(curItem, curItem.getStart(),
								curItem.getEnd(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					return;
				}
				item = afr.readItem();
			}

		}

		@Override
		public void itemSave(AsusFormatWriter afw) throws IOException {

			if (DEBUG_SAVE) {
				Log.d(TAG, "SerTextImgInfo.itemSave() Begin");
			}
			// text part
			if (mContent != null) {
				mTextImgString = mContent.toString();
			}
			else {
				mTextImgString = "";
			}
			// span part
			NoteItem[] spanItems = mContent.getSpans(0, mContent.length(),
					NoteItem.class);
			for (int i = 0; i < spanItems.length; i++) {
				int spanstart = mContent.getSpanStart(spanItems[i]);
				int spanend = mContent.getSpanEnd(spanItems[i]);

				spanItems[i].setStart(spanstart);
				spanItems[i].setEnd(spanend);
			}
			
			afw.writeByteArray(SNF_DITEM_TEXTIMG_BEGIN, null, 0, 0);
			super.itemSave(afw);
			afw.writeString(SNF_DITEM_TEXTIMG_CONTENT, mTextImgString);
			afw.writeFloatArray(SNF_DITEM_TEXTIMG_MATRIX_VALUES, mMatrixValues,
					0, mMatrixValues.length);
			afw.writeString(SNF_DITEM_TEXTIMG_NAME, mFileName);
			for (NoteItem item : spanItems) {
				if (item instanceof AsusFormat) {
					AsusFormat af = (AsusFormat) item;
					af.itemSave(afw);
				}
			}
			afw.writeInt(SNF_DITEM_TEXTIMG_WIDTH, mWidth);
			afw.writeInt(SNF_DITEM_TEXTIMG_HEIGHT, mHeight);
			afw.writeByteArray(SNF_DITEM_TEXTIMG_END, null, 0, 0);
			if (DEBUG_SAVE) {
				Log.d(TAG, "SerTextImgInfo.itemSave() End");
			}
		}

		public void setFileName(String name) {
			mFileName = name;
		}

		public void setTransform(Matrix matrix) {
			if (matrix != null) {
				mMatrixValues = new float[9];
				matrix.getValues(mMatrixValues);
			}
		}

		public void setContent(Editable content) {
			// TODO Auto-generated method stub
			mContent = content;
		}
	}
	// END:Show
	
    public static class SerPointInfo extends SerDrawInfo {
        private static final long serialVersionUID = 1L;
        protected short[] mPoints; //by wendy
        protected float[] mExtraInfo;//by Richard

        @Override
        protected DrawInfo getDrawInfoLock(String directory) {
            Paint paint = new Paint();
            PaintSelector.initPaint(paint, mColor, mStrokeWidth);
            DrawInfo drawInfo = null;
            switch (mPaintTool) {
                case PathDrawTool.NORMAL_TOOL:
                    PaintSelector.setNormal(paint);
                    drawInfo = new PathDrawInfo(new PathDrawTool(mPaintTool), paint, mPoints);
                    break;
                case NeonDrawTool.NEON_TOOL:
                    PaintSelector.setNeon(paint);
                    if(mPageVersion <= MetaData.ITEM_VERSION_303)
                    {
                    		//Carrot: new version of pens
                            drawInfo = new PathDrawInfo(new AirBrushDrawTool(), paint, mPoints);
                    }
                    else
                    {
                    		short[] pointsNoPressure = NeonDrawInfo.ConvertFromPressure(mPoints);                    		
                            drawInfo = new PathDrawInfo(new AirBrushDrawTool(), paint, pointsNoPressure);
                    }
                    drawInfo.setPageVersion(mPageVersion);
                    break;
                //end noah
                //BEGIN: RICHARD
                case ShapeDrawTool.ARC_TOOL:
                	PaintSelector.setNormal(paint);
                	drawInfo = new ArcDrawInfo(new ShapeDrawTool(mPaintTool), paint, mPoints,mExtraInfo);
                	break;
                //END: RICHARD
                case DrawTool.SCRIBBLE_TOOL:
                    PaintSelector.setScribble(paint);                  	
                    drawInfo = new PenDrawInfo(new PenDrawTool(mPaintTool), paint, mPoints);
                    ((PenDrawInfo)drawInfo).SetTexture(DoodleView.getPenTexure());
                    break;
                case DrawTool.SKETCH_TOOL:
                    PaintSelector.setSketch(paint);
                        drawInfo = new PencilDrawInfo(new PencilDrawTool(), paint, mPoints);
                        ((PencilDrawInfo)drawInfo).SetTexture(DoodleView.getPencilTexure());
                    break;
                case PathDrawTool.ERASE_TOOL:
                    PaintSelector.setErase(paint);
                    if(mPageVersion <= MetaData.ITEM_VERSION_302)
                    {
                    	drawInfo = new EraseDrawInfo(new EraseDrawTool(), paint, mPoints,false);
                    }
                    else
                    {
                    	drawInfo = new EraseDrawInfo(new EraseDrawTool(), paint, mPoints,true);
                    }
                    drawInfo.setPageVersion(mPageVersion);
                    break;
                    //begin wendy
                case PathDrawTool.MARKPEN_TOOL:
                	PaintSelector.setMarkPen(paint);
                	paint.setAlpha(mAlpha); // Better

	                	//Carrot: new version of pens
	                	drawInfo = new MarkerDrawInfo(new MarkerDrawTool(mPaintTool),paint,mPoints);
                	break;
                case PathDrawTool.WRITINGBRUSH_TOOL:
                	PaintSelector.setWritingBrush(paint);
	                	//Carrot: new version of pens
	                	drawInfo = new BrushDrawInfo(new BrushDrawTool(mPaintTool),paint,mPoints);
	                	((BrushDrawInfo)drawInfo).SetTexture(DoodleView.getBrushTexure());

                	break;
                	//end wendy
            }

            return drawInfo;
        }

        @Override
        protected boolean isEmpty() {
            return (mPoints == null) || (mPoints.length == 0);
        }

        @Override
        public void itemLoad(AsusFormatReader afr) throws IOException {
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerPointInfo.itemLoad() Begin");
            }
            super.itemLoad(afr);
            Item item = afr.readItem();
            while (item != null) {
                switch (item.getId()) {
                    case SNF_DITEM_POINT_BEGIN:
                        break;
                    case SNF_DITEM_POINT_POINTS:
                    	mPoints = item.getShortArray();
                        break;
                    //BEGIN: RICHARD
                    case SNF_DITEM_EXTRA_BEGIN:
                    	break;
                    case SNF_DITEM_EXTRA_END:
                    	break;
                    case SNF_DITEM_EXTRA_INFO:
                    	mExtraInfo = item.getFloatArray();
						if(mExtraInfo == null)
                    	{                    		
                    		short[] points = item.getShortArray();
                    		mExtraInfo = new float[points.length];
                    		for(int i = 0; i < points.length; i++)
                    		{
                    			mExtraInfo[i] = (float)points[i];
                    		}
                    	}
                    	break;
                    //END: RICHARD    
                    case SNF_DITEM_POINT_END:
                        if (DEBUG_SAVE) {
                            Log.d(TAG, "SerPointInfo.itemLoad() End");
                        }
                        return;
                }
                item = afr.readItem();
            }
        }

        @Override
        public void itemSave(AsusFormatWriter afw) throws IOException {
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerPointInfo.itemSave() Begin");
            }
            afw.writeByteArray(SNF_DITEM_POINT_BEGIN, null, 0, 0);
            super.itemSave(afw);
            
            //BEGIN: RICHARD
            if(mExtraInfo != null && mExtraInfo.length > 0)
            {
            	afw.writeByteArray(SNF_DITEM_EXTRA_BEGIN, null, 0, 0);
            	afw.writeFloatArray(SNF_DITEM_EXTRA_INFO, mExtraInfo, 0, mExtraInfo.length);
            	afw.writeByteArray(SNF_DITEM_EXTRA_END, null, 0, 0);
            }
            //END: RICHARD
            
            if (mPoints != null) {
                afw.writeShortArray(SNF_DITEM_POINT_POINTS, mPoints, 0, mPoints.length);
            }
            afw.writeByteArray(SNF_DITEM_POINT_END, null, 0, 0);
            if (DEBUG_SAVE) {
                Log.d(TAG, "SerPointInfo.itemSave() End");
            }
        }

        public void setPoints(LinkedList<Point> pointList) {
            int index = 0;

            mPoints = new short[pointList.size() * 2];
            for (Point point : pointList) {
                if (point != null) {
                    mPoints[index++] = (short) point.x;
                    mPoints[index++] = (short) point.y;
                }
            }
        }
        
        //begin wendy
        public void setPoints(LinkedList<Point> pointList, boolean hasPressure)
        {
        	 int index = 0;

             mPoints = new short[pointList.size() * 3];
             for (Point point : pointList) {
                 if (point != null) {
                     mPoints[index++] = (short) point.x;
                     mPoints[index++] = (short) point.y;
                     mPoints[index++] = (short) (point.pressure * MetaData.PRESSURE_FACTOR);
                 }
             }
        }

        public void setPoints(Point[] points) {
            int index = 0;
            mPoints = new short[points.length * 2];
            for (Point point : points) {
                if (point != null) {
                    mPoints[index++] = (short) point.x;
                    mPoints[index++] = (short) point.y;
                }
            }
        }

        public void setPoints(float[] points) {
            mPoints = new short[points.length];
            int index = 0;
            for (float point : points) {
                mPoints[index++] = (short) point;
            }
        }
        
        //BEGIN: RICHARD
        public void setExtraInfo(float[] info)
        {
        	mExtraInfo = new float[info.length];
            int index = 0;
            for (float fi : info) {
            	mExtraInfo[index++] =  fi;
            }
        }
        //END: RICHARD
    }

    private static final boolean DEBUG_SAVE = DoodleView.DEBUG_SAVE;
    private static final String TAG = DoodleView.TAG;
    private boolean mValidFile = false;
    protected short mCanvasWidth;
    protected short mCanvasHeight;
    private static final long serialVersionUID = 1L;
    private SerDrawInfo[] mSerInfoArray;

	public DoodleItem() {
    }

    public DoodleItem(int canvasWidth, int canvasHeight) {
        mCanvasWidth = (short) canvasWidth;
        mCanvasHeight = (short) canvasHeight;
    }

    public void addInfos(List<SerDrawInfo> serDrawInfos) {
        if (serDrawInfos == null) {
            return;
        }
        int size = serDrawInfos.size();
        mSerInfoArray = new SerDrawInfo[size];
        serDrawInfos.toArray(mSerInfoArray);
    }

    public int getCanvasHeight() {
        return mCanvasHeight;
    }

    public int getCanvasWidth() {
        return mCanvasWidth;
    }

    public boolean getParsingResult() {
        return mValidFile;
    }

    @Override
    public void itemLoad(AsusFormatReader afr) throws IOException {
        if (DEBUG_SAVE) {
            Log.d(TAG, "DoodleItem.itemLoad() Begin");
        }
        LinkedList<SerDrawInfo> serDrawInfos = new LinkedList<SerDrawInfo>();
        SerDrawInfo drawInfo = null;
        Item item = afr.readItem();
        if (item == null) {
            mValidFile = true;
            return;
        }
        if (item.getId() != SNF_DITEM_BEGIN) {
            mValidFile = false;
            return;
        }
        short tempVersion = 0;
        while (item != null) {
            switch (item.getId()) {
                case SNF_DITEM_BEGIN:
                    if (DEBUG_SAVE) {
                        Log.i(TAG, "DoodleItem itemLoad begin");
                    }
                    mValidFile = true;
                    break;
                case SNF_DITEM_VERSION:
                	if (!mValidFile) {
                		return;
                	}
                	tempVersion = item.getShortValue();
                	if (!PageDataLoader.isFileDataFormatVersionSupported(tempVersion)) {
                		mValidFile = false;
                		return;
                	}
                	break;
                case SNF_DITEM_END:
                    if (DEBUG_SAVE) {
                        Log.i(TAG, "DoodleItem itemLoad end");
                    }
                    int size = serDrawInfos.size();
                    if (size > 0) {
                        mSerInfoArray = new SerDrawInfo[size];
                        serDrawInfos.toArray(mSerInfoArray);
                    }
                    if (DEBUG_SAVE) {
                        Log.d(TAG, "DoodleItem.itemLoad() End");
                    }
                    mValidFile = true;
                    return;
                case SNF_DITEM_CANVAS_HEIGHT:
                case SNF_DITEM_CANVAS_HEIGHT_ERROR: // TODO: Remove this case
                    mCanvasHeight = item.getShortValue();
                    break;
                case SNF_DITEM_CANVAS_WIDTH:
                    mCanvasWidth = item.getShortValue();
                    break;
                case SNF_DITEM_POINT_BEGIN:
                    drawInfo = new SerPointInfo();
                    drawInfo.setPageVersion(tempVersion);//RICHARD
                    drawInfo.itemLoad(afr);
                    break;
                case SNF_DITEM_ERASE_BEGIN:
                    drawInfo = new SerEraseInfo();
                    drawInfo.setPageVersion(tempVersion);//RICHARD
                    drawInfo.itemLoad(afr);
                    break;
                case SNF_DITEM_GRAPHIC_BEGIN:
                    drawInfo = new SerGraphicInfo();
                    drawInfo.setPageVersion(tempVersion);//RICHARD
                    drawInfo.itemLoad(afr);
                    break;
                // BEGIN: Show
                case SNF_DITEM_TEXTIMG_BEGIN:
                	drawInfo = new SerTextImgInfo();
                	drawInfo.setPageVersion(tempVersion);//RICHARD
                	drawInfo.itemLoad(afr);
                	break;
				// END: Show
                case SNF_DITEM_ANNOTATION_BEGIN:
                    drawInfo = new SerAnnotationInfo();
                    drawInfo.setPageVersion(tempVersion);//RICHARD
                    drawInfo.itemLoad(afr);
                    break;
            }
            if (drawInfo != null) {
                if (DEBUG_SAVE) {
                    Log.i(TAG, "item loaded: " + drawInfo.getClass().getName());
                }
//                //Begin Allen for load version 0x31 template image
//                if(mVersion==49&&drawInfo instanceof SerGraphicInfo&&((SerGraphicInfo)drawInfo).isTemplate()){
//                	templateImageFileName = ((SerGraphicInfo)drawInfo).getFileName();
//                	return;
//                }
//                //End Allen
                drawInfo.setPageVersion(tempVersion);
                serDrawInfos.add(drawInfo);
                drawInfo = null;
            }
            item = afr.readItem();
        }
    }

    @Override
    public void itemSave(AsusFormatWriter afw) throws IOException {
        if (DEBUG_SAVE) {
            Log.d(TAG, "DoodleItem.itemSave() Begin");
        }
        afw.writeByteArray(SNF_DITEM_BEGIN, null, 0, 0);
        afw.writeShort(AsusFormat.SNF_DITEM_VERSION, MetaData.ITEM_VERSION);
        afw.writeShort(SNF_DITEM_CANVAS_HEIGHT, mCanvasHeight);
        afw.writeShort(SNF_DITEM_CANVAS_WIDTH, mCanvasWidth);
        afw.writeByteArray(SNF_DITEM_DRAWS_BEGIN, null, 0, 0);
        for (SerDrawInfo serInfo : mSerInfoArray) {
            if (serInfo != null && !serInfo.isEmpty()) {
                serInfo.itemSave(afw);
            }
        }
        afw.writeByteArray(SNF_DITEM_DRAWS_END, null, 0, 0);
        afw.writeByteArray(SNF_DITEM_END, null, 0, 0);
        if (DEBUG_SAVE) {
            Log.d(TAG, "DoodleItem.itemSave() End");
        }
    }

    public LinkedList<DrawInfo> load(String directory) {
        LinkedList<DrawInfo> list = new LinkedList<DrawInfo>();
        if (mSerInfoArray != null) {
            for (SerDrawInfo info : mSerInfoArray) {
                if (DEBUG_SAVE) {
                    Log.d(TAG, "info load: " + info);
                }
                if (info != null) {
                    DrawInfo drawInfo = info.getDrawInfo(directory);
                    if (drawInfo != null) {
                        list.add(drawInfo);
                    }
                }
            }
        }
        return list;
    }

    public void save(LinkedList<DrawInfo> drawInfoList, NotePage note) {
        int index = 0;
        if (note == null) {
            return;
        }
        mSerInfoArray = new SerDrawInfo[drawInfoList.size()];
        for (DrawInfo info : drawInfoList) {
            if (DEBUG_SAVE) {
                Log.d(DoodleView.TAG, "save info: " + info);
            }
            if (info != null) {
                mSerInfoArray[index] = info.save(note);
            }
            index++;
        }
    }

    public int size() {
        if (mSerInfoArray != null) {
            return mSerInfoArray.length;
        }
        return 0;
    }
}
