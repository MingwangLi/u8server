package com.u8.server.sdk.haowan;

public class HaoWanPayResult {
    private String serviceid;//游戏服务器名称
    private String roleid;//角色名称
    private String gameid;//游戏的AppId
    private String orderid;//CP订单号
    private String username;//登陆游戏的账号
    private String amount;//成功充值金额
    private String paytime;//玩家充值的时间
    private String productname;//商品描述
    private String channelorderid;//订单编号
    private String sign;//参数签名（用于验签对比）

    public String getServiceid() {
        return serviceid;
    }

    public void setServiceid(String serviceid) {
        this.serviceid = serviceid;
    }

    public String getRoleid() {
        return roleid;
    }

    public void setRoleid(String roleid) {
        this.roleid = roleid;
    }

    public String getGameid() {
        return gameid;
    }

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPaytime() {
        return paytime;
    }

    public void setPaytime(String paytime) {
        this.paytime = paytime;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getChannelorderid() {
        return channelorderid;
    }

    public void setChannelorderid(String channelorderid) {
        this.channelorderid = channelorderid;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
