package com.u8.server.web.pay;

import com.u8.server.cache.UApplicationContext;
import com.u8.server.constants.PayState;
import com.u8.server.constants.StateCode;
import com.u8.server.data.UGame;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.UHttpAgent;
import com.u8.server.service.UOrderManager;
import com.u8.server.service.UUserManager;
import com.u8.server.task.OrderTaskManager;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * U8Server向游戏服发送回调通知
 */
public class SendAgent {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String SIGN_MD5 = "md5";

    /**
     * U8Server支付成功，通知游戏服务器
     *
     * @param orderManager
     * @param order
     * @return
     */
    public static boolean sendCallbackToServer(UOrderManager orderManager, UOrder order) {

        UGame game = order.getGame();
        if (game == null) {
            return false;
        }

        UUserManager userManager = (UUserManager) UApplicationContext.getBean("userManager");
        UUser user = userManager.getUser(order.getUserID());
        if (user != null && (user.getFirstCharge() == null || user.getFirstCharge() == 0)) {
            //记录玩家首冲信息
            user.setFirstCharge(1);
            user.setFirstChargeTime(new Date());
            userManager.saveUser(user);
        }


        String callbackUrl = order.getNotifyUrl();
        if (StringUtils.isEmpty(callbackUrl)) {
            callbackUrl = game.getPayCallback();
        }

        if (StringUtils.isEmpty(callbackUrl)) {
            Log.d("the order paycallback url is not configed. no in order. no in game.");
            return false;
        }

        try {
            JSONObject data = new JSONObject();
            data.put("productID", order.getProductID());
            data.put("orderID", order.getOrderID() + "");
            data.put("userID", order.getUserID());
            data.put("channelID", order.getChannelID());
            data.put("gameID", order.getAppID());
            data.put("serverID", order.getServerID());
            data.put("money", order.getMoney());
            data.put("currency", order.getCurrency());
            data.put("extension", order.getExtension() + "");
            //如果需要将签名方式改为MD5，把下面两行SIGN_RSA改为SIGN_MD5
            String sign = generateSign(order);
            data.put("signType", SIGN_MD5);
            data.put("sign", sign);
            JSONObject response = new JSONObject();
            response.put("state", StateCode.CODE_SUCCESS);
            response.put("data", data);
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "text/html");

            String toStr = response.toString();
            Log.d("U8Server------------>>>>>>>>>GameServer--------start-----------");
            Log.d("--------------------callbackUrl = " + callbackUrl);
            Log.i("cp notice params:"+toStr);
            String serverRes = UHttpAgent.getInstance().post(callbackUrl, headers, new ByteArrayEntity(toStr.getBytes(Charset.forName("UTF-8"))));
            Log.d("U8Server------------>>>>>>>>>GameServer--------end-------------game server return data :" + serverRes);

            if (serverRes.equals("SUCCESS")) {
                if (1 != order.getAppID()) {
                    order.setState(PayState.STATE_COMPLETE);
                    orderManager.saveOrder(order);
                    return true;
                } else {
                    //测试游戏模拟支付成功 为了以示区分 修改状态为2
                    order.setState(PayState.STATE_SUC);
                    orderManager.saveOrder(order);
                    return true;
                }

            }
        } catch (Exception e) {
            Log.e(e.getMessage(), e);
            e.printStackTrace();
        }
        //失败了，加入重发队列，尝试6次
        OrderTaskManager.getInstance().addOrder(order);
        return false;
    }

    /***
     * 重发到游戏服
     * @param orderManager
     * @param order
     * @return
     */
    public static boolean resendCallbackToServer(UOrderManager orderManager, UOrder order) {

        UGame game = order.getGame();
        if (game == null) {
            return false;
        }

        String callbackUrl = order.getNotifyUrl();
        if (StringUtils.isEmpty(callbackUrl)) {
            callbackUrl = game.getPayCallback();
        }

        if (StringUtils.isEmpty(callbackUrl)) {
            Log.d("the order paycallback url is not configed. no in order. no in game.");
            return false;
        }

        try {
            JSONObject data = new JSONObject();
            data.put("productID", order.getProductID());
            data.put("orderID", order.getOrderID() + "");
            data.put("userID", order.getUserID());
            data.put("channelID", order.getChannelID());
            data.put("gameID", order.getAppID());
            data.put("serverID", order.getServerID());
            data.put("money", order.getMoney());
            data.put("currency", order.getCurrency());
            data.put("extension", order.getExtension());
            String sign = generateSign(order);
            data.put("signType", SIGN_MD5);
            data.put("sign", sign);
            JSONObject response = new JSONObject();
            response.put("state", StateCode.CODE_SUCCESS);
            response.put("data", data);
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "text/html");
            Log.d("U8Server-----------------------send message to Game_server-------to--------start---------");
            Log.d("U8Server-----------------------callbackUrl = " + callbackUrl);
            String serverRes = UHttpAgent.getInstance().post(callbackUrl, headers, new ByteArrayEntity(response.toString().getBytes(Charset.forName("UTF-8"))));
            Log.d("U8Server-----------------------send message to Game_server-------back ------end-----------" + serverRes);
            if (serverRes.equals("SUCCESS")) {
                order.setState(PayState.STATE_COMPLETE);
                orderManager.saveOrder(order);
                return true;
            }
        } catch (Exception e) {
            Log.e(e.getMessage(), e);
            e.printStackTrace();
        }


        return false;
    }

    /**
     * 生成签名
     **/
    private static String generateSign(UOrder order) {

        StringBuilder sb = new StringBuilder();
        sb.append("channelID=").append(order.getChannelID())
                .append("&currency=").append(order.getCurrency())
                .append("&extension=").append(order.getExtension())
                .append("&gameID=").append(order.getAppID())
                .append("&money=").append(order.getMoney())
                .append("&orderID=").append(order.getOrderID())
                .append("&productID=").append(order.getProductID())
                .append("&serverID=").append(order.getServerID())
                .append("&userID=").append(order.getUserID())
                .append(order.getGame().getAppSecret());

        String signStr = sb.toString();
        Log.i("============================ u8server -->> gameserver Sign:" + signStr);
        return EncryptUtils.md5(signStr).toLowerCase();
    }

}
