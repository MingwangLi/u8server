package com.u8.server.sdk.manba;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

import java.util.*;

/**
 *  曼巴SDK create by lizhong
 *  on 2018/03/19 20:39
 * */
public class ManBaSDK implements ISDKScript{
    private String cp_game_id;
    private String release_user_id;
    private String release_token;
    private String sign;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        cp_game_id = channel.getCpAppID();
        release_user_id = json.getString("release_user_id");
        release_token = json.getString("release_token");
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("cp_game_id", cp_game_id);
        params.put("release_user_id", release_user_id);
        params.put("release_token", release_token);
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
                    callback.onSuccess(new SDKVerifyResult(true,release_user_id,release_user_id,release_user_id,msg));
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
        Collections.sort(keys);
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = params.get(k);
            postdatasb.append(k + "=" + v + "&");
        }
        postdatasb.append("app_key=").append(channel.getCpAppKey());
//        postdatasb.deleteCharAt(postdatasb.length() - 1);
        //对排序后的参数附加开发商签名密钥
        return EncryptUtils.md5(postdatasb.toString().toLowerCase());
    }
}
