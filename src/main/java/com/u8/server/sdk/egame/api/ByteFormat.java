/**
 * 
 */
package com.u8.server.sdk.egame.api;

/**
 * Description TODO
 * 
 * @ClassName ByteFormat
 * 
 * @Copyright 炫彩互动
 * 
 * @Project openAPI
 * 
 * @Author dubin
 * 
 * @Create Date 014-5-15
 * 
 * @Modified by noregularne
 * 
 * @Modified Date
 */
public class ByteFormat {
    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);

        for (int i = 0; i < bArray.length; ++i) {
            String sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public static byte[] hexStringToBytes(String str) {
        if (str == null) {
            return null;
        }

        char[] hex = str.toCharArray();

        int length = hex.length / 2;
        byte[] raw = new byte[length];
        for (int i = 0; i < length; ++i) {
            int high = Character.digit(hex[(i * 2)], 16);
            int low = Character.digit(hex[(i * 2) + 1], 16);
            int value = high << 4 | low;
            if (value > 127)
                value -= 256;
            raw[i] = (byte) value;
        }
        return raw;
    }
}
