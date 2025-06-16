package com.asus.supernote.widget;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.view.View;
import android.widget.RemoteViews;

import com.asus.supernote.MemoPreviewActivity;
import com.asus.supernote.R;
import com.asus.supernote.data.AsusFormat;
import com.asus.supernote.data.AsusFormatReader;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.Memo;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.datacopy.DataCopyActivity;
import com.asus.supernote.fota.V1DataConverter;
import com.asus.supernote.fota.V1DataFormat;
import com.asus.supernote.inksearch.CFG;
import com.asus.supernote.picker.PageComparator;
import com.asus.supernote.picker.PickerActivity;
import com.asus.supernote.picker.SimplePageInfo;

public class WidgetProvider extends AppWidgetProvider {
    public static final String TAG = "WidgetProvider";

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        SharedPreferences pref = context.getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
        boolean isFirstLoad = pref.getBoolean(context.getResources().getString(R.string.pref_first_load), true);
        if (isFirstLoad) {
            new WidgetInitTask(context).execute();
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(context.getResources().getString(R.string.pref_first_load), false);
            MetaData.IS_LOAD = true;
            editor.commit();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Cursor cursor = context.getContentResolver().query(MetaData.MemoTable.uri, null, "is_hidden = 0", null, null);
        for (int id : appWidgetIds) {
            RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            remoteView.setViewVisibility(R.id.memoLoadingSpinner, View.GONE);
            if (cursor.getCount() == 0) {
                remoteView.setViewVisibility(R.id.noMemoHintView, View.VISIBLE);
                remoteView.setViewVisibility(R.id.memoStackView, View.GONE);
                try {
					Intent intent = new Intent(context, PickerActivity.class);
					PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, 0);
					remoteView.setOnClickPendingIntent(R.id.noMemoHintView, pendingIntent);
					appWidgetManager.updateAppWidget(id, remoteView);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            else {
                remoteView.setViewVisibility(R.id.noMemoHintView, View.GONE);
                remoteView.setViewVisibility(R.id.memoStackView, View.VISIBLE);
                try {
					Intent intent = new Intent(context, WidgetRemoteService.class);
					intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
					intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
					remoteView.setRemoteAdapter(id, R.id.memoStackView, intent);
					intent = new Intent();
					// intent.setAction(MemoReceiver.ACTION);
					intent.setClass(context, MemoPreviewActivity.class);
					PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, 0);
					remoteView.setPendingIntentTemplate(R.id.memoStackView, pendingIntent);
					appWidgetManager.updateAppWidget(id, remoteView);
					appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.memoStackView);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
        cursor.close();
    }

    public static class WidgetInitTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private ProgressDialog mDialog;
        private boolean mIsActivity = false;
        
        private int mLoadCFGResourceFlag = 0;
        private int mTutorialMode = -1; //Carol
        
        private HashMap<Integer, Integer> mSneThumailMap;//noah:sne文件及所对应的缩略图文件的map
        
        public WidgetInitTask(Context context) {
            mContext = context;
            mIsActivity = (mContext instanceof Activity);
            mSneThumailMap = new HashMap<Integer, Integer>();
            mSneThumailMap.put(R.raw.meeting_record, R.raw.thumail_meeting_record);
            mSneThumailMap.put(R.raw.product_sketch, R.raw.thumail_product_sketch);
        }
        
        public void setLoadCFGResource(int flag)
        {
        	mLoadCFGResourceFlag = flag;
        }
        
        private void readTutorial(InputStream is, int rawId) throws Exception {
        	File tempFile = File.createTempFile("SuperNote", "TEMP");
            BufferedInputStream bis = new BufferedInputStream(is);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile));
            byte[] byteIn = new byte[8192];
            int len = 0;
            while ((len = bis.read(byteIn)) > 0) {
                bos.write(byteIn, 0, len);
            }
            bis.close();
            bos.close();
            ArrayList<Long> bookIds = new ArrayList<Long>();
            ZipFile zipFile = new ZipFile(tempFile);
            Enumeration<?> entries = zipFile.entries();
    		while (entries.hasMoreElements()) {
    			ZipEntry ze = (ZipEntry) entries.nextElement();
    			if (ze.getName().equals(MetaData.BOOK_LIST)) {
    				BufferedInputStream bisZip = new BufferedInputStream(
    						zipFile.getInputStream(ze));
    				AsusFormatReader afr = new AsusFormatReader(bisZip,
    						NotePage.MAX_ARRAY_SIZE);
    				boolean isListCorrect = false, isInfoCorrect = false;;
    				Long id = 0L;
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
    		            	break;
    		            case AsusFormat.SNF_BOOKINFO_END:
    		            	if (isInfoCorrect) {
    		            		isInfoCorrect = false;
    		            		bookIds.add(id);
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
    		        break;
    			}
    		}
    		if (!bookIds.isEmpty()) {
    			for (Long id : bookIds) {
    				importBook(zipFile, id, rawId);
    			}
    		} else {
    			importBook(zipFile, 0L, rawId);
    		}
    		zipFile.close();
    		tempFile.delete();
            ContentResolver cr = mContext.getContentResolver();
            cr.delete(MetaData.ClipboardTable.uri, null, null);
            
                File nbsFile = new File(V1DataFormat.DATA_DIR, V1DataFormat.BOOKLIST_FILE_NAME);
            	if (nbsFile.exists()) {
            		publishProgress();
            		V1DataConverter converter = new V1DataConverter(mContext);
            		converter.preprocessBooks();
            	}
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mIsActivity) {
                mDialog = new ProgressDialog(mContext);
                mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mDialog.setCancelable(false);
                mDialog.setMessage(mContext.getResources().getString(R.string.load_tutorial));
            }
        }
        
