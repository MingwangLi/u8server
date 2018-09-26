package com.u8.server.sdk.jinghong;

public class JingHongPayResult {
    private String goodname;//商品名
    private String goodprice;//商品价格
    private String ordernumber;//订单号
    private String userid;//用户id
    private String gameorder;//游戏订单(自定义参数)
    private String sign;//验签签名
    private String app_key;//游戏厂商申请所得

    public String getGoodname() {
        return goodname;
    }

    public void setGoodname(String goodname) {
        this.goodname = goodname;
    }

    public String getGoodprice() {
        return goodprice;
    }

    public void setGoodprice(String goodprice) {
        this.goodprice = goodprice;
    }

    public String getOrdernumber() {
        return ordernumber;
    }

    public void setOrdernumber(String ordernumber) {
        this.ordernumber = ordernumber;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getGameorder() {
        return gameorder;
    }

    public void setGameorder(String gameorder) {
        this.gameorder = gameorder;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
    }
}
