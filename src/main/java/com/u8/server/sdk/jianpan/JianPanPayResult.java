package com.u8.server.sdk.jianpan;

/**
 * Created by lvxinmin on 2016/11/10.
 */
public class JianPanPayResult {

    private String cpTradeNo;           //通行证及支付服务生成的订单号
    private int gameId;                 //游戏编号
    private String userId;              //用户编号
    private String roleId;              //角色编号
    private int serverId;               //游戏区号(客户端传过来)
    private String channelId;                 //渠道编号
    private String itemId;              //购买的物品编号
    private String itemAmount;          //发货数量，优先使用money字段发货
    private String privateField;        //应用自定义字段varchar(128) 保留字段，除非有特别说明，否则一律为空字符串
    private int money;                  //发货金额（分，值为-1时使用itemAmount字段发货
    private String currencyType;        //货币类型：CNY(人民币);USD(美元)；
    private Float fee;                  //实际支付金额;
    private String status;              //交易状态，0表示成功
    private String giftId;              //赠品编号
    private String sign;                //验证签名，privateKey接入时分配

    public String getCpTradeNo() {
        return cpTradeNo;
    }

    public void setCpTradeNo(String cpTradeNo) {
        this.cpTradeNo = cpTradeNo;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(String itemAmount) {
        this.itemAmount = itemAmount;
    }

    public String getPrivateField() {
        return privateField;
    }

    public void setPrivateField(String privateField) {
        this.privateField = privateField;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public Float getFee() {
        return fee;
    }

    public void setFee(Float fee) {
        this.fee = fee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGiftId() {
        return giftId;
    }

    public void setGiftId(String giftId) {
        this.giftId = giftId;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
