package com.u8.server.data.analytics;


import com.u8.server.utils.StringUtils;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;

public class GameChannelTSummaryData implements Serializable {
    private Integer appID;
    private String totalMoney;      //充值金额
    private Integer channelID;
    private String totalCostNum;     //充值用户

    private String totalNewreg;      //新增用户
    private String totalNewCostNum;  //新增付费用户
    private String appName;
    private String channelName;

    private String payOfRegistRate; //新增付费率       新增付费量/新增

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appID",appID);
        jsonObject.put("appName",appName);
        jsonObject.put("channelID",channelID);
        jsonObject.put("channelName",channelName);
        jsonObject.put("totalMoney",totalMoney.substring(0,totalMoney.indexOf(".")));
        jsonObject.put("totalCostNum",totalCostNum);
        jsonObject.put("totalNewreg",totalNewreg);
        jsonObject.put("totalNewCostNum",totalNewreg);
        jsonObject.put("payOfRegistRate",caculate(totalNewCostNum,totalNewreg));
        return jsonObject;
    }

    public String caculate(String payOfRegist,String totalRegist) {
        if (StringUtils.isEmpty(payOfRegist) || StringUtils.isEmpty(totalRegist)) {
            return "0.00%";
        }
        if (new BigDecimal(totalRegist).compareTo(BigDecimal.ZERO) == 0) {
            return "0.00%";
        }
        BigDecimal bigDecimal = new BigDecimal(payOfRegist);
        bigDecimal = bigDecimal.divide(new BigDecimal(totalRegist),2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
        return bigDecimal.toString()+"%";
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

    public Integer getChannelID() {
        return channelID;
    }

    public void setChannelID(Integer channelID) {
        this.channelID = channelID;
    }

    public String getTotalCostNum() {
        return totalCostNum;
    }

    public void setTotalCostNum(String totalCostNum) {
        this.totalCostNum = totalCostNum;
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

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getPayOfRegistRate() {
        return payOfRegistRate;
    }

    public void setPayOfRegistRate(String payOfRegistRate) {
        this.payOfRegistRate = payOfRegistRate;
    }
}
