package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.SignUtils;
import com.u8.server.utils.StringUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Namespace("/pay/nwsy")
public class NaiWanPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(NaiWanPayCallbackAction.class);

    private String amount;
    private int appid;
    private String charid;
    private String cporderid;   //CP的游戏商订单号
    private String extinfo;
    private int gold;
    private String orderid;
    private String serverid;
    private int time;
    private int uid;
    private String sign;

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setAppid(int appid) {
        this.appid = appid;
    }

    public void setCharid(String charid) {
        this.charid = charid;
    }

    public void setCporderid(String cporderid) {
        this.cporderid = cporderid;
    }

    public void setExtinfo(String extinfo) {
        this.extinfo = extinfo;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Autowired
    private UOrderManager uOrderManager;


    @Action("payCallback")
    public void payCallback() {
        logger.debug("----耐玩支付回调参数amount:{}",amount);
        logger.debug("----耐玩支付回调参数appid:{}",appid);
        logger.debug("----耐玩支付回调参数charid:{}",charid);
        logger.debug("----耐玩支付回调参数cporderid:{}",cporderid);
        logger.debug("----耐玩支付回调参数extinfo:{}",extinfo);
        logger.debug("----耐玩支付回调参数gold:{}",gold);
        logger.debug("----耐玩支付回调参数orderid:{}",orderid);
        logger.debug("----耐玩支付回调参数serverid:{}",serverid);
        logger.debug("----耐玩支付回调参数time:{}",time);
        logger.debug("----耐玩支付回调参数uid:{}",uid);
        logger.debug("----耐玩支付回调参数sign:{}",sign);
        try {
            long orderID = Long.parseLong(cporderid);
            UOrder uOrder = uOrderManager.getOrder(orderID);
            if (null == uOrder) {
                logger.debug("----耐玩支付回调查询订单不存在,订单id:{}",orderID);
                //MailUtils.getInstance().sendMail("3462951792@qq.com","耐玩支付回调错误","订单id:"+cporderid);
                renderText("ERROR");
                return;
            }
            UChannel uChannel = uOrder.getChannel();
            if (null == uChannel) {
                logger.debug("----耐玩支付回调查询渠道不存在,渠道id:{}",uOrder.getChannelID());
                //MailUtils.getInstance().sendMail("3462951792@qq.com","耐玩支付回调错误","订单id:"+cporderid);
                renderText("ERROR");
                return;
            }
            if(uOrder.getState() > PayState.STATE_PAYING){
                logger.debug("The state of the order is complete. The state is {}" , uOrder.getState());
                //MailUtils.getInstance().sendMail("3462951792@qq.com","耐玩支付回调错误","订单id:"+cporderid);
                renderText("ERROR");
                return;
            }
            if (StringUtils.isEmpty(sign)) {
                logger.debug("----耐玩支付回调sign为空");
                //MailUtils.getInstance().sendMail("3462951792@qq.com","耐玩支付回调错误","订单id:"+cporderid);
                renderText("ERROR");
                return;
            }
            Map<String,String> params = new HashMap<>();
            params.put("amount",amount);
            params.put("appid",appid+"");
            params.put("charid",charid);
            params.put("cporderid",cporderid);
            params.put("extinfo",extinfo);
            params.put("gold",gold+"");
            params.put("orderid",orderid);
            params.put("serverid",serverid);
            params.put("time",time+"");
            params.put("uid",uid+"");
            String appKey = uChannel.getCpPayKey();
            String createSign = SignUtils.createSign(params,appKey,"耐玩支付回调");
            if (!sign.equals(createSign)) {
                logger.debug("----耐玩支付回调sign不合法");
                //MailUtils.getInstance().sendMail("3462951792@qq.com","耐玩支付回调错误","订单id:"+cporderid);
                renderText("ERROR");
                return;
            }
            BigDecimal bigDecimal = new BigDecimal(amount);
            bigDecimal.setScale(2);
            int realMoney = bigDecimal.intValue()*100;
            uOrder.setRealMoney(realMoney);
            uOrder.setCompleteTime(new Date());
            uOrder.setState(PayState.STATE_SUC);
            uOrder.setChannelOrderID(orderid);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(time+""));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String sdkOrderTime = sdf.format(calendar.getTime());
            uOrder.setSdkOrderTime(sdkOrderTime);
            uOrderManager.saveOrder(uOrder);
            SendAgent.sendCallbackToServer(uOrderManager,uOrder);
            renderText("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----耐玩支付回调异常,异常信息:{}",e.getMessage());
            //MailUtils.getInstance().sendMail("3462951792@qq.com","耐玩支付回调异常","订单id:"+cporderid + " 异常信息:"+e.getMessage());
            renderText("FAIL");
        }
    }
}
