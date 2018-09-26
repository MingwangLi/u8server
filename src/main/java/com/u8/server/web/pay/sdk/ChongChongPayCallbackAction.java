package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UChannelManager;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.StringUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * 虫虫SDK支付回调
 * Created by lizhong on 2017/11/16.
 */
@Controller
@Namespace("/pay/chongchong")
public class ChongChongPayCallbackAction extends UActionSupport{

    private String transactionNo;         //虫虫支付订单号
    private String partnerTransactionNo;  //商户订单号
    private String statusCode;            //订单状态
    private String productId;             //支付商品的Id
    private String orderPrice;            //订单金额
    private String packageId;             //游戏ID
    private String productName;           //支付商品的名称
    private String extParam;              //扩展字段
    private String userId;                //用户ID
    private String sign;                  //回调签名,开发者在接收到虫虫游戏回调请求时,先进行验签,再进行自己的业务处理。

    @Autowired
    private UOrderManager orderManager;
    @Autowired
    private UChannelManager channelManager;
    @Action("payCallback")
    public void payCallback(){
        long orderID = Long.parseLong(this.partnerTransactionNo);
        UOrder order = orderManager.getOrder(orderID);
        if(order == null){
            Log.d("The order is null or the channel is null.orderID:%s", orderID);
            this.renderState(false);
            return;
        }

        UChannel channel = channelManager.queryChannel(order.getChannelID());
        if(channel == null){
            Log.d("The channel is not exists of channelID:"+order.getChannelID());
            this.renderState(false);
            return;
        }
        if(order.getState() > PayState.STATE_PAYING) {
            Log.d("The state of the order is complete. orderID:%s;state:%s" , orderID, order.getState());
            this.renderState(true);
            return;
        }
        if(!isSignOK(channel)){
            Log.d("the sign is not valid. sign:%s;orderID:%s", sign, transactionNo);
            this.renderState(false);
            return;
        }
        if("0000".equals(statusCode)) {
            int money = (int) (100 * Float.valueOf(orderPrice));//分为单位
            order.setRealMoney(money);
            order.setSdkOrderTime("");
            order.setCompleteTime(new Date());
            order.setChannelOrderID(transactionNo);
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            this.renderState(true);
            SendAgent.sendCallbackToServer(this.orderManager, order);
        }else if ("0002".equals(statusCode)){
            order.setChannelOrderID(transactionNo);
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
            this.renderState(false);
        }

    }

    private boolean isSignOK(UChannel channel){

        StringBuilder sb = new StringBuilder();

        if(!StringUtils.isEmpty(extParam)){
            sb.append("extParam=").append(extParam).append("&");
        }

        if(!StringUtils.isEmpty(orderPrice)){
            sb.append("orderPrice=").append(orderPrice).append("&");
        }

        if(!StringUtils.isEmpty(packageId)){
            sb.append("packageId=").append(packageId).append("&");
        }

        if(!StringUtils.isEmpty(partnerTransactionNo)){
            sb.append("partnerTransactionNo=").append(partnerTransactionNo).append("&");
        }

        if(!StringUtils.isEmpty(productId)){
            sb.append("productId=").append(productId).append("&");
        }

        if(!StringUtils.isEmpty(productName)){
            sb.append("productName=").append(productName).append("&");
        }

        if(!StringUtils.isEmpty(statusCode)){
            sb.append("statusCode=").append(statusCode).append("&");

        }

        if(!StringUtils.isEmpty(transactionNo)){
            sb.append("transactionNo=").append(transactionNo).append("&");
        }

        if(!StringUtils.isEmpty(userId)){
            sb.append("userId=").append(userId).append("&");
        }

        sb.append(channel.getCpAppSecret());

        String md5Local = EncryptUtils.md5(sb.toString()).toLowerCase();

        Log.d("cczs check sign orig str:");
        Log.d(sb.toString());

        return md5Local.equals(this.sign);

    }

    private void renderState(boolean suc){
        PrintWriter out = null;
        try {
            String res = "success";
            if(!suc){
                res = "fail";
            }
            out = this.response.getWriter();
            out.write(res);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            out.flush();
            out.close();
        }
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public String getPartnerTransactionNo() {
        return partnerTransactionNo;
    }

    public void setPartnerTransactionNo(String partnerTransactionNo) {
        this.partnerTransactionNo = partnerTransactionNo;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(String orderPrice) {
        this.orderPrice = orderPrice;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getExtParam() {
        return extParam;
    }

    public void setExtParam(String extParam) {
        this.extParam = extParam;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
