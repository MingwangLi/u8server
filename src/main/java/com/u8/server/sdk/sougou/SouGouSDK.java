package com.u8.server.sdk.sougou;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.SignUtils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 搜狗SDK
 * Created by ant on 2016/5/10.
 */
public class SouGouSDK implements ISDKScript{

    private Logger logger = LoggerFactory.getLogger(SouGouSDK.class);

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        try{
            logger.debug("----搜狗游戏登陆认证extension:{}",extension);
            JSONObject json = JSONObject.fromObject(extension);
            String sessionKey = json.getString("sessionkey");
            final String channelUserID = json.getString("uid");
            String appKey = channel.getCpAppSecret();
            String gid = channel.getCpAppID();
            Map<String,String> signParams = new HashMap<>();
            signParams.put("gid",gid);
            signParams.put("sessionKey",sessionKey);
            signParams.put("userId",channelUserID);
            StringBuilder sb = new StringBuilder();
            sb.append("gid=").append(gid).
                    append("&sessionKey=").append(sessionKey).
                    append("&userId=").append(channelUserID);
            logger.debug("----搜狗游戏appKey:{}",appKey);
            logger.debug("----搜狗游戏登录认证签名体:{}",URLEncoder.encode(sb.toString())+"&"+appKey);
            String auth = EncryptUtils.md5(sb.toString()+"&"+appKey);
            logger.debug("----搜狗游戏auth:{}",auth);
            Map<String,String> params = new HashMap<String, String>();
            params.put("gid", channel.getCpAppID());
            params.put("sessionKey", sessionKey);
            params.put("userId", channelUserID);
            params.put("auth", auth);
            String url = channel.getChannelAuthUrl();
            UHttpAgent.getInstance().post(url, params, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {
                    try {
                        logger.debug("----搜狗游戏登录认证方返回的数据:{}",result);
                        JSONObject json = JSONObject.fromObject(result);
                        final int success = 0;
                        int code = json.getInt("code");
                        if (success == code) {
                            callback.onSuccess(new SDKVerifyResult(true,channelUserID,channelUserID,channelUserID));
                            return;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("----搜狗游戏登录认证解析仿佛数据异常,异常信息:{},返回数据:{}",e.getMessage(),result);
                        callback.onFailed(channel.getMaster().getSdkName() + " verify Exception. the exception is " + e.getMessage());
                    }

                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the result is " + result);
                }

                @Override
                public void failed(String e) {
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + e);
                }

            });

        }catch(Exception e){
            e.printStackTrace();
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is "+e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess(order.getChannel().getPayCallbackUrl());
        }
    }
}
