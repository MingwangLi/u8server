package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.service.UOrderManager;
import com.u8.server.web.pay.SendAgent;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Namespace("/pay/xinlang")
public class XinLangPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UOrderManager orderManager;

    /**
     * order_id|string       | 调用下单接口获取到的订单号 | true
     * amount|int      | 支付金额，单位 分  | true
     * order_uid|string       | 支付用户id  | true
     * source|string       | 应用的appkey | true
     * actual_amount|int      | 实际支付金额，单位 分 | true
     * pt|string|透传参数（该参数的有无决定于下单时有没有上传pt参数） | true
     * signature|string   |用于参数校验的签名，生成办法参考附录一| true
     */

    private String order_id;
    private Integer amount;
    private String order_uid;
    private String source;
    private Integer actual_amount;
    private String pt;
    private String signature;

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public void setOrder_uid(String order_uid) {
        this.order_uid = order_uid;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setActual_amount(Integer actual_amount) {
        this.actual_amount = actual_amount;
    }

    public void setPt(String pt) {
        this.pt = pt;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Action("payCallback")
    public void payCallback() {
        try {
            logger.info("----新浪支付回调参数order_id:{}",order_id);
            logger.info("----新浪支付回调参数amount:{}",amount);
            logger.info("----新浪支付回调参数order_uid:{}",order_uid);
            logger.info("----新浪支付回调参数source:{}",source);
            logger.info("----新浪支付回调参数actual_amount:{}",actual_amount);
            logger.info("----新浪支付回调参数pt:{}",pt);
            logger.info("----新浪支付回调参数signature:{}",signature);
            if (StringUtils.isEmpty(signature) || StringUtils.isEmpty(order_id)) {
                logger.warn("----新浪支付回调参数signature、order_id为空");
                renderText("FAIL");
                return;
            }
            Long orderID = Long.parseLong(pt);
            UOrder order = orderManager.getOrder(orderID);
            UChannel channel = order.getChannel();
            if (null == order || null == channel) {
                logger.warn("----新浪支付回调查询对应渠道和订单不存在,orderID:{},channelID:{}",orderID,order.getChannelID());
                renderText("FAIL");
                return;
            }
            if (order.getState()>PayState.STATE_PAYING) {
                logger.warn("----新浪支付回调查询订单u8吃力已完成,state:{}",order.getState());
                renderText("OK");
                return;
            }
            Map<String,String> signParam = new HashMap<>();
            signParam.put("order_id",order_id);
            signParam.put("amount",amount+"");
            signParam.put("order_uid",order_uid);
            signParam.put("source",source);
            signParam.put("actual_amount",actual_amount+"");
            signParam.put("pt",pt);
            signParam.put("signature",signature);
            String appSecret = channel.getCpPayKey();
            if (!validateSignature(signParam,appSecret)) {
                logger.warn("----新浪支付回调验签失败,sign:{}",signature);
                renderText("FAIL");
                return;
            }
            order.setState(PayState.STATE_SUC);
            order.setCompleteTime(new Date());
            order.setChannelOrderID(order_id);
            order.setRealMoney(amount);
            orderManager.saveOrder(order);
            boolean flag = SendAgent.sendCallbackToServer(orderManager,order);
            logger.debug("----新浪支付回调u8处理已完成,orderID:{},cp返回结果:{}",orderID,flag);
            renderText("OK");
        } catch (Exception e) {
            logger.error("----新浪支付回调异常,异常信息:{}",e.getMessage());
            renderText("FAIL");
        }
    }

    private boolean validateSignature(Map<String, String> params,String appSecret) {
        if(params != null){
            StringBuilder sValue = new StringBuilder();
            Object[] keys = params.keySet().toArray();
            Arrays.sort(keys);
            String temp = null;
            for(Object key : keys){
                if(!key.equals("signature")){
                    sValue.append(key).append("|");
                    temp = params.get(key);
                    if(temp == null){
                        sValue.append("").append("|");
                    }else{
                        sValue.append(temp).append("|");
                    }
                }
            }
            sValue.append(appSecret);
            String localSignature =getSHA1(sValue.toString());
            logger.debug("----新浪支付回调签名体:{}",localSignature);
            if(localSignature.equals(params.get("signature"))){
                return true;
            }else{
                return false;
            }

        }
        return false;

    }

    public String getSHA1(String input) {
        return encrypt(input, "SHA-1");
    }

    public String encrypt(String strSrc, String encName) {
        MessageDigest md = null;
        String strDes = null;
        byte[] bt = strSrc.getBytes();
        try {
            if (encName == null || encName.equals("")) {
                encName = "MD5";
            }
            md = MessageDigest.getInstance(encName);
            md.update(bt);
            strDes = bytes2Hex(md.digest()); // to HexString
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("Invalid algorithm.");
            return null;
        }
        return strDes;
    }


    public String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }
}
