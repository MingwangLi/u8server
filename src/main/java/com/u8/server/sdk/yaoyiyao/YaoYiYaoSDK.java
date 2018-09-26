package com.u8.server.sdk.yaoyiyao;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class YaoYiYaoSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        try {
            logger.info("----爻乂爻登陆认证获取参数:{}",extension);
            JSONObject json = JSONObject.fromObject(extension);
            final String UserID = json.getString("UserID");
            String CenterToken = json.getString("CenterToken");
            String AppID = channel.getCpAppID();
            final String OpCode = "24639";
            final String ChannelID = "ANDROID";
            String url = channel.getChannelAuthUrl();
            Map<String,String > params = new HashMap<>();
            params.put("UserID",UserID);
            params.put("CenterToken",CenterToken);
            params.put("AppID",AppID);
            params.put("OpCode",OpCode);
            params.put("ChannelID",ChannelID);
            UHttpAgent.getInstance().post(url, params, new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("-----爻乂爻登陆认证返回数据:{}",content);
                    JSONObject object = JSONObject.fromObject(content);
                    int Stat = object.getInt("Stat");
                    if (0 == Stat) {
                        callback.onSuccess(new SDKVerifyResult(true,UserID,UserID,UserID));
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
            logger.error("----爻乂爻登陆认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
