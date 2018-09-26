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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
@Controller
@Namespace("/pay/yaolaiwan")
public class YaoLaiWanPayCallbackAction extends UActionSupport {
    @Autowired
    private UOrderManager orderManager;
    public Map<String, String> params = null;
    @Action("payCallback")
    public void payCallback() throws UnsupportedEncodingException {
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
            Log.d("-----------------------" + name + " : " + valueStr);
        }
        if (params == null) {
            Log.e("----YaoLaiWan---pay callBack message is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(params.get("cp_order_id"));
        UOrder order = orderManager.getOrder(orderID);
        if (order == null || order.getChannel() == null) {
            Log.d("----YaoLaiWan---The order is null or the channel is null");
            this.renderState(false);
            return;
        }
        if(order.getState() > PayState.STATE_PAYING) {
            Log.d("The state of the order is complete. The state is " + order.getState());
            this.renderState(true);
            return;
        }
        boolean verify = verifyPay(order.getChannel(), params);
        if (!verify) {
            Log.d("----YaoLaiWan---The sign is not matched");
            this.renderState(false);
            return;
        }
        if ("2".equals(params.get("order_status"))) {
            float money = Float.parseFloat(params.get("product_price"));
            order.setRealMoney((int) money * 100);
            order.setSdkOrderTime(params.get("pay_time"));
            order.setCompleteTime(new Date());
            order.setChannelOrderID(params.get("order_id"));
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            renderState(true);
        } else {
            order.setChannelOrderID(params.get("order_id"));
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
            renderState(false);
        }

    }

    private void renderState(boolean suc) {
        String res = "SUCCESS";
        if (!suc) {
            res = "FAILURE";
        }
        PrintWriter out = null;
        try {
            out = this.response.getWriter();
            out.write(res);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            out.flush();
            out.close();
        }
    }

    /***
     * 支付 验证
     * @param channel
     * @param params
     * @return
     */
    public boolean verifyPay(UChannel channel, Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append("app_id=").append(channel.getCpAppID())
                .append("&cp_order_id=").append(URLEncoder.encode(params.get("cp_order_id"),"UTF-8"))
                .append("&mem_id=").append(params.get("mem_id"))
                .append("&order_id=").append(params.get("order_id"))
                .append("&order_status=").append(params.get("order_status"))
                .append("&pay_time=").append(params.get("pay_time"))
                .append("&product_id=").append(URLEncoder.encode(params.get("product_id"), "UTF-8"))
                .append("&product_name=").append(URLEncoder.encode(params.get("product_name"), "UTF-8"))
                .append("&product_price=").append(URLEncoder.encode(params.get("product_price"), "UTF-8"))
                .append("&app_key=").append(channel.getCpAppKey());
        return params.get("sign").equals(EncryptUtils.md5(sb.toString()));
    }


}

