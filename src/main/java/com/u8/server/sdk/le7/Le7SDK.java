package com.u8.server.sdk.le7;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lizhong on 2017/11/16.
 */
public class Le7SDK implements ISDKScript{
    private String userId;
    private String logintime;
    private String username;
    private String sign;
    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        userId = json.getString("userId");
        logintime = json.getString("logintime");
        username = json.getString("username");
        sign = json.getString("sign");
        Map<String, String> params = new HashMap<String, String>();
        params.put("userId", userId);
        params.put("logintime", logintime);
        params.put("sign", sign);
        if(isSignOK(params,channel)){
            callback.onSuccess(new SDKVerifyResult(true,userId,username,username));
            return;
        }
        callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the sign is error");
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
    public boolean isSignOK(Map<String, String> params, UChannel channel) {
        StringBuilder sb = new StringBuilder();
        sb.append(params.get("userId"))
                .append(params.get("logintime"))
                .append(channel.getCpAppKey());
        return  EncryptUtils.md5(sb.toString()).toLowerCase().equals(params.get("sign"));
    }
}
