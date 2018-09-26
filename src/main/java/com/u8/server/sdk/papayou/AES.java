package com.u8.server.sdk.papayou;
import java.net.URLDecoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AES {

    // 加密
    public static String Encrypt(String sSrc, String sKey) throws Exception {
        if (sKey == null) {
            System.out.print("Key为空null");
            return null;
        }
        // 判断Key是否为16位
        if (sKey.length() != 16) {
            System.out.print("Key长度不是16位");
            return null;
        }
        byte[] raw = sKey.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//"算法/模式/补码方式"
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
 
        return new Base64().encodeToString(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }
 
    // 解密
    public static String Decrypt(String sSrc, String sKey) throws Exception {
        try {
            // 判断Key是否正确
            if (sKey == null) {
                System.out.print("Key为空null");
                return null;
            }
            // 判断Key是否为16位
            if (sKey.length() != 16) {
                System.out.print("Key长度不是16位");
                return null;
            }
            byte[] raw = sKey.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted1 = new Base64().decode(sSrc);//先用base64解密
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original,"utf-8");
                return originalString;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }
 
    public static void main(String[] args) throws Exception {
        /*
         * 此处使用AES-128-ECB加密模式，key需要为16位。
         * {"userno":'jk1008',"code":'egapi_agent',"password":'123456',"sign":'39286EFCA4DEB50552DE2D26179AEA55',"timestamp":'1481089272'}

         */
        String cKey = "Clz07LEV0JzHU76tMrJ6zTUTzLXNq4kKufbhwr3MzGM=";
        // 需要加密的字串
//        String cSrc = "userno";Clz07LEV0JzHU76tMrJ6zTUTzLXNq4kKufbhwr3MzGM=

//        System.out.println(cSrc);
//        // 加密
//        String enString = AES.Encrypt(cSrc, cKey);
//        System.out.println("加密后的字串是：" + enString);
        String encryptResultStr="sRqjn9MMNMn7%2bbSBnxOsxo7XEgFks1yCWnNOpkB%2bujn0Sfq1lR9iylqyQnX%2f%2b77UVEc54u9UNNny7qEUBGgx8fXrXKJi7uVC0SzsghTm%2fUTmgbKMGMNzBtzOfu9kWBdrM1s7ehF6X1ipAzNdOpDd3DSlCDFDEbkv9rVlCls1ZPgeNQ%2b3QVN8S%2ffCzOXetDWz%2fv9jUJy%2bEr1PERbJ60FonoVWa3GqIV3yGTNfk8USNUW%2f0Q4MAxGmAMCqxNurkIHpNNeLca6M7vouSFc4FK8AtP%2bwPi%2b9syZYmqA6mlQsJzPiVU%2bYLAMJ35CiHWxV%2fzXKoT9ttMNDjiNCnNDm9xVpFw%3d%3d";
         System.out.println(URLDecoder.decode(encryptResultStr,"UTF-8"));
        // 解密
        String DeString = AES.Decrypt(URLDecoder.decode(encryptResultStr,"UTF-8"), cKey);
        System.out.println("解密后的字串是：" + DeString);
    }
}
