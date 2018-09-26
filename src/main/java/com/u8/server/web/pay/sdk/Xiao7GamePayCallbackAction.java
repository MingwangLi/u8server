package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.xiao7game.PayCallbackResponse;
import com.u8.server.sdk.xiao7game.VerifyUtils;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.JsonUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by lvxinmin on 2016/11/14.
 */
@Controller
@Namespace("/pay/xiao7game")
public class Xiao7GamePayCallbackAction extends UActionSupport {

    @Autowired
    private UOrderManager orderManager;
    public static UChannel channel = null;

    @Action("payCallback")
    public void payCallback() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
            Log.d("----------------" + name + ":" + valueStr);
        }

        if (params == null) {
            Log.d("-----------xiao7game callBack message is null");
            this.renderState(false);
            return;
        }
        Map<String, String> map = new TreeMap<String, String>();
        map.put("encryp_data", params.get("encryp_data"));
        map.put("extends_info_data", params.get("extends_info_data"));
        map.put("game_area", params.get("game_area"));
        map.put("game_orderid", params.get("game_orderid"));
        map.put("game_role_id", params.get("game_role_id"));
        map.put("game_role_name", params.get("game_role_name"));
        map.put("sdk_version", params.get("sdk_version"));
        map.put("subject", params.get("subject"));
        map.put("xiao7_goid", params.get("xiao7_goid"));
        String sourceStr = VerifyUtils.buildHttpQueryNoEncode(map);
        map.put("sign_data", params.get("sign_data"));
        long order_id = Long.parseLong(params.get("game_orderid"));
        UOrder order = orderManager.getOrder(order_id);
        channel = order.getChannel();

        if (order == null || channel == null) {
            Log.d("---------The order is null or the channel is null");
            this.renderState(false);
            return;
        }

        if (order.getState() > PayState.STATE_PAYING) {
            Log.d("-----------The state of the order is complete |  The state is " + order.getState());
            this.renderState(true);
            return;
        }
        /*if (!VerifyUtils.verifySign(sourceStr, map.get("sign_data"), VerifyUtils.loadPublicKeyByStr(channel.getCpPayKey())) )
        {
            Log.e("failed:sign_data_verify_failed");
            this.renderState(false);
        }*/
        String decryptData = new String(VerifyUtils.publicKeyDecrypt(VerifyUtils.loadPublicKeyByStr(channel.getCpPayKey()), VerifyUtils.baseDecode(map.get("encryp_data"))));
        /*************************************************************
         * 下面这里将会返回是一个包含game_orderid、guid、pay_price的双列集合
         * {game_orderid=xxxx, guid=xxxx, pay_price=xxxxx}
         *************************************************************/
        Map<String, String> decryptMap = VerifyUtils.decodeHttpQueryNoDecode(decryptData);
        /******************************************************
         * 这里需要判断是否存在game_orderid、pay_price、guid三个值。
         ******************************************************/
        if (!decryptMap.containsKey("game_orderid") || !decryptMap.containsKey("pay_price") || !decryptMap.containsKey("guid"))
        {
            Log.d("failed:encryp_data_decrypt_failed");
            this.renderState(false);
        }
        /*********************************************************************
         * 对比一下解出来的订单号与传递过来的订单号是否一致。这里同时要比较一下当前订单号是否是属于当前小7渠道。
         ********************************************************************/
        if(!decryptMap.get("game_orderid").equals(map.get("game_orderid"))){
            Log.d("failed:game_orderid error");
            this.renderState(false);
        }
        String realMoney = decryptMap.get("pay_price");
        order.setState(PayState.STATE_SUC);
        order.setCompleteTime(new Date());
        order.setRealMoney(Float.valueOf(realMoney).intValue() * 100);
        order.setChannelOrderID(params.get("xiao7_goid"));
        orderManager.saveOrder(order);
        SendAgent.sendCallbackToServer(this.orderManager, order);
        this.renderState(true);

    }
    private void renderState(boolean suc){
        String res = "success";
        if (!suc) {
            res = "fail";
        }
        renderText(res);
    }


}




