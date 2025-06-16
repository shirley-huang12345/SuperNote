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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.asus.supernote.data.MetaData;

import java.math.BigDecimal;
import java.net.*;
import java.io.*;

import javax.net.ssl.SSLHandshakeException;
import javax.xml.parsers.ParserConfigurationException;

import android.util.Log;

public class EEEStorageChangeData {
    private String mServicePortalURL = "cloudportal01.asuswebstorage.com"; //smilefish fix bug 953698
    private String mApproxyURL = "https://";//"https://approxy.asuswebstorage.com/api/";
    private String mServiceGatewayURL = "";
    private String mInfoRelayURL = "";
    private String mWebRelayURL = "";
    private String mUser = "";
    private String mPassword = "";
    private String mTokenStr = "";
    private String mAccountExpireTime = "";

    private String mUserLanguage = "zh_TW";
    private String mEtagString = "";
    private long mLastActionTime = 0;
    private long mLastActionDelta = 0;
    private long mLastActionLength = 0;
    private int mTimeout = 300000;//5*60*1000
    private String mLastActionCommand = "";
	private String mProductName = android.os.Build.MODEL;
	
	private static final String TAG = "WebStorageAPI";
	private static final String ERROR_INTERRUPT = "interrupted";
	
	private WebStorageHelper mHelper = new WebStorageHelper();
	
	public int mPageSize = 20; //20 file a page
	public int mPageEnable = 1;//enable page return
    private String mProgKey = "F52DA86886C24DA18B9D138E5DCD9EDD";
    private String mSid = "32012895";
    
	public EEEStorageChangeData() {

	}
    public void setUser(String user) {
        this.mUser = user;
    }
    public String getUser() {
        return this.mUser;
    }
    public void setPassword(String password) {
        this.mPassword = password;
    }
    public String getPassword() {
        return this.mPassword;
    }
    
	public String getWebRelayURL() {
	    return this.mWebRelayURL;
	}
    public void setWebRelayURL(String url) {
        mWebRelayURL = url;
    }
    
    public String getToken() {
        return this.mTokenStr;
    }
    public void setToken(String token) {
        mTokenStr = token;
    }
    public String getAccountExpireTime() {
        return this.mAccountExpireTime;
    }
    public String getServiceGatewayURL() {
        return this.mServiceGatewayURL;
    }
    public void setServiceGatewayURL(String url) {
        mServiceGatewayURL = url;
    }
    
    public String getInfoRelayURL() {
        return this.mInfoRelayURL;
    }
    public void setInfoRelayURL(String url) {
        mInfoRelayURL = url;
    }
    
    public String getEtagString() {
        return this.mEtagString;
    }
    public void setEtagString(String url) {
        mEtagString = url;
    }
    
    public void setTimeout(int timeout)
    {
    	mTimeout = timeout;
    }
    
    public int getTimeout()
    {
    	return mTimeout;
    }
    
    private String getSignature(String method, String timeStamp, String nonce)
    {
    	String temp = "";

		temp = "nonce="+ nonce + 
				"&signature_method=" + method+
				"&timestamp=" + timeStamp  ;

    	try
    	{
    		temp = URLEncoder.encode(temp, "UTF-8");
    	}
    	catch(Exception e)
    	{
    		
    	}
    	temp.toUpperCase();
    	
    	temp = mHelper.getSignWordSHA1(mProgKey,temp);

    	try
    	{
    		temp = URLEncoder.encode(temp, "UTF-8");
    	}
    	catch(Exception e)
    	{
    		
    	}
    	
    	return temp;
    }
    
