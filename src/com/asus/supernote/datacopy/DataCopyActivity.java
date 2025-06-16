package com.asus.supernote.datacopy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

import com.asus.supernote.R;
import com.asus.supernote.SuperNoteApplication;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;

/**
 * @author: noah_zhang
 * @date: 2013-06-08
 * @description: Data copy功能的Activity，该Activity没有UI
 * @remark
 * */
public class DataCopyActivity extends Activity {
	public static final String TAG = "DataCopyActivity";
	public static String FROM_DIR = "";
	public static String TO_DIR = "";
	public static final String DATA_COPIED = "datacopied";
	public static int RUN_COUNT = 0;

	static {
		if (MetaData.IS_HIDE_DATA) {
			FROM_DIR = Environment.getExternalStorageDirectory()
					+ "/AsusSuperNote/Data/";
			TO_DIR = Environment.getExternalStorageDirectory()
					+ "/.AsusSuperNote/Data/";
		} else {
			FROM_DIR = Environment.getExternalStorageDirectory()
					+ "/.AsusSuperNote/Data/";
			TO_DIR = Environment.getExternalStorageDirectory()
					+ "/AsusSuperNote/Data/";
		}
	}

	private static final int DIALOG_ID_COPY_PROGRESS = 100;
	private static final int DIALOG_ID_SDCARD_NOT_ENOUGH_SPACE = 102;
	private ProgressDialog mProgressDialog;


	private Handler mHandler = new Handler();
	private String mBookName = "";
	private boolean mOverwriteFloder = false;// 是否覆盖已经存在的book
	private Object mLockObject = new Object();

    //+++ Dave. Fix the the bug: a black screen will show when supernote is launched at first time
	private static final int DIALOG_ID_LOADING = 103;
	private static final int DIALOG_LOADING_MESSAGE = 1;
	private ProgressDialog mDialog;
	private Handler mToolHandler=new Handler(){
		public void handleMessage(Message msg) {
			final int what = msg.what;
			switch (what) {
			case DIALOG_LOADING_MESSAGE:
				try {
					mDialog.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
				}
				finishThis(false);

			default:
				break;
			}

			super.handleMessage(msg);
		};
	};
	
	public static Messenger mMessenger = null;
	
	public static IBinder getMessenger()
	{
		if(mMessenger != null)
			return mMessenger.getBinder();
		else {
			return null;
		}
	}
	//---
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		if (!needDataCopy()) {
		    //+++ Dave. Fix the the bug: a black screen will show when supernote is launched at first time
			mMessenger = new Messenger(mToolHandler);
			RUN_COUNT++;
//			finishThis(false);
			//---
			return;
		}
		new CopyTask().execute(FROM_DIR, TO_DIR);
		RUN_COUNT++;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		RUN_COUNT--;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ID_LOADING:
		{
            mDialog = new ProgressDialog(this);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setCancelable(false);
            mDialog.setMessage(this.getResources().getString(R.string.load_tutorial));
            mDialog.show();
            return mDialog;
		}
		
		case DIALOG_ID_COPY_PROGRESS: {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setTitle(getResources().getString(
					R.string.data_fomat_upgrade));
			mProgressDialog.setCancelable(false);
			return mProgressDialog;
		}

		case DIALOG_ID_SDCARD_NOT_ENOUGH_SPACE:
			AlertDialog.Builder builder2 = new Builder(this);
			builder2.setTitle(getResources().getString(
					R.string.not_enough_space));
			builder2.setMessage(getResources().getString(R.string.try_again));
			builder2.setNegativeButton(getResources().getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			AlertDialog dialog2 = builder2.create();
			dialog2.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface arg0) {
					// TODO Auto-generated method stub
					// mOverwriteFloder = false;
					notifyCopyContinue();
				}
			});
			return dialog2;
		default:
			break;
		}
		return null;
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
		BookCase bookCase = BookCase.getInstance(this);
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

	private void notifyCopyContinue() {
		synchronized (mLockObject) {
			mLockObject.notifyAll();
		}
	}

	private void finishThis(boolean changed) {
		Intent intent = getIntent();
		intent.putExtra("changed", changed);
		setResult(RESULT_OK, intent);
		finish();
	}

	public static boolean isRunning() {
		return RUN_COUNT > 0;
	}

	public static boolean needDataCopy() {
		if (!MetaData.Switch_DATA_COPY)
			return false;
		if(!MetaData.IS_HIDE_DATA)//只当升级到版本:隐藏数据，的时候，才复制以前的旧数据
			return false;
		if (checkDataCopied() || !checkExcessFolderExist())
			return false;
		return true;
	}

	public static boolean checkDataCopied() {
		SuperNoteApplication application = SuperNoteApplication.getInstance();//这里有可能null
		if(application == null)
			return true;
		SharedPreferences preferences = SuperNoteApplication.getContext()
				.getSharedPreferences("datacopy", MODE_PRIVATE);
		boolean flag = preferences.getBoolean(DATA_COPIED, false);
		return flag;
	}

	// 判断多余的文件夹是否存在
	public static boolean checkExcessFolderExist() {
		File file = new File(FROM_DIR);
		if (file.exists()) {
			File in = new File(FROM_DIR);
			long size = 0;
			try {
				size = getFolderSize(in);
				if (size == 0)
					return false;
			} catch (Exception e) {
				// TODO: handle exception
			}
			return true;
		} else {
			return false;
		}
	}

	private void saveDataCopied(boolean flag) {
		SharedPreferences preferences = this.getSharedPreferences("datacopy",
				MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(DATA_COPIED, flag);
		editor.commit();
	}

	private void handleFolderExits(String folderName) {
		mBookName = getBookName(folderName);
		mOverwriteFloder = false;
		Dialog dialog = createFolderExitsDialog();
		dialog.show();
	}

	private AlertDialog createFolderExitsDialog() {
		AlertDialog.Builder builder = new Builder(this);
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

	private void handleNotEnoughSpace() {
		showDialog(DIALOG_ID_SDCARD_NOT_ENOUGH_SPACE);
	}

	private long getSdcardAvailaleSize() {
		File path = Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
		// (availableBlocks * blockSize)/1024 KIB 单位
		// (availableBlocks * blockSize)/1024 /1024 MIB单位
	}

	/**********************************************
	 * CopyTask开始
	 ********************************************/

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
			long sdcardAvailaleSize = getSdcardAvailaleSize();
			if (mTotalSize * 2 > sdcardAvailaleSize) {// 保证有多余的剩余空间
				synchronized (mLockObject) {
					try {
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								handleNotEnoughSpace();
							}
						});
						mLockObject.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return false;
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
			if (mProgressDialog != null) {
				try {
					mProgressDialog.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			if (result) {
				saveDataCopied(result);
				Toast.makeText(DataCopyActivity.this,
						getResources().getString(R.string.successful_upgrade),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(DataCopyActivity.this,
						getResources().getString(R.string.upgrade_fails),
						Toast.LENGTH_SHORT).show();
			}
			finishThis(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showDialog(DIALOG_ID_COPY_PROGRESS);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			if (mProgressDialog != null)
				mProgressDialog.setProgress(values[0]);
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
						// makeRootDirectory();
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
	public static long getFolderSize(java.io.File file) throws Exception {
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
