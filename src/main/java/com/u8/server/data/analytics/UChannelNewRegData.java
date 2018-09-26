package com.u8.server.data.analytics;

import com.u8.server.cache.CacheManager;
import com.u8.server.data.UChannel;
import com.u8.server.data.UGame;
import net.sf.json.JSONObject;

public class UChannelNewRegData {
    private Integer appID;
    private Integer channelID;
    private String totalNewreg;
    private String totalNewCostNum;
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("appID", appID);

        UGame game = getGame();

        json.put("appName", game == null ? "" : game.getName());
        json.put("channelID", channelID);

        UChannel channel = getChannel();
        json.put("channelName", channel == null ? "" : channel.getMaster().getMasterName());
        json.put("totalNewreg",this.totalNewreg);
        json.put("totalNewCostNum",this.totalNewCostNum);
        return json;

    }
    public UChannel getChannel() {

        return CacheManager.getInstance().getChannel(this.channelID);
    }

    public UGame getGame() {
        return CacheManager.getInstance().getGame(this.appID);
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

    public String getTotalNewreg() {
        return totalNewreg;
    }

    public void setTotalNewreg(String totalNewreg) {
        this.totalNewreg = totalNewreg;
    }

    public String getTotalNewCostNum() {
        return totalNewCostNum;
    }

    public void setTotalNewCostNum(String totalNewCostNum) {
        this.totalNewCostNum = totalNewCostNum;
    }
}
