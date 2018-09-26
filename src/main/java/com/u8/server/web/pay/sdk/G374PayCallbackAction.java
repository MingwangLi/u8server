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
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

@Namespace("/pay/g374")
public class G374PayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(G374PayCallbackAction.class);

    private String out_trade_no;

    private Double price;   //需要保持两位小数(验签)

    private int pay_status;

    private String extend;

    private String signType;

    private String sign;

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setPay_status(int pay_status) {
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

    public void setOrderManager(UOrderManager orderManager) {
        this.orderManager = orderManager;
    }

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback() {
        logger.debug("----374支付回调参数out_trade_no:"+out_trade_no);
        logger.debug("----374支付回调参数price:"+price);
        logger.debug("----374支付回调参数pay_status:"+pay_status);
        logger.debug("----374支付回调参数extend:"+extend);
        logger.debug("----374支付回调参数signType:"+signType);
        logger.debug("----374支付回调参数sign:"+sign);
        try {
            if (StringUtils.isEmpty(extend)) {
                logger.debug("----374支付回调查询传的订单id为空:"+extend);
                renderState(false);
                return;
            }
            Long orderID = Long.parseLong(extend);
            UOrder uOrder = orderManager.getOrder(orderID);
            if (null == uOrder) {
                logger.debug("----374支付回调查询订单不存在,orderID:"+orderID);
                renderState(false);
                return;
            }
            UChannel uChannel = uOrder.getChannel();
            if (null == uChannel) {
                logger.debug("----374支付回调查询渠道不存在,channelID:"+uOrder.getChannelID());
                renderState(false);
                return;
            }
            if(uOrder.getState() > PayState.STATE_PAYING) {
                logger.debug("The state of the order is complete. The state is " + uOrder.getState());
                renderState(true);
                return;
            }
            if (StringUtils.isEmpty(sign)) {
                logger.debug("----374支付回调查询sign为空");
                renderState(false);
                return;
            }
            String key = uChannel.getCpAppKey();
            logger.debug("-----374游戏支付回调验签key:"+key);
            if (StringUtils.isEmpty(key)) {
                logger.debug("----374支付回调验签key为空");
                renderState(false);
                return;
            }
            if (!isSignOK(sign,out_trade_no,price,pay_status,extend,key)) {
                logger.debug("----374支付回调验签失败");
                renderState(false);
                return;
            }
            if (SDKStateCode.LOGINSUCCESS != pay_status) {
                logger.debug("----374游戏支付回调订单状态失败");
                renderState(false);
                return;
            }
            int realMoney = (int)(price*100);
            uOrder.setRealMoney(realMoney);
            uOrder.setState(PayState.STATE_SUC);
            uOrder.setChannelOrderID(out_trade_no);
            uOrder.setCompleteTime(new Date());
            orderManager.saveOrder(uOrder);
            boolean flag = SendAgent.sendCallbackToServer(orderManager,uOrder);
            if (flag) {
                renderState(true);
            }
            renderState(false);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----374游戏支付回调异常,异常信息:"+e.getMessage());
        }
    }


    private boolean isSignOK(String sign,String out_trade_no,Double price,int pay_status,String extend,String key) {
        BigDecimal bigDecimal = new BigDecimal(price);
        DecimalFormat df = new DecimalFormat("0.00");
        String money = df.format(bigDecimal);
        logger.debug("----374游戏支付回调验签price:"+money);
        StringBuilder sb = new StringBuilder();
        sb.append(out_trade_no).append(money).append(pay_status).append(extend).append(key);
        String createSign = EncryptUtils.md5(sb.toString()).toLowerCase();
        logger.debug("----374游戏支付回调验签生成的sign:"+createSign);
        return sign.equals(createSign);
    }


    private void renderState(boolean suc) throws IOException {
        String res = "success";
        if(!suc){
            res = "fail";
        }
        renderText(res);
    }
}
