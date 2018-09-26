package com.u8.server.sdk.wansdk;

/**
 * @Author: lizhong
 * @Des:
 * @Date: 2018/3/22 10:00
 * @Modified:
 */
public class YQWBPayResult {
    private String tradeno;     //一起玩吧生成的交易流水号
    private String cptradeno;   //合作平台的交易流水号
    private String appid;      //一起玩吧对应用分配的标识ID
    private String paytype;    //支付方式，默认为0
    private String num;        //本次交易中购买的商品数量
    private String money;      //本次交易用户所支付的总金额，单位：分
    private String result;     //交易结果，0：成功，1：失败
    private String tradetype;  //交易类型，0：消费，1：充值
    private String tradetime;   //交易时间，YYYY-mm-dd HH:ii:ss
    private String remark;      //交易备注，默认值[17wanba]
    private String uid;        //本次交易的用户标识ID

    public String getTradeno() {
        return tradeno;
    }

    public void setTradeno(String tradeno) {
        this.tradeno = tradeno;
    }

    public String getCptradeno() {
        return cptradeno;
    }

    public void setCptradeno(String cptradeno) {
        this.cptradeno = cptradeno;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getPaytype() {
        return paytype;
    }

    public void setPaytype(String paytype) {
        this.paytype = paytype;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTradetype() {
        return tradetype;
    }

    public void setTradetype(String tradetype) {
        this.tradetype = tradetype;
    }

    public String getTradetime() {
        return tradetime;
    }

    public void setTradetime(String tradetime) {
        this.tradetime = tradetime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "YQWBPayResult{" +
                "tradeno='" + tradeno + '\'' +
                ", cptradeno='" + cptradeno + '\'' +
                ", appid='" + appid + '\'' +
                ", paytype='" + paytype + '\'' +
                ", num='" + num + '\'' +
                ", money='" + money + '\'' +
                ", result='" + result + '\'' +
                ", tradetype='" + tradetype + '\'' +
                ", tradetime='" + tradetime + '\'' +
                ", remark='" + remark + '\'' +
                ", uid='" + uid + '\'' +
                '}';
    }
}
