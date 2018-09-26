package com.u8.server.sdk.ayy;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 *  爱应用SDK Created By lizhong
 *  Date: 2017/10/26.
 */
public class AyySDK implements ISDKScript{
    private String appid;//应用appid
    private String uid;//userid
    private String t;//vtoken

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        appid = channel.getCpAppID();
        JSONObject json = JSONObject.fromObject(extension);
        uid = json.getString("uid");
        t = json.getString("token");
        Map<String , String> params = new HashMap<String, String>();
        params.put("appid",appid);
        params.put("uid",uid);
        params.put("t",t);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
            @Override
            public void completed(String result) {
                if ("success".equals(result)){
                    callback.onSuccess(new SDKVerifyResult(true,uid,uid,uid));
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
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
