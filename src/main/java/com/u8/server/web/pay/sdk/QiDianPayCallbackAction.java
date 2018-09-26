package com.u8.server.web.pay.sdk;

import com.opensymphony.xwork2.ModelDriven;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.qidian.QiDianPayResult;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Date;

/**
 * Author: lizhong
 * Date: 2017/12/14.
 * 奇点 支付回调
 * */
@Controller
@Namespace("/pay/qidian")
public class QiDianPayCallbackAction extends UActionSupport implements ModelDriven<QiDianPayResult>{
    private QiDianPayResult rsp = new QiDianPayResult();
    @Autowired
    private UOrderManager orderManager;
    @Action("payCallback")
    public void payCallback() throws Exception {
            if (rsp == null) {
                Log.e("----奇点---pay callBack message is null");
                this.renderState(false);
                return;
            }
            long orderID = Long.parseLong(rsp.getCp_order_id());
            UOrder order = orderManager.getOrder(orderID);

            if (order == null || order.getChannel() == null) {
                Log.d("----奇点---The order is null or the channel is null");
                this.renderState(false);
                return;
            }
            if (order.getState() > PayState.STATE_PAYING) {
                Log.d("----奇点---The state of the order is complete |  The state is " + order.getState());
                this.renderState(true);
                return;
            }
            if (!verifyPay(order.getChannel(), rsp)) {
                Log.d("----奇点---The sign is not matched");
                this.renderState(false);
                return;
            }
            if ("2".equals(rsp.getOrder_status())) {
                float money = Float.parseFloat(rsp.getProduct_price());
                order.setRealMoney((int) money * 100);
                order.setSdkOrderTime(rsp.getPay_time());
                order.setCompleteTime(new Date());
                order.setChannelOrderID(rsp.getOrder_id());
                order.setState(PayState.STATE_SUC);
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(this.orderManager, order);
                renderState(true);
            } else {
                order.setChannelOrderID(rsp.getOrder_id());
                order.setState(PayState.STATE_FAILED);
                orderManager.saveOrder(order);
                renderState(false);
            }

    }
    /***
     * 支付 验证
     * @param channel
     * @param rsp
     * @return
     */
    public boolean verifyPay(UChannel channel, QiDianPayResult rsp) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("app_id=").append(channel.getCpAppID())
                .append("&cp_order_id=").append(URLEncoder.encode(rsp.getCp_order_id(),"UTF-8"))
                .append("&mem_id=").append(rsp.getMem_id())
                .append("&order_id=").append(rsp.getOrder_id())
                .append("&order_status=").append(rsp.getOrder_status())
                .append("&pay_time=").append(rsp.getPay_time())
                .append("&product_id=").append(URLEncoder.encode(rsp.getProduct_id(),"UTF-8"))
                .append("&product_name=").append(URLEncoder.encode(rsp.getProduct_name(),"UTF-8"))
                .append("&product_price=").append(URLEncoder.encode(rsp.getProduct_price(),"UTF-8"))
                .append("&app_key=").append(channel.getCpAppKey());
        return rsp.getSign().equals(EncryptUtils.md5(sb.toString()));
    }

    @Override
    public QiDianPayResult getModel() {
        if(rsp == null){
            rsp = new QiDianPayResult();
        }
        return rsp;
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
            out.flush();
            out.close();
        }
    }
}

