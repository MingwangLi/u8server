package com.u8.server.web.pay.sdk;

import com.opensymphony.xwork2.ModelDriven;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.jinghong.JingHongPayResult;
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
 * 惊鸿互娱 支付回调处理类
 * Created by lizhong on 2017/12/14.
 */
@Controller
@Namespace("/pay/jinghong")
public class JingHongPayCallbackAction extends UActionSupport implements ModelDriven<JingHongPayResult>{
    private JingHongPayResult rsp = new JingHongPayResult();

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback(){
        if (rsp == null) {
            Log.e("----惊鸿互娱---pay callBack message is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(rsp.getGameorder());
        UOrder order = orderManager.getOrder(orderID);

        if (order == null || order.getChannel() == null) {
            Log.d("----惊鸿互娱---The order is null or the channel is null");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("----惊鸿互娱---The state of the order is complete |  The state is " + order.getState());
            this.renderState(true);
            return;
        }
        if (!verifyPay(order.getChannel(), rsp)) {
            Log.d("----惊鸿互娱---The sign is not matched");
            this.renderState(false);
            return;
        }
        float money = Float.parseFloat(rsp.getGoodprice());
        order.setRealMoney((int) money * 100);
        order.setSdkOrderTime("");
        order.setCompleteTime(new Date());
        order.setChannelOrderID(rsp.getOrdernumber());
        order.setState(PayState.STATE_SUC);
        orderManager.saveOrder(order);
        SendAgent.sendCallbackToServer(this.orderManager, order);
        renderState(true);
    }
    /***
     * 支付 验证
     * @param channel
     * @param rsp
     * @return
     */
    public boolean verifyPay(UChannel channel, JingHongPayResult rsp){
        StringBuilder sb = new StringBuilder();
        sb.append("goodname=").append(rsp.getGoodname())
                .append("&goodprice=").append(rsp.getGoodprice())
                .append("&ordernumber=").append(rsp.getOrdernumber())
                .append("&userid=").append(rsp.getUserid())
                .append("&gameorder=").append(rsp.getGameorder())
                .append("&").append(channel.getCpAppKey());
        return rsp.getSign().equals(EncryptUtils.md5(sb.toString()));
    }

    private void renderState(boolean suc) {
        String res = "success";
        if (!suc) {
            res = "failure";
        }
        PrintWriter out = null;
        try {
            out = this.response.getWriter();
            out.write(res);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            out.flush();
            out.close();
        }
    }

    @Override
    public JingHongPayResult getModel() {
        if( rsp == null ){
            rsp = new JingHongPayResult();
        }
        return rsp;
    }
}
