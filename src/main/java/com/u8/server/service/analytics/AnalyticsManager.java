package com.u8.server.service.analytics;

import com.u8.server.common.OrderParameter;
import com.u8.server.common.OrderParameters;
import com.u8.server.common.Page;
import com.u8.server.dao.analytics.TChannelSummaryDao;
import com.u8.server.dao.analytics.TRetentionDao;
import com.u8.server.dao.analytics.TSummaryDao;
import com.u8.server.data.analytics.*;
import com.u8.server.utils.StringUtils;
import com.u8.server.utils.TimeUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ant on 2016/8/26.
 */
@Service("summaryManager")
public class AnalyticsManager {

    @Autowired
    private TSummaryDao summaryDao;

    @Autowired
    private TChannelSummaryDao channelSummaryDao;

    @Autowired
    private TRetentionDao retentionDao;

    public SummaryInfo getSummaryInfo(int appID, Date from, Date to){

        return summaryDao.querySummaryInfo(appID, from, to);

    }
    public Page<TSummary> getSummaryPage(int currPage, int num, Integer appID, Date beginCreateTime, Date endCreateTime){

        return summaryDao.querySummaryPage(currPage, num, appID, beginCreateTime, endCreateTime);
    }


    /**
     * 收集分析统计数量，生成展示需要的json格式
     * @return
     */
    public String collectSummaryInfo(int appID, Date from, Date to){

        SummaryInfo summary = summaryDao.querySummaryInfo(appID, from, to);

        JSONObject json = new JSONObject();
        json.put("deviceNum", summary.getDeviceNum());
        json.put("userNum", summary.getUserNum());
        json.put("payUserNum", summary.getPayUserNum());
        json.put("payMoney", summary.getPayMoney());

        List<TSummary> data = summary.getSummaryData();
        if(data != null && data.size() > 0){

            TSummary first = data.get(0);
            TSummary last = data.get(data.size()-1);
            SimpleDateFormat format = TimeUtils.FORMATER_5;
            if(TimeUtils.sameYear(first.getCurrTime(), last.getCurrTime())){
                format = TimeUtils.FORMATER_6;
            }

            JSONArray category = new JSONArray();
            for(TSummary s :data) {
                category.add(format.format(s.getCurrTime()));
            }

            json.put("keyCategory", category);


            JSONArray deviceNums = new JSONArray();
            for(TSummary s : data){
                deviceNums.add(s.getDeviceNum());
            }
            json.put("deviceData", deviceNums);

            JSONArray userNums = new JSONArray();
            for(TSummary s :data){
                userNums.add(s.getUserNum());
            }
            json.put("newUserData", userNums);


            JSONArray dauArray = new JSONArray();
            for(TSummary s : data){
                dauArray.add(s.getDau());
            }
            json.put("allDaudata", dauArray);

            JSONArray ndauArray = new JSONArray();
            for(TSummary s : data){
                ndauArray.add(s.getNdau());
            }
            json.put("newDaudata", ndauArray);


            JSONArray payUserArray = new JSONArray();
            for(TSummary s : data){
                payUserArray.add(s.getPayUserNum());
            }
            json.put("allPayData", payUserArray);

            JSONArray newPayUserArray = new JSONArray();
            for(TSummary s : data) {
                newPayUserArray.add(s.getNewPayUserNum());
            }
            json.put("newPayData", newPayUserArray);


            JSONArray moneyArray = new JSONArray();
            for(TSummary s : data){
                moneyArray.add(s.getMoney() / 100);
            }
            json.put("moneyData", moneyArray);

        }

        return json.toString();
    }

