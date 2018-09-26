package com.u8.server.sdk.lehihi;

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
 * 乐嗨嗨SDK
 * Created by lizhong on 2017/12/25.
 */
public class LeHiHiSDK implements ISDKScript{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        try{
            String pid = channel.getCpAppID();
            JSONObject json = JSONObject.fromObject(extension);
            final String username = json.getString("username");
            String token = json.getString("token");
            Map<String, String> params = new HashMap<String, String>();
            params.put("username", username);
            params.put("token", token);
            params.put("pid", pid);
            StringBuilder sb = new StringBuilder();
            sb.append("pid=").append(pid)
                    .append("&token=").append(token)
                    .append("&username=").append(username)
                    .append(channel.getCpAppSecret());
            String sign = EncryptUtils.md5(sb.toString());
            params.put("sign", sign);
            UHttpAgent httpClient = UHttpAgent.getInstance();
            httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {
                    logger.info("----乐嗨嗨(B)登录认证返回数据:{}",result);
                    JSONObject json = JSONObject.fromObject(result);
                    int state = json.getInt("state");
                    String msg = json.getString("msg");
                    JSONObject jsonData = JSONObject.fromObject(json.getString("data"));
                    if(state == 1){
                        callback.onSuccess(new SDKVerifyResult(true, username, username, username,msg));
                        return;
                    }
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
                }
                @Override
                public void failed(String e) {
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + e);
                }
            });
        }catch (Exception e){
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is "+e.getMessage());
            logger.debug("----乐嗨嗨(B)登录认证异常:{}",e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
