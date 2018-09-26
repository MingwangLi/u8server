package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UOrder;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 搜狗SDK支付回调处理类
 * Created by ant on 2016/5/10.
 * http://localhost:8080/pay/sougou/payCallback
 */

@Namespace("/pay/sougou")
public class SouGouPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(SouGouPayCallbackAction.class);

    private String gid        ;  //game id 由平台分配的游戏编号
    private String sid        ;  //server id 由平台分配的游戏区服编号
    private String uid        ;  //user id 平台的用户id
    private String role       ;  //若游戏需充值到角色，传角色名。默认会传空
    private String oid        ;  //订单号，同一订单有可能多次发送通知
    private String date       ;  //订单创建日期，格式为yyMMdd
    private String amount1    ;  //用户充值金额（人民币元）
    private String amount2    ;  //金额（游戏币数量）（手游忽略此参数，但校验时需要传递）
    private String time       ;  //此时间并不是订单的产生或支付时间，而是通知发送的时间，也即当前时间
    private String appdata    ;  //透传参数（可无），若需要须向平台方申请开启此功能，默认开启
    private String realAmount ;  //用户充值真实金额（人民币元）
    private String auth       ;  //验证字符串, 生成方式同auth token, 区别是在第三步, 附加支付秘钥而不是app secret


    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback(){
        try{
            logger.debug("----搜狗支付回调参数gid:{}",gid);
            logger.debug("----搜狗支付回调参数sid:{}",sid);
            logger.debug("----搜狗支付回调参数uid:{}",uid);
            logger.debug("----搜狗支付回调参数role:{}",role);
            logger.debug("----搜狗支付回调参数oid:{}",oid);
            logger.debug("----搜狗支付回调参数date:{}",date);
            logger.debug("----搜狗支付回调参数amount1:{}",amount1);
            logger.debug("----搜狗支付回调参数amount2:{}",amount2);
            logger.debug("----搜狗支付回调参数time:{}",time);
            logger.debug("----搜狗支付回调参数appdata:{}",appdata);
            logger.debug("----搜狗支付回调参数realAmount:{}",realAmount);
            logger.debug("----搜狗支付回调参数auth:{}",auth);
            long localOrderID = Long.parseLong(appdata);
            UOrder order = orderManager.getOrder(localOrderID);
            if(order == null || order.getChannel() == null){
                logger.debug("----搜狗支付回调The order is null or the channel is null.");
                renderText("ERR_500");
                return;
            }
            if(order.getState() > PayState.STATE_PAYING){
                logger.debug("----搜狗支付回调The state of the order is complete. The state is {}",order.getState());
                renderText("OK");
                return;
            }
            int realMoney = Integer.valueOf(this.realAmount) * 100;      //转换为分
            if(order.getMoney() != realMoney){
                logger.debug("----搜狗支付回调金额不匹配");
                renderText("ERR_100");
                return;
            }
            Map<String,String> map = new HashMap<>();
            map.put("gid",gid);
            map.put("sid",sid);
            map.put("uid",uid);
            map.put("role",role);
            map.put("oid",oid);
            map.put("date",date);
            map.put("amount1",amount1);
            map.put("amount2",amount2);
            map.put("time",time);
            map.put("appdata",appdata);
            map.put("realAmount",realAmount);
            Set<String> params = new TreeSet<>();
            params.add("gid");
            params.add("sid");
            params.add("uid");
            params.add("role");
            params.add("oid");
            params.add("date");
            params.add("amount1");
            params.add("amount2");
            params.add("time");
            params.add("appdata");
            params.add("realAmount");
            StringBuilder sb = new StringBuilder();
            for (String param:params) {
                sb.append(param).append("=").append(map.get(param)).append("&");
            }
            logger.debug("----搜狗支付回调签名体:{}",sb.toString()+order.getChannel().getCpPayKey());
            String createSign = EncryptUtils.md5(sb.toString()+order.getChannel().getCpPayKey()).toLowerCase();
            if(!auth.equals(createSign)){
                logger.debug("----搜狗支付回调验签失败");
                renderText("ERR_200");
                return;
            }
            order.setRealMoney(realMoney);
            order.setSdkOrderTime(date);
            order.setCompleteTime(new Date());
            order.setChannelOrderID(oid);
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            renderText("OK");
        }catch (Exception e){
            e.printStackTrace();
            logger.error("----搜狗支付回调异常,异常信息:"+e.getMessage());
            //MailUtils.getInstance().sendMail("3462951792@qq.com","搜狗支付回调异常","订单id:"+appdata + " 异常信息:"+e.getMessage());
            renderText("ERR_500");
        }
    }



    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAmount1() {
        return amount1;
    }

    public void setAmount1(String amount1) {
        this.amount1 = amount1;
    }

    public String getAmount2() {
        return amount2;
    }

    public void setAmount2(String amount2) {
        this.amount2 = amount2;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAppdata() {
        return appdata;
    }

    public void setAppdata(String appdata) {
        this.appdata = appdata;
    }

    public String getRealAmount() {
        return realAmount;
    }

    public void setRealAmount(String realAmount) {
        this.realAmount = realAmount;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }
}