    /**
     * 收集新增玩家统计信息
     * @param appID
     * @param from
     * @param to
     * @return
     */
    public String collectNewUserInfo(int appID, Date from, Date to){

        JSONObject json = new JSONObject();
        List<TSummary> data = summaryDao.querySummaryData(appID, from, to);

        if(data != null && data.size() > 0){

            TSummary first = data.get(0);
            TSummary last = data.get(data.size()-1);
            SimpleDateFormat format = TimeUtils.FORMATER_5;
            if(TimeUtils.sameYear(first.getCurrTime(), last.getCurrTime())){
                format = TimeUtils.FORMATER_6;
            }

            JSONArray category = new JSONArray();
            for(TSummary s :data) {
                category.add(format.format(s.getCurrTime()));
            }

            json.put("keyCategory", category);

            JSONArray deviceNums = new JSONArray();
            int totalDevice = 0;
            for(TSummary s : data){
                deviceNums.add(s.getDeviceNum());
                totalDevice += s.getDeviceNum();
            }
            json.put("deviceData", deviceNums);
            json.put("avgDevice", totalDevice/data.size());

            JSONArray userNums = new JSONArray();
            int userTotal = 0;
            for(TSummary s :data){
                userNums.add(s.getUserNum());
                userTotal += s.getUserNum();
            }
            json.put("newUserData", userNums);
            json.put("avgUser", userTotal/data.size());

            float ratioTotal = 0;
            JSONArray array = new JSONArray();
            for(TSummary s : data){
                float ratio = (s.getDeviceNum() == null || s.getDeviceNum().equals(0)) ? 0 : ((float)s.getUniUserNum() / (float)s.getDeviceNum() * 100f);
                array.add(ratio);
                ratioTotal += ratio;
            }
            json.put("ratioData", array);
            json.put("avgRatio", ratioTotal/data.size());

        }

        OrderParameters orderBy = new OrderParameters();
        orderBy.add("userNum", OrderParameter.OrderType.DESC);

        List<ChannelGroupInfo> groups = channelSummaryDao.queryChannelGroups(appID, from, to, orderBy);
        if(groups != null && groups.size() > 0){
            long totalUser = 0;
            JSONArray category = new JSONArray();
            for(ChannelGroupInfo s :groups) {
                category.add(String.format("%s[%s]", s.getChannelName(), s.getChannelID()));
                totalUser += s.getUserNum();
            }

            json.put("channelCategory", category);

            JSONArray userData = new JSONArray();
            JSONArray userRatio = new JSONArray();
            for(ChannelGroupInfo s : groups){
                userData.add(s.getUserNum());
                userRatio.add((int)(totalUser == 0 ? 0 : (float)s.getUserNum()/(float)totalUser * 100f));
            }
            json.put("userData", userData);
            json.put("userRatio", userRatio);
        }

        return json.toString();

    }

    /**
     * 收集玩家活跃信息
     * @param appID
     * @param from
     * @param to
     * @return
     */
    public String collectDAUInfo(int appID, Date from, Date to){
        JSONObject json = new JSONObject();
        List<TSummary> data = summaryDao.querySummaryData(appID, from, to);
        if(data != null && data.size() > 0){

            TSummary first = data.get(0);
            TSummary last = data.get(data.size()-1);
            SimpleDateFormat format = TimeUtils.FORMATER_5;
            if(TimeUtils.sameYear(first.getCurrTime(), last.getCurrTime())){
                format = TimeUtils.FORMATER_6;
            }

            JSONArray category = new JSONArray();
            for(TSummary s :data) {
                category.add(format.format(s.getCurrTime()));
            }

            json.put("keyCategory", category);


            JSONArray dauArray = new JSONArray();
            JSONArray ndauArray = new JSONArray();
            JSONArray wauArray = new JSONArray();
            JSONArray mauArray = new JSONArray();
            JSONArray dauMauArray = new JSONArray();
            JSONArray avgTimeArray = new JSONArray();

            float dauTotal = 0;
            float wauTotal = 0;
            float mauTotal = 0;
            float timeTotal = 0;

            for(TSummary s : data){
                dauArray.add(s.getDau());
                ndauArray.add(s.getNdau());
                wauArray.add(s.getWau());
                mauArray.add(s.getMau());
                dauMauArray.add((s.getMau() == null || s.getMau().equals(0)) ? 0 : (float)s.getDau()/(float)s.getMau());
                avgTimeArray.add(s.getAvg() == null ? 0 : Math.ceil(s.getAvg()/60f));

                dauTotal += s.getDau();
                wauTotal += s.getWau();
                mauTotal += s.getMau();
                timeTotal += (s.getAvg() == null ? 0 : Math.ceil(s.getAvg()/60f));

            }
            json.put("dauData", dauArray);
            json.put("ndauData", ndauArray);
            json.put("wauData", wauArray);
            json.put("mauData", mauArray);
            json.put("dauMauData", dauMauArray);
            json.put("avgData", avgTimeArray);

            int size = data.size();
            json.put("dauAvg", dauTotal/size);
            json.put("wauAvg", wauTotal/size);
            json.put("mauAvg", mauTotal/size);
            json.put("dauMauAvg", dauTotal/mauTotal);
            json.put("timeAvg", timeTotal/size);

        }

        return json.toString();
    }


