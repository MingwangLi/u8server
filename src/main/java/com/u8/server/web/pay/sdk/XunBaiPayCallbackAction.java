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

@Namespace("/pay/xunbai")
public class XunBaiPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String app_id;
    private String cp_order_id;
    private String mem_id;
    private String order_id;
    private String order_status; //平台订单状态 1 未支付 2成功支付 3支付失败
    private String pay_time;   //订单下单时间 时间戳, Unix timestamp
    private String product_id;
    private String product_name;
    private String product_price;  //商品价格(元);保留两位小数
    private String sign;
    private String ext;                //拓展参数 非必传

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

    public void setExc(String ext) {
        this.ext = ext;
    }

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback() {
        logger.info("----讯百支付回调参数app_id:{}",app_id);
        logger.info("----讯百支付回调参数cp_order_id:{}",cp_order_id);
        logger.info("----讯百支付回调参数mem_id:{}",mem_id);
        logger.info("----讯百支付回调参数order_id:{}",order_id);
        logger.info("----讯百支付回调参数order_status:{}",order_status);
        logger.info("----讯百支付回调参数pay_time:{}",pay_time);
        logger.info("----讯百支付回调参数product_id:{}",product_id);
        logger.info("----讯百支付回调参数product_name:{}",product_name);
        logger.info("----讯百支付回调参数product_price:{}",product_price);
        logger.info("----讯百支付回调参数sign:{}",sign);
        logger.info("----讯百支付回调参数ext:{}",ext);
        try {
            if (StringUtils.isEmpty(sign)) {
                logger.warn("----讯百支付回调sign为空");
                renderText("FAILURE");
                return;
            }
            if (!"2".equals(order_status)) {
                logger.warn("----讯百支付回调参数order_status错误,order_status:",order_status);
                renderText("FAILURE");
                return;
            }
            Long orderID = Long.parseLong(cp_order_id);
            UOrder order = orderManager.getOrder(orderID);
            if (null == order) {
                logger.warn("----讯百支付回调查询订单不存在,orderID:{}",orderID);
                renderText("FAILURE");
                return;
            }
            UChannel channel = order.getChannel();
            if (null == channel) {
                logger.warn("----讯百支付回调查询渠道不存在,channelID:{}",order.getChannelID());
                renderText("FAILURE");
                return;
            }
            //md5(app_id...&cp_order_id...&mem_id...&order_id...&order_status...&pay_time...&product_id...&product_name...&product_price...&app_key=...)
            String app_key = channel.getCpAppKey();
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
                    append("&app_key=").append(app_key);
            logger.debug("----讯百支付回调签名体:{}",sb.toString());
            String checkSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!sign.equals(checkSign)) {
                logger.warn("----讯百支付回调验签失败,sign:{}",sign);
                renderText("FAILURE");
                return;
            }
            //多线程 读写 存在线程安全问题 需要同步
            synchronized (this) {
                if (order.getState() > PayState.STATE_PAYING) {
                    logger.warn("----the order has been managed by u8server already,please do not request this anymore,orderID:",orderID);
                    renderText("SUCCESS");
                    return;
                }
                order.setChannelOrderID(order_id);
                order.setCompleteTime(new Date());
                order.setSdkOrderTime(pay_time);
                order.setState(PayState.STATE_SUC);
                BigDecimal bigDecimal = new BigDecimal(product_price);
                bigDecimal.setScale(2);
                bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
                order.setRealMoney(bigDecimal.intValue());  //以分为单位
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(orderManager,order);
                renderText("SUCCESS");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----讯百支付回调异常{}",e.getMessage());
            renderText("FAILURE");
        }


    }
}
