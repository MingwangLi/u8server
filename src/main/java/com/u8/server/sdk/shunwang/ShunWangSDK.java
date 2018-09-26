package com.u8.server.sdk.shunwang;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShunWangSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(ShunWangSDK.class);

    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback) throws Exception {
        logger.debug("----顺网SDK登陆认证参数:{}",extension);
        JSONObject jsonObject = JSONObject.fromObject(extension);
        String guid = jsonObject.getString("guid");
        callback.onSuccess(new SDKVerifyResult(true,guid,guid,guid));
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl()+"?oid="+order.getOrderID());
    }
}
