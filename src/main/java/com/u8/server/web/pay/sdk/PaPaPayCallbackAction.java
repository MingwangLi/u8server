package com.u8.server.web.pay.sdk;

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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;

/**
 * 啪啪游戏厅支付回调处理类
 * Created by ant on 2016/11/9.
 */
@Controller
@Namespace("/pay/papa")
public class PaPaPayCallbackAction extends UActionSupport{

    private String app_key         ;    //商户标识 Y
    private String app_order_id    ;    //游戏方内部订单号 Y
    private String app_district    ;    //区标识(数字) Y
    private String app_server      ;    //服标识(数字) Y
    private String app_user_id     ;    //用户角色编号 Y
    private String app_user_name   ;    //用户角色名称 Y
    private String product_id      ;    //购买产品编号 Y
    private String product_name    ;    //购买产品名称 Y
    private String money_amount    ;    //金额(单元:元) Y
    private String pa_open_uid     ;    //登录后返回的用户编号 Y
    private String app_extra1      ;    //附加信息(回调时原样返回) Y
    private String app_extra2      ;    //附加信息(回调时原样返回) Y
    private String pa_open_order_id;    //啪啪订单号 Y
    private String sign            ;    //签名 N


    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback(){

        try{

            long orderID = Long.parseLong(app_order_id);
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

            if(!isSignOK(order.getChannel())){
                Log.d("The sign is not matched.");
                this.renderState(false);
                return;
            }

            float money = Float.parseFloat(money_amount);
            int moneyInt = (int)(money * 100);  //以分为单位

            order.setRealMoney(moneyInt);
            order.setSdkOrderTime("");
            order.setCompleteTime(new Date());
            order.setChannelOrderID(pa_open_order_id);
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

    private boolean isSignOK(UChannel channel){
        StringBuilder sb = new StringBuilder();
        sb.append(channel.getCpAppKey()).append(channel.getCpAppSecret());
        sb.append("app_district=").append(app_district).append("&")
                .append("app_extra1=").append(app_extra1).append("&")
                .append("app_extra2=").append(app_extra2).append("&")
                .append("app_key=").append(app_key).append("&")
                .append("app_order_id=").append(app_order_id).append("&")
                .append("app_server=").append(app_server).append("&")
                .append("app_user_id=").append(app_user_id).append("&")
                .append("app_user_name=").append(app_user_name).append("&")
                .append("money_amount=").append(money_amount).append("&")
                .append("pa_open_order_id=").append(pa_open_order_id).append("&")
                .append("pa_open_uid=").append(pa_open_uid).append("&")
                .append("product_id=").append(product_id).append("&")
                .append("product_name=").append(product_name);

        Log.d("papa pay sign check sign md5 str:");
        Log.d(sb.toString());

        String md5Local = EncryptUtils.md5(sb.toString());

        return md5Local.toLowerCase().equals(sign);
    }

    private void renderState(boolean suc) throws IOException {

        String res = "ok";
        if(!suc){
            res = "no";
        }

        renderText(res);
    }

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
    }

    public String getApp_order_id() {
        return app_order_id;
    }

    public void setApp_order_id(String app_order_id) {
        this.app_order_id = app_order_id;
    }

    public String getApp_district() {
        return app_district;
    }

    public void setApp_district(String app_district) {
        this.app_district = app_district;
    }

    public String getApp_server() {
        return app_server;
    }

    public void setApp_server(String app_server) {
        this.app_server = app_server;
    }

    public String getApp_user_id() {
        return app_user_id;
    }

    public void setApp_user_id(String app_user_id) {
        this.app_user_id = app_user_id;
    }

    public String getApp_user_name() {
        return app_user_name;
    }

    public void setApp_user_name(String app_user_name) {
        this.app_user_name = app_user_name;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getMoney_amount() {
        return money_amount;
    }

    public void setMoney_amount(String money_amount) {
        this.money_amount = money_amount;
    }

    public String getPa_open_uid() {
        return pa_open_uid;
    }

    public void setPa_open_uid(String pa_open_uid) {
        this.pa_open_uid = pa_open_uid;
    }

    public String getApp_extra1() {
        return app_extra1;
    }

    public void setApp_extra1(String app_extra1) {
        this.app_extra1 = app_extra1;
    }

    public String getApp_extra2() {
        return app_extra2;
    }

    public void setApp_extra2(String app_extra2) {
        this.app_extra2 = app_extra2;
    }

    public String getPa_open_order_id() {
        return pa_open_order_id;
    }

    public void setPa_open_order_id(String pa_open_order_id) {
        this.pa_open_order_id = pa_open_order_id;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
