// BEGIN: Better

package com.asus.supernote.sync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import android.util.Log;

import com.asus.supernote.data.AsusFormat;
import com.asus.supernote.data.AsusFormatReader;
import com.asus.supernote.data.AsusFormatWriter;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.sync.SyncBookItem;
import com.asus.supernote.WebStorageException;
import com.asus.supernote.WebStorageHelper;

public class SyncTableInfo {
	private static final String TAG = "SyncTableInfo";
	private List<SyncBookItem> mSyncBookList;
	private String mVersion = "";
	private String mSha;

	private static WebStorageHelper sWebStorageHelper = new WebStorageHelper();

	public SyncTableInfo() {
		mSyncBookList = new ArrayList<SyncBookItem>();
		mSha = "";
	}

	public List<SyncBookItem> getBookList() {
		return mSyncBookList;
	}

	public void loadTableInfo() throws IOException {
		String tableInfoPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_DOWNLOAD_DIR
				+ MetaData.SYNC_TABLE_INFO_FILE_NAME;
		boolean bookInfoCorrect = false;
		boolean noteBookCorrect = false;
		SyncBookItem bookItem = null;
		FileInputStream fis = new FileInputStream(tableInfoPath);
		BufferedInputStream bis = new BufferedInputStream(fis);
		AsusFormatReader afr = new AsusFormatReader(bis,
				NotePage.MAX_ARRAY_SIZE);
		AsusFormatReader.Item item = afr.readItem();
		while (item != null) {
			switch (item.getId()) {
			case AsusFormat.SNF_TABLE_BOOK_INFO_BEGIN:
				bookInfoCorrect = true;
				break;
			case AsusFormat.SNF_TABLE_BOOK_INFO_END:
				bookInfoCorrect = false;
				noteBookCorrect = false;
				break;
			case AsusFormat.SNF_TABLE_NOTEBOOK_BEGIN:
				if (bookInfoCorrect) {
					noteBookCorrect = true;
					bookItem = new SyncBookItem();
				}
				break;
			case AsusFormat.SNF_TABLE_NOTEBOOK_END:
				if (noteBookCorrect) {
					mSyncBookList.add(bookItem);
					noteBookCorrect = false;
					bookItem = null;
				}
				break;
			case AsusFormat.SNF_TABLE_NOTEBOOK_BOOKID:
				if (noteBookCorrect) {
					bookItem.setBookId(item.getLongValue());
				}
				break;
			case AsusFormat.SNF_TABLE_NOTEBOOK_TITLE:
				if (noteBookCorrect) {
					bookItem.setTitle(item.getStringValue());
				}
				break;
			case AsusFormat.SNF_TABLE_NOTEBOOK_IS_LOCKED:
				if (noteBookCorrect) {
					bookItem.setLocked(item.getIntValue() == 0 ? false : true);
				}
				break;
			case AsusFormat.SNF_TABLE_NOTEBOOK_PAGE_COUNT:
				if (noteBookCorrect) {
					bookItem.setPageSize(item.getIntValue());
				}
				break;
			case AsusFormat.SNF_TABLE_NOTEBOOK_BAKCOLOR:
				if (noteBookCorrect) {
					bookItem.setColor(item.getIntValue());
				}
				break;
			case AsusFormat.SNF_TABLE_NOTEBOOK_GRIDLINE:
				if (noteBookCorrect) {
					bookItem.setGridLine(item.getIntValue());
				}
				break;
			case AsusFormat.SNF_TABLE_NOTEBOOK_TYPE:
				if (noteBookCorrect) {
					bookItem.setPhoneMemo(item.getIntValue() == 0 ? false
							: true);
				}
				break;
			case AsusFormat.SNF_TABLE_NOTEBOOK_PAGEID_COLLECTION:
				if (noteBookCorrect) {
					for (int i = 0; i < item.getLongArray().length; i++) {
						bookItem.getPageOrderList().add(item.getLongArray()[i]);
					}
				}
				break;
			case AsusFormat.SNF_TABLE_NOTEBOOK_BOOKMARK_COLLECTION:
				if (noteBookCorrect) {
					for (int i = 0; i < item.getLongArray().length; i++) {
						bookItem.getBookmarksList().add(item.getLongArray()[i]);
					}
				}
				break;
			case AsusFormat.SNF_TABLE_NOTEBOOK_LAST_SYNC_MODTIME:
				if (noteBookCorrect) {
					bookItem.setLastSyncModifiedTime(item.getLongValue());
				}
				break;
			case AsusFormat.SNF_TABLE_NOTEBOOK_IS_DELETED:
				if (noteBookCorrect) {
					bookItem.setDeleted(item.getByteValue() == 0 ? false : true);
				}
				break;
				//begin wendy allen++ for template 0706 
			case AsusFormat.SNF_TABLE_NOTEBOOK_TEMPLATE:
				if(noteBookCorrect)
				{
					bookItem.setTemplate(item.getIntValue());
				}
				break;
				//end wendy allen++ for template 0706
			// BEGIN: Better
			case AsusFormat.SNF_TABLE_NOTEBOOK_INDEXLANGUAGE:
				if (noteBookCorrect) {
					bookItem.setIndexLanguage(item.getIntValue());
				}
				break;
			// END: Better
			// BEGIN: Darwin
			case AsusFormat.SNF_TABLE_NOTEBOOK_INDEXCOVER:
				if (noteBookCorrect) {
					bookItem.setIndexCover(item.getIntValue());
				}
				break;
			case AsusFormat.SNF_TABLE_NOTEBOOK_COVERMODIFYTIME:
				if (noteBookCorrect) {
					bookItem.setCoverModifiedTime(item.getLongValue());
				}
				break;
			// END: Darwin
			default:
				break;
			}
			item = afr.readItem();
		}
		bis.close();
		fis.close();
		File tempFile = new File(tableInfoPath);
		if (tempFile.exists()) {
			tempFile.delete();
		}
	}

