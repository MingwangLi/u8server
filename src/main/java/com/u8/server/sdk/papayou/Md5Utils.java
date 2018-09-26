package com.u8.server.sdk.papayou;

import java.security.MessageDigest;

/***
 * MD5鍔犲瘑.
 * 
 * @author Administrator
 * 
 */
public class Md5Utils {

 
	public final static String MD5(String str) {
		char hexDigits[] = { 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
				'e', 'f' };
		try {
			byte[] strTemp = str.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte tmp[] = mdTemp.digest();  
			char strs[] = new char[16 * 2];   
			int k = 0; 
			for (int i = 0; i < 16; i++) { 
				byte byte0 = tmp[i];  
				strs[k++] = hexDigits[byte0 >>> 4 & 0xf]; 
				strs[k++] = hexDigits[byte0 & 0xf]; 
			}
			return new String(strs); 
		} catch (Exception e) {
			return null;
		}
	}



}
