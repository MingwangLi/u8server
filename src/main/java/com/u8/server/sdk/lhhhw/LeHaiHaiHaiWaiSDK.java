package com.u8.server.sdk.lhhhw;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import java.util.HashMap;
import java.util.Map;


public class LeHaiHaiHaiWaiSDK implements ISDKScript {

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        //Log.d("----extension:"+extension);
        JSONObject object = JSONObject.fromObject(extension);
        String username = object.getString("username");
        String token = object.getString("token");
        String pid = channel.getCpAppID();
        String secret = channel.getCpAppSecret();
        StringBuilder sb = new StringBuilder();
        sb.append("pid=").append(pid).
                append("&token=").append(token).
                append("&username=").append(username).
                append(secret);
        Log.d("乐嗨嗨海外签名体:%s",sb.toString());
        String sign = EncryptUtils.md5(sb.toString()).toLowerCase();
        Map<String,String> param = new HashMap<>();
        param.put("username",username);
        param.put("token",token);
        param.put("pid",pid);
        param.put("sign",sign);
        UHttpAgent.getInstance().post(channel.getMaster().getAuthUrl(), param, new UHttpFutureCallback() {
            @Override
            public void completed(String content) {
                try {
                    Log.d("乐嗨嗨海外登录认证返回数据:%s",content);
                    JSONObject jsonObject = JSONObject.fromObject(content);
                    int state = jsonObject.getInt("state");
                    String msg = jsonObject.getString("msg");
                    jsonObject = jsonObject.getJSONObject("data");
                    String backUsername = jsonObject.getString("username");
                    String channelUserID = jsonObject.getString("uid");
                    Log.d("data数据:",jsonObject.toString());
                    if (1 != state) {
                        callback.onFailed("乐嗨嗨海外登录认证失败:"+msg);
                        return;
                    }
                    callback.onSuccess(new SDKVerifyResult(true,channelUserID,backUsername,backUsername));
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed("乐嗨嗨海外登录认证出现异常,异常信息:"+e.getMessage());
                }
            }

            @Override
            public void failed(String err) {
                callback.onFailed(err);
            }
        });

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }

}