	public void saveTableInfo() throws IOException {
		String tempUploadDirPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_UPLOAD_DIR;
		File tempUploadDir = new File(tempUploadDirPath);
		if (!tempUploadDir.exists()) {
			tempUploadDir.mkdirs();
		}
		String tableInfoPath = tempUploadDirPath
				+ MetaData.SYNC_TABLE_INFO_FILE_NAME;
		File file = new File(tableInfoPath);
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		AsusFormatWriter afw = new AsusFormatWriter(bos);
		afw.writeByteArray(AsusFormat.SNF_TABLE_BOOK_INFO_BEGIN, null, 0, 0);
		for (SyncBookItem item : mSyncBookList) {
			afw.writeByteArray(AsusFormat.SNF_TABLE_NOTEBOOK_BEGIN, null, 0, 0);
			afw.writeLong(AsusFormat.SNF_TABLE_NOTEBOOK_BOOKID,
					item.getBookId());
			afw.writeString(AsusFormat.SNF_TABLE_NOTEBOOK_TITLE,
					item.getTitle());
			afw.writeInt(AsusFormat.SNF_TABLE_NOTEBOOK_IS_LOCKED,
					item.isLocked() ? 1 : 0);
			afw.writeInt(AsusFormat.SNF_TABLE_NOTEBOOK_PAGE_COUNT,
					item.getPageSize());
			afw.writeInt(AsusFormat.SNF_TABLE_NOTEBOOK_BAKCOLOR,
					item.getColor());
			afw.writeInt(AsusFormat.SNF_TABLE_NOTEBOOK_GRIDLINE,
					item.getGridLine());
			afw.writeInt(AsusFormat.SNF_TABLE_NOTEBOOK_TYPE,
					item.isPhoneMemo() ? 1 : 0);
			long[] pageIds = new long[item.getPageOrderList().size()];
			for (int i = 0; i < item.getPageOrderList().size(); i++) {
				pageIds[i] = item.getPageOrderList().get(i);
			}
			afw.writeLongArray(AsusFormat.SNF_TABLE_NOTEBOOK_PAGEID_COLLECTION,
					pageIds, 0, pageIds.length);
			long[] bookmarks = new long[item.getBookmarksList().size()];
			for (int i = 0; i < item.getBookmarksList().size(); i++) {
				bookmarks[i] = item.getBookmarksList().get(i);
			}
			afw.writeLongArray(
					AsusFormat.SNF_TABLE_NOTEBOOK_BOOKMARK_COLLECTION,
					bookmarks, 0, bookmarks.length);
			afw.writeLong(AsusFormat.SNF_TABLE_NOTEBOOK_LAST_SYNC_MODTIME,
					item.getLastSyncModifiedTime());
			afw.writeByte(AsusFormat.SNF_TABLE_NOTEBOOK_IS_DELETED,
					(byte) (item.isDeleted() ? 1 : 0));
			//begin wendy allen++ for template 0706
			afw.writeInt(AsusFormat.SNF_TABLE_NOTEBOOK_TEMPLATE, item.getTemplate());
			//end wendy allen++
			afw.writeInt(AsusFormat.SNF_TABLE_NOTEBOOK_INDEXLANGUAGE, item.getIndexLanguage());//Allen
			afw.writeInt(AsusFormat.SNF_TABLE_NOTEBOOK_INDEXCOVER, item.getIndexCover());//darwin
			afw.writeLong(AsusFormat.SNF_TABLE_NOTEBOOK_COVERMODIFYTIME, item.getCoverModifiedTime());//darwin
			afw.writeByteArray(AsusFormat.SNF_TABLE_NOTEBOOK_END, null, 0, 0);
		}
		afw.writeByteArray(AsusFormat.SNF_TABLE_BOOK_INFO_END, null, 0, 0);
		bos.close();
		fos.close();
	}

