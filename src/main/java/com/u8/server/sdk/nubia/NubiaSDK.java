package com.u8.server.sdk.nubia;

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
 * @Author: lizhong
 * @Des:努比亚SDK
 * @Date: 2018/2/1 15:33
 * @Modified:
 */
public class NubiaSDK implements ISDKScript{
    private String uid;
    private String data_timestamp;
    private String game_id;
    private String session_id;
    private String sign;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        JSONObject json = JSONObject.fromObject(extension);
        uid = json.getString("uid");
        data_timestamp = System.currentTimeMillis() + "";
        game_id = json.getString("game_id");
        session_id = json.getString("session_id");
        Map<String, String> params = new HashMap<String, String>();
        params.put("uid",uid);
        params.put("data_timestamp",data_timestamp);
        params.put("game_id",game_id);
        params.put("session_id",session_id);
        sign = generateSign(params,channel);
        params.put("sign",sign);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
            @Override
            public void completed(String res) {
                JSONObject res_json = JSONObject.fromObject(res);
                int code = res_json.getInt("code");
                if (0 == code){
                    callback.onSuccess(new SDKVerifyResult(true,uid,game_id,game_id));
                    return;
                }
            }

            @Override
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + err);
            }
        });
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {

    }
    //生成sign
    public static String generateSign(Map<String,String> params,UChannel channel){
		/*首先以key值自然排序,生成key1=val1&key2=val2......&keyN=valN格式的字符串*/
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder postdatasb=new StringBuilder();
        for(int i = 0;i < keys.size();i ++){
            String k = keys.get(i);
            String v  = params.get(k);
            postdatasb.append(k+"="+ v+"&");
        }
        postdatasb.deleteCharAt(postdatasb.length()-1);
        postdatasb.append(":").append(channel.getCpAppID()).append(":").append(channel.getCpAppSecret());
        //对排序后的参数附加开发商签名密钥
        return EncryptUtils.md5(postdatasb.toString());
    }
}
