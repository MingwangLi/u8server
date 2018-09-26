package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.service.UChannelManager;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.QuickSDKUtil;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;

@Namespace("/pay/quick")
public class QuickPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String nt_data;    //xml格式数据 需要先解密
    private String sign;      //签名参数
    private String md5Sign;  //签名验证
    private String u8ChannelID;    //渠道号


    public void setNt_data(String nt_data) {
        this.nt_data = nt_data;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public void setMd5Sign(String md5Sign) {
        this.md5Sign = md5Sign;
    }

    public void setU8ChannelID(String u8ChannelID) {
        this.u8ChannelID = u8ChannelID;
    }

    @Autowired
    private UOrderManager orderManager;

    @Autowired
    private UChannelManager channelManager;



    @Action("payCallback")
    public void payCallback() {
        try {
            logger.info("----QuickSDK支付回调参数nt_data:{}",nt_data);
            logger.info("----QuickSDK支付回调参数sign:{}",sign);
            logger.info("----QuickSDK支付回调参数md5Sign:{}",md5Sign);
            UChannel uchannel = channelManager.getChannelByChannelID(Integer.parseInt(u8ChannelID));
            String parseKey = uchannel.getCpPayKey();
            String md5_key = uchannel.getCpAppSecret();
            //原文  傻逼Quick
            String checkSign = EncryptUtils.md5(nt_data+sign+md5_key);
            //解密nt_data
            logger.debug("----QuickSDK支付回调解密key:{}",parseKey);
            nt_data = QuickSDKUtil.decode(nt_data,parseKey);
            //解密sign
            sign = QuickSDKUtil.decode(sign,parseKey);
            logger.debug("----QuickSDK支付回调解密参数:{}",nt_data);
            //解析nt_data
            Document document = DocumentHelper.parseText(nt_data);
            Element root = document.getRootElement();
            Element element = root.element("message");
            //QuickPayData data = new QuickPayData();
            String is_test = element.element("is_test").getTextTrim();
            String channel = element.element("channel").getTextTrim();
            String channel_uid = element.element("channel_uid").getTextTrim();
            String game_order = element.element("game_order").getTextTrim();  //cp订单号
            String order_no = element.element("order_no").getTextTrim();     //SDK订单号
            String pay_time = element.element("pay_time").getTextTrim();
            String amount = element.element("amount").getTextTrim();
            String status = element.element("status").getTextTrim();
            String extras_params = element.element("extras_params").getTextTrim();
            Long orderID = Long.parseLong(game_order);
            UOrder order = orderManager.getOrder(orderID);
            if (order.getState() > PayState.STATE_PAYING) {
                logger.warn("----Quick支付回调查询订单u8已处理完成,state:{}",order.getState());
                renderText("SUCCESS");
                return;
            }
            logger.debug("----Quick支付回调签名体:{}",nt_data+sign+md5_key);
            if (!md5Sign.equals(checkSign)) {
                logger.warn("----Quick支付回调验签失败,md5Sign:{}",md5Sign);
                renderText("FAIL");
                return;
            }
            order.setState(PayState.STATE_SUC);
            order.setCompleteTime(new Date());
            order.setSdkOrderTime(pay_time);
            order.setChannelOrderID(order_no);
            BigDecimal bigDecimal = new BigDecimal(amount);
            bigDecimal.setScale(BigDecimal.ROUND_CEILING,BigDecimal.ROUND_HALF_UP);
            bigDecimal= bigDecimal.multiply(new BigDecimal("100"));
            order.setRealMoney(bigDecimal.intValue());
            orderManager.saveOrder(order);
            boolean flag = SendAgent.sendCallbackToServer(orderManager,order);
            logger.debug("----u8处理Quick订单已完成,orderID:{},cp返回结果:{}",orderID,flag);
            renderText("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----Quick支付回调异常:{}",e.getMessage());
            renderText("FAIL");
        }
    }

}
