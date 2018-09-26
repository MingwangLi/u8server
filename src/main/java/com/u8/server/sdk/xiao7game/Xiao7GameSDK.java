package com.u8.server.sdk.xiao7game;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lizhong
 * Date: 2018/03/28.
 * 小7游戏
 */
public class Xiao7GameSDK implements ISDKScript {
    private String appKey;
    private String tokenkey;
    private String sign;
    private String guid;
    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        tokenkey = json.getString("tokenkey");
        Map<String, String> data = new HashMap<String, String>();
        data.put("tokenkey", tokenkey);
        appKey = channel.getCpAppKey();
        sign = EncryptUtils.md5(channel.getCpAppKey() + tokenkey).toLowerCase();
        data.put("sign", sign);
        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl(), data, new UHttpFutureCallback() {
            @Override
            public void completed(String result) {
                JSONObject res_json = JSONObject.fromObject(result);
                int errorno = res_json.getInt("errorno");
                if (errorno == 0) {
                    JSONObject data = JSONObject.fromObject(res_json.getString("data"));
                    guid = data.getString("guid");
                    callback.onSuccess(new SDKVerifyResult(true,guid,data.getString("username"),data.getString("username")));
                    return;
                }
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
            }

            @Override
            public void failed(String e) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + e);
            }
        });

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        String extends_info_data = "";
        String game_area = order.getServerName();
        String game_guid = user.getChannelUserID();
        String game_level = "1";
        String game_orderid = order.getOrderID() + "";
        String game_price = order.getMoney()/100 + "";
        String game_role_id = order.getRoleID();
        String game_role_name = order.getRoleName();
        String notify_id = "-1";
        String subject = order.getProductDesc();
        StringBuilder signStr = new StringBuilder();
        signStr.append("extends_info_data=").append(extends_info_data)
                .append("&game_area=").append(game_area)
                .append("&game_guid=").append(game_guid)
                .append("&game_level=").append(game_level)
                .append("&game_orderid=").append(game_orderid)
                .append("&game_price=").append(game_price)
                .append("&game_role_id=").append(game_role_id)
                .append("&game_role_name=").append(game_role_name)
                .append("&notify_id=").append(notify_id)
                .append("&subject=").append(subject)
                .append(user.getChannel().getCpPayKey());
        String game_sign = EncryptUtils.md5(signStr.toString());
        JSONObject data = new JSONObject();
        data.put("sign",game_sign);
        data.put("guid",game_guid);
        data.put("callbackurl",user.getChannel().getPayCallbackUrl());
        if (callback != null) {
            callback.onSuccess(data.toString());
        }
    }
}