    /**
     * 收集玩家留存信息
     * @param appID
     * @param from
     * @param to
     * @return
     */
    public String collectRetentionInfo(int appID, Date from, Date to){

        JSONObject json = new JSONObject();
        List<TRetention> data = retentionDao.queryRetentionData(appID, from, to);
        if(data != null && data.size() > 0){

            TRetention first = data.get(0);
            TRetention last = data.get(data.size()-1);
            SimpleDateFormat format = TimeUtils.FORMATER_5;
            if(TimeUtils.sameYear(first.getStatTime(), last.getStatTime())){
                format = TimeUtils.FORMATER_6;
            }

            JSONArray category = new JSONArray();
            for(TRetention s : data) {
                category.add(format.format(s.getStatTime()));
            }

            json.put("keyCategory", category);

            JSONArray dayArray = new JSONArray();
            JSONArray tdayArray = new JSONArray();
            JSONArray wdayArray = new JSONArray();
            JSONArray mdayArray = new JSONArray();

            float daySum = 0;
            int dayCount =0;
            float tdaySum = 0;
            int tdayCount = 0;
            float wdaySum = 0;
            int wdayCount = 0;
            float mdaySum = 0;
            int mdayCount = 0;

            for(TRetention s : data){

                if(s.getDayRetention() == null){
                    s.setDayRetention(",");
                }

                String rs = s.getDayRetention();
                if(rs.startsWith(",")){
                    rs = rs.substring(1);
                }
                rs = rs.trim();
                String[] userNums = rs.split(",");
                if(userNums.length >= 1){
                    String[] vals = userNums[0].split(":");
                    float v = (vals[0].length() <= 1 || Integer.valueOf(vals[1]) <= 0) ? 0 : Float.valueOf(vals[1])/Float.valueOf(vals[0]) * 100f;
                    daySum += v;
                    dayArray.add(v);
                    dayCount++;
                }else{
                    dayArray.add(0);
                }

                if(userNums.length >= 3){
                    String[] vals = userNums[2].split(":");

                    float v = (vals[0].length() <=1 || Integer.valueOf(vals[1]) <= 0) ? 0 : Float.valueOf(vals[1])/Float.valueOf(vals[0]) * 100f;
                    tdaySum += v;
                    tdayArray.add(v);
                    tdayCount++;
                }else{
                    tdayArray.add(0);
                }

                if(userNums.length >= 7){
                    String[] vals = userNums[6].split(":");

                    float v = (vals[0].length() <=1 || Integer.valueOf(vals[1]) <= 0) ? 0 : Float.valueOf(vals[1])/Float.valueOf(vals[0]) * 100f;
                    wdaySum += v;
                    wdayArray.add(v);
                    wdayCount++;
                }else{
                    wdayArray.add(0);
                }

                if(userNums.length >= 30){
                    String[] vals = userNums[29].split(":");

                    float v = (vals[0].length() <=1 || Integer.valueOf(vals[1]) <= 0) ? 0 : Float.valueOf(vals[1])/Float.valueOf(vals[0]) * 100f;
                    mdaySum += v;
                    mdayArray.add(v);
                    mdayCount++;
                }else{
                    mdayArray.add(0);
                }
            }

            json.put("dayArray", dayArray);
            json.put("tdayArray", tdayArray);
            json.put("wdayArray", wdayArray);
            json.put("mdayArray", mdayArray);

            json.put("dayAvg", daySum/(dayCount==0?1:dayCount));
            json.put("tdayAvg", tdaySum/(tdayCount==0?1:tdayCount));
            json.put("wdayAvg", wdaySum/(wdayCount==0?1:wdayCount));
            json.put("mdayAvg", mdaySum/(mdayCount==0?1:mdayCount));

        }



        return json.toString();
    }


