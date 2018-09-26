package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.HmacSHA1Encryption;
import com.u8.server.utils.StringUtils;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.http.util.TextUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 16 WIFI 支付回调处理类
 * Created by xiaohei on 16/11/20.
 */
@Controller
@Namespace("/pay/wifi16")
public class Wifi16PayCallbackAction extends UActionSupport{

    private String uid;
    private String payCode;
    private String orderId;
    private String verdorCode;
    private int status;
    private String cashAmount;
    private String sign;

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback(){

        try{

            long orderID = Long.parseLong(payCode);

            UOrder order = orderManager.getOrder(orderID);

            if(order == null || order.getChannel() == null){
                Log.d("The order is null or the channel is null.");
                this.renderState(false);
                return;
            }

            if(order.getState() > PayState.STATE_PAYING){
                Log.d("The state of the order is complete. The state is "+order.getState());
                this.renderState(false);
                return;
            }

            if(status != 9 && status != 11){
                Log.d("The returned state of the order is not success. The state is "+order.getState());
                this.renderState(false);
                return;
            }

            if(isValid(order.getChannel())){
                order.setRealMoney(Float.valueOf(cashAmount).intValue());
                order.setSdkOrderTime("");
                order.setCompleteTime(new Date());
                order.setChannelOrderID(this.orderId);
                order.setState(PayState.STATE_SUC);
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(this.orderManager, order);
                this.renderState(true);
            }else{
                order.setChannelOrderID(this.orderId);
                order.setState(PayState.STATE_FAILED);
                orderManager.saveOrder(order);
                this.renderState(false);
            }


        }catch (Exception e){
            e.printStackTrace();
            try {
                this.renderState(false);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    private boolean isValid(UChannel channel) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("uid",uid);
        params.put("payCode", payCode);
        params.put("orderId", orderId);
        params.put("verdorCode", verdorCode);
        params.put("status", status+"");
        params.put("cashAmount", cashAmount);
        params.put("sign", sign);

        String signConent = StringUtils.generateUrlSortedParamString(params, "&", true)+"key="+channel.getCpPayKey();

        String signLocal = EncryptUtils.md5(signConent).toUpperCase();


        return signLocal.equals(this.sign);
    }

    private void renderState(boolean suc) throws IOException {

        JSONObject json = new JSONObject();
        if (suc) {

            json.put("ReturnCode", 200);
        }else{
            json.put("ReturnCode", 201);
        }

        renderJson(json.toString());


    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPayCode() {
        return payCode;
    }

    public void setPayCode(String payCode) {
        this.payCode = payCode;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getVerdorCode() {
        return verdorCode;
    }

    public void setVerdorCode(String verdorCode) {
        this.verdorCode = verdorCode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(String cashAmount) {
        this.cashAmount = cashAmount;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
