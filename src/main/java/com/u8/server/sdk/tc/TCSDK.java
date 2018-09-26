package com.u8.server.sdk.tc;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lvxinmin on 2016/11/14.
 */
public class TCSDK implements ISDKScript {

    private String game_id = null;
    private String token = null;

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        try {

            game_id = channel.getCpAppID();

            UHttpAgent httpClient = UHttpAgent.getInstance();

            Map<String, String> params = new HashMap<String, String>();
            params.put("game_id", game_id);
            params.put("token", extension);

            String url = channel.getChannelAuthUrl();

            httpClient.get(url, params, new UHttpFutureCallback() {

                @Override
                public void completed(String result) {
                    Log.d("---------------------------TC verify SUCESS------------------------------");
                    Log.d("------TC verify result = " + result);
                    if (result != null) {
                            JSONObject data = JSONObject.fromObject(result);
                            int status = data.getInt("status");
                            String user_id = data.optString("user_id");
                            if (1 == status) {
                                Log.d("------TC---------------------------------------------------------------------" + result);
                                callback.onSuccess(new SDKVerifyResult(true, user_id, user_id, "", ""));
                                return;
                            }
                    }
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
                }

                @Override
                public void failed(String e) {
                    Log.d("---------------------------TC verify FAILED------------------------------");
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + e);
                }
            });

        } catch (Exception e) {
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is " + e.getMessage());
            Log.e(e.getMessage());
        }

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        if (callback != null) {
            callback.onSuccess(user.getName());
        }
    }


}
