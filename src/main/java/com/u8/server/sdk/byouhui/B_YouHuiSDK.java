package com.u8.server.sdk.byouhui;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import net.sf.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 爱谱游戏 SDK
 * create by lvxinmin
 */

public class B_YouHuiSDK implements ISDKScript {

    public String sign = null;
    public String logintime = null;
    public String username = null;
    public String appkey = null;


    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        try {
            JSONObject json = JSONObject.fromObject(extension);
            sign = json.optString("sign");
            logintime = json.optString("logintime");
            username = json.optString("username");
            appkey = channel.getCpAppKey();

            if (sign.equals(generateSign(username, appkey, logintime))) {
                callback.onSuccess(new SDKVerifyResult(true, username, username, "", ""));
            } else {
                callback.onFailed("verify failed | sign error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess("");
        }
    }


    public String generateSign(String username, String appkey, String logintime) {
        String sign = "";
        String Res = "username=" + username + "&appkey=" + appkey + "&logintime=" + logintime;
        sign = md5(Res);
        return sign;
    }


    public String md5(String res) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffers = md.digest(res.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < buffers.length; i++) {
                String s = Integer.toHexString(0xff & buffers[i]);
                if (s.length() == 1) {
                    sb.append("0" + s);
                }
                if (s.length() != 1) {
                    sb.append(s);
                }
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}



