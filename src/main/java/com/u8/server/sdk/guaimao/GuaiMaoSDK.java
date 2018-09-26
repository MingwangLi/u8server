package com.u8.server.sdk.guaimao;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GuaiMaoSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        logger.debug("----怪猫登陆认证extension:{}",extension);
        JSONObject object = JSONObject.fromObject(extension);
        String token = object.getString("token");
        final String action = "user.info";
        String url = channel.getMaster().getAuthUrl();
        logger.debug("----怪猫登陆认证url:{}",url);
        Map<String,String> map = new HashMap<>();
        map.put("action",action);
        map.put("token",token);
        logger.debug("----怪猫登陆认证参数:{}",map.toString());
        UHttpAgent.getInstance().post(url, map, new UHttpFutureCallback() {

            @Override
            public void completed(String content) {
                try {
                    logger.debug("----怪猫登陆认证返回数据:{}",content);
                    JSONObject jsonObject = JSONObject.fromObject(content);
                    String uid = jsonObject.getString("uid");
                    String nickname = jsonObject.getString("nickname");
                    callback.onSuccess(new SDKVerifyResult(true,uid,nickname,nickname));
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("----怪猫登陆认证异常:{}",e.getMessage());
                    callback.onFailed(e.getMessage());
                }
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
