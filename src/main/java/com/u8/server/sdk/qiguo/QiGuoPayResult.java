package com.u8.server.sdk.qiguo;

public class QiGuoPayResult {
    private String exorderno;           //七果生成的订单号
    private String transid;             //支付平台的交易流水号
    private String appid;               //七果对应用分配的标识id
    private int waresid;                //商品编码，默认为1
    private int feetype;                //计费方式，默认为0，消费型
    private int money;                  //本次交易的金额，单位：分
    private String count;               //本次交易中商品购买数量
    private int result;                 //交易结果，0：成功，1：失败
    private int transtype;              //交易类型，0：消费，1：充值
    private String transtime;           //交易时间，yyyy-mm-dd HH:ii:ss
    private String cpprivate;           //商户私有信息（可存商户订单号）
    private int paytype;                //支付方式，默认为0
    private String uid;                 //本次交易的用户

    public String getExorderno() {
        return exorderno;
    }

    public void setExorderno(String exorderno) {
        this.exorderno = exorderno;
    }

    public String getTransid() {
        return transid;
    }

    public void setTransid(String transid) {
        this.transid = transid;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public int getWaresid() {
        return waresid;
    }

    public void setWaresid(int waresid) {
        this.waresid = waresid;
    }

    public int getFeetype() {
        return feetype;
    }

    public void setFeetype(int feetype) {
        this.feetype = feetype;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getTranstype() {
        return transtype;
    }

    public void setTranstype(int transtype) {
        this.transtype = transtype;
    }

    public String getTranstime() {
        return transtime;
    }

    public void setTranstime(String transtime) {
        this.transtime = transtime;
    }

    public String getCpprivate() {
        return cpprivate;
    }

    public void setCpprivate(String cpprivate) {
        this.cpprivate = cpprivate;
    }

    public int getPaytype() {
        return paytype;
    }

    public void setPaytype(int paytype) {
        this.paytype = paytype;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