    /**
     * 收集玩家流失信息
     * @param appID
     * @param from
     * @param to
     * @param timeType 1:7日算流失;2:14日算流失;3:30日算流失
     * @return
     */
    public String collectFlowInfo(int appID, Date from, Date to, int timeType){

        if(timeType <= 0 || timeType > 3){
            throw new RuntimeException("timeType must between 1 and 3");
        }

        JSONObject json = new JSONObject();
        List<TRetention> data = retentionDao.queryRetentionData(appID, from, to);
        if(data != null && data.size() > 0){

            TRetention first = data.get(0);
            TRetention last = data.get(data.size()-1);
            SimpleDateFormat format = TimeUtils.FORMATER_5;
            if(TimeUtils.sameYear(first.getStatTime(), last.getStatTime())){
                format = TimeUtils.FORMATER_6;
            }

            JSONArray category = new JSONArray();
            for(TRetention s : data) {
                category.add(format.format(s.getStatTime()));
            }

            json.put("keyCategory", category);

            JSONArray wdayArray = new JSONArray();
            JSONArray wdayCountArray = new JSONArray();


            float wdaySum = 0;
            int wdayCountSum = 0;
            int wdayCount = 0;

            for(TRetention s : data){

                if(StringUtils.isEmpty(s.getDayPayRatio())){
                    s.setDayFlowRatio(",");
                }

                String rs = s.getDayFlowRatio();
                if(rs.startsWith(",")){
                    rs = rs.substring(1);
                }


                String[] userNums = rs.split(",");

                if(userNums.length >= timeType){
                    String[] vals = userNums[timeType-1].split(":");
                    float v = (vals[0].length() <=1 || Integer.valueOf(vals[1]) <= 0) ? 0 : Float.valueOf(vals[0])/Float.valueOf(vals[1]) * 100f;
                    wdaySum += v;
                    wdayCountSum += (vals[0].length() == 0 ? 0 : Integer.valueOf(vals[0]));
                    wdayArray.add(v);
                    wdayCountArray.add((vals[0].length() == 0 ? 0 : Integer.valueOf(vals[0])));
                    wdayCount++;
                }else{
                    wdayArray.add(0);
                    wdayCountArray.add(0);
                }
            }

            json.put("wdayData", wdayArray);
            json.put("wdayCountData", wdayCountArray);
            json.put("wdayAvg", wdaySum/wdayCount);
            json.put("wdayCountAvg", wdayCountSum/wdayCount);
            json.put("wdayCountTotal", wdayCountSum);


            JSONArray backArray = new JSONArray();
            JSONArray backCountArray = new JSONArray();


            float backSum = 0;
            int backCount = 0;
            int backCountSum = 0;

            for(TRetention s : data){

                if(s.getDayBackRatio() == null){
                    s.setDayBackRatio(",");
                }

                String rs = s.getDayBackRatio();
                if(rs.startsWith(",")){
                    rs = rs.substring(1);
                }

                String[] userNums = rs.split(",");

                if(userNums.length >= timeType){
                    String[] vals = userNums[timeType-1].split(":");
                    float v = (vals[0].length() <= 1 || Integer.valueOf(vals[1]) <= 0) ? 0 : Float.valueOf(vals[0])/Float.valueOf(vals[1]) * 100f;
                    backSum += v;
                    backCountSum += (vals[0].length() == 0 ? 0 : Integer.valueOf(vals[0]));
                    backArray.add(v);
                    backCountArray.add((vals[0].length() == 0 ? 0 : Integer.valueOf(vals[0])));
                    backCount++;
                }else{
                    backArray.add(0);
                    backCountArray.add(0);
                }
            }

            json.put("backData", backArray);
            json.put("backCountData", backCountArray);
            json.put("backAvg", backSum/backCount);
            json.put("backCountAvg", backCountSum/backCount);
            json.put("backCountTotal", backCountSum);

        }


        return json.toString();
    }


    /**
     * 收集收入数据
     * @param appID
     * @param from
     * @param to
     * @return
     */
    public String collectMoneyInfo(int appID, Date from, Date to){
        JSONObject json = new JSONObject();
        List<TSummary> data = summaryDao.querySummaryData(appID, from, to);
        if(data != null && data.size() > 0){

            TSummary first = data.get(0);
            TSummary last = data.get(data.size()-1);
            SimpleDateFormat format = TimeUtils.FORMATER_5;
            if(TimeUtils.sameYear(first.getCurrTime(), last.getCurrTime())){
                format = TimeUtils.FORMATER_6;
            }

            JSONArray category = new JSONArray();
            for(TSummary s :data) {
                category.add(format.format(s.getCurrTime()));
            }

            json.put("keyCategory", category);


            JSONArray moneyArray = new JSONArray();

            float total = 0;
            for(TSummary s : data){
                moneyArray.add(s.getMoney()/100f);
                total += s.getMoney()/100f;
            }
            json.put("moneyData", moneyArray);

            json.put("totalMoney", total);
            json.put("avgMoney", total/data.size());

        }

        List<ChannelGroupInfo> groups = channelSummaryDao.queryChannelGroups(appID, from, to, null);
        if(groups != null && groups.size() > 0){

            Collections.sort(groups, new Comparator<ChannelGroupInfo>() {
                @Override
                public int compare(ChannelGroupInfo o1, ChannelGroupInfo o2) {
                    return (int)Math.ceil(o2.getPayMoney() - o1.getPayMoney());
                }
            });
            JSONArray payUserNum = new JSONArray();
            JSONArray category = new JSONArray();
            float total = 0;
            for(ChannelGroupInfo s :groups) {
                category.add(String.format("%s[%s]", s.getChannelName(), s.getChannelID()));
                payUserNum.add(s.getPayUserNum());
                total += s.getPayMoney();
            }

            json.put("channelCategory", category);
            json.put("payUserNum", payUserNum);
            JSONArray moneyArray = new JSONArray();
            JSONArray moneyRatioArray = new JSONArray();
            for(ChannelGroupInfo s : groups){
                moneyArray.add(s.getPayMoney());
                moneyRatioArray.add(total <= 0 ? 0f : s.getPayMoney()/total*100f);
            }
            json.put("channelMoneyData", moneyArray);
            json.put("channelMoneyRatio", moneyRatioArray);
        }

        return json.toString();
    }

