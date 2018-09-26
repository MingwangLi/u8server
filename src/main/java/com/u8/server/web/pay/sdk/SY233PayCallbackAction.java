package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.sy233.UtilVerify;
import com.u8.server.service.UOrderManager;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

/**
 * Created by lz on 2017/11/20.
 */
@Controller
@Namespace("/pay/sy233")
public class SY233PayCallbackAction extends UActionSupport{
    private String gameOrderId;
    @Autowired
    private UOrderManager orderManager;
    @Action("payCallback")
    public void payCallback() {
        long orderID = Long.parseLong(gameOrderId);
        UOrder order = orderManager.getOrder(orderID);

        if (order == null || order.getChannel() == null) {
            Log.d("----sy233---The order is null or the channel is null ");
            this.renderState(false);
            return;
        }
        Map<Object, Object> params = UtilVerify.decryptData(this.request,order.getChannel().getCpPayKey());

        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("----sy233---The state of the order is complete  |  The state is " + order.getState());
            this.renderState(true);
            return;
        }
        if("2".equals(params.get("payStatus").toString())) {
            float money = Float.parseFloat(params.get("payAmount").toString());
            order.setRealMoney((int) money);
            order.setSdkOrderTime("");
            order.setCompleteTime(new Date());
            order.setChannelOrderID(params.get("orderId").toString());
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
        }else {
            order.setChannelOrderID(params.get("orderId").toString());
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
        }
        renderState(true);
    }
    private void renderState(boolean suc) {
        String res = "ok";
        if (!suc) {
            res = "error";
        }
        PrintWriter out = null;
        try {
            out = this.response.getWriter();
            out.write(res);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            out.close();
        }
    }

    public String getGameOrderId() {
        return gameOrderId;
    }

    public void setGameOrderId(String gameOrderId) {
        this.gameOrderId = gameOrderId;
    }
}
