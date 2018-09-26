package com.u8.server.sdk.qidian;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 奇点 SDK
 * Author: lizhong
 * Date: 2017/12/14.
 * Version: 火速V7.0
 */

public class QiDianSDK implements ISDKScript {
    private String app_id;
    private String mem_id;
    private String user_token;
    private String sign;
    //认证
    @Override
    public void verify(final UChannel channel, final String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        app_id = channel.getCpAppID();
        mem_id = json.getString("mem_id");
        user_token = json.getString("user_token");
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", app_id);
        params.put("mem_id", mem_id);
        params.put("user_token", user_token);
        sign = generateSign(params,channel);
        params.put("sign",sign);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
            @Override
            public void completed(String result) {
                JSONObject json = JSONObject.fromObject(result);
                int status = json.getInt("status");
                String msg = json.optString("msg");
                if (1 == status) {
                    callback.onSuccess(new SDKVerifyResult(true, mem_id, mem_id, mem_id));
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
    //生成sign
    public static String generateSign(Map<String,String> params,UChannel channel) {
        StringBuilder sb = new StringBuilder();
        sb.append("app_id=").append(params.get("app_id"))
                .append("&mem_id=").append(params.get("mem_id"))
                .append("&user_token=").append(params.get("user_token"))
                .append("&app_key=").append(channel.getCpAppKey());
        return EncryptUtils.md5(sb.toString());
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }

}
