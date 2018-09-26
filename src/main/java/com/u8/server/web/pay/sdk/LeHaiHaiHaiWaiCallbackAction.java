package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.util.Date;


@Namespace("/pay/lhhhw")
public class LeHaiHaiHaiWaiCallbackAction extends UActionSupport {


    @Autowired
    private UOrderManager orderManager;

    private String data;


    public void setData(String data) {
        this.data = data;
    }

    @Action("payCallback")
    public void payCallback() {
        try {
            JSONObject object = JSONObject.fromObject(data);
            String orderid = object.getString("orderid");
            int gameid = object.getInt("gameid");
            int serverid = object.getInt("serverid");
            String username = object.getString("username");
            float amount = Float.valueOf(object.getString("amount"));
            int time = object.getInt("time");
            String extendsInfo = object.getString("extendsInfo");
            String sign = object.getString("sign");
            Log.d("----乐嗨嗨海外支付回调参数:orderid:"+orderid);
            Log.d("----乐嗨嗨海外支付回调参数:gameid:"+gameid);
            Log.d("----乐嗨嗨海外支付回调参数:serverid:"+serverid);
            Log.d("----乐嗨嗨海外支付回调参数:username:"+username);
            Log.d("----乐嗨嗨海外支付回调参数:amount:"+amount);
            Log.d("----乐嗨嗨海外支付回调参数:time:"+time);
            Log.d("----乐嗨嗨海外支付回调参数:extendsInfo:"+extendsInfo);
            Log.d("----乐嗨嗨海外支付回调参数:sign:"+sign);
            if ((System.currentTimeMillis()/1000 - time) >= 300) {
                renderState(false);
                return;
            }
            UOrder order = orderManager.getOrder(Long.parseLong(extendsInfo));
            if (null == order) {
                Log.d("乐嗨嗨支付回调查询订单信息为null");
                renderState(false);
                return;
            }
            UChannel channel = order.getChannel();
            if (null == channel) {
                Log.d("乐嗨嗨支付回调查询渠道信息为null");
                renderState(false);
                return;
            }
            if(order.getState() > PayState.STATE_PAYING) {
                Log.d("The state of the order is complete. The state is " + order.getState());
                renderState(true);
                return;
            }
            if(!isSignOK(channel,amount+"",extendsInfo,gameid+"",orderid,serverid+"",time+"",username,sign)){
                Log.d("The sign verify failed");
                this.renderState(false);
                return;
            }
            int realMoney = (int)amount * 100;
            order.setRealMoney(realMoney);
            order.setSdkOrderTime(time+"");
            order.setCompleteTime(new Date());
            order.setChannelOrderID(orderid);
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            renderState(true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("乐嗨嗨支付回调异常,异常信息:"+e.getMessage());
        }
    }


    private boolean isSignOK(UChannel uChannel,String amount,String extendsInfo,String gameid,String orderid,String serverid,String time,String username,String sign) {
        StringBuilder sb = new StringBuilder();
        sb.append("amount=").append(amount).
            append("&extendsInfo=").append(extendsInfo).
            append("&gameid=").append(gameid).
            append("&orderid=").append(orderid).
            append("&serverid=").append(serverid).
            append("&time=").append(time).
            append("&username=").append(username).
            append(uChannel.getCpAppSecret());
        Log.d("乐嗨嗨海外支付回调验签签名体:"+sb.toString());
        Log.d("乐嗨嗨海外支付回调签名:"+sign);
        String createSign = EncryptUtils.md5(sb.toString()).toLowerCase();
        Log.d("乐嗨嗨海外支付回调生成的签名:"+createSign);
        return sign.equals(createSign);
    }


    private void renderState(boolean suc) throws IOException {

        String res = "succ";
        if(!suc){
            res = "fail";
        }
        renderText(res);
    }
}
