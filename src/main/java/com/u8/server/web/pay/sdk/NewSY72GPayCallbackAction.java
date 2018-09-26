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
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 72G 有戏
 * Created by lz on 2018/3/08.
 */
@Controller
@Namespace("/pay/newsy72g")
public class NewSY72GPayCallbackAction extends UActionSupport{
    @Autowired
    private UOrderManager orderManager;
    @Action("payCallback")
    public void payCallback() {
        Log.i("支付成功之后进入回调,路径为/pay/newsy72g/payCallback");
        Map requestParams = request.getParameterMap();
        Map<String,String> param = new HashMap<>();
        String orderid = request.getParameter("orderid");
        param.put("orderid",orderid);
        String oid = request.getParameter("oid");
        param.put("oid",oid);
        String gameid = request.getParameter("gameid");
        param.put("gameid",gameid);
        String money = request.getParameter("money");
        param.put("money",money);
        String timestamps = request.getParameter("timestamps");
        param.put("timestamps",timestamps);
        String ext = request.getParameter("ext");
        String nonce = request.getParameter("nonce");
        param.put("nonce",nonce);
        String sign = request.getParameter("sign");
        Log.i("支付成功之后的回调参数");
        Log.i("------------------orderid="+orderid+"-----------------");
        Log.i("------------------oid="+oid+"-----------------");
        Log.i("------------------gameid="+gameid+"-----------------");
        Log.i("------------------money="+money+"-----------------");
        Log.i("------------------timestamps="+timestamps+"-----------------");
        Log.i("------------------ext="+ext+"-----------------");
        Log.i("------------------nonce="+nonce+"-----------------");
        Log.i("------------------sign="+sign+"-----------------");
        if (requestParams == null) {
            Log.e("------new72G pay callBack | parmas is null");
            this.renderState(false);
            return;
        }
        long orderID = Long.parseLong(oid);
        UOrder order = orderManager.getOrder(orderID);

        UChannel channel = order.getChannel();

        if (order == null || channel == null) {
            Log.d("------new72G---------The order is null or the channel is null");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("------new72G---------The state of the order is complete  |  The state is " + order.getState());
            this.renderState(true);
            return;
        }
        if (!isSignOK(param,sign,channel.getCpAppSecret())){
            Log.e("-----------The Sign is not Match!");
            this.renderState(false);
            return;
        }
        int moneyInt = (int) Float.parseFloat(money);//以分为单位
        order.setRealMoney(moneyInt);
        order.setSdkOrderTime(timestamps);
        order.setCompleteTime(new Date());
        order.setChannelOrderID(oid);
        order.setState(PayState.STATE_SUC);
        orderManager.saveOrder(order);
        SendAgent.sendCallbackToServer(this.orderManager, order);
        this.renderState(true);

    }


    private void renderState(boolean suc) {
        JSONObject data = new JSONObject();
        JSONObject json = new JSONObject();
        int status = 0;
        String msg = "success";
        if (!suc) {
            status = 2;
            msg = "fail";
        }
        json.put("status",status);
        json.put("msg",msg);
        json.put("data",data);
        renderJson(json.toString());
    }

    public boolean isSignOK(Map<String, String> params,String sign,String appSecret) {
      StringBuilder sb = new StringBuilder();
      sb.append("gameid=").append(params.get("gameid"))
              .append("money=").append(params.get("money"))
              .append("nonce=").append(params.get("nonce"))
              .append("oid=").append(params.get("oid"))
              .append("orderid=").append(params.get("orderid"))
              .append("timestamps=").append(params.get("timestamps"))
              .append(appSecret);
        //Mademd5 md = new Mademd5();
       //String createSign = md.toMd5(sb.toString()).toLowerCase();
      String createSign = EncryptUtils.md5(sb.toString()).toLowerCase();
      Log.i("生成的签名为:"+createSign);
      return EncryptUtils.md5(sb.toString()).equals(sign);
    }
}
