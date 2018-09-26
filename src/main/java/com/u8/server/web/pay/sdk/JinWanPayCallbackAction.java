package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.StringUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;


@Namespace("/pay/jinwan")
public class JinWanPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UOrderManager orderManager;

    private String orderId;  //渠道订单号
    private int uid;
    private Integer amount;
    private String serverId;
    private String extraInfo;  //cp订单号
    private String sign;
    private int test;

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public void setTest(int test) {
        this.test = test;
    }

    @Action("payCallback")
    public void payCallback() {
        try {
            logger.info("----劲玩SDK支付回调参数orderId:{}",orderId);
            logger.info("----劲玩SDK支付回调参数uid:{}",uid);
            logger.info("----劲玩SDK支付回调参数amount:{}",amount);
            logger.info("----劲玩SDK支付回调参数serverId:{}",serverId);
            logger.info("----劲玩SDK支付回调参数extraInfo:{}",extraInfo);
            logger.info("----劲玩SDK支付回调参数sign:{}",sign);
            logger.info("----劲玩SDK支付回调参数test:{}",test);
            if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(orderId) || StringUtils.isEmpty(extraInfo) || null == amount) {
                logger.warn("----劲玩SDK支付回调参数 orderId amount sign为空");
                renderText("fail ");
                return;
            }
            Long orderID = Long.parseLong(extraInfo);
            UOrder order = orderManager.getOrder(orderID);
            UChannel channel = order.getChannel();
            String md5Key = channel.getCpAppSecret();
            if (null == order || null == channel) {
                logger.warn("----劲玩SDK支付回调参数channel order为空");
                renderText("fail ");
                return;
            }
            if (order.getState() > PayState.STATE_PAYING) {
                logger.warn("----劲玩SDK支付回调查询订单u8处理已完成,state:{}",order.getState());
                renderText("success");
                return;
            }
            String checkSign = EncryptUtils.md5(orderId+uid+serverId+amount+extraInfo+md5Key);
            if (!sign.equals(checkSign)) {
                logger.warn("-----劲玩SDK支付回调验签失败:{}",sign);
                renderText("fail");
                return;
            }
            order.setState(PayState.STATE_SUC);
            order.setCompleteTime(new Date());
            order.setRealMoney(amount*100);
            order.setChannelOrderID(orderId);
            orderManager.saveOrder(order);
            boolean flag = SendAgent.sendCallbackToServer(orderManager,order);
            logger.debug("----u8处理Quick订单已完成,orderID:{},cp返回结果:{}",orderID,flag);
            renderText("success");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("-----劲玩SDK支付回调异常:{}",e.getMessage());
            renderText("fail");
        }
    }
}
