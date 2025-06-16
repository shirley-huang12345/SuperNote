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

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class EEEStorageDownLoader {
    private URL mUrl;
    private HttpURLConnection mUrlCon;
    private int mFileLength;
    private String mFileID = "";
    private String mTokenString;
    private String mWebRelayURL = "";
    
    private static final String TAG = "WebStorageAPI";
    
    public void setFileID(String fileid) {
        mFileID = fileid;
    }
    
    public void setWebRelayUrl(String url) {
        mWebRelayURL = url;
    }
    
    public void setTokenString(String token) {
        mTokenString = token;
    }
    public String getTokenString() {
        return this.mTokenString;
    }
    
    /**
     * Download file 
     * @param path
     * @param fileName
     * @throws WebStorageException 
     */
    public boolean downloadOneFile(String path,String fileName) throws WebStorageException
    {
        if(mFileID == null || mFileID.length() <= 0){
            Log.i(TAG,"DownLoader-downloadOneFile: fileID==null return,nothing to download");
            return false;
        }
        if(path == null || path.length() <= 0 ){
            Log.i(TAG,"DownLoader-downloadOneFile: path is incorrect");
            return false;
        }
        if(!isFileExist(path)){
            try{
                File file = new File(path);
                file.mkdirs();
            }catch(Exception e){
                Log.i(TAG,"Create dirs error");
            }
        }
        
        boolean result = downloadFile("http://" + mWebRelayURL + "/webrelay/directdownload/"+mTokenString+"/?fi="+mFileID, path, fileName);
        return result;
    }
    
    /**
      * Read form inputstreame an save to a file.
      * @param urlStr   The url of the download file
      * @param path Which folder want to save to
      * @param fileName 
      * @return False while downloadFile not success,true otherwise
      
     * @throws WebStorageException */
    private boolean downloadFile(String urlStr,String path,String fileName) throws WebStorageException{
        InputStream inputStream=null;
        boolean lock = false;
        try {
            inputStream = getInputStreamFromUrl(urlStr);
            lock = true;
            File resultFile = writeFromInput(path, fileName, inputStream);
            if(resultFile == null){
                Log.e(TAG,"DownLoader-downloadFile: File null");
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG,"DownLoader-downloadFile: IOException");
            e.printStackTrace();
            return false;
        }finally{
            //Log.v(TAG,"DownLoader-downloadFile: finally");
            if(!lock) return false;
            try {
                inputStream.close();
                mUrlCon.disconnect();
            } catch (IOException e) {
                Log.e(TAG,"DownLoader-downloadFile: IOException-2");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    /**
      * Get inputstream according to the url.
      * @param urlStr
      * @return
      * @throws IOException
      
     * @throws WebStorageException */
    private InputStream getInputStreamFromUrl(String urlStr) throws IOException, WebStorageException{
        mUrl=new URL(urlStr);
        mUrlCon=(HttpURLConnection)mUrl.openConnection();
        mFileLength = mUrlCon.getContentLength();
        // BEGIN: Better
    	InputStream input = null;
	    try {
	    	input = mUrlCon.getInputStream();
	    } catch (IOException e) {
	    	InputStream errStream = mUrlCon.getErrorStream();
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
//        InputStream inputStream=mUrlCon.getInputStream();
        return input;
    }
    
    /**
     * Write inputstream to a file.
     * @param path  The folder path where the file in
     * @param fileName  File name with type, for example:file.txt
     * @param inputStream   
     * @return 
     */
    private File writeFromInput(String path,String fileName,InputStream inputStream)
    {
        File file = null;
        OutputStream output = null;
        try {
            file=createFile(path+fileName);
            output=new FileOutputStream(file);
            byte buffer[]=new byte[4*1024];
            int count = 0,sum = 0;
            while((count = inputStream.read(buffer)) > 0){
                output.write(buffer,0,count);
                sum+=count;
                System.out.println(fileName +"'s size is "+mFileLength+",now download is "+sum+".");
            }
            output.flush();
         } catch (FileNotFoundException e) {
                e.printStackTrace();
         } catch (IOException e) {
                    e.printStackTrace();            
         }finally{        
                 try {
                     output.close();         
                 } catch (IOException e) {
                     e.printStackTrace();          
                 }           
         }

         return file;
     }
    
    /**
     * Create a file.
     * @param fileName 
     * @return
     * @throws IOException
     */
    private File createFile(String fileName) throws IOException{
        File file = new File(fileName);
        file.createNewFile();
        return file;
    }
    
    /**
     * Judge if the file or folder exist.
     * @param fileName File or folder path
     * @return Return true if the file or folder exist,false otherwise
     */
    private boolean isFileExist(String fileName){
        File file = new File(fileName);
        return file.exists();
    }
    
    /**
     * If the file download is a text file then read text an return string in the text.
     * @param urlStr The url of the text file
     * @return File text
     */
    public String readFile(String urlStr) {
        StringBuffer sb=new StringBuffer();
        String line=null;
        BufferedReader buffer=null;
        try {
            mUrl=new URL(urlStr);
            HttpURLConnection urlcon=(HttpURLConnection)mUrl.openConnection();
            buffer=new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
            while((line=buffer.readLine())!=null){
                sb.append(line);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                buffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
