package com.u8.server.sdk.jinmu;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.test.DESCode;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 金木SDK
 */
public class JinMuSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        logger.info("----金木SDK登录认证获取参数:{}",extension);
        try {
            JSONObject json = JSONObject.fromObject(extension);
            final String uid = json.getString("uid");
            String token = json.getString("token");
            //解密uid    token没有加密
            //uid = DESCode.responseXml(uid);  //不需要 反而以后还要加密的
            //token = DESCode.responseXml(token);
            String game_id = channel.getCpAppID();
            String app_key = channel.getCpAppKey();
            String url = channel.getChannelAuthUrl();
            Map<String ,String> param = new HashMap<>();
            param.put("uid",uid);
            param.put("usertoken",token);
            param.put("game_id",game_id);
            StringBuilder sb = new StringBuilder();
            //uid 加密
            sb.append("uid=").append(uid).
                    append("&game_id=").append(game_id).
                    append("&usertoken=").append(token).
                    append("&app_key=").append(app_key);
            logger.debug("----金木登录认证签名体:{}",sb.toString());
            String sign = EncryptUtils.md5(sb.toString());
            param.put("sign",sign);
            UHttpAgent.getInstance().post(url, param, new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----金木SDK登录认证返回数据:{}",content);
                    //解密
                    content = DESCode.responseXml(content);
                    logger.info("----金木SDK登录认证返回解密数据:{}",content);
                    JSONObject result = JSONObject.fromObject(content);
                    String status = result.getString("status");
                    String userId = result.getString("uid");
                    if ("10000".equals(status)) {
                        //金木 uid需要解密
                        userId = DESCode.responseXml(uid);
                        callback.onSuccess(new SDKVerifyResult(true,userId,userId,userId));
                        return;
                    }
                    callback.onFailed("----the status is error status:"+status);
                }

                @Override
                public void failed(String err) {
                    callback.onFailed(err);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----金木SDK登录认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
