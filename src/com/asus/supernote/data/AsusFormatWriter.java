package com.asus.supernote.data;

/*	item = type (4 bytes) + size (4 bytes) + value (by size)
 * 	value : bytes array if size > 0, and size means array size 
 *  value : special definition below if size < 0, first byte as tag,lower 3 bytes means size by bytes
 *  value : short 	if size == 0x81000002
 *  value : integer if size == 0x82000004
 *  value : float 	if size == 0x83000004
 *  value : long	if size == 0x84000008
 *  value : boolean	if size == 0x85000001 
 *  value : byte	if size == 0x86000001   
 *  value : unsigned byte	if size == 0x87000004
 *  value : unsigned short	if size == 0x88000004
 *  value : short array 	if size == 0x91XXXXXX, and XXXXXX means size by bytes, e.g. 1 short 2 bytes 
 *  value : integer array 	if size == 0x92XXXXXX, and XXXXXX means size by bytes, e.g. 1 integer 4 bytes 
 *  value : float array 	if size == 0x93XXXXXX, and XXXXXX means size by bytes, e.g. 1 float 4 bytes 
 *  value : long array 		if size == 0x94XXXXXX, and XXXXXX means size by bytes, e.g. 1 long 8 bytes 
 *  value : string  		if size == 0x9FXXXXXX, and XXXXXX means size by bytes with utf8 encoding
 */

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class AsusFormatWriter {
    public final static int  FORMAT_DEFINED_SIZE_MASK_TAG          = 0xFF000000;
    public final static int  FORMAT_DEFINED_SIZE_MASK_BYTES_NUM    = 0x00FFFFFF;

    public final static int  FORMAT_DEFINED_SIZE_MAX_BYTES_NUM     = 0x00FFFFFF;

    public final static int  FORMAT_DEFINED_SIZE_SHORT             = 0x81000002;
    public final static int  FORMAT_DEFINED_SIZE_INTEGER           = 0x82000004;
    public final static int  FORMAT_DEFINED_SIZE_FLOAT             = 0x83000004;
    public final static int  FORMAT_DEFINED_SIZE_LONG              = 0x84000008;
    public final static int  FORMAT_DEFINED_SIZE_BOOLEAN           = 0x85000001;
    public final static int  FORMAT_DEFINED_SIZE_BYTE              = 0x86000001;
    public final static int  FORMAT_DEFINED_SIZE_UNSIGNED_BYTE     = 0x87000004;
    public final static int  FORMAT_DEFINED_SIZE_UNSIGNED_SHORT    = 0x88000004;

    public final static int  FORMAT_DEFINED_SIZE_TAG_ARRAY_SHORT   = 0x91000000;
    public final static int  FORMAT_DEFINED_SIZE_TAG_ARRAY_INTEGER = 0x92000000;
    public final static int  FORMAT_DEFINED_SIZE_TAG_ARRAY_FLOAT   = 0x93000000;
    public final static int  FORMAT_DEFINED_SIZE_TAG_ARRAY_LONG    = 0x94000000;
    public final static int  FORMAT_DEFINED_SIZE_TAG_STRING        = 0x9F000000;

    private DataOutputStream mOutputStream                         = null;

    public AsusFormatWriter(OutputStream output) {
        mOutputStream = new DataOutputStream(output);
    }

    public void close() throws IOException {
        mOutputStream.close();
    }

    public void writeByte(int id, byte value) throws IOException {
        mOutputStream.writeInt(id);
        mOutputStream.writeInt(FORMAT_DEFINED_SIZE_BYTE);
        mOutputStream.writeByte(value);
    }

    public void writeUnsignedByte(int id, int value) throws IOException {
        mOutputStream.writeInt(id);
        mOutputStream.writeInt(FORMAT_DEFINED_SIZE_UNSIGNED_BYTE);
        mOutputStream.writeInt(value);
    }

    public void writeBoolean(int id, boolean value) throws IOException {
        mOutputStream.writeInt(id);
        mOutputStream.writeInt(FORMAT_DEFINED_SIZE_BOOLEAN);
        mOutputStream.writeBoolean(value);
    }

    public void writeShort(int id, short value) throws IOException {
        mOutputStream.writeInt(id);
        mOutputStream.writeInt(FORMAT_DEFINED_SIZE_SHORT);
        mOutputStream.writeShort(value);
    }

    public void writeUnsignedShort(int id, int value) throws IOException {
        mOutputStream.writeInt(id);
        mOutputStream.writeInt(FORMAT_DEFINED_SIZE_UNSIGNED_SHORT);
        mOutputStream.writeInt(value);
    }

    public void writeInt(int id, int value) throws IOException {
        mOutputStream.writeInt(id);
        mOutputStream.writeInt(FORMAT_DEFINED_SIZE_INTEGER);
        mOutputStream.writeInt(value);
    }

    public void writeFloat(int id, float value) throws IOException {
        mOutputStream.writeInt(id);
        mOutputStream.writeInt(FORMAT_DEFINED_SIZE_FLOAT);
        mOutputStream.writeFloat(value);
    }

    public void writeLong(int id, long value) throws IOException {
        mOutputStream.writeInt(id);
        mOutputStream.writeInt(FORMAT_DEFINED_SIZE_LONG);
        mOutputStream.writeLong(value);
    }

    public void writeShortArray(int id, short[] data, int offset, int count) throws IOException {
        if ((count << 1) > FORMAT_DEFINED_SIZE_MAX_BYTES_NUM)
            throw new RuntimeException("Too large size " + count + " to support.");
        int size = FORMAT_DEFINED_SIZE_TAG_ARRAY_SHORT | (count << 1);
        mOutputStream.writeInt(id);
        mOutputStream.writeInt(size);
        for (int i = 0; i < count; i++) {
            mOutputStream.writeShort(data[offset + i]);
        }
    }

    public void writeIntArray(int id, int[] data, int offset, int count) throws IOException {
        if ((count << 2) > FORMAT_DEFINED_SIZE_MAX_BYTES_NUM)
            throw new RuntimeException("Too large size " + count + " to support.");
        int size = FORMAT_DEFINED_SIZE_TAG_ARRAY_INTEGER | (count << 2);
        mOutputStream.writeInt(id);
        mOutputStream.writeInt(size);
        for (int i = 0; i < count; i++) {
            mOutputStream.writeInt(data[offset + i]);
        }
    }

    public void writeFloatArray(int id, float[] data, int offset, int count) throws IOException {
        if ((count << 2) > FORMAT_DEFINED_SIZE_MAX_BYTES_NUM)
            throw new RuntimeException("Too large size " + count + " to support.");
        int size = FORMAT_DEFINED_SIZE_TAG_ARRAY_FLOAT | (count << 2);
        mOutputStream.writeInt(id);
        mOutputStream.writeInt(size);
        for (int i = 0; i < count; i++) {
            mOutputStream.writeFloat(data[offset + i]);
        }
    }

    public void writeLongArray(int id, long[] data, int offset, int count) throws IOException {
        if ((count << 3) > FORMAT_DEFINED_SIZE_MAX_BYTES_NUM)
            throw new RuntimeException("Too large size " + count + " to support.");
        int size = FORMAT_DEFINED_SIZE_TAG_ARRAY_LONG | (count << 3);
        mOutputStream.writeInt(id);
        mOutputStream.writeInt(size);
        for (int i = 0; i < count; i++) {
            mOutputStream.writeLong(data[offset + i]);
        }
    }

    public void writeString(int id, String value) throws IOException {
        byte[] data = value.getBytes("utf8");
        if (data.length > FORMAT_DEFINED_SIZE_MAX_BYTES_NUM)
            throw new RuntimeException("Too large size " + value.length() + " to support.");
        int size = FORMAT_DEFINED_SIZE_TAG_STRING | data.length;
        mOutputStream.writeInt(id);
        mOutputStream.writeInt(size);
        mOutputStream.write(data);
    }

    public void writeByteArray(int id, byte[] data, int offset, int count) throws IOException {
        mOutputStream.writeInt(id);
        mOutputStream.writeInt(count);
        if (count > 0)
            mOutputStream.write(data, offset, count);
    }
    
    // BEGIN: Better
    public void writeFileForSync(int id, String filePath) throws IOException {
    	File file = new File(filePath);
    	if (file.exists()) {
	    	mOutputStream.writeInt(id);
	    	mOutputStream.writeInt((int)file.length() & 0x7FFFFFFF);
	    	FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			byte[] buffer = new byte[4096];
			int bytes;
			while ((bytes = bis.read(buffer)) != -1) {
				mOutputStream.write(buffer, 0, bytes);
			}
			bis.close();
			fis.close();
    	}
    }
    // END: Better
}
