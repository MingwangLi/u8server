package com.u8.server.sdk.xiao7game;

/**
 * Created by lvxinmin on 2016/11/14.
 */
public class PayCallbackResponse {
    private String encryp_data;//加密数据
    private String game_orderid;//游戏订单号
    private String guid;//游戏账号在小 7 平台中的唯一标识符
    private String subject;//商品名称
    private String xiao7_goid;//小七平台订单号唯一标示

    private String sign_data;//签名数据

    public String getEncryp_data() {
        return encryp_data;
    }

    public void setEncryp_data(String encryp_data) {
        this.encryp_data = encryp_data;
    }

    public String getGame_orderid() {
        return game_orderid;
    }

    public void setGame_orderid(String game_orderid) {
        this.game_orderid = game_orderid;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getXiao7_goid() {
        return xiao7_goid;
    }

    public void setXiao7_goid(String xiao7_goid) {
        this.xiao7_goid = xiao7_goid;
    }

    public String getSign_data() {
        return sign_data;
    }

    public void setSign_data(String sign_data) {
        this.sign_data = sign_data;
    }
}
