package com.u8.server.sdk.sdk8868;

import com.u8.server.constants.SDKStateCode;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JG8868SDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        try {
            logger.info("----8868登录认证获取参数:{}",extension);
            JSONObject json = JSONObject.fromObject(extension);
            String sid = json.getString("sid");
            String url = channel.getChannelAuthUrl();
            logger.debug("----8868登录认证url:{}",url);
            JSONObject param = new JSONObject();
            param.put("id",System.currentTimeMillis());
            param.put("service","user.suidInfo");
            JSONObject data = new JSONObject();
            data.put("sid",sid);
            param.put("data",data);
            JSONObject game = new JSONObject();
            game.put("cpId",channel.getCpID());
            game.put("gameId",channel.getAppID());
            game.put("channelId",channel.getId());
            game.put("serverId",0);
            param.put("game",game);
            String sign = EncryptUtils.md5(channel.getCpID()+"sid="+sid+channel.getCpAppKey()).toLowerCase();
            param.put("sign",sign);
            logger.debug("----8868登录认证param:{}",param.toString());
            UHttpAgent.getInstance().post_json(url, param.toString(), new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----8868登陆认证返回数据:{}",content);
                    JSONObject jsonObject = JSONObject.fromObject(content);
                    JSONObject state = jsonObject.getJSONObject("state");
                    if (SDKStateCode.LOGINSUCCESS == state.getInt("code")) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        String uid = data.getString("uid");
                        //suid 账户唯一标识
                        //uid 用户唯一标识               此次逻辑有误 但是为了不影响之前游戏运营 暂时不做修改 在后续接入游戏修改(根据appID)
                        callback.onSuccess(new SDKVerifyResult(true,uid,data.get("suid")+"",data.getString("nickName")));
                        return;
                    }
                    callback.onFailed(content);
                }

                @Override
                public void failed(String err) {
                    callback.onFailed(err);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----8868登录认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(order.getChannel().getPayCallbackUrl());
    }
}
