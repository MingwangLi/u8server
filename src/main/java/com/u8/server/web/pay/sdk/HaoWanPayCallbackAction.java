package com.u8.server.web.pay.sdk;

import com.opensymphony.xwork2.ModelDriven;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.haowan.HaoWanPayResult;
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
/**
 * Author: lizhong
 * Date: 2017.12.11.
 * Desc: 濠玩互娱支付回调
 * */
@Controller
@Namespace("/pay/hwhy")
public class HaoWanPayCallbackAction extends UActionSupport implements ModelDriven<HaoWanPayResult>{
    private HaoWanPayResult rsp = new HaoWanPayResult();

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback() throws Exception{
        if (rsp == null) {
            Log.e("----濠玩互娱---pay callBack message is null");
            this.renderState(false);
            return;
        }
        long localOrderID = Long.parseLong(rsp.getOrderid());
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
        if (!isSignOK(order.getChannel(), rsp)) {
            Log.d("The sign verify failed");
            this.renderState(false);
            return;
        }
        int moneyInt = (int) (Float.valueOf(rsp.getAmount()) * 100);
        order.setRealMoney(moneyInt);
        order.setSdkOrderTime(rsp.getPaytime());
        order.setCompleteTime(new Date());
        order.setChannelOrderID(rsp.getChannelorderid());
        order.setState(PayState.STATE_SUC);
        orderManager.saveOrder(order);
        SendAgent.sendCallbackToServer(this.orderManager, order);
        renderState(true);
    }
    private void renderState(boolean suc) throws IOException {
        PrintWriter out = this.response.getWriter();
        if (suc) {
            out.write("2000");
        } else {
            out.write("2002");
        }
        out.flush();
        out.close();
    }

    @Override
    public HaoWanPayResult getModel() {
        if(rsp == null){
            rsp = new HaoWanPayResult();
        }
        return rsp;
    }

    public static boolean isSignOK(UChannel channel , HaoWanPayResult rsp){
        StringBuilder sb = new StringBuilder();
        sb.append("amount=").append(rsp.getAmount())
                .append("&appkey=").append(channel.getCpAppKey())
                .append("&channelorderid=").append(rsp.getChannelorderid())
                .append("&gameid=").append(channel.getCpAppID())
                .append("&orderid=").append(rsp.getOrderid())
                .append("&paytime=").append(rsp.getPaytime())
                .append("&productname=").append(rsp.getProductname())
                .append("&roleid=").append(rsp.getRoleid())
                .append("&serviceid=").append(rsp.getServiceid())
                .append("&username=").append(rsp.getUsername());
        return rsp.getSign().equals(EncryptUtils.md5(sb.toString()).toLowerCase());
    }
}
