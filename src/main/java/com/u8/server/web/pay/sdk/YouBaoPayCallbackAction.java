package com.u8.server.web.pay.sdk;


import com.u8.server.common.PayResult;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
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
import java.util.Date;

/**
 * 游宝---支付回调
 * @Author: lz
 * @Date: 2016/12/19 18:08.
 */
@SuppressWarnings("all")
@Controller
@Namespace("/pay/youbao")
public class YouBaoPayCallbackAction extends UActionSupport {
    @Autowired
    private UOrderManager orderManager;


    @Action("payCallback")
    public void payCallback() {
        try{
            BufferedReader br = this.request.getReader();
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\r\n");
            }

            String str = sb.toString();

            Log.d("----YouBao pay callBack | request params:" + str);
            PayResult rsp = (PayResult) JsonUtils.decodeJson(str, PayResult.class);


            if(rsp == null){
                this.renderState(false);
                return;
            }

            /*if(!"2".equals(rsp.getOrder_status())){
                Log.e("The order %s sdk returned status is %s, not success.", rsp.getAttach(), rsp.getOrder_status());
                this.renderState(false);
                return;
            }*/

            long orderID = Long.parseLong(rsp.getAttach());

            UOrder order = orderManager.getOrder(orderID);

            if(order == null){
                Log.d("The order is null. orderID:%s", rsp.getAttach());
                this.renderState(false);
                return;
            }

            UChannel channel = order.getChannel();
            if(channel == null){
                Log.d("the channel is null. orderID:%s", rsp.getAttach());
                this.renderState(false);
                return;
            }

            if(order.getState() > PayState.STATE_PAYING) {
                Log.d("The state of the order is complete. orderID:%s;state:%s", orderID, order.getState());
                this.renderState(true);
                return;
            }

            if(!isSignOK(channel, rsp)){
                Log.d("The sign verify failed.sign:%s;appKey:%s;orderID:%s", rsp.getSign(), channel.getCpAppKey(), orderID);
                this.renderState(false);
                return;
            }

            int moneyInt = (int)(Float.valueOf(rsp.getMoney()) * 100);
            order.setRealMoney(moneyInt);
            order.setSdkOrderTime(rsp.getPaytime());
            order.setCompleteTime(new Date());
            order.setChannelOrderID(rsp.getOrder_id());
            order.setState(PayState.STATE_SUC);

            orderManager.saveOrder(order);

            SendAgent.sendCallbackToServer(this.orderManager, order);
            renderState(true);

        }catch(Exception e){
            try {
                renderState(false);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }
    //  支付验证
    private boolean isSignOK(UChannel channel, PayResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("order_id=").append(result.getOrder_id())
                .append("&mem_id=").append(result.getMem_id())
                .append("&app_id=").append(result.getApp_id())
                .append("&money=").append(result.getMoney())
                .append("&order_status=").append(result.getOrder_status())
                .append("&paytime=").append(result.getPaytime())
                .append("&attach=").append(result.getAttach())
                .append("&app_key=").append(channel.getCpAppKey());
        return result.getSign().equals(EncryptUtils.md5(sb.toString()).toLowerCase());
    }
    private void renderState(boolean suc) {
        String res = "SUCCESS";
        if (!suc) {
            res = "FAILURE";
        }
        PrintWriter out = null;
        try {
            out = this.response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.write(res);
        out.flush();
    }
}
