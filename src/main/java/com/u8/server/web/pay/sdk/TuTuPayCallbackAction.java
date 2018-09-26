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

import java.io.IOException;
import java.util.Date;


@Namespace("/pay/ttzs")
public class TuTuPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(TuTuPayCallbackAction.class);

    @Autowired
    private UOrderManager uOrderManager;

    private String open_id;  //用户唯一标识
    private String cp_order_no;  //渠道订单号
    private String serial_number; //兔兔游戏平台支付流水号（请保存此订单号，方便订单追踪及对账） 渠道订单号
    private String amount;   //订单金额
    private String verfy;   //验证串

    public void setOpen_id(String open_id) {
        this.open_id = open_id;
    }

    public void setCp_order_no(String cp_order_no) {
        this.cp_order_no = cp_order_no;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setVerfy(String verfy) {
        this.verfy = verfy;
    }


    @Action("payCallback")
    public void payCallback() {
        logger.debug("----兔兔支付回调参数open_id:{}",open_id);
        logger.debug("----兔兔支付回调参数cp_order_no:{}",cp_order_no);
        logger.debug("----兔兔支付回调参数serial_number:{}",serial_number);
        logger.debug("----兔兔支付回调参数amount:{}",amount);
        logger.debug("----兔兔支付回调参数verfy:{}",verfy);
        try {
            long orderID = Long.parseLong(cp_order_no);
            UOrder uOrder = uOrderManager.getOrder(orderID);
            if (null == uOrder) {
                logger.debug("----兔兔支付回调查询订单不存在,订单id{}",orderID);
                //MailUtils.getInstance().sendMail("3462951792@qq.com","兔兔助手支付回调错误","订单id:"+cp_order_no);
                renderState("failure");
                return;
            }
            UChannel uChannel = uOrder.getChannel();
            if (null == uChannel) {
                logger.debug("----兔兔支付回调查询渠道不存在,渠道id{}",uOrder.getChannelID());
                //MailUtils.getInstance().sendMail("3462951792@qq.com","兔兔助手支付回调错误","订单id:"+cp_order_no);
                renderState("failure");
                return;
            }
            if(uOrder.getState() > PayState.STATE_PAYING){
                logger.debug("The state of the order is complete. The state is {}" , uOrder.getState());
                //MailUtils.getInstance().sendMail("3462951792@qq.com","兔兔助手支付回调错误","订单id:"+cp_order_no);
                renderState("failure");
                return;
            }
            if (StringUtils.isEmpty(verfy)) {
                logger.debug("----兔兔支付回调verfy为空");
                //MailUtils.getInstance().sendMail("3462951792@qq.com","兔兔助手支付回调错误","订单id:"+cp_order_no);
                renderState("failure");
                return;
            }
            String app_key = uChannel.getCpAppKey();
            String s_key = uChannel.getCpAppSecret();
            StringBuilder sb = new StringBuilder();
            sb.append(app_key).append(s_key).append(open_id).append(cp_order_no).append(serial_number).append(amount);
            logger.debug("----兔兔支付回调验签签名体:{}",sb.toString());
            String createSign = EncryptUtils.md5(sb.toString());
            if (!verfy.equals(createSign)) {
                logger.debug("----兔兔支付回调verfy不合法");
                //MailUtils.getInstance().sendMail("3462951792@qq.com","兔兔助手支付回调错误","订单id:"+cp_order_no);
                renderState("failure");
                return;
            }
            amount = amount.substring(0,amount.indexOf("."));
            int realMoney = Integer.parseInt(amount)*100;
            uOrder.setRealMoney(realMoney);
            uOrder.setCompleteTime(new Date());
            uOrder.setChannelOrderID(serial_number);
            uOrder.setState(PayState.STATE_SUC);
            uOrderManager.saveOrder(uOrder);
            SendAgent.sendCallbackToServer(uOrderManager,uOrder);
            renderState("success");
        }catch (Exception e) {
            logger.error("----兔兔支付回调异常,异常信息{}",e.getMessage());
            //MailUtils.getInstance().sendMail("3462951792@qq.com","兔兔助手支付回调异常","订单id:"+cp_order_no+" 异常信息:"+e.getMessage());
            try {
                renderState("failure");
            } catch (IOException e1) {
                e1.printStackTrace();
                logger.error("----兔兔支付回调向客户端发送数据IO异常,异常信息:{}",e1.getMessage());
            }
        }
    }

    private void renderState(String resultMsg) throws IOException {
        renderText(resultMsg);
    }

}
