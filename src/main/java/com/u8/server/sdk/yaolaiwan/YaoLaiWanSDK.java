package com.u8.server.sdk.yaolaiwan;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class YaoLaiWanSDK implements ISDKScript {
    public String app_id;
    public String mem_id;
    public String user_token;
    public String sign;
    @Override
    public void verify(final UChannel channel, final String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        app_id = channel.getCpAppID();
        mem_id = json.optString("mem_id");
        user_token = json.optString("user_token");
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id",app_id);
        params.put("mem_id",mem_id);
        params.put("user_token",user_token);
        sign = generateSign(params,channel);
        params.put("sign",sign);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
            @Override
            public void completed(String res) {
                JSONObject res_json = JSONObject.fromObject(res);
                String status = res_json.getString("status");
                String msg = res_json.getString("msg");
                if("1".equals(status)){
                    callback.onSuccess(new SDKVerifyResult(true,mem_id,mem_id,mem_id,msg));
                    return;
                }
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + res);
            }

            @Override
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
            }
        });
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }

    //sign=md5(app_id=1&mem_id=23&user_token=rkmi2huqu9dv6750g5os11ilv2&app_key=de933fdbede098c62cb309443c3cf251)
    public static String generateSign(Map<String,String> params, UChannel channel) {
        StringBuilder sb = new StringBuilder();
        sb.append("app_id=").append(params.get("app_id"))
                .append("&mem_id=").append(params.get("mem_id"))
                .append("&user_token=").append(params.get("user_token"))
                .append("&app_key=").append(channel.getCpAppKey());
        return EncryptUtils.md5(sb.toString());
    }
}
