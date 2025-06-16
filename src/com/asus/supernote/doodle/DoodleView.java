package com.asus.supernote.doodle;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.Thread.State;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.ContentValues;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.widget.TextView;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.InputManager;
import com.asus.supernote.PaintSelector;
import com.asus.supernote.R;
import com.asus.supernote.SuperNoteApplication;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.doodle.DoodleItem.SerDrawInfo;
import com.asus.supernote.doodle.drawinfo.AnnotationDrawInfo;
import com.asus.supernote.doodle.drawinfo.ArcDrawInfo;
import com.asus.supernote.doodle.drawinfo.BallPenDrawInfo;
import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.EraseDrawInfo;
import com.asus.supernote.doodle.drawinfo.GraphicDrawInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo.Point;
import com.asus.supernote.doodle.drawinfo.SelectionDrawInfo;
import com.asus.supernote.doodle.drawinfo.SpotDrawInfo;
import com.asus.supernote.doodle.drawinfo.TextImgDrawInfo;
import com.asus.supernote.doodle.drawinfo.TransformDrawInfo;
import com.asus.supernote.doodle.drawtool.AirBrushDrawTool;
import com.asus.supernote.doodle.drawtool.AnnotationDrawTool;
import com.asus.supernote.doodle.drawtool.BrushDrawTool;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawtool.EraseDrawTool;
import com.asus.supernote.doodle.drawtool.GraphicDrawTool;
import com.asus.supernote.doodle.drawtool.MarkerDrawTool;
import com.asus.supernote.doodle.drawtool.MyPoint;
import com.asus.supernote.doodle.drawtool.PathDrawTool;
import com.asus.supernote.doodle.drawtool.PenDrawTool;
import com.asus.supernote.doodle.drawtool.PencilDrawTool;
import com.asus.supernote.doodle.drawtool.SelectionDrawTool;
import com.asus.supernote.doodle.drawtool.ShapeDrawTool;
import com.asus.supernote.doodle.drawtool.TextImgDrawTool;
import com.asus.supernote.editable.LongClickDetector;
import com.asus.supernote.editable.NoteEditText;
import com.asus.supernote.editable.PageEditorWrapper;
import com.asus.supernote.editable.attacher.CameraAttacher;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.ga.GACollector;
import com.asus.supernote.inksearch.AsusShape;
import com.asus.supernote.inksearch.CFG;
import com.visionobjects.myscript.shape.DecorationType;
import com.visionobjects.myscript.shape.ShapeDecoratedEllipticArcData;
import com.visionobjects.myscript.shape.ShapeDecoratedLineData;
import com.visionobjects.myscript.shape.ShapeDocument;
import com.visionobjects.myscript.shape.ShapeEllipticArcData;
import com.visionobjects.myscript.shape.ShapeLineData;
import com.visionobjects.myscript.shape.ShapePointData;

