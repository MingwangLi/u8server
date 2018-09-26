package com.u8.server.test;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Random;


/**
 * des加密解密不带随机数
 * 
 * @author zxf
 * 
 */
public class DESCode {

	/**
	 * 自定义一个key
	 */
	public static Key getKey(byte[] keyByte) {
		Key key = null;
		// 创建一个空的八位数密钥,默认情况下为0
		byte[] byteTemp = new byte[8];
		// 将用户指定的规则转换成八位数
		for (int i = 0; i < byteTemp.length && i < keyByte.length; i++) {
			byteTemp[i] = keyByte[i];
		}
		key = new SecretKeySpec(byteTemp, "DES");
		return key;
	}

	/**
	 * 第二种随机产生key的方法
	 * 
	 * @return
	 */
	public Key getKey2() {
		Key key = null;
		// 创建一个可信任的随机数源，DES算法
		SecureRandom sr = new SecureRandom();
		try {
			// 用DES算法创建一个KeyGenerator对象
			KeyGenerator kg = KeyGenerator.getInstance("DES");
			// 初始化此密钥生成密钥,使其具有确定的密钥长度
			kg.init(sr);
			// 生成密匙
			key = kg.generateKey();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return key;
	}

	/**
	 * @param key
	 *            加密使用的密钥！
	 * @return 生成密文的字符串表示形式
	 */
	public static String getEncriptCode(Key key, String srcCode) {
		StringBuffer sb = null;

		try {
			
//			 * Cipher类无构造方法，调用getInstance()方法将所请求转换的名称传递给的参数 转换的名称，例如
//			 * DES/CBC/PKCS5Padding，这里我们使用DES转换
			 
			Cipher encriptCipher = Cipher.getInstance("DES");
			// 用密钥初始化 Cipher
			encriptCipher.init(Cipher.ENCRYPT_MODE, key);
			// 按单部分操作加密数据
			byte[] desCode = null;
			try {
				desCode = encriptCipher.doFinal(srcCode.getBytes("UTF-8"));
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 将加密后的数据转换成16进制的字符串返回
			sb = new StringBuffer(desCode.length * 2);
			for (int i = 0; i < desCode.length; i++) {
				int temp = desCode[i];
				// 把负数转换为正数
				if (temp < 0) {
					temp = temp + 256;// byte的最小的为-256,最大的为255
				}
				// 小于 0F 的数的要在前面加0
				if (temp < 16) {
					sb.append("0");
				}
				sb.append(Integer.toString(temp, 16));
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * 解密算法
	 * 
	 * @param encriptCode
	 * @param key
	 * @return
	 */
	public static String getDecriptCode(String encriptCode, Key key) {
		Cipher decriptCipher = null;
		String decriptString = null;
		byte[] encriptByte = encriptCode.getBytes();

		byte[] decriptByte = new byte[encriptByte.length / 2];
		for (int i = 0; i < encriptByte.length; i += 2) {
			String strTmp = new String(encriptByte, i, 2);
			decriptByte[i / 2] = (byte) Integer.parseInt(strTmp, 16);
		}

		try {
			decriptCipher = Cipher.getInstance("DES");
			decriptCipher.init(Cipher.DECRYPT_MODE, key);

			byte[] outByte = decriptCipher.doFinal(decriptByte);
				decriptString = new String(outByte,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}

		return decriptString;
	}

	/**
	 * 数据加密
	 * 
	 * @param str
	 * @return
	 */
	public static String requestXml(String str) {
		String keyRule = getRandomNumr(10);
		Key key = getKey(keyRule.getBytes());
		String encriptCode = getEncriptCode(key, str);
		return getRandomNumr(10) + keyRule + encriptCode + getRandomNumr(15);
	}

	
	/**
	 * 数据解密
	 * 
	 * @param str
	 * @return
	 */
	public static String responseXml(String str) {
		String keyRule = str.substring(10, 20);
		Key key = getKey(keyRule.getBytes());
		String xml = getDecriptCode(str.substring(20, str.length() - 15), key);
		return xml;
	}
	
	/**
	 * 数据加密
	 * 
	 * @param str 
	 * @return
	 */
	public static String requestString(String str,String type) {
		String keyRule = getRandomNumr(10);
		Key key = getKey(keyRule.getBytes());
		String encriptCode = getEncriptCode(key, type+"_"+str);
		return getRandomNumr(10) + keyRule + encriptCode + getRandomNumr(15);
	}

	
	/**
	 * 数据解密
	 * 
	 * @param str
	 * @return
	 */
	public static String responseString(String des_str,String type) {
		String keyRule = des_str.substring(10, 20);
		Key key = getKey(keyRule.getBytes());
		String xml = getDecriptCode(des_str.substring(20, des_str.length() - 15), key);
		String str = xml.substring(type.length()+1);
		return str;
	}
	/**
	 * 生成随机数据
	 * 
	 * @param length
	 *            随机数的长度
	 * @return
	 */
	public static String getRandomNumr(int length) {
		String val = "";
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			String charOrNum = random.nextInt(2) % 2 == 0 ? "abc" : "123"; // 输出字母还是数字

			if ("abc".equalsIgnoreCase(charOrNum)) // 字符串
			{
				int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; // 取得大写字母还是小写字母
				val += (char) (choice + random.nextInt(26));
			} else if ("123".equalsIgnoreCase(charOrNum)) // 数字
			{
				val += String.valueOf(random.nextInt(10));
			}
		}

		return val.toLowerCase();
	}
	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		// 原来字符串
		String src = "201606071549";
		System.out.println("加密后=" + DESCode.requestXml(src));
		// 密钥 &key=f8e66fc66145a10359400d77e7d6a412
		String xml = DESCode.responseXml("csba0g7p4t71cs7g8e7qa12e4994b1dccdfd9b2a72c3435ab2cb4a39ed6fababe8b24608763e4b250a357b4f3868158587794c92d838a36bf4b0b87abe036a0ee533w3q8031p6g3b1f3");
		System.out.println("解密后="+xml);
	}
}