	public void downloadTableInfoAttribute() throws Exception {
		mVersion = null;
		mSha = "";
		try {
			Node fileNode = MetaData.webStorage
					.getSpecificFileAttribute(MetaData.SYNC_TABLE_INFO_FILE_NAME);
			if (fileNode != null) {
				String tmpVersion = sWebStorageHelper.getNodeValue(fileNode, 
						MetaData.SYNC_FILE_VERSION);
				if ((tmpVersion == null) || tmpVersion.isEmpty()) {
					tmpVersion = sWebStorageHelper.getNodeValue(fileNode, "VN");
					if ((tmpVersion != null) && !tmpVersion.isEmpty()) {
						mVersion = tmpVersion;
					}
				} else {
					mVersion = tmpVersion;
				}
				
				String tmpSha = sWebStorageHelper.getNodeValue(fileNode,
						MetaData.SYNC_PAGE_FILE_ATTR_SHA);
				if (tmpSha != null) {
					mSha = tmpSha;
				}
			}
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: {
				Log.v(TAG, "Failed to get book.info SHA, Retry once...");
				try {
					Node fileNode = MetaData.webStorage
							.getSpecificFileAttribute(MetaData.SYNC_TABLE_INFO_FILE_NAME);
					if (fileNode != null) {
						String tmpSha = sWebStorageHelper.getNodeValue(fileNode,
								MetaData.SYNC_PAGE_FILE_ATTR_SHA);
						if (tmpSha != null) {
							mSha = tmpSha;
						}
					}
					Log.v(TAG, "Retry success");
				} catch (Exception ex) {
					Log.v(TAG, "Retry failed");
					throw ex;
				}
			}
			break;
			default: {
				throw webex;
			}
			}
		}
	}
	
	public String getVersion() {
		return mVersion;
	}
	
	public String getTableInfoSha() {
		return mSha;
	}
	
	// BEGIN: Better
	public void setTableInfoSha(String sha) {
		mSha = sha;
	}
	
	public String getDownloadedTableInfoFileSha() throws OutOfMemoryError, IOException {
		String tableInfoFilePath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_DOWNLOAD_DIR
				+ MetaData.SYNC_TABLE_INFO_FILE_NAME;
		
		return sWebStorageHelper.getFileSha1(tableInfoFilePath);
	}
	// END: Better
	
	public void downloadTableInfo() throws Exception {
		String tempDownloadDirPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_DOWNLOAD_DIR;
		File tempDownloadDir = new File(tempDownloadDirPath);
		if (!tempDownloadDir.exists()) {
			tempDownloadDir.mkdirs();
		}
		String tableInfoFilePath = tempDownloadDirPath
				+ MetaData.SYNC_TABLE_INFO_FILE_NAME;
		File tableInfoFile = new File(tableInfoFilePath);
		if (tableInfoFile.exists()) {
			tableInfoFile.delete();
		}

		try {
			if (MetaData.webStorage.downloadOneFile(
					MetaData.SYNC_TABLE_INFO_FILE_NAME, tableInfoFilePath) != 1) {
				throw new WebStorageException(WebStorageException.OTHER_ERROR);
			} else {
				Log.v(TAG, "Download '" + MetaData.SYNC_TABLE_INFO_FILE_NAME
						+ "' to '" + tableInfoFilePath + "' success");
			}
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: {
				Log.v(TAG, "Failed to download '"
						+ MetaData.SYNC_TABLE_INFO_FILE_NAME + "'" + "to '"
						+ tableInfoFilePath + "', Retry once...");
				try {
					if (MetaData.webStorage.downloadOneFile(
							MetaData.SYNC_TABLE_INFO_FILE_NAME,
							tableInfoFilePath) != 1) {
						throw new WebStorageException(
								WebStorageException.OTHER_ERROR);
					} else {
						Log.v(TAG, "Retry success");
					}
				} catch (WebStorageException wex) {
					Log.v(TAG, "Retry failed");
					throw wex;
				}
			}
				break;
			default: {
				throw webex;
			}
			}
		}
	}

	public String uploadTableInfo() throws Exception {
		String tableInfoFilePath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_UPLOAD_DIR
				+ MetaData.SYNC_TABLE_INFO_FILE_NAME;
		return uploadTableInfo(tableInfoFilePath);
	}
	
	public String getFileSha(String filePath) throws OutOfMemoryError, IOException {
		return sWebStorageHelper.getFileSha1(filePath);
	}
	
	public String uploadTableInfo(String tableInfoFilePath) throws Exception {
		String sha = sWebStorageHelper.getFileSha1(tableInfoFilePath);
		String attribute = "";
		attribute += "<" + MetaData.SYNC_FILE_VERSION + ">" + MetaData.SYNC_CURRENT_VERSION
				+ "</" + MetaData.SYNC_FILE_VERSION + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_SHA + ">" + sha
				+ "</" + MetaData.SYNC_PAGE_FILE_ATTR_SHA + ">";
		try {
			if (MetaData.webStorage.uploadOneFile(tableInfoFilePath, attribute,
					((mSha == null) || mSha.equals("")) ? null : mSha, sha) != 1) {
				throw new WebStorageException(WebStorageException.OTHER_ERROR);
			} else {
				Log.v(TAG, "Upload '" + tableInfoFilePath + "'" + " success");
				Log.v(TAG, "Attributes: " + attribute);
			}
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: {
				Log.v(TAG, "Failed to upload '" + tableInfoFilePath
						+ "', Retry once...");
				Log.v(TAG, "Attributes: " + attribute);
				try {
					if (MetaData.webStorage.uploadOneFile(tableInfoFilePath,
							attribute,
							((mSha == null) || mSha.equals("")) ? null : mSha, sha) != 1) {
						throw new WebStorageException(
								WebStorageException.OTHER_ERROR);
					} else {
						Log.v(TAG, "Retry success");
					}
				} catch (WebStorageException wex) {
					Log.v(TAG, "Retry failed");
					throw wex;
				}
			}
				break;
			default: {
				throw webex;
			}
			}
		}
		File tempFile = new File(tableInfoFilePath);
		if (tempFile.exists()) {
			tempFile.delete();
		}
		return sha;
	}
}

// END: Better
