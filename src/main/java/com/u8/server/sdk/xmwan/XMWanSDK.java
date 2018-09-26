package com.u8.server.sdk.xmwan;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.StringUtils;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 熊猫玩SDK
 * Created by xiaohei on 16/11/26.
 */
public class XMWanSDK implements ISDKScript{
    private String access_token;
    @Override
    public void verify(final UChannel channel, final String extension, final ISDKVerifyListener callback) {
        try{
            access_token = extension;
            Map<String,String> params = new HashMap<String, String>();
            params.put("access_token", access_token);

            String url = channel.getChannelAuthUrl();

            UHttpAgent.getInstance().get(url, params, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {
                    try {
                        Log.e("The auth result is " + result);
                        JSONObject jsonResult = JSONObject.fromObject(result);
                        if (jsonResult != null && jsonResult.containsKey("xmw_open_id") ) {
                            String uid = jsonResult.getString("xmw_open_id");
                            String nickname = jsonResult.getString("nickname");
                            //熊猫玩 下单的时候，需要用到token，这里将token，存储在uuser的channelUserNick中
                            SDKVerifyResult vResult = new SDKVerifyResult(true, uid, nickname,"");
                            callback.onSuccess(vResult);
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

        }catch(Exception e){
            e.printStackTrace();
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is "+e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, final ISDKOrderListener callback) {

        try{

            Map<String,String> params = new HashMap<String,String>();
            params.put("amount", "" + order.getMoney()/100);
            params.put("app_order_id", order.getOrderID()+"");
            params.put("app_user_id", user.getId()+"");
            params.put("notify_url", order.getChannel().getPayCallbackUrl());
            params.put("app_subject", order.getProductName());
            params.put("timestamp", System.currentTimeMillis() / 1000 + "");

            String signStr = StringUtils.generateUrlSortedParamString(params, "&", true);
            signStr += "&client_secret=" + order.getChannel().getCpAppSecret();

            Log.d("sign str : %s", signStr);

            String sign = EncryptUtils.md5(signStr);

            params.put("sign", sign);
            params.put("access_token", access_token);
            params.put("client_id", order.getChannel().getCpAppID());
            params.put("client_secret", order.getChannel().getCpAppSecret());

            UHttpAgent.getInstance().post(order.getChannel().getMaster().getOrderUrl(), params, new UHttpFutureCallback() {
                @Override
                public void completed(String content) {

                    try{
                        Log.d("xmw get order content:");
                        Log.d(content);

                        JSONObject json = JSONObject.fromObject(content);
                        if(json.containsKey("serial")){
                            callback.onSuccess(json.getString("serial"));
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }

                @Override
                public void failed(String err) {
                    callback.onFailed(err );
                }
            });


        }catch(Exception e){
            callback.onFailed(e.getMessage() );
            e.printStackTrace();
        }
    }
}
