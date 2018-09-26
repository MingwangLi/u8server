package com.u8.server.sdk.yikeyou;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YiKeYouSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        logger.info("----亿安客登录认证获取参数:{}",extension);
        try {
            JSONObject object = JSONObject.fromObject(extension);
            String user_id = object.getString("uid");
            String token = object.getString("token");
            object.clear();
            object.put("user_id",user_id);
            object.put("token",token);
            String url = channel.getChannelAuthUrl();
            logger.debug("----亿安客登录认证url:{}",url);
            logger.debug("----亿安客登录认证参数:{}",object.toString());
            UHttpAgent.getInstance().post_json(url, object.toString(), new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----安客登录认证返回的数据:{}",content);
                    JSONObject jsonObject = JSONObject.fromObject(content);
                    //{status:1,user_id:1,user_account:’wan001’}”
                    int status = jsonObject.getInt("status");
                    String uid = jsonObject.getString("user_id");
                    String userName = jsonObject.getString("user_account");
                    if (1 == status) {
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
            logger.error("----安客登录认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
