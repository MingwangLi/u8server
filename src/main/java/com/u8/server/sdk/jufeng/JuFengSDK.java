package com.u8.server.sdk.jufeng;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import net.sf.json.JSONObject;

/**
 * 飓风DK created by lizhong
 * DATE: 2017/11/23.
 * */
public class JuFengSDK implements ISDKScript{
    private String userId;
    private String userName;
    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback){
        JSONObject json = JSONObject.fromObject(extension);
        userId = json.getString("userId");
        userName = json.getString("userName");
        if(userId != null && userName != null){
            callback.onSuccess(new SDKVerifyResult(true,userId,userName,userName));
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
