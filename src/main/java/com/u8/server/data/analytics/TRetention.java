package com.u8.server.data.analytics;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by ant on 2016/8/17.
 */

@Entity
@Table(name = "tretention")
public class TRetention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer appID;          //AppID
    private String dayRetention;    //N日留存信息，记录30个， 比如5月1日这天新增的用户, 这里记录从5月1日到5月30日占当天登录的用户比例 50:100,20:100,50:100,...
    private String dayPayRatio;     //首N日付费率(付费转化)，记录30个，比如记录5月1日这天新增用户中在5月1日到5月30日中有充值行为的用户数占5月1日这天新增用户的比例 10:100,20:100,15:100,...

    private String dayFlowRatio;    //流失情况(统计3个，7日流失率；14日流失率；30日流失率) 30:100;10:100;50:100
    private String dayBackRatio;    //回归情况(统计3个，7日回归率;14日回归率；30日回归率) 30:100;10:100,50:100

    private Date statTime;          //统计日期
    private Date opTime;            //纪录生成时间

    private String ltv;           //ltv数据

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

    public Date getStatTime() {
        return statTime;
    }

    public void setStatTime(Date statTime) {
        this.statTime = statTime;
    }

    public String getDayRetention() {
        return dayRetention;
    }

    public void setDayRetention(String dayRetention) {
        this.dayRetention = dayRetention;
    }

    public Date getOpTime() {
        return opTime;
    }

    public void setOpTime(Date opTime) {
        this.opTime = opTime;
    }

    public String getDayPayRatio() {
        return dayPayRatio;
    }

    public void setDayPayRatio(String dayPayRatio) {
        this.dayPayRatio = dayPayRatio;
    }

    public String getDayFlowRatio() {
        return dayFlowRatio;
    }

    public void setDayFlowRatio(String dayFlowRatio) {
        this.dayFlowRatio = dayFlowRatio;
    }

    public String getDayBackRatio() {
        return dayBackRatio;
    }

    public void setDayBackRatio(String dayBackRatio) {
        this.dayBackRatio = dayBackRatio;
    }

    public String getLtv() {
        return ltv;
    }

    public void setLtv(String ltv) {
        this.ltv = ltv;
    }
}
