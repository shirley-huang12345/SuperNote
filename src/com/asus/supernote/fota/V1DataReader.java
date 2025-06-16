package com.asus.supernote.fota;

import java.io.DataInputStream;
import java.io.IOException;

public final class V1DataReader extends DataReader {

	public boolean readPreference(DataInputStream prefDis) throws IOException {
        // 0
        byte doodleStyle = prefDis.readByte();
        // 1
        byte doodleSize = prefDis.readByte();
        // 2
        byte eraserSize = prefDis.readByte();
        // 3
        byte scribbleSize = prefDis.readByte(); // Scribble size
        // 4
        byte annotationStyle = prefDis.readByte(); // Annotation style
        // 5
        byte annotationSize = prefDis.readByte(); // Annotation size
        // 6
        byte annotationEraserSize = prefDis.readByte(); // Annotation eraser size
        // 7
        byte charDis = prefDis.readByte(); // Character distance
        // 8
        prefDis.readByte(); // Fixed: 2
        // 9-10
        short stopTime = prefDis.readShort(); // Stop time
        // 11
        byte flag = prefDis.readByte(); // Flag
        // 12-15
        int doodleColor = prefDis.readInt();
        // 16-19
        int scribbleColor = prefDis.readInt(); // Scribble color
        // 20-23
        int annotationColor = prefDis.readInt(); // Annotation color
        // 24-27
        int version = prefDis.readInt(); // Version: 1
        // 28
        byte pwdLen = prefDis.readByte();
        int[] colors = new int[10];
        for (int i = 0; i < 10; i++) {
        	// 29-68
        	colors[i] = prefDis.readInt();
        }
        // 69-76
        prefDis.skip(8); // reserved
        // 77-78
        short colorPanelX = prefDis.readShort(); // X-axis for color panel
        // 79-80
        short colorPanelY = prefDis.readShort(); // Y-axis for color panel
        // 81-84
        int curBookId = prefDis.readInt(); // Current notebook id
        // 85-92
        long curPageId = prefDis.readLong(); // Current page id
        
        mOnReadListener.onReadByte(V1DataFormat.PREF_DOODLE_STYLE, doodleStyle);
        mOnReadListener.onReadByte(V1DataFormat.PREF_DOODLE_SIZE, doodleSize);
        mOnReadListener.onReadByte(V1DataFormat.PREF_ERASER_SIZE, eraserSize);
        mOnReadListener.onReadByte(V1DataFormat.PREF_SCRIBBLE_SIZE, scribbleSize);
        mOnReadListener.onReadByte(V1DataFormat.PREF_ANNOTATION_STYLE, annotationStyle);
        mOnReadListener.onReadByte(V1DataFormat.PREF_ANNOTATION_SIZE, annotationSize);
        mOnReadListener.onReadByte(V1DataFormat.PREF_ANNOTATION_ERASER_SIZE, annotationEraserSize);
        mOnReadListener.onReadByte(V1DataFormat.PREF_CHARACTER_DISTANCE, charDis);
        mOnReadListener.onReadShort(V1DataFormat.PREF_STOP_TIME, stopTime);
        mOnReadListener.onReadByte(V1DataFormat.PREF_FLAG, flag);
        mOnReadListener.onReadInt(V1DataFormat.PREF_DOODLE_COLOR, doodleColor);
        mOnReadListener.onReadInt(V1DataFormat.PREF_SCRIBBLE_COLOR, scribbleColor);
        mOnReadListener.onReadInt(V1DataFormat.PREF_ANNOTATION_COLOR, annotationColor);
        mOnReadListener.onReadInt(V1DataFormat.PREF_VERSION, version);
        mOnReadListener.onReadByte(V1DataFormat.PREF_PASSWORD_LEN, pwdLen);
        mOnReadListener.onReadIntArray(V1DataFormat.PREF_COLORS, colors);
        mOnReadListener.onReadShort(V1DataFormat.PREF_COLOR_PANEL_X, colorPanelX);
        mOnReadListener.onReadShort(V1DataFormat.PREF_COLOR_PANEL_Y, colorPanelY);
        mOnReadListener.onReadInt(V1DataFormat.PREF_CURRENT_BOOK_ID, curBookId);
        mOnReadListener.onReadLong(V1DataFormat.PREF_CURRENT_PAGE_ID, curPageId);
        
        // 93-127
        prefDis.skip(35);
        if (pwdLen > 0) {
	        byte[] pwdBytes = new byte[pwdLen];
	        prefDis.read(pwdBytes);
	        for (int i = 0; i < pwdBytes.length; i++) {
	        	pwdBytes[i] ^= 0x26;
	        }
	        
	        mOnReadListener.onReadByteArray(V1DataFormat.PREF_PASSWORD, pwdBytes);
        }
		
		return true;
	}
	
