package com.u8.server.web.pay.sdk;

import com.u8.server.common.PayResult;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
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
import java.security.MessageDigest;
import java.util.Date;

import static java.lang.Integer.toHexString;

@Controller
@Namespace("/pay/hongshouzhi")
public class HongShouZhiPayCallbackAction extends UActionSupport {

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

            Log.d("------HongShouZhi pay callBack | request params:" + sb.toString());
            PayResult rsp = (PayResult) JsonUtils.decodeJson(sb.toString(), PayResult.class);

            if (rsp == null) {
                Log.e("------HongShouZhi pay callBack | message is null");
                this.renderState(false);
                return;
            }

            long orderID = Long.parseLong(rsp.getAttach());
            UOrder order = orderManager.getOrder(orderID);

            if (order == null || order.getChannel() == null) {
                Log.d("------HongShouZhi---------The order is null or the channel is null");
                this.renderState(false);
                return;
            }


            if (order.getState() > PayState.STATE_PAYING) {
                Log.d("------HongShouZhi---------The state of the order is complete  |  The state is " + order.getState());
                this.renderState(true);
                return;
            }


            if (!verifyPay(order.getChannel(), rsp)) {
                Log.d("------HongShouZhi---------The sign is not matched ");
                this.renderState(true);
                return;
            }


            if ("2".equals(rsp.getOrder_status())) {
                float money = Float.parseFloat(rsp.getMoney());
                order.setRealMoney((int) (money * 100));
                order.setSdkOrderTime(rsp.getPaytime());
                order.setCompleteTime(new Date());
                order.setChannelOrderID(rsp.getOrder_id());
                order.setState(PayState.STATE_SUC);
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(this.orderManager, order);
            } else {
                order.setChannelOrderID(rsp.getOrder_id());
                order.setState(PayState.STATE_FAILED);
                orderManager.saveOrder(order);
            }
            renderState(true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(e.getMessage());
        }


    }


    private void renderState(boolean suc) throws IOException {
        String res = "SUCCESS";
        if (!suc) {
            res = "FAILURE";
        }
        PrintWriter out = this.response.getWriter();
        out.write(res);
        out.flush();
    }


    //-=================================================================================================

    /***
     * 支付 验证
     * @param channel
     * @param rsp
     * @return
     */
    public boolean verifyPay(UChannel channel, PayResult rsp) {
        String signSource =
                "order_id=" + rsp.getOrder_id() +
                        "&mem_id=" + rsp.getMem_id() +
                        "&app_id=" + rsp.getApp_id() +
                        "&money=" + rsp.getMoney() +
                        "&order_status=" + rsp.getOrder_status() +
                        "&paytime=" + rsp.getPaytime() +
                        "&attach=" + rsp.getAttach() +
                        "&app_key=" + channel.getCpAppSecret();

        String sign = md5(signSource);
        return sign.equals(rsp.getSign());
    }

    public String md5(String name) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            byte[] buffers = md.digest(name.getBytes());
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < buffers.length; i++) {
                String s = toHexString(0xff & buffers[i]);
                if (s.length() == 1) {
                    sb.append("0" + s);
                }
                if (s.length() != 1) {
                    sb.append(s);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
