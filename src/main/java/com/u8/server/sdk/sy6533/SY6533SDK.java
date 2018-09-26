package com.u8.server.sdk.sy6533;

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

public class SY6533SDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        try {
            logger.info("----6533登陆认证获取参数:{}",extension);
            JSONObject jsonObject = JSONObject.fromObject(extension);
            String app_id = channel.getCpAppID();
            String app_key = channel.getCpAppKey();
            final String mem_id = jsonObject.getString("mem_id");
            String user_token = jsonObject.getString("user_token");
            StringBuilder sb = new StringBuilder();
            sb.append("app_id=").append(app_id).append("&mem_id=").append(mem_id).append("&user_token=").append(user_token).append("&app_key=").append(app_key);
            logger.debug("----6533登陆认证签名体:{}",sb.toString());
            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();
            Map<String,String > params = new HashMap<>();
            params.put("app_id",app_id);
            params.put("mem_id",mem_id);
            params.put("user_token",user_token);
            params.put("sign",sign);
            String url = channel.getChannelAuthUrl();
            logger.debug("----6533登陆认证url:{}",url);
            logger.debug("----6533登陆认证参数:{}",params.toString());
            UHttpAgent.getInstance().post(url, params, new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----6533登陆认证返回数据:{}",content);
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
            logger.error("----6533登陆认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
