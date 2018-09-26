package com.u8.server.sdk.moge;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import org.apache.http.entity.ByteArrayEntity;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lizhong on 2017/07/25.
 */
public class MoGeSDK implements ISDKScript {

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {

        try {
            JSONObject json = JSONObject.fromObject(extension);
            String username = json.getString("username");
            String appkey = channel.getCpAppKey();
            String logintime = json.getString("logintime");
            String sign = json.getString("sign");
            StringBuilder sb = new StringBuilder();
            sb.append("username=").append(username)
                    .append("&appkey=").append(appkey)
                    .append("&logintime=").append(logintime);
            String newsign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if(newsign.equals(sign)){
                callback.onSuccess(new SDKVerifyResult(true, username, username, username, ""));
                return;
            }
            callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the sign is not match!");
        } catch (Exception e) {
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is " + e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess("");
        }
    }
}
