/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.u8.server.sdk.gg;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class SignUtils {
    private static String transdata;
    private static String sign;
    private static String signtype;

    /**
     * 组装请求参数
     *
     * @param respData
     *          从爱贝服务端获取的签名数据
     * @return 包含各个签名数据的一个map，有transdata，sign，signtype三个
     * .................
     */
    public static Map<String, String> getParmters(String respData) {
        //开始分割参数
        transdata = "transdata"; // "{\"loginname\":\"18701637882\",\"userid\":\"14382295\"}";
        sign = "sign"; // "HU6L6dZNR0PJEgsINI5Dlt2L2WfCsN8WDAUP+i/mLNIIwMVCHBBB6GKSrLvz10B5w5LGnX0PQf74oJx8O7JBOMJyQ7oQWoIs4NcpRi73BSxqdnt8XUTIBjfg33sfuGCCQO6GEW6gFHnocsXzNq8MIWk9mvCOFRL3pp/GmKdbbhQ=";
        signtype = "signtype"; // "RSA";

        Map<String, String> reslutMap = new HashMap<String, String>();

        String[] dataArray = respData.split("&");

        for (String s : dataArray) {

            if (s.startsWith(transdata)) {
                reslutMap.put(transdata, s.substring(s.indexOf("=") + 1, s.length()));
            } else if (s.startsWith(signtype)) {
                reslutMap.put(signtype, s.substring(s.indexOf("=") + 1, s.length()));
            } else if (s.startsWith(sign)) {
                reslutMap.put(sign, s.substring(s.indexOf("=") + 1, s.length()));
            }
        }
        return reslutMap;
    }
}
