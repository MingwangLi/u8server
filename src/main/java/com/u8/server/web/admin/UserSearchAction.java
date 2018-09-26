package com.u8.server.web.admin;

import com.u8.server.common.Page;
import com.u8.server.common.UActionSupport;
import com.u8.server.data.UAdmin;
import com.u8.server.data.UUser;
import com.u8.server.service.UAdminManager;
import com.u8.server.service.UUserManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * 用户查询
 * Created by ant on 2016/8/3.
 */
@Namespace("/admin/users")
public class UserSearchAction extends UActionSupport {

    private int page;           //当前请求的页码
    private int rows;           //当前每页显示的行数
    private Integer appID;
    private Integer channelID;
    private Integer userID;
    private Date beginRegTime;
    private Date endRegTime;
    private String channelUserID;
    private String channelUserName;
    private String channelNickName;
    private Date beginLoginTime;
    private Date endLoginTime;


    @Autowired
    private UUserManager userManager;

    @Autowired
    private UAdminManager adminManager;

    @Action("searchUsers")
    public void searchUsers() {
        try {

            UAdmin admin = (UAdmin) session.get("admin");

            Page<UUser> currPage = this.userManager.queryPage(page, rows, appID, channelID, userID, beginRegTime, endRegTime
                    , channelUserID, channelUserName, channelNickName, beginLoginTime, endLoginTime, adminManager.getPermissonedGameIDs(admin));

            JSONObject json = new JSONObject();
            json.put("total", currPage == null ? 0 : currPage.getTotalCount());
            JSONArray users = new JSONArray();
            if (currPage != null) {
                for (UUser m : currPage.getResultList()) {
                    users.add(m.toJSON());
                }
            }

            json.put("rows", users);

            renderJson(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public Integer getAppID() {
        return appID;
    }

    public void setAppID(Integer appID) {
        this.appID = appID;
    }

    public Integer getChannelID() {
        return channelID;
    }

    public void setChannelID(Integer channelID) {
        this.channelID = channelID;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public Date getBeginRegTime() {
        return beginRegTime;
    }

    public void setBeginRegTime(Date beginRegTime) {
        this.beginRegTime = beginRegTime;
    }

    public Date getEndRegTime() {
        return endRegTime;
    }

    public void setEndRegTime(Date endRegTime) {
        this.endRegTime = endRegTime;
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

    public String getChannelNickName() {
        return channelNickName;
    }

    public void setChannelNickName(String channelNickName) {
        this.channelNickName = channelNickName;
    }

    public Date getBeginLoginTime() {
        return beginLoginTime;
    }

    public void setBeginLoginTime(Date beginLoginTime) {
        this.beginLoginTime = beginLoginTime;
    }

    public Date getEndLoginTime() {
        return endLoginTime;
    }

    public void setEndLoginTime(Date endLoginTime) {
        this.endLoginTime = endLoginTime;
    }
}
