package com.u8.server.sdk.gg;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import com.u8.server.utils.RSAUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.CharEncoding;
import java.util.HashMap;
import java.util.Map;


/**
 * GG游戏SDK created by lizhong
 * DATE: 2017/10/20.
 * */
public class GGSDK implements ISDKScript{
    private String gameId;
    private String data;
    private String sign;
    private String gameToken;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        gameId = channel.getCpID();
        data = extension;
        JSONObject json = JSONObject.fromObject(data);
        gameToken = json.getString("gameToken");
        String signStr = "gameToken="+gameToken+channel.getCpAppKey();
        sign = EncryptUtils.md5(signStr);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("gameId",gameId);
        params.put("data",data);
        params.put("sign",sign);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post_json(channel.getChannelAuthUrl(), JsonUtils.map2JsonStr(params), new UHttpFutureCallback() {
            @Override
            public void completed(String result) {
                JSONObject json = JSONObject.fromObject(result);
                String rc = json.getString("rc");
                String msg = json.getString("msg");
                JSONObject data = JSONObject.fromObject(json.getString("data"));
                String accountId = data.getString("accountId");
                if("0".equals(rc)){
                    callback.onSuccess(new SDKVerifyResult(true,accountId,accountId,accountId,msg));
                    return;
                }
                callback.onFailed("rc:"+rc + " msg:"+msg);
            }

            @Override
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
            }
        });
    }

    @Override
    public void onGetOrderID(final UUser user, UOrder order, final ISDKOrderListener callback){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appid", user.getChannel().getCpAppID());
        jsonObject.put("waresid", 1);
        jsonObject.put("cporderid", order.getOrderID()+"");
        jsonObject.put("currency", "RMB");
        jsonObject.put("appuserid", order.getServerID());
        String waresname = order.getProductName();
        if(!waresname.isEmpty()){
            jsonObject.put("waresname",waresname);
        }
        jsonObject.put("price", Float.valueOf(order.getMoney()/100));
        jsonObject.put("notifyurl", user.getChannel().getPayCallbackUrl());
        String content = jsonObject.toString();// 组装成 json格式数据
        String sign = RSAUtils.sign(content, user.getChannel().getCpPayPriKey(),CharEncoding.UTF_8);
        String reqData = "transdata=" + content + "&sign=" + sign+ "&signtype=RSA";// 组装请求参数
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(user.getChannel().getChannelOrderUrl(), reqData, new UHttpFutureCallback() {
            @Override
            public void completed(String respData) {
                Map<String, String> reslutMap = SignUtils.getParmters(respData);
                String signtype = reslutMap.get("signtype"); // "RSA";
                if(signtype != null) {
                    if (RSAUtils.verify(reslutMap.get("transdata"), reslutMap.get("sign"), user.getChannel().getCpPayKey(), CharEncoding.UTF_8)) {
                        JSONObject json = JSONObject.fromObject(reslutMap.get("transdata"));
                        callback.onSuccess(json.getString("transid"));
                        return;
                    }
                    callback.onFailed(respData);
                }
            }
            @Override
            public void failed(String err) {
                callback.onFailed(err);
                return;
            }
        });
    }
}
