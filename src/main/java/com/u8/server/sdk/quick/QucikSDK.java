package com.u8.server.sdk.quick;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class QucikSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, final String extension, final ISDKVerifyListener callback) throws Exception {
        try {
            logger.info("----QuickSDK登陆认证获取参数:{}",extension);
            JSONObject object = JSONObject.fromObject(extension);
            final String uid = object.getString("UID");
            final String userName = object.getString("userName");
            String token = object.getString("token");
            String url = channel.getChannelAuthUrl();
            String product_code = channel.getCpAppKey();
            Map<String,String > params = new HashMap<>();
            params.put("uid",uid);
            params.put("token",token);
            params.put("product_code",product_code);
            UHttpAgent.getInstance().post(url, params, new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----QuickSDK登陆认证返回数据:{}",content);
                    if ("1".equals(content)) {
                        callback.onSuccess(new SDKVerifyResult(true,uid,userName,userName));
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
            logger.error("----QuickSDK登陆认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
