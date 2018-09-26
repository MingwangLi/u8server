package com.u8.server.sdk.yunwa;

import com.u8.server.cache.UApplicationContext;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.service.UChannelManager;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YunWaSDK implements ISDKScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void verify(UChannel channel, String extension, final ISDKVerifyListener callback) throws Exception {
        logger.info("----云哇SDK登陆认证获取参数:{}",extension);
        try {
            JSONObject object = JSONObject.fromObject(extension);
            String id = object.getString("id");
            String token = object.getString("token");
            //JSONObject dataObect = object.getJSONObject("date");
            String string = object.getString("date");
            String data = string.substring(1,string.length()-1);
            String key = channel.getCpAppKey();
            logger.info("----云蛙登陆认证签名体:{}",id+"|"+token+"|"+string+"|"+key);
            String sign = EncryptUtils.md5(id+"|"+token+"|"+string+"|"+key).toLowerCase();
            object.put("data",string);
            object.put("sign",sign);
            String cp_id = JSONObject.fromObject(data).getString("cp_id");
            String channel_id = object.getString("channel_id");
            //UChannelManager uChannelManager = UApplicationContext.getBean("uChannelManager",UChannelManager.class);
            //UChannelManager channelManager = (UChannelManager) UApplicationContext.getBean("uchannelManager");  被命名了channelManager  默认是uchannelManager
            UChannelManager channelManager = (UChannelManager) UApplicationContext.getBean("channelManager");
            //这两个信息存入数据库中 支付的时候要用到
            channel.setCpID(cp_id);
            channel.setCpPayID(channel_id);
            channelManager.saveChannel(channel);  //同步更新缓存
            String url = channel.getChannelAuthUrl()+"/"+cp_id+"/"+channel_id+"/Login";
            logger.debug("----云蛙登陆认证url:{}",url);
            logger.debug("----云蛙登陆参数:{}",object.toString());
            UHttpAgent.getInstance().post_json(url, object.toString(), new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----云蛙登陆认证返回数据:{}",content);
                    JSONObject json = JSONObject.fromObject(content);
                    int code = json.getInt("code");
                    if (0 == code) {
                        String id = json.getString("id");
                        String nick = json.getString("nick");
                        callback.onSuccess(new SDKVerifyResult(true,id,nick,nick));
                        return;
                    }
                    callback.onFailed(content);
                }

                @Override
                public void failed(String err) {
                    callback.onFailed(err);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----云蛙登陆认证异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, final ISDKOrderListener callback) throws Exception {
        try {
            final UChannel channel = order.getChannel();
            String cp_id = channel.getCpID();
            final String channel_id = channel.getCpPayID();
            String url = channel.getChannelAuthUrl()+"/"+cp_id+"/"+channel_id+"/SaveOrder";
            JSONObject object = new JSONObject();
            object.put("cporder",order.getOrderID()+"");
            JSONObject data = new JSONObject();
            data.put("itemid",order.getProductID());
            data.put("itemname",order.getProductName());
            data.put("price",order.getMoney()+"");
            object.put("data",data.toString());
            logger.debug("----云蛙获取订单data:{}",data.toString());
            object.put("uid",order.getUserID()+"");
            object.put("vertifyurl","");
            object.put("notifyurl",channel.getPayCallbackUrl());
            logger.debug("----云蛙获取订单签名体:{}",order.getOrderID()+"|"+data.toString()+"|"+channel.getCpAppKey());
            String sign = EncryptUtils.md5(order.getOrderID()+"|"+data.toString()+"|"+channel.getCpAppKey()).toLowerCase();
            object.put("sign",sign);
            logger.debug("----云蛙支付获取订单url:{},param:{}",url,object.toString());
            UHttpAgent.getInstance().post_json(url, object.toString(), new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    logger.info("----云蛙获取订单号返回数据:{}",content);
                    JSONObject json = JSONObject.fromObject(content);
                    int code = json.getInt("code");
                    if (0 == code) {
                        callback.onSuccess(channel.getPayCallbackUrl());
                        return;
                    }
                    callback.onFailed(content);
                }

                @Override
                public void failed(String err) {
                    callback.onFailed(err);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----云蛙获取订单异常:{}",e.getMessage());
            callback.onFailed(e.getMessage());
        }

    }
}
