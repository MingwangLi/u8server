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
 * @Author: lizhong
 * @Des: 九九玩
 * @Date: 2018/1/10 15:33
 * @Modified:
 */
@Controller
@Namespace("/pay/99w")
public class JiuJiuWanPayCallbackAction extends UActionSupport{
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
            Log.d("-----99玩----" + name + ":" + valueStr);
        }
        if (params == null) {
            Log.e("----99玩 pay callBack message is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(params.get("paygameorder"));
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
        if (!isSignOK(params,order.getChannel())){
            Log.e("-----------The Sign is not Match!");
            this.renderState(false);
            return;
        }
        if ("1".equals(params.get("paystatus"))) {
            float money = Float.parseFloat(params.get("paymoney"));
            order.setRealMoney((int) money);
            order.setSdkOrderTime("");
            order.setCompleteTime(new Date());
            order.setChannelOrderID(params.get("payorder"));
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            renderState(true);
        } else {
            order.setChannelOrderID(params.get("payorder"));
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
            renderState(false);
        }
    }
    public boolean isSignOK(Map<String, String> params,UChannel channel){
        String payorder = params.get("payorder");
        int a = Integer.valueOf(payorder.substring(8,payorder.length())) - Integer.valueOf(params.get("paymoney"));
        String signStr = EncryptUtils.md5(payorder.substring(0,8) + a) + channel.getCpAppKey();
        return  params.get("appkey").equals(EncryptUtils.md5(signStr));
    }
    private void renderState(boolean suc) {
        String res = "1";
        if (!suc) {
            res = "2";
        }
        PrintWriter out = null;
        try {
            out = this.response.getWriter();
            out.write(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.flush();
        out.close();
    }
}
