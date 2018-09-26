package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.moge.PayCallbackInfo;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lizhong on 2017/10/20.
 */
@Controller
@Namespace("/pay/moge")
public class MoGePayCallbackAction extends UActionSupport {

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback() {
        try {
            Map<String, String> params = new HashMap<String, String>();
            Map requestParams = request.getParameterMap();
            for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                // 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
                // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
                params.put(name, valueStr);
                Log.d("----------------" + name + ":" + valueStr);
            }
            if (params == null) {
                Log.d("------MoGe pay callBack message is null");
                this.renderState(false);
                return;
            }
            long orderID = Long.parseLong(params.get("attach"));
            UOrder order = orderManager.getOrder(orderID);

            if (order == null || order.getChannel() == null) {
                Log.d("【The order is null or the channel is null】");
                this.renderState(false);
                return;
            }

            if (order.getState() > PayState.STATE_PAYING) {
                Log.d("【The state of the order is complete】 |  The state is " + order.getState());
                this.renderState(true);
                return;
            }

            if (verifyPay(order.getChannel(), params)) {
                int moneyInt = Integer.valueOf(params.get("amount"));
                order.setRealMoney( moneyInt* 100);
                order.setSdkOrderTime(params.get("paytime"));
                order.setCompleteTime(new Date());
                order.setChannelOrderID(params.get("orderid"));
                order.setState(PayState.STATE_SUC);
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(this.orderManager, order);
            } else {
                order.setChannelOrderID(params.get("orderid"));
                order.setState(PayState.STATE_FAILED);
                orderManager.saveOrder(order);
            }

            renderState(true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(e.getMessage());
            try {
                this.renderState(false);
            } catch (Exception e2) {
                Log.e(e2.getMessage());
            }
        }
    }

    private void renderState(boolean suc) throws IOException {
        String res = "SUCCESS";
        if (!suc) {
            res = "FAILURE";
        }
        PrintWriter out = this.response.getWriter();
        out.write(res);
        out.flush();
    }
    /***
     * 验证支付
     * @param channel
     * @param params
     * @return
     */
    public boolean verifyPay(UChannel channel,Map<String, String> params) {
        String signSource = null;//app_key(游戏KEY)
        try {
            signSource = "orderid=" + URLEncoder.encode(params.get("orderid"), "UTF-8") +
                    "&username=" + URLEncoder.encode(params.get("username"), "UTF-8") +
                    "&gameid=" + params.get("gameid") +
                    "&roleid=" + URLEncoder.encode(params.get("roleid"), "UTF-8") +
                    "&serverid=" + params.get("serverid") +
                    "&paytype=" + params.get("paytype") +
                    "&amount=" + params.get("amount") +
                    "&paytime=" + params.get("paytime") +
                    "&attach=" + URLEncoder.encode(params.get("attach"), "UTF-8") +
                    "&appkey=" + channel.getCpAppKey();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return EncryptUtils.md5(signSource).equals(params.get("sign"));
    }
}


