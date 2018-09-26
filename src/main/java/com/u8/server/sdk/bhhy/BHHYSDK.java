package com.u8.server.sdk.bhhy;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *  冰狐互娱 SDK create by lizhong
 *  on 2018/03/23 18:21
 * */
public class BHHYSDK implements ISDKScript{
    private String app_id;
    private String mem_id;
    private String user_token;
    private String sign;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        app_id = channel.getCpAppID();
        mem_id = json.getString("memId");
        user_token = json.getString("token");
        Map<String, String> params = new LinkedHashMap<String, String>();
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
                String status = json.getString("status");
                String msg = json.getString("msg");
                if("1".equals(status)){
                    callback.onSuccess(new SDKVerifyResult(true,mem_id,mem_id,mem_id,msg));
                    return;
                }
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
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
    //生成签名
    public static String generateSign(Map<String, String> params,UChannel channel) {
        List<String> keys = new ArrayList<String>(params.keySet());
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = String.valueOf(params.get(k));
            postdatasb.append(k + "=" + v + "&");
        }
        postdatasb.append("app_key=").append(channel.getCpAppKey());
        return EncryptUtils.md5(postdatasb.toString());
    }
}
