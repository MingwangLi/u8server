package com.u8.server.sdk.jinghong;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
/**
 * Author: lizhong
 * Date: 2017.12.14.
 * Desc: 惊鸿互娱SDK
 */
public class JingHongSDK implements ISDKScript{
    private String userid;
    private String token;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        JSONObject json = JSONObject.fromObject(extension);
        userid = json.getString("userid");
        token = json.getString("token");
        Map<String, String> params = new HashMap<String, String>();
        params.put("userid",userid);
        params.put("token",token);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
            @Override
            public void completed(String result) {
                JSONObject res = JSONObject.fromObject(result);
                int status = res.getInt("status");
                if(1 == status) {
                    callback.onSuccess(new SDKVerifyResult(true,userid,userid,userid));
                    return;
                }
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
            }

            @Override
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
            }
        });
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
