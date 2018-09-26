package com.u8.server.sdk.wukong;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class WuKongSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        try {
            logger.info("----悟空登陆认证获取参数:{}",extension);
            JSONObject object = JSONObject.fromObject(extension);
            final String userId = object.getString("userId");
            final String nonceStr = UUID.randomUUID().toString();
            final String ext = "5ksy";
            String token = EncryptUtils.md5(ext+userId+nonceStr);
            JSONObject json = new JSONObject();
            json.put("userId",userId);
            json.put("nonceStr",nonceStr);
            json.put("token",token);
            String url = channel.getChannelAuthUrl();
            logger.debug("-----悟空登陆认证url:{}",url);
            logger.debug("----悟空登陆认证参数:{}",json.toString());
            UHttpAgent.getInstance().post_json(url, json.toString(), new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    final String successResult = "success";
                    logger.info("----悟空登陆认证返回数据:{}",content);
                    JSONObject jsonObject = JSONObject.fromObject(content);
                    String result = jsonObject.getString("message");
                    if (successResult.equals(result)) {
                        callback.onSuccess(new SDKVerifyResult(true,userId,userId,userId));
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
            logger.error("----悟空登陆认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
