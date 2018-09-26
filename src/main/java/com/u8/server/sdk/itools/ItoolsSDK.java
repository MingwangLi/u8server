package com.u8.server.sdk.itools;

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
 * Itools Android渠道
 * Created by lizhong on 2018/03/23.
 */
public class ItoolsSDK implements ISDKScript{
    private String uid;
    private String userName;
    private String sessionid;
    private String appid;
    private String sign;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        uid = json.getString("uid");
        sessionid = json.getString("sessionId");
        userName = json.getString("userName");
        appid = channel.getCpAppID();
        Map<String,String> params = new HashMap<String, String>();
        params.put("appid", appid);
        params.put("sessionid", sessionid);

        StringBuilder sb = new StringBuilder();
        sb.append("appid=").append(appid).append("&sessionid=").append(sessionid);
        sign = EncryptUtils.md5(sb.toString()).toLowerCase();
        params.put("sign", sign);

        UHttpAgent.getInstance().get(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
            @Override
            public void completed(String res) {
                    if(res != null && res.contains("success")){
                        callback.onSuccess(new SDKVerifyResult(true,uid,userName,userName));
                        return;
                    }
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the result is " + res);
            }

            @Override
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
            }
        });

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
