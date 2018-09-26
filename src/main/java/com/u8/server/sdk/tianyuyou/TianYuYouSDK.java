package com.u8.server.sdk.tianyuyou;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *  天宇游SDK create by lizhong
 *  on 2017/10/12 18:21
 * */
public class TianYuYouSDK implements ISDKScript{
    private String app_id;
    private String mem_id;
    private String user_token;
    private String sign;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        app_id = channel.getCpAppID();
        mem_id = json.getString("mem_id");
        user_token = json.getString("user_token");
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("app_id", app_id);
        params.put("mem_id", mem_id);
        params.put("user_token", user_token);
        sign = generateSign(params,channel);
        params.put("sign",sign);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post_json(channel.getChannelAuthUrl(), JsonUtils.map2JsonStr(params), new UHttpFutureCallback() {
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
    public static String generateSign(Map<String, Object> params,UChannel channel) {
        List<String> keys = new ArrayList<String>(params.keySet());
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = String.valueOf(params.get(k));
            postdatasb.append(k + "=" + v + "&");
        }
        postdatasb.append("app_key=").append(channel.getCpAppKey());
//        postdatasb.deleteCharAt(postdatasb.length() - 1);
        //对排序后的参数附加开发商签名密钥
        String signStr = postdatasb.toString().toLowerCase();
        String sign = EncryptUtils.md5(signStr);
        return sign;
    }
}
