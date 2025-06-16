package com.asus.supernote.picker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfRenderer;
import android.graphics.pdf.PdfRenderer.Page;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asus.commonui.colorful.ColorfulLinearLayout;
import com.asus.commonui.drawerlayout.ActionBarDrawerToggle;
import com.asus.commonui.drawerlayout.DrawerLayout;
import com.asus.supernote.EditorActivity;
import com.asus.supernote.EncryptAES;
import com.asus.supernote.PageOrderList;
import com.asus.supernote.R;
import com.asus.supernote.SettingActivity;
import com.asus.supernote.ShareToMeActivity;
import com.asus.supernote.SuperNoteApplication;
import com.asus.supernote.WebStorageException;
import com.asus.supernote.classutils.ColorfulStatusActionBarHelper;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.AsusFormat;
import com.asus.supernote.data.AsusFormatReader;
import com.asus.supernote.data.AsusFormatWriter;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.datacopy.DataCopyActivity;
import com.asus.supernote.dialog.utils.DialogUtils;
import com.asus.supernote.dialog.utils.TextViewUtils;
import com.asus.supernote.doodle.drawtool.GraphicDrawTool;
import com.asus.supernote.editable.attacher.Attacher;
import com.asus.supernote.editable.attacher.CameraAttacher;
import com.asus.supernote.editable.attacher.GalleryAttacher;
import com.asus.supernote.fota.CompatibleDataConverter;
import com.asus.supernote.fota.DataFormat;
import com.asus.supernote.fota.V1DataConverter;
import com.asus.supernote.fota.V1DataFormat;
import com.asus.supernote.ga.GACollector;
import com.asus.supernote.indexservice.IndexService;
import com.asus.supernote.indexservice.IndexServiceClient;
import com.asus.supernote.inksearch.CFG;
import com.asus.supernote.pdfconverter.PdfConverter;
import com.asus.supernote.ratingus.SuperNoteDialogFragment;
import com.asus.supernote.sync.CountDownClass;
import com.asus.supernote.sync.SyncHelper;
import com.asus.supernote.sync.SyncSetprivatePassword;
import com.asus.supernote.sync.SyncSettingActivity;
import com.asus.supernote.textsearch.TextSearchActivity;
import com.asus.supernote.ui.CoverHelper;
import com.asus.supernote.uservoice.UserVoiceConfig;
import com.asus.supernote.widget.InitSyncFilesInfoHelper;
import com.asus.supernote.widget.WidgetProvider.WidgetInitTask;
import com.asus.updatesdk.ZenUiFamily;
import com.itextpdf.text.exceptions.BadPasswordException;
import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.PdfReader;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class NoteBookPickerActivity extends Activity{
	public static final String TAG = "NoteBookPickerActivity";
	public static final String DIR = Environment.getExternalStorageDirectory()
			+ "/AsusSuperNote/";
	public static final String[] IMPORT_FILE_FILTER = new String[] { "sne", "snem", "snb" };
	public static final String[] IMPORT_PDF_FILTER = new String[] { "pdf" };
	public static final int REQUEST_IMPORT = 1;
	public static final int REQUEST_EXPORT_DIR_CHANGE = 3;

	// BEGIN: Better
	public static final int REQUEST_EXPORT_PDF_DIR_CHANGE = 5;
	// END: Better

	// BEGIN:shaun_xu@asus.com
	public static final int REQUEST_LOGIN = 6;
	public static final int REQUEST_DATACOPY = 101;//noah

	//darwin
	public static final int RESULT_CAMERA = 1001;
	public static final int RESULT_GALLERY = 1002;
	public static final int REQUEST_CROP_ICON = 1003;
	//darwin
	// IntentFrom
	public static final int STATEBAR_BOOK_ENABLE = 7;
	public static final int PICK_OTHERS = 8;
	public static final int SYNC_SETTING = 11;

	public static final int LOGIN = 13;
	public static final int PICK_OR_BOOK_PICKER = 14;
	
	public static final int REQUEST_TUTORIAL_INTRO_PAGE = 15;

	public static final int REQUEST_IMPORT_PDF = 16;

	public static final String NO_LAST_SYNC_TIME = "";
	
	public static final int DISPLAY_NONE = -1;
	public static final int DISPLAY_ALL = 0;
	public static final int DISPLAY_ALL_PAGE = 3;//darwin
	public static final int DISPLAY_BOOKMARK = 1;//2;
	public static final int DISPLAY_PERSONAL = 5; // darwin
	public static final int DISPLAY_TIMESTAMP = 2;//3; // ADD BY WENDY
	public static final int DISPLAY_PAGE = 99;// MODIFY BY darwin

	public static final int DIALOG_TYPES = 20;
	public static final int NO_DIALOG = -1;
	public static final int IMPORT_CONFIRM_DIALOG = 6;
	public static final int IMPORT_PROGRESS_DIALOG = 7;
	public static final int IMPORT_SUCCESS_DIALOG = 8;
	public static final int DELETE_BOOK_CONFIRM_DIALOG = 9;
	public static final int DELETE_BOOK_PROGRESS_DIALOG = 10;
	public static final int DELETE_BOOK_SUCCESS_DIALOG = 11;
	public static final int EXPORT_CONFIRM_DIALOG = 12;
	public static final int EXPORT_PROGRESS_DIALOG = 13;
	public static final int EXPORT_SUCCESS_DIALOG = 14;

	public static final int IMPORT_FAIL_DIALOG = 17;
	public static final int EXPORT_FAIL_DIALOG = 18;
	public static final int ADD_BOOK_DIALOG = 19;
	// BEGIN: Better
	public static final int EXPORT_PDF_CONFIRM_DIALOG = 20;
	public static final int EXPORT_PDF_PROGRESS_DIALOG = 21;
	public static final int EXPORT_PDF_SUCCESS_DIALOG = 22;
	public static final int EXPORT_PDF_FAIL_DIALOG = 23;

	// END: Better
	
    //+++ Dave  for Drawer UI
    private DrawerLayout mDrawerLayout;
    private RelativeLayout mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ColorfulLinearLayout mColorfulLinearLayout;
    private int mPendingSelectedItem = -1;
    //---
	
	//BEGIN: RICHARD
	public static final int HWR_MSG_DIALOG = 24;
	public static final int HWR_PROGRESS_DIALOG = 25;
	private static BookHWRTask mBookHWRTask;
    private IndexServiceClient mIndexServiceClient = null;
	//END: RICHARD
    
    // BEGIN: Better
    public static final int IMPORT_OPTION_DIALOG = 26;
    
    public static final int CLOUD_SYNCING_PROGRESS_DIALOG = 27;
    // END: Better

    public static final int EDITCOVER_CHOOSE_DIALOG = 29;//darwin
    public static final int SORT_TYPE_DIALOG = 28;//Allen
	public static final int MASK_ACTIVITY_REQUEST_CODE = 0xff;

	public static final boolean EXPORT_NEW_FORMAT = false;
	private static int mDisplayType = DISPLAY_NONE;
	
	public static final int ADD_NOTEBOOK_CHOOSE_DIALOG = 30; //smilefish

	public static final int IMPORT_PDF_SUCCESS_DIALOG = 31;
	
	private ActionBar mActionBar;
	private LinearLayout mbook_actionbar;// wendy

	private FrameLayout mLeftSideLayout;
	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	private NoteBookPageGridFragment mPageGridFragment;

	private AllPageViewFragment mAllPageViewFragment;
	private AllPageViewFragment mFavoritesPageFragment;
	private TimeStampViewFragment mTimestampViewFragment;// wendy
	private NoteBookPageGridFragment mPersonalPageFragment;
	private int deviceType;
	private SharedPreferences mPreference;
	private SharedPreferences.Editor mPreferenceEditor;
	private Resources mResources;
	private Context mContext;
	private BookCase mBookcase;

	// BEGIN:shaun
	private TextView mStatesync;
	private ImageView mSyncImage;
	private TextView mNoSync;
	private Bitmap failedImage;
	private Bitmap successImage;
	private Bitmap progressImage;
	private Animation mSyncProgressAnimation = null;
	// END:shaun

	private static boolean mIsImportTasRunning = false;
	private static boolean mIsImportPdfTaskRunning = false;
	private static boolean mIsDeleteBookTaskRunning = false;
	private static boolean mIsExportTaskRunning = false;
	// BEGIN: Better
	private static boolean mIsExport2PdfTaskRunning = false;
	
	private static boolean mIsCloudSyncingTaskRunning = false;
	// END: Better

	private static ImportFileTask mImportTask = null;
	private static DeleteBookTask mDeleteBooksTask = null;
	private static ExportTask mExportTask = null;
	private static ImportPdfTask mImportPdfTask = null;
	// BEGIN: Better
	private static Export2PdfTask mExport2PdfTask = null;
	
	private static ChangeBookStatusTask mCloudSyncingTask = null;
	// END: Better

	private static DeletePagesTask mDeletePagesTask = null;
	private static CopyPageTask mCopyPageTask = null;
	private static MovePageTask mMovePageTask = null;

	private static ContentResolver mContentResolver;

	// Views
	private static EditText mExportFileNameEditText;
	private static TextView mExportDirTextView;
	// BEGIN: Better
	private static TextView mExport2PdfDirTextView;
	// END: Better

	// BEGIN: Better
	private static EditText mExportPDFFileNameEditText;
	// END: Better

	private CountDownClass mCountDown = null;// add by wendy 0410
	private static SharedPreferences sSharedPreferences;
	private static boolean mHasPersonalPassword = false;
	private static Resources sResources;
	private static boolean mislocked = true;// wendy
	private static Context scontext = null;// wendy
	
	//begin darwin
	public static LanguageHelper sLanguageHelper = new LanguageHelper();
	//end   darwin
	public static boolean s_isMainActivityExist = false;

    private String NoteBookViewString;
    private String BookMarkViewString;
    private String TimeStampViewString;
    private String AllPageViewString;
    public boolean mHasBookMark = false;
    public boolean mHasTimeStamp = false;
    // BEGIN: Better
    public boolean mHasMemo = false;
    // END: Better
    private Dialog mAddBookDialog = null; //by show
    private static boolean mIsAddBookDialogShow = false; //by show
    
    private PopupWindow mCloudPopupWindow = null; //smilefish
    private View mStateBar = null; //smilefish
    private AlertDialog mEditCoverChooseDialog = null; //smilefish
    private static boolean mIsExportPdfWhenDDS = false; //smilefish
    private static boolean mIsExportWhenDDS = false; //smilefish
    private static boolean mIsImportWhenDDS = false; //smilefish
    private boolean mIsSettingWifi = false; //smilefish
    private String mLastAccount; //smilefish
    
    private static boolean mBackFromEditor = false; //Dave
    
    //darwin
    private EditText mEditBookCoverEditText = null;//by show
    private AlertDialog mEditBookCoverDialog = null; //by darwin
    private NoteBook mEditBookCoverBook = null; //by smilefish
    private EditBookCoverAdapter mEditBookCoverAdapter = null;
    private static int sConfigurationChange = 0;
    public static final int CONFIGURATIONCHANGE_BY_NOTEBOOKPICKER = 1;
    public static final int CONFIGURATIONCHANGE_BY_PICKER = 2;
    public static final int CONFIGURATIONCHANGE_BY_EDTOR = 3;
    public static void setConfigurationChangeBy( int configurationChange)
    {
    	sConfigurationChange = configurationChange;
    }
    public static int getConfigurationChangeBy()
    {
    	return sConfigurationChange;
    }
    
    //begin smilefish
    public void showStateBar(Boolean visible) {
    	if(MetaData.isATT()) //Carol
    		return;
		if(visible)
			mStateBar.setVisibility(View.VISIBLE);
		else {
			mStateBar.setVisibility(View.INVISIBLE);
		}
	}
    //end smilefish
    
    public static boolean hasCreated()
    {
		if(mIsFirstLoad == false)
		{
			return true;
		}else if(mIsFirstLoad == false && mHasCreated == true)
		{
			return true;
		}else {
			return false;
		}
    }
    
	//Begin:Dave. Fix the bug: when launched from widget,the button won't return to NotebookPickerActivity.
    public static void notifyFromEditor()
    {
		mBackFromEditor = true;
    }
    //End Dave.
    
    //darwin
    private void copyZipFile(InputStream is, OutputStream os) {
		byte[] byteBuffer = new byte[2048];
		int byteIn = 0;
		try {
			while ((byteIn = is.read(byteBuffer)) >= 0) {
				os.write(byteBuffer, 0, byteIn);
			}
			is.close();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    HashMap<Long, String> mImportOptionsMap = new HashMap<Long, String>();
    class MenuAdapter extends ArrayAdapter<String> {
    	List<String> mStringList;
        public MenuAdapter(
                Context context, int textViewResId,  List<String> strings) {
            super(context, textViewResId, strings);
            mStringList = strings;
        }
        public boolean areAllItemsEnabled() {
            return false;
        }
        public boolean isEnabled(int position) {
        	if(position == 0 ||
    			(position == 1) ||
    			(position == 2 && mHasMemo)) //Dave
        	{
        		return true;
        	}
        	return false;
        }
		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			View v = convertView;
			v = super.getDropDownView(position, convertView, parent);
			v.setBackgroundColor(Color.WHITE);
			v.setEnabled(isEnabled(position));
			return v;
		}
    }
    
    private static boolean mHasCreated = false; //Dave. Fix the Bug: improve the launch speed at first time.
    private boolean mIsShowAddDialog = false; //create book from widget[Carol]
    
	// BEGIN: Better
	// @Override
	public void onResume() {
		super.onResume();
		NoteBookPickerActivity.setConfigurationChangeBy(NoteBookPickerActivity.CONFIGURATIONCHANGE_BY_NOTEBOOKPICKER); //Dave
		//Begin:Dave. Fix the Bug: improve the launch speed at first time.
		if (!mIsFirstLoad) {
			if(mHasCreated){
				updateSyncStatusBar();
			}
			else {
				createActivity();
				
				if(mDisplayType == DISPLAY_ALL){ //smilefish fix bug 556030
					//emmanual to fix bug 479051
					if(mLeftSideLayout != null){
						mLeftSideLayout = (FrameLayout) findViewById(R.id.left_side);
					}
					mLeftSideLayout.setVisibility(View.VISIBLE);
					mFragmentTransaction = mFragmentManager.beginTransaction();
					mPageGridFragment = (NoteBookPageGridFragment) mFragmentManager
							.findFragmentByTag(NoteBookPageGridFragment.TAG);
					if (mPageGridFragment != null) {
						mFragmentTransaction.detach(mPageGridFragment);
						mFragmentTransaction.attach(mPageGridFragment);
					}
					mFragmentTransaction.commit();
					selectItem(0);
				}
			}
		}else {
			mIsFirstLoad = false;
		}
		mPreferenceEditor.putBoolean("activity_created", 
				true).commit();
		//End:Dave.
		
		//begin smilefish
		if(mExport2PdfTask != null && mIsExport2PdfTaskRunning)
			NoteBookPickerActivity.this.showDialog(EXPORT_PDF_PROGRESS_DIALOG);
		if(mExportTask != null && mIsExportTaskRunning)
			NoteBookPickerActivity.this.showDialog(EXPORT_PROGRESS_DIALOG);
		if(mImportTask != null && mIsImportTasRunning)
			NoteBookPickerActivity.this.showDialog(IMPORT_PROGRESS_DIALOG);
		if(mDeleteBooksTask != null && mIsDeleteBookTaskRunning)
			NoteBookPickerActivity.this.showDialog(DELETE_BOOK_PROGRESS_DIALOG);
		
        if(mDrawerToggle != null){
        	invalidateOptionsMenu();//emmanual to fix bug 457680
        	mDrawerToggle.syncState();
        }
        
        if(!MetaData.isATT() && mIsSettingWifi){ //Carol
        	showLoginDialog(STATEBAR_BOOK_ENABLE,"NoteBookPickerActivity");
        }
        	
		//end smilefish
        
    	//emmanual to fix bug 392671
		if (!mPreference.getBoolean(
		        mResources.getString(R.string.pref_first_start), true)
		        && widgetInitTask != null) {
			//removed by emmanual to fix bug 464265,441600
//			widgetInitTask.dismissProgressDialog();
//			widgetInitTask = null;
			
			showTutorialPage();
			
			//createActivity(); //smilefish fix bug 656611
		}else{
			//smilefish add for runtime permission
			if(MethodUtils.needShowPermissionPage(mContext)){
				MethodUtils.showPermissionPage(this, false);
			}
		}
		
		//emmanual to fix bug 447763
		showEditCoverDialog();

		
		//emmanual to fix bug 465126
		if (mDrawerLayout != null) {
			if (isShareMode()) {
				if(mCurrentDrawerIndex != DISPLAY_ALL) //smilefish fix bug 556675
					selectItem(DISPLAY_ALL);
				setDrawerEnabled(false);
			} else {
				setDrawerEnabled(true);
			}
		}
	}
	
	//smilefish add tutorial page
	private void showTutorialPage(){
		try {
			Intent intent = new Intent();
			intent.setClass(this, TutorialIntroPageActivity.class);
			if(isShareMode()){
				intent.putExtra(Intent.EXTRA_INTENT, getShareIntent());
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //smilefish fix bug 643136/656611
			startActivityForResult(intent,
					REQUEST_TUTORIAL_INTRO_PAGE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//end smilefish

	static public boolean getIsLock() {
		return mislocked;
	}
	
	static public void updateLockedstate(boolean lokced)
	{
		mislocked = lokced;
	}

	private void updateSyncStatusBar() {
		// BEGIN:shaun better
		if(MetaData.isATT()) //Carol
			return;
		
		SharedPreferences msPreference = getSharedPreferences(
				MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		String sAsusAccount = msPreference.getString(this.getResources()
				.getString(R.string.pref_AsusAccount), null);
		String RAsusKey =  mContext.getResources().getString(
				R.string.pref_AsusKey);
		String RAsusIV =  mContext.getResources().getString(
				R.string.pref_AsusIV);
		String keyStr = mPreference.getString(RAsusKey, "");
		String ivStr = mPreference.getString(RAsusIV, "");

		// END:shaun better

		if ((MetaData.CurUserAccount > 0) && !sAsusAccount.equals("")
				&& sAsusAccount != null) {
			//begin smilefish
				String account = null;
                try {
                	account = EncryptAES.decrypt( keyStr, ivStr, sAsusAccount);
	              } catch (Exception e) {
	                    e.printStackTrace();
	              }

				mNoSync.setText(account);
			//end smilefish
			if (MetaData.Sync_Result == -1) {
				int progress = 0;
				if (MetaData.SyncTotalPage > 0) {
					progress = (int) (((double) (MetaData.SyncCurrentPage) / MetaData.SyncTotalPage) * 100);
				}
				if (progress > 98) {
					progress = 98;
				}
				mSyncImage.setImageBitmap(progressImage);
				mSyncImage.setAnimation(mSyncProgressAnimation);
				mStatesync.setText(getResources().getString(
						R.string.sync_status_syncing)
						+ progress + "%");
			} else if (MetaData.Sync_Result == -2) {
				mStatesync.setText(getResources().getString(
						R.string.sync_status_failed));
				mSyncImage.setImageBitmap(failedImage);
				mSyncImage.setAnimation(null);
				
				int size = MetaData.NotSupportedVersionList.size();
				if (size > 0) {
					String msg = "";
					if (!MetaData.IsFileVersionCompatible) {
						msg += "(";
						int iter = 0;
						for (String v : MetaData.NotSupportedVersionList) {
							msg += v;
							if (iter != size - 1) {
								msg += ", ";
							}
						}
						msg += ")";
						msg = String.format(mContext.getString(R.string.prompt_incompatible_syncing), msg);
						MetaData.IsFileVersionCompatible = true;
					} else {
						msg += "(";
						int iter = 0;
						for (String v : MetaData.NotSupportedVersionList) {
							msg += v;
							if (iter != size - 1) {
								msg += ", ";
							}
						}
						msg += ")";
						msg = String.format(mContext.getString(R.string.prompt_incompatible_file), msg);
					}
					Toast.makeText(NoteBookPickerActivity.this, msg, Toast.LENGTH_LONG).show();
					MetaData.NotSupportedVersionList.clear();					
				}
			} else if (MetaData.Sync_Result == -3) {
				mSyncImage.setImageBitmap(successImage);
				mSyncImage.setAnimation(null);
				mStatesync.setText(MetaData.SyncSucessTime);
				
				int size = MetaData.NotSupportedVersionList.size();
				if (MetaData.IsFileVersionCompatible && (size > 0)) {
					String msg = "";
					msg += "(";
					int iter = 0;
					for (String v : MetaData.NotSupportedVersionList) {
						msg += v;
						if (iter != size - 1) {
							msg += ", ";
						}
					}
					msg += ")";
					msg = String.format(mContext.getString(R.string.prompt_incompatible_file), msg);
					Toast.makeText(NoteBookPickerActivity.this, msg, Toast.LENGTH_LONG).show();
					MetaData.NotSupportedVersionList.clear();
				}
				updateDrawerNumText(); //Dave

			} else if (MetaData.Sync_Result == -4) {
				mStatesync.setText("");
				mSyncImage.setImageBitmap(null);
				mSyncImage.setAnimation(null);
			}
		} else {
			if(mSyncImage == null || mStatesync == null)
				return;
			
			mSyncImage.setImageBitmap(null);
			mSyncImage.setAnimation(null);
			mStatesync.setText("");
			//begin smilefish
				mNoSync.setText(this.getResources().getString(
						R.string.statusbar_login));
			//end smilefish
		}
		// END:shaun
	}

	private static boolean mIsFirstLoad = true;  
	private static boolean mIsRunFirstLoad = false; //add by mars for firstload
	private BroadcastReceiver updateUiReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(MetaData.SYNC_UPDATE_UI)) {
				updateUI(); // Better
				updateDrawerNumText(); //Dave
			} else if (intent.getAction().equals(MetaData.CHOOSE_BOOKS)) {
				Bundle bundle = intent.getExtras();

				long[] mRemoteArray = new long[bundle

				.getLongArray("remotearray").length];
				mRemoteArray = bundle.getLongArray("remotearray");
				ArrayList<Long> mRemoteList = new ArrayList<Long>();
				for (int i = 0; i < mRemoteArray.length; i++) {
					mRemoteList.add(i, mRemoteArray[i]);
				}

				long[] mLocalArray = new long[bundle.getLongArray("localarray").length];
				mLocalArray = bundle.getLongArray("localarray");
				ArrayList<Long> mLocalList = new ArrayList<Long>();
				for (int i = 0; i < mLocalArray.length; i++) {
					mLocalList.add(i, mLocalArray[i]);
				}
				changeBookStatus(NoteBookPickerActivity.this, mRemoteList,
						mLocalList);

			} else if (intent.getAction().equals(MetaData.LOGIN_MESSAGE)) {
				Bundle b = intent.getExtras();
				// BEGIN: Shane_Wang 2012-10-8
				String iActivity = b.getString("invokeActivity");
				// END: Shane_Wang 2012-10-8
				if(iActivity.equals("NoteBookPickerActivity")) { // BEGIN: Shane_Wang 2012-10-8
					int ToastId = b.getInt(MetaData.LOGIN_RESULT);
					if (ToastId == NETWORK_UNAVAIABLE) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								NoteBookPickerActivity.this);
						builder.setTitle(R.string.sync_setting_networkless_title);
						builder.setMessage(R.string.sync_setting_networkless);
						builder.setPositiveButton(R.string.setting,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										try {
											Intent intent = new Intent(
													android.provider.Settings.ACTION_WIFI_SETTINGS);
											intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											startActivity(intent);
											mIsSettingWifi = true;
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								});
	
						builder.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});
						final AlertDialog dialog = builder.create();
						dialog.show();
					} else {
						String account = b.getString(MetaData.LOGIN_ACCOUNT);
						if (ToastId == LOGIN_FAILED) {
							String msg = String.format(
									getResources().getString(
											R.string.sync_setting_sign_failure),
									account);
							Toast toast = Toast.makeText(mContext, msg,
									Toast.LENGTH_LONG);
							toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
							toast.show();
						} else if (ToastId == LOGIN_SUCCESS) {
							String msg = String.format(
									getResources().getString(
											R.string.sync_setting_sign_success),
									account);
							Toast toast = Toast.makeText(mContext, msg,
									Toast.LENGTH_LONG);
							toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
							toast.show();
						}
					}
				}
			}
		}
	};

	// END: Better
	
	public static void setIsRunFirstLoad(){
		mIsRunFirstLoad = false;
	}
	
	// begin wendy
	public static boolean HasPersonalPassword() {
		// begin wendy
		// BEGIN: Better
		if (sSharedPreferences == null) {
			if (MetaData.AppContext == null) {
				return false;
			} else {
				sSharedPreferences = MetaData.AppContext.getSharedPreferences(
						MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
			}
		}
		if (sResources == null) {
			if (MetaData.AppContext == null) {
				return false;
			} else {
				sResources = MetaData.AppContext.getResources();
			}
		}
		// END: Better
		mHasPersonalPassword = sSharedPreferences.getBoolean(
				sResources.getString(R.string.pref_has_password), false);
		// end wendy
		return mHasPersonalPassword;
	}

	// end wendy
	// begin wendy
	public static Context getAppContext() {
		if (scontext != null) {
			return scontext;
		} else { // Better
			return MetaData.AppContext;
		}
	}

	// end wendy
	static boolean bIsOneinputPasswordExist = false;
	// begin wendy private

	AlertDialog inputPasswordDialog = null;
	private void inputPassword() {
		if (bIsOneinputPasswordExist && inputPasswordDialog != null
		        && inputPasswordDialog.isShowing())
		{
			return;
		}
		bIsOneinputPasswordExist = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View view = View.inflate(this, R.layout.one_input_dialog, null);
		final EditText hidePasswordCheck = (EditText) view
				.findViewById(R.id.input_edit_text);
		hidePasswordCheck.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		builder.setCancelable(false);
		builder.setTitle(R.string.password);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setNegativeButton(android.R.string.cancel, null);
		inputPasswordDialog = builder.create();
		inputPasswordDialog.show();
		hidePasswordCheck.requestFocus();//emmanual to fix bug 421148
		DialogUtils.showSoftInput(hidePasswordCheck);
		inputPasswordDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						bIsOneinputPasswordExist = false;
						String inputPassword = hidePasswordCheck.getText()
								.toString();
						String password = mPreference.getString(
								mResources.getString(R.string.pref_password),
								"");
						if ((inputPassword.length() > 0)
								&& (inputPassword.equals(password))) {
							{
								mislocked = false;
								mPreferenceEditor.putBoolean(mResources
										.getString(R.string.lock_state),
										mislocked);
								mPreferenceEditor.commit();

								// modify current book id
								if (mBookcase.getCurrentBookId() == 0) {
									long unlockedbookid = mBookcase
											.getNextUnlockedBookID(false);

									if (unlockedbookid == 0) {
										mBookcase
												.setCurrentBook(BookCase.NO_SELETED_BOOK);
									} else {
										mBookcase
												.setCurrentBook(unlockedbookid);
									}
								}

								updateFragment();
								displayDefaultType();
								Toast.makeText(NoteBookPickerActivity.this,getResources().getString(R.string.show_locked_notebook_info) , Toast.LENGTH_LONG).show();//by show

								//Begin by Emmanual
								if(MetaData.IS_GA_ON)
								{
									GACollector gaCollector = new GACollector(mContext);
									gaCollector.showLockedBooks();
								}
								//End
								}
							inputPasswordDialog.dismiss();
						} else {
							hidePasswordCheck.setText("");
							hidePasswordCheck.setHint(R.string.reset_password_dialog_password_invalid);
						}
					}
				});
		inputPasswordDialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						bIsOneinputPasswordExist = false;
						setDisplayType(mDisplayType);
						inputPasswordDialog.dismiss();
					}
				});
	}
	static boolean bIsOneNoPasswordExist = false;

	// end wendy private

	//begin darwin
	
	static public Bitmap getWidgetThumbnail(Context context,PageDataLoader loader, boolean isLoadAsync, NoteBook notebook ,NotePage page ,int color, int line) {
        Bitmap result = null;
        {
            Resources res = context.getResources();
            Bitmap content;
            Canvas resultCanvas, contentCanvas;
            Paint paint = new Paint();
            int targetWidth, targetHeight;
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            try {
                result = Bitmap.createBitmap((int)res.getDimension(R.dimen.widget_cover_width),(int)res.getDimension(R.dimen.widget_cover_height), Bitmap.Config.ARGB_8888);
                resultCanvas = new Canvas(result);
                targetWidth = (int) result.getWidth() ;//(* 0.9);
                targetHeight = (int) result.getHeight() ;//(* 0.85);
                content = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
                content.setDensity(Bitmap.DENSITY_NONE);
                contentCanvas = new Canvas(content);
                if (!isLoadAsync) {
                	loader.load(page);
                }
                page.load(loader, isLoadAsync, contentCanvas, true, false, false);
                resultCanvas.drawBitmap(content, 0, 0, paint);
                content.recycle();
                content = null;
            }
            catch (OutOfMemoryError e) {
                Log.w(TAG, "[OutOfMemoryError] Loading cover failed !!!");
            }
        }
        return result;
    }
	
	
	public Bitmap getThumbnailNoBackground(PageDataLoader loader, boolean isLoadAsync, NoteBook notebook ,NotePage page ,int color, int line) {
        Bitmap cover = null, result = null;
        {
            Resources res = getResources();
            Bitmap content;
            Canvas resultCanvas, contentCanvas;
            Paint paint = new Paint();
            int targetWidth, targetHeight;
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            try {
            	cover = CoverHelper.getDefaultCoverBitmap(color, line, res);//Allen

                result = Bitmap.createBitmap(cover.getWidth(), cover.getHeight(), Bitmap.Config.ARGB_8888);
                resultCanvas = new Canvas(result);
                cover.recycle();
                cover = null;
                targetWidth = (int) (result.getWidth() * 0.9);
                targetHeight = (int) (result.getHeight() * 0.85);
                content = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
                content.setDensity(Bitmap.DENSITY_NONE);
                contentCanvas = new Canvas(content);
                if (!isLoadAsync) {
                	loader.load(page);
                }
                page.load(loader, isLoadAsync, contentCanvas, true, false, false);
                float left = res.getDimension(R.dimen.thumb_padding_left);
                float top = res.getDimension(R.dimen.thumb_padding_top);
                resultCanvas.translate(left, top);
                resultCanvas.drawBitmap(content, 0, 0, paint);
                content.recycle();
                content = null;
            }
            catch (OutOfMemoryError e) {
                Log.w(TAG, "[OutOfMemoryError] Loading cover failed !!!");
            }
        }
        return result;
    }
	
	static public void saveBookWidgetCoverThumb(Context context,boolean isLoadAsync, NoteBook book ,NotePage page)
	{
		PageDataLoader loader = new PageDataLoader(context);
		try {
            Bitmap bitmap = getWidgetThumbnail(context,loader, isLoadAsync, book,page,book.getBookColor(),book.getGridType());
            if (bitmap != null) {
                File file = new File(page.getFilePath() , MetaData.THUMBNAIL_WIDGET);
                if (file.exists() == false) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bitmap.compress(Bitmap.CompressFormat.PNG, MetaData.THUMBNAIL_QUALITY, bos);
                bitmap.recycle();
                bitmap = null;
                bos.close();
                fos.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void saveBookCoverThumb(PageDataLoader loader, boolean isLoadAsync, NoteBook book ,NotePage page)
    {
    	try {
            Bitmap bitmap = getThumbnailNoBackground(loader, isLoadAsync, book,page,book.getBookColor(),book.getGridType());
            if (bitmap != null) {
                File file = new File(book.getBookPath(), MetaData.THUMBNAIL_PREFIX);
                if (file.exists() == false) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bitmap.compress(Bitmap.CompressFormat.PNG, MetaData.THUMBNAIL_QUALITY, bos);
                bitmap.recycle();
                bitmap = null;
                bos.close();
                fos.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	//RICHARD+FOR DARWIN
	public void genNotebookthumbFromBook(PageDataLoader loader,NoteBook notebook)
	{
		long pageid = notebook.getPageOrder(0);
		NotePage notepage = notebook.getNotePage(pageid);
		if (notepage != null) {
			saveBookCoverThumb(loader, false, notebook,notepage);
		}
	}
	//RICHARD-
	
	public void genNotebookthumb()
	{
		mBookcase = BookCase.getInstance(NoteBookPickerActivity.this);
		int bookNum = mBookcase.getTotalBookNum();
		if(bookNum != 0)
		{
			List<NoteBook> list = mBookcase.getBookList();
			if(list != null)
			{
				PageDataLoader loader = new PageDataLoader(mContext);
				for(int i = 0; i < list.size(); i++)
				{
					NoteBook notebook = list.get(i);
					if (notebook != null) {
						genNotebookthumbFromBook(loader,notebook);
					}
				}
			}
		}
	}
	public void putLastSyncTime(String time)
	{
		mPreferenceEditor.putString(mResources.getString(R.string.last_sync_time),time);
		mPreferenceEditor.commit();
	}
	
	public String getLastSyncTime()
	{
		return  mPreference.getString(mResources.getString(R.string.last_sync_time),NO_LAST_SYNC_TIME);
	}
	
	public void updateLastSyncTime(String time)
	{
		putLastSyncTime(time);
		MetaData.LAST_SYNC_TIME = getLastSyncTime();
	}
	
	// BEGIN: Better
	public void updateMemoStatus() {
		mHasMemo = false;
		Cursor cur = mContext.getContentResolver().query(MetaData.MemoTable.uri, 
        		null, 
        		"is_hidden = 0", 
        		null,
        		null);
        if(cur != null && (cur.getCount() > 0))
		{
        	cur.moveToFirst();
        	while(!cur.isAfterLast())
        	{
        		long bookid = cur.getLong(MetaData.MemoTable.INDEX_BOOK_ID);
        		Cursor cursor = mContext.getContentResolver().query(MetaData.BookTable.uri, 
                		null, 
                		"created_date = ? AND ((userAccount = 0) OR (userAccount = ?))", 
                		new String[] { Long.toString(bookid) ,Long.toString(MetaData.CurUserAccount)},
                		null);
        		if(cursor != null && (cursor.getCount() > 0))
        		{
        			mHasMemo = true;
        			cursor.close();
        			break;
        		}
        		cursor.close();
        		cur.moveToNext();
        	}
		}
        cur.close();
	}
	// END: Better
	
	public void updateTimeStampstatus()
	{
		
		boolean islocked = NoteBookPickerActivity.islocked();
        String selection = "created_date = ? AND ((userAccount = 0) OR (userAccount = ?))";
		
		mHasTimeStamp = false;
		Cursor cur = mContext.getContentResolver().query(MetaData.TimestampTable.uri, 
        		null, 
        		null, 
        		null,
        		null);
        if(cur != null && (cur.getCount() > 0))
		{
        	cur.moveToFirst();
        	while(!cur.isAfterLast())
        	{
        		long pageid = cur.getLong(MetaData.TimestampTable.INDEX_OWNER);
        		Cursor cursor = mContext.getContentResolver().query(MetaData.PageTable.uri, 
                		null, 
                		selection, 
                		new String[] { Long.toString(pageid) ,Long.toString(MetaData.CurUserAccount)},
                		null);
        		if(cursor != null && (cursor.getCount() > 0))
        		{
        			if(islocked)
                	{
        				mHasTimeStamp = false;
        				cursor.moveToFirst();
                		while(!cursor.isAfterLast())
                		{
        	        		long bookId = cursor.getLong(MetaData.PageTable.INDEX_OWNER);
        	        		Cursor cursorBook = mContext.getContentResolver().query(MetaData.BookTable.uri, 
        	                		null, 
        	                		"created_date = ?", 
        	                		new String[] { Long.toString(bookId) },
        	                		null);
        	        		if(cursorBook != null && (cursorBook.getCount() > 0))
        	        		{
        	        			cursorBook.moveToFirst();
        	        			if(cursorBook.getInt(MetaData.BookTable.INDEX_IS_LOCKED) == 0)
        	        			{
        	        				mHasTimeStamp = true;
        	        				cursorBook.close();
        	        				break;
        	        			}
        	        				
        	        		}
        	        		
        	        		cursorBook.close();
        	        		cursor.moveToNext();
                		}
                	}
                	else
                	{
        				mHasTimeStamp = true;
        			}
        			cursor.close();
        			break;
        		}
        		cursor.close();
        		cur.moveToNext();
        	}
		}
        cur.close();
	}
	
	public void updateBookmarkstatus()
	{
		
		boolean islocked = NoteBookPickerActivity.islocked();
        String selection = "(is_deleted = 0 AND is_bookmark = 1 AND ((userAccount = 0) OR (userAccount = ?)))";

		
		Cursor cur = mContext.getContentResolver().query(MetaData.PageTable.uri, 
        		null, 
        		selection, 
        		new String[] { Long.toString(MetaData.CurUserAccount) },
        		null);
        if(cur != null && (cur.getCount() > 0))
		{
        	if(islocked)
        	{
        		mHasBookMark = false;
        		cur.moveToFirst();
        		while(!cur.isAfterLast())
        		{
	        		long bookId = cur.getLong(MetaData.PageTable.INDEX_OWNER);
	        		Cursor cursor = mContext.getContentResolver().query(MetaData.BookTable.uri, 
	                		null, 
	                		"created_date = ?", 
	                		new String[] { Long.toString(bookId) },
	                		null);
	        		if(cursor != null && (cursor.getCount() > 0))
	        		{
	        			cursor.moveToFirst();
	        			if(cursor.getInt(MetaData.BookTable.INDEX_IS_LOCKED) == 0)
	        			{
	        				mHasBookMark = true;
	        				cursor.close();
	        				break;
	        			}
	        				
	        		}
	        		
	        		cursor.close();
	        		cur.moveToNext();
        		}
        	}
        	else
        	{
        		mHasBookMark = true;
        	}
		}
        else
        {
        	mHasBookMark = false;
        }
        cur.close();
	}
	
	static int mFirstIn = 0;
	static public int getFirstIn()
	{
		return mFirstIn;
	}
	static public void setFirstIn(int count) {
		mFirstIn = count;
	}

	//end darwin
	private Attacher[] mAttachers = new Attacher[2];
	private Attacher mAttacher = null;
	private Bundle currentInstanceBundle = null; //
	
	
	//Begin Dave. Add a import task from fileManager.
	private boolean checkIsSnb(String path)
	{
		String filetype = getExtensionName(path);
		if(filetype.equalsIgnoreCase("snb"))
			return true;
		else {
			return false;
		}
	}
	
    public static String getExtensionName(String filename) {   
        if ((filename != null) && (filename.length() > 0)) {   
            int dot = filename.lastIndexOf('.');   
            if ((dot >-1) && (dot < (filename.length() - 1))) {   
                return filename.substring(dot + 1);   
            }   
        }   
        return filename;   
    }   
	
	private boolean isImportMode()
	{
		Bundle bundle = getImportBundle();
		if(bundle == null)
			return false;
		else {
			return true;
		}
	}
	
	public Bundle getImportBundle(){
		Bundle extraBundle = getIntent().getBundleExtra(MetaData.IMPORT_INTENT);
		return extraBundle;
	}
	
	private void startImportTask(Bundle args) {

		final String importPath = args.getString("path");
		boolean isSnb = false;
		isSnb = checkIsSnb(importPath);
		int fileVersion = MetaData.UNKNOWN_VERSION;
		fileVersion = getImportFileVersion(importPath, isSnb);
		
		//Add format error condition.
		if(fileVersion == MetaData.UNKNOWN_VERSION)
		{
			showDialog(IMPORT_FAIL_DIALOG);
			return;
		}

		if (mIsImportTasRunning == false) {
			mImportOptionsMap.clear();
			if (!isSnb) {
				switch (fileVersion) {
				case MetaData.SNE_VERSION_0302:
				case MetaData.SNE_VERSION_3: {
					boolean hasList = false;
					ZipFile zipFile;
					try {
						zipFile = new ZipFile(importPath);

						ZipEntry ze = null;
						BufferedInputStream bis = null;
						AsusFormatReader afr = null;

						Enumeration<?> entries = zipFile.entries();
						while (entries.hasMoreElements()) {
							ze = (ZipEntry) entries.nextElement();
							if (ze.getName().equals(MetaData.BOOK_LIST)) {
								bis = new BufferedInputStream(
										zipFile.getInputStream(ze));
								afr = new AsusFormatReader(bis,
										NotePage.MAX_ARRAY_SIZE);
								boolean isListCorrect = false, isInfoCorrect = false;
								;
								long id = 0;
								String title = "";
								AsusFormatReader.Item item = null;
								for (item = afr.readItem(); item != null; item = afr
										.readItem()) {
									switch (item.getId()) {
									case AsusFormat.SNF_BOOKLIST_BEGIN:
										isListCorrect = true;
										break;
									case AsusFormat.SNF_BOOKINFO_BEGIN:
										if (isListCorrect) {
											isInfoCorrect = true;
										}
										break;
									case AsusFormat.SNF_BOOKINFO_ID:
										if (isInfoCorrect) {
											id = item.getLongValue();
										}
										break;
									case AsusFormat.SNF_BOOKINFO_TITLE:
										if (isInfoCorrect) {
											title = item.getStringValue();
										}
										break;
									case AsusFormat.SNF_BOOKINFO_END:
										if (isInfoCorrect) {
											isInfoCorrect = false;
											mImportOptionsMap.put(id, title);
										}
										break;
									case AsusFormat.SNF_BOOKLIST_END:
										isListCorrect = false;
										break;
									default:
										break;
									}
								}
								bis.close();
								hasList = true;
								break;
							}
						}
						zipFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					if (hasList) {
						Bundle b = new Bundle();
						b.putString("path", importPath);
						b.putInt("version", fileVersion);
						b.putBoolean("issnb", isSnb);
						showDialog(IMPORT_OPTION_DIALOG, b);
					} else {
						mImportTask = new ImportNewFileTask(importPath, null,
								false, fileVersion);
						if (mImportTask != null
								&& mImportTask.isCancelled() == false) {
							mImportTask.execute();
						}
					}
					break;
				}
				default:
					mImportTask = null;
					break;
				}
			} else {
				if ((fileVersion == MetaData.SNE_VERSION_0302)
						|| (fileVersion == MetaData.SNE_VERSION_3)) {
					Cursor cursor = null;
					try {
						ZipFile zipFile = new ZipFile(new File(importPath));
						ZipEntry dbZipEntry = new ZipEntry(
								MetaData.DATABASE_NAME);
						File dbFile = new File(MetaData.DATA_DIR,
								MetaData.DATABASE_NAME);
						BufferedOutputStream bos = new BufferedOutputStream(
								new FileOutputStream(dbFile));
						copyZipFile(zipFile.getInputStream(dbZipEntry), bos);
						bos.close();
						SQLiteDatabase db = SQLiteDatabase.openDatabase(
								dbFile.getAbsolutePath(), null,
								SQLiteDatabase.OPEN_READONLY);
						cursor = db.query(MetaData.BOOK_TABLE, null,
								null, null, null, null, null);
						cursor.moveToFirst();
						while (!cursor.isAfterLast()) {
							int index = -1;
							index = cursor
									.getColumnIndex(MetaData.BookTable.CREATED_DATE);
							if (index >= 0) {
								long id = cursor.getLong(index);
								index = cursor
										.getColumnIndex(MetaData.BookTable.TITLE);
								if (index >= 0) {
									String title = cursor.getString(index);
									mImportOptionsMap.put(id, title);
								}
							}
							cursor.moveToNext();
						}
						cursor.close();
						db.close();
						if (mImportOptionsMap.size() > 0) {
							Bundle b = new Bundle();
							b.putString("path", importPath);
							b.putInt("version", fileVersion);
							b.putBoolean("issnb", isSnb);
							showDialog(IMPORT_OPTION_DIALOG, b);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}finally{
						if(cursor != null)
							cursor.close(); //smilefish add for memory leak
					}
					
				} else {
					mImportTask = null;
				}
			}
		}
	}
	//End Dave.
	
	//start mars_li
	void initPerference(){
		sSharedPreferences = mPreference = getSharedPreferences(
				MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		mPreferenceEditor = mPreference.edit();
		sResources = mResources = getResources();
	}
	
	boolean isDirectToEditor() {
		boolean firstLoad = mPreference.getBoolean(
				mResources.getString(R.string.pref_first_start), true);
		return !mIsRunFirstLoad && !firstLoad;
	}
	
	//add by mars for  382097
	public boolean existLastPage(){
	    return mPreference.getBoolean(MetaData.existLastPage, false);
	}
	
	//end mars_li
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		currentInstanceBundle = savedInstanceState;
		super.onCreate(savedInstanceState);
		initPerference();

		mIsFirstLoad = true;
		mHasCreated = false;
		
		checkWidgetOneInstalledOrNot();//smilefish

		if(getIntent().getBooleanExtra(MetaData.START_FROM_WIDGET, false)){
			mIsShowAddDialog =true;
		}
		if(getIntent().getBooleanExtra(MetaData.START_FROM_WIDGET, false)){
			MetaData.ADD_BOOK_FROM_WIDGET = true;
		}
		
		//begin smilefish for do it later
		boolean firstLoad = mPreference.getBoolean(
                getResources().getString(R.string.pref_first_load),true);
		if(!firstLoad && loadPageFromDoItLater())//check if data is clean
			return;
		//end smilefish
		
		//Begin:Dave. Fix the Bug: improve the launch speed at first time.
		if( !isShareMode() && 
		        !isImportMode()&&
		        isDirectToEditor()&&
		        !mBackFromEditor&&
		        existLastPage()&& //fix bug 382097
		        !MetaData.ADD_BOOK_FROM_WIDGET){ //add book from widget[Carol]
		         
			//emmanual 
			boolean isOpenLastEditPage = getSharedPreferences(MetaData.PREFERENCE_NAME, Activity.MODE_PRIVATE)
			        .getBoolean(getResources().getString(R.string.pref_last_edit_page),false);// emmanual
			if (isOpenLastEditPage && checkLoadLast()) { //smilefish fix bug 651274
				return;
			}
		}
			
		//End:Dave.
		
		createActivity();
		if(!InitSyncFilesInfoHelper.isSyncFilesInfoInited() && !InitSyncFilesInfoHelper.InitSyncFilesInfoAsyncTask.isTaskRunning()){
			InitSyncFilesInfoHelper.InitSyncFilesInfoAsyncTask task = new InitSyncFilesInfoHelper.InitSyncFilesInfoAsyncTask();
			task.execute();
		}
		if(getIntent().getBooleanExtra(MetaData.START_FROM_WIDGET, false)){
			if(!MetaData.SuperNoteUpdateInfoSet.containsKey(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_WIDGET_DATA_INVALID)){
    			MetaData.SuperNoteUpdateInfoSet.put(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_WIDGET_DATA_INVALID,null);
    		}
    		MetaData.SuperNoteUpdateInfoSet.clear();
		}

		//Begin by Emmanual
		if(MetaData.IS_GA_ON)
		{
			GACollector gaCollector = new GACollector(mContext);
			gaCollector.openSuperNote();
		}
		//End
	}
	
	//begin smilefish: check widget one installed or not, if installed, need to save widget thumbnail
	private void checkWidgetOneInstalledOrNot(){
    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(NoteBookPickerActivity.this);
        List<AppWidgetProviderInfo> widgetList = appWidgetManager.getInstalledProviders();
        for(AppWidgetProviderInfo info:widgetList){
            String name = info.provider.getClassName();
            if(name.equalsIgnoreCase("com.asus.supernote.widget.WidgetOneProvider")){
            	MetaData.IS_ENABLE_WIDGET_THUMBNAIL = true;
            }
        }
    }
	//end smilefish
	
	private void createActivity()
	{
		scontext = getApplicationContext();// wendy
		// BEGIN: Better
		if (MetaData.AppContext == null) {
			MetaData.AppContext = scontext;
		}
		// END: Better
		if(!isShareMode() || !isImportMode())
			mFirstIn++ ;
		//begin darwin
		CountDownClass countDown = CountDownClass.getInstance(mContext);
		countDown.SetActivity((Activity)NoteBookPickerActivity.this);
		//end   darwin
		mAttachers[0] = new CameraAttacher();
		((CameraAttacher)mAttachers[0]).setActivity(this);
		mAttachers[1] = new GalleryAttacher();
		((GalleryAttacher)mAttachers[1]).setActivity(this);
		
		// BEGIN: RICHARD
		CFG.setPath(mContext.getDir("Data", 0).getAbsolutePath());
		// END: RICHARD

		deviceType = PickerUtility.getDeviceType(mContext);
		
        //Begin: show_wang@asus.com
    	//Modified reason: for multi-dpi
    	int IsOrientationAllowOrNot = this.getResources().getInteger(R.integer.is_orientation_allow); //by show
        if (IsOrientationAllowOrNot == 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        else  if (IsOrientationAllowOrNot ==1){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
		 else  {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //End: show_wang@asus.com
        
		s_isMainActivityExist = true;//darwin
		NoteBookViewString = mContext.getResources().getString(R.string.notebook_view);
	    BookMarkViewString = mContext.getResources().getString(R.string.bookmark_view);
	    TimeStampViewString = mContext.getResources().getString(R.string.timestamp_view);
	    AllPageViewString = mContext.getResources().getString(R.string.allpage_view);
		// BEGIN:shaun_xu@asus.com
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int densityDpi = metric.densityDpi;
		MetaData.DPI = densityDpi;
		if (deviceType > 100) {
			MetaData.BASE_DPI = 160;
		} else {
			MetaData.BASE_DPI = 240;
		}
		// END:shaun_xu@asus.com

		//begin darwin

		MetaData.LAST_SYNC_TIME = getLastSyncTime();
		//end darwin
		
		// BEGIN:shaun
		failedImage = BitmapFactory.decodeResource(mResources, R.drawable.asus_ep_sync_erro);
		successImage = BitmapFactory.decodeResource(mResources, R.drawable.asus_ep_sync);
		progressImage = BitmapFactory.decodeResource(mResources, R.drawable.asus_ep_sync);
		// END:shaun

		if (mPreference.getBoolean(
				//mResources.getString(R.string.pref_first_load), true) == false) {
				mResources.getString(R.string.pref_first_start), true) == false) {

			mContentResolver = getContentResolver();
			mBookcase = BookCase.getInstance(NoteBookPickerActivity.this);
			/*mars_li for colorfulbar*/
			//ColorfulStatusActionBarHelper.setContentView(R.layout.notebookpicker, true, this);
			setContentView(R.layout.notebookpicker);  //the colorful bar is set through drawer of common ui lib
			/*mars_li end*/
			
			// BEGIN: Better
			if (MetaData.IS_ENABLE_WEBSTORAGE_DATA_MIGRATING) {
				if (mPreference.getBoolean(mResources.getString(R.string.pref_upgrade_first_time), true)) {
					Cursor cursor = mContentResolver.query(MetaData.WebAccountTable.uri, null, null, null, null);
					if (cursor != null) {
						if (cursor.getCount() > 0) {
							cursor.moveToFirst();
							while (!cursor.isAfterLast()) {
								int index = cursor.getColumnIndex(MetaData.WebAccountTable.ID);
								if (index >= 0) {
									long id = cursor.getLong(index);
									File dir = new File(MetaData.USER_TO_UPGRAGE_LIST_DIR);
									if (!dir.exists()) {
										dir.mkdirs();
									}
									File file = new File(dir, Long.toString(id));
									if (!file.exists()) {
										try {
											file.createNewFile();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								}
								cursor.moveToNext();
							}
						}
						cursor.close();
					}
					mPreferenceEditor.putBoolean(mResources.getString(R.string.pref_upgrade_first_time), 
							false).commit();
				}
			}
			// END: Better

			mFragmentManager = getFragmentManager();
			mLeftSideLayout = (FrameLayout) findViewById(R.id.left_side);
			mbook_actionbar = (LinearLayout) View.inflate(
					NoteBookPickerActivity.this, R.layout.book_actionbar, null);// wendy
			
			int orignalDisplayType = DISPLAY_ALL;
			if (currentInstanceBundle != null) {
				orignalDisplayType = currentInstanceBundle.getInt("displayType");
				orignalDisplayType = mPreference
						.getInt(mResources
								.getString(R.string.pref_picker_display_type),
								DISPLAY_ALL);
			}
			
			//Begin Dave. Fix the Bug:screen rotation may cause incorrect display mode 
			if(mDisplayType != DISPLAY_NONE && mDisplayType != DISPLAY_PAGE)
				orignalDisplayType = mDisplayType;
			//End Dave.
			
			mActionBar = getActionBar(); //smilefish fix bug 576732
			mActionBar.setDisplayShowHomeEnabled(true); 
			
			setDisplayType(orignalDisplayType);
			mCurrentDrawerIndex = orignalDisplayType; //fix bug 556017

			// end wendy
			mLeftSideLayout.setOnDragListener(dragListener);

			// load tutorial automatically
			long tutorialBookId = BookCase.NO_SELETED_BOOK;
			if (deviceType > 100
					&& (tutorialBookId = mPreference
							.getLong(mResources
									.getString(R.string.pref_pad_tutorial_id),
									BookCase.NO_SELETED_BOOK)) != BookCase.NO_SELETED_BOOK) {
				mPreferenceEditor.putLong(
						mResources.getString(R.string.pref_pad_tutorial_id),
						BookCase.NO_SELETED_BOOK).commit();
				mBookcase.setCurrentBook(tutorialBookId);
			} else if (deviceType <= 100
					&& (tutorialBookId = mPreference.getLong(mResources
							.getString(R.string.pref_phone_tutorial_id),
							BookCase.NO_SELETED_BOOK)) != BookCase.NO_SELETED_BOOK) {
				mPreferenceEditor.putLong(
						mResources.getString(R.string.pref_phone_tutorial_id),
						BookCase.NO_SELETED_BOOK).commit();
				mBookcase.setCurrentBook(tutorialBookId);
				displayPageGridView(tutorialBookId, false);
			}

			// BEGIN: Better
			if ((MetaData.CurUserAccount > 0) && MetaData.IsJustLaunching) {
				mCountDown = CountDownClass
							.getInstance(getApplicationContext());
				//begin darwin
				mCountDown.SetActivity((Activity)NoteBookPickerActivity.this);
				//end   darwin
				if (mPreference.getString(
						getResources().getString(R.string.pref_asus_auto_sync),
						MetaData.DEFAULT_SYNC_MODE).equals("auto")) {
					// add by wendy 0409 begin++
					int default_synctime = mPreference.getInt(
							mResources.getString(R.string.pref_default_sync_time),
							MetaData.DEFAULT_SYNC_TIME);
					mCountDown.StartCountDown(default_synctime, true, true);
					// add by wendy 0409 end---
				} else {
					mCountDown.StartTask(true);
				}
			}
			
			if (MetaData.IsJustLaunching) {
		        try{ //smilefish to fix bug 442784
		        	PdfConverter.getInstance().Jpg2Pdf(new String[0], "");
		        }catch(Exception e){
		        	e.printStackTrace();
		        }
			}
			
			MetaData.IsJustLaunching = false;

			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(MetaData.SYNC_UPDATE_UI);
			intentFilter.addAction(MetaData.CHOOSE_BOOKS);
			intentFilter.addAction(MetaData.LOGIN_MESSAGE);
			registerReceiver(updateUiReceiver, intentFilter);
			
            mStateBar = (View) this.findViewById(R.id.statebar);
            View viewimage = (View) this
					.findViewById(R.id.sync_book_setting);
			if(!MetaData.isATT()){ //Carol
				mStateBar.setOnClickListener(stateBarClickListener);
				viewimage.setOnClickListener(stateBarClickListener);
			}else{
				mStateBar.setVisibility(View.GONE);
				viewimage.setVisibility(View.GONE);
			}

				if (!mIsLoadFotaDataTaskRunning) {
					if (!mIsFirstLoad) {
						Cursor cursor = mContentResolver
								.query(MetaData.BookTable.uri,
										new String[] { MetaData.BookTable.CREATED_DATE },
										"(version = ?)", new String[] { "1" },
										null);
						cursor.moveToFirst();
						while (!cursor.isAfterLast()) {
							long id = cursor.getLong(0);
							if (!MetaData.V1BookIdList.contains(id)) {
								MetaData.V1BookIdList.add(id);
							}
							cursor.moveToNext();
						}
						cursor.close();
						V1DataConverter converter = new V1DataConverter(
								mContext);
						converter.preprocessBooks();
					}
					File v1Dir = new File(V1DataFormat.DATA_DIR);
					if (v1Dir.exists()) {
						LoadFotaDataTask task = new LoadFotaDataTask(1);
						task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					} else {
					}
				}

			mIsFirstLoad = false; //Dave
			mHasCreated = true; //Dave
			mBackFromEditor = false; //Dave
			MetaData.IS_LOAD = true; //Carol
			
	        //BEGIN: RICHARD
			if(CFG.getCanDoVO() == true)//darwin
			{
		        ComponentName componantName = new ComponentName(this.getPackageName(), IndexService.class.getName());
		        Intent sIntent = new Intent().setComponent(componantName);
		        sIntent.setAction(IndexService.INDEXER_START_INTENT);
		        startService(sIntent);
		        mIndexServiceClient = new IndexServiceClient();
		        mIndexServiceClient.doBindService(this);     
			}
	        //END: RICHARD
			// END: Better
	        
	        
	        List<NoteBook> myList = mBookcase.getBookList();
	        if(myList.size() != 0)
	        {
	        	PageDataLoader loader = new PageDataLoader(mContext);
	        	for(NoteBook nb : myList)
	        	{
	        		long bookId = nb.getCreatedTime();
	        		String bookThumbPath = MetaData.DATA_DIR + Long.toString(bookId) + "/"
	        				+ MetaData.THUMBNAIL_PREFIX;
	        		File bookThumbFile = new File(bookThumbPath);
	        		if (!bookThumbFile.exists()) 
	        		{
	        			genNotebookthumbFromBook(loader,nb);
	        		}
	        	}
	        }
	        
	        //darwin
	        if (mPageGridFragment != null) {
	        mPageGridFragment.setonCreate();
	        }
	        //darwin
			
		} else {
			mIsFirstLoad = true;
			firstRunLoadResource(mPreference,mResources,this);
			mIsRunFirstLoad = true; //add by mars 
			
		}
		showAddBookDialog(mIsShowAddDialog, false); //by show
		mIsShowAddDialog = false;
		//begin noah;for share
		if(isShareMode()){
			if(mStateBar != null)
				mStateBar.setVisibility(View.GONE);
		}
		//end noah;for share
		
		SuperNoteApplication.setContext(getApplicationContext());

	    //+++ Dave. Fix the the bug: a black screen will show when supernote is launched at first time
		boolean firstStart = mPreference.getBoolean(
				mResources.getString(R.string.pref_first_start), true);
		boolean firstLoad = mPreference.getBoolean(
				mResources.getString(R.string.pref_first_load), true);
		//begin noah;for datacopy
		if(!isShareMode()){
			if(DataCopyActivity.needDataCopy() && !DataCopyActivity.isRunning())
			{
				new CopyTask().execute(DataCopyActivity.FROM_DIR, DataCopyActivity.TO_DIR);
				DataCopyActivity.RUN_COUNT++;
			}else if( firstStart && firstLoad && !DataCopyActivity.isRunning()) 
			{
				if (DataCopyActivity.needDataCopy()) {
					new CopyTask().execute(DataCopyActivity.FROM_DIR, DataCopyActivity.TO_DIR);
				}
				DataCopyActivity.RUN_COUNT++;
			}
		}else {
			if( firstStart && firstLoad && !DataCopyActivity.isRunning())
			{
				if (DataCopyActivity.needDataCopy()) {
					new CopyTask().execute(DataCopyActivity.FROM_DIR, DataCopyActivity.TO_DIR);
				}
				DataCopyActivity.RUN_COUNT++;
			}
		}
		//end noah
		
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putBoolean(getResources().getString(R.string.pref_first_start), false);
        editor.commit();
		//---
		
		//Begin Dave. Add a import task from fileManager.
		if(isImportMode())
		{
			Bundle args = getIntent().getBundleExtra(MetaData.IMPORT_INTENT);
			startImportTask(args);
		}
		//End Dave.

		//begin smilefish
		if(mIsExportPdfWhenDDS && mExport2PdfTask != null){
			mIsExportPdfWhenDDS = false;
			mExport2PdfTask.showSuccessDialog();
		}	
		if(mIsExportWhenDDS && mExportTask != null){
			mIsExportWhenDDS = false;
			mExportTask.showSuccessDialog();
		}	
		if(mIsImportWhenDDS && mImportTask != null){
			mIsImportWhenDDS = false;
			mImportTask.showSuccessDialog();
		}
		//end smilefish
		initDrawerLayout();
	}

	//+++ Dave  for DrawerLayout
	private TextView textView1;
	private TextView textView2;
	private TextView textView3;
	private TextView textAllPage;

	private TextView numView1;
	private TextView numView2;
	private TextView numView3;
	private TextView numAllPage;
	
	private ArrayList<TextView> textViewList;
	private ArrayList<TextView> numViewList;
	private ArrayList<LinearLayout> itemViewList;
	
	private int mCurrentDrawerIndex = 0;

	private void initDrawerLayout()
	{
	    if(mDrawerLayout!=null){
	    	mDrawerLayout.detachActivity();
	    }
		
		mColorfulLinearLayout = (ColorfulLinearLayout) findViewById(R.id.colorful_layout);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (RelativeLayout) findViewById(R.id.left_drawer);
        
        if(mDrawerLayout == null)
        	return;
        
        textView1 = (TextView) findViewById(R.id.drawer_textview1);
        textView2 = (TextView) findViewById(R.id.drawer_textview2);
        textView3 = (TextView) findViewById(R.id.drawer_textview3);
        textAllPage = (TextView) findViewById(R.id.drawer_allpage_text);
        textView1.setText(NoteBookViewString);
        textView2.setText(BookMarkViewString);
        textView3.setText(TimeStampViewString);
        textAllPage.setText(AllPageViewString);
        
        textViewList = new ArrayList<TextView>();
        textViewList.add(textView1);
        textViewList.add(textView2);
        textViewList.add(textView3);
        textViewList.add(textAllPage);
        
        numView1 = (TextView) findViewById(R.id.drawer_numview1);
        numView2 = (TextView) findViewById(R.id.drawer_numview2);
        numView3 = (TextView) findViewById(R.id.drawer_numview3);
        numAllPage = (TextView) findViewById(R.id.drawer_allpage_num);
        
        numViewList = new ArrayList<TextView>();
        numViewList.add(numView1);
        numViewList.add(numView2);
        numViewList.add(numView3);
        numViewList.add(numAllPage);
        
        LinearLayout itemView1 = (LinearLayout)findViewById(R.id.drawer_itemview1);
        LinearLayout itemView2 = (LinearLayout)findViewById(R.id.drawer_itemview2);
        LinearLayout itemView3 = (LinearLayout)findViewById(R.id.drawer_itemview3);
        LinearLayout itemAllPage = (LinearLayout)findViewById(R.id.drawer_allpage);
        
        itemViewList = new ArrayList<LinearLayout>();
        itemViewList.add(itemView1);
        itemViewList.add(itemView2);
        itemViewList.add(itemView3);
        itemViewList.add(itemAllPage);

        itemView1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				notifyActionBar(DISPLAY_ALL);
				selectItem(0);
			}
		});
        itemView2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(getBookmarkCount() != 0)
				{
					notifyActionBar(DISPLAY_BOOKMARK);
					selectItem(1);
				}
			}
		});
        itemView3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(getTimeStampCount() != 0)
				{
					notifyActionBar(DISPLAY_TIMESTAMP);
					selectItem(2);
				}
			}
		});
        itemAllPage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(getAllPageCount() != 0){
					notifyActionBar(DISPLAY_ALL_PAGE);
					selectItem(DISPLAY_ALL_PAGE);
				}
			}
		});
        updateDrawerItemColor(mCurrentDrawerIndex);
        notifyActionBar(mCurrentDrawerIndex);
        
        mStatesync = (TextView) findViewById(R.id.statebar_syncpro);
        mSyncImage = (ImageView) findViewById(R.id.statebar_image);
        mNoSync = (TextView) findViewById(R.id.statebar_nosync);
        if(!MetaData.isATT()){ //Carol
			mStatesync.setSelected(true);
			mNoSync.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					String RAsusAccount = mPreference.getString(
							mResources.getString(R.string.pref_AsusAccount), null);
	
					String RAsusPassword = mPreference.getString(
							mResources.getString(R.string.pref_AsusPassword), null);
					if (RAsusAccount != null && !RAsusAccount.equals("")
							&& RAsusPassword != null && !RAsusPassword.equals("")) {
							preparePopupWindowCloud(mNoSync);
					} else {
						try {
							showLoginDialog(STATEBAR_BOOK_ENABLE,"NoteBookPickerActivity");
	
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
			mSyncProgressAnimation = AnimationUtils.loadAnimation(this,
			        R.anim.sync_progress);
		}else{
			mStatesync.setVisibility(View.GONE);
			mSyncImage.setVisibility(View.GONE);
			mNoSync.setVisibility(View.GONE);
		}
		updateSyncStatusBar();
		
        if(mDrawerLayout != null && mDrawerList != null)
        {
			// set a custom shadow that overlays the main content when the
			// drawer opens
			mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,Gravity.START);
			
			// enable ActionBar app icon to behave as action to toggle nav drawer
			setActionBarHomeUp(true);
			
	        // ActionBarDrawerToggle ties together the proper interactions
	        // between the sliding drawer and the action bar app icon
	        mDrawerToggle = new ActionBarDrawerToggle(
	                this,             /* host Activity */
	                mDrawerLayout,         /* DrawerLayout object */
	                R.drawable.asus_ic_drawer,  /* nav drawer image to replace 'Up' caret */
	                R.string.drawer_open,  /* "open drawer" description for accessibility */
	                R.string.drawer_close  /* "close drawer" description for accessibility */
	                ) {

	            public void onDrawerStateChanged(int newState) {
	            	if (newState == DrawerLayout.STATE_IDLE) {
	                    if (mPendingSelectedItem >= 0) {
	                        changeFragement(mPendingSelectedItem);
	                        mPendingSelectedItem = -1;
	                    }
	                }
	            	mDrawerLayout.setBackgroundColor(mContext.getResources().getColor(R.color.drawer_background_color)); //smilefish fix bug 503241
	            }
	            
	            public void onDrawerSlide(View arg0, float arg1) {
	            	invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }
	        	
	            public void onDrawerClosed(View view) {
	            	invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }

	            public void onDrawerOpened(View drawerView) {
	            	updateDrawerStatus();
	            	invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }
	        };
	        mDrawerLayout.setDrawerListener(mDrawerToggle);

	        final boolean isTranslucentEnabled = (getResources().getIdentifier(
	                "windowTranslucentStatus", "attr", "android") != 0);
	        final boolean versionLorLater = android.os.Build.VERSION.SDK_INT >= 21;//android.os.Build.VERSION_CODES.L;
	        if (!isTranslucentEnabled || versionLorLater) {
	            // non-ASUS devices or Android version > L
	            if (!versionLorLater) {
	                // Customize action bar color if Android version <= 4.4
	                // App's action bar background color.
	                getActionBar().setBackgroundDrawable(
	                        new ColorDrawable(ColorfulStatusActionBarHelper.COLOR_STATUS_ACTION_BAR)) ;
	            }
	            mColorfulLinearLayout.setActionBarBackgroundVisibility(View.GONE);
	            mColorfulLinearLayout.setStatusBarBackgroundVisibility(View.GONE);
	            mDrawerLayout.attachActivity(this);
	        } else {
	            // Asus devices with Android version 4.3 or 4.4
	        	mDrawerLayout.setBackgroundColor(Color.WHITE); //different actionbar height problem
	            mDrawerLayout.attachActivity(this, mColorfulLinearLayout,
	            		ColorfulStatusActionBarHelper.COLOR_STATUS_ACTION_BAR);
	            
	            // To let ActionBar use different color with StatusBar
                mColorfulLinearLayout.setActionBarBackgroundColor(
                        getResources().getColor(R.color.action_bar_bg_color));
	        }
        }
	}
	
	private void notifyActionBar(int index)
	{
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		switch(index){
		case DISPLAY_ALL:
			if(!isShareMode())
				actionBar.setTitle(NoteBookViewString);
			break;
		case DISPLAY_BOOKMARK:
			actionBar.setTitle(BookMarkViewString);
			break;
		case DISPLAY_TIMESTAMP:
			actionBar.setTitle(TimeStampViewString);
			break;
		case DISPLAY_ALL_PAGE:
			actionBar.setTitle(AllPageViewString);
			break;
		default:
			actionBar.setTitle(NoteBookViewString);
		}
	}
	
	private void updateDrawerItemColor(int index)
	{
		if(index < textViewList.size() && index < numViewList.size())
		{
			for(int i= 0; i < textViewList.size(); i++)
			{
				if(i == index)
				{
					textViewList.get(i).setSelected(true);
					numViewList.get(i).setSelected(true);
					itemViewList.get(i).setSelected(true);
					textViewList.get(i).setTextSize(getResources().getInteger(R.integer.drawer_item_textsize_pressed));
					numViewList.get(i).setTextSize(getResources().getInteger(R.integer.drawer_item_textsize_pressed));
				}else
				{
					textViewList.get(i).setSelected(false);
					numViewList.get(i).setSelected(false);
					itemViewList.get(i).setSelected(false);
					textViewList.get(i).setTextSize(getResources().getInteger(R.integer.drawer_item_textsize_normal));
					numViewList.get(i).setTextSize(getResources().getInteger(R.integer.drawer_item_textsize_normal));
				}
			}
		}		
	}
	
	
	private void updateDrawerNumText()
	{
		if (numView1 != null && numView2 != null && numView3 != null && numAllPage != null) {
			numView1.setText(String.valueOf(getAllBookCount()));
			numView2.setText(String.valueOf(getBookmarkCount()));
			numView3.setText(String.valueOf(getTimeStampCount()));
			numAllPage.setText(String.valueOf(getAllPageCount()));
		}
	}
	
	
	private int getAllBookCount()
	{
		String mSortOrder = MetaData.BookTable.TITLE;
		boolean islocked = NoteBookPickerActivity.islocked();
		Cursor mCursor = null;
		if (islocked) {
			mCursor = mContext
					.getContentResolver()
					.query(MetaData.BookTable.uri,
							null,
							"(is_locked = 0) AND ((userAccount = 0) OR (userAccount = ?))",
							new String[] { Long
									.toString(MetaData.CurUserAccount) },
									mSortOrder);
		} else {
			mCursor = mContext.getContentResolver().query(
					MetaData.BookTable.uri, null,
					"(userAccount = 0) OR (userAccount = ?)",
					new String[] { Long.toString(MetaData.CurUserAccount) },
					mSortOrder);
			
		}
		int size = mCursor.getCount();
		mCursor.close();
		return size;
	}
	
	private int getAllPageCount(){
		int pageCount = 0;
		int lockedCount = 0;
		String selection = "(is_deleted = 0 AND ((userAccount = 0) OR (userAccount = ?)))";

		Cursor cur = mContext.getContentResolver().query(
		        MetaData.PageTable.uri, null, selection,
		        new String[] { Long.toString(MetaData.CurUserAccount) }, null);
		if (cur != null && (cur.getCount() > 0)) {
			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				long bookId = cur.getLong(MetaData.PageTable.INDEX_OWNER);
				Cursor cursor = mContext.getContentResolver().query(
				        MetaData.BookTable.uri, null, "created_date = ?",
				        new String[] { Long.toString(bookId) }, null);
				if (cursor != null && (cursor.getCount() > 0)) {
					cursor.moveToFirst();
					if (cursor.getInt(MetaData.BookTable.INDEX_IS_LOCKED) == 0) {
						pageCount++;
					}
					else{
						lockedCount++;
					}	
				}
				cursor.close();
				cur.moveToNext();
			}
		} 
		cur.close();

		if (mislocked) {
			return pageCount;
		}
		else{
			return pageCount + lockedCount;
		}
	}
	
	private int getBookmarkCount()
	{	
		int bookCount = 0;
		int lockedCount = 0;
		String selection = "(is_deleted = 0 AND is_bookmark = 1 AND ((userAccount = 0) OR (userAccount = ?)))";

		Cursor cur = mContext.getContentResolver().query(
		        MetaData.PageTable.uri, null, selection,
		        new String[] { Long.toString(MetaData.CurUserAccount) }, null);
		if (cur != null && (cur.getCount() > 0)) {
			mHasBookMark = false;
			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				long bookId = cur.getLong(MetaData.PageTable.INDEX_OWNER);
				Cursor cursor = mContext.getContentResolver().query(
				        MetaData.BookTable.uri, null, "created_date = ?",
				        new String[] { Long.toString(bookId) }, null);
				if (cursor != null && (cursor.getCount() > 0)) {
					cursor.moveToFirst();
					if (cursor.getInt(MetaData.BookTable.INDEX_IS_LOCKED) == 0) {
						bookCount++;
					}
					else{
						lockedCount++;
					}	
				}
				cursor.close();
				cur.moveToNext();
			}
		} else {
			mHasBookMark = false;
		}
		cur.close();

		//begin smilefish fix bug 366529
		if (mislocked) {
			return bookCount;
		}
		else{
			return bookCount + lockedCount;
		}
		//end smilefish
	}
	
	private int getTimeStampCount()
	{
		int bookCount = 0;
		int lockedCount = 0;
		String selection = "created_date = ? AND ((userAccount = 0) OR (userAccount = ?))";

		mHasTimeStamp = false;
		Cursor cur = mContext.getContentResolver().query(
		        MetaData.TimestampTable.uri, null, null, null, null);
		if (cur != null && (cur.getCount() > 0)) {
			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				long pageid = cur.getLong(MetaData.TimestampTable.INDEX_OWNER);
				Cursor cursor = mContext.getContentResolver().query(
				        MetaData.PageTable.uri,
				        null,
				        selection,
				        new String[] { Long.toString(pageid),
				                Long.toString(MetaData.CurUserAccount) }, null);
				if (cursor != null && (cursor.getCount() > 0)) {
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						long bookId = cursor
						        .getLong(MetaData.PageTable.INDEX_OWNER);
						Cursor cursorBook = mContext.getContentResolver()
						        .query(MetaData.BookTable.uri, null,
						                "created_date = ?",
						                new String[] { Long.toString(bookId) },
						                null);
						if (cursorBook != null && (cursorBook.getCount() > 0)) {
							cursorBook.moveToFirst();
							if (cursorBook
							        .getInt(MetaData.BookTable.INDEX_IS_LOCKED) == 0) {
								bookCount++;
							}
							else{
								lockedCount++;
							}
						}
						cursorBook.close();
						cursor.moveToNext();
					}
					cursor.close();
				}
				cursor.close();
				cur.moveToNext();
			}
		}
		cur.close();

		if (mislocked) {
			return bookCount;
		}
		else{
			return bookCount + lockedCount;
		}
	}	
	
	private void updateDrawerStatus()
	{
		updateDrawerNumText();
	}
	//---

    private void selectItem(int position) {
    	if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mPendingSelectedItem = position;
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            mPendingSelectedItem = -1;
            changeFragement(position);
        }
    }
    
    private void changeFragement(int position) {
    	int index = 0;
    	if(position == 0)
    	{
    		if(getAllBookCount() != 0)
    			index = 0;    		
    	}else if(position == 1)
    	{
    		if(getBookmarkCount() != 0)
    			index = 1;
    	}else if(position == 2)
    	{
    		if(getTimeStampCount() != 0)
    			index = 2;
    	}else if(position == DISPLAY_ALL_PAGE){
    		if(getAllPageCount() != 0)
    			index = DISPLAY_ALL_PAGE;
		}
		updateDrawerItemColor(index);
    	setDisplayType(index);
		mCurrentDrawerIndex = index;
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
    
    //begin smilefish
    private boolean loadPageFromDoItLater(){
		Intent callbackIntent = getIntent();
		if(callbackIntent.getAction() == MetaData.ACTION_READ_LATER){
			long bookId = callbackIntent.getLongExtra(MetaData.BOOK_ID, 0L);
			long pageId = callbackIntent.getLongExtra(MetaData.PAGE_ID, 0L);
			
		    if(bookId == 0&&pageId == 0){
		    	return false;
			} 
			
            if (mBookcase == null) {
	               mBookcase = BookCase.getInstance(this);
	        }
		    NoteBook book = mBookcase.getNoteBook(bookId);
		    boolean isCloudFile = book != null && book.getUserId() > 0;
			String sAsusPassword = mPreference.getString(
					mContext.getString(R.string.pref_AsusPassword), null);
		    if(book != null && isCloudFile && (sAsusPassword != null && sAsusPassword.equals(""))){ //cloud file+logout status
				return false; //Carol-TT445121
			}
		    if(book != null && book.getIsLocked())
		    	return false;
		    
		    try {
		       Intent intent = new Intent();
		       intent.setClass(this, EditorActivity.class);
		       intent.putExtra(MetaData.BOOK_ID, bookId);
		       intent.putExtra(MetaData.PAGE_ID, pageId);
		       startActivity(intent);
		    } catch (ActivityNotFoundException e) {
		       // TODO Auto-generated catch block
		       e.printStackTrace();
		    }

		    return true;
		}
		
		return false;
    }
    //end smilefish
	
	//Begin:Dave. Fix the Bug: improve the launch speed at first time.
	private boolean checkLoadLast(){
	     long bookId = 0;
	     long pageId = 0;
	     ContentResolver cr = getContentResolver();
	     Cursor cursor = cr.query(MetaData.PageTable.uri, null, "(version = ? AND is_deleted = 0 AND ((userAccount = 0) OR (userAccount = ?)))",
	          new String[] { "3",Long.toString(MetaData.CurUserAccount) }, "is_last_edit DESC");

	     if(cursor != null && cursor.getCount() != 0)
	     {
	        cursor.moveToFirst();
	        while(!cursor.isAfterLast())
	        {
	          if(NoteBookPickerActivity.getIsLock())
	          {
	             long bookid = cursor.getLong(MetaData.PageTable.INDEX_OWNER);

	             if (mBookcase == null) {
	               mBookcase = BookCase.getInstance(this);
	             }

	             NoteBook book = mBookcase.getNoteBook(bookid);

	             if(book == null )//|| (book.getIsLocked())
	             {
	               cursor.moveToNext();
	               continue;
	             }
	          }

	          long returnValue = cursor.getLong(MetaData.PageTable.INDEX_IS_LAST_EDIT);
	          if(returnValue != 0)
	          {
	             pageId = cursor.getLong(MetaData.PageTable.INDEX_CREATED_DATE);
	             bookId = cursor.getLong(MetaData.PageTable.INDEX_OWNER);
	          }
	          else
	          {
	             bookId = 0L;
	             pageId = 0L;
	          }
	          break;
	        }
	        cursor.close();
	     }

	     else
	     {
	        //BEGIN: RICHARD
	        if(cursor != null)
	        {
	          cursor.close();
	        }

	        //END: RICHARD
	        bookId = 0L;
	        pageId = 0L;
	     }

	     if(bookId == 0&&pageId == 0){
	        return false;
	     } 
	     
	     //emmanual to fix bug 458413
	     if (mBookcase == null) {
			mBookcase = BookCase.getInstance(this);
		 }
	     
	     //begin smilefish fix bug 338468
	     NoteBook book = mBookcase.getNoteBook(bookId);
	     if(book.getIsLocked())
	    	 return false;
	     //end smilefish
	     
	     try {
	        Intent intent = new Intent();
	        intent.setClass(this, EditorActivity.class);
	        intent.putExtra(MetaData.BOOK_ID, bookId);
	        intent.putExtra(MetaData.PAGE_ID, pageId);
	        startActivity(intent);
	     } catch (ActivityNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	     }
	     
	     return true; 
	   }
	//End:Dave.
	
	
	
	//begin noah;for share
	public boolean isShareMode(){
		Intent intent = getShareIntent();
		if(intent == null)
			return false;
		return true;
	}
	public Intent getShareIntent(){
		Intent extraIntent = getIntent().getParcelableExtra(Intent.EXTRA_INTENT);
		return extraIntent;
	}
	//end noah;for share
	
	static WidgetInitTask widgetInitTask;//emmanual
	public static void firstRunLoadResource(SharedPreferences preference,Resources resources,Activity activity)
	{
		int flag = MetaData.ONLY_LOAD_CFG_RESOURCE;
		
		if(preference.getBoolean(
				resources.getString(R.string.pref_first_load), true) == true)
		{
			flag = MetaData.LOAD_CFG_RESOURCE_AND_SNE;
		}
		
		widgetInitTask = new WidgetInitTask(activity);
		widgetInitTask.setLoadCFGResource(flag);
		widgetInitTask.execute();
		
        SharedPreferences.Editor editor = preference.edit();

        int recordIndexLaguage = sLanguageHelper.getRecordIndexLaguage();//darwin
        editor.putInt(resources.getString(R.string.pref_index_language), recordIndexLaguage);
        editor.apply();
        editor.commit();
	}
		
	//begin smilefish
	private final OnClickListener cloudWindowClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
            if (mCloudPopupWindow != null && mCloudPopupWindow.isShowing()) {
            	mCloudPopupWindow.dismiss();
            }
            	
            if(v.getId() == R.id.account_sign_out) 
            {
				AlertDialog.Builder builder = null;
				SharedPreferences pref = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, 
						Context.MODE_MULTI_PROCESS);
				String sAsusAccount = pref.getString(getResources().getString(R.string.pref_AsusAccount), null);
				String RAsusKey =  mContext.getResources().getString(R.string.pref_AsusKey);
				String RAsusIV =  mContext.getResources().getString(R.string.pref_AsusIV);
				String keyStr = mPreference.getString(RAsusKey, "");
				String ivStr = mPreference.getString(RAsusIV, "");
				try {
					sAsusAccount = EncryptAES.decrypt(keyStr, ivStr, sAsusAccount);
				} catch (Exception e) {
					e.printStackTrace();
				}
				builder = new AlertDialog.Builder(mContext);

				String title = String.format(getResources().getString(R.string.sync_setting_sign_out), sAsusAccount);
				builder.setTitle(title);
				builder.setMessage(R.string.sync_setting_sign_out_msg);
			   
				builder.setPositiveButton(android.R.string.ok,  new DialogInterface.OnClickListener() {
					 @Override
					 public void onClick(DialogInterface dialog, int which) {
						final SharedPreferences mPreference = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
						final SharedPreferences.Editor mPreferenceEditor = mPreference.edit();
						String RAsusAccount = mContext.getResources().getString(R.string.pref_AsusAccount);
						String RAsusPassword = mContext.getResources().getString(R.string.pref_AsusPassword);
						mPreferenceEditor.putString(RAsusAccount, "");															//shaun
						mPreferenceEditor.putString(RAsusPassword, "");
						mPreferenceEditor.commit();
						updateBookList(null);												
						
						CountDownClass countDown = CountDownClass.getInstance(mContext.getApplicationContext());
						countDown.stopCountDown();
						countDown.StopTask();
						
						updateSyncStatusBar();
						updateFragment(); //smilefish
						updateDrawerNumText(); //Dave

						//emmanual, update widget when sign out 
			        	Intent updateIntent = new Intent();
			    		updateIntent.setAction(MetaData.ACTION_SUPERNOTE_UPDATE);
			    		updateIntent.putExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM, MetaData.SuperNoteUpdateInfoSet);
			    		sendBroadcast(updateIntent);
			    		MetaData.SuperNoteUpdateInfoSet.clear();
					 }
				});
				
				builder.setNegativeButton(android.R.string.cancel,  new DialogInterface.OnClickListener() {
				 @Override
				 public void onClick(DialogInterface dialog, int which) {
					
				 }
				});  	
				AlertDialog dialog = builder.create();
				dialog.show();
            }
            else if(v.getId() == R.id.sync_book_list)
            {
				if (isNetworkConnected(NoteBookPickerActivity.this)) {
					try {
						Intent intent = new Intent(NoteBookPickerActivity.this,
								BookPickerView.class);
						intent.putExtra("intentfrom",
								NoteBookPickerActivity.PICK_OR_BOOK_PICKER);
						// BEGIN: Shane_Wang 2012-10-9
						intent.putExtra("invokeActivity", "NoteBookPickerActivity");
						// END: Shane_Wang 2012-10-9
						NoteBookPickerActivity.this.startActivity(intent);
					} catch (Exception e) {

						e.printStackTrace();
					}

				} else {
					Toast toast = Toast.makeText(NoteBookPickerActivity.this,
							getResources().getString(R.string.sync_setting_networkless), Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
					toast.show();
					
				}
            }
            else if(v.getId() == R.id.account_sync_settings)
            {
				try {
					Intent intent = new Intent();
					intent.setClass(NoteBookPickerActivity.this, SyncSettingActivity.class);
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
		}
		
	};
	
	private void preparePopupWindowCloud(final View view) {
		if(mCloudPopupWindow == null)
		{
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.cloud_account_popup, null, false);
            
            TextView signOutView = (TextView)popupView.findViewById(R.id.account_sign_out);
            TextView booklistView = (TextView)popupView.findViewById(R.id.sync_book_list);
            TextView settingsView = (TextView)popupView.findViewById(R.id.account_sync_settings);
            signOutView.setOnClickListener(cloudWindowClickListener);
            booklistView.setOnClickListener(cloudWindowClickListener);
            settingsView.setOnClickListener(cloudWindowClickListener);
            
            mCloudPopupWindow = new PopupWindow(
                    popupView,
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT,
                    false);
            
            mCloudPopupWindow.setFocusable(true);
            mCloudPopupWindow.setOutsideTouchable(true);
            mCloudPopupWindow.setTouchable(true);
            mCloudPopupWindow.setTouchInterceptor(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                	Log.i("getAction=", event.getAction() + "");
                    if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                        if (mCloudPopupWindow != null && mCloudPopupWindow.isShowing()) {
                        	mCloudPopupWindow.dismiss();
                        }
                        return true;
                    }

                    return false;
                }
            });
		}
		
		mCloudPopupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.spinner_dropdown_background));
		mCloudPopupWindow.showAsDropDown(view,0,0);
	}
	//end smilefish	
	
	// BEGIN: Better
	private final OnClickListener stateBarClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// EBGIN:shaun_xu@asus.com
			if (v.getId() == R.id.sync_book_setting) {
				String RAsusAccount = mPreference.getString(
						mResources.getString(R.string.pref_AsusAccount), null);

				String RAsusPassword = mPreference.getString(
						mResources.getString(R.string.pref_AsusPassword), null);
				if (RAsusAccount != null && !RAsusAccount.equals("")
						&& RAsusPassword != null && !RAsusPassword.equals("")) {
					//begin smilefish
						preparePopupWindowCloud(v);
					//end smilefish
				} else {
					try {
						showLoginDialog(STATEBAR_BOOK_ENABLE,"NoteBookPickerActivity");

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// BEGIN:shaun@asus.com
			} else if (v.getId() == R.id.statebar) {
				String sAsusAccount = mPreference.getString(
						mContext.getString(R.string.pref_AsusAccount), null);
				String sAsusPassword = mPreference.getString(
						mContext.getString(R.string.pref_AsusPassword), null);
				if (sAsusAccount != null && !sAsusAccount.equals("")
						&& sAsusPassword != null && !sAsusPassword.equals("")) {
					if (isNetworkConnected(NoteBookPickerActivity.this)) {

						mCountDown = CountDownClass
								.getInstance(getApplicationContext());
						//begin darwin
						mCountDown.SetActivity((Activity)NoteBookPickerActivity.this);
						//end   darwin
						String autosync = mPreference.getString(mContext.getResources().getString(R.string.pref_asus_auto_sync), MetaData.DEFAULT_SYNC_MODE);
						if (autosync.equalsIgnoreCase("auto")) {
							int interval = mPreference.getInt(mContext.getString(R.string.pref_default_sync_time), 
									MetaData.DEFAULT_SYNC_TIME);
							mCountDown.StartCountDown(interval, true, false);
						} else {
							mCountDown.StartTask(false);
						}

					} else {
						Toast toast = Toast.makeText(NoteBookPickerActivity.this,
								getResources().getString(R.string.sync_setting_networkless), Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
						toast.show();
					}
				} 
			}
			// END:shaun_xu@asus.com
		}

	};

	// END: Better
	
	//begin  darwin
	private AutoCompleteTextView account;
	private TextView show; 
	private EditText password; 
	private ArrayList<String> accountlist;
	public static final int NETWORK_UNAVAIABLE = 3;
	private int mIntentFrom;
	private String mInvokeActivity;
	public static int LOGIN_FAILED = 1;
	public static int LOGIN_SUCCESS = 2;	
	private static boolean mIsLoginTaskRunning = false; // Better
	AlertDialog mLoninDialog = null;
	Boolean bIsLoginShowing = false;
	
	public void showLoginDialog(final int intentfrom,final String invokeActivity){
		//display a dialog for the permission of Internet connection [Carol]
		if(SystemProperties.get("ro.build.asus.sku").equals("CN")){
			Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(
			        R.string.cta_act_mobile_network));
			builder.setMessage(getResources().getString(
			        R.string.cta_msg_mobile_network));
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setNegativeButton(android.R.string.cancel, null);
			final AlertDialog dialog = builder.create();
			dialog.show();
			dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
							showLoginWebstorageDialog(intentfrom, invokeActivity);
						}
					});
			dialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
		}else{
			showLoginWebstorageDialog(intentfrom, invokeActivity);
		}
	}
	
	private void showLoginWebstorageDialog(int intentfrom,String invokeActivity)
	{
		if(MetaData.isATT() || bIsLoginShowing)
		{
			return ;
		}
		bIsLoginShowing = true;
		Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.webstoragesignon, null, false);
        if (MetaData.AppContext == null) {
    		MetaData.AppContext = getApplicationContext();
		}
        
        
        Intent intent = this.getIntent();
        mIntentFrom = intentfrom;

        mInvokeActivity = invokeActivity;

        show = (TextView)dialogView.findViewById(R.id.webstorage_info);//By Show
        account = (AutoCompleteTextView)dialogView.findViewById(R.id.webstorage_account);
        TextViewUtils.enableCapSentences(account);
        password = (EditText)dialogView.findViewById(R.id.webstorage_password);
        getAccountList();
        account.setAdapter(new ArrayAdapter<String>(this,R.layout.account_prompt_text,accountlist));
                 //android.R.layout.simple_dropdown_item_1line, accountlist));
        //'R.layout.account_prompt_text' is based on 'android.R.layout.simple_dropdown_item_1line', change
        // the text color only - Carol
        
        builder.setView(dialogView);

        builder.setNeutralButton(R.string.webstorage_register, null); //smilefish
        builder.setPositiveButton(android.R.string.ok, null);
      	builder.setNegativeButton(android.R.string.cancel, null);
      	mLoninDialog = builder.create();
      	DialogUtils.forcePopupSoftInput(mLoninDialog);//noah
        account.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(account.getText().length() == 0)
					account.showDropDown();
			}
        	
        });
        
        //begin smilefish
        if(mIsSettingWifi){
        	account.setText(mLastAccount); 
        	password.requestFocus();
        	mIsSettingWifi = false;
        }
        //end smilefish
        
         //BEGIN: Show
        
        mLoninDialog.setCanceledOnTouchOutside(false);
        mLoninDialog.setOnDismissListener(null);
        mLoninDialog.setTitle(mContext.getResources().getString(R.string.webstorage_signon));
		if(NoteBookPickerActivity.this != null && !NoteBookPickerActivity.this.isFinishing()){//TT 534281
			mLoninDialog.show();
		}else{
			return; //smilefish fix google play bug
		}
        mLoninDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
									
					String sAccount = account.getText().toString();
					String sPassword = password.getText().toString();
					mLastAccount = sAccount;
					if(NoteBookPickerActivity.isNetworkConnected(mContext))
					{
						if (!sAccount.equals("") && !sPassword.equals("")) 
						{
							password.setEnabled(false);
							account.setEnabled(false);
							show.setVisibility(View.VISIBLE);
							show.setText(R.string.webstorage_login_signing_in);//By Show
							new LoginWorkTask().execute(sAccount, sPassword);
							//emmanual to fix bug 437822
							mLoninDialog.getButton(Dialog.BUTTON_POSITIVE).setClickable(false);
						}
						else
						{
							Toast pwToast = Toast.makeText(mContext, R.string.password_or_account_empty, Toast.LENGTH_LONG);
							pwToast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
							pwToast.show();
						}
					}
					else
					{
						Intent intent = new Intent(MetaData.LOGIN_MESSAGE);
						intent.putExtra(MetaData.LOGIN_RESULT, NETWORK_UNAVAIABLE);
						intent.putExtra(MetaData.LOGIN_ACCOUNT, "");
						intent.putExtra(MetaData.LOGIN_USER_SPACE, "");
						intent.putExtra(MetaData.LOGIN_INTENT_FROM,
								NoteBookPickerActivity.LOGIN);
						// BEGIN: Shane_Wang 2012-10-16
						intent.putExtra("invokeActivity", mInvokeActivity);
						// END: Shane_Wang 2012-10-16
						sendBroadcast(intent);
						
						mLoninDialog.dismiss();
					}
								
			}
        });
        mLoninDialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mLoninDialog.dismiss();
			}				
        	
        }
        );
        mLoninDialog.getButton(Dialog.BUTTON_NEUTRAL).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("https://www.asuswebstorage.com/navigate/reg");
				try {
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}				      	
        }
        );
        mLoninDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				
				bIsLoginShowing = false;
			}
		});
	}
	
	private void getAccountList()
	{
		if(accountlist==null)
		accountlist = new ArrayList<String>();
		else
			accountlist.clear();
		
		Cursor cur = mContext.getContentResolver().query(MetaData.WebAccountTable.uri, null, null, null, null);
		if(cur.getCount()>0)
		{
			cur.moveToFirst();
			while(!cur.isAfterLast()){
			String account = cur.getString(MetaData.WebAccountTable.INDEX_ACCOUNT);
			accountlist.add(account);
			cur.moveToNext();
			
			}
		}
		cur.close();
		
	}
	// BEGIN: Shane_Wang 2012-10-30
	public static boolean getIsLoginTaskRunning() {
		return mIsLoginTaskRunning;
	}
	// END: Shane_Wang 2012-10-30
	public class LoginWorkTask extends AsyncTask<String, Integer, Integer> {
		
		public static final String TAG = "LoginWorkTask";
		private String sAccount;
		private String sPassword;
		private String usedSpace;
		private String ToastAccount;

		public LoginWorkTask() {
			
				
		}

		@Override
		protected void onProgressUpdate (Integer... values)
		{
		}
		
		@Override
		protected synchronized Integer doInBackground(String... params) {
			mIsLoginTaskRunning = true; // Better
	
			Log.v(TAG, "Login error, try to login...");
			ToastAccount = params[0];
			try {
				publishProgress(50);
				
				boolean useNewFolder = false;
				if (MetaData.IS_ENABLE_WEBSTORAGE_DATA_MIGRATING) {
					Cursor cursor = mContext.getContentResolver().query(MetaData.WebAccountTable.uri, null, "account_name = ?", new String[]{params[0]}, null);
			    	if (cursor != null) {
						if(cursor.getCount() > 0) {
							cursor.moveToFirst();
							if (!cursor.isAfterLast()) {
								int index = cursor.getColumnIndex(MetaData.WebAccountTable.ID);
								if (index >= 0) {
									long id = cursor.getLong(index);
									File file = new File(MetaData.USER_TO_UPGRAGE_LIST_DIR, Long.toString(id));
									useNewFolder = !file.exists();
								}
							}   		
				    	}
						cursor.close();
			    	}
				}
				
				if (MetaData.webStorage.init(params[0],params[1], useNewFolder)){
					sAccount = params[0];
					sPassword = params[1];							    				
				}
				//BEGIN: Show
				 usedSpace = MetaData.webStorage.getMemberUsedCapacity();
				//END: Show
				
				 mIsLoginTaskRunning = false; // Better
			} 
			catch(WebStorageException ee)
			{
//				Log.v(TAG, "Exception: " + ee.toString()
//						+ " occurred, Message: " + ee.getMessage());
				
				mIsLoginTaskRunning = false; // Better
				
				return ee.getErrorKind();
			}
			catch (Exception e) {
				Log.v(TAG, "Exception: " + e.toString()
						+ " occurred, Message: " + e.getMessage());
				
				mIsLoginTaskRunning = false; // Better
				
				return -100;
			}
			return 100;
		}
		
		@Override
		protected void onPreExecute() {
			
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			//emmanual to fix bug 451558
			mLoninDialog.getButton(Dialog.BUTTON_POSITIVE).setClickable(true);
			
			password.setEnabled(true); //smilefish fix bug 700318
			account.setEnabled(true);
			show.setVisibility(View.GONE);//By Show
			if(result == 100) {
				updateBookList(sAccount);

				final SharedPreferences mPreference = mContext
						.getSharedPreferences(MetaData.PREFERENCE_NAME,
								Context.MODE_MULTI_PROCESS);
				final SharedPreferences.Editor mPreferenceEditor = mPreference
						.edit();

				byte[] iv = new byte[256 / 16];
				SecureRandom sr = new SecureRandom();
				sr.nextBytes(iv);

				String sKey = null;
				try {
					KeyGenerator keyGen = KeyGenerator.getInstance("AES");
					keyGen.init(256,new SecureRandom());
					SecretKey secretKey = keyGen.generateKey();

					sAccount = EncryptAES.encrypt(secretKey, iv, sAccount);
					sPassword = EncryptAES.encrypt(secretKey, iv, sPassword);

					sKey = Base64.encodeToString(secretKey.getEncoded(), Base64.NO_WRAP);
				} catch (Exception e) {
				
				}

				String RAsusAccount = mContext.getResources().getString(
						R.string.pref_AsusAccount);
				String RAsusPassword = mContext.getResources().getString(
						R.string.pref_AsusPassword);
				String RAsusKey =  mContext.getResources().getString(
						R.string.pref_AsusKey);
				String RAsusIV =  mContext.getResources().getString(
						R.string.pref_AsusIV);

				mPreferenceEditor.putString(RAsusAccount, sAccount);
				mPreferenceEditor.putString(RAsusPassword, sPassword);
				mPreferenceEditor.putString(RAsusKey, sKey);
				String sIV = Base64.encodeToString(iv, Base64.NO_WRAP);
				mPreferenceEditor.putString(RAsusIV, sIV);
				mPreferenceEditor.putString(mContext.getResources().getString(R.string.pref_Asus_AccountUsed), 
	            		usedSpace);
				mPreferenceEditor.commit();
				
				// BEGIN: Better
				CountDownClass countDown = CountDownClass.getInstance(mContext);
				String autosync = mPreference.getString(mContext.getResources().getString(R.string.pref_asus_auto_sync), MetaData.DEFAULT_SYNC_MODE);
				if (autosync.equalsIgnoreCase("auto")) {
					int interval = mPreference.getInt(mContext.getString(R.string.pref_default_sync_time), 
							MetaData.DEFAULT_SYNC_TIME);
    	            countDown.StartCountDown(interval, true, true);
				} else {
					countDown.StartTask(true);
				}
				// END: Better
				
				Intent intent = new Intent(MetaData.LOGIN_MESSAGE);
				intent.putExtra(MetaData.LOGIN_RESULT, LOGIN_SUCCESS);
				intent.putExtra(MetaData.LOGIN_ACCOUNT, ToastAccount);
				intent.putExtra(MetaData.LOGIN_USER_SPACE, usedSpace);
				intent.putExtra(MetaData.LOGIN_INTENT_FROM, NoteBookPickerActivity.LOGIN);
				// BEGIN: Shane_Wang 2012-10-9
				intent.putExtra("invokeActivity", mInvokeActivity);
				// END: Shane_Wang 2012-10-9
				NoteBookPickerActivity.this.sendBroadcast(intent);
				
				if(mIntentFrom == NoteBookPickerActivity.STATEBAR_BOOK_ENABLE)
				{
					Intent NewIntent = new Intent(NoteBookPickerActivity.this,BookPickerView.class);
					NewIntent.putExtra(MetaData.LOGIN_INTENT_FROM, NoteBookPickerActivity.LOGIN);
					// BEGIN: Shane_Wang 2012-10-9
					intent.putExtra("invokeActivity", mInvokeActivity);
					// END: Shane_Wang 2012-10-9
					NewIntent.putExtra(MetaData.LOGIN_ACCOUNT, ToastAccount);
					NoteBookPickerActivity.this.startActivity(NewIntent);
				}

				mLoninDialog.dismiss();
				//END:shaun_xu@asus.com				
			} else {
				final SharedPreferences mPreference = mContext
						.getSharedPreferences(MetaData.PREFERENCE_NAME,
								Context.MODE_MULTI_PROCESS);
				final SharedPreferences.Editor mPreferenceEditor = mPreference
						.edit();

				String RAsusAccount = mContext.getResources().getString(
						R.string.pref_AsusAccount);
				String RAsusPassword = mContext.getResources().getString(
						R.string.pref_AsusPassword);
				mPreferenceEditor.putString(RAsusAccount, "");
				mPreferenceEditor.putString(RAsusPassword, "");
				mPreferenceEditor.putString(mContext.getResources().getString(R.string.pref_Asus_AccountUsed), 
						"0MB of 0GB");
				mPreferenceEditor.commit();
				
				if(result == WebStorageException.LOGIN_ERROR)
				{
					Toast toast = Toast.makeText(mContext, R.string.webstorage_login_account_password_wrong, Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
					toast.show();
					
					Intent intent = new Intent(MetaData.LOGIN_MESSAGE);
					intent.putExtra(MetaData.LOGIN_RESULT, LOGIN_FAILED);
					intent.putExtra(MetaData.LOGIN_ACCOUNT, "");
					intent.putExtra(MetaData.LOGIN_USER_SPACE, "0MB of 0GB");
					intent.putExtra(MetaData.LOGIN_INTENT_FROM, NoteBookPickerActivity.LOGIN);
					// BEGIN: Shane_Wang 2012-10-9
					intent.putExtra("invokeActivity", mInvokeActivity);
					// END: Shane_Wang 2012-10-9
					NoteBookPickerActivity.this.sendBroadcast(intent);
					
					updateBookList(null);
				}
				else
				{
					Intent intent = new Intent(MetaData.LOGIN_MESSAGE);
					intent.putExtra(MetaData.LOGIN_RESULT, LOGIN_FAILED);
					intent.putExtra(MetaData.LOGIN_ACCOUNT, ToastAccount);
					intent.putExtra(MetaData.LOGIN_USER_SPACE, "0MB of 0GB");
					intent.putExtra(MetaData.LOGIN_INTENT_FROM, NoteBookPickerActivity.LOGIN);
					// BEGIN: Shane_Wang 2012-10-9
					intent.putExtra("invokeActivity", mInvokeActivity);
					// END: Shane_Wang 2012-10-9
					NoteBookPickerActivity.this.sendBroadcast(intent);
					
					updateBookList(null);
					mLoninDialog.dismiss();
				}
			}
			
		}
		
	}
	private void updateBookList(String accountName)
	{
		Long id = 0L;
		if (accountName != null) {
			// add by wendy webaccount begin
			Cursor cursor = mContext.getContentResolver().query(
					MetaData.WebAccountTable.uri, null, "account_name = ?",
					new String[] { accountName }, null);

			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				id = cursor.getLong(MetaData.WebAccountTable.INDEX_ID);
				// update Picker Activity
			} else {
				id = System.currentTimeMillis();
				ContentValues cvs = new ContentValues();
				cvs.put(MetaData.WebAccountTable.ID, id);
				cvs.put(MetaData.WebAccountTable.ACCOUNT_NAME, accountName);
				cvs.put(MetaData.WebAccountTable.ACCOUNT_PASSWORD,
                    "");
				mContext.getContentResolver().insert(
						MetaData.WebAccountTable.uri, cvs);
			}
			cursor.close();
			// add by wendy webaccount end
		}		
		MetaData.CurUserAccount = id;
	}
	//end darwin
	
	// BEGIN:shaun_xu@asus.com
	public static boolean isNetworkConnected(Context context) {

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = cm.getActiveNetworkInfo();
		if (network != null) {
			return network.isAvailable();
		}
		return false;
	}

	// END:shaun_xu@asus.com
	
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if(mDrawerToggle != null)
        	mDrawerToggle.syncState();
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		//begin darwin
		if(NoteBookPickerActivity.getConfigurationChangeBy() > NoteBookPickerActivity.CONFIGURATIONCHANGE_BY_NOTEBOOKPICKER)
		{
			NoteBookPickerActivity.setConfigurationChangeBy(NoteBookPickerActivity.CONFIGURATIONCHANGE_BY_NOTEBOOKPICKER);
			if (mFragmentManager == null) {
	            mFragmentManager = getFragmentManager();
	        }
			mFragmentTransaction = mFragmentManager.beginTransaction();

			if (mFavoritesPageFragment != null) {
				mFragmentTransaction.remove(mFavoritesPageFragment);
			}
			

			if (mAllPageViewFragment != null) {
				mFragmentTransaction.remove(mAllPageViewFragment);
			}
			

			if (mTimestampViewFragment != null) {
				mFragmentTransaction.remove(mTimestampViewFragment);
			}
			

			if (mPageGridFragment != null) {
				mFragmentTransaction.remove(mPageGridFragment);
			}
			mFragmentTransaction.commit();
			
			//Begin Dave. Fix the Bug: improve the launch speed at first time.
			mIsFirstLoad = false; 
			mHasCreated = false; 
			//End Dave.
		}
		else
		{
			NoteBookPickerActivity.setConfigurationChangeBy(NoteBookPickerActivity.CONFIGURATIONCHANGE_BY_NOTEBOOKPICKER);
		//end   darwin

        // Pass any configuration change to the drawer toggls
		if(mDrawerToggle != null){//emmanual to fix bug 389363
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
        
		setContentView(R.layout.notebookpicker);
		//ColorfulStatusActionBarHelper.setContentView(R.layout.notebookpicker, true, this);
        
		mFragmentManager = getFragmentManager();
		mLeftSideLayout = (FrameLayout) findViewById(R.id.left_side);
		mLeftSideLayout.setOnDragListener(dragListener);

		int orignalDisplayType = DISPLAY_ALL;
		orignalDisplayType = mPreference.getInt(
				mResources.getString(R.string.pref_picker_display_type),
				DISPLAY_ALL);
		
		//Begin Dave. Fix the Bug:screen rotation may cause incorrect display mode 
		if(mDisplayType != DISPLAY_NONE && mDisplayType != DISPLAY_PAGE)
			orignalDisplayType = mDisplayType;
		//End Dave.
			
		setDisplayType(orignalDisplayType);
		
		mStateBar = (View) this.findViewById(R.id.statebar);
		View viewimage = (View) this.findViewById(R.id.sync_book_setting);
		if(!MetaData.isATT()){ //Carol
			mStateBar.setOnClickListener(stateBarClickListener);
			viewimage.setOnClickListener(stateBarClickListener);	
		}else{
			mStateBar.setVisibility(View.GONE);
			viewimage.setVisibility(View.GONE);
		}
		
		initDrawerLayout(); //Dave
		
		if(mbook_actionbar == null)
		{
			mbook_actionbar = (LinearLayout) View.inflate(
					NoteBookPickerActivity.this, R.layout.book_actionbar, null);// wendy
		}

		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

		detachFragment(fragmentTransaction);
		reAttachFragment(fragmentTransaction);
		fragmentTransaction.commit();
		// BEGIN: Better
		updateSyncStatusBar();
		// END: Better
		showAddBookDialog(false, true);
		showEditCoverDialog();//smilefish
		}
		
		//emmanual to fix bug 529510
		if (mDrawerLayout != null) {
			if (isShareMode()) {
				if(mCurrentDrawerIndex != DISPLAY_ALL) //smilefish fix bug 556675
					selectItem(DISPLAY_ALL);
				setDrawerEnabled(false);
			} else {
				setDrawerEnabled(true);
			}
		}
		
        //smilefish fix bug 608207
        if (mCloudPopupWindow != null && mCloudPopupWindow.isShowing()) {
        	mCloudPopupWindow.dismiss();
        }
	}

	public void detachFragment(FragmentTransaction fragmentTransaction) {
		mFragmentManager = getFragmentManager();
		switch (/* mDisplayTypeSelector.getSelectedItemPosition() */mDisplayType) {
		case DISPLAY_ALL:
			mPageGridFragment = (NoteBookPageGridFragment) mFragmentManager
					.findFragmentByTag(NoteBookPageGridFragment.TAG);

			if (mPageGridFragment != null) {
				fragmentTransaction.detach(mPageGridFragment);//Modified by show
			}
			break;
			//begin darwin
		case DISPLAY_ALL_PAGE:
			mAllPageViewFragment = (AllPageViewFragment) mFragmentManager
					.findFragmentByTag(AllPageViewFragment.TAG);
			if (mAllPageViewFragment != null) {
				fragmentTransaction.detach(mAllPageViewFragment);//Modified by show
			}
			break;
			//end  darwin
		// begin wendy
		case DISPLAY_TIMESTAMP:
			mTimestampViewFragment = (TimeStampViewFragment) mFragmentManager
					.findFragmentByTag(TimeStampViewFragment.TAG);
			if (mTimestampViewFragment != null) {
				fragmentTransaction.detach(mTimestampViewFragment);//Modified by show
			}
			break;
		// end wendy
		case DISPLAY_BOOKMARK:
			mFavoritesPageFragment = (AllPageViewFragment) mFragmentManager
					.findFragmentByTag(FavoritesPageFragment.TAG);
			if (mFavoritesPageFragment != null) {
				fragmentTransaction.detach(mFavoritesPageFragment);//Modified by show
			}
			break;
		}
	}

	public void reAttachFragment(FragmentTransaction fragmentTransaction) {
		switch (mCurrentDisplayIndex) { //Dave
		case DISPLAY_ALL:
			if (mPageGridFragment != null) {
				fragmentTransaction.attach(mPageGridFragment);//Modified by show
			}
			mLeftSideLayout.setVisibility(View.VISIBLE);
			break;
			//begin darwin
		case DISPLAY_ALL_PAGE:
			if (mAllPageViewFragment != null) {
				fragmentTransaction.attach(mAllPageViewFragment);//Modified by show
			}

			break;
			//end darwin
		// begin wendy
		case DISPLAY_TIMESTAMP:
			if (mTimestampViewFragment != null) {
				fragmentTransaction.attach(mTimestampViewFragment);//Modified by show
			}
			break;
		// end wendy
		case DISPLAY_BOOKMARK:
			if (mFavoritesPageFragment != null) {
				fragmentTransaction.attach(mFavoritesPageFragment);//Modified by show
			}

			break;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onPause() {
		if ( mEditBookCoverDialog != null && mEditBookCoverDialog.isShowing() ) 
		{
			DialogUtils.hideSoftInput(mEditBookCoverEditText); //smilefish fix bug 533949/532183
		}
		
		deviceType = PickerUtility.getDeviceType(mContext);
        //Begin: show_wang@asus.com
        //Modified reason: for multi-dpi
		int IsOrientationAllowOrNot = this.getResources().getInteger(R.integer.is_orientation_allow); //by show
        if (IsOrientationAllowOrNot == 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        else  if (IsOrientationAllowOrNot ==1){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
		 else  {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //End: show_wang@asus.com
        
      //Begin Allen for update widget
        if(MetaData.SuperNoteUpdateInfoSet.size()>0){
        	Intent updateIntent = new Intent();
    		updateIntent.setAction(MetaData.ACTION_SUPERNOTE_UPDATE);
    		updateIntent.putExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM, MetaData.SuperNoteUpdateInfoSet);
    		sendBroadcast(updateIntent);
    		MetaData.SuperNoteUpdateInfoSet.clear();
        }
        MetaData.mCacheCoverThumbnailList.clear(); //delete all thumbnail cache[Carol]
        //End Allen
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// BEGIN: Better
		if (!mIsFirstLoad && mHasCreated) { //Dave. Fix the Bug: improve the launch speed at first time.
			//Emmanual. Add try-catch to fix 375968, avoid IllegalArgumentException.
			try { 
				unregisterReceiver(updateUiReceiver);
			} catch (IllegalArgumentException e) {  
			
			}
			
			mPreferenceEditor.putBoolean("activity_created", 
					false).commit();
			
	        //BEGIN: RICHARD
			if(mIndexServiceClient != null)//darwin
			{
				mIndexServiceClient.doUnbindService(this);
			}
	        //END: RICHARD
		}
		// END: Better	

        if(mEditBookCoverDialog != null)
            mEditBookCoverDialog.dismiss();
		
		//begin smilefish fix bug 324007
		if(mExport2PdfTask != null && mIsExport2PdfTaskRunning){
			mIsExportPdfWhenDDS = true;
		}
		if(mExportTask != null && mIsExportTaskRunning){
			mIsExportWhenDDS = true;
		}
		if(mImportTask != null && mIsImportTasRunning){
			mIsImportWhenDDS = true;
		}
		//end smilefish
		
		//+++ Dave fix bug 357919
		if(mLoninDialog != null)
		{
			mLoninDialog.dismiss();
		}
		//---
		//emmanual to fix bug 417030
		mHasCreated =false;
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		final SharedPreferences mPreference = this.getSharedPreferences(
				MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS); // shaun
		
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
       if (mDrawerToggle.onOptionsItemSelected(item)) {
           return true;
       }
       
		switch (item.getItemId()) {
		case R.id.menu_set:
			try {
				intent = new Intent();
				intent.setClass(this, SettingActivity.class);
				startActivity(intent);
			} catch (ActivityNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			return true;
		case R.id.menu_search: //add by smilefish
            try {
				intent = new Intent();
				intent.setClass(this, TextSearchActivity.class);
				//begin noah;for share
				if(isShareMode()){
					intent.putExtra(Intent.EXTRA_INTENT, getShareIntent());
					TextSearchActivity.mShareActivity = this;
				}
				startActivity(intent);
				//end noah;for share
			} catch (ActivityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		//Begin Allen
		case R.id.menu_lock:
		{
			String sUnlock = mContext.getString(R.string.nb_unhide);

			if (mPreference.getBoolean(
					mResources.getString(R.string.pref_has_password),
					false) == false) 
			{
				setupPassword();
			} else 
			{
				if (mislocked) {
					inputPassword();
				} else {
					mislocked = true;
					mPreferenceEditor.putBoolean(
							mResources.getString(R.string.lock_state),
							mislocked);
					mPreferenceEditor.commit();

					if (mislocked) {
						// modify current book id
						NoteBook notebook = mBookcase.getCurrentBook();
						if(notebook == null)
							mBookcase.setCurrentBook(BookCase.NO_SELETED_BOOK);
						else if ( notebook.getIsLocked()) {
							long unlockedbookid = mBookcase
									.getNextUnlockedBookID(true);
							if (unlockedbookid == 0) {
								mBookcase
										.setCurrentBook(BookCase.NO_SELETED_BOOK);
							} else {
								mBookcase
										.setCurrentBook(unlockedbookid);
							}
						}
					}
					item.setTitle(sUnlock);
					updateFragment();
					Toast.makeText(NoteBookPickerActivity.this,getResources().getString(R.string.hide_locked_notebook_info) , Toast.LENGTH_LONG).show();//by show

					//Begin by Emmanual
					if(MetaData.IS_GA_ON)
					{
						GACollector gaCollector = new GACollector(mContext);
						gaCollector.hideLockedBooks();
					}
					//End
				}
			}
		}
			break;
		//End Allen
		case R.id.menu_page_import:
			try {
				intent = new Intent(Intent.ACTION_GET_CONTENT);						
				intent.setType("application/file");
				intent.putExtra("ext", IMPORT_FILE_FILTER);
				intent.putExtra("mime_filter", false);
				intent.putExtra("path", mPreference.getString(
						mResources.getString(R.string.pref_export_dir),
						MetaData.EXPORT_DIR));
				startActivityForResult(Intent.createChooser(intent, ""), REQUEST_IMPORT);
				return true;
				//begin:clare  :if asus file manager exists,then use it ,else list all other file manager avaiable
			} catch (ActivityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				try {
					intent = new Intent(Intent.ACTION_GET_CONTENT  );			         	               	   
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);				
					intent.addCategory("android.intent.category.DEFAULT");								
					intent.setType("application/file");
					
					intent.putExtra("ext", IMPORT_FILE_FILTER);
					intent.putExtra("mime_filter", false);
					intent.putExtra("path", mPreference.getString(
							mResources.getString(R.string.pref_export_dir),
							MetaData.EXPORT_DIR));
					
					  startActivityForResult(intent, REQUEST_IMPORT);
				} catch (ActivityNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					Toast.makeText(mContext,R.string.import_export_book_fail_reason, Toast.LENGTH_SHORT).show();
					return false;
				}
				
				  return true;
			} 
			//end:clare

		case R.id.menu_sort:
			showDialog(SORT_TYPE_DIALOG);
			return true;
		// end emmanual
		case R.id.menu_encourageUs:
		{
			Fragment currentFragment = null;
			switch(mDisplayType){
			case DISPLAY_ALL:
				currentFragment = mPageGridFragment;
				break;
			case DISPLAY_BOOKMARK:
				currentFragment = mFavoritesPageFragment;
				break;
			case DISPLAY_TIMESTAMP:
				currentFragment = mTimestampViewFragment;
				break;
			case DISPLAY_ALL_PAGE:
				currentFragment = mAllPageViewFragment;
				break;
			default:
				currentFragment = mPageGridFragment;
			}
			if(currentFragment == null) return false;
			FragmentManager fm = currentFragment.getChildFragmentManager();
        	FragmentTransaction ft = fm.beginTransaction();
        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        	SuperNoteDialogFragment encourageUsFragment = (SuperNoteDialogFragment) fm
    				.findFragmentByTag(SuperNoteDialogFragment.TAG);
    		if (encourageUsFragment != null) {
    			ft.remove(encourageUsFragment);
    		}
    		ft.addToBackStack(null);

    		encourageUsFragment = SuperNoteDialogFragment.newDialog(SuperNoteDialogFragment.DIALOG_TYPE.ENCOURAGE_US);
    		encourageUsFragment.show(ft, SuperNoteDialogFragment.TAG);
    		return true;
    	}
		case R.id.menu_userVoice:
			showUserVoice(); //show 'UserVoice' page
			return true;
		case R.id.menu_zenFamily:
			ZenUiFamily.setGAEnable(GACollector.getEnableStatus());
			ZenUiFamily.launchZenUiFamily(mContext);
			return true;
		}
		return false;
	}
	
	private boolean importPdfAsBook(){
		Intent intent = null;
		try {
			intent  = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("application/pdf");
			intent.putExtra("ext", IMPORT_PDF_FILTER);
			intent.putExtra("mime_filter", false);
			intent.putExtra("path", mPreference.getString(
			        mResources.getString(R.string.pref_export_dir),
			        MetaData.EXPORT_DIR));
			startActivityForResult(Intent.createChooser(intent, ""),
			        REQUEST_IMPORT_PDF);
			return true;
		} catch (ActivityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			try {
				intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addCategory("android.intent.category.DEFAULT");
				intent.setType("application/pdf");

				intent.putExtra("ext", IMPORT_PDF_FILTER);
				intent.putExtra("mime_filter", false);
				intent.putExtra("path", mPreference.getString(
				        mResources.getString(R.string.pref_export_dir),
				        MetaData.EXPORT_DIR));

				startActivityForResult(intent, REQUEST_IMPORT_PDF);
			} catch (ActivityNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				Toast.makeText(mContext,
				        R.string.import_export_book_fail_reason, Toast.LENGTH_SHORT)
				        .show();
				return false;
			}

			return true;
		}
	}
	
	private void showUserVoice() {
		// TODO Auto-generated method stub
		UserVoiceConfig.init(mContext);
	}
	
	private void displayDefaultType() {		
		//+++ James5_Chan, Fix A66, TT-231170
        if (mFragmentManager == null) {
            mFragmentManager = getFragmentManager();
        }
        //---

		mFragmentTransaction = mFragmentManager.beginTransaction();
		mFavoritesPageFragment = (AllPageViewFragment) mFragmentManager
				.findFragmentByTag(FavoritesPageFragment.TAG);
		if (mFavoritesPageFragment != null) {
			mFragmentTransaction.remove(mFavoritesPageFragment);
		}
		
		mAllPageViewFragment = (AllPageViewFragment) mFragmentManager
				.findFragmentByTag(FavoritesPageFragment.TAG);
		if (mAllPageViewFragment != null) {
			mFragmentTransaction.remove(mAllPageViewFragment);
		}
		
		mTimestampViewFragment = (TimeStampViewFragment) mFragmentManager
				.findFragmentByTag(TimeStampViewFragment.TAG);
		if (mTimestampViewFragment != null) {
			mFragmentTransaction.remove(mTimestampViewFragment);
		}

			mLeftSideLayout.setVisibility(View.VISIBLE);
			mPageGridFragment = (NoteBookPageGridFragment) mFragmentManager
					.findFragmentByTag(NoteBookPageGridFragment.TAG);
			if (mPageGridFragment == null) {
				mPageGridFragment = new NoteBookPageGridFragment();
				mFragmentTransaction.replace(R.id.left_side,
						mPageGridFragment, NoteBookPageGridFragment.TAG);
			}

		mFragmentTransaction.commit();

		mDisplayType = DISPLAY_ALL;
		mPreferenceEditor.putInt(
				mResources.getString(R.string.pref_picker_display_type),
				mDisplayType);
		mPreferenceEditor.commit();
	}

	private void displayAllPageView() {
		mLeftSideLayout.setVisibility(View.VISIBLE);
		mFragmentTransaction = mFragmentManager.beginTransaction();
		mPageGridFragment = (NoteBookPageGridFragment) mFragmentManager
				.findFragmentByTag(NoteBookPageGridFragment.TAG);
		if (mPageGridFragment != null) {
			mFragmentTransaction.remove(mPageGridFragment);
		}
		mAllPageViewFragment = (AllPageViewFragment) mFragmentManager
				.findFragmentByTag(AllPageViewFragment.TAG);
		if (mAllPageViewFragment == null) {
			mAllPageViewFragment = new AllPageViewFragment();
			mFragmentTransaction.replace(R.id.left_side, mAllPageViewFragment,
					AllPageViewFragment.TAG);
		}
		mAllPageViewFragment.onDoResume();//darwin
		mFragmentTransaction.commit();
		//begin darwin
		mDisplayType = DISPLAY_ALL_PAGE;
		mPreferenceEditor.putInt(
				mResources.getString(R.string.pref_picker_display_type),
				DISPLAY_ALL_PAGE);
		//end darwin
		mPreferenceEditor.commit();
	}

	private void displayBookmarkOnly() {
		mLeftSideLayout.setVisibility(View.VISIBLE);
		mFragmentTransaction = mFragmentManager.beginTransaction();
		mPageGridFragment = (NoteBookPageGridFragment) mFragmentManager
				.findFragmentByTag(NoteBookPageGridFragment.TAG);
		if (mPageGridFragment != null) {
			mFragmentTransaction.remove(mPageGridFragment);
		}
		mFavoritesPageFragment = (AllPageViewFragment) mFragmentManager
				.findFragmentByTag(FavoritesPageFragment.TAG);
		if (mFavoritesPageFragment == null) {
			mFavoritesPageFragment = new AllPageViewFragment();
			mFragmentTransaction.replace(R.id.left_side,
					mFavoritesPageFragment, FavoritesPageFragment.TAG);
		}
		mFavoritesPageFragment.onDoResume();//darwin
		mFragmentTransaction.commit();
		mDisplayType = DISPLAY_BOOKMARK;
		mPreferenceEditor.putInt(
				mResources.getString(R.string.pref_picker_display_type),
				mDisplayType);
		mPreferenceEditor.commit();
	}

	// add by wendy begin
	private void displayTimeStampOnly() {
		mLeftSideLayout.setVisibility(View.VISIBLE);
		mFragmentTransaction = mFragmentManager.beginTransaction();
		mPageGridFragment = (NoteBookPageGridFragment) mFragmentManager
				.findFragmentByTag(NoteBookPageGridFragment.TAG);
		if (mPageGridFragment != null) {
			mFragmentTransaction.remove(mPageGridFragment);
		}
		mTimestampViewFragment = (TimeStampViewFragment) mFragmentManager
				.findFragmentByTag(TimeStampViewFragment.TAG);
		if (mTimestampViewFragment == null) {
			mTimestampViewFragment = new TimeStampViewFragment();
			mFragmentTransaction.replace(R.id.left_side,
					mTimestampViewFragment, TimeStampViewFragment.TAG);
		}
		mFragmentTransaction.commit();
		mDisplayType = DISPLAY_TIMESTAMP;
		mPreferenceEditor.putInt(
				mResources.getString(R.string.pref_picker_display_type),
				mDisplayType);
		mPreferenceEditor.commit();

	}

	public void displayPageGridView(Long bookId, boolean isPrivate) {
		mLeftSideLayout.setVisibility(View.VISIBLE);

		mDisplayType = DISPLAY_PAGE;
	}

	public int getDeviceType()
	{
		return deviceType;
	}
	private final OnDragListener dragListener = new OnDragListener() {
		@Override
		public boolean onDrag(View v, DragEvent event) {
			if (event.getAction() == DragEvent.ACTION_DROP) {
				if (mDisplayType == DISPLAY_ALL || mDisplayType == DISPLAY_PAGE) {
					mPageGridFragment = (NoteBookPageGridFragment) getFragmentManager()
							.findFragmentByTag(NoteBookPageGridFragment.TAG);
					if (mPageGridFragment != null
							&& mPageGridFragment.getDraggedView() != null) {
						mPageGridFragment.getDraggedView().setVisibility(
								View.VISIBLE);
					}
				} else {
					mPersonalPageFragment = (NoteBookPageGridFragment) getFragmentManager()
							.findFragmentByTag(
									NoteBookPageGridFragment.ALIAS_TAG);
					if (mPersonalPageFragment != null
							&& mPersonalPageFragment.getDraggedView() != null) {
						mPersonalPageFragment.getDraggedView().setVisibility(
								View.VISIBLE);
					}
				}

				return true;
			}
			float x = event.getX();
			float y = event.getY();
			if (event.getAction() == DragEvent.ACTION_DRAG_ENDED
					&& ((x == 0) && (y == 0))) {
				if (mDisplayType == DISPLAY_ALL) {
					mPageGridFragment = (NoteBookPageGridFragment) getFragmentManager()
							.findFragmentByTag(NoteBookPageGridFragment.TAG);
					if (mPageGridFragment != null
							&& mPageGridFragment.getDraggedView() != null) {
						mPageGridFragment.getDraggedView().setVisibility(
								View.VISIBLE);
					}
				} else {
					mPersonalPageFragment = (NoteBookPageGridFragment) getFragmentManager()
							.findFragmentByTag(
									NoteBookPageGridFragment.ALIAS_TAG);
					if (mPersonalPageFragment != null
							&& mPersonalPageFragment.getDraggedView() != null) {
						mPersonalPageFragment.getDraggedView().setVisibility(
								View.VISIBLE);
					}
				}
				return true;
			}

			return true;
		}
	};

	public int getDisplayType() {
		return mDisplayType;
	}

	public void setDrawerEnabled(boolean enabled) {
		if(enabled){
			if(mDrawerLayout != null)
				mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		}else{
			if(mDrawerLayout != null && mDrawerList != null)
				mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mDrawerList);
		}
	}
	
	//begin smilefish
	private void hideLockedBooks(){
		if(mislocked) return;
		
		mislocked = true;
		mPreferenceEditor.putBoolean(
				mResources.getString(R.string.lock_state),
				mislocked);
		mPreferenceEditor.commit();
		
		// modify current book id
		if(mBookcase != null){
			NoteBook notebook = mBookcase.getCurrentBook();
			if(notebook == null)
				mBookcase.setCurrentBook(BookCase.NO_SELETED_BOOK);
			else if ( notebook.getIsLocked()) {
				long unlockedbookid = mBookcase
						.getNextUnlockedBookID(true);
				if (unlockedbookid == 0) {
					mBookcase
							.setCurrentBook(BookCase.NO_SELETED_BOOK);
				} else {
					mBookcase
							.setCurrentBook(unlockedbookid);
				}
			}
		}
	}
	//end smilefish

	@Override
	public void onBackPressed() {
		//emmanual to fix bug 433860
		if(ShareToMeActivity.mShareToMeActivity != null){
			//add for TT349735 [Carol]
			ShareToMeActivity.mShareToMeActivity.finish();
			ShareToMeActivity.mShareToMeActivity = null;
		}
		
		if(mActionBar == null) //Dave. Fix a bug : occasionally crash when press back in page editor mode.
			return;
		
		if (mDisplayType == DISPLAY_PAGE) {
			setDisplayType(DISPLAY_ALL);//mPageGridFragment.isPrivateMode() ? DISPLAY_PERSONAL: 
					
		} else if (mDisplayType == DISPLAY_ALL_PAGE || mDisplayType == DISPLAY_BOOKMARK 
				|| mDisplayType == DISPLAY_TIMESTAMP)
		{
				//emmanual to fix bug 465363
				selectItem(0);
		}
		else {
			super.onBackPressed();
		}
		
        hideLockedBooks(); //smilefish fix bug 313241
	}

	//darwin
    public static String pathForCroppedPhoto(Context context, String fileName) {
        final File f = new File(MetaData.DATA_DIR + ".png");//MetaData.DATA_DIR 
        
        return f.getAbsolutePath();
    }
    
	//begin smilefish
	private Uri getImageUri(Uri selectedImage){
		//by emmanual, add try-catch to fix bug 382786 and 382931
		Cursor cursor = null;
		try{
			cursor = mContext.getContentResolver().query(selectedImage,new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
			if(cursor != null && cursor.moveToFirst()){
				cursor.close(); //smilefish fix memory leak
				return selectedImage;	
			}else{
				String filePath = PickerUtility.getRealFilePathForImage(mContext, selectedImage);
				filePath = Uri.decode(filePath);
		        ContentValues values = new ContentValues();
		        values.put(MediaStore.Images.Media.DATA, filePath);
		        cursor.close(); //smilefish fix memory leak
		        return mContext.getContentResolver().insert(
		                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			}
		}catch(Exception ex){
			String filePath = PickerUtility.getRealFilePathForImage(mContext, selectedImage);
	        ContentValues values = new ContentValues();
	        values.put(MediaStore.Images.Media.DATA, filePath);
	        return mContext.getContentResolver().insert(
	                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		}finally {
		    if (cursor != null) //smilefish fix memory leak
		        cursor.close();
		}
	}
	//end smilefish
   
    private String mStrPath = "";
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_IMPORT
				&& resultCode == Activity.RESULT_OK) {
			String path = null;
			if (data.getData().toString().contains("content://")) {
				Cursor cursor = getContentResolver().query(data.getData(),
						null, null, null, null);
				if(cursor == null) return; //smilefish fix bug 749082/749921
				if(cursor.getCount() == 0) //smilefish fix google play bug
		        {
		        	cursor.close();
		        	return;
		        }
				cursor.moveToFirst();
				int col = cursor.getColumnIndex("_data");
				if(col == -1){ //smilefish fix google play bug
					cursor.close();
		        	return;
				}
				String s = cursor.getString(col);
				path = s;
				cursor.close();//RICHARD FIX MEMORY LEAK
			} else {
				path = data.getData().getPath();
			}
			path = Uri.decode(path);
			// BEGIN: Better
			boolean isSnb = false;
			if (path.endsWith(MetaData.BACKUP_EXTENSION)) {
				isSnb = true;
			}
			int version = MetaData.UNKNOWN_VERSION;
			version = getImportFileVersion(path, isSnb);
			if (version == MetaData.UNKNOWN_VERSION) {
				showDialog(IMPORT_FAIL_DIALOG);
			} else {
				importNote(version, path, isSnb);
			}
			// END: Better

		} else if (requestCode == REQUEST_IMPORT_PDF
				&& resultCode == Activity.RESULT_OK) {
			//emmanual
			Uri uri = data.getData();
			if (uri != null
			        && (uri.toString().startsWith(
			                "content://com.google.android.apps.docs.storage") || uri
			                .toString()
			                .startsWith(
			                        "content://com.google.android.apps.photos.content"))) {
				// emmanual to check network
				if (!MetaData.isNetworkAvailable(NoteBookPickerActivity.this)) {
					EditorActivity.showToast(NoteBookPickerActivity.this,
					        R.string.sync_setting_networkless);
					return;
				}

				Cursor cursor = null;
				try {
					cursor = getContentResolver().query(uri, null, null, null,
					        null);
					if (cursor.getCount() == 0) {
						cursor.close();
						return;
					}
					cursor.moveToFirst();
					String path = MetaData.CROP_TEMP_DIR + cursor.getString(2);
					DownloadFromGoogleTask task = new DownloadFromGoogleTask(
					        uri, path, DownloadFromGoogleTask.IMPORT_PDF_CODE);
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
			
			String path = PickerUtility.getRealFilePath(NoteBookPickerActivity.this, data.getDataString());
			if (path == null) {
				if (data.getData().toString().contains("content://")) {
					Cursor cursor = null;
					try {
						cursor = getContentResolver().query(
						        data.getData(), null, null, null, null);
						cursor.moveToFirst();
						path = cursor.getString(cursor.getColumnIndex("_data"));
						cursor.close();
					} catch (Exception e) {
						Builder builder = new AlertDialog.Builder(NoteBookPickerActivity.this);
						builder.setMessage(R.string.reading_pdf_error);
						builder.setTitle(R.string.error);
						builder.setPositiveButton(android.R.string.ok, null);
						builder.create().show();
					}finally {
						if (cursor != null) { //smilefish fix memory leak
							cursor.close();
						}
					}
				} else {
					path = data.getData().getPath();
				}
			
				if (path == null) {
					Builder builder = new AlertDialog.Builder(
					        NoteBookPickerActivity.this);
					builder.setMessage(R.string.reading_pdf_error);
					builder.setTitle(R.string.error);
					builder.setPositiveButton(android.R.string.ok, null);
					builder.create().show();
					return;
				}
				path = Uri.decode(path);
			}
			
			if (!mIsReadPdfTaskRunning) {
				mIsReadPdfTaskRunning = true;
				PickerUtility.lockRotation(NoteBookPickerActivity.this);
				mReadPdfTask = new ReadPdfTask(path);
				mReadPdfTask.execute();
			}
		} else if (requestCode == REQUEST_EXPORT_DIR_CHANGE
				&& resultCode == Activity.RESULT_OK) {
			String path = data.getStringExtra("FILE_PATH");
			if (path == null) {
				return;
			}
			File file = new File(path);
			if (file.exists() == false) {
				file.mkdirs();
			}
			path = Uri.decode(path);
			mPreferenceEditor.putString(
					mResources.getString(R.string.pref_export_dir), path);
			mPreferenceEditor.commit();
			if (mExportDirTextView != null) {
				mExportDirTextView.setText(path);
			}
		} else if (requestCode == REQUEST_EXPORT_PDF_DIR_CHANGE
				&& resultCode == Activity.RESULT_OK) { // Better
			String path = data.getStringExtra("FILE_PATH");
			if (path == null) {
				return;
			}
			File file = new File(path);
			if (file.exists() == false) {
				file.mkdirs();
			}
			mPreferenceEditor.putString(
					mResources.getString(R.string.pref_export_pdf_dir), path);
			mPreferenceEditor.commit();
			if (mExport2PdfDirTextView != null) {
				mExport2PdfDirTextView.setText(path);
			}
		} 
//darwin
		else if(requestCode == RESULT_CAMERA || requestCode == RESULT_GALLERY)
		{
			//emmanual to fix bug 569009
			if(mAttacher == null){
				if(requestCode == RESULT_CAMERA){
					mAttacher = mAttachers[0];
				}else{
					mAttacher = mAttachers[1];
				}
				int index = mPreference.getInt(MetaData.LAST_EDITED_BOOK_INDEX, 0);
				if (index < mBookcase.getBookList().size()) {
					editBookCoverDialog(mBookcase.getBookList().get(index), false);
				}
			}
			if(resultCode == RESULT_OK){
				if (data != null && data.getData() != null) {//emmanual to fix bug 416405
					//emmanual to fix bug 382786
				    String uri = data.getData().toString();
				    if(uri != null&&uri.startsWith("content://com.google.android.apps.docs.storage")){
				        EditorActivity.showToast(this, R.string.prompt_err_open);
			            return;
				    }
				}
			    
				String croppedPath = "";
				Uri selectedImage = null;
				Intent intent = new Intent("com.android.camera.action.CROP");
				if(requestCode == RESULT_CAMERA)
				{
					try{ //TT Bug 324239
	                    mStrPath = Uri.decode(((CameraAttacher)mAttacher).getPath());
					}catch(Exception e){
						e.printStackTrace();
					}
					croppedPath = Uri.decode(pathForCroppedPhoto(mContext, mStrPath)); //smilefish fix bug 349687
					selectedImage = Uri.fromFile(new File(mStrPath));
					intent.setDataAndType(selectedImage, "image/*");
				    intent.putExtra("aspectX", 1);  
				    intent.putExtra("aspectY", 1);
				}
				else
				{
					//smilefish fix bug 405438/405431
					if(data == null){
						return ;
					}
					//end smilefish
					mStrPath = PickerUtility.getRealFilePathForImage(mContext, data.getData());
					croppedPath = Uri.decode(pathForCroppedPhoto(mContext, mStrPath));
					selectedImage = data.getData();
					//begin smilefish: fix bug 301809//content://com.google.android.apps.docs.storage/document/acc%3D1%3Bdoc%3D2
					selectedImage = getImageUri(selectedImage);//content://media/external/images/media/1071
					//end smilefish
					intent.setData(selectedImage);
			       intent.putExtra("crop", "true");   
			       intent.putExtra("aspectX", 1);  
			       intent.putExtra("aspectY", 1);  
			       //begin smilefish ?? gallery will crash when the size is larger than 400*400
			       int width = (int)getResources().getDimension(R.dimen.change_cover_crop_width);
			       int height = (int)getResources().getDimension(R.dimen.change_cover_crop_height);	       
			       if(height > 400)
			       {
			    	   width = width * 400 / height;
			    	   height = 400;
			       }
			       intent.putExtra("aspectX", width);  
			       intent.putExtra("aspectY", height); 
			       intent.putExtra("outputX", width); 
			       intent.putExtra("outputY", height);
			       //end smilefish
			       intent.putExtra("return-data", true);
				}
	
	            final Uri croppedPhotoUri = Uri.fromFile(new File(croppedPath));
	
		       intent.putExtra(MediaStore.EXTRA_OUTPUT, croppedPhotoUri);
		       
		       try{
		    	   startActivityForResult(intent, REQUEST_CROP_ICON);
		       }
		       catch(Exception e){
		       }
			}else{
				//emmanual to fix bug 466553
				if (mEditBookCoverAdapter != null) {
					mEditBookCoverAdapter.restoreCoverIndex();
				}
			}
		}
		else if(requestCode == REQUEST_CROP_ICON)
		{
			if(resultCode == RESULT_OK){
				if(mAttacher != null)
				{
					data.putExtra("path", pathForCroppedPhoto(mContext, mStrPath));
					mAttacher.attachItem(data);
				}
			}else{
				if (mEditBookCoverAdapter != null) { //smilefish fix bug 556631
					mEditBookCoverAdapter.restoreCoverIndex();
				}
			}
			
			//emmanual to fix bug 443721
			if (editCoverTitle != null) {
				editCoverTitle.requestFocus();
				//editCoverTitle.setText(editCoverTitleText); //smilefish fix bug 649483
			}
		}else if(resultCode == RESULT_OK && requestCode == REQUEST_DATACOPY){//added by noah;for data copy
			if(data == null)
				return;

		}else if(requestCode == REQUEST_TUTORIAL_INTRO_PAGE){
			if (widgetInitTask != null) {
				widgetInitTask.dismissProgressDialog();
				widgetInitTask = null;
			}
			
			if(resultCode == RESULT_OK){ //smilefish fix bug 663498
				this.finish();
			}
			
			if(MethodUtils.needShowPermissionPage(mContext)){
				MethodUtils.showPermissionPage(this, false);
			}
			
		}
		//darwin
	}

	//emmanual to fix bug 392671
	public static void dismissLoadProgressDialog(){
		if (widgetInitTask != null) {
			widgetInitTask.dismissProgressDialog();
			widgetInitTask = null;
		}
	}
	
	// BEGIN: Better
	public void updateUI() {
		if(mFragmentManager == null){ //smilefish fix bug 515432
			mFragmentManager = getFragmentManager();
		}
		mFragmentManager.executePendingTransactions();//darwin
		mPageGridFragment = (NoteBookPageGridFragment) mFragmentManager
				.findFragmentByTag(NoteBookPageGridFragment.TAG);
		if (mPageGridFragment != null) {
			mPageGridFragment.dataSetChangedUpdate();
		}

		mAllPageViewFragment = (AllPageViewFragment) mFragmentManager
				.findFragmentByTag(AllPageViewFragment.TAG);
		if (mAllPageViewFragment != null) {
			mAllPageViewFragment.dataSetChange();
		}

		mFavoritesPageFragment = (AllPageViewFragment) mFragmentManager
				.findFragmentByTag(FavoritesPageFragment.TAG);
		if (mFavoritesPageFragment != null) {
			mFavoritesPageFragment.dataSetChange();
		}

		mPersonalPageFragment = (NoteBookPageGridFragment) mFragmentManager
				.findFragmentByTag(NoteBookPageGridFragment.ALIAS_TAG);
		if (mPersonalPageFragment != null) {
			long id = mBookcase.getSelectedPrivateBookId();
			mPersonalPageFragment.changeData(id);
		}
		
		// begin wendy
		mTimestampViewFragment = (TimeStampViewFragment) mFragmentManager
				.findFragmentByTag(TimeStampViewFragment.TAG);
		if (mTimestampViewFragment != null) {
			mTimestampViewFragment.dataSetChange();
		}
		// end wendy

		// BEGIN: Better
		updateSyncStatusBar();
		// END: Better

	}
	// END: Better

	//begin darwin
	public void updateFragment_edit() {
		mFragmentManager.executePendingTransactions();//darwin
		mPageGridFragment = (NoteBookPageGridFragment) mFragmentManager
				.findFragmentByTag(NoteBookPageGridFragment.TAG);
		if (mPageGridFragment != null) {

			mPageGridFragment.dataSetChangedUpdate();
		}
	}
	//end   darwin
	public void updateFragment() {
		//begin smilefish
		if(mFragmentManager == null){
			mFragmentManager = getFragmentManager();
		}
		
		if(mFragmentManager.isDestroyed()) return;
		//end smilefish
			
		mFragmentManager.executePendingTransactions();//darwin
		mPageGridFragment = (NoteBookPageGridFragment) mFragmentManager
				.findFragmentByTag(NoteBookPageGridFragment.TAG);
		if (mPageGridFragment != null) {
			mPageGridFragment.dataSetChangedUpdate();
			mPageGridFragment.setNormalModeMenu();
		}

		mAllPageViewFragment = (AllPageViewFragment) mFragmentManager
				.findFragmentByTag(AllPageViewFragment.TAG);
		if (mAllPageViewFragment != null) {
			mAllPageViewFragment.dataSetChange();
			mAllPageViewFragment.setNormalModeMenu();
		}

		mFavoritesPageFragment = (AllPageViewFragment) mFragmentManager
				.findFragmentByTag(FavoritesPageFragment.TAG);
		if (mFavoritesPageFragment != null) {
			mFavoritesPageFragment.dataSetChange();
			mFavoritesPageFragment.setNormalModeMenu();
		}

		mPersonalPageFragment = (NoteBookPageGridFragment) mFragmentManager
				.findFragmentByTag(NoteBookPageGridFragment.ALIAS_TAG);
		if (mPersonalPageFragment != null) {
			long id = mBookcase.getSelectedPrivateBookId();
			mPersonalPageFragment.changeData(id);
			mPersonalPageFragment.setNormalModeMenu();
		}
		// begin wendy

		mTimestampViewFragment = (TimeStampViewFragment) mFragmentManager
				.findFragmentByTag(TimeStampViewFragment.TAG);
		if (mTimestampViewFragment != null) {
			mTimestampViewFragment.dataSetChange();
		}
		// end wendy

		// BEGIN: Better
		updateSyncStatusBar();
		// END: Better
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//begin smilefish
		if(mExport2PdfTask != null && mIsExport2PdfTaskRunning)
			NoteBookPickerActivity.this.dismissDialog(EXPORT_PDF_PROGRESS_DIALOG);
		if(mExportTask != null && mIsExportTaskRunning)
			NoteBookPickerActivity.this.dismissDialog(EXPORT_PROGRESS_DIALOG);
		if(mImportTask != null && mIsImportTasRunning)
			NoteBookPickerActivity.this.dismissDialog(IMPORT_PROGRESS_DIALOG);
		if(mDeleteBooksTask != null && mIsDeleteBookTaskRunning)
			NoteBookPickerActivity.this.dismissDialog(DELETE_BOOK_PROGRESS_DIALOG);
		//end smilefish
	}

	//darwin
	void checkPersonal()
	{
		if (mPreference.getBoolean(
				mResources.getString(R.string.pref_has_password),
				false) == false) 
		{
			setDisplayType(DISPLAY_ALL);
		}
	}
	void displayPersonal()
	{
		setActionBarHomeUp(true);
		if (mPreference.getBoolean(
				mResources.getString(R.string.pref_has_password),
				false) == false) 
		{
			//Emmanual
			setDisplayType(DISPLAY_ALL);
		} 
		else 
		{
 			inputPassword();
		}
		updateLockedstate(false);
	}
	void setActionBarHomeUp(boolean isHomeUp)
	{
		ActionBar bar = getActionBar();
		if(bar != null)
		{
			bar.setDisplayHomeAsUpEnabled(isHomeUp);
			bar.setHomeButtonEnabled(isHomeUp);
		}
	}
	
	int mCurrentDisplayIndex = 0; //Dave
	//darwin
	private void setDisplayType(int index) {
		switch (index) {
		case DISPLAY_ALL:
			displayDefaultType();
			//begin noah; for share;5.13

			if(isShareMode()){
	        	if(mActionBar != null){
	    			mActionBar.setCustomView(mbook_actionbar);
	    			mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
	        		mActionBar.setDisplayShowTitleEnabled(true);
	        		mActionBar.setTitle(R.string.widget_select_notebook);
	        		View view = mbook_actionbar.findViewById(R.id.cancelLayout);
	        		view.setVisibility(View.VISIBLE);
	        		view = mbook_actionbar.findViewById(R.id.cancelView);
	        		view.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View arg0) {// update by jason
							Intent shareIntent =getIntent().getParcelableExtra(Intent.EXTRA_INTENT);
							if (shareIntent!=null&&shareIntent.getBooleanExtra(MetaData.INSTANT_SHARE, false)) {
								NoteBook.deleteDir(new File(shareIntent.getStringExtra(MetaData.INSTANT_DOODLEITEM_PATH_STRING)));
							}// end
							onBackPressed();
						}
					});
	        	}
	        }
			else {
				if(mActionBar != null){
					mActionBar.setTitle(NoteBookViewString);
				}
			}
			//end noah; for share;
			break;
			//begin darwin
		case DISPLAY_ALL_PAGE:
			displayAllPageView();
			break;
			//end darwin
		case DISPLAY_BOOKMARK:
			displayBookmarkOnly();
			break;
		case DISPLAY_PERSONAL:
			displayPersonal();//darwin
			break;
		case DISPLAY_TIMESTAMP:// wendy
			displayTimeStampOnly();
			break;
		}
		mCurrentDisplayIndex = index;
	}

	private void setupPassword() {
		View view = View.inflate(NoteBookPickerActivity.this,
				R.layout.password_dialog, null);
		final View currPasswordGroup = view
				.findViewById(R.id.currPasswordGroup);
		final EditText oldPasswordText = (EditText) view
				.findViewById(R.id.oldPassword);
		final EditText newPasswordText = (EditText) view
				.findViewById(R.id.password);
		final EditText confirmPasswordText = (EditText) view
				.findViewById(R.id.password2);
		oldPasswordText.setTypeface(Typeface.DEFAULT);
		newPasswordText.setTypeface(Typeface.DEFAULT);
		confirmPasswordText.setTypeface(Typeface.DEFAULT);
		oldPasswordText
				.setTransformationMethod(new PasswordTransformationMethod());
		newPasswordText
				.setTransformationMethod(new PasswordTransformationMethod());
		confirmPasswordText
				.setTransformationMethod(new PasswordTransformationMethod());
		if (mPreference.getBoolean(NoteBookPickerActivity.this.getResources()
				.getString(R.string.pref_has_password), false) == false) {
			oldPasswordText.setEnabled(false);
		}
		boolean hasPassword = mPreference.getBoolean(
				getResources().getString(R.string.pref_has_password), false);
		currPasswordGroup.setVisibility(hasPassword ? View.VISIBLE : View.GONE);

		AlertDialog.Builder builder = new AlertDialog.Builder(
				NoteBookPickerActivity.this);
		builder.setCancelable(false);
		builder.setTitle(R.string.password);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setNegativeButton(android.R.string.cancel, null);
		final AlertDialog dialog = builder.create();
		DialogUtils.forcePopupSoftInput(dialog); //smilefish fix bug 556060
		dialog.show();
		dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// no old password
						String newPassword = newPasswordText.getText()
								.toString().trim();
						String confirmPassword = confirmPasswordText.getText()
								.toString().trim();
						int passwordLength = newPassword.length();
						// illegal passowrd length
						if (passwordLength < SettingActivity.MIN_PASSWORD_LENGTH
								|| passwordLength > SettingActivity.MAX_PASSWORD_LENGTH) {
							confirmPasswordText.setText("");
							newPasswordText.setText("");
							newPasswordText.setHint(R.string.password_tips);
							newPasswordText.requestFocus();
						} else if ((newPassword.equals(confirmPassword)) == false) {
							confirmPasswordText.setText("");
							confirmPasswordText
									.setHint(R.string.password_diff_password);
							newPasswordText.setText("");
							newPasswordText.requestFocus();
						} else if (newPasswordText
								.getText()
								.toString()
								.equals(confirmPasswordText.getText()
										.toString())) {
							dialog.dismiss();

							mPreferenceEditor.putInt(
									mResources
											.getString(R.string.pref_picker_display_type),
									DISPLAY_ALL);
							mPreferenceEditor.putString(
									NoteBookPickerActivity.this.getResources()
											.getString(R.string.pref_password),
									newPasswordText.getText().toString());
							mPreferenceEditor
									.putBoolean(
											NoteBookPickerActivity.this
													.getResources()
													.getString(
															R.string.pref_has_password),
											true);
							mPreferenceEditor.commit();
							SettingActivity.sendPasswordToMail(
									NoteBookPickerActivity.this,
									newPasswordText.getText().toString());

							// begin wendy

							mislocked = true;
							mPreferenceEditor.putBoolean(
									mResources.getString(R.string.lock_state),
									mislocked);
							mPreferenceEditor.commit();
							displayDefaultType();

							// end wendy
						}
					}
				});
		dialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						mDisplayType = DISPLAY_PERSONAL;
					}
				});
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		AlertDialog.Builder builder = null;
		switch (id) {
		case IMPORT_CONFIRM_DIALOG: {
			// Done
			String msg = mResources.getString(R.string.prompt_import) + "\n"
					+ args.getString("path");
			View view = View.inflate(this, R.layout.one_msg_dialog, null);
			final TextView textView = (TextView) view
					.findViewById(R.id.msg_text_view);
			textView.setText(msg);
			builder = new AlertDialog.Builder(mContext);
			builder.setCancelable(false);
			builder.setTitle(R.string.pg_import);
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							removeDialog(IMPORT_CONFIRM_DIALOG);
						}
					});
			AlertDialog dialog = builder.create();
			return dialog;
		}
		case IMPORT_OPTION_DIALOG: {
			builder = new AlertDialog.Builder(mContext);
			builder.setCancelable(false);
			builder.setTitle(R.string.pg_import);
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							removeDialog(IMPORT_OPTION_DIALOG);
						}
					});
			
			ArrayList<String> list = new ArrayList<String>();
			list.add(mResources.getString(R.string.select_all));
			list.addAll(mImportOptionsMap.values());
			int size = list.size();
			final boolean[] checked = new boolean[size];
			for (int i = 0; i < size; i++) {
				checked[i] = true;
			}
			builder.setMultiChoiceItems(list.toArray(new String[0]), checked, 
					new DialogInterface.OnMultiChoiceClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
							AlertDialog dialog = (AlertDialog) arg0;
							ListView lv = dialog.getListView();
							if (arg1 == 0) {
								int count = lv.getCount();
								if (arg2) {
									for (int i = 1; i < count; i++) {
										if (!lv.isItemChecked(i)) {
											checked[i] = true;
											lv.setItemChecked(i, true);
										}
									}
								} else {
										for (int i = 1; i < count; i++) {
											if (lv.isItemChecked(i) || checked[i]) {
												checked[i] = false;
												lv.setItemChecked(i, false);
											}
										}
								}
							} else {
								if (!arg2) {
									if (lv.isItemChecked(0)) {
										checked[0] = false;
										lv.setItemChecked(0, false);
									}
								}else{
									if (lv.getCheckedItemCount() == lv.getCount() - 1) {
										checked[0] = true;
										lv.setItemChecked(0, true);
									}
								}
							}
						}
						
					});
			AlertDialog dialog = builder.create();
			return dialog;
		}
		case IMPORT_PROGRESS_DIALOG: {
			// Done
			ProgressDialog dialog = new ProgressDialog(mContext);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setCancelable(false);
			dialog.setTitle(R.string.pg_import);
			DialogInterface.OnClickListener listener = null;
			dialog.setButton(Dialog.BUTTON_NEGATIVE, 
					mResources.getString(android.R.string.cancel), listener);
			return dialog;
		}
		case IMPORT_SUCCESS_DIALOG: {
			// Done
			View view = View.inflate(this, R.layout.one_msg_dialog, null);
			final TextView textView = (TextView) view
					.findViewById(R.id.msg_text_view);
			textView.setText(R.string.prompt_import_ok);
			builder = new AlertDialog.Builder(mContext);
			builder.setCancelable(false);
			builder.setTitle(R.string.pg_import);
			builder.setView(view);
			//Begin: show_wang@asus.com
			//Modified reason: setpassword if the import notebook is lucked when there is no password
			builder.setPositiveButton(android.R.string.ok, 
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					BookCase bookcase = BookCase.getInstance(NoteBookPickerActivity.this);
					if ( bookcase.getImportedNoteBook().getIsLocked() ) {
						boolean haspassword = NoteBookPickerActivity.HasPersonalPassword();
						if (!haspassword) {
								Log.v("wendy","has locked book!");
								try {
									final Intent intent = new Intent(mContext, SyncSetprivatePassword.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);	
									intent.putExtra("sendintent", "NoteBookActivity");
									mContext.startActivity(intent);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}	
						}
					}
				}
			});
			//End: show_wang@asus.com
			AlertDialog dialog = builder.create();
			return dialog;
		}
		case IMPORT_PDF_SUCCESS_DIALOG: {
			// Emmanual
			View view = View.inflate(this, R.layout.one_msg_dialog, null);
			final TextView textView = (TextView) view
					.findViewById(R.id.msg_text_view);
			textView.setText(R.string.prompt_import_ok);
			builder = new AlertDialog.Builder(mContext);
			builder.setCancelable(false);
			builder.setTitle(R.string.pdf_import);
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok, 
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			AlertDialog dialog = builder.create();
			return dialog;
		}
		case DELETE_BOOK_CONFIRM_DIALOG: {
			// Done
			View view = View.inflate(this, R.layout.one_msg_dialog, null);
			final TextView textView = (TextView) view
					.findViewById(R.id.msg_text_view);
			textView.setText(R.string.nb_del_msg);
			builder = new AlertDialog.Builder(mContext);
			builder.setCancelable(false);
			builder.setTitle(R.string.del);
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setNegativeButton(android.R.string.cancel, null);
			AlertDialog dialog = builder.create();
			return dialog;
		}
		case DELETE_BOOK_PROGRESS_DIALOG: {
			// Done
			ProgressDialog dialog = new ProgressDialog(mContext);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setCancelable(false);
			dialog.setTitle(R.string.del);
			return dialog;
		}
		case DELETE_BOOK_SUCCESS_DIALOG:
			break;
		case EXPORT_CONFIRM_DIALOG: {
			// Done
			final StringBuffer path = new StringBuffer(mPreference.getString(
					mResources.getString(R.string.pref_export_dir),
					MetaData.EXPORT_DIR));
			File defaultDir = new File(path.toString());
			defaultDir.mkdirs();
			if (defaultDir.exists() == false) {
				path.delete(0, path.length());
				path.append(MetaData.EXPORT_DIR);
			}
			View view = View.inflate(this, R.layout.export_note_dialog, null);
			mExportDirTextView = (TextView) view
					.findViewById(R.id.export_dir_text_view);
			mExportFileNameEditText = (EditText) view
					.findViewById(R.id.export_file_name_edit_text);
			// BEGIN: Better
			TextView fileName = (TextView) view
					.findViewById(R.id.export_file_name);
			if ((args == null) || !args.getBoolean("rename", false)) {
				mExportFileNameEditText.setVisibility(View.GONE);
				fileName.setVisibility(View.GONE);
			}
			// END: Better
			mExportDirTextView.setText(path);
			view.findViewById(R.id.browser_button).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							
							try {
								Intent intent = new Intent();
								intent.putExtra("DEFAULT_PATH", path.toString());
								intent.setClassName("com.asus.filemanager",
										"com.asus.filemanager.dialog.FolderSelection");
								startActivityForResult(intent,
										REQUEST_EXPORT_DIR_CHANGE);
								//begin:clare:?????????filemanager dialog,????????????apk????l???
							} catch (ActivityNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Toast.makeText(mContext, R.string.import_export_book_fail_reason, Toast.LENGTH_SHORT).show();							
							}
							//end:clare
						}
					});
			builder = new AlertDialog.Builder(mContext);
			builder.setCancelable(false);
			builder.setTitle(R.string.file_export);
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							removeDialog(EXPORT_CONFIRM_DIALOG);
						}
					});
			AlertDialog dialog = builder.create();
			return dialog;
		}
		case EXPORT_PROGRESS_DIALOG: {
			// Done
			ProgressDialog dialog = new ProgressDialog(mContext);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setCancelable(false);
			dialog.setTitle(R.string.file_export);
			// BEGIN: Better
			// BEGIN: Better
			DialogInterface.OnClickListener listener = null;
			dialog.setButton(Dialog.BUTTON_NEGATIVE, mResources.getString(android.R.string.cancel), listener);
			// END: Better
			return dialog;
		}
		case EXPORT_SUCCESS_DIALOG: {
			// Done
			View view = View.inflate(this, R.layout.one_msg_dialog, null);
			final TextView textView = (TextView) view
					.findViewById(R.id.msg_text_view);
			textView.setText(args.getString("msg",
					mResources.getString(R.string.file_export)));
			builder = new AlertDialog.Builder(mContext);
			builder.setCancelable(false);
			builder.setTitle(R.string.book_export);
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							removeDialog(EXPORT_SUCCESS_DIALOG);
							updateFragment();
						}
					});
			AlertDialog dialog = builder.create();
			return dialog;
		}
		case IMPORT_FAIL_DIALOG: {
			// Done
			View view = View.inflate(this, R.layout.one_msg_dialog, null);
			final TextView textView = (TextView) view
					.findViewById(R.id.msg_text_view);
			textView.setText(R.string.prompt_import_err);
			builder = new AlertDialog.Builder(mContext);
			builder.setCancelable(false);
			builder.setTitle(R.string.prompt_import);
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok, null);
			return builder.create();
		}

		case EXPORT_FAIL_DIALOG: {
			// Done
			View view = View.inflate(this, R.layout.one_msg_dialog, null);
			final TextView textView = (TextView) view
					.findViewById(R.id.msg_text_view);
			textView.setText(R.string.prompt_err_open);
			builder = new AlertDialog.Builder(mContext);
			builder.setCancelable(false);
			builder.setTitle(R.string.book_export);
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok, null);
			return builder.create();
		}
		// BEGIN: Better
		case EXPORT_PDF_CONFIRM_DIALOG: {
			final StringBuffer path = new StringBuffer(mPreference.getString(
					mResources.getString(R.string.pref_export_pdf_dir),
					MetaData.EXPORT_PDF_DIR));
			File defaultDir = new File(path.toString());
			defaultDir.mkdirs();
			if (defaultDir.exists() == false) {
				path.delete(0, path.length());
				path.append(MetaData.EXPORT_PDF_DIR);
			}
			View view = View.inflate(this, R.layout.export_note_dialog, null);
			mExport2PdfDirTextView = (TextView) view
					.findViewById(R.id.export_dir_text_view);
			mExportPDFFileNameEditText = (EditText) view
					.findViewById(R.id.export_file_name_edit_text);
			TextView fileName = (TextView) view
					.findViewById(R.id.export_file_name);
			if ((args == null) || !args.getBoolean("rename", false)) {
				mExportPDFFileNameEditText.setVisibility(View.GONE);
				fileName.setVisibility(View.GONE);
			}
			mExport2PdfDirTextView.setText(path);
			view.findViewById(R.id.browser_button).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try{
							Intent intent = new Intent();
							intent.putExtra("DEFAULT_PATH", path.toString());
							intent.setClassName("com.asus.filemanager",
									"com.asus.filemanager.dialog.FolderSelection");
							startActivityForResult(intent,
									REQUEST_EXPORT_PDF_DIR_CHANGE);
							//begin:clare:?????????filemanager dialog,????????????apk????l???
						} catch (ActivityNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Toast.makeText(mContext, R.string.import_export_book_fail_reason, Toast.LENGTH_SHORT).show();
						}
						//end:clare
						}
					});
			builder = new AlertDialog.Builder(mContext);
			builder.setCancelable(false);
			builder.setTitle(R.string.file_export);
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							removeDialog(EXPORT_PDF_CONFIRM_DIALOG);
						}
					});
			return builder.create();
		}
		case EXPORT_PDF_PROGRESS_DIALOG: {
			ProgressDialog pdfProgressDialog = new ProgressDialog(mContext);
			pdfProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pdfProgressDialog.setCancelable(false);
			DialogInterface.OnClickListener listener = null;
			pdfProgressDialog.setButton(Dialog.BUTTON_NEGATIVE, mResources.getString(android.R.string.cancel), listener);
			pdfProgressDialog.setTitle(R.string.file_export);
			return pdfProgressDialog;
		}
		case EXPORT_PDF_SUCCESS_DIALOG: {
			View pdfSuccessView = View.inflate(this, R.layout.one_msg_dialog,
					null);
			final TextView pdfSuccessTextView = (TextView) pdfSuccessView
					.findViewById(R.id.msg_text_view);
			pdfSuccessTextView.setText(args.getString("msg",
					mResources.getString(R.string.file_export)));
			builder = new AlertDialog.Builder(mContext);
			builder.setCancelable(false);
			builder.setTitle(R.string.book_export);
			builder.setView(pdfSuccessView);
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							removeDialog(EXPORT_PDF_SUCCESS_DIALOG);
							updateFragment();
						}
					});
			return builder.create();
		}
		case EXPORT_PDF_FAIL_DIALOG: {
			View pdfFailedView = View.inflate(this, R.layout.one_msg_dialog,
					null);
			final TextView pdfFailedTextView = (TextView) pdfFailedView
					.findViewById(R.id.msg_text_view);
			pdfFailedTextView.setText(R.string.prompt_err_open);
			builder = new AlertDialog.Builder(mContext);
			builder.setCancelable(false);
			builder.setTitle(R.string.book_export);
			builder.setView(pdfFailedView);
			builder.setPositiveButton(android.R.string.ok, null);
			return builder.create();
		}
		case CLOUD_SYNCING_PROGRESS_DIALOG: {
			ProgressDialog dlg = new ProgressDialog(mContext);
			dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dlg.setCancelable(false);
			dlg.setTitle(R.string.transfer);
			return dlg;
		}
		// END: Better
		//BEGIN: RICHARD
		case HWR_MSG_DIALOG:{
			final Long bookId = args.getLong(MetaData.BOOK_ID, 0L);
			View view = View.inflate(this,R.layout.notebook_hwr_dialog, null);
			builder = new AlertDialog.Builder(mContext);
			builder.setTitle(R.string.nb_Write_Text_Recognition);
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok, 								
					new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						removeDialog(HWR_MSG_DIALOG);
						showBookHWRStatusDialog(NoteBookPickerActivity.this,bookId);
					}
			});
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					removeDialog(HWR_MSG_DIALOG);
				}
		});
			return builder.create();

		}
		case HWR_PROGRESS_DIALOG:{
			final Long bookId = args.getLong(MetaData.BOOK_ID, 0L);
			final NoteBook noteBook = mBookcase.getNoteBook(bookId);
			
			builder = new AlertDialog.Builder(mContext);
			View view = View.inflate(this,
					R.layout.notebook_hwr_progress_bar_dialog, null);
			ProgressBar pb = (ProgressBar)view.findViewById(R.id.rectangleProgressBar);
			TextView tv = (TextView)view.findViewById(R.id.msg_text_view);
			
			final BookHWRTask bookHWRTask = new BookHWRTask(this,noteBook,pb,tv);
			mBookHWRTask = bookHWRTask;
			builder.setTitle(R.string.nb_Write_Text_Recognition);
			builder.setView(view);
			builder.setNegativeButton(android.R.string.cancel,null);
			builder.setCancelable(false);
	 
			Dialog dialog = builder.create();		
			bookHWRTask.setDialog(dialog);
			
			bookHWRTask.execute();
			return dialog;
		}
		//END: RICHARD		
		case EDITCOVER_CHOOSE_DIALOG:
		{
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
	        View view = View.inflate(this, R.layout.stop_time_dialog, null);
	        builder1.setTitle(R.string.nb_editcover_choose_title);
	        builder1.setView(view);
	        mEditCoverChooseDialog = builder1.create(); //smilefish
	        mEditCoverChooseDialog.setOnCancelListener(new OnCancelListener() {//emmanual to fix bug 466553
				
				@Override
				public void onCancel(DialogInterface arg0) {
					// TODO Auto-generated method stub
					if (mEditBookCoverAdapter != null) {
						mEditBookCoverAdapter.restoreCoverIndex();
					}
				}
			});
			return mEditCoverChooseDialog;
		}
		//Begin Allen
		case SORT_TYPE_DIALOG:
		{
	        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
	        View view = View.inflate(this, R.layout.stop_time_dialog, null);
	        builder1.setTitle(R.string.Sort);
	        builder1.setView(view);
	        builder1.setNegativeButton(android.R.string.cancel, null);
	        final AlertDialog dialog = builder1.create();	        
	        return dialog; 
		}
		//End Allen
	    //Begin smilefish
		case ADD_NOTEBOOK_CHOOSE_DIALOG:
		{
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
	        View view = View.inflate(this, R.layout.stop_time_dialog, null);
	        builder1.setTitle(R.string.nb_add_choose_title);
	        builder1.setView(view);
	        mAddBookDialog = builder1.create(); //smilefish
	        mAddBookDialog.setOnCancelListener(new OnCancelListener() {//emmanual to fix bug 466553
				
				@Override
				public void onCancel(DialogInterface arg0) {
					mIsAddBookDialogShow = false;
					NoteBookPageGridViewAdapter.resetNotebookPageGridProcessing();
				}
			});
			return mAddBookDialog;
		}    
	    //end smilefish    
		}
		return null;

	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		super.onPrepareDialog(id, dialog, args);
		switch (id) {
		case HWR_PROGRESS_DIALOG:
		{
			final AlertDialog d = (AlertDialog) dialog;
			d.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mBookHWRTask.cancelHWRTask();
						}
					});
			break;
		}
		
		case IMPORT_CONFIRM_DIALOG: {
			// Done
			final boolean isSnb = args.getBoolean("issnb", false);
			final String importPath = args.getString("path");
			final int fileVersion = args.getInt("version");
			AlertDialog d = (AlertDialog) dialog;
			d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							removeDialog(IMPORT_CONFIRM_DIALOG);
							if (mIsImportTasRunning == false) {
								mImportOptionsMap.clear();
								if (!isSnb) {
									switch (fileVersion) {
									case MetaData.SNE_VERSION_0302:
									case MetaData.SNE_VERSION_3: {
										boolean hasList = false;
										ZipFile zipFile;
										try {
											zipFile = new ZipFile(importPath);
											
											ZipEntry ze = null;
											BufferedInputStream bis = null;
											AsusFormatReader afr = null;
											
											Enumeration<?> entries = zipFile.entries();
											while (entries.hasMoreElements()) {
												ze = (ZipEntry) entries.nextElement();
												if (ze.getName().equals(MetaData.BOOK_LIST)) {
													bis = new BufferedInputStream(
															zipFile.getInputStream(ze));
													afr = new AsusFormatReader(bis,
															NotePage.MAX_ARRAY_SIZE);
													boolean isListCorrect = false, isInfoCorrect = false;;
													long id = 0;
													String title = "";
													AsusFormatReader.Item item = null;
											        for (item = afr.readItem(); item != null; item = afr.readItem()) {
											            switch (item.getId()) {
											            case AsusFormat.SNF_BOOKLIST_BEGIN:
											            	isListCorrect = true;
											            	break;
											            case AsusFormat.SNF_BOOKINFO_BEGIN:
											            	if (isListCorrect) {
											            		isInfoCorrect = true;
											            	}
											            	break;
											            case AsusFormat.SNF_BOOKINFO_ID:
											            	if (isInfoCorrect) {
											            		id = item.getLongValue();
											            	}
											            	break;
											            case AsusFormat.SNF_BOOKINFO_TITLE:
											            	if (isInfoCorrect) {
											            		title = item.getStringValue();
											            	}
											            	break;
											            case AsusFormat.SNF_BOOKINFO_END:
											            	if (isInfoCorrect) {
											            		isInfoCorrect = false;
											            		mImportOptionsMap.put(id, title);
											            	}
											            	break;
											            case AsusFormat.SNF_BOOKLIST_END:
											            	isListCorrect = false;
											            	break;
											            default:
											                break;
											            }
											        }
											        bis.close();
											        hasList = true;
											        break;
												}
											}
											zipFile.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
										
										if (hasList) {
											Bundle b = new Bundle();
											b.putString("path", importPath);
											b.putInt("version", fileVersion);
											b.putBoolean("issnb", isSnb);
											showDialog(IMPORT_OPTION_DIALOG, b);
										} else {
											mImportTask = new ImportNewFileTask(
													importPath, null, false, fileVersion);
											if (mImportTask != null
													&& mImportTask.isCancelled() == false) {
												mImportTask.execute();
											}
										}
										break;
									}
									default:
										mImportTask = null;
										break;
									}
								} else {
									if ((fileVersion == MetaData.SNE_VERSION_0302) 
											|| (fileVersion == MetaData.SNE_VERSION_3)) {
										Cursor cursor = null;
										try {
											ZipFile zipFile = new ZipFile(new File(importPath));
											ZipEntry dbZipEntry = new ZipEntry(MetaData.DATABASE_NAME);
											File dbFile = new File(MetaData.DATA_DIR, MetaData.DATABASE_NAME);
											BufferedOutputStream bos = new BufferedOutputStream(
														new FileOutputStream(dbFile));
											copyZipFile(zipFile.getInputStream(dbZipEntry), bos);
											bos.close();
											SQLiteDatabase db = SQLiteDatabase.openDatabase(
													dbFile.getAbsolutePath(), null, 
													SQLiteDatabase.OPEN_READONLY);
											cursor = db.query(MetaData.BOOK_TABLE, 
													null, null, null, null, null, null);
											cursor.moveToFirst();
											while (!cursor.isAfterLast()) {
												int index = -1;
												index = cursor.getColumnIndex(MetaData.BookTable.CREATED_DATE);
												if (index >= 0) {
													long id = cursor.getLong(index);
													index = cursor.getColumnIndex(MetaData.BookTable.TITLE);
													if (index >= 0) {
														String title = cursor.getString(index);
														mImportOptionsMap.put(id, title);
													}
												}
												cursor.moveToNext();
											}
											cursor.close();
											db.close();
											if (mImportOptionsMap.size() > 0) {
												Bundle b = new Bundle();
												b.putString("path", importPath);
												b.putInt("version", fileVersion);
												b.putBoolean("issnb", isSnb);
												showDialog(IMPORT_OPTION_DIALOG, b);
											}
										} catch (Exception e) {
											e.printStackTrace();
										}finally {
										    if (cursor != null) //smilefish fix memory leak
										        cursor.close();
										}
									} else {
										mImportTask = null;
									}
								}
							}
						}
					});
			break;
		}
		case IMPORT_OPTION_DIALOG: {
			final AlertDialog d = (AlertDialog) dialog;
			final String importPath = args.getString("path");
			final int fileVersion = args.getInt("version");
			final boolean isSnb = args.getBoolean("issnb", false);
			Button b = d.getButton(Dialog.BUTTON_POSITIVE);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					SparseBooleanArray state = d.getListView().getCheckedItemPositions();
					ArrayList<Long> list = new ArrayList<Long>();
					Long[] keys = mImportOptionsMap.keySet().toArray(new Long[0]);
					for (int i = 0; i < keys.length; i++) {
						if (state.get(i + 1)) {
							list.add(keys[i]);
						}
					}
					if (list.size() > 0) {
						removeDialog(IMPORT_OPTION_DIALOG);
						if ((fileVersion == MetaData.SNE_VERSION_0302) 
								|| (fileVersion == MetaData.SNE_VERSION_3)) {
							mImportTask = new ImportNewFileTask(
									importPath, list, isSnb, fileVersion);
							if (mImportTask != null
									&& mImportTask.isCancelled() == false) {
								mImportTask.execute();
							}
						}
						
					} else {
						Toast.makeText(mContext, 
								mResources.getString(R.string.prompt_no_book_selected), 
								Toast.LENGTH_LONG).show();
					}
				}
				
			});
			
			break;
		}
		case IMPORT_PROGRESS_DIALOG: {
			// Done
			ProgressDialog d = (ProgressDialog) dialog;
			if (mIsImportTasRunning) {
				mImportTask.setDialog(d);
			}
			
			ProgressBar v = (ProgressBar) d.findViewById(android.R.id.progress);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
			params.gravity = Gravity.CENTER_HORIZONTAL;
			params.width = LayoutParams.MATCH_PARENT;
			v.setLayoutParams(params);
			
			final Button b = d.getButton(Dialog.BUTTON_NEGATIVE);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if ((mImportTask != null) && mIsImportTasRunning) {
						mImportTask.cancel(true);
						b.setEnabled(false);
					}
				}
				
			});
			break;
		}
		case IMPORT_SUCCESS_DIALOG:
			break;
		case DELETE_BOOK_CONFIRM_DIALOG: {
			// Done
			final Long bookId = args.getLong(MetaData.BOOK_ID, 0L);
			AlertDialog d = (AlertDialog) dialog;
			//emmanual to fix bug 482702, 482771
			long[] bookIdArray = args.getLongArray(MetaData.BOOK_ID_LIST);
			final List<Long> bookIdList = new ArrayList<Long>();
			if(bookIdArray != null){
				for (long bid : bookIdArray) {
					bookIdList.add(bid);
				}
			}
			d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							removeDialog(DELETE_BOOK_CONFIRM_DIALOG);
							if (mIsDeleteBookTaskRunning == false) {
								if (bookId == -1) {
									mDeleteBooksTask = new DeleteBookTask(bookIdList);
									mDeleteBooksTask.execute();
								} else {
									mDeleteBooksTask = new DeleteBookTask(
											bookId);
									mDeleteBooksTask.execute();
								}
							}
						}
					});
			break;
		}
		case DELETE_BOOK_PROGRESS_DIALOG: {
			// Done
			ProgressDialog d = (ProgressDialog) dialog;
			if(args != null)
				d.setMax(args.getInt("max", 1));
			if (mIsDeleteBookTaskRunning) {
				mDeleteBooksTask.setDialog(d);
			}
			break;
		}
		case DELETE_BOOK_SUCCESS_DIALOG:
			break;
		case EXPORT_CONFIRM_DIALOG: {
			// Done
			// BEGIN: Better
			final boolean isToRename = (args != null)
					&& args.getBoolean("rename", false);
			// END: Better
			AlertDialog d = (AlertDialog) dialog;
			d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							removeDialog(EXPORT_CONFIRM_DIALOG);
							if (mExportTask != null) {
								if (isToRename) {
									mExportTask
											.setFileName(mExportFileNameEditText
													.getText().toString());
								}
								mExportTask.execute();
							}
							// }
						}
					});
			break;
		}
		case EXPORT_PROGRESS_DIALOG: {
			// Done
			ProgressDialog d = (ProgressDialog) dialog;
			if(args != null)
				d.setMax(args.getInt("max", 1));
			final Button b = d.getButton(Dialog.BUTTON_NEGATIVE);
			b.setOnClickListener(
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							if ((mExportTask != null) && mIsExportTaskRunning) {
								mExportTask.cancel(true);
								b.setEnabled(false);
							}
						}

					});
			if (mIsExportTaskRunning) {
				mExportTask.setDialog(d);
			}
			break;
		}
		case EXPORT_SUCCESS_DIALOG:
			break;

		// BEGIN: Better
		case EXPORT_PDF_CONFIRM_DIALOG: {
			final boolean isToRename = (args != null)
					&& args.getBoolean("rename", false);
			AlertDialog exportPdfConfirmDialog = (AlertDialog) dialog;
			exportPdfConfirmDialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (((isToRename && !mExportPDFFileNameEditText
									.getText().toString().isEmpty()) || !isToRename)
									&& !mIsExport2PdfTaskRunning) {
								removeDialog(EXPORT_PDF_CONFIRM_DIALOG);
								if (mExport2PdfTask != null) {
									if (isToRename) {
										mExport2PdfTask
												.setFileName(mExportPDFFileNameEditText
														.getText().toString());
									}
									mExport2PdfTask.execute();
								}
							}
						}
					});
			break;
		}
		case EXPORT_PDF_PROGRESS_DIALOG: {
			ProgressDialog exportPdfProgressDialog = (ProgressDialog) dialog;
			if(args != null)
				exportPdfProgressDialog.setMax(args.getInt("max", 1));
			final Button b = exportPdfProgressDialog.getButton(Dialog.BUTTON_NEGATIVE);
			b.setOnClickListener(
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							if ((mExport2PdfTask != null)
									&& mIsExport2PdfTaskRunning) {
								mExport2PdfTask.cancel(true);
								b.setEnabled(false);
							}
						}

					});
			if (mIsExport2PdfTaskRunning) {
				mExport2PdfTask.setDialog(exportPdfProgressDialog);
			}
			break;
		}
		case EXPORT_PDF_SUCCESS_DIALOG: {
			break;
		}
		case CLOUD_SYNCING_PROGRESS_DIALOG: {
			ProgressDialog dlg = (ProgressDialog) dialog;
			if (mIsCloudSyncingTaskRunning) {
				mCloudSyncingTask.setProgressDialog(dlg);
			}
			break;
		}
		// END: Better
		//Begin Allen
		case SORT_TYPE_DIALOG:
		{
			AlertDialog d = (AlertDialog)dialog;
			
			ListView countdownList = (ListView) d.findViewById(R.id.countdown_list);

	        String[] valueTexts = getResources().getStringArray(R.array.pg_sort);
	        countdownList.setAdapter(new ArrayAdapter<String>(this, R.layout.asus_list_item_single_choice, valueTexts));
	        countdownList.setOnItemClickListener(new OnItemClickListener() {

	            @Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	            	mPageGridFragment.SortNoteBook(position);
	                removeDialog(SORT_TYPE_DIALOG); // Better
	            }
	        });
	        //emmanual add try-catch to fix Bug 368364
	        try{
	        	countdownList.setItemChecked(mPageGridFragment.getSortOrder()==NoteBookPageGridFragment.SORT_BY_PAGE?0:1,true);
	        }catch (Exception e) {
				// TODO: handle exception
			}
	        break;
		}
		//End Allen
		//darwin
		case EDITCOVER_CHOOSE_DIALOG:
		{
			AlertDialog d = (AlertDialog)dialog;
			
			ListView countdownList = (ListView) d.findViewById(R.id.countdown_list);

	        String[] valueTexts = getResources().getStringArray(R.array.nb_editcover_choose_item);
	        countdownList.setAdapter(new ArrayAdapter<String>(this, R.layout.asus_list_item_single_nochoice, valueTexts));
	        countdownList.setOnItemClickListener(new OnItemClickListener() {

	            @Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	            	if(position == 0)
	            	{
	            		String[] countries = getResources().getStringArray(R.array.editor_func_insert_array);
	    				mAttacher = mAttachers[0];
	                    PickerUtility.lockRotation(NoteBookPickerActivity.this);
	                    startActivityForResult(Intent.createChooser(mAttacher.getIntent(), countries[EditorActivity.TAKE_PHOTO]), RESULT_CAMERA);
	                    removeDialog(EDITCOVER_CHOOSE_DIALOG);
	            	}
	            	else if(position == 1)
	            	{
	            		String[] countries = getResources().getStringArray(R.array.editor_func_insert_array);
	    				mAttacher = mAttachers[1];
	                    PickerUtility.lockRotation(NoteBookPickerActivity.this);
	                    startActivityForResult(Intent.createChooser(mAttacher.getIntent(), countries[EditorActivity.INSERT_PICTURE]), RESULT_GALLERY);
	                    removeDialog(EDITCOVER_CHOOSE_DIALOG);
	            	}
	            }
	        });
			break;
		}
		//darwin
		//smilefish
		case ADD_NOTEBOOK_CHOOSE_DIALOG:
		{
			final String bookname = args.getString("notebook");
			AlertDialog d = (AlertDialog)dialog;
			
			ListView countdownList = (ListView) d.findViewById(R.id.countdown_list);

	        String[] valueTexts = getResources().getStringArray(R.array.nb_add_choose_item);
	        
	        //emmanual to add book from PDF
	        List<String> valueTextList = new ArrayList<String>();
	        for(String s:valueTexts){
	        	valueTextList.add(s);
	        }
	        if(MethodUtils.isPenDevice(mContext)){
	        	valueTextList.add(getResources().getString(R.string.add_book_from_pdf));
	        }
	        
	        countdownList.setAdapter(new ArrayAdapter<String>(this, R.layout.asus_list_item_single_nochoice, valueTextList));
	        countdownList.setOnItemClickListener(new OnItemClickListener() {

	            @Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	            	//begin smilefish fix bug 556380/565139/559234
	            	if(mPageGridFragment == null)
	            		return;
	            	FragmentManager fm = mPageGridFragment.getChildFragmentManager();
	            	FragmentTransaction ft = fm.beginTransaction();
	            	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	            	AddNewDialogFragment addNewDialogFragment = (AddNewDialogFragment) fm
	        				.findFragmentByTag(AddNewDialogFragment.TAG);
	        		if (addNewDialogFragment != null) {
	        			ft.remove(addNewDialogFragment);
	        		}
	        		ft.addToBackStack(null);
	                
	            	if(position == 0)
	            	{
	            		addNewDialogFragment = AddNewDialogFragment.newInstance(bookname, true);
	            		addNewDialogFragment.show(ft, AddNewDialogFragment.TAG);
	                    removeDialog(ADD_NOTEBOOK_CHOOSE_DIALOG);
	                    mIsAddBookDialogShow = false;
	            	}
	            	else if(position == 1)
	            	{
	            		addNewDialogFragment = AddNewDialogFragment.newInstance(bookname, false);
	            		addNewDialogFragment.show(ft, AddNewDialogFragment.TAG);
	                    removeDialog(ADD_NOTEBOOK_CHOOSE_DIALOG);
	                    mIsAddBookDialogShow = false;
	            	}
	            	//end smilefish

	    	        //emmanual to add book from PDF
	            	else if(position == 2)
	            	{
	            		importPdfAsBook();
	                    removeDialog(ADD_NOTEBOOK_CHOOSE_DIALOG);
	                    mIsAddBookDialogShow = false;
	            	}
	            }
	        });
			break;
		}
		//end smilefish
		}
	}

	/********************************************************
	 * Delete NoteBook START
	 *******************************************************/
	public void deleteBook(Activity activity, ArrayList<Long> list) {
		Bundle b = new Bundle();
		b.putLong(MetaData.BOOK_ID, -1);
		//emmanual to fix bug 482702, 482771
		long[] array = new long[list.size()];
		for(int i=0;i<list.size();i++){
			array[i] = list.get(i);
		}
		b.putLongArray(MetaData.BOOK_ID_LIST, array);
		showDialog(DELETE_BOOK_CONFIRM_DIALOG, b);
	}

	public void deleteBook(Activity activity, Long id) {
		Bundle b = new Bundle();
		b.putLong(MetaData.BOOK_ID, id);
		showDialog(DELETE_BOOK_CONFIRM_DIALOG, b);
	}

	public void showBookHWRDialog(Activity activity,
			Long id) {
		// TODO Auto-generated method stub
		Bundle b = new Bundle();
		b.putLong(MetaData.BOOK_ID, id);
		showDialog(HWR_MSG_DIALOG, b);
		
	}
	
	public void showBookHWRStatusDialog(Activity activity,
			Long id) {
		// TODO Auto-generated method stub
		Bundle b = new Bundle();
		b.putLong(MetaData.BOOK_ID, id);
		showDialog(HWR_PROGRESS_DIALOG, b);
		
	}
	
	private class DeleteBookTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog mProgressDialog;
		private Long mBookId;
		private int mMax;
		private int mCount;
		private List<Long> mList = null;

		public DeleteBookTask(Long id) {
			mBookId = id;
			mMax = 5;
			mCount = 0;
		}
		
		public DeleteBookTask(List<Long> list) {
			//Begin:smilefish
			mList = new ArrayList<Long>();
			if (list != null) {
				for (Long item : list) {
					mList.add(item);
				}
			}
			//End:Smilefish
			mMax = 5;
			mCount = 0;
		}

		public void setDialog(ProgressDialog d) {
			mProgressDialog = d;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsDeleteBookTaskRunning = true;
			Bundle b = new Bundle();
			b.putInt("max", mMax);
			showDialog(DELETE_BOOK_PROGRESS_DIALOG, b);			
		}

		private void deleteOneBook(Long bookId)
		{
			NoteBook notebook = mBookcase.getNoteBook(bookId);
			notebook.deleteDir(new File(MetaData.DATA_DIR, Long
					.toString(bookId)));
			publishProgress();
			// begin wendy
			String selection = mislocked ? "is_locked = 0" : null;
			// end wendy
			Cursor cursor = mContentResolver.query(MetaData.BookTable.uri,
					null, selection, null, null);
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false) {
				int index = cursor.getPosition();
				if (cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE) == bookId
						.longValue()) {
					boolean b = (index > 0) ? cursor.moveToPrevious() : cursor
							.moveToNext();
					if (b == false) {
						// begin wendy
						mBookcase.setCurrentBook(BookCase.NO_SELETED_BOOK);

						break;
					} else {
						long id = cursor
								.getLong(MetaData.BookTable.INDEX_CREATED_DATE);
						// begin wendy
						mBookcase.setCurrentBook(id);

						break;
					}
				}
				cursor.moveToNext();
			}
			cursor.close();
			publishProgress();
			String[] selectionArgs = new String[] { Long.toString(bookId) };

			// add by wendy 0401 delete-> update begin++
			ContentValues cvbook = new ContentValues();
			cvbook.put(MetaData.BookTable.IS_DELETED, 1);
			cvbook.put(MetaData.BookTable.MODIFIED_DATE,
					System.currentTimeMillis());
			mContentResolver.update(MetaData.BookTable.uri, cvbook,
					"created_date = ?", selectionArgs);
			// add by wendy 0401 delete-> update end++

			publishProgress();
			// add by wendy 0401 delete -> update begin+++
			if (notebook != null) {
				int num = notebook.getTotalPageNumber();
				for (int i = 0; i < num; i++) {
					Long id = notebook.getPageOrder(i);
					mContentResolver.delete(MetaData.TimestampTable.uri, 
							"owner = ?", new String[]{ id.toString() });
					mContentResolver.delete(MetaData.ItemTable.uri, 
               	 			"_id = ?", new String[]{id.toString()});
					mContentResolver.delete(MetaData.DoodleTable.uri, 
               	 			"_id = ?", new String[]{id.toString()});
               	 	mContentResolver.delete(MetaData.AttachmentTable.uri, 
               	 			"_id = ?", new String[]{id.toString()});
				}
			}
			
			ContentValues cvpage = new ContentValues();
			cvpage.put(MetaData.PageTable.IS_DELETED, 1);
			mContentResolver.update(MetaData.PageTable.uri, cvpage,
					"owner = ?", selectionArgs);
			// add by wendy 0401 delete -> update end---
			publishProgress();

			mBookcase.removeBookFromList(notebook);
			publishProgress();
			
			//Begin Allen for update widget 
			MetaData.updateSuperNoteUpdateInfoSet(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_DELETE_BOOK,bookId);
			//End Allen
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			if(mList == null)
			{
				deleteOneBook(mBookId);
			}
			else
			{
				for(Long bookId : mList)
				{
					deleteOneBook(bookId);
				}
				mList = null;
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			mCount = mCount + 1;
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.setProgress(mCount);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mIsDeleteBookTaskRunning = false;
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				NoteBookPickerActivity.this.removeDialog(DELETE_BOOK_PROGRESS_DIALOG);
				NoteBookPickerActivity.this.updateFragment();
			}
		}

	}

	/********************************************************
	 * Delete NoteBook END
	 *******************************************************/

	// BEGIN: Better
	/********************************************************
	 * Export the data of Notebook to PDF START
	 *******************************************************/
	public void exportNoteToPdf(final Activity activity,
			SortedSet<SimplePageInfo> items) {
		mExport2PdfTask = new Export2PdfTask(activity, items);
		Bundle b = new Bundle();
		b.putBoolean("rename", true);
		showDialog(EXPORT_PDF_CONFIRM_DIALOG, b);
	}

	public void exportNoteToPdf(final Activity activity, ArrayList<Long> bookIds) {
		mExport2PdfTask = new Export2PdfTask(activity, bookIds);
		showDialog(EXPORT_PDF_CONFIRM_DIALOG);
	}

	public void exportNoteToPdf(final Activity activity,
			SortedSet<SimplePageInfo> items, String title) {
		mExport2PdfTask = new Export2PdfTask(activity, items, title);
		showDialog(EXPORT_PDF_CONFIRM_DIALOG);
	}

	private class Export2PdfTask extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog mProgressDialog;
		private SortedSet<SimplePageInfo> mItems;
		private String mFileName;
		private int mCount = 0;
		private int mMax = 0;
		private ArrayList<String> mJpgPathList = null;
		private String mExportDir = "";
		private boolean mIsExportBook = false;
		private ArrayList<Long> mBookIds = null;
		
		private String mTitle = "";
		private static final int MSG_UPDATE_PROGRESS = 0;
		private static final int MSG_EXPORT_TITLE = 1;
		private int mPdfFileCount = 0;
		
		private int mProgressStatus = MSG_UPDATE_PROGRESS;
		
		private ArrayList<String> mFiles = new ArrayList<String>();

		public Export2PdfTask(Activity activity, SortedSet<SimplePageInfo> items) {
			if (items != null) {
				mItems = new TreeSet<SimplePageInfo>(items);
			} else {
				mItems = new TreeSet<SimplePageInfo>();
			}

			mCount = 0;
			// BEGIN: Shane_Wang 2012-10-24
			mMax = mItems.size();
			// END: Shane_Wang 2012-10-24
			
			mPdfFileCount = 0;
		}

		public Export2PdfTask(Activity activity,
				SortedSet<SimplePageInfo> items, String title) {
			if (items != null) {
				mItems = new TreeSet<SimplePageInfo>(items);
			} else {
				mItems = new TreeSet<SimplePageInfo>();
			}
			mFileName = title + MetaData.EXPORT_PDF_EXTENSION;
			mTitle = title;

			mCount = 0;
			// BEGIN: Shane_Wang 2012-10-24
			mMax = mItems.size();
			// END: Shane_Wang 2012-10-24
			
			mPdfFileCount = 0;
		}

		public Export2PdfTask(Activity activity, ArrayList<Long> bookIds) {
			mBookIds = new ArrayList<Long>();
			mBookIds.addAll(bookIds);
			mIsExportBook = true;
			mCount = 0;
			mMax = 0;
			BookCase bookcase = BookCase.getInstance(activity);
			for (long id : mBookIds) {
				NoteBook book = bookcase.getNoteBook(id);
				if (book != null) {
					mMax += book.getTotalPageNumber();
				}
			}
			
			mPdfFileCount = 0;
		}

		public void setFileName(String fileName) {
			mFileName = fileName;
			mTitle = fileName;
			if (!mFileName.toLowerCase()
					.endsWith(MetaData.EXPORT_PDF_EXTENSION)) {
				mFileName += MetaData.EXPORT_PDF_EXTENSION;
			}
		}

		public void setDialog(ProgressDialog dialog) {
			if (mProgressDialog != null) {
				mProgressDialog = null;
			}
			mProgressDialog = dialog;
			
			if (mProgressStatus == MSG_EXPORT_TITLE) {
				mProgressDialog.setMax(mMax);
				String title = NoteBookPickerActivity.this.getResources().getString(R.string.export) + " " + mTitle;
				mProgressDialog.setTitle(title);
			}
			
			mProgressDialog.setProgress(mCount);
		}
		
		//begin smilefish
		public void showSuccessDialog(){
			int promptid = R.string.prompt_export_pdf_to;
			if (mPdfFileCount > 1) {
				promptid = R.string.prompt_exports_pdf_to;
			}
			if (mIsExportBook) {
				String msg = NoteBookPickerActivity.this.getResources()
						.getString(promptid);
				msg = msg + "\n" + mExportDir;
				Bundle b = new Bundle();
				b.putString("msg", msg);
				NoteBookPickerActivity.this.showDialog(EXPORT_PDF_SUCCESS_DIALOG, b);
			} else {
				File file = new File(mFileName);
				if (!file.exists()) {
					NoteBookPickerActivity.this.showDialog(EXPORT_PDF_FAIL_DIALOG);
				} else {
					Uri uri = Uri.parse("file://" + file);
					NoteBookPickerActivity.this.sendBroadcast(new Intent(
							Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
					String msg = NoteBookPickerActivity.this.getResources()
							.getString(promptid);
					msg = msg + "\n" + mFileName.substring(0, mFileName.lastIndexOf('/'));
					Bundle b = new Bundle();
					b.putString("msg", msg);
					NoteBookPickerActivity.this.showDialog(EXPORT_PDF_SUCCESS_DIALOG, b);
				}
			}
		}
		//end smilefish

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsExport2PdfTaskRunning = true;
			Bundle b = new Bundle();
			b.putInt("max", mMax);
			showDialog(EXPORT_PDF_PROGRESS_DIALOG, b);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (values != null) {
				if (values[0] == MSG_EXPORT_TITLE) {
					mProgressStatus = MSG_EXPORT_TITLE;
					
					mProgressDialog.setMax(mMax);
					String title = NoteBookPickerActivity.this.getResources().getString(R.string.export) + " " + mTitle;
					mProgressDialog.setTitle(title);
				}
				
				mProgressDialog.setProgress(mCount);
			}
		}

		private boolean exportPages(File exportDir) {
			// BEGIN: Shane_Wang 2012-10-25
			File tempFile = null;
			// END: Shane_Wang 2012-10-25
	
			try {
				BookCase bookCase = BookCase.getInstance(mContext);
				Long bookId = 0L;
				if (mItems != null) {
					if (mItems.size() > 0) {
						bookId = mItems.first().bookId;
					} else {
						bookId = mBookcase.getCurrentBookId();
					}
					NoteBook noteBook = bookCase.getNoteBook(bookId);
					if (noteBook != null) {
						// BEGIN: Shane_Wang 2012-10-24
						mMax = mItems.size();
						// END: Shane_Wang 2012-10-24
						mCount = 0;
						publishProgress(MSG_EXPORT_TITLE);
						mJpgPathList = new ArrayList<String>();
						for (SimplePageInfo pageInfo : mItems) {
							long pageId = pageInfo.pageId;
							NotePage notePage = noteBook.getNotePage(pageId);
							if (notePage != null) {
								boolean isPhoneScreen = noteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE;
								int width = isPhoneScreen ? (int)getResources().getDimension(R.dimen.phone_page_share_bitmap_default_width)
										: (int)getResources().getDimension(R.dimen.pad_page_share_bitmap_default_width);
								int height = isPhoneScreen ? (int)getResources().getDimension(R.dimen.phone_page_share_bitmap_default_height)
										: (int)getResources().getDimension(R.dimen.pad_page_share_bitmap_default_height);
								Bitmap bitmap = Bitmap.createBitmap(width,
										height, Bitmap.Config.ARGB_8888);
								ArrayList<String> fileList = notePage.getThumbnail(new PageDataLoader(mContext), false, 
										bitmap, false, true, true, null);

								if (!bitmap.isRecycled()) {
									bitmap.recycle();
									bitmap = null;
								}
								
								mJpgPathList.addAll(fileList);
								mFiles.addAll(fileList);
								mCount++;

								if (isCancelled()) {
									return false;
								}
							}
							publishProgress(MSG_UPDATE_PROGRESS);
						}
						// BEGIN: Shane_Wang 2012-10-25
						int pageRange = mJpgPathList.size();
						int PAGE_RANGE = 256;
						int times = (int)((pageRange + PAGE_RANGE - 1) / PAGE_RANGE);
						String[] tmpPath = new String[PAGE_RANGE];
						String baseFileName = mFileName.substring(0, mFileName.length()
								- MetaData.EXPORT_PDF_EXTENSION.length());
						String fileName = "";
						
						tempFile = new File(mExportDir, baseFileName + MetaData.EXPORT_PDF_EXTENSION);
						if (tempFile.exists()) {
							int index = 0;
							while (tempFile.exists()) {
							    baseFileName += "_" + index;
							    tempFile = new File(exportDir, baseFileName + MetaData.EXPORT_PDF_EXTENSION);
							    index++;
						    }
					    }
						
						for(int j = 0; j < times; ++j) {
							if(j + 1 >= times && pageRange % PAGE_RANGE > 0) {
								tmpPath = new String[pageRange % PAGE_RANGE];
							}
							for(int i = 0; i < PAGE_RANGE && PAGE_RANGE*j+i < pageRange; ++i) {
								tmpPath[i] = mJpgPathList.get(PAGE_RANGE*j+i);
							}

							//first book use default fileName.
							if(j == 0) {
								fileName = baseFileName + MetaData.EXPORT_PDF_EXTENSION; // BEGIN: Shane_Wang 2012-11-14
							}else {
								fileName = baseFileName; // BEGIN: Shane_Wang 2012-11-14
								fileName += "_" + Integer.toString(j+1) + MetaData.EXPORT_PDF_EXTENSION; // BEGIN: Shane_Wang 2012-11-14
							}
							
							tempFile = new File(mExportDir, fileName);
							try {
								
								if (!tempFile.exists()) {
									tempFile.createNewFile();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}						
							PdfConverter.getInstance().Jpg2Pdf(tmpPath,
									tempFile.getAbsolutePath());
							mPdfFileCount++;
						}
						// END: Shane_Wang 2012-10-25
						for (String filePath : mJpgPathList) {
							File file = new File(filePath);
							file.delete();
							mFiles.remove(filePath);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try{
				mFileName = tempFile.getPath();
			}catch(Exception e) {
				e.toString();
			}

			return true;
		}

		private boolean exportBooks(File exportDir) {
			BookCase bookCase = BookCase.getInstance(mContext);
			for (long bookId : mBookIds) {
				NoteBook noteBook = bookCase.getNoteBook(bookId);
				if (noteBook != null) {
					mTitle = noteBook.getTitle();
					// BEGIN: Shane_Wang 2012-10-24
					mMax = noteBook.getTotalPageNumber();
					// END: Shane_Wang 2012-10-24;
					mCount = 0;
					publishProgress(MSG_EXPORT_TITLE);
					mFileName = noteBook.getTitle()
							+ MetaData.EXPORT_PDF_EXTENSION;
					
					// BEGIN: Shane_Wang 2012-10-25
					File tempFile = null;
					
					int pageNum = noteBook.getTotalPageNumber();
					mJpgPathList = new ArrayList<String>();
					for (int i = 0; i < pageNum; i++) {
						NotePage notePage = noteBook.getNotePage(noteBook
								.getPageOrder(i));
						if (notePage != null) {
							boolean isPhoneScreen = noteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE;
							int width = isPhoneScreen ? (int)getResources().getDimension(R.dimen.phone_page_share_bitmap_default_width)
									: (int)getResources().getDimension(R.dimen.pad_page_share_bitmap_default_width);
							int height = isPhoneScreen ? (int)getResources().getDimension(R.dimen.phone_page_share_bitmap_default_height)
									: (int)getResources().getDimension(R.dimen.pad_page_share_bitmap_default_height);
							Bitmap bitmap = Bitmap.createBitmap(width, height,
									Bitmap.Config.ARGB_8888);
							ArrayList<String> fileList = notePage.getThumbnail(new PageDataLoader(mContext), false, 
									bitmap, false, true, true, null);

							if (!bitmap.isRecycled()) {
								bitmap.recycle();
								bitmap = null;
							}

							mJpgPathList.addAll(fileList);
							mFiles.addAll(fileList);
							mCount++;

							if (isCancelled()) {
								return false;
							}
						}
						publishProgress(MSG_UPDATE_PROGRESS);
					}
					// BEGIN: Shane_Wang 2012-10-25
					int pageRange = mJpgPathList.size();
					int PAGE_RANGE = 256;
					int times = (int)((pageRange + PAGE_RANGE - 1) / PAGE_RANGE);
					String[] tmpPath = new String[PAGE_RANGE];
					String baseFileName = mFileName.substring(0, mFileName.length()
							- MetaData.EXPORT_PDF_EXTENSION.length());
					String fileName = "";
					
					tempFile = new File(mExportDir, baseFileName + MetaData.EXPORT_PDF_EXTENSION);
					if (tempFile.exists()) {
						int index = 0;
						while (tempFile.exists()) {
						    baseFileName += "_" + index;
						    tempFile = new File(exportDir, baseFileName + MetaData.EXPORT_PDF_EXTENSION);
						    index++;
					    }
				    }
					
					for(int j = 0; j < times; ++j) {
						if(j + 1 >= times && pageRange % PAGE_RANGE > 0) {
							tmpPath = new String[pageRange % PAGE_RANGE];
						}
						for(int i = 0; i < PAGE_RANGE && PAGE_RANGE*j+i < pageRange; ++i) {
							tmpPath[i] = mJpgPathList.get(PAGE_RANGE*j+i);
						}

						//first book use default fileName.
						if(j == 0) {
							fileName = baseFileName + MetaData.EXPORT_PDF_EXTENSION; // BEGIN: Shane_Wang 2012-11-14
						}else {
							fileName = baseFileName; // BEGIN: Shane_Wang 2012-11-14
							fileName += "_" + Integer.toString(j+1) + MetaData.EXPORT_PDF_EXTENSION; // BEGIN: Shane_Wang 2012-11-14
						}
						
						tempFile = new File(mExportDir, fileName);
						try {
							
							if (!tempFile.exists()) {
								tempFile.createNewFile();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}						
						PdfConverter.getInstance().Jpg2Pdf(tmpPath,
								tempFile.getAbsolutePath());
						
						mFileName = tempFile.getAbsolutePath();
						File file = new File(mFileName);
						Uri uri = Uri.parse("file://" + file);
						sendBroadcast(new Intent(
								Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
						mPdfFileCount++;
					}
					// END: Shane_Wang 2012-10-25
					
					for (String filePath : mJpgPathList) {
						File file = new File(filePath);
						file.delete();
						mFiles.remove(filePath);
					}
					if (isCancelled()) {
						return false;
					}
				}
			}

			return true;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if (Looper.myLooper() == null) {
				Looper.prepare();
			}
			
			mExportDir = mPreference.getString(
					mResources.getString(R.string.pref_export_pdf_dir),
					MetaData.EXPORT_PDF_DIR);
			File exportDir = new File(mExportDir);
			exportDir.mkdirs();
			if (mIsExportBook) {
				return exportBooks(exportDir);
			} else {
				return exportPages(exportDir);
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mIsExport2PdfTaskRunning = false;
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				NoteBookPickerActivity.this.removeDialog(EXPORT_PDF_PROGRESS_DIALOG);
				int promptid = R.string.prompt_export_pdf_to;
				if (mPdfFileCount > 1) {
					promptid = R.string.prompt_exports_pdf_to;
				}
				if (mIsExportBook) {
					String msg = NoteBookPickerActivity.this.getResources()
							.getString(promptid);
					msg = msg + "\n" + mExportDir;
					Bundle b = new Bundle();
					b.putString("msg", msg);
					NoteBookPickerActivity.this.showDialog(EXPORT_PDF_SUCCESS_DIALOG, b);
				} else {
					File file = new File(mFileName);
					if (!file.exists()) {
						NoteBookPickerActivity.this.showDialog(EXPORT_PDF_FAIL_DIALOG);
					} else {
						Uri uri = Uri.parse("file://" + file);
						NoteBookPickerActivity.this.sendBroadcast(new Intent(
								Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
						String msg = NoteBookPickerActivity.this.getResources()
								.getString(promptid);
						msg = msg + "\n" + mFileName.substring(0, mFileName.lastIndexOf('/'));
						Bundle b = new Bundle();
						b.putString("msg", msg);
						NoteBookPickerActivity.this.showDialog(EXPORT_PDF_SUCCESS_DIALOG, b);
					}
				}
			}
		}

		@Override
		protected void onCancelled(Boolean result) {
			mIsExport2PdfTaskRunning = false;

			for (String path : mFiles) {
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				}
			}
			
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				NoteBookPickerActivity.this.removeDialog(EXPORT_PDF_PROGRESS_DIALOG);
				NoteBookPickerActivity.this.updateFragment();
			}
		}
	}

	/********************************************************
	 * Export the data of Notebook to PDF END
	 *******************************************************/
	// END: Better
	/********************************************************
	 * Export the data of SuperNote START
	 *******************************************************/

	// BEGIN: Better
	public void exportNote(final Activity activity,
			SortedSet<SimplePageInfo> items) {
		mExportTask = new ExportTask(activity, items);
		Bundle b = new Bundle();
		b.putBoolean("rename", true);
		showDialog(EXPORT_CONFIRM_DIALOG, b);
	}

	// END: Better

	public void exportNote(final Activity activity,
			SortedSet<SimplePageInfo> items, String title) {
		mExportTask = new ExportTask(activity, items, title);
		showDialog(EXPORT_CONFIRM_DIALOG);
	}

	public void exportNote(final Activity activity, ArrayList<Long> bookIds) {
		mExportTask = new ExportTask(activity, bookIds);
		Bundle b = new Bundle();
		if (bookIds.size() > 1) {
			b.putBoolean("rename", true);
		}
		showDialog(EXPORT_CONFIRM_DIALOG, b);
	}

	private class ExportTask extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog mProgressDialog;
		private SortedSet<SimplePageInfo> mItems;
		private String mFileName;
		// BEGIN: Better
		private int mMax = 0;
		private int mProgress = 0;
		private String mExportDir = "";
		// END: Better
		private boolean mbIsBooks = false;
		private ArrayList<Long> mbookIds;
		
		private String mTitle = "";
		private static final int MSG_UPDATE_PROGRESS = 0;
		private static final int MSG_TRANSFER_TITLE = 1;
		private static final int MSG_EXPORT_TITLE = 2;
		
		private int mProgressStatus = MSG_UPDATE_PROGRESS;
		
		private ArrayList<String> mFiles = new ArrayList<String>();

		public ExportTask(Activity activity, SortedSet<SimplePageInfo> items) {
			if (items != null) {
				mItems = new TreeSet<SimplePageInfo>(items);
			} else {
				mItems = new TreeSet<SimplePageInfo>();
			}
			mFileName = MetaData.EXPORT_PREFIX + "_"
					+ System.currentTimeMillis() + MetaData.EXPORT_EXTENSION;

			// BEGIN: Shane_Wang@asus.com 2013-1-15
			mMax = mItems.size();
			// END: Shane_Wang@asus.com 2013-1-15
		}

		public ExportTask(Activity activity, SortedSet<SimplePageInfo> items,
				String title) {
			if (items != null) {
				mItems = new TreeSet<SimplePageInfo>(items);
			} else {
				mItems = new TreeSet<SimplePageInfo>();
			}
			// BEGIN: Better
			
			// BEGIN: Shane_Wang@asus.com 2013-1-15
			mMax = mItems.size();
			// END: Shane_Wang@asus.com 2013-1-15
			
			mFileName = title + MetaData.EXPORT_EXTENSION;
			mTitle = title;
			// END: Better
		}

		public ExportTask(Activity activity, ArrayList<Long> bookIds) {
			mbIsBooks = true;
			
			//emmanual to fix bug 488402
			if(mbookIds == null){
				mbookIds = new ArrayList<Long>();
			}else{
				mbookIds.clear();
			}
			for (Long id : bookIds) {
				mbookIds.add(id);
			}
			
			mMax = 1;
		}

		public void setFileName(String fileName) {
			mFileName = fileName;
			mTitle = fileName;
		}

		public void setDialog(ProgressDialog d) {
			if (mProgressDialog != null) {
				mProgressDialog = null;
			}
			mProgressDialog = d;
			
			if (mProgressStatus == MSG_TRANSFER_TITLE) {
				mProgressDialog.setMax(mMax);
				String title = mResources.getString(R.string.transfer) + " " + mTitle;
				mProgressDialog.setTitle(title);
			} else if (mProgressStatus == MSG_EXPORT_TITLE) {
				mProgressDialog.setMax(mMax); 
				String title = mResources.getString(R.string.export) + " " + mTitle;
				mProgressDialog.setTitle(title);
			}
			
			mProgressDialog.setProgress(mProgress);
		}

		//begin smilefish
		public void showSuccessDialog(){
			File file = new File(mFileName);
			if (file.exists() == false) {
				NoteBookPickerActivity.this.showDialog(EXPORT_FAIL_DIALOG);
			} else {
				Uri uri = Uri.parse("file://" + file);
				NoteBookPickerActivity.this.sendBroadcast(new Intent(
						Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
				String msg = NoteBookPickerActivity.this.getResources().getString(
						R.string.prompt_export_to_);
				msg = msg + "\n" + mExportDir;// darwin
				Bundle b = new Bundle();
				b.putString("msg", msg);
				NoteBookPickerActivity.this.showDialog(EXPORT_SUCCESS_DIALOG, b);
			}
		}
		//end smilefish
		
		// BEGIN: Better
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (values != null) {
				if (values[0] == MSG_TRANSFER_TITLE) {
					mProgressStatus = MSG_TRANSFER_TITLE;
					mProgressDialog.setMax(mMax);
					String title = NoteBookPickerActivity.this.getResources().getString(R.string.transfer) + " " + mTitle;
					mProgressDialog.setTitle(title);
				} else if (values[0] == MSG_EXPORT_TITLE) {
					mProgressStatus = MSG_EXPORT_TITLE;
					mProgressDialog.setMax(mMax); 
					String title = NoteBookPickerActivity.this.getResources().getString(R.string.export) + " " + mTitle;
					mProgressDialog.setTitle(title);
				}
				
				mProgressDialog.setProgress(mProgress);
			}
		}
		// EDN: Better

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsExportTaskRunning = true;
			Bundle b = new Bundle();
			b.putInt("max", mMax);
			showDialog(EXPORT_PROGRESS_DIALOG, b);
			//emmanual to fix bug 465134
			PickerUtility.lockRotation(NoteBookPickerActivity.this);
		}
		
		private void saveBooks(String filename) {
			if (isCancelled()) {
				return;
			}
			
			String ext = mbookIds.size() > 1 ? MetaData.MUTLI_EXPORT_EXTENSION : MetaData.EXPORT_EXTENSION;
			
			mExportDir = mPreference.getString(
					mResources.getString(R.string.pref_export_dir),
					MetaData.EXPORT_DIR);
			File exportDir = new File(mExportDir);

			exportDir.mkdirs();
			//emmanual to fix bug 508366
			if(filename.equals("")){
				filename = "0";
			}
			File exportZip = new File(exportDir, filename + ext);
			
			if (exportZip.exists()) {
				int index = 0;
				while (exportZip.exists()) {
					exportZip = new File(exportDir, filename + "_" + index + ext);
					index++;
				}
			}
			
			try {
				exportZip.createNewFile();
				mFiles.add(exportZip.getAbsolutePath());
				
				if (isCancelled()) {
					return;
				}
				
				BookCase bookCase = BookCase.getInstance(mContext);
				ZipOutputStream zos = new ZipOutputStream(
						new FileOutputStream(exportZip));
				
				ZipEntry ze = new ZipEntry(MetaData.VERSION);
				zos.putNextEntry(ze);
				zos.write((MetaData.SNE_VERSION_3 >> 8) & 0x00FF);
				zos.write(MetaData.SNE_VERSION_3 & 0x00FF);
				
				if (isCancelled()) {
					zos.close();
					return;
				}
				
				ByteArrayOutputStream baos = null;
				AsusFormatWriter afw = null;
				
				if (mbookIds.size() > 1) {
					baos = new ByteArrayOutputStream();
					afw = new AsusFormatWriter(baos);
					afw.writeByteArray(AsusFormat.SNF_BOOKLIST_BEGIN, null, 0, 0);
					for (long bookId : mbookIds) {
						NoteBook book = bookCase.getNoteBook(bookId);
						if (book != null) {
							afw.writeByteArray(AsusFormat.SNF_BOOKINFO_BEGIN, null, 0, 0);
							afw.writeLong(AsusFormat.SNF_BOOKINFO_ID, bookId);
							afw.writeString(AsusFormat.SNF_BOOKINFO_TITLE, book.getTitle());
							
							afw.writeByteArray(AsusFormat.SNF_BOOKINFO_END, null, 0, 0);
						}
					}
			        afw.writeByteArray(AsusFormat.SNF_BOOKLIST_END, null, 0, 0);
			        
			        ze = new ZipEntry(MetaData.BOOK_LIST);
			        zos.putNextEntry(ze);
			        zos.write(baos.toByteArray());
			        baos.close();
				}
		        
		        if (isCancelled()) {
		        	zos.close();
					return;
				}
		        
		        for (long bookId : mbookIds) {
					NoteBook book = bookCase.getNoteBook(bookId);
					if (book != null) {
						mTitle = book.getTitle();
						mMax = book.getTotalPageNumber();
						SortedSet<SimplePageInfo> selectedItems = new TreeSet<SimplePageInfo>(
								new PageComparator());
						int totalpageNum = book.getTotalPageNumber();
						for (int i = 0; i < totalpageNum; i++) {
							long pageId = book.getPageOrder(i);
							NotePage page = book.getNotePage(pageId);
							if (page != null) {
								SimplePageInfo pageInfo = new SimplePageInfo(
										bookId, pageId, 0, i);
								if (selectedItems.contains(pageInfo) == false) {
									selectedItems.add(pageInfo);
								}
							}
						}
						book.setSelectedItems(selectedItems);
						ArrayList<NoteBook> bookList = new ArrayList<NoteBook>();
						bookList.add(book);
						bookCase.setSelectedBookList(bookList);
						
						if (isCancelled()) {
							zos.close();
							return;
						}
						
						if (book.getVersion() == 1) {
							mProgress = 0;
							publishProgress(MSG_TRANSFER_TITLE);
							PageDataLoader loader = new PageDataLoader(mContext);
							for (SimplePageInfo info : mItems) {
								NotePage page = book.getNotePage(info.pageId);
								if ((page != null) && (page.getVersion() == 1)) {
									loader.load(page);
									page.save(loader.getAllNoteItems(),
											loader.getDoodleItem());
									genThumb(loader, true, 
											page,
											book.getPageSize() == MetaData.PAGE_SIZE_PHONE);
									mProgress++;
									publishProgress(MSG_UPDATE_PROGRESS);
								}
								if (isCancelled()) {
									zos.close();
									return;
								}
							}
							book.setVersion(3);
							book.save();
							File file = new File(MetaData.DATA_DIR
									+ book.getCreatedTime(), "paintbook");
							if (file.exists()) {
								file.delete();
							}
						}
						
						
						mProgress = 0;
						publishProgress(MSG_EXPORT_TITLE);
						
				        baos = new ByteArrayOutputStream();
				        afw = new AsusFormatWriter(baos);
						bookCase.itemSave(afw);
						String entryName = null;
						if (mbookIds.size() > 1) {
							entryName = bookId + MetaData.BOOK_INFO_EXT;
						} else {
							entryName = MetaData.BOOK_INFORMATION;
						}
						ze = new ZipEntry(entryName);
						zos.putNextEntry(ze);
						zos.write(baos.toByteArray());
						baos.close();
						
						if (isCancelled()) {
							zos.close();
							return;
						}
						zipDirCover(this, (MetaData.DATA_DIR + bookId), zos, bookId);//darwin
						int pageNum = book.getTotalPageNumber();
						for (int i = 0; i < pageNum; i++) {
							long pageId = book.getPageOrder(i);
							String dirToZip = MetaData.DATA_DIR + bookId + "/"
									+ pageId;
							zipDir(this, dirToZip, zos, bookId, pageId);
							
							if (isCancelled()) {
								zos.close();
								return;
							}
							
							mProgress++;
							publishProgress(MSG_UPDATE_PROGRESS);
						}
					}
				}

				zos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			mFileName = exportZip.getPath();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if (Looper.myLooper() == null) {
				Looper.prepare();
			}
			
			if (mbIsBooks) {
				if (mbookIds.size() == 1) {
					Long id = mbookIds.get(0);
					NoteBook book = BookCase.getInstance(mContext).getNoteBook(id);
					if (book != null) {
						mTitle = book.getTitle();
						mFileName = mTitle;
					} else {
						return false;
					}
				}
				saveBooks(mFileName);
				mbIsBooks = false;
				return true;
			}
			// BEGIN: Better
			if (isCancelled()) {
				return false;
			}
			
			// BEGIN: Shane_Wang@asus.com 2013-1-15
			mFileName += MetaData.EXPORT_EXTENSION;
			// END: Shane_Wang@asus.com 2013-1-15
			mExportDir = mPreference.getString(
					mResources.getString(R.string.pref_export_dir),
					MetaData.EXPORT_DIR);
			File exportDir = new File(mExportDir);
			// END: Better
			exportDir.mkdirs();
			File exportZip = new File(exportDir, mFileName);
			
			if (exportZip.exists()) {
				int index = 0;
				String name = mFileName.substring(0, mFileName.length()
						- MetaData.EXPORT_EXTENSION.length());
				while (exportZip.exists()) {
					mFileName = name + "_" + index + MetaData.EXPORT_EXTENSION;
					exportZip = new File(exportDir, mFileName);
					index++;
				}
			}
			
			try {
				exportZip.createNewFile();
				mFiles.add(exportZip.getAbsolutePath());
				
				if (isCancelled()) {
					return false;
				}
				
				ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(
						exportZip));

				// [START]New book format
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				BookCase bookCase = BookCase.getInstance(mContext);
				ArrayList<NoteBook> bookList = new ArrayList<NoteBook>();
				Long bookId = 0L;
				if (mItems != null) {
					if (mItems.size() > 0) {
						bookId = mItems.first().bookId;
					} else {
						// wendy begin
						bookId = mBookcase.getCurrentBookId();
						// end wendy
					}
					bookList.add(bookCase.getNoteBook(bookId));
					NoteBook book = bookCase.getNoteBook(bookId);
					book.setSelectedItems(mItems);
					// BEGIN: Better
					if (book.getVersion() == 1) {
						mProgress = 0;
						publishProgress(MSG_TRANSFER_TITLE);
						PageDataLoader loader = new PageDataLoader(mContext);
						for (SimplePageInfo info : mItems) {
							NotePage page = book.getNotePage(info.pageId);
							if ((page != null) && (page.getVersion() == 1)) {
								loader.load(page);
								page.save(loader.getAllNoteItems(),//Allen
										loader.getDoodleItem());
								genThumb(loader, true, page,
										book.getPageSize() == MetaData.PAGE_SIZE_PHONE);
								mProgress++;
								publishProgress(MSG_UPDATE_PROGRESS);
							}
						}
						book.setVersion(3);
						book.save();
						File file = new File(MetaData.DATA_DIR
								+ book.getCreatedTime(), "paintbook");
						if (file.exists()) {
							file.delete();
						}
					}
					// END: Better
				}

				// BEGIN: Better
				if (isCancelled()) {
					zos.close();
					return false;
				}

				mProgress = 0;
				publishProgress(MSG_EXPORT_TITLE);
				// END: Better
				
				// [START]Add a zipEntry to the zip file to distinguish SNE
				// version
				ZipEntry ze = new ZipEntry(MetaData.VERSION);
				zos.putNextEntry(ze);
				zos.write((MetaData.SNE_VERSION_3 >> 8) & 0x00FF);
				zos.write(MetaData.SNE_VERSION_3 & 0x00FF);
				// [END]Add a zipEntry to the zip file to distinguish SNE
				// version
				
				bookCase.setSelectedBookList(bookList);
				AsusFormatWriter afw = new AsusFormatWriter(baos);
				bookCase.itemSave(afw);
				ze = new ZipEntry(MetaData.BOOK_INFORMATION);
				zos.putNextEntry(ze);
				zos.write(baos.toByteArray());
				baos.close();

				// BEGIN: Better
				if (isCancelled()) {
					zos.close();
					return false;
				}
				// END: Better

				// [END]New book format
				for (SimplePageInfo info : mItems) {
					// BEGIN: Better
					if (isCancelled()) {
						zos.close();
						return false;
					}
					// END: Better
					String dirToZip = MetaData.DATA_DIR + info.bookId + "/"
							+ info.pageId;
					zipDir(this, dirToZip, zos, bookId, info.pageId);//noah：来自不同book的几页，也保存在同一本书�?
//					zipDir(this, dirToZip, zos, info.bookId, info.pageId);
					// BEGIN: Better
					mProgress++;
					publishProgress(MSG_UPDATE_PROGRESS);
					// END: Better
				}
				zos.close();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			mFileName = exportZip.getPath();
			return true;
		}

		// BEGIN: Better
		@Override
		protected void onCancelled(Boolean result) {
			mIsExportTaskRunning = false;
			for (String path : mFiles) {
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				}
			}
			
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				NoteBookPickerActivity.this.removeDialog(EXPORT_PROGRESS_DIALOG);
				NoteBookPickerActivity.this.updateFragment();
			}
			//emmanual to fix bug 465134
			PickerUtility.unlockRotation(NoteBookPickerActivity.this);
		}
		// END: Better

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mIsExportTaskRunning = false;
			//emmanual to fix bug 465134
			PickerUtility.unlockRotation(NoteBookPickerActivity.this);
			// BEGIN: archie_huang@asus.com
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				NoteBookPickerActivity.this.removeDialog(EXPORT_PROGRESS_DIALOG);
				if (result) {
					File file = new File(mFileName);
					if (file.exists() == false) {
						NoteBookPickerActivity.this.showDialog(EXPORT_FAIL_DIALOG);
					} else {
						Uri uri = Uri.parse("file://" + file);
						NoteBookPickerActivity.this.sendBroadcast(new Intent(
								Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
						String msg = NoteBookPickerActivity.this.getResources().getString(
								R.string.prompt_export_to_);
						msg = msg + "\n" + mExportDir;// darwin
						Bundle b = new Bundle();
						b.putString("msg", msg);
						NoteBookPickerActivity.this.showDialog(EXPORT_SUCCESS_DIALOG, b);
					}
				} else {
					NoteBookPickerActivity.this.showDialog(EXPORT_FAIL_DIALOG);
				}
			}
			// END: archie_huang@asus.com
		}

		private void zipDirCover(ExportTask task, String dir, ZipOutputStream zos,Long bookId) {
			File zipDir = new File(dir);

			byte[] readBuffer = new byte[2048];
			int byteIn = 0;
				File f = new File(zipDir, MetaData.THUMBNAIL_COVER);

				try {
					FileInputStream fis = new FileInputStream(f);
					BufferedInputStream bis = new BufferedInputStream(fis);
					String entryName = "" + bookId + "/"
							+ f.getName();
					ZipEntry ze = new ZipEntry(entryName);
					zos.putNextEntry(ze);
					while ((byteIn = bis.read(readBuffer)) != -1) {
						zos.write(readBuffer, 0, byteIn);
					}
					bis.close();
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			
		}
		
		private void zipDir(ExportTask task, String dir, ZipOutputStream zos,
				Long bookId, Long pageId) {
			File zipDir = new File(dir);
			String[] files = zipDir.list();
			if (files == null) {
				return;
			}
			byte[] readBuffer = new byte[2048];
			int byteIn = 0;
			for (String file : files) {
				File f = new File(zipDir, file);
				if (f.isDirectory()) {
					zipDir(task, f.getPath(), zos, bookId, pageId);
					continue;
				}
				try {
					FileInputStream fis = new FileInputStream(f);
					BufferedInputStream bis = new BufferedInputStream(fis);
					String entryName = "" + bookId + "/" + pageId + "/"
							+ f.getName();
					ZipEntry ze = new ZipEntry(entryName);
					zos.putNextEntry(ze);
					while ((byteIn = bis.read(readBuffer)) != -1) {
						zos.write(readBuffer, 0, byteIn);
					}
					bis.close();
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	/********************************************************
	 * Export the data of SuperNote END
	 *******************************************************/

	/********************************************************
	 * Import the data of SuperNote START
	 *******************************************************/
	public void importNote(int version, String path, boolean isSnb) {
		Bundle b = new Bundle();
		b.putString("path", path);
		b.putInt("version", version);
		b.putBoolean("issnb", isSnb);
		showDialog(IMPORT_CONFIRM_DIALOG, b);
	}
	
	private class DownloadFromGoogleTask extends AsyncTask<Void, Void, String> {
		public static final int IMPORT_PDF_CODE = 2001;
		public static final int INSERT_IMAGE_CODE = 2002;
		public static final int INSERT_VOICE_CODE = 2003;
		
		private Uri mUri;
		private String mPath;
		private int mCode;
		private ProgressDialog mProgressDialog;
		
		public DownloadFromGoogleTask(Uri uri, String path, int code) {
			mUri = uri;
			mPath = path;
			mCode = code;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(NoteBookPickerActivity.this);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setMessage(NoteBookPickerActivity.this.getString(R.string.download_title));
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
						DownloadFromGoogleTask.this.cancel(true);
				}
			};
			mProgressDialog.setButton(Dialog.BUTTON_NEGATIVE,
					NoteBookPickerActivity.this.getString(android.R.string.cancel), listener);
			mProgressDialog.create();
			mProgressDialog.show();
			PickerUtility.lockRotation(NoteBookPickerActivity.this);
		}

		@Override
        protected String doInBackground(Void... arg0) {
			try {
				File file = new File(mPath);
				InputStream stream = NoteBookPickerActivity.this
				        .getContentResolver().openInputStream(mUri);
				File dir = new File(MetaData.CROP_TEMP_DIR);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				convertStreamToFile(stream, file);
			} catch (Exception e) {

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
			PickerUtility.unlockRotation(NoteBookPickerActivity.this);
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}
        }

		@Override
        protected void onPostExecute(String result) {
	        super.onPostExecute(result);
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}
			switch (mCode) {
			case IMPORT_PDF_CODE:
				if (!mIsReadPdfTaskRunning) {
					mIsReadPdfTaskRunning = true;
					mReadPdfTask = new ReadPdfTask(mPath);
					mReadPdfTask.execute();
				}
				break;
			case INSERT_IMAGE_CODE:
				String croppedPath = "";
				Uri selectedImage = null;
				Intent intent = new Intent("com.android.camera.action.CROP");
				croppedPath = Uri.decode(pathForCroppedPhoto(mContext, mPath));
				selectedImage = Uri.fromFile(new File(mStrPath));
				intent.setDataAndType(selectedImage, "image/*");
				intent.putExtra("aspectX", 1);
				intent.putExtra("aspectY", 1);
				final Uri croppedPhotoUri = Uri.fromFile(new File(croppedPath));

				intent.putExtra(MediaStore.EXTRA_OUTPUT, croppedPhotoUri);
				try {
					startActivityForResult(intent, REQUEST_CROP_ICON);
				} catch (Exception e) {
				}
				break;
			case INSERT_VOICE_CODE:
				break;
			default:
				break;
			}
        }
	}

	//Emmanual
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
			mProgressDialog = new ProgressDialog(NoteBookPickerActivity.this);
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
			PickerUtility.lockRotation(NoteBookPickerActivity.this);
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
						prepaerImaprtPdf(mPath, false);
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
			PickerUtility.unlockRotation(NoteBookPickerActivity.this);
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}
			if (pdfBitmapList != null) {
				pdfBitmapList.clear();
			}
			mIsReadPdfTaskRunning = false;
			if (error) {
				Builder builder = new AlertDialog.Builder(
				        NoteBookPickerActivity.this);
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
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}
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
		return (int) (NoteBookPickerActivity.this.getResources().getDimension(
		        R.dimen.select_dlg_grid_widthheight) + 0.5);
	}
	
	private void prepaerImaprtPdf(final String path, boolean isNew) {
		PickerUtility.lockRotation(NoteBookPickerActivity.this);
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

	public void importPdf() {
		mImportPdfTask = new ImportPdfTask();
		mImportPdfTask.execute();
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
				convertView = View.inflate(NoteBookPickerActivity.this,
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
	
	private class ImportPdfTask extends AsyncTask<Void, Integer, Boolean> {
		private ProgressDialog mProgressDialog;
		private Long mBookId;

		public ImportPdfTask() {

        }

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsImportPdfTaskRunning = true;

			mProgressDialog = new ProgressDialog(mContext);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setMessage(getString(R.string.import_pdf_prompt));
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (mIsImportPdfTaskRunning) {
						ImportPdfTask.this.cancel(true);
						mIsImportPdfTaskRunning = false;
					}
				}
			};
			mProgressDialog.setButton(Dialog.BUTTON_NEGATIVE,
			        getResources().getString(android.R.string.cancel), listener);
			mProgressDialog.create();
			mProgressDialog.show();
			PickerUtility.lockRotation(NoteBookPickerActivity.this);
		}

		public boolean saveBitmap(Bitmap bm, File f) {
			if (f.exists()) {
				f.delete();
			}
			try {
				FileOutputStream out = new FileOutputStream(f);
				bm.compress(Bitmap.CompressFormat.PNG, 90, out);
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
		
		@Override
        protected Boolean doInBackground(Void... arg0) {
			File file = new File(mReadpdfPath);
			if(!file.exists()){
				Toast.makeText(NoteBookPickerActivity.this,R.string.import_export_book_fail_reason, Toast.LENGTH_LONG).show();
				return false;
			}
			BookCase bookCase = BookCase.getInstance(getApplicationContext());
			NoteBook notebook = new NoteBook(mContext);
			notebook.setTemplate(MetaData.Template_type_blank);
			notebook.setGridType(MetaData.BOOK_GRID_BLANK);
			String filename = file.getName();
			if(filename.contains(".")){
				int dot = filename.indexOf(".");
				filename = filename.substring(0, dot);
			}
			notebook.setTitle(filename);
			notebook.setPageSize(PickerUtility.getDeviceType(mContext) > 100 ? MetaData.PAGE_SIZE_PAD
			        : MetaData.PAGE_SIZE_PHONE);
			bookCase.addNewBook(notebook);
			mBookId = notebook.getCreatedTime();

			boolean isPhoneScreen = notebook.getPageSize() == MetaData.PAGE_SIZE_PHONE;
			int displayWidth = isPhoneScreen ? (int)getResources().getDimension(R.dimen.phone_page_share_bitmap_default_width)
					: (int)getResources().getDimension(R.dimen.pad_page_share_bitmap_default_width);
			int displayHeight = isPhoneScreen ? (int)getResources().getDimension(R.dimen.phone_page_share_bitmap_default_height)
					: (int)getResources().getDimension(R.dimen.pad_page_share_bitmap_default_height);
 
			try {
				PdfRenderer renderer = new PdfRenderer(
				        ParcelFileDescriptor.open(file,
				                ParcelFileDescriptor.MODE_READ_ONLY));
				Bitmap mBitmap = null;
				PageDataLoader loader = new PageDataLoader(mContext);
				int width = 0;
				int height = 0;
				List<Integer> checkIndexList = new ArrayList<Integer>();
				for (int i = 0; i < pdfCheckList.size(); i++) {
					if (pdfCheckList.get(i)) {
						checkIndexList.add(i);
					}
				}
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

						width = (int) (page.getWidth() * s);
						height = (int) (page.getHeight() * s);
					}
					if (width == 0 && height == 0) {
						width = displayWidth;
						height = displayHeight;
					}

					// say we render for showing on the screen
					mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
					page.render(mBitmap, null, null, Page.RENDER_MODE_FOR_DISPLAY);
					
					// do stuff with the bitmap
					NotePage notepage = new NotePage(mContext, notebook.getCreatedTime());	
					notepage.setPageColor(notebook.getBookColor());
					notepage.setPageStyle(notebook.getGridType());
					String pagePath = notepage.getFilePath();
					String fileName = GalleryAttacher.GALLERY_PREFIX_NAME
					        + System.currentTimeMillis() + "."
					        + GalleryAttacher.GALLERY_FILE_EXTENSION;
					File bmpFile = new File(pagePath, fileName);
					if (saveBitmap(mBitmap, bmpFile)) {
						try {
							BitmapFactory.Options option = new BitmapFactory.Options();
							option.inJustDecodeBounds = true;
							BitmapFactory.decodeFile(bmpFile.getPath(), option);
							int picWidth = option.outWidth;
							int picHeight = option.outHeight;

							File doodlefile = new File(pagePath,
							        MetaData.DOODLE_ITEM_PREFIX);
							doodlefile.createNewFile();
							FileOutputStream fos = new FileOutputStream(doodlefile);
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
							return false;
						}
					}
					
					// close the page
					page.close();
					mBitmap.recycle();
					mBitmap = null;
					notepage.genAirViewThumb(loader, notebook);
					if (notebook.getPageNum() == 0) {
						notebook.saveBookCoverThumb(loader, false, notepage);
					}
					notebook.addPage(notepage);
					notebook.savePageThumb(loader, false, notepage);
				}
				
				// close the renderer
				renderer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				ImportPdfTask.this.cancel(true);
				Toast.makeText(NoteBookPickerActivity.this,R.string.prompt_import_err, Toast.LENGTH_SHORT).show();
		        return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				ImportPdfTask.this.cancel(true);
				Toast.makeText(NoteBookPickerActivity.this,R.string.prompt_import_err, Toast.LENGTH_SHORT).show();
		        return false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				ImportPdfTask.this.cancel(true);
				Toast.makeText(NoteBookPickerActivity.this,R.string.prompt_import_err, Toast.LENGTH_SHORT).show();
		        return false;
			}
	        return true;
        }

		@Override
		protected void onCancelled(Boolean result) {
			BookCase bookcase = BookCase.getInstance(NoteBookPickerActivity.this);
			NoteBook book = bookcase.getNoteBook(mBookId);
			if (book != null) {
				List<Long> list = new ArrayList<Long>();
				list.add(mBookId);
				bookcase.delBooks(list);
			}

			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}

			NoteBookPickerActivity.this.updateFragment();
			PickerUtility.unlockRotation(NoteBookPickerActivity.this);
			mIsImportPdfTaskRunning = false;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				String msg = NoteBookPickerActivity.this.getResources().getString(R.string.import_pdf_progress);
				mProgressDialog.setMessage(String.format(msg, values[0], values[1]));
			}
		}

		@Override
        protected void onPostExecute(Boolean result) {
	        // TODO Auto-generated method stub
	        super.onPostExecute(result);
			NoteBookPickerActivity.this.updateFragment();
			mIsImportPdfTaskRunning = false;
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}
			if(NoteBookPickerActivity.this != null && !NoteBookPickerActivity.this.isFinishing()){
				NoteBookPickerActivity.this.showDialog(IMPORT_PDF_SUCCESS_DIALOG);
			}
			PickerUtility.unlockRotation(NoteBookPickerActivity.this);
        }		
	}
	
	private class ImportNewFileTask extends ImportFileTask {
		private ProgressDialog mProgressDialog;
		private String mPath;
		private ZipFile mZipFile;
		private ArrayList<Long> mBookIds = new ArrayList<Long>();
		private ArrayList<Long> mImportedBookIds = new ArrayList<Long>();
		private boolean mIsSnb = false;
		private SQLiteDatabase mDB = null;
		private int mVersion = 0;
		
		public ImportNewFileTask(String path, ArrayList<Long> bookIds, boolean isSnb, int version) {
			File file = new File(path);
			if (file.exists() == false) {
				removeDialog(IMPORT_CONFIRM_DIALOG);
				showDialog(IMPORT_FAIL_DIALOG);
				cancel(true);
			} else {
				mPath = path;
				if ((bookIds != null) && (!bookIds.isEmpty())) {
					mBookIds.addAll(bookIds);
				}
			}
			mIsSnb = isSnb;
			mVersion = version;
		}

		@Override
		public void setDialog(ProgressDialog d) {
			mProgressDialog = d;
		}
		
		//begin smilefish
		@Override
		public void showSuccessDialog(){
			//emmanual to fix bug 482086
			if(NoteBookPickerActivity.this != null && !NoteBookPickerActivity.this.isFinishing()){
				NoteBookPickerActivity.this.showDialog(IMPORT_SUCCESS_DIALOG);
			}
		}
		//end smilefish

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsImportTasRunning = true;
			showDialog(IMPORT_PROGRESS_DIALOG);
		}
		
		private boolean importSnb(Long id) throws Exception {
			BookCase bookCase = BookCase.getInstance(getApplicationContext());
			
			if (isCancelled()) {
				return false;
			}
			
			Cursor cursor = mDB.query(MetaData.BOOK_TABLE, 
					null, "(created_date = ?)", new String[] {id.toString()}, null, null, null);
			cursor.moveToFirst();
			if (cursor.isAfterLast()) {
				cursor.close();
				return false;
			}
			List<Long> pageIdList = new LinkedList<Long>();
			int index = cursor.getColumnIndex(MetaData.BookTable.PAGE_ORDER);
	    	if (index >= 0) {
		        byte[] data = cursor.getBlob(index);
		        // translate old data to new data
		        if (data != null) {
		            try {
		                ByteArrayInputStream bais = new ByteArrayInputStream(data);
		                ObjectInputStream ois = new ObjectInputStream(bais);
		                PageOrderList list = (PageOrderList) ois.readObject();
		                pageIdList = list.getList();
		                ois.close();
		                bais.close();
		            } catch (Exception e) {
		                // read object failed, so read byte
		                ByteArrayInputStream bais = new ByteArrayInputStream(data);
		                DataInputStream dis = new DataInputStream(bais);
		                pageIdList = new ArrayList<Long>();
		                try {
		                    while (dis.available() != 0) {
		                        long value = dis.readLong();
		                        pageIdList.add(Long.valueOf(value));
		                    }
		                    dis.close();
		                    bais.close();
		                }
		                catch (IOException ee) {
		                    ee.printStackTrace();
		                }
		            }
		        }
	    	}
	    	
	    	Enumeration<?> entries = null;
	    	
			NoteBook book = new NoteBook(mContext);
			book.loadNewBook(cursor);	
			cursor.close();
			bookCase.addNewBook(book);
			mImportedBookIds.add(book.getCreatedTime());
			
			if (isCancelled()) {
				return false;
			}

			for (Long pageId : pageIdList) {
				cursor = mDB.query(MetaData.PAGE_TABLE, 
						null, "(created_date = ?)", new String[] {pageId.toString()}, null, null, null);
				cursor.moveToFirst();
				if (cursor.isAfterLast()) {
					cursor.close();
					continue;
				}
				NotePage page = new NotePage(mContext, book.getCreatedTime());
				page.loadNewPage(cursor);
				book.addPage(page);
				cursor.close();
				
				if (isCancelled()) {
					return false;
				}
				
				String filePath = page.getFilePath();
				String srcPath = id + "/" + pageId + "/";
				
				entries = mZipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry ze = (ZipEntry) entries.nextElement();
					String name = ze.getName();
					if (name.contains(srcPath)) {
						String fileName = name.substring(name.lastIndexOf('/') + 1);
						File file = new File(filePath, fileName);
						try {
							file.createNewFile();
							copyZipFile(mZipFile.getInputStream(ze),
									new BufferedOutputStream(new FileOutputStream(
											file)));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					if (isCancelled()) {
						return false;
					}
				}
			}
			
			int num = book.getTotalPageNumber();
			if(num > 0)
			{
				PageDataLoader loader = new PageDataLoader(mContext);
				boolean genBookThumb = true;
				for (int i = 0; i < num; i++) {
					NotePage notePage = book.getNotePage(book.getPageOrder(i));
					if (notePage != null) {
						if (loader.load(notePage)) {
							if (genBookThumb) {
								//emmanual to fix bug 380867
								File srcFile = new File(book.getBookPath() + "/" + book.getPageOrder(i), MetaData.THUMBNAIL_PREFIX);
								File dstFile = new File(book.getBookPath(), MetaData.THUMBNAIL_PREFIX);
								if (srcFile.exists()) {
									try {
										FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
										FileChannel destChannel = new FileOutputStream(dstFile).getChannel();
										destChannel.transferFrom(srcChannel, 0, srcChannel.size());
										srcChannel.close();
										destChannel.close();
									} catch (Exception e) {
										book.saveBookCoverThumb(loader, true, notePage);
									}
								}        
								//end emmanual
								genBookThumb = false;
							}
							notePage.updateSyncFilesInfo(loader, true);
							notePage.updateTimestampInfo(loader, true);
							notePage.genThumb(loader, true, book.getPageSize() == MetaData.PAGE_SIZE_PHONE);
						}
					}
				}
			}
			
			return true;
		}
		
		private boolean importBook(Long id) throws Exception {
			BookCase bookCase = BookCase
					.getInstance(getApplicationContext());
			Enumeration<?> entries = null;
			
			entries = mZipFile.entries();
			SortedSet<SimplePageInfo> pages = new TreeSet<SimplePageInfo>(
					new PageComparator());
			NoteBook book = null;
			
			File dir = new File(MetaData.HIDE_DIR); //smilefish fix bug 776294
			dir.mkdir();
			
			while (entries.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) entries.nextElement();
				String entryName = null;
				if (id > 0) {
					entryName = id + MetaData.BOOK_INFO_EXT;
				} else {
					entryName = MetaData.BOOK_INFORMATION;
				}
				if (ze.getName().equals(entryName)) {
					BufferedInputStream bis = new BufferedInputStream(
							mZipFile.getInputStream(ze));
					AsusFormatReader afr = new AsusFormatReader(bis,
							NotePage.MAX_ARRAY_SIZE);
					bookCase.itemLoad(afr);
					bis.close();
					book = bookCase.getImportedNoteBook();
					for (int i = 0; i < book.getTotalPageNumber(); ++i) {
						SimplePageInfo info = new SimplePageInfo(
								book.getCreatedTime(),
								book.getPageOrder(i), 0, i);
						pages.add(info);
					}
					
					mImportedBookIds.add(book.getCreatedTime());
					
					if (isCancelled()) {
						return false;
					}
					break;
				}
				
				if (isCancelled()) {
					return false;
				}
			}
			
			if (isCancelled()) {
				return false;
			}
			
			if (book != null) {
				List<Long> bookmarks = book.getBookmarks();
				book.clearPageOrder();
				
				for (SimplePageInfo info : pages) {
					NotePage page = new NotePage(NoteBookPickerActivity.this,
							book.getCreatedTime());
					page.setBookmark((bookmarks != null && bookmarks
							.contains(info.pageId)) ? true : false);
					String filePath = page.getFilePath();
					String fileBookPath = book.getBookPath();//darwin
							
					String srcPath = book.getImportedTime() + "/"
							+ info.pageId + "/";
					entries = mZipFile.entries();
					while (entries.hasMoreElements()) {
						ZipEntry ze = (ZipEntry) entries.nextElement();
						//darwin
						if (book.getCoverIndex() ==1) 
						{
							if(ze.getName().contains(MetaData.THUMBNAIL_COVER)) 
							{
								File file = new File(fileBookPath,
										MetaData.THUMBNAIL_COVER);
								BufferedOutputStream bos = new BufferedOutputStream(
										new FileOutputStream(file));
								copyZipFile(mZipFile.getInputStream(ze), bos);
							}
						}
						//darwin
						if (ze.getName().contains(srcPath)) {
							if (ze.getName().contains(MetaData.DOODLE_PREFIX)
									|| ze.getName().contains(
											MetaData.DOODLE_ITEM_PREFIX_LEGEND)) {
								File file = new File(filePath,
										MetaData.DOODLE_ITEM_PREFIX);
								BufferedOutputStream bos = new BufferedOutputStream(
										new FileOutputStream(file));
								copyZipFile(mZipFile.getInputStream(ze), bos);
							} else if (ze.getName().contains(
									MetaData.NOTE_ITEM_PREFIX)
									|| ze.getName()
											.contains(
													MetaData.NOTE_ITEM_PREFIX_DEPRECATED)) {
								File file = new File(filePath,
										MetaData.NOTE_ITEM_PREFIX);
								BufferedOutputStream bos = new BufferedOutputStream(
										new FileOutputStream(file));
								copyZipFile(mZipFile.getInputStream(ze), bos);
							} else if (ze.getName().contains(
									MetaData.THUMBNAIL_PREFIX)
									|| ze.getName().contains(
											MetaData.THUMBNAIL_PREFIX_LEGEND)) {
								File file = new File(filePath,
										MetaData.THUMBNAIL_PREFIX);
								BufferedOutputStream bos = new BufferedOutputStream(
										new FileOutputStream(file));
								copyZipFile(mZipFile.getInputStream(ze), bos);
							} else {
								String[] tokens = ze.getName().split("/");
								String fileName = tokens[tokens.length - 1];
								File file = new File(filePath, fileName);
								BufferedOutputStream bos = new BufferedOutputStream(
										new FileOutputStream(file));
								copyZipFile(mZipFile.getInputStream(ze), bos);
							}
						}
						
						if (isCancelled()) {
							return false;
						}
					}
					book.addPage(page);
					
					if (isCancelled()) {
						return false;
					}
				}
				
				int num = book.getTotalPageNumber();
				if(num > 0)
				{
					PageDataLoader loader = new PageDataLoader(mContext);
					boolean genBookThumb = true;
					for (int i = 0; i < num; i++) {
						NotePage notePage = book.getNotePage(book.getPageOrder(i));
						if (notePage != null) {
							if (loader.load(notePage)) {
								if (genBookThumb) {
									//emmanual to fix bug 380867
									File srcFile = new File(book.getBookPath() + "/" + book.getPageOrder(i), MetaData.THUMBNAIL_PREFIX);
									File dstFile = new File(book.getBookPath(), MetaData.THUMBNAIL_PREFIX);
									if (srcFile.exists()) {
										try {
											FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
											FileChannel destChannel = new FileOutputStream(dstFile).getChannel();
											destChannel.transferFrom(srcChannel, 0, srcChannel.size());
											srcChannel.close();
											destChannel.close();
										} catch (Exception e) {
											book.saveBookCoverThumb(loader, true, notePage);
										}
									}        
									//end emmanual
									genBookThumb = false;
								}
								notePage.updateSyncFilesInfo(loader, true);
								notePage.updateTimestampInfo(loader, true);
								if (mVersion != MetaData.SNE_VERSION_3) {
									notePage.genThumb(loader, true, book.getPageSize() == MetaData.PAGE_SIZE_PHONE);
								}
							}
						}
					}
				}
			}
			
			if (isCancelled()) {
				return false;
			}
			
			return true;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (Looper.myLooper() == null) {
				Looper.prepare();
			}
			
			try {
				mZipFile = new ZipFile(mPath);
				
				if (!mIsSnb) {
					if (!mBookIds.isEmpty()) {
						for (Long id : mBookIds) {
							importBook(id);
						}
					} else {
						importBook(0L);
					}
				} else {
					File dbFile = new File(MetaData.DATA_DIR, MetaData.DATABASE_NAME);
					if (!dbFile.exists()) {
						return false;
					}
					mDB = SQLiteDatabase.openDatabase(
							dbFile.getAbsolutePath(), null, 
							SQLiteDatabase.OPEN_READONLY);
					if (!mBookIds.isEmpty()) {
						for (Long id : mBookIds) {
							importSnb(id);
						}
					}
					mDB.close();
					if (dbFile.exists()) {
						dbFile.delete();
					}
				}
				mZipFile.close();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onCancelled(Boolean result) {
			mIsImportTasRunning = false;
			
			BookCase bookcase = BookCase.getInstance(NoteBookPickerActivity.this);
			bookcase.delBooks(mImportedBookIds);
			
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				NoteBookPickerActivity.this.removeDialog(IMPORT_PROGRESS_DIALOG);
				NoteBookPickerActivity.this.updateFragment();
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			mIsImportTasRunning = false;
			
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				NoteBookPickerActivity.this.removeDialog(IMPORT_PROGRESS_DIALOG);
				if (result) {
					NoteBookPickerActivity.this.showDialog(IMPORT_SUCCESS_DIALOG);
				} else {
					NoteBookPickerActivity.this.showDialog(IMPORT_FAIL_DIALOG);
				}
				NoteBookPickerActivity.this.updateFragment();
			}
		}

		private void copyZipFile(InputStream is, OutputStream os) {
			byte[] byteBuffer = new byte[2048];
			int byteIn = 0;
			try {
				while ((byteIn = is.read(byteBuffer)) >= 0) {
					os.write(byteBuffer, 0, byteIn);
				}
				is.close();
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/********************************************************
	 * Import the data of SuperNote END
	 *******************************************************/

	public void deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] subDirs = dir.list();
			for (String sub : subDirs) {
				deleteDir(new File(dir, sub));
			}
		}
		dir.delete();
	}

	/****************************
	 * Delete Page
	 ***************************/
	public void confirmDeletePages(SortedSet<SimplePageInfo> items) {
		if (mDeletePagesTask == null
				|| mDeletePagesTask.isTaskRunning() == false) {
			mDeletePagesTask = new DeletePagesTask(this, items);
		}
	}

	public void executeDeletePages() {
		if (mDeletePagesTask != null
				&& mDeletePagesTask.isTaskRunning() == false) {
			mDeletePagesTask.execute();
		}
	}

	public void setDeletePageProgressDialog(ProgressDialog d) {
		if (mDeletePagesTask != null && mDeletePagesTask.isTaskRunning()) {
			mDeletePagesTask.setDialog(d);
		}
	}

	/****************************
	 * Copy Page
	 ***************************/
	public void selectCopyDestBook(NoteBook srcBook) {
		if (mCopyPageTask == null || mCopyPageTask.isTaskRunning() == false) {
			mCopyPageTask = new CopyPageTask(this, srcBook);
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			Fragment fragment = mFragmentManager
					.findFragmentByTag(CopyPageDialogFragment.TAG);
			if (fragment != null && fragment.isAdded()) {
				ft.remove(fragment);
			}
			ft.commit();
			Bundle b = new Bundle();
			b.putInt("style", CopyPageDialogFragment.COPY_SELECT_DIALOG);
			CopyPageDialogFragment newFragment = CopyPageDialogFragment
					.newInstance(b);
			newFragment.show(mFragmentManager, CopyPageDialogFragment.TAG);

		}
	}

	public void selectCopyDestBook(SortedSet<SimplePageInfo> items,
			boolean isPrivate) {
		if (mCopyPageTask == null || mCopyPageTask.isTaskRunning() == false) {
			mCopyPageTask = new CopyPageTask(this, items);
			FragmentManager fm = getFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			Fragment fragment = fm
					.findFragmentByTag(CopyPageDialogFragment.TAG);
			if (fragment != null && fragment.isAdded()) {
				ft.remove(fragment);
			}
			ft.commit();
			Bundle b = new Bundle();
			// begin wendy
			b.putBoolean("isPrivate", mislocked);
			// end wendy
			b.putInt("style", CopyPageDialogFragment.COPY_SELECT_DIALOG_BOOK);
			CopyPageDialogFragment newFragment = CopyPageDialogFragment
					.newInstance(b);
			newFragment.show(mFragmentManager, CopyPageDialogFragment.TAG);

		}
	}

	public void setCopyPageDestBook(NoteBook destBook) {
		if (mCopyPageTask != null && mCopyPageTask.isTaskRunning() == false) {
			mCopyPageTask.setDestBook(destBook);
		}
	}

	public void confirmCopyPages(NoteBook destBook) {
		if (mCopyPageTask != null && mCopyPageTask.isTaskRunning() == false) {
			mCopyPageTask.setDestBook(destBook);
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			Fragment fragment = mFragmentManager
					.findFragmentByTag(CopyPageDialogFragment.TAG);
			if (fragment != null && fragment.isAdded()) {
				ft.remove(fragment);
			}
			ft.commit();
			Bundle b = new Bundle();
			b.putInt("style", CopyPageDialogFragment.COPY_CONFIRM_DIALOG_BOOK);
			b.putString("title", destBook.getTitle());
			CopyPageDialogFragment newFragment = CopyPageDialogFragment
					.newInstance(b);
			newFragment.show(mFragmentManager, CopyPageDialogFragment.TAG);
		}
	}

	public void executeCopyPage() {
		if (mCopyPageTask != null && mCopyPageTask.isTaskRunning() == false) {
			mCopyPageTask.execute();
		}
	}

	public void setCopyPagesProgressDialog(ProgressDialog d) {
		if (mCopyPageTask != null && mCopyPageTask.isTaskRunning() == true) {
			mCopyPageTask.setDialog(d);
		}
	}

	/****************************
	 * Move Page
	 ***************************/
	public void selectMoveDestBook(NoteBook srcBook, boolean isPrivate) {
		if (mMovePageTask == null || mMovePageTask.isTaskRunning() == false) {
			mMovePageTask = new MovePageTask(this, srcBook);
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			Fragment fragment = mFragmentManager
					.findFragmentByTag(MovePageDialogFragment.TAG);
			if (fragment != null && fragment.isAdded()) {
				ft.remove(fragment);
			}
			ft.commit();
			Bundle b = new Bundle();
			b.putInt("style", MovePageDialogFragment.MOVE_SELECT_DIALOG);
			// begin wendy
			// b.putBoolean("isPrivate", isPrivate);
			b.putBoolean("isPrivate", mislocked);
			// end wendy
			MovePageDialogFragment newFragment = MovePageDialogFragment
					.newInstance(b);
			newFragment.show(mFragmentManager, MovePageDialogFragment.TAG);
		}
	}

	public void selectMoveDestBook(SortedSet<SimplePageInfo> items,
			boolean isPrivate) {
		if (mMovePageTask == null || mMovePageTask.isTaskRunning() == false) {
			mMovePageTask = new MovePageTask(this, items);
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			Fragment fragment = mFragmentManager
					.findFragmentByTag(MovePageDialogFragment.TAG);
			if (fragment != null && fragment.isAdded()) {
				ft.remove(fragment);
			}
			ft.commit();
			Bundle b = new Bundle();
			b.putInt("style", MovePageDialogFragment.MOVE_SELECT_DIALOG);
			// begin wendy
			b.putBoolean("isPrivate", mislocked);
			// end wendy
			MovePageDialogFragment newFragment = MovePageDialogFragment
					.newInstance(b);
			newFragment.show(mFragmentManager, MovePageDialogFragment.TAG);
		}
	}

	public void setMovePageDestBook(NoteBook destBook) {
		if (mMovePageTask != null && mMovePageTask.isTaskRunning() == false) {
			mMovePageTask.setDestBook(destBook);
		}
	}

	public void confirmMovePages(NoteBook destBook) {
		if (mMovePageTask != null && mMovePageTask.isTaskRunning() == false) {
			mMovePageTask.setDestBook(destBook);
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			Fragment fragment = mFragmentManager
					.findFragmentByTag(MovePageDialogFragment.TAG);
			if (fragment != null && fragment.isAdded()) {
				ft.remove(fragment);
			}
			ft.commit();
			Bundle b = new Bundle();
			b.putInt("style", MovePageDialogFragment.MOVE_CONFIRM_DIALOG);
			b.putString("title", destBook.getTitle());
			MovePageDialogFragment newFragment = MovePageDialogFragment
					.newInstance(b);
			newFragment.show(mFragmentManager, MovePageDialogFragment.TAG);
		}
	}

	public void executeMovePage() {
		if (mMovePageTask != null && mMovePageTask.isTaskRunning() == false) {
			mMovePageTask.execute();
		}
	}

	public void setMovePagesProgressDialog(ProgressDialog d) {
		if (mMovePageTask != null && mMovePageTask.isTaskRunning() == true) {
			mMovePageTask.setDialog(d);
		}
	}

	/****************************
	 * Import Old File
	 ***************************/
	private abstract class ImportFileTask extends
			AsyncTask<Void, Void, Boolean> {
		public abstract void setDialog(ProgressDialog d);
		public abstract void showSuccessDialog();//smilefish
	}

	public int getImportFileVersion(String path, boolean isSnb) {
		File file = new File(path);
		if (file.exists() == false) {
			return MetaData.UNKNOWN_VERSION;
		}

		try {
			ZipFile zipFile = new ZipFile(file);
			Enumeration<?> entries = zipFile.entries();
			ZipEntry ze = null;
			while (entries.hasMoreElements()) {
				ze = (ZipEntry) entries.nextElement();
				if (!isSnb) {
					if (ze.getName().equals(MetaData.VERSION)) {
						BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(ze));
						int ver = bis.read();
						if (bis.available() > 0) {
							int lv = bis.read();
							ver = (ver << 8) | lv;
						}
						bis.close();
						if ((ver == MetaData.SNE_VERSION_0302) 
								|| (ver == MetaData.SNE_VERSION_3)) {
							zipFile.close();
							return ver;
						}
					}
				}
			}
			zipFile.close();
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return MetaData.UNKNOWN_VERSION;
	}

	public static boolean islocked() {
		return mislocked;
	}

	// end wendy

	/**
	 * 创建新的�?��并且加载
	 * @author noah_zhang
	 */
	public NotePage createNewPageAndLoad(NoteBook book) {
		NotePage notepage = new NotePage(getApplicationContext(),
				book.getCreatedTime());
		notepage.setTemplate(book.getTemplate());
		notepage.setIndexLanguage(book.getIndexLanguage());
		book.addPage(notepage);
		PageDataLoader loader = PageDataLoader.getInstance(mContext);
		loader.load(notepage);
		return notepage;
	}
	
	// BEGIN: Better
	private void deletePage(NoteBook noteBook, long pageId, boolean isSync) {
		noteBook.removePageFromOrder(pageId);
		ContentValues cv = new ContentValues();
		if (!isSync) {
			cv.put(MetaData.PageTable.IS_DELETED, 1);
			mContentResolver.update(MetaData.PageTable.uri, cv,
					"created_date = ?", new String[] { Long.toString(pageId) });
		} else {
			mContentResolver.delete(MetaData.PageTable.uri, "created_date = ?",
					new String[] { Long.toString(pageId) });
		}
	}

	private void deleteBook(long bookId) {
		mBookcase.removeNoteBookById(bookId);
		if(MetaData.CoverChangedBookIdList.contains(bookId)){ //remove record
			MetaData.CoverChangedBookIdList.remove(bookId);
		}
		String[] selectionArgs = new String[] { Long.toString(bookId) };

		ContentValues cvbook = new ContentValues();
		cvbook.put(MetaData.BookTable.IS_DELETED, 1);
		cvbook.put(MetaData.BookTable.MODIFIED_DATE, System.currentTimeMillis());
		mContentResolver.update(MetaData.BookTable.uri, cvbook,
				"created_date = ?", selectionArgs);
	}
private void genThumb(PageDataLoader loader, boolean isLoadAsync, NotePage page, boolean isPhoneSize) {
		Bitmap cover = null;
		int color = page.getPageColor();
		int line = page.getPageStyle();
		Resources res = mContext.getResources();
		cover = CoverHelper.getDefaultCoverBitmap(color, line, res);//Allen

		Paint paint = new Paint();
		int targetWidth, targetHeight;

		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setFilterBitmap(true);

		Bitmap result = Bitmap.createBitmap(cover.getWidth(),
				cover.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(cover, 0, 0, paint);
		cover.recycle();
		cover = null;
		targetWidth = (int) (result.getWidth() * 0.9);
		targetHeight = (int) (result.getHeight() * 0.85);
		Bitmap content;
		content = Bitmap.createBitmap(targetWidth, targetHeight,
				Bitmap.Config.ARGB_8888);
		content.setDensity(Bitmap.DENSITY_NONE);
		if (!isLoadAsync) {
			loader.load(page);
		}
		page.getThumbnail(loader, isLoadAsync, content, true, false, false, "");
		float left = res.getDimension(R.dimen.thumb_padding_left);
		float top = res.getDimension(R.dimen.thumb_padding_top);
		canvas.translate(left, top);
		canvas.drawBitmap(content, 0, 0, paint);
		content.recycle();
		content = null;
		File file = new File(page.getFilePath(), MetaData.THUMBNAIL_PREFIX);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (fos != null) {
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			result.compress(Bitmap.CompressFormat.PNG,
					MetaData.THUMBNAIL_QUALITY, bos);
			result.recycle();
			result = null;
			try {
				bos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/********************************************************
	 * Change NoteBook sync status START
	 *******************************************************/
	public void changeBookStatus(Activity activity,
			ArrayList<Long> remoteBookIds, ArrayList<Long> localBookIds) {
		if (!mIsCloudSyncingTaskRunning) {
			mCloudSyncingTask = new ChangeBookStatusTask(this,
					remoteBookIds, localBookIds);
			mCloudSyncingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private class ChangeBookStatusTask extends AsyncTask<Void, Integer, Void> {
		private ArrayList<Long> mRemoteBookIds;
		private ArrayList<Long> mLocalBookIds;
		private ProgressDialog mProgressDialog;
		private int mMax = 0;
		private int mProgress = 0;
		private String mTitle = "";
		private int mProgressStatus = PROMPT_UPDATE_PROGRESS;
		
		private static final int PROMPT_UPDATE_PROGRESS = 0;
		private static final int PROMPT_TRANSFER_MSG = 1;
		private static final int PROMPT_ENABLE_MSG = 2;
		private static final int PROMPT_CANCEL_MSG = 3;

		public ChangeBookStatusTask(Activity activity,
				ArrayList<Long> remoteBookIds, ArrayList<Long> localBookIds) {
			if (remoteBookIds != null) {
				mRemoteBookIds = new ArrayList<Long>();
				mRemoteBookIds.addAll(remoteBookIds);
			} else {
				mRemoteBookIds = null;
			}
			if (localBookIds != null) {
				mLocalBookIds = new ArrayList<Long>();
				mLocalBookIds.addAll(localBookIds);
			} else {
				mLocalBookIds = null;
			}
			mMax = 1;
		}
		
		public void setProgressDialog(ProgressDialog dlg) {
			if (mProgressDialog != null) {
				mProgressDialog = null;
			}
			
			mProgressDialog = dlg;
			
			mProgressDialog.setMax(mMax);
			
			if (mProgressStatus == PROMPT_TRANSFER_MSG) {
				String prefix = mContext.getString(R.string.transfer);
				mProgressDialog.setTitle(prefix + mTitle);
				mProgressDialog.setMax(mMax);
			} else if (mProgressStatus == PROMPT_ENABLE_MSG) {
				String prefix = mContext.getString(R.string.nb_sync);
				mProgressDialog.setTitle(prefix + mTitle);
				mProgressDialog.setMax(mMax);
			} else if (mProgressStatus == PROMPT_CANCEL_MSG) {
				String prefix = mContext.getString(R.string.nb_async);
				mProgressDialog.setTitle(prefix + mTitle);
				mProgressDialog.setMax(mMax);
			}
			
			mProgressDialog.setProgress(mProgress);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			CountDownClass.getInstance(mContext).disableSync();
			
			mIsCloudSyncingTaskRunning = true;
			showDialog(CLOUD_SYNCING_PROGRESS_DIALOG);
		}
		
		private void updateTimestampInfo(long oldPageId, long newPageId,boolean isSync) {
			ContentValues cv = new ContentValues();
			cv.put(MetaData.TimestampTable.OWNER, newPageId);
			cv.put(MetaData.TimestampTable.USER_ACCOUNT, isSync ? MetaData.CurUserAccount : 0);//darwin
			mContentResolver.update(MetaData.TimestampTable.uri, cv, "owner = ?", 
					new String[] {Long.toString(oldPageId)});
		}

		private void changeStatus(long bookId, boolean isSync) {
			MetaData.TransferringBookIdList.add(bookId);
			long oldBookId = bookId;
			String oldBookDirName = Long.toString(oldBookId);
			long curBookTime = System.currentTimeMillis();
			long newBookId = isSync ? SyncHelper.pageTime2Id(curBookTime)
					: curBookTime;
			String newBookDirName = Long.toString(newBookId);

			NoteBook oldNoteBook = mBookcase.getNoteBook(oldBookId);
			if (oldNoteBook == null) {
				MetaData.TransferringBookIdList.remove(bookId);
				return;
			}
			int oldIndex = mBookcase.getNoteBookIndex(oldBookId);

			// transfer version 1 book to version 3
			if (oldNoteBook.getVersion() == 1) {
				mProgress = 1;//darwin
				mTitle = oldNoteBook.getTitle();
				mMax = oldNoteBook.getTotalPageNumber();//darwin
				publishProgress(PROMPT_TRANSFER_MSG);
				PageDataLoader loader = new PageDataLoader(mContext);
				int numPage = oldNoteBook.getTotalPageNumber();
				for (int i = 0; i < numPage; i++) {
					long pageId = oldNoteBook.getPageOrder(i);
					NotePage page = oldNoteBook.getNotePage(pageId);
					if ((page != null) && (page.getVersion() == 1)) {
						loader.load(page);
						page.save(loader.getAllNoteItems(), loader.getDoodleItem());//Allen
						genThumb(loader, true, 
								page,
								oldNoteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE);
						mProgress++;
						publishProgress(PROMPT_UPDATE_PROGRESS);
					}
				}
				oldNoteBook.setVersion(3);
				File file = new File(MetaData.DATA_DIR
						+ oldNoteBook.getCreatedTime(), "paintbook");
				if (file.exists()) {
					file.delete();
				}
			}

			mProgress = 1;//darwin
			mTitle = oldNoteBook.getTitle();
			mMax = oldNoteBook.getTotalPageNumber();//darwin
			if (isSync) {
				publishProgress(PROMPT_ENABLE_MSG);
			} else {
				publishProgress(PROMPT_CANCEL_MSG);
			}
			File oldBookDir = new File(MetaData.DATA_DIR, oldBookDirName);
			File newBookDir = new File(MetaData.DATA_DIR, newBookDirName);
			oldBookDir.renameTo(newBookDir);
			
			//Begin smilefish:rename cover file when change sync status
			File srcCoverFile = new File(MetaData.DATA_DIR + newBookDirName, oldBookDirName + MetaData.THUMBNAIL_COVER_CROP);
			File destCoverFile = new File(MetaData.DATA_DIR + newBookDirName, newBookDirName + MetaData.THUMBNAIL_COVER_CROP);
			if(srcCoverFile.exists())
			{
				srcCoverFile.renameTo(destCoverFile);
			}
			//End Smilefish

			deleteBook(oldBookId);
			MetaData.TransferringBookIdList.add(newBookId);
			oldNoteBook.setCreatedTimeNoUpdateDB(newBookId);
			long userId = isSync ? MetaData.CurUserAccount : 0;
			oldNoteBook.setUserId(userId);
			mBookcase.addBookNoSetCurrent(oldNoteBook, oldIndex);
			try {
			Intent intent = new Intent(MetaData.SYNC_UPDATE_UI);
			
							mContext.sendBroadcast(intent);
			

			int pageNum = oldNoteBook.getTotalPageNumber();
			long[] oldPageIds = new long[pageNum];
			for (int i = 0; i < pageNum; i++) {
				oldPageIds[i] = oldNoteBook.getPageOrder(i);
			}

			for (long pageId : oldPageIds) {
				long oldPageId = pageId;
				String oldPageDirName = Long.toString(oldPageId);
				long curPageTime = System.currentTimeMillis();
				long newPageId = isSync ? SyncHelper.pageTime2Id(curPageTime)
						: curPageTime;
				String newPageDirName = Long.toString(newPageId);

				NotePage oldNotePage = oldNoteBook.getNotePage(oldPageId);

				File oldPageDir = new File(MetaData.DATA_DIR + newBookDirName,
						oldPageDirName);
				File newPageDir = new File(MetaData.DATA_DIR + newBookDirName,
						newPageDirName);
				oldPageDir.renameTo(newPageDir);
				deletePage(oldNoteBook, oldPageId, isSync);

				oldNotePage.setCreatedTime(newPageId);
				oldNotePage.setModifiedTime(curPageTime);
				oldNotePage.setOwnerBookId(newBookId);
				oldNotePage.setUserId(userId);
				oldNoteBook.addPageFromweb(oldNotePage);
				
				updateTimestampInfo(oldPageId, newPageId,isSync);//darwin
				//Begin Darwin_Yu@asus.com
				updateDoodleItemAttachmentDB(oldPageId,newPageId);
				//End   Darwin_Yu@asus.com
				
				mProgress++;
				publishProgress(PROMPT_UPDATE_PROGRESS);
				
				//renew the new page id for widget
				if(MetaData.WIDGET_BOOK_ID == oldBookId){
					renewId(MetaData.Changed_Page_List, oldPageId, newPageId);
				}
			}

            //renew the new book id for widget
			if(MetaData.WIDGET_BOOK_ID == oldBookId){
				renewId(MetaData.Changed_Book_List, oldBookId, newBookId);
			}
			MetaData.IS_STATUS_CHANGED = true;
			MetaData.TransferringBookIdList.remove(bookId);
			MetaData.TransferringBookIdList.remove(newBookId);
			mContext.sendBroadcast(intent);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void renewId(HashMap<Long, Long> list, long oldValue, long newValue){
			//update the Id of book or page for widget [Carol]
			if(list.isEmpty() || !list.containsValue(oldValue)){
				list.put(oldValue, newValue);
			}else{
				Long key = 0L;
				for (Map.Entry<Long, Long> e : list.entrySet()){
					if (oldValue == e.getValue()) {
						key = e.getKey();
					}
				}
				list.remove(key);
				list.put(oldValue, newValue);
			}
		}
		
		//Begin Darwin_Yu@asus.com

		
		private void updateDoodleItemAttachmentDB(long oldPageId, long newPageId)
		{	
			try
			{
			ContentValues cvDoodle = new ContentValues();
			cvDoodle.put(MetaData.DoodleTable.ID, newPageId);
	        mContentResolver.update(MetaData.DoodleTable.uri, cvDoodle, "_id = ?", new String[] {Long.toString(oldPageId)});
	        
			ContentValues cvItem = new ContentValues();
			cvItem.put(MetaData.ItemTable.ID, newPageId);
	        mContentResolver.update(MetaData.ItemTable.uri, cvItem, "_id = ?", new String[] {Long.toString(oldPageId)});
	        
			ContentValues cvAttachment = new ContentValues();
			cvAttachment.put(MetaData.AttachmentTable.ID, newPageId);
			
	        mContentResolver.update(MetaData.AttachmentTable.uri, cvAttachment, "_id = ?", new String[] {Long.toString(oldPageId)});
			}
			catch(Exception e)
			{
				Log.i("darwin test", e.toString());
			}

		}
		//End   Darwin_Yu@asus.com

		@Override
		protected Void doInBackground(Void... params) {
			if (mRemoteBookIds != null) {
				for (long bookId : mRemoteBookIds) {
					changeStatus(bookId, true);
				}
			}
			if (mLocalBookIds != null) {
				for (long bookId : mLocalBookIds) {
					changeStatus(bookId, false);
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);

			if (values != null) {
				if (values[0] == PROMPT_TRANSFER_MSG) {
					mProgressStatus = PROMPT_TRANSFER_MSG;
					String prefix = NoteBookPickerActivity.this.getString(R.string.transfer);
					mProgressDialog.setTitle(prefix + mTitle);
					mProgressDialog.setMax(mMax);
				} else if (values[0] == PROMPT_ENABLE_MSG) {
					mProgressStatus = PROMPT_ENABLE_MSG;
					String prefix = NoteBookPickerActivity.this.getString(R.string.nb_sync);
					mProgressDialog.setTitle(prefix + mTitle);
					mProgressDialog.setMax(mMax);
				} else if (values[0] == PROMPT_CANCEL_MSG) {
					mProgressStatus = PROMPT_CANCEL_MSG;
					String prefix = NoteBookPickerActivity.this.getString(R.string.nb_async);
					mProgressDialog.setTitle(prefix + mTitle);
					mProgressDialog.setMax(mMax);
				}
				
				mProgressDialog.setProgress(mProgress);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if ((mProgressDialog != null) && (mProgressDialog.isShowing())) {
				NoteBookPickerActivity.this.removeDialog(CLOUD_SYNCING_PROGRESS_DIALOG);
			}
			
			mIsCloudSyncingTaskRunning = false;
			
			CountDownClass.getInstance(NoteBookPickerActivity.this).enableSync();
		}
	}

	/********************************************************
	 * Change NoteBook sync status END
	 *******************************************************/
	// END: Better

	// BEGIN: Better
	private static boolean mIsLoadFotaDataTaskRunning = false;

	private class LoadFotaDataTask extends AsyncTask<Void, Void, Void>
			implements CompatibleDataConverter.Observer {
		private int mVersion;

		public LoadFotaDataTask(int version) {
			mVersion = version;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mIsLoadFotaDataTaskRunning = true;
			if (mVersion == 1) {
				V1DataConverter converter = new V1DataConverter(mContext);
				converter.registerObserver(this);
				converter.preprocessPages();
				converter.removeObserver(this);
			}
			deleteDir(new File(V1DataFormat.DIR));
			File rootDir = new File(DataFormat.FOTA_ROOT_DIR);
			String[] files = rootDir.list();
			if ((files == null) || (files.length == 0)) {
				rootDir.delete();
			}

			mPreferenceEditor.putBoolean(
					mResources.getString(R.string.load_fota_data), false);
			mPreferenceEditor.commit();
			mIsLoadFotaDataTaskRunning = false;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Void... params) {

		}

		@Override
		public void notify(long bookId, long pageId) {
			publishProgress();
		}
	}
	// END: Better

	private static boolean mIsMoveFotaDataTaskRunning = false;

	private class MoveFotaDataTask extends AsyncTask<Void, Void, Void>
			implements CompatibleDataConverter.Observer {

		public MoveFotaDataTask(int version) {
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mIsMoveFotaDataTaskRunning = true;
			MoveFotaData();

			mPreferenceEditor.putBoolean(
					mResources.getString(R.string.move_fota_data), true);
			mPreferenceEditor.commit();
			mIsMoveFotaDataTaskRunning = false;
			return null;
		}
		
		private void MoveFotaData()
		{
			String data_oldRootPath = MetaData.DIR + "Data/";// AsusSuperNote/Data
			String data_newRootPath = MetaData.HIDE_DIR + "Data/";//.AsusSuperNote/Data/			
			File oldDir = new File(data_oldRootPath);
			File newDir = new File(data_newRootPath);
			
			if(oldDir.exists() == false) return;
			
			File dir = new File(MetaData.HIDE_DIR);
			if(!dir.exists())
			{
				dir.mkdir();
			}		
			if(!newDir.exists())
			{
				newDir.mkdir();
			}
			
			String[] booklist = oldDir.list();
			
			for(String book:booklist)
			{
				File bookfile = new File(oldDir, book);
				File newbookfile = new File(newDir, book);
				Log.v("wendy","move books :" + bookfile.renameTo(newbookfile));
			}
			
			oldDir.delete();
		}
				
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Void... params) {
		}

		@Override
		public void notify(long bookId, long pageId) {
			publishProgress();
		}
	}
	
	//end wendy
	
	public void returnBitmapFromCameraAndGallery(Bitmap bitmap,String path,String name)
	{
		mEditBookCoverAdapter.changeCoverChooseBitmap(bitmap);
		mEditBookCoverAdapter.changeCoverFile(path,name);
	}
	
	public void showMenu()
	{
		showDialog(EDITCOVER_CHOOSE_DIALOG);
	}
	private void changeBookCoverThumb(NoteBook book ,Bitmap bitmap)
    {
    	try {
             if (bitmap != null) {
                File file = new File(book.getBookPath(), MetaData.THUMBNAIL_COVER);
                if (file.exists() == false) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bitmap.compress(Bitmap.CompressFormat.PNG, MetaData.THUMBNAIL_QUALITY, bos);
                bitmap.recycle();
                bitmap = null;
                bos.close();
                fos.close();
                //Begin Allen for update widget 
        		if(!MetaData.SuperNoteUpdateInfoSet.containsKey(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_EDIT_CORVER)){
        			MetaData.SuperNoteUpdateInfoSet.put(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_EDIT_CORVER,null);
        		}
        		//End Allen
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	static public Bitmap setBitmapMask(Bitmap bitmap, int width, int height)
	{
		Bitmap result = null;
        Canvas resultCanvas, contentCanvas;
        Paint paint = new Paint();
        int targetWidth, targetHeight;
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        int bitmapW = bitmap.getWidth();
        int bitmapH = bitmap.getHeight();
        int bitmapMaskW = width;
        int bitmapMaskH = height;
        	float scale = 0.0f;
        	if(((float)bitmapW * (float)bitmapMaskH / (float)bitmapMaskW) < bitmapH)
        	{
        		scale = (float) bitmapMaskW / (float) bitmapW; //modified by smilefish
        	}
        	else
        	{
        		scale = (float) bitmapMaskH / (float) bitmapH; //modified by smilefish
        	}
        
        try {
            result = Bitmap.createBitmap(bitmapMaskW, bitmapMaskH, Bitmap.Config.ARGB_8888);
            resultCanvas = new Canvas(result);
            resultCanvas.save();
            resultCanvas.scale(scale, scale);
            resultCanvas.drawBitmap(bitmap, 0, 0, paint);
            resultCanvas.restore();
        }
        catch(Exception e)
        {
        }

  		return result;
	}
	
	String editCoverTitleText = "";
	EditText editCoverTitle = null;
	public void editBookCoverDialog(NoteBook book, boolean shouldRequestFocus) {
		mEditBookCoverBook = book; //smilefish
		Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE); 
        View view = inflater.inflate(R.layout.notebook_editcover_picker,null); 
		GridView gridview = (GridView)view.findViewById(R.id.editcover_gridview);
		//begin smilefish
			editCoverTitle = (EditText)view.findViewById(R.id.editcover_title);
			editCoverTitle.setHint(book.getTitle());
			mEditBookCoverEditText = editCoverTitle;
			
		//emmanual to fix bug 411412
		if (!editCoverTitleText.equals("")) {
			editCoverTitle.setText(editCoverTitleText);
			editCoverTitleText = "";
		}
		//end smilefish
		TextViewUtils.enableCapSentences(editCoverTitle);//noah
		mEditBookCoverAdapter = new EditBookCoverAdapter(this.mContext,book,gridview,this); //smilefish
		gridview.setAdapter(mEditBookCoverAdapter);
		//begin smilefish fix bug 560666/546047
		builder.setPositiveButton(R.string.editcover_save, null); 
		builder.setNegativeButton(android.R.string.cancel, null);
        builder.setView(view);
	    mEditBookCoverDialog = builder.create();
	    mEditBookCoverAdapter.setDialog(mEditBookCoverDialog);
	    mEditBookCoverDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    mEditBookCoverDialog.show();

	    	mEditBookCoverDialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener(){
				@Override
	            public void onClick(View v) {
					mEditBookCoverDialog.dismiss();
				}
			});
			mEditBookCoverDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){
				@Override
	            public void onClick(View v) {
					if(mEditBookCoverAdapter.mCheckIndex != -1 && mEditBookCoverAdapter.mCheckIndex != 0) //smilefish
					{  
						String name = mEditBookCoverEditText.getText().toString();
						if(name.length() > 0)
							mEditBookCoverAdapter.mBook.setTitle(name); //smilefish
						//modified by smilefish
						if(mEditBookCoverAdapter.mCheckIndex == 1)
							mEditBookCoverAdapter.mBook.setCoverIndex(0);
						else if(mEditBookCoverAdapter.mCheckIndex == 17)
							mEditBookCoverAdapter.mBook.setCoverIndex(1);
						else
							mEditBookCoverAdapter.mBook.setCoverIndex(mEditBookCoverAdapter.mCheckIndex);
						//end
						long currentTime = System.currentTimeMillis();
						mEditBookCoverAdapter.mBook.setCoverModifyTime(currentTime);//darwin
						ContentValues cv = new ContentValues();
				        cv.put(MetaData.BookTable.INDEX_COVER, mEditBookCoverAdapter.mBook.getCoverIndex());
				        cv.put(MetaData.BookTable.COVER_MODIFYTIME, currentTime);
				        cv.put(MetaData.BookTable.MODIFIED_DATE, currentTime);
				        int count = mContentResolver.update(MetaData.BookTable.uri, cv, "created_date = ?", new String[] { Long.toString(mEditBookCoverAdapter.mBook.getCreatedTime()) });
	
						if(mEditBookCoverAdapter.mCheckIndex == 17) //modified by smilefish
						{						
							Bitmap bitmap = mEditBookCoverAdapter.list.get(mEditBookCoverAdapter.mCheckIndex);
							changeBookCoverThumb(mEditBookCoverAdapter.mBook,bitmap);
						}
						else if (mEditBookCoverAdapter.mCheckIndex == 1 ) //modified by smilefish
						{
						}
						else
						{
							//begin smilefish
							Bitmap bitmap = null;
								bitmap = NoteBook.getNoteBookCover(mEditBookCoverAdapter.mBook, NoteBookPickerActivity.this);
							//end smilefish
							changeBookCoverThumb(mEditBookCoverAdapter.mBook,bitmap);
						}

						//Begin by Emmanual
						if(MetaData.IS_GA_ON)
						{
							GACollector gaCollector = new GACollector(mContext);
							gaCollector.editBookCover();
						}
						//End						
						
						mEditBookCoverDialog.dismiss();
						mPageGridFragment.dataSetChanged();
					} else{//emmanual to fix bug 451553
						String name = mEditBookCoverEditText.getText().toString();
						if(name.length() > 0)
							mEditBookCoverAdapter.mBook.setTitle(name);
						mEditBookCoverDialog.dismiss();
						mPageGridFragment.dataSetChanged();
					}
				}
			}
			);
		//end smilefish

		if(shouldRequestFocus){ //smilefish fix bug 647106
			editCoverTitle.requestFocus();
			DialogUtils.showSoftInput(editCoverTitle);//noah
		}
	    
	    	int width = mContext.getResources().getDimensionPixelSize(R.dimen.edit_cover_dialog_width);
	    	int height = mContext.getResources().getDimensionPixelSize(R.dimen.edit_cover_dialog_height);
	    	mEditBookCoverDialog.getWindow().setLayout(width, height); //smilefish
	    	
	    mEditBookCoverDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			 public void onCancel(DialogInterface dialog) {
		        NoteBookPageGridViewAdapter.resetNotebookPageGridProcessing();
			 }
			});
	}
	//BEGIN: show_wang@asus.com
	//Modified reason: add notebook dialog
	private void showAddBookDialog(boolean isFromWidget, boolean isConfigurationChanged) {
		String notebookName = "";
		if(mAddBookDialog == null && isFromWidget){ //display dialog from widget 'Add'[Carol]
			createAddBookDialog(notebookName);
		}else if ( mAddBookDialog == null && mIsAddBookDialogShow ) {
			createAddBookDialog(notebookName);
		}
		
		if(isConfigurationChanged){ //smilefish fix bug 570315
			if(mPageGridFragment == null)
        		return;
        	FragmentManager fm = mPageGridFragment.getChildFragmentManager();
	    	AddNewDialogFragment addNewDialogFragment = (AddNewDialogFragment) fm
					.findFragmentByTag(AddNewDialogFragment.TAG);
			if (addNewDialogFragment != null && addNewDialogFragment.isDialogShown()) {
				String bookName = addNewDialogFragment.getBookName();
				Boolean type = addNewDialogFragment.getType();
				addNewDialogFragment.dismiss();
				addNewDialogFragment = AddNewDialogFragment.newInstance(bookName, type);
				FragmentTransaction ft = fm.beginTransaction();
	    		addNewDialogFragment.show(ft, AddNewDialogFragment.TAG);
			}
		}
	}
	
	//smilefish: reload dialog when rotate screen
	private void showEditCoverDialog() {
		if ( mEditBookCoverDialog != null && mEditBookCoverDialog.isShowing() ) 
		{
			editCoverTitleText = editCoverTitle.getText().toString();//emmanual to fix bug 411412
			int lastIndex = mEditBookCoverAdapter.getLastIndex();
			mEditBookCoverDialog.dismiss();
			boolean isChooseDialogShown = lastIndex == 0 && mEditCoverChooseDialog != null && mEditCoverChooseDialog.isShowing();
			editBookCoverDialog(mEditBookCoverBook, !isChooseDialogShown);
			mEditBookCoverAdapter.setLastIndex(lastIndex); //fix bug 301838 by smilefish
			if(mEditBookCoverAdapter.isNeedToSetSelection())
				mEditBookCoverAdapter.setCoverSelection(); //smilefish
			
			if(isChooseDialogShown){ //fix bug 301887 by smilefish
				removeDialog (EDITCOVER_CHOOSE_DIALOG);
				showDialog (EDITCOVER_CHOOSE_DIALOG);
			}
		} else{
			removeDialog(EDITCOVER_CHOOSE_DIALOG); //emmanual to fix bug 447763
		}
	}

	//darwin
	public void createAddBookDialog(String notebookname) {
		//emmanual
		if(MetaData.isSDCardFull()){
			MetaData.showFullNoAddToast(this);
			return ;
		}
		
		Bundle b = new Bundle();
		b.putString("bookname", notebookname);
		showDialog(ADD_NOTEBOOK_CHOOSE_DIALOG, b);
		mIsAddBookDialogShow = true;
	}
	//END: show_wang@asus.com
	
	//begin smilefish fix bug 397251
	public void showHideDialog(){
		boolean hasPassword = mPreference.getBoolean(
				mResources.getString(R.string.pref_has_password), false);
		if (hasPassword && mPageGridFragment.isShowLockDialogForOneBook()) {
			mPageGridFragment.showHideDialog();
		}
	}
	//end smilefish
	
	public boolean isNavigationDrawerShown(){
		return mDrawerLayout.isDrawerOpen(mDrawerList);
	}
	
	public void setNavigationDrawerBackground(int color){
		mDrawerLayout.setBackgroundColor(color);
	}
	
	/**********************************************
	 * CopyTask开始
	 ********************************************/

	public String mBookName = "";
	public boolean mOverwriteFloder = false;
	public static Object mLockObject = new Object();
	
	public class CopyTask extends AsyncTask<String, Integer, Boolean> {
		private String mTargetFolder;
		private long mTotalSize = 0;
		private long mCopiedSize = 0;

		@Override
		protected Boolean doInBackground(String... params) {// string[0]:from
															// dir, string[1]:to
			// TODO Auto-generated method stub
			mTargetFolder = params[1];
			mTargetFolder = mTargetFolder.endsWith("/") ? mTargetFolder
					: mTargetFolder + "/";
			publishProgress(0);
			File in = new File(params[0]);
			try {
				mTotalSize = getFolderSize(in);
				if (mTotalSize == 0)
					return true;
			} catch (Exception e) {
				// TODO: handle exception
			}
			boolean flag = copy(params[0], params[1]);
			if (flag)
				publishProgress(100);
			return flag;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (result) {
				saveDataCopied(result);
				Toast.makeText(NoteBookPickerActivity.this,
						getResources().getString(R.string.successful_upgrade),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(NoteBookPickerActivity.this,
						getResources().getString(R.string.upgrade_fails),
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		private void saveDataCopied(boolean flag) {
			SharedPreferences preferences = SuperNoteApplication.getContext().getSharedPreferences("datacopy",
					MODE_PRIVATE);
			Editor editor = preferences.edit();
			editor.putBoolean(DataCopyActivity.DATA_COPIED, flag);
			editor.commit();
		}
		
		// 拷贝文件夹
		private boolean copy(String file1, String file2) {
			File in = new File(file1);
			final File out = new File(file2);

			if (!in.exists()) {
				return true;
			}

			if (!out.exists()) {
				out.mkdirs();
			} else {
				long folderSize = 0;
				try {
					folderSize = getBookFolderSize(out);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (folderSize > 0) {//如果目标文件夹有内容，我们将提示用户是否覆盖
					Log.i(TAG, "mTargetFolder:" + mTargetFolder);
					String parent = out.getParent() + "/";
					if (parent.equalsIgnoreCase(mTargetFolder)) {// TO_DIR下面的一级目录代表一个book;只需对比这一层
						synchronized (mLockObject) {
							try {
								mHandler.post(new Runnable() {

									@Override
									public void run() {
										handleFolderExits(out.getName());
									}
								});
								mLockObject.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if (!mOverwriteFloder) {// 不覆盖
							return true;
						}
					}
				}
			}
			// System.out.println("cpoy start");
			File[] file = in.listFiles();
			FileInputStream fis = null;
			FileOutputStream fos = null;
			for (int i = 0; i < file.length; i++) {
				if (file[i].isFile()) {
					try {
						fis = new FileInputStream(file[i]);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}

					try {
						final File f = new File(file2, file[i].getName());
						if (f.exists()
								&& f.getName().equalsIgnoreCase(".nomedia"))
							continue;
						if (f.exists()) {
							f.delete();
						}
						fos = new FileOutputStream(f);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}

					int c;
					byte[] b = new byte[1024 * 5];
					try {
						while ((c = fis.read(b)) != -1) {
							fos.write(b, 0, c);
							mCopiedSize += c;
						}
						fis.close();
						fos.flush();
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (mTotalSize != 0) {
						float progressF = (float) mCopiedSize
								/ (float) mTotalSize * 100;
						publishProgress((int) progressF);
					}
					// return true;
				} else {
					String s1 = file1.endsWith("/") ? file1 : file1 + "/";
					String s2 = file2.endsWith("/") ? file2 : file2 + "/";
					copy(s1 + file[i].getName(), s2 + file[i].getName());
				}
			}
			return true;
		}

		private String getBookName(String createdTime) {
			String bookName = createdTime;
			long createdTimeL = 0L;
			try {
				createdTimeL = Long.parseLong(createdTime);
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (createdTimeL <= 0)
				return bookName;
			BookCase bookCase = BookCase.getInstance(NoteBookPickerActivity.this);
			Log.i(TAG, "bookCase.getBookList().size():"
					+ bookCase.getBookList().size());
			for (NoteBook book : bookCase.getBookList()) {
				if (createdTimeL == book.getCreatedTime()) {
					bookName = book.getTitle();
					break;
				}
			}
			return bookName;
		}
		
		private void handleFolderExits(String folderName) {
			mBookName = getBookName(folderName);
			mOverwriteFloder = false;
			Dialog dialog = createFolderExitsDialog();
			dialog.show();
		}

		private AlertDialog createFolderExitsDialog() {
			AlertDialog.Builder builder = new Builder(NoteBookPickerActivity.this);
			builder.setTitle(getResources().getString(R.string.the_book) + "\""
					+ mBookName + "\"" + getResources().getString(R.string.exits));
			builder.setMessage(getResources().getString(R.string.replace_the_book));
			builder.setPositiveButton(getResources().getString(R.string.yes),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							mOverwriteFloder = true;
						}
					});

			builder.setNegativeButton(getResources().getString(R.string.no),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							mOverwriteFloder = false;
						}
					});
			AlertDialog dialog = builder.create();
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface arg0) {
					// TODO Auto-generated method stub
					// mOverwriteFloder = false;
					notifyCopyContinue();
				}
			});
			return dialog;
		}

		private void notifyCopyContinue() {
			synchronized (mLockObject) {
				mLockObject.notifyAll();
			}
		}
		
		//获得除了缩略图以外的book文件夹大小
		private long getBookFolderSize(java.io.File file){
			long folderSize = 0;
			try {
				folderSize = getFolderSize(file);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(folderSize <= 0)
				return 0;
			long thumbSize = 0;
			File thumbFile = new File(file, "thumb.info");
			if(thumbFile.exists() && thumbFile.isFile()){
				thumbSize = thumbFile.length();
			}
			long size = folderSize - thumbSize;
			return size > 0 ? size : 0;
		}
	}

	/**
	 * 获取文件夹大小
	 * 
	 * @param file
	 *            File实例
	 * @return long 单位为byte
	 * @throws Exception
	 */
	public long getFolderSize(java.io.File file) throws Exception {
		long size = 0;
		java.io.File[] fileList = file.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isDirectory()) {
				size = size + getFolderSize(fileList[i]);
			} else {
				size = size + fileList[i].length();
			}
		}
		return size;
	}
	/**********************************************
	 * CopyTask结束
	 ********************************************/
}