    /**
     * Post data to outside ,real connect to out.
     * @param rUrl The url to connect
     * @param xmlData   xml data which to be post
     * @return
     */
	private String postData(String rUrl, String xmlData) {
		rUrl = mApproxyURL + rUrl;
		String backXmlData = "";
		URL url = null;
		try {
			url = new URL(rUrl);
		}
		catch (MalformedURLException e) {
			Log.e(TAG, "MalformedURLException");
		}

		
		if(url != null) {
			try {
				String osVer = android.os.Build.VERSION.INCREMENTAL;
				if(osVer.length() > 50)
				{
					osVer = osVer.substring(0,50);
				}
				HttpURLConnection urlCon = (HttpURLConnection)url.openConnection();
				urlCon.setDoOutput(true);
				urlCon.setDoInput(true);
				urlCon.setRequestMethod("POST");
				urlCon.setUseCaches(false);
				urlCon.setInstanceFollowRedirects(true);

				urlCon.setRequestProperty("Cookie", 
											"Cookie=OMNISTORE_VER=1_0;" +
											"a=" + mLastActionCommand + 
											";t=" + mLastActionTime + 
											";d=" + mLastActionDelta + 
											";l=" + mLastActionLength + 
											";c=0;v=3.0;"+
											"EEE_MANU=ASUSTeK ComputerINC.;"+
											"EEE_PROD=" + mProductName +
											";OS_VER="+osVer+
											//";sid=50000");
											";sid="+mSid);
				
				if(rUrl.indexOf("/member/acquiretoken") != -1)
				{
					long currenttime = System.currentTimeMillis();

					urlCon.setRequestProperty("Authorization","signature_method=\"HMAC-SHA1\""
						+",timestamp="+"\""+String.valueOf(currenttime) +"\""//
						+",nonce="+"\""+String.valueOf(currenttime) +"\""//
						+",signature=\""+getSignature("HMAC-SHA1",String.valueOf(currenttime),String.valueOf(currenttime))+"\"");
				}
				
				urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlCon.setRequestProperty("Content-Length", String.valueOf(xmlData.length()));
                urlCon.setConnectTimeout(mTimeout);
                urlCon.setReadTimeout(mTimeout);
				urlCon.connect();
					  
			    if(Thread.currentThread().interrupted())
	             {
	                Log.v("wendy","postData: thread is interrupted 165 ~~~~" + Thread.currentThread().getId());	                	
	    			urlCon.disconnect();
	                return ERROR_INTERRUPT;
	             }
		
				DataOutputStream out = new DataOutputStream(urlCon.getOutputStream());
				out.writeBytes(xmlData);
				out.flush();
				out.close();
				/*Get data*/
				//add by wendy
			    if(Thread.interrupted())
	             {
	                Log.v("wendy","postData: thread is interrupted 172" + Thread.currentThread().getId());	                	
	    			urlCon.disconnect();
	                return ERROR_INTERRUPT;
	             }
			     //add by wendy
			    
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
			   
				BufferedReader reader = new BufferedReader(new InputStreamReader(input,"UTF-8"));
				
				if(Thread.interrupted())
	             {
	                Log.v("wendy","postData: thread is interrupted 180" + Thread.currentThread().getId());	 
	                reader.close();
	    			urlCon.disconnect();
	                return ERROR_INTERRUPT;
	             }
				
				String inputLine = null;
				while((inputLine = reader.readLine()) != null) {
					backXmlData += inputLine;
					//Log.i(TAG,"inputLine is "+inputLine);
				}
				
				
				if(backXmlData.length() == 0)   Log.i(TAG,"backXmlData is Null.");
				reader.close();
				urlCon.disconnect();
			}catch(SSLHandshakeException e){
			    return "SSLHandshakeException";
			}catch(SocketTimeoutException e){
                Log.i(TAG,"SocketTimeoutException");
                return "SocketTimeoutException";
            }catch(IOException e) {
				Log.e(TAG,"IOException");
				e.printStackTrace();
			}catch(Exception e) {
			    Log.e(TAG,"Exception");
			    e.printStackTrace();
			}
		}
		else {
			Log.e(TAG,"Url NULL");
		}
		
		return backXmlData;
	}
	
	/**
	 * Request Service Gateway.
	 * The web storage will allocate server automatic according to IP address,
	 * if the network connection is not smooth or username and password incorrect
	 * it will be deal at here.
	 */
	private int requestServiceGateway() {
		/*Get mServiceGatewayURL*/
		String mXML = "";
		String responseXML = "";
		long time = System.currentTimeMillis();
		
		mXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
					"<requestservicegateway>"+
						"<userid>" + mUser +"</userid>"+
						"<password>"+ mPassword + "</password>"+
						"<language>"+ mUserLanguage + "</language>"+
						"<service>1</service>"+
						"<time>" + String.valueOf(time) + "</time>" +
					"</requestservicegateway>";		
		responseXML = postData(mServicePortalURL+"/member/requestservicegateway/",mXML);
		
		if(responseXML == null || responseXML.length() <= 0) {
		    mServiceGatewayURL = "";
		    /*Internet connect problem*/
		    return -1;
		}
		if(responseXML == "SSLHandshakeException") {
		    Log.i(TAG,"SSLHandshakeException");
		    return -4;
        }
		if(responseXML == "SocketTimeoutException") {
            return -5;
        }
		if(responseXML== ERROR_INTERRUPT || Thread.interrupted()){
			Log.v("wendy", "requestServiceGateway is interrupted ~~~" + responseXML);
			return -5;
		}
		/*Userid & password may be not correct*/
		Document doc = mHelper.parseStringToXml(responseXML);
        if(doc == null){
            Log.i(TAG,"in requestServiceGateway parseStringToXml error");
            return 0;
        }
        doc.normalize();
        Element root = doc.getDocumentElement();
        String status = mHelper.getNodeValue(root,"status");
        if(!status.equals("0")) {

            return -2;  //Cann't find this username
        }
        mServiceGatewayURL = mHelper.getNodeValue(root,"servicegateway");
        if (mServiceGatewayURL == null || mServiceGatewayURL.length() <= 0) {
            return -2;
        }
        return 1;
	}
	
