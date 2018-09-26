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
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Namespace("/pay/jgyx")
public class JGGamePayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(JGGamePayCallbackAction.class);

    @Autowired
    private UOrderManager uOrderManager;

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

    @Action("payCallback")
    public void payCallback() {
        logger.debug("----坚果支付回调参数app_id:{}",app_id);
        logger.debug("----坚果支付回调参数cp_order_id:{}",cp_order_id);
        logger.debug("----坚果支付回调参数mem_id:{}",mem_id);
        logger.debug("----坚果支付回调参数order_id:{}",order_id);
        logger.debug("----坚果支付回调参数order_status:{}",order_status);
        logger.debug("----坚果支付回调参数pay_time:{}",pay_time);
        logger.debug("----坚果支付回调参数product_id:{}",product_id);
        logger.debug("----坚果支付回调参数product_name:{}",product_name);
        logger.debug("----坚果支付回调参数product_price:{}",product_price);
        logger.debug("----坚果支付回调参数sign:{}",sign);
        logger.debug("----坚果支付回调参数ext:{}",ext);
        try {
            Long orderID = Long.parseLong(cp_order_id);
            UOrder uOrder = uOrderManager.getOrder(orderID);
            if (null == uOrder) {
                logger.debug("----坚果支付回调查询订单不存在,订单id:{}",orderID);
                renderText("FAILURE");
                return;
            }
            UChannel uChannel = uOrder.getChannel();
            if (null == uChannel) {
                logger.debug("----坚果支付回调查询渠道不存在,渠道id:{}",uOrder.getChannelID());
                renderText("FAILURE");
                return;
            }
            if(uOrder.getState() > PayState.STATE_PAYING){
                logger.debug("The state of the order is complete. The state is {}" , uOrder.getState());
                renderText("FAILURE");
                return;
            }
            if (StringUtils.isEmpty(sign)) {
                logger.debug("----坚果支付回调sign为空");
                renderText("FAILURE");
                return;
            }
            final int status = 2;
            if (status != Integer.parseInt(order_status)) {
                logger.debug("----坚果支付回调order_status错误");
                renderText("FAILURE");
                return;
            }
            String url_product_name = URLEncoder.encode(product_name);
            BigDecimal bigDecimal = new BigDecimal(product_price);
            bigDecimal.setScale(2);
            StringBuilder sb = new StringBuilder();
            sb.append("app_id=").append(app_id).
                    append("&cp_order_id=").append(cp_order_id).
                    append("&mem_id=").append(mem_id).
                    append("&order_id=").append(order_id).
                    append("&order_status=").append(order_status).
                    append("&pay_time=").append(pay_time).
                    append("&product_id=").append(product_id).
                    append("&product_name=").append(url_product_name).
                    append("&product_price=").append(bigDecimal.toString()).
                    append("&app_key=").append(uChannel.getCpAppKey());
            logger.debug("----坚果支付回调签名体:{}",sb.toString());
            String createSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!sign.equals(createSign)) {
                logger.debug("----坚果支付回调验签失败");
                renderText("FAILURE");
                return;
            }
            uOrder.setChannelOrderID(order_id);
            uOrder.setCompleteTime(new Date());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(pay_time));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String sdkOrderTime = sdf.format(calendar.getTime());
            uOrder.setSdkOrderTime(sdkOrderTime);
            uOrder.setRealMoney(bigDecimal.intValue()*100);
            uOrder.setState(PayState.STATE_SUC);
            uOrderManager.saveOrder(uOrder);
            SendAgent.sendCallbackToServer(uOrderManager,uOrder);
            renderText("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----坚果支付回调异常,异常信息:{}",e.getMessage());
            //MailUtils.getInstance().sendMail("3462951792@qq.com","坚果支付回调异常","订单id:"+cp_order_id + " 异常信息:"+e.getMessage());
            renderText("FAILURE");
        }
    }
}
