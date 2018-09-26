package com.u8.server.sdk.pengyouwan;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.JsonUtils;
import net.sf.json.JSONObject;
import org.apache.http.entity.ByteArrayEntity;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 朋友玩SDK
 * Created by ant on 2016/10/17.
 */
public class PYWSDK implements ISDKScript {

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {


        try{

            JSONObject json = JSONObject.fromObject(extension);
            final String uid = json.getString("userId");
            final String token = json.getString("token");
            final String ts = System.nanoTime()+"";

            UHttpAgent httpClient = UHttpAgent.getInstance();

            Map<String,Object> params = new HashMap<String, Object>();
            params.put("tid",ts);
            params.put("token", token);
            params.put("uid", uid);

            String jsonData = JsonUtils.encodeJson(params);

            Map<String,String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/x-www-form-urlencoded");

            httpClient.post(channel.getChannelAuthUrl(), headers, new ByteArrayEntity(jsonData.getBytes(Charset.forName("UTF-8"))), new UHttpFutureCallback() {
                @Override
                public void completed(String result) {

                    JSONObject json = JSONObject.fromObject(result);

                    if(json.containsKey("ack") && json.getInt("ack") == 200){

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
