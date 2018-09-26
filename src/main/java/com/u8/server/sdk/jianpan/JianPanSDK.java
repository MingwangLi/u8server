package com.u8.server.sdk.jianpan;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import net.sf.json.JSONObject;
import org.apache.http.util.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lvxinmin on 2016/11/10.
 */
public class JianPanSDK implements ISDKScript {


    public String version = null;
    public String sid = null;
    public String channel = null;
    public String userId = null;
    public String gameId = null;

    @Override
    public void verify(final UChannel uChannel, String extension, final ISDKVerifyListener callback) {
            JSONObject json = JSONObject.fromObject(extension);
            version = json.optString("version");
            sid = json.optString("sid");
            channel = json.optString("channel");
            userId = json.optString("uid");
            gameId = uChannel.getCpAppID();

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("version", version);
            params.put("sid", sid);
            params.put("channel", channel);
            params.put("userId", userId);
            params.put("gameId", gameId);
            String preSign = gameId + "|" + channel + "|" + userId + "|" + sid + "|" + version + "|" + uChannel.getCpAppKey();
            String sign = EncryptUtils.md5(preSign.toString());
            params.put("sign", sign);
            UHttpAgent httpClient = UHttpAgent.getInstance();
            httpClient.post_json(uChannel.getChannelAuthUrl(), JsonUtils.map2JsonStr(params), new UHttpFutureCallback() {
                @Override
                public void completed(String result) {
                    JSONObject json = JSONObject.fromObject(result);
                    String status = json.getString("status");
                    String msg = json.getString("msg");
                    if(status.equals("YHYZ_000")){
                        userId = json.getString("userId");
                        callback.onSuccess(new SDKVerifyResult(true, userId, userId, userId, userId));
                        return;
                    }
                }
                @Override
                public void failed(String err) {
                        callback.onFailed(uChannel.getMaster().getSdkName() + " verify failed. the post result is " + err);
                }
            });
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(userId);
        }
    }
}
