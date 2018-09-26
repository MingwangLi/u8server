package com.u8.server.sdk.leishen;

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

public class LeiShenSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        logger.info("-----雷神SDK登陆认证获取参数:{}",extension);
        try {
            JSONObject json = JSONObject.fromObject(extension);
            final Integer uin = json.getInt("userId");
            String token = json.getString("sessionId");
            Long ts = System.currentTimeMillis();
            Integer app = Integer.parseInt(channel.getCpAppID());
            String key = channel.getCpAppKey();
            StringBuilder sb = new StringBuilder();
            sb.append("app=").append(app).
                    append("&token=").append(token).
                    append("&ts=").append(ts).
                    append("&uin=").append(uin).
                    append(key);
            logger.debug("----雷神登录认证签名体:{}",sb.toString());
            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();
            Map<String,String> param = new HashMap<>();
            param.put("app",app+"");
            param.put("token",token);
            param.put("ts",ts+"");
            param.put("uin",uin+"");
            param.put("sign",sign);
            String url = channel.getChannelAuthUrl();
            UHttpAgent.getInstance().post(url, param, new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----雷神登录认证返回数据:{}",content);
                    if ("0".equals(content)) {
                        callback.onSuccess(new SDKVerifyResult(true,uin+"",uin+"",uin+""));
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
            logger.error("----雷神登录认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
