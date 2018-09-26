package com.u8.server.sdk.shendeng;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * 神灯
 * Created by lvxinmin on 16/12/05.
 */
public class ShenDengSDK implements ISDKScript {

    private String token;
    private String mobile;
    private String mid;
    private String sign;

    //产品ID(6位数字)
    public static String appId = "";
    //商户ID(6位数字)
    public static String cpId = "";
    //密钥(28位)
    public static String secret = "";

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {


        try {
            JSONObject json = JSONObject.fromObject(extension);
            token = json.getString("token");
            mobile = json.getString("mobile");
            mid = json.getString("mid");

            appId = channel.getCpAppID();
            cpId = channel.getCpID();
            secret = channel.getCpAppSecret();

            Map<String, String> params = new HashMap<String, String>();
            params.put("act", "userinfo");
            params.put("appId", appId);
            params.put("cpId", cpId);
            params.put("token", token);
            params.put("mid", mid);
            sign = generateSign(secret, "userInfo", appId, cpId, mid, token);
            params.put("sign", sign);

            String url = channel.getChannelAuthUrl();

            UHttpAgent.getInstance().get(url, params, new UHttpFutureCallback() {

                @Override
                public void completed(String result) {
                    JSONObject json = JSONObject.fromObject(result);
                    String status = json.getString("status");
                    if ("0".equals(status)) {
                        callback.onSuccess(new SDKVerifyResult(true, mid, mobile, ""));
                    }
                }

                @Override
                public void failed(String e) {
                    Log.e("------------channel server verify failed == " + e);
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + e);
                }
            });


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


    public String generateSign(String secret, String act, String appId, String cpId, String mid, String token) {
        String res = secret + "act" + act + "appId" + appId + "cpId" + cpId + "mid" + mid + "token" + token + secret;
        Log.d("---------ShenDeng--------pre sign : " + res);
        sign = preSign(res);
        Log.d("---------ShenDeng--------sign  is : " + sign);
        return sign;
    }


    /**
     * 加密指定字符串
     *
     * @param preStr 准备加密的字符串
     * @return 加密后字符串
     */
    public static String preSign(String preStr) {
        return MD5(byte2hex(preStr.getBytes()));
    }


    public static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }
        return sign.toString();
    }

    public final static String MD5(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
