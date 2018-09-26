package com.u8.server.sdk.naiwan;

import com.u8.server.constants.SDKStateCode;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.SignUtils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NaiWanSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(NaiWanSDK.class);

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        logger.debug("----耐玩登陆认证extension:{}",extension);
        JSONObject object = JSONObject.fromObject(extension);
        String sessionid = object.getString("sessionid");
        if (sessionid.contains("%2b") || sessionid.contains("%2f")
                || sessionid.contains("%2F") || sessionid.contains("%2B")) {
            sessionid = URLDecoder.decode(sessionid);
        }
        sessionid.replaceAll(" ","+ ");
        String appid = channel.getCpAppID();
        String ac = "check";
        String sdkversion = "5.4";
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        String time = calendar.getTimeInMillis()+"";
        Map<String,String> signParams = new HashMap<>();
        signParams.put("ac",ac);
        signParams.put("appid",appid);
        signParams.put("sdkversion",sdkversion);
        signParams.put("sessionid",sessionid);
        signParams.put("time",time);
        final String loginkey = channel.getCpAppKey();
        String sign = SignUtils.createSign(signParams,loginkey,"耐玩登陆认证");
        Map<String,String> params = new HashMap<>();
        params.put("ac",ac);
        params.put("appid",appid);
        params.put("sdkversion",sdkversion);
        params.put("sessionid",sessionid);
        params.put("time",time);
        params.put("sign",sign);
        String url = channel.getMaster().getAuthUrl();
        logger.debug("----耐玩登陆认证url:{}",url);
        UHttpAgent.getInstance().post(url, params, new UHttpFutureCallback() {
            @Override
            public void completed(String content) {
                logger.debug("----耐玩登陆认证返回的数据:{}",content);
                try {
                    JSONObject jsonObject = JSONObject.fromObject(content);
                    String code = jsonObject.getString("code");
                    jsonObject = jsonObject.getJSONObject("userInfo");
                    String channelUserID = jsonObject.getString("uid");
                    String channelUserName = jsonObject.getString("username");
                    if (SDKStateCode.LOGINSUCCESS == Integer.parseInt(code)) {
                        callback.onSuccess(new SDKVerifyResult(true,channelUserID,channelUserName,channelUserName));
                        return;
                    }
                    callback.onFailed(content);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.debug("----耐玩登陆认证返回数据解析异常,异常信息:{}",e.getMessage());
                }
            }

            @Override
            public void failed(String err) {
                callback.onFailed(err);
            }
        });
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
