package com.u8.server.sdk.hhw;

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
 * 好好玩SDK
 */
public class HaoHaoWanSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        try {
            logger.info("----好好玩SDK登陆认证获取参数:{}",extension);
            JSONObject object = JSONObject.fromObject(extension);
            final String username = object.getString("username");
            String appid = object.getString("appid");
            String gameid = object.getString("gameid");
            final String logintime  = object.getString("logintime");
            Map<String,String> param = new HashMap<>();
            param.put("username",username);
            param.put("appid",appid);
            param.put("gameid",gameid);
            param.put("logintime",logintime);
            String key = channel.getCpAppKey();
            StringBuilder sb = new StringBuilder();
            sb.append("appkey=").append(key).append("&logintime=").append(logintime).append("&username=").append(username);
            logger.debug("----好好玩SDK登陆认证签名体:{}",sb.toString());
            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();
            param.put("sign",sign);
            String url = channel.getChannelAuthUrl();
            logger.debug("----好好玩SDK登陆认证url:{},参数:{}",url,param);
            UHttpAgent.getInstance().post(url, param, new UHttpFutureCallback() {

                @Override
                public void completed(String content) {
                    logger.info("----好好玩SDK登陆认证返回数据:{}",content);
                    JSONObject json = JSONObject.fromObject(content);
                    String status = json.getString("status");
                    if ("1".equals(status)) {
                        callback.onSuccess(new SDKVerifyResult(true,username,username,username));  //该渠道没有返回uid 将用户名作为channleUserID存在问题 因为不同的游戏的用户名可以相同 我们要求channelUserID唯一
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
            logger.error("----好好玩SDK登陆认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
