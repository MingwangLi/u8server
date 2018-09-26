package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;

/**
 * 海马SDK支付回调处理类
 * Created by ant on 2016/4/23.
 */
@Namespace("/pay/haima")
public class HaimaPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(HaimaPayCallbackAction.class);

    @Autowired
    private UOrderManager orderManager;

    private String notify_time;
    private String appid;
    private String out_trade_no;
    private String total_fee;
    private String subject;
    private String body;
    private String trade_status;
    private String sign;
    private String user_param;

    @Action("payCallback")
    public void payCallback(){
        try{
            logger.debug("----海马支付回调参数notify_time:{}",notify_time);
            logger.debug("----海马支付回调参数appid:{}",appid);
            logger.debug("----海马支付回调参数out_trade_no:{}",out_trade_no);
            logger.debug("----海马支付回调参数total_fee:{}",total_fee);
            logger.debug("----海马支付回调参数subject:{}",subject);
            logger.debug("----海马支付回调参数body:{}",body);
            logger.debug("----海马支付回调参数trade_status:{}",trade_status);
            logger.debug("----海马支付回调参数sign:{}",sign);
            logger.debug("----海马支付回调参数user_param:{}",user_param);
            long orderID = Long.parseLong(out_trade_no);
            UOrder order = orderManager.getOrder(orderID);
            if(order == null){
                logger.debug("----海马支付回调");
                this.renderState(false);
                return;
            }

            UChannel channel = order.getChannel();
            if(channel == null){
                Log.d("the channel is null.");
                this.renderState(false);
                return;
            }

            if(order.getState() > PayState.STATE_PAYING) {
                Log.d("The state of the order is complete. The state is " + order.getState());
                this.renderState(true);
                return;
            }

            if(!trade_status.equals("1")){
                Log.d("the order pay faild on sdk side.orderID:"+out_trade_no);
                this.renderState(true);
                return;
            }

            if(!isSignOK(channel)){
                Log.d("The sign verify failed.sign:%s;appKey:%s;orderID:%s", sign, channel.getCpPayKey(), out_trade_no);
                this.renderState(false);
                return;
            }
            BigDecimal bigDecimal1 = new BigDecimal(total_fee);
            bigDecimal1.setScale(2);
            BigDecimal bigDecimal2 = new BigDecimal("100");
            bigDecimal1 = bigDecimal1.multiply(bigDecimal2);
            order.setRealMoney(bigDecimal1.intValue());
            order.setSdkOrderTime(notify_time);
            order.setCompleteTime(new Date());
            //order.setChannelOrderID();
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            renderState(true);

        }catch(Exception e){
            try {
                renderState(false);
                //MailUtils.getInstance().sendMail("3462951792@qq.com","海马支付回调异常","订单id:"+out_trade_no + " 异常信息:"+e.getMessage());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private boolean isSignOK(UChannel channel){

        try{

            StringBuilder sb = new StringBuilder();

            sb.append("notify_time=").append(URLEncoder.encode(notify_time, "UTF-8"))
                    .append("&appid=").append(URLEncoder.encode(appid, "UTF-8"))
                    .append("&out_trade_no=").append(URLEncoder.encode(out_trade_no, "UTF-8"))
                    .append("&total_fee=").append(URLEncoder.encode(total_fee, "UTF-8"))
                    .append("&subject=").append(URLEncoder.encode(subject, "UTF-8"))
                    .append("&body=").append(URLEncoder.encode(body, "UTF-8"))
                    .append("&trade_status=").append(URLEncoder.encode(trade_status, "UTF-8")).append(channel.getCpAppKey());


            Log.d("sign txt:"+sb.toString());

            String md5 = EncryptUtils.md5(sb.toString()).toLowerCase();

            Log.d("md5:"+md5);

            return md5.equals(this.sign);

        }catch (Exception e){
            e.printStackTrace();
        }

        return false;

    }

    private void renderState(boolean suc) throws IOException {

        String res = "success";
        if(!suc){
            res = "fail";
        }

        renderText(res);
    }

    public String getNotify_time() {
        return notify_time;
    }

    public void setNotify_time(String notify_time) {
        this.notify_time = notify_time;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getTotal_fee() {
        return total_fee;
    }

    public void setTotal_fee(String total_fee) {
        this.total_fee = total_fee;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTrade_status() {
        return trade_status;
    }

    public void setTrade_status(String trade_status) {
        this.trade_status = trade_status;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getUser_param() {
        return user_param;
    }

    public void setUser_param(String user_param) {
        this.user_param = user_param;
    }
}
