package com.u8.server.sdk.liebao;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

import java.util.*;

/**
 * 猎宝游戏
 * Created by lizhong on 2017/09/18.
 */
public class LiebaoSDK implements ISDKScript{
    private String gameid;
    private String username;
    private String logintime;
    private String sign;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        try{
            JSONObject json = JSONObject.fromObject(extension);
            username = json.getString("username");
            logintime = json.getString("logintime");
            gameid = channel.getCpID();
            Map<String,String> params = new LinkedHashMap<String, String>();
            params.put("gameid",gameid);
            params.put("username",username);
            params.put("logintime",logintime);
            sign = generateSign(params,channel);
            params.put("sign",sign);
            UHttpAgent httpClient = UHttpAgent.getInstance();
            httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {
                    JSONObject json = JSONObject.fromObject(result);
                    String code = json.getString("code");
                    boolean status = json.getBoolean("status");
                    String msg = json.getString("msg");
                    if("200".equals(code)&&status){
                        callback.onSuccess(new SDKVerifyResult(true,username,username,username,msg));
                        return;
                    }
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
                }
                @Override
                public void failed(String err) {
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is "+e.getMessage());
        }

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess("");
        }
    }
    public static String generateSign(Map<String, String> params,UChannel channel) {
        List<String> keys = new ArrayList<String>(params.keySet());
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = String.valueOf(params.get(k));
            postdatasb.append(k + "=" + v + "&");
        }
        postdatasb.append("appkey=").append(channel.getCpAppKey());
        //postdatasb.deleteCharAt(postdatasb.length() - 1);
        //对排序后的参数附加开发商签名密钥
        return EncryptUtils.md5(postdatasb.toString()).toLowerCase();
    }
}