	/**
     * Request token
     * @return
     */
    public int requestToken() {
        /*Get tokenStr*/
    	mTokenStr ="";//reset token
    	
        String mXML;
        String responseXML = "";
        long time = System.currentTimeMillis();
        try {
                int errCode = requestServiceGateway();
                if(mServiceGatewayURL == null || mServiceGatewayURL.length() <= 0)
                    return errCode;
            mXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
            		"<aaa>"+
        				"<userid>" + mUser +"</userid>"+
        				"<password>"+mHelper.getPassWordMD5(mPassword)+"</password>"+
        				"<time>" + String.valueOf(time) + "</time>"+
					"</aaa>";
            
            for(int i = 0; i < 3; i++) 
            {
            	responseXML = postData(mServiceGatewayURL + "/member/acquiretoken/", mXML);

        		if(!checkRespondXml(responseXML))
        		{
        			if(i == 2)
        			{
        				return 0;
        			}
        		}else
        		{
        			break;
        		}
            }

 
            /*Parse String to XML*/
            Document doc = mHelper.parseStringToXml(responseXML);
            if(doc == null){
                Log.i(TAG,"in requestToken parseStringToXml error");
                return 0;
            }
            doc.normalize();
            Element root = doc.getDocumentElement();
            /*Get token value*/
            mTokenStr = mHelper.getNodeValue(root,"token");
            if(mTokenStr == null || mTokenStr.length() <= 0) {
                Log.e(TAG, "ChangeData-requestToken: Authentication Fail");
                return -3;
            }
            mInfoRelayURL = mHelper.getNodeValue(root,"inforelay");
            mWebRelayURL =  mHelper.getNodeValue(root,"webrelay");
            NodeList root2 = doc.getElementsByTagName("expire");
            if(root2.getLength()>0){
                mAccountExpireTime = root2.item(0).getFirstChild().getNodeValue();
            }
            Log.i(TAG,"Get token successfully");
            return 1;
        }
        catch(Exception e) {
            this.mTokenStr = "";
            e.printStackTrace();
        }
        return -3;
    }

	public String createFolder(String folderName,String parentFolderID)throws Exception
	{
		if(!checkToken())
		{
			throw new WebStorageException(WebStorageException.TOKEN_ERROR);
		}
		
        String mXML = "";
        String responseXML = "";
        
        mXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
        		"<create>"+
        			"<token>"+mTokenStr+"</token>"+
        			"<userid>"+mUser+"</userid>"+
        			"<parent>"+parentFolderID+"</parent>"+
        			"<isencrypted>0</isencrypted>"+
        			"<display>"+mHelper.encodeBase64(folderName)+"</display>"+
        			"<issharing>0</issharing>"+
    			"</create>";
        /*If createFolder no response try 3 times,if still no response return null*/

        for(int i = 0;i < 3;)
        {
            responseXML = postData(mInfoRelayURL + "/folder/create/", mXML);
            if(responseXML == null || responseXML.length() <= 0) {
                i++;
            }else {
                break;
            }
        }
        
		if(!checkRespondXml(responseXML))
		{
			throw new WebStorageException(WebStorageException.RESPONSEXML_ERROR);
		}
		
		Document doc = mHelper.parseStringToXml(responseXML);
		Element root = doc.getDocumentElement();
        String status = mHelper.getNodeValue(root,"status");
		if(status.equals("0"))
		{
			return mHelper.getNodeValue(root,"id");
		} else if (status.equals("2")) { // Better
			throw new WebStorageException(WebStorageException.LOGIN_ERROR);
		}
		
		throw new WebStorageException(WebStorageException.CREATE_FOLDER_ERROR);
    }

    
    /*checkToken
     * whether token is exist
     * false not exitst
     * true exist
     * */
	public Boolean checkToken()
	{
	    if(mTokenStr == null || mTokenStr.length() <= 0) {
                return false;
        }
		return true;
	}

	/*checkToken
     * whether responseXML is ok
     * false not ok
     * true ok
     * */
	public Boolean checkRespondXml(String responseXML)
	{
	    if(responseXML == ERROR_INTERRUPT){
        	Log.v("wendy","getFileId is interrupted ~~ 486");
        	return false;
        }
        
        if(responseXML == null || responseXML.length() <= 0) {
            Log.i(TAG, "responseXML is null or length <= 0");
            return false;
        }
        if(responseXML == "SocketTimeoutException") {
            Log.i(TAG,"SocketTimeoutException");
            return false;
        }
        return true;
	}

