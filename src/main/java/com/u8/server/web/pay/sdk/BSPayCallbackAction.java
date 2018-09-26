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

import java.util.*;

/**
 * 蓝叠
 * Created by lz on 2018/3/19.
 */
@Controller
@Namespace("/pay/bsgame")
public class BSPayCallbackAction extends UActionSupport{
    @Autowired
    private UOrderManager orderManager;
    @Action("payCallback")
    public void payCallback() {
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
            Log.e("------蓝叠 pay callBack | message is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(params.get("cp_order_no"));
        UOrder order = orderManager.getOrder(orderID);

        if (order == null || order.getChannel() == null) {
            Log.d("------蓝叠---------The order is null or the channel is null");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("------蓝叠---------The state of the order is complete  |  The state is " + order.getState());
            this.renderState(true);
            return;
        }
        if (!isSignOK(params,order.getChannel())){
            Log.e("-----------The Sign is not Match!");
            this.renderState(false);
            return;
        }
        if (1 == Integer.valueOf(params.get("pay_status"))){
            int moneyInt = (int) Float.parseFloat(params.get("bs_order_amount"))*100;//以分为单位
            order.setRealMoney(moneyInt);
            order.setCompleteTime(new Date());
            order.setChannelOrderID(params.get("bs_order_no"));
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            this.renderState(true);
        }else{
            order.setChannelOrderID(params.get("bs_order_no"));
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
            renderState(false);
        }

    }
    private void renderState(boolean suc) {
        JSONObject json = new JSONObject();
        json.put("status",true);
        if (!suc) {
            json.put("status",false);
        }
        renderJson(json.toString());
    }

    public boolean isSignOK(Map<String, String> params,UChannel channel) {
      /*首先以key值自然排序,生成key1=val1&key2=val2......&keyN=valN格式的字符串*/
        List<String> keys=new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder postdatasb=new StringBuilder();
        for(int i=0;i<keys.size();i++){
            String k=keys.get(i);
            String v=params.get(k);
            if(!k.equals("u8ChannelID") && !k.equals("timestamp") && !k.equals("signature")) {
                postdatasb.append(v + "|");
            }
        }
        postdatasb.append(channel.getCpAppKey() + "|").append(params.get("timestamp"));
        return params.get("signature").equals(EncryptUtils.md5(postdatasb.toString()));
    }
}
