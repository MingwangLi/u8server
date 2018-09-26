package com.u8.server.sdk.kumi;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

import java.util.*;

/**
 * 酷米网SDK
 * create by lizhong
 */
public class KMSDK implements ISDKScript {
    public String app_id = null;
    public String mem_id = null;
    public String app_key = null;
    public String user_token = null;
    public String sign = null;

    @Override
    public void verify(final UChannel channel, final String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        app_id = channel.getCpAppID();
        mem_id = json.getString("memId");
        user_token = json.getString("token");
        app_key = channel.getCpAppKey();
        Map<String, String> params = new LinkedHashMap <String, String>();
        params.put("app_id", app_id);
        params.put("mem_id", mem_id);
        params.put("user_token", user_token);
        sign = generateSign(params,app_key);
        params.put("sign", sign);


        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
            @Override
            public void completed(String result) {
                    JSONObject json = JSONObject.fromObject(result);
                    String status = json.getString("status");
                    String msg = json.getString("msg");
                    if(status.equals("1")) {
                        callback.onSuccess(new SDKVerifyResult(true, mem_id, mem_id, mem_id, msg));
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
    public static String generateSign(Map<String, String> params,String app_key) {
        List<String> keys = new ArrayList<String>(params.keySet());
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = String.valueOf(params.get(k));
            postdatasb.append(k + "=" + v + "&");
        }
        postdatasb.append("app_key=").append(app_key);
        //postdatasb.deleteCharAt(postdatasb.length() - 1);
        //对排序后的参数附加开发商签名密钥
        return EncryptUtils.md5(postdatasb.toString()).toLowerCase();
    }

}
