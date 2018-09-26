package com.u8.server.utils;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ant on 2015/4/11.
 */
public class RSAUtils {

    public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM_MD5 = "MD5withRSA";
    public static final String SIGNATURE_ALGORITHM_SHA = "SHA1WithRSA";

    private static final String PUBLIC_KEY = "RSAPublicKey";
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 生成公钥和私钥
     *
     * @return
     * @throws Exception
     */
    public static Map<String, Object> generateKeys() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator
                .getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(1024);

        KeyPair keyPair = keyPairGen.generateKeyPair();

        // 公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        // 私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        Map<String, Object> keyMap = new HashMap<String, Object>(2);

        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /**
     * 使用公钥对RSA签名有效性进行检查
     * @param content 待签名数据
     * @param sign 签名值
     * @param publicKey  爱贝公钥
     * @param input_charset 编码格式
     * @return 布尔值
     */
    public static boolean verify(String content, String sign, String publicKey, String input_charset)
    {
        return verify(content, sign, publicKey, input_charset, SIGNATURE_ALGORITHM_MD5);
    }

    /**
     * 使用公钥对RSA签名有效性进行检查
     * @param content 待签名数据
     * @param sign 签名值
     * @param publicKey  爱贝公钥
     * @param input_charset 编码格式
     * @return 布尔值
     */
    public static boolean verify(String content, String sign, String publicKey, String input_charset, String algorithm)
    {
        try
        {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            byte[] encodedKey = Base64.decode2Bytes(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));


            java.security.Signature signature = java.security.Signature
                    .getInstance(algorithm);

            signature.initVerify(pubKey);
            signature.update( content.getBytes(input_charset));

            return signature.verify( Base64.decode2Bytes(sign) );

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 使用私钥对数据进行RSA签名
     * @param content 待签名数据
     * @param privateKey 商户私钥
     * @param input_charset 编码格式
     * @return 签名值
     */
    public static String sign(String content, String privateKey, String input_charset)
    {
        return sign(content, privateKey, input_charset, SIGNATURE_ALGORITHM_MD5);
    }

    /**
     * 使用私钥对数据进行RSA签名
     * @param content 待签名数据
     * @param privateKey 商户私钥
     * @param input_charset 编码格式
     * @return 签名值
     */
    public static String sign(String content, String privateKey, String input_charset, String algorithm)
    {
        try
        {
            PKCS8EncodedKeySpec priPKCS8 	= new PKCS8EncodedKeySpec( Base64.decode2Bytes(privateKey) );
            KeyFactory keyf 				= KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey priKey 				= keyf.generatePrivate(priPKCS8);

            java.security.Signature signature = java.security.Signature
                    .getInstance(algorithm);

            signature.initSign(priKey);
            signature.update( content.getBytes(input_charset) );

            byte[] signed = signature.sign();

            return Base64.encode(signed);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 取得私钥
     *
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);

        return Base64.encode(key.getEncoded());
    }

    /**
     * 取得公钥
     *
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static String getPublicKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);

        return Base64.encode(key.getEncoded());
    }

    public static void main(String[] args) throws Exception {


        String result = sign("","MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJ2vRSFv4FscUzKAg4+TJskxS5guG2sd+IWa65PPcae6tC2UCOghcIy21tdPQXUxvkT/SDspOzcThsQE1bNhBpRkzIPROW5w401JoOMGR6Moxm50c2k5Hlml50sC3aRIQvhoMp8cLpiqhYZl8EmBw8WvIqxiz6e6ncwqvxeWgYFtAgMBAAECgYBRZhPysdUIU3PjQxInAJhqDadvVIXU4vDSz2lCsTiDlE7EpIGAixqVmSEInqI0xEvrRDjeSWeHA+5yq+hBJBoHmLqBwct4wy+/bF5d+3UOFaQlAE2c8csk6SPDYRJ3+qeU2IBebCPloEuPWynA7UVUkcJS+dRcyD/g0+65gI225QJBAOnP8c5RXQFqCeiKpGRgwmjvqV/5oP1RhH3qQWY7vVvCSq5kSR7jsHwnv2ifkpXrAEAjp0cEdFrOrjCjoDIQxysCQQCspfB7vhrQRlcPaPb3aqF/uMjQkIjJ4L5kd3AUAs4Sot7GThsNt9vWfTK+/CExP+Nl+DJOJGVpDQuHrqBnTI3HAkBkV+fwoZ6GotmJwSZ4JcaJfoF0PQ/IZ5my6DwVAgJsROAEI+n1pcXyIqTU49bnHCnJXQPHSLQe3KCJI0/27JhtAkAc6i28i7ox55XtHsc96L3jhi8bvxkptlosuVQsBcw9ksl8rNgbFA/dfFpjYhatCOYszcSfEblqPqUPtE9o/YlBAkBC1dCEsMyXepn/dDGbdfO9JQMZ6jgiMa3BoWhYRi81VttXpTlQeQFtzrjrQTomvjv2DnMYNff9+uXxDyKcJseS\n","UTF-8");
        System.out.println(result);
        // Map<String, Object> keys = generateKeys();
        //
        // String pubKey = getPublicKey(keys);
        // String priKey = getPrivateKey(keys);
        //
        // System.out.println("The pubKey is ");
        // System.out.println(pubKey);
        // System.out.println(priKey);

    }

}
