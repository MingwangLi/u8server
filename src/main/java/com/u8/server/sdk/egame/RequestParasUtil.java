/**
 * 
 */
package com.u8.server.sdk.egame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.u8.server.sdk.egame.api.HmacSignature;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Description TODO
 * 
 * @ClassName RequestParasUtil
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
public class RequestParasUtil {

    private static final Log log = LogFactory.getLog(RequestParasUtil.class);

    private static List<String> paramConcat(Map<String, String> requestData) {
        List<String> params = new ArrayList<String>();

        StringBuffer keySort = new StringBuffer();
        StringBuffer values = new StringBuffer();
        if (requestData != null && requestData.size() > 0) {
            for (Entry<String, String> entry : requestData.entrySet()) {
                String name = entry.getKey() == null ? "" : entry.getKey();
                String value = entry.getValue() == null ? "" : entry.getValue();
                keySort.append(name).append("&");
                values.append(value);
            }
            keySort.deleteCharAt(keySort.lastIndexOf("&"));
        }

        params.add(keySort.toString());
        params.add(values.toString());
        return params;
    }

    public static void signature(String signLevel, String clientId, String clientSecret, String signMethod, String version, Map<String, String> requestData) throws Exception {
        String signature = "";
        String timestamp = "" + System.currentTimeMillis();

        if ((signLevel == null) || (clientId == null) || (clientSecret == null) || (signMethod == null) || (version == null) || (requestData == null)) {
            if (log.isDebugEnabled()) {
                log.debug("signLevel or clientId or clientSecret or signMethod or version or requestData can not be null");
            }
            return;
        }

        if (!"1".equals(signLevel) && !"2".equals(signLevel) && !"3".equals(signLevel)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid signLevel.Correct value: 1/2/3");
            }
            return;
        }

        if (!"MD5".equalsIgnoreCase(signMethod)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid signaMethod. SignMethod of this version must be MD5, since this is a beta version");
            }
            return;
        }

        if (!"1".equals(signLevel)) {
            requestData.put("client_id", clientId);
            requestData.put("sign_method", signMethod);
            requestData.put("version", version);
            requestData.put("timestamp", timestamp);

            List<String> concatList = null;
            Map<String, String> signMap = new HashMap<String, String>();

            if ("2".equals(signLevel)) {
                signMap.put("client_id", clientId);
                signMap.put("sign_method", signMethod);
                signMap.put("version", version);
                signMap.put("timestamp", timestamp);

                signMap.put("client_secret", clientSecret);

            } else if ("3".equals(signLevel)) {
                signMap.putAll(requestData);

                signMap.put("client_secret", clientSecret);
            }

            concatList = paramConcat(signMap);
            String signSort = concatList.get(0);
            String signCotent = concatList.get(1);
            signature = HmacSignature.encodeMD5(signCotent);

            requestData.put("sign_sort", signSort);
            requestData.put("signature", signature);
        }

    }

}
