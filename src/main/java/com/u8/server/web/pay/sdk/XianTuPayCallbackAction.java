package com.u8.server.web.pay.sdk;

import com.u8.server.cache.CacheManager;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.StringUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;

@Namespace("/pay/xiantu")
public class XianTuPayCallbackAction extends UActionSupport{


    private String out_trade_no;

    private Double price;

    private Integer pay_status;

    private String extend;

    private String signType;

    private String sign;

    @Autowired
    private UOrderManager uOrderManager;

    @Action("payCallback")
    public void payCallback() {
        //哎 多此一举了 以后还是直接用属性吧  毕竟要验签
        try {
            Log.i("---------------订单id:%s",out_trade_no);
            Log.i("---------------价格:"+price);
            Log.i("---------------订单状态:"+pay_status);
            Log.i("---------------extend:%s",extend);
            Log.i("---------------signType:%s",signType);
            Log.i("---------------sign:%s",sign);
            if (StringUtils.isEmpty(sign)) {
                Log.d("----闲兔支付回调sign为空");
                this.renderState("fail");
                return ;
            }
            Long orderID = Long.parseLong(extend);
            UOrder order = uOrderManager.getOrder(orderID);
            if (null == order) {
                Log.d("------闲兔支付回调查询订单不存，订单id:"+extend);
                this.renderState("fail");
                return;
            }
            Integer channelID = order.getChannelID();
            if (null == channelID) {
                Log.d("----闲兔支付回调查询渠道id为null");
                this.renderState("fail");
                return;
            }
            UChannel uChannel = CacheManager.getInstance().getChannel(channelID);
            if (null == uChannel) {
                Log.d("----闲兔支付回调查询渠道为null");
                this.renderState("fail");
                return ;
            }
            if(order.getState() > PayState.STATE_PAYING){
                Log.i("The state of the order is complete. The state is " + order.getState());
                this.renderState("fail");
                return;
            }
            String key = uChannel.getCpAppKey();
            Log.i("----闲兔支付回调查询key:%s",key);
            StringBuilder sb = new StringBuilder();
            DecimalFormat dcmFmt = new DecimalFormat("0.00");
            String money = dcmFmt.format(price);
            sb.append(out_trade_no).append(money).append(pay_status).append(extend).append(key);
            Log.i("-----闲兔支付验签Content:%s",sb.toString());
            if (!StringUtils.isEmpty(key)) {
                String createSign = EncryptUtils.md5(sb.toString()).toLowerCase();
                Log.i("----闲兔支付生成的sign:%s",createSign);
                if (!sign.equals(createSign)) {
                    Log.i("----闲兔支付验签失败,sign:%s",sign);
                    this.renderState("fail");
                    return ;
                }
               if (PayState.STATE_PAYING == pay_status) {
                    order.setChannelOrderID(out_trade_no);
                    Integer realMoney  = (int)(price * 100);
                    order.setRealMoney(realMoney);
                    order.setCompleteTime(new Date());
                    order.setState(PayState.STATE_SUC);
                    uOrderManager.saveOrder(order);
                    SendAgent.sendCallbackToServer(this.uOrderManager, order);
                    renderState("success");
                    return;
               } else {
                   order.setState(PayState.STATE_FAILED);
                   uOrderManager.saveOrder(order);
                   renderState("fail");
                   return;
               }
            }
        } catch (Exception e) {
            Log.e("----闲兔支付回调异常:%s",e.getMessage());
        }

    }

    private void renderState(String resultMsg) throws IOException {
        renderText(resultMsg);
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setPay_status(Integer pay_status) {
        this.pay_status = pay_status;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public void setuOrderManager(UOrderManager uOrderManager) {
        this.uOrderManager = uOrderManager;
    }
}
