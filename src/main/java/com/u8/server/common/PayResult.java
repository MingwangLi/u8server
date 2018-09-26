package com.u8.server.common;

/**
 * Created by ${lvxinmin} on 2016/11/25.
 */
public class PayResult {

    private String order_id;            //订单号
    private String mem_id;              //玩家ID
    private String app_id;              //游戏ID
    private String money;               //充值金额
    private String order_status;        //1 未支付  2成功支付 3ss支付失败
    private String paytime;             //时间戳
    private String attach;              //CP扩展参数,建议为英文与数字，CP用于校验此订单合法性
    private String sign;                //使用APP_KEY 对所有的参数md5加密串，用于与接口生成的验证串做比较，保证计费通知的合法性。

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
}
