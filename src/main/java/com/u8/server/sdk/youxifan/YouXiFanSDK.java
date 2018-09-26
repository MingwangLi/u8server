package com.u8.server.sdk.youxifan;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

/**
 * @Author: lz
 * @Date: 2017/1/18 10:10.
 * 游戏FanSDK 登录认证
 */
public class YouXiFanSDK implements ISDKScript{
    private String userId = null;//用户ID
    private String username = null;//用户名
    private String logintime = null;//登陆成功时间戳
    private String sign = null;//登陆成功后的签名

    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        userId = json.getString("userId");
        username = json.getString("username");
        logintime = json.getString("logintime");
        sign = json.getString("sign");
        if(isSignOK(channel,username,logintime,sign)){
            callback.onSuccess(new SDKVerifyResult(true,userId,username,username,""));
        }else {
            callback.onFailed("verify failed | sign error");
        }
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
    public boolean isSignOK(UChannel channel,String username,String logintime,String sign) {
            StringBuilder sb = new StringBuilder();
            sb.append("username=").append(username)
                    .append("&appkey=").append(channel.getCpAppKey())
                    .append("&logintime=").append(logintime);
            String strMd5 = EncryptUtils.md5(sb.toString()).toLowerCase();
        return sign.equals(strMd5);
    }
}
