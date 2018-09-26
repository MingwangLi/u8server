package com.u8.server.sdk.zll;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

/**
 * Created by lz on 2017/8/8.
 * 掌乐乐SDK登录认证
 */
public class ZLLSDK implements ISDKScript {
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post_json(channel.getChannelAuthUrl(),json.toString(),new UHttpFutureCallback(){
            @Override
            public void completed(String res) {
                JSONObject json = JSONObject.fromObject(res);
                int status = json.getInt("status");
                String user_account = json.getString("user_account");
                String user_id = String.valueOf(json.getInt("user_id"));
                if(status == 1){
                    callback.onSuccess(new SDKVerifyResult(true, user_id , user_account, user_account));
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
