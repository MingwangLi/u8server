package com.u8.server.data.analytics;

import com.u8.server.cache.CacheManager;
import com.u8.server.data.UChannel;
import com.u8.server.data.UGame;
import net.sf.json.JSONObject;

public class UChannelProfitData {
    private Integer appID;
    private String totalMoney;
    private Integer channelID;
    private String totalCostNum;
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("appID", appID);

        UGame game = getGame();

        json.put("appName", game == null ? "" : game.getName());
        json.put("channelID", channelID);

        UChannel channel = getChannel();
        json.put("channelName", channel == null ? "" : channel.getMaster().getMasterName());
        json.put("totalMoney",this.totalMoney);
        json.put("totalCostNum",this.totalCostNum);
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

    public String getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(String totalMoney) {
        this.totalMoney = totalMoney;
    }
    public String getTotalCostNum() {
        return totalCostNum;
    }

    public void setTotalCostNum(String totalCostNum) {
        this.totalCostNum = totalCostNum;
    }

    public Integer getChannelID() {
        return channelID;
    }

    public void setChannelID(Integer channelID) {
        this.channelID = channelID;
    }


}