    /**
     * 收集付费统计数据
     * @param appID
     * @param from
     * @param to
     * @return
     */
    public String collectPayInfo(int appID, Date from, Date to){

        JSONObject json = new JSONObject();
        List<TSummary> data = summaryDao.querySummaryData(appID, from, to);
        if(data != null && data.size() > 0){

            TSummary first = data.get(0);
            TSummary last = data.get(data.size()-1);
            SimpleDateFormat format = TimeUtils.FORMATER_5;
            if(TimeUtils.sameYear(first.getCurrTime(), last.getCurrTime())){
                format = TimeUtils.FORMATER_6;
            }

            JSONArray category = new JSONArray();
            for(TSummary s :data) {
                category.add(format.format(s.getCurrTime()));
            }

            json.put("keyCategory", category);


            JSONArray payArray = new JSONArray();
            JSONArray payCountArray = new JSONArray();
            JSONArray arpuArray = new JSONArray();
            JSONArray arppuArray = new JSONArray();
            float total = 0;
            float arpuTotal = 0;
            float arppuTotal = 0;
            float payCountTotal = 0;
            float payTotal = 0;
            for(TSummary s : data){
                payArray.add(s.getDau() <= 0 ? 0f : (float)s.getPayUserNum()/(float)s.getDau()*100f);
                payTotal += (s.getDau() <= 0 ? 0f : (float)s.getPayUserNum()/(float)s.getDau()*100f);
                payCountArray.add(s.getPayUserNum());
                payCountTotal += s.getPayUserNum();
                arpuArray.add(s.getDau() <= 0 ? 0f : ((float)s.getMoney()/100f)/(float)s.getDau());
                arpuTotal += (s.getDau() <= 0 ? 0f : ((float)s.getMoney()/100f)/(float)s.getDau());
                arppuArray.add(s.getPayUserNum() <= 0 ? 0f : ((float)s.getMoney()/100f)/(float)s.getPayUserNum());
                arppuTotal += (s.getPayUserNum() <= 0 ? 0f : ((float)s.getMoney()/100f)/(float)s.getPayUserNum());
                total += s.getPayUserNum();
            }
            json.put("payRatioData", payArray);
            json.put("payCountData", payCountArray);
            json.put("arpuData", arpuArray);
            json.put("arppuData", arppuArray);

            json.put("totalCount", total);
            json.put("arpuAvg", arpuTotal/data.size());
            json.put("arppuAvg",arppuTotal/data.size());
            json.put("payRatioAvg", payTotal/data.size());
            json.put("payCountAvg", payCountTotal/data.size());

        }


        return json.toString();
    }


