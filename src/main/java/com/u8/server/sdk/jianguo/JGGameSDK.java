package com.u8.server.sdk.jianguo;

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

/**
 * 坚果SDK
 */
public class JGGameSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(JGGameSDK.class);

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        logger.debug("----坚果登陆认证参数extension:{}",extension);
        JSONObject object = JSONObject.fromObject(extension);
        final String mem_id = object.getString("memId");
        String user_token = object.getString("token");
        String app_id = channel.getCpAppID();
        String app_key = channel.getCpAppKey();
        StringBuilder sb = new StringBuilder();
        sb.append("app_id=").append(app_id).
                append("&mem_id=").append(mem_id).
                append("&user_token=").append(user_token).
                append("&app_key=").append(app_key);
        logger.debug("----坚果登陆认证签名体:{}",sb.toString());
        String sign = EncryptUtils.md5(sb.toString()).toLowerCase();
        Map<String,String> params = new HashMap<>();
        params.put("app_id",app_id);
        params.put("mem_id",mem_id);
        params.put("user_token",user_token);
        params.put("sign",sign);
        String url = channel.getMaster().getAuthUrl();
        logger.debug("----坚果登陆认证url:{}",url);
        UHttpAgent.getInstance().post(url, params, new UHttpFutureCallback() {
            @Override
            public void completed(String content) {
                logger.debug("----坚果登录认证返回数据:{}",content);
                try {
                    JSONObject jsonObject = JSONObject.fromObject(content);
                    String status = jsonObject.getString("status");
                    String msg = jsonObject.getString("msg");
                    if (SDKStateCode.LOGINSUCCESS == Integer.parseInt(status)) {
                        callback.onSuccess(new SDKVerifyResult(true,mem_id,mem_id,mem_id));
                        return;
                    }
                    callback.onFailed(msg);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    logger.error("----坚果登陆认证返回数据解析异常,异常信息:{}",e.getMessage());
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
