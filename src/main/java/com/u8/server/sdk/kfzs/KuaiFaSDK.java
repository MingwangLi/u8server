package com.u8.server.sdk.kfzs;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by lizhong on 2017/11/20.
 * 快发助手SDK登录认证
 */
public class KuaiFaSDK implements ISDKScript {
    private String token;
    private String openid;
    private String userId;
    private String userName;
    private long timestamp;
    private String gamekey;
    private String _sign;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        token = json.getString("AuthToken");
        openid = json.getString("openId");
        userId = json.getString("userId");
        userName = json.getString("userName");
        timestamp = System.currentTimeMillis();
        gamekey = channel.getCpAppKey();
        Map<String, String> params = new HashMap<String, String>();
        params.put("token",token);
        params.put("openid",openid);
        params.put("timestamp",timestamp + "");
        params.put("gamekey",gamekey);
        _sign = generateSign(params,channel);
        params.put("_sign",_sign);

        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl(),params,new UHttpFutureCallback(){
            public void completed(String res) {
                JSONObject json = JSONObject.fromObject(res);
                String result = json.getString("result");
                String result_desc = json.getString("result_desc");
                if("0".equals(result)){
                    callback.onSuccess(new SDKVerifyResult(true, userId , userName, userName));
                    return;
                }
                callback.onFailed("verify failed" + json.toString());
            }
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + err);
            }
        });
    }
    //生成sign
    public static String generateSign(Map<String, String> params, UChannel channel){
		/*首先以key值自然排序,生成key1=val1&key2=val2......&keyN=valN格式的字符串*/
        List<String> keys=new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder postdatasb=new StringBuilder();
        for(int i=0;i<keys.size();i++){
            String k=keys.get(i);
            String v= null;
            try {
                v = URLEncoder.encode(params.get(k),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            postdatasb.append(k+"="+ v+"&");
        }
        postdatasb.deleteCharAt(postdatasb.length()-1);
        //对排序后的参数附加开发商签名密钥
        String signStr = EncryptUtils.md5(EncryptUtils.md5(postdatasb.toString()) + channel.getCpAppSecret());

        return signStr;
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }


}
