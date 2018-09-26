package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;

@Namespace("/pay/jinmu")
public class JinMuPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(JinMuPayCallbackAction.class);

    private String orderNo;   //渠道订单号
    private String money;    //支付金额(元) 保留两位小数
    private String payStatus; //'支付状态：0:支付中，1:支付成功，2:支付失败,3:交易关闭',
    private String paySuccessDate;   //yyyy-MM-dd HH:mm:ss  支付完成时间
    private String payChannel;   //支付渠道  '支付渠道：1支付宝 2微信 3平台币支付',
    private String extension;  //cp 聚合sdk 自定义拓展参数
    private String sign;      //签名

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    public void setPaySuccessDate(String paySuccessDate) {
        this.paySuccessDate = paySuccessDate;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback() {
        logger.info("----金木支付回调orderNo:{}",orderNo);
        logger.info("----金木支付回调money:{}",money);
        logger.info("----金木支付回调payStatus:{}",payStatus);
        logger.info("----金木支付回调paySuccessDate:{}",paySuccessDate);
        logger.info("----金木支付回调payChannel:{}",payChannel);
        logger.info("----金木支付回调extension:{}",extension);
        logger.info("----金木支付回调sign:{}",sign);
        try {
            if (StringUtils.isEmpty(sign)) {
                logger.warn("----金木支付回调参数sign为空");
                renderText("fail");
                return;
            }
            if (!"1".equals(payStatus)) {
                logger.warn("----金木支付回调参数pay_status错误,pay_status:{}",payStatus);
                renderText("fail");
                return;
            }
            Long orderID = Long.parseLong(extension);
            UOrder order = orderManager.getOrder(orderID);
            if (null == order) {
                logger.warn("----金木支付回调查询订单不存在,orderID:{}",orderID);
                renderText("fail");
                return;
            }
            UChannel channel = order.getChannel();
            if (null == channel) {
                logger.warn("----金木支付回调查询渠道不存在,channelID:{}",order.getChannelID());
                renderText("fail");
                return;
            }
            if (order.getState() > PayState.STATE_PAYING) {
                logger.warn("----the order has been managed by u8server already,please do not request this anymore");
                renderText("success");
                return;
            }
            String key = channel.getCpAppKey();
            StringBuilder sb = new StringBuilder();
            sb.append("extension=").append(extension).
                    append("&money=").append(money).
                    append("&orderNo=").append(orderNo).
                    append("&payChannel=").append(payChannel).
                    append("&payStatus=").append(payStatus).
                    append("&paySuccessDate=").append(paySuccessDate).
                    append(key);
            logger.debug("----金木支付回调签名体:{}",sb.toString());
            String checkSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!sign.equals(checkSign)) {
                logger.warn("----金木支付回调验签失败sign:{}",sign);
                renderText("fail");
                return;
            }
            order.setState(PayState.STATE_SUC);
            order.setCompleteTime(new Date());
            order.setSdkOrderTime(paySuccessDate);
            order.setChannelOrderID(orderNo);
            BigDecimal bigDecimal = new BigDecimal(money);
            bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
            bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
            order.setRealMoney(bigDecimal.intValue());  //以分为单位
            orderManager.saveOrder(order);
            logger.info("----u8处理金木支付回调完成 order:{}",order.toJSON());
            SendAgent.sendCallbackToServer(orderManager,order);
            renderText("success");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----金木支付回调异常:{}",e.getMessage());
            renderText("fail");
        }

    }
}
