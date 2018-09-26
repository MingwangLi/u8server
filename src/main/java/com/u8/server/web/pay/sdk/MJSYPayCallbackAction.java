package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.constants.SDKStateCode;
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

@Namespace("/pay/mjsy")
public class MJSYPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String out_trade_no;   //渠道订单号
    private Double price;         //价格
    private Integer pay_status;  //支付状态 0失败,1:成功 默认 传1
    private String extend;     //拓展参数  cp订单号
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

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback() {
        logger.info("----米家手游支付回调参数out_trade_no:{}",out_trade_no);
        logger.info("----米家手游支付回调参数price:{}",price);
        logger.info("----米家手游支付回调参数pay_status:{}",pay_status);
        logger.info("----米家手游支付回调参数extend:{}",extend);
        logger.info("----米家手游支付回调参数signType:{}",signType);
        logger.info("----米家手游支付回调参数sign:{}",sign);
        try {
            if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(extend)) {
                logger.warn("----米家手游支付回调参数sign、extend错误");
                renderText("fail");
                return;
            }
            Long orderID = Long.parseLong(extend);
            UOrder order = orderManager.getOrder(orderID);
            UChannel channel = order.getChannel();
            String key = channel.getCpAppKey();
            logger.debug("----米家手游支付回调key:{}",key);
            if (null == order || null == channel) {
                logger.warn("----米家手游支付回调查询渠道和订单为null,orderID:{},channelID:{}",orderID,order.getChannelID());
                renderText("fail");
                return;
            }
            if (SDKStateCode.LOGINSUCCESS != pay_status) {
                logger.warn("----米家手游支付回调参数pay_status错误,pay_status:{}",pay_status);
                renderText("fail");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(out_trade_no).append(String.format("%.2f",price)).append(pay_status).append(extend).append(key);
            logger.debug("----米家手游支付回调签名体:{}",sb.toString());
            String checkSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!sign.equals(checkSign)) {
                logger.warn("----米家手游支付回调验签失败:{}",sign);
                renderText("fail");
                return;
            }
            synchronized (this) {
                if (order.getState()>PayState.STATE_PAYING) {
                    logger.warn("-----米家手游支付回调查询订单状态u8已经处理完成,state:{}",order.getState());
                    renderText("success");
                    return;
                }
                //u8处理订单
                order.setState(PayState.STATE_SUC);
                order.setCompleteTime(new Date());
                BigDecimal bigDecimal = new BigDecimal(String.format("%.2f",price));
                bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
                bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
                order.setRealMoney(bigDecimal.intValue());
                order.setChannelOrderID(out_trade_no);
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(orderManager,order);
                logger.debug("----米家手游支付U8处理完成");
                renderText("success");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----米家手游支付回调异常:{}",e.getMessage());
            renderText("fail");
        }
    }
}
