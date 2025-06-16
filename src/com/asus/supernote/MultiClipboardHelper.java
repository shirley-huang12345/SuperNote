package com.asus.supernote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.content.ClipboardManager;
import android.widget.EditText;
import android.widget.TextView;

/*
 * author:mars_li
 * Description: for AMAX compile ,because these method just for A80,A68
 */
public class MultiClipboardHelper {
	public static final int SUPPORT_CLIPTYPE_NONE = 0x0;
	public static final int SUPPORT_CLIPTYPE_TEXT = 0x01;
	public static final int SUPPORT_CLIPTYPE_IMAGE = 0x02;
	public static final int SUPPORT_CLIPTYPE_FILE = 0x04;
	public static final int SUPPORT_CLIPTYPE_ALL = SUPPORT_CLIPTYPE_TEXT
			| SUPPORT_CLIPTYPE_IMAGE | SUPPORT_CLIPTYPE_FILE;
	
	
	public static class ProxyListener implements java.lang.reflect.InvocationHandler {

	    public ProxyListener() {
	    	
	    }    

	    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
	    {

	    	try{
	    		if (m.getName().equals("onClipDataSelected")) {

	    			onClipDataSelected(args[0], args[1]);

	    		}
	    		else if(m.getName().equals("onClipboardOpen")){
	    			onClipboardOpen();
	    		}
	    		else if(m.getName().equals("onClipboardClose")){
	    			onClipboardClose();
	    		}

	    	} catch (Exception e) {

	    		throw new RuntimeException("unexpected invocation exception: " +

	                                  e.getMessage());
	    	} 

	       return null;

	    }
	    
	    void onClipDataSelected(Object mimeType, Object data){
	    	
	    }
	    
	    void onClipboardOpen(){
	    	
	    }
	    
	    void onClipboardClose(){
	    	
	    }
	}
	
	public static void setSupportInputType(EditText editText, int type){
		Method m = null;
		try {
			m = TextView.class.getMethod("setSupportInputType", int.class);
			if (m != null) {
				m.invoke(editText, type);
			}
		} catch (NoSuchMethodException e) {
		} catch (Exception e) {
		}
	}
	
	public static void openClipboard(ClipboardManager clipManager, int type, InvocationHandler invokeInterface){
		Method m = null;
		try {
			Class<?> className = Class.forName("android.content.ClipboardManager$OnClipboardSelectedListener");
			if(className == null){
				return;
			}
			Object proxyInterface = Proxy.newProxyInstance(className.getClassLoader(),new Class[]{className} , invokeInterface);
			m = ClipboardManager.class.getMethod("openClipboard",int.class, className);
			if (m != null) {
				m.invoke(clipManager, type, proxyInterface);
			}
		} catch (NoSuchMethodException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Boolean isClipboardOpened(ClipboardManager clipManager){
		Method m = null;
	
		try {
			m = ClipboardManager.class.getMethod("isClipboardOpened");
			if (m != null) {
				return ((Boolean)m.invoke(clipManager)).booleanValue();
			}
		} catch (NoSuchMethodException e) {
		} catch (Exception e) {
		}
		return false;
	}
	
	public static void closeClipboard(ClipboardManager clipManager){
		Method m = null;
		try {
			m = ClipboardManager.class.getMethod("closeClipboard");
			if (m != null) {
				m.invoke(clipManager);
			}
		} catch (NoSuchMethodException e) {
		} catch (Exception e) {
		}
	}
}
