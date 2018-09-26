package com.u8.server.data;

import javax.persistence.*;
import java.util.Date;

/**
 * 设备信息
 * 应用首次启动记录玩家设备信息
 * Created by ant on 2016/8/12.
 */
@Entity
@Table(name = "udevice")
public class UDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String deviceID;        //唯一设备号
    private Integer appID;          //对应游戏ID
    private Integer channelID;      //渠道ID
    private String mac;             //mac地址
    private String deviceType;      //机型
    private Integer deviceOS;       //系统类型， 1:Android;2:iOS
    private String deviceDpi;       //分辨率
    private String area;            //地区
    private String ip;              //IP地址
    private Date createTime;        //记录时间

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getDeviceOS() {
        return deviceOS;
    }

    public void setDeviceOS(Integer deviceOS) {
        this.deviceOS = deviceOS;
    }

    public String getDeviceDpi() {
        return deviceDpi;
    }

    public void setDeviceDpi(String deviceDpi) {
        this.deviceDpi = deviceDpi;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getAppID() {
        return appID;
    }

    public void setAppID(Integer appID) {
        this.appID = appID;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getChannelID() {
        return channelID;
    }

    public void setChannelID(Integer channelID) {
        this.channelID = channelID;
    }
}
