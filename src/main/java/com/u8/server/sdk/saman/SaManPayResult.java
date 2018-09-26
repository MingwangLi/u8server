package com.u8.server.sdk.saman;

/**
 * Created by ${lvxinmin} on 2016/12/9.
 */
public class SaManPayResult {
    private String uid;
    private int sid;
    private String cp_order_no;
    private String platform_order_no;
    private float origin_money;
    private float Money;
    private String Sign;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public String getCp_order_no() {
        return cp_order_no;
    }

    public void setCp_order_no(String cp_order_no) {
        this.cp_order_no = cp_order_no;
    }

    public String getPlatform_order_no() {
        return platform_order_no;
    }

    public void setPlatform_order_no(String platform_order_no) {
        this.platform_order_no = platform_order_no;
    }

    public float getOrigin_money() {
        return origin_money;
    }

    public void setOrigin_money(float origin_money) {
        this.origin_money = origin_money;
    }

    public float getMoney() {
        return Money;
    }

    public void setMoney(float money) {
        Money = money;
    }

    public String getSign() {
        return Sign;
    }

    public void setSign(String sign) {
        Sign = sign;
    }
}
