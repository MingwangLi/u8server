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

@Namespace("/pay/xueshanbao")
public class XueShanBaoPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String appid;
    private String charid;
    private String cporderid;  //	CP游戏商订单号
    private String extinfo;
    private String gold;
    private String money;   //充值金额。人民币 如1.00（单位：元）
    private String orderid;
    private String serverid;
    private String time;
    private String uid;
    private String sign;

    public void setAppid(String appid) {
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

    public void setGold(String gold) {
        this.gold = gold;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback() {
        logger.info("----雪山豹支付回调参数appid:{}",appid);
        logger.info("----雪山豹支付回调参数charid:{}",charid);
        logger.info("----雪山豹支付回调参数cporderid:{}",cporderid);
        logger.info("----雪山豹支付回调参数extinfo:{}",extinfo);
        logger.info("----雪山豹支付回调参数gold:{}",gold);
        logger.info("----雪山豹支付回调参数money:{}",money);
        logger.info("----雪山豹支付回调参数orderid:{}",orderid);
        logger.info("----雪山豹支付回调参数serverid:{}",serverid);
        logger.info("----雪山豹支付回调参数time:{}",time);
        logger.info("----雪山豹支付回调参数uid:{}",uid);
        logger.info("----雪山豹支付回调参数sign:{}",sign);
        try {
            if (StringUtils.isEmpty(sign)) {
                logger.warn("----雪山豹支付回调sign为空");
                renderText("FAIL");
                return;
            }
            Long orderID = Long.parseLong(cporderid);
            UOrder order = orderManager.getOrder(orderID);
            UChannel channel = order.getChannel();
            if (null == order || null == channel) {
                logger.warn("----雪山豹支付回调查询订单和渠道为null,channelID:{},orderID:{}",order.getChannelID(),orderID);
                renderText("FAIL");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("appid=").append(appid).
                    append("&charid=").append(charid).
                    append("&cporderid=").append(cporderid).
                    append("&extinfo=").append(extinfo).
                    append("&gold=").append(gold).
                    append("&money=").append(money).
                    append("&orderid=").append(orderid).
                    append("&serverid=").append(serverid).
                    append("&time=").append(time).
                    append("&uid=").append(uid).
                    append(channel.getCpAppSecret());
            logger.info("----雪山豹支付回调签名体:{}",sb.toString());
            String checkSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!sign.equals(checkSign)) {
                logger.warn("----雪山豹支付回调验签失败sign:{}",sign);
                renderText("FAIL");
                return;
            }
            synchronized (this) {
                if (order.getState() > PayState.STATE_PAYING) {
                    logger.warn("----雪山豹支付回调查询订单已完成,state:{}",order.getState());
                    renderText("SUCCESS");
                    return;
                }
                order.setState(PayState.STATE_SUC);
                order.setCompleteTime(new Date());
                order.setChannelOrderID(orderid);
                BigDecimal bigDecimal = new BigDecimal(money);
                bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
                bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
                order.setRealMoney(bigDecimal.intValue());
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(orderManager,order);
                renderText("SUCCESS");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----雪山豹支付回调异常:{}",e.getMessage());
            renderText("FAIL");
        }
    }
}
