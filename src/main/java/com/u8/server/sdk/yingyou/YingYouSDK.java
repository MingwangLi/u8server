package com.u8.server.sdk.yingyou;

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

public class YingYouSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        try {
            logger.info("----鹰游登陆认证获取参数:{}",extension);
            JSONObject object = JSONObject.fromObject(extension);
            final String mem_id = object.getString("mem_id");
            String app_id = channel.getCpAppID();
            String user_token = object.getString("user_token");
            String app_key = channel.getCpAppKey();
            StringBuilder sb = new StringBuilder();
            sb.append("app_id=").append(app_id).
                    append("&mem_id=").append(mem_id).
                    append("&user_token=").append(user_token).
                    append("&app_key=").append(app_key);
            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();
            logger.debug("----鹰游登录认证签名体:{}",sb.toString());
            Map<String,String> param = new HashMap<>();
            param.put("app_id",app_id);
            param.put("mem_id",mem_id);
            param.put("user_token",user_token);
            param.put("sign",sign);
            String url = channel.getChannelAuthUrl();
            logger.debug("----鹰游登陆认证url:",url);
            UHttpAgent.getInstance().post(url, param, new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----鹰游登陆认证返回数据:",content);
                    JSONObject json = JSONObject.fromObject(content);
                    String status = json.getString("status");
                    if ("1".equals(status)) {
                        callback.onSuccess(new SDKVerifyResult(true,mem_id,mem_id,mem_id));
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
            logger.error("----鹰游登陆认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
    }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
