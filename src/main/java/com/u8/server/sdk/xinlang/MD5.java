package com.u8.server.sdk.xinlang;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5
{

    static final String HEXES = "0123456789abcdef";

    public static String getHex(byte[] raw)
    {
        if (raw == null) { return null; }
        final StringBuilder hex = new StringBuilder (2 * raw.length);
        for ( final byte b : raw )
        {
            hex.append (HEXES.charAt ((b & 0xF0) >> 4)).append (HEXES.charAt ((b & 0x0F)));
        }
        return hex.toString ();
    }

    public static String md5sum(String str)
    {
        // MD5算法
        MessageDigest messageDigest = null;

        try
        {
            messageDigest = MessageDigest.getInstance ("MD5");
            messageDigest.reset ();
            messageDigest.update (str.getBytes ("UTF-8"));
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace ();
        }

        byte[] byteArray = messageDigest.digest ();
        StringBuffer md5StrBuff = new StringBuffer ();

        for ( int i = 0 ; i < byteArray.length ; i++ )
        {
            if (Integer.toHexString (0xFF & byteArray[i]).length () == 1) md5StrBuff.append ("0").append (Integer.toHexString (0xFF & byteArray[i]));
            else md5StrBuff.append (Integer.toHexString (0xFF & byteArray[i]));
        }

        return md5StrBuff.toString ();
    }
    
    private static final char hexDigits[] = { 
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static String hexdigest(String string) {
        String s = null;

        try {
            s = hexdigest(string.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String hexdigest(byte[] bytes) {
        String s = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            byte tmp[] = md.digest();
            char str[] = new char[16 * 2];
            int k = 0;
            for (int i = 0; i < 16; i++) {
                byte byte0 = tmp[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            s = new String(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }
    
}
