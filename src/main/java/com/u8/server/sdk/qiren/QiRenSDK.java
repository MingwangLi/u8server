package com.u8.server.sdk.qiren;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.StringUtils;
import net.sf.json.JSONObject;

import java.util.*;

/**
 * 233游戏
 * Created by lizhong on 2017/11/17 16:17.
 */
public class QiRenSDK implements ISDKScript{
    private String uin;
    private String token;
    private String game_id;
    private String sign;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        uin = json.getString("uin");
        token = json.getString("token");
        game_id = channel.getCpAppID();
        Map<String, String> params = new HashMap<String, String>();
        params.put("uin",uin);
        params.put("token",token);
        params.put("game_id",game_id);
        sign = generateSign(params,channel.getCpAppKey());
        params.put("sign",sign);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
            @Override
            public void completed(String res) {
                JSONObject res_json = JSONObject.fromObject(res);
                int state = res_json.getInt("state");
                String msg = res_json.getString("msg");
                if(0 == state){
                    callback.onSuccess(new SDKVerifyResult(true, uin, uin, uin));
                    return;
                }
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed." + msg);
            }

            @Override
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
            }
        });

    }
    //生成sign
    public static String generateSign(Map<String, String> params, String signKey){
		/*首先以key值自然排序,生成key1=val1&key2=val2......&keyN=valN格式的字符串*/
        List<String> keys=new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder postdatasb=new StringBuilder();
        for(int i=0;i<keys.size();i++){
            String k=keys.get(i);
            String v=params.get(k);
            postdatasb.append(k+"="+v+"&");
        }
        postdatasb.deleteCharAt(postdatasb.length()-1);
        //对排序后的参数附加开发商签名密钥
        postdatasb.append(signKey);
        Log.d("the sign data is "+postdatasb.toString());
        return EncryptUtils.md5(postdatasb.toString());
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
