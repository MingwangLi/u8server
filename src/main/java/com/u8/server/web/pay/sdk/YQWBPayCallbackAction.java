package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.wansdk.YQWBPayResult;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.*;

/**
 * 宝莲灯  一起玩吧/玩币SDK
 * Created by lz on 2018/3/22.
 */
@Controller
@Namespace("/pay/yqwb")
public class YQWBPayCallbackAction extends UActionSupport{
    private String tradedata;
    private String sign;
    @Autowired
    private UOrderManager orderManager;
    @Action("payCallback")
    public void payCallback() {
        YQWBPayResult payResult = (YQWBPayResult) JsonUtils.decodeJson(tradedata, YQWBPayResult.class);
        if (payResult == null) {
            Log.e("------一起玩吧 pay callBack | message is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(payResult.getCptradeno());
        UOrder order = orderManager.getOrder(orderID);

        if (order == null || order.getChannel() == null) {
            Log.d("------一起玩吧---------The order is null or the channel is null");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("------一起玩吧---------The state of the order is complete  |  The state is " + order.getState());
            this.renderState(true);
            return;
        }
        if (!isSignOK(tradedata,order.getChannel())){
            Log.e("-----------The Sign is not Match!");
            this.renderState(false);
            return;
        }
        if (0 == Integer.valueOf(payResult.getResult())){
            int moneyInt = Integer.valueOf(payResult.getMoney());//以分为单位
            order.setRealMoney(moneyInt);
            order.setCompleteTime(new Date());
            order.setChannelOrderID(payResult.getTradeno());
            order.setSdkOrderTime(payResult.getTradetime());
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            this.renderState(true);
        }else{
            order.setChannelOrderID(payResult.getTradeno());
            order.setSdkOrderTime(payResult.getTradetime());
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
            renderState(false);
        }

    }
    private void renderState(boolean suc) {
        String res = "success";
        if (!suc) {
            res = "failed";
        }
       renderText(res);
    }

    public boolean isSignOK(String tradedata,UChannel channel) {
        JSONObject json = JSONObject.fromObject(tradedata);
        Map<String,Object> params = json;
      /*首先以key值自然排序,生成key1=val1&key2=val2......&keyN=valN格式的字符串*/
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder postdatasb=new StringBuilder();
        for(int i=0;i<keys.size();i++){
            String k = keys.get(i);
            String v = params.get(k) + "";
            postdatasb.append(k + "=" + v + "&");
        }
        postdatasb.deleteCharAt(postdatasb.length()-1);
        return sign.equals(EncryptUtils.md5(EncryptUtils.md5(postdatasb.toString()) + channel.getCpAppKey()));
    }

    public String getTradedata() {
        return tradedata;
    }

    public void setTradedata(String tradedata) {
        this.tradedata = tradedata;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
