package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.StringUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 熊猫玩SDK支付回调类处理
 * Created by xiaohei on 16/11/27.
 */
@Controller
@Namespace("/pay/xmw")
public class XmwanPayCallbackAction extends UActionSupport {


    private String serial;
    private String amount;
    private String status;
    private String app_order_id;
    private String app_user_id;
    private String sign;
    private String app_subject;
    private String app_description;
    private String app_ext1;
    private String app_ext2;

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback(){
        try{

            long orderID = Long.parseLong(app_order_id);

            UOrder order = orderManager.getOrder(orderID);

            if(order == null){
                Log.d("The order is null");
                this.renderState(false);
                return;
            }

            UChannel channel = order.getChannel();
            if(channel == null){
                Log.d("the channel is null.");
                this.renderState(false);
                return;
            }

            if(order.getState() > PayState.STATE_PAYING) {
                Log.d("The state of the order is complete. The state is " + order.getState());
                this.renderState(true);
                return;
            }

            if(!"success".equals(status)){
                Log.d("the state from sd of order is not success.status:%s;orderID:%s",status,orderID);
                this.renderState(false);
                return;
            }

            if(!isSignOK(channel)){
                Log.d("The sign verify failed.sign:%s;appKey:%s;orderID:%s", sign, channel.getCpAppSecret(), orderID);
                this.renderState(false);
                return;
            }



            int moneyInt = Integer.valueOf(amount);

            order.setRealMoney(moneyInt*100);
            order.setSdkOrderTime("");
            order.setCompleteTime(new Date());
            order.setChannelOrderID(serial);
            order.setState(PayState.STATE_SUC);

            orderManager.saveOrder(order);

            SendAgent.sendCallbackToServer(this.orderManager, order);
            renderState(true);

        }catch(Exception e){
            try {
                renderState(false);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private boolean isSignOK(UChannel channel){

        Map<String,String> params = new HashMap<String,String>();
        params.put("serial", serial);
        params.put("amount", amount);
        params.put("status", status);
        params.put("app_order_id", app_order_id);
        params.put("app_user_id", app_user_id);
        params.put("app_subject", app_subject);
        params.put("app_description", app_description);
        params.put("app_ext1", app_ext1);
        params.put("app_ext2", app_ext2);

        String signStr = StringUtils.generateUrlSortedParamString(params, "&", true);
        signStr += "&client_secret="+channel.getCpAppSecret();

        String signLocal = EncryptUtils.md5(signStr).toLowerCase();

        Log.d("sign str : %s", signStr);
        Log.d("sign local : %s", signLocal);
        Log.d("sign : %s", sign);

        return signLocal.equals(this.sign);
    }

    private void renderState(boolean suc) throws IOException {

        if(suc){
            renderText("success");
        }else{
            renderText("fail");
        }

    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApp_order_id() {
        return app_order_id;
    }

    public void setApp_order_id(String app_order_id) {
        this.app_order_id = app_order_id;
    }

    public String getApp_user_id() {
        return app_user_id;
    }

    public void setApp_user_id(String app_user_id) {
        this.app_user_id = app_user_id;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getApp_subject() {
        return app_subject;
    }

    public void setApp_subject(String app_subject) {
        this.app_subject = app_subject;
    }

    public String getApp_description() {
        return app_description;
    }

    public void setApp_description(String app_description) {
        this.app_description = app_description;
    }

    public String getApp_ext1() {
        return app_ext1;
    }

    public void setApp_ext1(String app_ext1) {
        this.app_ext1 = app_ext1;
    }

    public String getApp_ext2() {
        return app_ext2;
    }

    public void setApp_ext2(String app_ext2) {
        this.app_ext2 = app_ext2;
    }
}
