package com.u8.server.web.admin;

import com.opensymphony.xwork2.ModelDriven;
import com.u8.server.cache.CacheManager;
import com.u8.server.common.Page;
import com.u8.server.common.UActionSupport;
import com.u8.server.data.ChargePoint;
import com.u8.server.data.ChargePointVO;
import com.u8.server.data.UAdmin;
import com.u8.server.service.ChargePointManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Date;


/**
 * @Author: lizhong
 * @Des:
 * @Date: 2018/5/7 17:45
 * @Modified:
 */
//@Controller   测试这个注解是否有用 其实控制器是由Struts2扫描实例化的 默认多例
@Namespace("/admin")
public class ChargePointAction extends UActionSupport implements ModelDriven<ChargePoint> {

    private ChargePoint chargePoint;

    @Autowired
    private ChargePointManager chargePointManager;



    @Action(value = "chargePoint", results = {@Result(name = "success", location = "/WEB-INF/admin/chargePoint.jsp")})
    public String chargePoint() {
        return "success";
    }

    @Override
    public ChargePoint getModel() {
       if (null == chargePoint) {
           chargePoint = new ChargePoint();
       }
       return chargePoint;
    }


    private Integer page;

    private Integer rows;

    private String ganme;

    private String channelMaster;


    public void setPage(Integer page) {
        this.page = page;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public void setGanme(String ganme) {
        this.ganme = ganme;
    }

    public void setChannelMaster(String channelMaster) {
        this.channelMaster = channelMaster;
    }

    @Action("chargePointList")
    public void getAllChargePoint() {
        if (null == page) {
            page = 1;
        }
        if (null == rows) {
            rows = 20;
        }
        Page<ChargePoint> data = chargePointManager.getAllChargePointByPage(chargePoint,page,rows,ganme,channelMaster);
        JSONArray array = new JSONArray();
        for (ChargePoint chargePoint:data.getResultList()) {
            ChargePointVO chargePointVO = new ChargePointVO();
            BeanUtils.copyProperties(chargePoint,chargePointVO);
            Integer channelID = chargePoint.getChannelID();
            Integer appID = CacheManager.getInstance().getChannel(channelID).getAppID();
            String channelMaster =  CacheManager.getInstance().getChannel(channelID).getMaster().getMasterName();
            chargePointVO.setGanme(CacheManager.getInstance().getGame(appID).getName());
            chargePointVO.setChannelMaster(channelMaster);
            JSONObject object = JSONObject.fromObject(chargePointVO);
            array.add(object);
        }
        JSONObject object = new JSONObject();
        object.put("total",data.getTotalCount());
        object.put("rows",array);
        renderJson(object.toString());
    }


    @Action("chargePointAdd")
    public void chargePointAdd() {
        if (null == chargePoint.getId()) {
            //新增
            chargePoint.setCreateTime(new Date());
            chargePoint.setUpdateTime(new Date());
            UAdmin uadmin = (UAdmin)this.getSession().get("admin");
            chargePoint.setCreateBy(uadmin.getUsername());
            chargePoint.setStatus(1);
            chargePointManager.save(chargePoint);
        }else {
            //修改  先查询后修改
            ChargePoint editChargePoint = chargePointManager.find(chargePoint.getId());
            editChargePoint.setChannelID(chargePoint.getChannelID());
            editChargePoint.setMoney(chargePoint.getMoney());
            editChargePoint.setChargeCode(chargePoint.getChargeCode());
            editChargePoint.setChannelChargeCode(chargePoint.getChannelChargeCode());
            editChargePoint.setChargeName(chargePoint.getChargeName());
            editChargePoint.setChargeDesc(chargePoint.getChargeDesc());
            editChargePoint.setUpdateTime(new Date());
            chargePointManager.save(editChargePoint);
        }
        renderState(true);

    }

    @Action("chargePointDelete")
    public void chargePointDelete() {
        chargePointManager.delete(chargePoint.getId());
        renderState(true);
    }

    @Action("chargePointEdit")
    public void chargePointEdit() {
        chargePoint = chargePointManager.find(chargePoint.getId());
        renderJson(chargePoint.toJSON().toString());
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

}