	/*getMySyncFolderID
     * get my sync folder id
     * use info relay api
     * 
     * */
	public String getMySyncFolderID()throws Exception
	{
		if(!checkToken())
		{
			throw new WebStorageException(WebStorageException.TOKEN_ERROR);
		}
		
		String mXML = "";
        String responseXML = "";
        mXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
				"<getmysyncfolder>"+
					"<userid>" + mUser + "</userid>"+
					"<token>" + mTokenStr + "</token>"+
				"</getmysyncfolder>";

        responseXML = postData(mInfoRelayURL + "/folder/getmysyncfolder/", mXML);

		if(!checkRespondXml(responseXML))
		{
			throw new WebStorageException(WebStorageException.RESPONSEXML_ERROR);
		}

		Document doc = mHelper.parseStringToXml(responseXML);
		Element root = doc.getDocumentElement();
        String status = mHelper.getNodeValue(root,"status");
		if(status.equals("0"))
		{
			return mHelper.getNodeValue(root,"id");
		} else if (status.equals("2")) { // Better
			throw new WebStorageException(WebStorageException.LOGIN_ERROR);
		}
		
		return "";
	}
	
	
	public Node getEntryInfo(String ID,int isFolder)throws Exception
	{
		if(!checkToken())
		{
			throw new WebStorageException(WebStorageException.TOKEN_ERROR);
		}
		
		String mXML = "";
        String responseXML = "";
        mXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
				"<getentryinfo>"+
					"<token>" + mTokenStr + "</token>"+
					"<isfolder>" + isFolder + "</isfolder>"+
					"<entryid>" + ID + "</entryid>"+
				"</getentryinfo>";

        responseXML = postData(mInfoRelayURL + "/fsentry/getentryinfo/", mXML);

		if(!checkRespondXml(responseXML))
		{
			throw new WebStorageException(WebStorageException.RESPONSEXML_ERROR);
		}		
		
		Log.i(TAG,"responseXML" + responseXML);
		
		Document doc = mHelper.parseStringToXml(responseXML);
		Element root = doc.getDocumentElement();
		// BEGIN: Better
        String status = mHelper.getNodeValue(root,"status");
        if (status.equals("2")) {
			throw new WebStorageException(WebStorageException.LOGIN_ERROR);
		}
		// END: Better
		NodeList attNodeList = doc.getElementsByTagName("attribute");
		
		if(attNodeList.getLength() > 0)
		{
			return attNodeList.item(0);
		}
        
		return null;
	}
	
	//begin  darwin
	
	public Node getEntryInfoFolder(String ID,int isFolder)throws Exception
	{
		if(!checkToken())
		{
			throw new WebStorageException(WebStorageException.TOKEN_ERROR);
		}
		
		String mXML = "";
        String responseXML = "";
        mXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
				"<getentryinfo>"+
					"<token>" + mTokenStr + "</token>"+
					"<isfolder>" + isFolder + "</isfolder>"+
					"<entryid>" + ID + "</entryid>"+
				"</getentryinfo>";

        responseXML = postData(mInfoRelayURL + "/fsentry/getentryinfo/", mXML);

		if(!checkRespondXml(responseXML))
		{
			throw new WebStorageException(WebStorageException.RESPONSEXML_ERROR);
		}		
		
		Log.i(TAG,"responseXML" + responseXML);
		
		Document doc = mHelper.parseStringToXml(responseXML);
		Element root = doc.getDocumentElement();
		NodeList attNodeList = doc.getElementsByTagName("getentryinfo");//attribute
		
		if(attNodeList.getLength() > 0)
		{
			return attNodeList.item(0);
		}
        
		return null;
	}
	
	public Node getLatestTime(String id)throws Exception
	{
		if(!checkToken())
		{
			throw new WebStorageException(WebStorageException.TOKEN_ERROR);
		}
		
        String responseXML = "";
        
        String mXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + 
			"<browse>"+
				"<token>" + mTokenStr + "</token>"+
				"<language>" + mUserLanguage + "</language>" + 
				"<userid>" + mUser + "</userid>"+
				"<folderid>" + id + "</folderid>" + 
				"<page>" + 
					"<pageno>" + 1 +"</pageno>" + 
					"<pagesize>" + 1 +"</pagesize>" + 
					"<enable>" + 1 + "</enable>" + 
				"</page>" + 
				"<sortby>" + 2 + "</sortby>" +
				"<sortdirection>" + 1 + "</sortdirection>" + 
			"</browse>";

        responseXML = postData(mInfoRelayURL + "/folder/browse/", mXML);

		if(!checkRespondXml(responseXML))
		{
			throw new WebStorageException(WebStorageException.RESPONSEXML_ERROR);
		}		
		
		Log.i(TAG,"responseXML" + responseXML);
		
		Document doc = mHelper.parseStringToXml(responseXML);
		// BEGIN: Better
		Element root = doc.getDocumentElement();
        String status = mHelper.getNodeValue(root,"status");
		if (status.equals("2")) {
			throw new WebStorageException(WebStorageException.LOGIN_ERROR);
		}
		// END: Better
		NodeList attNodeList = doc.getElementsByTagName("file");
		
		if(attNodeList.getLength() > 0)
		{
			return attNodeList.item(0);
		}
        
		return null;
	}
	
