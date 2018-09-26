package com.u8.server.web.pay.sdk;

import com.opensymphony.xwork2.ModelDriven;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.sdk.yiliu.YiLiuPayResult;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.StringUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Author: lizhong
 * Date: 2017/12/4.
 * 天启16游戏支付回调
 * */
@Controller
@Namespace("/pay/16yx")
public class YiLiuPayCallbackAction extends UActionSupport implements ModelDriven<YiLiuPayResult>{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private YiLiuPayResult rsp = new YiLiuPayResult();

    @Autowired
    private UOrderManager orderManager;


    @Action("payCallback")
    public void payCallback() throws Exception {
        try {
            logger.info("----天启支付回调参数:{}",rsp.toString());
            if (StringUtils.isEmpty(rsp.getSign())) {
                logger.warn("----天启支付回调参数sign为空");
                renderText("FAILURE");
                return;
            }
            Long orderID = Long.parseLong(rsp.getAttach());
            UOrder order = orderManager.getOrder(orderID);
            if (null == order) {
                logger.warn("----天启支付回调查询订单为null,orderID:{}",orderID);
                renderText("FAILURE");
                return;
            }
            UChannel channel = order.getChannel();
            if (null == channel) {
                logger.warn("----天启支付回调查询渠道为null,channelID:{}",order.getChannelID());
                renderText("FAILURE");
                return;
            }
            if(order.getState() > PayState.STATE_PAYING){
                //已完成订单
                logger.warn("The state of the order is complete. The state is {}",order.getState());
                renderText("SUCCESS");
                return;
            }
            StringBuilder sb = new StringBuilder();
            String key = channel.getCpAppKey();
            sb.append("order_id=").append(rsp.getOrder_id()).
                    append("&mem_id=").append(rsp.getMem_id()).
                    append("&app_id=").append(rsp.getApp_id()).
                    append("&money=").append(rsp.getMoney()).
                    append("&order_status=").append(rsp.getOrder_status()).
                    append("&paytime=").append(rsp.getPaytime()).
                    append("&attach=").append(rsp.getAttach()).
                    append("&app_key=").append(key);
            logger.debug("----天启支付回调签名体:{}",sb.toString());
            String createSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!rsp.getSign().equals(createSign)) {
                logger.warn("----天启支付回调验签失败,sign:{}",rsp.getSign());
                renderText("FAILURE");
                return;
            }
            order.setSdkOrderTime(rsp.getPaytime());
            order.setChannelOrderID(rsp.getOrder_id());
            order.setCompleteTime(new Date());
            order.setState(PayState.STATE_SUC);
            BigDecimal bigDecimal = new BigDecimal(rsp.getMoney());
            bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
            int realMoney = bigDecimal.intValue();
            order.setRealMoney(realMoney);
            orderManager.saveOrder(order);
            logger.info("---u8Server处理天启订单成功,orderID:{}",order.getOrderID());
            SendAgent.sendCallbackToServer(orderManager,order);
            renderText("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----天启支付回调异常:{}",e.getMessage());
            renderText("FAILURE");
        }

    }


    @Override
    public YiLiuPayResult getModel() {
        return rsp;
    }

    //不提供set方法 rsp属性怎么赋值?? 大兄弟基础功不扎实啊
    public void setRsp(YiLiuPayResult rsp) {
        this.rsp = rsp;
    }
}

