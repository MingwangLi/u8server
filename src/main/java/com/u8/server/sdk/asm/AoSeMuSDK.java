package com.u8.server.sdk.asm;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.JsonUtils;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lizhong on 2017/11/21.
 * 奥瑟姆 SDK登录认证
 */
public class AoSeMuSDK implements ISDKScript {
    private String user_id;
    private String token;
    private String account;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        user_id = json.getString("uid");
        token = json.getString("token");
        account = json.getString("account");
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("user_id",user_id);
        params.put("token",token);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post_json(channel.getChannelAuthUrl(), JsonUtils.map2JsonStr(params),new UHttpFutureCallback(){
            @Override
            public void completed(String res) {
                JSONObject json = JSONObject.fromObject(res);
                int status = json.getInt("status");
                String user_id = String.valueOf(json.getInt("user_id"));
                if(status == 1){
                    callback.onSuccess(new SDKVerifyResult(true, user_id , account, account));
                    return;
                }
                callback.onFailed("verify failed" + json.toString());
            }
            @Override
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + err);
            }
        });
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
