package com.u8.server.sdk.jiujiuwan;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * @Author: lizhong
 * @Des: 99SDK
 * @Date: 2018/1/10 14:27
 * @Modified:
 */
public class JiuJiuWanSDK implements ISDKScript{
    private String userid;
    private String username;
    private String appkey;
    private String logintime;
    private String sign;
    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback) throws Exception {
        JSONObject json = JSONObject.fromObject(extension);
        userid = json.getString("userid");
        username = json.getString("username");
        appkey = channel.getCpAppKey();
        logintime = json.getString("logintime");
        Map<String,String> params = new LinkedHashMap<String, String>();
        params.put("username",userid);
        params.put("appkey",appkey);
        params.put("logintime",logintime);
        sign = json.getString("sign");
        if(sign.equals(generateSign(params))){
            callback.onSuccess(new SDKVerifyResult(true,userid,username,username));
            return;
        }
        callback.onFailed(channel.getMaster().getSdkName() + " verify failed. ");
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }

    //生成sign
    public static String generateSign(Map<String,String> params){
		/*首先以key值自然排序,生成key1=val1&key2=val2......&keyN=valN格式的字符串*/
        List<String> keys = new ArrayList<String>(params.keySet());
        StringBuilder postdatasb=new StringBuilder();
        for(int i = 0;i < keys.size();i ++){
            String k = keys.get(i);
            String v = null;
            try {
                v = URLEncoder.encode(params.get(k),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            postdatasb.append(k+"="+ v+"&");
        }
        postdatasb.deleteCharAt(postdatasb.length()-1);
        //对排序后的参数附加开发商签名密钥
        return EncryptUtils.md5(postdatasb.toString());
    }
}
