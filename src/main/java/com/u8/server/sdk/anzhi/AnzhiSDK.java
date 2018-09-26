package com.u8.server.sdk.anzhi;

import com.u8.server.constants.SDKStateCode;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.Base64;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.TimeUtils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 安智
 * Created by ant on 2015/4/29.
 */
public class AnzhiSDK implements ISDKScript{

    private Logger logger = LoggerFactory.getLogger(AnzhiSDK.class);

    @Override
    public void verify(final UChannel channel, final String extension, final ISDKVerifyListener callback) {
        try{
            logger.debug("----安智客户端登录认证传入参数:{}",extension);
            JSONObject json = JSONObject.fromObject(extension);
            final String cptoken = json.getString("cptoken");
            final String deviceid = json.getString("deviceid");
            final String url = json.getString("requestUrl");
            final String appSecret = channel.getCpAppSecret();
            Map<String,String> params = new HashMap<String, String>();
            params.put("time", TimeUtils.format_yyyyMMddHHmmssSSS(new Date()));
            params.put("appkey", channel.getCpAppKey());
            params.put("cptoken", cptoken);
            params.put("deviceid", deviceid);
            StringBuilder sb = new StringBuilder();
            sb.append(channel.getCpAppKey()).append(cptoken).append(channel.getCpAppSecret());
            //String sign = Base64.encode(sb.toString(), "UTF-8");
            String sign = EncryptUtils.md5(sb.toString());
            params.put("sign", sign);
            //String url = channel.getChannelAuthUrl();
            UHttpAgent.getInstance().post(url, params, new UHttpFutureCallback() {

                @Override
                public void completed(String result) {
                    try {
                        logger.debug("----安智登录认证返回的数据:{}",result);
                        JSONObject json = JSONObject.fromObject(result);
                        Integer code = json.getInt("code");
                        if(SDKStateCode.LOGINSUCCESS == code){
                            String data = json.getString("data");
                            String msg = Base64.decode(data);
                            JSONObject object = JSONObject.fromObject(msg);
                            String uid = object.getString("uid");
                            String channelUserID = Des3Util.decrypt(uid,appSecret);
                            callback.onSuccess(new SDKVerifyResult(true, channelUserID, "", ""));
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("----安智登录认证异常:{}",e.getMessage());
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
            logger.error("----安智登录认证异常:{}",e.getMessage());
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is "+e.getMessage());
        }

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess("");
        }
    }
}
