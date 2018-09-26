package com.u8.server.sdk.hongshouzhi;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 红手指 SDK
 */

public class HongShouZhiSDK implements ISDKScript {

    String app_id = null;
    String mem_id = null;
    String user_token = null;

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {

        JSONObject json = JSONObject.fromObject(extension);
        app_id = channel.getCpID();
        mem_id = json.optString("mem_id");
        user_token = json.optString("user_token");
        String sign = generateSign(app_id, mem_id, user_token, channel.getCpAppSecret());


        JSONObject data = new JSONObject();
        data.put("app_id", app_id);
        data.put("mem_id", mem_id);
        data.put("user_token", user_token);
        data.put("sign", sign);

        String s = data.toString();

        UHttpAgent httpClient = UHttpAgent.getInstance();
        httpClient.post(channel.getChannelAuthUrl(), s, new UHttpFutureCallback() {

            @Override
            public void completed(String result) {
                Log.d("The auth result is == " + result);
                JSONObject json = JSONObject.fromObject(result);
                String status = json.optString("status");
                String msg = json.optString("msg");
                if ("1".equals(status)) {
                    callback.onSuccess(new SDKVerifyResult(true, mem_id, mem_id, "", msg));
                    return;
                }
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
            }


            @Override
            public void failed(String e) {
                callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + e);
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
    public static String generateSign(String app_id, String mem_id, String user_token, String app_key) {
        String Res = "app_id=" + app_id + "&mem_id=" + mem_id + "&user_token=" + user_token + "&app_key=" + app_key;
        return md5(Res.toString());
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