public class DoodleView extends SurfaceView implements SurfaceHolder.Callback,
        SelectionDrawInfo.OnDeleteListener, LongClickDetector.OnLongClickListener , OnGestureListener {

    public class ActionRecord {
        public static final int ACTION_ADD = 0;
        public static final int ACTION_DELETE = 1;
        public static final int ACTION_GROUP = 2;
        public static final int ACTION_UNGROUP = 3;
        public static final int ACTION_TRANSFORM = 4;
        public static final int ACTION_GROUP_ADD = 5;
        public static final int ACTION_GROUP_ADD_NEW = 6;
        public static final int ACTION_SHAPE = 7;
        public static final int ACTION_SHAPE_AUTO_ADD = 8;

        private int mAction;
        private Matrix mMatrix;
        private LinkedList<DrawInfo> mReferences = new LinkedList<DrawInfo>();

        private int mShapeDocumentIndex = -1;

        public ActionRecord(int action, Collection<DrawInfo> references, Matrix transform) {
            mAction = action;
            mReferences.addAll(references);
            mMatrix = transform;
        }

        public ActionRecord(int action, DrawInfo reference, Matrix transform) {
            mAction = action;
            mReferences.add(reference);
            mMatrix = transform;
        }
        
        //BEGIN: RICHARD
        public ActionRecord(int action,int index)
        {
        	mAction = action;
        	mShapeDocumentIndex = index;
        }
        
        public int getAction() {
			return mAction;
		}
        //END: RICHARD

        private void add() {
            for (DrawInfo drawInfo : mReferences) {
                LinkedList<EraseDrawInfo> eraseInfos = drawInfo.getEraseDrawInfos();
                if (drawInfo instanceof EraseDrawInfo) {
                    for (DrawInfo info : mObjectList) {
                        info.add((EraseDrawInfo) drawInfo, true);
                    }
                }
                
                // BEGIN: Better
    			int pageNum = addToPageOrderedDrawList(drawInfo);
    			if (pageNum > mPageEditor.getPageNum()) {
    				mPageEditor.setPageNum(pageNum);
    				onNewPageAdded();
    			}
    			// END: Better
                
                mDrawList.addFirst(drawInfo);
                if ((eraseInfos != null) && (eraseInfos.size() > 0)) {
                    for (EraseDrawInfo eraseInfo : eraseInfos) {
                        if (!mDrawList.contains(eraseInfo)) {
                        	// BEGIN: Better
                        	DrawInfo info = eraseInfo.clone();
                        	
                			pageNum = addToPageOrderedDrawList(info);
                			if (pageNum > mPageEditor.getPageNum()) {
                				mPageEditor.setPageNum(pageNum);
                				onNewPageAdded();
                			}
                			
                            mDrawList.add(info);
                            // END: Better
                        }
                    }
                }
    	        if(drawInfo instanceof GraphicDrawInfo)
    	        {
    	        	int iCount = 0;
	                String name = ((GraphicDrawInfo)drawInfo).getFileName();
		        	for(DrawInfo drawInfoTemp : mDrawList)
		        	{
		        		if(drawInfoTemp instanceof GraphicDrawInfo)
		    	        {
		        			String nameTemp = ((GraphicDrawInfo)drawInfoTemp).getFileName();
		        			if(nameTemp.equalsIgnoreCase(name))
		        			{
		        				iCount++;
		        			}
		        			if(iCount >= 2)//this picture has copied , we can not delete it.
		        			{
		        				break;
		        			}
		    	        }
		        	}
		        	if(iCount < 2)
		        	{
		        		mPageEditor.updateAttachmentAddNameList(name);
		        	}
		        }                
            }

            //BEGIN: RICHARD
            setDoodleItemIsEmpty();
            //END: RICHARD
        }

        private void delete() {
            for (DrawInfo drawInfo : mReferences) {
                // Remove all DrawInfo's EraseDrawInfo
                if (drawInfo instanceof EraseDrawInfo) {
                    for (DrawInfo info : mObjectList) {
                    	//BEGIN: RICHARD
                    	if(info instanceof AnnotationDrawInfo)
                    	{
                    		for(DrawInfo detailInfo : ((AnnotationDrawInfo)info).getDrawInfos())
                    		{
                    			detailInfo.remove((EraseDrawInfo) drawInfo);
                    		}
                    	}
                    	//END: RICHARD
                    	else
                    	{
                    		info.remove((EraseDrawInfo) drawInfo);
                    	}
                    }
                }
                
                // BEGIN: Better
                deleteFromPageOrderedDrawList(drawInfo);
                // END: Better
                
                mDrawList.remove(drawInfo);
            }

            //BEGIN: RICHARD
            setDoodleItemIsEmpty();
            //END: RICHARD
        }

        private void group(int state) {
            AnnotationDrawInfo group = new AnnotationDrawInfo(new AnnotationDrawTool(), new Paint());
            for (DrawInfo drawInfo : mReferences) {
            	// BEGIN: Better
                deleteFromPageOrderedDrawList(drawInfo);
                // END: Better
                
                mDrawList.remove(drawInfo);
                boolean isRedo;
                if(state == 1) {
                	isRedo = true;
                }else {
                	isRedo = false;
                }
                group.add(drawInfo, isRedo);
            }
            
            // BEGIN: Better
            int pageNum = addToPageOrderedDrawList(group);
            if (pageNum > mPageEditor.getPageNum()) {
            	mPageEditor.setPageNum(pageNum);
            	onNewPageAdded();
            }
            // END: Better
            
            mDrawList.addFirst(group);
        }

        public void redo() {
            switch (mAction) {
                case ACTION_ADD:
                    add();
                    break;
                case ACTION_DELETE:
                    delete();
                    break;
                case ACTION_GROUP:
                    group(1);
                    break;
                case ACTION_UNGROUP:
                    ungroup(1);
                    break;
                    //BEGIN: RICHARD
                case ACTION_GROUP_ADD:
                	group_add_redo();
                	break;
                case ACTION_GROUP_ADD_NEW:
                	group_add_new_redo();
                	break;
                case ACTION_SHAPE:
                	shape_redo();
                	break;
                case ACTION_SHAPE_AUTO_ADD:
                	shape_auto_add_redo();
                	break;
                	//END: RICHARD
                case ACTION_TRANSFORM:
                    transform(false);
            }
        }

        @Override
        public String toString() {
            String action = null;
            switch (mAction) {
                case ACTION_ADD:
                    action = "ACTION_ADD";
                    break;
                case ACTION_DELETE:
                    action = "ACTION_DELETE";
                    break;
                case ACTION_GROUP:
                    action = "ACTION_GROUP";
                    break;
                case ACTION_UNGROUP:
                    action = "ACTION_UNGROUP";
                    break;
                case ACTION_TRANSFORM:
                    action = "ACTION_TRANSFORM";
                    break;
                    //BEGIN: RICHARD
                case ACTION_GROUP_ADD:
                	action = "ACTION_GROUP_ADD";
                	break;
                case ACTION_GROUP_ADD_NEW:
                	action = "ACTION_GROUP_ADD_NEW";
                	break;
                case ACTION_SHAPE:
                	action = "ACTION_SHAPE";
                	break;
                case ACTION_SHAPE_AUTO_ADD:
                	action = "ACTION_SHAPE_AUTO_ADD";
                	break;					
                	//END: RICHARD
            }
            return action;
        }

        public void transform(boolean inverse) {
            if (inverse) {
                Matrix inverseMatrix = new Matrix();
                if (mMatrix.invert(inverseMatrix)) {
                    for (DrawInfo drawInfo : mReferences) {
                        drawInfo.transform(inverseMatrix);
                    }
                }
            }
            else {
                for (DrawInfo drawInfo : mReferences) {
                    drawInfo.transform(mMatrix);
                }
            }
        }

        public void undo() {
            switch (mAction) {
                case ACTION_ADD:
                    delete();
                    break;
                case ACTION_DELETE:
                    add();
                    break;
                case ACTION_GROUP:
                    ungroup(0);
                    break;
                case ACTION_UNGROUP:
                    group(0);
                    break;
                    //BEGIN: RICHARD
                case ACTION_GROUP_ADD_NEW:
                	group_add_new_undo();
                	break;
                case ACTION_GROUP_ADD:
                	group_add_undo();
                	break;
                case ACTION_SHAPE:
                	shape_undo();
                	break;
                case ACTION_SHAPE_AUTO_ADD:
                	shape_auto_add_undo();
                	break;
                	//END: RICHARD
                case ACTION_TRANSFORM:
                    transform(true);
            }
        }

        private void ungroup(int state) {
            int size = mReferences.size();
            
            // BEGIN: Better
            deleteFromPageOrderedDrawList(mDrawList.get(0));
            // END: Better
            
            mDrawList.removeFirst();
            // 0 means undo 1 means redo
            if(state == 0){
                for (int i = size - 1; i >= 0; i--) {
                	// BEGIN: Better
                	DrawInfo info = mReferences.get(i);
                	int pageNum = addToPageOrderedDrawList(info);
                	if (pageNum > mPageEditor.getPageNum()) {
                		mPageEditor.setPageNum(pageNum);
                		onNewPageAdded();
                	}
                	// END: Better
                	
                    mDrawList.addFirst(info);
                }
            }else {
                for (int i = 0; i < size; i++) {
                	// BEGIN: Better
                	DrawInfo info = mReferences.get(i);
                	int pageNum = addToPageOrderedDrawList(info);
                	if (pageNum > mPageEditor.getPageNum()) {
                		mPageEditor.setPageNum(pageNum);
                		onNewPageAdded();
                	}
                	// END: Better
                	
                    mDrawList.addFirst(info);
                }
            	
            }

        }
 
        //BEGIN: RICHARD
        private void group_add_new_redo(){
        	// BEGIN: Better
        	DrawInfo info = mReferences.getFirst();
        	deleteFromPageOrderedDrawList(info);
			mDrawList.remove(info);
			// END: Better

            AnnotationDrawInfo group = new AnnotationDrawInfo(new AnnotationDrawTool(), new Paint());
            for (DrawInfo drawInfo : mReferences) {
                group.add(drawInfo, false);
            }
            
            // BEGIN: Better
            int pageNum = addToPageOrderedDrawList(group);
            if (pageNum > mPageEditor.getPageNum()) {
            	mPageEditor.setPageNum(pageNum);
            	onNewPageAdded();
            }
            // END: Better
            
            mDrawList.addFirst(group);
        }
        //END: RICHARD        
        
        //BEGIN: RICHARD
        private void group_add_new_undo(){
        	if (mDrawList == null || mDrawList.size() == 0) { //smilefish fix IndexOutOfBoundsException
				return;
			}
        	
        	// BEGIN: Better
        	deleteFromPageOrderedDrawList(mDrawList.get(0));
        	// END: Better
        	
        	mDrawList.removeFirst();
        	
        	// BEGIN: Better
        	DrawInfo info = mReferences.getFirst();
        	int pageNum = addToPageOrderedDrawList(info);
        	if (pageNum > mPageEditor.getPageNum()) {
        		mPageEditor.setPageNum(pageNum);
        		onNewPageAdded();
        	}
        	mDrawList.add(info);
        	mLatestDrawInfoGroup = null;

        	// END: Better
        }
        //END: RICHARD  
        
        //BEGIN: RICHARD
        private void group_add_redo(){
        	DrawInfo lastGroup = mDrawList.get(0);
        	if(lastGroup == null 
        			|| !(lastGroup instanceof AnnotationDrawInfo) )
        	{
        		//something wrong, Do nothing
        		return;
        	}
        	
        	AnnotationDrawInfo temp = (AnnotationDrawInfo)lastGroup;
        	if(temp.size() != mReferences.size() -1)
        	{
        		//something wrong, Do nothing
        		return;       		
        	}
        	
        	temp.add(mReferences.getLast(), true);
        	
        	// BEGIN: Better
        	int pageNum = addToPageOrderedDrawList(mReferences.getLast());
			if (pageNum > mPageEditor.getPageNum()) {
				mPageEditor.setPageNum(pageNum);
				onNewPageAdded();
			}
			
        	pageNum = updatePageOrderedShapeDrawList(temp);
			if (pageNum > mPageEditor.getPageNum()) {
				mPageEditor.setPageNum(pageNum);
				onNewPageAdded();
			}
        	// END: Better
        }
        //END: RICHARD
        
        //BEGIN: RICHARD
        private void group_add_undo(){
			if (mDrawList == null || mDrawList.size() == 0) {
				return;
			}
        	DrawInfo lastGroup = mDrawList.get(0);
        	if(lastGroup == null )
        	{
        		//something wrong, Do nothing
        		return;
        	}
        	
        	if(lastGroup instanceof AnnotationDrawInfo) 
        	{
	        	AnnotationDrawInfo temp = (AnnotationDrawInfo)lastGroup;
	        	if(temp.size() != mReferences.size())
	        	{
	        		//something wrong, Do nothing
	        		return;       		
	        	}
	        	
	        	// BEGIN: Better
	        	deleteFromPageOrderedDrawList(mDrawList.get(0));
	        	// END: Better
	        	mDrawList.removeFirst();
	        	
	            AnnotationDrawInfo group = new AnnotationDrawInfo(new AnnotationDrawTool(), new Paint());
	            for (int i = 0; i<mReferences.size()-1; i++) {
	                group.add(mReferences.get(i), true);
	            }
	            
	            // BEGIN: Better
	        	int pageNum = addToPageOrderedDrawList(group);
	        	if (pageNum > mPageEditor.getPageNum()) {
	        		mPageEditor.setPageNum(pageNum);
	        		onNewPageAdded();
	        	}
	        	// END: Better
	        	
	        	mDrawList.addFirst(group);
	        	mLatestDrawInfoGroup = group;  //Dave. Fix the Bug: undo may not effect in some case.
        	}

        }
        //END: RICHARD

        //BEGIN: RICHARD
        private void shape_redo()
        {
        	if(mShapeDocumentIndex == -1 ||
			mAsusShape == null || 
			mShapeDocumentList == null)//darwin
        		return;
        	mAsusShape.setShapeDocument(mShapeDocumentList.get(mShapeDocumentIndex));
        	doDrawShapeDocument(mAsusShape.getShapeDocument());
        }
        //END: RICHARD
        
        //BEGIN: RICHARD
        private void shape_undo()
        {
        	if(mShapeDocumentIndex == -1 ||
			mAsusShape == null || 
			mDrawInfoForShapeList == null || 
			mShapeDocumentList == null)//darwin
        		return;
        	if(mShapeDocumentIndex == 0)
        	{
        		mAsusShape.clearStrokes();
        		
        		// BEGIN: Better
        		deleteAllFromPageOrderedShapeDrawList();
        		// END: Better
        		
        		setDoodleItemIsEmpty();//RICHARD
        	}
        	else
        	{
	        	mAsusShape.setShapeDocument(mShapeDocumentList.get(mShapeDocumentIndex - 1));
	        	doDrawShapeDocument(mAsusShape.getShapeDocument());
        	}
        }
        //END: RICHARD
        
        //BEGIN: RICHARD
        public Boolean clearShapeDocument()
        {
        	if(mShapeDocumentIndex == -1 || mShapeDocumentList == null)//darwin
        		return false;
        	
        	//here remove is right  undolist 1234  redolist 765,
        	mShapeDocumentList.get(mShapeDocumentIndex).dispose();
        	mShapeDocumentList.remove(mShapeDocumentIndex);
        	
        	return true;
        }
        //END: RICHARD
        
        //BEGIN: RICHARD
        private void shape_auto_add_redo()
        {
        	AnnotationDrawInfo group = new AnnotationDrawInfo(new AnnotationDrawTool(), new Paint());
            for (DrawInfo drawInfo : mReferences) {
                group.add(drawInfo, true);
            }
            
            // BEGIN: Better
            int pageNum = addToPageOrderedDrawList(group);
            if (pageNum > mPageEditor.getPageNum()) {
            	mPageEditor.setPageNum(pageNum);
            	onNewPageAdded();
            }
            // END: Better
            
            mDrawList.addFirst(group);
        }
        //END: RICHARD
        
        //BEGIN: RICHARD
        private void shape_auto_add_undo()
        {
        	// BEGIN: Better
        	deleteFromPageOrderedDrawList(mDrawList.get(0));
        	// END: Better
        	
            mDrawList.removeFirst();
        }
        //END: RICHARD
    }

    public static final String TAG = "DoodleView";
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_TOUCH = false;
    public static final boolean DEBUG_SAVE = false;
    private static final boolean DEBUG_INSERT = false;
    private static final boolean DEBUG_SCROLL = false;
    private static final boolean DEBUG_ACTION = false;
    private static final int COPY_SHIFT_VALUE = 40;
    private static final int HINT_STROKE_WIDTH = 4;
    public static final int GRAPHIC_UPDATE = 5; //By Show
    public static final int GRAPHIC_CHANGE = 6; //By Show
    private static final long MEMORY_USAGE_LIMIT = (long) (Runtime.getRuntime().maxMemory() * 0.9);
    private static final String INIT_SIZE_MESSAGE = "Canvas size has not been initialized !! Call the method, initCanvasSize(), first.";
    private boolean mIsDrawing = false;
    private boolean mIsNeedDrawDoodleViewContent = false;//RICHARD
    
    public static boolean isOutOfMemory() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) > MEMORY_USAGE_LIMIT;
    }

    private boolean mIsEnable = true, mIsModified = false, mIsLineDrawn = false;
    private boolean mInsertMode = false, mPt1Validate = true;
    private int mViewHeight = 10, mViewWidth = 10;
    private int mCacheHeight = 10, mCacheWidth = 10;
    private int mScrollX = 0, mScrollY = 0;
    private int mBackgroundColor = Color.WHITE;
    private int mPointer1Id = 0;
    private float mScaleX = 1.0f, mScaleY = 1.0f;
    private float mStartX = -1, mStartY = -1, mEndX, mEndY;
    private float mLongClickX = -1, mLongClickY = -1;
    private DrawTool mCurrentDrawTool = null;
    private DrawInfo mDrawInfo = null;
    private static DrawInfo mTempDrawInfo = null;//By Show
    private static Matrix mTempMatrix = null; 	  //By Show
    private Editable mEditBoxTextContent; //By Show
    // BEGIN: Better
    private CopyOnWriteArrayList<CopyOnWriteArrayList<DrawInfo>> mPageOrderedDrawList = null;
    private CopyOnWriteArrayList<CopyOnWriteArrayList<DrawInfo>> mPageOrderedGraphicDrawList = null;
    // END: Better
    // In mDrawList, the last drawn object will be placed at first position.
    private LinkedList<DrawInfo> mDrawList = null, mObjectList = null;
    private LinkedList<ActionRecord> mUndoList = null, mRedoList = null;
    private SurfaceHolder mSurfaceHolder = null;
    private Paint mCurrentPaint = null, mBitmapPaint = null, mHintPaint = null;
    private RectF mTransBounds = null, mHintRect = null;
    private PageEditorWrapper mPageEditor;
    private LongClickDetector mLongClickDetector = null;
    private ScaleGestureDetector mScaleGestureDetector = null;
    
    // BEGIN: Better
    private int NUM_CACHE = 1;
    private Bitmap[] mScrollCacheBitmap = null;// new Bitmap[NUM_CACHE];
    private Canvas[] mScrollCacheCanvas = null;//new Canvas[NUM_CACHE];
    private Bitmap[] mScrollGraphicDrawListCacheBitmap = null;//new Bitmap[NUM_CACHE];
    private Canvas[] mScrollGraphicDrawListCacheCanvas = null;//new Canvas[NUM_CACHE];
    private Bitmap mShapeCacheBitmap = null;
    private Canvas mShapeCacheCanvas = null;//new Canvas[NUM_CACHE];
    private Object mShapeBitmapLock = new Object();
    private int mCacheIndex1 = 0;
    private int mCacheIndex2 = 1;//close continues mode
    private int mCurrentPageIndex = 0;
    // END: Better
    
    //Begin: Allen
    private GestureDetector detector; 
    private boolean mIsScrolling = false;
    private boolean mIsTouchDownValidate = false;
    private float[] mMoveX = new float[2];
    private float[] mMoveY = new float[2];
    private float[] mMovePressure = new float[2];
    private int mMoveToolType = 0;
    //End: Allen
    private boolean mIsMultiTouch = false;
    
    //BEGIN: Richard
	private AsusShape mAsusShape = null;
	private ArrayList<Object> mShapeList = null;
	// BEGIN: Better
	private ArrayList<ArrayList<DrawInfo>> mDrawInfoForShapeList = null;
	// END: Better
	private Boolean mIsNeedShape = false;
	
	private ArrayList<ShapeDocument> mShapeDocumentList = null;
	
	private Time mCurrentTime = new Time();
	private Time mLastDrawInfoTime=new Time();//.setToNow();
	private AnnotationDrawInfo mLatestDrawInfoGroup = null;//new AnnotationDrawInfo(new AnnotationDrawTool(), new Paint());
	
	private static final int ARROW_DEGREE = 15;
	private static final int ARROW_LINE_LENGTH_FACTOR = 15;
	
	private DrawTool mCurrentShapeDrawTool = null;
	private Paint mCurrentShapePaint = null;
	//END: Richard
	
	//darwin
	Context mContext = null;
	//darwin
	private SharedPreferences mPreferences = null; // Better
	private Boolean mIsNeedRefreshAllPage = false;
	private Boolean mIsFirstDraw = false;
	private Boolean mIsConfigSave = false;//by show
	private Boolean mIsCacheInited = false; //smilefish
	
	// Carrot
	static private Bitmap mTexturePencil;
	static private Bitmap mTexturePen;
	static private Bitmap mTextureBrush;
    private Bitmap mCarrotCacheBitmap = null;
    private Canvas mCarrotCacheCanvas = null;
    private Object mCarrotCacheBitmapLock = new Object();
    
    //+++ Dave for GA
    private int drawGACount = 0;
    private int fingerGACount = 0;
    //---
    
    //emmanual
	private int mRollerBrushCount = 0;
	private int mPenBrushCount = 0;
	private int mBrushBrushCount = 0;
	private int mAirBrushCount = 0;
	private int mPencilBrushCount = 0;
	private int mMarkerBrushCount = 0;
	
    private CountDownTimer mHintTimer = new CountDownTimer(MetaData.HINT_TIMEOUT, MetaData.HINT_TIMEOUT) {       
        @Override
        public void onFinish() {
            mHintRect = null;
            drawScreen(false, null);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

    };
    
    // BEGIN: Better
    private class CacheStatus {
    	private int mPageNo = -1;
    	private boolean mIsDrawing = false;
    	private boolean mIsNeedRefresh = false;
    	private boolean mIsShowOld = false;
    	private long mDirtyTime = 0;//it is not system clock,if this page is called to refresh,dirtyTime may need add 1.if it is clear ,the value is 0
    	private int mIsOnlyUpdateRecognizedShape = 0;//0 not set,1 for update all,2 for only update shape
    	private Object lockObj;
    	private Object mDrawingLockObj;
    	public static final int UPDATE_NOT_CARE = 0;
    	public static final int UPDATE_ALL = 1;
    	public static final int ONLY_UPDATE_SHAPE = 2;
    	public static final int ONLY_UPDATE_SHAPE_TO_MAIN_BITMAP = 3;//for save recognized shap to main bitmap
    	
    	public CacheStatus()
    	{
    		lockObj = new Object();
    		mDrawingLockObj = new Object();
    	}
    	
    	private Object getDrawingLockObj()
    	{
    		return mDrawingLockObj;
    	}
    	
    	public void clearStatus()
    	{
    		synchronized(lockObj)
    		{
    	    	mPageNo = -1;
    	    	mIsDrawing = false;
    	    	mIsNeedRefresh = false;
    	    	mIsShowOld = false;
    	    	mDirtyTime = 0;
    	    	mIsOnlyUpdateRecognizedShape = 0;
    		}
    	}
    	
    	public int getIsOnlyUpdateRecognizedShape()
    	{
    		synchronized(lockObj)
    		{
    			return mIsOnlyUpdateRecognizedShape;
    		}
    	}
    	
    	
    	public long getDirtyTime()
    	{
    		synchronized(lockObj)
    		{
    			return mDirtyTime;
    		}
    	}
    	
    	
    	public void setNeedRefresh(int pageNo,Boolean showOld,int isOnlyUpdateRecognizedShape)
    	{
    		synchronized(lockObj)
    		{
				mIsNeedRefresh = true;
				if(mPageNo != pageNo)
				{
					mPageNo = pageNo;
					mIsShowOld = false;
				}
				if(showOld)
				{
					mIsShowOld = true;
				}
				mDirtyTime++;
				
    			if(mIsOnlyUpdateRecognizedShape == 0)
    			{
    				mIsOnlyUpdateRecognizedShape = isOnlyUpdateRecognizedShape;
    			}
    			else if(mIsOnlyUpdateRecognizedShape != 1)
    			{
    				if(mIsOnlyUpdateRecognizedShape != isOnlyUpdateRecognizedShape)
    				{
    					mIsOnlyUpdateRecognizedShape = 1;//must update all
    				}
    			}
			}
    	}
    	
    	public void updateRefreshStatus(int pageNo, long currentDirtyTime)
    	{
    		synchronized(lockObj)
    		{
				//need check does we still need this page
				if(mPageNo == pageNo && mDirtyTime == currentDirtyTime)
				{
					mIsNeedRefresh = false;
					mDirtyTime = 0;
					mIsOnlyUpdateRecognizedShape = 0;
				}
				
				return;				
    		}
    	}
    	
    	public boolean getIsShowOld()
    	{
    		synchronized(lockObj)
    		{
    			return mIsShowOld;
    		}
    	}
    	
    	public boolean getIsNeedRefresh()
    	{
    		synchronized(lockObj)
    		{
    			if(mPageNo == -1)
    			{
    				return false;
    			}
    			return mIsNeedRefresh;
    		}
    	}
    	
    	public int getPageNo()
    	{
    		synchronized(lockObj)
    		{
    			return mPageNo;
    		}
    	}
    	
    	public void setIsDrawing(Boolean drawingFlag)
    	{
    		synchronized(lockObj)
    		{
    			mIsDrawing = drawingFlag;
    		}
    	}

    }
    private CopyOnWriteArrayList<CacheStatus> mCacheStatusList = new CopyOnWriteArrayList<CacheStatus>();
    
    private void doDrawPage(Canvas c, RectF bounds, List<DrawInfo> list, boolean isUsetempBitmap, float posX, float posY, float offX, float offY)
    {
		if (list != null) {
			int size = list.size();
			try{
				for (int i = 0; i < size; i++) {
					DrawInfo d = list.get(i);
					if (d != null) {
						RectF rc = d.getBounds();
						if (rc != null) {
							if (rc.intersect(bounds)) {
								//BEGIN: RICHARD
								if(d instanceof AnnotationDrawInfo)
								{
									LinkedHashMap<Integer, DrawInfo> drawInfos = ((AnnotationDrawInfo) d).getDrawInfoMap();
									
									TreeSet<Integer> keys = new TreeSet<Integer>(drawInfos.keySet());
									Iterator<Integer> iterator = keys.descendingIterator();
							        
							        while (iterator.hasNext()) {
							            int key = iterator.next();
							            DrawInfo childd = drawInfos.get(key);
										RectF rcChild = childd.getBounds();
										if (rcChild.intersect(bounds)) {
											if(mIsStopPreDrawThread || !mIsNeedDrawDoodleViewContent)
											{
												return;
											}
											childd.getDrawTool().draw(c, isUsetempBitmap, childd, posX, posY, offX, offY);
										}
									}
								}									
								else
								{
									if(mIsStopPreDrawThread ||!mIsNeedDrawDoodleViewContent)
									{
										return;
									}
									d.getDrawTool().draw(c, isUsetempBitmap, d, posX, posY, offX, offY);								
								}
								//END: RICHARD
							}
						}
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return;
			}
		}
    }
    
    private void doDrawPage(Canvas c,RectF bounds,List<DrawInfo> list)
    {
		doDrawPage(c, bounds, list, false, 0, 0, 0, 0);
    }
    
    private void drawRecognizedShape()
    {
    	//just one page
		synchronized(mShapeBitmapLock)
		{
	    	if(mShapeCacheBitmap == null)
	    	{
	    		return;
	    	}
	    	
			mShapeCacheBitmap.eraseColor(Color.TRANSPARENT);

		
	    	if(mDrawInfoForShapeList==null || mDrawInfoForShapeList.size() <= 0)
	    	{
	    		//check again
	    		return;
	    	}
	    	
	        RectF bounds = new RectF();
	        bounds.left = 0;
	        bounds.right = mCacheWidth;
	        bounds.top = 0;
	        bounds.bottom = mCacheHeight;
	    	
	    	ArrayList<DrawInfo> list = mDrawInfoForShapeList.get(0);
	    	doDrawPage(mShapeCacheCanvas, bounds, list);
		}
    }
    
    private void drawRecognizedShapeToMainBitmap()
    {
		synchronized(mShapeBitmapLock)
		{
	    	//just one page
	    	if(mShapeCacheBitmap == null)
	    	{
	    		return;
	    	}
	    	
	    	mScrollCacheCanvas[0].drawBitmap(mShapeCacheBitmap, 0, 0, mBitmapPaint);
	    	mShapeCacheBitmap.eraseColor(Color.TRANSPARENT);
		}
    	
    }
    
    private void drawPage(int index, int page, int offX, int offY) {
    	if ((mScrollCacheBitmap == null) || (mScrollCacheBitmap[index] == null)) {
    		return;
    	}
    	
    	if ((mScrollGraphicDrawListCacheBitmap == null) || (mScrollGraphicDrawListCacheBitmap[index] == null)) {
    		return;
    	}
    	
    	mScrollCacheBitmap[index].eraseColor(Color.TRANSPARENT);
    	mScrollGraphicDrawListCacheBitmap[index].eraseColor(Color.TRANSPARENT);       
    	
        mScrollCacheCanvas[index].save();
        mScrollCacheCanvas[index].translate(0, -offY);
         
        RectF bounds = new RectF();
        bounds.left = 0;
        bounds.right = mCacheWidth;
        bounds.top = offY;
        bounds.bottom = mCacheHeight + offY;

		if (page >= 0) {
			if (page < mPageOrderedDrawList.size()) {
				CopyOnWriteArrayList<DrawInfo> list = mPageOrderedDrawList.get(page);
				doDrawPage(mScrollCacheCanvas[index], bounds, list,true, 0, offY, 0, -page * mCacheHeight);

			}

			
			if(MetaData.IS_ENABLE_CONTINUES_MODE)
			{
				if (mDrawInfoForShapeList != null) {
					if (page < mDrawInfoForShapeList.size()) {
						ArrayList<DrawInfo> list = mDrawInfoForShapeList.get(page);
						doDrawPage(mScrollCacheCanvas[index], bounds, list, true, 0, offY, 0, -page * mCacheHeight);
					}
				}
			}
			else
			{
				//for non continues mode
				if (mDrawInfoForShapeList != null && mDrawInfoForShapeList.size() > 0)
				{
					drawRecognizedShape();
				}
				else
				{
					synchronized(mShapeBitmapLock)
					{
						if(mShapeCacheBitmap != null)
						{
							mShapeCacheBitmap.eraseColor(Color.TRANSPARENT);
						}
					}
				}
			}
				
	    	mScrollGraphicDrawListCacheBitmap[index].eraseColor(Color.TRANSPARENT);
	        if (page >= 0) {
		    	Boolean flag = drawLineLock(mScrollGraphicDrawListCacheCanvas[index], page,false);//darwin
		    	if(!flag)
		    	{
		    		mScrollCacheCanvas[index].restore();
		    		return;
		    	}
	        }
	            
	        mScrollGraphicDrawListCacheCanvas[index].save();
	        mScrollGraphicDrawListCacheCanvas[index].translate(0, -offY);
			if (page < mPageOrderedGraphicDrawList.size()) {
				CopyOnWriteArrayList<DrawInfo> list = mPageOrderedGraphicDrawList.get(page);
				doDrawPage(mScrollGraphicDrawListCacheCanvas[index],bounds,list);

			}
	        mScrollGraphicDrawListCacheCanvas[index].restore();
		}
         
        mScrollCacheCanvas[index].restore();
    }
    
    private boolean mIsPreDrawThreadRunning = false;
    private boolean mIsStopPreDrawThread = false;
	private int mNotifyCount = 0;
	private int getNotifyCount()
	{
		synchronized (mPreDrawThread) {
			int i = mNotifyCount;
			mNotifyCount = 0;
			return i;
		}
	}
	private void incraseNotifyCount()
	{
		synchronized (mPreDrawThread) {
			mNotifyCount++;
			if(mNotifyCount == 1)
			{
				mPreDrawThread.notify();
			}
		}
	}
	private void isNeedWait()
	{
		synchronized (mPreDrawThread) {
			if(mNotifyCount == 0)
			{
				try {
					mPreDrawThread.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	//begin jason
    private boolean released=false;
    //end jason
    Runnable mPreDrawRunnable = new Runnable() {

    	private void do_LoadPage(int page_index)
    	{	
    		if(page_index < 0)
    		{
    			return;
    		}
    		
			int loadCacheIndex = page_index % NUM_CACHE;
			int loadPageNo = page_index;
			int offsetX = 0;
			int offsetY = page_index * mCacheHeight;
								
			CacheStatus status = mCacheStatusList.get(loadCacheIndex);
				
			long currentDirtyTime;
			synchronized(status.getDrawingLockObj())
			{
				status.setIsDrawing(true);
				currentDirtyTime = status.getDirtyTime();
				if (!mIsStopPreDrawThread) {
					if(status.getIsOnlyUpdateRecognizedShape() == CacheStatus.ONLY_UPDATE_SHAPE && !MetaData.IS_ENABLE_CONTINUES_MODE)
					{
						//if not support continues mode
						drawRecognizedShape();
					}else if(status.getIsOnlyUpdateRecognizedShape() == CacheStatus.ONLY_UPDATE_SHAPE_TO_MAIN_BITMAP && !MetaData.IS_ENABLE_CONTINUES_MODE)
					{
						drawRecognizedShapeToMainBitmap();
					}
					else
					{
						drawPage(loadCacheIndex, loadPageNo, offsetX, offsetY);
					}			        
				}	        
		        status.setIsDrawing(false);
			}
			status.updateRefreshStatus(page_index,currentDirtyTime);	
    	}
    	
    	
    	private void callMainthreadToDrawScreen(Handler mainHandler)
    	{
			try
			{									
				mainHandler.post(new Runnable() {
	                    @Override
	                    public void run() {
	                    	//begin jason
	                    	if (released) {
								return;
							}
	                    	//end jason
							drawScreen(true,mDrawInfo);
	                    }
	                });
				Log.d("RICHARD","DRAW SCREEN");
			}catch(Exception e)
			{
				e.printStackTrace();
			}
    	}
    	
    	
		@Override
		public void run() {
			mIsPreDrawThreadRunning = true;
	        boolean isNeedDrawScreen = false;
	        Looper mainLooper = Looper.getMainLooper (); 
	        Handler mainHandler = new Handler(mainLooper);
	        
	        callMainthreadToDrawScreen(mainHandler);//do first time
	        
			while (true) {

				isNeedWait();

				isNeedDrawScreen = false;
				while(getNotifyCount() > 0 && mIsNeedDrawDoodleViewContent)
				{
					if (mIsStopPreDrawThread) {
						break;
					}
							
					int currentUsePage = mCacheIndex1;
					if((mScrollY + mPageEditor.getViewHeight())/mCacheHeight <= mCurrentPageIndex)
					{
						if(mCacheStatusList.get(mCacheIndex1).getIsNeedRefresh())
						{
							do_LoadPage(mCacheStatusList.get(mCacheIndex1).getPageNo());
							isNeedDrawScreen = true;
						}
					}else
					{
						if(mCacheStatusList.get(mCacheIndex1).getIsNeedRefresh())
						{
							do_LoadPage(mCacheStatusList.get(mCacheIndex1).getPageNo());
							isNeedDrawScreen = true;
						}
						if(MetaData.IS_ENABLE_CONTINUES_MODE)
						{
							if(mCacheStatusList.get(mCacheIndex2).getIsNeedRefresh())
							{
								do_LoadPage(mCacheStatusList.get(mCacheIndex2).getPageNo());
								isNeedDrawScreen = true;
							}
						}
					}
					if(isNeedDrawScreen && !isCurrentPageLoading())
					{
						callMainthreadToDrawScreen(mainHandler);
						isNeedDrawScreen = false;
					}
					
					
					if (mIsStopPreDrawThread) {
						break;
					}
				}
				
				if (mIsStopPreDrawThread) {
					break;
				}
				
				//Now we can preload other page
				Boolean isLoadAnotherPage = false;
				for(int i = 0; i< NUM_CACHE && mIsNeedDrawDoodleViewContent ; i++)
				{
					if(mCacheStatusList.get(i).getIsNeedRefresh())
					{
						if(!isLoadAnotherPage)
						{
							do_LoadPage(mCacheStatusList.get(i).getPageNo());
							isLoadAnotherPage = true;
							if(i == mCacheIndex1)
							{
								callMainthreadToDrawScreen(mainHandler);
							}
							else if(MetaData.IS_ENABLE_CONTINUES_MODE)
							{
								if(i == mCacheIndex2 && (mScrollY + mPageEditor.getViewHeight())/mCacheHeight > mCurrentPageIndex)
								{
									callMainthreadToDrawScreen(mainHandler);
								}
							}
						}

						incraseNotifyCount();
						break;

					}
				}
				
			}
			mIsPreDrawThreadRunning = false;
		}
    	
    };
    
    private Thread mPreDrawThread = new Thread(mPreDrawRunnable);
    // END: Better
    
    // BEGIN: Better
    public void init() {
    	if(MetaData.IS_ENABLE_CONTINUES_MODE)
        {
        	NUM_CACHE = 3;
        }
        
        mScrollCacheBitmap = new Bitmap[NUM_CACHE];
        mScrollCacheCanvas = new Canvas[NUM_CACHE];
        mScrollGraphicDrawListCacheBitmap = new Bitmap[NUM_CACHE];
        mScrollGraphicDrawListCacheCanvas = new Canvas[NUM_CACHE];
        
        getHolder().addCallback(this);

        // BEGIN: Better
        mPageOrderedDrawList = new CopyOnWriteArrayList<CopyOnWriteArrayList<DrawInfo>>();
        mPageOrderedGraphicDrawList = new CopyOnWriteArrayList<CopyOnWriteArrayList<DrawInfo>>();
        // END: Better
        mDrawList = new LinkedList<DrawInfo>();
        mObjectList = new LinkedList<DrawInfo>();
        mUndoList = new LinkedList<ActionRecord>();
        mRedoList = new LinkedList<ActionRecord>();
        mBitmapPaint = new Paint();
        mBitmapPaint.setDither(true);
        mBitmapPaint.setAntiAlias(true);
        mHintPaint = new Paint();
        PaintSelector.initPaint(mHintPaint, Color.LTGRAY, HINT_STROKE_WIDTH);

        mLongClickDetector = new LongClickDetector();
        mLongClickDetector.addLongClickListener(this);
        //Begin:Allen
        detector = new GestureDetector(this);
        //End:Allen
        
    	mLastDrawInfoTime.set(0L);//Richard fix bug
    	
    	if (mContext != null) {
    		mPreferences = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
    	}
    }
    // END: Better

    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        //darwin
    	mContext = context;
    	//darwin

        init(); // Better
        
        //textureInit();
    }

	public static Bitmap getPenTexure() {
		if (mTexturePen == null) {
			mTexturePen = BitmapFactory.decodeResource(SuperNoteApplication
					.getInstance().getResources(), R.drawable.pen);
		}
		return mTexturePen;
	}

	public static Bitmap getBrushTexure() {
		if (mTextureBrush == null) {
			mTextureBrush = BitmapFactory.decodeResource(SuperNoteApplication
					.getInstance().getResources(), R.drawable.brush);
			mTextureBrush.setDensity(Bitmap.DENSITY_NONE);//smilefish fix bug 383839 dds load resource error
		}
		return mTextureBrush;
	}

	public static Bitmap getPencilTexure() {
		if (mTexturePencil == null) {
			mTexturePencil = BitmapFactory.decodeResource(SuperNoteApplication
					.getInstance().getResources(), R.drawable.pencil);
		}
		return mTexturePencil;
	}

    public void bind(PageEditorWrapper pageEditor) {
        mPageEditor = pageEditor;
    }

    private void checkClear() {
        final View view = View.inflate(getContext(), R.layout.one_msg_dialog, null);
        final TextView textView = (TextView) view.findViewById(R.id.msg_text_view);
        textView.setText(getContext().getString(R.string.clear_all_alert));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(android.R.string.dialog_alert_title)
                .setView(view)
                .setPositiveButton(getContext().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        releaseListMemory(false);
                        redrawAll(false);
                        setModified(true);//Allen
                        //BEGIN: RICHARD
                        setDoodleItemIsEmpty();
                        //END: RICHARD
                        mPageEditor.setDoodleUndoEmpty(true);
                    }
                })
                .setNegativeButton(getContext().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        builder.show();
    }

	//emmanual to fix bug 519927, 5199291, 519931, 518870
    public void dismissDeleteDialog(){
    	if(mDeleteDialog != null && mDeleteDialog.isShowing()){
    		mDeleteDialog.dismiss();
    	}
    }

    private AlertDialog mDeleteDialog = null;
    private void checkDelete(final Collection<DrawInfo> drawInfos) {
        final View view = View.inflate(getContext(), R.layout.one_msg_dialog, null);
        final TextView textView = (TextView) view.findViewById(R.id.msg_text_view);
        textView.setText(getContext().getString(R.string.delete_alert));

        mDeleteDialog = new AlertDialog.Builder(getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(android.R.string.dialog_alert_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setView(view)
                .setPositiveButton(getContext().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        delete(drawInfos);
                    }
                })
                .setNegativeButton(getContext().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
    }

    public void clearView() {
        checkClear();
    }

    public void copy(NotePage page) { // Better
        if ((mDrawInfo != null) && (mDrawInfo instanceof SelectionDrawInfo)) { 
        	if ((page == null) || (mPreferences == null)) {
        		return;
        	}

        	((SelectionDrawInfo)mDrawInfo).clearSelectionFrame();//Siupo
        	Collection<DrawInfo> selectedInfos = ((SelectionDrawInfo) mDrawInfo).getSelectedObjects();
            int size = selectedInfos.size();
            if ((selectedInfos == null) || (size <= 0)) {
                return;
            }
            
            int doodleWidth = getWidth(), doodleHeight = getHeight();
            Bitmap bgBmp = Bitmap.createBitmap(doodleWidth, doodleHeight, Config.ARGB_8888);
            Canvas bgCanvas = new Canvas(bgBmp);
            bgCanvas.drawColor(Color.TRANSPARENT);//mBackgroundColor//darwin
            
            ContentResolver cr = getContext().getContentResolver();
            ContentProviderClient cpc = cr.acquireContentProviderClient(MetaData.ClipboardTable.uri);
            cr.delete(MetaData.ClipboardTable.uri, null, null);
            ContentValues[] cvs = new ContentValues[size];
            int index = 0;
            for (DrawInfo drawInfo : selectedInfos) {
                ContentValues cv = new ContentValues();
                Serializable s = drawInfo.save(page);
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                try {
                    ObjectOutputStream obj = new ObjectOutputStream(b);
                    obj.writeObject(s);
                    cv.put(MetaData.ClipboardTable.DATA, b.toByteArray());
                    obj.close();
                    b.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                cvs[index++] = cv;
                
                drawInfo.getDrawTool().draw(bgCanvas, false, drawInfo);//Richard null->false
            }
            try {
                cpc.bulkInsert(MetaData.ClipboardTable.uri, cvs);
                cpc.release();
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
            
            SharedPreferences.Editor prefEditor = mPreferences.edit();
            prefEditor.putInt(MetaData.PREFERENCE_EDITOR_COPY_CONTENT_TYPE, MetaData.CLIPBOARD_TYPE_DOODLE);
            if (page != null) {
            	prefEditor.putString(MetaData.PREFERENCE_EDITOR_COPY_PAGE_PATH, page.getFilePath());
            }
            prefEditor.commit();

            RectF rc;
            if(mDrawInfo instanceof SelectionDrawInfo)
            {
            	rc = ((SelectionDrawInfo)mDrawInfo).getBoundsForClipboard();
            }
            else
            {
            	rc = mDrawInfo.getBounds();
            }
            
            float width = rc.width(), height = rc.height();
            //BEGIN: RICHARD
            Bitmap bmp =  null;
            try
            {
            	bmp = Bitmap.createBitmap((int)width, (int)height, Config.ARGB_8888);
            }catch (OutOfMemoryError e) {
                Log.w(TAG, "[OutOfMemoryError]  Create copy bitmap failed !!!");
            	return;
            }
            catch(Exception e)
            {
            	e.printStackTrace();
            	return;
            }
            //END: RICHARD
            Canvas canvas = new Canvas(bmp);
			Rect src = new Rect((int)rc.left, (int)rc.top, (int)Math.ceil(rc.right), (int)Math.ceil(rc.bottom));
            RectF dst = new RectF(0, 0, width, height);
            canvas.drawBitmap(bgBmp, src, dst, mBitmapPaint);
            
            File dir = new File(MetaData.CLIPBOARD_TEMP_DIR);
            if (!dir.exists()) {
            	dir.mkdirs();
            }
            String fileName = System.currentTimeMillis() + ".png";
            File file = new File(dir, fileName);
            if (file.exists()) {
                file.delete();
            }
            boolean isSuccess = false;
            try {
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
	            BufferedOutputStream bos = new BufferedOutputStream(fos);
	            bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
	            bmp.recycle();
	            bmp = null;
	            bos.close();
	            fos.close();
	            isSuccess = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
            if (isSuccess) {
            	if (MetaData.HasMultiClipboardSupport) {
	            	try {
						Intent intent = new Intent();
						intent.setAction("com.asus.provider.clipboard.copyimage");
						intent.putExtra("file", MetaData.CLIPBOARD_TEMP_DIR + fileName);
						intent.putExtra("delete_after_copy", true);
						mContext.sendBroadcast(intent);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	} else {
            		ClipboardManager clipboard = (ClipboardManager) mPageEditor.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            		CharSequence copydata = "SuperNote Doodle Data";
            		ClipData clip = ClipData.newPlainText(MetaData.CLIPBOARD_DOODLE_DESC, copydata);
            		clipboard.setPrimaryClip(clip);
            	}
            }
            
            bgBmp.recycle();
            bgBmp = null;
            
            finishDrafting();
        }
    }

    public void delete() {
        if (mDrawInfo instanceof SelectionDrawInfo) {
            checkDelete(((SelectionDrawInfo) mDrawInfo).getSelectedObjects());
        }
    }

    private void delete(Collection<DrawInfo> drawInfos) {
    	//emmanual
    	if(mDrawInfo == null){
    		return;
    	}
    	
        Matrix gestureMatrix = null;
        Matrix invertMatrix = new Matrix();
		//Begin Darwin_Yu@asus.com
        Collection<DrawInfo> selectdrawInfos = ((SelectionDrawInfo) mDrawInfo).getSelectedObjects();
        for(DrawInfo drawInfo : selectdrawInfos)
        {
	        if(drawInfo instanceof GraphicDrawInfo)
	        {
	        	int iCount = 0;
	        	String name = ((GraphicDrawInfo)drawInfo).getFileName();
	        	for(DrawInfo drawInfoTemp : mDrawList)
	        	{
	        		if(drawInfoTemp instanceof GraphicDrawInfo)
	    	        {
	        			String nameTemp = ((GraphicDrawInfo)drawInfoTemp).getFileName();
	        			if(nameTemp.equalsIgnoreCase(name))
	        			{
	        				iCount++;
	        			}
	        			if(iCount >= 2)//this picture has copied , we can not delete it.
	        			{
	        				break;
	        			}
	    	        }
	        	}
	        	if(iCount < 2)
	        	{
	        		mPageEditor.updateAttachmentRemoveNameList(name);
	        	}
	        }
        }
        //End   Darwin_Yu@asus.com 
        if (mDrawInfo instanceof SelectionDrawInfo) {
            gestureMatrix = ((SelectionDrawInfo) mDrawInfo).getGestureMatrix();
            ((SelectionDrawInfo) mDrawInfo).clearSelection();
        }
        if (drawInfos != null) {
        	// BEGIN: Better
        	for (DrawInfo info : drawInfos) {
        		if (info != null) {
        			deleteFromPageOrderedDrawList(info);
        		}
        	}
        	// END: Better
            mDrawList.removeAll(drawInfos);
            if ((gestureMatrix != null) && !gestureMatrix.isIdentity()) {
                gestureMatrix.invert(invertMatrix);
                gestureMatrix.reset();
            }
            for (DrawInfo drawInfo : drawInfos) {
                if (!invertMatrix.isIdentity()) {
                    drawInfo.transform(invertMatrix);
                }
                delete(drawInfo.getEraseDrawInfos());
            }
            saveAction(ActionRecord.ACTION_DELETE, drawInfos, null);
        }

        selectFinish();
        drawScreen(false, null);

            if ((mDrawList.size() > 0) && (mDrawInfo instanceof SelectionDrawInfo)) {
                selectLast((SelectionDrawInfo) mDrawInfo);
            }
            else {
            	if(mDrawList.size()==0){
            		mPageEditor.setDoodleItemEmpty(true);
            	}
                redrawAll(true);
                mInsertMode = false;//emmanual to fix bug 555754
                mPageEditor.backToPreviousMode();
            }
    }

    // We don't need to add this action to undo list, because it's owner object will handle undo this action
    private void delete(LinkedList<EraseDrawInfo> erasInfos) {
        LinkedList<EraseDrawInfo> removeList = new LinkedList<EraseDrawInfo>();
        if (erasInfos == null) {
            return;
        }
        for (EraseDrawInfo eraseInfo : erasInfos) {
            int index = mDrawList.indexOf(eraseInfo);
            if ((index != -1) && (--((EraseDrawInfo) mDrawList.get(index)).mReferenceCount <= 0)) {
                removeList.add(eraseInfo);
            }
        }
        for (EraseDrawInfo info : removeList) {
        	// BEGIN: Better
        	deleteFromPageOrderedDrawList(info);
        	// END: Better
            mDrawList.remove(info);
        }
    }

    public void drawHint(RectF hint) {
        if (hint != null) {
            mHintRect = hint;
            // TODO: auto focus to the center of the hint
            hint.top -= mScrollY;
            hint.bottom -= mScrollY;
            mHintTimer.start();
            drawScreen(false, null);
        }
    }

    public void drawLine() {
    	// BEGIN: Better
    	drawObjects();
        // END: Better
    }

    private boolean drawLineLock(Canvas canvas, int pageNo,boolean bIsShare) {
        if ((mPageEditor == null) || (canvas == null)) {
            return false;
        }
        return mPageEditor.drawNoteEditTextLine(canvas, pageNo,bIsShare);
    }

    public void drawScreen()//Allen
    {
    	drawScreen(true,  mDrawInfo);
    }
    
    private Boolean isCurrentPageLoading()
    {
		if((mScrollY + mPageEditor.getViewHeight())/mCacheHeight == mCurrentPageIndex)
		{
			if(mCacheStatusList.get(mCacheIndex1).getIsNeedRefresh())
			{
				return true;
			}
		}else
		{
			if(mCacheStatusList.get(mCacheIndex1).getIsNeedRefresh())
			{
				return true;
			}		
			if(MetaData.IS_ENABLE_CONTINUES_MODE){
				if(mCacheStatusList.get(mCacheIndex2).getIsNeedRefresh())
				{
					return true;
				}
			}
		}
		
		return false;
    }
    
    private Boolean isEraseCachePage()
    {
		if((mScrollY + mPageEditor.getViewHeight())/mCacheHeight == mCurrentPageIndex)
		{
			if(!mCacheStatusList.get(mCacheIndex1).getIsShowOld())
			{
				return true;
			}
		}else
		{
			if(!mCacheStatusList.get(mCacheIndex1).getIsShowOld())
			{
				return true;
			}
			if(MetaData.IS_ENABLE_CONTINUES_MODE)
			{
				if(!mCacheStatusList.get(mCacheIndex2).getIsShowOld())
				{
					return true;
				}
			}
		}
		
		return false;
    }
    
    private void drawLastEraserPath(Canvas c,DrawInfo drawInfo,float xoff,float yoff)
    {
        c.save();
        c.translate(xoff, yoff);

        DrawTool drawTool = drawInfo.getDrawTool();
        EraseDrawTool eraseDrawTool = (EraseDrawTool)drawTool;
        eraseDrawTool.doDrawLastPath(c,drawInfo);
        c.restore();

    }

    //emmanual to fix bug 497657, 501683, 497370, 497834, 497744, 501734
	public boolean isRunningForeground() {
		String packageName = mContext.getPackageName();
		String topActivityClassName = getTopActivityName(mContext);
		if (packageName != null && topActivityClassName != null
		        && topActivityClassName.startsWith(packageName)) {
			return true;
		} else {
			return false;
		}
	}

    //emmanual to fix bug 497657, 501683, 497370, 497834, 497744, 501734
	public String getTopActivityName(Context context) {
		String topActivityClassName = null;
		ActivityManager activityManager = (ActivityManager) (context
		        .getSystemService(android.content.Context.ACTIVITY_SERVICE));
		List<RunningTaskInfo> runningTaskInfos = activityManager
		        .getRunningTasks(1);
		if (runningTaskInfos != null) {
			ComponentName f = runningTaskInfos.get(0).topActivity;
			topActivityClassName = f.getClassName();
		}
		return topActivityClassName;
	}
    
    public void drawScreen(boolean drawCurrentPath, DrawInfo drawInfo) {
        boolean isEraser = false;
        if (mSurfaceHolder == null||released||!mIsCacheInited) {// add condition by jason
            return;
        }
        //BEGIN: RICHARD
        synchronized (mSurfaceHolder) {
            if(isCurrentPageLoading() 
            	    //emmanual to fix bug 497657, 501683, 497370, 497834, 497744, 501734
            		&& isRunningForeground())
            {
            	if(!isEraseCachePage())
            	{
            		if(mIsFirstDraw)
            		{
    	            	Canvas c = mSurfaceHolder.lockCanvas(null);
    	            	if(c == null)
    	            	{
    	            		return;
    	            	}
    	            	if (mPageEditor.isDoodleBackroundTransparent()) {
    	            		c.drawColor(mBackgroundColor, PorterDuff.Mode.CLEAR);
    	            	} else {
    	            		c.drawColor(mBackgroundColor);
    	            	}
    	            	mSurfaceHolder.unlockCanvasAndPost(c);
    	            	mIsFirstDraw = false;
            		}
            		mIsNeedRefreshAllPage = true;
            		if(mPageEditor.getInputMode()!=InputManager.INPUT_METHOD_SELECTION_DOODLE){
            			//disable by mars_li
            			mPageEditor.setDrawingDialogStatus(true);
            			//end
            		}
            		return;
            	}
            	else
            	{
	            	Canvas c = mSurfaceHolder.lockCanvas(null);
	            	if(c == null)
	            	{
	            		return;
	            	}
	            	if (mPageEditor.isDoodleBackroundTransparent()) {
	            		c.drawColor(mBackgroundColor, PorterDuff.Mode.CLEAR);
	            	} else {
	            		c.drawColor(mBackgroundColor);
	            	}
	            	
	                if(mScrollY <= mPageEditor.getTemplateLayoutScaleHeight())
	                {
		                c.save();
		                c.translate(-mScrollX, -mScrollY);
		    			c.scale(mPageEditor.getScaleX(), mPageEditor.getScaleY());//RICHARD
		                mPageEditor.drawTemplateView(c);
		                c.restore(); 
	                }
	            	
	                mPageEditor.drawNoteEditText(c,mScrollX,mScrollY);//Allen
	            	mSurfaceHolder.unlockCanvasAndPost(c);
	            	
	            	mIsNeedRefreshAllPage = true;
	            	mPageEditor.setDrawingDialogStatus(true);
	            	return;
            	}
            }
        }
        //END: RICHARD

        if (drawInfo instanceof EraseDrawInfo) {
            isEraser = true;
            
            // BEGIN: Better
            int scrollY = mScrollY % mCacheHeight;
            
            synchronized(mCacheStatusList.get(mCacheIndex1).getDrawingLockObj())
            {
            	drawLastEraserPath(mScrollCacheCanvas[mCacheIndex1],drawInfo,mScrollX,scrollY);
            }

            if(MetaData.IS_ENABLE_CONTINUES_MODE)
            {
	            if((mScrollY + mPageEditor.getViewHeight())/mCacheHeight > mCurrentPageIndex)
	            {
	                synchronized(mCacheStatusList.get(mCacheIndex2).getDrawingLockObj())
	                {
	                	drawLastEraserPath(mScrollCacheCanvas[mCacheIndex2],drawInfo,mScrollX,scrollY- mCacheHeight);
	                }
	            }    
            }
            // END: Better
        }

        synchronized (mSurfaceHolder) {
            Canvas c = mSurfaceHolder.lockCanvas((drawInfo == null ||mIsNeedRefreshAllPage) ? null : drawInfo.getDirty());
            if (c != null) {
            	if(mIsNeedRefreshAllPage)
            	{
            		mPageEditor.setDrawingDialogStatus(false);
            	}
            	mIsNeedRefreshAllPage = false;
            	if (mPageEditor.isDoodleBackroundTransparent()) {
            		c.drawColor(mBackgroundColor, PorterDuff.Mode.CLEAR);
            	} else {
            		c.drawColor(mBackgroundColor);
            	}
                if(mScrollY <= mPageEditor.getTemplateLayoutScaleHeight())
                {
	                c.save();
	                c.translate(-mScrollX, -mScrollY);
	                c.scale(mPageEditor.getScaleX(), mPageEditor.getScaleY());//RICHARD
	                mPageEditor.drawTemplateView(c);//Allen
	                c.restore(); // Better
                }
                
                // BEGIN: Better
                {
                	int drawHeight = mCacheHeight;
                	if (drawHeight < c.getHeight()) {
                		drawHeight = c.getHeight();
                	}
	                c.save();
	                c.translate(-mScrollX, 0);
	                int offset = mScrollY % mCacheHeight;
	                Rect src = new Rect();
	            	src.left = 0;
	            	src.right = mCacheWidth;
	            	src.top = offset;
	            	src.bottom = mCacheHeight ;
	            	Rect dst = new Rect();
	            	dst.left = 0;
	            	dst.right = mCacheWidth;
	            	dst.top = 0;
	            	dst.bottom = mCacheHeight - offset ;
	                c.drawBitmap(mScrollGraphicDrawListCacheBitmap[mCacheIndex1], src, dst, mBitmapPaint);
	                c.drawBitmap(mScrollCacheBitmap[mCacheIndex1], src, dst, mBitmapPaint);
	                
	        		synchronized(mShapeBitmapLock)
	        		{
	        			if(mShapeCacheBitmap != null)
	        			{
			                if(mIsNeedShape && isNeedRefreshShapeBitmap() &&!MetaData.IS_ENABLE_CONTINUES_MODE)
			                {
			                	c.drawBitmap(mShapeCacheBitmap, src, dst, mBitmapPaint);
			                }
	        			}
	        		}
	
	                if(MetaData.IS_ENABLE_CONTINUES_MODE)
	                {
		                if((mScrollY + mPageEditor.getViewHeight())/mCacheHeight > mCurrentPageIndex)
		                {
		                	src.left = 0;
		                	src.right = mCacheWidth;
		                	src.top = 0;
		                	src.bottom = drawHeight - (mCacheHeight - offset) ;
		                	dst.left = 0;
		                	dst.right = mCacheWidth;
		                	dst.top = mCacheHeight - offset;
		                	dst.bottom = drawHeight ;
		                	c.drawBitmap(mScrollGraphicDrawListCacheBitmap[mCacheIndex2], src, dst, mBitmapPaint);
		                	c.drawBitmap(mScrollCacheBitmap[mCacheIndex2], src, dst, mBitmapPaint);
		
		                }
	                }
	                c.restore();
                }
//                 END: Better  

                if (!isEraser && (drawInfo != null) && drawCurrentPath) {
                	if(drawInfo instanceof BallPenDrawInfo){
                		//Carrot: draw special cache bitmap to screen
                		synchronized(mCarrotCacheBitmapLock){
                			//Carrot@20130910: reducing the draw area
	    	            	Rect dirty = drawInfo.getDirty();
	    	            	if(dirty != null)
	    	            		c.drawBitmap(mCarrotCacheBitmap, dirty, dirty, mBitmapPaint);
                		}
                	}else{
                		if(drawInfo instanceof GraphicDrawInfo)
	                	{
	                		drawInfo.getDrawTool().draw(c, false, drawInfo);//Richard null -> false
	                		drawInfo.resetDirty();
	                	}
	                	else{
	                		drawInfo.getDrawTool().draw(c, false, drawInfo);//Richard null -> false
	                		drawInfo.resetDirty();
	                	}
                	}
                	
                	RectF rc = drawInfo.getBounds();
                	if (rc != null) {
                		int num = (int) Math.ceil((rc.bottom + mScrollY) / mCacheHeight);
                		if (num > mPageEditor.getPageNum()) {
                			mPageEditor.setPageNum(num);
                		}
                	}
                }

                if (mHintRect != null) {
                    c.drawRect(mHintRect, mHintPaint);
                }
                mPageEditor.drawNoteEditText(c,mScrollX,mScrollY);//Allen
                if (c.getSaveCount()>0) {//add by jason
					c.restoreToCount(c.getSaveCount());
				}
                if(mSurfaceHolder != null)
                mSurfaceHolder.unlockCanvasAndPost(c);
            }
        }
//        Log.v("TEST_PERFORMEMCE", "ARGB_8888 cost:"+(System.nanoTime()-time)/1000000.0+"ms");
    }

    public void enable(boolean enable) {
        mIsEnable = enable;
    }

    // Add objects to mDrawList and draw them to cache bitmap
    private void finishDrafting() {
        if (DEBUG) {
            Log.d(TAG, "finishDrafting(), mDrawInfo = " + mDrawInfo);
        }
        // Finish selecting
        if (mDrawInfo instanceof SelectionDrawInfo) {
            selectFinish();
            return;
        }

        if(!((EditorActivity)mContext).isLongPressEraserState()){
            mDrawInfo = null;
        }
    }

    public Rect getBound(int targetWidth, int targetHeight) {
        float scaleX = (float) targetWidth / (float) mCacheWidth;
        float scaleY = (float) targetHeight / (float) mCacheHeight;
        LinkedList<DrawInfo> objects = getObjects();
        Matrix matrix = new Matrix();
        Rect resultBounds;
        RectF unionBounds = new RectF();

        matrix.setScale(scaleX, scaleY);
        for (DrawInfo drawInfo : objects) {
            RectF bounds = drawInfo.getBounds();
            matrix.mapRect(bounds);
            unionBounds.union(bounds);
        }

        resultBounds = new Rect(0, 0, (int) unionBounds.right, (int) unionBounds.bottom);
        return resultBounds;
    }

    private LinkedList<DrawInfo> getObjects() {
    	saveRecognizerShape();//RICHARD //do this before save
        LinkedList<DrawInfo> objects = new LinkedList<DrawInfo>();
        for (DrawInfo drawInfo : mDrawList) {
            if (!(drawInfo instanceof EraseDrawInfo)) {
                objects.add(drawInfo);
            }
        }

        if (mDrawInfo instanceof SelectionDrawInfo) {
            LinkedHashMap<Integer, DrawInfo> selections = ((SelectionDrawInfo) mDrawInfo).getSelections();
            Set<Integer> keySet = selections.keySet();
            TreeSet<Integer> sortedSet = new TreeSet<Integer>(keySet);
            Matrix tranMatrix = new Matrix();
            tranMatrix.postTranslate(mScrollX, mScrollY);
            for (Integer i : sortedSet) {
                DrawInfo info = selections.get(i).clone();
                info.transform(tranMatrix);
                if (i >= objects.size()) {
                    objects.addFirst(info);
                }
                else {
                    objects.add(i, info);
                }
            }
        }

        return objects;
    }
    
    // BEGIN: Better
    private void drawAllPage(Canvas canvas) {
		for (int page = 0; page < mPageEditor.getPageNum(); page++) {
			drawPage(canvas, page, page * mCacheHeight, 0);
		}
    }
    
    private void drawPage(Canvas canvas, int page, float offY, float transY) {
        canvas.save();
        canvas.translate(0, transY);
         
        RectF bounds = new RectF();
        bounds.left = 0;
        bounds.right = mCacheWidth;
        bounds.top = page * mCacheHeight;
        bounds.bottom = mCacheHeight + page * mCacheHeight;

		if (page >= 0) {
			if (page < mPageOrderedGraphicDrawList.size()) {
				CopyOnWriteArrayList<DrawInfo> list = mPageOrderedGraphicDrawList.get(page);
				doDrawPage(canvas, bounds, list);
			}

			if (page < mPageOrderedDrawList.size()) {
				CopyOnWriteArrayList<DrawInfo> list = mPageOrderedDrawList.get(page);
				doDrawPage(canvas, bounds, list, true, 0, offY, 0, -page * mCacheHeight);//RICHARD
			}
			
			if (mDrawInfo != null) {
				RectF rc = mDrawInfo.getBounds();
				if ((rc != null) && rc.intersect(bounds)) {
					if (mDrawInfo instanceof SelectionDrawInfo) {
			            LinkedHashMap<Integer, DrawInfo> selections = ((SelectionDrawInfo) mDrawInfo).getSelections();
			            Set<Integer> keySet = selections.keySet();
			            TreeSet<Integer> sortedSet = new TreeSet<Integer>(keySet);
			            Matrix tranMatrix = new Matrix();
			            tranMatrix.postTranslate(mScrollX, mScrollY);
			            for (Integer i : sortedSet) {
			                DrawInfo info = selections.get(i).clone();
			                info.transform(tranMatrix);
			                if(info instanceof AnnotationDrawInfo) {
								for(DrawInfo childd : ((AnnotationDrawInfo) info).getDrawInfos()) {
									RectF rcChild = childd.getBounds();
									if (rcChild.intersect(bounds)) {
										childd.getDrawTool().draw(canvas, true, childd, 0, offY, 0, -page * mCacheHeight);//RICHARD
									}
								}
							} else {
								info.getDrawTool().draw(canvas, true, info, 0, offY, 0, -page * mCacheHeight);	//RICHARD							
							}
			            }
			        } else if (mDrawInfo instanceof TransformDrawInfo) {
			            if (mDrawInfo != mDrawList.peekFirst()) {
			            	if(mDrawInfo instanceof AnnotationDrawInfo) {
								for(DrawInfo childd : ((AnnotationDrawInfo) mDrawInfo).getDrawInfos()) {
									RectF rcChild = childd.getBounds();
									if (rcChild.intersect(bounds)) {
										childd.getDrawTool().draw(canvas, true, childd, 0, offY, 0, -page * mCacheHeight);//RICHARD
									}
								}
							} else {
								mDrawInfo.getDrawTool().draw(canvas, true, mDrawInfo, 0, offY, 0, -page * mCacheHeight);	//RICHARD							
							}
			            }
			        }
				}
			}

			if (mDrawInfoForShapeList != null) {
				if (page < mDrawInfoForShapeList.size()) {
					ArrayList<DrawInfo> list = mDrawInfoForShapeList.get(page);
					doDrawPage(canvas, bounds, list);
				}
			}
		}
         
		canvas.restore();
    }
    // END: Better

    //Begin Allen
    public void getCacheResult(Canvas c,int width,int height,boolean isFromShare){//darwin
    	if(!isCurrentPageLoading()){
    		float scaleX = width/(float) mCacheWidth;
    		float scaleY = height/(float) mCacheHeight;
    		if(isFromShare)
    		{
    			c.drawColor(mBackgroundColor);//add for darwin
    		}
    		c.save();
    		c.scale(scaleX, scaleY);
    		try{
    		c.drawBitmap(mScrollGraphicDrawListCacheBitmap[mCacheIndex1], 0,0, mBitmapPaint);
            c.drawBitmap(mScrollCacheBitmap[mCacheIndex1], 0, 0, mBitmapPaint);
    		}catch(Exception ex){
    			
    		}
    		synchronized(mShapeBitmapLock)
    		{
    			if(mShapeCacheBitmap != null)
    			{
					c.drawBitmap(mShapeCacheBitmap, 0, 0, mBitmapPaint);//RICHARD ADD for shape
    			}
    		}
            c.restore();
    	}
    }
    //End Allen
    
    //Begin Emmanual
    public void getCacheResultForThumbnail(Canvas c,int width,int height,boolean isFromShare){//darwin
    	if(!isCurrentPageLoading()){
    		float scaleX = width/(float) mCacheWidth;
    		float scaleY = height/(float) mCacheHeight;
    		if(isFromShare)
    		{
    			c.drawColor(mBackgroundColor);//add for darwin
    		}
    		c.save();
    		c.scale(scaleX, scaleY);    		

        	Bitmap thumbDoodleBitmap = Bitmap.createBitmap(mCacheWidth, mCacheHeight, Bitmap.Config.ARGB_8888);     //ARGB8888 --> ARGB4444. Modified by Dave. To avoid memory leak from creatBitmap();
        	Canvas thumbDoodleCanvas = new Canvas(thumbDoodleBitmap);
            RectF bounds = new RectF();
            bounds.left = 0;
            bounds.right = mCacheWidth;
            bounds.top = 0;
            bounds.bottom = mCacheHeight;	            
            int page = mCacheStatusList.get(mCacheIndex1).getPageNo();
			if (page < mPageOrderedGraphicDrawList.size()) {
				CopyOnWriteArrayList<DrawInfo> list = mPageOrderedGraphicDrawList.get(page);
				doDrawPage(thumbDoodleCanvas,bounds,list);		
			}    		
    		c.drawBitmap(thumbDoodleBitmap, 0,0, mBitmapPaint);
			if (thumbDoodleBitmap != null && !thumbDoodleBitmap.isRecycled()) {
				thumbDoodleBitmap.recycle();
				thumbDoodleBitmap = null;
			}
    		
            c.drawBitmap(mScrollCacheBitmap[mCacheIndex1], 0, 0, mBitmapPaint);
    		synchronized(mShapeBitmapLock)
    		{
    			if(mShapeCacheBitmap != null)
    			{
					c.drawBitmap(mShapeCacheBitmap, 0, 0, mBitmapPaint);//RICHARD ADD for shape
    			}
    		}
            c.restore();
    	}
    }
    //End Emmanual
    
    //darwin
    public void getResult(Canvas canvas, int targetWidth, int targetHeight, boolean drawBackground, boolean hiddenLine, int templateDoodlePaddingTop, int pageNo,boolean bIsShare) {//darwin
        float scaleX = (float) targetWidth / (float) mCacheWidth;
        // BEGIN: Better
        float scaleY = (float) targetHeight / (float) ((pageNo >= 0) ? (mCacheHeight) : mCacheHeight * mPageEditor.getPageNum());// - templateDoodlePaddingTop * mPageEditor.getNoteEditTextScaleY()
        // END: Better
        canvas.save();
        canvas.scale(scaleX, scaleY);

        if (drawBackground) {
            canvas.drawColor(mBackgroundColor);
        }

//        if (!hiddenLine) {
//            drawLineLock(canvas);
//        }
        
		if (!hiddenLine) {
			if (pageNo >= 0) {
				drawLineLock(canvas, pageNo,bIsShare);//darwin
			} else {
				drawLineLock(canvas, 0,bIsShare);//darwin
			}
		}
        
        // BEGIN: Better
        if (pageNo >= 0) {
        	drawPage(canvas, pageNo, pageNo * mCacheHeight, -pageNo * mCacheHeight);
        } else {
        	drawAllPage(canvas);
        }
        // END: Better
        
        canvas.restore();
    }
    public void getLineLock(Canvas canvas, int targetWidth, int targetHeight, boolean drawBackground, boolean hiddenLine, int templateDoodlePaddingTop, int pageNo) {//darwin
        float scaleX = (float) targetWidth / (float) mCacheWidth;
        // BEGIN: Better
        float scaleY = (float) targetHeight / (float) ((pageNo >= 0) ? (mCacheHeight) : mCacheHeight * mPageEditor.getPageNum());// - templateDoodlePaddingTop * mPageEditor.getNoteEditTextScaleY()
        // END: Better

        canvas.save();
        canvas.scale(scaleX, scaleY);

        if (!hiddenLine) {
        	if (pageNo >= 0) {
        		drawLineLock(canvas, pageNo,true);//darwin
        	} else {
        		drawLineLock(canvas, 0,true);//darwin
        	}
        }

        canvas.restore();
    }
    //darwin
    
    public void getResult(Canvas canvas, int targetWidth, int targetHeight, boolean drawBackground, boolean hiddenLine,int thumbnailHeight) {//darwin
        float scaleX = (float) targetWidth / (float) mCacheWidth;
        float scaleY = (float) targetHeight / (float) (thumbnailHeight*mPageEditor.getScaleY());//Allen

        canvas.save();
        canvas.scale(scaleX, scaleY);

        if (drawBackground) {
            canvas.drawColor(mBackgroundColor);
        }

        if (!hiddenLine) {
            drawLineLock(canvas, 0,false);//darwin
        }

        drawPage(canvas, 0, 0, 0);
        canvas.restore();
    }

    public LinkedList<String> getUsingFiles() {
        LinkedList<DrawInfo> objects = getObjects();
        LinkedList<String> usingFiles = new LinkedList<String>();
        for (DrawInfo drawInfo : objects) {
            if (drawInfo instanceof AnnotationDrawInfo) {
                usingFiles.addAll(((AnnotationDrawInfo) drawInfo).getUsingFiles());
            }
            else if (drawInfo instanceof GraphicDrawInfo) {
                String fileName = ((GraphicDrawInfo) drawInfo).getFileName();
                if (fileName != null) {
                    usingFiles.add(fileName);
                }
            }
            //BEGIN: Show
            else if (drawInfo instanceof TextImgDrawInfo) {
                String fileName = ((TextImgDrawInfo) drawInfo).getFileName();
                if (fileName != null) {
                    usingFiles.add(fileName); 
                }
            }
            //END: Show
        }
        return usingFiles;
    }

    public boolean group() {
        DrawInfo group = null;
        if (mDrawInfo instanceof SelectionDrawInfo) {
            saveAction(ActionRecord.ACTION_GROUP, ((SelectionDrawInfo) mDrawInfo).getSelectedObjects(), null);
            group = ((SelectionDrawInfo) mDrawInfo).group();
            drawScreen(true, mDrawInfo);
            mPageEditor.setDoodleItemSelect(true, false, true);
        }
        return group != null;
    }

    public boolean haveObjects() {
        return mDrawList.size() > 0;
    }

    //BEGIN: RICHARD TEST
    public void reInitCache(int width,int height)
    {
    	Matrix matrix = new Matrix();
        if (mCacheWidth != 0) {
            mScaleX = (float) width / (float) mCacheWidth;
        }
        if (mCacheHeight != 0) {
            mScaleY = (float) height / (float) mCacheHeight;
        }
        if ((mScaleX != 1) || (mScaleY != 1)) {
            matrix.postScale(mScaleX, mScaleY);
            for (DrawInfo drawInfo : mDrawList) {
                drawInfo.transform(matrix);
            }
        }
        
        mIsCacheInited = false; //smilefish
    	initCanvasSize(width,height);
    	
    	//emmanual to fix bug 401865
    	try{
    		initCache();
    	}catch(OutOfMemoryError ex){
    		
    	}
    	
    	redrawAll(true);
    }
    //END: RICHARD
    //Begin:Dave. To avoid memory leak by creatBitmap();
    private static Bitmap mFreeBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
    //End:Dave.
    
    private void initCache() {
    	if(mIsCacheInited) //smilefish
    		return;

        // BEGIN: Better
        if (mIsPreDrawThreadRunning) {
        	mIsStopPreDrawThread = true;
        	synchronized (mPreDrawThread) {
        		mPreDrawThread.notify();
        	}
        	
        	while (mIsPreDrawThreadRunning) {
        		try {
					Thread.sleep(100);
					//begin smilefish fix bug 394791,393283 thread locked ANR
					synchronized (mPreDrawThread) {
		        		mPreDrawThread.notify();
		        	}
					//end smilefish
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        	mIsStopPreDrawThread = false;
        }
        
        mCacheStatusList.clear();
        for (int i = 0; i < NUM_CACHE; i++) {
        	if ((mScrollCacheBitmap[i] != null) && (!mScrollCacheBitmap[i].isRecycled())) {
        		if(mScrollCacheCanvas[i] != null)
        			mScrollCacheCanvas[i].setBitmap(mFreeBitmap);
        		mScrollCacheBitmap[i].recycle();
        		mScrollCacheBitmap[i] = null;
        	}
        	mScrollCacheBitmap[i] = Bitmap.createBitmap(mCacheWidth, mCacheHeight, Bitmap.Config.ARGB_8888);
        	mScrollCacheCanvas[i] = new Canvas(mScrollCacheBitmap[i]);
        	
        	if ((mScrollGraphicDrawListCacheBitmap[i] != null) && (!mScrollGraphicDrawListCacheBitmap[i].isRecycled())) {
        		if(mScrollGraphicDrawListCacheCanvas[i] != null)
        			mScrollGraphicDrawListCacheCanvas[i].setBitmap(mFreeBitmap);
        		mScrollGraphicDrawListCacheBitmap[i].recycle();
        		mScrollGraphicDrawListCacheBitmap[i] = null;
        	}
        	mScrollGraphicDrawListCacheBitmap[i] = Bitmap.createBitmap(mCacheWidth, mCacheHeight, Bitmap.Config.ARGB_8888);     //ARGB8888 --> ARGB4444. Modified by Dave. To avoid memory leak from creatBitmap();
        	mScrollGraphicDrawListCacheCanvas[i] = new Canvas(mScrollGraphicDrawListCacheBitmap[i]);
        	
        	CacheStatus status = new CacheStatus();
        	mCacheStatusList.add(status);
        	
        	//carrot: cache init
        	if((mCarrotCacheBitmap != null)&& !mCarrotCacheBitmap.isRecycled()){
        		if(mCarrotCacheCanvas != null)
        			mCarrotCacheCanvas.setBitmap(mFreeBitmap);
        		
        		mCarrotCacheBitmap.recycle();
        		mCarrotCacheBitmap = null; 
        	}
	        //emmanual to add try-catch to avoid OOM
	        try{
	        	mCarrotCacheBitmap = Bitmap.createBitmap(mCacheWidth, mCacheHeight, Bitmap.Config.ARGB_4444);  //ARGB8888 --> ARGB4444. By Emmanual to avoid OOM;
	        	mCarrotCacheCanvas = new Canvas(mCarrotCacheBitmap);
			} catch (OutOfMemoryError error) {
				
			}
        }
        
        if(!MetaData.IS_ENABLE_CONTINUES_MODE)
        {
	        if((mShapeCacheBitmap!= null) && !mShapeCacheBitmap.isRecycled())
	        {
	        	if(mShapeCacheCanvas != null)
	        		mShapeCacheCanvas.setBitmap(mFreeBitmap);
	        	
	        	mShapeCacheBitmap.recycle();
	        	mShapeCacheBitmap = null;
	        }
	        //emmanual to add try-catch to avoid OOM
	        try{
		        mShapeCacheBitmap = Bitmap.createBitmap(mCacheWidth, mCacheHeight, Bitmap.Config.ARGB_4444);  //ARGB8888 --> ARGB4444. Modified by Dave. To avoid memory leak from creatBitmap();
		        mShapeCacheCanvas = new Canvas(mShapeCacheBitmap);
			} catch (OutOfMemoryError error) {
				
			}
        }
        
        State state = mPreDrawThread.getState();
        if (state == State.NEW) {
        	mPreDrawThread.start(); 
        } else {
        	mPreDrawThread = new Thread(mPreDrawRunnable);
        	mPreDrawThread.start();
        }
        // END: Better    	
    	
        if(mShapeList != null)//darwin
        {
        	mShapeList.clear();
        }
        //END: Richard
        
        // BEGIN: Better
        int height = mCacheHeight;
        if (mPageEditor != null) {
        	height = mCacheHeight * mPageEditor.getPageNum();
        }
        mTransBounds = new RectF(0, 0, mCacheWidth, height);
        if (MetaData.IS_ENABLE_CONTINUES_MODE) {
        	if (mTransBounds.bottom < mScrollY + getHeight()) {
            	mTransBounds.bottom = mScrollY + getHeight();
            }
        }
        // END: Better
        
        mIsCacheInited = true; //smilefish
    }

    public void initCanvasSize(int width, int height) {
        if (DEBUG) {
            Log.d(TAG, "initCanvasSize(), [width, height] = " + "[" + width + ", " + height + "]");
        }
        mCacheWidth = width;
        mCacheHeight = height;
    }
    
    /**
     * 鎻掑叆鍥剧墖銆傚瀭鐩存柟鍚戦粯璁ゅ眳涓�
     * @param bitmap
     * @param fileName
     * @return
     */
    public boolean insertGraphic(Bitmap bitmap, String fileName){
    	float dy = computerGraphicHCenter(bitmap);
    	return insertGraphic(bitmap, fileName, dy);
    }

    /**
     * 鎻掑叆鍥剧墖
     * @param bitmap
     * @param fileName
     * @return
     */
    public boolean insertGraphic(Bitmap bitmap, String fileName, float dy) {
        if (DEBUG_INSERT) {
            Log.d(TAG, "insertGraphic");
        }
        if (bitmap == null) {
            return false;
        }
        if (isOutOfMemory()) {
            Runtime runtime = Runtime.getRuntime();
            long needSpace = (runtime.totalMemory() - runtime.freeMemory()) - MEMORY_USAGE_LIMIT;
            int freeSpace = releaseMemory(needSpace);
            if (freeSpace < needSpace) {
                bitmap.recycle();
                bitmap = null;
                showAlert(R.string.no_memory_dialog_content);
                return false;
            }
        }
        mIsFirstDraw = true; //smilefish. fix bug 309924
        finishDrafting();
        prepareGraphic(bitmap, fileName, dy);

        if (mSurfaceHolder != null) {
            redrawAll(true);
        }
        setModified(true);//Allen
        mPageEditor.setDoodleCropButtonsEnable(true);		//By Show
        mInsertMode = true;
        return true;
    }
    //begin jason
    public void insertShapeGraphic(int type,Path p,String fileName){
    	finishDrafting();
    	 if (mSurfaceHolder != null) {
             redrawAll(true);
         }
    	 finishDrafting();
    	 prepareShapeGraphic(type, p, fileName);
    	 setModified(true);
         mPageEditor.setDoodleCropButtonsEnable(false);		
         mInsertMode = true;
    }
    //end jason
    //BEGIN:Show
    public boolean insertTextImg(Bitmap bitmap, Editable content, String fileName) {
        if (DEBUG_INSERT) {
            Log.d(TAG, "insertTextImg");
        }
        if (bitmap == null) {
            return false;
        }
        if (isOutOfMemory()) {
            Runtime runtime = Runtime.getRuntime();
            long needSpace = (runtime.totalMemory() - runtime.freeMemory()) - MEMORY_USAGE_LIMIT;
            int freeSpace = releaseMemory(needSpace);
            if (freeSpace < needSpace) {
                bitmap.recycle();
                bitmap = null;
                showAlert(R.string.no_memory_dialog_content);
                return false;
            }
        }
        finishDrafting();
        prepareTextImg(bitmap, content, fileName);

        if (mSurfaceHolder != null) {
            redrawAll(true);
        }
        setModified(true);//Allen
        mPageEditor.setDoodleTextEditButtonsEnable(true);
        mInsertMode = true;
        return true;
    }
    
    private void prepareTextImg(Bitmap bitmap, Editable content, String fileName) {
        if (DEBUG_INSERT) {
            Log.d(TAG, "prepareTextImg");
        }

        SelectionDrawInfo selectDrawInfo = (SelectionDrawInfo) (new SelectionDrawTool(getContext())).getDrawInfo(mCurrentPaint);
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), selectDrawInfo);
        TextImgDrawInfo textImgDrawInfo = (TextImgDrawInfo) (new TextImgDrawTool(bitmap, content)).getDrawInfo(mBitmapPaint);
        Matrix matrix = new Matrix();
        RectF bounds = textImgDrawInfo.getBounds();
        String directory = mPageEditor.getFilePath();
        
        if(fileName == null)
        {
        	fileName = bitmap.toString();
        	FileOutputStream fos;
            try {
                fos = new FileOutputStream(new File(directory, fileName));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        File file = new File(directory, fileName);
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), option);
        textImgDrawInfo.setWidth((int)option.outWidth);
        textImgDrawInfo.setHeight((int)option.outHeight);
        
        textImgDrawInfo.setFileName(fileName);
        
        mPageEditor.setIsAttachmentModified(true);
        mPageEditor.addToAttachmentNameList(fileName);
        
        if ((mLongClickX == -1) && (mLongClickY == -1)) {
        	//Modified by show
        	int scroolY = 0;
        	if (mPageEditor.getTemplateType()==MetaData.Template_type_todo) {
        		scroolY = mPageEditor.getTemplateLinearLayoutScrollY();
        	}else {	
        		scroolY = mPageEditor.getTemplateLayoutScaleHeight() - mPageEditor.getTemplateLinearLayoutScrollY();
        	}
        	if (scroolY < 0 ) {
        		matrix.postTranslate((mViewWidth - bounds.width()) / 2, (mViewHeight - bounds.height()) / 2);
        	}
        	else {
        		matrix.postTranslate((mViewWidth - bounds.width()) / 2, (mViewHeight + scroolY - bounds.height()) / 2);
        	}
        }
        else {
            matrix.postTranslate(mLongClickX, mLongClickY);
            resetLongClick();
        }
        selectDrawInfo.addSelection(0, textImgDrawInfo);
        selectDrawInfo.transform(matrix);
        mCurrentDrawTool = selectDrawInfo.getDrawTool();
        mDrawInfo = selectDrawInfo;
        mObjectList.addAll(selectDrawInfo.getSelectedObjects());
        saveAction(ActionRecord.ACTION_ADD, selectDrawInfo.getSelectedObjects(), null);
    }
    //END: Show
    public boolean isModified() {
        return mIsModified;
    }
	
	// BEGIN: Wendy
    public void setModified(boolean value)
    {
    	mIsModified = value;
    	mPageEditor.onModified(value);//Allen
    }
	// END: Wendy

    public void load(DoodleItem item, Bitmap background) {
        if (DEBUG_SAVE) {
            Log.d(TAG, "load(bmp)");
        }

        finishDrafting();

        if ((mCacheWidth == 0) || (mCacheHeight == 0)) {
            throw new RuntimeException(INIT_SIZE_MESSAGE);
        }

        releaseListMemory(true);

        if (item != null) {
            Matrix matrix = new Matrix();
            int oriWidth = item.getCanvasWidth();
            int oriHeight = item.getCanvasHeight();

            mDrawList = item.load(mPageEditor.getFilePath());
            mObjectList.addAll(mDrawList);
            if (oriWidth != 0) {
                mScaleX = (float) mCacheWidth / (float) oriWidth;
            }
            if (oriHeight != 0) {
                mScaleY = (float) mCacheHeight / (float) oriHeight;
            }
            if ((mScaleX != 1) || (mScaleY != 1)) {
                matrix.postScale(mScaleX, mScaleY);
                for (DrawInfo drawInfo : mDrawList) {
                    drawInfo.transform(matrix);
                }
            }
            mIsModified = false;//wendy allen++
        }
        else {//this page have not doodle item (it's a new page) 
            mPageEditor.setDoodleModified();
        }

        if (mDrawList.size() > 0) {
            mPageEditor.setDoodleItemEmpty(false);
            
            // BEGIN: Better
            int pageNum = mPageEditor.getPageNum();
            for (int i = mDrawList.size() - 1; i >= 0; i--) {
            	DrawInfo drawInfo = mDrawList.get(i);
            	if (drawInfo != null) {
            		int num = addToPageOrderedDrawList(drawInfo);
            		if (num > pageNum) {
            			pageNum = num;
            		}
            	}
            }
            mPageEditor.setPageNum(pageNum);
            // END: Better
        }
        else {
            mPageEditor.setDoodleItemEmpty(true);
        }

        mPageEditor.setDoodleUndoEmpty(true);
        mPageEditor.setDoodleRedoEmpty(true);//by show

        if (DEBUG_SAVE) {
            Log.d(TAG, "Objects size after loaded: " + mDrawList.size());
        }

    }

    public void load(DoodleItem item, int backgroundColor) {
        if (DEBUG_SAVE) {
            Log.d(TAG, "load(color): Stored backgroundColor " + backgroundColor);
        }
        mBackgroundColor = backgroundColor;
        mIsFirstDraw = true;
        //BEGIN : RICHARD
        for(int i = 0 ; i < NUM_CACHE; i++)
        {
        	mCacheStatusList.get(i).clearStatus();
        }
		//END: RICHARD
        
        load(item, null); 
    }
    // begin jason
    public void mergeDoodleItem(DoodleItem item,String path,NotePage notePage){
    	LinkedList<DrawInfo> drawInfos=null;
    	if (item != null) {
            Matrix matrix = new Matrix();
            int oriWidth = item.getCanvasWidth();
            int oriHeight = item.getCanvasHeight();
            float scalex =1.0f;
            float scaley=1.0f;
            drawInfos=item.load(path);
            if (oriWidth != 0) {
            	scalex = (float) mCacheWidth / (float) oriWidth;
            }
            if (oriHeight != 0) {
            	scaley = (float) mCacheHeight / (float) oriHeight;
            }
            if ((scalex != 1.0) || (scaley != 1.0)) {
                matrix.postScale(mScaleX, mScaleY);
                for (DrawInfo drawInfo : drawInfos) {
                    drawInfo.transform(matrix);
                }
            }
            mIsModified = true;
		}else {
			 mPageEditor.setDoodleItemEmpty(true);
		}
    	 if (drawInfos!=null&&drawInfos.size() > 0) {
             mPageEditor.setDoodleItemEmpty(false);
             
             // BEGIN: Better
             int pageNum = mPageEditor.getPageNum();
             for (int i = drawInfos.size() - 1; i >= 0; i--) {
            	MetaData.CurrentDrawList.add(drawInfos.size()-1-i);//group all ,by jason
             	DrawInfo drawInfo = drawInfos.get(i);
             	if (drawInfo instanceof GraphicDrawInfo) {
					GraphicDrawInfo gd=(GraphicDrawInfo)drawInfo;
					gd.setFileName(null);
					gd.reStoreBitmap(notePage);
				}
             	if (drawInfo != null) {
             		int num = addToPageOrderedDrawList(drawInfo);
             		if (num > pageNum) {
             			pageNum = num;
             		}
             	}
             }
             mPageEditor.setPageNum(pageNum);
             mDrawList.addAll(drawInfos);
        	 mObjectList.addAll(drawInfos);
             // END: Better
         }
    	  setModified(true);
    	  mPageEditor.setDoodleUndoEmpty(true);
          mPageEditor.setDoodleRedoEmpty(true);
    }
    // end jason
    @Override
    public void onDelete(final Collection<DrawInfo> drawInfos) {
        checkDelete(drawInfos);
    }

    @Override
    public void onLongClick(MotionEvent event) {
    	if (mPageEditor.getInputMode() == InputManager.INPUT_METHOD_DOODLE
    			|| mPageEditor.getTemplateType()==MetaData.Template_type_todo
				|| mPageEditor.getTemplateType()==MetaData.Template_type_travel
        		|| mPageEditor.getTemplateType()==MetaData.Template_type_meeting) {
    		//BEGIN: RICHARD
    		if(!IsClickOnDrawItem((int)event.getX() + mScrollX,(int)event.getY()+ mScrollY)){//RICHARD 
    			//emmanual
    			try{
				((EditorActivity) (mPageEditor.getEditorUiUtility().getContext())).showEditTextPopMenu(
						mPageEditor.getPageEditor().getNoteEditText(), 
				        (int) event.getX() + mScrollX, 
				        (int) event.getY() + mScrollY, 
				        false);
				}catch(BadTokenException e){
					e.printStackTrace();
				}
    		}
    		//END: RICHARD
    	}
    }
    
    // BEGIN: Better
    public void onNewPageAdded() {
        if(MetaData.IS_ENABLE_CONTINUES_MODE)
        {
	    	int newPageNo = mPageEditor.getPageNum() - 1;
	    	int cacheIndex = newPageNo % NUM_CACHE;
	
			if((mScrollY + mPageEditor.getViewHeight())/mCacheHeight == newPageNo || mScrollY/mCacheHeight == newPageNo)
			{
		    	mCacheStatusList.get(cacheIndex).setNeedRefresh(newPageNo,true,0);
		    	incraseNotifyCount();
			}
        }

    }
    
    public void onPageNumChanged() {
    	if ((mTransBounds != null) && (mPageEditor != null)) {
    		mTransBounds.bottom = mCacheHeight * mPageEditor.getPageNum();
    		if (MetaData.IS_ENABLE_CONTINUES_MODE) {
	    		if (mTransBounds.bottom < mScrollY + getHeight()) {
	            	mTransBounds.bottom = mScrollY + getHeight();
	            }
    		}
    	}
    	onNewPageAdded();
    }
    // END: Better
   
    private void callThreadToLoadPage(int index,Boolean forceRefresh,int isOnlyShapeRecognized)
    {
    	//emmanual to avoid ArrayIndexOutOfBoundsException on monkey test
		if (index % NUM_CACHE >= mCacheStatusList.size()) {
			return;
		}
    	mCacheStatusList.get(index% NUM_CACHE).setNeedRefresh(index,forceRefresh,isOnlyShapeRecognized);//.setIsDrawing(true, index);
    	incraseNotifyCount();
    }
    
    public void scrollContentTo(int x,int y)
    {
    	// BEGIN: Better
    	if (mIsTouchDownValidate && !mIsMultiTouch) {
    		touch_up(mMoveX, mMoveY, mMoveToolType, mMovePressure, 0);
    	}
    	// END: Better

        // We need to told such information to TransformDrawInfo,
        // because it need to make sure that the transformation won't exceed the canvas bounds
        if (mDrawInfo instanceof TransformDrawInfo) {
            ((TransformDrawInfo) mDrawInfo).setOffset(x, y);
            relativeMotion(x - mScrollX, y - mScrollY);
        }
    	
        mScrollX = x;
        mScrollY = y;
        
        // BEGIN: Better
        if (MetaData.IS_ENABLE_CONTINUES_MODE) {
	        if ((mTransBounds != null) && (mTransBounds.bottom < mScrollY + getHeight())) {
	        	mTransBounds.bottom = mScrollY + getHeight();
	        }
        }
        // END: Better
    	
        int page_index = mScrollY / mCacheHeight;
        int index1 = 0;
        int index2 = 0;
        index1 = page_index % NUM_CACHE;
        index2 = (page_index + 1) % NUM_CACHE;            		
        		
        if(MetaData.IS_ENABLE_CONTINUES_MODE)
        {
	    	mCacheIndex1 = index1;//BEGIN: RICHRD close continues mode
	    	mCacheIndex2 = index2;//BEGIN: RICHRD close continues mode
        }
    	mCurrentPageIndex = page_index;
    	
        if(mCacheStatusList.get(mCacheIndex1).getPageNo() != page_index)
        {
        	callThreadToLoadPage(page_index,false,CacheStatus.UPDATE_NOT_CARE);
        }
        if(MetaData.IS_ENABLE_CONTINUES_MODE)
        {
	        if((mScrollY + mPageEditor.getViewHeight())/mCacheHeight > mCurrentPageIndex)
	        {
	        	if(mCacheStatusList.get(mCacheIndex2).getPageNo() != page_index+1)
	        	{
	        		callThreadToLoadPage(page_index + 1,false,CacheStatus.UPDATE_NOT_CARE);
	        	}
	        } 
        }

        drawScreen(true, mDrawInfo);    	
    }
    
    //BEGIN: RICHARD
    //set visiable Width and Height
    public void setVisibleSize(int w, int h) {
        mViewWidth = w;
        mViewHeight = h;

    }
    //END: RICHARD

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsEnable && mPageEditor.getTemplateType()!=MetaData.Template_type_todo
        		&& mPageEditor.getTemplateType()!=MetaData.Template_type_travel
        		&& mPageEditor.getTemplateType()!=MetaData.Template_type_meeting) {
            return false;
        }
        if(((EditorActivity) (mPageEditor.getEditorUiUtility().getContext())).isEditPopupDismissFromDoodle()){
        	((EditorActivity) (mPageEditor.getEditorUiUtility().getContext())).setEditPopupDismissFromDoodle(false);
        	return false;
        }

        //BEGIN: RICHARD
        if(isCurrentPageLoading())
        {
        	return false;
        }
        //END: RICHARD
        
        float[] x = new float[2];
        float[] y = new float[2];
        float[] pressure = new float[2];//wendy
        mIsMultiTouch = event.getPointerCount() > 1;
        
    	//Begin: Allen
    	boolean isSelected = false;
    	if(mDrawInfo instanceof SelectionDrawInfo)
    	{
    		if(!((SelectionDrawInfo) mDrawInfo).isSomethingSelected())
    		{
    	    	this.detector.onTouchEvent(event);
    	    }
    		isSelected = true;
    	}
    	else
    	{
    		this.detector.onTouchEvent(event);
    	}
    	if(event.getPointerCount()==2&&(event.getActionMasked()!=MotionEvent.ACTION_CANCEL))// by jason
    	{	
    		if(!isSelected||!((SelectionDrawInfo) mDrawInfo).isSomethingSelected())
    		{
    			//modify for pen touch when the pre pointer is finger touch,by jason
    			if (cancelOneDrawing()) {
    				if (DEBUG_TOUCH) {
    			        Log.d(TAG, "ACTION_UP: (x,y) = " + "(" + x[0] + "," + y[0] + ")");
    			    }
				}
        		mIsScrolling = true;
        		mLongClickDetector.onTouch(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));   		
        		return true;
    		}
    	}
            
    	//End: Allen
        int pt1Index = event.findPointerIndex(mPointer1Id);
        int actionIndex = event.getActionIndex();
        
        if (DEBUG_TOUCH) {
            Log.d(TAG, "mIsMultiTouch = " + mIsMultiTouch);
        }

        x[0] = event.getX(0);
        y[0] = event.getY(0);
        pressure[0] = event.getPressure();
        
		// emmanual to avoid super value
		if (x[0] > mViewWidth * 100 || y[0] > mViewHeight * 100) {
			Log.e(TAG, "Super Value: " + event.toString());
			return true;
		}

        if (mIsMultiTouch) {
            x[1] = event.getX(1);
            y[1] = event.getY(1);
            pressure[1] = event.getPressure(1);
        }

        if (mScaleGestureDetector != null) {
            mScaleGestureDetector.onTouchEvent(MotionEvent.obtain(event));
        }
        else {//Allen
            mLongClickDetector.onTouch(MotionEvent.obtain(event));
        }
        
        if(mPageEditor.getInputMode() == InputManager.INPUT_METHOD_DOODLE
        		||mScaleGestureDetector!=null){ //TT388525&387901[Carol]
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:       
            	//Begin:Dave. To fix the bug: keyboard will show and disappear one time in keyboard_input_mode.
            	if((mPageEditor.getPageEditor()!=null)&&mPageEditor.getPageEditor().HasPopUpWindows)// update by jason:nullpointer
            	{
            		return true;
            	}
            	//End Dave.
            	
                if (DEBUG_TOUCH) {
                    Log.d(TAG, "ACTION_DOWN: (x,y) = " + "(" + x[0] + "," + y[0] + ")");
                }
                mPointer1Id = event.getPointerId(0);
                mPt1Validate = true;
                //Wendy for Chinese Brush Modify Begin
                touch_down(x, y, event.getToolType(0), pressure, event.getEventTime()); // Better
                //Wendy for Chinese Brush Modify End
               
                //BEGIN: RICHARD TEST
                mPageEditor.requestToBeCurrent();
                //END: RICHARD                
                break;
            case MotionEvent.ACTION_MOVE:
            	//Begin:Dave. To fix the bug: keyboard will show and disappear one time in keyboard_input_mode.
            	if((mPageEditor.getPageEditor()!=null)&&mPageEditor.getPageEditor().HasPopUpWindows)// update by jason:nullpointer
            	{
            		return true;
            	}
            	//End Dave.
            	
                if (DEBUG_TOUCH) {
                    Log.d(TAG, "ACTION_MOVE: (x,y) = " + "(" + x[0] + "," + y[0] + ")");
                }
                //Begin: Allen
                if (!mIsScrolling && mPt1Validate || mIsMultiTouch) {
                	mPageEditor.setMicroViewVisible(false);//Allen
                	mIsDrawing = true;
                	if(!((EditorActivity) (mPageEditor.getEditorUiUtility().getContext())).isEditPopupMenuShowing()){                    	
                		touch_move(event, x, y, mIsMultiTouch, event.getToolType(0), pressure, event.getEventTime()); // Better
                	}
                }
               

                //End: Allen
                break;
            case MotionEvent.ACTION_UP:
            	//Begin:Dave. To fix the bug: keyboard will show and disappear one time in keyboard_input_mode.
            	if((mPageEditor.getPageEditor()!=null)&&mPageEditor.getPageEditor().HasPopUpWindows)// update by jason:nullpointer
            	{
            		mPageEditor.getPageEditor().HasPopUpWindows = false;
            		return true;
            	}
            	//End Dave.
            	
                if (DEBUG_TOUCH) {
                    Log.d(TAG, "ACTION_UP: (x,y) = " + "(" + x[0] + "," + y[0] + ")");
                }
                //Begin: Allen
                if (mIsTouchDownValidate&&!mIsScrolling && mPt1Validate || mIsMultiTouch) {
        			if(!((EditorActivity) (mPageEditor.getEditorUiUtility().getContext())).isEditPopupMenuShowing()
                			&& !checkClickableItem((int)x[0], (int)y[0], event)){
                		touch_up(x, y, event.getToolType(0), pressure, event.getEventTime()); // Better
                	}
                }
                mIsScrolling = false;
                mIsDrawing = false;
                //End: Allen
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (actionIndex == pt1Index) {
                    mPt1Validate = true;
					//Wendy for Chinese Brush Modify Begin
                    touch_down(x, y, event.getToolType(0), pressure,event.getEventTime()); // Better
					//Wendy for Chinese Brush Modify End
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (actionIndex == pt1Index) {
                    mPt1Validate = false;
                    touch_up(x, y, event.getToolType(0), pressure,event.getEventTime()); // Better
                } else if (mPt1Validate) {
                	touch_down(x, y, event.getToolType(0), pressure,event.getEventTime()); // Better
                }
                mIsDrawing = false;
                break;
                // begin jason
            case MotionEvent.ACTION_CANCEL:
            	if (event.getPointerCount()>=2) {
            		mIsScrolling = false;
				}
	            cancelOneDrawing();	
            	break;
            	// end jason
        }
        }
        return true;
    }
    
    //add by emmanual
    public boolean checkClickableItem(float x, float y, MotionEvent event) {
    	if(mPageEditor.getEditorUiUtility().getInputMode() == InputManager.INPUT_METHOD_SELECTION_DOODLE){
    		return false;
    	}
    	//emmanual to fix bug 445841
    	View view = ((EditorActivity)mContext).findViewById(R.id.first_item);
    	ArrayList<NoteEditText> mNoteEditTexts = getAllNoteEditText(view);
    	for(NoteEditText edittext: mNoteEditTexts){
    		if(edittext.checkAndStartClickableSpan((int) x, (int) y, event, true)){
    			return true;
    		}
    	}
    	return false;
    }

	//emmanual to fix bug 445841
	private ArrayList<NoteEditText> getAllNoteEditText(View v) {
		if (!(v instanceof ViewGroup)) {
			ArrayList<NoteEditText> viewArrayList = new ArrayList<NoteEditText>();
			if (v instanceof NoteEditText)
				viewArrayList.add((NoteEditText) v);
			return viewArrayList;
		}

		ArrayList<NoteEditText> result = new ArrayList<NoteEditText>();
		ViewGroup vg = (ViewGroup) v;
		for (int i = 0; i < vg.getChildCount(); i++) {
			View child = vg.getChildAt(i);
			ArrayList<NoteEditText> viewArrayList = new ArrayList<NoteEditText>();
			if (v instanceof NoteEditText)
				viewArrayList.add((NoteEditText) v);
			viewArrayList.addAll(getAllNoteEditText(child));
			result.addAll(viewArrayList);
		}
		return result;
	}

	private boolean cancelOneDrawing() {
		boolean ret=false;
		if (mIsDrawing) {
			mDrawInfo = null;
			drawScreen(false, null);
		    mIsDrawing = false;
		    ret=true;
		}
		return ret;
	}
    
    public void setInsertMode(boolean isInsert) {
    	mInsertMode = isInsert;
    }

    public void past(NotePage page) { // Better
        if ((page == null) || (mPreferences == null)) {
        	return;
        }
        
        boolean isSamePage = false;
        String dstPath = page.getFilePath();
        String srcPath = mPreferences.getString(MetaData.PREFERENCE_EDITOR_COPY_PAGE_PATH, "");
        if (srcPath.equalsIgnoreCase(dstPath)) {
        	isSamePage = true;
        }
    	
        ArrayList<DrawInfo> drawInfos = new ArrayList<DrawInfo>();
        
    	ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(MetaData.ClipboardTable.uri, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            byte[] data = cursor.getBlob(MetaData.ClipboardTable.INDEX_DATA);
            ByteArrayInputStream b = new ByteArrayInputStream(data);
            try {
                ObjectInputStream obj = new ObjectInputStream(b);
                SerDrawInfo serDrawInfo = (SerDrawInfo)obj.readObject();
                if (serDrawInfo != null) {
	                if (serDrawInfo instanceof DoodleItem.SerTextImgInfo) {
	                	ArrayList<NoteItem.NoteItemSaveData> itemDataList = ((DoodleItem.SerTextImgInfo) serDrawInfo).getNoteItemDataList();
	                	if (itemDataList.size() > 0) {
	                		ArrayList<NoteItem> items = new ArrayList<NoteItem>();
	                		for (NoteItem.NoteItemSaveData dat : itemDataList) {
	                			Class<?> privateClass = Class.forName(((NoteItem.NoteItemSaveData) dat).getOuterClassName());
	                            NoteItem noteitem = (NoteItem) privateClass.newInstance();
	                            noteitem.load((Serializable) dat, mPageEditor.getPageEditor());
	                            items.add(noteitem);
	                		}
	                		NoteItem[] temp = items.toArray(new NoteItem[0]);
	                		String str = temp[0].getText();
	                        Editable editable = new SpannableStringBuilder(str);
	                        for (int i = 1; i < temp.length; i++) {
	                            editable.setSpan(temp[i], temp[i].getStart(), temp[i].getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	                        }
	                        ((DoodleItem.SerTextImgInfo) serDrawInfo).setContent(editable);
	                	}
	                }
	              //Begin: show_wang@asus.com
                	//Modified reason: AnnotationInfo include textimginfo
                	if (serDrawInfo instanceof DoodleItem.SerAnnotationInfo) {
                		SerDrawInfo[] SerInfos = ((DoodleItem.SerAnnotationInfo) serDrawInfo).getSerAnnotationInfo();
                		 for (SerDrawInfo serInfo : SerInfos) {
                			if (serInfo instanceof DoodleItem.SerTextImgInfo) {
                				ArrayList<NoteItem.NoteItemSaveData> SeritemDataList = ((DoodleItem.SerTextImgInfo) serInfo).getNoteItemDataList();
        	                	if (SeritemDataList.size() > 0) {
        	                		ArrayList<NoteItem> items = new ArrayList<NoteItem>();
        	                		for (NoteItem.NoteItemSaveData dat : SeritemDataList) {
        	                			Class<?> privateClass = Class.forName(((NoteItem.NoteItemSaveData) dat).getOuterClassName());
        	                            NoteItem noteitem = (NoteItem) privateClass.newInstance();
        	                            noteitem.load((Serializable) dat, mPageEditor.getPageEditor());
        	                            items.add(noteitem);
        	                		}
        	                		NoteItem[] temp = items.toArray(new NoteItem[0]);
        	                		String str = temp[0].getText();
        	                        Editable editable = new SpannableStringBuilder(str);
        	                        for (int i = 1; i < temp.length; i++) {
        	                            editable.setSpan(temp[i], temp[i].getStart(), temp[i].getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	                        }
        	                        ((DoodleItem.SerTextImgInfo) serInfo).setContent(editable);
        	                	}
                			}
                		}
                	}
                	//End: show_wang@asus.com
                	//shane
	                DrawInfo drawInfo = serDrawInfo.getDrawInfo(srcPath);
	                if (drawInfo != null) {
		                if (((drawInfo instanceof GraphicDrawInfo) || (drawInfo instanceof TextImgDrawInfo))/* && !isSamePage*/) {
		                	String fileName = "";
		                	String srcFileName = "";
		                	if (drawInfo instanceof GraphicDrawInfo) {
		                		//begin jason
		                		GraphicDrawInfo gDrawInfo=((GraphicDrawInfo)drawInfo);
		                		if (gDrawInfo.isShapeGraphic()) {	                			
		                			StringBuilder nameBuilder=new StringBuilder();
		                	    	nameBuilder.append(DoodleItem.SerGraphicInfo.SHAPE_HEAD+
		                	    			gDrawInfo.getShapeGraphicType()+
		                	    			DoodleItem.SerGraphicInfo.SHAPE_LINE+
		                	    			System.currentTimeMillis()+
		                	    			DoodleItem.SerGraphicInfo.SHAPE_END);
		                	    	fileName=nameBuilder.toString();
								}
		                		//end jason
		                		else {
									srcFileName = ((GraphicDrawInfo) drawInfo).getFileName();
									String suffix = srcFileName.substring(srcFileName.lastIndexOf("."));
									fileName = reName(suffix);
								}
		                		
		                		((GraphicDrawInfo)drawInfo).setFileName(fileName);
		                	} else {
		                		srcFileName = ((TextImgDrawInfo) drawInfo).getFileName();
		                		String suffix = srcFileName.substring(srcFileName.lastIndexOf("."));
		                		fileName = reName(suffix);
		                		((TextImgDrawInfo)drawInfo).setFileName(fileName);
		                	}		    
		                	//begin jason
		                	if (drawInfo instanceof GraphicDrawInfo&&((GraphicDrawInfo)drawInfo).isShapeGraphic()) {
		                		drawInfos.add(drawInfo);
							}else {
								File srcFile = new File(srcPath, srcFileName);
			                	if (srcFile.exists()) {
			                		boolean isAdd = true;
			                		File dstFile = new File(dstPath, fileName);
			                		try {
			                            FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
			                            FileChannel destChannel = new FileOutputStream(dstFile).getChannel();
			                            destChannel.transferFrom(srcChannel, 0, srcChannel.size());
			                            srcChannel.close();
			                            destChannel.close();
			                        } catch (FileNotFoundException e) {
			                        	isAdd = false;
			                        } catch (IOException e) {
			                        	isAdd = false;
			                        }
			                		if (isAdd) {
			                			drawInfos.add(drawInfo);
			                		}
			                	}
							}
		                	//end jason
		                
		                } 
		                //Begin: show_wang@asus.com
		                //Modified reason: paste AnnotationDrawInfo
		                //shane
		                else if ((drawInfo instanceof AnnotationDrawInfo)/*&& !isSamePage*/) {
		                	String fileName = "";
		                	String srcFileName = "";
		                	LinkedList<String> usingFiles = new LinkedList<String>();
		                	usingFiles.addAll(((AnnotationDrawInfo) drawInfo).getUsingFiles());
		                	for ( int i = 0; i < usingFiles.size(); i++) {
		                		srcFileName = usingFiles.get(i);
		                		File srcFile = new File(srcPath, srcFileName);
		                		HashMap<String, DrawInfo> fileNameToInfo = ((AnnotationDrawInfo) drawInfo).getFileToInfoMap();
			                	if (srcFile.exists()) {
			                		// BEGIN: Shane_Wang@asus.com 2013-1-18
			                		String suffix = srcFileName.substring(srcFileName.lastIndexOf("."));
			                		fileName = reName(suffix);
			                		DrawInfo currentDrawInfo = fileNameToInfo.get(srcFileName);
			                		if(currentDrawInfo instanceof GraphicDrawInfo) {
			                			((GraphicDrawInfo)currentDrawInfo).setFileName(fileName);
			                		}else if(currentDrawInfo instanceof TextImgDrawInfo) {
			                			((TextImgDrawInfo)currentDrawInfo).setFileName(fileName);
			                		}
			                		// END: Shane_Wang@asus.com 2013-1-18
			                		File dstFile = new File(dstPath, fileName);
			                		try {
			                            FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
			                            FileChannel destChannel = new FileOutputStream(dstFile).getChannel();
			                            destChannel.transferFrom(srcChannel, 0, srcChannel.size());
			                            srcChannel.close();
			                            destChannel.close();
			                        } catch (FileNotFoundException e) {
			                        } catch (IOException e) {
			                        }			                			
			                	}
		                	}
		                	drawInfos.add(drawInfo);
		                } 
		                //End: show_wang@asus.com
		                else {
		                	drawInfos.add(drawInfo);
		                }
	                }
                }
                obj.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            cursor.moveToNext();
        }
        cursor.close();

		finishDrafting();
        saveAction(ActionRecord.ACTION_ADD, drawInfos, null);
        mObjectList.addAll(drawInfos);
        
        SelectionDrawInfo selectDrawInfo = (SelectionDrawInfo) (new SelectionDrawTool(getContext())).getDrawInfo(mCurrentPaint);
        selectDrawInfo.addSelection(0, drawInfos);
        if (isSamePage) {
	        Matrix matrix = new Matrix();
	        matrix.postTranslate(COPY_SHIFT_VALUE, COPY_SHIFT_VALUE * -1);
	        selectDrawInfo.transform(matrix);
        }
        mCurrentDrawTool = selectDrawInfo.getDrawTool();
        mDrawInfo = selectDrawInfo;
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), selectDrawInfo);
        mPageEditor.setDoodleItemSelect(true, selectDrawInfo.isMultiSelect(), selectDrawInfo.isGroup());
//        mIsModified = true;//fix bug for better
//        mPageEditor.setDoodleModified();//fix bug for better
        setModified(true);//Allen
        redrawAll(true);
    }
    //BEGIN: Show   
    public void updateGraphic(Bitmap bitmap, String fileName, int type)
    {  	
    	SelectionDrawInfo selectDrawInfo = (SelectionDrawInfo) (new SelectionDrawTool(getContext())).getDrawInfo(mCurrentPaint);
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), selectDrawInfo);
        GraphicDrawInfo bitmapDrawInfo = (GraphicDrawInfo) (new GraphicDrawTool(bitmap)).getDrawInfo(mBitmapPaint);
        bitmapDrawInfo.setFileName(fileName);
        for(DrawInfo drawinfo:mDrawList){
        	if(drawinfo.equals(mTempDrawInfo))
        	{
        		// BEGIN: Better
        		deleteFromPageOrderedDrawList(mTempDrawInfo);
        		// END: Better
        		mDrawList.remove(drawinfo);
        		break;
        	}
        }   	
         selectDrawInfo.addSelection(0, bitmapDrawInfo);
         selectDrawInfo.transform(mTempMatrix);
         if(type == GRAPHIC_UPDATE)
         {	 
	         Matrix mMatrix = new Matrix();
	         mMatrix.postTranslate(mScrollX * -1, mScrollY * -1);
	         selectDrawInfo.transform(mMatrix); //smilefish fix bug 619713
	         mMatrix.reset();
	         
	         float top = selectDrawInfo.getBounds().top;
	         float left = selectDrawInfo.getBounds().left;
             if(top < 0)
             {
            	 mMatrix.setTranslate(0, Math.abs(top) );
            	 selectDrawInfo.transform(mMatrix);
             }
             if(left < 0)
             {
            	 mMatrix.setTranslate(Math.abs(left) , 0);
            	 selectDrawInfo.transform(mMatrix);
             } 
         }

         mCurrentDrawTool = selectDrawInfo.getDrawTool();
         mDrawInfo = selectDrawInfo; 
         selectFinish();
         mObjectList.addAll(selectDrawInfo.getSelectedObjects());
         setModified(true);//Allen
    }

    public void updateTextImg(Bitmap bitmap, Editable content)
    {  
    	String oldFileName = getFileInfo();
    	String filename = bitmap.toString();
    	String path = mPageEditor.getFilePath();
    	File oldFile = new File(path + "/" + oldFileName);
    	if (oldFile.exists())
    	{
    		oldFile.delete();
    	}
    	File file = new File(path + "/" + filename);
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
	    }	    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
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

    	 SelectionDrawInfo selectDrawInfo = (SelectionDrawInfo) (new SelectionDrawTool(getContext())).getDrawInfo(mCurrentPaint);
         mScaleGestureDetector = new ScaleGestureDetector(getContext(), selectDrawInfo);
         
         TextImgDrawInfo textImgDrawInfo = (TextImgDrawInfo) (new TextImgDrawTool(bitmap, content)).getDrawInfo(mBitmapPaint);
         textImgDrawInfo.setFileName(filename);
         BitmapFactory.Options option = new BitmapFactory.Options();
         option.inJustDecodeBounds = true;
         BitmapFactory.decodeFile(file.getPath(), option);
         textImgDrawInfo.setWidth((int)option.outWidth);
         textImgDrawInfo.setHeight((int)option.outHeight);
         
         selectDrawInfo.addSelection(0, textImgDrawInfo);
         getFileInfo();//get mTempMatrix and mTempDrawInfo
         if(mTempMatrix == null)
         {
        	 return;
         }
         selectDrawInfo.transform(mTempMatrix);
         mCurrentDrawTool = selectDrawInfo.getDrawTool();
         mDrawInfo = selectDrawInfo; 
         selectFinish();
         mObjectList.addAll(selectDrawInfo.getSelectedObjects());
         mTempMatrix = null;
         setModified(true);//Allen
    	}
    
    public void deleteTextImg()
    {
    	getFileInfo(); //smilefish fixed bug 556374
    	// BEGIN: Better
    	deleteFromPageOrderedDrawList(mTempDrawInfo);
		// END: Better
        mDrawList.remove(mTempDrawInfo);
        ((SelectionDrawInfo) mDrawInfo).clearSelection();
		mObjectList.remove(mTempDrawInfo);
        selectFinish();
    }
  //EDN:Show
    
    // BEGIN: Better
    public void addDrawInfo(DrawInfo info) {
    	mDrawList.add(info);
    }
    // END: Better
    
    /**
     * 璁＄畻涓�釜bitmap鍨傜洿灞呬腑鐨勪笂杈规部
     * @param bitmap
     * @param fileName
     */
	private float computerGraphicHCenter(Bitmap bitmap){
		float dy = 0;
		if ((mLongClickX == -1) && (mLongClickY == -1)) {
			int scroolY = 0;
			if (mPageEditor.getTemplateType() == MetaData.Template_type_todo) {
				scroolY = mPageEditor.getTemplateLinearLayoutScrollY();
			} else {
				scroolY = mPageEditor.getTemplateLayoutScaleHeight()
						- mPageEditor.getTemplateLinearLayoutScrollY();
			}
			if (scroolY < 0) {
				dy = (mViewHeight - bitmap.getHeight()) / 2;
			} else {
				dy = (mViewHeight + scroolY - bitmap.getHeight()) / 2;
			}
			// END: Better
		} else {
			dy = mLongClickY;
		}
		return dy;
	}

    /**
     * 灏哹itmap灏佽鎴愪竴涓猄electionDrawInfo锛屽苟鏀剧疆鍦ㄥ悎閫傜殑浣嶇疆銆傞粯璁ゆ按骞冲眳涓�
     * @param bitmap
     * @param fileName
     * @param dy        y杞翠笂鐨勫亸绉婚�?
     */
    private void prepareGraphic(Bitmap bitmap, String fileName, float dy) {
        if (DEBUG_INSERT) {
            Log.d(TAG, "prepareGraphic");
        }

        SelectionDrawInfo selectDrawInfo = (SelectionDrawInfo) (new SelectionDrawTool(getContext())).getDrawInfo(mCurrentPaint);
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), selectDrawInfo);
        GraphicDrawInfo bitmapDrawInfo = (GraphicDrawInfo) (new GraphicDrawTool(bitmap)).getDrawInfo(mBitmapPaint);
        Matrix matrix = new Matrix();
        RectF bounds = bitmapDrawInfo.getBounds();
        
        String directory = mPageEditor.getFilePath();
        
        //BEGIN:Show
        if(fileName == null)
        {
        	fileName = bitmap.toString();
        	FileOutputStream fos;
            try {
                fos = new FileOutputStream(new File(directory, fileName));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        File file = new File(directory, fileName);
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), option);
        bitmapDrawInfo.setWidth((int)option.outWidth);
        bitmapDrawInfo.setHeight((int)option.outHeight);
        
        //END:Show
        bitmapDrawInfo.setFileName(fileName);
        
        float dx = 0;
        if ((mLongClickX == -1) && (mLongClickY == -1)) {
        	dx = (mViewWidth - bounds.width()) / 2;
        	// END: Better
        }
        else {
        	dx = mLongClickX;
        	resetLongClick();
        }
        //add by Emmanual to add bitmap after supernote restart
        if(dy == Float.MIN_NORMAL){
			SharedPreferences mPreference = mContext.getSharedPreferences(
			        MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
        	dx = mPreference.getInt(CameraAttacher.PAGE_WIDTH, 0) / 4;
        	dy = mPreference.getInt(CameraAttacher.PAGE_HEIGHT, 0) / 4;
        }
        matrix.postTranslate(dx, dy);
        
        selectDrawInfo.addSelection(0, bitmapDrawInfo);
        selectDrawInfo.transform(matrix);
        mCurrentDrawTool = selectDrawInfo.getDrawTool();
        mDrawInfo = selectDrawInfo;
        mObjectList.addAll(selectDrawInfo.getSelectedObjects());
        saveAction(ActionRecord.ACTION_ADD, selectDrawInfo.getSelectedObjects(), null);
    }
    //begin jason
    private void prepareShapeGraphic(int type,Path p,String fileName){
    	  SelectionDrawInfo selectDrawInfo = (SelectionDrawInfo) (new SelectionDrawTool(getContext())).getDrawInfo(mCurrentPaint);
          mScaleGestureDetector = new ScaleGestureDetector(getContext(), selectDrawInfo);
          Paint paint=new Paint();
          PaintSelector.initPaint(paint, Color.BLACK, 5);
          GraphicDrawInfo shapeDrawInfo = (GraphicDrawInfo) (new GraphicDrawTool(type)).getDrawInfo(paint);
          shapeDrawInfo.setFileName(fileName);
          RectF bounds=new RectF();
          p.computeBounds(bounds, false);
          shapeDrawInfo.setWidth((int)bounds.width());
          shapeDrawInfo.setHeight((int)bounds.height());
          shapeDrawInfo.setShapeGraphicPath(p);
          shapeDrawInfo.reSetPaint(GraphicDrawInfo.createPaintForShapeGraphic(shapeDrawInfo));
          shapeDrawInfo.initControlPointsForShape();
          Matrix matrix = new Matrix();
          if ((mLongClickX == -1) && (mLongClickY == -1)) {
        	// BEGIN: Better
        	//Modified by show
        	int scroolY = 0;
        	if (mPageEditor.getTemplateType()==MetaData.Template_type_todo) {
        		scroolY = mPageEditor.getTemplateLinearLayoutScrollY();
        	}else {		
        		scroolY = mPageEditor.getTemplateLayoutScaleHeight() - mPageEditor.getTemplateLinearLayoutScrollY();
        	}
        	if (scroolY < 0 ) {
        		matrix.postTranslate((mViewWidth - bounds.width()) / 2, (mViewHeight - bounds.height()) / 2);
        	}
        	else {
        		matrix.postTranslate((mViewWidth - bounds.width()) / 2, (mViewHeight + scroolY - bounds.height()) / 2);
        	}
        	// END: Better
        }
        else {
            matrix.postTranslate(mLongClickX, mLongClickY);
            resetLongClick();
        }
		float ratio = Math.min(getWidth() * 0.8f / bounds.width(),getHeight() * 0.6f / bounds.height());
		if (ratio < 1) {
			matrix.postTranslate(getWidth() * 0.1f, 0);
			matrix.postScale(ratio, ratio);
		}
		matrix.postTranslate(0, getHeight() * 0.1f);
        selectDrawInfo.addSelection(0, shapeDrawInfo);
        selectDrawInfo.transform(matrix);
        mCurrentDrawTool = selectDrawInfo.getDrawTool();
        mDrawInfo = selectDrawInfo;
        mObjectList.addAll(selectDrawInfo.getSelectedObjects());
        saveAction(ActionRecord.ACTION_ADD, selectDrawInfo.getSelectedObjects(), null);
    }
    //end jason
    public void reset() {
    	mSurfaceHolder = null;
    }

    public void recycleBitmaps() {
        if (DEBUG) {
            Log.d(TAG, "releaseMemory()");
        }
        //begin jason
        released=true;
        mIsCacheInited = false; //smilefish
        //end jason
        
        // BEGIN: Better
        if (mIsPreDrawThreadRunning) {
        	mIsStopPreDrawThread = true;
        	synchronized (mPreDrawThread) {
        		mPreDrawThread.notify();
        	}
        	
        	while (mIsPreDrawThreadRunning) {
        		try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        	mIsStopPreDrawThread = false;
        }

        for (int i = 0; i < NUM_CACHE; i++) {
        	if ((mScrollCacheBitmap[i] != null) && (!mScrollCacheBitmap[i].isRecycled())) {
        		
        		 //Begin Dave. To avoid memory leak from creatBitmap();
        		if(mScrollCacheCanvas[i] != null)
        			mScrollCacheCanvas[i].setBitmap(mFreeBitmap); 
        		//End Dave.
        		
        		mScrollCacheBitmap[i].recycle();
        		mScrollCacheBitmap[i] = null;
        		mScrollCacheCanvas[i] = null;
        		mScrollCacheCanvas = null;
        	}
        	
        	if ((mScrollGraphicDrawListCacheBitmap[i] != null) && (!mScrollGraphicDrawListCacheBitmap[i].isRecycled())) {
        		if(mScrollGraphicDrawListCacheCanvas[i] != null)
        			mScrollGraphicDrawListCacheCanvas[i].setBitmap(mFreeBitmap);
        		
        		mScrollGraphicDrawListCacheBitmap[i].recycle();
        		mScrollGraphicDrawListCacheBitmap[i] = null;
        		mScrollGraphicDrawListCacheCanvas[i] = null;
        		mScrollGraphicDrawListCacheCanvas = null;
        	}
        }
        
        if(!MetaData.IS_ENABLE_CONTINUES_MODE)
        {
    		synchronized(mShapeBitmapLock)
    		{
	        	if ((mShapeCacheBitmap != null) && (!mShapeCacheBitmap.isRecycled())) {
	        		//Begin Dave. To avoid memory leak from creatBitmap();
	        		if(mShapeCacheCanvas != null)
	        			mShapeCacheCanvas.setBitmap(mFreeBitmap);
	        		//End Dave.
	        			
	        		mShapeCacheBitmap.recycle();
	        		mShapeCacheBitmap = null;
	        		mShapeCacheCanvas = null;
	        	}
    		}
        }
        // END: Better
        
        // carrot
        if((mCarrotCacheBitmap != null)&&(!mCarrotCacheBitmap.isRecycled())){
        	//Begin Dave. To avoid memory leak from creatBitmap();
        	if(mCarrotCacheCanvas != null)
        		mCarrotCacheCanvas.setBitmap(mFreeBitmap);
        	//End Dave.
        	
        	mCarrotCacheBitmap.recycle();
        	mCarrotCacheBitmap = null;
        	mCarrotCacheCanvas = null;
        }
        
        releaseListMemory(true);
        System.gc();       //Dave. To avoid memory leak from creatBitmap();
    }

    public void redo() {
        ActionRecord action = null;
        action = mRedoList.pollLast();
        if (DEBUG_ACTION) {
            Log.d(TAG, "redo(), action = " + action);
        }

        if (action != null) {
            action.redo();
            mUndoList.add(action);
            redrawAll(false);
        }

        if (mRedoList.isEmpty()) {
            mPageEditor.setDoodleRedoEmpty(true);
        }

        if (!mUndoList.isEmpty()) {
            mPageEditor.setDoodleUndoEmpty(false);
        }
    }

    public void redrawAll(boolean drawCurrent) {
        redrawCache();
        drawScreen(drawCurrent, mDrawInfo);
    }

    private void redrawCache() {    	
        drawObjects();//wendy
    }
    
    // BEGIN: Better
    private void refreshCurrentCache(int isOnlyRefreshShape) {
    	int curPageNo = mScrollY / mCacheHeight;

        callThreadToLoadPage(curPageNo,true,isOnlyRefreshShape);
        if(MetaData.IS_ENABLE_CONTINUES_MODE)
        {	
	        if((mScrollY + mPageEditor.getViewHeight())/mCacheHeight > mCurrentPageIndex)
	        {
	        	callThreadToLoadPage(curPageNo + 1,true,isOnlyRefreshShape);
	        }
        }
    }
    // END: Better
    
    //begin wendy
    private void drawObjects()
    {   	     
        refreshCurrentCache(CacheStatus.UPDATE_ALL);
    }
    //end wendy

    private boolean relativeMotion(int scrollX, int scrollY) {
        // The selected object do relative motion
        if ((mDrawInfo != null) && (mDrawInfo instanceof SelectionDrawInfo)) {
            Matrix matrix = new Matrix();
            matrix.setTranslate(scrollX * -1, scrollY * -1);
            mDrawInfo.transform(matrix);

            if (DEBUG_SCROLL) {
                Log.d(TAG, "relativeMotion(), Screen has been scrolled X to " + scrollX + " --> translate selected info " + (scrollX * -1));
                Log.d(TAG, "relativeMotion(), Screen has been scrolled Y to " + scrollY + " --> translate selected info " + (scrollY * -1));
            }
            return true;
        }
        return false;
    }
    
    // BEGIN: Better
    private void deleteFromPageOrderedDrawList(DrawInfo drawInfo) {
    	RectF rc = drawInfo.getBounds();
    	if ((rc != null) && ((rc.top >= 0) || (rc.bottom >= 0))) {
			int minPageNo = (int) (rc.top / mCacheHeight);
			if (minPageNo < 0) {
				minPageNo = 0;
			}
			int maxPageNo = (int) (rc.bottom / mCacheHeight);
			deleteDrawInfoFromPageOrderedDrawList(drawInfo, minPageNo, maxPageNo);
    	}
    }
    
    private void deleteAllFromPageOrderedDrawList(boolean isDeleteGraphic) {
    	for (int i = 0; i < mPageOrderedDrawList.size(); i++) {
    		CopyOnWriteArrayList<DrawInfo> subList = mPageOrderedDrawList.get(i);
			if (subList != null) {
				subList.clear();
			}
		}
		mPageOrderedDrawList.clear();
		
		if (isDeleteGraphic) {
			for (int i = 0; i < mPageOrderedGraphicDrawList.size(); i++) {
				CopyOnWriteArrayList<DrawInfo> subList = mPageOrderedGraphicDrawList.get(i);
				if (subList != null) {
					subList.clear();
				}
			}
			mPageOrderedGraphicDrawList.clear();
		}
    }
    
    private void deleteAllFromPageOrderedShapeDrawList() {
    	for (int i = 0; i < mDrawInfoForShapeList.size(); i++) {
			ArrayList<DrawInfo> subList = mDrawInfoForShapeList.get(i);
			if (subList != null) {
				subList.clear();
			}
		}
    	mDrawInfoForShapeList.clear();
    	
    }
    // END: Better

    public void releaseListMemory(boolean deleteall) {
        if (DEBUG) {
            Log.d(TAG, "releaseListMemory()");
        }
		if (deleteall) {
			// BEGIN: Better
			deleteAllFromPageOrderedDrawList(true);
			if (mDrawInfoForShapeList != null) {
				deleteAllFromPageOrderedShapeDrawList();
			}
			// END: Better
			
			if (mDrawList != null) {
				releaseMemory(mDrawList);
				mDrawList.clear();
			}

			if (mObjectList != null) {
				releaseMemory(mObjectList);
				mObjectList.clear();
			}
		} else { // wendy test
			// BEGIN: Better
	    	deleteAllFromPageOrderedDrawList(false);
	    	if (mDrawInfoForShapeList != null) {
				deleteAllFromPageOrderedShapeDrawList();
			}
			// END: Better
			
			if (mDrawList != null) {
				for (DrawInfo drawInfo : new LinkedList<DrawInfo>(mDrawList)) {
					if (!(drawInfo instanceof GraphicDrawInfo)) {
						mDrawList.remove(drawInfo);
					}
				}
			}
			if (mObjectList != null) {
				for (DrawInfo drawInfo : new LinkedList<DrawInfo>(mObjectList)) {
					if (!(drawInfo instanceof GraphicDrawInfo)) {
						mObjectList.remove(drawInfo);
					}
				}
			}
		}
        //BEGIN: RICHARD
        //Change order, Clear redolist first
        if (mRedoList != null && mRedoList.size()>0) {
        	Collection<ActionRecord> delCollection  = new LinkedList<ActionRecord>();
   
            for(int index = 0; index < mRedoList.size() ; index++ )
            {
            	ActionRecord record = mRedoList.get(index);
            	if(record.clearShapeDocument())//RICHARD
            	{
            		delCollection.add(record);
            	}
            }
            mRedoList.clear();
        }
        
        if (mUndoList != null && mUndoList.size()>0) {

        	Collection<ActionRecord> delCollection  = new LinkedList<ActionRecord>();
        	ListIterator<ActionRecord> it = mUndoList.listIterator(mUndoList.size()); 
            while (it.hasPrevious()) {   
            	ActionRecord record = (ActionRecord) it.previous();   
            	if(record.clearShapeDocument())//RICHARD
            	{
            		delCollection.add(record);
            	}
            } 
            mUndoList.clear();
        }
        //END: RICHARD
        
        // BEGIN: Better
        if (mAsusShape != null) {
        	mAsusShape.clearStrokes();
        }
        // END: Better

    }

    private void releaseMemory(LinkedList<DrawInfo> drawInfos) {
        for (DrawInfo drawInfo : drawInfos) {
            if (drawInfo instanceof GraphicDrawInfo) {
                ((GraphicDrawInfo) drawInfo).releaseMemory();
            }
        }
    }

    private int releaseMemory(long space) {
        if (space <= 0) {
            return 0;
        }

        int freeSpace = 0;

        if (mRedoList != null) {
            freeSpace += releaseRecordMemory(mRedoList, ActionRecord.ACTION_ADD, space);
            mPageEditor.setDoodleRedoEmpty(mRedoList.isEmpty());
        }
        if (freeSpace >= space) {
            return freeSpace;
        }
        if (mUndoList != null) {
            freeSpace += releaseRecordMemory(mUndoList, ActionRecord.ACTION_DELETE, space - freeSpace);
            mPageEditor.setDoodleUndoEmpty(mUndoList.isEmpty());
        }
        return freeSpace;
    }

    private int releaseRecordMemory(LinkedList<ActionRecord> records, int action, long space) {
        if (records == null || space <= 0) {
            return 0;
        }

        int freeSpace = 0;
        LinkedList<ActionRecord> removedRecords = new LinkedList<ActionRecord>();
        for (ActionRecord record : records) {
            if (record != null && record.mAction == action) {
                for (DrawInfo drawInfo : record.mReferences) {
                    if (drawInfo instanceof GraphicDrawInfo) {
                        freeSpace += ((GraphicDrawInfo) drawInfo).getGraphic().getByteCount();
                        ((GraphicDrawInfo) drawInfo).releaseMemory();
                    }
                }
            }
            removedRecords.add(record);
            if (freeSpace >= space) {
                break;
            }
        }
        for (ActionRecord record : removedRecords) {
            records.remove(record);
        }
        removedRecords.clear();

        return freeSpace;
    }

    private void resetLongClick() {
        mLongClickX = -1;
        mLongClickY = -1;
    }

    public DoodleItem save(NotePage notePage) {
        finishDrafting();

        LinkedList<DrawInfo> objects = getObjects();
        // BEGIN: Better
        DoodleItem item = new DoodleItem(mPageEditor.getDoodleItemWidth(), mPageEditor.getDoodleItemHeight());
        // END: Better
        item.save(objects, notePage);

        return item;
    }

    //BEGIN: RICHARD
    private void clearRedoList()
    {
        if (mRedoList != null) {
            for (ActionRecord record : mRedoList) {
            	if(record.getAction() == ActionRecord.ACTION_ADD)
            	{
            		releaseMemory(record.mReferences);
            	}
                record.clearShapeDocument();
            }
            mRedoList.clear();
        }
        mPageEditor.setDoodleRedoEmpty(true);
    }
    //END: RICHARD
    
    //darwin
    public void clearRedoUndoList()
    {
    	clearRedoList();
    	clearUndoList();
    }
    private void clearUndoList()
    {
        if (mUndoList != null) {
            for (ActionRecord record : mUndoList) {
            	if(record.getAction() == ActionRecord.ACTION_DELETE)
            	{
            		releaseMemory(record.mReferences);
            	}
                record.clearShapeDocument();
            }
            mUndoList.clear();
        }
        mPageEditor.setDoodleUndoEmpty(true);
    }
    //darwin
    //BEGIN: RICHARD
    public void saveAction(int action, ShapeDocument shapeDoc) {
        clearRedoList();//RICHARD
        
        if(mShapeDocumentList != null)//darwin
        {
	        mShapeDocumentList.add(shapeDoc);
	        ActionRecord actionRecord = new ActionRecord(action, mShapeDocumentList.size()-1);
	        if (DEBUG_ACTION) {
	            Log.d(TAG, "saveAction(), action = " + actionRecord);
	        }
	        mUndoList.add(actionRecord);
        }
        
        setModified(true);//Allen
        mPageEditor.setDoodleUndoEmpty(false);
    }
    //END: RICHARD
    
    private void saveAction(int action, Collection<DrawInfo> drawInfos, Matrix transform) {
        ActionRecord actionRecord = new ActionRecord(action, drawInfos, transform);
        if (DEBUG_ACTION) {
            Log.d(TAG, "saveAction(), action = " + actionRecord);
        }
        mUndoList.add(actionRecord);
        mPageEditor.setDoodleUndoEmpty(false);
        clearRedoList();//RICHARD
//        mIsModified = true;
//        mPageEditor.setDoodleModified();
        setModified(true);//Allen
    }

    private void saveAction(int action, DrawInfo drawInfo, Matrix transform) {
        ActionRecord actionRecord = new ActionRecord(action, drawInfo, transform);
        if (DEBUG_ACTION) {
            Log.d(TAG, "saveAction(), action = " + actionRecord);
        }
        mUndoList.add(actionRecord);
        mPageEditor.setDoodleUndoEmpty(false);
        clearRedoList();//RICHARD
        setModified(true);//Allen
    }
    
    // BEGIN: Better
    private int addToPageOrderedShapeDrawList(DrawInfo drawInfo) {
    	int pageNum = 0;
    	if (mDrawInfoForShapeList != null) {
	    	RectF rc = drawInfo.getBounds();
	        if ((rc != null) && ((rc.top >= 0) || (rc.bottom >= 0))) {
	        	int minPageNo = (int)rc.top / mCacheHeight;
	        	if (minPageNo < 0) {
	        		minPageNo = 0;
	        	}
	        	int maxPageNo = (int)rc.bottom / mCacheHeight;
	    		for (int i = minPageNo; i <= maxPageNo; i++) {
	        		ArrayList<DrawInfo> subList = null;
	        		if (i < mDrawInfoForShapeList.size()) {
	        			subList = mDrawInfoForShapeList.get(i);
	        		} else {
	        			for (int j = mDrawInfoForShapeList.size(); j <= i; j++) {
	        				mDrawInfoForShapeList.add(null);
	        			}
	        		}
	        		if (subList == null) {
	        			subList = new ArrayList<DrawInfo>();
	        			mDrawInfoForShapeList.set(i, subList);
	        		}
	        		subList.add(drawInfo);
	        	}
	
	        	if (maxPageNo + 1 > pageNum) {
	        		pageNum = maxPageNo + 1;
	        	}
	        }
    	}
        return pageNum;
    }
    
    private int updatePageOrderedShapeDrawList(DrawInfo drawInfo) {
    	int pageNum = 0;
    	
    	if (mDrawInfoForShapeList != null) {
	    	boolean isExist = false;
	    	for (int i = 0; i < mDrawInfoForShapeList.size(); i++) {
				ArrayList<DrawInfo> subList = mDrawInfoForShapeList.get(i);
				if (subList != null) {
					for (int j = 0; j < subList.size(); j++) {
						DrawInfo info = subList.get(j);
						if (info != null) {
							if (info.equals(drawInfo)) {
								subList.remove(j);
								isExist = true;
								j--;
							}
						}
					}
				}
			}
	
	    	if (isExist) {
		    	RectF rc = drawInfo.getBounds();
		        if ((rc != null) && ((rc.top >= 0) || (rc.bottom >= 0))) {
		        	int minPageNo = (int)rc.top / mCacheHeight;
		        	if (minPageNo < 0) {
		        		minPageNo = 0;
		        	}
		        	int maxPageNo = (int)rc.bottom / mCacheHeight;
		    		for (int i = minPageNo; i <= maxPageNo; i++) {
		        		ArrayList<DrawInfo> subList = null;
		        		if (i < mDrawInfoForShapeList.size()) {
		        			subList = mDrawInfoForShapeList.get(i);
		        		} else {
		        			for (int j = mDrawInfoForShapeList.size(); j <= i; j++) {
		        				mDrawInfoForShapeList.add(null);
		        			}
		        		}
		        		if (subList == null) {
		        			subList = new ArrayList<DrawInfo>();
		        			mDrawInfoForShapeList.set(i, subList);
		        		}
		        		subList.add(drawInfo);
		        	}
		
		        	if (maxPageNo + 1 > pageNum) {
		        		pageNum = maxPageNo + 1;
		        	}
		        }
	    	}
    	}
    	
        return pageNum;
    }
    
    private int addToPageOrderedDrawList(DrawInfo drawInfo) {
    	int pageNum = 0;
    	RectF rc = drawInfo.getBounds();
        if ((rc != null) && ((rc.top >= 0) || (rc.bottom >= 0))) {
        	int minPageNo = (int)rc.top / mCacheHeight;
        	if (minPageNo < 0) {
        		minPageNo = 0;
        	}
        	int maxPageNo = (int)rc.bottom / mCacheHeight;
        	addDrawInfoToPageOrderedDrawList(drawInfo, minPageNo, maxPageNo);
        	if (maxPageNo + 1 > pageNum) {
        		pageNum = maxPageNo + 1;
        	}
        }
        return pageNum;
    }
    
    private void selectFinish() {
        if (!(mDrawInfo instanceof SelectionDrawInfo)) {
            return;
        }
        LinkedHashMap<Integer, DrawInfo> selections = ((SelectionDrawInfo) mDrawInfo).getSelections();
        Matrix gestureMatrix = ((SelectionDrawInfo) mDrawInfo).getGestureMatrix();
        Matrix tranMatrix = new Matrix();
		//Begin: show_wang@asus.com
		//Modified reason: for dds
    	if( mIsConfigSave ) {
    		MetaData.CurrentDrawList.clear();
    	}
		//End: show_wang@asus.com
    	TreeSet<Integer> ts = new TreeSet<Integer>(new Comparator(){
      		 public int compare(Object o1,Object o2){
      		 Integer i1 = (Integer)o1;
      		 Integer i2 = (Integer)o2;
      		 return i2.intValue() - i1.intValue();
      		 }
      		 });
          	ts.addAll(selections.keySet());
      	
           Set<Integer> sortedSet = ts;
           
        if (selections.size() > 0) {
            tranMatrix.postTranslate(mScrollX, mScrollY);
            for (Integer i : sortedSet) {
                DrawInfo info = selections.get(i);
                info.transform(tranMatrix);
                if (i >= mDrawList.size()) {
                    mDrawList.addLast(info);
					//Begin: show_wang@asus.com
					//Modified reason: for dds
                    if( mIsConfigSave ) {
                		LinkedList<DrawInfo> removeList = new LinkedList<DrawInfo>();
                    	for (int index=0; index< mDrawList.size() - 1; index++) {
                    		DrawInfo drawinfo = mDrawList.get(index);
                			if (drawinfo instanceof EraseDrawInfo) {
                				removeList.add(drawinfo);
                			}
                		}
                	    for (DrawInfo removeinfo : removeList) {
                	        mDrawList.remove(removeinfo);
                	    }
	        	        if (!MetaData.CurrentDrawList.contains((mDrawList.size()-1))) {
	        					MetaData.CurrentDrawList.add((mDrawList.size()-1));
	        			}
                    }
					//End: show_wang@asus.com
                }
                else {
					//Begin: show_wang@asus.com
					//Modified reason: for dds
                    if( mIsConfigSave ) {
                    	LinkedList<DrawInfo> removeList = new LinkedList<DrawInfo>();
                    	int count = 0;
                    	for (int index=0; index<= i; index++) {
                    		DrawInfo drawinfo = mDrawList.get(index);
                			if (drawinfo instanceof EraseDrawInfo) {
                				removeList.add(drawinfo);
                			}
                			else{
                				if (i != 0 ){
                					count++;
                				}
                			}
                		}
                	    for (DrawInfo removeinfo : removeList) {
                	        mDrawList.remove(removeinfo);
                	    }
	        	        if (!MetaData.CurrentDrawList.contains(count)) {
	        					MetaData.CurrentDrawList.add(count);
	        			}
	        	        mDrawList.add(count, info);
                    }
					//End: show_wang@asus.com
                    else {
                    	mDrawList.add(i, info);
                    }
                }
                
                // BEGIN: Better
                int pageNum = addToPageOrderedDrawList(info);
                if (pageNum > mPageEditor.getPageNum()) {
                	mPageEditor.setPageNum(pageNum);
                	onNewPageAdded();
                }
                // END: Better
            }
        }
        if (!gestureMatrix.isIdentity()) {
            saveAction(ActionRecord.ACTION_TRANSFORM, ((SelectionDrawInfo) mDrawInfo).getSelectedObjects(), new Matrix(gestureMatrix));
            gestureMatrix.reset();
        }
        ((SelectionDrawInfo) mDrawInfo).clearSelection();
        mPageEditor.setDoodleItemSelect(false, false, false);
        redrawCache();
        if (mDrawList.size() > 0) {
            mPageEditor.setDoodleItemEmpty(false);
        }
        if (mUndoList.size() > 0) {
            mPageEditor.setDoodleUndoEmpty(false);
        }

        if (mInsertMode && !MetaData.INSERT_PHOTO_SELECTION //modified [Carol]
        		&& mPageEditor.getEditorUiUtility().getInputMode()!= InputManager.INPUT_METHOD_SELECTION_DOODLE){ //emmanual
            // Reset information to avoid backing to previous mode again and again
            mDrawInfo = null;
            mInsertMode = false;
            mPageEditor.backToPreviousMode();
        }
    }

    // Select the last none eraser DrawInfo
    private void selectLast(SelectionDrawInfo selectDrawInfo) {
        DrawInfo selectedInfo = null;
        int index = 0;

        if ((mLongClickX >= 0) && (mLongClickY >= 0)) {
        	DrawInfo bakDrawInfo = null;
        	int bakIndex = -1;
        	for (DrawInfo drawInfo : mDrawList) {
                if (!(drawInfo instanceof EraseDrawInfo)) {
                    if (drawInfo.isTouched(mLongClickX , mLongClickY)) {
                    	if ((drawInfo instanceof GraphicDrawInfo) || 
                    			drawInfo instanceof TextImgDrawInfo) {
                    		selectedInfo = drawInfo;
                    		break;
                    	} else {
                    		if (bakDrawInfo == null) {
                    			bakDrawInfo = drawInfo;
                    			bakIndex = index;
                    		}
                    	}
                    }
                }
                index++;
            }
        	if ((selectedInfo == null) && (bakDrawInfo != null)) {
        		selectedInfo = bakDrawInfo;
        		index = bakIndex;
        	}
        } else {
	        for (DrawInfo drawInfo : mDrawList) {
	            if (!(drawInfo instanceof EraseDrawInfo)) {
	                selectedInfo = drawInfo;
	                break;
	            }
	            index++;
	        }
        }
        if (selectedInfo != null) {
            RectF bounds;
            int scrollX = 0;
            int scrollY = 0;

            // Scroll screen to focus selectedInfo on the center of screen
            // 1. Translate the selected object to the correct position according to the screen's position
            // 2. Scroll the screen to the center of the selected object
            selectDrawInfo.addSelection(index, selectedInfo);

            bounds = selectedInfo.getBounds();

            if (DEBUG_SCROLL) {
                Log.d(TAG, "selectLast()");
                Log.d(TAG, "Center of selected info: [centerX, centerY] = " + "[" + bounds.centerX() + ", " + bounds.centerY() + "]");
            }
            scrollX = (int) (bounds.centerX() - (mViewWidth / 2));
            scrollY = (int) (bounds.centerY() - (mViewHeight / 2));

            // BEGIN: Better
            deleteFromPageOrderedDrawList(mDrawList.get(index));
            // END: Better
            mDrawList.remove(index);

            if (DEBUG_SCROLL) {
                Log.d(TAG, "The screen will scroll X from " + mScrollX + " to " + scrollX);
                Log.d(TAG, "The screen will scroll Y from " + mScrollY + " to " + scrollY);
            }
            relativeMotion(mScrollX, mScrollY);
            mPageEditor.ScrollViewTo(scrollX,scrollY,true);//Allen
            mPageEditor.setDoodleItemSelect(true, false, selectDrawInfo.isGroup());    
            //BEGIN:Show            
            if(selectedInfo instanceof GraphicDrawInfo&& !((GraphicDrawInfo)selectedInfo).isShapeGraphic())//update by jason
            {
          	  	mPageEditor.setDoodleCropButtonsEnable(true);   
            }
            
            if(selectedInfo instanceof TextImgDrawInfo)
            {
          	  	mPageEditor.setDoodleTextEditButtonsEnable(true);  
          	  	mEditBoxTextContent = ((TextImgDrawInfo) selectedInfo).getContent();
            }
            //END: Show
            redrawAll(true);
        }
    }
    
    //BEGIN: Show   
    public String getFileInfo(){
    	String FileName = null;
    	
        if (!(mDrawInfo instanceof SelectionDrawInfo) || mDrawInfo == null) {
            return null;
        }    
        Collection<DrawInfo> selections = ((SelectionDrawInfo) mDrawInfo).getSelectedObjects();
	
        if(selections.size() == 1 )
        { 
        	for(DrawInfo drawinfo:selections){
        	
	        	if(drawinfo instanceof GraphicDrawInfo)
	        	{    
	        		FileName = (String)((GraphicDrawInfo) drawinfo).getFileName();
	        		mTempDrawInfo = drawinfo;
	        		mTempMatrix = ((GraphicDrawInfo) drawinfo).getMatrix(); 
        		}
	        	if(drawinfo instanceof TextImgDrawInfo)
	        	{    
	        		FileName = (String)((TextImgDrawInfo) drawinfo).getFileName();
	        		mTempDrawInfo = drawinfo;
	        		mTempMatrix = ((TextImgDrawInfo) drawinfo).getMatrix(); 
        		}
        	}
        }
        return FileName;
    }   
    
    public Editable getEditBoxTextContent(){
    	return mEditBoxTextContent;
    }
    
    public void UpDateCurDrawInfo(String filename)
    {		
    	  String dir = mPageEditor.getFilePath();     
    	  BitmapFactory.Options option = new BitmapFactory.Options();
          option.inJustDecodeBounds = true;
          option.inJustDecodeBounds = false;
          Bitmap b = BitmapFactory.decodeFile(dir+"/"+filename, option);         
          updateGraphic(b, filename, GRAPHIC_UPDATE);         	 
    }   
    //END: Show  

    public void setBackground(Bitmap bitmap) {
        setBackground(bitmap, -1);
    }

    public void setBackground(Bitmap background, int color) {
        if ((mBackgroundColor == color) && (background == null)) {
            return;
        }

        mBackgroundColor = color;

        if (mCurrentDrawTool != null) {
            drawScreen(true, mDrawInfo);
        }
        else {
            drawScreen(false, null);
        }
    }

    public void setBackground(int color) {
        setBackground(null, color);
    }

    public void setPaint(Paint paint) {
        mCurrentPaint = paint;
    }

    public void setPaintTool(DrawTool paintTool) {
        if (DEBUG) {
            Log.d(TAG, "setPaintTool(), paintTool = " + paintTool.getClass().getName());
        }

        finishDrafting();

        if (mCurrentDrawTool instanceof SelectionDrawTool) {
            ((SelectionDrawTool) mCurrentDrawTool).releaseCache();
        }
        
        //carrot: set texture for pencil, brush, pen
        if(paintTool instanceof PencilDrawTool){
        	((PencilDrawTool)paintTool).SetTexture(getPencilTexure());
        }        
        else if(paintTool instanceof BrushDrawTool){
        	((BrushDrawTool)paintTool).SetTexture(getBrushTexure());
        }
        else if(paintTool instanceof PenDrawTool){
        	((PenDrawTool)paintTool).SetTexture(getPenTexure());
        }

        mCurrentDrawTool = paintTool;
        if(!((EditorActivity)mContext).isLongPressEraserState()){
            mDrawInfo = null;
        }
        mScaleGestureDetector = null;
        if ((paintTool instanceof SelectionDrawTool) && (mDrawList.size() > 0)) {
            mDrawInfo = paintTool.getDrawInfo(mCurrentPaint);
            ((TransformDrawInfo) mDrawInfo).setTransformBounds(mTransBounds);
            //Begin: show_wang@asus.com
            //Modified reason: for dds
            if ( MetaData.CurrentDrawList.size() > 0) {
            	selectDrawInfo((SelectionDrawInfo) mDrawInfo);
            }else {
            //End: show_wang@asus.com
            	selectLast((SelectionDrawInfo) mDrawInfo);
            }
            mScaleGestureDetector = new ScaleGestureDetector(getContext(), (OnScaleGestureListener) mDrawInfo);
        }
        else {
            drawScreen(false, mDrawInfo);
        }

    }
    
    // BEGIN: Better
    public int getPaintTool() {
    	return mCurrentDrawTool.getToolCode();
    }
    // END: Better

    private void showAlert(int message) {
        final View view = View.inflate(getContext(), R.layout.one_msg_dialog, null);
        final TextView textView = (TextView) view.findViewById(R.id.msg_text_view);
        textView.setText(getContext().getString(message));
        
        new AlertDialog.Builder(getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT) //add by mars for AMAX THEME
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(android.R.string.dialog_alert_title)
                .setView(view)
                .setPositiveButton(getContext().getString(android.R.string.ok), null)
                .show();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (DEBUG) {
            Log.d(TAG, "surfaceChanged()");
        }
        if (mSurfaceHolder != null) {
        	
        	//Begin:Dave. Fix a bug: a black backgroud may appear when the page loaded or rotated.
        	if(!mIsFirstDraw)
        		return;
        	//End:Dave.
        	
            redrawAll(true);
        }
    }

    //darwin
    public boolean mIsPageEditForceModified = false;
    public boolean mIsAllForceModified = false;
    public void setForceModify()
    {
    	mIsPageEditForceModified = true;
    }
    public boolean IsAllForceModify()
    {
    	return mIsAllForceModified;
    }
    //darwin
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (DEBUG) {
            Log.d(TAG, "surfaceCreate()");
        }
        //begin jason
        if (released) {
			init();
		}
        released=false;
        //end jason
        initCache();
        //darwin
        if(mIsPageEditForceModified)
        {
        	mIsAllForceModified = true;
        }
        //darwin
        
        //BEGIN: Richard
        if(CFG.getCanDoVO() == true)//darwin
        {
	        try
	        {
		    	mAsusShape = new AsusShape();
		    	mAsusShape.initShapeRecognizer();
		    	mAsusShape.prepareShapeDocument();
	        }catch(Exception e)
	        {
	        	e.printStackTrace();
	        	mAsusShape = null;
	        }
	    	mShapeList = new ArrayList<Object>();
	    	// BEGIN: Better
	    	mDrawInfoForShapeList = new ArrayList<ArrayList<DrawInfo>>();
	    	// END: Better
	    	mShapeDocumentList = new ArrayList<ShapeDocument>();
	    	
        }
    	//END: Richard
        mSurfaceHolder = holder;
        mSurfaceHolder.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (DEBUG) {
            Log.d(TAG, "surfaceDestroyed()");
        }
        mSurfaceHolder = null;
        
        //BEGIN: Richard
        if(CFG.getCanDoVO() == true)//darwin
        {
        	saveRecognizerShape();
	        try
	        {
		    	mAsusShape.deinitShapeRecognizer();
		    	mAsusShape = null;
		    	
		    	mShapeList.clear();
		    	// BEGIN: Better
		    	mDrawInfoForShapeList.clear();
		    	// END: Better
		    	mShapeDocumentList.clear();
	        }catch(Exception e)
	        {
	        	e.printStackTrace();
	        	mAsusShape = null;
	        }
        }
    	//END: Richard
        
		//+++ Dave  GA
		if(MetaData.IS_GA_ON)
		{
			GACollector gaCollector = new GACollector(mContext);
			gaCollector.penDrawCount(drawGACount);
			gaCollector.fingerDrawCount(fingerGACount);
			drawGACount = 0;
			fingerGACount = 0;

			//emmanual
			gaCollector.doodleBrushCount(0, mRollerBrushCount);
			gaCollector.doodleBrushCount(1, mPenBrushCount);
			gaCollector.doodleBrushCount(2, mBrushBrushCount);
			gaCollector.doodleBrushCount(3, mAirBrushCount);
			gaCollector.doodleBrushCount(4, mPencilBrushCount);
			gaCollector.doodleBrushCount(5, mMarkerBrushCount);
			mRollerBrushCount = 0;
			mPenBrushCount = 0;
			mBrushBrushCount = 0;
			mAirBrushCount = 0;
			mPencilBrushCount = 0;
			mMarkerBrushCount = 0;
		}
		//--- 
    }

    //Carrot: add timestamp as input for speed calculation
    private void touch_down(float[] x, float[] y, int tooltype, float[] pressure, long timestamp) { // Better
		//Wendy for Chinese Brush Modify
        if ((mLongClickX != -1) && (mLongClickY != -1)) {
            // Another drawing which not insert graphic begins
            resetLongClick();
        }
        mDrawInfo = mCurrentDrawTool.getDrawInfo(mCurrentPaint);

        // BEGIN: Better
        mMoveX[0] = x[0];
        mMoveX[1] = x[1];
        mMoveY[0] = y[0];
        mMoveY[1] = y[1];
        mMoveToolType = tooltype;
        mMovePressure[0] = pressure[0];
        mMovePressure[1] = pressure[1];
        // END: Better

        if (mDrawInfo instanceof TransformDrawInfo) {
            ((TransformDrawInfo) mDrawInfo).setTransformBounds(mTransBounds);
        }

        if (mDrawInfo instanceof SelectionDrawInfo) {
            ((SelectionDrawInfo) mDrawInfo).register(this);
        }

        if (mTransBounds.contains(x[0] + mScrollX, y[0] + mScrollY)) {
        	mIsTouchDownValidate = true;//Allen
            mStartX = x[0];
            mStartY = y[0];
            mEndX = x[0];
            mEndY = y[0];
            if(mDrawInfo instanceof SpotDrawInfo){//carrot:for new version of rollerpen, pen, brush, marker
	    		//((SpotDrawInfo)mDrawInfo).onDown(x,y,timestamp);
	    		((SpotDrawInfo)mDrawInfo).onDown(x,y,tooltype,timestamp,pressure);
	    	}
//			//Wendy for Chinese Brush Modify Begin
            else if(mDrawInfo instanceof EraseDrawInfo) {
	    		((EraseDrawInfo) mDrawInfo).onDown(x, y, tooltype,pressure);
	    	}
	    	else if(mIsEnable){
        		 mDrawInfo.onDown(x, y);
        	}
           //Wendy for Chinese Brush Modify End
        }else{
        	//emmanual to fix bug 546821, 558456, 555493, 547922, 559252, 540155
			if (mDrawInfo instanceof PathDrawInfo) {
				LinkedList<Point> mPoints = ((PathDrawInfo) mDrawInfo).getPoints();
				if (mPoints == null || mPoints.size() == 0) {
					mDrawInfo = null;
					return;
				}
			}        	
        }

        if ((mDrawInfo instanceof SelectionDrawInfo) && (!mDrawInfo.isTouched(x[0], y[0]))) {
            // No Objects are touched --> cancel selection, make the transformation of selected object effected
            selectFinish();
        }
        
        //Carrot: spot draw on special cache bitmap in realtime, clear cache bitmap every touch down
        if(mDrawInfo!=null && mDrawInfo instanceof BallPenDrawInfo){
	        synchronized(mCarrotCacheBitmapLock){
	        	mCarrotCacheBitmap.eraseColor(Color.TRANSPARENT);
	        	mDrawInfo.getDrawTool().draw(mCarrotCacheCanvas, false, mDrawInfo);
	        	//mDrawInfo.resetDirty();
	        }
        }
        
        drawScreen(true, mDrawInfo);
    }

    private void touch_move(MotionEvent event, float[] x, float[] y, boolean mIsMultiTouch, int tooltype, float[] pressure, long timestamp) { // Better
        boolean isPoint1InRange = mTransBounds.contains(x[0] + mScrollX, y[0] + mScrollY);
        if (MetaData.IS_ENABLE_CONTINUES_MODE) {
        	isPoint1InRange = isPoint1InRange && (y[0] < getHeight());
        }
        boolean isPoint2InRange = true;
        // BEGIN: Better
        mMoveX[0] = x[0];
        mMoveX[1] = x[1];
        mMoveY[0] = y[0];
        mMoveY[1] = y[1];
        mMoveToolType = tooltype;
        mMovePressure[0] = pressure[0];
        mMovePressure[1] = pressure[1];
        // END: Better

        if (mIsMultiTouch && !mTransBounds.contains(x[1] + mScrollX, y[1] + mScrollY)) {
            isPoint2InRange = false;
        }
        if (!isPoint1InRange) {
            if ((mStartX != -1) && (mStartY != -1)) {
                mEndX = x[0];
                mEndY = y[0];
                touch_up(x, y, tooltype, pressure,timestamp); // Better
                mStartX = -1;
                mStartY = -1;
            }
        }
        else if (isPoint1InRange && isPoint2InRange) {
            if ((mStartX == -1) && (mStartY == -1)) {
                mStartX = x[0];
                mStartY = y[0];
				//Wendy for Chinese Brush Modify Begin
                touch_down(x, y, 0, pressure,timestamp); // Better
				//Wendy for Chinese Brush Modify End
            }
            else if (mDrawInfo != null) {
            	if(mDrawInfo instanceof SpotDrawInfo){//carrot:for new version of rollerpen,pen,brush, marker
		    		//((SpotDrawInfo)mDrawInfo).onMove(x,y, mIsMultiTouch,timestamp);
					((SpotDrawInfo)mDrawInfo).onMove(x,y,mIsMultiTouch,tooltype,timestamp,pressure);
		    	}
            	else if(mDrawInfo instanceof EraseDrawInfo){//RICHARD
            		((EraseDrawInfo) mDrawInfo).onMove(x, y, mIsMultiTouch,tooltype, pressure);
            	}
            	else if(mIsEnable){
            		mDrawInfo.onMove(event, x, y, mIsMultiTouch);
            	}
            	
            	//Carrot: spot draw on special cache bitmap in realtime
            	if(mDrawInfo!=null && mDrawInfo instanceof BallPenDrawInfo){
	            	synchronized(mCarrotCacheBitmapLock){
	                	mDrawInfo.getDrawTool().draw(mCarrotCacheCanvas, false, mDrawInfo);
	                	//mDrawInfo.resetDirty();
	                }
            	}            	
            	
                drawScreen(true, mDrawInfo);
            }
        }
        mIsMultiTouch = false;
    }

    //darwin
    public class ShapeRecgnizeCountdownCounter extends CountDownTimer {
        private ProgressDialog mPd;
        private boolean mNoteShow = false;
        public ShapeRecgnizeCountdownCounter(Long time , Long countDownInterval) {
            super(time, countDownInterval);
        }

        public void setProgressDialog(ProgressDialog pd)
        {
        	mPd = pd;
        }
        
        public void setNotShow()
        {
        	mNoteShow = true;
        }
        
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
        	if(!mNoteShow)
        	{
        		mPd.show();
        	}
        }
    }
    //darwin
    
    private void touch_up(float[] x, float[] y, int tooltype, float[] pressure, long timestamp) { // Better
    	mIsTouchDownValidate = false;//Allen
    	Matrix scrollMatrix = new Matrix();
		
    	//+++ Dave for GA
    	if(tooltype == 1)
    	{
    		fingerGACount++;
    	}else if(tooltype == 2)
    	{
    		drawGACount++;
    	}
    	//---
    	
    	//emmanual
		if (mCurrentDrawTool instanceof PencilDrawTool) {
			mPencilBrushCount++;
		} else if (mCurrentDrawTool instanceof BrushDrawTool) {
			mBrushBrushCount++;
		} else if (mCurrentDrawTool instanceof PenDrawTool) {
			mPenBrushCount++;
		} else if (mCurrentDrawTool instanceof MarkerDrawTool) {
			mMarkerBrushCount++;
		} else if (mCurrentDrawTool instanceof AirBrushDrawTool) {
			mAirBrushCount++;
		} else if (mCurrentDrawTool instanceof PathDrawTool) {
			mRollerBrushCount++;
		}
		
        if (mDrawInfo == null) {
            return;
        }

        if (mTransBounds.contains(x[0] + mScrollX, y[0] + mScrollY)) {
            if ((mStartX == x[0]) && (mStartY == y[0])) {
                x[0] += 1;
                y[0] += 1;
            }
            
            if(mDrawInfo instanceof SpotDrawInfo){
        		((SpotDrawInfo)mDrawInfo).onUp(x,y,tooltype,timestamp,pressure);
        	}
            else if(mDrawInfo instanceof EraseDrawInfo){//RICHARD
        		((EraseDrawInfo) mDrawInfo).onUp(x, y,tooltype, pressure);
        	}
            else if(mIsEnable){
            	mDrawInfo.onUp(x, y);
            }
        }
        else {
            if ((mStartX == mEndX) && (mStartY == mEndY)) {
                mEndX += 1;
                mEndY += 1;
            }
            
            if(mDrawInfo instanceof SpotDrawInfo){//carrot:for new version of rollerpen,pen,brush, marker
        		((SpotDrawInfo)mDrawInfo).onUp(x,y,tooltype,timestamp,pressure);
        	}
            else if(mDrawInfo instanceof EraseDrawInfo){//RICHARD
            	((EraseDrawInfo) mDrawInfo).onUp(x, y,tooltype, pressure);
        	}
            else {
            	mDrawInfo.onUp(new float[] { mEndX }, new float[] { mEndY });
            }
        }
        mStartX = -1;
        mStartY = -1;
        
        //Carrot@20130910: do specil job of draw to cache for pen & brush
        //Carrot: spot draw on special cache bitmap in realtime
    	if(mDrawInfo!=null && mDrawInfo instanceof BallPenDrawInfo){
        	synchronized(mCarrotCacheBitmapLock){
            	mDrawInfo.getDrawTool().draw(mCarrotCacheCanvas, false, mDrawInfo);
            }
    	}

        // SelectionDrawInfo will never be added to mDrawList
        if ((mDrawInfo instanceof SelectionDrawInfo)) {
            if (((SelectionDrawInfo) mDrawInfo).isSomethingSelected()) {
                drawScreen(true, mDrawInfo);
            }
            else {
                Collection<DrawInfo> selections = ((SelectionDrawInfo) mDrawInfo).select(mViewWidth, mViewHeight, mDrawList, mScrollX, mScrollY);
                if (selections.size() > 0) {
                	// BEGIN: Better
                	for (DrawInfo info : selections) {
                		if (info != null) {
                			deleteFromPageOrderedDrawList(info);
                		}
                	}
                	// END: Better
                    mDrawList.removeAll(selections);
                    redrawAll(true);
                    mPageEditor.setDoodleItemSelect(true, ((SelectionDrawInfo) mDrawInfo).isMultiSelect(), ((SelectionDrawInfo) mDrawInfo).isGroup() || ((SelectionDrawInfo) mDrawInfo).containGroup());
                    //BEGIN:Show
                    if(selections.size() == 1 )
                    { 
                    	for(DrawInfo drawinfo:selections){
                    	
            	        	if(drawinfo instanceof GraphicDrawInfo&& !((GraphicDrawInfo)drawinfo).isShapeGraphic())//update by jason
            	        	{ 
            	        		mPageEditor.setDoodleCropButtonsEnable(true);
            	        	}
            	        	if(drawinfo instanceof TextImgDrawInfo)
            	        	{ 
            	        		mPageEditor.setDoodleTextEditButtonsEnable(true);
            	        		mEditBoxTextContent = ((TextImgDrawInfo) drawinfo).getContent();
            	        	}
                    	}
                    } 
                    //END:Show
                }
                else {
                    ((SelectionDrawInfo) mDrawInfo).clearSelectionFrame();
                    redrawAll(false);
                    mPageEditor.setDoodleItemSelect(false, false, false);
                }
            }
            return;
        }
        // TransformDrawInfo will be added to mDrawList when finish drawing
        else if (mDrawInfo instanceof TransformDrawInfo) {
            drawScreen(true, mDrawInfo);
            return;
        }

        // Translate to the actual position
        scrollMatrix.postTranslate(mScrollX, mScrollY);
        mDrawInfo.transform(scrollMatrix);

        // We save erase info to each DrawInfo
        // We retain original EraseDrawInfo id because redo/undo need it
        if (mDrawInfo instanceof EraseDrawInfo) {        
        	//boolean hasGraphic = false;//wendy
            for (DrawInfo drawInfo : mDrawList) {

            	if(drawInfo instanceof GraphicDrawInfo || drawInfo instanceof TextImgDrawInfo) continue;//modified by show
            	
            	//wendy end 
                if (drawInfo.add((EraseDrawInfo) mDrawInfo, false)) {
                    ((EraseDrawInfo) mDrawInfo).mReferenceCount++; 
                }
            }
            if (((EraseDrawInfo) mDrawInfo).mReferenceCount > 0) {
                ((EraseDrawInfo) mDrawInfo).clearData();
                // BEGIN: Better
                int pageNum = addToPageOrderedDrawList(mDrawInfo);
                if (pageNum > mPageEditor.getPageNum()) {
                	mPageEditor.setPageNum(pageNum);
                	onNewPageAdded();
                }
                // END: Better
                mDrawList.addFirst(mDrawInfo);
                mObjectList.add(mDrawInfo);
                saveAction(ActionRecord.ACTION_ADD, mDrawInfo, null);
            }
        }
        else if (!mDrawInfo.getBounds().isEmpty()) {
            //BGEIN: RICHARD
            if(mIsNeedShape // carrot: enable for new brushes
            		&& (mDrawInfo instanceof PathDrawInfo || mDrawInfo instanceof SpotDrawInfo) 
            		&& mAsusShape!= null)
            {//Start shape recognization
            	ProgressDialog dialog = new ProgressDialog(mContext);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setProgress(0);
                dialog.setMax(1);
                dialog.setCancelable(false);
                dialog.setTitle(getContext().getString(R.string.recognizing));
                ShapeRecgnizeTask SRT = new ShapeRecgnizeTask(this,mAsusShape,mDrawInfo);
                //begin jason
                canShowDialogInSevice(dialog);
                //end jason
                SRT.setDialog(dialog);
                ShapeRecgnizeCountdownCounter scc = new ShapeRecgnizeCountdownCounter((long)1000, (long)1000);
                scc.setProgressDialog(dialog);
                SRT.setCounter(scc);
                SRT.execute();
                scc.start();
            }
            else
            {
            	//Carrot@20130910: do specil job of draw to cache for pen & brush
            	drawCurrentPathToCache();
                
            	mCurrentTime.setToNow();
            	if(mCurrentTime.toMillis(true) - mLastDrawInfoTime.toMillis(true) < 3000 && mDrawList.size()>0)
            	{// less than 3s
            		if(mLatestDrawInfoGroup == null)
            		{
            			mLatestDrawInfoGroup = new AnnotationDrawInfo(new AnnotationDrawTool(), new Paint());

            			// BEGIN: Better
            			deleteFromPageOrderedDrawList(mDrawList.get(0));
            			// END: Better
            			DrawInfo info = mDrawList.remove(0);
            			mLatestDrawInfoGroup.add(info, false);
            			mLatestDrawInfoGroup.add(mDrawInfo, false);
            			saveAction(ActionRecord.ACTION_GROUP_ADD_NEW, mLatestDrawInfoGroup.getDrawInfos(), null);
            			
            			// BEGIN: Better
            			int pageNum = addToPageOrderedDrawList(mLatestDrawInfoGroup);
            			if (pageNum > mPageEditor.getPageNum()) {
            				mPageEditor.setPageNum(pageNum);
            				onNewPageAdded();
            			}
            			// END: Better
            			mDrawList.addFirst(mLatestDrawInfoGroup);
            		}else
            		{
            			mLatestDrawInfoGroup.add(mDrawInfo, false);
            			// BEGIN: Better
            			int pageNum = addToPageOrderedDrawList(mDrawInfo);
            			if (pageNum > mPageEditor.getPageNum()) {
            				mPageEditor.setPageNum(pageNum);
            				onNewPageAdded();
            			}
            			// END: Better
            			saveAction(ActionRecord.ACTION_GROUP_ADD, mLatestDrawInfoGroup.getDrawInfos(), null);
            		}
            	}
            	else //long than 3s
            	{
            		// BEGIN: Better
        			int pageNum = addToPageOrderedDrawList(mDrawInfo);
        			if (pageNum > mPageEditor.getPageNum()) {
        				mPageEditor.setPageNum(pageNum);
        				onNewPageAdded();
        			}
        			// END: Better
	                mDrawList.addFirst(mDrawInfo);
	                saveAction(ActionRecord.ACTION_ADD, mDrawInfo, null);
	                
	                mLatestDrawInfoGroup = null;
            	}
                mObjectList.add(mDrawInfo);
//            	mCurrentDrawTool.draw(mCacheBmpCanvas, mTempBitmap, mDrawInfo);
                
            	mLastDrawInfoTime.setToNow();
            }
            //END: RICHARD
            drawScreen(false, mDrawInfo);
        }

        //BEGIN: RICHARD
        setDoodleItemIsEmpty();
        //END: RICHARD
        mDrawInfo = null;

		//emmanual to fix bug 537067, 537904
		if (mCurrentDrawTool != null
		        && mCurrentDrawTool instanceof MarkerDrawTool) {
			drawScreen();
		}
    }
    //begin jason
    private void canShowDialogInSevice(Dialog dlg){
    	dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    }
    //end jason
    
    private void drawCurrentPathToCache(){
    	// BEGIN: Better Richard Modify
		synchronized(mCacheStatusList.get(mCacheIndex1).getDrawingLockObj())
		{
			doCurrentPathToCache(mScrollCacheCanvas[mCacheIndex1],mDrawInfo,mCurrentDrawTool,0,-(mCacheHeight * (mScrollY / mCacheHeight)));
		}
		if(MetaData.IS_ENABLE_CONTINUES_MODE)
        {
	        if((mScrollY + mPageEditor.getViewHeight())/mCacheHeight > mCurrentPageIndex)
	        {
    			synchronized(mCacheStatusList.get(mCacheIndex2).getDrawingLockObj())
    			{
    				doCurrentPathToCache(mScrollCacheCanvas[mCacheIndex2],mDrawInfo,mCurrentDrawTool,0,-(mCacheHeight * (mScrollY / mCacheHeight + 1) ));
    			}
	        } 
        }
		// END: Better
    }
    
    private void doCurrentPathToCache(Canvas c,DrawInfo drawInfo,DrawTool drawtool,float xoff,float yoff) {
    	c.save();
        c.translate(xoff, yoff);
        //Carrot@20130910: do specil job of draw to cache for pen & brush
        if(drawInfo instanceof BallPenDrawInfo){
        	RectF  rtF = drawInfo.getBounds();
        	Rect rtd = new Rect();
        	rtd.set((int)rtF.left,(int)rtF.top,(int)rtF.right,(int)rtF.bottom); 
        	Rect rts = new Rect(rtd);
        	rts.offset(-mScrollX, -mScrollY);
        	c.drawBitmap(mCarrotCacheBitmap, rts, rtd, mBitmapPaint);
        	//mCarrotCacheBitmap.eraseColor(Color.TRANSPARENT);
        }
        else{    	
        	drawtool.draw(c, false, drawInfo);////Richard null -> false
        }
        c.restore();
    }
    
    PathDrawInfo getArrowLinePathDrawInfo(short[] line,float ang,float size)
    {
		MyPoint p1 = new MyPoint(line[2] - line[0],line[3] - line[1], size);
		float[] dst1 = p1.getRotateResMyPoint(ang);
		short[] pointsArrowLine = new short[4];
		pointsArrowLine[0] = line[0];
		pointsArrowLine[1] = line[1];
		pointsArrowLine[2] = (short)(line[0] + dst1[0]);
		pointsArrowLine[3] = (short)(line[1] + dst1[1]);
		
		PathDrawInfo pathDrawInfoArrowLine = new PathDrawInfo(mCurrentShapeDrawTool,mCurrentShapePaint,pointsArrowLine);
		
		return pathDrawInfoArrowLine;
    }
    
    int doDrawArrow(AnnotationDrawInfo group, short[] line,float ang,double size , int localIndex)
    {
		PathDrawInfo arrowLinePathDrawInfo1 = getArrowLinePathDrawInfo(line,ang,(float)size);

		group.add(/*localIndex++,*/arrowLinePathDrawInfo1, true);
		// BEGIN: Better
		int pageNum = updatePageOrderedShapeDrawList(group);
		if (pageNum > mPageEditor.getPageNum()) {
			mPageEditor.setPageNum(pageNum);
			onNewPageAdded();
		}
		// END: Better
		
		PathDrawInfo arrowLinePathDrawInfo2 = getArrowLinePathDrawInfo(line,-ang,(float)size);
		group.add(/*localIndex++,*/arrowLinePathDrawInfo2, true);
		// BEGIN: Better
		pageNum = updatePageOrderedShapeDrawList(group);
		if (pageNum > mPageEditor.getPageNum()) {
			mPageEditor.setPageNum(pageNum);
			onNewPageAdded();
		}
		// END: Better
		
		return localIndex;
    }
    
    int doDrawArcArrow(AnnotationDrawInfo group, Boolean direct,float realStartAng ,float[] info,float ang,double size , int localIndex)
    {
		MyPoint startPoint; 
		MyPoint tangentPoint; 
				
		if(realStartAng == 90)
		{
			startPoint=new MyPoint(0,info[3]);
			tangentPoint = new MyPoint(-1,info[3]);
		}
		else if(realStartAng == 270)
		{
			startPoint=new MyPoint(0,-info[3]);
			tangentPoint = new MyPoint(1,-info[3]);
		}
		else
		{
			double radAng = realStartAng/180*Math.PI;
			double tanValue = Math.tan(radAng);
			double dx = (1/Math.sqrt(1.0/(info[2]*info[2]) + Math.pow(tanValue,2)/(info[3]*info[3])));
			double dy = (dx * tanValue);
			
			float x = (float)dx;
			float y = (float)dy;
			
			double sinValue = Math.sin(radAng);
			double cosValue = Math.cos(radAng);
			
			float tangentPointy = 1;
			if(sinValue > 0 && y < 0)
			{
				y = -y;
			}
			else if(sinValue < 0 && y > 0)
			{
				y = -y;
			}
			
			if(cosValue < 0)
			{
				x = -x;
				if(direct)
				{
					tangentPointy = y-1;
				}
				else
				{
					tangentPointy = y+1;
				}
			}
			else
			{
				if(direct)
				{
					tangentPointy = y+1;
				}
				else
				{
					tangentPointy = y-1;
				}
			}

			startPoint = new MyPoint(x,y);
			
			tangentPoint = new MyPoint((1-y*tangentPointy/(info[3]*info[3]))*(info[2]*info[2])/x,tangentPointy );
		}
		
		
		float[] startPointAfterRotate = startPoint.getRotateResMyPoint(info[6]);
		float[] tangentPointAfterRotate = tangentPoint.getRotateResMyPoint(info[6]);

		
		short[] pointsEnd = new short[4];
		pointsEnd[0] = (short)(startPointAfterRotate[0] + 0.5 + (short)info[0]);
		pointsEnd[1] = (short)(startPointAfterRotate[1] + 0.5 + (short)info[1]);
		pointsEnd[2] = (short)(pointsEnd[0] + ((tangentPointAfterRotate[0] - startPointAfterRotate[0])*100));
		pointsEnd[3] = (short)(pointsEnd[1] + ((tangentPointAfterRotate[1] - startPointAfterRotate[1])*100));
		
		localIndex = doDrawArrow(group, pointsEnd,ang,size,localIndex);
		
		return localIndex;
    }
    
    void doDrawShapeToBitmap(ArrayList<Object> shapeList)
    {
    	AnnotationDrawInfo group = new AnnotationDrawInfo(new AnnotationDrawTool(), new Paint());
    	int localIndex = 0;
    	Boolean groupflag = false;
    	for(Object shape :shapeList)
    	{
			if(shape instanceof ShapeLineData)
			{
				ShapeLineData shapedata = (ShapeLineData)shape;
				
				final ShapePointData p1_ = shapedata.getP1();
				final ShapePointData p2_ = shapedata.getP2();
				
				short[] points = new short[4];
				points[0] = (short) p1_.getX();
				points[1] = (short) p1_.getY();
				points[2] = (short) p2_.getX();
				points[3] = (short) p2_.getY();				

				PathDrawInfo pathDrawInfo = new PathDrawInfo(mCurrentShapeDrawTool,mCurrentShapePaint,points);
				if(groupflag)
				{
					group.add(pathDrawInfo, false);
					// BEGIN: Better
					int pageNum = updatePageOrderedShapeDrawList(group);
					if (pageNum > mPageEditor.getPageNum()) {
						mPageEditor.setPageNum(pageNum);
						onNewPageAdded();
					}
					// END: Better
				}
				else
				{
					if(mDrawInfoForShapeList != null)//darwin
					{
//						mDrawInfoForShapeList.add(pathDrawInfo);
						// BEGIN: Better
						int pageNum = addToPageOrderedShapeDrawList(pathDrawInfo);
						if (pageNum > mPageEditor.getPageNum()) {
							mPageEditor.setPageNum(pageNum);
							onNewPageAdded();
						}
						// END: Better
					}
				}
			}
			else if(shape instanceof  ShapeEllipticArcData)
			{
				final ShapeEllipticArcData data = (ShapeEllipticArcData)shape;
				
				DrawTool drawTool = new ShapeDrawTool(ShapeDrawTool.ARC_TOOL);
				ArcDrawInfo arcDrawInfo = new ArcDrawInfo(drawTool, mCurrentShapePaint, data);	
				if(groupflag)
				{
					group.add(/*localIndex++,*/arcDrawInfo, false);
					// BEGIN: Better
					int pageNum = updatePageOrderedShapeDrawList(group);
					if (pageNum > mPageEditor.getPageNum()) {
						mPageEditor.setPageNum(pageNum);
						onNewPageAdded();
					}
					// END: Better
				}
				else
				{
					if(mDrawInfoForShapeList != null)//darwin
					{
//						mDrawInfoForShapeList.add(arcDrawInfo);
						// BEGIN: Better
						int pageNum = addToPageOrderedShapeDrawList(arcDrawInfo);
						if (pageNum > mPageEditor.getPageNum()) {
							mPageEditor.setPageNum(pageNum);
							onNewPageAdded();
						}
						// END: Better
					}
				}
			}
			else if(shape instanceof ShapeDecoratedLineData)
			{
				if(!groupflag)
				{
					groupflag = true;
					if(localIndex != 0)
					{
						group = new AnnotationDrawInfo(new AnnotationDrawTool(), new Paint());
						localIndex = 0;
					}
					if(mDrawInfoForShapeList != null)//darwin
					{
//						mDrawInfoForShapeList.add(group);
						// BEGIN: Better
						int pageNum = addToPageOrderedShapeDrawList(group);
						if (pageNum > mPageEditor.getPageNum()) {
							mPageEditor.setPageNum(pageNum);
							onNewPageAdded();
						}
						// END: Better
					}
				}
				
				ShapeDecoratedLineData shapeDecoratedLineData = (ShapeDecoratedLineData)shape;
				
				final ShapePointData p1_ = shapeDecoratedLineData.getLine().getP1();
				final ShapePointData p2_ = shapeDecoratedLineData.getLine().getP2();
				
				short[] points = new short[4];
				points[0] = (short) p1_.getX();
				points[1] = (short) p1_.getY();
				points[2] = (short) p2_.getX();
				points[3] = (short) p2_.getY();
				
				PathDrawInfo pathDrawInfo = new PathDrawInfo(mCurrentShapeDrawTool,mCurrentShapePaint,points);
				group.add(/*localIndex++,*/pathDrawInfo, false);
				// BEGIN: Better
				int pageNum = updatePageOrderedShapeDrawList(group);
				if (pageNum > mPageEditor.getPageNum()) {
					mPageEditor.setPageNum(pageNum);
					onNewPageAdded();
				}
				// END: Better
				
				if(shapeDecoratedLineData.getP1Decoration() == DecorationType.ARROW_HEAD)
				{
					localIndex = doDrawArrow(group, points,ARROW_DEGREE,Math.sqrt(mCurrentShapePaint.getStrokeWidth()) * ARROW_LINE_LENGTH_FACTOR,localIndex);
				}

				if(shapeDecoratedLineData.getP2Decoration() == DecorationType.ARROW_HEAD)
				{
					short[] pointsEnd = new short[4];
					pointsEnd[0] = points[2];
					pointsEnd[1] = points[3];
					pointsEnd[2] = points[0];
					pointsEnd[3] = points[1];
					
					localIndex = doDrawArrow(group, pointsEnd,ARROW_DEGREE,Math.sqrt(mCurrentShapePaint.getStrokeWidth()) * ARROW_LINE_LENGTH_FACTOR,localIndex);
				}
			}
			else if(shape instanceof ShapeDecoratedEllipticArcData)
			{
				if(!groupflag)
				{
					groupflag = true;
					if(localIndex != 0)
					{
						group = new AnnotationDrawInfo(new AnnotationDrawTool(), new Paint());
						localIndex = 0;
					}
					if(mDrawInfoForShapeList != null)//darwin
					{
						// BEGIN: Better
						int pageNum = addToPageOrderedShapeDrawList(group);
						if (pageNum > mPageEditor.getPageNum()) {
							mPageEditor.setPageNum(pageNum);
							onNewPageAdded();
						}
						// END: Better
					}
				}
				
				final ShapeDecoratedEllipticArcData data = (ShapeDecoratedEllipticArcData)shape;
				
				DrawTool drawTool = new ShapeDrawTool(ShapeDrawTool.ARC_TOOL);
				ArcDrawInfo arcDrawInfo = new ArcDrawInfo(drawTool, mCurrentShapePaint, data.getArc());	

				group.add(/*localIndex++,*/arcDrawInfo, false);
				// BEGIN: Better
				int pageNum = updatePageOrderedShapeDrawList(group);
				if (pageNum > mPageEditor.getPageNum()) {
					mPageEditor.setPageNum(pageNum);
					onNewPageAdded();
				}
				
				final ShapePointData center = data.getArc().getCenter();
				short maxR = (short)data.getArc().getMaxRadius();
				short minR = (short)data.getArc().getMinRadius();
				float[] info = new float[7];
				info[0] = (short)center.getX();
				info[1] = (short)center.getY();
				info[2] = maxR;//data.getMaxRadius();
				info[3] = minR;//data.getMinRadius();
				
				info[4] = (float)Math.toDegrees(data.getArc().getStartAngle());
				info[5] = (float)Math.toDegrees(data.getArc().getSweepAngle());
				info[6] = (float)Math.toDegrees(data.getArc().getOrientation());
				
				if(data.getFirstDecoration() == DecorationType.ARROW_HEAD)
				{
					localIndex = doDrawArcArrow(group,data.getArc().getSweepAngle()<0?false:true,(float)Math.toDegrees(data.getArc().getStartAngle()),info,ARROW_DEGREE,Math.sqrt(mCurrentPaint.getStrokeWidth()) * ARROW_LINE_LENGTH_FACTOR,localIndex);
				}
				
				if(data.getLastDecoration() == DecorationType.ARROW_HEAD)
				{
					localIndex = doDrawArcArrow(group,data.getArc().getSweepAngle()<0?true:false,(float)Math.toDegrees(data.getArc().getSweepAngle()+data.getArc().getStartAngle()),info,ARROW_DEGREE,Math.sqrt(mCurrentPaint.getStrokeWidth()) * ARROW_LINE_LENGTH_FACTOR,localIndex);
				}

			}
			else if(shape instanceof Integer)
			{
				final Integer i = (Integer)shape;
				if(i == 1)
				{
					groupflag = false;
				}
				else if(i > 1)
				{					
					groupflag = true;
					if(localIndex != 0)
					{
						group = new AnnotationDrawInfo(new AnnotationDrawTool(), new Paint());
						localIndex = 0;
					}
					if(mDrawInfoForShapeList != null)//darwin
					{
						// BEGIN: Better
						int pageNum = addToPageOrderedShapeDrawList(group);
						if (pageNum > mPageEditor.getPageNum()) {
							mPageEditor.setPageNum(pageNum);
							onNewPageAdded();
						}
						// END: Better
					}
				}
			}
    	}
    }
    
    //BEGIN: Richard
    public void doDrawShapeDocument(ShapeDocument shapedocument)
    {    	   	    	
		if(	mAsusShape == null || 
		mDrawInfoForShapeList == null)//darwin
		{
			return;
		}
		
    	ArrayList<Object> shapeList = mAsusShape.getShapeAndStrok(shapedocument);
		mShapeList = shapeList;
		
		// BEGIN: Better
		deleteAllFromPageOrderedShapeDrawList();
		// END: Better
		
		if(shapeList != null && shapeList.size() != 0)
		{
			doDrawShapeToBitmap(shapeList);
		}
		else
		{
			mPageEditor.showShapeFailedToast();//smilefish
		}
		
    	refreshCurrentCache(CacheStatus.ONLY_UPDATE_SHAPE);//RICHARD
    	
    	setDoodleItemIsEmpty();//RICHARD
    }
    
    void doShapeRecgnizeAndDraw(DrawInfo drawInfo)
    {	
		if(drawInfo instanceof PathDrawInfo)
		{
			PathDrawInfo temp = (PathDrawInfo)drawInfo;
			if(mAsusShape != null)//darwin
	    	{
	    		mAsusShape.addStroke(temp.getPointArray());
	    	}
		}
		else
		{
			return;
		}
		if(mAsusShape != null)//darwin
    	{
			ShapeDocument shapedocument = mAsusShape.getResultShapeDocument(); 
    	
	    	if(shapedocument == null)
	    	{
	    		return;
	    	}
	    	try
	    	{
		    	saveAction(ActionRecord.ACTION_SHAPE,(ShapeDocument)shapedocument.clone());
		    	doDrawShapeDocument(shapedocument);
		    }catch(Exception e)
			{
				e.printStackTrace();
				return;
			}
    	}
    }
    //END: Richard
    
    // Back to previous draw state
    // 1. If previous state is [Scale] or [Trans], it means we has not finish drawing
    // i.e the DrawInfo is not put into hist list
    // 2. Other DrawInfo means we has finished drawing
    // i.e the DrawInfo is put into hist list
    public void undo() {
        ActionRecord action = null;
        finishDrafting();

        action = mUndoList.pollLast();
        if (DEBUG_ACTION) {
            Log.d(TAG, "undo(), action = " + action);
        }

        if (action != null) {
            action.undo();
            // Put current Action into redo list
            mRedoList.add(action);
            redrawAll(false);
        }

        if (mUndoList.isEmpty()) {
            mPageEditor.setDoodleUndoEmpty(true);
        }

        if (!mRedoList.isEmpty()) {
            mPageEditor.setDoodleRedoEmpty(false);
        }
    }

    public boolean unGroup() {
        Collection<DrawInfo> groups = null;
        if (mDrawInfo instanceof SelectionDrawInfo) {
            groups = ((SelectionDrawInfo) mDrawInfo).unGroup();
            saveAction(ActionRecord.ACTION_UNGROUP, groups, null);
            drawScreen(true, mDrawInfo);
            mPageEditor.setDoodleItemSelect(true, ((SelectionDrawInfo) mDrawInfo).isMultiSelect(), ((SelectionDrawInfo) mDrawInfo).isGroup());

            selectFinish();
            drawScreen(false, null);
            selectLast((SelectionDrawInfo) mDrawInfo);
        }
        return groups != null;
    }
    //Begin: Allen
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if(e2.getPointerCount()==2)
		{
			mIsScrolling = true;
			mPageEditor.scrollEditText(Math.round(distanceX), Math.round(distanceY));
		}
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	//End: Allen

	//BEGIN: RICHARD
	public void setIsAutoRecognizerShape(boolean checked) {
		saveRecognizerShape();
		
		mIsNeedShape = checked;
		
		if(checked)
		{
				mCurrentShapeDrawTool = new PathDrawTool(DrawTool.NORMAL_TOOL);
				mCurrentShapePaint =  new Paint(mCurrentPaint);
				PaintSelector.reset(mCurrentShapePaint);
		}
	}
	
	public void clearShapeDocumentAction()
	{
        //Change order, Clear redolist first
        if (mRedoList != null && mRedoList.size()>0) {
        	Collection<ActionRecord> delCollection  = new LinkedList<ActionRecord>();  
            for(int index = 0; index < mRedoList.size() ; index++ )
            {
            	ActionRecord record = mRedoList.get(index);
            	if(record.clearShapeDocument())//RICHARD
            	{
            		delCollection.add(record);
            	}
            }
        	mRedoList.removeAll(delCollection);
        }
        
        if (mUndoList != null && mUndoList.size()>0) {
        	Collection<ActionRecord> delCollection  = new LinkedList<ActionRecord>();
            ListIterator<ActionRecord> it = mUndoList.listIterator(mUndoList.size());   
              
            while (it.hasPrevious()) {   
            	ActionRecord record = (ActionRecord) it.previous();   
            	if(record.clearShapeDocument())//RICHARD
            	{
            		delCollection.add(record);
            	}
            }   
            
            mUndoList.removeAll(delCollection);
        }
        mPageEditor.setDoodleUndoEmpty(mUndoList.isEmpty());
        mPageEditor.setDoodleRedoEmpty(mRedoList.isEmpty());
        //END: RICHARD
	}
	
	public void saveRecognizerShape()
	{
		if(mDrawInfoForShapeList == null || mDrawInfoForShapeList.size() == 0)
		{
			//nothing need to do.
			
			clearShapeDocumentAction();
			return;
		}
		
		// BEGIN: Better
		for(ArrayList<DrawInfo> subList :mDrawInfoForShapeList)
		{
			if (subList != null) {
				for (DrawInfo drawInfo : subList) {
					// BEGIN: Better
					int pageNum = addToPageOrderedDrawList(drawInfo);
					if (pageNum > mPageEditor.getPageNum()) {
						mPageEditor.setPageNum(pageNum);
						onNewPageAdded();
					}
					// END: Better
					mDrawList.addFirst(drawInfo);
					mObjectList.add(drawInfo);
					if(drawInfo instanceof AnnotationDrawInfo )
					{
						saveAction(ActionRecord.ACTION_SHAPE_AUTO_ADD, ((AnnotationDrawInfo) drawInfo).getDrawInfos(), null);
					}else
					{
						saveAction(ActionRecord.ACTION_ADD, drawInfo, null);
					}
				}
			}
		}
		// END: Better
		
		//BEGIN: RICHARD
		if(!MetaData.IS_ENABLE_CONTINUES_MODE)
		{
			refreshCurrentCache(CacheStatus.ONLY_UPDATE_SHAPE_TO_MAIN_BITMAP);//RICHARD
		}
		//END: RICHARD
		
		clearShapeDocumentAction();
		// BEGIN: Better
		deleteAllFromPageOrderedShapeDrawList();
		// END: Better
		
		if(mAsusShape != null)//darwin
    	{
			mAsusShape.clearStrokes();
    	}
	}
	//END: RICHARD
	//BEGIN: RICHARD TEST
	public boolean IsClickOnDrawItem(int x, int y)
	{
        for (DrawInfo drawInfo : mDrawList) {
            if (!(drawInfo instanceof EraseDrawInfo)) {
                if (drawInfo.isTouched(x, y)) {//RICHARD MODIFY 2013/1/7
                	mLongClickX = x;
                    mLongClickY = y;
                    mDrawInfo = null;
                	mPageEditor.setInputMode(InputManager.INPUT_METHOD_SELECTION_DOODLE);
                	if((mPageEditor.getTemplateType() == MetaData.Template_type_meeting 
                			||mPageEditor.getTemplateType() == MetaData.Template_type_todo 
                			||mPageEditor.getTemplateType() == MetaData.Template_type_travel)
                			&& mPageEditor.getEditorUiUtility().getInputMode() != InputManager.INPUT_METHOD_DOODLE){
                		redrawAll(true);
                	}
                	return true;
                }
            }
        }
        
        mLongClickX = -1;//reset
        mLongClickY = -1;//reset
        return false;
	}
	//END: RICHARD
	
	public void resetLastLongClick()
	{
        mLongClickX = -1;//reset
        mLongClickY = -1;//reset
	}
	
	private void setDoodleItemIsEmpty()
	{
        if (mDrawList.size() > 0 || (mDrawInfoForShapeList != null && mDrawInfoForShapeList.size() > 0)) { //RICHARD  //darwin
            mPageEditor.setDoodleItemEmpty(false);
        }else
        {
        	mPageEditor.setDoodleItemEmpty(true);
        }
	}
	
    //Begin: show_wang@asus.com
    //Modified reason: for dds
    public void saveSelectionDrawInfo() {
    	if ( mDrawInfo == null ) {
    		return;
    	}
    	if (mDrawInfo instanceof SelectionDrawInfo) {
    		mIsConfigSave = true;
    		finishDrafting();
    		mIsConfigSave = false;
    	}
    }
    
    private void selectDrawInfo(SelectionDrawInfo selectDrawInfo) {
        DrawInfo selectedInfo = null;
        boolean hasGroup = false;
        if ( mDrawList == null || mDrawList.size() < 1) {
        	return;
        }
        int size = MetaData.CurrentDrawList.size();
        if (size > 0 ) {
        	for (int i = size - 1; i >= 0; i--)  {
        		int index = MetaData.CurrentDrawList.get(i);
        		if (index >= mDrawList.size()) {// protect by jason
					continue;
				}
        		DrawInfo drawInfo = mDrawList.get(index);
        		selectedInfo = drawInfo;
        		if (drawInfo != null && drawInfo instanceof AnnotationDrawInfo) {
        			hasGroup = true;
        		}
        		if (selectedInfo != null) {
        			selectDrawInfo.addSelection(index, selectedInfo);
                    // BEGIN: Better
                    deleteFromPageOrderedDrawList(mDrawList.get(index));
                    // END: Better
                    mDrawList.remove(index);
		        }
        	}
        	
        	//emmanual to fix bug 485876
        	if(selectedInfo == null){
        		return ;
        	}
        	
        	RectF bounds;       
			int scrollX = 0;
			int scrollY = 0;
			bounds = selectedInfo.getBounds();
			scrollX = (int) (bounds.centerX() - (mViewWidth / 2));
			scrollY = (int) (bounds.centerY() - (mViewHeight / 2));

			relativeMotion(mScrollX, mScrollY);
            mPageEditor.ScrollViewTo(scrollX,scrollY,true);//Allen
            mPageEditor.setDoodleItemSelect(true, false, selectDrawInfo.isGroup());    
            //BEGIN:Show 
            if (size == 1) {
	            if(selectedInfo instanceof GraphicDrawInfo && !((GraphicDrawInfo)selectedInfo).isShapeGraphic())//update by jason
	            {
	          	  	mPageEditor.setDoodleCropButtonsEnable(true);   
	            }
	            
	            if(selectedInfo instanceof TextImgDrawInfo)
	            {
	          	  	mPageEditor.setDoodleTextEditButtonsEnable(true);  
	          	  	mEditBoxTextContent = ((TextImgDrawInfo) selectedInfo).getContent();
	            }
            }else {
            	if (hasGroup) {
            		mPageEditor.setDoodleUnGroupButtonsEnable(hasGroup);
            	}
            	mPageEditor.setDoodleGroupButtonsEnable(true);
            }
            //END: Show
            redrawAll(true);
        }
    	MetaData.CurrentDrawList.clear();
    }
    
    public void selectCurrentDrawInfo() {    	
    	mDrawInfo = (SelectionDrawInfo) (new SelectionDrawTool(getContext())).getDrawInfo(mCurrentPaint);
    	if (mPageEditor.getInputMode() == InputManager.INPUT_METHOD_INSERT) {
    		mCurrentDrawTool = mDrawInfo.getDrawTool();
    		mInsertMode = true;
    	}
   		selectDrawInfo((SelectionDrawInfo) mDrawInfo);
    }
    
    //End: show_wang@asus.com
	
	    //BEGIN: RICHARD
    public void setIsNeedDrawDoodleViewContent(boolean flag)
    {
    	mIsNeedDrawDoodleViewContent = flag;
    	if(flag)
    	{
            if (mSurfaceHolder != null) {
                redrawAll(true);
            }
    	}
    }
    //END: RICHARD
    // BEGIN: Shane_Wang@asus.com 2013-1-17
    public String reName(String suffix) {
    	return "SupperNotePic_" + System.currentTimeMillis() + suffix;
    }
    // END: Shane_Wang@asus.com 2013-1-17
    
    //Begin: show_wang@asus.com
    //Modifide reason: for eraser
    public void addDrawInfoToPageOrderedDrawList(DrawInfo drawInfo, int minPageNo, int maxPageNo) {
    	if ((drawInfo instanceof GraphicDrawInfo) || (drawInfo instanceof TextImgDrawInfo)) {
    		for (int i = minPageNo; i <= maxPageNo; i++) {
    			CopyOnWriteArrayList<DrawInfo> subList = null;
        		if (i < mPageOrderedGraphicDrawList.size()) {
        			subList = mPageOrderedGraphicDrawList.get(i);
        		} else {
        			for (int j = mPageOrderedGraphicDrawList.size(); j <= i; j++) {
        				mPageOrderedGraphicDrawList.add(null);
        			}
        		}
        		if (subList == null) {
        			subList = new CopyOnWriteArrayList<DrawInfo>();
        			mPageOrderedGraphicDrawList.set(i, subList);
        		}
	        	subList.add(drawInfo);
        	}
    	} else {
    		if (drawInfo instanceof AnnotationDrawInfo) {

    			for(DrawInfo detailInfo : ((AnnotationDrawInfo)drawInfo).getDrawInfos()){
    				addDrawInfoToPageOrderedDrawList(detailInfo, minPageNo, maxPageNo);
    			}
    			
    		} else {
            	for (int i = minPageNo; i <= maxPageNo; i++) {
            		CopyOnWriteArrayList<DrawInfo> subList = null;
            		if (i < mPageOrderedDrawList.size()) {
            			subList = mPageOrderedDrawList.get(i);
            		} else {
            			for (int j = mPageOrderedDrawList.size(); j <= i; j++) {
            				mPageOrderedDrawList.add(null);
            			}
            		}
            		if (subList == null) {
            			subList = new CopyOnWriteArrayList<DrawInfo>();
            			mPageOrderedDrawList.set(i, subList);
            		}
            		subList.add(drawInfo);
            	}
    		}
    	}
    }

    public void deleteDrawInfoFromPageOrderedDrawList(DrawInfo drawInfo, int minPageNo, int maxPageNo) {
		if ((drawInfo instanceof GraphicDrawInfo) || (drawInfo instanceof TextImgDrawInfo)) {
			for (int i = minPageNo; i <= maxPageNo; i++) {
				if (i < mPageOrderedGraphicDrawList.size()) {
					CopyOnWriteArrayList<DrawInfo> subList = mPageOrderedGraphicDrawList.get(i);
					if (subList != null) {
						for (DrawInfo info : subList) {
							if ((info != null) && info.equals(drawInfo)) {
								subList.remove(info);
							}
						}
					}
				}
			}
		} else {
			if (drawInfo instanceof AnnotationDrawInfo) {
    			for(DrawInfo detailInfo : ((AnnotationDrawInfo)drawInfo).getDrawInfos()){
    				deleteDrawInfoFromPageOrderedDrawList(detailInfo, minPageNo, maxPageNo);
    			}
    		}else {
    			for (int i = minPageNo; i <= maxPageNo; i++) {
    				if (i < mPageOrderedDrawList.size()) {
    					CopyOnWriteArrayList<DrawInfo> subList = mPageOrderedDrawList.get(i);
    					if (subList != null) {
    						for (DrawInfo info : subList) {
    							if ((info != null) && info.equals(drawInfo)) {
      								subList.remove(info);
    							}
    						}
    					}
    				}
    			}
    		}
		}
    }
    //End: show_wang@asus.com
    //begin jason
    @Override
    public boolean dispatchTouchEvent(MotionEvent arg0) {
    	if (MethodUtils.isPenDevice(mContext)&&mPageEditor!=null&&mPageEditor.getEditorUiUtility()!=null) { //smilefish
			if((mPageEditor.getEditorUiUtility().getmIsStylusInputOnly())){
				if (arg0.getToolType(arg0.getActionIndex())!=MotionEvent.TOOL_TYPE_STYLUS ) {
					return false;
				}
			}
		}
    	return super.dispatchTouchEvent(arg0);
    };
    public boolean hasAnyObjects(){
    	if(haveObjects()){
    		return true;
    	}
    	if (mIsNeedShape&&mAsusShape!=null&&mAsusShape.getResultShapeDocument()!=null) {
    		int count =mAsusShape.getResultShapeDocument().getStrokeCount();
			if (count>0) {
				return true;
			}
		}
    	return false;
    }
    public void clearScreen(){
    	if (mSurfaceHolder == null) {
            return;
        }
    	synchronized (mSurfaceHolder) {
    		long t=System.currentTimeMillis();
			Canvas canvas=mSurfaceHolder.lockCanvas();
			t=System.currentTimeMillis() - t;
			Log.v("TIME_P", ""+t);
			canvas.drawColor(Color.TRANSPARENT,android.graphics.PorterDuff.Mode.CLEAR);
			mSurfaceHolder.unlockCanvasAndPost(canvas);
			
		}
    }
    public void showRect(Rect rect,Bitmap bitmap){
    	if (mSurfaceHolder == null||bitmap==null||bitmap.isRecycled()||rect==null) {
            return;
        }
    	synchronized (mSurfaceHolder) {
			Canvas canvas=mSurfaceHolder.lockCanvas();
			canvas.drawColor(Color.TRANSPARENT,android.graphics.PorterDuff.Mode.CLEAR);
			canvas.save();
			canvas.clipRect(rect);
			canvas.drawBitmap(bitmap, new Matrix(), new Paint());
			canvas.restore();
			mSurfaceHolder.unlockCanvasAndPost(canvas);
		}
    }
    //end jason
    
	/**
	 * 鑾峰緱閫変腑DrawInfo鐨勫乏涓婅鍧愭爣鍜屽彸涓嬭鍧愭爣;濡傛灉褰撳墠娌℃湁閫変腑锛屽垯杩斿洖null
	 * 
	 * @author noah_zhang
	 * @return point[0],宸︿笂瑙掑乏杈�point[1]鍙充笅瑙掑潗鏍�
	 */
	public Point[] getSelectionDrawInfoCorner() {
		if (mDrawInfo instanceof SelectionDrawInfo) {
			SelectionDrawInfo drawInfo = (SelectionDrawInfo) mDrawInfo;
			Point[] points = drawInfo.getCorners();
			return new Point[] { points[TransformDrawInfo.CTR_LEFT_TOP],
					points[TransformDrawInfo.CTR_RIGHT_BOTTOM] };
		} else {
			return null;
		}
	}
	
	//add by mars get just input mode if doodle mode
	private boolean isNeedRefreshShapeBitmap(){
		if(mPageEditor == null)
			return false;
		return mPageEditor.getInputMode() == InputManager.INPUT_METHOD_DOODLE;
	}
}


