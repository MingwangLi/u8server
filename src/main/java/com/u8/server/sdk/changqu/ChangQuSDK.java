package com.u8.server.sdk.changqu;

import com.u8.server.constants.SDKStateCode;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangQuSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        try {
            logger.info("----畅趣登陆认证获取参数:{}",extension);
            JSONObject object = JSONObject.fromObject(extension);
            String user_id = object.getString("user_id");
            String token = object.getString("token");
            JSONObject param = new JSONObject();
            param.put("user_id",user_id);
            param.put("token",token);
            String url = channel.getChannelAuthUrl();
            logger.debug("----畅趣登陆认证url:{}",url);
            UHttpAgent.getInstance().post_json(url, param.toString(), new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----畅趣登陆认证返回数据:{}",content);
                    JSONObject json = JSONObject.fromObject(content);
                    int status = json.getInt("status");
                    if (SDKStateCode.LOGINSUCCESS == status) {
                        String user_id = json.getString("user_id");
                        String user_acount = json.getString("user_account");
                        callback.onSuccess(new SDKVerifyResult(true,user_id,user_acount,user_acount));
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
            logger.error("----畅趣登陆认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
