package com.u8.server.sdk.aile;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 爱乐 SDK
 * create by lizhong
 */

public class AiLeSDK implements ISDKScript {

    public String app_id = null;
    public String mem_id = null;
    public String token = null;
    public String sign = null;

    @Override
    public void verify(final UChannel channel, final String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        app_id = channel.getCpAppID();
        mem_id = json.optString("mem_id");
        token = json.optString("user_token");
        sign = generateSign(app_id, mem_id, token, channel.getCpAppKey());
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", app_id);
        params.put("mem_id", mem_id);
        params.put("user_token", token);
        params.put("sign", sign);
        UHttpAgent httpClient = UHttpAgent.getInstance();

        httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {

            @Override
            public void completed(String result) {
                JSONObject json = JSONObject.fromObject(result);
                String status = json.optString("status");
                String msg = json.optString("msg");
                if ("1".equals(status)) {
                    callback.onSuccess(new SDKVerifyResult(true, mem_id, mem_id, "", msg));
                    return;
                }
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
            }

            @Override
            public void failed(String e) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + e);
            }

        });
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
    //登录 生成sign
    public static String generateSign(String appid, String mem_id, String user_token, String app_key) {
        String gignRes =
                "app_id=" + appid +
                        "&mem_id=" + mem_id +
                        "&user_token=" + user_token +
                        "&app_key=" + app_key;

        String sign = md5(gignRes.toString());
        return sign;
    }

    public static String md5(String name) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffers = md.digest(name.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < buffers.length; i++) {
                String s = Integer.toHexString(0xff & buffers[i]);
                if (s.length() == 1) {
                    sb.append("0" + s);
                }
                if (s.length() != 1) {
                    sb.append(s);
                }
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}
