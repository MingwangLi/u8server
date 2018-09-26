package com.u8.server.sdk.saman;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 任性 SDK
 * create by lvxinmin
 */

public class SaManSDK implements ISDKScript {

    public String app_id = null;
    public String app_key = null;
    public String token = null;

    public String sign = null;


    @Override
    public void verify(final UChannel channel, final String extension, final ISDKVerifyListener callback) {
        Log.d("----------------------------SDK------------------------------");


        app_id = channel.getCpID();
        app_key = channel.getCpAppKey();
        token = extension;
        sign = generateSign(app_id, token, app_key);


        Map<String, String> params = new HashMap<String, String>();
        params.put("gid", app_id);
        params.put("token", token);
        params.put("sign", sign);


        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.get(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {

            @Override
            public void completed(String result) {
                    JSONObject json = JSONObject.fromObject(result);
                    int status = json.getInt("status_code");
                    String msg = json.optString("data");
                    if (200 == status) {
                        String userId = json.optString("uid");
                        callback.onSuccess(new SDKVerifyResult(true, userId, userId, "", msg));
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
            callback.onSuccess("");
        }
    }


    //登录 生成sign
    public static String generateSign(String appid, String token, String appkey) {
        return EncryptUtils.md5(appid + token + appkey);
    }
}
