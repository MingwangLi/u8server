package com.u8.server.sdk.game374;

import com.u8.server.constants.SDKStateCode;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Game374SDK implements ISDKScript {

    //工具类Log虽然可以节省这行代码 但是看不到日志所在类和位置信息
    private Logger logger = LoggerFactory.getLogger(Game374SDK.class);

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        JSONObject object = JSONObject.fromObject(extension);
        String user_id = object.getString("uid");
        String token = object.getString("token");
        object.clear();
        object.put("user_id",user_id);
        object.put("token",token);
        String param = object.toString();
        UHttpAgent.getInstance().post_json(channel.getMaster().getAuthUrl(), param, new UHttpFutureCallback() {
            @Override
            public void completed(String content) {
                try {
                    logger.debug("----374游戏登陆认证sdk返回的数据:"+content);
                    JSONObject jsonObject = JSONObject.fromObject(content);
                    int status = jsonObject.getInt("status");
                    if (SDKStateCode.OK == status) {
                        String channelUserID = jsonObject.getString("user_id");
                        String user_account = jsonObject.getString("user_account");
                        callback.onSuccess(new SDKVerifyResult(true,channelUserID,user_account,user_account));
                        return ;
                    }
                    callback.onFailed("----374游戏登陆认证sdk返回status错误");
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed("异常信息:"+e.getMessage());
                }
            }

            @Override
            public void failed(String err) {
                callback.onFailed(err);
            }
        });

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
