package com.u8.server.sdk.i4;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * iOS i4渠道SDK处理类
 * Created by ant on 2016/2/24.
 */
public class I4SDK implements ISDKScript{
    @Override
    public void verify(final UChannel channel, final String extension, final ISDKVerifyListener callback) {

        try{

            JSONObject json = JSONObject.fromObject(extension);
            String token = json.getString("token");
            final String userName = json.getString("username");


            Map<String,String> params = new HashMap<String, String>();
            params.put("token", token);

            String url = channel.getChannelAuthUrl();

            UHttpAgent.getInstance().get(url, params, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {

                    try {

                        Log.e("The auth result is " + result);

                        JSONObject jsonResult = JSONObject.fromObject(result);

                        if(jsonResult != null && jsonResult.containsKey("status") && jsonResult.getInt("status") == 0){

                            SDKVerifyResult vResult = new SDKVerifyResult(true, jsonResult.getString("userid"), userName, "");

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
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess("");
        }
    }
}
