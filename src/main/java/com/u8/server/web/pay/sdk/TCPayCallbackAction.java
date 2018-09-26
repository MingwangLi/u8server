package com.u8.server.web.pay.sdk;


import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.tc.TCPayCallbackInfo;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.JsonUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by lvxinmin on 2016/11/15.
 */
@Namespace("/pay/tc")
public class TCPayCallbackAction extends UActionSupport {

    @Autowired
    private UOrderManager orderManager;

    public Map<String, String> params = null;

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
        String s = mapTojson(params);
        Log.d("-------TC paycallBack | request params:" + s);
        TCPayCallbackInfo rsp = (TCPayCallbackInfo) JsonUtils.decodeJson(s, TCPayCallbackInfo.class);
        if (rsp == null) {
            Log.d("----------TC paycallBack message is null");
            this.renderState(false);
            return;
        }

        long orderID = Long.parseLong(rsp.getOrder());
        UOrder order = orderManager.getOrder(orderID);

        if (order == null || order.getChannel() == null) {
            Log.d("------The order is null or the channel is null ");
            this.renderState(false);
            return;
        }

        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("------The state of the order is complete  |  The state is " + order.getState());
            this.renderState(true);
            return;
        }

        order.setRealMoney(rsp.getCoin_amount());
        order.setSdkOrderTime("");
        order.setCompleteTime(new Date());
        order.setChannelOrderID(rsp.getOrder());
        order.setState(PayState.STATE_SUC);
        orderManager.saveOrder(order);
        SendAgent.sendCallbackToServer(this.orderManager, order);
        renderState(true);
    }


    private void renderState(boolean suc) {
        String res = "SUCCESS";
        if (!suc)
            res = "FAILED";
        try {
            PrintWriter out = this.response.getWriter();
            out.write(res);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //===================================================================================================


    public static String mapTojson(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "null";
        }
        String jsonStr = "{";
        Set<?> keySet = map.keySet();
        for (Object key : keySet) {
            jsonStr += "\"" + key + "\":\"" + map.get(key) + "\",";
        }
        jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
        jsonStr += "}";
        return jsonStr;
    }
}
