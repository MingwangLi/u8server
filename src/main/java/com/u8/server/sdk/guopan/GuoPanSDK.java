package com.u8.server.sdk.guopan;

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
 * 果盘SDK v1.0.2
 * Created by lizhong on 2017/10/20.
 */
public class GuoPanSDK implements ISDKScript{
    private String game_uin;
    private String token;
    private String appid;
    private String accountName;
    private String t;
    private String sign;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
            JSONObject json = JSONObject.fromObject(extension);
            game_uin = json.getString("game_uin");
            token = json.getString("token");
            accountName = json.getString("accountName");
            appid = channel.getCpAppID();
            t = System.currentTimeMillis() + "";
            Map<String,String> params = new HashMap<String, String>();
            params.put("game_uin", game_uin);
            params.put("appid", channel.getCpAppID());
            params.put("token", token);
            params.put("t", t);
            StringBuilder sb = new StringBuilder();
            sb.append(game_uin).append(appid).append(t).append(channel.getCpAppSecret());
            sign = EncryptUtils.md5(sb.toString());
            params.put("sign", sign);
            String url = channel.getChannelAuthUrl();
            UHttpAgent.getInstance().get(url, params, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {
                    if("true".equals(result)){
                        callback.onSuccess(new SDKVerifyResult(true, game_uin, accountName, accountName));
                        return;
                    }
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the get result is " + result);
                }
                @Override
                public void failed(String err) {
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
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
