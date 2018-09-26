package com.u8.server.sdk.shouyoucun;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.sdk.tt.SignUtils;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import com.u8.server.utils.StringUtils;
import net.sf.json.JSONObject;
import org.apache.http.entity.ByteArrayEntity;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 手游村 SDK
 * Created by ant on 2016/11/21.
 */
public class SYCSDK implements ISDKScript {


    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {


        try{

            JSONObject json = JSONObject.fromObject(extension);
            final String uid = json.getString("userId");
            final String token = json.getString("token");

            UHttpAgent httpClient = UHttpAgent.getInstance();

            Map<String,String> params = new HashMap<String, String>();
            params.put("app_id", channel.getCpAppID());
            params.put("mem_id", uid);
            params.put("user_token", token);

            String signStr = StringUtils.generateUrlSortedParamString(params, "&", true);
            signStr += "&app_key=" + channel.getCpAppKey();

            Log.d("the sign str:%s", signStr);

            String sign = EncryptUtils.md5(signStr).toLowerCase();
            params.put("sign", sign);

            String jsonData = JsonUtils.encodeJson(params);


            Map<String,String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            httpClient.post(channel.getChannelAuthUrl(), headers, new ByteArrayEntity(jsonData.getBytes(Charset.forName("UTF-8"))), new UHttpFutureCallback() {
                @Override
                public void completed(String result) {

                    JSONObject json = JSONObject.fromObject(result);

                    if(json.containsKey("status") && json.getInt("status") == 1){

                        callback.onSuccess(new SDKVerifyResult(true, uid, "", ""));
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
            Log.e(e.getMessage());
        }

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }

}
