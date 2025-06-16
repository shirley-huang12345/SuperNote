package com.asus.supernote;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Base64;
import android.util.Log;

public class WebStorageHelper {
	private static final String TAG = "WebStorageHelper";

	/**
	 * Encoding string with base64.
	 * 
	 * @param str
	 *            String need to encode.
	 * @return String after encoded.
	 */
	public String encodeBase64(String str) {
		byte[] encodeBt = str.getBytes();
		String encodedStr = Base64.encodeToString(encodeBt, Base64.NO_WRAP);
		return encodedStr;
	}

	/**
	 * Decoding string with base64.
	 * 
	 * @param str
	 *            String need to decode.
	 * @return String after decode.
	 */
	public String decodeBase64(String str) {
		byte[] decodeBytes = Base64.decode(str, Base64.NO_WRAP);
		String decodedStr = new String(decodeBytes);
		return decodedStr;
	}

	public String getPassWordMD5(String password) {

		byte[] defaultBytes = password.getBytes();
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(defaultBytes);
			byte messageDigest[] = algorithm.digest();

			return bytesToHexString(messageDigest);
		} catch (NoSuchAlgorithmException nsae) {
			return null;
		}

	}
	
	public String getSignWordSHA1(String keyString,String password) 
	{ 
		try
		{
			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), mac.getAlgorithm());
			
			mac.init(key);

			byte[] bytes = mac.doFinal(password.getBytes("UTF-8"));

			return Base64.encodeToString(bytes,Base64.NO_WRAP);
		}catch(Exception e)
		{
			
		}
		return "";
	}

	/**
	 * 适用于上G大的文件
	 */
	public String getFileSha1(String path) throws OutOfMemoryError, IOException {
		File file = new File(path);
		FileInputStream in = new FileInputStream(file);
		MessageDigest messagedigest;
		try {
			messagedigest = MessageDigest.getInstance("SHA-512");

			byte[] buffer = new byte[1024 * 1024 * 10];
			int len = 0;

			while ((len = in.read(buffer)) > 0) {
				// 该对象通过使用 update（）方法处理数据
				messagedigest.update(buffer, 0, len);
			}

			// 对于给定数量的更新数据，digest 方法只能被调用一次。在调用 digest 之后，MessageDigest
			// 对象被重新设置成其初始状态。
			return bytesToHexString(messagedigest.digest());
		} catch (NoSuchAlgorithmException e) {
			Log.e("getFileSha1->NoSuchAlgorithmException###", e.toString());
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			Log.e("getFileSha1->OutOfMemoryError###", e.toString());
			e.printStackTrace();
			throw e;
		} finally {
			in.close();
		}
		return null;
	}

	public String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * Get XML node value
	 * 
	 * @param start
	 *            node root
	 * @param nodeName
	 *            which node do you want to get
	 * @return node value
	 */
	public String getNodeValue(Node start, String nodeName) {
		for (Node child = start.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			/* Ignore the blank lines */
			if (child instanceof Element) {
				/* Return node value */
				String name = child.getNodeName();
				if ((name != null) && name.compareTo(nodeName) == 0) {
					Node node = child.getFirstChild();
					if (node != null) {
						return node.getNodeValue();
					} else {
						return "";
					}
				}
			}
			/* Recursive */
			if (child != null) {
				String res = getNodeValue(child, nodeName);
				if (res != "") {
					return res;
				}
			}
		}
		/* Return null while nodeName not found */
		return "";
	}

	/**
	 * input string, parse string to XML element
	 * 
	 * @param str
	 * @return return XML element while normal; return null while error
	 */

	public Document parseStringToXml(String str) {
		if (str == null || str.equals("SocketTimeoutException")
				|| str.equals("SSLHandshakeException")) {
			Log.i(TAG, "parseStringToXml str is null or some exception");
			return null;
		}
		Log.v(TAG, "In parseStringToXml");
		StringReader sr = new StringReader(str);
		InputSource is = new InputSource(sr);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document doc = null;
		if (Thread.interrupted()) {
			Log.v("wendy", "parseStringToXml is interrupted ");
			return null;
		}
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			Log.e(TAG,
					"Error in function <parseStringToXml> ParserConfigurationException");
			e.printStackTrace();
			return null;
		}
		if (builder != null) {
			try {
				doc = builder.parse(is);
			} catch (SAXException e) {
				Log.e(TAG, "Error in function <parseStringToXml> SAXException");
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				Log.e(TAG, "Error in function <parseStringToXml> IOException");
				e.printStackTrace();
				return null;
			}
		}
		Log.v(TAG, "Out parseStringToXml");
		// Return XML element while normal;return null while something error
		return doc;
	}
}
