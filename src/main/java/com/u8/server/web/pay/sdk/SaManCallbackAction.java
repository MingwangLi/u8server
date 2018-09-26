package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.saman.SaManPayResult;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
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

import static com.u8.server.utils.JsonUtils.map2JsonStr;


@Controller
@Namespace("/pay/saman")

public class SaManCallbackAction extends UActionSupport {

    @Autowired
    private UOrderManager orderManager;


    @Action("payCallback")
    public void payCallback() {

        Map parameterMap = this.request.getParameterMap();
        Map map = transToMAP(parameterMap);
        String s = map2JsonStr(map).toString();
        SaManPayResult result = (SaManPayResult) JsonUtils.decodeJson(s, SaManPayResult.class);


        if (result == null) {
            Log.e("----sa man---pay callBack message is null");
            this.renderState(false);
            return;
        }

        long orderID = Long.parseLong(result.getCp_order_no());
        UOrder order = orderManager.getOrder(orderID);

        if (order == null || order.getChannel() == null) {
            Log.d("----sa man---The order is null or the channel is null ");
            this.renderState(false);
            return;
        }


        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("----sa man---The state of the order is complete  |  The state is " + order.getState());
            this.renderState(true);
            return;
        }


        if (!verifyPay(order.getChannel(), result)) {
            Log.d("----sa man---The sign is not matched");
            order.setChannelOrderID(result.getCp_order_no());
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
            return;
        } else {
            int money = (int) result.getMoney() * 100;
            order.setRealMoney(money);
            order.setCompleteTime(new Date());
            order.setChannelOrderID(result.getPlatform_order_no());
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
        }
        renderState(true);
    }


    private void renderState(boolean suc) {
        String res = "{\"status_code\":200}";
        if (!suc) {
            res = "{\"status_code\":100}";
        }
        PrintWriter out = null;
        try {
            out = this.response.getWriter();
            out.write(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.flush();
    }


//-=================================================================================================

    /***
     * 支付 验证
     * @param channel
     * @param rsp
     * @return
     */
    public boolean verifyPay(UChannel channel, SaManPayResult rsp) {
        String sign = EncryptUtils.md5(rsp.getUid() + rsp.getSid() + rsp.getMoney() + rsp.getPlatform_order_no() + rsp.getCp_order_no() + channel.getCpAppKey());
        return sign.equals(rsp.getSign());
    }


    private Map transToMAP(Map parameterMap) {
        // 返回值Map
        Map returnMap = new HashMap();
        Iterator entries = parameterMap.entrySet().iterator();
        Map.Entry entry;
        String name = "";
        String value = "";
        while (entries.hasNext()) {
            entry = (Map.Entry) entries.next();
            name = (String) entry.getKey();
            Object valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for (int i = 0; i < values.length; i++) {
                    value = values[i] + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = valueObj.toString();
            }
            returnMap.put(name, value);
        }
        return returnMap;
    }

}
