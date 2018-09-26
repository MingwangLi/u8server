package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.shendeng.ShenDengPayResult;
import com.u8.server.sdk.shendeng.ShenDengSDK;
import com.u8.server.service.UOrderManager;
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

/**
 * Created by ${lvxinmin} on 2016/12/5.
 */


@Controller
@Namespace("/pay/shendeng")
public class ShenDengPayCallbackAction extends UActionSupport {

    @Autowired
    private UOrderManager orderManager;


    @Action("payCallback")
    public void payCallback() {

        Map parameterMap = this.request.getParameterMap();
        Map map = transToMAP(parameterMap);

        String s = map2JsonStr(map).toString();
        ShenDengPayResult result = (ShenDengPayResult) JsonUtils.decodeJson(s, ShenDengPayResult.class);


        if (result == null) {
            Log.e("----ShenDeng---pay callBack message is null");
            this.renderState(false);
            return;
        }

        long orderID = Long.parseLong(result.getProductDesc());
        UOrder order = orderManager.getOrder(orderID);

        if (order == null || order.getChannel() == null) {
            Log.d("----ShenDeng---The order is null or the channel is null ");
            this.renderState(false);
            return;
        }


        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("----ShenDeng---The state of the order is complete  |  The state is " + order.getState());
            this.renderState(true);
            return;
        }


        if (!verifyPay(order.getChannel(), result)) {
            Log.d("----ShenDeng---The sign is not matched");
            this.renderState(true);
            return;
        }

        if ("3".equals(result.getResult())) {
            String price = result.getPrice();
            order.setRealMoney(Integer.parseInt(price));
            order.setSdkOrderTime(result.getPayedTime());
            order.setCompleteTime(new Date());
            order.setChannelOrderID(result.getOrderId());
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
        } else {
            order.setChannelOrderID(result.getOrderId());
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
        }
        renderState(true);
    }


    //=======================================================================
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
        }
        out.flush();
    }


    public boolean verifyPay(UChannel channle, ShenDengPayResult result) {
        String secret = channle.getCpAppSecret();

        String Res = secret +
                "mid" + result.getMid() +
                "orderId" + result.getOrderId() +
                "price" + result.getPrice() +
                "productDesc" + result.getProductDesc() +
                "productName" + result.getProductName() +
                "result" + result.getResult() + secret;

        String sign = ShenDengSDK.MD5(Res);
        return sign.equals(result.getSign());
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