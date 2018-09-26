package com.u8.server.sdk.coolpad;

import com.u8.server.utils.RSAUtils;

public class SignHelper
{
	// 字符编码格式 ，目前支持  utf-8
	public static String input_charset = "utf-8";
	
	public static boolean verify(String content, String sign, String pubKey)
	{
		// 目前版本，只支持RSA
		return RSAUtils.verify(content, sign, pubKey, input_charset);
	}
	
	public static String sign(String content, String privateKey)
	{
		return RSAUtils.sign(content, privateKey, input_charset);
	}
}
