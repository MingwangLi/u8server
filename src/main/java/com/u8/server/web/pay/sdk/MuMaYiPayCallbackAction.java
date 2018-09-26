package com.u8.server.web.pay.sdk;

import com.opensymphony.xwork2.ModelDriven;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.mumayi.MMYPayResult;
import com.u8.server.sdk.mumayi.MuMaYiSDK;
import com.u8.server.service.UOrderManager;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * 木蚂蚁支付回调
 * Created by ant on 2015/9/14.
 */
@Controller
@Namespace("/pay/mumayi")
public class MuMaYiPayCallbackAction extends UActionSupport implements ModelDriven<MMYPayResult>{
    private MMYPayResult result = new MMYPayResult();
    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback(){
        try{
            if(result == null){
                renderState(false);
                return;
            }
            long orderID = Long.parseLong(result.getProductDesc());

            UOrder order = orderManager.getOrder(orderID);

            if(order == null || order.getChannel() == null){
                Log.d("The order is null or the channel is null.");
                this.renderState(false);
                return;
            }

            UChannel channel = order.getChannel();
            if(channel == null){

                this.renderState(false);
                return;
            }

            if(order.getState() > PayState.STATE_PAYING){
                Log.d("The state of the order is complete. The state is "+order.getState());
                this.renderState(true);
                return;
            }

            if(!MuMaYiSDK.verify(result.getTradeSign(), channel.getCpAppKey(), result.getOrderID())){
                Log.d("The sign verify failed.sign:%s;appKey:%s;orderID:%s", result.getTradeSign(), channel.getCpAppKey(), result.getOrderID());
                this.renderState(true);
                return;
            }

            //TODO:这里价格单位需要和木蚂蚁技术沟通确认下
            float money = Float.parseFloat(result.getProductPrice());
            int moneyInt = (int)(money * 100);  //以分为单位
            order.setRealMoney(moneyInt);
            order.setSdkOrderTime(result.getOrderTime());
            order.setCompleteTime(new Date());
            order.setChannelOrderID(result.getOrderID());
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            this.renderState(true);
        }catch(Exception e){
            try {
                renderState(false);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private void renderState(boolean suc) throws IOException {

        String res = "success";
        if(!suc){
            res = "fail";
        }

        PrintWriter out = this.response.getWriter();
        out.write(res);
        out.flush();
    }


    @Override
    public MMYPayResult getModel() {
        if (this.result == null){
            this.result = new MMYPayResult();
        }
        return this.result;
    }
}
