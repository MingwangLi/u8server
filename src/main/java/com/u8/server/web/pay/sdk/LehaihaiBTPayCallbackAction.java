package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.lehaihai.LehaihaiPayResult;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Date;

/**
 * 乐嗨嗨BT(Android)支付回调处理类
 * Created by lizhong on 2017/11/20.
 */

@Controller
@Namespace("/pay/btgame")
public class LehaihaiBTPayCallbackAction extends UActionSupport{
    private String data;
    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback(){
        try{

            LehaihaiPayResult rsp = (LehaihaiPayResult) JsonUtils.decodeJson(data, LehaihaiPayResult.class);

            if(rsp == null){
                Log.e("the data parse failed. data:"+data);
                return;
            }
            long orderID = Long.parseLong(rsp.getExtendsInfo());
            UOrder order = orderManager.getOrder(orderID);
            if(order == null){
                Log.d("The order is null");
                this.renderState(false);
                return;
            }
            UChannel channel = order.getChannel();
            if(channel == null){
                Log.d("the channel is null.");
                this.renderState(false);
                return;
            }
            if(order.getState() > PayState.STATE_PAYING) {
                Log.d("The state of the order is complete. The state is " + order.getState());
                this.renderState(true);
                return;
            }
            if(!isSignOK(channel, rsp)){
                Log.d("The sign verify failed.sign:%s;appKey:%s;orderID:%s", rsp.getSign(), channel.getCpPayKey(), rsp.getExtendsInfo());
                this.renderState(false);
                return;
            }
            int moneyInt = (int)(Float.valueOf(rsp.getAmount()) * 100);  //以分为单位
            order.setRealMoney(moneyInt);
            order.setSdkOrderTime(rsp.getTime());
            order.setCompleteTime(new Date());
            order.setChannelOrderID(rsp.getOrderid());
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            renderState(true);
        }catch(Exception e){
            try {
                renderState(false);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private boolean isSignOK(UChannel channel, LehaihaiPayResult rsp){
        StringBuilder sb = new StringBuilder();
        sb.append("amount=").append(rsp.getAmount())
                .append("&extendsInfo=").append(rsp.getExtendsInfo())
                .append("&gameid=").append(rsp.getGameid())
                .append("&orderid=").append(rsp.getOrderid())
                .append("&serverid=").append(rsp.getServerid())
                .append("&time=").append(rsp.getTime())
                .append("&uid=").append(rsp.getUid())
                .append(channel.getCpAppSecret());
        return EncryptUtils.md5(sb.toString()).toLowerCase().equals(rsp.getSign());
    }

    private void renderState(boolean suc) throws IOException {

        String res = "succ";
        if(!suc){
            res = "fail";
        }
        renderText(res);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
