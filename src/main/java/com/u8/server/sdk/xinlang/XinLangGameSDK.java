package com.u8.server.sdk.xinlang;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class XinLangGameSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        try {
            logger.info("----新浪游戏登陆认证获取参数:{}",extension);
            JSONObject jsonObject = JSONObject.fromObject(extension);
            String appkey = channel.getCpAppKey();
            final String suid = jsonObject.getString("suid");
            String deviceid = jsonObject.getString("deviceid");
            String token = jsonObject.getString("token");
            Map<String,String> params = new HashMap<>();
            params.put("suid", suid);
            params.put("appkey", appkey);
            params.put("deviceid", deviceid);
            params.put("token", token);
            String secret = channel.getCpAppSecret();
            String signature = createUserSignature(params,secret);
            params.put("signature",signature);
            String url = channel.getChannelAuthUrl();
            logger.debug("-----新浪游戏登陆认证url:{},params:{}",url,params.toString());
            UHttpAgent.getInstance().post(url, params, new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----新浪游戏登陆认证返回数据:{}",content);
                    JSONObject json = JSONObject.fromObject(content);
                    if (StringUtils.isNotEmpty(json.getString("suid")) && StringUtils.isNotEmpty(json.getString("token"))) {
                        callback.onSuccess(new SDKVerifyResult(true,suid,suid,suid));
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
            logger.error("----新浪游戏登陆认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }

    }



    /**
     * 计算用户接口签名
     * @param params
     * @param signatureKey
     * @return
     */
    private String createUserSignature(Map<String,String> params, String signatureKey) {
        String signature = null;
        if(params != null){
            StringBuilder sValue = new StringBuilder();
            Object[] keys = params.keySet().toArray();
            Arrays.sort(keys);
            String temp = null;
            for(Object key : keys){
                sValue.append(key).append("=");
                temp = params.get(key);
                if(temp == null){
                    sValue.append("").append("&");
                }else{
                    sValue.append(temp).append("&");
                }
            }
            if(sValue.length()>0){
                sValue.deleteCharAt(sValue.length() - 1);
                sValue.append("|").append(signatureKey);
            }
            signature = MD5.md5sum(sValue.toString());
        }
        return signature;
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
