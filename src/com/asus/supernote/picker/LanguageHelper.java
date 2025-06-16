package com.asus.supernote.picker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.util.Log;

import com.asus.supernote.data.MetaData;
import com.asus.supernote.languagehelp.Language;
import com.asus.supernote.languagehelp.LanguageParser;

public class LanguageHelper {
	
	public static final String TAG = "LanguageHelper";
	
	public LanguageHelper()
	{
		
	}
	
	public int getRecordIndexLaguage(){
		int recordIndexLaguage = MetaData.INDEX_LANGUAGE_EN_US;//0
		String strCountry = "US";
		String strLanguage = "EN";
		
		final Locale defaultLocale = Locale.getDefault();
		strCountry = defaultLocale.getCountry().toUpperCase();
		strLanguage = defaultLocale.getLanguage().toUpperCase();
		
		if(strCountry.equalsIgnoreCase("CN"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_ZH_CN;//1
		}
		else if(strCountry.equalsIgnoreCase("TW"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_ZH_TW;//2
		}
		else if(strCountry.equalsIgnoreCase("AR"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_AR;//5
		}
		else if(strCountry.equalsIgnoreCase("CZ"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_CS_CZ;//6
		}
		else if(strCountry.equalsIgnoreCase("DK"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_DA_DK;//7
		}
		else if(strCountry.equalsIgnoreCase("DE"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_DE_DE;//8
		}
		else if(strCountry.equalsIgnoreCase("GR"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_EL_GR;//9
		}
		else if(strCountry.equalsIgnoreCase("CA"))
		{
			if(strLanguage.equalsIgnoreCase("EN"))
			{
				recordIndexLaguage = MetaData.INDEX_LANGUAGE_EN_CA;//10
			}
			else if(strLanguage.equalsIgnoreCase("FR"))
			{
				recordIndexLaguage = MetaData.INDEX_LANGUAGE_FR_CA;//14
			}
		}
		else if(strCountry.equalsIgnoreCase("GB"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_EN_GB;//11
		}
		else if(strCountry.equalsIgnoreCase("ES"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_ES_ES;//12
		}
		else if(strCountry.equalsIgnoreCase("FI"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_FI_FI;//13
		}
		else if(strCountry.equalsIgnoreCase("FR"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_FR_FR;//15
		}
		else if(strCountry.equalsIgnoreCase("IL"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_HE_IL;//16
		}
		else if(strCountry.equalsIgnoreCase("HU"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_HU_HU;//17
		}
		else if(strCountry.equalsIgnoreCase("IT"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_IT_IT;//18
		}
		else if(strCountry.equalsIgnoreCase("KR"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_KO_KR;//19
		}
		else if(strCountry.equalsIgnoreCase("NL"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_NL_NL;//20
		}
		else if(strCountry.equalsIgnoreCase("NO"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_NO_NO;//21
		}
		else if(strCountry.equalsIgnoreCase("PL"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_PL_PL;//22
		}
		else if(strCountry.equalsIgnoreCase("BR"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_PT_BR;//23
		}
		else if(strCountry.equalsIgnoreCase("PT"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_PT_PT;//24
		}
		else if(strCountry.equalsIgnoreCase("RU"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_RU_RU;//25
		}
		else if(strCountry.equalsIgnoreCase("SE"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_SV_SE;//26
		}
		else if(strCountry.equalsIgnoreCase("TR"))
		{
			recordIndexLaguage = MetaData.INDEX_LANGUAGE_TR_TR;//27
		}
		return recordIndexLaguage;
	}
}