	public String getEndTime(String id)
	{
		try
		{
			Node node = getLatestTime(id);
			if (node != null) {
				String endtime = mHelper.getNodeValue(node,
						"createdtime");
				return endtime;
			}
		}
		catch(Exception e)
		{
			Log.i("darwin", e.toString());
		}
		return null;
	}
	//end darwin
	
	/*updateFileAttribute
	 * @fileID file's id
	 * -1 token not exist
	 * -998 responseXML error
	 * 0 for success
	 * 2 auth error
	 * 219 file not exist
	 * 225 param error
	 * 999 general error
	 * */
	public int updateFileAttribute(String fileID, String attribute)throws Exception
	{
		if(!checkToken())
		{
			throw new WebStorageException(WebStorageException.TOKEN_ERROR);
		}
		
		String mXML = "";
        String responseXML = "";
        mXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
				"<updateattribute>"+
					"<token>" + mTokenStr + "</token>"+
					"<userid>" + mUser + "</userid>"+
					"<fileid>" + fileID + "</fileid>"+
					"<attribute>" + attribute + "</attribute>"+
				"</updateattribute>";

        responseXML = postData(mInfoRelayURL + "/file/updateattribute/", mXML);

		if(!checkRespondXml(responseXML))
		{
			throw new WebStorageException(WebStorageException.RESPONSEXML_ERROR);
		}
		
		Document doc = mHelper.parseStringToXml(responseXML);
		Element root = doc.getDocumentElement();
        String status = mHelper.getNodeValue(root,"status");
        
        if(status =="") {
        	return -999;
        } else if (status.equals("2")) { // Better
        	throw new WebStorageException(WebStorageException.LOGIN_ERROR);
        }
        
        return Integer.valueOf(status);
	}
	
	
	public Document getFileNodeList(String folderID,int pageno)throws Exception
	{
		if(pageno == -1)
		{
			mPageEnable = 0;
		}
		else
		{
			mPageEnable = 1;
		}
		
		//because there may be a lot of folder.
		//Begin Darwin_Yu@asus.com
		String responseXML = null;
		if(MetaData.THIS_SYNC_TIME.equalsIgnoreCase("") || MetaData.LAST_SYNC_TIME.equalsIgnoreCase(""))
		{
			responseXML = getFileList(folderID,pageno,mPageSize,mPageEnable,"","","");
		}
		else
		{
			responseXML = getFileList(folderID,pageno,mPageSize,mPageEnable,"",MetaData.LAST_SYNC_TIME,MetaData.THIS_SYNC_TIME);
		}
		
		//End Darwin_Yu@asus.com
		if(responseXML == null)
		{
			return null;
		}
		
		Document doc = mHelper.parseStringToXml(responseXML);
		// BEGIN: Better
		Element root = doc.getDocumentElement();
        String status = mHelper.getNodeValue(root,"status");
		if (status.equals("2")) {
			throw new WebStorageException(WebStorageException.LOGIN_ERROR);
		}
		// END: Better
		return doc;
	}
//Begin Darwin_Yu@asus.com
	//Modify reason add filter file ext and start time end time
	//Description: fileext -- just like"jpg""mp3" Separated by "," "jpg,mp3"
	//			   starttime -- yyyyMMddHHmmssSSS   20100331150308057
	private String getFileList(String folderID,int pageno,int pagesize,int enable,String fileExt,String startTime,String endTime)throws Exception
	{
		if(!checkToken())
		{
			throw new WebStorageException(WebStorageException.TOKEN_ERROR);
		}
		
		String mXML = "";
        String responseXML = "";
        mXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
				"<browse>"+
					"<token>" + mTokenStr + "</token>"+
					"<language>"+ mUserLanguage +"</language>"+//may need change
					"<userid>" + mUser + "</userid>"+
					"<folderid>" + folderID + "</folderid>";
		if(!fileExt.isEmpty())
		{
			mXML += "<fileext>" + fileExt + "</fileext>";
		}
		mXML += "<page>" + 
				"<pageno>" +pageno +"</pageno>"+
				"<pagesize>" +pagesize +"</pagesize>"+
				"<enable>" +enable +"</enable>"+
				"</page>";
		if(!startTime.isEmpty() && !endTime.isEmpty())
		{
			mXML +=  "<starttime>" + startTime + "</starttime>"
					+"<endtime>"   + endTime   + "</endtime>";
		}			
		
		mXML += "<sortby>"+2+"</sortby>"+//latest change time
					"<sortdirection>"+1+"</sortdirection>"+//desc
				"</browse>";

        responseXML = postData(mInfoRelayURL + "/folder/browse/", mXML);

		if(!checkRespondXml(responseXML))
		{
			throw new WebStorageException(WebStorageException.RESPONSEXML_ERROR);
		}
		
		Log.i(TAG,"File list is " +responseXML);
		return responseXML;
	}
	
