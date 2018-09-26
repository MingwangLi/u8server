package com.u8.server.sdk.egame;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 爱游戏
 * Created by ant on 2016/11/25.
 */
public class EGameSDK implements ISDKScript{

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {


        try{

            String code = extension;

            UHttpAgent httpClient = UHttpAgent.getInstance();

            Map<String,String> params = new HashMap<String, String>();
            params.put("grant_type","authorization_code");
            params.put("code",code);

            params.put("client_secret", channel.getCpAppSecret());
            RequestParasUtil.signature("2", channel.getCpAppID(), channel.getCpAppSecret(), "MD5", "v1.0", params);

            httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {

                    Log.d("The auth result is " + result);

                    JSONObject json = JSONObject.fromObject(result);
                    if(json.containsKey("user_id")){

                        String userID = json.getString("user_id");

                        callback.onSuccess(new SDKVerifyResult(true, userID, "", ""));
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
            callback.onSuccess(order.getChannel().getCpConfig());
        }
    }

}
