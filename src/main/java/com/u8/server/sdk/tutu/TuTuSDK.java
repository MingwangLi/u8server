package com.u8.server.sdk.tutu;

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

public class TuTuSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(TuTuSDK.class);

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        logger.debug("----兔兔SDK登陆认证extension:{}",extension);
        JSONObject object = JSONObject.fromObject(extension);
        String open_key = object.getString("openKey");
        String open_id = object.getString("openId");
        final String userName = object.getString("userName");
        final String channelUserID = object.getString("uid");
        String appKey = channel.getCpAppKey();
        String mapp_key = EncryptUtils.md5(appKey);
        String verfy = EncryptUtils.md5("www.feng.com"+open_id+open_key+mapp_key);
        String url = channel.getMaster().getAuthUrl();
        logger.debug("----兔兔登陆认证url:{}",url);
        Map<String,String> params = new HashMap<>();
        params.put("open_key",open_key);
        params.put("open_id",open_id);
        params.put("mapp_key",mapp_key);
        params.put("verfy",verfy);
        logger.debug("----兔兔登陆认证参数:{}",params.toString());
        UHttpAgent.getInstance().post(url, params, new UHttpFutureCallback() {
            @Override
            public void completed(String content) {
                logger.debug("----兔兔登陆认证返回的数据:{}",content);
                try {
                    JSONObject jsonObject = JSONObject.fromObject(content);
                    String code = jsonObject.getString("success");
                    String msg = jsonObject.getString("info");
                    if (SDKStateCode.LOGINSUCCESS == Integer.parseInt(code)) {
                        callback.onSuccess(new SDKVerifyResult(true,channelUserID,userName,userName));
                        return;
                    }
                    callback.onFailed("----兔兔登陆认证失败,提示信息:"+msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("----兔兔登陆认证异常,异常信息:{}",e.getMessage());
                }
            }

            @Override
            public void failed(String err) {
                callback.onFailed("----兔兔登陆认证失败,返回的数据:"+err);
            }
        });
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
