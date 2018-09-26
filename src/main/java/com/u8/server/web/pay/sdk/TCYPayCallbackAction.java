package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
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

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Date;

@Namespace("/pay/tcy")
public class TCYPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String notify_data;
    private String sign;

    public void setNotify_data(String notify_data) {
        this.notify_data = notify_data;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback() {
        logger.info("----同城玩支付回调参数:{}",notify_data);
        logger.info("----同城玩支付回调参数:{}",sign);
        notify_data = URLDecoder.decode(notify_data);
        JSONObject json = JSONObject.fromObject(notify_data);
        //int app_id = json.getInt("app_id");
        String order_no = json.getString("order_no");
        String out_order_no = json.getString("out_order_no");
        String price = json.getString("price");
        Integer timestamp = json.getInt("timestamp");
        if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(out_order_no)) {
            logger.warn("----同城玩支付回调参数为空,sign:{},out_order_no:{}",sign,out_order_no);
            renderState(false);
            return;
        }
        Long orderID = Long.parseLong(out_order_no);
        UOrder order = orderManager.getOrder(orderID);
        UChannel channel = order.getChannel();
        String appSecret = channel.getCpAppSecret();
        if (null == order || null == channel) {
            logger.warn("----同城玩支付回到插叙订单渠道为null,orderID:{},channelID:{}",orderID,order.getChannelID());
            renderState(false);
            return;
        }
        String checkSign = EncryptUtils.md5(notify_data+appSecret).toLowerCase();
        if (!sign.equals(checkSign)) {
            logger.warn("----同城玩支付验签失败,sign:{}",sign);
            renderState(false);
            return;
        }
        synchronized (this) {
            if (order.getState() > PayState.STATE_PAYING) {
                logger.warn("----同城玩支付回调查询订单U8已经处理完成,state:{}",order.getState());
                renderState(true);
                return;
            }
            order.setChannelOrderID(order_no);
            order.setState(PayState.STATE_SUC);
            order.setCompleteTime(new Date());
            order.setSdkOrderTime(timestamp+"");
            BigDecimal bigDecimal = new BigDecimal(price);
            bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
            bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
            order.setRealMoney(bigDecimal.intValue());
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(orderManager,order);
            renderState(true);
        }
    }


    public void renderState(boolean flag) {
        JSONObject jsonObject = new JSONObject();
        if (flag) {
            jsonObject.put("status",true);
        }else {
            jsonObject.put("status",false);
        }
        renderText(jsonObject.toString());
    }
}
