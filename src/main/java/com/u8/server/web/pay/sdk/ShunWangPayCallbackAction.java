package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UOrder;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.StringUtils;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Namespace("/pay/shunwang")
public class ShunWangPayCallbackAction extends UActionSupport {

    private String orderNo;
    private Integer gameId;
    private String guid;
    private Integer money;  //单位:元
    private Integer coins;
    private String coinMes;
    private String time;
    private String oid;
    private String sign;


    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public void setCoins(Integer coins) {
        this.coins = coins;
    }

    public void setCoinMes(String coinMes) {
        this.coinMes = coinMes;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    private Logger logger = LoggerFactory.getLogger(ShunWangPayCallbackAction.class);


    @Autowired
    private UOrderManager uOrderManager;

    @Action("payCallback")
    public void payCallback() {
        logger.debug("----顺网SDK支付回调参数orderNo:{}",orderNo);
        logger.debug("----顺网SDK支付回调参数gameId:{}",gameId);
        logger.debug("----顺网SDK支付回调参数guid:{}",guid);
        logger.debug("----顺网SDK支付回调参数money:{}",money);
        logger.debug("----顺网SDK支付回调参数coins:{}",coins);
        logger.debug("----顺网SDK支付回调参数coinMes:{}",coinMes);
        logger.debug("----顺网SDK支付回调参数time:{}",time);
        logger.debug("----顺网SDK支付回调参数oid:{}",oid);
        logger.debug("----顺网SDK支付回调参数sign:{}",sign);
        try {
            JSONObject object = new JSONObject();
            if (StringUtils.isEmpty(oid) || StringUtils.isEmpty(sign)) {
                logger.debug("----顺网SDK支付回调参数out_order_no、sign为null");
                //MailUtils.getInstance().sendMail("3462951792@qq.com","顺网支付回调错误","订单id:"+oid);
                renderText("1");
                return;
            }
            Long orderID = Long.parseLong(oid);
            UOrder uOrder = uOrderManager.getOrder(orderID);
            if (null == uOrder || null == uOrder.getChannel()) {
                logger.debug("----顺网SDK支付回调参数查询order、channel为null");
                //MailUtils.getInstance().sendMail("3462951792@qq.com","顺网支付回调错误","订单id:"+orderID);
                renderText("1");
                renderText(object.toString());
                return;
            }
            StringBuilder sb = new StringBuilder();
            if (!StringUtils.isEmpty(coinMes)) {
                sb.append(coinMes).append("|");
            }
            if (null != coins) {
                sb.append(coins).append("|");
            }
            if (null != gameId) {
                sb.append(gameId).append("|");
            }
           if (!StringUtils.isEmpty(guid)) {
                sb.append(guid).append("|");
            }
            if (null != money) {
                sb.append(money).append("|");
            }
            if (!StringUtils.isEmpty(oid)) {
                sb.append(oid).append("|");
            }
            if (!StringUtils.isEmpty(orderNo)) {
                sb.append(orderNo).append("|");
            }
            if (!StringUtils.isEmpty(time)) {
                sb.append(time).append("|");
            }
            sb.append(uOrder.getChannel().getCpAppKey());
            logger.debug("----顺网支付回调签名体:{}",sb.toString());
            String createSign = EncryptUtils.md5(sb.toString()).toUpperCase();
            if (!sign.equals(createSign)) {
                logger.debug("----顺网SDK支付回调参数查询验签失败");
                //MailUtils.getInstance().sendMail("3462951792@qq.com","顺网支付回调错误","订单id:"+oid);
                renderText("1");
                return;
            }
            if (uOrder.getState() > PayState.STATE_PAYING) {
                logger.debug("----顺网支付回调查询订单状态错误,订单状态:{}",uOrder.getState());
                //MailUtils.getInstance().sendMail("3462951792@qq.com","顺网支付回调错误","订单id:"+oid);
                renderText("1");
                return;
            }
            int realMoney = money*100;   //以分为单位
            uOrder.setRealMoney(realMoney);
            uOrder.setSdkOrderTime(time);
            uOrder.setChannelOrderID(orderNo);
            uOrder.setCompleteTime(new Date());
            uOrder.setState(PayState.STATE_SUC);
            uOrderManager.saveOrder(uOrder);
            SendAgent.sendCallbackToServer(uOrderManager,uOrder);
            renderText("0");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----顺网支付回调异常,异常信息:{}",e.getMessage());
            //MailUtils.getInstance().sendMail("3462951792@qq.com","顺网支付回调异常","订单id:"+oid + " 异常信息:"+e.getMessage());
        }
    }
}
