package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * 黑鸭子支付回调
 * @Author: lz
 * @Date: 2016/12/20 14:33.
 */

@SuppressWarnings("all")
@Controller
@Namespace("/pay/heiyazi")
public class HeiYaZiPayCallbackAction extends UActionSupport{
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
            Log.d("-----------------------" + name + ":" + valueStr);
        }
        if (params == null) {
            Log.e("----HeiYaZi---pay callBack message is null");
            this.renderState(false);
            return;
        }
        long localOrderID = Long.parseLong(params.get("cporder"));
        UOrder order = orderManager.getOrder(localOrderID);
        if (order == null || order.getChannel() == null) {
            Log.d("The order is null or the channel is null.");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("The state of the order is complete. The state is " + order.getState());
            this.renderState(true);
            return;
        }
        if(!isSignOK(order.getChannel(), params)){
            Log.d("The sign verify failed");
            this.renderState(false);
            return;
        }
        int moneyInt = (int)(Float.valueOf(params.get("amount")) * 100);
        order.setRealMoney(moneyInt);
        order.setSdkOrderTime(params.get("paytime"));
        order.setCompleteTime(new Date());
        order.setChannelOrderID(params.get("orderid"));
        order.setState(PayState.STATE_SUC);
        orderManager.saveOrder(order);

        SendAgent.sendCallbackToServer(this.orderManager, order);
        renderState(true);

    }catch (Exception e){
        try {
            renderState(false);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        e.printStackTrace();
    }
    }

    private void renderState(boolean suc) throws IOException {

        PrintWriter out = this.response.getWriter();
        if (suc) {
            out.write("success");
        } else {
            out.write("fail");
        }
        out.flush();
    }
    //  支付验证
    private boolean isSignOK(UChannel channel, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append(params.get("role"))
                .append("amount=").append(params.get("amount"))
                .append("&cporder=").append(params.get("cporder"))
                .append("&&orderid=").append(params.get("orderid"))
                .append("&paytime=").append(params.get("paytime"))
                .append("&paytype=").append(params.get("paytype"))
                .append("&role=").append(params.get("role"))
                .append("&server=").append(params.get("server"))
                .append("&time=").append(params.get("time"))
                .append("&user=").append(params.get("user"))
                .append(channel.getCpAppKey());
        return params.get("sign").equals(EncryptUtils.md5(sb.toString()).toLowerCase());
    }
}
