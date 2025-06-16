package com.asus.supernote;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.graphics.pdf.PdfRenderer.Page;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.ResultReceiver;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.asus.laterhandle.DoItLaterHelper;
import com.asus.supernote.classutils.ColorfulStatusActionBarHelper;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.AsusFormat;
import com.asus.supernote.data.AsusFormatWriter;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.BrushCollection;
import com.asus.supernote.data.BrushInfo;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.dialog.utils.DialogUtils;
import com.asus.supernote.doodle.DoodleItem;
import com.asus.supernote.doodle.DoodleView;
import com.asus.supernote.doodle.drawinfo.BrushDrawInfo;
import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.MarkerDrawInfo;
import com.asus.supernote.doodle.drawinfo.PathDrawInfo;
import com.asus.supernote.doodle.drawinfo.PenDrawInfo;
import com.asus.supernote.doodle.drawinfo.PencilDrawInfo;
import com.asus.supernote.doodle.drawtool.AirBrushDrawTool;
import com.asus.supernote.doodle.drawtool.BrushDrawTool;
import com.asus.supernote.doodle.drawtool.DrawTool;
import com.asus.supernote.doodle.drawtool.GraphicDrawTool;
import com.asus.supernote.doodle.drawtool.MarkerDrawTool;
import com.asus.supernote.doodle.drawtool.PathDrawTool;
import com.asus.supernote.doodle.drawtool.PenDrawTool;
import com.asus.supernote.doodle.drawtool.PencilDrawTool;
import com.asus.supernote.editable.BrushLibraryAdapter;
import com.asus.supernote.editable.ContiniousLineTipsActivity;
import com.asus.supernote.editable.EditorScrollBarContainer;
import com.asus.supernote.editable.NavigateTutorialActivity;
import com.asus.supernote.editable.NoteEditText;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.PageEditorManager;
import com.asus.supernote.editable.attacher.Attacher;
import com.asus.supernote.editable.attacher.CameraAttacher;
import com.asus.supernote.editable.attacher.GalleryAttacher;
import com.asus.supernote.editable.attacher.TextFileAttacher;
import com.asus.supernote.editable.attacher.VideoAttacher;
import com.asus.supernote.editable.attacher.VoiceAttacher;
import com.asus.supernote.fota.LoadPageTaskVersionTwo;
import com.asus.supernote.ga.GACollector;
import com.asus.supernote.indexservice.IndexService;
import com.asus.supernote.indexservice.IndexServiceClient;
import com.asus.supernote.inksearch.CFG;
import com.asus.supernote.picker.FavoriteActivity;
import com.asus.supernote.picker.NoteBookPickerActivity;
import com.asus.supernote.picker.PickerUtility;
import com.asus.supernote.share.utils.ShareUtils;
import com.asus.supernote.template.TemplateEditText;
import com.asus.supernote.template.TemplateImageView;
import com.asus.supernote.template.TemplateLinearLayout;
import com.asus.supernote.template.TemplateLinearLayout.IStylusButtonPress;
import com.asus.supernote.template.TemplateToDoUtility;
import com.asus.supernote.template.TemplateToDoUtility.onLoadingPageIsFullListener;
import com.asus.supernote.template.widget.ToDoWidgetService;
import com.asus.supernote.textsearch.TextSearchActivity;
import com.asus.supernote.transtion.PageWidget;
import com.asus.supernote.ui.ColorHelper;
import com.asus.supernote.ui.ColorPickerSnapView;
import com.asus.supernote.ui.ColorPickerViewCustom;
import com.asus.supernote.ui.CoverHelper;
import com.asus.supernote.ui.CursorIconLibrary;
import com.asus.supernote.uservoice.UserVoiceConfig;
import com.itextpdf.text.exceptions.BadPasswordException;
import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.PdfReader;

public class EditorActivity extends Activity {
	// BEGIN: Better
	public static final int SUPPORT_CLIPTYPE_NONE = 0x0;
	public static final int SUPPORT_CLIPTYPE_TEXT = 0x01;
	public static final int SUPPORT_CLIPTYPE_IMAGE = 0x02;
	public static final int SUPPORT_CLIPTYPE_FILE = 0x04;
	public static final int SUPPORT_CLIPTYPE_ALL = SUPPORT_CLIPTYPE_TEXT
	        | SUPPORT_CLIPTYPE_IMAGE | SUPPORT_CLIPTYPE_FILE;

	public static final String MIMETYPE_TEXT_PLAIN = "text/plain";
	public static final String MIMETYPE_TEXT_IMAGE = "text/image";
	public static final String MIMETYPE_TEXT_FILE = "text/file";
	
	public static final String[] IMPORT_PDF_FILTER = new String[] { "pdf" };

	private ClipboardManager mClipboardManager = null;
	// END: Better

	// This number should match the array's sequence.
	private static final int INSERT_MENU_NUM = 12;

	private static final String EXTRA_IS_SHARED = "extra_is_shared";// noah:其他ap分享过来的内容是否已经处理过

	private static final int INSERT_SHAPE = 11;
	private static final int INSERT_STAMP = 10;// Jason
	private static final int INSERT_FROM_CLIPBOARD = 9; // Better
	private static final int INSERT_TIMESTAMP = 8;
	private static final int INSERT_TEXT_FILE = 7;
	private static final int VIDEO_CAPTURE = 6;
	private static final int SOUND_RECORDER = 5;
	private static final int INSERT_AUDIO_FILE = 4; //Emmanual
	private static final int INSERT_PDF_PAGE = 3; // Better
	private static final int INSERT_TEXT_IMAGE = 2;
	public static final int INSERT_PICTURE = 1;
	public static final int TAKE_PHOTO = 0;

	private int[] mInsertMenuIndex = new int[INSERT_MENU_NUM];

	// BEGIN: archie_huang@asus.com
	private static final int MSG_INSERT_SPACE = 6;
	private static final int MSG_INSERT_BACK_SPACE = 7;
	private static final int MSG_INSERT_ENTER = 8;

	private static final int LONG_CLICK_DELAY_TIME = 100;
	// END: archie_huang@asus.com

	private static final String TAG = "EditorActivity";

	// BETTER
	private final int POINT_COUNT = 80; // 80
	private float mPoints[] = new float[POINT_COUNT * 2];
	private SeekBar SeekBar_Alpha = null;
	private TextView Alpha_Text = null;
	// better
	private static final int VIEW_BOOKMARK_REQUEST = 10;
	private static final int CROP_IMAGE_REQUEST = 11; // By Show
	private static final int RESULT_CHANGEGALLERY = 12;// By Show
	private static final int CROP_IMAGE_REQUEST_TEMPLATE = 13;// Allen++ for
	                                                          // template
	public static final int CONTACT_PICKER_RESULT = 14;// Allen++ for template
	public static final int CONTINIOUS_LINE_TIPS_REQUEST = 15; // Better
	public static final int REQUEST_IMPORT_PDF = 16; // Better
	public static final int VOICE_RECOGNITION_REQUEST_CODE = 17;// Allen
	public static final int NAVIGATOR_TUTORIAL_REQUEST = 19; //smilefish
	private static final float BUTTON_ALPHA_DISABLE = 0.3f;
	private static final float BUTTON_ALPHA_ENABLE = 1.0f;
	private static final int ACTIONMENU_ALPHA_ENABLE = 0xFF;
	private static final int ACTIONMENU_ALPHA_DISABLE = 0x4C;

	private static final int IMPORT_PDF_PROGRESS_DIALOG = 0; // Better
	public static final int LOADING_PROGRESS_DIALOG = 1; // Richard
	public static final int DRAWING_PROGRESS_DIALOG = 2; // Richard

	public static int PAD_PAGE_SHARE_BITMAP_DAFAULT_WIDTH = 1164;// darwin
	public static int PAD_PAGE_SHARE_BITMAP_DAFAULT_HEIGHT = 1593;// darwin
	public static int PHONE_PAGE_SHARE_BITMAP_DAFAULT_WIDTH = 540;// darwin
	public static int PHONE_PAGE_SHARE_BITMAP_DAFAULT_HEIGHT = 647;// darwin

	private boolean isGallaryPickerShowing = false;// Allen

	private boolean mIsResumeFromCropActivity = false;// darwin

	// BEGIN: james5
	// for wakelock acquire value, the value = 5 mins
	private static final long WAKELOCK_ACQUIRED_TIME = 5L * 60L * 1000L;
	private static final long WAKELOCK_COUNTDOWN_TIME = 1L * 60L * 1000L;
	// END: james5

	// Begin Allen
	private boolean mStartVoiceInput = false;// show voice input dialog
	// End Allen

	private int deviceType;

	// private Context mContext;
	private long mPageId = 0L;
	private long mBookId = 0L;
	// begin darwin
	static private long mPageIdOld = 0L;
	static private long mBookIdOld = 0L;
	// end darwin
	// BEGIN: Better
	private boolean mIsLoadingAsync = false;
	// END: Better
	private boolean mIsCreate = false; // by show
	private boolean mIsConfig = false; // by show
	private boolean mIsEraserOn = false; // by show
	private boolean mIsCropConfig = false; // by show
	private String cropFileName = null;// by show
	private String cropOrgFileName = null;// by show
	private static boolean mIsInsertGalleryAttacher = false; // by show
	private static boolean mIsInsertCameraAttacher = false; // by show
	private static boolean mIsInsertVoiceAttacher = false; // by show
	private static boolean mIsInsertVoiceFileAttacher = false;
	private static boolean mIsInsertVideoAttacher = false; // by show
	private static boolean mIsInsertTextFileAttacher = false; // by show
	private static boolean mIsChangeGraphicCropConfig = false; // by show
	private String changeGarphicCropFileName = null;// by show
	private Intent mIntent = null;// by show
	private BookCase mBookCase = null;
	private NoteBook mNoteBook = null;
	private NotePage mNotePage = null;

	private EditorUiUtility mEditorUiUtility = null;
	private EditorIdList mEditorIdList = null;
	private EditorPadIdList mEditorPadIdList = null;
	private EditorPhoneIdList mEditorPhoneIdList = null;
	private EditorPortraitPadIdList mEditorPortraitPadIdList = null;
	private EditorLandPhoneIdList mEditorLandPhoneIdList = null; // Carol
	private MenuItem mBookmarkMenuItem = null;
	// darwin
	private MenuItem mSaveMenuItem = null;
	private static final int SAVE_BUTTON_DEFAULT = -1;
	private static final int SAVE_BUTTON_DISABLE = 0;
	private static final int SAVE_BUTTON_ENABLE = 1;
	private static int mSaveButtonStatus = SAVE_BUTTON_DEFAULT; // -1 ---
	                                                            // default 0
	                                                            // ---- disable
	                                                            // 1 -----
	                                                            // enable
	// darwin
	private SharedPreferences mSharedPreference = null;
	private static Toast mActivityToast = null;
	// About View

	private View mKeyboardButtons = null;
	private View mScribbleButtons = null;
	private View mDoodleButtons = null;

	private View mKeyboardFuncs = null;
	private View mScribleFuncs = null;
	private View mDoodleFuncs = null;
	private View mSelectionTextFuncs = null;
	private View mReadOnlyFuncs = null;
	private View mInsertFuncs = null;
	private View mSelectionDoodle = null;
	private View mTextImgKeyboardFunc = null;
	private View mTextImgScribbleFunc = null;
	private View mMemoFuncs = null;

	// begin smilefish for color picker
	private View mColorPickerFuncs = null;
	private ViewStub mColorPickerViewStub = null;
	private Bitmap mSnapbitmap = null;
	private ColorPickerSnapView mColorPickerSnapView = null;
	private View mColorPickerHint = null;
	private View mColorChosenShow = null;
	private int mColorChosen = -1;
	private Button mColorPickerDoneButton = null;
	private boolean mIsColorChosen = false;
	private boolean mIsCustomColorSet = false;
	// end smilefish

	// END: show_wang@asus.com
	// BEGIN:shaun_xu@asus.com
	private ImageView mPenPreview = null;
	// END:shaun_xu@asus.com
	private PopupWindow mDoodleEraserPopupWindow = null;
	private PopupWindow mDoodleBrushPopupWindow = null;
	private PopupWindow mInsertPopupWindow = null;
	private PopupWindow mModePopupWindow = null;
	private PopupWindow mColorBoldPopupWindow = null;

	private Button mCurrentDoodleEraserButton = null;
	private Button mCurrentDoodleBrushButton = null;
	private TextView mCurrentPageNumberTextView = null;
	private TextView mTotalPageNumberTextView = null;
	private View mBottomMackView = null;
	private View mCurrentPopupParentView = null;

	private List<View> mPasteButtons = new ArrayList<View>();
	private List<View> mSelectButtons = new ArrayList<View>();
	private List<View> mDoodleUndoButtons = new ArrayList<View>();
	private List<View> mBoxUndoButtons = new ArrayList<View>();
	private List<View> mNoteUndoButtons = new ArrayList<View>();
	private List<View> mDoodleRedoButtons = new ArrayList<View>();
	private List<View> mBoxRedoButtons = new ArrayList<View>();
	private List<View> mNoteRedoButtons = new ArrayList<View>();
	private List<View> mUnGroupButtons = new ArrayList<View>();
	private List<View> mGroupButtons = new ArrayList<View>();
	private List<View> mDoodleSelectPastButtons = new ArrayList<View>();
	private List<View> mDoodleSelectCropButtons = new ArrayList<View>();// By
	                                                                    // Show
	private List<View> mDoodleSelectTextEditButtons = new ArrayList<View>();// By
	                                                                        // Show
	private List<View> mDoodleSelectChangeImgButtons = new ArrayList<View>();// By
	                                                                         // Show
	private List<View> mColorButtons = new ArrayList<View>();
	private List<View> mBoldButtons = new ArrayList<View>();
	private List<View> mDoodleFuncButtons = new ArrayList<View>();

	private List<View> mPopupColors = new ArrayList<View>();
	private List<View> mPopupBolds = new ArrayList<View>();
	private List<View> mPopupMode = new ArrayList<View>();
	private List<View> mPopupEraser = new ArrayList<View>();

	private Menu mOptionsMenu = null;
	private View mInsertView = null;
	// Share Dialog
	public long mShareBaseSize = 0;
	public Spinner mSpinner_preview_size = null;
	private CheckBox mShareCheckBoxTextOnly = null;
	private CheckBox mShareCheckBoxHideGrid = null;
	private ScrollView mTextInfoScrollView = null; // Carol
	private TextView mTextInfo = null; // Carol
	private static String mSharedText = ""; // Carol
	private static AlertDialog mShareToDialog = null;

	private static Bitmap mShareBitmap = null;
	private int mShareBitmapOriginalWidth = -1;
	private int mShareBitmapOriginalHeight = -1;

	private float mShareScale = 1.0f;

	private AlertDialog mDeletePageDialog = null;
	private int mNowColor = Color.BLACK;
	private boolean mIsNowBold = false;
	private boolean mIsTextImgEdit = false; // By Show

	// BEGIN: archie_huang@asus.com
	private boolean mEndLongClick = true;
	// END: archie_huang@asus.com

	// bold and color
	private float mStrokeWidth = MetaData.DOODLE_PAINT_WIDTHS[MetaData.DOODLE_DEFAULT_PAINT_WIDTH];
	private float mEraserWidth = MetaData.DOODLE_ERASER_WIDTHS[MetaData.DOODLE_DEFAULT_ERASER_WIDTHS];
	private int mDoodleToolCode = DrawTool.NORMAL_TOOL;
	private int mDoodleToolAlpha = 0x5F; // Better
	private boolean mIsPalette = false; // By Show
	private boolean mIsColorMode = false; // By Show
	private int mCustomColor = -1; // By Show
	private int mSelectedColorIndex = 0;
	private int[] mDefaultColorCodes = { 0xffffffff, // white
	        0xffb4b4b4, // gray
	        0xff5a5a5a, // dark gray
	        0xff000000, // black
	        0xffe70012, // red
	        0xffff9900, // orange
	        0xfffff100, // yellow
	        0xff8fc31f, // green
	        0xff009944, // lime green
	        0xff00a0e9, // blue
	        0xff1d2088, // dark blue
	        0xffe5007f // pink
	}; // smilefish

	// for orientation cursor
	private int mCursorPos = -1;
	private int mScrollBarX = -1; // smilefish
	private int mScrollBarY = -1; // smilefish

	// BEGIN: Better
	private AutoSaveCountdownCounter mAutoSaveTimer = null;
	private static final long AUTO_SAVE_INTERVAL = 10 * 60 * 1000;
	private boolean mIsAutoSaveNeeded = false;
	// END: Better

	// BEGIN: james5
	// for screen on
	private WakeLock mWakeLock;
	private WakeLockCountdownCounter mCountdownCounter;
	private Long mWakeLockCounter;
	// END: james5

	// BEGIN: RICHARD
	private ToggleButton mWTToggleButton = null; // is auto change hand write to
	                                             // type.
	private ToggleButton mRecognizerShapeButton = null;
	private Boolean isLastWTToggleButtonChecked = false;
	private Boolean isLastRecognizerShapeButtonChecked = false;
	private IndexServiceClient mIndexServiceClient = null;
	public static String mSearchString = "";
	private Boolean mFirstLoadData = true;
	private Intent mVideoData = null;
	private Intent mTextfileData = null;
	private boolean mPhotoData = false;
	private Intent mGalleryData = null;
	private String mTravelFileName = "";
	private Editable mAttendeeText = null;
	// END: RICHARD

	private int IsOrientationAllowOrNot = 0; // by show
	private BrushCollection mBrushCollection = null; // smilefish
	private BrushLibraryAdapter mBrushAdapter = null; // smilefish
	private PopupWindow mMenuPopupWindow = null; // smilefish
	private View mMenuEditorView = null;// smilefish
	private View mMenuReadonlyView = null;// smilefish
	private View mMenuBookmarkView = null;// smilefish
	private boolean mIsColorBoldPopupShown = false; //smilefish
	private boolean mIsAddNewBookEnable = true; //smilefish
	private boolean mIsKeyboardShown = false; //smilefish

	private Toast mAddPageToast;// noah

	private boolean mInsertStamp = false; // Carol
	private Dialog dlg = null;
	private boolean mIsShowingInsertDialog = false;

	//Emmanual
	private long mKeyboardTime, mScribbleTime, mDoodleTime, mRecordTime;
	private boolean mPauseForHome = true;
	
	// darwin
	@Override
	public void onBackPressed() {
		if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
			currentPopupWindow.dismiss();
			return;
		}

		// begin smilefish. hide hand write view when press back button
		if (mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_SCRIBBLE
		        && mEditorUiUtility.isHandWriteViewEnable()) {
			if (mEditorUiUtility.reponseBackKey()) {
				mEditorUiUtility.setHandWriteViewEnable(false);
				return;
			}
		}
		// end smilefish
		// begin darwin
		if (s_oldOrientaton != getResources().getConfiguration().orientation) {
			NoteBookPickerActivity
			        .setConfigurationChangeBy(NoteBookPickerActivity.CONFIGURATIONCHANGE_BY_EDTOR);

		}
		s_oldOrientaton = -1;
		// end darwin

		this.finish();
	}

	// darwin

	public EditorUiUtility getEditorUiUtility() {
		return mEditorUiUtility;
	}

	// BEGIN: RICHARD
	private void setAutoRecognizerShapeState(Boolean flag) {
		if (mRecognizerShapeButton == null) {
			return;
		}

		if (flag && !mIsEraserOn)// modified by show
		{
			mRecognizerShapeButton.setEnabled(true);
			((View) mRecognizerShapeButton).setAlpha(BUTTON_ALPHA_ENABLE);
			mEditorUiUtility.getPageEditor().setIsAutoRecognizerShape(
			        mRecognizerShapeButton.isChecked());
		} else {
			mEditorUiUtility.getPageEditor().setIsAutoRecognizerShape(false);

				mRecognizerShapeButton.setEnabled(false);
				((View) mRecognizerShapeButton).setAlpha(BUTTON_ALPHA_DISABLE);
		}
		isLastRecognizerShapeButtonChecked = mRecognizerShapeButton.isChecked();
		mRecognizerShapeButton.setSelected(isLastRecognizerShapeButtonChecked);
	}

	// END: RICHARD
	
	//emmanual
	private void recordInputMethodTime(){
		int currentInputMode = mEditorUiUtility.getInputMode();
		if (mRecordTime > 0) {		
			long lastPeriod = new Date().getTime() - mRecordTime;
			if(currentInputMode == InputManager.INPUT_METHOD_SCRIBBLE){
				mScribbleTime += lastPeriod;
			}else if(currentInputMode == InputManager.INPUT_METHOD_KEYBOARD){
				mKeyboardTime += lastPeriod;
			}else if(currentInputMode == InputManager.INPUT_METHOD_DOODLE){
				mDoodleTime += lastPeriod;
			}
        }
		mRecordTime = new Date().getTime();
	}

	// Listener
	private OnClickListener mFuncBarClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.note_kb_scribble:
				recordInputMethodTime();//emmanual
				mEditorUiUtility
				        .setInputMode(InputManager.INPUT_METHOD_SCRIBBLE);
				setModeSelected(v);
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector
					        .editorModeChange(InputManager.INPUT_METHOD_SCRIBBLE);
				}
				// ---
				break;
			case R.id.note_kb_keyboard:
				recordInputMethodTime();//emmanual
				mEditorUiUtility
				        .setInputMode(InputManager.INPUT_METHOD_KEYBOARD);
				setModeSelected(v);
				
				boolean isNavigate = mSharedPreference
						.getBoolean(getResources().getString(R.string.pref_prompt_navigate_tutorial), true);
				if (isNavigate) {
					try {
						Intent intent = new Intent();
						intent.setClass(EditorActivity.this, NavigateTutorialActivity.class);
						startActivityForResult(intent, NAVIGATOR_TUTORIAL_REQUEST);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					mEditorUiUtility.showSoftKeyboard();
				}
				
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector
					        .editorModeChange(InputManager.INPUT_METHOD_KEYBOARD);
				}
				// ---
				break;
			case R.id.note_kb_doodle:
				recordInputMethodTime();//emmanual
				mEditorUiUtility.setInputMode(InputManager.INPUT_METHOD_DOODLE);
				setModeSelected(v);
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector
					        .editorModeChange(InputManager.INPUT_METHOD_DOODLE);
				}
				// ---
				break;
			case R.id.note_kb_undo:
				mEditorUiUtility.undo();
				break;
			case R.id.note_kb_redo:
				mEditorUiUtility.redo();
				dismissAllPopupWindow();
				break;
			case R.id.note_kb_copy:
				mEditorUiUtility.copy(mNotePage);
				break;
			// BEGIN: RICHARD
			case R.id.note_kb_write_to_type:
				mEditorUiUtility.setIsAutoChangeWriteToType(mWTToggleButton
				        .isChecked());
				isLastWTToggleButtonChecked = mWTToggleButton.isChecked();
				mWTToggleButton.setSelected(isLastWTToggleButtonChecked);
				break;
			case R.id.note_kb_sel_write_to_type:
				mEditorUiUtility.changeHandWriteToType();
				break;
			case R.id.note_kb_d_shape:
				setAutoRecognizerShapeState(true);
				break;
			// END: RICHARD
			case R.id.note_kb_d_eraser:
				setAutoRecognizerShapeState(false);// RICHARD
				if (mEditorUiUtility.getDoodleTool() == DrawTool.ERASE_TOOL) { // Better
					for (View view : mPopupEraser) {
						if (view.isSelected()) {
							mEditorUiUtility
							        .changeScribleStroke(MetaData.DOODLE_ERASER_WIDTHS[mPopupEraser
							                .indexOf(view)]);
						}
					}
					mCurrentDoodleEraserButton = (Button) v;
					mDoodleEraserPopupWindow = preparePopupWindowImageView(v,
					        mDoodleEraserPopupWindow,
					        R.layout.editor_func_popup_d_eraser,
					        mEditorIdList.editorDoodleEraserIds,
					        mDoodleEraserImageClickListener);
					selectThisDoodleTool(v);
					selectCurrentEraserWidthOnPopup();
				} else { // Better
					mEditorUiUtility.setDoodleTool(DrawTool.ERASE_TOOL);
					mEditorUiUtility.changeScribleStroke(mEraserWidth);
					mCurrentDoodleEraserButton = (Button) v;
					selectThisDoodleTool(mCurrentDoodleEraserButton);
				}
				break;

			// BEGIN:shaun_xu@asus.com
			case R.id.note_kb_d_brush:
				mCurrentDoodleBrushButton = (Button) v;
				if (mEditorUiUtility.getDoodleTool() != DrawTool.ERASE_TOOL) { // Better
					openPenColorPopupWindow(v);// By Show
					selectCurrentBrushOnPopup();
					selectThisDoodleTool(mCurrentDoodleBrushButton);
				} else { // Better
					mEditorUiUtility.setDoodleTool(mDoodleToolCode);
					mEditorUiUtility.changeScribleStroke(mStrokeWidth);
					selectThisDoodleTool(mCurrentDoodleBrushButton);
				}
				setAutoRecognizerShapeState(true);// RICHARD
				break;
			// END:shaun_xu@asus.com
			case R.id.note_kb_done:
				mEditorUiUtility.quitSelectionTextMode();
				//emmanual to fix bug 547968
				mEditorUiUtility.getPageEditor().setEditTextModified();
				break;
			case R.id.note_kb_d_done:
				mEditorUiUtility.setInputMode(mEditorUiUtility
				        .getInputModeFromPreference());
				mEditorUiUtility.getInputManager().setHandWritePanelEnable(
				        false);
				if (!mIsEraserOn) {// by show
					mEditorUiUtility.setDoodleTool(mDoodleToolCode);
				}
				MetaData.INSERT_PHOTO_SELECTION = false; // Carol
				//emmanual to fix bug 547968
				mEditorUiUtility.getPageEditor().setDoodleModified();
				break;
			case R.id.note_kb_textimg_keyboard:
				//emmanual to fix bug 407909
				if(deviceType > 100 && isPhoneScreen()){
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
				}
        				
				mEditorUiUtility.toggleSoftKeyboard();
				mEditorUiUtility
				        .setInputMode(InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD);
				break;
			case R.id.note_kb_textimg_scribble:
				mEditorUiUtility
				        .setInputMode(InputManager.INPUT_METHOD_TEXT_IMG_SCRIBBLE);
				hiddenSoftKeyboard();
				break;
			case R.id.note_kb_textimg_done:

				// BEGIN:Show
				if (mIsTextImgEdit) {
					mEditorUiUtility.finishBoxEditText();
					mEditorUiUtility.cancleBoxEditor();
					mEditorUiUtility
					        .setInputMode(InputManager.INPUT_METHOD_SELECTION_DOODLE);
					mIsTextImgEdit = false;
					
					//smilefish to fix bug 605702,606558,607274,621533
					if(mEditorUiUtility.getPageEditor().getTemplateType() == MetaData.Template_type_todo){
						mEditorUiUtility.redrawDoodleView();
					}
				} else {
					// END:Show
					mEditorUiUtility.finishBoxEditText();
				}// By Show

				mEditorUiUtility
				        .getContext()
				        .getSharedPreferences(MetaData.PREFERENCE_NAME,
				                Activity.MODE_PRIVATE)
				        .edit()
				        .putBoolean(
				                MetaData.PREFERENCE_IS_TEXTIMG_SELECTION_TEXT,
				                false).commit(); // by show
				//emmanual to fix bug 547968
				mEditorUiUtility.getPageEditor().setDoodleModified();
				break;
			case R.id.note_kb_textimg_cancle:
				// BEGIN:Show
				if (mIsTextImgEdit) {
					mSelectionDoodle = prepareFuncButtons(mSelectionDoodle,
					        R.id.selectiondoodlefuncViewStub,
					        mEditorIdList.funcButtonIdsSelectionDoodle, -1);
					setBottomButtonGone();
					showEditorHint(getResources().getString(
					        R.string.editor_bottom_hint_selected_mode));
					setOptionsMentNone();
					mEditorUiUtility.setDoodleTool(DrawTool.SELECTION_TOOL);
					mEditorUiUtility.cancleBoxEditor();
					mIsTextImgEdit = false;
					mEditorUiUtility
					        .setInputMode(InputManager.INPUT_METHOD_SELECTION_DOODLE);
				} else {
					// END:Show
					mEditorUiUtility.setInputMode(mEditorUiUtility
					        .getInputModeFromPreference());
				}// By Show
				hiddenSoftKeyboard();
				mEditorUiUtility
				        .getContext()
				        .getSharedPreferences(MetaData.PREFERENCE_NAME,
				                Activity.MODE_PRIVATE)
				        .edit()
				        .putBoolean(
				                MetaData.PREFERENCE_IS_TEXTIMG_SELECTION_TEXT,
				                false).commit(); // by show
				break;
			case R.id.note_kb_color_done:// smilefish
				setColorFromSnapshot();
				mColorPickerViewStub.setVisibility(View.GONE);
				releaseSnapBitmapMemory();
				mEditorUiUtility.setInputMode(mEditorUiUtility
				        .getInputModeFromPreference());
				break;
			case R.id.note_kb_color_cancel:// smilefish
				if (mIsColorChosen) {
					mColorPickerDoneButton.setEnabled(false);
					mColorPickerHint.setVisibility(View.VISIBLE);
					mColorChosenShow.setVisibility(View.GONE);
					mIsColorChosen = false;
					mColorPickerSnapView.setColorXY(0, 0);
				} else {
					mColorPickerViewStub.setVisibility(View.GONE);
					releaseSnapBitmapMemory();
					mEditorUiUtility.setInputMode(mEditorUiUtility
					        .getInputModeFromPreference());
				}
				break;
			case R.id.note_kb_d_group:
				if (mEditorUiUtility.groupDoodleObject(true)) {
					setDoodleUnGroupButtonsEnable(true);
				}
				break;
			case R.id.note_kb_d_ungroup:
				if (mEditorUiUtility.groupDoodleObject(false)) {
					setDoodleUnGroupButtonsEnable(false);
				}
				break;
			case R.id.note_inputmode:
				mEditorUiUtility.getPageEditor().HasPopUpWindows = true; // Dave.
				                                                         // To
				                                                         // fix
				                                                         // the
				                                                         // bug:
				                                                         // keyboard
				                                                         // will
				                                                         // show
				                                                         // and
				                                                         // disappear
				                                                         // one
				                                                         // time
				                                                         // in
				                                                         // keyboard_input_mode.
				mModePopupWindow = preparePopupWindowImageView(v,
				        mModePopupWindow, R.layout.editor_func_popup_mode,
				        mEditorPhoneIdList.editorModeIds, this);
				break;

			case R.id.note_kb_color_bold:
				dismissEditPopupMenu();
				mIsColorBoldPopupShown = true; //smilefish
				mColorBoldPopupWindow = preparePopupWindowImageView(v,
				        mColorBoldPopupWindow,
				        R.layout.editor_func_popup_color_bold,
				        mEditorIdList.editorColorBoldIds,
				        mColorOrBoldImageClickListener);
				break;
			// BEGIN:Show
			case R.id.note_kb_d_crop: {
				String fileName = mEditorUiUtility.getFileInfo();
				String title = mNoteBook.getTitle();
				if (fileName != null) {
					try {
						Intent intent = new Intent();
						intent.putExtra("filePath",
						        mEditorUiUtility.getFilePath());

						intent.putExtra("fileName", fileName);
						intent.putExtra("title", title);
						intent.setClass(EditorActivity.this,
						        CropImageActivity.class);
						startActivityForResult(intent, CROP_IMAGE_REQUEST);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
				break;
			case R.id.note_kb_d_textedit:
				mIsTextImgEdit = true;
				mEditorUiUtility
				        .setInputMode(InputManager.INPUT_METHOD_TEXT_IMG_SCRIBBLE);
				break;
			// END:Show
			// begin smilefish
			case R.id.note_kb_s_more:
				showMenuPopupWindow(v);
				break;
			case R.id.note_kb_k_more:
				showMenuPopupWindow(v);
				break;
			case R.id.note_kb_d_more:
				showMenuPopupWindow(v);
				break;
			case R.id.note_kb_more:
				showMenuReadonlyPopupWindow(v);
				break;
			case R.id.note_kb_editabel:
				mEditorUiUtility.setInputMode(mEditorUiUtility
				        .getInputModeFromPreference());
				
				//emmanual to fix bug 506379
				if ((deviceType <= 100)
				        && !mSharedPreference.getBoolean(getResources().getString(R.string.pref_default_read_mode), false)
				        && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {
					boolean isPrompt = mSharedPreference
							.getBoolean(getResources().getString(R.string.pref_prompt_handwriting_animating), true);
					if (isPrompt) {
						getIntent().removeExtra(MetaData.IS_NEW_PAGE);

						try {
							Intent intent = new Intent();
							intent.setClass(EditorActivity.this,
							        ContiniousLineTipsActivity.class);
							startActivityForResult(intent,
							        CONTINIOUS_LINE_TIPS_REQUEST);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				
				break;
			// end smilefish
			}
		}
	};

	private OnClickListener mBottomFuncClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bottom_last:
				prevPage();
				break;
			case R.id.bottom_next:
				nextPage();
				break;
			case R.id.note_kb_insert:
				setAutoRecognizerShapeState(true);// RICHARD
				preparePopupWindowInsert(mInsertView);
				break;
			case R.id.note_kb_add:
				addNewPage(true);
				break;
			}
		}
	};

	// BEGIN: archie_huang@asus.com
	private OnTouchListener mBottomFuncTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				endLongClick();
			}
			return false;
		}
	}; // END: archie_huang@asus.com

	// BEGIN: archie_huang@asus.com
	private Handler mBottomFuncController = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_INSERT_SPACE:
				mEditorUiUtility.insertSpace();
				break;
			case MSG_INSERT_BACK_SPACE:
				mEditorUiUtility.insertBackSpace();
				break;
			case MSG_INSERT_ENTER:
				mEditorUiUtility.insertEnter();
				break;
			default:
				endLongClick();
			}
			if (!mEndLongClick) {
				sendEmptyMessageDelayed(msg.what, LONG_CLICK_DELAY_TIME);
			}
		}

	}; // END: archie_huang@asus.com

	// BEGIN: archie_huang@asus.com
	private void endLongClick() {
		mEndLongClick = true;
		mBottomFuncController.removeMessages(MSG_INSERT_SPACE);
		mBottomFuncController.removeMessages(MSG_INSERT_ENTER);
		mBottomFuncController.removeMessages(MSG_INSERT_BACK_SPACE);
	} // END: archie_huang@asus.com

	private OnClickListener mColorOrBoldImageClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.editor_func_color_green:
				mEditorUiUtility.changeColor(Color.GREEN);
				break;
			case R.id.editor_func_color_red:
				mEditorUiUtility.changeColor(Color.RED);
				break;
			case R.id.editor_func_color_blue:
				mEditorUiUtility.changeColor(Color.BLUE);
				break;
			case R.id.editor_func_color_black:
				mEditorUiUtility.changeColor(Color.BLACK);
				break;
			case R.id.editor_func_bold_true:
				mEditorUiUtility.changeTextStyle(Typeface.BOLD);
				break;
			case R.id.editor_func_bold_false:
				mEditorUiUtility.changeTextStyle(Typeface.NORMAL);
				break;
			}
			if (mColorBoldPopupWindow != null) {
				mColorBoldPopupWindow.dismiss();
			}
		}

	};

	private OnClickListener mDoodleEraserImageClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mEditorUiUtility.setDoodleTool(DrawTool.ERASE_TOOL);
			switch (v.getId()) {
			case R.id.editor_func_eraser_3:
				// begin smilefish
				Drawable drawableTop = getResources().getDrawable(
				        R.drawable.asus_supernote_function_eraser1);
				mCurrentDoodleEraserButton
				        .setCompoundDrawablesWithIntrinsicBounds(null,
				                drawableTop, null, null);
				// end smilefish
				mEraserWidth = MetaData.DOODLE_ERASER_WIDTHS[0];
				setEraserSelected(v);
				break;
			case R.id.editor_func_eraser_2:
				// begin smilefish
				drawableTop = getResources().getDrawable(
				        R.drawable.asus_supernote_function_eraser2);
				mCurrentDoodleEraserButton
				        .setCompoundDrawablesWithIntrinsicBounds(null,
				                drawableTop, null, null);
				// end smilefish
				mEraserWidth = MetaData.DOODLE_ERASER_WIDTHS[1];
				setEraserSelected(v);
				break;
			case R.id.editor_func_eraser_1:
				// begin smilefish
				drawableTop = getResources().getDrawable(
				        R.drawable.asus_supernote_function_eraser3);
				mCurrentDoodleEraserButton
				        .setCompoundDrawablesWithIntrinsicBounds(null,
				                drawableTop, null, null);
				// end smilefish
				mEraserWidth = MetaData.DOODLE_ERASER_WIDTHS[2];
				setEraserSelected(v);
				break;
			case R.id.clear_all:
				mEditorUiUtility.clearAll();
				mEditorUiUtility.setDoodleTool(mDoodleToolCode);
				mEditorUiUtility.changeScribleStroke(mStrokeWidth);
				selectThisDoodleTool(mCurrentDoodleBrushButton);
				setAutoRecognizerShapeState(true);// RICHARD//darwin modify to
				                                  // fix A68 Bug 246603
				break;
			}
			if (v.getId() != R.id.clear_all) {
				mEditorUiUtility.changeScribleStroke(mEraserWidth);
			}
			if (mDoodleEraserPopupWindow != null) {
				mDoodleEraserPopupWindow.dismiss();
			}
		}
	};

	// BEGIN:shaun_xu@asus.com
	private OnClickListener mDoodleUnityClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// begin smilefish
			ImageButton selectBrush = null;
			TextView selectBrushName = null;

			selectBrush = (ImageButton) mDoodleBrushPopupWindow
			        .getContentView().findViewById(R.id.select_brush);
			selectBrushName = (TextView) mDoodleBrushPopupWindow
			        .getContentView().findViewById(R.id.select_brush_name);

			int color = -1, Colorid = -1;
			boolean isColorChanged = false; // smilefish
			switch (v.getId()) {
			case R.id.editor_func_d_brush_normal:
				mDoodleToolCode = DrawTool.NORMAL_TOOL; // roller

				selectBrush.setImageResource(R.drawable.asus_popup_pen3);
				selectBrushName.setText(R.string.brush_edit_dialog_rollerpen);

				// --- Carrot: temporary add for limitation of brush width ---
				SetCurAttr(mDoodleToolCode);
				UpdatePopupByCurBrush();
				// --- Carrot: temporary add for limitation of brush width ---
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleBrushSet(mCurAttrIndex,
					        (int) mStrokeWidth);
				}
				// ---
				break;
			case R.id.editor_func_d_brush_scribble:
				mDoodleToolCode = DrawTool.SCRIBBLE_TOOL; // pen

				selectBrush.setImageResource(R.drawable.asus_popup_pen2);
				selectBrushName.setText(R.string.brush_edit_dialog_pen);

				// --- Carrot: temporary add for limitation of brush width ---
				SetCurAttr(mDoodleToolCode);
				UpdatePopupByCurBrush();
				// --- Carrot: temporary add for limitation of brush width ---
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleBrushSet(mCurAttrIndex,
					        (int) mStrokeWidth);
				}
				// ---
				break;
			case R.id.editor_func_d_brush_mark:
				mDoodleToolCode = DrawTool.NEON_TOOL; // air brush

				selectBrush.setImageResource(R.drawable.asus_popup_pen6);
				selectBrushName.setText(R.string.brush_edit_dialog_airbrush);

				// --- Carrot: temporary add for limitation of brush width ---
				SetCurAttr(mDoodleToolCode);
				UpdatePopupByCurBrush();
				// --- Carrot: temporary add for limitation of brush width ---
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleBrushSet(mCurAttrIndex,
					        (int) mStrokeWidth);
				}
				// ---
				break;
			case R.id.editor_func_d_brush_sketch:
				mDoodleToolCode = DrawTool.SKETCH_TOOL; // pencil

				selectBrush.setImageResource(R.drawable.asus_popup_pen1);
				selectBrushName.setText(R.string.brush_edit_dialog_pencil);

				// --- Carrot: temporary add for limitation of brush width ---
				SetCurAttr(mDoodleToolCode);
				UpdatePopupByCurBrush();
				// --- Carrot: temporary add for limitation of brush width ---
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleBrushSet(mCurAttrIndex,
					        (int) mStrokeWidth);
				}
				// ---
				break;
			// begin wendy
			case R.id.editor_func_d_brush_markpen:
				mDoodleToolCode = DrawTool.MARKPEN_TOOL; // marker

				selectBrush.setImageResource(R.drawable.asus_popup_pen4);
				selectBrushName.setText(R.string.brush_edit_dialog_marker);

				// --- Carrot: temporary add for limitation of brush width ---
				SetCurAttr(mDoodleToolCode);
				UpdatePopupByCurBrush();
				// --- Carrot: temporary add for limitation of brush width ---
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleBrushSet(mCurAttrIndex,
					        (int) mStrokeWidth);
				}
				// ---
				break;
			case R.id.editor_func_d_brush_writingbrush:
				mDoodleToolCode = DrawTool.WRITINGBRUSH_TOOL; // brush

				selectBrush.setImageResource(R.drawable.asus_popup_pen5);
				selectBrushName.setText(R.string.brush_edit_dialog_brush);

				// --- Carrot: temporary add for limitation of brush width ---
				SetCurAttr(mDoodleToolCode);
				UpdatePopupByCurBrush();
				// --- Carrot: temporary add for limitation of brush width ---
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleBrushSet(mCurAttrIndex,
					        (int) mStrokeWidth);
				}
				// ---
				break;
			// end wendy
			// Modify by show, begin
			case R.id.editor_func_color_A:
				color = mDefaultColorCodes[0];
				mSelectedColorIndex = 0;
				mIsPalette = false;
				isColorChanged = true; // smilefish
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleColorSet(mCurAttrIndex,
					        String.valueOf(color));
				}
				// ---
				break;
			case R.id.editor_func_color_B:
				color = mDefaultColorCodes[1];
				mSelectedColorIndex = 1;
				mIsPalette = false;
				isColorChanged = true; // smilefish
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleColorSet(mCurAttrIndex,
					        String.valueOf(color));
				}
				// ---
				break;
			case R.id.editor_func_color_C:
				color = mDefaultColorCodes[2];
				mSelectedColorIndex = 2;
				mIsPalette = false;
				isColorChanged = true; // smilefish
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleColorSet(mCurAttrIndex,
					        String.valueOf(color));
				}
				// ---
				break;
			case R.id.editor_func_color_D:
				color = mDefaultColorCodes[3];
				mSelectedColorIndex = 3;
				mIsPalette = false;
				isColorChanged = true; // smilefish
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleColorSet(mCurAttrIndex,
					        String.valueOf(color));
				}
				// ---
				break;
			case R.id.editor_func_color_E:
				color = mDefaultColorCodes[4];
				mSelectedColorIndex = 4;
				mIsPalette = false;
				isColorChanged = true; // smilefish
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleColorSet(mCurAttrIndex,
					        String.valueOf(color));
				}
				// ---
				break;
			case R.id.editor_func_color_F:
				color = mDefaultColorCodes[5];
				mSelectedColorIndex = 5;
				mIsPalette = false;
				isColorChanged = true; // smilefish
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleColorSet(mCurAttrIndex,
					        String.valueOf(color));
				}
				// ---
				break;
			case R.id.editor_func_color_G:
				color = mDefaultColorCodes[6];
				mSelectedColorIndex = 6;
				mIsPalette = false;
				isColorChanged = true; // smilefish
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleColorSet(mCurAttrIndex,
					        String.valueOf(color));
				}
				// ---
				break;
			case R.id.editor_func_color_H:
				color = mDefaultColorCodes[7];
				mSelectedColorIndex = 7;
				mIsPalette = false;
				isColorChanged = true; // smilefish
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleColorSet(mCurAttrIndex,
					        String.valueOf(color));
				}
				// ---
				break;
			case R.id.editor_func_color_I:
				color = mDefaultColorCodes[8];
				mSelectedColorIndex = 8;
				mIsPalette = false;
				isColorChanged = true; // smilefish
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleColorSet(mCurAttrIndex,
					        String.valueOf(color));
				}
				// ---
				break;
			case R.id.editor_func_color_J:
				color = mDefaultColorCodes[9];
				mSelectedColorIndex = 9;
				mIsPalette = false;
				isColorChanged = true; // smilefish
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleColorSet(mCurAttrIndex,
					        String.valueOf(color));
				}
				// ---
				break;
			case R.id.editor_func_color_K:
				color = mDefaultColorCodes[10];
				mSelectedColorIndex = 10;
				mIsPalette = false;
				isColorChanged = true; // smilefish
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleColorSet(mCurAttrIndex,
					        String.valueOf(color));
				}
				// ---
				break;
			case R.id.editor_func_color_M: // smilefish
				color = mDefaultColorCodes[11];
				mSelectedColorIndex = 11;
				mIsPalette = false;
				isColorChanged = true; // smilefish
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleColorSet(mCurAttrIndex,
					        String.valueOf(color));
				}
				// ---
				break;
			case R.id.editor_func_color_L:
				if (!mIsCustomColorSet) // smilefish
				{
					color = -1;
					mIsCustomColorSet = true;
					((ImageView) v).setBackgroundColor(-1);
				} else {
					color = mCustomColor;
				}

				// Carrot: individually set color of every brush
				mSelectedColorIndex = COLOR_PALETTE_INDEX;
				// Carrot: individually set color of every brush
				mIsPalette = true;
				isColorChanged = true; // smilefish
				// +++ Dave GA
				if (MetaData.IS_GA_ON) {
					GACollector gaCollector = new GACollector(
					        mEditorUiUtility.getContext());
					gaCollector.doodleColorSet(mCurAttrIndex,
					        String.valueOf(color));
				}
				// ---
				break;
			}
			// Modify by show, end

			if (isColorChanged) // smilefish
			{
				mEditorUiUtility.changeColor(color);
				// Carrot: individually set color of every brush
				attrs.get(mCurAttrIndex).ColorInfo.Index = mSelectedColorIndex;
				attrs.get(mCurAttrIndex).ColorInfo.Color = color;
				// Carrot: individually set color of every brush
			}

			if (mDoodleBrushPopupWindow == null
			        || !mDoodleBrushPopupWindow.isShowing()) {
				return;
			}
			for (int id : mEditorIdList.editorDoodleUnityColorIds) {
				View vv = mDoodleBrushPopupWindow.getContentView()
				        .findViewById(id);
				if (vv != null) // smilefish
				{
					vv.setSelected(false);
					((ImageView) vv)
					        .setImageResource(R.drawable.asus_color_frame_n);

					Colorid = getColorId();

					if (vv.getId() == Colorid) {
						vv.setSelected(true);
						((ImageView) vv)
						        .setImageResource(R.drawable.asus_color_frame_p);
					}
				}
			}
			// BEGIN: show_wang@asus.com
			// Modified reason: penmenu
			mCurrentDoodleBrushButton.setCompoundDrawablesWithIntrinsicBounds(
			        null, drawCurrentBrushThickness(drawCurrentBrushType()),
			        null, null);

			// END: show_wang@asus.com
			selectThisDoodleTool(mCurrentDoodleBrushButton);
			mEditorUiUtility.setDoodleTool(mDoodleToolCode);
			selectCurrentBrushOnPopup();
			if (mDoodleToolCode != DrawTool.MARKPEN_TOOL) {
				if ((SeekBar_Alpha != null) && (SeekBar_Alpha.isEnabled())) {
					setSeekBarAlphaEnable(false);
				}
				if ((Alpha_Text != null) && (Alpha_Text.isEnabled())) {
					setAlphaTextEnable(false);
				}
			} else {
				if ((SeekBar_Alpha != null) && (!SeekBar_Alpha.isEnabled())) {
					float AlphaPrecent = mDoodleToolAlpha / 255.0f;
					int AlphaProgress = Math.round(AlphaPrecent * 100);
					SeekBar_Alpha.setProgress(AlphaProgress);
					setSeekBarAlphaEnable(true);
				}
				if ((Alpha_Text != null) && (!Alpha_Text.isEnabled())) {
					setAlphaTextEnable(true);
				}
			}

			if (mPenPreview != null)
				DrawPreview(mPenPreview);

			// BEGIN: RICHARD
			setAutoRecognizerShapeState(true);
		}
	};

	// END:shaun_xu@asus.com
	// BEGIN: Show
	public int getColorId() {
		int Colorid = -1;
		if (mIsPalette) {
			Colorid = R.id.editor_func_color_L;
		} else {
			int color = mEditorUiUtility.getDoodlePaint().getColor();
			switch (color & 0x00FFFFFF) {
			case 0x00ffffff:
				Colorid = R.id.editor_func_color_A;
				break;
			case 0x00b4b4b4:
				Colorid = R.id.editor_func_color_B;
				break;
			case 0x005a5a5a:
				Colorid = R.id.editor_func_color_C;
				break;
			case 0x00000000:
				Colorid = R.id.editor_func_color_D;
				break;
			case 0x00e70012:
				Colorid = R.id.editor_func_color_E;
				break;
			case 0x00ff9900:
				Colorid = R.id.editor_func_color_F;
				break;
			case 0x00fff100:
				Colorid = R.id.editor_func_color_G;
				break;
			case 0x008fc31f:
				Colorid = R.id.editor_func_color_H;
				break;
			case 0x00009944:
				Colorid = R.id.editor_func_color_I;
				break;
			case 0x0000a0e9:
				Colorid = R.id.editor_func_color_J;
				break;
			case 0x001d2088:
				Colorid = R.id.editor_func_color_K;
				break;
			case 0x00e5007f:
				Colorid = R.id.editor_func_color_M; // smilefish
				break;
			}
		}
		return Colorid;
	}

	// END: Show

	// BEGIN:shaun_xu@asus.com
	private void DrawPreview(ImageView v) {
		Paint DoodlePaint = mEditorUiUtility.getDoodlePaint();
		float width = getResources().getDimension(R.dimen.preview_image_width);
		float height = getResources()
		        .getDimension(R.dimen.preview_image_height);
		Bitmap PreviewBitmap = Bitmap.createBitmap((int) width, (int) height,
		        Config.ARGB_8888);
		Canvas PreviewCanvas = new Canvas(PreviewBitmap);

		draw(PreviewCanvas, DoodlePaint, mDoodleToolCode);

		v.setImageBitmap(PreviewBitmap);
	}

	// END:shaun_xu@asus.com

	private void genPath(int width, int height, int marginWidth,
	        int marginHeight) {
		int index = 0;
		double sinmax = Math.PI * 2.0;

		double sinx = 0.0;
		double siny = 0.0;
		while ((sinx < sinmax) && (index < POINT_COUNT * 2 - 1)) {
			siny = Math.sin(sinx);

			if (siny > 1) {
				siny = 1;
			}

			if (siny < -1) {
				siny = -1;
			}

			float x = (float) (width / sinmax * sinx);
			float y = (float) (height * (siny + 1) / 2);
			mPoints[index++] = (x + marginWidth);
			mPoints[index++] = (y + marginHeight);
			sinx += sinmax / POINT_COUNT;
		}

	}

	public void draw(Canvas canvas, Paint paint, int toolCode) {
		DrawInfo info = null;
		Paint prePaint = new Paint(paint);
		prePaint.setStrokeWidth(paint.getStrokeWidth()/* * 0.65f */);
		switch (toolCode) {
		case DrawTool.NORMAL_TOOL:
			info = new PathDrawInfo(new PathDrawTool(PathDrawTool.NORMAL_TOOL),
			        prePaint, mPoints);
			break;
		case DrawTool.NEON_TOOL:
			info = new PathDrawInfo(new AirBrushDrawTool(), prePaint, mPoints);
			break;
		// end noah
		case DrawTool.SCRIBBLE_TOOL:

			float[] pointsScribble = new float[POINT_COUNT * 3];
			int j1 = 0;
			int i1 = 0;
			while (i1 < POINT_COUNT * 2 - 1) {
				pointsScribble[j1++] = mPoints[i1++];
				pointsScribble[j1++] = mPoints[i1++];
				pointsScribble[j1++] = (1.0f * MetaData.PRESSURE_FACTOR);
			}
			info = new PenDrawInfo(new PenDrawTool(DrawTool.SCRIBBLE_TOOL),
			        prePaint, pointsScribble);
			((PenDrawInfo) info).SetTexture(DoodleView.getPenTexure());
			break;
		case DrawTool.SKETCH_TOOL:
			info = new PencilDrawInfo(new PencilDrawTool(), prePaint, mPoints);
			((PencilDrawInfo) info).SetTexture(DoodleView.getPencilTexure());
			break;
		case DrawTool.MARKPEN_TOOL:
			info = new MarkerDrawInfo(
			        new MarkerDrawTool(DrawTool.MARKPEN_TOOL), prePaint,
			        mPoints);
			break;
		case DrawTool.WRITINGBRUSH_TOOL:
			float[] pointsWritingbrush = new float[POINT_COUNT * 3];
			int j2 = 0;
			int i2 = 0;
			while (i2 < POINT_COUNT * 2 - 1) {
				pointsWritingbrush[j2++] = mPoints[i2++];
				pointsWritingbrush[j2++] = mPoints[i2++];
				pointsWritingbrush[j2++] = (1.0f * MetaData.PRESSURE_FACTOR);
			}

			info = new BrushDrawInfo(new BrushDrawTool(
			        DrawTool.WRITINGBRUSH_TOOL), prePaint, pointsWritingbrush);
			((BrushDrawInfo) info).SetTexture(DoodleView.getBrushTexure());
			break;
		}
		if (info != null) {
			if (toolCode == DrawTool.WRITINGBRUSH_TOOL
			        || toolCode == DrawTool.MARKPEN_TOOL
			        || toolCode == DrawTool.SCRIBBLE_TOOL) {
				info.getDrawTool().drawPreview(canvas, info);
			} else {
				info.getDrawTool().draw(canvas, false, info);// Richard null ->
				                                             // false
				info.resetDirty();
			}
		}
	}

	// END: Better

	private OnItemClickListener mInsertListItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
		        long id) {
			if(EditorActivity.this == null || EditorActivity.this.isFinishing()){//TT 536548
				return ;
			}
			String[] countries = getResources().getStringArray(
			        R.array.editor_func_insert_array);
			// emmanual
			ArrayList<String> templist = new ArrayList<String>();
			for (String c : countries) {
				templist.add(c);
			}
			templist.add(INSERT_AUDIO_FILE,
			        getResources().getString(R.string.insert_audiofile));
			countries = templist.toArray(countries);

			int GAIndex = 0; // Dave for GA.
			if (id == mInsertMenuIndex[TAKE_PHOTO]) {
				mPauseForHome = false; //Emmanual
				GAIndex = TAKE_PHOTO; // Dave for GA
				PickerUtility.lockRotation(EditorActivity.this);
				startActivityForResult(Intent.createChooser(CameraAttacher
				        .getIntent(mEditorUiUtility.getPageEditorManager()),
				        countries[TAKE_PHOTO]), InputManager.RESULT_CAMERA);
				MetaData.INSERT_PHOTO_SELECTION = true; // Carol
			} else if (id == mInsertMenuIndex[INSERT_PICTURE]) {
				mPauseForHome = false; //Emmanual
				GAIndex = INSERT_PICTURE; // Dave for GA
				PickerUtility.lockRotation(EditorActivity.this);
				startActivityForResult(Intent.createChooser(
				        GalleryAttacher.getIntentGallery(),
				        countries[INSERT_PICTURE]), InputManager.RESULT_GALLERY);
				MetaData.INSERT_PHOTO_SELECTION = true; // Carol
			} else if (id == mInsertMenuIndex[INSERT_TEXT_IMAGE]) {
				mPauseForHome = false; //Emmanual
				GAIndex = INSERT_TEXT_IMAGE; // Dave for GA
				mIsTextImgEdit = false; // By Show
				mEditorUiUtility
				        .setInputMode(InputManager.INPUT_METHOD_TEXT_IMG_SCRIBBLE);// Modified
				                                                                   // By
				                                                                   // Show
				MetaData.INSERT_PHOTO_SELECTION = true; // Carol
			} else if (id == mInsertMenuIndex[INSERT_PDF_PAGE]) { // Better
				mPauseForHome = false; //Emmanual
				GAIndex = INSERT_PDF_PAGE; // Dave for GA
				try {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("application/pdf");
					intent.putExtra("ext", IMPORT_PDF_FILTER);
					intent.putExtra("mime_filter", false);
					intent.putExtra("path", mSharedPreference.getString(
							getResources().getString(R.string.pref_export_dir),
							MetaData.EXPORT_DIR));
					startActivityForResult(Intent.createChooser(intent, ""), REQUEST_IMPORT_PDF);
				} catch (ActivityNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					try {
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.addCategory("android.intent.category.DEFAULT");
						intent.setType("application/pdf");

						intent.putExtra("ext", IMPORT_PDF_FILTER);
						intent.putExtra("mime_filter", false);
						intent.putExtra("path", mSharedPreference.getString(
						        getResources().getString(
						                R.string.pref_export_dir),
						        MetaData.EXPORT_DIR));

						startActivityForResult(intent, REQUEST_IMPORT_PDF);
					} catch (ActivityNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						Toast.makeText(EditorActivity.this,R.string.import_export_book_fail_reason, Toast.LENGTH_SHORT).show();
					}
				} 
			} else if (id == mInsertMenuIndex[SOUND_RECORDER]) {
//				View parentView = EditorActivity.this.findViewById(R.id.note_kb_insert);
//				mEditorUiUtility.insertRecorder(parentView); //smilefish
				
				mPauseForHome = false; //Emmanual
				GAIndex = SOUND_RECORDER; // Dave for GA
				PickerUtility.lockRotation(EditorActivity.this);
				// startActivityForResult(Intent.createChooser(VoiceAttacher.getIntent(mEditorUiUtility.getPageEditorManager()),
				// countries[SOUND_RECORDER]), InputManager.RESULT_VOICE);
				// add by mars for 3gpp format
				
				//by emmanual, add try-catch
				try{
					startActivityForResult(VoiceAttacher.getIntent(mEditorUiUtility
				        .getPageEditorManager()), InputManager.RESULT_VOICE);
				}catch(Exception ex){
					
				}
			} else if (id == mInsertMenuIndex[INSERT_AUDIO_FILE]) {//Emmanual
				mPauseForHome = false; //Emmanual
				PickerUtility.lockRotation(EditorActivity.this);
				//emmanual to fix bug 452120
				String[] FILE_FILTER = new String[] { "audio/mpeg", "audio/midi",
				        "audio/x-wav", "audio/x-ms-wma", "audio/3gpp", "video/3gpp",
				        "audio/amr", "application/ogg" };
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.setType("audio/*");
				intent.putExtra("path", Environment.getExternalStorageDirectory().toString());
				intent.putExtra("mime", FILE_FILTER);
				startActivityForResult(Intent.createChooser(intent,
				        countries[INSERT_AUDIO_FILE]),
				        InputManager.RESULT_VOICEFILE);
			} else if (id == mInsertMenuIndex[VIDEO_CAPTURE]) {
				mPauseForHome = false; //Emmanual
				GAIndex = VIDEO_CAPTURE; // Dave for GA
				PickerUtility.lockRotation(EditorActivity.this);
				startActivityForResult(Intent.createChooser(VideoAttacher
				        .getIntent(mEditorUiUtility.getPageEditorManager()),
				        countries[VIDEO_CAPTURE]), InputManager.RESULT_VIDEO);
			} else if (id == mInsertMenuIndex[INSERT_TEXT_FILE]) {
				mPauseForHome = false; //Emmanual
				GAIndex = INSERT_TEXT_FILE; // Dave for GA
				PickerUtility.lockRotation(EditorActivity.this);
				startActivityForResult(Intent.createChooser(
				        TextFileAttacher.getIntentTextFile(),
				        countries[INSERT_TEXT_FILE]), InputManager.RESULT_TEXT);
			} else if (id == mInsertMenuIndex[INSERT_TIMESTAMP]) {
				GAIndex = INSERT_TIMESTAMP; // Dave for GA
				Log.v("wendy", "insert timestamp !");
				mEditorUiUtility.insertTimestamp(System.currentTimeMillis(),
				        mPageId);
			} else if (id == mInsertMenuIndex[INSERT_FROM_CLIPBOARD]) { // Better
				if (MetaData.HasMultiClipboardSupport
				        && (mClipboardManager != null)) {
					GAIndex = INSERT_FROM_CLIPBOARD; // Dave for GA
					try {
						MultiClipboardHelper.openClipboard(mClipboardManager,
						        SUPPORT_CLIPTYPE_TEXT | SUPPORT_CLIPTYPE_IMAGE,
						        new java.lang.reflect.InvocationHandler() {

							        @Override
							        public Object invoke(Object proxy,
							                Method m, Object[] args)
							                throws Throwable {
								        // TODO Auto-generated method stub
								        if (m.getName().equals(
								                "onClipDataSelected")) {
									        onClipDataSelected(
									                (String) args[0],
									                (String) args[1]);
								        } else if (m.getName().equals(
								                "onClipboardOpen")) {
									        onClipboardOpen();
								        } else if (m.getName().equals(
								                "onClipboardClose")) {
									        onClipboardClose();
								        }
								        return null;
							        }

							        public void onClipDataSelected(
							                String mimeType, String data) {
								        if (mimeType
								                .equalsIgnoreCase(MIMETYPE_TEXT_IMAGE)) {
									        Attacher attacher = mEditorUiUtility
									                .getInputManager()
									                .doAttach(
									                        InputManager.RESULT_CLIPBOARD);
									        Intent intent = new Intent();
									        intent.setData(Uri.parse(data));
									        attacher.attachItem(intent);
								        }
								        if (mimeType
								                .equalsIgnoreCase(MIMETYPE_TEXT_PLAIN)) {
									        if (mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_DOODLE) {
										        mEditorUiUtility
										                .getPageEditor()
										                .insertText(data);
										        mEditorUiUtility
										                .getPageEditor()
										                .getDoodleView()
										                .redrawAll(true);
									        }
								        }
							        }

							        public void onClipboardOpen() {
							        }

							        public void onClipboardClose() {
							        }

						        });

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (id == mInsertMenuIndex[INSERT_STAMP]) {// jason
				GAIndex = INSERT_STAMP; // Dave for GA
				mInsertStamp = true; // Carol
				showGridSelectDialog(STAMP_DLG);
				MetaData.INSERT_PHOTO_SELECTION = true; // Carol
			} else if (id == mInsertMenuIndex[INSERT_SHAPE]) {// jason
				GAIndex = INSERT_SHAPE; // Dave for GA
				mInsertStamp = false;
				showGridSelectDialog(SHAPE_DLG);
				MetaData.INSERT_PHOTO_SELECTION = true; // Carol
			}

			// +++ Dave GA
			if (MetaData.IS_GA_ON) {
				GACollector gaCollector = new GACollector(
				        mEditorUiUtility.getContext());
				gaCollector.objectInsert(GAIndex);
			}
			// ---
			dismissAllPopupWindow();
		}
	};

	private OnTouchListener mOnPupupWindowOnTouch = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Log.i("getAction=", event.getAction() + "");
			if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
				dismissAllPopupWindow();
				return true;
			}
			return false;
		}
	};

	private OnClickListener mOnShareOKButtonClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mShareToDialog != null) {
				mShareToDialog.dismiss();
			}

			if ((mShareCheckBoxTextOnly != null)
			        && mShareCheckBoxTextOnly.isChecked()) {
				shareOnlyText();
			} else {
				if (mShareBitmap != null) {
					mShareBitmap.recycle();
					mShareBitmap = null;
				}
				int width = isPhoneScreen() ? (int) getResources()
				        .getDimension(
				                R.dimen.phone_page_share_bitmap_default_width)
				        : (int) getResources().getDimension(
				                R.dimen.pad_page_share_bitmap_default_width);
				int height = isPhoneScreen() ? (int) getResources()
				        .getDimension(
				                R.dimen.phone_page_share_bitmap_default_height)
				        : (int) getResources().getDimension(
				                R.dimen.pad_page_share_bitmap_default_height);

				mShareBitmap = mEditorUiUtility.getScreenShotNotForPdf(
				        (int) (width), (int) (height), false, // false);
				        mShareCheckBoxHideGrid.isChecked(), mShareScale);
				if (mShareBitmap == null) {
					return;
				}
				//emmanual
				shareBitmap(mShareBitmap);
			}
			clearStaticParam();
			mPauseForHome = false; //Emmanual
		}
	};

	//emmanual
	public void shareBitmap(Bitmap bm) {
		File file = new File(MetaData.SHARE_DIR);
		if (file.exists() == false) {
			file.mkdir();
		}
		String path = MetaData.SHARE_DIR + System.currentTimeMillis() + ".jpg";
		file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
		}

		try {
			FileOutputStream out = new FileOutputStream(file);
			bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			
		} catch (IOException e) {
			
		}

		try {
			String uriString = null;
		    if(android.os.Build.VERSION.SDK_INT >= 19){  //smilefish fix bug 760480
		        ContentValues newValues = new ContentValues(5);
	            newValues.put(MediaStore.Images.Media.DISPLAY_NAME,
	                    file.getName());
	            newValues.put(MediaStore.Images.Media.DATA, file.getPath());
	            newValues.put(MediaStore.Images.Media.DATE_MODIFIED,
	                    System.currentTimeMillis() / 1000);
	            newValues.put(MediaStore.Images.Media.SIZE, file.length());
	            newValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

                ContentResolver contentResolver = getContentResolver();
                Uri uri = contentResolver.insert( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, newValues);
                uriString = MethodUtils.toSafeString(uri);
	        }else{
	            uriString  = MediaStore.Images.Media.insertImage(
	            		getContentResolver(), path, null, null);
	        }
		    
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("image/jpeg");
			if (!uriString.equals("")){
				shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(uriString));
				startActivity(shareIntent);
			}
        } catch (Exception e) {

        }
	}
	
	private OnClickListener mOnShareCancelButtonClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mShareToDialog != null) {
				mShareToDialog.dismiss();
			}

			if (mShareBitmap != null) {
				mShareBitmap.recycle();
				mShareBitmap = null;
			}
			clearStaticParam();
		}
	};

	private OnCheckedChangeListener mOnShareHideGridCheckedChange = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
		        boolean isChecked) {

			mIsChecked_hidegrid = isChecked;// darwin

		}
	};

	private OnCheckedChangeListener mOnShareDataCheckedChange = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
		        boolean isChecked) {	
			mShareCheckBoxHideGrid.setEnabled(!mShareCheckBoxTextOnly
			        .isChecked());

			mIsChecked_onlytext = isChecked;// darwin

			if (isChecked) {
				// [Carol]if text is the only content, display the text and also
				// hide the spinnerlayout
				if(mSharedText.equals("")){
					mSharedText = CFG.getCanDoVO() ? mEditorUiUtility
				        .getOnlyTextWithVO() : mEditorUiUtility.getOnlyText();
				}
				if (mSharedText.length() > 0) {
					mTextInfo.setText(mSharedText);
					mTextInfoScrollView.setVisibility(View.VISIBLE);
					mShareCheckBoxHideGrid.setVisibility(View.GONE);
					mSpinner_preview_size.setVisibility(View.GONE);
				}
			} else {
				// unchecked
				mTextInfoScrollView.setVisibility(View.GONE);
				mShareCheckBoxHideGrid.setVisibility(View.VISIBLE);
				mSpinner_preview_size.setVisibility(View.VISIBLE);
			}
		}
	};

	// begin darwin
	public String getPagePath(NoteBook book, NotePage page) {
		File dir = new File(MetaData.DATA_DIR);
		dir.mkdir();
		File bookDir = new File(MetaData.DATA_DIR, Long.toString(book
		        .getCreatedTime()));
		bookDir.mkdir();
		File pageDir = new File(bookDir, Long.toString(page.getCreatedTime()));
		pageDir.mkdir();
		return pageDir.getPath();
	}

	public String getBookPath(NoteBook book) {
		File dir = new File(MetaData.DATA_DIR);
		dir.mkdir();
		File bookDir = new File(MetaData.DATA_DIR, Long.toString(book
		        .getCreatedTime()));
		bookDir.mkdir();

		return bookDir.getPath();
	}

	// darwin
	public void setSelectAllText() {
		mEditorUiUtility.setSelectAllText();
	}

	public static int s_orientation = -1;
	private static int s_oldOrientaton = -1;
	// begin jason
	public static final String CLOSE_ACTIVITY = "CLOSE_ACTIVITY";
	private AirTextViewTool mAirTextViewTool = null;
	// end jason	

	private PageWidget mPageWidget;
	private View mTotalLayoutView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(MetaData.DEBUG_TAG, "onCreate" + savedInstanceState);
		
		// BEGIN: Better
		if (MetaData.AppContext == null) {
			MetaData.AppContext = getApplicationContext();
		}
		// END: Better
		if (!isShareMode())
			NoteBookPickerActivity.setFirstIn(NoteBookPickerActivity
			        .getFirstIn() + 1);

		// BEGIN: RICHARD
		CFG.setPath(this.getDir("Data", 0).getAbsolutePath());
		// END: RICHARD

		s_orientation = getResources().getConfiguration().orientation;
		// darwin
		if (s_oldOrientaton == -1) {
			s_oldOrientaton = s_orientation;
		}

		// Begin : Darwin_yu@asus.com
		// Modified reason : multi-DPI support
		DisplayMetrics dm = new DisplayMetrics();
		(this).getWindowManager().getDefaultDisplay().getMetrics(dm);
		// End : Darwin_yu@asus.com

		deviceType = PickerUtility.getDeviceType(this);

		// Begin: show_wang@asus.com
		// Modified reason: for multi-dpi
		IsOrientationAllowOrNot = this.getResources().getInteger(
		        R.integer.is_orientation_allow); // by show
		if (IsOrientationAllowOrNot == 0) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		} else if (IsOrientationAllowOrNot == 1) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		// End: show_wang@asus.com
		if (savedInstanceState != null) {
			mPageId = savedInstanceState.getLong(MetaData.PAGE_ID);// 0 for no
			                                                       // mapping
			mBookId = savedInstanceState.getLong(MetaData.BOOK_ID);// 0 for no
			                                                       // mapping

			getIntent().putExtra(MetaData.PAGE_ID, mPageId);
			getIntent().putExtra(MetaData.BOOK_ID, mBookId);

		}

		NoteBookPickerActivity.setIsRunFirstLoad(); // smilefish fix bug 357951

		checkWidgetOneInstalledOrNot(); //check existence of widget one
		
		super.onCreate(null);
		mAirTextViewTool = new AirTextViewTool(getApplicationContext());// by
		                                                                // jason
		// begin jason
		IntentFilter filter = new IntentFilter();
		filter.addAction(CLOSE_ACTIVITY);
		registerReceiver(mBroadCastReceiver, filter);
		// end jason
		// darwin
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int densityDpi = metric.densityDpi;
		MetaData.DPI = densityDpi;
		if (deviceType > 100) {
			MetaData.BASE_DPI = 160;
		} else {
			MetaData.BASE_DPI = 240;
		}
		// darwin

		// Begin Siupo
		MetaData.Scale = getResources()
		        .getDimension(R.dimen.edittext_scale_pad);
		// End Siupo

		// Begin Siupo
		PAD_PAGE_SHARE_BITMAP_DAFAULT_WIDTH = (int) getResources()
		        .getDimension(R.dimen.pad_page_share_bitmap_default_width);
		PAD_PAGE_SHARE_BITMAP_DAFAULT_HEIGHT = (int) getResources()
		        .getDimension(R.dimen.pad_page_share_bitmap_default_height);
		PHONE_PAGE_SHARE_BITMAP_DAFAULT_WIDTH = (int) getResources()
		        .getDimension(R.dimen.phone_page_share_bitmap_default_width);
		PHONE_PAGE_SHARE_BITMAP_DAFAULT_HEIGHT = (int) getResources()
		        .getDimension(R.dimen.phone_page_share_bitmap_default_height);
		// End Siupo
		// BEGIN: Wendy
		// receive save intent before sync and reload page intent after sync
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MetaData.SAVE_PAGE);
		intentFilter.addAction(MetaData.RELOAD_PAGE);
		registerReceiver(updateUiReceiver, intentFilter);
		// END: Wendy

		mEditorPhoneIdList = new EditorPhoneIdList();
		mEditorPadIdList = new EditorPadIdList();
		mEditorPortraitPadIdList = new EditorPortraitPadIdList();
		mEditorLandPhoneIdList = new EditorLandPhoneIdList(); // Carol

		if (deviceType <= 100) {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				mEditorIdList = mEditorLandPhoneIdList; // Carol
			} else {
				mEditorIdList = mEditorPhoneIdList;
			}
		} else {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				mEditorIdList = mEditorPadIdList;
			} else {
				mEditorIdList = mEditorPortraitPadIdList;
			}
		}
		/* mars_li for colorfulbar */
		ColorfulStatusActionBarHelper.setContentView(R.layout.editor_aitvity,
		        false, this);		
		templateLinearLayout = (TemplateLinearLayout) findViewById(R.id.template_linearlayout);
		/* mars_li end */
		// setContentView(R.layout.editor_aitvity);
		// Begin: show_wang@asus.com
		// Modified reason: for dds
		Object obj = (Object) getLastNonConfigurationInstance();
		if (obj != null) {
			mIsConfig = true;
		}
		// End: show_wang@asus.com
		mBookCase = BookCase.getInstance(this);
		// BEGIN james5_chan@asus.com if it comes from Phone (Not Phone mode).
		
		//by emmanual to avoid adding page repeatly when first load(Fix bug 376359,380222,375037)
		mSharedPreference = getSharedPreferences(MetaData.PREFERENCE_NAME,
		        Activity.MODE_PRIVATE);
		if (mSharedPreference.getBoolean(
		        getResources().getString(R.string.pref_first_load), true) == true) {
			MetaData.mAddPageFromPhoneRepeat = true;
		}
		if (!MetaData.mAddPageFromPhoneRepeat
		        || mSharedPreference.getBoolean(
		                getResources().getString(R.string.pref_first_load),
		                true) == true) {
			isTelephoneMemo();
		}
		MetaData.mAddPageFromPhoneRepeat = false;

		// Begin Allen
		if (mFirstLoadData && savedInstanceState != null) // carol
			mStartVoiceInput = savedInstanceState
			        .getBoolean("first_voice_input");
		else {
			mStartVoiceInput = getIntent().getBooleanExtra(
			        ToDoWidgetService.EXTRA_TODO_WIDGET_VOICE_INPUT, false);
			getIntent().putExtra(
			        ToDoWidgetService.EXTRA_TODO_WIDGET_VOICE_INPUT, false);
		}

		mBookId = getIntent().getLongExtra(MetaData.BOOK_ID, 0L);
		mPageId = getIntent().getLongExtra(MetaData.PAGE_ID, 0L);

		MetaData.CurrentEditPageId = mPageId;

		// BEGIN: Better
		mIsLoadingAsync = false;
		getIntent().removeExtra(MetaData.IS_ASYNC);
		// END: Better
		mNoteBook = mBookId == 0L ? mBookCase.getCurrentBook() : mBookCase
		        .getNoteBook(mBookId);
		if (mNoteBook == null) {
			Toast.makeText(this, R.string.memo_not_exists, Toast.LENGTH_SHORT)
			        .show();
			MetaData.CurrentEditPageId = -1;
			finish();
			// Begin Allen ++ catch book not exist exception
			if (getIntent().getBooleanExtra(MetaData.START_FROM_WIDGET, false)) {
				if (!MetaData.SuperNoteUpdateInfoSet
				        .containsKey(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_WIDGET_DATA_INVALID)) {
					MetaData.SuperNoteUpdateInfoSet
					        .put(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_WIDGET_DATA_INVALID,
					                null);
				}
				Intent updateIntent = new Intent();
				updateIntent.setAction(MetaData.ACTION_SUPERNOTE_UPDATE);
				updateIntent.putExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM,
				        MetaData.SuperNoteUpdateInfoSet);
				sendBroadcast(updateIntent);
				MetaData.SuperNoteUpdateInfoSet.clear();
			}
			return;
			// End Allen
		}
		mNotePage = mNoteBook.getNotePage(mPageId);
		if (mNotePage == null) {
			Toast.makeText(this, R.string.memo_not_exists, Toast.LENGTH_SHORT)
			        .show();
			MetaData.CurrentEditPageId = -1;
			finish();
			// Begin Allen ++ catch book not exist exception
			if (getIntent().getBooleanExtra(MetaData.START_FROM_WIDGET, false)) {
				if (!MetaData.SuperNoteUpdateInfoSet
				        .containsKey(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_WIDGET_DATA_INVALID)) {
					MetaData.SuperNoteUpdateInfoSet
					        .put(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_WIDGET_DATA_INVALID,
					                null);
				}
				Intent updateIntent = new Intent();
				updateIntent.setAction(MetaData.ACTION_SUPERNOTE_UPDATE);
				updateIntent.putExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM,
				        MetaData.SuperNoteUpdateInfoSet);
				sendBroadcast(updateIntent);
				MetaData.SuperNoteUpdateInfoSet.clear();
			}
			// End Allen
			return;
		}
		CoverHelper.initDefaultCoverBitmap(mNoteBook.getBookColor(),
		        mNoteBook.getGridType(), getResources());// Allen

		SuperNoteApplication.setContext(getApplicationContext());
		mEditorUiUtility = new EditorUiUtility(this);
		writeViewLastPage(); //fix bug 382097
		// Begin: show_wang@asus.com
		// Modified reason: for dds

		if (savedInstanceState != null) {
			mIsTextImgEdit = savedInstanceState
			        .getBoolean("textimage_modified_status");
			mIsEraserOn = savedInstanceState.getBoolean("eraser_button_status");
			mIsShowingInsertDialog = savedInstanceState
			        .getBoolean("insert_dialog_status"); // Carol
			mInsertStamp = savedInstanceState.getBoolean("insert_dialog_type");
		}
		if (mIsShowingInsertDialog) // display the stamp/shape dialog again
		                            // after rotating [Carol]
			showGridSelectDialog(mInsertStamp ? STAMP_DLG : SHAPE_DLG);

		// End: show_wang@asus.com
		int inputMode = mSharedPreference.getBoolean(
		        getResources().getString(R.string.pref_default_read_mode),
		        false) ? InputManager.INPUT_METHOD_READONLE : mEditorUiUtility
		        .getInputModeFromPreference();
		getEditorTextStyleAndColorFromPreference();
		getColorsBrushStrokeFromPreference();

		mBrushCollection = new BrushCollection(this); // smilefish

		// BEGIN: RICHARD
		getWTAndShapeButtonStatusFromPreference();
		// END: RICHARD

		// Begin Allen
		mEditorUiUtility.getPageEditor().setLoadingPageIsFullListener(
		        new onLoadingPageIsFullListener() {

			        @Override
			        public boolean onLoadingPageIsFull() {
				        boolean isAddToDoItemFromWidget = getIntent()
				                .getBooleanExtra(
				                        ToDoWidgetService.EXTRA_TODO_WIDGET_IS_ADD_TODO_ITEM,
				                        false);
				        if (isAddToDoItemFromWidget) {
					        addNewPage(false);
					        return true;
				        } else {
					        return false;
				        }
			        }
		        });
		// End Allen

		mEditorUiUtility.setInputMode(inputMode);

		// +++ Dave GA
		if (MetaData.IS_GA_ON) {
			GACollector gaCollector = new GACollector(
			        mEditorUiUtility.getContext());
			if (inputMode == InputManager.INPUT_METHOD_KEYBOARD
			        || inputMode == InputManager.INPUT_METHOD_SCRIBBLE
			        || inputMode == InputManager.INPUT_METHOD_DOODLE) {
				gaCollector.editorModeChange(inputMode);
			}

			int orientation = 0;
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				orientation = 1;
			} else {
				orientation = 0;
			}
			gaCollector.screenModeChange(orientation);

		}
		// ---
		
		// emmanual
		mKeyboardTime = 0;
		mScribbleTime = 0;
		mDoodleTime = 0;
		mRecordTime = 0;

		getActionBar().hide();// smilefish
		// BEGIN: archie_huang@asus.com
		View backgroundView = findViewById(R.id.pageActivityLayout);
		if (backgroundView != null) {
			backgroundView.setBackgroundColor(getBookColor());
		}
		// END: archie_huang@asus.com
		// begin smilefish
		View functionBarView = findViewById(R.id.functionBarLayout);
		if (functionBarView != null) {
			if(deviceType > 100){
				ImageView line = (ImageView)findViewById(R.id.functionBarDividingLine);
				if(isPhoneScreen()){
					functionBarView.setBackgroundColor(getResources().getColor(
					        R.color.edit_page_bg_color));
					if(line != null){
						line.setBackgroundColor(getResources().getColor(
						        R.color.edit_page_bg_color));
						line.setImageResource(R.drawable.no);
					}
				}else{
					functionBarView.setBackgroundColor(getBookColor());
					if(line != null){
						line.setBackgroundColor(getBookColor());
						if(getBookTemplateType() == MetaData.Template_type_normal ||
								getBookTemplateType() == MetaData.Template_type_blank)
							line.setImageResource(R.color.function_bar_dividing_line_color);
						else
							line.setImageResource(R.drawable.no);
					}
				}
			}else{
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
					functionBarView.setBackgroundColor(getResources().getColor(
					        R.color.edit_page_bg_color));
				else
					functionBarView.setBackgroundColor(getBookColor());
			}
		}
		// end smilefish

		// darwin
		if ((getIntent().getLongExtra(MetaData.TEMPLATE_BOOK_NEW, 0L) != 0)
		        && !((mPageIdOld == mPageId) && (mBookIdOld == mBookId))) {
			mPageIdOld = mPageId;
			mBookIdOld = mBookId;
			mSaveButtonStatus = SAVE_BUTTON_DISABLE;
		}
		// darwin

		if (savedInstanceState != null) {
			mCursorPos = savedInstanceState.getInt("cursor position");
		} else if (getIntent().hasExtra(MetaData.TIMESTAMP_POS)) {
			mCursorPos = getIntent().getIntExtra(MetaData.TIMESTAMP_POS, 0);
		} else {
			mCursorPos = -1;
		}
		// James5, using acquireWakeLock and releaseWakeLock, in new version
		float pwidth = getResources().getDimension(
		        R.dimen.preview_content_width);
		float pheight = getResources().getDimension(
		        R.dimen.preview_content_height);
		float px = getResources().getDimension(R.dimen.preview_content_x);
		float py = getResources().getDimension(R.dimen.preview_content_y);
		genPath((int) pwidth, (int) pheight, (int) px, (int) py);

		// BEGIN: Better
		mAutoSaveTimer = new AutoSaveCountdownCounter(AUTO_SAVE_INTERVAL);
		// END: Better

		// BEGIN: RICHARD

		if (CFG.getCanDoVO() == true)// darwin
		{
			ComponentName componantName = new ComponentName(
			        this.getPackageName(), IndexService.class.getName());
			Intent sIntent = new Intent().setComponent(componantName);
			sIntent.setAction(IndexService.INDEXER_START_INTENT);
			startService(sIntent);
			mIndexServiceClient = new IndexServiceClient();
			mIndexServiceClient.doBindService(this);
		}
		// END: RICHARD

		if (MetaData.HasMultiClipboardSupport) {
			mClipboardManager = (ClipboardManager) getApplicationContext()
			        .getSystemService(Context.CLIPBOARD_SERVICE); // Better
			NoteEditText editText = mEditorUiUtility.getPageEditor()
			        .getNoteEditText();
			if (editText != null) {
				MultiClipboardHelper.setSupportInputType(editText,
				        SUPPORT_CLIPTYPE_TEXT | SUPPORT_CLIPTYPE_IMAGE);
			}
		}

		mFirstLoadData = true;
		MetaData.IS_LOAD = false; // Carol
		this.EnableSaveButton(mSaveButtonStatus == SAVE_BUTTON_ENABLE);

		if (mSharedPreference.getBoolean(
		        getResources().getString(R.string.pref_first_load), true) == true) {
			NoteBookPickerActivity.firstRunLoadResource(mSharedPreference,
			        getResources(), this);
		}

		showShareDialog();// darwin
		mIsCreate = true; // by show
		
		//emmanual to fix bug 399909
		int currentEditId = 0;
		if (savedInstanceState != null) {
			currentEditId = savedInstanceState.getInt("current_edittext_id");
		}
		if(currentEditId != 0){
			View view = findViewById(currentEditId);
			if (view != null) {
				view.requestFocus();
			}
		}else{
			//emmanual to fix 419038
			if(mEditorUiUtility.getPageEditor().getTemplateType() == MetaData.Template_type_travel
					&& !MetaData.IS_AUTO_PAGING){
				View view = findViewById(R.id.travel_title_edit);
				if (view != null) {
					view.requestFocus();
				}
			}else{
				mEditorUiUtility.getPageEditor().getCurrentEditor().requestFocus();
			}
		}

		//emmanual to fix bug 411589
		if (savedInstanceState != null) {
			TemplateEditText attendee = (TemplateEditText) findViewById(R.id.attendee_edit);
			if(attendee != null){
				attendee.setText(savedInstanceState.getString("attendee_nametext"));
			}
		}
		
		mPageWidget = (PageWidget) findViewById(R.id.page_widget);
		mTotalLayoutView = findViewById(R.id.totalLayoutView);
		WindowManager wm = this.getWindowManager();
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
		Bitmap mCurPageBitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_4444);
		mPageWidget.setBitmaps(mCurPageBitmap);		

		if (savedInstanceState != null) {
			if(savedInstanceState.getBoolean("select_pdf_dialog_show")){				
				Parcelable[] bitmaps = savedInstanceState.getParcelableArray("select_pdf_bitmaps");
				if (bitmaps != null) {
					microBmpList = new ArrayList<Bitmap>();
					for (Parcelable bm : bitmaps) {
						microBmpList.add((Bitmap) bm);
					}
				}			
				boolean[] checks = savedInstanceState.getBooleanArray("select_pdf_checks");
				if (checks != null) {
					pdfCheckList = new ArrayList<Boolean>();
					for (boolean c : checks) {
						pdfCheckList.add(c);
					}
				}

				mReadpdfPath = savedInstanceState.getString("select_pdf_path");
				prepareImportPdf(mReadpdfPath, false);
			}
		}
	}

	private void showPageTranstion(final boolean toNext) {
		// emmanual: add page transtion switch
		boolean showPageTranstion = getSharedPreferences(
		        MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE).getBoolean(
		        getResources().getString(R.string.pref_page_transtion), true);
		if (!showPageTranstion) {
			return;
		}
	
		new Handler().post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub				
				mPageWidget.setVisibility(View.VISIBLE);
				if(toNext){
					mPageWidget.nextPage();
				}else{
					mPageWidget.previousPage();
				}
				
				Bitmap bitmap = Bitmap.createBitmap(mTotalLayoutView.getWidth(),
				        mTotalLayoutView.getHeight(), Bitmap.Config.ARGB_4444);
				Canvas c = new Canvas(bitmap);
				mTotalLayoutView.draw(c);
		
				View view = EditorActivity.this.findViewById(R.id.first_item);
				if (view != null) {
					PageEditor pe = mEditorUiUtility.getPageEditor();
					Bitmap contentBitmap = Bitmap.createBitmap(view.getWidth(),
					        view.getHeight(), Bitmap.Config.ARGB_4444);
					if (deviceType > 100 && isPhoneScreen())
						contentBitmap.eraseColor(getResources().getColor(
						        R.color.notebook_gridview_color));
					else
						contentBitmap.eraseColor(getBookColor());
					Canvas canvas = new Canvas(contentBitmap);
					pe.drawSnapshot(canvas, false);
					int top = findViewById(R.id.functionBarLayout).getMeasuredHeight();
					int bottom = findViewById(R.id.bottom_button_container).getMeasuredHeight();
					Rect src = new Rect();
					src.left = 0;
					src.top = 0;
					src.right = view.getWidth();
					src.bottom = view.getHeight() - bottom;
					Rect dst = new Rect();
					dst.left = 0;
					dst.top = top;
					dst.right = view.getWidth();
					dst.bottom = top + view.getHeight() - bottom;
					c.drawBitmap(contentBitmap, src, dst, null);
				}
				mPageWidget.setBitmaps(bitmap);
			}
		});
	}
	
	public boolean onUpperViewShortPress(MotionEvent ev) {
		if ((deviceType <= 100)
		        || mNoteBook.getPageSize() != MetaData.PAGE_SIZE_PHONE) {
			return false;
		}
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		boolean result = false;
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			result = dispatchToChangePage(x, y);
		}
		return result;
	}

	private boolean dispatchToChangePage(int x, int y) {
		for (ImageView view : mImageViews) {
			if (checkPointInsideView(x, y, view)) {
				return view.performClick();
			}
		}
		for (ImageView view : mRightImageViews) {
			if (checkPointInsideView(x, y, view)) {
				return view.performClick();
			}
		}
		return false;
	}

	private boolean checkPointInsideView(int x, int y, View view) {
		y += 118; // Action Bar Height + Func Bar Height;
		int[] locate = new int[2];
		view.getLocationInWindow(locate);
		return x >= view.getLeft() + locate[0]
		        && x <= view.getRight() + locate[0]
		        && y >= view.getTop() + locate[1]
		        && y <= view.getBottom() + locate[1];
	}

	protected void selectThisDoodleTool(View v) {
		for (View view : mDoodleFuncButtons) {
			view.setSelected(false);
		}
		if (v != null) {
			v.setSelected(true);
		}
		CursorIconLibrary.setStylusIcon(templateLinearLayout, getDoodleDrawable());
	}

	private void fileFormatError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.pg_open_fail);
		builder.setPositiveButton(android.R.string.ok, null);
		Dialog dialog = builder.create();

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				finish();

			}
		});

		dialog.show();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.v(MetaData.DEBUG_TAG, "onConfigurationChanged");
	}

	public void doAfterResume() {
		if (mCursorPos != -1) {
			// begin smilefish
			if (mCursorPos > 0)
				mEditorUiUtility.setCursorPos(mCursorPos);
			if (mScrollBarX > 0 || mScrollBarY > 0)
				mEditorUiUtility.getPageEditor().ScrollViewTo(mScrollBarX,
				        mScrollBarY, true);
			// end smilefish
		}
		// Begin: show_wang@asus.com
		// Modified reason: for dds
		if ((mIsConfig && mIsCreate)) {// || (!mIsConfig && !mIsCreate)
										// emmanual to fix bug 474908
			int savedInputMode = mSharedPreference.getInt(
			        MetaData.PREFERENCE_CURRENT_INPUT_MODE, -1);
			switch (savedInputMode) {
			case InputManager.INPUT_METHOD_SELECTION_TEXT:
				if (mSharedPreference.getBoolean(
				        MetaData.PREFERENCE_IS_TEXTIMG_SELECTION_TEXT, false)) {
					int textImgMode = mSharedPreference.getInt(
					        MetaData.PREFERENCE_TEXTIMG_MODE_SELECTION_TEXT,
					        InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD);
					mEditorUiUtility.setInputMode(textImgMode);
					mEditorUiUtility.setBoxEditTextContent();
					if (mIsTextImgEdit) {
						mEditorUiUtility.selectCurrentDrawInfo();
					}
					mEditorUiUtility.setTextImgStatus();
					mEditorUiUtility.getPageEditor().setFirstTextChange(true);
					mEditorUiUtility.getInputManager().setInputMode(
					        savedInputMode);
				} else {
					mEditorUiUtility.getPageEditor().setFirstTextChange(true);
					mEditorUiUtility.setInputMode(savedInputMode);
					// Begin: show_wang@asus.com
					// Modified reason: for setselection
					int start = mSharedPreference.getInt(
					        MetaData.PREFERENCE_SELECTION_TEXT_START, 0);
					int end = mSharedPreference.getInt(
					        MetaData.PREFERENCE_SELECTION_TEXT_END, 0);
					NoteEditText editText = mEditorUiUtility.getPageEditor()
					        .getNoteEditText();
					if (end > editText.getText().length()) {
						end = editText.length();
					}
					if (start > editText.length()) {
						start = editText.length();
					}
					editText.setSelection(start, end);
					// End: show_wang@asus.com

				}
				break;
			case InputManager.INPUT_METHOD_INSERT:
				mEditorUiUtility.setInputMode(savedInputMode);
				if (!MetaData.INSERT_PHOTO_SELECTION) // Carol
					mEditorUiUtility.selectCurrentDrawInfo();
				// begin smilefish : fix bug 294088
				if (mNoteBook.getTemplate() == MetaData.Template_type_todo)
					mEditorUiUtility.redrawDoodleView();
				// end smilefish
				break;
			case InputManager.INPUT_METHOD_SELECTION_DOODLE:
				mEditorUiUtility.setInputMode(savedInputMode);
				// begin smilefish : fix bug 294088
				if (mNoteBook.getTemplate() == MetaData.Template_type_todo)
					mEditorUiUtility.redrawDoodleView();
				// end smilefish
				break;
			case InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD:
			case InputManager.INPUT_METHOD_TEXT_IMG_SCRIBBLE:
				mEditorUiUtility.setInputMode(savedInputMode);
				mEditorUiUtility.setBoxEditTextContent();
				if (mIsTextImgEdit) {
					mEditorUiUtility.selectCurrentDrawInfo();
				}
				break;
			case InputManager.INPUT_METHOD_COLOR_PICKER: // smilefish
				mEditorUiUtility.setInputMode(savedInputMode);
				colorPickerViewShow(true);
				mColorPickerDoneButton.setEnabled(false);
				mColorPickerHint.setVisibility(View.VISIBLE);
				mColorChosenShow.setVisibility(View.GONE);
				break;
			default:
				break;
			}
		}
		if (mIsCropConfig) {
			mEditorUiUtility.selectCurrentDrawInfo();
			mEditorUiUtility.updateCropDrawInfo(cropFileName, cropOrgFileName);
			mEditorUiUtility.setInputMode(mEditorUiUtility
			        .getInputModeFromPreference());
			//emmanual to fix bug 466565, 508849
			mEditorUiUtility.setDoodleTool(mDoodleToolCode);
			mIsCropConfig = false;
		}
		if (mIsChangeGraphicCropConfig) {
			Bitmap bmp = BitmapFactory.decodeFile(getFilePath() + "/"
			        + changeGarphicCropFileName);
			((ImageView) findViewById(R.id.travel_image)).setImageBitmap(bmp);
			((TemplateImageView) findViewById(R.id.travel_image))
			        .setImageFilePath(changeGarphicCropFileName);
			mIsChangeGraphicCropConfig = false;
		}
		// End: show_wang@asus.com
		boolean isPrompt = false, isNewPage = false;
		if ((deviceType <= 100) 
				//emmanual to fix bug 506379
				&& !mSharedPreference.getBoolean(getResources().getString(R.string.pref_default_read_mode), false)
		        && (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {
			isPrompt = mSharedPreference.getBoolean(getResources()
			        .getString(R.string.pref_prompt_handwriting_animating),
			        true);
			if (isPrompt) {
				isNewPage = getIntent().getBooleanExtra(MetaData.IS_NEW_PAGE, false);
				if (isNewPage) {
					getIntent().removeExtra(MetaData.IS_NEW_PAGE);

					try {
						Intent intent = new Intent();
						intent.setClass(this, ContiniousLineTipsActivity.class);
						startActivityForResult(intent,
						        CONTINIOUS_LINE_TIPS_REQUEST);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		// begin noah;for share
		if (!isPrompt && !isNewPage && isShareMode()) {
			final Intent shareIntent = getShareIntent();
			boolean isShared = shareIntent.getBooleanExtra(EXTRA_IS_SHARED,
			        false);
			if (!isShared) {
				shareIntent.putExtra(EXTRA_IS_SHARED, true);
				new Handler().post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						addShareAttacher(shareIntent);
					}
				});
			}
		}
		// end noah;for share

		// Begin: show_wang@asus.com
		// Modified reason: for dds
		if (mIsConfig) {
			if (mIsInsertGalleryAttacher && mIntent != null) {
				GalleryAttacher.attachItem(mIntent,
				        mEditorUiUtility.getPageEditorManager());
				mIsInsertGalleryAttacher = false;
			}
			if (mIsInsertCameraAttacher) {
				CameraAttacher.attachItem(mEditorUiUtility
				        .getPageEditorManager());
				mIsInsertCameraAttacher = false;
			}
			if (mIsInsertVoiceAttacher && mIntent != null) {
				VoiceAttacher.attachItem(mIntent,
				        mEditorUiUtility.getPageEditorManager());
				mIsInsertVoiceAttacher = false;
			}
			if (mIsInsertVoiceFileAttacher && mIntent != null) {//smilefish fix bug 755270
				VoiceAttacher.attachFile(mIntent,
				        mEditorUiUtility.getPageEditorManager());
				mIsInsertVoiceFileAttacher = false;
			}
			if (mIsInsertVideoAttacher && mIntent != null) {
				VideoAttacher.attachItem(mIntent,
				        mEditorUiUtility.getPageEditorManager());
				mIsInsertVideoAttacher = false;
			}
			if (mIsInsertTextFileAttacher && mIntent != null) {
				TextFileAttacher.attachItem(mIntent,
				        mEditorUiUtility.getPageEditorManager());
				mIsInsertTextFileAttacher = false;
			}
			mIntent = null;
		}
		if (mSharedPreference.getInt(MetaData.PREFERENCE_CURRENT_INPUT_MODE, -1) == InputManager.INPUT_METHOD_KEYBOARD
		        && !mSharedPreference.getBoolean(getResources().getString(R.string.pref_default_read_mode), false)) {
			if(MetaData.IS_AUTO_PAGING || isImageShareMode()){
				hiddenSoftKeyboard();
			} else {
				DialogUtils.showSoftInput((NoteEditText) mEditorUiUtility.getPageEditor().getCurrentEditor());
			}
		}
		mSharedPreference.edit()
		        .putInt(MetaData.PREFERENCE_CURRENT_INPUT_MODE, -1).commit(); // By
		                                                                      // Show
		mIsConfig = false;
		mIsCreate = false;
		// End: show_wang@asus.com
		// Begin Allen
		boolean isBaseline = mSharedPreference.getBoolean(getResources()
		        .getString(R.string.pref_baseline), false); // smilefish
		mEditorUiUtility.switchBaselineMode(isBaseline);

		if (getIntent().hasExtra(
		        ToDoWidgetService.EXTRA_TODO_WIDGET_IS_ADD_TODO_ITEM)) {
			if (mSharedPreference.getBoolean(
			        getResources().getString(R.string.pref_default_read_mode),
			        false) && getIntent().getSourceBounds() == null) {
				mSharedPreference.edit().putBoolean(
				        getResources().getString(
				                R.string.pref_default_read_mode), false).commit();
				mEditorUiUtility.setInputMode(mEditorUiUtility
				        .getInputModeFromPreference());
			}
			getIntent()
			        .putExtra(
			                ToDoWidgetService.EXTRA_TODO_WIDGET_IS_ADD_TODO_ITEM,
			                false);
			//emmanual to fix bug 480757
			getIntent().removeExtra(ToDoWidgetService.EXTRA_TODO_WIDGET_IS_ADD_TODO_ITEM);
		}
		// End Allen
		mEditorUiUtility.insertTextForPhone(getIntent());
		
		//begin smilefish fix bug 373033
		if(mVideoData != null){
			PageEditorManager pageEditorManager = mEditorUiUtility
			        .getPageEditorManager();
			VideoAttacher.attachItem(mVideoData, pageEditorManager);
			mVideoData = null;
		}
		//end smilefish

		// begin emmanual fix bug 392684
		if (mPhotoData) {
			mEditorUiUtility.setInputMode(InputManager.INPUT_METHOD_INSERT);
			PageEditorManager pageEditorManager = mEditorUiUtility
			        .getPageEditorManager();
			CameraAttacher.attachItem(pageEditorManager);
			mPhotoData = false;
		}
		// end emmanual

		// begin emmanual fix bug 392684
		if (mGalleryData != null) {
			mEditorUiUtility.setInputMode(InputManager.INPUT_METHOD_INSERT);
			PageEditorManager pageEditorManager = mEditorUiUtility
			        .getPageEditorManager();
			GalleryAttacher.attachItem(mGalleryData, pageEditorManager);
			mGalleryData = null;
		}
		// end emmanual
		
		//begin emmanual fix bug 398067
		if(mTextfileData != null){
			PageEditorManager pageEditorManager = mEditorUiUtility
			        .getPageEditorManager();
			TextFileAttacher.attachItem(mTextfileData, pageEditorManager);
			mTextfileData = null;
		}
		//end emmanual

		//begin emmanual fix bug 411589
		if(mAttendeeText != null){
			TemplateEditText attendee = (TemplateEditText) findViewById(R.id.attendee_edit);
			attendee.setText(mAttendeeText);
			attendee.setSelection(mAttendeeText.length());
			mAttendeeText = null;
		}
		//end emmanual

		//begin emmanual fix bug 460810, 484265, 486870, 452510
		if (mTravelFileName != null && !mTravelFileName.equals("")) {
			Bitmap bmp = BitmapFactory.decodeFile(getFilePath() + "/"
			        + mTravelFileName);
			((ImageView) findViewById(R.id.travel_image)).setImageBitmap(bmp);
			((TemplateImageView) findViewById(R.id.travel_image))
			        .setImageFilePath(mTravelFileName);
			mTravelFileName = "";
		}
		//end emmanual
		
		if(MetaData.IS_AUTO_PAGING && MetaData.PAUSE_AUTO_PAGING){
			MetaData.PAUSE_AUTO_PAGING = false;
			try {
	            Thread.sleep(200);
            } catch (InterruptedException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
			addNewPage();
		}
	}
	
	//emmanual to fix bug 532241
	private boolean isImageShareMode(){
		if(isShareMode()){			
			Intent shareIntent = getShareIntent();
			String type = shareIntent.getType();
			if (type == null || type == "")
			{
				if (shareIntent.getBooleanExtra(MetaData.INSTANT_SHARE, false)) {
					type = "";
				} else {
					return false;
				}
			}
			if (type.startsWith("image/")) {
				return true;
			}
		}
		return false;
	}

	// begin noah;for share
	public boolean isShareMode() {
		Intent intent = getShareIntent();
		if (intent == null)
			return false;
		return true;
	}

	public Intent getShareIntent() {
		Intent extraIntent = getIntent()
		        .getParcelableExtra(Intent.EXTRA_INTENT);
		return extraIntent;
	}

	private void addShareAttacher(Intent shareIntent) {
		if (!mIsConfig) {
			if (ShareUtils.isFromBrower(shareIntent)) {
				addBrowserShare(shareIntent);
			} else {
				addCommonShare(shareIntent);
			}
		} else {
			// deleted by emmanual_chen 解决分享图片到SuperNote时，旋转屏幕时重复添加的BUG
			mIntent = shareIntent;
		}
		//emmanual to fix bug 433860
		if(ShareToMeActivity.mShareToMeActivity != null){
			//add for TT349735 [Carol]
			ShareToMeActivity.mShareToMeActivity.finish();
			ShareToMeActivity.mShareToMeActivity = null;
		}
	}

	/**
	 * 非浏览器share过来的时�?
	 * 
	 * @author noah_zhang
	 */
	private void addCommonShare(Intent shareIntent) {
		String type = shareIntent.getType();
		if (type == null || type == "")// update by jason
		{
			if (shareIntent.getBooleanExtra(MetaData.INSTANT_SHARE, false)) {
				type = "";
			} else {
				return;
			}
		}
		PageEditorManager pageEditorManager = mEditorUiUtility
		        .getPageEditorManager();
		if (type.startsWith("image/")) {
			shareIntent.setData((Uri) shareIntent
			        .getParcelableExtra(Intent.EXTRA_STREAM));
			if(isFromGoogle(shareIntent)){
				downloadFromGoogle(shareIntent, pageEditorManager, DownloadFromGoogleTask.SHARE_IMAGE_CODE);
				return;
			}
			GalleryAttacher.attachItem(shareIntent, pageEditorManager);
		} else if (type.startsWith("text/plain")) {
			MetaData.IS_PASTE_OR_SHARE = true;
			Uri uri = (Uri) shareIntent.getParcelableExtra(Intent.EXTRA_STREAM);
			int templateType = mEditorUiUtility.getPageEditor()
			        .getTemplateType();
			if (uri != null) {
				shareIntent.setData(uri);
				TextFileAttacher.attachItem(shareIntent, pageEditorManager);
			} else {
				String sharedText = shareIntent
				        .getStringExtra(Intent.EXTRA_TEXT);
				if (sharedText != null && sharedText != "") {
					if (templateType == MetaData.Template_type_todo) {// todo超过标题大小，要提示
						TemplateToDoUtility toDoUtility = mEditorUiUtility
						        .getPageEditor().getTemplateUtility()
						        .geTemplateToDoUtility();
						if (toDoUtility == null) {
							return;
						}
						if (toDoUtility.isOverTitleWidth(sharedText)) {
							EditorActivity.showToast(this,
							        R.string.content_is_too_long);
							return;
						}
					}
					pageEditorManager.getCurrentPageEditor().insetSharedText(sharedText);
				}
			}
		} else if (shareIntent.getBooleanExtra(MetaData.INSTANT_SHARE, false)) {// by
			                                                                    // jason
			shareByInstantPage(shareIntent);
			// begin jason
			shareIntent.removeExtra(MetaData.SELECT_MODE); // avoid of entering
			                                               // the selection mode
			                                               // again
			// end jason
		}
	}

	/**
	 * 浏览器share过来的时�?
	 * 
	 * @author noah_zhang
	 */
	private void addBrowserShare(Intent shareIntent) {
		int positionY = 0;
		PageEditor pageEditor = mEditorUiUtility.getPageEditor();
		int lineHeight = pageEditor.getNoteEditText().getLineHeight();
		int doodleViewHeight = pageEditor.getDoodleView().getHeight();
		Log.i(TAG, "share link,doodleViewHeight:" + doodleViewHeight);
		float dy = lineHeight * 2;// Template_type_blank:图片离上�?行高的距�?
		int templateType = pageEditor.getTemplateType();
		if (shareIntent.hasExtra("share_screenshot")
		        && templateType != MetaData.Template_type_todo) {// todo
			                                                     // 模板不添加图�?
			Parcelable localParcelable = shareIntent
			        .getParcelableExtra("share_screenshot");
			if (localParcelable instanceof Bitmap) {
				Bitmap bitmap = (Bitmap) localParcelable;
				if (templateType == MetaData.Template_type_blank
				        || templateType == MetaData.Template_type_normal) {// 将图片插入到中间位置
					GalleryAttacher.attachItem((Bitmap) localParcelable,
					        mEditorUiUtility.getPageEditorManager(), dy);
					com.asus.supernote.doodle.drawinfo.PathDrawInfo.Point[] points = pageEditor
					        .getDoodleView().getSelectionDrawInfoCorner();
					if (points != null && points.length >= 2) {
						float y = points[1].y;
						positionY = (int) Math.ceil(y);
					}
					Log.i(TAG, "share link,dy:" + dy);
				} else if (templateType == MetaData.Template_type_meeting
				        || templateType == MetaData.Template_type_travel) {// 将把图片插入到模板控件的下方
					dy = pageEditor.getTemplateLayoutScaleHeight();
					GalleryAttacher.attachItem((Bitmap) localParcelable,
					        mEditorUiUtility.getPageEditorManager(), dy);
					positionY = bitmap.getHeight();
					Log.i(TAG, "share link,dy:" + dy);
				}
			}
		}

		String link = shareIntent.getStringExtra(Intent.EXTRA_TEXT);
		link = link == null ? "" : link;
		String subject = shareIntent.getStringExtra(Intent.EXTRA_SUBJECT);
		subject = subject == null ? "" : subject;
		MetaData.INSERT_BROWSER_SHARE = true;
		pageEditor.insertShareLink(subject, link, positionY);// 这里inputmode已经改变
		MetaData.INSERT_BROWSER_SHARE = false;

		com.asus.supernote.doodle.drawinfo.PathDrawInfo.Point[] points = pageEditor
		        .getDoodleView().getSelectionDrawInfoCorner();
		if (points != null) {// 说明已经正确插入了图�?
			mEditorUiUtility.getInputManager().setInputMode(
			        InputManager.INPUT_METHOD_INSERT);
		}
	}

	// end noah;for share
	private void shareByInstantPage(Intent shareIntent) {
		if (shareIntent == null) {
			return;
		}
		String path = shareIntent
		        .getStringExtra(MetaData.INSTANT_DOODLEITEM_PATH_STRING);
		GalleryAttacher.attachDoodleItem(path,
		        mEditorUiUtility.getPageEditorManager());
		NoteBook.deleteDir(new File(path));
	}
	
	private void loadData(){
		// Begin: show_wang@asus.com
		// Modified reason: for dds
		boolean isConfigChanged = mIsConfig || mIsCropConfig || mIsChangeGraphicCropConfig;
		// End: show_wang@asus.com
		boolean loadSucceed = false;
		boolean autoPagingFlag = MetaData.IS_AUTO_PAGING && MetaData.PAUSE_AUTO_PAGING; //smilefish fix bug 704117
		if (autoPagingFlag || isConfigChanged || mFirstLoadData) { //smilefish fix bug 614472/616676
			Log.v(MetaData.DEBUG_TAG, "isConfigChanged");

			setMenuItemBookmark();

			while (MetaData.SavingPageIdList.contains(mPageId))
				;
			if (mIsLoadingAsync) {
				Log.v(MetaData.DEBUG_TAG, "load async");
				loadSucceed = mEditorUiUtility.loadPageAsync(mNotePage);
				mIsLoadingAsync = false;
			} else {
				Log.v(MetaData.DEBUG_TAG, "load sync");
				
				loadSucceed = mEditorUiUtility.loadPage(mNotePage, true);
			}
			if (!loadSucceed) {
				Log.v(MetaData.DEBUG_TAG, "load data failed!");
				fileFormatError();
			}
			mEditorUiUtility.cleanEdited();
			if (mIsResumeFromCropActivity) {
				mEditorUiUtility.getPageEditor().setDoodleModified();// darwin
				mIsResumeFromCropActivity = false;
			}

		}else{ 
			//smilefish fix bug 633943/633427
			boolean isBaseline = mSharedPreference.getBoolean(getResources()
			        .getString(R.string.pref_baseline), false); 
			mEditorUiUtility.switchBaselineMode(isBaseline);
		}
		
		
		mFirstLoadData = false;
		MetaData.IS_LOAD = true; // Carol

		// BEGIN archie_huang@asus.com
		// Re-check input mode because input mode may be changed in Setting
		// (i.e, change to ReadOnly)
		boolean isReadOnly = mSharedPreference.getBoolean(getResources()
		        .getString(R.string.pref_default_read_mode), false);
		int currentInputMode = mEditorUiUtility.getInputMode();
		if (isReadOnly
		        && currentInputMode != InputManager.INPUT_METHOD_READONLE) {
			mEditorUiUtility.setInputMode(InputManager.INPUT_METHOD_READONLE);
		} else if (!isReadOnly
		        && currentInputMode == InputManager.INPUT_METHOD_READONLE) {
			mEditorUiUtility.setInputMode(mEditorUiUtility
			        .getInputModeFromPreference());
		}
		mEditorUiUtility.reloadTimer();
		mEditorUiUtility.drawHint((RectF) getIntent().getParcelableExtra(
		        MetaData.MEMO_INFO));
		// END archie_huang@asus.com

		//Begin by Emmanual
		if(MetaData.IS_GA_ON)
		{
			if (currentInputMode == InputManager.INPUT_METHOD_KEYBOARD
			        || currentInputMode == InputManager.INPUT_METHOD_SCRIBBLE
			        || currentInputMode == InputManager.INPUT_METHOD_DOODLE) {
				mRecordTime = new Date().getTime();
			}
		}
		//End

		if (isReadOnly) {
			mEditorUiUtility.getPageEditorManager()
			        .setNoteEditTextEnable(false);// Allen todolist is not ready
			if (!loadSucceed) {
				if (mIsLoadingAsync) {
					Log.v(MetaData.DEBUG_TAG, "load async");
					loadSucceed = mEditorUiUtility.loadPageAsync(mNotePage);
					mIsLoadingAsync = false;
				} else {
					Log.v(MetaData.DEBUG_TAG, "load sync");
					loadSucceed = mEditorUiUtility.loadPage(mNotePage, true);
				}
				if (!loadSucceed) {
					Log.v(MetaData.DEBUG_TAG, "load data failed!");
					fileFormatError();
				}
				mEditorUiUtility.cleanEdited();
				if (mIsResumeFromCropActivity) {
					mEditorUiUtility.getPageEditor().setDoodleModified();// darwin
					mIsResumeFromCropActivity = false;
				}
			}
		}

		mEditorUiUtility.resumeScrollChange();
		if (!mIsReadPdfTaskRunning && !mIsDownloadTaskRunning && !mIsImportPdfTaskRunning) { //smilefish fix bug 645298/635890
			PickerUtility.unlockRotation(EditorActivity.this);
		}

		// BEGIN: Better
		mAutoSaveTimer.start();
		mIsAutoSaveNeeded = true;
		// END: Better

		// BEGIN: RICHARD
		setAutoRecognizerShapeState(true);

		boolean isPenOnly = mSharedPreference.getBoolean(
		        MetaData.STYLUS_ONLY_STATUS, false);
		setStylusInputOnly(isPenOnly);
	}

	@Override
	protected void onResume() {
		Log.v(MetaData.DEBUG_TAG, "onResume");
		//emmanual to fix bug 519927, 5199291, 519931, 518870
		mEditorUiUtility.getPageEditor().getDoodleView().dismissDeleteDialog();

		super.onResume();
		if (isFinishing()) {
			return;
		}
		
		//emmanual to fix bug 495438
		if (mBookCase != null && mNotePage != null
		        && mBookCase.getNoteBook(mNotePage.getOwnerBookId()) == null) {
			finish();
			return;
		}
		// BEGIN james5
		acquireWakeLock();
		// END james5
		
		//smilefish add for runtime permission
		if(MethodUtils.needShowPermissionPage(getApplicationContext())){
			MethodUtils.showPermissionPage(this, true);
		}else{
			loadData();
		}
	}
	
	//Begin Emmanual:hide System UI for pen only mode
	private void setStylusInputOnly(boolean penonly) {
		mEditorUiUtility.setmIsStylusInputOnly(penonly);
		changeSystemUIState(penonly);
	}
	
	private void changeSystemUIState(boolean penonly){
		if (penonly && MethodUtils.isPenDevice(getApplicationContext())) {
			hideSystemUI();
		} else {
			showSystemUI();
		}
	}
	
	private void hideSystemUI() {
		View view = findViewById(R.id.totalLayoutView);
		view.setSystemUiVisibility(
		        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//		                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//		                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//		                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
		                | View.SYSTEM_UI_FLAG_IMMERSIVE
		                );
	}

	private void showSystemUI() {
		View view = findViewById(R.id.totalLayoutView);
		int systemUiVisibility = view.getSystemUiVisibility();
		int hideNavigation = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		if (systemUiVisibility % (2 * hideNavigation) >= hideNavigation) {
			view.setSystemUiVisibility(systemUiVisibility - hideNavigation);
		}

	}
	//End Emmanual

	// Beign Allen
	public void checkVoiceInput() {
		if (mStartVoiceInput && getIntent().getSourceBounds() == null) {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
			        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 6);
			try {
				startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mStartVoiceInput = false;
		}
	}

	// End Allen

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mEditorUiUtility != null) {
			// begin smilefish
			mCursorPos = mEditorUiUtility.getCursorPos();
			mScrollBarX = mEditorUiUtility.getScrollBarX();
			mScrollBarY = mEditorUiUtility.getScrollBarY();
			// end smilefish
			outState.putInt("cursor position", mEditorUiUtility.getCursorPos());
			outState.putBoolean("textimage_modified_status", mIsTextImgEdit); // by
			                                                                  // show
			outState.putBoolean("eraser_button_status", mIsEraserOn); // by show
		}
		outState.putLong(MetaData.BOOK_ID, mBookId);// Richard
		outState.putLong(MetaData.PAGE_ID, mPageId);// Richard
		mIsShowingInsertDialog = false; // save data for rotation [Carol]
		if (dlg != null && dlg.isShowing()) {
			mIsShowingInsertDialog = true;
			dlg.dismiss();
		}
		outState.putBoolean("insert_dialog_status", mIsShowingInsertDialog);
		outState.putBoolean("insert_dialog_type", mInsertStamp);
		outState.putBoolean("first_voice_input", mStartVoiceInput); // carol
		
		outState.putInt("current_edittext_id", 
				mEditorUiUtility.getPageEditor().getCurrentEditor().getId());//emmanual

		//emmanual to fix bug 411589
		TemplateEditText attendee = (TemplateEditText) findViewById(R.id.attendee_edit);
		if(attendee != null){
			outState.putString("attendee_nametext", attendee.getText().toString());
		}
		
		if(selectPdfDialog != null && selectPdfDialog.isShowing()){
			outState.putBoolean("select_pdf_dialog_show", true);
			Bitmap[] bitmaps = new Bitmap[microBmpList.size()];
			for (int i = 0; i < microBmpList.size(); i++) {
				bitmaps[i] = microBmpList.get(i);
			}
			outState.putParcelableArray("select_pdf_bitmaps", bitmaps);

			boolean[] checks = new boolean[pdfCheckList.size()];
			for (int i = 0; i < pdfCheckList.size(); i++) {
				checks[i] = pdfCheckList.get(i);
			}
			outState.putBooleanArray("select_pdf_checks", checks);
			outState.putString("select_pdf_path", mReadpdfPath);
		}
	}

	// begin darwin
	private void saveBookCoverThumb(NoteBook book, PageEditor pe) {
		try {
			Bitmap bitmap = pe.getThumbnail(mNoteBook.getBookColor(),
			        mNoteBook.getGridType(), false);// mNotePage.getPageColor(),
			                                        // mNotePage.getPageStyle()
			if (bitmap != null) {
				File file = new File(book.getBookPath(),
				        MetaData.THUMBNAIL_PREFIX);
				if (file.exists() == false) {
					file.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				bitmap.compress(Bitmap.CompressFormat.PNG,
				        MetaData.THUMBNAIL_QUALITY, bos);
				bitmap.recycle();
				bitmap = null;
				bos.close();
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// end darwin
	// BEGIN: Better
	private void saveThumbnail(PageEditor pe) {
		saveAPThumbnail(pe);
		if(MetaData.IS_ENABLE_WIDGET_THUMBNAIL)
			saveWidgetThumbnail(pe);
		if (MethodUtils.isEnableAirview(getApplicationContext())) {
			saveAirViewThumbnail(pe);// Allen

		}
	}

	private void saveAirViewThumbnail(PageEditor pe) {

		try {
			Bitmap bitmap = pe.getAirViewThumbnail(mNoteBook.getBookColor(),
			        mNoteBook.getGridType());
			if (bitmap != null) {
				File file = new File(getFilePath(), MetaData.AIRVIEW_PREFIX);
				if (file.exists() == false) {
					file.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				bitmap.compress(Bitmap.CompressFormat.PNG,
				        MetaData.THUMBNAIL_QUALITY, bos);
				bitmap.recycle();
				bitmap = null;
				bos.close();
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveWidgetThumbnail(PageEditor pe) {

		try {
			Bitmap bitmap = pe.getWidgetThumbnail(mNoteBook.getBookColor(),
			        mNoteBook.getGridType(), true);// mNotePage.getPageColor(),
			                                       // mNotePage.getPageStyle()
			if (bitmap != null) {
				File file = new File(getFilePath(), MetaData.THUMBNAIL_WIDGET);
				if (file.exists() == false) {
					file.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				bitmap.compress(Bitmap.CompressFormat.PNG,
				        MetaData.THUMBNAIL_QUALITY, bos);
				bitmap.recycle();
				bitmap = null;
				bos.close();
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveAPThumbnail(PageEditor pe) {

		try {
			Bitmap bitmap = pe.getThumbnail(mNoteBook.getBookColor(),
			        mNoteBook.getGridType(), true);// mNotePage.getPageColor(),
			                                       // mNotePage.getPageStyle()
			if (bitmap != null) {
				File file = new File(getFilePath(), MetaData.THUMBNAIL_PREFIX);
				if (file.exists() == false) {
					file.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				bitmap.compress(Bitmap.CompressFormat.PNG,
				        MetaData.THUMBNAIL_QUALITY, bos);
				bitmap.recycle();
				bitmap = null;
				bos.close();
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// END: Better

	// BEGIN: Better
	public void dismissClipboard() {
		if (MetaData.HasMultiClipboardSupport && (mClipboardManager != null)) {
			try {
				if (MultiClipboardHelper.isClipboardOpened(mClipboardManager)) {
					MultiClipboardHelper.closeClipboard(mClipboardManager);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// END: Better

	private void OnSavePage() {
		Log.i(MetaData.DEBUG_TAG, "OnSavePage start");
		PageEditor pe = mEditorUiUtility.getPageEditor();
		if (pe == null || !pe.beginSave()) // Carol-monkey test(check if
		                                   // pe==null)
		{
			return;
		}
		long id = mNotePage.getCreatedTime();
		if (pe.getNotePage() == null || id == pe.getNotePage().getCreatedTime()) {
			if (pe.isDoodleModified() || pe.isEditTextModified()) {
				Log.i(MetaData.DEBUG_TAG, "OnSavePage start need save");

				MetaData.SavingPageIdList.add(id);

				saveThumbnail(pe);
				long bookId = mNotePage.getOwnerBookId();
				NoteBook book = mBookCase.getNoteBook(bookId);
				//emmanual to fix bug 500587
				if(book == null){
					return ;
				}
				long pageid = book.getPageOrder(0);
				if (mNotePage.getCreatedTime() == pageid) {
					saveBookCoverThumb(book, pe);
					if(!MetaData.CoverChangedBookIdList.contains(bookId)){
					    MetaData.CoverChangedBookIdList.add(bookId); //for widget thumbnail
					}
				}
				// Begin Allen
				ArrayList<NoteItemArray> noteItems = null;
				DoodleItem doodleItem = null;
				if (mNotePage.getVersion() == 1) {
					noteItems = pe.getNoteItem();
					doodleItem = pe.getDoodleItem(mNotePage);

				} else {
					if (pe.isEditTextModified()) {
						noteItems = pe.getNoteItem();
					}
					if (pe.isDoodleModified()) {
						doodleItem = pe.getDoodleItem(mNotePage);
					}
				}
				// End Allen

				SavePage(getApplicationContext(), noteItems, doodleItem, id, pe);

				pe.cleanEdited(true);// Allen
				Log.i(MetaData.DEBUG_TAG, "OnSavePage save over");

			} else {
				// Begin Allen++ create default thumbnail
				String pagePath = getFilePath();
				File thumbFile = new File(pagePath, MetaData.THUMBNAIL_PREFIX);
				if (!thumbFile.exists()) {
					saveAPThumbnail(pe);
				}

				long bookId = mNotePage.getOwnerBookId();
				NoteBook book = mBookCase.getNoteBook(bookId);
				// begin noah;for share
				if (book == null) {
					this.finish();
					return;
				}
				// end noah; for share
				String bookPath = book.getBookPath();
				File bookThumbFile = new File(bookPath,
				        MetaData.THUMBNAIL_PREFIX);
				if (!bookThumbFile.exists()) {
					long pageid = book.getPageOrder(0);
					if (mNotePage.getCreatedTime() == pageid) {
						saveBookCoverThumb(book, pe);
					}
				}
				if (MethodUtils.isEnableAirview(getApplicationContext())) {
					File airviewFile = new File(getFilePath(),
					        MetaData.AIRVIEW_PREFIX);
					if (!airviewFile.exists()) {
						saveAirViewThumbnail(pe);
					}
				}
			}
			// End Allen
			EnableSaveButton(false);
			mSaveButtonStatus = this.SAVE_BUTTON_DISABLE;
			Log.i(MetaData.DEBUG_TAG, "OnSavePage finish");
		} else {
			String string = "";
			if (pe.getNotePage() != null)
				string = pe.getNotePage().getCreatedTime() + "";
			Log.i(MetaData.DEBUG_TAG,
			        "do not save current pe's notepage is not equal to mNotepage"
			                + id + " pe's " + string);
		}
		pe.endSave();
		deleteStampTempDir();// by jason
	}

	// begin jason
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		dismissAllPopupWindow();
	}

	// end
	@Override
	protected void onPause() {
		if(MethodUtils.needShowPermissionPage(getApplicationContext())){
			super.onPause();
			return;
		}

		if (mDialog != null && mDialog.isShowing()) {
			MetaData.PAUSE_AUTO_PAGING = true;
		}
		//emmanual
		recordInputMethodTime();
		mRecordTime = 0;
		
		Log.v(MetaData.DEBUG_TAG, "onPause start");
		// begin jason
		if (mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_SCRIBBLE) {
			mEditorUiUtility.flushUnStoreData();
		}
		// end jason
		// BEGIN: Better
		mIsAutoSaveNeeded = false;
		mAutoSaveTimer.cancel();
		// END: Better

		// BEGIN, james5
		releaseWakeLock();
		// END, james5
		// add by mars for preformance
		if (mSharedPreference != null) {
			Editor editor = mSharedPreference.edit();
			setColorBrushStrokeToPreference(editor);
			setEditorTextStyleAndColorToPreference(editor);
			setWTAndShapeButtonStatusFromPreference(editor);// RICHARD
			editor.commit();
		}
		setAutoRecognizerShapeState(true);// RICHARD
		mEditorUiUtility.clearSelfWindow();// by jason
		// Begin: show_wang@asus.com
		// Modified reason: for dds
		mSharedPreference
		        .edit()
		        .putInt(MetaData.PREFERENCE_CURRENT_INPUT_MODE,
		                mEditorUiUtility.getInputMode()).commit();
		int currentMode = mSharedPreference.getInt(
		        MetaData.PREFERENCE_CURRENT_INPUT_MODE,
		        InputManager.INPUT_METHOD_SCRIBBLE);
		boolean isTextImgSelectionText = mSharedPreference.getBoolean(
		        MetaData.PREFERENCE_IS_TEXTIMG_SELECTION_TEXT, false);
		switch (currentMode) {
		case InputManager.INPUT_METHOD_SELECTION_TEXT:
			if (isTextImgSelectionText) {
				mEditorUiUtility.saveBoxEditTextContent();
				mEditorUiUtility.saveSelectionDrawInfo();
			}
			break;
		case InputManager.INPUT_METHOD_INSERT:
			mEditorUiUtility.saveSelectionDrawInfo();
			break;
		case InputManager.INPUT_METHOD_SELECTION_DOODLE:
			mEditorUiUtility.saveSelectionDrawInfo();
			if (isTextImgSelectionText) {
				mSharedPreference
				        .edit()
				        .putBoolean(
				                MetaData.PREFERENCE_IS_TEXTIMG_SELECTION_TEXT,
				                false).commit();
			}
			break;
		case InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD:
		case InputManager.INPUT_METHOD_TEXT_IMG_SCRIBBLE:
			mEditorUiUtility.saveBoxEditTextContent();
			mEditorUiUtility.saveSelectionDrawInfo();
			if (isTextImgSelectionText) {
				mSharedPreference
				        .edit()
				        .putBoolean(
				                MetaData.PREFERENCE_IS_TEXTIMG_SELECTION_TEXT,
				                false).commit();
			}
			break;
		default:
			if (isTextImgSelectionText) {
				mSharedPreference
				        .edit()
				        .putBoolean(
				                MetaData.PREFERENCE_IS_TEXTIMG_SELECTION_TEXT,
				                false).commit();
			}
			break;
		}
		// End: show_wang@asus.com
		savePage();// darwin

		clearPageThumbBitmapList();

		// BEGIN: Better
		dismissClipboard();
		// END: Better
		// Begin Allen for update widget
		if (MetaData.SuperNoteUpdateInfoSet.size() > 0) {
			Intent updateIntent = new Intent();
			updateIntent.setAction(MetaData.ACTION_SUPERNOTE_UPDATE);
			updateIntent.putExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM,
			        MetaData.SuperNoteUpdateInfoSet);
			sendBroadcast(updateIntent);
			MetaData.SuperNoteUpdateInfoSet.clear();
		}
		// End Allen
		LoadPageTaskVersionTwo loadPageTask = mEditorUiUtility
		        .getLoadPageTask();
		
	    KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);   

		if (loadPageTask != null && loadPageTask.isTaskRunning()
				&& !mKeyguardManager.inKeyguardRestrictedInputMode()) { //smilefish fix bug 789847
			loadPageTask.cancel(true);
		}

		mEditorUiUtility.quitSelectionTextMode();
		super.onPause();
		Log.v(MetaData.DEBUG_TAG, "onPause finish");
		
		//emmanual to fix bug 434720
		if (!mSharedPreference.getBoolean(
		        getResources().getString(R.string.pref_first_load), true)
		        && mPauseForHome && getIntent().getExtras() != null
		        && getIntent().getExtras().containsKey(MetaData.PHONE_TIME)
		        && getIntent().getExtras().containsKey(MetaData.PHONE_WHO)
		        //emmanual to fix bug 474398, 499881
		        && getResources().getConfiguration().orientation == s_orientation
		        //emmanual to fix bug 492244
		        && !mSharedPreference.getBoolean(getResources()
				        .getString(R.string.pref_prompt_handwriting_animating),
				        true)){
			finish();
		}
		mPauseForHome = true;		
	}

	// BEGIN: archie_huang@asus.com
	@Override
	protected void onDestroy() {
		// BEGIN: Better
		MetaData.CurrentEditPageId = -1;

		//Begin by Emmanual
		if(MetaData.IS_GA_ON)
		{
			GACollector gaCollector = new GACollector(this);
			gaCollector.inputMethodTime(InputManager.INPUT_METHOD_KEYBOARD, mKeyboardTime);
			gaCollector.inputMethodTime(InputManager.INPUT_METHOD_SCRIBBLE, mScribbleTime);
			gaCollector.inputMethodTime(InputManager.INPUT_METHOD_DOODLE, mDoodleTime);
		}
		//End

		if (mAutoSaveTimer != null) {
			mAutoSaveTimer.cancel();
			mAutoSaveTimer = null;
		}
		// END: Better

		// BEGIN: archie_huang@asus.com
		// To avoid memory leak
		mActivityToast = null;
		if (mDefaultPageThumb != null && !mDefaultPageThumb.isRecycled()) {
			mDefaultPageThumb.recycle();
			mDefaultPageThumb = null;
		}

		// END: archie_huang@asus.com
		if (mEditorUiUtility != null) {
			mEditorUiUtility.unbindResources();
		}
		
		if(mPageWidget != null){ //smilefish
			mPageWidget.recycleBitmap();
		}

		unregisterReceiver(updateUiReceiver);

		// BEGIN: RICHARD
		if (mIndexServiceClient != null)// darwin
		{
			mIndexServiceClient.doUnbindService(this);
		}
		// END: RICHARD
		// begin jason
		unregisterReceiver(mBroadCastReceiver);
		// end jason
		deleteTemp();// noah;7.15
		
		super.onDestroy();
	}// END; archie_huang@asus.com

	/**
	 * 删除临时目录
	 * 
	 * @author noah
	 */
	private void deleteTemp() {
		try {
			File tempFile = new File(MetaData.TEMP_DIR);
			if (tempFile.exists()) {
				tempFile.delete();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Boolean isConfig = true;
		return isConfig;
	}

	// BEGIN: archie_huang@asus.com
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// BEGIN: james5_chan@asus.com
		if (requestCode == VIEW_BOOKMARK_REQUEST && resultCode == RESULT_OK) {
			Long bookId = data.getLongExtra(MetaData.BOOK_ID, 0L);
			Long pageId = data.getLongExtra(MetaData.PAGE_ID, 0L);
			if (bookId != 0L && pageId != 0L) {
				mBookId = bookId;
				mPageId = pageId;
				mNoteBook = mBookCase.getNoteBook(mBookId);
				mNotePage = mNoteBook.getNotePage(mPageId);
				setMenuItemBookmark();
				boolean loadSucceed = mEditorUiUtility.loadPage(mNotePage,
				        false);
				if (!loadSucceed) {
					fileFormatError();
				}
			} else { // Better
				mNotePage = mNoteBook.getNotePage(mPageId);
				if (mNotePage == null) {
					finish();
					return;
				}
				setMenuItemBookmark();
			}
		}// END: james5_chan@asus.com
		 // Begin: show_wang@asus.com
		 // Modified reason: for dds
		else if (requestCode == InputManager.RESULT_VOICE
		        && (resultCode == RESULT_OK)) {
			if (!mIsConfig) {
				PageEditorManager pageEditorManager = mEditorUiUtility
				        .getPageEditorManager();
				VoiceAttacher.attachItem(data, pageEditorManager);
				mEditorUiUtility.reflashScreen();
			} else {
				mIsInsertVoiceAttacher = true;
				mIntent = data;
			}
			mIsResumeFromCropActivity = true;// darwin
		}
		//Emmanual
		else if (requestCode == InputManager.RESULT_VOICEFILE
		        && (resultCode == RESULT_OK)) {
			if (!mIsConfig) {
				PageEditorManager pageEditorManager = mEditorUiUtility
				        .getPageEditorManager();
				if(isFromGoogle(data)){
					downloadFromGoogle(data, pageEditorManager, DownloadFromGoogleTask.INSERT_VOICE_CODE);
					return;
				}
				VoiceAttacher.attachFile(data, pageEditorManager);
				mEditorUiUtility.reflashScreen();
			} else {
				mIsInsertVoiceFileAttacher = true;
				mIntent = data;
			}
			mIsResumeFromCropActivity = true;// darwin
		} else if (requestCode == InputManager.RESULT_VIDEO
		        && (resultCode == RESULT_OK)) {
			if (!mIsConfig) {
				PageEditorManager pageEditorManager = mEditorUiUtility
				        .getPageEditorManager();
				
				//begin smilefish fix bug 373033
				if(mFirstLoadData){
					mVideoData = data;
				}
				else{
					VideoAttacher.attachItem(data, pageEditorManager);
				}
				//end smilefish
			} else {
				mIsInsertVideoAttacher = true;
				mIntent = data;
			}
			mIsResumeFromCropActivity = true;// darwin
		} else if (requestCode == InputManager.RESULT_GALLERY
		        && (resultCode == RESULT_OK)) {
			//emmanual
			if(data == null){
				return ;
			}
			if (!mIsConfig) {
				PageEditorManager pageEditorManager = mEditorUiUtility
				        .getPageEditorManager();
				
				//emmanual
				if(isFromGoogle(data)){
					downloadFromGoogle(data, pageEditorManager, DownloadFromGoogleTask.INSERT_IMAGE_CODE);
					return;
				}
				
				if(mFirstLoadData){
					mGalleryData = data;
				}
				else{
					GalleryAttacher.attachItem(data, pageEditorManager);
				}
			} else {
				mIsInsertGalleryAttacher = true;
				mIntent = data;
			}
			mIsResumeFromCropActivity = true;// darwin
		} else if (requestCode == InputManager.RESULT_CAMERA
		        && (resultCode == RESULT_OK)) {
			if (!mIsConfig) {
				PageEditorManager pageEditorManager = mEditorUiUtility
				        .getPageEditorManager();
				//begin emmanual fix bug 392684
				if(mFirstLoadData){
					mPhotoData = true;
				}
				else{
					CameraAttacher.attachItem(pageEditorManager);
				}
				//end emmanual
				
			} else {
				mIsInsertCameraAttacher = true;
			}
			mIsResumeFromCropActivity = true;// darwin
		} else if (requestCode == InputManager.RESULT_TEXT
		        && (resultCode == RESULT_OK)) {
			if (!mIsConfig) {
				MetaData.INDEX_CURRENT_LANGUAGE = NoteBookPickerActivity.sLanguageHelper
				        .getRecordIndexLaguage();
				PageEditorManager pageEditorManager = mEditorUiUtility
				        .getPageEditorManager();
				// begin emmanual fix bug 398067
				if (mFirstLoadData) {
					mTextfileData = data;
				} else {
					TextFileAttacher.attachItem(data, pageEditorManager);
				}
				// emmanual
			} else {
				mIsInsertTextFileAttacher = true;
				mIntent = data;
			}
			mIsResumeFromCropActivity = true;// darwin
		} else if (requestCode == InputManager.RESULT_VIEW) {
		}// END ryan_lin@asus.com
		 // End: show_wang@asus.com

		// BEGIN:Show
		else if (requestCode == CROP_IMAGE_REQUEST) {
			if (resultCode == RESULT_OK) {
				if (!data.hasExtra("FileName"))
					return;
				String fileName = data.getStringExtra("FileName");
				// Begin Darwin_Yu@asus.com
				if (!data.hasExtra("OriginalFileName"))
					return;
				String originalFileName = data
				        .getStringExtra("OriginalFileName");
				// End Darwin_Yu@asus.com
				if (data.getBooleanExtra("IsConfig", false)) {
					mIsCropConfig = true;
					cropFileName = fileName;
					cropOrgFileName = originalFileName;
				} else {
					if (fileName != null) {
						mEditorUiUtility.updateCropDrawInfo(fileName,
						        originalFileName);
					}
				}
				mEditorUiUtility.setInputMode(mEditorUiUtility
				        .getInputModeFromPreference());
				mEditorUiUtility.setDoodleTool(mDoodleToolCode);
			}
			mIsResumeFromCropActivity = true;// darwin
		}
		// Begin Allen++ for template
		else if (requestCode == RESULT_CHANGEGALLERY) {
			isGallaryPickerShowing = false;
			if (resultCode == RESULT_OK) {
				//emmanual to fix bug 390945
				if(data == null){
					return ;
				}
				
				//smilefish fix bug 661669
				PageEditorManager pageEditorManager = mEditorUiUtility
				        .getPageEditorManager();
				if(isFromGoogle(data)){
					downloadFromGoogle(data, pageEditorManager, DownloadFromGoogleTask.CHANGE_IMAGE_CODE);
					return;
				}
				
				String fileFullPath = PickerUtility.getRealFilePathForImage(
				        EditorUiUtility.getContextStatic(), data.getData());
				startCropImage(fileFullPath);
			}
			return;

		} else if (requestCode == CROP_IMAGE_REQUEST_TEMPLATE) {
			if (resultCode == RESULT_OK) {
				if (!data.hasExtra("FileName"))
					return;

				String fileName = data.getStringExtra("FileName");
				// Begin Darwin_Yu@asus.com
				if (!data.hasExtra("OriginalFileName"))
					return;
				String originalFileName = data
				        .getStringExtra("OriginalFileName");
				// End Darwin_Yu@asus.com
				// Begin: show_wang@asus.com
				// Modified reason: for dds
				if (data.getBooleanExtra("IsConfig", false)) {
					mIsChangeGraphicCropConfig = true;
					changeGarphicCropFileName = fileName;
				// End: show_wang@asus.com
				} else {
					//emmanual fix bug 460810, 484265, 486870, 452510
					if (mFirstLoadData) {
						mTravelFileName = fileName;
					} else {
						Bitmap bmp = BitmapFactory.decodeFile(getFilePath() + "/"
						        + fileName);// modified by show
						((ImageView) findViewById(R.id.travel_image))
						        .setImageBitmap(bmp);
						((TemplateImageView) findViewById(R.id.travel_image))
						        .setImageFilePath(fileName);
					}
				}
				mEditorUiUtility.getPageEditor().setIsAttachmentModified(true);
				mEditorUiUtility.getPageEditor().getAttachmentNameList()
				        .add(fileName);
				boolean bIsRemoved = false;
				for (String name : mEditorUiUtility.getPageEditor()
				        .getAttachmentNameList()) {
					if (name.equalsIgnoreCase(originalFileName)) {
						mEditorUiUtility.getPageEditor()
						        .getAttachmentNameList()
						        .remove(originalFileName);
						bIsRemoved = true;
					}
				}
				if (!bIsRemoved) {
					mEditorUiUtility.getPageEditor()
					        .getAttachmentRemoveNameList()
					        .add(originalFileName);
				}
			}else{
				if (data != null && data.hasExtra("OutOfMemoryError")) { //fix google play error
					// emmanual to fix bug 579037, 579254
					final View view = View.inflate(this,
					        R.layout.one_msg_dialog, null);
					final TextView textView = (TextView) view
					        .findViewById(R.id.msg_text_view);
					textView.setText(R.string.no_memory_dialog_content);

					new AlertDialog.Builder(this,
					        AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
					        // add by mars for AMAX THEME
					        .setIconAttribute(android.R.attr.alertDialogIcon)
					        .setTitle(android.R.string.dialog_alert_title)
					        .setView(view)
					        .setPositiveButton(
					                this.getString(android.R.string.ok), null)
					        .show();
				}
			}
		}
		// End Allen++ for template
		else if (requestCode == CONTACT_PICKER_RESULT) {
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = getContentResolver().query(contactData, null, null,
				        null, null);
				if (c.moveToFirst()) {
					String name = c
					        .getString(c
					                .getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
					if (name != null) {
						TemplateEditText attendee = (TemplateEditText) findViewById(R.id.attendee_edit);

						attendee.append(name + ",");
						
						attendee.requestFocus();
						mEditorUiUtility.getPageEditor().ScrollViewTo(0, 0,
						        true);
						attendee.setIsModified(true);
						
						//emmanual to fix bug 411589
						if(getConfigStatus()){
							mAttendeeText = attendee.getText();
						}
					}
				}
				c.close();// RICHARD FIX MEMORY LEAK
			}
		} else if (requestCode == CONTINIOUS_LINE_TIPS_REQUEST){
			if (resultCode == Activity.RESULT_OK) { // Better
				mEditorUiUtility.addContent();
			}
			//emmanual to fix bug 458182
			if (isShareMode()) {
				final Intent shareIntent = getShareIntent();
				boolean isShared = shareIntent.getBooleanExtra(EXTRA_IS_SHARED,
				        false);
				if (!isShared) {
					shareIntent.putExtra(EXTRA_IS_SHARED, true);
					new Handler().post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							addShareAttacher(shareIntent);
						}
					});
				}
			}
		} else if ((requestCode == REQUEST_IMPORT_PDF)
		        && (resultCode == Activity.RESULT_OK)) { // Better
			if(isFromGoogle(data)){
				downloadFromGoogle(data, null, DownloadFromGoogleTask.INSERT_PDF_CODE);
				return;
			}
			
			String path = PickerUtility.getRealFilePath(EditorActivity.this, data.getDataString());
			if(path == null){
				if (data.getData().toString().contains("content://")) {
					Cursor cursor = null;
					try {
						cursor = getContentResolver().query(data.getData(),
								null, null, null, null);
						cursor.moveToFirst();
						String s = cursor.getString(cursor.getColumnIndex("_data"));
						path = s;
						cursor.close();
					} catch (Exception e) {
						Builder builder = new AlertDialog.Builder(EditorActivity.this);
						builder.setMessage(R.string.reading_pdf_error);
						builder.setTitle(R.string.error);
						builder.setPositiveButton(android.R.string.ok, null);
						builder.create().show();
					}finally {
					    if (cursor != null) //smilefish fix memory leak
					        cursor.close();
					}
				} else {
					path = data.getData().getPath();
				}
				path = Uri.decode(path);
			}

			if (!mIsReadPdfTaskRunning) {
				mIsReadPdfTaskRunning = true;
				PickerUtility.lockRotation(EditorActivity.this);
				mReadPdfTask = new ReadPdfTask(path);
				mReadPdfTask.execute();
			}

		}
		// Begin Allen
		else if ((requestCode == VOICE_RECOGNITION_REQUEST_CODE)
		        && (resultCode == Activity.RESULT_OK)) {
			ArrayList<String> matches = data
			        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (matches != null && matches.size() > 0) {
				String result = matches.get(0);
				if (result.trim().length() != 0) {
					PageEditorManager pageEditorManager = mEditorUiUtility
					        .getPageEditorManager();
					pageEditorManager.getCurrentPageEditor()
					        .onVoiceRecognitionResult(result);
				}
			}
		}
		// End Allen
		// END:Show
		else if(requestCode == MethodUtils.REQUEST_PERMISSION_PAGE){ //smilefish add for runtime permission
			if(resultCode == RESULT_OK){
				loadData();
			}
		}
		else if(requestCode == NAVIGATOR_TUTORIAL_REQUEST){ //smilefish add for navigator tutorial
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					mEditorUiUtility.showSoftKeyboard();
				}
			}, 500);
		}
	} // END; archie_huang@asus.com

	// BEGIN: Better

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case IMPORT_PDF_PROGRESS_DIALOG: {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setCancelable(false);
			DialogInterface.OnClickListener listener = null;
			dialog.setButton(Dialog.BUTTON_NEGATIVE,
			        getResources().getString(android.R.string.cancel), listener);
			return dialog;
		}
		case DRAWING_PROGRESS_DIALOG: {
			ProgressDialog drawingDialog = new ProgressDialog(this,
			        R.style.progress_dialog);
			drawingDialog.setCanceledOnTouchOutside(false);
			drawingDialog.setCancelable(false);
			drawingDialog.setMessage(getString(R.string.loading_page));
			drawingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			return drawingDialog;
		}
		case LOADING_PROGRESS_DIALOG: {
			ProgressDialog loadingDialog = new ProgressDialog(this,
			        R.style.progress_dialog);
			loadingDialog.setCanceledOnTouchOutside(false);
			loadingDialog.setCancelable(false);
			loadingDialog.setMessage(getString(R.string.loading_page));
			loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			return loadingDialog;
		}
		}

		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		super.onPrepareDialog(id, dialog, args);
		switch (id) {
		case IMPORT_PDF_PROGRESS_DIALOG: {
			ProgressDialog d = (ProgressDialog) dialog;

			ProgressBar v = (ProgressBar) d.findViewById(android.R.id.progress);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v
			        .getLayoutParams();
			params.gravity = Gravity.CENTER_HORIZONTAL;
			params.width = LayoutParams.MATCH_PARENT;
			v.setLayoutParams(params);

			final Button b = d.getButton(Dialog.BUTTON_NEGATIVE);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if ((mImportPdfTask != null) && mIsImportPdfTaskRunning) {
						mImportPdfTask.cancel(true);
						b.setEnabled(false);
					}
				}

			});
			break;
		}
		}
	}

	//Emmanual
	private boolean isFromGoogle(Intent data) {
		Uri uri = data.getData();
		if (uri != null
		        && (uri.toString().startsWith(
		                "content://com.google.android.apps.docs.storage") || uri
		                .toString()
		                .startsWith(
		                        "content://com.google.android.apps.photos.content"))) {
			return true;
		}
		return false;
	}
	
	private void downloadFromGoogle(Intent data,
	        PageEditorManager pageEditorManager, int code) {
		// emmanual to check network
		if (!MetaData.isNetworkAvailable(EditorActivity.this)) {
			EditorActivity.showToast(EditorActivity.this,
			        R.string.sync_setting_networkless);
			return;
		}

		Uri uri = data.getData();
		Cursor cursor = null;
		try {
			String path = null;
			cursor = getContentResolver().query(uri, null, null, null, null);
			if (cursor.getCount() == 0) {
				cursor.close();
				return;
			}
			cursor.moveToFirst();
			if(cursor.getString(0).contains(".")){ //image from google photos
				path = MetaData.CROP_TEMP_DIR + cursor.getString(0);
			}else if(cursor.getString(1).contains(".")){ //image from goole photos
				path = MetaData.CROP_TEMP_DIR + cursor.getString(1);
			}else if(cursor.getString(2).contains(".")){ //image from goole drive
				path = MetaData.CROP_TEMP_DIR + cursor.getString(2);
			}else{
				path = MetaData.CROP_TEMP_DIR + "image.jpg";
			}

			DownloadFromGoogleTask task = new DownloadFromGoogleTask(uri, path,
			        pageEditorManager, code);
			task.execute();
		} catch (Exception e) {
			Log.e(TAG, "getRealFilePath error");
		} finally {
			if (cursor != null) {
				cursor.close();
				}
			}
			return;
	}
	
	private void startCropImage(String fileFullPath){
		if (fileFullPath == null) {
			showToast(this, R.string.wrong_file_type);
			return;
		}
		String fileName = fileFullPath.substring(
				fileFullPath.lastIndexOf("/") + 1,
				fileFullPath.length());
		String filePath = fileFullPath.substring(0,
				fileFullPath.lastIndexOf("/"));
		if (fileName != null) {
			try {
				Intent intent = new Intent();
				intent.putExtra("filePath", filePath);
				intent.putExtra("fileName", fileName);
				intent.putExtra("pageFilePath", getFilePath());
				intent.putExtra("IsTemplate", 1);
				if (isPhoneScreen()) {
					intent.putExtra("IsPhoneSizePage", 1);
				}

				intent.setClass(EditorActivity.this,
				        CropImageActivity.class);
				startActivityForResult(intent,
				        CROP_IMAGE_REQUEST_TEMPLATE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static boolean mIsDownloadTaskRunning = false;

	private class DownloadFromGoogleTask extends AsyncTask<Void, Void, String> {
		public static final int INSERT_PDF_CODE = 2001;
		public static final int INSERT_IMAGE_CODE = 2002;
		public static final int INSERT_VOICE_CODE = 2003;
		public static final int CHANGE_IMAGE_CODE = 2004;
		public static final int SHARE_IMAGE_CODE = 2005;
		
		private Uri mUri;
		private String mPath;
		private int mCode;
		private ProgressDialog mProgressDialog;
		private PageEditorManager mPageEditorManager;

		public DownloadFromGoogleTask(Uri uri, String path,
		        PageEditorManager pageEditorManager, int code) {
			mUri = uri;
			mPath = path;
			mPageEditorManager = pageEditorManager;
			mCode = code;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsDownloadTaskRunning = true;
			PickerUtility.lockRotation(EditorActivity.this);
			mProgressDialog = new ProgressDialog(EditorActivity.this);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setMessage(EditorActivity.this.getString(R.string.download_title));
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
						DownloadFromGoogleTask.this.cancel(true);
				}
			};
			mProgressDialog.setButton(Dialog.BUTTON_NEGATIVE,
					EditorActivity.this.getString(android.R.string.cancel), listener);
			mProgressDialog.create();
			mProgressDialog.show();
		}

		@Override
        protected String doInBackground(Void... arg0) {
			try {
				File file = new File(mPath);
				InputStream stream = EditorActivity.this
				        .getContentResolver().openInputStream(mUri);
				File dir = new File(MetaData.CROP_TEMP_DIR);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				convertStreamToFile(stream, file);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return mPath;
        }
		
		private void convertStreamToFile(InputStream ins, File file) {
			try {
				if(file.exists()){
					file.delete();
				}
				file.createNewFile();
				OutputStream os = new FileOutputStream(file);
				int bytesRead = 0;
				byte[] buffer = new byte[8192];
				while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
					os.write(buffer, 0, bytesRead);
				}
				os.close();
				ins.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
        protected void onCancelled(String result) {
	        super.onCancelled(result);
	        mIsDownloadTaskRunning = false;
			PickerUtility.unlockRotation(EditorActivity.this);
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}
        }

		@Override
        protected void onPostExecute(String result) {
	        super.onPostExecute(result);
			if (mProgressDialog != null && mProgressDialog.isShowing()
			        && EditorActivity.this != null
			        && !EditorActivity.this.isFinishing()) {
				try{
					mProgressDialog.cancel();
				}catch(Exception e){  //smilefish fix bug 568240
					e.printStackTrace();
				}
			}
			mIsDownloadTaskRunning = false;
			switch (mCode) {
			case INSERT_PDF_CODE:
				if (!mIsReadPdfTaskRunning) {
					mIsReadPdfTaskRunning = true;
					mReadPdfTask = new ReadPdfTask(mPath);
					mReadPdfTask.execute();
				}
				break;
			case INSERT_IMAGE_CODE:
				GalleryAttacher.attachItem(mPath, mPageEditorManager);
				PickerUtility.unlockRotation(EditorActivity.this);
				break;
			case INSERT_VOICE_CODE:
				VoiceAttacher.attachFile(mPath, mPageEditorManager);
				mEditorUiUtility.reflashScreen();
				PickerUtility.unlockRotation(EditorActivity.this);
				break;
			case CHANGE_IMAGE_CODE:
				startCropImage(mPath);
				PickerUtility.unlockRotation(EditorActivity.this);
				break;
			case SHARE_IMAGE_CODE:
				GalleryAttacher.attachItem(mPath, mPageEditorManager);
				PickerUtility.unlockRotation(EditorActivity.this);
				break;
			default:
				break;
			}
        }
	}
	
	private static boolean mIsReadPdfTaskRunning = false;
	private static ReadPdfTask mReadPdfTask = null;

	private class ReadPdfTask extends AsyncTask<Void, Void, List<String>> {
		private String mPath;
		private List<String> pdfBitmapList;
		private ProgressDialog mProgressDialog;
		private boolean error;
		private int errorMsgId;
		
		public ReadPdfTask(String path){
			mPath = path;
			error = false;
			errorMsgId = 0;
		}

		private boolean canPdfOpen(String filename) {
			try {
				new PdfReader(new FileInputStream(filename));
			} catch (BadPasswordException ex) {			
				errorMsgId = R.string.reading_pdf_encrypted;
				return false;
			} catch (InvalidPdfException ex) {
				errorMsgId = R.string.reading_pdf_invalid;
				return false;
			} catch (Exception ex) {
				
			} catch (Throwable ex) {

			}
			return true;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsReadPdfTaskRunning = true;
			
			mProgressDialog = new ProgressDialog(EditorActivity.this);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setMessage(getString(R.string.reading_pdf_prompt));
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (mIsReadPdfTaskRunning) {
						ReadPdfTask.this.cancel(true);
						mIsReadPdfTaskRunning = false;
					}
				}
			};
			mProgressDialog.setButton(Dialog.BUTTON_NEGATIVE,
			        getResources().getString(android.R.string.cancel), listener);
			mProgressDialog.create();
			mProgressDialog.show();
		}

		@Override
        protected List<String> doInBackground(Void... arg0) {
			pdfBitmapList = new ArrayList<String>();
			if (!canPdfOpen(mPath)) {
				error = true;
				ReadPdfTask.this.cancel(true);
				return pdfBitmapList;
			}
			
			File file = new File(mPath);
			microBmpList = new ArrayList<Bitmap>();
			try {
				PdfRenderer renderer = new PdfRenderer(
				        ParcelFileDescriptor.open(file,
				                ParcelFileDescriptor.MODE_READ_ONLY));
				final int pageCount = renderer.getPageCount();

				pdfCheckList = new ArrayList<Boolean>();
				for (int i = 0; i < pageCount; i++) {
					if(isCancelled() || !mIsReadPdfTaskRunning){
						renderer.close();
						return pdfBitmapList;
					}
					pdfCheckList.add(false);
				}
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						prepareImportPdf(mPath, false);
					}
				});
				
				int itemnHeight = getMicroPdfHeight();
				for (int i = 0; i < pageCount; i++) {
					if(isCancelled() || !mIsReadPdfTaskRunning){
						renderer.close();
						return pdfBitmapList;
					}
					Page page = renderer.openPage(i);
					Bitmap microBmp = Bitmap.createBitmap(itemnHeight, itemnHeight, Bitmap.Config.ARGB_4444);
					page.render(microBmp, null, null, Page.RENDER_MODE_FOR_DISPLAY);
					microBmpList.add(microBmp);
					mHandler.sendEmptyMessage(0);
					// close the page
					page.close();
				}				
				// close the renderer
				renderer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				error = true;
				ReadPdfTask.this.cancel(true);
		        return pdfBitmapList;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				error = true;
				ReadPdfTask.this.cancel(true);
		        return pdfBitmapList;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				error = true;
				ReadPdfTask.this.cancel(true);
		        return pdfBitmapList;
			}
	        return pdfBitmapList;
        }
		
		@Override
        protected void onCancelled(List<String> result) {
	        super.onCancelled(result);
	        if(!mIsImportPdfTaskRunning){
	        	PickerUtility.unlockRotation(EditorActivity.this);
	        }
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}
			if (pdfBitmapList != null) {
				pdfBitmapList.clear();
			}
			mIsReadPdfTaskRunning = false;
			if (error) {
				Builder builder = new AlertDialog.Builder(EditorActivity.this);
				builder.setMessage(errorMsgId != 0 ? errorMsgId : R.string.reading_pdf_error);
				builder.setTitle(R.string.error);
				builder.setPositiveButton(android.R.string.ok, null);
				builder.create().show();
				error = false;
			}
        }

		@Override
        protected void onPostExecute(List<String> result) {
	        super.onPostExecute(result);
			mIsReadPdfTaskRunning = false;
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}
        }

		public void dismissProgress() {
			if (mProgressDialog != null && mProgressDialog.isShowing()
			        && EditorActivity.this != null
			        && !EditorActivity.this.isFinishing()) {
				try{
					mProgressDialog.cancel();
				}catch(Exception e){  //smilefish fix bug 802616
					e.printStackTrace();
				}
			}
        }
	}

	public boolean saveBitmap(Bitmap bm, File f) {
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private Dialog selectPdfDialog = null;
	private boolean mPdfAutoCheck = false;
	private List<Bitmap> microBmpList;
	private List<Boolean> pdfCheckList;
	private String mReadpdfPath;
	private SelectPdfAdapter mSelectPdfAdapter;
	private PdfHandler mHandler = new PdfHandler();
	
	public class PdfHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0){
				if(mSelectPdfAdapter != null){
					mSelectPdfAdapter.notifyDataSetChanged();
				}
			}
		}
	}
	
	private int getMicroPdfHeight() {
		return (int) (EditorActivity.this.getResources().getDimension(
		        R.dimen.select_dlg_grid_widthheight) + 0.5);
	}
	
	private void prepareImportPdf(final String path, boolean isNew) {
		PickerUtility.lockRotation(EditorActivity.this);
		mReadpdfPath = path;
		selectPdfDialog = new Dialog(this);
		View v = ((LayoutInflater) this
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
		        R.layout.insert_pdf_grid, null);
		selectPdfDialog.setContentView(v);
		selectPdfDialog.setCanceledOnTouchOutside(false);
		selectPdfDialog.setCancelable(false);
		GridView gridView = (GridView) v.findViewById(R.id.pdf_select_content);
		int count = getResources().getInteger(R.integer.select_dlg_colums_num);
		gridView.setNumColumns(count);
		
		final TextView selectNumText = (TextView) v.findViewById(R.id.select_num);
		final CheckBox selectAllBox = (CheckBox) v.findViewById(R.id.select_all);
		final String msg = getResources().getString(R.string.pdf_seelcted);
		
		selectAllBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(mPdfAutoCheck){
					return ;
				}
				mPdfAutoCheck = true;
				if(isChecked){
					selectNumText.setText(msg + " " + pdfCheckList.size() + "/" + pdfCheckList.size());
					for (int i = 0; i < pdfCheckList.size(); i++) {
						pdfCheckList.set(i, true);
					}
				}else{
					selectNumText.setText(msg + " 0/" + pdfCheckList.size());
					for (int i = 0; i < pdfCheckList.size(); i++) {
						pdfCheckList.set(i, false);
					}
				}
				if(mSelectPdfAdapter != null){
					mSelectPdfAdapter.notifyDataSetChanged();
				}
				mPdfAutoCheck = false;
			}
		});
		
		if (isNew) {
			pdfCheckList = new ArrayList<Boolean>();
			for (int i = 0; i < microBmpList.size(); i++) {
				pdfCheckList.add(false);
			}
		}
		int selectNum = 0;
		for (Boolean check : pdfCheckList) {
			if (check) {
				selectNum++;
			}
		}

		selectNumText.setText(msg + " " + selectNum + "/" + pdfCheckList.size());
		mPdfAutoCheck = true;
		if (selectNum == pdfCheckList.size()) {
			selectAllBox.setChecked(true);
		} else {
			selectAllBox.setChecked(false);
		}
		mPdfAutoCheck = false;
		
		selectPdfDialog.setTitle(getResources().getString(
		        R.string.choose_pdf_prompt));
		mSelectPdfAdapter = new SelectPdfAdapter();
		gridView.setAdapter(mSelectPdfAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                    long arg3) {
				boolean isCheck = pdfCheckList.get(position);
				pdfCheckList.set(position, !isCheck);
				int selectNum = 0;
				for (Boolean check : pdfCheckList) {
					if (check) {
						selectNum++;
					}
				}
				selectNumText.setText(msg + " " + selectNum + "/" + pdfCheckList.size());
				mPdfAutoCheck = true;
				if (selectNum == pdfCheckList.size()) {
					selectAllBox.setChecked(true);
				} else {
					selectAllBox.setChecked(false);
				}
				mPdfAutoCheck = false;
				
				if(mSelectPdfAdapter != null){
					mSelectPdfAdapter.notifyDataSetChanged();
				}
            }
		});
		final Resources resources = getResources();
		selectPdfDialog
		        .getWindow()
		        .setLayout(
		                (int) (resources
		                        .getDimension(R.dimen.select_dlg_shadow) + (int) ((count - 1)
		                        * resources
		                                .getDimension(R.dimen.select_dlg_grid_space) + count
		                        * (resources
		                                .getDimension(R.dimen.select_dlg_grid_widthheight) + 0.5))),
		                android.widget.AbsListView.LayoutParams.WRAP_CONTENT);

		Button cancalBtn = (Button) v.findViewById(R.id.pdf_select_cancel);
		cancalBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				selectPdfDialog.dismiss();
				if(mIsReadPdfTaskRunning && mReadPdfTask != null){
					mReadPdfTask.cancel(true);
					mIsReadPdfTaskRunning = false; 
				}
			}
		});
		Button okBtn = (Button) v.findViewById(R.id.pdf_select_ok);
		okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				selectPdfDialog.dismiss();
				for (int i = 0; i < microBmpList.size(); i++) {
					Bitmap bmp = microBmpList.get(i);
					bmp.recycle();
					bmp = null;
				}
				boolean needImport = false;
				for (Boolean check : pdfCheckList) {
					if (check) {
						needImport = true;
						break;
					}
				}
				if (needImport) {
					importPdf();
				}
				if(mIsReadPdfTaskRunning && mReadPdfTask != null){
					mReadPdfTask.cancel(true);
					mIsReadPdfTaskRunning = false; 
				}
			}
		});
		if(mIsReadPdfTaskRunning && mReadPdfTask != null){
			mReadPdfTask.dismissProgress();
		}
		selectPdfDialog.show();
	}
		
	public class SelectPdfAdapter extends BaseAdapter{
		int itemnHeight;
		public SelectPdfAdapter() {
			itemnHeight = getMicroPdfHeight();
        }

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return pdfCheckList.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return pdfCheckList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			SelectPdfHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(EditorActivity.this,
				        R.layout.insert_pdf_cell, null);
				holder = new SelectPdfHolder();
				holder.pdfImage = (ImageView) convertView
				        .findViewById(R.id.pdf_page);
				holder.pdfCheck = (ImageView) convertView
				        .findViewById(R.id.pdf_check);
				holder.pdfIndex = (TextView) convertView
				        .findViewById(R.id.pdf_pagenum);
				convertView.setTag(holder);
			} else {
				holder = (SelectPdfHolder) convertView.getTag();
			}
			if (microBmpList.size() > position) {
				holder.pdfImage.setImageBitmap(microBmpList.get(position));
			} else {
				holder.pdfImage.setImageBitmap(null);
				holder.pdfImage.getLayoutParams().height = itemnHeight;
			}
			holder.pdfCheck.setVisibility(pdfCheckList.get(position) ? View.VISIBLE : View.GONE);
			holder.pdfIndex.setText(position + 1 + "");
			return convertView;
		}

		public class SelectPdfHolder{
			ImageView pdfImage;
			ImageView pdfCheck;
			TextView pdfIndex;
		}
	}

	/********************************************************
	 * Import PDF START
	 *******************************************************/

	private static boolean mIsImportPdfTaskRunning = false;
	private static ImportPdfTask mImportPdfTask = null;

	public void importPdf() {
		if (!mIsImportPdfTaskRunning) {
			mImportPdfTask = new ImportPdfTask();
			mImportPdfTask.execute();
		}
    }
	
	public void importPdf(String[] filePathSet) {
		if (!mIsImportPdfTaskRunning) {
			mImportPdfTask = new ImportPdfTask(filePathSet);
			mImportPdfTask.execute();
		}
	}

	private class ImportPdfTask extends AsyncTask<Void, Integer, Boolean> {
		private ProgressDialog mProgressDialog = null;

		private ArrayList<String> mFilePathList = new ArrayList<String>();
		private ArrayList<Long> mPageIdList = new ArrayList<Long>();

		public ImportPdfTask(String[] filePathSet) {
			int len = filePathSet.length;
			for (int i = 0; i < len; i++) {
				mFilePathList.add(filePathSet[i]);
			}
		}

		public ImportPdfTask() {

        }

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsImportPdfTaskRunning = true;
			PickerUtility.lockRotation(EditorActivity.this);
			mProgressDialog = new ProgressDialog(EditorActivity.this);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setMessage(getString(R.string.import_pdf_prompt));
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (mIsImportPdfTaskRunning) {
						ImportPdfTask.this.cancel(true);
						mIsReadPdfTaskRunning = false;
					}
				}
			};
			mProgressDialog.setButton(Dialog.BUTTON_NEGATIVE,
			        getResources().getString(android.R.string.cancel), listener);
			mProgressDialog.create();
			mProgressDialog.show();

			// BEGIN: Shane_Wang 2012-10-19
			Context context = getApplicationContext();
			BookCase bookCase = BookCase.getInstance(context);
			NoteBook book = bookCase.getNoteBook(mBookId);
			mCurrentPageIndex = book.getPageIndex(mPageId);
			// END: Shane_Wang 2012-10-19
		}

		// BEGIN: Shane_Wang 2012-10-19
		int mCurrentPageIndex = 0;

		// END: Shane_Wang 2012-10-19

		@Override
		protected Boolean doInBackground(Void... params) {
			if (Looper.myLooper() == null) {
				Looper.prepare();  
			}

			final Context context = getApplicationContext();

			BookCase bookcase = BookCase.getInstance(context);
			final NoteBook book = bookcase.getNoteBook(mBookId);
			if (book == null) {
				return false;
			}

			PageEditor pe = mEditorUiUtility.getPageEditor();
			final int displayWidth = (int) (pe.getNoteEditTextWidth() * pe.getScaleX());
			final int displayHeight;
			if (getBookTemplateType() == MetaData.Template_type_todo) {
				displayHeight = (int) (pe.getNoteEditText().getEditorPageHeight() * pe
				        .getScaleY());
			}else{
				displayHeight = (int) (pe.getNoteEditTextHeight() * pe.getScaleY() + pe
					        .getTemplateLayoutScaleHeight());
			}

			File pdffile = new File(mReadpdfPath);
			if(!pdffile.exists()){
				Toast.makeText(EditorActivity.this,R.string.import_export_book_fail_reason, Toast.LENGTH_LONG).show();
				return false;
			}
			int width = 0;
			int height = 0;
			try {
				PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor
				        .open(pdffile, ParcelFileDescriptor.MODE_READ_ONLY));
				Bitmap mBitmap = null;
				List<Integer> checkIndexList = new ArrayList<Integer>();
				for (int i = 0; i < pdfCheckList.size(); i++) {
					if (pdfCheckList.get(i)) {
						checkIndexList.add(i);
					}
				}
				PageDataLoader loader = new PageDataLoader(EditorActivity.this);
				for (int i = 0; i < checkIndexList.size(); i++) {
					if(isCancelled() || !mIsImportPdfTaskRunning){
						renderer.close();
						return null;
					}
					publishProgress(i + 1, checkIndexList.size());
					Page page = renderer.openPage(checkIndexList.get(i));

					if (width == 0 && height == 0 && page.getWidth() > 0
					        && page.getHeight() > 0) {
						float s = 1.0f;
						float sx = (float) displayWidth / page.getWidth();
						float sy = (float) displayHeight / page.getHeight();
						if (sx < sy) {
							s = sx;
						} else {
							s = sy;
						}
						// if (s > 1.0f) {
						// s = 1.0f;
						// }
						width = (int) (page.getWidth() * s);
						height = (int) (page.getHeight() * s);
					}
					if (width == 0 && height == 0) {
						width = displayWidth;
						height = displayHeight;
					}
					// say we render for showing on the screen
					mBitmap = Bitmap.createBitmap(width, height,
					        Bitmap.Config.ARGB_8888);
					page.render(mBitmap, null, null,
					        Page.RENDER_MODE_FOR_DISPLAY);

					NotePage notepage = new NotePage(context, book.getCreatedTime());
					notepage.setPageColor(book.getBookColor());
					notepage.setPageStyle(book.getGridType());
					mPageIdList.add(notepage.getCreatedTime());
					String pagePath = notepage.getFilePath();
					String fileName = "pdf" + System.currentTimeMillis() + "."
					        + GalleryAttacher.GALLERY_FILE_EXTENSION;
					File bmpFile = new File(pagePath, fileName);
					if (saveBitmap(mBitmap, bmpFile)) {
//						mFilePathList.add(bmpFile.getPath());
						try {
							BitmapFactory.Options option = new BitmapFactory.Options();
							option.inJustDecodeBounds = true;
							BitmapFactory.decodeFile(bmpFile.getPath(), option);
							int picWidth = option.outWidth;
							int picHeight = option.outHeight;
	
							File file = new File(pagePath,
							        MetaData.DOODLE_ITEM_PREFIX);
							file.createNewFile();
							FileOutputStream fos = new FileOutputStream(file);
							BufferedOutputStream bos = new BufferedOutputStream(fos);
							AsusFormatWriter afw = new AsusFormatWriter(bos);
	
							afw.writeByteArray(AsusFormat.SNF_DITEM_BEGIN, null, 0,
							        0);
							afw.writeShort(AsusFormat.SNF_DITEM_VERSION,
							        MetaData.ITEM_VERSION);
							afw.writeShort(AsusFormat.SNF_DITEM_CANVAS_WIDTH,
							        (short) displayWidth);
							afw.writeShort(AsusFormat.SNF_DITEM_CANVAS_HEIGHT,
							        (short) displayHeight);
	
							afw.writeByteArray(AsusFormat.SNF_DITEM_GRAPHIC_BEGIN,
							        null, 0, 0);
	
							afw.writeByteArray(AsusFormat.SNF_DITEM_DRAW_BEGIN,
							        null, 0, 0);
							afw.writeInt(AsusFormat.SNF_DITEM_DRAW_PAINT_TOOL,
							        GraphicDrawTool.GRAPHIC_TOOL);
							afw.writeByteArray(
							        AsusFormat.SNF_DITEM_DRAW_ERASES_BEGIN, null,
							        0, 0);
							afw.writeByteArray(
							        AsusFormat.SNF_DITEM_DRAW_ERASES_END, null, 0,
							        0);
							afw.writeByteArray(AsusFormat.SNF_DITEM_DRAW_END, null,
							        0, 0);
	
							afw.writeString(AsusFormat.SNF_DITEM_GRAPHIC_FILE_NAME,
							        fileName);
							Matrix matrix = new Matrix();
							float s = 1.0f;
							float sx = (float) displayWidth / picWidth;
							float sy = (float) displayHeight / picHeight;
							if (sx < sy) {
								s = sx;
							} else {
								s = sy;
							}
//							if (s > 1.0f) {
//								s = 1.0f;
//							}
							matrix.postScale(s, s);
							float tx = (displayWidth - picWidth * s) / 2;
							float ty = (displayHeight - picHeight * s) / 2;
							matrix.postTranslate(tx, ty);
							float[] values = new float[9];
							matrix.getValues(values);
	
							afw.writeFloatArray(
							        AsusFormat.SNF_DITEM_GRAPHIC_MATRIX_VALUES,
							        values, 0, values.length);
							afw.writeInt(AsusFormat.SNF_DITEM_GRAPHIC_WIDTH,
							        picWidth);
							afw.writeInt(AsusFormat.SNF_DITEM_GRAPHIC_HEIGHT,
							        picHeight);
	
							// afw.writeInt(AsusFormat.SNF_DITEM_GRAPHIC_TEMPLATE_FLAG,
							// 0);
							afw.writeByteArray(AsusFormat.SNF_DITEM_GRAPHIC_END,
							        null, 0, 0);
	
							afw.writeByteArray(AsusFormat.SNF_DITEM_DRAWS_BEGIN,
							        null, 0, 0);
							afw.writeByteArray(AsusFormat.SNF_DITEM_DRAWS_END,
							        null, 0, 0);
							afw.writeByteArray(AsusFormat.SNF_DITEM_END, null, 0, 0);
	
							bos.close();
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					page.close();		
					mBitmap.recycle();
					mBitmap = null;

					book.addPage(notepage, mCurrentPageIndex + i + 1);
					notepage.genAPThumb(loader, false, book.getPageSize() == MetaData.PAGE_SIZE_PHONE);
				}
				renderer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				ImportPdfTask.this.cancel(true);
				Toast.makeText(EditorActivity.this,R.string.prompt_import_err, Toast.LENGTH_SHORT).show();
		        return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				ImportPdfTask.this.cancel(true);
				Toast.makeText(EditorActivity.this,R.string.prompt_import_err, Toast.LENGTH_SHORT).show();
				return false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				ImportPdfTask.this.cancel(true);
				Toast.makeText(EditorActivity.this,R.string.prompt_import_err, Toast.LENGTH_SHORT).show();
				return false;
			}

			return true;
		}

		@Override
		protected void onCancelled(Boolean result) {
			PickerUtility.unlockRotation(EditorActivity.this);
			BookCase bookcase = BookCase.getInstance(EditorActivity.this);
			NoteBook book = bookcase.getNoteBook(mBookId);
			if (book != null) {
				book.deletePages(mPageIdList);
			}

			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}

			mIsImportPdfTaskRunning = false;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				String msg = EditorActivity.this.getResources().getString(R.string.import_pdf_progress);
				mProgressDialog.setMessage(String.format(msg, values[0], values[1]));
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			int num = mPageIdList.size();
			EditorActivity.this.reloadPage(mPageIdList.get(num - 1), false);
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}
			PickerUtility.unlockRotation(EditorActivity.this);

			mIsImportPdfTaskRunning = false;
		}
	}

	/********************************************************
	 * Import PDF END
	 *******************************************************/
	// END: Better

	// Begin Allen++ for template
	public void onTravelTemplateImageClick() {
		if (!isGallaryPickerShowing) {
			PickerUtility.lockRotation(EditorActivity.this);
			startActivityForResult(Intent.createChooser(
			        GalleryAttacher.getIntentGallery(),
			        getResources().getStringArray(
			                R.array.editor_func_insert_array)[INSERT_PICTURE]),
			        RESULT_CHANGEGALLERY);
			isGallaryPickerShowing = true;
		}
	}

	// End Allen++
	public boolean isPhoneScreen() {
		return mNoteBook == null ? false
		        : mNoteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE;
	}

	public int getBookFontSize() {
		return mNoteBook == null ? MetaData.BOOK_FONT_NORMAL : mNoteBook
		        .getFontSize();
	}

	public int getBookColor() {
		return mNoteBook == null ? MetaData.BOOK_COLOR_WHITE : mNoteBook
		        .getBookColor();
	}

	public int getBookGridType() {
		return mNoteBook == null ? MetaData.BOOK_GRID_LINE : mNoteBook
		        .getGridType();
	}

	// Begin Allen
	public int getBookTemplateType() {
		return mNoteBook == null ? MetaData.Template_type_normal : mNoteBook
		        .getTemplate();
	}

	// End Allen

	// BEGIN: Better
	public NotePage getNotePage() {
		return mNotePage;
	}

	public NoteBook getNoteBook() {
		return mNoteBook;
	}

	// END: Better

	private void hiddenSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm == null || getCurrentFocus() == null) {
			return;
		}
		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
		        InputMethodManager.HIDE_NOT_ALWAYS);
	}

	Toast fullToast;
	private void addNewPage(boolean needSaved) {
		//begin smilefish fix bug 405012/405023
		if(mIsAddNewBookEnable)
			mIsAddNewBookEnable = false;
		else
			return;
		//end smilefish
		
		//emmanual
		if(MetaData.isSDCardFull()){
			MetaData.showFullNoAddToast(this);
			return ;
		}
		
		// BEGIN Shane_Wang@asus.com 2012-11-22
		dismissClipboard();
		// END Shane_Wang@asus.com 2012-11-22
		NotePage notePage = new NotePage(this, mNoteBook.getCreatedTime());
		notePage.setPageColor(mNoteBook.getBookColor());
		notePage.setPageSize(mNoteBook.getPageSize());
		// begin wendy allen++ for template 0706
		notePage.setTemplate(mNoteBook.getTemplate());
		// end wendy allen++

		// BEGIN: RICHARD
		notePage.setIndexLanguage(mNoteBook.getIndexLanguage());
		// END: RICHARD

		mNoteBook.addPage(notePage, mNotePage);

		if(!MetaData.IS_AUTO_PAGING){
			showPageTranstion(true);
		}
		
		reloadPage(notePage.getCreatedTime(), needSaved, true);
		showAddPageToast();

		// +++ Dave GA
		if (MetaData.IS_GA_ON) {
			GACollector gaCollector = new GACollector(
			        mEditorUiUtility.getContext());
			gaCollector.addNewPage(mNoteBook.getTemplate(),
			        mNoteBook.getPageNum());
		}
		// ---

		if (deviceType <= 100
				//emmanual to fix bug 506379
				&& !mSharedPreference.getBoolean(getResources().getString(R.string.pref_default_read_mode), false)) {
			boolean isPrompt = mSharedPreference.getBoolean(getResources()
			        .getString(R.string.pref_prompt_handwriting_animating),
			        true);
			if (isPrompt) {
				try {
					Intent intent = new Intent();
					intent.setClass(this, ContiniousLineTipsActivity.class);
					startActivityForResult(intent, CONTINIOUS_LINE_TIPS_REQUEST);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		this.EnableSaveButton(false);// darwin
	}
	
	//emmanual to auto add page
	public synchronized void addNewPage() {
		if(MetaData.PAUSE_AUTO_PAGING){
			return;
		}
		saveTemplateData();
		addNewPage(true);
		
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				loadTemplateData();
				NoteEditText mNoteEditText = mEditorUiUtility.getPageEditor().getNoteEditText();
				mNoteEditText.requestFocus();
				mEditorUiUtility.getPageEditor().setmCurrentEditText(mNoteEditText);
				mEditorUiUtility.paste(mNotePage);
			}
		}, 200);
	}

	public void addNewMemoPage(final boolean isUsingSetColorOrStyle,final CharSequence task) {
		addNewPage(true);
		try {
	        Thread.sleep(100);
        } catch (InterruptedException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		new Handler().post(new Runnable() {

			@Override
			public void run() {
				mEditorUiUtility
				        .getPageEditor()
				        .getTemplateUtility()
				        .geTemplateToDoUtility()
				        .AddNewTodoItem(false, isUsingSetColorOrStyle, task,
				                null);
			}
		});
	}
	
	private void saveTemplateData(){
		PageEditor pageEditor = mEditorUiUtility.getPageEditor();
		if(pageEditor.getTemplateType() == MetaData.Template_type_meeting){
			pageEditor.getTemplateUtility().saveMeetingData();
		}else if(pageEditor.getTemplateType() == MetaData.Template_type_travel){
			pageEditor.getTemplateUtility().saveTravelData();			
		}
	}
	
	private void loadTemplateData(){
		PageEditor pageEditor = mEditorUiUtility.getPageEditor();
		if(pageEditor.getTemplateType() == MetaData.Template_type_meeting){
			pageEditor.getTemplateUtility().loadMeetingData();
		}else if(pageEditor.getTemplateType() == MetaData.Template_type_travel){
			pageEditor.getTemplateUtility().loadTravelData();			
		}
		pageEditor.getNoteEditText().clearUndoRedoStack();
	}
	
	
	ProgressDialog mDialog;
	public void showAutoPageProgress(){
		//emmanual to fix bug 468844
		if (this == null || this.isFinishing()) {
			return;
		}
		MetaData.IS_AUTO_PAGING = true;
		PickerUtility.lockRotation(this);//emmanual to fix bug 471321
		if (mDialog == null || !mDialog.isShowing()) {
			mDialog = new ProgressDialog(this);
			mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mDialog.setCancelable(false);
			mDialog.setMessage(getResources().getString(R.string.auto_add_page));
			mDialog.show();
		}
	}
	public void dismissAutoPageProgress() {
		//emmanual to fix bug 468844
		if (this == null || this.isFinishing()) {
			return;
		}
		MetaData.IS_AUTO_PAGING = false;
		PickerUtility.unlockRotation(this);//emmanual to fix bug 471321
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
			mDialog = null;
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					mEditorUiUtility.getPageEditor().recopyTheSelectionText();
				}
			}, 1000);
		}
	}
	
	public boolean isCurrentLastPage() {
		return mNoteBook.getPageNum() == mNoteBook.getPageIndex(mPageId) + 1;
	}

	public void deleteThisPage() {
		long newId = mNoteBook.getNextPageId(mPageId);
		if (newId == NoteBook.END_PAGE) {
			newId = mNoteBook.getPrevPageId(mPageId);
			if (newId == NoteBook.START_PAGE) {
				mNoteBook.deletePage(mPageId);
				addNewPage(false);
				// darwin
				mNoteBook.changeBookCover();
				// darwin
				return;
			}
		}
		// darwin
		int pageIndex = mNoteBook.getPageIndex(mPageId);
		mNoteBook.deletePage(mPageId);
		if (pageIndex == 0) {
			mNoteBook.changeBookCover();
		}
		// darwin
		reloadPage(newId, false);
	}

	public void jumpToPage(long pageId) {
		reloadPage(pageId, true);
	}

	public void nextPage() {
		long pageId = mNoteBook.getNextPageId(mPageId);
		if (pageId == NoteBook.END_PAGE) {
			showToast(this, R.string.pg_warning_at_last);
			return;
		}

		showPageTranstion(true);
		reloadPage(pageId, true);
		this.EnableSaveButton(false);// darwin
	}

	public void prevPage() {
		long pageId = mNoteBook.getPrevPageId(mPageId);
		if (pageId == NoteBook.START_PAGE) {
			showToast(this, R.string.pg_warning_at_start);
			return;
		}

		showPageTranstion(false);
		reloadPage(pageId, true);
		this.EnableSaveButton(false);// darwin
	}

	// BEGIN: Wendy
	private BroadcastReceiver updateUiReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(MetaData.SAVE_PAGE)) {
				MetaData.SavingEditPage = true;
				MetaData.SavedEditPage = false;
				savePage();
				MetaData.SavedEditPage = true;
				MetaData.SavingEditPage = false;
			}
			if (intent.getAction().equals(MetaData.RELOAD_PAGE)) {
				Log.v("wendy", "receive");
				long pageId = intent.getLongExtra(MetaData.RELOAD_PAGE_ID, -1);
				if (pageId > 0) {
					reloadPageBySync(pageId);
				}
			}
		}
	};

	private void reloadPageBySync(long pageId) {
		mNotePage = mNoteBook.getNotePage(pageId);
		if (mNotePage != null) {
			boolean loadSucceed = mEditorUiUtility.loadPage(mNotePage, false);
			if (!loadSucceed) {
				long NextpageId = mNoteBook.getNextPageId(pageId);
				if (NextpageId == NoteBook.END_PAGE) {
					long PrepageId = mNoteBook.getPrevPageId(pageId);
					if (PrepageId == NoteBook.START_PAGE) {
						MetaData.CurrentEditPageId = -1;
						finish();
					} else {
						reloadPage(PrepageId, true);
					}
				} else {
					reloadPage(NextpageId, true);
				}
			} else {
				mPageId = pageId;
				MetaData.CurrentEditPageId = mPageId;
			}
		}
	}

	// public boolean saved = true;
	public void savePage() {
		//emmanual
		if(MetaData.isSDCardFull()){
			MetaData.showFullNoSaveToast(this);
			return ;
		}
		
		Log.v("wendy", "savaPage");
		if (mEditorUiUtility != null && mNotePage != null) {
			// BEGIN: RICHARD
			Boolean flag = mEditorUiUtility.isNeedUpdateIndexFile(mNotePage);
			// END: RICHARD
			OnSavePage();
			// BEGIN: RICHARD
			if (flag) {
				if (mIndexServiceClient != null)// darwin
				{
					mIndexServiceClient.sendPageIDToIndexService(mPageId);
				}
			}
			// END: RICHARD
		}
	}

	// END: Wendy

	// begin noah;7.5
	private void reloadPage(long pageId, boolean isNeedToSave) {
		reloadPage(pageId, isNeedToSave, false);
	}

	private void reloadPage(long pageId, boolean isNeedToSave,
	        boolean fromAddNew) {
		if (isNeedToSave && mNotePage != null) {
			// BEGIN: RICHARD
			savePage();
		}

		mPageId = pageId;
		getIntent().putExtra(MetaData.PAGE_ID, mPageId);
		mNotePage = mNoteBook.getNotePage(mPageId);

		MetaData.CurrentEditPageId = mPageId;

		//emmanual
		if(mEditorUiUtility.getPageEditor().getTemplateType() == MetaData.Template_type_todo){
			TextView dateView = (TextView)findViewById(R.id.todo_title_date);
			if(dateView != null){
				Date date = new Date();
				date.setTime(mNotePage.getModifiedTime());
				java.text.DateFormat dateFormat = DateFormat.getDateFormat(mEditorUiUtility.getContext());
				dateView.setText(dateFormat.format(date));
			}
		}

		// begin noah;for share;4.15
		if (mNotePage == null) {
			this.finish();
			return;
		}
		// end noah;for share;4.15
		boolean loadSucceed = mEditorUiUtility.loadPage(mNotePage, false,
		        fromAddNew);
		if (!loadSucceed) {
			fileFormatError();
		}
		setMenuItemBookmark();
	}

	// LoadPageTaskVersionTwo任务完成之后回调该方�?
	public void updatePageNumber(boolean fromAddNew) {
		mIsAddNewBookEnable = true; //smilefish
		if (mCurrentPageNumberTextView != null) {
			mCurrentPageNumberTextView
			        .setText((mNoteBook.getPageIndex(mPageId) + 1) + "");
		}
		if (mTotalPageNumberTextView != null) {
			mTotalPageNumberTextView.setText(""
			        + mNoteBook.getTotalPageNumber());
		}
	}

	// end noah;7.5

	private void openDeletePageDialog() {
		if (mDeletePageDialog == null) {
			Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(
			        R.string.editor_delete_page_dialog_title));
			builder.setMessage(getResources().getString(
			        R.string.editor_delete_page_dialog_message));
			builder.setPositiveButton(
			        getResources().getString(android.R.string.ok),
			        new DialogInterface.OnClickListener() {

				        @Override
				        public void onClick(DialogInterface dialog, int which) {
					        deleteThisPage();
				        }
			        });

			builder.setNegativeButton(
			        getResources().getString(android.R.string.cancel),
			        new DialogInterface.OnClickListener() {

				        @Override
				        public void onClick(DialogInterface dialog, int which) {
				        }
			        });

			mDeletePageDialog = builder.create();
		}

		if (!mDeletePageDialog.isShowing()) {
			mDeletePageDialog.show();
		}
	}

	// begin smilefish
	BrushLibraryAdapter.INotifyOuter notifyOuter = new BrushLibraryAdapter.INotifyOuter() {

		@Override
		public void selectBrush(BrushInfo info) {
			EditorActivity.this.selectBrush(info);
			CursorIconLibrary.setStylusIcon(templateLinearLayout, getDoodleDrawable());
		}

		@Override
		public void showAddBrushButton() {
			EditorActivity.this.showAddBrushButton();
		}

		@Override
		public void draw(Canvas canvas, Paint paint, int toolCode) {
			EditorActivity.this.draw(canvas, paint, toolCode);
		}

	};

	// end smilefish

	private void openPenColorPopupWindow(View v) {
		mDoodleBrushPopupWindow = preparePopupWindowImageView(v,
		        mDoodleBrushPopupWindow, R.layout.editor_func_popup_penmenu,
		        mEditorIdList.editorDoodleUnityIds, mDoodleUnityClickListener);

		final Button brushLibraryButton = (Button) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.brush_library);
		brushLibraryButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				View brushColorThicknessLayout = (View) mDoodleBrushPopupWindow
				        .getContentView().findViewById(
				                R.id.brush_color_thickness_layout);
				ListView brushLibraryView = (ListView) mDoodleBrushPopupWindow
				        .getContentView().findViewById(R.id.brush_library_view);
				View brushPreviewView = mDoodleBrushPopupWindow.getContentView()
						.findViewById(R.id.brush_preview);
				if (brushColorThicknessLayout.getVisibility() == View.VISIBLE) {
					int height = brushColorThicknessLayout.getHeight();
					brushLibraryView.getLayoutParams().height = height;
					brushColorThicknessLayout.setVisibility(View.GONE);
					brushLibraryView.setVisibility(View.VISIBLE);
					brushLibraryButton.setText(R.string.brush_edit_dialog_back);
					brushLibraryButton.setSelected(true);
					brushPreviewView.setSelected(true);

					mBrushAdapter = new BrushLibraryAdapter(
					        EditorActivity.this, mBrushCollection);
					mBrushAdapter.addOuterListener(notifyOuter); // smilefish
					brushLibraryView.setAdapter(mBrushAdapter);
				} else {
					brushColorThicknessLayout.setVisibility(View.VISIBLE);
					brushLibraryView.setVisibility(View.GONE);
					brushLibraryButton
					        .setText(R.string.brush_edit_dialog_library);
					brushLibraryButton.setSelected(false);
					brushPreviewView.setSelected(false);
				}
			}

		});

		// Carrot: initial color list
		View cvv = mDoodleBrushPopupWindow.getContentView().findViewById(
		        R.id.editor_func_color_A);
		((ImageView) cvv).setBackgroundColor(mDefaultColorCodes[0]);
		cvv = mDoodleBrushPopupWindow.getContentView().findViewById(
		        R.id.editor_func_color_B);
		((ImageView) cvv).setBackgroundColor(mDefaultColorCodes[1]);
		cvv = mDoodleBrushPopupWindow.getContentView().findViewById(
		        R.id.editor_func_color_C);
		((ImageView) cvv).setBackgroundColor(mDefaultColorCodes[2]);
		cvv = mDoodleBrushPopupWindow.getContentView().findViewById(
		        R.id.editor_func_color_D);
		((ImageView) cvv).setBackgroundColor(mDefaultColorCodes[3]);
		cvv = mDoodleBrushPopupWindow.getContentView().findViewById(
		        R.id.editor_func_color_E);
		((ImageView) cvv).setBackgroundColor(mDefaultColorCodes[4]);
		cvv = mDoodleBrushPopupWindow.getContentView().findViewById(
		        R.id.editor_func_color_F);
		((ImageView) cvv).setBackgroundColor(mDefaultColorCodes[5]);
		cvv = mDoodleBrushPopupWindow.getContentView().findViewById(
		        R.id.editor_func_color_G);
		((ImageView) cvv).setBackgroundColor(mDefaultColorCodes[6]);
		cvv = mDoodleBrushPopupWindow.getContentView().findViewById(
		        R.id.editor_func_color_H);
		((ImageView) cvv).setBackgroundColor(mDefaultColorCodes[7]);
		cvv = mDoodleBrushPopupWindow.getContentView().findViewById(
		        R.id.editor_func_color_I);
		((ImageView) cvv).setBackgroundColor(mDefaultColorCodes[8]);
		cvv = mDoodleBrushPopupWindow.getContentView().findViewById(
		        R.id.editor_func_color_J);
		((ImageView) cvv).setBackgroundColor(mDefaultColorCodes[9]);
		cvv = mDoodleBrushPopupWindow.getContentView().findViewById(
		        R.id.editor_func_color_K);
		((ImageView) cvv).setBackgroundColor(mDefaultColorCodes[10]);
		cvv = mDoodleBrushPopupWindow.getContentView().findViewById(
		        R.id.editor_func_color_M);
		if (cvv != null) {
			((ImageView) cvv).setBackgroundColor(mDefaultColorCodes[11]);
		}

		// BEGIN: Show
		if (mIsCustomColorSet) // smilefish
		{
			View vv = mDoodleBrushPopupWindow.getContentView().findViewById(
			        R.id.editor_func_color_L);
			((ImageView) vv).setBackgroundColor(mCustomColor);
		}
		// END: Show

		mPenPreview = (ImageView) mDoodleBrushPopupWindow.getContentView()
		        .findViewById(R.id.penpreview);

		final ImageButton addBrushBtn = (ImageButton) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.add_brush);
		if (mBrushCollection.isBrushFull())
			addBrushBtn.setVisibility(View.INVISIBLE); // fix bug: 301630
		addBrushBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Boolean isSuccess = mBrushCollection.addBrush(mStrokeWidth,
				        mDoodleToolCode, mDoodleToolAlpha, mIsPalette,
				        mIsColorMode, mCustomColor, mSelectedColorIndex,
				        mColorPalette_X, mColorPalette_Y);
				if (isSuccess) {
					ListView brushLibraryView = (ListView) mDoodleBrushPopupWindow
					        .getContentView().findViewById(
					                R.id.brush_library_view);
					if (brushLibraryView.getVisibility() == View.VISIBLE)
						mBrushAdapter.notifyDataSetChanged();

					if (mBrushCollection.isBrushFull()) {
						showToast(EditorActivity.this,
						        R.string.brush_edit_dialog_full);
						addBrushBtn.setVisibility(View.INVISIBLE);
					} else { // smilefish
						Toast toast = Toast.makeText(EditorActivity.this,
						        R.string.brush_edit_dialog_save_successful,
						        Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
						toast.show();
					}
				} else {
					Toast toast = Toast
					        .makeText(EditorActivity.this,
					                R.string.brush_edit_dialog_save,
					                Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
					toast.show();
				}
			}

		});

		ImageView colorStrawBtn = (ImageView) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.editor_func_color_straw);
		colorStrawBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// begin smilefish
				mDoodleBrushPopupWindow.dismiss();
				mEditorUiUtility
				        .setInputMode(InputManager.INPUT_METHOD_COLOR_PICKER);
				colorPickerViewShow(false);
				mColorPickerDoneButton.setEnabled(false);
				// end smilefish
			}

		});

		final ColorPickerViewCustom ColorPicker = (ColorPickerViewCustom) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.color_picker_view);
		ColorPicker.SetListener(new ColorPickerViewCustom.ColorChange() {

			@Override
			public void OnColorChange(int Color) {
				// TODO Auto-generated method stub
				// +++ Dave GA
				if (MetaData.IS_GA_ON && mCustomColor != Color) {
					GACollector gaCollector = new GACollector(mEditorUiUtility
					        .getContext());
					gaCollector.doodleColorSet(mCurAttrIndex,
					        String.valueOf(Color));
				}
				// ---

				mEditorUiUtility.changeColor(Color);
				mCustomColor = Color;
				mIsCustomColorSet = true; // smilefish
				mCurrentDoodleBrushButton
				        .setCompoundDrawablesWithIntrinsicBounds(
				                null,
				                drawCurrentBrushThickness(drawCurrentBrushType()),
				                null, null);
				mIsPalette = true;
				if (mPenPreview != null)
					DrawPreview(mPenPreview);

				for (int id : mEditorIdList.editorDoodleUnityColorIds) {
					View vv = mDoodleBrushPopupWindow.getContentView()
					        .findViewById(id);
					if (vv != null) // smilefish
					{
						vv.setSelected(false);
						((ImageView) vv)
						        .setImageResource(R.drawable.asus_color_frame_n);
						if (id == R.id.editor_func_color_L) {
							((ImageView) vv)
							        .setImageResource(R.drawable.asus_color_frame_p);
							((ImageView) vv).setBackgroundColor(Color);
						}
					}
				}

				// Carrot: individually set color of every brush
				mSelectedColorIndex = COLOR_PALETTE_INDEX;
				mColorPalette_X = ColorPicker.getCurX();
				mColorPalette_Y = ColorPicker.getCurY();
				attrs.get(mCurAttrIndex).ColorInfo.Color = Color;
				attrs.get(mCurAttrIndex).ColorInfo.Index = mSelectedColorIndex;
				attrs.get(mCurAttrIndex).ColorInfo.ColorPalette_X = mColorPalette_X;
				attrs.get(mCurAttrIndex).ColorInfo.ColorPalette_Y = mColorPalette_Y;
				// Carrot: individually set color of every brush

				ImageButton selectColor = (ImageButton) mDoodleBrushPopupWindow
				        .getContentView().findViewById(R.id.select_color);
				Drawable background1 = CoverHelper.createRoundColorBackground(
				        EditorActivity.this, R.drawable.asus_popup_pen_triangle_2,
				        Color);
				selectColor.setBackgroundDrawable(background1);
				TextView colorName = (TextView) mDoodleBrushPopupWindow
				        .getContentView().findViewById(R.id.select_color_name);
				colorName.setText(ColorHelper.displayColorName(Color)); // Carol-choose
				                                                        // color
				                                                        // from
				                                                        // panel

				Drawable background = CoverHelper.createGradientColorAndCover(
				        EditorActivity.this, R.drawable.asus_pen_scrollbar_bg,
				        Color);
				SeekBar_Alpha.setBackgroundDrawable(background);

				// BEGIN: RICHARD
				setAutoRecognizerShapeState(true);
				// END: RICHARD

			}
		});

		SeekBar SeekBar_width = (SeekBar) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.seekbar_width);

		// --- Carrot: temporary add for limitation of brush width ---
		float WidthPrecent;
		int WidthProgress;

		WidthPrecent = attrs.get(mCurAttrIndex).Width
		        / attrs.get(mCurAttrIndex).MaxWidth;
		WidthProgress = (int) (WidthPrecent * 100);
		SeekBar_width.setProgress(WidthProgress);

		// --- Carrot: temporary add for limitation of brush width ---
		TextView strokeWidth = (TextView) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.stroke_width);
		// --- Carrot: temporary add for limitation of brush width ---
		strokeWidth.setText(String.format(
		        getResources().getString(R.string.brush_edit_dialog_stroke),
		        (int) attrs.get(mCurAttrIndex).Width));
		// --- Carrot: temporary add for limitation of brush width ---

		ImageButton selectBrush = (ImageButton) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.select_brush);
		TextView selectBrushName = (TextView) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.select_brush_name);
		switch (mDoodleToolCode) {
		case DrawTool.NORMAL_TOOL:
			selectBrush.setImageResource(R.drawable.asus_popup_pen3);
			selectBrushName.setText(R.string.brush_edit_dialog_rollerpen);
			break;
		case DrawTool.SKETCH_TOOL:
			selectBrush.setImageResource(R.drawable.asus_popup_pen1);
			selectBrushName.setText(R.string.brush_edit_dialog_pencil);
			break;
		case DrawTool.MARKPEN_TOOL:
			selectBrush.setImageResource(R.drawable.asus_popup_pen4);
			selectBrushName.setText(R.string.brush_edit_dialog_marker);
			break;
		case DrawTool.SCRIBBLE_TOOL:
			selectBrush.setImageResource(R.drawable.asus_popup_pen2);
			selectBrushName.setText(R.string.brush_edit_dialog_pen);
			break;
		case DrawTool.WRITINGBRUSH_TOOL:
			selectBrush.setImageResource(R.drawable.asus_popup_pen5);
			selectBrushName.setText(R.string.brush_edit_dialog_brush);
			break;
		case DrawTool.NEON_TOOL:
			selectBrush.setImageResource(R.drawable.asus_popup_pen6);
			selectBrushName.setText(R.string.brush_edit_dialog_airbrush);
			break;
		}
		selectBrush.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				View brushLayout = (View) mDoodleBrushPopupWindow
				        .getContentView()
				        .findViewById(R.id.brush_select_layout);
				if (brushLayout.getVisibility() == View.VISIBLE)
					brushLayout.setVisibility(View.GONE);
				else
					brushLayout.setVisibility(View.VISIBLE);
			}

		});

		ImageButton selectColor = (ImageButton) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.select_color);
		selectColor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				View colorLayout = (View) mDoodleBrushPopupWindow
				        .getContentView()
				        .findViewById(R.id.color_select_layout);
				if (colorLayout.getVisibility() == View.VISIBLE)
					colorLayout.setVisibility(View.GONE);
				else
					colorLayout.setVisibility(View.VISIBLE);
			}

		});

		SeekBar_width.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
			        boolean fromTouch) {
				// --- Carrot: temporary add for limitation of brush width ---
				mStrokeWidth = attrs.get(mCurAttrIndex).MaxWidth
				        * (float) (progress / 100.0);
				if (mStrokeWidth < attrs.get(mCurAttrIndex).MinWidth) {
					mStrokeWidth = attrs.get(mCurAttrIndex).MinWidth;
				}
				attrs.get(mCurAttrIndex).Width = mStrokeWidth;
				// --- Carrot: temporary add for limitation of brush width ---

				mEditorUiUtility.changeScribleStroke(mStrokeWidth);
				mCurrentDoodleBrushButton
				        .setCompoundDrawablesWithIntrinsicBounds(
				                null,
				                drawCurrentBrushThickness(drawCurrentBrushType()),
				                null, null);
				if (mPenPreview != null)
					DrawPreview(mPenPreview);

				TextView strokeWidth = (TextView) mDoodleBrushPopupWindow
				        .getContentView().findViewById(R.id.stroke_width);
				strokeWidth.setText(String.format(
				        getResources().getString(
				                R.string.brush_edit_dialog_stroke),
				        (int) (mStrokeWidth + 0.5f)));

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				System.out.println(seekBar.getProgress());
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// BEGIN: RICHARD
				setAutoRecognizerShapeState(true);
				// END: RICHARD
			}
		});

		SeekBar_Alpha = (SeekBar) mDoodleBrushPopupWindow.getContentView()
		        .findViewById(R.id.seekbar_Alpha);

		Alpha_Text = (TextView) mDoodleBrushPopupWindow.getContentView()
		        .findViewById(R.id.alpha_text);

		float AlphaPercent = mDoodleToolAlpha / 255.0f;
		int AlphaValue = Math.round(AlphaPercent * 100); // smilefish fix bug
		                                                 // 354618
		SeekBar_Alpha.setProgress(AlphaValue);

		TextView alphaPercentage = (TextView) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.alpha_percentage);
		alphaPercentage.setText(AlphaValue + "%");

		Drawable background = CoverHelper.createGradientColorAndCover(this,
		        R.drawable.asus_pen_scrollbar_bg, mEditorUiUtility
		                .getDoodlePaint().getColor());
		SeekBar_Alpha.setBackgroundDrawable(background);

		SeekBar_Alpha.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
			        boolean fromTouch) {
				// darwin
				if (mDoodleToolCode != DrawTool.MARKPEN_TOOL) {
					return;
				}
				// darwin
				int Alpha = 255 * progress / 100;
				mDoodleToolAlpha = Alpha;
				mEditorUiUtility.changeAlpha(mDoodleToolAlpha); // Better
				if (mPenPreview != null)
					DrawPreview(mPenPreview);

				TextView alphaPercentage = (TextView) mDoodleBrushPopupWindow
				        .getContentView().findViewById(R.id.alpha_percentage);
				alphaPercentage.setText(progress + "%");

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				System.out.println(seekBar.getProgress());
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// BEGIN: RICHARD
				setAutoRecognizerShapeState(true);
				// END: RICHARD
			}
		});

		if (mDoodleToolCode != DrawTool.MARKPEN_TOOL) {
			setSeekBarAlphaEnable(false);
			setAlphaTextEnable(false);
		}

		if (mPenPreview != null)
			DrawPreview(mPenPreview);
	}

	private void setEditorTextStyleAndColorToPreference(Editor editor) {
		if (editor == null) {
			return;
		}

		editor.putInt(MetaData.PREFERENCE_EDITOR_TEXT_STYLE,
		        mEditorUiUtility.getTextStyle());
		editor.putInt(MetaData.PREFERENCE_EDITOR_TEXT_COLOR,
		        mEditorUiUtility.getTextColor());
	}

	private void getEditorTextStyleAndColorFromPreference() {
		if (mSharedPreference == null) {
			return;
		}

		int style = mSharedPreference.getInt(
		        MetaData.PREFERENCE_EDITOR_TEXT_STYLE, Typeface.NORMAL);
		int color = mSharedPreference.getInt(
		        MetaData.PREFERENCE_EDITOR_TEXT_COLOR, Color.BLACK);

		mEditorUiUtility.changeTextStyle(style);
		mEditorUiUtility.changeTextColor(color);
	}

	private void getColorsBrushStrokeFromPreference() {
		if (mSharedPreference == null) {
			return;
		}

		mDoodleToolCode = mSharedPreference.getInt(
		        MetaData.PREFERENCE_DOODLE_BRUSH, DrawTool.NORMAL_TOOL);
		// BEGIN: archie_huang@asus.com
		// Setup default stroke width
		float testStroke = 0;
		testStroke = mSharedPreference
		        .getFloat(
		                MetaData.PREFERENCE_DOODLE_ERASER_WIDTH,
		                MetaData.DOODLE_ERASER_WIDTHS[MetaData.DOODLE_DEFAULT_ERASER_WIDTHS]);
		for (float stroke : MetaData.DOODLE_ERASER_WIDTHS) {
			if (stroke == testStroke) {
				mEraserWidth = testStroke;
				break;
			}
		}
		// END: archie_huang@asus.com
		// begin jason
		mStrokeWidth = mSharedPreference
		        .getFloat(
		                MetaData.PREFERENCE_DOODLE_STROKE,
		                MetaData.DOODLE_PAINT_WIDTHS[MetaData.DOODLE_DEFAULT_PAINT_WIDTH]);
		// end jason
		mSelectedColorIndex = mSharedPreference.getInt(
		        MetaData.PREFERENCE_DOODLE_SEL_COLOR_INDEX, 5);
		mIsPalette = mSharedPreference.getBoolean(
		        MetaData.PREFERENCE_IS_PALETTE, false); // By Show
		mIsColorMode = mSharedPreference.getBoolean(
		        MetaData.PREFERENCE_IS_COLOR_MODE, false); // By Show
		mCustomColor = mSharedPreference.getInt(
		        MetaData.PREFERENCE_PALETTE_COLOR, -1); // By Show
		mDoodleToolAlpha = mSharedPreference.getInt(
		        MetaData.PREFERENCE_DOODLE_ALPHA, 0x5F); // Better
		mIsCustomColorSet = mSharedPreference.getBoolean(
		        MetaData.PREFERENCE_IS_CUSTOM_COLOR_SET, false); // By Smilefish

		// --- Carrot: temporary add for limitation of brush width ---
		if (attrs.size() < 1) {
			InitBrushAttributs();
		}
		attrs.get(0).Width = mSharedPreference.getFloat(
		        MetaData.TEMP_ROLLER_WIDTH, 6);
		attrs.get(1).Width = mSharedPreference.getFloat(
		        MetaData.TEMP_PEN_WIDTH, 6);
		attrs.get(2).Width = mSharedPreference.getFloat(
		        MetaData.TEMP_BRUSH_WIDTH, 25.5f);
		attrs.get(3).Width = mSharedPreference.getFloat(
		        MetaData.TEMP_AIRBRUSH_WIDTH, 30.5f);
		attrs.get(4).Width = mSharedPreference.getFloat(
		        MetaData.TEMP_PENCIL_WIDTH, 3);
		attrs.get(5).Width = mSharedPreference.getFloat(
		        MetaData.TEMP_MARKER_WIDTH, 64);

		// Carrot: individually set color of every brush
		attrs.get(0).ColorInfo.Color = mSharedPreference.getInt(
		        MetaData.TEMP_ROLLER_COLOR, 0xffe70012);
		attrs.get(1).ColorInfo.Color = mSharedPreference.getInt(
		        MetaData.TEMP_PEN_COLOR, 0xff8fc31f);
		attrs.get(2).ColorInfo.Color = mSharedPreference.getInt(
		        MetaData.TEMP_BRUSH_COLOR, 0xff000000);
		attrs.get(3).ColorInfo.Color = mSharedPreference.getInt(
		        MetaData.TEMP_AIRBRUSH_COLOR, 0xff00a0e9);
		attrs.get(4).ColorInfo.Color = mSharedPreference.getInt(
		        MetaData.TEMP_PENCIL_COLOR, 0xff000000);
		attrs.get(5).ColorInfo.Color = mSharedPreference.getInt(
		        MetaData.TEMP_MARKER_COLOR, 0xff8fc31f);

		attrs.get(0).ColorInfo.Index = mSharedPreference.getInt(
		        MetaData.TEMP_ROLLER_COLOR_INDEX, 4);
		attrs.get(1).ColorInfo.Index = mSharedPreference.getInt(
		        MetaData.TEMP_PEN_COLOR_INDEX, 7);
		attrs.get(2).ColorInfo.Index = mSharedPreference.getInt(
		        MetaData.TEMP_BRUSH_COLOR_INDEX, 3);
		attrs.get(3).ColorInfo.Index = mSharedPreference.getInt(
		        MetaData.TEMP_AIRBRUSH_COLOR_INDEX, 9);
		attrs.get(4).ColorInfo.Index = mSharedPreference.getInt(
		        MetaData.TEMP_PENCIL_COLOR_INDEX, 3);
		attrs.get(5).ColorInfo.Index = mSharedPreference.getInt(
		        MetaData.TEMP_MARKER_COLOR_INDEX, 7);

		attrs.get(0).ColorInfo.ColorPalette_X = mSharedPreference.getInt(
		        MetaData.TEMP_ROLLER_COLOR_X, 0);
		attrs.get(1).ColorInfo.ColorPalette_X = mSharedPreference.getInt(
		        MetaData.TEMP_PEN_COLOR_X, 0);
		attrs.get(2).ColorInfo.ColorPalette_X = mSharedPreference.getInt(
		        MetaData.TEMP_BRUSH_COLOR_X, 0);
		attrs.get(3).ColorInfo.ColorPalette_X = mSharedPreference.getInt(
		        MetaData.TEMP_AIRBRUSH_COLOR_X, 0);
		attrs.get(4).ColorInfo.ColorPalette_X = mSharedPreference.getInt(
		        MetaData.TEMP_PENCIL_COLOR_X, 0);
		attrs.get(5).ColorInfo.ColorPalette_X = mSharedPreference.getInt(
		        MetaData.TEMP_MARKER_COLOR_X, 0);

		attrs.get(0).ColorInfo.ColorPalette_Y = mSharedPreference.getInt(
		        MetaData.TEMP_ROLLER_COLOR_Y, 0);
		attrs.get(1).ColorInfo.ColorPalette_Y = mSharedPreference.getInt(
		        MetaData.TEMP_PEN_COLOR_Y, 0);
		attrs.get(2).ColorInfo.ColorPalette_Y = mSharedPreference.getInt(
		        MetaData.TEMP_BRUSH_COLOR_Y, 0);
		attrs.get(3).ColorInfo.ColorPalette_Y = mSharedPreference.getInt(
		        MetaData.TEMP_AIRBRUSH_COLOR_Y, 0);
		attrs.get(4).ColorInfo.ColorPalette_Y = mSharedPreference.getInt(
		        MetaData.TEMP_PENCIL_COLOR_Y, 0);
		attrs.get(5).ColorInfo.ColorPalette_Y = mSharedPreference.getInt(
		        MetaData.TEMP_MARKER_COLOR_Y, 0);
		// Carrot: individually set color of every brush

		SetCurAttrInit(mDoodleToolCode);
		// --- Carrot: temporary add for limitation of brush width ---
	}

	// BEGIN: RICHARD
	private void setWTAndShapeButtonStatusFromPreference(Editor editor) {
		if (editor == null) {
			return;
		}
		editor.putBoolean(MetaData.PREFERENCE_EDITOR_TEXT_WT,
		        isLastWTToggleButtonChecked);
		editor.putBoolean(MetaData.PREFERENCE_DOODLE_SHAPE,
		        isLastRecognizerShapeButtonChecked);
	}

	private void getWTAndShapeButtonStatusFromPreference() {
		if (mSharedPreference == null) {
			isLastWTToggleButtonChecked = false;
			isLastRecognizerShapeButtonChecked = false;
			return;
		}

		isLastWTToggleButtonChecked = mSharedPreference.getBoolean(
		        MetaData.PREFERENCE_EDITOR_TEXT_WT, false);
		isLastRecognizerShapeButtonChecked = mSharedPreference.getBoolean(
		        MetaData.PREFERENCE_DOODLE_SHAPE, false);
	}

	// END: RICHARD
	// add by mars
	private void setColorBrushStrokeToPreference(Editor editor) {
		if (editor == null) {
			return;
		}

		editor.putInt(MetaData.PREFERENCE_DOODLE_BRUSH, mDoodleToolCode);
		editor.putFloat(MetaData.PREFERENCE_DOODLE_STROKE, mStrokeWidth);
		editor.putFloat(MetaData.PREFERENCE_DOODLE_ERASER_WIDTH, mEraserWidth);
		editor.putInt(MetaData.PREFERENCE_DOODLE_SEL_COLOR_INDEX,
		        mSelectedColorIndex);
		editor.putInt(MetaData.PREFERENCE_DOODLE_ALPHA, mDoodleToolAlpha); // Better
		editor.putBoolean(MetaData.PREFERENCE_IS_PALETTE, mIsPalette); // By
		                                                               // Show
		editor.putBoolean(MetaData.PREFERENCE_IS_COLOR_MODE, mIsColorMode); // By
		                                                                    // Show
		editor.putInt(MetaData.PREFERENCE_PALETTE_COLOR, mCustomColor); // By
		                                                                // Show
		editor.putBoolean(MetaData.PREFERENCE_IS_CUSTOM_COLOR_SET,
		        mIsCustomColorSet); // By Smilefish

		// --- Carrot: temporary add for limitation of brush width ---
		if (attrs.size() > 0) {
			editor.putFloat(MetaData.TEMP_PENCIL_WIDTH, attrs.get(4).Width);
			editor.putFloat(MetaData.TEMP_ROLLER_WIDTH, attrs.get(0).Width);
			editor.putFloat(MetaData.TEMP_PEN_WIDTH, attrs.get(1).Width);
			editor.putFloat(MetaData.TEMP_MARKER_WIDTH, attrs.get(5).Width);
			editor.putFloat(MetaData.TEMP_BRUSH_WIDTH, attrs.get(2).Width);
			editor.putFloat(MetaData.TEMP_AIRBRUSH_WIDTH, attrs.get(3).Width);

			// Carrot: individually set color of every brush
			editor.putInt(MetaData.TEMP_PENCIL_COLOR,
			        attrs.get(4).ColorInfo.Color);
			editor.putInt(MetaData.TEMP_ROLLER_COLOR,
			        attrs.get(0).ColorInfo.Color);
			editor.putInt(MetaData.TEMP_PEN_COLOR, attrs.get(1).ColorInfo.Color);
			editor.putInt(MetaData.TEMP_MARKER_COLOR,
			        attrs.get(5).ColorInfo.Color);
			editor.putInt(MetaData.TEMP_BRUSH_COLOR,
			        attrs.get(2).ColorInfo.Color);
			editor.putInt(MetaData.TEMP_AIRBRUSH_COLOR,
			        attrs.get(3).ColorInfo.Color);

			editor.putInt(MetaData.TEMP_PENCIL_COLOR_INDEX,
			        attrs.get(4).ColorInfo.Index);
			editor.putInt(MetaData.TEMP_ROLLER_COLOR_INDEX,
			        attrs.get(0).ColorInfo.Index);
			editor.putInt(MetaData.TEMP_PEN_COLOR_INDEX,
			        attrs.get(1).ColorInfo.Index);
			editor.putInt(MetaData.TEMP_MARKER_COLOR_INDEX,
			        attrs.get(5).ColorInfo.Index);
			editor.putInt(MetaData.TEMP_BRUSH_COLOR_INDEX,
			        attrs.get(2).ColorInfo.Index);
			editor.putInt(MetaData.TEMP_AIRBRUSH_COLOR_INDEX,
			        attrs.get(3).ColorInfo.Index);

			editor.putInt(MetaData.TEMP_PENCIL_COLOR_X,
			        attrs.get(4).ColorInfo.ColorPalette_X);
			editor.putInt(MetaData.TEMP_ROLLER_COLOR_X,
			        attrs.get(0).ColorInfo.ColorPalette_X);
			editor.putInt(MetaData.TEMP_PEN_COLOR_X,
			        attrs.get(1).ColorInfo.ColorPalette_X);
			editor.putInt(MetaData.TEMP_MARKER_COLOR_X,
			        attrs.get(5).ColorInfo.ColorPalette_X);
			editor.putInt(MetaData.TEMP_BRUSH_COLOR_X,
			        attrs.get(2).ColorInfo.ColorPalette_X);
			editor.putInt(MetaData.TEMP_AIRBRUSH_COLOR_X,
			        attrs.get(3).ColorInfo.ColorPalette_X);

			editor.putInt(MetaData.TEMP_PENCIL_COLOR_Y,
			        attrs.get(4).ColorInfo.ColorPalette_Y);
			editor.putInt(MetaData.TEMP_ROLLER_COLOR_Y,
			        attrs.get(0).ColorInfo.ColorPalette_Y);
			editor.putInt(MetaData.TEMP_PEN_COLOR_Y,
			        attrs.get(1).ColorInfo.ColorPalette_Y);
			editor.putInt(MetaData.TEMP_MARKER_COLOR_Y,
			        attrs.get(5).ColorInfo.ColorPalette_Y);
			editor.putInt(MetaData.TEMP_BRUSH_COLOR_Y,
			        attrs.get(2).ColorInfo.ColorPalette_Y);
			editor.putInt(MetaData.TEMP_AIRBRUSH_COLOR_Y,
			        attrs.get(3).ColorInfo.ColorPalette_Y);
			// Carrot: individually set color of every brush
		}
		// --- Carrot: temporary add for limitation of brush width ---
	}

	private void shareOnlyText() {
		if (mSharedText.length() > 0) {
			try {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, mSharedText); //smilefish fix bug 709326
				EditorActivity.this.startActivity(intent);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			showToast(this, R.string.editor_share_to_dialog_nothidg);
		}
	}

	private final OnItemSelectedListener selectPreviewSizeListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view,
		        int index, long arg3) {
			//emmanual:change the scale-- 0->0.5 1->0.75 2->1
			mShareScale = 0.5f + 0.25f * index;
			mSelect_Index = index;
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	// darwin
	private static boolean mIsShareDialogShow = false; // by darwin
	private static boolean mIsRecreateDialog = false; // by darwin
	private static boolean mIsCheckboxShow = false; // by darwin
	private static boolean mIsChecked_hidegrid = false; // by darwin
	private static boolean mIsChecked_onlytext = false; // by darwin
	private static int mSelect_Index = 1; // by darwin

	public void clearStaticParam() {
		mIsShareDialogShow = false; // by darwin
		mIsRecreateDialog = false; // by darwin
		mIsCheckboxShow = false; // by darwin
		mIsChecked_hidegrid = false; // by darwin
		mIsChecked_onlytext = false; // by darwin
		mSharedText = "";
		if (mShareBitmap != null) {
			mShareBitmap.recycle();
			mShareBitmap = null;
		}
		mSelect_Index = 1; // by darwin
	}

	private void showShareDialog() {
		if (mShareToDialog == null && mIsShareDialogShow) {
			openShareToDialog();
		} else if (mShareToDialog != null && mShareToDialog.isShowing()) {
			try{ //smilefish fix bug 400010
				mShareToDialog.dismiss();
			}catch(Exception e){
				e.printStackTrace();
			}
			mIsRecreateDialog = true;
			mShareToDialog = null;
			openShareToDialog();
		}
	}
	
	//begin smilefish
	private void readItLater(){
        try {
			if (mShareBitmap != null) {
				mShareBitmap.recycle();
				mShareBitmap = null;
			}
			int width = isPhoneScreen() ? (int) getResources()
			        .getDimension(
			                R.dimen.phone_page_share_bitmap_default_width)
			        : (int) getResources().getDimension(
			                R.dimen.pad_page_share_bitmap_default_width);
			int height = isPhoneScreen() ? (int) getResources()
			        .getDimension(
			                R.dimen.phone_page_share_bitmap_default_height)
			        : (int) getResources().getDimension(
			                R.dimen.pad_page_share_bitmap_default_height);
			mShareBitmap = mEditorUiUtility.getScreenShotNotForPdf(
			        (int) (width), (int) (height), false, false, 0.5f);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();    
			mShareBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        	
        	View animation_view = DoItLaterHelper.getFullActivityView(EditorActivity.this);
        	 
        	Bundle bundle = new Bundle();
        	bundle.putString(DoItLaterHelper.EXTRA_STRING_LATER_TITLE, mNoteBook.getTitle());
        	bundle.putInt(DoItLaterHelper.EXTRA_INT_LATER_TASK_TYPE, DoItLaterHelper.READ_LATER);
        	bundle.putLong(DoItLaterHelper.EXTRA_LONG_LATER_TIME, System.currentTimeMillis());
        	bundle.putBoolean(DoItLaterHelper.EXTRA_BOOLEAN_ISSAVEIMAGE, false);
        	bundle.putByteArray(DoItLaterHelper.EXTRA_LATER_IMAGE, baos.toByteArray());
        	 
        	Intent callbackIntent = new Intent(this, NoteBookPickerActivity.class);
        	callbackIntent.setAction(MetaData.ACTION_READ_LATER);
        	callbackIntent.putExtra(MetaData.BOOK_ID, mBookId);
        	callbackIntent.putExtra(MetaData.PAGE_ID, mPageId);
        	 
        	DoItLaterHelper.sendToLater(EditorActivity.this, animation_view, bundle, null, callbackIntent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//end smilefish

	// darwin
	private void openShareToDialog() {
		Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) this
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = inflater.inflate(R.layout.editor_share_to_dialog,
		        null, false);
		mShareBitmapOriginalWidth = isPhoneScreen() ? (int) getResources().getDimension(
		        R.dimen.phone_page_share_bitmap_default_width)
		        : (int) getResources().getDimension(
		                R.dimen.pad_page_share_bitmap_default_width);
		mShareBitmapOriginalHeight = isPhoneScreen() ? (int) getResources().getDimension(
		        R.dimen.phone_page_share_bitmap_default_height)
		        : (int) getResources().getDimension(
		                R.dimen.pad_page_share_bitmap_default_height);

		mSpinner_preview_size = (Spinner) dialogView
		        .findViewById(R.id.spinner_preview_size);

		mShareCheckBoxHideGrid = (CheckBox) dialogView
		        .findViewById(R.id.dialog_hide_gridline);
		mShareCheckBoxHideGrid.setChecked(mIsChecked_hidegrid);
		mShareCheckBoxHideGrid
		        .setOnCheckedChangeListener(mOnShareHideGridCheckedChange);

		setShareImageSize();
		if (mEditorUiUtility.isEditTexthaveObjects()
		        || (mIsRecreateDialog && mIsCheckboxShow))// &&
		                                                  // !mEditorUiUtility.isTemplatehaveObjects()
		{
			mIsCheckboxShow = true;
			LinearLayout ll = (LinearLayout) dialogView
			        .findViewById(R.id.linearLayout_only_share_text);
			if (ll != null) {
				ll.setVisibility(View.VISIBLE);
			}
			mTextInfoScrollView = (ScrollView) dialogView
			        .findViewById(R.id.dialog_share_textRegionScrollView);
			mTextInfo = (TextView) dialogView
			        .findViewById(R.id.dialog_share_textRegion);
			mShareCheckBoxTextOnly = (CheckBox) dialogView
			        .findViewById(R.id.dialog_share_checkbox_text);
			mShareCheckBoxTextOnly
			        .setOnCheckedChangeListener(mOnShareDataCheckedChange);
			mShareCheckBoxTextOnly.setChecked(mIsChecked_onlytext);
		} else {
			mIsCheckboxShow = false;
			mShareCheckBoxTextOnly = null;
		}

		builder.setPositiveButton(R.string.next, null);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setView(dialogView);

		builder.setTitle(getResources().getString(R.string.editor_menu_share));

		mShareToDialog = builder.create();
		// BEGIN: archie_huang@asus.com
		// To avoid memory leak
		mShareToDialog.setOnDismissListener(null);
		mShareToDialog
		        .setOnDismissListener(new DialogInterface.OnDismissListener() {

			        @Override
			        public void onDismiss(DialogInterface dialog) {
				        if (!mIsRecreateDialog) {
					        mShareToDialog = null;
					        clearStaticParam();
				        } else {
					        mIsRecreateDialog = false;
				        }
				        mIsShareDialogShow = false;// darwin

			        }
		        });
		// END: archie_huang@asus.com
		mShareToDialog.show();
		mShareToDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(
		        mOnShareOKButtonClick);
		mShareToDialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(
		        mOnShareCancelButtonClick);
		mIsShareDialogShow = true;// darwin
	}

	public boolean CalculateImageSize(int showWidth, int showHeight) {
		Bitmap bitmap = null;
		if (mShareBitmap == null || mShareBitmap.isRecycled()) {
			Log.e(TAG, "mShareBitmap == null");
			return false;
		} else {
			bitmap = mShareBitmap;
		}

		return true;
	}
	
	private void setShareImageSize() {
		List<String> allItems = new ArrayList<String>();
		allItems.add(getResources().getString(R.string.share_small) + ":"
		        + String.valueOf((int) (mShareBitmapOriginalWidth * 0.5))
		        + " * "
		        + String.valueOf((int) (mShareBitmapOriginalHeight * 0.5)));
		allItems.add(getResources().getString(R.string.share_medium) + ":"
		        + String.valueOf((int) (mShareBitmapOriginalWidth * 0.75))
		        + " * "
		        + String.valueOf((int) (mShareBitmapOriginalHeight * 0.75)));
		allItems.add(getResources().getString(R.string.share_large) + ":"
		        + String.valueOf((int) (mShareBitmapOriginalWidth * 1)) + " * "
		        + String.valueOf((int) (mShareBitmapOriginalHeight * 1)));
		ArrayAdapter adapter = new ArrayAdapter(this,
		        R.layout.share_spinner_item, allItems);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner_preview_size.setAdapter(adapter);
		mSpinner_preview_size.setSelection(mSelect_Index);
		mSpinner_preview_size
		        .setOnItemSelectedListener(selectPreviewSizeListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//emmanual to add an empty menu and replace it with popup window
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.empty_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	//emmanual
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu.hasVisibleItems()) {
			MenuItem item = menu.findItem(R.id.editor_menu_setting);
			if (item != null) {
				item.setVisible(false);
			}
			return false;
		}
		
		Button btn = null;
		if (mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_READONLE) {
			btn = (Button) findViewById(R.id.note_kb_more);
			if (btn != null) {
				showMenuReadonlyPopupWindow(btn);
			}
		} else {
			switch (mEditorUiUtility.getInputMode()) {
			case InputManager.INPUT_METHOD_KEYBOARD:
				btn = (Button) findViewById(R.id.note_kb_k_more);
				break;
			case InputManager.INPUT_METHOD_SCRIBBLE:
				btn = (Button) findViewById(R.id.note_kb_s_more);
				break;
			case InputManager.INPUT_METHOD_DOODLE:
				btn = (Button) findViewById(R.id.note_kb_d_more);
				break;
			default:
				break;
			}
			if (btn != null) {
				showMenuPopupWindow(btn);
			}
		}
	    return false;
    }

	private void setOptionsMentNone() {
		if (mOptionsMenu == null) {
			return;
		}
		mOptionsMenu.clear();
	}

	// darwin
	public void EnableSaveButton(boolean bEnable) {
		mSaveButtonStatus = bEnable ? SAVE_BUTTON_ENABLE : SAVE_BUTTON_DISABLE;// update
		                                                                       // by
		                                                                       // jason
		if (mSaveMenuItem != null) {
			mSaveMenuItem.setEnabled(bEnable);
			int alpha = bEnable ? ACTIONMENU_ALPHA_ENABLE
			        : ACTIONMENU_ALPHA_DISABLE;
			// mSaveMenuItem.getIcon().setAlpha(alpha);
		}
	}

	private void setMenuItemBookmark() {
		if (mOptionsMenu != null && mBookmarkMenuItem == null) {
			mBookmarkMenuItem = mOptionsMenu
			        .findItem(R.id.editor_menu_favorite_toggle);
		}
		if (mOptionsMenu == null || mBookmarkMenuItem == null
		        || mNotePage == null) {
			return;
		}
		int id = mNotePage.isBookmark() ? R.string.remove_from_list
		        : R.string.add_to_list;
		String title = getResources().getString(id);
		mBookmarkMenuItem.setTitle(title);
	}

	// begin smilefish
	private void startSearchActivity() {
		try {
			Intent intent = new Intent();
			intent.setClass(this, TextSearchActivity.class);
			intent.putExtra("bookId", mNoteBook.getCreatedTime());

			startActivity(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// end smilefish
	private void showUserVoice() {
	    // TODO Auto-generated method stub
		UserVoiceConfig.init(this);//mEditorUiUtility.getContext());
    }

	// private boolean mIsPressCtrl = false;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mEditorUiUtility == null) {
				return false;
			}
			int mode = mEditorUiUtility.getInputMode();
			if (mode == InputManager.INPUT_METHOD_INSERT
			        || mode == InputManager.INPUT_METHOD_SELECTION_DOODLE
			        || mode == InputManager.INPUT_METHOD_SELECTION_TEXT
			        || mode == InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD
			        || mode == InputManager.INPUT_METHOD_TEXT_IMG_SCRIBBLE) {
				mEditorUiUtility.setInputMode(mEditorUiUtility
				        .getInputModeFromPreference());
				// BEGIN: archie_huang@asus.com
				// After insert graphic from the modes other than Doodle mode
				// Doodle need to change the draw tool to the previous tool
				if (!mIsEraserOn) {// by show
					mEditorUiUtility.setDoodleTool(mDoodleToolCode);
				}
				// END: archie_huang@asus.com
				MetaData.INSERT_PHOTO_SELECTION = false; // Carol
				return true;
			}

			// begin smilefish for color picker
			if (mode == InputManager.INPUT_METHOD_COLOR_PICKER) {
				if (mIsColorChosen) {
					mColorPickerDoneButton.setEnabled(false);
					mColorPickerHint.setVisibility(View.VISIBLE);
					mColorChosenShow.setVisibility(View.GONE);
					mIsColorChosen = false;
					mColorPickerSnapView.setColorXY(0, 0);
				} else {
					mColorPickerViewStub.setVisibility(View.GONE);
					releaseSnapBitmapMemory();
					mEditorUiUtility.setInputMode(mEditorUiUtility
					        .getInputModeFromPreference());
				}
				return true;
			}
			// end smilefish
		}

		boolean superRes = super.onKeyDown(keyCode, event);

		return superRes;
	}
	
	//emmanual to fix bug 576211
	public void cancelBoxEditText(){
		if (!mIsEraserOn) {
			mEditorUiUtility.setDoodleTool(mDoodleToolCode);
		}
	}

	// Begin: show_wang@asus.com
	// Modified reason: for new layout
	private void setfuncBarViewGone(int setSelectionId) {
		if (setSelectionId != R.id.note_kb_scribble) {
			if (mScribleFuncs != null)
				mScribleFuncs.setVisibility(View.INVISIBLE);
		}
		if (setSelectionId != R.id.note_kb_keyboard) {
			if (mKeyboardFuncs != null)
				mKeyboardFuncs.setVisibility(View.INVISIBLE);
		}
		if (setSelectionId != R.id.note_kb_doodle) {
			if (mDoodleFuncs != null)
				mDoodleFuncs.setVisibility(View.INVISIBLE);
		}
		if (mSelectionTextFuncs != null)
			mSelectionTextFuncs.setVisibility(View.INVISIBLE);
		if (mReadOnlyFuncs != null)
			mReadOnlyFuncs.setVisibility(View.INVISIBLE);
		if (mInsertFuncs != null)
			mInsertFuncs.setVisibility(View.INVISIBLE);
		if (mSelectionDoodle != null)
			mSelectionDoodle.setVisibility(View.INVISIBLE);
		if (mTextImgKeyboardFunc != null)
			mTextImgKeyboardFunc.setVisibility(View.INVISIBLE);
		if (mTextImgScribbleFunc != null)
			mTextImgScribbleFunc.setVisibility(View.INVISIBLE);
		if (mMemoFuncs != null)
			mMemoFuncs.setVisibility(View.INVISIBLE);
		if (mColorPickerFuncs != null)
			mColorPickerFuncs.setVisibility(View.INVISIBLE); // smilefish
	}

	// End: show_wang@asus.com

	private void setBottomButtonGone() {
		if (mKeyboardButtons != null)
			mKeyboardButtons.setVisibility(View.GONE);
		if (mScribbleButtons != null)
			mScribbleButtons.setVisibility(View.GONE);
		if (mDoodleButtons != null)
			mDoodleButtons.setVisibility(View.GONE);
		setScrollBarBottom(true);
	}

	int mOriginalBottom = 0;

	// add by emmanual_chen
	public void setScrollBarBottom(boolean isBottom) {
		if(mEditorUiUtility.getHandWritingView().getEnable()){
			return ;
		}
		EditorScrollBarContainer mEditTextHorizontScrollerContainer = (EditorScrollBarContainer) findViewById(R.id.horizontalScrollBarContainer);
		EditorScrollBarContainer mEditTextVerticalScrollerContainer = (EditorScrollBarContainer) findViewById(R.id.verticalScrollBarContainer);
		FrameLayout.LayoutParams horizontalParams = (FrameLayout.LayoutParams) (mEditTextHorizontScrollerContainer
		        .getLayoutParams());
		FrameLayout.LayoutParams verticalParams = (FrameLayout.LayoutParams) (mEditTextVerticalScrollerContainer
		        .getLayoutParams());

		if (verticalParams.bottomMargin == 0) {
			return;
		}
		if (verticalParams.bottomMargin != 30) {
			mOriginalBottom = verticalParams.bottomMargin;
		}
		if (isBottom) {
			verticalParams.bottomMargin = 30;
			if (mEditTextHorizontScrollerContainer != null) {
				horizontalParams.bottomMargin = 30;
			}
		} else {
			verticalParams.bottomMargin = mOriginalBottom;
			if (mEditTextHorizontScrollerContainer != null) {
				horizontalParams.bottomMargin = mOriginalBottom;
			}
		}
	}

	private void showBottomButtonVisible() {
		View bottomButtonContainer = findViewById(R.id.bottom_button_container);
		bottomButtonContainer.setVisibility(View.VISIBLE);
	}

	public void onInputModeChange(int inputMode) {
		dismissClipboard(); // Better
		// find layout
		MetaData.READ_ONLY_MODE = false;
		switch (inputMode) {
		case InputManager.INPUT_METHOD_KEYBOARD:
			mKeyboardFuncs = prepareFuncButtons(mKeyboardFuncs,
			        R.id.keyboardfuncViewStub,
			        mEditorIdList.funcButtonIdsKeyboard, R.id.note_kb_keyboard);
			mKeyboardButtons = prepareButtomButton(mKeyboardButtons,
			        R.id.keyboardMenuViewStub,
			        mEditorIdList.bottomButtonIdsKeyboard);
			mKeyboardFuncs.setVisibility(View.VISIBLE);// by show
			setScrollBarBottom(false);
			dismissAllPopupWindow();
			break;
		case InputManager.INPUT_METHOD_SCRIBBLE:
			if (mScribbleButtons == null) {
				showBottomButtonVisible();
			}
			mScribleFuncs = prepareFuncButtons(mScribleFuncs,
			        R.id.scribblefuncViewStub,
			        mEditorIdList.funcButtonIdsScribble, R.id.note_kb_scribble);
			mScribbleButtons = prepareButtomButton(mScribbleButtons,
			        R.id.scribbleMenuViewStub,
			        mEditorIdList.bottomButtonIdsScrible);
			mScribleFuncs.setVisibility(View.VISIBLE);// by show
			setScrollBarBottom(false);
			hiddenSoftKeyboard();
			dismissAllPopupWindow();
			PageEditor mPageEditor = mEditorUiUtility.getPageEditor();
			mPageEditor.updateVerticalScrollerHeight();
			break;
		case InputManager.INPUT_METHOD_DOODLE:
			if (mDoodleButtons == null) {
				showBottomButtonVisible();
			}
			mDoodleFuncs = prepareFuncButtons(mDoodleFuncs,
			        R.id.doodlefuncViewStub, mEditorIdList.funcButtonIdsDoodle,
			        R.id.note_kb_doodle);
			mDoodleButtons = prepareButtomButton(mDoodleButtons,
			        R.id.doodleMenuViewStub,
			        mEditorIdList.bottomButtonIdsOthers);
			mDoodleButtons.setVisibility(View.VISIBLE);// by show
			setScrollBarBottom(false);
			hiddenSoftKeyboard();
			dismissAllPopupWindow();
			if (mIsPalette && mIsCustomColorSet) // smilefish
			{
				mEditorUiUtility.changeColor(mCustomColor); // By Show
			} else
				mEditorUiUtility
				        .changeColor(mDefaultColorCodes[mSelectedColorIndex]);
			// Begin: show_wang@asus.com
			// Modified reason: for dds
			if (mIsEraserOn) {
				mEditorUiUtility.setDoodleTool(DrawTool.ERASE_TOOL);
				mEditorUiUtility.changeScribleStroke(mEraserWidth);
				selectThisDoodleTool(mCurrentDoodleEraserButton);
			} else {
				// End: show_wang@asus.com
				mEditorUiUtility.setDoodleTool(mDoodleToolCode);
				mEditorUiUtility.changeScribleStroke(mStrokeWidth);
				selectThisDoodleTool(mCurrentDoodleBrushButton);
				SetCurAttr(mDoodleToolCode);
			}

			setAutoRecognizerShapeState(true);// RICHARD fix bug 163
			break;
		case InputManager.INPUT_METHOD_SELECTION_TEXT:
			mSelectionTextFuncs = prepareFuncButtons(mSelectionTextFuncs,
			        R.id.selectiontextfuncViewStub,
			        mEditorIdList.funcButtonIdsSelectionText, -1);
			mSelectionTextFuncs.setVisibility(View.VISIBLE);// by show
			setBottomButtonGone();
			setOptionsMentNone();
			break;
		case InputManager.INPUT_METHOD_READONLE:
			MetaData.READ_ONLY_MODE = true;
			hiddenSoftKeyboard();
			if (mDoodleButtons == null) {
				showBottomButtonVisible();
			}
			mReadOnlyFuncs = prepareFuncButtons(mReadOnlyFuncs,
			        R.id.readonlyViewStub, mEditorIdList.funcButtonIdsReadOnly,
			        -1);
			mDoodleButtons = prepareButtomButton(mDoodleButtons,
			        R.id.doodleMenuViewStub,
			        mEditorIdList.bottomButtonIdsOthers);
			mReadOnlyFuncs.setVisibility(View.VISIBLE);// by show
			boolean landscape_phonePage = (mNoteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE)
			        && (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT);
			if (mNoteBook != null
			        && (mNoteBook.getPageSize() == MetaData.PAGE_SIZE_PAD || landscape_phonePage)) { // Carol
				                                                                                     // -
				                                                                                     // add
				                                                                                     // landscape
				                                                                                     // phone
				                                                                                     // page
				showToastAtPhoneMode(R.string.editor_phone_toast_readonly);
			}
			break;
		case InputManager.INPUT_METHOD_INSERT:
			mInsertFuncs = prepareFuncButtons(mInsertFuncs,
			        R.id.insertViewStub, mEditorIdList.funcButtonIdsInsert, -1);
			mInsertFuncs.setVisibility(View.VISIBLE);// by show
			// BEGIN: archie_huang@asus.com
			setBottomButtonGone();
			// END: archie_huang@asus.com
			showEditorHint(getResources().getString(
			        R.string.editor_bottom_hint_selected_mode));
			showToastAtPhoneMode(R.string.editor_bottom_hint_selected_mode);
			setOptionsMentNone();
			if (MetaData.INSERT_PHOTO_SELECTION) // Carol
				mEditorUiUtility.setDoodleTool(DrawTool.SELECTION_TOOL);
			break;
		case InputManager.INPUT_METHOD_SELECTION_DOODLE:
			hiddenSoftKeyboard();
			mSelectionDoodle = prepareFuncButtons(mSelectionDoodle,
			        R.id.selectiondoodlefuncViewStub,
			        mEditorIdList.funcButtonIdsSelectionDoodle, -1);
			mSelectionDoodle.setVisibility(View.VISIBLE);// by show
			// BEGIN: archie_huang@asus.com
			setBottomButtonGone();
			// END: archie_huang@asus.com
			showEditorHint(getResources().getString(
			        R.string.editor_bottom_hint_selected_mode));
			setOptionsMentNone();
			// BEGIN: archie_huang@asus.com
			mEditorUiUtility.setDoodleTool(DrawTool.SELECTION_TOOL);
			// END: archie_huang@asus.com
			break;
		case InputManager.INPUT_METHOD_TEXT_IMG_KEYBOARD:
			mTextImgKeyboardFunc = prepareFuncButtons(mTextImgKeyboardFunc,
			        R.id.textimgkeyboardfuncViewStub,
			        mEditorIdList.funcButtonIdsTextImgKeyboard,
			        R.id.note_kb_textimg_keyboard);
			mTextImgKeyboardFunc.setVisibility(View.VISIBLE);// by show
			setOptionsMentNone();
			break;
		case InputManager.INPUT_METHOD_TEXT_IMG_SCRIBBLE:
			mTextImgScribbleFunc = prepareFuncButtons(mTextImgScribbleFunc,
			        R.id.textimgscribblefuncViewStub,
			        mEditorIdList.funcButtonIdsTextImgScribble,
			        R.id.note_kb_textimg_scribble);
			setBottomButtonGone();// smilefish
			mTextImgScribbleFunc.setVisibility(View.VISIBLE);// by show
			setOptionsMentNone();
			break;
		case InputManager.INPUT_METHOD_COLOR_PICKER: // smilefish
			mColorPickerFuncs = prepareFuncButtons(mColorPickerFuncs,
			        R.id.colorpickerfuncViewStub,
			        mEditorIdList.funcButtonIdsColorPicker, -1);
			mColorPickerFuncs.setVisibility(View.VISIBLE);
			setBottomButtonGone();
			setOptionsMentNone();
			break;
		}
		
		if (inputMode == InputManager.INPUT_METHOD_KEYBOARD
		        || inputMode == InputManager.INPUT_METHOD_SCRIBBLE
		        || inputMode == InputManager.INPUT_METHOD_DOODLE) {
			CursorIconLibrary
			        .tryStylusIcon(
			                (android.hardware.input.InputManager) getSystemService(Context.INPUT_SERVICE),
			                templateLinearLayout,
			                templateLinearLayout.getAirViewDrawableId());
			CursorIconLibrary.setStylusIcon(templateLinearLayout, templateLinearLayout
			        .getAirViewDrawableId());
		}
	}

	private void showEditorHint(String text) { // smilefish fix bug 322207
		if (deviceType <= 100) {
			return;
		}

		showToast(this, text);
	}

	private View prepareButtomButton(View buttons, int buttonsContainerId,
	        int[] buttonIds) {
		setBottomButtonGone();
		if (buttons == null) {
			// BEGIN: archie_huang@asus.com
			buttons = prepareViewStub(buttonsContainerId, buttonIds,
			        mBottomFuncClickListener, null, mBottomFuncTouchListener,
			        -1);
			// END: archie_huang@asus.com
		}
		mCurrentPageNumberTextView = (TextView) buttons
		        .findViewById(R.id.textview_page_number);
		mTotalPageNumberTextView = (TextView) buttons
		        .findViewById(R.id.textview_page_number_total);
		mBottomMackView = buttons.findViewById(R.id.bottom_mask);
		if (mNoteBook != null) {
			if (mCurrentPageNumberTextView != null) {
				mCurrentPageNumberTextView.setText(""
				        + (mNoteBook.getPageIndex(mPageId) + 1));
			}
			if (mTotalPageNumberTextView != null) {
				mTotalPageNumberTextView.setText(""
				        + mNoteBook.getTotalPageNumber());
			}

			// set mask view

			int bottomMackViewWidth = getResources().getInteger(
			        R.integer.bottomMackView_Width);
			if (mBottomMackView != null) {
				if ((deviceType > 100)
				        && mNoteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE) {
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
						mBottomMackView.setVisibility(View.GONE);
					} else {
						mBottomMackView.getLayoutParams().width = bottomMackViewWidth;
					}
				}

				// Begin: show_wang@asus.cpm
				// Modified reason: landspace
				if ((deviceType < 100)
				        && mNoteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE) {
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						mBottomMackView.getLayoutParams().width = bottomMackViewWidth;
					}
				}
				// End: show_wang@asus.cpm

				if (mNoteBook.getBookColor() == MetaData.BOOK_COLOR_YELLOW) {
					mBottomMackView.setBackgroundDrawable(getResources()
					        .getDrawable(R.drawable.content_bg_bar_yellow));
				}
			}
		}

		// emmanual for airview
		if (MethodUtils.isEnableAirview(getApplicationContext())) {
			for (int i : buttonIds) {
				View v = buttons.findViewById(i);
				if (v != null) {
					CursorIconLibrary.setStylusIcon(v,
					        CursorIconLibrary.STYLUS_ICON_FOCUS);
					v.setOnTouchListener(mAirTextViewTool);
					v.setOnHoverListener(mAirTextViewTool);
				}
			}
		}
		
		buttons.setVisibility(View.VISIBLE);
		setScrollBarBottom(false);
		
		// begin smilefish fixed bug 400840
		mInsertView = buttons.findViewById(R.id.note_kb_insert);
		if(MetaData.READ_ONLY_MODE)
			mInsertView.setVisibility(View.INVISIBLE);
		else
			mInsertView.setVisibility(View.VISIBLE);
		// end smilefish

		return buttons;
	}

	private View prepareFuncButtons(View funcView, int buttonsContainerId,
	        int[] buttonIds, int setSelectionId) {
		setfuncBarViewGone(setSelectionId);
		if (funcView == null) {
			OnLongClickListener listener = (deviceType > 100) ? null
			        : mFuncIconLongClickListener;
			// BEGIN: archie_huang@asus.com
			funcView = prepareViewStub(buttonsContainerId, buttonIds,
			        mFuncBarClickListener, listener, null, setSelectionId);
			// END: archie_huang@asus.com
		}
		if (funcView == null) {
			return null;
		}
		// for airview
		if (MethodUtils.isEnableAirview(getApplicationContext())) {
			for (int i : buttonIds) {
				View v = funcView.findViewById(i);
				if (v != null) {
					CursorIconLibrary.setStylusIcon(v,
					        CursorIconLibrary.STYLUS_ICON_FOCUS);
					v.setOnTouchListener(mAirTextViewTool);
					v.setOnHoverListener(mAirTextViewTool);
				}
			}
		}
		//
		funcView.setVisibility(View.VISIBLE);

		return funcView;
	}

	private OnLongClickListener mFuncIconLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			//emmanual to fix bug 479888
			if (((Button) v).getText().toString().equals("")) {
				if (v.getId() == R.id.note_kb_textimg_done) {
					showToast(EditorActivity.this,
					        R.string.editor_func_button_done);
				} else if (v.getId() == R.id.note_kb_textimg_cancle) {
					showToast(EditorActivity.this,
					        R.string.editor_func_button_cancle);
				}
			} else {
				showToast(EditorActivity.this, ((Button) v).getText().toString());
			}
			return true;
		}
	};

	private View prepareViewStub(int viewStubId, int[] ids,
	        OnClickListener onClickListener,
	        OnLongClickListener onLongClickListener,
	        OnTouchListener onTouchListener, int setSelectionId) {
		ViewStub viewstub = (ViewStub) findViewById(viewStubId);
		if (viewstub == null) {
			Log.e(TAG, "Cannot find view stub id = 0x" + Integer.toHexString(viewStubId));
			return null;
		}

		View view = viewstub.inflate();
		View subView = null;
		for (int id : ids) {
			subView = view.findViewById(id);
			if (subView == null) {
				Log.e(TAG,
				        "subView not found! id = 0x" + Integer.toHexString(id));
				continue;
			}
			subView.setFocusable(false);
			subView.setOnClickListener(onClickListener);
			subView.setOnLongClickListener(onLongClickListener);
			// BEGIN: archie_huang@asus.com
			subView.setOnTouchListener(onTouchListener);
			// END: archie_huang@asus.com
			if (id == setSelectionId) {
				subView.setSelected(true);
			}
			// find out some special button.
			if (id == R.id.note_kb_d_ungroup) {
				setViewAndAddViewToList(subView, mUnGroupButtons,
				        mEditorUiUtility.isDoodleUnGroupAvailable());
			}

			if (id == R.id.note_kb_d_group) {
				setViewAndAddViewToList(subView, mGroupButtons,
				        mEditorUiUtility.isDoodleUnGroupAvailable());
			}

			if (viewStubId != R.id.selectiontextfuncViewStub) {
				if (id == R.id.note_kb_copy) {
					setViewAndAddViewToList(subView, mDoodleSelectPastButtons,
					        mEditorUiUtility.isDoodlePastAvailable());
				}
			}

			// BEGIN:Show
			if (id == R.id.note_kb_d_crop) {
				setViewAndAddViewToList(subView, mDoodleSelectCropButtons,
				        mEditorUiUtility.isDoodleCropAvailable());
			}

			if (id == R.id.note_kb_d_textedit) {
				setViewAndAddViewToList(subView, mDoodleSelectTextEditButtons,
				        mEditorUiUtility.isDoodleTextEditAvailable());
			}

			// END:Show

			// BEGIN: RICHARD
			// begin darwin
			if (CFG.getCanDoVO() == false) {
				if (id == R.id.note_kb_d_shape
				        || id == R.id.note_kb_sel_write_to_type
				        || id == R.id.note_kb_write_to_type) {
					subView.setEnabled(false);
					subView.setVisibility(View.GONE);
				}
			} else {
				// end darwin
				if (id == R.id.note_kb_write_to_type) {
					mWTToggleButton = (ToggleButton) subView;
					if (isLastWTToggleButtonChecked) {
						mWTToggleButton.setChecked(true);
						mWTToggleButton.setSelected(true);
						mEditorUiUtility
						        .setIsAutoChangeWriteToType(mWTToggleButton
						                .isChecked());
					}
				}
				if (id == R.id.note_kb_d_shape) {
					mRecognizerShapeButton = (ToggleButton) subView;
					if (isLastRecognizerShapeButtonChecked) {
						mRecognizerShapeButton.setChecked(true);
						mRecognizerShapeButton.setSelected(true);
						setAutoRecognizerShapeState(true);
					}
				}
			}
			// END: RICHARD

			if (id == R.id.note_kb_color_bold) {
				mColorButtons.add(subView);
				mBoldButtons.add(subView);
				setColorButton(mEditorUiUtility.getPaintColor());
				setBoldButton(mEditorUiUtility.getTextStyle());
			}

			if (id == R.id.note_kb_undo
			        && viewStubId == R.id.doodlefuncViewStub) {
				setViewAndAddViewToList(subView, mDoodleUndoButtons,
				        mEditorUiUtility.isDoodleUndoStackAvailable());
			} else if (id == R.id.note_kb_undo
			        && (viewStubId == R.id.textimgkeyboardfuncViewStub || viewStubId == R.id.textimgscribblefuncViewStub)) {

				setViewAndAddViewToList(subView, mBoxUndoButtons,
				        mEditorUiUtility.isBoxEditorTextUndoStackAvailable());

			} else if (id == R.id.note_kb_undo) {

				setViewAndAddViewToList(subView, mNoteUndoButtons,
				        mEditorUiUtility.isNoteEditTextUndoStackAvailable());
			}
			// ///////////////////////////////////////////////////////////////////////////////////
			if (id == R.id.note_kb_redo
			        && viewStubId == R.id.doodlefuncViewStub) {
				setViewAndAddViewToList(subView, mDoodleRedoButtons,
				        mEditorUiUtility.isDoodleRedoStackAvailable());
			} else if (id == R.id.note_kb_redo
			        && (viewStubId == R.id.textimgkeyboardfuncViewStub || viewStubId == R.id.textimgscribblefuncViewStub)) {

				setViewAndAddViewToList(subView, mBoxRedoButtons,
				        mEditorUiUtility.isBoxEditorTextRedoStackAvailable());

			} else if (id == R.id.note_kb_redo) {

				setViewAndAddViewToList(subView, mNoteRedoButtons,
				        mEditorUiUtility.isNoteEditTextRedoStackAvailable());
			}

			if (id == R.id.note_kb_d_brush) {
				if (mIsPalette && mIsCustomColorSet) // smilefish
				{
					mEditorUiUtility.changeColor(mCustomColor); // By Show
				} else
					mEditorUiUtility
					        .changeColor(mDefaultColorCodes[mSelectedColorIndex]);

				((Button) subView).setCompoundDrawablesWithIntrinsicBounds(
				        null,
				        drawCurrentBrushThickness(drawCurrentBrushType()),
				        null, null);

			}
			if (id == R.id.note_kb_d_brush || id == R.id.note_kb_d_eraser) {
				mDoodleFuncButtons.add(subView);
				if (id == R.id.note_kb_d_brush) {
					mCurrentDoodleBrushButton = (Button) subView;
				}
			}
			// Begin: show_wang@asus.com
			// Modified reason: eraser init Icon
			if (id == R.id.note_kb_d_eraser) {
				// begin smilefish
				Drawable drawableTop = getResources().getDrawable(
				        drawCurrentEraserIcon());
				((Button) subView).setCompoundDrawablesWithIntrinsicBounds(
				        null, drawableTop, null, null);
				// end smilefish

				mCurrentDoodleEraserButton = (Button) subView; // by show
			}
			// End: show_wang@asus.com

			if (id == R.id.note_kb_color_done) { // smilefish
				mColorPickerDoneButton = (Button) subView;
			}
		}
		return view;
	}

	private int getIdFrom(int brushValue) {
		switch (brushValue) {
		case DrawTool.NORMAL_TOOL:
			return R.id.editor_func_d_brush_normal;
		case DrawTool.SCRIBBLE_TOOL:
			return R.id.editor_func_d_brush_scribble;
		case DrawTool.NEON_TOOL:
			return R.id.editor_func_d_brush_mark;
		case DrawTool.SKETCH_TOOL:
			return R.id.editor_func_d_brush_sketch;
			// begin wendy
		case DrawTool.MARKPEN_TOOL:
			return R.id.editor_func_d_brush_markpen;
		case DrawTool.WRITINGBRUSH_TOOL:
			return R.id.editor_func_d_brush_writingbrush;
			// end wendy
		}
		return DrawTool.NORMAL_TOOL;
	}

	// BEGIN: archie_huang@asus.com
	private int getEraserIdFrom(float widthValue) {
		if (widthValue == MetaData.DOODLE_ERASER_WIDTHS[0]) {
			return R.id.editor_func_eraser_3;
		}
		if (widthValue == MetaData.DOODLE_ERASER_WIDTHS[1]) {
			return R.id.editor_func_eraser_2;
		}
		if (widthValue == MetaData.DOODLE_ERASER_WIDTHS[2]) {
			return R.id.editor_func_eraser_1;
		}
		return 0;
	} // END: archie_huang@asus.com

	// BEGIN: archie_huang@asus.com
	private void selectCurrentEraserWidthOnPopup() {
		if (mDoodleEraserPopupWindow == null
		        || !mDoodleEraserPopupWindow.isShowing()) {
			return;
		}
		for (int id : mEditorIdList.editorDoodleEraserIds) {
			View v = mDoodleEraserPopupWindow.getContentView().findViewById(id);
			v.setSelected(false);
			if (v.getId() == getEraserIdFrom(mEraserWidth)) {
				v.setSelected(true);
			}
		}
	} // END: archie_huang@asus.com

	private void selectCurrentBrushOnPopup() {
		if (mDoodleBrushPopupWindow == null
		        || !mDoodleBrushPopupWindow.isShowing()) {
			return;
		}
		for (int id : mEditorIdList.editorDoodleBrushIds) {
			View v = mDoodleBrushPopupWindow.getContentView().findViewById(id);
			v.setSelected(false);
			if (v.getId() == getIdFrom(mDoodleToolCode)) {
				v.setSelected(true);
			}
		}
		// BEGIN: Show
		int colorid = getColorId();
		for (int id : mEditorIdList.editorDoodleUnityColorIds) {
			View v = mDoodleBrushPopupWindow.getContentView().findViewById(id);
			if (v != null) // smilefish
			{
				v.setSelected(false);
				((ImageView) v).setImageResource(R.drawable.asus_color_frame_n); // smilefish
				if (v.getId() == colorid) {
					v.setSelected(true);
					((ImageView) v)
					        .setImageResource(R.drawable.asus_color_frame_p);
				}
			}
		}
		// END: Show

		ImageButton selectColor = (ImageButton) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.select_color);
		Drawable background1 = CoverHelper.createRoundColorBackground(
		        EditorActivity.this, R.drawable.asus_popup_pen_triangle_2,
		        mEditorUiUtility.getDoodlePaint().getColor());
		selectColor.setBackgroundDrawable(background1);
		TextView colorName = (TextView) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.select_color_name);
		colorName.setText(ColorHelper.displayColorName(mEditorUiUtility
		        .getDoodlePaint().getColor())); // Carol-choose color from grid

		Drawable background = CoverHelper.createGradientColorAndCover(
		        EditorActivity.this, R.drawable.asus_pen_scrollbar_bg,
		        mEditorUiUtility.getDoodlePaint().getColor());
		SeekBar_Alpha.setBackgroundDrawable(background);
	}

	private void setViewAndAddViewToList(View view, List<View> list,
	        boolean enable) {
		if (view == null) {
			Log.e(TAG, "setViewAndAddView view == null");
		}
		float alpha = enable ? BUTTON_ALPHA_ENABLE : BUTTON_ALPHA_DISABLE;
		view.setAlpha(alpha);
		view.setEnabled(enable);
		list.add(view);
	}

	private void preparePopupWindowInsert(final View view) {

		if (view != null) {
			view.setSelected(true);
			mCurrentPopupParentView = view;
		}// mars fix for monkey test
		else {
			return;
		}
//		if (mInsertPopupWindow == null) {
			LayoutInflater inflater = (LayoutInflater) this
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View popupView = inflater.inflate(
			        R.layout.editor_func_popup_insert, null, false);

			String[] countries = getResources().getStringArray(
			        R.array.editor_func_insert_array);
			// Begin : Show_wang@asus.com
			// Modified reason : update insert popupwindow
			// begin jason
			int[] resIds = null;
			resIds = new int[] {
			        R.drawable.asus_insert_photo, // Carol-use selector instead
			                                      // of pic
			        R.drawable.asus_insert_pic, R.drawable.asus_insert_text,
			        R.drawable.asus_insert_pdf, R.drawable.asus_insert_audio,
			        R.drawable.asus_memo_voice_ic,//emmanual
			        R.drawable.asus_insert_video,
			        R.drawable.asus_insert_textfile,
			        R.drawable.asus_insert_time, R.drawable.asus_insert_clip,
			        R.drawable.asus_insert_stamp, R.drawable.asus_insert_shape };
			if (countries.length < INSERT_MENU_NUM) {
				ArrayList<String> templist = new ArrayList<String>(
				        INSERT_MENU_NUM);
				for (String c : countries) {
					templist.add(c);
				}
				templist.add(INSERT_AUDIO_FILE, getResources().getString(R.string.insert_audiofile));//emmanual
				countries = templist.toArray(countries);//emmanual
			}
			// end
			int index = 0;
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < INSERT_MENU_NUM; i++) {
				if (!MetaData.HasMultiClipboardSupport
				        && ((i == INSERT_PDF_PAGE && !MethodUtils.isPenDevice(getApplicationContext())) || 
				        		(i == INSERT_FROM_CLIPBOARD))
						//emmanual to fix bug 495530
				        || (getCurrentFocus().getId() == R.id.travel_title_edit || getCurrentFocus().getId() == R.id.topic_edit 
				        	|| getCurrentFocus().getId() == R.id.attendee_edit)
				        	&& (i == INSERT_AUDIO_FILE || i == SOUND_RECORDER || i == VIDEO_CAPTURE)) {
					mInsertMenuIndex[i] = -1;
					continue;
				}

				mInsertMenuIndex[i] = index;
				index++;
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("text", countries[i]);
				map.put("img", resIds[i]);
				list.add(map);
			}

			SimpleAdapter adapter = new SimpleAdapter(this, list,
			        R.layout.editor_insert_list_item, new String[] { "text",
			                "img" }, new int[] { R.id.listitem_text,
			                R.id.listitem_img });
			// End : Show_wang@asus.com
			ListView listView = (ListView) popupView
			        .findViewById(R.id.insertlistView);
			// listView.setAdapter(new ArrayAdapter<String>(this,
			// R.layout.editor_insert_list_item, countries));
			listView.setAdapter(adapter);
			listView.setItemsCanFocus(false);
			listView.setOnItemClickListener(mInsertListItemClickListener);

			mInsertPopupWindow = new PopupWindow(popupView,
			        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);

			mInsertPopupWindow.setFocusable(true);
			mInsertPopupWindow.setOutsideTouchable(true);
			mInsertPopupWindow.setTouchable(true);

			mInsertPopupWindow.setTouchInterceptor(mOnPupupWindowOnTouch);
			mInsertPopupWindow.update();
			mInsertPopupWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					if (mCurrentPopupParentView != null) {
						mCurrentPopupParentView.setSelected(false);
					}
				}

			});
//		}
		mEditorUiUtility.hiddenSoftKeyboard();

		// BEGIN: Shane_Wang 2012-9-26 :27:41
		int positionX = 0;
		int positionY = 0;
		if (MetaData.HasMultiClipboardSupport) {
			positionX = (int) getResources().getDimension(
			        R.dimen.insert_dropdown_lmr_position_x);
			positionY = (int) (getResources().getDimension(
			        R.dimen.insert_dropdown_lmr_position_y) * 11 / 9);
		} else {
			positionX = (int) getResources().getDimension(
			        R.dimen.insert_dropdown_lmr_position_x);
			positionY = (int) getResources().getDimension(
			        R.dimen.insert_dropdown_lmr_position_y);
		}
		
		//emmanual to adjust popup window height with pdf
		View listItem = adapter.getView(0, null, listView);
		listItem.measure(0, 0);
		int itemHeight = listItem.getMeasuredHeight();
		if(mInsertMenuIndex[INSERT_PDF_PAGE] != -1 
				&& getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			positionY -= itemHeight;
		}
		//emmanual to fix bug 495530
		if ((getCurrentFocus().getId() == R.id.travel_title_edit
		        || getCurrentFocus().getId() == R.id.topic_edit || getCurrentFocus()
		        .getId() == R.id.attendee_edit)
		        && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			positionY += itemHeight * 3;
		}

		// END: Shane_Wang 2012-9-26 :28:01
		mInsertPopupWindow.setBackgroundDrawable(getResources().getDrawable(
		        R.drawable.asus_insert_popup));
		mInsertPopupWindow.showAsDropDown(view, positionX, positionY);
	}

	public void preparePopupWindowInsert() {
		if (mInsertView == null) {
			return;
		}
		preparePopupWindowInsert(mInsertView);
	}

	private PopupWindow preparePopupWindowImageView(final View view,
	        PopupWindow popupWindow, int popupWindowId, int[] ids,
	        OnClickListener onClickListener) {
		float x = 0, y = 0;
		if (view != null) {
			mCurrentPopupParentView = view;
			view.setSelected(true);
		}
		if (popupWindow == null) {
			LayoutInflater inflater = (LayoutInflater) this
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View imageViewContainer = inflater.inflate(popupWindowId, null,
			        false);
			popupWindow = new PopupWindow(imageViewContainer,
			        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);
			View subView = null;
			for (int id : ids) {
				subView = imageViewContainer.findViewById(id);
				if (subView == null) {
					Log.e(TAG,
					        "subView not found! id = 0x"
					                + Integer.toHexString(id));
				}

				if (id == R.id.note_kb_redo) { // Better
					setViewAndAddViewToList(subView, mNoteRedoButtons,
					        mEditorUiUtility.isNoteEditTextRedoStackAvailable());
				}

				if (id == R.id.editor_func_color_black
				        || id == R.id.editor_func_color_blue
				        || id == R.id.editor_func_color_green
				        || id == R.id.editor_func_color_red) {
					mPopupColors.add(subView);
				}
				if (id == R.id.editor_func_bold_false
				        || id == R.id.editor_func_bold_true) {
					mPopupBolds.add(subView);
				}
				if (id == R.id.note_kb_scribble || id == R.id.note_kb_keyboard
				        || id == R.id.note_kb_doodle) {
					mPopupMode.add(subView);
					int mode = mEditorUiUtility.getInputModeFromPreference();
					if (id == R.id.note_kb_scribble
					        && mode == InputManager.INPUT_METHOD_SCRIBBLE) {
						subView.setSelected(true);
					}
					if (id == R.id.note_kb_doodle
					        && mode == InputManager.INPUT_METHOD_DOODLE) {
						subView.setSelected(true);
					}
					if (id == R.id.note_kb_keyboard
					        && mode == InputManager.INPUT_METHOD_KEYBOARD) {
						subView.setSelected(true);
					}
				}
				if (id == R.id.editor_func_eraser_1
				        || id == R.id.editor_func_eraser_2
				        || id == R.id.editor_func_eraser_3) {
					mPopupEraser.add(subView);
				}
				if (subView != null) // smilefish
					subView.setOnClickListener(onClickListener);
			}

			if ((popupWindowId == R.layout.editor_func_popup_color_bold)
			        && mEditorUiUtility != null) {
				setColorButton(mEditorUiUtility.getPaintColor());
				setBoldButton(mEditorUiUtility.getTextStyle());
			}

			int bgResId = R.drawable.asus_supernote_dropdown_r;

			if (popupWindowId == R.layout.editor_func_popup_d_eraser) {
				bgResId = R.drawable.asus_supernote_dropdown_eraser;
			}

			if ((popupWindowId == R.layout.editor_func_popup_mode)
			        && deviceType <= 100) {
				bgResId = R.drawable.asus_supernote_mode_dropdownbg;
			}

			if (popupWindowId == R.layout.editor_func_popup_color_bold) {
				bgResId = R.drawable.asus_supernote_dropdown_r;
			}
			// Begin Dave.To fix the bug: keyboard will show and disappear one
			// time in keyboard_input_mode.
			if (popupWindowId != R.layout.editor_func_popup_mode) {
				popupWindow.setFocusable(true);
			}
			// End Dave
			popupWindow.setTouchable(true);
			popupWindow.setOutsideTouchable(true);
			// begin smilefish
			if (popupWindowId == R.layout.editor_func_popup_penmenu) {
				if (deviceType > 100)
					popupWindow.setBackgroundDrawable(getResources()
					        .getDrawable(R.drawable.asus_supernote_dropdown_m));
				else
					popupWindow.setBackgroundDrawable(getResources()
					        .getDrawable(R.drawable.asus_brush_popup));
			} else
				popupWindow.setBackgroundDrawable(getResources().getDrawable(
				        bgResId));
			// end smilefish
			popupWindow.setTouchInterceptor(mOnPupupWindowOnTouch);
			popupWindow.update();
			popupWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					mIsColorBoldPopupShown = false; //smilefish
					if (mCurrentPopupParentView != null) {
						// begin smilefish fix bug 301398
						if (mCurrentPopupParentView.getId() == R.id.note_kb_d_brush) {
							mBrushCollection.setBrushsToXMl();
						}
						// end smilefish

						if (mCurrentPopupParentView.getId() == R.id.note_kb_d_eraser
						        || ((mCurrentPopupParentView.getId() == R.id.note_kb_d_brush)
						                && mCurrentDoodleEraserButton != null && !mCurrentDoodleEraserButton
						                    .isSelected())) {
							return;
						}
						if (mCurrentPopupParentView.getId() != R.id.note_kb_d_brush)
							mCurrentPopupParentView.setSelected(false);
					}
				}

			});
		} else {
			if (popupWindowId == R.layout.editor_func_popup_color_bold) {
				popupWindow.setBackgroundDrawable(getResources().getDrawable(
				        R.drawable.asus_supernote_dropdown_r));
			}
		}
		
		// Begin: show_wang@asus.com
		// Modified reason: location of popupwindow
		if (popupWindowId == R.layout.editor_func_popup_penmenu) {
			x = getResources().getInteger(R.integer.penmenu_popup_position_x);
			y = getResources().getInteger(R.integer.penmenu_popup_position_y);
		}

		if (popupWindowId == R.layout.editor_func_popup_color_bold) {
			if (mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_SELECTION_TEXT) {
				x = getResources().getInteger(
				        R.integer.sel_color_bold_popup_menu_position_x);
				y = getResources().getInteger(
				        R.integer.sel_color_bold_popup_menu_position_y);
			} else {
				x = getResources().getInteger(
				        R.integer.color_bold_popup_menu_position_x);
				y = getResources().getInteger(
				        R.integer.color_bold_popup_menu_position_y);
			}
		}

		if (popupWindowId == R.layout.editor_func_popup_mode) {
			x = getResources().getInteger(R.integer.mode_popup_menu_position_x);
			y = getResources().getInteger(R.integer.mode_popup_menu_position_y);
		}

		if (popupWindowId == R.layout.editor_func_popup_d_eraser) {
			x = getResources().getInteger(
			        R.integer.eraser_popup_menu_position_x);
			y = getResources().getInteger(
			        R.integer.eraser_popup_menu_position_y);
		}
		// End: show_wang@asus.com

		popupWindow.showAsDropDown(view, (int) x, (int) y);
		mEditorUiUtility.hiddenSoftKeyboard();
		return popupWindow;
	}

	private void dismissAllPopupWindow() {
		if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
			currentPopupWindow.dismiss();
		}
		if (mDoodleEraserPopupWindow != null
		        && mDoodleEraserPopupWindow.isShowing()) {
			mDoodleEraserPopupWindow.dismiss();
		}
		if (mDoodleBrushPopupWindow != null
		        && mDoodleBrushPopupWindow.isShowing()) {
			mDoodleBrushPopupWindow.dismiss();
		}
		if (mInsertPopupWindow != null && mInsertPopupWindow.isShowing()) {
			mInsertPopupWindow.dismiss();
		}
		if (mModePopupWindow != null && mModePopupWindow.isShowing()) {
			mModePopupWindow.dismiss();
		}
		if (mColorBoldPopupWindow != null && mColorBoldPopupWindow.isShowing()) {
			mColorBoldPopupWindow.dismiss();
		}
		// begin smilefish
		if (mMenuPopupWindow != null && mMenuPopupWindow.isShowing()) {
			mMenuPopupWindow.dismiss();
		}
		// end smilefish
	}

	private void setEraserSelected(View view) {
		for (View v : mPopupEraser) {
			v.setSelected(false);
		}

		if (view.getId() != R.id.clear_all) {
			view.setSelected(true);
		}
	}

	private void setModeSelected(View view) {
		if (deviceType >= 3) {// smilefish
			return;
		}
		for (View v : mPopupMode) {
			v.setSelected(false);
		}

		view.setSelected(true);
		mEditorUiUtility.getPageEditor().HasPopUpWindows = false; // Dave. To
		                                                          // fix the
		                                                          // bug:
		                                                          // keyboard
		                                                          // will show
		                                                          // and
		                                                          // disappear
		                                                          // one time in
		                                                          // keyboard_input_mode.
	}

	// BEGIN: james5_chan@asus.com
	private void addPageToBookmark(MenuItem item) {
		int id = mNotePage.isBookmark() ? R.string.remove_from_list
		        : R.string.add_to_list;

		showToast(this, id);
		// modify by Wendy
		// mNotePage.setBookmark(!mNotePage.isBookmark());
		mNotePage.setBookmarkByUser(!mNotePage.isBookmark());
		// modify by wendy
		id = mNotePage.isBookmark() ? R.string.remove_from_list
		        : R.string.add_to_list;
		String title = getResources().getString(id);
		item.setTitle(title);
	}

	// begin smilefish
	private void addPageToBookmark(TextView item) {
		int id = mNotePage.isBookmark() ? R.string.remove_from_list
		        : R.string.add_to_list;

		showToast(this, id);
		// modify by Wendy
		mNotePage.setBookmarkByUser(!mNotePage.isBookmark());
		// modify by wendy
		id = mNotePage.isBookmark() ? R.string.remove_from_list
		        : R.string.add_to_list;
		String title = getResources().getString(id);
		item.setText(title);
	}

	// end smilefish

	// BEGIN Shane_Wang@asus.com 2012-11-23
	// BEGIN: james5_chan@asus.com
	private void isTelephoneMemo() {
		if (MetaData.PHONE_MEMO_ACTION.equals(getIntent().getAction())) {
			generateBookAndPage(getResources().getString(
			        MetaData.PHONE_BOOK_NAME));
		}
	}

	// END: james5_chan@asus.com
	// END Shane_Wang@asus.com 2012-11-23
	void generateBookAndPage(String bookName) {
		Intent intent = getIntent();
        //emmanual to fix bug 492244
		intent.putExtra(MetaData.IS_NEW_PAGE, true);
		Cursor cursor = getContentResolver().query(MetaData.BookTable.uri,
		        null, "title = ?", new String[] { bookName }, null);
		if (cursor != null && cursor.getCount() != 0) {
			// using the existing book and page for phone memo
			cursor.moveToFirst();
			long bookId = cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
			NoteBook book = mBookCase.getNoteBook(bookId);
			NotePage page = new NotePage(EditorActivity.this, bookId);
			cursor.close();// RICHARD FIX MEMORY LEAK
			if (book != null) {
				book.addPage(page);
				intent.putExtra(MetaData.BOOK_ID, bookId);
				intent.putExtra(MetaData.PAGE_ID, page.getCreatedTime());
				return;
			}
		}
		// BEGEN: RICHARD
		else if (cursor != null) {
			cursor.close();// RICHARD FIX MEMORY LEAK
		}
		// END: RICHARD

		// must add a new book and a new page for phone memo
		NoteBook book = new NoteBook(EditorActivity.this);
		book.setTitle(bookName);
		book.setBookColor(MetaData.BOOK_COLOR_WHITE);
		book.setPageSize(MetaData.PAGE_SIZE_PHONE);
		book.setGridType(MetaData.BOOK_GRID_LINE);
		book.setIsLocked(false);
		NotePage page = new NotePage(EditorActivity.this, book.getCreatedTime());
		book.addPage(page);
		mBookCase.addNewBook(book);
		intent.putExtra(MetaData.BOOK_ID, book.getCreatedTime());
		intent.putExtra(MetaData.PAGE_ID, page.getCreatedTime());
	}
	// END Shane_Wang@asus.com 2012-11-19

	// BEGIN: james5_chan@asus.com
	public String getFilePath() {
		return mNotePage.getFilePath();
	}

	// END: james5_chan@asus.com

	public void setPasteButtonsEnable(boolean enabled) {
		for (View v : mPasteButtons) {
			setViewEnable(v, enabled);
		}
	}

	// Begin Allen
	public void setInsertButtonsEnable(boolean enabled) {
		if (mInsertView != null) {
			setViewEnable(mInsertView, enabled);
		}
	}

	// End Allen

	// BEGIN: archie_huang@asus.com
	public void setSelectButtonsEnable(boolean enabled) {
		for (View v : mSelectButtons) {
			setViewEnable(v, enabled);
		}
	} // END: archie_huang@asus.com

	public void setDoodleUndoButtonsEnable(boolean enabled) {
		for (View v : mDoodleUndoButtons) {
			setViewEnable(v, enabled);
		}
	}

	public void setBoxUndoButtonsEnable(boolean enabled) {
		for (View v : mBoxUndoButtons) {
			setViewEnable(v, enabled);
		}
	}

	public void setNoteUndoButtonsEnable(boolean enabled) {
		for (View v : mNoteUndoButtons) {
			setViewEnable(v, enabled);
		}
	}

	public void setDoodleRedoButtonsEnable(boolean enabled) {
		for (View v : mDoodleRedoButtons) {
			setViewEnable(v, enabled);
		}
	}

	public void setBoxRedoButtonsEnable(boolean enabled) {
		for (View v : mBoxRedoButtons) {
			setViewEnable(v, enabled);
		}
	}

	public void setNoteRedoButtonsEnable(boolean enabled) {
		for (View v : mNoteRedoButtons) {
			setViewEnable(v, enabled);
		}
	}

	public void setDoodleUnGroupButtonsEnable(boolean enabled) {
		for (View v : mUnGroupButtons) {
			setViewEnable(v, enabled);
		}
	}

	// BEGIN: archie_huang@asus.com
	public void setDoodleGroupButtonsEnable(boolean enabled) {
		for (View v : mGroupButtons) {
			setViewEnable(v, enabled);
		}
	} // END: archie_huang@asus.com

	// BEGIN: archie_huang@asus.com
	public void setDoodlePastButtonsEnable(boolean enabled) {
		for (View v : mDoodleSelectPastButtons) {
			if (enabled) {
				v.setVisibility(View.VISIBLE);
				float alpha = enabled ? BUTTON_ALPHA_ENABLE
				        : BUTTON_ALPHA_DISABLE;
				v.setAlpha(alpha);
				v.setEnabled(enabled);
			} else
				v.setVisibility(View.GONE);
		}
	} // END: archie_huang@asus.com

	// BEGIN: Show
	public void setDoodleCropButtonsEnable(boolean enabled) {
		for (View v : mDoodleSelectCropButtons) {
			if (enabled) {
				v.setVisibility(View.VISIBLE);
				float alpha = enabled ? BUTTON_ALPHA_ENABLE
				        : BUTTON_ALPHA_DISABLE;
				v.setAlpha(alpha);
				v.setEnabled(enabled);
			} else
				v.setVisibility(View.GONE);
		}
	}

	public void setDoodleTextEditButtonsEnable(boolean enabled) {
		for (View v : mDoodleSelectTextEditButtons) {
			if (enabled) {
				v.setVisibility(View.VISIBLE);
				float alpha = enabled ? BUTTON_ALPHA_ENABLE
				        : BUTTON_ALPHA_DISABLE;
				v.setAlpha(alpha);
				v.setEnabled(enabled);
			} else
				v.setVisibility(View.GONE);
		}
	}

	public void setDoodleChangeImgButtonsEnable(boolean enabled) {
		for (View v : mDoodleSelectChangeImgButtons) {
			if (enabled) {
				v.setVisibility(View.VISIBLE);
				float alpha = enabled ? BUTTON_ALPHA_ENABLE
				        : BUTTON_ALPHA_DISABLE;
				v.setAlpha(alpha);
				v.setEnabled(enabled);
			} else
				v.setVisibility(View.GONE);

		}
	}

	public void setSeekBarAlphaEnable(boolean enabled) {
		float alpha = enabled ? BUTTON_ALPHA_ENABLE : BUTTON_ALPHA_DISABLE;
		SeekBar_Alpha.setAlpha(alpha);
		SeekBar_Alpha.setEnabled(enabled);
		//emmanual to fix bug 468618
		if (!enabled) {
			SeekBar_Alpha.setProgress(100);
		}
	}

	public void setAlphaTextEnable(boolean enabled) {
		float alpha = enabled ? BUTTON_ALPHA_ENABLE : BUTTON_ALPHA_DISABLE;
		Alpha_Text.setAlpha(alpha);
		Alpha_Text.setEnabled(enabled);

		TextView alphaPercentage = (TextView) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.alpha_percentage);
		alphaPercentage.setAlpha(alpha);
		alphaPercentage.setEnabled(enabled);
		//emmanual to fix bug 468618
		if (!enabled) {
			alphaPercentage.setText(100 + "%");
		}
	}

	// END: Show

	public void setSelectedModeHintTextWith(boolean selected) {
		if (mEditorUiUtility.getInputMode() != InputManager.INPUT_METHOD_SELECTION_DOODLE) {
			return;
		}
		if (selected) {
			showEditorHint(getResources().getString(
			        R.string.editor_bottom_hint_selected_mode));
		} else {
			showEditorHint(getResources().getString(
			        R.string.editor_bottom_hint_selected_mode_nothing_selected));
		}
	}

	public void setColorButton(int color) {

		int drawId = -1;
		int viewId = -1;
		mNowColor = color;
		switch (color) {
		case Color.RED:
			drawId = mIsNowBold ? R.drawable.asus_supernote_a_red
			        : R.drawable.asus_supernote_a_red_tin;
			viewId = R.id.editor_func_color_red;
			break;
		case Color.GREEN:
			drawId = mIsNowBold ? R.drawable.asus_supernote_a_green
			        : R.drawable.asus_supernote_a_green_tin;
			viewId = R.id.editor_func_color_green;
			break;
		case Color.BLUE:
			drawId = mIsNowBold ? R.drawable.asus_supernote_a_blue
			        : R.drawable.asus_supernote_a_blue_tin;
			viewId = R.id.editor_func_color_blue;
			break;
		case Color.BLACK:
			drawId = mIsNowBold ? R.drawable.asus_supernote_a_black
			        : R.drawable.asus_supernote_a_black_tin;
			viewId = R.id.editor_func_color_black;
			break;
		default:
			drawId = mIsNowBold ? R.drawable.asus_supernote_a_black
			        : R.drawable.asus_supernote_a_black_tin;
			viewId = R.id.editor_func_color_black;
			Log.e(TAG, "when setColorButton, the color was wrong! color = "
			        + mNowColor);
			break;
		}

		if (drawId != -1) {
			for (View v : mColorButtons) {
				if (!(v instanceof Button)) {
					Log.e(TAG, "The view " + v.getId()
					        + " is not instance of Button");
					continue;
				}

				Button colorButton = (Button) v;
				Drawable drawableTop = getResources().getDrawable(drawId);
				colorButton.setCompoundDrawablesWithIntrinsicBounds(null,
				        drawableTop, null, null);
			}
		}

		if (viewId != -1) {
			for (View v : mPopupColors) {
				if (v.getId() == viewId) {
					v.setSelected(true);
				} else {
					v.setSelected(false);
				}
			}
		}

		if (drawId == -1) {
			Exception e = new Exception();
			Log.e(TAG, "drawId == -1");
			e.printStackTrace();
		}
		if (viewId == -1) {
			Exception e = new Exception();
			Log.e(TAG, "viewId == -1");
			e.printStackTrace();
		}
	}

	public void setBoldButton(int style) {
		int drawId = -1;
		int viewId = -1;
		if (style == Typeface.NORMAL) {
			mIsNowBold = false;
			switch (mNowColor) {
			case Color.RED:
				drawId = R.drawable.asus_supernote_a_red_tin; // modified by
				                                              // Carol
				break;
			case Color.GREEN:
				drawId = R.drawable.asus_supernote_a_green_tin; // modified by
				                                                // Carol
				break;
			case Color.BLUE:
				drawId = R.drawable.asus_supernote_a_blue_tin; // modified by
				                                               // Carol
				break;
			case Color.BLACK:
				drawId = R.drawable.asus_supernote_a_black_tin; // modified by
				                                                // Carol
				break;
			default:
				Log.w(TAG, "when setBoldButton, the color was wrong! color = "
				        + mNowColor);
				drawId = R.drawable.asus_supernote_a_black_tin; // modified by
				                                                // Carol
				// }
			}
			viewId = R.id.editor_func_bold_false;
		} else {
			mIsNowBold = true;
			switch (mNowColor) {
			case Color.RED:
				drawId = R.drawable.asus_supernote_a_red; // modified by Carol
				break;
			case Color.GREEN:
				drawId = R.drawable.asus_supernote_a_green; // modified by Carol
				break;
			case Color.BLUE:
				drawId = R.drawable.asus_supernote_a_blue; // modified by Carol
				break;
			case Color.BLACK:
				drawId = R.drawable.asus_supernote_a_black; // modified by Carol
				break;
			default:
				Log.w(TAG, "when setBoldButton, the color was wrong! color = "
				        + mNowColor);
				drawId = R.drawable.asus_supernote_a_black; // modified by Carol
			}
			// }
			viewId = R.id.editor_func_bold_true;
		}

		for (View v : mBoldButtons) {
			if (!(v instanceof Button)) {
				Log.e(TAG, "The view " + v.getId()
				        + " is not instance of Button");
				continue;
			}
			Button boldButton = (Button) v;
			Drawable drawableTop = getResources().getDrawable(drawId);
			boldButton.setCompoundDrawablesWithIntrinsicBounds(null,
			        drawableTop, null, null);

		}

		for (View v : mPopupBolds) {
			if (v.getId() == viewId) {
				v.setSelected(true);
			} else {
				v.setSelected(false);
			}
		}
	}

	public void showSelectionTextHint(boolean show) {
		if (show) {
			if (deviceType > 100) {
				showEditorHint(getResources().getString(
				        R.string.editor_phone_toast_seletext));
			} else {
				showToastAtPhoneMode(R.string.editor_phone_toast_seletext);
			}
		} else {
			switch (mEditorUiUtility.getInputMode()) {
			case InputManager.INPUT_METHOD_KEYBOARD:
				mKeyboardButtons = prepareButtomButton(mKeyboardButtons,
				        R.id.keyboardMenuViewStub,
				        mEditorIdList.bottomButtonIdsKeyboard);
				break;
			case InputManager.INPUT_METHOD_SCRIBBLE:
				mScribbleButtons = prepareButtomButton(mScribbleButtons,
				        R.id.scribbleMenuViewStub,
				        mEditorIdList.bottomButtonIdsScrible);
				break;
			case InputManager.INPUT_METHOD_DOODLE:
				mDoodleButtons = prepareButtomButton(mDoodleButtons,
				        R.id.doodleMenuViewStub,
				        mEditorIdList.bottomButtonIdsOthers);
				break;
			}
		}
	}

	private void setViewEnable(View view, boolean enabled) {
		float alpha = enabled ? BUTTON_ALPHA_ENABLE : BUTTON_ALPHA_DISABLE;
		view.setAlpha(alpha);
		view.setEnabled(enabled);
	}

	// BEGIN: james5_chan@asus.com
	private void showAllFavoritePage() {
		try {
			Intent intent = new Intent(EditorActivity.this,
			        FavoriteActivity.class);
			intent.putExtra(MetaData.BOOK_ID, mBookId);
			startActivityForResult(intent, VIEW_BOOKMARK_REQUEST);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	// END: james5_chan@asus.com

	private void showToastAtPhoneMode(int resourceId) {
		if (deviceType <= 100) {
			showToast(this, resourceId);
		}
	}

	// begin noah;7.5
	private void showAddPageToast() {
		if (mAddPageToast == null) {
			mAddPageToast = Toast.makeText(getApplicationContext(),
			        R.string.pg_create_page, Toast.LENGTH_SHORT);
		}
		if (MetaData.AUTO_ADD_PAGE) {//emmanual
			mAddPageToast.setGravity(Gravity.AXIS_SPECIFIED | Gravity.BOTTOM, 0, 87);
		}else{
			mAddPageToast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
		}
		mAddPageToast.show();
	}

	// end noah

	public static void showToast(Context context, int resourceId) {
		if(context == null){
			return ;
		}
		if (mActivityToast == null) {
			// BEGIN: archie_huang@asus.com
			// To avoid memory leak
			mActivityToast = Toast.makeText(context.getApplicationContext(),
			        resourceId, Toast.LENGTH_LONG);
			// END: archie_huang@asus.com
		} else {
			mActivityToast.setText(resourceId);
		}
		mActivityToast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
		mActivityToast.show();
	}

	public static void showToast(Context context, String str) {
		if (mActivityToast == null) {
			// BEGIN: archie_huang@asus.com
			// To avoid memory leak
			mActivityToast = Toast.makeText(context.getApplicationContext(),
			        str, Toast.LENGTH_LONG);
			// END: archie_huang@asus.com
		} else {
			mActivityToast.setText(str);
		}
		mActivityToast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
		mActivityToast.show();
	}

	public int getDeviceType() {
		return deviceType;
	}

	private Bitmap mDefaultPageThumb = null;
	private List<ImageView> mImageViews = new ArrayList<ImageView>();
	private List<ImageView> mRightImageViews = new ArrayList<ImageView>();
	private List<Bitmap> mPageThumbBitmpaList = new ArrayList<Bitmap>();

	private void clearPageThumbBitmapList() {
		if (mPageThumbBitmpaList != null && mPageThumbBitmpaList.size() != 0) {
			for (Bitmap bitmap : mPageThumbBitmpaList) {
				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
				}
			}
		}
	}

	private void acquireWakeLock() {
		if (mWakeLock == null) {
			Log.d(TAG, "[acquireWakeLock]");
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
			        EditorActivity.class.getSimpleName());
			mWakeLock.acquire(WAKELOCK_ACQUIRED_TIME);
			mWakeLockCounter = 0L;
		}
		if (mCountdownCounter == null) {
			mCountdownCounter = new WakeLockCountdownCounter(
			        WAKELOCK_COUNTDOWN_TIME);
			mCountdownCounter.start();
		}

	}

	private void releaseWakeLock() {
		if (mWakeLock != null && mWakeLock.isHeld()) {
			Log.d(TAG, "[releaseWakeLock]");
			mWakeLock.release();
			mWakeLock = null;
		}
		if (mCountdownCounter != null) {
			mCountdownCounter.cancel();
			mCountdownCounter = null;
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		//Emmanual
		if (ev.getAction() == MotionEvent.ACTION_DOWN 
				&& mEditorUiUtility.getInputMode() != InputManager.INPUT_METHOD_KEYBOARD  //smilefish fix bug 643128
				&& ev.getY() > getResources().getDimensionPixelSize(R.dimen.actionbar_and_statusbar_height)) {
			if (MethodUtils.isPenDevice(getApplicationContext())) {
				boolean isPenOnly = mSharedPreference.getBoolean(
				        MetaData.STYLUS_ONLY_STATUS, false);
				changeSystemUIState(isPenOnly);
			}
		}
		
		// BEGIN, james5
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			mWakeLockCounter++;
			if(mLongPressEraser && ev.getToolType(ev.getActionIndex())== MotionEvent.TOOL_TYPE_STYLUS){
				mEditorUiUtility.changeScribleStroke(mStrokeWidth);
				mEditorUiUtility.setDoodleTool(mDoodleToolCode);
				selectThisDoodleTool(mCurrentDoodleBrushButton);
				CursorIconLibrary.setStylusIcon(templateLinearLayout, getDoodleDrawable());
				mLongPressEraser = false;
			}
		}
		// END, james5
		
		if (templateLinearLayout != null
		        && mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_DOODLE
		        && mEditorUiUtility.getDoodleTool() == DrawTool.ERASE_TOOL) {
			switch (ev.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				templateLinearLayout.addEraserView(ev.getX(), ev.getY());
				break;
			case MotionEvent.ACTION_MOVE:
				templateLinearLayout.updateEraserView(ev.getX(), ev.getY());
				break;
			case MotionEvent.ACTION_UP:
				templateLinearLayout.removeEraserView();
				break;
			default:
				break;
			}
		} else if (templateLinearLayout != null) {
			templateLinearLayout.removeEraserView();
		}

		return super.dispatchTouchEvent(ev);
	}

    private Drawable getDoodleDrawable() {
    	switch (mEditorUiUtility.getDoodleTool()) {
    		case DrawTool.NORMAL_TOOL :
    			return drawCurrentDoodleBrush(R.drawable.asus_indicator_brushes_rollerpen);
    		case DrawTool.NEON_TOOL :
				return drawCurrentDoodleBrush(R.drawable.asus_indicator_brushes_airbrush);
    		case DrawTool.SCRIBBLE_TOOL :
				return drawCurrentDoodleBrush(R.drawable.asus_indicator_brushes_pen);
    		case DrawTool.MARKPEN_TOOL :
				return drawCurrentDoodleBrush(R.drawable.asus_indicator_brushes_marker);
    		case DrawTool.WRITINGBRUSH_TOOL :
				return drawCurrentDoodleBrush(R.drawable.asus_indicator_brushes_brush);
    		case DrawTool.SKETCH_TOOL :
				return drawCurrentDoodleBrush(R.drawable.asus_indicator_brushes_pencil);
    		case DrawTool.ERASE_TOOL :
    			return getEraserDrawable();
    	}
    	return resIdToDrawable(R.drawable.asus_indicator_default);
    }
  	
  	private Drawable getEraserDrawable() {
  		float eraserWidth = mEditorUiUtility.getEraserWidth();
  		if(eraserWidth == MetaData.DOODLE_ERASER_WIDTHS[0]) {
  			return resIdToDrawable(R.drawable.asus_supernote_function_eraser3);
  		} else if(eraserWidth == MetaData.DOODLE_ERASER_WIDTHS[1]) {
  			return resIdToDrawable(R.drawable.asus_supernote_function_eraser2);
  		} else {
  			return resIdToDrawable(R.drawable.asus_supernote_function_eraser1);
  		}
  	}
  	
  	private Drawable resIdToDrawable(int resId) {
  		Bitmap bmpTmp = BitmapFactory.decodeResource(getResources(), resId);
  		Drawable drawable = new BitmapDrawable(bmpTmp);
  		return drawable;
  	}

	private class WakeLockCountdownCounter extends CountDownTimer {
		private Long currValue;

		public WakeLockCountdownCounter(Long time) {
			super(time, time);
			currValue = mWakeLockCounter;
		}

		@Override
		public void onTick(long millisUntilFinished) {

		}

		@Override
		public void onFinish() {
			if (mWakeLockCounter > currValue) {;
				currValue = mWakeLockCounter;
				mWakeLock.acquire(WAKELOCK_ACQUIRED_TIME);
			}
			mCountdownCounter = new WakeLockCountdownCounter(
			        WAKELOCK_COUNTDOWN_TIME);
			mCountdownCounter.start();

		}
	}

	// BEGIN: Better
	private class AutoSaveCountdownCounter extends CountDownTimer {

		public AutoSaveCountdownCounter(Long time) {
			super(time, time);
		}

		@Override
		public void onTick(long millisUntilFinished) {

		}

		@Override
		public void onFinish() {
			// BEGIN: Better
			// mIsPageSaving = true;
			PageEditor pe = mEditorUiUtility.getPageEditor();
			if (pe.isDoodleModified() || pe.isEditTextModified()) {
				saveThumbnail(pe);
				// Begin Allen
				ArrayList<NoteItemArray> noteItems = null;
				DoodleItem doodleItem = null;
				if (mNotePage.getVersion() == 1) {
					noteItems = pe.getNoteItem();
					doodleItem = pe.getDoodleItem(mNotePage);
				} else {
					if (pe.isEditTextModified()) {
						noteItems = pe.getNoteItem();
					}
					if (pe.isDoodleModified()) {
						doodleItem = pe.getDoodleItem(mNotePage);
					}
				}
				// End Allen
				mNotePage.save(noteItems, doodleItem);

				// Begin Allen for update widget
				if (!MetaData.SuperNoteUpdateInfoSet
				        .containsKey(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_MODIFY_PAGE)) {
					MetaData.SuperNoteUpdateInfoSet
					        .put(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_MODIFY_PAGE,
					                null);
				}
				// End Allen
			}
			// mIsPageSaving = false;
			if (mIsAutoSaveNeeded) {
				mAutoSaveTimer.start();
			}
			// END: Better
		}

	}

	// begin darwin
	private void SavePage(Context context, ArrayList<NoteItemArray> noteItems,
	        DoodleItem doodleItem, long pageId, PageEditor pe) {
		ArrayList<String> mFilesList = new ArrayList<String>();
		mFilesList.addAll(pe.getPageUsedFileList());

		Boolean flag = true;
		if (noteItems == null || noteItems.size() == 0)// Allen
		{
			flag = false;
		}
		// END: RICHARD
		mNotePage.save(noteItems, doodleItem);
		// Begin Darwin_Yu@asus.com
		mNotePage.modifyAttachmentDB(pe);
		// End Darwin_Yu@asus.com

		if (!mIsConfig) {
			mNotePage.deleteInvalidData(mFilesList);
		}

		MetaData.SavingPageIdList.remove(pageId);
		Intent intent = new Intent(MetaData.SYNC_UPDATE_UI);

		// BEGIN: RICHARD
		if (flag) {
			if (mIndexServiceClient != null)// darwin
			{
				mIndexServiceClient.sendPageIDToIndexService(pageId);
			}
		}
		// END: RICHARD
		context.sendBroadcast(intent);

		// Begin Allen for update widget
		if (!MetaData.SuperNoteUpdateInfoSet
		        .containsKey(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_MODIFY_PAGE)) {
			MetaData.SuperNoteUpdateInfoSet
			        .put(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_MODIFY_PAGE,
			                null);
		}
		// End Allen
	}
	// end darwin

	// BEGIN:Show
	public boolean IsTextImgEdit() {
		return mIsTextImgEdit;
	}
	// END:Show

	// BEGIN: RICHARD
	void traverseView(View view, int index) {
		if (view instanceof SearchView) {
			SearchView v = (SearchView) view;
			for (int i = 0; i < v.getChildCount(); i++) {
				traverseView(v.getChildAt(i), i);
			}
		} else if (view instanceof LinearLayout) {
			LinearLayout ll = (LinearLayout) view;
			for (int i = 0; i < ll.getChildCount(); i++) {
				traverseView(ll.getChildAt(i), i);
			}
		} else if (view instanceof EditText) {
			((EditText) view).setTextColor(Color.YELLOW);
			((EditText) view).setHintTextColor(Color.BLUE);
			((EditText) view).setBackgroundColor(Color.TRANSPARENT);
			((EditText) view).setTextSize(TypedValue.COMPLEX_UNIT_PX,
			        mEditorUiUtility.getPageEditor().getNoteEditText()
			                .getFontSize());
		} else if (view instanceof TextView) {
			((TextView) view).setTextColor(Color.YELLOW);
		} else {
			// Log.v("View Scout", "Undefined view type here...");
		}

	}

	// END: RICHARD
	// begin darwin
	final MenuItem.OnActionExpandListener viewClose = new MenuItem.OnActionExpandListener() {

		@Override
		public boolean onMenuItemActionExpand(MenuItem arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onMenuItemActionCollapse(MenuItem arg0) {
			// TODO Auto-generated method stub
			mEditorUiUtility.setInputMode(mEditorUiUtility
			        .getInputModeFromPreference());
			return false;
		}
	};

	// BEGIN: show_wang@asus.com
	// Modified reason: penmenu
	private Bitmap setDrawableColor(int id) {
		int color = mEditorUiUtility.getDoodlePaint().getColor();
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] colors = new int[width * height];
		bitmap.getPixels(colors, 0, width, 0, 0, width, height);
		for (int i = 0; i < width * height; i++) {
			colors[i] = (colors[i] & 0xFF000000) | (color & 0x00FFFFFF);
		}
		Bitmap newbitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		newbitmap.setPixels(colors, 0, width, 0, 0, width, height);
		return newbitmap;
	}

	// END: show_wang@asus.com

	private int drawCurrentBrushType() { // Carol
		int brushID = getIdFrom(mDoodleToolCode);
		int pen = -1;
		switch (brushID) {
		case R.id.editor_func_d_brush_normal:
			pen = R.drawable.asus_supernote_function_pen3;
			break;
		case R.id.editor_func_d_brush_mark:
			pen = R.drawable.asus_supernote_function_pen6;
			break;
		case R.id.editor_func_d_brush_markpen:
			pen = R.drawable.asus_supernote_function_pen4;
			break;
		case R.id.editor_func_d_brush_scribble:
			pen = R.drawable.asus_supernote_function_pen2;
			break;
		case R.id.editor_func_d_brush_writingbrush:
			pen = R.drawable.asus_supernote_function_pen5;
			break;
		case R.id.editor_func_d_brush_sketch:
			pen = R.drawable.asus_supernote_function_pen1;
			break;
		}
		return pen;
	}

	private Drawable drawCurrentBrushThickness(int brushType) { // Carol
		Bitmap bmpTmp1 = BitmapFactory.decodeResource(getResources(),
		        R.drawable.asus_supernote_function_pen3_line1);
		Bitmap bitmap = Bitmap.createBitmap(bmpTmp1.getWidth(),
		        bmpTmp1.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		// --- Carrot: temporary add for limitation of brush width ---
		if (attrs.size() > 0) {
			float min = attrs.get(mCurAttrIndex).MinWidth;
			float max = attrs.get(mCurAttrIndex).MaxWidth;
			float delta = (max - min) / 5.0f;
			if (mStrokeWidth >= min && mStrokeWidth <= min + delta) {
				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line1);
			} else if (mStrokeWidth > min + delta
			        && mStrokeWidth <= min + 2.0 * delta) {
				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line2);
			} else if (mStrokeWidth > min + 2.0 * delta
			        && mStrokeWidth <= min + 3.0 * delta) {
				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line3);
			} else if (mStrokeWidth > min + 3.0 * delta
			        && mStrokeWidth <= min + 4.0 * delta) {
				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line4);
			} else if (mStrokeWidth > min + 4.0 * delta && mStrokeWidth <= max) {
				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line5);
			}
		} else {// --- Carrot: temporary add for limitation of brush width ---
			if (mStrokeWidth > 0 && mStrokeWidth <= 6) {

				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line1);
			} else if (mStrokeWidth > 6 && mStrokeWidth <= 12) {

				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line2);
			} else if (mStrokeWidth > 12 && mStrokeWidth <= 18) {

				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line3);
			} else if (mStrokeWidth > 18 && mStrokeWidth <= 24) {

				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line4);
			} else if (mStrokeWidth > 24 && mStrokeWidth <= 30) {

				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line5);
			}
		}
		Matrix matrix = new Matrix();
		double scale = MetaData.DPI * 1.0 / 160;
		matrix.setScale((float) scale, (float) scale);
		canvas.drawBitmap(bmpTmp1, 0, 0, paint);
		// begin smilefish
		Bitmap bmpTmp2 = BitmapFactory
		        .decodeResource(getResources(), brushType);
		canvas.drawBitmap(bmpTmp2, 0, 0, paint);
		// end smilefish
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
		        bitmap.getHeight(), matrix, true);
		Drawable drawable = new BitmapDrawable(newbmp);
		return drawable;
	}

	// BEGIN: show_wang@asus.com
	// Modified reason: markpen alpha
	public int getDoodleToolAlpha() {
		return mDoodleToolAlpha;
	}

	// Modified reason: eraser init Icon
	private int drawCurrentEraserIcon() { // modified by carol-use selector for
		                                  // A86
		int drawableID;
		if (mEraserWidth == MetaData.DOODLE_ERASER_WIDTHS[0]) {
			drawableID = R.drawable.asus_supernote_function_eraser1;
		} else if (mEraserWidth == MetaData.DOODLE_ERASER_WIDTHS[1]) {
			drawableID = R.drawable.asus_supernote_function_eraser2;
		} else {
			drawableID = R.drawable.asus_supernote_function_eraser3;
		}
		return drawableID;
	}

	// Modified reason: for dds
	public Boolean getConfigStatus() {
		return mIsConfig;
	}

	public void setEraserStatus(boolean status) {
		mIsEraserOn = status;
	}

	// Modified reason: for airview
	public float getEraserWidth() {
		return mEraserWidth;
	}

	// modified reaseon:for A86 brush storage
	public void selectBrush(BrushInfo brush) {
		mDoodleToolCode = brush.getDoodleToolCode();
		mStrokeWidth = brush.getStrokeWidth();
		mSelectedColorIndex = brush.getSelectedColorIndex();
		mIsPalette = brush.getIsPalette();
		mIsColorMode = brush.getIsColorMode();
		mCustomColor = brush.getCustomColor();
		mDoodleToolAlpha = brush.getDoodleToolAlpha();
		int colorX = brush.getCurrentX();
		int colorY = brush.getCurrentY();

		if (mIsPalette && mIsCustomColorSet) // smilefish
		{
			View vv = mDoodleBrushPopupWindow.getContentView().findViewById(
			        R.id.editor_func_color_L);
			((ImageView) vv).setBackgroundColor(mCustomColor);

			mEditorUiUtility.changeColor(mCustomColor);
			ColorPickerViewCustom ColorPicker = (ColorPickerViewCustom) mDoodleBrushPopupWindow
			        .getContentView().findViewById(R.id.color_picker_view);
			ColorPicker.setColorXY(colorX, colorY);
			// Carrot: individually set color of every brush
			SetCurAttrIndex(mDoodleToolCode);
			attrs.get(mCurAttrIndex).ColorInfo.Index = COLOR_PALETTE_INDEX;
			attrs.get(mCurAttrIndex).ColorInfo.Color = mCustomColor;
			attrs.get(mCurAttrIndex).ColorInfo.ColorPalette_X = colorX;
			attrs.get(mCurAttrIndex).ColorInfo.ColorPalette_Y = colorY;
			// Carrot: individually set color of every brush
		} else {
			mEditorUiUtility
			        .changeColor(mDefaultColorCodes[mSelectedColorIndex]);
			// Carrot: individually set color of every brush
			SetCurAttrIndex(mDoodleToolCode);
			attrs.get(mCurAttrIndex).ColorInfo.Index = mSelectedColorIndex;
			attrs.get(mCurAttrIndex).ColorInfo.Color = mDefaultColorCodes[mSelectedColorIndex];
			// Carrot: individually set color of every brush
		}

		for (int id : mEditorIdList.editorDoodleUnityColorIds) {
			View vv = mDoodleBrushPopupWindow.getContentView().findViewById(id);
			((ImageView) vv).setImageResource(R.drawable.asus_color_frame_n);
		}

		mEditorUiUtility.setDoodleTool(mDoodleToolCode);
		mEditorUiUtility.changeScribleStroke(mStrokeWidth);
		mCurrentDoodleBrushButton.setCompoundDrawablesWithIntrinsicBounds(null,
		        drawCurrentBrushThickness(drawCurrentBrushType()), null, null);

		selectCurrentBrushOnPopup(); // set brush and color
		if (mDoodleToolCode != DrawTool.MARKPEN_TOOL) {
			if ((SeekBar_Alpha != null) && (SeekBar_Alpha.isEnabled())) {
				setSeekBarAlphaEnable(false);
			}
			if ((Alpha_Text != null) && (Alpha_Text.isEnabled())) {
				setAlphaTextEnable(false);
			}
		} else {
			if (SeekBar_Alpha != null) {
				float AlphaPrecent = mDoodleToolAlpha / 255.0f;
				int AlphaProgress = Math.round(AlphaPrecent * 100);
				SeekBar_Alpha.setProgress(AlphaProgress);
				setSeekBarAlphaEnable(true);
			}
			if ((Alpha_Text != null) && (!Alpha_Text.isEnabled())) {
				setAlphaTextEnable(true);
			}
		}

		SeekBar SeekBar_width = (SeekBar) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.seekbar_width);

		// --- Carrot: temporary add for limitation of brush width ---
		attrs.get(mCurAttrIndex).Width = mStrokeWidth;
		float WidthPrecent = attrs.get(mCurAttrIndex).Width
		        / attrs.get(mCurAttrIndex).MaxWidth;
		int WidthProgress = (int) (WidthPrecent * 100);
		SeekBar_width.setProgress(WidthProgress);
		// --- Carrot: temporary add for limitation of brush width ---

		if (mPenPreview != null)
			DrawPreview(mPenPreview);

		setAutoRecognizerShapeState(true);

		mDoodleBrushPopupWindow.dismiss(); // smilefish
	}

	public void showAddBrushButton() {
		ImageButton addBrushBtn = (ImageButton) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.add_brush);
		if (addBrushBtn.getVisibility() == View.INVISIBLE)
			addBrushBtn.setVisibility(View.VISIBLE);
	}

	// End: Smilefish

	// begin smilefish
	private final OnClickListener menuItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			boolean needToHideFlag = true;
			switch (v.getId()) {
			case R.id.editor_menu_search:
				savePage();
				startSearchActivity(); // smilefish
				break;
			case R.id.editor_menu_save:
				savePage();
				break;
			case R.id.editor_menu_delete:
				openDeletePageDialog();
				break;
			case R.id.editor_menu_bookmark:
				needToHideFlag = false;
				showBookmarkPopWindow();
				break;
			case R.id.editor_menu_favorite_toggle:
				addPageToBookmark((TextView) v);
				break;
			case R.id.editor_menu_favorite_showall:
				showAllFavoritePage();
				break;
			case R.id.editor_menu_read:
				savePage();
				mEditorUiUtility
				        .setInputMode(InputManager.INPUT_METHOD_READONLE);
				break;
			case R.id.editor_menu_pen:
				if (MethodUtils.isPenDevice(getApplicationContext())) { // smilefish
					CheckBox item = (CheckBox) v;
					setStylusInputOnly(item.isChecked());
					mSharedPreference
					        .edit()
					        .putBoolean(MetaData.STYLUS_ONLY_STATUS,
					                item.isChecked()).commit();
					if (item.isChecked()) {
						item.setChecked(true);
					} else {
						item.setChecked(false);
					}
				}
				break;
			case R.id.editor_menu_read_later:
				readItLater();
				break;
			case R.id.editor_menu_share:
				openShareToDialog();
				break;
			case R.id.editor_menu_setting:
				try {
					Intent intent = new Intent();
					intent.setClass(EditorActivity.this, SettingActivity.class);
					EditorActivity.this.startActivity(intent);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}

			if (needToHideFlag && mMenuPopupWindow != null
			        && mMenuPopupWindow.isShowing())
				mMenuPopupWindow.dismiss();
		}

	};

	private void creatMenuPopupWindow() {
		if (mMenuPopupWindow == null) {
			LayoutInflater inflater = (LayoutInflater) this
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View popupView = inflater.inflate(R.layout.editor_func_popup_menu,
			        null, false);

			mMenuPopupWindow = new PopupWindow(popupView,
			        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);

			mMenuPopupWindow.setFocusable(true);
			mMenuPopupWindow.setOutsideTouchable(true);
			mMenuPopupWindow.setTouchable(true);
			mMenuPopupWindow.setTouchInterceptor(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
						if (mMenuPopupWindow != null
						        && mMenuPopupWindow.isShowing()) {
							mMenuPopupWindow.dismiss();
						}
						return true;
					}

					return false;
				}
			});
			mMenuPopupWindow.setOnDismissListener(new OnDismissListener(){

				@Override
                public void onDismiss() {
					if(mIsKeyboardShown){
						mIsKeyboardShown = false;
						new Handler().postDelayed(new Runnable() {

							@Override
							public void run() {
								mEditorUiUtility.showSoftKeyboard(); //smilefish fix bug 704623
							}
						}, 100);
					}
                }
				
			});
		}
	}

	private void showMenuPopupWindow(View view) {
		creatMenuPopupWindow();

		if (mMenuEditorView == null) {
			View popupView = mMenuPopupWindow.getContentView();
			ViewStub viewstub = (ViewStub) popupView
			        .findViewById(R.id.editorMenuViewStub);
			mMenuEditorView = viewstub.inflate();
		}

		for (int buttonId : mEditorIdList.menuItemIds) {
			TextView textView = (TextView) mMenuEditorView
			        .findViewById(buttonId);
			textView.setOnClickListener(menuItemClickListener);
		}

		View searchGroup = mMenuEditorView
		        .findViewById(R.id.editor_menu_search_group);
		if (CFG.getCanDoVO()) {
			searchGroup.setVisibility(View.VISIBLE);
		} else {
			searchGroup.setVisibility(View.GONE);
		}

		View readLaterGroup = mMenuEditorView.findViewById(R.id.editor_menu_read_later_group);
		if(checkIfAppExist(MetaData.DO_IT_LATER_PACKAGENAME)){
			//show the item if 'Do it later' exists [Carol]
			readLaterGroup.setVisibility(View.VISIBLE);
		}else{
			readLaterGroup.setVisibility(View.GONE);
		}
		
		View penGroup = mMenuEditorView
		        .findViewById(R.id.editor_menu_pen_group);
		if (MethodUtils.isPenDevice(getApplicationContext())
		        && mEditorUiUtility.getInputMode() != InputManager.INPUT_METHOD_READONLE) {
			boolean isPenOnly = mSharedPreference.getBoolean(
			        MetaData.STYLUS_ONLY_STATUS, false);
			setStylusInputOnly(isPenOnly);
			CheckBox penView = (CheckBox) mMenuEditorView
			        .findViewById(R.id.editor_menu_pen);
			if (isPenOnly) {
				penView.setChecked(true);
			} else {
				penView.setChecked(false);
			}
			penGroup.setVisibility(View.VISIBLE);
		} else {
			penGroup.setVisibility(View.GONE);
		}

		mMenuEditorView.setVisibility(View.VISIBLE);
		if (mMenuBookmarkView != null)
			mMenuBookmarkView.setVisibility(View.GONE);
		if (mMenuReadonlyView != null)
			mMenuReadonlyView.setVisibility(View.GONE);

		mMenuPopupWindow.setBackgroundDrawable(getResources().getDrawable(
		        R.drawable.menu_popup_panel_holo_light));
		if(PickerUtility.isDeviceWithSoftKey()) //smilefish fix bug 385579
			mMenuPopupWindow.showAtLocation(view, Gravity.END | Gravity.TOP, 0, 240);
		else
			mMenuPopupWindow.showAsDropDown(view, 0, 0);

		mIsKeyboardShown = MetaData.IS_KEYBOARD_SHOW;
		if(mIsKeyboardShown)
			mEditorUiUtility.hiddenSoftKeyboard(); // smilefish
	}

	private void showMenuReadonlyPopupWindow(View view) {
		creatMenuPopupWindow();

		if (mMenuReadonlyView == null) {
			View popupView = mMenuPopupWindow.getContentView();
			ViewStub viewstub = (ViewStub) popupView
			        .findViewById(R.id.readonlyMenuViewStub);
			mMenuReadonlyView = viewstub.inflate();
		}

		for (int buttonId : mEditorIdList.menuItemReadonlyIds) {
			TextView textView = (TextView) mMenuReadonlyView
			        .findViewById(buttonId);
			textView.setOnClickListener(menuItemClickListener);
		}

		mMenuReadonlyView.setVisibility(View.VISIBLE);
		if (mMenuBookmarkView != null)
			mMenuBookmarkView.setVisibility(View.GONE);
		if (mMenuEditorView != null)
			mMenuEditorView.setVisibility(View.GONE);

		
		View readLaterReadOnly = mMenuReadonlyView.findViewById(R.id.editor_menu_read_later_readonly);
		if(checkIfAppExist(MetaData.DO_IT_LATER_PACKAGENAME)){
			//show the item if 'Do it later' exists [Carol]
			readLaterReadOnly.setVisibility(View.VISIBLE);
		}else{
			readLaterReadOnly.setVisibility(View.GONE);
		}

		mMenuPopupWindow.setBackgroundDrawable(getResources().getDrawable(
		        R.drawable.menu_popup_panel_holo_light));
		if(PickerUtility.isDeviceWithSoftKey())
			mMenuPopupWindow.showAtLocation(view, Gravity.END | Gravity.TOP, 0, 240);
		else
			mMenuPopupWindow.showAsDropDown(view, 0, 0);
	}

	private void showBookmarkPopWindow() {
		View popupView = mMenuPopupWindow.getContentView();
		if (mMenuBookmarkView == null) {
			ViewStub viewstub = (ViewStub) popupView
			        .findViewById(R.id.bookmarkMenuViewStub);
			mMenuBookmarkView = viewstub.inflate();

			TextView textView1 = (TextView) mMenuBookmarkView
			        .findViewById(R.id.editor_menu_favorite_toggle);
			textView1.setOnClickListener(menuItemClickListener);
			TextView textView2 = (TextView) mMenuBookmarkView
			        .findViewById(R.id.editor_menu_favorite_showall);
			textView2.setOnClickListener(menuItemClickListener);

		}

		TextView textView = (TextView) mMenuBookmarkView
		        .findViewById(R.id.editor_menu_favorite_toggle);
		int id = mNotePage.isBookmark() ? R.string.remove_from_list
		        : R.string.add_to_list;
		textView.setText(id);

		mMenuBookmarkView.setVisibility(View.VISIBLE);
		if (mMenuEditorView != null)
			mMenuEditorView.setVisibility(View.GONE);
		if (mMenuReadonlyView != null)
			mMenuReadonlyView.setVisibility(View.GONE);
	}

	// end smilefish

	// Begin jason
	private static final int STAMP_DLG = 0;
	private static final int SHAPE_DLG = 1;

	void showGridSelectDialog(int type) {
		dlg = new Dialog(this);
		View v = ((LayoutInflater) this
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
		        R.layout.insert_select, null);
		dlg.setContentView(v);
		dlg.setCancelable(true);

		Button button = (Button) v.findViewById(R.id.insert_select_cancel);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dlg.dismiss();
			}
		});
		GridView gridView = (GridView) v
		        .findViewById(R.id.insert_select_content);
		int count = getResources().getInteger(R.integer.select_dlg_colums_num);
		gridView.setNumColumns(count);
		if (type == STAMP_DLG) {
			dlg.setTitle(getResources().getString(R.string.insert_stamp_dlg));// translate
			gridView.setAdapter(new SelectAdapter(true));
			gridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
				        int arg2, long arg3) {
					// TODO Auto-generated method stub
					int stamp_id = (Integer) arg1.getTag();
					fooHandler.sendMessage(fooHandler.obtainMessage(
					        INSERT_STAMP_MSG, stamp_id, -1));
					dlg.dismiss();
				}
			});
		} else if (type == SHAPE_DLG) {
			dlg.setTitle(getResources().getString(R.string.insert_shape_dlg));// translate
			gridView.setAdapter(new SelectAdapter(false));
			gridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
				        int arg2, long arg3) {
					// TODO Auto-generated method stub
					int shape_id = (Integer) arg1.getTag() + 1;
					if (shape_id > SupernoteIconList.CUT_NUM_ONE
					        && shape_id <= SupernoteIconList.CUT_NUM_TWO) {
						shape_id += SupernoteIconList.LEVEL_ONE;
					} else if (shape_id > SupernoteIconList.CUT_NUM_TWO) {
						shape_id += SupernoteIconList.LEVEL_TWO;
					}
					fooHandler.sendMessage(fooHandler.obtainMessage(
					        INSERT_SHAPE_MSG, shape_id, -1));
					dlg.dismiss();
				}
			});
		}
		final Resources resources = getResources();
		dlg.getWindow()
		        .setLayout(
		                (int) (resources
		                        .getDimension(R.dimen.select_dlg_shadow) + (int) ((count - 1)
		                        * resources
		                                .getDimension(R.dimen.select_dlg_grid_space) + count
		                        * (resources
		                                .getDimension(R.dimen.select_dlg_grid_widthheight) + 0.5))),
		                android.widget.AbsListView.LayoutParams.WRAP_CONTENT);
		dlg.show();
	}

	private class SelectAdapter extends BaseAdapter {
		final int[] content;
		final int itemnHeight;

		public SelectAdapter(boolean stamp) {
			if (stamp) {
				content = SupernoteIconList.STAMP_ICON_POP;
			} else {
				content = SupernoteIconList.SHAPE_ICON_POP;
			}
			itemnHeight = getMicroPdfHeight();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return content.length;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return content[arg0];
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ImageView imageView = null;
			if (arg1 == null) {
				imageView = new ImageView(EditorActivity.this);
				imageView.setLayoutParams(new AbsListView.LayoutParams(
				        itemnHeight, itemnHeight));
			} else {
				imageView = (ImageView) arg1;
			}
			imageView.setTag(arg0);
			imageView.setImageResource(content[arg0]);
			imageView.setScaleType(ScaleType.CENTER);
			imageView.setBackgroundResource(R.drawable.insert_selector);
			return imageView;
		}

	}

	// =========================================
	private static final int INSERT_STAMP_MSG = 1;
	private static final int INSERT_SHAPE_MSG = 2;
	// ========================================
	private Handler fooHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INSERT_STAMP_MSG:
				insert_stamp(msg);
				break;
			case INSERT_SHAPE_MSG:
				insert_ShapeGraphic(msg);
				break;
			default:
				break;
			}
		};

		private void insert_stamp(Message msg) {
			final int res = msg.arg1;
			if (res < 0 || res >= SupernoteIconList.STAMP_ICON_RES.length) {
				return;
			}
			Bitmap bitmap = BitmapFactory.decodeStream(getResources()
			        .openRawResource(SupernoteIconList.STAMP_ICON_RES[res]));
			File file = new File(getInsertStampTempDir());
			if (!file.exists()) {
				file.mkdirs();
			}
			String path = getInsertStampTempDir() + "insert_stamp"
			        + System.currentTimeMillis() + ".png";
			try {
				file = new File(path);
				FileOutputStream fos = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				bitmap.recycle();
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				bitmap = null;
				return;
			} catch (IOException e) {
				e.printStackTrace();
				bitmap = null;
				return;
			}
			PageEditorManager pageEditorManager = mEditorUiUtility
			        .getPageEditorManager();
			pageEditorManager.setInsertState(1);//emmanual to fix bug 415028
			GalleryAttacher.attachItem(path, pageEditorManager);
		}
	};

	private String getInsertStampTempDir() {
		return MetaData.SYSTEM_DIR + "/temp/insert_stamp/";
	}

	private void insert_ShapeGraphic(Message msg) {
		PageEditorManager pageEditorManager = mEditorUiUtility
		        .getPageEditorManager();
		pageEditorManager.setInsertState(1);//emmanual to fix bug 415028
		GalleryAttacher.attachItem(msg.arg1, pageEditorManager);
	}

	private void deleteStampTempDir() {
		File file = new File(getInsertStampTempDir());
		if (file.exists() && file.isDirectory()) {
			for (File f : file.listFiles()) {
				f.delete();
			}
		}
	}

	// end

	// --- Carrot: temporary add for limitation of brush width ---
	class BrushAttributes {
		public int Index;
		public int Type;
		public float Width = 2;
		public float MaxWidth = 30;
		public float MinWidth = 2;
		public ColorInfo ColorInfo = new ColorInfo();// Carrot: individually set
		                                             // color of every brush

		public BrushAttributes(int id, int type, float width, float min,
		        float max) {
			Index = id;
			Type = type;
			Width = width;
			MaxWidth = max;
			MinWidth = min;
		}

		// Carrot: individually set color of every brush
		public void SetColorInfo(int color, int id, int x, int y) {
			ColorInfo.Color = color;
			ColorInfo.Index = id;
			ColorInfo.ColorPalette_X = x;
			ColorInfo.ColorPalette_Y = y;
		}

		// Carrot: individually set color of every brush

		public BrushAttributes(int id, int type, float width, float min,
		        float max, ColorInfo cinfo) {
			Index = id;
			Type = type;
			Width = width;
			MaxWidth = max;
			MinWidth = min;
			ColorInfo = cinfo;
		}
	}

	// Carrot: individually set color of every brush
	class ColorInfo {
		public int Color = 0xffe70012;
		public int Index = 4;
		public int ColorPalette_X = 0;
		public int ColorPalette_Y = 0;

		public ColorInfo() {

		}

		public ColorInfo(int color, int id, int x, int y) {
			Color = color;
			Index = id;
			ColorPalette_X = x;
			ColorPalette_Y = y;
		}
	}

	// Carrot: individually set color of every brush

	private ArrayList<BrushAttributes> attrs = new ArrayList<BrushAttributes>();
	private int mCurAttrIndex = 0;
	// Carrot: individually set color of every brush
	private int mColorPalette_X = 0;
	private int mColorPalette_Y = 0;
	private static final int COLOR_PALETTE_INDEX = 100;

	// Carrot: individually set color of every brush

	private void InitBrushAttributs() {
		attrs.add(0, new BrushAttributes(0, DrawTool.NORMAL_TOOL, 6, 2, 80,
		        new ColorInfo(0xffe70012, 4, 0, 0)));
		attrs.add(1, new BrushAttributes(1, DrawTool.SCRIBBLE_TOOL, 6, 2, 80,
		        new ColorInfo(0xff8fc31f, 7, 0, 0)));
		attrs.add(2, new BrushAttributes(2, DrawTool.WRITINGBRUSH_TOOL, 25.5f,
		        2, 80, new ColorInfo(0xff000000, 3, 0, 0)));
		attrs.add(3, new BrushAttributes(3, DrawTool.NEON_TOOL, 30.5f, 2, 80,
		        new ColorInfo(0xff00a0e9, 9, 0, 0)));
		attrs.add(4, new BrushAttributes(4, DrawTool.SKETCH_TOOL, 3, 2, 80,
		        new ColorInfo(0xff000000, 3, 0, 0)));
		attrs.add(5, new BrushAttributes(5, DrawTool.MARKPEN_TOOL, 64, 2, 80,
		        new ColorInfo(0xff8fc31f, 7, 0, 0)));
	}

	private void SetCurAttr(int mToolCode) {
		SetCurAttrIndex(mToolCode);
		SetValueByCurAttr();
		mEditorUiUtility.setDoodleTool(mDoodleToolCode);
		mEditorUiUtility.changeScribleStroke(mStrokeWidth);
		// Carrot: individually set color of every brush
		mEditorUiUtility.changeColor(attrs.get(mCurAttrIndex).ColorInfo.Color);
		// Carrot: individually set color of every brush
	}

	// DONOT set to EditorUiUtility in initial, which will cause initial paint
	// error of the writing layer
	private void SetCurAttrInit(int mToolCode) {
		SetCurAttrIndex(mToolCode);
		SetValueByCurAttr();
	}

	private void SetValueByCurAttr() {
		mStrokeWidth = attrs.get(mCurAttrIndex).Width;
		// Carrot: individually set color of every brush
		mSelectedColorIndex = attrs.get(mCurAttrIndex).ColorInfo.Index;
		if (mSelectedColorIndex == COLOR_PALETTE_INDEX) {
			mCustomColor = attrs.get(mCurAttrIndex).ColorInfo.Color;
			mColorPalette_X = attrs.get(mCurAttrIndex).ColorInfo.ColorPalette_X;
			mColorPalette_Y = attrs.get(mCurAttrIndex).ColorInfo.ColorPalette_Y;
			mIsPalette = true;
		} else {
			mIsPalette = false;
		}
		// Carrot: individually set color of every brush
	}

	private void SetCurAttrIndex(int mToolCode) {
		switch (mToolCode) {
		case DrawTool.NORMAL_TOOL:
			mCurAttrIndex = 0;
			break;
		case DrawTool.NEON_TOOL:
			mCurAttrIndex = 3;
			break;
		case DrawTool.SCRIBBLE_TOOL:
			mCurAttrIndex = 1;
			break;
		case DrawTool.MARKPEN_TOOL:
			mCurAttrIndex = 5;
			break;
		case DrawTool.WRITINGBRUSH_TOOL:
			mCurAttrIndex = 2;
			break;
		case DrawTool.SKETCH_TOOL:
			mCurAttrIndex = 4;
			break;
		}
	}

	private void UpdatePopupByCurBrush() {
		float WidthPrecent = attrs.get(mCurAttrIndex).Width
		        / attrs.get(mCurAttrIndex).MaxWidth;
		int WidthProgress = (int) (WidthPrecent * 100);
		SeekBar SeekBar_width = (SeekBar) mDoodleBrushPopupWindow
		        .getContentView().findViewById(R.id.seekbar_width);
		SeekBar_width.setProgress(WidthProgress);

		// Carrot: individually set color of every brush
		if (mIsPalette) {
			for (int id : mEditorIdList.editorDoodleUnityColorIds) {
				View vv = mDoodleBrushPopupWindow.getContentView()
				        .findViewById(id);
				if (vv != null) {
					if (vv.getId() == R.id.editor_func_color_L) {
						vv.setSelected(true);
						((ImageView) vv)
						        .setImageResource(R.drawable.asus_color_frame_p);
						((ImageView) vv).setBackgroundColor(mCustomColor);
					} else {
						vv.setSelected(false);
						((ImageView) vv)
						        .setImageResource(R.drawable.asus_color_frame_n);
					}
				}
			}
			ColorPickerViewCustom ColorPicker = (ColorPickerViewCustom) mDoodleBrushPopupWindow
			        .getContentView().findViewById(R.id.color_picker_view);
			ColorPicker.setColorXY(mColorPalette_X, mColorPalette_Y);
		}
		// Carrot: individually set color of every brush
	}

	// --- Carrot: temporary add for limitation of brush width ---

	// Begin jason ==================================================
	private BroadcastReceiver mBroadCastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			if (arg1.getAction().equals(CLOSE_ACTIVITY)) {
				EditorActivity.this.finish();
			}
		}

	};
	/***
	 * press Stylus Button to change the width or erase
	 */
	private TemplateLinearLayout templateLinearLayout = null;
	private int indexInLib = 0;

	// only call when InputManager.INPUT_METHOD_DOODLE
	private void updateCurrDoodle() {
		final int dooleCode = mEditorUiUtility.getDoodleTool();
		if (dooleCode == DrawTool.MARKPEN_TOOL
		        || dooleCode == DrawTool.WRITINGBRUSH_TOOL
		        || dooleCode == DrawTool.SKETCH_TOOL
		        || dooleCode == DrawTool.NORMAL_TOOL
		        || dooleCode == DrawTool.NEON_TOOL
		        || dooleCode == DrawTool.SCRIBBLE_TOOL) {
			if (mBrushCollection.brushSize() > 0) {
				indexInLib = (indexInLib + 1) % mBrushCollection.brushSize();
				changeToBrush(mBrushCollection.getBrush(indexInLib));
			}
		} else if (dooleCode == DrawTool.ERASE_TOOL) {
			changeToBrushStateFromEraser();
		}

	}

	private void changeToEraserState() {
		if(mDoodleFuncs == null) return; //smilefish fix bug 593695
		mEditorUiUtility.getPageEditor().setDoodleModified();
		mEditorUiUtility.setDoodleTool(DrawTool.ERASE_TOOL);
		mEditorUiUtility.changeScribleStroke(mEraserWidth);
		mCurrentDoodleEraserButton = (Button) mDoodleFuncs
		        .findViewById(R.id.note_kb_d_eraser);
		selectThisDoodleTool(mCurrentDoodleEraserButton);
	}
	
	public boolean isLongPressEraserState(){
		return mLongPressEraser;
	}

	private void changeToBrushStateFromEraser() {
		if (mBrushCollection.brushSize() == 0) {
			mEditorUiUtility.setDoodleTool(mDoodleToolCode);
			mEditorUiUtility.changeScribleStroke(mStrokeWidth);
		} else {
			changeToBrush(mBrushCollection.getBrush(0));
		}
		selectThisDoodleTool(mCurrentDoodleBrushButton);
	}

	public void changeToBrush(BrushInfo brush) {
		mDoodleToolCode = brush.getDoodleToolCode();
		mStrokeWidth = brush.getStrokeWidth();
		mSelectedColorIndex = brush.getSelectedColorIndex();
		mIsPalette = brush.getIsPalette();
		mIsColorMode = brush.getIsColorMode();
		mCustomColor = brush.getCustomColor();
		mDoodleToolAlpha = brush.getDoodleToolAlpha();
		int colorX = brush.getCurrentX();
		int colorY = brush.getCurrentY();

		if (mIsPalette && mIsCustomColorSet) // smilefish
		{
			mEditorUiUtility.changeColor(mCustomColor); //smilefish fix bug 607259
			if (mDoodleBrushPopupWindow != null) {
				ColorPickerViewCustom ColorPicker = (ColorPickerViewCustom) mDoodleBrushPopupWindow
				        .getContentView().findViewById(R.id.color_picker_view);
				ColorPicker.setColorXY(colorX, colorY);
			}
			
			// Carrot: individually set color of every brush
			SetCurAttrIndex(mDoodleToolCode);
			attrs.get(mCurAttrIndex).ColorInfo.Index = COLOR_PALETTE_INDEX;
			attrs.get(mCurAttrIndex).ColorInfo.Color = mCustomColor;
			attrs.get(mCurAttrIndex).ColorInfo.ColorPalette_X = colorX;
			attrs.get(mCurAttrIndex).ColorInfo.ColorPalette_Y = colorY;
			// Carrot: individually set color of every brush
		} else {
			mEditorUiUtility
			        .changeColor(mDefaultColorCodes[mSelectedColorIndex]);
			// Carrot: individually set color of every brush
			SetCurAttrIndex(mDoodleToolCode);
			attrs.get(mCurAttrIndex).ColorInfo.Index = mSelectedColorIndex;
			attrs.get(mCurAttrIndex).ColorInfo.Color = mDefaultColorCodes[mSelectedColorIndex];
			// Carrot: individually set color of every brush
		}

		mEditorUiUtility.setDoodleTool(mDoodleToolCode);
		mEditorUiUtility.changeScribleStroke(mStrokeWidth);

		mCurrentDoodleBrushButton.setCompoundDrawablesWithIntrinsicBounds(null,
		        drawCurrentBrushThickness(drawCurrentBrushType()), null, null);

		setAutoRecognizerShapeState(true);
	}

	private Drawable drawCurrentDoodleBrush(int resId) {
		Bitmap bmpTmp = BitmapFactory.decodeResource(getResources(), resId);
		Bitmap bmpTmp1 = BitmapFactory.decodeResource(getResources(),
		        R.drawable.asus_supernote_function_pen3_line1);
		Bitmap bitmap = Bitmap.createBitmap(bmpTmp.getWidth(),
		        bmpTmp.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		float strokeWidth = mEditorUiUtility.getDoodlePaint().getStrokeWidth();
		// --- Carrot: temporary add for limitation of brush width ---
		if (attrs.size() > 0) {
			float min = attrs.get(mCurAttrIndex).MinWidth;
			float max = attrs.get(mCurAttrIndex).MaxWidth;
			float delta = (max - min) / 5.0f;
			if (mStrokeWidth >= min && mStrokeWidth <= min + delta) {
				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line1);
			} else if (mStrokeWidth > min + delta
			        && mStrokeWidth <= min + 2.0 * delta) {
				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line2);
			} else if (mStrokeWidth > min + 2.0 * delta
			        && mStrokeWidth <= min + 3.0 * delta) {
				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line3);
			} else if (mStrokeWidth > min + 3.0 * delta
			        && mStrokeWidth <= min + 4.0 * delta) {
				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line4);
			} else if (mStrokeWidth > min + 4.0 * delta && mStrokeWidth <= max) {
				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line5);
			}
		} else {// --- Carrot: temporary add for limitation of brush width ---
			if (strokeWidth > 0 && strokeWidth <= 6) {

				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line1);
			} else if (strokeWidth > 6 && strokeWidth <= 12) {

				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line2);
			} else if (strokeWidth > 12 && strokeWidth <= 18) {

				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line3);
			} else if (strokeWidth > 18 && strokeWidth <= 24) {

				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line4);
			} else if (strokeWidth > 24 && strokeWidth <= 30) {

				bmpTmp1 = setDrawableColor(R.drawable.asus_supernote_function_pen3_line5);
			}
		}

		canvas.drawBitmap(bmpTmp, 0, 0, paint);
		canvas.drawBitmap(bmpTmp1, 0, 0, paint);
		Drawable drawable = new BitmapDrawable(bitmap);
		return drawable;
	}

	private boolean mLongPressEraser = false;
	public class StylusButtonPressImpl implements IStylusButtonPress {

		@Override
		public void longPress(boolean isTouch) {
			// TODO Auto-generated method stub
			mLongPressEraser = true;
			changeToEraserState();//emmanual
		}

		@Override
		public void shortPress(boolean isTouch) {
			// TODO Auto-generated method stub
			final int state = mEditorUiUtility.getInputMode();
			if (!isTouch) {
				switch (state) {
				case InputManager.INPUT_METHOD_DOODLE:
					updateCurrDoodle();
					break;
				default:
					break;
				}
			}
		}

		//emmanual
		@Override
		public void resetToBrush() {
			mEditorUiUtility.setDoodleTool(mDoodleToolCode);
			mEditorUiUtility.changeScribleStroke(mStrokeWidth);
			selectThisDoodleTool(mCurrentDoodleBrushButton);
			setAutoRecognizerShapeState(true);
		}

		@Override
		public Drawable getDoodleDrawable(int brushRes) {
			// TODO Auto-generated method stub
			return drawCurrentDoodleBrush(brushRes);
		}

	}

	// end jason========================

	// begin smilefish for color picker
	private void colorPickerViewShow(boolean isFirstLoad) {
		View view = this.findViewById(R.id.first_item);
		if (view != null) {
			PageEditor pe = mEditorUiUtility.getPageEditor();
			mSnapbitmap = Bitmap.createBitmap(view.getWidth(),
			        view.getHeight(), Bitmap.Config.ARGB_8888);
			if (deviceType > 100 && isPhoneScreen())
				mSnapbitmap.eraseColor(getResources().getColor(
				        R.color.edit_page_bg_color));
			else
				mSnapbitmap.eraseColor(getBookColor());
			Canvas canvas = new Canvas(mSnapbitmap);
			pe.drawSnapshot(canvas, isFirstLoad);

			if (mColorPickerViewStub == null) {
				mColorPickerViewStub = (ViewStub) this
				        .findViewById(R.id.colorPickerViewStub);
				View cpView = mColorPickerViewStub.inflate();

				mColorPickerHint = (View) mColorPickerFuncs
				        .findViewById(R.id.color_picker_hint);
				mColorChosenShow = (View) mColorPickerFuncs
				        .findViewById(R.id.color_chosen_show);

				mColorPickerSnapView = (ColorPickerSnapView) cpView
				        .findViewById(R.id.color_picker_snapview);
				mColorPickerSnapView.setBitmap(mSnapbitmap);
				mColorPickerSnapView.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View view, MotionEvent event) {

						float cx = event.getX();
						float cy = event.getY();

						mColorPickerSnapView.setColorXY((int) cx, (int) cy);

						mColorPickerHint.setVisibility(View.GONE);
						mColorChosenShow.setVisibility(View.VISIBLE);
						mColorPickerDoneButton.setEnabled(true);

						// GET the color
						// fix bug: 301271,301267 by smilefish
						int x = (int) cx;
						int y = (int) cy;
						if (mSnapbitmap != null && x >= 0 && x < mSnapbitmap.getWidth() && y >= 0
						        && y < mSnapbitmap.getHeight()) {
							mColorChosen = mSnapbitmap.getPixel(x, y);
							ImageView colorSelection = (ImageView) mColorPickerFuncs
							        .findViewById(R.id.color_chosen);
							colorSelection.setBackgroundColor(mColorChosen);
							TextView colorChosenName = (TextView) mColorPickerFuncs
							        .findViewById(R.id.color_chosen_name);
							colorChosenName.setText(ColorHelper
							        .displayColorName(mColorChosen)
							        .replace('\n', ' '));//emmanual to fix bug 426200
						}
						Log.d("carrot",
						        cx + "; " + cy + "; "
						                + Integer.toHexString(mColorChosen));

						mIsColorChosen = true;

						return true;
					}
				});

			} else {
				mColorPickerViewStub.setVisibility(View.VISIBLE);
				mColorPickerHint.setVisibility(View.VISIBLE);
				mColorChosenShow.setVisibility(View.GONE);
				mColorPickerDoneButton.setEnabled(false);
				mColorPickerSnapView.setBitmap(mSnapbitmap);
			}
		} else {
			Log.d("carrot", "view==null");
		}
	}

	private void releaseSnapBitmapMemory() {
		if ((mSnapbitmap != null) && !mSnapbitmap.isRecycled()) {
			mSnapbitmap.recycle();
			mSnapbitmap = null;
		}
	}

	private void setColorFromSnapshot() {
		mEditorUiUtility.changeColor(mColorChosen);
		mCustomColor = mColorChosen;

		mCurrentDoodleBrushButton.setCompoundDrawablesWithIntrinsicBounds(null,
		        drawCurrentBrushThickness(drawCurrentBrushType()), null, null);

		mIsPalette = true;

		mSelectedColorIndex = COLOR_PALETTE_INDEX;
		mColorPalette_X = 0;
		mColorPalette_Y = 0;
		attrs.get(mCurAttrIndex).ColorInfo.Color = mColorChosen;
		attrs.get(mCurAttrIndex).ColorInfo.Index = mSelectedColorIndex;
		attrs.get(mCurAttrIndex).ColorInfo.ColorPalette_X = mColorPalette_X;
		attrs.get(mCurAttrIndex).ColorInfo.ColorPalette_Y = mColorPalette_Y;

		if (mDoodleBrushPopupWindow != null) {
			ColorPickerViewCustom ColorPicker = (ColorPickerViewCustom) mDoodleBrushPopupWindow
			        .getContentView().findViewById(R.id.color_picker_view);
			ColorPicker.setColorXY(mColorPalette_X, mColorPalette_Y);
		} else {
			mSharedPreference
			        .edit()
			        .putInt(MetaData.PREFERENCE_PALETTE_COLORX, mColorPalette_X)
			        .commit();
			mSharedPreference
			        .edit()
			        .putInt(MetaData.PREFERENCE_PALETTE_COLORY, mColorPalette_Y)
			        .commit();
		}

		mIsColorChosen = false;
		mIsCustomColorSet = true;
	}

	// end smilefish

	private void createEditTextPopMenu(boolean selected) {
		if (selected && selectionPopupWindow == null) {
			LayoutInflater inflater = (LayoutInflater) this
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			selectionPopView = inflater.inflate(
			        R.layout.editor_selection_popmenu, null, false);
			selectionArrow = (ImageView) selectionPopView
			        .findViewById(R.id.arrow);

			selectionPopupWindow = new PopupWindow(selectionPopView,
			        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);

			selectionPopupWindow.setTouchable(true);
			selectionPopupWindow.setOutsideTouchable(true);
			selectionPopupWindow.setTouchInterceptor(mOnPupupWindowOnTouch);
			selectionPopupWindow.update();
		} else if (!selected && pointPopupWindow == null) {
			LayoutInflater inflater = (LayoutInflater) this
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			pointPopView = inflater.inflate(R.layout.editor_point_popmenu,
			        null, false);
			pointArrow = (ImageView) pointPopView.findViewById(R.id.arrow);

			pointPopupWindow = new PopupWindow(pointPopView,
			        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);

			pointPopupWindow.setTouchable(true);
			pointPopupWindow.setOutsideTouchable(true);
			pointPopupWindow.setTouchInterceptor(mOnPupupWindowOnTouch);
			pointPopupWindow.update();
		}
	}

	ImageView selectionArrow, pointArrow, currentArrow;
	PopupWindow selectionPopupWindow, pointPopupWindow, currentPopupWindow;
	View selectionPopView, pointPopView, currentPopView;

	public void showEditTextPopMenu(NoteEditText view, int x, int y, boolean selected){
		if( mColorBoldPopupWindow != null && mColorBoldPopupWindow.isShowing()) {
			return ;
		}
		if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
			currentPopupWindow.dismiss();
		}
    	createEditTextPopMenu(selected);
		currentPopupWindow = selected ? selectionPopupWindow : pointPopupWindow;
		currentPopView = selected ? selectionPopView : pointPopView;
		currentArrow = selected ? selectionArrow : pointArrow;
		preparePopupMenuButton(view, selected);
		
		if(currentPopView.getVisibility() == View.GONE){
			return ;
		}

		FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) currentArrow
				.getLayoutParams();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
		int mScreenWidth = dm.widthPixels;
		if (x > mScreenWidth - currentPopupWindow.getContentView().getWidth()
				* mEditorUiUtility.getPageEditor().getScaleX()) {
			params.leftMargin = (int) (x  - mScreenWidth
			        + currentPopupWindow.getContentView().getWidth()
					* mEditorUiUtility.getPageEditor().getScaleX() + 10);
		} else {
			params.leftMargin = 10;
		}

		if(y < getResources().getDimension(R.dimen.edit_func_bar_height)){
			y = (int) getResources().getDimension(R.dimen.edit_func_bar_height);
		}

		//emmanual - activity will be null when screen rotates sometimes 
		try{
			currentPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, x - 10, y);
		}catch(Exception e){
			
		}

		currentPopupWindow.getContentView().setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				currentPopupWindow.setFocusable(false);
				currentPopupWindow.dismiss();
				if(mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_DOODLE){
					mEditPopupDismissFromDoodle = true;
				}
				return true;
			}
		});
    }

	private boolean mEditPopupDismissFromDoodle = false; 
	
	public boolean isEditPopupDismissFromDoodle() {
		return mEditPopupDismissFromDoodle;
	}

	public void setEditPopupDismissFromDoodle(boolean mEditPopupDismissFromDoodle) {
		this.mEditPopupDismissFromDoodle = mEditPopupDismissFromDoodle;
	}

	private void preparePopupMenuButton(NoteEditText text, boolean selected) {
		if (selected) {
			Button copyBtn = (Button) currentPopView
			        .findViewById(R.id.note_popup_copy);
			Button cutBtn = (Button) currentPopView
			        .findViewById(R.id.note_popup_cut);
			Button pasteBtn = (Button) currentPopView
			        .findViewById(R.id.note_popup_paste);
			Button deleteBtn = (Button) currentPopView
			        .findViewById(R.id.note_popup_delete);
			copyBtn.setOnClickListener(mEditorPopButtonListener);
			cutBtn.setOnClickListener(mEditorPopButtonListener);
			pasteBtn.setOnClickListener(mEditorPopButtonListener);
			deleteBtn.setOnClickListener(mEditorPopButtonListener);
			//emmanual to fix bug 503911, 489238, 499120, 498495
			TextView devision = (TextView) currentPopView
			        .findViewById(R.id.note_popup_division);
			if(((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).hasPrimaryClip()){
				devision.setVisibility(View.VISIBLE);
				pasteBtn.setVisibility(View.VISIBLE);
			}else{
				devision.setVisibility(View.GONE);
				pasteBtn.setVisibility(View.GONE);
			}
		} else {
			Button pasteBtn = (Button) currentPopView
			        .findViewById(R.id.note_popup_paste);
			pasteBtn.setOnClickListener(mEditorPopButtonListener);
			Button selectBtn = (Button) currentPopView
			        .findViewById(R.id.note_popup_select);
			TextView devision = (TextView) currentPopView
			        .findViewById(R.id.note_popup_division);
			if (text.getText().length() > 0) {
				selectBtn.setVisibility(View.VISIBLE);
				selectBtn.setOnClickListener(mEditorPopButtonListener);
			} else {
				selectBtn.setVisibility(View.GONE);
			}
			//emmanual to fix bug 503911, 489238, 499120, 498495
			if(((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).hasPrimaryClip()){
				pasteBtn.setVisibility(View.VISIBLE);
			}else{
				pasteBtn.setVisibility(View.GONE);
			}
			if(selectBtn.getVisibility() == View.GONE && pasteBtn.getVisibility() == View.GONE){
				currentPopView.setVisibility(View.GONE);
			}else{
				currentPopView.setVisibility(View.VISIBLE);
				if(selectBtn.getVisibility() == View.VISIBLE && pasteBtn.getVisibility() == View.VISIBLE){
					devision.setVisibility(View.VISIBLE);
				}else{
					devision.setVisibility(View.GONE);
				}
			}
		}
	}

	private OnClickListener mEditorPopButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			currentPopupWindow.dismiss();
			switch (v.getId()) {
			case R.id.note_popup_copy:
				mEditorUiUtility.copy(mNotePage);
				break;
			case R.id.note_popup_cut:
				mEditorUiUtility.cut(mNotePage);
				break;
			case R.id.note_popup_paste:
				MetaData.IS_PASTE_OR_SHARE = true;
				mEditorUiUtility.paste(mNotePage);
				break;
			case R.id.note_popup_delete:
				mEditorUiUtility.insertBackSpace();
				break;
			case R.id.note_popup_select:
				if(mEditorUiUtility.getInputMode() == InputManager.INPUT_METHOD_DOODLE){
					mEditorUiUtility.setInputMode(InputManager.INPUT_METHOD_SCRIBBLE);
					mEditorUiUtility.setHandWriteViewEnable(false);
					mEditorUiUtility.getContext().getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
					        .edit().putInt(MetaData.PREFERENCE_INPUT_MODE_KEY, InputManager.INPUT_METHOD_DOODLE).commit();
				}
				mEditorUiUtility.getPageEditor().switchToSelectionTextMode();
				setSelectAllText();
				break;
			}
		}
	};

	public void dismissEditPopupMenu() {
		if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
			currentPopupWindow.dismiss();
		}
	}

	public boolean isEditPopupMenuShowing() {
		if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
			return true;
		}
		return false;
	}
	//fix bug 382097
	protected void writeViewLastPage(){
	    if(mSharedPreference != null){
	       Editor editor = mSharedPreference.edit();
	       editor.putBoolean(MetaData.existLastPage, true);
	       editor.commit();
	    }
    }
	
	//begin smilefish
	public boolean isColorBoldPopupShown(){
		return mIsColorBoldPopupShown;
	}
	//end smilefish
	
	//emmanual
	public class IMMResult extends ResultReceiver{
		public IMMResult() {
	        super(null);
        }
		
        @Override 
        public void onReceiveResult(int r, Bundle data) {
			switch (r) {
			case InputMethodManager.RESULT_HIDDEN:
				try {
	                Thread.sleep(300);
                } catch (InterruptedException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                }
				mEditorUiUtility.getPageEditor().getDoodleView().redrawAll(true);
				break;
			default:
				break;
			}
        }

	}
	
	public boolean checkIfAppExist(String appName){
		//check if this app exists [Carol]
		if(appName == null||"".equals(appName))
			return false;
		try{
			ApplicationInfo info = getPackageManager().getApplicationInfo(appName,
			PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		}catch(NameNotFoundException e){
			return false;
		}
	}
	
	//begin smilefish: check widget one installed or not, if installed, need to save widget thumbnail
	private void checkWidgetOneInstalledOrNot(){
    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(EditorActivity.this);
        List<AppWidgetProviderInfo> widgetList = appWidgetManager.getInstalledProviders();
        for(AppWidgetProviderInfo info:widgetList){
            String name = info.provider.getClassName();
            if(name.equalsIgnoreCase("com.asus.supernote.widget.WidgetOneProvider")){
            	MetaData.IS_ENABLE_WIDGET_THUMBNAIL = true;
            }
        }
    }
	//end smilefish

}
