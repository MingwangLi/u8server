package com.u8.server.data;

import net.sf.json.JSONObject;
import java.util.Date;

/**
 * @Author: lizhong
 * @Des:
 * @Date: 2018/5/7 15:20
 * @Modified:
 */
public class ChargePointVO {


    private Integer id;
    private Integer channelID;
    private Integer money;
    private String chargeCode;
    private String channelChargeCode;
    private String chargeName;
    private String chargeDesc;
    private Date createTime;
    private Date updateTime;
    private String createBy;
    private Integer status;
    private String ganme;
    private String channelMaster;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getChannelID() {
        return channelID;
    }

    public void setChannelID(Integer channelID) {
        this.channelID = channelID;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public String getChargeCode() {
        return chargeCode;
    }

    public void setChargeCode(String chargeCode) {
        this.chargeCode = chargeCode;
    }

    public String getChannelChargeCode() {
        return channelChargeCode;
    }

    public void setChannelChargeCode(String channelChargeCode) {
        this.channelChargeCode = channelChargeCode;
    }

    public String getChargeName() {
        return chargeName;
    }

    public void setChargeName(String chargeName) {
        this.chargeName = chargeName;
    }

    public String getChargeDesc() {
        return chargeDesc;
    }

    public void setChargeDesc(String chargeDesc) {
        this.chargeDesc = chargeDesc;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getGanme() {
        return ganme;
    }

    public void setGanme(String ganme) {
        this.ganme = ganme;
    }

    public String getChannelMaster() {
        return channelMaster;
    }

    public void setChannelMaster(String channelMaster) {
        this.channelMaster = channelMaster;
    }
}
