package com.u8.server.sdk.xueshanbao;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class XueShanBaoSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        logger.info("----雪山豹登录认证获取参数:{}",extension);
        try {
            JSONObject json = JSONObject.fromObject(extension);
            String ac = "checklogin";
            String appid = channel.getCpAppID();
            String sdkversion = "1.4";
            String sessionid = json.getString("sessionId");
            String time = System.currentTimeMillis()+"";
            time = time.substring(0,time.length()-3);
            String key = channel.getCpAppKey();
            logger.debug("----雪山豹登录认证获取key:{}",key);
            StringBuilder sb = new StringBuilder();
            sb.append("ac=").append(ac).
                    append("&appid=").append(appid).
                    append("&sdkversion=").append(sdkversion).
                    append("&sessionid=").append(URLDecoder.decode(sessionid)).
                    append("&time=").append(time).
                    append(key);
            logger.debug("----雪山豹登录认证签名体:{}",sb.toString());
            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("ac",ac);
            paramMap.put("appid",appid);
            paramMap.put("sdkversion",sdkversion);
            paramMap.put("sessionid",URLDecoder.decode(sessionid));
            paramMap.put("time",time);
            paramMap.put("sign",sign);
            String url = channel.getChannelAuthUrl();
            logger.debug("----雪山豹登录认证url:{}",url);
            UHttpAgent.getInstance().post(url, paramMap, new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----雪山豹登录认证返回数据:{}",content);
                    JSONObject object = JSONObject.fromObject(content);
                    String status = object.getString("status");
                    if ("success".equals(status)) {
                        object = object.getJSONObject("userInfo");
                        String uid = object.getString("uid");
                        String userName = object.getString("username");
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
            logger.error("----雪山豹登录认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
