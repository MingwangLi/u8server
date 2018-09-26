package com.u8.server.sdk.putao;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import net.sf.json.JSONObject;

/**
 * 葡萄游戏SDK created by lizhong
 * DATE: 2017/10/20.
 * */
public class PuTaoSDK implements ISDKScript{
    private String uid;
    private String token;
    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback){
        JSONObject json = JSONObject.fromObject(extension);
        uid = json.getString("uid");
        token = json.getString("token");
        if(uid != null && token != null){
            callback.onSuccess(new SDKVerifyResult(true,uid,uid,uid));
            return;
        }
        callback.onFailed(extension);
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
