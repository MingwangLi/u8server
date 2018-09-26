package com.u8.server.web.pay.sdk;

import com.u8.server.cache.CacheManager;
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
import java.util.Date;

@Controller
@Namespace("/pay/yijie")
public class YiJiePayCallbackAction extends UActionSupport {


    @Autowired
    private UOrderManager orderManager;

    @Autowired
    private UChannelManager channelManager;

    //private String secret = "YU583S3Z3SV522DBYOGA8HTTKIMQ5K0G";


    private String app;

    private String cbi;

    private Long ct;

    private Integer fee;

    private Long pt;

    private String sdk;

    private String ssid;

    private Integer st;

    private String tcd;

    private String uid;

    private String ver;

    private String sign;

    public void setApp(String app) {
        this.app = app;
    }

    public void setCbi(String cbi) {
        this.cbi = cbi;
    }

    public void setCt(Long ct) {
        this.ct = ct;
    }

    public void setFee(Integer fee) {
        this.fee = fee;
    }

    public void setPt(Long pt) {
        this.pt = pt;
    }

    public void setSdk(String sdk) {
        this.sdk = sdk;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public void setSt(Integer st) {
        this.st = st;
    }

    public void setTcd(String tcd) {
        this.tcd = tcd;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Action("payCallback")
    public void payCallback(){

        Log.i("----app:"+app);
        Log.i("----cbi:"+cbi);
        Log.i("----ct:"+ct);
        Log.i("----fee:"+fee);
        Log.i("----pt:"+pt);
        Log.i("----sdk:"+sdk);
        Log.i("----ssid:"+ssid);
        Log.i("----st:"+st);
        Log.i("----tcd:"+tcd);
        Log.i("----uid:"+uid);
        Log.i("----ver:"+ver);
        Log.i("----sign:"+sign);
        try {
            if (StringUtils.isEmpty(sign)) {
                Log.d("----易接支付回调签名为空");
                this.renderState("fail");
                return ;
            }
            if (StringUtils.isEmpty(tcd)) {
                Log.d("----订单id为空");
                this.renderState("fail");
                return ;
            }
            if (null == fee) {
                Log.d("----充值金额为null");
                this.renderState("fail");
                return ;
            }
            UOrder order = orderManager.getOrderByChannelOrderID(tcd);
            //Long orderID = Long.parseLong(tcd);
            //UOrder order = orderManager.getOrder(orderID);
            Integer channelID = order.getChannelID();
            if (null == channelID) {
                Log.d("----易接支付回调查询渠道id为null");
                this.renderState("fail");
                return;
            }
            UChannel uChannel = CacheManager.getInstance().getChannel(channelID);
            if (null == uChannel) {
                Log.d("---易接支付回调查询渠道为null");
                this.renderState("fail");
                return ;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("app=").append(app).
                    append("&cbi=").append(cbi).
                    append("&ct=").append(ct).
                    append("&fee=").append(fee).
                    append("&pt=").append(pt).
                    append("&sdk=").append(sdk).
                    append("&ssid=").append(ssid).
                    append("&st=").append(st).
                    append("&tcd=").append(tcd).
                    append("&uid=").append(uid).
                    append("&ver=").append(ver).
                    append(uChannel.getCpPayKey());
            Log.i("----签名体:"+sb.toString());
            String createSign = EncryptUtils.md5(sb.toString());
            Log.i("----createSign:"+createSign);
            if (!sign.equals(createSign)) {
                Log.d("----易接支付验签失败");
                this.renderState("fail");
                return ;
            }
            if(order.getState() > PayState.STATE_PAYING){
                Log.i("The state of the order is complete. The state is " + order.getState());
                this.renderState("fail");
                return;
            }
            order.setRealMoney(fee);
            order.setCompleteTime(new Date());
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(this.orderManager, order);
            renderState("SUCCESS");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void renderState(String resultMsg) throws IOException {
        renderText(resultMsg);
    }


}
