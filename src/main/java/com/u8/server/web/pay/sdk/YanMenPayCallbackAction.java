package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Date;

/**宴门支付回调
 * Created by ant on 2016/11/17.
 */

@Controller
@Namespace("/pay/yanmen")
public class YanMenPayCallbackAction extends UActionSupport{

    private String orderid   ;          //   String  35  sdk订单号  1
    private String username  ;          //   String  30  sdk登录账号  2
    private String gameid    ;          //   int  11  游戏ID  3
    private String roleid    ;          //   String  30  游戏角色ID  4
    private String serverid  ;          //   int  11  服务器ID  5
    private String paytype   ;          //   String  10  支付类型,支付参数说明  6
    private String amount    ;          //   int  11  成功充值金额，单位(元)  7
    private String paytime   ;          //   int  11  玩家充值时间，时间戳形式，如1394087000  8
    private String attach    ;          //   String 商户拓展参数  9
    private String sign      ;          //   String  32  参数签名（用于验签对比）  10

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback(){
        try{

            long orderID = Long.parseLong(attach);

            UOrder order = orderManager.getOrder(orderID);

            if(order == null){
                Log.d("The order is null");
                this.renderState(false);
                return;
            }

            UChannel channel = order.getChannel();
            if(channel == null){
                Log.d("the channel is null.");
                this.renderState(false);
                return;
            }

            if(order.getState() > PayState.STATE_PAYING) {
                Log.d("The state of the order is complete. The state is " + order.getState());
                this.renderState(true);
                return;
            }

            if(!isSignOK(channel)){
                Log.d("The sign verify failed.sign:%s;appKey:%s;orderID:%s", sign, channel.getCpAppKey(), orderID);
                this.renderState(false);
                return;
            }

            int moneyInt = Integer.valueOf(amount);

            order.setRealMoney(moneyInt);
            order.setSdkOrderTime("");
            order.setCompleteTime(new Date());
            order.setChannelOrderID(orderid);
            order.setState(PayState.STATE_SUC);

            orderManager.saveOrder(order);

            SendAgent.sendCallbackToServer(this.orderManager, order);
            renderState(true);

        }catch(Exception e){
            try {
                renderState(false);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private boolean isSignOK(UChannel channel){

        StringBuilder sb = new StringBuilder();
        sb.append("orderid=").append(orderid)
                .append("&username=").append(username)
                .append("&gameid=").append(gameid)
                .append("&roleid=").append(roleid)
                .append("&serverid=").append(serverid)
                .append("&paytype=").append(paytype)
                .append("&amount=").append(amount)
                .append("&paytime=").append(paytime)
                .append("&attach=").append(attach)
                .append("&appkey=").append(channel.getCpAppKey());

        String signLocal = EncryptUtils.md5(sb.toString()).toLowerCase();

        return signLocal.equals(this.sign);
    }

    private void renderState(boolean suc) throws IOException {

        if(suc){
            renderText("success");
        }else{
            renderText("fail");
        }

    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGameid() {
        return gameid;
    }

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public String getRoleid() {
        return roleid;
    }

    public void setRoleid(String roleid) {
        this.roleid = roleid;
    }

    public String getServerid() {
        return serverid;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }

    public String getPaytype() {
        return paytype;
    }

    public void setPaytype(String paytype) {
        this.paytype = paytype;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPaytime() {
        return paytime;
    }

    public void setPaytime(String paytime) {
        this.paytime = paytime;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
