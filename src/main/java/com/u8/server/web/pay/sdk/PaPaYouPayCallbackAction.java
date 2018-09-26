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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 啪啪游SDK 支付回调处理类
 * Created by lizhong on 2017/10/19.
 */

@Controller
@Namespace("/pay/ppy")
public class PaPaYouPayCallbackAction extends UActionSupport{
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
            Log.d("-------啪啪游---------" + name + ":" + valueStr);
        }
        if (params == null) {
            Log.e("------啪啪游 pay callBack | message is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(params.get("outorderno"));
        UOrder order = orderManager.getOrder(orderID);

        if (order == null || order.getChannel() == null) {
            Log.d("------啪啪游---------The order is null or the channel is null");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("------啪啪游---------The state of the order is complete  |  The state is " + order.getState());
            this.renderState(true);
            return;
        }
        if (!isSignOK(params,order.getChannel())){
            Log.e("-----------The Sign is not Match!");
            this.renderState(false);
            return;
        }
        int moneyInt = (int)(Float.parseFloat(params.get("dealprice")))*100;//以分为单位
        order.setRealMoney(moneyInt);
        order.setSdkOrderTime(params.get("submittime"));
        order.setCompleteTime(new Date());
        order.setChannelOrderID(params.get("orderno"));
        order.setState(PayState.STATE_SUC);
        orderManager.saveOrder(order);
        SendAgent.sendCallbackToServer(this.orderManager, order);
        this.renderState(true);

    }
    public boolean isSignOK(Map<String, String> params,UChannel channel) throws UnsupportedEncodingException {
        //signSrc=submittime+outorderno+orderno+userid+dealprice+paytype+key;
        StringBuilder sb = new StringBuilder();
        sb.append(params.get("submittime"))
                .append(params.get("outorderno"))
                .append(params.get("orderno"))
                .append(params.get("userid"))
                .append(params.get("dealprice"))
                .append(params.get("paytype"))
                .append(channel.getCpAppKey());
        String signStr = sb.toString();
        String newSign = EncryptUtils.md5(signStr).toUpperCase();
        return  newSign.equals(params.get("sign"));
    }
    private void renderState(boolean suc) throws IOException {
        String res = "10001";
        if (!suc) {
            res = "10002";
        }
        PrintWriter out = this.response.getWriter();
        out.write(res);
        out.flush();
        out.close();
    }
}
