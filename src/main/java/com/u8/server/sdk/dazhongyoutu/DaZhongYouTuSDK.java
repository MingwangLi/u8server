package com.u8.server.sdk.dazhongyoutu;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * Created by Administrator on 2016/10/13.
 */
public class DaZhongYouTuSDK implements ISDKScript {


    public String sessionid = "";

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {

        sessionid = extension;
        Log.d("========================sessionid=================================== " + sessionid);
        try {
            UHttpAgent httpClient = UHttpAgent.getInstance();
            Map<String, String> params = new HashMap<String, String>();
            params.put("ac", "check");
            params.put("appid", channel.getAppID() + "");
            params.put("sessionid", sessionid);
            params.put("sdkversion", "1.0");
            params.put("time", System.currentTimeMillis() + "");
            String sign = generateSign(params, channel.getCpAppKey());
            params.put("sign", sign);


            httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {

                @Override
                public void completed(String result) {
                    Log.d("The auth result is " + result);

                    JSONObject json = JSONObject.fromObject(result);
                    if (!json.containsKey("returnCode") || "0".equals(json.getString("returnCode"))) {
                        callback.onSuccess(new SDKVerifyResult(true, sessionid, "", ""));
                        return;
                    }
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
                }

                @Override
                public void failed(String err) {
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
                }
            });

        } catch (Exception e) {
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is " + e.getMessage());
            Log.e(e.getMessage());
        }
    }


    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }


    /***
     *
     *
     *
     * @param params
     * @param signKey
     * @return
     */
    public static String generateSign(Map<String, String> params, String signKey) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = params.get(k);
            postdatasb.append(k + "=" + v + "&");
        }
        postdatasb.deleteCharAt(postdatasb.length() - 1);
        postdatasb.append("&signKey=" + signKey);
        String sign = md5(postdatasb.toString().getBytes());
        Log.d("the sign data is " + postdatasb.toString());
        return sign;
    }

    public static String md5(byte[] source) {
        String s = null;
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.update(source);
            byte tmp[] = md.digest();
            char str[] = new char[16 * 2];
            int k = 0;
            for (int i = 0; i < 16; i++) {
                byte byte0 = tmp[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            s = new String(str);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return s;
    }


}
