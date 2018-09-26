package com.u8.server.data.analytics;

import java.util.List;

/**
 * Created by ant on 2016/8/26.
 */
public class SummaryInfo {

    private long deviceNum;
    private long userNum;
    private long payUserNum;
    private long payMoney;

    private List<TSummary> summaryData;

    public long getDeviceNum() {
        return deviceNum;
    }

    public void setDeviceNum(long deviceNum) {
        this.deviceNum = deviceNum;
    }

    public long getUserNum() {
        return userNum;
    }

    public void setUserNum(long userNum) {
        this.userNum = userNum;
    }

    public long getPayUserNum() {
        return payUserNum;
    }

    public void setPayUserNum(long payUserNum) {
        this.payUserNum = payUserNum;
    }

    public long getPayMoney() {
        return payMoney;
    }

    public void setPayMoney(long payMoney) {
        this.payMoney = payMoney;
    }

    public List<TSummary> getSummaryData() {
        return summaryData;
    }

    public void setSummaryData(List<TSummary> summaryData) {
        this.summaryData = summaryData;
    }
}
