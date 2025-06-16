package com.asus.supernote.editable.attacher;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

public abstract class BitmapAttacher implements Attacher {
    private static final int MAX_BITMAP_SIZE = 1920 * 1200;
    private static final String TAG = "BitmapAttacher";

    protected int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

        int roundedSize;

        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        }
        else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength),

                Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        }
        else if (minSideLength == -1) {
            return lowerBound;
        }
        else {
            return upperBound;
        }
    }

    public static Bitmap saveBitmap(File srcFile, File destFile, Bitmap.CompressFormat format) {
        if (srcFile == null || destFile == null) {
            return null;
        }

        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(srcFile.getPath(), option);
        Bitmap b = null;
        double w = option.outWidth;
        double h = option.outHeight;

        double ratio = ((w * h) / MAX_BITMAP_SIZE);
        try {
            if (ratio > 1.0f) {
                ratio = Math.sqrt(ratio);
                w = w / ratio;
                h = h / ratio;
                int scale = 1;
                while ((option.outWidth * option.outHeight) * (1 / Math.pow(scale, 2)) > MAX_BITMAP_SIZE) {
                    scale++;
                }
                scale--;
                option.inSampleSize = scale;
                option.inJustDecodeBounds = false;
                Bitmap t = BitmapFactory.decodeFile(srcFile.getPath(), option);

                w = option.outWidth;
                h = option.outHeight;
                ratio = ((w * h) / MAX_BITMAP_SIZE);
                ratio = Math.sqrt(ratio);
                w = w / ratio;
                h = h / ratio;
                b = Bitmap.createScaledBitmap(t, (int) w, (int) h, true);
                Log.d("[91]", "bw = " + b.getWidth() + ", bh = " + b.getHeight());
                t.recycle();
                t = null;

            }
            else {
                b = BitmapFactory.decodeFile(srcFile.getPath());
            }
        }
        catch (OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] Insert Bitmap failed !!!");
        }

        Bitmap rotateBitmap = null;
        if (b != null) {
        	//Begin Allen adjust picture orientation
            Matrix matrix = new Matrix();
            float rotation = rotationForImage(srcFile.getPath());
            if (rotation != 0f) {
                 matrix.preRotate(rotation);
                 rotateBitmap = Bitmap.createBitmap(
                         b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                 
                 b.recycle();
                 b = null;
            }
            else{
            	rotateBitmap = b;
            }
        	//End Allen
        	
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));
                rotateBitmap.compress(format, 100, bos);
                bos.close();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }      
        return rotateBitmap;
    }

    //emmanual to fix bug 424283
    public static Bitmap saveStampShape(File srcFile, File destFile, Bitmap.CompressFormat format) {
        if (srcFile == null || destFile == null) {
            return null;
        }

        Bitmap b = null;
        try {
            b = BitmapFactory.decodeFile(srcFile.getPath());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));
            b.compress(format, 100, bos);
            bos.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }
    
    //Begin Emmanual
    public static Bitmap saveBitmap(File srcFile, File destFile, Bitmap.CompressFormat format, int screenWidth, int screenHeight) {
        if (srcFile == null || destFile == null) {
            return null;
        }

        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = 1;
        option.inJustDecodeBounds = false;
		Bitmap rotateBitmap = null;

		// emmanual to scale inserted stamp
		try {
			Bitmap origin = BitmapFactory.decodeFile(srcFile.getPath(), option);
			Bitmap temp = null;
            Matrix matrix = new Matrix();
            float rotation = rotationForImage(srcFile.getPath());
            if (rotation != 0f) {
                 matrix.preRotate(rotation);
                 temp = Bitmap.createBitmap(
                         origin, 0, 0, origin.getWidth(), origin.getHeight(), matrix, true);                 
                 origin.recycle();
                 origin = null;
            }else{
            	temp = origin;
            }
            
			double tw = temp.getWidth();
			double th = temp.getHeight();
			double ratio = Math.max(tw / screenWidth / 0.8, th / screenHeight
			        / 0.85);
			ratio = Math.max(ratio, Math.sqrt((tw * th) / MAX_BITMAP_SIZE));
			if (ratio > 1) {
				int scale = 1;
				while ((option.outWidth * option.outHeight)
				        * (1 / Math.pow(scale, 2)) > MAX_BITMAP_SIZE) {
					scale++;
				}
				scale--;
				option.inSampleSize = scale;
				option.inJustDecodeBounds = false;
				//begin smilefish
				int width = (int) (tw / ratio);
				int height = (int) (th / ratio);
				if(width <= 0)
					width = 1;
				if(height <= 0)
					height = 1;
				//end smilefish
				rotateBitmap = Bitmap.createScaledBitmap(temp, width, height, true);
				temp.recycle();
				temp = null;
			} else {
				rotateBitmap = temp;
			}
		}
        catch (OutOfMemoryError e) {
            Log.w(TAG, "[OutOfMemoryError] Insert Bitmap failed !!!");
        }
		catch (Exception ex){
			
		}

        if (rotateBitmap != null) {        	
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));
                rotateBitmap.compress(format, 100, bos);
                bos.close();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }      
        return rotateBitmap;
    }
    //End Emmanual
    
    //Begin Allen
	public static float rotationForImage(String fileName) {
		try {
			ExifInterface exif = new ExifInterface(fileName);
			int rotation = (int) exifOrientationToDegrees(exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL));
			return rotation;
		} catch (IOException e) {
			Log.e(TAG, "Error checking exif", e);
		}
		return 0f;
	}

	private static float exifOrientationToDegrees(int exifOrientation) {
		if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
			return 90;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
			return 180;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
			return 270;
		}
		return 0;
	}    
	//End Allen
}
