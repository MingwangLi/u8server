package com.u8.server.sdk.tt;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import net.sf.json.JSONObject;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static com.u8.server.web.pay.sdk.Xiao7GamePayCallbackAction.channel;

/**
 *
 * TT 语音SDK V2.2.3
 * Created by lizhong on 2017/09/18.
 */
public class TTSDK implements ISDKScript{
    private String uid;
    private String gameId;
    private String sign;
    private String sid;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        try{
            JSONObject json = JSONObject.fromObject(extension);
            uid = json.getString("uid");
            gameId = channel.getCpAppID();
            sid = json.getString("sid");
            Map<String,Object> params = new LinkedHashMap<String, Object>();
            params.put("gameId",gameId);
            params.put("uid", uid);
            String jsonData = JsonUtils.encodeJson(params);
            sign = SignUtils.sign(jsonData, channel.getCpAppSecret());

            Log.d("jsonData:%s", jsonData);
            Log.d("appSecret:%s", channel.getCpAppSecret());
            Log.d("sign:%s", sign);

            Map<String,Object> headers = new HashMap<String, Object>();
//            headers.put("Content-Type", "application/json");
            headers.put("sid", sid);
            headers.put("sign",sign);
            UHttpAgent httpClient = UHttpAgent.getInstance();
            String resStr = HttpUtil.doPost("http://usdk.52tt.com/server/rest/user/loginstatus.view", com.alibaba.fastjson.JSONObject.toJSONString(params), headers);
            JSONObject res = JSONObject.fromObject(resStr);
            if(res.containsKey("head")){
                JSONObject head = res.getJSONObject("head");
                if(head.containsKey("result") && head.getInt("result") == 0){
                    callback.onSuccess(new SDKVerifyResult(true, uid, uid, uid));
                    return;
                }
                callback.onFailed("Fail");
            }
            callback.onFailed("Fail");
        }catch (Exception e){
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is "+e.getMessage());
            Log.e(e.getMessage());
        }

    }
    //生成签名
    public static String generateSign(Map<String, String> params,UChannel channel) {
        List<String> keys = new ArrayList<String>(params.keySet());
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = String.valueOf(params.get(k));
            postdatasb.append(k + "=" + v + "&");
        }
        postdatasb.append("app_key=").append(channel.getCpAppKey());
//        postdatasb.deleteCharAt(postdatasb.length() - 1);
        //对排序后的参数附加开发商签名密钥
        String signStr = postdatasb.toString();
        String sign = EncryptUtils.md5(signStr);
        return sign;
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
