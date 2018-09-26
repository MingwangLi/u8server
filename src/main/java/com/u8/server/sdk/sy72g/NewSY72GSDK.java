package com.u8.server.sdk.sy72g;

import com.u8.server.cache.CacheManager;
import com.u8.server.data.UChannel;
import com.u8.server.data.UGame;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * @Author: lizhong
 * @Des:
 * @Date: 2018/4/18 9:58
 * @Modified:
 */
public class NewSY72GSDK implements ISDKScript {

    public static String commitOrderUrl = "http://easysdk.72g.com/Api/Pay/PlaceOrder";

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        JSONObject object = JSONObject.fromObject(extension);
        String token = object.getString("token");
        Map<String,String> param = new HashMap<>();
        param.put("token",token);
        UHttpAgent agent = UHttpAgent.getInstance();
        agent.post(channel.getChannelAuthUrl(), param, new UHttpFutureCallback() {
            @Override
            public void completed(String content) {
                JSONObject jsonObject = JSONObject.fromObject(content);
                int status = jsonObject.getInt("status");
                jsonObject = jsonObject.getJSONObject("data");
                String channelUserID = jsonObject.getString("uid");
                if (1 == status) {
                    callback.onSuccess(new SDKVerifyResult(true,channelUserID,"",""));
                    return;
                }
                callback.onFailed(channel.getMaster().getSdkName() + "verify failed. the post result is " + content);
            }

            @Override
            public void failed(String err) {
                callback.onFailed(channel.getMaster().getSdkName() + "verify failed " + err);
            }
        });
    }



    @Override
    public void onGetOrderID(UUser user, UOrder order, final ISDKOrderListener callback) throws Exception {
        if (null == order) {
            return ;
        }
        final Long oid = order.getOrderID();
        String uid = null;
        if (null != user) {
            uid = user.getChannelUserID();
        }

        //Integer gameid = order.getAppID();
        String gameid = CacheManager.getInstance().getChannel(order.getChannelID()).getCpID();
        String goods_name = order.getProductName();
        String  goods_num =  "1";
        Integer money = order.getMoney();
        Integer goods_note = order.getState();
        String ext = null;
        StringBuilder sb = new StringBuilder();
        Integer channelID = order.getChannelID();
        UChannel channel = CacheManager.getInstance().getChannel(channelID);
        //UGame ugame = CacheManager.getInstance().getGame(gameid);

        sb.append("gameid=").append(gameid).append("goods_name=").append(goods_name).append("goods_note=").append(goods_note).append("goods_num=").append(goods_num).append("money=").
                append(money).append("oid=").append(oid).append("uid=").append(uid).append(channel.getCpAppSecret());
        String sign = EncryptUtils.md5(sb.toString());
        UHttpAgent agent = UHttpAgent.getInstance();
        Map<String,String> param = new HashMap<String,String>();
        param.put("oid",oid+"");
        param.put("uid",uid+"");
        param.put("gameid",gameid);
        param.put("goods_name",goods_name);
        param.put("goods_num",goods_num);
        param.put("money",money+"");
        param.put("goods_note",goods_note+"");
        param.put("ext",ext);
        param.put("sign",sign);
        Log.i("获取订单id接口参数:"+param.toString());
        agent.post(commitOrderUrl, param, new UHttpFutureCallback() {
            @Override
            public void completed(String content) {
                JSONObject object = JSONObject.fromObject(content);
                int status = object.getInt("status");
                object = object.getJSONObject("data");
                String orderid = object.getString("orderid");
                if (1 == status) {
                    //获取订单id成功
                    callback.onSuccess(orderid);
                    return;
                }
                //获取订单id失败
                Log.e("获取订单id失败 订单id="+oid);
                callback.onFailed(oid+"");
            }

            @Override
            public void failed(String err) {
                //获取订单id失败
                callback.onFailed(oid+"");
            }
        });

    }
}
