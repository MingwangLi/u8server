package com.u8.server.sdk.heiyazi;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.http.util.TextUtils;

import java.util.*;

/**
 * 黑鸭子 SDK
 * @Author: lz
 * @Date: 2016/12/20 9:53.
 */
@SuppressWarnings("unchecked")
public class HeiYaZiSDK implements ISDKScript{
    public int gameid;//游戏ID
    public long tick;//时间戳
    public String token = null;//以后token
    public String sign = null;//消息签名
    public String cpid = null;
    public String apikey = null;
    public String username = null;


    //http://sdk.api.bdgames.com/checktoken黑鸭子Token校验请求地址
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        gameid = Integer.parseInt(channel.getCpAppID());
        tick = System.currentTimeMillis();
        token = json.getString("token");
        cpid = channel.getCpID();
        apikey = channel.getCpAppKey();
        username = json.getString("nickname");

        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("token", token);
        params.put("tick", tick);
        params.put("apikey", apikey);
        params.put("gameid", gameid);
        params.put("cpid", cpid);
        sign = generateSign(params);

        Map<String, Object> params_data = new HashMap<String, Object>();
        params_data.put("token", token);
        params_data.put("game", gameid);
        params_data.put("tick", tick);
        params_data.put("sign", sign);

        Map<String, Object> params_json = new HashMap<String, Object>();
        params_json.put("data", params_data);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post_json(channel.getChannelAuthUrl(), JsonUtils.map2JsonStr(params_json), new UHttpFutureCallback() {
            @Override
            public void completed(String result) {
                try {
                    if (!TextUtils.isEmpty(result)) {
                        JSONObject res_data = JSONObject.fromObject(result);
                        int code = res_data.optInt("code");
                        String  userId = res_data.optString("user");
                        if (code == 1) {
                            callback.onSuccess(new SDKVerifyResult(true, userId, username, username, result));
                            return;
                        }
                    }
                } catch (JSONException e) {
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

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            callback.onSuccess("");
        }
    }

    //生成签名
    public static String generateSign(Map<String, Object> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = String.valueOf(params.get(k));
            postdatasb.append(v + "&");
        }
        postdatasb.deleteCharAt(postdatasb.length() - 1);
        //对排序后的参数附加开发商签名密钥
        String signStr = postdatasb.toString().toLowerCase();
        String sign = EncryptUtils.md5(signStr);
        return sign;
    }

}
