package com.asus.supernote;

public class WebStorageException extends Exception {	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int LOGIN_ERROR =1; 
	public static final int USER_ACCOUNT_FROZEN_OR_CLOSED =2;
	public static final int USER_ACCOUNT_SPACE_FULL =3;
	public static final int UPLOAD_SHA_ERROR =4;
	public static final int NETWORK_ERROR =5;
	public static final int TIMEOUT_ERROR =6;
	public static final int TOKEN_ERROR =7;
	public static final int OTHER_ERROR =8;
	public static final int RESPONSEXML_ERROR =8;
	public static final int CREATE_FOLDER_ERROR = 9;
	public static final int FILE_NOT_EXIST = 10;
	public static final int UPLOAD_INIT_SHAVALUE_NOT_MATCH = 11;
	
	public int getErrorKind()
	{
		try
		{
			String temp = this.getMessage();
			return Integer.parseInt(temp);
		}catch(Exception ex)
		{}
		return 5;
	}
	
	public WebStorageException(String message)
	{
		super(message);
	}
	
	public WebStorageException(int message)
	{
		super(Integer.toString(message));
	}
	
	/**	* 自定义错误信息和异常抛出
	* @param message
	* @param cause
	*/
	public WebStorageException(String message,Throwable cause)
	{
		super(message,cause);
	}
	
	/**
	* 只有异常抛出
	* @param cause
	*/
	public WebStorageException(Throwable cause)
	{
		super(cause);
	}
	
}
