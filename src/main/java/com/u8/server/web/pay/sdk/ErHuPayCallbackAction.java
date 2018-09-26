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

import java.math.BigDecimal;
import java.util.Date;

@Namespace("/pay/erhu")
public class ErHuPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String order;       //cp订单号
    private String pipaworder;  //渠道订单号
    private String subject;
    private String extraParam;
    private String amount;//金额(元) 保留两位小数
    private String player_id;
    private String sign;
    private String version;

    public void setOrder(String order) {
        this.order = order;
    }

    public void setPipaworder(String pipaworder) {
        this.pipaworder = pipaworder;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setExtraParam(String extraParam) {
        this.extraParam = extraParam;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setPlayer_id(String player_id) {
        this.player_id = player_id;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback() {
        logger.info("----二狐支付回调获取参数order:{}",order);
        logger.info("----二狐支付回调获取参数pipaworder:{}",pipaworder);
        logger.info("----二狐支付回调获取参数subject:{}",subject);
        logger.info("----二狐支付回调获取参数extraParam:{}",extraParam);
        logger.info("----二狐支付回调获取参数amount:{}",amount);
        logger.info("----二狐支付回调获取参数player_id:{}",player_id);
        logger.info("----二狐支付回调获取参数sign:{}",sign);
        logger.info("----二狐支付回调获取参数version:{}",version);
        if (StringUtils.isEmpty(sign)) {
            logger.warn("----二狐支付回调sign为空");
            renderText("NO");
            return;
        }
        Long orderID = Long.parseLong(order);
        UOrder uorder = orderManager.getOrder(orderID);
        UChannel channel = uorder.getChannel();
        if (null == uorder || null == channel) {
            logger.warn("----二狐支付回调查询订单和渠道为null,orderID:{},channelID:{}",orderID,uorder.getChannelID());
            renderText("NO");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("amount=").append(amount).
                append("&extraParam=").append(extraParam).
                append("&order=").append(order).
                append("&pipaworder=").append(pipaworder).
                append("&player_id=").append(player_id).
                append("&subject=").append(subject).
                append(channel.getCpAppSecret());
        logger.info("----二狐支付回调签名体:{}",sb.toString());
        String checkSign = EncryptUtils.md5(sb.toString()).toLowerCase();
        if (!sign.equals(checkSign)) {
            logger.warn("----二狐支付回调验签失败");
            renderText("NO");
            return;
        }
        synchronized (this) {
            if (uorder.getState() > PayState.STATE_PAYING) {
                logger.warn("----二狐支付回调查询订单已完成,orderID:{},state:{}",orderID,uorder.getState());
                renderText("OK");
                return;
            }
            uorder.setState(PayState.STATE_SUC);
            uorder.setChannelOrderID(pipaworder);
            uorder.setCompleteTime(new Date());
            BigDecimal bigDecimal = new BigDecimal(amount);
            bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
            bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
            uorder.setRealMoney(bigDecimal.intValue());
            orderManager.saveOrder(uorder);
            SendAgent.sendCallbackToServer(orderManager,uorder);
            renderText("OK");
        }
    }
}
