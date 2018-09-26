package com.u8.server.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class SignUtils {

    private static Logger logger = LoggerFactory.getLogger(SignUtils.class);


    /**
     * 参数排序 key1=value1&key2=value2...appKey
     * @param params
     * @param appKey
     * @param name
     * @return
     */
    public static String createSign(Map<String,String> params, String appKey,String name) {
        Set<String> set = params.keySet();
        Set<String> param = new TreeSet<>();
        param.addAll(set);
        StringBuilder sb = new StringBuilder();
        for (String key:param) {
            sb.append(key).append("=").append(params.get(key)).append("&");
        }
        String string = sb.substring(0,sb.length()-1);
        string +=appKey;
        logger.debug("----{}签名体:{}",name,string);
        return EncryptUtils.md5(string).toLowerCase();
    }

    /**
     * 参数排序  key1=value1&key2=value2...&appKey
     * @param params
     * @param appKey
     * @param name
     * @return
     */
    public static String createSignWithLastYu(Map<String,String> params, String appKey,String name) {
        Set<String> set = params.keySet();
        Set<String> param = new TreeSet<>();
        param.addAll(set);
        StringBuilder sb = new StringBuilder();
        for (String key:param) {
            sb.append(key).append("=").append(params.get(key)).append("&");
        }
        //String string = sb.substring(0,sb.length()-1);
        //string +=appKey;
        String string = sb.append(appKey).toString();
        logger.debug("----{}签名体:{}",name,string);
        return EncryptUtils.md5(string).toLowerCase();
    }


    /**
     * 参数排序  key1=URLEncode.encode(value1)&key2=URLEncode.encode(value2)...appKey
     * @param params
     * @param appKey
     * @param name
     * @return
     */
    public static String createSignWithURLEncode(Map<String,String> params, String appKey,String name) {
        Set<String> set = params.keySet();
        Set<String> param = new TreeSet<>();
        param.addAll(set);
        StringBuilder sb = new StringBuilder();
        for (String key:param) {
            sb.append(key).append("=").append(URLEncoder.encode(params.get(key))).append("&");
        }
        String string = sb.substring(0,sb.length()-1);
        string +=appKey;
        logger.debug("----{}签名体:{}",name,string);
        return EncryptUtils.md5(string).toLowerCase();
    }

}
