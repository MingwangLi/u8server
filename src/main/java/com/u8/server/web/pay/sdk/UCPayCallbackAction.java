package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.uc.PayCallbackResponse;
import com.u8.server.sdk.uc.UCSDK;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;


/***
 * UC渠道的支付回调请求处理
 */
@Controller
@Namespace("/pay/uc")
public class UCPayCallbackAction extends UActionSupport{

    @Autowired
    private UOrderManager orderManager;

    private int u8ChannelID;

    @Action("payCallback")
    public void payCallback(){
        try{
            BufferedReader br = this.request.getReader();
            String line;
            StringBuilder sb = new StringBuilder();
            while((line=br.readLine()) != null){
                sb.append(line).append("\r\n");
            }
            Log.d("UC Pay Callback . request params:" + sb.toString());
            PayCallbackResponse rsp = (PayCallbackResponse) JsonUtils.decodeJson(sb.toString(), PayCallbackResponse.class);

            if(rsp == null){
                this.renderState(false);
                return;
            }

            long orderID = Long.parseLong(rsp.getData().getCallbackInfo());
            UOrder order = orderManager.getOrder(orderID);

            if(order == null || order.getChannel() == null){
                Log.d("The order is null or the channel is null.");
                this.renderState(false);
                return;
            }
            if(order.getState() > PayState.STATE_PAYING){
                Log.d("The state of the order is complete. The state is "+order.getState());
                this.renderState(false);
                return;
            }
            if(!verifyPay(order.getChannel(), rsp)){
                Log.d("The sign is not matched.");
                this.renderState(true);
                return;
            }
            if("S".equals(rsp.getData().getOrderStatus())){
                float money = Float.parseFloat(rsp.getData().getAmount());
                int moneyInt = (int)(money * 100);  //以分为单位
                order.setRealMoney(moneyInt);
                order.setSdkOrderTime("");
                order.setCompleteTime(new Date());
                order.setChannelOrderID(rsp.getData().getOrderId());
                order.setState(PayState.STATE_SUC);
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(this.orderManager, order);
                renderState(true);
            }else{
                order.setChannelOrderID(rsp.getData().getOrderId());
                order.setState(PayState.STATE_FAILED);
                orderManager.saveOrder(order);
                renderState(false);
            }

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

    private void renderState(boolean suc) throws IOException{

        String res = "SUCCESS";
        if(!suc){
            res = "FAILURE";
        }

        PrintWriter out = this.response.getWriter();
        out.write(res);
        out.flush();
        out.close();
    }

    public int getU8ChannelID() {
        return u8ChannelID;
    }

    public void setU8ChannelID(int u8ChannelID) {
        this.u8ChannelID = u8ChannelID;
    }


    /***
     * 验证支付
     * @param channel
     * @param rsp
     * @return
     */
    public boolean verifyPay(UChannel channel, PayCallbackResponse rsp){

        String signSource= "accountId="+rsp.getData().getAccountId()+"amount="+rsp.getData().getAmount()+"callbackInfo="+rsp.getData().getCallbackInfo();
        if(rsp.getData().getCpOrderId() != null && rsp.getData().getCpOrderId().length() > 0){
            signSource += "cpOrderId="+rsp.getData().getCpOrderId();
        }
        signSource = signSource+"creator="+rsp.getData().getCreator()+"failedDesc="+rsp.getData().getFailedDesc()+"gameId="+rsp.getData().getGameId()
                +"orderId="+rsp.getData().getOrderId()+"orderStatus="+rsp.getData().getOrderStatus()
                +"payWay="+rsp.getData().getPayWay()
                +channel.getCpAppKey();
        return EncryptUtils.md5(signSource).equals(rsp.getSign());

    }
}
