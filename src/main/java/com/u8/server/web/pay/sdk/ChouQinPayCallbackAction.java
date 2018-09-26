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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lizhong on 2017/11/20.
 */
@Controller
@Namespace("/pay/chouqin")
public class ChouQinPayCallbackAction extends UActionSupport {
    @Autowired
    private UOrderManager orderManager;
    @Action("payCallback")
    public void payCallback() throws IOException {
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
            Log.d("------酬勤-----" + name + ":" + valueStr);
        }
        if (params == null) {
            Log.d("The 酬勤 Params is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(params.get("extend"));
        UOrder order = orderManager.getOrder(orderID);
        if (order == null || order.getChannel() == null) {
            Log.d("The 酬勤 order is null");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("The 酬勤 Pay state error");
            this.renderState(true);
            return;
        }
        if (!isSignOK(params,order.getChannel())){
            Log.d("The 酬勤 sign is error");
            this.renderState(false);
            return;
        }
        if(1 == Integer.valueOf(params.get("pay_status"))){
            int moneyInt = (int)(Double.valueOf(params.get("price"))*100);  //以元为单位
            order.setRealMoney(moneyInt);
            order.setSdkOrderTime(System.currentTimeMillis()+"");
            order.setCompleteTime(new Date());
            order.setChannelOrderID(params.get("out_trade_no"));
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            this.renderState(true);
        }else {
            order.setChannelOrderID(params.get("out_trade_no"));
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
            this.renderState(false);
        }

    }
    public boolean isSignOK(Map<String, String> params,UChannel channel) {
        //sign=MD5(订单号+价格+支付状态+扩展参数+KEY）
        StringBuilder sb = new StringBuilder();
        sb.append(params.get("out_trade_no"))
                .append(params.get("price"))
                .append(params.get("pay_status"))
                .append(params.get("extend"))
                .append(channel.getCpAppKey());
        return  params.get("sign").equals(EncryptUtils.md5(sb.toString()).toLowerCase());
    }

    private void renderState(boolean suc) throws IOException {
        String res = "success";
        if (!suc) {
            res = "error";
        }
        PrintWriter out = this.response.getWriter();
        out.write(res);
        out.close();
    }

}
