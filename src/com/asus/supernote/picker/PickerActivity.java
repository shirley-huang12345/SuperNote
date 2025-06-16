package com.asus.supernote.picker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.SettingActivity;
import com.asus.supernote.classutils.ColorfulStatusActionBarHelper;
import com.asus.supernote.classutils.MethodUtils;
import com.asus.supernote.data.AsusFormatWriter;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.fota.DataFormat;
import com.asus.supernote.fota.V1DataConverter;
import com.asus.supernote.fota.V1DataFormat;
import com.asus.supernote.pdfconverter.PdfConverter;
import com.asus.supernote.ratingus.SuperNoteDialogFragment;
import com.asus.supernote.textsearch.TextSearchActivity;
import com.asus.supernote.ui.CoverHelper;
import com.asus.supernote.uservoice.UserVoiceConfig;
import com.asus.supernote.fota.CompatibleDataConverter;
import com.asus.supernote.ga.GACollector;
import com.asus.supernote.inksearch.CFG;
import com.asus.updatesdk.ZenUiFamily;

public class PickerActivity extends Activity {
	public static final String TAG = "PickerActivity";
	public static final String DIR = Environment.getExternalStorageDirectory()
			+ "/AsusSuperNote/";

	public static final int REQUEST_EXPORT_DIR_CHANGE = 3;
	// BEGIN: Better
	public static final int REQUEST_EXPORT_PDF_DIR_CHANGE = 5;
	// END: Better

	public static final int DISPLAY_NONE = -1;
	public static final int DISPLAY_ALL = 0;
	public static final int DISPLAY_PERSONAL = 5; // wendy
	public static final int DISPLAY_PAGE = 4;// MODIFY BY WENDY

	public static final int DELETE_BOOK_CONFIRM_DIALOG = 9;
	public static final int DELETE_BOOK_PROGRESS_DIALOG = 10;
	public static final int DELETE_BOOK_SUCCESS_DIALOG = 11;
	public static final int EXPORT_CONFIRM_DIALOG = 12;
	public static final int EXPORT_PROGRESS_DIALOG = 13;
	public static final int EXPORT_SUCCESS_DIALOG = 14;

	public static final int EXPORT_FAIL_DIALOG = 18;
	public static final int ADD_BOOK_DIALOG = 19;
	// BEGIN: Better
	public static final int EXPORT_PDF_CONFIRM_DIALOG = 20;
	public static final int EXPORT_PDF_PROGRESS_DIALOG = 21;
	public static final int EXPORT_PDF_SUCCESS_DIALOG = 22;
	public static final int EXPORT_PDF_FAIL_DIALOG = 23;
	// END: Better

	public static final int SORT_TYPE_DIALOG = 26;//Allen

	public static final int SHARE_FILE_PROGRESS_DIALOG = 27; //Carol
	public static final int SHARE_FILE_FAIL_DIALOG = 28; //Carol
	public boolean shareAsSupernoteFormat = false; //Carol
	private static int mDisplayType = DISPLAY_NONE;

	private ActionBar mActionBar;
	private FrameLayout mRightSideLayout;
	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	private PageGridFragment mPageGridFragment;
	private AllPageViewFragment mAllPageViewFragment;
	private AllPageViewFragment mFavoritesPageFragment;
	private TimeStampViewFragment mTimestampViewFragment;// wendy
	private PageGridFragment mPersonalPageFragment;
	private int deviceType;
	private SharedPreferences mPreference;
	private SharedPreferences.Editor mPreferenceEditor;
	private Resources mResources;
	private Context mContext;
	private BookCase mBookcase;
	
	private static PickerActivity mCurrentInstance = null;

	private static boolean mIsDeleteBookTaskRunning = false;
	private static boolean mIsExportTaskRunning = false;
	private static boolean mIsShareTaskRunning = false; //Carol
	// BEGIN: Better
	private static boolean mIsExport2PdfTaskRunning = false;
	// END: Better

	private static DeleteBookTask mDeleteBooksTask = null;
	private static ExportTask mExportTask = null;
	// BEGIN: Better
	private static Export2PdfTask mExport2PdfTask = null;
	// END: Better

	private static ShareFilesTask mShareTask = null; //Carol
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

	private static EditText mAddBookFileNameEditText;
	private static Spinner mAddBookPageSizeSpinner;
	private static RadioGroup mAddBookPageColor;
	private static RadioGroup mAddBookPageStyle;
	// BEGIN: Better
	private static EditText mExportPDFFileNameEditText;
	// END: Better

	private static Context scontext = null;// wendy

	// BEGIN: Better
	// @Override
	public void onResume() {
		super.onResume();
		
		//smilefish add for runtime permission
		if(MethodUtils.needShowPermissionPage(scontext)){
			MethodUtils.showPermissionPage(this, false);
		}
	}

