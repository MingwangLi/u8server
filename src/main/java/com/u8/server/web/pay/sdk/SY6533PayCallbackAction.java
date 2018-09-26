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
import java.util.Date;

@Namespace("/pay/6533")
public class SY6533PayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /*app_id	是	STRING	游戏ID
    cp_order_id	是	STRING	游戏传入的外部订单号。服务器会根据这个订单号生成对应的平台订单号，请保证每笔订单传入的订单号的唯一性。URLencodeing
    mem_id	是	STRING	玩家ID
    order_id	是	STRING	平台订单号
    order_status	是	STRING	平台订单状态 1 未支付 2成功支付 3支付失败
    pay_time	是	STRING	订单下单时间 时间戳, Unix timestamp
    product_id	是	STRING	商品idURLencodeing
    product_name	是	STRING	商品名称 URLencodeing
    product_price	是	STRING	商品价格(元);保留两位小数URLencodeing
    sign	是	STRING	MD5 签名
    ext	否	STRING	透传参数 CP下单时的原样放回 URLencodeing*/

    private String app_id;
    private String cp_order_id;
    private String mem_id;
    private String order_id;
    private String order_status;
    private String pay_time;
    private String product_id;
    private String product_name;
    private String product_price;
    private String sign;
    private String ext;

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

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback() {
        try {
            logger.info("----6533支付回调参数app_id:{}",app_id);
            logger.info("----6533支付回调参数cp_order_id:{}",cp_order_id);
            logger.info("----6533支付回调参数mem_id:{}",mem_id);
            logger.info("----6533支付回调参数order_id:{}",order_id);
            logger.info("----6533支付回调参数order_status:{}",order_status);
            logger.info("----6533支付回调参数pay_time:{}",pay_time);
            logger.info("----6533支付回调参数product_id:{}",product_id);
            logger.info("----6533支付回调参数product_name:{}",product_name);
            logger.info("----6533支付回调参数product_price:{}",product_price);
            logger.info("----6533支付回调参数sign:{}",sign);
            logger.info("----ext:{}",ext);
            if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(cp_order_id) ) {
                logger.warn("----6533支付回调参数sign、cp_order_id为空");
                renderText("FAILURE");
                return;
            }
            Long orderID = Long.parseLong(cp_order_id);
            UOrder order = orderManager.getOrder(orderID);
            UChannel channel = order.getChannel();
            if (null == channel || null == order) {
                logger.warn("----6533支付回调查询订单、渠道为null,orderID:{},channelID:{}",orderID,channel.getChannelID());
                renderText("FAILURE");
                return;
            }
            if (!"2".equals(order_status)) {
                logger.warn("----6533支付回调查询订单未支付,order_status:{}",order_status);
                renderText("FAILURE");
                return;
            }
            if (order.getState()>PayState.STATE_PAYING) {
                logger.warn("----6533支付回调查询订单u8处理已完成,state:{}",order.getState());
                renderText("SUCCESS");
                return;
            }
            String md5Key = channel.getCpAppKey();
            StringBuilder sb = new StringBuilder();
            sb.append("app_id=").append(app_id).
                    append("&cp_order_id=").append(cp_order_id).
                    append("&mem_id=").append(mem_id).
                    append("&order_id=").append(order_id).
                    append("&order_status=").append(order_status).
                    append("&pay_time=").append(pay_time).
                    append("&product_id=").append(product_id).
                    append("&product_name=").append(product_name).
                    append("&product_price=").append(product_price).
                    append("&app_key=").append(md5Key);
            logger.debug("----6533支付回调sign:{},签名体:{}",sb.toString());
            String checkSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!sign.equals(checkSign)) {
                logger.warn("----6533支付回调验签失败,sign:{}",sign);
                renderText("FAILURE");
                return;
            }
            order.setState(PayState.STATE_SUC);
            order.setCompleteTime(new Date());
            order.setSdkOrderTime(pay_time);
            order.setChannelOrderID(order_id);
            BigDecimal bigDecimal = new BigDecimal(product_price);
            bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
            bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
            order.setRealMoney(bigDecimal.intValue());
            orderManager.saveOrder(order);
            boolean cpResult = SendAgent.sendCallbackToServer(orderManager,order);
            logger.debug("----6533支付u8处理完成,cp返回处理结果:{}",cpResult);
            renderText("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----6533支付回调异常:{}",e.getMessage());
            renderText("FAILURE");
        }
    }
}
