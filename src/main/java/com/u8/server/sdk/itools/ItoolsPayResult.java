package com.u8.server.sdk.itools;

/**
 * @Author: lizhong
 * @Des:
 * @Date: 2018/3/23 10:09
 * @Modified:
 */
public class ItoolsPayResult {
    private String order_id_com;
    private String user_id;
    private String amount;
    private String account;
    private String order_id;
    private String result;

    public String getOrder_id_com() {
        return order_id_com;
    }

    public void setOrder_id_com(String order_id_com) {
        this.order_id_com = order_id_com;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ItoolsPayResult{" +
                "order_id_com='" + order_id_com + '\'' +
                ", user_id='" + user_id + '\'' +
                ", amount='" + amount + '\'' +
                ", account='" + account + '\'' +
                ", order_id='" + order_id + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
