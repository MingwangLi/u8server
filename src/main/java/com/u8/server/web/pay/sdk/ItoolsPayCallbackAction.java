package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.itools.ItoolsPayResult;
import com.u8.server.sdk.itools.RSASignature;
import com.u8.server.service.UChannelManager;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.JsonUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.util.Date;

/**
 * ITools支付回调
 * Created by lizhong on 2018/03/23.
 */

@Controller
@Namespace("/pay/itools")
public class ItoolsPayCallbackAction extends UActionSupport{

    private String notify_data;
    private String sign;
    private int u8ChannelID;

    @Autowired
    private UOrderManager orderManager;

    @Autowired
    private UChannelManager channelManager;

    @Action("payCallback")
    public void payCallback() throws Exception {
            UChannel channel = channelManager.queryChannel(this.u8ChannelID);
            if(channel == null){
                Log.e("The channel is not exists. channelID:" + this.u8ChannelID + ";data:" + this.notify_data);
                return;
            }
            String notifyJson = RSASignature.decrypt(this.notify_data);

            boolean verified = RSASignature.verify(notifyJson,sign);

            ItoolsPayResult data = (ItoolsPayResult) JsonUtils.decodeJson(notifyJson, ItoolsPayResult.class);

            if(data == null){
                Log.e("The content parse error...");
                this.renderState(false);
                return;
            }

            long localOrderID = Long.parseLong(data.getOrder_id_com());

            UOrder order = orderManager.getOrder(localOrderID);

            if(order == null || order.getChannel() == null){
                Log.d("The order is null or the channel is null.");
                this.renderState(false);
                return;
            }

            if(order.getState() > PayState.STATE_PAYING){
                Log.d("The state of the order is complete. The state is "+order.getState());
                this.renderState(true);
                return;
            }

            if(!"success".equals(data.getResult())){
                Log.d("平台支付失败 local orderID:"+localOrderID+"; order id:" + data.getOrder_id());
                this.renderState(false);
                return;
            }
            if (!verified){
                Log.d("The sign not match!");
                this.renderState(false);
                return;
            }
            order.setChannelOrderID(data.getOrder_id());
            order.setRealMoney((int) (Float.valueOf(data.getAmount()) * 100));
            order.setSdkOrderTime("");
            order.setCompleteTime(new Date());
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            this.renderState(true);


    }

    private void renderState(boolean suc){
        String res = "success";
        if(!suc){
            res = "fail";
        }
        renderText(res);
    }

    public String getNotify_data() {
        return notify_data;
    }

    public void setNotify_data(String notify_data) {
        this.notify_data = notify_data;
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
