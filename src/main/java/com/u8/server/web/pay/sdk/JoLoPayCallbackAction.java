package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.jolo.RSASignature;
import com.u8.server.service.UOrderManager;
import com.u8.server.web.pay.SendAgent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: lz
 * @Date: 2017/1/12 15:42.
 *聚乐JoLoPlay(HTC) SDK  支付回调
 */
@Controller
@Namespace("/pay/htc")
public class JoLoPayCallbackAction extends UActionSupport{
    @Autowired
    private UOrderManager orderManager;

    private JSONObject json = null;

    @Action("payCallback")
    public void payCallback() throws IOException ,JSONException{
        String payContent = new String(IOUtils.toByteArray(request.getInputStream()), "utf-8");
        if(payContent == null){
            Log.e("----JoLoPayCallBack message is null");
            this.renderState(false);
            return;
        }
        Map<String, String> paramters = changeToParamters(payContent);
        String signType = paramters.get("sign_type");//签名类型，一般是RSA
        String sign = URLDecoder.decode(paramters.get("sign"),"utf-8");// 签名
        String orderStr = paramters.get("order");
        // 订单真实数据
        String orderDecoderToJson = URLDecoder.decode(orderStr, "utf-8");// urlDecoder

        json = new JSONObject(orderDecoderToJson);
        int resultCode=json.getInt("result_code");//1成功 0失败
        //String resultMsg=(String)json.get("result_msg");//支付信息
        String gameCode=(String)json.get("game_code");//游戏编号
        int realAmount=(int)json.getInt("real_amount");//付款成功金额，单位人民币分
        String cpOrderId=(String)json.get("game_order_id");//cp自身的订单号
        String joloOrderId=(String)json.get("jolo_order_id");//jolo订单
        String createTime=(String)json.get("gmt_create");//创建时间 订单创建时间 yyyy-MM-dd  HH:mm:ss
        String payTime=(String)json.get("gmt_payment");//支付时间 订单支付时间  yyyy-MM-dd  HH:mm:ss

        long localOrderID = Long.parseLong(cpOrderId);
        UOrder order = orderManager.getOrder(localOrderID);
        if (order == null || order.getChannel() == null) {
            Log.d("The order is null or the channel is null.");
            this.renderState(false);
            return;
        }
        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("The state of the order is complete. The state is " + order.getState());
            this.renderState(true);
            return;
        }
        //校验订单
        /**
         *  order
         *  sign
         *  CpPayKey  由聚乐提供的rsa公钥
         * */
        boolean isCheckOK = RSASignature.doCheck(orderDecoderToJson,sign,order.getChannel().getCpAppKey());
        if(!isCheckOK){
            Log.e("----RSA_SIGN Failed---");
            this.renderState(false);
            return;
        }
        if (resultCode == 1) {
            order.setRealMoney(realAmount);
            order.setCompleteTime(new Date());
            order.setSdkOrderTime(payTime);
            order.setChannelOrderID(joloOrderId);
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            this.renderState(true);
        }else {
            order.setChannelOrderID(joloOrderId);
            order.setSdkOrderTime(payTime);
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
            renderState(false);
        }


    }

    private void renderState(boolean suc) throws IOException {
        PrintWriter out = this.response.getWriter();
        if (suc) {
            out.write("success");
        } else {
            out.write("error");
        }
        out.flush();
        out.close();
    }

    private Map<String, String> changeToParamters(String payContent) {
        Map<String, String> map = new HashMap<String, String>();
        if (StringUtils.isNotBlank(payContent)) {
            String[] paramertes = payContent.split("&");
            for (String parameter : paramertes) {
                String[] p = parameter.split("=");
                map.put(p[0], p[1].replaceAll("\"", ""));
            }
        }
        return map;
    }

}