	public Document getFileNodeList(String folderID,int pageno,String fileExt)throws Exception
	{
		if(pageno == -1)
		{
			mPageEnable = 0;
		}
		else
		{
			mPageEnable = 1;
		}
		
		//because there may be a lot of folder.
		//Begin Darwin_Yu@asus.com
		String responseXML = getFileList(folderID,pageno,mPageSize,mPageEnable,fileExt,"","");
		//End Darwin_Yu@asus.com
		if(responseXML == null)
		{
			return null;
		}
		
		Document doc = mHelper.parseStringToXml(responseXML);
		// BEGIN: Better
		Element root = doc.getDocumentElement();
        String status = mHelper.getNodeValue(root,"status");
		if (status.equals("2")) {
			throw new WebStorageException(WebStorageException.LOGIN_ERROR);
		}
		// END: Better
		return doc;
	}
	//End Darwin_Yu@asus.com
	
	/*getFileList
	 * @folderID folder
	 * return xml
	 * */
	private String getFileList(String folderID,int pageno,int pagesize,int enable)throws Exception
	{
		if(!checkToken())
		{
			throw new WebStorageException(WebStorageException.TOKEN_ERROR);
		}
		
		String mXML = "";
        String responseXML = "";
        mXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
				"<browse>"+
					"<token>" + mTokenStr + "</token>"+
					"<language>"+ mUserLanguage +"</language>"+//may need change
					"<userid>" + mUser + "</userid>"+
					"<folderid>" + folderID + "</folderid>"+
					"<page>" + 
						"<pageno>" +pageno +"</pageno>"+
						"<pagesize>" +pagesize +"</pagesize>"+
						"<enable>" +enable +"</enable>"+
					"</page>"+
					"<sortby>"+2+"</sortby>"+//latest change time
					"<sortdirection>"+1+"</sortdirection>"+//desc
				"</browse>";

        responseXML = postData(mInfoRelayURL + "/folder/browse/", mXML);

		if(!checkRespondXml(responseXML))
		{
			throw new WebStorageException(WebStorageException.RESPONSEXML_ERROR);
		}
		
		Log.i(TAG,"File list is " +responseXML);
		return responseXML;
	}
	
	/*getLatestChangefiles
	 * Return xml
	 * @fileName file's name
	 * @parentFolderID parent folder's ID
	 * @type system.file or system.folder
	 * */
	public String getLatestChangefiles(int top,int direction)throws Exception
	{
		
		String mXML = "";
        String responseXML = "";
        mXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
				"<propfind>"+
					"<token>" + mTokenStr + "</token>"+
					"<userid>" + mUser + "</userid>"+
					"<top>" + top + "</top>"+
					"<targetroot>" + "-5" + "</targetroot>"+
					"<sortdirection>" + direction + "</sortdirection>"+
				"</propfind>";

        responseXML = postData(mInfoRelayURL + "/file/getlatestchangefiles/", mXML);

		if(!checkRespondXml(responseXML))
		{
			throw new WebStorageException(WebStorageException.RESPONSEXML_ERROR);
		}
		
		return responseXML;
	}
	
	/*foundSpecificFileORFolder
	 * Return xml
	 * @fileName file's name
	 * @parentFolderID parent folder's ID
	 * @type system.file or system.folder
	 * */
	public String foundSpecificFileORFolder(String Name,String parentFolderID, String type)throws WebStorageException
	{
		
		String mXML = "";
        String responseXML = "";
        mXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
				"<propfind>"+
					"<token>" + mTokenStr + "</token>"+
					"<userid>" + mUser + "</userid>"+
					"<parent>" + parentFolderID + "</parent>"+
					"<find>" + mHelper.encodeBase64(Name) + "</find>"+
					//"<find>" + Name + "</find>"+
					"<type>" + type + "</type>"+
				"</propfind>";

        responseXML = postData(mInfoRelayURL + "/find/propfind/", mXML);

		if(!checkRespondXml(responseXML))
		{
			throw new WebStorageException(WebStorageException.RESPONSEXML_ERROR);
		}
		
		// BEGIN: Better
  		Document doc = mHelper.parseStringToXml(responseXML);
		Element root = doc.getDocumentElement();
        String status = mHelper.getNodeValue(root,"status");
		if (status.equals("2")) {
			throw new WebStorageException(WebStorageException.LOGIN_ERROR);
		}
  		// END: Better
		
		return responseXML;
	}

