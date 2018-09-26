package com.u8.server.sdk.sy233;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.servlet.http.HttpServletRequest;

import com.u8.server.utils.JsonUtils;
import org.apache.commons.codec.binary.Base64;

public class UtilVerify {


  /**
   * 解析交易数据
   * 
   * @param request HttpServletRequest
   * @param rsaPublicKey 游戏的RSA Public key
   * @return 交易数据map, 如果解析失败则返回空
   */
  public static Map<Object, Object> decryptData(HttpServletRequest request, String rsaPublicKey) {
    Map<Object, Object> result = null;
    String encrypData = request.getParameter("encrypData");
    String sign = request.getParameter("sign");
    if (isNotEmptyAll(encrypData, sign)) {
      try {
        byte[] publickey = Base64.decodeBase64(rsaPublicKey);
        byte[] desKeyBytes = decryptByPublicKey(Base64.decodeBase64(sign), publickey);
        String json = new String(desEdeDecrypt(Base64.decodeBase64(encrypData), desKeyBytes), "utf-8");
        result = JsonUtils.jsonStr2Map(json);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  private static byte[] decryptByPublicKey(byte[] data, byte[] key) throws Exception {
    X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.DECRYPT_MODE, publicKey);
    return cipher.doFinal(data);
  }

  /**
   * desEde解密
   * 
   * @param data 需要解密数据
   * @param key 秘钥
   * @return
   * @throws Exception
   */
  private static byte[] desEdeDecrypt(byte[] data, byte[] key) throws Exception {
    Key k = desEdeToKey(key);
    Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, k);
    return cipher.doFinal(data);
  }

  /**
   * 转换密钥
   * 
   * @param key
   *          二进制密钥
   * @return Key 密钥
   * @throws Exception
   */
  private static Key desEdeToKey(byte[] key) throws Exception {
    DESedeKeySpec dks = new DESedeKeySpec(key);
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
    SecretKey secretKey = keyFactory.generateSecret(dks);
    return secretKey;
  }

  private static boolean isNotEmptyAll(String... strings) {
    if (null == strings || strings.length == 0) {
      return false;
    }
    for (String string : strings) {
      if (null == string || string.isEmpty()) {
        return false;
      }
    }
    return true;
  }
}
