package com.u8.server.sdk.k57;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: lizhong
 * @Des: 57k SDK
 * @Date: 2018/4/10 9:47
 * @Modified:
 */
public class K57SDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback){
        logger.info("----57k登录认证获取参数:{}",extension);
        JSONObject json = JSONObject.fromObject(extension);
        String app_id = channel.getCpAppID();
        final String mem_id = json.getString("memId");
        String user_token = json.getString("token");
        //LinkedHashMap 非线程安全 LinkedHashMap保证了元素迭代的顺序。该迭代顺序可以是插入顺序或者是访问顺序。
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("app_id", app_id);
        params.put("mem_id", mem_id);
        params.put("user_token", user_token);
        String sign = generateSign(params,channel);
        params.put("sign",sign);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        logger.info("----57k登录认证url:{}",channel.getChannelAuthUrl());
        logger.info("----57k登录认证参数:{}",params.toString());
        httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
            @Override
            public void completed(String result) {
                logger.info("----57k登录认证返回数据:{}",result);
                JSONObject json = JSONObject.fromObject(result);
                String status = json.getString("status");
                String msg = json.getString("msg");
                if("1".equals(status)){
                    callback.onSuccess(new SDKVerifyResult(true,mem_id,mem_id,mem_id,msg));
                    return;
                }
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
            }
            @Override
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
            }
        });
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
    //生成签名
    public static String generateSign(Map<String, String> params,UChannel channel) {
        List<String> keys = new ArrayList<String>(params.keySet());
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = String.valueOf(params.get(k));
            postdatasb.append(k + "=" + v + "&");
        }
        postdatasb.append("app_key=").append(channel.getCpAppKey());
//        postdatasb.deleteCharAt(postdatasb.length() - 1);
        //对排序后的参数附加开发商签名密钥
        return EncryptUtils.md5(postdatasb.toString());
    }
}
