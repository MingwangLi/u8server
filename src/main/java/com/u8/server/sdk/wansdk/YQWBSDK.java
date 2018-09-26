package com.u8.server.sdk.wansdk;

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
 * @Des: 宝莲灯  一起玩吧/玩币SDK
 * @Date: 2018/3/21 17:36
 * @Modified:
 */
public class YQWBSDK implements ISDKScript{
    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback) throws Exception {
        JSONObject json = JSONObject.fromObject(extension);
        String uid = json.getString("uid");
        callback.onSuccess(new SDKVerifyResult(true,uid,uid,uid));
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        if(callback != null){
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
