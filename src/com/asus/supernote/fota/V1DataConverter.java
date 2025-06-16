package com.asus.supernote.fota;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Paint.FontMetricsInt;
import android.net.Uri;
import android.util.Log;

import com.asus.supernote.R;
import com.asus.supernote.data.AsusFormatReader;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.NotePageValue;
import com.asus.supernote.doodle.DoodleItem;
import com.asus.supernote.doodle.DoodleItem.SerAnnotationInfo;
import com.asus.supernote.doodle.DoodleItem.SerDrawInfo;
import com.asus.supernote.doodle.DoodleItem.SerEraseInfo;
import com.asus.supernote.doodle.DoodleItem.SerGraphicInfo;
import com.asus.supernote.doodle.DoodleItem.SerPointInfo;
import com.asus.supernote.doodle.drawinfo.DrawInfo;
import com.asus.supernote.doodle.drawinfo.EraseDrawInfo;
import com.asus.supernote.doodle.drawtool.DrawTool;
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
import com.asus.supernote.editable.noteitem.NoteHandWriteItem.PathInfo;
import com.asus.supernote.picker.PickerUtility;

public class V1DataConverter extends CompatibleDataConverter implements
		DataReader.OnReadListener {
	
	public static final String TAG = "V1DataConverter";

	private Context mContext = null;
	private V1DataReader mReader = new V1DataReader();
	
	private BookCase mBookCase = null;
	private NoteBook mNoteBook = null;
	private NotePage mNotePage = null;
	
	private int		 mBookIndex = -1;
	
	private boolean mIsPaintBook = false;
	
	private boolean mIsBookDirExists = false;
	private boolean mIsPageFileExists = false;
	
	private NoteStringItem            mStringItem        = null;
	private NoteForegroundColorItem   mColorItem         = null;
	private NoteTextStyleItem         mStyleItem         = null;
	private NoteSendIntentItem        mIntentItem        = null;
	private NoteTimestampItem         mTimestampItem     = null;

	private ArrayList<NoteItem> mNoteItemList = new ArrayList<NoteItem>();

	private int mSendIntentStartAt = -1;
	private int mSendIntentEndAt   = -1;

	private static final short       HW_DOWN_DETECT_VALUE     = 255;
    private static final short       HW_MASK_BLOD             = 0x00F0;
    private static final short       HW_MASK_ALIGNMENT        = 0x000F;
    private static final short       HW_BLOD                  = 0x0030;
    private static final short       HW_ALIGNMENT_BOTTOM      = 0x0002;
    
    private static final float       NOTE_BOOK_SCALE          = 1.5f;
    private static final float       PAINT_BOOK_SCALE		  = 1.0f;
    private static  int         DEFAULT_CANVAS_WIDTH     = 1280 ;//darwin
    private static  int         DEFAULT_CANVAS_HEIGHT    = 1593 ;//darwin
    private static final short       LAYER_OP_STYLE_NORMAL    = 1;
    private static final short       LAYER_OP_STYLE_SCRIBBLE  = 5;
    private static final short       LAYER_OP_STYLE_SKETCH    = 6;
    private static final short       LAYER_OP_STYLE_NEON      = 8;
    private static final short       LAYER_OP_STYLE_ERASER    = 20;
    private short                    mCurrentLayerVersion     = -1;
    private short                    mCurrentRotateDegree     = 0;
    private short                    mCurrentPaintBrushStyle  = 0;
    private int                      mCurrentPenBrushColor    = Color.BLACK;
    private float                    mCurrentPenBrushWidth    = MetaData.DOODLE_DEFAULT_PAINT_WIDTH;
    private float[]                  mCurrentPoints           = null;
    private Matrix                   mCurrentLayerTransMatrix = new Matrix();
    private String                   mCurrentFileName         = null;
    private Rect                     mCurrentLayerRect        = null;
    private Rect                     mCurrentOpRect 		  = new Rect();
    private LinkedList<SerPointInfo> mCurrentPathList         = new LinkedList<SerPointInfo>();
    private LinkedList<SerEraseInfo> mCurrentEraseList        = new LinkedList<SerEraseInfo>();
    private LinkedList<SerDrawInfo>  mDrawInfoList            = new LinkedList<SerDrawInfo>();
    private DoodleItem               mDoodleItem              = null;

    private Paint                    mHwPaint                 = new Paint();
    private boolean                  mHwAlignBottom           = false;
    private int                      mStart;
	
	public V1DataConverter(Context context) {
		mContext = context;
		mBookCase = BookCase.getInstance(context);
		mReader.setOnReadListener(this);
		mHwPaint.setStyle(Paint.Style.STROKE);
		DEFAULT_CANVAS_WIDTH = (int)context.getResources().getDimension(R.dimen.default_canvas_width);//Begin Siupo
		DEFAULT_CANVAS_HEIGHT = (int)context.getResources().getDimension(R.dimen.default_canvas_height);//Begin Siupo
	}
	
	@Override
	public boolean preprocessBooks() {		
		File bookListFile = new File(V1DataFormat.DATA_DIR, 
				V1DataFormat.BOOKLIST_FILE_NAME);
		if (bookListFile.exists()) {
			DataInputStream bookListDis = null;
			try {
				bookListDis = new DataInputStream(
						new FileInputStream(bookListFile));
			} catch (FileNotFoundException fnfEx) {
				bookListDis = null;
				fnfEx.printStackTrace();
			}
			if (bookListDis != null) {
				try {
					mReader.readBookList(bookListDis);
				} catch (IOException ioEx) {
					ioEx.printStackTrace();
				} finally {
					try {
						bookListDis.close();
					} catch (IOException ioEx) {
						ioEx.printStackTrace();
					}
				}
			}
			bookListFile.delete();
		}
		
		return true;
	}
	
	@Override
	public boolean preprocessPages() {		
		ArrayList<Long> bookList = new ArrayList<Long>(MetaData.V1BookIdList);
		for (long bookId : bookList) {
			mNoteBook = mBookCase.getNoteBook(bookId);
			if (mNoteBook != null) {
				File pageListFile = new File(MetaData.DATA_DIR + bookId, 
						V1DataFormat.PAGELIST_FILE_NAME);
				if (pageListFile.exists()) {
					DataInputStream pageListDis = null;
					try {
						pageListDis = new DataInputStream(
								new FileInputStream(pageListFile));
					} catch (FileNotFoundException fnfEx) {
						pageListDis = null;
						fnfEx.printStackTrace();
					}
					if (pageListDis != null) {
						try {
							mReader.readPageList(pageListDis);
						} catch (IOException ioEx) {
							ioEx.printStackTrace();
						} finally {
							try {
								pageListDis.close();
							} catch (IOException ioEx) {
								ioEx.printStackTrace();
							}
						}
					}
					pageListFile.delete();
				}
				
				for (Observer observer : mObservers) {
					observer.notify(mNoteBook.getCreatedTime(), -1);
				}
			}

			MetaData.V1BookIdList.remove(bookId);
		}
		
		File srcAttachDir = new File(V1DataFormat.DATA_DIR, "attach");
		if (srcAttachDir.exists()) {
			File dstAttachDir = new File(MetaData.DATA_DIR, "attach");
			srcAttachDir.renameTo(dstAttachDir);
		}
		
		return true;
	}
	
	public ArrayList<NoteItem> getPageItems() {
		return mNoteItemList;
	}
	
	public DoodleItem getPageDoodleItem() {
		return mDoodleItem;
	}
	
	//BEGIN: RICHARD
	//if flag is false not load doodleitem
	public boolean load(NotePage notePage,Boolean flag) {		
		mNoteItemList.clear();
		mDoodleItem = null;
		
		mCurrentPathList.clear();
		mCurrentEraseList.clear();
		mDrawInfoList.clear();
		
		mNotePage = notePage;
		mNoteBook = mBookCase.getNoteBook(mNotePage.getOwnerBookId());
		
		File file = new File(MetaData.DATA_DIR + mNotePage.getOwnerBookId(), "paintbook");
		mIsPaintBook = file.exists();
		
		File itemsFile = new File(mNotePage.getFilePath(), 
				MetaData.NOTE_ITEM_PREFIX);
		if (itemsFile.exists()) {		
			DataInputStream itemsDis = null;
			try {
				itemsDis = new DataInputStream(
						new FileInputStream(itemsFile));
			} catch (FileNotFoundException fnfEx) {
				itemsDis = null;
				fnfEx.printStackTrace();
			}
			if (itemsDis != null) {
				try {
					onReadString(V1DataFormat.FILE_PAGE_CONTENT_BEGIN, itemsFile.getAbsolutePath());
					mReader.readPageItems(itemsDis);
					onReadString(V1DataFormat.FILE_PAGE_CONTENT_END, itemsFile.getAbsolutePath());
				} catch (IOException ioEx) {
					ioEx.printStackTrace();
				} finally {
					try {
						itemsDis.close();
					} catch (IOException ioEx) {
						ioEx.printStackTrace();
					}
				}
			}
		}
		if(flag)
		{
			File doodlesFile = new File(mNotePage.getFilePath(), 
					MetaData.DOODLE_ITEM_PREFIX);
			if (doodlesFile.exists()) {
				DataInputStream doodlesDis = null;
				try {
					doodlesDis = new DataInputStream(
							new FileInputStream(doodlesFile));
				} catch (FileNotFoundException fnfEx) {
					doodlesDis = null;
					fnfEx.printStackTrace();
				}
				if (doodlesDis != null) {
					try {
						onReadString(V1DataFormat.FILE_PAGE_LAYER_BEGIN, itemsFile.getAbsolutePath());
						mReader.readPageDoodles(doodlesDis);
						onReadString(V1DataFormat.FILE_PAGE_LAYER_END, itemsFile.getAbsolutePath());
					} catch (IOException ioEx) {
						ioEx.printStackTrace();
					} finally {
						try {
							doodlesDis.close();
						} catch (IOException ioEx) {
							ioEx.printStackTrace();
						}
					}
				}
			}
		}
		
		return true;
	}
	//END: RICHARD
	
	@Override
	public boolean load(NotePage notePage) {		
		mNoteItemList.clear();
		mDoodleItem = null;
		
		mCurrentPathList.clear();
		mCurrentEraseList.clear();
		mDrawInfoList.clear();
		
		mNotePage = notePage;
		mNoteBook = mBookCase.getNoteBook(mNotePage.getOwnerBookId());
		
		File file = new File(MetaData.DATA_DIR + mNotePage.getOwnerBookId(), "paintbook");
		mIsPaintBook = file.exists();
		
		File itemsFile = new File(mNotePage.getFilePath(), 
				MetaData.NOTE_ITEM_PREFIX);
		if (itemsFile.exists()) {		
			DataInputStream itemsDis = null;
			try {
				itemsDis = new DataInputStream(
						new FileInputStream(itemsFile));
			} catch (FileNotFoundException fnfEx) {
				itemsDis = null;
				fnfEx.printStackTrace();
			}
			if (itemsDis != null) {
				try {
					onReadString(V1DataFormat.FILE_PAGE_CONTENT_BEGIN, itemsFile.getAbsolutePath());
					mReader.readPageItems(itemsDis);
					onReadString(V1DataFormat.FILE_PAGE_CONTENT_END, itemsFile.getAbsolutePath());
				} catch (IOException ioEx) {
					ioEx.printStackTrace();
				} finally {
					try {
						itemsDis.close();
					} catch (IOException ioEx) {
						ioEx.printStackTrace();
					}
				}
			}
		}
		
		File doodlesFile = new File(mNotePage.getFilePath(), 
				MetaData.DOODLE_ITEM_PREFIX);
		if (doodlesFile.exists()) {
			DataInputStream doodlesDis = null;
			try {
				doodlesDis = new DataInputStream(
						new FileInputStream(doodlesFile));
			} catch (FileNotFoundException fnfEx) {
				doodlesDis = null;
				fnfEx.printStackTrace();
			}
			if (doodlesDis != null) {
				try {
					onReadString(V1DataFormat.FILE_PAGE_LAYER_BEGIN, itemsFile.getAbsolutePath());
					mReader.readPageDoodles(doodlesDis);
					onReadString(V1DataFormat.FILE_PAGE_LAYER_END, itemsFile.getAbsolutePath());
				} catch (IOException ioEx) {
					ioEx.printStackTrace();
				} finally {
					try {
						doodlesDis.close();
					} catch (IOException ioEx) {
						ioEx.printStackTrace();
					}
				}
			}
		}
		
		return true;
	}
	
	@Override
	public boolean convert() {
		return true;
	}

	@Override
	public void onReadByte(int id, byte value) {
		onReadShort(id, value);
	}

	@Override
	public void onReadUnsignedByte(int id, int value) {
		onReadInt(id, value);
	}

	@Override
	public void onReadShort(int id, short value) {
		handleItem(new AsusFormatReader.Item(id, value));
	}

	@Override
	public void onReadUnsignedShort(int id, int value) {
		onReadInt(id, value);
	}

	@Override
	public void onReadInt(int id, int value) {
		handleItem(new AsusFormatReader.Item(id, value));
	}

	@Override
	public void onReadFloat(int id, float value) {
		handleItem(new AsusFormatReader.Item(id, value));
	}

	@Override
	public void onReadLong(int id, long value) {
		handleItem(new AsusFormatReader.Item(id, value));
	}

	@Override
	public void onReadBoolean(int id, boolean value) {
		handleItem(new AsusFormatReader.Item(id, value));
	}

	@Override
	public void onReadShortArray(int id, short[] data) {
		handleItem(new AsusFormatReader.Item(id, data));
	}

	@Override
	public void onReadIntArray(int id, int[] data) {
		handleItem(new AsusFormatReader.Item(id, data));
	}

	@Override
	public void onReadFloatArray(int id, float[] data) {
		handleItem(new AsusFormatReader.Item(id, data));
	}

	@Override
	public void onReadLongArray(int id, long[] data) {
		handleItem(new AsusFormatReader.Item(id, data));
	}

	@Override
	public void onReadString(int id, String value) {
		handleItem(new AsusFormatReader.Item(id, value));
	}

	@Override
	public void onReadByteArray(int id, byte[] data) {
		handleItem(new AsusFormatReader.Item(id, data));
	}
	
	private void handleItem(AsusFormatReader.Item item) {
		int id = item.getId();
		if ((V1DataFormat.FILE_BEGIN <= id)
				&& V1DataFormat.FILE_END >= id) {
			fileChange(item);
			switch (id) {
			case V1DataFormat.FILE_NBS_BEGIN:
			case V1DataFormat.FILE_NBS_END:
				loadBooks(item);
				break;
			case V1DataFormat.FILE_INDEX_BEGIN:
			case V1DataFormat.FILE_INDEX_END:
			case V1DataFormat.FILE_MF_BEGIN:
			case V1DataFormat.FILE_MF_END:
				loadBook(item);
				break;
			case V1DataFormat.FILE_PAGE_CONTENT_BEGIN:
			case V1DataFormat.FILE_PAGE_CONTENT_END:
				loadItems(item);
				loadDoodles(item);
				loadAttachment(item);
				break;
			case V1DataFormat.FILE_PAGE_THUMBNAIL_BEGIN:
			case V1DataFormat.FILE_PAGE_THUMBNAIL_END:
				//loadThumbnail(item);
				break;
			case V1DataFormat.FILE_PAGE_LAYER_BEGIN:
			case V1DataFormat.FILE_PAGE_LAYER_END:
				loadDoodles(item);
				break;
			case V1DataFormat.FILE_EXTERNAL_FILE_BEGIN:
			case V1DataFormat.FILE_EXTERNAL_FILE_END:
				loadAttachment(item);
				break;

			}
			return;
		}
		if ((V1DataFormat.NBS_BEGIN <= id)
				&& (V1DataFormat.NBS_END >= id)) {
			loadBooks(item);
			return;
		}
		if ((V1DataFormat.INDEX_BEGIN <= id)
				&& V1DataFormat.INDEX_END >= id) {
			loadBook(item);
			return;
		}
		if ((V1DataFormat.MF_BEGIN <= id)
				&& V1DataFormat.MF_END >= id) {
			loadBook(item);
			return;
		}
		if ((V1DataFormat.PAGECOTENT_BEGIN <= id)
				&& V1DataFormat.PAGECOTENT_END >= id) {
			switch (id) {
			case V1DataFormat.PAGECOTENT_STRING:
				loadItems(item);
				break;
			case V1DataFormat.PAGECOTENT_HW_TYPE:
			case V1DataFormat.PAGECOTENT_HW_SCALE:
			case V1DataFormat.PAGECOTENT_HW_FONT_COLOR:
			case V1DataFormat.PAGECOTENT_HW_MODE:
			case V1DataFormat.PAGECOTENT_HW_HEIGHT_SCALE:
			case V1DataFormat.PAGECOTENT_HW_POS_IN_STRING:
			case V1DataFormat.PAGECOTENT_HW_BYTE_NUM:
			case V1DataFormat.PAGECOTENT_HW_PATH_XY:
				loadDoodles(item);
				break;
			case V1DataFormat.PAGECOTENT_ICON_POS_IN_STRING:
			case V1DataFormat.PAGECOTENT_ICON_ID:
			case V1DataFormat.PAGECOTENT_TIMESTAMP_POS_IN_STRING:
			case V1DataFormat.PAGECOTENT_TIMESTAMP_VALUE:
			case V1DataFormat.PAGECOTENT_FONTCOLOR_COLOR:
			case V1DataFormat.PAGECOTENT_FONTCOLOR_POS_BEGIN:
			case V1DataFormat.PAGECOTENT_FONTCOLOR_POS_END:
			case V1DataFormat.PAGECOTENT_FONTSTYLE_STYLE:
			case V1DataFormat.PAGECOTENT_FONTSTYLE_POS_BEGIN:
			case V1DataFormat.PAGECOTENT_FONTSTYLE_POS_END:
				loadItems(item);
				break;
			case V1DataFormat.PAGECOTENT_ATTACHMENT_POS_BEGIN:
			case V1DataFormat.PAGECOTENT_ATTACHMENT_POS_END:
			case V1DataFormat.PAGECOTENT_ATTACHMENT_PATH_BYTE_NUM:
			case V1DataFormat.PAGECOTENT_ATTACHMENT_PATH:
				loadAttachment(item);
				loadItems(item);
				break;
			}
			return;
		}
		if ((V1DataFormat.PAGETHUMBNAIL_BEGIN <= id)
				&& V1DataFormat.PAGETHUMBNAIL_END >= id) {
			//loadThumbnail(item);
			return;
		}
		if ((V1DataFormat.PAGELAYER_BEGIN <= id)
				&& V1DataFormat.PAGELAYER_END >= id) {
			loadDoodles(item);
			return;
		}
		if ((V1DataFormat.EXTERNALFILE_BEGIN <= id)
				&& V1DataFormat.EXTERNALFILE_END >= id) {
			loadAttachment(item);
			return;
		}
	}
	
	private void fileChange(AsusFormatReader.Item item) {
		int id = item.getId();
		switch (id) {
		case V1DataFormat.FILE_INDEX_BEGIN:
			break;
		case V1DataFormat.FILE_INDEX_END:
			break;
		}
	}
	
	private void loadBooks(AsusFormatReader.Item item) {
		int id = item.getId();
		switch (id) {
		case V1DataFormat.NBS_BOOK_VERSION:
			mIsBookDirExists = false;
			mIsPaintBook = false;
			mNoteBook = new NoteBook(mContext);
			mNoteBook.setVersion(1);
			mNoteBook.setGridType(MetaData.BOOK_GRID_LINE);
            mNoteBook.setPageSize(MetaData.PAGE_SIZE_PAD);
            PickerUtility.forceMkDir(MetaData.DATA_DIR + mNoteBook.getCreatedTime());
			break;
		case V1DataFormat.NBS_BOOK_FONT_SIZE:
			mNoteBook.setFontSizeNoUpdateDB(item.getIntValue());
			break;
		case V1DataFormat.NBS_BOOK_BAK_COLOR:
			mNoteBook.setBookColor(item.getIntValue());
			break;
		case V1DataFormat.NBS_BOOK_ID:
			mBookIndex = item.getIntValue();
			File oldBookDir = new File(V1DataFormat.DATA_DIR, 
					Integer.toString(mBookIndex));
			mIsBookDirExists = oldBookDir.exists();
			if (mIsBookDirExists) {
				File newBookDir = new File(MetaData.DATA_DIR, 
						Long.toString(mNoteBook.getCreatedTime()));
				oldBookDir.renameTo(newBookDir);
				
				File bookInfoFile = new File(newBookDir, 
						V1DataFormat.BOOKINFO_FILE_NAME);
				if (bookInfoFile.exists()) {
					DataInputStream bookInfoDis = null;
					try {
						bookInfoDis = new DataInputStream(
								new FileInputStream(bookInfoFile));
					} catch (FileNotFoundException fnfEx) {
						bookInfoDis = null;
						fnfEx.printStackTrace();
					}
					if (bookInfoDis != null) {
						try {
							mReader.readBookInfo(bookInfoDis);
						} catch (IOException ioEx) {
							ioEx.printStackTrace();
						} finally {
							try {
								bookInfoDis.close();
							} catch (IOException ioEx) {
								ioEx.printStackTrace();
							}
						}
					}
					bookInfoFile.delete();
				}
			}
			break;
		case V1DataFormat.NBS_BOOK_TYPE:
			if (mIsBookDirExists) {
				short type = item.getShortValue();
				if ((type & 0x01) != 0) {
					mIsPaintBook = true;
				}
				if ((type & 0x02) != 0) {
					mNoteBook.setLockedNoUpdateDB(true);
				}
			}
			break;
		case V1DataFormat.NBS_BOOK_NAME:
			if (mIsBookDirExists) {
				String name = "";
				try {
					name = new String(item.getByteArray(), DataFormat.UTF8_CHARSET_NAME);
				} catch (NullPointerException npEx) {
					name = "untitled";
				} catch (UnsupportedEncodingException ueEx) {
					name = "untitled";
				}
				mNoteBook.setTitleNoUpdateDB(name);
				mBookCase.addNewBookFromWebPage(mNoteBook);
				MetaData.V1BookIdList.add(mNoteBook.getCreatedTime());
				if (mIsPaintBook) {
					File file = new File(MetaData.DATA_DIR + mNoteBook.getCreatedTime(), "paintbook");
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			break;
		}
	}
	
	private void loadBook(AsusFormatReader.Item item) {
		int id = item.getId();
		switch (id) {
		case V1DataFormat.MF_DEFAULT_FONT_SIZE:
			int value = item.getIntValue();
			int size = MetaData.BOOK_FONT_NORMAL;
            if (40 == value) {
                size = MetaData.BOOK_FONT_BIG;
            }
            else if (35 == value) {
                size = MetaData.BOOK_FONT_NORMAL;
            }
            else if (29 == value) {
                size = MetaData.BOOK_FONT_SMALL;
            }
			mNoteBook.setFontSizeNoUpdateDB(size);
			break;
		case V1DataFormat.MF_BACKGROUND_COLOR:
			mNoteBook.setBookColor(item.getIntValue());
			break;
		case V1DataFormat.MF_PAGE_TYPE:
			mIsPaintBook = (item.getShortValue() == 1);
			break;
		case V1DataFormat.INDEX_PAGE_VERSION:
			mIsPageFileExists = false;
			mNotePage = new NotePage(mContext, mNoteBook.getCreatedTime());
			mNotePage.setVersion(1);
			mNotePage.setPageSize(MetaData.PAGE_SIZE_PAD);
			PickerUtility.forceMkDir(mNotePage.getFilePath());
			break;
		case V1DataFormat.INDEX_PAGE_ID:
			String oldIdName = Long.toString(item.getLongValue());
			String newIdName = Long.toString(mNotePage.getCreatedTime());
			File bookDir = new File(MetaData.DATA_DIR, 
					Long.toString(mNoteBook.getCreatedTime()));
			File newPageDir = new File(bookDir, newIdName);
			File newItemsFile = new File(newPageDir, MetaData.NOTE_ITEM_PREFIX);
			File newDoodleFile = new File(newPageDir, MetaData.DOODLE_ITEM_PREFIX);
			File newThumbFile = new File(newPageDir, MetaData.THUMBNAIL_PREFIX);
			
			for (File file : bookDir.listFiles()) {
				String fileName = file.getName();
				if (file.isDirectory()) {
					if (fileName.equals(oldIdName)) {
						mIsPageFileExists = true;
						for (File subFile : file.listFiles()) {
							subFile.renameTo(new File(newPageDir, subFile.getName()));
						}
						file.delete();
					}
				} else {
					if (fileName.equals(oldIdName + "_00")) {
						mIsPageFileExists = true;
						file.renameTo(newItemsFile);
					} else if (fileName.equals(oldIdName + "_01")) {
						mIsPageFileExists = true;
						file.renameTo(newThumbFile);
					} else if (fileName.equals(oldIdName + "_05")) {
						mIsPageFileExists = true;
						file.renameTo(newDoodleFile);
					} else if (fileName.equals(oldIdName + "_04")) {
						mIsPageFileExists = true;
						file.delete();
					}
				}
			}
			break;
		case V1DataFormat.INDEX_PAGE_IS_BOOKMARK:
			if (mIsPageFileExists) {
				mNotePage.setBookmarkNoUpdateDB((item.getIntValue() == 0) ? false : true);
				mNoteBook.addPage(mNotePage);
			}
			break;
		}
	}
	
	private void loadItems(AsusFormatReader.Item item) {
		int id = item.getId();

		// String Item
		if (id == V1DataFormat.PAGECOTENT_STRING) {
			mStringItem = new NoteStringItem(item.getStringValue());
			mNoteItemList.add(0, mStringItem);
		}

		// Foreground color items
		if (id == V1DataFormat.PAGECOTENT_FONTCOLOR_COLOR) {
			mColorItem = new NoteForegroundColorItem(item.getIntValue());
		}
		if (id == V1DataFormat.PAGECOTENT_FONTCOLOR_POS_BEGIN
				&& mColorItem != null) {
			mColorItem.setStart(item.getIntValue());
		}
		if (id == V1DataFormat.PAGECOTENT_FONTCOLOR_POS_END
				&& mColorItem != null) {
			mColorItem.setEnd(item.getIntValue());
			for (int i = mColorItem.getStart(); i < mColorItem.getEnd(); i++) {
				NoteForegroundColorItem colorItem = new NoteForegroundColorItem(
						mColorItem.getForegroundColor());
				colorItem.setStart(i);
				colorItem.setEnd(i + 1);
				mNoteItemList.add(colorItem);
			}
			mColorItem = null;
		}

		// Test style items
		if (id == V1DataFormat.PAGECOTENT_FONTSTYLE_STYLE) {
			mStyleItem = new NoteTextStyleItem(item.getIntValue());
		}
		if (id == V1DataFormat.PAGECOTENT_FONTSTYLE_POS_BEGIN
				&& mStyleItem != null) {
			mStyleItem.setStart(item.getIntValue());
		}
		if (id == V1DataFormat.PAGECOTENT_FONTSTYLE_POS_END
				&& mStyleItem != null) {
			mStyleItem.setEnd(item.getIntValue());
			for (int i = mStyleItem.getStart(); i < mStyleItem.getEnd(); i++) {
				NoteTextStyleItem styleItem = new NoteTextStyleItem(
						mStyleItem.getStyle());
				styleItem.setStart(i);
				styleItem.setEnd(i + 1);
				mNoteItemList.add(styleItem);
			}
			mStyleItem = null;
		}

		// Send intent item
		if (id == V1DataFormat.PAGECOTENT_ATTACHMENT_POS_BEGIN) {
			mSendIntentStartAt = item.getIntValue();
		}
		if (id == V1DataFormat.PAGECOTENT_ATTACHMENT_POS_END) {
			mSendIntentEndAt = item.getIntValue();
		}
		if (id == V1DataFormat.PAGECOTENT_ATTACHMENT_PATH) {
			String filePath = item.getStringValue();
			int dotPos = filePath.lastIndexOf(".");
			String extension = filePath
					.substring(dotPos + 1, filePath.length());
			String intentType = null;
			if (extension.equals(NoteSendIntentItem.FILENAME_EXTENSION_VIDEO)) {
				intentType = NoteSendIntentItem.INTENT_TYPE_VIDEO;
			} else if (extension.equals(NoteSendIntentItem.FILENAME_EXTENSION_VIDEO_OLD)) {
				intentType = NoteSendIntentItem.INTENT_TYPE_VIDEO_OLD;
			}
			else if (extension
					.equals(NoteSendIntentItem.FILENAME_EXTENSION_VOICE)) {
				intentType = NoteSendIntentItem.INTENT_TYPE_VOICE;
			} else {
				Log.w(TAG, "Do not support to open this file : " + filePath);
				return;
			}
			try {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse("/" + filePath), intentType);

				mIntentItem = new NoteSendIntentItem(intent);
				mIntentItem.setStart(mSendIntentStartAt);
				mIntentItem.setEnd(mSendIntentEndAt);
				mIntentItem.prepareIntent(mNotePage);
				
				String fullPath = item.getStringValue() + "//" + mIntentItem.getFileName();
				
				boolean isVideo = false;
				if (mIntentItem.getIntent() != null) {
					isVideo = mIntentItem.getIntent().getType().equals(NoteSendIntentItem.INTENT_TYPE_VIDEO) ? true : false;   
				}
				else {
					isVideo = true;
				}
				
				//Begin Dave.To modify voice/video attacher UI. 

				AttacherTool tool = new AttacherTool();
				String imageItemInfo = tool.getFileNameNoEx(mIntentItem.getFileName()) + tool.getElapsedTime(fullPath);
				NoteImageItem imageItem = new NoteImageItem(mContext, isVideo, getImageSpanHeight(
						mNoteBook,
						false,
						mNoteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE),imageItemInfo);
				//End Dave.
				imageItem.setStart(mIntentItem.getStart());
				imageItem.setEnd(mIntentItem.getEnd());
				
				mNoteItemList.add(mIntentItem);
				mNoteItemList.add(imageItem);
				mIntentItem = null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Image item
		if (id == V1DataFormat.PAGECOTENT_ICON_POS_IN_STRING) {

		}
		if (id == V1DataFormat.PAGECOTENT_ICON_ID) {

		}

		// Timestamp item
		if (id == V1DataFormat.PAGECOTENT_TIMESTAMP_POS_IN_STRING) {
			mTimestampItem = new NoteTimestampItem(mNotePage.getCreatedTime(),getImageSpanHeight(
					mNoteBook,
					false,
					mNoteBook.getPageSize() == MetaData.PAGE_SIZE_PHONE));
			mTimestampItem.setStart(item.getIntValue());
			mTimestampItem.setEnd(item.getIntValue() + 1);
		}
		if (id == V1DataFormat.PAGECOTENT_TIMESTAMP_VALUE
				&& mTimestampItem != null) {
			mTimestampItem.setTimestamp(item.getLongValue());
			mNoteItemList.add(mTimestampItem);
			mTimestampItem = null;
		}
	}
	
	private int getImageSpanHeight(NoteBook noteBook, boolean isFull,
			boolean isSmallScreen) {
		int textSize = MetaData.BOOK_FONT_NORMAL;
		textSize = noteBook.getFontSize();
		
		//Begin: Siupo
		float fontSize = 0;
		fontSize = NotePageValue.getFontSize(mContext, textSize, isSmallScreen);
		//End Siupo
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
	
	private void loadDoodles(AsusFormatReader.Item item) {
        SerDrawInfo savedInfo = null;
        switch (item.getId()) {
        case V1DataFormat.FILE_PAGE_LAYER_BEGIN:
            mDoodleItem = new DoodleItem(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT);
            break;
        case V1DataFormat.FILE_PAGE_LAYER_END:
            savedInfo = getDrawInfo();
            if (savedInfo != null) {
                mDrawInfoList.addFirst(savedInfo);
            }
            mDoodleItem.addInfos(mDrawInfoList);
            resetLayerInformation();
            mDrawInfoList.clear();
            mCurrentLayerVersion = -1;
            break;
        case V1DataFormat.PAGELAYER_LAYER_VERSION:
            savedInfo = getDrawInfo();
            if (savedInfo != null) {
                mDrawInfoList.addFirst(savedInfo);
            }
            resetLayerInformation();
            mCurrentLayerVersion = item.getShortValue();
            break;
        case V1DataFormat.PAGELAYER_LAYER_RECT:
            mCurrentLayerRect = getRect(item.getLongValue());
            break;
        case V1DataFormat.PAGELAYER_LAYER_ROTATE_DEGREE:
            mCurrentRotateDegree = item.getShortValue();
            break;
        case V1DataFormat.PAGELAYER_LAYER_FILE_ID:
            long fileId = item.getLongValue();
            if (fileId != 0) {
                mCurrentFileName = Long.toString(fileId);
            }
            break;
        case V1DataFormat.PAGELAYER_LAYER_OP_DESC_VERSION0:
        case V1DataFormat.PAGELAYER_LAYER_OP_DESC_VERSION1:
        case V1DataFormat.PAGELAYER_LAYER_OP_DESC_RECT:
        case V1DataFormat.PAGELAYER_LAYER_OP_DESC_BRUSH_STYLE:
        case V1DataFormat.PAGELAYER_LAYER_OP_DESC_VERSION3:
        case V1DataFormat.PAGELAYER_LAYER_OP_DESC_REAL_WIDTH:
        case V1DataFormat.PAGELAYER_LAYER_OP_DESC_REAL_HEIGHT:
        case V1DataFormat.PAGELAYER_LAYER_OP_NUM:
        case V1DataFormat.PAGELAYER_LAYER_OP_PT_VERSION:
        case V1DataFormat.PAGELAYER_LAYER_OP_PT_NUM:
            break;
        case V1DataFormat.PAGELAYER_LAYER_OP_PT_XY:
            mCurrentPoints = item.getFloatArray();
            break;
        case V1DataFormat.PAGELAYER_LAYER_OP_PT_SEED:
            break;
        case V1DataFormat.PAGELAYER_LAYER_OP_VERSION0:
            SerPointInfo pointInfo = getPointInfo();
            if (pointInfo != null) {
                mCurrentPathList.add(pointInfo);
            }
            break;
        case V1DataFormat.PAGELAYER_LAYER_OP_RECT:
            mCurrentOpRect.union(getRect(item.getLongValue()));
            break;
        case V1DataFormat.PAGELAYER_LAYER_OP_STYLE:
            mCurrentPaintBrushStyle = item.getShortValue();
            break;
        case V1DataFormat.PAGELAYER_LAYER_OP_VERSION2:
        case V1DataFormat.PAGELAYER_LAYER_OP_REAL_WIDTH:
        case V1DataFormat.PAGELAYER_LAYER_OP_REAL_HEIGHT:
            break;
        case V1DataFormat.PAGELAYER_LAYER_OP_PEN_SHIFT_XY:
            int[] shifts = item.getIntArray();
            mCurrentLayerTransMatrix.postTranslate(-shifts[0], -shifts[1]);
            break;
        case V1DataFormat.PAGELAYER_LAYER_OP_PEN_RT_CENTER_XY:
            int[] rotates = item.getIntArray();
            mCurrentLayerTransMatrix.postRotate(-rotates[2] * 90, rotates[0], rotates[1]);
            break;
        case V1DataFormat.PAGELAYER_LAYER_OP_PEN_SCALE_CENTER_XY:
            float[] scales = item.getFloatArray();
            mCurrentLayerTransMatrix.postScale(1 / scales[2], 1 / scales[2], scales[0], scales[1]);
            break;
        case V1DataFormat.PAGELAYER_LAYER_OP_PEN_BRUSH_COLOR:
            mCurrentPenBrushColor = item.getIntValue();
            break;
        case V1DataFormat.PAGELAYER_LAYER_OP_PEN_BRUSH_WIDTH:
            mCurrentPenBrushWidth = item.getFloatValue();
            break;
        case V1DataFormat.PAGECOTENT_HW_TYPE:
        case V1DataFormat.PAGECOTENT_HW_SCALE:
            break;
        case V1DataFormat.PAGECOTENT_HW_FONT_COLOR:
            mHwPaint.setColor(item.getIntValue());
            break;
        case V1DataFormat.PAGECOTENT_HW_MODE:
            short mode = item.getShortValue();
            if ((mode & HW_MASK_BLOD) == HW_BLOD) {
                mHwPaint.setStrokeWidth(MetaData.SCRIBBLE_PAINT_WIDTHS_BOLD);
            }
            else {
                mHwPaint.setStrokeWidth(MetaData.SCRIBBLE_PAINT_WIDTHS_NORMAL);
            }
            if ((mode & HW_MASK_ALIGNMENT) == HW_ALIGNMENT_BOTTOM) {
                mHwAlignBottom = true;
            }
            else {
                mHwAlignBottom = false;
            }
            break;
        case V1DataFormat.PAGECOTENT_HW_HEIGHT_SCALE:
            break;
        case V1DataFormat.PAGECOTENT_HW_POS_IN_STRING:
            mStart = item.getIntValue();
            break;
        case V1DataFormat.PAGECOTENT_HW_BYTE_NUM:
            break;
        case V1DataFormat.PAGECOTENT_HW_PATH_XY:
            Path path = new Path();
            LinkedList<PathInfo> pathInfoList = new LinkedList<PathInfo>();
            LinkedList<Short> pointList = new LinkedList<Short>();
            short[] pointArray = item.getShortArray();
            int size = pointArray.length;
            int index = 0;
            while (index < size) {
                short x = pointArray[index++];
                short y = pointArray[index++];
                if (x == HW_DOWN_DETECT_VALUE) {
                    if (pointList.size() > 0) {
                        short[] content = toShortArray(pointList);
                        pathInfoList.add(new PathInfo(content));
                        path.reset();
                        pointList.clear();
                    }
                }
                else {
                    pointList.add(x);
                    pointList.add(y);
                }
            }
            if (pointList.size() > 0) {
                short[] content = toShortArray(pointList);
                pathInfoList.add(new PathInfo(content));
                path.reset();
                pointList.clear();
            }
            NoteHandWriteItem hwItem;
            if (mHwAlignBottom) {
                hwItem = new NoteHandWriteItem(pathInfoList, mHwPaint);
            }
            else {
                hwItem = new NoteHandWriteBaselineItem(pathInfoList, mHwPaint);
            }
            hwItem.setStart(mStart);
            hwItem.setEnd(mStart + 1);
            mNoteItemList.add(hwItem);
            break;
        }
	}
	
	private void resetLayerInformation() {
        mCurrentLayerRect = null;
        mCurrentRotateDegree = 0;
        mCurrentFileName = null;
        mCurrentOpRect = new Rect();
        mCurrentLayerTransMatrix.reset();
    }
	
	private short[] toShortArray(LinkedList<Short> shortList) {
        if (shortList == null) {
            return null;
        }

        short[] shortArray = new short[shortList.size()];
        int index = 0;
        for (Short point : shortList) {
            shortArray[index++] = point;
        }
        return shortArray;
    }

    private Rect getRect(long rectValues) {
        short left = 0, top = 0, right = 0, bottom = 0;
        ShortBuffer shortBuffer;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(8);
        byteBuffer.putLong(rectValues);
        byteBuffer.flip();
        shortBuffer = byteBuffer.asShortBuffer();
        left = shortBuffer.get();
        top = shortBuffer.get();
        right = shortBuffer.get();
        bottom = shortBuffer.get();
        return new Rect(left, top, right, bottom);
    }
    
    private SerPointInfo getPointInfo() {

        if (mCurrentPoints == null) {
            return null;
        }

        int toolCode;
        switch (mCurrentPaintBrushStyle) {
        case LAYER_OP_STYLE_NORMAL:
        case LAYER_OP_STYLE_SCRIBBLE:
            toolCode = DrawTool.NORMAL_TOOL;
            break;
        case LAYER_OP_STYLE_NEON:
            toolCode = DrawTool.NEON_TOOL;
            break;
        case LAYER_OP_STYLE_SKETCH:
            toolCode = DrawTool.SKETCH_TOOL;
            break;
        case LAYER_OP_STYLE_ERASER:
            toolCode = DrawTool.ERASE_TOOL;
            break;
        default:
            toolCode = DrawTool.NORMAL_TOOL;
        }

        SerPointInfo pointInfo = null;
        if (mCurrentPoints != null) {
            if (toolCode == DrawTool.ERASE_TOOL) {
                SerEraseInfo eraseInfo = new SerEraseInfo();
                int pointCount = mCurrentPoints.length / 2;
                int lineCount = pointCount - 1;
                float[] points = new float[lineCount * 2 * 2];
                int index = 0;
                for (int i = 0 ; i < lineCount ; i++) {
                    int startPointIndex = i;
                    int endPointIndex = i + 1;
                    points[index++] = mCurrentPoints[2 * startPointIndex];
                    points[index++] = mCurrentPoints[2 * startPointIndex + 1];
                    points[index++] = mCurrentPoints[2 * endPointIndex];
                    points[index++] = mCurrentPoints[2 * endPointIndex + 1];
                }
                
                eraseInfo.setPoints(points);
                eraseInfo.setStrokeWidth(mCurrentPenBrushWidth);
                mCurrentEraseList.add(eraseInfo);
            }
            else {
                pointInfo = new SerPointInfo();
                pointInfo.setColor(mCurrentPenBrushColor);
                pointInfo.setStrokeWidth(mCurrentPenBrushWidth);
                pointInfo.setPoints(mCurrentPoints);
                pointInfo.setPaintTool(toolCode);
            }
        }

        initPathInfo();
        return pointInfo;
    }
    
    private void initPathInfo() {
        mCurrentPenBrushColor = Color.BLACK;
        mCurrentPenBrushWidth = MetaData.DOODLE_DEFAULT_PAINT_WIDTH;
        mCurrentPoints = null;
    }

    private SerDrawInfo getDrawInfo() {
        SerDrawInfo drawInfo = null;
        switch (mCurrentLayerVersion) {
        case 2:
            drawInfo = getFromFile();
            break;
        case 3:
        case 4:
            drawInfo = getFromFileAndPaths();
            break;
        }
        return drawInfo;
    }

    private SerGraphicInfo getFromFile() {
        if (mCurrentFileName == null) {
            return null;
        }

        SerGraphicInfo graphicInfo = new SerGraphicInfo();
        Matrix transMatrix = new Matrix();
        String destDirPath = mNotePage.getFilePath();

        File file = new File(destDirPath + "/" + mCurrentFileName);
        if (!file.exists()) {
            return null;
        }

        graphicInfo.setFileName(mCurrentFileName);
        if (mCurrentLayerRect != null) {
            float scaleX;
            float scaleY;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(destDirPath + "/" + mCurrentFileName, options);
            scaleX = (float) mCurrentLayerRect.width() / (float) options.outWidth;
            scaleY = (float) mCurrentLayerRect.height() / (float) options.outHeight;
            transMatrix.postScale(scaleX, scaleY);
            transMatrix.postTranslate(mCurrentLayerRect.left, mCurrentLayerRect.top);
            if (mCurrentLayerRect.isEmpty()) {
                transMatrix.postRotate(mCurrentRotateDegree * 90);
            }
            else {
                transMatrix.postRotate(mCurrentRotateDegree * 90, mCurrentLayerRect.centerX(), mCurrentLayerRect.centerY());
            }
        }

        transMatrix.postConcat(mCurrentLayerTransMatrix);

        if (mIsPaintBook) {
            transMatrix.postScale(PAINT_BOOK_SCALE, PAINT_BOOK_SCALE);
        }
        else {
            transMatrix.postScale(NOTE_BOOK_SCALE, NOTE_BOOK_SCALE);
        }

        graphicInfo.setTransform(transMatrix);

        return graphicInfo;
    }

    private SerAnnotationInfo getFromFileAndPaths() {    	
        // Get lastest point info
        SerPointInfo pointInfo = getPointInfo();
        if (pointInfo != null) {
            mCurrentPathList.add(pointInfo);
        }

        SerGraphicInfo graphicInfo = getFromFile();
        LinkedHashMap<Integer, DrawInfo> drawInfos = new LinkedHashMap<Integer, DrawInfo>();
        String destDirPath = mNotePage.getFilePath();

        if (mIsPaintBook) {
            mCurrentLayerTransMatrix.postScale(PAINT_BOOK_SCALE, PAINT_BOOK_SCALE);
        }
        else {
            mCurrentLayerTransMatrix.postScale(NOTE_BOOK_SCALE, NOTE_BOOK_SCALE);
        }

        int index = mCurrentPathList.size() - 1;

        if (graphicInfo != null) {
            drawInfos.put(index + 1, graphicInfo.getDrawInfo(destDirPath));
        }

        for (SerPointInfo info : mCurrentPathList) {
            DrawInfo drawInfo = info.getDrawInfo(destDirPath);
            drawInfo.transform(mCurrentLayerTransMatrix);
            drawInfos.put(index--, drawInfo);
        }

        for (SerEraseInfo info : mCurrentEraseList) {
            EraseDrawInfo eraseInfo = (EraseDrawInfo) info.getDrawInfo(destDirPath);
    		eraseInfo.transform(mCurrentLayerTransMatrix);
            Collection<DrawInfo> values = drawInfos.values();
            for (DrawInfo drawInfo : values) {
                drawInfo.add(eraseInfo, false);
            }
        }

        SerAnnotationInfo annotationInfo = new SerAnnotationInfo(drawInfos, mNotePage);

        mCurrentPathList.clear();
        mCurrentEraseList.clear();
        
        return annotationInfo;
    }
	
	private void loadAttachment(AsusFormatReader.Item item) {
        int id = item.getId();
        switch (id) {
        case V1DataFormat.FILE_PAGE_CONTENT_BEGIN:
            break;
        case V1DataFormat.PAGECOTENT_ATTACHMENT_POS_BEGIN:
            break;
        case V1DataFormat.PAGECOTENT_ATTACHMENT_POS_END:
            break;
        case V1DataFormat.PAGECOTENT_ATTACHMENT_PATH:
            if (mNotePage != null) {
            	String attachPath = MetaData.DATA_DIR + "attach/";
                PickerUtility.forceMkDir(mNotePage.getFilePath());
                File srcFile = new File(attachPath + item.getStringValue());
                if (srcFile.exists()) {
	                File dstFile = new File(mNotePage.getFilePath(), item.getStringValue());
	                srcFile.renameTo(dstFile);
	                File attachDir = new File(attachPath);
	                String files[] = attachDir.list();
	                if ((files == null) || (files.length == 0)) {
	                	attachDir.delete();
	                }
                }
            }
            break;
        case V1DataFormat.FILE_PAGE_CONTENT_END:
            break;
        }
	}
	
}
