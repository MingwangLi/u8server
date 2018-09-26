package com.u8.server.web.admin;

import com.u8.server.common.Page;
import com.u8.server.common.UActionSupport;
import com.u8.server.data.analytics.UUserInfo;
import com.u8.server.service.UUserInfoService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Namespace("/admin")
public class UserInfoController extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(UserInfoController.class);

    @Autowired
    private UUserInfoService uUserInfoService;

    @Action(value = "/userinfo",results = {@Result(name = "userinfo", location = "/WEB-INF/admin/userinfo.jsp")} )
    public String goToUserInfo() {
        return "userinfo";
    }

    private Integer appID;

    private Integer userID;

    private String beginCreateTime;

    private String endCreateTime;

    private Integer rows;

    private Integer page;

    private String roleName;


    public void setAppID(Integer appID) {
        this.appID = appID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public void setBeginCreateTime(String beginCreateTime) {
        this.beginCreateTime = beginCreateTime;
    }

    public void setEndCreateTime(String endCreateTime) {
        this.endCreateTime = endCreateTime;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Action("/uuserinfolist")
    public String getListWithPage() {
        try {
            if (null == page) {
                page  = 1;
            }
            if (null == rows) {
                page  = 20;
            }
            Page<UUserInfo> list = uUserInfoService.selectWithPage(appID,userID,beginCreateTime,endCreateTime,page,rows,roleName);
            JSONObject json = new JSONObject();
            json.put("total",null == list?0:list.getTotalCount());
            JSONArray jsonArray = new JSONArray();
            if (null != list && null != list.getResultList()) {
                for (UUserInfo uUserInfo:list.getResultList()) {
                    jsonArray.add(uUserInfo.toJSONString());
                }
            }
            json.put("rows",jsonArray);
            renderJson(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----查询角色异常,异常信息:{}",e.getMessage());
        }
        return null;
    }
}
