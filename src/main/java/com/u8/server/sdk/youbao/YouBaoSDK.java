package com.u8.server.sdk.youbao;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.http.util.TextUtils;

import java.util.*;
/**
 * 游宝SDK
 * @Author: lz
 * @Date: 2016/12/19 15:15.
 */
public class YouBaoSDK implements ISDKScript{

    private String app_id =null;
    private String mem_id = null;
    private String user_token = null;
    private String sign = null;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        mem_id = json.getString("mem_id");
        user_token = json.getString("user_token");
        app_id = channel.getCpAppID();
        String signStr = "app_id="+app_id+"&mem_id="+mem_id+"&user_token="+user_token+"&app_key="+channel.getCpAppKey();
        sign = generateSign(signStr);
        Map<String, String> params = new HashMap<String, String>();
        params.put("mem_id",mem_id);
        params.put("user_token",user_token);
        params.put("app_id",app_id);
        params.put("sign",sign);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.get(channel.getAuthUrl(),params , new UHttpFutureCallback() {
            @Override
            public void completed(String result) {
                try {
                    if(!TextUtils.isEmpty(result)){
                        JSONObject res_data = JSONObject.fromObject(result);
                        String status = res_data.getString("status");
                        String msg = res_data.getString("msg");
                        if("1".equals(status)){
                            callback.onSuccess(new SDKVerifyResult(true, mem_id, mem_id,"",msg));
                            return;
                        }
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
            }
            @Override
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
            }
        });
    }
    private String generateSign(String sign){
        return Md5Util.md5(sign);
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess("");
        }
    }
}
