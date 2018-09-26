package com.u8.server.sdk.xiantu;

import com.u8.server.constants.SDKStateCode;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;



public class XianTuSDK implements ISDKScript {
    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        Log.d("---------闲兔SDK登陆认证url:%s----参数:%s",channel.getChannelAuthUrl(),extension);
        JSONObject object = JSONObject.fromObject(extension);
        String user_id = object.getString("uid");
        object.remove("uid");
        object.put("user_id",user_id);
        Log.d("----闲兔SDK请求参数:%s",object.toString());
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post_json(channel.getChannelAuthUrl(), object.toString(), new UHttpFutureCallback() {
            @Override
            public void completed(String content) {
                Log.i("---------闲兔SDK登陆认证返回数据:%s",content);
                JSONObject result = JSONObject.fromObject(content);
                int status = result.getInt("status");
                String channelUserID = result.getString("user_id");
                String infomation = result.getString("user_account");
                if (SDKStateCode.LOGINSUCCESS == status) {
                    callback.onSuccess(new SDKVerifyResult(true,channelUserID,infomation,infomation));
                    return;
                }
                callback.onFailed(content);
            }

            @Override
            public void failed(String err) {
                callback.onFailed(err);
            }
        });

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(user.getChannel().getPayCallbackUrl());
    }
}
