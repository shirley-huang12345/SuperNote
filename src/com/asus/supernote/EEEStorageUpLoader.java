/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asus.supernote;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;


import javax.net.ssl.SSLHandshakeException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import android.util.Log;




public class EEEStorageUpLoader {
	private String mParentFolderID = "";
	private String mTokenString;
	private String mWebRelayURL = "";
	private String mFileID = "";
	private String mTransID="";
	private String mOffset="";
	public String mSHAValue="";
	public String mCurrentSHAValue="";
	public String mReturnFileID="";
	
    private String mSid = "32012895";
    
	private static final String TAG = "WebStorageAPI";
	private WebStorageHelper mHelper = new WebStorageHelper();

	
	public String getWebRelayUrl() {
	    return this.mWebRelayURL;
	}
	
	public void setWebRelayUrl(String url) {
	    mWebRelayURL = url;
	}
   
    public void setParentFolderID(String folderid) {
        mParentFolderID = folderid;
    }
    
    public void setTokenString(String token) {
        mTokenString = token;
    }    
    
    public void setFileID(String fileid) {
        mFileID = fileid;
    }
    /**
     * Upload file.
     * @param uploadFile
     * @return  1,upload success;
     *          -1,User's storage space has been exhausted;
     *          -2,The user's account has been frozen or closed;
     *          0,other error
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
	public int uploadOneFile(String uploadFile,String attribute) throws ParserConfigurationException, SAXException, IOException
	{
		String cookies = "";
	
		Log.i(TAG,"Enter uploadOneFile" + mFileID + " mParentFolderID is  " + mParentFolderID);
		Hashtable<String, String> querystring = new Hashtable<String, String>();
		
		if(attribute != null && attribute != "")
		{
			querystring.put("at", attribute);
		}
		querystring.put("fi", mFileID);
		querystring.put("pa", mParentFolderID);
		querystring.put("d", uploadFile.substring(uploadFile.lastIndexOf(File.separator)+1));
		querystring.put("u", "");

		/*Uploading*/
		String outBackData = UploadFileEx(uploadFile,
				"http://" + mWebRelayURL + "/webrelay/directupload/" + mTokenString + "/", "", "",
                querystring, cookies);
        /*Is upload success?*/
		if(outBackData==null || outBackData.length() == 0) {
            Log.i(TAG,"upload response is NULL.");
            return 0;
		}
		/*Parse string to xml*/
        StringReader sr = new StringReader(outBackData);  
        InputSource is = new InputSource(sr);  
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder = factory.newDocumentBuilder();  
        Document doc = builder.parse(is);
        doc.normalize();
        Element root = doc.getDocumentElement();
        
