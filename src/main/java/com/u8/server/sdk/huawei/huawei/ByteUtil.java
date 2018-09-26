package com.u8.server.sdk.huawei.huawei;

import java.io.UnsupportedEncodingException;

/**
 *
 * Created by ant on 2016/10/17.
 */
public class ByteUtil {

    private static final int MAX_LINE_CHAR = 16;
    private byte[] buf = null;
    private int initSize = 1024;
    private int offset = 0;

    public ByteUtil()
    {
        this.buf = new byte[this.initSize];
    }

    public ByteUtil(int paramInt)
    {
        this.initSize = paramInt;
        this.buf = new byte[paramInt];
    }

    public int getSize()
    {
        return this.offset;
    }

    public int getBufferSize()
    {
        return this.buf.length;
    }

    public void writeBytes(byte[] paramArrayOfByte, int paramInt)
    {
        if (paramInt <= 0)
            return;
        if (this.buf.length - this.offset >= paramInt)
        {
            System.arraycopy(paramArrayOfByte, 0, this.buf, this.offset, paramInt);
        }
        else
        {
            byte[] arrayOfByte = new byte[this.buf.length + paramInt << 1];
            System.arraycopy(this.buf, 0, arrayOfByte, 0, this.offset);
            System.arraycopy(paramArrayOfByte, 0, arrayOfByte, this.offset, paramInt);
            this.buf = arrayOfByte;
        }
        this.offset += paramInt;
    }

    public static String printData(byte[] paramArrayOfByte)
    {
        StringBuffer localStringBuffer = new StringBuffer(paramArrayOfByte.length * 3);
        int k = 0;
        while (k < paramArrayOfByte.length)
        {
            int i;
            if (((i = paramArrayOfByte[k] & 0xFF) <= 9) && (i >= 0))
                localStringBuffer.append('0').append(i);
            else
                localStringBuffer.append(Integer.toHexString(i));
            if (++k % 16 == 0)
            {
                String str;
                try
                {
                    str = new String(paramArrayOfByte, k - 16, 16, "UTF-8");
                }
                catch (UnsupportedEncodingException localUnsupportedEncodingException1)
                {
                    str = "";
                }
                localStringBuffer.append("    ").append(str);
                localStringBuffer.append("\r\n");
            }
            if (k == paramArrayOfByte.length - 1)
            {
                int j = k % 16;
                int l = 16 - j;
                for (int i1 = 0; i1 < l; ++i1)
                    localStringBuffer.append("   ");
                try
                {
                    localStringBuffer.append("    ").append(new String(paramArrayOfByte, k - j, j, "UTF-8"));
                }
                catch (UnsupportedEncodingException localUnsupportedEncodingException2)
                {
                    localStringBuffer.append("    ");
                }
            }
            localStringBuffer.append(" ");
        }
        return localStringBuffer.toString();
    }

    public void clean()
    {
        this.buf = new byte[this.initSize];
        this.offset = 0;
    }

    public byte[] getBytes()
    {
        if (this.offset <= 0)
            return new byte[0];
        byte[] arrayOfByte = new byte[this.offset];
        System.arraycopy(this.buf, 0, arrayOfByte, 0, this.offset);
        return arrayOfByte;
    }

    public static String byteArrayToHex(byte[] paramArrayOfByte)
    {
        char[] arrayOfChar1 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] arrayOfChar2 = new char[paramArrayOfByte.length << 1];
        int i = 0;
        for (int l : paramArrayOfByte)
        {
            arrayOfChar2[(i++)] = arrayOfChar1[(l >>> 4 & 0xF)];
            arrayOfChar2[(i++)] = arrayOfChar1[(l & 0xF)];
        }
        return new String(arrayOfChar2);
    }

    public static byte[] decodeHex(char[] paramArrayOfChar)
            throws IllegalArgumentException
    {
        int i;
        if (((i = paramArrayOfChar.length) & 0x1) != 0)
            throw new IllegalArgumentException("Odd number of characters.");
        byte[] arrayOfByte = new byte[i >> 1];
        int j = 0;
        int k = 0;
        while (k < i)
        {
            int l = toDigit(paramArrayOfChar[k], k) << 4;
            l |= toDigit(paramArrayOfChar[(++k)], k);
            ++k;
            arrayOfByte[j] = (byte)l;
            ++j;
        }
        return arrayOfByte;
    }

    protected static int toDigit(char paramChar, int paramInt)
            throws IllegalArgumentException
    {
        int i;
        if ((i = Character.digit(paramChar, 16)) == -1)
            throw new IllegalArgumentException("Illegal hexadecimal character " + paramChar + " at index " + paramInt);
        return i;
    }

}
