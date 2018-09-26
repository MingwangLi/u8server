package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.StringUtils;
import com.u8.server.web.pay.SendAgent;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Namespace("/pay/leishen")
public class LeiShenPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UOrderManager orderManager;

    private Integer app;     //应用id
    private Long at;         //订单创建时间(系统时间毫秒数)
    private String cbi;   //透传参数
    private Integer cn;      //渠道Id
    private Integer fee;   //充值金额 分
    private String kr;      //随机数
    private Integer pt;  //支付方式 2-支付宝网页 3-微信web 4-支付宝app  5-微信插件  16-平台币/钱包余额）
    private Integer res;  //支付结果 0支付成功,其他失败
    private Long st;    //当前服务器时间(系统时间毫秒数)
    private Long tid;  //流水号
    private String ud; //cp订单号      uid ver  sign
    private Integer uid; //用户id
    private Integer ver; //版本号
    private String sign;

    public void setApp(Integer app) {
        this.app = app;
    }

    public void setAt(Long at) {
        this.at = at;
    }

    public void setCbi(String cbi) {
        this.cbi = cbi;
    }

    public void setCn(Integer cn) {
        this.cn = cn;
    }

    public void setFee(Integer fee) {
        this.fee = fee;
    }

    public void setKr(String kr) {
        this.kr = kr;
    }

    public void setPt(Integer pt) {
        this.pt = pt;
    }

    public void setRes(Integer res) {
        this.res = res;
    }

    public void setSt(Long st) {
        this.st = st;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public void setUd(String ud) {
        this.ud = ud;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public void setVer(Integer ver) {
        this.ver = ver;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }



    @Action("payCallback")
    public void payCallback() {
        try {
            logger.info("----雷神支付回调app:{}",app);
            logger.info("----雷神支付回调at:{}",at);
            logger.info("----雷神支付回调cbi:{}",cbi);
            logger.info("----雷神支付回调cn:{}",cn);
            logger.info("----雷神支付回调fee:{}",fee);
            logger.info("----雷神支付回调kr:{}",kr);
            logger.info("----雷神支付回调pt:{}",pt);
            logger.info("----雷神支付回调res:{}",res);
            logger.info("----雷神支付回调st:{}",st);
            logger.info("----雷神支付回调tid:{}",tid);
            logger.info("----雷神支付回调ud:{}",ud);
            logger.info("----雷神支付回调uid:{}",uid);
            logger.info("----雷神支付回调ver:{}",ver);
            logger.info("----雷神支付回调sign:{}",sign);
            if (StringUtils.isEmpty(sign)) {
                logger.warn("----雷神支付回调签名为空");
                renderText("FAILURE");
                return;
            }
            Long orderID = Long.parseLong(ud);
            UOrder order = orderManager.getOrder(orderID);
            if (null == order) {
                logger.warn("----雷神支付回调查询订单不存在,orderID:{}",order);
                renderText("FAILURE");
                return;
            }
            UChannel channel = order.getChannel();
            if (null == channel) {
                logger.warn("----雷神支付回调查询渠道不存在,channelID:{}",order.getChannelID());
                renderText("FAILURE");
                return;
            }
            if (0 != res) {
                logger.warn("----雷神支付回调查询支付状态为:{}",res);
                renderText("FAILURE");
                return;
            }
            String secret = channel.getCpAppSecret();
            StringBuilder sb = new StringBuilder();
            sb.append("app=").append(app).
                    append("&at=").append(at).
                    append("&cbi=").append(cbi).
                    append("&cn=").append(cn).
                    append("&fee=").append(fee).
                    append("&kr=").append(kr).
                    append("&pt=").append(pt).
                    append("&res=").append(res).
                    append("&st=").append(st).
                    append("&tid=").append(tid).
                    append("&ud=").append(ud).
                    append("&uid=").append(uid).
                    append("&ver=").append(ver).
                    append(secret);
            logger.debug("----雷神支付回调签名体:{}",sb.toString());
            String checkSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!sign.equals(checkSign)) {
                logger.warn("----雷神支付回调验签失败sign:{}",sign);
                renderText("FAILURE");
                return;
            }
            synchronized (this) {
                if (order.getState() > PayState.STATE_PAYING) {
                    logger.warn("----雷神 the order has already complete by u8Server,the order state is {}",order.getState());
                    renderText("SUCCESS");
                    return;
                }
                order.setState(PayState.STATE_SUC);
                order.setCompleteTime(new Date());
                order.setRealMoney(fee);
                order.setChannelOrderID(tid+"");
                order.setSdkOrderTime(st+"");
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(orderManager,order);
                renderText("SUCCESS");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("----雷神支付回调异常:{}",e.getMessage());
            renderText("FAILURE");
        }
    }
}
