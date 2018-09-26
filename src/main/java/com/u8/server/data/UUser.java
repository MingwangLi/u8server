package com.u8.server.data;

import com.u8.server.cache.CacheManager;
import com.u8.server.utils.TimeUtils;
import net.sf.json.JSONObject;

import javax.persistence.*;
import java.util.Date;

/**
 * 用户数据对象
 */

@Entity
@Table(name = "uuser")
public class UUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int appID;
    private int channelID;
    private String name;
    private String channelUserID;
    private String channelUserName;
    private String channelUserNick;
    private Date createTime;
    private Date lastLoginTime;
    private String token;
    private Integer firstCharge;        //是否充值过(没有：0；充值过：1)
    private Date firstChargeTime;       //首冲时间
    private String deviceID;            //设备ID
    private Integer isCreatedRole;    //用户是否创建了角色  1：创建了角色  0：没有  默认0
    private Integer status;          //用户是否可以登陆成功  1：可以登陆成功   0：不可以登陆成功   默认1
    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("appID", appID);
        UGame game = getGame();
        json.put("appName", game == null? "": game.getName());
        json.put("channelID", channelID);
        UChannel channel = getChannel();
        json.put("channelName", channel == null ? "":channel.getMaster().getMasterName());
        json.put("name", name);
        json.put("channelUserID", channelUserID);
        json.put("channelUserName", channelUserName);
        json.put("channelUserNick", channelUserNick);
        json.put("createTime", TimeUtils.format_default(createTime));
        json.put("lastLoginTime", TimeUtils.format_default(lastLoginTime));
        json.put("isCreatedRole", isCreatedRole);
        json.put("status",status);
        return json;
    }

    public UChannel getChannel(){
        return CacheManager.getInstance().getChannel(this.channelID);
    }

    public UGame getGame(){
        return CacheManager.getInstance().getGame(this.appID);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public int getChannelID() {
        return channelID;
    }

    public void setChannelID(int channelID) {
        this.channelID = channelID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChannelUserID() {
        return channelUserID;
    }

    public void setChannelUserID(String channelUserID) {
        this.channelUserID = channelUserID;
    }

    public String getChannelUserName() {
        return channelUserName;
    }

    public void setChannelUserName(String channelUserName) {
        this.channelUserName = channelUserName;
    }

    public String getChannelUserNick() {
        return channelUserNick;
    }

    public void setChannelUserNick(String channelUserNick) {
        this.channelUserNick = channelUserNick;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getFirstCharge() {
        return firstCharge;
    }

    public void setFirstCharge(Integer firstCharge) {
        this.firstCharge = firstCharge;
    }

    public Date getFirstChargeTime() {
        return firstChargeTime;
    }

    public void setFirstChargeTime(Date firstChargeTime) {
        this.firstChargeTime = firstChargeTime;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public Integer getIsCreatedRole() {
        return isCreatedRole;
    }

    public void setIsCreatedRole(Integer isCreatedRole) {
        this.isCreatedRole = isCreatedRole;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
