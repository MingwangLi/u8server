package com.u8.server.sdk.yiliu;

/**
 * 天启（16游戏） 回调结果
 * Author: lizhong
 * Date: 2017/12/7.
 * Version: 火速V7.2
 */
public class YiLiuPayResult {
    private String order_id;
    private String mem_id;
    private String app_id;
    private String money;
    private String order_status;
    private String paytime;
    private String attach;
    private String sign;

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getMem_id() {
        return mem_id;
    }

    public void setMem_id(String mem_id) {
        this.mem_id = mem_id;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getPaytime() {
        return paytime;
    }

    public void setPaytime(String paytime) {
        this.paytime = paytime;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "YiLiuPayResult{" +
                "order_id='" + order_id + '\'' +
                ", mem_id='" + mem_id + '\'' +
                ", app_id='" + app_id + '\'' +
                ", money='" + money + '\'' +
                ", order_status='" + order_status + '\'' +
                ", paytime='" + paytime + '\'' +
                ", attach='" + attach + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
