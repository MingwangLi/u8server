package com.u8.server.web.pay;

import com.u8.server.cache.SDKCacheManager;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.StateCode;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.service.UOrderManager;
import com.u8.server.service.UUserManager;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/***
 * 请求获取订单号
 */
@Controller
@Namespace("/pay")
public class PayAction extends UActionSupport {
    private int userID;
    private String productID;  //当前商品ID
    private String productName;
    private String productDesc;
    private int money;          //单位 分
    private String roleID;      //玩家在游戏服中的角色ID
    private String roleName;    //玩家在游戏服中的角色名称
    private String serverID;    //玩家所在的服务器ID
    private String serverName;  //玩家所在的服务器名称
    private String extension;
    private String notifyUrl;   //支付回调通知的游戏服地址
    private String signType;    //签名算法， RSA|MD5
    private String sign;        //RSA签名

    @Autowired
    private UUserManager userManager;

    @Autowired
    private UOrderManager orderManager;

    private boolean isSignOK(UUser user) throws UnsupportedEncodingException {

        StringBuilder sb = new StringBuilder();
        sb.append("userID=").append(this.userID)
                .append("&productID=").append(this.productID)
                .append("&productName=").append(this.productName)
                .append("&productDesc=").append(this.productDesc)
                .append("&money=").append(this.money)
                .append("&roleID=").append(this.roleID)
                .append("&roleName=").append(this.roleName)
                .append("&serverID=").append(this.serverID)
                .append("&serverName=").append(this.serverName)
                .append("&extension=").append(this.extension + "");
        if (!StringUtils.isEmpty(notifyUrl)) {
            sb.append("&notifyUrl=").append(this.notifyUrl);
        }
        Log.d("The Appsecret : "+user.getGame().getAppSecret());
        sb.append(user.getGame().getAppSecret());
        Log.i("签名体Content:"+sb.toString());
        String encoded = URLEncoder.encode(sb.toString(), "UTF-8");
        String newSign = EncryptUtils.md5(encoded);
        Log.d("the newSign : " + newSign);
        return newSign.toLowerCase().equals(this.sign);
    }

    @Action("getOrderID")
    public void getOrderID() {
        Log.i("---------------------------Start GetOrderID-------------------------------------------");
        Log.i("------------------------userID: " + userID + "----------------------------------------");
        Log.i("---------------------productID: " + productID + "-------------------------------------");
        Log.i("-------------------productName: " + productName + "-----------------------------------");
        Log.i("-------------------productDesc: " + productDesc + "-----------------------------------");
        Log.i("-------------------------money: " + money + "-----------------------------------------");
        Log.i("------------------------roleID: " + roleID + "----------------------------------------");
        Log.i("----------------------roleName: " + roleName + "--------------------------------------");
        Log.i("----------------------serverID: " + serverID + "--------------------------------------");
        Log.i("--------------------serverName: " + serverName + "------------------------------------");
        Log.i("---------------------extension: " + extension + "-------------------------------------");
        Log.i("---------------------notifyUrl: " + notifyUrl + "-------------------------------------");
        Log.i("----------------------signType: " + signType + "--------------------------------------");
        Log.i("--------------------------sign: " + sign + "------------------------------------------");
        Log.i("--------------------------------------------------------------------------------------");
        try {
            UUser user = userManager.getUser(this.userID);
            Integer appID = user.getAppID();
            if (user == null) {
                Log.e("the user is not found. userID:" + this.userID);
                renderState(StateCode.CODE_USER_NONE, null);
                return;
            }

            if (money < 0) {
                Log.e("the money is not valid. money:" + money);
                renderState(StateCode.CODE_MONEY_ERROR, null);
                return;
            }

            if(1 != appID) {
                if (!isSignOK(user)) {
                    Log.e("the sign is not valid. sign:" + this.sign);
                    renderState(StateCode.CODE_SIGN_ERROR, null);
                    return;
                }
            }
            if (!user.getChannel().isPayOpen()) {
                Log.e("the pay not opened in u8server manage system.. ");
                renderState(StateCode.CODE_PAY_CLOSED, null);
                return;
            }
            Log.d("充值关闭时间:"+user.getChannel().getChargeCloseTime());
            if (!StringUtils.isEmpty(user.getChannel().getChargeCloseTime())) {
                String[] array = user.getChannel().getChargeCloseTime().split("_");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date begin = sdf.parse(array[0]);
                Date end = sdf.parse(array[1]);
                Date now = new Date();
                if (now.after(begin) && now.before(end)) {
                    Log.e("the pay is closed on the channel at this time....");
                    renderState(StateCode.CODE_PAY_CLOSED_Time, null);
                    return;
                }
            }
            Log.i("==================================start generate order========================================");
            final UOrder order = orderManager.generateOrder(user, money, productID, productName, productDesc, roleID, roleName, serverID, serverName, extension, notifyUrl);
            Log.i("==================================end generate order========================================");

            if (1 == appID) {
                //模拟渠道  通知cp发放道具
                SendAgent.sendCallbackToServer(this.orderManager, order);
                JSONObject data = new JSONObject();
                data.put("order", order.getOrderID() + "");
                data.put("extension", user.getChannel().getChannelAuthUrl());
                renderState(StateCode.CODE_SUCCESS, data);
                return;
            }
            if (order != null) {
                ISDKScript script = SDKCacheManager.getInstance().getSDKScript(order.getChannel());

                if (script == null) {
                    Log.e("the ISDKScript is not found. channelID:" + order.getChannelID());
                    renderState(StateCode.CODE_ORDER_ERROR, null);
                    return;
                }

                script.onGetOrderID(user, order, new ISDKOrderListener() {

                    @Override
                    public void onSuccess(String jsonStr) {
                        if (jsonStr.contains("meizu")) {
                            JSONObject object = JSONObject.fromObject(jsonStr);
                            jsonStr = object.toString();
                        }

                        Log.i("---------------Get order success | jsonStr = "+jsonStr+"");
                        JSONObject data = new JSONObject();
                        data.put("order", order.getOrderID() + "");
                        data.put("extension", jsonStr);
                        renderState(StateCode.CODE_SUCCESS, data);
                    }

                    @Override
                    public void onFailed(String err) {
                        Log.i("---------------Get order failed | err = " + err);
                        JSONObject data = new JSONObject();
                        data.put("orderID", order.getOrderID() + "");
                        data.put("extension", "");
                        renderState(StateCode.CODE_SUCCESS, data);
                    }
                });
            }
        } catch (Exception e) {
            renderState(StateCode.CODE_ORDER_ERROR, null);
            Log.e(e.getMessage());
        }
    }

    private String OderNo;

    private String orderId;

    public void setOderNo(String oderNo) {
        OderNo = oderNo;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Action("/getYiJieOrderID")
    public void getYiJieOrderID() {
        Log.i("----易接订单号:%s",OderNo);
        Long orderID = Long.parseLong(orderId);
        UOrder uorder = orderManager.getOrder(orderID);
        uorder.setChannelOrderID(OderNo);
        orderManager.saveOrder(uorder);
    }


    private void renderState(int state, JSONObject data) {
        JSONObject json = new JSONObject();
        json.put("state", state);
        json.put("data", data);
        Log.d("---------------back to sdk---------------:" + json.toString());
        super.renderJson(json.toString());
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public String getRoleID() {
        return roleID;
    }

    public void setRoleID(String roleID) {
        this.roleID = roleID;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }


}
