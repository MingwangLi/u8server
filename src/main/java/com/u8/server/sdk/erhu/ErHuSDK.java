package com.u8.server.sdk.erhu;

import com.u8.server.constants.SDKStateCode;
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

public class ErHuSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        logger.info("----二狐游戏登陆认证获取参数:{}",extension);
        try {
            JSONObject json = JSONObject.fromObject(extension);
            String username = json.getString("userName");
            String time = json.getString("time");
            String sid = json.getString("sid");
            String appId = channel.getCpAppID();
            final String merchantId = "1745";
            String merchantAppId = channel.getCpID();
            Map<String,String > params = new HashMap<>();
            params.put("username",username);
            params.put("time",time);
            params.put("sid",sid);
            params.put("appId",appId);
            params.put("merchantId",merchantId);
            params.put("merchantAppId",merchantAppId);
            String url = channel.getChannelAuthUrl();
            logger.debug("----二狐游戏登陆认证url:{}",url);
            UHttpAgent.getInstance().post(url, params, new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----二狐登陆认证返回数据:{}",content);
                    JSONObject object = JSONObject.fromObject(content);
                    Integer result = object.getInt("result");
                    if (SDKStateCode.LOGINSUCCESS == result) {
                        String username = object.getString("username");
                        String uid = object.getString("uid");
                        callback.onSuccess(new SDKVerifyResult(true,uid,username,username));
                        return;
                    }
                    callback.onFailed(content);
                }

                @Override
                public void failed(String err) {
                    callback.onFailed(err);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----二狐登陆认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        UChannel channel = order.getChannel();
        if (null == channel) {
            logger.warn("----二狐获取订单号查询渠道不存在,channelID:{}",order.getChannelID());
            return;
        }
        JSONObject json = new JSONObject();
        json.put("payCallbackUrl",channel.getPayCallbackUrl());
        StringBuilder content = new StringBuilder();
        String ids = "1745"+channel.getCpID()+channel.getCpAppID();
        content.append(ids).append(user.getChannelUserName()).append(order.getOrderID()).append(order.getProductName()).append(order.getMoney()/100).append(channel.getCpAppSecret());
        logger.debug("----二狐获取订单号签名体:{}",content.toString());
        String sign = EncryptUtils.md5(content.toString()).toLowerCase();
        json.put("sign",sign);
        callback.onSuccess(json.toString());
    }
}
