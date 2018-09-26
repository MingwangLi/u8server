package com.u8.server.sdk.cczs;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 虫虫SDK
 * Created by lizhong on 2017/11/16.
 */
public class CCZSSDK implements ISDKScript {

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        try{

            JSONObject json = JSONObject.fromObject(extension);
            final String uid = json.getString("uid");
            final String username = json.getString("userName");
            final String token = json.getString("token");

            UHttpAgent httpClient = UHttpAgent.getInstance();

            Map<String,String> params = new HashMap<String, String>();
            params.put("user_id",uid);
            params.put("token",token);

            httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {

                    Log.d("The auth result is " + result);

                    if("success".equalsIgnoreCase(result)){

                        callback.onSuccess(new SDKVerifyResult(true, uid, username, username));
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
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
