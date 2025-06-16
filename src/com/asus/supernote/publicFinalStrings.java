package com.asus.supernote;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import com.asus.supernote.data.MetaData;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import java.io.File;


//information about super note info must in this class
public class publicFinalStrings {
    private static final String TAG_EEESTORAGE = "SuperNote_EEEstorage";
    private  EEEStorageChangeData sChangeData = new EEEStorageChangeData();
	
    //richard +
    private String mMySyncFolderID = "0";
	private String mMySuperNoteFolderName = "SuperNoteCloudSync";
	private String mMySuperNoteFolderID = "";
	//richard -
    
    public boolean isInternetOk(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        if(network != null){
            return network.isAvailable();
        }else{
            return false;
        }
    }

    public void setTimeout(int timeout)
    {
    	sChangeData.setTimeout(timeout);
    }
    
    public int getTimeout()
    {
    	return sChangeData.getTimeout();
    }
    
    //richard+
    /*Init
     * 
     * */
    public Boolean init(String userID , String passWD, boolean useNewFolder)throws Exception
    {
    	passWD = passWD.toLowerCase();
    	if(1 == login(userID,passWD))
    	{
    		if (MetaData.IS_ENABLE_WEBSTORAGE_DATA_MIGRATING && useNewFolder) {
    			mMySyncFolderID = "0";
    		} else {
    			mMySyncFolderID = sChangeData.getMySyncFolderID();
    		}
    		if(mMySyncFolderID != null && mMySyncFolderID != "")
    		{
    			if(findOrCreateSupterNoteFolder())
	    		{
        			return true;
	    		}
    		}
    	}
    		
    	return false;
    }
    
	/*findOrCreateSupterNoteFolder
	 * do this after logon
	 * 
	 * */
	public Boolean findOrCreateSupterNoteFolder()throws Exception
	{
		Log.i(TAG_EEESTORAGE,"findOrCreateSupterNoteFolder");
		for(int i = 0;i<2;i++)
		{
			mMySuperNoteFolderID =  sChangeData.getSpecificFolderID(mMySuperNoteFolderName,mMySyncFolderID);
			if(mMySuperNoteFolderID != null && mMySuperNoteFolderID!="")
			{
				return true;
			}
			else
			{
				sChangeData.createFolder(mMySuperNoteFolderName,mMySyncFolderID);
			}
		}
		return false;
	}
    
	/*
	 * 0 for success
	 * */
	public int setSpecificFileAttribute(String fileName,String newAttribute)throws Exception
	{
		String fileId = sChangeData.getSpecificFileID(fileName, mMySuperNoteFolderID);
		if(fileId==null || fileId == "")
		{
			return -998;
		}
		return sChangeData.updateFileAttribute(fileId, newAttribute);
	}
	
	/*not null is sucess
	 * *
	 */
	public Node getSpecificFileAttribute(String fileName)throws Exception
	{
		String fileId = sChangeData.getSpecificFileID(fileName, mMySuperNoteFolderID);
		if(fileId==null || fileId == "")
		{
			return null;
		}
		return  sChangeData.getEntryInfo(fileId, 0);
	}
	
	//begin darwin
	/*not null is sucess
	 * *
	 */
	public String getEndTime()
	{
		return  sChangeData.getEndTime(mMySuperNoteFolderID);
	}
	
	
	//end darwin
	
	/*if pagno is -1,it return all.
	 * */
	public Document getSuperNoteFileList(int pageno)throws Exception
	{
		return sChangeData.getFileNodeList(mMySuperNoteFolderID,pageno);
	}
	
	/*uploadOneFile to super note folder
	 * @path path of file
	 * @attribute attribute of update file
	 * 1 for success
	 * -998 for file not exist
	 * 0 for upload error
	 * -999 cann't happen
	 * -1,User's storage space has been exhausted
     * -2,The user's account has been frozen or closed
	 * */
    public int uploadOneFile(String path,String attribute,String SHAValue, String fileShaValue)throws Exception
    {
        File file=new File(path);
        if(!file.exists()){
            Log.i(TAG_EEESTORAGE,"upload file "+path+" not exists");
            return -998;
        }
        
    	String fileName = path.substring(path.lastIndexOf(File.separator)+1);
    	String fileId = sChangeData.getSpecificFileID(fileName, mMySuperNoteFolderID);
    	
    	//if fileid == "",not exist or some error happen
    	Log.i(TAG_EEESTORAGE,"before uploadOneFile mMySuperNoteFolderID"+mMySuperNoteFolderID);
    	int upStatus = sChangeData.uploadOneFile(path,fileId,mMySuperNoteFolderID ,attribute,SHAValue, fileShaValue);
    	
    	if(1 == upStatus)
    	{
    		// BEGIN: Better
    		if ((fileShaValue != null) && (fileShaValue != "")) {
	    		String newFileId = sChangeData.getSpecificFileID(fileName, mMySuperNoteFolderID);
	    		if ((newFileId != null) && (newFileId != "")) {
		    		if (!sChangeData.validateUploadStatus(path, newFileId, mMySuperNoteFolderID, attribute, fileShaValue)) {
		    			Log.i(TAG_EEESTORAGE, "uploadOneFile validated failed");
		    			throw new WebStorageException(WebStorageException.UPLOAD_SHA_ERROR);
		    		}
	    		} else {
	    			Log.i(TAG_EEESTORAGE, "uploadOneFile validated failed");
	    			throw new WebStorageException(WebStorageException.UPLOAD_SHA_ERROR);
	    		}
    		}
    		// END: Better
    		Log.i(TAG_EEESTORAGE,"uploadOneFile sucess");
    		return upStatus;
    	}
        
    	return 0;
    }
    
