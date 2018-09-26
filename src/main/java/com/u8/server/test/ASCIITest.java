package com.u8.server.test;

public class ASCIITest {
    public static void main(String[] args) {

        //System.out.println(getUncode("token"));
        System.out.println(unicode2String("\\u9a8c\\u8bc1\\u4e0d\\u6210\\u529f"));
    }

    /**
     * 字符转UNCODE
     * @param str
     * @return
     */
    public static String getUncode(String str) {
        if (str == null) return "";
        String hs = "";
        try {
            byte b[] = str.getBytes("UTF-16");
            for (int n = 0; n < b.length; n++) {
                str = (java.lang.Integer.toHexString(b[n] & 0XFF));
                if (str.length() == 1)
                    hs = hs + "0" + str;
                else
                    hs = hs + str;
                if (n < b.length - 1) hs = hs + "";
            }
//去除第一个标记字符
            str = hs.toUpperCase().substring(4);
            char[] chs = str.toCharArray();
            str = "";
            for (int i = 0; i < chs.length; i = i + 4) {
                str += "\\u" + chs[i] + chs[i + 1] + chs[i + 2] + chs[i + 3];
            }
            return str;
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        return str;
    }

    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }


    /**
     * UNCODE转中文
     * @param unicode
     * @return
     */
    public static String unicode2String(String unicode) {

        StringBuffer string = new StringBuffer();

        String[] hex = unicode.split("\\\\u");

        for (int i = 0; i < hex.length; i++) {

            try {
                // 汉字范围 \u4e00-\u9fa5 (中文)
                if (hex[i].length() >= 4) {//取前四个，判断是否是汉字
                    String chinese = hex[i].substring(0, 4);
                    try {
                        int chr = Integer.parseInt(chinese, 16);
                        boolean isChinese = ASCIITest.isChinese((char) chr);
                        //转化成功，判断是否在  汉字范围内
                        if (isChinese) {//在汉字范围内
                            // 追加成string
                            string.append((char) chr);
                            //并且追加  后面的字符
                            String behindString = hex[i].substring(4);
                            string.append(behindString);
                        } else {
                            string.append(hex[i]);
                        }
                    } catch (NumberFormatException e1) {
                        string.append(hex[i]);
                    }

                } else {
                    string.append(hex[i]);
                }
            } catch (NumberFormatException e) {
                string.append(hex[i]);
            }
        }

        return string.toString();

        }
    }
