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
 * 七忍SDK
 * Created by lizhong on 2017/11/20.
 */
@Controller
@Namespace("/pay/qrgame")
public class QiRenPayCallbackAction extends UActionSupport {
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
            Log.d("----七忍----" + name + ":" + valueStr);
        }
        if (params == null) {
            Log.d("The 七忍 Params is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(params.get("extra_param"));
        UOrder order = orderManager.getOrder(orderID);
        if (order == null || order.getChannel() == null) {
            Log.d("The 七忍 order is null");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("The 七忍 Pay state error");
            this.renderState(true);
            return;
        }
        if (!isSignOK(params,order.getChannel())){
            Log.d("The 七忍 sign is error");
            this.renderState(false);
            return;
        }
        if("TRADE_SUCCESS".equals(params.get("trade_status"))){
            int moneyInt = (int)(Double.valueOf(params.get("amount"))*100);//以元为单位
            order.setRealMoney(moneyInt);
            order.setSdkOrderTime(System.currentTimeMillis()+"");
            order.setCompleteTime(new Date());
            order.setChannelOrderID(params.get("order_id"));
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            this.renderState(true);
        }else {
            order.setChannelOrderID(params.get("order_id"));
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
            this.renderState(false);
        }

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
        postdatasb.deleteCharAt(postdatasb.length()-1);
        //对排序后的参数附加开发商签名密钥
        postdatasb.append(channel.getCpPayKey());
        return params.get("sign").equals(EncryptUtils.md5(postdatasb.toString()));
    }

    private void renderState(boolean suc) throws IOException {
        String res = "OK";
        if (!suc) {
            res = "ERROR";
        }
        PrintWriter out = this.response.getWriter();
        out.write(res);
        out.close();
    }

}
