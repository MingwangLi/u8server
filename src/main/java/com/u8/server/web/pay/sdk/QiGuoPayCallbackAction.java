package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.qiguo.QiGuoPayResult;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @Author: lizhong
 * @Date: Created in 14:20 2017/9/1
 * @Description:
 * @Modify By:
 */
@Controller
@Namespace("/pay/qiguo")
public class QiGuoPayCallbackAction extends UActionSupport{
    private String transdata;
    private String sign;
    @Autowired
    private UOrderManager orderManager;
    @Action("payCallback")
    public void payCallback() throws IOException {
        QiGuoPayResult rsp = (QiGuoPayResult) JsonUtils.decodeJson(transdata,QiGuoPayResult.class);
        if (rsp == null) {
            Log.e("------七果 pay callBack | message is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(rsp.getCpprivate());
        UOrder order = orderManager.getOrder(orderID);

        if (order == null || order.getChannel() == null) {
            Log.d("------七果---------The order is null or the channel is null");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("------七果---------The state of the order is complete  |  The state is " + order.getState());
            this.renderState(true);
            return;
        }
        /*if(rsp.getMoney() != order.getMoney()) {
            Log.d("------七果---------The money not match");
            this.renderState(false);
            return;
        }*/
        if(!isSignOK(transdata, order.getChannel())){
            Log.d("------七果---------The sign not match");
            this.renderState(false);
            return;
        }
        order.setRealMoney(rsp.getMoney());//以分为单位
        order.setSdkOrderTime(rsp.getTranstime());
        order.setCompleteTime(new Date());
        order.setChannelOrderID(rsp.getExorderno());
        order.setState(PayState.STATE_SUC);
        orderManager.saveOrder(order);
        SendAgent.sendCallbackToServer(this.orderManager, order);
        this.renderState(true);
    }

    //生成签名
    public boolean isSignOK(String jsonData,UChannel channel) {
        JSONObject json = JSONObject.fromObject(jsonData);
        Map<String,Object> params = json;
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder postdatasb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i).toString();
            String v = String.valueOf(params.get(k));
            postdatasb.append(k + "=" + v + "&");
        }
        postdatasb.deleteCharAt(postdatasb.length() - 1);
        //对排序后的参数附加开发商签名密钥
        String signStr = postdatasb.toString();
        String newSign= EncryptUtils.md5(EncryptUtils.md5(signStr)+ channel.getCpAppKey());
        return sign.equals(newSign);
    }
    private void renderState(boolean suc) {
        String res = "SUCCESS";
        if (!suc) {
            res = "FAILURE";
        }
        PrintWriter out = null;
        try {
            out = this.response.getWriter();
            out.write(res);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            out.flush();
            out.close();
        }
    }

    public String getTransdata() {
        return transdata;
    }

    public void setTransdata(String transdata) {
        this.transdata = transdata;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public UOrderManager getOrderManager() {
        return orderManager;
    }

    public void setOrderManager(UOrderManager orderManager) {
        this.orderManager = orderManager;
    }
}
