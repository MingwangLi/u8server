package com.u8.server.data.analytics;

/**
 * Created by ant on 2016/8/29.
 */
public class ChannelGroupInfo {

    private int channelID;
    private String channelName;
    private long deviceNum;
    private long userNum;
    private long payUserNum;
    private double payMoney;

    public int getChannelID() {
        return channelID;
    }

    public void setChannelID(int channelID) {
        this.channelID = channelID;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

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

    public double getPayMoney() {
        return payMoney;
    }

    public void setPayMoney(double payMoney) {
        this.payMoney = payMoney;
    }
}
