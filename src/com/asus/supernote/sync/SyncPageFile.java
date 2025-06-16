// BEGIN: Better

package com.asus.supernote.sync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.asus.supernote.WebStorageException;
import com.asus.supernote.WebStorageHelper;
import com.asus.supernote.data.AsusFormat;
import com.asus.supernote.data.AsusFormatReader;
import com.asus.supernote.data.AsusFormatWriter;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.sync.SyncPageCompareFun.TimeStampItem;

public class SyncPageFile {
	private static String TAG = "SyncPageFile";
	private static WebStorageHelper sWebStorageHelper = new WebStorageHelper();

	public SyncPageFile() {
	}
	//Begin Darwin_Yu@asus.com
	
	public static ArrayList<LocalPageItem> mListAttachmentModify		 = new ArrayList<LocalPageItem>();
	
	public static List<String> getServerPageAttachmentNameList( String pageId) throws Exception {
		List<String> nameList = new ArrayList<String>();
		Document doc;
		try 
		{
			doc = MetaData.webStorage.getSuperNoteFileList(-1,MetaData.ATTACHMENT_TAG);
		} 
		catch (WebStorageException webex) 
		{
			switch (webex.getErrorKind()) 
			{
			case WebStorageException.OTHER_ERROR: 
				Log.v(TAG, "Get SuperNote file list success, Retry once...");
				try {
					doc = MetaData.webStorage.getSuperNoteFileList(-1,MetaData.ATTACHMENT_TAG);
					Log.v(TAG, "Retry success");
				} catch (Exception ex) {
					Log.v(TAG, "Retry failed");
					throw ex;
				}
				break;
			default: 
				throw webex;
			}
		}
		if (doc != null) {
			NodeList nodeList = doc.getElementsByTagName("file");
			if (nodeList != null) {
				for (int i = 0; i < nodeList.getLength(); i++) {
					String strFileName = sWebStorageHelper.decodeBase64(sWebStorageHelper.getNodeValue(
							nodeList.item(i), "display"));
					if (strFileName != null) {
						if (strFileName
								.endsWith(MetaData.SYNC_ATTACHMENT_FILE_EXTENSION)) {
							String str[] = new String[2];
							str = getPageIdAndTrueNameFromAttachmentName(strFileName);
							
							String strPageId = str[0];
							
							if(pageId.equalsIgnoreCase(strPageId))
							{
								nameList.add(strFileName);
							}
						}
					} else {
						Log.v(TAG, "Can not get " + strFileName
								+ "'s 'display' attribute");
					}
				}
			} else {
				Log.v(TAG, "Can not get 'file' attribute");
			}
		} else {
			Log.v(TAG, "None file list got");
		}
		return nameList;
	}
	
