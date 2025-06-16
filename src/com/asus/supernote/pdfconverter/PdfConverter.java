package com.asus.supernote.pdfconverter;

public class PdfConverter {
    static {
        System.loadLibrary("PdfConverter");
    }
    
    private static PdfConverter mInstance;
    
    private PdfConverter() {
        
    }
    
    public static PdfConverter getInstance() {
        if (mInstance == null) {
            mInstance = new PdfConverter();
        }
        return mInstance;
    }
    
    public native void Jpg2Pdf(String[] jpgPath, String pdfPath);
}
