package com.u8.server.sdk.sy233;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import com.u8.server.utils.StringUtils;
import net.sf.json.JSONObject;

/**
 * 233游戏
 * Created by lizhong on 2017/11/17 16:17.
 */
public class SY233SDK implements ISDKScript{
    private String userId;
    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback) {
        JSONObject json = JSONObject.fromObject(extension);
        userId = json.getString("userId");
        if (!"".equals(userId) && !StringUtils.isEmpty(userId)) {
            callback.onSuccess(new SDKVerifyResult(true, userId, userId, userId));
            return;
        }
        callback.onFailed(channel.getMaster().getSdkName() + " verify failed." );
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getChannel().getPayCallbackUrl());
        }
    }
}
