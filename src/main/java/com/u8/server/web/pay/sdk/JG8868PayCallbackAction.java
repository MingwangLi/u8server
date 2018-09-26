package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Namespace("/pay/8868")
public class JG8868PayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UOrderManager orderManager;




    @Action("payCallback")
    public void payCallback() throws Exception{
        //InputStream is = null;
        //BufferedReader br = null;
        try {
            String contentType = request.getHeader("Content-Type");
            logger.info("----8868这个傻逼渠道reqeustHeader:{}",contentType);
            //is = request.getInputStream();
            //StringBuilder sb = new StringBuilder();
            //br = new BufferedReader(new InputStreamReader(is));
            //String read = null;
            // while ((read=br.readLine()) != null) {
            //     sb.append(read);
            // }
            // br.close();
            // is.close();
            // String jsonParam = sb.toString();
            StringBuilder param = new StringBuilder();
            Map<String,String[]> paramMap = request.getParameterMap();
            for (String key:paramMap.keySet()) {
                param.append(key);
                param.append(paramMap.get(key)[0]);
            }
            String jsonParam = param.toString();
            logger.info("----8868支付回调获取参数:{}",jsonParam);
            JSONObject response = JSONObject.fromObject(jsonParam);
            String sign = response.getString("sign");
            JSONObject data = response.getJSONObject("data");
            String orderId = data.getString("orderId");
            String gameId = data.getString("gameId");
            String serverId = data.getString("serverId");
            String suid = data.getString("suid");
            String roleId = data.getString("roleId");
            String payWay = data.getString("payWay");
            String amount = data.getString("amount");
            String callbackInfo = data.getString("callbackInfo");
            String orderStatus = data.getString("orderStatus");
            String failedDesc = data.getString("failedDesc");
            if (StringUtils.isEmpty(sign)) {
                logger.warn("----8868支付回调sign为空");
                renderText("FAILURE");
                return;
            }
            Long orderID = Long.parseLong(callbackInfo);
            UOrder order = orderManager.getOrder(orderID);
            if (null == order ) {
                logger.warn("----8868支付回调查询订单为null,orderID:",orderID);
                renderText("FAILURE");
                return;
            }
            UChannel channel = order.getChannel();
            if (null == channel) {
                logger.warn("----8868支付回调查询渠道为null,channelID:",channel.getId());
                renderText("FAILURE");
                return;
            }
            if (order.getState() > PayState.STATE_PAYING) {
                logger.warn("-----8868支付回调查询订单已完成,orderID:{},status:{}",orderID,order.getState());
                renderText("SUCCESS");
                return;
            }
            String cpId = channel.getCpID();
            String key  = channel.getCpAppKey();
            StringBuilder sb = new StringBuilder();
            sb.append(cpId).append("amount=").append(amount).
                    append("callbackInfo=").append(callbackInfo).
                    append("failedDesc=").append(failedDesc).
                    append("gameId=").append(gameId).
                    append("orderId=").append(orderId).
                    append("orderStatus=").append(orderStatus).
                    append("payWay=").append(payWay).
                    append("roleId=").append(roleId).
                    append("serverId=").append(serverId).
                    append("suid=").append(suid).append(key);
            logger.debug("----8868支付回调签名体:{}",sb.toString());
            String createSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!sign.equals(createSign)) {
                logger.warn("----8868支付回调验签失败:{}",sign);
                renderText("FAILURE");
                return;
            }
            if (!"S".equals(orderStatus)) {
                logger.warn("----8868支付回调参数orderStatus错误:{}",orderStatus);
                renderText("FAILURE");
                return;
            }
            order.setState(PayState.STATE_SUC);
            order.setCompleteTime(new Date());
            order.setChannelOrderID(orderId);
            BigDecimal bigDecimal = new BigDecimal(amount);
            bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
            bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
            order.setRealMoney(bigDecimal.intValue());
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(orderManager,order);
            renderText("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----8868支付回调异常:{}",e.getMessage());
            renderText("FAILURE");
        }
    }
}
