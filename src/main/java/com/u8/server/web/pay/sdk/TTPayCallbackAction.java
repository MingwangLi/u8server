package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.tt.PayCallback;
import com.u8.server.sdk.tt.SignUtils;
import com.u8.server.service.UChannelManager;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.JsonUtils;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;

/**
 * TT 语音SDK支付回调处理类
 * Created by lizhong on 2016/11/10.
 */
@Controller
@Namespace("/pay/tt")
public class TTPayCallbackAction extends UActionSupport {

    private int u8ChannelID;

    @Autowired
    private UOrderManager orderManager;

    @Autowired
    private UChannelManager channelManager;


    @Action("payCallback")
    public void payCallback(){

        try{

            String sign = this.request.getHeader("sign");

            Log.d("the tt pay callback sign is "+sign);

            BufferedReader br = this.request.getReader();
            String line;
            StringBuilder sb = new StringBuilder();
            while((line=br.readLine()) != null){
                sb.append(line);
            }

            String urlData = URLDecoder.decode(sb.toString(), "utf-8");

            Log.d("TT Pay Callback . request params:" + sb.toString());

            UChannel channel = channelManager.queryChannel(this.u8ChannelID);
            if(channel == null){
                Log.e("tt pay callback error. channel is not exists:"+this.u8ChannelID);
                renderState(false);
                return;
            }

            String signLocal = SignUtils.sign(urlData, channel.getCpPayKey());
            if(!signLocal.equals(sign)){
                Log.e("tt pay callback error. sign not matched. signLocal:%s;sign:%s",signLocal, sign);
                renderState(false);
                return;
            }

            PayCallback rsp = (PayCallback) JsonUtils.decodeJson(urlData, PayCallback.class);

            if(rsp == null){
                Log.e("json data decode failed. jsonData:%s", urlData);
                renderState(false);
                return;
            }

            long orderID = Long.parseLong(rsp.getCpOrderId());
            UOrder order = orderManager.getOrder(orderID);

            if(order == null || order.getChannel() == null){
                Log.d("The order is null or the channel is null.");
                this.renderState(false);
                return;
            }

            if(order.getState() > PayState.STATE_PAYING){
                Log.d("The state of the order is complete. The state is " + order.getState());
                this.renderState(true);
                return;
            }

            int moneyInt = (int)(rsp.getPayFee() * 100);  //以分为单位

            order.setRealMoney(moneyInt);
            order.setSdkOrderTime(rsp.getPayDate());
            order.setCompleteTime(new Date());
            order.setChannelOrderID(rsp.getSdkOrderId());
            order.setState(PayState.STATE_SUC);

            orderManager.saveOrder(order);

            SendAgent.sendCallbackToServer(this.orderManager, order);


            renderState(true);

        }catch (Exception e){
            e.printStackTrace();
            try{
                this.renderState(false);
            }catch (Exception e2){
                e2.printStackTrace();
                Log.e(e2.getMessage());
            }

            Log.e(e.getMessage());

        }

    }

    private void renderState(boolean suc) throws IOException {

        JSONObject json = new JSONObject();
        JSONObject head = new JSONObject();
        if(suc){
            head.put("result", 0);
            head.put("message","成功");
        }else{
            head.put("result", -1);
            head.put("message","失败");
        }
        json.put("head", head);
        renderJson(json.toString());
    }

    public int getU8ChannelID() {
        return u8ChannelID;
    }

    public void setU8ChannelID(int u8ChannelID) {
        this.u8ChannelID = u8ChannelID;
    }
}
