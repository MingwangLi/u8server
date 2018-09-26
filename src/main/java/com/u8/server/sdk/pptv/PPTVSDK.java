package com.u8.server.sdk.pptv;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.SignUtils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * PPTV
 * Created by xiaohei on 15/12/23.
 */
public class PPTVSDK implements ISDKScript{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        try{
            logger.info("----PPTV登录认证获取参数:{}",extension);
            JSONObject json = JSONObject.fromObject(extension);
            String extra = json.getString("extra");
            String token = json.getString("token");
            final String userName = json.getString("userName");
            final String user_id = json.getString("uid");
            String platform = json.getString("platformId");
            String sub_platform = json.getString("subPlatformId");
            final String key = channel.getCpAppKey();
            logger.debug("----PPTV登录认证key:{}",key);
            Map<String,String> params = new HashMap<String, String>();
            params.put("ext", extra);
            params.put("token", token);
            params.put("ext", userName);
            params.put("gid",channel.getCpAppID());
            params.put("time",System.currentTimeMillis()+"");
            params.put("user_id",user_id);
            params.put("username",userName);
            params.put("platform",platform);
            params.put("sub_platform",sub_platform);
            String sign = SignUtils.createSignWithURLEncode(params,key,"PPTV登录认证");
            params.put("sign",sign);
            String url = channel.getChannelAuthUrl();
            logger.debug("----PPTV登录认证url:{}",url);
            logger.debug("----PPTV登录认证param:{}",params.toString());
            UHttpAgent.getInstance().post(url, params, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {
                    try {
                        logger.info("----PPTV登录验证返回数据:{}",result);
                        JSONObject json = JSONObject.fromObject(result);
                        int code = json.getInt("code");
                        if (code == 1) {
                            callback.onSuccess(new SDKVerifyResult(true,user_id,userName,userName));
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onFailed(e.getMessage());
                    }

                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
                }

                @Override
                public void failed(String e) {
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + e);
                }


            });


        }catch (Exception e){
            e.printStackTrace();
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is "+e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess(order.getChannel().getPayCallbackUrl());
        }
    }
}
