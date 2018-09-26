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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

@Namespace("/pay/yunwa")
public class YunWaPayCallback extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(YunWaPayCallback.class);

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback() {

        JSONObject object = new JSONObject();
        try {
            InputStream is = this.request.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String read = null;
            while((read = br.readLine()) != null) {
                sb.append(read);
            }
            logger.debug("----云蛙支付回调获取参数:{}",sb.toString());
            JSONObject json = JSONObject.fromObject(sb.toString());
            int code = json.getInt("code");
            String id = json.getString("id");
            String order = json.getString("order");
            String cporder = json.getString("cporder");
            String info = json.getString("info");
            String sign = json.getString("sign");
            String amount = json.getString("amount");
            logger.info("----云蛙支付回到参数code:{}",code);
            logger.info("----云蛙支付回到参数id:{}",id);
            logger.info("----云蛙支付回到参数order:{}",order);
            logger.info("----云蛙支付回到参数cporder:{}",cporder);
            logger.info("----云蛙支付回到参数info:{}",info);
            logger.info("----云蛙支付回到参数sign:{}",sign);
            logger.info("----云蛙支付回到参数amount:{}",amount);
            if(StringUtils.isEmpty(sign) || StringUtils.isEmpty(amount) || StringUtils.isEmpty(cporder)){
                object.put("code",1);
                object.put("msg","sign、amount、cporder参数错误");
                renderText(object.toString());
                return;
            }
            Long orderID = Long.parseLong(cporder);
            UOrder uorder = orderManager.getOrder(orderID);
            UChannel channel = uorder.getChannel();
            if (uorder.getState()>PayState.STATE_PAYING) {
                object.put("code",0);
                object.put("msg","改订单已经完成");
                renderText(object.toString());
                return;
            }
            String key = channel.getCpAppKey();
            logger.debug("----云蛙支付验签签名体:{}",code+"|"+id+"|"+order+"|"+cporder+"|"+info+"|"+key);
            String checkSign = EncryptUtils.md5(code+"|"+id+"|"+order+"|"+cporder+"|"+info+"|"+key);
            if (!sign.equals(checkSign)) {
                object.put("code",1);
                object.put("msg","签名错误");
                renderText(object.toString());
                return;
            }
            uorder.setChannelOrderID(order);
            uorder.setState(PayState.STATE_SUC);
            uorder.setCompleteTime(new Date());
            uorder.setRealMoney(Integer.parseInt(amount));
            orderManager.saveOrder(uorder);
            boolean flag = SendAgent.sendCallbackToServer(orderManager,uorder);
            logger.debug("----u8处理云蛙订单完成，订单id:{},cp返回结果:{}",orderID,flag);
            object.put("code",0);
            object.put("msg","处理成功");
            renderText(object.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----云蛙支付回调异常:{}",e.getMessage());
            object.put("code",1);
            object.put("msg",e.getMessage());
            renderText(object.toString());
        }
    }
}
