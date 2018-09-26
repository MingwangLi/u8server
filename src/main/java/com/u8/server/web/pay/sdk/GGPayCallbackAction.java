package com.u8.server.web.pay.sdk;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.gg.GGPayResult;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.JsonUtils;
import com.u8.server.utils.RSAUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.commons.lang.CharEncoding;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * GG游戏SDK
 * Created by lizhong on 2017/10/20.
 */
@Controller
@Namespace("/pay/gg")
public class GGPayCallbackAction extends UActionSupport{
    private String transdata;
    private String sign;
    private String signtype;
    @Autowired
    private UOrderManager orderManager;
    @Action("payCallback")
    public void payCallback() throws IOException {
        GGPayResult rsp = (GGPayResult) JsonUtils.decodeJson(transdata, GGPayResult.class);
        if (rsp == null) {
            Log.e("----GG pay callBack message is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(rsp.getCporderid());
        UOrder order = orderManager.getOrder(orderID);
        if (order == null || order.getChannel() == null) {
            Log.d("----The order is null or the channel is null ");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("----The state of the order is complete  |  The state is " + order.getState());
            this.renderState(true);
            return;
        }
        if(!verifyRes(transdata,sign,signtype,order.getChannel())){
            Log.d("----The verifyRes is error ");
            this.renderState(false);
            return;
        }
        float money = rsp.getMoney() * 100;
        order.setRealMoney((int) money);
        order.setSdkOrderTime(rsp.getTranstime());
        order.setCompleteTime(new Date());
        order.setChannelOrderID(rsp.getTransid());
        order.setState(PayState.STATE_SUC);
        orderManager.saveOrder(order);
        SendAgent.sendCallbackToServer(this.orderManager, order);
        renderState(true);
    }
    public static boolean verifyRes(String transdata, String sign , String signtype, UChannel channel){
        if("RSA".equals(signtype)) {
           if(RSAUtils.verify(transdata,sign,channel.getCpPayKey(), CharEncoding.UTF_8)){
               return true;
           }
           return false;
        }
        return false;
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

    public String getSigntype() {
        return signtype;
    }

    public void setSigntype(String signtype) {
        this.signtype = signtype;
    }
}
