package com.u8.server.web.analytics;

import com.u8.server.cache.CacheManager;
import com.u8.server.common.Page;
import com.u8.server.common.UActionSupport;
import com.u8.server.data.analytics.GameChannelTSummaryData;
import com.u8.server.data.analytics.TSummary;
import com.u8.server.log.Log;
import com.u8.server.service.analytics.AnalyticsManager;
import com.u8.server.utils.DownloadUtil;
import com.u8.server.utils.TimeUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.poi.xssf.usermodel.*;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 统计分析
 * Created by ant on 2016/8/25.
 */
@Controller
@Namespace("/analytics")
public class AnalyticsAction extends UActionSupport {
    private int page;           //当前请求的页码
    private int rows;           //当前每页显示的行数

    private Integer appID;
    private Integer timeType;
    private Date beginTime;
    private Date endTime;


    @Autowired
    private AnalyticsManager summaryManager;

    @Action(value = "summary", results = {@Result(name = "success", location = "/WEB-INF/admin/t_summary.jsp")})
    public String showSummary() {
        return "success";
    }

    @Action(value = "newUsers", results = {@Result(name = "success", location = "/WEB-INF/admin/t_newuser.jsp")})
    public String showNewUsers() {
        return "success";
    }

    @Action(value = "dau", results = {@Result(name = "success", location = "/WEB-INF/admin/t_dau.jsp")})
    public String showDAUData() {
        return "success";
    }

    @Action(value = "retention", results = {@Result(name = "success", location = "/WEB-INF/admin/t_retention.jsp")})
    public String showRetentionData() {
        return "success";
    }

    @Action(value = "flow", results = {@Result(name = "success", location = "/WEB-INF/admin/t_flow.jsp")})
    public String showFlowData() {
        return "success";
    }

    @Action(value = "money", results = {@Result(name = "success", location = "/WEB-INF/admin/t_money.jsp")})
    public String showMoneyData() {
        return "success";
    }

    @Action(value = "pay", results = {@Result(name = "success", location = "/WEB-INF/admin/t_pay.jsp")})
    public String showPayData() {
        return "success";
    }

    @Action(value = "payratio", results = {@Result(name = "success", location = "/WEB-INF/admin/t_payratio.jsp")})
    public String showPayRatioData() {
        return "success";
    }

    @Action(value = "currTime", results = {@Result(name = "success", location = "/WEB-INF/admin/t_analytics.jsp")})
    public String showCurrTimeData() {
        return "success";
    }

    @Action(value = "gameSummaryData", results = {@Result(name = "success", location = "/WEB-INF/admin/t_gameData.jsp")})
    public String showGameSummaryData(){
        return "success";
    }

    @Action("runTestData")
    public void runTestData() {
        renderState(true, "11");
    }

