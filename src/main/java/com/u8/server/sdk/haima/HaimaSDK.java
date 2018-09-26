package com.u8.server.sdk.haima;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 海马玩SDK
 * Created by ant on 2016/4/23.
 */
public class HaimaSDK implements ISDKScript{

    private Logger logger = LoggerFactory.getLogger(HaimaSDK.class);
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        try{
            logger.debug("----海马SDK登陆认证extension:{}",extension);
            JSONObject json = JSONObject.fromObject(extension);
            final String playerID = json.getString("userId");
            String token = json.getString("token");
            final String username = json.getString("userName");
            UHttpAgent httpClient = UHttpAgent.getInstance();
            Map<String, String> params = new HashMap<String, String>();
            params.put("appid", channel.getCpAppID());
            params.put("t", token);
            params.put("uid", playerID);
            httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {
                    logger.debug("----海马SDK登陆认证返回的resulst:{}",result);
                    if(result.contains("success")){
                        callback.onSuccess(new SDKVerifyResult(true, playerID, username, ""));
                        return;
                    }
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
                }

                @Override
                public void failed(String e) {
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + e);
                }
            });

        }catch (Exception e){
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is "+e.getMessage());
            Log.e(e.getMessage());
        }

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess(order.getChannel().getPayCallbackUrl());
        }
    }
}