	/*foundSpecificFolder
	 * Return folderID
	 * @fileName file's name
	 * @parentFolderID parent folder's ID
	 * */
	public String foundSpecificFolder(String folderName, String parentFolderID)throws Exception
	{
		if(!checkToken())
		{
			throw new WebStorageException(WebStorageException.TOKEN_ERROR);
		}
		
        String responseXML = foundSpecificFileORFolder(folderName,parentFolderID,"system.folder");
		
		return responseXML;
	}

	/*foundSpecificFile
	 * Return xml
	 * @fileName file's name
	 * @parentFolderID parent folder's ID
	 * */
	private String foundSpecificFile(String fileName, String parentFolderID)throws WebStorageException
	{
		if(!checkToken())
		{
			throw new WebStorageException(WebStorageException.TOKEN_ERROR);
		}
		
        String responseXML = foundSpecificFileORFolder(fileName,parentFolderID,"system.file");
		
		return responseXML;
	}
	
	private String getSpecificFileOrFolderInfoFromXml(String responseXML,String info,String type) throws WebStorageException
	{
		Document doc = mHelper.parseStringToXml(responseXML);
		Element root = doc.getDocumentElement();
        String status = mHelper.getNodeValue(root,"status");
        String result = mHelper.getNodeValue(root,"type");
		if(status.equals("0") && result.equals(type))
		{
			String infoFromXml = mHelper.getNodeValue(root,info);
			return infoFromXml;
		} else if (status.equals("2")) { // Better
			throw new WebStorageException(WebStorageException.LOGIN_ERROR);
		}
		return "";
	}
	
	private String getSpecificFolderInfoFromXml(String responseXML,String info) throws WebStorageException
	{
		if(responseXML =="")
		{
			return "";
		}		
		return getSpecificFileOrFolderInfoFromXml(responseXML,info,"system.folder");
	}
	
	private String getSpecificFileInfoFromXml(String responseXML,String info) throws WebStorageException
	{
		if(responseXML =="")
		{
			return "";
		}		
		return getSpecificFileOrFolderInfoFromXml(responseXML,info,"system.file");
	}

	
	public String getSpecificFolderID(String folderName, String parentFolderID)throws Exception
	{
		String responseXML = foundSpecificFolder(folderName,parentFolderID);
		return getSpecificFolderInfoFromXml(responseXML,"id");
	}
	
	
	/*getSpecificFileID
	 * Return file's ID
	 * @fileName file's name
	 * @parentFolderID parent folder's ID
	 * */
	public String getSpecificFileID(String fileName, String parentFolderID)throws WebStorageException
	{
		Log.i(TAG,"foundSpecificFile "+fileName);
		String responseXML = foundSpecificFile(fileName,parentFolderID);
		Log.i(TAG,"foundSpecificFile "+responseXML);
		return getSpecificFileInfoFromXml(responseXML,"id");
	}
	
