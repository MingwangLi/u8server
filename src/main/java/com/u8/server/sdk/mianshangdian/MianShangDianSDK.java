package com.u8.server.sdk.mianshangdian;

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

/**
 * 免商店（蜗牛）
 * Created by xiaohei on 15/12/22.
 */
public class MianShangDianSDK implements ISDKScript{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        try{
            JSONObject json = JSONObject.fromObject(extension);
            logger.info("----免商店登录认证获取参数:{}",extension);
            final String uin = json.getString("uin");
            String sessionId = json.getString("sessionId");
            final String act = "4";
            String appId = channel.getCpAppID();
            String key = channel.getCpAppKey();
            StringBuilder sb = new StringBuilder();
            sb.append(appId).append(act).append(uin).append(sessionId).append(key);
            logger.debug("----免商店登录认证签名体:{}",sb.toString());
            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();
            Map<String,String> params = new HashMap<String, String>();
            params.put("AppId",appId);
            params.put("Act",act);
            params.put("Uin",uin);
            params.put("SessionId",sessionId);
            params.put("Sign",sign);
            String url = channel.getChannelAuthUrl();
            logger.info("----免商店登录认证url:{}",url);
            UHttpAgent.getInstance().get(url, params, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {
                    try {
                        logger.debug("----免商店登录认证返回数据:{}",result);
                        JSONObject object = JSONObject.fromObject(result);
                        String code = object.getString("ErrorCode");
                        String account = object.getString("Account");
                        if ("1".equals(code)) {
                            callback.onSuccess(new SDKVerifyResult(true,account,account,account));
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the result is " + result);
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
