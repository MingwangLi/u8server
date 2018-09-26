package com.u8.server.web.pay.sdk;

import com.u8.server.common.PayResult;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.*;

/**
 * 72G 有戏
 * Created by lz on 2018/3/08.
 */
@Controller
@Namespace("/pay/sy72g")
public class SY72GPayCallbackAction extends UActionSupport{
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
            Log.e("------72G pay callBack | message is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(params.get("oid"));
        UOrder order = orderManager.getOrder(orderID);

        if (order == null || order.getChannel() == null) {
            Log.d("------72G---------The order is null or the channel is null");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("------72G---------The state of the order is complete  |  The state is " + order.getState());
            this.renderState(true);
            return;
        }
        if (!isSignOK(params,order.getChannel())){
            Log.e("-----------The Sign is not Match!");
            this.renderState(false);
            return;
        }
        int moneyInt = (int) Float.parseFloat(params.get("money"))*100;//以分为单位
        order.setRealMoney(moneyInt);
        order.setSdkOrderTime(params.get("timestamps"));
        order.setCompleteTime(new Date());
        order.setChannelOrderID(params.get("orderid"));
        order.setState(PayState.STATE_SUC);
        orderManager.saveOrder(order);
        SendAgent.sendCallbackToServer(this.orderManager, order);
        this.renderState(true);

    }


    private void renderState(boolean suc) {
        JSONObject data = new JSONObject();
        JSONObject json = new JSONObject();
        int status = 0;
        String msg = "success";
        if (!suc) {
            status = 2;
            msg = "fail";
        }
        json.put("status",status);
        json.put("msg",msg);
        json.put("data",data);
        renderJson(json.toString());
    }

    public boolean isSignOK(Map<String, String> params,UChannel channel) {
      StringBuilder sb = new StringBuilder();
      sb.append("gameid=").append(params.get("gameid"))
              .append("money=").append(params.get("money"))
              .append("nonce=").append(params.get("nonce"))
              .append("oid=").append(params.get("oid"))
              .append("orderid=").append(params.get("orderid"))
              .append("timestamps=").append(params.get("timestamps"))
              .append(channel.getCpAppSecret());
      return EncryptUtils.md5(sb.toString()).equals(params.get("sign"));
    }
}
