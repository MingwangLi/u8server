package com.u8.server.sdk.jiuqu;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/****
 * 九趣游戏
 * create by lvxinmin
 */

public class JiuQuSDK implements ISDKScript {

    public String app_id = null;
    public String mem_id = null;
    public String user_token = null;
    public String sign = null;

    @Override
    public void verify(final UChannel channel, final String extension, final ISDKVerifyListener callback) {

        Log.d("---------------------------------------SDK--------------------------------------");
            JSONObject json = JSONObject.fromObject(extension);
            app_id = channel.getCpAppID();
            mem_id = json.optString("mem_id");
            user_token = json.optString("user_token");
            sign = generateSign(app_id, mem_id, user_token, channel.getCpAppKey());

            JSONObject jsonData = new JSONObject();
            jsonData.put("app_id", app_id);
            jsonData.put("mem_id", mem_id);
            jsonData.put("user_token", user_token);
            jsonData.put("sign", sign);

            UHttpAgent httpClient = UHttpAgent.getInstance();
            httpClient.post(channel.getChannelAuthUrl(), jsonData.toString(), new UHttpFutureCallback() {

                @Override
                public void completed(String result) {
                    Log.d("------------SDK-Server auth result == " + result);
                        JSONObject json = JSONObject.fromObject(result);
                        String status = json.getString("status");
                        String msg = json.getString("msg");
                        if ("1".equals(status)) {
                            callback.onSuccess(new SDKVerifyResult(true, mem_id, mem_id, "", msg));
                            return;
                        }
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
                }

                @Override
                public void failed(String err) {
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + err);
                }
            });
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess("");
        }
    }


    //登录 生成sign
    public String generateSign(String appid, String mem_id, String user_token, String app_key) {
        String gignRes = "app_id=" + appid + "&mem_id=" + mem_id + "&user_token=" + user_token + "&app_key=" + app_key;
        String sign = md5(gignRes.toString());
        return sign;
    }

    public static String md5(String name) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffers = md.digest(name.getBytes());
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
