package com.u8.server.data.analytics;

import com.u8.server.cache.CacheManager;
import com.u8.server.data.UChannel;
import com.u8.server.data.UChannelMaster;
import net.sf.json.JSONObject;
import javax.persistence.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name="uuserinfo")
public class UUserInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer userID;
    private String roleID;
    private String roleName;
    private String roleLevel;
    private String serverID;
    private String serverName;
    private Integer appID;
    private Date createdTime;
    private Integer channelID;

    public UUserInfo(){}

    public UUserInfo(Integer userID,String roleID,String roleName,String roleLevel,String serverID,String serverName,Integer appID,Integer channelID) {
        this.userID = userID;
        this.roleID = roleID;
        this.roleName = roleName;
        this.roleLevel = roleLevel;
        this.serverID = serverID;
        this.serverName = serverName;
        this.appID = appID;
        this.createdTime = new Date();
        this.channelID = channelID;
    }

    public String toJSONString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = "";
        String masterName = "";
        if (null != createdTime) {
            time = simpleDateFormat.format(createdTime);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",id);
        jsonObject.put("userID",userID);
        jsonObject.put("roleID",roleID);
        jsonObject.put("roleName",roleName);
        jsonObject.put("roleLevel",roleLevel);
        jsonObject.put("serverID",serverID);
        jsonObject.put("serverName",serverName);
        jsonObject.put("appID",CacheManager.getInstance().getGame(appID).getName());
        jsonObject.put("createdTime",time);
        UChannel uChannel = null;
        if (null != channelID) {
            uChannel = CacheManager.getInstance().getChannel(channelID);
        }
        UChannelMaster master = null;
        if(null != uChannel) {
            master = uChannel.getMaster();
        }
        if (null != master) {
            masterName = master.getMasterName();
        }
        //jsonObject.put("channelID",CacheManager.getInstance().getChannel(channelID).getMaster().getMasterName());
        jsonObject.put("channelID",masterName);
        return jsonObject.toString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getRoleID() {
        return roleID;
    }

    public void setRoleID(String roleID) {
        this.roleID = roleID;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(String roleLevel) {
        this.roleLevel = roleLevel;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Integer getAppID() {
        return appID;
    }

    public void setAppID(Integer appID) {
        this.appID = appID;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getChannelID() {
        return channelID;
    }

    public void setChannelID(Integer channelID) {
        this.channelID = channelID;
    }
}
