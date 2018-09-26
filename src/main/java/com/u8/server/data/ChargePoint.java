package com.u8.server.data;

import net.sf.json.JSONObject;

import javax.persistence.*;
import java.util.Date;

/**
 * @Author: lizhong
 * @Des:
 * @Date: 2018/5/7 15:20
 * @Modified:
 */
@Entity
@Table(name="charge_point")
public class ChargePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("channelID",channelID);
        json.put("money", money);
        json.put("channelChargeCode", channelChargeCode);
        json.put("chargeCode", chargeCode);
        json.put("chargeName", chargeName);
        json.put("chargeDesc", chargeDesc);
        json.put("createTime", createTime);
        json.put("updateTime", updateTime);
        json.put("createBy", createBy);
        json.put("status", status);
        return json;
    }

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
}
