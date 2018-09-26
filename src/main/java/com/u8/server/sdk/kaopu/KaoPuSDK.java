package com.u8.server.sdk.kaopu;

import com.u8.server.constants.SDKStateCode;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 靠谱助手SDK
 * Created by xiaohei on 16/10/16.
 */
public class KaoPuSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        logger.info("----靠谱SDK登录认证获取参数:{}",extension);
        try {
            /*final JSONObject json = JSONObject.fromObject(extension);
            final String openid = json.getString("openId");
            final String token = json.getString("token");
            final String imei = json.getString("imei");
            final String tag = json.getString("tag");
            final String channelkey = json.getString("channelKey");
            String tagid = json.getString("tagid");
            String verifyURL = json.getString("verifyURL");
            verifyURL = verifyURL.substring(0,verifyURL.lastIndexOf("?"));
            String sign = json.getString("sign");
            String appid = json.getString("appId");
            final String devicetype = json.getString("deviceType");
            String r = json.getString("r");
            String timespans = json.getString("timeSpans");
            Map<String,String> param = new HashMap<>();
            param.put("appid",appid);
            param.put("channelkey",channelkey);
            param.put("devicetype",devicetype);
            param.put("imei",imei);
            param.put("openid",openid);
            param.put("r",r);
            param.put("tag",tag);
            param.put("tagid",tagid);
            param.put("token",token);
            param.put("timespans",timespans);
            param.put("sign",sign);*/
            final JSONObject json = JSONObject.fromObject(extension);
            final String verifyURL = json.getString("verifyURL");
            final String token = json.getString("token");
            final String imei = json.getString("imei");
            final String tag = json.getString("tag");
            final String openid = json.getString("openId");
            logger.info("----靠谱登录认证verifyURL:{}",verifyURL);
            if (StringUtils.isNotEmpty(verifyURL) && verifyURL.contains(".kpzs.com")) {
                UHttpAgent.getInstance().get(verifyURL, null, new UHttpFutureCallback() {
                    @Override
                    public void completed(String content) {
                        logger.info("----靠谱SDK登录认证返回数据:{}",content);
                        JSONObject jsonObject = JSONObject.fromObject(content);
                        Map<String, String> map = new HashMap<>();
                        map.put("0","18257284-7F5D-348D-AB09-299E5B7DD997");
                        map.put("1","655A957D-157D-7C21-E3A7-9CAAFA835318");
                        map.put("2","F467CA93-D550-346D-6BCB-173995F7C83A");
                        map.put("3","BD32817A-99F9-2E26-5B33-15208F7B360A");
                        int code = jsonObject.getInt("code");
                        String backSign = jsonObject.getString("sign");
                        String brackR = jsonObject.getString("r");
                        String signBody = code+tag+imei.toLowerCase()+token.toLowerCase()+map.get(brackR);
                        logger.debug("----靠谱登录认证返回sign校验签名体:{}",signBody);
                        String checkSign = EncryptUtils.md5(signBody);
                        if (SDKStateCode.LOGINSUCCESS == code && backSign.equals(checkSign)) {
                            callback.onSuccess(new SDKVerifyResult(true,openid,openid,openid));
                            return;
                        }
                        callback.onFailed(content);
                    }

                    @Override
                    public void failed(String err) {
                        callback.onFailed(err);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----靠谱SDK登录认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }

}
