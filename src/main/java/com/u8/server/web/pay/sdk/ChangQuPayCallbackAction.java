package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.constants.SDKStateCode;
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

@Namespace("/pay/changqu")
public class ChangQuPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UOrderManager orderManager;


    private String out_trade_no;
    private Double price;  //价格(元)
    private Integer pay_status;  	//0失败,1:成功 默认 传1
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
        try {
            logger.info("----畅趣支付回调参数out_trade_no:{}",out_trade_no);
            logger.info("----畅趣支付回调参数price:{}",price);
            logger.info("----畅趣支付回调参数:{}",pay_status);
            logger.info("----畅趣支付回调参数extend:{}",extend);
            logger.info("----畅趣支付回调参数signType:{}",signType);
            logger.info("----畅趣支付回调参数sign:{}",sign);
            if (StringUtils.isEmpty(out_trade_no)||null == price||StringUtils.isEmpty(sign)|| null == pay_status) {
                logger.warn("----畅趣支付回调参数错误");
                renderText("fail");
                return;
            }
            Long orderID = Long.parseLong(out_trade_no);
            UOrder order = orderManager.getOrder(orderID);
            UChannel channel = order.getChannel();
            if (null == order || null == channel) {
                logger.warn("----畅趣支付回调参数错误,out_trade_no:{}",out_trade_no);
                renderText("fail");
                return;
            }
            if (SDKStateCode.LOGINSUCCESS != pay_status) {
                logger.warn("----畅趣支付回调参数错误,pay_status:{}",pay_status);
                renderText("fail");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(out_trade_no).append(price).append(pay_status).append(extend).append(channel.getCpAppKey());
            logger.debug("----畅趣支付回调签名体:{}",sb.toString());
            String checkSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!sign.equalsIgnoreCase(checkSign)) {
                logger.warn("----畅趣支付回调验签失败,sign:{}",sign);
                renderText("fail");
                return;
            }
            if (order.getState()>PayState.STATE_PAYING) {
                logger.warn("---畅趣支付回调查询订单已完成,orderID:{}",orderID);
                renderText("success");
                return;
            }
            order.setState(PayState.STATE_SUC);
            order.setCompleteTime(new Date());
            BigDecimal bigDecimal = new BigDecimal(price);
            bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
            bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
            order.setRealMoney(bigDecimal.intValue());
            orderManager.saveOrder(order);
            boolean flag = SendAgent.sendCallbackToServer(orderManager,order);
            logger.debug("----u8处理畅趣支付回调完成,orderID:{},cpFlag:{}",orderID,flag);
            renderText("success");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----畅趣支付回调异常:{}",e.getMessage());
            renderText("fail");
        }
    }
}
