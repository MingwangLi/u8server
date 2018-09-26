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

import java.net.URLDecoder;
import java.util.*;

@Namespace("/pay/wukong")
public class WuKongPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UOrderManager orderManager;

    private String openId;
    private String serverId;
    private String serverName;
    private String roleId;
    private String roleName;
    private String orderId;
    private String orderStatus;
    private String payType;
    private String amount;
    private String remark;
    private String callBackInfo;
    private String payTime;
    private String paySUTime;
    private String sign;

    @Action("payCallback")
    public void payCallback() {
        logger.info("----悟空支付回调参数openId:{}",openId);
        logger.info("----悟空支付回调参数serverId:{}",serverId);
        logger.info("----悟空支付回调参数serverName:{}",serverName);
        logger.info("----悟空支付回调参数roleId:{}",roleId);
        logger.info("----悟空支付回调参数roleName:{}",roleName);
        logger.info("----悟空支付回调参数orderId:{}",orderId);
        logger.info("----悟空支付回调参数orderStatus:{}",orderStatus);
        logger.info("----悟空支付回调参数payType:{}",payType);
        logger.info("----悟空支付回调参数amount:{}",amount);
        logger.info("----悟空支付回调参数remark:{}",remark);
        logger.info("----悟空支付回调参数callBackInfo:{}",callBackInfo);
        logger.info("----悟空支付回调参数payTime:{}",payTime);
        logger.info("----悟空支付回调参数paySUTime:{}",paySUTime);
        logger.info("----悟空支付回调参数sign:{}",sign);
        if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(callBackInfo) || StringUtils.isEmpty(amount)) {
            logger.warn("----悟空支付回调sign、callBackInfo、amount为空");
            renderText("error");
            return;
        }
        Long orderID = Long.parseLong(callBackInfo);
        UOrder order = orderManager.getOrder(orderID);
        UChannel channel = order.getChannel();
        if (null == order || null == channel) {
            logger.warn("----悟空支付查询order、channel为空,orderID:{},channelID:{}",orderID,channel.getChannelID());
            renderText("error");
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            logger.warn("----悟空支付回到查询订单已完成,state:{}",order.getState());
            renderText("success");
            return;
        }
        Map<String,String> paramMap = new HashMap<>();
        if (StringUtils.isNotEmpty(openId)) {
            paramMap.put("openId",openId);
        }
        if (StringUtils.isNotEmpty(serverId)) {
            paramMap.put("serverId",serverId);
        }
        if (StringUtils.isNotEmpty(serverName)) {
            paramMap.put("serverName",URLDecoder.decode(serverName));
        }
        if (StringUtils.isNotEmpty(roleId)) {
            paramMap.put("roleId",roleId);
        }
        if (StringUtils.isNotEmpty(roleName)) {
            paramMap.put("roleName",URLDecoder.decode(roleName));
        }
        if (StringUtils.isNotEmpty(orderId)) {
            paramMap.put("orderId",orderId);
        }
        if (StringUtils.isNotEmpty(orderStatus)) {
            paramMap.put("orderStatus",orderStatus);
        }
        if (StringUtils.isNotEmpty(payType)) {
            paramMap.put("payType",URLDecoder.decode(payType));
        }
        if (StringUtils.isNotEmpty(amount)) {
            paramMap.put("amount",amount);
        }
        if (StringUtils.isNotEmpty(remark)) {
            paramMap.put("remark",URLDecoder.decode(remark));
        }
        if (StringUtils.isNotEmpty(callBackInfo)) {
            paramMap.put("callBackInfo",callBackInfo);
        }
        if (StringUtils.isNotEmpty(payTime)) {
            paramMap.put("payTime",payTime);
        }
        if (StringUtils.isNotEmpty(paySUTime)) {
            paramMap.put("paySUTime",paySUTime);
        }
        //paramMap.put("appKey",channel.getCpAppKey());
        String linkString = createLinkString(paramMap);
        String md5 = linkString+"&appKey="+channel.getCpAppKey();
        logger.debug("----悟空支付回调签名体:{}",md5);
        String createSign = EncryptUtils.md5(md5).toLowerCase();
        if (!sign.equalsIgnoreCase(createSign)) {
            logger.warn("----悟空支付回调签名体不合法:{}",sign);
            renderText("errorSign");
            return;
        }
        order.setState(PayState.STATE_SUC);
        order.setCompleteTime(new Date());
        order.setChannelOrderID(orderId);
        order.setSdkOrderTime(paySUTime);
        order.setRealMoney(Integer.parseInt(amount));
        orderManager.saveOrder(order);
        boolean flag = SendAgent.sendCallbackToServer(orderManager,order);
        logger.debug("----悟空支付回调u8Server处理完成,cp返回数据:{}",flag);
        renderText("success");
    }


   /* public static Map<String,String> convertMap(Map requestParams){
        Map<String,String> tradeParameters = new HashMap<String, String>();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            if(values==null||values.length==0||"".equals(values[0])){
                continue;
            }
            tradeParameters.put(name, values[0]);
        }
        return tradeParameters;
    }*/


    public String createLinkString(Map<String, String> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        final StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if (key.equals("sign"))
                continue;
            if (null != sb && sb.length() > 0) {
                if (null != params.get(key) && null != params.get(key).toString()) {
                    sb.append('&').append(key).append('=').append(params.get(key));
                }
            } else {
                if (null != params.get(key) && null != params.get(key).toString()) {
                    sb.append(key).append('=').append(params.get(key));
                }
            }
        }
        return sb.toString();
    }



    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setCallBackInfo(String callBackInfo) {
        this.callBackInfo = callBackInfo;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public void setPaySUTime(String paySUTime) {
        this.paySUTime = paySUTime;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