    /**
     * 收集付费转化率数据
     * @param appID
     * @param from
     * @param to
     * @return
     */
    public String collectPayRatioInfo(int appID, Date from, Date to){

        JSONObject json = new JSONObject();

        List<TSummary> data = summaryDao.querySummaryData(appID, from, to);
        if(data != null && data.size() > 0){

            TSummary first = data.get(0);
            TSummary last = data.get(data.size()-1);
            SimpleDateFormat format = TimeUtils.FORMATER_5;
            if(TimeUtils.sameYear(first.getCurrTime(), last.getCurrTime())){
                format = TimeUtils.FORMATER_6;
            }

            JSONArray category = new JSONArray();
            for(TSummary s :data) {
                category.add(format.format(s.getCurrTime()));
            }

            json.put("payCategory", category);


            JSONArray payArray = new JSONArray();
            JSONArray totalPayArray = new JSONArray();
            JSONArray totalRatioArray = new JSONArray();

            float ratioTotal = 0;
            for(TSummary s : data){
                payArray.add(s.getNewPayUserNum());
                totalPayArray.add(s.getTotalPayUserNum());
                totalRatioArray.add((float)s.getTotalPayUserNum()/(float)s.getTotalUserNum() * 100f);
                ratioTotal += (float)s.getTotalPayUserNum()/(float)s.getTotalUserNum() * 100f;
            }
            json.put("payData", payArray);
            json.put("payTotalData", totalPayArray);
            json.put("payRatioData", totalRatioArray);

            json.put("ratioAvg", ratioTotal/data.size());

        }



        List<TRetention> rData = retentionDao.queryRetentionData(appID, from, to);
        if(rData != null && rData.size() > 0) {

            TRetention first = rData.get(0);
            TRetention last = rData.get(rData.size() - 1);
            SimpleDateFormat format = TimeUtils.FORMATER_5;
            if (TimeUtils.sameYear(first.getStatTime(), last.getStatTime())) {
                format = TimeUtils.FORMATER_6;
            }

            JSONArray category = new JSONArray();
            for (TRetention s : rData) {
                category.add(format.format(s.getStatTime()));
            }

            json.put("keyCategory", category);

            JSONArray dayArray = new JSONArray();
            JSONArray tdayArray = new JSONArray();
            JSONArray wdayArray = new JSONArray();
            JSONArray mdayArray = new JSONArray();

            float daySum = 0;
            int dayCount = 0;
            float tdaySum = 0;
            int tdayCount = 0;
            float wdaySum = 0;
            int wdayCount = 0;
            float mdaySum = 0;
            int mdayCount = 0;

            for (TRetention s : rData) {

                if (s.getDayPayRatio() == null) {
                    s.setDayPayRatio(",");
                }

                String rs = s.getDayPayRatio();
                if (rs.startsWith(",")) {
                    rs = rs.substring(1);
                }


                String[] userNums = rs.split(",");
                if (userNums.length >= 3) {
                    String[] vals = userNums[0].split(":");

                    float v = (vals[0].length() <=1 || Integer.valueOf(vals[1]) <= 0) ? 0 : Float.valueOf(vals[0]) / Float.valueOf(vals[1]) * 100f;
                    daySum += v;
                    dayArray.add(v);
                    dayCount++;
                } else {
                    dayArray.add(0);
                }

                if (userNums.length >= 7) {
                    String[] vals = userNums[2].split(":");
                    float v = (vals[0].length() <=1 || Integer.valueOf(vals[1]) <= 0) ? 0 : Float.valueOf(vals[0]) / Float.valueOf(vals[1]) * 100f;
                    tdaySum += v;
                    tdayArray.add(v);
                    tdayCount++;
                } else {
                    tdayArray.add(0);
                }

                if (userNums.length >= 14) {
                    String[] vals = userNums[6].split(":");
                    float v = (vals[0].length() <=1 || Integer.valueOf(vals[1]) <= 0) ? 0 : Float.valueOf(vals[0]) / Float.valueOf(vals[1]) * 100f;
                    wdaySum += v;
                    wdayArray.add(v);
                    wdayCount++;
                } else {
                    wdayArray.add(0);
                }

                if (userNums.length >= 30) {
                    String[] vals = userNums[29].split(":");
                    float v = (vals[0].length() <=1 || Integer.valueOf(vals[1]) <= 0) ? 0 : Float.valueOf(vals[0]) / Float.valueOf(vals[1]) * 100f;
                    mdaySum += v;
                    mdayArray.add(v);
                    mdayCount++;
                } else {
                    mdayArray.add(0);
                }
            }

            json.put("dayArray", dayArray);
            json.put("tdayArray", tdayArray);
            json.put("wdayArray", wdayArray);
            json.put("mdayArray", mdayArray);

            json.put("dayAvg", daySum / (dayCount == 0 ? 1 : dayCount));
            json.put("tdayAvg", tdaySum / (tdayCount == 0 ? 1 : tdayCount));
            json.put("wdayAvg", wdaySum / (wdayCount == 0 ? 1 : wdayCount));
            json.put("mdayAvg", mdaySum / (mdayCount == 0 ? 1 : mdayCount));
        }

        return json.toString();
    }
    /**
     * 渠道收入数据
     * @param appID
     * @param from
     * @param to
     * @return
     */
    public JSONArray collectProfitInfo(int appID, Date from, Date to, String appIDList,String masterName){
        List<UChannelProfitData> currDataList = summaryDao.queryProfitData(appID,from,to, appIDList);
        JSONArray jsonArray = new JSONArray();
        float totalMoney = 0F;
        int totalCostNum = 0;
        for(UChannelProfitData currData:currDataList){
            if(!masterName.equals("全部渠道")) {
                if (currData.toJSON().containsValue(masterName)) {
                    totalMoney += Float.valueOf(currData.getTotalMoney());
                    totalCostNum += Integer.valueOf(currData.getTotalCostNum());
                    jsonArray.add(currData.toJSON());
                }
            }else{
                totalMoney += Float.valueOf(currData.getTotalMoney());
                totalCostNum += Integer.valueOf(currData.getTotalCostNum());
                jsonArray.add(currData.toJSON());
            }
        }
        jsonArray.add(totalMoney);
        jsonArray.add(totalCostNum);
        return jsonArray;
    }
    /**
     * 渠道新增数据
     * @param appID
     * @param from
     * @param to
     * @return
     */
    public JSONArray collectNewRegInfo(int appID, Date from, Date to, String appIDList, String masterName){
        List<UChannelNewRegData> currDataList = summaryDao.queryNewRegData(appID,from,to,appIDList);
        JSONArray jsonArray = new JSONArray();
        int totalReg = 0;
        int totalNewPayRate = 0;
        for(UChannelNewRegData currData:currDataList){
            if(!masterName.equals("全部渠道")) {
                if (currData.toJSON().containsValue(masterName)) {
                    totalReg += Integer.valueOf(currData.getTotalNewreg());
                    totalNewPayRate += Integer.valueOf(currData.getTotalNewCostNum());
                    jsonArray.add(currData.toJSON());
                }
            }else{
                totalReg += Integer.valueOf(currData.getTotalNewreg());
                totalNewPayRate += Integer.valueOf(currData.getTotalNewCostNum());
                jsonArray.add(currData.toJSON());
            }
        }
        jsonArray.add(totalReg);
        jsonArray.add(totalNewPayRate);
        return jsonArray;
    }


