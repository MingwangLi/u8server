package com.u8.server.web.pay.sdk;

import com.nox.NoxConstant;
import com.nox.entity.KSPayResponseEntity;
import com.nox.notify.NotifyPayResult;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.UHttpAgent;
import com.u8.server.sdk.uc.PayCallbackResponse;
import com.u8.server.sdk.uc.UCSDK;
import com.u8.server.service.UChannelManager;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import com.u8.server.utils.TimeUtils;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 朋友玩支付回调处理
 * Created by ant on 2016/10/17.
 */
@Controller
@Namespace("/pay/pengyouwan")
public class PengYouWanCallbackAction extends UActionSupport {

    @Autowired
    private UOrderManager orderManager;


    @Action("payCallback")
    public void payCallback(){

        try{

            String data = "";
            Enumeration<String> names = this.request.getParameterNames();
            for(Enumeration e = names; e.hasMoreElements();){
                String name = e.nextElement().toString();
                if(name.equals("u8ChannelID")){
                    continue;
                }
                data = name;
                break;
            }

            Log.d("the data from pengyouwan post json:");
            Log.d(data);

            PYWPayCallbackData rsp = (PYWPayCallbackData) JsonUtils.decodeJson(data, PYWPayCallbackData.class);

            if(rsp == null){
                this.renderState(false);
                return;
            }

            long orderID = Long.parseLong(rsp.getCp_orderid());
            UOrder order = orderManager.getOrder(orderID);

            if(order == null || order.getChannel() == null){
                Log.d("The order is null or the channel is null.");
                this.renderState(false);
                return;
            }

            if(order.getState() > PayState.STATE_PAYING){
                Log.d("The state of the order is complete. The state is " + order.getState());
                this.renderState(true);
                return;
            }

            if(!isSignOK(order.getChannel(), rsp)){
                Log.d("The sign is not matched.");
                this.renderState(false);
                return;
            }

            float money = Float.parseFloat(rsp.getAmount());
            int moneyInt = (int)(money * 100);  //以分为单位

            order.setRealMoney(moneyInt);
            order.setSdkOrderTime("");
            order.setCompleteTime(new Date());
            order.setChannelOrderID(rsp.getCh_orderid());
            order.setState(PayState.STATE_SUC);

            orderManager.saveOrder(order);

            SendAgent.sendCallbackToServer(this.orderManager, order);


            renderState(true);

        }catch (Exception e){
            e.printStackTrace();
            try{
                this.renderState(false);
            }catch (Exception e2){
                e2.printStackTrace();
                Log.e(e2.getMessage());
            }

            Log.e(e.getMessage());

        }

    }

    private boolean isSignOK(UChannel channel, PYWPayCallbackData data){
        StringBuilder sb = new StringBuilder();
        sb.append(channel.getCpAppSecret()).append(data.getCp_orderid())
                .append(data.getCh_orderid()).append(data.getAmount());

        Log.d("pengyouwan check sign md5 str:");
        Log.d(sb.toString());

        String md5Local = EncryptUtils.md5(sb.toString());

        return md5Local.toLowerCase().equals(data.getSign());
    }

    private void renderState(boolean suc) throws IOException{

        String res = "SUCCESS";
        if(!suc){
            res = "FAILURE";
        }

        PrintWriter out = this.response.getWriter();
        out.write(res);
        out.flush();
    }

    static class PYWPayCallbackData {
        private String ver;
        private String tid;
        private String sign;
        private String gamekey;
        private String channel;
        private String cp_orderid;
        private String ch_orderid;
        private String amount;//元
        private String cp_param;

        public String getVer() {
            return ver;
        }

        public void setVer(String ver) {
            this.ver = ver;
        }

        public String getTid() {
            return tid;
        }

        public void setTid(String tid) {
            this.tid = tid;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getGamekey() {
            return gamekey;
        }

        public void setGamekey(String gamekey) {
            this.gamekey = gamekey;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getCp_orderid() {
            return cp_orderid;
        }

        public void setCp_orderid(String cp_orderid) {
            this.cp_orderid = cp_orderid;
        }

        public String getCh_orderid() {
            return ch_orderid;
        }

        public void setCh_orderid(String ch_orderid) {
            this.ch_orderid = ch_orderid;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getCp_param() {
            return cp_param;
        }

        public void setCp_param(String cp_param) {
            this.cp_param = cp_param;
        }
    }
}
