package com.asus.supernote.data;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.util.Log;

import com.asus.supernote.R;
import com.asus.supernote.doodle.DoodleItem;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.AttacherTool;
import com.asus.supernote.editable.noteitem.NoteForegroundColorItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteBaselineItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteImageItem;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteSendIntentItem;
import com.asus.supernote.editable.noteitem.NoteStringItem;
import com.asus.supernote.editable.noteitem.NoteTextStyleItem;
import com.asus.supernote.editable.noteitem.NoteTimestampItem;
import com.asus.supernote.fota.V1DataConverter;

public class PageDataLoader {

	private ArrayList<NoteItem> mNoteItemList = null;
	private ArrayList<NoteItemArray> mTemplateItemList = null;//Allen
	private DoodleItem mDoodleItem = null;

	private static PageDataLoader mInstance = null;
	private Context mContext = null;
	
	private boolean mIsLoading = false;

	public static synchronized PageDataLoader getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new PageDataLoader(context.getApplicationContext());
		}
		return mInstance;
	}
	
	public static boolean isFileDataFormatVersionSupported(short version) {
		int len = MetaData.SupportedDataFormatVersions.length;
		for (int i = 0; i < len; i++) {
			if (MetaData.SupportedDataFormatVersions[i] == version) {
				return true;
			}
		}
		return false;
	}

	//Begin Allen
	public ArrayList<NoteItemArray> getAllNoteItems() {
		if ((mTemplateItemList != null) && (mTemplateItemList.size() > 0)) {
			Log.v(MetaData.DEBUG_TAG, "getAllNoteItems not null");
			return mTemplateItemList;
		} else {
			Log.v(MetaData.DEBUG_TAG, "getAllNoteItems null");
			return null;
		}
	}
	
	public ArrayList<NoteItemArray> getAllNoteItemsForSearch(){
		if ((mTemplateItemList != null) && (mTemplateItemList.size() > 0)) {
			for(NoteItemArray item:mTemplateItemList){
				item.initUnitCount();
			}
			return mTemplateItemList;
		} else {
			return null;
		}
	}
	//End Allen
	
	public NoteItem[] getNoteItems(){
		if ((mNoteItemList != null) && (mNoteItemList.size() > 0)) {
			return mNoteItemList.toArray(new NoteItem[0]);
		} else {
			return null;
		}
	}
	
	public void setNoteItems(NoteItem[] items) {
		mNoteItemList = (ArrayList<NoteItem>)Arrays.asList(items);
	}

	public DoodleItem getDoodleItem() {
		return mDoodleItem;
	}
	
	public void setDoodleItem(DoodleItem item) {
		mDoodleItem = item;
	}

	public boolean load(NotePage notePage) {
		if (mIsLoading || notePage == null) { //smilefish fix bug 601253
			Log.v(MetaData.DEBUG_TAG, "loadpage, loading return false");
			return false;
		}
		
		mIsLoading = true;
		
		mNoteItemList = null;
		mTemplateItemList = null;//Allen
		mDoodleItem = null;
		
		boolean isSucceeded = true;
		if (notePage.getVersion() == 3) {
			isSucceeded = loadNoteItems(notePage);
			Log.v(MetaData.DEBUG_TAG, "PageDataLoader 3: load item: " + isSucceeded);
			if (isSucceeded) {
				isSucceeded = loadDoodleItem(notePage);
				Log.v(MetaData.DEBUG_TAG, "PageDataLoader 3: load doodle: " + isSucceeded);
			}
		} else if (notePage.getVersion() == 1) {
			V1DataConverter converter = new V1DataConverter(mContext);
			isSucceeded = converter.load(notePage);
			Log.v(MetaData.DEBUG_TAG, "PageDataLoader 1: load: " + isSucceeded);
			if (isSucceeded) {
				mNoteItemList = converter.getPageItems();
				mDoodleItem = converter.getPageDoodleItem();
			}
		} else {
			isSucceeded = false;
		}
		
		mIsLoading = false;
		
		return isSucceeded;
	}
	
	//BEGIN: RICHARD
	//if flag false , just load NoteItemList
	public boolean load(NotePage notePage,Boolean flag) {
		if (mIsLoading) {
			return false;
		}
		
		mIsLoading = true;
		
		mNoteItemList = null;
		mTemplateItemList =null;//Allen
		mDoodleItem = null;
		
		boolean isSucceeded = true;
		if (notePage.getVersion() == 3) {
			isSucceeded = loadNoteItems(notePage);
			if (isSucceeded && flag) {
				isSucceeded = loadDoodleItem(notePage);
			}
		} else if (notePage.getVersion() == 1) {
			V1DataConverter converter = new V1DataConverter(mContext);
			isSucceeded = converter.load(notePage,flag);
			if (isSucceeded) {
				mNoteItemList = converter.getPageItems();
				mDoodleItem = converter.getPageDoodleItem();
			}
		} else {
			isSucceeded = false;
		}
		
		mIsLoading = false;
		
		return isSucceeded;
	}
	//END: RICHARD

	public PageDataLoader(Context context) {//RICHARD
		mContext = context;
	}
	
	public boolean loadNoteItems(NotePage notePage) {
		NoteBook noteBook = BookCase.getInstance(mContext).getNoteBook(
				notePage.getOwnerBookId());
		boolean isNoteItemCorrect = false;
		File file = new File(notePage.getFilePath(), MetaData.NOTE_ITEM_PREFIX);
		if (file == null || !file.exists()) {
			file = new File(notePage.getFilePath(),
					MetaData.NOTE_ITEM_PREFIX_DEPRECATED);
		}
		if (file != null && file.exists()) {
			try {
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				AsusFormatReader afr = new AsusFormatReader(bis,
						NotePage.MAX_ARRAY_SIZE);
				AsusFormatReader.Item item = afr.readItem();
				if(item!=null){
					mTemplateItemList = new ArrayList<NoteItemArray>();//Allen
				}

				short version = 0;//Allen
				NoteItemArray noteItemArray = null;//Allen
				while (item != null) {
					switch (item.getId()) {
					case AsusFormat.SNF_NITEM_BEGIN:
						isNoteItemCorrect = true;
						mNoteItemList = new ArrayList<NoteItem>();//Allen
						noteItemArray = new NoteItemArray(mNoteItemList,NoteItemArray.TEMPLATE_CONTENT_DEFAULT_NOTE_EDITTEXT);//Allen
						mTemplateItemList.add(noteItemArray);//Allen
						break;
					case AsusFormat.SNF_NITEM_VERSION:
                    	if (!isNoteItemCorrect) {
                    		return false;
                    	}
                    	version = item.getShortValue();
                    	if (!isFileDataFormatVersionSupported(version)) {
                    		Log.v(MetaData.DEBUG_TAG, "load item, version not supported: " + version);
                    		return false;
                    	}
                    	break;
                    	//Begin Allen
					case AsusFormat.SNF_NITEM_TEMPLATE_ITEM_TYPE:
						short templateItemType = item.getShortValue();
						if(noteItemArray!=null){
							noteItemArray.setTemplateItemType(templateItemType);
						}
						//End Allen
					case AsusFormat.SNF_NITEM_STRING_BEGIN:
						if (!isNoteItemCorrect) {
							Log.v(MetaData.DEBUG_TAG, "load item, string without correct begin");
							return false;
						}
						NoteStringItem stringItem = new NoteStringItem();
						stringItem.itemLoad(afr);
						if (mNoteItemList != null) {
							mNoteItemList.add(0, stringItem);
						} else {
							Log.v(MetaData.DEBUG_TAG, "load item, string, mNoteItemList null");
							return false;
						}
						break;
					case AsusFormat.SNF_NITEM_HANDWRITE_BEGIN:
						if (!isNoteItemCorrect) {
							Log.v(MetaData.DEBUG_TAG, "load item, handwriting item without correct begin");
							return false;
						}
						NoteHandWriteItem hwItem = new NoteHandWriteItem();
						hwItem.itemLoad(afr);
						if (mNoteItemList != null) {
							mNoteItemList.add(hwItem);
						} else {
							Log.v(MetaData.DEBUG_TAG, "load item, handwriting, mNoteItemList null");
							return false;
						}
						break;
					case AsusFormat.SNF_NITEM_HANDWRITEBL_BEGIN:
						if (!isNoteItemCorrect) {
							Log.v(MetaData.DEBUG_TAG, "load item, handwritingbl item without correct begin");
							return false;
						}
						NoteHandWriteBaselineItem hwblItem = new NoteHandWriteBaselineItem();
						hwblItem.itemLoad(afr);
						if (mNoteItemList != null) {
							mNoteItemList.add(hwblItem);
						} else {
							Log.v(MetaData.DEBUG_TAG, "load item, handwritingbl, mNoteItemList null");
							return false;
						}
						break;
					case AsusFormat.SNF_NITEM_TEXTSTYLE_BEGIN:
						if (!isNoteItemCorrect) {
							Log.v(MetaData.DEBUG_TAG, "load item, style item without correct begin");
							return false;
						}
						NoteTextStyleItem styleItem = new NoteTextStyleItem();
						styleItem.itemLoad(afr);
						if (mNoteItemList != null) {
							mNoteItemList.add(styleItem);
						} else {
							Log.v(MetaData.DEBUG_TAG, "load item, style, mNoteItemList null");
							return false;
						}
						break;
					case AsusFormat.SNF_NITEM_FCOLOR_BEGIN:
						if (!isNoteItemCorrect) {
							Log.v(MetaData.DEBUG_TAG, "load item, color item without correct begin");
							return false;
						}
						NoteForegroundColorItem fColorItem = new NoteForegroundColorItem();
						fColorItem.itemLoad(afr);
						if (mNoteItemList != null) {
							mNoteItemList.add(fColorItem);
						} else {
							Log.v(MetaData.DEBUG_TAG, "load item, color, mNoteItemList null");
							return false;
						}
						break;
					case AsusFormat.SNF_NITEM_SENDINTENT_BEGIN:
						if (!isNoteItemCorrect) {
							Log.v(MetaData.DEBUG_TAG, "load item, sendintent item without correct begin");
							return false;
						}
						NoteSendIntentItem sendIntentItem = new NoteSendIntentItem();
						sendIntentItem.itemLoad(afr);
						sendIntentItem.prepareIntent(notePage);

						boolean isVideo = false;
						if (sendIntentItem.getIntent() != null) {
							isVideo = (sendIntentItem
									.getIntent()
									.getType()
									.equals(NoteSendIntentItem.INTENT_TYPE_VIDEO) 
									|| sendIntentItem
									.getIntent()
									.getType()
									.equals(NoteSendIntentItem.INTENT_TYPE_VIDEO_OLD)) ? true : false;
							
						} else {
							isVideo = true;
							
						}
						
						//Begin Dave.To modify voice/video attacher UI. 
						
						String fullPath = notePage.getFilePath() + "//" +  sendIntentItem.getFileName();
						AttacherTool tool = new AttacherTool();
						String imageItemInfo = tool.getFileNameNoEx(sendIntentItem.getFileName()) + tool.getElapsedTime(fullPath);
						NoteImageItem imageItem = new NoteImageItem(
								mContext, 
								isVideo, 
								getImageSpanHeight(
										noteBook,
										false,
										noteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE), imageItemInfo);
						//End Dave.
						imageItem.setStart(sendIntentItem.getStart());
						imageItem.setEnd(sendIntentItem.getEnd());

						if (mNoteItemList != null) {
							mNoteItemList.add(sendIntentItem);
						} else {
							Log.v(MetaData.DEBUG_TAG, "load item, sendintent, mNoteItemList null");
							return false;
						}
						if (mNoteItemList != null) {
							mNoteItemList.add(imageItem);
						} else {
							Log.v(MetaData.DEBUG_TAG, "load item, imageitem, mNoteItemList null");
							return false;
						}
						break;
					case AsusFormat.SNF_NITEM_TIMESTAMP_BEGIN:
						if (!isNoteItemCorrect) {
							Log.v(MetaData.DEBUG_TAG, "load item, timestamp item without correct begin");
							return false;
						}
						NoteTimestampItem timestampItem = new NoteTimestampItem(notePage.getCreatedTime(),
								getImageSpanHeight(
										noteBook,
										false,
										noteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE));
						timestampItem.itemLoad(afr);
						if (mNoteItemList != null) {
							mNoteItemList.add(timestampItem);
						} else {
							Log.v(MetaData.DEBUG_TAG, "load item, timestamp, mNoteItemList null");
							return false;
						}
						break;
					case AsusFormat.SNF_NITEM_END:
						isNoteItemCorrect = false;
						break;
					default:
						break;
					}
					item = afr.readItem();
				}
				
				for(NoteItemArray noteItems:mTemplateItemList){//Allen
					if(noteItems.getTemplateItemType() == NoteItemArray.TEMPLATE_CONTENT_DEFAULT_NOTE_EDITTEXT){
						mNoteItemList = noteItems.getNoteItems();
						break;
					}
				}
				
				Log.v(MetaData.DEBUG_TAG, "load item, result: true");
				fis.close();
				return true;
			} catch (Exception e) {
				Log.v(MetaData.DEBUG_TAG, "load item, exception: " + e.toString());
				e.printStackTrace();
				return false;
			}
		} else {
			Log.v(MetaData.DEBUG_TAG, "load item, result: true, not exists");
			return true;
		}
	}
	
	private boolean loadDoodleItem(NotePage notePage) {
		File doodleFile = new File(notePage.getFilePath(),
				MetaData.DOODLE_ITEM_PREFIX);
		if (!doodleFile.exists()) {
			doodleFile = new File(notePage.getFilePath(),
					MetaData.DOODLE_ITEM_PREFIX_LEGEND);
		}
		if ((doodleFile != null) && doodleFile.exists()) {
			try {
				boolean isDoodleItemCorrect = false;
				mDoodleItem = new DoodleItem();
				FileInputStream fis = new FileInputStream(doodleFile);
				BufferedInputStream bis = new BufferedInputStream(fis);
				AsusFormatReader afr = new AsusFormatReader(bis,
						NotePage.MAX_ARRAY_SIZE);
				if (mDoodleItem != null) {
					mDoodleItem.itemLoad(afr);
				} else {
					Log.v(MetaData.DEBUG_TAG, "load doodle, mDoodleItem null");
					bis.close();
					fis.close();
					return false;
				}
				if (mDoodleItem != null) {
					isDoodleItemCorrect = mDoodleItem.getParsingResult();
				} else {
					Log.v(MetaData.DEBUG_TAG, "load doodle, mDoodleItem null");
					bis.close();
					fis.close();
					return false;
				}
				if (mDoodleItem != null) {
					Log.v(MetaData.DEBUG_TAG, "load doodle, mDoodleItem size >= 0");
					mDoodleItem = mDoodleItem.size() > 0 ? mDoodleItem : null;
				} else {
					Log.v(MetaData.DEBUG_TAG, "load doodle, mDoodleItem null");
					bis.close();
					fis.close();
					return false;
				}

				bis.close();
				fis.close();
				
				Log.v(MetaData.DEBUG_TAG, "load doodle, result: " + isDoodleItemCorrect);
				return isDoodleItemCorrect;
			} catch (Exception e) {
				Log.v(MetaData.DEBUG_TAG, "load doodle, exception: " + e.toString());
				e.printStackTrace();
				return false;
			}
		} else {
			Log.v(MetaData.DEBUG_TAG, "load doodle, result: true, not exists");
			return true;
		}
	}

	private int getImageSpanHeight(NoteBook noteBook, boolean isFull,
			boolean isSmallScreen) {
		int textSize = MetaData.BOOK_FONT_NORMAL;
		float fontSize = 0;
		textSize = noteBook.getFontSize();
		boolean mIsXLargeScreenSize = (mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                == Configuration.SCREENLAYOUT_SIZE_XLARGE;
		
		//BEGIN: RICHARD
		fontSize = NotePageValue.getFontSize(mContext,textSize,isSmallScreen);
		//END: RICHARD

		FontMetricsInt fontMetricsInt;
		Paint paint = new Paint();
		paint.setTextSize(fontSize);
		fontMetricsInt = paint.getFontMetricsInt();
		if (!isFull) {
			return (int) (fontMetricsInt.descent
					* PageEditor.FONT_DESCENT_RATIO - fontMetricsInt.ascent);
		} else {
			return (int) (getFirstLineHeight(isSmallScreen) + fontMetricsInt.descent
					* PageEditor.FONT_DESCENT_RATIO);
		}
	}

	private int getFirstLineHeight(boolean isSmallScreen) {
		if (isSmallScreen) {
			return mContext.getResources().getInteger(
					R.integer.first_line_height_small_screen);
		} else {
			return mContext.getResources().getInteger(
					R.integer.first_line_height);
		}
	}

}
