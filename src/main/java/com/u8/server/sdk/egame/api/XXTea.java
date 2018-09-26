/**
 * 
 */
package com.u8.server.sdk.egame.api;

/**
 * Description TODO
 * 
 * @ClassName XXTea
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
public class XXTea {

    public static String encrypt(String plain, String charset, String hexKey) throws Exception {
        if ((plain == null) || (charset == null) || (hexKey == null)) {
            return null;
        }

        return ByteFormat.bytesToHexString(encrypt(plain.getBytes(charset), ByteFormat.hexStringToBytes(hexKey)));
    }

    public static String decrypt(String cipherHex, String charset, String hexKey) throws Exception {
        if ((cipherHex == null) || (charset == null) || (hexKey == null)) {
            return null;
        }
        return new String(decrypt(ByteFormat.hexStringToBytes(cipherHex), ByteFormat.hexStringToBytes(hexKey)), charset);
    }

    public static byte[] encrypt(byte[] plainData, byte[] key) {
        if ((plainData == null) || (plainData.length == 0) || (key == null)) {
            return null;
        }
        return toByteArray(encrypt(toIntArray(plainData, true), toIntArray(key, false)), false);
    }

    public static byte[] decrypt(byte[] cipherData, byte[] key) {
        if ((cipherData == null) || (cipherData.length == 0) || (key == null)) {
            return null;
        }
        return toByteArray(decrypt(toIntArray(cipherData, false), toIntArray(key, false)), true);
    }

    private static int[] encrypt(int[] v, int[] k) {
        int n = v.length - 1;

        if (n < 1) {
            return v;
        }
        if (k.length < 4) {
            int[] key = new int[4];

            System.arraycopy(k, 0, key, 0, k.length);
            k = key;
        }
        int z = v[n];
        int y = v[0];
        int delta = -1640531527;
        int sum = 0;
        int q = 6 + 52 / (n + 1), e, p;

        while (q-- > 0) {
            sum += delta;
            e = sum >>> 2 & 0x3;
            for (p = 0; p < n; ++p) {
                y = v[(p + 1)];
                z = v[p] += ((z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[(p & 0x3 ^ e)] ^ z));
            }
            y = v[0];
            z = v[n] += ((z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[(p & 0x3 ^ e)] ^ z));
        }
        return v;
    }

    private static int[] decrypt(int[] v, int[] k) {
        int n = v.length - 1;

        if (n < 1) {
            return v;
        }
        if (k.length < 4) {
            int[] key = new int[4];

            System.arraycopy(k, 0, key, 0, k.length);
            k = key;
        }
        int z = v[n];
        int y = v[0];
        int delta = -1640531527;
        int q = 6 + 52 / (n + 1), e, p;

        int sum = q * delta;
        while (sum != 0) {
            e = sum >>> 2 & 0x3;
            for (p = n; p > 0; --p) {
                z = v[(p - 1)];
                y = v[p] -= ((z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[(p & 0x3 ^ e)] ^ z));
            }
            z = v[n];
            y = v[0] -= ((z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[(p & 0x3 ^ e)] ^ z));
            sum -= delta;
        }
        return v;
    }

    private static int[] toIntArray(byte[] data, boolean includeLength) {
        int n = ((data.length & 0x3) == 0) ? data.length >>> 2 : (data.length >>> 2) + 1;
        int[] result;
        if (includeLength) {
            result = new int[n + 1];
            result[n] = data.length;
        } else {
            result = new int[n];
        }
        n = data.length;
        for (int i = 0; i < n; ++i) {
            result[(i >>> 2)] |= (0xFF & data[i]) << ((i & 0x3) << 3);
        }
        return result;
    }

    private static byte[] toByteArray(int[] data, boolean includeLength) {
        int n = data.length << 2;
        if (includeLength) {
            int m = data[(data.length - 1)];

            if ((m > n) || (m <= 0)) {
                return null;
            }
            n = m;
        }

        byte[] result = new byte[n];

        for (int i = 0; i < n; ++i) {
            result[i] = (byte) (data[(i >>> 2)] >>> ((i & 0x3) << 3) & 0xFF);
        }
        return result;
    }

}
