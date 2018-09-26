/*
Copyright (C) Huawei Technologies Co., Ltd. 2015. All rights reserved.
See LICENSE.txt for this sample's licensing information.
*/

package com.u8.server.sdk.huawei.huawei;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Cipher;


public class RSAUtil {
	
    public static final String KEY_ALGORITHM = "RSA";


    public static final String SIGNATURE_ALGORITHM = "SHA1WithRSA";
    

    public static final String SIGNATURE_ALGORITHM_SHA256 = "SHA256WithRSA";


    public static final String PUBLIC_KEY = "RSAPublicKey";


    public static final String PRIVATE_KEY = "RSAPrivateKey";


    private static final int MAX_ENCRYPT_BLOCK = 117;
    

    private static final int MAX_DECRYPT_BLOCK = 128;


    public static String toBase64String(Key key)
    {
        return Base64Util.encode(key.getEncoded());
    }

    private static Key toPublicKey(String sKey) throws Exception
    {
    	 return toKey(ByteUtil.decodeHex(sKey.toCharArray()), true);
    }
    
    private static Key toKey(byte[] keyBytes, boolean isPublic) throws Exception
    {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        if (isPublic)
        {
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
            return keyFactory.generatePublic(x509KeySpec);
        }
        else
        {
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            return keyFactory.generatePrivate(pkcs8KeySpec);
        }
    }

    public static String encryptByPrivateKey(byte[] data, String privateKey) throws Exception
    {
        byte[] keyBytes = Base64Util.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateK);
        signature.update(data);
        return Base64Util.encode(signature.sign());
    }
    
    
    public static String sha256WithRsa(byte[] data, String privateKey) throws Exception
    {
        byte[] keyBytes = Base64Util.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM_SHA256);
        signature.initSign(privateK);
        signature.update(data);
        return Base64Util.encode(signature.sign());
    }

    
    public static boolean verify(byte[] data, String publicKey, String sign)
            throws Exception
    {
        byte[] keyBytes = Base64Util.decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM_SHA256);
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(Base64Util.decode(sign));
    }


    public static byte[] encryptByPublicKey(byte[] data, String sKey)
            throws Exception
    {
        Key key = toPublicKey(sKey);
        return doFinal(key, data, Cipher.ENCRYPT_MODE, MAX_ENCRYPT_BLOCK);
    }
    
    public static byte[] decryptByPublicKey(byte[] encryptedData, String sKey)
            throws Exception
    {
        Key key = toPublicKey(sKey);
        return doFinal(key, encryptedData, Cipher.DECRYPT_MODE,
                MAX_DECRYPT_BLOCK);
    }

    private static byte[] doFinal(Key key, byte[] data, int mode, int maxBlock)
            throws Exception
    {
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM,"BC");
        cipher.init(mode, key);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        while (inputLen - offSet > 0)
        {
            if (inputLen - offSet > maxBlock)
            {
                cache = cipher.doFinal(data, offSet, maxBlock);
            }
            else
            {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxBlock;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    
    public static String getPrivateKey(Map<String, Object> keyMap)
            throws Exception
    {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return Base64Util.encode(key.getEncoded());
    }

   
    public static String getPublicKey(Map<String, Object> keyMap)
            throws Exception
    {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return Base64Util.encode(key.getEncoded());
    }

   

    public static class PVKey
    {
        private String publicKey;
        private String privateKey;

        public String getPublicKey()
        {
            return publicKey;
        }

        public String getPrivateKey()
        {
            return privateKey;
        }

        public PVKey(String publicKey, String privateKey)
        {
            super();
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        @Override
        public String toString()
        {
            return "PVKey [publicKey=" + publicKey + ", privateKey="
                    + privateKey + "]";
        }

    }
   
    public static String getRechargeRSASign(Map<String, String> params, String privateKey)
        throws Exception
    {
        StringBuffer base = new StringBuffer();
        Map<String, String> tempMap = new TreeMap<String, String>();
        
        String k = null;
        String v = null;
       
        for (Map.Entry<String, String> entry : params.entrySet())
        {
  
            k = entry.getKey();
            v = entry.getValue();
            tempMap.put(k + "=" + v, v);
        }
   
        for (Map.Entry<String, String> entry : tempMap.entrySet())
        {
        
            k = entry.getKey();
            base.append(k).append("&");
        }
     
        base.deleteCharAt(base.length() - 1);
        
 
        String sign = null;
        sign = encryptByPrivateKey(base.toString().getBytes("UTF-8"), privateKey);
        return sign;
    }
}
