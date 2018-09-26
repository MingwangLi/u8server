package com.u8.server.sdk.xuanyou;

import com.u8.server.constants.SDKStateCode;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XuanYouSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("all")
    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        logger.info("----轩游登录认证获取参数:{}",extension);
        try {
            String url = channel.getChannelAuthUrl();
            logger.debug("----轩游登陆认证url:{}",url);
            UHttpAgent.getInstance().post_json(url, extension, new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----轩游登陆认证返回数据:{}",content);
                    JSONObject jsonObject = JSONObject.fromObject(content);
                    int status = jsonObject.getInt("status");
                    if (SDKStateCode.OK == status) {
                        String user_id = jsonObject.getString("user_id");
                        String user_account = jsonObject.getString("user_account");
                        callback.onSuccess(new SDKVerifyResult(true,user_id,user_account,user_account));
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
            logger.error("----轩游登陆认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
