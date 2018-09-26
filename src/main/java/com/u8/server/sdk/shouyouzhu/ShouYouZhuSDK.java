package com.u8.server.sdk.shouyouzhu;


import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ShouYouZhuSDK implements ISDKScript{

    //单例 多线程下访问 不能有全局变量
    // private String appid;
    // private String userId;
    // private String token;
    // private String qtime;
    // private String sign;
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        logger.info("----手游猪登录认证获取参数:{}",extension);
        try {
            String appid = channel.getCpAppID();
            JSONObject json = JSONObject.fromObject(extension);
            //userId = json.getString("userId");
            String token = json.getString("token");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String qtime = simpleDateFormat.format(new Date());
            String signStr = appid + qtime + channel.getCpAppKey();
            String sign = EncryptUtils.md5(signStr);
            Map<String,String> params = new HashMap<>();
            params.put("appid",appid);
            params.put("token",token);
            params.put("qtime",qtime);
            params.put("sign",sign);
            UHttpAgent httpClient = UHttpAgent.getInstance();
            String url = channel.getChannelAuthUrl();
            logger.debug("----手游猪登录认证url:{}",url);
            logger.debug("----手游猪登录认证参数:{}",params.toString());
            httpClient.post(url, params, new UHttpFutureCallback(){

                @Override
                public void completed(String result){
                    logger.info("----手游猪登陆认证返回数据:{}",result);
                    JSONObject json = JSONObject.fromObject(result);
                    String code = json.getString("code");
                    String content = json.getString("content");
                    if("1".equals(code)){
                        callback.onSuccess(new SDKVerifyResult(true,content,content,content));
                        return;
                    }
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);

                }
                @Override
                public void failed(String err) {
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + err);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----手游猪登录认证失败:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
