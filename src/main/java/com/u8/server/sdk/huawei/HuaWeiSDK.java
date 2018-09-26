package com.u8.server.sdk.huawei;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.sdk.huawei.huawei.RSAUtil;
import com.u8.server.utils.Base64;
import com.u8.server.utils.RSAUtils;
import net.sf.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by lizhong on 2018/1/31.
 */
public class HuaWeiSDK implements ISDKScript{

    //华为登录验签统一公钥
    private static final String LOGIN_RSA_PUBLIC = "";
    private String appId;
    private String cpId;
    private String method = "external.hms.gs.checkPlayerSign";
    private String playerId;
    private String playerLevel;
    private String playerSSign;
    private String ts;
    private String cpSign;
    private String nickname;
    @Override
    public void verify(final UChannel channel,String extension, final ISDKVerifyListener callback) throws Exception {
        appId = channel.getCpAppID();
        cpId = channel.getCpID();
        JSONObject json = JSONObject.fromObject(extension);
        ts = json.getString("ts");
        playerId = json.getString("playerId");
        playerLevel = json.getString("playerLevel");
        playerSSign = json.getString("gameAuthSign");
        nickname = json.getString("displayName");
        Map<String ,String> params = new HashMap<String, String>();
        params.put("method",method);
        params.put("appId",appId);
        params.put("cpId",cpId);
        params.put("ts",ts);
        params.put("playerId",playerId);
        params.put("playerLevel",playerLevel);
        params.put("playerSSign",playerSSign);
        cpSign = generateSign(params , channel);
        params.put("cpSign",cpSign);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl(), params, new UHttpFutureCallback() {
            @Override
            public void completed(String res) {
                JSONObject res_json = JSONObject.fromObject(res);
                int rtnCode = res_json.getInt("rtnCode");
                String rtnSign = res_json.getString("rtnSign");
                if (0 == rtnCode && rtnSign.equals(cpSign)){
                    callback.onSuccess(new SDKVerifyResult(true,playerId,nickname,nickname));
                    return;
                }

            }

            @Override
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
            }
        });

            ///2016-10-27 华为这版本流程这里，分为两步，这里不便服务器端做验证，注释下面这些
//            StringBuilder sb = new StringBuilder();
//            sb.append(channel.getCpAppID()).append(ts).append(playerId);

//            boolean ok = RSAUtil.verify(sb.toString().getBytes("UTF-8"), LOGIN_RSA_PUBLIC, accessToken);
//            if(ok){
//
//                SDKVerifyResult vResult = new SDKVerifyResult(true, playerId, "", nickname);
//
//                callback.onSuccess(vResult);
//            }else{
//                callback.onFailed(channel.getMaster().getSdkName() + " verify failed.");
//            }

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if(callback != null){
            String sign = getSign(user.getChannel(), order);
            callback.onSuccess(sign);
        }
    }

    private String getSign(UChannel channel, UOrder data){

        int money = (int)(data.getMoney() / 100);
        Map<String, String> params = new HashMap<String, String>();
        // 必填字段，这里支付ID配置在
        params.put("userID", data.getChannel().getCpPayID());
        // 必填字段，不能为null或者""，请填写从联盟获取的应用ID
        params.put("applicationID", data.getChannel().getCpAppID());
        // 必填字段，不能为null或者""，单位是元，精确到小数点后两位，如1.00
        params.put("amount", money+".00");
        // 必填字段，不能为null或者""，道具名称
        params.put("productName", data.getProductName());
        // 必填字段，不能为null或者""，道具描述
        params.put("productDesc", data.getProductDesc());
        // 必填字段，不能为null或者""，最长30字节，不能重复，否则订单会失败
        params.put("requestId", data.getOrderID()+"");

        String noSign = getSignData(params);

        Log.d("The noSign data is "+noSign);

        String sign = RSAUtils.sign(noSign, channel.getCpPayPriKey(), "UTF-8");
        Log.d("The sign is ： " + sign);

        return sign;
    }

    public static String getSignData(Map<String, String> params)
    {
        StringBuffer content = new StringBuffer();
        // 按照key做排序
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++)
        {
            String key = (String) keys.get(i);
            if ("sign".equals(key))  //  如果是sign字段，字不作为签名校验参数
            {
                continue;
            }
            String value = (String) params.get(key);
            if (value != null)
            {
                content.append((i == 0 ? "" : "&") + key + "=" + value);
            }
            else   //  如果value为null，则写成：key=&key1=value1
            {
                content.append((i == 0 ? "" : "&") + key + "=");
            }
        }
        return content.toString();
    }

    //生成sign
    public static String generateSign(Map<String,String> params,UChannel channel) throws Exception {
		/*首先以key值自然排序,生成key1=val1&key2=val2......&keyN=valN格式的字符串*/
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
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
        return URLEncoder.encode(Base64.encode(RSAUtil.sha256WithRsa(postdatasb.toString().getBytes(),LOGIN_RSA_PUBLIC).getBytes()),"UTF-8");
    }
}
