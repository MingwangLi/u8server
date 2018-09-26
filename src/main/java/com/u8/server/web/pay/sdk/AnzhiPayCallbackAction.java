package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.constants.SDKStateCode;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.sdk.anzhi.Des3Util;
import com.u8.server.service.UChannelManager;
import com.u8.server.service.UOrderManager;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * 安智支付回调
 * Created by ant on 2015/4/29.
 */
@Controller
@Namespace("/pay/anzhi")
public class AnzhiPayCallbackAction extends UActionSupport{

    private Logger logger = LoggerFactory.getLogger(AnzhiPayCallbackAction.class);

    private String data;

    private int u8ChannelID;//此值通过urlewrite.xml的配置实现

    public void setData(String data) {
        this.data = data;
    }

    public void setU8ChannelID(int u8ChannelID) {
        this.u8ChannelID = u8ChannelID;
    }

    @Autowired
    private UOrderManager orderManager;

    @Autowired
    private UChannelManager channelManager;

    @Action("payCallback")
    public void payCallback(){
        try{
            logger.debug("----安智支付回调加密参数:{}",data);
            logger.debug("-----安智支付回调u8ChannelID:{}",u8ChannelID);
            UChannel uChannel = channelManager.queryChannel(u8ChannelID);
            if (null == uChannel) {
                logger.warn("----安智支付查询渠道不存在,channelID:{}",u8ChannelID);
                renderState(false);
                return;
            }
            String key = uChannel.getCpAppSecret();
            data = Des3Util.decrypt(data,key);
            logger.debug("----安智支付回调解密参数:{}",data);
            JSONObject object = JSONObject.fromObject(data);
            String uid = object.getString("uid");
            String orderId = object.getString("orderId");
            String orderAmount = object.getString("orderAmount");
            String orderTime = object.getString("orderTime");
            int code = object.getInt("code");
            String cpInfo = object.getString("cpInfo");
            int notifyTime = object.getInt("notifyTime");
            long orderID = Long.parseLong(cpInfo);
            UOrder uOrder = orderManager.getOrder(orderID);
            if (null == uOrder) {
                logger.warn("-----安智支付回调查询订单不存在,订单id:{}",orderID);
                renderState(false);
                return;
            }
            if(uOrder.getState() > PayState.STATE_PAYING){
                logger.warn("----安智支付回调The state of the order is complete. The state is {}",uOrder.getState());
                renderState(true);
                return;
            }
            if (SDKStateCode.LOGINSUCCESS != code) {
                logger.warn("----安智支付回调code is not correct,code:",code);
                renderState(false);
                return;
            }
            uOrder.setChannelOrderID(orderId);
            uOrder.setSdkOrderTime(orderTime);
            uOrder.setCompleteTime(new Date());
            uOrder.setState(PayState.STATE_SUC);
            uOrder.setRealMoney(Integer.valueOf(orderAmount));
            orderManager.saveOrder(uOrder);
            SendAgent.sendCallbackToServer(this.orderManager, uOrder);
            this.renderState(true);
        }catch (Exception e){
          logger.error("----安智支付回调异常,异常信息:{}",e.getMessage());
          renderState(false);
        }
    }

    private void renderState(boolean suc){
        try {
            PrintWriter out = this.response.getWriter();
            if(suc){
                out.write("success");
            }else{
                out.write("failure");
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("----安智支付回调返回数据异常,异常信息:{}",e.getMessage());
        }
    }



}
