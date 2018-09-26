package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.dazhongyoutu.DaZhongYouTuSDK;
import com.u8.server.service.UChannelManager;
import com.u8.server.service.UOrderManager;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/13.
 */
@Controller
@Namespace("/pay/dazhongyoutu")
public class DaZhongYouTuPayCallbackAction extends UActionSupport {

    private String cporderid;   //游戏传过来的orderid
    private String orderid;     //SDK平台的订单ID
    private int appid;          //SDK平台的游戏ID
    private int uid;            //用户ID
    private int time;           //时间戳
    private String extinfo;     //游戏传过来的callbackInfo字段的值
    private float amount;       //用户充值金额（人民币元）
    private String serverid;    //服务器ID
    private String charid;      //角色ID
    private int gold;           //游戏币数量

    private String sign;        //签名

    @Autowired
    private UOrderManager orderManager;

    @Autowired
    private UChannelManager channelManager;


    @Action("payCallback")
    public void payCallback() {

        try {
            long orderID = Long.parseLong(this.orderid);
            UOrder order = orderManager.getOrder(orderID);
            if (order == null) {
                Log.d("The order is null or the channel is null.orderID:%s", orderID);
                this.renderState(false);
                return;
            }

            UChannel channel = channelManager.queryChannel(order.getChannelID());
            if (channel == null) {
                Log.d("The channel is not exists of channelID:" + order.getChannelID());
                this.renderState(false);
                return;
            }

            if (order.getState() > PayState.STATE_PAYING) {
                Log.d("The state of the order is complete. orderID:%s;state:%s", orderID, order.getState());
                this.renderState(true);
                return;
            }

            if (!isSignOK(channel)) {
                Log.d("the sign is not valid. --------- orderID:%s", orderid);
                this.renderState(false);
                return;
            }

            int money = (int) (Float.valueOf(amount) * 100);
            order.setRealMoney(money);
            order.setSdkOrderTime(System.currentTimeMillis() + "");
            order.setCompleteTime(new Date());
            order.setChannelOrderID(orderid);
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            this.renderState(true);
            SendAgent.sendCallbackToServer(this.orderManager, order);
        } catch (Exception e) {
            try {
                renderState(false);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    //==============================================================================
    private void renderState(boolean suc) throws IOException {
        PrintWriter out = this.response.getWriter();
        if (suc) {
            out.write("SUCCESS");
        } else {
            out.write("FAILED");
        }
        out.flush();
    }

    //==============================================================================


    private boolean isSignOK(UChannel channel) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("cporderid", cporderid);
        params.put("orderid", orderid);
        params.put("appid", channel.getAppID() + "");
        params.put("uid", uid + "");
        params.put("channelid", channel.getChannelID() + "");
        params.put("time", System.currentTimeMillis() + "");
        params.put("extinfo", extinfo);
        params.put("money", amount + "");
        params.put("serverid", serverid);
        params.put("charid", charid);
        params.put("gold", gold + "");
        String signLocal = DaZhongYouTuSDK.generateSign(params, channel.getCpPayKey());
        Log.d("the sign local is :" + signLocal);
        return signLocal.equals(this.sign);
    }


    public String getCporderid() {
        return cporderid;
    }

    public void setCporderid(String cporderid) {
        this.cporderid = cporderid;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public int getAppid() {
        return appid;
    }

    public void setAppid(int appid) {
        this.appid = appid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getExtinfo() {
        return extinfo;
    }

    public void setExtinfo(String extinfo) {
        this.extinfo = extinfo;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) { this.amount = amount; }

    public String getServerid() {
        return serverid;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }

    public String getCharid() {
        return charid;
    }

    public void setCharid(String charid) {
        this.charid = charid;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }
}
