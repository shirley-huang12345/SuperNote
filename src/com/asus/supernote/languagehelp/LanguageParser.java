package com.asus.supernote.languagehelp;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LanguageParser extends DefaultHandler{
	private List<Language> languages = null; 
	private Language currentLanguage;
	public List<Language> getLanguages() { 
		return languages; 
	} 
	
	@Override 
	public void startDocument() throws SAXException { 
		languages = new ArrayList<Language>(); 
	} 
	
	
	@Override 
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException { 
		if(localName.equals("language")){ 
			currentLanguage = new Language(); 
			String lang = atts.getValue("lang");
			currentLanguage.setLanguage(lang);
			String country = atts.getValue("country");
			currentLanguage.setCountry(country);
			String timezone = atts.getValue("timezone");
			currentLanguage.setTimeZone(timezone);
			String mobiledata = atts.getValue("mobiledata");
			currentLanguage.setMobiledata(mobiledata);
			String datasync = atts.getValue("datasync");
			currentLanguage.setDatasync(datasync);
		} 
	} 
	
	@Override public void endElement(String uri, String localName, String name) throws SAXException { 
		if(localName.equals("language")){ 
			languages.add(currentLanguage); 
			currentLanguage = null; 
		} 
	} 
}
