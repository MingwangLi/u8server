package com.u8.server.sdk.moge;

/**
 * Created by lvxinmin on 2016/11/15.
 */
public class PayCallbackInfo {

    private String orderid;
    private String username;
    private int gameid;
    private String roleid;
    private int serverid;
    private String paytype;
    private int amount;
    private int paytime;
    private String attatch;
    private String appkey;
    private String sign;
    public String u8ChannelID;

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getGameid() {
        return gameid;
    }

    public void setGameid(int gameid) {
        this.gameid = gameid;
    }

    public String getRoleid() {
        return roleid;
    }

    public void setRoleid(String roleid) {
        this.roleid = roleid;
    }

    public int getServerid() {
        return serverid;
    }

    public void setServerid(int serverid) {
        this.serverid = serverid;
    }

    public String getPaytype() {
        return paytype;
    }

    public void setPaytype(String paytype) {
        this.paytype = paytype;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getPaytime() {
        return paytime;
    }

    public void setPaytime(int paytime) {
        this.paytime = paytime;
    }

    public String getAttatch() {
        return attatch;
    }

    public void setAttatch(String attatch) {
        this.attatch = attatch;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getU8ChannelID() {
        return u8ChannelID;
    }

    public void setU8ChannelID(String u8ChannelID) {
        this.u8ChannelID = u8ChannelID;
    }
}
