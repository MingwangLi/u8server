package com.u8.server.sdk.bs;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import net.sf.json.JSONObject;

import java.util.*;

/**
 * @Author: lizhong
 * @Date: Created in 9:58 2017/9/21
 * @Description: 蓝叠SDK
 */
public class BSSDK implements ISDKScript {
    private String bs_guid;
    private String bs_token;
    private String signature;
    private String api_identifier;
    private long timestamp;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        final JSONObject json = JSONObject.fromObject(extension);
        bs_guid = json.getString("guid");
        bs_token = json.getString("token");
        timestamp = System.currentTimeMillis();
        api_identifier = channel.getCpAppID();
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("bs_guid",bs_guid);
        params.put("bs_token",bs_token);
        params.put("api_identifier",api_identifier);
        signature =  generateSign(params,channel);
        params.put("timestamp",timestamp);
        params.put("signature",signature);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post_json(channel.getChannelAuthUrl(), JsonUtils.map2JsonStr(params), new UHttpFutureCallback() {
            @Override
            public void completed(String res) {
                JSONObject res_json = JSONObject.fromObject(res);
                boolean successs = res_json.getBoolean("success");
                int error_code = res_json.getInt("error_code");
                String message = res_json.getString("message");
                if(successs == true && error_code == 0){
                    JSONObject result = JSONObject.fromObject(res_json.getString("result"));
                    String bs_uname = result.getString("bs_uname");
                    String bs_guid = result.getString("bs_guid");
                    callback.onSuccess(new SDKVerifyResult(true,bs_guid,bs_uname,bs_uname,message));
                    return;
                }
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + res);
            }

            @Override
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
            }
        });

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        long cp_timestamp = System.currentTimeMillis();
        String cp_api_identifier = user.getChannel().getCpAppID();
        String goods_name = order.getProductName();
        int goods_price = order.getMoney()/100;
        int goods_nums = 1;
        long cp_order_no = order.getOrderID();
        int frost_type = 2;
        String callback_url = user.getChannel().getPayCallbackUrl();
        String game_name = user.getGame().getName();
        String role_name = order.getRoleName();
        String cp_client_ip = "127.0.0.1";
        String partner = user.getChannel().getCpID();
        int order_money = goods_price * goods_nums;
        String extra = "";
        Map<String,Object> params = new HashMap<String ,Object>();
        params.put("cp_timestamp",cp_timestamp);
        params.put("goods_name",goods_name);
        params.put("goods_price",goods_price);
        params.put("goods_nums",goods_nums);
        params.put("cp_order_no",cp_order_no);
        params.put("frost_type",frost_type);
        params.put("callback_url",callback_url);
        params.put("game_name",game_name);
        params.put("role_name",role_name);
        params.put("cp_client_ip",cp_client_ip);
        params.put("partner",partner);
        params.put("order_money",order_money);
        params.put("extra",extra);
        String cp_signature = generateGetOrderSign(params, user.getChannel());
        JSONObject backToClient = new JSONObject();
        backToClient.put("cp_client_ip",cp_client_ip);
        backToClient.put("callbackurl",callback_url);
        backToClient.put("cp_timestamp",cp_timestamp);
        backToClient.put("cp_signature",cp_signature);
        callback.onSuccess(backToClient.toString());
    }
    public String generateSign(Map<String, Object> params, UChannel channel) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = String.valueOf(params.get(k));
            postdatasb.append(v + "|");
        }
        postdatasb.append(channel.getCpAppKey() + "|").append(timestamp);
        return EncryptUtils.md5(postdatasb.toString());
    }
    public String generateGetOrderSign(Map<String, Object> params, UChannel channel) throws Exception{
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = String.valueOf(params.get(k));
            if(!k.equals("cp_timestamp")) {
                postdatasb.append(v + "|");
            }
        }
        postdatasb.append(channel.getCpAppKey() + "|").append(params.get("cp_timestamp"));
        //postdatasb.deleteCharAt(postdatasb.length() - 1);
        //对排序后的参数附加开发商签名密钥
        return EncryptUtils.md5(postdatasb.toString());
    }
}
