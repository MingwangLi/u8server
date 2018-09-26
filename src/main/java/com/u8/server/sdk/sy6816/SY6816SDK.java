package com.u8.server.sdk.sy6816;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import net.sf.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * 6816手游SDK
 * Created by lizhong on 2017/11/27.
 */
public class SY6816SDK implements ISDKScript{
    private String ac;
    private String uid;
    private String username;
    private String appid;
    private String sdkversion;
    private String sessionid;
    private String time;
    private String sign;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) throws UnsupportedEncodingException {
        JSONObject json = JSONObject.fromObject(extension);
        ac = "checklogin";
        uid = json.getString("uid");
        username = json.getString("username");
        appid = channel.getCpAppID();
        sdkversion = "1.4";
        sessionid = json.getString("sessionid");
        if(sessionid.contains("%2F")){
            sessionid = URLDecoder.decode(sessionid,"UTF-8");
            sessionid.replace(" ","+");
        }
        time = System.currentTimeMillis() + "";
        Map<String, String> params = new HashMap<String,String>();
        params.put("ac",ac);
        params.put("appid",appid);
        params.put("sdkversion",sdkversion);
        params.put("sessionid",sessionid);
        params.put("time",time);
        sign = generateSign(params,channel);
        params.put("sign",sign);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
            @Override
            public void completed(String res) {
                JSONObject res_json = JSONObject.fromObject(res);
                String status = res_json.getString("status");
                if ("success".equals(status)){
                    JSONObject userInfo = JSONObject.fromObject(res_json.getString("userInfo"));
                    String uid = userInfo.getString("uid");
                    String username = userInfo.getString("username");
                    callback.onSuccess(new SDKVerifyResult(true,uid,username,username));
                    return;
                }
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed." );
            }

            @Override
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is "+err);
            }
        });
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
    public  String generateSign(Map<String, String> params, UChannel channel) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = params.get(k);
            postdatasb.append(k+"="+ v+"&");
        }
        postdatasb.deleteCharAt(postdatasb.length()-1);
        postdatasb.append(channel.getCpAppKey());
        return EncryptUtils.md5(postdatasb.toString());
    }
}