    public List<TSummary> getSummaryPageAll(Integer appID,Date beginTime,Date endTime) {
        return summaryDao.getSummaryPageAll(appID,beginTime,endTime);
    }

    @SuppressWarnings("all")
    public String getLTVData(int appID,String beginTime,String endTime) throws Exception {
        JSONObject object = new JSONObject();
        object.put("state",1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date begin = sdf.parse(beginTime);
        Date end = sdf.parse(endTime);
        List<String> date = new ArrayList<>();
        List<Double> today = new ArrayList<>();
        List<Double> threeDay = new ArrayList<>();
        List<Double> week = new ArrayList<>();
        List<Double> month = new ArrayList<>();
        String todayLTV = null;
        String threeDayLTV = null;
        String weekLTV = null;
        String monthLTV = null;
        String avgMoneyOne = null;
        String avgMoneyThree = null;
        String avgMoneyWeek = null;
        String avgMoneyMonth = null;
        Double todayAVG = 0D;
        Double threeDayAVG = 0D;
        Double weekAVG = 0D;
        Double monthAVG = 0D;
        List<Object[]> ltvData = retentionDao.getLTVData(appID,begin,end);
        for (Object[] objects:ltvData) {
            String value = (String)objects[0];
            String[] time = (objects[1]+"").split(" ");
            date.add(time[0]);
            String[] ltvs = value.split(",");
            for(int i = 0;i<ltvs.length;i++) {
                todayLTV = ltvs[0];
                if (ltvs.length < 3) {
                    threeDayLTV = "0:0";
                }else {
                    threeDayLTV = ltvs[2];
                }
                if (ltvs.length < 7) {
                    weekLTV = "0:0";
                }else {
                    weekLTV = ltvs[6];
                }
                if (ltvs.length < 30) {
                    monthLTV = "0:0";
                }else {
                    monthLTV = ltvs[29];
                }

                if (StringUtils.isEmpty(todayLTV)) {
                    todayLTV = "0:0";
                }
                if (StringUtils.isEmpty(threeDayLTV)) {
                    threeDayLTV = "0:0";
                }
                if (StringUtils.isEmpty(weekLTV)) {
                    weekLTV = "0:0";
                }
                if (StringUtils.isEmpty(monthLTV)) {
                    monthLTV = "0:0";
                }

            }
            String[] moneyAndPayCountOne = todayLTV.split(":");
            if ("0".equals(moneyAndPayCountOne[1])) {
                avgMoneyOne = "0";
                today.add(Double.parseDouble(avgMoneyOne));
            }else {
                BigDecimal bigDecimal1 = new BigDecimal(moneyAndPayCountOne[0]);
                bigDecimal1 = bigDecimal1.divide(new BigDecimal(moneyAndPayCountOne[1]),2).divide(new BigDecimal("100"));
                avgMoneyOne = bigDecimal1.toString();
                today.add(Double.parseDouble(avgMoneyOne));
            }


            String[] moneyAndPayCountThree = threeDayLTV.split(":");
            if ("0".equals(moneyAndPayCountThree[1])) {
                avgMoneyThree = "0";
                threeDay.add(Double.parseDouble(avgMoneyThree));
            }else {
                BigDecimal bigDecimal3 = new BigDecimal(moneyAndPayCountThree[0]);
                bigDecimal3.setScale(2);
                bigDecimal3 = bigDecimal3.divide(new BigDecimal(moneyAndPayCountThree[1]),2).divide(new BigDecimal("100"));
                avgMoneyThree = bigDecimal3.toString();
                threeDay.add(Double.parseDouble(avgMoneyThree));
            }


            String[] moneyAndPayCountWeek = weekLTV.split(":");
            if ("0".equals(moneyAndPayCountWeek[1])) {
                avgMoneyWeek = "0";
                week.add(Double.parseDouble(avgMoneyWeek));
            }else {
                BigDecimal bigDecimal7 = new BigDecimal(moneyAndPayCountWeek[0]);
                bigDecimal7.setScale(2);
                bigDecimal7 = bigDecimal7.divide(new BigDecimal(moneyAndPayCountWeek[1]),2).divide(new BigDecimal("100"));
                avgMoneyWeek = bigDecimal7.toString();
                week.add(Double.parseDouble(avgMoneyWeek));
            }


            String[] moneyAndPayCountMonth = monthLTV.split(":");
            if ("0".equals(moneyAndPayCountMonth[1])) {
                avgMoneyMonth = "0";
                month.add(Double.parseDouble(avgMoneyMonth));
            }else {
                BigDecimal bigDecimal30 = new BigDecimal(moneyAndPayCountMonth[0]);
                bigDecimal30.setScale(2);
                bigDecimal30 = bigDecimal30.divide(new BigDecimal(moneyAndPayCountMonth[1]),2).divide(new BigDecimal("100"));
                avgMoneyMonth = bigDecimal30.toString();
                month.add(Double.parseDouble(avgMoneyMonth));
            }
        }

        for(int j=0;j<today.size();j++) {
            BigDecimal todayDecimal = new BigDecimal(todayAVG);
            todayDecimal = todayDecimal.add(new BigDecimal(today.get(j)));
            todayAVG = todayDecimal.doubleValue();

            BigDecimal threeDayDecimal = new BigDecimal(threeDayAVG);
            threeDayDecimal=threeDayDecimal.add(new BigDecimal(threeDay.get(j)));
            threeDayAVG = threeDayDecimal.doubleValue();

            BigDecimal weekDecimal = new BigDecimal(weekAVG);
            weekDecimal=weekDecimal.add(new BigDecimal(week.get(j)));
            weekAVG = weekDecimal.doubleValue();

            BigDecimal monthDecimal = new BigDecimal(monthAVG);
            monthDecimal=monthDecimal.add(new BigDecimal(month.get(j)));
            monthAVG = monthDecimal.doubleValue();
        }
        JSONObject data = new JSONObject();
        data.put("today",JSONArray.fromObject(today));
        data.put("threeDay",JSONArray.fromObject(threeDay));
        data.put("week",JSONArray.fromObject(week));
        data.put("month",JSONArray.fromObject(month));
        data.put("date",JSONArray.fromObject(date));
        data.put("todayAVG",new BigDecimal(todayAVG).divide(new BigDecimal(today.size()+""),2));
        data.put("threeDayAVG",new BigDecimal(threeDayAVG).divide(new BigDecimal(threeDay.size()+""),2));
        data.put("weekAVG",new BigDecimal(weekAVG).divide(new BigDecimal(week.size()+""),2));
        data.put("monthAVG",new BigDecimal(monthAVG).divide(new BigDecimal(month.size()+""),2));
        object.put("data",data);
        return object.toString();
    }
}
