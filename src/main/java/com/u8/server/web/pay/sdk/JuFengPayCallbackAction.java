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
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lizhong on 2017/8/10.
 */
@Controller
@Namespace("/pay/5qwan")
public class JuFengPayCallbackAction extends UActionSupport{
    @Autowired
    private UOrderManager orderManager;
    @Action("payCallback")
    public void payCallback(){
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
            Log.d("-----飓风----" + name + ":" + valueStr);
        }
        if (params == null) {
            Log.e("----飓风 pay callBack message is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(params.get("yforder"));
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
        float money = Float.parseFloat(params.get("rmb"));
        order.setRealMoney((int) money * 100);
        order.setSdkOrderTime(params.get("time"));
        order.setCompleteTime(new Date());
        order.setChannelOrderID(params.get("order"));
        order.setState(PayState.STATE_SUC);
        orderManager.saveOrder(order);
        SendAgent.sendCallbackToServer(this.orderManager, order);
        renderState(true);
    }
    public boolean isSignOK(Map<String, String> params,UChannel channel){
        StringBuilder sb = new StringBuilder();
        sb.append(params.get("userid"))
                .append(params.get("gold"))
                .append(params.get("server_id"))
                .append(params.get("order"))
                .append(channel.getCpAppKey());
        return  EncryptUtils.md5(sb.toString()).toLowerCase().equals(params.get("sign"));
    }
    private void renderState(boolean suc) {
        JSONObject res_json = new JSONObject();
        String res = "1";
        res_json.put("msg","充值成功");
        if (!suc) {
            res = "0";
            res_json.put("msg","充值失败");
        }
        res_json.put("result",res);
        PrintWriter out = null;
        try {
            out = this.response.getWriter();
            out.write(res_json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            out.close();
        }
    }
}
