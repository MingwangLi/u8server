package com.u8.server.sdk.yiliu;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;


/**
 * 天启（16游戏） SDK
 * Author: lizhong
 * Date: 2017/12/4.
 * Version: 火速V7.2
 */

public class YiLiuSDK implements ISDKScript {
    //傻逼 SDK是通过反射实例化的(单例) 多线程下必有线程安全问题  By MingwangLi 2018-07-19
    //private String app_id;
    //private String mem_id;
    //private String user_token;
    //private String sign;
    //认证
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(final UChannel channel, final String extension, final ISDKVerifyListener callback) {
        logger.info("----天启登录认证获取参数:{}",extension);
        JSONObject json = JSONObject.fromObject(extension);
        String app_id = channel.getCpAppID();
        final String mem_id = json.getString("mem_id");
        String user_token = json.getString("user_token");
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", app_id);
        params.put("mem_id", mem_id);
        params.put("user_token", user_token);
        String sign = generateSign(params,channel);
        params.put("sign",sign);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
            @Override
            public void completed(String result) {
                logger.info("----天启登录认证返回数据:{}",result);
                JSONObject json = JSONObject.fromObject(result);
                int status = json.getInt("status");
                String msg = json.optString("msg");
                if (1 == status) {
                    callback.onSuccess(new SDKVerifyResult(true, mem_id, mem_id, mem_id));
                    return;
                }
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
            }
            @Override
            public void failed(String e) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + e);
            }

        });
    }
    //生成sign
    public static String generateSign(Map<String,String> params,UChannel channel) {
        StringBuilder sb = new StringBuilder();
        sb.append("app_id=").append(params.get("app_id"))
                .append("&mem_id=").append(params.get("mem_id"))
                .append("&user_token=").append(params.get("user_token"))
                .append("&app_key=").append(channel.getCpAppKey());
        return EncryptUtils.md5(sb.toString());
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }

}
