package com.u8.server.web.pay.sdk;

import com.u8.server.common.PayResult;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.JsonUtils;
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
 * Created by lz on 2017/8/10.
 */
@Controller
@Namespace("/pay/a07073sy")
public class A07073SYPayCallbackAction extends UActionSupport{
    @Autowired
    private UOrderManager orderManager;
    @Action("payCallback")
    public void payCallback() {

        try {
            BufferedReader br = this.request.getReader();
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\r\n");
            }
            String str = sb.toString();
            Log.d("----A07073SY pay callBack  | request params : " + str);
            PayResult rsp = (PayResult) JsonUtils.decodeJson(sb.toString(), PayResult.class);

            if (rsp == null) {
                Log.e("----A07073SY---pay callBack message is null");
                this.renderState(false);
                return;
            }

            long orderID = Long.parseLong(rsp.getAttach());
            UOrder order = orderManager.getOrder(orderID);

            if (order == null || order.getChannel() == null) {
                Log.d("----Aile---The order is null or the channel is null ");
                this.renderState(false);
                return;
            }


            if (order.getState() > PayState.STATE_PAYING) {
                Log.d("----Aile---The state of the order is complete  |  The state is " + order.getState());
                this.renderState(true);
                return;
            }

                float money = Float.parseFloat(rsp.getMoney());
                order.setRealMoney((int) money * 100);
                order.setSdkOrderTime(rsp.getPaytime());
                order.setCompleteTime(new Date());
                order.setChannelOrderID(rsp.getOrder_id());
                order.setState(PayState.STATE_SUC);
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(this.orderManager, order);

        } catch (IOException e) {
            e.printStackTrace();
        }
        renderState(true);
    }


    private void renderState(boolean suc) {
        String res = "SUCCESS";
        if (!suc) {
            res = "FAILURE";
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
}