	public static List<LocalPageItem> getServerFileListAll(String fileExt) throws Exception {
		List<LocalPageItem> pageItemList = new ArrayList<LocalPageItem>();
		Document doc;
		try 
		{
			doc = MetaData.webStorage.getSuperNoteFileList(-1,fileExt);
		} 
		catch (WebStorageException webex) 
		{
			switch (webex.getErrorKind()) 
			{
			case WebStorageException.OTHER_ERROR: 
				Log.v(TAG, "Get SuperNote file list success, Retry once...");
				try {
					doc = MetaData.webStorage.getSuperNoteFileList(-1,fileExt);
					Log.v(TAG, "Retry success");
				} catch (Exception ex) {
					Log.v(TAG, "Retry failed");
					throw ex;
				}
				break;
			default: 
				throw webex;
			}
		}
		if (doc != null) {
			NodeList nodeList = doc.getElementsByTagName("file");
			if (nodeList != null) {
				for (int i = 0; i < nodeList.getLength(); i++) {
					LocalPageItem pageItem = new LocalPageItem();
					String strFileName = sWebStorageHelper.decodeBase64(sWebStorageHelper.getNodeValue(
							nodeList.item(i), "display"));
					if (strFileName != null) {
						if (strFileName
								.endsWith(MetaData.SYNC_ATTACHMENT_FILE_EXTENSION)) {
							// id
							String str[] = new String[2];
							str = getPageIdAndTrueNameFromAttachmentName(strFileName);
							String strPageId = str[0];
							if ((strPageId == null) || strPageId.isEmpty()) {
								Log.e(TAG, "attachment id is null, file name '" + strFileName + "'");
								continue;
							}
							try {
								pageItem.setPageId(Long.parseLong(strPageId));
							} catch (Exception e) {
								e.printStackTrace();
								Log.e(TAG, "attachment id is invalid, file name '" + strFileName + "'");
								continue;
							}
							
							// file name
							String strName = str[1];
							if ((strName == null) || strName.isEmpty()) {
								Log.e(TAG, "attachment file name is null, file name '" + strFileName + "'");
								continue;
							}
							pageItem.setAttachmentName(strName);
							
							// sha
							String sha = sWebStorageHelper.getNodeValue(
									nodeList.item(i),
									MetaData.SYNC_ATTR_SHA);
							if ((sha == null) || sha.isEmpty()) {
								Log.e(TAG, "attachment attribute '" + MetaData.SYNC_ATTR_SHA + "' is null");
							}
							pageItem.setSha(sha);
							
							// last sync modified time
							String strLastSyncTime = sWebStorageHelper.getNodeValue(
									nodeList.item(i),
									MetaData.SYNC_ATTR_LASTCHANGETIME);
							if ((strLastSyncTime == null) || strLastSyncTime.isEmpty()) {
								Log.e(TAG, "attachment attribute '" + MetaData.SYNC_ATTR_LASTCHANGETIME + "' is null");
								pageItem.setLastSyncModifiedTime(System.currentTimeMillis());
							} else {
								try {
									pageItem.setLastSyncModifiedTime(Long.parseLong(strLastSyncTime));
								} catch (Exception e) {
									e.printStackTrace();
									Log.e(TAG, "attachment attribute '" + MetaData.SYNC_ATTR_LASTCHANGETIME 
											+ "' with value '" + strLastSyncTime + "'is invalid ");
									pageItem.setLastSyncModifiedTime(System.currentTimeMillis());
								}
							}
							
							// deleted
							String strDeleted = sWebStorageHelper.getNodeValue(
									nodeList.item(i),
									MetaData.SYNC_ATTACHMENT_ATTR_ISDELETED);
							if ((strDeleted == null) || strDeleted.isEmpty()) {
								Log.e(TAG, "attachment attribute '" + MetaData.SYNC_ATTACHMENT_ATTR_ISDELETED + "' is null");
							} else {
								try {
									pageItem.setDeleted(Boolean.parseBoolean(strDeleted));
								} catch (Exception e) {
									e.printStackTrace();
									Log.e(TAG, "attachment attribute '" + MetaData.SYNC_ATTACHMENT_ATTR_ISDELETED 
											+ "' with value '" + strDeleted + "'is invalid ");
								}
							}
							
							pageItemList.add(pageItem);
						}
					} else {
						Log.v(TAG, "Can not get " + strFileName
								+ "'s 'display' attribute");
					}
				}
			} else {
				Log.v(TAG, "Can not get 'file' attribute");
			}
		} else {
			Log.v(TAG, "None file list got");
		}
		return pageItemList;
	}
	public static List<LocalPageItem> getServerFileList(String fileExt,long pageID) throws Exception {
		List<LocalPageItem> pageItemList = new ArrayList<LocalPageItem>();
		Document doc;
		try 
		{
			doc = MetaData.webStorage.getSuperNoteFileList(-1,fileExt);
		} 
		catch (WebStorageException webex) 
		{
			switch (webex.getErrorKind()) 
			{
			case WebStorageException.OTHER_ERROR: 
				Log.v(TAG, "Get SuperNote file list success, Retry once...");
				try {
					doc = MetaData.webStorage.getSuperNoteFileList(-1,fileExt);
					Log.v(TAG, "Retry success");
				} catch (Exception ex) {
					Log.v(TAG, "Retry failed");
					throw ex;
				}
				break;
			default: 
				throw webex;
			}
		}
		if (doc != null) {
			NodeList nodeList = doc.getElementsByTagName("file");
			if (nodeList != null) {
				for (int i = 0; i < nodeList.getLength(); i++) {
					LocalPageItem pageItem = new LocalPageItem();
					String strFileName = sWebStorageHelper.decodeBase64(sWebStorageHelper.getNodeValue(
							nodeList.item(i), "display"));
					if (strFileName != null) {
						if (strFileName
								.endsWith(MetaData.SYNC_ATTACHMENT_FILE_EXTENSION)) {
							String str[] = new String[2];
							str = getPageIdAndTrueNameFromAttachmentName(strFileName);
							
							String strPageId = str[0];
							if(strPageId.equalsIgnoreCase(Long.toString(pageID)))
							{
								pageItem.setPageId(Long.parseLong(strPageId));
								
								String strName = str[1];
								pageItem.setAttachmentName(strName);
								
								pageItem.setSha(sWebStorageHelper.getNodeValue(
										nodeList.item(i),
										MetaData.SYNC_ATTR_SHA));
								pageItem.setLastSyncModifiedTime(Long.parseLong(sWebStorageHelper.getNodeValue(
										nodeList.item(i),
										MetaData.SYNC_ATTR_LASTCHANGETIME)));
								pageItem.setDeleted(Boolean.parseBoolean(sWebStorageHelper.getNodeValue(
										nodeList.item(i),
										MetaData.SYNC_ATTACHMENT_ATTR_ISDELETED)));
	
								pageItemList.add(pageItem);
							}
						}
					} else {
						Log.v(TAG, "Can not get " + strFileName
								+ "'s 'display' attribute");
					}
				}
			} else {
				Log.v(TAG, "Can not get 'file' attribute");
			}
		} else {
			Log.v(TAG, "None file list got");
		}
		return pageItemList;
	}
	public static Document getServerModifyFileList() throws Exception {
		Document doc;
		try {
			doc = MetaData.webStorage.getSuperNoteFileList(-1);
			Log.v(TAG, "Get SuperNote file list success");
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: 
				Log.v(TAG, "Get SuperNote file list success, Retry once...");
				try {
					doc = MetaData.webStorage.getSuperNoteFileList(-1);
					Log.v(TAG, "Retry success");
				} catch (Exception ex) {
					Log.v(TAG, "Retry failed");
					throw ex;
				}
				break;
			default: 
				throw webex;
			}
		}
		return doc;
	}
	//End   Darwin_Yu@asus.com
	public static List<SyncPageItem> getServerFileList() throws Exception {
		List<SyncPageItem> pageItemList = new ArrayList<SyncPageItem>();
		Document doc;
		try {
			doc = MetaData.webStorage.getSuperNoteFileList(-1);
			Log.v(TAG, "Get SuperNote file list success");
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: 
				Log.v(TAG, "Get SuperNote file list success, Retry once...");
				try {
					doc = MetaData.webStorage.getSuperNoteFileList(-1);
					Log.v(TAG, "Retry success");
				} catch (Exception ex) {
					Log.v(TAG, "Retry failed");
					throw ex;
				}
				break;
			default: 
				throw webex;
			}
		}
		if (doc != null) {
			NodeList nodeList = doc.getElementsByTagName("file");
			if (nodeList != null) {
				for (int i = 0; i < nodeList.getLength(); i++) {
					SyncPageItem pageItem = new SyncPageItem();
					String strFileName = sWebStorageHelper.decodeBase64(sWebStorageHelper.getNodeValue(
							nodeList.item(i), "display"));
					if (strFileName != null) {
						if (strFileName
								.endsWith(MetaData.SYNC_PAGE_FILE_EXTENSION)) {
							Node node = nodeList.item(i);
							if (node != null) {
								pageItem.setVersion(sWebStorageHelper.getNodeValue(node, 
										MetaData.SYNC_FILE_VERSION));
								String strPageId = strFileName.substring(
										0,
										strFileName.length()
												- MetaData.SYNC_PAGE_FILE_EXTENSION
														.length());
								pageItem.setPageId(Long.parseLong(strPageId));
								pageItem.setSha(sWebStorageHelper.getNodeValue(
										node,
										MetaData.SYNC_PAGE_FILE_ATTR_SHA));
								pageItem.setLastSyncModifiedTime(Long.parseLong(sWebStorageHelper.getNodeValue(
										node,
										MetaData.SYNC_PAGE_FILE_ATTR_LASTCHANGETIME)));
								pageItem.setDeleted(Boolean.parseBoolean(sWebStorageHelper.getNodeValue(
										node,
										MetaData.SYNC_PAGE_FILE_ATTR_ISDELETED)));
								pageItem.setLastSyncOwnerBookId(Long.parseLong(sWebStorageHelper.getNodeValue(
										nodeList.item(i),
										MetaData.SYNC_PAGE_FILE_ATTR_LASTNOTEBOOKID)));
								pageItem.setNotebookTitle(decodeBase64(sWebStorageHelper.getNodeValue(
										node,
										MetaData.SYNC_PAGE_FILE_ATTR_NOTEBOOKTITLE)));
								pageItem.setBookmark(Boolean.parseBoolean(sWebStorageHelper.getNodeValue(
										node,
										MetaData.SYNC_PAGE_FILE_ATTR_ISBOOKMARK)));
								pageItem.setNotebookBakColor(Integer.parseInt(sWebStorageHelper.getNodeValue(
										node,
										MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTBACKGROUND)));
								pageItem.setNotebookGridLine(Integer.parseInt(sWebStorageHelper.getNodeValue(
										node,
										MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTLINE)));
								pageItem.setNotebookPhoneMemo(Integer.parseInt((sWebStorageHelper.getNodeValue(
										node,
										MetaData.SYNC_PAGE_FILE_ATTR_CATEGORY))) > 0);
								pageItemList.add(pageItem);
							}
						}
					} else {
						Log.v(TAG, "Can not get " + strFileName
								+ "'s 'display' attribute");
					}
				}
			} else {
				Log.v(TAG, "Can not get 'file' attribute");
			}
		} else {
			Log.v(TAG, "None file list got");
		}
		return pageItemList;
	}

	public static void downloadPage(long pageId) throws WebStorageException {
		String tempDownloadDirPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_DOWNLOAD_DIR;
		File tempDownloadDir = new File(tempDownloadDirPath);
		if (!tempDownloadDir.exists()) {
			tempDownloadDir.mkdirs();
		}
		String strPageFileName = pageId + MetaData.SYNC_PAGE_FILE_EXTENSION;
		File tempFile = new File(tempDownloadDirPath + strPageFileName);
		if (tempFile.exists()) {
			tempFile.delete();
		}
		try {
			if (MetaData.webStorage.downloadOneFile(strPageFileName,
					tempDownloadDirPath + strPageFileName) != 1) {
				throw new WebStorageException(WebStorageException.OTHER_ERROR);
			} else {
				Log.v(TAG, "Download '" + strPageFileName + "'" + " to '"
						+ tempDownloadDirPath + strPageFileName + "' success");
			}
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: 
				Log.v(TAG, "Failed to download '" + strPageFileName + "'"
						+ " to '" + tempDownloadDirPath + strPageFileName
						+ "', Retry once...");
				try {
					if (MetaData.webStorage.downloadOneFile(strPageFileName,
							tempDownloadDirPath + strPageFileName) != 1) {
						throw new WebStorageException(
								WebStorageException.OTHER_ERROR);
					} else {
						Log.v(TAG, "Retry success");
					}
				} catch (WebStorageException wex) {
					Log.v(TAG, "Retry failed");
					throw wex;
				}
				break;
			default: 
				throw webex;
			}
		}
	}

	public static void uploadPage(SyncPageItem pageItem) throws Exception {
		String strPageFilePath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_UPLOAD_DIR + pageItem.getPageId()
				+ MetaData.SYNC_PAGE_FILE_EXTENSION;
		String fileSha = sWebStorageHelper.getFileSha1(strPageFilePath);
		String attribute = "";
		attribute += "<" + MetaData.SYNC_FILE_VERSION + ">" 
				+ MetaData.SYNC_CURRENT_VERSION + "</" 
				+ MetaData.SYNC_FILE_VERSION + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_SHA + ">"
				+ fileSha + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_SHA + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_LASTCHANGETIME + ">"
				+ pageItem.getLastSyncModifiedTime() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_LASTCHANGETIME + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_ISDELETED + ">"
				+ pageItem.isDeleted() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_ISDELETED + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_LASTNOTEBOOKID + ">"
				+ pageItem.getLastSyncOwnerBookId() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_LASTNOTEBOOKID + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_NOTEBOOKTITLE + ">"
				+ encodeBase64(pageItem.getNotebookTitle()) + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_NOTEBOOKTITLE + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_ISBOOKMARK + ">"
				+ pageItem.isBookmark() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_ISBOOKMARK + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTBACKGROUND + ">"
				+ pageItem.getNotebookBakColor() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTBACKGROUND + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTLINE + ">"
				+ pageItem.getNotebookGridLine() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTLINE + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_CATEGORY + ">"
				+ (pageItem.isNotebookPhoneMemo() ? 1 : 0) + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_CATEGORY + ">";
		//begin darwin
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_TEMPLATE + ">"
				+ pageItem.getNotebookTemplate() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_TEMPLATE + ">";
		//end   darwin
		
		//BEGIN: RICHARD
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_INDEXLANGUAGE + ">"
				+ pageItem.getNotebookIndexLanguage() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_INDEXLANGUAGE + ">";
		//END: RICHARD
		
		try {
			if (MetaData.webStorage.uploadOneFile(strPageFilePath, attribute,
					((pageItem.getSha() == null) || pageItem.getSha()
							.equals("")) ? null : pageItem.getSha(), fileSha) != 1) {
				throw new WebStorageException(WebStorageException.OTHER_ERROR);
			} else {
				Log.v(TAG, "Upload '" + strPageFilePath + "'" + " success");
				Log.v(TAG, "Attributes: " + attribute);
				// BEGIN: Shane_Wang@asus.com 2012-12-11
				SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.PageTable.uri, pageItem.getPageId(), fileSha));
				// END: Shane_Wang@asus.com 2012-12-11
			}
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: {
				Log.v(TAG, "Failed to upload '" + strPageFilePath
						+ "', Retry once...");
				Log.v(TAG, "Attributes: " + attribute);
				try {
					if (MetaData.webStorage.uploadOneFile(strPageFilePath,
							attribute,
							((pageItem.getSha() == null) || pageItem.getSha()
									.equals("")) ? null : pageItem.getSha(), fileSha) != 1) {
						throw new WebStorageException(
								WebStorageException.OTHER_ERROR);
					} else {
						Log.v(TAG, "Retry success");
						// BEGIN: Shane_Wang@asus.com 2012-12-11
						SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.PageTable.uri, pageItem.getPageId(), fileSha));
						// END: Shane_Wang@asus.com 2012-12-11
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
		File tempFile = new File(strPageFilePath);
		if (tempFile.exists()) {
			tempFile.delete();
		}
	}
	
	public static void updatePageAttribute(SyncPageItem pageItem) throws Exception {
		String strPageFileName = pageItem.getPageId()
				+ MetaData.SYNC_PAGE_FILE_EXTENSION;
		String attribute = "";
		attribute += "<" + MetaData.SYNC_FILE_VERSION + ">" 
				+ MetaData.SYNC_CURRENT_VERSION + "</" 
				+ MetaData.SYNC_FILE_VERSION + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_SHA + ">"
				+ pageItem.getSha() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_SHA + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_LASTCHANGETIME + ">"
				+ pageItem.getLastSyncModifiedTime() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_LASTCHANGETIME + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_ISDELETED + ">"
				+ pageItem.isDeleted() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_ISDELETED + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_LASTNOTEBOOKID + ">"
				+ pageItem.getLastSyncOwnerBookId() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_LASTNOTEBOOKID + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_NOTEBOOKTITLE + ">"
				+ encodeBase64(pageItem.getNotebookTitle()) + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_NOTEBOOKTITLE + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_ISBOOKMARK + ">"
				+ pageItem.isBookmark() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_ISBOOKMARK + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTBACKGROUND + ">"
				+ pageItem.getNotebookBakColor() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTBACKGROUND + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTLINE + ">"
				+ pageItem.getNotebookGridLine() + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_DEFAULTLINE + ">";
		attribute += "<" + MetaData.SYNC_PAGE_FILE_ATTR_CATEGORY + ">"
				+ (pageItem.isNotebookPhoneMemo() ? 1 : 0) + "</"
				+ MetaData.SYNC_PAGE_FILE_ATTR_CATEGORY + ">";
		try {
			if (MetaData.webStorage.setSpecificFileAttribute(strPageFileName, attribute) != 0) {
				throw new WebStorageException(WebStorageException.OTHER_ERROR);
			} else {
				Log.v(TAG, "Page '" + strPageFileName + "' set to '" + attribute + "'");
			}
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: {
				Log.v(TAG, "Failed to set '" + strPageFileName
						+ "' attribute, Retry once...");
				try {
					if (MetaData.webStorage.setSpecificFileAttribute(strPageFileName,
							attribute) != 0) {
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

	//Begin Darwin_Yu@asus.com
	
	public static void downloadPageAttachment(long pageId) throws WebStorageException 
	{
		for(int i = 0; i < mListAttachmentModify.size(); i++)
		{
			LocalPageItem modifyItem =  mListAttachmentModify.get(i);
			if(modifyItem.getIsLocalOrServerModify() == 1)
			{
				downloadPageOneAttachment(pageId,modifyItem.getAttachmentName());
			}
		}
	}
	
	public static void downloadPageOneAttachment(long pageId ,String fileName) throws WebStorageException 
	{
		String tempDownloadDirPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_DOWNLOAD_DIR;
		File tempDownloadDir = new File(tempDownloadDirPath);
		if (!tempDownloadDir.exists()) {
			tempDownloadDir.mkdirs();
		}
		String strPageFileName = pageId + "." + fileName + MetaData.SYNC_ATTACHMENT_FILE_EXTENSION;
		File tempFile = new File(tempDownloadDirPath + strPageFileName);
		if (tempFile.exists()) {
			tempFile.delete();
		}
		try {
			if (MetaData.webStorage.downloadOneFile(strPageFileName,
					tempDownloadDirPath + strPageFileName) != 1) {
				throw new WebStorageException(WebStorageException.OTHER_ERROR);
			} else {
				Log.v(TAG, "Download '" + strPageFileName + "'" + " to '"
						+ tempDownloadDirPath + strPageFileName + "' success");
			}
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: 
				Log.v(TAG, "Failed to download '" + strPageFileName + "'"
						+ " to '" + tempDownloadDirPath + strPageFileName
						+ "', Retry once...");
				try {
					if (MetaData.webStorage.downloadOneFile(strPageFileName,
							tempDownloadDirPath + strPageFileName) != 1) {
						throw new WebStorageException(
								WebStorageException.OTHER_ERROR);
					} else {
						Log.v(TAG, "Retry success");
					}
				} catch (WebStorageException wex) {
					Log.v(TAG, "Retry failed");
					throw wex;
				}
				break;
			default: 
				throw webex;
			}
		}
	}
	
	public static void downloadPageItem(long pageId) throws WebStorageException 
	{
		String tempDownloadDirPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_DOWNLOAD_DIR;
		File tempDownloadDir = new File(tempDownloadDirPath);
		if (!tempDownloadDir.exists()) {
			tempDownloadDir.mkdirs();
		}
		String strPageFileName = pageId + MetaData.SYNC_ITEM_FILE_EXTENSION;
		File tempFile = new File(tempDownloadDirPath + strPageFileName);
		if (tempFile.exists()) {
			tempFile.delete();
		}
		try {
			if (MetaData.webStorage.downloadOneFile(strPageFileName,
					tempDownloadDirPath + strPageFileName) != 1) {
				throw new WebStorageException(WebStorageException.OTHER_ERROR);
			} else {
				Log.v(TAG, "Download '" + strPageFileName + "'" + " to '"
						+ tempDownloadDirPath + strPageFileName + "' success");
			}
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: 
				Log.v(TAG, "Failed to download '" + strPageFileName + "'"
						+ " to '" + tempDownloadDirPath + strPageFileName
						+ "', Retry once...");
				try {
					if (MetaData.webStorage.downloadOneFile(strPageFileName,
							tempDownloadDirPath + strPageFileName) != 1) {
						throw new WebStorageException(
								WebStorageException.OTHER_ERROR);
					} else {
						Log.v(TAG, "Retry success");
					}
				} catch (WebStorageException wex) {
					Log.v(TAG, "Retry failed");
					throw wex;
				}
				break;
			default: 
				throw webex;
			}
		}
	}
	
	public static void downloadPageDoodle(long pageId) throws WebStorageException 
	{
		String tempDownloadDirPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_DOWNLOAD_DIR;
		File tempDownloadDir = new File(tempDownloadDirPath);
		if (!tempDownloadDir.exists()) {
			tempDownloadDir.mkdirs();
		}
		String strPageFileName = pageId + MetaData.SYNC_DOODLE_FILE_EXTENSION;
		File tempFile = new File(tempDownloadDirPath + strPageFileName);
		if (tempFile.exists()) {
			tempFile.delete();
		}
		try {
			if (MetaData.webStorage.downloadOneFile(strPageFileName,
					tempDownloadDirPath + strPageFileName) != 1) {
				throw new WebStorageException(WebStorageException.OTHER_ERROR);
			} else {
				Log.v(TAG, "Download '" + strPageFileName + "'" + " to '"
						+ tempDownloadDirPath + strPageFileName + "' success");
			}
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: 
				Log.v(TAG, "Failed to download '" + strPageFileName + "'"
						+ " to '" + tempDownloadDirPath + strPageFileName
						+ "', Retry once...");
				try {
					if (MetaData.webStorage.downloadOneFile(strPageFileName,
							tempDownloadDirPath + strPageFileName) != 1) {
						throw new WebStorageException(
								WebStorageException.OTHER_ERROR);
					} else {
						Log.v(TAG, "Retry success");
					}
				} catch (WebStorageException wex) {
					Log.v(TAG, "Retry failed");
					throw wex;
				}
				break;
			default: 
				throw webex;
			}
		}
	}
	
	private static void updateAttachmentDB(Context context,String fileName,long pageId)
	{
		long modifiedTime = System.currentTimeMillis();
		ContentValues cvAttachment = new ContentValues();
    	cvAttachment.put(MetaData.AttachmentTable.ID, pageId);
    	cvAttachment.put(MetaData.AttachmentTable.MODIFIED_DATE, modifiedTime);
    	cvAttachment.put(MetaData.AttachmentTable.IS_DELETE,  1 );
    	cvAttachment.put(MetaData.AttachmentTable.FILE_NAME, pageId + "." + fileName);
    	try
    	{
    		Uri uri = context.getContentResolver().insert(MetaData.AttachmentTable.uri, cvAttachment);
    	}
    	catch(Exception e)
    	{
    		Log.i("darwin", e.toString());
    	}
    	
    	Cursor cur = context.getContentResolver().query(MetaData.AttachmentTable.uri, null, "_id = ?", new String[]{Long.toString( pageId )}, null);
		if(cur != null && cur.getCount() > 0)
		{
			cur.moveToFirst();
			while (!cur.isAfterLast()) 
			{
                cur.moveToNext();
            }
		}
		//BEGIN: RICHARD
		if(cur != null)
		{
			cur.close();
		}
		//END: RICHARD
	}
	
	public static void unpackPageOneAttachment(long bookId, long pageId,String fileName,Context context) throws IOException 
	{
		String DirPath = MetaData.DATA_DIR + bookId + "/"
				+ pageId;
		String[] str = new String[2];
		str = getPageIdAndTrueNameFromAttachmentName(fileName);
		String DoodleFilePath = DirPath + "/" + str[1];
		File packedFile = new File(DoodleFilePath);
		if (packedFile.exists()) {
			packedFile.delete();
		}
		updateAttachmentDB(context,str[1],pageId);
		String downloadPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_DOWNLOAD_DIR + "/" + fileName;
		File fileInput = new File(downloadPath);
		File pageDir = new File(DirPath);
		try
		{
			if (pageDir.isDirectory() && pageDir.exists())
			{
				packedFile.createNewFile();
				InputStream in = null;
				in = new BufferedInputStream(new FileInputStream(fileInput));
				OutputStream out = null;
				out = new BufferedOutputStream(new FileOutputStream(packedFile));
				byte[] buffer = new byte[4096];
				int bytes;
				while ((bytes = in.read(buffer)) != -1) {
					out.write(buffer, 0, bytes);
				}
				in.close();
				out.close();
			}
		}
		catch(Exception e)
		{
			Log.i("darwin", e.toString());
		}
	}
	
	public static void unpackPageAttachment(long bookId, long pageId,Context context) throws IOException 
	{
		String downloadPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_DOWNLOAD_DIR;
		File pageDir = new File(downloadPath);
		
		for(String fileName : pageDir.list())
		{
			if(fileName.endsWith(MetaData.SYNC_ATTACHMENT_FILE_EXTENSION))
			{
				unpackPageOneAttachment(bookId,pageId,fileName,context);
				File tempFile = new File(downloadPath + fileName );
				if (tempFile.exists()) {
					tempFile.delete();
				}
			}
		}
	}
	
	public static void unpackPageItem(long bookId, long pageId,SyncPageCompareFun spcf) throws IOException 
	{
		String ItemDirPath = MetaData.DATA_DIR + bookId + "/"
				+ pageId;
		String ItemFilePath = ItemDirPath + "/" + MetaData.NOTE_ITEM_PREFIX;
		File packedFile = new File(ItemFilePath);
		if (packedFile.exists()) {
			packedFile.delete();
		}
		
		String downloadPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_DOWNLOAD_DIR + "/" + pageId + MetaData.SYNC_ITEM_FILE_EXTENSION;
		File fileInput = new File(downloadPath);
		File pageDir = new File(ItemDirPath);
		
		boolean isRename = true;
		
		if (pageDir.isDirectory() && pageDir.exists()) {
			FileInputStream fis = new FileInputStream(fileInput);
			BufferedInputStream bis = new BufferedInputStream(fis);
			AsusFormatReader afr = new AsusFormatReader(bis,
					NotePage.MAX_ARRAY_SIZE);
			AsusFormatReader.Item item = afr.readItem();

			boolean fileInfoCorrect = false;			
			
			if ((item != null) && (item.getId() == AsusFormat.SNF_SYNC_FILE_NOTE_BEGIN)) {
				while (item != null) {
					//TimeStampItem tsi = new TimeStampItem();
					switch (item.getId()) {
					case AsusFormat.SNF_SYNC_FILE_NOTE_BEGIN:
						fileInfoCorrect = true;
						break;
					case AsusFormat.SNF_SYNC_FILE_NOTE_END:
						fileInfoCorrect = false;
						break;
					case AsusFormat.SNF_SYNC_FILE_NOTE: {
						if (fileInfoCorrect) {
							boolean bCopy = false;
							File noteFile = new File(ItemFilePath);
							if (fileInfoCorrect) {
								noteFile.createNewFile();
								{
									bCopy = true;
								}
							}
							FileOutputStream fos = new FileOutputStream(noteFile);
							BufferedOutputStream bos = new BufferedOutputStream(fos);
							if (item.getType() == AsusFormatReader.Item.ITEM_TYPE_TOO_LARGE) {
								int size = item.getIntValue();
								byte[] buffer = new byte[4096];
								while (size - 4096 > 0) {
									bis.read(buffer);
									if (bCopy) {
										bos.write(buffer);
									}
									size -= 4096;
								}
								if (size > 0) {
									bis.read(buffer, 0, size);
									if (bCopy) {
										bos.write(buffer, 0, size);
									}
								}
							} else {
								if (bCopy) {
									bos.write(item.getByteArray());
								}
							}
							bos.close();
							fos.close();
							
							isRename = false;
						}
						break;
					}
					default:
						break;
					}
					item = afr.readItem();
				}
			}
			bis.close();
			fis.close();
		}
		
		if (!isRename || (isRename && fileInput.renameTo(packedFile))) {
			long id = -1;
			long pageid = pageId;
			long position = -1;
			
			boolean editorInfoCorrect = false;
			boolean timeStampCorrect = false;
			
			FileInputStream fis = new FileInputStream(packedFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			AsusFormatReader afr = new AsusFormatReader(bis,
					NotePage.MAX_ARRAY_SIZE);
			AsusFormatReader.Item item = afr.readItem();			
			
			while (item != null) {
				switch (item.getId()) {
				case AsusFormat.SNF_NITEM_BEGIN:
					editorInfoCorrect = true;
					break;
				case AsusFormat.SNF_NITEM_END:
					editorInfoCorrect = false;
					break;
				case AsusFormat.SNF_NITEM_TIMESTAMP_BEGIN:
					if (editorInfoCorrect) {
						timeStampCorrect = true;
					}
					break;
				case AsusFormat.SNF_NITEM_TIMESTAMP_END:
					if (timeStampCorrect) {
						if((id != -1) && (pageid != -1) && (position != -1))
						{
							spcf.updateTimeStampDB(id,pageid,position);
							id = 0;
							pageid = 0;
							position = 0;
						}
						timeStampCorrect = false;
					}
					break;
				case AsusFormat.SNF_NITEM_TIMESTAMP_POS_START:
					if (timeStampCorrect) {
						position = item.getLongValue();
					}
					break;
				case AsusFormat.SNF_NITEM_TIMESTAMP_POS_END:
					break;
				case AsusFormat.SNF_NITEM_TIMESTAMP_TIME:
					if (timeStampCorrect) {
						id = item.getLongValue();
					}
					break;
				default:
					break;
				}
				item = afr.readItem();
			}
			
			bis.close();
			fis.close();
		}

		File tempFile = new File(downloadPath);
		if (tempFile.exists()) {
			tempFile.delete();
		}
	}
	
	public static void unpackPageDoodle(long bookId, long pageId)
	{
		String DoodleDirPath = MetaData.DATA_DIR + bookId + "/"
				+ pageId;
		String DoodleFilePath = DoodleDirPath + "/" + MetaData.DOODLE_ITEM_PREFIX;
		File packedFile = new File(DoodleFilePath);
		if (packedFile.exists()) {
			packedFile.delete();
		}
		
		String downloadPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_DOWNLOAD_DIR + "/" + pageId + MetaData.SYNC_DOODLE_FILE_EXTENSION;
		File fileInput = new File(downloadPath);
		File pageDir = new File(DoodleDirPath);
		try
		{
			if (pageDir.isDirectory() && pageDir.exists())
			{
				fileInput.renameTo(packedFile);
			}
		}
		catch(Exception e)
		{
			Log.i("darwin", e.toString());
		}
		File tempFile = new File(downloadPath);
		if (tempFile.exists()) {
			tempFile.delete();
		}
		
	}
	
	public static void uploadPageDoodle(LocalPageItem pageItem , String Sha) throws Exception 
	{
		String strPageFilePath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_UPLOAD_DIR + pageItem.getPageId()
				+ MetaData.SYNC_DOODLE_FILE_EXTENSION;
		File tempFile = new File(strPageFilePath);
		if (!tempFile.exists()) {
			return;
		}
		
		String fileSha = sWebStorageHelper.getFileSha1(strPageFilePath);
		
		String attribute = "";
		attribute += "<" + MetaData.SYNC_ATTR_SHA + ">"
				+ fileSha + "</"
				+ MetaData.SYNC_ATTR_SHA + ">";
		attribute += "<" + MetaData.SYNC_ATTR_LASTCHANGETIME + ">"
				+ Long.toString(pageItem.getCurModifiedTime()) + "</"
				+ MetaData.SYNC_ATTR_LASTCHANGETIME + ">";
		attribute += "<" + MetaData.SYNC_DOODLE_ATTR_ISDELETED + ">"
				+ pageItem.isDeleted() + "</"
				+ MetaData.SYNC_DOODLE_ATTR_ISDELETED + ">";
		attribute += "<" + MetaData.SYNC_ATTR_FILE_VERSION + ">"
				+ "2.5.2.8.5" + "</"
				+ MetaData.SYNC_ATTR_FILE_VERSION + ">";
		try {
			if (MetaData.webStorage.uploadOneFile(strPageFilePath, attribute,
					((Sha == null) || Sha
							.equals("")) ? null : Sha, fileSha) != 1) {
				throw new WebStorageException(WebStorageException.OTHER_ERROR);
			} else {
				Log.v(TAG, "Upload '" + strPageFilePath + "'" + " success");
				Log.v(TAG, "Attributes: " + attribute);
				// BEGIN: Shane_Wang@asus.com 2012-12-11
				SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.DoodleTable.uri, pageItem.getPageId(), fileSha));
				// END: Shane_Wang@asus.com 2012-12-11
			}
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: {
				Log.v(TAG, "Failed to upload '" + strPageFilePath
						+ "', Retry once...");
				Log.v(TAG, "Attributes: " + attribute);
				try {
					if (MetaData.webStorage.uploadOneFile(strPageFilePath,
							attribute,
							((Sha == null) || Sha
									.equals("")) ? null : Sha, fileSha) != 1) {
						throw new WebStorageException(
								WebStorageException.OTHER_ERROR);
					} else {
						Log.v(TAG, "Retry success");
						// BEGIN: Shane_Wang@asus.com 2012-12-11
						SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.DoodleTable.uri, pageItem.getPageId(), fileSha));
						// END: Shane_Wang@asus.com 2012-12-11
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
		
		if (tempFile.exists()) {
			tempFile.delete();
		}
	}
	
	public static void uploadPageItem(LocalPageItem pageItem , String Sha) throws Exception 
	{
		String strPageFilePath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_UPLOAD_DIR + pageItem.getPageId()
				+ MetaData.SYNC_ITEM_FILE_EXTENSION;
		File tempFile = new File(strPageFilePath);
		if (!tempFile.exists()) {
			return;
		}
		
		String fileSha = sWebStorageHelper.getFileSha1(strPageFilePath);
		
		String attribute = "";
		attribute += "<" + MetaData.SYNC_ATTR_SHA + ">"
				+ fileSha + "</"
				+ MetaData.SYNC_ATTR_SHA + ">";
		attribute += "<" + MetaData.SYNC_ATTR_LASTCHANGETIME + ">"
				+ Long.toString(pageItem.getCurModifiedTime()) + "</"
				+ MetaData.SYNC_ATTR_LASTCHANGETIME + ">";
		attribute += "<" + MetaData.SYNC_DOODLE_ATTR_ISDELETED + ">"
				+ pageItem.isDeleted() + "</"
				+ MetaData.SYNC_DOODLE_ATTR_ISDELETED + ">";
		attribute += "<" + MetaData.SYNC_ATTR_FILE_VERSION + ">"
				+ "2.5.2.8.5" + "</"
				+ MetaData.SYNC_ATTR_FILE_VERSION + ">";
		try {
			if (MetaData.webStorage.uploadOneFile(strPageFilePath, attribute,
					((Sha == null) || Sha
							.equals("")) ? null : Sha, fileSha) != 1) {
				throw new WebStorageException(WebStorageException.OTHER_ERROR);
			} else {
				Log.v(TAG, "Upload '" + strPageFilePath + "'" + " success");
				Log.v(TAG, "Attributes: " + attribute);
				// BEGIN: Shane_Wang@asus.com 2012-12-11
				SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.ItemTable.uri, pageItem.getPageId(), fileSha));
				// END: Shane_Wang@asus.com 2012-12-11
			}
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: {
				Log.v(TAG, "Failed to upload '" + strPageFilePath
						+ "', Retry once...");
				Log.v(TAG, "Attributes: " + attribute);
				try {
					if (MetaData.webStorage.uploadOneFile(strPageFilePath,
							attribute,
							((Sha == null) || Sha
									.equals("")) ? null : Sha, fileSha) != 1) {
						throw new WebStorageException(
								WebStorageException.OTHER_ERROR);
					} else {
						Log.v(TAG, "Retry success");
						// BEGIN: Shane_Wang@asus.com 2012-12-11
						SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.ItemTable.uri, pageItem.getPageId(), fileSha));
						// END: Shane_Wang@asus.com 2012-12-11
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
		
		if (tempFile.exists()) {
			tempFile.delete();
		}
	}
	
	public static void uploadPageAttachment(ArrayList<LocalPageItem> PageItemList) throws Exception 
	{
		String tempUploadDirPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_UPLOAD_DIR;
		File tempUploadDir = new File(tempUploadDirPath);
		if (!tempUploadDir.exists()) {
			tempUploadDir.mkdirs();
		}
		uploadPageAttachmentOneByOne(PageItemList, tempUploadDirPath);
	}
	
	public static String[] getPageIdAndTrueNameFromAttachmentName(String Name)
	{
		String str[] = new String[2];
		int index = Name.indexOf(".");
		if(Name.length()<=(index + (MetaData.SYNC_ATTACHMENT_FILE_EXTENSION).length()))
		{
			return null;
		}
		else
		{
			str[0] = Name.substring(0, index);
			str[1] = Name.substring(index + 1, Name.length()-(MetaData.SYNC_ATTACHMENT_FILE_EXTENSION).length());
			return str;
		}
	}
	
	public static void uploadPageAttachmentOneByOne(ArrayList<LocalPageItem> PageItemList,String folder) throws Exception 
	{
		File tempFolder = new File(folder);
		
		for (String fileName : tempFolder.list()) 
		{
			if(fileName.endsWith(MetaData.SYNC_ATTACHMENT_FILE_EXTENSION))
			{
				for(int i = 0 ; i < PageItemList.size();i++)
				{
					LocalPageItem pageItemTemp = PageItemList.get(i);
					String str[] = new String[2];
					str = getPageIdAndTrueNameFromAttachmentName(fileName);
					if(pageItemTemp.getAttachmentName().equalsIgnoreCase(str[1]) && (pageItemTemp.getIsLocalOrServerModify() == 0))
					{
						try{
							uploadPageOneAttachment(pageItemTemp,fileName);
						}catch(WebStorageException webex) {
							// BEGIN: Shane_Wang@asus.com 2012-12-12
							if (webex.getErrorKind() == WebStorageException.UPLOAD_INIT_SHAVALUE_NOT_MATCH) {
									Node fileNode = MetaData.webStorage
											.getSpecificFileAttribute(pageItemTemp.getPageId() + MetaData.SYNC_ATTACHMENT_FILE_EXTENSION);
									if (fileNode != null) {
										String tmpSha = sWebStorageHelper.getNodeValue(fileNode,
												MetaData.SYNC_PAGE_FILE_ATTR_SHA);
										if (tmpSha != null) {
											if (tmpSha.equals(pageItemTemp.getSha())) {
												pageItemTemp.setSha("");
												try {
													uploadPageOneAttachment(pageItemTemp, fileName);
												} catch (Exception e1) {
													throw webex;
												}
											}
											// BEGIN: Shane_Wang@asus.com 2012-12-10
											else {
												SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.AttachmentTable.uri, pageItemTemp.getPageId(), tmpSha));
												throw webex;
											}
											// END: Shane_Wang@asus.com 2012-12-10
										}
										else{
											throw webex;
										}
										
									}
									else{
										throw webex;
									}
							}
							else{
								throw webex;
							}
						}
						break;
					}
				}
				
			}
		}
		for(int i = 0 ; i < PageItemList.size();i++)
		{
			LocalPageItem pageItemT = PageItemList.get(i);
			if(pageItemT.getIsLocalOrServerModify() == 1)			
			{
				deleteRemoteAttachment(pageItemT);
			}
		}
	}
	
	public static void deleteRemoteAttachment(LocalPageItem pageItem)
	{
		try
		{
			MetaData.webStorage.deleteRemoteFile(pageItem.getPageId() + "." + pageItem.getAttachmentName() + MetaData.SYNC_ATTACHMENT_FILE_EXTENSION);
		}
		catch(Exception e)
		{
			Log.i("darwin", e.toString());
		}
	}
	
	public static void uploadPageOneAttachment(LocalPageItem pageItem ,String fileName) throws Exception 
	{
		String strPageFilePath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_UPLOAD_DIR + fileName;
		String fileSha = sWebStorageHelper.getFileSha1(strPageFilePath);
		String attribute = "";
		attribute += "<" + MetaData.SYNC_ATTR_SHA + ">"
				+ fileSha + "</"
				+ MetaData.SYNC_ATTR_SHA + ">";
		attribute += "<" + MetaData.SYNC_ATTR_LASTCHANGETIME + ">"
				+ Long.toString(pageItem.getCurModifiedTime()) + "</"
				+ MetaData.SYNC_ATTR_LASTCHANGETIME + ">";
		attribute += "<" + MetaData.SYNC_ATTACHMENT_ATTR_ISDELETED + ">"
				+ pageItem.isDeleted() + "</"
				+ MetaData.SYNC_ATTACHMENT_ATTR_ISDELETED + ">";
		attribute += "<" + MetaData.SYNC_ATTR_FILE_VERSION + ">"
				+ "2.5.2.8.5" + "</"
				+ MetaData.SYNC_ATTR_FILE_VERSION + ">";
		try {
			if (MetaData.webStorage.uploadOneFile(strPageFilePath, attribute,
					((pageItem.getSha() == null) || pageItem.getSha()
							.equals("")) ? null : pageItem.getSha(), fileSha) != 1) {
				throw new WebStorageException(WebStorageException.OTHER_ERROR);
			} else {
				Log.v(TAG, "Upload '" + strPageFilePath + "'" + " success");
				// BEGIN: Shane_Wang@asus.com 2012-12-11
				SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.AttachmentTable.uri, pageItem.getPageId(), fileSha, pageItem.getAttachmentName()));
				// END: Shane_Wang@asus.com 2012-12-11
				Log.v(TAG, "Attributes: " + attribute);
			}
		} catch (WebStorageException webex) {
			switch (webex.getErrorKind()) {
			case WebStorageException.OTHER_ERROR: {
				Log.v(TAG, "Failed to upload '" + strPageFilePath
						+ "', Retry once...");
				Log.v(TAG, "Attributes: " + attribute);
				try {
					if (MetaData.webStorage.uploadOneFile(strPageFilePath,
							attribute,
							((pageItem.getSha() == null) || pageItem.getSha()
									.equals("")) ? null : pageItem.getSha(), fileSha) != 1) {
						throw new WebStorageException(
								WebStorageException.OTHER_ERROR);
					} else {
						Log.v(TAG, "Retry success");
						// BEGIN: Shane_Wang@asus.com 2012-12-11
						SyncPageCompareFun.AddToShaUpdateList(new DBInsertItem(MetaData.AttachmentTable.uri, pageItem.getPageId(), fileSha, pageItem.getAttachmentName()));
						// END: Shane_Wang@asus.com 2012-12-11
						Log.v(TAG, "Attributes: " + attribute);
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
		File tempFile = new File(strPageFilePath);
		if (tempFile.exists()) {
			tempFile.delete();
		}
	}
	
	
	public static void packPageAll(long bookId, long pageId,boolean isDoodleChange,boolean isItemChange, boolean isAttachmentChange) throws IOException 
	{
		String tempUploadDirPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_UPLOAD_DIR;
		File tempUploadDir = new File(tempUploadDirPath);
		if (!tempUploadDir.exists()) {
			tempUploadDir.mkdirs();
		}
		if(isDoodleChange)
		{
			packPageDoodle(bookId,pageId,tempUploadDirPath);
		}
		if(isItemChange)
		{
			packPageItem(bookId,pageId,tempUploadDirPath);
		}
		if(isAttachmentChange)
		{
			packPageAttachment(bookId,pageId,tempUploadDirPath);
		}
	}
	
	public static void packPageDoodle(long bookId, long pageId,String uploadPath) throws IOException 
	{
		String pageDirPath = MetaData.DATA_DIR + Long.toString(bookId) + "/"
				+ Long.toString(pageId);
		File pageDir = new File(pageDirPath);
		File fileInput = new File(pageDirPath + "/"	+ MetaData.DOODLE_ITEM_PREFIX);
		if (fileInput.exists()) {		
			String packedFilePath = uploadPath + pageId
					+ MetaData.SYNC_DOODLE_FILE_EXTENSION;
			File packedFile = new File(packedFilePath);
			if (packedFile.exists()) {
				packedFile.delete();
			}
	
			try
			{
				if (pageDir.isDirectory() && pageDir.exists())
						//&& packedFile.createNewFile()) 
				{
					if(!packedFile.exists())
					{
						packedFile.createNewFile();
					}
					InputStream in = null;
					in = new BufferedInputStream(new FileInputStream(fileInput));
					OutputStream out = null;
					out = new BufferedOutputStream(new FileOutputStream(packedFile));
					byte[] buffer = new byte[4096];
					int bytes;
					while ((bytes = in.read(buffer)) != -1) {
						out.write(buffer, 0, bytes);
					}
					in.close();
					out.close();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

	}
	
	public static void packPageAttachment(long bookId, long pageId,String uploadPath) throws IOException 
	{
		
		String pageDirPath = MetaData.DATA_DIR + Long.toString(bookId) + "/"
				+ Long.toString(pageId);
		File pageDir = new File(pageDirPath);
		if (pageDir.isDirectory() && pageDir.exists()) 
		{
			
			for (String fileName : pageDir.list()) 
			{
				boolean bIsNeedToUpload = false;
				for(int i = 0;i < mListAttachmentModify.size();i++)
				{
					if(mListAttachmentModify.get(i).getAttachmentName().equalsIgnoreCase(fileName)
							&& mListAttachmentModify.get(i).getIsLocalOrServerModify() == 0)
					{
						bIsNeedToUpload = true;
						break;
					}
				}
				if(bIsNeedToUpload)
				{
					if ((!fileName.equals(MetaData.THUMBNAIL_PREFIX))
							&& (!fileName.equals(MetaData.DOODLE_ITEM_PREFIX))
							&& (!fileName.equals(MetaData.NOTE_ITEM_PREFIX))
							) 
					{
						try {
							String packedFilePath = uploadPath + pageId + "." + fileName
									+ MetaData.SYNC_ATTACHMENT_FILE_EXTENSION;
							File packedFile = new File(packedFilePath);
							if (packedFile.exists()) {
								packedFile.delete();
							}
							if (!packedFile.exists()) {
								packedFile.createNewFile();
							}
							{
								InputStream in = null;
								in = new BufferedInputStream(new FileInputStream(pageDir + "/" + fileName));
								OutputStream out = null;
								out = new BufferedOutputStream(new FileOutputStream(packedFile));
								byte[] buffer = new byte[4096];
								int bytes;
								while ((bytes = in.read(buffer)) != -1) {
									out.write(buffer, 0, bytes);
								}
								in.close();
								out.close();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			
		}
		
	}
	
	public static void packPageItem(long bookId, long pageId,String uploadPath) throws IOException 
	{
		String pageDirPath = MetaData.DATA_DIR + Long.toString(bookId) + "/"
				+ Long.toString(pageId);
		File pageDir = new File(pageDirPath);
		File itemFile = new File(pageDir, MetaData.NOTE_ITEM_PREFIX);
		if (itemFile.exists()) {
			String packedFilePath = uploadPath + pageId
					+ MetaData.SYNC_ITEM_FILE_EXTENSION;
			File packedFile = new File(packedFilePath);
			if (packedFile.exists()) {
				packedFile.delete();
			}
			
			try {
				if (pageDir.isDirectory() && pageDir.exists()
						&& packedFile.createNewFile()) 
				{
					InputStream in = null;
					in = new BufferedInputStream(new FileInputStream(itemFile));
					OutputStream out = null;
					out = new BufferedOutputStream(new FileOutputStream(packedFile));
					byte[] buffer = new byte[4096];
					int bytes;
					while ((bytes = in.read(buffer)) != -1) {
						out.write(buffer, 0, bytes);
					}
					in.close();
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		m_listTimeStampUpload.clear();
	}

	
	static List<TimeStampItem> m_listTimeStampUpload = new ArrayList<TimeStampItem>();
	static List<TimeStampItem> m_listTimeStampDownload = new ArrayList<TimeStampItem>();
	
	
	public static void packPage(long bookId, long pageId,String version, long time) throws IOException {
		String tempUploadDirPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_UPLOAD_DIR;
		File tempUploadDir = new File(tempUploadDirPath);
		if (!tempUploadDir.exists()) {
			tempUploadDir.mkdirs();
		}
		String packedFilePath = tempUploadDirPath + pageId
				+ MetaData.SYNC_PAGE_FILE_EXTENSION;
		File packedFile = new File(packedFilePath);
		if (packedFile.exists()) {
			packedFile.delete();
		}
		String pageDirPath = MetaData.DATA_DIR + Long.toString(bookId) + "/"
				+ Long.toString(pageId);
		File pageDir = new File(pageDirPath);
		if (pageDir.isDirectory() && pageDir.exists()
				&& packedFile.createNewFile()) {
			FileOutputStream fos = new FileOutputStream(packedFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			AsusFormatWriter afw = new AsusFormatWriter(bos);
			afw.writeByteArray(AsusFormat.SNF_SYNC_FILE_INFO_BEGIN, null, 0, 0);

			afw.writeLong(0, time);
			afw.writeByteArray(AsusFormat.SNF_SYNC_FILE_INFO_END, null, 0, 0);
			bos.close();
			fos.close();
		}
	}
	//End   Darwin_Yu@asus.com
	public static void packPage(long bookId, long pageId) throws IOException {
		String tempUploadDirPath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_UPLOAD_DIR;
		File tempUploadDir = new File(tempUploadDirPath);
		if (!tempUploadDir.exists()) {
			tempUploadDir.mkdirs();
		}
		String packedFilePath = tempUploadDirPath + pageId
				+ MetaData.SYNC_PAGE_FILE_EXTENSION;
		File packedFile = new File(packedFilePath);
		if (packedFile.exists()) {
			packedFile.delete();
		}
		String pageDirPath = MetaData.DATA_DIR + Long.toString(bookId) + "/"
				+ Long.toString(pageId);
		File pageDir = new File(pageDirPath);
		if (pageDir.isDirectory() && pageDir.exists()
				&& packedFile.createNewFile()) {
			FileOutputStream fos = new FileOutputStream(packedFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			AsusFormatWriter afw = new AsusFormatWriter(bos);
			afw.writeByteArray(AsusFormat.SNF_SYNC_FILE_INFO_BEGIN, null, 0, 0);
			afw.writeFileForSync(AsusFormat.SNF_SYNC_FILE_THUMBNAIL, pageDirPath + "/"
					+ MetaData.THUMBNAIL_PREFIX);
			afw.writeFileForSync(AsusFormat.SNF_SYNC_FILE_DOODLE, pageDirPath + "/"
					+ MetaData.DOODLE_ITEM_PREFIX);
			afw.writeFileForSync(AsusFormat.SNF_SYNC_FILE_NOTE, pageDirPath + "/"
					+ MetaData.NOTE_ITEM_PREFIX);
			afw.writeByteArray(AsusFormat.SNF_SYNC_FILE_ATTACHMENT_INFO_BEGIN,
					null, 0, 0);
			for (String fileName : pageDir.list()) {
				if ((!fileName.equals(MetaData.THUMBNAIL_PREFIX))
						&& (!fileName.equals(MetaData.DOODLE_ITEM_PREFIX))
						&& (!fileName.equals(MetaData.NOTE_ITEM_PREFIX))) {
					afw.writeByteArray(AsusFormat.SNF_SYNC_FILE_FILE_BEGIN,
							null, 0, 0);
					afw.writeString(AsusFormat.SNF_SYNC_FILE_FILE_NAME,
							fileName);
					afw.writeFileForSync(AsusFormat.SNF_SYNC_FILE_FILE_CONTENT,
							pageDirPath + "/" + fileName);
					afw.writeByteArray(AsusFormat.SNF_SYNC_FILE_FILE_END, null,
							0, 0);
				}
			}
			afw.writeByteArray(AsusFormat.SNF_SYNC_FILE_ATTACHMENT_INFO_END,
					null, 0, 0);
			afw.writeByteArray(AsusFormat.SNF_SYNC_FILE_INFO_END, null, 0, 0);
			bos.close();
			fos.close();
		}
	}

	public static void unpackPage(long bookId, long pageId) throws IOException {
		String packedFilePath = MetaData.DATA_DIR
				+ MetaData.SYNC_TEMP_DOWNLOAD_DIR + pageId
				+ MetaData.SYNC_PAGE_FILE_EXTENSION;
		File packedFile = new File(packedFilePath);
		String pageDirPath = MetaData.DATA_DIR + Long.toString(bookId) + "/"
				+ Long.toString(pageId);
		File pageDir = new File(pageDirPath);
		if (pageDir.exists()) {
			rmdir(pageDirPath);
		}
		if (!pageDir.exists()) {
			pageDir.mkdirs();
		}
		boolean fileInfoCorrect = false;
		boolean attachmentCorrect = false;
		boolean fileCorrect = false;
		String fileName = null;
		FileInputStream fis = new FileInputStream(packedFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		AsusFormatReader afr = new AsusFormatReader(bis,
				NotePage.MAX_ARRAY_SIZE);
		AsusFormatReader.Item item = afr.readItem();
		while (item != null) {
			switch (item.getId()) {
			case AsusFormat.SNF_SYNC_FILE_INFO_BEGIN:
				fileInfoCorrect = true;
				break;
			case AsusFormat.SNF_SYNC_FILE_INFO_END:
				fileInfoCorrect = false;
				attachmentCorrect = false;
				fileCorrect = false;
				break;
			case AsusFormat.SNF_SYNC_FILE_THUMBNAIL: {
				boolean bCopy = false;
				File thumbFile = new File(pageDir + "/"
						+ MetaData.THUMBNAIL_PREFIX);
				if (fileInfoCorrect) {
					if (thumbFile.createNewFile()) {
						bCopy = true;
					}
				}
				FileOutputStream fos = new FileOutputStream(thumbFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				if (item.getType() == AsusFormatReader.Item.ITEM_TYPE_TOO_LARGE) {
					int size = item.getIntValue();
					byte[] buffer = new byte[4096];
					while (size - 4096 > 0) {
						bis.read(buffer);
						if (bCopy) {
							bos.write(buffer);
						}
						size -= 4096;
					}
					if (size > 0) {
						bis.read(buffer, 0, size);
						if (bCopy) {
							bos.write(buffer, 0, size);
						}
					}
				} else {
					if (bCopy) {
						bos.write(item.getByteArray());
					}
				}
				bos.close();
				fos.close();
				break;
			}
			case AsusFormat.SNF_SYNC_FILE_DOODLE: {
				boolean bCopy = false;
				File doodleFile = new File(pageDir + "/"
						+ MetaData.DOODLE_ITEM_PREFIX);
				if (fileInfoCorrect) {
					if (doodleFile.createNewFile()) {
						bCopy = true;
					}
				}
				FileOutputStream fos = new FileOutputStream(doodleFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				if (item.getType() == AsusFormatReader.Item.ITEM_TYPE_TOO_LARGE) {
					int size = item.getIntValue();
					byte[] buffer = new byte[4096];
					while (size - 4096 > 0) {
						bis.read(buffer);
						if (bCopy) {
							bos.write(buffer);
						}
						size -= 4096;
					}
					if (size > 0) {
						bis.read(buffer, 0, size);
						if (bCopy) {
							bos.write(buffer, 0, size);
						}
					}
				} else {
					if (bCopy) {
						bos.write(item.getByteArray());
					}
				}
				bos.close();
				fos.close();
				break;
			}
			case AsusFormat.SNF_SYNC_FILE_NOTE: {
				boolean bCopy = false;
				File noteFile = new File(pageDir + "/"
						+ MetaData.NOTE_ITEM_PREFIX);
				if (fileInfoCorrect) {
					if (noteFile.createNewFile()) {
						bCopy = true;
					}
				}
				FileOutputStream fos = new FileOutputStream(noteFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				if (item.getType() == AsusFormatReader.Item.ITEM_TYPE_TOO_LARGE) {
					int size = item.getIntValue();
					byte[] buffer = new byte[4096];
					while (size - 4096 > 0) {
						bis.read(buffer);
						if (bCopy) {
							bos.write(buffer);
						}
						size -= 4096;
					}
					if (size > 0) {
						bis.read(buffer, 0, size);
						if (bCopy) {
							bos.write(buffer, 0, size);
						}
					}
				} else {
					if (bCopy) {
						bos.write(item.getByteArray());
					}
				}
				bos.close();
				fos.close();
				break;
			}
			case AsusFormat.SNF_SYNC_FILE_ATTACHMENT_INFO_BEGIN:
				if (fileInfoCorrect) {
					attachmentCorrect = true;
				}
				break;
			case AsusFormat.SNF_SYNC_FILE_ATTACHMENT_INFO_END:
				if (fileInfoCorrect) {
					attachmentCorrect = false;
					fileCorrect = false;
				}
				break;
			case AsusFormat.SNF_SYNC_FILE_FILE_BEGIN:
				if (attachmentCorrect) {
					fileCorrect = true;
				}
				break;
			case AsusFormat.SNF_SYNC_FILE_FILE_END:
				if (attachmentCorrect) {
					fileCorrect = false;
				}
				break;
			case AsusFormat.SNF_SYNC_FILE_FILE_NAME:
				if (fileCorrect) {
					fileName = item.getStringValue();
					fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
				}
				break;
			case AsusFormat.SNF_SYNC_FILE_FILE_CONTENT: {
				boolean bCopy = false;
				File file = null;
				if (fileCorrect) {
					if (fileName != null) {
						file = new File(pageDir, fileName);
						if (file.exists()) {
							file.delete();
						}
						if (!file.exists()) {
							if (file.createNewFile()) {
								bCopy = true;
							}
						}
					}
				}
				FileOutputStream fos = null;
				BufferedOutputStream bos = null;
				if (bCopy) {
					fos = new FileOutputStream(file);
					bos = new BufferedOutputStream(fos);
				}
				if (item.getType() == AsusFormatReader.Item.ITEM_TYPE_TOO_LARGE) {
					int size = item.getIntValue();
					byte[] buffer = new byte[4096];
					while (size - 4096 > 0) {
						bis.read(buffer);
						if (bCopy) {
							bos.write(buffer);
						}
						size -= 4096;
					}
					if (size > 0) {
						bis.read(buffer, 0, size);
						if (bCopy) {
							bos.write(buffer, 0, size);
						}
					}
				} else {
					if (bCopy) {
						bos.write(item.getByteArray());
					}
				}
				if (bCopy) {
					bos.close();
					fos.close();
				}
				fileName = null;
			}
				break;
			default:
				break;
			}
			item = afr.readItem();
		}
		bis.close();
		fis.close();
		File tempFile = new File(packedFilePath);
		if (tempFile.exists()) {
			tempFile.delete();
		}
	}

	public static void copyPageFile(long bookId, long pageId) {
		File src = new File(MetaData.DATA_DIR + bookId + "/" + pageId);
		File dst = new File(MetaData.DATA_DIR + bookId + "/"
				+ System.currentTimeMillis());
		copyFiles(src, dst);
	}

	public static void rmdir(String strDir) {
		File dir = new File(strDir);
		if (dir.isDirectory()) {
			for (String strFile : dir.list()) {
				File file = new File(strDir + "/" + strFile);
				if (file.isDirectory()) {
					rmdir(strDir + "/" + strFile);
				} else if (file.isFile()) {
					file.delete();
				}
			}
			dir.delete();
		} else if (dir.isFile()) {
			dir.delete();
		}
	}

	private static void copyFiles(File src, File dest) {
		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdir();
			}
			String[] children = src.list();
			for (String child : children) {
				copyFiles(new File(src, child), new File(dest, child));
			}
		} else {
			try {
				FileChannel srcChannel = new FileInputStream(src).getChannel();
				FileChannel destChannel = new FileOutputStream(dest)
						.getChannel();
				destChannel.transferFrom(srcChannel, 0, srcChannel.size());
				srcChannel.close();
				destChannel.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
	}
	public static String encodeBase64(String str) {
    	byte[] encodeBt;
    	try {
    		encodeBt = str.getBytes("UTF-8");
    	} catch (Exception e) {
    		encodeBt = str.getBytes();
    	}
    	String encodedStr;
    	try {
    		encodedStr = Base64.encodeToString(encodeBt, 0, encodeBt.length,
				Base64.URL_SAFE);
    	} catch (Exception e) {
    		encodedStr = "";
    	}
		return encodedStr;
	}
	private static String decodeBase64(String str) {
		String decodedStr;
		try {
			byte[] decodeBytes = Base64.decode(str, Base64.URL_SAFE);
			try {
				decodedStr = new String(decodeBytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				decodedStr = new String(decodeBytes);
			}
		} catch (Exception e) {
			decodedStr = "";
		}
		return decodedStr;
	}
}

// END: Better
