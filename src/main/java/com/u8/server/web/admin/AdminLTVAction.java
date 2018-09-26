package com.u8.server.web.admin;

import com.u8.server.common.UActionSupport;
import com.u8.server.service.analytics.AnalyticsManager;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Namespace("/admin")
public class AdminLTVAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(AdminLTVAction.class);

    private int appID;

    private String beginTime;

    private String endTime;


    @Autowired
    private AnalyticsManager analyticsManager;

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Action(value = "ltv", results = {@Result(name = "ltv", location = "/WEB-INF/admin/ltv.jsp")})
    public String toLTV() {
        return "ltv";
    }

    @Action("getLTVData")
    public void getLTVData() {
        String ltvjson = null;
        try {
            ltvjson = analyticsManager.getLTVData(appID,beginTime,endTime);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("统计ltv数据异常,异常信息:{}",e.getMessage());
        }
       renderJson(ltvjson);
    }
}
