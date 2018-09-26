package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.uc.PayCallbackResponse;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.JsonUtils;
import com.u8.server.utils.StringUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 手游村支付回调
 * Created by ant on 2016/11/21.
 */
@Controller
@Namespace("/pay/shouyoucun")
public class ShouYouCunPayCallbackAction extends UActionSupport{


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

            Log.d("ShouYouCun Pay Callback . request params:" + sb.toString());

            PayCallbackData rsp = (PayCallbackData) JsonUtils.decodeJson(sb.toString(), PayCallbackData.class);

            if(rsp == null){
                this.renderState(false);
                return;
            }

            if(!"2".equals(rsp.getOrder_status())){
                Log.e("The order %s sdk returned status is %s, not success.", rsp.getAttach(), rsp.getOrder_status());
                this.renderState(false);
                return;
            }

            long orderID = Long.parseLong(rsp.getAttach());

            UOrder order = orderManager.getOrder(orderID);

            if(order == null){
                Log.d("The order is null. orderID:%s", rsp.getAttach());
                this.renderState(false);
                return;
            }

            UChannel channel = order.getChannel();
            if(channel == null){
                Log.d("the channel is null. orderID:%s", rsp.getAttach());
                this.renderState(false);
                return;
            }

            if(order.getState() > PayState.STATE_PAYING) {
                Log.d("The state of the order is complete. orderID:%s;state:%s", orderID, order.getState());
                this.renderState(true);
                return;
            }

            if(!isSignOK(channel, rsp)){
                Log.d("The sign verify failed.sign:%s;appKey:%s;orderID:%s", rsp.getSign(), channel.getCpAppKey(), orderID);
                this.renderState(false);
                return;
            }

            int moneyInt = (int)(Float.valueOf(rsp.getMoney()) * 100);

            order.setRealMoney(moneyInt);
            order.setSdkOrderTime("");
            order.setCompleteTime(new Date());
            order.setChannelOrderID(rsp.getOrder_id());
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

    private static boolean isSignOK(UChannel channel, PayCallbackData data){

        Map<String,Object> params = new LinkedHashMap<String,Object>(); //保证输入顺序和输出顺序相同，这里试用LinkedHashMap
        params.put("order_id", data.getOrder_id());
        params.put("mem_id", data.getMem_id());
        params.put("app_id", data.getApp_id());
        params.put("money", data.getMoney());
        params.put("order_status",data.getOrder_status());
        params.put("paytime", data.getPaytime());
        params.put("attach", data.getAttach());

        String signStr = StringUtils.generateUrlParamString(params, "&", true);
        signStr += "&app_key=" + channel.getCpAppKey();
        String signLocal = EncryptUtils.md5(signStr).toLowerCase();

        Log.d("signStr:%s", signStr);
        Log.d("md5Local:%s", signLocal);

        return signLocal.equals(data.getSign());
    }

    private void renderState(boolean suc) throws IOException {

        if(suc){
            renderText("SUCCESS");
        }else{
            renderText("FAILURE");
        }

    }

    public static class PayCallbackData{
        private String order_id	        ; //string	订单号
        private String mem_id	        ; //string	玩家ID
        private String app_id	        ; //string	游戏ID
        private String money	        ; //string	充值金额 (单位：元)
        private String order_status	    ; //string	1 未支付  2成功支付 3支付失败
        private String paytime	        ; //string	时间戳, Unix timestamp
        private String attach	        ; //string	CP扩展参数,建议为英文与数字，CP用于校验此订单合法性
        private String sign	            ; //string	使用APP_KEY 对所有的参数md5加密串，用于与接口生成的验证串做比较，保证计费通知的合法性。

        public String getOrder_id() {
            return order_id;
        }

        public void setOrder_id(String order_id) {
            this.order_id = order_id;
        }

        public String getMem_id() {
            return mem_id;
        }

        public void setMem_id(String mem_id) {
            this.mem_id = mem_id;
        }

        public String getApp_id() {
            return app_id;
        }

        public void setApp_id(String app_id) {
            this.app_id = app_id;
        }

        public String getMoney() {
            return money;
        }

        public void setMoney(String money) {
            this.money = money;
        }

        public String getOrder_status() {
            return order_status;
        }

        public void setOrder_status(String order_status) {
            this.order_status = order_status;
        }

        public String getPaytime() {
            return paytime;
        }

        public void setPaytime(String paytime) {
            this.paytime = paytime;
        }

        public String getAttach() {
            return attach;
        }

        public void setAttach(String attach) {
            this.attach = attach;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }
    }

    public int getU8ChannelID() {
        return u8ChannelID;
    }

    public void setU8ChannelID(int u8ChannelID) {
        this.u8ChannelID = u8ChannelID;
    }
}
