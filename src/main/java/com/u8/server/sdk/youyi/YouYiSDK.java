package com.u8.server.sdk.youyi;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class YouYiSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        logger.info("----优亿登录认证获取参数:{}",extension);
        JSONObject object = JSONObject.fromObject(extension);
        final String openid = object.getString("openId");
        String token = object.getString("tokenId");
        Map<String,String> map = new HashMap<>();
        map.put("openid",openid);
        map.put("token",token);
        String url = channel.getChannelAuthUrl();
        logger.debug("-----优亿登录认证url:{}",url);
        UHttpAgent.getInstance().get(url, map, new UHttpFutureCallback() {
            @Override
            public void completed(String content) {
                logger.info("----优亿登录认证返回数据:{}",content);
                JSONObject jsonObject = JSONObject.fromObject(content);
                String code = jsonObject.getString("code");
                if ("I001000".equals(code)) {
                    callback.onSuccess(new SDKVerifyResult(true,openid,openid,openid));
                    return;
                }
                callback.onFailed(content);
            }

            @Override
            public void failed(String err) {
                callback.onFailed(err);
            }
        });
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
