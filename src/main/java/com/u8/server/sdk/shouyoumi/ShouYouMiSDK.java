package com.u8.server.sdk.shouyoumi;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 手游迷SDK
 */
public class ShouYouMiSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback) throws Exception {
        logger.info("----手游迷登陆认证获取参数:{}",extension);


    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {

    }
}
