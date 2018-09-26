package com.u8.server.sdk.yijie;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class YiJieSDK implements ISDKScript {

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        JSONObject jsonObject = JSONObject.fromObject(extension);
        String app = jsonObject.getString("app");
        String sdk = jsonObject.getString("sdk");
        String uin = jsonObject.getString("uin");
        String sess = jsonObject.getString("sess");
        Log.d("app:%s,sdk:%s,uin:%s,sess:%s");
        Map<String,String> map = new HashMap<>();
        map.put("app",app);
        map.put("sdk",sdk);
        map.put("uin",uin);
        map.put("sess",sess);
        final Integer SUCCESS = 0;
        UHttpAgent.getInstance().post(channel.getChannelAuthUrl(), map, new UHttpFutureCallback() {
            @Override
            public void completed(String content) {
                Log.i("易接登陆成功,返回的数据:%s",content);
                if (SUCCESS == Integer.valueOf(content)) {
                    callback.onSuccess(new SDKVerifyResult(true,content,content,content));
                    return;
                }
                callback.onFailed(content);
                return;
            }

            @Override
            public void failed(String err) {
                Log.i("易接登陆失败,返回的信息:%s",err);
                callback.onFailed(err);
            }
        });
    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        callback.onSuccess(user.getChannel().getChannelAuthUrl());
    }
}
