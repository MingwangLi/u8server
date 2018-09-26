package com.u8.server.web.admin;

import com.opensymphony.xwork2.ModelDriven;
import com.u8.server.common.Page;
import com.u8.server.common.UActionSupport;
import com.u8.server.data.UAdmin;
import com.u8.server.data.UChannel;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.service.*;
import com.u8.server.utils.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * 渠道管理
 * Created by ant on 2015/8/22.
 */
@Controller
@Namespace("/admin/channels")
public class ChannelAction extends UActionSupport implements ModelDriven<UChannel> {

    private int page;           //当前请求的页码
    private int rows;           //当前每页显示的行数

    private UChannel channel;
    private String chargeCloseTimeBegin;
    private String chargeCloseTimeEnd;
    private int currChannelID;
    private String searchMaserName;
    private String searchGameName;

    @Autowired
    private UChannelManager channelManager;

    @Autowired
    private UUserManager userManager;

    @Autowired
    private UChannelMasterManager channelMasterManager;

    @Autowired
    private UAdminManager adminManager;

    @Autowired
    private UGameManager gameManager;


    @Action(value = "channelManage", results = {@Result(name = "success", location = "/WEB-INF/admin/channels.jsp")})
    public String channelManage() {

        return "success";
    }

    @Action("getAllChannels")
    public void getAllChannels() {
        try {

            String masterIDs = "";
            //int masterID = 0;
            if (!StringUtils.isEmpty(searchMaserName)) {
                //masterID = -1;
                //UChannelMaster master = channelMasterManager.getMasterByName(searchMaserName);
                masterIDs = channelMasterManager.getMasterByNameList(searchMaserName);
                /*if (master != null) {
                    masterID = master.getMasterID();
                }*/
            }

            String gameIDs = "";
            //int gameID = 0;
            if (!StringUtils.isEmpty(searchGameName)) {
                // gameID = -1;
                // UGame game = gameManager.getGameByName(searchGameName);
                // if (game != null) {
                //     gameID = game.getAppID();
                // }
                gameIDs = gameManager.getGameByNameList(searchGameName);
            }

            UAdmin admin = (UAdmin) session.get("admin");

            //Page<UChannel> currPage = channelManager.queryPage(page, rows, channel.getChannelID(), gameID, masterID, adminManager.getPermissonedGameIDs(admin));
            Page<UChannel> currPage = channelManager.queryPage(page, rows, channel.getChannelID(), gameIDs, masterIDs, adminManager.getPermissonedGameIDs(admin));
            JSONObject json = new JSONObject();

            json.put("total", currPage == null ? 0 : currPage.getTotalCount());
            JSONArray users = new JSONArray();
            if (currPage != null) {
                for (UChannel m : currPage.getResultList()) {
                    users.add(m.toJSON());
                }
            }

            json.put("rows", users);

            renderJson(json.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Action("recommendChannelID")
    public void recommendChannelID() {
        try {

            int channelID = channelManager.getValidChannelID();

            JSONObject json = new JSONObject();
            json.put("state", 1);
            json.put("data", channelID);
            renderJson(json.toString());

        } catch (Exception e) {
            renderState(false);
            e.printStackTrace();
        }
    }

    //添加或者编辑
    @Action("addChannel")
    public void addChannel() {
        try {
            if ((!StringUtils.isEmpty(chargeCloseTimeBegin)) && (!StringUtils.isEmpty(chargeCloseTimeEnd))) {
                String chargeCloseTime = chargeCloseTimeBegin + "_" + chargeCloseTimeEnd;
                channel.setChargeCloseTime(chargeCloseTime);
            }
            Log.d("add.channel.info." + this.channel.toJSON().toString());
            UChannel exists = channelManager.queryChannel(channel.getChannelID());
            if (exists != null) {
                renderState(false, "操作失败,当前渠道号已经存在");
                return;
            }

            if (this.channel.getOpenPayFlag() == null) {
                this.channel.setOpenPayFlag(0);
            }

            channelManager.saveChannel(this.channel);
            renderState(true);

            return;

        } catch (Exception e) {
            e.printStackTrace();
        }

        renderState(false);
    }

    //添加或者编辑
    @Action("saveChannel")
    public void saveChannel() {
        try {
            if ((!StringUtils.isEmpty(chargeCloseTimeBegin)) && (!StringUtils.isEmpty(chargeCloseTimeEnd))) {
                String chargeCloseTime = chargeCloseTimeBegin + "_" + chargeCloseTimeEnd;
                channel.setChargeCloseTime(chargeCloseTime);
            }
            Log.d("save.channel.info." + this.channel.toJSON().toString());
            if (this.channel.getOpenPayFlag() == null) {
                this.channel.setOpenPayFlag(0);
            }

            channelManager.saveChannel(this.channel);
            renderState(true);

            return;

        } catch (Exception e) {
            e.printStackTrace();
        }

        renderState(false);
    }

    @Action("removeChannel")
    public void removeChannel() {
        try {

            Log.d("Curr channelID is " + this.currChannelID);
            UChannel c = this.channelManager.queryChannel(this.currChannelID);
            if (c == null) {
                renderState(false);
                return;
            }

            List<UUser> lst = this.userManager.getUsersByChannel(this.currChannelID);
            if (lst.size() > 0) {
                renderState(false, "请先删除该渠道下面的所有用户数据");
                return;
            }

            this.channelManager.deleteChannel(c);

            renderState(true);
            return;

        } catch (Exception e) {
            e.printStackTrace();
        }

        renderState(false);
    }

    private void renderState(boolean suc) {
        JSONObject json = new JSONObject();
        json.put("state", suc ? 1 : 0);
        json.put("msg", suc ? "操作成功" : "操作失败");
        renderText(json.toString());
    }

    private void renderState(boolean suc, String msg) {
        JSONObject json = new JSONObject();
        json.put("state", suc ? 1 : 0);
        json.put("msg", msg);
        renderText(json.toString());
    }


    @Override
    public UChannel getModel() {

        if (this.channel == null) {
            this.channel = new UChannel();
        }

        return this.channel;
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

    public UChannel getChannel() {
        return channel;
    }

    public void setChannel(UChannel channel) {
        this.channel = channel;
    }

    public int getCurrChannelID() {
        return currChannelID;
    }

    public void setCurrChannelID(int currChannelID) {
        this.currChannelID = currChannelID;
    }

    public String getSearchMaserName() {
        return searchMaserName;
    }

    public void setSearchMaserName(String searchMaserName) {
        this.searchMaserName = searchMaserName;
    }

    public String getSearchGameName() {
        return searchGameName;
    }

    public void setSearchGameName(String searchGameName) {
        this.searchGameName = searchGameName;
    }

    public void setChargeCloseTimeBegin(String chargeCloseTimeBegin) {
        this.chargeCloseTimeBegin = chargeCloseTimeBegin;
    }

    public void setChargeCloseTimeEnd(String chargeCloseTimeEnd) {
        this.chargeCloseTimeEnd = chargeCloseTimeEnd;
    }
}
