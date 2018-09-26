package com.u8.server.sdk.aochuang;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.http.util.TextUtils;

import java.util.*;

/**\
 * 奥创SDK
 * @Author: lz
 * @Date: 2016/12/19 16:02.
 */
public class AoChuangSDK implements ISDKScript{
    public String ac = "check";//值固定为check
    public String appid = null;//SDK平台的游戏ID
    public String sdkversion = "3.2";//值固定为3.2
    public String sessionid = null;//游戏端返回的sessionid
    public String time = null;//时间戳
    public String sign = null;//验证字符串
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        appid = channel.getCpAppID();
        sessionid = json.optString("sessionid");
        time = json.optString("time");
        Map<String,String> params = new LinkedHashMap<String, String>();
        params.put("ac",ac);
        params.put("appid",appid);
        params.put("sdkversion",sdkversion);
        params.put("sessionid",sessionid);
        params.put("time",time);
        sign = generateSign(params,channel.getCpAppKey());
        params.put("sign",sign);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.get(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
            @Override
            public void completed(String result) {
                try {
                    if(!TextUtils.isEmpty(result)){
                        JSONObject json_result = JSONObject.fromObject(result);
                        int code = json_result.optInt("code");
                        String userInfo = json_result.optString("userInfo");
                        if(code==1){
                            callback.onSuccess(new SDKVerifyResult(true, sessionid, "", "",userInfo));
                            return;
                        }
                    }
                }catch (JSONException e){
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
    //登录 生成sign
    public static String generateSign(Map<String, String> params, String signKey) {
//md5(“ac=check&appid=1&sdkversion=3.2&sessionid=urlencode(xxx)&time=12233+key”)
        List<String> keys=new ArrayList<String>(params.keySet());
        StringBuilder postdatasb=new StringBuilder();
        for(int i=0;i<keys.size();i++){
            String k=keys.get(i);
            String v=params.get(k);
            postdatasb.append(k+"="+v+"&");
        }
        postdatasb.deleteCharAt(postdatasb.length()-1);
        //对排序后的参数附加开发商签名密钥
        postdatasb.append(signKey);
        String sign= EncryptUtils.md5(postdatasb.toString());
        Log.d("the sign data is "+postdatasb.toString());
        return sign;
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess("");
        }
    }
}
