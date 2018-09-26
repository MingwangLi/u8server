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
import java.util.*;

/**
 * @Author: lizhong
 * @Des: 努比亚SDK
 * @Date: 2018/2/2 10:14
 * @Modified:
 */
@Controller
@Namespace("/pay/nubia")
public class NubiaPayCallbackAction extends UActionSupport{
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
            Log.d("---------Nubia-------" + name + ":" + valueStr);
        }
        if (params == null) {
            this.renderState(10000,"参数为空");
            return;
        }
        long orderID = Long.parseLong(params.get("order_no"));
        UOrder order = orderManager.getOrder(orderID);
        if (order == null || order.getChannel() == null) {
            this.renderState(10000,"order_no错误");
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            this.renderState(0,"成功");
            return;
        }
        if (!isSignOK(params,order.getChannel())){
            Log.d("THE SIGN ERROR");
            this.renderState(10000,"签名失败");
            return;
        }
        if(Integer.valueOf(params.get("pay_success")) == 1){
            int moneyInt = Integer.valueOf(params.get("amount"));
            order.setRealMoney(moneyInt);
            order.setSdkOrderTime(params.get("data_timestamp"));
            order.setCompleteTime(new Date());
            order.setChannelOrderID(params.get("order_serial"));
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            this.renderState(0,"成功");
        }else {
            order.setChannelOrderID(params.get("order_serial"));
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
            this.renderState(10000,"返回的订单状态是失败状态");
        }
    }
    public boolean isSignOK(Map<String, String> params,UChannel channel){
        /*首先以key值自然排序,生成key1=val1&key2=val2......&keyN=valN格式的字符串*/
        List<String> keys=new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder postdatasb=new StringBuilder();
        for(int i=0;i<keys.size();i++){
            String k=keys.get(i);
            String v=params.get(k);
            if(!k.equals("u8ChannelID") && !k.equals("sign") && !k.equals("order_serial") && !k.equals("order_sign")) {
                postdatasb.append(k + "=" + v + "&");
            }
        }
        postdatasb.deleteCharAt(postdatasb.length()-1);
        //对排序后的参数附加开发商签名密钥
        postdatasb.append(":").append(channel.getCpAppID()).append(":").append(channel.getCpAppSecret());
        return params.get("order_sign").equals(EncryptUtils.md5(postdatasb.toString()).toLowerCase());
    }
    private void renderState(int code ,String msg) throws IOException {
        JSONObject data = new JSONObject();
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("data",data);
        json.put("message",msg);
        this.renderJson(json.toString());
    }
}
