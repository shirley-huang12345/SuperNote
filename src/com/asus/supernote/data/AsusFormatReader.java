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

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AsusFormatReader {
    public final static int FORMAT_DEFINED_SIZE_MASK_TAG          = 0xFF000000;
    public final static int FORMAT_DEFINED_SIZE_MASK_BYTES_NUM    = 0x00FFFFFF;

    public final static int FORMAT_DEFINED_SIZE_MAX_BYTES_NUM     = 0x00FFFFFF;

    public final static int FORMAT_DEFINED_SIZE_SHORT             = 0x81000002;
    public final static int FORMAT_DEFINED_SIZE_INTEGER           = 0x82000004;
    public final static int FORMAT_DEFINED_SIZE_FLOAT             = 0x83000004;
    public final static int FORMAT_DEFINED_SIZE_LONG              = 0x84000008;
    public final static int FORMAT_DEFINED_SIZE_BOOLEAN           = 0x85000001;
    public final static int FORMAT_DEFINED_SIZE_BYTE              = 0x86000001;
    public final static int FORMAT_DEFINED_SIZE_UNSIGNED_BYTE     = 0x87000004;
    public final static int FORMAT_DEFINED_SIZE_UNSIGNED_SHORT    = 0x88000004;

    public final static int FORMAT_DEFINED_SIZE_TAG_ARRAY_SHORT   = 0x91000000;
    public final static int FORMAT_DEFINED_SIZE_TAG_ARRAY_INTEGER = 0x92000000;
    public final static int FORMAT_DEFINED_SIZE_TAG_ARRAY_FLOAT   = 0x93000000;
    public final static int FORMAT_DEFINED_SIZE_TAG_ARRAY_LONG    = 0x94000000;
    public final static int FORMAT_DEFINED_SIZE_TAG_STRING        = 0x9F000000;

    private OnReadListener  mOnReadListener                       = null;
    private InputStream     mInputStream                          = null;
    private int             mMaxArraySize;

    public AsusFormatReader(InputStream inputStream, int maxArraySize) {
        mInputStream = inputStream;
        mMaxArraySize = maxArraySize;
    }

    public void setOnReadListener(OnReadListener listener) {
        mOnReadListener = listener;
    }

    public OnReadListener getOnReadListener() {
        return mOnReadListener;
    }

    public void read() throws IOException {
        read(mInputStream);
    }

    private void read(InputStream input) throws IOException {
        DataInputStream dis = new DataInputStream(input);
        int id, size, tag;
        while (true) {
            try {
                id = dis.readInt();
            }
            catch (EOFException e) {
                break;
            }
            size = dis.readInt();
            if (size > 0) {
                if (size > mMaxArraySize)
                    throw new UnsupportedOperationException("id " + id + " must be with array size <= " + mMaxArraySize);
                byte[] data = new byte[size];
                dis.readFully(data);
                mOnReadListener.onReadByteArray(id, data);
                continue;
            }
            if (size == 0) {
                mOnReadListener.onReadByteArray(id, new byte[0]);
                continue;
            }
            if (FORMAT_DEFINED_SIZE_SHORT == size) {
                short value = dis.readShort();
                mOnReadListener.onReadShort(id, value);
                continue;
            }
            if (FORMAT_DEFINED_SIZE_UNSIGNED_SHORT == size) {
                int value = dis.readInt();
                mOnReadListener.onReadUnsignedShort(id, value);
                continue;
            }
            if (FORMAT_DEFINED_SIZE_INTEGER == size) {
                int value = dis.readInt();
                mOnReadListener.onReadInt(id, value);
                continue;
            }
            if (FORMAT_DEFINED_SIZE_FLOAT == size) {
                float value = dis.readFloat();
                mOnReadListener.onReadFloat(id, value);
                continue;
            }
            if (FORMAT_DEFINED_SIZE_LONG == size) {
                long value = dis.readLong();
                mOnReadListener.onReadLong(id, value);
                continue;
            }
            if (FORMAT_DEFINED_SIZE_BOOLEAN == size) {
                boolean value = dis.readBoolean();
                mOnReadListener.onReadBoolean(id, value);
                continue;
            }
            if (FORMAT_DEFINED_SIZE_BYTE == size) {
                byte value = dis.readByte();
                mOnReadListener.onReadByte(id, value);
                continue;
            }
            if (FORMAT_DEFINED_SIZE_UNSIGNED_BYTE == size) {
                int value = dis.readInt();
                mOnReadListener.onReadUnsignedByte(id, value);
                continue;
            }
            tag = size & FORMAT_DEFINED_SIZE_MASK_TAG;
            if (FORMAT_DEFINED_SIZE_TAG_ARRAY_SHORT == tag) {
                int count = (size & FORMAT_DEFINED_SIZE_MAX_BYTES_NUM) >> 1;
                if (count > mMaxArraySize)
                    throw new UnsupportedOperationException("id " + id + " must be with array size <= " + mMaxArraySize);
                short[] data = new short[count];
                for (int i = 0; i < count; i++) {
                    data[i] = dis.readShort();
                }
                mOnReadListener.onReadShortArray(id, data);
                continue;
            }
            if (FORMAT_DEFINED_SIZE_TAG_ARRAY_INTEGER == tag) {
                int count = (size & FORMAT_DEFINED_SIZE_MAX_BYTES_NUM) >> 2;
                if (count > mMaxArraySize)
                    throw new UnsupportedOperationException("id " + id + " must be with array size <= " + mMaxArraySize);
                int[] data = new int[count];
                for (int i = 0; i < count; i++) {
                    data[i] = dis.readInt();
                }
                mOnReadListener.onReadIntArray(id, data);
                continue;
            }
            if (FORMAT_DEFINED_SIZE_TAG_ARRAY_FLOAT == tag) {
                int count = (size & FORMAT_DEFINED_SIZE_MAX_BYTES_NUM) >> 2;
                if (count > mMaxArraySize)
                    throw new UnsupportedOperationException("id " + id + " must be with array size <= " + mMaxArraySize);
                float[] data = new float[count];
                for (int i = 0; i < count; i++) {
                    data[i] = dis.readFloat();
                }
                mOnReadListener.onReadFloatArray(id, data);
                continue;
            }
            if (FORMAT_DEFINED_SIZE_TAG_ARRAY_LONG == tag) {
                int count = (size & FORMAT_DEFINED_SIZE_MAX_BYTES_NUM) >> 3;
                if (count > mMaxArraySize)
                    throw new UnsupportedOperationException("id " + id + " must be with array size <= " + mMaxArraySize);
                long[] data = new long[count];
                for (int i = 0; i < count; i++) {
                    data[i] = dis.readLong();
                }
                mOnReadListener.onReadLongArray(id, data);
                continue;
            }
            if (FORMAT_DEFINED_SIZE_TAG_STRING == tag) {
                int count = (size & FORMAT_DEFINED_SIZE_MAX_BYTES_NUM);
                if (count > mMaxArraySize)
                    throw new UnsupportedOperationException("id " + id + " must be with array size <= " + mMaxArraySize);
                byte[] data = new byte[count];
                dis.readFully(data);
                mOnReadListener.onReadString(id, new String(data, "utf8"));
                continue;
            }
            throw new RuntimeException("Unsupport id " + String.format("0x%08X", id) + " size " + String.format("0x%08X", size));
        }
        dis.close();
    }

    public Item readItem() throws IOException {
        return readItem(mInputStream);
    }

    private Item readItem(InputStream input) throws IOException {
        DataInputStream dis = new DataInputStream(input);
        int id, size, tag;
        try {
            id = dis.readInt();
        }
        catch (EOFException e) {
            return null;
        }

        size = dis.readInt();
        if (size > 0) {
        	// BEGIN: Better
			// When size > max array size, read by fragment
        	if (size > mMaxArraySize) {
                //throw new UnsupportedOperationException("id " + id + " must be with array size <= " + mMaxArraySize);
        		return new Item(id, size, Item.ITEM_TYPE_TOO_LARGE);
        	}
            byte[] data = new byte[size];
            // END: Better
            dis.readFully(data);
            return new Item(id, data);
        }
        if (size == 0) {
            return new Item(id, new byte[0]);
        }
        if (FORMAT_DEFINED_SIZE_BYTE == size) {
            byte value = dis.readByte();
            return new Item(id, value);
        }
        if (FORMAT_DEFINED_SIZE_UNSIGNED_BYTE == size) {
            int value = dis.readInt();
            return new Item(id, value, Item.ITEM_TYPE_UNSIGNED_BYTE);
        }
        if (FORMAT_DEFINED_SIZE_BOOLEAN == size) {
            boolean value = dis.readBoolean();
            return new Item(id, value);
        }
        if (FORMAT_DEFINED_SIZE_SHORT == size) {
            short value = dis.readShort();
            return new Item(id, value);
        }
        if (FORMAT_DEFINED_SIZE_UNSIGNED_SHORT == size) {
            int value = dis.readInt();
            return new Item(id, value, Item.ITEM_TYPE_UNSIGNED_SHORT);
        }
        if (FORMAT_DEFINED_SIZE_INTEGER == size) {
            int value = dis.readInt();
            return new Item(id, value);
        }
        if (FORMAT_DEFINED_SIZE_FLOAT == size) {
            float value = dis.readFloat();
            return new Item(id, value);
        }
        if (FORMAT_DEFINED_SIZE_LONG == size) {
            long value = dis.readLong();
            return new Item(id, value);
        }
        tag = size & FORMAT_DEFINED_SIZE_MASK_TAG;
        if (FORMAT_DEFINED_SIZE_TAG_ARRAY_SHORT == tag) {
            int count = (size & FORMAT_DEFINED_SIZE_MAX_BYTES_NUM) >> 1;
            if (count > mMaxArraySize)
                throw new UnsupportedOperationException("id " + id + " must be with array size <= " + mMaxArraySize);
            short[] data = new short[count];
            for (int i = 0; i < count; i++) {
                data[i] = dis.readShort();
            }
            return new Item(id, data);
        }
        if (FORMAT_DEFINED_SIZE_TAG_ARRAY_INTEGER == tag) {
            int count = (size & FORMAT_DEFINED_SIZE_MAX_BYTES_NUM) >> 2;
            if (count > mMaxArraySize)
                throw new UnsupportedOperationException("id " + id + " must be with array size <= " + mMaxArraySize);
            int[] data = new int[count];
            for (int i = 0; i < count; i++) {
                data[i] = dis.readInt();
            }
            return new Item(id, data);
        }
        if (FORMAT_DEFINED_SIZE_TAG_ARRAY_FLOAT == tag) {
            int count = (size & FORMAT_DEFINED_SIZE_MAX_BYTES_NUM) >> 2;
            if (count > mMaxArraySize)
                throw new UnsupportedOperationException("id " + id + " must be with array size <= " + mMaxArraySize);
            float[] data = new float[count];
            for (int i = 0; i < count; i++) {
                data[i] = dis.readFloat();
            }
            return new Item(id, data);
        }
        if (FORMAT_DEFINED_SIZE_TAG_ARRAY_LONG == tag) {
            int count = (size & FORMAT_DEFINED_SIZE_MAX_BYTES_NUM) >> 3;
            if (count > mMaxArraySize)
                throw new UnsupportedOperationException("id " + id + " must be with array size <= " + mMaxArraySize);
            long[] data = new long[count];
            for (int i = 0; i < count; i++) {
                data[i] = dis.readLong();
            }
            return new Item(id, data);
        }
        if (FORMAT_DEFINED_SIZE_TAG_STRING == tag) {
            int count = (size & FORMAT_DEFINED_SIZE_MAX_BYTES_NUM);
            if (count > mMaxArraySize)
                throw new UnsupportedOperationException("id " + id + " must be with array size <= " + mMaxArraySize);
            byte[] data = new byte[count];
            dis.readFully(data);
            return new Item(id, new String(data, "utf8"));
        }
        
        // BEGIN: Better
        return new Item(id, size, Item.ITEM_TYPE_TOO_LARGE);
        //throw new RuntimeException("Unsupport id " + String.format("0x%08X", id) + " size " + String.format("0x%08X", size));
        // END: Better
    }
    
    // BEGIN: Better
    public void readFileForSync(int bytes, String filePath) throws IOException {
    	File file = new File(filePath);
    	if (file.exists()) {
    		file.delete();
    	}
    	if (!file.exists()) {
    		file.createNewFile();
    	}
    	FileOutputStream fos = new FileOutputStream(file);
    	BufferedOutputStream bos = new BufferedOutputStream(fos);
    	byte[] buffer = new byte[4096];
    	int bytesRead = 0;
    	while (((bytesRead = mInputStream.read(buffer, 0, bytes > 4096 ? 4096 : bytes)) != -1)) {
    		bos.write(buffer, 0, bytesRead);
    		bytes -= bytesRead;
    	}
    	bos.close();
    	fos.close();
    }
    // END: Better

    public interface OnReadListener {
        public void onReadByte(int id, byte value);

        public void onReadUnsignedByte(int id, int value);

        public void onReadShort(int id, short value);

        public void onReadUnsignedShort(int id, int value);

        public void onReadInt(int id, int value);

        public void onReadFloat(int id, float value);

        public void onReadLong(int id, long value);

        public void onReadBoolean(int id, boolean value);

        public void onReadShortArray(int id, short[] data);

        public void onReadIntArray(int id, int[] data);

        public void onReadFloatArray(int id, float[] data);

        public void onReadLongArray(int id, long[] data);

        public void onReadString(int id, String value);

        public void onReadByteArray(int id, byte[] data);
    }

    public static class Item {
        public final static int ITEM_TYPE_SHORT          = 0x0001;
        public final static int ITEM_TYPE_INTEGER        = 0x0002;
        public final static int ITEM_TYPE_FLOAT          = 0x0003;
        public final static int ITEM_TYPE_LONG           = 0x0004;
        public final static int ITEM_TYPE_STRING         = 0x0005;
        public final static int ITEM_TYPE_BYTE           = 0x0006;
        public final static int ITEM_TYPE_BOOLEAN        = 0x0007;
        public final static int ITEM_TYPE_UNSIGNED_BYTE  = 0x0008;
        public final static int ITEM_TYPE_UNSIGNED_SHORT = 0x0009;
        public final static int ITEM_TYPE_SHORT_ARRAY    = 0x0011;
        public final static int ITEM_TYPE_INTEGER_ARRAY  = 0x0012;
        public final static int ITEM_TYPE_FLOAT_ARRAY    = 0x0013;
        public final static int ITEM_TYPE_LONG_ARRAY     = 0x0014;
        public final static int ITEM_TYPE_BYTE_ARRAY     = 0x0015;
        
        // BEGIN: Better
        public final static int ITEM_TYPE_TOO_LARGE		 = 0x0016;
        // END: Better

        private int             type;
        private int             itemId;
        private byte            byteValue;
        private short           shortValue;
        private int             intValue;
        private float           floatValue;
        private long            longValue;
        private boolean         booleanValue;
        private String          stringValue;
        private short[]         shortArray;
        private int[]           intArray;
        private float[]         floatArray;
        private long[]          longArray;
        private byte[]          byteArray;

        public Item(int id, int v, int t) {
        	// BEGIN: Better
            if ((t != ITEM_TYPE_UNSIGNED_BYTE) && (t != ITEM_TYPE_UNSIGNED_SHORT) && 
            		(t != ITEM_TYPE_TOO_LARGE))
                throw new IllegalArgumentException("last arg must be ITEM_TYPE_UNSIGNED_BYTE or ITEM_TYPE_UNSIGNED_SHORT");
            // END: Better
            itemId = id;
            intValue = v;
            type = t;
        }

        public Item(int id, boolean v) {
            itemId = id;
            type = ITEM_TYPE_BOOLEAN;
            booleanValue = v;
        }

        public Item(int id, byte v) {
            itemId = id;
            type = ITEM_TYPE_BYTE;
            byteValue = v;
        }

        public Item(int id, short v) {
            itemId = id;
            type = ITEM_TYPE_SHORT;
            shortValue = v;
        }

        public Item(int id, int v) {
            itemId = id;
            type = ITEM_TYPE_INTEGER;
            intValue = v;
        }

        public Item(int id, float v) {
            itemId = id;
            type = ITEM_TYPE_FLOAT;
            floatValue = v;
        }

        public Item(int id, long v) {
            itemId = id;
            type = ITEM_TYPE_LONG;
            longValue = v;
        }

        public Item(int id, String v) {
            itemId = id;
            type = ITEM_TYPE_STRING;
            stringValue = v;
        }

        public Item(int id, short[] v) {
            itemId = id;
            type = ITEM_TYPE_SHORT_ARRAY;
            shortArray = v;
        }

        public Item(int id, int[] v) {
            itemId = id;
            type = ITEM_TYPE_INTEGER_ARRAY;
            intArray = v;
        }

        public Item(int id, float[] v) {
            itemId = id;
            type = ITEM_TYPE_FLOAT_ARRAY;
            floatArray = v;
        }

        public Item(int id, long[] v) {
            itemId = id;
            type = ITEM_TYPE_LONG_ARRAY;
            longArray = v;
        }

        public Item(int id, byte[] v) {
            itemId = id;
            type = ITEM_TYPE_BYTE_ARRAY;
            byteArray = v;
        }

        public int getType() {
            return type;
        }

        public int getId() {
            return itemId;
        }

        public byte getByteValue() {
            return byteValue;
        }

        public short getShortValue() {
            return shortValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public float getFloatValue() {
            return floatValue;
        }

        public long getLongValue() {
            return longValue;
        }

        public String getStringValue() {
            return stringValue;
        }

        public short[] getShortArray() {
            return shortArray;
        }

        public int[] getIntArray() {
            return intArray;
        }

        public float[] getFloatArray() {
            return floatArray;
        }

        public long[] getLongArray() {
            return longArray;
        }

        public byte[] getByteArray() {
            return byteArray;
        }

        public final int MAX_ARRAY_TO_STRING  = 16;
        public final int MAX_STRING_TO_STRING = MAX_ARRAY_TO_STRING * 4;

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            switch (type) {
                case ITEM_TYPE_BYTE:
                    sb.append("Byte id ").append(String.format("0x%08X", itemId)).append(" value ").append(String.format("0x%02X", byteValue));
                    break;
                case ITEM_TYPE_BOOLEAN:
                    sb.append("Booleab id ").append(String.format("0x%08X", itemId)).append(" value ").append(booleanValue);
                    break;
                case ITEM_TYPE_SHORT:
                    sb.append("Short id ").append(String.format("0x%08X", itemId)).append(" value ").append(String.format("0x%04X", shortValue));
                    break;
                case ITEM_TYPE_INTEGER:
                    sb.append("Int id ").append(String.format("0x%08X", itemId)).append(" value ").append(String.format("0x%08X", intValue));
                    break;
                case ITEM_TYPE_UNSIGNED_BYTE:
                    sb.append("UnsignedByte id ").append(String.format("0x%08X", itemId)).append(" value ").append(intValue);
                    break;
                case ITEM_TYPE_UNSIGNED_SHORT:
                    sb.append("UnsignedShort id ").append(String.format("0x%08X", itemId)).append(" value ").append(intValue);
                    break;
                case ITEM_TYPE_FLOAT:
                    sb.append("Float id ").append(String.format("0x%08X", itemId)).append(" value ").append(floatValue);
                    break;
                case ITEM_TYPE_LONG:
                    sb.append("Long id ").append(String.format("0x%08X", itemId)).append(" value ").append(String.format("0x%016X", longValue));
                    break;
                case ITEM_TYPE_STRING: {
                    sb.append("String id ").append(String.format("0x%08X", itemId)).append(" length ").append(stringValue.length()).append(" value ");
                    String value = stringValue.replace("\n", "\\n");
                    if (value.length() > (MAX_STRING_TO_STRING)) {
                        sb.append('"').append(value.substring(0, MAX_STRING_TO_STRING)).append(" ...").append('"');
                    }
                    else {
                        sb.append('"').append(value).append('"');
                    }
                    break;
                }
                case ITEM_TYPE_SHORT_ARRAY: {
                    sb.append("ShortArray id ").append(String.format("0x%08X", itemId)).append(" length ").append(shortArray.length).append(" value ");
                    int length = (shortArray.length > MAX_ARRAY_TO_STRING) ? MAX_ARRAY_TO_STRING : shortArray.length;
                    for (int i = 0; i < length; i++)
                        sb.append(String.format("0x%04X ", shortArray[i]));
                    if (shortArray.length > MAX_ARRAY_TO_STRING)
                        sb.append(" ...");
                    break;
                }
                case ITEM_TYPE_INTEGER_ARRAY: {
                    sb.append("IntAtrray id ").append(String.format("0x%08X", itemId)).append(" length ").append(intArray.length).append(" value ");
                    int length = (intArray.length > MAX_ARRAY_TO_STRING) ? MAX_ARRAY_TO_STRING : intArray.length;
                    for (int i = 0; i < length; i++)
                        sb.append(String.format("0x%08X ", intArray[i]));
                    if (intArray.length > MAX_ARRAY_TO_STRING)
                        sb.append(" ...");
                    break;
                }
                case ITEM_TYPE_FLOAT_ARRAY: {
                    sb.append("FloatArray id ").append(String.format("0x%08X", itemId)).append(" length ").append(floatArray.length).append(" value ");
                    int length = (floatArray.length > MAX_ARRAY_TO_STRING) ? MAX_ARRAY_TO_STRING : floatArray.length;
                    for (int i = 0; i < length; i++)
                        sb.append(floatArray[i]).append(' ');
                    if (floatArray.length > MAX_ARRAY_TO_STRING)
                        sb.append(" ...");
                    break;
                }
                case ITEM_TYPE_LONG_ARRAY: {
                    sb.append("LongArray id ").append(String.format("0x%08X", itemId)).append(" length ").append(longArray.length).append(" value ");
                    int length = (longArray.length > MAX_ARRAY_TO_STRING) ? MAX_ARRAY_TO_STRING : longArray.length;
                    for (int i = 0; i < length; i++)
                        sb.append(String.format("0x%016X ", longArray[i]));
                    if (longArray.length > MAX_ARRAY_TO_STRING)
                        sb.append(" ...");
                    break;
                }
                case ITEM_TYPE_BYTE_ARRAY: {
                    sb.append("ByteArray id ").append(String.format("0x%08X", itemId)).append(" length ").append(byteArray.length).append(" value ");
                    int length = (byteArray.length > MAX_ARRAY_TO_STRING) ? MAX_ARRAY_TO_STRING : byteArray.length;
                    for (int i = 0; i < length; i++)
                        sb.append(String.format("0x%02X ", byteArray[i]));
                    if (byteArray.length > MAX_ARRAY_TO_STRING)
                        sb.append(" ...");
                    break;
                }
                default:
            }
            return sb.toString();
        }
    }

    private static void debugMesg(String mesg) {
        System.out.print(mesg);
    }

    public static void main(String[] args) throws Exception {

        OnReadListener onReadListener = new OnReadListener() {
            @Override
            public void onReadByte(int id, byte value) {
                debugMesg("Byte id " + String.format("0x%08X", id) + " value " + String.format("0x%02X", value) + "\n");
            }

            @Override
            public void onReadUnsignedByte(int id, int value) {
                debugMesg("UnsignedByte id " + String.format("0x%08X", id) + " value " + String.format("0x%02X", value) + "\n");
            }

            @Override
            public void onReadShort(int id, short value) {
                debugMesg("Short id " + String.format("0x%08X", id) + " value " + String.format("0x%04X", value) + "\n");
            }

            @Override
            public void onReadUnsignedShort(int id, int value) {
                debugMesg("UnsignedShort id " + String.format("0x%08X", id) + " value " + String.format("0x%04X", value) + "\n");
            }

            @Override
            public void onReadInt(int id, int value) {
                debugMesg("Int id " + String.format("0x%08X", id) + " value " + String.format("0x%08X", value) + "\n");
            }

            @Override
            public void onReadFloat(int id, float value) {
                debugMesg("Float id " + String.format("0x%08X", id) + " value " + value + "\n");
            }

            @Override
            public void onReadLong(int id, long value) {
                debugMesg("Long id " + String.format("0x%08X", id) + " value " + String.format("0x%016X", value) + "\n");
            }

            @Override
            public void onReadBoolean(int id, boolean value) {
                debugMesg("Boolean id " + String.format("0x%08X", id) + " value " + value + "\n");

            }

            @Override
            public void onReadShortArray(int id, short[] data) {
                debugMesg("ShortArray id " + String.format("0x%08X", id) + " ");
                for (int i = 0; i < data.length; i++)
                    debugMesg(" " + String.format("0x%04X", data[i]));
                debugMesg("\n");
            }

            @Override
            public void onReadIntArray(int id, int[] data) {
                debugMesg("IntArray id " + String.format("0x%08X", id) + " value ");
                for (int i = 0; i < data.length; i++)
                    debugMesg(" " + String.format("0x%08X", data[i]));
                debugMesg("\n");
            }

            @Override
            public void onReadFloatArray(int id, float[] data) {
                debugMesg("FloatArray id " + String.format("0x%08X", id) + " value ");
                for (int i = 0; i < data.length; i++)
                    debugMesg(" " + data[i]);
                debugMesg("\n");
            }

            @Override
            public void onReadLongArray(int id, long[] data) {
                debugMesg("LongArray id " + String.format("0x%08X", id) + " value ");
                for (int i = 0; i < data.length; i++)
                    debugMesg(" " + String.format("0x%016X", data[i]));
                debugMesg("\n");
            }

            @Override
            public void onReadString(int id, String value) {
                debugMesg("String id " + String.format("0x%08X", id) + " value " + value + "\n");
            }

            @Override
            public void onReadByteArray(int id, byte[] data) {
                debugMesg("ByteArray id " + String.format("0x%08X", id) + " value ");
                for (int i = 0; i < data.length; i++)
                    debugMesg(" " + String.format("0x%02X", data[i]));
                debugMesg("\n");
            }
        };

        {
            FileInputStream fis = new FileInputStream("asus.raw");
            AsusFormatReader afr = new AsusFormatReader(fis, 100000);
            afr.setOnReadListener(onReadListener);

            afr.read(fis);
            fis.close();
        }
        debugMesg("\n\n-------------------------------------\n\n");
        {
            FileInputStream fis = new FileInputStream("asus.raw");
            AsusFormatReader afr = new AsusFormatReader(fis, 100000);
            for (AsusFormatReader.Item item = afr.readItem(); null != item; item = afr.readItem(fis))
                debugMesg(item + "\n");
            fis.close();
        }
    }
}
