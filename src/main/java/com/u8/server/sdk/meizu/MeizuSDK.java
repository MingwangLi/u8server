package com.u8.server.sdk.meizu;

import com.u8.server.cache.CacheManager;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.StringUtils;
import net.sf.json.JSONObject;
import org.apache.poi.poifs.filesystem.EntryUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 魅族SDK
 * Created by lizhong on 2018/1/29.
 */
public class MeizuSDK implements ISDKScript{

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {
        try{

            JSONObject json = JSONObject.fromObject(extension);

            String app_id = channel.getCpAppID();

            final String uid = json.getString("uid");
            String session_id = json.getString("session");
            String ts = "" + System.currentTimeMillis();
            String sign_type = "md5";

            StringBuilder sb = new StringBuilder();
            sb.append("app_id=").append(app_id).append("&")
                    .append("session_id=").append(session_id).append("&")
                    .append("ts=").append(ts).append("&")
                    .append("uid=").append(uid).append(":").append(channel.getCpAppSecret());

            String sign = EncryptUtils.md5(sb.toString());

            Map<String,String> params = new HashMap<String, String>();
            params.put("app_id", app_id);
            params.put("session_id", session_id);
            params.put("uid", uid);
            params.put("ts", ts);
            params.put("sign_type",sign_type);
            params.put("sign", sign);

            String url = channel.getChannelAuthUrl();

            UHttpAgent.getInstance().post(url, params, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {
                    try {
                        Log.e("The auth result is " + result);

                        JSONObject json = JSONObject.fromObject(result);
                        int code = json.getInt("code");
                        if(code == 200){
                            callback.onSuccess(new SDKVerifyResult(true, uid, uid, uid));
                            return;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + result);
                }

                @Override
                public void failed(String e) {
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + e);
                }


            });

        }catch (Exception e){
            e.printStackTrace();
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is "+e.getMessage());
        }
    }

    @Override
    public void onGetOrderID(final UUser user,final UOrder order, final ISDKOrderListener callback) {
        if(callback != null){
            //获取订单相关信息返回给客户端
           /* if (null == user) {
                Log.e("魅族充值失败,用户信息为nul");
            }
            if (null == order) {
                Log.e("魅族充值失败,订单信息为nul");
            }
            String cp_order_id = order.getOrderID()+"";
            Long ts = System.currentTimeMillis();
            String sign_type = "md5";
            Integer channleID = order.getChannelID();
            UChannel channel = CacheManager.getInstance().getChannel(channleID);
            String app_id = channel.getCpAppID();
            String appSecret = channel.getCpAppSecret();
            StringBuilder sb = new StringBuilder();
            sb.append("app_id=").append(app_id)
                    .append("&cp_order_id=").append(cp_order_id)
                    .append("&ts=").append(ts).append(":"+appSecret);
            Log.i("魅族充值签名参数:"+sb.toString());
            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();
            Log.i("魅族充值签名:"+sign);
            Map<String,String> params = new HashMap<>();
            params.put("app_id",app_id+"");
            params.put("cp_order_id",cp_order_id);
            params.put("ts",ts+"");
            params.put("sign_type",sign_type);
            params.put("sign",sign);
            UHttpAgent.getInstance().post(channel.getChannelOrderUrl(), params,new UHttpFutureCallback() {

                @Override
                public void completed(String content) {
                    JSONObject object = JSONObject.fromObject(content);
                    if (200 == object.get("code")) {
                        JSONObject jsonObject = object.getJSONObject("value");
                        String backSign = jsonObject.get("sign")+"";
                        StringBuilder sbs = new StringBuilder();
                        sbs.append("app_id=").append(jsonObject.get("app_id"))
                                .append("&buy_amount=").append(jsonObject.get("buy_amount"))
                                .append("&cp_order_id=").append(jsonObject.get("cp_order_id"))
                                .append("&create_time=").append(jsonObject.get("create_time"))
                                .append("&pay_type=").append(jsonObject.get("pay_type"))
                                .append("&product_body=").append(jsonObject.get("product_body"))
                                .append("&product_id=").append(jsonObject.get("product_id"))
                                .append("&product_per_price=").append(jsonObject.get("product_per_price"))
                                .append("&product_subject=").append(jsonObject.get("product_subject"))
                                .append("&product_unit=")
                                .append("&total_price=").append(jsonObject.get("total_price")+"")
                                .append("&uid=").append(jsonObject.get("uid"))
                                .append("&user_info=");
                        String createSign = EncryptUtils.md5(sbs.toString()).toLowerCase();
                        Log.i(sbs.toString());
                        Log.i("请求支付生成的sign="+createSign);
                        if (!StringUtils.isEmpty(backSign) && !StringUtils.isEmpty(createSign)) {
                            if (!backSign.equals(createSign)) {
                                Log.e("魅族充值验签没有通过,返回的sign="+backSign+",验签生成的sign="+createSign);
                                callback.onFailed("魅族充值验签失败");
                            }
                        }
                        String notifyUrl = user.getChannel().getPayCallbackUrl();
                        Long createTime = order.getCreatedTime().getTime();
                        JSONObject backObject = new JSONObject();
                        backObject.put("notifyUrl",notifyUrl);
                        backObject.put("createTime",createTime);
                        backObject.put("sign",createSign);
                        callback.onSuccess(backObject.toString());
                        return;
                    }
                    callback.onFailed("魅族充值失败,code is not 200");
                }

                @Override
                public void failed(String err) {
                    Log.e("魅族充值失败，返回的信息:"+err);
                    callback.onFailed(err);
                }
            });*/
           UChannel channel = user.getChannel();
           String app_id = channel.getCpAppID();
           String orderId = order.getOrderID()+"";
           String create_time = order.getCreatedTime().getTime()+"";
            StringBuilder sb = new StringBuilder();
            sb.append("app_id=").append(app_id)
                    .append("&buy_amount=").append("1")
                    .append("&cp_order_id=").append(orderId)
                    .append("&create_time=").append(create_time)
                    .append("&pay_type=").append("0")
                    .append("&product_body=").append(order.getProductName())
                    .append("&product_id=").append(order.getProductID())
                    .append("&product_per_price=").append(order.getMoney()/100)
                    .append("&product_subject=").append(order.getProductName())
                    .append("&product_unit=")
                    .append("&total_price=").append(order.getMoney()/100)
                    .append("&uid=").append(user.getChannelUserID())
                    .append("&user_info=").append(":"+channel.getCpAppSecret());
            Log.i("魅族充值验签参数:"+sb.toString());
            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();
            Log.i("魅族充值sign"+sign);
            String notifyUrl = channel.getPayCallbackUrl();
            Long createTime = order.getCreatedTime().getTime();
            JSONObject backObject = new JSONObject();
            backObject.put("notifyUrl",notifyUrl);
            backObject.put("createTime",createTime);
            backObject.put("sign",sign);
            callback.onSuccess(backObject.toString());


        }
    }
}
