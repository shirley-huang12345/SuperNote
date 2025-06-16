package com.asus.supernote.languagehelp;

public class Language {
	private String lang = null; 
	private String country = null; 
	private String timezone = null; 
	private String mobiledata = null; 
	private String datasync = null; 
	
	public String getLanguage() { 
		return lang; 
	} 

	public void setLanguage(String str) { 
		this.lang = str; 
	} 
	
	public String getCountry() { 
		return country; 
	} 

	public void setCountry(String str) { 
		this.country = str; 
	} 
	
	public String getTimeZone() { 
		return timezone; 
	} 

	public void setTimeZone(String str) { 
		this.timezone = str; 
	}
	
	public String getMobiledata() { 
		return mobiledata; 
	} 

	public void setMobiledata(String str) { 
		this.mobiledata = str; 
	} 
	
	public String getDatasync() { 
		return datasync; 
	} 

	public void setDatasync(String str) { 
		this.datasync = str; 
	}

}
