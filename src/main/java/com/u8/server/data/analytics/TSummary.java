package com.u8.server.data.analytics;

import com.u8.server.cache.CacheManager;
import com.u8.server.data.UGame;
import com.u8.server.utils.TimeUtils;
import net.sf.json.JSONObject;

import javax.persistence.*;
import java.util.Date;

/**
 * 统计总揽表,以天为单位，每天一条记录
 * 存储过程凌晨执行，统计昨天一天的情况，插入一条新记录
 * Created by ant on 2016/8/16.
 */
@Entity
@Table(name = "tsummary")
public class TSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer appID;              //appID
    private Integer deviceNum;          //新增设备数量
    private Integer userNum;            //新增用户数量
    private Integer uniUserNum;         //新增用户数量(同一个设备ID的多个账户只算一个)
    private Long totalUserNum;          //截至当日，总用户数量

    private Integer dau;                //今日登录过的不重复用户数量(DAU)
    private Integer ndau;               //今日新用户带来的活跃（NDAU）
    private Integer wau;                //七日内登录过的不重复用户数量(WAU)
    private Integer mau;                //一个月内登录过的不重复用户数量(MAU)
    private Integer avg;                //每个玩家每天平均在线时长
    private Integer payUserNum;         //今日付费用户数量
    private Long totalPayUserNum;       //截至到当日，总付费用户数量(排重)
    private Integer newPayUserNum;      //新增付费用户数量(首冲)

    private Long money;             //收入，单位分
    private Date currTime;          //当前日期

    private Double arppu;   //付费用户平均值

    private Double arpu;  //注册用户平均值

    private Double payRate; //注册用户付费率

    public JSONObject toJSON(){

        JSONObject json = new JSONObject();
        json.put("id", id);
        UGame game = getGame();
        json.put("appName", game == null ? "" : game.getName());
        json.put("deviceNum", deviceNum);
        json.put("userNum", userNum);
        json.put("uniUserNum", uniUserNum);
        json.put("totalUserNum", totalUserNum);
        json.put("dau", dau);
        json.put("ndau", ndau);
        json.put("wau", wau);
        json.put("mau", mau);
        json.put("avg", avg);
        json.put("payUserNum", payUserNum);
        json.put("totalPayUserNum", totalPayUserNum);
        json.put("newPayUserNum", newPayUserNum);
        json.put("money", String.format("%.2f", money/100f));
        json.put("currTime", TimeUtils.format(currTime, TimeUtils.FORMATER_7));
        json.put("arppu",arppu+"");
        json.put("arpu",arpu+"");
        json.put("payRate",payRate+"");
        return json;
    }


    public UGame getGame() {
        return CacheManager.getInstance().getGame(this.appID);
    }

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

    public Integer getPayUserNum() {
        return payUserNum;
    }

    public void setPayUserNum(Integer payUserNum) {
        this.payUserNum = payUserNum;
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

    public Integer getDau() {
        return dau;
    }

    public void setDau(Integer dau) {
        this.dau = dau;
    }

    public Integer getNdau() {
        return ndau;
    }

    public void setNdau(Integer ndau) {
        this.ndau = ndau;
    }

    public Integer getWau() {
        return wau;
    }

    public void setWau(Integer wau) {
        this.wau = wau;
    }

    public Integer getMau() {
        return mau;
    }

    public void setMau(Integer mau) {
        this.mau = mau;
    }

    public Integer getAvg() {
        return avg;
    }

    public void setAvg(Integer avg) {
        this.avg = avg;
    }

    public Integer getNewPayUserNum() {
        return newPayUserNum;
    }

    public void setNewPayUserNum(Integer newPayUserNum) {
        this.newPayUserNum = newPayUserNum;
    }

    public Long getTotalUserNum() {
        return totalUserNum;
    }

    public void setTotalUserNum(Long totalUserNum) {
        this.totalUserNum = totalUserNum;
    }

    public Long getTotalPayUserNum() {
        return totalPayUserNum;
    }

    public void setTotalPayUserNum(Long totalPayUserNum) {
        this.totalPayUserNum = totalPayUserNum;
    }

    public Integer getUniUserNum() {
        return uniUserNum;
    }

    public void setUniUserNum(Integer uniUserNum) {
        this.uniUserNum = uniUserNum;
    }


    public Double getArppu() {
        return arppu;
    }

    public void setArppu(Double arppu) {
        this.arppu = arppu;
    }

    public Double getArpu() {
        return arpu;
    }

    public void setArpu(Double arpu) {
        this.arpu = arpu;
    }

    public Double getPayRate() {
        return payRate;
    }

    public void setPayRate(Double payRate) {
        this.payRate = payRate;
    }
}