	public boolean readBookList(DataInputStream bookListDis) throws IOException {
		byte version = bookListDis.readByte(); // version
       	short num = bookListDis.readShort();
       	
       	mOnReadListener.onReadByte(V1DataFormat.NBS_VERSION, version);
       	mOnReadListener.onReadShort(V1DataFormat.NBS_BOOK_NUM, num);
       	
       	for (int i = 0; i < num; i++) {
       		byte bookVersion = bookListDis.readByte(); // version
       		int bookFontSize = bookListDis.readInt();
       		int bookBakColor = bookListDis.readInt();
       		bookListDis.skip(16); // reserved
       		int bookId = bookListDis.readInt();
       		short nameLen = bookListDis.readShort();
       		byte type = bookListDis.readByte();
       		byte[] nameByte = new byte[nameLen];
       		bookListDis.readFully(nameByte);
       		
       		mOnReadListener.onReadByte(V1DataFormat.NBS_BOOK_VERSION, bookVersion);
       		mOnReadListener.onReadInt(V1DataFormat.NBS_BOOK_FONT_SIZE, bookFontSize);
       		mOnReadListener.onReadInt(V1DataFormat.NBS_BOOK_BAK_COLOR, bookBakColor);
       		mOnReadListener.onReadInt(V1DataFormat.NBS_BOOK_ID, bookId);
       		mOnReadListener.onReadShort(V1DataFormat.NBS_BOOK_NAME_LEN, nameLen);
       		mOnReadListener.onReadByte(V1DataFormat.NBS_BOOK_TYPE, type);
       		mOnReadListener.onReadByteArray(V1DataFormat.NBS_BOOK_NAME, nameByte);
       	}
		
		return true;
	}
	
	public boolean readBookInfo(DataInputStream bookInfoDis) throws IOException {
		byte version = bookInfoDis.readByte();
        int defaultFontSize = bookInfoDis.readInt();
        int backgroundColor = bookInfoDis.readInt();
        byte pageType = bookInfoDis.readByte();
        mOnReadListener.onReadByte(V1DataFormat.MF_VERSION, version);
        mOnReadListener.onReadInt(V1DataFormat.MF_DEFAULT_FONT_SIZE, defaultFontSize);
        mOnReadListener.onReadInt(V1DataFormat.MF_BACKGROUND_COLOR, backgroundColor);
        mOnReadListener.onReadByte(V1DataFormat.MF_PAGE_TYPE, pageType);
		
		return true;
	}
	
	public boolean readPageList(DataInputStream pageListFis) throws IOException {
		byte version = pageListFis.readByte();
        short pageNum = pageListFis.readShort();

        mOnReadListener.onReadByte(V1DataFormat.INDEX_VERSION, version);
        mOnReadListener.onReadShort(V1DataFormat.INDEX_PAGE_NUM, pageNum);
        for (int i = 0; i < pageNum; i++) {
            byte pageVer = pageListFis.readByte();
            long pageId = pageListFis.readLong();
            long createTime = pageListFis.readLong();
            long modifiedTime = pageListFis.readLong();
            int defaultFontSize = pageListFis.readInt();
            byte isBookmark = pageListFis.readByte();
            long reserved = pageListFis.readLong();
            byte timestampNum = pageListFis.readByte();
            
            mOnReadListener.onReadByte(V1DataFormat.INDEX_PAGE_VERSION, pageVer);
            mOnReadListener.onReadLong(V1DataFormat.INDEX_PAGE_ID, pageId);
            mOnReadListener.onReadLong(V1DataFormat.INDEX_PAGE_CREATE_TIME, createTime);
            mOnReadListener.onReadLong(V1DataFormat.INDEX_PAGE_MODIFIED_TIME, modifiedTime);
            mOnReadListener.onReadInt(V1DataFormat.INDEX_PAGE_DEFAULT_FONT_SIZE, defaultFontSize);
            mOnReadListener.onReadByte(V1DataFormat.INDEX_PAGE_IS_BOOKMARK, isBookmark);
            mOnReadListener.onReadLong(V1DataFormat.INDEX_PAGE_RESERVED, reserved);
            mOnReadListener.onReadByte(V1DataFormat.INDEX_PAGE_TIMESTAMP_NUM, timestampNum);
            
            if (timestampNum > 0) {
	            long[] timestamps = new long[timestampNum];
	            for (int j = 0; j < timestampNum; j++) {
	                timestamps[j] = pageListFis.readLong();
	            }
	            
	            mOnReadListener.onReadLongArray(V1DataFormat.INDEX_PAGE_TIMESTAMP_VALUE, timestamps);
            }
        }
		
		return true;
	}
	