        private boolean importBook(ZipFile zipFile, Long id, int rawId) throws Exception {
			BookCase bookCase = BookCase.getInstance(mContext);
			Enumeration<?> entries = null;
			
			entries = zipFile.entries();
			SortedSet<SimplePageInfo> pages = new TreeSet<SimplePageInfo>(
					new PageComparator());
			NoteBook book = null;
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
							zipFile.getInputStream(ze));
					AsusFormatReader afr = new AsusFormatReader(bis,
							NotePage.MAX_ARRAY_SIZE);
					//BEGIN: RICHARD
					bookCase.itemLoadFromTutorial(afr);
					//END: RICHARD
					bis.close();
					book = bookCase.getImportedNoteBook();
					for (int i = 0; i < book.getTotalPageNumber(); ++i) {
						SimplePageInfo info = new SimplePageInfo(
								book.getCreatedTime(),
								book.getPageOrder(i), 0, i);
						pages.add(info);
					}
					break;
				}
			}
			if (book != null) {
				List<Long> bookmarks = book.getBookmarks();
				book.clearPageOrder();
				
				for (SimplePageInfo info : pages) {
					NotePage page = new NotePage(mContext,
							book.getCreatedTime());
					page.setBookmark((bookmarks != null && bookmarks
							.contains(info.pageId)) ? true : false);
					String filePath = page.getFilePath();
					String srcPath = book.getImportedTime() + "/"
							+ info.pageId + "/";
					entries = zipFile.entries();
					while (entries.hasMoreElements()) {
						ZipEntry ze = (ZipEntry) entries.nextElement();
						if (ze.getName().contains(srcPath)) {
							if (ze.getName().contains(MetaData.DOODLE_PREFIX)) {
								File file = new File(filePath,
										MetaData.DOODLE_ITEM_PREFIX);
								BufferedOutputStream bos = new BufferedOutputStream(
										new FileOutputStream(file));
								copyZipFile(zipFile.getInputStream(ze), bos);
							} else if (ze.getName().contains(MetaData.NOTE_ITEM_PREFIX)) {
								File file = new File(filePath, MetaData.NOTE_ITEM_PREFIX);
								BufferedOutputStream bos = new BufferedOutputStream(
										new FileOutputStream(file));
								copyZipFile(zipFile.getInputStream(ze), bos);
							} else if (ze.getName().contains(
									MetaData.THUMBNAIL_PREFIX)) {
								File file = new File(filePath,
										MetaData.THUMBNAIL_PREFIX);
								BufferedOutputStream bos = new BufferedOutputStream(
										new FileOutputStream(file));
								copyZipFile(zipFile.getInputStream(ze), bos);
							} else {
								String[] tokens = ze.getName().split("/");
								String fileName = tokens[tokens.length - 1];
								File file = new File(filePath, fileName);
								BufferedOutputStream bos = new BufferedOutputStream(
										new FileOutputStream(file));
								copyZipFile(zipFile.getInputStream(ze), bos);
							}
						}
					}
					book.addPage(page);
				}
				copyThumailFromRaw(book, rawId);
			}
			return true;
		}
        /**
         * 从raw文件夹下拷贝缩略图到book的文件夹下
         * @author noah_zhang
         */
        private void copyThumailFromRaw(NoteBook book, int rawId){
        	Integer thumailId = mSneThumailMap.get(rawId);
        	if(thumailId != null){
        		InputStream inputStream = mContext.getResources().openRawResource(thumailId);
        		book.saveBookCoverThumb(inputStream);
        	}
        }
        
        private void createNoMediaFile(String dir) {
        	File nomediaDir = new File(dir);
    		if (!nomediaDir.exists()) {
    			nomediaDir.mkdirs();
    		}
    		File nomedia = new File(nomediaDir, ".nomedia"); 
    		if (!nomedia.exists()) {
    			try {
    				nomedia.createNewFile();
    			} catch (IOException ioe) {
    				ioe.printStackTrace();
    			}
    		}
        }

        @Override
        protected Void doInBackground(Void... params) {
			// begin noah;7.12
			if (mLoadCFGResourceFlag == MetaData.ONLY_LOAD_CFG_RESOURCE) {
				CFG.copyResources(mContext);
				return null;
			} else if (mLoadCFGResourceFlag == MetaData.LOAD_CFG_RESOURCE_AND_SNE) {
				CFG.copyResources(mContext);
			}
			// BEGIN: Better
			createNoMediaFile(MetaData.DATA_DIR);
			createNoMediaFile(MetaData.CLIPBOARD_TEMP_DIR);
			createNoMediaFile(MetaData.SHARE_DIR);

			Resources res = mContext.getResources();
			// Begin: show_wang@asus.com
			// Modified reason: tutorial mode
			mTutorialMode = mContext.getResources().getInteger(
					R.integer.tutorial_mode);
			if (isPadFone()) {
				mTutorialMode = 2;
			}
			InputStream is = null;
			ArrayList<Integer> rawIds = new ArrayList<Integer>();

			if (MetaData.SWITCH_IMPORT_BOOKS) {
				if (mTutorialMode >= 1){
					rawIds.add(R.raw.meeting_record);
					rawIds.add(R.raw.product_sketch);
				}
			}
			for (Integer id : rawIds) {
				try {
					is = res.openRawResource(id);
					readTutorial(is, id);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
			// end noah;
            
        	SharedPreferences pref = mContext.getSharedPreferences(MetaData.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
           
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(mContext.getResources().getString(R.string.pref_first_load), false);
            MetaData.IS_LOAD = true;
            editor.commit();
            return null;
        }

        //+++ Dave. Fix the the bug: a black screen will show when supernote is opened at first time
    	private static final int DIALOG_LOADING_MESSAGE = 1;
    	//---
    	
		//emmanual to fix bug 392671
		public void dismissProgressDialog() {
			if (mDialog != null && mDialog.isShowing()) {
				try {
					mDialog.dismiss();
				} catch (Exception e) {
					e.toString();
				}
			}
		}
        
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Memo.updateWidget(mContext);
            if (mIsActivity) {              
    		    //+++ Dave. Fix the the bug: a black screen will show when supernote is launched at first time
                IBinder binder = DataCopyActivity.getMessenger();
                if(binder != null)
                {
                	Messenger messenger = new Messenger(binder);
        			Message msg = new Message();
        			msg.what = DIALOG_LOADING_MESSAGE;
                	try {
						messenger.send(msg);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
                //---
            }
            if(!InitSyncFilesInfoHelper.isSyncFilesInfoInited() 
            		&& !InitSyncFilesInfoHelper.InitSyncFilesInfoAsyncTask.isTaskRunning()){
            	InitSyncFilesInfoHelper.InitSyncFilesInfoAsyncTask task = new InitSyncFilesInfoHelper.InitSyncFilesInfoAsyncTask();
            	task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//并行运行
            }
        }

        private void copyZipFile(InputStream is, OutputStream os) {
            byte[] byteBuffer = new byte[8192];
            int byteIn = 0;
            try {
                while ((byteIn = is.read(byteBuffer)) >= 0) {
                    os.write(byteBuffer, 0, byteIn);
                }
                is.close();
                os.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

		String[] padFoneModels = { 
				"PadFone 2", 
				"PadFone Infinity", 
				"ASUS A80",
				"PadFone Infinity A86" 
				};
		private boolean isPadFone() {
			String model = SystemProperties.get("ro.product.model");
			if (model != null) {
				for (String padFoneModel : padFoneModels) {
					if (model.equals(padFoneModel)) {
						return true;
					}
				}
			}
			return false;
		}
    }
}