    @Action("summaryData")
    public void getSummaryData() {
        try {

            Date from = null;
            Date to = new Date();

            switch (timeType) {
                case 1:
                    Date lastDay = TimeUtils.lastDay();
                    from = TimeUtils.dateBegin(lastDay);
                    break;
                case 2:
                    Date lastWeek = TimeUtils.dateSub(new Date(), 7);
                    from = TimeUtils.dateBegin(lastWeek);
                    break;
                case 3:
                    Date lastMonth = TimeUtils.dateSub(new Date(), 30);
                    from = TimeUtils.dateBegin(lastMonth);
                    break;
            }

            String json = summaryManager.collectSummaryInfo(appID, from, to);
            Log.d("the json data is ");
            Log.d(json);
            renderState(true, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Action("newUserData")
    public void getNewUserData() {
        try {

            if (this.beginTime == null || this.endTime == null) {
                renderState(false, "请指定时间段");
                return;
            }
            String json = summaryManager.collectNewUserInfo(appID, this.beginTime, this.endTime);
            Log.d("the json data is ");
            Log.d(json);
            renderState(true, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Action("dauData")
    public void getDAUData() {
        try {

            if (this.beginTime == null || this.endTime == null) {
                renderState(false, "请指定时间段");
                return;
            }
            String json = summaryManager.collectDAUInfo(appID, this.beginTime, this.endTime);
            Log.d("the json data is ");
            Log.d(json);
            renderState(true, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Action("retentionData")
    public void getRetentionData() {
        try {

            if (this.beginTime == null || this.endTime == null) {
                renderState(false, "请指定时间段");
                return;
            }
            String json = summaryManager.collectRetentionInfo(appID, this.beginTime, this.endTime);
            Log.d("the json data is ");
            Log.d(json);
            renderState(true, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Action("flowData")
    public void getFlowData() {
        try {

            if (this.beginTime == null || this.endTime == null) {
                renderState(false, "请指定时间段");
                return;
            }
            String json = summaryManager.collectFlowInfo(appID, this.beginTime, this.endTime, this.timeType);
            Log.d("the json data is ");
            Log.d(json);
            renderState(true, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Action("moneyData")
    public void getMoneyData() {
        try {

            if (this.beginTime == null || this.endTime == null) {
                renderState(false, "请指定时间段");
                return;
            }

            String json = summaryManager.collectMoneyInfo(appID, this.beginTime, this.endTime);
            Log.d("the json data is ");
            Log.d(json);
            renderState(true, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Action("payData")
    public void getPayData() {
        try {

            if (this.beginTime == null || this.endTime == null) {
                renderState(false, "请指定时间段");
                return;
            }
            String json = summaryManager.collectPayInfo(appID, this.beginTime, this.endTime);
            Log.d("the json data is ");
            Log.d(json);
            renderState(true, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Action("payRatioData")
    public void getPayRatioData() {
        try {
            if (this.beginTime == null || this.endTime == null) {
                renderState(false, "请指定时间段");
                return;
            }
            String json = summaryManager.collectPayRatioInfo(appID, this.beginTime, this.endTime);
            Log.d("the json data is ");
            Log.d(json);
            renderState(true, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //实时数据
    @Action("currTimeData")
    public void getCurrTimeData(){
        if (this.beginTime == null || this.endTime == null) {
            renderState(false, "请指定时间段");
            return;
        }
        //Calendar calendar = Calendar.getInstance();
        //calendar.setTime(endTime);
        //calendar.add(Calendar.HOUR,24);
        String game_datas = super.request.getParameter("game_datas");
        String masterName = super.request.getParameter("masterName");
        //收益
        JSONArray profit_json = summaryManager.collectProfitInfo(appID, this.beginTime, this.endTime, game_datas, masterName);
        //新增
        JSONArray newreg_json = summaryManager.collectNewRegInfo(appID, this.beginTime, this.endTime, game_datas, masterName);
        JSONArray jsonArray = new JSONArray();
        List<Integer> appIDs = new ArrayList<>();
        List<Integer> channelIDs = new ArrayList<>();
        for(int i = 0;i < profit_json.size()-2;i++) {
            GameChannelTSummaryData gameChannelTSummaryData = new GameChannelTSummaryData();
            JSONObject profitObject = (JSONObject)profit_json.get(i);
            gameChannelTSummaryData.setAppID(profitObject.getInt("appID"));
            gameChannelTSummaryData.setChannelID(profitObject.getInt("channelID"));
            gameChannelTSummaryData.setAppName(profitObject.getString("appName"));
            gameChannelTSummaryData.setChannelName(profitObject.getString("channelName"));
            gameChannelTSummaryData.setTotalMoney(profitObject.getString("totalMoney").substring(0,profitObject.getString("totalMoney").indexOf(".")));
            gameChannelTSummaryData.setTotalCostNum(profitObject.getString("totalCostNum"));
            Integer appID = profitObject.getInt("appID");
            Integer channelID = profitObject.getInt("channelID");
            boolean flag = true;
            for(int j = 0;j < newreg_json.size()-2;j++) {
                JSONObject newregObject = (JSONObject) newreg_json.get(j);
                Integer id = newregObject.getInt("appID");
                Integer newregeChannelID = newregObject.getInt("channelID");
                if (appID == id && channelID.intValue() == newregeChannelID.intValue()) {
                    appIDs.add(id);
                    channelIDs.add(channelID);
                    gameChannelTSummaryData.setTotalNewreg(newregObject.getString("totalNewreg"));
                    gameChannelTSummaryData.setTotalNewCostNum(newregObject.getString("totalNewCostNum"));
                    gameChannelTSummaryData.setPayOfRegistRate(gameChannelTSummaryData.caculate(gameChannelTSummaryData.getTotalNewCostNum(),gameChannelTSummaryData.getTotalNewreg()));
                    flag = false;
                    break;
                }
            }
            if (flag) {
                gameChannelTSummaryData.setTotalNewreg("0");
                gameChannelTSummaryData.setTotalNewCostNum("0");
                gameChannelTSummaryData.setPayOfRegistRate("0.00%");
            }
            jsonArray.add(gameChannelTSummaryData);
        }

        for(int j = 0;j < newreg_json.size()-2;j++) {
            JSONObject newregObject = (JSONObject) newreg_json.get(j);
            Integer id = newregObject.getInt("appID");
            Integer channelID = newregObject.getInt("channelID");
            boolean flag = true;
            for(int i = 0;i < channelIDs.size(); i++) {
                if((appIDs.get(i) == id && channelIDs.get(i).intValue() == channelID.intValue())) {
                    //重复的数据 已经合并的
                    flag = false;
                    break;
                }
            }
            if (flag) {
                //遍历所有的 都没有匹配的 需要添加
                GameChannelTSummaryData gameChannelTSummaryData = new GameChannelTSummaryData();
                gameChannelTSummaryData.setAppID(id);
                gameChannelTSummaryData.setAppName(newregObject.getString("appName"));
                gameChannelTSummaryData.setChannelID(newregObject.getInt("channelID"));
                gameChannelTSummaryData.setChannelName(newregObject.getString("channelName"));
                gameChannelTSummaryData.setTotalMoney("0");
                gameChannelTSummaryData.setTotalCostNum("0");
                gameChannelTSummaryData.setTotalNewreg(newregObject.getString("totalNewreg"));
                gameChannelTSummaryData.setTotalNewCostNum(newregObject.getString("totalNewCostNum"));
                gameChannelTSummaryData.setPayOfRegistRate(gameChannelTSummaryData.caculate(gameChannelTSummaryData.getTotalNewCostNum(),gameChannelTSummaryData.getTotalNewreg()));
                jsonArray.add(gameChannelTSummaryData);
            }
        }

        JSONObject json = new JSONObject();
        //json.put("profit_json",profit_json);
        //json.put("newreg_json",newreg_json);
        json.put("profitAndNewReg",jsonArray.toString());
        json.put("totalMoney",profit_json.get(profit_json.size()-2));
        json.put("totalCostNum",profit_json.get(profit_json.size()-1));
        json.put("totalNewreg",newreg_json.get(newreg_json.size()-2));
        json.put("totalNewCostNum",newreg_json.get(newreg_json.size()-1));
        Log.d("----profitAndNewReg:%s",jsonArray.toString());
        renderState(true, json.toString());
    }

    //统计数据Start
    @Action("getGameSummaryData")
    public void getGameSummaryData(){
        try{

            Page<TSummary> pages = summaryManager.getSummaryPage(page, rows, appID, beginTime, endTime);
            List<TSummary> list = pages.getResultList();
            for (TSummary tSummary:list) {
                tSummary.setArppu(caculate(tSummary.getMoney(),tSummary.getPayUserNum()));
                tSummary.setArpu(caculate(tSummary.getMoney(),tSummary.getDau()));
                tSummary.setPayRate(caculatePay(tSummary.getPayUserNum(),tSummary.getDau()));
            }
            pages.setResultList(list);
            JSONObject json = new JSONObject();

            JSONArray users = new JSONArray();
            if(pages != null){
                json.put("total", pages.getTotalCount());
                for(TSummary m : pages.getResultList()){
                    users.add(m.toJSON());
                }
            }

            json.put("rows", users);

            renderJson(json.toString());


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //统计数据End
    @SuppressWarnings("all")
    @Action("/downloadSummary")
    public void downloadSummary() {
        OutputStream os = null;
        try{
            os = this.response.getOutputStream();
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("统计数据模板.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(is);
            XSSFSheet sheet = workbook.getSheetAt(0);
            XSSFRow row = sheet.getRow(0);
            XSSFCell cell = row.getCell(0);
            String title = CacheManager.getInstance().getGame(appID).getName()+"最近一个月统计数据";
            cell.setCellValue(title);
            cell.getCellStyle().setAlignment(XSSFCellStyle.ALIGN_CENTER_SELECTION);
            row = sheet.getRow(1);
            List<XSSFCellStyle> cellStyleList = new ArrayList<>();
            for(int i = 0;i < 17;i++) {
                cell = row.getCell(i);
                XSSFCellStyle cellStyle = cell.getCellStyle();
                cellStyleList.add(cellStyle);
            }
            is.close();
            List<TSummary> list = summaryManager.getSummaryPageAll(appID, beginTime, endTime);
            for (TSummary tSummary:list) {
                tSummary.setArppu(caculate(tSummary.getMoney(),tSummary.getPayUserNum()));
                tSummary.setArpu(caculate(tSummary.getMoney(),tSummary.getDau()));
                tSummary.setPayRate(caculatePay(tSummary.getPayUserNum(),tSummary.getDau()));
            }
            Integer maxRow = 1048576;
            if(list.size() >= maxRow) {
                Log.d("----下载订单失败,数据量过大");
                String content = "下载订单失败,数据量过大,请重新筛选";
                os.write(content.getBytes("UTF-8"));
                return;
            }
            List<String> values = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for(int i = 2;i < list.size()+2;i++) {
                row = sheet.createRow(i);
                TSummary tSummary = list.get(i-2);
                values.add(sdf.format(tSummary.getCurrTime()));
                values.add(CacheManager.getInstance().getGame(tSummary.getAppID()).getName());
                values.add(tSummary.getDeviceNum()+"");
                values.add(tSummary.getUserNum()+"");
                values.add(tSummary.getUniUserNum()+"");
                values.add(tSummary.getTotalUserNum()+"");
                values.add(tSummary.getDau()+"");
                values.add(tSummary.getNdau()+"");
                values.add(tSummary.getWau()+"");
                values.add(tSummary.getMau()+"");
                values.add(tSummary.getPayUserNum()+"");
                values.add(tSummary.getTotalPayUserNum()+"");
                values.add(tSummary.getNewPayUserNum()+"");
                values.add((tSummary.getMoney()/100)+".00");
                values.add(tSummary.getArppu()+"");
                values.add(tSummary.getArpu()+"");
                values.add(tSummary.getPayRate()+"");
                for (int j = 0;j<17;j++) {
                    cell = row.createCell(j);
                    XSSFCellStyle style = cellStyleList.get(j);
                    XSSFCellStyle cellStyle = workbook.createCellStyle();
                    cellStyle.setBorderBottom(style.getBorderBottom());
                    cellStyle.setBorderLeft(style.getBorderLeft());
                    cellStyle.setBorderTop(style.getBorderTop());
                    cellStyle.setBorderRight(style.getBorderRight());
                    cell.setCellStyle(cellStyle);
                    cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
                    cell.setCellValue(values.get(j));
                }
                values.clear();
            }
            list.clear();
            String fileName = "统计数据.xlsx";
            DownloadUtil downloadUtil = new DownloadUtil();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            downloadUtil.download(outputStream,this.response,fileName);
        }catch(Exception e) {
            Log.e("-----下载订单失败:"+e.getMessage());
            return ;
        }
    }


    //保留两位小数
    private Double caculate(Long money,Integer number) {
        if (0 == number) {
            return 0D;
        }
        money = money/100;
        BigDecimal bigDecimal = new BigDecimal(money.doubleValue());
        bigDecimal = bigDecimal.divide(new BigDecimal(number.doubleValue()),2,BigDecimal.ROUND_HALF_UP);
        return bigDecimal.doubleValue();
    }

    private Double caculatePay(Integer pay,Integer Regist) {
        if (0 == Regist) {
            return 0D;
        }
        BigDecimal bigDecimal = new BigDecimal(pay.doubleValue());
        bigDecimal = bigDecimal.divide(new BigDecimal(Regist.doubleValue()),2,BigDecimal.ROUND_HALF_UP);
        return bigDecimal.doubleValue();
    }




    private void renderState(boolean suc, String data) {
        JSONObject json = new JSONObject();
        json.put("state", suc ? 1 : 0);
        json.put("data", data);
        renderJson(json.toString());
    }




    public Integer getAppID() {
        return appID;
    }

    public void setAppID(Integer appID) {
        this.appID = appID;
    }

    public Integer getTimeType() {
        return timeType;
    }

    public void setTimeType(Integer timeType) {
        this.timeType = timeType;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
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


}
