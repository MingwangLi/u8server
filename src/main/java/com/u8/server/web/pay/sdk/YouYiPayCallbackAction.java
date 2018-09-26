package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.SignUtils;
import com.u8.server.utils.StringUtils;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Namespace("/pay/youyi")
public class YouYiPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback() {
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        String line = null;
        try {
            is = request.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            while (null != (line = br.readLine())) {
                sb.append(line);
            }
            is.close();
            br.close();
            logger.info("----优亿支付回调参数:{}",sb.toString());
            JSONObject jsonObject = JSONObject.fromObject(sb.toString());
            if (StringUtils.isEmpty(jsonObject.getString("signature"))) {
                logger.warn("----优亿支付回调查询signature为空");
                renderText("fail");
                return;
            }
            String orderID = jsonObject.getString("out_trade_no");
            UOrder order = orderManager.getOrder(Long.parseLong(orderID));
            if (null == order) {
                logger.warn("----优亿支付回调查询订单不存在,订单id:{}",orderID);
                renderText("fail");
                return;
            }
            UChannel channel = order.getChannel();
            if (null == channel) {
                logger.warn("----优亿支付回调查询渠道为null,channelID:{}",order.getChannelID());
                renderText("fail");
                return;
            }
            if(order.getState() > PayState.STATE_PAYING){
                //已完成订单
                logger.warn("The state of the order is complete. The state is {}",order.getState());
                renderText("success");
                return;
            }
            Map<String,String> param = new HashMap<String ,String>();
            param.put("app_id",jsonObject.getString("app_id"));
            param.put("sourceId",jsonObject.getString("sourceId"));
            param.put("out_trade_no",jsonObject.getString("out_trade_no"));
            param.put("pay_type",jsonObject.getString("pay_type"));
            param.put("subject_id",jsonObject.getString("subject_id"));
            param.put("subject",jsonObject.getString("subject"));
            param.put("total_fee",jsonObject.getInt("total_fee")+"");
            param.put("unit_fee",jsonObject.get("unit_fee")+"");
            param.put("items",jsonObject.getInt("items")+"");
            param.put("pay_status",jsonObject.getString("pay_status"));
            param.put("pay_time",jsonObject.get("pay_time")+"");
            param.put("sourceId",jsonObject.getString("sourceId"));
            String key = channel.getCpAppKey();
            String createSign = SignUtils.createSignWithLastYu(param,key,"优亿");
            if (!jsonObject.getString("signature").equals(createSign)) {
                logger.warn("----优亿支付回调验签失败:{}",jsonObject.getString("signature"));
                renderText("fail");
                return;
            }
            if ("1".equals(jsonObject.getString("pay_status"))) {
                //更新订单信息
                //order.setChannelOrderID();  //他们不传他们订单号
                order.setSdkOrderTime(jsonObject.get("pay_time")+"");
                order.setCompleteTime(new Date());
                order.setState(PayState.STATE_SUC);
                order.setRealMoney(jsonObject.getInt("total_fee"));
                orderManager.saveOrder(order);
                logger.info("----U8处理优亿支付成功,订单:{}",order.toJSON());
                SendAgent.sendCallbackToServer(orderManager,order);
                renderText("success");
                return;
            }
            logger.warn("----优亿支付回调pay_status错误:{}",jsonObject.getString("pay_status"));
            renderText("fail");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----优亿支付回调异常:{}",e.getMessage());
            renderText("fail");
        }
    }
}