	/*getSpecificFileAttribute
	 * Return file's attribute
	 * @fileName file's name
	 * @parentFolderID parent folder's ID
	 * */
	public String getSpecificFileAttribute(String fileName, String parentFolderID)throws Exception
	{
		Log.i(TAG,"getSpecificFileAttribute "+fileName);
		String responseXML = foundSpecificFile(fileName,parentFolderID);
		Log.i(TAG,"getSpecificFileAttribute "+responseXML);
		return getSpecificFileInfoFromXml(responseXML,"attribute");
	}
	
	
	/*uploadOneFile
	 * 0 for fail
	 * 1 for success
	 * 
	 * 
	 * */
	public int uploadOneFile(String path, String fileID, String folderID, String attribute,String SHAValue, String fileShaValue)throws Exception
	{
        EEEStorageUpLoader ul = new EEEStorageUpLoader();
        ul.setWebRelayUrl(getWebRelayURL());
        ul.setTokenString(getToken());
        ul.setParentFolderID(folderID);
        ul.setFileID(fileID);
        
        Log.i(TAG," in uploadOneFile");
        try {
            int j = ul.upLoadOneFileBinary(path, attribute,SHAValue, fileShaValue);

            switch(j){
                case 1:
                    Log.i(TAG,"UpLoad "+path+" Success");
                    break;
                default:
                	Log.i(TAG,"UpLoad "+path+" fail");
                	return -1;
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return 0;
        } catch (SAXException e) {
            e.printStackTrace();
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        
        return 1;
	}
	
	// BEGIN: Better
	public boolean validateUploadStatus(String path, String fileID, String folderID, String attribute, String fileShaValue) throws Exception {
		EEEStorageUpLoader ul = new EEEStorageUpLoader();
        ul.setWebRelayUrl(getWebRelayURL());
        ul.setTokenString(getToken());
        ul.setParentFolderID(folderID);
        ul.setFileID(fileID);
        
        Log.i(TAG," in validateUploadStatus");

        try {
        	return ul.validateUploadStatus(path, attribute, fileShaValue);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return false;
	}
	// END: Better
	
	/*downloadOneFile
	 * 0 for fail
	 * 1 for success
	 * 
	 * 
	 * */
	public int downloadOneFile(String path, String fileID, String folderID) throws WebStorageException
	{
        EEEStorageDownLoader dl= new EEEStorageDownLoader();
        dl.setTokenString(getToken());
        dl.setWebRelayUrl(getWebRelayURL());
        dl.setFileID(fileID); 
        String saveName = path.substring(path.lastIndexOf(File.separator)+1);
        String savePath = path.substring(0,path.lastIndexOf(File.separator)+1);
        boolean success = dl.downloadOneFile(savePath, saveName);

        if(success) {
            Log.i(TAG,"DownLoad "+ saveName +" Success");
        }else {
            Log.i(TAG,"DownLoad "+ saveName +" Failure");
            return -1;
        }
        return 1;
	}
	//Begin Darwin_Yu@asus.com
	/*deleteSpecificFile
	 * Return xml
	 * */
	public String deleteSpecificFile(String fileID)throws WebStorageException
	{
		
		String mXML = "";
        String responseXML = "";
        mXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
				"<remove>"+
					"<token>" + mTokenStr + "</token>"+
					"<userid>" + mUser + "</userid>"+
					"<id>" + fileID + "</id>"+
				"</remove>";

        responseXML = postData(mInfoRelayURL + "/file/remove/", mXML);

		if(!checkRespondXml(responseXML))
		{
			throw new WebStorageException(WebStorageException.RESPONSEXML_ERROR);
		}
		
		// BEGIN: Better
  		Document doc = mHelper.parseStringToXml(responseXML);
		Element root = doc.getDocumentElement();
        String status = mHelper.getNodeValue(root,"status");
		if (status.equals("2")) {
			throw new WebStorageException(WebStorageException.LOGIN_ERROR);
		}
  		// END: Better
		
		return responseXML;
	}
	//End   Darwin_Yu@asus.com
	//BEGIN: Show, get account space usage from webstorage
	public String foundMemberInfo()throws Exception
	{
		if(!checkToken())
		{
			throw new WebStorageException(WebStorageException.TOKEN_ERROR);
		}
		
		String mXML = "";
        String responseXML = "";
        long time = System.currentTimeMillis();
        mXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
				"<getinfo>"+
					"<userid>" + mUser + "</userid>"+
					"<token>" + mTokenStr + "</token>"+
					"<time>" + String.valueOf(time) + "</time>" +
				"</getinfo>";

        responseXML = postData(mServiceGatewayURL + "/member/getinfo/", mXML);

		if(!checkRespondXml(responseXML))
		{
			throw new WebStorageException(WebStorageException.RESPONSEXML_ERROR);
		}
		
		return responseXML;		
	}
	
	public String getMemberUsedCapacity()throws Exception
	{
		String responseXML = foundMemberInfo();
		Log.i(TAG,"foundMemberInfo "+responseXML);
		
		Document doc = mHelper.parseStringToXml(responseXML);
		Element root = doc.getDocumentElement();
		// BEGIN: Better
        String status = mHelper.getNodeValue(root,"status");
		if (status.equals("2")) {
			throw new WebStorageException(WebStorageException.LOGIN_ERROR);
		}
		// END: Better
        String usedCapacity = mHelper.getNodeValue(root,"usedcapacity");
        String capacity = mHelper.getNodeValue(root,"capacity");
        //begin smilefish fix bug 381557
        int i = Integer.valueOf(capacity).intValue();
        BigDecimal bd1 = new BigDecimal(i / 1024.0);
    	bd1 = bd1.setScale(1, 4);
        capacity=String.valueOf(bd1.floatValue());
        int used = Integer.valueOf(usedCapacity).intValue();
        if(used >= 1024){
        	BigDecimal bd2 = new BigDecimal(used / 1024.0);
        	bd2 = bd2.setScale(1, 4);
        	usedCapacity = String.valueOf(bd2.floatValue());
        	return usedCapacity + "GB of " + capacity + "GB";
        }else{
        	return usedCapacity + "MB of " + capacity + "GB";
        }
        //end smilefish
	}
	//END: Show
	
}