	public boolean readPageItems(DataInputStream itemsDis) throws IOException {
		byte version = itemsDis.readByte();
		int byteNum = itemsDis.readUnsignedShort();
		byte[] data = new byte[byteNum];
		itemsDis.readFully(data);
		String content = new String(data, DataFormat.UTF8_CHARSET_NAME);
		if (1 != version)
			throw new RuntimeException("unsupport page content file version "
					+ version);

		mOnReadListener.onReadByte(V1DataFormat.PAGECOTENT_VERSION, version);
		mOnReadListener.onReadUnsignedShort(V1DataFormat.PAGECOTENT_BYTE_NUM,
				byteNum);
		mOnReadListener.onReadString(V1DataFormat.PAGECOTENT_STRING, content);

		while (true) {
			byte type = (byte) itemsDis.read();
			if (0 > type)
				break;

			mOnReadListener.onReadByte(V1DataFormat.PAGECOTENT_HW_TYPE, type);

			if (124 == type) {
				float handwriteScale = itemsDis.readFloat();
				int color = itemsDis.readInt();
				byte mode = itemsDis.readByte();
				byte heightScale = itemsDis.readByte();
				int posInString = itemsDis.readUnsignedShort();
				int handwriteByteNum = itemsDis.readUnsignedShort();

				mOnReadListener.onReadFloat(V1DataFormat.PAGECOTENT_HW_SCALE,
						handwriteScale);
				mOnReadListener.onReadInt(
						V1DataFormat.PAGECOTENT_HW_FONT_COLOR, color);
				mOnReadListener.onReadByte(V1DataFormat.PAGECOTENT_HW_MODE,
						mode);
				mOnReadListener.onReadByte(
						V1DataFormat.PAGECOTENT_HW_HEIGHT_SCALE, heightScale);
				mOnReadListener.onReadUnsignedShort(
						V1DataFormat.PAGECOTENT_HW_POS_IN_STRING, posInString);
				mOnReadListener.onReadUnsignedShort(
						V1DataFormat.PAGECOTENT_HW_BYTE_NUM, handwriteByteNum);

				short[] xys = new short[handwriteByteNum];
				for (int i = 0; i < handwriteByteNum; i++) {
					xys[i] = (short) itemsDis.readUnsignedByte();
				}

				mOnReadListener.onReadShortArray(
						V1DataFormat.PAGECOTENT_HW_PATH_XY, xys);

				continue;
			}
			if (122 == type) {
				int posInString = itemsDis.readUnsignedShort();
				int iconId = itemsDis.readUnsignedByte();

				mOnReadListener
						.onReadUnsignedShort(
								V1DataFormat.PAGECOTENT_ICON_POS_IN_STRING,
								posInString);
				mOnReadListener.onReadUnsignedByte(
						V1DataFormat.PAGECOTENT_ICON_ID, iconId);

				continue;
			}
			if (120 == type) {
				int posInString = itemsDis.readUnsignedShort();
				long time = itemsDis.readLong();

				mOnReadListener.onReadUnsignedShort(
						V1DataFormat.PAGECOTENT_TIMESTAMP_POS_IN_STRING,
						posInString);
				mOnReadListener.onReadLong(
						V1DataFormat.PAGECOTENT_TIMESTAMP_VALUE, time);

				continue;
			}
			if (2 == type) {
				int color = itemsDis.readInt();
				int posBegin = itemsDis.readUnsignedShort();
				int posEnd = itemsDis.readUnsignedShort();

				mOnReadListener.onReadInt(
						V1DataFormat.PAGECOTENT_FONTCOLOR_COLOR, color);
				mOnReadListener.onReadUnsignedShort(
						V1DataFormat.PAGECOTENT_FONTCOLOR_POS_BEGIN, posBegin);
				mOnReadListener.onReadUnsignedShort(
						V1DataFormat.PAGECOTENT_FONTCOLOR_POS_END, posEnd);

				continue;
			}
			if (7 == type) {
				int style = itemsDis.readInt();
				int posBegin = itemsDis.readUnsignedShort();
				int posEnd = itemsDis.readUnsignedShort();

				mOnReadListener.onReadInt(
						V1DataFormat.PAGECOTENT_FONTSTYLE_STYLE, style);
				mOnReadListener.onReadUnsignedShort(
						V1DataFormat.PAGECOTENT_FONTSTYLE_POS_BEGIN, posBegin);
				mOnReadListener.onReadUnsignedShort(
						V1DataFormat.PAGECOTENT_FONTSTYLE_POS_END, posEnd);

				continue;
			}
			if (121 == type) {
				int posBegin = itemsDis.readUnsignedShort();
				int posEnd = itemsDis.readUnsignedShort();
				int pathByteNum = itemsDis.readUnsignedByte();
				byte[] pathData = new byte[pathByteNum];
				itemsDis.readFully(pathData);
				String path = new String(pathData, DataFormat.UTF8_CHARSET_NAME);

				mOnReadListener.onReadUnsignedShort(
						V1DataFormat.PAGECOTENT_ATTACHMENT_POS_BEGIN, posBegin);
				mOnReadListener.onReadUnsignedShort(
						V1DataFormat.PAGECOTENT_ATTACHMENT_POS_END, posEnd);
				mOnReadListener.onReadUnsignedByte(
						V1DataFormat.PAGECOTENT_ATTACHMENT_PATH_BYTE_NUM,
						pathByteNum);
				mOnReadListener.onReadString(
						V1DataFormat.PAGECOTENT_ATTACHMENT_PATH, path);

				continue;
			}

			throw new RuntimeException("unsupport page content type " + type);
		}

		return true;
	}

