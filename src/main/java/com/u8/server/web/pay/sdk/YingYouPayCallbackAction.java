package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
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
import java.net.URLEncoder;
import java.util.Date;

@Namespace("/pay/yingyou")
public class YingYouPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UOrderManager orderManager;

    private String app_id;   //游戏ID
    private String cp_order_id;  //游戏传入的外部订单号   URLencodeing
    private String mem_id;       //玩家ID
    private String order_id;     //平台订单号
    private String order_status; //	平台订单状态 1 未支付 2成功支付 3支付失败
    private String pay_time;  //订单下单时间 时间戳
    private String product_id;  //商品idURLencodeing
    private String product_name;  //商品名称 URLencodeing
    private String product_price; //商品价格(元);保留两位小数URLencodeing
    private String sign;
    private String ext;

    @Action("payCallback")
    public void payCallback() {
        logger.info("----鹰游支付回调参数app_id:{}",app_id);
        logger.info("----鹰游支付回调参数cp_order_id:{}",cp_order_id);
        logger.info("----鹰游支付回调参数mem_id:{}",mem_id);
        logger.info("----鹰游支付回调参数order_id:{}",order_id);
        logger.info("----鹰游支付回调参数order_status:{}",order_status);
        logger.info("----鹰游支付回调参数pay_time:{}",pay_time);
        logger.info("----鹰游支付回调参数product_id:{}",product_id);
        logger.info("----鹰游支付回调参数product_name:{}",product_name);
        logger.info("----鹰游支付回调参数product_price:{}",product_price);
        logger.info("----鹰游支付回调参数sign:{}",sign);
        logger.info("----鹰游支付回调参数ext:{}",ext);
        final String status = "2";
        if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(cp_order_id) || StringUtils.isEmpty(product_price)) {
            logger.warn("----鹰游支付回调参数sign、cp_order_id、product_price为空");
            renderText("FAILURE");
            return;
        }
        Long orderID = Long.parseLong(cp_order_id);
        UOrder order = orderManager.getOrder(orderID);
        UChannel channel = order.getChannel();
        if (null == channel || null == order) {
            logger.warn("----鹰游支付回调查询订单渠道为空,orderID:{},channelID:{}",orderID,channel.getChannelID());
            renderText("FAILURE");
            return;
        }
        if (!status.equals(order_status)) {
            logger.warn("----鹰游支付回调查询订单状态不合法,order_status:{}",order_status);
            renderText("FAILURE");
            return;
        }
        String key = channel.getCpAppKey();
        StringBuilder sb = new StringBuilder();
        sb.append("app_id=").append(app_id).
                append("&cp_order_id=").append(URLEncoder.encode(cp_order_id)).
                append("&mem_id=").append(mem_id).
                append("&order_id=").append(order_id).
                append("&order_status=").append(order_status).
                append("&pay_time=").append(pay_time).
                append("&product_id=").append(URLEncoder.encode(product_id)).
                append("&product_name=").append(URLEncoder.encode(product_name)).
                append("&product_price=").append(URLEncoder.encode(product_price)).
                append("&app_key=").append(key);
        logger.debug("----鹰游支付回调签名体:{}",sb.toString());
        String checkSign = EncryptUtils.md5(sb.toString()).toLowerCase();
        if (!sign.equalsIgnoreCase(checkSign)) {
            logger.warn("----鹰游支付回调签名不合法,sign:{}",sign);
            renderText("FAILURE");
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            logger.warn("----鹰游支付回调查询订单已完成,state:{}",order.getState());
            renderText("SUCCESS");
            return;
        }
        order.setChannelOrderID(order_id);
        order.setSdkOrderTime(pay_time);
        order.setCompleteTime(new Date());
        BigDecimal bigDecimal = new BigDecimal(product_price);
        bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
        bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
        order.setRealMoney(bigDecimal.intValue());
        order.setState(PayState.STATE_SUC);
        orderManager.saveOrder(order);
        boolean flag= SendAgent.sendCallbackToServer(orderManager,order);
        logger.debug("----鹰游支付回调U8处理完成,调用cp返回数据:{}",flag);
        renderText("SUCCESS");
    }


    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public void setCp_order_id(String cp_order_id) {
        this.cp_order_id = cp_order_id;
    }

    public void setMem_id(String mem_id) {
        this.mem_id = mem_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public void setPay_time(String pay_time) {
        this.pay_time = pay_time;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public void setProduct_price(String product_price) {
        this.product_price = product_price;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }
}
