package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.jianpan.JianPanPayResult;
import com.u8.server.service.UChannelManager;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

/**
 * Created by lizhong on 2017/10/18.
 */
@Controller
@Namespace("/pay/hanfeng")
public class JianPanPayCallbackAction extends UActionSupport {
    @Autowired
    private UOrderManager orderManager;

    @Autowired
    private UChannelManager channelManager;

    @Action("payCallback")
    public void payCallback() {

        try {
            BufferedReader br = this.request.getReader();
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\r\n");
            }

            Log.d("------Jian Pan pay callBack | request params:" + sb.toString());
            JianPanPayResult data = (JianPanPayResult) JsonUtils.decodeJson(sb.toString(), JianPanPayResult.class);

            if (data == null) {
                Log.e("The content parse error...");
                this.renderState(false, "data error");
                return;
            }

            long localOrderID = Long.parseLong(data.getPrivateField());
            UOrder order = orderManager.getOrder(localOrderID);

            if (order == null || order.getChannel() == null) {
                Log.d("The order is null or the channel is null.");
                this.renderState(false, "notifyId 错误");
                return;
            }

            if (order.getState() > PayState.STATE_PAYING) {
                Log.d("The state of the order is complete. The state is " + order.getState());
                this.renderState(true, "该订单已经被处理,或者CP订单号重复");
                return;
            }

            if (!"0".equals(data.getStatus())) {
                Log.e("平台支付失败 local orderID:" + localOrderID + "; order id:" + data.getCpTradeNo());
                this.renderState(false, "支付中心返回的结果是支付失败");
                return;
            }

            order.setChannelOrderID(data.getCpTradeNo());
            order.setRealMoney((int) (data.getFee() * 100));
            order.setSdkOrderTime("");
            order.setCompleteTime(new Date());
                order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            this.renderState(true, data.getCpTradeNo());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean isSignOK(JianPanPayResult rsp, UChannel channel) throws UnsupportedEncodingException {
        //md5(cpTradeNo|gameId|userId|roleId|serverId|channelId|itemId|itemAmount|privateField|money|status|privateKey)
        StringBuilder sb = new StringBuilder();
        sb.append(rsp.getCpTradeNo()).append("|")
                .append(channel.getCpAppID()).append("|")
                .append(rsp.getUserId()).append("|")
                .append(rsp.getRoleId()).append("|")
                .append(rsp.getServerId()).append("|")
                .append(rsp.getChannelId()).append("|")
                .append(rsp.getItemId()).append("|")
                .append(rsp.getItemAmount()).append("|")
                .append(rsp.getPrivateField()).append("|")
                .append(rsp.getMoney()).append("|")
                .append(rsp.getStatus()).append("|")
                .append(channel.getCpAppKey());
        return  EncryptUtils.md5(sb.toString()).equals(rsp.getSign());
    }

    private void renderState(boolean suc, String msg) throws IOException {
        PrintWriter out = this.response.getWriter();
        if (suc) {
            out.write(msg);
        } else {
            out.write("-1");
        }
        out.flush();
    }

}