	public boolean readPageDoodles(DataInputStream doodlesDis)
			throws IOException {		
		byte ver = doodlesDis.readByte();
		short layerNum = doodlesDis.readShort();
		if (0 != ver)
			throw new RuntimeException("unsupport page layer file version "
					+ ver);

		mOnReadListener.onReadByte(V1DataFormat.PAGELAYER_VERSION, ver);
		mOnReadListener.onReadShort(V1DataFormat.PAGELAYER_LAYER_NUM, layerNum);
		
		for (int i = 0; i < layerNum; i++) {			
			byte layerVer = doodlesDis.readByte();

			mOnReadListener.onReadByte(V1DataFormat.PAGELAYER_LAYER_VERSION,
					layerVer);

			if (2 == layerVer) {				
				long rect = doodlesDis.readLong();
				byte degree = doodlesDis.readByte();
				long fileId = doodlesDis.readLong();

				mOnReadListener.onReadLong(V1DataFormat.PAGELAYER_LAYER_RECT,
						rect);
				mOnReadListener.onReadByte(
						V1DataFormat.PAGELAYER_LAYER_ROTATE_DEGREE, degree);
				mOnReadListener.onReadLong(
						V1DataFormat.PAGELAYER_LAYER_FILE_ID, fileId);

				continue;
			}
			
			if (3 == layerVer) {				
				long fileId = doodlesDis.readLong();
				byte degree = doodlesDis.readByte();

				mOnReadListener.onReadLong(
						V1DataFormat.PAGELAYER_LAYER_FILE_ID, fileId);
				mOnReadListener.onReadByte(
						V1DataFormat.PAGELAYER_LAYER_ROTATE_DEGREE, degree);

				readPageDoodleOpDesc(doodlesDis);
				
				continue;
			}
			
			if (4 == layerVer) {				
				long rect = doodlesDis.readLong();
				long fileId = doodlesDis.readLong();
				byte degree = doodlesDis.readByte();

				mOnReadListener.onReadLong(V1DataFormat.PAGELAYER_LAYER_RECT,
						rect);
				mOnReadListener.onReadLong(
						V1DataFormat.PAGELAYER_LAYER_FILE_ID, fileId);
				mOnReadListener.onReadByte(
						V1DataFormat.PAGELAYER_LAYER_ROTATE_DEGREE, degree);

				readPageDoodleOpDesc(doodlesDis);

				continue;
			}

			throw new RuntimeException("unsupport layer version " + layerVer);
		}

		return true;
	}

