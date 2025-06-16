package com.asus.supernote.fota;

public class DataReader {
	
	protected OnReadListener mOnReadListener = null;
	
	public void setOnReadListener(OnReadListener onReadListener) {
		mOnReadListener = onReadListener;
	}

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
	
}