    /*downloadOneFile
     * -1 for fail
     * 1 for success
     * */
    public int downloadOneFile(String fileName,String savePath)throws WebStorageException
    {
    	String fileId = sChangeData.getSpecificFileID(fileName, mMySuperNoteFolderID);
    	if(fileId =="")
    	{
    		return -1;
    	}
    	
    	int downStaus = sChangeData.downloadOneFile(savePath,fileId,mMySuperNoteFolderID);
    	return downStaus;
    }
    
    /*isFileExistOnEeeStorage
     * true for exist
     * false for not exist or some error
     * */
    public boolean isFileExistOnEeeStorage(String fileName)throws Exception
    {
    	String fileId = sChangeData.getSpecificFileID(fileName, mMySuperNoteFolderID);
    	if(fileId =="")
    	{
    		return false;
    	}
    	return true;
    }
    
    /**
     * Login handler.
     * @param userName
     * @param passwd
     * @return 1,login success; 
     *         0,invalid username or password; 
     *         -1,unstable network connection.
     */
    public int login(String userName,String password) throws Exception
    {
    	
        sChangeData.setUser(userName);
        sChangeData.setPassword(password);

        /*Get token and response a code.*/
        int errCode = sChangeData.requestToken();
        switch(errCode){
            case 1:     /*Login Success*/
                Log.i(TAG_EEESTORAGE,"LOGIN_SUCCESS");
                break;
            case 0:     /*Response is NULL,Unstable network connection*/
            case -1:    /*Unstable network connection*/
                throw new WebStorageException(WebStorageException.NETWORK_ERROR);
            case -2:    /*Invalid username*/
            case -3:    /*Invalid password*/
            	throw new WebStorageException(WebStorageException.LOGIN_ERROR);
			case -4:    /*SSLHandshakeException*/
				throw new WebStorageException(WebStorageException.OTHER_ERROR);
			case -5:    /*SocketTimeoutException*/
				throw new WebStorageException(WebStorageException.TIMEOUT_ERROR);
        }
        return 1;
    }
 
    public void internetErrorDialog(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("R.string.dialog_connectFail")//R.string.dialog_connectFail
               .setMessage("R.string.dialog_connectFailInfo")//R.string.dialog_connectFailInfo
               .setPositiveButton("R.string.options_string_settings", new DialogInterface.OnClickListener() {//R.string.options_string_settings
                   public void onClick(DialogInterface dialog, int id) {
                       Intent i = new Intent();
                       ComponentName comp = new ComponentName("com.android.settings","com.android.settings.Settings$WirelessSettingsActivity");
                       i.setComponent(comp);
                       i.setAction("android.settings.WIRELESS_SETTINGS");
                       context.startActivity(i); 
                   }  
               })
               .setNegativeButton("R.string.cancel", new DialogInterface.OnClickListener() {//R.string.cancel
                   public void onClick(DialogInterface dialog, int id) {
                   }  
               });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    public boolean deleteUpdateFile(String path){
        File f = new File(path);
        return f.delete();
    }
    
    //BEGIN: Show, get accout space usage from webstorage
	public String getMemberUsedCapacity()throws Exception
	{
		return sChangeData.getMemberUsedCapacity();
	}
    //END: Show
	//Begin Darwin_Yu@asus.com
  	public String foundSpecificFileORFolder(String fileName,boolean isFolder)throws Exception
  	{
  		return sChangeData.foundSpecificFileORFolder(fileName, mMySuperNoteFolderID, isFolder ? ("system.folder") : ("system.file"));
  	}
  	
  	public Document getSuperNoteFileList(int pageno,String fileExt)throws Exception
	{
		return sChangeData.getFileNodeList(mMySuperNoteFolderID,pageno,fileExt);
	}
  	
  	public void deleteRemoteFile(String fileName)throws Exception
  	{
  		String fileId = sChangeData.getSpecificFileID(fileName, mMySuperNoteFolderID);
  		String resultXml = sChangeData.deleteSpecificFile(fileId);
  	}
  	//End   Darwin_Yu@asus.com
}