	private boolean readPageDoodleOp(DataInputStream doodleOpDis, byte ver)
			throws IOException {
		mOnReadListener.onReadByte(V1DataFormat.PAGELAYER_LAYER_OP_VERSION0,
				ver);

		if (1 == ver) {
			long rect = doodleOpDis.readLong();

			mOnReadListener.onReadLong(V1DataFormat.PAGELAYER_LAYER_OP_RECT,
					rect);
		}
		byte style = doodleOpDis.readByte();

		mOnReadListener
				.onReadByte(V1DataFormat.PAGELAYER_LAYER_OP_STYLE, style);

		byte ver2 = doodleOpDis.readByte();

		mOnReadListener.onReadByte(V1DataFormat.PAGELAYER_LAYER_OP_VERSION2,
				ver2);

		if (1 == ver2) {
			if (127 == style) {
				int shiftX = doodleOpDis.readInt();
				int shiftY = doodleOpDis.readInt();
				int[] shiftXY = { shiftX, shiftY };

				mOnReadListener.onReadIntArray(
						V1DataFormat.PAGELAYER_LAYER_OP_PEN_SHIFT_XY, shiftXY);
			} else if (125 == style) {
				int rtX = doodleOpDis.readInt();
				int rtY = doodleOpDis.readInt();
				int rtDegree = doodleOpDis.readInt();
				int[] rtXYDegree = { rtX, rtY, rtDegree };

				mOnReadListener.onReadIntArray(
						V1DataFormat.PAGELAYER_LAYER_OP_PEN_RT_CENTER_XY,
						rtXYDegree);
			} else if (126 == style) {
				int scaleX = doodleOpDis.readInt();
				int scaleY = doodleOpDis.readInt();
				float scaleRatio = doodleOpDis.readFloat();
				float[] scaleXY = { scaleX, scaleY, scaleRatio };
				mOnReadListener.onReadFloatArray(
						V1DataFormat.PAGELAYER_LAYER_OP_PEN_SCALE_CENTER_XY,
						scaleXY);
			} else if (6 == style) {
				int color = doodleOpDis.readInt();
				float width = doodleOpDis.readFloat();

				mOnReadListener.onReadInt(
						V1DataFormat.PAGELAYER_LAYER_OP_PEN_BRUSH_COLOR, color);
				mOnReadListener.onReadFloat(
						V1DataFormat.PAGELAYER_LAYER_OP_PEN_BRUSH_WIDTH, width);

				readPageDoodleOpPoints(doodleOpDis);
			} else {
				int color = doodleOpDis.readInt();
				float width = doodleOpDis.readFloat();

				mOnReadListener.onReadInt(
						V1DataFormat.PAGELAYER_LAYER_OP_PEN_BRUSH_COLOR, color);
				mOnReadListener.onReadFloat(
						V1DataFormat.PAGELAYER_LAYER_OP_PEN_BRUSH_WIDTH, width);
			}
		}
		int width = doodleOpDis.readUnsignedShort();
		int height = doodleOpDis.readUnsignedShort();

		mOnReadListener.onReadUnsignedShort(
				V1DataFormat.PAGELAYER_LAYER_OP_REAL_WIDTH, width);
		mOnReadListener.onReadUnsignedShort(
				V1DataFormat.PAGELAYER_LAYER_OP_REAL_HEIGHT, height);

		readPageDoodleOpPoints(doodleOpDis);

		return true;
	}

	private boolean readPageDoodleOpDesc(DataInputStream doodleOpDescDis)
			throws IOException {
		byte ver0 = doodleOpDescDis.readByte();
		readPageDoodleOpDesc(doodleOpDescDis, ver0);

		return true;
	}

