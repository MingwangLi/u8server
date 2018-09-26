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
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *  爱应用支付回调 Created By lizhong
 *  Date: 2017/10/26.
 */
@Controller
@Namespace("/pay/ayy")
public class AyyPayCallbackAction extends UActionSupport{
    @Autowired
    private UOrderManager orderManager;
    @Action("payCallback")
    public void payCallback() throws Exception {
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
            Log.d("-----爱应用----" + name + ":" + valueStr);
        }
        if (params == null) {
            Log.e("----爱应用 pay callBack message is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(params.get("o_id"));
        UOrder order = orderManager.getOrder(orderID);
        if (order == null || order.getChannel() == null) {
            Log.d("----The order is null or the channel is null ");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("----The state of the order is complete  |  The state is " + order.getState());
            this.renderState(true);
            return;
        }
        if(!"1".equals(params.get("t_status"))){
            Log.d("----The order status is not pay");
            this.renderState(false);
            return;
        }
        if (!isSignOK(params,order.getChannel())){
            Log.e("-----------The Sign is not Match!");
            this.renderState(false);
            return;
        }
        float money = Float.parseFloat(params.get("t_fee"));
        order.setRealMoney((int) money * 100);
        order.setSdkOrderTime(params.get("n_time"));
        order.setCompleteTime(new Date());
        order.setChannelOrderID(params.get("o_orderid"));
        order.setState(PayState.STATE_SUC);
        orderManager.saveOrder(order);
        SendAgent.sendCallbackToServer(this.orderManager, order);
        renderState(true);

    }
    public boolean isSignOK(Map<String, String> params,UChannel channel) throws Exception {
        /*n_time=$n_time&appid=$appid&o_id=$o_id&t_fee=$t_fee&g_name=$g_name&g_body=$g_body&t_status=$t_status$appkey*/
        StringBuilder sb = new StringBuilder();
        sb.append("n_time=").append(URLEncoder.encode(params.get("n_time"),"UTF-8"))
                .append("&appid=").append(channel.getCpAppID())
                .append("&o_id=").append(URLEncoder.encode(params.get("o_id"),"UTF-8"))
                .append("&t_fee=").append(URLEncoder.encode(params.get("t_fee"),"UTF-8"))
                .append("&g_name=").append(URLEncoder.encode(params.get("g_name"),"UTF-8"))
                .append("&g_body=").append(URLEncoder.encode(params.get("g_body"),"UTF-8"))
                .append("&t_status=").append(params.get("t_status"))
                .append(channel.getCpAppKey());
        String signStr = sb.toString();
        String newSign = EncryptUtils.md5(signStr);
        return  newSign.equals(params.get("o_sign"));
    }
    private void renderState(boolean suc) {
        String res = "success";
        if (!suc) {
            res = "failure";
        }
        PrintWriter out = null;
        try {
            out = this.response.getWriter();
            out.write(res);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            out.close();
        }
    }
}
