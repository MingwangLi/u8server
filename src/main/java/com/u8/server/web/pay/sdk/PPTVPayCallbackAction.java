package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.SignUtils;
import com.u8.server.utils.StringUtils;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * PPTV 支付回调处理类
 * Created by ant on 2016/1/21.
 *
 * 
 */

@Namespace("/pay/pptv")
public class PPTVPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String sid		;   //服务器标识，使用字母与数字的组合
    private String user_id	;   //用户id
    private String trade_no ;   //订单号 char (20), 不允许重复
    private String out_trade_no ; //cp订单号
    private String amount;
    private String gold;
    private String platform;
    private String sub_platform;
    private String time     ;   //充值发起时间，unix 时间戳
    private String sign     ;   //验证串
    private String ext      ;   //拓展信息

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setTrade_no(String trade_no) {
        this.trade_no = trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setGold(String gold) {
        this.gold = gold;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setSub_platform(String sub_platform) {
        this.sub_platform = sub_platform;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    @Autowired
    private UOrderManager orderManager;


    @Action("payCallback")
    public void payCallback(){
        JSONObject object = new JSONObject();
        try{
            logger.info("----PPTV支付回调参数sid:{}",sid);
            logger.info("----PPTV支付回调参数user_id:{}",user_id);
            logger.info("----PPTV支付回调参数trade_no:{}",trade_no);
            logger.info("----PPTV支付回调参数out_trade_no:{}",out_trade_no);
            logger.info("----PPTV支付回调参数amount:{}",amount);
            logger.info("----PPTV支付回调参数gold:{}",gold);
            logger.info("----PPTV支付回调参数platform:{}",platform);
            logger.info("----PPTV支付回调参数sub_platform:{}",sub_platform);
            logger.info("----PPTV支付回调参数time:{}",time);
            logger.info("----PPTV支付回调参数sign:{}",sign);
            logger.info("----ext:{}",ext);
            Map<String,String> params = new HashMap<>();
            params.put("sid",sid);
            params.put("user_id",user_id);
            params.put("trade_no",trade_no);
            params.put("out_trade_no",out_trade_no);
            params.put("amount",amount);
            params.put("gold",gold);
            params.put("platform",platform);
            params.put("sub_platform",sub_platform);
            params.put("time",time);
            params.put("ext",ext);
            if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(out_trade_no)) {
                logger.warn("-----PPTV支付回调参数错误");
                object.put("code",1001);
                object.put("data",null);
                object.put("message","参数sign、out_trade_no错误");
                renderText(object.toString());
                return;
            }
            long orderID = Long.parseLong(out_trade_no);
            UOrder order = orderManager.getOrder(orderID);
            UChannel channel = order.getChannel();
            String key = channel.getCpAppSecret();
            logger.debug("----PPTV支付回调查询key:{}",key);
            String checkSign = SignUtils.createSignWithURLEncode(params,key,"-PPTV支付回调");
            if(null == order || null == channel){
                logger.warn("-----PPTV支付回调查询订单和渠道为null,orderID:{},channelID:{}",orderID,order.getChannelID());
                object.put("code",1001);
                object.put("data",null);
                object.put("message","参数out_trade_no错误");
                renderText(object.toString());
                return;
            }
            if (!sign.equals(checkSign)) {
                logger.warn("-----PPTV支付回调验签失败sign:{}",sign);
                object.put("code",1001);
                object.put("data",null);
                object.put("message","验签失败");
                renderText(object.toString());
                return;
            }
            synchronized (this){
                if(order.getState() > PayState.STATE_PAYING) {
                    Log.d("The state of the order is complete. The state is " + order.getState());
                    object.put("code",1);
                    object.put("data",null);
                    object.put("message","该订单已经U8已经处理完成,请不要再提交,orderID:"+orderID);
                    renderText(object.toString());
                    return;
                }
                BigDecimal bigDecimal = new BigDecimal(amount);
                bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
                bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
                int realMoney = bigDecimal.intValue();
                order.setRealMoney(realMoney);
                order.setSdkOrderTime(time);
                order.setCompleteTime(new Date());
                order.setChannelOrderID(trade_no);
                order.setState(PayState.STATE_SUC);
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(this.orderManager, order);
                object.put("code",1);
                object.put("data",null);
                object.put("message","U8Server处理完成,orderID:"+orderID);
                renderText(object.toString());
            }
        }catch(Exception e){
            e.printStackTrace();
            logger.error("----PPTV支付回调异常:{}",e.getMessage());
            object.put("code",1111);
            object.put("data",null);
            object.put("message","系统异常");
            renderText(object.toString());
        }
    }




}
