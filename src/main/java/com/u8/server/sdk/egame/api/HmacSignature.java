/**
 * 
 */
package com.u8.server.sdk.egame.api;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Description TODO
 * 
 * @ClassName HmacSignature
 * 
 * @Copyright 炫彩互动
 * 
 * @Project openAPI
 * 
 * @Author dubin
 * 
 * @Create Date 2014-5-16
 * 
 * @Modified by none
 * 
 * @Modified Date
 */
public class HmacSignature {

    public static byte[] initHmacMD5Key() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("HmacMD5");

        SecretKey secretKey = generator.generateKey();

        byte[] key = secretKey.getEncoded();
        return key;
    }

    public static String encodeHmacMD5(byte[] data, byte[] key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, "HmacMD5");

        Mac mac = Mac.getInstance(secretKey.getAlgorithm());

        mac.init(secretKey);

        byte[] digest = mac.doFinal(data);
        return ByteFormat.bytesToHexString(digest);
    }

    public static byte[] initHmacSHAKey() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("HmacSHA1");

        SecretKey secretKey = generator.generateKey();

        byte[] key = secretKey.getEncoded();
        return key;
    }

    public static String encodeHmacSHA1(byte[] data, byte[] key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, "HmacSHA1");

        Mac mac = Mac.getInstance(secretKey.getAlgorithm());

        mac.init(secretKey);

        byte[] digest = mac.doFinal(data);

        return ByteFormat.bytesToHexString(digest);
    }

    public static byte[] initHmacSHA256Key() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("HmacSHA256");

        SecretKey secretKey = generator.generateKey();

        byte[] key = secretKey.getEncoded();
        return key;
    }

    public static String encodeHmacSHA256(byte[] data, byte[] key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, "HmacSHA256");

        Mac mac = Mac.getInstance(secretKey.getAlgorithm());

        mac.init(secretKey);

        byte[] digest = mac.doFinal(data);
        return ByteFormat.bytesToHexString(digest);
    }

    public static byte[] initHmacSHA384Key() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("HmacSHA384");

        SecretKey secretKey = generator.generateKey();

        byte[] key = secretKey.getEncoded();
        return key;
    }

    public static String encodeHmacSHA384(byte[] data, byte[] key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, "HmacSHA384");

        Mac mac = Mac.getInstance(secretKey.getAlgorithm());

        mac.init(secretKey);

        byte[] digest = mac.doFinal(data);
        return ByteFormat.bytesToHexString(digest);
    }

    public static byte[] initHmacSHA512Key() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("HmacSHA512");

        SecretKey secretKey = generator.generateKey();

        byte[] key = secretKey.getEncoded();
        return key;
    }

    public static String encodeHmacSHA512(byte[] data, byte[] key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, "HmacSHA512");

        Mac mac = Mac.getInstance(secretKey.getAlgorithm());

        mac.init(secretKey);

        byte[] digest = mac.doFinal(data);
        return ByteFormat.bytesToHexString(digest);
    }

    public static String encodeMD5(String data) throws Exception {
        byte[] bytes = data.getBytes("utf-8");
        return encodeMD5(bytes);
    }

    public static String encodeMD5(byte[] data) throws Exception {
        String signStr = "";

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(data);
        byte[] digest = md5.digest();
        signStr = ByteFormat.bytesToHexString(digest);

        return signStr;
    }

}
