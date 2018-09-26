package com.u8.server.sdk.douyou8;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DouYou8SDK implements ISDKScript{
    private String username;
    private String memkey;
    private String gameid;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        JSONObject json = JSONObject.fromObject(extension);
        username = json.getString("username");
        memkey = json.getString("memkey");
        gameid = channel.getCpID();
        Map<String, String> params = new HashMap<String, String>();
        params.put("username",username);
        params.put("memkey",memkey);
        params.put("gameid",gameid);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
            @Override
            public void completed(String res) {
                callback.onSuccess(new SDKVerifyResult(true,username ,username, username));
                return;
            }
            @Override
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
            }
        });
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
