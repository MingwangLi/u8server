package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.StringUtils;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;

@Namespace("/pay/mianshangdian")
public class MianShangDianPayCallbackAction extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
//String appId,String act, String productName,String consumeStreamId,
// 			String cooOrderSerial,String uin,String goodsId,String goodsInfo,String goodsCount,
// 			String originalMoney,String orderMoney,String note,
// 			String payStatus,String createTime,String fromSign
    private String AppId;
    private String Act;
    private String ProductName;
    private String ConsumeStreamId;
    private String CooOrderSerial;
    private String Uin;
    private String GoodsId;
    private String GoodsInfo;
    private String GoodsCount;
    private String OriginalMoney;
    private String OrderMoney;
    private String Note;
    private String PayStatus;
    private String CreateTime;
    private String Sign;

    public void setAppId(String appId) {
        AppId = appId;
    }

    public void setAct(String act) {
        Act = act;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public void setConsumeStreamId(String consumeStreamId) {
        ConsumeStreamId = consumeStreamId;
    }

    public void setCooOrderSerial(String cooOrderSerial) {
        CooOrderSerial = cooOrderSerial;
    }

    public void setUin(String uin) {
        Uin = uin;
    }

    public void setGoodsId(String goodsId) {
        GoodsId = goodsId;
    }

    public void setGoodsInfo(String goodsInfo) {
        GoodsInfo = goodsInfo;
    }

    public void setGoodsCount(String goodsCount) {
        GoodsCount = goodsCount;
    }

    public void setOriginalMoney(String originalMoney) {
        OriginalMoney = originalMoney;
    }

    public void setOrderMoney(String orderMoney) {
        OrderMoney = orderMoney;
    }

    public void setNote(String note) {
        Note = note;
    }

    public void setPayStatus(String payStatus) {
        PayStatus = payStatus;
    }

    public void setCreateTime(String createTime) {
        CreateTime = createTime;
    }

    public void setSign(String sign) {
        Sign = sign;
    }

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback() {
        try {
            logger.debug("----免商店支付回调参数:{}",AppId);
            logger.debug("----免商店支付回调参数:{}",Act);
            logger.debug("----免商店支付回调参数:{}",ProductName);
            logger.debug("----免商店支付回调参数:{}",ConsumeStreamId);
            logger.debug("----免商店支付回调参数:{}",CooOrderSerial);
            logger.debug("----免商店支付回调参数:{}",Uin);
            logger.debug("----免商店支付回调参数:{}",GoodsId);
            logger.debug("----免商店支付回调参数:{}",GoodsInfo);
            logger.debug("----免商店支付回调参数:{}",GoodsCount);
            logger.debug("----免商店支付回调参数:{}",OriginalMoney);
            logger.debug("----免商店支付回调参数:{}",OrderMoney);
            logger.debug("----免商店支付回调参数:{}",Note);
            logger.debug("----免商店支付回调参数:{}",PayStatus);
            logger.debug("----免商店支付回调参数:{}",CreateTime);
            logger.debug("----免商店支付回调参数:{}",Sign);
            if (StringUtils.isEmpty(Sign)) {
                logger.warn("----免商店支付回调Sign为空");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ErrorCode","0");
                jsonObject.put("ErrorDesc","接收失败");
                renderText(jsonObject.toString());
                return;
            }
            Long orderID = Long.parseLong(CooOrderSerial);
            UOrder order = orderManager.getOrder(orderID);
            if (null == order) {
                logger.warn("-----免商店支付回调查询订单不存在,orderID:{}",orderID);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ErrorCode","0");
                jsonObject.put("ErrorDesc","接收失败");
                renderText(jsonObject.toString());
                return;
            }
            UChannel channel = order.getChannel();
            if (null == channel) {
                logger.warn("-----免商店支付回调查询渠道不存在,channelID:{}",order.getChannelID());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ErrorCode","0");
                jsonObject.put("ErrorDesc","接收失败");
                renderText(jsonObject.toString());
                return;
            }
            if(order.getState() > PayState.STATE_PAYING){
                //已完成订单
                logger.warn("The state of the order is complete. The state is {}",order.getState());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ErrorCode","1");
                jsonObject.put("ErrorDesc","接收成功");
                renderText(jsonObject.toString());
                return;
            }
            String key = channel.getCpAppKey();
            StringBuilder sb = new StringBuilder();
            sb.append(AppId).
                    append(Act).
                    append(ProductName).
                    append(ConsumeStreamId).
                    append(CooOrderSerial).
                    append(Uin).
                    append(GoodsId).
                    append(GoodsInfo).
                    append(GoodsCount).
                    append(OriginalMoney).
                    append(OrderMoney).
                    append(Note).
                    append(PayStatus).
                    append(CreateTime).
                    append(key);
            logger.debug("----免商店支付回调签名体:{}",sb.toString());
            String createSign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!Sign.equals(createSign)) {
                logger.warn("----免商店支付回调验签失败:{}",Sign);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ErrorCode","0");
                jsonObject.put("ErrorDesc","接收失败");
                renderText(jsonObject.toString());
                return;
            }
            if ("1".equals(PayStatus)) {
                order.setCompleteTime(new Date());
                order.setChannelOrderID(ConsumeStreamId);
                order.setSdkOrderTime(CreateTime);
                order.setState(PayState.STATE_SUC);
                BigDecimal bigDecimal = new BigDecimal(OriginalMoney);
                bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
                bigDecimal = bigDecimal.multiply(new BigDecimal("100"));
                order.setRealMoney(bigDecimal.intValue());  //以分为单位
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(orderManager,order);
                logger.info("----u8Server处理免商店支付接口完成,订单:{}",order.toJSON());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ErrorCode","1");
                jsonObject.put("ErrorDesc","接收成功");
                renderText(jsonObject.toString());
                return;
            }
            logger.warn("----免商店支付回调PayStatus:{}",PayStatus);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ErrorCode","0");
            jsonObject.put("ErrorDesc","接收失败");
            renderText(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----免商店支付回调异常:{}",e.getMessage());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ErrorCode","0");
            jsonObject.put("ErrorDesc","接收失败");
            renderText(jsonObject.toString());
        }

    }

}
