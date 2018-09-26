package com.u8.server.sdk.yanmen;

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
 * 宴门SDK
 * Created by ant on 2016/11/17.
 */
public class YanMenSDK implements ISDKScript{

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {


        try{

            JSONObject json = JSONObject.fromObject(extension);
            final String username = json.getString("username");
            final String logintime = json.getString("logintime");
            final String sign = json.getString("sign");

            StringBuilder sb = new StringBuilder();
            sb.append("username=").append(username).append("&appkey=").append(channel.getCpAppKey()).append("&logintime=").append(logintime);
            String md5local = EncryptUtils.md5(sb.toString()).toLowerCase();

            if(md5local.equals(sign)){
                callback.onSuccess(new SDKVerifyResult(true, username, username, ""));
                return;
            }else{
                callback.onFailed("yanmen check sign failed.");
            }


        }catch (Exception e){
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is "+e.getMessage());
            Log.e(e.getMessage());
        }

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }

}
