package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.anfeng.AnFengSDK;
import com.u8.server.service.UChannelManager;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.TimeUtils;
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
 * 爱游戏支付回调
 * 开启断代支付和没有开启断代支付，有区别
 * 爱游戏支付回调，同一个地址，分为 IF1和IF2两个接口，IF1用于客户端断代支付二次确认，IF2是支付完成回调
 * Created by ant on 2016/11/25.
 */

@Controller
@Namespace("/pay/egame")
public class EGamePayCallbackAction extends UActionSupport {

    private String cp_order_id 	; //M 	String 	即PAY_PARAMS_KEY_CP_PARAMS，CP业务流水号，由CP在游戏中支付发起时生成，CP应保证每次支付此流水号唯一不重复，并能依据此流水号区分支付产生的游戏、道具、价格、用户等相关信息 ，长度32位以内
    private String correlator 	; //M 	String 	爱游戏平台流水号（32位以内）
    private String order_time 	; //M 	String 	订单时间戳，14位时间格式(yyyyMMddHHmmss)
    private String method 	    ; //M 	String 	固定值“check”
    private String sign 	    ; //M 	String 	MD5(cp_order_id+correlator+order_time+method+appKey)
    private String version 	    ; //M 	String 	回调接口版本号：当前为1

    //IF2 多的三个参数； IF1没有下面三个参数
    private String result_code 	; //M 	String 为扣费成功，其他状态码均为扣费不成功请勿发放道具，详见附录
    private String fee;         //计费金额，单位：元，服务器端请务必自行校验订购金额和计费金额是否一致
    private String pay_type; 	//M 	String 	计费类型，smsPay：短代；alipay：支付宝；ipay：爱贝


    @Autowired
    private UOrderManager orderManager;

    @Autowired
    private UChannelManager channelManager;

    @Action("payCallback")
    public void payCallback(){
        if("check".equals(method)){
            payCallbackF1();
        }else if("callback".equals(method)){
            payCallbackF2();
        }
    }

    private void payCallbackF1(){

        try{

            long orderID = Long.parseLong(this.cp_order_id);

            UOrder order = orderManager.getOrder(orderID);

            if(order == null){
                Log.d("The order is null or the channel is null.orderID:%s", orderID);
                return;
            }

            UChannel channel = channelManager.queryChannel(order.getChannelID());
            if(channel == null){
                Log.d("The channel is not exists of channelID:"+order.getChannelID());
                this.renderStateF1(order, false);
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(cp_order_id).append(correlator).append(order_time).append(method)
                    .append(channel.getCpAppSecret());

            String md5 = EncryptUtils.md5(sb.toString()).toLowerCase();

            Log.d("check sign str:%s",sb.toString());
            Log.d("md5 local:%s", md5);
            Log.d("sign : %s", sign);

            if(!md5.equals(this.sign)){
                Log.d("the egame callback if1 check sign failed.");
                renderStateF1(order, false);
                return;
            }

            renderStateF1(order, true);


        }catch (Exception e){
            Log.e(e.getMessage());
            e.printStackTrace();
        }

    }

    private void payCallbackF2(){
        try{

            long orderID = Long.parseLong(this.cp_order_id);

            UOrder order = orderManager.getOrder(orderID);

            if(order == null){
                Log.d("The order is null or the channel is null.orderID:%s", orderID);
                this.renderStateF2(false);
                return;
            }

            UChannel channel = channelManager.queryChannel(order.getChannelID());
            if(channel == null){
                Log.d("The channel is not exists of channelID:"+order.getChannelID());
                this.renderStateF2(false);
                return;
            }


            StringBuilder sb = new StringBuilder();
            sb.append(cp_order_id).append(correlator).append(result_code).append(fee).append(pay_type).append(method)
                    .append(channel.getCpAppSecret());

            String md5 = EncryptUtils.md5(sb.toString()).toLowerCase();

            Log.d("check sign str:%s",sb.toString());
            Log.d("md5 local:%s", md5);
            Log.d("sign : %s", sign);

            if(!md5.equals(this.sign)){
                Log.d("the egame callback if1 check sign failed.");
                this.renderStateF2(false);
                return;
            }

            int money = (int)(Float.valueOf(fee) * 100);
            order.setRealMoney(money);
            order.setCompleteTime(new Date());
            order.setChannelOrderID(correlator);
            order.setState(PayState.STATE_SUC);

            orderManager.saveOrder(order);
            this.renderStateF2(true);

            SendAgent.sendCallbackToServer(this.orderManager, order);

        }catch (Exception e){
            this.renderStateF2(false);
            e.printStackTrace();
        }
    }


    private void renderStateF1(UOrder order, boolean suc){

        StringBuilder sb = new StringBuilder();
        sb.append("<sms_pay_check_resp>");
            sb.append("<cp_order_id>" + cp_order_id + "</cp_order_id>");
            sb.append("<correlator>" + correlator + "</correlator>");
            sb.append("<game_account>" + order.getUserID() + "</game_account>");
            sb.append("<fee>" + fee + "</fee>");

        if(suc){
            sb.append("<if_pay>0</if_pay>");
        }else{
            sb.append("<if_pay>1</if_pay>");
        }
        sb.append("<order_time>" + TimeUtils.format_yyyyMMddHHmmss(new Date()) + "</order_time>");

        sb.append("</sms_pay_check_resp>");

        super.renderText(sb.toString());
    }

    private void renderStateF2(boolean suc){

        StringBuilder sb = new StringBuilder();
        sb.append("<cp_notify_resp>");
        if(suc){
            sb.append("<h_ret>0</h_ret>");
        }else{
            sb.append("<h_ret>1</h_ret>");
        }

        sb.append("<correlator>" + correlator + "</correlator>");
        sb.append("<cp_order_id>" + cp_order_id + "</cp_order_id>");

        sb.append("</cp_notify_resp>");

        super.renderText(sb.toString());
    }


    public String getCp_order_id() {
        return cp_order_id;
    }

    public void setCp_order_id(String cp_order_id) {
        this.cp_order_id = cp_order_id;
    }

    public String getCorrelator() {
        return correlator;
    }

    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    public String getOrder_time() {
        return order_time;
    }

    public void setOrder_time(String order_time) {
        this.order_time = order_time;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getResult_code() {
        return result_code;
    }

    public void setResult_code(String result_code) {
        this.result_code = result_code;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getPay_type() {
        return pay_type;
    }

    public void setPay_type(String pay_type) {
        this.pay_type = pay_type;
    }
}
