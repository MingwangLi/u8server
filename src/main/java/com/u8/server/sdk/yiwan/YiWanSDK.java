package com.u8.server.sdk.yiwan;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

/**
 * 奕玩SDK
 * Created by lizhong on 2017/11/27.
 */
public class YiWanSDK implements ISDKScript{
    private String username;
    private String logintime;
    private String sign;
    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        username = json.getString("username");
        logintime = json.getString("logintime");
        sign = json.getString("sign");
        if(verifySign(channel)){
            callback.onSuccess(new SDKVerifyResult(true,username,username,username));
            return;
        }
        callback.onFailed(channel.getMaster().getSdkName() + " verify failed." );
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
    //登陆签名校验
    public boolean verifySign (UChannel channel){
        StringBuilder sb = new StringBuilder();
        sb.append("username=").append(username)
                .append("&appkey=").append(channel.getCpAppKey())
                .append("&logintime=").append(logintime);
        return EncryptUtils.md5(sb.toString()).equals(sign);
    }
}
