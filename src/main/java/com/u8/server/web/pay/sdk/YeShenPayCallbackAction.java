package com.u8.server.web.pay.sdk;

import com.nox.NoxConstant;
import com.nox.entity.KSPayResponseEntity;
import com.nox.notify.NotifyPayResult;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UChannelManager;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.TimeUtils;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Date;

/**
 * 夜神SDK支付回调处理
 * Created by ant on 2016/10/17.
 */

@Controller
@Namespace("/pay/yeshen")
public class YeShenPayCallbackAction extends UActionSupport{

    private String transdata;
    private String sign;
    private int u8ChannelID;

    @Autowired
    private UOrderManager orderManager;

    @Autowired
    private UChannelManager channelManager;


    @Action("payCallback")
    public void payCallback(){
        try{

            UChannel channel = channelManager.queryChannel(this.u8ChannelID);
            if(channel == null){
                Log.e("The channel is not exists. channelID:"+this.u8ChannelID+";data:"+this.transdata);
                this.renderState(false);
                return;
            }

            Log.d("this.transdata:"+this.transdata);
            Log.d("this.sign:"+this.sign);

            NoxConstant.APP_ID = channel.getCpAppID();
            NoxConstant.APP_KEY = channel.getCpAppKey();
            KSPayResponseEntity payInfo = NotifyPayResult.getNotifyResult(this.transdata, sign);
            Log.d("yeshen pay notify data:%s", payInfo);

            long orderID = Long.parseLong(payInfo.getGoodsOrderId());

            UOrder order = orderManager.getOrder(orderID);

            if(order == null){
                Log.d("The order is null");
                this.renderState(false);
                return;
            }

            if(payInfo.getErrNum() != NoxConstant.SUCCESS){
                Log.e("yeshen pay notify result error. orderID:%s; code:%s;msg:%s", orderID, payInfo.getErrNum(), payInfo.getErrMsg());
                this.renderState(false);
                return;
            }

            if(payInfo.getPayStatus() != 2){
                Log.e("yeshen pay notify status error. orderID:%s; status:%s", orderID, payInfo.getPayStatus());
                this.renderState(false);
                return;
            }

            if(order.getState() > PayState.STATE_PAYING) {
                Log.d("The state of the order is complete. orderID:%s; The state: %s ", orderID, order.getState());
                this.renderState(true);
                return;
            }


            int moneyInt = Integer.valueOf(payInfo.getOrderMoney()+"");

            order.setRealMoney(moneyInt);
            order.setSdkOrderTime(TimeUtils.format_default(payInfo.getOrderTime()));
            order.setCompleteTime(new Date());
            order.setChannelOrderID(payInfo.getOrderId());
            order.setState(PayState.STATE_SUC);

            orderManager.saveOrder(order);

            SendAgent.sendCallbackToServer(this.orderManager, order);
            renderState(true);

        }catch(Exception e){
            try {
                renderState(false);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private void renderState(boolean suc) throws IOException {

        JSONObject json = new JSONObject();
        if(suc){
            json.put("status", "success");
            json.put("desc", "通知成功");
        }else{
            json.put("status", "failed");
            json.put("desc", "CP处理失败");
        }

        renderJson(json.toString());
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

    public int getU8ChannelID() {
        return u8ChannelID;
    }

    public void setU8ChannelID(int u8ChannelID) {
        this.u8ChannelID = u8ChannelID;
    }
}
