package com.u8.server.sdk.jolo;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import net.sf.json.JSONObject;

import java.io.UnsupportedEncodingException;


/**
 * @Author: lizhong
 * @Date: 2018/4/3 10:18.
 * 聚乐JoLoPlay(HTC) SDK
 */
public class JoLoSDK implements ISDKScript{

    private String userId;
    private String userName;
    private String account;
    private String account_sign;
    private String session;

    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback){
        JSONObject json = JSONObject.fromObject(extension);
        userId = json.getString("userId");
        userName = json.getString("userName");
        account = json.getString("account");
        account_sign = json.getString("accountSign");
        session = json.getString("session");
        String a1 = account.substring(1,account.length());
        String a2 = a1.substring(0,a1.length() - 1);
        if(account==null || account_sign==null){
            callback.onFailed("account and accountSign must be needed");
            return;
        }
        if (RSASignature.doCheck(a2,account_sign,channel.getCpAppKey())){
            callback.onSuccess(new SDKVerifyResult(true,userId,userName,userName));
            return;
        }
        callback.onFailed(channel.getMaster().getSdkName() + " verify failed!");

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws UnsupportedEncodingException {
        /*String callbackurl = user.getChannel().getPayCallbackUrl();
        Order or = new Order();
        or.setAmount(order.getMoney() + ""); // 设置支付金额，单位分
        or.setGameCode(user.getChannel().getCpAppID()); // 设置游戏唯一ID,由Jolo提供
        or.setGameName(user.getGame().getName()); // 设置游戏名称
        or.setGameOrderid(order.getOrderID() + ""); // 设置游戏订单号
        or.setNotifyUrl(callbackurl); // 设置支付通知
        or.setProductDes(order.getProductDesc()); // 设置产品描述
        or.setProductID(order.getProductID()); // 设置产品ID
        or.setProductName(order.getProductName()); // 设置产品名称
        or.setSession(session); // 设置用户session
        or.setUsercode(userId); // 设置用户ID
        String jsonOrder = or.toJsonOrder();
        String sign = RSASignature.sign(or.toJsonOrder(),user.getChannel().getCpPayPriKey()); // 签名
        */
        JSONObject data = new JSONObject();
        //user.getChannel().getPayCallbackUrl()
        data.put("prikey",user.getChannel().getCpPayPriKey());
        data.put("callbackurl",user.getChannel().getPayCallbackUrl());
        callback.onSuccess(data.toString());
    }
    public boolean isSignOK(String account, String account_sign,String appKey) {
        return RSASignature.doCheck(account,account_sign,appKey);
    }

}