	private boolean readPageDoodleOpDesc(DataInputStream doodleOpDescDis,
			byte ver0) throws IOException {
		mOnReadListener.onReadByte(
				V1DataFormat.PAGELAYER_LAYER_OP_DESC_VERSION0, ver0);

		if (2 == ver0) {
			byte ver1 = doodleOpDescDis.readByte();

			mOnReadListener.onReadByte(
					V1DataFormat.PAGELAYER_LAYER_OP_DESC_VERSION1, ver1);

			if (1 == ver1) {
				long rect = doodleOpDescDis.readLong();

				mOnReadListener.onReadLong(
						V1DataFormat.PAGELAYER_LAYER_OP_DESC_RECT, rect);
			}
			byte brushStyle = doodleOpDescDis.readByte();
			byte ver3 = doodleOpDescDis.readByte();
			int width = doodleOpDescDis.readUnsignedShort();
			int height = doodleOpDescDis.readUnsignedShort();

			mOnReadListener.onReadByte(
					V1DataFormat.PAGELAYER_LAYER_OP_DESC_BRUSH_STYLE,
					brushStyle);
			mOnReadListener.onReadByte(
					V1DataFormat.PAGELAYER_LAYER_OP_DESC_VERSION3, ver3);
			mOnReadListener.onReadUnsignedShort(
					V1DataFormat.PAGELAYER_LAYER_OP_DESC_REAL_WIDTH, width);
			mOnReadListener.onReadUnsignedShort(
					V1DataFormat.PAGELAYER_LAYER_OP_DESC_REAL_HEIGHT, height);

			readPageDoodleOpPoints(doodleOpDescDis);
			readPageDoodleOpOrOpDesc(doodleOpDescDis);

			return true;
		}

		throw new RuntimeException("unsupport layer operation desc version "
				+ ver0);
	}

	private boolean readPageDoodleOpPoints(DataInputStream doodleOpPiontsDis)
			throws IOException {
		byte ver = doodleOpPiontsDis.readByte();

		mOnReadListener.onReadByte(V1DataFormat.PAGELAYER_LAYER_OP_PT_VERSION,
				ver);

		if (1 == ver) {
			int ptsNum = doodleOpPiontsDis.readUnsignedShort();

			mOnReadListener.onReadUnsignedShort(
					V1DataFormat.PAGELAYER_LAYER_OP_PT_NUM, ptsNum);

			float[] xys = new float[ptsNum * 2];
			int idx = 0;
			for (int i = 0; i < ptsNum; i++) {
				xys[idx++] = doodleOpPiontsDis.readFloat();
				xys[idx++] = doodleOpPiontsDis.readFloat();
			}

			mOnReadListener.onReadFloatArray(
					V1DataFormat.PAGELAYER_LAYER_OP_PT_XY, xys);

			return true;
		}
		if (2 == ver) {
			int ptsNum = doodleOpPiontsDis.readUnsignedShort();

			mOnReadListener.onReadUnsignedShort(
					V1DataFormat.PAGELAYER_LAYER_OP_PT_NUM, ptsNum);

			float[] xys = new float[ptsNum * 2];
			long[] seeds = new long[ptsNum];
			int idx = 0;
			for (int i = 0; i < ptsNum; i++) {
				xys[idx++] = doodleOpPiontsDis.readFloat();
				xys[idx++] = doodleOpPiontsDis.readFloat();
				seeds[i] = doodleOpPiontsDis.readLong();
			}

			mOnReadListener.onReadFloatArray(
					V1DataFormat.PAGELAYER_LAYER_OP_PT_XY, xys);
			mOnReadListener.onReadLongArray(
					V1DataFormat.PAGELAYER_LAYER_OP_PT_SEED, seeds);

			return true;
		}
		if (0 == ver) {
			return true;
		}

		throw new RuntimeException("unsupport layer operation points version "
				+ ver);
	}

	private void readPageDoodleOpOrOpDesc(DataInputStream doodleOpDis)
			throws IOException {
		int opNum = doodleOpDis.readUnsignedShort();
		mOnReadListener.onReadUnsignedShort(
				V1DataFormat.PAGELAYER_LAYER_OP_PT_NUM, opNum);
		for (int i = 0; i < opNum; i++) {
			byte ver = doodleOpDis.readByte();
			if (2 == ver) {
				readPageDoodleOpDesc(doodleOpDis, ver);
			} else if ((0 == ver) || (1 == ver)) {
				readPageDoodleOp(doodleOpDis, ver);
			} else {
				throw new RuntimeException(
						"unsupport layer operation or desc version " + ver);
			}
		}
	}
	
}
