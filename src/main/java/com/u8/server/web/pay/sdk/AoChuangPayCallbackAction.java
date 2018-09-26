package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UChannelManager;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * 奥创---支付回调
 * @Author: lz
 * @Date: 2016/12/19 16:22.
 */
@SuppressWarnings("all")
@Controller
@Namespace("/pay/aochuang")
public class AoChuangPayCallbackAction extends UActionSupport{

    @Autowired
    private UOrderManager orderManager;

    @Autowired
    private UChannelManager channelManager;

    @Action("payCallback")
    public void payCallback() {
        try {
            // 获取支付宝POST过来反馈信息
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
                Log.d("-----------------------" + name + ":" + valueStr);
            }
            if (params == null) {
                Log.e("----AoChuang---pay callBack message is null");
                this.renderState(false);
                return;
            }
            long localOrderID = Long.parseLong(params.get("cporderid"));
            UOrder order = orderManager.getOrder(localOrderID);
            if (order == null || order.getChannel() == null) {
                Log.d("The order is null or the channel is null.");
                this.renderState(false);
                return;
            }
            if (order.getState() > PayState.STATE_PAYING) {
                Log.d("The state of the order is complete. The state is " + order.getState());
                this.renderState(true);
                return;
            }
            if (isSignOK(order.getChannel(), params)) {
                order.setChannelOrderID(params.get("orderid"));
                int money = Integer.parseInt(params.get("amount")) * 100;
                order.setRealMoney(money);
                order.setSdkOrderTime(params.get("time"));
                order.setCompleteTime(new Date());
                order.setState(PayState.STATE_SUC);
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(this.orderManager, order);
                this.renderState(true);
            }else{
                Log.d("The sign is not matched");
                order.setChannelOrderID(params.get("orderid"));
                order.setState(PayState.STATE_FAILED);
                orderManager.saveOrder(order);
                this.renderState(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                this.renderState(false);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }
    private void renderState(boolean suc) throws IOException {

        PrintWriter out = this.response.getWriter();
        if (suc) {
            out.write("SUCCESS");
        } else {
            out.write("ERROR");
        }
        out.flush();
    }
//  支付验证
    private boolean isSignOK(UChannel channel, Map<String, String> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = params.get(k);
            postdatasb.append(k + "=" + v + "&");
        }
        postdatasb.deleteCharAt(postdatasb.length() - 1);
        //对排序后的参数附加开发商签名密钥
        postdatasb.append(channel.getCpAppKey());
        String sign = EncryptUtils.md5(postdatasb.toString());
        Log.d("the sign data is " + postdatasb.toString());
        return sign.equals(params.get("sign"));
    }


}


