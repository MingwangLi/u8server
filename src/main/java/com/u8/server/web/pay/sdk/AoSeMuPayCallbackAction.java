package com.u8.server.web.pay.sdk;

import com.opensymphony.xwork2.ModelDriven;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.asm.AoSeMuPayResult;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Created by lizhong on 2017/11/20.
 */
@Controller
@Namespace("/pay/asm")
public class AoSeMuPayCallbackAction extends UActionSupport implements ModelDriven<AoSeMuPayResult> {
    private AoSeMuPayResult rsp = new AoSeMuPayResult();
    @Autowired
    private UOrderManager orderManager;
    @Action("payCallback")
    public void payCallback() throws IOException {
        if (rsp == null) {
            Log.d("The 奥瑟姆 Params is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(rsp.getExtend());
        UOrder order = orderManager.getOrder(orderID);
        if (order == null || order.getChannel() == null) {
            Log.d("The 奥瑟姆 order is null");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("The 奥瑟姆 Pay state error");
            this.renderState(true);
            return;
        }
        if (!isSignOK(rsp,order.getChannel())){
            Log.d("The 奥瑟姆 sign is error");
            this.renderState(false);
            return;
        }
        if(1 == Integer.parseInt(rsp.getPay_status())){
            int moneyInt = (int)(Float.valueOf(rsp.getPrice())*100);  //以元为单位
            order.setRealMoney(moneyInt);
            order.setSdkOrderTime(System.currentTimeMillis()+"");
            order.setCompleteTime(new Date());
            order.setChannelOrderID(rsp.getOut_trade_no());
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            this.renderState(true);
        }else {
            order.setChannelOrderID(rsp.getOut_trade_no());
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
            this.renderState(false);
        }

    }
    public boolean isSignOK(AoSeMuPayResult rsp,UChannel channel) {
        //sign=MD5(订单号+价格+支付状态+扩展参数+KEY）
        StringBuilder sb = new StringBuilder();
        sb.append(rsp.getOut_trade_no())
                .append(rsp.getPrice())
                .append(rsp.getPay_status())
                .append(rsp.getExtend())
                .append(channel.getCpAppKey());
        return rsp.getSign().equals(EncryptUtils.md5(sb.toString()).toLowerCase());
    }

    private void renderState(boolean suc) throws IOException {
        String res = "success";
        if (!suc) {
            res = "error";
        }
        PrintWriter out = this.response.getWriter();
        out.write(res);
        out.close();
    }

    @Override
    public AoSeMuPayResult getModel() {
        if( this.rsp == null ){
            this.rsp = new AoSeMuPayResult();
        }
        return this.rsp;
    }
}
