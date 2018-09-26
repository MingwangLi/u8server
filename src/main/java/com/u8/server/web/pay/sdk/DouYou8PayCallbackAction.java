package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UChannelManager;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.RSAUtils;
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

/**
 * 逗游8SDK 支付回调处理类
 * Created by lizhong on 2017/10/19.
 */

@Controller
@Namespace("/pay/douyou8")
public class DouYou8PayCallbackAction extends UActionSupport{
    @Autowired
    private UOrderManager orderManager;

    @Autowired
    private UChannelManager channelManager;

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
            Log.d("-------douyou8---------" + name + ":" + valueStr);
        }
        if (params == null) {
            Log.e("------douyou8 pay callBack | message is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(params.get("attach"));
        UOrder order = orderManager.getOrder(orderID);

        if (order == null || order.getChannel() == null) {
            Log.d("------douyou8---------The order is null or the channel is null");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("------douyou8---------The state of the order is complete  |  The state is " + order.getState());
            this.renderState(true);
            return;
        }
        if (!isSignOK(params,order.getChannel())){
            Log.e("-----------The Sign is not Match!");
            this.renderState(false);
            return;
        }
        int moneyInt = (int)(Float.parseFloat(params.get("amount"))*100);//以分为单位
        order.setRealMoney(moneyInt);
        order.setSdkOrderTime(params.get("paytime"));
        order.setCompleteTime(new Date());
        order.setChannelOrderID(params.get("orderid"));
        order.setState(PayState.STATE_SUC);
        orderManager.saveOrder(order);
        SendAgent.sendCallbackToServer(this.orderManager, order);
        this.renderState(true);

    }
    public boolean isSignOK(Map<String, String> params,UChannel channel) throws UnsupportedEncodingException {
        //sign=MD5(订单号+价格+支付状态+扩展参数+KEY）
        StringBuilder sb = new StringBuilder();
        sb.append("orderid=").append(URLEncoder.encode(params.get("orderid"),"UTF-8"))
                .append("&username=").append(URLEncoder.encode(params.get("username"),"UTF-8"))
                .append("&gameid=").append(channel.getCpID())
                .append("&roleid=").append(URLEncoder.encode(params.get("roleid"),"UTF-8"))
                .append("&serverid=").append(params.get("serverid"))
                .append("&paytype=").append(URLEncoder.encode(params.get("paytype"),"UTF-8"))
                .append("&amount=").append(params.get("amount"))
                .append("&paytime=").append(params.get("paytime"))
                .append("&attach=").append(URLEncoder.encode(params.get("attach"),"UTF-8"))
                .append("&appkey=").append(channel.getCpAppKey());
        return  EncryptUtils.md5(sb.toString()).equals(params.get("sign"));
    }
    private void renderState(boolean suc) throws IOException {
        String res = "success";
        if (!suc) {
            res = "error";
        }
        PrintWriter out = this.response.getWriter();
        out.write(res);
        out.flush();
        out.close();
    }
}
