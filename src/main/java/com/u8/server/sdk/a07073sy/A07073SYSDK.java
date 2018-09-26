package com.u8.server.sdk.a07073sy;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import net.sf.json.JSONObject;

/**
 * Created by lz on 2017/8/10.
 */
public class A07073SYSDK implements ISDKScript{
    private String uid;
    private String username;
    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        uid = json.getString("uid");
        username = json.getString("username");
        callback.onSuccess(new SDKVerifyResult(true,uid,username,username));
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
