package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UOrderManager;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lvxinmin on 2016/11/23.
 */

@Controller
@Namespace("/pay/youhui")
public class YouHuiPayCallbackAction extends UActionSupport {

    @Autowired
    private UOrderManager orderManager;

    Map<String, String> params = null;

    @Action("payCallback")
    public void payCallback() {
        // 获取支付宝POST过来反馈信息
        params = new HashMap<String, String>();
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
        }

        if (params == null) {
            Log.e("----AiPu---pay callBack message is null");
            this.renderState(false);
            return;
        }

        long orderID = Long.parseLong(params.get("orderid"));
        UOrder order = orderManager.getOrder(orderID);

        if (order == null || order.getChannel() == null) {
            Log.d("The order is null or the channel is null");
            this.renderState(false);
            return;
        }


        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("The state of the order is complete |  The state is " + order.getState());
            this.renderState(true);
            return;
        }


        if (!verifyPay(order.getChannel(), params)) {
            Log.d("The sign is not matched");
            order.setChannelOrderID(params.get("orderid"));
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
            this.renderState(true);
            return;
        } else {
            int money = Integer.parseInt(params.get("amount")) * 100;
            order.setRealMoney(money);
            order.setSdkOrderTime(params.get("paytime"));
            order.setCompleteTime(new Date());
            order.setChannelOrderID(params.get("orderid"));
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
        }
        renderState(true);
    }


    private void renderState(boolean suc) {
        String res = "success";
        PrintWriter out = null;
        try {
            out = this.response.getWriter();
            out.write(res);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /***
     * 支付 验证
     * @param channel
     * @param params
     * @return
     */
    public boolean verifyPay(UChannel channel, Map<String, String> params) {
        String signSource =
                "orderid=" + params.get("orderid") +
                        "&username=" + params.get("username") +
                        "&gameid=" + params.get("gameid") +
                        "&roleid=" + params.get("roleid") +
                        "&serverid=" + params.get("serverid") +
                        "&paytype=" + params.get("paytype") +
                        "&amount=" + params.get("amount") +
                        "&paytime=" + params.get("paytime") +
                        "&attach=" + params.get("attach") +
                        "&appkey=" + channel.getCpAppKey();

        String sign = md5(signSource);
        return sign.equals(params.get("sign"));
    }

    public String md5(String name) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffers = md.digest(name.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < buffers.length; i++) {
                String s = Integer.toHexString(0xff & buffers[i]);
                if (s.length() == 1) {
                    sb.append("0" + s);
                }
                if (s.length() != 1) {
                    sb.append(s);
                }
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
