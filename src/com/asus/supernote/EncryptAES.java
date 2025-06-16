package com.asus.supernote;

import android.util.Base64;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptAES {

    public static String encrypt(SecretKey secretKey, byte[] iv, String cleartext) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        byte[] byteCipherText = cipher.doFinal(cleartext.getBytes("UTF-8"));
        String encryptedText = Base64.encodeToString(byteCipherText, Base64.NO_WRAP);
        return encryptedText;
    }

    public static String decrypt(String keyStr, String ivStr, String encrypted) throws Exception{
        if(ivStr.length() > 0){
            byte[] iv = Base64.decode(ivStr, Base64.NO_WRAP);
            byte[] decodedKey = Base64.decode(keyStr, Base64.NO_WRAP);
            SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] encryptedText = Base64.decode(encrypted, Base64.NO_WRAP);
            byte[] decryptedText = cipher.doFinal(encryptedText);
            return new String(decryptedText);
        }else{
            return decrypt("asus", encrypted);
        }
    }
       
    public static String decrypt(String seed, String encrypted) throws Exception {   
        byte[] rawKey = getRawKey(seed.getBytes());   
        byte[] enc = toByte(encrypted);   
        byte[] result = decrypt(rawKey, enc);   
        return new String(result);   
    }   
  
    private static byte[] getRawKey(byte[] seed) throws Exception {   
        KeyGenerator kgen = KeyGenerator.getInstance("AES");   
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");   
        sr.setSeed(seed);   
        kgen.init(128, sr); // 192 and 256 bits may not be available   
        SecretKey skey = kgen.generateKey();   
        byte[] raw = skey.getEncoded();   
        return raw;   
    }
  
    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {   
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");   
        Cipher cipher = Cipher.getInstance("AES");   
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);   
        byte[] decrypted = cipher.doFinal(encrypted);   
        return decrypted;   
    }   
  
    public static String toHex(String txt) {   
        return toHex(txt.getBytes());   
    }   
    public static String fromHex(String hex) {   
        return new String(toByte(hex));   
    }   
       
    public static byte[] toByte(String hexString) {   
        int len = hexString.length()/2;   
        byte[] result = new byte[len];   
        for (int i = 0; i < len; i++)   
            result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();   
        return result;   
    }   
  
    public static String toHex(byte[] buf) {   
        if (buf == null)   
            return "";   
        StringBuffer result = new StringBuffer(2*buf.length);   
        for (int i = 0; i < buf.length; i++) {   
            appendHex(result, buf[i]);   
        }   
        return result.toString();   
    }   
    private final static String HEX = "0123456789ABCDEF";   
    private static void appendHex(StringBuffer sb, byte b) {   
        sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));   
    }   

}
