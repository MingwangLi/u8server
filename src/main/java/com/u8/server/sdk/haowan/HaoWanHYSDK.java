package com.u8.server.sdk.haowan;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import net.sf.json.JSONObject;
/**
 * Author: lizhong
 * Date: 2017.12.11
 * Desc: 濠玩互娱SDK
 * Ver: 1.0.3
 * */
public class HaoWanHYSDK implements ISDKScript{
    private String userid;
    private String username;
    private String token;
    private String phone;
    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback) throws Exception {
        JSONObject json = JSONObject.fromObject(extension);
        userid = json.getString("userid");
        username = json.getString("username");
        token = json.getString("token");
        phone = json.getString("phone");
        if(!"".equals(userid)){
            callback.onSuccess(new SDKVerifyResult(true,userid,username,username));
            return;
        }
        callback.onFailed(channel.getMaster().getSdkName() + " verify failed.");
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        if(callback != null){
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
