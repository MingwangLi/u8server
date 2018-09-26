package com.u8.server.sdk.lefeifan;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

/**
 * Created by ${lvxinmin} on 2016/11/30.
 */
public class LeFeiFanSDK implements ISDKScript {

    private String userId = null;
    private String timestamp = null;
    private String sign = null;

    @Override
    public void verify(final UChannel channel, String extension, ISDKVerifyListener callback) {

        JSONObject json = JSONObject.fromObject(extension);
        userId = json.optString("userId");
        timestamp = json.optString("timestamp");
        sign = json.optString("sign");

        if (genrateSign(channel, sign)) {
            callback.onSuccess(new SDKVerifyResult(true, userId, userId, "", ""));
        } else {
            callback.onFailed(channel.getMaster().getSdkName() + " ------verify failed | sign not match");
        }
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {

    }


    public boolean genrateSign(UChannel channel, String sign) {
        String Res = userId + "&" + timestamp + "&" + channel.getCpAppKey();
        String s = EncryptUtils.md5(Res.toString());
        return sign.equals(s) ? true : false;
    }
}
