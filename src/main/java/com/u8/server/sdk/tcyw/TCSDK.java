package com.u8.server.sdk.tcyw;

import com.u8.server.cache.UApplicationContext;
import com.u8.server.dao.UChargePointDao;
import com.u8.server.data.ChargePoint;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * @Author: lizhong
 * @Des: 天橙游玩
 * @Date: 2018/3/9 11:37
 * @Modified:
 */
public class TCSDK implements ISDKScript{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback) throws Exception {
        JSONObject json = JSONObject.fromObject(extension);
        String account = json.getString("LoginAccount");
        callback.onSuccess(new SDKVerifyResult(true,account,account,account));
        return;
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        if (callback != null) {
            Integer appID = order.getAppID();
            if (null == appID) {
                callback.onFailed("订单对应游戏不存在");
                return;
            }
            if (appID <= 11) {
                //为了不影响之前线上的游戏(11之前 计费点固定死) 后来新加的游戏本应该是在13之后 却被改为了4 导致走了之前的逻辑
                if (4 != appID) {
                    callback.onSuccess(user.getChannel().getPayCallbackUrl());
                    return;
                }
            }
            ApplicationContext applicationContext = UApplicationContext.getApplicationContext();
            UChargePointDao uChargePointDao = (UChargePointDao)applicationContext.getBean("uChargePointDao");
            ChargePoint chargePoint = uChargePointDao.getChargePointByChannelIDAndProductID(order.getChannelID(),order.getProductID());
            if (null == chargePoint) {
                callback.onSuccess(order.getChannel().getPayCallbackUrl());
                return;
            }
            JSONObject object = new JSONObject();
            object.put("channelChargeCode",chargePoint.getChannelChargeCode());
            callback.onSuccess(object.toString());
        }
    }
}