        Log.i(TAG,"Before exit uploadOneFile");
        /*Get status*/
        String status = mHelper.getNodeValue(root,"status");
        /*Status code 0,success;other code fail*/
        if(status.equals("0")) {
            return 1;
        }else if(status.equals("224")){
            Log.i(TAG,"User's storage space has been exhausted.");
            return -1;
        }else if(status.equals("226")){
            Log.i(TAG,"The user's account has been frozen or closed.");
            return -2;
        }else {
            Log.i(TAG,"mParentFolderID =" +mParentFolderID);
            Log.i(TAG,outBackData);
            return 0;
        }
        
	}
	
	/**
	 * Exec upload file
	 * @param uploadfile Upload file path+name
	 * @param url  The uploadURL
	 * @param fileFormName 
	 * @param contenttype 
	 * @param querystring  Upload param
	 * @param cookies
	 * @return
	 */
	private String UploadFileEx(String uploadfile, String url,
			String fileFormName, String contenttype, Hashtable<String, String> querystring,
			String cookies) {
		String backdata = "";
		
		try {
			if((fileFormName == "") || (fileFormName.length() == 0)) {
				fileFormName = "file";
			}
			if((contenttype == "") || (contenttype.length() == 0)) {
				contenttype = "application/octet-stream";
			}
			
			URL uri = null;
			try {
				uri = new URL(url);
			}
			catch (MalformedURLException e) {
				Log.e(TAG, "MalformedURLException");
			}
			
			if(uri != null) {
				try {
					HttpURLConnection urlCon = (HttpURLConnection)uri.openConnection();
					urlCon.setDoOutput(true);
					urlCon.setDoInput(true);
					urlCon.setRequestMethod("POST");
					urlCon.setUseCaches(false);
					urlCon.setReadTimeout(300000);
					urlCon.setInstanceFollowRedirects(true);
					
					String boundary = "----------" + String.format("%x", System.currentTimeMillis());
					urlCon.setRequestProperty("Cookie", cookies);
					urlCon.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
					
					/*Build up the post message header*/
					StringBuffer sb = new StringBuffer();
					if(querystring != null) {
						Enumeration keys = querystring.keys();
						while(keys.hasMoreElements()) {
							String key = (String)keys.nextElement();
							String value = (String)querystring.get(key);
							
							sb.append("--");
	                        sb.append(boundary);
	                        sb.append("\r\n");
	                        sb.append("Content-Disposition: form-data; name=\"");
	                        sb.append(key);
	                        sb.append("\"");
	                        sb.append("\r\n\r\n");
	                        sb.append(value);
	                        sb.append("\r\n");
						}
					}
					sb.append("--");
	                sb.append(boundary);
	                sb.append("\r\n");
	                sb.append("Content-Disposition: form-data; name=\"");
	                sb.append(fileFormName);
	                sb.append("\"; filename=\"");
	                sb.append(uploadfile.substring(uploadfile.lastIndexOf(File.separator)+1));
	                sb.append("\"");
	                sb.append("\r\n");
	                sb.append("Content-Type: ");
	                sb.append(contenttype);
	                sb.append("\r\n");
	                sb.append("\r\n");
	                
	                String postHeader = sb.toString();
	                Log.i(TAG, "postHeader is" + postHeader);
	                String boundary1 = "\r\n--" + boundary + "\r\n";
	                File file = new File(uploadfile);
	                long length = sb.length() + boundary1.length() + file.length();
	                urlCon.setRequestProperty("Content-Length",String.valueOf(length));
	                DataOutputStream out = new DataOutputStream(urlCon.getOutputStream());
	                out.writeBytes(postHeader);
	                
	                int bytesRead = 0;
	                int progressValue = 0;
	                int totalRead = 0;
	                byte bytesBuffer[] = new byte[4*1024];
	                
	                try {
	                	FileInputStream stream = new FileInputStream(uploadfile);
	                	BufferedInputStream buffer = new BufferedInputStream(stream);
	                	while((bytesRead = buffer.read(bytesBuffer, 0, 1024)) != -1) {
	                		totalRead += bytesRead;
	                		out.write(bytesBuffer, 0, bytesRead);
	                		progressValue = totalRead * 100 / (int)file.length();
	                	    if(Thread.interrupted())
	    			        {
	    			        	Log.v("wendy","UploadFileEx thread interrupted 231 ");
	    			        	return null;
	    			        }
	                	}
	                	
	                	out.writeBytes(boundary1);
	                	out.flush();
	    				out.close();
	    				
	    			     if(Thread.interrupted())
	    			        {
	    			        	Log.v("wendy","UploadFileEx thread interrupted 242 ");
	    			        	return null;
	    			        }
	    				/*Get returned data*/
	    				BufferedReader reader = new BufferedReader(new InputStreamReader(urlCon.getInputStream(),"UTF-8"));
	    				
	    				   if(Thread.interrupted())
	    			        {
	    			        	Log.v("wendy","UploadFileEx thread interrupted---- 250  ");
	    			        	return null;
	    			        }
	    				String inputLine = null;
	    				/*Processing the returned data */
	    				while((inputLine = reader.readLine()) != null) {
	    					backdata += inputLine;
	    				}
	    				reader.close();
	    				urlCon.disconnect();
	    				
	    				progressValue = 100;
	                } catch(FileNotFoundException e) {
	                	Log.e(TAG, "FileNotFoundException");
	                }
				} catch(IOException e) {
					Log.e(TAG,"IOException");
				}
			}
		} catch(Exception e) {
		    Log.e(TAG,"Exception");
		}
		return backdata;
	}

    
    
    public int initBinaryUpload(String uploadFile,String attribute, String fileShaValue)throws Exception
    {
		String cookies = "";
		File file=new File(uploadFile);
		long filesize = file.length();
		
		Log.i(TAG,"Enter initBinaryUpload" + mFileID + " mParentFolderID is  " + mParentFolderID);
		Hashtable<String, String> querystring = new Hashtable<String, String>();
		querystring.put("at", attribute);
		querystring.put("pa", mParentFolderID);
		querystring.put("dis", mSid);
		//querystring.put("sg", mCurrentSHAValue);//SYSTEM.RESERVED
		//querystring.put("sg", "system.reserved");
		querystring.put("fs", Long.toString(filesize));
		querystring.put("na", mHelper.encodeBase64(uploadFile.substring(uploadFile.lastIndexOf(File.separator)+1)));
		if(mFileID != null && mFileID!="")
		{
			querystring.put("sg", "system.reserved");
			querystring.put("fi", mFileID);
		}
		else
		{
//			mCurrentSHAValue = mHelper.getFileSha1(uploadFile);
			querystring.put("sg", fileShaValue);
		}
		//querystring.put("na", uploadFile.substring(uploadFile.lastIndexOf(File.separator)+1));
		
		postQureyString(uploadFile,
				"http://" + mWebRelayURL + "/webrelay/initbinaryupload/" +"?tk="+ mTokenString ,
				"file",
				"application/octet-stream",
                querystring,
                cookies);
		
		Log.i(TAG,"Exit initBinaryUpload" + mFileID + " mParentFolderID is  " + mParentFolderID);
		
		return 0;
    }
    	
    public void postQureyString(String uploadfile, String url,
			String fileFormName, String contenttype, Hashtable<String, String> querystring,
			String cookies)throws Exception
    {
    	Log.i(TAG, "Enter postQureyString");
    	String backXmlData="";
    	String temp = "";

		/*Build up the post message header*/
		StringBuffer sbtemp = new StringBuffer();
		if(querystring != null) {
			Enumeration keys = querystring.keys();
			while(keys.hasMoreElements()) {
				String key = (String)keys.nextElement();
				String value = (String)querystring.get(key);
				
				sbtemp.append("&");					
				sbtemp.append(key);
				sbtemp.append("=");
				sbtemp.append(URLEncoder.encode(value, "UTF-8"));
			}
		}
		temp =  url + sbtemp.toString();
    	
		HttpURLConnection urlCon = setUPHttpURLConnection(temp);
		//HttpURLConnection urlCon = setUPHttpURLConnection("http://w01.asuswebstorage.com/webrelay/initbinaryupload/?tk=60obkqoyl7w96&fs=52&at=<FileSHA>f1a24ac10c7d64a75cce89e9093876fed8304c2fd4010013cc9248a11de7b06166b981b29eca0b54b162ecfd2346747ae88941844d3e6c7d83b139bfaa14187a</FileSHA><LastChangeTime>1331102988179</LastChangeTime><PageIsDelete>false</PageIsDelete><LastNoteBookID>1325331774305</LastNoteBookID><NoteBookTitle>Tutorial for phone</NoteBookTitle><IsBookmark>false</IsBookmark><DefaultBackground>-1</DefaultBackground><DefaultLine>0</DefaultLine><Category>false</Category>&na=Nzg5LnR4dA==&sg=system.reserved&pa=30637373&fi=254542557&dis=50000");
		//HttpURLConnection urlCon = setUPHttpURLConnection("http://w03.asuswebstorage.com/webrelay/initbinaryupload/?tk=am9t3wxqa2qqbz&fs=60&at=<zidingyi>798</zidingyi>&na=MjM0NTY3OC50eHQ=&sg=91260d34638c6303a19fdcb43d269e0c1e96d6221d2be835d309782b820d460ed85f84e83f6e6ca93b755df21c1f7a94de02ea42940c70e3c0d407754c2c3330&pa=1883328&dis=50000");
    	if(urlCon == null)
    	{
    		throw new WebStorageException(WebStorageException.OTHER_ERROR);//error happen
    	}
    	
    	// BEGIN: Better
    	InputStream input = null;
	    try {
	    	input = urlCon.getInputStream();
	    } catch (Exception e) {
	    	InputStream errStream = urlCon.getErrorStream();
	    	String err = "";
	    	if (errStream != null) {
		    	BufferedReader errReader = new BufferedReader(new InputStreamReader(errStream,"UTF-8"));
		    	String line = null;
				while((line = errReader.readLine()) != null) {
					err += line;
				}
				errReader.close();
	    	}
			if (err.contains("Validate token fail, Error Code:2")) {
				throw new WebStorageException(WebStorageException.LOGIN_ERROR);
			} else {
				throw e;
			}
	    }
	    // END: Better
    	
		Log.i(TAG, "befro Enter BufferedReader");
		BufferedReader reader = new BufferedReader(new InputStreamReader(input,"UTF-8"));
		//BufferedReader reader;
		//InputStreamReader reader = new InputStreamReader(urlCon.getInputStream(),"UTF-8")
		
		Log.i(TAG, "befro Enter after get BufferedReader");
		
		if(Thread.interrupted())
		{
			throw new WebStorageException(WebStorageException.OTHER_ERROR);//error happen
		}
		
		String inputLine = null;
		while((inputLine = reader.readLine()) != null) {
			backXmlData += inputLine;
		}
		
		reader.close();
		//in.close();
		//tempin.close();
		urlCon.disconnect();
		
		checkXmlStatus(backXmlData);
		
		Document doc = mHelper.parseStringToXml(backXmlData);
		Element root = doc.getDocumentElement();
		
		mTransID = mHelper.getNodeValue(root,"transid");
		mOffset = mHelper.getNodeValue(root,"offset");
		mSHAValue = mHelper.getNodeValue(root,"latestchecksum");
		mReturnFileID= mHelper.getNodeValue(root,"fileid");

		Log.i(TAG,"backXmlData is " + backXmlData);
    }
    
    private void checkXmlStatus(String backXmlData)throws Exception
    {
		if(backXmlData.length() == 0)   
		{
			Log.i(TAG,"backXmlData is Null.");
			throw new WebStorageException(WebStorageException.OTHER_ERROR);//error happen
		}
		
		Document doc = mHelper.parseStringToXml(backXmlData);
		Element root = doc.getDocumentElement();
		
		String status = mHelper.getNodeValue(root,"status");
		if(status == null || status=="")
		{
			throw new WebStorageException(WebStorageException.OTHER_ERROR);//error happen
		}
		int statusint = Integer.parseInt(status);
		if(statusint == 0)
		{
			return;
		} else if (statusint == 2) { // Better
			throw new WebStorageException(WebStorageException.LOGIN_ERROR);
		}
		else if(statusint == 250)
		{
			throw new WebStorageException(WebStorageException.UPLOAD_SHA_ERROR);
		}
		else if(statusint == 224)
		{
			throw new WebStorageException(WebStorageException.USER_ACCOUNT_SPACE_FULL);//error happen
		}
		else if(statusint == 226)
		{
			throw new WebStorageException(WebStorageException.USER_ACCOUNT_FROZEN_OR_CLOSED);//error happen
		}
		else
		{
			throw new WebStorageException(WebStorageException.OTHER_ERROR);//error happen
		}
    }
    
    public void postResumeBinaryUpload(String transcationID,String uploadfile)throws Exception
    {
    	String temp = "http://" + mWebRelayURL + "/webrelay/resumebinaryupload/" +
    	    	"?tk="+ URLEncoder.encode(mTokenString, "UTF-8")+"&" +
    	    	"dis="+ mSid+"&"+
    	    	"tx="+ URLEncoder.encode(transcationID, "UTF-8");
    	
    	HttpURLConnection urlCon = setUPHttpURLConnection(temp);
    	if(urlCon == null)
    	{
    		throw new WebStorageException(WebStorageException.OTHER_ERROR);//error happen
    	}
    	
		try
		{
            DataOutputStream out = new DataOutputStream(urlCon.getOutputStream());

            doUpload(out,uploadfile);
            
            // BEGIN: Better
        	InputStream input = null;
    	    try {
    	    	input = urlCon.getInputStream();
    	    } catch (Exception e) {
    	    	BufferedReader errReader = new BufferedReader(new InputStreamReader(urlCon.getErrorStream(),"UTF-8"));
    	    	String err = "";
    	    	String line = null;
    			while((line = errReader.readLine()) != null) {
    				err += line;
    			}
    			errReader.close();
    			if (err.contains("Validate token fail, Error Code:2")) {
    				throw new WebStorageException(WebStorageException.LOGIN_ERROR);
    			} else {
    				throw e;
    			}
    	    }
    	    // END: Better
			
            BufferedReader reader = new BufferedReader(new InputStreamReader(input,"UTF-8"));
				
			if(Thread.interrupted())
             {
				throw new WebStorageException(WebStorageException.OTHER_ERROR);//error happen
             }
				
			String backXmlData = "";
			String inputLine = null;
			while((inputLine = reader.readLine()) != null) {
				backXmlData += inputLine;
			}
					
			reader.close();
			urlCon.disconnect();
			
			checkXmlStatus(backXmlData);
			
		}catch(SSLHandshakeException e){
			Log.i(TAG,"SSLHandshakeException");
		    return ;
		}catch(SocketTimeoutException e){
            Log.i(TAG,"SocketTimeoutException");
            return;
        }catch(IOException e) {
			Log.e(TAG,"IOException");
			e.printStackTrace();
		}
    }
    
    public HttpURLConnection setUPHttpURLConnection(String tempurl)
    {
    	URL url = null;
    	try
    	{
    		url = new URL(tempurl);
    	}catch(Exception exc)
    	{
    		return null;
    	}
    	
		try {
			HttpURLConnection urlCon = (HttpURLConnection)url.openConnection();
			urlCon.setDoOutput(true);
			urlCon.setDoInput(true);
			urlCon.setRequestMethod("POST");
			urlCon.setUseCaches(false);
			urlCon.setInstanceFollowRedirects(true);
			urlCon.setReadTimeout(300000);

			urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			//String boundary = "----------" + String.format("%x", System.currentTimeMillis());
			//urlCon.setRequestProperty("Cookie", "");
			//urlCon.setRequestProperty("Content-Type", "binary/form-data; boundary=" + boundary);
			
			return urlCon;
		}catch(Exception exce)
		{
			return null;
		}
    }
    
    public int postFinshBinaryUpload()throws Exception
    {
    	String temp = "http://" + mWebRelayURL + "/webrelay/finishbinaryupload/" +
    	    	"?tk="+ URLEncoder.encode(mTokenString, "UTF-8")+"&" +
    	    	"dis="+ mSid+"&"+
    	    	"tx="+ URLEncoder.encode(mTransID, "UTF-8");
    	
    	if(mSHAValue !=null &&mSHAValue!="")
    	{
    		temp = temp + "&lsg="+URLEncoder.encode(mSHAValue, "UTF-8");;
    	}
    	
    	HttpURLConnection urlCon = setUPHttpURLConnection(temp);
    	if(urlCon == null)
    	{
    		return -1;//error happen
    	}
    	

		BufferedReader reader = new BufferedReader(new InputStreamReader(urlCon.getInputStream(),"UTF-8"));
		if(Thread.interrupted())
        {
           Log.v("wendy","postData: thread is interrupted 180" + Thread.currentThread().getId());	 
           reader.close();
			urlCon.disconnect();
           return -1;
        }
		
		String backXmlData = "";
		String inputLine = null;
		while((inputLine = reader.readLine()) != null) {
			backXmlData += inputLine;
		}
		Log.i(TAG,"backXmlData is " + backXmlData);
		
		reader.close();
		urlCon.disconnect();
		
		checkXmlStatus(backXmlData);
		
		return 1;
    }
    
    public void doUpload(DataOutputStream out, String uploadfile)
    {
        int bytesRead = 0;
        int progressValue = 0;
        int totalRead = 0;
        byte bytesBuffer[] = new byte[8*1024];
        int offset = 0;
        if(mOffset!= null && mOffset!="")
        {
        	try
        	{
        		offset=Integer.parseInt(mOffset);
        	}catch(Exception exc)
        	{
        		offset = 0;
        	}
        }
        
        File file = new File(uploadfile);
        
        try {
        	FileInputStream stream = new FileInputStream(uploadfile);
        	BufferedInputStream buffer = new BufferedInputStream(stream);
        	while((bytesRead = buffer.read(bytesBuffer, offset, 8*1024)) != -1) {
        		offset = 0;
        		totalRead += bytesRead;
        		out.write(bytesBuffer, 0, bytesRead);
        		progressValue = totalRead * 100 / (int)file.length();
        	    if(Thread.interrupted())
		        {
		        	Log.v("wendy","UploadFileEx thread interrupted 231 ");
		        	return ;
		        }
        	}
        	
        	out.flush();
			out.close();
			
			
			progressValue = 100;
        } catch(Exception e) {
        	Log.e(TAG, "Exception");
        }
        return ;
    }
    
	/**
	 * Exec upload file
	 * @param uploadfile Upload file path+name
	 * @param url  The uploadURL
	 * @param fileFormName 
	 * @param contenttype 
	 * @param querystring  Upload param
	 * @param cookies
	 * @return
	 */
	public int upLoadOneFileBinary(String uploadFile,String attribute,String  SHAValue, String fileShaValue)throws Exception
	{	
		initBinaryUpload(uploadFile,attribute, fileShaValue);
    	
    	if(mFileID != null && mFileID!="")
    	{
	    	if(mSHAValue!= null &&mSHAValue !="")
	    	{
	    		if(SHAValue!= null && SHAValue!="")
	    		{
	    			//if pass SHAValue, we should check
		    		if(!SHAValue.equals(mSHAValue))
		    		{
		    			throw new WebStorageException(WebStorageException.UPLOAD_INIT_SHAVALUE_NOT_MATCH);//error happen
		    		}
	    		}
	    	}
	    	if(mReturnFileID!=null &&mReturnFileID.equals(mFileID))
	    	{
	    		//never come here
	    		return 1;
	    	}
    	}
    	else if(mReturnFileID!=null && mReturnFileID!="")
    	{
    		return 1;
    	}
    	Log.i(TAG,"before postResumeBinaryUpload");
		postResumeBinaryUpload(mTransID,uploadFile);
		Log.i(TAG,"after postResumeBinaryUpload");
		return postFinshBinaryUpload();  
	}
	
	// BEGIN: Better
	public boolean validateUploadStatus(String uploadFile, String attribute, String fileShaValue) throws Exception {
		if ((fileShaValue != null) && (fileShaValue != "")) {
			initBinaryUpload(uploadFile, attribute, fileShaValue);
	    	
			if((mSHAValue != null) && fileShaValue.equals(mSHAValue)) {
				Log.v("better", "validateUploadStatus success. file: " + uploadFile + ", local SHA: " + fileShaValue + ", remote SHA: " + mSHAValue);
				return true;
			} else {
				Log.v("better", "validateUploadStatus failed. file: " + uploadFile + ", local SHA: " + fileShaValue + ", remote SHA: " + mSHAValue);
				return false;
			}
		} else {
			Log.v("better", "validateUploadStatus success. file: " + uploadFile + ", local SHA: " + fileShaValue);
			return true;
		}
	}
	// END: Better
}
