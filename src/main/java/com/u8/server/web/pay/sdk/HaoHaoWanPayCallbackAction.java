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

import java.net.URLDecoder;
import java.util.Date;

@Namespace("/pay/hhw")
public class HaoHaoWanPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //orderid	String	35	游戏汇订单号	1
    //username	String	30	游戏汇登录账号	2
    //gameid	int	11	游戏 ID	3
    //roleid	String	30	游戏角色 ID	4
    //serverid	int	11	服务器 ID	5
    //paytype	String	10	支付类型,支付参数说明	6
    //amount	int	11	成功充值金额，单位(元)	7
    //paytime	int	11	玩家充值时间，时间戳形式，如 1394087000	9
    //attach	String		商户拓展参数	10
    //sign	String	32	参数签名（用于验签对比）	11

    private String orderid;
    private String username;
    private Integer gameid;
    private String roleid;
    private Integer serverid;
    private String paytype;
    private Integer amount;
    private Integer paytime;
    private String attach;
    private String sign;

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setGameid(Integer gameid) {
        this.gameid = gameid;
    }

    public void setRoleid(String roleid) {
        this.roleid = roleid;
    }

    public void setServerid(Integer serverid) {
        this.serverid = serverid;
    }

    public void setPaytype(String paytype) {
        this.paytype = paytype;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public void setPaytime(Integer paytime) {
        this.paytime = paytime;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback() {
        try {
            logger.debug("----好好玩SDK支付回调参数orderid:{}",orderid);
            logger.debug("----好好玩SDK支付回调参数username:{}",username);
            logger.debug("----好好玩SDK支付回调参数gameid:{}",gameid);
            logger.debug("----好好玩SDK支付回调参数roleid:{}",roleid);
            logger.debug("----好好玩SDK支付回调参数serverid:{}",serverid);
            logger.debug("----好好玩SDK支付回调参数paytype:{}",paytype);
            logger.debug("----好好玩SDK支付回调参数amount:{}",amount);
            logger.debug("----好好玩SDK支付回调参数paytime:{}",paytime);
            logger.debug("----好好玩SDK支付回调参数attach:{}",attach);
            logger.debug("----好好玩SDK支付回调参数sign:{}",sign);
            if(StringUtils.isEmpty(attach) || StringUtils.isEmpty(sign) || null == amount) {
                logger.warn("----好好玩SDK支付回调参数attch、sign、amount为空");
                renderText("fail");
                return;
            }
            Long orderID = Long.parseLong(attach);
            UOrder order = orderManager.getOrder(orderID);
            UChannel channel = order.getChannel();
            String md5key = channel.getCpAppKey();
            if (null == order || null == channel) {
                logger.warn("----好好玩SDK支付回调查询order、channel为空,orderID:{},channleID:{}",orderID,order.getChannelID());
                renderText("fail");
                return;
            }
            if(order.getState() > PayState.STATE_PAYING) {
                logger.warn("----好好玩SDK支付回调查询订单已完成,orderID:{}",orderID);
                renderText("success");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("orderid=");
            if (!StringUtils.isEmpty(orderid)) {
                 sb.append(URLDecoder.decode(orderid));
            }
            sb.append("&username=");
            if (!StringUtils.isEmpty(username)) {
                sb.append(URLDecoder.decode(username));
            }
            sb.append("&gameid=");
            if (null != gameid) {
                sb.append(gameid);
            }
            sb.append("&roleid=");
            if (!StringUtils.isEmpty(roleid)) {
                sb.append(URLDecoder.decode(roleid));
            }
            sb.append("&serverid=");
            if (null != serverid) {
                sb.append(serverid);
            }
            sb.append("&paytype=");
            if (!StringUtils.isEmpty(paytype)) {
                sb.append(URLDecoder.decode(paytype));
            }
            sb.append("&amount=");
            if (null != amount) {
                sb.append(amount);
            }
            sb.append("&paytime=");
            if (null != paytime) {
                sb.append(paytime);
            }
            sb.append("&attach=");
            if (!StringUtils.isEmpty(attach)) {
                sb.append(URLDecoder.decode(attach));
            }
            sb.append("&appkey=").append(md5key);
            logger.debug("----好好玩SDK支付回调签名体:{}",sb.toString());
            String checkSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!sign.equals(checkSign)) {
                logger.warn("----好好玩SDK支付回调验签失败sign:{}",sign);
                renderText("fail");
                return;
            }
            order.setState(PayState.STATE_SUC);
            order.setCompleteTime(new Date());
            order.setSdkOrderTime(String.format("%d",paytime));
            order.setChannelOrderID(orderid);
            order.setRealMoney(amount*100);
            orderManager.saveOrder(order);
            boolean flag = SendAgent.sendCallbackToServer(orderManager,order);
            logger.debug("----u8处理好好玩SDK支付订单已完成,orderID:{},cp返回结果:{}",orderID,flag);
            renderText("success");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----好好玩SDK支付回调异常:{}",e.getMessage());
            renderText("fail");
        }
    }
}
