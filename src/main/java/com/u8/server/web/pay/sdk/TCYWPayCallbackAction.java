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
import java.util.*;

/**
 * 天橙游玩SDK支付回调
 * Created by lz on 2018/3/09.
 */
@Controller
@Namespace("/pay/tcyw")
public class TCYWPayCallbackAction extends UActionSupport{
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
            Log.e("------TCYW pay callBack | message is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(params.get("out_trade_no"));
        UOrder order = orderManager.getOrder(orderID);

        if (order == null || order.getChannel() == null) {
            Log.d("------TCYW---------The order is null or the channel is null");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("------TCYW---------The state of the order is complete  |  The state is " + order.getState());
            this.renderState(true);
            return;
        }
        if (!isSignOK(params,order.getChannel())){
            Log.e("-----------The Sign is not Match!");
            this.renderState(false);
            return;
        }
        if ("SUCCESS".equals(params.get("result_code"))){
            int moneyInt = (int) Float.parseFloat(params.get("total"))*100;//以分为单位
            order.setRealMoney(moneyInt);
            order.setSdkOrderTime(params.get("time_end"));
            order.setCompleteTime(new Date());
            order.setChannelOrderID(params.get("transaction_id"));
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            this.renderState(true);
        }else{
            order.setChannelOrderID(params.get("transaction_id"));
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
            renderState(false);
        }

    }

    private void renderState(boolean suc) {
        String res = "success";
        if(!suc){
            res = "failure";
        }
        PrintWriter out = null;
        try {
            out = this.response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.write(res);
        out.flush();
        out.close();
    }

    public boolean isSignOK(Map<String, String> params,UChannel channel) {
       /*首先以key值自然排序,生成key1=val1&key2=val2......&keyN=valN格式的字符串*/
        List<String> keys=new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder postdatasb=new StringBuilder();
        for(int i=0;i<keys.size();i++){
            String k=keys.get(i);
            String v=params.get(k);
            if(!k.equals("u8ChannelID") && !k.equals("sign")) {
                postdatasb.append(k + "=" + v + "&");
            }
        }
        //对排序后的参数附加开发商签名密钥
        postdatasb.append("key=").append(channel.getCpAppKey());
        return params.get("sign").equals(EncryptUtils.md5(postdatasb.toString()).toUpperCase());
    }
}
