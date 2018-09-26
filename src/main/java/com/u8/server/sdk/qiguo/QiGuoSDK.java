package com.u8.server.sdk.qiguo;

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
 * @Date: Created in 11:33 2017/9/1
 * @Description: 七果SDK
 * @Modify By:
 */
public class QiGuoSDK implements ISDKScript{
    private String userId;
    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback) {
        userId = extension;
        if(userId != null){
            callback.onSuccess(new SDKVerifyResult(true,userId,userId,userId));
            return;
        }
        callback.onFailed("verify failed");
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
