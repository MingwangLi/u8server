package com.u8.server.data.analytics;

import javax.persistence.*;
import java.util.Date;

/**
 * 渠道基本统计信息
 * Created by ant on 2016/8/22.
 */
@Entity
@Table(name = "tchannelsummary")
public class TChannelSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer appID;          //appID
    private Integer channelID;      //渠道ID
    private String channelName;     //渠道名称
    private Integer deviceNum;     //新增设备数量
    private Integer userNum;       //新增用户数量
    private Integer payUserNum;     //付费用户数量
    private Integer newPayUserNum; //新增付费用户数量(首冲)
    private Long money;             //收入，单位分
    private Date currTime;          //当前日期


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAppID() {
        return appID;
    }

    public void setAppID(Integer appID) {
        this.appID = appID;
    }

    public Integer getDeviceNum() {
        return deviceNum;
    }

    public void setDeviceNum(Integer deviceNum) {
        this.deviceNum = deviceNum;
    }

    public Integer getUserNum() {
        return userNum;
    }

    public void setUserNum(Integer userNum) {
        this.userNum = userNum;
    }

    public Integer getNewPayUserNum() {
        return newPayUserNum;
    }

    public void setNewPayUserNum(Integer newPayUserNum) {
        this.newPayUserNum = newPayUserNum;
    }

    public Long getMoney() {
        return money;
    }

    public void setMoney(Long money) {
        this.money = money;
    }

    public Date getCurrTime() {
        return currTime;
    }

    public void setCurrTime(Date currTime) {
        this.currTime = currTime;
    }

    public Integer getChannelID() {
        return channelID;
    }

    public void setChannelID(Integer channelID) {
        this.channelID = channelID;
    }

    public Integer getPayUserNum() {
        return payUserNum;
    }

    public void setPayUserNum(Integer payUserNum) {
        this.payUserNum = payUserNum;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}
