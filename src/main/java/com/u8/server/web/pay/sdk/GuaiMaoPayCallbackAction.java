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

@Namespace("/pay/guaimao")
public class GuaiMaoPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String order_id;       //渠道订单号
    private Integer server_id;
    private String role_id;
    private String developerinfo;  //u8订单号
    private String coin;          //人民币 元
    private String signature;


    @Autowired
    private UOrderManager uOrderManager;

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public void setServer_id(Integer server_id) {
        this.server_id = server_id;
    }

    public void setRole_id(String role_id) {
        this.role_id = role_id;
    }

    public void setDeveloperinfo(String developerinfo) {
        this.developerinfo = developerinfo;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Action("payCallback")
    public void payCallback() {
        logger.debug("---怪猫支付回调参数order_id:{}",order_id);
        logger.debug("---怪猫支付回调参数server_id:{}",server_id);
        logger.debug("---怪猫支付回调参数role_id:{}",role_id);
        logger.debug("---怪猫支付回调参数developerinfo:{}",developerinfo);
        logger.debug("---怪猫支付回调参数coin:{}",coin);
        logger.debug("---怪猫支付回调参数signature:{}",signature);
        try {
            Long orderID = Long.parseLong(developerinfo);
            UOrder uOrder = uOrderManager.getOrder(orderID);

            if (StringUtils.isEmpty(signature)) {
                logger.warn("----怪猫支付回调签名为空");
                renderText("fail");
                return;
            }
            if (null == uOrder) {
                logger.warn("----怪猫支付回调查询订单为null,orderID:{}",orderID);
                renderText("fail");
                return;
            }
            UChannel uChannel = uOrder.getChannel();
            if (null == uChannel) {
                logger.warn("----怪猫支付回调查询渠道为null,channelID:{}",uOrder.getChannelID());
                renderText("fail");
                return;
            }
            if(uOrder.getState() > PayState.STATE_PAYING){
                //已完成订单
                logger.warn("The state of the order is complete. The state is {}",uOrder.getState());
                renderText("ok");
                return;
            }
            String key = uChannel.getCpAppSecret();
            StringBuilder sb = new StringBuilder();
            sb.append("order_id=").append(order_id).
                    append("&server_id=").append(server_id).
                    append("&role_id=").append(role_id).
                    append("&developerinfo=").append(developerinfo).
                    append("&coin=").append(coin).
                    append("&").append(key);
            logger.debug("----怪猫支付回调签名体:{}",sb.toString());
            String createSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!signature.equals(createSign)) {
                logger.warn("----怪猫支付回调验签失败,signature:{}",signature);
                renderText("fail");
                return;
            }
            uOrder.setState(PayState.STATE_SUC);
            uOrder.setCompleteTime(new Date());;
            BigDecimal bigDecimal = new BigDecimal(coin);
            bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
            bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
            int money = bigDecimal.intValue();
            uOrder.setRealMoney(money);
            uOrder.setChannelOrderID(order_id);
            uOrderManager.saveOrder(uOrder);
            SendAgent.sendCallbackToServer(uOrderManager,uOrder);
            renderText("ok");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----怪猫怪猫支付回调异常:{}",e.getMessage());
            renderText("fail");
        }
    }
}
