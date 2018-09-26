package com.u8.server.sdk.papa;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 啪啪游戏厅
 * Created by ant on 2016/11/9.
 */
public class PaPaSDK implements ISDKScript{

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {


        try{

            JSONObject json = JSONObject.fromObject(extension);
            final String uid = json.getString("uid");
            final String token = json.getString("token");

            UHttpAgent httpClient = UHttpAgent.getInstance();

            Map<String,String> params = new HashMap<String, String>();
            params.put("app_key",channel.getCpAppKey());
            params.put("token", token);
            params.put("uid", uid);
            params.put("sign", generateSign(channel, uid, token));


            httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {

                    JSONObject json = JSONObject.fromObject(result);

                    if(json.containsKey("error") && json.getInt("error") == 0){

                        JSONObject jData = json.getJSONObject("data");
                        if(jData.containsKey("is_success") && jData.getBoolean("is_success")){
                            callback.onSuccess(new SDKVerifyResult(true, uid, "", ""));
                            return;
                        }

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

    private String generateSign(UChannel channel, String uid, String token){
        StringBuilder sb = new StringBuilder();
        sb.append(channel.getCpAppKey()).append(channel.getCpAppSecret())
                .append("app_key=").append(channel.getCpAppKey())
                .append("&token=").append(token).append("&uid=").append(uid);

        Log.d("the papa login verify sign str:%s",sb.toString());

        return EncryptUtils.md5(sb.toString()).toLowerCase();
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }

}
