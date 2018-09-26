package com.u8.server.sdk.sy72g;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import net.sf.json.JSONObject;

/**
 * @Author: lizhong
 * @Des: 72G手游SDK 1.0.5
 * @Date: 2018/3/7 10:58
 * @Modified:
 */
public class SY72GSDK implements ISDKScript{
    private String uid;
    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback) throws Exception {
        JSONObject json = JSONObject.fromObject(extension);
        uid = json.getString("uid");
        callback.onSuccess(new SDKVerifyResult(true,uid,uid,uid));
        return;
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