	private boolean mIsFirstLoad = false;
	private BroadcastReceiver updateUiReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(MetaData.SYNC_UPDATE_UI)) {
				updateUI(); // Better
			}
		}
	};

	// END: Better

	private long mBookId = 0L;

	//darwin
	public Bitmap getThumbnailNoBackgroundInPageView(PageDataLoader loader, boolean isLoadAsync, NoteBook notebook ,NotePage page ,int color, int line) {
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
	public void saveBookCoverThumbInPageView(PageDataLoader loader, boolean isLoadAsync, NoteBook book ,NotePage page)
    {
    	try {
            Bitmap bitmap = getThumbnailNoBackgroundInPageView(loader, isLoadAsync, book,page,book.getBookColor(),book.getGridType());
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
	//darwin
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		mCurrentInstance = this;
		scontext = getApplicationContext();// wendy
		// BEGIN: Better
		if (MetaData.AppContext == null) {
			MetaData.AppContext = scontext;
		}
		// END: Better
		// begin darwin
		mBookId = getIntent().getLongExtra(MetaData.BOOK_ID, 0L);
		// end darwin

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
        
		super.onCreate(savedInstanceState);

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

		mPreference = getSharedPreferences(
				MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		mPreferenceEditor = mPreference.edit();
		mResources = getResources();

		if (mPreference.getBoolean(
				mResources.getString(R.string.pref_first_load), true) == false) {

			mContentResolver = getContentResolver();
			mBookcase = BookCase.getInstance(PickerActivity.this);
			ColorfulStatusActionBarHelper.setContentView(R.layout.picker, true, this);//smilefish

			mFragmentManager = getFragmentManager();
			mRightSideLayout = (FrameLayout) findViewById(R.id.right_side);

			int orignalDisplayType = DISPLAY_ALL;
			setDisplayType(orignalDisplayType);

			mActionBar = getActionBar();
			// begin darwin
			NoteBook book = mBookcase.getNoteBook(mBookId);
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(MetaData.CHOOSE_BOOKS);
			intentFilter.addAction(MetaData.SYNC_UPDATE_UI);
			intentFilter.addAction(MetaData.LOGIN_MESSAGE);
			registerReceiver(updateUiReceiver, intentFilter);
			if(book == null)
			{
				Log.e(TAG, "book is null");

				//Begin Allen ++ catch book not exist exception
	            Toast.makeText(this, R.string.memo_not_exists, Toast.LENGTH_SHORT).show();
				finish();
				if(getIntent().getBooleanExtra(MetaData.START_FROM_WIDGET, false)){
					if(!MetaData.SuperNoteUpdateInfoSet.containsKey(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_WIDGET_DATA_INVALID)){
		    			MetaData.SuperNoteUpdateInfoSet.put(MetaData.SuperNoteUpdateFrom.SUPERNOTE_UPDATE_FROM_WIDGET_DATA_INVALID,null);
		    		}
		            Intent updateIntent = new Intent();
		    		updateIntent.setAction(MetaData.ACTION_SUPERNOTE_UPDATE);
		    		updateIntent.putExtra(MetaData.EXTRA_SUPERNOTE_UPDATE_FROM, MetaData.SuperNoteUpdateInfoSet);
		    		sendBroadcast(updateIntent);
		    		MetaData.SuperNoteUpdateInfoSet.clear();
				}
	          //End Allen
	            return;
			}
			if(mActionBar == null)
			{
				Log.e(TAG, "ActionBar is null");
				super.onBackPressed();
				return;
			}
			mActionBar.setTitle(book.getTitle());
			mActionBar.setDisplayHomeAsUpEnabled(true);
			// end darwin
			mRightSideLayout.setOnDragListener(dragListener);

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
			// begin darwin
			mBookcase.setCurrentBook(mBookId);
			// end darwin
			// BEGIN: Better

			getActionBar().setDisplayShowHomeEnabled(false); //by smilefish

			// check fota data
			boolean isLoadFotaData = mPreference.getBoolean(
					mResources.getString(R.string.load_fota_data), false);
			if (isLoadFotaData) {
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
						mPreferenceEditor.putBoolean(
								mResources.getString(R.string.load_fota_data),
								false);
						mPreferenceEditor.commit();
					}
				}
			}

			mIsFirstLoad = false;
			MetaData.IS_LOAD = true;  //Carol
			// END: Better
		} else {
			mIsFirstLoad = true;
			MetaData.IS_LOAD = false;  //Carol
			NoteBookPickerActivity.firstRunLoadResource(mPreference, mResources, this);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//begin darwin
		if(NoteBookPickerActivity.getConfigurationChangeBy() > NoteBookPickerActivity.CONFIGURATIONCHANGE_BY_PICKER)
		{
			NoteBookPickerActivity.setConfigurationChangeBy(NoteBookPickerActivity.CONFIGURATIONCHANGE_BY_PICKER);
			this.recreate();
		}
		else
		{
			NoteBookPickerActivity.setConfigurationChangeBy(NoteBookPickerActivity.CONFIGURATIONCHANGE_BY_PICKER);
		//end   darwin

		ColorfulStatusActionBarHelper.setContentView(R.layout.picker, true, this);//smilefish

		mRightSideLayout = (FrameLayout) findViewById(R.id.right_side);
		mRightSideLayout.setOnDragListener(dragListener);

		// END: Better
		int orignalDisplayType = DISPLAY_ALL;
		setDisplayType(orignalDisplayType);
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		detachFragment(fragmentTransaction);
		reAttachFragment(fragmentTransaction);
		fragmentTransaction.commit();
		
		//begin smilefish
		if (mPageGridFragment != null && mPageGridFragment.isActionMode())
		{
			mPageGridFragment.updateSelectionCountTitle();
		}
		//end smilefish
		}
	}

	public void detachFragment(FragmentTransaction fragmentTransaction) {
		mFragmentManager = getFragmentManager();
		switch (mDisplayType) {
		case DISPLAY_ALL:
			mPageGridFragment = (PageGridFragment) mFragmentManager
					.findFragmentByTag(PageGridFragment.TAG);
			if (mPageGridFragment != null) {
				fragmentTransaction.detach(mPageGridFragment);//Modified by show
			}
			break;
		}
	}

	public void reAttachFragment(FragmentTransaction fragmentTransaction) {
			if (mPageGridFragment != null) {
				fragmentTransaction.attach(mPageGridFragment);//Modified by show
			}
			mRightSideLayout
					.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onPause() {
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
        //End Allen
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// BEGIN: Better
		if (!mIsFirstLoad) {
			unregisterReceiver(updateUiReceiver);
		}
		// END: Better
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
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
		case R.id.menu_encourageUs:
		{
			if(mPageGridFragment == null) return false;
			FragmentManager fm = mPageGridFragment.getChildFragmentManager();
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
		case R.id.menu_search: //add by smilefish
            try {
				intent = new Intent();
				intent.setClass(this, TextSearchActivity.class);
				intent.putExtra("bookId",mBookId);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(intent);

			} catch (ActivityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		// begin emmanual
		case R.id.menu_sort:
			showDialog(SORT_TYPE_DIALOG);
			return true;
		}
		// end emmanual
		return false;
	}

	private void showUserVoice() {
	    // TODO Auto-generated method stub
		UserVoiceConfig.init(mContext);
    }

	private void displayDefaultType() {
		mRightSideLayout.setVisibility((deviceType > 100) ? View.VISIBLE : View.GONE);

        if (mFragmentManager == null) {
            mFragmentManager = getFragmentManager();
        }
		mFragmentTransaction = mFragmentManager.beginTransaction();
		if (deviceType > 100) {
			// begin darwin
			mRightSideLayout.setVisibility(View.VISIBLE);
			mPageGridFragment = (PageGridFragment) mFragmentManager
					.findFragmentByTag(PageGridFragment.TAG);
			if (mPageGridFragment == null) {
				mPageGridFragment = new PageGridFragment();
				mFragmentTransaction.replace(R.id.right_side,
						mPageGridFragment, PageGridFragment.TAG);
			}
			// end darwin
		} else {
			mRightSideLayout.setVisibility(View.VISIBLE);
			mPageGridFragment = (PageGridFragment) mFragmentManager
					.findFragmentByTag(PageGridFragment.TAG);
			if (mPageGridFragment == null) {
				mPageGridFragment = new PageGridFragment();
				mFragmentTransaction.replace(R.id.right_side,
						mPageGridFragment, PageGridFragment.TAG);
			}
		}
		mFragmentTransaction.commit();
		mDisplayType = DISPLAY_ALL;
	}

	public void displayPageGridView(Long bookId, boolean isPrivate) {
		mRightSideLayout.setVisibility(View.VISIBLE);
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
					mPageGridFragment = (PageGridFragment) getFragmentManager()
							.findFragmentByTag(PageGridFragment.TAG);
					if (mPageGridFragment != null
							&& mPageGridFragment.getDraggedView() != null) {
						mPageGridFragment.getDraggedView().setVisibility(
								View.VISIBLE);
					}
				} else {
					mPersonalPageFragment = (PageGridFragment) getFragmentManager()
							.findFragmentByTag(PageGridFragment.ALIAS_TAG);
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
					mPageGridFragment = (PageGridFragment) getFragmentManager()
							.findFragmentByTag(PageGridFragment.TAG);
					if (mPageGridFragment != null
							&& mPageGridFragment.getDraggedView() != null) {
						mPageGridFragment.getDraggedView().setVisibility(
								View.VISIBLE);
					}
				} else {
					mPersonalPageFragment = (PageGridFragment) getFragmentManager()
							.findFragmentByTag(PageGridFragment.ALIAS_TAG);
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

	public void setDisplaySelectorEnabled(boolean enabled) {
		if (enabled) {
			NoteBook book = mBookcase.getNoteBook(mBookId);
			mActionBar.setTitle(book.getTitle());
			mActionBar.setDisplayHomeAsUpEnabled(true);

		}
	}

	@Override
	public void onBackPressed() {
		if (mDisplayType == DISPLAY_PAGE) {
			NoteBook book = mBookcase.getNoteBook(mBookId);
			mActionBar.setTitle(book.getTitle());
			mActionBar.setDisplayHomeAsUpEnabled(true);
			setDisplayType(mPageGridFragment.isPrivateMode() ? DISPLAY_PERSONAL
					: DISPLAY_ALL);
		} else if (mDisplayType != DISPLAY_ALL) {
			NoteBook book = mBookcase.getNoteBook(mBookId);
			mActionBar.setTitle(book.getTitle());
			mActionBar.setDisplayHomeAsUpEnabled(true);
			setDisplayType(DISPLAY_ALL);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_EXPORT_DIR_CHANGE
				&& resultCode == Activity.RESULT_OK) {
			String path = data.getStringExtra("FILE_PATH");
			if (path == null) {
				return;
			}
			File file = new File(path);
			if (file.exists() == false) {
				file.mkdirs();
			}
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
	}
	
	// BEGIN: Better
	public void updateUI() {
		mPageGridFragment = (PageGridFragment) mFragmentManager
				.findFragmentByTag(PageGridFragment.TAG);
		if (mPageGridFragment != null) {
			long bookid = mBookcase.getCurrentBookId();
			NoteBook book = mBookcase.getNoteBook(bookid);
			if ((bookid == BookCase.NO_SELETED_BOOK) || (book == null)) {
				finish();
			} else {
				if(mActionBar != null) {
					mActionBar.setTitle(book.getTitle());
				}
				mPageGridFragment.changeData(bookid);// wendy
			}
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

		mPersonalPageFragment = (PageGridFragment) mFragmentManager
				.findFragmentByTag(PageGridFragment.ALIAS_TAG);
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
	}
	// END: Better

	public void changeTitle(NoteBook book)
	{
		if(mActionBar != null && book != null)
		{
			mBookId = book.getCreatedTime();
			mActionBar.setTitle(book.getTitle());
		}
	}
	
	public void updateFragment() {
		mPageGridFragment = (PageGridFragment) mFragmentManager
				.findFragmentByTag(PageGridFragment.TAG);
		if (mPageGridFragment != null) {
			mBookId = mBookcase.getCurrentBookId();
			mPageGridFragment.changeData(mBookId);// wendy
			mPageGridFragment.setNormalModeMenu();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	}

	private void setDisplayType(int index) {
		switch (index) {
		case DISPLAY_ALL:
			displayDefaultType();
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		AlertDialog.Builder builder = null;
		switch (id) {
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
							//begin:clare
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
			DialogInterface.OnClickListener listener = null;
			dialog.setButton(Dialog.BUTTON_NEGATIVE, mResources.getString(android.R.string.cancel), listener);
			// END: Better
			return dialog;
		}
		case SHARE_FILE_PROGRESS_DIALOG:{ //Carol
			ProgressDialog dialog = new ProgressDialog(mContext);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setCancelable(false);
			dialog.setTitle(R.string.editor_menu_share); //share- title of the process dialog
			DialogInterface.OnClickListener listener = null;
			dialog.setButton(Dialog.BUTTON_NEGATIVE, mResources.getString(android.R.string.cancel), listener);
			return dialog;
		}
		case EXPORT_SUCCESS_DIALOG: {
			// Done
			View view = View.inflate(this, R.layout.one_msg_dialog, null);
			final TextView textView = (TextView) view
					.findViewById(R.id.msg_text_view);
			textView.setText(args.getString("msg",
					mResources.getString(R.string.book_export)));
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
		case ADD_BOOK_DIALOG: {
			// Done
			String[] options = mResources.getStringArray(R.array.pg_size);
			List<HashMap<String, Object>> contents = new ArrayList<HashMap<String, Object>>();
			{
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("icon", R.drawable.asus_supernote_cover_pad2);
				map.put("title", options[0]);
				contents.add(map);
			}
			{
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("icon", R.drawable.asus_supernote_cover_phone2);
				map.put("title", options[1]);
				contents.add(map);
			}
			View view = View.inflate(mContext, R.layout.notebooks_new, null);
			mAddBookFileNameEditText = (EditText) view
					.findViewById(R.id.nb_name);
			mAddBookPageSizeSpinner = (Spinner) view
					.findViewById(R.id.pageSize);
			mAddBookPageColor = (RadioGroup) view
					.findViewById(R.id.nb_pagecolor);
			mAddBookPageStyle = (RadioGroup) view.findViewById(R.id.nb_style);
			mAddBookPageSizeSpinner.setAdapter(new SimpleAdapter(this,
					contents, R.layout.page_size_spinner_item, new String[] {
							"icon", "title" }, new int[] { R.id.book_size,
							R.id.book_title }));
			mAddBookPageSizeSpinner.setSelection((deviceType > 100) ? 0 : 1);
			builder = new AlertDialog.Builder(mContext);
			builder.setView(view);
			builder.setCancelable(true);
			builder.setTitle(R.string.add_new);
			builder.setPositiveButton(android.R.string.ok, addBookListener);
			builder.setNegativeButton(android.R.string.cancel, addBookListener);
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
							try {
								Intent intent = new Intent();
								intent.putExtra("DEFAULT_PATH", path.toString());
								intent.setClassName("com.asus.filemanager",
										"com.asus.filemanager.dialog.FolderSelection");
								startActivityForResult(intent,
										REQUEST_EXPORT_PDF_DIR_CHANGE);
							//begin:clare
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
					mResources.getString(R.string.book_export)));
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
		case SHARE_FILE_FAIL_DIALOG: { //Carol
			View pdfFailedView = View.inflate(this, R.layout.one_msg_dialog,
					null);
			final TextView pdfFailedTextView = (TextView) pdfFailedView
					.findViewById(R.id.msg_text_view);
			pdfFailedTextView.setText(R.string.prompt_err_open);
			builder = new AlertDialog.Builder(mContext);
			builder.setCancelable(false);
			builder.setTitle(R.string.editor_menu_share);//book_export);
			builder.setView(pdfFailedView);
			builder.setPositiveButton(android.R.string.ok, null);
			return builder.create();
		}
		// END: Better
		//Begin Allen
		case SORT_TYPE_DIALOG:
	        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
	        View view = View.inflate(this, R.layout.stop_time_dialog, null);
	        builder1.setTitle(R.string.Sort);
	        builder1.setView(view);
	        builder1.setNegativeButton(android.R.string.cancel, null);
	        final AlertDialog dialog = builder1.create();	        
	        return dialog; 
		//End Allen
		}
		return null;

	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		super.onPrepareDialog(id, dialog, args);
		switch (id) {
		case DELETE_BOOK_PROGRESS_DIALOG: {
			// Done
			ProgressDialog d = (ProgressDialog) dialog;
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
							if (((isToRename && !mExportFileNameEditText
									.getText().toString().isEmpty()) || !isToRename)
									&& !mIsExportTaskRunning) {
								removeDialog(EXPORT_CONFIRM_DIALOG);
								if (mExportTask != null) {
									if (isToRename) {
										mExportTask
												.setFileName(mExportFileNameEditText
														.getText().toString());
									}
									mExportTask.execute();
								}
							}
						}
					});
			break;
		}
		case EXPORT_PROGRESS_DIALOG: {
			// Done
			ProgressDialog d = (ProgressDialog) dialog;
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
		case ADD_BOOK_DIALOG: {
			// Done
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					removeDialog(ADD_BOOK_DIALOG);
				}
			});
			break;
		}

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
		case SHARE_FILE_PROGRESS_DIALOG:{ //Carol
			ProgressDialog shareProgressDialog = (ProgressDialog) dialog;
			shareProgressDialog.setMax(args.getInt("max", 1));
			final Button b = shareProgressDialog.getButton(Dialog.BUTTON_NEGATIVE);
			b.setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if ((mShareTask != null)&& mIsShareTaskRunning) {
								mShareTask.cancel(true);
								b.setEnabled(false);
							}
							if( shareAsSupernoteFormat && mExportTask != null && mIsExportTaskRunning){
								mExportTask.cancel(true);
								b.setEnabled(false);
							}
						}
					});
			if (mIsShareTaskRunning) 
				mShareTask.setDialog(shareProgressDialog);
			if (shareAsSupernoteFormat && mIsExportTaskRunning)
				mExportTask.setDialog(shareProgressDialog);
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
	            	mPageGridFragment.SortNotePage(position);
	                removeDialog(SORT_TYPE_DIALOG); // Better
	            }
	        });
			countdownList.setItemChecked(mPageGridFragment.getSortOrder()==null?0:1,true);
			break;
		}
		//End Allen
		}
	}

	/********************************************************
	 * Delete NoteBook START
	 *******************************************************/
	public void deleteBook(Activity activity, Long id) {
		Bundle b = new Bundle();
		b.putLong(MetaData.BOOK_ID, id);
		showDialog(DELETE_BOOK_CONFIRM_DIALOG, b);
	}

	private class DeleteBookTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog mProgressDialog;
		private Long mBookId;
		private int mMax;
		private int mCount;

		public DeleteBookTask(Long id) {
			mBookId = id;
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

		@Override
		protected Void doInBackground(Void... params) {
			NoteBook notebook = mBookcase.getNoteBook(mBookId);
			notebook.deleteDir(new File(MetaData.DATA_DIR, Long
					.toString(mBookId)));
			publishProgress();
			// begin wendy
			String selection = NoteBookPickerActivity.getIsLock() ? "is_locked = 0" : null;
			// end wendy
			Cursor cursor = mContentResolver.query(MetaData.BookTable.uri,
					null, selection, null, null);
			if (cursor != null) {
				if(cursor.getCount() > 0) {
					cursor.moveToFirst();
					while (cursor.isAfterLast() == false) {
						int index = cursor.getPosition();
						if (cursor.getLong(MetaData.BookTable.INDEX_CREATED_DATE) == mBookId
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
				}
				cursor.close();
			}
			publishProgress();
			String[] selectionArgs = new String[] { Long.toString(mBookId) };

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
			ContentValues cvpage = new ContentValues();
			cvpage.put(MetaData.PageTable.IS_DELETED, 1);
			mContentResolver.update(MetaData.PageTable.uri, cvpage,
					"owner = ?", selectionArgs);
			// add by wendy 0401 delete -> update end---
			publishProgress();

			mBookcase.removeBookFromList(notebook);
			publishProgress();
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
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mIsDeleteBookTaskRunning = false;
				mProgressDialog.dismiss();
				updateFragment();
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

	private class Export2PdfTask extends ExportShareCommonTask{ //modified by Carol 20130326
		private ProgressDialog mProgressDialog;
		private String mFileName;
		private String mExportPdfDir = "";
		private String mTitle = "";
		private static final int MSG_UPDATE_PROGRESS = 0;
		private static final int MSG_EXPORT_TITLE = 1;
		private int mProgressStatus = MSG_UPDATE_PROGRESS;
		
		public Export2PdfTask(Activity activity, SortedSet<SimplePageInfo> items) {
			super(activity, items);
		}

		public Export2PdfTask(Activity activity,
				SortedSet<SimplePageInfo> items, String title) {
			super(activity, items, title);
			mFileName = super.mFileName;
			mTitle = super.mTitle;
		}

		public Export2PdfTask(Activity activity, ArrayList<Long> bookIds) {
			super(activity, bookIds);
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
				mProgressDialog.setMax(super.mMax);
				String title = mCurrentInstance.getResources().getString(R.string.export) + " " + mTitle;
				mProgressDialog.setTitle(title);
			}
			
			mProgressDialog.setProgress(super.mCount);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsExport2PdfTaskRunning = true;
			Bundle b = new Bundle();
			b.putInt("max", super.mMax);
			showDialog(EXPORT_PDF_PROGRESS_DIALOG, b);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (values != null) {
				if (values[0] == MSG_EXPORT_TITLE) {
					mProgressStatus = MSG_EXPORT_TITLE;
					mProgressDialog.setMax(super.mMax);
					String title = mCurrentInstance.getString(R.string.export) + " " + mTitle;
					mProgressDialog.setTitle(title);
				}
				
				mProgressDialog.setProgress(super.mCount);
			}
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if (Looper.myLooper() == null) {
				Looper.prepare();
			}
			mExportPdfDir = mPreference.getString(
					mResources.getString(R.string.pref_export_pdf_dir),
					MetaData.EXPORT_PDF_DIR);
			boolean value = super.exportPages(true, mExportPdfDir);
			mFileName = super.mFileName;
			return value;
			// END: Shane_Wang 2012-10-25
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mIsExport2PdfTaskRunning = false;
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mCurrentInstance.removeDialog(EXPORT_PDF_PROGRESS_DIALOG);
				int promptid = R.string.prompt_export_pdf_to;
				if (super.mPdfFileCount > 1) {
					promptid = R.string.prompt_exports_pdf_to;
				}
				if (super.mIsExportBook) {
					String msg = mCurrentInstance.getResources().getString(
							promptid);
					msg = msg + "\n" + mExportPdfDir;
					Bundle b = new Bundle();
					b.putString("msg", msg);
					mCurrentInstance.showDialog(EXPORT_PDF_SUCCESS_DIALOG, b);
				} else {
					File file = new File(super.mFileName);
					if (!file.exists()) {
						mCurrentInstance.showDialog(EXPORT_PDF_FAIL_DIALOG);
					} else {
						Uri uri = Uri.parse("file://" + file);
						mCurrentInstance.sendBroadcast(new Intent(
								Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
						String msg = mCurrentInstance.getResources()
								.getString(promptid);
						msg = msg + "\n" + mFileName.substring(0, mFileName.lastIndexOf('/'));
						Bundle b = new Bundle();
						b.putString("msg", msg);
						mCurrentInstance.showDialog(EXPORT_PDF_SUCCESS_DIALOG, b);
					}
				}
			}
		}

		@Override
		protected void onCancelled(Boolean result) {
			mIsExport2PdfTaskRunning = false;

			for (String path : super.mFiles) {
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				}
			}
			
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mCurrentInstance.removeDialog(EXPORT_PDF_PROGRESS_DIALOG);
				mCurrentInstance.updateFragment();
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
		mExportTask = new ExportTask(activity, items, title, true);
		showDialog(EXPORT_CONFIRM_DIALOG);
	}

  	private class ExportTask extends AsyncTask<String, Integer, Boolean> { //modified by Carol 20130326
  		//[Carol]use boolean var 'shareAsSupernoteFormat' to distinguish the export task and share supernote format task
  		private ProgressDialog mProgressDialog;
		private SortedSet<SimplePageInfo> mItems;
		private String mFileName;
		// BEGIN: Better
		private int mMax = 0;
		private int mProgress = 0;
		private String mExportSneDir = "";
		private String mTitle = "";
		private static final int MSG_UPDATE_PROGRESS = 0;
		private static final int MSG_TRANSFER_TITLE = 1;
		private static final int MSG_EXPORT_TITLE = 2;
		private ArrayList<String> mFiles = new ArrayList<String>();
		// END: Better
		
		private int mProgressStatus = MSG_UPDATE_PROGRESS;
		private boolean isExporting = false;
		
		public ExportTask(Activity activity, SortedSet<SimplePageInfo> items) {
			if (items != null) {
				mItems = new TreeSet<SimplePageInfo>(items);
			} else {
				mItems = new TreeSet<SimplePageInfo>();
			}
			mFileName = MetaData.EXPORT_PREFIX + "_"
					+ System.currentTimeMillis() + MetaData.EXPORT_EXTENSION;
			// BEGIN: Shane_Wang@asus.com 2013-1-7
			mMax = mItems.size();
			// END: Shane_Wang@asus.com 2013-1-7
		}

		public ExportTask(Activity activity, SortedSet<SimplePageInfo> items,
				String title, boolean isExport) {
			if (items != null) {
				mItems = new TreeSet<SimplePageInfo>(items);
			} else {
				mItems = new TreeSet<SimplePageInfo>();
			}
			
			// BEGIN: Better
			// BEGIN: Shane_Wang@asus.com 2013-1-7
			mMax = mItems.size();
			// END: Shane_Wang@asus.com 2013-1-7
			mFileName = title + MetaData.EXPORT_EXTENSION;
			mTitle = title;
			// END: Better
			isExporting = isExport;
			shareAsSupernoteFormat = !isExport;
		}

		public void setFileName(String fileName) {
			mFileName = fileName;
			mTitle = fileName;
			if (!mFileName.toLowerCase().endsWith(MetaData.EXPORT_EXTENSION)) {
				mFileName += MetaData.EXPORT_EXTENSION;
			}
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

		// BEGIN: Better
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (values != null) {
				if(isExporting){
					if (values[0] == MSG_TRANSFER_TITLE) {
						mProgressStatus = MSG_TRANSFER_TITLE;
						String title = mCurrentInstance.getResources().getString(R.string.transfer) + " " + mTitle;
						mProgressDialog.setTitle(title);
					}
					if (values[0] == MSG_EXPORT_TITLE) {
						mProgressStatus = MSG_EXPORT_TITLE;
						String title = mCurrentInstance.getResources().getString(R.string.export) + " " + mTitle;
						mProgressDialog.setTitle(title);
					}
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
			b.putInt("max", (mItems == null) ? 0 : mMax);
			showDialog(isExporting ? EXPORT_PROGRESS_DIALOG : SHARE_FILE_PROGRESS_DIALOG, b);
		}

		private boolean exportPages(){
			
			File exportDir = new File(mExportSneDir);
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
						if(isExporting) //Carol
							publishProgress(MSG_TRANSFER_TITLE);
						PageDataLoader loader = new PageDataLoader(mContext);
						for (SimplePageInfo info : mItems) {
							NotePage page = book.getNotePage(info.pageId);
							if ((page != null) && (page.getVersion() == 1)) {
								loader.load(page);
								page.save(loader.getAllNoteItems(),//Allen
										loader.getDoodleItem());
								genThumb(loader, true, 
										page,
										book.getPageSize() == MetaData.PAGE_SIZE_PHONE);
								mProgress++;
								publishProgress(MSG_UPDATE_PROGRESS);
							}
							if (isCancelled()) {
								zos.close();
								return false;
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
				if(isExporting) //Carol
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
					zipDir(this, dirToZip, zos, info.bookId, info.pageId);
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
		
		@Override
		protected Boolean doInBackground(String... params) {
			if (Looper.myLooper() == null) {
				Looper.prepare();
			}
			
			// BEGIN: Better
			mExportSneDir = mPreference.getString(
					mResources.getString(R.string.pref_export_dir),
					isExporting? MetaData.EXPORT_DIR : MetaData.SHARE_DIR);
			exportPages();
			return true;
		}
		
		// BEGIN: Better
		@Override
		protected void onCancelled(Boolean result) {
			mIsExportTaskRunning = false;
			shareAsSupernoteFormat = false;
			for (String path : mFiles) {
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				}
			}
			
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				removeDialog(isExporting ? EXPORT_PROGRESS_DIALOG : SHARE_FILE_PROGRESS_DIALOG);
				mCurrentInstance.updateFragment();
			}
		}

		// END: Better

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mIsExportTaskRunning = false;
			// BEGIN: archie_huang@asus.com
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				removeDialog(isExporting ? EXPORT_PROGRESS_DIALOG : SHARE_FILE_PROGRESS_DIALOG);
				if (result) {
					if(isExporting){
						File file = new File(mFileName);
						if (file.exists() == false) {
							mCurrentInstance.showDialog(EXPORT_FAIL_DIALOG);
						} else {
							Uri uri = Uri.parse("file://" + file);
							mCurrentInstance.sendBroadcast(new Intent(
									Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
							String msg = mCurrentInstance.getResources().getString(
									R.string.prompt_export_to_);
							msg = msg + "\n" + mFileName.substring(0, mFileName.lastIndexOf('/'));
							Bundle b = new Bundle();
							b.putString("msg", msg);
							mCurrentInstance.showDialog(EXPORT_SUCCESS_DIALOG, b);
						}
					}else{
						if(mExportTask.mFileName == null)
							mCurrentInstance.showDialog(SHARE_FILE_FAIL_DIALOG);
						else{
							File file = new File(mFileName);
							Uri uri = Uri.parse("file://" + file);
							mCurrentInstance.sendBroadcast(new Intent(
									Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
							ArrayList<String> pathList = new ArrayList<String>();
							pathList.add(mExportTask.mFileName);
							Intent shareIntent = shareMultipleFileIntent(pathList, "sne");
							mCurrentInstance.startActivity(shareIntent);
							mCurrentInstance.updateFragment();
						}
					}
				} else {
					mCurrentInstance.showDialog(isExporting ? EXPORT_FAIL_DIALOG : SHARE_FILE_FAIL_DIALOG);
				}
			}
			shareAsSupernoteFormat = false;
			// END: archie_huang@asus.com
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
			 b.putBoolean("isPrivate", isPrivate);//darwin
			// end wendy
			b.putInt("style", CopyPageDialogFragment.COPY_SELECT_DIALOG);
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
			b.putInt("style", CopyPageDialogFragment.COPY_CONFIRM_DIALOG);
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
			b.putBoolean("isPrivate", NoteBookPickerActivity.getIsLock());
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
			b.putBoolean("isPrivate", NoteBookPickerActivity.getIsLock());
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
		//mars fix bug for monkey test
		if(destBook == null)
			return;
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
		return NoteBookPickerActivity.getIsLock();
	}

	// end wendy

	private DialogInterface.OnClickListener addBookListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == Dialog.BUTTON_NEGATIVE) {
				removeDialog(ADD_BOOK_DIALOG);
				return;
			}
			NoteBook book = new NoteBook(mContext);
			if (mAddBookFileNameEditText == null
					|| mAddBookFileNameEditText.getText().toString().equals("")) {
				book.setTitle(PickerUtility
						.getDefaultBookName(getApplicationContext()));
			} else {
				book.setTitle(mAddBookFileNameEditText.getText().toString());
			}
			int pageSize = MetaData.PAGE_SIZE_PAD;
			switch (mAddBookPageSizeSpinner.getSelectedItemPosition()) {
			case 0:
				pageSize = MetaData.PAGE_SIZE_PAD;
				break;
			case 1:
				pageSize = MetaData.PAGE_SIZE_PHONE;
				break;
			}
			int color = MetaData.BOOK_COLOR_WHITE;
			switch (mAddBookPageColor.getCheckedRadioButtonId()) {
			case R.id.nb_yellow:
				color = MetaData.BOOK_COLOR_YELLOW;
				break;
			case R.id.nb_white:
				color = MetaData.BOOK_COLOR_WHITE;
				break;
			}
			int style = MetaData.BOOK_GRID_LINE;
			switch (mAddBookPageStyle.getCheckedRadioButtonId()) {
			case R.id.nb_grid_line:
				style = MetaData.BOOK_GRID_LINE;
				break;
			case R.id.nb_grid_grid:
				style = MetaData.BOOK_GRID_GRID;
				break;
			}
			book.setPageSize(pageSize);
			book.setBookColor(color);
			book.setGridType(style);
			book.setIsLocked(false);
			NotePage notepage = new NotePage(getApplicationContext(),
					book.getCreatedTime());
			book.addPage(notepage);
			mBookcase.addNewBook(book);
			updateFragment();
			removeDialog(ADD_BOOK_DIALOG);
			// BNGIN, James5
			mDisplayType = DISPLAY_PAGE;
			// BEGIN: archie_huang@asus.com
			// To avoid NullPointerException when back to Launcher
			// NullPointerException is caused in function onBackPressed(),
			// mPageGridFragment == null
			if (deviceType <= 100) {
				displayPageGridView(book.getCreatedTime(), false);
			}
			// END: archie_huang@asus.com
			// BEGIN: Better
			PageDataLoader loader = PageDataLoader.getInstance(mContext);
			loader.load(notepage);
			// END: Better
			try {
				Intent intent = new Intent();
				intent.setClass(mContext, EditorActivity.class);
				intent.putExtra(MetaData.BOOK_ID, notepage.getOwnerBookId());
				intent.putExtra(MetaData.PAGE_ID, notepage.getCreatedTime());
				mContext.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// End, James5
		}
	};

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
		Canvas contentCanvas;
		content = Bitmap.createBitmap(targetWidth, targetHeight,
				Bitmap.Config.ARGB_8888);
		content.setDensity(Bitmap.DENSITY_NONE);
		contentCanvas = new Canvas(content);
		if (!isLoadAsync) {
			loader.load(page);
		}
		page.load(loader, isLoadAsync, contentCanvas, true, false, false);
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
	
	//begin darwin
	public void genThumb(PageDataLoader loader, boolean isLoadAsync, NoteBook book, long pageId) {
		Bitmap cover = null;
		int color = book.getBookColor();
		int line = book.getGridType();
		NotePage page = book.getNotePage(pageId);
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
		Canvas contentCanvas;
		content = Bitmap.createBitmap(targetWidth, targetHeight,
				Bitmap.Config.ARGB_8888);
		content.setDensity(Bitmap.DENSITY_NONE);
		contentCanvas = new Canvas(content);
		if (!isLoadAsync) {
			loader.load(page);
		}
		page.load(loader, isLoadAsync, contentCanvas, true, false, false);
		float left = res.getDimension(R.dimen.thumb_padding_left);
		float top = res.getDimension(R.dimen.thumb_padding_top);
		canvas.translate(left, top);
		canvas.drawBitmap(content, 0, 0, paint);
		content.recycle();
		content = null;
		File file = new File(page.getFilePath(), MetaData.THUMBNAIL_PREFIX);
		if (file.exists()) {
			try {
				file.delete();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
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
	//end  darwin

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
	
	//Begin Carol
	/********************************************************
	 * Share the data of SuperNote to other apps
	 *******************************************************/
	
	private Intent shareMultipleFileIntent(ArrayList<String> pathList, String shareType) {
        try {
        	ArrayList<Uri> fileUris = new ArrayList<Uri>();
        	Intent intent = new Intent();
        	if(shareType == "image") //TT344967&344983[Carol]
        		intent.setType("image/*");
        	else if(shareType == "sne")
        		intent.setType("application/zip");
        	else
        		intent.setType("*/*");
        	for(int i=0;i<pathList.size();i++)
        		fileUris.add(Uri.fromFile(new File(pathList.get(i))));
			intent.setAction(Intent.ACTION_SEND_MULTIPLE);
			intent.putExtra(Intent.EXTRA_SUBJECT, "");
			intent.putExtra(Intent.EXTRA_TEXT, "");
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris);
			return intent;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
	
	public void shareAsImage(final Activity activity, SortedSet<SimplePageInfo> items, String title)
	{
		mShareTask = new ShareFilesTask(activity, items, title, false);
		mShareTask.execute();
	}
	
	public void shareAsPDF(final Activity activity, SortedSet<SimplePageInfo> items, String title)
	{
		mShareTask = new ShareFilesTask(activity, items, title, true);
		mShareTask.execute();
	}
	
	public void shareAsSupernoteFormat(final Activity activity, SortedSet<SimplePageInfo> items, String title)
	{
		mExportTask = new ExportTask(activity, items, title, false);
		mExportTask.execute();
	}
		
	private class ShareFilesTask extends ExportShareCommonTask {
		//[Carol]Share Task for sharing image and pdf file
		private boolean mIsPdf = false;
		private Intent mShareIntent;
		private ProgressDialog mProgressDialog;
		private String mShareDir = "";
		
		public ShareFilesTask(Activity activity, SortedSet<SimplePageInfo> items, boolean isPDF) {
			super(activity, items);
			mIsPdf = isPDF;
		}

		public ShareFilesTask(Activity activity,SortedSet<SimplePageInfo> items, String title,
				boolean isPDF) {
			super(activity, items, title);
			mIsPdf = isPDF;
		}

		public ShareFilesTask(Activity activity, ArrayList<Long> bookIds, boolean isPDF) {
			super(activity, bookIds);
			mIsPdf = isPDF;
		}
		
		public void setDialog(ProgressDialog dialog) {
			if (mProgressDialog != null) {
				mProgressDialog = null;
			}
			mProgressDialog = dialog;
			mProgressDialog.setProgress(super.mCount);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsShareTaskRunning = true;
			mShareDir = mPreference.getString(
					mResources.getString(R.string.pref_export_dir),
					MetaData.SHARE_DIR);
			Bundle b = new Bundle();
			b.putInt("max", super.mMax);
			showDialog(SHARE_FILE_PROGRESS_DIALOG, b);
		}
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			if (Looper.myLooper() == null) {
				Looper.prepare();
			}
			
			boolean value = true;
			if(mIsPdf){
				value = exportPages(false, mShareDir);
			}else{
				if ( super.mItems != null ) {
					NoteBook noteBook = getOneNotebook();
					if (noteBook != null) {
						generateThumbnail(noteBook, false);
					}
				}
			}
			return value;
		}
	
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if(values != null)
			{
				mProgressDialog.setProgress(super.mCount);
				Log.d("Count", "time="+System.currentTimeMillis());
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);		
			mIsShareTaskRunning = false;
			mCurrentInstance.removeDialog(SHARE_FILE_PROGRESS_DIALOG);
			
			if(mIsPdf){
				File file = new File(super.mFileName);
				if(!file.exists()) //file exists, process differently according to the content
					mCurrentInstance.showDialog(SHARE_FILE_FAIL_DIALOG);
				else{
					Uri uri = Uri.parse("file://" + file);
					mCurrentInstance.sendBroadcast(new Intent(
							Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
					ArrayList<String> pathList = new ArrayList<String>();
					pathList.add(super.mFileName);
					mShareIntent = shareMultipleFileIntent(pathList, "pdf");
				}
			}else{ //image
				if(super.mJpgPathList == null)
					mCurrentInstance.showDialog(SHARE_FILE_FAIL_DIALOG);
				else{
					ArrayList<String> pathList = super.mJpgPathList;
					mShareIntent = shareMultipleFileIntent(pathList, "image");
				}
			}
			if(mShareIntent != null){
				mCurrentInstance.startActivity(mShareIntent);
			}
			mCurrentInstance.updateFragment();
		}
		
		@Override
		protected void onCancelled(Boolean result) {
			mIsShareTaskRunning = false;
			for (String path : super.mFiles) {
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				}
			}
			
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mCurrentInstance.removeDialog(SHARE_FILE_PROGRESS_DIALOG);
				mCurrentInstance.updateFragment();
			}
		}
	}
	
	private class ExportShareCommonTask extends AsyncTask<String, Integer, Boolean> {
		//[Carol]detach from previous Export2PdfTask
		private SortedSet<SimplePageInfo> mItems;
		private String mFileName;
		private String mTitle = "";
		private String mExportDir = "";
		private ArrayList<String> mJpgPathList = null;
		private ArrayList<String> mFiles = new ArrayList<String>();
		private ArrayList<Long> mBookIds = null;
		private int mCount = 0;
		private int mMax = 0;
		private int mPdfFileCount = 0;
		private boolean mIsExportBook = false;
		private static final int MSG_UPDATE_PROGRESS = 0;
		private static final int MSG_EXPORT_TITLE = 1;
		
		public ExportShareCommonTask(Activity activity, SortedSet<SimplePageInfo> items) {
			
			if (items != null) {
				mItems = new TreeSet<SimplePageInfo>(items);
			} else {
				mItems = new TreeSet<SimplePageInfo>();
			}

			mCount = 0;
			mMax = mItems.size();
		}

		public ExportShareCommonTask(Activity activity,
				SortedSet<SimplePageInfo> items, String title) {
			
			if (items != null) {
				mItems = new TreeSet<SimplePageInfo>(items);
			} else {
				mItems = new TreeSet<SimplePageInfo>();
			}
			mFileName = title + MetaData.EXPORT_PDF_EXTENSION;
			mTitle = title;

			mCount = 0;
			mMax = mItems.size();			
			mPdfFileCount = 0;
		}

		public ExportShareCommonTask(Activity activity, ArrayList<Long> bookIds) {
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

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		public NoteBook getOneNotebook()
		{
			BookCase bookCase = BookCase.getInstance(mContext);
			Long bookId = 0L;
			if (mItems != null) {
				if (mItems.size() > 0) {
					bookId = mItems.first().bookId;
				} else {
					bookId = mBookcase.getCurrentBookId();
				}
				return bookCase.getNoteBook(bookId);
			}else
				return null;
		}
		
		public boolean generateThumbnail(NoteBook noteBook, boolean isExporting){
			if (noteBook != null) {
				if(isExporting) //Carol
					publishProgress(MSG_EXPORT_TITLE);
				mJpgPathList = new ArrayList<String>();
				PageDataLoader loader = new PageDataLoader(mContext);
				for (SimplePageInfo pageInfo : mItems) {
					long pageId = pageInfo.pageId;
					NotePage notePage = noteBook.getNotePage(pageId);
					if (notePage != null) {
						boolean isPhoneScreen = noteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE;
						int width = isPhoneScreen ? EditorActivity.PHONE_PAGE_SHARE_BITMAP_DAFAULT_WIDTH
								: EditorActivity.PAD_PAGE_SHARE_BITMAP_DAFAULT_WIDTH;
						int height = isPhoneScreen ? EditorActivity.PHONE_PAGE_SHARE_BITMAP_DAFAULT_HEIGHT
								: EditorActivity.PAD_PAGE_SHARE_BITMAP_DAFAULT_HEIGHT;
						Bitmap bitmap = Bitmap.createBitmap(width,
								height, Bitmap.Config.ARGB_8888);
						ArrayList<String> fileList = notePage.getThumbnail(loader, false, 
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
			}
			return true;
		}
		
		public boolean exportPages(boolean isExporting, String exportDir) {
			mExportDir = exportDir;
			File tempFile = null;
			try {
				if (mItems != null) {
					NoteBook noteBook = getOneNotebook();
					if (noteBook != null) {
						if(!generateThumbnail(noteBook, isExporting))
							return false;
						
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
							    tempFile = new File(mExportDir, baseFileName + MetaData.EXPORT_PDF_EXTENSION);//exportDir, baseFileName);
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

		@Override
		protected Boolean doInBackground(String... params) {
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled(Boolean result) {
		}
	}
	//End Carol
}
