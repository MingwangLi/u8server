package com.u8.server.web.pay.sdk;

import com.alibaba.fastjson.JSONObject;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.service.UChannelManager;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Date;

/**
 * 靠谱助手支付回调类
 * Created by xiaohei on 16/10/16.
 */
@Controller
@Namespace("/pay/kaopu")
public class KaoPuPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Integer u8ChannelID;   //通过urlrewrite.xml配置

    public void setU8ChannelID(Integer u8ChannelID) {
        this.u8ChannelID = u8ChannelID;
    }

    private String username;         //用户名
    private String kpordernum;       //靠谱订单号
    private String ywordernum;       //用户订单号
    private int status;           //订单状态
    private int paytype;           //支付方式
    private int amount;           //金额(分)
    private String errdesc;          //错误描述
    private String paytime;          //支付时间
    private String gamename;         //游戏名称
    private String gameserver;       //服务器
    private String sign;

    @Autowired
    private UOrderManager orderManager;

    @Autowired
    private UChannelManager channelManager;

    @Action("payCallback")
    public void payCallback() throws Exception{
        JSONObject response = new JSONObject();
        String key = "";
        try{
            logger.info("----靠谱助手支付回调参数username:{}",username);
            logger.info("----靠谱助手支付回调参数kpordernum:{}",kpordernum);
            logger.info("----靠谱助手支付回调参数ywordernum:{}",ywordernum);
            logger.info("----靠谱助手支付回调参数status:{}",status);
            logger.info("----靠谱助手支付回调参数paytype:{}",paytype);
            logger.info("----靠谱助手支付回调参数amount:{}",amount);
            logger.info("----靠谱助手支付回调参数errdesc:{}",errdesc);
            logger.info("----靠谱助手支付回调参数paytime:{}",paytime);
            logger.info("----靠谱助手支付回调参数gamename:{}",gamename);
            logger.info("----靠谱助手支付回调参数gameserver:{}",gameserver);
            logger.info("----靠谱助手支付回调参数sign:{}",sign);
            logger.info("----靠谱助手支付回调参数u8ChannelID:{}",u8ChannelID);
            long orderID = Long.parseLong(this.ywordernum);
            UOrder order = orderManager.getOrder(orderID);
            UChannel channel = channelManager.queryChannel(u8ChannelID);
            key = channel.getCpAppKey();
            if(null == order){
                logger.warn("----靠谱助手支付回调查询订单不存在,orderID:{}",orderID);
                String sign = EncryptUtils.md5("1003"+"|"+key);
                response.put("code","1003");
                response.put("msg","订单号错误");
                response.put("sign",sign);
                renderText(response.toJSONString());
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(username).append("|")
                    .append(kpordernum).append("|")
                    .append(ywordernum).append("|")
                    .append(status).append("|")
                    .append(paytype).append("|")
                    .append(amount).append("|")
                    .append(gameserver).append("|")
                    .append(errdesc).append("|")
                    .append(paytime).append("|")
                    .append(gamename).append("|")
                    .append(key);
            logger.debug("----靠谱助手支付回调验签签名体:{}",sb.toString());
            String checkSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (StringUtils.isNotEmpty(sign) && !sign.equals(checkSign)) {
                logger.warn("----靠谱助手支付回调验签失败sign:{}",sign);
                String sign = EncryptUtils.md5("1002"+"|"+key);
                response.put("code","1002");
                response.put("msg","check sign fail");
                response.put("sign",sign);
                renderText(response.toJSONString());
                return;
            }
            int money = order.getMoney();
            if (money != amount) {
                logger.warn("----靠谱助手支付amount与money不匹配amount:{},money:{}",amount,money);
                String sign = EncryptUtils.md5("1009"+"|"+key);
                response.put("code","1009");
                response.put("msg","the param of amount is illegal");
                response.put("sign",sign);
                renderText(response.toJSONString());
                return;
            }
            if (1 != status) {
                logger.warn("----靠谱助手支付回调参数status错误:{}",status);
                String sign = EncryptUtils.md5("1004"+"|"+key);
                response.put("code","1004");
                response.put("msg","the param of status is error");
                response.put("sign",sign);
                renderText(response.toJSONString());
                return;
            }
            //涉及order status的读写 多线程下存在线程安全问题
            synchronized (this) {
                if(order.getState() > PayState.STATE_PAYING) {
                    logger.warn("----靠谱助手支付回调查询订单已经处理完成");
                    String sign = EncryptUtils.md5("1000"+"|"+key);
                    response.put("code","1000");
                    response.put("msg","The order is already completed.Please don't request this more");
                    response.put("sign",sign);
                    renderText(response.toJSONString());
                    return;
                }
                //the order is need to operator
                order.setChannelOrderID(kpordernum);
                order.setCompleteTime(new Date());
                order.setState(PayState.STATE_SUC);
                order.setRealMoney(amount);
                orderManager.saveOrder(order);
                logger.info("----u8Server处理靠谱订单已完成,order:{}",order.toJSON());
                SendAgent.sendCallbackToServer(orderManager,order);
                String sign = EncryptUtils.md5("1000"+"|"+key);
                response.put("code","1000");
                response.put("msg","The order is already completed.Please don't request this more");
                response.put("sign",sign);
                renderText(response.toJSONString());
            }
        }catch(Exception e){
           e.printStackTrace();
           logger.error("----靠谱助手支付异常:{}",e.getMessage());
           String sign = EncryptUtils.md5("1005"+"|"+key);
           response.put("code","1005");
           response.put("msg","系统异常");
           response.put("sign",sign);
           renderText(response.toJSONString());
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setKpordernum(String kpordernum) {
        this.kpordernum = kpordernum;
    }

    public void setYwordernum(String ywordernum) {
        this.ywordernum = ywordernum;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setPaytype(int paytype) {
        this.paytype = paytype;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setErrdesc(String errdesc) {
        this.errdesc = errdesc;
    }

    public void setPaytime(String paytime) {
        this.paytime = paytime;
    }

    public void setGamename(String gamename) {
        this.gamename = gamename;
    }

    public void setGameserver(String gameserver) {
        this.gameserver = gameserver;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
