package com.u8.server.sdk.nubia;

import java.math.BigDecimal;

/**
 * @Author: lizhong
 * @Des:
 * @Date: 2018/2/2 11:11
 * @Modified:
 */
public class NubiaPayResult {
    private String order_no;        //游戏订单号
    private String data_timestamp;  //支付通知时间
    private Integer pay_success;    //支付成功与否(成功为1)
    private String sign;            //支付通知签名
    private String app_id;          //应用ID
    private String uid;             //登录时获取的uid
    private BigDecimal amount;      //支付金额（精确到分）
    private String product_name;    //支付金额（精确到分）
    private String product_des;     //商品描述
    private Integer number;         //商品数量
    private Integer order_serial;   //Nubia支付的订单号
    private String order_sign;      //支付通知签名

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public String getData_timestamp() {
        return data_timestamp;
    }

    public void setData_timestamp(String data_timestamp) {
        this.data_timestamp = data_timestamp;
    }

    public Integer getPay_success() {
        return pay_success;
    }

    public void setPay_success(Integer pay_success) {
        this.pay_success = pay_success;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_des() {
        return product_des;
    }

    public void setProduct_des(String product_des) {
        this.product_des = product_des;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getOrder_serial() {
        return order_serial;
    }

    public void setOrder_serial(Integer order_serial) {
        this.order_serial = order_serial;
    }

    public String getOrder_sign() {
        return order_sign;
    }

    public void setOrder_sign(String order_sign) {
        this.order_sign = order_sign;
    }
}
