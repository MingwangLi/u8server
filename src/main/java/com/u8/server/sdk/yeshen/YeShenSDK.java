package com.u8.server.sdk.yeshen;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 夜神SDK
 * Created by ant on 2016/10/17.
 */
public class YeShenSDK implements ISDKScript{
    private String accessToken;
    private String uid;
    private String appId;
    private String username;
    private String nickname;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        try{

            JSONObject json = JSONObject.fromObject(extension);
            uid = json.getString("uid");
            accessToken = json.getString("accessToken");
            appId = channel.getCpAppID();
            if(json.containsKey("username")){
                username = json.getString("username");
            }
            if(json.containsKey("nickname")){
                nickname = json.getString("nickname");
            }
            Map<String, String > params = new HashMap<String, String>();
            params.put("uid", uid);
            params.put("accessToken", accessToken);
            params.put("appId", appId);
            UHttpAgent httpClient = UHttpAgent.getInstance();
            httpClient.get(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
                @Override
                public void completed(String res) {
                    JSONObject res_json = JSONObject.fromObject(res);
                    String errNum = res_json.getString("errNum");
                    JSONObject transdata = JSONObject.fromObject(res_json.getString("transdata"));
                    int isValidate = transdata.getInt("isValidate");
                    if("0".equals(errNum) && (1 == isValidate)){
                        callback.onSuccess(new SDKVerifyResult(true,uid,username,nickname));
                        return;
                    }
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + res);
                }

                @Override
                public void failed(String err) {
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
                }
            });
            callback.onSuccess(new SDKVerifyResult(true, uid, username, nickname));
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
