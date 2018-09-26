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

@Namespace("/pay/yikeyou")
public class YiKeYouPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UOrderManager orderManager;

    private String out_trade_no;
    private Double price;
    private Integer pay_status;
    private String extend;
    private String signType;
    private String sign;

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setPay_status(Integer pay_status) {
        this.pay_status = pay_status;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Action("payCallback")
    public void payCallback() {
        logger.info("----亿客游支付回调参数out_trade_no:{}",out_trade_no);
        logger.info("----亿客游支付回调参数price:{}",price);
        logger.info("----亿客游支付回调参数pay_status:{}",pay_status);
        logger.info("----亿客游支付回调参数extend:{}",extend);
        logger.info("----亿客游支付回调参数signType:{}",signType);
        logger.info("----亿客游支付回调参数sign:{}",sign);
        try {
            if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(extend)) {
                logger.warn("----亿客游支付回调sign extend为空 sign:{},extend:{}",sign,extend);
                renderText("fail");
                return;
            }
            Long orderID = Long.parseLong(extend);
            UOrder order = orderManager.getOrder(orderID);
            if (null == order) {
                logger.warn("----亿客游支付回调查询订单为null,orderID:{}",orderID);
                renderText("fail");
                return;
            }
            UChannel channel = order.getChannel();
            if (null == channel) {
                logger.warn("----亿客游支付回调查询渠道为null,channelID:{}",order.getChannelID());
                renderText("fail");
                return;
            }
            if (order.getState() > PayState.STATE_PAYING) {
                logger.warn("----the state of the order is complete. The state is {}",order.getState());
                renderText("success");
                return;
            }
            String key = channel.getCpAppKey();
            StringBuilder sb = new StringBuilder();
            //price需要转化为两位小数 验签
            String signPrice = String.format("%.2f",price);
            sb.append(out_trade_no).append(signPrice).append(pay_status).append(extend).append(key);
            logger.debug("----亿客游支付回调签名体:{}",sb.toString());
            String createSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!sign.equals(createSign)) {
                logger.warn("----亿客游支付回调验签失败,createSign:{}",createSign);
                renderText("fail");
                return;
            }
            if (1 == pay_status) {
                order.setCompleteTime(new Date());
                order.setChannelOrderID(out_trade_no);
                order.setState(PayState.STATE_SUC);
                logger.debug("----signPrice:{}",signPrice);
                BigDecimal bigDecimal = new BigDecimal(signPrice);
                bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
                int readlMoney = bigDecimal.intValue();
                logger.debug("----realMoney:{}",readlMoney);
                order.setRealMoney(readlMoney);
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(orderManager,order);
                renderText("success");
                return;
            }
            logger.warn("----亿客游支付回调pay_status:{}",pay_status);
            renderText("fail");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----亿客游支付回调异常:{}",e.getMessage());
            renderText("fail");
        }
    }
}
