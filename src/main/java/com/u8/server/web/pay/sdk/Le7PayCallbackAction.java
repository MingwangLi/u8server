package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lizhong on 2018/03/19.
 */
@Controller
@Namespace("/pay/le7")
public class Le7PayCallbackAction extends UActionSupport {
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
            Log.d("-------乐七-------" + name + ":" + valueStr);
        }
        if (params == null) {
            Log.d("The 乐七 Params is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(params.get("attach"));
        UOrder order = orderManager.getOrder(orderID);
        if (order == null || order.getChannel() == null) {
            Log.d("The 乐七 order is null");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("The 乐七 Pay state error");
            this.renderState(true);
            return;
        }
        if (!isSignOK(params,order.getChannel())){
            Log.d("The 乐七 sign is error");
            this.renderState(false);
            return;
        }
        int moneyInt = (int)(Double.valueOf(params.get("amount"))*100);//以分为单位
        order.setRealMoney(moneyInt);
        order.setSdkOrderTime(params.get("paytime"));
        order.setCompleteTime(new Date());
        order.setChannelOrderID(params.get("orderid"));
        order.setState(PayState.STATE_SUC);
        orderManager.saveOrder(order);
        SendAgent.sendCallbackToServer(this.orderManager, order);
        this.renderState(true);

    }
    public boolean isSignOK(Map<String, String> params,UChannel channel) {
        StringBuilder sb = new StringBuilder();
        sb.append("orderid=").append(params.get("orderid"))
                .append("&username=").append(params.get("username"))
                .append("&gameid=").append(channel.getCpID())
                .append("&roleid=").append(params.get("roleid"))
                .append("&serverid=").append(params.get("serverid"))
                .append("&paytype=").append(params.get("paytype"))
                .append("&amount=").append(params.get("amount"))
                .append("&paytime=").append(params.get("paytime"))
                .append("&attach=").append(params.get("attach"))
                .append("&appkey=").append(channel.getCpAppKey());
        return  EncryptUtils.md5(sb.toString()).equals(params.get("sign"));
    }

    private void renderState(boolean suc) throws IOException {
        JSONObject json = new JSONObject();
        json.put("status","succ");
        json.put("msg","充值成功");
        if (!suc) {
            json.put("status","fail");
            json.put("msg","充值失败");
        }
        renderJson(json.toString());
    }

}
