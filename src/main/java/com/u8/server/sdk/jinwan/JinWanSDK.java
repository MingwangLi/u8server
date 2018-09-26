package com.u8.server.sdk.jinwan;

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

public class JinWanSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        try {
            logger.info("----劲玩SDK登陆认证获取参数:{}",extension);
            String appid = channel.getCpAppID();
            JSONObject jsonObject = JSONObject.fromObject(extension);
            final String uid = jsonObject.getString("uid");
            String token = jsonObject.getString("token");
            String time = jsonObject.getString("time");
            String secret = channel.getCpAppSecret();
            String sign = EncryptUtils.md5(appid+uid+token+time+secret).toLowerCase();
            Map<String,String > params = new HashMap<>();
            params.put("appid",appid);
            params.put("uid",uid);
            params.put("token",token);
            params.put("time",time);
            params.put("sign",sign);
            String url = channel.getChannelAuthUrl();
            logger.debug("----劲玩SDK登陆认证url:{},参数:{}",url,params.toString());
            UHttpAgent.getInstance().post(url, params, new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----劲玩SDK登陆认证返回数据:{}",content);
                    if ("success".equals(content)) {
                        callback.onSuccess(new SDKVerifyResult(true,uid,uid,uid));
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
            logger.error("----劲玩SDK登陆认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
