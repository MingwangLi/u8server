package com.u8.server.sdk.tcy;

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

public class TCYSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        logger.info("----同城玩登陆认证获取参数:{}",extension);
        try {
            JSONObject json = JSONObject.fromObject(extension);
            Map<String,String> params = new HashMap<>();
            String appID = channel.getCpAppID();
            params.put("appID",appID);
            final String userID = json.getString("userID");
            params.put("userID",userID);
            String accessToken = json.getString("accessToken");
            params.put("accessToken",accessToken);
            StringBuilder sb = new StringBuilder();
            sb.append(accessToken).append(appID).append(channel.getCpAppSecret()).append(userID);
            logger.debug("----同城游登陆认证签名体:{}",sb.toString());
            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();
            params.put("sign",sign);
            String url = channel.getChannelAuthUrl();
            UHttpAgent.getInstance().get(url, params, new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----同城游登陆认证返回数据:{}",content);
                    JSONObject object = JSONObject.fromObject(content);
                    int statusCode = object.getInt("StatusCode");
                    if (0 == statusCode) {
                        callback.onSuccess(new SDKVerifyResult(true,userID,userID,userID));
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
            logger.error("----同城游登陆认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
