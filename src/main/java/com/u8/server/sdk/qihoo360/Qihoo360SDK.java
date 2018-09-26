package com.u8.server.sdk.qihoo360;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.JsonUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: lizhong
 * Date: 2018/1/4.
 * Desc: 360SDK登录认证
 */
public class Qihoo360SDK implements ISDKScript {

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {

        try{
            Map<String, String> data = new HashMap<String, String>();
            data.put("access_token", extension);
            data.put("fields", "id,name,avatar,sex,area,nick");
            String url = channel.getChannelAuthUrl();
            UHttpAgent.getInstance().get(url, data, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {
                    try{
                        QihooUserInfo userInfo = (QihooUserInfo) JsonUtils.decodeJson(result, QihooUserInfo.class);

                        if(userInfo != null && !StringUtils.isEmpty(userInfo.getId())){
                            SDKVerifyResult vResult = new SDKVerifyResult(true, userInfo.getId(), userInfo.getName(), userInfo.getNick());
                            callback.onSuccess(vResult);
                            return;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        QihooErrorInfo error = (QihooErrorInfo)JsonUtils.decodeJson(result, QihooErrorInfo.class);
                        if(error != null){
                            callback.onFailed(channel.getMaster().getSdkName()+" verify failed. msg:"+error.getError());
                            return;
                        }

                    }
                    callback.onFailed(channel.getMaster().getSdkName() +" verify failed. the get result is "+result);
                }
                @Override
                public void failed(String e) {
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + e);
                }
            });

        }catch(Exception e){
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is "+e.getMessage());
            e.printStackTrace();
        }

    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
           /* String app_key = order.getChannel().getCpAppKey();
            String product_id = order.getProductID();
            String product_name = order.getProductName();
            String amount = order.getMoney()+"";
            String app_uid = user.getChannelUserID();
            String app_uname = user.getChannelUserName();
            String user_id = user.getId()+"";
            String sign_type = "md5";
            StringBuilder sb = new StringBuilder();
            String sign = "";*/
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
