package com.u8.server.sdk.yaowang;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.JsonUtils;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: lizhong
 * @Des: 遥望SDK v1.2
 * @Date: 2018/3/20 11:25
 * @Modified:
 */
public class YaoWangSDK implements ISDKScript{
    private String openId;
    private String submit_time;
    private String gameId;
    private String sign;
    SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        Date date = new Date();
        JSONObject json = JSONObject.fromObject(extension);
        openId = json.getString("openId");
        submit_time = dateFormater.format(date);
        gameId = channel.getCpAppID();
        Map<String,String> params = new HashMap<String ,String>();
        params.put("openId",openId);
        params.put("submit_time",submit_time);
        params.put("gameId",gameId);
        sign = generateSign(params ,channel);
        params.put("sign",sign);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl()+"sdk/server/checkOpenId.html", params, new UHttpFutureCallback() {
            @Override
            public void completed(String res) {
                JSONObject res_json = JSONObject.fromObject(res);
                int status = res_json.getInt("status");
                if (1 == status){
                    JSONObject data = JSONObject.fromObject(res_json.getString("data"));
                    String userId = data.getString("userId");
                    callback.onSuccess(new SDKVerifyResult(true,userId,userId,userId));
                    return;
                }
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + res);

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
    public String generateSign(Map<String, String> params, UChannel channel) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = params.get(k);
            postdatasb.append(k + "=" + v + "&");
        }
        postdatasb.deleteCharAt(postdatasb.length()-1);
        postdatasb.append(channel.getCpAppKey());
        return DigestUtils.shaHex(postdatasb.toString()).toUpperCase();
    }

}
