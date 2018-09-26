package com.u8.server.sdk.papayou;

import java.security.MessageDigest;
import java.security.Security;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * MD5算法加密
 */
public class MD5Encrypt {

	private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

	private final static String[] romdonDigts = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
			"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
			"W", "X", "Y", "Z" };

	/**
	 * MD5加密算法. 用法：MD5Encrypt.MD5Encode("123456")
	 * 
	 * @param origin
	 *            原始密码
	 * @return String MD5加密后的密码
	 */
	public static String MD5Encode(String origin) {
		String resultString = null;

		try {
			resultString = new String(origin);
			MessageDigest md = MessageDigest.getInstance("MD5");
			resultString = byteArrayToString(md.digest(resultString.getBytes()));
			// resultString = origin;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return resultString;
	}
	public static void main(String[] args) {
		System.out.println(MD5Encode("123456"));
	}
	/**
	 * 转换字节数组为16进制字串
	 * 
	 * @param b
	 *            字节数组
	 * @return 16进制字串
	 */
	public static String byteArrayToString(byte[] b) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			resultSb.append(byteToHexString(b[i]));// 若使用本函数转换则可得到加密结果的16进制表示，即数字字母混合的形式
			// resultSb.append(byteToNumString(b[i]));//使用本函数则返回加密结果的10进制数字字串，即全数字形式
		}
		return resultSb.toString();
	}

	/**
	 * DES加密算法
	 */
	public byte[] encodeDES(byte[] data, int offset, int len, byte[] passwd)
			throws Exception {
		if (passwd.length != DESKeySpec.DES_KEY_LEN) {
			throw new Exception("DES加密方法的密码位数为8，指定的密码位数为" + passwd.length);
		}

		Security.addProvider(new com.sun.crypto.provider.SunJCE());
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			DESKeySpec keySpec = new DESKeySpec(passwd);
			SecretKey deskey = keyFactory.generateSecret(keySpec);
			Cipher c = Cipher.getInstance("DES");
			c.init(Cipher.ENCRYPT_MODE, deskey);
			return c.doFinal(data, offset, len);
		} catch (Exception ex) {
			throw new RuntimeException("DES加密失败", ex);
		}
	}

	/**
	 * DES解密算法
	 */
	public byte[] decodeDES(byte[] data, int offset, int len, byte[] passwd)
			throws Exception {
		if (passwd.length != DESKeySpec.DES_KEY_LEN) {
			throw new Exception("DES加密方法的密码位数为8，指定的密码位数为" + passwd.length);
		}
 		Security.addProvider(new com.sun.crypto.provider.SunJCE());
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			DESKeySpec keySpec = new DESKeySpec(passwd);
			SecretKey deskey = keyFactory.generateSecret(keySpec);
			Cipher c = Cipher.getInstance("DES");
			c.init(Cipher.DECRYPT_MODE, deskey);
			return c.doFinal(data, offset, len);
		} catch (Exception ex) {
			throw new RuntimeException("DES解密失败", ex);
		}
	}

	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0) {
			n = 256 + n;
		}
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigits[d1] + hexDigits[d2];
	}
	
	public static String randomString(){
		Random random = new Random();
		StringBuffer result = new StringBuffer("");
		for(int i = 0;i<16;i++){
			int index = Math.abs(random.nextInt()) % 36;
			result.append(romdonDigts[index]);
		}
		return result.toString();
		
	}

}
